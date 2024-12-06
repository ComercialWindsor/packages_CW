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
package org.fgonzalez.process;


//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.Properties;
import java.util.logging.*;
//import java.math.BigDecimal;
//import org.compiere.model.MOrder;
//import org.compiere.model.X_C_OrderLine;
import org.compiere.model.MInvoice;
import org.compiere.model.X_C_Invoice;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
//import org.compiere.util.DB;
//import org.compiere.util.Env;

public class CompleteInvoice extends SvrProcess 
{
	//private Properties 		m_ctx;	
	
	private int p_Order = 0;
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
			else if (name.equals("C_Invoice_ID"))
				p_Order =  para[i].getParameterAsInt();
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
		
		MInvoice order = new MInvoice(getCtx(), p_Order, get_TrxName());
		String mensaje= "No Completa";
	   if (order!=null )
	   {
		   if (order.getGrandTotal().intValue()>0)
		   {
			   order.processIt(X_C_Invoice.DOCACTION_Complete);
			   order.completeIt();
			   order.save();
			   mensaje="Completada";
		   }
		   else
			   mensaje = "No Completada, Sin Lineas o valor 0";
		
	   }
	   
				
				
	return mensaje;
	}//	doIt	
}
