package com.finastra.iso8583.atm.processes;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;

public class ATMTransactionResponseMapping {

	private static final String AvailableBalance = "AvailableBalance";
	private static final String AvailableBalMinusClearedBal = "AvailableBalMinusClearedBal";
	private static final String D = "D";
	private static final String C = "C";
	private static final String _007 = "007";
	private static final String accountBalance = "accountBalance";
	private static final String miniStatementData = "miniStatementData";

	private static final String TransactionDetails = "TransactionDetails";
	private static final String POSTINGDATE = "POSTINGDATE";
	private static final String ORIGINALAMOUNT = "ORIGINALAMOUNT";
	private static final String DEBITCREDITFLAG = "DEBITCREDITFLAG";

	public static HashMap<String, Object> prepareSuccessResponse(HashMap<String, Object> isoMessage)
			throws IOException {

		ATMPOSTransactionDetails atmPosTransactionDetails = new ATMPOSTransactionDetails();
		String accountId = ISOParsingConstants.EMPTY_STRING;
		String creditDebit;
		if (null != isoMessage.get(ISOParsingConstants.accountIdentification_1)) {
			accountId = (String) isoMessage.get(ISOParsingConstants.accountIdentification_1);
		} else {
			accountId = (String) isoMessage.get(ISOParsingConstants.accountIdentification_2);
		}
		HashMap<String, BigDecimal> balances = AvailableBalanceFunction.run(accountId);
		BigDecimal clearedBalance = (balances.get(AvailableBalance))
				.subtract(balances.get(AvailableBalMinusClearedBal));
		int sign = clearedBalance.signum();
		String accountAvailableBalance = atmPosTransactionDetails.getRawAmount((balances.get(AvailableBalance)),
				(String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
		String accountLedgerBalance = atmPosTransactionDetails.getRawAmount(clearedBalance,
				(String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
		if (-1 == sign) {
			creditDebit = D;
		} else {
			creditDebit = C;
		}
		String field54tag7 = (creditDebit.concat(accountAvailableBalance))
				.concat((String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
		String field54tag7Length = org.apache.commons.lang.StringUtils.leftPad((Integer.toString(field54tag7.length())),
				3, ISOParsingConstants.ZERO);
		String rawValuefield54tag7 = ((_007).concat(field54tag7Length)).concat(field54tag7);

		isoMessage.put(accountBalance, accountLedgerBalance);
		isoMessage.put(ISOParsingConstants.additionalAmounts, rawValuefield54tag7);
		return isoMessage;
	}

	public static HashMap<String, Object> prepareMiniStatementSuccessResponse(HashMap<String, Object> isoMessage,
			HashMap<String, Object> essenceResponse) {
		ATMPOSTransactionDetails atmPosTransactionDetails = new ATMPOSTransactionDetails();
		String accountId = ISOParsingConstants.EMPTY_STRING;
		String creditDebit;
		if (null != isoMessage.get(ISOParsingConstants.accountIdentification_1)) {
			accountId = (String) isoMessage.get(ISOParsingConstants.accountIdentification_1);
		} else {
			accountId = (String) isoMessage.get(ISOParsingConstants.accountIdentification_2);
		}
		HashMap<String, BigDecimal> balances = AvailableBalanceFunction.run(accountId);
		BigDecimal clearedBalance = (balances.get(AvailableBalance))
				.subtract(balances.get(AvailableBalMinusClearedBal));
		int sign = clearedBalance.signum();
		String accountAvailableBalance = atmPosTransactionDetails.getRawAmount((balances.get(AvailableBalance)),
				(String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
		String accountLedgerBalance = atmPosTransactionDetails.getRawAmount(clearedBalance,
				(String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
		if (-1 == sign) {
			creditDebit = D;
		} else {
			creditDebit = C;
		}
		String field54tag7 = (creditDebit.concat(accountAvailableBalance))
				.concat((String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
		String field54tag7Length = org.apache.commons.lang.StringUtils.leftPad((Integer.toString(field54tag7.length())),
				3, ISOParsingConstants.ZERO);
		String rawValuefield54tag7 = ((_007).concat(field54tag7Length)).concat(field54tag7);

		isoMessage.put(accountBalance, accountLedgerBalance);
		isoMessage.put(ISOParsingConstants.additionalAmounts, rawValuefield54tag7);
		StringBuilder field62 = new StringBuilder();

		if (essenceResponse.get(TransactionDetails) != null) {

			Map<?, ?> txnDetail = null;

			VectorTable transactionDetails = (VectorTable) essenceResponse.get(TransactionDetails);

			for (int i = 0; i < transactionDetails.size(); i++) {
				txnDetail = transactionDetails.getRowTags(i);

				String dateTime = String.valueOf((Timestamp) txnDetail.get(POSTINGDATE));
				String date = dateTime.substring(0, 11);
				String postingDate = ParsingEngine.getFormattedToRawData(date, ISOParsingConstants.YYMMDD, 6);
				String transactionPostingAction = (String) txnDetail.get(DEBITCREDITFLAG);
				String transactionAmount = atmPosTransactionDetails.getRawAmount(
						(BigDecimal) txnDetail.get(ORIGINALAMOUNT),
						(String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
				// ATM used as mnumonic based on FBIT-9292
				(((field62.append(postingDate)).append(ISOParsingConstants.ATM)).append(transactionPostingAction))
						.append(transactionAmount);
			}
		}
		isoMessage.put(miniStatementData, field62.toString());
		return isoMessage;

	}

}