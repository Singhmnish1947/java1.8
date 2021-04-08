/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ExtNwCashWithdrawal.java,v $
 * Revision 1.14  2008/11/23 10:58:50  sukirtim
 * Deleivery of Sprint7
 *
 * Revision 1.2  2008/11/01 14:33:03  bhavyag
 * updated during the fix of bug13963.
 *
 * Revision 1.1  2008/10/10 05:50:58  debjitb
 * updated version after added Amount4
 *
 * Revision 1.13  2008/08/12 20:15:06  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.11.4.3  2008/07/29 01:30:07  prashantk
 * Status Of Microflow is being returned instead of its relying on the Posting Microflow to throw an error. This if for Bug # 11450.
 *
 * Revision 1.11.4.2  2008/07/03 17:55:28  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
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
 * Revision 1.11.4.1  2008/05/17 17:20:01  thrivikramj
 * Changes done for Pseudonym Implementation
 *
 * Revision 1.1  2008/05/08 22:29:41  arjuny
 * Implemented Psedoname
 *
 * Revision 1.11  2008/02/08 15:20:07  sushmax
 * Refactoring The Processors + Check Ins after Sprint Cycle 5 & 6.
 *
 * Revision 1.17  2008/02/04 12:48:08  prashantk
 * Bug Fixes
 *
 * Revision 1.14  2008/01/24 14:20:56  sushmax
 * Corrections done as part of issue fixing
 *
 * Revision 1.13  2008/01/23 09:57:00  varap
 * *** empty log message ***
 *
 * Revision 1.12  2008/01/22 09:49:16  sushmax
 * corrected password protection flag
 *
 * Revision 1.7  2008/01/21 12:24:08  prashantk
 * Updated for Password Protection Check.
 *
 * Revision 1.6  2008/01/21 11:47:18  sushmax
 * Updated files
 *
 * Revision 1.9  2008/01/18 07:21:18  sushmax
 * Updated files
 *
 * Revision 1.3  2008/01/16 14:28:25  sushmax
 * Corrections done as part of issue fixing
 *
 * Revision 1.2  2008/01/10 14:25:07  prashantk
 * Updations for Incorporating Module Config. Changes for ATM
 *
 * Revision 1.8  2007/12/07 13:46:29  prashantk
 * Code Clean UP:- Removed all Warning Messages
 *
 * Revision 1.7  2007/11/30 09:50:55  sushmax
 * calls to ATMCache methods changed to call ATMHelper methods
 *
 * Revision 1.6  2007/11/29 08:18:29  sushmax
 * Changed code postingMessage.FORCEPOST set  for FORCEPOST flag
 *
 * Revision 1.5  2007/11/28 09:45:17  sushmax
 * Code modified for calling Posting Engine.
 *
 * Revision 1.4  2007/11/27 11:46:41  sushmax
 * Corrected code - checked for boolean value of proceed before posting
 *
 * Revision 1.3  2007/11/26 05:38:08  sushmax
 * Corrrected code to include 'D' and 'C' while posting
 *
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMExNwMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOExternalLoroSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * This class provides calls the ATMMessageValidator methods for the commomn message validations,
 * performs validations that are specific to the External Network CashWithdrawal, External Network
 * Fast Cash(Messages - 620, 680 respectively). and posts them using the postingEngine.
 */
public final class ExtNwCashWithdrawal extends ATMFinancialProcessor {

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
    private transient final static Log logger = LogFactory.getLog(ExtNwCashWithdrawal.class.getName());

    /**
     * Holds the cardHoldersAccount
     */
    private String cardHoldersAccount = CommonConstants.EMPTY_STRING;

    /**
     * Holds the settlementAccount
     */
    private String settlementAccount = CommonConstants.EMPTY_STRING;

    /**
     * Holds the transactionCode
     */
    private String transactionCode = CommonConstants.EMPTY_STRING;
    /**
     * Holds the transactionType
     */

    private String customerTransactionNarration = CommonConstants.EMPTY_STRING;
    private String contraTransactionNarration = CommonConstants.EMPTY_STRING;

    /**
     * Holds the configuration details
     */
    private ATMControlDetails controlDetails = null;
    /**
     * Where clause for transaction history record retrieval
     */
    private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";

    private static final String EXTERNAL_LORO_SETTLEMENT_ACCOUNT_DETAILS_QUERRY = "where " + IBOExternalLoroSettlementAccount.ID
            + " =?";
    /**
     * Instance of ATMMessageValidator
     */
    ATMMessageValidator atmMessageValidator = new ATMMessageValidator();

    /**
     * Instance of ATMHelper
     */
    ATMHelper atmHelper = new ATMHelper();

    /**
     * Final variable for Message 614
     */
    private final String EXTERNAL_POS_REFUND = "614";

    /**
     * Constructor
     */
    public ExtNwCashWithdrawal() {

    }

    /**
     * This method validates the message received for cashwithdrawal, creates messages for posting
     * and calls postTransactions() for posting.
     */

    public void execute(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {
        boolean proceed = true;
        // get ATM configuration details
        controlDetails = ATMConfigCache.getInstance().getInformation(env);
        // validate message
        validateExtNwCashWithdrawal(atmSparrowMessage, env);

        // Get the Settlement Account.
        if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
            getSettlementAccount((ATMExNwMessage) atmSparrowMessage, env);
        }
        if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;

        }
        if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
            proceed = checkForDuplicates((ATMExNwMessage) atmSparrowMessage, env);
        }
        if (proceed) {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
            // create posting messages and call post transaction in
            // ATMFinancialProcessor
            postTransactions((ATMSparrowFinancialMessage) atmSparrowMessage, env);

        }
    }

    /**
     * This method validates the Source Account in the ATM Sparrow message.
     */
    private void validateCardHoldersAccount(ATMExNwMessage atmExNwMessage, BankFusionEnvironment env) {
        String branchCode = atmHelper.getBranchSortCode(atmExNwMessage.getSourceBranchCode(), env);
        ATMMessageValidator messageValidator = new ATMMessageValidator();
        String forcePost = atmExNwMessage.getForcePost();
        BusinessValidatorBean validatorBean = new BusinessValidatorBean();
        // Changes for closed account starts
        Object[] field = new Object[] { atmExNwMessage.getAccount() };
        // Changes for closed account ends
        // 1.Validate if the Given Account is a Valid GL Account.
        cardHoldersAccount = atmExNwMessage.getAccount();
        String transactionType = atmExNwMessage.getMessageType() + atmExNwMessage.getTransactionType();
        boolean isaccountMapped = messageValidator.isAccountMappedToCard(atmExNwMessage.getCardNumber(),
                atmExNwMessage.getAccount(), env);

        if (!isaccountMapped) {
            if (ATMConstants.FORCEPOST_0.equals(forcePost) || ATMConstants.FORCEPOST_6.equals(forcePost)) {
                /*
                 * String errorMessage =
                 * BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7537, env,
                 * new Object[] { atmExNwMessage.getCardNumber(), cardHoldersAccount });
                 */
                String errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407537,
                        new Object[] { atmExNwMessage.getCardNumber(), cardHoldersAccount },
                        BankFusionThreadLocal.getUserSession().getUserLocale());
                logger.error(errorMessage);
                atmExNwMessage.setErrorCode(ATMConstants.ERROR);
                atmExNwMessage.setErrorDescription(errorMessage);
                atmExNwMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            }
            else {
                String pseudoName = null;
                // Added check for POS refund
                if (transactionType.equals(EXTERNAL_POS_REFUND)) {
                    pseudoName = controlDetails.getNetworkCrSuspenseAccount();

                    if (atmExNwMessage.getVariableDataType().equals("E")) {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(pseudoName,
                                atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                    }
                    else {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(pseudoName,
                                atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                    // cardHoldersAccount = atmHelper
                    // .getAccountIDfromPseudoName(pseudoName,
                    // atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                }
                else {
                    pseudoName = controlDetails.getNetworkDrSuspenseAccount();
                    // cardHoldersAccount = atmHelper
                    // .getAccountIDfromPseudoName(pseudoName,
                    // atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);

                    if (atmExNwMessage.getVariableDataType().equals("E")) {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(pseudoName,
                                atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                    }
                    else {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(pseudoName,
                                atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                }
                /*
                 * String errorMessage =
                 * BankFusionMessages.getFormattedMessage(BankFusionMessages.MESSAGE_LEVEL, 7539,
                 * env, new Object[] { atmExNwMessage.getCardNumber(), cardHoldersAccount });
                 */
                String errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40400069,
                        new Object[] { atmExNwMessage.getCardNumber(), atmExNwMessage.getAccount() },
                        BankFusionThreadLocal.getUserSession().getUserLocale());
                logger.error(errorMessage);
                atmExNwMessage.setErrorCode(ATMConstants.WARNING);
                atmExNwMessage.setErrorDescription(errorMessage);
                atmExNwMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
            }
            return;
        }

        // Validate if The Given Account is Mapped to the Card Number
        // Provided.

        IBOAttributeCollectionFeature accountValues = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                IBOAttributeCollectionFeature.BONAME, cardHoldersAccount);

        boolean result = validatorBean.validateAccountClosed(accountValues, env);
        if (result) {
            // Changes for closed account starts
            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING, 40580103,
                    BankFusionMessages.ERROR_LEVEL, atmExNwMessage, field, env);
            if (logger.isDebugEnabled()) {
                logger.debug("Account : " + atmExNwMessage.getAccount() + " is Closed !");
                // Changes for closed account starts

            }
            return;
        }
        int optionCreditDebit;
        if (transactionType.equals(EXTERNAL_POS_REFUND)) {
            optionCreditDebit = PasswordProtectedConstants.OPERATION_CREDIT;
        }
        else {
            optionCreditDebit = PasswordProtectedConstants.OPERATION_DEBIT;
        }
        result = atmHelper.isAccountValid(atmExNwMessage, atmExNwMessage.getAccount(), optionCreditDebit, env);
        if (atmExNwMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        if (!result) {
            String psesudoName = controlDetails.getAtmDrSuspenseAccount();
            if (atmExNwMessage.getVariableDataType().equals("E")) {
                cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmExNwMessage.getCurrencySourceAccount(),
                        branchCode, env);
            }
            else {
                cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmExNwMessage.getCurrencyDestDispensed(),
                        branchCode, env);
            }
            // cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmExNwMessage
            // .getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
            return;
        }

        // 4.Validate if the Account is Stopped.
        result = validatorBean.validateAccountStopped(accountValues, env);
        if (result) {
            if (ATMConstants.FORCEPOST_0.equals(forcePost)) {
                String errorMessage = validatorBean.getErrorMessage().getLocalisedMessage();
                logger.error(errorMessage);
                // atmExNwMessage.setErrorCode(ATMConstants.WARNING);
                // atmExNwMessage.setErrorDescription(errorMessage);
                // atmExNwMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR,
                        CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, atmExNwMessage, field, env);
            }
            else {
                String psesudoName = null;
                String errorMessage = validatorBean.getErrorMessage().getLocalisedMessage();
                // Added check for POS refund
                if (transactionType.equals(EXTERNAL_POS_REFUND)) {
                    psesudoName = controlDetails.getNetworkCrSuspenseAccount();

                    if (atmExNwMessage.getVariableDataType().equals("E")) {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                    }
                    else {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                    // cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                    // atmExNwMessage.getCurrencyDestDispensed(), branchCode,
                    // env);
                }
                else {
                    psesudoName = controlDetails.getNetworkDrSuspenseAccount();
                    if (atmExNwMessage.getVariableDataType().equals("E")) {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                    }
                    else {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                    // cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                    // atmExNwMessage.getCurrencyDestDispensed(), branchCode,
                    // env);
                }
                logger.error(errorMessage);
                // atmExNwMessage.setErrorCode(ATMConstants.CRITICAL);
                // atmExNwMessage.setErrorDescription(errorMessage);
                // atmExNwMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                        atmExNwMessage, field, env);

            }
            return;
        }

        // Checking for Password protection Flag.
        boolean passwordProtected = atmHelper.isAccountPasswordProtected(atmExNwMessage, optionCreditDebit,
                ATMConstants.SOURCEACCOUNTTYPE, env);
        if (passwordProtected) {
            return;
        }
        else {
            if (atmExNwMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {

                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR,
                        ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, BankFusionMessages.ERROR_LEVEL, atmExNwMessage,
                        field, env);
            }
            else {
                String psesudoName = null;
                populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                        atmExNwMessage, field, env);
                if (transactionType.equals(EXTERNAL_POS_REFUND)) {

                    psesudoName = controlDetails.getNetworkCrSuspenseAccount();
                    if (atmExNwMessage.getVariableDataType().equals("E")) {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                    }
                    else {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                    // cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                    // atmExNwMessage.getCurrencyDestDispensed(), branchCode,
                    // env);
                }
                else {
                    psesudoName = controlDetails.getNetworkDrSuspenseAccount();
                    if (atmExNwMessage.getVariableDataType().equals("E")) {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                    }
                    else {
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                    // cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                    // atmExNwMessage.getCurrencyDestDispensed(), branchCode,
                    // env);
                }
            }
            return;
        }
    }

    /**
     * This method fetches the settlement account based on Source Country + IMD or network ID in the
     * message for posting.
     */
    private void getSettlementAccount(ATMExNwMessage atmExNwMessage, BankFusionEnvironment env) {
        String branchCode = atmHelper.getBranchSortCode(atmExNwMessage.getSourceBranchCode(), env);
        String pseudoName = CommonConstants.EMPTY_STRING;
        String transactionType = atmExNwMessage.getMessageType() + atmExNwMessage.getTransactionType();
        ATMHelper atmHelper = new ATMHelper();
        try {
            ArrayList params = new ArrayList();
            params.add(atmExNwMessage.getExternalNetworkID());
            List externalAccountListDetails = env.getFactory().findByQuery(IBOExternalLoroSettlementAccount.BONAME,
                    EXTERNAL_LORO_SETTLEMENT_ACCOUNT_DETAILS_QUERRY, params, null);
            if (externalAccountListDetails.size() == 0) {
                /*
                 * String errorMessage =
                 * BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7520, env,
                 * new Object[] { CommonConstants.EMPTY_STRING });
                 */
                String errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407520,
                        new Object[] { CommonConstants.EMPTY_STRING }, BankFusionThreadLocal.getUserSession().getUserLocale());
                logger.error(errorMessage);
                if (ATMConstants.FORCEPOST_0.equals(atmExNwMessage.getForcePost())
                        || ATMConstants.FORCEPOST_6.equals(atmExNwMessage.getForcePost())) {
                    atmExNwMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    atmExNwMessage.setErrorCode(ATMConstants.ERROR);
                    atmExNwMessage.setErrorDescription(errorMessage);
                    return;
                }
                else {
                    atmExNwMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                    atmExNwMessage.setErrorCode(ATMConstants.WARNING);
                    atmExNwMessage.setErrorDescription(errorMessage + " Posting to Suspense Account");
                    if (transactionType.equals(EXTERNAL_POS_REFUND)) {
                        String psesudoName = controlDetails.getNetworkDrSuspenseAccount();
                        if (atmExNwMessage.getVariableDataType().equals("E")) {
                            settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                    atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                        }
                        else {
                            settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                    atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                        }

                        // settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                        // atmExNwMessage.getCurrencyDestDispensed(), branchCode,
                        // env);
                    }
                    else {
                        String psesudoName = controlDetails.getNetworkCrSuspenseAccount();
                        if (atmExNwMessage.getVariableDataType().equals("E")) {
                            settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                    atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                        }
                        else {
                            settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                    atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                        }

                        // settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                        // atmExNwMessage.getCurrencyDestDispensed(), branchCode,
                        // env);
                    }
                }

            }
            else {
                IBOExternalLoroSettlementAccount settlementAccountDetails = (IBOExternalLoroSettlementAccount) externalAccountListDetails
                        .get(0);
                pseudoName = settlementAccountDetails.getF_SETTLEMENTACCOUNT();

                if (atmExNwMessage.getVariableDataType().equals("E")) {
                    settlementAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmExNwMessage.getCurrencySourceAccount(),
                            branchCode, env);
                }
                else {
                    settlementAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmExNwMessage.getCurrencyDestDispensed(),
                            branchCode, env);
                }

                // settlementAccount = atmHelper.getAccountIDfromPseudoName(pseudoName,
                // atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                if (CommonConstants.EMPTY_STRING.equals(settlementAccount)) {

                    if (ATMConstants.FORCEPOST_0.equals(atmExNwMessage.getForcePost())
                            || ATMConstants.FORCEPOST_6.equals(atmExNwMessage.getForcePost())) {
                        /*
                         * String errorMessage =
                         * BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                         * 7520, env, new Object[] { pseudoName });
                         */
                        String errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407520,
                                new Object[] { pseudoName }, BankFusionThreadLocal.getUserSession().getUserLocale());
                        logger.error(errorMessage);
                        atmExNwMessage.setErrorCode(ATMConstants.ERROR);
                        atmExNwMessage.setErrorDescription(errorMessage);
                        atmExNwMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                        return;
                    }
                    else {
                        /*
                         * String errorMessage =
                         * BankFusionMessages.getFormattedMessage(BankFusionMessages.MESSAGE_LEVEL,
                         * 7521, env, new Object[] { pseudoName });
                         */
                        String errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407521,
                                new Object[] { pseudoName }, BankFusionThreadLocal.getUserSession().getUserLocale());
                        logger.error(errorMessage);
                        atmExNwMessage.setErrorCode(ATMConstants.WARNING);
                        atmExNwMessage.setErrorDescription(errorMessage + " Posting to Suspense Account");
                        String psesudoName = null;
                        if (transactionType.equals(EXTERNAL_POS_REFUND)) {
                            psesudoName = controlDetails.getNetworkDrSuspenseAccount();
                        }
                        else {
                            psesudoName = controlDetails.getNetworkCrSuspenseAccount();
                        }

                        if (atmExNwMessage.getVariableDataType().equals("E")) {
                            settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                    atmExNwMessage.getCurrencySourceAccount(), branchCode, env);
                        }
                        else {
                            settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                    atmExNwMessage.getCurrencyDestDispensed(), branchCode, env);
                        }

                        // settlementAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                        // atmExNwMessage.getCurrencyDestDispensed(), branchCode,
                        // env);
                        atmExNwMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                    }
                }
            }
        }
        catch (BankFusionException exception) {

        }
    }

    private void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {

        HashMap map = new HashMap();
        String accountCurrencyCode = CommonConstants.EMPTY_STRING;
        String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
        String transactionType = message.getMessageType() + message.getTransactionType();
        try {
            dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
                    message.getCurrencyDestDispensed(), true);
            accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencySourceAccount(),
                    true);
        }
        catch (BankFusionException exception) {
            dispensedCurrencyCode = SystemInformationManager.getInstance().getBaseCurrencyCode();
            accountCurrencyCode = SystemInformationManager.getInstance().getBaseCurrencyCode();
        }
        if (message.getVariableDataType().equals("E")) {
            dispensedCurrencyCode = accountCurrencyCode;
        }
        map.put("ACCOUNT1_ACCOUNTID", cardHoldersAccount);
        map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
        map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
        map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);

        // Added check for POS refund
        if (transactionType.equals(EXTERNAL_POS_REFUND)) {
            map.put("ACCOUNT1_POSTINGACTION", "C");
        }
        else {
            map.put("ACCOUNT1_POSTINGACTION", "D");
        }

        map.put("ACCOUNT1_TRANSCODE", transactionCode);

        map.put("ACCOUNT2_ACCOUNTID", settlementAccount);
        if (message.getVariableDataType().equals("E")) {
            map.put("ACCOUNT2_AMOUNT", message.getAmount1().abs());
        }
        else {
            map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
        }
        // map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
        map.put("ACCOUNT2_AMOUNT_CurrCode", dispensedCurrencyCode);
        map.put("ACCOUNT2_NARRATIVE", contraTransactionNarration);

        // Added check for POS refund
        if (transactionType.equals(EXTERNAL_POS_REFUND)) {
            map.put("ACCOUNT2_POSTINGACTION", "D");
        }
        else {
            map.put("ACCOUNT2_POSTINGACTION", "C");
        }
        map.put("ACCOUNT2_TRANSCODE", transactionCode);
        map.put("BASEEQUIVALENT", message.getAmount3());
        map.put("TRANSACTIONREFERENCE", getTransactionReference(message));

        logger.info("Value Date:" + message.getDateTimeofTxn());
        // Start: Fix for artf949632
        // map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
        // map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
        map.put("MANUALVALUEDATE", atmHelper.checkForwardValuedTime(message));
        map.put("MANUALVALUETIME", new Time(atmHelper.checkForwardValuedTime(message).getTime()));
        // End: Fix for artf949632
        map.put("MESSAGENUMBER", transactionType);
        map.put("BASEEQUIVALENT", message.getAmount3().abs());
        map.put("AMOUNT4", message.getAmount4().abs());
        map.put("MAINACCOUNTID", message.getAccount());
        // //ChannelID code changes starts.
        String messagenumber = message.getMessageType() + message.getTransactionType();
        if (messagenumber.equals("620") || messagenumber.equals("680")) { // Updated for artf34792.
            map.put("CHANNELID", "ATM");
        }
        else {
            map.put("CHANNELID", "POS");
        }
        // ChannelID code changes ends.
        // External branch code changes starts
        map.put("EXTERNAL_BRANCH_CODE", controlDetails.getEXTERNAL_BRANCH_CODE());
        // External branch code changes ends
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
            if (authorizedFlag.equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
                String errorMessage = outputParams.get("ERRORMESSAGE").toString();
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                if (!errorMessage.equals("")) {
                    message.setErrorCode(ATMConstants.CRITICAL);
                    message.setErrorDescription(errorMessage);
                }
                logger.error(errorMessage);
            }
            if (authorizedFlag.equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                String errorMessage = outputParams.get("ERRORMESSAGE").toString();
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                message.setErrorCode(ATMConstants.CRITICAL);
                message.setErrorDescription(errorMessage);
                logger.error(errorMessage);
                try {
					env.getFactory().rollbackTransaction();
                    env.getFactory().beginTransaction()  ;   ///
                }
                catch (Exception ignored) {
                }
                return;
            }
            env.getFactory().commitTransaction();
            env.getFactory().beginTransaction()  ;   ///
        }
        catch (BankFusionException exception) {
            logger.info(exception.getMessage());
            message.setErrorCode(ATMConstants.ERROR);
            message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            message.setErrorDescription(exception.getMessage());
            try {
                env.getFactory().rollbackTransaction();
                env.getFactory().beginTransaction()  ;   ///
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
     * This method validates the local message details.
     */
    private void validateExtNwCashWithdrawal(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

        atmMessageValidator.validateMessage(atmSparrowMessage, env, ATMMessageValidator.EXTERNAL_MESSAGE_TYPE);
        if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Validate Card Holders Account.
        if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            validateCardHoldersAccount((ATMExNwMessage) atmSparrowMessage, env);
        }
        if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        if (cardHoldersAccount.equals(((ATMSparrowFinancialMessage) atmSparrowMessage).getAccount())) {
            atmMessageValidator.validateSourceCurrency((ATMSparrowFinancialMessage) atmSparrowMessage, env);
        }
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
                atmSparrowMessage.setErrorCode(ATMConstants.CRITICAL);
                atmSparrowMessage.setErrorDescription(errorMessage);
                logger.error(errorMessage);
                return;
            }
            else {
                String errorMessage = "Transaction Not Mapped. Using Default Transaction Type";
                atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                atmSparrowMessage.setErrorCode(ATMConstants.CRITICAL);
                atmSparrowMessage.setErrorDescription(errorMessage);
                logger.error(errorMessage);
                transactionCode = controlDetails.getAtmTransactionType();
            }
        }
    }

    private String getTransactionReference(ATMSparrowFinancialMessage atmExNwMessage) {

        ATMHelper atmHelper = new ATMHelper();
        return atmHelper.getTransactionReference(atmExNwMessage);
    }

    /**
     * This method checks the Transaction details for possible duplicate message.
     * 
     * @returns boolean value
     */
    private boolean checkForDuplicates(ATMExNwMessage atmExNwMessage, BankFusionEnvironment env) {
        boolean proceed = true;
        ArrayList params = new ArrayList();
        List transactionDetails = null;
        params.add(getTransactionReference(atmExNwMessage));

        // find original transaction
        try {
            transactionDetails = env.getFactory().findByQuery(IBOTransaction.BONAME, txnHistoryWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            // if exception then not a duplicate message. Proceed to post.
            proceed = true;
        }
        if(transactionDetails != null){
        	if (transactionDetails.size() > 0 && atmExNwMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
        		proceed = false;
        		Object[] field = new Object[] { getTransactionReference(atmExNwMessage) };
            /*
             * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
             * 7523, BankFusionMessages.MESSAGE_LEVEL, atmExNwMessage, field, env);
             */
        		populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                    ChannelsEventCodes.W_TRANSACTION_ALREADY_POSTED, BankFusionMessages.MESSAGE_LEVEL, atmExNwMessage, field, env);
        	}
        }
        return proceed;
    }

    /**
     * This method populates the error details in the message
     * 
     * @returns String
     */
    private void populateErrorDetails(String authorisedFlag, String errorCode, int errorNo, String errorLevel,
            ATMExNwMessage atmExNwMessage, Object[] fields, BankFusionEnvironment env) {
        atmExNwMessage.setAuthorisedFlag(authorisedFlag);
        atmExNwMessage.setErrorCode(errorCode);
        atmExNwMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorNo, fields));
    }
}
