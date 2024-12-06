

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

import java.util.Properties;





import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;

//import org.compiere.model.MOrder;

import org.compiere.model.MProduct;
import org.compiere.util.Env;

//import org.compiere.util.Env;

/**
 *	Comercial Windsor Callout.
 *
 *  @author Fdo Gonzalez
 *  @version  $Id: CalloutOrderLineCW.java,v 1.0 $ 02-09-2019
 */
public class CallOutCOrderLine extends CalloutEngine
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
	public String setOLogistico (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		m_ctx = Env.getCtx();
		Integer product_ID = (Integer)				value;		//	PRoducto
	//	String mensaje="Se Activa. PasarOv="+pasarov;
		if (product_ID == null || product_ID <=0)
			return "";
		if(product_ID.intValue()>0)
		{
			
		
        
		//	Rescato cliente para consultar stock
			int client_id = (Integer)mTab.getValue("AD_Client_ID");
			if(client_id!=1000000)
				return "";
			MProduct product = new MProduct (m_ctx, product_ID.intValue(), null);
			mTab.setValue("UnitsPerPack", product.get_ValueAsInt ("UnitsPerPack") );
			
							 
							
					
					
							 
						 
						 
		}
					 
		
		
		
		return "";
	}	//	setOLogistico
//
	
}	//	CalloutBPOLogistico
