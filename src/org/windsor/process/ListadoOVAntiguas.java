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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


//import org.compiere.model.X_M_InOut;
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
public class ListadoOVAntiguas extends SvrProcess
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
			;//	p_order=para[i].getParameterAsInt();
				
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
	
		 String destinatario =  "controldespachos@comercialwindsor.cl"; //A quien le quieres escribir.
		    String asunto = "Notas y despachos sin facturacion";
		   
		    
		    String sql = "Select 'Nota: ' ||  o.documentno as Nota, "+
		    		"'Fecha: ' || to_char(o.DateAcct,'dd-mm-yyyy') as Fecha, "+
		    		"'Cliente: ' || bp.Name As Cliente, "+
		    		"'Vendedor: ' || u.name As Vendedor, "+
		    		"'Neto: ' || o.totallines as Neto "+
		    		"From C_order o  "+
		    		"inner join C_bpartner bp on (o.c_bpartner_ID=bp.c_Bpartner_ID) "+
		    		"left outer join ad_user u on (o.salesRep_ID=u.ad_user_ID) "+
		    		"where o.DateAcct between '01-01-2020' and sysdate-30 "+
		    		"and o.DocStatus='CO' "+
		    		"and o.totallines>0 "+
		    		"and o.c_Doctype_ID in (1000030, 1000568) "+
		    		"and not exists (select * from c_invoice i where i.c_order_id=o.c_order_ID and i.docstatus='CO') "+
		    		"and not exists (select * from m_inout io where io.m_inout_ID=o.m_inout_ID and io.docstatus='CO')";
		    		
		    		String cuerpo = ""; //"Esta es una prueba de correo...";
		    try
		    {
		    	PreparedStatement pstmt = null;
				pstmt = DB.prepareStatement(sql, get_TrxName());
				ResultSet rs = pstmt.executeQuery();
				
				
				
				
				if (rs.next())
				{
					cuerpo=rs.getString ("Nota")+"<br>";
					cuerpo=cuerpo + rs.getString ("Fecha")+"<br>";
					cuerpo=cuerpo + rs.getString ("Cliente")+"<br>";
					cuerpo=cuerpo + rs.getString ("Neto")+"<br>";
					cuerpo=cuerpo + rs.getString ("Vendedor")+"<br>";
					cuerpo=cuerpo + "<br>";
				}
		    }
	
		    catch (Exception e)
		    {
		
		    	log.log(Level.SEVERE, e.getMessage(), e);
		    }	
					
		   

		    enviarConGMail(destinatario, asunto, cuerpo);
		
		return "";		
				
			
	}	//	doIt
	
	
	private static void enviarConGMail(String destinatario, String asunto, String cuerpo) {
	    // Esto es lo que va delante de @gmail.com en tu cuenta de correo. Es el remitente también.
	  String remitente = "soporte@comercialwindsor.cl";  //Para la dirección nomcuenta@gmail.com

	    Properties props = System.getProperties();
	    props.put("mail.smtp.host", "smtp.gmail.com");  //El servidor SMTP de Google
	    props.put("mail.smtp.user", remitente);
	    props.put("mail.smtp.clave", "Cw9121100");    //La clave de la cuenta
	    props.put("mail.smtp.auth", "true");    //Usar autenticación mediante usuario y clave
	    props.put("mail.smtp.starttls.enable", "true"); //Para conectar de manera segura al servidor SMTP
	    props.put("mail.smtp.port", "587"); //El puerto SMTP seguro de Google

	    Session session = Session.getDefaultInstance(props);
	    MimeMessage message = new MimeMessage(session);

	    try {
	        message.setFrom(new InternetAddress(remitente));
	        message.addRecipients(Message.RecipientType.TO, destinatario);   //Se podrían añadir varios de la misma manera
	        message.setSubject(asunto);
	        message.setText(cuerpo, "ISO-8859-1","html");
	        Transport transport = session.getTransport("smtp");
	        transport.connect("smtp.gmail.com", remitente, "Cw9121100");
	        transport.sendMessage(message, message.getAllRecipients());
	        transport.close();
	    }
	    catch (MessagingException me) {
	        me.printStackTrace();   //Si se produce un error
	    }
	}
}	//	OrderOpen
