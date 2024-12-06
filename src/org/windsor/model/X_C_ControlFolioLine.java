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
/** Generated Model - DO NOT CHANGE */
package org.windsor.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
/** Generated Model for C_ControlFolioLine
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_C_ControlFolioLine extends PO implements I_C_ControlFolioLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20181110L;

    /** Standard Constructor */
    public X_C_ControlFolioLine (Properties ctx, int C_ControlFolioLine_ID, String trxName)
    {
      super (ctx, C_ControlFolioLine_ID, trxName);
      /** if (C_ControlFolioLine_ID == 0)
        {
			setC_ControlFolioLine_ID (0);
			setFOLIO (0);
        } */
    }

    /** Load Constructor */
    public X_C_ControlFolioLine (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_C_ControlFolioLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** C_ControlFolio_ID AD_Reference_ID=1000084 */
	public static final int C_CONTROLFOLIO_ID_AD_Reference_ID=1000084;
	/** Set C_ControlFolio_ID.
		@param C_ControlFolio_ID C_ControlFolio_ID	  */
	public void setC_ControlFolio_ID (String C_ControlFolio_ID)
	{

		set_Value (COLUMNNAME_C_ControlFolio_ID, C_ControlFolio_ID);
	}

	/** Get C_ControlFolio_ID.
		@return C_ControlFolio_ID	  */
	public String getC_ControlFolio_ID () 
	{
		return (String)get_Value(COLUMNNAME_C_ControlFolio_ID);
	}

	/** Set C_ControlFolioLine_ID.
		@param C_ControlFolioLine_ID C_ControlFolioLine_ID	  */
	public void setC_ControlFolioLine_ID (int C_ControlFolioLine_ID)
	{
		if (C_ControlFolioLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_ControlFolioLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_ControlFolioLine_ID, Integer.valueOf(C_ControlFolioLine_ID));
	}

	/** Get C_ControlFolioLine_ID.
		@return C_ControlFolioLine_ID	  */
	public int getC_ControlFolioLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ControlFolioLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_Invoice getC_Invoice() throws RuntimeException
    {
		return (I_C_Invoice)MTable.get(getCtx(), I_C_Invoice.Table_Name)
			.getPO(getC_Invoice_ID(), get_TrxName());	}

	/** Set Invoice.
		@param C_Invoice_ID 
		Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID)
	{
		if (C_Invoice_ID < 1) 
			set_Value (COLUMNNAME_C_Invoice_ID, null);
		else 
			set_Value (COLUMNNAME_C_Invoice_ID, Integer.valueOf(C_Invoice_ID));
	}

	/** Get Invoice.
		@return Invoice Identifier
	  */
	public int getC_Invoice_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Invoice_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_Invoice getC_Invoice2() throws RuntimeException
    {
		return (I_C_Invoice)MTable.get(getCtx(), I_C_Invoice.Table_Name)
			.getPO(getC_Invoice2_ID(), get_TrxName());	}

	/** Set C_Invoice2_ID.
		@param C_Invoice2_ID C_Invoice2_ID	  */
	public void setC_Invoice2_ID (int C_Invoice2_ID)
	{
		if (C_Invoice2_ID < 1) 
			set_Value (COLUMNNAME_C_Invoice2_ID, null);
		else 
			set_Value (COLUMNNAME_C_Invoice2_ID, Integer.valueOf(C_Invoice2_ID));
	}

	/** Get C_Invoice2_ID.
		@return C_Invoice2_ID	  */
	public int getC_Invoice2_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Invoice2_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Disponible.
		@param Disponible Disponible	  */
	public void setDisponible (boolean Disponible)
	{
		set_Value (COLUMNNAME_Disponible, Boolean.valueOf(Disponible));
	}

	/** Get Disponible.
		@return Disponible	  */
	public boolean isDisponible () 
	{
		Object oo = get_Value(COLUMNNAME_Disponible);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** DocStatus AD_Reference_ID=131 */
	public static final int DOCSTATUS_AD_Reference_ID=131;
	/** Drafted = DR */
	public static final String DOCSTATUS_Drafted = "DR";
	/** Completed = CO */
	public static final String DOCSTATUS_Completed = "CO";
	/** Approved = AP */
	public static final String DOCSTATUS_Approved = "AP";
	/** Not Approved = NA */
	public static final String DOCSTATUS_NotApproved = "NA";
	/** Voided = VO */
	public static final String DOCSTATUS_Voided = "VO";
	/** Invalid = IN */
	public static final String DOCSTATUS_Invalid = "IN";
	/** Reversed = RE */
	public static final String DOCSTATUS_Reversed = "RE";
	/** Closed = CL */
	public static final String DOCSTATUS_Closed = "CL";
	/** Unknown = ?? */
	public static final String DOCSTATUS_Unknown = "??";
	/** In Progress = IP */
	public static final String DOCSTATUS_InProgress = "IP";
	/** Waiting Payment = WP */
	public static final String DOCSTATUS_WaitingPayment = "WP";
	/** Waiting Confirmation = WC */
	public static final String DOCSTATUS_WaitingConfirmation = "WC";
	/** Shipped = SP */
	public static final String DOCSTATUS_Shipped = "SP";
	/** Set Document Status.
		@param DocStatus 
		The current status of the document
	  */
	public void setDocStatus (String DocStatus)
	{

		set_Value (COLUMNNAME_DocStatus, DocStatus);
	}

	/** Get Document Status.
		@return The current status of the document
	  */
	public String getDocStatus () 
	{
		return (String)get_Value(COLUMNNAME_DocStatus);
	}

	/** DOCSTATUS2 AD_Reference_ID=131 */
	public static final int DOCSTATUS2_AD_Reference_ID=131;
	/** Drafted = DR */
	public static final String DOCSTATUS2_Drafted = "DR";
	/** Completed = CO */
	public static final String DOCSTATUS2_Completed = "CO";
	/** Approved = AP */
	public static final String DOCSTATUS2_Approved = "AP";
	/** Not Approved = NA */
	public static final String DOCSTATUS2_NotApproved = "NA";
	/** Voided = VO */
	public static final String DOCSTATUS2_Voided = "VO";
	/** Invalid = IN */
	public static final String DOCSTATUS2_Invalid = "IN";
	/** Reversed = RE */
	public static final String DOCSTATUS2_Reversed = "RE";
	/** Closed = CL */
	public static final String DOCSTATUS2_Closed = "CL";
	/** Unknown = ?? */
	public static final String DOCSTATUS2_Unknown = "??";
	/** In Progress = IP */
	public static final String DOCSTATUS2_InProgress = "IP";
	/** Waiting Payment = WP */
	public static final String DOCSTATUS2_WaitingPayment = "WP";
	/** Waiting Confirmation = WC */
	public static final String DOCSTATUS2_WaitingConfirmation = "WC";
	/** Shipped = SP */
	public static final String DOCSTATUS2_Shipped = "SP";
	/** Set DOCSTATUS2.
		@param DOCSTATUS2 DOCSTATUS2	  */
	public void setDOCSTATUS2 (String DOCSTATUS2)
	{

		set_Value (COLUMNNAME_DOCSTATUS2, DOCSTATUS2);
	}

	/** Get DOCSTATUS2.
		@return DOCSTATUS2	  */
	public String getDOCSTATUS2 () 
	{
		return (String)get_Value(COLUMNNAME_DOCSTATUS2);
	}

	/** Set FOLIO.
		@param FOLIO FOLIO	  */
	public void setFOLIO (int FOLIO)
	{
		set_Value (COLUMNNAME_FOLIO, Integer.valueOf(FOLIO));
	}

	/** Get FOLIO.
		@return FOLIO	  */
	public int getFOLIO () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_FOLIO);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Line No.
		@param Line 
		Unique line for this document
	  */
	public void setLine (int Line)
	{
		set_Value (COLUMNNAME_Line, Integer.valueOf(Line));
	}

	/** Get Line No.
		@return Unique line for this document
	  */
	public int getLine () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}