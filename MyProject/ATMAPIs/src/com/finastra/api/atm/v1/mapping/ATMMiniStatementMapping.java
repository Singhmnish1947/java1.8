package com.finastra.api.atm.v1.mapping;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.constant.ATMEssenceRequestConstant;
import com.finastra.api.atm.v1.model.MiniStatementRequest;
import com.finastra.api.atm.v1.model.MiniStatementRequest.MessageFunctionEnum;
import com.finastra.api.atm.v1.model.MiniStatementResponse;
import com.finastra.api.atm.v1.model.RepeatBlockForMiniStatement;
import com.finastra.api.utils.ATMTransactionUtil;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;

public class ATMMiniStatementMapping {

	HashMap<String, Object> financialMessageInput;

	public HashMap<String, Object> prepareEssenceRequest(MiniStatementRequest mnstRq) {
		String msgFunction = String.valueOf(mnstRq.getMessageFunction());
		financialMessageInput = new HashMap<String, Object>();

		String messageTypeIdentifier = mnstRq.getMessageTypeIdentifier();
		String MTI = messageTypeIdentifier.substring(1);

		if (("100").equals(MTI) || ("101").equals(MTI)) {
			mnstRq.setMessageTypeIdentifier(messageTypeIdentifier);
		} else {
			ATMTransactionUtil.handleEvent(40000123, new String[] { "messageTypeIdentifier", messageTypeIdentifier });
		}
		if (("Request").equals(msgFunction) || ("Repeat").equals(msgFunction)) {
			msgFunction = "";
		}

		String dateTime = String.valueOf(mnstRq.getTimeLocalTransaction());
		Date date = Date.valueOf(dateTime.substring(0, 10));
		Time time = Time.valueOf(dateTime.substring(11, 19));

		financialMessageInput.put(ATMEssenceRequestConstant.MESSAGE_TYPE, ATMEssenceRequestConstant.ATM_MINI_STATEMENT);
		financialMessageInput.put(ATMEssenceRequestConstant.PRODUCT_INDICATOR, mnstRq.getChannelId());
		financialMessageInput.put(ATMEssenceRequestConstant.MSGFUNCTION, msgFunction);
		financialMessageInput.put(ATMEssenceRequestConstant.ENVIRONMENT_60_2,
				ATMEssenceRequestConstant.ENVIRONMENT_60_2_VALUE);
		financialMessageInput.put(ATMEssenceRequestConstant.RETRIEVALREFERENCENO_37,
				mnstRq.getRetrievalReferenceNumber());
		financialMessageInput.put(ATMEssenceRequestConstant.ACCOUNTNUMBER1_102_2, mnstRq.getAccountIdentification1());
		financialMessageInput.put(ATMEssenceRequestConstant.ACQUIRINGINSTITUTIONID_32,
				mnstRq.getAcquiringInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.FORWARDINGINSTITUIONID_33,
				mnstRq.getForwardingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONTYPE_3_2,
				ATMAPIConstant.processingCodeTransactionTypeMst);
		financialMessageInput.put(ATMEssenceRequestConstant.PROCESSING_CODE_3,
				ATMAPIConstant.processingCodeTransactionTypeMst);
		financialMessageInput.put(ATMEssenceRequestConstant.CURRENCYCODE_49,
				mnstRq.getAmountCurrencyCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.COMMNUMCURRENCYCODE,
				mnstRq.getAmountCurrencyCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERFEEAMT, mnstRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDHOLDERBILLINGAMT, mnstRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSACTIONFEEAMOUNT_28, mnstRq.getAmountCardholderBillingFee());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORID_42,
				mnstRq.getCardAcceptorIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORNAMELOC_43,
				mnstRq.getCardAcceptorNameLocation());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDACCEPTORTERMINALID_41,
				mnstRq.getCardAcceptorTerminalIdentification());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERAUTHORISER_61,
				mnstRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDISSUERFIID_61_2, mnstRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.RECEIVINGINSTITUTIONID_100,
				mnstRq.getReceivingInstitutionIdentificationCode());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35, mnstRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.CARDNUMBER_35_2,
				mnstRq.getPrimaryAccountNumberIdentifier());
		financialMessageInput.put(ATMEssenceRequestConstant.TRANSMISSIONDATETIME_7, mnstRq.getTimeLocalTransaction());

		financialMessageInput.put(ATMEssenceRequestConstant.LOCALTRANSACTIONSQLDATE_13, date);
		financialMessageInput.put(ATMEssenceRequestConstant.LocalTransactionSqlTime_12, time);

		return financialMessageInput;
	}

	public MiniStatementResponse prepareSuccessResponse(MiniStatementRequest MnstRq,
			HashMap<String, Object> essenceResponse) {

		MiniStatementResponse miniStatementRs = new MiniStatementResponse();
		String version = MnstRq.getMessageTypeIdentifier().substring(0, 1);
		String MTI = MnstRq.getMessageTypeIdentifier().substring(1);

		String emptyString = "";
		String msgFunction = String.valueOf(MnstRq.getMessageFunction());

		if (emptyString.equals(msgFunction) && ATMAPIConstant.MTI_100.equals(MTI)) {
			MnstRq.setMessageFunction(MessageFunctionEnum.REQUEST);
		} else if (emptyString.equals(msgFunction) && ATMAPIConstant.MTI_101.equals(MTI)) {
			MnstRq.setMessageFunction(MessageFunctionEnum.REPEAT);
		}
		if (ATMAPIConstant.MTI_100.equals(MTI) || ATMAPIConstant.MTI_101.equals(MTI)) {
			MTI = ATMAPIConstant.MTI_110;
		}

		MTI = version + MTI;
		MnstRq.setMessageTypeIdentifier(MTI);
		miniStatementRs.setBlockATMMiniStatement(MnstRq);

		HashMap<String, BigDecimal> balances = AvailableBalanceFunction.run(MnstRq.getAccountIdentification1());
		BigDecimal ClearedBalance = (balances.get("AvailableBalance"))
				.subtract(balances.get("AvailableBalMinusClearedBal"));

		miniStatementRs.setAccountAvailableBalance((BigDecimal) balances.get("AvailableBalance"));
		miniStatementRs.setAccountAvailableBalanceWithoutCredit(
				(BigDecimal) balances.get("AvailableBalanceWithOutCreditLimit"));
		miniStatementRs.setAccountLedgerBalance(ClearedBalance);

		if (essenceResponse.get("TransactionDetails") != null) {

			Map<?, ?> txnDetail = null;

			VectorTable transactionDetails = (VectorTable) essenceResponse.get("TransactionDetails");

			for (int i = 0; i < transactionDetails.size(); i++) {
				txnDetail = transactionDetails.getRowTags(i);

				RepeatBlockForMiniStatement repeatBlockForMiniStatement = new RepeatBlockForMiniStatement();

				repeatBlockForMiniStatement.setDateTransactionPosting((Timestamp) txnDetail.get("POSTINGDATE"));
				repeatBlockForMiniStatement.setDateTransactionValue((Timestamp) txnDetail.get("VALUEDATE"));
				repeatBlockForMiniStatement.setTransactionType((String) txnDetail.get("TYPE"));
				repeatBlockForMiniStatement.setTransactionPostingAction((String) txnDetail.get("DEBITCREDITFLAG"));
				repeatBlockForMiniStatement.setTransactionAmount((BigDecimal) txnDetail.get("ORIGINALAMOUNT"));
				repeatBlockForMiniStatement.setTransactionCurrency((String) txnDetail.get("ISOCURRENCYCODE"));
				repeatBlockForMiniStatement.setTransactionNarrative((String) txnDetail.get("NARRATION"));
				repeatBlockForMiniStatement
						.setTransactionLedgerBalance((BigDecimal) txnDetail.get("CLEAREDRUNNINGBALANCE"));
				repeatBlockForMiniStatement.setTransactionAvailableBalance((BigDecimal) txnDetail.get("BOOKBALANCE"));
				miniStatementRs.addStatementItem(repeatBlockForMiniStatement);
				miniStatementRs.setStatement(miniStatementRs.getStatement());
			}
		}
		return miniStatementRs;
	}

	public com.finastra.api.atm.v1.model.Error prepareFailureResponse(String errorCode, String errorCodeMsg) {

		return ATMTransactionUtil.createErrorResponse(errorCode, errorCodeMsg);
	}
}