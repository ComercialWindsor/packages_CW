/******************************************************************************
0 * Product: Adempiere ERP & CRM Smart Business Solution                        *
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
package org.windsor.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//import java.util.Properties;
import java.util.logging.*;

import org.compiere.model.MBPartner;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;
import org.windsor.model.*;


//import org.compiere.util.Env;
 
/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class ProcesarOB2C extends SvrProcess
{
	//private Properties 		m_ctx;	
	private int id=0; 

	      
//	private int p_Issue = 0;
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_OrderB2C_ID"))
				id= para[i].getParameterAsInt();
				
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	//	m_ctx = Env.getCtx();
		
		//int cliente = Env.getAD_User_ID(getCtx());
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws AdempiereSystemError
	{
		/*********/
	
		
		X_C_OrderB2C order = new X_C_OrderB2C (getCtx() , id, get_TrxName());
		
		int errorline=0, errorhead=0;
		if ( order.getC_BPartner_ID()==0 )
			errorhead++;
		if ( order.getC_BPartner_Location_ID()==0 )
			errorhead++;
		if ( order.getSalesRep_ID()==0 )
			errorhead++;
		
		if (errorhead!=0 )
				return "Los datos del cliente o vendedor no pueden ir vacios";
		String	sql = "Select * from C_orderb2cline where processed<>'Y' and c_orderb2c_ID="+ order.getC_OrderB2C_ID();
		try
		{
			PreparedStatement pstmt = DB.prepareStatement (sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
			{
				X_C_OrderB2CLine line = new X_C_OrderB2CLine (getCtx() , rs.getInt("C_OrderB2CLine_ID"), get_TrxName());
				if (line.getM_Product_ID()==0)
					return "producto no puede ir vacio, verifique linea:"+ line.getLine();
				if (line.getQtyEntered().intValue()<=0)
					return "Cantidad no puede ser 0 o menor, verifique linea:"+ line.getLine();
				if (line.getPriceEntered()<=0)
					return "Precio no puede ser 0 o menor, verifique linea:"+ line.getLine();
				String sqls = "Select "+ // qtyavailableofb(p.m_product_ID,"+ order.getM_Warehouse_ID()  + ") "+ 
						" COALESCE ( "+
					      "         (SELECT SUM (s.qtyonhand) "+
					       "           FROM rv_storage s "+
					        "         WHERE     s.M_Product_ID = p.m_product_id "+
					         "              AND s.m_warehouse_id IN (1000001, 1000010) "+
					          "             AND s.isactive = 'Y'), "+
					           "    0) "+
					          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
					         "         FROM C_orderline ol2      "+
					          "             INNER JOIN C_Order o2  "+
					           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
					            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
					             "          AND o2.m_warehouse_id ="+ order.getM_Warehouse_ID() +
					              "         AND o2.saldada <> 'Y' "+
					               "        AND o2.docstatus IN ('IP', 'CO', 'CL') "+
					                "       AND o2.issotrx = 'Y' "+
					                 "      AND o2.c_doctypetarget_ID NOT IN "+
					                  "            (1000110, 1000048, 1000568)) "+
					            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
					             "     FROM M_Requisitionline rl     "+
					              "         INNER JOIN M_Requisition r  "+
					               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
					                " WHERE     rl.M_Product_ID = p.m_product_id  "+
					                 "      AND r.m_warehouse_id ="+ order.getM_Warehouse_ID() +
					                  "     AND r.docstatus IN ('CO', 'CL')  "+
					                   "    AND r.issotrx = 'Y'))  "+
						
						" as Disponible,  p.ProductType, " +
						//"qtyavailableofb(p.m_product_ID,1000001)+qtyavailableofb(p.m_product_ID,1000010)"+
						" COALESCE ( "+
					      "         (SELECT SUM (s.qtyonhand) "+
					       "           FROM rv_storage s "+
					        "         WHERE     s.M_Product_ID = p.m_product_id "+
					         "              AND s.m_warehouse_id IN (1000001, 1000010) "+
					          "             AND s.isactive = 'Y'), "+
					           "    0) "+
					          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
					         "         FROM C_orderline ol2      "+
					          "             INNER JOIN C_Order o2  "+
					           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
					            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
					             "          AND o2.m_warehouse_id = 1000001 "+
					              "         AND o2.saldada <> 'Y' "+
					               "        AND o2.docstatus IN ('IP', 'CO', 'CL') "+
					                "       AND o2.issotrx = 'Y' "+
					                 "      AND o2.c_doctypetarget_ID NOT IN "+
					                  "            (1000110, 1000048, 1000568)) "+
					            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
					             "     FROM M_Requisitionline rl     "+
					              "         INNER JOIN M_Requisition r  "+
					               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
					                " WHERE     rl.M_Product_ID = p.m_product_id  "+
					                 "      AND r.m_warehouse_id = 1000001  "+
					                  "     AND r.docstatus IN ('CO', 'CL')  "+
					                   "    AND r.issotrx = 'Y'))  "+
						
						" as OtroDisponible"+
					   " from M_product p where p.m_product_ID="+ line.getM_Product_ID();
				try
				{
					PreparedStatement pstmts = DB.prepareStatement (sqls, get_TrxName());
					ResultSet rss = pstmts.executeQuery ();
					
					if (rss.next())
						if(rss.getString("ProductType").equals("I"))
						{
						if (line.getQtyEntered().intValue()<=rss.getInt("Disponible"))
						{

							line.setPASARAOV(true);
							
						}
						else
						{
							//System.out.print("Cantidad:"+rs.getInt("Cantidad") + " Disponible"+rsps.getInt("Disponible"));
							errorline++;
							line.setPASARAOV(false);
							 BigDecimal sob = new BigDecimal (rss.getInt("OtroDisponible"));
							 line.setErrorMsg("No hay stock en la bodega actual, verificar en otras bodegas");
							line.setStockBodegas(sob);
						
						}
						
						}
						else
							line.setPASARAOV(true);
					line.save();
						
						
					rss.close();
					pstmts.close();
				}
				catch(Exception e)
				{
					
					log.log(Level.SEVERE, e.getMessage(), e);
				} //stock
			}
			rs.close();
			pstmt.close();
		}
		catch(Exception e)
		{
			
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		if (errorline!=0)
			return "Verifique stock de productos de esta orden";
		MOrder ord = null;
		int contador=1;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement (sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
			{
				if (ord==null)
				{
					ord = new MOrder (getCtx() , 0, get_TrxName());
					
					
					MBPartner bp = new MBPartner (getCtx(), order.getC_BPartner_ID(), get_TrxName());
					ord.setClientOrg (order.getAD_Client_ID(),order.getAD_Org_ID());
					//ord.setC_DocTypeTarget_ID(1000030);
					if (order.getC_BPartner_ID()!=1011716)
						ord.setC_DocTypeTarget_ID(1000030);
						else
						ord.setC_DocTypeTarget_ID(1000568);	
					ord.setIsSOTrx(true);
					ord.setDeliveryRule("O");
					ord.setC_BPartner_ID(order.getC_BPartner_ID());
					ord.setC_BPartner_Location_ID(order.getC_BPartner_Location_ID());
					ord.setPOReference(order.getPOReference());
					ord.setAD_User_ID(order.getSalesRep_ID());
					//	Bill Partner
					ord.setBill_BPartner_ID(order.getC_BPartner_ID());
					ord.setBill_Location_ID(order.getC_BPartner_Location_ID());
					//
					if (order.getDescription() != null)
						ord.setDescription(order.getDescription());
					ord.setC_PaymentTerm_ID(bp.getC_PaymentTerm_ID());
					ord.setM_PriceList_ID(order.getM_PriceList_ID());
					ord.setM_Warehouse_ID(order.getM_Warehouse_ID());
					ord.setSalesRep_ID(order.getSalesRep_ID());
					BigDecimal stbd= new BigDecimal(order.getC_BPartner_SubTienda_ID());
					if(stbd!=null && order.getC_BPartner_SubTienda_ID()>0)
						ord.set_CustomColumn("C_BPartner_SubTienda_ID", order.getC_BPartner_SubTienda_ID());
					//
					ord.setDateOrdered(order.getDateAcct()   );
					ord.setDateAcct( order.getDateAcct());
					ord.setInvoiceRule("D");
					
					//Faltan firmas horas de firma y quien firmo forma de compra medio compra
					//
					ord.set_CustomColumn("FIRMA2", "Y");
					ord.set_CustomColumn("FIRMA3", "Y");
					ord.set_CustomColumn("FIRMACOM", order.getDateAcct());
					ord.set_CustomColumn("FIRMAFIN", order.getDateAcct());
					ord.set_CustomColumn("USERFIRMCOM", 1003890); //mcladeron
					ord.set_CustomColumn("USERFIRMFIN", 1003303); //eumanzor
					ord.set_CustomColumn("FormaCompra", order.get_Value("FormaCompra"));
					ord.set_CustomColumn("MedioCompra", order.get_Value("MedioCompra"));
					ord.set_CustomColumn("VentaInvierno", "N");
					ord.save();
					
					
				}
				
				if (contador<=16)
				{
				//	  noInsertLine++;
					MOrderLine line = new MOrderLine(ord);
					X_C_OrderB2CLine lines= new X_C_OrderB2CLine (getCtx() , rs.getInt("C_OrderB2CLine_ID"),get_TrxName());
				// 	line.setC_Order_ID(order.getC_Order_ID());
					line.setM_Product_ID(lines.getM_Product_ID());
					line.setPriceEntered( new BigDecimal  (lines.getPriceEntered()));
					line.setPriceActual(new BigDecimal  (lines.getPriceEntered()));
					line.setPriceList(new BigDecimal  (lines.getPriceEntered()));
					line.setQtyEntered( (lines.getQtyEntered()));
					line.setLine(contador*10);
					line.setPrice(new BigDecimal  (lines.getPriceEntered()));
					line.setQty(lines.getQtyEntered());
					line.set_CustomColumn("Demand", lines.getQtyEntered());
					
				//	line.setLineNetAmt(rs.getBigDecimal("LineNetAmt"));
					//int la = rs.getBigDecimal("PriceEntered").intValue() * rs.getBigDecimal("QtyEntered").intValue();
					//BigDecimal lab = new BigDecimal (la);
					
					
						
						//line.set_CustomColumn("Discount2", rs.getBigDecimal("Discount2"));
						//line.set_CustomColumn("Discount3", rs.getBigDecimal("Discount3"));
						BigDecimal df = new BigDecimal (0);
						line.set_CustomColumn("Discount2", df);
						//BigDecimal df = new BigDecimal (0);
						line.set_CustomColumn("Discount3", df);
						//BigDecimal df = new BigDecimal (0);
						line.set_CustomColumn("Discount", df);
						line.set_CustomColumn("Discount3", df);
						line.set_CustomColumn("Discount4", df);
						line.set_CustomColumn("Discount5", df);
						line.set_CustomColumn("NotPrint", "N");
					
					
					line.setLineNetAmt();
					line.save();
					//line.set_ValueOfColumn("TEMPLINE_ID", rs.getInt("M_INVENTORYLINETEMP_ID"));
					
					lines.setC_Order_ID(ord.getC_Order_ID());
					lines.setProcessed(true);
					lines.save();
					contador++;
				}
			if (contador==17)
			{
				ord.setDocAction("CO");
				ord.processIt ("CO");
				ord.save();
				
				contador=1;
				
				ord=null;
			}
				
				
						
						
					
			 }
			
			ord.setDocAction("CO");
			ord.processIt ("CO");
			ord.save();
			order.setProcessed(true);
			order.save();
			rs.close();
			pstmt.close();
		}
			
		
		catch(Exception e)
		{
			
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return "Procesado";		
				
			
	}	//	doIt
	
}	//	OrderOpen
