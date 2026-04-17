/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation.                                           *
 *****************************************************************************/
package org.cwindsor.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * Importa pedidos desde ov_ordenesmuro@ERPMuro I_OrderB2C y genera Orden de
 * Venta (C_Order), procesando SOLO el C_Order_ID indicado.
 *
 * Cambios clave: - Parámetro C_Order_ID (TableDirect C_Order) - Precarga por
 * dblink ov_ordenesmuro@ERPMuro (Java 1.6) - Mantiene I_OrderB2C /
 * I_OrderB2CLine - Bloques try/catch/finally clásicos (sin try-with-resources)
 */
public class ProcesarOrdenMuroMashini extends SvrProcess {

	/** Client/Org */
	private int m_AD_Client_ID = 1000000;
	private int m_AD_Org_ID = 1000000;

	/** Parámetro: procesar solo un documento */
	private int p_C_Order_ID = 0;

	private StringBuffer sql = null;
	private int no = 0;
	private String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;

	// variables de correo (igual a original)
	final String miCorreo = "soporte@comercialwindsor.cl";
	final String miContrasena = "Cw9121100";
	final String servidorSMTP = "smtp.gmail.com";
	final String puertoEnvio = "465";
	String mailReceptor = null;
	String asunto = null;
	String cuerpo = null;

	private Timestamp m_DateValue = null;

	private Integer m_M_WareHouse_ID = 1000001; // Lampa
	private Integer m_M_WareHouse_ab_ID = 1000010; // Abastecimiento

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = 1000000;
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = 1000000;
			else if (name.equals("C_Order_ID"))
				p_C_Order_ID = para[i].getParameterAsInt(); // nuevo
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (m_DateValue == null)
			m_DateValue = new Timestamp(System.currentTimeMillis());
	} // prepare

	@Override
	protected String doIt() throws Exception {
		String menj1 = null, menj2 = null, menj3 = null,menj4 = null, menj5=null, menj6=null, menj7 = null;

		// Precarga SOLO el documento indicado, usando dblink (Java 1.6)
		prepararIOrderB2C();

		// **** Prepare ****
		log.info ("Reset=" + actualizaClient ());
		log.fine("Set BP from Value=" + actualizaBP());
		// log.fine("Found Location=" + actualizaBPL());
		log.fine("Set Product from Value=" + actualizaProduct ());
		log.fine("Set SalesRep_ID=" + actualizaSalesRep ());
		commitEx();

		ArrayList<String> ordenescompra = new ArrayList<String>();
		List<MOrder> listaOrder = new ArrayList<MOrder>();
		StringBuffer mensajeCorreo = new StringBuffer();

		// -- New Orders -----------------------------------------------------
		String slqoc  = "Select Documentno, BPartnerValue from I_OrderB2C where i_isImported<>'Y' and C_BPartner_ID=1001237 "
				+ clientCheck +  " Group by DocumentNo, BPartnerValue";

		// obtener ordenes para importar
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement (slqoc, null);
			rs = pstmt.executeQuery ();
			while (rs.next()) {
				String sqlcreados = "Select count(o.DocumentNO) as cuenta from I_orderB2C o  where o.i_isImported<>'Y' "
						+ " and o.bpartnervalue='76281810' and o.documentno in (select b2.poreference from c_orderb2c b2 "
						+ " where o.documentno=b2.poreference and b2.bpartnerValue=o.bpartnervalue) "
						+ " and o.documentno='"+rs.getString("DocumentNo")+"'";

				PreparedStatement pstmtc = null;
				ResultSet rsc = null;
				try {
					pstmtc = DB.prepareStatement(sqlcreados, null);
					rsc = pstmtc.executeQuery ();
					if (rsc.next()) {
						if(rsc.getInt("cuenta")==0) {
							ordenescompra.add(rs.getString("DocumentNo"));
						} else {
							log.fine("Set SalesRep_ID=" + beforeImport (rs.getString("DocumentNo"),rs.getString("BPartnerValue")));
						}
					}
				} catch(Exception e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					try { if (rsc != null) rsc.close(); } catch (Exception ignore) {}
					try { if (pstmtc != null) pstmtc.close(); } catch (Exception ignore) {}
				}
			}
		} catch(Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception ignore) {}
			try { if (pstmt != null) pstmt.close(); } catch (Exception ignore) {}
		}
		// fin obtener ordenes para importar

		int docok = 0;
		int docnotok= 0;

		if (!ordenescompra.isEmpty()) { // ordenes no esta vacio?
			for (int i=0; i < ordenescompra.size(); i++) { // recorrer Ordenes
				System.out.println("recorre " + ordenescompra.get(i));

				String recorre = "Select documentno, COALESCE(c_bpartner_ID,0) C_BPartner_ID, COALESCE(1011338,0)c_bpartner_location_ID,"
						+ " COALESCE(M_product_ID, 0) M_product_ID,"
						+ " replace(description,'/','-') description, name as tipogt, ContactName formacgt,"
						+ " trim(upper(Address2)) as subtienda, ChargeName as vendedor, Coalesce (SalesRep_ID,0) SalesRep_ID , "
						+ " LineDescription as Generico, COALESCE(QtyOrdered,0) as Cantidad, COALESCE(PriceActual,0) Precio, to_char(Created,'yyyy-mm-dd')Created, ProductValue, BPartnerValue, Address1, DOCTYPENAME, POReference,"
						+ " Address1, ov_org_id, C_ORDERMURO_id, NOMBRESHOPIFY, DIRECCIONSHOPIFY, CreatedBy "
						+ " from  I_OrderB2C where bpartnervalue='76281810' and documentNo='"+ ordenescompra.get(i)+"'";

				// ----- INICIO: bloque corregido para ejecutar "recorre" (Java 1.6) -----
				PreparedStatement pstmtRecorre = null;
				ResultSet rsRecorre = null;

				// variables de control/estado por documento
				int errorp = 0;
				int errorbp= 0;
				int errorbpl=0;
				int erroruser=0;
				int errorst = 0;
				int errorsl= 0;

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

				try {
					pstmtRecorre = DB.prepareStatement (recorre, null);
					rsRecorre = pstmtRecorre.executeQuery ();

					int lineas = 1;
					int b2c_id = 0;

					while (rsRecorre.next()) {
						boolean isInsertLine = false;

						Integer Cantidad = rsRecorre.getInt("Cantidad");
						C_BPartner_ID = rsRecorre.getInt("C_BPartner_ID");
						String BPartnerValue = rsRecorre.getString("BPartnerValue");
						C_BPartner_Location_ID = rsRecorre.getInt("C_BPartner_Location_ID");
						String Address1 = rsRecorre.getString("Address1");
						Integer M_Product_ID = rsRecorre.getInt("M_Product_ID");
						String Generico = rsRecorre.getString("Generico");
						description = rsRecorre.getString("description");
						documentno = rsRecorre.getString("documentno");
						Created = rsRecorre.getString("Created");
						SalesRep_ID = rsRecorre.getInt("SalesRep_ID");
						String vendedor = rsRecorre.getString("vendedor");
						String subtienda = rsRecorre.getString("subtienda");
						Integer Precio = rsRecorre.getInt("Precio");
						String ProductValue = rsRecorre.getString("ProductValue");
						String DOCTYPENAME = rsRecorre.getString("DOCTYPENAME");
						String VentaEnVerde = "";
						String Processed = "";
						DateAcct = rsRecorre.getString("Created");
						String ErrorMsgCab = "";
						C_PaymentTerm_ID = DB.getSQLValue(null, "SELECT MAX(C_PaymentTerm_ID) FROM C_BPartner WHERE C_BPartner_ID = " + C_BPartner_ID);
						C_OrderMuro_ID = rsRecorre.getInt("C_OrderMuro_ID");
						NOMBRESHOPIFY = rsRecorre.getString("NOMBRESHOPIFY")==null?"":rsRecorre.getString("NOMBRESHOPIFY");
						DIRECCIONSHOPIFY = rsRecorre.getString("DIRECCIONSHOPIFY")==null?"":rsRecorre.getString("DIRECCIONSHOPIFY");
						POReference = rsRecorre.getString("POReference");

						if(Cantidad>=1) {
							if (C_BPartner_ID==0) {
								errorbp=1; menj1="Rut Cliente no encontrado:"+ BPartnerValue;
							}
							if(C_BPartner_Location_ID==0) {
								errorbpl=2; menj2="Direccion no coincide con la base de datos:"+ Address1;
							}
							if(M_Product_ID==0) {
								errorp=3; menj3="No viene el codigo Windsor o no coincide con la Base de datos";
							}
							if(M_Product_ID==0 && (Generico==null  | Generico.equals("") )) {
								errorp=4; menj4="Sin Codigo Windsor y sin Sku cliente";
							}
						}
						if(SalesRep_ID==0) {
							erroruser=6; menj6="Venedor no encontrado:"+ vendedor;
						}

						if (errorbp!=0) {
							ErrorMsgCab =  menj1;
						} else if (errorbpl!=0) {
							ErrorMsgCab = menj2;
						} else if (erroruser!=0) {
							ErrorMsgCab = menj6;
						}

						if (b2c_id==0) {
							// cabecera C_ORDERB2C (igual a tu flujo)
							b2c_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1004938,'N') from c_charge where c_charge_ID=1000010"));
							String fc="";
							mc = "Internet";
							VentaEnVerde = "N";
							FormaCompra = fc;
							Processed = "N";

							// subtienda
							if(C_BPartner_ID>0 && subtienda!=null) {
								String sqlst = "Select count (*) as cuenta from C_BPartner_SubTienda where c_Bpartner_ID="+C_BPartner_ID +
										" and trim(upper(name))= trim(upper('"+subtienda+"'))";
								PreparedStatement pstmtst = null;
								ResultSet rsst = null;
								try {
									pstmtst = DB.prepareStatement (sqlst, null);
									rsst = pstmtst.executeQuery ();
									if(rsst.next()) {
										if (rsst.getInt("cuenta")>0) {
											String sqlstv= "Select max( C_BPartner_SubTienda_ID)as C_BPartner_SubTienda_ID  from C_BPartner_SubTienda where c_Bpartner_ID="+C_BPartner_ID +
													" and trim(upper(name))= trim(upper('"+subtienda+"'))";
											PreparedStatement pstmtstv = null;
											ResultSet rsstv = null;
											try {
												pstmtstv = DB.prepareStatement (sqlstv, null);
												rsstv = pstmtstv.executeQuery ();
												if(rsstv.next())
													C_BPartner_SubTienda_ID = rsstv.getInt("C_BPartner_SubTienda_ID");
											} catch(Exception e) {
												log.log(Level.SEVERE, e.getMessage(), e);
											} finally {
												try { if (rsstv != null) rsstv.close(); } catch (Exception ignore) {}
												try { if (pstmtstv != null) pstmtstv.close(); } catch (Exception ignore) {}
											}
										} else {
											errorst=8;
										}
									}
								} catch(Exception e) {
									log.log(Level.SEVERE, e.getMessage(), e);
								} finally {
									try { if (rsst != null) rsst.close(); } catch (Exception ignore) {}
									try { if (pstmtst != null) pstmtst.close(); } catch (Exception ignore) {}
								}
							}

							String insert = "INSERT INTO C_ORDERB2C (C_OrderB2C_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,C_BPartner_ID,C_BPartner_Location_ID,POReference,DocumentNo,DateAcct,"
									+ "Processed,BPartnerValue,Address1,M_WAREHOUSE_ID,SalesRep_ID,M_PriceList_ID,"
									+ "Description,MedioCompra,VentaEnVerde,FormaCompra,C_BPartner_SubTienda_ID,ErrorMsg, C_OrderMuro_ID, DocumentoMuro, NOMBRESHOPIFY, DIRECCIONSHOPIFY) "
									+ "VALUES "
									+ "("+b2c_id+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+C_BPartner_ID+","+C_BPartner_Location_ID+",'"+POReference+"',(Select NEXTIDFUNC(1004942,'N') from c_charge where c_charge_ID=1000010),to_date('"+Created+"','yyyy-mm-dd'),"
									+ "'"+Processed+"','"+BPartnerValue+"',"+(Address1==null?"null":"'"+Address1+"'")+","+M_Warehouse_ID+","+SalesRep_ID+",1000000,"
									+ "'"+description+"','"+mc+"','"+VentaEnVerde+"','"+FormaCompra+"',"+(C_BPartner_SubTienda_ID==0?"null":C_BPartner_SubTienda_ID)+",'"+ErrorMsgCab+"', "+C_OrderMuro_ID+", '"+documentno+"', '"+NOMBRESHOPIFY.replaceAll("'", "")+"', '"+DIRECCIONSHOPIFY.replaceAll("'", "")+"')";
							ejecutarSQL(insert);
						}

						BigDecimal sob = BigDecimal.ZERO;

						if (b2c_id != 0) {
							int salto=0;
							int obl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1004939,'N') from c_charge where c_charge_ID=1000010"));
							String pasarAOV = "";
							String ErrorMsg = "";
							if(errorp>0) {
								if(errorp==3) { ErrorMsg = menj3; }
								else if(errorp==4) { ErrorMsg = menj4; }
							}
							BigDecimal cant = new BigDecimal (Cantidad);

							// Validación producto
							String sqlp = "Select count(*) as cuenta from m_product where m_product_ID="+M_Product_ID;
							PreparedStatement pstmtp = null; ResultSet rsp = null;
							try {
								pstmtp = DB.prepareStatement (sqlp, null);
								rsp = pstmtp.executeQuery();
								if (rsp.next()) {
									if (rsp.getInt("cuenta")==1) {
										// Reserva e-commerce
										String sqlqr =	"select count(1) encontrado "+
												" from m_requisition r " +
												" where r.docstatus='CO' and r.c_doctype_ID=1000570 "+
												" and r.c_bpartner_ID="+C_BPartner_ID
												+ " and r.M_RequisitionRef_ID in " +
												" (select r2.m_requisition_ID "+
												" from m_requisition r2 "+
												" inner join m_requisitionline rl on (r2.m_requisition_ID=rl.m_Requisition_ID) "+
												" where r2.docstatus='CO' and rl.m_product_ID="+M_Product_ID+
												" and (rl.qtyreserved)>0 and r2.c_doctype_ID=1000569 and rl.liberada='N' )";
										PreparedStatement pstmtre = null; ResultSet rsre = null;
										try {
											pstmtre = DB.prepareStatement (sqlqr, null);
											rsre = pstmtre.executeQuery ();
											if(rsre.next() && rsre.getInt("encontrado")>0) {
												String sqlqrq =	" select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "+
														" from m_Requisitionline rl "+
														" inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "+
														" where r.docstatus='CO'  "+
														" and r.c_doctype_id=1000569 "+
														" and rl.liberada='N' "+
														" and rl.m_product_ID ="+M_Product_ID +
														" and exists  "+
														" (select * "+
														" from m_requisition r2 "+
														" where R2.M_REQUISITIONREF_ID=r.m_requisition_ID "+
														"  and r2.c_doctype_ID=1000570 "+
														"  and r2.docstatus='CO' "+
														"  and r2.c_bpartner_ID="+C_BPartner_ID+ " ) ";
												PreparedStatement pstmtreq = null; ResultSet rsreq = null;
												try {
													pstmtreq = DB.prepareStatement (sqlqrq, null);
													rsreq = pstmtreq.executeQuery ();
													if(rsreq.next()) {
														if (Cantidad<=rsreq.getInt("qtyreserved")) {
															BigDecimal neto = new BigDecimal (Cantidad * Precio );
															String insertLine = "INSERT INTO C_ORDERB2CLine (C_OrderB2CLine_ID,C_OrderB2C_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																	+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																	+ "VALUES ("+obl_id+","+b2c_id+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+(lineas * 10)+","+cant+","+neto+",'"+ProductValue+"',"
																	+ (DOCTYPENAME==null?"null":"'"+DOCTYPENAME+"'")+","+M_Product_ID+",'Y',"+Precio+","+(Generico==null?"null":"'"+Generico+"'")+","+rsreq.getInt("M_RequisitionLine_ID")+")";
															ejecutarSQL(insertLine);
															isInsertLine = true;
															cant = BigDecimal.ZERO;
															salto=1;
														} else if (Cantidad>rsreq.getInt("qtyreserved")) {
															BigDecimal aux = new BigDecimal (rsreq.getInt("qtyreserved"));
															BigDecimal neto = new BigDecimal (rsreq.getInt("qtyreserved") * Precio );
															String insertLine = "INSERT INTO C_ORDERB2CLine (C_OrderB2CLine_ID,C_OrderB2C_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																	+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																	+ "VALUES ("+obl_id+","+b2c_id+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+(lineas * 10)+","+aux+","+neto+",'"+ProductValue+"',"
																	+ (DOCTYPENAME==null?"null":"'"+DOCTYPENAME+"'")+","+M_Product_ID+",'Y',"+Precio+","+(Generico==null?"null":"'"+Generico+"'")+","+rsreq.getInt("M_RequisitionLine_ID")+")";
															ejecutarSQL(insertLine);
															isInsertLine = true;
															cant = new BigDecimal(cant.intValue() - aux.intValue());
															salto=2;
														}
													}
												} catch(Exception e) {
													log.log(Level.SEVERE, e.getMessage(), e);
												} finally {
													try { if (rsreq != null) rsreq.close(); } catch (Exception ignore) {}
													try { if (pstmtreq != null) pstmtreq.close(); } catch (Exception ignore) {}
												}
											}
										} catch(Exception e) {
											log.log(Level.SEVERE, e.getMessage(), e);
										} finally {
											try { if (rsre != null) rsre.close(); } catch (Exception ignore) {}
											try { if (pstmtre != null) pstmtre.close(); } catch (Exception ignore) {}
										}

										// Reserva física
										if(salto==0 || salto==2) {
											String sqlrfn=" select count(1) encontrado "+
													" from m_requisition r  "+
													" inner join m_requisitionline rl on (r.m_requisition_ID=rl.m_Requisition_ID) "+
													" where r.docstatus='CO' and r.c_doctype_ID=1000111 "+
													" and r.c_bpartner_ID="+C_BPartner_ID+
													" and (r.c_bpartner_location_ID="+C_BPartner_Location_ID+
													" or R.OVERWRITEREQUISITION='Y') "+
													" and rl.m_product_ID="+M_Product_ID+
													" and (rl.qtyreserved)>0  "+
													" and  rl.LIBERADA='N' ";
											PreparedStatement pstmtrfn = null; ResultSet rsrfn = null;
											try {
												pstmtrfn = DB.prepareStatement (sqlrfn, null);
												rsrfn = pstmtrfn.executeQuery ();
												if(rsrfn.next() && rsrfn.getInt("encontrado")>0) {
													if(salto==2) {
														lineas++;
														obl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1004939,'N') from c_charge where c_charge_ID=1000010"));
													}
													String sqlrn =	" select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "+
															" from m_Requisitionline rl "+
															" inner join m_requisition r on (rl.m_Requisition_ID=r.m_Requisition_ID) "+
															" where r.docstatus='CO'  "+
															" and r.c_doctype_id=1000111 "+
															" and rl.m_product_ID ="+M_Product_ID +
															" and r.c_bpartner_ID="+C_BPartner_ID +
															" and (r.c_bpartner_location_ID="+C_BPartner_Location_ID +
															" or R.OVERWRITEREQUISITION='Y') "+
															" and (rl.qtyreserved)>0"+
															" and  rl.LIBERADA='N' " ;
													PreparedStatement pstmtrn = null; ResultSet rsrn = null;
													try {
														pstmtrn = DB.prepareStatement (sqlrn, null);
														rsrn = pstmtrn.executeQuery ();
														if(rsrn.next()) {
															if (Cantidad<=rsrn.getInt("qtyreserved")) {
																BigDecimal neto = new BigDecimal (cant.intValue() * Precio );
																String insertLine = "INSERT INTO C_ORDERB2CLine (C_OrderB2CLine_ID,C_OrderB2C_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																		+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																		+ "VALUES ("+obl_id+","+b2c_id+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+(lineas * 10)+","+cant+","+neto+",'"+ProductValue+"',"
																		+ "'"+DOCTYPENAME+"',"+M_Product_ID+",'Y',"+Precio+",'"+Generico+"',"+rsrn.getInt("M_RequisitionLine_ID")+")";
																ejecutarSQL(insertLine);
																isInsertLine = true;
																cant = BigDecimal.ZERO;
																salto=3;
															} else if (Cantidad>rsrn.getInt("qtyreserved")) {
																BigDecimal aux = new BigDecimal (rsrn.getInt("qtyreserved"));
																BigDecimal neto = new BigDecimal (rsrn.getInt("qtyreserved") * Precio );
																String insertLine = "INSERT INTO C_ORDERB2CLine (C_OrderB2CLine_ID,C_OrderB2C_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																		+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,M_RequisitionLine_ID) "
																		+ "VALUES ("+obl_id+","+b2c_id+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+(lineas * 10)+","+aux+","+neto+",'"+ProductValue+"',"
																		+ "'"+DOCTYPENAME+"',"+M_Product_ID+",'Y',"+Precio+",'"+Generico+"',"+rsrn.getInt("M_RequisitionLine_ID")+")";
																ejecutarSQL(insertLine);
																isInsertLine = true;
																cant = new BigDecimal(cant.intValue() - aux.intValue());
																salto=4;
															}
														}
													} catch(Exception e) {
														log.log(Level.SEVERE, e.getMessage(), e);
													} finally {
														try { if (rsrn != null) rsrn.close(); } catch (Exception ignore) {}
														try { if (pstmtrn != null) pstmtrn.close(); } catch (Exception ignore) {}
													}
												}
											} catch(Exception e) {
												log.log(Level.SEVERE, e.getMessage(), e);
											} finally {
												try { if (rsrfn != null) rsrfn.close(); } catch (Exception ignore) {}
												try { if (pstmtrfn != null) pstmtrfn.close(); } catch (Exception ignore) {}
											}
										}

										// Disponible
										if(salto==0 || salto==4 || salto==2) {
											if (salto==4 || salto==2) {
												obl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1004939,'N') from c_charge where c_charge_ID=1000010"));
												lineas++;
											}
											String sqlps = "Select " +
													" COALESCE ( "+
													"         (SELECT SUM (s.qtyonhand) "+
													"           FROM rv_storage380 s "+
													"         WHERE     s.M_Product_ID = p.m_product_id "+
													"              AND s.m_warehouse_id IN (1000001, 1000010,1000033) "+
													"             AND s.isactive = 'Y'), "+
													"    0) "+
													" - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
													"         FROM C_orderline ol2      "+
													"             INNER JOIN C_Order o2  "+
													"               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
													"     WHERE     ol2.M_Product_ID = p.m_product_id "+
													"          AND o2.m_warehouse_id = 1000001 "+
													"         AND o2.saldada <> 'Y' "+
													"        AND o2.docstatus IN ('IP', 'CO', 'CL') "+
													"       AND o2.issotrx = 'Y' "+
													"      AND o2.c_doctypetarget_ID NOT IN "+
													"            (1000110, 1000048, 1000568)) "+
													" + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
													"     FROM M_Requisitionline rl     "+
													"         INNER JOIN M_Requisition r  "+
													"           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
													" WHERE     rl.M_Product_ID = p.m_product_id  "+
													"      AND r.m_warehouse_id = 1000001  "+
													"     AND r.docstatus IN ('CO', 'CL')  "+
													"    AND r.issotrx = 'Y'))  "+
													" as Disponible, p.ProductType,  " +
													" COALESCE ( "+
													"         (SELECT SUM (s.qtyonhand) "+
													"           FROM rv_storage s "+
													"         WHERE     s.M_Product_ID = p.m_product_id "+
													"              AND s.m_warehouse_id IN (1000024, 1000025) "+
													"             AND s.isactive = 'Y'), "+
													"    0) "+
													" - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
													"         FROM C_orderline ol2      "+
													"             INNER JOIN C_Order o2  "+
													"               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
													"     WHERE     ol2.M_Product_ID = p.m_product_id "+
													"          AND o2.m_warehouse_id in ( 1000024,1000025) "+
													"         AND o2.saldada <> 'Y' "+
													"        AND o2.docstatus IN ('IP', 'CO', 'CL') "+
													"       AND o2.issotrx = 'Y' "+
													"      AND o2.c_doctypetarget_ID NOT IN "+
													"            (1000110, 1000048, 1000568)) "+
													" + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
													"     FROM M_Requisitionline rl     "+
													"         INNER JOIN M_Requisition r  "+
													"           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
													" WHERE     rl.M_Product_ID = p.m_product_id  "+
													"      AND r.m_warehouse_id in ( 1000024,1000025)  "+
													"     AND r.docstatus IN ('CO', 'CL')  "+
													"    AND r.issotrx = 'Y'))  "+
													" as OtroDisponible"+
													" from M_product p where  p.m_product_ID="+ M_Product_ID;
											PreparedStatement pstmtps = null; ResultSet rsps = null;
											try {
												pstmtps = DB.prepareStatement (sqlps, null);
												rsps = pstmtps.executeQuery ();
												if(rsps.next()) {
													if("I".equals(rsps.getString("ProductType"))) {
														if (cant.intValue()<=rsps.getInt("Disponible")) {
															BigDecimal neto = new BigDecimal (cant.intValue() * Precio );
															String insertLine = "INSERT INTO C_ORDERB2CLine (C_OrderB2CLine_ID,C_OrderB2C_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
																	+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO) "
																	+ "VALUES ("+obl_id+","+b2c_id+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+(lineas * 10)+","+cant+","+neto+",'"+ProductValue+"',"
																	+ "'"+DOCTYPENAME+"',"+M_Product_ID+",'Y',"+Precio+",'"+Generico+"')";
															ejecutarSQL(insertLine);
															isInsertLine = true;
															salto=5;
														} else {
															errorp=5;
															salto=6;
															pasarAOV = "N";
															sob = new BigDecimal (rsps.getInt("OtroDisponible"));
															ErrorMsg = "No hay stock en la bodega actual, verificar en otras bodegas";
															mensajeCorreo.append("Pedido " + documentno + " Producto " + ProductValue + " No hay stock en la bodega actual, verificar en otras bodegas");
															mensajeCorreo.append("<br />");
														}
													} else {
														pasarAOV = "Y";
													}
												}
											} catch(Exception e) {
												log.log(Level.SEVERE, e.getMessage(), e);
											} finally {
												try { if (rsps != null) rsps.close(); } catch (Exception ignore) {}
												try { if (pstmtps != null) pstmtps.close(); } catch (Exception ignore) {}
											}
										}
									} else {
										errorp=7;
										pasarAOV = "N";
										// ErrorMsg ya seteado
									}
								}
							} catch(Exception e) {
								log.log(Level.SEVERE, e.getMessage(), e);
							} finally {
								try { if (rsp != null) rsp.close(); } catch (Exception ignore) {}
								try { if (pstmtp != null) pstmtp.close(); } catch (Exception ignore) {}
							}

							if(errorst==8) {
								// ErrorMsg = "Subtienda no existe:"+subtienda;
							}

							if (!isInsertLine) {
								BigDecimal neto = new BigDecimal (Cantidad * Precio );
								String insertLine = "INSERT INTO C_ORDERB2CLine (C_OrderB2CLine_ID,C_OrderB2C_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,Line,QtyEntered,LineNetAmt,ProductValue,"
										+ "NOMBREGENERICO,M_Product_ID,PASARAOV,PriceEntered,CODIGOGENERICO,StockBodegas,ErrorMsg) "
										+ "VALUES ("+obl_id+","+b2c_id+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+(lineas * 10)+","+Cantidad+","+neto+",'"+ProductValue+"',"
										+ "'"+DOCTYPENAME+"',"+M_Product_ID+",'"+pasarAOV+"',"+Precio+",'"+Generico+"',"+sob+",null)";
								ejecutarSQL(insertLine);
							}
						}
						lineas++;
					} // while rsRecorre

					// === Crear OV si no hay errores controlados y hay líneas OK ===
					if (errorp + errorbp +  errorbpl +  erroruser + errorst==0) {
						MOrder order = null;
						int orderId = 0;
						int contador = 1;
						int contadordoc = 0;
						int documentNo = 1;

						// validar que existan líneas en C_OrderB2CLine
						String sqlvl = "Select count(*) cuenta from C_OrderB2CLine where C_OrderB2C_ID="+
								Integer.parseInt(DB.getSQLValueString(null, "SELECT MAX(C_OrderB2C_ID) FROM C_ORDERB2C WHERE DocumentoMuro='"+documentno+"'"));
						PreparedStatement pstmtvl = null; ResultSet rsvl = null;
						try {
							pstmtvl = DB.prepareStatement (sqlvl, null);
							rsvl = pstmtvl.executeQuery ();
							if(rsvl.next() && rsvl.getInt("cuenta")>0) {
								// validar que todas estén para pasar a OV
								String sqlvlok= "Select count(*) cuenta from C_OrderB2CLine where PASARAOV<>'Y' and C_OrderB2C_ID=(SELECT MAX(C_OrderB2C_ID) FROM C_ORDERB2C WHERE DocumentoMuro='"+documentno+"')";
								PreparedStatement pstmtvlok = null; ResultSet rsvlok = null;
								try {
									pstmtvlok = DB.prepareStatement (sqlvlok, null);
									rsvlok = pstmtvlok.executeQuery ();
									if (rsvlok.next() && rsvlok.getInt("cuenta")==0) { //  abre if(rsvlok...)
									    String sqllines = "Select ol.*, COALESCE(mp.C_UOM_ID,100) AS C_UOM_ID from C_OrderB2CLine ol LEFT JOIN M_PRODUCT mp ON ol.M_PRODUCT_ID = mp.M_PRODUCT_ID WHERE C_OrderB2C_ID = "
									            + Integer.parseInt(DB.getSQLValueString(null, "SELECT MAX(C_OrderB2C_ID) FROM C_ORDERB2C WHERE DocumentoMuro='"+documentno+"'"));
									    PreparedStatement pstmtlines = null; 
									    ResultSet rslines = null;
									    try { //  abre try interno
									        pstmtlines = DB.prepareStatement (sqllines, null);
									        rslines = pstmtlines.executeQuery ();
									        while (rslines.next()) { //  abre while
									            if(orderId==0) { //  abre if(orderId==0)
									                orderId = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(232,'N') from c_charge where c_charge_ID=1000010"));
									                contadordoc=contadordoc+1;

									                int C_DocTypeTarget_ID = 1000030;
									                String documentNoSeq = (documentNo > 1) ? documentno+"-"+documentNo : documentno;

									                String insertOrder = "INSERT INTO C_Order (C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,C_DocTypeTarget_ID,IsSOTrx,DeliveryRule,"
									                        + "C_BPartner_ID,C_BPartner_Location_ID,POReference,AD_User_ID,Bill_BPartner_ID,Bill_Location_ID,Description,"
									                        + "C_PaymentTerm_ID,M_PriceList_ID,M_Warehouse_ID,SalesRep_ID,DateOrdered,DateAcct,InvoiceRule,"
									                        + "DOCUMENTOMURO,VENTAINVIERNO,DocumentNo,DOCSTATUS,DOCACTION,C_DOCTYPE_ID,C_CURRENCY_ID,"
									                        + "PAYMENTRULE,FREIGHTCOSTRULE,DELIVERYVIARULE,PRIORITYRULE,DatePromised,C_BPartner_SubTienda_ID,FIRMA2,FIRMA3,FIRMACOM,FIRMAFIN,"
									                        + "USERFIRMCOM,FormaCompra,MedioCompra, C_OrderMuro_ID) "
									                        + "VALUES ("+orderId+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+C_DocTypeTarget_ID+",'Y','O',"
									                        + C_BPartner_ID+","+C_BPartner_Location_ID+",'"+POReference+"',"+SalesRep_ID+","+C_BPartner_ID+","+C_BPartner_Location_ID+",'"+description+"',"
									                        + C_PaymentTerm_ID+","+M_PriceList_ID+","+M_Warehouse_ID+","+SalesRep_ID+",to_date('"+DateAcct+"','yyyy-mm-dd'),to_date('"+DateAcct+"','yyyy-mm-dd'),'D',"
									                        + "'"+documentno+"','N','"+documentNoSeq+"','DR','CO',1000030,228,"
									                        + "'P','I','P','5',to_date('"+Created+"','yyyy-mm-dd'),"+C_BPartner_SubTienda_ID+",'Y','Y',to_date('"+DateAcct+"','yyyy-mm-dd'),to_date('"+DateAcct+"','yyyy-mm-dd'),"
									                        + 1003655+",'"+FormaCompra+"','"+mc+"', "+C_OrderMuro_ID+")";
									                ejecutarSQL(insertOrder);
									            } //  cierra if(orderId==0)

									            if (contador<=25) { //  abre if(contador<=25)
									                int orderLineId = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(233,'N') from c_charge where c_charge_ID=1000010"));
									                String insertOrderLine = "INSERT INTO C_OrderLine (C_OrderLine_ID,C_Order_ID,AD_Client_ID,AD_Org_ID,CreatedBy,UpdatedBy,M_Product_ID,PriceEntered,PriceActual,PriceList,"
									                        + "QtyEntered,Line,Qty,Demand,Discount,Discount2,Discount3,Discount4,Discount5,NotPrint,LineNetAmt,DATEORDERED,M_WAREHOUSE_ID,C_UOM_ID,"
									                        + "C_CURRENCY_ID,C_TAX_ID,M_RequisitionLine_ID,C_BPARTNER_ID,C_BPARTNER_LOCATION_ID,DATEPROMISED) "
									                        + "VALUES ("+orderLineId+","+orderId+","+m_AD_Client_ID+","+m_AD_Org_ID+","+CreatedBy+","+CreatedBy+","+rslines.getInt("M_Product_ID")+","+rslines.getBigDecimal("priceentered")+","+rslines.getBigDecimal("priceentered")+","+rslines.getBigDecimal("priceentered")+"," 
									                        + rslines.getBigDecimal("qtyentered")+","+(contador * 10)+","+rslines.getBigDecimal("qtyentered")+","+rslines.getBigDecimal("qtyentered")+",0,0,0,0,0,'N',"+(rslines.getBigDecimal("priceentered").multiply(rslines.getBigDecimal("qtyentered")).setScale(0, RoundingMode.HALF_UP))+",to_date('"+Created+"','yyyy-mm-dd'),"+M_Warehouse_ID+","+rslines.getBigDecimal("C_UOM_ID")+"," 
									                        + "228,1000000,"+rslines.getBigDecimal("M_RequisitionLine_ID")+","+C_BPartner_ID+","+C_BPartner_Location_ID+",to_date('"+Created+"','yyyy-mm-dd'))";
									                ejecutarSQL(insertOrderLine);
									                contador++;

									                if (rslines.getBigDecimal("M_RequisitionLine_ID") != null) { //  abre if(M_RequisitionLine_ID!=null)
									                    String sqlUpRL = "SELECT SUM(QTYENTERED) "
									                            + " FROM C_OrderLine col"
									                            + " INNER JOIN C_Order co ON (col.C_Order_ID = co.C_Order_ID)"
									                            + " WHERE M_RequisitionLine_ID = " + rslines.getBigDecimal("M_RequisitionLine_ID") + " AND co.DocStatus IN ('DR','IP','CO','CL','IN')";
									                    BigDecimal amt = DB.getSQLValueBD(get_TrxName(), sqlUpRL);
									                    if (amt == null) amt = Env.ZERO; //  if interno
									                    String update = "UPDATE M_RequisitionLine"
									                            + " SET QtyUsed = " + amt
									                            + " WHERE M_RequisitionLine_ID = " + rslines.getBigDecimal("M_RequisitionLine_ID");
									                    ejecutarSQL(update);
									                } //  cierra if(M_RequisitionLine_ID!=null)

									                String updateOrderB2CLine = "UPDATE C_OrderB2CLine"
									                        + " SET C_Order_ID = "+orderId+", Processed = 'Y', c_orderline_id = "+orderLineId
									                        + " WHERE C_OrderB2CLine_ID = "+rslines.getBigDecimal("C_OrderB2CLine_ID");
									                ejecutarSQL(updateOrderB2CLine);
									            } //  cierra if(contador<=25)

									            if (contador==26) { //  abre if(contador==26)
									                MOrder orderTmp = new MOrder(getCtx(), orderId, null);
									                orderTmp.calculateTaxTotal();
									                orderTmp.saveEx();
									                if (!orderTmp.getDocStatus().equalsIgnoreCase("CO")) { //  abre if(co)
									                    orderTmp.setDocAction("CO");
									                    if (!orderTmp.processIt("CO")) { //  abre if(process)
									                        System.out.println("Error al completar Documento " + orderTmp.getDocumentNo());
									                    } else { //  else de if(process)
									                        orderTmp.save();
									                    } //  cierra if(process)
									                } //  cierra if(co)
									                docok++;
									                contador = 1;
									                orderTmp = null;
									                orderId = 0;
									                documentNo++;
									            } //  cierra if(contador==26)
									        } //  cierra while rslines

									        MOrder orderFin = new MOrder(getCtx(), orderId, null);
									        orderFin.calculateTaxTotal();
									        orderFin.saveEx();
									        if (!orderFin.getDocStatus().equalsIgnoreCase("CO")) { //  abre if
									            orderFin.setDocAction("CO");
									            if (!orderFin.processIt("CO")) { //  abre if
									                System.out.println("Error al completar Documento " + orderFin.getDocumentNo());
									            } else { //  else
									                orderFin.save();
									            } //  cierra if
									        } //  cierra if
									        listaOrder.add(orderFin);
									        String update = "UPDATE C_ORDERB2C SET Processed = 'Y' WHERE C_ORDERB2C_ID = (SELECT MAX(C_OrderB2C_ID) FROM C_ORDERB2C WHERE DocumentoMuro='"+documentno+"')";
									        ejecutarSQL(update);
									        contador=1;
									        docok++;
									        orderFin=null;
									        documentNo++;
									        commitEx();

									        sql = new StringBuffer ("UPDATE I_OrderB2C "
									                + "SET I_IsImported='Y' , processed='Y' "
									                + "WHERE Documentno='" + documentno + "'");
									        no = DB.executeUpdate(sql.toString(), get_TrxName());
									        commitEx();
									        if (no == -1) //  if (no == -1)
									            log.warning ("No importado=" + no);
									    } catch(Exception e) { //  catch del try interno
									        log.log(Level.SEVERE, e.getMessage(), e);
									    } finally { //  finally del try interno
									        try { if (rslines != null) rslines.close(); } catch (Exception ignore) {}
									        try { if (pstmtlines != null) pstmtlines.close(); } catch (Exception ignore) {}
									    }
									} //  cierra if(rsvlok.next() && ... )  *** ESTA LLAVE FALTABA ***
									else { //  ahora este else cuelga del if(rsvlok.next() && ...)
									    errorsl = 10;
									}

									
								} catch(Exception e) {
									log.log(Level.SEVERE, e.getMessage(), e);
								} finally {
									try { if (rsvlok != null) rsvlok.close(); } catch (Exception ignore) {}
									try { if (pstmtvlok != null) pstmtvlok.close(); } catch (Exception ignore) {}
								}
							}
						} catch(Exception e) {
							log.log(Level.SEVERE, e.getMessage(), e);
						} finally {
							try { if (rsvl != null) rsvl.close(); } catch (Exception ignore) {}
							try { if (pstmtvl != null) pstmtvl.close(); } catch (Exception ignore) {}
						}
					} else {
						docnotok++;
						commitEx();
					}
					if (errorsl==9) {
						// sin líneas
					}

				} catch(Exception e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					try { if (rsRecorre != null) rsRecorre.close(); } catch (Exception ignore) {}
					try { if (pstmtRecorre != null) pstmtRecorre.close(); } catch (Exception ignore) {}
				}
				// ----- FIN bloque corregido -----
			}
		}

		// enviarCorreo(listaOrder, mensajeCorreo.toString());

		return "Proceso terminado - Ordenes Creadas:" + docok + " - Ordenes con Error: "+ docnotok;
	} // doIt

	/**
	 * Precarga SOLO el C_Order_ID indicado desde ov_ordenesmuro@ERPMuro hacia
	 * I_OrderB2C. Java 1.6 (sin try-with-resources).
	 */
	private void prepararIOrderB2C() {
		// Validación
		if (p_C_Order_ID <= 0) {
			throw new IllegalArgumentException(
					"Debe indicar un C_Order_ID válido (>0).");
		}

		// Limpiar staging solo del BP que usas acá (igual a tu original)
		ejecutarSQL("DELETE FROM I_OrderB2C WHERE C_BPartner_ID=1001237");

		PreparedStatement pst = null;
		ResultSet res = null;

		try {
			String sqlSelect = "SELECT "
					+ " C_Order_ID, DocumentNo, Description, AD_Org_ID, C_BPartner_Location_ID,"
					+ " POReference, QtyEntered, Value, UPC, NombreShopify, DireccionShopify "
					+ "FROM ov_ordenesmuro@ERPMuro " + "WHERE C_Order_ID = "
					+ p_C_Order_ID;

			pst = DB.prepareStatement(sqlSelect, null);
			res = pst.executeQuery();

			while (res.next()) {
				int id = Integer
						.parseInt(DB
								.getSQLValueString(null,
										"Select NEXTIDFUNC(1004937,'N') from c_charge where c_charge_ID=1000010"));
				String description = res.getString("Description") == null ? ""
						: res.getString("Description");
				String NombreShopify = res.getString("NombreShopify") == null ? ""
						: res.getString("NombreShopify");
				String DireccionShopify = res.getString("DireccionShopify") == null ? ""
						: res.getString("DireccionShopify");

				DB.executeUpdate(
						"INSERT INTO I_OrderB2C (I_OrderB2C_ID, C_OrderMuro_ID, DocumentNo, Description, OV_ORG_ID, OV_BPARTNER_LOCATION_ID,"
								+ " POReference, QtyOrdered, ProductValue, UPC, C_DocType_ID, SalesRep_ID, C_BPartner_ID, C_PaymentTerm_ID, M_Warehouse_ID,"
								+ " M_PriceList_ID, Bill_BPartner_ID, C_Tax_ID, NombreShopify, DireccionShopify, AD_Org_ID, BPartnerValue, AD_Client_ID,"
								+ " I_IsImported, IsActive, Created, CreatedBy, Updated, UpdatedBy)"
								+ " VALUES ("
								+ id
								+ ", "
								+ res.getInt("C_Order_ID")
								+ ", '"
								+ res.getString("DocumentNo")
								+ "', '"
								+ description.replaceAll("'", "")
								+ "', "
								+ res.getInt("AD_Org_ID")
								+ ", "
								+ res.getInt("C_BPartner_Location_ID")
								+ ","
								+ " '"
								+ res.getString("POReference")
								+ "', "
								+ res.getBigDecimal("QtyEntered")
								+ ", '"
								+ res.getString("Value")
								+ "', '"
								+ res.getString("UPC")
								+ "', 1000030, 1003655, 1001237, 1000010, 1000001,"
								+ " 1000040, 1001237, 1000000, '"
								+ NombreShopify.replaceAll("'", "")
								+ "', '"
								+ DireccionShopify.replaceAll("'", "")
								+ "', 1000000, '76281810', "
								+ m_AD_Client_ID
								+ ", " + " 'N','Y', sysdate, 100, sysdate, 100)",
						get_TrxName());
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Error al consultar ov_ordenesmuro@ERPMuro",
					e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (res != null)
					res.close();
			} catch (Exception ignore) {
			}
			try {
				if (pst != null)
					pst.close();
			} catch (Exception ignore) {
			}
		}
	}

	public int actualizaClient() {
		sql = new StringBuffer("UPDATE I_OrderB2C "
				+ "SET AD_Client_ID = COALESCE (AD_Client_ID,")
				.append(m_AD_Client_ID)
				.append(")," + " AD_Org_ID = COALESCE (AD_Org_ID,")
				.append(m_AD_Org_ID)
				.append(")," + " IsActive = COALESCE (IsActive, 'Y'),"
						+ " Created = COALESCE (Created, SysDate),"
						+ " CreatedBy = COALESCE (CreatedBy, 0),"
						+ " Updated = COALESCE (Updated, SysDate),"
						+ " UpdatedBy = COALESCE (UpdatedBy, 0),"
						+ " I_ErrorMsg = ' '," + " I_IsImported = 'N' "
						+ "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaBP() {
		sql = new StringBuffer(
				"UPDATE I_OrderB2C o "
						+ "SET C_BPartner_ID=(SELECT MAX(C_BPartner_ID) FROM C_BPartner bp"
						+ " WHERE trim(o.BPartnerValue)=trim(bp.Value) AND o.AD_Client_ID=bp.AD_Client_ID) "
						+ "WHERE C_BPartner_ID IS NULL AND BPartnerValue IS NOT NULL"
						+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaBPL() {
		sql = new StringBuffer(
				"UPDATE I_OrderB2C o "
						+ "SET (BillTo_ID,C_BPartner_Location_ID)=(SELECT max(C_BPartner_Location_ID)C_BPartner_Location_ID,max(C_BPartner_Location_ID)C_BPartner_Location_ID"
						+ " FROM C_BPartner_Location bpl INNER JOIN C_Location l ON (bpl.C_Location_ID=l.C_Location_ID)"
						+ " WHERE o.C_BPartner_ID=bpl.C_BPartner_ID AND bpl.AD_Client_ID=o.AD_Client_ID"
						+ " AND ( trim(upper(o.Address1))=trim(upper(l.Address1) )  or  trim(upper(o.Address1))=trim(upper(bpl.Name) )  ) )"
						+ "WHERE C_BPartner_ID IS NOT NULL AND C_BPartner_Location_ID IS NULL"
						+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaProduct() {
		sql = new StringBuffer(
				"UPDATE I_OrderB2C o "
						+ "SET M_Product_ID=(SELECT MAX(M_Product_ID) FROM M_Product p"
						+ " WHERE trim(o.ProductValue)=trim(p.Value) AND o.AD_Client_ID=p.AD_Client_ID) "
						+ "WHERE  ProductValue IS NOT NULL"
						+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int actualizaSalesRep() {
		sql = new StringBuffer(
				"UPDATE I_OrderB2C  "
						+ "SET SalesRep_ID=(SELECT MAX(u.AD_User_ID) FROM ad_user u"
						+ " WHERE trim(lower(ChargeName))=trim(lower(u.name)) AND AD_Client_ID=u.AD_Client_ID) "
						+ "WHERE ChargeName IS NOT NULL "
						+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int beforeImport(String document, String bpvalue) {
		sql = new StringBuffer(
				"UPDATE I_OrderB2C  "
						+ "SET I_IsImported='Y' , I_ERRORMSG = 'Orden Importada previamente' "
						+ "WHERE documentno= " + document
						+ " and bpartnervalue=" + bpvalue);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	private void ejecutarSQL(String sql) {
		Trx trx = Trx.get(Trx.createTrxName("newTrx"), true);
		try {
			trx.start();
			DB.executeUpdate(sql, trx.getTrxName());
			log.info("SQL=" + sql.toString());
			trx.commit();
		} catch (Exception e) {
			try {
				trx.rollback();
			} catch (Exception ignore) {
			}
			System.out.println(e.getMessage());
		} finally {
			try {
				trx.close();
			} catch (Exception ignore) {
			}
		}
	}

	@SuppressWarnings("unused")
	private void enviarCorreo(List<MOrder> lista, String mensajePie) {
		StringBuffer cuerpoMail = new StringBuffer();
		if (lista.size() > 0) {
			cuerpoMail
					.append("Se muestran a continuacion las ordenes insertadas en Adempiere");
			cuerpoMail.append("<br /><br />");
			cuerpoMail.append("<table border=\"1\">\r\n" + "  <tr>\r\n"
					+ "    <th>ID</th>\r\n" + "    <th>Nro Documento</th>\r\n"
					+ "    <th>Total lineas</th>\r\n"
					+ "    <th>Total</th>\r\n" + "    <th>Estado</th>\r\n"
					+ "  </tr>\r\n");
			for (MOrder order : lista) {
				for (MOrderLine line : order.getLines()) {
					cuerpoMail.append("  <tr>\r\n");
					cuerpoMail
							.append("    <td>" + order.get_ID() + "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getDocumentNo()
							+ "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getTotalLines()
							+ "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getGrandTotal()
							+ "</td>\r\n");
					cuerpoMail.append("    <td>" + order.getDocStatus()
							+ "</td>\r\n");
					cuerpoMail.append("  </tr>\r\n");
				}
			}
			cuerpoMail.append("</table>");
			cuerpoMail.append("<br /><br />");
			cuerpoMail.append(mensajePie);
		}

		MClient M_Client = new MClient(Env.getCtx(), null);
		String correoTo = "icastroruz@gmail.com";
		EMail email = M_Client.createEMail(
				correoTo,
				"Ordenes de Muro inyectadas "
						+ new Timestamp(System.currentTimeMillis()),
				cuerpoMail.toString(), true);
		EMail.SENT_OK.equals(email.send());
	}
}
