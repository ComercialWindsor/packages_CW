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
package org.windsor.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;

import org.compiere.util.KeyNamePair;

/** Generated Interface for M_ReposicionOrgProduct
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS
 */
public interface I_M_ReposicionOrgProduct 
{

    /** TableName=M_ReposicionOrgProduct */
    public static final String Table_Name = "M_ReposicionOrgProduct";

    /** AD_Table_ID=1000094 */
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

    /** Column name APROBADO */
    public static final String COLUMNNAME_APROBADO = "APROBADO";

	/** Set APROBADO	  */
	public void setAPROBADO (boolean APROBADO);

	/** Get APROBADO	  */
	public boolean isAPROBADO();

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

    /** Column name GENERADANOTA */
    public static final String COLUMNNAME_GENERADANOTA = "GENERADANOTA";

	/** Set GENERADANOTA	  */
	public void setGENERADANOTA (boolean GENERADANOTA);

	/** Get GENERADANOTA	  */
	public boolean isGENERADANOTA();

    /** Column name M_Product_Category_ID */
    public static final String COLUMNNAME_M_Product_Category_ID = "M_Product_Category_ID";

	/** Set Product Category.
	  * Category of a Product
	  */
	public void setM_Product_Category_ID (int M_Product_Category_ID);

	/** Get Product Category.
	  * Category of a Product
	  */
	public int getM_Product_Category_ID();

	public I_M_Product_Category getM_Product_Category() throws RuntimeException;

    /** Column name M_PRODUCT_FAMILY_ID */
    public static final String COLUMNNAME_M_PRODUCT_FAMILY_ID = "M_PRODUCT_FAMILY_ID";

	/** Set M_PRODUCT_FAMILY	  */
	public void setM_PRODUCT_FAMILY_ID (int M_PRODUCT_FAMILY_ID);

	/** Get M_PRODUCT_FAMILY	  */
	public int getM_PRODUCT_FAMILY_ID();

	public I_M_PRODUCT_FAMILY getM_PRODUCT_FAMILY() throws RuntimeException;

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

    /** Column name M_PRODUCT_LINEA_ID */
    public static final String COLUMNNAME_M_PRODUCT_LINEA_ID = "M_PRODUCT_LINEA_ID";

	/** Set M_PRODUCT_LINEA	  */
	public void setM_PRODUCT_LINEA_ID (int M_PRODUCT_LINEA_ID);

	/** Get M_PRODUCT_LINEA	  */
	public int getM_PRODUCT_LINEA_ID();

	public I_M_PRODUCT_LINEA getM_PRODUCT_LINEA() throws RuntimeException;

    /** Column name M_ReposicionOrg_ID */
    public static final String COLUMNNAME_M_ReposicionOrg_ID = "M_ReposicionOrg_ID";

	/** Set M_ReposicionOrg_ID	  */
	public void setM_ReposicionOrg_ID (int M_ReposicionOrg_ID);

	/** Get M_ReposicionOrg_ID	  */
	public int getM_ReposicionOrg_ID();

	public I_M_ReposicionOrg getM_ReposicionOrg() throws RuntimeException;

    /** Column name M_ReposicionOrgProduct_ID */
    public static final String COLUMNNAME_M_ReposicionOrgProduct_ID = "M_ReposicionOrgProduct_ID";

	/** Set M_ReposicionOrgProduct_ID	  */
	public void setM_ReposicionOrgProduct_ID (int M_ReposicionOrgProduct_ID);

	/** Get M_ReposicionOrgProduct_ID	  */
	public int getM_ReposicionOrgProduct_ID();

    /** Column name OVWINDOR */
    public static final String COLUMNNAME_OVWINDOR = "OVWINDOR";

	/** Set OVWINDOR	  */
	public void setOVWINDOR (String OVWINDOR);

	/** Get OVWINDOR	  */
	public String getOVWINDOR();

    /** Column name PEDIDO */
    public static final String COLUMNNAME_PEDIDO = "PEDIDO";

	/** Set PEDIDO	  */
	public void setPEDIDO (int PEDIDO);

	/** Get PEDIDO	  */
	public int getPEDIDO();

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

    /** Column name QtyOnHand */
    public static final String COLUMNNAME_QtyOnHand = "QtyOnHand";

	/** Set On Hand Quantity.
	  * On Hand Quantity
	  */
	public void setQtyOnHand (BigDecimal QtyOnHand);

	/** Get On Hand Quantity.
	  * On Hand Quantity
	  */
	public BigDecimal getQtyOnHand();

    /** Column name QTYONHANDWINDSOR */
    public static final String COLUMNNAME_QTYONHANDWINDSOR = "QTYONHANDWINDSOR";

	/** Set QTYONHANDWINDSOR	  */
	public void setQTYONHANDWINDSOR (BigDecimal QTYONHANDWINDSOR);

	/** Get QTYONHANDWINDSOR	  */
	public BigDecimal getQTYONHANDWINDSOR();

    /** Column name SUGERIDO */
    public static final String COLUMNNAME_SUGERIDO = "SUGERIDO";

	/** Set SUGERIDO	  */
	public void setSUGERIDO (int SUGERIDO);

	/** Get SUGERIDO	  */
	public int getSUGERIDO();

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

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();

    /** Column name VENTAS1 */
    public static final String COLUMNNAME_VENTAS1 = "VENTAS1";

	/** Set VENTAS1	  */
	public void setVENTAS1 (int VENTAS1);

	/** Get VENTAS1	  */
	public int getVENTAS1();

    /** Column name VENTAS2 */
    public static final String COLUMNNAME_VENTAS2 = "VENTAS2";

	/** Set VENTAS2	  */
	public void setVENTAS2 (int VENTAS2);

	/** Get VENTAS2	  */
	public int getVENTAS2();

    /** Column name VENTAS3 */
    public static final String COLUMNNAME_VENTAS3 = "VENTAS3";

	/** Set VENTAS3	  */
	public void setVENTAS3 (int VENTAS3);

	/** Get VENTAS3	  */
	public int getVENTAS3();

    /** Column name VENTAS4 */
    public static final String COLUMNNAME_VENTAS4 = "VENTAS4";

	/** Set VENTAS4	  */
	public void setVENTAS4 (int VENTAS4);

	/** Get VENTAS4	  */
	public int getVENTAS4();
}
