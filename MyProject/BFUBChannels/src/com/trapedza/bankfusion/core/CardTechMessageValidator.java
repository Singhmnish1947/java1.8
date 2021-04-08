/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Id: CardTechMessageValidator.java,v 1.3 2008/11/27 08:19:21 akleshs Exp $
 *
 * $Log: CardTechMessageValidator.java,v $
 * Revision 1.3  2008/11/27 08:19:21  akleshs
 * Cvs Versioning added
 *
 */
package com.trapedza.bankfusion.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.features.AccountLimitFeature;
import com.trapedza.bankfusion.features.LimitsFeature;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.persistence.exceptions.FinderException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

/**
 * The CardTechMessageValidator contains common validations for the Card Tech
 * messages.
 */
public class CardTechMessageValidator {

	private static final transient Log LOGGER = LogFactory
			.getLog(CardTechMessageValidator.class.getName());

	/*
	 * @author Aklesh Singh
	 */
	@SuppressWarnings("unused")
	private String errorMessage = CommonConstants.EMPTY_STRING;
	ATMHelper atmHelper = new ATMHelper();
	/**
	 * Where clause to validate card account mapping
	 */
	private static final String cardAccMapWhereClause = "WHERE "
			+ IBOATMCardAccountMap.ATMCARDNUMBER + "=? AND "
			+ IBOATMCardAccountMap.ACCOUNTID + "=?";
	private static String findNumericCurrencyCode = "WHERE "
			+ IBOCurrency.NumericISOCurrencyCode + "= ?";
	private static String findAccount = "WHERE "
			+ IBOAttributeCollectionFeature.ACCOUNTID + "= ?";
	private static final String CARDTECH_MODULE_NAME = "CARDTECH";

	private static final String CRADTECH_DOMESTIC_TXNCODE = "DOMESTICTXNCODE";

	private static final String CRADTECH_INTERNATIONAL_TXNCODE = "INTERNATIONALTXNCODE";

	public boolean LimitValidationStatus = true;

	/**
	 * This method validates the whether the account in the message is mapped to
	 * the card in the message. returns true if it is mapped.
	 */
	public boolean isAccountMappedToCard(String cardNo, String accountNo,
			BankFusionEnvironment env) throws BankFusionException {
		boolean isAccMapped = false;
		List cardAccMap = null;
		ArrayList params = new ArrayList();
		params.add(cardNo);
		params.add(accountNo);
		try {
			cardAccMap = env.getFactory().findByQuery(
					IBOATMCardAccountMap.BONAME, cardAccMapWhereClause, params,
					null);
		} catch (BankFusionException bfe) {
			isAccMapped = false;
		}
		if (cardAccMap != null && cardAccMap.size() > 0) {
			isAccMapped = true;
		}
		return isAccMapped;
	}

	public boolean isCardNumberValid(String cardNumber,
			BankFusionEnvironment env) {
		boolean isCardNumberValid = false;
		try {
			IBOATMCardDetails cardDetails = (IBOATMCardDetails) env
					.getFactory().findByPrimaryKey(IBOATMCardDetails.BONAME,
							cardNumber);
			if (cardDetails.getBoID().equals(cardNumber)) {
				isCardNumberValid = true;
			}
		} catch (BankFusionException exception) {
			isCardNumberValid = false;
		}
		return isCardNumberValid;
	}

	/*
	 * This Method Checks if the Given Account exists in the
	 */
	public boolean isAccountExist(String accountNumber,
			BankFusionEnvironment env) {
		boolean isAccountValid = false;
		errorMessage = CommonConstants.EMPTY_STRING;
		try {
			ArrayList params = new ArrayList();
			params.add(accountNumber);
			List accValues = env.getFactory().findByQuery(
					IBOAttributeCollectionFeature.BONAME, findAccount, params,
					null);
			if (accValues.size() > 0) {
				isAccountValid = true;
			}
		} catch (BankFusionException exception) {
			errorMessage = "Account Does not exist";
		}

		return isAccountValid;
	}

	/*
	 * This Method Checks if the Given Account is dormant in the
	 */
	public boolean accountDormantPostingAction(String accountNumber,
			String fileType, BankFusionEnvironment env) {
		boolean allowPosting = false;
		String txnCode = null;
		errorMessage = CommonConstants.EMPTY_STRING;
		IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory
				.getInstance()
				.getServiceManager()
				.getServiceForName(
						BusinessInformationService.BUSINESS_INFORMATION_SERVICE))
				.getBizInfo();
		try {
			IBOAttributeCollectionFeature accValues = (IBOAttributeCollectionFeature) env
					.getFactory()
					.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
							accountNumber);
			boolean dormantStatus = accValues.isF_DORMANTSTATUS();
			if (dormantStatus) {
				if (fileType.equals("D")) {
					txnCode = (String) bizInfo.getModuleConfigurationValue(
							CARDTECH_MODULE_NAME, CRADTECH_DOMESTIC_TXNCODE,
							env);
				} else {
					txnCode = (String) bizInfo.getModuleConfigurationValue(
							CARDTECH_MODULE_NAME,
							CRADTECH_INTERNATIONAL_TXNCODE, env);
				}
				// Get the transaction code details from MISTransactionCodes
				// table
				// Using the Cache of TransactionScreenControl Table for
				// fetching the details.
				MISTransactionCodeDetails mistransDetails;
				IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
						.getInstance()
						.getServiceManager()
						.getServiceForName(
								IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
				mistransDetails = ((IBusinessInformation) ubInformationService
						.getBizInfo()).getMisTransactionCodeDetails(txnCode);

				IBOMisTransactionCodes misTransObj = mistransDetails
						.getMisTransactionCodes();
				String DormancyPostingAction = misTransObj
						.getF_DORMANCYPOSTINGACTION();
				if (DormancyPostingAction.equals("0")) {
					allowPosting = true;
				}
			} else {
				allowPosting = true;
			}

		} catch (BankFusionException exception) {
			errorMessage = "Account is Dormant";
		}

		return allowPosting;
	}

	public boolean isAccountValid(String accountNumber,
			BankFusionEnvironment env) {
		boolean isAccountValid = false;
		errorMessage = CommonConstants.EMPTY_STRING;
		try {
			IBOAttributeCollectionFeature accValues = (IBOAttributeCollectionFeature) env
					.getFactory()
					.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
							accountNumber);
			isAccountValid = true;
			BusinessValidatorBean accountValidator = new BusinessValidatorBean();
			if (accountValidator.validateAccountClosed(accValues, env)) {
				isAccountValid = false;
				errorMessage = accountValidator.getErrorMessage()
						.getLocalisedMessage();
			} else if (accountValidator.validateAccountStopped(accValues, env)) {
				isAccountValid = false;
				errorMessage = accountValidator.getErrorMessage()
						.getLocalisedMessage();
			}

		} catch (FinderException exception) {
			errorMessage = "Invalid Main Account";
		} catch (BankFusionException exception) {
			errorMessage = "Invalid Main Account";
		}

		return isAccountValid;
	}

	public boolean isCurrencyValid(String accountNumber, String currencyCode,
			BankFusionEnvironment env) {
		boolean result = false;
		try {
			if ((currencyCode == null)
					|| (CommonConstants.EMPTY_STRING.equals(currencyCode))) {
				result = false;
				return result;
			}
			IBOAttributeCollectionFeature accountValues = (IBOAttributeCollectionFeature) env
					.getFactory()
					.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
							accountNumber);
			if (accountValues.getF_ISOCURRENCYCODE().equalsIgnoreCase(
					currencyCode)) {
				result = true;
			} else {
				result = false;
			}

		} catch (Exception exception) {
			result = false;
		}
		return result;

	}

	public boolean isAccountPasswordProtected(String accountID, String crDr,
			BankFusionEnvironment env) {
		this.errorMessage = "";
		IBOAccount attributeCollectionFeature = null;
		try {
			attributeCollectionFeature = (IBOAccount) env.getFactory()
					.findByPrimaryKey(IBOAccount.BONAME, accountID);
		} catch (BankFusionException exception) {
			errorMessage = exception.getLocalisedMessage();
			return true;
		}
		boolean result = false;
		int passwordProtectionFlag = attributeCollectionFeature
				.getF_ACCRIGHTSINDICATOR();
		// Password Protection Flag 2
		if (passwordProtectionFlag == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY) {
			this.errorMessage = "Account is Stopped. No Posting/Enquiry Allowed";
			result = true;
			return result;
		}
		// Password Protection Flag 3
		if (passwordProtectionFlag == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY) {
			this.errorMessage = "Account is Stopped. Password required for Posting/Enquiry";
			result = true;
			return result;
		}
		// Password Protection Flag 4
		if (passwordProtectionFlag == PasswordProtectedConstants.DEBITS_NOT_ALLOWED
				&& crDr.equals("D")) {
			this.errorMessage = "Account is Password Protected. Debits Not allowed";
			result = true;
			return result;
		}
		// Password Protection Flag 6
		if (passwordProtectionFlag == PasswordProtectedConstants.CREDITS_NOT_ALLOWED
				&& crDr.equals("C")) {
			this.errorMessage = "Account is Password Protected. Credits Not allowed";
			result = true;
			return result;
		}
		// Password Protection Flag -1
		if (passwordProtectionFlag == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY) {
			this.errorMessage = "Account is Password Protected for Posting Enquiry";
			result = true;
			return result;
		}
		// Password Protection Flag 1
		if (passwordProtectionFlag == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING) {
			this.errorMessage = "Account is Password Protected for Posting ";
			result = true;
			return result;
		}
		// Password Protection Flag 5
		if (passwordProtectionFlag == PasswordProtectedConstants.DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE
				&& crDr.equals("D")) {
			this.errorMessage = "Account is Password Protected. Debits Not allowed";
			result = true;
			return result;
		}
		// Password Protection Flag 7
		if (passwordProtectionFlag == PasswordProtectedConstants.CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE
				&& crDr.equals("C")) {
			this.errorMessage = "Account is Password Protected. Credits Not allowed";
			result = true;
			return result;
		}

		return result;
	}

	/**
	 * Validate group limit
	 * 
	 * @param postingMsg
	 * @throws BankFusionException
	 */
	public boolean checkLimits(BigDecimal amount, String accountNumber,
			String currencyCode, String Sign, BankFusionEnvironment env) {

		// Call LimitsFeature to validate limits
		LimitsFeature limitsFeature = new LimitsFeature(env);
		limitsFeature.setF_IN_AccountCurrencyIfPseudonym(currencyCode);
		limitsFeature.setF_IN_AccountNo(accountNumber);
		limitsFeature.setF_IN_Amount(amount);
		limitsFeature.setF_IN_Amount_Curr(currencyCode);
		limitsFeature.setF_IN_AmountSign(Sign);
		limitsFeature.setF_IN_PerformValidationsOnly(true);
		limitsFeature.setF_IN_ProcessAccountLimits(true);
		try {
			limitsFeature.process(env);
			LimitValidationStatus = limitsFeature.isF_OUT_ProcessStatus()
					.booleanValue();
		} catch (BankFusionException bFExcp) {
			LimitValidationStatus = false;
		}
		return LimitValidationStatus;
	}

	/**
	 * Validate account limit
	 * 
	 * @param postingMsg
	 * @throws BankFusionException
	 */

	public boolean checkAccountLimits(BigDecimal amount, String accountNumber,
			String currencyCode, String Sign, BankFusionEnvironment env) {

		// Call AccountLimitFeature to validate limits
		AccountLimitFeature accLimitFeature = new AccountLimitFeature(env);
		accLimitFeature.setF_IN_ACCOUNTID(accountNumber);
		accLimitFeature.setF_IN_TRANSACTIONAMOUNT(amount);
		accLimitFeature.setF_IN_TRANSACTIONCURRENCY(currencyCode);
		accLimitFeature.setF_IN_TRANSACTIONSIGN(Sign);

		accLimitFeature.process(env);
		LimitValidationStatus = accLimitFeature.isF_OUT_LIMITVALIDATIONSTATUS()
				.booleanValue();

		return LimitValidationStatus;
	}

	public String getAlphaCurrencyCode(String ISONumericCurrencuCode,
			BankFusionEnvironment env) {
		String alphaCode = "";
		try {
			ArrayList params = new ArrayList();
			params.add(ISONumericCurrencuCode);
			List currencyList = env.getFactory().findByQuery(
					IBOCurrency.BONAME, findNumericCurrencyCode, params, null);
			if (currencyList.size() > 0) {
				IBOCurrency currency = (IBOCurrency) currencyList.get(0);
				alphaCode = currency.getBoID();
			}
		} catch (BankFusionException exception) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
		}
		return alphaCode;
	}

	public boolean getSettlementAccount(String settlementaccount,
			BankFusionEnvironment env) {
		if (settlementaccount.equals("")) {
			return true;
		} else {
			return false;
		}
	}

}
