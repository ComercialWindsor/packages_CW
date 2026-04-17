package org.cwindsor.apps.form;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.compiere.apps.ADialog;
import org.compiere.apps.form.FormFrame;
import org.compiere.apps.form.FormPanel;
import org.compiere.grid.ed.VDate;
import org.compiere.grid.ed.VString;
import org.compiere.minigrid.MiniTable;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.swing.CButton;
import org.compiere.swing.CPanel;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

// Apache POI HSSF (XLS)
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * OVMuroNotProcessed - ADempiere (Java 1.6 compatible)
 * - Filtros por fecha y dirección
 * - Paginación (30)
 * - Exportar a XLS
 * - Importar seleccionados (llama SvrProcess con C_Order_ID)
 * - Selección persistente entre páginas (Seleccionar Página / Todo / Limpiar)
 * - Refresh inmediato + diferido post-proceso
 * - "Ver resultados": lista C_Order_ID, DocumentNo, DocStatus y Summary del último procesamiento
 */
public class OVMuroNotProcessed extends CPanel implements FormPanel, ActionListener {

    private static final long serialVersionUID = 1L;
    private static final CLogger log = CLogger.getCLogger(OVMuroNotProcessed.class);

    private static final int PAGE_SIZE = 30;

    private FormFrame m_frame;

    // Filtros
    private VDate fDateFrom = new VDate();
    private VDate fDateTo   = new VDate();
    private VString fDireccion = new VString();

    // Botones
    private CButton bSearch        = new CButton("Buscar");
    private CButton bClear         = new CButton("Limpiar");
    private CButton bExport        = new CButton("Exportar XLS");
    private CButton bPrev          = new CButton("« Anterior");
    private CButton bNext          = new CButton("Siguiente »");
    private CButton bImport        = new CButton("Importar");
    private CButton bSelectPage    = new CButton("Seleccionar Página");
    private CButton bSelectAll     = new CButton("Seleccionar Todo");
    private CButton bClearSelection= new CButton("Limpiar Selección");
    private CButton bShowResults   = new CButton("Ver resultados");

    // Estado inferior
    private JLabel  lblStatus = new JLabel("Registros: 0 | Página 0 de 0");
    private JProgressBar progressBar = new JProgressBar(0, 100);

    // Tabla
    private MiniTable table   = new MiniTable();

    // Datos de la página actual para exportar (sin columnas ocultas)
    private ArrayList<Vector<Object>> pageData = new ArrayList<Vector<Object>>();

    // Paginación
    private int totalRows  = 0;
    private int totalPages = 0;
    private int currentPage = 1;

    // Índices de columnas en el modelo
    private static final int COL_SEL        = 0; // Boolean
    private static final int COL_DOCNO      = 1; // String
    private static final int COL_POREF      = 2; // String
    private static final int COL_CREATED    = 3; // Timestamp
    private static final int COL_NSHOP      = 4; // String
    private static final int COL_DESCR      = 5; // String
    private static final int COL_CORDERID   = 6; // Integer (OCULTA)

    // Clase del proceso a ejecutar
    private static final String PROCESS_CLASS = "org.cwindsor.process.ImportOrderB2CRFAutByOrderId";

    // Selección persistente
    private final Set<Integer> selectedIds = new HashSet<Integer>();
    // Track de OK en esta corrida (para ocultar inmediato)
    private final Set<Integer> processedOkThisRun = new HashSet<Integer>();

    // Resultados del último procesamiento (para "Ver resultados")
    private final ArrayList<Integer> lastProcIds = new ArrayList<Integer>();
    private final ArrayList<String>  lastProcDocNos = new ArrayList<String>();
    private final ArrayList<String>  lastProcDocStatuses = new ArrayList<String>();
    private final ArrayList<String>  lastProcSummaries = new ArrayList<String>();

    public OVMuroNotProcessed() {
        super();
        setLayout(new BorderLayout());
    }

    // Helper para obtener SIEMPRE el DefaultTableModel actual de la tabla
    private DefaultTableModel tm() {
        return (DefaultTableModel) table.getModel();
    }

    // ===== FormPanel =====
    public void init (int WindowNo, FormFrame frame) {
        m_frame = frame;
        m_frame.setTitle("Órdenes (ov_ordenesmuro)");

        // Panel de filtros (NORTH)
        JPanel pFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        fDateFrom.setName("CreatedFrom");
        fDateTo.setName("CreatedTo");
        fDireccion.setName("Direccion");
        fDireccion.setColumns(20);
        pFilters.setBorder(BorderFactory.createTitledBorder("Filtros"));
        pFilters.add(new JLabel("Desde:"));
        pFilters.add(fDateFrom);
        pFilters.add(new JLabel("Hasta:"));
        pFilters.add(fDateTo);
        pFilters.add(new JLabel("Dirección (contiene):"));
        pFilters.add(fDireccion);

        bSearch.addActionListener(this);
        bClear.addActionListener(this);
        bExport.addActionListener(this);
        bSelectPage.addActionListener(this);
        bSelectAll.addActionListener(this);
        bClearSelection.addActionListener(this);
        bShowResults.addActionListener(this);
        bExport.setEnabled(false);
        bShowResults.setEnabled(false);

        pFilters.add(bSearch);
        pFilters.add(bClear);
        pFilters.add(bExport);
        pFilters.add(bSelectPage);
        pFilters.add(bSelectAll);
        pFilters.add(bClearSelection);
        pFilters.add(bShowResults);

        // Modelo de tabla
        DefaultTableModel model = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_SEL) return Boolean.class;
                if (columnIndex == COL_CREATED) return Timestamp.class;
                if (columnIndex == COL_CORDERID) return Integer.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == COL_SEL; // solo checkbox editable
            }
        };
        model.addColumn("Seleccionar");   // 0
        model.addColumn("Documento");     // 1
        model.addColumn("Orden Shopify"); // 2
        model.addColumn("Creado");        // 3
        model.addColumn("Nombre Shopify");// 4
        model.addColumn("Descripcion");   // 5
        model.addColumn("C_Order_ID");    // 6 (oculta)

        table.setModel(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowSelectionAllowed(true);

        // Forzar editabilidad del checkbox en MiniTable
        try {
            table.setColumnClass(COL_SEL, Boolean.class, false); // false => editable
            table.setColumnClass(COL_DOCNO, String.class, true);
            table.setColumnClass(COL_POREF, String.class, true);
            table.setColumnClass(COL_CREATED, Timestamp.class, true);
            table.setColumnClass(COL_NSHOP, String.class, true);
            table.setColumnClass(COL_DESCR, String.class, true);
            table.setColumnClass(COL_CORDERID, Integer.class, true);
        } catch (Throwable ignore) {}

        // Listener para mantener selectedIds sincronizado
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() != TableModelEvent.UPDATE) return;
                int col = e.getColumn();
                if (col != COL_SEL) return;
                int row = e.getFirstRow();
                DefaultTableModel m = tm();
                if (row < 0 || row >= m.getRowCount()) return;

                Object idObj = m.getValueAt(row, COL_CORDERID);
                if (!(idObj instanceof Number)) return;
                int id = ((Number) idObj).intValue();

                boolean checked = Boolean.TRUE.equals(m.getValueAt(row, COL_SEL));
                if (checked) {
                    selectedIds.add(Integer.valueOf(id));
                } else {
                    selectedIds.remove(Integer.valueOf(id));
                }
                updateStatus();
            }
        });

        // Anchos visibles
        table.getColumnModel().getColumn(COL_SEL).setPreferredWidth(90);
        table.getColumnModel().getColumn(COL_DOCNO).setPreferredWidth(120);
        table.getColumnModel().getColumn(COL_POREF).setPreferredWidth(140);
        table.getColumnModel().getColumn(COL_CREATED).setPreferredWidth(140);
        table.getColumnModel().getColumn(COL_NSHOP).setPreferredWidth(220);
        table.getColumnModel().getColumn(COL_DESCR).setPreferredWidth(260);

        // Ocultar C_Order_ID
        TableColumn hidden = table.getColumnModel().getColumn(COL_CORDERID);
        hidden.setMinWidth(0);
        hidden.setMaxWidth(0);
        hidden.setPreferredWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultados"));

        add(pFilters, BorderLayout.NORTH);
        add(scroll,   BorderLayout.CENTER);

        // Barra inferior
        bPrev.addActionListener(this);
        bNext.addActionListener(this);
        bPrev.setEnabled(false);
        bNext.setEnabled(false);

        bImport.addActionListener(this);
        bImport.setToolTipText("Importa las filas seleccionadas llamando al proceso y pasando C_Order_ID");

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("");

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        south.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        south.add(lblStatus);
        south.add(bPrev);
        south.add(bNext);
        south.add(progressBar);
        south.add(bImport);
        m_frame.getContentPane().add(south, BorderLayout.SOUTH);

        updateStatus();
        m_frame.getContentPane().add(this, BorderLayout.CENTER);
    }

    public void dispose() { }

    // ===== Eventos =====
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == bSearch) {
            onSearch();
        } else if (src == bClear) {
            onClear();
        } else if (src == bExport) {
            onExportXls();
        } else if (src == bImport) {
            onImportSelected();
        } else if (src == bPrev) {
            if (currentPage > 1) { currentPage--; loadPage(); }
        } else if (src == bNext) {
            if (currentPage < totalPages) { currentPage++; loadPage(); }
        } else if (src == bSelectPage) {
            onSelectPage();
        } else if (src == bSelectAll) {
            onSelectAll();
        } else if (src == bClearSelection) {
            onClearSelection();
        } else if (src == bShowResults) {
            onShowResults();
        }
    }

    private void onClear() {
        fDateFrom.setValue(null);
        fDateTo.setValue(null);
        fDireccion.setValue("");
        selectedIds.clear();
        processedOkThisRun.clear();
        clearLastResults();
        clearTable();
        pageData.clear();
        bExport.setEnabled(false);
        bShowResults.setEnabled(false);
        totalRows = 0;
        totalPages = 0;
        currentPage = 1;
        bPrev.setEnabled(false);
        bNext.setEnabled(false);
        updateStatus();
        progressBar.setValue(0);
        progressBar.setString("");
    }

    private void onSearch() {
        // Mantengo selectedIds? preferible limpiar al cambiar filtros
        selectedIds.clear();
        processedOkThisRun.clear();
        clearLastResults();
        pageData.clear();
        bExport.setEnabled(false);
        bShowResults.setEnabled(false);

        totalRows = countTotalRows();
        totalPages = (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
        if (totalPages <= 0) totalPages = 0;
        currentPage = (totalPages > 0) ? 1 : 0;

        bPrev.setEnabled(false);
        bNext.setEnabled(totalPages > 1);
        loadPage();
    }

    private void clearTable() {
        tm().setRowCount(0);
    }

    /** Carga página actual */
    private void loadPage() {
        clearTable();
        pageData.clear();

        if (totalRows <= 0 || totalPages <= 0 || currentPage <= 0) {
            updateStatus();
            return;
        }

        int from = (currentPage - 1) * PAGE_SIZE + 1;
        int to   = Math.min(currentPage * PAGE_SIZE, totalRows);

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        StringBuffer where = new StringBuffer();
        ArrayList<Object> params = new ArrayList<Object>();
        buildWhere(where, params);

        StringBuffer sql = new StringBuffer();
        sql.append("WITH Q AS (")
           .append("  SELECT ")
           .append("    o.C_ORDER_ID, ")
           .append("    MIN(o.DOCUMENTNO)    AS DOCUMENTNO, ")
           .append("    MIN(o.POREFERENCE)   AS POREFERENCE, ")
           .append("    MIN(o.CREATED)       AS CREATED, ")
           .append("    MIN(o.NOMBRESHOPIFY) AS NOMBRESHOPIFY, ")
           .append("    MIN(o.DESCRIPTION)   AS DESCRIPTION, ")
           .append("    ROW_NUMBER() OVER (ORDER BY MIN(o.CREATED) DESC) AS RN ")
           .append("  FROM ov_ordenesmuro@erpmuro o ")
           .append("  WHERE 1=1 ")
           .append(where.toString())
           .append("  GROUP BY o.C_ORDER_ID ")
           .append(") ")
           .append("SELECT C_ORDER_ID, DOCUMENTNO, POREFERENCE, CREATED, NOMBRESHOPIFY, DESCRIPTION ")
           .append("FROM Q ")
           .append("WHERE RN BETWEEN ? AND ? ")
           .append("ORDER BY RN");

        try {
            pstmt = DB.prepareStatement(sql.toString(), null);
            int idx = 1;
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Timestamp) pstmt.setTimestamp(idx++, (Timestamp)p);
                else                         pstmt.setString(idx++, String.valueOf(p));
            }
            pstmt.setInt(idx++, from);
            pstmt.setInt(idx++, to);

            rs = pstmt.executeQuery();
            DefaultTableModel model = tm();
            while (rs.next()) {
                int cOrder = rs.getInt("C_ORDER_ID");

                // Ocultar si ya fue procesado OK en esta sesión (feedback inmediato)
                if (processedOkThisRun.contains(Integer.valueOf(cOrder))) {
                    continue;
                }

                String docNo   = rs.getString("DOCUMENTNO");
                String poRef   = rs.getString("POREFERENCE");
                Timestamp cre  = rs.getTimestamp("CREATED");
                String nShop   = rs.getString("NOMBRESHOPIFY");
                String descr   = rs.getString("DESCRIPTION");

                // Datos para exportar (sin selección ni C_Order_ID)
                Vector<Object> line = new Vector<Object>(5);
                line.add(docNo);
                line.add(poRef);
                line.add(cre);
                line.add(nShop);
                line.add(descr);
                pageData.add(line);

                // El checkbox se marca si el ID está seleccionado globalmente
                Boolean sel = Boolean.valueOf(selectedIds.contains(Integer.valueOf(cOrder)));

                // Agregar fila a la tabla (incluye checkbox y C_Order_ID oculto)
                model.addRow(new Object[] { sel, docNo, poRef, cre, nShop, descr, Integer.valueOf(cOrder) });
            }

            bExport.setEnabled(!pageData.isEmpty());
            bPrev.setEnabled(currentPage > 1);
            bNext.setEnabled(currentPage < totalPages);

        } catch (SQLException ex) {
            log.severe("Error consultando página: " + ex.getMessage());
            ADialog.error(0, m_frame, "Error consultando datos (paginado):\n" + ex.getMessage());
            bExport.setEnabled(false);
        } finally {
            DB.close(rs, pstmt);
        }

        updateStatus();
        progressBar.setValue(0);
        progressBar.setString("");
    }

    private void buildWhere(StringBuffer where, ArrayList<Object> params) {
        Timestamp tsFrom = getTimestamp(fDateFrom.getValue());
        if (tsFrom != null) {
            where.append(" AND TRUNC(o.CREATED) >= TRUNC(?) ");
            params.add(tsFrom);
        }

        Timestamp tsTo = getTimestamp(fDateTo.getValue());
        if (tsTo != null) {
            where.append(" AND TRUNC(o.CREATED) <= TRUNC(?) ");
            params.add(tsTo);
        }

        String dir = fDireccion.getText();
        if (dir != null && dir.trim().length() > 0) {
            where.append(" AND UPPER(o.DIRECCIONSHOPIFY) LIKE '%' || UPPER(?) || '%' ");
            params.add(dir.trim());
        }
    }

    private int countTotalRows() {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        StringBuffer where = new StringBuffer();
        ArrayList<Object> params = new ArrayList<Object>();
        buildWhere(where, params);

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(*) AS CNT ")
           .append("FROM ( ")
           .append("  SELECT o.C_ORDER_ID ")
           .append("  FROM ov_ordenesmuro@erpmuro o ")
           .append("  WHERE 1=1 ")
           .append(where.toString())
           .append("  GROUP BY o.C_ORDER_ID ")
           .append(")");

        try {
            pstmt = DB.prepareStatement(sql.toString(), null);
            int idx = 1;
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Timestamp) pstmt.setTimestamp(idx++, (Timestamp)p);
                else                         pstmt.setString(idx++, String.valueOf(p));
            }
            rs = pstmt.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException ex) {
            log.severe("Error contando registros: " + ex.getMessage());
            ADialog.error(0, m_frame, "Error contando registros:\n" + ex.getMessage());
        } finally {
            DB.close(rs, pstmt);
        }

        return count;
    }

    private Timestamp getTimestamp(Object v) {
        if (v == null) return null;
        if (v instanceof Timestamp) return (Timestamp)v;
        if (v instanceof Date) return new Timestamp(((Date)v).getTime());
        return null;
    }

    // ===== Selecciones =====
    /** Seleccionar todos los registros de la página actual */
    private void onSelectPage() {
        DefaultTableModel model = tm();
        int rows = model.getRowCount();
        for (int r = 0; r < rows; r++) {
            Object idObj = model.getValueAt(r, COL_CORDERID);
            if (!(idObj instanceof Number)) continue;
            int id = ((Number) idObj).intValue();
            selectedIds.add(Integer.valueOf(id));
            model.setValueAt(Boolean.TRUE, r, COL_SEL);
        }
        updateStatus();
    }

    /** Seleccionar todos los registros de todos los resultados (según filtros actuales) */
    private void onSelectAll() {
        java.util.List<Integer> allIds = fetchAllMatchingOrderIds();
        for (int i = 0; i < allIds.size(); i++) {
            selectedIds.add(allIds.get(i));
        }
        // Reflejar en la página visible
        DefaultTableModel model = tm();
        for (int r = 0; r < model.getRowCount(); r++) {
            Object idObj = model.getValueAt(r, COL_CORDERID);
            if (idObj instanceof Number && selectedIds.contains(Integer.valueOf(((Number) idObj).intValue()))) {
                model.setValueAt(Boolean.TRUE, r, COL_SEL);
            }
        }
        updateStatus();
    }

    /** Limpiar toda la selección */
    private void onClearSelection() {
        selectedIds.clear();
        DefaultTableModel model = tm();
        for (int r = 0; r < model.getRowCount(); r++) {
            model.setValueAt(Boolean.FALSE, r, COL_SEL);
        }
        updateStatus();
    }

    /** Trae todos los C_Order_ID que cumplen el filtro actual (sin paginar) */
    private java.util.List<Integer> fetchAllMatchingOrderIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        StringBuffer where = new StringBuffer();
        ArrayList<Object> params = new ArrayList<Object>();
        buildWhere(where, params);

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT o.C_ORDER_ID ")
           .append("FROM ov_ordenesmuro@erpmuro o ")
           .append("WHERE 1=1 ")
           .append(where.toString())
           .append("GROUP BY o.C_ORDER_ID");

        try {
            pstmt = DB.prepareStatement(sql.toString(), null);
            int idx = 1;
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Timestamp) pstmt.setTimestamp(idx++, (Timestamp)p);
                else                         pstmt.setString(idx++, String.valueOf(p));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ids.add(Integer.valueOf(rs.getInt(1)));
            }
        } catch (SQLException ex) {
            log.severe("Error obteniendo todos los IDs: " + ex.getMessage());
            ADialog.error(0, m_frame, "Error obteniendo todos los IDs:\n" + ex.getMessage());
        } finally {
            DB.close(rs, pstmt);
        }
        return ids;
    }

    // ===== Importar seleccionados =====
    private void onImportSelected() {
        if (selectedIds.isEmpty()) {
            ADialog.info(0, m_frame, "No hay filas seleccionadas. Use los checkboxes o los botones de selección.");
            return;
        }

        final ArrayList<Integer> selected = new ArrayList<Integer>(selectedIds);

        bImport.setEnabled(false);
        bPrev.setEnabled(false);
        bNext.setEnabled(false);
        bSearch.setEnabled(false);
        bClear.setEnabled(false);
        bExport.setEnabled(false);
        bSelectPage.setEnabled(false);
        bSelectAll.setEnabled(false);
        bClearSelection.setEnabled(false);
        bShowResults.setEnabled(false);

        final Cursor old = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        progressBar.setMinimum(0);
        progressBar.setMaximum(selected.size());
        progressBar.setValue(0);
        progressBar.setString("0 / " + selected.size());

        new SwingWorker<Void, Integer>() {
            int okCount = 0;
            int errCount = 0;
            StringBuilder sbSummary = new StringBuilder();
            final ArrayList<Integer> okIds = new ArrayList<Integer>();

            @Override
            protected Void doInBackground() {
                final int total = selected.size();
                for (int i = 0; i < total; i++) {
                    int cOrderId = selected.get(i).intValue();
                    String documentNo = getDocumentNo(cOrderId);
                    ProcResult res = runProcessForOrder(cOrderId);
                    if (res.ok) {
                        okCount++;
                        okIds.add(Integer.valueOf(cOrderId));
                    } else {
                        errCount++;
                    }

                    sbSummary.append("Documento ")
                             .append(documentNo != null ? documentNo : cOrderId)
                             .append(": ")
                             .append(res.summary != null ? res.summary : (res.ok ? "OK" : "Error"))
                             .append("\n");

                    publish(Integer.valueOf(i + 1));
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int current = chunks.get(chunks.size() - 1).intValue();
                progressBar.setValue(current);
                progressBar.setString(current + " / " + selected.size());
            }

            @Override
            protected void done() {
                try { get(); } catch (Exception ignore) {}

                setCursor(old);
                bImport.setEnabled(true);
                bSearch.setEnabled(true);
                bClear.setEnabled(true);
                bPrev.setEnabled(currentPage > 1);
                bNext.setEnabled(currentPage < totalPages);
                bExport.setEnabled(!pageData.isEmpty());
                bSelectPage.setEnabled(true);
                bSelectAll.setEnabled(true);
                bClearSelection.setEnabled(true);

                processedOkThisRun.clear();
                processedOkThisRun.addAll(okIds);
                removeOkFromCurrentPage(okIds);

                fillLastResults(selected, sbSummary.toString());
                bShowResults.setEnabled(!lastProcIds.isEmpty());

                onSearch();
                refreshAfterProcessWithDelay(1200);

                //  Mensaje final con documento(s)
                String msg = "Importación finalizada.\nOK: " + okCount + " | Error: " + errCount;
                ADialog.info(0, m_frame, msg + "\n\n" + sbSummary.toString());
            }
        }.execute();
    }

    /** Obtiene el DocumentNo de C_Order para mostrar en el mensaje */
    private String getDocumentNo(int cOrderId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String docNo = null;
        try {
            ps = DB.prepareStatement("SELECT DocumentNo FROM C_Order WHERE C_Order_ID=?", null);
            ps.setInt(1, cOrderId);
            rs = ps.executeQuery();
            if (rs.next())
                docNo = rs.getString(1);
        } catch (Exception ex) {
            log.warning("No se pudo obtener DocumentNo para " + cOrderId + ": " + ex.getMessage());
        } finally {
            DB.close(rs, ps);
        }
        return docNo;
    }


    /** Ejecuta el proceso SvrProcess para un C_Order_ID específico */
    private ProcResult runProcessForOrder(int cOrderId) {
        ProcessInfo pi = new ProcessInfo("Importar OV desde Muro", 0);
        pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));

        // Pasar parámetro C_Order_ID
        ProcessInfoParameter[] params = new ProcessInfoParameter[] {
            new ProcessInfoParameter("C_Order_ID", Integer.valueOf(cOrderId), null, null, null)
        };
        pi.setParameter(params);

        // Ejecutar el SvrProcess por classname
        return runSvrProcessByClassname(PROCESS_CLASS, pi);
    }

    /** Resultado de ejecución del proceso */
    private static class ProcResult {
        final boolean ok;
        final String summary;
        ProcResult(boolean ok, String summary) {
            this.ok = ok;
            this.summary = summary;
        }
    }

    /**
     * Ejecuta un proceso (SvrProcess) por Classname usando startProcess(ctx, pi, trx).
     * Maneja transacción: commit si ok, rollback si falla.
     * Compatible con ADempiere 3.6.0+.
     */
    private ProcResult runSvrProcessByClassname(String className, ProcessInfo pi) {
        Trx trx = null;
        try {
            Class<?> clazz = Class.forName(className);
            if (!SvrProcess.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("La clase no extiende SvrProcess: " + className);
            }
            SvrProcess proc = (SvrProcess) clazz.getDeclaredConstructor().newInstance();

            String trxName = Trx.createTrxName("RUNPROC");
            trx = Trx.get(trxName, true);

            boolean ok = proc.startProcess(Env.getCtx(), pi, trx);
            if (ok) trx.commit(); else trx.rollback();

            String summary = pi.getSummary();
            if (summary == null || summary.trim().isEmpty()) summary = ok ? "OK" : "Error";
            return new ProcResult(ok, summary);

        } catch (Throwable t) {
            if (trx != null) { try { trx.rollback(); } catch (Exception ignore) {} }
            log.severe("Error ejecutando proceso: " + t.getMessage());
            String shortMsg = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
            return new ProcResult(false, "Error: " + shortMsg);
        } finally {
            if (trx != null) { try { trx.close(); } catch (Exception ignore) {} }
        }
    }

    // ===== Resultados post-proceso =====

    /** Llena las listas de resultados consultando C_Order (DocumentNo, DocStatus) y asociando el summary */
    private void fillLastResults(ArrayList<Integer> processedIds, String bigSummary) {
        clearLastResults();
        if (processedIds == null || processedIds.isEmpty()) return;

        // Parsear summaries por línea para mapear rápido C_Order_ID -> summary
        java.util.HashMap<Integer,String> mapSummary = new java.util.HashMap<Integer,String>();
        if (bigSummary != null && bigSummary.length() > 0) {
            String[] lines = bigSummary.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                // formato: "C_Order_ID 123: <texto>"
                int idx = line.indexOf("C_Order_ID ");
                if (idx >= 0) {
                    String rest = line.substring(idx + "C_Order_ID ".length()).trim();
                    int colon = rest.indexOf(':');
                    if (colon > 0) {
                        String idStr = rest.substring(0, colon).trim();
                        try {
                            Integer id = Integer.valueOf(Integer.parseInt(idStr));
                            String sm = rest.substring(colon + 1).trim();
                            mapSummary.put(id, sm);
                        } catch (Exception ignore) {}
                    }
                }
            }
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DB.prepareStatement(
                "SELECT DocumentNo, DocStatus FROM C_Order WHERE C_Order_ID = ?", null
            );
            for (int i = 0; i < processedIds.size(); i++) {
                Integer id = processedIds.get(i);
                ps.setInt(1, id.intValue());
                rs = ps.executeQuery();
                String docNo = null;
                String docSt = null;
                if (rs.next()) {
                    docNo = rs.getString(1);
                    docSt = rs.getString(2);
                }
                DB.close(rs, null); rs = null;

                lastProcIds.add(id);
                lastProcDocNos.add(docNo != null ? docNo : "");
                lastProcDocStatuses.add(docSt != null ? docSt : "");
                String sm = mapSummary.get(id);
                lastProcSummaries.add(sm != null ? sm : "");
            }
        } catch (SQLException ex) {
            log.severe("Error consultando C_Order para resultados: " + ex.getMessage());
        } finally {
            DB.close(rs, ps);
        }
    }

    private void clearLastResults() {
        lastProcIds.clear();
        lastProcDocNos.clear();
        lastProcDocStatuses.clear();
        lastProcSummaries.clear();
    }

    private void onShowResults() {
        if (lastProcIds.isEmpty()) {
            ADialog.info(0, m_frame, "No hay resultados del último procesamiento.");
            return;
        }
        // Crear tabla con 4 columnas
        DefaultTableModel m = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        m.addColumn("C_Order_ID");
        m.addColumn("Documento");
        m.addColumn("DocStatus");
        m.addColumn("Resultado");

        for (int i = 0; i < lastProcIds.size(); i++) {
            Object[] row = new Object[] {
                lastProcIds.get(i),
                lastProcDocNos.get(i),
                lastProcDocStatuses.get(i),
                lastProcSummaries.get(i)
            };
            m.addRow(row);
        }

        JTable t = new JTable(m);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.getColumnModel().getColumn(0).setPreferredWidth(90);
        t.getColumnModel().getColumn(1).setPreferredWidth(140);
        t.getColumnModel().getColumn(2).setPreferredWidth(100);
        t.getColumnModel().getColumn(3).setPreferredWidth(420);

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createTitledBorder("Resultados del último procesamiento"));

        // *** HAZLA FINAL para poder usarla en el ActionListener (Java 1.6) ***
        final JDialog dlg = new JDialog((Frame) null, "Resultados", true);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        CButton close = new CButton("Cerrar");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dlg.dispose(); // ahora compila en 1.6 porque 'dlg' es final
            }
        });
        south.add(close);
        dlg.getContentPane().add(south, BorderLayout.SOUTH);

        dlg.setSize(800, 400);
        dlg.setLocationRelativeTo(m_frame);
        dlg.setVisible(true);
    }


    /** Quita de la tabla visible las filas cuyos IDs estén en okIds */
    private void removeOkFromCurrentPage(java.util.List<Integer> okIds) {
        if (okIds == null || okIds.isEmpty()) return;
        DefaultTableModel model = tm();
        for (int r = model.getRowCount() - 1; r >= 0; r--) {
            Object idObj = model.getValueAt(r, COL_CORDERID);
            if (idObj instanceof Number) {
                int id = ((Number) idObj).intValue();
                if (okIds.contains(Integer.valueOf(id))) {
                    model.removeRow(r);
                    selectedIds.remove(Integer.valueOf(id));
                }
            }
        }
        if (totalRows > 0) {
            totalRows = Math.max(0, totalRows - okIds.size());
            totalPages = (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
            if (currentPage > totalPages) currentPage = totalPages;
        }
        updateStatus();
    }

    /** Lanza un refresco diferido para dar tiempo a que la vista remota refleje los cambios */
    private void refreshAfterProcessWithDelay(int millis) {
        try {
            javax.swing.Timer t = new javax.swing.Timer(millis, new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onSearch();
                }
            });
            t.setRepeats(false);
            t.start();
        } catch (Throwable ignore) {
            onSearch();
        }
    }

    // ===== Exportar XLS =====
    private void onExportXls() {
        if (pageData == null || pageData.isEmpty()) {
            ADialog.info(0, m_frame, "No hay datos para exportar.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
        String ts = sdf.format(new Date());
        String suggested = "OrdenesMuro_" + ts + ".xls";

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(suggested));
        int r = chooser.showSaveDialog(m_frame);
        if (r != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = chooser.getSelectedFile();

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Ordenes");

        // Estilo header
        HSSFFont headerFont = wb.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFont(headerFont);

        // Estilo fecha
        HSSFCellStyle dateStyle = wb.createCellStyle();
        HSSFDataFormat df = wb.createDataFormat();
        short dateFmt = df.getFormat("m/d/yy h:mm");
        dateStyle.setDataFormat(dateFmt);

        // Header
        String[] headers = new String[] { "Documento", "Orden Shopify", "Creado", "Nombre Shopify", "Descripcion" };
        HSSFRow r0 = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell c = r0.createCell(i);
            c.setCellType(HSSFCell.CELL_TYPE_STRING);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        // Datos (página visible)
        int rowIdx = 1;
        for (int i = 0; i < pageData.size(); i++) {
            Vector<Object> v = pageData.get(i);
            HSSFRow xr = sheet.createRow(rowIdx++);

            HSSFCell c0 = xr.createCell((short)0);
            c0.setCellType(HSSFCell.CELL_TYPE_STRING);
            c0.setCellValue(v.get(0) != null ? String.valueOf(v.get(0)) : "");

            HSSFCell c1 = xr.createCell((short)1);
            c1.setCellType(HSSFCell.CELL_TYPE_STRING);
            c1.setCellValue(v.get(1) != null ? String.valueOf(v.get(1)) : "");

            HSSFCell c2 = xr.createCell((short)2);
            Object createdObj = v.get(2);
            if (createdObj instanceof Timestamp) {
                c2.setCellValue(new Date(((Timestamp) createdObj).getTime()));
                c2.setCellStyle(dateStyle);
            } else {
                c2.setCellType(HSSFCell.CELL_TYPE_STRING);
                c2.setCellValue(createdObj != null ? String.valueOf(createdObj) : "");
            }

            HSSFCell c3 = xr.createCell((short)3);
            c3.setCellType(HSSFCell.CELL_TYPE_STRING);
            c3.setCellValue(v.get(3) != null ? String.valueOf(v.get(3)) : "");

            HSSFCell c4 = xr.createCell((short)4);
            c4.setCellType(HSSFCell.CELL_TYPE_STRING);
            c4.setCellValue(v.get(4) != null ? String.valueOf(v.get(4)) : "");
        }

        // Anchos
        for (short i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, (short) 6000);
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            wb.write(fos);
            fos.flush();
            ADialog.info(0, m_frame, "Archivo exportado:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            log.severe("Error exportando XLS: " + ex.getMessage());
            ADialog.error(0, m_frame, "Error exportando XLS:\n" + ex.getMessage());
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (IOException ignore) {}
            }
        }
    }

    // ---- Helpers UI ----
    private void updateStatus() {
        String pageTxt = (totalPages <= 0 || currentPage <= 0)
                ? "Página 0 de 0"
                : ("Página " + currentPage + " de " + totalPages);
        lblStatus.setText("Registros: " + totalRows + " | " + pageTxt + " | Seleccionados: " + selectedIds.size());
    }
}
