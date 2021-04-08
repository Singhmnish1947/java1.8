/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 * $Log: TravellersCheque.java,v $
 * Revision 1.4  2008/11/01 14:33:03  bhavyag
 * updated during the fix of bug13963.
 *
 * Revision 1.3  2008/10/10 06:13:08  debjitb
 * updated version after added Amount4
 *
 * Revision 1.3  2008/08/31 13:16:31  nishantd
 * Interface sprint 1 delivery
 *
 * Revision 1.2  2008/08/28 20:49:56  nishantd
 * bug 10369
 *
 * Revision 1.2  2008/08/20 00:24:44  nishantd
 * Taken from Rel-Branch-1_01 . Checked in as a merging activity.
 *
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;



import java.util.ArrayList;
import java.sql.Date;
import java.sql.Time;
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
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;
import com.trapedza.bankfusion.utils.CommonEventCodes;



/**
 * The TravelersCheque class processes the messages for Traveller's Cheque(Message - 529).
 * This class calls the ATMMessageValidator methods for the commomn message validations, performs validations 
 * that are specific to the Travellers Cheque 
 * The transactions are then posted using the postingEngine. 
 */
public final class TravellersCheque extends ATMFinancialProcessor {

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
	private transient final static Log logger = LogFactory.getLog(TravellersCheque.class.getName());

	/**
	 * Holds shared switch value
	 */
	private boolean sharedSwitch = false;
	/**
	 * This holds the indicator for 2nd currency 
	 */
	//private boolean isSecondCurrency = false;	
	/**
	 * Holds the cardHoldersAccount
	 */
	private String cardHoldersAccount = null;

	/**
	 * Holds the travellersCheque
	 */
	private String travellersCheque = null;
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
	 * Where clause for atmSource selection  
	 */
	private static final String atmSourceWhereClause = "WHERE " + IBOATMCardIssuersDetail.ISOCOUNTRYCODE +"=?"
	+ "AND " + IBOATMCardIssuersDetail.IMDCODE + "=?";
	/**
	 * Where clause for transaction history record retrieval 
	 */
	private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";
	
	private static final String atmSettlementAccountWhereClause = "WHERE " + IBOATMSettlementAccount.ATMDEVICEID +"=?";
	
	private static final String FindBytransactionCode="WHERE " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + "=?";
	
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
	public TravellersCheque() {

	}

	/**
	 * This method validates the message received for travellers cheque, creates messages for posting
	 * and calls postTransactions() for posting.
	 */
	public void execute(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) throws BankFusionException {
		
		boolean proceed = true;
		//get ATM configuration details
		controlDetails = ATMConfigCache.getInstance().getInformation(env);
		//validate message
		validateTravellersChequeDetails(atmSparrowMessage, env);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
			//get Travellers Cheque account and settlement account
			getTravellersChequeAccountOrSettlementAccount((ATMLocalMessage)atmSparrowMessage, env);
			if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
				if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
					proceed = checkForDuplicates((ATMLocalMessage)atmSparrowMessage, env);
				}
				if (proceed) {
					atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
					//create posting messages and call post transaction in ATMFinancialProcessor
					postTransactions((ATMSparrowFinancialMessage)atmSparrowMessage, env);			
				} 
			}
		}
	}

	/**
	 * This method validates the Source Account in the ATM Sparrow message. 
	 */
	private void validateCardHoldersAccount (ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {

		Object[] field = new Object[] { atmLocalMessage.getAccount() };
		IBOAttributeCollectionFeature accountItem = null;
		//check for closed or stopped accounts
		try {
			accountItem = (IBOAttributeCollectionFeature) env.getFactory()
			.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, atmLocalMessage.getAccount());
			cardHoldersAccount = atmLocalMessage.getAccount();
		} catch (BankFusionException exception) {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
//				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(), "", env);
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
//					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7567, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
//					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					if (controlDetails != null) {
						String psesudoName =  controlDetails.getAtmDrSuspenseAccount();
						cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(), "", env);
						return;
					}
				}
				logger.error("Account : " + atmLocalMessage.getAccount() + " is Stopped !");
			}
		}
		//sushma added 28/05/2008
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
			return;
		}
		//end
		
		//Check ATMCardAccMap to see whether the card no and the account are mapped.
		if (!atmMessageValidator.isAccountMappedToCard(atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount(), env)){
			field = new Object[] { atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7537, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			} else {
			 /*
			     populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7511, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_CARD_FORCE_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName =  controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(), "", env); */
				
				//For Fix artf792517
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
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
			return;
		}
		//Checking for Password protection Flag.
		boolean result = atmHelper.isAccountValid(atmLocalMessage, PasswordProtectedConstants.OPERATION_DEBIT, env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		if (!result && controlDetails !=null ) {
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
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
				
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			} else {
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
	 * This method fetches the Traveller's Cheque account to be used in posting. 
	 */
	private void getTravellersChequeAccount (ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {
		
		//System fetches traveler's check account based on ATM device number for dispensed currency
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
			if (!atmSettlementAccount.getF_TCSETTLEMENTACCOUNT().equals(CommonConstants.EMPTY_STRING)){
			travellersCheque = atmHelper.getAccountIDfromPseudoName(atmSettlementAccount.getF_TCSETTLEMENTACCOUNT(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
			if (travellersCheque.equals(CommonConstants.EMPTY_STRING)) {
				Object[] field = new Object[] { atmSettlementAccount.getF_TCSETTLEMENTACCOUNT() };
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7570, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_TRAVELLERS_CHEQUE_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					return;
				} 
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
					if (controlDetails != null) {
						travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
					}
				}
			} else {
				Object[] field = new Object[] { travellersCheque };
				IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory()
				.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, travellersCheque);
				BusinessValidatorBean validatorBean = new BusinessValidatorBean();
				if (validatorBean.validateAccountClosed(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7568, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_TRAVELLERS_CQE_ACCT_CLOSED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						if (controlDetails != null) {
							travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
						}
					logger.error("Account : " + travellersCheque + " is Closed !");
					}
					
				}	else if (validatorBean.validateAccountStopped(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7569, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_TRAVELLERS_CQE_ACCT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						if (controlDetails != null) {
							travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
						}
					logger.error("Account : " + travellersCheque + " is Stopped !");
					}
				}
			}
		}else	{
			Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7549, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				//atmLocalMessage.setErrorDescription(String.valueOf(FatomUtils.getBankFusionException(7549, new Object[] { atmLocalMessage.getAccount() }, logger, env)));
//				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
				//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7570, env, new Object[] { atmLocalMessage.getAccount()}));
				atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_INVALID_TRAVELLERS_CHEQUE_ACCOUNT,  new Object[] { atmLocalMessage.getAccount()}));
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
				if (controlDetails != null) {
					travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
				}
			}
		}
		} else	{
			Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7549, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR,ChannelsEventCodes.E_INV_DEVICE_ID_CASH_ACCT_NOT_FOUND , BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				//atmLocalMessage.setErrorDescription(String.valueOf(FatomUtils.getBankFusionException(7549, new Object[] { atmLocalMessage.getAccount() }, logger, env)));
				//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7570, env, new Object[] { atmLocalMessage.getAccount()}));
				//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage( ChannelsEventCodes.E_INVALID_TRAVELLERS_CHEQUE_ACCOUNT,  new Object[] { atmLocalMessage.getAccount()}));
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INV_DEVICE_ID_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
				}
			}
		}
	}
	/**
	 * This method fetches the settlement account based on Source Country + IMD or network ID in the message for posting.
	 */
	private void getSettlementAccount (ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {

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
			settlementAccount = atmHelper.getAccountIDfromPseudoName(cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
			if (settlementAccount.equals(CommonConstants.EMPTY_STRING)) {
				Object[] field = new Object[] { cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT() };
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7521, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
					if (controlDetails != null) {
						settlementAccount = atmHelper.getAccountIDfromPseudoName( controlDetails.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
					}
				}
			}	else {
				Object[] field = new Object[] { settlementAccount };
				IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory()
				.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, settlementAccount);
				BusinessValidatorBean validatorBean = new BusinessValidatorBean();
				if (validatorBean.validateAccountClosed(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_CLOSED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
//						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						if (controlDetails != null) {
							settlementAccount = atmHelper.getAccountIDfromPseudoName( controlDetails.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
						}
						logger.error("Account : " + settlementAccount + " is Closed !");
					}	else if (validatorBean.validateAccountStopped(accountItem, env)) {

						if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//							populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
							populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
//							populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
							populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
							if (controlDetails != null) {
								settlementAccount = atmHelper.getAccountIDfromPseudoName( controlDetails.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
							}
							logger.error("Account : " + settlementAccount + " is Stopped !");
						}
					}
				}
			}
		}	else	{
			Object[] field = new Object[] { atmLocalMessage.getSourceCountryCode(), atmLocalMessage.getSourceIMD()};
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7521, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					settlementAccount = atmHelper.getAccountIDfromPseudoName( controlDetails.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
				}
			}
		}
	}
	

	/**
	 * This method validates the local message details.
	 */
	private void validateTravellersChequeDetails (ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) throws BankFusionException {
		
		
		atmMessageValidator.validateMessage(atmSparrowMessage, env, ATMMessageValidator.LOCAL_MESSGE_TYPE);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
			return;
		}
		
		transactionCode = atmHelper.getBankTransactionCode(atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType(), env);
		
		atmHelper.updateTransactionNarration(atmSparrowMessage, env);
		customerTransactionNarration = atmSparrowMessage.getTxnCustomerNarrative();
		contraTransactionNarration = atmSparrowMessage.getTxnContraNarrative();

		if (transactionCode.equals(CommonConstants.EMPTY_STRING))	{
			if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6))	{
				String errorMessage = "Transaction Not Mapped";
				atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				atmSparrowMessage.setErrorCode(ATMConstants.ERROR);
				atmSparrowMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				return;
			}
			else	{
				String errorMessage = "Transaction Not Mapped. Using Default Transaction Type";
				atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				atmSparrowMessage.setErrorCode(ATMConstants.WARNING);
				atmSparrowMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				transactionCode = controlDetails.getAtmTransactionType();
			}
		}

		if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			validateCardHoldersAccount((ATMLocalMessage)atmSparrowMessage, env);
		}
		if (((ATMSparrowFinancialMessage)atmSparrowMessage).getAccount().equals(cardHoldersAccount)){
		if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			atmMessageValidator.validateSourceCurrency((ATMSparrowFinancialMessage)atmSparrowMessage, env);
		}
		}
		if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			atmMessageValidator.validateDispensedCurrency((ATMSparrowFinancialMessage)atmSparrowMessage, env);
		}
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
	private boolean checkForDuplicates (ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {
		boolean proceed = true;
		ArrayList params = new ArrayList();
		List transactionDetails = null;
		//params.add(atmLocalMessage.getAccount());
		params.add(getTransactionReference(atmLocalMessage));

		//find original transaction
		try {
			transactionDetails = env.getFactory().findByQuery(IBOTransaction.BONAME, txnHistoryWhereClause, params, null);
		} catch (BankFusionException bfe) {
			//if exception then not a duplicate message. Proceed to post.
			proceed = true;
		}
		if (transactionDetails != null) {
		    if (transactionDetails.size() > 0 && atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
			     proceed = false;
			     Object[] field = new Object[] { getTransactionReference(atmLocalMessage)};
//			     populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7523, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
			     populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.W_TRANSACTION_ALREADY_POSTED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
		    }
		}
		return proceed;
	}

	
	private void getTravellersChequeAccountOrSettlementAccount (ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {

		if (controlDetails != null) {
			sharedSwitch = controlDetails.isSharedSwitch();
			if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
				if (!sharedSwitch) {
					getTravellersChequeAccount(atmLocalMessage, env);
				} else if (sharedSwitch) {
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
	private void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) throws BankFusionException	{
		
		HashMap map = new HashMap();
		String accountCurrencyCode = CommonConstants.EMPTY_STRING;
		String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
		
		try	{
			dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencyDestDispensed(), true);
			accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencySourceAccount(), true);
		}
		catch(BankFusionException exception)	{
		}
		String messageNumber=message.getMessageType() + message.getTransactionType();
		map.put("ACCOUNT1_ACCOUNTID", cardHoldersAccount);
		map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
		map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
		map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
		map.put("ACCOUNT1_POSTINGACTION", "D");
		map.put("ACCOUNT1_TRANSCODE", transactionCode);
		map.put("AMOUNT4",message.getAmount4().abs());
		map.put("MAINACCOUNTID",message.getAccount());
		
		if (!sharedSwitch) {   
			map.put("ACCOUNT2_ACCOUNTID", travellersCheque);
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
		map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
		map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
		map.put("MESSAGENUMBER", messageNumber);
		if (ATMConstants.FORCEPOST_0.equals(message.getForcePost()) || ATMConstants.FORCEPOST_6.equals(message.getForcePost()))	{
			map.put("FORCEPOST", new Boolean(false));
		}
		else	{
			map.put("FORCEPOST", new Boolean(true));
		}
		
		//Post the Transactions.
		try	{
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
					env.getFactory().beginTransaction(); //
				}
				catch (Exception ignored) {
				}
				return;
			}
			env.getFactory().commitTransaction();
			env.getFactory().beginTransaction(); //
		}
		catch(BankFusionException exception)	{
			logger.info(exception.getMessage());
			message.setErrorCode(ATMConstants.ERROR);
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorDescription(exception.getMessage());
			try	{
				env.getFactory().rollbackTransaction();
				env.getFactory().beginTransaction(); //
			}
			catch(Exception ignored)	{
				
			}
		}
		finally	{
			try	{
				env.getFactory().beginTransaction();
			}
			catch(Exception ignored)	{
				
			}
		}
	}

	/**
	 * This method  populates the error details in the message
	 * @returns String  
	 */
	private void populateErrorDetails (String authorisedFlag, String errorCode, int errorNo, String errorLevel,
			ATMLocalMessage atmLocalMessage, Object[] fields, BankFusionEnvironment env) throws BankFusionException {
		atmLocalMessage.setAuthorisedFlag(authorisedFlag);
		atmLocalMessage.setErrorCode(errorCode);
		atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage( errorNo ,  fields ));
	}
	
	private String getTransactionNarration(String transCode, String deviceId, BankFusionEnvironment env)	{
		String transNarration = CommonConstants.EMPTY_STRING;
		try	{
			ArrayList params = new ArrayList();
			params.add(transCode);
            IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);
			
			//IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findByPrimaryKey(IBOATMTransactionCodes.BONAME, transCode);
			transNarration = atmTransactionCodes.getF_NARRATIVE() + deviceId + atmTransactionCodes.getF_DESCRIPTION();
		}
		catch(BankFusionException exception)	{
		}
		return transNarration;
		
	}
	
}
