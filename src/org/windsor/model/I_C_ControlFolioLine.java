/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.windsor.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;
import org.compiere.model.*;

/** Generated Interface for C_ControlFolioLine
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS
 */
public interface I_C_ControlFolioLine 
{

    /** TableName=C_ControlFolioLine */
    public static final String Table_Name = "C_ControlFolioLine";

    /** AD_Table_ID=1000652 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name C_ControlFolio_ID */
    public static final String COLUMNNAME_C_ControlFolio_ID = "C_ControlFolio_ID";

	/** Set C_ControlFolio_ID	  */
	public void setC_ControlFolio_ID (String C_ControlFolio_ID);

	/** Get C_ControlFolio_ID	  */
	public String getC_ControlFolio_ID();

    /** Column name C_ControlFolioLine_ID */
    public static final String COLUMNNAME_C_ControlFolioLine_ID = "C_ControlFolioLine_ID";

	/** Set C_ControlFolioLine_ID	  */
	public void setC_ControlFolioLine_ID (int C_ControlFolioLine_ID);

	/** Get C_ControlFolioLine_ID	  */
	public int getC_ControlFolioLine_ID();

    /** Column name C_Invoice_ID */
    public static final String COLUMNNAME_C_Invoice_ID = "C_Invoice_ID";

	/** Set Invoice.
	  * Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID);

	/** Get Invoice.
	  * Invoice Identifier
	  */
	public int getC_Invoice_ID();

	public I_C_Invoice getC_Invoice() throws RuntimeException;

    /** Column name C_Invoice2_ID */
    public static final String COLUMNNAME_C_Invoice2_ID = "C_Invoice2_ID";

	/** Set C_Invoice2_ID	  */
	public void setC_Invoice2_ID (int C_Invoice2_ID);

	/** Get C_Invoice2_ID	  */
	public int getC_Invoice2_ID();

	public I_C_Invoice getC_Invoice2() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name Disponible */
    public static final String COLUMNNAME_Disponible = "Disponible";

	/** Set Disponible	  */
	public void setDisponible (boolean Disponible);

	/** Get Disponible	  */
	public boolean isDisponible();

    /** Column name DocStatus */
    public static final String COLUMNNAME_DocStatus = "DocStatus";

	/** Set Document Status.
	  * The current status of the document
	  */
	public void setDocStatus (String DocStatus);

	/** Get Document Status.
	  * The current status of the document
	  */
	public String getDocStatus();

    /** Column name DOCSTATUS2 */
    public static final String COLUMNNAME_DOCSTATUS2 = "DOCSTATUS2";

	/** Set DOCSTATUS2	  */
	public void setDOCSTATUS2 (String DOCSTATUS2);

	/** Get DOCSTATUS2	  */
	public String getDOCSTATUS2();

    /** Column name FOLIO */
    public static final String COLUMNNAME_FOLIO = "FOLIO";

	/** Set FOLIO	  */
	public void setFOLIO (int FOLIO);

	/** Get FOLIO	  */
	public int getFOLIO();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name Line */
    public static final String COLUMNNAME_Line = "Line";

	/** Set Line No.
	  * Unique line for this document
	  */
	public void setLine (int Line);

	/** Get Line No.
	  * Unique line for this document
	  */
	public int getLine();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();
}
