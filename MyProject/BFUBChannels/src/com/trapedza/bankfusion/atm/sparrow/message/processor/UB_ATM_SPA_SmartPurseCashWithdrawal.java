/*
 * Copyright (c) 2009 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 * Revision 1.0  2009/04/13 biswajit sinha
 * Smart Card-Sprint4-New Code
 *
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardIssuersDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * The class UB_ATM_SPA_SmartPurseCashWithdrawal will handle the messages which are cash withdrawals
 * from the Smart Card Purse via an ATM. Class UB_ATM_SPA_SmartPurseCashWithdrawal will extend
 * LocalCashWithdrawal and override the validateCardHoldersAccount,getCashAccount,
 * getSettlementAccount methods for posting the Cash withdrawal from Smart purse account transaction
 * (message type 581).
 * 
 */
public class UB_ATM_SPA_SmartPurseCashWithdrawal extends LocalCashWithdrawal {

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
    protected static final transient Log logger = LogFactory.getLog(ATMFundsTransfer.class.getName());
    /**
     * Holds the Branch Code for the Settlement Account
     */
    protected String branchCode;

    /**
     * Holds the Main Account, valid Card Holder's Account or the Suspense Account in case the Card
     * Holder's Account is not valid/stopped/dormant/closed. This will be used to debit UB Charges
     * for the transaction (if any).
     */
    protected String mainAccount;
    /**
     * This is an internal account for 581 messages.Ammount1(Amount2+Commission) will be debited
     * from this account.
     */
    protected String pursePoolAccount;

    protected IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    /**
     * Where clause for atmSource selection
     */
    private static final String atmSourceWhereClause = "WHERE " + IBOATMCardIssuersDetail.ISOCOUNTRYCODE + "=?" + "AND "
            + IBOATMCardIssuersDetail.IMDCODE + "=?";

    public UB_ATM_SPA_SmartPurseCashWithdrawal() {
        super();
    }

    /**
     * This method validates the Source Account in the ATM Sparrow message.
     * 
     * @param atmLocalMessage
     *            it stores the ATM Sparrow Local Messages.
     * @param env
     *            holds the Session variables.
     * @override validateCardHoldersAccount().
     */

    protected void validateCardHoldersAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        // calling the method with the appropriate name, as per the
        // functionality

        // Validate Card Holder's Account (Main Account)
        validateMainAccount(atmLocalMessage, env);

        // Validate Purse Pool Account (Source Account)
        validatePursePoolAccount(atmLocalMessage, env);
    }

    /**
     * This method validates the local message details.
     * 
     * @param atmSparrowMessage
     *            it is the ATM Sparrow Local Messages.
     * @param env
     *            holds the Session variables.
     */
    protected void validateCashWithdrawalDetails(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

        atmMessageValidator.validateMessage(atmSparrowMessage, env, ATMMessageValidator.LOCAL_MESSGE_TYPE);
        if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }

        transactionCode = atmHelper.getBankTransactionCode(
                atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType(), env);
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
                transactionCode = controlDetails.getSmartCardDefaultTransactionType(); // Updated
                                                                                       // for
                                                                                       // artf39481.
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
     * This method validate Stop,Closed ,Empty,SmartCard for CardHolder's Account
     * 
     * @param atmLocalMessage
     * @param env
     */
    protected void validateMainAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        Object[] field = new Object[] { atmLocalMessage.getAccount() };
        IBOAttributeCollectionFeature accountItem = null;
        IBOAccount messageAccount = null; // Added for Invalid CardHolder's Account.
        // check for closed or stopped accounts
        accountItem = (IBOAttributeCollectionFeature) factory.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
                atmLocalMessage.getAccount(), true);
        mainAccount = atmLocalMessage.getAccount();
        // validation for Invalid Cardholder's Account that is not present in Database starts.
        messageAccount = (IBOAccount) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOAccount.BONAME,
                mainAccount, true);
        if (messageAccount == null) {
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field,
                        env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid CardHolders Account" + atmLocalMessage.getAccount());
                }
            }
            // added for artf44997 [start]
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {

                Object[] fieldForInvalidAcc = new Object[] { atmLocalMessage.getAccount() };

                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_INVALID_CARD_HOLDR_ACC_TRANS_POSTED, BankFusionMessages.MESSAGE_LEVEL,
                        atmLocalMessage, fieldForInvalidAcc, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid card holders account" + atmLocalMessage.getAccount() + "Transaction will be posted.");
                }
                if (controlDetails != null) {
                    String psesudoName = getSuspenseAccountForMainAccount();
                    mainAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                            CommonConstants.EMPTY_STRING, env);

                }
            }
            // added for artf44997 [end]
            if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                return;
            }
        }

        // validation for Invalid Cardholder's Account that is not present in Database ends.
        if (accountItem == null) {
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid account" + atmLocalMessage.getAccount());
                }
                return;
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.W_INVALID_CARD_HOLDR_ACC_TRANS_POSTED, BankFusionMessages.MESSAGE_LEVEL,
                        atmLocalMessage, field, env);
                String psesudoName = getSuspenseAccountForMainAccount();
                mainAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                        CommonConstants.EMPTY_STRING, env);

                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid card holders account" + atmLocalMessage.getAccount() + "Transaction will be posted.");
                }
                return;

            }
        }
        // updation as per the latest Use case for account closed starts.
        if (accountItem != null) {
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_ACC_CLOSED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Account is closed" + atmLocalMessage.getAccount());
                    }
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.W_INVALID_CARD_HOLDR_ACC_TRANS_POSTED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid card holders account" + atmLocalMessage.getAccount() + "Transaction will be posted.");
                    }

                }
            }
            // updation as per the latest Use case for account closed ends.
            else if (validatorBean.validateAccountStopped(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_INVALID_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid account" + atmLocalMessage.getAccount());
                    }
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.W_INVALID_CARD_HOLDR_ACC_TRANS_POSTED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid card holders account" + atmLocalMessage.getAccount() + "Transaction will be posted.");
                    }
                    if (controlDetails != null) {
                        String psesudoName = getSuspenseAccountForMainAccount();
                        mainAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                                CommonConstants.EMPTY_STRING, env);
                        return;
                    }
                }

            }
        }
        // Check ATMCardAccMap to see whether the card no and the account are
        // mapped.
        if (!atmMessageValidator.isAccountMappedToCard(atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount(), env)) {
            field = new Object[] { atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED, BankFusionMessages.ERROR_LEVEL,
                        atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.error(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                            ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED, new Object[] { field }));
                }
            }
            // added for artf44997 [start]
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {

                Object[] fieldForInvalidAcc = new Object[] { atmLocalMessage.getAccount() };

                /*
                 * atmHelper.populateErrorDetails( ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.WARNING, ChannelsEventCodes.W_INVALID_CARD_HOLDR_ACC_TRANS_POSTED,
                 * BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, fieldForInvalidAcc, env); if
                 * (controlDetails != null) { String psesudoName =
                 * getSuspenseAccountForMainAccount(); mainAccount =
                 * atmHelper.getAccountIDfromPseudoName( psesudoName, atmLocalMessage
                 * .getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
                 */
                // For Fix artf792517
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC, BankFusionMessages.ERROR_LEVEL,
                        atmLocalMessage, field, env);
                if (controlDetails != null) {
                    String psesudoName = getSuspenseAccountForMainAccount();
                    cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
                    // For Fix artf792517
                }

                if (logger.isDebugEnabled()) {
                    logger.error("Invalid card holders account" + atmLocalMessage + "Transaction will be posted.");
                }
                return;

            }
            // added for artf44997 [end]
        }
        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Checking for Password protection Flag.
        // added for artf44710 [start]
        boolean result = atmHelper.isAccountValid(atmLocalMessage, PasswordProtectedConstants.OPERATION_DEBIT, env);
        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        if (!result) {
            String psesudoName = getSuspenseAccountForMainAccount();
            cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                    CommonConstants.EMPTY_STRING, env);
            return;
        }
        boolean passwordProtected = atmHelper.isAccountPasswordProtected(atmLocalMessage,
                PasswordProtectedConstants.OPERATION_DEBIT, ATMConstants.SOURCEACCOUNTTYPE, env);
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
                String psesudoName = getSuspenseAccountForMainAccount();
                cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                        CommonConstants.EMPTY_STRING, env);
            }
            return;
        }
        // added for artf44710 [end]
    }

    /**
     * This is the method which derives the pseudoname for the Main Account. Override this method in
     * case you want to use a different pseudoname for Main account
     * 
     * @return pseudoname
     * @return
     */
    protected String getSuspenseAccountForMainAccount() {
        return controlDetails.getSmartCardDebitSuspenseAccount();
    }

    /**
     * This method validates the Purse Pool Account in the ATM Sparrow message.
     * 
     * @param atmLocalMessage
     *            it stores the ATM Sparrow Local Messages.
     * @param env
     *            holds the Session variables.
     */

    protected void validatePursePoolAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        String psName = null;
        IBOAttributeCollectionFeature accountItem = null;
        psName = controlDetails.getSmartCardPursePoolAccount();
        pursePoolAccount = atmHelper.getAccountIDfromPseudoName(psName, atmLocalMessage.getCurrencySourceAccount(),
                CommonConstants.EMPTY_STRING, env);
        if (pursePoolAccount != null && pursePoolAccount != CommonConstants.EMPTY_STRING) {
            accountItem = (IBOAttributeCollectionFeature) (factory.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
                    pursePoolAccount, true));

        }

        if ((pursePoolAccount == null) || (pursePoolAccount.equals(CommonConstants.EMPTY_STRING))) {
            Object[] field = new Object[] { psName };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_SMARTPURSEPOOL_ACCOUNT, // Changed for correct
                                                                             // error message.
                        BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid SmartPursePool Account" + psName);
                }
                return;
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_INVALID_PURSE_POOL_ACC_SUSP_ACC_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                        atmLocalMessage, field, env);
                String psesudoName = getSourceAccountSuspense();
                pursePoolAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                        CommonConstants.EMPTY_STRING, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid Purse account,Suspense account will be updated.");
                }
                return;
            }
        }
        // check for closed or stopped accounts
        if (accountItem != null) {
            Object[] field = new Object[] { accountItem };
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_INVALID_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid account" + accountItem);
                    }
                    return;
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    String psesudoName = getSourceAccountSuspense();
                    pursePoolAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
                }
                logger.error("Account is closed" + accountItem);
            }
            else if (validatorBean.validateAccountStopped(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_INVALID_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid account" + accountItem);
                    }
                    return;
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    String psesudoName = getSourceAccountSuspense();
                    pursePoolAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
                }
                logger.error(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                        ChannelsEventCodes.E_ACCOUNT_STOPPED, new Object[] { field }));
            }

        }
        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Checking for Password protection Flag.

        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // cardHoldersAccount = pursePoolAccount;

    }

    /**
     * This is the method which derives the pseudoname for the Purse Pool Account. Override this
     * method in case you want to use a different pseudoname for Purse Pool account
     * 
     * @return pseudoname
     */
    protected String getSourceAccountSuspense() {
        return controlDetails.getSmartCardDebitSuspenseAccount();
    }

    /**
     * This method fetches the settlement account based on Source Country + IMD or network ID in the
     * message for posting.
     * 
     * @param atmLocalMessage
     *            it stores the ATM Sparrow Local Messages.
     * @param env
     *            holds the Session variables.
     * @override getSettlementAccount()
     */

    protected void getSettlementAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        // System fetches settlement account based on Source Country + IMD or
        // network ID in the message
        branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
        ArrayList<String> params = new ArrayList<String>();
        params.add(atmLocalMessage.getSourceCountryCode());
        params.add(atmLocalMessage.getSourceIMD());
        IBOATMCardIssuersDetail cardIssuersSettlementAccount = null;
        cardIssuersSettlementAccount = (IBOATMCardIssuersDetail) factory.findFirstByQuery(IBOATMCardIssuersDetail.BONAME,
                atmSourceWhereClause, params, true);
        if (cardIssuersSettlementAccount != null) {
            validateSettlementAccount(cardIssuersSettlementAccount, atmLocalMessage, env);
        }
        else {
            Object[] field = new Object[] { atmLocalMessage.getSourceCountryCode(), atmLocalMessage.getSourceIMD() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 7520,
                        BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                return;
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                String psesudoName = getPseudoNameForSettlementAccount();
                settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(),
                        branchCode, env);
            }
            atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                    ChannelsEventCodes.W_INVALID_SETTL_ACC_SUSP_ACC_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage,
                    field, env);
        }
    }

    /**
     * This is the method which derives the pseudoname for the settlement Account. Override this
     * method in case you want to use a different pseudoname for settlement account
     * 
     * @return pseudoname
     */
    protected String getPseudoNameForSettlementAccount() {
        return controlDetails.getSmartCardCreditSuspenseAccount();
    }

    /**
     * This method validate Stop,Closed and Empty for Settlement Account
     * 
     * @param cardIssuersSettlementAccount
     * @param atmLocalMessage
     * @param env
     */
    protected void validateSettlementAccount(IBOATMCardIssuersDetail cardIssuersSettlementAccount, ATMLocalMessage atmLocalMessage,
            BankFusionEnvironment env) {

        settlementAccount = atmHelper.getAccountIDfromPseudoName(cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT(),
                atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
        if (settlementAccount.equals(CommonConstants.EMPTY_STRING)) {
            Object[] field = new Object[] { cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, // changed for event code.
                        BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Error: 404-07520 Invalid settlement account"
                            + cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT());
                }

                return;
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {

                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_INVALID_SETTL_ACC_SUSP_ACC_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage,
                        field, env);
                String psesudoName = getPseudoNameForSettlementAccount();
                settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(),
                        branchCode, env);
                if (logger.isDebugEnabled()) { // Updated for artf39771.Put debugger and changed
                                               // getformattedMessage method.
                    logger.debug("Invalid settlement account" + cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT()
                            + "Suspense account will be updated.");
                }
            }
        }
        else {
            Object[] field = new Object[] { settlementAccount };
            IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) factory.findByPrimaryKey(
                    IBOAttributeCollectionFeature.BONAME, settlementAccount, true);
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_SETTLEMENT_ACCOUNT_CLOSED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field,
                            env);
                    logger.error("Settlement Account is Closed" + settlementAccount);
                    return;
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.W_SETTL_ACC_CLOSED_SUSP_ACC_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    String psesudoName = getPseudoNameForSettlementAccount();
                    settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                    logger.error("Settlement Account is Closed" + settlementAccount + "Suspense Account will be updated.");
                }
            }
            else if (validatorBean.validateAccountStopped(accountItem, env)) {

                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_SETTLEMENT_ACCOUNT_STOPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field,
                            env);
                    logger.error("Settlement Account is stopped" + settlementAccount);

                    return;
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.W_SETTL_ACC_STOPPED_SUSP_ACC_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                            atmLocalMessage, field, env);
                    String psesudoName = getPseudoNameForSettlementAccount();
                    settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                    logger.error("Settlement Account is Stopped" + settlementAccount + "Suspense Account will be updated");
                }
            }
        }
    }

    /**
     * This method fetches the cash account to be used in posting.
     * 
     * @param atmLocalMessage
     *            it stores the ATM Sparrow Local Messages.
     * @param env
     *            holds the Session variables.
     * @override getCashAccount()
     */
    protected void getCashAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

        // System fetches cash account based on ATM device number for dispensed
        // currency
        String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
        logger.info(branchCode);
        ArrayList<String> params = new ArrayList<String>();
        params.add(atmLocalMessage.getDeviceId());
        IBOATMSettlementAccount atmSettlementAccount = null;

        atmSettlementAccount = (IBOATMSettlementAccount) factory.findFirstByQuery(IBOATMSettlementAccount.BONAME,
                atmSettlementAccountWhereClause, params, true);

        if (atmSettlementAccount != null) {
            validateCashAccount(atmSettlementAccount, atmLocalMessage, env);
        }
        else {
            Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_CASH_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                logger.error("Invalid cash account" + atmLocalMessage.getDeviceId());
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_INVALID_CASH_ACC_SUSP_ACC_UPDATED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                        field, env);
                String psesudoName = getPseudoNameForCashAccount();
                cashAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(),
                        branchCode, env);
                logger.error("Invalid cash account" + atmLocalMessage.getDeviceId() + "Suspense account ll be updated.");
            }
        }
    }

    /**
     * This method validate Stop,Closed and Empty for Cash Account
     * 
     * @param atmSettlementAccount
     * @param atmLocalMessage
     * @param env
     */
    protected void validateCashAccount(IBOATMSettlementAccount atmSettlementAccount, ATMLocalMessage atmLocalMessage,
            BankFusionEnvironment env) {

        cashAccount = atmHelper.getAccountIDfromPseudoName(atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT(),
                atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
        if (cashAccount.equals(CommonConstants.EMPTY_STRING)) {
            Object[] field = new Object[] { atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_CASH_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid cash account" + atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT());
                }
                return;
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                String psesudoName = getPseudoNameForCashAccount();
                cashAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(),
                        branchCode, env);
                atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_INVALID_CASH_ACC_SUSP_ACC_UPDATED, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage,
                        field, env);
                if (logger.isDebugEnabled()) { // Updated for artf40365.Put debugger.
                    logger.error("Invalid cash account" + atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT()
                            + "Suspense account ll be updated.");
                }
            }
        }
        else {
            Object[] field = new Object[] { cashAccount };
            IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) factory.findByPrimaryKey(
                    IBOAttributeCollectionFeature.BONAME, cashAccount, true);
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_CASH_ACCOUNT_CLOSED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    logger.error("Cash Account is Closed" + cashAccount);
                    return;

                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_CASH_ACCOUNT_CLOSED_SUSPENSE_ACCOUNT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                            atmLocalMessage, field, env);
                    String psesudoName = getPseudoNameForCashAccount();
                    cashAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(),
                            branchCode, env);
                    logger.error("Cash Account is Closed" + cashAccount + "Suspense account will be updated");
                }

            }
            else if (validatorBean.validateAccountStopped(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    atmHelper.populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_CASH_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                    logger.error("Cash Account is stopped" + cashAccount);
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    atmHelper.populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_CASH_ACCOUNT_STOPPED_SUSPENSE_ACCOUNT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                            atmLocalMessage, field, env);
                    String psesudoName = getPseudoNameForCashAccount();
                    cashAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencyDestDispensed(),
                            branchCode, env);
                    logger.error("Cash Account is stopped" + cashAccount + "Suspense account will be updated.");
                }
            }

        }

    }

    /**
     * This method is overridden as the postTransactions method in LocalCashWithdrawal would pick up
     * the Card Holder's Account as the main account. It did not handle the scenario where the Card
     * Holder's Account could be invalid (stopped/dormant/closed), in that case it should pick up
     * the Suspense Account instead.
     */
    protected void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {

        HashMap<String, Object> map = new HashMap<String, Object>();
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
        map.put("TRANSACTIONSOURCE", "Local");
        map.put("ACCOUNT1_ACCOUNTID", pursePoolAccount);
        map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
        map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
        map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
        map.put("ACCOUNT1_POSTINGACTION", "D");
        map.put("ACCOUNT1_TRANSCODE", transactionCode);
        map.put("AMOUNT4", message.getAmount4().abs());
        // This is changed for the scenario where the Card Holder's Account
        // would be invalid (stopped/dormant/closed), so pick up the Suspense
        // Account instead.
        map.put("MAINACCOUNTID", message.getAccount());
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
        map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
        map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
        map.put("MESSAGENUMBER", message.getMessageType() + message.getTransactionType());
        if (ATMConstants.FORCEPOST_0.equals(message.getForcePost()) || ATMConstants.FORCEPOST_6.equals(message.getForcePost())) {
            map.put("FORCEPOST", new Boolean(false));
        }
        else {
            map.put("FORCEPOST", new Boolean(true));
        }

        // Post the Transactions.
        try {
            HashMap<String, Object> outputParams = MFExecuter.executeMF(ATMConstants.FINANCIAL_POSTING_MICROFLOW_NAME, env, map);

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
                    factory.rollbackTransaction();
                    factory.beginTransaction();
                }
                catch (Exception ignored) {
                }
                return;
            }
            factory.commitTransaction();
            factory.beginTransaction();
        }
        catch (BankFusionException exception) {
            logger.info("Transaction is Not Authorized: --> " + exception.getMessage());
            message.setErrorCode(ATMConstants.ERROR);
            message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            message.setErrorDescription(exception.getMessage());
            try {
                factory.rollbackTransaction();
                factory.beginTransaction();

            }
            catch (Exception ignored) {

            }
        }
        finally {
            try {
                factory.beginTransaction();
            }
            catch (Exception ignored) {

            }
        }
    }

    /**
     * This is the method which derives the pseudoname for the Cash Account. Override this method in
     * case you want to use a different pseudoname for Cash account
     * 
     * @return
     */
    protected String getPseudoNameForCashAccount() {
        return controlDetails.getSmartCardCreditSuspenseAccount();
    }

}
