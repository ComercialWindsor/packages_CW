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


//import ftp
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
  
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


//import org.compiere.util.Env;
 
/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class FtpUpload extends SvrProcess
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
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws AdempiereSystemError, SQLException
	{
		/*********/
	
		FTPClient ftpClient = new FTPClient();
        
        try {
  
            ftpClient.connect("186.103.166.99", 21);
            ftpClient.login("catalogo_mashini", "cmashini");
            ftpClient.enterLocalPassiveMode();
  
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
  
            File LocalFile = new File("C:/v19.muro/EnternetAPPFull/AgenteV19/ARCHIVOS/EMIDTEIN/321194-1.txt");
  
            String remoteFile = "/321194-1.txt";
            InputStream inputStream = new FileInputStream(LocalFile);
  
            log.fine("Start uploading first file");
            boolean done = ftpClient.storeFile(remoteFile, inputStream);
            inputStream.close();
            
            if (done) {
                log.info("The first file is uploaded using FTP successfully."+p_order);
            }
             
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
             
        } finally {
             
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
                 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
             
        } 
		
		return "Enviado";		
				
			
	}	//	doIt
	
}	//	OrderOpen
