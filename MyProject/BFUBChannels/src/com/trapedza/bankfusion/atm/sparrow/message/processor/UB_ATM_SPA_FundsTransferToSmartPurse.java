/* **********************************************************
 * Copyright (c) 2009 Misys Software Solutions
 *
 * This software is the proprietary information of Misys International Financial
 * Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * Revision 1.0  Creation 09 April 2009 Karthikeyan M
 * Sprint 4 - FundTransferToSmartPurse - Message type 584 for fund transfer
 * from CardHoldersAccount to SmartPursePoolAccount
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
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * The UB_ATM_SPA_FundsTransferToSmartPurse class processes the message for
 * Smart Card Fund Transfer from CardHolder's Account to SmartPursePool
 * Account(Message Type - 584). This class extends the ATMFundsTransfer class
 * for common message validations and performs account validations that are
 * specific to the ATMFund Transfer(Message - 540). The transactions are then
 * posted using the postingEngine.
 */
public class UB_ATM_SPA_FundsTransferToSmartPurse extends ATMFundsTransfer {

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
	private static final transient Log logger = LogFactory
			.getLog(ATMFundsTransfer.class.getName());

	/**
	 * This method returns the SmartCardDebitSuspenseAccount and can be
	 * overridden anywhere to change the source suspense account
	 *
	 * @return SmartCardDebitSuspenseAccount
	 */
	protected String getSourceSuspenseAccount() {

		return controlDetails.getSmartCardDebitSuspenseAccount();
	}

	/**
	 * This method returns the SmartCardCreditSuspenseAccount and can be
	 * overridden anywhere to change the Destination suspense account
	 *
	 * @return SmartCardCreditSuspenseAccount
	 */
	protected String getDestinationSuspenseAccount() {

		return controlDetails.getSmartCardCreditSuspenseAccount();
	}

	// Validation for Card Holders Account. In 584 Message Source Account is the
	// Card Holders Account
	/**
	 * Validation for Card Holder's Account - Source Account for 584 Message
	 * Type This method validates the Source Account in the ATM Sparrow message.
	 *
	 * @param atmLocalMessage
	 *            it stores the ATM Sparrow Local Messages.
	 * @param env
	 *            it holds the Session variables.
	 */

	protected void validateCardHoldersAccount(ATMLocalMessage atmLocalMessage,
			BankFusionEnvironment env) {
		Object[] field = new Object[] { atmLocalMessage.getAccount() };
		IBOAttributeCollectionFeature accountItem = null;
		IBOAccount messageAccount =null; //Added for Invalid CardHolder's Account.
		// check for closed or stopped accounts
		// Validation for Account existence.
		accountItem = (IBOAttributeCollectionFeature) BankFusionThreadLocal
				.getPersistanceFactory().findByPrimaryKey(
						IBOAttributeCollectionFeature.BONAME,
						atmLocalMessage.getAccount(), true);
		cardHoldersAccount = atmLocalMessage.getAccount();
		 //validation for Invalid Cardholder's Account that is not present in Database starts.
        messageAccount = (IBOAccount) BankFusionThreadLocal.getPersistanceFactory().
                                           findByPrimaryKey(IBOAccount.BONAME, cardHoldersAccount,true);
        if(messageAccount==null){
        	if(atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
        		||atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)||
        		  atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)){
            atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
            		ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCOUNT,
                    BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
            if (logger.isDebugEnabled()) {
                logger.error(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                		ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCOUNT,
                		field));
            }
        }
        	if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                return;
            }
      }

      //validation for Invalid Cardholder's Account that is not present in Database ends.
		if (accountItem == null) {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
				atmHelper.populateErrorDetails(
						ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
						ATMConstants.WARNING,
						ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCOUNT,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field,
						env);
				logWarn(BankFusionMessages
						.getFormattedMessage(
								ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCOUNT,
								field));

			} else if (atmLocalMessage.getForcePost().equals(
					ATMConstants.FORCEPOST_1) || (atmLocalMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_3))) {
				atmHelper
						.populateErrorDetails(
								ATMConstants.AUTHORIZED_MESSAGE_FLAG,
								ATMConstants.WARNING,
								ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED,
								BankFusionMessages.ERROR_LEVEL,
								atmLocalMessage, field, env);
				logWarn(BankFusionMessages
						.getFormattedMessage(
								ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED,
								field));

				if (controlDetails != null) {
					String pseudoName = getSourceSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(
							pseudoName, atmLocalMessage
									.getCurrencyDestDispensed(),
							CommonConstants.EMPTY_STRING, env);
					return;
				}
			}
		} else
		// check for closed accounts
		if (accountItem != null) {
			BusinessValidatorBean validatorBean = new BusinessValidatorBean();
			if (validatorBean.validateAccountClosed(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_0)) {
					atmHelper.populateErrorDetails(
							ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
							ATMConstants.WARNING,
							CommonsEventCodes.E_ACCOUNT_CLOSED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
							field, env);
					logError(BankFusionMessages.getFormattedMessage(
							CommonsEventCodes.E_ACCOUNT_CLOSED, field));

				} else if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_1)||(atmLocalMessage.getForcePost().equals(
								ATMConstants.FORCEPOST_3))) {
					atmHelper
							.populateErrorDetails(
									ATMConstants.AUTHORIZED_MESSAGE_FLAG,
									ATMConstants.WARNING,
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									BankFusionMessages.ERROR_LEVEL,
									atmLocalMessage, field, env);
					logWarn(BankFusionMessages
							.getFormattedMessage(
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									field));

					if (controlDetails != null) {
						String pseudoName = getSourceSuspenseAccount();
						cardHoldersAccount = atmHelper
								.getAccountIDfromPseudoName(pseudoName,
										atmLocalMessage
												.getCurrencySourceAccount(),
										CommonConstants.EMPTY_STRING, env);
						return;
					}
				}
			}
			// check for stopped accounts
			else if (validatorBean.validateAccountStopped(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_0)) {
					atmHelper.populateErrorDetails(
							ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
							ATMConstants.WARNING,
							CommonsEventCodes.E_ACCOUNT_STOPPED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
							field, env);
					logWarn(BankFusionMessages.getFormattedMessage(
							CommonsEventCodes.E_ACCOUNT_STOPPED, field));

				} else if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_1)||(atmLocalMessage.getForcePost().equals(
								ATMConstants.FORCEPOST_3))) {
					atmHelper
							.populateErrorDetails(
									ATMConstants.AUTHORIZED_MESSAGE_FLAG,
									ATMConstants.WARNING,
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									BankFusionMessages.ERROR_LEVEL,
									atmLocalMessage, field, env);
					logError(BankFusionMessages
							.getFormattedMessage(
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									field));

					if (controlDetails != null) {
						String pseudoName = getSourceSuspenseAccount();
						cardHoldersAccount = atmHelper
								.getAccountIDfromPseudoName(pseudoName,
										atmLocalMessage
												.getCurrencySourceAccount(),
										CommonConstants.EMPTY_STRING, env);
						return;
					}
				}
			}
		}
		// Check ATMCardAccMap to see whether the card no and the account are
		// mapped.
		if (!atmMessageValidator.isAccountMappedToCard(atmLocalMessage
				.getCardNumber(), atmLocalMessage.getAccount(), env)) {
			field = new Object[] { atmLocalMessage.getCardNumber(),
					atmLocalMessage.getAccount() };
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
					|| atmLocalMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_6)) {
				atmHelper
						.populateErrorDetails(
								ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
								ATMConstants.WARNING,
								ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
								BankFusionMessages.ERROR_LEVEL,
								atmLocalMessage, field, env);
				logWarn(BankFusionMessages
						.getFormattedMessage(
								ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
								field));

			}
			// added for artf44619 [start]
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
					|| atmLocalMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_3)){


				Object[] fieldForInvalidAcc = new Object[] { atmLocalMessage.getAccount() };
				/*atmHelper
						.populateErrorDetails(
								ATMConstants.AUTHORIZED_MESSAGE_FLAG,
								ATMConstants.WARNING,
								ChannelsEventCodes.W_INVALID_CARD_HOLDER_ACCOUNT_SUSP_ACCOUNT_UPDATED,
								BankFusionMessages.ERROR_LEVEL,
								atmLocalMessage, fieldForInvalidAcc, env);
				logError(BankFusionMessages
						.getFormattedMessage(
								ChannelsEventCodes.W_INVALID_CARD_HOLDER_ACCOUNT_SUSP_ACCOUNT_UPDATED,
								fieldForInvalidAcc));*/

				//For Fix artf792517
				atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				if (controlDetails != null) {
					String psesudoName = getSourceSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
							.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
				}
					//For Fix artf792517(ended)

				if (controlDetails != null) {
					String pseudoName = getSourceSuspenseAccount();
					cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(
							pseudoName, atmLocalMessage
									.getCurrencySourceAccount(),
							CommonConstants.EMPTY_STRING, env);
					return;
				}
			}
			// added for artf44619 [end]
		}
		if (atmLocalMessage.getAuthorisedFlag().equals(
				ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		// Checking for Password protection Flag.
		// added for artf44710 [start]
		boolean result = atmHelper.isAccountValid(atmLocalMessage, PasswordProtectedConstants.OPERATION_DEBIT, env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		if (!result) {
			String psesudoName = getSourceSuspenseAccount();
			cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
					.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
			return;
		}
		boolean passwordProtected = atmHelper.isAccountPasswordProtected(atmLocalMessage,
				PasswordProtectedConstants.OPERATION_DEBIT, ATMConstants.SOURCEACCOUNTTYPE, env);
		if(passwordProtected){
			return;
		}
		else {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)){
				atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR, ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
			} else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)||atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
				atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, ChannelsEventCodes.W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
				String psesudoName = getSourceSuspenseAccount();
				cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage
						.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
			}
			return;
		}
		// added for artf44710 [end]
	}

	/**
	 * This method is used to return error log if debugging is enabled
	 */

	private void logError(String formattedMessage) {
		if (logger.isDebugEnabled()) {
			logger.error(formattedMessage);
		}
	}

	/**
	 * This method is used to return warning log if debugging is enabled
	 */

	private void logWarn(String formattedMessage) {
		if (logger.isDebugEnabled()) {
			logger.warn(formattedMessage);
		}
	}

	/**
	 *
	 * Method Description:All the validation that are related to source account
	 * and destination account will happen in validateFundsTransferDetails method.
	 * @param atmSparrowMessage it stores the ATM Sparrow Local Messages.
	 * @param env it holds the Session variables.
	 */

	protected void validateFundsTransferDetails(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
		atmMessageValidator.validateMessage(atmLocalMessage, env, ATMMessageValidator.LOCAL_MESSGE_TYPE);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		transactionCode = atmHelper.getBankTransactionCode(atmLocalMessage.getMessageType()
				+ atmLocalMessage.getTransactionType(), env);
		atmHelper.updateTransactionNarration(atmLocalMessage, env);
		customerTransactionNarration = atmLocalMessage.getTxnCustomerNarrative();
		contraTransactionNarration = atmLocalMessage.getTxnContraNarrative();

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
			else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
					|| atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
				String errorMessage = "Transaction Not Mapped, Using Default Transaction Type";
				atmLocalMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				atmLocalMessage.setErrorCode(ATMConstants.ERROR);
				atmLocalMessage.setErrorDescription(errorMessage);
				transactionCode = getDefaultTransactionType();
			}
		}
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		validateCardHoldersAccount((ATMLocalMessage) atmLocalMessage, env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		if ((((ATMSparrowFinancialMessage) atmLocalMessage).getAccount()).equals(cardHoldersAccount)) {
			atmMessageValidator.validateSourceCurrency((ATMSparrowFinancialMessage) atmLocalMessage, env);
		}
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		validateDestAccount((ATMLocalMessage) atmLocalMessage, env);
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		if ((((ATMSparrowFinancialMessage) atmLocalMessage).getDestAccountNumber().substring(0, 14))
				.equals(destAccount)) {
			atmMessageValidator.validateDestCurrency((ATMSparrowFinancialMessage) atmLocalMessage, env);
		}
		if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
	}



	/**
	 * Validation for DestinationAccount - SmartPursePoolAccount(Internal
	 * Account).
	 *
	 * @param atmLocalMessage
	 *            it stores the ATM Sparrow Local Messages.
	 * @param envholds
	 *            the Session variables.
	 */

	@Override
	protected void validateDestAccount(ATMLocalMessage atmLocalMessage,
			BankFusionEnvironment env) {
		ATMControlDetails moduleDetails = new ATMControlDetails(env);
		String smartPursePoolAccountName = moduleDetails
				.getSmartCardPursePoolAccount();
		String smartPursePoolAccountNumber = atmHelper
				.getAccountIDfromPseudoName(smartPursePoolAccountName,
						atmLocalMessage.getCurrencyDestDispensed(),
						CommonConstants.EMPTY_STRING, env);
		Object[] field = new Object[] { smartPursePoolAccountName };
		IBOAttributeCollectionFeature accountItem = null;
		// check for closed or stopped accounts
		accountItem = (IBOAttributeCollectionFeature) BankFusionThreadLocal
				.getPersistanceFactory().findByPrimaryKey(
						IBOAttributeCollectionFeature.BONAME,
						smartPursePoolAccountNumber, true);
		destAccount = smartPursePoolAccountNumber;
		if (accountItem == null) {
			if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {

				atmHelper.populateErrorDetails(
						ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
						ATMConstants.WARNING,
						ChannelsEventCodes.E_INVALID_SMARTPURSEPOOL_ACCOUNT,
						BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field,
						env);
				logWarn(BankFusionMessages.getFormattedMessage(
						ChannelsEventCodes.E_INVALID_SMARTPURSEPOOL_ACCOUNT,
						field));

			} else if (atmLocalMessage.getForcePost().equals(
					ATMConstants.FORCEPOST_1) ||(atmLocalMessage.getForcePost().equals(
							ATMConstants.FORCEPOST_3)) ) {

				atmHelper
						.populateErrorDetails(
								ATMConstants.AUTHORIZED_MESSAGE_FLAG,
								ATMConstants.WARNING,
								ChannelsEventCodes.E_INVALID_SMARTPURSEPOOL_ACCT_SUS_ACCT_UPDATED,
								BankFusionMessages.ERROR_LEVEL,
								atmLocalMessage, field, env);
				logWarn(BankFusionMessages
						.getFormattedMessage(
								ChannelsEventCodes.E_INVALID_SMARTPURSEPOOL_ACCT_SUS_ACCT_UPDATED,
								field));

				if (controlDetails != null) {
					String pseudoName = getDestinationSuspenseAccount();
					destAccount = atmHelper.getAccountIDfromPseudoName(
							pseudoName, atmLocalMessage
									.getCurrencyDestDispensed(),
							CommonConstants.EMPTY_STRING, env);
					return;
				}
			}
		} else
		// check for closed accounts
		if (accountItem != null) {
			BusinessValidatorBean validatorBean = new BusinessValidatorBean();
			if (validatorBean.validateAccountClosed(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_0)) {

					atmHelper.populateErrorDetails(
							ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
							ATMConstants.WARNING,
							CommonsEventCodes.E_ACCOUNT_CLOSED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
							field, env);
					logWarn(BankFusionMessages.getFormattedMessage(40580103, field));

				} else if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_1)) {

					atmHelper
							.populateErrorDetails(
									ATMConstants.AUTHORIZED_MESSAGE_FLAG,
									ATMConstants.CRITICAL,
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									BankFusionMessages.MESSAGE_LEVEL,
									atmLocalMessage, field, env);
					logError(BankFusionMessages
							.getFormattedMessage(
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									field));

					if (controlDetails != null) {
						String pseudoName = getDestinationSuspenseAccount();
						destAccount = atmHelper.getAccountIDfromPseudoName(
								pseudoName, atmLocalMessage
										.getCurrencyDestDispensed(),
								CommonConstants.EMPTY_STRING, env);
						return;
					}
				}
			}
			// check for stopped accounts
			else if (validatorBean.validateAccountStopped(accountItem, env)) {
				if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_0)) {

					atmHelper.populateErrorDetails(
							ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
							ATMConstants.WARNING,
							CommonsEventCodes.E_ACCOUNT_STOPPED,
							BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
							field, env);
					logWarn(BankFusionMessages.getFormattedMessage(70009706, field));

				} else if (atmLocalMessage.getForcePost().equals(
						ATMConstants.FORCEPOST_1)) {

					atmHelper
							.populateErrorDetails(
									ATMConstants.AUTHORIZED_MESSAGE_FLAG,
									ATMConstants.CRITICAL,
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									BankFusionMessages.MESSAGE_LEVEL,
									atmLocalMessage, field, env);
					logError(BankFusionMessages
							.getFormattedMessage(
									ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED,
									field));

					if (controlDetails != null) {
						String pseudoName = getDestinationSuspenseAccount();
						destAccount = atmHelper.getAccountIDfromPseudoName(
								pseudoName, atmLocalMessage
										.getCurrencyDestDispensed(),
								CommonConstants.EMPTY_STRING, env);
						return;
					}
				}
			}
		}
	}
	//Added for artf39481 starts.
    protected String getDefaultTransactionType() {
    	return controlDetails.getSmartCardDefaultTransactionType();
    }
  //Added for artf39481 ends.

    //Extended the method for narratives starts.(artf43792)
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
	map.put("ACCOUNT2_NARRATIVE", contraTransactionNarration);
	map.put("ACCOUNT2_POSTINGACTION", "C");
	map.put("ACCOUNT2_TRANSCODE", transactionCode);
	map.put("BASEEQUIVALENT", message.getAmount3());
	map.put("TRANSACTIONREFERENCE", atmHelper.getTransactionReference(message));
	map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
	map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
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
				message.setErrorCode(ATMConstants.WARNING);
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
		logger.info(exception.getLocalisedMessage());
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
  //Extended the method for narratives ends.(artf43792)
}
