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
//import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
public class Job_Venta02018 extends SvrProcess
{
	//private Properties 		m_ctx;

	/*private int 		p_Order_ID = 0;
	
	private String 		p_DocumentNo = "";*/
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
	/*	ProcessInfoParameter[] para = getParameter();
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

		
		
		
		
		
			DB.executeUpdate("Update T_OVNOFACTURADAMU set newDolar=730", this.get_TrxName());
			//DB. ("delete from t_Venta02018insertar", this.get_TrxName());
			String sql = "DELETE from  t_Venta02018insertar";
		//	String mensaje = "";
			PreparedStatement pstmt = null;
			//PreparedStatement pstmtDELETE = null;
			ResultSet rs = null;
			 Statement stmt = DB.createStatement();
			try
			{
				int x = stmt.executeUpdate(sql); 
			//	pstmtDELETE = DB.prepareStatement(sql);
				//pstmtDELETE.executeUpdate();
				if (x == 0 )
						;
				sql = "select count(*) Cantidad from   t_Venta02018insertar";
				pstmt = DB.prepareStatement (sql, get_TrxName());
				rs = pstmt.executeQuery();			
				
				if (rs.next()){
					
				
				
				
				/*
				 * 
				 * 
				 */
					String sqlSelect = "SELECT AD_CLIENT_ID, " +
						   "      AD_ORG_ID, " +
						   "    CREATED, " +
						   "      CREATEDBY, " +
						   "      ISACTIVE, " +
						   "    UPDATED, " +
						   "      UPDATEDBY, " +
						   "      ENERO2015, " +
						   "      FEBRERO2015, " +
						   "      MARZO2015, " +
						   "        ABRIL2015, " +
						   "      MAYO2015, " +
						   "      JUNIO2015, " +
						   "      JULIO2015, " +
						   "      AGOSTO2015, " +
						   "      SEPTIEMBRE2015, " +
						   "      OCTUBRE2015, " +
						   "      NOVIEMBRE2015, " +
						   "      DICIEMBRE2015, " +
						   "      AGRUPACION, " +
						   "      M_PRODUCT_FAMILY_ID, " +
						   "      M_PRODUCT_CATEGORY_ID, " +
						   "      M_PRODUCT_ID, " +
						   "      C_YEAR_ID, " +
						   "      M_PRODUCT_CLIENT_ID, " +
						   "      M_PRODUCT_MARK_ID, " +
						  "    DATEINOUT1, " +
						  "    DATEINOUT2, " +
						   "    DATEINOUT3, " +
						   "    DATEINOUT4, " +
						   "    DATEINOUT5, " +
						   "      QTYINOUT1, " +
						   "      QTYINOUT2, " +
						   "      QTYINOUT3, " +
						   "      QTYINOUT4, " +
						   "      QTYINOUT5, " +
						   "      PRODUCTVALUE, " +
						   "      QTYONHAND, " +
						   "      QTYAVIABLE, " +
						   "    DATEPROMISED1, " +
						   "    DATEPROMISED2, " +
						   "    DATEPROMISED3, " +
						   "    DATEPROMISED4, " +
						   "      QTYOC1, " +
						   "      QTYOC2, " +
						   "      QTYOC3, " +
						   "      QTYOC4, " +
						   "      ORDENC1, " +
						   "      ORDENC2, " +
						   "      ORDENC3, " +
						   "      ORDENC4, " +
						   "      QTYRECIBIDA, " +
						   "      QTYDESPACHADA, " +
						   "      SALDO, " +
						   "      M_PRODUCT_LINEA_ID, " +
						   "      QTYRESERVED, " +
						   "      ENERO2018, " +
						   "      FEBRERO2018, " +
						   "      MARZO2018, " +
						   "      ABRIL2018, " +
						   "      MAYO2018, " +
						   "      JUNIO2018, " +
						   "      JULIO2018, " +
						   "      AGOSTO2018, " +
						   "      SEPTIEMBRE2018, " +
						   "      OCTUBRE2018, " +
						   "      NOVIEMBRE2018, " +
						   "      DICIEMBRE2018, " +
						   "      SUMOCS, " +
						   "      ISBOM, " +
						   "      M_PRODUCTI_ID, " +
						   "      PRODUCVALUEI, " +
						   "      QTYONHANDI, " +
						   "      QTYAVIABLEI, " +
						   "     DATEPROMISED1I," +
						   "     DATEPROMISED2I, " +
						   "     DATEPROMISED3I, " +
						   "     DATEPROMISED4I," +
						  "      QTYOC1I, " +
						   "      QTYOC2I, " +
						   "      QTYOC3I, " +
						   "      QTYOC4I, " +
						   "      ORDENC1I, " +
						   "      ORDENC2I, " +
						   "      ORDENC3I, " +
						   "      ORDENC4I, " +
						   "      QTYONHANDT, " +
						   "      QTYRESERVEDI, " +
						   "      TOTALOCI, " +
						   "      VTA2015, " +
						   "      VTA2018, " +
						   "      QTYPRODUCTO, " +
						   "      QTYINSUMO, " +
						   "      QTYPROTPROTINS, " +
						   "      COSTPRICE, " +
						   "      NEWDOLAR, " +
						   "      PRICELIST, " +
						   "      PRICELIQ, " +
						   "      NEWCOST, " +
						   "      VTA2017, " +
						   "      ENERO2017, " +
						   "      FEBRERO2017, " +
						   "      MARZO2017, " +
						   "      ABRIL2017, " +
						   "      MAYO2017, " +
						   "      JUNIO2017, " +
						   "      JULIO2017, " +
						   "      AGOSTO2017, " +
						   "      SEPTIEMBRE2017, " +
						   "      OCTUBRE2017, " +
						   "      NOVIEMBRE2017, " +
						   "      DICIEMBRE2017, " +
						   "      MEASURE, " +
						   "      COLOR, " +
						   "      DESIGN, " +
						   "      ENERO2016, " +
						   "      FEBRERO2016, " +
						   "      MARZO2016, " +
						   "      ABRIL2016, " +
						   "      MAYO2016, " +
						   "      JUNIO2016, " +
						   "      JULIO2016, " +
						   "      AGOSTO2016, " +
						   "      SEPTIEMBRE2016, " +
						   "      OCTUBRE2016, " +
						   "      NOVIEMBRE2016, " +
						   "      DICIEMBRE2016, " +
						   "      VTA2016, " +
						   "      CLASIFICACION, " +
						   "      TIPOVENTA, " +
						   "      SHELFDEPTH, " +
						   "      SHELFHEIGHT, " +
						   "      SHELFWIDTH, " +
						   "      M3, " +
						   "      OCINOUT1, " +
						   "      OCINOUT2, " +
						   "      OCINOUT3, " +
						   "      OCINOUT4, " +
						   "      OCINOUT5, " +
						   "     VIGENCIA " +
						   " FROM rvcw_ventacero201804a2018j  ";
		//			String sqlSelect2 ="select ad_Client_ID, DESCRIPTION as VIGENCIA from c_charge";
					PreparedStatement pstmtSelect = null;
					
					ResultSet rsSelect  = null;
					
					try {
					//	System.out.print(sqlSelect);
						//pstmtSelect = DB.prepareStatement (sqlSelect, get_TrxName());
						pstmtSelect = DB.prepareStatement (sqlSelect,null);
						rsSelect = pstmtSelect.executeQuery();
					
						
						
						while (rsSelect.next()){
							
							//System.out.println("AD_Client_ID="+rsSelect.getInt("AD_Client_ID")+" , VIGENCIA="+rsSelect.getString("VIGENCIA"));
							
						
//							String hhhs = "" ;
//
							
							String	sql2 = "insert into t_Venta02018insertar( AD_CLIENT_ID, "+
									   " AD_ORG_ID, " +
									   " CREATED,  " +
									   " CREATEDBY,  " +
									   " ISACTIVE, " +
									  " UPDATED, " +
									"   UPDATEDBY, " +
								 "  ENERO2015, " +
									  " FEBRERO2015, " +
									   " MARZO2015, " +
								   " ABRIL2015, " +
									  " MAYO2015, " +
									  " JUNIO2015, " +
									  " JULIO2015, " +
									   " AGOSTO2015, " +
									   " SEPTIEMBRE2015, " +
									   " OCTUBRE2015, " +
									   " NOVIEMBRE2015, " +
									   " DICIEMBRE2015, " +
									   " AGRUPACION, " +
									   " M_PRODUCT_FAMILY_ID, " +
									   " M_PRODUCT_CATEGORY_ID, " +
									   " M_PRODUCT_ID, " +
									   " C_YEAR_ID, " +
									   " M_PRODUCT_CLIENT_ID, " +
									   "  M_PRODUCT_MARK_ID, " +
									   " DATEINOUT1, " +
									   " DATEINOUT2, " +
									   " DATEINOUT3, " +
									   " DATEINOUT4, " +
									   " DATEINOUT5, " +
									   " QTYINOUT1, " +
									   " QTYINOUT2, " +
									   " QTYINOUT3, " +
									   " QTYINOUT4, " +
									   " QTYINOUT5, " +
									   " PRODUCTVALUE, " +
									   " QTYONHAND, " +
									   " QTYAVIABLE, " +
									   " DATEPROMISED1, " +
									   " DATEPROMISED2, " +
									   " DATEPROMISED3, " +
									   " DATEPROMISED4, " +
									   " QTYOC1, " +
									   " QTYOC2, " +
									   " QTYOC3, " +
									   " QTYOC4, " +
									   " ORDENC1, " +
									   " ORDENC2, " +
									   " ORDENC3, " +
									   " ORDENC4, " +
									   " QTYRECIBIDA, " +
									   " QTYDESPACHADA, " +
									   " SALDO, " +
									   " M_PRODUCT_LINEA_ID, " +
									   " QTYRESERVED, " +
									   " ENERO2018, " +
									   " FEBRERO2018, " +
									   " MARZO2018, " +
									   " ABRIL2018, " +
									   " MAYO2018, " +
									   " JUNIO2018, " +
									   " JULIO2018, " +
									   " AGOSTO2018, " +
									   " SEPTIEMBRE2018, " +
									   " OCTUBRE2018, " +
									   " NOVIEMBRE2018, " +
									   " DICIEMBRE2018, " +
									   " SUMOCS, " +
									   " ISBOM, " +
									   " M_PRODUCTI_ID, " +
									   " PRODUCVALUEI, " +
									   " QTYONHANDI, " +
									   " QTYAVIABLEI, " +
									   " DATEPROMISED1I, " +
									   " DATEPROMISED2I, " +
									   " DATEPROMISED3I, " +
									   " DATEPROMISED4I, " +
									   " QTYOC1I, " +
									   " QTYOC2I, " +
									   " QTYOC3I, " +
									   " QTYOC4I, " +
									   " ORDENC1I, " +
									   " ORDENC2I, " +
									   " ORDENC3I, " +
									   " ORDENC4I, " +
									   " QTYONHANDT, " +
									   " QTYRESERVEDI, " +
									   " TOTALOCI, " +
									   " VTA2015, " +
									   " VTA2018, " +
									   " QTYPRODUCTO, " +
									   " QTYINSUMO, " +
									   " QTYPROTPROTINS, " +
									   " COSTPRICE, " +
									   " NEWDOLAR, " +
									   " PRICELIST, " +
									   " PRICELIQ, " +
									   " NEWCOST, " +
									   " VTA2017, " +
									   " ENERO2017, " +
									   " FEBRERO2017, " +
									   " MARZO2017, " +
									   " ABRIL2017, " +
									   " MAYO2017, " +
									   " JUNIO2017, " +
									   " JULIO2017, " +
									   " AGOSTO2017, " +
									   " SEPTIEMBRE2017, " +
									   " OCTUBRE2017, " +
									   " NOVIEMBRE2017, " +
									   " DICIEMBRE2017, " +
									   " MEASURE, " +
									   " COLOR, " +
									   " DESIGN, " +
									   " ENERO2016, " +
									   " FEBRERO2016, " +
									   " MARZO2016, " +
									   " ABRIL2016, " +
									   " MAYO2016, " +
									   " JUNIO2016, " +
									   " JULIO2016, " +
									   " AGOSTO2016, " +
									   " SEPTIEMBRE2016, " +
									   " OCTUBRE2016, " +
									   " NOVIEMBRE2016, " +
									   " DICIEMBRE2016, " +
									   " VTA2016, " +
									   " CLASIFICACION, " +
									   " TIPOVENTA, " +
									   " SHELFDEPTH, " +
									   " SHELFHEIGHT, " +
									   " SHELFWIDTH, " +
								   " M3, " +
									   " OCINOUT1, " +
									   " OCINOUT2, " +
									   " OCINOUT3, " +
									   " OCINOUT4, " +
									   " OCINOUT5, " +
									   " VIGENCIA) values("+ rsSelect.getInt("AD_CLIENT_ID")+ " , " +
									   				   rsSelect.getInt("AD_ORG_ID")+" , " +
									  " TO_DATE('"+ 			  rsSelect.getDate("Created") + "','yyyy-mm-dd') , " +
									   rsSelect.getInt("CREATEDBY") + " , " +
									"'"+   rsSelect.getString("ISACTIVE") + "' , " +
									  " TO_DATE('"+   rsSelect.getDate("UPDATED")+ "','yyyy-mm-dd') , " +
									   rsSelect.getInt("UPDATEDBY")+ " , " +
									   rsSelect.getInt("ENERO2015")+ " , " +
									   rsSelect.getInt("FEBRERO2015") + " , " +
									   rsSelect.getInt("MARZO2015") + " , " +
									   rsSelect.getInt("ABRIL2015") + " , " +
									   rsSelect.getInt("MAYO2015") + " , " +
									   rsSelect.getInt("JUNIO2015") + " , " +
									   rsSelect.getInt("JULIO2015") + " , " +
									   rsSelect.getInt("AGOSTO2015") + " , " +
									   rsSelect.getInt("SEPTIEMBRE2015") + " , " +
									   rsSelect.getInt("OCTUBRE2015") + " , " +
									   rsSelect.getInt("NOVIEMBRE2015") + " , " +
									   rsSelect.getInt("DICIEMBRE2015") + " , " +
									   "Case when '"+    rsSelect.getString("AGRUPACION") + "' like 'null' then '' else  '" +    rsSelect.getString("AGRUPACION") + "' end , " +
									   rsSelect.getInt("M_PRODUCT_FAMILY_ID") + " , " +
									   rsSelect.getInt("M_PRODUCT_CATEGORY_ID") + " , " +
									   rsSelect.getInt("M_PRODUCT_ID") + " , " +
									   rsSelect.getInt("C_YEAR_ID") + " , " +
								       rsSelect.getInt("M_PRODUCT_CLIENT_ID") + " , " +
									   rsSelect.getInt("M_PRODUCT_MARK_ID") + " , " +
									 "case when '"+     rsSelect.getDate("DATEINOUT1")+ "' like 'null' then " +		 rsSelect.getDate("DATEINOUT1")   +" else  TO_DATE('"+   rsSelect.getDate("DATEINOUT1")+ "','yyyy-mm-dd') end , " +
									 "case when '"+     rsSelect.getDate("DATEINOUT2")+ "' like 'null' then " +		 rsSelect.getDate("DATEINOUT2")   +" else  TO_DATE('"+   rsSelect.getDate("DATEINOUT2")+ "','yyyy-mm-dd') end , " +
									 "case when '"+     rsSelect.getDate("DATEINOUT3")+ "' like 'null' then " +		 rsSelect.getDate("DATEINOUT3")   +" else  TO_DATE('"+   rsSelect.getDate("DATEINOUT3")+ "','yyyy-mm-dd') end , " +
									 "case when '"+     rsSelect.getDate("DATEINOUT4")+ "' like 'null' then " +		 rsSelect.getDate("DATEINOUT4")   +" else  TO_DATE('"+   rsSelect.getDate("DATEINOUT4")+ "','yyyy-mm-dd') end , " +
									 "case when '"+     rsSelect.getDate("DATEINOUT5")+ "' like 'null' then " +		 rsSelect.getDate("DATEINOUT5")   +" else  TO_DATE('"+   rsSelect.getDate("DATEINOUT5")+ "','yyyy-mm-dd') end , " +
									   rsSelect.getInt("QTYINOUT1") + " , " +
									   rsSelect.getInt("QTYINOUT2") + " , " +
									   rsSelect.getInt("QTYINOUT3") + " , " +
									   rsSelect.getInt("QTYINOUT4") + " , " +
									   rsSelect.getInt("QTYINOUT5") + " , " +
									   "Case when '"+     rsSelect.getString("PRODUCTVALUE") + "' like 'null' then '' else  '" +    rsSelect.getString("PRODUCTVALUE")  + "' end , " +
									   rsSelect.getInt("QTYONHAND") + " , " +
									   rsSelect.getInt("QTYAVIABLE") + " , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED1")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED1")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED1")+ "','yyyy-mm-dd') end , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED2")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED2")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED2")+ "','yyyy-mm-dd') end , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED3")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED3")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED3")+ "','yyyy-mm-dd') end , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED4")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED4")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED4")+ "','yyyy-mm-dd') end , " +
									   rsSelect.getInt("QTYOC1") + " , " +
									   rsSelect.getInt("QTYOC2") + " , " +
									   rsSelect.getInt("QTYOC3") + " , " +
									   rsSelect.getInt("QTYOC4") + " , " +
									   rsSelect.getInt("ORDENC1") + " , " +
									   rsSelect.getInt("ORDENC2") + " , " +
									   rsSelect.getInt("ORDENC3") + " , " +
									   rsSelect.getInt("ORDENC4") + " , " +
									   rsSelect.getInt("QTYRECIBIDA") + " , " +
									   rsSelect.getInt("QTYDESPACHADA") + " , " +
									   rsSelect.getInt("SALDO") + " , " +
									   rsSelect.getInt("M_PRODUCT_LINEA_ID") + " , " +
									   rsSelect.getInt("QTYRESERVED") + " , " +
									   rsSelect.getInt("ENERO2018") + " , " +
									   rsSelect.getInt("FEBRERO2018") + " , " +
									   rsSelect.getInt("MARZO2018") + " , " +
									   rsSelect.getInt("ABRIL2018") + " , " +
									   rsSelect.getInt("MAYO2018") + " , " +
									   rsSelect.getInt("JUNIO2018") + " , " +
									   rsSelect.getInt("JULIO2018") + " , " +
					    	    	   rsSelect.getInt("AGOSTO2018") + " , " +
					    	    	   rsSelect.getInt("SEPTIEMBRE2018") + " , " +
									   rsSelect.getInt("OCTUBRE2018") + " , " +
									   rsSelect.getInt("NOVIEMBRE2018") + " , " +
									   rsSelect.getInt("DICIEMBRE2018") + " , " +
									   rsSelect.getInt("SUMOCS") + " , " +
									   "Case when '"+    rsSelect.getString("ISBOM") + "' like 'null' then '' else  '" +    rsSelect.getString("ISBOM")  + "' end , " +
								       rsSelect.getInt("M_PRODUCTI_ID") + " , " +
								       "Case when '"+  	   rsSelect.getString("PRODUCVALUEI") + "' like 'null' then '' else  '" +    rsSelect.getString("PRODUCVALUEI")  + "' end , " +
									   rsSelect.getInt("QTYONHANDI") + " , " +
									   rsSelect.getInt("QTYAVIABLEI") + " , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED1I")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED1I")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED1I")+ "','yyyy-mm-dd') end , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED2I")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED2I")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED2I")+ "','yyyy-mm-dd') end , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED3I")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED3I")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED3I")+ "','yyyy-mm-dd') end , " +
									   "case when '"+     rsSelect.getDate("DATEPROMISED4I")+ "' like 'null' then " +		 rsSelect.getDate("DATEPROMISED4I")   +" else  TO_DATE('"+   rsSelect.getDate("DATEPROMISED4I")+ "','yyyy-mm-dd') end , " +
									  rsSelect.getInt("QTYOC1I") + " , " +
									  rsSelect.getInt("QTYOC2I") + " , " +
									  rsSelect.getInt("QTYOC3I") + " , " +
									  rsSelect.getInt("QTYOC4I") + " , " +
									  rsSelect.getInt("ORDENC1I") + " , " +
									  rsSelect.getInt("ORDENC2I") + " , " +
									  rsSelect.getInt("ORDENC3I") + " , " +
									  rsSelect.getInt("ORDENC4I") + " , " +
									  rsSelect.getInt("QTYONHANDT") + " , " +
									  rsSelect.getInt("QTYRESERVEDI") + " , " +
									  rsSelect.getInt("TOTALOCI") + " , " +
									  rsSelect.getInt("VTA2015") + " , " +
									  rsSelect.getInt("VTA2018") + " , " +
									  rsSelect.getInt("QTYPRODUCTO") + " , " +
									  rsSelect.getInt("QTYINSUMO") + " , " +
									  rsSelect.getInt("QTYPROTPROTINS") + " , " +
									  rsSelect.getInt("COSTPRICE") + " , " +
									  rsSelect.getInt("NEWDOLAR") + " , " +
									  rsSelect.getInt("PRICELIST") + " , " +
									  rsSelect.getInt("PRICELIQ") + " , " +
									  rsSelect.getInt("NEWCOST") + " , " +
									  rsSelect.getInt("VTA2017") + " , " +
									  rsSelect.getInt("ENERO2017") + " , " +
									  rsSelect.getInt("FEBRERO2017") + " , " +
									  rsSelect.getInt("MARZO2017") + " , " +
									  rsSelect.getInt("ABRIL2017") + " , " +
									  rsSelect.getInt("MAYO2017") + " , " +
									  rsSelect.getInt("JUNIO2017") + " , " +
									  rsSelect.getInt("JULIO2017") + " , " +
									  rsSelect.getInt("AGOSTO2017") + " , " +
									  rsSelect.getInt("SEPTIEMBRE2017") + " , " +
									  rsSelect.getInt("OCTUBRE2017") + " , " +
									  rsSelect.getInt("NOVIEMBRE2017") + " , " +
									  rsSelect.getInt("DICIEMBRE2017") + " , " +
									  "Case when '"+  	  rsSelect.getString("MEASURE") + "' like 'null' then '' else  '" +    rsSelect.getString("MEASURE")   + "' end , " +
									  "Case when '"+ 	  rsSelect.getString("COLOR") + "' like 'null' then '' else  '" +    rsSelect.getString("COLOR")  + "' end , " +
									  "Case when '"+  	  rsSelect.getString("DESIGN") + "' like 'null' then '' else  '" +    rsSelect.getString("DESIGN")  + "' end , " +
									    rsSelect.getInt("ENERO2016") + " , " +
									  rsSelect.getInt("FEBRERO2016") + " , " +
									  rsSelect.getInt("MARZO2016") + " , " +
									  rsSelect.getInt("ABRIL2016") + " , " +
									  rsSelect.getInt("MAYO2016") + " , " +
									  rsSelect.getInt("JUNIO2016") + " , " +
									  rsSelect.getInt("JULIO2016") + " , " +
									  rsSelect.getInt("AGOSTO2016") + " , " +
									  rsSelect.getInt("SEPTIEMBRE2016") + " , " +
									  rsSelect.getInt("OCTUBRE2016") + " , " +
									  rsSelect.getInt("NOVIEMBRE2016") + " , " +
									  rsSelect.getInt("DICIEMBRE2016") + " , " +
									  rsSelect.getInt("VTA2016") + " , " +
									  "Case when '"+   rsSelect.getString("CLASIFICACION")  + "' like 'null' then '' else  '" +    rsSelect.getString("CLASIFICACION")    + "' end , " +
									  "Case when '"+	  rsSelect.getString("TIPOVENTA")  + "' like 'null' then '' else  '" +    rsSelect.getString("TIPOVENTA")   + "' end , " +
									  rsSelect.getInt("SHELFDEPTH") + " , " +
									  rsSelect.getInt("SHELFHEIGHT") + " , " +
									  rsSelect.getInt("SHELFWIDTH") + " , " +
									  rsSelect.getInt("M3") + " , " +
									  rsSelect.getInt("OCINOUT1") + " , " +
									  rsSelect.getInt("OCINOUT2") + " , " +
									  rsSelect.getInt("OCINOUT3") + " , " +
									  rsSelect.getInt("OCINOUT4") + " , " +
									  rsSelect.getInt("OCINOUT5") + " , " +
									  "Case when '"+ 	  rsSelect.getString("VIGENCIA") + "' like 'null' then '' else  '" +    rsSelect.getString("VIGENCIA")   + "' end ) ";
							
										PreparedStatement pstmt2 = null;
										//ResultSet rs2 = null;
										try{
											System.out.print(sql2);
											pstmt2 = DB.prepareStatement (sql2, get_TrxName());
											pstmt2.executeUpdate();
											/*rs2 = pstmt.executeQuery();			
//											if (rs2.next())
//											{
//												return "Insertado";
//											}
//											
//											rs2.close();*/
											pstmt2.close();
											}
										catch (Exception e)
										{
											log.log(Level.SEVERE, e.getMessage(), e);
										}
//							/*
//							 * 
//							 * While Select
//							 * 
//							 */
//						
							}
					}
					catch (Exception e)
					{
						log.log(Level.SEVERE, e.getMessage(), e);
					}
				
				} 
				rs.close();
				pstmt.close();
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		
	
		return "";
	}	//	doIt
	
}	//	OrderOpen
