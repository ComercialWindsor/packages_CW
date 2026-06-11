package org.windsor.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BultoPropuesta
{
	public List       lineas    = new ArrayList();   // LineaPropuesta
	public BigDecimal pesoLimit = BigDecimal.ZERO;

	public BigDecimal pesoTotal()
	{
		BigDecimal total = BigDecimal.ZERO;
		for (int i = 0; i < lineas.size(); i++)
			total = total.add(((LineaPropuesta) lineas.get(i)).pesoTotal());
		return total;
	}

	/** Fusiona entradas duplicadas del mismo producto */
	public void consolidar()
	{
		Map  agg   = new HashMap();
		List orden = new ArrayList();
		for (int i = 0; i < lineas.size(); i++)
		{
			LineaPropuesta lp  = (LineaPropuesta) lineas.get(i);
			Integer        key = new Integer(lp.productId);
			if (agg.containsKey(key))
			{
				LineaPropuesta ex = (LineaPropuesta) agg.get(key);
				ex.qty = ex.qty.add(lp.qty);
			}
			else
			{
				agg.put(key, lp);
				orden.add(lp);
			}
		}
		lineas = orden;
	}
}
