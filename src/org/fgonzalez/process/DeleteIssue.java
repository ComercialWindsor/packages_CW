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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.util.Properties;
import java.util.logging.*;

import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;
//import org.compiere.util.Env;
 
/**
 *	Genera  Inventario desde WS
 *	Process_ID = 1000450
 *  AD_Menu_ID=1000497
 *  @author fernando gonzalez
 *  @version $Id: PResSalesOrderToOrder.java,v 1.2 2014/09/12 $
 */
public class DeleteIssue extends SvrProcess
{
	//private Properties 		m_ctx;	
	
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
			else if (name.equals("AD_Issue2_ID"))
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
	
		
	
	  
	   
		
		String sql = "delete from AD_Issue isu where isu.Created< (select max(i.StatementDate)-180 from c_bankstatement i )";
		
		try
		{
			PreparedStatement pstmt = null;
			pstmt = DB.prepareStatement(sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			
			
			
			
			if (rs.next())
				return "";
			
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
						
				
		return "";		
				
			
	}	//	doIt
	
}	//	OrderOpen
