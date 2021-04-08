package com.misys.ub.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.ComplexTypeConvertorFactory;
import com.misys.bankfusion.common.IComplexTypeConvertor;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.microflow.IMFManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.IMicroflowHelper;
import com.trapedza.bankfusion.servercommon.microflow.MicroflowHelper;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_NonSTPPaymentResponseFatom;

import bf.com.misys.cbs.msgs.v1r0.TransferResponse;
import bf.com.misys.cbs.types.InstructionUpdate;
import bf.com.misys.cbs.types.InstructionUpdateItem;
import bf.com.misys.cbs.types.TransactionEvent;
import bf.com.misys.cbs.types.header.RsHeader;

/**
 * @author Anand.Pandey
 * @date July 26, 2018
 * @project Universal Banking
 * @Description: Code to handle Events 40430059,40430049.
 * 
 */

public class NonSTPPaymentResponseHandler extends AbstractUB_CMN_NonSTPPaymentResponseFatom {
	private transient final static Log logger = LogFactory.getLog(NonSTPPaymentResponseHandler.class.getName());
	private static final String ENDPOINT = "QM_BFDC_UB_Response";
	private static final String SUCCESS = "40430059";
	private static final String CANCEL = "40430049";
	public String responseMessage;

	@SuppressWarnings("deprecation")
	public NonSTPPaymentResponseHandler(BankFusionEnvironment env) {
		super(env);
	}

	@SuppressWarnings("deprecation")
	public NonSTPPaymentResponseHandler() {
		super(BankFusionThreadLocal.getBankFusionEnvironment());
	}

	@Override
	public void process(BankFusionEnvironment env) {
		try {
			postResponse(getF_IN_ChannelRef(), getF_IN_EventCode(), getF_IN_OrigChannelId(), getF_IN_PaymentReference(),
					env);
		} catch (Exception e) {
			logger.error("Exception occurred while creating response for payments", e);

		}
	}

	private void postResponse(String channelRef, String eventCode, String origChannelId, String paymentReference,
			BankFusionEnvironment env) throws Exception {

		TransferResponse transferResponse = new TransferResponse();

		RsHeader rsHeader = new RsHeader();
		rsHeader.setOrigCtxtId(origChannelId);
		transferResponse.setRsHeader(rsHeader);
		transferResponse.setReqPayload("REQUEST_PAYLOAD");

		InstructionUpdate instructionStatusUpdateNotification = new InstructionUpdate();
		InstructionUpdateItem[] insItem = new InstructionUpdateItem[1];

		for (int i = 0; i < 1; i++) {
			insItem[i] = new InstructionUpdateItem();
			insItem[i].setTransactionalItem(channelRef);
			TransactionEvent transactionEvent = new TransactionEvent();
			insItem[i].setTransactionEvent(transactionEvent);

			if (eventCode.equals(SUCCESS)) {
				transactionEvent.setReasonCode(SUCCESS);
				insItem[i].setNewStatus("PROCESS_SUCCESSFULLY");
				insItem[i].setTransactionId(paymentReference);
				String eventMessage = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventCode), new String[] {});
				insItem[i].getTransactionEvent().setDefaultMessage(eventMessage);
				insItem[i].getTransactionEvent().setFormattedMessage(eventMessage);
				insItem[i].setSoReference(channelRef);
			} else {
				transactionEvent.setReasonCode(CANCEL);
				insItem[i].setNewStatus("REJECTED");
				insItem[i].getTransactionEvent().setDefaultMessage(eventCode);
				insItem[i].getTransactionEvent().setFormattedMessage(eventCode);
			}
			
		}
		instructionStatusUpdateNotification.setInstructionUpdateItem(insItem);
		transferResponse.setInstructionStatusUpdateNotification(instructionStatusUpdateNotification);
		responseMessage = convertObjectToXMLString(env, transferResponse);
		postToServiceProviderQueue(responseMessage);
	}

	public static String convertObjectToXMLString(BankFusionEnvironment env, Object obj) {

		IMicroflowHelper microflowHelper = new MicroflowHelper(env);
		IMFManager mfManager = microflowHelper.getMFManager();
        ClassLoader cl = mfManager.getDynamicClassLoader();
		IComplexTypeConvertor complexTypeConvertor = ComplexTypeConvertorFactory.getComplexTypeConvertor(cl);
		return complexTypeConvertor.getXmlFromJava(obj.getClass().getName(), obj);
	}

	private static void postToServiceProviderQueue(String message) {
		logger.info("message sent from Essence is \n" + message);
		logger.info("---- Posting the message in the following queue " + ENDPOINT);
		MessageProducerUtil.sendMessage(message, ENDPOINT);
	}

}
