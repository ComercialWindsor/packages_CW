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
// Migrado desde org.windsor.model (packages_OV) a org.cwindsor.model (packages_CW). FG 2026-06
package org.cwindsor.model;

import java.math.BigDecimal;

import org.compiere.model.MClient;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *	Validator for company WINDSOR
 *
 *  @author Italo Ni�oles
 */
public class ModWindsorValidShipLine implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModWindsorValidShipLine ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModWindsorValidShipLine.class);
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
		//	Documents to be monitored
		//engine.addDocValidate(MOrder.Table_Name, this);
		engine.addDocValidate(MInOut.Table_Name, this);

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	OFB Consulting Ltda. By italo ni�oles
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
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()== MInOut.Table_ID)  
		{
			MInOut ship = (MInOut) po;
			int countSeleccion = DB.getSQLValue(po.get_TrxName(), "SELECT Count(*)"
					+ " FROM ov_seleccion_ovhdr"
					+ " WHERE M_InOut_ID = " + ship.getM_InOut_ID());
			
			if(ship.isSOTrx() && ship.getC_DocType().getDocBaseType().compareTo("MMR") !=0  && countSeleccion <= 0) //validacion no sera para devoluciones
			{
				MInOutLine[] sLines = ship.getLines(false);
				for (int i = 0; i < sLines.length; i++)
				{
					MInOutLine sLine = sLines[i];
					if(sLine.getM_Product_ID() > 0 && sLine.getM_Locator_ID() > 0)
					{	
						BigDecimal qtyOnhand = getTotalLocator(sLine.getM_Locator_ID(), sLine.getM_Product_ID(), sLine.getM_AttributeSetInstance_ID());
						if(qtyOnhand == null)
							qtyOnhand = Env.ZERO;
						
						BigDecimal qtyOtherlines = DB.getSQLValueBD(po.get_TrxName(), "SELECT SUM(movementQty)" +
								" FROM M_InOutLine iol" +
								" WHERE M_InOut_ID = "+ship.get_ID()+" AND M_Product_ID = "+sLine.getM_Product_ID()+
								" AND M_Locator_ID = "+sLine.getM_Locator_ID()+" AND M_InOutLine_ID <> "+sLine.get_ID());
						if(qtyOtherlines == null)
							qtyOtherlines=Env.ZERO;
						BigDecimal total = qtyOtherlines.add(sLine.getMovementQty());
						if(total == null)
							total = Env.ZERO;
						if(total.compareTo(qtyOnhand) > 0)
						{
							return "ERROR DE STOCK: Producto "+sLine.getM_Product().getName()+" Ubicacion "+sLine.getM_Locator().getValue()+"."+
								" Actual:"+qtyOnhand+ ". Solicitado:"+total;
						}
					}
				}
			}
		}		
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
	private BigDecimal getTotalLocator(int Locator_ID, int Product_ID, int ATTRIBUTESETINSTANCE_ID)
	{		
		String sql=" Select Sum(QtyOnhand) from M_Storage where M_Locator_ID=? and M_Product_ID=? ";
		
		if(ATTRIBUTESETINSTANCE_ID>0)
		 sql+=" and M_ATTRIBUTESETINSTANCE_ID=?";

		BigDecimal rvalue= new BigDecimal(0);
		
		if(ATTRIBUTESETINSTANCE_ID>0)
			rvalue=DB.getSQLValueBD(null, sql, Locator_ID,Product_ID,ATTRIBUTESETINSTANCE_ID);
		else
			rvalue=DB.getSQLValueBD(null, sql, Locator_ID,Product_ID);
		
		if(rvalue == null)
			rvalue= new BigDecimal(0);
		
		return rvalue;		
	}
}	