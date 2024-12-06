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
package org.windsor.model;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.model.MClient;
import org.compiere.model.MInvoice;

import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;


/**
 *	
 *
 *  @author 
 */
public class ModelInvoiceFolio implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelInvoiceFolio ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelInvoiceFolio.class);
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
		
			
		engine.addModelChange(MInvoice.Table_Name, this);
		engine.addDocValidate(MInvoice.Table_Name, this);
	}	//	initializes

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		if(po.get_TableName().equals(MInvoice.Table_Name))
		{
			if(type==TYPE_BEFORE_NEW  || type==TYPE_BEFORE_CHANGE)
			{
		
			//	X_C_SolicitudNC bp = (X_C_SolicitudNC) po;
			    
				if (po.get_ValueAsInt("AD_Client_ID") == 1000000 && po.get_ValueAsString("DocStatus").equals("DR") )
				{
					
				
				 
						  
			//	MInvoice invoice = new MInvoice (po.getCtx(),po.get_ValueAsInt("C_Invoice_ID"), po.get_TrxName()) ;	
				
				Boolean resultado = false;
				
				try
				{
					Integer.parseInt(po.get_ValueAsString("DocumentNo").replace("<", "").replace(">", "") );
					resultado = true;
				}
				catch (NumberFormatException excepcion)
				{
					//String numero = po.get_ValueAsString("DocumentNo").replace("<", "").replace(">", "");
					resultado = false;
				
				}
				int documento = 0;
				if(	resultado)
				 documento= Integer.parseInt(po.get_ValueAsString("DocumentNo").replace("<", "").replace(">", ""));
				else
					return "";
				
				String sql ="Select count (*) as cuenta  "+
						  " from C_ControlFolioLine cfl " +
						  " inner join c_controlfolio cf on (cfl.c_controlfolio_ID=cf.c_controlfolio_ID) " +
						  " where cf.C_DocTypeTarget_ID="+ po.get_ValueAsInt("C_DoctypeTarget_ID") +
						  " and cf.valido='Y'"+
						  " and cfl.Folio= " +documento;
				
				
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try
					{
						pstmt = DB.prepareStatement(sql, null);
						rs = pstmt.executeQuery();
						
						if (rs.next())
							{
								//si no existe control de folios
								if (rs.getInt("cuenta")==0 )
									return "";
								else
								{
									
									/*
									 * Aca se programa el control de folio si hay uno disponible menor al actual
									 * 
									 */
									
									String sqlfolio ="Select min (folio) as folio  "+
											  " from C_ControlFolioLine cfl " +
											  " inner join c_controlfolio cf on (cfl.c_controlfolio_ID=cf.c_controlfolio_ID) " +
											  " where cf.C_DocTypeTarget_ID="+ po.get_ValueAsInt("C_DoctypeTarget_ID")  +
											  " and cf.valido='Y'"+
											  " and cfl.disponible='Y' and cfl.Folio<= " +documento;
									
									PreparedStatement pstmtfolio = null;
									ResultSet rsfolio = null;
									try
									{
										pstmtfolio = DB.prepareStatement(sqlfolio, null);
										rsfolio = pstmtfolio.executeQuery();
										
										while (rsfolio.next())
										{
											po.set_ValueOfColumn("DocumentNo",   Integer.toString(rsfolio.getInt("folio") ) );
										//	invoice.save();
											
											
											//marcar como no disponible
											String sqlidfolio = "Select min(c_controlfolioline_ID) as c_controlfolioline_ID  "+
													  " from C_ControlFolioLine cfl " +
													  " inner join c_controlfolio cf on (cfl.c_controlfolio_ID=cf.c_controlfolio_ID) " +
													  " where cf.C_DocTypeTarget_ID="+ po.get_ValueAsInt("C_DoctypeTarget_ID")  +
													  " and cf.valido='Y'"+
													  " and cfl.disponible='Y' and cfl.Folio<= " +rsfolio.getInt("folio");
											PreparedStatement pstmtidfolio = null;
											ResultSet rsidfolio = null;
											try
											{
												pstmtidfolio = DB.prepareStatement(sqlidfolio, null);
												rsidfolio = pstmtidfolio.executeQuery();
												if(rsidfolio.next())
												{
											
													X_C_ControlFolioLine cfl = new X_C_ControlFolioLine (po.getCtx(),rsidfolio.getInt("c_controlfolioline_ID"), po.get_TrxName()) ;
													cfl.setDisponible(false);
												//	cfl.setC_Invoice_ID(invoice.getC_Invoice_ID());
												// cfl.setDocStatus(invoice.getDocStatus());
													cfl.save();
												}
											rsidfolio.close();  
											pstmtidfolio.close();
											DB.close(rsidfolio, pstmtidfolio);
											}
										catch (SQLException e)
											{
												log.log(Level.SEVERE, sqlfolio, e);
												return e.getLocalizedMessage();
											}
												
										finally
											{
												
												
											}
											
										}
										
										rsfolio.close();  
										pstmtfolio.close();
										DB.close(rsfolio, pstmtfolio);
									}
									catch (SQLException e)
									{
										log.log(Level.SEVERE, sqlfolio, e);
										return e.getLocalizedMessage();
									}
										
								finally
									{
										
										
									}
									
								}
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
						
							
				} 
				
				
				
				}
			}
			
		
			
	return null;
	}	//	modelChange

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

		if(timing == TIMING_BEFORE_COMPLETE && po.get_Table_ID()== MInvoice.Table_ID)  
		{
			MInvoice invoice = (MInvoice) po;
			
			Boolean resultado = false;
			
			try
			{
				Integer.parseInt(invoice.getDocumentNo());
				resultado = true;
			}
			catch (NumberFormatException excepcion)
			{
				resultado = false;
			
			}
			int documento = 0;
			if(	resultado)
			 documento= Integer.parseInt( invoice.getDocumentNo());
			else
				return "";
			String sql ="Select count (*) as cuenta  "+
					  " from C_ControlFolioLine cfl " +
					  " inner join c_controlfolio cf on (cfl.c_controlfolio_ID=cf.c_controlfolio_ID) " +
					  " where cf.C_DocTypeTarget_ID="+ invoice.getC_DocTypeTarget_ID() +
					  " and cf.valido='Y'"+
					  " and cfl.Folio= " +documento;
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, null);
				rs = pstmt.executeQuery();
				
				if (rs.next())
					{
						//si no existe control de folios
						if (rs.getInt("cuenta")==0 )
							return "";
					
						
							String sqlidfolio = "Select min(c_controlfolioline_ID) as c_controlfolioline_ID  "+
									  " from C_ControlFolioLine cfl " +
									  " inner join c_controlfolio cf on (cfl.c_controlfolio_ID=cf.c_controlfolio_ID) " +
									  " where cf.C_DocTypeTarget_ID="+ invoice.getC_DocTypeTarget_ID() +
									  " and cf.valido='Y'"+
									  " and  cfl.folio= " +  documento;// invoice.getC_Invoice_ID()  ;
							PreparedStatement pstmtidfolio = null;
							ResultSet rsidfolio = null;
							try
							{
								pstmtidfolio = DB.prepareStatement(sqlidfolio, null);
								rsidfolio = pstmtidfolio.executeQuery();
								if(rsidfolio.next())
								{
							
									X_C_ControlFolioLine cfl = new X_C_ControlFolioLine (po.getCtx(),rsidfolio.getInt("c_controlfolioline_ID"), po.get_TrxName()) ;
									cfl.setDisponible(false);
									cfl.setC_Invoice_ID(invoice.getC_Invoice_ID());
									cfl.setDocStatus("CO");
									cfl.save();
								}
							rsidfolio.close();  
							pstmtidfolio.close();
							DB.close(rsidfolio, pstmtidfolio);
							}
						catch (SQLException e)
							{
								log.log(Level.SEVERE, sqlidfolio, e);
								return e.getLocalizedMessage();
							}
								
						finally
							{
								
								
							}
						
					
							
					
					}
				
				
				
				
				
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
			
				catch (SQLException e)
				{
					log.log(Level.SEVERE, sql, e);
					return e.getLocalizedMessage();
				}
					
			finally
				{
					
				
				}
			
		}
		
		if(timing == TIMING_BEFORE_VOID && po.get_Table_ID()== MInvoice.Table_ID)  
		{
			MInvoice invoice = (MInvoice) po;
			
			Boolean resultado = false;
			
			try
			{
				Integer.parseInt(invoice.getDocumentNo());
				resultado = true;
			}
			catch (NumberFormatException excepcion)
			{
				resultado = false;
			
			}
			int documento = 0;
			if(	resultado)
			 documento= Integer.parseInt( invoice.getDocumentNo());
			else
				return "";
			String sql ="Select count (*) as cuenta  "+
					  " from C_ControlFolioLine cfl " +
					  " inner join c_controlfolio cf on (cfl.c_controlfolio_ID=cf.c_controlfolio_ID) " +
					  " where cf.C_DocTypeTarget_ID="+ invoice.getC_DocTypeTarget_ID() +
					  " and cf.valido='Y'"+
					  " and cfl.Folio= " +documento;
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, null);
				rs = pstmt.executeQuery();
				
				if (rs.next())
					{
						//si no existe control de folios
						if (rs.getInt("cuenta")==0 )
							return "";
					
							/*
							 * al anular la factura actualiza el estado de documento en el control de folio.
							 */
							
							
								
											//si existe control de folio se debe actualizar
											String sqlidfolio = "Select min(c_controlfolioline_ID) as c_controlfolioline_ID  "+
													  " from C_ControlFolioLine cfl " +
													  " inner join c_controlfolio cf on (cfl.c_controlfolio_ID=cf.c_controlfolio_ID) " +
													  " where cf.C_DocTypeTarget_ID="+ invoice.getC_DocTypeTarget_ID() +
													  " and cf.valido='Y'"+
													  " and  cfl.c_invoice_ID= " + invoice.getC_Invoice_ID()  ;
											PreparedStatement pstmtidfolio = null;
											ResultSet rsidfolio = null;
											try
											{
												pstmtidfolio = DB.prepareStatement(sqlidfolio, null);
												rsidfolio = pstmtidfolio.executeQuery();
												if(rsidfolio.next())
												{
											
													X_C_ControlFolioLine cfl = new X_C_ControlFolioLine (po.getCtx(),rsidfolio.getInt("c_controlfolioline_ID"), po.get_TrxName()) ;
													cfl.setDisponible(true);
													cfl.setC_Invoice2_ID(invoice.getC_Invoice_ID());
													cfl.setC_Invoice_ID(0);
													cfl.setDocStatus("VO");
													cfl.setDOCSTATUS2("VO");
													cfl.save();
													po.set_ValueOfColumn("C_DocType_ID",   1000046 );
													po.set_ValueOfColumn("C_DocTypeTarget_ID",   1000046 );
													invoice.setC_DocType_ID(1000046);
													invoice.setC_DocTypeTarget_ID(1000046);
													invoice.save();
												}
											rsidfolio.close();  
											pstmtidfolio.close();
											DB.close(rsidfolio, pstmtidfolio);
											}
										catch (SQLException e)
											{
												log.log(Level.SEVERE, sqlidfolio, e);
												return e.getLocalizedMessage();
											}
												
										finally
											{
												
												
											}
											//si existe control de folio se deve actualizar
											
										
									
							
							
					
					}
				
				
				
				
				
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
			
				catch (SQLException e)
				{
					log.log(Level.SEVERE, sql, e);
					return e.getLocalizedMessage();
				}
					
			finally
				{
					
				
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


	

}	