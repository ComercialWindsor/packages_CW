package org.cwindsor.model;

import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MOrderLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.model.MOrder;

/**
 * Bloquea la anulación (VOID) de C_Order cuando es compra (IsSOTrx='N')
 * para el usuario AD_User_ID = 1003845.
 *
 * Probado con Java 1.6 y ADempiere 3.9.x/360.
 */
public class ModBlockVoidPOForUser implements ModelValidator {

    private int m_AD_Client_ID = -1;
    /** Usuario restringido **/
    private static final int BLOCKED_USER_ID = 1003845;

    public ModBlockVoidPOForUser ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModBlockVoidPOForUser.class);
	/** Client			*/


	public void initialize (ModelValidationEngine engine, MClient client) {
    	//client = null for global validator
    			if (client != null) {
    				m_AD_Client_ID = client.getAD_Client_ID();
    				log.info(client.toString());
    			}
    			else  {
    				log.info("Initializing global validator: "+this.toString());
    			}
        // Registrar validación de documento para C_Order
        engine.addDocValidate(MOrder.Table_Name, this);
    }

    @Override
    public int getAD_Client_ID() {
        return m_AD_Client_ID;
    }

    
    public void setClient_ID(int AD_Client_ID) {
        m_AD_Client_ID = AD_Client_ID;
    }

    @Override
    public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
        return null; // sin-op
    }

    @Override
    public String docValidate(PO po, int timing) {
        if (!(po instanceof MOrder))
            return null;

        // Solo nos importa antes de VOID
        if (timing != ModelValidator.TIMING_BEFORE_VOID)
            return null;

        MOrder order = (MOrder) po;

        // Solo compras (IsSOTrx = N)
        if (order.isSOTrx())
            return null;

        // Usuario actual
        int currentUserId = Env.getAD_User_ID(order.getCtx());

        if (currentUserId == BLOCKED_USER_ID) {
            // lanzar excepción para bloquear
            throw new AdempiereException("No tiene permiso para anular órdenes de compra.");
        }

        return null;
    }

    @Override
    public String modelChange(PO po, int type) { 
        return null; 
    }

	
}
