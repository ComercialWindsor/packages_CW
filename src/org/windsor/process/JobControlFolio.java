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
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
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
 *  @version $Id: JobControlFiolio.java,v 1.2 2018/11/18 $
 */
public class JobControlFolio extends SvrProcess
{
	//private Properties 		m_ctx;	
	private String sqlhead = "select * from C_controlFolio where isactive='Y' and Valido='Y' "; 
	private ArrayList<Integer> rangofolio = new ArrayList<Integer>(); 
	private ArrayList<Integer> idfactura = new ArrayList<Integer>(); 
//	private int p_Issue = 0;
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	//variables de ccorreo
	final String miCorreo = "soporte@comercialwindsor.cl";
    final String miContraseña = "Cw9121100";
    final String servidorSMTP = "smtp.gmail.com";
    final String puertoEnvio = "465";
    String mailReceptor = null;
    String asunto = null;
    String cuerpo = null;
	
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_ControlFolio_ID"))
				//cfolioID= para[i].getParameterAsInt();
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
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws AdempiereSystemError
	{
		/*********/
	
		
	//String mensaje="";
	  
	 	
	   
		String sql = "select count (*) as cuenta from c_controlfolio where Valido = 'Y' and IsActive='Y' ";
		
		try
		{ 
			PreparedStatement pstmt = null;
			pstmt = DB.prepareStatement(sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			
			
			
			
			if (rs.next())
				if (rs.getInt("cuenta")==0)
				{
					
					return "";
				}
				else
				{ //existen folios validos
				//	mensaje="los folios son validos";
					
					try
					{
						PreparedStatement pstmthead = null;
						pstmthead = DB.prepareStatement(sqlhead, get_TrxName());
						ResultSet rshead = pstmthead.executeQuery();
						
						while (rshead.next())
						{
							
						
							String sqllinec = "select count (*) as cuenta from C_controlFolioLine where isactive='Y' and c_controlfolio_ID="+rshead.getInt("C_ControlFolio_ID");
							
							try
							{
								PreparedStatement pstmtlinec = null;
								pstmtlinec = DB.prepareStatement(sqllinec, get_TrxName());
								ResultSet rslinec = pstmtlinec.executeQuery();
								while (rslinec.next())
								if(rslinec.getInt("cuenta")==0)
								{
									//mensaje = "no existe detalle, se crea";
									//crear todas las lineas y validarlas
								//	X_C_ControlFolio cf = new X_C_ControlFolio(getCtx(), rshead.getInt("C_ControlFolio_ID"), get_TrxName());
									int x= rshead.getInt("RangoInicial") ;
									int l= 10;
									while (x<= rshead.getInt("RangoFinal") )
									{
										X_C_ControlFolioLine cfl = new X_C_ControlFolioLine(getCtx(), 0, get_TrxName());
										cfl.setFOLIO(x);
										cfl.setLine(l);
										cfl.setDisponible(true);
										cfl.set_CustomColumn("C_ControlFolio_ID", rshead.getInt("C_ControlFolio_ID"));
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
								
								//mensaje="creadas todas las lineas si no existian";			
							}
						
						}	
							
						rshead.close();
						pstmthead.close();
						DB.close(rshead, pstmthead);
								
						
							
					}
					catch (Exception e)
					{
						
						log.log(Level.SEVERE, e.getMessage(), e);
					}
					finally
					{
						
									
					}
					
					
					
					//ahora a validar folios ocuapdos en un arraylist
					try
					{
						PreparedStatement pstmthead = null;
						pstmthead = DB.prepareStatement(sqlhead, get_TrxName());
						ResultSet rshead = pstmthead.executeQuery();
						while (rshead.next())
						{
							String sqlvalidar = "Select * from c_invoice where c_doctypetarget_ID="+rshead.getInt("C_DoctypeTarget_ID") +
									" and DocStatus<>'VO'"; 
							
							try {
								
							
								PreparedStatement pstmtvalidar = null;
								pstmtvalidar = DB.prepareStatement(sqlvalidar, get_TrxName());
								ResultSet rsvalidar = pstmtvalidar.executeQuery();
								while (rsvalidar.next())
								{
									
									Boolean resultado = false;
									
									try
									{
										Integer.parseInt(rsvalidar.getString("DocumentNo"));
										resultado = true;
									}
									catch (NumberFormatException excepcion)
									{
										resultado = false;
									}
									if (resultado)
									{
										rangofolio.add(Integer.parseInt(rsvalidar.getString("DocumentNo")));
										idfactura.add(rsvalidar.getInt("C_Invoice_ID"));
									}
								}//while obtengo facturas ocupadas
								rsvalidar.close();
								pstmtvalidar.close();
								DB.close(rsvalidar, pstmtvalidar);
							}
												
							catch (Exception e)
							{
								
								log.log(Level.SEVERE, e.getMessage(), e);
							}
							finally
							{
								
											
							}
								
								
							}
							
						rshead.close();
						pstmthead.close();
						DB.close(rshead, pstmthead);
					}//ahora a validar folios ocuapdos en un arraylist
					catch (Exception e)
					{
						
						log.log(Level.SEVERE, e.getMessage(), e);
					}
					finally
					{
						
									
					}
					//ahora a validar folios ocuapdos
					try
					{
						PreparedStatement pstmthead = null;
						pstmthead = DB.prepareStatement(sqlhead, get_TrxName());
						ResultSet rshead = pstmthead.executeQuery();
						while (rshead.next())
						{
							String sqline = "select * from c_controlfolioline where c_controlfolio_ID="+rshead.getInt("C_ControlFolio_ID");
							try
							{
								PreparedStatement pstmtline = null;
								pstmtline = DB.prepareStatement(sqline, get_TrxName());
								ResultSet rsline = pstmtline.executeQuery();
								while (rsline.next()) 
								{
									//mensaje = "recorro  folios" + rsline.getInt("Folio");
									X_C_ControlFolioLine cfl = new X_C_ControlFolioLine (getCtx(), rsline.getInt("C_ControlFolioLine_ID"), get_TrxName());
									
									if (cfl.get_ValueAsBoolean("IsActive"))//si la linea esta activa
									{
										
									//mensaje = "el folio esta activo?" ; 
									//String sqlinvoicec ="Select * from c_invoice where c_doctypetarget_ID="+cf.getC_DocTypeTarget_ID() +
										//" and DocStatus<>'VO'";
													//	" and to_number(documentno)= "+cfl.getFOLIO();
									
										for ( int i=0; i < rangofolio.size() -1; i++ ) 
										{
											if ( cfl.getFOLIO() == rangofolio.get(i).intValue()  )
											{
												MInvoice invoice = new MInvoice (getCtx(), idfactura.get(i), get_TrxName());
												cfl.setC_Invoice_ID(idfactura.get(i));
												cfl.setDisponible(false);
												cfl.setDocStatus(invoice.getDocStatus());
												cfl.save();
												break;
											}
										}//for que valida los con los folios existentes
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
						
						rshead.close();
						pstmthead.close();
						DB.close(rshead, pstmthead);
					}//ahora a validar folios ocuapdos en un arraylist
					catch (Exception e)
					{
						
						log.log(Level.SEVERE, e.getMessage(), e);
					}
					finally
					{
						
									
					}
					
					//ahora a validar folios ocuapdos
					
					
				}//existen folios validos y ya se crearon sus lineas, ELSE
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
				
					//mensaje =  " aqui ya existen las lineas";
					
	 //ver disponibilidad
	//contar para enviar correo
		 sql = "select count (*) as cuenta from c_controlfolio where Valido = 'Y' and IsActive='Y' ";
			
			try
			{ 
				PreparedStatement pstmt = null;
				pstmt = DB.prepareStatement(sql, get_TrxName());
				ResultSet rs = pstmt.executeQuery();
				
				if (rs.next())
					if (rs.getInt("cuenta")==0)
					{
						
						return "";
					}
					else
					{
						String sqlhead ="Select cf.c_controlfolio_ID, dt.name, cf.C_Doctypetarget_ID, cf.rangoinicial, cf.rangofinal, cf.minimodisponible, cf.AlertaEmail, u.EMailUser from c_controlfolio cf "+
										" inner join C_Doctype dt on (cf.C_Doctypetarget_ID=dt.c_doctype_ID) "+
										" inner join ad_user u on(cf.ad_user_ID=u.ad_user_ID) "+
								        " where cf.Valido = 'Y' and cf.IsActive='Y'";
						
						try
						{ 
							PreparedStatement pstmthead = null;
							pstmthead = DB.prepareStatement(sqlhead, get_TrxName());
							ResultSet rshead = pstmthead.executeQuery();
							
							while (rshead.next())
							{
								String sqldisponible = "Select count(*) as disponible from c_controlfolioline cfl "+
													" where cfl.isactive='Y' and cfl.disponible='Y' and cfl.c_controlfolio_ID="+rshead.getInt("C_ControlFolio_ID");
													
								try
								{ 
									PreparedStatement pstmtdisponible= null;
									pstmtdisponible = DB.prepareStatement(sqldisponible, get_TrxName());
									ResultSet rsdisponible = pstmtdisponible.executeQuery();
									
									if(rsdisponible.next())
										if(rshead.getString("AlertaEmail").equals("Y"))
										if(rsdisponible.getInt("disponible")<=rshead.getInt("minimodisponible"))
										{
											 Properties props = new Properties();
										        props.put("mail.smtp.user", miCorreo);
										        props.put("mail.smtp.host", servidorSMTP);
										        props.put("mail.smtp.port", puertoEnvio);
										        props.put("mail.smtp.starttls.enable", "true");
										        props.put("mail.smtp.auth", "true");
										        props.put("mail.smtp.socketFactory.port", puertoEnvio);
										        props.put("mail.smtp.socketFactory.class",
										                "javax.net.ssl.SSLSocketFactory");
										        props.put("mail.smtp.socketFactory.fallback", "false");

										      //  SecurityManager security = System.getSecurityManager();

										        try {
										            Authenticator auth = new autentificadorSMTP();
										            Session session = Session.getInstance(props, auth);
										            // session.setDebug(true);

										            MimeMessage msg = new MimeMessage(session);
										            msg.setText("Estimado usuario para el tipo de documento: "+rshead.getString("name") +" con rango de folios: "+
										            rshead.getInt("rangoinicial") + " hasta: " + rshead.getInt("rangofinal") + " quedan solo "+rsdisponible.getInt("disponible")+" folios disponibles" );
										            msg.setSubject("Aviso Control Folio, quedan pocos!!");
										            msg.setFrom(new InternetAddress(miCorreo));
										            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
										            		rshead.getString("emailuser")));
										            Transport.send(msg);
										            
										        } catch (Exception mex) {
										            mex.printStackTrace();
										        }
										}
									
									
									rsdisponible.close();
									pstmtdisponible.close();
									DB.close(rsdisponible, pstmtdisponible);
												
								}
								catch (Exception e)
								{
									
									log.log(Level.SEVERE, e.getMessage(), e);
								}
								finally
								{
									
												
								}
							}
							
							rshead.close();
							pstmthead.close();
							DB.close(rshead, pstmthead);
										
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
					
				
				 		
		return "Proceso terminado";		
				
			
	}	//	doIt
	

	    private class autentificadorSMTP extends javax.mail.Authenticator {
	        public PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(miCorreo, miContraseña);
	        }
	    }
}	//	OrderOpen
