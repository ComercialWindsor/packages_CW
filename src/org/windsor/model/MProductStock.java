/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) Windsor                                                      *
 *****************************************************************************/
package org.windsor.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Properties;

import org.compiere.util.DB;

/**
 * Model class for M_ProductStock - Histórico diario de stock
 * 
 * @author Windsor
 * @version 1.0
 */
public class MProductStock extends X_M_ProductStock {

    private static final long serialVersionUID = 20260127L;

    /**
     * Constructor estándar
     * @param ctx contexto
     * @param M_ProductStock_ID ID (0 para nuevo)
     * @param trxName transacción
     */
    public MProductStock(Properties ctx, int M_ProductStock_ID, String trxName) {
        super(ctx, M_ProductStock_ID, trxName);
        if (M_ProductStock_ID == 0) {
            setDateTrx(new Timestamp(System.currentTimeMillis()));
            setWeekNo(getCurrentWeekNo());
        }
    }

    /**
     * Constructor de carga
     * @param ctx contexto
     * @param rs ResultSet
     * @param trxName transacción
     */
    public MProductStock(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

    /**
     * Constructor conveniente para crear registro
     * @param ctx contexto
     * @param M_Product_ID producto
     * @param dateTrx fecha
     * @param trxName transacción
     */
    public MProductStock(Properties ctx, int M_Product_ID, Timestamp dateTrx, String trxName) {
        this(ctx, 0, trxName);
        setM_Product_ID(M_Product_ID);
        setDateTrx(dateTrx);
        setWeekNo(getWeekNo(dateTrx));
    }

    /**
     * Obtiene número de semana ISO para una fecha
     * ISO-8601: Lunes es el primer día de la semana
     * @param date fecha
     * @return número de semana (1-53)
     */
    public static int getWeekNo(Timestamp date) {
        if (date == null) {
            return getCurrentWeekNo();
        }
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);  // ISO: Lunes primer día
        cal.setMinimalDaysInFirstWeek(4);        // ISO: Mínimo 4 días en primera semana
        cal.setTimeInMillis(date.getTime());
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Obtiene número de semana actual (ISO)
     * @return número de semana (1-53)
     */
    public static int getCurrentWeekNo() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Verifica si ya existe registro para producto/fecha
     * @param ctx contexto
     * @param AD_Client_ID cliente
     * @param M_Product_ID producto
     * @param dateTrx fecha
     * @param trxName transacción
     * @return true si existe
     */
    public static boolean exists(Properties ctx, int AD_Client_ID, int M_Product_ID, 
            Timestamp dateTrx, String trxName) {
        String sql = "SELECT COUNT(*) FROM M_ProductStock " +
                     "WHERE AD_Client_ID = ? AND M_Product_ID = ? AND TRUNC(DateTrx) = TRUNC(?)";
        int count = DB.getSQLValue(trxName, sql, AD_Client_ID, M_Product_ID, dateTrx);
        return count > 0;
    }

    /**
     * Elimina registros existentes para cliente/fecha
     * @param ctx contexto
     * @param AD_Client_ID cliente
     * @param dateTrx fecha
     * @param trxName transacción
     * @return cantidad eliminada
     */
    public static int deleteByDate(Properties ctx, int AD_Client_ID, Timestamp dateTrx, String trxName) {
        String sql = "DELETE FROM M_ProductStock WHERE AD_Client_ID = ? AND TRUNC(DateTrx) = TRUNC(?)";
        return DB.executeUpdate(sql, new Object[]{AD_Client_ID, dateTrx}, false, trxName);
    }

    /**
     * Setea valores desde resultado de función qtyavailableopenvia
     * Formato: qtydisponible;qtyhand;qtybom;qtypendiente;qtyres
     * @param result string con valores separados por ;
     */
    public void setFromQtyAvailableResult(String result) {
        if (result == null || !result.contains(";")) {
            setQtyAvailable(0);
            setQTYHAND(0);
            setQtyBOM(0);
            setQTYPENDIENTE(0);
            setQtyReserved(0);
            return;
        }
        
        String[] parts = result.split(";");
        if (parts.length >= 5) {
            setQtyAvailable(toInt(parts[0]));
            setQTYHAND(toInt(parts[1]));
            setQtyBOM(toInt(parts[2]));
            setQTYPENDIENTE(toInt(parts[3]));
            setQtyReserved(toInt(parts[4]));
        }
    }

    /**
     * Convierte string a int de forma segura
     * @param value valor
     * @return int o 0 si error
     */
    private int toInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            return bd.intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Antes de guardar
     * @param newRecord nuevo registro
     * @return true si continuar
     */
    protected boolean beforeSave(boolean newRecord) {
        // Asegurar WeekNo esté seteado
        if (getWeekNo() == 0 && getDateTrx() != null) {
            setWeekNo(getWeekNo(getDateTrx()));
        }
        return super.beforeSave(newRecord);
    }
}