package com.finastra.api.atm.v1.mapping;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.constant.ATMEssenceRequestConstant;
import com.finastra.api.atm.v1.model.BalanceEnquiryRequest;
import com.finastra.api.atm.v1.model.BalanceEnquiryRequest.MessageFunctionEnum;
import com.finastra.api.atm.v1.model.BalanceEnquiryResponse;
import com.finastra.api.utils.ATMTransactionUtil;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;

public class ATMBalanceEnquiryMapping {

	HashMap<String, Object> financialMessageInput;

	public HashMap<String, Object> prepareEssenceRequest(BalanceEnquiryRequest balEnqRq) {

		String msgFunction = String.valueOf(balEnqRq.getMessageFunction());

		String messageTypeIdentifier = balEnqRq.getMessageTypeIdentifier();
		String MTI = messageTypeIdentifier.substring(1);

		if (("100").equals(MTI) || ("101").equals(MTI)) {
			balEnqRq.setMessageTypeIdentifier(messageTypeIdentifier);
		} else {
			ATMTransactionUtil.handleEvent(40000123, new String[] { "messageTypeIdentifier", messageTypeIdentifier });
		}

		String dateTime = String.valueOf(balEnqRq.getTimeLocalTransaction());
		Date date = Date.valueOf(dateTime.substring(0, 10));
		Time time = Time.valueOf(dateTime.substring(11, 19));

		if (("Request").equals(msgFunction) || ("Repeat").equals(msgFunction)) {
			msgFunction = "";
		}

		financialMessageInput = new HashMap<String, Object>();

		financialMessageInput.put(ATMEssenceRequestConstant.MESSAGE_TYPE,
				ATMEssenceRequestConstant.ATM_BALANCE_ENQUIRY);
		financialMessageInput.put(ATMEssenceRequestConstant.PRODUCT_INDICATOR, balEnqRq.getChannelId());

		financialMessageInput.put(ATMEssenceRequestConstant.MSGFUNCTION, msgFunction);
		financialMessageInput.put(ATMEssenceRequestConstant.ENVIRONMENT_60_2,
				ATMEssenceRequestConstant.ENVIRONMENT_60_2_VALUE);
		financialMessageInput.put(ATMEssenceRequestConstant.RETRIEVALREFERENCENO_37,
				balEnqRq.getRetrievalReferenceNumber());
		financialMessageInput.put(ATMEssenceRequestConstant.ACCOUNTNUMBER1_102_2, balEnqRq.getAccountIdentification1());
		financialMessageInput.put(ATMEssenceRequestConstant.CURRENCYCODE_49,
				balEnqRq.getAmountCurrencyCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.COMMNUMCURRENCYCODE,
				balEnqRq.getAmountCurrencyCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.ACQUIRINGINSTITUTIONID_32,
				balEnqRq.getAcquiringInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONTYPE_3_2,
				ATMAPIConstant.processingCodeTransactionTypeBal);
		financialMessageInput.put(ATMEssenceRequestConstant.PROCESSING_CODE_3,
				ATMAPIConstant.processingCodeTransactionTypeBal);
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERFEEAMT, balEnqRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERBILLINGAMT,
				balEnqRq.getAmountCardholderBillingFee());

		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONFEEAMOUNT_28,
				balEnqRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHLDBILLINGCURR,
				balEnqRq.getAmountCurrencyCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORID_42,
				balEnqRq.getCardAcceptorIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORNAMELOC_43,
				balEnqRq.getCardAcceptorNameLocation());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORTERMINALID_41,
				balEnqRq.getCardAcceptorTerminalIdentification());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERAUTHORISER_61,
				balEnqRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERFIID_61_2,
				balEnqRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.RECEIVINGINSTITUTIONID_100,
				balEnqRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35,
				balEnqRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35_2,
				balEnqRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSMISSIONDATETIME_7, balEnqRq.getTimeLocalTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.LOCALTRANSACTIONSQLDATE_13, date);
		financialMessageInput.put(ATMEssenceRequestConstant.LocalTransactionSqlTime_12, time);

		return financialMessageInput;
	}

	public BalanceEnquiryResponse prepareSuccessResponse(BalanceEnquiryRequest balEnqRq,
			HashMap<String, Object> essenceResponse) {

		BalanceEnquiryResponse balanceEnquiryRs = new BalanceEnquiryResponse();
		String MTI = balEnqRq.getMessageTypeIdentifier().substring(1);
		String version = balEnqRq.getMessageTypeIdentifier().substring(0, 1);
		String emptyString = "";
		String msgFunction = String.valueOf(balEnqRq.getMessageFunction());

		if (emptyString.equals(msgFunction) && ATMAPIConstant.MTI_100.equals(MTI)) {
			balEnqRq.setMessageFunction(MessageFunctionEnum.REQUEST);
		} else if (emptyString.equals(msgFunction) && ATMAPIConstant.MTI_101.equals(MTI)) {
			balEnqRq.setMessageFunction(MessageFunctionEnum.REPEAT);
		}

		if (ATMAPIConstant.MTI_100.equals(MTI) || ATMAPIConstant.MTI_101.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_110;
			MTI = version + MTI;
			balEnqRq.setMessageTypeIdentifier(MTI);
			balanceEnquiryRs.setBlockATMBalanceEnquiry(balEnqRq);

			HashMap<String, BigDecimal> balances = AvailableBalanceFunction.run(balEnqRq.getAccountIdentification1());
			BigDecimal ClearedBalance = (balances.get("AvailableBalance"))
					.subtract(balances.get("AvailableBalMinusClearedBal"));

			balanceEnquiryRs.setAccountAvailableBalance((BigDecimal) balances.get("AvailableBalance"));
			balanceEnquiryRs.setAccountAvailableBalanceWithoutCredit(
					(BigDecimal) balances.get("AvailableBalanceWithOutCreditLimit"));
			balanceEnquiryRs.setAccountLedgerBalance(ClearedBalance);
		}

		return balanceEnquiryRs;
	}

	public com.finastra.api.atm.v1.model.Error prepareFailureResponse(String errorCode, String errorCodeMsg) {
		return ATMTransactionUtil.createErrorResponse(errorCode, errorCodeMsg);
	}

}