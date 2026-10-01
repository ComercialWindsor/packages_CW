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
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MStorage;
import org.compiere.model.MWarehouse;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_M_StorageReservation;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *	Validator for company WINDSOR
 *
 *  @author Italo Niyoles
 */
public class ModWindsorUpdateReserved implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModWindsorUpdateReserved ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModWindsorUpdateReserved.class);
	/** Client			*/
	private int		m_AD_Client_ID = -1;


	/**
	 *	Initialize Validation
	 *	@param engine validation engine
	 *	@param client client
	 */
	public void initialize (ModelValidationEngine engine, MClient client)
	{
		//client = null for global validator
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		}
		else  {
			log.info("Initializing global validator: "+this.toString());
		}

		//	Documents to be monitored
		engine.addDocValidate(MOrder.Table_Name, this);
		engine.addDocValidate(MRequisition.Table_Name, this);

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		return null;
	}	//	modelChange

	public static String rtrim(String s, char c) {
	    int i = s.length()-1;
	    while (i >= 0 && s.charAt(i) == c)
	    {
	        i--;
	    }
	    return s.substring(0,i+1);
	}

	/**
	 *	Validate Document.
	 */
	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);

		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()== MOrder.Table_ID)
		{
			MOrder order = (MOrder) po;
			if(order.isSOTrx())
			{
				MOrderLine[] oLines = order.getLines(false, null);
				for (int i = 0; i < oLines.length; i++)
				{
					MOrderLine oLine = oLines[i];
					if(oLine.getM_Product_ID() > 0 && oLine.getM_Product().isStocked()
							&& oLine.getM_Product().getProductType().compareTo("I") == 0)
					{
						if(oLine.get_ValueAsInt("M_RequisitionLine_ID") > 0)
						{
							MRequisitionLine reqL = new MRequisitionLine(po.getCtx(), oLine.get_ValueAsInt("M_RequisitionLine_ID"), po.get_TrxName());
							MWarehouse wh = MWarehouse.get(reqL.getCtx(), reqL.getM_Requisition().getM_Warehouse_ID());

							MStorage.add(wh.getCtx(), wh.getM_Warehouse_ID(),wh.getDefaultLocator().getM_Locator_ID(),
									oLine.getM_Product_ID(),
								oLine.getM_AttributeSetInstance_ID(), oLine.getM_AttributeSetInstance_ID(),
								Env.ZERO, oLine.getQtyOrdered().negate(), Env.ZERO, order.get_TrxName());
						}
					}
				}
			}
		}
		if(timing == TIMING_BEFORE_VOID && po.get_Table_ID()== MOrder.Table_ID)
		{
			MOrder order = (MOrder) po;
			if(order.isSOTrx())
			{
				MOrderLine[] oLines = order.getLines(false, null);
				for (int i = 0; i < oLines.length; i++)
				{
					MOrderLine oLine = oLines[i];
					if(oLine.getM_Product_ID() > 0 && oLine.getM_Product().isStocked()
							&& oLine.getM_Product().getProductType().compareTo("I") == 0)
					{
						if(oLine.get_ValueAsInt("M_RequisitionLine_ID") > 0)
						{
							MRequisitionLine reqL = new MRequisitionLine(po.getCtx(), oLine.get_ValueAsInt("M_RequisitionLine_ID"), po.get_TrxName());
							MWarehouse wh = MWarehouse.get(reqL.getCtx(), reqL.getM_Requisition().getM_Warehouse_ID());

							MStorage.add(wh.getCtx(), wh.getM_Warehouse_ID(),wh.getDefaultLocator().getM_Locator_ID(),
									oLine.getM_Product_ID(),
								oLine.getM_AttributeSetInstance_ID(), oLine.getM_AttributeSetInstance_ID(),
								Env.ZERO, oLine.getQtyOrdered(), Env.ZERO, order.get_TrxName());
						}
					}
				}
			}
		}
		if(timing == TIMING_BEFORE_VOID && po.get_Table_ID()== MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			if(req.isSOTrx())
			{
				MRequisitionLine[] rLines = req.getLines();
				for (int i = 0; i < rLines.length; i++)
				{
					MRequisitionLine rLine = rLines[i];
					if(rLine.getM_Product_ID() > 0 && rLine.getM_Product().isStocked()
							&& rLine.getM_Product().getProductType().compareTo("I") == 0)
					{
						MWarehouse wh = MWarehouse.get(req.getCtx(), req.getM_Warehouse_ID());

						MStorage.add(wh.getCtx(), wh.getM_Warehouse_ID(),wh.getDefaultLocator().getM_Locator_ID(),
								rLine.getM_Product_ID(),
							rLine.getM_AttributeSetInstance_ID(), rLine.getM_AttributeSetInstance_ID(),
							Env.ZERO, rLine.getQty().negate(), Env.ZERO, req.get_TrxName());
					}
				}
			}
		}

		// Al completar la requisicion:
		// FIX 2026-06-09 (FG): semantica correcta:
		//   QtyUsed     = SUM(QtyDelivered) de OVs CO/CL/IP  (lo realmente entregado)
		//   QtyReserved = SUM(QtyOrdered - QtyDelivered) de OVs CO/IP  (comprometido pendiente, CL ya cerrada)
		// Antes: qtyUsed=SUM(QtyOrdered) y qtyReserved=Qty-qtyUsed -> daba "libre", no "reservado"
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()== MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			if(req.isSOTrx())
			{
				MRequisitionLine[] rLines = req.getLines();
				for (int i = 0; i < rLines.length; i++)
				{
					MRequisitionLine rLine = rLines[i];
					BigDecimal qtyUsed = DB.getSQLValueBD(req.get_TrxName(),
							"SELECT COALESCE(SUM(NVL(co.QtyDelivered,0)),0)" +
							" FROM C_OrderLine co" +
							" INNER JOIN C_Order col ON (col.C_Order_ID = co.C_Order_ID)" +
							" WHERE col.DocStatus IN ('CO','CL','IP')" +
							" AND co.M_RequisitionLine_ID = ?",
							rLine.get_ID());
					if(qtyUsed == null)
						qtyUsed = Env.ZERO;
					BigDecimal qtyReserved = DB.getSQLValueBD(req.get_TrxName(),
							"SELECT COALESCE(SUM(co.QtyOrdered - NVL(co.QtyDelivered,0)),0)" +
							" FROM C_OrderLine co" +
							" INNER JOIN C_Order col ON (col.C_Order_ID = co.C_Order_ID)" +
							" WHERE col.DocStatus IN ('CO','IP')" +
							" AND co.M_RequisitionLine_ID = ?",
							rLine.get_ID());
					if(qtyReserved == null)
						qtyReserved = Env.ZERO;
					if(qtyReserved.compareTo(Env.ZERO) < 0)
						qtyReserved = Env.ZERO;
					BigDecimal oldRes = toBD(rLine.get_Value("QtyReserved"));
					BigDecimal oldUsed = toBD(rLine.get_Value("QtyUsed"));
					BigDecimal reqQty = toBD(rLine.get_Value("Qty"));
					auditReserva(po, rLine.get_ID(), oldRes, oldUsed, qtyReserved, qtyUsed,
							"REQ_AFTER_COMPLETE", req.get_ID(), req.getDocumentNo(), req.getDocStatus(), reqQty);
					rLine.set_CustomColumn("QtyReserved", qtyReserved);
					rLine.set_CustomColumn("QtyUsed", qtyUsed);
					rLine.save();
				}
			}
		}
		// Al anular la requisicion: liberar toda la reserva
		if(timing == TIMING_AFTER_VOID && po.get_Table_ID()== MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			if(req.isSOTrx())
				DB.executeUpdate("UPDATE M_RequisitionLine SET QtyReserved = 0 WHERE M_Requisition_ID = "+req.get_ID(), po.get_TrxName());
		}
		// Al completar / anular / reactivar una OV: recalcular QtyReserved y QtyUsed de la req line
		// FIX 2026-06-09 (FG): semantica correcta:
		//   QtyUsed     = SUM(QtyDelivered) de OVs CO/CL/IP
		//   QtyReserved = SUM(QtyOrdered - QtyDelivered) de OVs CO/IP (CL ya cerrada, sin pendiente)
		if((timing == TIMING_AFTER_COMPLETE || timing == TIMING_AFTER_VOID
				|| timing == TIMING_AFTER_REACTIVATE) && po.get_Table_ID()== MOrder.Table_ID)
		{
			MOrder order = (MOrder) po;
			if(order.isSOTrx())
			{
				String trigger;
				if (timing == TIMING_AFTER_COMPLETE) trigger = "ORDER_AFTER_COMPLETE";
				else if (timing == TIMING_AFTER_VOID) trigger = "ORDER_AFTER_VOID";
				else trigger = "ORDER_AFTER_REACTIVATE";
				MOrderLine[] oLines = order.getLines(false, null);
				for (int i = 0; i < oLines.length; i++)
				{
					MOrderLine oLine = oLines[i];
					if(oLine.get_ValueAsInt("M_RequisitionLine_ID") > 0)
					{
						MRequisitionLine rLine = new MRequisitionLine(po.getCtx(), oLine.get_ValueAsInt("M_RequisitionLine_ID"), po.get_TrxName());

						/*BigDecimal qtyUsed = DB.getSQLValueBD(po.get_TrxName(),
								"SELECT COALESCE(SUM(NVL(co.QtyDelivered,0)),0)" +
								" FROM C_OrderLine co" +
								" INNER JOIN C_Order col ON (col.C_Order_ID = co.C_Order_ID)" +
								" WHERE col.DocStatus IN ('CO','CL','IP') AND co.M_RequisitionLine_ID = ?",
								rLine.get_ID());
						if(qtyUsed == null)
							qtyUsed = Env.ZERO;*/
						BigDecimal qtyUsed =DB.getSQLValueBD(po.get_TrxName(), "SELECT SUM(QtyOrdered)" +
								" FROM C_OrderLine co" +
								" INNER JOIN C_Order col ON (col.C_Order_ID = co.C_Order_ID)" +
								" WHERE DocStatus IN ('CO','CL','IP') AND M_RequisitionLine_ID = ?",rLine.get_ID());
						if(qtyUsed == null)							
							qtyUsed = Env.ZERO;
						/*BigDecimal qtyReserved = DB.getSQLValueBD(po.get_TrxName(),
								"SELECT COALESCE(SUM(co.QtyOrdered - NVL(co.QtyDelivered,0)),0)" +
								" FROM C_OrderLine co" +
								" INNER JOIN C_Order col ON (col.C_Order_ID = co.C_Order_ID)" +
								" WHERE col.DocStatus IN ('CO','IP') AND co.M_RequisitionLine_ID = ?",
								rLine.get_ID());*/
						BigDecimal qtyReserved = rLine.getQty().subtract(qtyUsed); 
						if(qtyReserved == null)
							qtyReserved = Env.ZERO;
						if(qtyReserved.compareTo(Env.ZERO) < 0)
							qtyReserved = Env.ZERO;
						BigDecimal oldRes = toBD(rLine.get_Value("QtyReserved"));
						BigDecimal oldUsed = toBD(rLine.get_Value("QtyUsed"));
						BigDecimal reqQty = toBD(rLine.get_Value("Qty"));
						auditReserva(po, rLine.get_ID(), oldRes, oldUsed, qtyReserved, qtyUsed,
								trigger, order.get_ID(), order.getDocumentNo(), order.getDocStatus(), reqQty);
						/*DB.executeUpdate("UPDATE M_RequisitionLine SET QtyReserved = "+oldRes.subtract(qtyReserved)+", QtyUsed = "+reqQty.subtract(oldRes.subtract(qtyReserved))+
								" WHERE M_RequisitionLine_ID = "+rLine.get_ID(), po.get_TrxName());*/
						DB.executeUpdate("UPDATE M_RequisitionLine SET QtyReserved = "+qtyReserved+", qtyUsed = " +qtyUsed+
								" WHERE M_RequisitionLine_ID = "+rLine.get_ID(), po.get_TrxName());
					}
				}
			}
		}
		return null;
	}	//	docValidate

	/**
	 *	User Login.
	 */
	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		log.info("AD_User_ID=" + AD_User_ID);
		return null;
	}	//	login

	/**
	 *	Get Client to be monitored
	 */
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}	//	getAD_Client_ID

	/**
	 * 	String Representation
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("QSS_Validator");
		return sb.toString ();
	}	//	toString

	// FIX 2026-06-09 (FG): helper conversion to BigDecimal safe for null
	private BigDecimal toBD(Object v)
	{
		if (v == null) return Env.ZERO;
		if (v instanceof BigDecimal) return (BigDecimal) v;
		try { return new BigDecimal(v.toString()); } catch (Exception e) { return Env.ZERO; }
	}

	// FIX 2026-06-09 (FG): registra cada movimiento QtyReserved/QtyUsed en Z_ReservaAuditLog
	// con stack trace para identificar quien dispara el cambio. Best-effort, no rompe trx si falla.
	private void auditReserva(PO po, int reqLineId, BigDecimal oldRes, BigDecimal oldUsed,
			BigDecimal newRes, BigDecimal newUsed, String source, int sourceDocId,
			String docNo, String docStatus, BigDecimal reqQty)
	{
		PreparedStatement ps = null;
		try {
			StringBuilder stack = new StringBuilder();
			StackTraceElement[] elems = Thread.currentThread().getStackTrace();
			int limit = elems.length;
			if (limit > 14) limit = 14;
			for (int i = 2; i < limit; i++) {
				stack.append(elems[i].getClassName()).append('.').append(elems[i].getMethodName())
					.append(':').append(elems[i].getLineNumber()).append('\n');
			}
			String stackStr = stack.toString();
			if (stackStr.length() > 3900) stackStr = stackStr.substring(0, 3900);

			log.warning("ReservaAudit ReqLine=" + reqLineId + " src=" + source
					+ " oldRes=" + oldRes + " newRes=" + newRes
					+ " oldUsed=" + oldUsed + " newUsed=" + newUsed
					+ " Doc=" + docNo + " Status=" + docStatus + " SrcDoc=" + sourceDocId);

			String sql = "INSERT INTO Z_ReservaAuditLog (Z_ReservaAuditLog_ID, AD_Client_ID, AD_Org_ID, " +
					"IsActive, Created, CreatedBy, Updated, UpdatedBy, " +
					"M_RequisitionLine_ID, TriggerSource, C_Order_ID, DocumentNo, DocStatus, " +
					"OldQtyReserved, NewQtyReserved, OldQtyUsed, NewQtyUsed, ReqQty, StackTrace) " +
					"VALUES (Z_ReservaAuditLog_SEQ.NEXTVAL, ?, ?, 'Y', SYSDATE, ?, SYSDATE, ?, " +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			ps = DB.prepareStatement(sql, po.get_TrxName());
			int userId = Env.getAD_User_ID(po.getCtx());
			ps.setInt(1, po.getAD_Client_ID());
			ps.setInt(2, po.getAD_Org_ID());
			ps.setInt(3, userId);
			ps.setInt(4, userId);
			ps.setInt(5, reqLineId);
			ps.setString(6, source);
			if (sourceDocId > 0) ps.setInt(7, sourceDocId);
			else ps.setNull(7, java.sql.Types.NUMERIC);
			ps.setString(8, docNo);
			ps.setString(9, docStatus);
			ps.setBigDecimal(10, oldRes);
			ps.setBigDecimal(11, newRes);
			ps.setBigDecimal(12, oldUsed);
			ps.setBigDecimal(13, newUsed);
			ps.setBigDecimal(14, reqQty);
			ps.setString(15, stackStr);
			ps.executeUpdate();
		} catch (Exception e) {
			log.warning("ReservaAudit insert fail: " + e.getMessage());
		} finally {
			if (ps != null) try { ps.close(); } catch (Exception ex) {}
		}
	}

	public void OFBReservation(int M_WareHouse_ID, int M_Product_ID, BigDecimal ordered, BigDecimal reserved, PO po)
	{
		PreparedStatement pstmt = null;

		String mysql="SELECT * from M_StorageReservation where M_WareHouse_ID = ? and M_Product_ID = ?";
		try
		{
			pstmt = DB.prepareStatement(mysql, po.get_TrxName());
			pstmt.setInt(1, M_WareHouse_ID);
			pstmt.setInt(2, M_Product_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				X_M_StorageReservation storage = new X_M_StorageReservation (po.getCtx(),rs,po.get_TrxName());
					storage.setQtyReserved(storage.getQtyReserved().subtract(reserved));
					storage.save();
			}
			else{
				X_M_StorageReservation storage = new X_M_StorageReservation (po.getCtx(),0,po.get_TrxName() );
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

		String mysql="SELECT count(1) from M_StorageReservation";

		if(!DB.isOracle())
			mysql = "select count(1) from AD_Table where tablename='M_StorageReservation'";
		try
		{
			pstmt = DB.prepareStatement(mysql,Trx);
			rs = pstmt.executeQuery();
			if (rs.next())
				if(rs.getInt(1)>0)
					val = true;

		}
		catch (Exception e)
		{
			val = false;
		}
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return val;
	}
}
