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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for T_CuadroShopiFlow
 *  @author Adempiere (generated) 
 *  @version Release 3.6.0LTS - $Id$ */
public class X_T_CuadroShopiFlow extends PO implements I_T_CuadroShopiFlow, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20240722L;

    /** Standard Constructor */
    public X_T_CuadroShopiFlow (Properties ctx, int T_CuadroShopiFlow_ID, String trxName)
    {
      super (ctx, T_CuadroShopiFlow_ID, trxName);
      /** if (T_CuadroShopiFlow_ID == 0)
        {
			setDateAcct (new Timestamp( System.currentTimeMillis() ));
// @#Date@-1
			setFABRICSFLOWNETO (Env.ZERO);
			setFABRICSFLOWQTY (Env.ZERO);
			setFABRICSHOPNETO (Env.ZERO);
			setFABRICSHOPQTY (Env.ZERO);
			setMASHINIFLOWNETO (Env.ZERO);
			setMASHINIFLOWQTY (Env.ZERO);
			setMASHINISHOPNETO (Env.ZERO);
			setMASHINISHOPQTY (Env.ZERO);
			setT_CuadroShopiFlow_ID (0);
			setWINDSORFLOWNETO (Env.ZERO);
			setWINDSORFLOWQTY (Env.ZERO);
			setWINDSORSHOPNETO (Env.ZERO);
			setWINDSORSHOPQTY (Env.ZERO);
        } */
    }

    /** Load Constructor */
    public X_T_CuadroShopiFlow (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_T_CuadroShopiFlow[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Account Date.
		@param DateAcct 
		Accounting Date
	  */
	public void setDateAcct (Timestamp DateAcct)
	{
		set_Value (COLUMNNAME_DateAcct, DateAcct);
	}

	/** Get Account Date.
		@return Accounting Date
	  */
	public Timestamp getDateAcct () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateAcct);
	}

	/** Set FABRICMPNETO.
		@param FABRICMPNETO FABRICMPNETO	  */
	public void setFABRICMPNETO (BigDecimal FABRICMPNETO)
	{
		set_Value (COLUMNNAME_FABRICMPNETO, FABRICMPNETO);
	}

	/** Get FABRICMPNETO.
		@return FABRICMPNETO	  */
	public BigDecimal getFABRICMPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICMPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICMPQTY.
		@param FABRICMPQTY FABRICMPQTY	  */
	public void setFABRICMPQTY (BigDecimal FABRICMPQTY)
	{
		set_Value (COLUMNNAME_FABRICMPQTY, FABRICMPQTY);
	}

	/** Get FABRICMPQTY.
		@return FABRICMPQTY	  */
	public BigDecimal getFABRICMPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICMPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSDIFNETO.
		@param FABRICSDIFNETO FABRICSDIFNETO	  */
	public void setFABRICSDIFNETO (BigDecimal FABRICSDIFNETO)
	{
		set_Value (COLUMNNAME_FABRICSDIFNETO, FABRICSDIFNETO);
	}

	/** Get FABRICSDIFNETO.
		@return FABRICSDIFNETO	  */
	public BigDecimal getFABRICSDIFNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSDIFNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSDIFQTY.
		@param FABRICSDIFQTY FABRICSDIFQTY	  */
	public void setFABRICSDIFQTY (BigDecimal FABRICSDIFQTY)
	{
		set_Value (COLUMNNAME_FABRICSDIFQTY, FABRICSDIFQTY);
	}

	/** Get FABRICSDIFQTY.
		@return FABRICSDIFQTY	  */
	public BigDecimal getFABRICSDIFQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSDIFQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSFLOWNETO.
		@param FABRICSFLOWNETO FABRICSFLOWNETO	  */
	public void setFABRICSFLOWNETO (BigDecimal FABRICSFLOWNETO)
	{
		set_Value (COLUMNNAME_FABRICSFLOWNETO, FABRICSFLOWNETO);
	}

	/** Get FABRICSFLOWNETO.
		@return FABRICSFLOWNETO	  */
	public BigDecimal getFABRICSFLOWNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSFLOWNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSFLOWQTY.
		@param FABRICSFLOWQTY FABRICSFLOWQTY	  */
	public void setFABRICSFLOWQTY (BigDecimal FABRICSFLOWQTY)
	{
		set_Value (COLUMNNAME_FABRICSFLOWQTY, FABRICSFLOWQTY);
	}

	/** Get FABRICSFLOWQTY.
		@return FABRICSFLOWQTY	  */
	public BigDecimal getFABRICSFLOWQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSFLOWQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSHOPNETO.
		@param FABRICSHOPNETO FABRICSHOPNETO	  */
	public void setFABRICSHOPNETO (BigDecimal FABRICSHOPNETO)
	{
		set_Value (COLUMNNAME_FABRICSHOPNETO, FABRICSHOPNETO);
	}

	/** Get FABRICSHOPNETO.
		@return FABRICSHOPNETO	  */
	public BigDecimal getFABRICSHOPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSHOPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSHOPQTY.
		@param FABRICSHOPQTY FABRICSHOPQTY	  */
	public void setFABRICSHOPQTY (BigDecimal FABRICSHOPQTY)
	{
		set_Value (COLUMNNAME_FABRICSHOPQTY, FABRICSHOPQTY);
	}

	/** Get FABRICSHOPQTY.
		@return FABRICSHOPQTY	  */
	public BigDecimal getFABRICSHOPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSHOPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSVPNETO.
		@param FABRICSVPNETO FABRICSVPNETO	  */
	public void setFABRICSVPNETO (BigDecimal FABRICSVPNETO)
	{
		set_Value (COLUMNNAME_FABRICSVPNETO, FABRICSVPNETO);
	}

	/** Get FABRICSVPNETO.
		@return FABRICSVPNETO	  */
	public BigDecimal getFABRICSVPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSVPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set FABRICSVPQTY.
		@param FABRICSVPQTY FABRICSVPQTY	  */
	public void setFABRICSVPQTY (BigDecimal FABRICSVPQTY)
	{
		set_Value (COLUMNNAME_FABRICSVPQTY, FABRICSVPQTY);
	}

	/** Get FABRICSVPQTY.
		@return FABRICSVPQTY	  */
	public BigDecimal getFABRICSVPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FABRICSVPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIDIFNETO.
		@param MASHINIDIFNETO MASHINIDIFNETO	  */
	public void setMASHINIDIFNETO (BigDecimal MASHINIDIFNETO)
	{
		set_Value (COLUMNNAME_MASHINIDIFNETO, MASHINIDIFNETO);
	}

	/** Get MASHINIDIFNETO.
		@return MASHINIDIFNETO	  */
	public BigDecimal getMASHINIDIFNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIDIFNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIDIFQTY.
		@param MASHINIDIFQTY MASHINIDIFQTY	  */
	public void setMASHINIDIFQTY (BigDecimal MASHINIDIFQTY)
	{
		set_Value (COLUMNNAME_MASHINIDIFQTY, MASHINIDIFQTY);
	}

	/** Get MASHINIDIFQTY.
		@return MASHINIDIFQTY	  */
	public BigDecimal getMASHINIDIFQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIDIFQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIFLOWNETO.
		@param MASHINIFLOWNETO MASHINIFLOWNETO	  */
	public void setMASHINIFLOWNETO (BigDecimal MASHINIFLOWNETO)
	{
		set_Value (COLUMNNAME_MASHINIFLOWNETO, MASHINIFLOWNETO);
	}

	/** Get MASHINIFLOWNETO.
		@return MASHINIFLOWNETO	  */
	public BigDecimal getMASHINIFLOWNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIFLOWNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIFLOWQTY.
		@param MASHINIFLOWQTY MASHINIFLOWQTY	  */
	public void setMASHINIFLOWQTY (BigDecimal MASHINIFLOWQTY)
	{
		set_Value (COLUMNNAME_MASHINIFLOWQTY, MASHINIFLOWQTY);
	}

	/** Get MASHINIFLOWQTY.
		@return MASHINIFLOWQTY	  */
	public BigDecimal getMASHINIFLOWQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIFLOWQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIMPNETO.
		@param MASHINIMPNETO MASHINIMPNETO	  */
	public void setMASHINIMPNETO (BigDecimal MASHINIMPNETO)
	{
		set_Value (COLUMNNAME_MASHINIMPNETO, MASHINIMPNETO);
	}

	/** Get MASHINIMPNETO.
		@return MASHINIMPNETO	  */
	public BigDecimal getMASHINIMPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIMPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIMPQTY.
		@param MASHINIMPQTY MASHINIMPQTY	  */
	public void setMASHINIMPQTY (BigDecimal MASHINIMPQTY)
	{
		set_Value (COLUMNNAME_MASHINIMPQTY, MASHINIMPQTY);
	}

	/** Get MASHINIMPQTY.
		@return MASHINIMPQTY	  */
	public BigDecimal getMASHINIMPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIMPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINISHOPNETO.
		@param MASHINISHOPNETO MASHINISHOPNETO	  */
	public void setMASHINISHOPNETO (BigDecimal MASHINISHOPNETO)
	{
		set_Value (COLUMNNAME_MASHINISHOPNETO, MASHINISHOPNETO);
	}

	/** Get MASHINISHOPNETO.
		@return MASHINISHOPNETO	  */
	public BigDecimal getMASHINISHOPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINISHOPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINISHOPQTY.
		@param MASHINISHOPQTY MASHINISHOPQTY	  */
	public void setMASHINISHOPQTY (BigDecimal MASHINISHOPQTY)
	{
		set_Value (COLUMNNAME_MASHINISHOPQTY, MASHINISHOPQTY);
	}

	/** Get MASHINISHOPQTY.
		@return MASHINISHOPQTY	  */
	public BigDecimal getMASHINISHOPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINISHOPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIVPNETO.
		@param MASHINIVPNETO MASHINIVPNETO	  */
	public void setMASHINIVPNETO (BigDecimal MASHINIVPNETO)
	{
		set_Value (COLUMNNAME_MASHINIVPNETO, MASHINIVPNETO);
	}

	/** Get MASHINIVPNETO.
		@return MASHINIVPNETO	  */
	public BigDecimal getMASHINIVPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIVPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set MASHINIVPQTY.
		@param MASHINIVPQTY MASHINIVPQTY	  */
	public void setMASHINIVPQTY (BigDecimal MASHINIVPQTY)
	{
		set_Value (COLUMNNAME_MASHINIVPQTY, MASHINIVPQTY);
	}

	/** Get MASHINIVPQTY.
		@return MASHINIVPQTY	  */
	public BigDecimal getMASHINIVPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_MASHINIVPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set T_CuadroShopiFlow_ID.
		@param T_CuadroShopiFlow_ID T_CuadroShopiFlow_ID	  */
	public void setT_CuadroShopiFlow_ID (int T_CuadroShopiFlow_ID)
	{
		if (T_CuadroShopiFlow_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_T_CuadroShopiFlow_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_T_CuadroShopiFlow_ID, Integer.valueOf(T_CuadroShopiFlow_ID));
	}

	/** Get T_CuadroShopiFlow_ID.
		@return T_CuadroShopiFlow_ID	  */
	public int getT_CuadroShopiFlow_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_T_CuadroShopiFlow_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set WINDSORDIFNETO.
		@param WINDSORDIFNETO WINDSORDIFNETO	  */
	public void setWINDSORDIFNETO (BigDecimal WINDSORDIFNETO)
	{
		set_Value (COLUMNNAME_WINDSORDIFNETO, WINDSORDIFNETO);
	}

	/** Get WINDSORDIFNETO.
		@return WINDSORDIFNETO	  */
	public BigDecimal getWINDSORDIFNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORDIFNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORDIFQTY.
		@param WINDSORDIFQTY WINDSORDIFQTY	  */
	public void setWINDSORDIFQTY (BigDecimal WINDSORDIFQTY)
	{
		set_Value (COLUMNNAME_WINDSORDIFQTY, WINDSORDIFQTY);
	}

	/** Get WINDSORDIFQTY.
		@return WINDSORDIFQTY	  */
	public BigDecimal getWINDSORDIFQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORDIFQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORFLOWNETO.
		@param WINDSORFLOWNETO WINDSORFLOWNETO	  */
	public void setWINDSORFLOWNETO (BigDecimal WINDSORFLOWNETO)
	{
		set_Value (COLUMNNAME_WINDSORFLOWNETO, WINDSORFLOWNETO);
	}

	/** Get WINDSORFLOWNETO.
		@return WINDSORFLOWNETO	  */
	public BigDecimal getWINDSORFLOWNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORFLOWNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORFLOWQTY.
		@param WINDSORFLOWQTY WINDSORFLOWQTY	  */
	public void setWINDSORFLOWQTY (BigDecimal WINDSORFLOWQTY)
	{
		set_Value (COLUMNNAME_WINDSORFLOWQTY, WINDSORFLOWQTY);
	}

	/** Get WINDSORFLOWQTY.
		@return WINDSORFLOWQTY	  */
	public BigDecimal getWINDSORFLOWQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORFLOWQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORMPNETO.
		@param WINDSORMPNETO WINDSORMPNETO	  */
	public void setWINDSORMPNETO (BigDecimal WINDSORMPNETO)
	{
		set_Value (COLUMNNAME_WINDSORMPNETO, WINDSORMPNETO);
	}

	/** Get WINDSORMPNETO.
		@return WINDSORMPNETO	  */
	public BigDecimal getWINDSORMPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORMPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORMPQTY.
		@param WINDSORMPQTY WINDSORMPQTY	  */
	public void setWINDSORMPQTY (BigDecimal WINDSORMPQTY)
	{
		set_Value (COLUMNNAME_WINDSORMPQTY, WINDSORMPQTY);
	}

	/** Get WINDSORMPQTY.
		@return WINDSORMPQTY	  */
	public BigDecimal getWINDSORMPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORMPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORSHOPNETO.
		@param WINDSORSHOPNETO WINDSORSHOPNETO	  */
	public void setWINDSORSHOPNETO (BigDecimal WINDSORSHOPNETO)
	{
		set_Value (COLUMNNAME_WINDSORSHOPNETO, WINDSORSHOPNETO);
	}

	/** Get WINDSORSHOPNETO.
		@return WINDSORSHOPNETO	  */
	public BigDecimal getWINDSORSHOPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORSHOPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORSHOPQTY.
		@param WINDSORSHOPQTY WINDSORSHOPQTY	  */
	public void setWINDSORSHOPQTY (BigDecimal WINDSORSHOPQTY)
	{
		set_Value (COLUMNNAME_WINDSORSHOPQTY, WINDSORSHOPQTY);
	}

	/** Get WINDSORSHOPQTY.
		@return WINDSORSHOPQTY	  */
	public BigDecimal getWINDSORSHOPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORSHOPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORVPNETO.
		@param WINDSORVPNETO WINDSORVPNETO	  */
	public void setWINDSORVPNETO (BigDecimal WINDSORVPNETO)
	{
		set_Value (COLUMNNAME_WINDSORVPNETO, WINDSORVPNETO);
	}

	/** Get WINDSORVPNETO.
		@return WINDSORVPNETO	  */
	public BigDecimal getWINDSORVPNETO () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORVPNETO);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set WINDSORVPQTY.
		@param WINDSORVPQTY WINDSORVPQTY	  */
	public void setWINDSORVPQTY (BigDecimal WINDSORVPQTY)
	{
		set_Value (COLUMNNAME_WINDSORVPQTY, WINDSORVPQTY);
	}

	/** Get WINDSORVPQTY.
		@return WINDSORVPQTY	  */
	public BigDecimal getWINDSORVPQTY () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_WINDSORVPQTY);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}