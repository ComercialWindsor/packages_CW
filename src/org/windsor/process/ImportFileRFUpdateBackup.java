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
//import java.math.BigDecimal;
import java.util.logging.Level;

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
public class ImportFileRFUpdateBackup extends SvrProcess
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
	//	commitEx();
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
		    		int sumar=0;
		    		if(product_id>=1000000)
		    		{
		    			rq.setM_Product_ID(product_id);
		    			disponible= 	Integer.parseInt(DB.getSQLValueString(null, "Select qtyavailableofb("+product_id+",1000001)+qtyavailableofb("+product_id+",1000010) from m_product where m_product_ID="+product_id));
		    			rq.setQtyAvailable(disponible);
		    			
		    		}
		    		else
		    		{
		    			rq.setQtyAvailable(disponible);
		    			rq.setERROR("Producto no encontrado, el codigo no existe en el sistema o no esta activo");
		    			
		    		}
		    		
		    		if(cantidad<=0)
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
	    				//	commitEx();
	    					log.config("Actualizada:"+no);
		    				//rql.setM_Requisition_ID(p_requisition_id);
		    			//	rql.setM_Product_ID(product_id);
		    			//	BigDecimal su= new BigDecimal( sumar);
		    			//	rql.setQty(su);
		    			//	rql.set_ValueOfColumn("QtyReserved", new BigDecimal(sumar));
		    			//	rql.setLine(10);
		    			//	rql.save();
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
			    					//commitEx();
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
				    					//commitEx();
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
					    				//	commitEx();
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
		    		
		    		rq.set_ValueOfColumn("archivo", p_PathFile);
		    		rq.set_ValueOfColumn("M_Requisition_ID", p_requisition_id);
		    		rq.save();
		    	}
	    	}
	    	}
	    	linea=br.readLine();
	    	cantLine++;
		}
	 //   commitEx();
		br.close();
	    
		return "Procesado: Redcuerde Revisar Informe Actualiza Pre-Reserva";
	}	//	doIt


}	
