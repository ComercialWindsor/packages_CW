package org.windsor.apps.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.compiere.apps.ADialog;
import org.compiere.apps.ConfirmPanel;
import org.compiere.apps.ProcessCtl;
import org.compiere.apps.form.FormFrame;
import org.compiere.apps.form.FormPanel;
import org.compiere.apps.search.Info;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.swing.CButton;
import org.compiere.swing.CPanel;
import org.compiere.swing.CTextField;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.windsor.process.BultoPropuesta;
import org.windsor.process.LineaPropuesta;
import org.windsor.process.ZCalcularBultos;

/**
 * VCalcularBultos - Previsualiza y edita la propuesta de bultos antes de persistir.
 * El usuario puede reasignar lineas a otros bultos, editar cantidades,
 * "Aplicar Cambios" persiste y refresca; el boton de cerrar abajo solo cierra.
 */
public class VCalcularBultos extends CPanel
	implements FormPanel, ActionListener, TableModelListener
{
	private static final long serialVersionUID = 1L;

	/** 0=Bulto#, 1=Codigo, 2=Producto, 3=Cantidad, 4=PesoUnit, 5=PesoTotal */
	private static final String[] COLS = new String[]
		{ "Bulto", "Codigo", "Producto", "Cantidad", "Peso Unit", "Peso Total" };

	private FormFrame    m_frame;
	private int          m_WindowNo;

	private CTextField   txtOrden;
	private CButton      btnCargar;
	private CButton      btnAddBulto;
	private CButton      btnRecalcular;
	private CButton      btnAplicar;
	private CButton      btnImprimir;
	private JLabel       lblResumen;

	private static final int AD_PROCESS_ETIQUETAS = 1000783;
	private JTable       tabla;
	private DefaultTableModel modelo;
	private ConfirmPanel confirmPanel;

	private int          m_C_Order_ID = 0;
	private boolean      m_loading    = false;

	public VCalcularBultos() {}

	public void init(int WindowNo, FormFrame frame)
	{
		m_WindowNo = WindowNo;
		m_frame    = frame;
		try
		{
			buildUI();
			frame.getContentPane().add(this, BorderLayout.CENTER);
			frame.pack();
			frame.setSize(new Dimension(950, 600));
		}
		catch (Exception e)
		{
			ADialog.error(m_WindowNo, this, "Error", e.getMessage());
		}
	}

	public void dispose()
	{
		if (m_frame != null) m_frame.dispose();
		m_frame = null;
	}

	// =========================================================================
	// UI
	// =========================================================================
	private void buildUI()
	{
		setLayout(new BorderLayout(5, 5));

		// --- Norte: selector + acciones ---
		CPanel north = new CPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		north.add(new JLabel("Nota de Venta:"));
		txtOrden = new CTextField(15);
		txtOrden.setToolTipText("Doble click o Enter para buscar nota de venta");
		txtOrden.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() >= 2) abrirBuscadorOrden();
			}
		});
		txtOrden.addActionListener(this);
		north.add(txtOrden);
		btnCargar = new CButton("Cargar Propuesta");
		btnCargar.addActionListener(this);
		north.add(btnCargar);
		btnRecalcular = new CButton("Recalcular");
		btnRecalcular.addActionListener(this);
		north.add(btnRecalcular);
		btnAddBulto = new CButton("+ Bulto");
		btnAddBulto.addActionListener(this);
		north.add(btnAddBulto);
		btnAplicar = new CButton("Aplicar Cambios");
		btnAplicar.setToolTipText("Persiste la propuesta y refresca la tabla con los bultos guardados");
		btnAplicar.addActionListener(this);
		north.add(btnAplicar);
		btnImprimir = new CButton("Imprimir Etiquetas");
		btnImprimir.setToolTipText("Genera un PDF con una etiqueta por bulto persistido");
		btnImprimir.addActionListener(this);
		north.add(btnImprimir);
		add(north, BorderLayout.NORTH);

		// --- Centro: tabla ---
		modelo = new DefaultTableModel(COLS, 0)
		{
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col)
			{
				return col == 0 || col == 3;  // Bulto#, Cantidad
			}
			public Class getColumnClass(int col)
			{
				if (col == 0) return Integer.class;
				if (col == 3 || col == 4 || col == 5) return BigDecimal.class;
				return String.class;
			}
		};
		modelo.addTableModelListener(this);
		tabla = new JTable(modelo);
		tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		add(new JScrollPane(tabla), BorderLayout.CENTER);

		// --- Sur: resumen + cerrar ---
		CPanel south = new CPanel(new BorderLayout());
		lblResumen = new JLabel("  ");
		south.add(lblResumen, BorderLayout.NORTH);
		confirmPanel = new ConfirmPanel(true);
		confirmPanel.addActionListener(this);
		south.add(confirmPanel, BorderLayout.SOUTH);
		add(south, BorderLayout.SOUTH);
	}

	// =========================================================================
	// EVENTOS
	// =========================================================================
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src == txtOrden)                           cargarPropuesta();
		else if (src == btnCargar || src == btnRecalcular) cargarPropuesta();
		else if (src == btnAddBulto)                   agregarBultoVacio();
		else if (src == btnAplicar)                    aplicar();
		else if (src == btnImprimir)                   imprimirEtiquetas();
		else if (src == confirmPanel.getOKButton())    dispose();
		else if (src == confirmPanel.getCancelButton()) dispose();
	}

	public void tableChanged(TableModelEvent e)
	{
		if (m_loading) return;
		if (e.getColumn() == 3)
		{
			int row = e.getFirstRow();
			BigDecimal qty      = toBD(modelo.getValueAt(row, 3));
			BigDecimal pesoUnit = toBD(modelo.getValueAt(row, 4));
			m_loading = true;
			modelo.setValueAt(round2(qty.multiply(pesoUnit)), row, 5);
			m_loading = false;
		}
		else if (e.getColumn() == 0)
		{
			renumerarBultos();
		}
		actualizarResumen();
	}

	/** Compacta la columna Bulto a 1..N conservando el orden relativo */
	private void renumerarBultos()
	{
		java.util.TreeSet distintos = new java.util.TreeSet();
		for (int r = 0; r < modelo.getRowCount(); r++)
		{
			Integer bn = (Integer) modelo.getValueAt(r, 0);
			if (bn != null && bn.intValue() > 0) distintos.add(bn);
		}
		Map remap = new HashMap();
		int n = 1;
		for (java.util.Iterator it = distintos.iterator(); it.hasNext(); n++)
			remap.put(it.next(), new Integer(n));

		m_loading = true;
		for (int r = 0; r < modelo.getRowCount(); r++)
		{
			Integer bn = (Integer) modelo.getValueAt(r, 0);
			Integer nv = (Integer) remap.get(bn);
			if (nv != null && !nv.equals(bn))
				modelo.setValueAt(nv, r, 0);
		}
		m_loading = false;
	}

	// =========================================================================
	// ACCIONES
	// =========================================================================
	private void abrirBuscadorOrden()
	{
		Frame parent = Env.getFrame(this);
		Info info = Info.create(parent, true, m_WindowNo,
			"C_Order", "C_Order_ID", "", false, "");
		if (info == null) return;
		info.setVisible(true);
		if (!info.isOkPressed()) return;
		Object key = info.getSelectedKey();
		if (key == null) return;
		int orderId = ((Integer) key).intValue();
		String docNo = DB.getSQLValueString(null,
			"SELECT DocumentNo FROM C_Order WHERE C_Order_ID = ?", orderId);
		m_C_Order_ID = orderId;
		txtOrden.setText(docNo != null ? docNo : String.valueOf(orderId));
		cargarPropuesta();
	}

	private void cargarPropuesta()
	{
		String txt = txtOrden.getText().trim();
		if (txt.length() == 0)
		{
			ADialog.error(m_WindowNo, this, "Error", "Ingrese DocumentNo o use el buscador");
			return;
		}
		try
		{
			m_C_Order_ID = resolveOrderId(txt);
			if (m_C_Order_ID <= 0)
			{
				ADialog.error(m_WindowNo, this, "Error", "Nota de Venta no encontrada: " + txt);
				return;
			}
			// Si ya tiene bultos guardados, traerlos; si no, calcular propuesta
			if (tieneBultos(m_C_Order_ID))
				cargarDesdeBD(m_C_Order_ID);
			else
			{
				List bultos = ZCalcularBultos.proponer(Env.getCtx(), m_C_Order_ID, null);
				llenarTabla(bultos);
			}
		}
		catch (Exception ex)
		{
			ADialog.error(m_WindowNo, this, "Error", ex.getMessage());
		}
	}

	private boolean tieneBultos(int orderId)
	{
		int n = DB.getSQLValue(null,
			"SELECT COUNT(*) FROM C_Bulto WHERE C_Order_ID = ? AND IsActive = 'Y'",
			orderId);
		return n > 0;
	}

	private int resolveOrderId(String txt)
	{
		try
		{
			int id = Integer.parseInt(txt);
			int exists = DB.getSQLValue(null,
				"SELECT C_Order_ID FROM C_Order WHERE C_Order_ID = ? AND AD_Client_ID = ?",
				id, Env.getAD_Client_ID(Env.getCtx()));
			if (exists > 0) return exists;
		}
		catch (NumberFormatException nfe) { /* no es id */ }
		return DB.getSQLValue(null,
			"SELECT C_Order_ID FROM C_Order WHERE DocumentNo = ? AND AD_Client_ID = ?",
			txt, Env.getAD_Client_ID(Env.getCtx()));
	}

	private void llenarTabla(List bultos)
	{
		m_loading = true;
		modelo.setRowCount(0);
		for (int b = 0; b < bultos.size(); b++)
		{
			BultoPropuesta bp = (BultoPropuesta) bultos.get(b);
			int bultoNo = b + 1;
			for (int i = 0; i < bp.lineas.size(); i++)
			{
				LineaPropuesta lp = (LineaPropuesta) bp.lineas.get(i);
				modelo.addRow(new Object[]
					{ new Integer(bultoNo), lp.productValue, lp.productName,
					  lp.qty, round2(lp.pesoUnitario), round2(lp.pesoTotal()) });
			}
		}
		m_loading = false;
		actualizarResumen();
	}

	/** Carga los bultos ya persistidos para la orden */
	private void cargarDesdeBD(int orderId) throws Exception
	{
		m_loading = true;
		modelo.setRowCount(0);
		String sql =
			"SELECT cb.BultoNo, cbl.Value, cbl.Description, cbl.QtyEntered, " +
			"       NVL(p.PESO, 0) AS PESO " +
			"FROM C_Bulto cb " +
			"JOIN C_BultoLine cbl ON cb.C_Bulto_ID = cbl.C_Bulto_ID " +
			"LEFT JOIN M_Product p ON cbl.M_Product_ID = p.M_Product_ID " +
			"WHERE cb.C_Order_ID = ? AND cb.IsActive = 'Y' AND cbl.IsActive = 'Y' " +
			"ORDER BY TO_NUMBER(cb.BultoNo), cbl.Line";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, orderId);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				int bultoNo = Integer.parseInt(rs.getString("BultoNo"));
				String code = rs.getString("Value");
				String name = rs.getString("Description");
				BigDecimal qty      = rs.getBigDecimal("QtyEntered");
				BigDecimal pesoUnit = rs.getBigDecimal("PESO");
				if (pesoUnit == null) pesoUnit = BigDecimal.ZERO;
				modelo.addRow(new Object[]
					{ new Integer(bultoNo), code, name, qty,
					  round2(pesoUnit), round2(pesoUnit.multiply(qty)) });
			}
		}
		finally
		{
			DB.close(rs, pstmt);
		}
		m_loading = false;
		actualizarResumen();
	}

	private void agregarBultoVacio()
	{
		int max = 0;
		for (int r = 0; r < modelo.getRowCount(); r++)
		{
			int n = ((Integer) modelo.getValueAt(r, 0)).intValue();
			if (n > max) max = n;
		}
		ADialog.info(m_WindowNo, this, "Nuevo Bulto",
			"El proximo bulto sera #" + (max + 1) +
			". Cambie la columna 'Bulto' de alguna fila a ese numero.");
	}

	private void actualizarResumen()
	{
		Map pesos = new HashMap();
		for (int r = 0; r < modelo.getRowCount(); r++)
		{
			Integer bn    = (Integer) modelo.getValueAt(r, 0);
			BigDecimal pt = toBD(modelo.getValueAt(r, 5));
			BigDecimal ac = (BigDecimal) pesos.get(bn);
			if (ac == null) ac = BigDecimal.ZERO;
			pesos.put(bn, ac.add(pt));
		}
		StringBuffer sb = new StringBuffer("Bultos: ").append(pesos.size()).append("  |  ");
		List keys = new ArrayList(pesos.keySet());
		java.util.Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++)
		{
			Integer k = (Integer) keys.get(i);
			sb.append(" #").append(k).append("=").append(round2((BigDecimal) pesos.get(k))).append("kg ");
		}
		lblResumen.setText(sb.toString());
	}

	private void aplicar()
	{
		if (modelo.getRowCount() == 0)
		{
			ADialog.error(m_WindowNo, this, "Error", "No hay propuesta cargada");
			return;
		}
		int op = JOptionPane.showConfirmDialog(this,
			"Esto reemplazara los bultos existentes de la orden. Continuar?",
			"Aplicar Cambios", JOptionPane.YES_NO_OPTION);
		if (op != JOptionPane.YES_OPTION) return;

		// Reagrupa + renumera 1..N automaticamente
		List bultos = construirDesdeTabla();
		Trx trx = Trx.get(Trx.createTrxName("CalcBultos"), true);
		try
		{
			int total = ZCalcularBultos.persistir(Env.getCtx(), m_C_Order_ID, bultos, trx.getTrxName());
			trx.commit();
			// Refresca desde BD para que la tabla muestre 1..N consistente con X/N
			cargarDesdeBD(m_C_Order_ID);
			ADialog.info(m_WindowNo, this, "OK", total + " bulto(s) aplicados.");
		}
		catch (Exception ex)
		{
			trx.rollback();
			ADialog.error(m_WindowNo, this, "Error", ex.getMessage());
		}
		finally
		{
			trx.close();
		}
	}

	private void imprimirEtiquetas()
	{
		if (m_C_Order_ID <= 0)
		{
			ADialog.error(m_WindowNo, this, "Error", "Cargue primero una nota de venta");
			return;
		}
		if (!tieneBultos(m_C_Order_ID))
		{
			ADialog.error(m_WindowNo, this, "Error", "La orden no tiene bultos guardados. Aplique cambios primero.");
			return;
		}
		try
		{
			ProcessInfo pi = new ProcessInfo("Imprimir Etiquetas de Bultos", AD_PROCESS_ETIQUETAS);
			pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
			pi.setAD_User_ID (Env.getAD_User_ID (Env.getCtx()));
			pi.setRecord_ID  (m_C_Order_ID);
			ProcessInfoParameter[] para = new ProcessInfoParameter[] {
				new ProcessInfoParameter("C_Order_ID",
					new java.math.BigDecimal(m_C_Order_ID), null, null, null)
			};
			pi.setParameter(para);

			// Saltamos el dialogo de parametros: creamos MPInstance y MPInstancePara
			// manualmente, y lanzamos ProcessCtl directamente (no via process()).
			MPInstance instance = new MPInstance(Env.getCtx(), AD_PROCESS_ETIQUETAS, m_C_Order_ID);
			if (!instance.save())
			{
				ADialog.error(m_WindowNo, this, "Error", "No se pudo crear AD_PInstance");
				return;
			}
			MPInstancePara ipPara = new MPInstancePara(instance, 10);
			ipPara.setParameter("C_Order_ID", new java.math.BigDecimal(m_C_Order_ID));
			if (!ipPara.save())
			{
				ADialog.error(m_WindowNo, this, "Error", "No se pudo guardar parametro C_Order_ID");
				return;
			}
			pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());

			ProcessCtl worker = new ProcessCtl(null, m_WindowNo, pi, null);
			worker.start();
		}
		catch (Exception ex)
		{
			ADialog.error(m_WindowNo, this, "Error", ex.getMessage());
		}
	}

	/** Reagrupa filas por columna Bulto#; persistir() despues renumera a 1..N */
	private List construirDesdeTabla()
	{
		Map agrupado = new HashMap();
		for (int r = 0; r < modelo.getRowCount(); r++)
		{
			Integer bn = (Integer) modelo.getValueAt(r, 0);
			if (bn == null || bn.intValue() <= 0) continue;
			BigDecimal qty = toBD(modelo.getValueAt(r, 3));
			if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;

			String code = (String) modelo.getValueAt(r, 1);
			String name = (String) modelo.getValueAt(r, 2);
			BigDecimal pesoUnit = toBD(modelo.getValueAt(r, 4));
			int productId = DB.getSQLValue(null,
				"SELECT M_Product_ID FROM M_Product WHERE Value = ? AND AD_Client_ID = ?",
				code, Env.getAD_Client_ID(Env.getCtx()));

			BultoPropuesta bp = (BultoPropuesta) agrupado.get(bn);
			if (bp == null) { bp = new BultoPropuesta(); agrupado.put(bn, bp); }
			bp.lineas.add(new LineaPropuesta(productId, code, name, qty, pesoUnit));
		}
		List keys = new ArrayList(agrupado.keySet());
		java.util.Collections.sort(keys);
		List result = new ArrayList();
		for (int i = 0; i < keys.size(); i++)
		{
			BultoPropuesta bp = (BultoPropuesta) agrupado.get(keys.get(i));
			bp.consolidar();
			result.add(bp);
		}
		return result;
	}

	private static BigDecimal round2(BigDecimal v)
	{
		if (v == null) return BigDecimal.ZERO;
		return v.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	private static BigDecimal toBD(Object o)
	{
		if (o == null) return BigDecimal.ZERO;
		if (o instanceof BigDecimal) return (BigDecimal) o;
		if (o instanceof Number)     return new BigDecimal(((Number) o).toString());
		try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
	}
}
