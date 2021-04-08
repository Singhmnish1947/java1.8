package com.trapedza.bankfusion.fatoms;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

import com.misys.ub.payment.swift.DBUtils.MessageHeaderTable;
import com.misys.ub.payment.swift.DBUtils.RemittanceDetailsTable;
import com.misys.ub.payment.swift.posting.PostNonStpTransaction;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_OutwardRemittanceProcess;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

/**
 * @author Machamma.Devaiah
 *
 */
public class UB_SWT_OutwardRemittanceProcess extends AbstractUB_SWT_OutwardRemittanceProcess {
	/**
	 * @param args
	 */
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final transient Log LOGGER = LogFactory.getLog(UB_SWT_OutwardRemittanceProcess.class.getName());

	/**
	 * 
	 */
	public UB_SWT_OutwardRemittanceProcess() {
		super();
	}

	/**
	 * @param env
	 */
	@SuppressWarnings("deprecation")
	public UB_SWT_OutwardRemittanceProcess(BankFusionEnvironment env) {
		super(env);
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_OutwardRemittanceProcess#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		LOGGER.info(":::::::::::START UB_SWT_OutwardRemittanceProcess:::::::::::");
		UB_SWT_RemittanceProcessRq remittanceRq = getF_IN_RemittanceRq();
		RsHeader rsHeader = new RsHeader();
		RemittanceDetailsTable remitterTable = new RemittanceDetailsTable();
		PostNonStpTransaction postTxn = new PostNonStpTransaction();
		MessageStatus status = new MessageStatus();
		status.setOverallStatus("S");
		rsHeader.setStatus(status);
		rsHeader.setOrigCtxtId(StringUtils.EMPTY);
		//read the remittance table to get the remittance Status.
		IBOUB_SWT_RemittanceTable remittanceDtls = remitterTable.findByMessageId(remittanceRq.getREMITTANCE_ID());
		//if not equal to  P
		if (remittanceDtls!=null && !remittanceDtls.getF_UBREMITTANCESTATUS().equals(PaymentSwiftConstants.PROCESSED)) {
			//posting logic added here
			rsHeader = postTxn.postNonStpTransaction(remittanceRq, remittanceDtls);
			//update remittance and header status as Processed
			if(PaymentSwiftConstants.SUCCESS.equalsIgnoreCase(rsHeader.getStatus().getOverallStatus())){
					updateRemittanceStatus(remittanceDtls,remittanceRq);
					//remittanceIdPk
					rsHeader.setMessageType(remittanceDtls.getBoID());
			}
		}
		else{    
		    SubCode subCode = new SubCode();
            subCode.setCode(PaymentSwiftConstants.NO_RECORD_EXISTS);
            subCode.setDescription(CommonConstants.EMPTY_STRING);
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(PaymentSwiftConstants.ERROR_STATUS);
            status.addCodes(subCode);
		    status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
	        rsHeader.setStatus(status);
		    
		}
		if (!PaymentSwiftConstants.SUCCESS.equalsIgnoreCase(rsHeader.getStatus().getOverallStatus())) {
			PaymentSwiftUtils.handleEvent(Integer.parseInt(rsHeader.getStatus().getCodes(0).getCode()), new String[] { PaymentSwiftUtils.getEventParameter(rsHeader) });
		}

		//write into swift disposal table.
		LOGGER.info(":::::::::::END  UB_SWT_OutwardRemittanceProcess::::::::::::");
		setF_OUT_rsHeader(rsHeader);
	}

	/**
	 * @param remittanceDtls
	 */
	private void updateRemittanceStatus(IBOUB_SWT_RemittanceTable remittanceDtls,UB_SWT_RemittanceProcessRq remittanceRq) {
		RemittanceDetailsTable remitanceTableDtls = new RemittanceDetailsTable();
		MessageHeaderTable msgHeader = new MessageHeaderTable();
		if (remittanceDtls != null && !StringUtils.isEmpty(remittanceDtls.getBoID())) {
			//update remittance and header status as Processed
			remitanceTableDtls.updateRemittanceDetails(remittanceRq, PaymentSwiftConstants.PROCESSED, remittanceDtls);
			LOGGER.info("::::Update  RemittanceDetailsTable :::::" + remittanceDtls.getBoID());
			msgHeader.updateMessageHeader(remittanceDtls.getF_UBMESSAGEREFID(), PaymentSwiftConstants.PROCESSED,remittanceDtls.getBoID());
			LOGGER.info("::::Update MessageHeaderTable:::::::::::" + remittanceDtls.getF_UBMESSAGEREFID());
		}
	}
}
