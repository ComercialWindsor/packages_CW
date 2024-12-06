/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                        *
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.math.BigDecimal;
import java.util.logging.Level;











import org.compiere.model.MRequisitionLine;
//import org.compiere.model.*;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.*;
import org.windsor.model.X_I_ReservaReq;

/**
 *	
 *	
 *  @author italo niñoles ininoles
 *  @version $Id: ProcessPaymentRequest.java,v 1.2 2011/06/12 00:51:01  $
 */
public class ImportFileRFUpdate extends SvrProcess
{
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	
	private String 			p_PathFile;
	private int p_requisition_id;
	
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("archivo"))
				p_PathFile = para[i].getParameter().toString();
			else if (name.equals("M_Requisition_ID"))
				p_requisition_id = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		String[] datosFStr = new String[4];
    	FileInputStream fis =new FileInputStream(p_PathFile);
	    InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
	    BufferedReader br = new BufferedReader(isr);
	    //se lee primera linea de archivo 
	    String linea=br.readLine();		   
	    //DB.executeUpdate("DELETE FROM PP_ForecastLineCENCO",get_TrxName());
	    int cantLine = 0;
	    int no=0;
    	String clientCheck = " and ad_client_ID=1000000";
    	StringBuffer sql = new StringBuffer ("delete from I_ReservaReq where ad_org_ID=1000000 ").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		//commitEx();
		log.config("Borradas:"+no);
	    //se recorre archivo
	    while(linea!=null)
	    {	
	    	
	    	log.config(linea);
	    	
	    	if(cantLine >= 0)
	    	{
		    	//se separan campos separados por;
		    	datosFStr = linea.split(";");
		    	//se valida que no hayan datos nulos
		    	if(datosFStr[0] != null || datosFStr[1] != null  )
		    	{
		    		String bloquear = "N";
		    		String codigo = datosFStr[0];
		    		int cantidad = Integer.parseInt(datosFStr[1].toString().trim().replace(".",""));
		    		if(datosFStr[2] != null)
		    		{
		    			if(datosFStr[2].equals("S"))
		    					bloquear="Y";
		    			else
		    				bloquear="N";
		    		}
		    			
		    		else
		    			bloquear="N";
		    		//	ola.setAD_Org_ID(Env.getAD_Org_ID(getCtx()));
		    		//ola.setNombreOla(Sacar0Izq(datosFStr[0]));
		    	
		    		X_I_ReservaReq rq= new X_I_ReservaReq(getCtx(), 0, get_TrxName());
		    		
		    		rq.setValue(codigo);
		    		rq.setQtyEntered(cantidad);
		    		rq.set_ValueOfColumn("archivo", p_PathFile);
		    		rq.set_ValueOfColumn("M_Requisition_ID", p_requisition_id);
		    		rq.set_ValueOfColumn("Bloquear", bloquear);
		    		rq.save();
		    		String sqlp= "Select coalesce(max(m_product_ID),0) from m_product where isactive='Y' and ad_client_ID=1000000 and upper(trim(value))='"+codigo.trim().toUpperCase()+"'";
		    		int product_id=Integer.parseInt(DB.getSQLValueString(null,sqlp)  ); 
		    		int disponible=0;
		    	//	int sumar=0;
		    		if(product_id>=1000000)
		    		{
		    			rq.setM_Product_ID(product_id);
		    			disponible= 	Integer.parseInt(DB.getSQLValueString(null, "Select "+
		    			//qtyavailableofb("+product_id+",1000001)+qtyavailableofb("+product_id+",1000010)
		    			" COALESCE ( "+
					      "         (SELECT SUM (s.qtyonhand) "+
					       "           FROM rv_storage s "+
					        "         WHERE     s.M_Product_ID = p.m_product_id "+
					         "              AND s.m_warehouse_id IN (1000001, 1000010,1000033) "+
					          "             AND s.isactive = 'Y'), "+
					           "    0) "+
					          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
					         "         FROM C_orderline ol2      "+
					          "             INNER JOIN C_Order o2  "+
					           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
					            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
					             "          AND o2.m_warehouse_id = 1000001 "+
					              "         AND o2.saldada <> 'Y' "+
					               "        AND o2.docstatus IN ('IP', 'CO') "+
					                "       AND o2.issotrx = 'Y' "+
					                 "      AND o2.c_doctypetarget_ID NOT IN "+
					                  "            (1000110, 1000048, 1000568)) "+
					            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
					             "     FROM M_Requisitionline rl     "+
					              "         INNER JOIN M_Requisition r  "+
					               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
					                " WHERE     rl.M_Product_ID = p.m_product_id  "+
					                 "      AND r.m_warehouse_id = 1000001  "+
					                  "     AND r.docstatus IN ('CO', 'CL')  "+
					                   "    AND r.issotrx = 'Y'))  "+
		    					" from m_product p where p.m_product_ID="+product_id));
		    			rq.setQtyAvailable(disponible);
		    			rq.save();
		    			log.config("disponible:"+disponible);
		    			int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(m_requisitionline_ID),0) from m_requisitionline where isactive='Y' and LIBERADA<>'Y' and "+
								" m_requisition_id="+p_requisition_id+" and m_product_ID="+product_id)); 
		    			log.config("RL_ID:"+rl_id);
		    			
		    			if(rl_id>0)
		    			{
		    				rq.set_CustomColumn("M_RequisitionLine_ID", rl_id);
		    				MRequisitionLine rl = new MRequisitionLine (getCtx(), rl_id, get_TrxName());
		    				rq.set_CustomColumn("Qty", rl.getQty());
		    				int usedr = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(sum(qtyused),0) from m_requisitionline where isactive='Y' and LIBERADA<>'Y' and "+
									" m_requisitionline_id="+rl_id)); 
		    				rq.set_CustomColumn("QtyUsed",new BigDecimal (usedr) );
		    				usedr = Integer.parseInt(DB.getSQLValueString(null, "select coalesce(sum(OL.QTYentered),0) "+
																				  " from c_orderline ol "+ 
																				  " inner join c_order o on (ol.c_order_ID=o.c_order_ID) "+
																				  " where o.docstatus not in ('VO') and ol.m_requisitionline_ID="+rl_id));
		    				rq.set_CustomColumn("QtyOrdered",new BigDecimal(usedr ));
		    				usedr = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(sum(qtyreserved),0) from m_requisitionline where isactive='Y' and LIBERADA<>'Y' and "+
									" m_requisitionline_id="+rl_id)); 
		    				rq.set_CustomColumn("QtyReserved",new BigDecimal(usedr) );
		    				rq.save();
		    			}
		    			
		    		}
		    		else
		    		{
		    			rq.setQtyAvailable(disponible);
		    			rq.setERROR("Producto no encontrado, el codigo no existe en el sistema o no esta activo");
		    			rq.save();
		    		}
		    		
		    		/*if(cantidad<=0)
		    		{
		    			sumar=cantidad;
		    		}
		    		else
		    		{
		    			if(cantidad>disponible)
		    				if(disponible>0)
		    					sumar=disponible;
		    				else
		    				{
		    					sumar=0;
		    					rq.setERROR("Producto sin disponible:"+disponible);
		    				}
		    					
		    			else
		    				sumar=cantidad;
		    		}
		    		if(disponible<0 && cantidad>0)
		    		{
    					sumar=0;
    					rq.setERROR("Producto sin disponible:"+disponible);
    				}
		    		if(disponible<0 && cantidad<0)
		    		{
    					sumar=cantidad;
    					rq.setERROR("Producto sin disponible:"+disponible);
    				}	
		    		
		    		log.config("Sumar:"+sumar);
		    		if (product_id>=1000000)
		    		{
		    			int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(m_requisitionline_ID),0) from m_requisitionline where isactive='Y' and LIBERADA<>'Y' and "+
								" m_requisition_id="+p_requisition_id+" and m_product_ID="+product_id)); 
		    			log.config("RL_ID:"+rl_id);
		    			log.config("RL_ID: Select coalesce(max(m_requisitionline_ID),0) from m_requisitionline where isactive='Y' and LIBERADA<>'Y' and "+
								" m_requisition_id="+p_requisition_id+" and m_product_ID="+product_id);
		    		//	MRequisitionLine rql = new MRequisitionLine (getCtx(), rl_id, get_TrxName());
		    			//if (disponible>0 )
		    			if(rl_id==0 && sumar>0)
		    			{
		    				if (disponible>0)
		    				{
		    					
		    				
		    				
		    				log.config("RL_ID:0 y sumar positivo");
		    				rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(      920,'N') from c_charge where c_charge_ID=1000010")); 
		    				int um_id  =Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(C_uom_ID),100) from m_product where "+
									" m_product_ID="+product_id)); 

		    				
		    				sql = new StringBuffer ("Insert into m_requisitionline (m_requisition_ID,m_requisitionline_id, line, m_product_ID, qty, qtyreserved, ad_Client_ID, ad_org_ID,"+
		    				" created, createdby, isactive, updated, updatedby,QTYUSED, Bloquear,C_UOM_ID)values("+p_requisition_id+","+rl_id+",10,"+product_id+","+sumar+","+sumar+",1000000,1000000,sysdate,100,'Y', sysdate, 100,0,'"+bloquear+"',"+um_id+")");
	    					no = DB.executeUpdate(sql.toString(), get_TrxName());
	    					commitEx();
	    					log.config("Actualizada:"+no);
		    		
		    				rq.setProcessed(true);
		    				rq.set_ValueOfColumn("Msg", "Producto agregado con la cantidad:"+sumar);
		    				}
		    				else
    						{ 
    							//agregar=reser;
    							rq.set_ValueOfColumn("Msg", "No Hay disponible para aumentar la reserva, Disponible:"+disponible);
    						}
		    				
		    			}
		    			else
		    			{
		    				log.config("RL_ID!=0 ");
		    				if (rl_id>0)
		    				{
		    					
		    				
		    				log.config("log reser:"+Integer.parseInt(DB.getSQLValueString(null, "Select coalesce((QtyReserved),0) from m_requisitionline where isactive='Y'  and "+
									" m_requisitionline_id="+rl_id+" and m_product_ID="+product_id)));
		    					int reser= Integer.parseInt(DB.getSQLValueString(null, "Select coalesce((QtyReserved),0) from m_requisitionline where isactive='Y'  and "+
									" m_requisitionline_id="+rl_id+" and m_product_ID="+product_id)); 
		    					int used=Integer.parseInt(DB.getSQLValueString(null, "Select coalesce((QtyUsed),0) from m_requisitionline where isactive='Y'  and "+
										" m_requisitionline_id="+rl_id+" and m_product_ID="+product_id)); 
	    						int qty=Integer.parseInt(DB.getSQLValueString(null, "Select coalesce((Qty),0) from m_requisitionline where isactive='Y'  and "+
										" m_requisitionline_id="+rl_id+" and m_product_ID="+product_id)); 
		    					if(sumar<0)//sacar
		    					{
		    						int aux=sumar* -1;
		    						
		    						if(reser-aux<0)
		    						{
		    							used=used+reser;
		    							reser=0;
		    							
		    						}
		    							
		    						else
		    						{
		    							used=used+aux;
		    							reser=reser-aux;
		    						}
		    							
		    						
		    						sql = new StringBuffer ("Update  M_RequisitionLine set QtyUsed="+used+" , bloquear = '"+bloquear+"', QtyReserved="+reser +" where m_requisitionline_ID="+rl_id).append (clientCheck);
			    					no = DB.executeUpdate(sql.toString(), get_TrxName());
			    					commitEx();
			    					log.config("Actualizada:"+no);
		    					
		    					}
		    					else//aumentar
		    					{
		    						
		    						if(sumar<=disponible)
		    						{
		    							qty=qty+sumar;
			    						reser=reser+sumar;
			    						sql = new StringBuffer ("Update  M_RequisitionLine set Qty="+qty+" , bloquear = '"+bloquear+"', QtyReserved="+reser +", Qtyused="+used +"where m_requisitionline_ID="+rl_id).append (clientCheck);
				    					no = DB.executeUpdate(sql.toString(), get_TrxName());
				    					commitEx();
				    					log.config("Actualizada:"+no);
				    					rq.set_ValueOfColumn("Msg", "La canrtidad se aumenta correctamente");
		    						}
		    						else
		    						{
		    							if(disponible>0)
			    						{ 
			    							reser=reser+disponible;	
			    							qty=qty+disponible;
			    							sql = new StringBuffer ("Update  M_RequisitionLine set Qty="+qty+" , bloquear = '"+bloquear+"', QtyReserved="+reser +", Qtyused="+used +"where m_requisitionline_ID="+rl_id).append (clientCheck);
					    					no = DB.executeUpdate(sql.toString(), get_TrxName());
					    					commitEx();
					    					log.config("Actualizada:"+no);
			    							rq.set_ValueOfColumn("Msg", "La canrtidad se ajusta al disponible en bodega, pues se solicita mas de lo que hay");
			    						}
			    						
			    						else
			    						{ 
			    							//agregar=reser;
			    							rq.set_ValueOfColumn("Msg", "No Hay disponible para aumentar la reserva");
			    						}
		    						}
		    						
		    					}
		    				}
		    			
		    				rq.setProcessed(true);
		    			}
		    			
		    		}*/
		    		rq.set_ValueOfColumn("archivo", p_PathFile);
		    		rq.set_ValueOfColumn("M_Requisition_ID", p_requisition_id);
		    		rq.save();
		    	}
	    	}
	    	linea=br.readLine();
	    	cantLine++;
		}
	//    commitEx();
		br.close();
	    
		
		//recorrer lo leido
	String	sqlr = "Select * from I_ReservaReq where m_product_id is not null and m_requisitionline_ID is not null";
		
	try
	{
		

		PreparedStatement pstmt = DB.prepareStatement (sqlr, get_TrxName());
		ResultSet rs = pstmt.executeQuery ();
		while (rs.next())
		{
			X_I_ReservaReq rq= new X_I_ReservaReq(getCtx(), rs.getInt("I_ReservaReq_ID"), get_TrxName());
			MRequisitionLine rl = new MRequisitionLine (getCtx(), rs.getInt("M_RequisitionLine_ID"), get_TrxName());
			//Caso Disponible Negativo
			
			if(rq.getQtyAvailable()<=0)
			{
				log.config("Disponible Negativo:"+rq.getQtyAvailable());
				if(rq.getQtyEntered()<0) //Caso solicitado Negativo
				{
					log.config("Disponible Negativo y Pedido Negativo:"+rq.getQtyEntered());
					if(rq.getQtyAvailable()<rq.getQtyEntered())//Caso disponible negativo no cubre solicitado
					{
						
						int reserva= rs.getInt("Qty")-rs.getInt("QtyOrdered");
						
						if(reserva>0)
						{
							int raad=reserva+rq.getQtyAvailable();
							if (raad>=0)
							{
								int qtyint= raad+rs.getInt("QtyOrdered");
								BigDecimal qty = new BigDecimal (qtyint );
								rl.setQty(qty);
								log.config("Disponible:"+rq.getQtyAvailable());
								log.config("Rebaja:"+rq.getQtyEntered());
								log.config("Reserva Actual:"+reserva);
							//	log.config("rebaja:"+rebaja);
								log.config("Cantidad Usada:"+rs.getInt("QtyOrdered"));
								log.config("New Cantidad:"+qty);
								log.config("New reserva:"+raad);

								rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
								rl.set_ValueOfColumn("QtyReserved", new BigDecimal( raad));
								rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
								rl.save();
								rq.set_ValueOfColumn("Msg","Se Ajusta Reserva para regularizar disponible");
								rq.setProcessed(true);
								rq.save();
							}
							else 
							{
								BigDecimal qty = new BigDecimal (rs.getInt("QtyOrdered") );
								rl.setQty(qty);
								log.config("Disponible:"+rq.getQtyAvailable());
								log.config("Rebaja:"+rq.getQtyEntered());
								log.config("Reserva Actual:"+reserva);
							//	log.config("rebaja:"+rebaja);
								log.config("Cantidad Usada:"+rs.getInt("QtyOrdered"));
								log.config("New Cantidad:"+qty);
								log.config("New reserva:"+0);

								rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
								rl.set_ValueOfColumn("QtyReserved", new BigDecimal(0));
								rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
								rl.save();
								rq.set_ValueOfColumn("Msg","Se Ajusta Reserva a 0 para regularizar disponible");
								rq.setProcessed(true);
								rq.save();
							}
						}
						else //reserva<=0
						{
							BigDecimal qty = new BigDecimal (rs.getInt("QtyOrdered") );
							log.config("Disponible:"+rq.getQtyAvailable());
							log.config("Rebaja:"+rq.getQtyEntered());
							log.config("Reserva Actual:"+reserva);
						//	log.config("rebaja:"+rebaja);
							log.config("Cantidad Usada:"+rs.getInt("QtyOrdered"));
							log.config("New Cantidad:"+qty);
							log.config("New reserva:"+0);

							
							rl.setQty(qty);
							rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
							rl.set_ValueOfColumn("QtyReserved",new BigDecimal( 0));
							rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
							rl.save();
							rq.set_ValueOfColumn("Msg","Se Ajustan cantidades para regularizar disponible");
							rq.setProcessed(true);
							rq.save();
						}// end reserva<=0
						
						
						
						
					}//End Diponible menor a rebaja
					if(rq.getQtyAvailable()>=rq.getQtyEntered())//Caso disponible negativo cubre solicitado
					{
						int reserva= rs.getInt("Qty")-rs.getInt("QtyOrdered");
						
						if(reserva>0)
						{
							if (reserva+rq.getQtyEntered()>0)
							{
								int rebaja=rq.getQtyEntered();
								int raddp=reserva+rebaja;
								int qtyint=raddp+rs.getInt("QtyOrdered");
								BigDecimal qty = new BigDecimal (qtyint) ;
								
								log.config("Disponible:"+rq.getQtyAvailable());
								log.config("Rebaja:"+rq.getQtyEntered());
								log.config("Reserva Actual:"+reserva);
								log.config("rebaja:"+rebaja);
								log.config("Cantidad Usada:"+rs.getInt("QtyOrdered"));
								log.config("New Cantidad:"+qty);
								log.config("New reserva:"+raddp);
								rl.setQty(qty);
								rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
								rl.set_ValueOfColumn("QtyReserved", new BigDecimal(raddp));
								rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
								rl.save();
								rq.set_ValueOfColumn("Msg", "Cantidad Se ajusta segun solicitado");
								rq.setProcessed(true);
								rq.save();
							}
							else
							{
								BigDecimal qty = new BigDecimal (rs.getInt("QtyOrdered")) ;
								rl.setQty(qty);
								rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
								rl.set_ValueOfColumn("QtyReserved", new BigDecimal(0));
								rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
								rl.save();
								rq.set_ValueOfColumn("Msg", "Cantidad Se ajusta a 0 para regularizar disponible");
								rq.setProcessed(true);
								rq.save();
							}
						}
						
						
					}//End Caso disponible negativo cubre solicitado
					
				}//End Caso solicitado Negativo
				int suma=rq.getQtyEntered() + rq.getQtyAvailable();
				log.info("Suma"+suma);
				if(rq.getQtyEntered()>=0) //Caso Solicitado Positivo
				{
					
					if(rq.getQtyEntered() + rq.getQtyAvailable()>=0) //Nueva Reserva amenta el negativo
					{
						log.config("Disponible Negativo y Pedido Positivo:"+rq.getQtyEntered());
						log.config("Reserva:"+rq.get_ValueAsInt("QtyReserved"));
						//int reserva= Math.abs(rq.getQtyEntered()+rq.getQtyAvailable());
						int qtyint= rs.getInt("QtyOrdered");
						BigDecimal qty = new BigDecimal (qtyint) ;
						log.config("New Qty:"+ qty );
						rl.setQty(qty);
						rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
						rl.set_ValueOfColumn("QtyReserved",new BigDecimal( 0));
						rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
						rl.save();
						rq.set_ValueOfColumn("Msg", "Se Ajusta Reserva para rebajar disponible negativo");
						
						rq.setProcessed(true);
						rq.save();
					}//End Nueva Reserva deja en positivo?
					else //Nueva Reserva rebaja negativo
					{
						int reserva= rq.get_ValueAsInt("QtyReserved");
						log.config("Usado Real:" + rs.getInt("QtyOrdered"));
						log.config("Disponible Negativo y Pedido Positivo:"+rq.getQtyEntered());
						log.config("Reserva:"+rq.get_ValueAsInt("QtyReserved"));
						if(reserva>0)
						{
							int pedido= rq.getQtyEntered() ;
							if(reserva>pedido)
							
								reserva=reserva-pedido;
							
							else
								reserva=0;
						}
						else
							reserva=0;
						
						int disponible= rq.getQtyAvailable();
						if(reserva+disponible>0)
						{
							reserva=reserva+disponible;
						}
						else
							reserva=0;
						int qtyint=reserva + rs.getInt("QtyOrdered");
						BigDecimal qty = new BigDecimal (qtyint) ;
						rl.setQty(qty);
						rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
						rl.set_ValueOfColumn("QtyReserved",new BigDecimal( reserva));
						rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
						rl.save();
						rq.set_ValueOfColumn("Msg", "Se Ajusta Reserva");
						
						rq.setProcessed(true);
						rq.save();
					}//End Nueva Reserva deja en negativo?
				}//End Caso Solicitado Positivo
				else
				{
					log.info("Else recien creado");
					int reserva= rq.get_ValueAsInt("QtyReserved");
					log.config("Usado Real:" + rs.getInt("QtyOrdered"));
					log.config("Disponible Negativo y Pedido Positivo:"+rq.getQtyEntered());
					log.config("Reserva:"+rq.get_ValueAsInt("QtyReserved"));
					if(reserva>0)
					{
						int pedido= rq.getQtyEntered() ;
						if(reserva>pedido)
							if(pedido<0)
								reserva=reserva+ pedido;
						
						else
							reserva=0;
					}
					else
						reserva=0;
					
					int disponible= rq.getQtyAvailable();
					if(reserva+disponible>0)
					{
						reserva=reserva+disponible;
					}
					else
						reserva=0;
					int qtyint=reserva + rs.getInt("QtyOrdered");
					BigDecimal qty = new BigDecimal (qtyint) ;
					rl.setQty(qty);
					rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
					rl.set_ValueOfColumn("QtyReserved",new BigDecimal( reserva));
					rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
					rl.save();
					rq.set_ValueOfColumn("Msg", "Se Ajusta Reserva");
					
					rq.setProcessed(true);
					rq.save();
					
					
					
				}
			}//End Caso Disponible Negativo
			else //Disponible Positivo
			{
			 if(rq.getQtyAvailable()>0)
			 {
				if(rq.getQtyEntered()<0) //Caso solicitado Negativo
				{
					
					if(rq.getQtyAvailable()>=rq.getQtyEntered())//Disponible Cubre lo pedido
					{
						int qtyr = rs.getInt("Qty")-rs.getInt("QtyOrdered");
						rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
						int raddp=qtyr + rq.getQtyEntered();
						int qtyint= raddp+rs.getInt("QtyOrdered") ;
						BigDecimal qty = new BigDecimal ( qtyint);
						
						if( raddp>0)//nuevo reservado positivo
						{
							rl.set_ValueOfColumn("QtyReserved", new BigDecimal(raddp));
							rl.setQty(qty);
							rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
							rl.save();
							rq.set_ValueOfColumn("Msg", "Se Ajusta Reserva segun solicitado ");
							rq.setProcessed(true);
							rq.save();
						}
							
						else //nuevo reservado negativo
						{
							rl.set_ValueOfColumn("QtyReserved",new BigDecimal( 0));
							qty=new BigDecimal( rs.getInt("QtyOrdered"));
							rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
							rl.setQty(qty);
							rl.save();
							rq.set_ValueOfColumn("Msg", "Se Ajusta Reserva a 0 ");
							rq.setProcessed(true);
							rq.save();
						}
							
						
						
					}//END Disponible Cubre lo pedido
				
				}//END Caso solicitado Negativo
				if(rq.getQtyEntered()>=0) //Caso solicitado Positivo
				{
					int qtyr = rs.getInt("Qty")-rs.getInt("QtyOrdered");
					log.config("Qty Inicial Real:"+rs.getInt("Qty"));
					log.config("Qty Used:"+rs.getInt("QtyUsed"));
					log.config("Qty RFI:"+rs.getInt("QtyReserved"));
					log.config("Usado Real:"+rs.getInt("QtyOrdered"));
					log.config("Reservado Real:"+qtyr);
					log.config("Pedidos:"+rq.getQtyEntered());
					log.config("Disponible:"+rq.getQtyAvailable());
					log.config("RL_ID="+rl.getM_RequisitionLine_ID());
					int raddp=qtyr+rq.getQtyEntered();
					log.config("Reservado Actual + Pedido="+raddp);
					int daddr=qtyr+rq.getQtyAvailable()  ; 
					
					log.config("Disponible + Reservado ="+daddr);
					log.config("if pedido mayor que disponible="+(qtyr+rq.getQtyEntered()>rq.getQtyAvailable()+qtyr));
					
					if((rs.getInt("Qty")==rs.getInt("QtyUsed") && rs.getInt("QtyReserved")==0) || (rs.getInt("QtyReserved")==0))
					{
						qtyr=0;
						raddp=rq.getQtyEntered();
						daddr=rq.getQtyAvailable()  ; 
						log.config("se repara reserva");
					}
					
					
					if(raddp>daddr) //Aumento es mayor al disponible
					{
						int qtyint=daddr+ rs.getInt("QtyOrdered");
						BigDecimal qty=new BigDecimal( qtyint );
						log.config("New Qty:"+qty);
						rl.setQty(qty);
						log.config("New QtyUsed:"+rs.getInt("QtyOrdered"));
						rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
						log.config("New Reserved:"+daddr);
						rl.set_ValueOfColumn("QtyReserved", new BigDecimal(daddr));
						rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
						rl.save();
						rq.set_ValueOfColumn("Msg", "Se aumenta reserva segun disponible quedando en:"+daddr);
						rq.setProcessed(true);
						rq.save();
						
					}//Aumento es mayor al disponible
					if(raddp<=daddr)//Aumento es menor al disponible
					{
						BigDecimal qty=new BigDecimal( rs.getInt("QtyOrdered") +raddp );
						log.config("New Qty:"+qty);
						rl.setQty(qty);
						rl.set_ValueOfColumn("QtyUsed",new BigDecimal(rs.getInt("QtyOrdered")));
						log.config("New Reserva:"+raddp);
						rl.set_ValueOfColumn("QtyReserved",new BigDecimal( raddp));
						rl.set_ValueOfColumn("Bloquear", rs.getString("bloquear"));
						rl.save();
						rq.set_ValueOfColumn("Msg", "Se aumenta reserva segun Solicitado Quedando en :" +raddp);
						rq.setProcessed(true);
						rq.save();
					}
				}
			 }//if disponible>0	
			 else
			 {
				 rq.set_ValueOfColumn("Msg", "Disponible actual= 0, se salta producto" );
					rq.setProcessed(true);
					rq.save(); 
			 }
			}//End Disponible Positivo
		}//While existe en la reserva
		rs.close();
		pstmt.close();
	}
	catch(Exception e)
	{
		
		log.log(Level.SEVERE, e.getMessage(), e);
	}
	
	
	sqlr = "Select * from I_ReservaReq where m_product_id is not null and m_requisitionline_ID is null";
	//recorrer los que no existen en reserva
	try
	{
		

		PreparedStatement pstmt = DB.prepareStatement (sqlr, get_TrxName());
		ResultSet rs = pstmt.executeQuery ();
		while (rs.next())
		{
			X_I_ReservaReq rq= new X_I_ReservaReq(getCtx(), rs.getInt("I_ReservaReq_ID"), get_TrxName());
			log.config("Disponible:"+rq.getQtyAvailable());
			log.config("Codigo:"+rq.getValue());
			if(rq.getQtyAvailable()>0) //Disponible positivo, aolo ai exiate
			{
				log.config("Pedido:"+rq.getQtyEntered());
				if(rq.getQtyEntered()<=rq.getQtyAvailable())//disponible cuble lo solicitado
				{
				//	int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(      920,'N') from c_charge where c_charge_ID=1000010")); 
    				int um_id  =Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(C_uom_ID),100) from m_product where "+
							" m_product_ID="+rq.getM_Product_ID())); 

    				//log.info("ID:"+rl_id);
    				MRequisitionLine mrl = new MRequisitionLine(getCtx() ,0,get_TrxName());
    				mrl.setM_Requisition_ID(p_requisition_id);
    				mrl.setM_Product_ID(rq.getM_Product_ID());
    				BigDecimal qty = new BigDecimal(rq.getQtyEntered());
    				mrl.setQty(  qty);
    				mrl.set_CustomColumn("QtyReserved", qty);
    				//mrl.set_CustomColumn("QtyReserved", rq.getQtyEntered());
    				mrl.set_CustomColumn("QtyUsed", new BigDecimal(0));
    				mrl.setLine(10);
    				mrl.set_CustomColumn("Bloquear", rs.getString("bloquear"));
    				mrl.setC_UOM_ID(um_id );
    				
    				mrl.save();
    			/*	Timestamp timeStampDate=null;
    				try {
    				      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    				      Date date = dateFormat.parse("24/08/2021");
    				      timeStampDate = new Timestamp(date.getTime());
    				      System.out.println(timeStampDate);
    				    } catch (ParseException e) {
    				      System.out.println(e);
    				    }
    				String sql2 = "Insert into m_requisitionline (m_requisition_ID,m_requisitionline_id, "+
    				"line, m_product_ID, qty, qtyreserved, ad_Client_ID, ad_org_ID,"+
    				" created, createdby, isactive, updated, updatedby,QTYUSED, Bloquear,C_UOM_ID)values("+p_requisition_id+","+rl_id+
    				",10,"+rq.getM_Product_ID()+","+rq.getQtyEntered()+","+rq.getQtyEntered()+",1000000,1000000,"
    				+timeStampDate+",100,'Y',"+ timeStampDate+", 100,0,'"+rs.getString("bloquear")+"',"+um_id+")";
					log.info(sql2);*/
				
				//	PreparedStatement pstmtc = DB.prepareStatement (sql2, get_TrxName());
				////	pstmtc.setString(2, "2");
				//	pstmtc.setString(3, "3");
				//	pstmtc.setString(4, "4");
				//	pstmtc.setString(5, "1");
				//	pstmtc.setString(6, "2");
				//	pstmtc.setString(7, "3");
				//	pstmtc.setString(8, "4");
				//	ResultSet rsc = pstmtc.executeQuery ();
					
					
				//	no = DB.executeUpdate(sql2, get_TrxName());
				//	commitEx();
					log.config("Actualizado");
    		
    				rq.setProcessed(true);
    				rq.set_ValueOfColumn("Msg", "Producto agregado con la cantidad:"+rq.getQtyEntered());
    				rq.save();
				}//end disponible cuble lo solicitado
				else //disponible no cuble lo solicitad
				{
				//	int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(      920,'N') from c_charge where c_charge_ID=1000010")); 
    				int um_id  =Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(C_uom_ID),100) from m_product where "+
							" m_product_ID="+rq.getM_Product_ID())); 
    		MRequisitionLine mrl = new MRequisitionLine(getCtx() ,0,get_TrxName());
    				mrl.setM_Requisition_ID(p_requisition_id);
    				mrl.setM_Product_ID(rq.getM_Product_ID());
    				BigDecimal qty = new BigDecimal(rq.getQtyAvailable());
    				mrl.setQty(  qty);
    				mrl.set_CustomColumn("QtyReserved", qty);
    				//mrl.set_CustomColumn("QtyReserved", rq.getQtyEntered());
    				mrl.set_CustomColumn("QtyUsed", new BigDecimal(0));
    				mrl.setLine(10);
    				mrl.set_CustomColumn("Bloquear", rs.getString("bloquear"));
    				mrl.setC_UOM_ID(um_id );
    				mrl.save();
    				
    			/*	sql = new StringBuffer ("Insert into m_requisitionline (m_requisition_ID,m_requisitionline_id, line, m_product_ID, qty, qtyreserved, ad_Client_ID, ad_org_ID,"+
    				" created, createdby, isactive, updated, updatedby,QTYUSED, Bloquear,C_UOM_ID)values("+p_requisition_id+","+rl_id+",10,"+rq.getM_Product_ID()+","+rq.getQtyAvailable()+","+rq.getQtyAvailable()+",1000000,1000000,"+rq.getCreated()+",100,'Y', "+rq.getCreated()+", 100,0,'"+rs.getString("bloquear")+"',"+um_id+")");
					no = DB.executeUpdate(sql.toString(), get_TrxName());*/
				//	commitEx();
					log.config("Actualizada:");
    		
    				rq.setProcessed(true);
    				rq.set_ValueOfColumn("Msg", "Producto agregado con el disponible restante:"+rq.getQtyAvailable());
    				rq.save();
				}//End disponible no cuble lo solicitad
			}//solo si hay disponible
		}//End While no existen en reserva
		rs.close();
		pstmt.close();
	}
	catch(Exception e)
	{
		
		log.log(Level.SEVERE, e.getMessage(), e);
	}
	
		return "Procesado: Redcuerde Revisar Informe Actualiza Pre-Reserva";
	}	//	doIt


}	
