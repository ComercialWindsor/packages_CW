/** Generated Model - DO NOT CHANGE */
package org.windsor.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for C_Bulto
 *  @author Windsor (generated)
 *  @version Release 3.6.0LTS */
public class X_C_Bulto extends PO implements I_Persistent
{
	private static final long serialVersionUID = 20260417L;

	public X_C_Bulto (Properties ctx, int C_Bulto_ID, String trxName)
	{
		super (ctx, C_Bulto_ID, trxName);
		if (C_Bulto_ID == 0)
		{
			setIsActive(true);
			setTotalBulto(0);
			setEnTransporte(false);
		}
	}

	public X_C_Bulto (Properties ctx, ResultSet rs, String trxName)
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
		StringBuffer sb = new StringBuffer ("X_C_Bulto[")
			.append(get_ID()).append("]");
		return sb.toString();
	}

	public static final String Table_Name = "C_Bulto";
	public static final int Table_ID = MTable.getTable_ID(Table_Name);
	public static final BigDecimal accessLevel = BigDecimal.valueOf(3);

	public static final String COLUMNNAME_C_Bulto_ID       = "C_Bulto_ID";
	public static final String COLUMNNAME_DocumentNo       = "DocumentNo";
	public static final String COLUMNNAME_BultoNo          = "BultoNo";
	public static final String COLUMNNAME_TotalBulto       = "TotalBulto";
	public static final String COLUMNNAME_C_Order_ID       = "C_Order_ID";
	public static final String COLUMNNAME_C_BPartner_ID    = "C_BPartner_ID";
	public static final String COLUMNNAME_C_BPartner_Location_ID = "C_BPartner_Location_ID";
	public static final String COLUMNNAME_Address1         = "Address1";
	public static final String COLUMNNAME_City             = "City";
	public static final String COLUMNNAME_DateAcct         = "DateAcct";
	public static final String COLUMNNAME_EnTransporte     = "EnTransporte";
	public static final String COLUMNNAME_M_Shipman_ID     = "M_Shipman_ID";
	public static final String COLUMNNAME_Description      = "Description";

	public void setDocumentNo (String DocumentNo)
	{
		set_Value (COLUMNNAME_DocumentNo, DocumentNo);
	}
	public String getDocumentNo ()
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

	public void setBultoNo (String BultoNo)
	{
		set_Value (COLUMNNAME_BultoNo, BultoNo);
	}
	public String getBultoNo ()
	{
		return (String)get_Value(COLUMNNAME_BultoNo);
	}

	public void setTotalBulto (int TotalBulto)
	{
		set_Value (COLUMNNAME_TotalBulto, Integer.valueOf(TotalBulto));
	}
	public int getTotalBulto ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_TotalBulto);
		if (ii == null) return 0;
		return ii.intValue();
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

	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1) set_Value (COLUMNNAME_C_BPartner_ID, null);
		else set_Value (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}
	public int getC_BPartner_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null) return 0;
		return ii.intValue();
	}

	public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
	{
		if (C_BPartner_Location_ID < 1) set_Value (COLUMNNAME_C_BPartner_Location_ID, null);
		else set_Value (COLUMNNAME_C_BPartner_Location_ID, Integer.valueOf(C_BPartner_Location_ID));
	}
	public int getC_BPartner_Location_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_Location_ID);
		if (ii == null) return 0;
		return ii.intValue();
	}

	public void setAddress1 (String Address1)
	{
		set_Value (COLUMNNAME_Address1, Address1);
	}
	public String getAddress1 ()
	{
		return (String)get_Value(COLUMNNAME_Address1);
	}

	public void setCity (String City)
	{
		set_Value (COLUMNNAME_City, City);
	}
	public String getCity ()
	{
		return (String)get_Value(COLUMNNAME_City);
	}

	public void setDateAcct (Timestamp DateAcct)
	{
		set_Value (COLUMNNAME_DateAcct, DateAcct);
	}
	public Timestamp getDateAcct ()
	{
		return (Timestamp)get_Value(COLUMNNAME_DateAcct);
	}

	public void setEnTransporte (boolean EnTransporte)
	{
		set_Value (COLUMNNAME_EnTransporte, Boolean.valueOf(EnTransporte));
	}
	public boolean isEnTransporte ()
	{
		Object oo = get_Value(COLUMNNAME_EnTransporte);
		if (oo != null)
		{
			if (oo instanceof Boolean) return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	public void setM_Shipman_ID (int M_Shipman_ID)
	{
		if (M_Shipman_ID < 1) set_Value (COLUMNNAME_M_Shipman_ID, null);
		else set_Value (COLUMNNAME_M_Shipman_ID, Integer.valueOf(M_Shipman_ID));
	}
	public int getM_Shipman_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Shipman_ID);
		if (ii == null) return 0;
		return ii.intValue();
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
