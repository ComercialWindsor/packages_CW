package org.windsor.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Modelo para C_BultoLine - Línea de detalle de un bulto
 */
public class MCBultoLine extends X_C_BultoLine
{
	private static final long serialVersionUID = 20260417L;

	public MCBultoLine (Properties ctx, int C_BultoLine_ID, String trxName)
	{
		super (ctx, C_BultoLine_ID, trxName);
	}

	public MCBultoLine (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
	}

	/** Constructor de conveniencia para crear una línea nueva */
	public MCBultoLine (Properties ctx, int C_Bulto_ID, int C_Order_ID, String trxName)
	{
		this (ctx, 0, trxName);
		setC_Bulto_ID(C_Bulto_ID);
		setC_Order_ID(C_Order_ID);
	}

	public int getC_BultoLine_ID()
	{
		return get_ID();
	}

	/** Calcula el peso de esta línea: peso unitario * cantidad */
	public BigDecimal getPesoTotal(BigDecimal pesoUnitario)
	{
		if (pesoUnitario == null || pesoUnitario.compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;
		return pesoUnitario.multiply(getQtyEntered());
	}
}
