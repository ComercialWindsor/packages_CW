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
import org.compiere.util.KeyNamePair;

/** Generated Model for M_PRODUCT_LINEA
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_M_PRODUCT_LINEA extends PO implements I_M_PRODUCT_LINEA, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200306L;

    /** Standard Constructor */
    public X_M_PRODUCT_LINEA (Properties ctx, int M_PRODUCT_LINEA_ID, String trxName)
    {
      super (ctx, M_PRODUCT_LINEA_ID, trxName);
      /** if (M_PRODUCT_LINEA_ID == 0)
        {
			setM_PRODUCT_LINEA_ID (0);
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_M_PRODUCT_LINEA (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_M_PRODUCT_LINEA[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Agrupacion.
		@param Agrupacion Agrupacion	  */
	public void setAgrupacion (boolean Agrupacion)
	{
		set_Value (COLUMNNAME_Agrupacion, Boolean.valueOf(Agrupacion));
	}

	/** Get Agrupacion.
		@return Agrupacion	  */
	public boolean isAgrupacion () 
	{
		Object oo = get_Value(COLUMNNAME_Agrupacion);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Categoria.
		@param Categoria Categoria	  */
	public void setCategoria (boolean Categoria)
	{
		set_Value (COLUMNNAME_Categoria, Boolean.valueOf(Categoria));
	}

	/** Get Categoria.
		@return Categoria	  */
	public boolean isCategoria () 
	{
		Object oo = get_Value(COLUMNNAME_Categoria);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Color.
		@param Color Color	  */
	public void setColor (boolean Color)
	{
		set_Value (COLUMNNAME_Color, Boolean.valueOf(Color));
	}

	/** Get Color.
		@return Color	  */
	public boolean isColor () 
	{
		Object oo = get_Value(COLUMNNAME_Color);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
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

	/** Set Diseno.
		@param Diseno Diseno	  */
	public void setDiseno (boolean Diseno)
	{
		set_Value (COLUMNNAME_Diseno, Boolean.valueOf(Diseno));
	}

	/** Get Diseno.
		@return Diseno	  */
	public boolean isDiseno () 
	{
		Object oo = get_Value(COLUMNNAME_Diseno);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Mark.
		@param Mark Mark	  */
	public void setMark (boolean Mark)
	{
		set_Value (COLUMNNAME_Mark, Boolean.valueOf(Mark));
	}

	/** Get Mark.
		@return Mark	  */
	public boolean isMark () 
	{
		Object oo = get_Value(COLUMNNAME_Mark);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Medida.
		@param Medida Medida	  */
	public void setMedida (boolean Medida)
	{
		set_Value (COLUMNNAME_Medida, Boolean.valueOf(Medida));
	}

	/** Get Medida.
		@return Medida	  */
	public boolean isMedida () 
	{
		Object oo = get_Value(COLUMNNAME_Medida);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set M_PRODUCT_LINEA.
		@param M_PRODUCT_LINEA_ID M_PRODUCT_LINEA	  */
	public void setM_PRODUCT_LINEA_ID (int M_PRODUCT_LINEA_ID)
	{
		if (M_PRODUCT_LINEA_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_PRODUCT_LINEA_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_PRODUCT_LINEA_ID, Integer.valueOf(M_PRODUCT_LINEA_ID));
	}

	/** Get M_PRODUCT_LINEA.
		@return M_PRODUCT_LINEA	  */
	public int getM_PRODUCT_LINEA_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_PRODUCT_LINEA_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }

	/** Set Proyeccion.
		@param Proyeccion Proyeccion	  */
	public void setProyeccion (boolean Proyeccion)
	{
		set_Value (COLUMNNAME_Proyeccion, Boolean.valueOf(Proyeccion));
	}

	/** Get Proyeccion.
		@return Proyeccion	  */
	public boolean isProyeccion () 
	{
		Object oo = get_Value(COLUMNNAME_Proyeccion);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Reposicion.
		@param Reposicion Reposicion	  */
	public void setReposicion (boolean Reposicion)
	{
		set_Value (COLUMNNAME_Reposicion, Boolean.valueOf(Reposicion));
	}

	/** Get Reposicion.
		@return Reposicion	  */
	public boolean isReposicion () 
	{
		Object oo = get_Value(COLUMNNAME_Reposicion);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
}