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
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Model for C_OrderB2C
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_C_OrderB2C extends PO implements I_C_OrderB2C, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190902L;

    /** Standard Constructor */
    public X_C_OrderB2C (Properties ctx, int C_OrderB2C_ID, String trxName)
    {
      super (ctx, C_OrderB2C_ID, trxName);
      /** if (C_OrderB2C_ID == 0)
        {
			setC_OrderB2C_ID (0);
        } */
    }

    /** Load Constructor */
    public X_C_OrderB2C (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_C_OrderB2C[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Address 1.
		@param Address1 
		Address line 1 for this location
	  */
	public void setAddress1 (String Address1)
	{
		set_Value (COLUMNNAME_Address1, Address1);
	}

	/** Get Address 1.
		@return Address line 1 for this location
	  */
	public String getAddress1 () 
	{
		return (String)get_Value(COLUMNNAME_Address1);
	}

	/** Set Business Partner Key.
		@param BPartnerValue 
		Key of the Business Partner
	  */
	public void setBPartnerValue (String BPartnerValue)
	{
		set_Value (COLUMNNAME_BPartnerValue, BPartnerValue);
	}

	/** Get Business Partner Key.
		@return Key of the Business Partner
	  */
	public String getBPartnerValue () 
	{
		return (String)get_Value(COLUMNNAME_BPartnerValue);
	}

	public I_C_BPartner getC_BPartner() throws RuntimeException
    {
		return (I_C_BPartner)MTable.get(getCtx(), I_C_BPartner.Table_Name)
			.getPO(getC_BPartner_ID(), get_TrxName());	}

	/** Set Business Partner .
		@param C_BPartner_ID 
		Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1) 
			set_Value (COLUMNNAME_C_BPartner_ID, null);
		else 
			set_Value (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner .
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_BPartner_Location getC_BPartner_Location() throws RuntimeException
    {
		return (I_C_BPartner_Location)MTable.get(getCtx(), I_C_BPartner_Location.Table_Name)
			.getPO(getC_BPartner_Location_ID(), get_TrxName());	}

	/** Set Partner Location.
		@param C_BPartner_Location_ID 
		Identifies the (ship to) address for this Business Partner
	  */
	public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
	{
		if (C_BPartner_Location_ID < 1) 
			set_Value (COLUMNNAME_C_BPartner_Location_ID, null);
		else 
			set_Value (COLUMNNAME_C_BPartner_Location_ID, Integer.valueOf(C_BPartner_Location_ID));
	}

	/** Get Partner Location.
		@return Identifies the (ship to) address for this Business Partner
	  */
	public int getC_BPartner_Location_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_Location_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_BPartner_SubTienda getC_BPartner_SubTienda() throws RuntimeException
    {
		return (I_C_BPartner_SubTienda)MTable.get(getCtx(), I_C_BPartner_SubTienda.Table_Name)
			.getPO(getC_BPartner_SubTienda_ID(), get_TrxName());	}

	/** Set C_BPartner_SubTienda_ID.
		@param C_BPartner_SubTienda_ID C_BPartner_SubTienda_ID	  */
	public void setC_BPartner_SubTienda_ID (int C_BPartner_SubTienda_ID)
	{
		if (C_BPartner_SubTienda_ID < 1) 
			set_Value (COLUMNNAME_C_BPartner_SubTienda_ID, null);
		else 
			set_Value (COLUMNNAME_C_BPartner_SubTienda_ID, Integer.valueOf(C_BPartner_SubTienda_ID));
	}

	/** Get C_BPartner_SubTienda_ID.
		@return C_BPartner_SubTienda_ID	  */
	public int getC_BPartner_SubTienda_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_SubTienda_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set C_OrderB2C_ID.
		@param C_OrderB2C_ID C_OrderB2C_ID	  */
	public void setC_OrderB2C_ID (int C_OrderB2C_ID)
	{
		if (C_OrderB2C_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_OrderB2C_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_OrderB2C_ID, Integer.valueOf(C_OrderB2C_ID));
	}

	/** Get C_OrderB2C_ID.
		@return C_OrderB2C_ID	  */
	public int getC_OrderB2C_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_OrderB2C_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Account Date.
		@param DateAcct 
		Accounting Date
	  */
	public void setDateAcct (Timestamp DateAcct)
	{
		set_Value (COLUMNNAME_DateAcct, DateAcct);
	}

	/** Get Account Date.
		@return Accounting Date
	  */
	public Timestamp getDateAcct () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateAcct);
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Document No.
		@param DocumentNo 
		Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo)
	{
		set_Value (COLUMNNAME_DocumentNo, DocumentNo);
	}

	/** Get Document No.
		@return Document sequence number of the document
	  */
	public String getDocumentNo () 
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getDocumentNo());
    }

	/** Set Error Msg.
		@param ErrorMsg Error Msg	  */
	public void setErrorMsg (String ErrorMsg)
	{
		set_Value (COLUMNNAME_ErrorMsg, ErrorMsg);
	}

	/** Get Error Msg.
		@return Error Msg	  */
	public String getErrorMsg () 
	{
		return (String)get_Value(COLUMNNAME_ErrorMsg);
	}

	/** FormaCompra AD_Reference_ID=1000082 */
	public static final int FORMACOMPRA_AD_Reference_ID=1000082;
	/** En Verde = En Verde */
	public static final String FORMACOMPRA_EnVerde = "En Verde";
	/** Cyber = Cyber */
	public static final String FORMACOMPRA_Cyber = "Cyber";
	/** Tienda = Tienda */
	public static final String FORMACOMPRA_Tienda = "Tienda";
	/** Marketplace = Marketplace */
	public static final String FORMACOMPRA_Marketplace = "Marketplace";
	/** Set FormaCompra.
		@param FormaCompra FormaCompra	  */
	public void setFormaCompra (String FormaCompra)
	{

		set_Value (COLUMNNAME_FormaCompra, FormaCompra);
	}

	/** Get FormaCompra.
		@return FormaCompra	  */
	public String getFormaCompra () 
	{
		return (String)get_Value(COLUMNNAME_FormaCompra);
	}

	/** Set GENERARORDEN.
		@param GENERARORDEN GENERARORDEN	  */
	public void setGENERARORDEN (String GENERARORDEN)
	{
		set_Value (COLUMNNAME_GENERARORDEN, GENERARORDEN);
	}

	/** Get GENERARORDEN.
		@return GENERARORDEN	  */
	public String getGENERARORDEN () 
	{
		return (String)get_Value(COLUMNNAME_GENERARORDEN);
	}

	/** MedioCompra AD_Reference_ID=1000081 */
	public static final int MEDIOCOMPRA_AD_Reference_ID=1000081;
	/** Tiendas = Tiendas */
	public static final String MEDIOCOMPRA_Tiendas = "Tiendas";
	/** Internet = Internet */
	public static final String MEDIOCOMPRA_Internet = "Internet";
	/** Set MedioCompra.
		@param MedioCompra MedioCompra	  */
	public void setMedioCompra (String MedioCompra)
	{

		set_Value (COLUMNNAME_MedioCompra, MedioCompra);
	}

	/** Get MedioCompra.
		@return MedioCompra	  */
	public String getMedioCompra () 
	{
		return (String)get_Value(COLUMNNAME_MedioCompra);
	}

	public I_M_PriceList getM_PriceList() throws RuntimeException
    {
		return (I_M_PriceList)MTable.get(getCtx(), I_M_PriceList.Table_Name)
			.getPO(getM_PriceList_ID(), get_TrxName());	}

	/** Set Price List.
		@param M_PriceList_ID 
		Unique identifier of a Price List
	  */
	public void setM_PriceList_ID (int M_PriceList_ID)
	{
		if (M_PriceList_ID < 1) 
			set_Value (COLUMNNAME_M_PriceList_ID, null);
		else 
			set_Value (COLUMNNAME_M_PriceList_ID, Integer.valueOf(M_PriceList_ID));
	}

	/** Get Price List.
		@return Unique identifier of a Price List
	  */
	public int getM_PriceList_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_PriceList_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_M_Warehouse getM_Warehouse() throws RuntimeException
    {
		return (I_M_Warehouse)MTable.get(getCtx(), I_M_Warehouse.Table_Name)
			.getPO(getM_Warehouse_ID(), get_TrxName());	}

	/** Set Warehouse.
		@param M_Warehouse_ID 
		Storage Warehouse and Service Point
	  */
	public void setM_Warehouse_ID (int M_Warehouse_ID)
	{
		if (M_Warehouse_ID < 1) 
			set_Value (COLUMNNAME_M_Warehouse_ID, null);
		else 
			set_Value (COLUMNNAME_M_Warehouse_ID, Integer.valueOf(M_Warehouse_ID));
	}

	/** Get Warehouse.
		@return Storage Warehouse and Service Point
	  */
	public int getM_Warehouse_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Warehouse_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Order Reference.
		@param POReference 
		Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner
	  */
	public void setPOReference (String POReference)
	{
		set_Value (COLUMNNAME_POReference, POReference);
	}

	/** Get Order Reference.
		@return Transaction Reference Number (Sales Order, Purchase Order) of your Business Partner
	  */
	public String getPOReference () 
	{
		return (String)get_Value(COLUMNNAME_POReference);
	}

	/** Set Processed.
		@param Processed 
		The document has been processed
	  */
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Processed.
		@return The document has been processed
	  */
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	public I_AD_User getSalesRep() throws RuntimeException
    {
		return (I_AD_User)MTable.get(getCtx(), I_AD_User.Table_Name)
			.getPO(getSalesRep_ID(), get_TrxName());	}

	/** Set Sales Representative.
		@param SalesRep_ID 
		Sales Representative or Company Agent
	  */
	public void setSalesRep_ID (int SalesRep_ID)
	{
		if (SalesRep_ID < 1) 
			set_Value (COLUMNNAME_SalesRep_ID, null);
		else 
			set_Value (COLUMNNAME_SalesRep_ID, Integer.valueOf(SalesRep_ID));
	}

	/** Get Sales Representative.
		@return Sales Representative or Company Agent
	  */
	public int getSalesRep_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SalesRep_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set VentaEnVerde.
		@param VentaEnVerde VentaEnVerde	  */
	public void setVentaEnVerde (boolean VentaEnVerde)
	{
		set_Value (COLUMNNAME_VentaEnVerde, Boolean.valueOf(VentaEnVerde));
	}

	/** Get VentaEnVerde.
		@return VentaEnVerde	  */
	public boolean isVentaEnVerde () 
	{
		Object oo = get_Value(COLUMNNAME_VentaEnVerde);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
}