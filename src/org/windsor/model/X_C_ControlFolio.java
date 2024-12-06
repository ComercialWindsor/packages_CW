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
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.util.KeyNamePair;
import org.compiere.model.*;
/** Generated Model for C_ControlFolio
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_C_ControlFolio extends PO implements I_C_ControlFolio, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20181110L;

    /** Standard Constructor */
    public X_C_ControlFolio (Properties ctx, int C_ControlFolio_ID, String trxName)
    {
      super (ctx, C_ControlFolio_ID, trxName);
      /** if (C_ControlFolio_ID == 0)
        {
			setC_ControlFolio_ID (0);
			setC_DocTypeTarget_ID (0);
			setFechaCaducidad (new Timestamp( System.currentTimeMillis() ));
			setRangoFinal (0);
			setRangoInicial (0);
        } */
    }

    /** Load Constructor */
    public X_C_ControlFolio (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_C_ControlFolio[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_AD_User getAD_User() throws RuntimeException
    {
		return (I_AD_User)MTable.get(getCtx(), I_AD_User.Table_Name)
			.getPO(getAD_User_ID(), get_TrxName());	}

	/** Set User/Contact.
		@param AD_User_ID 
		User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User_ID (int AD_User_ID)
	{
		if (AD_User_ID < 1) 
			set_Value (COLUMNNAME_AD_User_ID, null);
		else 
			set_Value (COLUMNNAME_AD_User_ID, Integer.valueOf(AD_User_ID));
	}

	/** Get User/Contact.
		@return User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set C_ControlFolio_ID.
		@param C_ControlFolio_ID C_ControlFolio_ID	  */
	public void setC_ControlFolio_ID (int C_ControlFolio_ID)
	{
		if (C_ControlFolio_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_ControlFolio_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_ControlFolio_ID, Integer.valueOf(C_ControlFolio_ID));
	}

	/** Get C_ControlFolio_ID.
		@return C_ControlFolio_ID	  */
	public int getC_ControlFolio_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ControlFolio_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_DocType getC_DocTypeTarget() throws RuntimeException
    {
		return (I_C_DocType)MTable.get(getCtx(), I_C_DocType.Table_Name)
			.getPO(getC_DocTypeTarget_ID(), get_TrxName());	}

	/** Set Target Document Type.
		@param C_DocTypeTarget_ID 
		Target document type for conversing documents
	  */
	public void setC_DocTypeTarget_ID (int C_DocTypeTarget_ID)
	{
		if (C_DocTypeTarget_ID < 1) 
			set_Value (COLUMNNAME_C_DocTypeTarget_ID, null);
		else 
			set_Value (COLUMNNAME_C_DocTypeTarget_ID, Integer.valueOf(C_DocTypeTarget_ID));
	}

	/** Get Target Document Type.
		@return Target document type for conversing documents
	  */
	public int getC_DocTypeTarget_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocTypeTarget_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set FechaCaducidad.
		@param FechaCaducidad FechaCaducidad	  */
	public void setFechaCaducidad (Timestamp FechaCaducidad)
	{
		set_Value (COLUMNNAME_FechaCaducidad, FechaCaducidad);
	}

	/** Get FechaCaducidad.
		@return FechaCaducidad	  */
	public Timestamp getFechaCaducidad () 
	{
		return (Timestamp)get_Value(COLUMNNAME_FechaCaducidad);
	}

	/** Set GENERATECONTROL.
		@param GENERATECONTROL GENERATECONTROL	  */
	public void setGENERATECONTROL (String GENERATECONTROL)
	{
		set_Value (COLUMNNAME_GENERATECONTROL, GENERATECONTROL);
	}

	/** Get GENERATECONTROL.
		@return GENERATECONTROL	  */
	public String getGENERATECONTROL () 
	{
		return (String)get_Value(COLUMNNAME_GENERATECONTROL);
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }

	/** Set RangoFinal.
		@param RangoFinal RangoFinal	  */
	public void setRangoFinal (int RangoFinal)
	{
		set_Value (COLUMNNAME_RangoFinal, Integer.valueOf(RangoFinal));
	}

	/** Get RangoFinal.
		@return RangoFinal	  */
	public int getRangoFinal () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_RangoFinal);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set RangoInicial.
		@param RangoInicial RangoInicial	  */
	public void setRangoInicial (int RangoInicial)
	{
		set_Value (COLUMNNAME_RangoInicial, Integer.valueOf(RangoInicial));
	}

	/** Get RangoInicial.
		@return RangoInicial	  */
	public int getRangoInicial () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_RangoInicial);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set VALIDO.
		@param VALIDO VALIDO	  */
	public void setVALIDO (boolean VALIDO)
	{
		set_Value (COLUMNNAME_VALIDO, Boolean.valueOf(VALIDO));
	}

	/** Get VALIDO.
		@return VALIDO	  */
	public boolean isVALIDO () 
	{
		Object oo = get_Value(COLUMNNAME_VALIDO);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Search Key.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}