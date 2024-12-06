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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
//import java.util.Properties;
import java.util.logging.*;

import org.compiere.model.MInvoice;
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
public class CrearControlFolioDetalle extends SvrProcess
{
	//private Properties 		m_ctx;	
	private int cfolioID=0; 
	private ArrayList<Integer> rangofolio = new ArrayList<Integer>(); 
	private ArrayList<Integer> idfactura = new ArrayList<Integer>(); 
	      
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
			else if (name.equals("C_ControlFolio_ID"))
				cfolioID= para[i].getParameterAsInt();
				
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
	
		
	String mensaje="";
	
	   if (cfolioID==0){
		   return "No existe folio para procesar";
	   }
		
	   
		String sql = "select count (*) as cuenta from c_controlfolio where Valido = 'Y' and IsActive='Y' and c_controlfolio_ID="+cfolioID;
		
		try
		{
			PreparedStatement pstmt = null;
			pstmt = DB.prepareStatement(sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			
			
			
			
			if (rs.next())
				if (rs.getInt("cuenta")==0)
				{
					
					return "no es un rango folios valido";
				}
				else
				{
					mensaje="los folios son validos";
					String sqlinec = "select count (*) as cuenta from C_controlFolioLine where isactive='Y' and c_controlfolio_ID="+cfolioID;
					try
					{
						PreparedStatement pstmtlinec = null;
						pstmtlinec = DB.prepareStatement(sqlinec, get_TrxName());
						ResultSet rslinec = pstmtlinec.executeQuery();
						
						if (rslinec.next())
							if(rslinec.getInt("cuenta")==0)
							{
								mensaje = "no existe detalle, se crea";
								//crear todas las lineas y validarlas
								X_C_ControlFolio cf = new X_C_ControlFolio(getCtx(), cfolioID, get_TrxName());
								int x= cf.getRangoInicial() ;
								int l= 10;
								while (x<= cf.getRangoFinal() )
								{
									X_C_ControlFolioLine cfl = new X_C_ControlFolioLine(getCtx(), 0, get_TrxName());
									cfl.setFOLIO(x);
									
									cfl.setLine(l);
									cfl.setDisponible(true);
									cfl.set_CustomColumn("C_ControlFolio_ID", cfolioID);
									cfl.save();
									
									x=x+1;
									l=l+10;
								}
								
								
							}
							
							
						rslinec.close();
						pstmtlinec.close();
						DB.close(rslinec, pstmtlinec);
								
						
							
					}
					catch (Exception e)
					{
						
						log.log(Level.SEVERE, e.getMessage(), e);
					}
					finally
					{
						
									
					}
					//validar todas las lineas si tienen facturas asociadas
					
					
					
					//aqui se rescatan todos los folios no anulados del tipo de documento 
					X_C_ControlFolio cf = new X_C_ControlFolio(getCtx(), cfolioID, get_TrxName());
					String sqlinvoicec ="Select * from c_invoice where c_doctypetarget_ID="+cf.getC_DocTypeTarget_ID() +
							" and DocStatus<>'VO'";
					try
					{
						PreparedStatement pstmtinvoicec = null;
						pstmtinvoicec = DB.prepareStatement(sqlinvoicec, get_TrxName());
						ResultSet rsinvoicec = pstmtinvoicec.executeQuery();
						while (rsinvoicec.next())
						{
							
							Boolean resultado = false;
							
							try
							{
								Integer.parseInt(rsinvoicec.getString("DocumentNo"));
								resultado = true;
							}
							catch (NumberFormatException excepcion)
							{
								resultado = false;
							}
							if (resultado)
							{
								rangofolio.add(Integer.parseInt(rsinvoicec.getString("DocumentNo")));
								idfactura.add(rsinvoicec.getInt("C_Invoice_ID"));
							}
						}
						rsinvoicec.close();
						pstmtinvoicec.close();
						DB.close(rsinvoicec, pstmtinvoicec);
					}
					catch (Exception e)
					{
						
						log.log(Level.SEVERE, e.getMessage(), e);
					}
					finally
					{
						
									
					}
					
					
					
					
					//aqui se rescatan todos los folios no anulados del tipo de documento
					
					mensaje = mensaje + " aqui ya existen las lineas";
					String sqline = "select * from c_controlfolioline where c_controlfolio_ID="+cfolioID;
					try
					{
						PreparedStatement pstmtline = null;
						pstmtline = DB.prepareStatement(sqline, get_TrxName());
						ResultSet rsline = pstmtline.executeQuery();
						while (rsline.next()) 
						{
							mensaje = "recorro  folios" + rsline.getInt("Folio");
							X_C_ControlFolioLine cfl = new X_C_ControlFolioLine (getCtx(), rsline.getInt("C_ControlFolioLine_ID"), get_TrxName());
							
							if (cfl.get_ValueAsBoolean("IsActive"))//si la linea esta activa
							{
								
								mensaje = "el folio esta activo?" ; 
							//String sqlinvoicec ="Select * from c_invoice where c_doctypetarget_ID="+cf.getC_DocTypeTarget_ID() +
								//" and DocStatus<>'VO'";
											//	" and to_number(documentno)= "+cfl.getFOLIO();
							
								for ( int i=0; i < rangofolio.size() ; i++ ) 
								{
									if ( cfl.getFOLIO() == rangofolio.get(i).intValue()  )
									{
										MInvoice invoice = new MInvoice (getCtx(), idfactura.get(i), get_TrxName());
										cfl.setC_Invoice_ID(idfactura.get(i));
										cfl.setDisponible(false);
										cfl.setDocStatus(invoice.getDocStatus());
										cfl.save();
									}
								}
							}
						}
						rsline.close();
						pstmtline.close();
						DB.close(rsline, pstmtline);
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
						
				
		return mensaje;		
				
			
	}	//	doIt
	
}	//	OrderOpen
