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

//import java.math.BigDecimal;
import java.util.logging.Level;

import org.compiere.model.*;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.*;
//import org.joda.time.LocalDateTime;
//import org.joda.time.format.DateTimeFormatter;
//import org.windsor.model.X_I_ReservaReq;
import org.windsor.model.X_C_UploadAllocation;

/**
 *	
 *	
 *  @author Maximiliano Rojas SinnaIT
 *  @version $Id: ProcessPaymentRequest.java,v 1.2 2021/04/07 00:51:01  $
 */
public class UploadAllocation extends SvrProcess
{
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	
	private String 			p_PathFile;
	//private int p_requisition_id;
	
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
				;//p_requisition_id = para[i].getParameterAsInt();
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
		String[] datosFStr = new String[5];
    	FileInputStream fis =new FileInputStream(p_PathFile);
	    InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
	    BufferedReader br = new BufferedReader(isr);
	 //   int cuenta =0;
	    //se lee primera linea de archivo 
	    String linea=br.readLine();		   
	    //DB.executeUpdate("DELETE FROM PP_ForecastLineCENCO",get_TrxName());
	    int cantLine = 0;
	    int no=0;
    	//String clientCheck = " and ad_client_ID=1000000";
    	StringBuffer sql =new StringBuffer ("select * from c_charge");// new StringBuffer ("delete from T_OrderVoid where ad_org_ID=1000000 ").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
	//	commitEx();
		log.config("Borradas:"+no);
		no=0;
		int errorbp=0;
		int errorpay=0;
		int errorinv=0;
		int aux=0;
		int grupo_id=0;
		int instance= getAD_PInstance_ID();
	    //se recorre archivo
	    while(linea!=null)
	    {	
	    	aux++;
	    	log.config(linea);
	    	
	    	if(cantLine >= 0)
	    	{
		    	//se separan campos separados por;
		    	datosFStr = linea.split(";");
		    	//se valida que no hayan datos nulos
		    	if(datosFStr[0] != null && datosFStr[1] != null && datosFStr[2] != null && datosFStr[3] != null && datosFStr[4] != null)
		    	{
		    		
		    		
		    		String rut = datosFStr[3].trim().replace(".","").trim();
		    		int amt =  Integer.parseInt(datosFStr[4].toString().trim().replace(".",""));
		    		String pago = datosFStr[0].trim().replace(".","").trim();
		    		String factura = datosFStr[1].trim().replace(".","").trim();
		    		//	ola.setAD_Org_ID(Env.getAD_Org_ID(getCtx()));
		    		//ola.setNombreOla(Sacar0Izq(datosFStr[0]));
		    		int bp_id = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce( max(c_bpartner_ID),0)   "+
									 " from c_bpartner bp "+
									 " where bp.isactive='Y' and bp.ad_Client_ID=1000000 and  bp.value='"+rut+"'"));
		    		int pago_id=0;
		    		int factura_id=0;
		    		if(bp_id>0)
		    		{
		    			pago_id=	Integer.parseInt(DB.getSQLValueString(null, "Select coalesce( max(c_payment_ID),0) from C_payment "+
						 		 " where isactive='Y' and ISALLOCATED='N' and docstatus in ('CO','CL') and documentno ='"+pago+"' and c_bpartner_ID="+bp_id));
		    			
		    			factura_id=Integer.parseInt(DB.getSQLValueString(null, "Select coalesce( max(c_invoice_ID),0) from C_invoice "+
						 		 " where isactive='Y' and ispaid='N' and docstatus in ('CO','CL')  and documentno= '"+factura+"' and c_bpartner_ID="+bp_id));
		    		}
		    			
		    		else
		    			errorbp=1;
		    		if(pago_id==0 && errorbp==0)
		    			errorpay=1;
		    		if(bp_id==0 && errorbp==0)
		    			errorinv=1;
		    		
		    		
		    	X_C_UploadAllocation repo = new X_C_UploadAllocation (getCtx(), 0, get_TrxName());
		    	repo.setValue(rut);
		    	repo.setC_BPartner_ID(bp_id);
		    	repo.setPaymentNo(pago);
		    	repo.setInvoiceNo(factura);
		    	repo.setAmount(amt);
		    	repo.set_CustomColumn("AD_PInstance_ID", instance);
		    	repo.save();
		    	repo.setDateAcct(repo.getCreated());
		    /*	try {
		    		 log.info("pase por aqui fecha 0");
		    		 
			    	 String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
			    	 log.info("pase por aqui fecha 01");
			    	 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			    	 log.info("pase por aqui fecha 02");
			    	 
			    	 Date parsedDate = (Date) dateFormat.parse(timeStamp);
			    	 log.info("pase por aqui fecha 03");
			    	 Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
			    	 log.info("pase por aqui fecha");
			    	 log.info("pase por aqui fecha"+timestamp);
			    	 repo.setDateAcct(timestamp);
		    	} catch(Exception e) { //this generic but you can control another types of exception
		    	    // look the origin of excption 
		    	}*/
		    	repo.set_CustomColumn("archivo",p_PathFile);
		    	if (errorbp==1 || errorpay==1 || errorinv==1)
		    	{
		    		if (errorbp==1)
		    			repo.setErrorMsj("Rut no encontrado");
		    		if (errorpay==1)
		    			repo.setErrorMsj("Pago no encontrado o error con el rut");
		    		if (errorinv==1)
		    			repo.setErrorMsj("Factura no encontrada o error con el rut");
		    	}
		    	else
		    	{
		    		repo.setC_BPartner_ID(bp_id);
			    	repo.setC_Invoice_ID(factura_id);
			    	repo.setC_Payment_ID(pago_id);
			    	
			    	String msj="";
			    	if(factura_id==0)
			    		msj="No se encuentra Factura, o previamente asignada";
			    	if(pago_id==0)
			    		msj=msj+", No se encuentra Pago, o previamente asignado";
			    	
			    	repo.setErrorMsj(msj);
			    	repo.save();
			    	repo.setDateAcct(repo.getCreated());
			    	
			    	repo.save();
			    	
		    	}
		    		
			    if(aux==1)
			    	grupo_id=repo.getC_UploadAllocation_ID();
			    repo.set_ValueOfColumn("C_Group_ID", grupo_id);
			    repo.save();
	    	linea=br.readLine();
	    	cantLine++;
		}
		    	else
		    	{
		    		br.close();
		    		return "No puede cargar en blanco los siguientes campos: Pago, Factura, Monto o Rut";	
		    	}
		    		
	  }
	 }
	    br.close();
	  String  asg = "Select * from C_UploadAllocation where ErrorMsj is null and AD_PInstance_ID="+ instance;
	  int contadorok=0;
	  int contadorerror=0;
	  try
		{
			PreparedStatement pstmt = DB.prepareStatement (asg, get_TrxName());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
			{
				X_C_UploadAllocation repo = new X_C_UploadAllocation (getCtx(),rs.getInt("C_UploadAllocation_ID"), get_TrxName());
				
				int saldopago=	Integer.parseInt(DB.getSQLValueString(null, "select paymentAvailable(c_payment_ID)   from c_payment where c_payment_ID="+repo.getC_Payment_ID()));
				int saldofactura= Integer.parseInt(DB.getSQLValueString(null, "select invoiceOpen(c_invoice_ID,null)  from c_invoice where c_invoice_ID="+repo.getC_Invoice_ID()));
				
				if(repo.getAmount()<=saldopago)
				{
				//	Boolean pagob=repo.getAmount()<=saldopago;
				////	log.info(repo.getAmount() + "<= "+ saldopago + " -- " + pagob);
					//Boolean facturab=repo.getAmount()<=saldofactura;
				//	log.info(repo.getAmount() + "<= "+ saldofactura + " -- " + facturab);
					if(repo.getAmount()<=saldofactura)
					{
						log.info("pase por aqui 1");
						X_C_AllocationHdr ah = new X_C_AllocationHdr (getCtx(), 0, get_TrxName());
					//	log.info("pase por aqui 2");
						ah.setDateAcct(repo.getDateAcct());
					//	log.info("pase por aqui 3");
						ah.setDateTrx(repo.getDateAcct());
						ah.setC_Currency_ID(228);
						ah.setDocStatus("DR");
						ah.setDocAction("CO");
					//	log.info("pase por aqui 4");
						ah.save();
					//	log.info("pase por aqui 5");
						X_C_AllocationLine al = new X_C_AllocationLine (getCtx(), 0, get_TrxName());
						al.setC_AllocationHdr_ID(ah.getC_AllocationHdr_ID());
						al.setC_BPartner_ID(repo.getC_BPartner_ID());
						al.setC_Payment_ID(repo.getC_Payment_ID());
						al.setC_Invoice_ID(repo.getC_Invoice_ID());
						al.setAmount(new BigDecimal (repo.getAmount()));
						al.save();
						
						
						MAllocationHdr alloc = new MAllocationHdr(getCtx(), ah.getC_AllocationHdr_ID(), get_TrxName());
						alloc.processIt(DocAction.ACTION_Complete);

						//	o.completeIt()
						alloc.save();
						
						repo.setC_AllocationHdr_ID( ah.getC_AllocationHdr_ID());
						repo.setProcessed(true);
						repo.save();
						contadorok++;
					}
					else
					{
						repo.setErrorMsj("El Monto a asignar es mayor que el saldo de la Factura");
						repo.setProcessed(true);
						repo.save();
						contadorerror++;
					}
				}
				else
				{
					repo.setErrorMsj("El Monto a asignar es mayor que el saldo del Pago");
					repo.setProcessed(true);
					repo.save();
					contadorerror++;
				}
				
				
				
			}
				
		
	  
		} catch(Exception e) { //this generic but you can control another types of exception
	    // look the origin of excption 
		}
		return "Se procesan Correctamente: "+contadorok + ", Con error: "+contadorerror;
	}	//	doIt


}	
