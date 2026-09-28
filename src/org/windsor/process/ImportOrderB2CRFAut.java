/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
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

import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Trx;

/**
 * Import Order from I_Order
 *
 * @author Isaac Castro
 *         <li>BF [ 2936629 ] Error when creating bpartner in the importation order
 *         <li>https://sourceforge.net/tracker/?func=detail&aid=2936629&group_id=176962&atid=879332
 * @author Isaac Castro
 * @version $Id: ImportOrder.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class ImportOrderB2CRFAut extends SvrProcess {

	/** Client to be imported to */
	private int m_AD_Client_ID = 1000000;
	/** Organization to be imported to */
	private int m_AD_Org_ID = 1000000;

	private StringBuffer sql = null;
	private int no = 0;
	private String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;

	// variables de correo (no usado en este proceso actualmente)
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

	/** Effective */
	private Timestamp m_DateValue = null;

	private Integer m_M_WareHouse_ID = 1000001; // Lampa
	private Integer m_M_WareHouse_ab_ID = 1000010; // Abastecimiento

	/** Cierre silencioso solo para Connection externa (Oracle Muro) */
	private void closeConnQuietly(Connection c) {
		try {
			if (c != null)
				c.close();
		} catch (Exception e) {
		}
	}

	class ConexioDBMuro {
		Connection conn;

		public ConexioDBMuro() {
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				String connectionUrl = "jdbc:oracle:thin:@db2358.prolinux.cl:1524:muroerp";
				conn = DriverManager.getConnection(connectionUrl, "adempierem", "adempiere");
				System.out.println("Conectado.");
			} catch (SQLException ex) {
				System.out.println("Error en Conexion Muro ERP. " + ex.toString());
			} catch (ClassNotFoundException e) {
				System.out.println("Error al cargar Driver. " + e.toString());
			}
		}
	}

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
				; // m_deleteOldImported = "Y".equals(para[i].getParameter());
			else if (name.equals("DocAction"))
				; // m_docAction = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (m_DateValue == null)
			m_DateValue = new Timestamp(System.currentTimeMillis());
	}

	protected String doIt() throws java.lang.Exception {

		String menj1 = null, menj2 = null, menj3 = null, menj4 = null, menj5 = null, menj6 = null, menj7 = null;

		prepararIOrderB2CAut();

		// **** Prepare ****
		log.info("Reset=" + actualizaClient());
		log.fine("Set BP from Value=" + actualizaBP());
		log.fine("Set Product from Value=" + actualizaProduct());
		log.fine("Set SalesRep_ID=" + actualizaSalesRep());
		commitEx();

		ArrayList<String> ordenescompra = new ArrayList<String>();
		List<MOrder> listaOrder = new ArrayList<MOrder>();
		StringBuffer mensajeCorreo = new StringBuffer();

		String slqoc = "Select Documentno, BPartnerValue from I_OrderB2CAut where i_isImported<>'Y' and C_BPartner_ID=1001237 "
				+ clientCheck + " Group by DocumentNo, BPartnerValue";

		// obtener ordenes para importar
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(slqoc, null);
			rs = pstmt.executeQuery();
			while (rs.next()) {

				String sqlcreados = "Select count(o.DocumentNO) as cuenta from I_OrderB2CAut o  where o.i_isImported<>'Y' "
						+ " and o.bpartnervalue='76281810' and o.documentno in (select b2.poreference from C_ORDERB2CAUT b2 "
						+ " where o.documentno=b2.poreference and b2.bpartnerValue=o.bpartnervalue) "
						+ " and o.documentno='" + rs.getString("DocumentNo") + "'";

				PreparedStatement pstmtc = null;
				ResultSet rsc = null;
				try {
					pstmtc = DB.prepareStatement(sqlcreados, null);
					rsc = pstmtc.executeQuery();

					if (rsc.next()) {
						if (rsc.getInt("cuenta") == 0) {
							ordenescompra.add(rs.getString("DocumentNo"));
						} else {
							log.fine("Set SalesRep_ID=" + beforeImport(rs.getString("DocumentNo"), rs.getString("BPartnerValue")));
						}
					}
				} catch (Exception e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					DB.close(rsc, pstmtc);
					rsc = null;
					pstmtc = null;
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		// fin obtener ordenes para importar

		int docok = 0;
		int docnotok = 0;

		if (!ordenescompra.isEmpty()) {
			for (int i = 0; i < ordenescompra.size(); i++) {

				System.out.println("recorre " + ordenescompra.get(i));

				String recorre = "Select documentno, COALESCE(c_bpartner_ID,0) C_BPartner_ID, COALESCE(1011338,0)c_bpartner_location_ID,"
						+ "	 COALESCE(M_product_ID, 0) M_product_ID,"
						+ " replace(description,'/','-') description, name as tipogt, ContactName formacgt,"
						+ " trim(upper(Address2)) as subtienda, ChargeName as vendedor, Coalesce (SalesRep_ID,0) SalesRep_ID , "
						+ " LineDescription as Generico, COALESCE(QtyOrdered,0) as Cantidad, COALESCE(PriceActual,0) Precio, to_char(Created,'yyyy-mm-dd')Created, ProductValue, BPartnerValue, Address1, DOCTYPENAME, POReference,"
						+ " Address1, ov_org_id, C_ORDERMURO_id, NOMBRESHOPIFY, DIRECCIONSHOPIFY, CreatedBy "
						+ " from  I_OrderB2CAut where bpartnervalue='76281810' and documentNo='" + ordenescompra.get(i) + "'";

				int errorp = 0;
				int errorbp = 0;
				int errorbpl = 0;
				int erroruser = 0;
				int errorst = 0;
				int errorsl = 0;

				/*
				 * Tabla de errores
				 * 0: sin errores
				 * 1: sin socio de negocio
				 * 2: sin direccion
				 * 3: sin producto
				 * 4: sin producto ni generico
				 * 5: sin stock
				 * 6: sin vendedor
				 * 7: producto ingresado no existe
				 * 8: Error subtienda
				 * 9: Error sin lineas
				 * 10: una o mas lineas que no pasaron
				 */
				Integer C_BPartner_ID = 0;
				Integer C_BPartner_Location_ID = 0;
				String documentno = "";
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
				Integer C_OrderMuro_ID = 0;
				String NOMBRESHOPIFY = "";
				String DIRECCIONSHOPIFY = "";
				String POReference = "";

				PreparedStatement pstmtRec = null;
				ResultSet rsRec = null;

				try {
					pstmtRec = DB.prepareStatement(recorre, null);
					rsRec = pstmtRec.executeQuery();

					int lineas = 1;
					int b2c_id = 0;

					while (rsRec.next()) {

						boolean isInsertLine = false;

						// Asignacion de valores
						Integer Cantidad = rsRec.getInt("Cantidad");
						C_BPartner_ID = rsRec.getInt("C_BPartner_ID");
						String BPartnerValue = rsRec.getString("BPartnerValue");
						C_BPartner_Location_ID = rsRec.getInt("C_BPartner_Location_ID");
						String Address1 = rsRec.getString("Address1");
						Integer M_Product_ID = rsRec.getInt("M_Product_ID");
						String Generico = rsRec.getString("Generico");
						description = rsRec.getString("description");
						documentno = rsRec.getString("documentno");
						Created = rsRec.getString("Created");
						SalesRep_ID = rsRec.getInt("SalesRep_ID");
						String vendedor = rsRec.getString("vendedor");
						String subtienda = rsRec.getString("subtienda");
						Integer Precio = rsRec.getInt("Precio");
						String ProductValue = rsRec.getString("ProductValue");
						String DOCTYPENAME = rsRec.getString("DOCTYPENAME");
						String VentaEnVerde = "";
						String Processed = "";
						DateAcct = rsRec.getString("Created");
						String ErrorMsgCab = "";
						C_PaymentTerm_ID = DB.getSQLValue(null, "SELECT MAX(C_PaymentTerm_ID) FROM C_BPartner WHERE C_BPartner_ID = " + C_BPartner_ID);
						C_OrderMuro_ID = rsRec.getInt("C_OrderMuro_ID");
						NOMBRESHOPIFY = rsRec.getString("NOMBRESHOPIFY") == null ? "" : rsRec.getString("NOMBRESHOPIFY");
						DIRECCIONSHOPIFY = rsRec.getString("DIRECCIONSHOPIFY") == null ? "" : rsRec.getString("DIRECCIONSHOPIFY");
						POReference = rsRec.getString("POReference");

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
							if (M_Product_ID == 0 && (Generico == null | Generico.equals(""))) {
								errorp = 4;
								menj4 = "Sin Codigo Windsor y sin Sku cliente";
							}
						}
						if (SalesRep_ID == 0) {
							erroruser = 6;
							menj6 = "Venedor no encontrado:" + vendedor;
						}

						if (errorbp != 0) {
							ErrorMsgCab = menj1;
						} else if (errorbpl != 0) {
							ErrorMsgCab = menj2;
						} else if (erroruser != 0) {
							ErrorMsgCab = menj6;
						}

						if (b2c_id == 0) {
							// cabecera
							b2c_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1005416,'N') from c_charge where c_charge_ID=1000010"));
							String fc = "";

							mc = "Internet";
							VentaEnVerde = "N";
							FormaCompra = fc;
							Processed = "N";

							// subtienda
							if (C_BPartner_ID > 0 && subtienda != null) {
								String sqlst = "Select count (*) as cuenta from C_BPartner_SubTienda where c_Bpartner_ID=" + C_BPartner_ID
										+ " and trim(upper(name))= trim(upper('" + subtienda + "'))";

								PreparedStatement pstmtst = null;
								ResultSet rsst = null;
								try {
									pstmtst = DB.prepareStatement(sqlst, null);
									rsst = pstmtst.executeQuery();

									if (rsst.next()) {
										if (rsst.getInt("cuenta") > 0) {
											String sqlstv = "Select max( C_BPartner_SubTienda_ID)as C_BPartner_SubTienda_ID  from C_BPartner_SubTienda where c_Bpartner_ID="
													+ C_BPartner_ID + " and trim(upper(name))= trim(upper('" + subtienda + "'))";

											PreparedStatement pstmtstv = null;
											ResultSet rsstv = null;
											try {
												pstmtstv = DB.prepareStatement(sqlstv, null);
												rsstv = pstmtstv.executeQuery();
												if (rsstv.next())
													C_BPartner_SubTienda_ID = rsstv.getInt("C_BPartner_SubTienda_ID");
											} catch (Exception e) {
												log.log(Level.SEVERE, e.getMessage(), e);
											} finally {
												DB.close(rsstv, pstmtstv);
												rsstv = null;
												pstmtstv = null;
											}
										} else {
											errorst = 8;
										}
									}
								} catch (Exception e) {
									log.log(Level.SEVERE, e.getMessage(), e);
								} finally {
									DB.close(rsst, pstmtst);
									rsst = null;
									pstmtst = null;
								}
							} // subtienda

							String insert = "INSERT INTO C_ORDERB2CAUT (C_ORDERB2CAUT_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,C_BPartner_ID,C_BPartner_Location_ID,POReference,DocumentNo,DateAcct,"
									+ "Processed,BPartnerValue,Address1,M_WAREHOUSE_ID,SalesRep_ID,M_PriceList_ID,"
									+ "Description,MedioCompra,VentaEnVerde,FormaCompra,C_BPartner_SubTienda_ID,ErrorMsg, C_OrderMuro_ID, DocumentoMuro, NOMBRESHOPIFY, DIRECCIONSHOPIFY) "
									+ "VALUES "
									+ "(" + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + C_BPartner_ID + "," + C_BPartner_Location_ID
									+ ",'" + POReference + "',(Select NEXTIDFUNC(1004942,'N') from c_charge where c_charge_ID=1000010),to_date('" + Created + "','yyyy-mm-dd'),"
									+ "'" + Processed + "','" + BPartnerValue + "'," + (Address1 == null ? "null" : "'" + Address1 + "'") + "," + M_Warehouse_ID + "," + SalesRep_ID
									+ ",1000000,'" + description + "','" + mc + "','" + VentaEnVerde + "','" + FormaCompra + "'," + (C_BPartner_SubTienda_ID == 0 ? "null" : C_BPartner_SubTienda_ID)
									+ ",'" + ErrorMsgCab + "', " + C_OrderMuro_ID + ", '" + documentno + "', '" + NOMBRESHOPIFY.replaceAll("'", "") + "', '" + DIRECCIONSHOPIFY.replaceAll("'", "") + "')";
							ejecutarSQL(insert);
						} // cabecera

						BigDecimal sob = new BigDecimal(0);

						if (b2c_id != 0) {
							int salto = 0;
							int obl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1005417,'N') from c_charge where c_charge_ID=1000010"));
							String pasarAOV = "";
							String ErrorMsg = "";

							if (errorp > 0) {
								if (errorp == 3)
									ErrorMsg = menj3;
								else if (errorp == 4)
									ErrorMsg = menj4;
							}

							log.finer("M_product_ID=" + M_Product_ID);
							BigDecimal cant = new BigDecimal(Cantidad);

							String sqlp = "Select count(*) as cuenta from m_product where m_product_ID=" + M_Product_ID;

							PreparedStatement pstmtp = null;
							ResultSet rsp = null;
							try {
								pstmtp = DB.prepareStatement(sqlp, null);
								rsp = pstmtp.executeQuery();

								if (rsp.next()) {
									if (rsp.getInt("cuenta") == 1) {

										// Reserva e-commerce
										String sqlqr = "select count(1) encontrado "
												+ " from m_requisition r "
												+ " where r.docstatus='CO' and r.c_doctype_ID=1000570 "
												+ " and r.c_bpartner_ID=" + C_BPartner_ID
												+ "and "
												+ " r.M_RequisitionRef_ID in "
												+ " (select r2.m_requisition_ID "
												+ " from m_requisition r2 "
												+ " inner join m_requisitionline rl on (r2.m_requisition_ID=rl.m_Requisition_ID) "
												+ " where r2.docstatus='CO' and rl.m_product_ID=" + M_Product_ID
												+ " and (rl.qtyreserved)>0 and r2.c_doctype_ID=1000569 and rl.liberada='N' )";

										PreparedStatement pstmtre = null;
										ResultSet rsre = null;
										try {
											pstmtre = DB.prepareStatement(sqlqr, null);
											rsre = pstmtre.executeQuery();
											if (rsre.next()) {
												log.fine("ID Codigo: " + M_Product_ID + "En Reserva Eccomerce: " + rsre.getInt("encontrado"));
												if (rsre.getInt("encontrado") > 0) {

													String sqlqrq = " select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "
															+ " from m_Requisitionline rl "
															+ " inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "
															+ " where r.docstatus='CO'  "
															+ " and r.c_doctype_id=1000569 "
															+ " and rl.liberada='N' "
															+ " and rl.m_product_ID =" + M_Product_ID
															+ " and exists  "
															+ " (select * "
															+ " from m_requisition r2 "
															+ " where R2.M_REQUISITIONREF_ID=r.m_requisition_ID "
															+ "  and r2.c_doctype_ID=1000570 "
															+ " and r2.docstatus='CO' "
															+ " and r2.c_bpartner_ID=" + C_BPartner_ID + " ) ";

													PreparedStatement pstmtreq = null;
													ResultSet rsreq = null;
													try {
														pstmtreq = DB.prepareStatement(sqlqrq, null);
														rsreq = pstmtreq.executeQuery();
														if (rsreq.next()) {
															log.fine("ID Codigo: " + M_Product_ID + "En Reserva Eccomerce QtyR: " + rsreq.getInt("qtyreserved") + " Pedido: " + Cantidad);

															if (Cantidad <= rsreq.getInt("qtyreserved")) {
																BigDecimal neto = new BigDecimal(Cantidad * Precio);

																String insertLine = "INSERT INTO C_ORDERB2CLINEAUT (C_ORDERB2CLINEAUT_ID,C_ORDERB2CAUT_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																		+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																		+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + ","
																		+ cant + "," + neto + ",'" + ProductValue + "',"
																		+ (DOCTYPENAME == null ? "null" : "'" + DOCTYPENAME + "'") + "," + M_Product_ID + ",'Y'," + Precio + ","
																		+ (Generico == null ? "null" : "'" + Generico + "'") + "," + rsreq.getInt("M_RequisitionLine_ID") + ")";
																ejecutarSQL(insertLine);
																isInsertLine = true;
																cant = new BigDecimal(0);
																salto = 1;

															} else if (Cantidad > rsreq.getInt("qtyreserved")) {
																BigDecimal aux = new BigDecimal(rsreq.getInt("qtyreserved"));
																BigDecimal neto = new BigDecimal(rsreq.getInt("qtyreserved") * Precio);

																String insertLine = "INSERT INTO C_ORDERB2CLINEAUT (C_ORDERB2CLINEAUT_ID,C_ORDERB2CAUT_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																		+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																		+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + ","
																		+ aux + "," + neto + ",'" + ProductValue + "',"
																		+ (DOCTYPENAME == null ? "null" : "'" + DOCTYPENAME + "'") + "," + M_Product_ID + ",'Y'," + Precio + ","
																		+ (Generico == null ? "null" : "'" + Generico + "'") + "," + rsreq.getInt("M_RequisitionLine_ID") + ")";
																ejecutarSQL(insertLine);
																isInsertLine = true;
																cant = new BigDecimal(cant.intValue() - aux.intValue());
																salto = 2;
															}
															log.fine("Salto: " + salto + " ID Codigo: " + M_Product_ID);
														}
													} catch (Exception e) {
														log.log(Level.SEVERE, e.getMessage(), e);
													} finally {
														DB.close(rsreq, pstmtreq);
														rsreq = null;
														pstmtreq = null;
													}
												}
											}
										} catch (Exception e) {
											log.log(Level.SEVERE, e.getMessage(), e);
										} finally {
											DB.close(rsre, pstmtre);
											rsre = null;
											pstmtre = null;
										}

										// ====== desde aqu�, tu l�gica original sigue igual ======
										// (solo se ajustaron cierres con DB.close(rs, ps) donde corresponde)

										if (salto == 0 || salto == 2) {
											// reserva ecommerce no cubre todo sacar de la fisica
											log.info("Producto_ID:" + M_Product_ID);
											log.info("BPartner_ID:" + C_BPartner_ID);

											String sqlrfn = " select count(1) encontrado "
													+ " from m_requisition r  "
													+ " inner join m_requisitionline rl on (r.m_requisition_ID=rl.m_Requisition_ID) "
													+ " where r.docstatus='CO' and r.c_doctype_ID=1000111 "
													+ " and r.c_bpartner_ID=" + C_BPartner_ID
													+ " and (r.c_bpartner_location_ID=" + C_BPartner_Location_ID
													+ " or R.OVERWRITEREQUISITION='Y') "
													+ " and  "
													+ " rl.m_product_ID=" + M_Product_ID
													+ " and (rl.qtyreserved)>0  "
													+ " and  rl.LIBERADA='N' ";

											PreparedStatement pstmtrfn = null;
											ResultSet rsrfn = null;
											try {
												pstmtrfn = DB.prepareStatement(sqlrfn, null);
												rsrfn = pstmtrfn.executeQuery();
												if (rsrfn.next()) {
													log.fine("ID Codigo: " + M_Product_ID + "En Reserva Fisica: " + rsrfn.getInt("encontrado"));
													if (rsrfn.getInt("encontrado") > 0) {
														if (salto == 2) {
															lineas++;
															obl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1005417,'N') from c_charge where c_charge_ID=1000010"));
														}

														String sqlrn = " select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "
																+ " from m_Requisitionline rl "
																+ " inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "
																+ " where r.docstatus='CO'  "
																+ " and r.c_doctype_id=1000111 "
																+ " and rl.m_product_ID =" + M_Product_ID
																+ " and r.c_bpartner_ID=" + C_BPartner_ID
																+ " and (r.c_bpartner_location_ID=" + C_BPartner_Location_ID
																+ " or R.OVERWRITEREQUISITION='Y') "
																+ " and (rl.qtyreserved)>0"
																+ " and  rl.LIBERADA='N' ";

														PreparedStatement pstmtrn = null;
														ResultSet rsrn = null;
														try {
															pstmtrn = DB.prepareStatement(sqlrn, null);
															rsrn = pstmtrn.executeQuery();
															if (rsrn.next()) {
																log.fine("ID Codigo: " + M_Product_ID + "En Reserva Eccomerce QtyR: " + rsrn.getInt("qtyreserved") + " Pedido: " + Cantidad);
																if (Cantidad <= rsrn.getInt("qtyreserved")) {
																	BigDecimal neto = new BigDecimal(cant.intValue() * Precio);
																	String insertLine = "INSERT INTO C_ORDERB2CLINEAUT (C_ORDERB2CLINEAUT_ID,C_ORDERB2CAUT_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																			+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																			+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + ","
																			+ cant + "," + neto + ",'" + ProductValue + "',"
																			+ "'" + DOCTYPENAME + "'," + M_Product_ID + ",'Y'," + Precio + ",'" + Generico + "'," + rsrn.getInt("M_RequisitionLine_ID") + ")";
																	ejecutarSQL(insertLine);
																	isInsertLine = true;
																	cant = new BigDecimal(0);
																	salto = 3;
																} else if (Cantidad > rsrn.getInt("qtyreserved")) {
																	BigDecimal aux = new BigDecimal(rsrn.getInt("qtyreserved"));
																	BigDecimal neto = new BigDecimal(rsrn.getInt("qtyreserved") * Precio);
																	String insertLine = "INSERT INTO C_ORDERB2CLINEAUT (C_ORDERB2CLINEAUT_ID,C_ORDERB2CAUT_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																			+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																			+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + ","
																			+ aux + "," + neto + ",'" + ProductValue + "',"
																			+ "'" + DOCTYPENAME + "'," + M_Product_ID + ",'Y'," + Precio + ",'" + Generico + "'," + rsrn.getInt("M_RequisitionLine_ID") + ")";
																	ejecutarSQL(insertLine);
																	isInsertLine = true;
																	cant = new BigDecimal(cant.intValue() - aux.intValue());
																	salto = 4;
																}
																log.finer("Salto: " + salto + " ID Codigo: " + M_Product_ID);
															}
														} catch (Exception e) {
															log.log(Level.SEVERE, e.getMessage(), e);
														} finally {
															DB.close(rsrn, pstmtrn);
															rsrn = null;
															pstmtrn = null;
														}
													}
												}
											} catch (Exception e) {
												log.log(Level.SEVERE, e.getMessage(), e);
											} finally {
												DB.close(rsrfn, pstmtrfn);
												rsrfn = null;
												pstmtrfn = null;
											}
										}

										if (salto == 0 || salto == 4 || salto == 2) {
											if (salto == 4 || salto == 2) {
												obl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1005417,'N') from c_charge where c_charge_ID=1000010"));
												lineas++;
											}

											String sqlps = "Select "
													+ " COALESCE ( "
													+ "         (SELECT SUM (s.qtyonhand) "
													+ "           FROM rv_storage380 s "
													+ "         WHERE     s.M_Product_ID = p.m_product_id "
													+ "              AND s.m_warehouse_id IN (1000001, 1000010,1000033) "
													+ "             AND s.isactive = 'Y'), "
													+ "    0) "
													+ " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "
													+ "         FROM C_orderline ol2      "
													+ "             INNER JOIN C_Order o2  "
													+ "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "
													+ "     WHERE     ol2.M_Product_ID = p.m_product_id "
													+ "          AND o2.m_warehouse_id = 1000001 "
													+ "         AND o2.saldada <> 'Y' "
													+ "        AND o2.docstatus IN ('IP', 'CO', 'CL') "
													+ "       AND o2.issotrx = 'Y' "
													+ "      AND o2.c_doctypetarget_ID NOT IN "
													+ "            (1000110, 1000048, 1000568)) "
													+ " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "
													+ "     FROM M_Requisitionline rl     "
													+ "         INNER JOIN M_Requisition r  "
													+ "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "
													+ " WHERE     rl.M_Product_ID = p.m_product_id  "
													+ "      AND r.m_warehouse_id = 1000001  "
													+ "     AND r.docstatus IN ('CO', 'CL')  "
													+ "    AND r.issotrx = 'Y'))  "
													+ " as Disponible, p.ProductType,  "
													+ " COALESCE ( "
													+ "         (SELECT SUM (s.qtyonhand) "
													+ "           FROM rv_storage s "
													+ "         WHERE     s.M_Product_ID = p.m_product_id "
													+ "              AND s.m_warehouse_id IN (1000024, 1000025) "
													+ "             AND s.isactive = 'Y'), "
													+ "    0) "
													+ " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "
													+ "         FROM C_orderline ol2      "
													+ "             INNER JOIN C_Order o2  "
													+ "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "
													+ "     WHERE     ol2.M_Product_ID = p.m_product_id "
													+ "          AND o2.m_warehouse_id in ( 1000024,1000025) "
													+ "         AND o2.saldada <> 'Y' "
													+ "        AND o2.docstatus IN ('IP', 'CO', 'CL') "
													+ "       AND o2.issotrx = 'Y' "
													+ "      AND o2.c_doctypetarget_ID NOT IN "
													+ "            (1000110, 1000048, 1000568)) "
													+ " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "
													+ "     FROM M_Requisitionline rl     "
													+ "         INNER JOIN M_Requisition r  "
													+ "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "
													+ " WHERE     rl.M_Product_ID = p.m_product_id  "
													+ "      AND r.m_warehouse_id in ( 1000024,1000025)  "
													+ "     AND r.docstatus IN ('CO', 'CL')  "
													+ "    AND r.issotrx = 'Y'))  "
													+ " as OtroDisponible"
													+ " from M_product p where  p.m_product_ID=" + M_Product_ID;

											PreparedStatement pstmtps = null;
											ResultSet rsps = null;
											try {
												pstmtps = DB.prepareStatement(sqlps, null);
												rsps = pstmtps.executeQuery();
												if (rsps.next()) {
													log.fine("ID Codigo: " + M_Product_ID + "Disponible : " + rsps.getInt("Disponible") + " Pedido: " + Cantidad);
													if (rsps.getString("ProductType").equals("I")) {
														if (cant.intValue() <= rsps.getInt("Disponible")) {
															BigDecimal neto = new BigDecimal(cant.intValue() * Precio);
															String insertLine = "INSERT INTO C_ORDERB2CLINEAUT (C_ORDERB2CLINEAUT_ID,C_ORDERB2CAUT_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																	+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO) "
																	+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + ","
																	+ cant + "," + neto + ",'" + ProductValue + "',"
																	+ "'" + DOCTYPENAME + "'," + M_Product_ID + ",'Y'," + Precio + ",'" + Generico + "')";
															ejecutarSQL(insertLine);
															isInsertLine = true;
															salto = 5;
														} else {
															errorp = 5;
															salto = 6;
															pasarAOV = "N";
															sob = new BigDecimal(rsps.getInt("OtroDisponible"));
															ErrorMsg = "No hay stock en la bodega actual, verificar en otras bodegas";
															mensajeCorreo.append("Pedido " + documentno + " Producto " + ProductValue + " No hay stock en la bodega actual, verificar en otras bodegas");
															mensajeCorreo.append("<br />");
														}
													} else {
														pasarAOV = "Y";
													}
												}
												log.fine("Salto: " + salto);
											} catch (Exception e) {
												log.log(Level.SEVERE, e.getMessage(), e);
											} finally {
												DB.close(rsps, pstmtps);
												rsps = null;
												pstmtps = null;
											}
										}

									} else {
										errorp = 7;
										pasarAOV = "N";
										ErrorMsg = "Error: Codigo Windsor ingresadp no Existe.";
									}
								}

							} catch (Exception e) {
								log.log(Level.SEVERE, e.getMessage(), e);
							} finally {
								DB.close(rsp, pstmtp);
								rsp = null;
								pstmtp = null;
							}

							if (errorp > 0) {
								if (errorp == 5) {
									docnotok++;
								}
							}

							if (errorst == 8) {
								ErrorMsg = "Subtienda no existe:" + subtienda;
							}

							if (!isInsertLine) {
								BigDecimal neto = new BigDecimal(Cantidad * Precio);
								String insertLine = "INSERT INTO C_ORDERB2CLINEAUT (C_ORDERB2CLINEAUT_ID,C_ORDERB2CAUT_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
										+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,StockBodegas,ErrorMsg) "
										+ "VALUES (" + obl_id + "," + b2c_id + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + (lineas * 10) + ","
										+ Cantidad + "," + neto + ",'" + ProductValue + "',"
										+ "'" + DOCTYPENAME + "'," + M_Product_ID + ",'" + pasarAOV + "'," + Precio + ",'" + Generico + "'," + sob + ",'" + ErrorMsg + "')";
								ejecutarSQL(insertLine);
							}
						}

						lineas++;
					} // while productos

					// Crear nota de venta sin errores
					if (errorp + errorbp + errorbpl + erroruser + errorst == 0) {

						MOrder order = null;
						int orderId = 0;
						int contador = 1;
						int documentNo = 1;

						// validar que todas las lineas esten okey y que hay lineas
						String sqlvl = "Select count(*) cuenta from C_ORDERB2CLINEAUT where C_ORDERB2CAUT_ID=" + b2c_id;

						PreparedStatement pstmtvl = null;
						ResultSet rsvl = null;
						try {
							pstmtvl = DB.prepareStatement(sqlvl, null);
							rsvl = pstmtvl.executeQuery();

							if (rsvl.next()) {
								if (rsvl.getInt("cuenta") > 0) {

									String sqlvlok = "Select count(*) cuenta from C_ORDERB2CLINEAUT where PASARAOV<>'Y' and C_ORDERB2CAUT_ID=" + b2c_id;

									PreparedStatement pstmtvlok = null;
									ResultSet rsvlok = null;
									try {
										pstmtvlok = DB.prepareStatement(sqlvlok, null);
										rsvlok = pstmtvlok.executeQuery();

										if (rsvlok.next()) {
											if (rsvlok.getInt("cuenta") == 0) {

												String sqllines = "Select ol.*, COALESCE(mp.C_UOM_ID,100) AS C_UOM_ID "
														+ "from C_ORDERB2CLINEAUT ol "
														+ "LEFT JOIN M_PRODUCT mp ON ol.M_PRODUCT_ID = mp.M_PRODUCT_ID "
														+ "WHERE C_ORDERB2CAUT_ID = " + b2c_id;

												PreparedStatement pstmtlines = null;
												ResultSet rslines = null;

												// ==========
												// Trx �NICA por documento (C_Order + l�neas + updates)
												// ==========
												Trx trxDoc = null;

												try {
													pstmtlines = DB.prepareStatement(sqllines, null);
													rslines = pstmtlines.executeQuery();

													trxDoc = Trx.get(Trx.createTrxName("B2C_" + documentno), true);
													trxDoc.start();

													while (rslines.next()) {
														if (orderId == 0) {
															orderId = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(232,'N') from c_charge where c_charge_ID=1000010"));

															MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, null);
															int C_DocTypeTarget_ID = 1000030;

															String documentNoSeq = "";
															if (documentNo > 1)
																documentNoSeq = documentno + "-" + documentNo;
															else
																documentNoSeq = documentno;

															String insertOrder = "INSERT INTO C_Order (C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,C_DocTypeTarget_ID,IsSOTrx,DeliveryRule,"
																	+ "C_BPartner_ID,C_BPartner_Location_ID,POReference,AD_User_ID,Bill_BPartner_ID,Bill_Location_ID,Description,"
																	+ "C_PaymentTerm_ID,M_PriceList_ID,M_Warehouse_ID,SalesRep_ID,DateOrdered,DateAcct,InvoiceRule,"
																	+ "DOCUMENTOMURO,VENTAINVIERNO,DocumentNo,DOCSTATUS,DOCACTION,C_DOCTYPE_ID,C_CURRENCY_ID,"
																	+ "PAYMENTRULE,FREIGHTCOSTRULE,DELIVERYVIARULE,PRIORITYRULE,DatePromised,C_BPartner_SubTienda_ID,FIRMA2,FIRMA3,FIRMACOM,FIRMAFIN,"
																	+ "USERFIRMCOM,FormaCompra,MedioCompra, C_OrderMuro_ID) "
																	+ "VALUES (" + orderId + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + "," + C_DocTypeTarget_ID + ",'Y','O',"
																	+ C_BPartner_ID + "," + C_BPartner_Location_ID + ",'" + POReference + "'," + SalesRep_ID + "," + C_BPartner_ID + "," + C_BPartner_Location_ID + ",'"
																	+ description + "'," + C_PaymentTerm_ID + "," + M_PriceList_ID + "," + M_Warehouse_ID + "," + SalesRep_ID + ",to_date('" + DateAcct
																	+ "','yyyy-mm-dd'),to_date('" + DateAcct + "','yyyy-mm-dd'),'D',"
																	+ "'" + documentno + "','N','" + documentNoSeq + "','DR','CO',1000030,228,"
																	+ "'P','I','P','5',to_date('" + Created + "','yyyy-mm-dd')," + C_BPartner_SubTienda_ID + ","
																	+ "'Y','Y',to_date('" + DateAcct + "','yyyy-mm-dd'),to_date('" + DateAcct + "','yyyy-mm-dd'),"
																	+ 1003655 + ",'" + FormaCompra + "','" + mc + "', " + C_OrderMuro_ID + ")";

															ejecutarSQL(insertOrder, trxDoc);
														}

														if (contador <= 25) {
															int orderLineId = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(233,'N') from c_charge where c_charge_ID=1000010"));

															String insertOrderLine = "INSERT INTO C_OrderLine (C_OrderLine_ID,C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,M_Product_ID,PriceEntered,PriceActual,PriceList,"
																	+ "QtyEntered,Line,Qty,Demand,Discount,Discount2,Discount3,Discount4,Discount5,NotPrint,LineNetAmt,DATEORDERED,M_WAREHOUSE_ID,C_UOM_ID,"
																	+ "C_CURRENCY_ID,C_TAX_ID,M_RequisitionLine_ID,C_BPARTNER_ID,C_BPARTNER_LOCATION_ID,DATEPROMISED) "
																	+ "VALUES (" + orderLineId + "," + orderId + "," + m_AD_Client_ID + "," + m_AD_Org_ID + "," + CreatedBy + "," + CreatedBy + ","
																	+ rslines.getInt("M_Product_ID") + "," + rslines.getBigDecimal("priceentered") + "," + rslines.getBigDecimal("priceentered") + "," + rslines.getBigDecimal("priceentered") + ","
																	+ rslines.getBigDecimal("qtyentered") + "," + (contador * 10) + "," + rslines.getBigDecimal("qtyentered") + "," + rslines.getBigDecimal("qtyentered")
																	+ ",0,0,0,0,0,'N',"
																	+ (rslines.getBigDecimal("priceentered").multiply(rslines.getBigDecimal("qtyentered")).setScale(0, RoundingMode.HALF_UP))
																	+ ",to_date('" + Created + "','yyyy-mm-dd')," + M_Warehouse_ID + "," + rslines.getBigDecimal("C_UOM_ID") + ","
																	+ "228,1000000," + rslines.getBigDecimal("M_RequisitionLine_ID") + "," + C_BPartner_ID + "," + C_BPartner_Location_ID + ",to_date('" + Created + "','yyyy-mm-dd'))";

															ejecutarSQL(insertOrderLine, trxDoc);
															contador++;

															// FIX 2026-07-20 (FG): UPDATE de QtyUsed DESACTIVADO (mismo fix que
															// ImportOrderB2CRFAutOdoo 2026-06-09). Escribia solo QtyUsed con formula
															// SUM(QtyEntered DR/IP/CO/CL/IN), divergente del ModelValidator
															// ModWindsorUpdateReserved (SUM QtyDelivered CO + QtyReserved CO/IP)
															// y sin ajustar QtyReserved/Qty -> dejaba disponible negativo.
															// El validator AFTER_COMPLETE de la OV es la unica fuente de verdad.
															// if (rslines.getBigDecimal("M_RequisitionLine_ID") != null) {
															//	String sqlUpRL = "SELECT SUM(QTYENTERED) "
															//			+ " FROM C_OrderLine col"
															//			+ " INNER JOIN C_Order co ON (col.C_Order_ID = co.C_Order_ID)"
															//			+ " WHERE M_RequisitionLine_ID = " + rslines.getBigDecimal("M_RequisitionLine_ID") + " AND co.DocStatus IN ('DR','IP','CO','CL','IN')";
															//	BigDecimal amt = DB.getSQLValueBD(trxDoc.getTrxName(), sqlUpRL);
															//	if (amt == null)
															//		amt = Env.ZERO;
															//
															//	String update = "UPDATE M_RequisitionLine"
															//			+ " SET QtyUsed = " + amt
															//			+ " WHERE M_RequisitionLine_ID = " + rslines.getBigDecimal("M_RequisitionLine_ID");
															//	ejecutarSQL(update, trxDoc);
															// }

															String updateOrderB2CLine = "UPDATE C_ORDERB2CLINEAUT"
																	+ " SET C_Order_ID = " + orderId + ", Processed = 'Y', c_orderline_id = " + orderLineId
																	+ " WHERE C_ORDERB2CLINEAUT_ID = " + rslines.getBigDecimal("C_ORDERB2CLINEAUT_ID");
															ejecutarSQL(updateOrderB2CLine, trxDoc);
														}

														if (contador == 26) {
															order = new MOrder(getCtx(), orderId, trxDoc.getTrxName());
															order.calculateTaxTotal();
															order.saveEx();
															if (!order.getDocStatus().equalsIgnoreCase("CO")) {
																System.out.println("Completar 1 " + order.getDocumentNo());
																order.setDocAction("CO");
																if (!order.processIt("CO")) {
																	System.out.println("Error al completar 1 Documento " + order.getDocumentNo());
																} else {
																	order.save();
																}
															}
															docok++;
															contador = 1;
															order = null;
															orderId = 0;
															documentNo++;
														}
													} // while l�neas

													// Completa el �ltimo documento abierto
													order = new MOrder(getCtx(), orderId, trxDoc.getTrxName());
													order.calculateTaxTotal();
													order.saveEx();
													if (!order.getDocStatus().equalsIgnoreCase("CO")) {
														System.out.println("Completar 2 " + order.getDocumentNo());
														order.setDocAction("CO");
														if (!order.processIt("CO")) {
															System.out.println("Error al completar 2 Documento " + order.getDocumentNo());
														} else {
															order.save();
														}
													}

													listaOrder.add(order);

													String update = "UPDATE C_ORDERB2CAUT SET Processed = 'Y' WHERE C_ORDERB2CAUT_ID = " + b2c_id;
													ejecutarSQL(update, trxDoc);

													sql = new StringBuffer("UPDATE I_OrderB2CAut "
															+ "SET I_IsImported='Y' , processed='Y' "
															+ "WHERE Documentno='" + documentno + "'");
													DB.executeUpdate(sql.toString(), trxDoc.getTrxName());

													trxDoc.commit();

													contador = 1;
													docok++;
													order = null;
													documentNo++;

												} catch (Exception e) {
													if (trxDoc != null)
														trxDoc.rollback();
													log.log(Level.SEVERE, e.getMessage(), e);
												} finally {
													if (trxDoc != null)
														trxDoc.close();

													DB.close(rslines, pstmtlines);
													rslines = null;
													pstmtlines = null;
												}

											} else {
												errorsl = 10;
											}
										}

									} catch (Exception e) {
										log.log(Level.SEVERE, e.getMessage(), e);
									} finally {
										DB.close(rsvlok, pstmtvlok);
										rsvlok = null;
										pstmtvlok = null;
									}
								} else {
									errorsl = 9;
								}
							}

						} catch (Exception e) {
							log.log(Level.SEVERE, e.getMessage(), e);
						} finally {
							DB.close(rsvl, pstmtvl);
							rsvl = null;
							pstmtvl = null;
						}

					} else {
						docnotok++;
						commitEx();
					}

				} catch (Exception e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					DB.close(rsRec, pstmtRec);
					rsRec = null;
					pstmtRec = null;
				}
			}
		}

		// enviarCorreo(listaOrder, mensajeCorreo.toString());

		return "Proceso terminado - Ordenes Creadad:" + docok + " - Ordenes con Error: " + docnotok;
	}

	private void prepararIOrderB2CAut() {
		// Abrir conexion a base de datos Muro
		ConexioDBMuro conexion = new ConexioDBMuro();

		// Limpiar tabla I_OrderB2CAut
		ejecutarSQL("DELETE FROM I_OrderB2CAut WHERE C_BPartner_ID=1001237");

		PreparedStatement pst = null;
		ResultSet res = null;
		try {
			pst = conexion.conn.prepareStatement("SELECT * FROM ov_ordenesmuro");
			res = pst.executeQuery();
			while (res.next()) {
				int id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1005415,'N') from c_charge where c_charge_ID=1000010"));
				String description = res.getString("Description") == null ? "" : res.getString("Description");
				String NombreShopify = res.getString("NombreShopify") == null ? "" : res.getString("NombreShopify");
				String DireccionShopify = res.getString("DireccionShopify") == null ? "" : res.getString("DireccionShopify");

				DB.executeUpdate("INSERT INTO I_OrderB2CAut (I_OrderB2CAut_ID, C_OrderMuro_ID, DocumentNo, Description, OV_ORG_ID, OV_BPARTNER_LOCATION_ID,"
						+ " POReference, QtyOrdered, ProductValue, UPC, C_DocType_ID, SalesRep_ID, C_BPartner_ID, C_PaymentTerm_ID, M_Warehouse_ID,"
						+ " M_PriceList_ID, Bill_BPartner_ID, C_Tax_ID, NombreShopify, DireccionShopify, AD_Org_ID, BPartnerValue)"
						+ " VALUES (" + id + ", " + res.getInt("C_Order_ID") + ", '" + res.getString("DocumentNo") + "', '" + description.replaceAll("'", "") + "', " + res.getInt("AD_Org_ID") + ", " + res.getInt("C_BPartner_Location_ID") + ","
						+ " '" + res.getString("POReference") + "', " + res.getBigDecimal("QtyEntered") + ", '" + res.getString("Value") + "', '" + res.getString("UPC") + "', 1000030, 1003655, 1001237, 1000010, 1000001,"
						+ " 1000040, 1001237, 1000000, '" + NombreShopify.replaceAll("'", "") + "', '" + DireccionShopify.replaceAll("'", "") + "', 1000000, '76281810')", get_TrxName());
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			DB.close(res, pst); // rs, ps
			res = null;
			pst = null;
			closeConnQuietly(conexion.conn);
			conexion.conn = null;
		}
	}

	public int actualizaClient() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut "
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

	// Socio de Negocio
	public int actualizaBP() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut o "
				+ " SET C_BPartner_ID=(SELECT MAX(C_BPartner_ID) FROM C_BPartner bp"
				+ " 					 WHERE trim(o.BPartnerValue)=trim(bp.Value) AND o.AD_Client_ID=bp.AD_Client_ID) "
				+ " WHERE C_BPartner_ID IS NULL AND BPartnerValue IS NOT NULL"
				+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	// direccion
	public int actualizaBPL() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut o"
				+ " SET (BillTo_ID,C_BPartner_Location_ID)=(SELECT max(C_BPartner_Location_ID)C_BPartner_Location_ID,max(C_BPartner_Location_ID)C_BPartner_Location_ID"
				+ " 										  FROM C_BPartner_Location bpl INNER JOIN C_Location l ON (bpl.C_Location_ID=l.C_Location_ID)"
				+ " 										  WHERE o.C_BPartner_ID=bpl.C_BPartner_ID AND bpl.AD_Client_ID=o.AD_Client_ID"
				+ " 										  AND ( trim(upper(o.Address1))=trim(upper(l.Address1) )  or  trim(upper(o.Address1))=trim(upper(bpl.Name) )  ) )"
				+ " WHERE C_BPartner_ID IS NOT NULL AND C_BPartner_Location_ID IS NULL"
				+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	// Producto
	public int actualizaProduct() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut o"
				+ " SET M_Product_ID=(SELECT MAX(M_Product_ID) FROM M_Product p"
				+ " 					WHERE trim(o.ProductValue)=trim(p.Value) AND o.AD_Client_ID=p.AD_Client_ID)"
				+ " WHERE  ProductValue IS NOT NULL"
				+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	// Vendedor
	public int actualizaSalesRep() {
		sql = new StringBuffer("UPDATE I_OrderB2CAut"
				+ " SET SalesRep_ID=(SELECT MAX(u.AD_User_ID) FROM ad_user u"
				+ " 				   WHERE trim(lower(ChargeName))=trim(lower(u.name)) AND AD_Client_ID=u.AD_Client_ID)"
				+ " WHERE ChargeName IS NOT NULL"
				+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int beforeImport(String document, String bpvalue) {
		sql = new StringBuffer("UPDATE I_OrderB2CAut  "
				+ " SET I_IsImported='Y' , I_ERRORMSG = 'Orden Importada previamente' "
				+ " WHERE documentno= " + document
				+ " AND bpartnervalue=" + bpvalue);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	/**
	 * Ejecuta SQL manteniendo tu l�gica original (crea trx propia).
	 * OJO: Esto sigue existiendo porque lo ocupas en muchas partes del flujo �staging�.
	 */
	private void ejecutarSQL(String sql) {
		Trx trx = Trx.get(Trx.createTrxName("newTrx"), true);
		try {
			trx.start();
			DB.executeUpdate(sql, trx.getTrxName());
			log.info("SQL=" + sql);
			trx.commit();
		} catch (Exception e) {
			trx.rollback();
			System.out.println(e.getMessage());
		} finally {
			trx.close();
		}
	}

	/**
	 * NUEVO: Ejecuta SQL usando la MISMA trx (para no crear una transacci�n por l�nea).
	 * Se usa en la creaci�n de C_Order + C_OrderLine + updates por documento.
	 */
	private void ejecutarSQL(String sql, Trx trx) {
		if (trx == null)
			throw new IllegalArgumentException("trx is null");
		DB.executeUpdate(sql, trx.getTrxName());
		log.info("SQL=" + sql);
	}

	private void enviarCorreo(List<MOrder> lista, String mensajePie) {
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
			for (MOrder order : lista) {
				for (MOrderLine line : order.getLines()) {
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
		EMail email = M_Client.createEMail(correoTo, "Ordenes de Muro inyectadas " + new Timestamp(System.currentTimeMillis()), cuerpoMail.toString(), true);
		EMail.SENT_OK.equals(email.send());
	}
}
