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

import org.compiere.model.*;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.*;
//import org.windsor.model.X_I_ReservaReq;
import org.windsor.model.X_T_OrderVoid;

/**
 *	
 *	
 *  @author italo niñoles ininoles
 *  @version $Id: ProcessPaymentRequest.java,v 1.2 2011/06/12 00:51:01  $
 */
public class ImportFileToVoidOrder extends SvrProcess
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
		String[] datosFStr = new String[4];
    	FileInputStream fis =new FileInputStream(p_PathFile);
	    InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
	    BufferedReader br = new BufferedReader(isr);
	    int cuenta =0;
	    //se lee primera linea de archivo 
	    String linea=br.readLine();		   
	    //DB.executeUpdate("DELETE FROM PP_ForecastLineCENCO",get_TrxName());
	    int cantLine = 0;
	    int no=0;
    	String clientCheck = " and ad_client_ID=1000000";
    	StringBuffer sql = new StringBuffer ("delete from T_OrderVoid where ad_org_ID=1000000 ").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
	//	commitEx();
		log.config("Borradas:"+no);
		no=0;
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
		    		
		    		String rut = datosFStr[0].trim().replace(".","").trim();
		    		String orden_str =  datosFStr[1].trim();//Integer.parseInt(datosFStr[1].toString().trim().replace(".",""));
		    		//	ola.setAD_Org_ID(Env.getAD_Org_ID(getCtx()));
		    		//ola.setNombreOla(Sacar0Izq(datosFStr[0]));
		    		no=	Integer.parseInt(DB.getSQLValueString(null, "Select coalesce( max(c_order_ID),0) from C_Order o "+
		    													 " inner join c_bpartner bp on (o.c_bpartner_ID=bp.c_Bpartner_ID) "+
		    													 " where bp.isactive='Y' and o.c_doctype_ID in (1000030,1000568, 1000103) and o.documentno='"+Integer.parseInt(orden_str)+"'"+
		    													 " and bp.value='"+rut+"'"));
		    	X_T_OrderVoid repo = new X_T_OrderVoid (getCtx(), 0, get_TrxName());
		    	repo.setRut(rut);
		    	repo.setDocumentNo(orden_str);
			    	if (no>0)
			    	{
			    		cuenta++;
			    		MOrder o = new MOrder (getCtx(), no, get_TrxName());
			    		int io_id = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(m_InOut_ID),0) from m_inout where docstatus in ('DR','IP','CO','IN') "+
								" and m_inout_ID="+ o.get_ValueAsInt("M_InOut_ID") ));
			    		int io2_id = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(m_InOut_ID),0) from m_inout where docstatus in ('DR','IP','CO','IN') "+
								" and c_order_ID="+ no ));
			    		int inv_id = Integer.parseInt(DB.getSQLValueString(null, "Select coalesce(max(c_invoice_ID),0) from c_invoice where docstatus in ('DR','IP','CO','IN') "+
								" and C_order_ID="+ no ));
			    		repo.setC_Order_ID(no);
			    		repo.setC_BPartner_ID(o.getC_BPartner_ID());
			    		String error ="";
						if(io_id==0 && io2_id==0)
						{ 
							
							if(inv_id==0)
							{
								o.processIt(MOrder.ACTION_Void);
								//	o.completeIt()
									o.save();
							repo.setDocStatus(o.getDocStatus());		
							}
							else
								error=" Pues tiene factura asociada";
							
						}
						else
							error=" Pues tiene despacho asociado";
						repo.setDocStatus(o.getDocStatus());
						if(o.getDocStatus().endsWith("VO"))
						{
							repo.setMsg("Orden:"+orden_str+" Asociada al rut:"+rut + "Anulada Correctamente");
						}
						else
						{
							repo.setERROR("Orden de Venta:"+orden_str + " asociada al rut:"+rut + " No se pudo anular " + error );
						}
			    	}
			    	else
			    	{
			    		repo.setERROR("Orden de Venta:"+orden_str + " asociada al rut:"+rut + " NO ENCONTRADA" );
			    	}
		    		
			    	repo.save();
		    	}
		    	
	    	}
	    	linea=br.readLine();
	    	cantLine++;
		}
	    
		br.close();
	    
		return "Ordenes Procesadas:"+cuenta + " de " + cantLine + " Revise Informe Notas A Anular";
	}	//	doIt


}	
