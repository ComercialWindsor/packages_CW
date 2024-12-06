
package org.fgonzalez.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import org.compiere.model.MOrder;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;
//import org.compiere.util.Env;
 
/**
 *	Actualiza numero de bultos en la nota de venta
 *	Process_ID = 1000531
 *  
 *  @author Fernando Gonzalez
 *  @version $Id: GenerateInvoiceWS.java,v 1.2 2018/08/27 $
 */
public class PCW_Bultosnota extends SvrProcess
{
	//variables para obtener parametros
	private int 		p_Order_ID = 0;
	
	private int 	p_Totalbulto = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		
			
		/*
		 * Intento hacerlo por esta via y me arroja el siguiente error:
		 *  DB_PostgreSQL.convertStatement: PostgreSQL =>SELECT t.Name, p.ProcedureName,p.ClassName, p.AD_Process_ID, p.isReport, p.IsDirectPrint,p.AD_ReportView_ID,p.AD_Workflow_ID, CASE WHEN COALESCE(p.Statistic_Count,0)=0 THEN 0 ELSE p.Statistic_Seconds/p.Statistic_Count END, p.IsServerProcess, p.JasperReport FROM AD_Process p INNER JOIN AD_PInstance i ON (p.AD_Process_ID=i.AD_Process_ID) INNER JOIN AD_Process_Trl t ON (p.AD_Process_ID=t.AD_Process_ID AND t.AD_Language='es_CL') WHERE p.IsActive='Y' AND i.AD_PInstance_ID=?<= <SELECT t.Name, p.ProcedureName,p.ClassName, p.AD_Process_ID, p.isReport, p.IsDirectPrint,p.AD_ReportView_ID,p.AD_Workflow_ID, CASE WHEN COALESCE(p.Statistic_Count,0)=0 THEN 0 ELSE p.Statistic_Seconds/p.Statistic_Count END CASE, p.IsServerProcess, p.JasperReport FROM AD_Process p INNER JOIN AD_PInstance i ON (p.AD_Process_ID=i.AD_Process_ID)  INNER JOIN AD_Process_Trl t ON (p.AD_Process_ID=t.AD_Process_ID AND t.AD_Language='es_CL') WHERE p.IsActive='Y' AND i.AD_PInstance_ID=?> [18]
14:16:57.327     ProcessCtl.startProcess: ProcessInfo[Total Bulto Nota,Process_ID=1000531,AD_PInstance_ID=6292482,ClassName=org.fgonzalez.process.PCW_Bultosnota,Error=false,Summary=,Log=0] [18]
14:16:57.327     PCW_Bultosnota.lock: AD_PInstance_ID=6292482 [18]
14:16:57.333     DB_PostgreSQL.convertStatement: PostgreSQL =>SELECT p.ParameterName, p.P_String,p.P_String_To, p.P_Number,p.P_Number_To, p.P_Date,p.P_Date_To, p.Info,p.Info_To, i.AD_Client_ID, i.AD_Org_ID, i.AD_User_ID FROM AD_PInstance_Para p INNER JOIN AD_PInstance i ON (p.AD_PInstance_ID=i.AD_PInstance_ID) WHERE p.AD_PInstance_ID=? ORDER BY p.SeqNo<= <SELECT p.ParameterName, p.P_String,p.P_String_To, p.P_Number,p.P_Number_To, p.P_Date,p.P_Date_To, p.Info,p.Info_To,  i.AD_Client_ID, i.AD_Org_ID, i.AD_User_ID FROM AD_PInstance_Para p INNER JOIN AD_PInstance i ON (p.AD_PInstance_ID=i.AD_PInstance_ID) WHERE p.AD_PInstance_ID=? ORDER BY p.SeqNo> [18]
===========> PCW_Bultosnota.process: String index out of range: 45 [18]
java.lang.StringIndexOutOfBoundsException: String index out of range: 45
	at java.lang.String.charAt(Unknown Source)
	at org.compiere.dbPort.Convert_PostgreSQL.convertUpdate(Convert_PostgreSQL.java:448)
	at org.compiere.dbPort.Convert_PostgreSQL.convertStatement(Convert_PostgreSQL.java:139)
	at org.compiere.dbPort.Convert.convertIt(Convert.java:258)
	at org.compiere.dbPort.Convert.convert(Convert.java:231)
	at org.compiere.db.DB_PostgreSQL.convertStatement(DB_PostgreSQL.java:289)
	at org.compiere.db.PreparedStatementProxy.<init>(PreparedStatementProxy.java:39)
	at org.compiere.db.ProxyFactory.newCPreparedStatement(ProxyFactory.java:54)
	at org.compiere.util.DB.executeUpdate(DB.java:1007)
	at org.compiere.util.DB.executeUpdate(DB.java:877)
	at org.compiere.util.DB.executeUpdate(DB.java:864)
		 */
		//ProcessInfoParameter[] para = getParameter();
	/*	for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			
			else if (name.equals("C_Order_ID"))
				p_Order_ID = para[i].getParameterAsInt();
			else if (name.equals("TOTALBULTO"))
				p_Totalbulto = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}*/
	
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

		
		//select con el id del proceso para obtener la ultima instancia de este, aunque esta solucion
		//no es la optima, funciona.
		String sql = "select max(pi.AD_PINSTANCE_ID) as AD_PInstance_ID "+
					 " from AD_PINSTANCE pi "+
					 "where pi.AD_Process_ID=1000531";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			rs = pstmt.executeQuery();			
			if (rs.next())
			{
				//select con los parametros 
				String sql2 = "SELECT i.Record_ID, p.ParameterName, p.P_String, p.P_Number,p.P_NUMBER_TO, p.P_Date "+
						" FROM AD_PINSTANCE i, AD_PINSTANCE_PARA p "+
						" WHERE i.AD_PInstance_ID=" + rs.getInt("AD_PInstance_ID") + 
						" AND i.AD_PInstance_ID=p.AD_PInstance_ID "+ 
						" ORDER BY p.SeqNo ";
				PreparedStatement pstmt2 = null;
				ResultSet rs2 = null;
				try
				{
					pstmt2 = DB.prepareStatement (sql2, get_TrxName());
					rs2 = pstmt2.executeQuery();
					//aca rescato los valores de los parametros
					while (rs2.next())
					{
						if (rs2.getString("ParameterName").equals("C_Order_ID") )
							p_Order_ID = rs2.getInt("P_Number");
						if (rs2.getString("ParameterName").equals("TOTALBULTO") )
							p_Totalbulto = rs2.getInt("P_Number");
					}
					rs2.close();
					pstmt2.close();
				}
				
				catch (Exception e)
				{
					log.log(Level.SEVERE, e.getMessage(), e);
				}
			if (p_Order_ID>0)
			{
				//instancio la clase pues con DB.executeUpdate( no me resulto el update.
				MOrder order = null;
				order = new MOrder(getCtx(), p_Order_ID, get_TrxName());
				order.set_CustomColumn("TOTALBULTO", p_Totalbulto);
				order.save();
				//DB.executeUpdate("UPDATE C_Order SET TOTALBULTO= = "+p_Totalbulto+" WHERE C_Order_ID = "+p_Order_ID, get_TrxName());
			
				return "Se actualizo El numero de bultos de la nota: " + order.getDocumentNo();
			
			}
			}
			rs.close();
			pstmt.close();
		}catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	
		//	doIt
		return "No Se actualizo El numero de bultos";
}	//	OrderOpen
}