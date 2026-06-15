package org.windsor.process;

import java.math.BigDecimal;

public class LineaPropuesta
{
	public int        productId;
	public String     productValue;
	public String     productName;
	public BigDecimal qty;
	public BigDecimal pesoUnitario;

	public LineaPropuesta(int id, String value, String name, BigDecimal q, BigDecimal pesoUnit)
	{
		this.productId    = id;
		this.productValue = value;
		this.productName  = name;
		this.qty          = q;
		this.pesoUnitario = pesoUnit;
	}

	public BigDecimal pesoTotal()
	{
		if (pesoUnitario == null || qty == null) return BigDecimal.ZERO;
		return pesoUnitario.multiply(qty);
	}
}
