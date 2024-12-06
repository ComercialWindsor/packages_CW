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

/** Generated Interface for M_PRODUCT_LINEA
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS
 */
public interface I_M_PRODUCT_LINEA 
{

    /** TableName=M_PRODUCT_LINEA */
    public static final String Table_Name = "M_PRODUCT_LINEA";

    /** AD_Table_ID=1000316 */
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

    /** Column name Agrupacion */
    public static final String COLUMNNAME_Agrupacion = "Agrupacion";

	/** Set Agrupacion	  */
	public void setAgrupacion (boolean Agrupacion);

	/** Get Agrupacion	  */
	public boolean isAgrupacion();

    /** Column name Categoria */
    public static final String COLUMNNAME_Categoria = "Categoria";

	/** Set Categoria	  */
	public void setCategoria (boolean Categoria);

	/** Get Categoria	  */
	public boolean isCategoria();

    /** Column name Color */
    public static final String COLUMNNAME_Color = "Color";

	/** Set Color	  */
	public void setColor (boolean Color);

	/** Get Color	  */
	public boolean isColor();

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

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name Diseno */
    public static final String COLUMNNAME_Diseno = "Diseno";

	/** Set Diseno	  */
	public void setDiseno (boolean Diseno);

	/** Get Diseno	  */
	public boolean isDiseno();

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

    /** Column name Mark */
    public static final String COLUMNNAME_Mark = "Mark";

	/** Set Mark	  */
	public void setMark (boolean Mark);

	/** Get Mark	  */
	public boolean isMark();

    /** Column name Medida */
    public static final String COLUMNNAME_Medida = "Medida";

	/** Set Medida	  */
	public void setMedida (boolean Medida);

	/** Get Medida	  */
	public boolean isMedida();

    /** Column name M_PRODUCT_LINEA_ID */
    public static final String COLUMNNAME_M_PRODUCT_LINEA_ID = "M_PRODUCT_LINEA_ID";

	/** Set M_PRODUCT_LINEA	  */
	public void setM_PRODUCT_LINEA_ID (int M_PRODUCT_LINEA_ID);

	/** Get M_PRODUCT_LINEA	  */
	public int getM_PRODUCT_LINEA_ID();

    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/** Set Name.
	  * Alphanumeric identifier of the entity
	  */
	public void setName (String Name);

	/** Get Name.
	  * Alphanumeric identifier of the entity
	  */
	public String getName();

    /** Column name Proyeccion */
    public static final String COLUMNNAME_Proyeccion = "Proyeccion";

	/** Set Proyeccion	  */
	public void setProyeccion (boolean Proyeccion);

	/** Get Proyeccion	  */
	public boolean isProyeccion();

    /** Column name Reposicion */
    public static final String COLUMNNAME_Reposicion = "Reposicion";

	/** Set Reposicion	  */
	public void setReposicion (boolean Reposicion);

	/** Get Reposicion	  */
	public boolean isReposicion();

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
