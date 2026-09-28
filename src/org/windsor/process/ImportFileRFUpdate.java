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
import org.compiere.util.Trx;
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

		// FIX 2026-08-27 (FG): Paso 1 en su propia transaccion, comiteada al terminar.
		// Antes usaba get_TrxName() (la transaccion del proceso, que solo comitea cuando
		// TODO doIt() termina). Paso 2 abre despues una transaccion separada (batchTrx2)
		// para marcar Processed='Y' sobre estas mismas filas -- si aun no estaban
		// comiteadas, esa segunda transaccion choca en lock con la principal y el UPDATE
		// de Processed queda pisado en silencio (el dato de M_RequisitionLine SI se graba
		// bien porque usa batchTrx2 de punta a punta; solo el bookkeeping de Processed
		// se pierde). Comiteando Paso 1 antes de arrancar Paso 2 se elimina el conflicto.
		Trx batchTrx1 = Trx.get(Trx.createTrxName("RF_P1"), true);

		StringBuffer sql = new StringBuffer("delete from I_ReservaReq where ad_org_ID=1000000 ").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), batchTrx1.getTrxName());
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

					X_I_ReservaReq rq = new X_I_ReservaReq(getCtx(), 0, batchTrx1.getTrxName());
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
						disponible = Integer.parseInt(DB.getSQLValueString(batchTrx1.getTrxName(),
							"Select qtyavailableopenviaNum(?) FROM dual", product_id));
						rq.setQtyAvailable(disponible);
						log.config("disponible:" + disponible);

						// Buscar linea de requisicion existente para este producto
						// FIX 2026-07-07: incluir LIBERADA='Y' para reutilizar y desliberar
						// Preferencia: linea no liberada primero, luego liberada
						int rl_id = Integer.parseInt(DB.getSQLValueString(null,
							"SELECT COALESCE(MAX(CASE WHEN NVL(LIBERADA,'N')<>'Y' THEN m_requisitionline_ID END)," +
							" MAX(m_requisitionline_ID), 0) " +
							"FROM m_requisitionline " +
							"WHERE isactive='Y' " +
							"AND m_requisition_id=" + p_requisition_id +
							" AND m_product_ID=" + product_id));
						log.config("RL_ID:" + rl_id);

						if (rl_id > 0)
						{
							rq.set_CustomColumn("M_RequisitionLine_ID", rl_id);
							MRequisitionLine rl = new MRequisitionLine(getCtx(), rl_id, batchTrx1.getTrxName());
							rq.set_CustomColumn("Qty", rl.getQty());

							int usedr = Integer.parseInt(DB.getSQLValueString(null,
								"Select coalesce(sum(qtyused),0) from m_requisitionline " +
								"where isactive='Y' and LIBERADA<>'Y' and m_requisitionline_id=" + rl_id));
							rq.set_CustomColumn("QtyUsed", new BigDecimal(usedr));

							// FIX 2026-07-09 (FG): formula correcta QtyUsed = SUM(QtyDelivered CO) + SUM(QtyReserved CO/IP)
						// QtyDelivered CO: lo entregado en OVs aun abiertas (CO)
						// QtyReserved CO/IP: lo aun comprometido pendiente de entrega
						int usedDeliveredCO = Integer.parseInt(DB.getSQLValueString(null,
								"select coalesce(sum(NVL(OL.QtyDelivered,0)),0) " +
								"from c_orderline ol " +
								"inner join c_order o on (ol.c_order_ID=o.c_order_ID) " +
								"where o.docstatus = 'CO' " +
								"and ol.m_requisitionline_ID=" + rl_id));
						int usedReservedCOIP = Integer.parseInt(DB.getSQLValueString(null,
								"select coalesce(sum(NVL(OL.QtyReserved,0)),0) " +
								"from c_orderline ol " +
								"inner join c_order o on (ol.c_order_ID=o.c_order_ID) " +
								"where o.docstatus in ('CO','IP') " +
								"and ol.m_requisitionline_ID=" + rl_id));
						usedr = usedDeliveredCO + usedReservedCOIP;
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

		// FIX 2026-08-27 (FG): comitear y cerrar el staging de Paso 1 ANTES de que Paso 2
		// abra su propia transaccion sobre las mismas filas de I_ReservaReq (ver nota mas
		// arriba en la declaracion de batchTrx1).
		batchTrx1.commit();
		batchTrx1.close();
		log.config("Paso 1: staging comiteado (" + cantLine + " lineas leidas)");

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
			"SELECT I_ReservaReq_ID, M_RequisitionLine_ID, M_Product_ID, " +
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
			fila.put("M_Product_ID",           new Integer(rs.getInt("M_Product_ID")));
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

		// FIX 2026-07-09 (FG): procesar en lotes de 100 con commit intermedio
		// Evita transacciones enormes y reduce tiempo de lock en M_RequisitionLine.
		final int BATCH_SIZE = 100;
		Trx batchTrx2 = null;
		int procesados2 = 0;
		try
		{
		batchTrx2 = Trx.get(Trx.createTrxName("RF_P2"), true);

		for (int i = 0; i < filasExistentes.size(); i++)
		{
			HashMap fila = (HashMap) filasExistentes.get(i);
			int rq_id          = ((Integer) fila.get("I_ReservaReq_ID")).intValue();
			int rl_id          = ((Integer) fila.get("M_RequisitionLine_ID")).intValue();
			int product_id     = ((Integer) fila.get("M_Product_ID")).intValue();
			int delta          = ((Integer) fila.get("QtyEntered")).intValue();
			int disponibleOrig = ((Integer) fila.get("QtyAvailable")).intValue();
			int currentReservedStale = ((Integer) fila.get("QtyReserved")).intValue();
			int qtyOrdenadoStale = ((Integer) fila.get("QtyOrdered")).intValue();
			String bloquear    = (String) fila.get("bloquear");

			// FIX 2026-08-27 (FG): tanto QtyReserved como QtyOrdered en el staging son una
			// foto tomada en Paso 1, linea por linea mientras se lee el CSV (puede tardar
			// minutos con archivos grandes). Si en el medio se completa/vence otra OV de la
			// misma reqline (dispara ModWindsorUpdateReserved), ambos valores quedan
			// desactualizados y Qty/QtyReserved/QtyUsed se calcularian mal. Se revalidan en
			// vivo justo antes de aplicar el cambio.
			int currentReserved = Integer.parseInt(DB.getSQLValueString(batchTrx2.getTrxName(),
				"SELECT NVL(QtyReserved,0) FROM M_RequisitionLine WHERE M_RequisitionLine_ID=" + rl_id));
			if (currentReserved != currentReservedStale)
				log.config("RL=" + rl_id + " QtyReserved desactualizado en staging: stale=" + currentReservedStale +
						" real=" + currentReserved + " -> se usa el real");

			// misma formula de Paso 1: QtyDelivered(CO) + QtyReserved(CO,IP) de C_OrderLine.
			int qtyOrdered = Integer.parseInt(DB.getSQLValueString(batchTrx2.getTrxName(),
				"SELECT COALESCE(SUM(CASE WHEN o.DocStatus='CO' THEN NVL(ol.QtyDelivered,0) ELSE 0 END),0) + " +
				"COALESCE(SUM(CASE WHEN o.DocStatus IN ('CO','IP') THEN NVL(ol.QtyReserved,0) ELSE 0 END),0) " +
				"FROM C_OrderLine ol INNER JOIN C_Order o ON (o.C_Order_ID = ol.C_Order_ID) " +
				"WHERE ol.M_RequisitionLine_ID=" + rl_id));
			if (qtyOrdered != qtyOrdenadoStale)
				log.config("RL=" + rl_id + " QtyUsed desactualizado en staging: stale=" + qtyOrdenadoStale +
						" real=" + qtyOrdered + " -> se usa el real");

			// Restar lo que ya consumio esta RL en iteraciones previas
			Integer yaConsumido = (Integer) consumoAcumulado.get(new Integer(rl_id));
			int consumo = (yaConsumido != null) ? yaConsumido.intValue() : 0;
			int disponible = disponibleOrig - consumo;

			int newReserved;
			String msg;

			if (delta <= 0)
			{
				// Reducir reserva: resta delta (es negativo), piso en 0
				// Bajar la reserva jamas puede dejar el disponible mas negativo -- no hace
				// falta validar disponible en esta rama.
				newReserved = Math.max(0, currentReserved + delta);
				msg = "Reserva reducida " + currentReserved + "->" + newReserved;
			}
			else
			{
				// FIX 2026-08-27 (FG): unica rama riesgosa (sube la reserva). disponibleOrig
				// es la foto de Paso 1 (puede tener minutos de antiguedad); se revalida en
				// vivo para garantizar que, si la reserva queda positiva, el disponible
				// resultante nunca quede negativo.
				int disponibleReal = Integer.parseInt(DB.getSQLValueString(batchTrx2.getTrxName(),
					"SELECT qtyavailableopenvianum(" + product_id + ") FROM dual"));
				if (disponibleReal != disponibleOrig)
					log.config("RL=" + rl_id + " disponible desactualizado en staging: stale=" + disponibleOrig +
							" real=" + disponibleReal + " -> se usa el real");
				disponible = disponibleReal - consumo;

				// Aumentar reserva: limitado por backing fisico
				int maxReservable = Math.max(0, disponible + currentReserved);
				newReserved = Math.min(currentReserved + delta, maxReservable);
				if (newReserved > currentReserved)
					msg = "Reserva aumentada " + currentReserved + "->" + newReserved;
				else
					msg = "Sin disponible para aumentar (disp=" + disponible + ", max=" + maxReservable + ")";

				int nuevoConsumo = consumo + (newReserved - currentReserved);
				consumoAcumulado.put(new Integer(rl_id), new Integer(nuevoConsumo));
			}

			int newQty = qtyOrdered + newReserved;

			// FIX 2026-07-07: desliberar si estaba liberada
			// FIX 2026-08-27 (FG): tocar Updated/UpdatedBy -- el UPDATE directo bypasea
			// save() y dejaba el timestamp congelado (mismo bug que en
			// ModWindsorUpdateReserved.capReservaFisica, ver FIX_SUMMARY_QTYAVAIL_v1.md)
			DB.executeUpdate(
				"UPDATE M_RequisitionLine SET " +
				"QtyReserved=" + newReserved + ", " +
				"Qty=" + newQty + ", " +
				"QtyUsed=" + qtyOrdered + ", " +
				"Bloquear='" + bloquear + "', " +
				"LIBERADA='N', FECHALIBEReACION=NULL, ad_userref_ID=NULL, " +
				"Updated=SYSDATE, UpdatedBy=" + Env.getAD_User_ID(getCtx()) + " " +
				"WHERE M_RequisitionLine_ID=" + rl_id + " AND AD_Client_ID=1000000",
				batchTrx2.getTrxName());

			DB.executeUpdate(
				"UPDATE I_ReservaReq SET Processed='Y', Msg='" + msg.replaceAll("'", "") + "' " +
				"WHERE I_ReservaReq_ID=" + rq_id,
				batchTrx2.getTrxName());

			log.config("RL=" + rl_id + " " + msg +
				" delta=" + delta + " disp=" + disponible + " (orig=" + disponibleOrig + ", consumo=" + consumo + ")" +
				" qtyOrd=" + qtyOrdered);

			procesados2++;
			if (procesados2 % BATCH_SIZE == 0)
			{
				batchTrx2.commit();
				log.config("Paso 2: commit lote " + procesados2 + "/" + filasExistentes.size());
			}
		}
		batchTrx2.commit();
		log.config("Paso 2: commit final lote (" + procesados2 + " total)");
		}
		finally
		{
			if (batchTrx2 != null) batchTrx2.close();
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

		// FIX 2026-07-09 (FG): lotes de 100 con commit intermedio
		Trx batchTrx3 = null;
		int procesados3 = 0;
		try
		{
		batchTrx3 = Trx.get(Trx.createTrxName("RF_P3"), true);

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

				MRequisitionLine mrl = new MRequisitionLine(getCtx(), 0, batchTrx3.getTrxName());
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
					batchTrx3.getTrxName());
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
					batchTrx3.getTrxName());
				log.config("Skipped producto=" + product_id + ": " + msg);
			}

			procesados3++;
			if (procesados3 % BATCH_SIZE == 0)
			{
				batchTrx3.commit();
				log.config("Paso 3: commit lote " + procesados3 + "/" + filasNuevas.size());
			}
		}
		batchTrx3.commit();
		log.config("Paso 3: commit final lote (" + procesados3 + " total)");
		}
		finally
		{
			if (batchTrx3 != null) batchTrx3.close();
		}

		return "Procesado: Recuerde Revisar Informe Actualiza Pre-Reserva";
	}
}
