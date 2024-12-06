

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

//import org.compiere.model.MOrder;

//import org.compiere.util.Env;

//import org.compiere.util.Env;

/**
 *	Comercial Windsor Callout.
 *
 *  @author Fdo Gonzalez
 *  @version  $Id: CalloutOrderLineCW.java,v 1.0 $ 02-09-2019
 */
public class CalloutProductVolumen extends CalloutEngine
{
	//private Properties 		m_ctx;	
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
	public String setOLogistico (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		//m_ctx = Env.getCtx();
		BigDecimal bp = (BigDecimal)				value;		//	Si es que marco pasar a OV la linea
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (bp == null || bp.doubleValue() <=0)
			return "";
		if(bp.doubleValue()>0)
		{
			
		
        
		//	Rescato cliente para consultar stock
			int client_id = (Integer)mTab.getValue("AD_Client_ID");
			if(client_id!=1000000)
				return "";
			BigDecimal alto= (BigDecimal) mTab.getValue("ALTOEMPAQUECMS") ;
			BigDecimal ancho = (BigDecimal) mTab.getValue("ANCHOEMPAQUECMS") ;
			BigDecimal largo = (BigDecimal) mTab.getValue("LARGOEMPAQUECMS") ;
					Double volumen= (alto.doubleValue() *
										ancho.doubleValue() *
										largo.doubleValue()) /1000000;
			mTab.setValue("Volumen", new BigDecimal( volumen) );
			
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setOLogistico
//
	
}	//	CalloutBPOLogistico
