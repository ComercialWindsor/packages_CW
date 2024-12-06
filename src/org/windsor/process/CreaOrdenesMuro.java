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
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProductPrice;
import org.compiere.model.X_C_Order;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;
import org.windsor.model.X_C_RepoMuro;
import org.windsor.model.X_C_RepoMuroProduct;
import org.windsor.model.X_C_RepoMuroTienda;


//import org.compiere.util.Env;
 
/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class CreaOrdenesMuro extends SvrProcess
{
	//private Properties 		m_ctx;	


	      
	private int repomuro_id = 0;
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
			else if (name.equals("C_RepoMuro_ID"))
				repomuro_id=para[i].getParameterAsInt()  ;
				
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
	

		
		X_C_RepoMuro repo = new X_C_RepoMuro (getCtx() , repomuro_id,get_TrxName());
		
		String sqltiendas = "Select * from C_RepoMuroTienda where c_repoMuro_ID="+repo.getC_RepoMuro_ID();
		try 
		{
			PreparedStatement pstmtt = DB.prepareStatement (sqltiendas, get_TrxName());
			ResultSet rst = pstmtt.executeQuery ();
			
			while(rst.next())
			{
				X_C_RepoMuroTienda tienda = new X_C_RepoMuroTienda (getCtx() , rst.getInt("C_RepoMuroTienda_ID"),get_TrxName());
			
				log.config("Tienda=" + tienda.getTienda());
				String sqlp = "Select * from C_RepoMuroProduct where C_RepoMuroTienda_ID="+tienda.getC_RepoMuroTienda_ID();
				try 
				{
					PreparedStatement pstmtp = DB.prepareStatement (sqlp, get_TrxName());
					ResultSet rsp = pstmtp.executeQuery ();
					//crear notas
					int lov= 10;
					MOrder order=null;
					int salto = 0;
					int error = 0;
					while(rsp.next())
					{
						salto = 0;
						X_C_RepoMuroProduct prod =  new X_C_RepoMuroProduct(getCtx() , rsp.getInt("C_RepoMuroProduct_ID"),get_TrxName());
						log.config("Producto=" + prod.getM_Product());
						
						if (lov==10)
						{
							order = new MOrder (getCtx() , 0,get_TrxName());
							order.setC_BPartner_ID(1001237);
							order.setC_BPartner_Location_ID(tienda.getC_BPartner_Location_ID());
							order.setBill_BPartner_ID(1001237);
							order.setBill_Location_ID(tienda.getC_BPartner_Location_ID());
							order.setM_Warehouse_ID(1000001);
							order.setM_PriceList_ID(1000040);
							//order.set_CustomColumn("Firma2", true);
							//order.set_CustomColumn("Firma3", true);
							order.setC_DocTypeTarget_ID(1000030);	
							order.setIsSOTrx(true);
							order.setDeliveryRule("O");
							MBPartnerLocation bpl = new MBPartnerLocation (getCtx() , rst.getInt("C_Bpartner_Location_ID"),get_TrxName());
							order.setAD_User_ID(bpl.get_ValueAsInt("SalesRep_ID")  );
							order.setSalesRep_ID(bpl.get_ValueAsInt("SalesRep_ID") );
							MBPartner bp = new MBPartner (getCtx() , 1001237,get_TrxName());
							order.setC_PaymentTerm_ID(bp.getC_PaymentTerm_ID());
							order.setDateOrdered( rst.getTimestamp("Created")  );
							order.setDateAcct(  rst.getTimestamp("Created"));
							order.setInvoiceRule("D");
							
							//Faltan firmas horas de firma y quien firmo forma de compra medio compra							//
							order.set_CustomColumn("FIRMA2", "Y");
							order.set_CustomColumn("FIRMA3", "Y");
							order.set_CustomColumn("FIRMACOM",  rst.getTimestamp("Created"));
							order.set_CustomColumn("FIRMAFIN",  rst.getTimestamp("Created"));
							order.set_CustomColumn("USERFIRMCOM", 1003726); //cchihuaicura
							order.set_CustomColumn("USERFIRMFIN", 1003726); //cchihuaicura
							
							order.set_CustomColumn("VentaInvierno", "N");
							order.save();
							log.config("Orden=" + order.getDocumentNo());
						}
						
						int cantidad =  prod.getPEDIDO();
						if (lov<=170)
						{
							
							
							log.config("Creando linea = " + lov);
							MOrderLine oline = new MOrderLine (order);
							oline.setLine(lov);
							MProductPrice pp = new MProductPrice (getCtx() ,1000039, prod.getM_Product_ID(),get_TrxName());
							oline.setM_Product_ID(prod.getM_Product_ID());
							oline.setPriceEntered(pp.getPriceStd());
							oline.setPriceActual(pp.getPriceStd());
							oline.setPriceList(pp.getPriceStd());
							
						
							oline.setPrice(pp.getPriceStd());
							
							lov=lov+10;
							//existe pre reserva?
							String sqlqr =	"select count(1) encontrado "+
									" from m_requisition r " +
									" where r.docstatus='CO' and r.c_doctype_ID=1000570 "+
									" and r.c_bpartner_ID="+1001237
									+ " and (r.OVERWRITEREQUISITION='Y' or r.c_Bpartner_location_ID="+ rst.getInt("C_Bpartner_Location_ID")+ " )"+ 
									" and r.M_RequisitionRef_ID in " +
									" (select r2.m_requisition_ID "+
									" from m_requisition r2 "+
									" inner join m_requisitionline rl on (r2.m_requisition_ID=rl.m_Requisition_ID) "+ 
									" where r2.docstatus='CO' and  rl.qtyreserved>0  and rl.m_product_ID="+rsp.getInt("M_Product_ID")+
									 " and (rl.qty-rl.qtyused)>0 and r2.c_doctype_ID=1000569 and rl.liberada='N' )";
							log.config("Sql de pre-reserva " + lov);
							try
							{
								PreparedStatement pstmtre = DB.prepareStatement (sqlqr, get_TrxName());
								ResultSet rsre = pstmtre.executeQuery ();
								
								if(rsre.next())
								{
									if(rsre.getInt("encontrado")>0)
									{
										log.config("Existe Pre-Reserva Fisica ");
										String sqlqrq =	" select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "+
												" from m_Requisitionline rl "+
												" inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "+
												" where r.docstatus='CO'  "+
												" and r.c_doctype_id=1000569 "+
												" and rl.liberada='N' "+
												" and rl.qtyreserved>0 "+
												" and rl.m_product_ID ="+rsp.getInt("M_Product_ID") +
												" and exists  "+
														" (select * "+ 
														" from m_requisition r2 "+
														" where R2.M_REQUISITIONREF_ID=r.m_requisition_ID "+
														"  and r2.c_doctype_ID=1000570 "+
														" and r2.docstatus='CO' "+
														" and (r2.OVERWRITEREQUISITION='Y' or r2.c_Bpartner_location_ID="+ rst.getInt("C_Bpartner_Location_ID")+ " )"+ 
														" and r2.c_bpartner_ID="+1001237+ " ) ";
									
									
										try
										{
											PreparedStatement pstmtreq = DB.prepareStatement (sqlqrq, get_TrxName());
											ResultSet rsreq = pstmtreq.executeQuery ();
											
											if(rsreq.next())
											{
												if (cantidad <=rsreq.getInt("qtyreserved"))
												{
													oline.setQty(new BigDecimal  (prod.getPEDIDO()));
													oline.setQtyEntered(new BigDecimal  (prod.getPEDIDO()));
													oline.set_CustomColumn("Demand", new BigDecimal  (prod.getPEDIDO()));
													oline.set_ValueOfColumn("M_RequisitionLine_ID", rsreq.getInt("m_requisitionline_ID"));
													oline.save();
													prod.setC_Order_ID(order.getC_Order_ID());
													prod.setC_OrderLine_ID(oline.getC_OrderLine_ID());
													prod.setM_RequisitionLine_ID(rsreq.getInt("m_requisitionline_ID"));
													prod.save();
													salto=1;
													log.config("Salto = " + salto);
													
												}
												else
												{
													oline.setQty(new BigDecimal  (rsreq.getInt("qtyreserved")));
													oline.setQtyEntered(new BigDecimal  (rsreq.getInt("qtyreserved")));
													oline.set_CustomColumn("Demand", new BigDecimal  (rsreq.getInt("qtyreserved")));
													cantidad=cantidad-rsreq.getInt("qtyreserved");
													oline.set_ValueOfColumn("M_RequisitionLine_ID", rsreq.getInt("m_requisitionline_ID"));
													oline.save();
													prod.setC_Order_ID(order.getC_Order_ID());
													prod.setC_OrderLine_ID(oline.getC_OrderLine_ID());
													prod.setM_RequisitionLine_ID(rsreq.getInt("m_requisitionline_ID"));
													prod.save();
													salto=2;
													log.config("Salto = " + salto);
												}
													
											}
											
											
											pstmtreq.close();
											rsreq.close();
											DB.close(rsreq, pstmtreq);	
										}
										catch(Exception e)
										{
											
											log.log(Level.SEVERE, e.getMessage(), e);
										}
									
									}
								}
								
								pstmtre.close();
								rsre.close();
								DB.close(rsre, pstmtre);
									
							}
							catch(Exception e)
							{
								
								log.log(Level.SEVERE, e.getMessage(), e);
							}
							//existe pre reserva? 
							
							//existe reserva fisica??
							
							if(salto == 0 || salto==2 )
							{
								String sqlrfn=" select count(1) encontrado "+ 
										 " from m_requisition r  "+
												" inner join m_requisitionline rl on (r.m_requisition_ID=rl.m_Requisition_ID) "+ 
												" where r.docstatus='CO' and r.c_doctype_ID=1000111 "+
												" and r.c_bpartner_ID="+1001237+
												" and (r.c_bpartner_location_ID="+tienda.getC_BPartner_Location_ID()
												+ "or R.OVERWRITEREQUISITION='Y') "+
												" and  "+
												" rl.m_product_ID="+prod.getM_Product_ID()+
												" and (rl.qty-rl.qtyused)>0  "+
												" and  rl.LIBERADA='N' " ;
							
								try
								{
									PreparedStatement pstmtrfn = DB.prepareStatement (sqlrfn, get_TrxName());
									ResultSet rsrfn = pstmtrfn.executeQuery ();
									if(rsrfn.next())
									{
										//si existe rfisica,
										if(rsrfn.getInt("encontrado")>0)
										{
											log.config("Existe Reserva Fisica");
											String sqlrn =	" select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "+
													" from m_Requisitionline rl "+
													" inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "+
													" where r.docstatus='CO'  "+
													" and r.c_doctype_id=1000111 "+
													" and rl.m_product_ID ="+prod.getM_Product_ID()+
													" and r.c_bpartner_ID="+1001237+
													" and (r.c_bpartner_location_ID="+tienda.getC_BPartner_Location_ID()+
													 " or R.OVERWRITEREQUISITION='Y') "+
													" and (rl.qty-rl.qtyused)>0"+
													 " and  rl.LIBERADA='N' " ;
											
											try
											{
												PreparedStatement pstmtrn = DB.prepareStatement (sqlrn, get_TrxName());
												ResultSet rsrn = pstmtrn.executeQuery ();
												if(rsrn.next())
												{//reserva fisica normal rebajar
													if (cantidad<=rsrn.getInt("qtyreserved"))
													{
														oline.setQty(new BigDecimal  (cantidad));
														oline.setQtyEntered(new BigDecimal  (cantidad));
														oline.set_CustomColumn("Demand", new BigDecimal  (cantidad));
														oline.set_ValueOfColumn("M_RequisitionLine_ID", rsrn.getInt("m_requisitionline_ID"));
														oline.save();
														prod.setC_Order_ID(order.getC_Order_ID());
														prod.setC_OrderLine_ID(oline.getC_OrderLine_ID());
														prod.setM_RequisitionLine_ID(rsrn.getInt("m_requisitionline_ID"));
														prod.save();
														salto=3;
														log.config("Salto = " + salto);
													}
													else
													{
														oline.setQty(new BigDecimal  (rsrn.getInt("qtyreserved")));
														oline.setQtyEntered(new BigDecimal  (rsrn.getInt("qtyreserved")));
														oline.set_CustomColumn("Demand", new BigDecimal  (rsrn.getInt("qtyreserved")));
														oline.set_ValueOfColumn("M_RequisitionLine_ID", rsrn.getInt("m_requisitionline_ID"));
														oline.save();
														prod.setC_Order_ID(order.getC_Order_ID());
														prod.setC_OrderLine_ID(oline.getC_OrderLine_ID());
														prod.setM_RequisitionLine_ID(rsrn.getInt("m_requisitionline_ID"));
														prod.save();
														salto=4;
														log.config("Salto = " + salto);
													}
												}
												pstmtrn.close();
												rsrn.close();
												DB.close(rsrn, pstmtrn);
											}
											catch(Exception e)
											{
												
												log.log(Level.SEVERE, e.getMessage(), e);
											}
											
										}
									}
									pstmtrfn.close();
									rsrfn.close();
									DB.close(rsrfn, pstmtrfn);
								}
								catch(Exception e)
								{
									
									log.log(Level.SEVERE, e.getMessage(), e);
								}
								//existe reserva fisica??
							
								//sacar del stock
								
								if(salto==0 || salto==4)
								{
									log.config("Si habia reserva no cubre el pedido, a verificar disponible");
									String sqlps = "Select qtyavailableofb(p.m_product_ID, 1000001  ) + qtyavailableofb(p.m_product_ID, 1000010  ) as Disponible, p.ProductType,  " +
											"qtyavailableofb(p.m_product_ID,1000024)+qtyavailableofb(p.m_product_ID,1000025) OtroDisponible"+
										   " from M_product p where  p.m_product_ID="+prod.getM_Product_ID();
									try
									{
										

										PreparedStatement pstmtps = DB.prepareStatement (sqlps, get_TrxName());
										ResultSet rsps = pstmtps.executeQuery ();
										
										if(rsps.next())
										{
											if(rsps.getString("ProductType").equals("I"))
											{
												if (cantidad<=rsps.getInt("Disponible"))
												{
													log.config("Cantidad = " + cantidad + " - Disponible = "+ rsps.getInt("Disponible"));
													oline.setQty(new BigDecimal  (cantidad));
													oline.setQtyEntered(new BigDecimal  (cantidad));
													oline.set_CustomColumn("Demand", new BigDecimal  (cantidad));
													oline.save();
													prod.setC_Order_ID(order.getC_Order_ID());
													prod.setC_OrderLine_ID(oline.getC_OrderLine_ID());
													prod.save();
													salto=5;
													log.config("Salto = " + salto);
												}
												else
												{
													if (rsps.getInt("Disponible")>0)
													{
														
													
													oline.setQty(new BigDecimal  (rsps.getInt("Disponible")));
													oline.setQtyEntered(new BigDecimal  (rsps.getInt("Disponible")));
													oline.set_CustomColumn("Demand", new BigDecimal  (rsps.getInt("Disponible")));
													oline.save();
													prod.setC_Order_ID(order.getC_Order_ID());
													prod.setC_OrderLine_ID(oline.getC_OrderLine_ID());
													prod.save();
													salto=6;
													error++;
													log.config("Sin diponible suficiente, Salto = " + salto);
													}
													else
													{
														oline.setQty(new BigDecimal  (0));
														oline.setQtyEntered(new BigDecimal  (0));
														oline.set_CustomColumn("Demand", new BigDecimal  (0));
														oline.set_CustomColumn("NOTPRINT", true);
														oline.save();
														prod.setC_Order_ID(order.getC_Order_ID());
														prod.setC_OrderLine_ID(oline.getC_OrderLine_ID());
														prod.save();
														salto=7;	
														error++;
														log.config("sin disponible - Salto = " + salto);
													}
														
												}
											}
											
										}
										pstmtps.close();
										rsps.close();
										DB.close(rsps, pstmtps);
									}
									catch(Exception e)
									{
										
										log.log(Level.SEVERE, e.getMessage(), e);
									}
								}
								
								//sacar del stock
								
							}
							
							
							
							
						}
						if (lov==170)
						{
							if(salto==5 || salto==1)
							{
								//Se completa la orden
								if(error==0)
								{
									order.processIt(X_C_Order.DOCACTION_Complete);
									order.save();
	   							    order.setDocAction("CO");
									order.processIt ("CO");
									order.save();
									log.config("Completamos orden de llegar al limite de lineas= " + salto);
								}
									
								
							}
							lov=10;
						}
							
						
						
						
						
					prod.setProcessed(true);	
					prod.save();
					}//while productos
					if (lov<170 )
					{
						if(salto==5 || salto==1)
						{
							if(error==0)
							{
								order.processIt(X_C_Order.DOCACTION_Complete);
								order.save();
								order.setDocAction("CO");
								order.processIt ("CO");
								order.save();
								log.config("Completamos orden de tener menos de 17 lineas, salto = " + salto);
							}
							
						}
					}
					pstmtp.close();
					rsp.close();
					DB.close(rsp, pstmtp);
					
				
				}
				catch(Exception e)
				{
					
					log.log(Level.SEVERE, e.getMessage(), e);
				}
				tienda.setProcessed(true);
				tienda.save();
			}//while tienda

		pstmtt.close();
		rst.close();
		DB.close(rst, pstmtt);
		}
		
		catch(Exception e)
		{
			
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		repo.setProcessed(true);
		repo.save();
		return "Procesado.";		
		
			
	}	//	doIt
	
}	//	OrderOpen
