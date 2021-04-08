package com.finastra.api.atm.v1.mapping;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.constant.ATMEssenceRequestConstant;
import com.finastra.api.atm.v1.model.CashWithdrawalRequest;
import com.finastra.api.atm.v1.model.CashWithdrawalRequest.MessageFunctionEnum;
import com.finastra.api.atm.v1.model.CashWithdrawalResponse;
import com.finastra.api.atm.v1.model.CashWithdrawalResponse.TransactionStatusEnum;
import com.finastra.api.atm.v1.model.Error;
import com.finastra.api.utils.ATMTransactionUtil;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;

public class ATMCashWithdrawalMapping {

	public HashMap<String, Object> prepareEssenceRequest(CashWithdrawalRequest cwdRq) {

		HashMap<String, Object> financialMessageInput;

		String msgFunction = String.valueOf(cwdRq.getMessageFunction());
		String fnCode = String.valueOf(cwdRq.getFunctionCode());

		String messageTypeIdentifier = cwdRq.getMessageTypeIdentifier();
		String MTI = messageTypeIdentifier.substring(1);

		if (("200").equals(MTI) || ("220").equals(MTI) || ("201").equals(MTI) || ("221").equals(MTI)
				|| ("420").equals(MTI) || ("421").equals(MTI)) {
			cwdRq.setMessageTypeIdentifier(messageTypeIdentifier);
		} else {
			ATMTransactionUtil.handleEvent(40000123, new String[] { "messageTypeIdentifier", messageTypeIdentifier });
		}
		financialMessageInput = new HashMap<String, Object>();
		String dateTime = String.valueOf(cwdRq.getTimeLocalTransaction());
		Date date = Date.valueOf(dateTime.substring(0, 10));
		Time time = Time.valueOf(dateTime.substring(11, 19));

		if (("Online").equals(msgFunction)) {
			msgFunction = "";
		}

		financialMessageInput.put(ATMEssenceRequestConstant.MESSAGE_TYPE,
				ATMEssenceRequestConstant.ATM_CASH_WITHDRAWAL);
		financialMessageInput.put(ATMEssenceRequestConstant.PRODUCT_INDICATOR, cwdRq.getChannelId());

		financialMessageInput.put(ATMEssenceRequestConstant.MSGFUNCTION, msgFunction);
		financialMessageInput.put(ATMEssenceRequestConstant.ENVIRONMENT_60_2,
				ATMEssenceRequestConstant.ENVIRONMENT_60_2_VALUE);
		financialMessageInput.put(ATMEssenceRequestConstant.RETRIEVALREFERENCENO_37,
				cwdRq.getRetrievalReferenceNumber());
		financialMessageInput.put(ATMEssenceRequestConstant.ACCOUNTNUMBER1_102_2, cwdRq.getAccountIdentification1());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONAMOUNT_4, cwdRq.getAmountTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.CURRENCYCODE_49, cwdRq.getAmountCurrencyTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.ACQUIRINGINSTITUTIONID_32,
				cwdRq.getAcquiringInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.FORWARDINGINSTITUIONID_33,
				cwdRq.getForwardingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONTYPE_3_2,
				ATMAPIConstant.processingCodeTransactionTypeCwd);
		financialMessageInput.put(ATMEssenceRequestConstant.PROCESSING_CODE_3,
				ATMAPIConstant.processingCodeTransactionTypeCwd);
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERFEEAMT, cwdRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.AMOUNTRECON, cwdRq.getAmountSettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.AMOUNTRECONCURRENCY, cwdRq.getAmountCurrencySettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.ACQUIRERFEE_95_2, cwdRq.getConversionRateSettlement());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERBILLINGAMT, cwdRq.getAmountCardholderBilling());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHLDBILLINGCURR,
				cwdRq.getAmountCurrencyCardholderBilling());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONFEEAMOUNT_28, cwdRq.getValueAmountFee());
		financialMessageInput.put(ATMEssenceRequestConstant.COMMNUMCURRENCYCODE, cwdRq.getCurrencyCodeAmountFee());

		if (("Reversal").equals(msgFunction) || ("RepeatReversal").equals(msgFunction)
				|| ("Replacement").equals(msgFunction)) {
			financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALTRANSACTIONTYPE_90_1,
					cwdRq.getOriginalAcquiringInstitutionIdentificationCode());
			financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALSEQUENCENUMBER_90_2,
					cwdRq.getOriginalMessageTypeIdentifier());
			financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALTRANSACTIONDATE_90_3,
					cwdRq.getOriginalDateAndTimeLocalTransaction());
			if (("202").equals(fnCode)) {
				financialMessageInput.put(ATMEssenceRequestConstant.ORIGINALTXNAMT, cwdRq.getAmountOriginal());
			}
		}
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORID_42,
				cwdRq.getCardAcceptorIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORNAMELOC_43,
				cwdRq.getCardAcceptorNameLocation());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORTERMINALID_41,
				cwdRq.getCardAcceptorTerminalIdentification());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERAUTHORISER_61,
				cwdRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERFIID_61_2,
				cwdRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.RECEIVINGINSTITUTIONID_100,
				(String) cwdRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35, cwdRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35_2, cwdRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSMISSIONDATETIME_7, cwdRq.getTimeLocalTransaction());
		financialMessageInput.put(ATMEssenceRequestConstant.LOCALTRANSACTIONSQLDATE_13, date);
		financialMessageInput.put(ATMEssenceRequestConstant.LocalTransactionSqlTime_12, time);

		return financialMessageInput;
	}

	public CashWithdrawalResponse prepareSuccessResponse(CashWithdrawalRequest cwdRq,
			HashMap<String, Object> essenceResponse) {

		CashWithdrawalResponse cashWithdrawalRs = new CashWithdrawalResponse();
		String emptyString = "";
		String msgFunction = String.valueOf(cwdRq.getMessageFunction());

		if (emptyString.equals(msgFunction)) {
			cwdRq.setMessageFunction(MessageFunctionEnum.ONLINE);
		}

		String version = cwdRq.getMessageTypeIdentifier().substring(0, 1);
		String MTI = cwdRq.getMessageTypeIdentifier().substring(1);

		if (ATMAPIConstant.MTI_200.equals(MTI) || ATMAPIConstant.MTI_201.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_210;
		} else if (ATMAPIConstant.MTI_220.equals(MTI) || ATMAPIConstant.MTI_221.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_230;
		} else if (ATMAPIConstant.MTI_420.equals(MTI) || ATMAPIConstant.MTI_421.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_460;
		}

		MTI = version + MTI;
		cwdRq.setMessageTypeIdentifier(MTI);
		cashWithdrawalRs.setBlockATMCashWithdrawal(cwdRq);

		cashWithdrawalRs.transactionStatus(TransactionStatusEnum.S);

		HashMap<String, BigDecimal> balances = AvailableBalanceFunction.run(cwdRq.getAccountIdentification1());
		BigDecimal ClearedBalance = (balances.get("AvailableBalance"))
				.subtract(balances.get("AvailableBalMinusClearedBal"));

		cashWithdrawalRs.setAccountAvailableBalance((BigDecimal) balances.get("AvailableBalance"));
		cashWithdrawalRs.setAccountAvailableBalanceWithoutCredit(
				(BigDecimal) balances.get("AvailableBalanceWithOutCreditLimit"));
		cashWithdrawalRs.setAccountLedgerBalance(ClearedBalance);
		return cashWithdrawalRs;
	}

	public Error prepareFailureResponse(String errorCode, String errorCodeMsg) {
		return ATMTransactionUtil.createErrorResponse(errorCode, errorCodeMsg);
	}

}
