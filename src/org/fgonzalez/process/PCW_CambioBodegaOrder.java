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
package org.fgonzalez.process;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.*;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;

 
/**
 *	Actualiza bodega en una OC
 *	
 *  @author fernando gonzalez
 *  @version $Id: PCW_CambioBodegaOrder.java,v 1.2 2018/08/29 $
 */
public class PCW_CambioBodegaOrder extends SvrProcess
{
	//private Properties 		m_ctx;

	private int 		p_Order_ID = 0;

	private int 		p_WareHouse_ID = 0;
	
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
			
			else if (name.equals("C_Order_ID"))
				p_Order_ID = para[i].getParameterAsInt();
			else if (name.equals("M_Warehouse_ID"))
				p_WareHouse_ID = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	//m_ctx = Env.getCtx();
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws AdempiereSystemError
	{
		/*********/

		
		
		
		
		
		if (p_Order_ID>0)
		{
			
			String sql ="Select max(io.M_Inout_ID)M_Inout_ID from m_inout io "+
					    " where io.C_Order_ID="+p_Order_ID;	
						PreparedStatement pstmt = null;
						ResultSet rs = null;
			int inout_id=0;
						try
						{
							
							pstmt = DB.prepareStatement(sql, null);
							rs = pstmt.executeQuery();
							while (rs.next())
							{
								inout_id= rs.getInt("M_Inout_ID");
							}
							if(inout_id>0) 
							{
								return "La orden tiene recibos asociados";
								
							}
							else {
								DB.executeUpdate("Update C_Order set M_Warehouse_ID=" +p_WareHouse_ID+ " where c_order_ID="+p_Order_ID, this.get_TrxName());
								DB.executeUpdate("Update C_OrderLine set M_Warehouse_ID=" +p_WareHouse_ID+ " where c_order_ID="+p_Order_ID, this.get_TrxName());
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
						
		
				return "Cambio Exitoso";
		}
		return "No se realiza cambio";
	}	//	doIt
	
}	//	OrderOpen
