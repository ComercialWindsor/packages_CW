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

//import org.compiere.model.MOrder;
//import org.compiere.model.X_M_InOut;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
//import org.compiere.util.DB;




import com.jcraft.jsch.*;


//import java.awt.*;
//import javax.swing.*;



//import org.compiere.util.Env;
 
/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class SshUpload extends SvrProcess
{
	//private Properties 		m_ctx;	


	      
	//private int p_order = 0;
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
		;//		
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
		
		
		System.out.println("preparing the host information for sftp.");
		Channel channel=null;
		
		 ChannelSftp channelSftp=null;
		// Boolean conexion=false;
				 String home="";
		try {
		  JSch jsch = new JSch();
		  Session session;
		  session = jsch.getSession("root", "192.168.10.148", 22);
		  session.setPassword("Fabrics...");
		  java.util.Properties config = new java.util.Properties();
		  config.put("StrictHostKeyChecking", "no");
		  session.setConfig(config);
		  session.connect();
		  log.info("Host connected.");
		  
		  channel =  session.openChannel("sftp");
		  
		  channel.connect();
		  log.info("sftp channel opened and connected.");
		  String remoteTempPath="Documentos";
		  channelSftp = (ChannelSftp) channel;
		/*  ArrayList<String> list = new ArrayList<String>();
		  Vector<LsEntry> entries = channelSftp.ls("*.*");
		  for (LsEntry entry : entries) {
		      
		          list.add(entry.getFilename());
		          log.fine(entry.getFilename());
		      }*/
		  
		  
		  boolean flag = true;
          try{
        	// conexion=channelSftp.isConnected();
        	 home=channelSftp.getHome();
              channelSftp.cd("Documentos/");
        //      home=channelSftp.getHome(); 
            /*  entries = channelSftp.ls("*.*");
    		  for (LsEntry entry : entries) {
    		      
    		          list.add(entry.getFilename());
    		          log.fine(entry.getFilename());
    		      }*/
              
          }catch(SftpException e){
              flag = false;
          }
         log.info("change working directory : /Documentos -->" + (flag?"SUCCESS":"FAIL") + " - " + home );
         if(!flag){
             try{
        // log.fine(   "Listado:"+	 channelSftp.ls("/"));
                 channelSftp.mkdir(remoteTempPath);
                 flag = true;
             }catch(SftpException ignored){}
         }
         log.info("make directory : " + remoteTempPath + "-->" + (flag?"SUCCESS":"FAIL"));
		//  channelSftp.cd("/Documentos");
		  String fileName="321194-1.txt";
		 // File f = new File(fileName);
		  fileName="321194-1.txt";
		  channelSftp.put("c:/v19.muro/EnternetAPPFull/AgenteV19/ARCHIVOS/EMIDTEIN/321194-1.txt", fileName);
		  log.info("File transfered successfully to host.");
		} 
		catch (Exception ex) {
		  channelSftp.exit();
		  System.out.println("sftp Channel exited.");
		  channel.disconnect();
			}
		
			
				
			
		
		return "Enviado";	
	}//	doIt

}	//	OrderOpen
