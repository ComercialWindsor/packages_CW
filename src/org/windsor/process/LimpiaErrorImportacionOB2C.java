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
import java.util.ArrayList;
import java.util.logging.*;

//import org.compiere.model.MOrder;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;
import org.windsor.model.X_C_OrderB2C;
import org.windsor.model.X_C_OrderB2CLine;



//import org.compiere.util.Env;
 
/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class LimpiaErrorImportacionOB2C extends SvrProcess
{
	//private Properties 		m_ctx;	


	      
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
				;
				
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
	
		
		StringBuffer sql = null;
		int no = 0;
		int noaux=0;
		Boolean encuentra=false;
		//	****	Prepare	****

		//	Delete Old Imported
		
			
			
			String sqldocs = "Select count (b2c.c_orderb2c_ID) Cuenta "+
							 " from c_orderb2c b2c "+
							 " inner join c_orderb2cLine b2cl on (b2c.c_orderb2c_ID=b2cl.c_orderb2c_ID) "+
							 " inner join c_order o on (b2cl.c_order_ID=o.c_order_ID)"+
							 " where o.docstatus<>'CO'";
			
			try
			{
				PreparedStatement pstmtdocs = null;
				pstmtdocs = DB.prepareStatement(sqldocs, get_TrxName());
				ResultSet rsdocs = pstmtdocs.executeQuery();
				
				if (rsdocs.next())
					if (rsdocs.getInt("cuenta")>0)
						encuentra=true;
				rsdocs.close();
				pstmtdocs.close();
				DB.close(rsdocs, pstmtdocs);
						
				
					
			}
			catch (Exception e)
			{
				
				log.log(Level.SEVERE, e.getMessage(), e);
			}
			finally
			{
				
							
			}
			
			
			if(encuentra)
			{
					sqldocs = "Select b2c.c_orderb2c_ID "+
						 " from c_orderb2c b2c "+
						 " inner join c_orderb2cLine b2cl on (b2c.c_orderb2c_ID=b2cl.c_orderb2c_ID) "+
						 " inner join c_order o on (b2cl.c_order_ID=o.c_order_ID) "+
						 " where o.docstatus<>'CO' "+
						 " group by b2c.c_orderb2c_ID";
					ArrayList<Integer> idborrar = new ArrayList<Integer>();
					ArrayList<Integer> idov = new ArrayList<Integer>();
					try
					{
						PreparedStatement pstmtdocs = null;
						pstmtdocs = DB.prepareStatement(sqldocs, get_TrxName());
						ResultSet rsdocs = pstmtdocs.executeQuery();
						
						while (rsdocs.next())
						{
							X_C_OrderB2C b2c = new X_C_OrderB2C (getCtx(), rsdocs.getInt("c_orderb2c_ID"), get_TrxName());
							idborrar.add(b2c.getC_OrderB2C_ID());
							String sqllines = "Select * from c_orderb2cLine b2cl where b2cl.C_OrderB2C_ID="+b2c.getC_OrderB2C_ID();
							try
							{
								PreparedStatement pstmtlines = null;
								pstmtlines = DB.prepareStatement(sqllines, get_TrxName());
								ResultSet rslines = pstmtlines.executeQuery();
								
								while (rslines.next())
								{
									X_C_OrderB2CLine b2cl = new X_C_OrderB2CLine (getCtx(), rslines.getInt("c_orderb2cline_ID"), get_TrxName());
									
									idov.add(b2cl.getC_Order_ID())	;	
								}
								rslines.close();
								pstmtlines.close();
								DB.close(rslines, pstmtlines);
										
								
									
							}
							catch (Exception e)
							{
								
								log.log(Level.SEVERE, e.getMessage(), e);
							}
							finally
							{
								
											
							}
							
						}

						rsdocs.close();
						pstmtdocs.close();
						DB.close(rsdocs, pstmtdocs);
								
						
							
					}
					catch (Exception e)
					{
						
						log.log(Level.SEVERE, e.getMessage(), e);
					}
					finally
					{
						
									
					}
				
			
					for ( int i=0; i < idov.size() ; i++ )
					{
						
					
					sql = new StringBuffer ("DELETE C_Order "
						  + "WHERE  c_order_ID="+idov.get(i) );
					no = DB.executeUpdate(sql.toString(), get_TrxName());
					log.fine("Delete Order =" + no);
					
			
					}
					for ( int i=0; i < idborrar.size() ; i++ )
					{
						
					
					sql = new StringBuffer ("DELETE C_Orderb2c "
						  + "WHERE  c_orderb2c_ID="+idborrar.get(i) );
					no = DB.executeUpdate(sql.toString(), get_TrxName());
					log.fine("Delete Order =" + no);
					
					noaux=noaux+no;
					}
					
					
			
			
			} //encuentra ordenes con ov no completas
			log.fine("Delete Old Impored =" + noaux);
			
			
			//las no processed
			 sqldocs = "Select count (b2c.c_orderb2c_ID) Cuenta "+
					 " from c_orderb2c b2c "+
					 " where b2c.processed<>'Y'";
			 encuentra = false;
	
	try
	{
		PreparedStatement pstmtdocs = null;
		pstmtdocs = DB.prepareStatement(sqldocs, get_TrxName());
		ResultSet rsdocs = pstmtdocs.executeQuery();
		
		if (rsdocs.next())
			if (rsdocs.getInt("cuenta")>0)
				encuentra=true;
		rsdocs.close();
		pstmtdocs.close();
		DB.close(rsdocs, pstmtdocs);
				
		
			
	}
	catch (Exception e)
	{
		
		log.log(Level.SEVERE, e.getMessage(), e);
	}
	finally
	{
		
					
	}
	
	
	if(encuentra)
	{
			sqldocs = "Select b2c.c_orderb2c_ID "+
				 " from c_orderb2c b2c "+
				 " where b2c.processed<>'Y' ";

			
			try
			{
				PreparedStatement pstmtdocs = null;
				pstmtdocs = DB.prepareStatement(sqldocs, get_TrxName());
				ResultSet rsdocs = pstmtdocs.executeQuery();
				
				while (rsdocs.next())
				{
					sql = new StringBuffer ("DELETE C_Orderb2c "
							  + "WHERE  c_orderb2c_ID="+rsdocs.getInt("c_orderb2c_ID"));
						no = DB.executeUpdate(sql.toString(), get_TrxName());
						log.fine("Delete Order =" + no);
			
					noaux=noaux+no;
					
				}

				rsdocs.close();
				pstmtdocs.close();
				DB.close(rsdocs, pstmtdocs);
						
				
					
			}
			catch (Exception e)
			{
				
				log.log(Level.SEVERE, e.getMessage(), e);
			}
			finally
			{
				
							
			}
		
	
				
			
		
			
			
			
	
	
	} //no processed
			
	//commitEx();
		return "Registros limpiados";		
				
			
	}	//	doIt
	
}	//	OrderOpen
