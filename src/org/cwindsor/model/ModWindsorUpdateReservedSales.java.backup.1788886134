/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.cwindsor.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.compiere.model.MClient;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_M_StorageReservation;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 * Validator Windsor — actualiza QtyReserved y QtyUsed en M_RequisitionLine
 * al completar OV, y QtyReserved en C_OrderLine al completar despacho.
 *
 * FIX vs OFB_360 original: el bloque AFTER_COMPLETE de MOrder ahora actualiza
 * TAMBIEN QtyUsed, manteniendo el invariante QtyReserved + QtyUsed = Qty.
 *
 * Migrado desde org.windsor.model.ModWindsorUpdateReservedSales (OFB_360)
 * a org.cwindsor.model para unificar en package_CW.
 *
 * @author Italo Niyoles / fix Fernando Gonzalez 2026-06
 */
public class ModWindsorUpdateReservedSales implements ModelValidator
{
	public ModWindsorUpdateReservedSales ()
	{
		super ();
	}

	private static CLogger log = CLogger.getCLogger(ModWindsorUpdateReservedSales.class);
	private int m_AD_Client_ID = -1;

	public void initialize (ModelValidationEngine engine, MClient client)
	{
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		} else {
			log.info("Initializing global validator: " + this.toString());
		}

		// Solo MInOut: el recalculo de M_RequisitionLine.QtyReserved/QtyUsed
		// en MOrder ya lo hace ModWindsorUpdateReserved (package_CW, seq=40).
		engine.addDocValidate(MInOut.Table_Name, this);
	}

	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: " + type);
		return null;
	}

	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: " + timing);

		// Al completar despacho (MInOut): actualiza QtyReserved en C_OrderLine
		// segun cantidad ya despachada. No toca M_RequisitionLine (correcto).
		if (timing == TIMING_AFTER_COMPLETE && po.get_Table_ID() == MInOut.Table_ID)
		{
			MInOut ship = (MInOut) po;
			if (ship.isSOTrx())
			{
				MInOutLine[] sLines = ship.getLines(false);
				for (int i = 0; i < sLines.length; i++)
				{
					MInOutLine sLine = sLines[i];
					if (sLine.getM_Product_ID() > 0)
					{
						if (sLine.getC_OrderLine_ID() > 0)
						{
							MOrderLine oLine = new MOrderLine(
									po.getCtx(),
									sLine.getC_OrderLine_ID(),
									po.get_TrxName());

							BigDecimal qtyDesp = DB.getSQLValueBD(po.get_TrxName(),
									"SELECT SUM(movementQty)" +
									" FROM M_InOutLine iol" +
									" INNER JOIN M_InOut io ON (iol.M_InOut_ID = io.M_InOut_ID)" +
									" WHERE DocStatus IN ('CO','CL')" +
									" AND C_OrderLine_ID = ?",
									oLine.get_ID());
							if (qtyDesp == null)
								qtyDesp = Env.ZERO;

							BigDecimal qtyDif = oLine.getQtyOrdered().subtract(qtyDesp);
							if (qtyDif.compareTo(Env.ZERO) < 0)
								qtyDif = Env.ZERO;

							DB.executeUpdate(
									"UPDATE C_OrderLine SET QtyReserved = " + qtyDif +
									" WHERE C_OrderLine_ID = " + oLine.get_ID(),
									po.get_TrxName());
						}
					}
				}
			}
		}

		return null;
	}

	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		log.info("AD_User_ID=" + AD_User_ID);
		return null;
	}

	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}

	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("ModWindsorUpdateReservedSales");
		return sb.toString ();
	}

	public void OFBReservation(int M_WareHouse_ID, int M_Product_ID, BigDecimal ordered, BigDecimal reserved, PO po)
	{
		PreparedStatement pstmt = null;
		String mysql = "SELECT * from M_StorageReservation where M_WareHouse_ID = ? and M_Product_ID = ?";
		try
		{
			pstmt = DB.prepareStatement(mysql, po.get_TrxName());
			pstmt.setInt(1, M_WareHouse_ID);
			pstmt.setInt(2, M_Product_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				X_M_StorageReservation storage = new X_M_StorageReservation(po.getCtx(), rs, po.get_TrxName());
				storage.setQtyReserved(storage.getQtyReserved().subtract(reserved));
				storage.save();
			}
			else
			{
				X_M_StorageReservation storage = new X_M_StorageReservation(po.getCtx(), 0, po.get_TrxName());
				storage.setQtyReserved(reserved.negate());
				storage.setM_Product_ID(M_Product_ID);
				storage.setM_Warehouse_ID(M_WareHouse_ID);
				storage.save();
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public boolean existReservationTable(String Trx)
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean val = false;
		String mysql = "SELECT count(1) from M_StorageReservation";
		if (!DB.isOracle())
			mysql = "select count(1) from AD_Table where tablename='M_StorageReservation'";
		try
		{
			pstmt = DB.prepareStatement(mysql, Trx);
			rs = pstmt.executeQuery();
			if (rs.next())
				if (rs.getInt(1) > 0)
					val = true;
		}
		catch (Exception e)
		{
			val = false;
		}
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		return val;
	}
}
