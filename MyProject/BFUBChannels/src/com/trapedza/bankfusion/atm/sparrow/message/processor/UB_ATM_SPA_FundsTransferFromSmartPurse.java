/* ********************************************************************************
 *  Copyright (c) 2009 MISYS Financial Systems Limited. All Rights Reserved.
 *
 *  This software is the proprietary information of MISYS Financial Systems Limited.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * * Revision 1.1  2009/04/14 debjitb
 * *** empty log message ***
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
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
 * The UB_ATM_SPA_FundsTransferFromSmartPurse class processes the message for Smart Card Fund
 * Transfer from Smart Purse Pool Account to Card Holder's Account(Messages - 589). This class
 * extends the ATMFundsTransfer class for common message validations, performs validations that are
 * specific to the ATMFund Transfer(Messages - 540). The transactions are then posted using the
 * postingEngine.
 */

public class UB_ATM_SPA_FundsTransferFromSmartPurse extends ATMFundsTransfer {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    protected Object mainAccount;

    public UB_ATM_SPA_FundsTransferFromSmartPurse() {
        super();
    }

    /**
     */
    /**
     * Holds the reference for logger object
     */
    private static final transient Log logger = LogFactory.getLog(UB_ATM_SPA_FundsTransferFromSmartPurse.class.getName());

    /**
     * Validation for Smart Purse Pool Account(Internal Account).
     * 
     * @param atmLocalMessage
     *            it stores the ATM Sparrow Local Messages.
     * @param env
     *            holds the Session variables.
     */
    @Override
    protected void validateCardHoldersAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        validatePursePoolAccount(atmLocalMessage, env);
    }

    /**
     * Validation for Card Holder's Account(In 589 message destination account is Card Holder's
     * Account).
     * 
     * @param atmLocalMessage
     *            it stores the ATM Sparrow Local Messages.
     * @param env
     *            it holds the Session variables.
     */
    @Override
    protected void validateDestAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        String tempDestAccountNumber = atmLocalMessage.getAccount();
        Object[] field = new Object[] { tempDestAccountNumber };
        // It hold's reference of all the attribute collection features.
        IBOAttributeCollectionFeature accountItem = null;
        IBOAccount messageAccount = null; // Added for Invalid CardHolder's Account.
        // Validation for Account existence.
        accountItem = (IBOAttributeCollectionFeature) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
                IBOAttributeCollectionFeature.BONAME, tempDestAccountNumber, true);
        destAccount = tempDestAccountNumber;
        // validation for Invalid Cardholder's Account that is not present in Database starts.
        messageAccount = (IBOAccount) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOAccount.BONAME,
                tempDestAccountNumber, true);
        if (messageAccount == null) {
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field,
                        env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid CardHolders Account" + tempDestAccountNumber);
                }
            }
            if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                return;
            }
        }

        // validation for Invalid Cardholder's Account that is not present in Database ends.
        if (accountItem == null) {
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field,
                        env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid CardHolders Account" + tempDestAccountNumber);
                }
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                        atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid CardHolders Account" + tempDestAccountNumber + "Suspense Account will be Updated");
                }
                if (controlDetails != null) {
                    String pseudoName = getDestSuspenseAccount();
                    destAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmLocalMessage.getCurrencySourceAccount(),
                            CommonConstants.EMPTY_STRING, env);
                    return;
                }
            }
        }
        else {
            // Changes for account closed starts.
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            CommonsEventCodes.E_ACCOUNT_CLOSED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The Account:" + tempDestAccountNumber + "is closed");
                    }
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Account" + tempDestAccountNumber
                                + "is Either Stopped or Closed, Suspense Account will be updated");
                    }
                    if (controlDetails != null) {
                        String pseudoName = getDestSuspenseAccount();
                        destAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmLocalMessage.getCurrencySourceAccount(),
                                CommonConstants.EMPTY_STRING, env);
                        return;
                    }
                }
            }
            // Changes for account closed ends.

            // Validation for AccountStopped starts.
            else if (validatorBean.validateAccountStopped(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The Account:" + tempDestAccountNumber + "is stopped");
                    }
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Account" + tempDestAccountNumber
                                + "is Either Stopped or Closed, Suspense Account will be updated");
                    }
                    if (controlDetails != null) {
                        String pseudoName = getDestSuspenseAccount();
                        destAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmLocalMessage.getCurrencySourceAccount(),
                                CommonConstants.EMPTY_STRING, env);
                        return;
                    }
                }
            }
        }// Validation for AccountStopped ends.
        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Check ATMCardAccMap to see whether the card no and the account are
        // mapped.
        if (!atmMessageValidator.isAccountMappedToCard(atmLocalMessage.getCardNumber(), tempDestAccountNumber, env)) {
            field = new Object[] { atmLocalMessage.getCardNumber(), tempDestAccountNumber };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED, BankFusionMessages.ERROR_LEVEL,
                        atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.error(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                            ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED, field));
                }
            }
            // added for artf45104 [start]
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {

                Object[] fieldForInvalidAcc = new Object[] { atmLocalMessage.getAccount() };
                /*
                 * atmHelper .populateErrorDetails( ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.CRITICAL,
                 * ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED,
                 * BankFusionMessages.ERROR_LEVEL, atmLocalMessage, fieldForInvalidAcc, env);
                 */
                // For Fix artf792517
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC, BankFusionMessages.ERROR_LEVEL,
                        atmLocalMessage, field, env);
                if (controlDetails != null) {
                    String psesudoName = getSourceSuspenseAccount();
                    cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
                }
                // For Fix artf792517(ended)

                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid CardHolders Account" + atmLocalMessage.getAccount() + "Suspense Account will be Updated");
                }

                if (controlDetails != null) {
                    String pseudoName = getDestSuspenseAccount();
                    destAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmLocalMessage.getCurrencySourceAccount(),
                            CommonConstants.EMPTY_STRING, env);
                    return;
                }

            }
            // added for artf45104 [end]
        }
        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Checking for Password protection Flag.
        // added for artf44710 [start]
        boolean result = atmHelper.isAccountValid(atmLocalMessage, PasswordProtectedConstants.OPERATION_CREDIT, env);
        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        if (!result) {
            String psesudoName = getDestSuspenseAccount();
            destAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                    CommonConstants.EMPTY_STRING, env);
            return;
        }
        boolean passwordProtected = atmHelper.isAccountPasswordProtected(atmLocalMessage,
                PasswordProtectedConstants.OPERATION_CREDIT, ATMConstants.SOURCEACCOUNTTYPE, env);
        if (passwordProtected) {
            return;
        }
        else {
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR,
                        ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                        field, env);
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                        atmLocalMessage, field, env);
                String psesudoName = getDestSuspenseAccount();
                destAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                        CommonConstants.EMPTY_STRING, env);
            }
            return;
        }
        // added for artf44710 [end]
    }

    /**
     * Method Description: This method gets the Pseudonym for the Source Suspense Account. Override
     * this method in case you want to use a different pseudonym.
     * 
     * @return
     */
    protected String getDestSuspenseAccount() {
        return controlDetails.getSmartCardCreditSuspenseAccount();
    }

    /**
     * Method Description: This method gets the Pseudonym for the Destination Suspense Account.
     * Override this method in case you want to use a different pseudonym.
     * 
     * @return
     */
    protected String getSourceSuspenseAccount() {
        return controlDetails.getSmartCardDebitSuspenseAccount();
    }

    // Validation for Smart Purse Pool Account(In 589 message source account
    // is Smart Purse Pool account).
    private void validatePursePoolAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        ATMControlDetails moduleDetails = new ATMControlDetails(env);
        String smartPursePoolAccountName = moduleDetails.getSmartCardPursePoolAccount();
        String smartPursePoolAccountNumber = atmHelper.getAccountIDfromPseudoName(smartPursePoolAccountName,
                atmLocalMessage.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
        Object[] field = new Object[] { smartPursePoolAccountNumber };
        IBOAttributeCollectionFeature accountItem = null;
        accountItem = (IBOAttributeCollectionFeature) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
                IBOAttributeCollectionFeature.BONAME, smartPursePoolAccountNumber, true);
        cardHoldersAccount = smartPursePoolAccountNumber;
        // Check for Account existence.
        if (accountItem == null) {
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_SMARTPURSEPOOL_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                        field, env);
                if (logger.isDebugEnabled()) {
                    logger.error("Invalid SmartPursePool Account" + smartPursePoolAccountNumber);
                }
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_SMARTPURSEPOOL_ACCT_SUS_ACCT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                        atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid SmartPursePool Account" + smartPursePoolAccountNumber
                            + "Suspense Account will be Updated");
                }
                if (controlDetails != null) {
                    String psesudoName = getSourceSuspenseAccount();
                    cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
                    return;
                }
            }
        }
        else {
            // Validation for Smart Purse Pool Account stopped/Closed starts.
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            CommonsEventCodes.E_ACCOUNT_CLOSED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The Account:" + smartPursePoolAccountNumber + "is closed");
                    }
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                            atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Account" + smartPursePoolAccountNumber
                                + "is Either Stopped or Closed, Suspense Account will be updated");
                    }
                    if (controlDetails != null) {
                        String psesudoName = getSourceSuspenseAccount();
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmLocalMessage.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
                        return;
                    }
                }
            }
            else if (validatorBean.validateAccountStopped(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The Account:" + smartPursePoolAccountNumber + "is stopped");
                    }
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                            atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Account" + smartPursePoolAccountNumber
                                + "is Either Stopped or Closed, Suspense Account will be updated");
                    }
                    if (controlDetails != null) {
                        String psesudoName = getSourceSuspenseAccount();
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmLocalMessage.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
                        return;
                    }
                }
            }
        }
    }

    // Added for artf39481 starts.
    protected String getDefaultTransactionType() {
        return controlDetails.getSmartCardDefaultTransactionType();
    }

    // Added for artf39481 ends.
    // Extended the method for narratives starts.(artf43792)
    protected void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
        HashMap map = new HashMap();
        String accountCurrencyCode = CommonConstants.EMPTY_STRING;
        String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;

        try {
            dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
                    message.getCurrencyDestDispensed(), true);
            accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencySourceAccount(),
                    true);
        }
        catch (BankFusionException exception) {
        }

        map.put("ACCOUNT1_ACCOUNTID", cardHoldersAccount);
        map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
        map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
        map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
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
        map.put("AMOUNT4", message.getAmount4().abs());
        map.put("MAINACCOUNTID", message.getAccount());
        map.put("MESSAGENUMBER", message.getMessageType() + message.getTransactionType()); // Added
                                                                                           // for
                                                                                           // SmartCard
                                                                                           // development.
        if (ATMConstants.FORCEPOST_0.equals(message.getForcePost()) || ATMConstants.FORCEPOST_6.equals(message.getForcePost())) {
            map.put("FORCEPOST", new Boolean(false));
        }
        else {
            map.put("FORCEPOST", new Boolean(true));
        }

        // Post the Transactions.
        try {
            HashMap outputParams = MFExecuter.executeMF(ATMConstants.FINANCIAL_POSTING_MICROFLOW_NAME, env, map);
            String authorizedFlag = outputParams.get("AUTHORIZEDFLAG").toString();
            // changes for bug 24369 starts
            if (authorizedFlag.equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
                String errorMessage = outputParams.get("ERRORMESSAGE").toString();
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                if (!errorMessage.equals("")) {
                    message.setErrorCode(ATMConstants.WARNING);
                    message.setErrorDescription(errorMessage);
                }
                logger.error(errorMessage);
            }
            // changes for bug 24369 ends
            if (authorizedFlag.equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
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
    // Extended the method for narratives ends.(artf43792)
}
