/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 *****************************************************************************/
package org.cwindsor.model;

import java.math.BigDecimal;

import org.compiere.model.MClient;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 * Bloquea el completar de un Inventario Fisico o un Movimiento de bodega si
 * la operacion resta stock de Lampa (1000001) o Abastecimiento (1000010) --
 * las unicas bodegas que cuenta qtyavailableopenvianum() -- y deja el
 * disponible resultante negativo para un producto que tiene una reserva
 * fisica activa (M_RequisitionLine.QtyReserved > 0, requisicion CO, no
 * liberada).
 *
 * Bodegas fuera de Lampa/Abastecimiento (ej. Reproceso 1000019) quedan fuera
 * a proposito, igual que en la formula: no cuentan para este chequeo.
 *
 * TIMING_AFTER_COMPLETE: M_Storage ya esta actualizado por completeIt() de
 * Adempiere antes de que se grabe el cambio de DocStatus (ver
 * HANDOFF_negativos_901541_20260824.md seccion 3), asi que qtyavailableopenvianum
 * ya ve el resultado final del documento. Devolver un String no-null aca
 * aborta la operacion y hace rollback completo del documento.
 *
 * FIX 2026-08-31 (FG)
 */
public class ModWindsorValidaDispNegativo implements ModelValidator
{
	private static CLogger log = CLogger.getCLogger(ModWindsorValidaDispNegativo.class);
	private int m_AD_Client_ID = -1;

	private static final int WH_LAMPA = 1000001;
	private static final int WH_ABASTECIMIENTO = 1000010;

	public ModWindsorValidaDispNegativo()
	{
		super();
	}

	public void initialize(ModelValidationEngine engine, MClient client)
	{
		if (client != null)
		{
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		}
		else
		{
			log.info("Initializing global validator: " + this.toString());
		}

		engine.addDocValidate(MInventory.Table_Name, this);
		engine.addDocValidate(MMovement.Table_Name, this);
	}

	public String modelChange(PO po, int type) throws Exception
	{
		return null;
	}

	public String docValidate(PO po, int timing)
	{
		if (timing != TIMING_AFTER_COMPLETE)
			return null;

		if (po.get_Table_ID() == MInventory.Table_ID)
			return validarInventario((MInventory) po);
		if (po.get_Table_ID() == MMovement.Table_ID)
			return validarMovimiento((MMovement) po);

		return null;
	}

	private String validarInventario(MInventory inv)
	{
		MInventoryLine[] lines = inv.getLines(false);
		StringBuilder error = new StringBuilder();

		for (int i = 0; i < lines.length; i++)
		{
			MInventoryLine line = lines[i];
			int whId = getWarehouseOfLocator(line.getM_Locator_ID());
			if (whId != WH_LAMPA && whId != WH_ABASTECIMIENTO)
				continue; // fuera de Lampa/Abastecimiento, no cuenta

			BigDecimal delta = nz(line.getQtyCount()).subtract(nz(line.getQtyBook()));
			if (delta.compareTo(BigDecimal.ZERO) >= 0)
				continue; // solo bloquea si esta linea RESTA stock

			checkProducto(line.getM_Product_ID(), inv.get_TrxName(), error);
		}
		return error.length() > 0 ? error.toString() : null;
	}

	private String validarMovimiento(MMovement mov)
	{
		MMovementLine[] lines = mov.getLines(false);
		StringBuilder error = new StringBuilder();

		for (int i = 0; i < lines.length; i++)
		{
			MMovementLine line = lines[i];
			int whFrom = getWarehouseOfLocator(line.getM_Locator_ID());
			int whTo = getWarehouseOfLocator(line.getM_LocatorTo_ID());
			boolean fromCounted = (whFrom == WH_LAMPA || whFrom == WH_ABASTECIMIENTO);
			boolean toCounted = (whTo == WH_LAMPA || whTo == WH_ABASTECIMIENTO);

			if (!fromCounted)
				continue; // no sale de una bodega contada, no puede empeorar el disponible ahi
			if (toCounted)
				continue; // traslado interno Lampa<->Abastecimiento: neto cero, no valida

			// Sale de Lampa/Abastecimiento hacia una bodega NO contada (ej. Reproceso):
			// resta disponible real sin que entre nada de vuelta.
			checkProducto(line.getM_Product_ID(), mov.get_TrxName(), error);
		}
		return error.length() > 0 ? error.toString() : null;
	}

	private void checkProducto(int productId, String trxName, StringBuilder error)
	{
		BigDecimal reservado = DB.getSQLValueBD(trxName,
			"SELECT COALESCE(SUM(rl.QtyReserved),0) FROM M_RequisitionLine rl" +
			" INNER JOIN M_Requisition r ON r.M_Requisition_ID = rl.M_Requisition_ID" +
			" WHERE rl.M_Product_ID = ? AND r.DocStatus = 'CO' AND NVL(rl.Liberada,'N') <> 'Y'",
			productId);
		if (reservado == null || reservado.compareTo(BigDecimal.ZERO) <= 0)
			return; // sin reserva fisica activa, no hay nada que proteger

		BigDecimal disponible = DB.getSQLValueBD(trxName,
			"SELECT qtyavailableopenvianum(?) FROM DUAL", productId);
		if (disponible != null && disponible.compareTo(BigDecimal.ZERO) < 0)
		{
			String codigo = DB.getSQLValueString(trxName, "SELECT Value FROM M_Product WHERE M_Product_ID=" + productId);
			error.append("Producto ").append(codigo)
				.append(" queda con disponible negativo (").append(disponible)
				.append(") y tiene reserva fisica activa (").append(reservado).append("). ");
		}
	}

	private int getWarehouseOfLocator(int locatorId)
	{
		if (locatorId <= 0)
			return -1;
		return DB.getSQLValue(null, "SELECT M_Warehouse_ID FROM M_Locator WHERE M_Locator_ID=" + locatorId);
	}

	private BigDecimal nz(BigDecimal v)
	{
		return v == null ? Env.ZERO : v;
	}

	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		return null;
	}

	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}

	public String toString()
	{
		return "ModWindsorValidaDispNegativo";
	}
}
