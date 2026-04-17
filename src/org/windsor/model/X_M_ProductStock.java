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

/** Generated Model for M_ProductStock
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_M_ProductStock extends PO implements I_M_ProductStock, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20260127L;

    /** Standard Constructor */
    public X_M_ProductStock (Properties ctx, int M_ProductStock_ID, String trxName)
    {
      super (ctx, M_ProductStock_ID, trxName);
      /** if (M_ProductStock_ID == 0)
        {
			setDateTrx (new Timestamp( System.currentTimeMillis() ));
			setM_Product_ID (0);
			setM_ProductStock_ID (0);
			setWeekNo (0);
        } */
    }

    /** Load Constructor */
    public X_M_ProductStock (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_M_ProductStock[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Transaction Date.
		@param DateTrx 
		Transaction Date
	  */
	public void setDateTrx (Timestamp DateTrx)
	{
		set_Value (COLUMNNAME_DateTrx, DateTrx);
	}

	/** Get Transaction Date.
		@return Transaction Date
	  */
	public Timestamp getDateTrx () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateTrx);
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

	/** Set M_ProductStock_ID.
		@param M_ProductStock_ID M_ProductStock_ID	  */
	public void setM_ProductStock_ID (int M_ProductStock_ID)
	{
		if (M_ProductStock_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_ProductStock_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_ProductStock_ID, Integer.valueOf(M_ProductStock_ID));
	}

	/** Get M_ProductStock_ID.
		@return M_ProductStock_ID	  */
	public int getM_ProductStock_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_ProductStock_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Set QtyBOM.
		@param QtyBOM QtyBOM	  */
	public void setQtyBOM (int QtyBOM)
	{
		set_Value (COLUMNNAME_QtyBOM, Integer.valueOf(QtyBOM));
	}

	/** Get QtyBOM.
		@return QtyBOM	  */
	public int getQtyBOM () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QtyBOM);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set QTYHAND.
		@param QTYHAND QTYHAND	  */
	public void setQTYHAND (int QTYHAND)
	{
		set_Value (COLUMNNAME_QTYHAND, Integer.valueOf(QTYHAND));
	}

	/** Get QTYHAND.
		@return QTYHAND	  */
	public int getQTYHAND () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QTYHAND);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set QtyOnHand.
		@param QtyOnHand 
		On Hand Quantity
	  */
	public void setQtyOnHand (int QtyOnHand)
	{
		set_Value (COLUMNNAME_QtyOnHand, Integer.valueOf(QtyOnHand));
	}

	/** Get QtyOnHand.
		@return On Hand Quantity
	  */
	public int getQtyOnHand () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QtyOnHand);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set QTYPENDIENTE.
		@param QTYPENDIENTE QTYPENDIENTE	  */
	public void setQTYPENDIENTE (int QTYPENDIENTE)
	{
		set_Value (COLUMNNAME_QTYPENDIENTE, Integer.valueOf(QTYPENDIENTE));
	}

	/** Get QTYPENDIENTE.
		@return QTYPENDIENTE	  */
	public int getQTYPENDIENTE () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QTYPENDIENTE);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Reserved Quantity.
		@param QtyReserved 
		Reserved Quantity
	  */
	public void setQtyReserved (int QtyReserved)
	{
		set_Value (COLUMNNAME_QtyReserved, Integer.valueOf(QtyReserved));
	}

	/** Get Reserved Quantity.
		@return Reserved Quantity
	  */
	public int getQtyReserved () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QtyReserved);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set WeekNo.
		@param WeekNo WeekNo	  */
	public void setWeekNo (int WeekNo)
	{
		set_Value (COLUMNNAME_WeekNo, Integer.valueOf(WeekNo));
	}

	/** Get WeekNo.
		@return WeekNo	  */
	public int getWeekNo () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_WeekNo);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}