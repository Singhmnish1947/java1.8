package com.finastra.atm.helper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.constant.ATMEssenceRequestConstant;
import com.finastra.api.atm.v1.model.MiniStatementRequest;
import com.finastra.api.atm.v1.model.MiniStatementRequest.MessageFunctionEnum;
import com.finastra.api.utils.ATMTransactionUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOISOATM_ActivityUpdate;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;

public class ATMMiniStatementHelper {
	private IPersistenceObjectsFactory factory;
	private static final String REQUEST = "Request";
	private static final String REPEATREQUEST = "RepeatRequest";

	public void updateATMActivity(Map<String, Object> updateData) {

		factory = BankFusionThreadLocal.getPersistanceFactory();

		IBOATMActivityDetail iboATMActivityDetail = (IBOATMActivityDetail) factory
				.getStatelessNewInstance(IBOATMActivityDetail.BONAME);

		String atmActivityIdPk = GUIDGen.getNewGUID();

		iboATMActivityDetail.setBoID(atmActivityIdPk);

		iboATMActivityDetail.setF_ACCOUNTID((String) updateData.get("AccountId"));
		iboATMActivityDetail.setF_ACCOUNTCURRENCY((String) updateData.get("update_ACCOUNTCURRENCY"));
		iboATMActivityDetail.setF_AMOUNTDISPENSED((BigDecimal) updateData.get("update_AMOUNTDISPENSED"));
		if (CommonUtil.checkIfNotNullOrEmpty((String) updateData.get("update_ATMCARDNUMBER"))) {
			iboATMActivityDetail.setF_ATMCARDNUMBER((String) updateData.get("update_ATMCARDNUMBER"));
		} else {
			iboATMActivityDetail.setF_ATMCARDNUMBER("API");
		}
		iboATMActivityDetail.setF_ATMDEVICEID((String) updateData.get("update_ATMDEVICEID"));
		iboATMActivityDetail.setF_ATMTRANDESC((String) updateData.get("update_ATMTRANDESC"));
		iboATMActivityDetail.setF_ATMTRANSACTIONCODE((String) updateData.get("update_ATMTRANSACTIONCODE"));
		iboATMActivityDetail.setF_AUTHORIZEDFLAG((Integer) updateData.get("update_AUTHORIZEDFLAG"));
		iboATMActivityDetail.setF_BASEEQUIVALENT((BigDecimal) updateData.get("update_BASEEQUIVALENT"));
		iboATMActivityDetail.setF_CARDSEQUENCENUMBER((Integer) updateData.get("update_CARDSEQUENCENUMBER"));
		iboATMActivityDetail.setF_COMMAMOUNT((BigDecimal) updateData.get("update_COMMAMOUNT"));
		iboATMActivityDetail.setF_DESTACCOUNTID((String) updateData.get("update_DESTACCOUNTID"));
		iboATMActivityDetail.setF_DESTCIB((String) updateData.get("update_DESTCIB"));
		iboATMActivityDetail.setF_ERRORDESC((String) updateData.get("update_ERRORDESCRIPTION"));
		iboATMActivityDetail.setF_ERRORSTATUS((String) updateData.get("update_ERRORSTATUS"));
		iboATMActivityDetail.setF_FORCEPOST((Integer) updateData.get("update_FORCEPOST"));
		iboATMActivityDetail.setF_MISTRANSACTIONCODE((String) updateData.get("update_MISTRANSACTIONCODE"));
		iboATMActivityDetail.setF_MSGRECVDATETIME((Timestamp) updateData.get("update_MSGRECVDATETIME"));
		iboATMActivityDetail.setF_POSTDATETIME((Timestamp) updateData.get("update_POSTDATETIME"));
		iboATMActivityDetail.setF_SOURCECIB((String) updateData.get("update_SOURCECIB"));
		iboATMActivityDetail.setF_TRANSACTIONAMOUNT((BigDecimal) updateData.get("update_TRANSACTIONAMOUNT"));
		iboATMActivityDetail.setF_TRANSACTIONDTTM((Timestamp) updateData.get("update_TRANSACTIONDTTM"));
		iboATMActivityDetail.setF_TRANSACTIONID((String) updateData.get("update_TRANSACTIONID"));
		iboATMActivityDetail.setF_TRANSACTIONREFERENCE((String) updateData.get("update_TRANSACTIONREFERENCE"));
		iboATMActivityDetail.setF_UBISCHARGEWAIVED((Boolean)updateData.get("isChargeWaivedBasedOnCounter"));
		iboATMActivityDetail.setF_TRANSNARRATION((String) updateData.get("update_TRANSNARRATION"));
		iboATMActivityDetail.setF_TRANSSEQ((Integer) updateData.get("update_TRANSSEQ"));
		factory.create(IBOATMActivityDetail.BONAME, iboATMActivityDetail);
		UB_ATM_Financial_Details atmPosting = (UB_ATM_Financial_Details) updateData.get("update_atmPosting");

		updateATMActivityISOExtn(atmPosting, atmActivityIdPk, updateData);
		factory.commitTransaction();
	}

	private void updateATMActivityISOExtn(UB_ATM_Financial_Details atmPosting, String atmActivityIdPk,
			Map<String, Object> updateData) {

		IBOISOATM_ActivityUpdate iboISOATM_ActivityUpdate = (IBOISOATM_ActivityUpdate) factory
				.getStatelessNewInstance(IBOISOATM_ActivityUpdate.BONAME);

		iboISOATM_ActivityUpdate.setBoID(atmActivityIdPk);

		iboISOATM_ActivityUpdate.setF_UBACTUALTXNAMOUNT((BigDecimal) updateData.get("update_TRANSACTIONAMOUNT"));
		iboISOATM_ActivityUpdate.setF_UBACTUALTXNFEE(atmPosting.getReplacementAmount().getAcquirerFee());
		iboISOATM_ActivityUpdate.setF_UBAQUIRERID(atmPosting.getFinancialDetails().getAcquiringInstitutionId());
		iboISOATM_ActivityUpdate.setF_UBCAPTUREDTMON((String) updateData.get("update_DATEMON"));
		iboISOATM_ActivityUpdate.setF_UBCARDACCEPTORDATA(atmPosting.getFinancialDetails().getCardAcceptorNameLoc());
		iboISOATM_ActivityUpdate.setF_UBCARDACCEPTORID(atmPosting.getFinancialDetails().getCardAcceptorId());
		iboISOATM_ActivityUpdate.setF_UBCARDISSUERDATA(atmPosting.getCardIssuerData().getCardIssuerAuthoriser());
		iboISOATM_ActivityUpdate.setF_UBDEPOSITCREDITAMT(atmPosting.getDepositCreditAmount());
		iboISOATM_ActivityUpdate.setF_UBORIGINALTXNDATA((String) updateData.get("update_UBORIGINALTXNDATA"));
		iboISOATM_ActivityUpdate.setF_UBPROCESSINGCODE((String) updateData.get("update_UBPROCESSINGCODE"));
		iboISOATM_ActivityUpdate.setF_UBRECEIVINGINSTID(atmPosting.getReceivingInstitutionId());
		iboISOATM_ActivityUpdate
				.setF_UBSYSTEMTRACEAUDITNO(atmPosting.getFinancialDetails().getSystemsTraceAuditNumber());
		factory.create(IBOISOATM_ActivityUpdate.BONAME, iboISOATM_ActivityUpdate);
	}

	public void updateATMActivity(MiniStatementRequest miniStatement, String errorCode, String errorCodeMsg) {

		factory = BankFusionThreadLocal.getPersistanceFactory();

		int forcePost = 0;

		String messageFunction = String.valueOf(miniStatement.getMessageFunction());

		IBOATMActivityDetail iboATMActivityDetail = (IBOATMActivityDetail) factory
				.getStatelessNewInstance(IBOATMActivityDetail.BONAME);

		String atmActivityIdPk = GUIDGen.getNewGUID();

		iboATMActivityDetail.setBoID(atmActivityIdPk);

		iboATMActivityDetail.setF_SOURCECIB(miniStatement.getAcquiringInstitutionIdentificationCode());
		iboATMActivityDetail.setF_ACCOUNTID(miniStatement.getAccountIdentification1());

		iboATMActivityDetail.setF_MSGRECVDATETIME(miniStatement.getTimeLocalTransaction());
		iboATMActivityDetail.setF_ACCOUNTID(miniStatement.getAccountIdentification1());
		if (CommonUtil.checkIfNotNullOrEmpty(miniStatement.getPrimaryAccountNumberIdentifier())) {
			iboATMActivityDetail.setF_ATMCARDNUMBER(miniStatement.getPrimaryAccountNumberIdentifier());
		} else {
			iboATMActivityDetail.setF_ATMCARDNUMBER("API");
		}
		iboATMActivityDetail.setF_ATMDEVICEID(miniStatement.getCardAcceptorTerminalIdentification());
		iboATMActivityDetail.setF_ATMTRANDESC(ATMEssenceRequestConstant.ATM_CASH_WITHDRAWAL);
		String transactionCode = getTransactionCode((ATMEssenceRequestConstant.ATM_CASH_WITHDRAWAL),
				miniStatement.getMessageFunction(), miniStatement.getChannelId());
		iboATMActivityDetail.setF_ATMTRANSACTIONCODE(transactionCode);
		iboATMActivityDetail.setF_ERRORSTATUS(errorCode);
		iboATMActivityDetail.setF_ERRORDESC(errorCodeMsg);
		if (REQUEST.equals(messageFunction) || REPEATREQUEST.equals(messageFunction)) {
			forcePost = 1;
		}
		iboATMActivityDetail.setF_FORCEPOST(forcePost);
		iboATMActivityDetail.setF_POSTDATETIME(miniStatement.getDateTimeTransmission());
		iboATMActivityDetail.setF_TRANSACTIONDTTM(miniStatement.getDateTimeTransmission());
		iboATMActivityDetail.setF_TRANSACTIONREFERENCE(miniStatement.getRetrievalReferenceNumber());

		factory.create(IBOATMActivityDetail.BONAME, iboATMActivityDetail);
		updateATMActivityISOExtn(miniStatement, atmActivityIdPk);
		factory.commitTransaction();
	}

	private void updateATMActivityISOExtn(MiniStatementRequest miniStatement, String atmActivityIdPk) {

		IBOISOATM_ActivityUpdate iboISOATM_ActivityUpdate = (IBOISOATM_ActivityUpdate) factory
				.getStatelessNewInstance(IBOISOATM_ActivityUpdate.BONAME);

		iboISOATM_ActivityUpdate.setBoID(atmActivityIdPk);

		iboISOATM_ActivityUpdate.setF_UBAQUIRERID(miniStatement.getAcquiringInstitutionIdentificationCode());
		iboISOATM_ActivityUpdate.setF_UBCARDACCEPTORDATA(miniStatement.getCardAcceptorNameLocation());
		iboISOATM_ActivityUpdate.setF_UBCARDACCEPTORID(miniStatement.getCardAcceptorIdentificationCode());
		iboISOATM_ActivityUpdate.setF_UBCARDISSUERDATA(miniStatement.getCardIssuerReferenceNumber());
		iboISOATM_ActivityUpdate.setF_UBPROCESSINGCODE(ATMAPIConstant.processingCodeTransactionTypeMst);
		iboISOATM_ActivityUpdate.setF_UBRECEIVINGINSTID(miniStatement.getReceivingInstitutionIdentificationCode());
		factory.create(IBOISOATM_ActivityUpdate.BONAME, iboISOATM_ActivityUpdate);
	}

	public String getTransactionCode(String messageType, MessageFunctionEnum msgFunction, String channelId) {
		String param = "";
		String messageFunction = String.valueOf(msgFunction);
		if (REQUEST.equals(messageFunction) || REPEATREQUEST.equals(messageFunction)) {
			param = messageType;
		}
		return ATMTransactionUtil.getModuleConfigurationValue(param, param, null);
	}
}
