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
import java.sql.SQLException;
import java.util.logging.*;

import org.compiere.model.MOrder;
//import org.compiere.model.X_M_InOut;
import org.compiere.process.*;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.DB;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import org.compiere.util.Env;
 
/**
 *	Genera  Detalle de control de folios
 *	
 *  @author Guillermo Rojas
 *  @version $Id: CrearControlFiolioDetalle.java,v 1.2 2018/11/12 $
 */
public class EditXML extends SvrProcess
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
		Document document = null;
		 try {
		        //Cargamos el document del fichero XML existente
			 File file = new File ("c:/v19/EnternetAPPFull/AgenteV19/entsend.xml");
			 String file2="c:\\v19\\EnternetAPPFull\\AgenteV19\\entsend.xml";
		        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        DocumentBuilder db = dbf.newDocumentBuilder();
		        document = db.parse(file2); //("//c/v19/EnternetAPPFull/AgenteV19/entsend.xml"));
		        document.getDocumentElement().normalize();
		    } catch (ParserConfigurationException e) {
		        e.printStackTrace();
		    } catch (SAXException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }

		    //Obtenemos todas las habitaciones de la casa
		    NodeList listaNodos = document.getDocumentElement().getElementsByTagName("PARMS");
		    Node nodo;
		    for(int i=0; i<listaNodos.getLength(); i++){
		        nodo = listaNodos.item(i);

		        //Obtenemos una lista de todas las características de cada habitación
		        NodeList listaCaracteristicas = nodo.getChildNodes();
		        Node caracteristica;

		        for(int z=0; z<listaCaracteristicas.getLength();z++){
		            //Obtenemos cada característica individual
		            caracteristica = listaCaracteristicas.item(z);
		            //Si la característica es el color y su valor es azul la eliminamos
		            if(caracteristica.getNodeName().equals("Color") && caracteristica.getTextContent().equals("Azul")){
		                //El padre del nodo es Habitación y su padre Casa. Eliminamos Habitación de Casa.
		                caracteristica.getParentNode().getParentNode().removeChild(caracteristica.getParentNode());
		            }
		        }
		    }
		
		return "Listo";		
				
			
	}	//	doIt
	
}	//	OrderOpen
