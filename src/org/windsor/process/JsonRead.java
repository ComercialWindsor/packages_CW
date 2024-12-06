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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.*;

import org.apache.xml.security.utils.Base64;
//import org.apache.xml.security.utils.Base64;
//import org.compiere.model.MOrder;
//import org.compiere.model.X_M_InOut;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;








//import sun.net.www.protocol.http.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import java.io.*;

/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class JsonRead extends SvrProcess
{
	//private Properties 		m_ctx;	


	      
	private int p_order = 0;
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
				p_order=para[i].getParameterAsInt();
				
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
	 * @throws IOException 
	 *  @throws Exception if not successful
	 */

	protected String doIt() throws AdempiereSystemError, SQLException, IOException
	{
		/*********/
		 String uri ="https://@mashini-chile.myshopify.com/admin/api/2020-10/orders.json?financial_status=paid";
		 String content = "grant_type=password&username=" + "706defa8fddf340a187b6aaf29422820" + "&password=" +  "09577478ae998d6b3d779e5d56dadb32";
       /*  URL url = new URL(uri);
         HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Authorization", "Basic "+ Base64.encode(("706defa8fddf340a187b6aaf29422820" + ":" + "09577478ae998d6b3d779e5d56dadb32").getBytes()));
         connection.setRequestProperty("content-type", "text/xml; charset=UTF-8");

//       Aqui me gustaria mandarle el cuerpo pero no es correcto
       //  connection.setRequestProperty("body", archivo);
        
         connection.setConnectTimeout(10000);
         connection.connect();
        
         System.out.println("Codigo : " + connection.getResponseCode());*/
		 BufferedReader reader = null;
         URL url = new URL(uri);//your url i.e fetch data from .
         try {
         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         String auth = "706defa8fddf340a187b6aaf29422820" + ":" + "09577478ae998d6b3d779e5d56dadb32";
        // byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(UTF8));
         conn.setDoOutput(true);
         conn.setRequestProperty( "Authorization", "Bearer " +auth);   //.setRequestProperty("Accept", "application/json");
         conn.setConnectTimeout(10000);
         conn.setReadTimeout(10000);
         /*if (conn.getResponseCode() != 200) {
             throw new RuntimeException("Failed : HTTP Error code : "
                     + conn.getResponseCode());
         }*/
         /*InputStreamReader in = new InputStreamReader(conn.getInputStream());
         BufferedReader br = new BufferedReader(in);
         String output;
         while ((output = br.readLine()) != null) {
             System.out.println(output);
         }*/
         
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         conn.setRequestProperty("Accept", "application/json");
    //     log.info(" Con: "+conn.getResponseCode() );
    //     conn.connect();
         System.setProperty("https.protocols", "TLSv1.2");
         try{
        	 BufferedReader br = new BufferedReader(
           		  new InputStreamReader(conn.getInputStream(), "utf-8"));
        		    StringBuilder response = new StringBuilder();
        		    String responseLine = null;
        		    while ((responseLine = br.readLine()) != null) {
        		        response.append(responseLine.trim());
        		    }
        		    System.out.println(response.toString());
        		}catch (Exception e) {
        			 e.printStackTrace();
                } 
         
         int status = conn.getResponseCode();
         PrintStream os = new PrintStream(conn.getOutputStream());
         os.print(content);
         os.close();
        // conn.setRequestMethod("GET");
         reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String line = null;
         StringWriter out = new StringWriter(conn.getContentLength() > 0 ? conn.getContentLength() : 2048);
         while ((line = reader.readLine()) != null) {
             out.append(line);
         }
         String response = out.toString();
         System.out.println(response);
         conn.disconnect();
         System.out.println(response);
         } catch (Exception e) {

         } 
		return "Listo";		
				
			
	}	//	doIt
	
}	//	OrderOpen
