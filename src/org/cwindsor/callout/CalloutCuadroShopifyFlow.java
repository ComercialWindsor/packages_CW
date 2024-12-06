

/******************************************************************************
 
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 *
 * Copyright (C) 2005 Robert KLEIN. robeklein@gmail.com * 
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.cwindsor.callout;

//import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Properties;





import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MBPartner;
import org.windsor.model.X_T_CuadroShopiFlow;
import org.compiere.util.Env;

//import org.compiere.util.Env;

/**
 *	Comercial Windsor Callout.
 *
 *  @author Fdo Gonzalez
 *  @version  $Id: CalloutOrderLineCW.java,v 1.0 $ 02-09-2019
 */
public class CalloutCuadroShopifyFlow extends CalloutEngine
{
	private Properties 		m_ctx;	
	/**
	 *	Table: C_OrderLine
	 * Desarrollo de callout que permiten varias cosas en la linea de orden de venta
	 *  @param ctx      Context
	 *  @param WindowNo current Window No
	 *  @param mTab     Model Tab
	 *  @param mField   Model Field
	 *  @param value    The new value
	 *  @param oldValue The old value
	 *	@return error message or "" if OK
	 */
	public String setDifAmtFabShopy (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		m_ctx = Env.getCtx();
		BigDecimal valor= (BigDecimal)				value;		//	Si es que marco pasar a OV la linea
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (valor == null )
			return "";
		if(valor!=null)
		{
			BigDecimal MP= (BigDecimal)mTab.getValue("FABRICMPNETO");
			BigDecimal FL= (BigDecimal)mTab.getValue("FABRICSFLOWNETO");
			BigDecimal SH= (BigDecimal)mTab.getValue("FABRICSHOPNETO");
			BigDecimal VP= (BigDecimal)mTab.getValue("FABRICSVPNETO");
			BigDecimal FCT= (BigDecimal)mTab.getValue("FABRICSFTCNETO");
			int dif = SH.intValue() - FL.intValue() - MP.intValue() - VP.intValue() - FCT.intValue();
		//	Rescato cliente para consultar stock
			
			mTab.setValue("FABRICSDIFNETO", new BigDecimal (dif) );
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setDifAmtFabShopy 
//
	public String setDifQtyFabShopy (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		m_ctx = Env.getCtx();
		BigDecimal valor= (BigDecimal)				value;		//	Si es que marco pasar a OV la linea
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (valor == null )
			return "";
		if(valor!=null)
		{
			BigDecimal MP= (BigDecimal)mTab.getValue("FABRICMPQTY");
			BigDecimal FL= (BigDecimal)mTab.getValue("FABRICSFLOWQTY");
			BigDecimal SH= (BigDecimal)mTab.getValue("FABRICSHOPQTY");
			BigDecimal VP= (BigDecimal)mTab.getValue("FABRICSVPQTY");
			BigDecimal FTC= (BigDecimal)mTab.getValue("FABRICSFTCQTY");
			
			int dif = SH.intValue() - FL.intValue() - MP.intValue() - VP.intValue() -FTC.intValue();
		//	Rescato cliente para consultar stock
			
			mTab.setValue("FABRICSDIFQTY", new BigDecimal (dif) );
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setDifQtyFabShopy 
	
	//Mashini
	public String setDifAmtMasShopy (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		m_ctx = Env.getCtx();
		BigDecimal valor= (BigDecimal)				value;		//	Si es que marco pasar a OV la linea
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (valor == null )
			return "";
		if(valor!=null)
		{
			BigDecimal MP= (BigDecimal)mTab.getValue("MASHINIMPNETO");
			BigDecimal FL= (BigDecimal)mTab.getValue("MASHINIFLOWNETO");
			BigDecimal SH= (BigDecimal)mTab.getValue("MASHINISHOPNETO");
			BigDecimal VP= (BigDecimal)mTab.getValue("MASHINIVPNETO");
			int dif = SH.intValue() - FL.intValue() - MP.intValue() - VP.intValue();
		//	Rescato cliente para consultar stock
			
			mTab.setValue("MASHINIDIFNETO", new BigDecimal (dif) );
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setDifAmtMasShopy 
//
	public String setDifQtyMasShopy (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		m_ctx = Env.getCtx();
		BigDecimal valor= (BigDecimal)				value;		//	Si es que marco pasar a OV la linea
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (valor == null )
			return "";
		if(valor!=null)
		{
			BigDecimal MP= (BigDecimal)mTab.getValue("MASHINIMPQTY");
			BigDecimal FL= (BigDecimal)mTab.getValue("MASHINIFLOWQTY");
			BigDecimal SH= (BigDecimal)mTab.getValue("MASHINISHOPQTY");
			BigDecimal VP= (BigDecimal)mTab.getValue("MASHINIVPQTY");
			int dif = SH.intValue() - FL.intValue() - MP.intValue() - VP.intValue();
		//	Rescato cliente para consultar stock
			
			mTab.setValue("MASHINIDIFQTY", new BigDecimal (dif) );
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setDifQtyMasShopy 
	//Windsor
	public String setDifAmtWinShopy (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		m_ctx = Env.getCtx();
		BigDecimal valor= (BigDecimal)				value;		//	Si es que marco pasar a OV la linea
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (valor == null )
			return "";
		if(valor!=null)
		{
			BigDecimal MP= (BigDecimal)mTab.getValue("WINDSORMPNETO");
			BigDecimal FL= (BigDecimal)mTab.getValue("WINDSORFLOWNETO");
			BigDecimal SH= (BigDecimal)mTab.getValue("WINDSORSHOPNETO");
			BigDecimal VP= (BigDecimal)mTab.getValue("WINDSORVPNETO");
			int dif = SH.intValue() - FL.intValue() - MP.intValue() - VP.intValue();
		//	Rescato cliente para consultar stock
			
			mTab.setValue("WINDSORDIFNETO", new BigDecimal (dif) );
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setDifAmtWinShopy 
//
	public String setDifQtyWinShopy (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		m_ctx = Env.getCtx();
		BigDecimal valor= (BigDecimal)				value;		//	Si es que marco pasar a OV la linea
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (valor == null )
			return "";
		if(valor!=null)
		{
			BigDecimal MP= (BigDecimal)mTab.getValue("WINDSORMPQTY");
			BigDecimal FL= (BigDecimal)mTab.getValue("WINDSORFLOWQTY");
			BigDecimal SH= (BigDecimal)mTab.getValue("WINDSORSHOPQTY");
			BigDecimal VP= (BigDecimal)mTab.getValue("WINDSORVPQTY");
			int dif = SH.intValue() - FL.intValue() - MP.intValue() - VP.intValue();
		//	Rescato cliente para consultar stock
			
			mTab.setValue("WINDSORDIFQTY", new BigDecimal (dif) );
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setDifQtyMasShopy 
}	
