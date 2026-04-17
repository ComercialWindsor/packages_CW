package org.windsor.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.util.DB;

/**
 * Modelo para C_Bulto - Etiqueta de bulto por nota de venta
 */
public class MCBulto extends X_C_Bulto
{
	private static final long serialVersionUID = 20260417L;

	public MCBulto (Properties ctx, int C_Bulto_ID, String trxName)
	{
		super (ctx, C_Bulto_ID, trxName);
		if (C_Bulto_ID == 0)
			setDateAcct(new Timestamp(System.currentTimeMillis()));
	}

	public MCBulto (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
	}

	/** Obtiene todas las líneas de este bulto ordenadas por LINE */
	public MCBultoLine[] getLines()
	{
		String sql = "SELECT * FROM C_BultoLine WHERE C_Bulto_ID=? AND IsActive='Y' ORDER BY Line";
		java.util.List list = new java.util.ArrayList();
		java.sql.PreparedStatement pstmt = null;
		java.sql.ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, getC_Bulto_ID());
			rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MCBultoLine(getCtx(), rs, get_TrxName()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			DB.close(rs, pstmt);
		}
		MCBultoLine[] retValue = new MCBultoLine[list.size()];
		list.toArray(retValue);
		return retValue;
	}

	public int getC_Bulto_ID()
	{
		return get_ID();
	}
}
