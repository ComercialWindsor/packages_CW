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

/** Generated Model for C_RepoMuroTienda
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_C_RepoMuroTienda extends PO implements I_C_RepoMuroTienda, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200306L;

    /** Standard Constructor */
    public X_C_RepoMuroTienda (Properties ctx, int C_RepoMuroTienda_ID, String trxName)
    {
      super (ctx, C_RepoMuroTienda_ID, trxName);
      /** if (C_RepoMuroTienda_ID == 0)
        {
			setC_BPartner_Location_ID (0);
			setC_RepoMuro_ID (0);
			setC_RepoMuroTienda_ID (0);
        } */
    }

    /** Load Constructor */
    public X_C_RepoMuroTienda (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_C_RepoMuroTienda[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	public I_C_RepoMuro getC_RepoMuro() throws RuntimeException
    {
		return (I_C_RepoMuro)MTable.get(getCtx(), I_C_RepoMuro.Table_Name)
			.getPO(getC_RepoMuro_ID(), get_TrxName());	}

	/** Set C_RepoMuro_ID.
		@param C_RepoMuro_ID C_RepoMuro_ID	  */
	public void setC_RepoMuro_ID (int C_RepoMuro_ID)
	{
		if (C_RepoMuro_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_RepoMuro_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_RepoMuro_ID, Integer.valueOf(C_RepoMuro_ID));
	}

	/** Get C_RepoMuro_ID.
		@return C_RepoMuro_ID	  */
	public int getC_RepoMuro_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_RepoMuro_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set C_RepoMuroTienda_ID.
		@param C_RepoMuroTienda_ID C_RepoMuroTienda_ID	  */
	public void setC_RepoMuroTienda_ID (int C_RepoMuroTienda_ID)
	{
		if (C_RepoMuroTienda_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_RepoMuroTienda_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_RepoMuroTienda_ID, Integer.valueOf(C_RepoMuroTienda_ID));
	}

	/** Get C_RepoMuroTienda_ID.
		@return C_RepoMuroTienda_ID	  */
	public int getC_RepoMuroTienda_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_RepoMuroTienda_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Tienda AD_Reference_ID=1000074 */
	public static final int TIENDA_AD_Reference_ID=1000074;
	/** Alameda = Alameda */
	public static final String TIENDA_Alameda = "Alameda";
	/** Gral. Mackenna = Gral. Mackenna */
	public static final String TIENDA_GralMackenna = "Gral. Mackenna";
	/** Vivo Maipu = Vivo Maipu */
	public static final String TIENDA_VivoMaipu = "Vivo Maipu";
	/** Quilin = Quilin */
	public static final String TIENDA_Quilin = "Quilin";
	/** Vicuña Mackenna = Vicuña Mackenna */
	public static final String TIENDA_VicunaMackenna = "Vicu�a Mackenna";
	/** Apumanque = Apumanque */
	public static final String TIENDA_Apumanque = "Apumanque";
	/** 21 de Mayo = 21 de Mayo */
	public static final String TIENDA_21DeMayo = "21 de Mayo";
	/** Easton = Easton */
	public static final String TIENDA_Easton = "Easton";
	/** La Fabrica = La Fabrica */
	public static final String TIENDA_LaFabrica = "La Fabrica";
	/** Curauma = Curauma */
	public static final String TIENDA_Curauma = "Curauma";
	/** Buenaventura = Buenaventura */
	public static final String TIENDA_Buenaventura = "Buenaventura";
	/** Vivo La Florida = Vivo La Florida */
	public static final String TIENDA_VivoLaFlorida = "Vivo La Florida";
	/** Catalogo Digital = Catalogo Digital */
	public static final String TIENDA_CatalogoDigital = "Catalogo Digital";
	/** Irarrazaval = Irarrazaval */
	public static final String TIENDA_Irarrazaval = "Irarrazaval";
	/** Liquidacion Mackenna = Liquidacion Mackenna */
	public static final String TIENDA_LiquidacionMackenna = "Liquidacion Mackenna";
	/** Catalogo Mackenna = Catalogo Mackenna */
	public static final String TIENDA_CatalogoMackenna = "Catalogo Mackenna";
	/** Viña Outlet Park = Viña Outlet Park */
	public static final String TIENDA_VinaOutletPark = "Vi�a Outlet Park";
	/** Ecommerce = Ecommerce */
	public static final String TIENDA_Ecommerce = "Ecommerce";
	/** Set Tienda.
		@param Tienda Tienda	  */
	public void setTienda (String Tienda)
	{

		set_Value (COLUMNNAME_Tienda, Tienda);
	}

	/** Get Tienda.
		@return Tienda	  */
	public String getTienda () 
	{
		return (String)get_Value(COLUMNNAME_Tienda);
	}
}