package com.finastra.iso8583.atm.processes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOISOATM_ActivityUpdate;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

public class POSTransactionHelper {
	private IPersistenceObjectsFactory factory;
	private static final IBusinessInformationService BIZ_INFO_SERVICE;

	public static IBusinessInformationService getBizInfoService() {
		return BIZ_INFO_SERVICE;
	}

	static {
		BIZ_INFO_SERVICE = (IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
				.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

	}

	private static final String ADVICE = "Advice";
	private static final String REPEATADVICE = "RepeatAdvice";

	private static final String NONJMS = "NON-JMS";

	public void updateATMActivity(Map<String, Object> updateData) {


	}

    public void updateATMActivityError(HashMap<String, Object> essenceRq, String errorCode, String errorCodeMsg,
            Map<String, String> field48Parsed) {

		factory = BankFusionThreadLocal.getPersistanceFactory();

		int forcePost = 0;
		String msgFunction = String.valueOf(essenceRq.get("MsgFunction"));

		IBOATMActivityDetail iboATMActivityDetail = (IBOATMActivityDetail) factory
				.getStatelessNewInstance(IBOATMActivityDetail.BONAME);

		String atmActivityIdPk = GUIDGen.getNewGUID();

		iboATMActivityDetail.setBoID(atmActivityIdPk);

		iboATMActivityDetail.setF_SOURCECIB((String) essenceRq.get(ISOParsingConstants.acquiringInstitutionId_32));
		if (null != essenceRq.get(ISOParsingConstants.accountNumber1_102_2)) {
			iboATMActivityDetail.setF_ACCOUNTID((String) essenceRq.get(ISOParsingConstants.accountNumber1_102_2));
		} else {
			iboATMActivityDetail.setF_ACCOUNTID((String) essenceRq.get(ISOParsingConstants.accountNumber2_103_2));
		}
		if (CommonUtil.checkIfNotNullOrEmpty((String) essenceRq.get(ISOParsingConstants.cardNumber_35))) {
			iboATMActivityDetail.setF_ATMCARDNUMBER((String) essenceRq.get(ISOParsingConstants.cardNumber_35));
		} else {
			iboATMActivityDetail.setF_ATMCARDNUMBER(NONJMS);
		}

		String currencyCode = ISOParsingConstants.EMPTY_STRING;
        if (null != essenceRq.get(ISOParsingConstants.AmountReconCurrency_50)) {
			currencyCode = ATMTransactionUtil
                    .getCurrencyFromNumericCurrency((String) essenceRq.get(ISOParsingConstants.AmountReconCurrency_50));
		} else {
			if (null != (String) essenceRq.get(ISOParsingConstants.accountNumber1_102_2)) {
				currencyCode = ATMTransactionUtil
						.getCurrencyForAccount((String) essenceRq.get(ISOParsingConstants.accountNumber1_102_2));
			}
		}
		iboATMActivityDetail
				.setF_ISOCURRENCYCODE(Integer.parseInt((String) essenceRq.get(ISOParsingConstants.currencyCode_49)));
		iboATMActivityDetail.setF_ACCOUNTCURRENCY(currencyCode);
		iboATMActivityDetail.setF_MSGRECVDATETIME(
				Timestamp.valueOf((String) essenceRq.get(ISOParsingConstants.transmissionDateTime_7)));

		iboATMActivityDetail.setF_ATMDEVICEID((String) essenceRq.get(ISOParsingConstants.cardAcceptorTerminalId_41));
		iboATMActivityDetail.setF_ATMTRANDESC((String) essenceRq.get(ISOParsingConstants.Message_Type));
		String transactionCode = getTransactionCode((String) essenceRq.get(ISOParsingConstants.Message_Type),
				msgFunction, (String) essenceRq.get(ISOParsingConstants.CHANNELID));
		iboATMActivityDetail.setF_ATMTRANSACTIONCODE(transactionCode);
		iboATMActivityDetail.setF_ERRORSTATUS(errorCode);
		iboATMActivityDetail.setF_ERRORDESC(errorCodeMsg);
		if (ADVICE.equals(msgFunction) || REPEATADVICE.equals(msgFunction)) {
			forcePost = 1;
		}
		iboATMActivityDetail.setF_FORCEPOST(forcePost);
		iboATMActivityDetail.setF_POSTDATETIME(
				Timestamp.valueOf((String) essenceRq.get(ISOParsingConstants.transmissionDateTime_7)));
		iboATMActivityDetail.setF_TRANSACTIONDTTM(
				Timestamp.valueOf((String) essenceRq.get(ISOParsingConstants.transmissionDateTime_7)));
		iboATMActivityDetail
				.setF_TRANSACTIONREFERENCE((String) essenceRq.get(ISOParsingConstants.retrievalReferenceNo_37));
		iboATMActivityDetail.setF_UBCMSUNIQUEENDTXNREF(
				(String) essenceRq.get(ISOParsingConstants.uniqueEndTransactionReference_48_031));

		factory.create(IBOATMActivityDetail.BONAME, iboATMActivityDetail);
		
        updateATMActivityISOExtnError(essenceRq, atmActivityIdPk, field48Parsed);
	}

    private void updateATMActivityISOExtnError(HashMap<String, Object> essenceRq, String atmActivityIdPk,
            Map<String, String> field48Parsed) {

		IBOISOATM_ActivityUpdate iboISOATM_ActivityUpdate = (IBOISOATM_ActivityUpdate) factory
				.getStatelessNewInstance(IBOISOATM_ActivityUpdate.BONAME);

		iboISOATM_ActivityUpdate.setBoID(atmActivityIdPk);

		iboISOATM_ActivityUpdate
				.setF_UBACTUALTXNAMOUNT((BigDecimal) essenceRq.get(ISOParsingConstants.transactionAmount_4));
        iboISOATM_ActivityUpdate.setF_UBACTUALTXNFEE((BigDecimal) essenceRq.get(ISOParsingConstants.AmountRecon_5));
		iboISOATM_ActivityUpdate
				.setF_UBAQUIRERID((String) essenceRq.get(ISOParsingConstants.acquiringInstitutionId_32));
		iboISOATM_ActivityUpdate
				.setF_UBCARDACCEPTORDATA((String) essenceRq.get(ISOParsingConstants.cardAcceptorNameLoc_43));
		iboISOATM_ActivityUpdate.setF_UBCARDACCEPTORID((String) essenceRq.get(ISOParsingConstants.cardAcceptorId_42));
		iboISOATM_ActivityUpdate
				.setF_UBCARDISSUERDATA((String) essenceRq.get(ISOParsingConstants.receivingInstitutionId_100));
		iboISOATM_ActivityUpdate.setF_UBPROCESSINGCODE((String) essenceRq.get(ISOParsingConstants.Processing_Code_3));
		iboISOATM_ActivityUpdate
				.setF_UBRECEIVINGINSTID((String) essenceRq.get(ISOParsingConstants.receivingInstitutionId_100));
				
        iboISOATM_ActivityUpdate.setF_UBNETWORK((String) field48Parsed.get(ISOParsingConstants._026));
        iboISOATM_ActivityUpdate.setF_UBTERMINALTYPE((String) field48Parsed.get(ISOParsingConstants._002));
        iboISOATM_ActivityUpdate.setF_UBAUTHCODE((String) essenceRq.get(ISOParsingConstants.approvalCode));
        if (null != essenceRq.get(ISOParsingConstants.AmountRecon_5)) {
            iboISOATM_ActivityUpdate.setF_UBAMOUNTACCOUNT((BigDecimal) essenceRq.get(ISOParsingConstants.AmountRecon_5));
        }
        else {
            iboISOATM_ActivityUpdate.setF_UBAMOUNTACCOUNT(new BigDecimal(0));
        }
        iboISOATM_ActivityUpdate.setF_UBISMATCHEDTXN(ISOParsingConstants.IsMatchedUnmached);
				
		factory.create(IBOISOATM_ActivityUpdate.BONAME, iboISOATM_ActivityUpdate);
	}

	public String getTransactionCode(String messageType, String msgFunction, String channelId) {
		String param = messageType;
		return ATMTransactionUtil.getModuleConfigurationValue(param, param, null);
	}

}
