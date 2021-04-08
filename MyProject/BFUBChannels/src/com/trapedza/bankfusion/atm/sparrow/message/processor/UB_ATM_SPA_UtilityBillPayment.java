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
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_ATMUTILITYBILLCONFIG;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class UB_ATM_SPA_UtilityBillPayment extends ATMFinancialProcessor {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(UB_ATM_SPA_UtilityBillPayment.class.getName());
	/**
	 * Holds shared switch value
	 */
	private boolean sharedSwitch = false;
	/**
	 * Holds the cardHoldersAccount
	 */
	private String cardHoldersAccount = null;
	/**
	 * Holds the UtilityBill
	 */
	private String utilityBill = null;
	
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
	 * Where clause for atmSource selection  
	 */
	private static final String atmSourceWhereClause = "WHERE " + IBOATMCardIssuersDetail.ISOCOUNTRYCODE +"=?"
	+ "AND " + IBOATMCardIssuersDetail.IMDCODE + "=?";
	/**
	 * Where clause for transaction history record retrieval 
	 */
	private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";
	
	private static final String atmSettlementAccountWhereClause = "WHERE " + IBOUBTB_ATMUTILITYBILLCONFIG.UBSUBINDEXBILLTYPE +"=? " +" and "+IBOUBTB_ATMUTILITYBILLCONFIG.UBDEACTIVATEDT+"<=? ";
	
	private static final String atmSettlementAccountCheckWhereClause = "WHERE " + IBOUBTB_ATMUTILITYBILLCONFIG.UBSUBINDEXBILLTYPE +"=? ";
	
	/**
	 * Instance of ATMMessageValidator 
	 */
	ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
	/**
	 * Instance of ATMHelper 
	 */
	ATMHelper atmHelper = new ATMHelper();
	
	@Override
	
	
	public void execute(ATMSparrowMessage atmSparrowMessage,
			BankFusionEnvironment env) {
				
		boolean proceed = true;
		
		if(checkProcess((ATMLocalMessage)atmSparrowMessage, env))
			proceed = true;
		else
			proceed = false;
		if(proceed){
		//get ATM configuration details
		controlDetails = ATMConfigCache.getInstance().getInformation(env);
		//validate message
		validateUtilityBillDetails(atmSparrowMessage, env);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
			//get Utility Bill account and settlement account
			getUtilityBillAccountOrSettlementAccount((ATMLocalMessage)atmSparrowMessage, env);
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
	

	}
	
	/**
	 * 
	 * @param atmLocalMessage
	 * @param env
	 * @return boolean value to show whether the bill is a valid bill.
	 */
	private boolean checkProcess(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env)
	{
		Object[] field = new Object[] { atmLocalMessage.getAccount() };
		
		ArrayList param = new ArrayList();	
		Iterator atmSettlementAccountCheck = null;
		param.add(atmLocalMessage.getSubIndex());
		
		ArrayList params = new ArrayList();
		Iterator atmSettlementAccountDetails = null;
		params.add(atmLocalMessage.getSubIndex());
		params.add(new java.sql.Date(0));
		
		atmSettlementAccountDetails = env.getFactory().findByQuery(IBOUBTB_ATMUTILITYBILLCONFIG.BONAME,
				atmSettlementAccountCheckWhereClause, param, 1);
		
		if(atmSettlementAccountDetails.hasNext())
		{
			atmSettlementAccountDetails = env.getFactory().findByQuery(IBOUBTB_ATMUTILITYBILLCONFIG.BONAME,
					atmSettlementAccountWhereClause, params, 1);
			if (atmSettlementAccountDetails.hasNext())
				return true;
			else{
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, 7563, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_UNABLE_TO_PROCESS_STOPPED_UTILITY_BILL, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				return false;
			}
		}
		else
		{
//			populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, 7564, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_UTILITY_BILL_AND_BILL_NUMBER_NOT_CONFIGURED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			return false;
		}
			
	}
	
	/**
	 * Method validates the card holders account.
	 * @param atmLocalMessage
	 * @param env
	 * @throws BankFusionException
	 * 
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
		if (accountItem != null) {
			BusinessValidatorBean validatorBean = new BusinessValidatorBean();
			//updation as per the latest Use case for account closed starts.
			if (validatorBean.validateAccountClosed(accountItem, env)) {
				/*populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7566,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);*/
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_CLOSED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			if (logger.isDebugEnabled()) {
				logger.error("Account : " + atmLocalMessage.getAccount() + " is Closed !");
				}
			}	
			//updation as per the latest Use case for account closed starts.
			else if (validatorBean.validateAccountStopped(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7567, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)||atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
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
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7511, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				/*populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_CARD_FORCE_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName =  controlDetails.getAtmDrSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(), "", env);*/

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
		boolean result = atmHelper.isAccountValid(atmLocalMessage,PasswordProtectedConstants.OPERATION_DEBIT, env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
			return;
		}
		if (!result && controlDetails != null)	{
			String psesudoName =  controlDetails.getAtmDrSuspenseAccount();
			cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
					.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
			return;
		}
		boolean passwordProtected = atmHelper.isAccountPasswordProtected(atmLocalMessage, 
				PasswordProtectedConstants.OPERATION_DEBIT,ATMConstants.SOURCEACCOUNTTYPE,  env);
		if(passwordProtected){
			return;
		}
		else {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {	
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
	 * This method fetches the Utility Bill account to be used in posting.
	 * @param atmLocalMessage
	 * @param env
	 * @throws BankFusionException
	 *  
	 */
	private void getUtilityBillAccount (ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {
		
		String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
		logger.info(branchCode);
		
		ArrayList params = new ArrayList();
				
		Iterator atmSettlementAccountDetails = null;
		IBOUBTB_ATMUTILITYBILLCONFIG atmSettlementAccount = null;
		params.add(atmLocalMessage.getSubIndex());
		params.add(new java.sql.Date(0));
		
		atmSettlementAccountDetails = env.getFactory().findByQuery(IBOUBTB_ATMUTILITYBILLCONFIG.BONAME,
				atmSettlementAccountWhereClause, params, 1);
		if (atmSettlementAccountDetails.hasNext()) {
			atmSettlementAccount = (IBOUBTB_ATMUTILITYBILLCONFIG) atmSettlementAccountDetails.next();
			if (!atmSettlementAccount.getF_UBBILLISSUERACCOUNT().equals(CommonConstants.EMPTY_STRING))	{
				utilityBill = atmHelper.getAccountIDfromPseudoName(atmSettlementAccount.getF_UBBILLISSUERACCOUNT(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
			if (utilityBill.equals(CommonConstants.EMPTY_STRING)) {
				Object[] field = new Object[] { atmSettlementAccount.getF_UBBILLISSUERACCOUNT() };
				//Fix for bug 15168 starts
				if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7565, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				} 
				else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7550, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
					// modified errorStatus and errorMessage for artf49787, instead of E_TCPCONN_REFUSED_TO_CONNECT_TO_ATM_PORT
					// using E_INVALID_UTILITY_BILL_ACC and errorStatus as WARNING instead of CRITICAL
					populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACC, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
					if (controlDetails != null) {
						utilityBill = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
					}
				}
			} else {
				Object[] field = new Object[] { utilityBill };
				IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory()
				.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, utilityBill);
				BusinessValidatorBean validatorBean = new BusinessValidatorBean();
				if (validatorBean.validateAccountClosed(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7565, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7550, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						// modified errorStatus and errorMessage for artf49787, instead of E_TCPCONN_REFUSED_TO_CONNECT_TO_ATM_PORT
						// using E_INVALID_UTILITY_BILL_ACC and errorStatus as WARNING instead of CRITICAL
						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACC, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						if (controlDetails != null) {
							utilityBill = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
						}
					logger.error("Account : " + utilityBill + " is Closed !");
					}
					
				}	else if (validatorBean.validateAccountStopped(accountItem, env)) {
					if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7565, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
					} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7550, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						// modified errorStatus and errorMessage for artf49787, instead of E_TCPCONN_REFUSED_TO_CONNECT_TO_ATM_PORT
						// using E_INVALID_UTILITY_BILL_ACC and errorStatus as WARNING instead of CRITICAL
						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACC, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
						if (controlDetails != null) {
							utilityBill = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
						}
					logger.error("Account : " + utilityBill + " is Stopped !");
					}
				}
			}
		}else	{
			Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7565, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7554, env, new Object[] { atmLocalMessage.getAccount()}));
				atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage( ChannelsEventCodes.E_MAIN_ACCT_STOPPED_SUSPENSE_ACC_WILL_BE_UPDATED,  new Object[] { atmLocalMessage.getAccount()}));
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7550, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
				// modified errorStatus and errorMessage for artf49787, instead of E_TCPCONN_REFUSED_TO_CONNECT_TO_ATM_PORT
				// using E_INVALID_UTILITY_BILL_ACC and errorStatus as WARNING instead of CRITICAL
				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INVALID_UTILITY_BILL_ACC, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
				if (controlDetails != null) {
					utilityBill = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
				}
			}
		}
		} else	{
			Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7565, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_INV_DEVICE_ID_CASH_ACCT_NOT_FOUND, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7554, env, new Object[] { atmLocalMessage.getAccount()}));
				//atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, ChannelsEventCodes.E_MAIN_ACCT_STOPPED_SUSPENSE_ACC_WILL_BE_UPDATED, env, new Object[] { atmLocalMessage.getAccount()}));
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3) ) {
//				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7550, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
				populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.E_INV_DEVICE_ID_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					utilityBill = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
				}
				//Fix for bug 15168 ends
			}
		}
	}
	
	/**
	 * This method fetches the settlement account based on Source Country + IMD or network ID in the message for posting.
	 * @param atmLocalMessage
	 * @param env
	 * @throws BankFusionException
	 * 
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
						populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
						if (controlDetails != null) {
							settlementAccount = atmHelper.getAccountIDfromPseudoName( controlDetails.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
						}
						logger.error("Account : " + settlementAccount + " is Closed !");
					}	else if (validatorBean.validateAccountStopped(accountItem, env)) {

						if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
//							populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
							populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
						} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
//							populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
							populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
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
	 * @param atmSparrowMessage
	 * @param env
	 * @throws BankFusionException
	 * 
	 */
	private void validateUtilityBillDetails (ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) throws BankFusionException {
		
		
		atmMessageValidator.validateMessage(atmSparrowMessage, env, ATMMessageValidator.LOCAL_MESSGE_TYPE);
		if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
			return;
		}
		transactionCode = atmHelper.getBankTransactionCode(atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType(), env);
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
				atmSparrowMessage.setErrorCode(ATMConstants.ERROR);
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
			     populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL, ChannelsEventCodes.W_TRANSACTION_ALREADY_POSTED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
		    }
		}
		return proceed;
	}
	
	/**
	 * To decide what will be the settlement account.
	 * @param atmLocalMessage
	 * @param env
	 * @throws BankFusionException
	 * 
	 */
	private void getUtilityBillAccountOrSettlementAccount (ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {

		if (controlDetails != null) {
			sharedSwitch = controlDetails.isSharedSwitch();
			if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
				if (!sharedSwitch) {
					
						getUtilityBillAccount(atmLocalMessage, env);
				} else if (sharedSwitch) {
					getSettlementAccount(atmLocalMessage, env);
				}
			}
		}
	}
	
	/**
	 * Method to do the postings.
	 * @param message
	 * @param env
	 * @throws BankFusionException
	 */
	private void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) throws BankFusionException	{
		
		HashMap map = new HashMap();
		String accountCurrencyCode = CommonConstants.EMPTY_STRING;
		String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
		String customerTransactionNarration = CommonConstants.EMPTY_STRING;
		String contraTransactionNarration = CommonConstants.EMPTY_STRING;
		atmHelper.updateTransactionNarration(message, env);
		customerTransactionNarration = message.getTxnCustomerNarrative();
		contraTransactionNarration = message.getTxnContraNarrative();

		try	{
			dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencyDestDispensed(), true);
			accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencySourceAccount(), true);
			//fix for bug 15109 starts
			ArrayList params1 = new ArrayList();
			Iterator atmSettlementAccountDetails = null;
			params1.add(message.getSubIndex());
			params1.add(new java.sql.Date(0));
			IBOUBTB_ATMUTILITYBILLCONFIG atmSettlementAccount = null;
			atmSettlementAccountDetails = env.getFactory().findByQuery(IBOUBTB_ATMUTILITYBILLCONFIG.BONAME,
					atmSettlementAccountWhereClause, params1, 1);
			atmSettlementAccount = (IBOUBTB_ATMUTILITYBILLCONFIG) atmSettlementAccountDetails.next();
			int length = atmSettlementAccount.getF_UBLENGTHOFBILLREFERENCE();
			
			//fix for bug 15109 ends
		}
		catch(BankFusionException exception)	{
		}
		
		map.put("ACCOUNT1_ACCOUNTID", cardHoldersAccount);
		map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
		map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
		map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
		map.put("ACCOUNT1_POSTINGACTION", "D");
		map.put("ACCOUNT1_TRANSCODE", transactionCode);
		map.put("AMOUNT4",message.getAmount4().abs());
		map.put("MAINACCOUNTID",message.getAccount());
		map.put("MESSAGENUMBER",message.getMessageType() + message.getTransactionType());
				
		if (!sharedSwitch) {   
			map.put("ACCOUNT2_ACCOUNTID", utilityBill);
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
					env.getFactory().beginTransaction();
				}
				catch (Exception ignored) {
				}
				return;
			}
			env.getFactory().commitTransaction();
			env.getFactory().beginTransaction();
		}
		catch(BankFusionException exception)	{
			logger.info("Transaction is Not Authorized: --> " + exception.getMessage());
			message.setErrorCode(ATMConstants.ERROR);
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorDescription(exception.getMessage());
			try	{
				env.getFactory().rollbackTransaction();
				env.getFactory().beginTransaction();
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
		atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(  errorNo , fields ));
	}
	
}
