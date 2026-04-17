/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) Windsor                                                      *
 *****************************************************************************/
package org.windsor.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.windsor.model.MProductStock;

/**
 * Proceso para generar histórico diario de stock por producto.
 * Crea registros en M_ProductStock para productos activos tipo Artículo.
 * Procesa en lotes de 2000 registros para optimizar performance.
 * 
 * @author Windsor
 * @version 1.0
 */
public class GenerateProductStock extends SvrProcess {

    /** Tamaño del lote */
    private static final int BATCH_SIZE = 2000;
    
    /** Fecha de transacción */
    private Timestamp p_DateTrx = null;
    
    /** Client ID */
    private int p_AD_Client_ID = 0;
    
    /** Contadores */
    private int m_count = 0;
    private int m_errors = 0;
    private int m_totalProducts = 0;

    /**
     * Preparar parámetros
     */
    protected void prepare() {
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null) {
                // skip
            } else if (name.equals("DateTrx")) {
                p_DateTrx = (Timestamp) para[i].getParameter();
            }
        }
        
        if (p_DateTrx == null) {
            p_DateTrx = new Timestamp(System.currentTimeMillis());
        }
        p_AD_Client_ID = Env.getAD_Client_ID(getCtx());
    }

    /**
     * Ejecutar proceso
     */
    protected String doIt() throws Exception {
        log.info("Iniciando GenerateProductStock - DateTrx=" + p_DateTrx 
                + ", AD_Client_ID=" + p_AD_Client_ID + ", BatchSize=" + BATCH_SIZE);
        
        String trxName = get_TrxName();
        
        // 1. Contar productos
        m_totalProducts = countProducts(trxName);
        log.info("Total productos a procesar: " + m_totalProducts);
        
        if (m_totalProducts == 0) {
            return "No hay productos activos tipo Artículo para procesar";
        }
        
        // 2. Eliminar registros del día
        int deleted = MProductStock.deleteByDate(getCtx(), p_AD_Client_ID, p_DateTrx, trxName);
        if (deleted > 0) {
            log.info("Registros eliminados del día: " + deleted);
        }
        
        // 3. Procesar por lotes
        int totalBatches = (m_totalProducts + BATCH_SIZE - 1) / BATCH_SIZE;
        log.info("Procesando en " + totalBatches + " lotes de " + BATCH_SIZE);
        
        for (int batch = 0; batch < totalBatches; batch++) {
            int offset = batch * BATCH_SIZE;
            processBatch(offset, BATCH_SIZE, trxName);
            
            int processed = Math.min((batch + 1) * BATCH_SIZE, m_totalProducts);
            log.info("Progreso: " + processed + "/" + m_totalProducts 
                    + " (" + (processed * 100 / m_totalProducts) + "%)");
            
            commitEx();
        }
        
        String msg = "Proceso completado. Registros: " + m_count + "/" + m_totalProducts;
        if (m_errors > 0) {
            msg += " - Errores: " + m_errors;
        }
        
        return msg;
    }
    
    /**
     * Cuenta productos a procesar
     */
    private int countProducts(String trxName) {
        String sql = "SELECT COUNT(*) FROM M_Product " +
                     "WHERE AD_Client_ID = ? AND IsActive = 'Y' AND ProductType = 'I'";
        return DB.getSQLValue(trxName, sql, p_AD_Client_ID);
    }
    
    /**
     * Procesa un lote de productos
     */
    private void processBatch(int offset, int limit, String trxName) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT M_Product_ID, AD_Org_ID, Value FROM (");
        sql.append("  SELECT p.M_Product_ID, p.AD_Org_ID, p.Value, ");
        sql.append("         ROW_NUMBER() OVER (ORDER BY p.M_Product_ID) AS rn ");
        sql.append("  FROM M_Product p ");
        sql.append("  WHERE p.AD_Client_ID = ? ");
        sql.append("    AND p.IsActive = 'Y' ");
        sql.append("    AND p.ProductType = 'I'");
        sql.append(") WHERE rn > ? AND rn <= ?");
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = DB.prepareStatement(sql.toString(), trxName);
            pstmt.setInt(1, p_AD_Client_ID);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, offset + limit);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int M_Product_ID = rs.getInt("M_Product_ID");
                int AD_Org_ID = rs.getInt("AD_Org_ID");
                String productValue = rs.getString("Value");
                
                try {
                    processProduct(M_Product_ID, AD_Org_ID, trxName);
                    m_count++;
                } catch (Exception e) {
                    m_errors++;
                    log.log(Level.WARNING, "Error producto " + productValue 
                            + " (ID=" + M_Product_ID + "): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error procesando lote offset=" + offset, e);
        } finally {
            DB.close(rs, pstmt);
            rs = null;
            pstmt = null;
        }
    }
    
    /**
     * Procesa un producto individual
     */
    private void processProduct(int M_Product_ID, int AD_Org_ID, String trxName) {
        // Crear registro usando modelo
        MProductStock stock = new MProductStock(getCtx(), M_Product_ID, p_DateTrx, trxName);
        stock.setAD_Org_ID(AD_Org_ID);
        
        // Existencia de RV_Storage
        int qtyOnHand = getQtyOnHand(M_Product_ID, trxName);
        if (qtyOnHand<0)
        	qtyOnHand=0;
        stock.setQtyOnHand(qtyOnHand);
        
        // Disponible desde función
        String resultDisp = getQtyAvailableResult(M_Product_ID, trxName);
        stock.setFromQtyAvailableResult(resultDisp);
        
        // Guardar
        if (!stock.save()) {
            throw new RuntimeException("Error guardando M_ProductStock para producto " + M_Product_ID);
        }
    }
    
    /**
     * Obtiene existencia de RV_Storage
     */
    private int getQtyOnHand(int M_Product_ID, String trxName) {
        String sql = "SELECT COALESCE(SUM(QtyOnHand), 0) FROM RV_Storage WHERE M_Product_ID = ?";
        BigDecimal qty = DB.getSQLValueBD(trxName, sql, M_Product_ID);
        return qty != null ? qty.intValue() : 0;
    }
    
    /**
     * Obtiene resultado de función qtyavailableopenvia
     */
    private String getQtyAvailableResult(int M_Product_ID, String trxName) {
        String sql = "SELECT qtyavailableopenvia(?) FROM DUAL";
        return DB.getSQLValueString(trxName, sql, M_Product_ID);
    }
}
