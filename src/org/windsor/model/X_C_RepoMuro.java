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

/** Generated Model for C_RepoMuro
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_C_RepoMuro extends PO implements I_C_RepoMuro, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200306L;

    /** Standard Constructor */
    public X_C_RepoMuro (Properties ctx, int C_RepoMuro_ID, String trxName)
    {
      super (ctx, C_RepoMuro_ID, trxName);
      /** if (C_RepoMuro_ID == 0)
        {
			setC_RepoMuro_ID (0);
			setID_REPOMURO (0);
        } */
    }

    /** Load Constructor */
    public X_C_RepoMuro (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_C_RepoMuro[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

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

	/** Set GENERATE.
		@param GENERATE GENERATE	  */
	public void setGENERATE (String GENERATE)
	{
		set_Value (COLUMNNAME_GENERATE, GENERATE);
	}

	/** Get GENERATE.
		@return GENERATE	  */
	public String getGENERATE () 
	{
		return (String)get_Value(COLUMNNAME_GENERATE);
	}

	/** Set ID_REPOMURO.
		@param ID_REPOMURO ID_REPOMURO	  */
	public void setID_REPOMURO (int ID_REPOMURO)
	{
		set_Value (COLUMNNAME_ID_REPOMURO, Integer.valueOf(ID_REPOMURO));
	}

	/** Get ID_REPOMURO.
		@return ID_REPOMURO	  */
	public int getID_REPOMURO () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ID_REPOMURO);
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
}