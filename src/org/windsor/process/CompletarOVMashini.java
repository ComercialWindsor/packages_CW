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



//import java.util.Properties;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;




//import org.compiere.util.Env;
 
/**
 *	
 *	
 *  @author Guillermo Rojas, SinnaIT
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class CompletarOVMashini extends SvrProcess
{
	//private Properties 		m_ctx;	


	      
	//private int p_order = 0;
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
				;//p_order=para[i].getParameterAsInt();
				
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	//	m_ctx = Env.getCtx();
		
		//int cliente = Env.getAD_User_ID(getCtx());
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message
	 * @throws SQLException 
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws AdempiereSystemError, SQLException
	{
		/*********/
	
		int Cantidad =0;
		int total= 0;
		String sql = "select c_order_ID , unidadesmuro from c_order where c_bpartner_ID=1001237 and c_bpartner_location_ID=1011338 and docstatus in ('DR') and unidadesmuro>0";
		
		try
		{
			PreparedStatement pstmt = null;
			pstmt = DB.prepareStatement(sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next())
			{
				String sqlline = "select sum(QtyEntered ) Cantidad from c_orderline where NotPrint<>'Y' and QtyEntered>0 and C_order_ID="+rs.getInt("c_order_ID") ;
				
				try
				{
					PreparedStatement pstmtl = null;
					pstmtl = DB.prepareStatement(sqlline, get_TrxName());
					ResultSet rsl = pstmtl.executeQuery();
					
					while (rsl.next())
					{
						total++;
						MOrder o = new MOrder (getCtx(), rs.getInt("c_order_ID"), get_TrxName());
						if(rsl.getInt("Cantidad")==rs.getInt("unidadesmuro"))
						{
							
							
														
								
						//	minout.processIt(X_M_InOut.DOCACTION_Complete);
						//	minout.save();
								o.processIt(MOrder.DOCACTION_Complete );
								
								o.completeIt();
								o.setDocAction(MOrder.DOCACTION_Close);
								o.save();
								o.setProcessed(true);
								o.set_CustomColumn("FIRMA2", "Y");
								o.save();
								if (o.getDocStatus().equals("CO"))
								{
									//o.set_CustomColumn("FIRMA2", "Y");
									o.set_ValueOfColumn("CompletarMashiniMsg", "Unidades Cuadradas, se procede a completar");
									Cantidad++;
									o.save();
								}
									
								else
								{
									o.set_ValueOfColumn("CompletarMashiniMsg", "Ocurrio un error, posiblemente de stock, favor validar");
									o.processIt(MOrder.DOCSTATUS_Drafted);
									
									o.save();
								}
									
						}
						else
						{
							o.set_ValueOfColumn("CompletarMashiniMsg", "Unidades no cuandran con la nota en Muro");
							o.save();
						}
					}
					
					
					rsl.close();
					pstmtl.close();
					DB.close(rsl, pstmtl);
										
								
									
				}
				catch (Exception e)
				{
								
						log.log(Level.SEVERE, e.getMessage(), e);
				}
				finally
				{
								
											
				}
			}
			
			
			rs.close();
			pstmt.close();
			DB.close(rs, pstmt);
								
						
							
		}
		catch (Exception e)
		{
						
				log.log(Level.SEVERE, e.getMessage(), e);
		}
		finally
		{
						
									
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
				
		
	/*	StringBuffer sql = null;
		int no = 0;


		//	****	Prepare	****

		//	Delete Old Imported
		
			sql = new StringBuffer ("DELETE I_OrderB2C "
				  + "WHERE  ad_client_ID=1000000");
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
			
			commitEx();*/
		
		return "Se completan "+Cantidad+" de " + total + " ordenes Mashini";		
				
			
	}	//	doIt
	
}	//	OrderOpen
