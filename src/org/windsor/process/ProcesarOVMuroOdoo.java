package org.windsor.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.compiere.model.MOrder;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

public class ProcesarOVMuroOdoo extends SvrProcess {

	private int m_AD_Client_ID = 1000000;
	private int m_AD_Org_ID = 1000000;
	private int m_AD_User_ID = 0;

	/** Tamano del lote: cantidad de pares TIENDA+ODOO_ID distintos por ciclo */
	private static final int BATCH_SIZE = 20;

	// Constantes de negocio
	private static final int BPARTNER_ID = 1001237;        // INVERSIONES MURO LTDA.
	private static final int PAYMENT_TERM_ID = 1000007;     // Contado
	private static final int PRICELIST_ID = 1000040;        // Muro
	private static final int WAREHOUSE_ID = 1000001;        // Lampa
	private static final int DOCTYPE_ID = 1000030;          // Orden de Venta

	// Contadores globales
	private int ordersCreated = 0;
	private int ordersCompleted = 0;
	private int linesProcessed = 0;
	private int linesSkipped = 0;

	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = 1000000;
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = 1000000;
			else if (name.equals("CreatedBy"))
				m_AD_User_ID = new BigDecimal(para[i].getParameter().toString()).intValue();
			else if (name.equals("DeleteOldImported"))
				;
			else if (name.equals("DocAction"))
				;
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	protected String doIt() throws Exception {

		log.info("=== INICIO ProcesarOVMuroOdoo === PInstance=" + getAD_PInstance_ID());

		// --- Verificar si ImportOrderB2CRFAutOdoo esta en ejecucion ---
		if (isOtherProcessRunning(1000778)) {
			String msg = "ImportOrderB2CRFAutOdoo (1000778) en ejecucion. Se pospone para evitar bloqueos.";
			log.info(msg);
			addLog(0, null, null, msg);
			return msg;
		}

		// --- Paso 0: Actualizar cantidad usada de reservas fisicas ---
		actualizarQtyUsed();
		commitEx();

		// --- Paso 1: Eliminar lineas con cantidad 0 ---
		ejecutarSQL("DELETE FROM I_SALESIMPORT WHERE cantidad = 0");
		commitEx();

		// --- Paso 2: Liberar registros huerfanos de instancias que ya terminaron ---
		liberarProcessingHuerfano();
		commitEx();

		// --- Paso 3: Contar total de pares TIENDA+ODOO_ID pendientes ---
		int totalPares = DB.getSQLValue(get_TrxName(),
			"SELECT COUNT(*) FROM (SELECT DISTINCT TIENDA, ODOO_ID FROM I_SALESIMPORT "
			+ "WHERE I_ISIMPORTED = 'N' AND CODIGO IS NOT NULL AND NVL(Processing,'N') = 'N')");
		if (totalPares < 0) totalPares = 0;

		int totalLineas = DB.getSQLValue(get_TrxName(),
			"SELECT COUNT(*) FROM I_SALESIMPORT "
			+ "WHERE I_ISIMPORTED = 'N' AND CODIGO IS NOT NULL AND NVL(Processing,'N') = 'N'");
		if (totalLineas < 0) totalLineas = 0;

		addLog(0, null, null, "=== Total pendiente: " + totalPares + " ODOO_IDs, " + totalLineas + " lineas ===");
		log.info("Total pendiente: " + totalPares + " ODOO_IDs, " + totalLineas + " lineas");

		if (totalPares == 0) {
			return "Sin registros pendientes de procesar.";
		}

		int paresProcesados = 0;
		int batchNumber = 0;

		// =====================================================================
		// LOOP PRINCIPAL POR LOTES DE TIENDA+ODOO_ID
		// =====================================================================
		while (true) {
			batchNumber++;

			// --- Reclamar un lote de pares TIENDA+ODOO_ID ---
			List /*<String[]>*/ paresLote = claimBatch(BATCH_SIZE);
			if (paresLote.isEmpty()) {
				log.info("No hay mas pares pendientes. Fin.");
				break;
			}

			addLog(0, null, null, "--- Lote #" + batchNumber + ": " + paresLote.size()
				+ " ODOO_IDs reclamados (acumulado " + paresProcesados + "/" + totalPares + ") ---");

			// --- Procesar cada par TIENDA+ODOO_ID del lote ---
			for (int t = 0; t < paresLote.size(); t++) {
				String[] par = (String[]) paresLote.get(t);
				String tienda = par[0];
				String odooId = par[1];
				paresProcesados++;

				addLog(0, null, null, "  Procesando " + paresProcesados + "/" + totalPares
					+ " [" + tienda + " / ODOO=" + odooId + "]");

				try {
					processOneTiendaOdoo(tienda, odooId);
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error procesando [" + tienda + "/" + odooId + "]: " + e.getMessage(), e);
					addLog(0, null, null, "  ERROR [" + tienda + "/" + odooId + "]: " + e.getMessage());
					markParReleased(tienda, odooId);
				}
			}

			// --- Commit del lote ---
			commitEx();
			addLog(0, null, null, "--- Lote #" + batchNumber + " completado. "
				+ "Ordenes=" + ordersCompleted + " Lineas OK=" + linesProcessed
				+ " Lineas skip=" + linesSkipped + " ---");
			log.info("Lote #" + batchNumber + " completado.");
		}

		addLog(0, null, null, "=== PROCESO TERMINADO. Ordenes completadas=" + ordersCompleted
			+ " Lineas procesadas=" + linesProcessed + " Lineas omitidas=" + linesSkipped + " ===");

		return "Proceso terminado - Ordenes completadas: " + ordersCompleted
			+ " - Lineas procesadas: " + linesProcessed
			+ " - Lineas omitidas: " + linesSkipped;
	}

	// =========================================================================
	// CONTROL DE LOTES
	// =========================================================================

	/**
	 * Reclama un lote de pares TIENDA+ODOO_ID distintos marcandolos con Processing='Y'
	 * y estampando el AD_PInstance_ID de esta ejecucion.
	 * @return lista de String[] {tienda, odooId} reclamados
	 */
	private List /*<String[]>*/ claimBatch(int batchSize) {
		// Marcar las lineas de N pares TIENDA+ODOO_ID con Processing='Y' y AD_PInstance_ID
		String sqlClaim =
			"UPDATE I_SALESIMPORT SET Processing = 'Y', "
			+ "AD_PInstance_ID = " + getAD_PInstance_ID() + " "
			+ "WHERE I_ISIMPORTED = 'N' AND CODIGO IS NOT NULL "
			+ "AND NVL(Processing,'N') = 'N' "
			+ "AND (TIENDA, ODOO_ID) IN ("
			+ "  SELECT TIENDA, ODOO_ID FROM ("
			+ "    SELECT DISTINCT TIENDA, ODOO_ID FROM I_SALESIMPORT "
			+ "    WHERE I_ISIMPORTED = 'N' AND CODIGO IS NOT NULL "
			+ "    AND NVL(Processing,'N') = 'N' "
			+ "    ORDER BY TIENDA, ODOO_ID"
			+ "  ) WHERE ROWNUM <= " + batchSize
			+ ")";
		int rows = DB.executeUpdate(sqlClaim, get_TrxName());
		log.info("claimBatch: " + rows + " filas marcadas con Processing='Y' PInstance=" + getAD_PInstance_ID());

		// Obtener los pares reclamados por ESTA instancia
		List /*<String[]>*/ pares = new ArrayList();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sqlPares = "SELECT DISTINCT TIENDA, ODOO_ID FROM I_SALESIMPORT "
				+ "WHERE I_ISIMPORTED = 'N' AND CODIGO IS NOT NULL "
				+ "AND Processing = 'Y' AND AD_PInstance_ID = " + getAD_PInstance_ID() + " "
				+ "ORDER BY TIENDA, ODOO_ID";
			pst = DB.prepareStatement(sqlPares, get_TrxName());
			rs = pst.executeQuery();
			while (rs.next()) {
				String[] par = new String[2];
				par[0] = rs.getString("TIENDA");
				par[1] = rs.getString("ODOO_ID");
				pares.add(par);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error obteniendo pares del lote: " + e.getMessage(), e);
		} finally {
			closeQuietly(rs);
			closeQuietly(pst);
		}
		return pares;
	}

	/**
	 * Libera registros que quedaron con Processing='Y' de ejecuciones anteriores
	 * cuya AD_PInstance ya NO esta activa (IsProcessing='Y').
	 * NO toca registros de instancias que aun estan corriendo.
	 */
	private void liberarProcessingHuerfano() {
		String sql = "UPDATE I_SALESIMPORT SET Processing = 'N', AD_PInstance_ID = NULL "
			+ "WHERE Processing = 'Y' AND I_ISIMPORTED = 'N' "
			+ "AND (AD_PInstance_ID IS NULL "
			+ "  OR AD_PInstance_ID NOT IN ("
			+ "    SELECT AD_PInstance_ID FROM AD_PInstance "
			+ "    WHERE IsProcessing = 'Y'"
			+ "  ))";
		int rows = DB.executeUpdate(sql, get_TrxName());
		if (rows > 0) {
			addLog(0, null, null, "Liberados " + rows + " registros con Processing huerfano.");
			log.info("Liberados " + rows + " registros con Processing huerfano.");
		}
	}

	/**
	 * Libera el Processing de un par TIENDA+ODOO_ID (en caso de error).
	 */
	private void markParReleased(String tienda, String odooId) {
		String sql = "UPDATE I_SALESIMPORT SET Processing = 'N', AD_PInstance_ID = NULL "
			+ "WHERE TIENDA = '" + tienda + "' AND ODOO_ID = '" + odooId + "' AND I_ISIMPORTED = 'N'";
		DB.executeUpdate(sql, get_TrxName());
	}

	/**
	 * Marca las lineas de una tienda+odooId+codigo como importadas.
	 */
	private void markLineImported(String codigo, String tienda, String odooId, int orderId) {
		String sql = "UPDATE I_SALESIMPORT SET I_ISIMPORTED = 'Y', Processing = 'Y', "
			+ "AD_PInstance_ID = NULL, "
			+ "C_Order_ID = " + orderId
			+ " WHERE CODIGO = '" + codigo + "' AND TIENDA = '" + tienda + "'"
			+ " AND ODOO_ID = '" + odooId + "'";
		ejecutarSQL(sql);
	}

	/**
	 * Revierte las lineas de una tienda+odooId+codigo a no importadas.
	 */
	private void markLineNotImported(String codigo, String tienda, String odooId) {
		String sql = "UPDATE I_SALESIMPORT SET I_ISIMPORTED = 'N', Processing = 'N', "
			+ "AD_PInstance_ID = NULL, "
			+ "C_Order_ID = null "
			+ "WHERE CODIGO = '" + codigo + "' AND TIENDA = '" + tienda + "'"
			+ " AND ODOO_ID = '" + odooId + "'";
		ejecutarSQL(sql);
	}

	/**
	 * Marca las lineas como importadas con error (producto no encontrado, etc).
	 * Usa I_ISIMPORTED='E' para que no se re-procesen pero queden identificables.
	 * NOTA: Si I_SALESIMPORT tiene columna I_ERRORMSG, descomentar la linea correspondiente.
	 */
	private void markLineImportedError(String codigo, String tienda, String odooId, String errorMsg) {
		String sql = "UPDATE I_SALESIMPORT SET I_ISIMPORTED = 'E', Processing = 'Y', "
			+ "AD_PInstance_ID = NULL, "
			+ "I_ERRORMSG = '" + errorMsg.replaceAll("'", "") + "' "
			+ "WHERE CODIGO = '" + codigo + "' AND TIENDA = '" + tienda + "'"
			+ " AND ODOO_ID = '" + odooId + "'";
		ejecutarSQL(sql);
		log.warning("Marcado con error [" + errorMsg + "]: CODIGO=" + codigo
			+ " TIENDA=" + tienda + " ODOO_ID=" + odooId);
	}

	// =========================================================================
	// PROCESAMIENTO DE UN PAR TIENDA + ODOO_ID
	// =========================================================================

	/**
	 * Procesa todas las lineas de un par TIENDA+ODOO_ID, generando 1 o mas C_Order.
	 * Se cargan TODAS las filas en memoria antes de procesar, evitando inestabilidad
	 * del cursor Oracle al hacer DML sobre I_SALESIMPORT con el ResultSet abierto.
	 *
	 * Agrupacion: TIENDA + ODOO_ID + CODIGO (productos consolidados).
	 * Si supera 25 lineas, se corta la orden y se crea otra con el mismo ODOO_ID.
	 */
	private void processOneTiendaOdoo(String tienda, String odooId) throws Exception {
		// Resolver BPartnerLocation
		Map /*<Integer,Integer>*/ mapLocation = getBPartnerLocationID(tienda);
		if (mapLocation.isEmpty()) {
			log.warning("Tienda [" + tienda + "] sin BPartnerLocation, marcando con error.");
			addLog(0, null, null, "    WARN: Tienda [" + tienda + "] sin direccion, marcada con error.");
			String sqlErr = "UPDATE I_SALESIMPORT SET I_ISIMPORTED = 'E', Processing = 'Y', "
				+ "AD_PInstance_ID = NULL, "
				+ "I_ERRORMSG = 'Tienda sin BPartnerLocation: " + tienda.replaceAll("'", "") + "' "
				+ "WHERE TIENDA = '" + tienda + "' AND ODOO_ID = '" + odooId + "' AND I_ISIMPORTED = 'N'";
			DB.executeUpdate(sqlErr, get_TrxName());
			return;
		}

		Map.Entry /*<Integer,Integer>*/ bpLocEntry =
			(Map.Entry) mapLocation.entrySet().iterator().next();
		int bPartnerLocationID = ((Integer) bpLocEntry.getKey()).intValue();
		int billLocationID = ((Integer) bpLocEntry.getValue()).intValue();

		// =====================================================================
		// PASO 1: Cargar filas agrupadas por CODIGO en memoria y cerrar cursor
		// =====================================================================
		String sqlLines = "SELECT max(I_SALESIMPORT_ID) I_SALESIMPORT_ID, CODIGO, "
			+ "sum(CANTIDAD) CANTIDAD "
			+ "FROM I_SALESIMPORT "
			+ "WHERE I_ISIMPORTED = 'N' AND CODIGO IS NOT NULL "
			+ "AND Processing = 'Y' AND AD_PInstance_ID = " + getAD_PInstance_ID() + " "
			+ "AND TIENDA = '" + tienda + "' "
			+ "AND ODOO_ID = '" + odooId + "' "
			+ "GROUP BY CODIGO "
			+ "ORDER BY CODIGO";

		List /*<String[]>*/ filas = new ArrayList(); // cada elemento: {codigo, cantidad}
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = DB.prepareStatement(sqlLines, get_TrxName());
			rs = pst.executeQuery();
			while (rs.next()) {
				String[] fila = new String[2];
				fila[0] = rs.getString("CODIGO");
				fila[1] = rs.getString("CANTIDAD");
				filas.add(fila);
			}
		} finally {
			closeQuietly(rs);
			closeQuietly(pst);
		}

		if (filas.isEmpty()) {
			log.info("Par [" + tienda + "/" + odooId + "] sin lineas agrupadas pendientes.");
			markParReleased(tienda, odooId);
			return;
		}

		log.info("Par [" + tienda + "/" + odooId + "]: " + filas.size() + " codigos cargados en memoria.");

		// =====================================================================
		// PASO 2: Iterar sobre la lista en memoria (cursor ya cerrado)
		// =====================================================================
		int orderId = 0;
		int contador = 1;

		for (int i = 0; i < filas.size(); i++) {
			String[] fila = (String[]) filas.get(i);
			String codigo = fila[0];
			BigDecimal cant = new BigDecimal(fila[1]);

			// Resolver producto
			Map /*<Integer,Integer>*/ mapProducto = getProductID(codigo);
			if (mapProducto.isEmpty()) {
				log.warning("Producto [" + codigo + "] no encontrado, marcando con error.");
				markLineImportedError(codigo, tienda, odooId,
					"Producto no encontrado en M_Product: " + codigo);
				linesSkipped++;
				continue;
			}
			Map.Entry /*<Integer,Integer>*/ prodEntry =
				(Map.Entry) mapProducto.entrySet().iterator().next();
			int productId = ((Integer) prodEntry.getKey()).intValue();
			int uomId = ((Integer) prodEntry.getValue()).intValue();
			BigDecimal precio = getPrice(codigo, PRICELIST_ID);

			// Crear cabecera de orden si es necesario
			if (orderId == 0) {
				orderId = createOrderHeader(bPartnerLocationID, billLocationID, odooId);
				ordersCreated++;
				contador = 1;
			}

			// Procesar linea (reserva ecommerce -> reserva fisica -> disponible)
			int lineasInsertadas = processOneLine(orderId, productId, uomId, precio, cant,
				codigo, tienda, odooId, bPartnerLocationID, contador);

			if (lineasInsertadas > 0) {
				contador += lineasInsertadas;
				linesProcessed++;
			} else {
				linesSkipped++;
			}

			// Corte por 25 lineas: nueva OV con mismo ODOO_ID
			if (contador > 25) {
				completeOrder(orderId);
				ordersCompleted++;
				commitEx(); // Liberar locks de M_RequisitionLine inmediatamente
				orderId = 0;
				contador = 1;
			}
		}

		// Completar ultima orden pendiente
		if (orderId != 0) {
			completeOrder(orderId);
			ordersCompleted++;
			commitEx(); // Liberar locks de M_RequisitionLine inmediatamente
		}

		// Liberar Processing de lineas que no se importaron (por skip)
		String sqlRelease = "UPDATE I_SALESIMPORT SET Processing = 'N', AD_PInstance_ID = NULL "
			+ "WHERE TIENDA = '" + tienda + "' AND ODOO_ID = '" + odooId + "'"
			+ " AND Processing = 'Y' AND I_ISIMPORTED = 'N'";
		DB.executeUpdate(sqlRelease, get_TrxName());
	}

	// =========================================================================
	// CREACION DE CABECERA DE ORDEN
	// =========================================================================

	private int createOrderHeader(int bPartnerLocationID, int billLocationID, String odooId) {
		int orderId = Integer.parseInt(DB.getSQLValueString(null,
			"Select NEXTIDFUNC(232,'N') from c_charge where c_charge_ID=1000010"));
		String documentNo = DB.getSQLValueString(null,
			"Select NEXTIDFUNC(1000096,'N') from c_charge where c_charge_ID=1000010");

		String insertOrder = "INSERT INTO C_Order "
			+ "(C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,C_DocTypeTarget_ID,IsSOTrx,DeliveryRule,"
			+ "C_BPartner_ID,C_BPartner_Location_ID,AD_User_ID,Bill_BPartner_ID,Bill_Location_ID,"
			+ "C_PaymentTerm_ID,M_PriceList_ID,M_Warehouse_ID,SalesRep_ID,DateOrdered,DateAcct,InvoiceRule,"
			+ "VENTAINVIERNO,DocumentNo,DOCSTATUS,DOCACTION,C_DOCTYPE_ID,C_CURRENCY_ID,"
			+ "PAYMENTRULE,FREIGHTCOSTRULE,DELIVERYVIARULE,PRIORITYRULE,DatePromised,MedioCompra,ODOO_ID,POReference) "
			+ "VALUES ("
			+ orderId + "," + m_AD_Client_ID + "," + m_AD_Org_ID + ","
			+ m_AD_User_ID + "," + m_AD_User_ID + "," + DOCTYPE_ID + ",'Y','O',"
			+ BPARTNER_ID + "," + bPartnerLocationID + "," + m_AD_User_ID + ","
			+ BPARTNER_ID + "," + billLocationID + ","
			+ PAYMENT_TERM_ID + "," + PRICELIST_ID + "," + WAREHOUSE_ID + ",1000032,"
			+ "sysdate,sysdate,'D',"
			+ "'N','" + documentNo + "','DR','CO'," + DOCTYPE_ID + ",228,"
			+ "'P','I','P','5',sysdate,'Internet','" + odooId + "','" + odooId + "')";
		ejecutarSQL(insertOrder);

		log.info("Orden creada ID=" + orderId + " DocNo=" + documentNo);
		return orderId;
	}

	// =========================================================================
	// PROCESAMIENTO DE UNA LINEA (RESERVAS + STOCK)
	// =========================================================================

	/**
	 * Procesa una linea buscando stock en: Reserva Ecommerce -> Reserva Fisica -> Disponible.
	 * Cada fuente genera su propia C_OrderLine con su M_RequisitionLine_ID.
	 * Sigue la misma logica que ImportOrderB2CRFAutOdoo.
	 *
	 * @return cantidad de C_OrderLine insertadas (siempre >= 1 por la linea noStock)
	 */
	private int processOneLine(int orderId, int productId, int uomId,
			BigDecimal precio, BigDecimal cant, String codigo, String tienda,
			String odooId, int bPartnerLocationID, int contador) {

		// Guardar cantidad original para usarla en Demand si no hay stock
		BigDecimal cantOriginal = cant;
		BigDecimal cantPendiente = cant;
		int lineasInsertadas = 0;

		// =====================================================================
		// PASO 1: Buscar en Reserva ECommerce (c_doctype_id=1000569/1000570)
		// =====================================================================
		int[] resEcom = getReservaEcommerce(productId);
		int reqLineIdEcom = resEcom[0];
		int qtyResEcom = resEcom[1];

		if (reqLineIdEcom > 0 && cantPendiente.intValue() > 0) {
			BigDecimal qtyUsar;
			if (cantPendiente.intValue() <= qtyResEcom) {
				qtyUsar = cantPendiente;
			} else {
				qtyUsar = new BigDecimal(qtyResEcom);
			}
			insertOrderLine(orderId, productId, uomId, precio, qtyUsar,
				contador + lineasInsertadas, reqLineIdEcom);
			lineasInsertadas++;
			cantPendiente = cantPendiente.subtract(qtyUsar);
			log.info("Producto " + productId + ": Reserva Ecommerce aplicada qty=" + qtyUsar
				+ " ReqLine=" + reqLineIdEcom + " Pendiente=" + cantPendiente);

			if (cantPendiente.compareTo(BigDecimal.ZERO) <= 0) {
				markLineImported(codigo, tienda, odooId, orderId);
				return lineasInsertadas;
			}
		}

		// =====================================================================
		// PASO 2: Buscar en Reserva Fisica (c_doctype_id=1000111)
		// =====================================================================
		int[] resFisica = getReservaFisica(productId, bPartnerLocationID);
		int reqLineIdFis = resFisica[0];
		int qtyResFis = resFisica[1];

		if (reqLineIdFis > 0 && cantPendiente.intValue() > 0) {
			BigDecimal qtyUsar;
			if (cantPendiente.intValue() <= qtyResFis) {
				qtyUsar = cantPendiente;
			} else {
				qtyUsar = new BigDecimal(qtyResFis);
			}
			insertOrderLine(orderId, productId, uomId, precio, qtyUsar,
				contador + lineasInsertadas, reqLineIdFis);
			lineasInsertadas++;
			cantPendiente = cantPendiente.subtract(qtyUsar);
			log.info("Producto " + productId + ": Reserva Fisica aplicada qty=" + qtyUsar
				+ " ReqLine=" + reqLineIdFis + " Pendiente=" + cantPendiente);

			if (cantPendiente.compareTo(BigDecimal.ZERO) <= 0) {
				markLineImported(codigo, tienda, odooId, orderId);
				return lineasInsertadas;
			}
		}

		// =====================================================================
		// PASO 3: Stock disponible (sin reserva)
		// =====================================================================
		BigDecimal stock = getStockLA(productId);
		if (stock.compareTo(cantPendiente) >= 0) {
			// Stock cubre el pendiente
			insertOrderLine(orderId, productId, uomId, precio, cantPendiente,
				contador + lineasInsertadas, 0);
			lineasInsertadas++;
			log.info("Producto " + productId + ": Stock disponible aplicado qty=" + cantPendiente
				+ " StockDisponible=" + stock);
			markLineImported(codigo, tienda, odooId, orderId);
			return lineasInsertadas;
		} else {
			// No hay stock suficiente para cubrir el pendiente
			// Revertir TODAS las lineas parciales ya insertadas para este producto
			log.info("Producto " + productId + " sin stock suficiente. Disponible=" + stock
				+ " Pedido=" + cantPendiente + " (original=" + cantOriginal + ")."
				+ " Revirtiendo " + lineasInsertadas + " lineas parciales.");
			if (lineasInsertadas > 0) {
				revertOrderLinesForProduct(orderId, productId);
			}
			// Insertar linea con Qty=0, Demand=cantOriginal, NotPrint='Y'
			insertOrderLineNoStock(orderId, productId, uomId, precio, cantOriginal, contador);
			markLineImported(codigo, tienda, odooId, orderId);
			return 1;
		}
	}

	/**
	 * Obtiene reserva ecommerce para un producto.
	 * @return int[] {M_RequisitionLine_ID, qtyreserved} o {0,0} si no hay
	 */
	private int[] getReservaEcommerce(int productId) {
		int[] result = new int[]{0, 0};
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT SUM(rl.qtyreserved) qtyreserved, "
				+ "MAX(rl.m_requisitionline_ID) M_RequisitionLine_ID "
				+ "FROM m_Requisitionline rl "
				+ "JOIN m_requisition r ON rl.m_Requisition_ID = r.m_requisition_ID "
				+ "WHERE r.docstatus = 'CO' "
				+ "AND r.c_doctype_id = 1000569 "
				+ "AND rl.liberada = 'N' "
				+ "AND rl.m_product_ID = " + productId
				+ " AND rl.qtyreserved > 0 "
				+ "AND EXISTS (SELECT * FROM m_requisition r2 "
				+ "  WHERE R2.M_REQUISITIONREF_ID = r.m_requisition_ID "
				+ "  AND r2.c_doctype_ID = 1000570 "
				+ "  AND r2.docstatus = 'CO' "
				+ "  AND r2.c_bpartner_ID = " + BPARTNER_ID + ")";
			pst = DB.prepareStatement(sql, get_TrxName());
			rs = pst.executeQuery();
			if (rs.next()) {
				result[0] = rs.getInt("M_RequisitionLine_ID");
				result[1] = rs.getInt("qtyreserved");
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error reserva ecommerce: " + e.getMessage(), e);
		} finally {
			closeQuietly(rs);
			closeQuietly(pst);
		}
		return result;
	}

	/**
	 * Obtiene reserva fisica para un producto y ubicacion.
	 * @return int[] {M_RequisitionLine_ID, qtyreserved} o {0,0} si no hay
	 */
	private int[] getReservaFisica(int productId, int bPartnerLocationID) {
		int[] result = new int[]{0, 0};
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT SUM(rl.qtyreserved) qtyreserved, "
				+ "MAX(rl.m_requisitionline_ID) m_requisitionline_ID "
				+ "FROM m_Requisitionline rl "
				+ "JOIN m_requisition r ON (rl.m_Requisition_ID = r.m_requisition_ID) "
				+ "WHERE r.docstatus = 'CO' "
				+ "AND r.c_doctype_id = 1000111 "
				+ "AND rl.m_product_ID = " + productId
				+ " AND r.c_bpartner_ID = " + BPARTNER_ID
				+ " AND (r.c_bpartner_location_ID = " + bPartnerLocationID
				+ " OR R.OVERWRITEREQUISITION = 'Y') "
				+ "AND rl.qtyreserved > 0 "
				+ "AND rl.LIBERADA = 'N'";
			pst = DB.prepareStatement(sql, get_TrxName());
			rs = pst.executeQuery();
			if (rs.next()) {
				result[0] = rs.getInt("m_requisitionline_ID");
				result[1] = rs.getInt("qtyreserved");
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error reserva fisica: " + e.getMessage(), e);
		} finally {
			closeQuietly(rs);
			closeQuietly(pst);
		}
		return result;
	}

	// =========================================================================
	// INSERT / COMPLETE DE ORDENES Y LINEAS
	// =========================================================================

	/**
	 * Inserta una linea de C_OrderLine.
	 * @param reqLineId M_RequisitionLine_ID, 0 si no aplica
	 */
	private void insertOrderLine(int orderId, int productId, int uomId,
			BigDecimal precio, BigDecimal qty, int lineNo, int reqLineId) {

		int orderLineId = Integer.parseInt(DB.getSQLValueString(null,
			"Select NEXTIDFUNC(233,'N') from c_charge where c_charge_ID=1000010"));

		BigDecimal lineNetAmt = precio.multiply(qty).setScale(0, RoundingMode.HALF_UP);

		String reqCol = "";
		String reqVal = "";
		if (reqLineId > 0) {
			reqCol = ", m_requisitionline_id";
			reqVal = ", " + reqLineId;
		}

		String sql = "INSERT INTO C_OrderLine "
			+ "(C_OrderLine_ID,C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,"
			+ "M_Product_ID,PriceEntered,PriceActual,PriceList,"
			+ "QtyEntered,Line,Qty,Demand,"
			+ "Discount,Discount2,Discount3,Discount4,Discount5,"
			+ "NotPrint,LineNetAmt,DATEORDERED,M_WAREHOUSE_ID,C_UOM_ID,"
			+ "C_CURRENCY_ID,C_TAX_ID,DatePromised" + reqCol + ") "
			+ "VALUES ("
			+ orderLineId + "," + orderId + "," + m_AD_Client_ID + "," + m_AD_Org_ID + ","
			+ m_AD_User_ID + "," + m_AD_User_ID + ","
			+ productId + "," + precio + "," + precio + "," + precio + ","
			+ qty + "," + (lineNo * 10) + "," + qty + "," + qty + ","
			+ "0,0,0,0,0,'N',"
			+ lineNetAmt + ",sysdate," + WAREHOUSE_ID + "," + uomId + ","
			+ "228,1000000,sysdate" + reqVal + ")";
		ejecutarSQL(sql);
	}

	/**
	 * Inserta una linea de C_OrderLine con Qty=0, Demand=cantOriginal, NotPrint='Y'.
	 * Se usa cuando el producto no tiene stock suficiente para que la linea quede
	 * registrada en la OV sin afectar el despacho.
	 */
	private void insertOrderLineNoStock(int orderId, int productId, int uomId,
			BigDecimal precio, BigDecimal cantOriginal, int lineNo) {

		int orderLineId = Integer.parseInt(DB.getSQLValueString(null,
			"Select NEXTIDFUNC(233,'N') from c_charge where c_charge_ID=1000010"));

		String sql = "INSERT INTO C_OrderLine "
			+ "(C_OrderLine_ID,C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,"
			+ "M_Product_ID,PriceEntered,PriceActual,PriceList,"
			+ "QtyEntered,Line,Qty,Demand,"
			+ "Discount,Discount2,Discount3,Discount4,Discount5,"
			+ "NotPrint,LineNetAmt,DATEORDERED,M_WAREHOUSE_ID,C_UOM_ID,"
			+ "C_CURRENCY_ID,C_TAX_ID,DatePromised) "
			+ "VALUES ("
			+ orderLineId + "," + orderId + "," + m_AD_Client_ID + "," + m_AD_Org_ID + ","
			+ m_AD_User_ID + "," + m_AD_User_ID + ","
			+ productId + "," + precio + "," + precio + "," + precio + ","
			+ "0," + (lineNo * 10) + ",0," + cantOriginal + ","
			+ "0,0,0,0,0,'Y',"
			+ "0,sysdate," + WAREHOUSE_ID + "," + uomId + ","
			+ "228,1000000,sysdate)";
		ejecutarSQL(sql);
		log.info("Linea sin stock insertada: Producto=" + productId
			+ " Qty=0 Demand=" + cantOriginal + " NotPrint=Y OrderLine=" + orderLineId);
	}

	/**
	 * Completa una orden: setea DocAction y ejecuta workflow.
	 * Si el processIt falla (ej: validador ModWindsorValidAvaiQtyOrder exige reserva ecommerce),
	 * convierte TODAS las lineas a noStock (Qty=0, Demand=cantOriginal, NotPrint='Y')
	 * y reintenta una sola vez.
	 */
	/**
	 * Completa una orden. FIRMA2='Y' ya va en el INSERT de createOrderHeader.
	 * Si processIt falla, detecta las lineas problematicas una por una:
	 *   - Convierte la linea a noStock (Qty=0, Demand=cantOriginal, NotPrint='Y')
	 *   - Reintenta completar
	 *   - Repite hasta que complete o no queden lineas con Qty>0
	 */
	private void completeOrder(int orderId) {
		if (orderId == 0) return;
		try {
			MOrder order = new MOrder(getCtx(), orderId, get_TrxName());
			if ("CO".equals(order.getDocStatus())) return;

			order.setDocAction("CO");
			if (order.processIt("CO")) {
				order.save();
				//DB.executeUpdateEx("UPDATE C_Order SET FIRMA2='Y' WHERE C_Order_ID=" + orderId, get_TrxName());
				log.info("Orden completada: " + order.getDocumentNo());
				return;
			}

			// === Fallo el primer intento: ir linea por linea ===
			String docNo = order.getDocumentNo();
			log.warning("Error al completar orden " + docNo + ". Buscando lineas problematicas...");

			List /*<int[]>*/ lineas = getOrderLinesWithQty(orderId);
			if (lineas.isEmpty()) {
				log.warning("Orden " + docNo + " sin lineas con qty > 0.");
				addLog(0, null, null, "    WARN: Orden " + docNo + " sin lineas activas.");
				return;
			}

			int intentos = 0;
			int maxIntentos = lineas.size();

			while (intentos < maxIntentos) {
				intentos++;

				int[] lineaProblema = (int[]) lineas.get(0);
				int orderLineId = lineaProblema[0];
				int productId = lineaProblema[1];

				log.info("Intento " + intentos + "/" + maxIntentos
					+ ": Convirtiendo linea " + orderLineId + " (Producto " + productId + ") a noStock");
				addLog(0, null, null, "    Orden " + docNo + ": linea producto " + productId
					+ " convertida a noStock (intento " + intentos + ")");

				String sqlFix = "UPDATE C_OrderLine SET "
					+ "Demand = QtyEntered, "
					+ "QtyEntered = 0, Qty = 0, LineNetAmt = 0, "
					+ "NotPrint = 'Y', M_RequisitionLine_ID = NULL "
					+ "WHERE C_OrderLine_ID = " + orderLineId;
				DB.executeUpdate(sqlFix, get_TrxName());

				MOrder orderRetry = new MOrder(getCtx(), orderId, get_TrxName());
				orderRetry.setDocAction("CO");
				if (orderRetry.processIt("CO")) {
					orderRetry.save();
				//	DB.executeUpdateEx("UPDATE C_Order SET FIRMA2='Y' WHERE C_Order_ID=" + orderId, get_TrxName());
					log.info("Orden completada (intento " + intentos + "): " + docNo);
					return;
				}

				lineas = getOrderLinesWithQty(orderId);
				if (lineas.isEmpty()) {
					MOrder orderFinal = new MOrder(getCtx(), orderId, get_TrxName());
					orderFinal.setDocAction("CO");
					if (orderFinal.processIt("CO")) {
						orderFinal.save();
					//	DB.executeUpdateEx("UPDATE C_Order SET FIRMA2='Y' WHERE C_Order_ID=" + orderId, get_TrxName());
						log.info("Orden completada (todas noStock): " + docNo);
					} else {
						log.warning("Orden " + docNo + " no se pudo completar ni con todas las lineas en 0.");
						addLog(0, null, null, "    WARN: Orden " + docNo + " imposible de completar.");
					}
					return;
				}
			}

			log.warning("Orden " + docNo + ": agotados " + maxIntentos + " intentos. Dejando en DR.");
			addLog(0, null, null, "    WARN: Orden " + docNo + " no completada tras " + maxIntentos + " intentos.");

		} catch (Exception e) {
			log.log(Level.SEVERE, "Error completando orden ID=" + orderId + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Obtiene las lineas de una orden que tienen QtyEntered > 0.
	 * @return List de int[] {C_OrderLine_ID, M_Product_ID}
	 */
	private List /*<int[]>*/ getOrderLinesWithQty(int orderId) {
		List /*<int[]>*/ result = new ArrayList();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT C_OrderLine_ID, M_Product_ID FROM C_OrderLine "
				+ "WHERE C_Order_ID = " + orderId + " AND QtyEntered > 0 ORDER BY Line";
			pst = DB.prepareStatement(sql, get_TrxName());
			rs = pst.executeQuery();
			while (rs.next()) {
				result.add(new int[]{rs.getInt(1), rs.getInt(2)});
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error obteniendo lineas con qty: " + e.getMessage(), e);
		} finally {
			closeQuietly(rs);
			closeQuietly(pst);
		}
		return result;
	}

	/**
	 * Elimina lineas de una orden para un producto especifico (reversion por falta de stock).
	 */
	private void revertOrderLinesForProduct(int orderId, int productId) {
		String sql = "DELETE FROM C_OrderLine WHERE C_Order_ID = " + orderId
			+ " AND M_Product_ID = " + productId;
		ejecutarSQL(sql);
	}

	// =========================================================================
	// METODOS AUXILIARES EXISTENTES (optimizados)
	// =========================================================================

	private Map /*<Integer, Integer>*/ getBPartnerLocationID(String local) {
		Map /*<Integer, Integer>*/ par = new HashMap();
		PreparedStatement pst = null;
		ResultSet res = null;
		try {
			String sql = "SELECT C_BPartner_Location_ID, C_Location_ID"
				+ " FROM C_BPartner_Location"
				+ " WHERE Tienda = ?";
			pst = DB.prepareStatement(sql, get_TrxName());
			pst.setString(1, local);
			res = pst.executeQuery();
			if (res.next()) {
				par.put(new Integer(res.getInt("C_BPartner_Location_ID")),
						new Integer(res.getInt("C_Location_ID")));
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Error al buscar direccion de local [" + local + "]: " + e.getMessage(), e);
		} finally {
			closeQuietly(res);
			closeQuietly(pst);
		}
		return par;
	}

	private Map /*<Integer, Integer>*/ getProductID(String value) {
		Map /*<Integer, Integer>*/ par = new HashMap();
		PreparedStatement pst = null;
		ResultSet res = null;
		try {
			String sql = "SELECT M_Product_ID, C_UOM_ID"
				+ " FROM M_Product"
				+ " WHERE Value = ?";
			pst = DB.prepareStatement(sql, get_TrxName());
			pst.setString(1, value);
			res = pst.executeQuery();
			if (res.next()) {
				par.put(new Integer(res.getInt("M_Product_ID")),
						new Integer(res.getInt("C_UOM_ID")));
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Error al buscar producto [" + value + "]: " + e.getMessage(), e);
		} finally {
			closeQuietly(res);
			closeQuietly(pst);
		}
		return par;
	}

	private BigDecimal getPrice(String value, int priceListID) {
		BigDecimal price = BigDecimal.ZERO;
		PreparedStatement pst = null;
		ResultSet res = null;
		try {
			String sql = "SELECT PriceSTD"
				+ " FROM M_ProductPrice"
				+ " WHERE M_PriceList_Version_ID = ("
				+ "   SELECT M_PriceList_Version_ID FROM M_PriceList_Version "
				+ "   WHERE IsActive = 'Y' AND M_PriceList_ID = ?)"
				+ " AND M_Product_ID = (SELECT M_Product_ID FROM M_Product WHERE Value = ?)";
			pst = DB.prepareStatement(sql, get_TrxName());
			pst.setInt(1, priceListID);
			pst.setString(2, value);
			res = pst.executeQuery();
			if (res.next()) {
				price = res.getBigDecimal("PriceSTD");
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Error al buscar precio de producto [" + value + "]: " + e.getMessage(), e);
		} finally {
			closeQuietly(res);
			closeQuietly(pst);
		}
		return price;
	}

	private BigDecimal getStockLA(int M_Product_ID) {
		BigDecimal disponible = BigDecimal.ZERO;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT qtyavailableofb(p.m_product_ID,1000010) "
				+ "+ qtyavailableofb(p.m_product_ID,1000001) as disponible"
				+ " FROM M_product p"
				+ " WHERE p.m_product_ID = " + M_Product_ID;
			pst = DB.prepareStatement(sql, get_TrxName());
			rs = pst.executeQuery();
			if (rs.next()) {
				disponible = rs.getBigDecimal("disponible");
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Error al buscar stock de producto " + M_Product_ID + ": " + e.getMessage(), e);
		} finally {
			closeQuietly(rs);
			closeQuietly(pst);
		}
		return disponible;
	}

	private void actualizarQtyUsed() {
		StringBuffer sql = new StringBuffer("MERGE INTO m_requisitionline rl"
			+ " USING ("
			+ "    SELECT rl.M_REQUISITIONLINE_ID,"
			+ "           (rl.qty - SUM(ol.QTYENTERED)) AS new_qtyreserved,"
			+ "           SUM(ol.QTYENTERED) AS new_qtyused"
			+ "    FROM m_requisitionline rl"
			+ "    JOIN c_orderline ol ON ol.M_REQUISITIONLINE_ID = rl.M_REQUISITIONLINE_ID"
			+ "    JOIN c_order o ON o.C_ORDER_ID = ol.C_ORDER_ID AND o.DOCSTATUS IN ('CO', 'DR', 'IN')"
			+ "    JOIN m_product p ON p.M_PRODUCT_ID = ol.M_PRODUCT_ID"
			+ "    WHERE rl.m_requisition_id = 1001932"
			+ "    GROUP BY rl.M_REQUISITIONLINE_ID, rl.QTY, rl.QTYRESERVED, rl.QTYUSED"
			+ "    HAVING rl.QTYUSED <> SUM(ol.QTYENTERED)"
			+ "       AND rl.QTYRESERVED > (SUM(ol.QTYENTERED) - rl.QTYUSED)"
			+ " ) data"
			+ " ON (rl.M_REQUISITIONLINE_ID = data.M_REQUISITIONLINE_ID)"
			+ " WHEN MATCHED THEN UPDATE"
			+ " SET rl.QTYUSED = data.new_qtyused,"
			+ "    rl.QTYRESERVED = data.new_qtyreserved");
		DB.executeUpdate(sql.toString(), get_TrxName());
	}

	// =========================================================================
	// UTILIDADES
	// =========================================================================

	/**
	 * Verifica si otro proceso esta REALMENTE en ejecucion.
	 * Solo considera activas las instancias actualizadas en los ultimos 30 minutos.
	 * Si llevan mas tiempo con IsProcessing='Y', son fantasmas y se ignoran.
	 */
	private boolean isOtherProcessRunning(int adProcessId) {
		int count = DB.getSQLValue(get_TrxName(),
			"SELECT COUNT(*) FROM AD_PInstance "
			+ "WHERE IsProcessing = 'Y' "
			+ "AND AD_Process_ID = " + adProcessId
			+ " AND Updated > (SYSDATE - 30/1440)");
		return count > 0;
	}

	/**
	 * Ejecuta SQL usando la transaccion del proceso.
	 */
	private void ejecutarSQL(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return;
		try {
			DB.executeUpdate(sql, get_TrxName());
			log.fine("SQL OK");
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error ejecutando SQL: " + e.getMessage() + " | SQL=" + sql, e);
			throw new RuntimeException(e);
		}
	}

	private void closeQuietly(ResultSet rs) {
		if (rs != null) {
			try { rs.close(); } catch (Exception e) { /* ignorar */ }
		}
	}

	private void closeQuietly(PreparedStatement ps) {
		if (ps != null) {
			try { ps.close(); } catch (Exception e) { /* ignorar */ }
		}
	}

}