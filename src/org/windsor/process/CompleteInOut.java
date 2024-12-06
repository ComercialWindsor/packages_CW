package org.windsor.process;

//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.Properties;
import java.util.logging.*;

import org.compiere.model.MInOut;
//import java.math.BigDecimal;
//import org.compiere.model.MOrder;
//import org.compiere.model.X_C_OrderLine;

import org.compiere.model.X_M_InOut;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
//import org.compiere.util.DB;
//import org.compiere.util.Env;

public class CompleteInOut extends SvrProcess 
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
			else if (name.equals("M_InOut_ID"))
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
		
		MInOut order = new MInOut(getCtx(), p_Order, get_TrxName());
		String mensaje= "No Completa";
	   if (order!=null )
	   {
		   if (order.getDocStatus().equals("IP") || order.getDocStatus().equals("IN") )
		   {
			   
			  
			   order.processIt(X_M_InOut.DOCACTION_Complete);
			  // order.completeIt();
			   order.save();
			   mensaje="Completado";
		   }
		   else
			   mensaje = "No Completada, revisar stock o precios"+p_Order;
		
	   }
	   
				
				
	return mensaje;
	}//	doIt	
}
