
package org.windsor.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

//import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;

/**
 * @author Isaac Castro
 * @version Optimizada 3.0 por FGonzalez
 */
public class ImportOrderB2CRFAutOdoo extends SvrProcess {

	/** Client to be imported to */
	private int m_AD_Client_ID = 1000000;
	/** Organization to be imported to */
	private int m_AD_Org_ID = 1000000;

	private StringBuffer sql = null;
	private int no = 0;
	private String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;

	// variables de correo
	final String miCorreo = "soporte@comercialwindsor.cl";
	final String miContrasena = "Cw9121100";
	final String servidorSMTP = "smtp.gmail.com";
	final String puertoEnvio = "465";
	String mailReceptor = null;
	String asunto = null;
	String cuerpo = null;

	String telefono = null;
	String email = null;
	Integer direccion = null;
	private int docok = 0;
	private int docnotok = 0;
	/** Effective */
	private Timestamp m_DateValue = null;

	//private Integer m_M_WareHouse_ID = 1000001; // Lampa
	//private Integer m_M_WareHouse_ab_ID = 1000010; // Abastecimiento

	/** Tamano del lote de documentos a procesar */
	private static final int BATCH_SIZE = 20;

	/** Flag para detectar conexion perdida (SQLRecoverableException) y abortar */
	private boolean m_connectionLost = false;

	/*class ConexioDBMuro {
		Connection conn;
		public ConexioDBMuro() {
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				String connectionUrl = "jdbc:oracle:thin:@db2358.prolinux.cl:1524:muroerp";
				conn = DriverManager.getConnection(connectionUrl, "adempierem", "adempiere");
				log.info("Conectado a Muro ERP.");
			} catch (SQLException ex) {
				log.log(Level.SEVERE, "Error en Conexion Muro ERP. " + ex.toString(), ex);
			} catch (ClassNotFoundException e) {
				log.log(Level.SEVERE, "Error al cargar Driver. " + e.toString(), e);
			}
		}
	}*/

	/**
	 * Prepare - e.g., get Parameters.
	 */
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = 1000000;
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = 1000000;
			else if (name.equals("DeleteOldImported"))
				;
			else if (name.equals("DocAction"))
				;
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (m_DateValue == null)
			m_DateValue = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * Perform process.
	 * @return Message
	 * @throws Exception
	 */
	protected String doIt() throws java.lang.Exception {

		log.info("=== INICIO ImportOrderB2CRFAutOdoo === PInstance=" + getAD_PInstance_ID());

		// --- Verificar si ProcesarOVMuroOdoo esta en ejecucion ---
		// Ambos procesos compiten por C_Order y M_RequisitionLine, causando bloqueos
		if (isOtherProcessRunning(1000779)) {
			String msg = "ProcesarOVMuroOdoo (1000779) en ejecucion. Se pospone para evitar bloqueos.";
			log.info(msg);
			addLog(0, null, null, msg);
			return msg;
		}

		// --- Limpieza inicial de staging NO convertido ---
		limpiarStagingNoConvertido(get_TrxName());
		commitEx();

		// --- Paso previo: Liberar registros huerfanos de instancias que ya terminaron ---
		liberarProcessingHuerfano();
		commitEx();

		// --- Preparacion: actualizar datos de referencia ---
		log.info("Reset=" + actualizaClient());
		log.fine("Set BP from Value=" + actualizaBP());
		log.fine("Set Product from Value=" + actualizaProduct());
		log.fine("Set SalesRep_ID=" + actualizaSalesRep());
		commitEx();

		// --- Contar total de documentos pendientes ---
		int totalDocs = DB.getSQLValue(get_TrxName(),
			"SELECT COUNT(*) FROM ("
			+ "SELECT 1 FROM I_OrderB2CAut_Odoo "
			+ "WHERE Processed='N' AND NVL(Processing,'N')='N' "
			+ "GROUP BY DocumentNo, BPartnerValue)");
		if (totalDocs < 0) totalDocs = 0;

		addLog(0, null, null, "=== Total documentos pendientes: " + totalDocs + " ===");
		log.info("Total documentos pendientes: " + totalDocs);

		int processedCount = 0;
		int batchNumber = 0;
		List /*<MOrder>*/ listaOrderGlobal = new ArrayList();
		StringBuffer mensajeCorreoGlobal = new StringBuffer();

		// =====================================================================
		// LOOP PRINCIPAL POR LOTES
		// =====================================================================
		while (true) {
			batchNumber++;

			// --- Verificar conexion antes de continuar ---
			if (m_connectionLost) {
				addLog(0, null, null, "*** ABORTANDO: conexion a BD perdida. Pendientes no procesados.");
				log.severe("Conexion perdida detectada. Abortando loop de lotes.");
				break;
			}

			// --- PASO 1: Reclamar un lote de documentos con Processing ---
			int claimed = claimBatch(BATCH_SIZE);
			if (claimed == 0) {
				log.info("No hay mas documentos pendientes. Fin.");
				break;
			}

			addLog(0, null, null, "--- Lote #" + batchNumber + ": " + claimed
				+ " documentos reclamados (acumulado " + processedCount + "/" + totalDocs + ") ---");
			log.info("Lote #" + batchNumber + ": " + claimed + " docs reclamados");

			// --- PASO 2: Obtener los DocumentNo del lote reclamado por ESTA instancia ---
			ArrayList /*<String[]>*/ docsEnLote = new ArrayList(); // cada elemento es {DocumentNo, BPartnerValue}
			PreparedStatement pstmtBatch = null;
			ResultSet rsBatch = null;
			try {
				String sqlBatch = "SELECT DocumentNo, BPartnerValue "
					+ "FROM I_OrderB2CAut_Odoo "
					+ "WHERE Processed='N' AND Processing='Y' "
					+ "AND AD_PInstance_ID = " + getAD_PInstance_ID() + " "
					+ "GROUP BY DocumentNo, BPartnerValue "
					+ "ORDER BY DocumentNo";
				pstmtBatch = DB.prepareStatement(sqlBatch, get_TrxName());
				rsBatch = pstmtBatch.executeQuery();
				while (rsBatch.next()) {
					docsEnLote.add(new String[]{rsBatch.getString("DocumentNo"), rsBatch.getString("BPartnerValue")});
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error obteniendo lote: " + e.getMessage(), e);
			} finally {
				closeQuietly(rsBatch);
				closeQuietly(pstmtBatch);
			}

			// --- PASO 3: Procesar cada documento del lote ---
			for (int d = 0; d < docsEnLote.size(); d++) {
				// Abortar si se perdio la conexion
				if (m_connectionLost) {
					log.severe("Conexion perdida. Abortando lote actual.");
					break;
				}
				String[] docInfo = (String[]) docsEnLote.get(d);
				String docNo = docInfo[0];
				String bpValue = docInfo[1];
				processedCount++;

				addLog(0, null, null, "  Procesando " + processedCount + "/" + totalDocs
					+ " Doc=" + docNo + " BP=" + bpValue);

				try {
					// Verificar si ya fue importado previamente
					if (isAlreadyImported(docNo, bpValue)) {
						log.fine("Doc=" + docNo + " ya importado previamente, marcando.");
						beforeImport(docNo, bpValue);
						markDocumentProcessed(docNo);
					} else {
						// Procesar el documento completo
						boolean ok = processOneDocument(docNo, bpValue, listaOrderGlobal, mensajeCorreoGlobal);
						if (ok) {
							docok++;
						} else {
							docnotok++;
						}
						// Marcar las lineas de I_OrderB2CAut_Odoo como procesadas
						markDocumentProcessed(docNo);
					}
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error procesando Doc=" + docNo + ": " + e.getMessage(), e);
					docnotok++;
					// Detectar conexion perdida para abortar inmediatamente
					if (isSQLRecoverable(e)) {
						m_connectionLost = true;
						log.severe("*** Conexion a BD perdida en Doc=" + docNo + ". Abortando.");
						break;
					}
					// Marcar como procesado para no reintentar indefinidamente
					markDocumentProcessed(docNo);
				}
				// Commit despues de cada documento para liberar locks de M_RequisitionLine
				commitEx();
			}

			// --- PASO 4: Commit del lote completo ---
			commitEx();
			addLog(0, null, null, "--- Lote #" + batchNumber + " completado. OK=" + docok + " Error=" + docnotok + " ---");
			log.info("Lote #" + batchNumber + " completado. OK=" + docok + " Error=" + docnotok);

		} // while lotes

		addLog(0, null, null, "=== PROCESO TERMINADO. Creadas=" + docok + " Con Error=" + docnotok + " ===");
		return "Proceso terminado - Ordenes Creadas:" + docok + " - Ordenes con Error: " + docnotok;
	}

	// =========================================================================
	// METODOS DE CONTROL DE LOTES
	// =========================================================================

	/**
	 * Reclama un lote de documentos marcandolos con Processing='Y'.
	 * Usa UPDATE con ROWNUM para garantizar atomicidad.
	 * Solo reclama documentos que NO esten siendo procesados por otra instancia.
	 *
	 * @param batchSize cantidad maxima de documentos a reclamar
	 * @return cantidad de filas actualizadas (lineas, no documentos unicos)
	 */
	private int claimBatch(int batchSize) {
		// Primero obtener los DocumentNo unicos del lote
		// Usamos una subquery para limitar a N documentos distintos
		String sqlClaim =
			"UPDATE I_OrderB2CAut_Odoo SET Processing = 'Y', "
			+ "AD_PInstance_ID = " + getAD_PInstance_ID() + " "
			+ "WHERE Processed = 'N' AND NVL(Processing,'N') = 'N' "
			+ "AND DocumentNo IN ("
			+ "  SELECT DocumentNo FROM ("
			+ "    SELECT DISTINCT DocumentNo FROM I_OrderB2CAut_Odoo "
			+ "    WHERE Processed = 'N' AND NVL(Processing,'N') = 'N' "
			+ "    ORDER BY DocumentNo"
			+ "  ) WHERE ROWNUM <= " + batchSize
			+ ")";
		int rows = DB.executeUpdate(sqlClaim, get_TrxName());
		if (rows < 0) rows = 0;
		log.info("claimBatch: " + rows + " filas marcadas con Processing='Y' PInstance=" + getAD_PInstance_ID());
		return rows;
	}

	/**
	 * Libera registros que quedaron con Processing='Y' de ejecuciones anteriores
	 * cuya AD_PInstance ya NO esta activa (IsProcessing='Y').
	 * NO toca registros de instancias que aun estan corriendo.
	 */
	private void liberarProcessingHuerfano() {
		String sql = "UPDATE I_OrderB2CAut_Odoo SET Processing = 'N', AD_PInstance_ID = NULL "
			+ "WHERE Processing = 'Y' AND Processed = 'N' "
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
	 * Marca un documento especifico como Processed='Y' y libera Processing y AD_PInstance_ID.
	 */
	private void markDocumentProcessed(String documentNo) {
		String sqlMark = "UPDATE I_OrderB2CAut_Odoo "
			+ "SET Processed = 'Y', Processing = 'N', AD_PInstance_ID = NULL "
			+ "WHERE DocumentNo = '" + documentNo + "'";
		DB.executeUpdate(sqlMark, get_TrxName());
	}

	/**
	 * Verifica si un documento ya fue importado previamente
	 * (existe en C_ORDERB2CAUT_Odoo con el mismo POReference y BPartnerValue).
	 */
	private boolean isAlreadyImported(String documentNo, String bpValue) {
		String sqlCheck = "SELECT COUNT(*) FROM I_OrderB2CAut_Odoo o "
			+ "WHERE o.Processed = 'N' "
			+ "AND o.DocumentNo = '" + documentNo + "' "
			+ "AND o.DocumentNo IN ("
			+ "  SELECT b2.POReference FROM C_ORDERB2CAUT_Odoo b2 "
			+ "  WHERE o.DocumentNo = b2.POReference "
			+ "  AND b2.BPartnerValue = o.BPartnerValue)";
		int count = DB.getSQLValue(get_TrxName(), sqlCheck);
		return count > 0;
	}

	// =========================================================================
	// PROCESAMIENTO DE UN DOCUMENTO INDIVIDUAL
	// =========================================================================

	/**
	 * Procesa un documento completo (cabecera + lineas + creacion de C_Order).
	 *
	 * @return true si se creo la orden exitosamente, false si hubo error controlado
	 */
	private boolean processOneDocument(String documentNo, String bpValueParam,
			List /*<MOrder>*/ listaOrder, StringBuffer mensajeCorreo) {

		String menj1 = null, menj2 = null, menj3 = null, menj4 = null, menj6 = null;

		// Query agrupada por ProductValue para evitar lineas duplicadas en la misma orden
		// SUM(QtyOrdered) consolida cantidades del mismo producto
		String recorre = "SELECT documentno, "
			+ "MAX(COALESCE(c_bpartner_ID,0)) C_BPartner_ID, "
			+ "COALESCE(1011338,0) c_bpartner_location_ID, "
			+ "MAX(COALESCE(M_product_ID, 0)) M_product_ID, "
			+ "MAX(replace(description,chr(47),chr(45))) description, "
			+ "MAX(name) as tipogt, MAX(ContactName) formacgt, "
			+ "MAX(trim(upper(Address2))) as subtienda, MAX(ChargeName) as vendedor, "
			+ "MAX(Coalesce(SalesRep_ID,0)) SalesRep_ID, "
			+ "MAX(LineDescription) as Generico, SUM(COALESCE(QtyOrdered,0)) as Cantidad, "
			+ "MAX(COALESCE(PriceActual,0)) Precio, MAX(to_char(Created,'yyyy-mm-dd')) Created, "
			+ "ProductValue, MAX(BPartnerValue) BPartnerValue, MAX(Address1) Address1, "
			+ "MAX(DOCTYPENAME) DOCTYPENAME, MAX(POReference) POReference, "
			+ "MAX(ov_org_id) ov_org_id, MAX(ODOO_ID) ODOO_ID, "
			+ "MAX(NOMBRESHOPIFY) NOMBRESHOPIFY, MAX(DIRECCIONSHOPIFY) DIRECCIONSHOPIFY, "
			+ "MAX(CreatedBy) CreatedBy "
			+ "FROM I_OrderB2CAut_Odoo WHERE DocumentNo = '" + documentNo + "' "
			+ "GROUP BY documentno, ProductValue "
			+ "ORDER BY ProductValue";

		int errorp = 0, errorbp = 0, errorbpl = 0, erroruser = 0, errorst = 0, errorsl = 0;

		Integer C_BPartner_ID = 0;
		Integer C_BPartner_Location_ID = 0;
		Integer SalesRep_ID = 0;
		String description = "";
		Integer M_PriceList_ID = 1000040;
		Integer M_Warehouse_ID = 1000001;
		Integer C_BPartner_SubTienda_ID = 0;
		String Created = "";
		String FormaCompra = "";
		String mc = "";
		String DateAcct = null;
		Integer C_PaymentTerm_ID = 0;
		Integer CreatedBy = 100;
		String ODOO_ID = "";
		String NOMBRESHOPIFY = "";
		String DIRECCIONSHOPIFY = "";
		String POReference = "";

		// =====================================================================
		// PASO 1: Cargar TODAS las filas agrupadas en memoria y cerrar cursor
		// =====================================================================
		List /*<Map>*/ filasDoc = new ArrayList();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(recorre, get_TrxName());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				java.util.HashMap /*<String,String>*/ fila = new java.util.HashMap();
				fila.put("Cantidad", String.valueOf(rs.getInt("Cantidad")));
				fila.put("C_BPartner_ID", String.valueOf(rs.getInt("C_BPartner_ID")));
				fila.put("BPartnerValue", rs.getString("BPartnerValue"));
				fila.put("C_BPartner_Location_ID", String.valueOf(rs.getInt("C_BPartner_Location_ID")));
				fila.put("Address1", rs.getString("Address1"));
				fila.put("M_Product_ID", String.valueOf(rs.getInt("M_Product_ID")));
				fila.put("Generico", rs.getString("Generico"));
				fila.put("description", rs.getString("description"));
				fila.put("Created", rs.getString("Created"));
				fila.put("SalesRep_ID", String.valueOf(rs.getInt("SalesRep_ID")));
				fila.put("vendedor", rs.getString("vendedor"));
				fila.put("subtienda", rs.getString("subtienda"));
				fila.put("Precio", String.valueOf(rs.getInt("Precio")));
				fila.put("ProductValue", rs.getString("ProductValue"));
				fila.put("DOCTYPENAME", rs.getString("DOCTYPENAME"));
				fila.put("ODOO_ID", rs.getString("ODOO_ID"));
				String ns = rs.getString("NOMBRESHOPIFY");
				fila.put("NOMBRESHOPIFY", ns == null ? "" : ns);
				String ds = rs.getString("DIRECCIONSHOPIFY");
				fila.put("DIRECCIONSHOPIFY", ds == null ? "" : ds);
				fila.put("POReference", rs.getString("POReference"));
				fila.put("CreatedBy", String.valueOf(rs.getInt("CreatedBy")));
				filasDoc.add(fila);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error cargando filas Doc=" + documentNo + ": " + e.getMessage(), e);
			return false;
		} finally {
			closeQuietly(rs);
			closeQuietly(pstmt);
		}

		if (filasDoc.isEmpty()) {
			log.warning("Doc=" + documentNo + " sin filas agrupadas.");
			return false;
		}

		log.info("Doc=" + documentNo + ": " + filasDoc.size() + " filas cargadas en memoria.");

		// =====================================================================
		// PASO 2: Iterar sobre la lista en memoria (cursor ya cerrado)
		// =====================================================================
		int lineas = 1;
		int b2c_id = 0;

		try {
			for (int idx = 0; idx < filasDoc.size(); idx++) {
				java.util.HashMap /*<String,String>*/ fila = (java.util.HashMap) filasDoc.get(idx);

				boolean isInsertLine = false;
				Integer Cantidad = Integer.parseInt((String) fila.get("Cantidad"));
				C_BPartner_ID = Integer.parseInt((String) fila.get("C_BPartner_ID"));
				String BPartnerValue = (String) fila.get("BPartnerValue");
				C_BPartner_Location_ID = Integer.parseInt((String) fila.get("C_BPartner_Location_ID"));
				String Address1 = (String) fila.get("Address1");
				Integer M_Product_ID = Integer.parseInt((String) fila.get("M_Product_ID"));
				String Generico = (String) fila.get("Generico");
				description = (String) fila.get("description");
				Created = (String) fila.get("Created");
				SalesRep_ID = Integer.parseInt((String) fila.get("SalesRep_ID"));
				String vendedor = (String) fila.get("vendedor");
				String subtienda = (String) fila.get("subtienda");
				Integer Precio = Integer.parseInt((String) fila.get("Precio"));
				String ProductValue = (String) fila.get("ProductValue");
				String DOCTYPENAME = (String) fila.get("DOCTYPENAME");
				String VentaEnVerde = "";
				String Processed = "";
				DateAcct = (String) fila.get("Created");
				String ErrorMsgCab = "";
				C_PaymentTerm_ID = DB.getSQLValue(null,
					"SELECT MAX(C_PaymentTerm_ID) FROM C_BPartner WHERE C_BPartner_ID = " + C_BPartner_ID);
				ODOO_ID = (String) fila.get("ODOO_ID");
				NOMBRESHOPIFY = (String) fila.get("NOMBRESHOPIFY");
				DIRECCIONSHOPIFY = (String) fila.get("DIRECCIONSHOPIFY");
				POReference = (String) fila.get("POReference");
				CreatedBy = Integer.parseInt((String) fila.get("CreatedBy"));

				if (Cantidad >= 1) {
					if (C_BPartner_ID == 0) {
						errorbp = 1;
						menj1 = "Rut Cliente no encontrado:" + BPartnerValue;
					}
					if (C_BPartner_Location_ID == 0) {
						errorbpl = 2;
						menj2 = "Direccion no coincide con la base de datos:" + Address1;
					}
					if (M_Product_ID == 0) {
						errorp = 3;
						menj3 = "No viene el codigo Windsor o no coincide con la Base de datos";
					}
					if (M_Product_ID == 0 && (Generico == null || Generico.equals(""))) {
						errorp = 4;
						menj4 = "Sin Codigo Windsor y sin Sku cliente";
					}
				}
				if (SalesRep_ID == 0) {
					erroruser = 6;
					menj6 = "Vendedor no encontrado:" + vendedor + " se asigna jgalemiri ";
					SalesRep_ID = 1000032;
				}

				if (errorbp != 0) {
					ErrorMsgCab = menj1;
				} else if (errorbpl != 0) {
					ErrorMsgCab = menj2;
				} else if (erroruser != 0) {
					ErrorMsgCab = menj6;
				}

				if (b2c_id == 0) {
					// === CREAR CABECERA ===
					b2c_id = Integer.parseInt(DB.getSQLValueString(null,
						"Select NEXTIDFUNC(1005416,'N') from c_charge where c_charge_ID=1000010"));

					mc = "Internet";
					VentaEnVerde = "N";
					FormaCompra = "";
					Processed = "N";

					// Subtienda
					if (C_BPartner_ID > 0 && subtienda != null) {
						C_BPartner_SubTienda_ID = resolveSubtienda(C_BPartner_ID, subtienda);
						if (C_BPartner_SubTienda_ID < 0) {
							errorst = 8;
							C_BPartner_SubTienda_ID = 0;
						}
					}

					String insert = "INSERT INTO C_ORDERB2CAUT_Odoo "
						+ "(C_ORDERB2CAUT_Odoo_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,"
						+ "C_BPartner_ID,C_BPartner_Location_ID,POReference,DocumentNo,DateAcct,"
						+ "Processed,BPartnerValue,Address1,M_WAREHOUSE_ID,SalesRep_ID,M_PriceList_ID,"
						+ "Description,MedioCompra,VentaEnVerde,FormaCompra,C_BPartner_SubTienda_ID,"
						+ "ErrorMsg, ODOO_ID, DocumentoMuro, NOMBRESHOPIFY, DIRECCIONSHOPIFY) "
						+ "VALUES "
						+ "(" + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy
						+ "," + C_BPartner_ID + "," + C_BPartner_Location_ID + ",'" + POReference + "',"
						+ "(Select NEXTIDFUNC(1004942,'N') from c_charge where c_charge_ID=1000010),"
						+ "to_date('" + Created + "','yyyy-mm-dd'),"
						+ "'" + Processed + "','" + BPartnerValue + "',"
						+ (Address1 == null ? "null" : "'" + Address1 + "'")
						+ "," + M_Warehouse_ID + "," + SalesRep_ID + ",1000000,"
						+ "'" + description + "','" + mc + "','" + VentaEnVerde + "','" + FormaCompra + "',"
						+ (C_BPartner_SubTienda_ID == 0 ? "null" : C_BPartner_SubTienda_ID)
						+ ",'" + ErrorMsgCab + "', '" + ODOO_ID + "', '" + documentNo + "', '"
						+ NOMBRESHOPIFY.replaceAll("'", "") + "', '"
						+ DIRECCIONSHOPIFY.replaceAll("'", "") + "')";
					ejecutarSQL(insert);
				}

				BigDecimal sob = new BigDecimal(0);

				if (b2c_id != 0) {
					int salto = 0;
					int obl_id = Integer.parseInt(DB.getSQLValueString(null,
						"Select NEXTIDFUNC(1005417,'N') from c_charge where c_charge_ID=1000010"));
					String pasarAOV = "";
					String ErrorMsg = "";
					if (errorp > 0) {
						if (errorp == 3) {
							ErrorMsg = menj3;
						} else if (errorp == 4) {
							ErrorMsg = menj4;
						}
					}
					log.finer("M_product_ID=" + M_Product_ID);
					BigDecimal cant = new BigDecimal(Cantidad);

					// === VALIDAR PRODUCTO Y STOCK ===
					int cuentaProducto = DB.getSQLValue(get_TrxName(),
						"Select count(*) from m_product where m_product_ID=" + M_Product_ID);

					if (cuentaProducto == 1) {
						// --- Reserva e-commerce ---
						int[] resultadoEcom = processReservaEcommerce(
							M_Product_ID, C_BPartner_ID, Cantidad, Precio, cant,
							obl_id, b2c_id, lineas, CreatedBy, ProductValue,
							DOCTYPENAME, Generico);
						salto = resultadoEcom[0];
						if (resultadoEcom[1] == 1) isInsertLine = true;
						cant = new BigDecimal(resultadoEcom[2]);

						// --- Reserva fisica ---
						if (salto == 0 || salto == 2) {
							if (salto == 2) {
								lineas++;
								obl_id = Integer.parseInt(DB.getSQLValueString(null,
									"Select NEXTIDFUNC(1005417,'N') from c_charge where c_charge_ID=1000010"));
							}
							int[] resultadoFisica = processReservaFisica(
								M_Product_ID, C_BPartner_ID, C_BPartner_Location_ID,
								Cantidad, Precio, cant, obl_id, b2c_id, lineas,
								CreatedBy, ProductValue, DOCTYPENAME, Generico, salto);
							salto = resultadoFisica[0];
							if (resultadoFisica[1] == 1) isInsertLine = true;
							cant = new BigDecimal(resultadoFisica[2]);
						}

						// --- Disponible ---
						if (salto == 0 || salto == 4 || salto == 2) {
							if (salto == 4 || salto == 2) {
								obl_id = Integer.parseInt(DB.getSQLValueString(null,
									"Select NEXTIDFUNC(1005417,'N') from c_charge where c_charge_ID=1000010"));
								lineas++;
							}
							int[] resultadoDisp = processDisponible(
								M_Product_ID, Cantidad, Precio, cant,
								obl_id, b2c_id, lineas, CreatedBy,
								ProductValue, DOCTYPENAME, Generico, documentNo,
								mensajeCorreo);
							salto = resultadoDisp[0];
							if (resultadoDisp[1] == 1) isInsertLine = true;
							if (resultadoDisp[3] > 0) {
								errorp = 5;
								pasarAOV = "N";
								sob = new BigDecimal(resultadoDisp[4]);
								ErrorMsg = "No hay stock en la bodega actual, verificar en otras bodegas";
								// FIX 2026-07-07 (FG): si ya se insertaron líneas parciales, agregar
								// línea noStock por el resto no cubierto (qty=0, demand=cantRestante, NotPrint)
								int cantResto = resultadoDisp[2];
								if (isInsertLine && cantResto > 0) {
									lineas++;
									int oblNoStock = Integer.parseInt(DB.getSQLValueString(null,
										"Select NEXTIDFUNC(1005417,'N') from c_charge where c_charge_ID=1000010"));
									BigDecimal netoNS = new BigDecimal(cantResto * Precio);
									String insertNoStock = "INSERT INTO C_ORDERB2CLINEAUT_ODOO "
										+ "(C_ORDERB2CLINEAUT_ODOO_ID,C_ORDERB2CAUT_Odoo_ID,AD_Client_ID,AD_Org_ID,"
										+ "CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
										+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,Demand,StockBodegas,ErrorMsg) "
										+ "VALUES (" + oblNoStock + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID
										+ "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + ",0," + netoNS
										+ ",'" + ProductValue + "','" + DOCTYPENAME + "'," + M_Product_ID + ",'N',"
										+ Precio + ",'" + Generico + "'," + cantResto + "," + sob + ",'" + ErrorMsg + "')";
									ejecutarSQL(insertNoStock);
									isInsertLine = true;
								}
							}
							if (resultadoDisp[5] == 1) {
								pasarAOV = "Y"; // no almacenable
							}
						}
					} else {
						errorp = 7;
						pasarAOV = "N";
						ErrorMsg = "Error: Codigo Windsor ingresado no Existe.";
					}

					if (errorst == 8) {
						ErrorMsg = "Subtienda no existe:" + subtienda;
					}

					if (!isInsertLine) {
						BigDecimal neto = new BigDecimal(Cantidad * Precio);
						String insertLine = "INSERT INTO C_ORDERB2CLINEAUT_ODOO "
							+ "(C_ORDERB2CLINEAUT_ODOO_ID,C_ORDERB2CAUT_Odoo_ID,AD_Client_ID,AD_Org_ID,"
							+ "CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
							+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,StockBodegas,ErrorMsg) "
							+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID
							+ "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + "," + Cantidad + ","
							+ neto + ",'" + ProductValue + "',"
							+ "'" + DOCTYPENAME + "'," + M_Product_ID + ",'" + pasarAOV + "'," + Precio
							+ ",'" + Generico + "'," + sob + ",'" + ErrorMsg + "')";
						ejecutarSQL(insertLine);
					}
				}
				lineas++;
			} // for filasDoc

		} catch (Exception e) {
			log.log(Level.SEVERE, "Error en processOneDocument Doc=" + documentNo + ": " + e.getMessage(), e);
			return false;
		}

		// === CREAR NOTA DE VENTA SI NO HAY ERRORES ===
		if (errorp + errorbp + errorbpl + erroruser + errorst == 0) {
			return createOrder(b2c_id, C_BPartner_ID, C_BPartner_Location_ID,
				SalesRep_ID, description, M_Warehouse_ID, M_PriceList_ID,
				C_PaymentTerm_ID, Created, DateAcct, FormaCompra, mc,
				C_BPartner_SubTienda_ID, documentNo, POReference,
				ODOO_ID, CreatedBy, listaOrder);
		} else {
			return false;
		}
	}

	// =========================================================================
	// METODOS AUXILIARES DE RESERVA / STOCK
	// =========================================================================

	/**
	 * Procesa reserva e-commerce.
	 * @return int[] {salto, insertLine(0/1), cantRestante}
	 */
	private int[] processReservaEcommerce(int M_Product_ID, int C_BPartner_ID,
			int Cantidad, int Precio, BigDecimal cant,
			int obl_id, int b2c_id, int lineas, int CreatedBy,
			String ProductValue, String DOCTYPENAME, String Generico) {

		int salto = 0;
		int inserted = 0;
		int cantResult = cant.intValue();

		PreparedStatement pstmtre = null;
		ResultSet rsre = null;
		try {
			String sqlqr = "select count(1) encontrado "
				+ "from m_requisition r "
				+ "where r.docstatus='CO' and r.c_doctype_ID=1000570 "
				+ "and r.c_bpartner_ID=" + C_BPartner_ID + " and "
				+ "r.M_RequisitionRef_ID in "
				+ "(select r2.m_requisition_ID "
				+ "from m_requisition r2 "
				+ "inner join m_requisitionline rl on (r2.m_requisition_ID=rl.m_Requisition_ID) "
				+ "where r2.docstatus='CO' and rl.m_product_ID=" + M_Product_ID
				+ " and (rl.qtyreserved)>0 and r2.c_doctype_ID=1000569 and rl.liberada='N' )";

			pstmtre = DB.prepareStatement(sqlqr, get_TrxName());
			rsre = pstmtre.executeQuery();
			if (rsre.next() && rsre.getInt("encontrado") > 0) {
				PreparedStatement pstmtreq = null;
				ResultSet rsreq = null;
				try {
					String sqlqrq = " select sum(rl.qtyreserved) qtyreserved, "
						+ "max(rl.m_requisitionline_ID) m_requisitionline_ID "
						+ "from m_Requisitionline rl "
						+ "inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "
						+ "where r.docstatus='CO' "
						+ "and r.c_doctype_id=1000569 "
						+ "and rl.liberada='N' "
						+ "and rl.m_product_ID =" + M_Product_ID
						+ " and exists "
						+ "(select * from m_requisition r2 "
						+ "where R2.M_REQUISITIONREF_ID=r.m_requisition_ID "
						+ "and r2.c_doctype_ID=1000570 "
						+ "and r2.docstatus='CO' "
						+ "and r2.c_bpartner_ID=" + C_BPartner_ID + ")";

					pstmtreq = DB.prepareStatement(sqlqrq, get_TrxName());
					rsreq = pstmtreq.executeQuery();
					if (rsreq.next()) {
						int qtyRes = rsreq.getInt("qtyreserved");
						int reqLineId = rsreq.getInt("m_requisitionline_ID");
						// FIX 2026-07-07 (FG): lock RL y cap a dispReal para evitar race condition
						BigDecimal dispReal = getDisponibleRealConLock(reqLineId);
						int qtyEfectivo = dispReal.min(new BigDecimal(qtyRes)).intValue();
						if (qtyEfectivo <= 0) {
							log.warning("ReservaEcom RL=" + reqLineId + " dispReal=" + dispReal + " <=0, skip");
						} else {
							int qtyUsar = Math.min(cant.intValue(), qtyEfectivo);
							BigDecimal aux = new BigDecimal(qtyUsar);
							BigDecimal neto = new BigDecimal(qtyUsar * Precio);
							insertB2CLine(obl_id, b2c_id, lineas, aux, neto,
								ProductValue, DOCTYPENAME, M_Product_ID, Precio, Generico,
								CreatedBy, "Y", reqLineId);
							inserted = 1;
							cantResult = cant.intValue() - qtyUsar;
							salto = (cantResult <= 0) ? 1 : 2;
						}
					}
				} finally {
					closeQuietly(rsreq);
					closeQuietly(pstmtreq);
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error reserva ecommerce: " + e.getMessage(), e);
		} finally {
			closeQuietly(rsre);
			closeQuietly(pstmtre);
		}

		return new int[]{salto, inserted, cantResult};
	}

	/**
	 * Procesa reserva fisica.
	 * @return int[] {salto, insertLine(0/1), cantRestante}
	 */
	private int[] processReservaFisica(int M_Product_ID, int C_BPartner_ID,
			int C_BPartner_Location_ID, int Cantidad, int Precio, BigDecimal cant,
			int obl_id, int b2c_id, int lineas, int CreatedBy,
			String ProductValue, String DOCTYPENAME, String Generico, int saltoIn) {

		int salto = saltoIn;
		int inserted = 0;
		int cantResult = cant.intValue();

		PreparedStatement pstmtrfn = null;
		ResultSet rsrfn = null;
		try {
			String sqlrfn = " select count(1) encontrado "
				+ "from m_requisition r "
				+ "inner join m_requisitionline rl on (r.m_requisition_ID=rl.m_Requisition_ID) "
				+ "where r.docstatus='CO' and r.c_doctype_ID=1000111 "
				+ "and r.c_bpartner_ID=" + C_BPartner_ID
				+ " and (r.c_bpartner_location_ID=" + C_BPartner_Location_ID
				+ " or R.OVERWRITEREQUISITION='Y') "
				+ "and rl.m_product_ID=" + M_Product_ID
				+ " and (rl.qtyreserved)>0 "
				+ "and rl.LIBERADA='N' ";

			pstmtrfn = DB.prepareStatement(sqlrfn, get_TrxName());
			rsrfn = pstmtrfn.executeQuery();
			if (rsrfn.next() && rsrfn.getInt("encontrado") > 0) {
				PreparedStatement pstmtrn = null;
				ResultSet rsrn = null;
				try {
					String sqlrn = " select sum(rl.qtyreserved) qtyreserved, "
						+ "max(rl.m_requisitionline_ID) m_requisitionline_ID "
						+ "from m_Requisitionline rl "
						+ "inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "
						+ "where r.docstatus='CO' "
						+ "and r.c_doctype_id=1000111 "
						+ "and rl.m_product_ID =" + M_Product_ID
						+ " and r.c_bpartner_ID=" + C_BPartner_ID
						+ " and (r.c_bpartner_location_ID=" + C_BPartner_Location_ID
						+ " or R.OVERWRITEREQUISITION='Y') "
						+ "and (rl.qtyreserved)>0 "
						+ "and rl.LIBERADA='N' ";

					pstmtrn = DB.prepareStatement(sqlrn, get_TrxName());
					rsrn = pstmtrn.executeQuery();
					if (rsrn.next()) {
						int qtyRes = rsrn.getInt("qtyreserved");
						int reqLineId = rsrn.getInt("m_requisitionline_ID");
						// FIX 2026-07-07 (FG): lock RL y cap a dispReal para evitar race condition
						BigDecimal dispReal = getDisponibleRealConLock(reqLineId);
						int qtyEfectivo = dispReal.min(new BigDecimal(qtyRes)).intValue();
						if (qtyEfectivo <= 0) {
							log.warning("ReservaFisica RL=" + reqLineId + " dispReal=" + dispReal + " <=0, skip");
						} else {
							int qtyUsar = Math.min(cant.intValue(), qtyEfectivo);
							BigDecimal aux = new BigDecimal(qtyUsar);
							BigDecimal neto = new BigDecimal(qtyUsar * Precio);
							insertB2CLine(obl_id, b2c_id, lineas, aux, neto,
								ProductValue, DOCTYPENAME, M_Product_ID, Precio, Generico,
								CreatedBy, "Y", reqLineId);
							inserted = 1;
							cantResult = cant.intValue() - qtyUsar;
							salto = (cantResult <= 0) ? 3 : 4;
						}
					}
				} finally {
					closeQuietly(rsrn);
					closeQuietly(pstmtrn);
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error reserva fisica: " + e.getMessage(), e);
		} finally {
			closeQuietly(rsrfn);
			closeQuietly(pstmtrfn);
		}

		return new int[]{salto, inserted, cantResult};
	}

	/**
	 * Procesa disponible (stock libre).
	 * @return int[] {salto, insertLine(0/1), cantRestante, sinStock(0/1), otroDisponible, noAlmacenable(0/1)}
	 */
	private int[] processDisponible(int M_Product_ID, int Cantidad, int Precio,
			BigDecimal cant, int obl_id, int b2c_id, int lineas, int CreatedBy,
			String ProductValue, String DOCTYPENAME, String Generico,
			String documentNo, StringBuffer mensajeCorreo) {

		int salto = 0;
		int inserted = 0;
		int cantResult = cant.intValue();
		int sinStock = 0;
		int otroDisponible = 0;
		int noAlmacenable = 0;

		PreparedStatement pstmtps = null;
		ResultSet rsps = null;
		try {
			String sqlps = "Select "
				+ "COALESCE ("
				+ "  (SELECT SUM(s.qtyonhand) FROM rv_storage380 s "
				+ "   WHERE s.M_Product_ID = p.m_product_id "
				+ "   AND s.m_warehouse_id IN (1000001, 1000010, 1000033) "
				+ "   AND s.isactive = 'Y'), 0) "
				+ "- ( (SELECT COALESCE(SUM(ol2.qtyreserved), 0) "
				+ "     FROM C_orderline ol2 "
				+ "     INNER JOIN C_Order o2 ON (ol2.C_ORDER_ID = o2.c_order_ID) "
				+ "     WHERE ol2.M_Product_ID = p.m_product_id "
				+ "     AND o2.m_warehouse_id = 1000001 "
				+ "     AND o2.saldada <> 'Y' "
				+ "     AND o2.docstatus IN ('IP', 'CO', 'CL') "
				+ "     AND o2.issotrx = 'Y' "
				+ "     AND o2.c_doctypetarget_ID NOT IN (1000110, 1000048, 1000568)) "
				+ "  + (SELECT COALESCE(SUM(rl.qtyreserved), 0) "
				+ "     FROM M_Requisitionline rl "
				+ "     INNER JOIN M_Requisition r ON (rl.M_Requisition_ID = r.M_Requisition_ID) "
				+ "     WHERE rl.M_Product_ID = p.m_product_id "
				+ "     AND r.m_warehouse_id = 1000001 "
				+ "     AND r.docstatus IN ('CO', 'CL') "
				+ "     AND r.issotrx = 'Y')) "
				+ "as Disponible, p.ProductType, "
				+ "COALESCE ("
				+ "  (SELECT SUM(s.qtyonhand) FROM rv_storage s "
				+ "   WHERE s.M_Product_ID = p.m_product_id "
				+ "   AND s.m_warehouse_id IN (1000024, 1000025) "
				+ "   AND s.isactive = 'Y'), 0) "
				+ "- ( (SELECT COALESCE(SUM(ol2.qtyreserved), 0) "
				+ "     FROM C_orderline ol2 "
				+ "     INNER JOIN C_Order o2 ON (ol2.C_ORDER_ID = o2.c_order_ID) "
				+ "     WHERE ol2.M_Product_ID = p.m_product_id "
				+ "     AND o2.m_warehouse_id IN (1000024, 1000025) "
				+ "     AND o2.saldada <> 'Y' "
				+ "     AND o2.docstatus IN ('IP', 'CO', 'CL') "
				+ "     AND o2.issotrx = 'Y' "
				+ "     AND o2.c_doctypetarget_ID NOT IN (1000110, 1000048, 1000568)) "
				+ "  + (SELECT COALESCE(SUM(rl.qtyreserved), 0) "
				+ "     FROM M_Requisitionline rl "
				+ "     INNER JOIN M_Requisition r ON (rl.M_Requisition_ID = r.M_Requisition_ID) "
				+ "     WHERE rl.M_Product_ID = p.m_product_id "
				+ "     AND r.m_warehouse_id IN (1000024, 1000025) "
				+ "     AND r.docstatus IN ('CO', 'CL') "
				+ "     AND r.issotrx = 'Y')) "
				+ "as OtroDisponible "
				+ "from M_product p where p.m_product_ID=" + M_Product_ID;

			pstmtps = DB.prepareStatement(sqlps, get_TrxName());
			rsps = pstmtps.executeQuery();
			if (rsps.next()) {
				if (rsps.getString("ProductType").equals("I")) {
					int dispBodega = rsps.getInt("Disponible");
					if (dispBodega >= cant.intValue()) {
						// Stock cubre todo el pendiente
						BigDecimal neto = new BigDecimal(cant.intValue() * Precio);
						String insertLine = "INSERT INTO C_ORDERB2CLINEAUT_ODOO "
							+ "(C_ORDERB2CLINEAUT_ODOO_ID,C_ORDERB2CAUT_Odoo_ID,AD_Client_ID,AD_Org_ID,"
							+ "CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
							+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO) "
							+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID
							+ "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + "," + cant + "," + neto
							+ ",'" + ProductValue + "','" + DOCTYPENAME + "'," + M_Product_ID + ",'Y',"
							+ Precio + ",'" + Generico + "')";
						ejecutarSQL(insertLine);
						inserted = 1;
						cantResult = 0;
						salto = 5;
					} else if (dispBodega > 0) {
						// FIX 2026-07-07 (FG): stock parcial — insertar lo disponible, retornar resto para noStock
						BigDecimal parcial = new BigDecimal(dispBodega);
						BigDecimal neto = new BigDecimal(dispBodega * Precio);
						String insertLine = "INSERT INTO C_ORDERB2CLINEAUT_ODOO "
							+ "(C_ORDERB2CLINEAUT_ODOO_ID,C_ORDERB2CAUT_Odoo_ID,AD_Client_ID,AD_Org_ID,"
							+ "CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
							+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO) "
							+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID
							+ "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + "," + parcial + "," + neto
							+ ",'" + ProductValue + "','" + DOCTYPENAME + "'," + M_Product_ID + ",'Y',"
							+ Precio + ",'" + Generico + "')";
						ejecutarSQL(insertLine);
						inserted = 1;
						cantResult = cant.intValue() - dispBodega;
						sinStock = 1;
						otroDisponible = rsps.getInt("OtroDisponible");
						salto = 6;
						mensajeCorreo.append("Pedido " + documentNo + " Producto " + ProductValue
							+ " Stock parcial: " + dispBodega + " de " + cant.intValue() + " solicitados<br />");
					} else {
						sinStock = 1;
						otroDisponible = rsps.getInt("OtroDisponible");
						salto = 6;
						mensajeCorreo.append("Pedido " + documentNo + " Producto " + ProductValue
							+ " No hay stock en la bodega actual, verificar en otras bodegas<br />");
					}
				} else {
					noAlmacenable = 1;
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error disponible: " + e.getMessage(), e);
		} finally {
			closeQuietly(rsps);
			closeQuietly(pstmtps);
		}

		return new int[]{salto, inserted, cantResult, sinStock, otroDisponible, noAlmacenable};
	}

	/**
	 * Inserta una linea en C_ORDERB2CLINEAUT_ODOO con M_RequisitionLine_ID.
	 */
	private void insertB2CLine(int obl_id, int b2c_id, int lineas,
			BigDecimal qty, BigDecimal neto, String ProductValue,
			String DOCTYPENAME, int M_Product_ID, int Precio, String Generico,
			int CreatedBy, String pasarAOV, int reqLineId) {

		String insertLine = "INSERT INTO C_ORDERB2CLINEAUT_ODOO "
			+ "(C_ORDERB2CLINEAUT_ODOO_ID,C_ORDERB2CAUT_Odoo_ID,AD_Client_ID,AD_Org_ID,"
			+ "CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
			+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
			+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID
			+ "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + "," + qty + "," + neto
			+ ",'" + ProductValue + "',"
			+ (DOCTYPENAME == null ? "null" : "'" + DOCTYPENAME + "'")
			+ "," + M_Product_ID + ",'" + pasarAOV + "'," + Precio + ","
			+ (Generico == null ? "null" : "'" + Generico + "'")
			+ "," + reqLineId + ")";
		ejecutarSQL(insertLine);
	}

	// =========================================================================
	// CREACION DE LA ORDEN C_Order
	// =========================================================================

	/**
	 * Crea la C_Order y sus lineas a partir de C_ORDERB2CAUT_Odoo / C_ORDERB2CLINEAUT_ODOO.
	 */
	private boolean createOrder(int b2c_id, int C_BPartner_ID, int C_BPartner_Location_ID,
			int SalesRep_ID, String description, int M_Warehouse_ID, int M_PriceList_ID,
			int C_PaymentTerm_ID, String Created, String DateAcct, String FormaCompra,
			String mc, int C_BPartner_SubTienda_ID, String documentNo, String POReference,
			String ODOO_ID, int CreatedBy, List /*<MOrder>*/ listaOrder) {

		// Validar que haya lineas
		int cuentaLineas = DB.getSQLValue(get_TrxName(),
			"Select count(*) from C_ORDERB2CLINEAUT_ODOO where C_ORDERB2CAUT_Odoo_ID=" + b2c_id);
		if (cuentaLineas <= 0) {
			log.warning("Doc=" + documentNo + " sin lineas en staging (errorsl=9)");
			return false;
		}

		// Validar que todas las lineas tengan PASARAOV='Y'
		int cuentaNoOk = DB.getSQLValue(get_TrxName(),
			"Select count(*) from C_ORDERB2CLINEAUT_ODOO where PASARAOV<>'Y' and C_ORDERB2CAUT_Odoo_ID=" + b2c_id);
		if (cuentaNoOk > 0) {
			log.warning("Doc=" + documentNo + " tiene lineas sin PASARAOV='Y' (errorsl=10)");
			return false;
		}

		// =====================================================================
		// Cargar TODAS las lineas B2C en memoria y cerrar cursor
		// =====================================================================
		List /*<java.util.HashMap>*/ filasLineas = new ArrayList();
		List /*<Integer>*/ reqLineIdsToUpdate = new ArrayList();
		PreparedStatement pstmtlines = null;
		ResultSet rslines = null;
		try {
			String sqllines = "Select ol.*, COALESCE(mp.C_UOM_ID,100) AS C_UOM_ID "
				+ "from C_ORDERB2CLINEAUT_ODOO ol "
				+ "LEFT JOIN M_PRODUCT mp ON ol.M_PRODUCT_ID = mp.M_PRODUCT_ID "
				+ "WHERE C_ORDERB2CAUT_Odoo_ID = " + b2c_id;

			pstmtlines = DB.prepareStatement(sqllines, get_TrxName());
			rslines = pstmtlines.executeQuery();
			while (rslines.next()) {
				java.util.HashMap /*<String,String>*/ fila = new java.util.HashMap();
				fila.put("C_ORDERB2CLINEAUT_ODOO_ID", String.valueOf(rslines.getBigDecimal("C_ORDERB2CLINEAUT_ODOO_ID")));
				fila.put("M_Product_ID", String.valueOf(rslines.getInt("M_Product_ID")));
				fila.put("priceentered", rslines.getBigDecimal("priceentered").toString());
				fila.put("qtyentered", rslines.getBigDecimal("qtyentered").toString());
				fila.put("C_UOM_ID", rslines.getBigDecimal("C_UOM_ID").toString());
				BigDecimal reqLineIdBD = rslines.getBigDecimal("M_RequisitionLine_ID");
				fila.put("M_RequisitionLine_ID", reqLineIdBD == null ? null : reqLineIdBD.toString());
				filasLineas.add(fila);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error cargando lineas B2C Doc=" + documentNo + ": " + e.getMessage(), e);
			return false;
		} finally {
			closeQuietly(rslines);
			closeQuietly(pstmtlines);
		}

		if (filasLineas.isEmpty()) {
			log.warning("Doc=" + documentNo + " sin lineas B2C en memoria.");
			return false;
		}

		log.info("Doc=" + documentNo + ": " + filasLineas.size() + " lineas B2C cargadas en memoria.");

		// =====================================================================
		// Iterar sobre la lista en memoria (cursor ya cerrado)
		// =====================================================================
		try {
			int orderId = 0;
			int contador = 1;
			int documentNoSeqNum = 1;

			for (int idx = 0; idx < filasLineas.size(); idx++) {
				java.util.HashMap /*<String,String>*/ fila = (java.util.HashMap) filasLineas.get(idx);

				if (orderId == 0) {
					orderId = Integer.parseInt(DB.getSQLValueString(null,
						"Select NEXTIDFUNC(232,'N') from c_charge where c_charge_ID=1000010"));

					int C_DocTypeTarget_ID = 1000030;
					String documentNoSeq = (documentNoSeqNum > 1)
						? documentNo + "-" + documentNoSeqNum
						: documentNo;

					String insertOrder = "INSERT INTO C_Order "
						+ "(C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,"
						+ "C_DocTypeTarget_ID,IsSOTrx,DeliveryRule,"
						+ "C_BPartner_ID,C_BPartner_Location_ID,POReference,AD_User_ID,"
						+ "Bill_BPartner_ID,Bill_Location_ID,Description,"
						+ "C_PaymentTerm_ID,M_PriceList_ID,M_Warehouse_ID,SalesRep_ID,"
						+ "DateOrdered,DateAcct,InvoiceRule,"
						+ "DOCUMENTOMURO,VENTAINVIERNO,DocumentNo,DOCSTATUS,DOCACTION,"
						+ "C_DOCTYPE_ID,C_CURRENCY_ID,"
						+ "PAYMENTRULE,FREIGHTCOSTRULE,DELIVERYVIARULE,PRIORITYRULE,"
						+ "DatePromised,C_BPartner_SubTienda_ID,"
						+ "FIRMA2,FIRMA3,FIRMACOM,FIRMAFIN,"
						+ "USERFIRMCOM,FormaCompra,MedioCompra, ODOO_ID) "
						+ "VALUES ("
						+ orderId + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + ","
						+ C_DocTypeTarget_ID + ",'Y','O',"
						+ C_BPartner_ID + "," + C_BPartner_Location_ID + ",'" + POReference + "'," + SalesRep_ID + ","
						+ C_BPartner_ID + "," + C_BPartner_Location_ID + ",'" + description + "',"
						+ C_PaymentTerm_ID + "," + M_PriceList_ID + "," + M_Warehouse_ID + "," + SalesRep_ID + ","
						+ "to_date('" + DateAcct + "','yyyy-mm-dd'),to_date('" + DateAcct + "','yyyy-mm-dd'),'D',"
						+ "'" + documentNo + "','N','" + documentNoSeq + "','DR','CO',1000030,228,"
						+ "'P','I','P','5',"
						+ "to_date('" + Created + "','yyyy-mm-dd')," + C_BPartner_SubTienda_ID + ","
						+ "'Y','Y',to_date('" + DateAcct + "','yyyy-mm-dd'),to_date('" + DateAcct + "','yyyy-mm-dd'),"
						+ 1003655 + ",'" + FormaCompra + "','" + mc + "', '" + ODOO_ID + "')";
					ejecutarSQL(insertOrder);
				}

				if (contador <= 25) {
					int orderLineId = Integer.parseInt(DB.getSQLValueString(null,
						"Select NEXTIDFUNC(233,'N') from c_charge where c_charge_ID=1000010"));

					BigDecimal priceEntered = new BigDecimal((String) fila.get("priceentered"));
					BigDecimal qtyEntered = new BigDecimal((String) fila.get("qtyentered"));
					String reqLineIdStr = (String) fila.get("M_RequisitionLine_ID");
					BigDecimal reqLineIdBD = (reqLineIdStr != null) ? new BigDecimal(reqLineIdStr) : null;

					// FIX 2026-06-09 (FG): pre-check disponible real con FOR UPDATE lock
					// evita inflación y race con otros batches. Si reserva saturada,
					// degradar línea a Qty=0 + NotPrint='Y' para no romper la orden.
					if (reqLineIdBD != null) {
						BigDecimal dispReal = getDisponibleRealConLock(reqLineIdBD.intValue());
						if (dispReal.compareTo(qtyEntered) < 0) {
							log.warning("B2C OrderLine producto=" + fila.get("M_Product_ID")
								+ " ReqLine=" + reqLineIdBD + " qtySolicitada=" + qtyEntered
								+ " dispReal=" + dispReal + " AJUSTANDO a dispReal");
							if (dispReal.compareTo(BigDecimal.ZERO) > 0) {
								qtyEntered = dispReal;
							} else {
								qtyEntered = BigDecimal.ZERO;
								reqLineIdBD = null;
							}
						}
					}
					BigDecimal lineNetAmt = priceEntered.multiply(qtyEntered).setScale(0, RoundingMode.HALF_UP);

					String insertOrderLine = "INSERT INTO C_OrderLine "
						+ "(C_OrderLine_ID,C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,"
						+ "M_Product_ID,PriceEntered,PriceActual,PriceList,"
						+ "QtyEntered,Line,Qty,Demand,Discount,Discount2,Discount3,Discount4,Discount5,"
						+ "NotPrint,LineNetAmt,DATEORDERED,M_WAREHOUSE_ID,C_UOM_ID,"
						+ "C_CURRENCY_ID,C_TAX_ID,M_RequisitionLine_ID,"
						+ "C_BPARTNER_ID,C_BPARTNER_LOCATION_ID,DATEPROMISED) "
						+ "VALUES ("
						+ orderLineId + "," + orderId + "," + m_AD_Client_ID + "," + m_AD_Org_ID
						+ "," + CreatedBy + "," + CreatedBy + ","
						+ Integer.parseInt((String) fila.get("M_Product_ID")) + "," + priceEntered + "," + priceEntered + "," + priceEntered + ","
						+ qtyEntered + "," + (contador * 10) + "," + qtyEntered + "," + qtyEntered
						+ ",0,0,0,0,0,'N'," + lineNetAmt
						+ ",to_date('" + Created + "','yyyy-mm-dd')," + M_Warehouse_ID
						+ "," + new BigDecimal((String) fila.get("C_UOM_ID")) + ","
						+ "228,1000000," + (reqLineIdBD == null ? "null" : reqLineIdBD)
						+ "," + C_BPartner_ID + "," + C_BPartner_Location_ID
						+ ",to_date('" + Created + "','yyyy-mm-dd'))";
					ejecutarSQL(insertOrderLine);
					contador++;

					// Acumular reqLineIds para batch update al final
					if (reqLineIdBD != null) {
						reqLineIdsToUpdate.add(reqLineIdBD.toBigInteger().intValue());
					}

					// Actualizar B2C Line
					String updateOrderB2CLine = "UPDATE C_ORDERB2CLINEAUT_ODOO"
						+ " SET C_Order_ID = " + orderId + ", Processed = 'Y', c_orderline_id = " + orderLineId
						+ " WHERE C_ORDERB2CLINEAUT_ODOO_ID = " + (String) fila.get("C_ORDERB2CLINEAUT_ODOO_ID");
					ejecutarSQL(updateOrderB2CLine);
				}

				// Si supera 25 lineas, completar orden actual y crear nueva
				if (contador == 26) {
					completeOrder(orderId, documentNo);
					contador = 1;
					orderId = 0;
					documentNoSeqNum++;
				}
			}

			// Completar ultima orden si quedo pendiente
			if (orderId != 0) {
				completeOrder(orderId, documentNo);
				MOrder order = new MOrder(getCtx(), orderId, get_TrxName());
				listaOrder.add(order);
			}

			// FIX 2026-06-09 (FG): batchUpdateRequisitionLineQtyUsed DESACTIVADO.
			// Fórmula basada en SUM(QtyEntered) divergía del ModelValidator
			// ModWindsorUpdateReserved que usa SUM(QtyDelivered) post-fix.
			// El validator AFTER_COMPLETE de la OV es la unica fuente de verdad.
			// if (!reqLineIdsToUpdate.isEmpty()) {
			//	batchUpdateRequisitionLineQtyUsed(reqLineIdsToUpdate);
			// }

			// Marcar cabecera B2C como procesada
			String update = "UPDATE C_ORDERB2CAUT_Odoo SET Processed = 'Y' WHERE C_ORDERB2CAUT_Odoo_ID = " + b2c_id;
			ejecutarSQL(update);

			return true;

		} catch (Exception e) {
			log.log(Level.SEVERE, "Error creando orden Doc=" + documentNo + ": " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Completa una orden: calcula impuestos y ejecuta workflow CO.
	 */
	private void completeOrder(int orderId, String documentNo) {
		try {
			MOrder order = new MOrder(getCtx(), orderId, get_TrxName());
			order.calculateTaxTotal();
			order.saveEx();
			if (!"CO".equalsIgnoreCase(order.getDocStatus())) {
				order.setDocAction("CO");
				if (!order.processIt("CO")) {
					log.warning("Error al completar Documento " + order.getDocumentNo());
				} else {
					order.save();
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error completando orden ID=" + orderId + " Doc=" + documentNo, e);
		}
	}

	/**
	 * Batch update de QtyUsed en M_RequisitionLine usando un solo MERGE.
	 * Evita N updates individuales que disparan afterSave N veces.
	 */
	private void batchUpdateRequisitionLineQtyUsed(List /*<Integer>*/ reqLineIds) {
		if (reqLineIds == null || reqLineIds.isEmpty()) return;

		// Construir lista de IDs sin duplicados
		StringBuffer idList = new StringBuffer();
		java.util.HashSet /*<Integer>*/ seen = new java.util.HashSet();
		for (int i = 0; i < reqLineIds.size(); i++) {
			Integer id = (Integer) reqLineIds.get(i);
			if (seen.add(id)) {
				if (idList.length() > 0) idList.append(",");
				idList.append(id);
			}
		}

		// FIX 2026-06: actualizar TAMBIEN QtyReserved = MAX(0, Qty - newQtyUsed)
		// DocStatus: solo CO/IP/CL — DR e IN no cuentan como consumo confirmado
		String sql = "MERGE INTO M_RequisitionLine rl"
			+ " USING ("
			+ "   SELECT col.M_RequisitionLine_ID,"
			+ "          SUM(col.QTYENTERED) AS new_qtyused,"
			+ "          GREATEST(0, rl2.Qty - SUM(col.QTYENTERED)) AS new_qtyreserved"
			+ "   FROM C_OrderLine col"
			+ "   INNER JOIN C_Order co ON (col.C_Order_ID = co.C_Order_ID)"
			+ "   INNER JOIN M_RequisitionLine rl2 ON (rl2.M_RequisitionLine_ID = col.M_RequisitionLine_ID)"
			+ "   WHERE col.M_RequisitionLine_ID IN (" + idList.toString() + ")"
			+ "   AND co.DocStatus IN ('IP','CO','CL')"
			+ "   GROUP BY col.M_RequisitionLine_ID, rl2.Qty"
			+ " ) data"
			+ " ON (rl.M_RequisitionLine_ID = data.M_RequisitionLine_ID)"
			+ " WHEN MATCHED THEN UPDATE"
			+ "   SET rl.QtyUsed = data.new_qtyused,"
			+ "       rl.QtyReserved = data.new_qtyreserved"
			+ "   WHERE rl.QtyUsed <> data.new_qtyused OR rl.QtyReserved <> data.new_qtyreserved";
		DB.executeUpdate(sql, get_TrxName());
		log.info("Batch update QtyUsed: " + seen.size() + " M_RequisitionLine actualizadas.");
	}

	// =========================================================================
	// METODOS AUXILIARES
	// =========================================================================

	/**
	 * Resuelve C_BPartner_SubTienda_ID para un BP y nombre de subtienda.
	 * @return ID de subtienda, o -1 si no existe.
	 */
	private int resolveSubtienda(int C_BPartner_ID, String subtienda) {
		int count = DB.getSQLValue(get_TrxName(),
			"Select count(*) from C_BPartner_SubTienda where c_Bpartner_ID=" + C_BPartner_ID
			+ " and trim(upper(name))= trim(upper('" + subtienda + "'))");
		if (count > 0) {
			int stId = DB.getSQLValue(get_TrxName(),
				"Select max(C_BPartner_SubTienda_ID) from C_BPartner_SubTienda where c_Bpartner_ID="
				+ C_BPartner_ID + " and trim(upper(name))= trim(upper('" + subtienda + "'))");
			return stId;
		}
		return -1;
	}

	/**
	 * Detecta si una excepcion (o su causa raiz) es SQLRecoverableException,
	 * lo que indica que la conexion a la BD se perdio irrecuperablemente.
	 */
	private boolean isSQLRecoverable(Throwable t) {
		while (t != null) {
			if (t instanceof java.sql.SQLRecoverableException) {
				return true;
			}
			String msg = t.getMessage();
			if (msg != null && (msg.contains("datos para leer del socket")
				|| msg.contains("No more data to read from socket")
				|| msg.contains("Connection reset")
				|| msg.contains("Closed Connection"))) {
				return true;
			}
			t = t.getCause();
		}
		return false;
	}

	/**
	 * Verifica si otro proceso esta en ejecucion consultando AD_PInstance.
	 * @param adProcessId AD_Process_ID del proceso a verificar
	 * @return true si hay alguna instancia activa de ese proceso
	 */
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
	 * Cierra un ResultSet silenciosamente.
	 */
	private void closeQuietly(ResultSet rs) {
		if (rs != null) {
			try { rs.close(); } catch (Exception e) { /* ignorar */ }
		}
	}

	/**
	 * Cierra un PreparedStatement silenciosamente.
	 */
	private void closeQuietly(PreparedStatement ps) {
		if (ps != null) {
			try { ps.close(); } catch (Exception e) { /* ignorar */ }
		}
	}

	// FIX 2026-06-09 (FG): obtiene espacio real sin comprometer en ReqLine
	// con FOR UPDATE lock. Mantiene lock hasta commit/rollback de la trx actual.
	//
	// FIX 2026-06-11 (FG): LEAST de dos checks:
	//   1) Qty - SUM(OVs CO/IP/DR) - SUM(QtyDelivered CL): espacio no comprometido
	//   2) GREATEST(0, qtyavailableopenvianum + QtyReserved): backing fisico real
	//      (qtyavailableopenvianum ya descontó QtyReserved; sumarlo devuelve el
	//       stock fisico que respalda ESTA reserva. Si es 0, la reserva esta obsoleta.)
	// El LEAST previene consumir mas de lo que el stock fisico puede respaldar.
	//
	// FIX 2026-07-07 (FG): check1 ahora RESTA SUM(QtyDelivered de OVs CL). Antes solo
	// contaba QtyOrdered de CO/IP/DR e IGNORABA lo ya entregado en OVs cerradas (CL).
	// Pero ModWindsorUpdateReserved computa QtyUsed=SUM(QtyDelivered CO/CL/IP), asi que
	// al completar la OV el recalculo daba QtyUsed+QtyReserved > Qty -> DISPONIBLE<0.
	// Restar el entregado-CL alinea el pre-check con el recalculo y evita el negativo.
	private BigDecimal getDisponibleRealConLock(int reqLineId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// FIX 2026-07-08 (FG): qtyavailableopenvianum es costosa (10 sub-queries para BOM).
			// Llamarla DENTRO del FOR UPDATE mantiene el lock durante toda su ejecucion (20-95ms),
			// serializando todos los imports que comparten la misma reqLine.
			// Fix: pre-calcular el cap de bodega SIN lock, luego usar el valor literal en el FOR UPDATE.
			BigDecimal dispBodega = DB.getSQLValueBD(get_TrxName(),
				"SELECT NVL(qtyavailableopenvianum(M_Product_ID),0)" +
				" FROM M_RequisitionLine WHERE M_RequisitionLine_ID=" + reqLineId,
				new Object[0]);
			if (dispBodega == null) dispBodega = BigDecimal.ZERO;
			// FIX 2026-07-30 (FG): NO clampear dispBodega negativo a 0 aqui. El GREATEST(0, ...)
			// de mas abajo ya es el piso correcto DESPUES de sumar rl.QtyReserved. Clampear antes
			// perdia la senal de negativo: con dispBodega=-7 y QtyReserved=3, el resultado correcto
			// es GREATEST(0,-7+3)=0 (reserva sin respaldo fisico, bloquea consumo); clampeando a 0
			// primero daba GREATEST(0,0+3)=3, dejando usar la reserva pese al disponible negativo.

			String sql = "SELECT LEAST(" +
				" rl.Qty - NVL((" +
				"   SELECT SUM(co.QtyOrdered) FROM C_OrderLine co" +
				"   JOIN C_Order o ON o.C_Order_ID = co.C_Order_ID" +
				"   WHERE co.M_RequisitionLine_ID = rl.M_RequisitionLine_ID" +
				"     AND o.DocStatus IN ('CO','IP','DR')" +
				" ), 0) - NVL((" +
				"   SELECT SUM(co2.QtyDelivered) FROM C_OrderLine co2" +
				"   JOIN C_Order o2 ON o2.C_Order_ID = co2.C_Order_ID" +
				"   WHERE co2.M_RequisitionLine_ID = rl.M_RequisitionLine_ID" +
				"     AND o2.DocStatus = 'CL'" +
				" ), 0)," +
				" GREATEST(0, " + dispBodega.toPlainString() + " + rl.QtyReserved)" +
				") AS disponible" +
				" FROM M_RequisitionLine rl" +
				" WHERE rl.M_RequisitionLine_ID = " + reqLineId + " FOR UPDATE";
			ps = DB.prepareStatement(sql, get_TrxName());
			rs = ps.executeQuery();
			if (rs.next()) {
				BigDecimal d = rs.getBigDecimal("disponible");
				return d == null ? BigDecimal.ZERO : d;
			}
			return BigDecimal.ZERO;
		} catch (Exception e) {
			log.log(Level.SEVERE, "getDisponibleRealConLock ReqLine=" + reqLineId
				+ " err=" + e.getMessage(), e);
			return BigDecimal.ZERO;
		} finally {
			closeQuietly(rs);
			closeQuietly(ps);
		}
	}

	// =========================================================================
	// METODOS EXISTENTES (actualizacion de staging)
	// =========================================================================

	private void prepararIOrderB2CAut() {
		//ConexioDBMuro conexion = new ConexioDBMuro();
		ejecutarSQL("DELETE FROM I_OrderB2CAut_Odoo");
		/*try {
			PreparedStatement pst = conexion.conn.prepareStatement("SELECT * FROM ov_ordenesmuro");
			ResultSet res = pst.executeQuery();
			while (res.next()) {
				int id = Integer.parseInt(DB.getSQLValueString(null,
					"Select NEXTIDFUNC(1005415,'N') from c_charge where c_charge_ID=1000010"));
				String description = res.getString("Description") == null ? "" : res.getString("Description");
				String NombreShopify = res.getString("NombreShopify") == null ? "" : res.getString("NombreShopify");
				String DireccionShopify = res.getString("DireccionShopify") == null ? "" : res.getString("DireccionShopify");
				DB.executeUpdate("INSERT INTO I_OrderB2CAut_Odoo "
					+ "(I_OrderB2CAut_ODOO_ID, ODOO_ID, DocumentNo, Description, OV_ORG_ID, OV_BPARTNER_LOCATION_ID,"
					+ " POReference, QtyOrdered, ProductValue, UPC, C_DocType_ID, SalesRep_ID, C_BPartner_ID,"
					+ " C_PaymentTerm_ID, M_Warehouse_ID, M_PriceList_ID, Bill_BPartner_ID, C_Tax_ID,"
					+ " NombreShopify, DireccionShopify, AD_Org_ID, BPartnerValue)"
					+ " VALUES (" + id + ", '" + res.getInt("C_Order_ID") + "', '" + res.getString("DocumentNo") + "',"
					+ " '" + description.replaceAll("'", "") + "', " + res.getInt("AD_Org_ID") + ","
					+ " " + res.getInt("C_BPartner_Location_ID") + ","
					+ " '" + res.getString("POReference") + "', " + res.getBigDecimal("QtyEntered") + ","
					+ " '" + res.getString("Value") + "', '" + res.getString("UPC") + "', 1000030, 1003655,"
					+ " 1001237, 1000010, 1000001, 1000040, 1001237, 1000000,"
					+ " '" + NombreShopify.replaceAll("'", "") + "',"
					+ " '" + DireccionShopify.replaceAll("'", "") + "', 1000000, '76281810')",
					get_TrxName());
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Error prepararIOrderB2CAut: " + e.getMessage(), e);
		}
		try {
			conexion.conn.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Error cerrando conexion Muro: " + e.getMessage(), e);
		}*/
	}

	public int actualizaClient() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut_Odoo "
			+ "SET AD_Client_ID = COALESCE (AD_Client_ID,").append(m_AD_Client_ID).append("),"
			+ " AD_Org_ID = COALESCE (AD_Org_ID,").append(m_AD_Org_ID).append("),"
			+ " IsActive = COALESCE (IsActive, 'Y'),"
			+ " Created = COALESCE (Created, SysDate),"
			+ " CreatedBy = COALESCE (CreatedBy, 0),"
			+ " Updated = COALESCE (Updated, SysDate),"
			+ " UpdatedBy = COALESCE (UpdatedBy, 0),"
			+ " I_ErrorMsg = ' ',"
			+ " I_IsImported = 'N' "
			+ "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaBP() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut_Odoo o "
			+ " SET C_BPartner_ID=(SELECT MAX(C_BPartner_ID) FROM C_BPartner bp"
			+ "                    WHERE trim(o.BPartnerValue)=trim(bp.Value) AND o.AD_Client_ID=bp.AD_Client_ID) "
			+ " WHERE C_BPartner_ID IS NULL AND BPartnerValue IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaBPL() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut_Odoo o"
			+ " SET (BillTo_ID,C_BPartner_Location_ID)=(SELECT max(C_BPartner_Location_ID)C_BPartner_Location_ID,"
			+ "max(C_BPartner_Location_ID)C_BPartner_Location_ID"
			+ " FROM C_BPartner_Location bpl INNER JOIN C_Location l ON (bpl.C_Location_ID=l.C_Location_ID)"
			+ " WHERE o.C_BPartner_ID=bpl.C_BPartner_ID AND bpl.AD_Client_ID=o.AD_Client_ID"
			+ " AND ( trim(upper(o.Address1))=trim(upper(l.Address1)) or trim(upper(o.Address1))=trim(upper(bpl.Name))))"
			+ " WHERE C_BPartner_ID IS NOT NULL AND C_BPartner_Location_ID IS NULL"
			+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaProduct() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut_Odoo o"
			+ " SET M_Product_ID=(SELECT MAX(M_Product_ID) FROM M_Product p"
			+ "                   WHERE trim(o.ProductValue)=trim(p.Value) AND o.AD_Client_ID=p.AD_Client_ID)"
			+ " WHERE ProductValue IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaSalesRep() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut_Odoo"
			+ " SET SalesRep_ID=(SELECT MAX(u.AD_User_ID) FROM ad_user u"
			+ "                  WHERE trim(lower(ChargeName))=trim(lower(u.name)) AND AD_Client_ID=u.AD_Client_ID) "
			+ " WHERE ChargeName IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int beforeImport(String document, String bpvalue) {
		sql = new StringBuffer("UPDATE I_OrderB2CAut_Odoo"
			+ " SET I_IsImported = 'Y' , I_ERRORMSG = 'Orden Importada previamente'"
			+ " WHERE documentno = '" + document + "'"
			+ " AND bpartnervalue = '" + bpvalue + "'");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	private void ejecutarSQL(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return;
		try {
			DB.executeUpdate(sql, get_TrxName());
			log.fine("SQL OK");
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error ejecutando SQL: " + e.getMessage() + " | SQL=" + sql, e);
			if (isSQLRecoverable(e)) {
				m_connectionLost = true;
			}
			throw new RuntimeException(e);
		}
	}

	private void enviarCorreo(List lista, String mensajePie) {
		StringBuffer cuerpoMail = new StringBuffer();
		if (lista.size() > 0) {
			cuerpoMail.append("Se muestran a continuacion las ordenes insertadas en Adempiere");
			cuerpoMail.append("<br />");
			cuerpoMail.append("<br />");
			cuerpoMail.append("<table border=\"1\">\r\n"
				+ "  <tr>\r\n"
				+ "    <th>ID</th>\r\n"
				+ "    <th>Nro Documento</th>\r\n"
				+ "    <th>Total lineas</th>\r\n"
				+ "    <th>Total</th>\r\n"
				+ "    <th>Estado</th>\r\n"
				+ "  </tr>\r\n");
			for (int i = 0; i < lista.size(); i++) {
				MOrder order = (MOrder) lista.get(i);
				MOrderLine[] lines = order.getLines();
				for (int j = 0; j < lines.length; j++) {
					cuerpoMail.append("  <tr>\r\n");
					cuerpoMail.append("    <td>" + order.get_ID() + "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getDocumentNo() + "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getTotalLines() + "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getGrandTotal() + "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getDocStatus() + "</td>\r\n");
					cuerpoMail.append("  </tr>\r\n");
				}
			}
			cuerpoMail.append("</table>");
			cuerpoMail.append("<br />");
			cuerpoMail.append("<br />");
			cuerpoMail.append(mensajePie);
		}
		MClient M_Client = new MClient(Env.getCtx(), null);
		String correoTo = "icastroruz@gmail.com";
		EMail emailObj = M_Client.createEMail(correoTo,
			"Ordenes de Muro inyectadas " + new Timestamp(System.currentTimeMillis()),
			cuerpoMail.toString(), true);
		EMail.SENT_OK.equals(emailObj.send());
	}

	/**
	 * Limpia registros de staging que NO se han convertido en Nota de Venta.
	 */
	private void limpiarStagingNoConvertido(String trxName) {
		log.info("Iniciando limpieza de staging no convertido...");

		String sqlDelLines =
			"DELETE FROM C_ORDERB2CLINEAUT_ODOO l "
			+ "WHERE l.C_Order_ID IS NULL "
			+ "AND EXISTS ( "
			+ "  SELECT 1 FROM C_ORDERB2CAUT_Odoo h "
			+ "  WHERE h.C_ORDERB2CAUT_Odoo_ID = l.C_ORDERB2CAUT_Odoo_ID "
			+ "  AND NVL(h.Processed,'N') <> 'Y' "
			+ ")";
		int delLines = DB.executeUpdate(sqlDelLines, trxName);

		String sqlDelHdr =
			"DELETE FROM C_ORDERB2CAUT_Odoo h "
			+ "WHERE NVL(h.Processed,'N') <> 'Y' "
			+ "AND NOT EXISTS ( "
			+ "  SELECT 1 FROM C_ORDERB2CLINEAUT_ODOO l "
			+ "  WHERE l.C_ORDERB2CAUT_Odoo_ID = h.C_ORDERB2CAUT_Odoo_ID "
			+ "  AND l.C_Order_ID IS NOT NULL "
			+ ")";
		int delHdr = DB.executeUpdate(sqlDelHdr, trxName);

		addLog(0, null, null,
			"Limpieza staging completada. Lineas eliminadas=" + delLines
			+ " Cabeceras eliminadas=" + delHdr);
		log.info("Limpieza staging OK. Lineas=" + delLines + " Cabeceras=" + delHdr);
	}

}