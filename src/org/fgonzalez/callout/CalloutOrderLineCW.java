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
package org.fgonzalez.callout;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.DB;
//import org.compiere.util.Env;

/**
 *	Comercial Windsor Callout.
 *
 *  @author Fdo Gonzalez
 *  @version  $Id: CalloutOrderLineCW.java,v 1.0 $
 */
public class CalloutOrderLineCW extends CalloutEngine
{
	
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
	public String preVenta (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		Integer M_Product_ID = (Integer)value;		//	Producto que busco
		if (M_Product_ID  == null || M_Product_ID.intValue() == 0)
			return "";
        String mensaje="";
		//	Rescato datos para hacer comparacion con alguna preventa
		int client_id = (Integer)mTab.getValue("AD_Client_ID");
		if(client_id!=1000000)
			return "";
		int order_id = (Integer)mTab.getValue("C_Order_ID");
		int bp_id = (Integer)mTab.getValue("C_BPartner_ID");
		int bpl_id = (Integer)mTab.getValue("C_BPartner_Location_ID");
		
		int doctype_ID = 0;

		String sql = "SELECT o.c_doctypetarget_ID "						//  tipo de documento a validad
			+ "FROM C_Order o "
			+ "WHERE o.C_Order_ID="+order_id;	//	#1
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			

				pstmt = DB.prepareStatement(sql, null);
			
				rs = pstmt.executeQuery();
				if (rs.next())
					doctype_ID = rs.getInt("C_DocTypeTarget_ID");
				DB.close(rs, pstmt);
				rs = null;
				pstmt = null;
			
			    if(doctype_ID==1000030 || doctype_ID==1000048)
			    {
			    	sql = "SELECT max(ol.C_Order_ID) as C_Order_ID "						//  tipo de documento a validad
			    			+ "FROM C_OrderLine ol "
			    			+ "inner join C_order o on (ol.c_order_ID=o.c_order_ID) "
			    			+ "WHERE o.C_Order_ID<>"+order_id
			    			+ " and ol.m_product_ID=" + M_Product_ID
			    			+ " and o.c_bpartner_ID="+bp_id
			    			+ " and ol.QtyEntered>0"
			    			+ " and o.c_doctype_ID=1000048"
			    			+ " and o.docstatus='CO'"
			    			+ " and o.c_bpartner_location_ID="+bpl_id
			    			+ " and not exists (Select * from M_inout io where io.c_order_ID=o.c_order_ID and io.docstatus='CO')";
			    	pstmt = DB.prepareStatement(sql, null);
					//pstmt.setInt(1, C_DocType_ID.intValue());
					rs = pstmt.executeQuery();
					if (rs.next()) //si encuentra una orden que cumpla la condicion
					{
						Integer neworden = rs.getInt("C_Order_ID");
						if (neworden!=null && neworden!=0)
						{
							mTab.setValue("C_OrderPreventa_ID", rs.getInt("C_Order_ID") );
							mensaje="Existe Preventa Con reserva Activa";
						}
					
					}
					DB.close(rs, pstmt);
					
			    }
			
			
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
			return e.getLocalizedMessage();
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		return ""+mensaje;
	}	//	preVenta

}	//	CalloutOrderLineCW
