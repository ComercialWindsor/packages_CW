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
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for T_CuadroShopiFlow
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS
 */
public interface I_T_CuadroShopiFlow 
{

    /** TableName=T_CuadroShopiFlow */
    public static final String Table_Name = "T_CuadroShopiFlow";

    /** AD_Table_ID=1000823 */
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

    /** Column name DateAcct */
    public static final String COLUMNNAME_DateAcct = "DateAcct";

	/** Set Account Date.
	  * Accounting Date
	  */
	public void setDateAcct (Timestamp DateAcct);

	/** Get Account Date.
	  * Accounting Date
	  */
	public Timestamp getDateAcct();

    /** Column name FABRICMPNETO */
    public static final String COLUMNNAME_FABRICMPNETO = "FABRICMPNETO";

	/** Set FABRICMPNETO	  */
	public void setFABRICMPNETO (BigDecimal FABRICMPNETO);

	/** Get FABRICMPNETO	  */
	public BigDecimal getFABRICMPNETO();

    /** Column name FABRICMPQTY */
    public static final String COLUMNNAME_FABRICMPQTY = "FABRICMPQTY";

	/** Set FABRICMPQTY	  */
	public void setFABRICMPQTY (BigDecimal FABRICMPQTY);

	/** Get FABRICMPQTY	  */
	public BigDecimal getFABRICMPQTY();

    /** Column name FABRICSDIFNETO */
    public static final String COLUMNNAME_FABRICSDIFNETO = "FABRICSDIFNETO";

	/** Set FABRICSDIFNETO	  */
	public void setFABRICSDIFNETO (BigDecimal FABRICSDIFNETO);

	/** Get FABRICSDIFNETO	  */
	public BigDecimal getFABRICSDIFNETO();

    /** Column name FABRICSDIFQTY */
    public static final String COLUMNNAME_FABRICSDIFQTY = "FABRICSDIFQTY";

	/** Set FABRICSDIFQTY	  */
	public void setFABRICSDIFQTY (BigDecimal FABRICSDIFQTY);

	/** Get FABRICSDIFQTY	  */
	public BigDecimal getFABRICSDIFQTY();

    /** Column name FABRICSFLOWNETO */
    public static final String COLUMNNAME_FABRICSFLOWNETO = "FABRICSFLOWNETO";

	/** Set FABRICSFLOWNETO	  */
	public void setFABRICSFLOWNETO (BigDecimal FABRICSFLOWNETO);

	/** Get FABRICSFLOWNETO	  */
	public BigDecimal getFABRICSFLOWNETO();

    /** Column name FABRICSFLOWQTY */
    public static final String COLUMNNAME_FABRICSFLOWQTY = "FABRICSFLOWQTY";

	/** Set FABRICSFLOWQTY	  */
	public void setFABRICSFLOWQTY (BigDecimal FABRICSFLOWQTY);

	/** Get FABRICSFLOWQTY	  */
	public BigDecimal getFABRICSFLOWQTY();

    /** Column name FABRICSHOPNETO */
    public static final String COLUMNNAME_FABRICSHOPNETO = "FABRICSHOPNETO";

	/** Set FABRICSHOPNETO	  */
	public void setFABRICSHOPNETO (BigDecimal FABRICSHOPNETO);

	/** Get FABRICSHOPNETO	  */
	public BigDecimal getFABRICSHOPNETO();

    /** Column name FABRICSHOPQTY */
    public static final String COLUMNNAME_FABRICSHOPQTY = "FABRICSHOPQTY";

	/** Set FABRICSHOPQTY	  */
	public void setFABRICSHOPQTY (BigDecimal FABRICSHOPQTY);

	/** Get FABRICSHOPQTY	  */
	public BigDecimal getFABRICSHOPQTY();

    /** Column name FABRICSVPNETO */
    public static final String COLUMNNAME_FABRICSVPNETO = "FABRICSVPNETO";

	/** Set FABRICSVPNETO	  */
	public void setFABRICSVPNETO (BigDecimal FABRICSVPNETO);

	/** Get FABRICSVPNETO	  */
	public BigDecimal getFABRICSVPNETO();

    /** Column name FABRICSVPQTY */
    public static final String COLUMNNAME_FABRICSVPQTY = "FABRICSVPQTY";

	/** Set FABRICSVPQTY	  */
	public void setFABRICSVPQTY (BigDecimal FABRICSVPQTY);

	/** Get FABRICSVPQTY	  */
	public BigDecimal getFABRICSVPQTY();

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

    /** Column name MASHINIDIFNETO */
    public static final String COLUMNNAME_MASHINIDIFNETO = "MASHINIDIFNETO";

	/** Set MASHINIDIFNETO	  */
	public void setMASHINIDIFNETO (BigDecimal MASHINIDIFNETO);

	/** Get MASHINIDIFNETO	  */
	public BigDecimal getMASHINIDIFNETO();

    /** Column name MASHINIDIFQTY */
    public static final String COLUMNNAME_MASHINIDIFQTY = "MASHINIDIFQTY";

	/** Set MASHINIDIFQTY	  */
	public void setMASHINIDIFQTY (BigDecimal MASHINIDIFQTY);

	/** Get MASHINIDIFQTY	  */
	public BigDecimal getMASHINIDIFQTY();

    /** Column name MASHINIFLOWNETO */
    public static final String COLUMNNAME_MASHINIFLOWNETO = "MASHINIFLOWNETO";

	/** Set MASHINIFLOWNETO	  */
	public void setMASHINIFLOWNETO (BigDecimal MASHINIFLOWNETO);

	/** Get MASHINIFLOWNETO	  */
	public BigDecimal getMASHINIFLOWNETO();

    /** Column name MASHINIFLOWQTY */
    public static final String COLUMNNAME_MASHINIFLOWQTY = "MASHINIFLOWQTY";

	/** Set MASHINIFLOWQTY	  */
	public void setMASHINIFLOWQTY (BigDecimal MASHINIFLOWQTY);

	/** Get MASHINIFLOWQTY	  */
	public BigDecimal getMASHINIFLOWQTY();

    /** Column name MASHINIMPNETO */
    public static final String COLUMNNAME_MASHINIMPNETO = "MASHINIMPNETO";

	/** Set MASHINIMPNETO	  */
	public void setMASHINIMPNETO (BigDecimal MASHINIMPNETO);

	/** Get MASHINIMPNETO	  */
	public BigDecimal getMASHINIMPNETO();

    /** Column name MASHINIMPQTY */
    public static final String COLUMNNAME_MASHINIMPQTY = "MASHINIMPQTY";

	/** Set MASHINIMPQTY	  */
	public void setMASHINIMPQTY (BigDecimal MASHINIMPQTY);

	/** Get MASHINIMPQTY	  */
	public BigDecimal getMASHINIMPQTY();

    /** Column name MASHINISHOPNETO */
    public static final String COLUMNNAME_MASHINISHOPNETO = "MASHINISHOPNETO";

	/** Set MASHINISHOPNETO	  */
	public void setMASHINISHOPNETO (BigDecimal MASHINISHOPNETO);

	/** Get MASHINISHOPNETO	  */
	public BigDecimal getMASHINISHOPNETO();

    /** Column name MASHINISHOPQTY */
    public static final String COLUMNNAME_MASHINISHOPQTY = "MASHINISHOPQTY";

	/** Set MASHINISHOPQTY	  */
	public void setMASHINISHOPQTY (BigDecimal MASHINISHOPQTY);

	/** Get MASHINISHOPQTY	  */
	public BigDecimal getMASHINISHOPQTY();

    /** Column name MASHINIVPNETO */
    public static final String COLUMNNAME_MASHINIVPNETO = "MASHINIVPNETO";

	/** Set MASHINIVPNETO	  */
	public void setMASHINIVPNETO (BigDecimal MASHINIVPNETO);

	/** Get MASHINIVPNETO	  */
	public BigDecimal getMASHINIVPNETO();

    /** Column name MASHINIVPQTY */
    public static final String COLUMNNAME_MASHINIVPQTY = "MASHINIVPQTY";

	/** Set MASHINIVPQTY	  */
	public void setMASHINIVPQTY (BigDecimal MASHINIVPQTY);

	/** Get MASHINIVPQTY	  */
	public BigDecimal getMASHINIVPQTY();

    /** Column name T_CuadroShopiFlow_ID */
    public static final String COLUMNNAME_T_CuadroShopiFlow_ID = "T_CuadroShopiFlow_ID";

	/** Set T_CuadroShopiFlow_ID	  */
	public void setT_CuadroShopiFlow_ID (int T_CuadroShopiFlow_ID);

	/** Get T_CuadroShopiFlow_ID	  */
	public int getT_CuadroShopiFlow_ID();

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

    /** Column name WINDSORDIFNETO */
    public static final String COLUMNNAME_WINDSORDIFNETO = "WINDSORDIFNETO";

	/** Set WINDSORDIFNETO	  */
	public void setWINDSORDIFNETO (BigDecimal WINDSORDIFNETO);

	/** Get WINDSORDIFNETO	  */
	public BigDecimal getWINDSORDIFNETO();

    /** Column name WINDSORDIFQTY */
    public static final String COLUMNNAME_WINDSORDIFQTY = "WINDSORDIFQTY";

	/** Set WINDSORDIFQTY	  */
	public void setWINDSORDIFQTY (BigDecimal WINDSORDIFQTY);

	/** Get WINDSORDIFQTY	  */
	public BigDecimal getWINDSORDIFQTY();

    /** Column name WINDSORFLOWNETO */
    public static final String COLUMNNAME_WINDSORFLOWNETO = "WINDSORFLOWNETO";

	/** Set WINDSORFLOWNETO	  */
	public void setWINDSORFLOWNETO (BigDecimal WINDSORFLOWNETO);

	/** Get WINDSORFLOWNETO	  */
	public BigDecimal getWINDSORFLOWNETO();

    /** Column name WINDSORFLOWQTY */
    public static final String COLUMNNAME_WINDSORFLOWQTY = "WINDSORFLOWQTY";

	/** Set WINDSORFLOWQTY	  */
	public void setWINDSORFLOWQTY (BigDecimal WINDSORFLOWQTY);

	/** Get WINDSORFLOWQTY	  */
	public BigDecimal getWINDSORFLOWQTY();

    /** Column name WINDSORMPNETO */
    public static final String COLUMNNAME_WINDSORMPNETO = "WINDSORMPNETO";

	/** Set WINDSORMPNETO	  */
	public void setWINDSORMPNETO (BigDecimal WINDSORMPNETO);

	/** Get WINDSORMPNETO	  */
	public BigDecimal getWINDSORMPNETO();

    /** Column name WINDSORMPQTY */
    public static final String COLUMNNAME_WINDSORMPQTY = "WINDSORMPQTY";

	/** Set WINDSORMPQTY	  */
	public void setWINDSORMPQTY (BigDecimal WINDSORMPQTY);

	/** Get WINDSORMPQTY	  */
	public BigDecimal getWINDSORMPQTY();

    /** Column name WINDSORSHOPNETO */
    public static final String COLUMNNAME_WINDSORSHOPNETO = "WINDSORSHOPNETO";

	/** Set WINDSORSHOPNETO	  */
	public void setWINDSORSHOPNETO (BigDecimal WINDSORSHOPNETO);

	/** Get WINDSORSHOPNETO	  */
	public BigDecimal getWINDSORSHOPNETO();

    /** Column name WINDSORSHOPQTY */
    public static final String COLUMNNAME_WINDSORSHOPQTY = "WINDSORSHOPQTY";

	/** Set WINDSORSHOPQTY	  */
	public void setWINDSORSHOPQTY (BigDecimal WINDSORSHOPQTY);

	/** Get WINDSORSHOPQTY	  */
	public BigDecimal getWINDSORSHOPQTY();

    /** Column name WINDSORVPNETO */
    public static final String COLUMNNAME_WINDSORVPNETO = "WINDSORVPNETO";

	/** Set WINDSORVPNETO	  */
	public void setWINDSORVPNETO (BigDecimal WINDSORVPNETO);

	/** Get WINDSORVPNETO	  */
	public BigDecimal getWINDSORVPNETO();

    /** Column name WINDSORVPQTY */
    public static final String COLUMNNAME_WINDSORVPQTY = "WINDSORVPQTY";

	/** Set WINDSORVPQTY	  */
	public void setWINDSORVPQTY (BigDecimal WINDSORVPQTY);

	/** Get WINDSORVPQTY	  */
	public BigDecimal getWINDSORVPQTY();
}
