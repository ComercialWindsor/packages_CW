/** Generated Model - DO NOT CHANGE */
package org.windsor.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for C_BultoLine
 *  @author Windsor (generated)
 *  @version Release 3.6.0LTS */
public class X_C_BultoLine extends PO implements I_Persistent
{
	private static final long serialVersionUID = 20260417L;

	public X_C_BultoLine (Properties ctx, int C_BultoLine_ID, String trxName)
	{
		super (ctx, C_BultoLine_ID, trxName);
		if (C_BultoLine_ID == 0)
		{
			setIsActive(true);
			setLine(0);
			setQtyEntered(Env.ZERO);
		}
	}

	public X_C_BultoLine (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
	}

	protected int get_AccessLevel()
	{
		return accessLevel.intValue();
	}

	protected POInfo initPO (Properties ctx)
	{
		POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
		return poi;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer ("X_C_BultoLine[")
			.append(get_ID()).append("]");
		return sb.toString();
	}

	public static final String Table_Name = "C_BultoLine";
	public static final int Table_ID = MTable.getTable_ID(Table_Name);
	public static final BigDecimal accessLevel = BigDecimal.valueOf(3);

	public static final String COLUMNNAME_C_BultoLine_ID = "C_BultoLine_ID";
	public static final String COLUMNNAME_C_Bulto_ID     = "C_Bulto_ID";
	public static final String COLUMNNAME_Line           = "Line";
	public static final String COLUMNNAME_M_Product_ID   = "M_Product_ID";
	public static final String COLUMNNAME_QtyEntered     = "QtyEntered";
	public static final String COLUMNNAME_Description    = "Description";
	public static final String COLUMNNAME_C_Order_ID     = "C_Order_ID";
	public static final String COLUMNNAME_Value          = "Value";

	public void setC_Bulto_ID (int C_Bulto_ID)
	{
		set_Value (COLUMNNAME_C_Bulto_ID, Integer.valueOf(C_Bulto_ID));
	}
	public int getC_Bulto_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Bulto_ID);
		if (ii == null) return 0;
		return ii.intValue();
	}

	public void setLine (int Line)
	{
		set_Value (COLUMNNAME_Line, Integer.valueOf(Line));
	}
	public int getLine ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
		if (ii == null) return 0;
		return ii.intValue();
	}

	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) set_Value (COLUMNNAME_M_Product_ID, null);
		else set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}
	public int getM_Product_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null) return 0;
		return ii.intValue();
	}

	public void setQtyEntered (BigDecimal QtyEntered)
	{
		set_Value (COLUMNNAME_QtyEntered, QtyEntered);
	}
	public BigDecimal getQtyEntered ()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyEntered);
		if (bd == null) return Env.ZERO;
		return bd;
	}

	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}
	public String getDescription ()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	public void setC_Order_ID (int C_Order_ID)
	{
		if (C_Order_ID < 1) set_Value (COLUMNNAME_C_Order_ID, null);
		else set_Value (COLUMNNAME_C_Order_ID, Integer.valueOf(C_Order_ID));
	}
	public int getC_Order_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Order_ID);
		if (ii == null) return 0;
		return ii.intValue();
	}

	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}
	public String getValue ()
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}
