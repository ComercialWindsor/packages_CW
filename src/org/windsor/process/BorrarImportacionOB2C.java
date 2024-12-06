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
import java.sql.SQLException;
import java.util.logging.*;

import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;



//import org.compiere.util.Env;
 
/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class BorrarImportacionOB2C extends SvrProcess
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
			else if (name.equals("C_OrderB2C_ID"))
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
	 * @throws SQLException 
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws AdempiereSystemError, SQLException
	{
		/*********/
	
		
		StringBuffer sql = null;
		int no = 0;


		//	****	Prepare	****

		//	Delete Old Imported
		
			sql = new StringBuffer ("DELETE I_OrderB2C "
				  + "WHERE  ad_client_ID=1000000 and C_BPartner_ID<>1001237");
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
			
			//commitEx();
		
		return "Borrada carga anterior, Registros borrados:"+no;		
				
			
	}	//	doIt
	
}	//	OrderOpen
