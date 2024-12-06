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

/** Generated Model for C_RepoMuroProduct
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_C_RepoMuroProduct extends PO implements I_C_RepoMuroProduct, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200306L;

    /** Standard Constructor */
    public X_C_RepoMuroProduct (Properties ctx, int C_RepoMuroProduct_ID, String trxName)
    {
      super (ctx, C_RepoMuroProduct_ID, trxName);
      /** if (C_RepoMuroProduct_ID == 0)
        {
			setC_RepoMuroProduct_ID (0);
			setM_Product_ID (0);
        } */
    }

    /** Load Constructor */
    public X_C_RepoMuroProduct (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_C_RepoMuroProduct[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	/** Set C_RepoMuroProduct_ID.
		@param C_RepoMuroProduct_ID C_RepoMuroProduct_ID	  */
	public void setC_RepoMuroProduct_ID (int C_RepoMuroProduct_ID)
	{
		if (C_RepoMuroProduct_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_RepoMuroProduct_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_RepoMuroProduct_ID, Integer.valueOf(C_RepoMuroProduct_ID));
	}

	/** Get C_RepoMuroProduct_ID.
		@return C_RepoMuroProduct_ID	  */
	public int getC_RepoMuroProduct_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_RepoMuroProduct_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_RepoMuroTienda getC_RepoMuroTienda() throws RuntimeException
    {
		return (I_C_RepoMuroTienda)MTable.get(getCtx(), I_C_RepoMuroTienda.Table_Name)
			.getPO(getC_RepoMuroTienda_ID(), get_TrxName());	}

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

	public I_M_RequisitionLine getM_RequisitionLine() throws RuntimeException
    {
		return (I_M_RequisitionLine)MTable.get(getCtx(), I_M_RequisitionLine.Table_Name)
			.getPO(getM_RequisitionLine_ID(), get_TrxName());	}

	/** Set Requisition Line.
		@param M_RequisitionLine_ID 
		Material Requisition Line
	  */
	public void setM_RequisitionLine_ID (int M_RequisitionLine_ID)
	{
		if (M_RequisitionLine_ID < 1) 
			set_Value (COLUMNNAME_M_RequisitionLine_ID, null);
		else 
			set_Value (COLUMNNAME_M_RequisitionLine_ID, Integer.valueOf(M_RequisitionLine_ID));
	}

	/** Get Requisition Line.
		@return Material Requisition Line
	  */
	public int getM_RequisitionLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_RequisitionLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set PEDIDO.
		@param PEDIDO PEDIDO	  */
	public void setPEDIDO (int PEDIDO)
	{
		set_Value (COLUMNNAME_PEDIDO, Integer.valueOf(PEDIDO));
	}

	/** Get PEDIDO.
		@return PEDIDO	  */
	public int getPEDIDO () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_PEDIDO);
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