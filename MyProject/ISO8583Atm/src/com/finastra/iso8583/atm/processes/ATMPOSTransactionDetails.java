package com.finastra.iso8583.atm.processes;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;

public class ATMPOSTransactionDetails {

	public static final String ATM_CASH_WITHDRAWAL = "ATMCashWithdrawal";
	public static final String ATM_CASH_DEPOSIT = "ATMCashDeposit";
	public static final String ATM_BALANCE_ENQUIRY = "ATMBalanceEnquiry";
	public static final String ATM_MINI_STATEMENT = "ATMMiniStatement";
	public static final String ATM_FUND_TRANSFER = "ATMFundTransfer";
	public static final String ATMAccPOS = "ATMAccPOS";
	public static final String ATMDualAccPOS = "ATMDualAccPOS";
	public static final String UpdateAccountHold = "UpdateAccountHold";
	public static final String ADVICE = "Advice";
	public static final String REPLACEMENT = "Replacement";
	public static final String REVERSAL = "Reversal";
	public static final String REPEAT = "Repeat";
	public static final String CANCEL = "Cancel";
	public static final String REFUND = "Refund";
	public static final String DUAL = "Dual";
	public static final String ZERO = "0";
	public static final String ONE = "1";
	public static final String TWO = "2";
	public static final String FOUR = "4";
	public static final String _102 = "102";
	public static final String _204 = "204";
	public static final String _205 = "205";

	public static final String processingCodeTransactionTypeCwd = "01";
	public static final String processingCodeTransactionTypeCdp = "21";
	public static final String processingCodeTransactionTypeBal = "31";
	public static final String processingCodeTransactionTypeMst = "39";
	public static final String processingCodeTransactionTypeFnd = "40";

	public StringBuilder getATMMessageFunction(String messageTypeIndicator, String functionCode) {

		String MTI = messageTypeIndicator.substring(1);
		StringBuilder messageFunction = new StringBuilder();

		StringBuilder repeatCons = new StringBuilder();

		if (MTI.endsWith(ONE))
			repeatCons.append(REPEAT);

		if (MTI.startsWith(TWO, 0) && MTI.startsWith(ZERO, 1))
			messageFunction.append(repeatCons);
		else if ((MTI.startsWith(TWO, 0) && MTI.startsWith(TWO, 1))
				|| (MTI.startsWith(ONE, 0) && MTI.startsWith(TWO, 1))) {
			messageFunction.append(repeatCons);
			messageFunction.append(ADVICE);
		} else if (MTI.startsWith(FOUR, 0) && MTI.startsWith(TWO, 1)) {
			// For Replacement
			if ((_102).equals(functionCode)) {
				messageFunction.append(REPLACEMENT);
			} else {
				messageFunction.append(repeatCons);
				messageFunction.append(REVERSAL);
			}
		}
		// COPK CUSTOMIZED (For Dual Message)
		else if (MTI.startsWith(ONE, 0) && MTI.startsWith(ZERO, 1))
			messageFunction.append(repeatCons);

		return messageFunction;
	}

	public StringBuilder getPOSMessageFunction(String messageTypeIndicator, String functionCode) {

		String MTI = messageTypeIndicator.substring(1);
		StringBuilder messageFunction = new StringBuilder();

		StringBuilder repeatCons = new StringBuilder();

		if (MTI.endsWith(ONE))
			repeatCons.append(REPEAT);
		if ((MTI.startsWith(TWO, 0))) {
			messageFunction = repeatCons;
			if (_204.equals(functionCode)) {
				messageFunction.append(repeatCons);
				messageFunction.append(CANCEL);
			}
		} else if (MTI.startsWith(TWO, 0) && MTI.startsWith(TWO, 1)) {
			{
				messageFunction.append(repeatCons);
				messageFunction.append(ADVICE);
			}
			if (_204.equals(functionCode)) {
				messageFunction.append(repeatCons);
				messageFunction.append(CANCEL);
			}
			if (_205.equals(functionCode)) {
				messageFunction.append(repeatCons);
				messageFunction.append(REFUND);
			}
		} else if (MTI.startsWith(FOUR, 0) && MTI.startsWith(TWO, 1)) {

			messageFunction.append(repeatCons);
			messageFunction.append(REVERSAL);
		}

		return messageFunction;
	}

	public StringBuilder getPOSMessageType(String messageTypeIndicator) {

		StringBuilder messageType = new StringBuilder();
		String MTI = messageTypeIndicator.substring(1);

		if (ISOParsingConstants.MTI_200.equals(MTI) || ISOParsingConstants.MTI_201.equals(MTI)
				|| ISOParsingConstants.MTI_420.equals(MTI) || ISOParsingConstants.MTI_421.equals(MTI)
				|| ISOParsingConstants.MTI_220.equals(MTI) || ISOParsingConstants.MTI_221.equals(MTI)) {
			messageType.append(ATMAccPOS);
		}
		if (ISOParsingConstants.MTI_100.equals(MTI) || ISOParsingConstants.MTI_101.equals(MTI)) {
			messageType.append(ATMDualAccPOS);
		}
		if (ISOParsingConstants.MTI_120.equals(MTI) || ISOParsingConstants.MTI_121.equals(MTI)) {
			messageType.append(UpdateAccountHold);
		}
		return messageType;
	}

	public StringBuilder getATMMessageType(String processingCode, String messageTypeIndicator) {

		StringBuilder messageType = new StringBuilder();
		StringBuilder dualConst = new StringBuilder();
		String MTI = messageTypeIndicator.substring(1);

		if (ISOParsingConstants.MTI_100.equals(MTI)) {
//			dualConst.append(DUAL);
		}
		if (processingCodeTransactionTypeCwd.equals(processingCode)) {
			messageType.append(dualConst);
			messageType.append(ATM_CASH_WITHDRAWAL);
		}
		if (processingCodeTransactionTypeCdp.equals(processingCode)) {
			messageType.append(ATM_CASH_DEPOSIT);
		}
		if (processingCodeTransactionTypeBal.equals(processingCode)) {
			messageType.append(ATM_BALANCE_ENQUIRY);
		}
		if (processingCodeTransactionTypeMst.equals(processingCode)) {
			messageType.append(ATM_MINI_STATEMENT);
		}
		if (processingCodeTransactionTypeFnd.equals(processingCode)) {
			messageType.append(ATM_FUND_TRANSFER);
		}
		return messageType;
	}

	public BigDecimal parseAmount(String amount, String currency) {
		int scale = SystemInformationManager.getInstance().getCurrencyScale(currency);
		double amountScale = Math.pow(10, scale);
		double parsingAmount = Double.parseDouble(amount);
		BigDecimal parsedAmount = BigDecimal.valueOf(parsingAmount / amountScale);
		return parsedAmount;
	}

	public BigDecimal parseRate(String rate, int scale) {
		double amountScale = Math.pow(10, scale);
		double parsingAmount = Double.parseDouble(rate);
		BigDecimal parsedRate = BigDecimal.valueOf(parsingAmount / amountScale);
		return parsedRate;
	}

	public String getRawAmount(BigDecimal amount, String currency) {

		int scale = SystemInformationManager.getInstance().getCurrencyScale(currency);
		BigDecimal newScaledAmount = amount.setScale(scale, RoundingMode.DOWN);
		BigDecimal amountScale = BigDecimal.valueOf((Math.pow(10, scale)));
		Integer parsedAmount = (newScaledAmount.multiply(amountScale)).intValue();
		String rawAmountValue = org.apache.commons.lang.StringUtils.leftPad(parsedAmount.toString(), 12,
				ISOParsingConstants.ZERO);
		return rawAmountValue;
	}

	public static String getResponseMTI(String messageTypeIndicator) {

		String version = messageTypeIndicator.substring(0, 1);
		String MTI = messageTypeIndicator.substring(1);
		if (MTI.equals(ISOParsingConstants.MTI_420) || MTI.equals(ISOParsingConstants.MTI_421)) {
			MTI = ISOParsingConstants.MTI_430;
		} else if (MTI.equals(ISOParsingConstants.MTI_100) || MTI.equals(ISOParsingConstants.MTI_101)) {
			MTI = ISOParsingConstants.MTI_110;
		} else if (MTI.equals(ISOParsingConstants.MTI_120) || MTI.equals(ISOParsingConstants.MTI_121)) {
			MTI = ISOParsingConstants.MTI_130;
		} else if (ISOParsingConstants.MTI_200.equals(MTI) || ISOParsingConstants.MTI_201.equals(MTI)) {
			MTI = ISOParsingConstants.MTI_210;
		} else if (ISOParsingConstants.MTI_220.equals(MTI) || ISOParsingConstants.MTI_221.equals(MTI)) {
			MTI = ISOParsingConstants.MTI_230;
		} else if (ISOParsingConstants.MTI_420.equals(MTI) || ISOParsingConstants.MTI_421.equals(MTI)) {
			MTI = ISOParsingConstants.MTI_421;
		} else if (ISOParsingConstants.MTI_804.equals(MTI)) {
			MTI = ISOParsingConstants.MTI_814;
		}

		MTI = version + MTI;
		return MTI;
	}

}