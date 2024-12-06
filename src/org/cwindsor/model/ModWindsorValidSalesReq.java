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
import org.compiere.model.MClient;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *	Validator for company WINDSOR
 *
 *  @author Italo Niñoles
 */
public class ModWindsorValidSalesReq implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModWindsorValidSalesReq ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModWindsorValidSalesReq.class);
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

		//	Tables to be monitored
		engine.addModelChange(MRequisitionLine.Table_Name, this);
		//	Documents to be monitored
		//engine.addDocValidate(MOrder.Table_Name, this);

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	OFB Consulting Ltda. By italo niñoles
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		if((type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE)&& po.get_Table_ID()== MRequisitionLine.Table_ID)  
		{
			MRequisitionLine rLine = (MRequisitionLine) po;
			MRequisition req = rLine.getParent();
			if(req.isSOTrx()) 
			{
				if(rLine.getM_Product_ID() > 0 && rLine.getM_Product().isStocked()
						&& rLine.getM_Product().getProductType().compareTo("I") == 0
						&& rLine.getAD_Client_ID() == 1000000)
				{
					BigDecimal qtyAvai = Env.ZERO; 
					if(req.getAD_Client_ID() == 1000000)
					{
						qtyAvai= DB.getSQLValueBD(po.get_TrxName(), "SELECT "+ 
								//" qtyavailableofb(mp.M_Product_ID,1000001) FROM M_Product mp WHERE mp.M_Product_ID = "+oLine.getM_Product_ID());
								" COALESCE ( "+
							      "         (SELECT SUM (s.qtyonhand) "+
							       "           FROM rv_storage s "+
							        "         WHERE     s.M_Product_ID = p.m_product_id "+
							         "              AND s.m_warehouse_id IN (1000001) "+
							          "             AND s.isactive = 'Y'), "+
							           "    0) "+
							          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
							         "         FROM C_orderline ol2      "+
							          "             INNER JOIN C_Order o2  "+
							           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
							            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
							             "          AND o2.m_warehouse_id = 1000001 "+
							              "         AND o2.saldada <> 'Y' "+
							               "        AND o2.docstatus IN ('IP', 'CO') "+
							                "       AND o2.issotrx = 'Y' "+
							                 "      AND o2.c_doctypetarget_ID NOT IN "+
							                  "            (1000110, 1000048, 1000568)) "+
							            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
							             "     FROM M_Requisitionline rl     "+
							              "         INNER JOIN M_Requisition r  "+
							               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
							                " WHERE     rl.M_Product_ID = p.m_product_id  "+
							                 "      AND r.m_warehouse_id = 1000001  "+
							                  "     AND r.docstatus IN ('CO')  "+
							                   "    AND r.issotrx = 'Y'))  "+
								
												" FROM M_Product p WHERE p.M_Product_ID = "+rLine.getM_Product_ID());
						if(qtyAvai == null)
							qtyAvai = Env.ZERO;
						BigDecimal aux = DB.getSQLValueBD(po.get_TrxName(), "SELECT "+ 
								//" qtyavailableofb(mp.M_Product_ID,1000010) FROM M_Product mp WHERE mp.M_Product_ID = "+oLine.getM_Product_ID());
								" COALESCE ( "+
							      "         (SELECT SUM (s.qtyonhand) "+
							       "           FROM rv_storage s "+
							        "         WHERE     s.M_Product_ID = p.m_product_id "+
							         "              AND s.m_warehouse_id IN (1000010) "+
							          "             AND s.isactive = 'Y'), "+
							           "    0) "+
							          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
							         "         FROM C_orderline ol2      "+
							          "             INNER JOIN C_Order o2  "+
							           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
							            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
							             "          AND o2.m_warehouse_id = 1000010 "+
							              "         AND o2.saldada <> 'Y' "+
							               "        AND o2.docstatus IN ('IP', 'CO') "+
							                "       AND o2.issotrx = 'Y' "+
							                 "      AND o2.c_doctypetarget_ID NOT IN "+
							                  "            (1000110, 1000048, 1000568)) "+
							            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
							             "     FROM M_Requisitionline rl     "+
							              "         INNER JOIN M_Requisition r  "+
							               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
							                " WHERE     rl.M_Product_ID = p.m_product_id  "+
							                 "      AND r.m_warehouse_id = 1000010  "+
							                  "     AND r.docstatus IN ('CO')  "+
							                   "    AND r.issotrx = 'Y'))  "+
								
												" FROM M_Product p WHERE p.M_Product_ID = "+rLine.getM_Product_ID());
						if(aux == null)
							aux = Env.ZERO;
						qtyAvai = qtyAvai.add(aux);
						aux = DB.getSQLValueBD(po.get_TrxName(), "SELECT "+ 
								//" qtyavailableofb(mp.M_Product_ID,1000010) FROM M_Product mp WHERE mp.M_Product_ID = "+oLine.getM_Product_ID());
								" COALESCE ( "+
							      "         (SELECT SUM (s.qtyonhand) "+
							       "           FROM rv_storage s "+
							        "         WHERE     s.M_Product_ID = p.m_product_id "+
							         "              AND s.m_warehouse_id IN (1000033) "+
							          "             AND s.isactive = 'Y'), "+
							           "    0) "+
							          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
							         "         FROM C_orderline ol2      "+
							          "             INNER JOIN C_Order o2  "+
							           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
							            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
							             "          AND o2.m_warehouse_id = 1000033 "+
							              "         AND o2.saldada <> 'Y' "+
							               "        AND o2.docstatus IN ('IP', 'CO') "+
							                "       AND o2.issotrx = 'Y' "+
							                 "      AND o2.c_doctypetarget_ID NOT IN "+
							                  "            (1000110, 1000048, 1000568)) "+
							            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
							             "     FROM M_Requisitionline rl     "+
							              "         INNER JOIN M_Requisition r  "+
							               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
							                " WHERE     rl.M_Product_ID = p.m_product_id  "+
							                 "      AND r.m_warehouse_id = 1000033  "+
							                  "     AND r.docstatus IN ('CO')  "+
							                   "    AND r.issotrx = 'Y'))  "+
								
												" FROM M_Product p WHERE p.M_Product_ID = "+rLine.getM_Product_ID());
						if(aux == null)
							aux = Env.ZERO;
						qtyAvai = qtyAvai.add(aux);
						if(req.get_ValueAsInt("C_BPartner_Location_ID") == 1010879)
						{
							BigDecimal aux2 = DB.getSQLValueBD(po.get_TrxName(), "SELECT "+ 
									//" qtyavailableofb(mp.M_Product_ID,1000024) FROM M_Product mp WHERE mp.M_Product_ID = "+oLine.getM_Product_ID());
									" COALESCE ( "+
								      "         (SELECT SUM (s.qtyonhand) "+
								       "           FROM rv_storage s "+
								        "         WHERE     s.M_Product_ID = p.m_product_id "+
								         "              AND s.m_warehouse_id IN (1000024) "+
								          "             AND s.isactive = 'Y'), "+
								           "    0) "+
								          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
								         "         FROM C_orderline ol2      "+
								          "             INNER JOIN C_Order o2  "+
								           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
								            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
								             "          AND o2.m_warehouse_id = 10000240 "+
								              "         AND o2.saldada <> 'Y' "+
								               "        AND o2.docstatus IN ('IP', 'CO') "+
								                "       AND o2.issotrx = 'Y' "+
								                 "      AND o2.c_doctypetarget_ID NOT IN "+
								                  "            (1000110, 1000048, 1000568)) "+
								            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
								             "     FROM M_Requisitionline rl     "+
								              "         INNER JOIN M_Requisition r  "+
								               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
								                " WHERE     rl.M_Product_ID = p.m_product_id  "+
								                 "      AND r.m_warehouse_id = 1000024  "+
								                  "     AND r.docstatus IN ('CO')  "+
								                   "    AND r.issotrx = 'Y'))  "+
									
 												" FROM M_Product p WHERE p.M_Product_ID = "+rLine.getM_Product_ID());
							if(aux2 == null)
								aux2 = Env.ZERO;
							qtyAvai = qtyAvai.add(aux2);
						}
					}
					else
					{
						qtyAvai= DB.getSQLValueBD(po.get_TrxName(), "SELECT bomqtyavailableofb(mp.M_Product_ID,"+req.getM_Warehouse_ID()+",0) FROM M_Product mp WHERE mp.M_Product_ID = "+rLine.getM_Product_ID());
						if(req.get_ValueAsInt("C_BPartner_Location_ID") == 1010879)
						{
							BigDecimal aux2 = DB.getSQLValueBD(po.get_TrxName(), "SELECT "+ 
									//" qtyavailableofb(mp.M_Product_ID,1000024) FROM M_Product mp WHERE mp.M_Product_ID = "+oLine.getM_Product_ID());
									" COALESCE ( "+
								      "         (SELECT SUM (s.qtyonhand) "+
								       "           FROM rv_storage s "+
								        "         WHERE     s.M_Product_ID = p.m_product_id "+
								         "              AND s.m_warehouse_id IN (1000024) "+
								          "             AND s.isactive = 'Y'), "+
								           "    0) "+
								          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
								         "         FROM C_orderline ol2      "+
								          "             INNER JOIN C_Order o2  "+
								           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
								            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
								             "          AND o2.m_warehouse_id = 1000024 "+
								              "         AND o2.saldada <> 'Y' "+
								               "        AND o2.docstatus IN ('IP', 'CO') "+
								                "       AND o2.issotrx = 'Y' "+
								                 "      AND o2.c_doctypetarget_ID NOT IN "+
								                  "            (1000110, 1000048, 1000568)) "+
								            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
								             "     FROM M_Requisitionline rl     "+
								              "         INNER JOIN M_Requisition r  "+
								               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
								                " WHERE     rl.M_Product_ID = p.m_product_id  "+
								                 "      AND r.m_warehouse_id = 1000024  "+
								                  "     AND r.docstatus IN ('CO')  "+
								                   "    AND r.issotrx = 'Y'))  "+
									
 												" FROM M_Product p WHERE p.M_Product_ID = "+rLine.getM_Product_ID());
							if(aux2 == null)
								aux2 = Env.ZERO;
							qtyAvai = qtyAvai.add(aux2);
						}
					}
					//validacion solo si es borrador ya que despues la linea se actualiza por la NV y manda error.
					if(req.getDocStatus().compareTo("DR") == 0
							|| req.getDocStatus().compareTo("IP") == 0)
						
					{
						if(qtyAvai == null)
							qtyAvai = Env.ZERO;
						if(rLine.getQty().compareTo(qtyAvai) > 0)
							return "ERROR: Stock Insuficiente. Stock Disponible:"+qtyAvai.intValue();
					}
				}
			}
		}
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
	 *	Called as first step of DocAction.prepareIt
     *	when you called addDocValidate for the table.
     *	Note that totals, etc. may not be correct.
	 *	@param po persistent object
	 *	@param timing see TIMING_ constants
     *	@return error message or null
	 */
	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		return null;
	}	//	docValidate

	/**
	 *	User Login.
	 *	Called when preferences are set
	 *	@param AD_Org_ID org
	 *	@param AD_Role_ID role
	 *	@param AD_User_ID user
	 *	@return error message or null
	 */
	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		log.info("AD_User_ID=" + AD_User_ID);

		return null;
	}	//	login


	/**
	 *	Get Client to be monitored
	 *	@return AD_Client_ID client
	 */
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}	//	getAD_Client_ID


	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("QSS_Validator");
		return sb.toString ();
	}	//	toString
}	