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
import org.compiere.model.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.util.Env;

/** Generated Model for C_OrderB2CLine
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_C_OrderB2CLine extends PO implements I_C_OrderB2CLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20181217L;

    /** Standard Constructor */
    public X_C_OrderB2CLine (Properties ctx, int C_OrderB2CLine_ID, String trxName)
    {
      super (ctx, C_OrderB2CLine_ID, trxName);
      /** if (C_OrderB2CLine_ID == 0)
        {
			setC_OrderB2C_ID (0);
			setC_OrderB2CLine_ID (0);
        } */
    }

    /** Load Constructor */
    public X_C_OrderB2CLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_C_OrderB2CLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set CODIGOGENERICO.
		@param CODIGOGENERICO CODIGOGENERICO	  */
	public void setCODIGOGENERICO (String CODIGOGENERICO)
	{
		set_Value (COLUMNNAME_CODIGOGENERICO, CODIGOGENERICO);
	}

	/** Get CODIGOGENERICO.
		@return CODIGOGENERICO	  */
	public String getCODIGOGENERICO () 
	{
		return (String)get_Value(COLUMNNAME_CODIGOGENERICO);
	}

	public I_C_OrderB2C getC_OrderB2C() throws RuntimeException
    {
		return (I_C_OrderB2C)MTable.get(getCtx(), I_C_OrderB2C.Table_Name)
			.getPO(getC_OrderB2C_ID(), get_TrxName());	}

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

	/** Set C_OrderB2CLine_ID.
		@param C_OrderB2CLine_ID C_OrderB2CLine_ID	  */
	public void setC_OrderB2CLine_ID (int C_OrderB2CLine_ID)
	{
		if (C_OrderB2CLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_OrderB2CLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_OrderB2CLine_ID, Integer.valueOf(C_OrderB2CLine_ID));
	}

	/** Get C_OrderB2CLine_ID.
		@return C_OrderB2CLine_ID	  */
	public int getC_OrderB2CLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_OrderB2CLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_Order getC_Order() throws RuntimeException
    {
		return (I_C_Order)MTable.get(getCtx(), I_C_Order.Table_Name)
			.getPO(getC_Order_ID(), get_TrxName());	}

	/** Set Order.
		@param C_Order_ID 
		Order
	  */
	public void setC_Order_ID (int C_Order_ID)
	{
		if (C_Order_ID < 1) 
			set_Value (COLUMNNAME_C_Order_ID, null);
		else 
			set_Value (COLUMNNAME_C_Order_ID, Integer.valueOf(C_Order_ID));
	}

	/** Get Order.
		@return Order
	  */
	public int getC_Order_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Order_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_OrderLine getC_OrderLine() throws RuntimeException
    {
		return (I_C_OrderLine)MTable.get(getCtx(), I_C_OrderLine.Table_Name)
			.getPO(getC_OrderLine_ID(), get_TrxName());	}

	/** Set Sales Order Line.
		@param C_OrderLine_ID 
		Sales Order Line
	  */
	public void setC_OrderLine_ID (int C_OrderLine_ID)
	{
		if (C_OrderLine_ID < 1) 
			set_Value (COLUMNNAME_C_OrderLine_ID, null);
		else 
			set_Value (COLUMNNAME_C_OrderLine_ID, Integer.valueOf(C_OrderLine_ID));
	}

	/** Get Sales Order Line.
		@return Sales Order Line
	  */
	public int getC_OrderLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_OrderLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Set Line Amount.
		@param LineNetAmt 
		Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	public void setLineNetAmt (BigDecimal LineNetAmt)
	{
		set_Value (COLUMNNAME_LineNetAmt, LineNetAmt);
	}

	/** Get Line Amount.
		@return Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	public BigDecimal getLineNetAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_LineNetAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
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

	/** Set PASARAOV.
		@param PASARAOV PASARAOV	  */
	public void setPASARAOV (boolean PASARAOV)
	{
		set_Value (COLUMNNAME_PASARAOV, Boolean.valueOf(PASARAOV));
	}

	/** Get PASARAOV.
		@return PASARAOV	  */
	public boolean isPASARAOV () 
	{
		Object oo = get_Value(COLUMNNAME_PASARAOV);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Price.
		@param PriceEntered 
		Price Entered - the price based on the selected/base UoM
	  */
	public void setPriceEntered (int PriceEntered)
	{
		set_Value (COLUMNNAME_PriceEntered, Integer.valueOf(PriceEntered));
	}

	/** Get Price.
		@return Price Entered - the price based on the selected/base UoM
	  */
	public int getPriceEntered () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_PriceEntered);
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

	/** Set Product Key.
		@param ProductValue 
		Key of the Product
	  */
	public void setProductValue (String ProductValue)
	{
		set_Value (COLUMNNAME_ProductValue, ProductValue);
	}

	/** Get Product Key.
		@return Key of the Product
	  */
	public String getProductValue () 
	{
		return (String)get_Value(COLUMNNAME_ProductValue);
	}

	/** Set Quantity.
		@param QtyEntered 
		The Quantity Entered is based on the selected UoM
	  */
	public void setQtyEntered (BigDecimal QtyEntered)
	{
		set_Value (COLUMNNAME_QtyEntered, QtyEntered);
	}

	/** Get Quantity.
		@return The Quantity Entered is based on the selected UoM
	  */
	public BigDecimal getQtyEntered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyEntered);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set StockBodegas.
		@param StockBodegas StockBodegas	  */
	public void setStockBodegas (BigDecimal StockBodegas)
	{
		set_Value (COLUMNNAME_StockBodegas, StockBodegas);
	}

	/** Get StockBodegas.
		@return StockBodegas	  */
	public BigDecimal getStockBodegas () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_StockBodegas);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}