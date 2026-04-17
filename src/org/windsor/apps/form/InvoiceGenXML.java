/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.windsor.apps.form;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.apps.IStatusBar;
import org.compiere.apps.form.GenForm;
import org.compiere.minigrid.IDColumn;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MRole;
import org.compiere.model.MUser;
import org.compiere.model.Query;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.ofb.model.OFBForward;

import org.windsor.process.ExportDTEInvoice;

/**
 * Generate Invoice (manual) controller class
 * 
 */
public class InvoiceGenXML extends GenForm
{
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(InvoiceGenXML.class);
	//
	
	public Object 			m_AD_Org_ID = null;
	public Object 			m_C_BPartner_ID = null;
	public Object 			m_DateFrom = null;
	public Object 			m_DateTo = null;
	public Object  			m_User_ID = null; 
	//public Object  m_POReference = null;
	public Object  			m_BPLocation_ID= null;
	public Object  			m_IsTxtGen= null;
	
	public void dynInit() throws Exception
	{
		setTitle("InvGenerateInfo");
		setReportEngineType(ReportEngine.INVOICE);
		setAskPrintMsg("PrintInvoices");
	}
	
	public void configureMiniTable(IMiniTable miniTable)
	{
		//  create Columns
		miniTable.addColumn("C_Order_ID");
		miniTable.addColumn("AD_Org_ID");
		miniTable.addColumn("C_DocType_ID");
		miniTable.addColumn("DocumentNo");
		miniTable.addColumn("Description");
		miniTable.addColumn("C_BPartner_ID");
		miniTable.addColumn("DateOrdered");
		miniTable.addColumn("TotalLines");
		//
		miniTable.setMultiSelection(true);
	   //  set details
		miniTable.setColumnClass(0, IDColumn.class, false, " ");
		miniTable.setColumnClass(1, String.class, true, Msg.translate(Env.getCtx(), "AD_Org_ID"));
		miniTable.setColumnClass(2, String.class, true, Msg.translate(Env.getCtx(), "C_DocType_ID"));
		miniTable.setColumnClass(3, String.class, true, Msg.translate(Env.getCtx(), "DocumentNo"));
		miniTable.setColumnClass(4, String.class, true, Msg.translate(Env.getCtx(), "Description"));
		miniTable.setColumnClass(5, String.class, true, Msg.translate(Env.getCtx(), "C_BPartner_ID"));
		miniTable.setColumnClass(6, Timestamp.class, true, Msg.translate(Env.getCtx(), "DateInvoiced"));
		miniTable.setColumnClass(7, BigDecimal.class, true, Msg.translate(Env.getCtx(), "TotalLines"));
		miniTable.autoSize();
	}
	
	/**
	 * Get SQL for Orders that needs to be shipped
	 * @return sql
	 */
	private String getOrderSQL(int ID_Type)
	{
	    StringBuffer sql = new StringBuffer(
	            "SELECT ic.C_Invoice_ID, o.Name, dt.Name, ic.DocumentNo, bp.Name, ic.DateInvoiced, ic.TotalLines "
	            + "FROM C_InvoiceTxt ic, AD_Org o, C_BPartner bp, C_DocType dt "
	            + "WHERE ic.AD_Org_ID=o.AD_Org_ID"
	            + " AND ic.C_BPartner_ID=bp.C_BPartner_ID"
	            + " AND ic.C_DocType_ID=dt.C_DocType_ID"
	            + " AND ic.AD_Client_ID=?");

        if (m_AD_Org_ID != null)
            sql.append(" AND ic.AD_Org_ID=").append(m_AD_Org_ID);
        //ininoles se agrega validacion para que no muestre las facturas de todas la ORG
        else//codigo de validaciones para organizaciones        	
        {
        	MRole role = new MRole(Env.getCtx(), Env.getAD_Role_ID(Env.getCtx()),null);
        	if(!role.isAccessAllOrgs())//sin acceso a todas las organizaciones
        	{
            	String sqlValidOrg = "";
        		if(role.isUseUserOrgAccess())//usuario usa acceso a organizaciones
        			sqlValidOrg = "SELECT AD_Org_ID FROM AD_User_OrgAccess WHERE IsActive = 'Y' AND AD_User_ID = "+Env.getAD_User_ID(Env.getCtx());
        		else //rol usa acceso a organizacion 
        			sqlValidOrg = "SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE IsActive = 'Y' AND AD_Role_ID = "+role.get_ID();        		
        		sql.append(" AND ic.AD_Org_ID IN ("+sqlValidOrg+")");
        	}        	
        }//ininoles end	
        if (m_C_BPartner_ID != null)
            sql.append(" AND ic.C_BPartner_ID=").append(m_C_BPartner_ID);
        
        if (m_DateFrom != null || m_DateTo != null)
		{
			Timestamp from = (Timestamp) m_DateFrom;
			Timestamp to = (Timestamp) m_DateTo;
			if (from == null && to != null)
				sql.append(" AND TRUNC(ic.DateInvoiced) <= ?");
			else if (from != null && to == null)
				sql.append(" AND TRUNC(ic.DateInvoiced) >= ?");
			else if (from != null && to != null)
				sql.append(" AND TRUNC(ic.DateInvoiced) BETWEEN ? AND ?");
		}
        
        
       if(m_User_ID!=null)
    	   sql.append(" AND ic.AD_User_ID=?");
       if(m_BPLocation_ID!=null)
    	   sql.append(" AND ic.C_BPartner_Location_ID=?");
       if(ID_Type == 1)
   		   sql.append(" AND ic.IsTxtGen = 'Y' ");
   	   else
   		   sql.append(" AND (ic.IsTxtGen <> 'Y' OR ic.IsTxtGen IS NULL)");

       
       sql.append(" ORDER BY o.Name,bp.Name,ic.DateInvoiced");
        
       return sql.toString();
	}
	
	/**
	 * Get SQL for Customer RMA that need to be invoiced
	 * @return sql
	 */
	
	public void executeQuery(KeyNamePair IsTxtGenKNPair, IMiniTable miniTable)
	{
		log.info("");
		int AD_Client_ID = Env.getAD_Client_ID(Env.getCtx());
		//  Create SQL
		
		String sql = getOrderSQL(IsTxtGenKNPair.getKey());
       
		//  reset table
		int row = 0;
		miniTable.setRowCount(row);
		//  Execute
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, AD_Client_ID);
			int i=2;
			 if (m_DateFrom != null || m_DateTo != null)
				{
				 Timestamp from = (Timestamp) m_DateFrom;
					Timestamp to = (Timestamp) m_DateTo;
					log.fine("Date From=" + from + ", To=" + to);
					if (from == null && to != null)
						pstmt.setTimestamp(i++, to);
					else if (from != null && to == null)
						pstmt.setTimestamp(i++, from);
					else if (from != null && to != null)
					{
						pstmt.setTimestamp(i++, from);
						pstmt.setTimestamp(i++, to);
					}
				}
			 if(m_User_ID!=null)
				 pstmt.setInt(i++, (Integer)m_User_ID);
		     if(m_BPLocation_ID!=null)
		    	 pstmt.setInt(i++, (Integer)m_BPLocation_ID);
		     //if(m_POReference!=null && ((String)m_POReference).length()>0)
		    //	 pstmt.setString(i++, (String)m_POReference);
			ResultSet rs = pstmt.executeQuery();
			//
			while (rs.next())
			{
				//  extend table
				miniTable.setRowCount(row+1);
				//  set values
				miniTable.setValueAt(new IDColumn(rs.getInt(1)), row, 0);   //  C_Order_ID
				miniTable.setValueAt(rs.getString(2), row, 1);              //  Org
				miniTable.setValueAt(rs.getString(3), row, 2);              //  DocType
				miniTable.setValueAt(rs.getString(4), row, 3);              //  Doc No
				if(OFBForward.NewDescriptionInvoiceGenPA())
				{
					String newDescription = DB.getSQLValueString(null, "SELECT string_agg(documentno, ', ' order by documentno)" +
							" FROM M_InOut WHERE C_Order_ID = "+rs.getInt(1)); 
					newDescription = newDescription +"-"+new MOrder(Env.getCtx(),rs.getInt(1),null).getSalesRep().getName();
					miniTable.setValueAt(newDescription, row, 4);              //  Description faaguuilar Added
				}
				else
					miniTable.setValueAt(new MInvoice(Env.getCtx(),rs.getInt(1),null).getDescription(), row, 4);              //  Description 
				miniTable.setValueAt(rs.getString(5), row, 5);              //  BPartner
				miniTable.setValueAt(rs.getTimestamp(6), row, 6);           //  DateOrdered
				miniTable.setValueAt(rs.getBigDecimal(7), row, 7);          //  TotalLines
				//  prepare next
				row++;
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		//
		miniTable.autoSize();
	//	statusBar.setStatusDB(String.valueOf(miniTable.getRowCount()));
	}   //  executeQuery
	
	/**
	 *	Save Selection & return selecion Query or ""
	 *  @return where clause like C_Order_ID IN (...)
	 */
	public void saveSelection(IMiniTable miniTable)
	{
		log.info("");
		//  Array of Integers
		ArrayList<Integer> results = new ArrayList<Integer>();
		setSelection(null);

		//	Get selected entries
		int rows = miniTable.getRowCount();
		for (int i = 0; i < rows; i++)
		{
			IDColumn id = (IDColumn)miniTable.getValueAt(i, 0);     //  ID in column 0
		//	log.fine( "Row=" + i + " - " + id);
			if (id != null && id.isSelected())
				results.add(id.getRecord_ID());
		}

		if (results.size() == 0)
			return;
		log.config("Selected #" + results.size());
		setSelection(results);
	}	//	saveSelection

	
	/**************************************************************************
	 *	Generate Invoices
	 */
	public String generate(IStatusBar statusBar, KeyNamePair detTypeKNPair)
	{
		String info = "";
		String trxName = Trx.createTrxName("IVG");
		Trx trx = Trx.get(trxName, true);	//trx needs to be committed too
		
		setSelectionActive(false);  //  prevents from being called twice
		statusBar.setStatusLine(Msg.getMsg(Env.getCtx(), "InvGenerateGen"));
		statusBar.setStatusDB(String.valueOf(getSelection().size()));		
		
		//insert selection
		for(Integer selectedId : getSelection())
		{
			info = generateXML(trxName, trx ,selectedId, detTypeKNPair.getKey());
			
			if(info!= null){
				return info;
			}
			//se generan archivos de txt en base a cada factura
			//MInvoice inv = new MInvoice(Env.getCtx(), selectedId, trxName);
			/*int AD_Process_ID = DB.getSQLValue(trxName, "SELECT AD_Process_ID FROM AD_Process WHERE value like 'TestGenTxt'");;
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, selectedId);
			instance.createParameter(10, "", detTypeKNPair.getKey());
			if (!instance.save())
			{
				info = Msg.getMsg(Env.getCtx(), "ProcessNoInstance");
				return info;
			}
			ProcessInfo pi = new ProcessInfo ("", AD_Process_ID);
			pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
			setTrx(trx);
			setProcessInfo(pi);*/
			/*MUser user = new MUser(Env.getCtx(), Env.getAD_User_ID(Env.getCtx()), trxName);
			try 
			{
	            String ruta = user.get_ValueAsString("InvoicePathFile");
	            ruta=ruta+inv.getDocumentNo()+".txt";
	            String ln = System.getProperty("line.separator");
				StringBuilder str = new StringBuilder();
				//se agrega cabecera de archivo
				String sqlCab = "select LINEA1,LINEA2,LINEA3,LINEA4,LINEA5,LINEA6,LINEA7," +
						" LINEA8,LINEA9,LINEA10,LINEA11,LINEA12,LINEA13,LINEA14,LINEA15," +
						" LINEA16,LINEA17,LINEA18,LINEA19,LINEA20,LINEA21,LINEA22,LINEA23," +
						" LINEA24,LINEA25,LINEA26,LINEA27,LINEA28,LINEA29,LINEA30,LINEA31," +
						" LINEA32,LINEA33,LINEA34 FROM RVCW_CABECERAFACTURAELECW" +
						" WHERE c_invoice_ID="+inv.get_ID();
				log.config("sql cabecera:"+sqlCab);
				PreparedStatement pstmt = DB.prepareStatement(sqlCab, trxName);					
				ResultSet rs = pstmt.executeQuery();
				if(rs.next())
				{
					for(int a=1;a<=34;a++)
					{
						if(a != 10)
						{
							str.append(rs.getString("LINEA"+a));
							str.append(ln);
						}
					}
				}
				//se agrega linea cabecera detalle
				str.append("2:|ITEM|TIPO ITEM |CODIGO |DESCRIPCION |CANTIDAD |PRECIO |DESCUENTO MONTO|TOTAL");
				
				//se agrega detalle a archivo
				String sqlDet = "";
				if(detTypeKNPair.getKey() == 1)
					sqlDet= "SELECT DETALLE FROM RVCW_LINEAFACTURAELECW WHERE c_invoice_ID=?";
				else if (detTypeKNPair.getKey() == 2)
					sqlDet= "SELECT DETALLE FROM RVCW_LINEAFACTURAELECWCG WHERE c_invoice_ID=?";
				else if (detTypeKNPair.getKey() == 3)
					sqlDet= "SELECT DETALLE FROM RVCW_LINEAFACTURAELECOMBO WHERE c_invoice_ID=?";
					
				log.config("sql detalle:"+sqlDet);
				PreparedStatement pstmtDet = DB.prepareStatement(sqlDet, trxName);	
				pstmtDet.setInt(1, inv.get_ID());
				ResultSet rsDet = pstmtDet.executeQuery();
				while(rsDet.next())
				{
					str.append(ln);
					str.append(rsDet.getString("DETALLE"));					
				}
				
	            File file = new File(ruta);
	            // Si el archivo no existe es creado
	            if (!file.exists()) {
	                file.createNewFile();
	            }
	            FileWriter fw = new FileWriter(file);
	            BufferedWriter bw = new BufferedWriter(fw);
	            bw.write(str.toString());
	            bw.close();
	            
	            //ininoles se actualiza campo indicativo
	            DB.executeUpdate("UPDATE C_Invoice SET IsTxtGen = 'Y' WHERE C_Invoice_ID = "+inv.get_ID(), trxName);
			} catch (Exception e) {
	            e.printStackTrace();
	        }
			 */
		}		
		
		return info;
	}	//	generateInvoices
	
	private String generateXML (String trxName, Trx trx,int C_Invoice_ID, int detailType){
		ArrayList<String> detail = new ArrayList<String>();
		detail.add("N");
		detail.add("N");
		detail.add("G");
		detail.add("C");
		String className=ExportDTEInvoice.class.getName();
		Query q = new Query(Env.getCtx(), MProcess.Table_Name, " ClassName=?", null).setOnlyActiveRecords(true)
				.setParameters(className);
		if(q != null && q.count() > 1) {
			String processValue = "";
			q = new Query(Env.getCtx(), MProcess.Table_Name, " Value=? ", trxName).setOnlyActiveRecords(true)
				.setParameters(processValue);
		}
		MProcess pr = null;
		if(q != null)
			pr = q.first();
		
		if (pr==null) {
	      log.warning("Process " + className + " does not exist. ");
		} else if(pr.getClassname() != null) {
			// Create an instance of the actual process class.
			ExportDTEInvoice process = new ExportDTEInvoice();

			MPInstance mpi = new MPInstance(Env.getCtx(), pr.getAD_Process_ID(), C_Invoice_ID);
			//mpi.setClientOrg(this);
			mpi.save();
			
			int seqNo = 10;
			for(MProcessPara para : pr.getParameters()) {
				if(para.getColumnName().equals("DetailType")) {
					mpi.createParameter(seqNo, para.getColumnName(), detail.get(detailType));
				}
				seqNo = seqNo+10;
			}
			
			// Connect the process to the process instance.
			ProcessInfo pi = new ProcessInfo("Generacion de XML", pr.getAD_Process_ID());//new ProcessInfo("Envio de TXT a Balanza", pr.getAD_Process_ID(), MBSCAPriceChange.Table_ID, getBSCA_PriceChange_ID());
			setTrx(trx);
			setProcessInfo(pi);
			pi.setAD_PInstance_ID(mpi.get_ID());
			pi.setRecord_ID(C_Invoice_ID);
			
			log.info("Starting process " + pr.getName());
			
			try {
				if(!process.startProcess(Env.getCtx(), pi, trx)) {
					String errorMsg = "Error en Proceso " + className + ": " + pi.getSummary();
					log.severe(errorMsg);
					return errorMsg;
				} else {
					//se actualiza campo indicativo
		           // DB.executeUpdateEx("UPDATE C_Invoice SET IsTxtGen = 'Y' WHERE C_Invoice_ID = "+ C_Invoice_ID, trxName);
					Thread.sleep(3000);
				}
			} catch (IllegalAccessError e) {
				return e.getMessage().toString();
			} catch (InterruptedException e) {
				return e.getMessage().toString();
			}
		}
		return null;
	}
	
}