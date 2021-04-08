/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * $Log: ATMFundsTransfer.java,v $
 * Revision 1.2  2008/11/01 14:33:03  bhavyag
 * updated during the fix of bug13963.
 *
 * Revision 1.1  2008/10/10 05:28:40  debjitb
 * updated version after added Amount4
 *
 * Revision 1.5  2008/08/12 20:15:05  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.3.4.5  2008/07/29 01:30:06  prashantk
 * Status Of Microflow is being returned instead of its relying on the Posting Microflow to throw an error. This if for Bug # 11450.
 *
 * Revision 1.3.4.4  2008/07/16 16:13:18  varap
 * Code cleanup - CVS revision tag added.
 * 
 * Revision         2009/04/10 Debjit Basu
 * changed access from private to protected(validateCardHoldersAccount(),validateDestAccount()).
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
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


// TODO: Auto-generated Javadoc
/**
 * The Class ATMFundsTransfer.
 */
public class ATMFundsTransfer extends ATMFinancialProcessor {

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
	private transient final static Log logger = LogFactory.getLog(ATMFundsTransfer.class.getName());

	/** The card holders account. */
	protected String cardHoldersAccount;
	
	/** The dest account. */
	protected String destAccount;
	
	/** The transaction code. */
	protected String transactionCode;
	
	/** The transaction narration. */
	protected String customerTransactionNarration;
	protected String contraTransactionNarration;
	
	/** The atm helper. */
	protected ATMHelper atmHelper = new ATMHelper();
	
	/** The atm message validator. */
	protected ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
	
	/** The control details. */
	protected ATMControlDetails controlDetails = null;

	/**
	 * Where clause for transaction history record retrieval 
	 */
	private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.atm.sparrow.message.processor.ATMFinancialProcessor#execute(com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage, com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void execute(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {
		controlDetails = ATMConfigCache.getInstance().getInformation(env);
		boolean proceed = true;
		validateFundsTransferDetails(atmSparrowMessage, env);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
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
	/**
	 * 
	 * Method Description:All the validation that are related to source account 
	 * and destination account will happen in validateFundsTransferDetails method.
	 * @param atmSparrowMessage it stores the ATM Sparrow Local Messages.
	 * @param env it holds the Session variables.
	 */

	protected void validateFundsTransferDetails(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {
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
			else if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
					|| atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
					|| atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
					|| atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
				String errorMessage = "Transaction Not Mapped, Using Default Transaction Type";
				atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				atmSparrowMessage.setErrorCode(ATMConstants.ERROR);
				atmSparrowMessage.setErrorDescription(errorMessage);
				transactionCode = getDefaultTransactionType();  //Updated for artf39481.
			}
		}
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		validateCardHoldersAccount((ATMLocalMessage) atmSparrowMessage, env);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		if ((((ATMSparrowFinancialMessage) atmSparrowMessage).getAccount()).equals(cardHoldersAccount)) {
			atmMessageValidator.validateSourceCurrency((ATMSparrowFinancialMessage) atmSparrowMessage, env);
		}
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		validateDestAccount((ATMLocalMessage) atmSparrowMessage, env);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		if ((((ATMSparrowFinancialMessage) atmSparrowMessage).getDestAccountNumber().substring(0, 14))
				.equals(destAccount)) {
			atmMessageValidator.validateDestCurrency((ATMSparrowFinancialMessage) atmSparrowMessage, env);
		}
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
	}
	//Changes for artf39481 starts.
	protected String getDefaultTransactionType() {
		return controlDetails.getAtmTransactionType();
	}
	//Changes for artf39481 ends.
	/**
	 * 
	 * Method Description: For Normal financial posting will happen.
	 * @param message it stores the ATM Sparrow Financial Messages.
	 * @param env it holds the Session variables.
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

		map.put("ACCOUNT1_ACCOUNTID", cardHoldersAccount);
		map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
		map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
		map.put("ACCOUNT1_NARRATIVE",customerTransactionNarration);
		map.put("ACCOUNT1_POSTINGACTION", "D");
		map.put("ACCOUNT1_TRANSCODE", transactionCode);

		map.put("ACCOUNT2_ACCOUNTID", destAccount);
		map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
		map.put("ACCOUNT2_AMOUNT_CurrCode", dispensedCurrencyCode);
		map.put("ACCOUNT2_NARRATIVE",contraTransactionNarration);
		map.put("ACCOUNT2_POSTINGACTION", "C");
		map.put("ACCOUNT2_TRANSCODE", transactionCode);
		map.put("BASEEQUIVALENT", message.getAmount3());
		map.put("TRANSACTIONREFERENCE", atmHelper.getTransactionReference(message));
		// Merger for Issue number artf905860 & artf949632
//		map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
//		map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
		map.put("MANUALVALUEDATE", atmHelper.checkForwardValuedTime(message));
		map.put("MANUALVALUETIME", new Time(atmHelper.checkForwardValuedTime(message).getTime()));
		// Merger for Issue number artf905860 & artf949632
		map.put("AMOUNT4",message.getAmount4().abs());
		map.put("MAINACCOUNTID",message.getAccount());
		map.put("MESSAGENUMBER", message.getMessageType() + message.getTransactionType()); //Added for SmartCard development.
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
					message.setErrorCode(ATMConstants.ERROR);
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
			logger.info(exception.getLocalisedMessage());
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
	    * 
	    * Method Description:It will performs validations 
        * that are specific to the ATMFund Transfer(Messages - 540).
	    * @param atmLocalMessage it stores the ATM Sparrow Local Messages.
	    * @param env it holds the Session variables.
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
				//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516,
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_ACCOUNT,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
				//populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517,
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
							.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
					return;
				}
			}
		}
		//updated as per the latest Use case for account closed starts.
		if (accountItem != null) {
			BusinessValidatorBean validatorBean = new BusinessValidatorBean();
			if (validatorBean.validateAccountClosed(accountItem, env)) {
				//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7566,
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_CLOSED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			if (logger.isDebugEnabled()) {
				logger.error("Account : " + atmLocalMessage.getAccount() + " is Closed !");
				}
			}
		//updated as per the latest Use case for account closed ends.
			else if (validatorBean.validateAccountStopped(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
					//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7567,
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_STOPPED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
					//populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517,
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
				//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7537,
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else {
				//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7539,
				
				//For Fix artf792517
			/*	populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_CARD_AND_ACCT_NUM_UNMAPPED_NOTPOSTED_FORCE_POST,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
							.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);*/
				
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
							.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
					//For Fix artf792517
					return;
				}
			}
		}
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		//Checking for Password protection Flag.
		boolean result = atmHelper.isAccountValid(atmLocalMessage, PasswordProtectedConstants.OPERATION_DEBIT,env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		if (!result && controlDetails!=null) {
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
	 * Populate error details.
	 * 
	 * @param authorisedFlag the authorized flag is 0 or 1 we are passing to that method.
	 * @param errorCode the error code is warning ,critical or information.
	 * @param errorNo the error number(like 7566)
	 * @param errorLevel the error level will be 'Message' or 'Error'
	 * @param atmLocalMessage the ATM local message stores the ATM Sparrow Local Messages.
	 * @param fields the field is object
	 * @param env the env holds the Session variables.
	 */
	private void populateErrorDetails(String authorisedFlag, String errorCode, int errorNo, String errorLevel,
			ATMLocalMessage atmLocalMessage, Object[] fields, BankFusionEnvironment env) {
		atmLocalMessage.setAuthorisedFlag(authorisedFlag);
		atmLocalMessage.setErrorCode(errorCode);
		//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorLevel, errorNo, env, fields));
		atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorNo,fields));
	}

	 /**
 	 * Validate destination account.
 	 * 
 	 * @param atmLocalMessage the ATM local message stores the ATM Sparrow Local Messages.
 	 * @param env the env holds the Session variables.
 	 */
 	protected void validateDestAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		ATMControlDetails moduleDetails = new ATMControlDetails(env);
		String tempDestAccountNumber= atmLocalMessage.getDestAccountNumber().substring(0,moduleDetails.getDestAccountLength());
		//Account length should be picked up from configuration (as being done above), rather than being hardcoded (as done below, so commented 
		// String finalAccountnumber =tempDestAccountNumber.substring(0,13); //Updated for bug artf31986
		Object[] field = new Object[] { tempDestAccountNumber };
		IBOAttributeCollectionFeature accountItem = null;
		//check for closed or stopped accounts
		try {
			accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(  
					IBOAttributeCollectionFeature.BONAME, tempDestAccountNumber);
			destAccount = tempDestAccountNumber;  
			
		}
		catch (FinderException fe) {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
				//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7546,
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_DESTINATION_ACCOUNT,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			}
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
				//populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7547,
				populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_DEVICE_ID,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = controlDetails.getAtmCrSuspenseAccount();
					destAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
							.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
					return;
				}
			}
		}
		if (accountItem != null) {
			BusinessValidatorBean validatorBean = new BusinessValidatorBean();
			if (validatorBean.validateAccountClosed(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
					//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7546,
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_CLOSED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
					//populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7547,
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_DEVICE_ID,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					if (controlDetails != null) {
						String psesudoName = controlDetails.getAtmCrSuspenseAccount();
						destAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
								.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
						return;
					}
				}
				logger.error("Account : " + atmLocalMessage.getDestAccountNumber() + " is Closed !");
			}
			else if (validatorBean.validateAccountStopped(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
					//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7546,
					populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_STOPPED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				}
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
					//populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7547,
					populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_DEVICE_ID,
							BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					if (controlDetails != null) {
						String psesudoName = controlDetails.getAtmCrSuspenseAccount();
						destAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
								.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
						return;
					}
				}
				logger.error("Account : " + tempDestAccountNumber + " is Stopped !");
			}
		}
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		boolean result = atmHelper.isAccountValid(atmLocalMessage, tempDestAccountNumber,
				PasswordProtectedConstants.OPERATION_CREDIT, env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		if (!result && controlDetails!=null) {
			String psesudoName = controlDetails.getAtmCrSuspenseAccount();
			destAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(),
					CommonConstants.EMPTY_STRING, env);
			return;
		}
		boolean passwordProtected = atmHelper.isAccountPasswordProtected(atmLocalMessage, PasswordProtectedConstants.OPERATION_CREDIT, ATMConstants.DESTACCOUNTTYPE, env);
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
				String psesudoName = controlDetails.getAtmCrSuspenseAccount();
				destAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
						.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
			}
			return;
		}
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
				//populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7523,
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,ChannelsEventCodes.W_TRANSACTION_ALREADY_POSTED,
						BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
			}
		}
		return proceed;
	}

	/**
	 * This method concatenates the different message items to form the transaction reference 
	 * used for posting. 
	 * @returns String transaction reference number it passing.
	 */
	private String getTransactionReference(ATMLocalMessage atmLocalMessage) {
		return atmHelper.getTransactionReference(atmLocalMessage);
	}
}
