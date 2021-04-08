package com.finastra.api.atm.v1.mapping;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.constant.ATMEssenceRequestConstant;
import com.finastra.api.atm.v1.model.CashDepositRequest;
import com.finastra.api.atm.v1.model.CashDepositRequest.MessageFunctionEnum;
import com.finastra.api.atm.v1.model.CashDepositResponse;
import com.finastra.api.atm.v1.model.CashDepositResponse.TransactionStatusEnum;
import com.finastra.api.utils.ATMTransactionUtil;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;

public class ATMCashDepositMapping {

	HashMap<String, Object> financialMessageInput;

	public HashMap<String, Object> prepareEssenceRequest(CashDepositRequest cdpRq) {

		String msgFunction = String.valueOf(cdpRq.getMessageFunction());
		String fnCode = String.valueOf(cdpRq.getFunctionCode());

		String messageTypeIdentifier = cdpRq.getMessageTypeIdentifier();
		String MTI = messageTypeIdentifier.substring(1);

		if (("200").equals(MTI) || ("220").equals(MTI) || ("201").equals(MTI) || ("221").equals(MTI)
				|| ("420").equals(MTI) || ("421").equals(MTI)) {
			cdpRq.setMessageTypeIdentifier(messageTypeIdentifier);
		} else {
			ATMTransactionUtil.handleEvent(40000123, new String[] { "messageTypeIdentifier", messageTypeIdentifier });
		}

		String dateTime = String.valueOf(cdpRq.getTimeLocalTransaction());
		Date date = Date.valueOf(dateTime.substring(0, 10));
		Time time = Time.valueOf(dateTime.substring(11, 19));

		if (("Online").equals(msgFunction)) {
			msgFunction = "";
		}

		financialMessageInput = new HashMap<String, Object>();

		financialMessageInput.put(ATMEssenceRequestConstant.MESSAGE_TYPE, ATMEssenceRequestConstant.ATM_CASH_DEPOSIT);
		financialMessageInput.put(ATMEssenceRequestConstant.PRODUCT_INDICATOR, cdpRq.getChannelId());

		financialMessageInput.put(ATMEssenceRequestConstant.MSGFUNCTION, msgFunction);
		financialMessageInput.put(ATMEssenceRequestConstant.ENVIRONMENT_60_2,
				ATMEssenceRequestConstant.ENVIRONMENT_60_2_VALUE);
		financialMessageInput.put(ATMEssenceRequestConstant.RETRIEVALREFERENCENO_37,
				cdpRq.getRetrievalReferenceNumber());
		financialMessageInput.put(ATMEssenceRequestConstant.ACCOUNTNUMBER2_103_2, cdpRq.getAccountIdentification2());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONAMOUNT_4, cdpRq.getAmountTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.CURRENCYCODE_49, cdpRq.getAmountCurrencyTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.ACQUIRINGINSTITUTIONID_32,
				cdpRq.getAcquiringInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONTYPE_3_2,
				ATMAPIConstant.processingCodeTransactionTypeCdp);
		financialMessageInput.put(ATMEssenceRequestConstant.PROCESSING_CODE_3,
				ATMAPIConstant.processingCodeTransactionTypeCdp);
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERFEEAMT, cdpRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.AMOUNTRECON, cdpRq.getAmountSettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.AMOUNTRECONCURRENCY, cdpRq.getAmountCurrencySettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.ACQUIRERFEE_95_2, cdpRq.getConversionRateSettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERBILLINGAMT, cdpRq.getAmountCardholderBilling());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHLDBILLINGCURR,
				cdpRq.getAmountCurrencyCardholderBilling());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONFEEAMOUNT_28, cdpRq.getValueAmountFee());
		financialMessageInput.put(ATMEssenceRequestConstant.COMMNUMCURRENCYCODE, cdpRq.getCurrencyCodeAmountFee());

		if (("Reversal").equals(msgFunction) || ("RepeatReversal").equals(msgFunction)
				|| ("Replacement").equals(msgFunction)) {
			financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALTRANSACTIONTYPE_90_1,
					cdpRq.getOriginalAcquiringInstitutionIdentificationCode());
			financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALSEQUENCENUMBER_90_2,
					cdpRq.getOriginalMessageTypeIdentifier());
			financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALTRANSACTIONDATE_90_3,
					cdpRq.getOriginalDateAndTimeLocalTransaction());
			if (fnCode.equals("202")) {
				financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALTXNAMT, cdpRq.getAmountOriginal());
			}
		}
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORID_42,
				cdpRq.getCardAcceptorIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORNAMELOC_43,
				cdpRq.getCardAcceptorNameLocation());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORTERMINALID_41,
				cdpRq.getCardAcceptorTerminalIdentification());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERAUTHORISER_61,
				cdpRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERFIID_61_2, cdpRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.FORWARDINGINSTITUIONID_33,
				cdpRq.getForwardingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.RECEIVINGINSTITUTIONID_100,
				cdpRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35, cdpRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35_2, cdpRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSMISSIONDATETIME_7, cdpRq.getTimeLocalTransaction());

		financialMessageInput.put(ATMEssenceRequestConstant.LOCALTRANSACTIONSQLDATE_13, date);
		financialMessageInput.put(ATMEssenceRequestConstant.LocalTransactionSqlTime_12, time);

		return financialMessageInput;
	}

	public CashDepositResponse prepareSuccessResponse(CashDepositRequest cdpRq,
			HashMap<String, Object> essenceResponse) {

		CashDepositResponse cashDepositRs = new CashDepositResponse();
		String emptyString = "";
		String msgFunction = String.valueOf(cdpRq.getMessageFunction());

		if (emptyString.equals(msgFunction)) {
			cdpRq.setMessageFunction(MessageFunctionEnum.ONLINE);
		}
		String version = cdpRq.getMessageTypeIdentifier().substring(0, 1);
		String MTI = cdpRq.getMessageTypeIdentifier().substring(1);

		if (ATMAPIConstant.MTI_200.equals(MTI) || ATMAPIConstant.MTI_201.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_210;
		} else if (ATMAPIConstant.MTI_220.equals(MTI) || ATMAPIConstant.MTI_221.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_230;
		} else if (ATMAPIConstant.MTI_420.equals(MTI) || ATMAPIConstant.MTI_421.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_460;
		}

		MTI = version + MTI;
		cdpRq.setMessageTypeIdentifier(MTI);
		cashDepositRs.setBlockATMCashDeposit(cdpRq);
		cashDepositRs.setTransactionStatus(TransactionStatusEnum.S);

		HashMap<String, BigDecimal> balances = AvailableBalanceFunction.run(cdpRq.getAccountIdentification2());
		BigDecimal ClearedBalance = (balances.get("AvailableBalance"))
				.subtract(balances.get("AvailableBalMinusClearedBal"));

		cashDepositRs.setAccountAvailableBalance((BigDecimal) balances.get("AvailableBalance"));
		cashDepositRs.setAccountAvailableBalanceWithoutCredit(
				(BigDecimal) balances.get("AvailableBalanceWithOutCreditLimit"));
		cashDepositRs.setAccountLedgerBalance(ClearedBalance);

		return cashDepositRs;
	}

	public com.finastra.api.atm.v1.model.Error prepareFailureResponse(String errorCode, String errorCodeMsg) {
		return ATMTransactionUtil.createErrorResponse(errorCode, errorCodeMsg);
	}

}