package com.finastra.api.atm.v1.mapping;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.constant.ATMEssenceRequestConstant;
import com.finastra.api.atm.v1.model.PosRequest;
import com.finastra.api.atm.v1.model.PosRequest.MessageFunctionEnum;
import com.finastra.api.atm.v1.model.PosRequest.ProcessingCodeTransactionTypeEnum;
import com.finastra.api.atm.v1.model.PosResponse;
import com.finastra.api.atm.v1.model.PosResponse.TransactionStatusEnum;
import com.finastra.api.utils.ATMTransactionUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;

public class POSTransactionMapping {

	HashMap<String, Object> financialMessageInput;

	public HashMap<String, Object> prepareEssenceRequest(PosRequest posRq) {

		String msgFunction = String.valueOf(posRq.getMessageFunction());
		ProcessingCodeTransactionTypeEnum processingCodeTransactionType = posRq.getProcessingCodeTransactionType();

		String messageTypeIdentifier = posRq.getMessageTypeIdentifier();
		String MTI = messageTypeIdentifier.substring(1);
		String cardId = "";

		if (("200").equals(MTI) || ("220").equals(MTI) || ("201").equals(MTI) || ("221").equals(MTI)
				|| ("420").equals(MTI) || ("421").equals(MTI) || ("100").equals(MTI) || ("101").equals(MTI)
				|| ("121").equals(MTI) || ("120").equals(MTI)) {
			posRq.setMessageTypeIdentifier(messageTypeIdentifier);
		} else {
			ATMTransactionUtil.handleEvent(40000123, new String[] { "messageTypeIdentifier", messageTypeIdentifier });
		}
		financialMessageInput = new HashMap<String, Object>();

		String dateTime = String.valueOf(posRq.getTimeLocalTransaction());
		Date date = Date.valueOf(dateTime.substring(0, 10));
		Time time = Time.valueOf(dateTime.substring(11, 19));

		if (("Online").equals(msgFunction)) {
			msgFunction = "";
		}

		financialMessageInput.put(ATMEssenceRequestConstant.POS_MESSAGE_TYPE, prepareMessageType(posRq));
		financialMessageInput.put(ATMEssenceRequestConstant.POS_PRODUCT_INDICATOR, posRq.getChannelId());

		financialMessageInput.put(ATMEssenceRequestConstant.POS_MSGFUNCTION, msgFunction);
		financialMessageInput.put(ATMEssenceRequestConstant.POS_PIN_DATA_52, msgFunction);
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ENVIRONMENT_60_2, posRq.getCurrencyCodeAmountFee());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_RETRIEVALREFERENCENO_37,
				posRq.getRetrievalReferenceNumber());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ACCOUNT_NUMBER_1_102,
				posRq.getAccountIdentification1());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_TRANSACTIONAMOUNT_4, posRq.getAmountTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ACTUALTRANSACTIONAMOUNT_95_1,
				posRq.getAmountTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CURRENCYCODE_49, posRq.getAmountCurrencyTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ISOCURRENCYCODE, posRq.getAmountCurrencyTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_SYSTEMSTRACEAUDITNUMBER_11,
				posRq.getSystemTraceAuditNumber());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ACQUIRINGINSTITUTIONID_32,
				posRq.getAcquiringInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_FORWARDINGINSTITUIONID_33,
				posRq.getForwardingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_TRANSACTIONTYPE_3_2, processingCodeTransactionType);
		financialMessageInput.put(ATMEssenceRequestConstant.POS_PROCESSING_CODE_3, processingCodeTransactionType);
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDHOLDERFEEAMT,
				posRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_TERMINAL_DATA_60, posRq.getCurrencyCodeAmountFee());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_AMOUNTRECON, posRq.getAmountSettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_AMOUNTRECONCURRENCY,
				posRq.getAmountCurrencySettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ACQUIRERFEE_95_2, posRq.getConversionRateSettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDHOLDERBILLINGAMT,
				posRq.getAmountCardholderBilling());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDHOLDERBILLINGCURRENCY,
				posRq.getAmountCurrencyCardholderBilling());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_TRANSACTIONFEEAMOUNT_28, posRq.getValueAmountFee());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ORIGINALTRANSACTIONTYPE_90_1,
				posRq.getOriginalAcquiringInstitutionIdentificationCode());
		if (("Reversal").equals(msgFunction) || ("RepeatReversal").equals(msgFunction) || ("Refund").equals(msgFunction)
				|| ("RepeatRefund").equals(msgFunction) || ("Cancel").equals(msgFunction)
				|| ("RepeatCancel").equals(msgFunction) || ("120").equals(MTI)) {
			financialMessageInput.put(ATMEssenceRequestConstant.POS_ORIGINALDATAELEMENTS_90,
					posRq.getOriginalSystemTraceAuditNumber());
			financialMessageInput.put(ATMEssenceRequestConstant.POS_ORIGINALSEQUENCENUMBER_90_2,
					posRq.getOriginalMessageTypeIdentifier());
			financialMessageInput.put(ATMEssenceRequestConstant.POS_ORIGINALTRANSACTIONDATE_90_3,
					posRq.getOriginalDateAndTimeLocalTransaction());
			financialMessageInput.put(ATMEssenceRequestConstant.POS_ORIGINALTXNAMT, posRq.getAmountOriginal());
		}
		if (("Refund").equals(msgFunction)) {
			financialMessageInput.put(ATMEssenceRequestConstant.POS_REFUNDTAG, msgFunction);
		}
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDACCEPTORID_42,
				posRq.getCardAcceptorIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDACCEPTORNAMELOC_43,
				posRq.getCardAcceptorNameLocation());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDACCEPTORTERMINALID_41,
				posRq.getCardAcceptorTerminalIdentification());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDISSUERAUTHORISER_61,
				posRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDISSUERAUTHORISER_61_6,
				posRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDISSUERFIID_61_2,
				posRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_ORIGINALCAPTUREDATE_90_5,
				posRq.getOriginalDateAndTimeLocalTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_RECEIVINGINSTITUTIONID_100,
				posRq.getReceivingInstitutionIdentificationCode());
		if (CommonUtil.checkIfNotNullOrEmpty(posRq.getPrimaryAccountNumberIdentifier())) {
			cardId = posRq.getPrimaryAccountNumberIdentifier();
		} else {
			cardId = "API";
		}
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDNUMBER_35, cardId);
		financialMessageInput.put(ATMEssenceRequestConstant.POS_CARDNUMBER_35_2, cardId);
		financialMessageInput.put(ATMEssenceRequestConstant.POS_TRANSMISSIONDATETIME_7,
				posRq.getTimeLocalTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.POS_LOCALTRANSACTIONSQLDATE_13, date);
		financialMessageInput.put(ATMEssenceRequestConstant.POS_LOCALTRANSACTIONSQLTIME_12, time);

		return financialMessageInput;
	}

	public PosResponse prepareSuccessResponse(PosRequest posRq, HashMap<String, Object> essenceResponse) {

		PosResponse posRs = new PosResponse();
		String emptyString = "";
		String msgFunction = String.valueOf(posRq.getMessageFunction());

		if (emptyString.equals(msgFunction)) {
			posRq.setMessageFunction(MessageFunctionEnum.ONLINE);
		}

		String version = String.valueOf(posRq.getMessageTypeIdentifier()).substring(0, 1);
		String MTI = String.valueOf(posRq.getMessageTypeIdentifier()).substring(1);

		if (MTI.equals(ATMAPIConstant.MTI_200) || MTI.equals(ATMAPIConstant.MTI_201)) {
			MTI = ATMAPIConstant.MTI_210;
		} else if (MTI.equals(ATMAPIConstant.MTI_220) || MTI.equals(ATMAPIConstant.MTI_221)) {
			MTI = ATMAPIConstant.MTI_230;
		} else if (MTI.equals(ATMAPIConstant.MTI_420) || MTI.equals(ATMAPIConstant.MTI_421)) {
			MTI = ATMAPIConstant.MTI_430;
		} else if (MTI.equals(ATMAPIConstant.MTI_100) || MTI.equals(ATMAPIConstant.MTI_101)) {
			MTI = ATMAPIConstant.MTI_110;
		} else if (MTI.equals(ATMAPIConstant.MTI_120) || MTI.equals(ATMAPIConstant.MTI_121)) {
			MTI = ATMAPIConstant.MTI_130;
		}

		MTI = version + MTI;
		posRq.setMessageTypeIdentifier(MTI);
		posRs.setBlockPOSpurchase(posRq);

		posRs.setTransactionStatus(TransactionStatusEnum.S);

		HashMap<String, BigDecimal> balances = AvailableBalanceFunction.run(posRq.getAccountIdentification1());
		BigDecimal ClearedBalance = (balances.get("AvailableBalance"))
				.subtract(balances.get("AvailableBalMinusClearedBal"));

		posRs.setAccountAvailableBalance((BigDecimal) balances.get("AvailableBalance"));
		posRs.setAccountAvailableBalanceWithoutCredit((BigDecimal) balances.get("AvailableBalanceWithOutCreditLimit"));
		posRs.setAccountLedgerBalance(ClearedBalance);
		return posRs;
	}

	public com.finastra.api.atm.v1.model.Error prepareFailureResponse(String errorCode, String errorCodeMsg) {

		return ATMTransactionUtil.createErrorResponse(errorCode, errorCodeMsg);
	}

	public static String prepareMessageType(PosRequest posRq) {
		String messageTypeIdentifier = posRq.getMessageTypeIdentifier();
		String MTI = String.valueOf(messageTypeIdentifier).substring(1);
		String messageType = "";

		if (MTI.equals("200") || MTI.equals("201") || MTI.equals("220") || MTI.equals("221") || MTI.equals("420")
				|| MTI.equals("421")) {
			messageType = "ATMAccPOS";
		}
		if (MTI.equals("100") || MTI.equals("101")) {
			messageType = "ATMDualAccPOS";
		}

		if (MTI.equals("120") || MTI.equals("121")) {
			messageType = "UpdateAccountHold";
		}
		return messageType;
	}
}
