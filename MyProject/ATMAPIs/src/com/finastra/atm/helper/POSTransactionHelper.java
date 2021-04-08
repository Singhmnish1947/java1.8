package com.finastra.atm.helper;

import java.util.Map;

import com.finastra.api.atm.v1.mapping.POSTransactionMapping;
import com.finastra.api.atm.v1.model.PosRequest;
import com.finastra.api.atm.v1.model.PosRequest.MessageFunctionEnum;
import com.finastra.api.utils.ATMTransactionUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOISOATM_ActivityUpdate;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

public class POSTransactionHelper {

	private IPersistenceObjectsFactory factory;
	private static final String ADVICE = "Advice";
	private static final String REPEATADVICE = "RepeatAdvice";

	public void updateATMActivity(Map<String, Object> essenceRs) {
		factory = BankFusionThreadLocal.getPersistanceFactory();
		factory.commitTransaction();
	}

	public void updateATMActivity(PosRequest posRequest, String errorCode, String errorCodeMsg) {

		factory = BankFusionThreadLocal.getPersistanceFactory();

		int forcePost = 0;

		String msgFunction = String.valueOf(posRequest.getMessageFunction());
		IBOATMActivityDetail iboATMActivityDetail = (IBOATMActivityDetail) factory
				.getStatelessNewInstance(IBOATMActivityDetail.BONAME);

		String atmActivityIdPk = GUIDGen.getNewGUID();

		iboATMActivityDetail.setBoID(atmActivityIdPk);

		iboATMActivityDetail.setF_SOURCECIB(posRequest.getAcquiringInstitutionIdentificationCode());
		iboATMActivityDetail.setF_ACCOUNTID(posRequest.getAccountIdentification1());
		if (CommonUtil.checkIfNotNullOrEmpty(posRequest.getPrimaryAccountNumberIdentifier())) {
			iboATMActivityDetail.setF_ATMCARDNUMBER(posRequest.getPrimaryAccountNumberIdentifier());
		} else {
			iboATMActivityDetail.setF_ATMCARDNUMBER("API");
		}
		iboATMActivityDetail.setF_ATMDEVICEID(posRequest.getCardAcceptorTerminalIdentification());
		iboATMActivityDetail.setF_ACCOUNTCURRENCY(posRequest.getAmountCurrencyCardholderBilling());
		iboATMActivityDetail.setF_MSGRECVDATETIME(posRequest.getTimeLocalTransaction());

		iboATMActivityDetail.setF_ATMTRANDESC(POSTransactionMapping.prepareMessageType(posRequest));
		String transactionCode = getTransactionCode((POSTransactionMapping.prepareMessageType(posRequest)),
				posRequest.getMessageFunction(), posRequest.getChannelId());
		iboATMActivityDetail.setF_ATMTRANSACTIONCODE(transactionCode);
		iboATMActivityDetail.setF_ERRORSTATUS(errorCode);
		iboATMActivityDetail.setF_ERRORDESC(errorCodeMsg);
		if (ADVICE.equals(msgFunction) || REPEATADVICE.equals(msgFunction)) {
			forcePost = 1;
		}
		iboATMActivityDetail.setF_FORCEPOST(forcePost);
		iboATMActivityDetail.setF_POSTDATETIME(posRequest.getDateTimeTransmission());
		iboATMActivityDetail.setF_TRANSACTIONDTTM(posRequest.getDateTimeTransmission());
		iboATMActivityDetail.setF_TRANSACTIONREFERENCE(posRequest.getRetrievalReferenceNumber());

		factory.create(IBOATMActivityDetail.BONAME, iboATMActivityDetail);
		updateATMActivityISOExtn(posRequest, atmActivityIdPk);
		factory.commitTransaction();
	}

	private void updateATMActivityISOExtn(PosRequest posRequest, String atmActivityIdPk) {

		IBOISOATM_ActivityUpdate iboISOATM_ActivityUpdate = (IBOISOATM_ActivityUpdate) factory
				.getStatelessNewInstance(IBOISOATM_ActivityUpdate.BONAME);

		iboISOATM_ActivityUpdate.setBoID(atmActivityIdPk);

		iboISOATM_ActivityUpdate.setF_UBACTUALTXNAMOUNT(posRequest.getAmountTransaction());
		iboISOATM_ActivityUpdate.setF_UBACTUALTXNFEE(posRequest.getAmountSettlement());
		iboISOATM_ActivityUpdate.setF_UBAQUIRERID(posRequest.getAcquiringInstitutionIdentificationCode());
		iboISOATM_ActivityUpdate.setF_UBCARDACCEPTORDATA(posRequest.getCardAcceptorNameLocation());
		iboISOATM_ActivityUpdate.setF_UBCARDACCEPTORID(posRequest.getCardAcceptorIdentificationCode());
		iboISOATM_ActivityUpdate.setF_UBCARDISSUERDATA(posRequest.getCardIssuerReferenceNumber());
		iboISOATM_ActivityUpdate.setF_UBPROCESSINGCODE(String.valueOf(posRequest.getProcessingCodeTransactionType()));
		iboISOATM_ActivityUpdate.setF_UBRECEIVINGINSTID(posRequest.getReceivingInstitutionIdentificationCode());
		factory.create(IBOISOATM_ActivityUpdate.BONAME, iboISOATM_ActivityUpdate);
	}

	
	public String getTransactionCode(String messageType, MessageFunctionEnum msgFunction, String channelId) {
		String param = messageType;
		return ATMTransactionUtil.getModuleConfigurationValue(param, param, null);
	}

}
