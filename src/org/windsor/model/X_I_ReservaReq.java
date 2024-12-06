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

/** Generated Model for I_ReservaReq
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_I_ReservaReq extends PO implements I_I_ReservaReq, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200527L;

    /** Standard Constructor */
    public X_I_ReservaReq (Properties ctx, int I_ReservaReq_ID, String trxName)
    {
      super (ctx, I_ReservaReq_ID, trxName);
      /** if (I_ReservaReq_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_I_ReservaReq (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_I_ReservaReq[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set ERROR.
		@param ERROR ERROR	  */
	public void setERROR (String ERROR)
	{
		set_Value (COLUMNNAME_ERROR, ERROR);
	}

	/** Get ERROR.
		@return ERROR	  */
	public String getERROR () 
	{
		return (String)get_Value(COLUMNNAME_ERROR);
	}

	/** Set I_ReservaReq_ID.
		@param I_ReservaReq_ID I_ReservaReq_ID	  */
	public void setI_ReservaReq_ID (int I_ReservaReq_ID)
	{
		if (I_ReservaReq_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_I_ReservaReq_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_I_ReservaReq_ID, Integer.valueOf(I_ReservaReq_ID));
	}

	/** Get I_ReservaReq_ID.
		@return I_ReservaReq_ID	  */
	public int getI_ReservaReq_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_I_ReservaReq_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_M_Product getM_Product() throws RuntimeException
    {
		return (I_M_Product)MTable.get(getCtx(), I_M_Product.Table_Name)
			.getPO(getM_Product_ID(), get_TrxName());	}

	/** Set Product.
		@param M_Product_ID 
		Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_Value (COLUMNNAME_M_Product_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Product.
		@return Product, Service, Item
	  */
	public int getM_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
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

	/** Set Available Quantity.
		@param QtyAvailable 
		Available Quantity (On Hand - Reserved)
	  */
	public void setQtyAvailable (int QtyAvailable)
	{
		set_Value (COLUMNNAME_QtyAvailable, Integer.valueOf(QtyAvailable));
	}

	/** Get Available Quantity.
		@return Available Quantity (On Hand - Reserved)
	  */
	public int getQtyAvailable () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QtyAvailable);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Quantity.
		@param QtyEntered 
		The Quantity Entered is based on the selected UoM
	  */
	public void setQtyEntered (int QtyEntered)
	{
		set_Value (COLUMNNAME_QtyEntered, Integer.valueOf(QtyEntered));
	}

	/** Get Quantity.
		@return The Quantity Entered is based on the selected UoM
	  */
	public int getQtyEntered () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QtyEntered);
		if (ii == null)
			 return 0;
		return ii.intValue();
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