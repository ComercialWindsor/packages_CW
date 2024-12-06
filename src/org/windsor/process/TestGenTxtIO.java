/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is Compiere ERP & CRM Smart Business Solution. The Initial
 * Developer of the Original Code is Jorg Janke. Portions created by Jorg Janke
 * are Copyright (C) 1999-2005 Jorg Janke.
 * All parts are Copyright (C) 1999-2005 ComPiere, Inc.  All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.windsor.process;

import org.compiere.process.SvrProcess;

/**
 *	Posting Documents
 *	
 *  @author Italo Niñoles
 *  @version $Id: PostDocs.java,v 1.12 2008/05/31 05:42:02 jjanke Exp $
 */
public class TestGenTxtIO extends SvrProcess
{

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{

		
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		addLog("Archivos generados");
		
		return "Archivos generados";
		
	}	//	doIt
	
	
}	//	PostDocs
