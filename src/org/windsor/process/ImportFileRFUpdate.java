/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 *****************************************************************************/
package org.windsor.process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.compiere.model.MRequisitionLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.*;
import org.windsor.model.X_I_ReservaReq;

/**
 * Actualiza pre-reservas (M_RequisitionLine) desde archivo CSV.
 * Formato CSV: codigo;cantidad;bloquear(S/N)
 *
 * FIX 2026-06-11 (FG):
 * - Cargar staging en memoria antes de DML (evita cursor abierto durante save)
 * - Formula unificada reemplaza 350 lineas de if/else anidado
 * - new_QtyReserved = MAX(0, MIN(current+delta, disponible+current))
 *   donde (disponible+current) = backing fisico real de esta reserva
 * - DB.executeUpdate() en lugar de MRequisitionLine.save() para updates
 *
 * @author ininoles
 */
public class ImportFileRFUpdate extends SvrProcess
{
	private String p_PathFile;
	private int p_requisition_id;

	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("archivo"))
				p_PathFile = para[i].getParameter().toString();
			else if (name.equals("M_Requisition_ID"))
				p_requisition_id = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	}

	protected String doIt() throws Exception
	{
		// =====================================================================
		// PASO 1: Leer CSV y cargar staging I_ReservaReq
		// =====================================================================
		String[] datosFStr = new String[4];
		FileInputStream fis = new FileInputStream(p_PathFile);
		InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
		BufferedReader br = new BufferedReader(isr);
		String linea = br.readLine();
		int cantLine = 0;
		int no = 0;
		String clientCheck = " and ad_client_ID=1000000";

		StringBuffer sql = new StringBuffer("delete from I_ReservaReq where ad_org_ID=1000000 ").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Borradas:" + no);

		while (linea != null)
		{
			log.config(linea);
			if (cantLine >= 0)
			{
				datosFStr = linea.split(";");
				if (datosFStr[0] != null || datosFStr[1] != null)
				{
					String bloquear = "N";
					String codigo = datosFStr[0];
					int cantidad = Integer.parseInt(datosFStr[1].toString().trim().replace(".", ""));
					if (datosFStr[2] != null)
					{
						if (datosFStr[2].toUpperCase().equals("S") || datosFStr[2].toUpperCase().equals("Y"))
							bloquear = "Y";
						else
							bloquear = "N";
					}
					else
						bloquear = "N";

					X_I_ReservaReq rq = new X_I_ReservaReq(getCtx(), 0, get_TrxName());
					rq.setValue(codigo);
					rq.setQtyEntered(cantidad);
					rq.set_ValueOfColumn("archivo", p_PathFile);
					rq.set_ValueOfColumn("M_Requisition_ID", p_requisition_id);
					rq.set_ValueOfColumn("Bloquear", bloquear);
					rq.save();

					String sqlp = "Select coalesce(max(m_product_ID),0) from m_product " +
						"where isactive='Y' and ad_client_ID=1000000 and upper(trim(value))='" +
						codigo.trim().toUpperCase() + "'";
					int product_id = Integer.parseInt(DB.getSQLValueString(null, sqlp));
					int disponible = 0;

					if (product_id >= 1000000)
					{
						rq.setM_Product_ID(product_id);
						disponible = Integer.parseInt(DB.getSQLValueString(get_TrxName(),
							"Select qtyavailableopenviaNum(?) FROM dual", product_id));
						rq.setQtyAvailable(disponible);
						log.config("disponible:" + disponible);

						// Buscar linea de requisicion existente para este producto
						int rl_id = Integer.parseInt(DB.getSQLValueString(null,
							"Select coalesce(max(m_requisitionline_ID),0) from m_requisitionline " +
							"where isactive='Y' and LIBERADA<>'Y' " +
							"and m_requisition_id=" + p_requisition_id +
							" and m_product_ID=" + product_id));
						log.config("RL_ID:" + rl_id);

						if (rl_id > 0)
						{
							rq.set_CustomColumn("M_RequisitionLine_ID", rl_id);
							MRequisitionLine rl = new MRequisitionLine(getCtx(), rl_id, get_TrxName());
							rq.set_CustomColumn("Qty", rl.getQty());

							int usedr = Integer.parseInt(DB.getSQLValueString(null,
								"Select coalesce(sum(qtyused),0) from m_requisitionline " +
								"where isactive='Y' and LIBERADA<>'Y' and m_requisitionline_id=" + rl_id));
							rq.set_CustomColumn("QtyUsed", new BigDecimal(usedr));

							usedr = Integer.parseInt(DB.getSQLValueString(null,
								"select coalesce(sum(OL.QTYentered),0) " +
								"from c_orderline ol " +
								"inner join c_order o on (ol.c_order_ID=o.c_order_ID) " +
								"where o.docstatus not in ('VO') " +
								"and ol.m_requisitionline_ID=" + rl_id));
							rq.set_CustomColumn("QtyOrdered", new BigDecimal(usedr));

							usedr = Integer.parseInt(DB.getSQLValueString(null,
								"Select coalesce(sum(qtyreserved),0) from m_requisitionline " +
								"where isactive='Y' and LIBERADA<>'Y' and m_requisitionline_id=" + rl_id));
							rq.set_CustomColumn("QtyReserved", new BigDecimal(usedr));
						}
						rq.save();
					}
					else
					{
						rq.setQtyAvailable(disponible);
						rq.setERROR("Producto no encontrado, el codigo no existe en el sistema o no esta activo");
						rq.save();
					}
				}
			}
			linea = br.readLine();
			cantLine++;
		}
		br.close();

		// =====================================================================
		// PASO 2: Actualizar lineas existentes en M_RequisitionLine
		// Carga en memoria primero (cursor cerrado), luego DML.
		//
		// FORMULA UNIFICADA (FIX 2026-06-11):
		// maxReservable = disponible + currentReserved
		//   (qtyavailableopenvianum ya descontó currentReserved → sumarlo devuelve
		//    el backing fisico real para esta linea de reserva)
		//
		// Si delta <= 0 (reducir): new = MAX(0, current + delta)
		// Si delta >  0 (aumentar): new = MIN(current + delta, MAX(0, maxReservable))
		//   → previene que la reserva supere el stock fisico disponible
		//
		// new_Qty = QtyOrdered + new_QtyReserved
		// =====================================================================
		String sqlExistente =
			"SELECT I_ReservaReq_ID, M_RequisitionLine_ID, " +
			"QtyEntered, QtyAvailable, Qty, QtyOrdered, QtyReserved, QtyUsed, bloquear " +
			"FROM I_ReservaReq " +
			"WHERE m_product_id IS NOT NULL AND m_requisitionline_ID IS NOT NULL";

		ArrayList filasExistentes = new ArrayList();
		PreparedStatement pstmt = DB.prepareStatement(sqlExistente, get_TrxName());
		ResultSet rs = pstmt.executeQuery();
		while (rs.next())
		{
			HashMap fila = new HashMap();
			fila.put("I_ReservaReq_ID",       new Integer(rs.getInt("I_ReservaReq_ID")));
			fila.put("M_RequisitionLine_ID",   new Integer(rs.getInt("M_RequisitionLine_ID")));
			fila.put("QtyEntered",             new Integer(rs.getInt("QtyEntered")));
			fila.put("QtyAvailable",           new Integer(rs.getInt("QtyAvailable")));
			fila.put("QtyOrdered",             new Integer(rs.getInt("QtyOrdered")));
			fila.put("QtyReserved",            new Integer(rs.getInt("QtyReserved")));
			String bl = rs.getString("bloquear");
			fila.put("bloquear", bl == null ? "N" : bl);
			filasExistentes.add(fila);
		}
		rs.close();
		pstmt.close();

		log.config("Paso 2: " + filasExistentes.size() + " lineas con RL a actualizar");

		// Rastrear consumo acumulado por RL dentro del paso 2 para evitar over-reservation
		// cuando hay multiples filas del mismo producto
		HashMap consumoAcumulado = new HashMap();

		for (int i = 0; i < filasExistentes.size(); i++)
		{
			HashMap fila = (HashMap) filasExistentes.get(i);
			int rq_id          = ((Integer) fila.get("I_ReservaReq_ID")).intValue();
			int rl_id          = ((Integer) fila.get("M_RequisitionLine_ID")).intValue();
			int delta          = ((Integer) fila.get("QtyEntered")).intValue();
			int disponibleOrig = ((Integer) fila.get("QtyAvailable")).intValue();
			int currentReserved= ((Integer) fila.get("QtyReserved")).intValue();
			int qtyOrdered     = ((Integer) fila.get("QtyOrdered")).intValue();
			String bloquear    = (String) fila.get("bloquear");

			// Restar lo que ya consumio esta RL en iteraciones previas
			Integer yaConsumido = (Integer) consumoAcumulado.get(new Integer(rl_id));
			int consumo = (yaConsumido != null) ? yaConsumido.intValue() : 0;
			int disponible = disponibleOrig - consumo;

			int newReserved;
			String msg;

			if (delta <= 0)
			{
				// Reducir reserva: resta delta (es negativo), piso en 0
				newReserved = Math.max(0, currentReserved + delta);
				msg = "Reserva reducida " + currentReserved + "->" + newReserved;
			}
			else
			{
				// Aumentar reserva: limitado por backing fisico
				// maxReservable: si disponible ya es negativo (reserva obsoleta),
				// limita el aumento para no empeorar el disponible global
				int maxReservable = Math.max(0, disponible + currentReserved);
				newReserved = Math.min(currentReserved + delta, maxReservable);
				if (newReserved > currentReserved)
					msg = "Reserva aumentada " + currentReserved + "->" + newReserved;
				else
					msg = "Sin disponible para aumentar (disp=" + disponible + ", max=" + maxReservable + ")";

				// Acumular consumo para la siguiente fila del mismo RL
				int nuevoConsumo = consumo + (newReserved - currentReserved);
				consumoAcumulado.put(new Integer(rl_id), new Integer(nuevoConsumo));
			}

			int newQty = qtyOrdered + newReserved;

			DB.executeUpdate(
				"UPDATE M_RequisitionLine SET " +
				"QtyReserved=" + newReserved + ", " +
				"Qty=" + newQty + ", " +
				"QtyUsed=" + qtyOrdered + ", " +
				"Bloquear='" + bloquear + "' " +
				"WHERE M_RequisitionLine_ID=" + rl_id + " AND AD_Client_ID=1000000",
				get_TrxName());

			DB.executeUpdate(
				"UPDATE I_ReservaReq SET Processed='Y', Msg='" + msg.replaceAll("'", "") + "' " +
				"WHERE I_ReservaReq_ID=" + rq_id,
				get_TrxName());

			log.config("RL=" + rl_id + " " + msg +
				" delta=" + delta + " disp=" + disponible + " (orig=" + disponibleOrig + ", consumo=" + consumo + ")" +
				" qtyOrd=" + qtyOrdered);
		}

		// =====================================================================
		// PASO 3: Crear nuevas lineas (producto existe pero sin RL en la requisicion)
		// Solo crea si disponible > 0 y delta > 0.
		// qty nueva = MIN(delta, disponible) para no sobre-reservar.
		// =====================================================================
		String sqlNuevas =
			"SELECT I_ReservaReq_ID, M_Product_ID, QtyEntered, QtyAvailable, bloquear " +
			"FROM I_ReservaReq " +
			"WHERE m_product_id IS NOT NULL AND m_requisitionline_ID IS NULL";

		ArrayList filasNuevas = new ArrayList();
		pstmt = DB.prepareStatement(sqlNuevas, get_TrxName());
		rs = pstmt.executeQuery();
		while (rs.next())
		{
			HashMap fila = new HashMap();
			fila.put("I_ReservaReq_ID", new Integer(rs.getInt("I_ReservaReq_ID")));
			fila.put("M_Product_ID",    new Integer(rs.getInt("M_Product_ID")));
			fila.put("QtyEntered",      new Integer(rs.getInt("QtyEntered")));
			fila.put("QtyAvailable",    new Integer(rs.getInt("QtyAvailable")));
			String bl = rs.getString("bloquear");
			fila.put("bloquear", bl == null ? "N" : bl);
			filasNuevas.add(fila);
		}
		rs.close();
		pstmt.close();

		log.config("Paso 3: " + filasNuevas.size() + " productos sin RL existente");

		for (int i = 0; i < filasNuevas.size(); i++)
		{
			HashMap fila = (HashMap) filasNuevas.get(i);
			int rq_id      = ((Integer) fila.get("I_ReservaReq_ID")).intValue();
			int product_id = ((Integer) fila.get("M_Product_ID")).intValue();
			int delta      = ((Integer) fila.get("QtyEntered")).intValue();
			int disponible = ((Integer) fila.get("QtyAvailable")).intValue();
			String bloquear= (String) fila.get("bloquear");

			log.config("Sin RL: producto=" + product_id + " disponible=" + disponible + " delta=" + delta);

			if (disponible > 0 && delta > 0)
			{
				int qtyNueva = Math.min(delta, disponible);
				int um_id = Integer.parseInt(DB.getSQLValueString(null,
					"Select coalesce(max(C_uom_ID),100) from m_product where m_product_ID=" + product_id));

				MRequisitionLine mrl = new MRequisitionLine(getCtx(), 0, get_TrxName());
				mrl.setM_Requisition_ID(p_requisition_id);
				mrl.setM_Product_ID(product_id);
				mrl.setQty(new BigDecimal(qtyNueva));
				mrl.set_CustomColumn("QtyReserved", new BigDecimal(qtyNueva));
				mrl.set_CustomColumn("QtyUsed", new BigDecimal(0));
				mrl.setLine(10);
				mrl.set_CustomColumn("Bloquear", bloquear);
				mrl.setC_UOM_ID(um_id);
				mrl.save();

				DB.executeUpdate(
					"UPDATE I_ReservaReq SET Processed='Y', " +
					"Msg='Nueva linea creada qty=" + qtyNueva + "' " +
					"WHERE I_ReservaReq_ID=" + rq_id,
					get_TrxName());
				log.config("Nueva RL creada: producto=" + product_id + " qty=" + qtyNueva);
			}
			else
			{
				String msg;
				if (disponible <= 0)
					msg = "Sin disponible (disp=" + disponible + ")";
				else
					msg = "Cantidad solicitada <= 0";

				DB.executeUpdate(
					"UPDATE I_ReservaReq SET Processed='Y', " +
					"Msg='" + msg.replaceAll("'", "") + "' " +
					"WHERE I_ReservaReq_ID=" + rq_id,
					get_TrxName());
				log.config("Skipped producto=" + product_id + ": " + msg);
			}
		}

		return "Procesado: Recuerde Revisar Informe Actualiza Pre-Reserva";
	}
}
