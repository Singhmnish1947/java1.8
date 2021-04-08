/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: LocalCashWithdrawal.java,v $
 * Revision 1.3  2008/11/06 07:11:49  bhavyag
 * updated for bug 14020.
 *
 * Revision 1.1  2008/10/10 05:57:07  debjitb
 * updated version after added Amount4
 *
 * Revision 1.17  2008/08/12 20:15:05  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.15.4.3  2008/07/29 01:30:07  prashantk
 * Status Of Microflow is being returned instead of its relying on the Posting Microflow to throw an error. This if for Bug # 11450.
 *
 * Revision 1.15.4.2  2008/07/03 17:55:28  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.6  2008/06/19 09:26:34  arun
 * FatomUtils' usage of getBankFusionException changed to call BankFusionException directly
 *
 * Revision 1.5  2008/06/16 15:18:45  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:50:11  arun
 *  RIO on Head
 *
 * Revision 1.15.4.1  2008/05/17 17:20:01  thrivikramj
 * Changes done for Pseudonym Implementation
 *
 * Revision 1.1  2008/05/08 22:29:41  arjuny
 * Implemented Psedoname
 *
 * Revision 1.15  2008/02/22 07:06:30  sushmax
 * Corrected for card holdars account throwing nullpointer exception  if invalid
 *
 * Revision 1.14  2008/02/08 15:20:07  sushmax
 * Refactoring The Processors + Check Ins after Sprint Cycle 5 & 6.
 *
 * Revision 1.18  2008/02/08 10:09:25  prashantk
 * Bug Fixes
 *
 * Revision 1.17  2008/02/07 13:11:42  sushmax
 * corrected authorised flag checking code
 *
 * Revision 1.14  2008/02/04 09:02:30  prashantk
 * Second Currency Change
 *
 * Revision 1.13  2008/01/28 07:40:56  sushmax
 * LORO and Cash Withdrawals
 *
 * Revision 1.13  2008/01/24 16:22:44  varap
 * added if condition at validatecashwithdraw method to return if it is not authorised.
 *
 * Revision 1.12  2008/01/23 09:57:00  varap
 * *** empty log message ***
 *
 * Revision 1.11  2008/01/22 07:43:16  sushmax
 * *** empty log message ***
 *
 * Revision 1.8  2008/01/21 12:20:52  prashantk
 * Updated for Password Protection Check.
 *
 * Revision 1.7  2008/01/21 12:03:10  sushmax
 * Updated files
 *
 * Revision 1.4  2008/01/18 15:53:32  varap
 * query corrected
 *
 * Revision 1.3  2008/01/16 14:28:25  sushmax
 * Corrections done as part of issue fixing
 *
 * Revision 1.2  2008/01/10 14:25:07  prashantk
 * Updations for Incorporating Module Config. Changes for ATM
 *
 * Revision 1.11  2007/12/07 12:12:40  prashantk
 * Code Clean UP:- Removed all Warning Messages
 *
 * Revision 1.10  2007/12/07 11:26:40  sushmax
 * Call MFExecuter ATM Financial Posting microflow for posting
 *
 * Revision 1.9  2007/12/05 13:29:14  sushmax
 * Call MFExecuter ATM Financial Posting microflow for posting
 *
 * Revision 1.8  2007/12/05 08:43:46  sushmax
 * Call MFExecuter for posting
 *
 * Revision 1.7  2007/11/30 09:50:55  sushmax
 * calls to ATMCache methods changed to call ATMHelper methods
 *
 * Revision 1.6  2007/11/29 08:18:29  sushmax
 * Changed code postingMessage.FORCEPOST set  for FORCEPOST flag
 *
 * Revision 1.5  2007/11/28 09:45:31  sushmax
 * Code modified for calling Posting Engine.
 *
 * Revision 1.4  2007/11/27 11:46:51  sushmax
 * Corrected code - checked for boolean value of proceed before posting
 *
 * Revision 1.3  2007/11/26 05:39:49  sushmax
 * Corrected code to retrieve error code descriptions from message.properties and error .properties
 *
 * Revision 1.2  2007/11/14 11:06:53  prashantk
 * ATM Financial Message Processors
 *
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardIssuersDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.persistence.exceptions.FinderException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * The LocalCashWithdrawal class processes the messages for ATM cash withdrawals(Messages - 520, 580 and 585).
 * This class calls the ATMMessageValidator methods for the commomn message validations, performs validations 
 * that are specific to the Local Cash Withdrawal, Fast Cash Message and Local 2nd Currency Withdrawal(Messages - 520, 580 and 585 respectively).
 * The transactions are then posted using the postingEngine. 
 */
public class LocalCashWithdrawal extends ATMFinancialProcessor {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */
	/**
	 * Holds the reference for logger object
	 */
	private transient final static Log logger = LogFactory.getLog(LocalCashWithdrawal.class.getName());

	/**
	 * Holds shared switch value
	 */
	protected boolean sharedSwitch = false;
	/**
	 * This holds the indicator for 2nd currency 
	 */
	private boolean isSecondCurrency = false;
	/**
	 * Holds the cardHoldersAccount
	 */
	protected String cardHoldersAccount = null;

	/**
	 * Holds the cashAccount
	 */
	protected String cashAccount = null;
	/**
	 * Holds the settlementAccount
	 */
	protected String settlementAccount = null;

	/**
	 * Holds the configuration details
	 */
	protected ATMControlDetails controlDetails = null;
	/**
	 * Holds the transactionCode
	 */
	protected String transactionCode = CommonConstants.EMPTY_STRING;
	/**
	 * Holds the transactionNarration
	 */

	protected String customerTransactionNarration = CommonConstants.EMPTY_STRING;

	protected String contraTransactionNarration = CommonConstants.EMPTY_STRING;
	

	/**
	 * Where clause for atmSource selection  
	 */
	private static final String atmSourceWhereClause = "WHERE " + IBOATMCardIssuersDetail.ISOCOUNTRYCODE + "=?"
			+ "AND " + IBOATMCardIssuersDetail.IMDCODE + "=?";
	/**
	 * Where clause for transaction history record retrieval 
	 */
	private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";

	protected static final String atmSettlementAccountWhereClause = "WHERE " + IBOATMSettlementAccount.ATMDEVICEID + "=?";

	/**
	 * Instance of ATMMessageValidator 
	 */
	ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
	/**
	 * Instance of ATMHelper 
	 */
	ATMHelper atmHelper = new ATMHelper();

	/**
	 * Constructor 
	 */
	public LocalCashWithdrawal() {

	}

	/**
	 * This method validates the message received for cashwithdrawal, creates messages for posting
	 * and calls postTransactions() for posting.
	 */
	public void execute(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

		boolean proceed = true;
		//get ATM configuration details
		controlDetails = ATMConfigCache.getInstance().getInformation(env);
		//validate message
		validateCashWithdrawalDetails(atmSparrowMessage, env);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
			//get cash account and settlement account
			getCashAccountOrSettlementAccount((ATMLocalMessage) atmSparrowMessage, env);
			if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
				if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
					proceed = checkForDuplicates((ATMLocalMessage) atmSparrowMessage, env);
				}
				if (proceed) {
					atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
					//create posting messages and call post transaction in ATMFinancialProcessor
					postTransactions((ATMSparrowFinancialMessage) atmSparrowMessage, env);
				}
			}
		}
	}

	/**
	 * This method validates the Source Account in the ATM Sparrow message. 
	 */
	protected void validateCardHoldersAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		Object[] field = new Object[] { atmLocalMessage.getAccount() };
		IBOAttributeCollectionFeature accountItem = null;
		//check for closed or stopped accounts
		try {
			accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
					IBOAttributeCollectionFeature.BONAME, atmLocalMessage.getAccount());
			cardHoldersAccount = atmLocalMessage.getAccount();
		}
		catch (FinderException fe) {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_ACCOUNT,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
				/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
							.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
					return;
				}
			}
		}
		//updation as per the latest Use case for account closed starts.
		if (accountItem != null) {
			BusinessValidatorBean validatorBean = new BusinessValidatorBean();
			if (validatorBean.validateAccountClosed(accountItem, env)) {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7566,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_CLOSED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			if (logger.isDebugEnabled()) {
				logger.error("Account : " + atmLocalMessage.getAccount() + " is Closed !");
				}
			}
		//updation as per the latest Use case for account closed ends.
			else if (validatorBean.validateAccountStopped(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
					/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_STOPPED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)||atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
					/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					if (controlDetails != null) {
						String psesudoName = controlDetails.getAtmDrSuspenseAccount();
						cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
								.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
						return;
					}
				}
				logger.error("Account : " + atmLocalMessage.getAccount() + " is Stopped !");
			}
		}
		//Check ATMCardAccMap to see whether the card no and the account are mapped.
		if (!atmMessageValidator.isAccountMappedToCard(atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount(),
				env)) {
			field = new Object[] { atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7537,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7511,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
							.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
					return;
				}
			}
		}
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		//Checking for Password protection Flag.
		boolean result = atmHelper.isAccountValid(atmLocalMessage, PasswordProtectedConstants.OPERATION_DEBIT, env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		if (!result && controlDetails!= null) {
			String psesudoName = controlDetails.getAtmDrSuspenseAccount();
			cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
					.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
			return;
		}
		boolean passwordProtected = atmHelper.isAccountPasswordProtected(atmLocalMessage, PasswordProtectedConstants.OPERATION_DEBIT, ATMConstants.SOURCEACCOUNTTYPE, env);
		if(passwordProtected){
			return;
		}
		else {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)){
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)||atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				String psesudoName = controlDetails.getAtmDrSuspenseAccount();
				cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
						.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
			}
			return;
		}
	}

	/**
	 * This method fetches the cash account to be used in posting. 
	 */
	protected void getCashAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		//System fetches cash account based on ATM device number for dispensed currency
		String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
		logger.info(branchCode);
		ArrayList params = new ArrayList();
		params.add(atmLocalMessage.getDeviceId());
		Iterator atmSettlementAccountDetails = null;
		IBOATMSettlementAccount atmSettlementAccount = null;

		atmSettlementAccountDetails = env.getFactory().findByQuery(IBOATMSettlementAccount.BONAME,
				atmSettlementAccountWhereClause, params, 1);
		if (atmSettlementAccountDetails.hasNext()) {
			atmSettlementAccount = (IBOATMSettlementAccount) atmSettlementAccountDetails.next();
			cashAccount = atmHelper.getAccountIDfromPseudoName(atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT(),
					atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
			if (cashAccount.equals(CommonConstants.EMPTY_STRING)) {
				Object[] field = new Object[] { atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT() };
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
					/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7518,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_CASH_ACCOUNT,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
						|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
					if (isSecondCurrency) {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7529,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					}
					else {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					}
					if (controlDetails != null) {
						cashAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
								atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
					}
				}
			}
			else {
				Object[] field = new Object[] { cashAccount };
				IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory()
						.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, cashAccount);
				BusinessValidatorBean validatorBean = new BusinessValidatorBean();
				if (validatorBean.validateAccountClosed(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
						/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7518,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_CASH_ACCOUNT,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					}
					else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
							|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
						if (isSecondCurrency) {
							/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7529,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						}
						else {
							/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						}
						if (controlDetails != null) {
							cashAccount = atmHelper.getAccountIDfromPseudoName(
									controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage
											.getCurrencyDestDispensed(), branchCode, env);
						}
						logger.error("Account : " + cashAccount + " is Closed !");
					}

				}
				else if (validatorBean.validateAccountStopped(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
						/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7518,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_CASH_ACCOUNT,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					}
					else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
							|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
						if (isSecondCurrency) {
							/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7529,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						}
						else {
							/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						}
						if (controlDetails != null) {
							cashAccount = atmHelper.getAccountIDfromPseudoName(
									controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage
											.getCurrencyDestDispensed(), branchCode, env);
						}
						logger.error("Account : " + cashAccount + " is Closed !");
					}
				}
			}
		}
		else {
			Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7518,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INV_DEVICE_ID_CASH_ACCT_NOT_FOUND,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
				if (isSecondCurrency) {
					/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7529,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INV_DEVICE_ID_SUS_ACCT_UPDATED,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				}
				else {
					/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INV_DEVICE_ID_SUS_ACCT_UPDATED,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				}
				if (controlDetails != null) {
					cashAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
							atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
				}
			}
		}
	}

	/**
	 * This method fetches the settlement account based on Source Country + IMD or network ID in the message for posting.
	 */
	protected void getSettlementAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		//System fetches settlement account based on Source Country + IMD or network ID in the message
		String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
		ArrayList params = new ArrayList();
		params.add(atmLocalMessage.getSourceCountryCode());
		params.add(atmLocalMessage.getSourceIMD());
		Iterator cardIssuersSettlementDetails = null;
		IBOATMCardIssuersDetail cardIssuersSettlementAccount = null;
		cardIssuersSettlementDetails = env.getFactory().findByQuery(IBOATMCardIssuersDetail.BONAME,
				atmSourceWhereClause, params, 2);
		if (cardIssuersSettlementDetails.hasNext()) {
			cardIssuersSettlementAccount = (IBOATMCardIssuersDetail) cardIssuersSettlementDetails.next();
			settlementAccount = atmHelper.getAccountIDfromPseudoName(cardIssuersSettlementAccount
					.getF_SETTLEMENTACCOUNT(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
			if (settlementAccount.equals(CommonConstants.EMPTY_STRING)) {
				Object[] field = new Object[] { cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT() };
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
					/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
						|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
					if (isSecondCurrency) {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7530,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_2ND_CUR_SETLMT_UPDT_ACCT_ENW_CR_SUS_ACCT,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					}
					else {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7521,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					}
					if (controlDetails != null) {
						settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails
								.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode,
								env);
					}
				}
			}
			else {
				Object[] field = new Object[] { settlementAccount };
				IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory()
						.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, settlementAccount);
				BusinessValidatorBean validatorBean = new BusinessValidatorBean();
				if (validatorBean.validateAccountClosed(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
						/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					}
					else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						if (controlDetails != null) {
							settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails
									.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(),
									branchCode, env);
						}
						logger.error("Account : " + settlementAccount + " is Closed !");
					}
					else if (validatorBean.validateAccountStopped(accountItem, env)) {

						if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
							/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516,
									BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_ACCOUNT,
									BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						}
						else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
							/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
							if (controlDetails != null) {
								settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails
										.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(),
										branchCode, env);
							}
							logger.error("Account : " + settlementAccount + " is Stopped !");
						}
					}
				}
			}
		}
		else {
			Object[] field = new Object[] { atmLocalMessage.getSourceCountryCode(), atmLocalMessage.getSourceIMD() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
				if (isSecondCurrency) {
					/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7530,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_2ND_CUR_SETLMT_UPDT_ACCT_ENW_CR_SUS_ACCT,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				}
				else {
					/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7521,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				}
				if (controlDetails != null) {
					settlementAccount = atmHelper
							.getAccountIDfromPseudoName(controlDetails.getNetworkCrSuspenseAccount(), atmLocalMessage
									.getCurrencyDestDispensed(), branchCode, env);
				}
			}
		}
	}

	/**
	 * This method validates the local message details.
	 */
	protected void validateCashWithdrawalDetails(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

		atmMessageValidator.validateMessage(atmSparrowMessage, env, ATMMessageValidator.LOCAL_MESSGE_TYPE);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		transactionCode = atmHelper.getBankTransactionCode(atmSparrowMessage.getMessageType()
				+ atmSparrowMessage.getTransactionType(), env);
		atmHelper.updateTransactionNarration(atmSparrowMessage, env);
		customerTransactionNarration = atmSparrowMessage.getTxnCustomerNarrative();
		contraTransactionNarration = atmSparrowMessage.getTxnContraNarrative();

		if (transactionCode.equals(CommonConstants.EMPTY_STRING)) {
			if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
					|| atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
				String errorMessage = "Transaction Not Mapped";
				atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				atmSparrowMessage.setErrorCode(ATMConstants.WARNING);
				atmSparrowMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				return;
			}
			else {
				String errorMessage = "Transaction Not Mapped. Using Default Transaction Type";
				atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				atmSparrowMessage.setErrorCode(ATMConstants.ERROR);
				atmSparrowMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				transactionCode = controlDetails.getAtmTransactionType();
			}
		}

		if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			validateCardHoldersAccount((ATMLocalMessage) atmSparrowMessage, env);
		}
		if (((ATMSparrowFinancialMessage) atmSparrowMessage).getAccount().equals(cardHoldersAccount)) {
			if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
				atmMessageValidator.validateSourceCurrency((ATMSparrowFinancialMessage) atmSparrowMessage, env);
			}
		}
		if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			atmMessageValidator.validateDispensedCurrency((ATMSparrowFinancialMessage) atmSparrowMessage, env);
		}
		checkSecondCurrency((ATMLocalMessage) atmSparrowMessage);
	}

	/**
	 * This method concatenates the different message items to form the transaction reference 
	 * used for posting.
	 * @returns String  
	 */
	private String getTransactionReference(ATMLocalMessage atmLocalMessage) {
		return atmHelper.getTransactionReference(atmLocalMessage);
	}

	/**
	 * This method checks the Transaction details for possible duplicate message.
	 * @returns boolean value 
	 */
	private boolean checkForDuplicates(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
		boolean proceed = true;
		ArrayList params = new ArrayList();
		List transactionDetails = null;
		//params.add(atmLocalMessage.getAccount());
		params.add(getTransactionReference(atmLocalMessage));

		//find original transaction
		try {
			transactionDetails = env.getFactory().findByQuery(IBOTransaction.BONAME, txnHistoryWhereClause, params,
					null);
		}
		catch (BankFusionException bfe) {
			//if exception then not a duplicate message. Proceed to post.
			proceed = true;
		}
		if (transactionDetails != null) {
			if (transactionDetails.size() > 0 && atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
				proceed = false;
				Object[] field = new Object[] { getTransactionReference(atmLocalMessage) };
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7523,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_TRANSACTION_ALREADY_POSTED,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
			}
		}
		return proceed;
	}

	/**
	 * This method checks whether the message is of 2nd currency 
	 * used for posting.
	 *   
	 */
	protected void checkSecondCurrency(ATMLocalMessage atmLocalMessage) {
		if (atmLocalMessage.getTransactionType().equals("85")) {
			isSecondCurrency = true;
		}
	}

	/**
	 * This method checks whether the message is of 2nd currency 
	 * used for posting.
	 *   
	 */
	private void getCashAccountOrSettlementAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		if (controlDetails != null) {
			sharedSwitch = controlDetails.isSharedSwitch();
			if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
				if (!sharedSwitch) {
					getCashAccount(atmLocalMessage, env);
				}
				else if (sharedSwitch) {
					getSettlementAccount(atmLocalMessage, env);
				}
			}
		}
	}

	/**
	 * This method calls the ATM financial posting business process to post the financial transaction.
	 * used for posting.
	 *   
	 */
	protected void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {

		HashMap map = new HashMap();
		String accountCurrencyCode = CommonConstants.EMPTY_STRING;
		String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;

		try {
			dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
					message.getCurrencyDestDispensed(), true);
			accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
					message.getCurrencySourceAccount(), true);
		}
		catch (BankFusionException exception) {
		}
		map.put("TRANSACTIONSOURCE", "Local");
		map.put("ACCOUNT1_ACCOUNTID", cardHoldersAccount);
		map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
		map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
		map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
		map.put("ACCOUNT1_POSTINGACTION", "D");
		map.put("ACCOUNT1_TRANSCODE", transactionCode);
		map.put("AMOUNT4",message.getAmount4().abs());
		map.put("MAINACCOUNTID",message.getAccount());
		if (!sharedSwitch) {
			map.put("ACCOUNT2_ACCOUNTID", cashAccount);
		}
		else if (sharedSwitch) {
			map.put("ACCOUNT2_ACCOUNTID", settlementAccount);
			
		}
		map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
		map.put("ACCOUNT2_AMOUNT_CurrCode", dispensedCurrencyCode);
		map.put("ACCOUNT2_NARRATIVE", contraTransactionNarration);
		map.put("ACCOUNT2_POSTINGACTION", "C");
		map.put("ACCOUNT2_TRANSCODE", transactionCode);
		map.put("BASEEQUIVALENT", message.getAmount3().abs());
		map.put("TRANSACTIONREFERENCE", atmHelper.getTransactionReference(message));

		//Start: Fix for artf949632 
		
		//map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
		//map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
		map.put("MANUALVALUEDATE", atmHelper.checkForwardValuedTime(message));
		map.put("MANUALVALUETIME", new Time(atmHelper.checkForwardValuedTime(message).getTime()));
		
		//End: Fix for artf949632 
		map.put("MESSAGENUMBER", message.getMessageType() + message.getTransactionType());
		if (ATMConstants.FORCEPOST_0.equals(message.getForcePost())
				|| ATMConstants.FORCEPOST_6.equals(message.getForcePost())) {
			map.put("FORCEPOST", new Boolean(false));
		}
		else {
			map.put("FORCEPOST", new Boolean(true));
		}

		//Post the Transactions.
		try {
			HashMap outputParams = MFExecuter.executeMF(ATMConstants.FINANCIAL_POSTING_MICROFLOW_NAME, env, map);
		
			String authorizedFlag = outputParams.get("AUTHORIZEDFLAG").toString();
			//changes for bug 24369 starts
			if (authorizedFlag.equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)){
				String errorMessage = outputParams.get("ERRORMESSAGE").toString();
				message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				if(!errorMessage.equals("")){
					message.setErrorCode(ATMConstants.CRITICAL);
					message.setErrorDescription(errorMessage);
				}
				logger.error(errorMessage);
			}
			//changes for bug 24369 ends
			if (authorizedFlag.equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
				String errorMessage = outputParams.get("ERRORMESSAGE").toString();
				message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				message.setErrorCode(ATMConstants.ERROR);
				message.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				try {
					env.getFactory().rollbackTransaction();
					env.getFactory().beginTransaction();   //
				}
				catch (Exception ignored) {
				}
				return;
			}
			env.getFactory().commitTransaction();
			env.getFactory().beginTransaction();   //
		}
		catch (BankFusionException exception) {
			logger.info("Transaction is Not Authorized: --> " + exception.getMessage());
			message.setErrorCode(ATMConstants.ERROR);
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorDescription(exception.getMessage());
			try {
				env.getFactory().rollbackTransaction();
				env.getFactory().beginTransaction();   //
				
			}
			catch (Exception ignored) {

			}
		}
		finally {
			try {
				env.getFactory().beginTransaction();
			}
			catch (Exception ignored) {

			}
		}
	}

	/**
	 * This method  populates the error details in the message
	 * @returns String  
	 */
	private void populateErrorDetails(String authorisedFlag, String errorCode, int errorNo, String errorLevel,
			ATMLocalMessage atmLocalMessage, Object[] fields, BankFusionEnvironment env) {
		atmLocalMessage.setAuthorisedFlag(authorisedFlag);
		atmLocalMessage.setErrorCode(errorCode);
		//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorLevel, errorNo, env, fields));
		atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage( errorNo,  fields));
	}

}
