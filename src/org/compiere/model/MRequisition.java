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
package org.compiere.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.ofb.model.OFBForward;
import org.ofb.process.ExportDTEMInOutCGProvectis;
import org.ofb.process.ExportDTEMInOutFOL;

/**
 *	Requisition Model
 *	
 *  @author Jorg Janke
 *
 *  @author victor.perez@e-evolution.com, e-Evolution http://www.e-evolution.com
 * 			<li> FR [ 2520591 ] Support multiples calendar for Org 
 *			@see http://sourceforge.net/tracker2/?func=detail&atid=879335&aid=2520591&group_id=176962 
 *  @version $Id: MRequisition.java,v 1.2 2006/07/30 00:51:05 jjanke Exp $
 *  @author red1
 *  		<li>FR [ 2214883 ] Remove SQL code and Replace for Query  
 *  @author Teo Sarca, www.arhipac.ro
 *  		<li>FR [ 2744682 ] Requisition: improve error reporting
 */
public class MRequisition extends X_M_Requisition implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 898606565778668659L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_Requisition_ID id
	 */
	public MRequisition (Properties ctx, int M_Requisition_ID, String trxName)
	{
		super (ctx, M_Requisition_ID, trxName);
		if (M_Requisition_ID == 0)
		{
		//	setDocumentNo (null);
		//	setAD_User_ID (0);
		//	setM_PriceList_ID (0);
		//	setM_Warehouse_ID(0);
			setDateDoc(new Timestamp(System.currentTimeMillis()));
			setDateRequired (new Timestamp(System.currentTimeMillis()));
			setDocAction (DocAction.ACTION_Complete);	// CO
			setDocStatus (DocAction.STATUS_Drafted);		// DR
			setPriorityRule (PRIORITYRULE_Medium);	// 5
			setTotalLines (Env.ZERO);
			setIsApproved (false);
			setPosted (false);
			setProcessed (false);
		}
	}	//	MRequisition

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRequisition (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MRequisition
	
	/** Lines						*/
	private MRequisitionLine[]		m_lines = null;
	
	/**
	 * 	Get Lines
	 *	@return array of lines
	 */
	public MRequisitionLine[] getLines()
	{
		if (m_lines != null) {
			set_TrxName(m_lines, get_TrxName());
			return m_lines;
		}
		
		//red1 - FR: [ 2214883 ] Remove SQL code and Replace for Query  
 	 	final String whereClause = I_M_RequisitionLine.COLUMNNAME_M_Requisition_ID+"=?";
	 	List <MRequisitionLine> list = new Query(getCtx(), I_M_RequisitionLine.Table_Name, whereClause, get_TrxName())
			.setParameters(get_ID())
			.setOrderBy(I_M_RequisitionLine.COLUMNNAME_Line)
			.list();
	 	//  red1 - end -

		m_lines = new MRequisitionLine[list.size ()];
		list.toArray (m_lines);
		return m_lines;
	}	//	getLines
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MRequisition[");
		sb.append(get_ID()).append("-").append(getDocumentNo())
			.append(",Status=").append(getDocStatus()).append(",Action=").append(getDocAction())
			.append ("]");
		return sb.toString ();
	}	//	toString
	
	/**
	 * 	Get Document Info
	 *	@return document info
	 */
	public String getDocumentInfo()
	{
		return Msg.getElement(getCtx(), "M_Requisition_ID") + " " + getDocumentNo();
	}	//	getDocumentInfo
	
	/**
	 * 	Create PDF
	 *	@return File or null
	 */
	public File createPDF ()
	{
		try
		{
			File temp = File.createTempFile(get_TableName()+get_ID()+"_", ".pdf");
			return createPDF (temp);
		}
		catch (Exception e)
		{
			log.severe("Could not create PDF - " + e.getMessage());
		}
		return null;
	}	//	getPDF

	/**
	 * 	Create PDF file
	 *	@param file output file
	 *	@return file if success
	 */
	public File createPDF (File file)
	{
	//	ReportEngine re = ReportEngine.get (getCtx(), ReportEngine.INVOICE, getC_Invoice_ID());
	//	if (re == null)
			return null;
	//	return re.getPDF(file);
	}	//	createPDF

	/**
	 * 	Set default PriceList
	 */
	public void setM_PriceList_ID()
	{
		MPriceList defaultPL = MPriceList.getDefault(getCtx(), false);
		if (defaultPL == null)
			defaultPL = MPriceList.getDefault(getCtx(), true);
		if (defaultPL != null)
			setM_PriceList_ID(defaultPL.getM_PriceList_ID());
	}	//	setM_PriceList_ID()
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		if (getM_PriceList_ID() == 0)
			setM_PriceList_ID();
		return true;
	}	//	beforeSave
	
	@Override
	protected boolean beforeDelete() {
		for (MRequisitionLine line : getLines()) {
			line.deleteEx(true);
		}
		return true;
	}

	/**************************************************************************
	 * 	Process document
	 *	@param processAction document action
	 *	@return true if performed
	 */
	public boolean processIt (String processAction)
	{
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
		return engine.processIt (processAction, getDocAction());
	}	//	process
	
	/**	Process Message 			*/
	private String			m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean 		m_justPrepared = false;

	/**
	 * 	Unlock Document.
	 * 	@return true if success 
	 */
	public boolean unlockIt()
	{
		log.info("unlockIt - " + toString());
		setProcessing(false);
		return true;
	}	//	unlockIt
	
	/**
	 * 	Invalidate Document
	 * 	@return true if success 
	 */
	public boolean invalidateIt()
	{
		log.info("invalidateIt - " + toString());
		return true;
	}	//	invalidateIt
	
	/**
	 *	Prepare Document
	 * 	@return new status (In Progress or Invalid) 
	 */
	public String prepareIt()
	{
		log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		MRequisitionLine[] lines = getLines();
		
		//	Invalid
		if (getAD_User_ID() == 0 
			|| getM_PriceList_ID() == 0
			|| getM_Warehouse_ID() == 0)
		{
			return DocAction.STATUS_Invalid;
		}
		if(!OFBForward.NoValidationReqNoLine())
		{
			if(lines.length == 0)
			{
				throw new AdempiereException("@NoLines@");
			}
		}		
		//	Std Period open?
		MPeriod.testPeriodOpen(getCtx(), getDateDoc(), MDocType.DOCBASETYPE_PurchaseRequisition, getAD_Org_ID());
		
		//	Add up Amounts
		int precision = MPriceList.getStandardPrecision(getCtx(), getM_PriceList_ID());
		BigDecimal totalLines = Env.ZERO;
		for (int i = 0; i < lines.length; i++)
		{
			MRequisitionLine line = lines[i];
			BigDecimal lineNet = line.getQty().multiply(line.getPriceActual());
			lineNet = lineNet.setScale(precision, BigDecimal.ROUND_HALF_UP);
			if (lineNet.compareTo(line.getLineNetAmt()) != 0)
			{
				line.setLineNetAmt(lineNet);
				line.saveEx();
			}
			totalLines = totalLines.add (line.getLineNetAmt());
		}
		if (totalLines.compareTo(getTotalLines()) != 0)
		{
			setTotalLines(totalLines);
			saveEx();
		}
		
		//faaguilar OFB begin
		//ininoles se saca validacion para DPP y se genera un model
		/*if(getDocBase().equals("RRC") && !isSOTrx()) //solicitud de materiales gore
		{
			for(MRequisitionLine line : lines)
			{
				if(line.getM_Product_ID()>0)
				{
					BigDecimal found = getQtyOnHand (line.getM_AttributeSetInstance_ID(), line.getM_Product_ID(),line.getParent().getM_Warehouse_ID());
					if(found.compareTo(line.getQty())<0)
					{
						m_processMsg = "No existe Stock para Producto "+ line.getM_Product().getName() + " linea " + line.getLine() + " encontrado :" + found.intValue();
						return DocAction.STATUS_Drafted;
					}
				}
			}
		}*/
		
		String validation=budgetValidation();
		//nuevos estados para TSM ininoles
		/*try
		{	
			if (OFBForward.ValidatorRequisitionTSM())
			{
				if(getDocStatus().compareTo("DR") == 0)
				{						
					return DocAction.STATUS_WaitingValorization;
				}
				if(getDocStatus().compareTo("WV") == 0)
				{						
					MUser userApro = new MUser(getCtx(), Env.getAD_User_ID(getCtx()), get_TrxName());
					BigDecimal amtToCmp = (BigDecimal)userApro.get_Value("AmtApproval");
					if (amtToCmp.compareTo(getTotalLines()) < 0)
					{
						return "Monto solicitud sobrepasa monto de usuario";
					}else
					{
						setProcessed(true);
						return DocAction.STATUS_WaitingConfirmation;
					}
				}
			}
		}catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}*/
		
		if(validation.length()>1)
			return validation;
		//faaguilar OFB end
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		m_justPrepared = true;
		return DocAction.STATUS_InProgress;
	}	//	prepareIt
	
	/**
	 * 	Approve Document
	 * 	@return true if success 
	 */
	public boolean  approveIt()
	{
		log.info("approveIt - " + toString());
		setIsApproved(true);
		return true;
	}	//	approveIt
	
	/**
	 * 	Reject Approval
	 * 	@return true if success 
	 */
	public boolean rejectIt()
	{
		log.info("rejectIt - " + toString());
		setIsApproved(false);
		return true;
	}	//	rejectIt
	
	/**
	 * 	Complete Document
	 * 	@return new status (Complete, In Progress, Invalid, Waiting ..)
	 */
	public String completeIt()
	{
		//	Re-Check
		if (!m_justPrepared)
		{
			String status = prepareIt();
			if (!DocAction.STATUS_InProgress.equals(status))
				return status;
		}

		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		//	Implicit Approval
		if (!isApproved())
			approveIt();
		log.info(toString());
		
		//	User Validation
		/*String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocAction.STATUS_Invalid;
		}*/

		// Set the definite document number after completed (if needed)
		setDefiniteDocumentNo();
		
		//faaguilar OFB begin
		String accion=completeOFB();
		if(accion.length()>1)
			return accion;
		//faaguilar OFB end

		//
		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocAction.STATUS_Invalid;
		}
		setProcessed(true);
		setDocAction(ACTION_Close);
		return DocAction.STATUS_Completed;
	}	//	completeIt
	
	/**
	 * 	Set the definite document number after completed
	 */
	private void setDefiniteDocumentNo() {
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		if (dt.isOverwriteDateOnComplete()) {
			setDateDoc(new Timestamp (System.currentTimeMillis()));
		}
		if (dt.isOverwriteSeqOnComplete()) {
			String value = DB.getDocumentNo(getC_DocType_ID(), get_TrxName(), true, this);
			if (value != null)
				setDocumentNo(value);
		}
	}

	/**
	 * 	Void Document.
	 * 	Same as Close.
	 * 	@return true if success 
	 */
	public boolean voidIt()
	{
		log.info("voidIt - " + toString());
		
		freeBudget();//faaguilar OFB
		
		// Before Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_VOID);
		if (m_processMsg != null)
			return false;
		
		if (!closeIt())
			return false;
		
		// After Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_VOID);
		if (m_processMsg != null)
			return false;
		
		return true;
	}	//	voidIt
	
	/**
	 * 	Close Document.
	 * 	Cancel not delivered Qunatities
	 * 	@return true if success 
	 */
	public boolean closeIt()
	{
		log.info("closeIt - " + toString());
		// Before Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
		if (m_processMsg != null)
			return false;

		//	Close Not delivered Qty
		MRequisitionLine[] lines = getLines();
		BigDecimal totalLines = Env.ZERO;
		for (int i = 0; i < lines.length; i++)
		{
			MRequisitionLine line = lines[i];
			BigDecimal finalQty = line.getQty();
			if (line.getC_OrderLine_ID() == 0)
				finalQty = Env.ZERO;
			else
			{
				MOrderLine ol = new MOrderLine (getCtx(), line.getC_OrderLine_ID(), get_TrxName());
				finalQty = ol.getQtyOrdered();
			}
			//	final qty is not line qty
			if (finalQty.compareTo(line.getQty()) != 0)
			{
				String description = line.getDescription();
				if (description == null)
					description = "";
				description += " [" + line.getQty() + "]"; 
				line.setDescription(description);
				line.setQty(finalQty);
				line.setLineNetAmt();
				line.saveEx();
			}
			// Si es reserva fisica Qty = QtyUsed
			if (getC_DocType_ID() == 1000111) {
				line.setQty(new BigDecimal(line.get_Value("QtyUsed").toString()));
				line.setLineNetAmt();
				line.saveEx();
			}
			totalLines = totalLines.add (line.getLineNetAmt());
		}
		if (totalLines.compareTo(getTotalLines()) != 0)
		{
			setTotalLines(totalLines);
			saveEx();
		}
		// After Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
		if (m_processMsg != null)
			return false;
		
		return true;
	}	//	closeIt
	
	/**
	 * 	Reverse Correction
	 * 	@return true if success 
	 */
	public boolean reverseCorrectIt()
	{
		log.info("reverseCorrectIt - " + toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		// After reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		return false;
	}	//	reverseCorrectionIt
	
	/**
	 * 	Reverse Accrual - none
	 * 	@return true if success 
	 */
	public boolean reverseAccrualIt()
	{
		log.info("reverseAccrualIt - " + toString());
		// Before reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;

		// After reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;				
		
		return false;
	}	//	reverseAccrualIt
	
	/** 
	 * 	Re-activate
	 * 	@return true if success 
	 */
	public boolean reActivateIt()
	{
		log.info("reActivateIt - " + toString());
		// Before reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REACTIVATE);
		if (m_processMsg != null)
			return false;

	//	setProcessed(false);
		if (! reverseCorrectIt())
			return false;

		// After reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REACTIVATE);
		if (m_processMsg != null)
			return false;

		return true;
	}	//	reActivateIt
	
	/*************************************************************************
	 * 	Get Summary
	 *	@return Summary of Document
	 */
	public String getSummary()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getDocumentNo());
		//	 - User
		sb.append(" - ").append(getUserName());
		//	: Total Lines = 123.00 (#1)
		sb.append(": ").
			append(Msg.translate(getCtx(),"TotalLines")).append("=").append(getTotalLines())
			.append(" (#").append(getLines().length).append(")");
		//	 - Description
		if (getDescription() != null && getDescription().length() > 0)
			sb.append(" - ").append(getDescription());
		return sb.toString();
	}	//	getSummary
	
	/**
	 * 	Get Process Message
	 *	@return clear text error message
	 */
	public String getProcessMsg()
	{
		return m_processMsg;
	}	//	getProcessMsg
	
	/**
	 * 	Get Document Owner
	 *	@return AD_User_ID
	 */
	public int getDoc_User_ID()
	{
		return getAD_User_ID();
	}
	
	/**
	 * 	Get Document Currency
	 *	@return C_Currency_ID
	 */
	public int getC_Currency_ID()
	{
		MPriceList pl = MPriceList.get(getCtx(), getM_PriceList_ID(), get_TrxName());
		return pl.getC_Currency_ID();
	}

	/**
	 * 	Get Document Approval Amount
	 *	@return amount
	 */
	public BigDecimal getApprovalAmt()
	{
		return getTotalLines();
	}
	
	/**
	 * 	Get User Name
	 *	@return user name
	 */
	public String getUserName()
	{
		return MUser.get(getCtx(), getAD_User_ID()).getName();
	}	//	getUserName

	/**
	 * 	Document Status is Complete or Closed
	 *	@return true if CO, CL or RE
	 */
	public boolean isComplete()
	{
		String ds = getDocStatus();
		return DOCSTATUS_Completed.equals(ds) 
			|| DOCSTATUS_Closed.equals(ds)
			|| DOCSTATUS_Reversed.equals(ds);
	}	//	isComplete
	
	
//faaguilar OFB custom methods
	
	/**faaguilar OFB
	 * verificacion de presupuesto
	 * */
	public String budgetValidation(){
		
		MClient client=MClient.get(Env.getCtx(), Env.getAD_Client_ID(Env.getCtx()));
		Boolean useBudget=false;
		try{
			useBudget=client.get_ValueAsBoolean("UseBudget");
		}
		catch(Exception e){}
		
		if(useBudget){
			MRole myrole = new MRole(getCtx(),Env.getAD_Role_ID(getCtx()),get_TrxName() );
			MUser currentUser = new MUser(getCtx(),Env.getAD_User_ID(getCtx()),get_TrxName() );
			String commenthelp=new String();
			String sql="select SUM(rl.LineNetAmt)as LineNetAmt,c.m_product_category_id,c.name, rl.AD_Org_ID from M_RequisitionLine rl"
				+ " inner join M_Product p on (rl.m_product_id=p.m_product_id)"
				+ " inner join M_Product_Category c on (p.m_product_category_id=c.m_product_category_id)"
				+ " where rl.m_requisition_id=?"
				+ " group by c.m_product_category_id,c.name,rl.AD_Org_ID";
			PreparedStatement pstmt = null;
			boolean sobrePresupuesto=false;
			boolean sobreanual=false;
			BigDecimal totalamt = Env.ZERO;
			try{
				pstmt = DB.prepareStatement (sql, get_TrxName());
				pstmt.setInt (1, getM_Requisition_ID()  );
				ResultSet rs = pstmt.executeQuery ();
				while (rs.next ())
				{

					BigDecimal monto = MConversionRate.convertBase(getCtx(), rs.getBigDecimal(1), getM_PriceList().getC_Currency_ID(), getDateDoc(), 0, getAD_Client_ID(), rs.getInt(4));
					if(monto==null)
					{
						m_processMsg = "No existe tasa de cambio";
						return DocAction.STATUS_Invalid;
					}
					log.config("monto :" + monto);
					totalamt = totalamt.add(monto);
					
					long ActualBudget=getBudget(rs.getInt(2),getDateDoc(),rs.getInt(4),0);//product 0
					long YearBudget =getBudgetYear(rs.getInt(2),MPeriod.get(getCtx(), getDateDoc()).getC_Year_ID() ,rs.getInt(4),0 );//product 0
					
					if(ActualBudget<monto.longValue()){
						
						long valor = 0;
						
						if(ActualBudget<=0)
							valor = (ActualBudget*-1) + monto.longValue();
						else
							valor = ActualBudget - monto.longValue();
						
						log.config("rs.getDouble(1): "+monto.doubleValue());
						log.config("ActualBudget: "+ActualBudget);
						log.config("valor: "+valor);
						commenthelp+=" | Categoria: "+rs.getString(3)+" Sobrepasado: "+valor;
						sobrePresupuesto=true;
						
						if(YearBudget<monto.doubleValue())
							sobreanual=true;
					}
				}
				rs.close ();
				pstmt.close ();
				pstmt = null;
			}
			catch(Exception e)
			{
				log.severe(e.getMessage());
			}
			 
			//nombre color alertas
			if(!sobrePresupuesto && !sobreanual)
				commenthelp="VERDE "+commenthelp;
			if(sobrePresupuesto && !sobreanual)
				commenthelp="AMARILLO "+commenthelp;
			if(sobreanual)
				commenthelp="ROJO "+commenthelp;
				
				if(!myrole.get_ValueAsBoolean("RequisitionAdmin") && !myrole.get_ValueAsBoolean("RequisitionSupervisor")){
					setProcessed(true);
					setHelp(commenthelp);
					return DocAction.STATUS_WaitingConfirmation;
				}
				else if(myrole.get_ValueAsBoolean("RequisitionSupervisor") && 
						(totalamt.doubleValue()>((BigDecimal)currentUser.get_Value("AmtApproval")).doubleValue()) ){
					setProcessed(true);
					setHelp(commenthelp);
					return DocAction.STATUS_WaitingConfirmation;
				}
				else if(myrole.get_ValueAsBoolean("RequisitionSupervisor") && 
						(totalamt.doubleValue()<((BigDecimal)currentUser.get_Value("AmtApproval")).doubleValue() &&  sobrePresupuesto) ){
					setProcessed(true);
					setHelp(commenthelp);
					return DocAction.STATUS_WaitingConfirmation;
				}
				else if(myrole.get_ValueAsBoolean("RequisitionAdmin") && ((BigDecimal)currentUser.get_Value("AmtApproval")).doubleValue()<totalamt.doubleValue() && ((BigDecimal)currentUser.get_Value("AmtApproval")).doubleValue()!=0 ){
					setProcessed(true);
					setHelp(commenthelp);
					return DocAction.STATUS_WaitingConfirmation;
				}
				else if(myrole.get_ValueAsBoolean("RequisitionAdmin") && ((BigDecimal)currentUser.get_Value("AmtApproval")).doubleValue()>totalamt.doubleValue() && sobreanual){
					setProcessed(true);
					setHelp(commenthelp);
					return DocAction.STATUS_WaitingConfirmation;
				}
				
			
		}
		return "";
	}
	
public long getBudget(int category_ID, Timestamp dateDoc, int Org_ID, int Product_ID){
		
	long actualBudget=0;
	boolean found = false;
	
	if(Product_ID>0)
	{
		String sql="select SUM(b.Budget-b.BudgetUsed) from m_product_category_Budget b"
				+ " inner join C_Period p on (b.C_Period_ID=p.C_Period_ID)"
				+ " where p.periodno between 1 and ?  and p.C_Year_ID=? and b.m_product_id=? and b.ad_org_id=?";
		PreparedStatement pstmt = null;
		MPeriod period= MPeriod.get(getCtx(), dateDoc);
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setInt (1, period.getPeriodNo()  );
			pstmt.setInt (2, period.getC_Year_ID()  );
			pstmt.setInt (3, Product_ID  );
			pstmt.setInt (4, Org_ID  );
			ResultSet rs = pstmt.executeQuery ();
			rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				actualBudget=rs.getLong(1);
				found = true;
			}
			
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "getBudget", e);
		}
		
	}//*
	
	if(Product_ID==0 || !found){
		String sql="select SUM(b.Budget-b.BudgetUsed) from m_product_category_Budget b"
				+ " inner join C_Period p on (b.C_Period_ID=p.C_Period_ID)"
				+ " where p.periodno between 1 and ?  and p.C_Year_ID=? and b.m_product_category_id=? and b.ad_org_id=?";
		PreparedStatement pstmt = null;
		MPeriod period= MPeriod.get(getCtx(), dateDoc);
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setInt (1, period.getPeriodNo()  );
			pstmt.setInt (2, period.getC_Year_ID()  );
			pstmt.setInt (3, category_ID  );
			pstmt.setInt (4, Org_ID  );
			ResultSet rs = pstmt.executeQuery ();
			rs = pstmt.executeQuery ();
			if (rs.next ())
				actualBudget=rs.getLong(1);
			
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "getBudget", e);
		}
	}//* 
	
	
		return actualBudget;
	}
	
	public long getBudgetYear(int category_ID, int Year_ID, int Org_ID, int Product_ID){
		
		long actualBudget=0;
		boolean found = false;
		
		if(Product_ID>0){
			String sql="select SUM(b.Budget-b.BudgetUsed) from m_product_category_Budget b"
					+ " inner join C_Period p on (b.C_Period_ID=p.C_Period_ID)"
					+ " where p.periodno between 1 and 12  and p.C_Year_ID=? and b.m_product_id=? and b.ad_org_id=?";
			
			PreparedStatement pstmt = null;
			
			try
			{
				pstmt = DB.prepareStatement (sql, get_TrxName());
				pstmt.setInt (1, Year_ID  );
				pstmt.setInt (2, Product_ID  );
				pstmt.setInt (3, Org_ID  );
				ResultSet rs = pstmt.executeQuery ();
				rs = pstmt.executeQuery ();
				if (rs.next ()){
					actualBudget=rs.getLong(1);
					found = true;
				}
				
				rs.close ();
				pstmt.close ();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "getBudget", e);
			}
			
		}//*
		
		if(Product_ID==0 || !found){
			String sql="select SUM(b.Budget-b.BudgetUsed) from m_product_category_Budget b"
					+ " inner join C_Period p on (b.C_Period_ID=p.C_Period_ID)"
					+ " where p.periodno between 1 and 12  and p.C_Year_ID=? and b.m_product_category_id=? and b.ad_org_id=?";
			PreparedStatement pstmt = null;
			
			try
			{
				pstmt = DB.prepareStatement (sql, get_TrxName());
				pstmt.setInt (1, Year_ID  );
				pstmt.setInt (2, category_ID  );
				pstmt.setInt (3, Org_ID  );
				ResultSet rs = pstmt.executeQuery ();
				rs = pstmt.executeQuery ();
				if (rs.next ())
					actualBudget=rs.getLong(1);
				
				rs.close ();
				pstmt.close ();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "getBudget", e);
			}
		}//*	
			
		return actualBudget;
			
	}
	
	/**faaguilar OFB completeOFB*/
	public String completeOFB(){
		
		DB.executeUpdate("update M_REQUISITIONLINE set Processed='Y' where M_REQUISITION_ID="+getM_Requisition_ID(),get_TrxName());
		
		
		if(isSOTrx()) //faaguilar OFB reservar para solicitudes de venta
		{
			MRequisitionLine[] lines = getLines();
			for (int i = 0; i < lines.length; i++)
			{
				MRequisitionLine line = lines[i];
				
				if (line.getM_Product_ID() >0) 
				{
					MProduct product=new MProduct(getCtx(),line.getM_Product_ID(),get_TrxName());
					if (product.isStocked())
					{
						MWarehouse wh = MWarehouse.get(getCtx(), getM_Warehouse_ID());
						if (!MStorage.add(getCtx(), wh.getM_Warehouse_ID(),wh.getDefaultLocator().getM_Locator_ID(), 
								line.getM_Product_ID(), 
								line.getM_AttributeSetInstance_ID(), line.getM_AttributeSetInstance_ID(),
								Env.ZERO, line.getQty(), Env.ZERO, get_TrxName())){
								m_processMsg = "No se puede Reservar";
								return DocAction.STATUS_Invalid;
						}
					}
				}
			}
		}
		
		//faaguilar OFB requisition presupuesto product begin
		if("Y".equals(DB.getSQLValueString(get_TrxName(), "select UseBudget from AD_Client where AD_Client_ID="+getAD_Client_ID())) && !isSOTrx()){
			String sql="select SUM(rl.LineNetAmt)as LineNetAmt,c.m_product_category_id, rl.AD_Org_ID from M_RequisitionLine rl"
				+ " inner join M_Product p on (rl.m_product_id=p.m_product_id)"
				+ " inner join M_Product_Category c on (p.m_product_category_id=c.m_product_category_id)"
				+ " where rl.m_requisition_id=?"
				+ " group by c.m_product_category_id, rl.AD_Org_ID";
			PreparedStatement pstmt = null;
			MPeriod period= MPeriod.get(getCtx(), getDateDoc());
            
			try{
				pstmt = DB.prepareStatement (sql, get_TrxName());
				pstmt.setInt (1, getM_Requisition_ID()  );
				ResultSet rs = pstmt.executeQuery ();
				while (rs.next ())
				{
					
					BigDecimal monto = MConversionRate.convertBase(getCtx(), rs.getBigDecimal(1), getM_PriceList().getC_Currency_ID(), getDateDoc(), 0, getAD_Client_ID(), rs.getInt(3));
					if(monto==null)
					{
						m_processMsg = "No existe tasa de cambio";
						return DocAction.STATUS_Invalid;
					}
					DB.executeUpdate("Update m_product_category_Budget set BudgetUsed=BudgetUsed+"+monto.doubleValue()
							+" where m_product_category_id="+rs.getInt(2)+" And AD_Org_ID="+rs.getInt(3)+ " and c_period_ID="+period.getC_Period_ID(), get_TrxName());
				}
				rs.close ();
				pstmt.close ();
				pstmt = null;
			}
			catch(Exception e)
			{
				log.severe(e.getMessage());
			}
		}		
		if(getDocBase().equals("RRC") && !isSOTrx()) //solicitud de materiales gore
		{
			String clientName = MClient.get(getCtx(), getAD_Client_ID()).getName().toUpperCase();
			MRequisitionLine[] lines = getLines();
			MInventory inv = null;
			MWarehouse wh = MWarehouse.get(getCtx(), getM_Warehouse_ID(), get_TrxName());
			if(OFBForward.ValidStockMaterialReq())
			{
				for (int a = 0; a < lines.length; a++)
				{
					MRequisitionLine line = lines[a];
					if (line.getM_Product_ID() >0)
					{
						BigDecimal qtyEntered = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(qtyonhand) FROM M_Storage " +
								" WHERE M_Product_ID="+line.getM_Product_ID()+" AND M_Locator_ID="+wh.getDefaultLocator().getM_Locator_ID());
						BigDecimal qtyIntransit = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(ml.QtyInternalUse) FROM M_InventoryLine ml " +
								" INNER JOIN M_Inventory mi ON (mi.M_Inventory_ID = ml.M_Inventory_ID) " +
								" WHERE ml.M_Product_ID="+line.getM_Product_ID()+" AND ml.M_Locator_ID="+wh.getDefaultLocator().getM_Locator_ID()+
								" AND mi.DocStatus IN ('DR','IP')");
						if(qtyEntered == null)
							qtyEntered = Env.ZERO;
						if(qtyIntransit == null)
							qtyIntransit = Env.ZERO;
						qtyEntered = qtyEntered.subtract(qtyIntransit);
						
						if(line.getQty().compareTo(qtyEntered) > 0)
						{
							MLocator loc = new MLocator(getCtx(), wh.getDefaultLocator().getM_Locator_ID(), get_TrxName());
							String ret = "ERROR: Producto "+line.getM_Product().getName()+" sin stock. Stock:"+qtyEntered+
							". Solicitado:"+line.getQty()+" en ubicaciòn "+loc.getValue();
							//throw new AdempiereException(ret);							
							m_processMsg = ret;
							return DocAction.STATUS_Invalid;
						}
					}
				}
			}
			for (int i = 0; i < lines.length; i++)
			{
				MRequisitionLine line = lines[i];
				if (line.getM_Product_ID() >0)
				{
					if(inv == null)
					{  
						inv = new MInventory(getCtx(),0,get_TrxName());
						inv.setAD_Org_ID(getAD_Org_ID());
						inv.setM_Warehouse_ID(getM_Warehouse_ID());						
						if(clientName.indexOf("GOBIERNO")>0 )
							inv.setC_DocType_ID(1000023);
						else if(clientName.indexOf("GEMINIS")>0 )
							inv.setC_DocType_ID(1000058);
						inv.set_CustomColumn("AD_User_ID", getAD_User_ID());						
						
						//ininoles se setea nuevo campo en cabecera de m_inventory 
						try{
							inv.set_CustomColumn("M_Requisition_ID", get_ID());
						}
						catch (Exception e)
						{
							log.log(Level.SEVERE,"No se pudo asignar la variable M_Requisition_ID en M_Inventory",e);							
						}
						//end ininoles
						
						inv.set_CustomColumn("AD_User_ID", getAD_User_ID());						
						inv.save();						
					}					
					MInventoryLine il = new MInventoryLine(getCtx(),0,get_TrxName());
					il.setM_Inventory_ID(inv.getM_Inventory_ID() );
					il.setAD_Org_ID(getAD_Org_ID());
					il.setM_Product_ID(line.getM_Product_ID());
					il.setM_Locator_ID(wh.getDefaultLocator().getM_Locator_ID());
					il.setQtyInternalUse(line.getQty());
					il.setC_Charge_ID(get_ValueAsInt("C_Charge_ID"));
					il.save();					
				}			
			}			
			if(inv!=null)
				set_CustomColumn("M_Inventory_ID", inv.getM_Inventory_ID());
		}
		
		if(!isSOTrx()) //faaguilar OFB solicitud de compra con rfq
		{
			
			MRequisitionLine[] lines = getLines();
			MRfQ rqf = null;
			BigDecimal total = Env.ZERO;
			for (int i = 0; i < lines.length; i++)
			{
				MRequisitionLine line = lines[i];
				
				if (line.getM_Product_ID() >0 && line.get_ValueAsBoolean("IsRfQ"))
				{
					
					if(rqf == null && get_ValueAsInt("C_RfQ_ID")==0)
					{
						rqf = new MRfQ(getCtx(),0,get_TrxName());
						rqf.setSalesRep_ID(getAD_User_ID());
						rqf.set_CustomColumn("M_Requisition_ID", getM_Requisition_ID());
						rqf.setDescription(getDescription());
						rqf.set_CustomColumn("M_PriceList_ID", getM_PriceList_ID());
						rqf.setAD_Org_ID(getAD_Org_ID());
						rqf.setName(getDocumentNo()+" " + getDescription());
						rqf.setC_Currency_ID(getM_PriceList().getC_Currency_ID());
						rqf.save();
					}
					else if(rqf == null && get_ValueAsInt("C_RfQ_ID")>0)
					{
						rqf = new MRfQ(getCtx(),get_ValueAsInt("C_RfQ_ID"),get_TrxName());
					}
					
					MRfQLine rfqline = new MRfQLine(rqf);
					rfqline.setAD_Org_ID(line.getAD_Org_ID());
					rfqline.setLine(line.getLine());
					rfqline.setM_Product_ID(line.getM_Product_ID());
					rfqline.setDescription(line.getDescription());
					rfqline.set_CustomColumn("Qty", line.getQty());
					rfqline.set_CustomColumn("PriceActual", line.getPriceActual());
					rfqline.set_CustomColumn("LineNetAmt", line.getLineNetAmt());
					rfqline.set_CustomColumn("M_RequisitionLine_ID", line.getM_RequisitionLine_ID());
					rfqline.save();
					
					total = total.add(line.getLineNetAmt());
				}
				
			}
			if(rqf!=null){
				rqf.set_CustomColumn("TotalAmt", total);
				rqf.setSalesRep_ID(getAD_User_ID());
				rqf.save();
			
				set_CustomColumn("C_RfQ_ID", rqf.getC_RfQ_ID());
			}
			
			
		}//faaguilar OFB
		
		
		
		return "";
		
	}
	
	public boolean isSOTrx()
	{
		boolean issotrx="Y".equals(DB.getSQLValueString(get_TrxName(), "Select IsSoTrx from C_DocType where C_DocType_ID="+getC_DocType_ID()));
		
		return issotrx;
	}
	
	public String getDocBase()
	{
		String base = DB.getSQLValueString(get_TrxName(), "Select docbasetype from C_DocType where C_DocType_ID="+getC_DocType_ID());
		return base;
	}
	
   private BigDecimal getQtyOnHand (int M_AttributeSetInstance_ID, int M_Product_ID, int M_Warehouse_ID) {
		
		
		log.config("M_Warehouse_ID: "+ M_Warehouse_ID + "M_Product_ID: "+M_Product_ID + " M_AttributeSetInstance_ID: "+M_AttributeSetInstance_ID);
		BigDecimal bd = null;
		String sql = "SELECT sum(s.QtyOnHand) FROM M_Storage s " +
				" INNER JOIN M_Locator l on (l.M_Locator_ID=s.M_locator_ID) "
			+ "WHERE s.M_Product_ID=?"	//	1
			+ " AND l.M_Warehouse_ID=?";		//	2
		if (M_AttributeSetInstance_ID > 0)
			sql = "SELECT sum(s.QtyOnHand) FROM M_Storage s " +
			" INNER JOIN M_Locator l on (l.M_Locator_ID=s.M_locator_ID) "
		+ "WHERE s.M_Product_ID=?"	//	1
		+ " AND l.M_Warehouse_ID=?"		//	2
		+ " AND s.M_AttributeSetInstance_ID=?";
		
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, M_Product_ID);
			pstmt.setInt(2, M_Warehouse_ID);
			if (M_AttributeSetInstance_ID > 0)
				pstmt.setInt(3, M_AttributeSetInstance_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				
				bd = rs.getBigDecimal(1);
				log.config("QtyOnHand: "+ bd);
				if (bd != null)
					return bd;
			} 
			else {
				
				return new BigDecimal(0);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
			return new BigDecimal(0);
		}
		
		
		return new BigDecimal(0);
	}
  
  
  /**
	 * faaguilar OFB
	 * actualiza el presupuesto al anular requisition*/
	private void freeBudget(){
		
		if(getDocStatus().equals(STATUS_Completed))
			if("Y".equals(DB.getSQLValueString(get_TrxName(), "select UseBudget from AD_Client where AD_Client_ID="+getAD_Client_ID())) && !isSOTrx()){
				
				MRequisitionLine[] lines = getLines();
				for(MRequisitionLine line:lines){
						
						MPeriod period= MPeriod.get(getCtx(), line.getParent().getDateDoc());
						
						if(!line.get_ValueAsBoolean("IsCorrected")){
							DB.executeUpdate("Update m_product_category_Budget set BudgetUsed=BudgetUsed - "+ line.getLineNetAmt().doubleValue()
									+" where m_product_category_id="+ line.getM_Product().getM_Product_Category_ID() +" And AD_Org_ID="+ line.getAD_Org_ID()+ " and c_period_ID="+period.getC_Period_ID(), get_TrxName());
						
							line.set_CustomColumn("IsCorrected", true);
							line.save();
						}
						
						
				}//end for
				
			}//end if
		
	}//freeBudget
	
}	//	MRequisition

