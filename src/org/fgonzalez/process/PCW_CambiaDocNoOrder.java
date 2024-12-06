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


//import java.util.Properties;
import java.util.logging.*;

import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;
//import org.compiere.util.Env;
 
/**
 *	Genera Movimientos de Inventario desde WS
 *	Process_ID = 1000445
 *  AD_Menu_ID=1000495
 *  @author fabian aguilar
 *  @version $Id: GenerateInvoiceWS.java,v 1.2 2018/07/03 $
 */
public class PCW_CambiaDocNoOrder extends SvrProcess
{
	//private Properties 		m_ctx;

	private int 		p_Order_ID = 0;
	
	private String 		p_DocumentNo = "";
	
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
			
			else if (name.equals("C_Order_ID"))
				p_Order_ID = para[i].getParameterAsInt();
			else if (name.equals("DocumentNo"))
				p_DocumentNo = (String) para[i].getParameter();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	//m_ctx = Env.getCtx();
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws AdempiereSystemError
	{
		/*********/

		
		
		
		
		
		if (p_Order_ID>0)
		{
			
			DB.executeUpdate("Update C_Order set DocumentNo=" +p_DocumentNo+ " where  C_Order_ID="+p_Order_ID, this.get_TrxName());
		
		
		
		}
		return "";
	}	//	doIt
	
}	//	OrderOpen
