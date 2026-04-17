package org.windsor.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.DB;

/**
 * Modelo para Z_BultoParam - Parámetros de peso máximo por bulto
 * Lookup por zona y/o categoría de producto con fallback en cascada.
 */
public class MZBultoParam extends X_Z_BultoParam
{
	private static final long serialVersionUID = 20260417L;

	/** Peso máximo por defecto si no hay configuración: 30 kg */
	public static final BigDecimal DEFAULT_PESO_MAX_KG = new BigDecimal("30");

	public MZBultoParam (Properties ctx, int Z_BultoParam_ID, String trxName)
	{
		super (ctx, Z_BultoParam_ID, trxName);
	}

	public MZBultoParam (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
	}

	public int getZ_BultoParam_ID()
	{
		return get_ID();
	}

	/**
	 * Obtiene el peso máximo por bulto para una zona y categoría dadas.
	 * Cascada de lookup:
	 *   1. categoria + zona específica
	 *   2. categoria + zona NULL (aplica a todas las zonas)
	 *   3. categoria NULL + zona específica
	 *   4. categoria NULL + zona NULL (global)
	 *   5. DEFAULT_PESO_MAX_KG (30 kg)
	 *
	 * @param ctx         contexto
	 * @param categoryID  M_Product_Category_ID del producto (0 si no aplica)
	 * @param zona        valor de C_City.Zona del cliente (null o vacío = Santiago)
	 * @param trxName     nombre de transacción
	 * @return peso máximo en kg
	 */
	public static BigDecimal getPesoMax(Properties ctx, int categoryID, String zona, String trxName)
	{
		String zonaParam = (zona == null || zona.trim().length() == 0) ? null : zona.trim();

		String sql =
			"SELECT PesoMaxKg FROM Z_BultoParam " +
			"WHERE AD_Client_ID = ? AND IsActive = 'Y' " +
			"  AND (M_Product_Category_ID = ? OR M_Product_Category_ID IS NULL) " +
			"  AND (Zona = ? OR (Zona IS NULL AND ? IS NULL) OR Zona IS NULL) " +
			"ORDER BY " +
			"  CASE WHEN M_Product_Category_ID = ? THEN 0 ELSE 1 END, " +
			"  CASE WHEN Zona = ? THEN 0 WHEN Zona IS NULL THEN 1 ELSE 2 END " +
			"FETCH FIRST 1 ROWS ONLY";

		int adClientId = Integer.parseInt(org.compiere.util.Env.getAD_Client_ID(ctx) + "");
		BigDecimal result = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt   (1, adClientId);
			pstmt.setInt   (2, categoryID > 0 ? categoryID : 0);
			pstmt.setString(3, zonaParam);
			pstmt.setString(4, zonaParam);
			pstmt.setInt   (5, categoryID > 0 ? categoryID : 0);
			pstmt.setString(6, zonaParam);
			rs = pstmt.executeQuery();
			if (rs.next())
				result = rs.getBigDecimal(1);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error buscando Z_BultoParam: " + e.getMessage(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
		}
		return result != null ? result : DEFAULT_PESO_MAX_KG;
	}
}
