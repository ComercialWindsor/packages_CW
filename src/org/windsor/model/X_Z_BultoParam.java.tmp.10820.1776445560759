/** Generated Model - DO NOT CHANGE */
package org.windsor.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for Z_BultoParam
 *  @author Windsor (generated)
 *  @version Release 3.6.0LTS */
public class X_Z_BultoParam extends PO implements I_Persistent
{
	private static final long serialVersionUID = 20260417L;

	public X_Z_BultoParam (Properties ctx, int Z_BultoParam_ID, String trxName)
	{
		super (ctx, Z_BultoParam_ID, trxName);
		if (Z_BultoParam_ID == 0)
		{
			setIsActive(true);
			setPesoMaxKg(new BigDecimal("30"));
		}
	}

	public X_Z_BultoParam (Properties ctx, ResultSet rs, String trxName)
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
		StringBuffer sb = new StringBuffer ("X_Z_BultoParam[")
			.append(get_ID()).append("]");
		return sb.toString();
	}

	public static final String Table_Name = "Z_BultoParam";
	public static final int Table_ID = MTable.getTable_ID(Table_Name);
	public static final BigDecimal accessLevel = BigDecimal.valueOf(3);

	public static final String COLUMNNAME_Z_BultoParam_ID       = "Z_BultoParam_ID";
	public static final String COLUMNNAME_Name                  = "Name";
	public static final String COLUMNNAME_Zona                  = "Zona";
	public static final String COLUMNNAME_M_ProductCategory_ID  = "M_Product_Category_ID";
	public static final String COLUMNNAME_PesoMaxKg             = "PesoMaxKg";
	public static final String COLUMNNAME_Description           = "Description";

	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}
	public String getName ()
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	public void setZona (String Zona)
	{
		set_Value (COLUMNNAME_Zona, Zona);
	}
	public String getZona ()
	{
		return (String)get_Value(COLUMNNAME_Zona);
	}

	public void setM_ProductCategory_ID (int M_ProductCategory_ID)
	{
		if (M_ProductCategory_ID < 1) set_Value (COLUMNNAME_M_ProductCategory_ID, null);
		else set_Value (COLUMNNAME_M_ProductCategory_ID, Integer.valueOf(M_ProductCategory_ID));
	}
	public int getM_ProductCategory_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_ProductCategory_ID);
		if (ii == null) return 0;
		return ii.intValue();
	}

	public void setPesoMaxKg (BigDecimal PesoMaxKg)
	{
		set_Value (COLUMNNAME_PesoMaxKg, PesoMaxKg);
	}
	public BigDecimal getPesoMaxKg ()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PesoMaxKg);
		if (bd == null) return new BigDecimal("30");
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
}
