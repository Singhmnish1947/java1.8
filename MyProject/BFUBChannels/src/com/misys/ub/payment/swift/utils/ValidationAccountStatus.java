package com.misys.ub.payment.swift.utils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

public class ValidationAccountStatus implements IValidateBasicCheck {
	String errorMessage = CommonConstants.EMPTY_STRING;
	String dormantAccount = CommonConstants.EMPTY_STRING;
	String closedOrStoppedAccount = CommonConstants.EMPTY_STRING;
	
	
	@Override
	public RsHeader validate(OutwardSwtRemittanceRq outwardRq,RsHeader rsHeader) {
		MessageStatus status = rsHeader.getStatus();
		PaymentSwiftUtils utils = new PaymentSwiftUtils();
		//TODO:
		//String chgFundingAccount = outwardRq.getIntlPmtInputRq().getCharges(0).getCharge().getFundingAccount().getStandardAccountId();
		String channel = outwardRq.getRqHeader().getOrig().getChannelId();
		String debTxnCode = outwardRq.getSwftAdditionalDetails().getDebitTxnCode();
		String crtTxnCode = outwardRq.getSwftAdditionalDetails().getCreditTxnCode();
		String debitTxnCode = utils.getTransactionCode(channel, crtTxnCode, debTxnCode, "debit");
		String creditTxnCode = utils.getTransactionCode(channel, crtTxnCode, debTxnCode, "credit");
		/* Fetch debit account details */
		String creditAccountNumber = !StringUtils.isBlank(outwardRq.getIntlPmtInputRq().getFundingPosting().getAccount().getStandardAccountId()) ? outwardRq.getIntlPmtInputRq().getFundingPosting().getAccount().getStandardAccountId() : CommonConstants.EMPTY_STRING;
		String debitAccountNumber = !StringUtils.isBlank(outwardRq.getIntlPmtInputRq().getPaymentPosting().getAccount().getStandardAccountId()) ? outwardRq.getIntlPmtInputRq().getPaymentPosting().getAccount().getStandardAccountId() : CommonConstants.EMPTY_STRING;
		// Validate credit account, it should not be stopped or closed
		status = validateCreditAccount(creditAccountNumber, status);
		//validate if credit account number is dormant
		status = validateAccountDormant(creditAccountNumber, creditTxnCode, status);
		/* Validating debit account for dormancy/close/password flag */
		status = validateAccountDormant(debitAccountNumber, debitTxnCode, status);
		// Validate debit account, it should not be stopped or closed
		status = validateDebitAccount(debitAccountNumber, status);
		rsHeader.setStatus(status);
		return rsHeader;
	}

	private MessageStatus validateDebitAccount(String debitAccountNumber, MessageStatus status) {
		if (status.getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
			errorMessage = UB_IBI_PaymentsHelper.validateAccount(debitAccountNumber, IfmConstants.DR);
			if (!PaymentSwiftConstants.OK.equalsIgnoreCase(errorMessage)) {
				closedOrStoppedAccount = debitAccountNumber;
				if (!StringUtils.isBlank(errorMessage)) {
					SubCode subcode = new SubCode();
					EventParameters vParameters = new EventParameters();
					status.setOverallStatus(PaymentSwiftConstants.ERROR);
					vParameters.setEventParameterValue(debitAccountNumber);
					subcode.addParameters(vParameters);
					status.addCodes(PaymentSwiftUtils.addEventCode(errorMessage,subcode));
				}
			}
		}
		return status;
	}

	private MessageStatus validateCreditAccount(String creditAccountNumber, MessageStatus status) {
		if (status.getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
			errorMessage = UB_IBI_PaymentsHelper.validateAccount(creditAccountNumber, IfmConstants.CR);
			if (!PaymentSwiftConstants.OK.equalsIgnoreCase(errorMessage)) {
				closedOrStoppedAccount = creditAccountNumber;
				if (!StringUtils.isBlank(errorMessage)) {
					SubCode subcode = new SubCode();
					EventParameters vParameters = new EventParameters();
					status.setOverallStatus(PaymentSwiftConstants.ERROR);
					vParameters.setEventParameterValue(creditAccountNumber);
					subcode.addParameters(vParameters);
					status.addCodes(PaymentSwiftUtils.addEventCode(errorMessage,subcode));
				}
			}
		}
		return status;
	}

	private MessageStatus validateAccountDormant(String accountNumber, String hostTxnCode, MessageStatus status) {
		if ((UB_IBI_PaymentsHelper.isAccountDormant(accountNumber, hostTxnCode)) && status.getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
			dormantAccount = accountNumber;
			SubCode subcode = new SubCode();
			EventParameters vParameters = new EventParameters();
			status.setOverallStatus(PaymentSwiftConstants.ERROR);
			vParameters.setEventParameterValue(accountNumber);
			subcode.addParameters(vParameters);
			status.addCodes(PaymentSwiftUtils.addEventCode("40409528",subcode));
		}
		return status;
	}
}
