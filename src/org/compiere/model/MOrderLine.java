/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.ProductNotOnPriceListException;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.ofb.model.OFBForward;

/**
 *  Order Line Model.
 * 	<code>
 * 			MOrderLine ol = new MOrderLine(m_order);
			ol.setM_Product_ID(wbl.getM_Product_ID());
			ol.setQtyOrdered(wbl.getQuantity());
			ol.setPrice();
			ol.setPriceActual(wbl.getPrice());
			ol.setTax();
			ol.save();

 *	</code>
 *  @author Jorg Janke
 *  @version $Id: MOrderLine.java,v 1.6 2006/10/02 05:18:39 jjanke Exp $
 * 
 * @author Teo Sarca, SC ARHIPAC SERVICE SRL
 *			<li>BF [ 2588043 ] Insufficient message ProductNotOnPriceList
 * @author Michael Judd, www.akunagroup.com
 * 			<li>BF [ 1733602 ] Price List including Tax Error - when a user changes the orderline or
 * 				invoice line for a product on a price list that includes tax, the net amount is
 * 				incorrectly calculated.
 */
public class MOrderLine extends X_C_OrderLine
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7305265800857547603L;

	/**
	 * 	Get Order Unreserved Qty
	 *	@param ctx context
	 *	@param M_Warehouse_ID wh
	 *	@param M_Product_ID product
	 *	@param M_AttributeSetInstance_ID asi
	 *	@param excludeC_OrderLine_ID exclude C_OrderLine_ID
	 *	@return Unreserved Qty
	 */
	public static BigDecimal getNotReserved (Properties ctx, int M_Warehouse_ID, 
		int M_Product_ID, int M_AttributeSetInstance_ID, int excludeC_OrderLine_ID)
	{
		BigDecimal retValue = Env.ZERO;
		String sql = "SELECT SUM(QtyOrdered-QtyDelivered-QtyReserved) "
			+ "FROM C_OrderLine ol"
			+ " INNER JOIN C_Order o ON (ol.C_Order_ID=o.C_Order_ID) "
			+ "WHERE ol.M_Warehouse_ID=?"	//	#1
			+ " AND M_Product_ID=?"			//	#2
			+ " AND o.IsSOTrx='Y' AND o.DocStatus='DR'"
			+ " AND QtyOrdered-QtyDelivered-QtyReserved<>0"
			+ " AND ol.C_OrderLine_ID<>?";
		if (M_AttributeSetInstance_ID != 0)
			sql += " AND M_AttributeSetInstance_ID=?";
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, M_Warehouse_ID);
			pstmt.setInt (2, M_Product_ID);
			pstmt.setInt (3, excludeC_OrderLine_ID);
			if (M_AttributeSetInstance_ID != 0)
				pstmt.setInt (4, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				retValue = rs.getBigDecimal(1);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log (Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		if (retValue == null)
			s_log.fine("-");
		else
			s_log.fine(retValue.toString());
		return retValue;
	}	//	getNotReserved
	
	/**	Logger	*/
	private static CLogger s_log = CLogger.getCLogger (MOrderLine.class);
	
	/**************************************************************************
	 *  Default Constructor
	 *  @param ctx context
	 *  @param  C_OrderLine_ID  order line to load
	 *  @param trxName trx name
	 */
	public MOrderLine (Properties ctx, int C_OrderLine_ID, String trxName)
	{
		super (ctx, C_OrderLine_ID, trxName);
		if (C_OrderLine_ID == 0)
		{
		//	setC_Order_ID (0);
		//	setLine (0);
		//	setM_Warehouse_ID (0);	// @M_Warehouse_ID@
		//	setC_BPartner_ID(0);
		//	setC_BPartner_Location_ID (0);	// @C_BPartner_Location_ID@
		//	setC_Currency_ID (0);	// @C_Currency_ID@
		//	setDateOrdered (new Timestamp(System.currentTimeMillis()));	// @DateOrdered@
			//
		//	setC_Tax_ID (0);
		//	setC_UOM_ID (0);
			//
			setFreightAmt (Env.ZERO);
			setLineNetAmt (Env.ZERO);
			//
			setPriceEntered(Env.ZERO);
			setPriceActual (Env.ZERO);
			setPriceLimit (Env.ZERO);
			setPriceList (Env.ZERO);
			//
			setM_AttributeSetInstance_ID(0);
			//
			setQtyEntered (Env.ZERO);
			setQtyOrdered (Env.ZERO);	// 1
			setQtyDelivered (Env.ZERO);
			setQtyInvoiced (Env.ZERO);
			setQtyReserved (Env.ZERO);
			//
			setIsDescription (false);	// N
			setProcessed (false);
			setLine (0);
		}
	}	//	MOrderLine
	
	/**
	 *  Parent Constructor.
	 		ol.setM_Product_ID(wbl.getM_Product_ID());
			ol.setQtyOrdered(wbl.getQuantity());
			ol.setPrice();
			ol.setPriceActual(wbl.getPrice());
			ol.setTax();
			ol.save();
	 *  @param  order parent order
	 */
	public MOrderLine (MOrder order)
	{
		this (order.getCtx(), 0, order.get_TrxName());
		if (order.get_ID() == 0)
			throw new IllegalArgumentException("Header not saved");
		setC_Order_ID (order.getC_Order_ID());	//	parent
		setOrder(order);
	}	//	MOrderLine

	/**
	 *  Load Constructor
	 *  @param ctx context
	 *  @param rs result set record
	 *  @param trxName transaction
	 */
	public MOrderLine (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MOrderLine

	private int 			m_M_PriceList_ID = 0;
	//
	private boolean			m_IsSOTrx = true;
	//	Product Pricing
	private MProductPricing	m_productPrice = null;

	/** Tax							*/
	private MTax 		m_tax = null;
	
	/** Cached Currency Precision	*/
	private Integer			m_precision = null;
	/**	Product					*/
	private MProduct 		m_product = null;
	/**	Charge					*/
	private MCharge 		m_charge = null;
	/** Parent					*/
	private MOrder			m_parent = null;
	
	/**
	 * 	Set Defaults from Order.
	 * 	Does not set Parent !!
	 * 	@param order order
	 */
	public void setOrder (MOrder order)
	{
		setClientOrg(order);
		setC_BPartner_ID(order.getC_BPartner_ID());
		setC_BPartner_Location_ID(order.getC_BPartner_Location_ID());
		setM_Warehouse_ID(order.getM_Warehouse_ID());
		setDateOrdered(order.getDateOrdered());
		setDatePromised(order.getDatePromised());
		setC_Currency_ID(order.getC_Currency_ID());
		//
		setHeaderInfo(order);	//	sets m_order
		//	Don't set Activity, etc as they are overwrites
	}	//	setOrder

	/**
	 * 	Set Header Info
	 *	@param order order
	 */
	public void setHeaderInfo (MOrder order)
	{
		m_parent = order;
		m_precision = new Integer(order.getPrecision());
		m_M_PriceList_ID = order.getM_PriceList_ID();
		m_IsSOTrx = order.isSOTrx();
	}	//	setHeaderInfo
	
	/**
	 * 	Get Parent
	 *	@return parent
	 */
	public MOrder getParent()
	{
		if (m_parent == null)
			m_parent = new MOrder(getCtx(), getC_Order_ID(), get_TrxName());
		return m_parent;
	}	//	getParent
	
	/**
	 * 	Set Price Entered/Actual.
	 * 	Use this Method if the Line UOM is the Product UOM 
	 *	@param PriceActual price
	 */
	public void setPrice (BigDecimal PriceActual)
	{
		setPriceEntered(PriceActual);
		setPriceActual (PriceActual);
	}	//	setPrice

	/**
	 * 	Set Price Actual.
	 * 	(actual price is not updateable)
	 *	@param PriceActual actual price
	 */
	public void setPriceActual (BigDecimal PriceActual)
	{
		if (PriceActual == null) 
			throw new IllegalArgumentException ("PriceActual is mandatory");
		set_ValueNoCheck("PriceActual", PriceActual);
	}	//	setPriceActual

	/**
	 * 	Set Price for Product and PriceList.
	 * 	Use only if newly created.
	 * 	Uses standard price list of not set by order constructor
	 */
	public void setPrice()
	{
		if (getM_Product_ID() == 0)
			return;
		if (m_M_PriceList_ID == 0)
			throw new IllegalStateException("PriceList unknown!");
		setPrice (m_M_PriceList_ID);
	}	//	setPrice

	/**
	 * 	Set Price for Product and PriceList
	 * 	@param M_PriceList_ID price list
	 */
	public void setPrice (int M_PriceList_ID)
	{
		if (getM_Product_ID() == 0)
			return;
		//
		log.fine(toString() + " - M_PriceList_ID=" + M_PriceList_ID);
		getProductPricing (M_PriceList_ID);
		setPriceActual (m_productPrice.getPriceStd());
		setPriceList (m_productPrice.getPriceList());
		setPriceLimit (m_productPrice.getPriceLimit());
		//
		if (getQtyEntered().compareTo(getQtyOrdered()) == 0)
			setPriceEntered(getPriceActual());
		else
			setPriceEntered(getPriceActual().multiply(getQtyOrdered()
				.divide(getQtyEntered(), 12, BigDecimal.ROUND_HALF_UP)));	//	recision
		
		//	Calculate Discount
		setDiscount(m_productPrice.getDiscount());
		//	Set UOM
		setC_UOM_ID(m_productPrice.getC_UOM_ID());
	}	//	setPrice

	/**
	 * 	Get and calculate Product Pricing
	 *	@param M_PriceList_ID id
	 *	@return product pricing
	 */
	private MProductPricing getProductPricing (int M_PriceList_ID)
	{
		m_productPrice = new MProductPricing (getM_Product_ID(), 
			getC_BPartner_ID(), getQtyOrdered(), m_IsSOTrx);
		m_productPrice.setM_PriceList_ID(M_PriceList_ID);
		m_productPrice.setPriceDate(getDateOrdered());
		//
		m_productPrice.calculatePrice();
		return m_productPrice;
	}	//	getProductPrice
	
	/**
	 *	Set Tax
	 *	@return true if tax is set
	 */
	public boolean setTax()
	{
		int ii = Tax.get(getCtx(), getM_Product_ID(), getC_Charge_ID(), getDateOrdered(), getDateOrdered(),
			getAD_Org_ID(), getM_Warehouse_ID(),
			getC_BPartner_Location_ID(),		//	should be bill to
			getC_BPartner_Location_ID(), m_IsSOTrx);
		if (ii == 0)
		{
			log.log(Level.SEVERE, "No Tax found");
			return false;
		}
		setC_Tax_ID (ii);
		return true;
	}	//	setTax
	
	/**
	 * 	Calculate Extended Amt.
	 * 	May or may not include tax
	 */
	public void setLineNetAmt ()
	{
		BigDecimal bd = getPriceActual().multiply(getQtyOrdered()); 
		
		boolean documentLevel = getTax().isDocumentLevel();
		
		//	juddm: Tax Exempt & Tax Included in Price List & not Document Level - Adjust Line Amount
		//  http://sourceforge.net/tracker/index.php?func=detail&aid=1733602&group_id=176962&atid=879332
		if (isTaxIncluded() && !documentLevel)	{
			BigDecimal taxStdAmt = Env.ZERO, taxThisAmt = Env.ZERO;
			
			MTax orderTax = getTax();
			MTax stdTax = null;
			
			//	get the standard tax
			if (getProduct() == null)
			{
				if (getCharge() != null)	// Charge 
				{
					stdTax = new MTax (getCtx(), 
							((MTaxCategory) getCharge().getC_TaxCategory()).getDefaultTax().getC_Tax_ID(),
							get_TrxName());
				}
					
			}
			else	// Product
				stdTax = new MTax (getCtx(), 
							((MTaxCategory) getProduct().getC_TaxCategory()).getDefaultTax().getC_Tax_ID(), 
							get_TrxName());

			if (stdTax != null)
			{
				log.fine("stdTax rate is " + stdTax.getRate());
				log.fine("orderTax rate is " + orderTax.getRate());
				
				taxThisAmt = taxThisAmt.add(orderTax.calculateTax(bd, isTaxIncluded(), getPrecision()));
				taxStdAmt = taxStdAmt.add(stdTax.calculateTax(bd, isTaxIncluded(), getPrecision()));
				
				bd = bd.subtract(taxStdAmt).add(taxThisAmt);
				
				log.fine("Price List includes Tax and Tax Changed on Order Line: New Tax Amt: " 
						+ taxThisAmt + " Standard Tax Amt: " + taxStdAmt + " Line Net Amt: " + bd);	
			}
			
		}
		
		if (bd.scale() > getPrecision())
			bd = bd.setScale(getPrecision(), BigDecimal.ROUND_HALF_UP);
		super.setLineNetAmt (bd);
	}	//	setLineNetAmt
	
	/**
	 * 	Get Charge
	 *	@return product or null
	 */
	public MCharge getCharge()
	{
		if (m_charge == null && getC_Charge_ID() != 0)
			m_charge =  MCharge.get (getCtx(), getC_Charge_ID());
		return m_charge;
	}
	/**
	 * 	Get Tax
	 *	@return tax
	 */
	protected MTax getTax()
	{
		if (m_tax == null)
			m_tax = MTax.get(getCtx(), getC_Tax_ID());
		return m_tax;
	}	//	getTax

	/**
	 * 	Get Currency Precision from Currency
	 *	@return precision
	 */
	public int getPrecision()
	{
		if (m_precision != null)
			return m_precision.intValue();
		//
		if (getC_Currency_ID() == 0)
		{
			setOrder (getParent());
			if (m_precision != null)
				return m_precision.intValue();
		}
		if (getC_Currency_ID() != 0)
		{
			MCurrency cur = MCurrency.get(getCtx(), getC_Currency_ID());
			if (cur.get_ID() != 0)
			{
				m_precision = new Integer (cur.getStdPrecision());
				return m_precision.intValue();
			}
		}
		//	Fallback
		String sql = "SELECT c.StdPrecision "
			+ "FROM C_Currency c INNER JOIN C_Order x ON (x.C_Currency_ID=c.C_Currency_ID) "
			+ "WHERE x.C_Order_ID=?";
		int i = DB.getSQLValue(get_TrxName(), sql, getC_Order_ID());
		m_precision = new Integer(i);
		return m_precision.intValue();
	}	//	getPrecision
	
	/**
	 * 	Set Product
	 *	@param product product
	 */
	public void setProduct (MProduct product)
	{
		m_product = product;
		if (m_product != null)
		{
			setM_Product_ID(m_product.getM_Product_ID());
			setC_UOM_ID (m_product.getC_UOM_ID());
		}
		else
		{
			setM_Product_ID(0);
			set_ValueNoCheck ("C_UOM_ID", null);
		}
		setM_AttributeSetInstance_ID(0);
	}	//	setProduct

	
	/**
	 * 	Set M_Product_ID
	 *	@param M_Product_ID product
	 *	@param setUOM set also UOM
	 */
	public void setM_Product_ID (int M_Product_ID, boolean setUOM)
	{
		if (setUOM)
			setProduct(MProduct.get(getCtx(), M_Product_ID));
		else
			super.setM_Product_ID (M_Product_ID);
		setM_AttributeSetInstance_ID(0);
	}	//	setM_Product_ID
	
	/**
	 * 	Set Product and UOM
	 *	@param M_Product_ID product
	 *	@param C_UOM_ID uom
	 */
	public void setM_Product_ID (int M_Product_ID, int C_UOM_ID)
	{
		super.setM_Product_ID (M_Product_ID);
		if (C_UOM_ID != 0)
			super.setC_UOM_ID(C_UOM_ID);
		setM_AttributeSetInstance_ID(0);
	}	//	setM_Product_ID
	
	
	/**
	 * 	Get Product
	 *	@return product or null
	 */
	public MProduct getProduct()
	{
		if (m_product == null && getM_Product_ID() != 0)
			m_product =  MProduct.get (getCtx(), getM_Product_ID());
		return m_product;
	}	//	getProduct
	
	/**
	 * 	Set M_AttributeSetInstance_ID
	 *	@param M_AttributeSetInstance_ID id
	 */
	public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID)
	{
		if (M_AttributeSetInstance_ID == 0)		//	 0 is valid ID
			set_Value("M_AttributeSetInstance_ID", new Integer(0));
		else
			super.setM_AttributeSetInstance_ID (M_AttributeSetInstance_ID);
	}	//	setM_AttributeSetInstance_ID
	
	/**
	 * 	Set Warehouse
	 *	@param M_Warehouse_ID warehouse
	 */
	public void setM_Warehouse_ID (int M_Warehouse_ID)
	{
		if (getM_Warehouse_ID() > 0
			&& getM_Warehouse_ID() != M_Warehouse_ID
			&& !canChangeWarehouse())
			log.severe("Ignored - Already Delivered/Invoiced/Reserved");
		else
			super.setM_Warehouse_ID (M_Warehouse_ID);
	}	//	setM_Warehouse_ID
	
	/**
	 * 	Can Change Warehouse
	 *	@return true if warehouse can be changed
	 */
	public boolean canChangeWarehouse()
	{
		if (getQtyDelivered().signum() != 0)
		{
			log.saveError("Error", Msg.translate(getCtx(), "QtyDelivered") + "=" + getQtyDelivered());
			return false;
		}
		if (getQtyInvoiced().signum() != 0)
		{
			log.saveError("Error", Msg.translate(getCtx(), "QtyInvoiced") + "=" + getQtyInvoiced());
			return false;
		}
		if (getQtyReserved().signum() != 0)
		{
			log.saveError("Error", Msg.translate(getCtx(), "QtyReserved") + "=" + getQtyReserved());
			return false;
		}
		//	We can change
		return true;
	}	//	canChangeWarehouse
	
	/**
	 * 	Get C_Project_ID
	 *	@return project
	 */
	public int getC_Project_ID()
	{
		int ii = super.getC_Project_ID ();
		if (ii == 0)
			ii = getParent().getC_Project_ID();
		return ii;
	}	//	getC_Project_ID
	
	/**
	 * 	Get C_Activity_ID
	 *	@return Activity
	 */
	public int getC_Activity_ID()
	{
		int ii = super.getC_Activity_ID ();
		if (ii == 0)
			ii = getParent().getC_Activity_ID();
		return ii;
	}	//	getC_Activity_ID
	
	/**
	 * 	Get C_Campaign_ID
	 *	@return Campaign
	 */
	public int getC_Campaign_ID()
	{
		int ii = super.getC_Campaign_ID ();
		if (ii == 0)
			ii = getParent().getC_Campaign_ID();
		return ii;
	}	//	getC_Campaign_ID
	
	/**
	 * 	Get User2_ID
	 *	@return User2
	 */
	public int getUser1_ID ()
	{
		int ii = super.getUser1_ID ();
		if (ii == 0)
			ii = getParent().getUser1_ID();
		return ii;
	}	//	getUser1_ID

	/**
	 * 	Get User2_ID
	 *	@return User2
	 */
	public int getUser2_ID ()
	{
		int ii = super.getUser2_ID ();
		if (ii == 0)
			ii = getParent().getUser2_ID();
		return ii;
	}	//	getUser2_ID

	/**
	 * 	Get AD_OrgTrx_ID
	 *	@return trx org
	 */
	public int getAD_OrgTrx_ID()
	{
		int ii = super.getAD_OrgTrx_ID();
		if (ii == 0)
			ii = getParent().getAD_OrgTrx_ID();
		return ii;
	}	//	getAD_OrgTrx_ID

	/**************************************************************************
	 * 	String Representation
	 * 	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MOrderLine[")
			.append(get_ID())
			.append(", Line=").append(getLine())
			.append(", Ordered=").append(getQtyOrdered())
			.append(", Delivered=").append(getQtyDelivered())
			.append(", Invoiced=").append(getQtyInvoiced())
			.append(", Reserved=").append(getQtyReserved())
			.append(", LineNet=").append(getLineNetAmt())
			.append ("]");
		return sb.toString ();
	}	//	toString

	/**
	 * 	Add to Description
	 *	@param description text
	 */
	public void addDescription (String description)
	{
		String desc = getDescription();
		if (desc == null)
			setDescription(description);
		else
			setDescription(desc + " | " + description);
	}	//	addDescription
	
	/**
	 * 	Get Description Text.
	 * 	For jsp access (vs. isDescription)
	 *	@return description
	 */
	public String getDescriptionText()
	{
		return super.getDescription();
	}	//	getDescriptionText
	
	/**
	 * 	Get Name
	 *	@return get the name of the line (from Product)
	 */
	public String getName()
	{
		getProduct();
		if (m_product != null)
			return m_product.getName();
		if (getC_Charge_ID() != 0)
		{
			MCharge charge = MCharge.get(getCtx(), getC_Charge_ID());
			return charge.getName();
		}
		return "";
	}	//	getName

	/**
	 * 	Set C_Charge_ID
	 *	@param C_Charge_ID charge
	 */
	public void setC_Charge_ID (int C_Charge_ID)
	{
		super.setC_Charge_ID (C_Charge_ID);
		if (C_Charge_ID > 0)
			set_ValueNoCheck ("C_UOM_ID", null);
	}	//	setC_Charge_ID
	/**
	 *	Set Discount
	 */
	public void setDiscount()
	{
		BigDecimal list = getPriceList();
		//	No List Price
		if (Env.ZERO.compareTo(list) == 0)
			return;
		BigDecimal discount = list.subtract(getPriceActual())
			.multiply(new BigDecimal(100))
			.divide(list, getPrecision(), BigDecimal.ROUND_HALF_UP);
		setDiscount(discount);
	}	//	setDiscount

	/**
	 *	Is Tax Included in Amount
	 *	@return true if tax calculated
	 */
	public boolean isTaxIncluded()
	{
		if (m_M_PriceList_ID == 0)
		{
			m_M_PriceList_ID = DB.getSQLValue(get_TrxName(),
				"SELECT M_PriceList_ID FROM C_Order WHERE C_Order_ID=?",
				getC_Order_ID());
		}
		MPriceList pl = MPriceList.get(getCtx(), m_M_PriceList_ID, get_TrxName());
		return pl.isTaxIncluded();
	}	//	isTaxIncluded

	
	/**
	 * 	Set Qty Entered/Ordered.
	 * 	Use this Method if the Line UOM is the Product UOM 
	 *	@param Qty QtyOrdered/Entered
	 */
	public void setQty (BigDecimal Qty)
	{
		super.setQtyEntered (Qty);
		super.setQtyOrdered (getQtyEntered());
	}	//	setQty

	/**
	 * 	Set Qty Entered - enforce entered UOM 
	 *	@param QtyEntered
	 */
	public void setQtyEntered (BigDecimal QtyEntered)
	{
		if (QtyEntered != null && getC_UOM_ID() != 0)
		{
			int precision = MUOM.getPrecision(getCtx(), getC_UOM_ID());
			QtyEntered = QtyEntered.setScale(precision, BigDecimal.ROUND_HALF_UP);
		}
		super.setQtyEntered (QtyEntered);
	}	//	setQtyEntered

	/**
	 * 	Set Qty Ordered - enforce Product UOM 
	 *	@param QtyOrdered
	 */
	public void setQtyOrdered (BigDecimal QtyOrdered)
	{
		MProduct product = getProduct();
		if (QtyOrdered != null && product != null)
		{
			int precision = product.getUOMPrecision();
			QtyOrdered = QtyOrdered.setScale(precision, BigDecimal.ROUND_HALF_UP);
		}
		super.setQtyOrdered(QtyOrdered);
	}	//	setQtyOrdered

	/**************************************************************************
	 * 	Before Save
	 *	@param newRecord
	 *	@return true if it can be saved
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		if (newRecord && getParent().isComplete()) {
			log.saveError("ParentComplete", Msg.translate(getCtx(), "C_OrderLine"));
			return false;
		}
		
		/**faaguilar OFB validation*/
		if(getC_Charge_ID() == 0 && getM_Product_ID() == 0)
		{
			log.saveError("Error"," No product - No Charge");
			return false;
		}
			
		/**faaguilar OFB
		 * validacion de linea repetida*/
		//faaguilar OFB validacion existencia Begin
		int existe=0;
		String desc=new String();
		String sub=new String();
		String prom=new String();
		
		
		if(getDescription()==null)
		desc=" and Description is null";
		else
		desc=" and Description ='"+getDescription()+"'";
		
		if(get_ValueAsInt("C_SubProjectOFB_ID")==0)
		sub=" and C_SubProjectOFB_ID is null ";
		else
		sub=" and C_SubProjectOFB_ID="+get_ValueAsInt("C_SubProjectOFB_ID")+" ";
		
		if(this.getDatePromised()!=null)
			prom= " and DatePromised=?";
		
		existe=DB.getSQLValue(null,"select count(1) from c_orderline where C_OrderLine_ID<>"+getC_OrderLine_ID()+ desc + sub
					+ " and M_Product_ID="+getM_Product_ID() +" and AD_Org_ID="+getAD_Org_ID() +" and C_Order_ID="+getC_Order_ID() + prom,getDatePromised());
					
		log.fine("select count(1) from c_orderline where C_OrderLine_ID<>"+getC_OrderLine_ID()+ desc + sub
					+ " and M_Product_ID="+getM_Product_ID() +" and AD_Org_ID="+getAD_Org_ID() +" and C_Order_ID="+getC_Order_ID() + prom + getDatePromised());
		
		if(OFBForward.NoValidationLineOrderRep())
			log.config("Sin validacion linea repetida");
		else if(OFBForward.ValidationLineOrderRepWindsor())
		{
			if(get_ValueAsInt("M_RequisitionLine_ID") > 0)
				existe=DB.getSQLValue(null,"select count(1) from c_orderline where C_OrderLine_ID<>"+getC_OrderLine_ID()+ desc + sub+
						" and M_Product_ID="+getM_Product_ID() +" and AD_Org_ID="+getAD_Org_ID() +" " +
						" AND M_RequisitionLine_ID > 0 AND C_Order_ID="+getC_Order_ID() + prom,getDatePromised());
			else
				existe=DB.getSQLValue(null,"select count(1) from c_orderline where C_OrderLine_ID<>"+getC_OrderLine_ID()+ desc + sub+
						" and M_Product_ID="+getM_Product_ID() +" and AD_Org_ID="+getAD_Org_ID() +" " +
						" AND M_RequisitionLine_ID IS NULL AND C_Order_ID="+getC_Order_ID() + prom,getDatePromised());
			if(existe > 0)
				log.saveError("Error", "Ya existe una linea con este Producto y misma informacion");
		}
		else
		{
			if(existe>0 && !MClient.get(getCtx()).getName().equals("Red Educacional Pedro de Valdivia")) //omitir cliente colegios PDV y Mutual
			{
				log.saveError("Error", "Ya existe una linea con este Producto y misma informacion");
				//m_processMsg = "Ya existe una linea con este Producto y Organizacion";
				return false;
			}
		}
	   //faaguilar validacion existencia END
					
	   /**
	    * faaguilar OFB
	    * validacion de numero maximo de lineas permitidas*/
		//faaguilar OFB validation lines number
		if(newRecord)
		{
			int maxlines=10;
			if(IsSoTrx())
				maxlines=DB.getSQLValue(null,"select MaxSoLines from Ad_Client where AD_Client_ID="+getAD_Client_ID());
			else
				maxlines=DB.getSQLValue(null,"select MaxPoLines from Ad_Client where AD_Client_ID="+getAD_Client_ID());
						
			int currentlines=DB.getSQLValue(null,"select count(1) from C_OrderLine where C_Order_ID="+getC_Order_ID());
			if(maxlines==currentlines)
			{
			log.saveError("AVISO", "Esta cotizacion a superado el numero maximo de lineas permitidas en una Factura :"+maxlines);
			return false;
			}
		}
		//end validation			
		
		//	Get Defaults from Parent
		if (getC_BPartner_ID() == 0 || getC_BPartner_Location_ID() == 0
			|| getM_Warehouse_ID() == 0 
			|| getC_Currency_ID() == 0)
			setOrder (getParent());
		if (m_M_PriceList_ID == 0)
			setHeaderInfo(getParent());

		
		//	R/O Check - Product/Warehouse Change
		if (!newRecord 
			&& (is_ValueChanged("M_Product_ID") || is_ValueChanged("M_Warehouse_ID"))) 
		{
			if (!canChangeWarehouse())
				return false;
		}	//	Product Changed
		
		//	Charge
		if (getC_Charge_ID() != 0 && getM_Product_ID() != 0)
				setM_Product_ID(0);
		//	No Product
		if (getM_Product_ID() == 0)
			setM_AttributeSetInstance_ID(0);
		//	Product
		else	//	Set/check Product Price
		{
			//	Set Price if Actual = 0
			//ininoles manejo de validacion para precio 0 para mutual
			if(!OFBForward.NoValidationPriceOrderLineZero())
			{
				if (m_productPrice == null 
						&&  Env.ZERO.compareTo(getPriceActual()) == 0
						&&  Env.ZERO.compareTo(getPriceList()) == 0)
						setPrice();
			}				
			//	Check if on Price list
			if (m_productPrice == null)
				getProductPricing(m_M_PriceList_ID);
			if (!m_productPrice.isCalculated())
			{
				throw new ProductNotOnPriceListException(m_productPrice, getLine());
			}
		}

		//	UOM
		if (getC_UOM_ID() == 0 
			&& (getM_Product_ID() != 0 
				|| getPriceEntered().compareTo(Env.ZERO) != 0
				|| getC_Charge_ID() != 0))
		{
			int C_UOM_ID = MUOM.getDefault_UOM_ID(getCtx());
			if (C_UOM_ID > 0)
				setC_UOM_ID (C_UOM_ID);
		}
		//	Qty Precision
		if (newRecord || is_ValueChanged("QtyEntered"))
			setQtyEntered(getQtyEntered());
		if (newRecord || is_ValueChanged("QtyOrdered"))
			setQtyOrdered(getQtyOrdered());
		
		//faaguilar OFB begin
		log.config("*recarga variable*");
		log.config("Line_ID:"+this.getC_OrderLine_ID());
		log.config("DocNo:" + this.getParent().getDocumentNo());
		log.config("IsSOTrx:"+this.getParent().isSOTrx());
		m_IsSOTrx = this.getParent().isSOTrx();
		//faaguilar OFB end
		
		//	Qty on instance ASI for SO
		if (m_IsSOTrx 
			&& getM_AttributeSetInstance_ID() != 0
			&& (newRecord || is_ValueChanged("M_Product_ID")
				|| is_ValueChanged("M_AttributeSetInstance_ID")
				|| is_ValueChanged("M_Warehouse_ID")))
		{
			MProduct product = getProduct();
			if (product.isStocked() && product.get_ValueAsBoolean("CheckQty"))//faaguilar OFB add && product.get_ValueAsBoolean("CheckQty")
			{
				int M_AttributeSet_ID = product.getM_AttributeSet_ID();
				boolean isInstance = M_AttributeSet_ID != 0;
				if (isInstance)
				{
					MAttributeSet mas = MAttributeSet.get(getCtx(), M_AttributeSet_ID);
					isInstance = mas.isInstanceAttribute();
				}
				//	Max
				if (isInstance)
				{
					MStorage[] storages = MStorage.getWarehouse(getCtx(), 
						getM_Warehouse_ID(), getM_Product_ID(), getM_AttributeSetInstance_ID(), 
						M_AttributeSet_ID, false, null, true, get_TrxName());
					BigDecimal qty = Env.ZERO;
					for (int i = 0; i < storages.length; i++)
					{
						if (storages[i].getM_AttributeSetInstance_ID() == getM_AttributeSetInstance_ID())
							qty = qty.add(storages[i].getQtyOnHand());
					}
					
					if (getQtyOrdered().compareTo(qty) > 0)
					{
						log.warning("Qty - Stock=" + qty + ", Ordered=" + getQtyOrdered());
						log.saveError("QtyInsufficient", "=" + qty); 
						return false;
					}
				}
			}	//	stocked
		}	//	SO instance
		
		// valida para productos con lista de materiales (1000582: Orden de venta volumen)
		/*if (m_IsSOTrx && this.getParent().getC_DocTypeTarget_ID() == 1000582) {
			// Stock Producto Terminado
			// M_WareHouse_ID 1000010: Abastecimiento
			// M_WareHouse_ID 1000001: Lampa
			String sql = "SELECT (qtyavailableopenvia(p.m_product_ID,1000010) + qtyavailableopenvia(p.m_product_ID,1000001)) as disponible"
					+ " FROM M_product p"
					+ " WHERE  p.m_product_ID="	+ getM_Product_ID();
			BigDecimal dispPT = null;
			try {
				PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
				ResultSet rsps = pstmtps.executeQuery();
				if (rsps.next()) {
					dispPT = rsps.getBigDecimal("disponible");
				}
				rsps.close();
				pstmtps.close();
			} catch (SQLException e) {
				s_log.log (Level.SEVERE, sql, e);
			}
			
			if (getQtyOrdered().compareTo(dispPT) > 0) {
				log.warning("Qty - Stock=" + dispPT + ", Ordered=" + getQtyOrdered());
				log.saveError("QtyInsufficient", "=" + dispPT); 
				return false;
			}
			
		}*/
		
		//	FreightAmt Not used
		if (Env.ZERO.compareTo(getFreightAmt()) != 0)
			setFreightAmt(Env.ZERO);

		//	Set Tax
		if (getC_Tax_ID() == 0)
			setTax();

		//	Get Line No
		if (getLine() == 0)
		{
			String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM C_OrderLine WHERE C_Order_ID=?";
			int ii = DB.getSQLValue (get_TrxName(), sql, getC_Order_ID());
			setLine (ii);
		}
		
		//	Calculations & Rounding
		setLineNetAmt();	//	extended Amount with or without tax		
		BigDecimal desc2 = getDiscount();
		if(desc2.equals(Env.ZERO))
			setDiscount();
		
		//faaguilar OFB manejo de cargos begin
		if(getC_Charge_ID() != 0)
		{
			MCharge charge = MCharge.get(getCtx(), getC_Charge_ID());
			if(charge.getC_ChargeType_ID()>0)
			{
				if(!charge.getC_ChargeType().getValue().equals("TCAF") &&  !charge.getC_ChargeType().getValue().equals("TCAG")  &&  !charge.getC_ChargeType().getValue().equals("TCRC"))
					setQtyDelivered(this.getQtyEntered());
				else
					setQtyDelivered(Env.ZERO);
				
				int ID_AssetGroupValid = 0;
				int ID_AssetGroupValidOl = get_ValueAsInt("A_Asset_Group_ID");
				
				if(charge.getC_ChargeType().getValue().equals("TCAF") && get_ValueAsInt("M_RequisitionLine_ID")>0) //copia desde solicitud compra de activo
				{
					MRequisitionLine rline = new  MRequisitionLine(getCtx(),get_ValueAsInt("M_RequisitionLine_ID") , get_TrxName());
					//se crean campos para que no de error de asignaciones multiples a la misma columna
					ID_AssetGroupValid = rline.get_ValueAsInt("A_Asset_Group_ID");					
					
					//ininoles se valida que campos de la solicitud no sean nulos 
					if(rline.get_ValueAsBoolean("A_CreateAsset"))
						set_CustomColumn("A_CreateAsset", rline.get_Value("A_CreateAsset"));
					if (rline.get_Value("A_CapvsExp") != null)
						set_CustomColumn("A_CapvsExp", rline.get_Value("A_CapvsExp"));					
					if (ID_AssetGroupValid > 0 && ID_AssetGroupValidOl < 1)
						set_CustomColumn("A_Asset_Group_ID", rline.get_Value("A_Asset_Group_ID"));
					//ininoles end					
					if(get_ValueAsInt("A_Asset_ID") > 0)
						set_CustomColumn("A_Asset_ID", rline.get_Value("A_Asset_ID"));
					
				}
				else if (charge.getC_ChargeType().getValue().equals("TCAF") )
				{
					ID_AssetGroupValid = get_ValueAsInt("A_Asset_Group_ID");
				}
				
				if(charge.getC_ChargeType().getValue().equals("TCAF"))
				{	
					if(get_ValueAsString("A_CapvsExp").equals(""))
					{
						log.saveError("Activo","se debe indicar si es compra o gasto de activo");
						return false;
					}
					
					if(get_ValueAsString("A_CapvsExp").equals(X_C_InvoiceLine.A_CAPVSEXP_Expense))//gasto activo
						if(get_ValueAsInt("A_Asset_ID")<=0)
						{
							log.saveError("Activo","Para gastos relacionados con un activo, el activo es obligatorio");
							return false;
						}
					
					if(get_ValueAsString("A_CapvsExp").equals(X_C_InvoiceLine.A_CAPVSEXP_Capital))//compra activo
						if(ID_AssetGroupValid <= 0)
						{
							int group_ID = DB.getSQLValue(get_TrxName(), "select a_asset_group_id from a_asset_group where isdefault='Y' and ad_client_id="+this.getAD_Client_ID());
							if(group_ID <= 0)
							{
								log.saveError("Activo","No existe un grupo por defecto para activos, debe indicar un grupo");
								return false;
							}
							else
								if (ID_AssetGroupValidOl < 1)
									set_CustomColumn("A_Asset_Group_ID", group_ID);
						}
				}
			}
			
			
			/*se modifico en clase SubCurrentLine para mejor optimizaci�n
			//ininoles sobreescribimos qtyDelivered para cargo usado en pos
			MOrder orderPOS = new MOrder(getCtx(), getC_Order_ID(), get_TrxName());
			if(charge.getName().equalsIgnoreCase("DESCUENTO") && orderPOS.isSOTrx()==true)
				setQtyDelivered(Env.ZERO);
			//end ininoles
			*/
		}
		//faaguilar OFB manejo de cargos end
		
		//faaguilar OFB begin chequeo rangos recibos
		if(!newRecord){
			if(isRangeActive() &&  is_ValueChanged("QtyDelivered") && !getParent().isSOTrx()){//rangos qty compras
				
				MClient client = MClient.get(Env.getCtx());
				int currentPercent = (this.getQtyDelivered().multiply(Env.ONEHUNDRED)).divide(this.getQtyEntered(), 0, BigDecimal.ROUND_HALF_UP).intValue();
				if(currentPercent<100)
					currentPercent=100-currentPercent;
				else
					currentPercent= currentPercent-100;
				
				log.info("currentPercent :" + currentPercent);
				this.set_CustomColumn("CanReceipt", true);
				
				if(this.getQtyDelivered().compareTo(this.getQtyEntered())<=0 && currentPercent <= client.get_ValueAsInt("RangeReceiptLow"))
					this.set_CustomColumn("CanReceipt", false);
					
				if(this.getQtyDelivered().compareTo(this.getQtyEntered())>=0 && currentPercent <=client.get_ValueAsInt("RangeReceiptHi"))
					this.set_CustomColumn("CanReceipt", false);
				
			}
			if(isRangeActive() &&  is_ValueChanged("CanReceipt") && !getParent().isSOTrx() && this.get_ValueAsBoolean("CanReceipt") ){
				
				MClient client = MClient.get(Env.getCtx());
				int currentPercent = (this.getQtyDelivered().multiply(Env.ONEHUNDRED)).divide(this.getQtyEntered(), 0, BigDecimal.ROUND_HALF_UP).intValue();
				if(currentPercent<100)
					currentPercent=100-currentPercent;
				else
					currentPercent= currentPercent-100;
				
				log.info("currentPercent :" + currentPercent);
				log.info("linea reactivada");
				
				if(this.getQtyDelivered().compareTo(this.getQtyEntered())>0  && currentPercent > client.get_ValueAsInt("RangeReceiptHi"))
				{
					log.saveError("No puede recibir esta linea, supera el porcentaje", "=" + currentPercent); 
					return false;
				}
				
			}
			
		}
		//faaguilar OFB end
		
		// Validacion, no se pueden crear lineas de productos volumen (solo desde ventana de distrubucion)
		boolean validaVolumen = DB.getSQLValueString(get_TrxName(), "SELECT ov_valida_volumen FROM AD_Client WHERE AD_Client_ID = ?", getAD_Client_ID()).equals("Y");
		boolean esMaquilador = DB.getSQLValueString(getName(), "SELECT OV_ESMAQUILADOR FROM c_bpartner WHERE c_bpartner_ID = ?", getC_Order().getC_BPartner_ID()).equals("Y");
		if (validaVolumen) {
			if (!esMaquilador) {
				if (productIsVolumen(getM_Product_ID()) && !get_ValueAsBoolean("ov_distribucion") && getC_Order().getC_DocTypeTarget_ID() != 1000582 && getC_Order().isSOTrx()) {
					log.saveError("Error","Producto Volumen no puede ser agregado (Solo desde Distribución)");
					return false;
				}
			}
		}

		return true;
	}	//	beforeSave

	
	private boolean productIsVolumen(int m_Product_ID) {
		String sql = "SELECT count(*) FROM M_Product_BOM WHERE OV_PRINCIPAL = 'Y' AND M_Product_ID = " + m_Product_ID;
		if (DB.getSQLValue(get_TrxName(), sql) > 0) {
			sql = "SELECT count(*) FROM M_Product a, M_PRODUCT_LINEA b WHERE a.M_PRODUCT_LINEA_ID = b.M_PRODUCT_LINEA_ID AND b.ISVOLUME = 'Y' AND a.M_Product_ID = " + m_Product_ID;
			if (DB.getSQLValue(get_TrxName(), sql) > 0) {
				return true;
			}
		}	
		return false;
	}

	/**
	 * 	Before Delete
	 *	@return true if it can be deleted
	 */
	protected boolean beforeDelete ()
	{
		//	R/O Check - Something delivered. etc.
		if (Env.ZERO.compareTo(getQtyDelivered()) != 0)
		{
			log.saveError("DeleteError", Msg.translate(getCtx(), "QtyDelivered") + "=" + getQtyDelivered());
			return false;
		}
		if (Env.ZERO.compareTo(getQtyInvoiced()) != 0)
		{
			log.saveError("DeleteError", Msg.translate(getCtx(), "QtyInvoiced") + "=" + getQtyInvoiced());
			return false;
		}
		if (Env.ZERO.compareTo(getQtyReserved()) != 0)
		{
			//	For PO should be On Order
			log.saveError("DeleteError", Msg.translate(getCtx(), "QtyReserved") + "=" + getQtyReserved());
			return false;
		}
		
		// UnLink All Requisitions
		MRequisitionLine.unlinkC_OrderLine_ID(getCtx(), get_ID(), get_TrxName());
		
		return true;
	}	//	beforeDelete
	
	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return saved
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (!success)
			return success;
		if (!newRecord && is_ValueChanged("C_Tax_ID"))
		{
			//	Recalculate Tax for old Tax
			if (!getParent().isProcessed())
				if (!updateOrderTax(true))
					return false;
		}
		// FIX 2026-07-05 (FG): bloque ininoles eliminado.
		// Usaba SUM(QTYENTERED) con DocStatus='DR' — generaba QtyUsed > Qty -> DISPONIBLE negativo.
		// QtyUsed/QtyReserved los maneja ModWindsorUpdateReserved (ORDER_AFTER_COMPLETE/REACTIVATE).

		return updateHeaderTax();
	}	//	afterSave

	/**
	 * 	After Delete
	 *	@param success success
	 *	@return deleted
	 */
	protected boolean afterDelete (boolean success)
	{
		if (!success)
			return success;
		if (getS_ResourceAssignment_ID() != 0)
		{
			MResourceAssignment ra = new MResourceAssignment(getCtx(), getS_ResourceAssignment_ID(), get_TrxName());
			ra.delete(true);
		}
		
		// FIX 2026-07-05 (FG): bloque ininoles eliminado (mismo fix que afterSave).

		return updateHeaderTax();
	}	//	afterDelete
	
	/**
	 * Recalculate order tax
	 * @param oldTax true if the old C_Tax_ID should be used
	 * @return true if success, false otherwise
	 * 
	 * @author teo_sarca [ 1583825 ]
	 */
	private boolean updateOrderTax(boolean oldTax) {
		MOrderTax tax = MOrderTax.get (this, getPrecision(), oldTax, get_TrxName());
		if (tax != null) {
			if (!tax.calculateTaxFromLines())
				return false;
			if (tax.getTaxAmt().signum() != 0) {
				if (!tax.save(get_TrxName()))
					return false;
			}
			else {
				if (!tax.is_new() && !tax.delete(false, get_TrxName()))
					return false;
			}
		}
		return true;
	}
	
	/**
	 *	Update Tax & Header
	 *	@return true if header updated
	 */
	private boolean updateHeaderTax()
	{
		//	Recalculate Tax for this Tax
		if (!getParent().isProcessed())
			if (!updateOrderTax(false))
				return false;
		
		//	Update Order Header
		String sql = "UPDATE C_Order i"
			+ " SET TotalLines="
				+ "(SELECT COALESCE(SUM(LineNetAmt),0) FROM C_OrderLine il WHERE i.C_Order_ID=il.C_Order_ID) "
			+ "WHERE C_Order_ID=" + getC_Order_ID();
		int no = DB.executeUpdate(sql, get_TrxName());
		if (no != 1)
			log.warning("(1) #" + no);

		if (isTaxIncluded())
			sql = "UPDATE C_Order i "
				+ " SET GrandTotal=TotalLines "
				+ "WHERE C_Order_ID=" + getC_Order_ID();
		else
			sql = "UPDATE C_Order i "
				+ " SET GrandTotal=TotalLines+"
					+ "(SELECT COALESCE(SUM(TaxAmt),0) FROM C_OrderTax it WHERE i.C_Order_ID=it.C_Order_ID) "
					+ "WHERE C_Order_ID=" + getC_Order_ID();
		no = DB.executeUpdate(sql, get_TrxName());
		if (no != 1)
			log.warning("(2) #" + no);
		m_parent = null;
		return no == 1;
	}	//	updateHeaderTax
	
	/**
	 * faaguilar OFB
	 * retorna si la linea es de una orden de venta o compra*/
	private boolean IsSoTrx()
	{
		
		String issotrx=DB.getSQLValueString(null,"select issotrx from C_Order where C_Order_ID=?",getC_Order_ID());
		if(issotrx==null)
		  return false;
		
		if(issotrx.equals("Y"))
		  return true;
		else
		  return false;
		
	}
	
	/**
	 * faaguilar OFB 
	 * custom methods 
	 * active range for receipt*/
	protected boolean isRangeActive()
	{
		MClient client = MClient.get(Env.getCtx());
		
		if(client.get_ValueAsInt("RangeReceiptLow")>0 && client.get_ValueAsInt("RangeReceiptHi")>0)
			return true;
		else
			return false;
	}
}	//	MOrderLine

