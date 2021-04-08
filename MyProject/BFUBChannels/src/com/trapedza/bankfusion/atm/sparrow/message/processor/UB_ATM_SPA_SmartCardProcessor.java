/*
 * Copyright (c) 2009 Misys Software Solutions Pvt Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Software Solutions Pvt Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
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
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMExNwMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMPOSBLOCKINGCONF;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AddDaysToDate;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;

	/**
	 * The UB_ATM_SPA_SmartCardProcessor class processes the following
	 * Messages- 623,626,625,614,633,628,610,611,612,613,622,624and 633.
	 * This class extends the POSCashProcessor class
	 * for common message validations and performs account validations that are
	 * specific to the POS Transactions. The transactions are then
	 * posted using the Financial Posting Engine.
	 */

public class UB_ATM_SPA_SmartCardProcessor extends POSCashProcessor {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public UB_ATM_SPA_SmartCardProcessor(){
		super();
	}
	
	/** The Constant logger.
	 * 
	 * Holds the reference for logger object
	 *
	 */
	private transient final static Log logger = LogFactory.getLog(POSCashProcessor.class.getName());
	/**
	 * Final variables for Message 623,626,625,614,633,628,610,611,612,613,622,624.
	 */
	private static final String MERCHANT_DEBIT_TRANSACTION = "626";
	
	/** The Constant MERCHANT_CREDIT_TRANSACTION. */
	private static final String MERCHANT_CREDIT_TRANSACTION = "625";
	private static final String POS_REFUND = "623";
	// added for artf45353 [start]
	private static final String SMART_CARD_FUNDS_TRANSFER = "627";
	// added for artf45353 [end]	
	private static final String EXTERNAL_POS_REFUND = "614";
	private static final String SMART_CARD_DEBIT = "633";
	private static final String CREDIT_PURSE_CASH = "628";
	private static final String External_Sale = "610";
	private static final String External_CASHBACK = "611";
	private static final String External_QUASICASH = "612";
	private static final String External_POSCASH = "613";
	private static final String External_Sale_REQUEST = "622";
	private static final String External_CASH_REQUEST = "621";
	private static final String External_CASHREQUEST = "624";
	private boolean isOffUsTransaction = false;
	private ATMControlDetails atmControlDetails = null;
	//artf210330 changes start
	private String transactionNarration = CommonConstants.EMPTY_STRING;	
	String customerTransactionNarration = CommonConstants.EMPTY_STRING;
	String contraTransactionNarration = CommonConstants.EMPTY_STRING;
	BigDecimal blockedAmount = CommonConstants.BIGDECIMAL_ZERO;
	//artf210330 changes ends
	String commissionAccount = "";
	private Object[] fields = null;
	
	// added for artf44890 [start]
	private static final String getPOSBlockingDetails = "where " + IBOATMPOSBLOCKINGCONF.IMDCODE + "= ?" + " AND "
	+ IBOATMPOSBLOCKINGCONF.MISTRANSACTIONCODE + "=?";
	// added for artf44890 [end]
	
	private static final String FindBytransactionCode="WHERE " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + "=?";
	
	private Object [] field = null;
	/**
	 *  @returns String
	 * This Method will get the main Account for the posting. if the Card holders
	 * account does not exist we will get The  Dr Suspense account if the
	 * force post is 1 or 7.
	 */
	protected String getMainAccount(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env) {
		atmControlDetails = ATMConfigCache.getInstance().getInformation(env);
		field = new Object[] {financialMessage.getAccount()};
		ATMMessageValidator messageValidator = new ATMMessageValidator();
		BusinessValidatorBean accountValidator = new BusinessValidatorBean();
		IBOAttributeCollectionFeature accValues = (IBOAttributeCollectionFeature)BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
				IBOAttributeCollectionFeature.BONAME, financialMessage.getAccount(),true);
		String debitAccount = CommonConstants.EMPTY_STRING;
		//artf45119, artf45120 changes start
		fields = new Object[] { financialMessage.getAccount() };
		if (MERCHANT_CREDIT_TRANSACTION.equals(transactionType) || 
				MERCHANT_DEBIT_TRANSACTION.equals(transactionType) 
				//changes for artf52970 start
				|| SMART_CARD_FUNDS_TRANSFER.equals(transactionType)
				//changes for artf52970 end
				){
			fields = new Object[] { financialMessage.getAccount() };
			if (accValues == null) {
				if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)) {
					financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
					financialMessage.setErrorCode(ATMConstants.WARNING);
					if (MERCHANT_CREDIT_TRANSACTION.equals(transactionType)){
						financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.W_INV_MER_ACCT_SUSP_ACCT_UPDT, fields));
						financialMessage.setAccount(atmHelper.getAccountIDfromPseudoName(
							atmControlDetails.getSmartCardMerchantCreditSuspenseAccount(), financialMessage
							.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env));
					commissionAccount = atmHelper.getAccountIDfromPseudoName(
							atmControlDetails.getSmartCardDebitSuspenseAccount(), financialMessage
							.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
					debitAccount = financialMessage.getAccount();
					return debitAccount;
					}
					else if (MERCHANT_DEBIT_TRANSACTION.equals(transactionType)){
						financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.W_INV_MER_ACCT_DEB_SUSP_ACCT_UPDT, fields));
						financialMessage.setAccount(atmHelper.getAccountIDfromPseudoName(
								atmControlDetails.getSmartCardMerchantDebitSuspenseAccount(), financialMessage
								.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env));
						commissionAccount = atmHelper.getAccountIDfromPseudoName(
								atmControlDetails.getSmartCardDebitSuspenseAccount(), financialMessage
								.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
						debitAccount = financialMessage.getAccount();
						return debitAccount;
					}
					//changes for artf52970 start
					else if (SMART_CARD_FUNDS_TRANSFER.equals(transactionType)){
						financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED, fields));
						financialMessage.setErrorCode(ATMConstants.WARNING);
						financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
						debitAccount = financialMessage.getAccount();
						return debitAccount;
					}
					//changes for artf52970 end
				}
				//changes for artf52970 start
				else if ((financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) &&
						SMART_CARD_FUNDS_TRANSFER.equals(transactionType)){
					financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_INVALID_ACCOUNT, fields));
					financialMessage.setErrorCode(ATMConstants.ERROR);
					financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
					debitAccount = financialMessage.getAccount();
					return debitAccount;
				}
				else if (SMART_CARD_FUNDS_TRANSFER.equals(transactionType) && 
						financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)){
					financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED, fields));
					financialMessage.setErrorCode(ATMConstants.WARNING);
					financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
					debitAccount = financialMessage.getAccount();
					return debitAccount;
				}
				//changes for artf52970 end
			}
		}
		//artf45119, artf45120 changes end
		String accountNumber = financialMessage.getAccount();
		String forcePost = financialMessage.getForcePost();
		if(transactionType.equals(SMART_CARD_DEBIT)||transactionType.equals(CREDIT_PURSE_CASH)){
			debitAccount=getAccountForCardDebitCash(financialMessage,env);
			return debitAccount;
		}
		if(isMessageExternalSale(transactionType)||transactionType.equals(EXTERNAL_POS_REFUND)||transactionType.equals(POS_REFUND)){
 			debitAccount = getAccountForOffUsPOS( financialMessage,  env);
		}
		if(isOffUsTransaction){
			financialMessage.setAccount(debitAccount);
			if (!doesBlockingTransactionExist) {
				debitAccount = ifBlockingNotExist( financialMessage,  env,  debitAccount);
			}
			return debitAccount; 
		}
		//artf46925 changes start
		BusinessValidatorBean validatorBean = new BusinessValidatorBean();
		if (validatorBean.validateAccountClosed(accValues, env)) {
			populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
					ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_CLOSED,
					BankFusionMessages.ERROR_LEVEL, financialMessage, field,
					env);
			if (logger.isDebugEnabled()) {
				logger.error("Account : " + financialMessage.getAccount()
						+ " is Closed !");
			}
		} else if (validatorBean.validateAccountStopped(accValues, env)) {
			if (financialMessage.getForcePost()
					.equals(ATMConstants.FORCEPOST_0)
					|| financialMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_6)) {
				populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
						ATMConstants.ERROR,
						CommonsEventCodes.E_ACCOUNT_STOPPED,
						BankFusionMessages.ERROR_LEVEL, financialMessage,
						field, env);
			} else if (financialMessage.getForcePost().equals(
					ATMConstants.FORCEPOST_1)
					|| financialMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_3)
					|| financialMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_5)
					|| financialMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_8)) {
				populateErrorDetails(
						ATMConstants.AUTHORIZED_MESSAGE_FLAG,
						ATMConstants.WARNING,
						ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
						BankFusionMessages.MESSAGE_LEVEL, financialMessage,
						field, env);
				if (controlDetails != null) {
					String psesudoName = CommonConstants.EMPTY_STRING;
					if (isPosTransacion) {
						if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
							psesudoName = atmControlDetails.getSmartCardMerchantDebitSuspenseAccount();
						} else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
							psesudoName = atmControlDetails.getSmartCardMerchantCreditSuspenseAccount();
							// Added check for POS refund
						} else if (transactionType.equals(POS_REFUND)||transactionType.equals(CREDIT_PURSE_CASH)) {
							psesudoName = atmControlDetails.getSmartCardMerchantPoolAccount();
						} else if (transactionType.equals(SMART_CARD_DEBIT)){
							psesudoName = atmControlDetails.getSmartCardPursePoolAccount();
						}
						//Changes for artf40256 starts.
						else if (transactionType.equals(EXTERNAL_POS_REFUND) && accountValidator.validateAccountStopped(accValues, env)){
							psesudoName = atmControlDetails.getSmartCardCreditSuspenseAccount();
						}
						//Changes for artf40256 ends.
						else {
							psesudoName = atmControlDetails.getSmartCardDebitSuspenseAccount();
						}

					} else {
						psesudoName = atmControlDetails.getPosOutwardAccount();
					}
					debitAccount = atmHelper.getAccountIDfromPseudoName(
							psesudoName, financialMessage
									.getCurrencySourceAccount(),
							CommonConstants.EMPTY_STRING, env);
					return debitAccount;
				}
			}
			logger.error("Account : " + financialMessage.getAccount()
					+ " is Stopped !");
		}
		//artf46925 changes end
		// added for artf45035 [start]		
		boolean isAccountValid = atmHelper.isAccountValid(financialMessage, PasswordProtectedConstants.OPERATION_DEBIT, env);
		if (!isAccountValid) {
			// added for artf45353 [start]
			if(SMART_CARD_FUNDS_TRANSFER.equals(transactionType))
			{
				if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) 
				{
							
					financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
					financialMessage.setErrorCode(ATMConstants.ERROR);
					financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_ACCOUNT_DORMANT,  fields));
					debitAccount = CommonConstants.EMPTY_STRING;
					return debitAccount;
				}				
				
			}
			// added for artf45353 [end]
			if (forcePost.equals(ATMConstants.FORCEPOST_0) || forcePost.equals(ATMConstants.FORCEPOST_6)) {				
				debitAccount = CommonConstants.EMPTY_STRING;
				return debitAccount;
		// added for artf45035 [end]
			} else if (forcePost.equals("1") || forcePost.equals(ATMConstants.FORCEPOST_3) || forcePost.equals(ATMConstants.FORCEPOST_5)
					|| forcePost.equals(ATMConstants.FORCEPOST_8)) {
				financialMessage.setErrorCode(ATMConstants.WARNING);
				financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.W_ACC_DORMANT_SUS_ACC_UPDTD,  fields));
				String pName = CommonConstants.EMPTY_STRING;
				if (isPosTransacion) {
					if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
						pName = atmControlDetails.getSmartCardMerchantDebitSuspenseAccount();
					} else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
						pName = atmControlDetails.getSmartCardMerchantCreditSuspenseAccount();
						// Added check for POS refund
					} else if (transactionType.equals(POS_REFUND)||transactionType.equals(CREDIT_PURSE_CASH)) {
						pName = atmControlDetails.getSmartCardMerchantPoolAccount();
					} else if (transactionType.equals(SMART_CARD_DEBIT)){
						pName = atmControlDetails.getSmartCardPursePoolAccount();
					}
					//Changes for artf40256 starts.
					else if (transactionType.equals(EXTERNAL_POS_REFUND) && accountValidator.validateAccountStopped(accValues, env)){
						pName = atmControlDetails.getSmartCardCreditSuspenseAccount();
					}
					//Changes for artf40256 ends.
					else {
						pName = atmControlDetails.getSmartCardDebitSuspenseAccount();
					}

				} else {
					pName = atmControlDetails.getPosOutwardAccount();
				}
				if(ifDispenseCurrencyToBeUsed){
				debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(),
						CommonConstants.EMPTY_STRING, env);
				}else {
					debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(),
							CommonConstants.EMPTY_STRING, env);
					
				}
				if(debitAccount.equals("")){
					field = new Object[] { pName };
					atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
				}
			}
			return debitAccount;
		}
		boolean isAccountPasswordProtected = false;  //Updated for artf40256.
		if (transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND)) {
			isAccountPasswordProtected = atmHelper.isAccountPasswordProtected(financialMessage, 
					PasswordProtectedConstants.OPERATION_CREDIT, ATMConstants.SOURCEACCOUNTTYPE, env);
		} else {
			isAccountPasswordProtected = atmHelper.isAccountPasswordProtected(financialMessage, 
					PasswordProtectedConstants.OPERATION_DEBIT,ATMConstants.SOURCEACCOUNTTYPE,  env);
		}
		if (!isAccountPasswordProtected) {   //Updated for artf40256.
			if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
				atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED,
						BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			}else if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)||financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)||
				financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)) {
				atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED,
						BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			String pName = CommonConstants.EMPTY_STRING;
			if (isPosTransacion) {

				if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
					pName = atmControlDetails.getSmartCardMerchantDebitSuspenseAccount();
					// check for POS refund
				} 
				//Changes for artf40289 starts.
				else if ((transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND))&& !isOffUsTransaction) {
					pName = atmControlDetails.getSmartCardCreditSuspenseAccount();
				}
				//Changes for artf40289 ends.
				else if (transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND)) {
					pName = atmControlDetails.getPosOutwardAccount();
				}else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
					pName = atmControlDetails.getSmartCardMerchantCreditSuspenseAccount();
				} else {
					pName = atmControlDetails.getSmartCardDebitSuspenseAccount();
				}

			} else {
				
					pName = atmControlDetails.getNetworkDrSuspenseAccount();
				
			}
			try{
				if(ifDispenseCurrencyToBeUsed) {
			debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING,
					env);
				}else {
					debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(),CommonConstants.EMPTY_STRING,
							env);
					
				}
			}
			catch(BankFusionException e){
				field = new Object[] { pName };
				atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			}
			return debitAccount;
		}
			}
		if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return debitAccount;
		}
		/*//changes for artf40256 starts.
		else
			if (isPosTransacion) {
				String pName = CommonConstants.EMPTY_STRING;
				 if (transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND)) {
					pName = atmControlDetails.getSmartCardCreditSuspenseAccount();
					debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(),
							CommonConstants.EMPTY_STRING, env);
				 }
			}
		//changes for artf40256 ends.	
*/		//changes for artf40285 starts
			if(!transactionType.equals(POS_REFUND)){
			debitAccount = financialMessage.getAccount();
		}	
		//changes for artf40285 ends
		if (!doesBlockingTransactionExist) {
			if(!(transactionType.equals(MERCHANT_CREDIT_TRANSACTION)||transactionType.equals(MERCHANT_DEBIT_TRANSACTION))){
			debitAccount = ifBlockingNotExist( financialMessage,  env,  debitAccount);
			}
		}
		return debitAccount;

	}	
	
	/**
	 * @returns Boolean
	 * This Method if blocking transaction does not exist then to which account the posting should happen.
	 */
	
	private String ifBlockingNotExist(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env, String debitAccount){
		
		if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)&&(!transactionType.equals(POS_REFUND))&&(!transactionType.equals(EXTERNAL_POS_REFUND))) {
			String errorMessage = "Pre Authorization Does Not Exist";
			financialMessage.setErrorCode(ATMConstants.WARNING);
			financialMessage.setErrorDescription(errorMessage);
			logger.warn(errorMessage);
		} else if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
			String errorMessage = "Pre Auth Does Not Exist. Posting to Suspense Account";
			financialMessage.setErrorCode(ATMConstants.CRITICAL);
			financialMessage.setErrorDescription(errorMessage);
			logger.warn(errorMessage);
			String pName = CommonConstants.EMPTY_STRING;
			if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
					pName = atmControlDetails.getSmartCardCreditSuspenseAccount();
				} else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
					pName = atmControlDetails.getSmartCardDebitSuspenseAccount();
				} else {
					pName = atmControlDetails.getSmartCardDebitSuspenseAccount();
				}
			if(ifDispenseCurrencyToBeUsed){ 
			debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null, env);
			}else{
				debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null, env);	
			}
			
			if(debitAccount.equals("")){
				field = new Object[] { pName };
				atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			}
		}
	return debitAccount;
	}
	
	/**
	 * @returns Boolean
	 * This Method is to check if the message is POS or External
	 */
	private boolean isMessagePOSExternal(String transactionType){
		if (transactionType.equals("610")||transactionType.equals("611")||transactionType.equals("612")||transactionType.equals("613")||transactionType.equals("621")||transactionType.equals("622")||transactionType.equals("624")){
			return true;
		}
		return false;
	}
	
	/**
	 * @returns Boolean
	 * This Method is to get the Contra Account for the Smart Card Transaction
	 */
	protected String getContraAccount(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env) {
		String contraAccount = CommonConstants.EMPTY_STRING;
		String pName = CommonConstants.EMPTY_STRING;
		atmControlDetails = ATMConfigCache.getInstance().getInformation(env);
		boolean isMessagePOSExternal=isMessagePOSExternal(transactionType);
		if(financialMessage.getVariableDataType().equals("E") && 
			( transactionType.equals(External_Sale)    ||
			 transactionType.equals(External_CASHBACK)||
			 transactionType.equals(External_QUASICASH) ||
			 transactionType.equals(External_POSCASH))  ||
			 transactionType.equals(EXTERNAL_POS_REFUND)){
		/*
		 * If it is not a POS txn and it is an external network txn , then it should pick up the contra from ext network settlement account.*/
		
		ATMExNwMessage message = (ATMExNwMessage) financialMessage;
		pName = getExternalNetworkSuspenceAccount(message.getExternalNetworkID(), financialMessage.getSourceCountryCode(), financialMessage
				.getSourceIMD(), env);

		
	}else if (isPosTransacion) {
			if(isMessagePOSExternal||transactionType.equals(MERCHANT_CREDIT_TRANSACTION)||transactionType.equals(MERCHANT_DEBIT_TRANSACTION)||transactionType.equals(SMART_CARD_DEBIT)||
					transactionType.equals(EXTERNAL_POS_REFUND)||(transactionType.equals(POS_REFUND))){ //Added New condition for artf40249 & artf40280 for external POS refund check.
				pName= atmControlDetails.getSmartCardMerchantPoolAccount();
			}
			else{
			pName = atmControlDetails.getSmartCardPursePoolAccount();
			}

		}else if(transactionType.equals(POS_REFUND)||transactionType.equals(EXTERNAL_POS_REFUND)||isMessageExternalSale(transactionType)){
			pName= atmControlDetails.getSmartCardMerchantPoolAccount();
		}else {
			ATMExNwMessage message = (ATMExNwMessage) financialMessage;
			pName = getExternalNetworkSuspenceAccount(message.getExternalNetworkID(), financialMessage.getSourceCountryCode(), financialMessage
					.getSourceIMD(), env);
		}
		if(ifDispenseCurrencyToBeUsed){
		contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null, env);
		}else{
			contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null, env);	
		}
		//changes for artf40571 starts
		if (contraAccount.equals(CommonConstants.EMPTY_STRING)) {
			financialMessage.setErrorCode(ATMConstants.CRITICAL);
			financialMessage.setErrorDescription("Invalid Purse Pool Account, posting to suspense account");
		}
		else if (contraAccount.equals(CommonConstants.EMPTY_STRING) && financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
		//changes for artf40571 ends
			financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			financialMessage.setErrorCode(ATMConstants.CRITICAL);
			financialMessage.setErrorDescription("Invalid Purse Pool Account");
		}
		if (!contraAccount.equals(CommonConstants.EMPTY_STRING)) {
			return contraAccount;
		}
		pName = CommonConstants.EMPTY_STRING;
		if(financialMessage.getVariableDataType().equals("E") && 
				( transactionType.equals(External_Sale)    ||
				 transactionType.equals(External_CASHBACK)||
				 transactionType.equals(External_QUASICASH) ||
				 transactionType.equals(External_POSCASH))  ||
				 transactionType.equals(EXTERNAL_POS_REFUND)){
			pName = atmControlDetails.getNetworkCrSuspenseAccount();
		}else if (isPosTransacion) {
			// Added check for POS refund
			if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION) || transactionType.equals(EXTERNAL_POS_REFUND)
					|| transactionType.equals(POS_REFUND)) {
				pName = atmControlDetails.getSmartCardDebitSuspenseAccount();
			}
			//Condition added for artf40479 starts.
			else if((transactionType.equals(MERCHANT_CREDIT_TRANSACTION))&& (contraAccount.equals(CommonConstants.EMPTY_STRING))){
				pName = atmControlDetails.getSmartCardMerchantDebitSuspenseAccount();
			}
			//Condition added for artf40479 ends.
			else {
				pName = atmControlDetails.getSmartCardCreditSuspenseAccount();
			}
		} else {
			pName = atmControlDetails.getNetworkCrSuspenseAccount();
		}
		try {
			if(ifDispenseCurrencyToBeUsed) {
		contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null, env);
		}else {
			contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null, env);	
		}
		}
		catch(BankFusionException e){
			field = new Object[] { pName };
			atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
		}
		return contraAccount;
	}
	/**
	 * @returns String
	 * This Method is to Check if the message is off us or On us 
	 * 
	 */
	private int isOffUs(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env){
		ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
		int cardAccountIndicator=0;
		boolean isCardValid = atmMessageValidator.isCardNumberValid(financialMessage.getCardNumber(), env);
		boolean isAccountValid = atmMessageValidator.isAccountValid(financialMessage.getAccount(), env);
		if(!isCardValid&&!isAccountValid){
			cardAccountIndicator = 1;
		}
		else if(!isCardValid&& isAccountValid){
			cardAccountIndicator = 2;
		}
		else if(isCardValid&& !isAccountValid){
			cardAccountIndicator = 3;
		}
		else if(isCardValid&& isAccountValid){
			cardAccountIndicator = 0;
		}
		return cardAccountIndicator;
	}
	/**
	 * @returns int
	 * This Method is to get the account if the message is off us or On us 
	 * 
	 */
	private String getAccountForOffUsPOS(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env) {
		String debitAccount=CommonConstants.EMPTY_STRING;
		String psName=CommonConstants.EMPTY_STRING;
		ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
		String cardNumber = financialMessage.getCardNumber();
		Object[] field = new Object[] { financialMessage.getCurrencyDestDispensed() };
		int cardAccountValid = isOffUs(financialMessage, env);
		if(cardAccountValid==1){
			try{
			psName = atmControlDetails.getPosOutwardAccount();
			debitAccount = atmHelper.getAccountIDfromPseudoName(psName, financialMessage.getCurrencySourceAccount(), null, env);
			} catch(BankFusionException e){
					field = new Object[] { psName };
					atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			}
			isOffUsTransaction = true;
		}
		//Changes for artf40289 starts.
		else if(transactionType.equals(POS_REFUND)&&(atmMessageValidator.isSmartCard(cardNumber, env))&&(atmControlDetails.getSmartCardSupported().equals("N"))){
			debitAccount=financialMessage.getAccount();
		}
		//Changes for artf40289 ends.
		else if(transactionType.equals(POS_REFUND)&&(atmMessageValidator.isSmartCard(cardNumber, env))){
			try{
			psName = atmControlDetails.getSmartCardPursePoolAccount();
			debitAccount = atmHelper.getAccountIDfromPseudoName(psName, financialMessage.getCurrencySourceAccount(), null, env);
			}
			catch(BankFusionException e){
				field = new Object[] { psName };
				atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			}
		}
		else{
			debitAccount = financialMessage.getAccount();
		}
	
		return debitAccount;
	}
	/**
	 * @returns String
	 * This Method is to get account for the Sale external messages. 
	 * 
	 */
	private String getAccountForCardDebitCash(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env){
		String debitAccount = CommonConstants.EMPTY_STRING;
		String psName = CommonConstants.EMPTY_STRING;
		Object[] field = new Object[] { psName };
		if(transactionType.equals(SMART_CARD_DEBIT)){
			psName = atmControlDetails.getSmartCardPursePoolAccount();
			debitAccount = atmHelper.getAccountIDfromPseudoName(psName, financialMessage.getCurrencySourceAccount(), null, env);
			if(debitAccount.equals("")){
			field = new Object[] { psName };
				atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			}
		}
		else{
			psName = atmControlDetails.getSmartCardMerchantPoolAccount();
			debitAccount = atmHelper.getAccountIDfromPseudoName(psName, financialMessage.getCurrencySourceAccount(), null, env);
			if(debitAccount.equals("")){
				field = new Object[] { psName };
				atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.E_INTERNAL_ACCOUNT_FOR_PSEUDONYM, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
			}
		}
		return debitAccount;
	}
	/**
	 * @returns boolean
	 * This Method is to Check if the message type is one of the following 614,633,628,610,611,612,613,622,624
	 * 
	 */
	private boolean isMessageExternalSale(String transactionType){
	//artf46639 Changes started
		if(transactionType.equals(External_Sale)
				||transactionType.equals(External_CASHBACK)
					||transactionType.equals(External_QUASICASH)
						||transactionType.equals(External_POSCASH)
							||transactionType.equals(External_Sale_REQUEST)
								||transactionType.equals(External_CASHREQUEST) 
									||transactionType.equals(External_CASH_REQUEST)){
			//artf46639 Changes ends
				return true;
		}
		return false;
	}
	/**
	 * @returns boolean
	 * This an overridden method of POS Cash Processor Method is to validate transaction
	 * 
	 */
	protected void validateTransaction(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
		String magStripTrans = "N";
		atmControlDetails = ATMConfigCache.getInstance().getInformation(env);
		boolean isAccountMappedtoCard = false;
		ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
		magStripTrans = atmControlDetails.getProcessMagstripeTxns();
		if (transactionType.equals(POS_REFUND)) {
					atmMessageValidator.validateMessage(atmPosMessage, env, "local");
		} else {
			atmMessageValidator.validateMessage(atmPosMessage, env, "external");
		}

		if (atmPosMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))
			return;

		if (atmMessageValidator.doesCardExist(atmPosMessage.getCardNumber(), env)) {
			field = new Object[] { atmPosMessage.getCardNumber(),atmPosMessage.getAccount() };
			isAccountMappedtoCard = atmMessageValidator.isAccountMappedToCard(atmPosMessage.getCardNumber(), atmPosMessage.getAccount(), env);
			if (!isAccountMappedtoCard) {
				if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
//					atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
//					atmPosMessage.setErrorCode(ATMConstants.ERROR);
//					atmPosMessage.setErrorDescription("Card Not Mapped to Account");
					
					 populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
								BankFusionMessages.ERROR_LEVEL,atmPosMessage, field, env);
					return;
				}

				if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_1) || atmPosMessage.getForcePost().equals("7")
						|| atmPosMessage.getForcePost().equals("3")|| atmPosMessage.getForcePost().equals("5")|| atmPosMessage.getForcePost().equals("8")) {
					atmPosMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
					//atmPosMessage.setErrorCode("WARNING");
					//atmPosMessage.setErrorDescription("Card Not Mapped to Account: Force Post not Posted.");
					
						
							
							String pseudoName = CommonConstants.EMPTY_STRING;
							pseudoName = atmControlDetails.getSmartCardDebitSuspenseAccount();
							mainAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmPosMessage.getCurrencySourceAccount(),null, env);
					 populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC,
						BankFusionMessages.ERROR_LEVEL,atmPosMessage, field, env);
					 
					 	//return;
						}
				}
			
		}
		
		// added for artf45353 [start]
//		if(SMART_CARD_FUNDS_TRANSFER.equals(transactionType))
//		{
//			isAccountMappedtoCard = atmMessageValidator.isAccountMappedToCard(atmPosMessage.getCardNumber(), atmPosMessage.getAccount(), env);
//			if (!isAccountMappedtoCard) 
//			{
//				if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) 
//				{					
//					//changes for artf52970 start
//					atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
//					atmPosMessage.setErrorCode(ATMConstants.ERROR);
//					atmPosMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_INVALID_ACCOUNT, fields));
//					//changes for artf52970 end
//					return;
//					
//				}	
//				else if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_5) 
//						//changes for artf52970 start
//						|| atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)
//						//changes for artf52970 end
//						){
//					fields = new Object[] { atmPosMessage.getAccount() };
//					atmPosMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
//					atmPosMessage.setErrorCode(ATMConstants.WARNING);
//					atmPosMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED, fields));
//					atmPosMessage.setAccount(atmHelper.getAccountIDfromPseudoName(
//							atmControlDetails.getSmartCardDebitSuspenseAccount(), atmPosMessage
//							.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env));
//					commissionAccount = atmHelper.getAccountIDfromPseudoName(
//							atmControlDetails.getSmartCardDebitSuspenseAccount(), atmPosMessage
//							.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
//				}
//			}
//		}
		// added for artf45353 [end]
		
		transactionCode = atmHelper.getBankTransactionCode(atmPosMessage.getMessageType() + atmPosMessage.getTransactionType(), env);
		//artf210330 changes start
		atmHelper.updateTransactionNarration(atmPosMessage, env);
		customerTransactionNarration = atmPosMessage.getTxnCustomerNarrative();
		contraTransactionNarration = atmPosMessage.getTxnContraNarrative();
		//artf210330 changes ends
		if (transactionCode.equals(CommonConstants.EMPTY_STRING)) {
			if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
				String errorMessage = "Transaction Not Mapped";
				atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				atmPosMessage.setErrorCode(ATMConstants.WARNING);
				atmPosMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				return;
			} else {
				String errorMessage = "Transaction Not Mapped. Using Default Transaction Type";
				atmPosMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				atmPosMessage.setErrorCode(ATMConstants.ERROR);
				atmPosMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				transactionCode = controlDetails.getSmartCardDefaultTransactionType();
			}
		}

	}
	/**
	 * @returns void
	 * This an overridden method of POS Cash Processor.This Method is to perform blocking
	 * 
	 */
	protected void performBlocking(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
		BigDecimal amount = atmPosMessage.getAmount1().abs();
		BigDecimal availableBalance = atmHelper.getAvailableBalance(atmPosMessage.getAccount(), env);
		String account  = atmPosMessage.getAccount();
		String message = CommonConstants.EMPTY_STRING;
		Boolean forcePost = new Boolean(false);
		if (amount.equals(CommonConstants.BIGDECIMAL_ZERO)) {
			message = "Zero Amount Blocking Not Allowed";
			atmPosMessage.setErrorCode(ATMConstants.WARNING);
			atmPosMessage.setErrorDescription(message);
			atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			return;
		}
		
		if (availableBalance.compareTo(amount) < 0 && account.equals(mainAccount)&& !atmPosMessage.getForcePost().equals("7")){
			message = "Available balance is insufficient for this transaction";
			atmPosMessage.setErrorCode(ATMConstants.WARNING);
			atmPosMessage.setErrorDescription(message);
			atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			return;
		}
		
		String blockingCategory = "ATM";
		String reference = getTransactionReference(atmPosMessage);
		Date postingDate = SystemInformationManager.getInstance().getBFBusinessDate();
		Timestamp unBlockingDateTime = getUnBlockingDate(atmPosMessage, env);
		HashMap<String,Object> paramsforTellerBlocking  = new HashMap<String,Object>();
		paramsforTellerBlocking.put("ACCOUNTID", mainAccount);
		paramsforTellerBlocking.put("AMOUNT", amount.abs());
		paramsforTellerBlocking.put("BLOCKINGCATEGORY", blockingCategory);
		paramsforTellerBlocking.put("NARRATIVE", customerTransactionNarration);
		paramsforTellerBlocking.put("POSTINGDATE", postingDate);
		paramsforTellerBlocking.put("TRANSACTIONCODE", transactionCode);
		paramsforTellerBlocking.put("TRANSACTIONREFERENCE", reference);
		paramsforTellerBlocking.put("UNBLOCKINGDATETIME", unBlockingDateTime);
		paramsforTellerBlocking.put("ISBLOCKING", new Boolean(true));
		paramsforTellerBlocking.put("POSTINGACTION", "C");
		if(atmPosMessage.getForcePost().equals("7")){
			forcePost = new Boolean(true);
		}
		paramsforTellerBlocking.put("FORCEPOST", forcePost);
		
		try {
			MFExecuter.executeMF(ATMConstants.BLOCKING_TRANSACTION, env, paramsforTellerBlocking);
			env.getFactory().commitTransaction();
			env.getFactory().beginTransaction();
		} catch (BankFusionException exception) {
			message = exception.getMessage();
			atmPosMessage.setErrorCode(ATMConstants.WARNING);
			atmPosMessage.setErrorDescription(message);
			atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			try {
				env.getFactory().rollbackTransaction();
				env.getFactory().beginTransaction();
			} catch (Exception ignored) {
				logger.info("Failed to rollback the transaction");
			}
			return;
		} finally {
			try {
				env.getFactory().beginTransaction();
			} catch (Exception ignored) {
				logger.info("Failed to begin the transaction");
			}
		}
	}
	/**
	 * @returns void
	 * This an overridden method of POS Cash Processor.To post the transaction. 
	 * 
	 */
	protected void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
		HashMap map = new HashMap();
		String accountCurrencyCode = CommonConstants.EMPTY_STRING;
		String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
		////ChannelID code changes starts.
		String messagenumber=message.getMessageType()+message.getTransactionType();
		
		try {
			if(ifDispenseCurrencyToBeUsed){
			dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencyDestDispensed(), true);
			}
			accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencySourceAccount(), true);
		} catch (BankFusionException exception) {

		}
		map.put("TRANSACTIONSOURCE", "POS");
		map.put("COMMISSION_ACCOUNT", commissionAccount);
		map.put("ACCOUNT1_ACCOUNTID", mainAccount);
		map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
		map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
		//artf210330 changes start
		map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
		//artf210330 changes ends
		map.put("ACCOUNT1_TRANSCODE", transactionCode);

		map.put("ACCOUNT2_ACCOUNTID", contraAccount);
		if(ifDispenseCurrencyToBeUsed){
		map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
		map.put("ACCOUNT2_AMOUNT_CurrCode", dispensedCurrencyCode);
		}else {
			map.put("ACCOUNT2_AMOUNT", message.getAmount1().abs());
			map.put("ACCOUNT2_AMOUNT_CurrCode", accountCurrencyCode);
			
		}
		// artf210330 changes starts
		map.put("ACCOUNT2_NARRATIVE",contraTransactionNarration);
		//artf210330 changes ends
		map.put("ACCOUNT2_TRANSCODE", transactionCode);
		map.put("AMOUNT4", message.getAmount4().abs());
		map.put("MAINACCOUNTID", message.getAccount()); //Updated for artf40345.
		map.put("MESSAGENUMBER", messagenumber);
		// Check if the Transaction type is not 614 && 623 && 625 then set
		// Account1 as "C" Account2 as "D"
		if (!(MERCHANT_CREDIT_TRANSACTION.equals(transactionType)) && !(transactionType.equals(EXTERNAL_POS_REFUND))
				&& !(transactionType.equals(POS_REFUND))) {
			map.put("ACCOUNT1_POSTINGACTION", "D");
			map.put("ACCOUNT2_POSTINGACTION", "C");
		} else {
			map.put("ACCOUNT1_POSTINGACTION", "C");
			map.put("ACCOUNT2_POSTINGACTION", "D");

		}
		if(messagenumber.equals("620")||messagenumber.equals("680")){ //Updated for artf34792.
			map.put("CHANNELID", "ATM");
		}else{
			map.put("CHANNELID", "POS");
		}
		map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
		map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
		map.put("TRANSACTIONREFERENCE", getTransactionReference(message));

		if (ATMConstants.FORCEPOST_0.equals(message.getForcePost()) || ATMConstants.FORCEPOST_6.equals(message.getForcePost())) {
			map.put("FORCEPOST", new Boolean(false));
		} else {
			map.put("FORCEPOST", new Boolean(true));
		}
		map.put("CARDNUMBER", message.getCardNumber());

		// Post the Transactions.
		try {
			HashMap outputParams = MFExecuter.executeMF(ATMConstants.FINANCIAL_POSTING_MICROFLOW_NAME, env, map);
			String authorizedFlag = outputParams.get("AUTHORIZEDFLAG").toString();
				if (authorizedFlag.equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
				String errorMessage = outputParams.get("ERRORMESSAGE").toString();
				message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				//changes for artf40571 starts
				if (!errorMessage.equals("")) {
				//changes for artf40571 ends	
					message.setErrorCode(ATMConstants.WARNING);
					message.setErrorDescription(errorMessage);
				}
				logger.error(errorMessage);
			}
			
			if (authorizedFlag.equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
				String errorMessage = outputParams.get("ERRORMESSAGE").toString();
				message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				message.setErrorCode(ATMConstants.ERROR);
				message.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				try {
					env.getFactory().rollbackTransaction();
					env.getFactory().beginTransaction();
				} catch (Exception ignored) {
				}
				return;
			}
			env.getFactory().commitTransaction();
			env.getFactory().beginTransaction();
		} catch (BankFusionException exception) {
			logger.info(exception.getMessage());
			message.setErrorCode(ATMConstants.ERROR);
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorDescription(exception.getMessage());
			try {
				env.getFactory().rollbackTransaction();
				env.getFactory().beginTransaction();
			} catch (Exception ignored) {
				logger.info("Failed to rollback the transaction");
			}
		} finally {
			try {
				env.getFactory().beginTransaction();
			} catch (Exception ignored) {
				logger.info("Failed to begin the transaction");
			}
		}
	}
	
	// added for artf44890 [start]
	protected Timestamp getUnBlockingDate(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
		int unBlockingDays = 0;
		Date unBlockingDate = null;
		ArrayList params = new ArrayList();
		ArrayList params1 = new ArrayList();
		ArrayList params2 = new ArrayList();
		params.add(atmPosMessage.getCardDestinationIMD());
		String ATMTransCode = atmPosMessage.getMessageType() + atmPosMessage.getTransactionType();
		
		params2.add(ATMTransCode);
        IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params2, false);

		//SimplePersistentObject ATMTransactionCodesList = env.getFactory().findByPrimaryKey(IBOATMTransactionCodes.BONAME, ATMTransCode);

		IBOATMTransactionCodes misTransCodes = (IBOATMTransactionCodes) atmTransactionCodes;
		params.add(misTransCodes.getF_MISTRANSACTIONCODE());

		List cardIssuersList = env.getFactory().findByQuery(IBOATMPOSBLOCKINGCONF.BONAME, getPOSBlockingDetails, params, null);
		if (cardIssuersList.size() == 1) {
			IBOATMPOSBLOCKINGCONF cardIssuersDetails = (IBOATMPOSBLOCKINGCONF) cardIssuersList.get(0);
			unBlockingDays = cardIssuersDetails.getF_BLOCKINGPERIOD();
			if (unBlockingDays == 0) {
				unBlockingDays = Integer.parseInt(ATMConfigCache.getInstance().getInformation(env).getSmartCardBlockingPeriod());
			}
		} else {
			unBlockingDays = Integer.parseInt(ATMConfigCache.getInstance().getInformation(env).getSmartCardBlockingPeriod());
		}
		unBlockingDate = AddDaysToDate.run(atmPosMessage.getDateTimeofTxn(), unBlockingDays);
		Timestamp unBlockingTimestamp = new Timestamp(unBlockingDate.getTime());
		return unBlockingTimestamp;
	}
	// added for artf44890 [end]
}
