/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: LocalLoro.java,v $
 * Revision 1.2  2008/11/01 14:33:03  bhavyag
 * updated during the fix of bug13963.
 *
 * Revision 1.1  2008/10/10 06:00:56  debjitb
 * updated version after added Amount4
 *
 * Revision 1.14  2008/08/12 20:15:06  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.12.4.3  2008/07/29 01:31:00  prashantk
 * Status Of Microflow is being returned instead of its relying on the Posting Microflow to throw an error. This if for Bug # 11450.
 *
 * Revision 1.12.4.2  2008/07/03 17:55:29  vivekr
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
 * Revision 1.12.4.1  2008/05/17 17:20:01  thrivikramj
 * Changes done for Pseudonym Implementation
 *
 * Revision 1.1  2008/05/08 22:29:41  arjuny
 * Implemented Psedoname
 *
 * Revision 1.12  2008/01/28 07:40:56  sushmax
 * LORO and Cash Withdrawals
 *
 * Revision 1.4  2008/01/21 11:47:17  sushmax
 * Updated files
 *
 * Revision 1.11  2008/01/18 07:21:51  sushmax
 * Updated files
 *
 * Revision 1.2  2008/01/10 14:25:07  prashantk
 * Updations for Incorporating Module Config. Changes for ATM
 *
 * Revision 1.10  2007/12/07 13:33:21  sushmax
 * Code cleanup done
 *
 * Revision 1.9  2007/12/05 13:29:34  sushmax
 * Call MFExecuter ATM Financial Posting microflow for posting
 *
 * Revision 1.8  2007/12/05 08:44:20  sushmax
 * Call MFExecuter ATM Financial Posting microflow for posting
 *
 * Revision 1.7  2007/11/30 09:50:54  sushmax
 * calls to ATMCache methods changed to call ATMHelper methods
 *
 * Revision 1.6  2007/11/29 08:18:29  sushmax
 * Changed code postingMessage.FORCEPOST set  for FORCEPOST flag
 *
 * Revision 1.5  2007/11/28 09:45:30  sushmax
 * Code modified for calling Posting Engine.
 *
 * Revision 1.4  2007/11/27 11:47:00  sushmax
 * Corrected code - checked for boolean value of proceed before posting
 *
 * Revision 1.3  2007/11/26 05:40:49  sushmax
 * Corrected code for 2nd leg of posting.
 *
 * Revision 1.2  2007/11/14 11:06:53  prashantk
 * ATM Financial Message Processors
 *
 * Revision 1.12  2007/10/29 06:53:58  prashantk
 * Updated
 *
 * Revision 1.1.2.1  2007/08/08 18:42:08  prashantk
 * Message processor for ATM Messages
 *
 * Revision 1.11  2007/07/05 07:58:30  sushmax
 * *** empty log message ***
 *
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOExternalLoroSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.exceptions.FinderException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.utils.FatomUtils;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * This class provides calls the ATMMessageValidator methods for the commomn message validations, performs validations 
 * that are specific to the Local LORO transactions(Message - 599)
 * and posts them using the postingEngine. 
 */
public final class LocalLoro extends ATMFinancialProcessor {

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
	private transient final static Log logger = LogFactory.getLog(LocalLoro.class.getName());

	/**
	 * Holds the cashAccount
	 */
	private String cashAccount = null;
	/**
	 * Holds the settlementAccount
	 */
	private String settlementAccount = null;

	/**
	 * Holds the configuration details
	 */
	private ATMControlDetails controlDetails = null;
	/**
	 * Holds the transactionCode
	 */
	private String transactionCode = CommonConstants.EMPTY_STRING;
	/**
	 * Holds the transactionNarration
	 */
	private String customerTransactionNarration = CommonConstants.EMPTY_STRING;
	private String contraTransactionNarration = CommonConstants.EMPTY_STRING;

	/**
	 * Holds the messagenumber
	 */
	private String messagenumber = CommonConstants.EMPTY_STRING;
	
	/**
	 * Where clause for transaction history record retrieval 
	 */
	// modified query for artf46138 [start]
	// For LORO transactions we should not use accountID, 
	// because the accountID is of bank other than UB
	private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";
	// modified query for artf46138 [end]

	private static final String atmSettlementAccountWhereClause = "WHERE " + IBOATMSettlementAccount.ATMDEVICEID + "=?";
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
	public LocalLoro() {

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
		validateLORODetails((ATMLocalMessage) atmSparrowMessage, env);
		//get cash account and settlement account
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
			getCashAccount((ATMLocalMessage) atmSparrowMessage, env);
			getSettlementAccount((ATMLocalMessage) atmSparrowMessage, env);
			//changes for bug 31389 starts
			//changes done for artf694741 start
			if(settlementAccount == "" || settlementAccount == null){
				return;
			}
			//changes done for artf694741 ends  
			if(settlementAccount == ""){
				return;
			}
            //changes for bug 31389 ends
			// for artf46138 added condition for FORCEPOST_2
			if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)) {
				proceed = checkForDuplicates((ATMLocalMessage) atmSparrowMessage, env);
			}
			if (proceed) {
				atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				//create posting messages and call post transaction in ATMFinancialProcessor
				postTransactions((ATMSparrowFinancialMessage) atmSparrowMessage, env);
			}
		}
	}

	/**
	 * This method fetches the cash account to be used in posting. 
	 */
	private void getCashAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
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
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_CASH_ACCOUNT,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
						|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
					/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
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
						populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_CASH_ACCOUNT,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					}
					else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
							|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
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
						populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_CASH_ACCOUNT,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					}
					else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
							|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
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
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INV_DEVICE_ID_CASH_ACCT_NOT_FOUND,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				/*atmLocalMessage.setErrorDescription(String.valueOf(new BankFusionException(7518,
						new Object[] { atmLocalMessage.getAccount() }, logger, env)));*/
			}
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
				/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7519,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.E_INV_DEVICE_ID_SUS_ACCT_UPDATED,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
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
	private void getSettlementAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		//System fetches settlement account based on Loro Mailbox in the message

		String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
		try {
			IBOExternalLoroSettlementAccount externalSettlementAccount = (IBOExternalLoroSettlementAccount) env
					.getFactory().findByPrimaryKey(IBOExternalLoroSettlementAccount.BONAME,
							atmLocalMessage.getLoroMailbox());
			settlementAccount = atmHelper.getAccountIDfromPseudoName(
					externalSettlementAccount.getF_SETTLEMENTACCOUNT(), atmLocalMessage.getCurrencyDestDispensed(),
					branchCode, env);
			if (settlementAccount.equals(CommonConstants.EMPTY_STRING)) {
				Object[] field = new Object[] { externalSettlementAccount.getF_SETTLEMENTACCOUNT() };
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
					/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					//changes for bug 31389 starts
					return;
					//changes for bug 31389 ends
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
						|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
					/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7532,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_INVALID_SETTLEMT_ACCT_ATM_DR_SUSE_ACCT_UPDATED,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					if (controlDetails != null) {
						settlementAccount = atmHelper
								.getAccountIDfromPseudoName(controlDetails.getAtmDrSuspenseAccount(), atmLocalMessage
										.getCurrencyDestDispensed(), branchCode, env);
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
						/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT,
								BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					}
					else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
							|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
						/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7521,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
						populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT,
								BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						if (controlDetails != null) {
							settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails
									.getAtmDrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode,
									env);
						}
						logger.error("Account : " + settlementAccount + " is Closed !");
					}
					else if (validatorBean.validateAccountStopped(accountItem, env)) {

						if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
							/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520,
									BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT,
									BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						}
						else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
								|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
							/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7521,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
							populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT,
									BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
							if (controlDetails != null) {
								settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails
										.getAtmDrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(),
										branchCode, env);
							}
							logger.error("Account : " + settlementAccount + " is Stopped !");
						}
					}
				}
			}
		}
		catch (FinderException fe) {
			Object[] field = new Object[] { atmLocalMessage.getLoroMailbox() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
				/*populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7532,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_INVALID_SETTLEMT_ACCT_ATM_DR_SUSE_ACCT_UPDATED,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmDrSuspenseAccount(),
							atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
				}
			}
		}
	}

	/**
	 * This method validates the local message details.
	 */
	private void validateLORODetails(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		atmMessageValidator.validateMessage(atmLocalMessage, env, ATMMessageValidator.LOCAL_MESSGE_TYPE);
		
		transactionCode = atmHelper.getBankTransactionCode(atmLocalMessage.getMessageType()
				+ atmLocalMessage.getTransactionType(), env);
		atmHelper.updateTransactionNarration(atmLocalMessage, env);
		customerTransactionNarration = atmLocalMessage.getTxnCustomerNarrative();
		contraTransactionNarration = atmLocalMessage.getTxnContraNarrative();

		messagenumber = atmLocalMessage.getMessageType()+ atmLocalMessage.getTransactionType();
		if (transactionCode.equals(CommonConstants.EMPTY_STRING)) {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
				String errorMessage = "Transaction Not Mapped";
				atmLocalMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				atmLocalMessage.setErrorCode(ATMConstants.WARNING);
				atmLocalMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				return;
			}
			else {
				String errorMessage = "Transaction Not Mapped. Using Default Transaction Type";
				atmLocalMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				atmLocalMessage.setErrorCode(ATMConstants.ERROR);
				atmLocalMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				transactionCode = controlDetails.getAtmTransactionType();
			}
		}

		if (!atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			atmMessageValidator.validateDispensedCurrencyForLoro((ATMSparrowFinancialMessage) atmLocalMessage, env);
		}
	}

	/**
	 * This method is to get the transaction reference.
	 */
	private String getTransactionReference(ATMLocalMessage atmLocalMessage) {

		ATMHelper atmHelper = new ATMHelper();
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
		// for artf46138 removed account from query		
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
		//fix for bug 14808 starts
		// for artf46138 added condition for FORCEPOST_2
		if (transactionDetails != null){
		if (transactionDetails.size() > 0 && (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_2))) {
		//fix for bug 14808 ends	
			proceed = false;
			Object[] field = new Object[] { getTransactionReference(atmLocalMessage) };
			/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.INFORMATION, 7523,
					BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);*/
			populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.INFORMATION, ChannelsEventCodes.W_TRANSACTION_ALREADY_POSTED,
					BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);

			}
		}
		return proceed;
	}

	private void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {

		HashMap map = new HashMap();
		String currencyCode = CommonConstants.EMPTY_STRING;
		try {
			currencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
					message.getCurrencyDestDispensed(), true);
		}
		catch (BankFusionException exception) {
			currencyCode = SystemInformationManager.getInstance().getBaseCurrencyCode();
		}
		map.put("ACCOUNT1_ACCOUNTID", settlementAccount);
		map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
		map.put("ACCOUNT1_AMOUNT_CurrCode", currencyCode);
		map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
		map.put("ACCOUNT1_POSTINGACTION", "D");
		map.put("ACCOUNT1_TRANSCODE", transactionCode);

		map.put("ACCOUNT2_ACCOUNTID", cashAccount);
		map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
		map.put("ACCOUNT2_AMOUNT_CurrCode", currencyCode);
		map.put("ACCOUNT2_NARRATIVE", contraTransactionNarration);
		map.put("ACCOUNT2_POSTINGACTION", "C");
		map.put("ACCOUNT2_TRANSCODE", transactionCode);
		map.put("TRANSACTIONREFERENCE", atmHelper.getTransactionReference(message));
		//Start: Fix for artf949632
		
		//map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
		//map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
		map.put("MANUALVALUEDATE", atmHelper.checkForwardValuedTime(message));
		map.put("MANUALVALUETIME", new Time(atmHelper.checkForwardValuedTime(message).getTime()));
		
		//End: Fix for artf949632
		map.put("AMOUNT4",message.getAmount4().abs());
		map.put("MAINACCOUNTID",message.getAccount());
		map.put("MESSAGENUMBER",messagenumber);
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
			if (authorizedFlag.equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
				String errorMessage = outputParams.get("ERRORMESSAGE").toString();
				message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				message.setErrorCode(ATMConstants.ERROR);
				message.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				try {
					env.getFactory().rollbackTransaction();
					env.getFactory().beginTransaction();//
				}
				catch (Exception ignored) {
				}
				return;
			}
			env.getFactory().commitTransaction();
			env.getFactory().beginTransaction();//
			
		}
		catch (BankFusionException exception) {
			logger.info(exception.getMessage());
			message.setErrorCode(ATMConstants.ERROR);
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorDescription(exception.getMessage());
			try {
				env.getFactory().rollbackTransaction();
				env.getFactory().beginTransaction();//
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
		atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage( errorNo,  fields));
	}
}
