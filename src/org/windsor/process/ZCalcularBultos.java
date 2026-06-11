package org.windsor.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MOrder;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.windsor.model.MCBulto;
import org.windsor.model.MCBultoLine;
import org.windsor.model.MZBultoParam;

/**
 * ZCalcularBultos — Calcula y genera los bultos para una Nota de Venta.
 * Expone proponer() y persistir() estaticos para reuso desde formularios.
 */
public class ZCalcularBultos extends SvrProcess
{
	private int p_C_Order_ID = 0;

	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter p : para)
		{
			if ("C_Order_ID".equals(p.getParameterName()))
				p_C_Order_ID = p.getParameterAsInt();
		}
	}

	@Override
	protected String doIt() throws Exception
	{
		if (p_C_Order_ID <= 0)
			throw new Exception("Se requiere una Nota de Venta");

		List bultos = proponer(getCtx(), p_C_Order_ID, get_TrxName());
		int  total  = persistir(getCtx(), p_C_Order_ID, bultos, get_TrxName());

		MOrder order = new MOrder(getCtx(), p_C_Order_ID, get_TrxName());
		return "OK: " + total + " bulto(s) generados para la orden " + order.getDocumentNo();
	}

	// =========================================================================
	// API PUBLICA: PROPONER (sin tocar BD)
	// =========================================================================
	public static List proponer(Properties ctx, int orderId, String trxName)
		throws Exception
	{
		MOrder order = new MOrder(ctx, orderId, trxName);
		if (order.get_ID() == 0)
			throw new Exception("Nota de Venta no encontrada: " + orderId);

		String zona   = getClienteZona(order.getC_BPartner_Location_ID(), trxName);
		List   lineas = getOrderLineasConProducto(orderId, trxName);
		if (lineas.isEmpty())
			throw new Exception("La nota de venta no tiene lineas de producto activas");

		return distribuirEnBultos(ctx, lineas, zona, trxName);
	}

	// =========================================================================
	// API PUBLICA: PERSISTIR la propuesta (acepta lista editada por el form)
	// =========================================================================
	public static int persistir(Properties ctx, int orderId, List bultos, String trxName)
		throws Exception
	{
		MOrder order   = new MOrder(ctx, orderId, trxName);
		String address = getAddress(order.getC_BPartner_Location_ID(), trxName);
		String city    = getCity   (order.getC_BPartner_Location_ID(), trxName);

		// Filtrar bultos vacios
		List validos = new ArrayList();
		for (int i = 0; i < bultos.size(); i++)
		{
			BultoPropuesta bp = (BultoPropuesta) bultos.get(i);
			if (bp.lineas != null && !bp.lineas.isEmpty())
				validos.add(bp);
		}
		int totalBultos = validos.size();
		if (totalBultos == 0)
			throw new Exception("No hay bultos a generar");

		deleteBultosExistentes(orderId, trxName);

		int bultoNo = 1;
		for (int b = 0; b < validos.size(); b++)
		{
			BultoPropuesta bp = (BultoPropuesta) validos.get(b);

			MCBulto bulto = new MCBulto(ctx, 0, trxName);
			bulto.setC_Order_ID(orderId);
			bulto.setC_BPartner_ID(order.getC_BPartner_ID());
			bulto.setC_BPartner_Location_ID(order.getC_BPartner_Location_ID());
			bulto.setAddress1(address);
			bulto.setCity(city);
			bulto.setDateAcct(new Timestamp(System.currentTimeMillis()));
			bulto.setBultoNo(String.valueOf(bultoNo));
			bulto.setTotalBulto(totalBultos);
			bulto.setDocumentNo(orderId + "-" + bultoNo);
			bulto.setIsActive(true);
			if (!bulto.save())
				throw new Exception("Error al guardar C_Bulto " + bultoNo);

			int lineNo = 10;
			for (int i = 0; i < bp.lineas.size(); i++)
			{
				LineaPropuesta lp = (LineaPropuesta) bp.lineas.get(i);
				MCBultoLine linea = new MCBultoLine(ctx, bulto.getC_Bulto_ID(), orderId, trxName);
				linea.setLine(lineNo);
				linea.setM_Product_ID(lp.productId);
				linea.setValue(lp.productValue);
				linea.setDescription(lp.productName);
				linea.setQtyEntered(lp.qty);
				linea.setIsActive(true);
				if (!linea.save())
					throw new Exception("Error al guardar C_BultoLine " + lp.productValue);
				lineNo += 10;
			}
			bultoNo++;
		}

		// Actualizar cantidad de bultos en la nota de venta
		order.set_ValueOfColumn("Bultos", new Integer(totalBultos));
		if (!order.save(trxName))
			throw new Exception("No se pudo actualizar Bultos en la Nota de Venta");

		return totalBultos;
	}

	// =========================================================================
	// LOOKUPS (estaticos)
	// =========================================================================
	private static String getClienteZona(int bplId, String trxName)
	{
		if (bplId <= 0) return null;
		String sql =
			"SELECT ci.Zona FROM C_BPartner_Location bpl " +
			"JOIN C_Location l ON bpl.C_Location_ID = l.C_Location_ID " +
			"LEFT JOIN C_City ci ON l.C_City_ID = ci.C_City_ID " +
			"WHERE bpl.C_BPartner_Location_ID = ?";
		return DB.getSQLValueString(trxName, sql, bplId);
	}

	private static String getAddress(int bplId, String trxName)
	{
		if (bplId <= 0) return null;
		String sql =
			"SELECT l.Address1 FROM C_BPartner_Location bpl " +
			"JOIN C_Location l ON bpl.C_Location_ID = l.C_Location_ID " +
			"WHERE bpl.C_BPartner_Location_ID = ?";
		return DB.getSQLValueString(trxName, sql, bplId);
	}

	private static String getCity(int bplId, String trxName)
	{
		if (bplId <= 0) return null;
		String sql =
			"SELECT NVL(ci.Name, l.City) FROM C_BPartner_Location bpl " +
			"JOIN C_Location l ON bpl.C_Location_ID = l.C_Location_ID " +
			"LEFT JOIN C_City ci ON l.C_City_ID = ci.C_City_ID " +
			"WHERE bpl.C_BPartner_Location_ID = ?";
		return DB.getSQLValueString(trxName, sql, bplId);
	}

	private static List getOrderLineasConProducto(int orderId, String trxName)
	{
		List result = new ArrayList();
		String sql =
			"SELECT ol.M_Product_ID, ol.QtyOrdered, " +
			"       p.Value, p.Name, " +
			"       NVL(p.PESO, 0) AS PESO, " +
			"       NVL(p.PesoBox, 0) AS PesoBox, " +
			"       NVL(p.UnitsPerPack, 0) AS UnitsPerPack, " +
			"       NVL(p.M_Product_Category_ID, 0) AS M_Product_Category_ID " +
			"FROM C_OrderLine ol " +
			"JOIN M_Product p ON ol.M_Product_ID = p.M_Product_ID " +
			"WHERE ol.C_Order_ID = ? " +
			"  AND ol.IsActive = 'Y' AND p.ProductType = 'I' AND ol.QtyOrdered > 0 " +
			"ORDER BY ol.Line";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, orderId);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				LineaProductoOrden lp = new LineaProductoOrden();
				lp.productId    = rs.getInt("M_Product_ID");
				lp.productValue = rs.getString("Value");
				lp.productName  = rs.getString("Name");
				lp.qty          = rs.getBigDecimal("QtyOrdered");
				lp.pesoUnitario = rs.getBigDecimal("PESO");
				lp.pesoBox      = rs.getBigDecimal("PesoBox");
				lp.unitsPerPack = rs.getInt("UnitsPerPack");
				lp.categoryId   = rs.getInt("M_Product_Category_ID");
				result.add(lp);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error leyendo lineas de orden: " + e.getMessage(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
		}
		return result;
	}

	private static List distribuirEnBultos(Properties ctx, List lineas, String zona, String trxName)
	{
		List bultos = new ArrayList();
		BultoPropuesta bt = new BultoPropuesta();
		bultos.add(bt);

		for (int i = 0; i < lineas.size(); i++)
		{
			LineaProductoOrden lp = (LineaProductoOrden) lineas.get(i);
			BigDecimal pesoLimit  = MZBultoParam.getPesoMax(ctx, lp.categoryId, zona, trxName);
			bt.pesoLimit          = pesoLimit;

			if (lp.unitsPerPack > 0 && lp.qty.compareTo(BigDecimal.ZERO) > 0)
			{
				int numCajas = lp.qty.divide(new BigDecimal(lp.unitsPerPack), 0, BigDecimal.ROUND_DOWN).intValue();
				int resto    = lp.qty.intValue() % lp.unitsPerPack;
				BigDecimal pesoCaja = lp.pesoBox.compareTo(BigDecimal.ZERO) > 0
					? lp.pesoBox
					: lp.pesoUnitario.multiply(new BigDecimal(lp.unitsPerPack));
				for (int c = 0; c < numCajas; c++)
					bt = agregarUnidad(bultos, bt, lp, new BigDecimal(lp.unitsPerPack), pesoCaja, pesoLimit);
				if (resto > 0)
					bt = agregarUnidad(bultos, bt, lp, new BigDecimal(resto),
						lp.pesoUnitario.multiply(new BigDecimal(resto)), pesoLimit);
			}
			else
			{
				int total = lp.qty.intValue();
				for (int u = 0; u < total; u++)
					bt = agregarUnidad(bultos, bt, lp, BigDecimal.ONE, lp.pesoUnitario, pesoLimit);
			}
		}

		for (int b = 0; b < bultos.size(); b++)
			((BultoPropuesta) bultos.get(b)).consolidar();
		return bultos;
	}

	private static BultoPropuesta agregarUnidad(List bultos, BultoPropuesta bt, LineaProductoOrden lp,
		BigDecimal qty, BigDecimal peso, BigDecimal pesoLimit)
	{
		BigDecimal pesoActual = bt.pesoTotal();
		if (pesoActual.compareTo(BigDecimal.ZERO) > 0
			&& pesoActual.add(peso).compareTo(pesoLimit) > 0)
		{
			bt = new BultoPropuesta();
			bt.pesoLimit = pesoLimit;
			bultos.add(bt);
		}
		BigDecimal pesoUnit = qty.compareTo(BigDecimal.ZERO) > 0
			? peso.divide(qty, 4, BigDecimal.ROUND_HALF_UP)
			: BigDecimal.ZERO;
		bt.lineas.add(new LineaPropuesta(lp.productId, lp.productValue, lp.productName, qty, pesoUnit));
		return bt;
	}

	private static void deleteBultosExistentes(int orderId, String trxName)
	{
		DB.executeUpdate("DELETE FROM C_BultoLine WHERE C_Order_ID = " + orderId, trxName);
		DB.executeUpdate("DELETE FROM C_Bulto     WHERE C_Order_ID = " + orderId, trxName);
	}

	private static class LineaProductoOrden
	{
		int        productId;
		String     productValue;
		String     productName;
		BigDecimal qty;
		BigDecimal pesoUnitario;
		BigDecimal pesoBox;
		int        unitsPerPack;
		int        categoryId;
	}
}
