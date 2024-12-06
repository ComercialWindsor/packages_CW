 package org.windsor.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;
import org.compiere.model.*;

/** Generated Interface for C_OrderB2CLine
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS
 */
public interface I_C_OrderB2CLine 
{

    /** TableName=C_OrderB2CLine */
    public static final String Table_Name = "C_OrderB2CLine";

    /** AD_Table_ID=1000658 */
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

    /** Column name CODIGOGENERICO */
    public static final String COLUMNNAME_CODIGOGENERICO = "CODIGOGENERICO";

	/** Set CODIGOGENERICO	  */
	public void setCODIGOGENERICO (String CODIGOGENERICO);

	/** Get CODIGOGENERICO	  */
	public String getCODIGOGENERICO();

    /** Column name C_OrderB2C_ID */
    public static final String COLUMNNAME_C_OrderB2C_ID = "C_OrderB2C_ID";

	/** Set C_OrderB2C_ID	  */
	public void setC_OrderB2C_ID (int C_OrderB2C_ID);

	/** Get C_OrderB2C_ID	  */
	public int getC_OrderB2C_ID();

	public I_C_OrderB2C getC_OrderB2C() throws RuntimeException;

    /** Column name C_OrderB2CLine_ID */
    public static final String COLUMNNAME_C_OrderB2CLine_ID = "C_OrderB2CLine_ID";

	/** Set C_OrderB2CLine_ID	  */
	public void setC_OrderB2CLine_ID (int C_OrderB2CLine_ID);

	/** Get C_OrderB2CLine_ID	  */
	public int getC_OrderB2CLine_ID();

    /** Column name C_Order_ID */
    public static final String COLUMNNAME_C_Order_ID = "C_Order_ID";

	/** Set Order.
	  * Order
	  */
	public void setC_Order_ID (int C_Order_ID);

	/** Get Order.
	  * Order
	  */
	public int getC_Order_ID();

	public I_C_Order getC_Order() throws RuntimeException;

    /** Column name C_OrderLine_ID */
    public static final String COLUMNNAME_C_OrderLine_ID = "C_OrderLine_ID";

	/** Set Sales Order Line.
	  * Sales Order Line
	  */
	public void setC_OrderLine_ID (int C_OrderLine_ID);

	/** Get Sales Order Line.
	  * Sales Order Line
	  */
	public int getC_OrderLine_ID();

	public I_C_OrderLine getC_OrderLine() throws RuntimeException;

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

    /** Column name ErrorMsg */
    public static final String COLUMNNAME_ErrorMsg = "ErrorMsg";

	/** Set Error Msg	  */
	public void setErrorMsg (String ErrorMsg);

	/** Get Error Msg	  */
	public String getErrorMsg();

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

    /** Column name Line */
    public static final String COLUMNNAME_Line = "Line";

	/** Set Line No.
	  * Unique line for this document
	  */
	public void setLine (int Line);

	/** Get Line No.
	  * Unique line for this document
	  */
	public int getLine();

    /** Column name LineNetAmt */
    public static final String COLUMNNAME_LineNetAmt = "LineNetAmt";

	/** Set Line Amount.
	  * Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	public void setLineNetAmt (BigDecimal LineNetAmt);

	/** Get Line Amount.
	  * Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	public BigDecimal getLineNetAmt();

    /** Column name M_Product_ID */
    public static final String COLUMNNAME_M_Product_ID = "M_Product_ID";

	/** Set Product.
	  * Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID);

	/** Get Product.
	  * Product, Service, Item
	  */
	public int getM_Product_ID();

	public I_M_Product getM_Product() throws RuntimeException;

    /** Column name PASARAOV */
    public static final String COLUMNNAME_PASARAOV = "PASARAOV";

	/** Set PASARAOV	  */
	public void setPASARAOV (boolean PASARAOV);

	/** Get PASARAOV	  */
	public boolean isPASARAOV();

    /** Column name PriceEntered */
    public static final String COLUMNNAME_PriceEntered = "PriceEntered";

	/** Set Price.
	  * Price Entered - the price based on the selected/base UoM
	  */
	public void setPriceEntered (int PriceEntered);

	/** Get Price.
	  * Price Entered - the price based on the selected/base UoM
	  */
	public int getPriceEntered();

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name ProductValue */
    public static final String COLUMNNAME_ProductValue = "ProductValue";

	/** Set Product Key.
	  * Key of the Product
	  */
	public void setProductValue (String ProductValue);

	/** Get Product Key.
	  * Key of the Product
	  */
	public String getProductValue();

    /** Column name QtyEntered */
    public static final String COLUMNNAME_QtyEntered = "QtyEntered";

	/** Set Quantity.
	  * The Quantity Entered is based on the selected UoM
	  */
	public void setQtyEntered (BigDecimal QtyEntered);

	/** Get Quantity.
	  * The Quantity Entered is based on the selected UoM
	  */
	public BigDecimal getQtyEntered();

    /** Column name StockBodegas */
    public static final String COLUMNNAME_StockBodegas = "StockBodegas";

	/** Set StockBodegas	  */
	public void setStockBodegas (BigDecimal StockBodegas);

	/** Get StockBodegas	  */
	public BigDecimal getStockBodegas();

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
}
