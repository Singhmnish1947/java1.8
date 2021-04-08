/*Copyright (c) 2009 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 **/

package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.math.BigDecimal;
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
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardIssuersDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBlockingTransactions;
import com.trapedza.bankfusion.bo.refimpl.IBOExternalLoroSettlementAccount;
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

public class UB_ATM_SPA_ReversalAndCorrectionTxns extends ATMFinancialProcessor {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     * Constructor
     */
    public UB_ATM_SPA_ReversalAndCorrectionTxns() {

    }

    /**
     * Holds the configuration details
     */
    private ATMControlDetails controlDetails;
    /**
     * where clause for transaction reference
     */
    private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";
    /**
     * holds the loro transacton type
     */
    private static final String LORO_TRANCATION_TYPE = "99";
    /**
     * holds the Travellers Cheque transacton type
     */
    private static final String TRAVELLERS_CHEQUE_TRANSACTION_TYPE = "29";
    /**
     * where clause to find the blocking transaction
     */
    private static final String QUERRY_FIND_BLOCKING_TRANS = "WHERE " + IBOTransaction.REFERENCE + " = ? AND "
            + IBOTransaction.TYPE + " = ?";
    /**
     * where clause to find details of the blocking transaction
     */
    private static final String findBlockingTransQuerry = "WHERE " + IBOBlockingTransactions.ACCOUNTID + "=?" + " AND "
            + IBOBlockingTransactions.BLOCKINGREFERENCE + "=?" + " AND " + IBOBlockingTransactions.UNBLOCKING + " = ?";

    /**
     * Where clause for atmSource selection
     */
    private static final String atmSourceWhereClause = "WHERE " + IBOATMCardIssuersDetail.ISOCOUNTRYCODE + "=?" + "AND "
            + IBOATMCardIssuersDetail.IMDCODE + "=?";

    /**
     * where clause for atm settlement account
     */
    private static final String atmSettlementAccountWhereClause = "WHERE " + IBOATMSettlementAccount.ATMDEVICEID + "=?";
    /**
     * where clause for transaction details from atm activity details for a transaction reference
     */
    private static final String atmActivityDetailsWhereClause = "WHERE " + IBOATMActivityDetail.TRANSACTIONREFERENCE + "=?";

    private static final String FindBytransactionCode = "WHERE " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + "=?";

    private transient final static Log logger = LogFactory.getLog(UB_ATM_SPA_ReversalAndCorrectionTxns.class.getName());

    /**
     * Holds the cardHoldersAccount
     */
    private String cardHoldersAccount;
    /**
     * This holds the indicator for 2nd currency
     */
    private boolean isSecondCurrency;
    /**
     * Holds shared switch value
     */
    private boolean sharedSwitch;
    /**
     * Holds the cashAccount
     */
    private String cashAccount;
    /**
     * Holds the settlementAccount
     */
    private String settlementAccount;
    /**
     * Holds the loroAccount
     */
    private String loroAccount;
    /**
     * holds the transaction code
     */
    String transCode = CommonConstants.EMPTY_STRING;

    /**
     * holds the transaction reference
     */
    String transactionReference = CommonConstants.EMPTY_STRING;
    /**
     * holds whether the transaction is blocking transaction
     */
    boolean isBlockingTransaction;
    ArrayList transactionList;
    /**
     * Instance of ATMHelper
     */
    ATMHelper atmHelper = new ATMHelper();
    /**
     * holds whether the transaction is reversed
     */
    boolean isTransactionReversed;
    /**
     * holds whether the transaction is Travellers Cheque transaction
     */
    boolean isTravellersChequeTransaction;
    /**
     * Holds the travellersCheque
     */
    private String travellersCheque;
    /**
     * holds whether the transaction is LORO transaction
     */
    boolean isLOROTransaction;
    /**
     * holds the transaction type
     */
    String transactionType = ATMMessageValidator.LOCAL_MESSGE_TYPE;

    String customerTransactionNarration = CommonConstants.EMPTY_STRING;
    String contraTransactionNarration = CommonConstants.EMPTY_STRING;

    /**
     * instance of ATMMessageValidator
     */
    ATMMessageValidator messageValidator = new ATMMessageValidator();

    /**
     * This Function will be Called by the ATMFinancialFatom.
     * 
     * @param message
     * @param env
     */
    public void execute(ATMSparrowMessage message, BankFusionEnvironment env) {
        ATMSparrowFinancialMessage financialMessage = (ATMSparrowFinancialMessage) message;
        validateForcePost(financialMessage);
        atmHelper.updateTransactionNarration(message, env);
        customerTransactionNarration = message.getTxnCustomerNarrative();
        contraTransactionNarration = message.getTxnContraNarrative();

        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        isLOROTransaction = isLOROTransaction(financialMessage);
        isTravellersChequeTransaction = isTravellersChequeTransaction(financialMessage);
        transactionType = getTransactionType(financialMessage);
        messageValidator.validateMessage(financialMessage, env, transactionType);
        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }

        // Code has been commented for issue artf53425 : [M01][SMARTCARD] Reversal of Off - Us
        // transactions
        // [START artf53425]

        /*
         * if (!isLOROTransaction) { if
         * (!messageValidator.doesCardExist(financialMessage.getCardNumber(), env)) {
         * financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
         * financialMessage.setErrorCode(ATMConstants.CRITICAL);
         * financialMessage.setErrorDescription("Invalid Card " + financialMessage.getCardNumber());
         * logger.error("Invalid Card " + financialMessage.getCardNumber()); return; }
         * 
         * // artf46137 [start] // In case of reversal of 625[reversal-725] and 626[reversal-726] we
         * are not suppose // to do validation of mapping of account and card
         * if(!((financialMessage.getMessageType() +
         * financialMessage.getTransactionType()).equals("725") ||
         * (financialMessage.getMessageType() +
         * financialMessage.getTransactionType()).equals("726")|| (financialMessage.getMessageType()
         * + financialMessage.getTransactionType()).equals("727"))&&
         * (financialMessage.getForcePost().equals("2") ||
         * financialMessage.getForcePost().equals("3"))) { if
         * (!messageValidator.areCardandAccountMapped(financialMessage.getCardNumber(),
         * financialMessage .getAccount(), env)) { // changes start for the issue artf45253 and
         * artf44942 if (((financialMessage.getMessageType() +
         * financialMessage.getTransactionType()).equals("089") ||
         * (financialMessage.getMessageType() + financialMessage.getTransactionType()).equals("081")
         * || (financialMessage.getMessageType() +
         * financialMessage.getTransactionType()).equals("084"))&&
         * (financialMessage.getForcePost().equals("2") ||
         * financialMessage.getForcePost().equals("3"))){ Object[] fields = new Object[] {
         * financialMessage.getAccount() };
         * financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
         * financialMessage.setErrorCode(ATMConstants.WARNING);
         * financialMessage.setErrorDescription(BankFusionMessages.
         * getFormattedMessage(ChannelsEventCodes.E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED,
         * fields)); } else {
         * financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
         * financialMessage.setErrorCode(ATMConstants.CRITICAL);
         * financialMessage.setErrorDescription("Card and Account Not Mapped"); return; } // changes
         * ends for the issue artf45253 and artf44942 } } // artf46137 [end] } //[END artf53425]
         */

        isBlockingTransaction = isBlockingTransaction(financialMessage, env);

        if (isBlockingTransaction) {
            boolean isTransactionReversed = checkForPreAuthorization(financialMessage, env);
            if (isTransactionReversed) {
                performUnBlocking(financialMessage, env);
                return;
            }
            else {
                financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                financialMessage.setErrorCode(ATMConstants.INFORMATION);
                financialMessage.setErrorDescription("Transaction Already Reversed");
                return;
            }
        }

        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        String txnID = getTransactionID(financialMessage, env);
        reverseTransactions(financialMessage, txnID, env);

        if (financialMessage.getMessageType().startsWith("8")) {
            controlDetails = ATMConfigCache.getInstance().getInformation(env);
            validateTransactionDetails(message, env);
            if (message.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
                // get cash account and settlement account
                getCashAccountOrSettlementAccount((ATMLocalMessage) message, env);
                if (message.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
                    // create posting messages and call post transaction in ATMFinancialProcessor
                    postTransactions((ATMSparrowFinancialMessage) message, env);
                }
            }
        }
    }

    /**
     * This method fetches the Traveller's Cheque account to be used in posting.
     */
    private void getTravellersChequeAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) throws BankFusionException {

        // System fetches traveler's check account based on ATM device number for dispensed currency
        String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
        logger.info(branchCode);
        ArrayList params = new ArrayList();
        params.add(atmLocalMessage.getDeviceId());
        Iterator atmSettlementAccountDetails = null;
        IBOATMSettlementAccount atmSettlementAccount = null;

        atmSettlementAccountDetails = env.getFactory().findByQuery(IBOATMSettlementAccount.BONAME, atmSettlementAccountWhereClause,
                params, 1);
        if (atmSettlementAccountDetails.hasNext()) {
            atmSettlementAccount = (IBOATMSettlementAccount) atmSettlementAccountDetails.next();
            if (!atmSettlementAccount.getF_TCSETTLEMENTACCOUNT().equals(CommonConstants.EMPTY_STRING)) {
                travellersCheque = atmHelper.getAccountIDfromPseudoName(atmSettlementAccount.getF_TCSETTLEMENTACCOUNT(),
                        atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                if (travellersCheque.equals(CommonConstants.EMPTY_STRING)) {
                    Object[] field = new Object[] { atmSettlementAccount.getF_TCSETTLEMENTACCOUNT() };
                    if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                        // populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                        // ATMConstants.WARNING, 7549, BankFusionMessages.ERROR_LEVEL,
                        // atmLocalMessage, field, env);
                        populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST, atmLocalMessage, field);
                    }
                    else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                            || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                        // populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                        // ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL,
                        // atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
                        populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, atmLocalMessage,
                                new Object[] { atmLocalMessage.getAccount() });
                        if (controlDetails != null) {
                            travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                        }
                    }
                }
                else {
                    Object[] field = new Object[] { travellersCheque };
                    IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                            IBOAttributeCollectionFeature.BONAME, travellersCheque);
                    BusinessValidatorBean validatorBean = new BusinessValidatorBean();
                    if (validatorBean.validateAccountClosed(accountItem, env)) {
                        if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                            // populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                            // ATMConstants.WARNING, 7549, BankFusionMessages.ERROR_LEVEL,
                            // atmLocalMessage, field, env);
                            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                    ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST, atmLocalMessage, field);
                        }
                        else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                                || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                            // populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                            // ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL,
                            // atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, atmLocalMessage,
                                    new Object[] { atmLocalMessage.getAccount() });
                            if (controlDetails != null) {
                                travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                                        atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                            }
                            logger.error("Account : " + travellersCheque + " is Closed !");
                        }

                    }
                    else if (validatorBean.validateAccountStopped(accountItem, env)) {
                        if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                            // populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                            // ATMConstants.WARNING, 7549, BankFusionMessages.ERROR_LEVEL,
                            // atmLocalMessage, field, env);
                            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                    ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST, atmLocalMessage, field);
                        }
                        else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                                || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                            // populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                            // ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL,
                            // atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, atmLocalMessage,
                                    new Object[] { atmLocalMessage.getAccount() });
                            if (controlDetails != null) {
                                travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                                        atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                            }
                            logger.error("Account : " + travellersCheque + " is Closed !");
                        }
                    }
                }
            }
            else {
                Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    // populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                    // ATMConstants.WARNING, 7549, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                    // field, env);
                    populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST, atmLocalMessage, field);
                    // atmLocalMessage.setErrorDescription(String.valueOf(FatomUtils.getBankFusionException(7549,
                    // new Object[] { atmLocalMessage.getAccount() }, logger, env)));
                    // atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                    // 7554, env, new Object[] { atmLocalMessage.getAccount()}));
                    atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(
                            ChannelsEventCodes.E_MAIN_ACCT_STOPPED_SUSPENSE_ACC_WILL_BE_UPDATED,
                            new Object[] { atmLocalMessage.getAccount() }));
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    // populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                    // ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL,
                    // atmLocalMessage, new Object[] { atmLocalMessage.getAccount() }, env);
                    populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, atmLocalMessage,
                            new Object[] { atmLocalMessage.getAccount() });
                    if (controlDetails != null) {
                        travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                                atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                }
            }
        }
        else {
            Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                // populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7549, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                // field, env);
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST, atmLocalMessage, field);
                // atmLocalMessage.setErrorDescription(String.valueOf(FatomUtils.getBankFusionException(7549,
                // new Object[] { atmLocalMessage.getAccount() }, logger, env)));
                // atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                // 7554, env, new Object[] { atmLocalMessage.getAccount()}));
                // atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(7554,
                // new Object[] { atmLocalMessage.getAccount()}));
                atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(
                        ChannelsEventCodes.E_MAIN_ACCT_STOPPED_SUSPENSE_ACC_WILL_BE_UPDATED,
                        new Object[] { atmLocalMessage.getAccount() }));
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                // populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7548, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage,
                // new Object[] { atmLocalMessage.getAccount() }, env);
                populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL, atmLocalMessage,
                        new Object[] { atmLocalMessage.getAccount() });
                if (controlDetails != null) {
                    travellersCheque = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                            atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                }
            }
        }
    }

    /**
     * This method is to get the LORO Account if the message type is LORO.
     * 
     * @param message
     * @param env
     */
    private void getLOROAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {
        String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
        try {
            IBOExternalLoroSettlementAccount externalSettlementAccount = (IBOExternalLoroSettlementAccount) env.getFactory()
                    .findByPrimaryKey(IBOExternalLoroSettlementAccount.BONAME, atmLocalMessage.getLoroMailbox());
            loroAccount = atmHelper.getAccountIDfromPseudoName(externalSettlementAccount.getF_SETTLEMENTACCOUNT(),
                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
            if (loroAccount.equals(CommonConstants.EMPTY_STRING)) {
                Object[] field = new Object[] { externalSettlementAccount.getF_SETTLEMENTACCOUNT() };
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    /*
                     * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                     * field, env);
                     */
                    populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, atmLocalMessage, field);
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    /*
                     * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.CRITICAL, 7532, BankFusionMessages.MESSAGE_LEVEL,
                     * atmLocalMessage, field, env);
                     */
                    populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_INVALID_SETTLEMT_ACCT_ATM_DR_SUSE_ACCT_UPDATED, atmLocalMessage, field);
                    if (controlDetails != null) {
                        loroAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmDrSuspenseAccount(),
                                atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                }
            }
            else {
                Object[] field = new Object[] { loroAccount };
                IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                        IBOAttributeCollectionFeature.BONAME, loroAccount);
                BusinessValidatorBean validatorBean = new BusinessValidatorBean();
                if (validatorBean.validateAccountClosed(accountItem, env)) {
                    if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                        /*
                         * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, atmLocalMessage, field);
                    }
                    else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                            || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                        /*
                         * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.CRITICAL, 7521, BankFusionMessages.MESSAGE_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT, atmLocalMessage, field);
                        if (controlDetails != null) {
                            loroAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmDrSuspenseAccount(),
                                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                        }
                        logger.error("Account : " + loroAccount + " is Closed !");
                    }
                    else if (validatorBean.validateAccountStopped(accountItem, env)) {

                        if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                            /*
                             * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                    ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, atmLocalMessage, field);
                        }
                        else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                                || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                            /*
                             * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.CRITICAL, 7521, BankFusionMessages.MESSAGE_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT, atmLocalMessage, field);
                            if (controlDetails != null) {
                                loroAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmDrSuspenseAccount(),
                                        atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                            }
                            logger.error("Account : " + loroAccount + " is Stopped !");
                        }
                    }
                }
            }
        }
        catch (FinderException fe) {
            Object[] field = new Object[] { atmLocalMessage.getLoroMailbox() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                /*
                 * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                 * field, env);
                 */
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, atmLocalMessage, field);
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                /*
                 * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                 * 7532, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
                 */
                populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.W_INVALID_SETTLEMT_ACCT_ATM_DR_SUSE_ACCT_UPDATED, atmLocalMessage, field);
                if (controlDetails != null) {
                    loroAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmDrSuspenseAccount(),
                            atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                }
            }
        }
    }

    /**
     * This method calls the ATM financial posting business process to post the financial
     * transaction. used for posting.
     * 
     * @param message
     * @param env
     */
    private void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {

        HashMap map = new HashMap();
        String accountCurrencyCode = CommonConstants.EMPTY_STRING;
        String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
        BigDecimal newAmount2 = null;
        BigDecimal newAmount4 = null;
        ArrayList params = new ArrayList();
        params.add(atmHelper.getTransactionReference(message));
        Iterator atmActivity = null;
        IBOATMActivityDetail atmActivityDetail = null;
        String messagenumber = message.getMessageType() + message.getTransactionType();
        atmActivity = env.getFactory().findByQuery(IBOATMActivityDetail.BONAME, atmActivityDetailsWhereClause, params, 1);
        if (atmActivity.hasNext()) {
            atmActivityDetail = (IBOATMActivityDetail) atmActivity.next();
            newAmount2 = atmActivityDetail.getF_TRANSACTIONAMOUNT().subtract(message.getAmount2());
            newAmount4 = atmActivityDetail.getF_COMMAMOUNT().subtract(message.getAmount4());
        }

        try {
            dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
                    message.getCurrencyDestDispensed(), true);
            accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencySourceAccount(),
                    true);
        }
        catch (BankFusionException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception occured" + exception);
            }
        }
        map.put("TRANSACTIONSOURCE", "Local");
        if (isLOROTransaction) {
            map.put("ACCOUNT1_ACCOUNTID", loroAccount);
        }
        else {
            map.put("ACCOUNT1_ACCOUNTID", cardHoldersAccount);
        }
        map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
        map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
        map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
        map.put("ACCOUNT1_POSTINGACTION", "D");
        map.put("ACCOUNT1_TRANSCODE", transCode);
        map.put("AMOUNT4", newAmount4.abs());
        map.put("MAINACCOUNTID", message.getAccount());
        if (!sharedSwitch) {
            if (isTravellersChequeTransaction)
                map.put("ACCOUNT2_ACCOUNTID", travellersCheque);
            else map.put("ACCOUNT2_ACCOUNTID", cashAccount);
        }
        else if (sharedSwitch) {
            map.put("ACCOUNT2_ACCOUNTID", settlementAccount);

        }
        map.put("ACCOUNT2_AMOUNT", newAmount2.abs());
        map.put("ACCOUNT2_AMOUNT_CurrCode", dispensedCurrencyCode);
        map.put("ACCOUNT2_NARRATIVE", contraTransactionNarration);
        map.put("ACCOUNT2_POSTINGACTION", "C");
        map.put("ACCOUNT2_TRANSCODE", transCode);
        map.put("BASEEQUIVALENT", message.getAmount3().abs());
        map.put("TRANSACTIONREFERENCE", atmHelper.getTransactionReference(message));
        map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
        map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
        map.put("MESSAGENUMBER", messagenumber);
        if (ATMConstants.FORCEPOST_0.equals(message.getForcePost()) || ATMConstants.FORCEPOST_6.equals(message.getForcePost())) {
            map.put("FORCEPOST", Boolean.FALSE);
        }
        else {
            map.put("FORCEPOST", Boolean.TRUE);
        }

        // Post the Transactions.
        try {
            HashMap outputParams = MFExecuter.executeMF(ATMConstants.FINANCIAL_POSTING_MICROFLOW_NAME, env, map);

            String authorizedFlag = outputParams.get("AUTHORIZEDFLAG").toString();
            if (authorizedFlag.equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
                String errorMessage = outputParams.get("ERRORMESSAGE").toString();
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                if (!errorMessage.equals("")) {
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
                    env.getFactory().beginTransaction();//
                }
                catch (Exception ignored) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Exception occured" + ignored);
                    }
                }
                return;
            }
            env.getFactory().commitTransaction();
            env.getFactory().beginTransaction();
        }
        catch (BankFusionException exception) {
            logger.info("Transaction is Not Authorized: --> " + exception.getMessage());
            message.setErrorCode(ATMConstants.ERROR);
            message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            message.setErrorDescription(exception.getMessage());
            try {
                env.getFactory().rollbackTransaction();
                env.getFactory().beginTransaction();
            }
            catch (Exception ignored) {
                if (logger.isErrorEnabled()) {
                    logger.error("Exception occured" + ignored);
                }
            }
        }
        finally {
            try {
                env.getFactory().beginTransaction();
            }
            catch (Exception ignored) {

                if (logger.isErrorEnabled()) {
                    logger.error("Exception occured" + ignored);
                }
            }
        }
    }

    /**
     * This method is used to decide the settlement account based upon the shared switch
     * 
     * @param atmLocalMessage
     * @param env
     */
    private void getCashAccountOrSettlementAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

        if (controlDetails != null) {
            sharedSwitch = controlDetails.isSharedSwitch();
            if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
                if (!sharedSwitch) {
                    if (isTravellersChequeTransaction)
                        getTravellersChequeAccount(atmLocalMessage, env);
                    else getCashAccount(atmLocalMessage, env);
                }
                else if (sharedSwitch) {
                    getSettlementAccount(atmLocalMessage, env);
                }
            }
        }
    }

    /**
     * This method fetches the cash account to be used in posting.
     * 
     * @param atmLocalMessage
     * @param env
     */
    private void getCashAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

        // System fetches cash account based on ATM device number for dispensed currency
        String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
        logger.info(branchCode);
        ArrayList params = new ArrayList();
        params.add(atmLocalMessage.getDeviceId());
        Iterator atmSettlementAccountDetails = null;
        IBOATMSettlementAccount atmSettlementAccount = null;

        atmSettlementAccountDetails = env.getFactory().findByQuery(IBOATMSettlementAccount.BONAME, atmSettlementAccountWhereClause,
                params, 1);
        if (atmSettlementAccountDetails.hasNext()) {
            atmSettlementAccount = (IBOATMSettlementAccount) atmSettlementAccountDetails.next();
            cashAccount = atmHelper.getAccountIDfromPseudoName(atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT(),
                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
            if (cashAccount.equals(CommonConstants.EMPTY_STRING)) {
                Object[] field = new Object[] { atmSettlementAccount.getF_CASHSETTLEMENTACCOUNT() };
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    /*
                     * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.WARNING, 7518, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                     * field, env);
                     */
                    populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_INVALID_CASH_ACCOUNT, atmLocalMessage, field);
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    if (isSecondCurrency) {
                        /*
                         * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.CRITICAL, 7529, BankFusionMessages.MESSAGE_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                ChannelsEventCodes.W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD, atmLocalMessage, field);
                    }
                    else {
                        /*
                         * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.CRITICAL, 7519, BankFusionMessages.MESSAGE_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED, atmLocalMessage, field);
                    }
                    if (controlDetails != null) {
                        cashAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                                atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                }
            }
            else {
                Object[] field = new Object[] { cashAccount };
                IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                        IBOAttributeCollectionFeature.BONAME, cashAccount);
                BusinessValidatorBean validatorBean = new BusinessValidatorBean();
                if (validatorBean.validateAccountClosed(accountItem, env)) {
                    if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                        /*
                         * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.WARNING, 7518, BankFusionMessages.ERROR_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                ChannelsEventCodes.E_INVALID_CASH_ACCOUNT, atmLocalMessage, field);
                    }
                    else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                            || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                        if (isSecondCurrency) {
                            /*
                             * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.CRITICAL, 7529, BankFusionMessages.MESSAGE_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD, atmLocalMessage, field);
                        }
                        else {
                            /*
                             * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.CRITICAL, 7519, BankFusionMessages.MESSAGE_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED, atmLocalMessage, field);
                        }
                        if (controlDetails != null) {
                            cashAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                        }
                        logger.error("Account : " + cashAccount + " is Closed !");
                    }

                }
                else if (validatorBean.validateAccountStopped(accountItem, env)) {
                    if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                        /*
                         * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.WARNING, 7518, BankFusionMessages.ERROR_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                ChannelsEventCodes.E_INVALID_CASH_ACCOUNT, atmLocalMessage, field);
                    }
                    else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                            || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                        if (isSecondCurrency) {
                            /*
                             * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.CRITICAL, 7529, BankFusionMessages.MESSAGE_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD, atmLocalMessage, field);
                        }
                        else {
                            /*
                             * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.CRITICAL, 7519, BankFusionMessages.MESSAGE_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED, atmLocalMessage, field);
                        }
                        if (controlDetails != null) {
                            cashAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                        }
                        logger.error("Account : " + cashAccount + " is Closed !");
                    }
                }
            }
        }
        else {
            Object[] field = new Object[] { atmLocalMessage.getDeviceId() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                /*
                 * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.WARNING, 7518, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                 * field, env);
                 */
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_CASH_ACCOUNT, atmLocalMessage, field);
                /*
                 * atmLocalMessage.setErrorDescription(String.valueOf(new BankFusionException(,
                 * Object[] params, logger, env)));
                 */
                atmLocalMessage.setErrorDescription(String.valueOf(new BankFusionException(
                        ChannelsEventCodes.E_INVALID_CASH_ACCOUNT, new Object[] { atmLocalMessage.getAccount() }, logger, env)));
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                if (isSecondCurrency) {
                    /*
                     * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.CRITICAL, 7529, BankFusionMessages.MESSAGE_LEVEL,
                     * atmLocalMessage, field, env);
                     */
                    populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD, atmLocalMessage, field);
                }
                else {
                    /*
                     * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.CRITICAL, 7519, BankFusionMessages.MESSAGE_LEVEL,
                     * atmLocalMessage, field, env);
                     */
                    populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED, atmLocalMessage, field);
                }
                if (controlDetails != null) {
                    cashAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getAtmCrSuspenseAccount(),
                            atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                }
            }
        }
    }

    /**
     * This method fetches the settlement account based on Source Country + IMD or network ID in the
     * message for posting.
     * 
     * @param atmLocalMessage
     * @param env
     */
    private void getSettlementAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

        // System fetches settlement account based on Source Country + IMD or network ID in the
        // message
        String branchCode = atmHelper.getBranchSortCode(atmLocalMessage.getSourceBranchCode(), env);
        ArrayList params = new ArrayList();
        params.add(atmLocalMessage.getSourceCountryCode());
        params.add(atmLocalMessage.getSourceIMD());
        Iterator cardIssuersSettlementDetails = null;
        IBOATMCardIssuersDetail cardIssuersSettlementAccount = null;

        cardIssuersSettlementDetails = env.getFactory()
                .findByQuery(IBOATMCardIssuersDetail.BONAME, atmSourceWhereClause, params, 2);
        if (cardIssuersSettlementDetails.hasNext()) {
            cardIssuersSettlementAccount = (IBOATMCardIssuersDetail) cardIssuersSettlementDetails.next();
            settlementAccount = atmHelper.getAccountIDfromPseudoName(cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT(),
                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
            if (settlementAccount.equals(CommonConstants.EMPTY_STRING)) {
                Object[] field = new Object[] { cardIssuersSettlementAccount.getF_SETTLEMENTACCOUNT() };
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    /*
                     * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                     * field, env);
                     */
                    populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, atmLocalMessage, field);
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    if (isSecondCurrency) {
                        /*
                         * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.CRITICAL, 7530, BankFusionMessages.MESSAGE_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                ChannelsEventCodes.W_INVALID_2ND_CUR_SETLMT_UPDT_ACCT_ENW_CR_SUS_ACCT, atmLocalMessage, field);
                    }
                    else {
                        /*
                         * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.CRITICAL, 7521, BankFusionMessages.MESSAGE_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT, atmLocalMessage, field);
                    }
                    if (controlDetails != null) {
                        settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getNetworkCrSuspenseAccount(),
                                atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                    }
                }
            }
            else {
                Object[] field = new Object[] { settlementAccount };
                IBOAttributeCollectionFeature accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                        IBOAttributeCollectionFeature.BONAME, settlementAccount);
                BusinessValidatorBean validatorBean = new BusinessValidatorBean();
                if (validatorBean.validateAccountClosed(accountItem, env)) {
                    if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                        /*
                         * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                ChannelsEventCodes.E_INVALID_ACCOUNT, atmLocalMessage, field);
                    }
                    else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
                        /*
                         * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                         * ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL,
                         * atmLocalMessage, field, env);
                         */
                        populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, atmLocalMessage, field);
                        if (controlDetails != null) {
                            settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getNetworkCrSuspenseAccount(),
                                    atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                        }
                        logger.error("Account : " + settlementAccount + " is Closed !");
                    }
                    else if (validatorBean.validateAccountStopped(accountItem, env)) {

                        if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                            /*
                             * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                                    ChannelsEventCodes.E_INVALID_ACCOUNT, atmLocalMessage, field);
                        }
                        else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
                            /*
                             * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                             * ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL,
                             * atmLocalMessage, field, env);
                             */
                            populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                                    ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, atmLocalMessage, field);
                            if (controlDetails != null) {
                                settlementAccount = atmHelper.getAccountIDfromPseudoName(
                                        controlDetails.getNetworkCrSuspenseAccount(), atmLocalMessage.getCurrencyDestDispensed(),
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
                /*
                 * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.WARNING, 7520, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                 * field, env);
                 */
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_SETTLEMENT_ACCOUNT, atmLocalMessage, field);

            }

            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                if (isSecondCurrency) {
                    /*
                     * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.CRITICAL, 7530, BankFusionMessages.MESSAGE_LEVEL,
                     * atmLocalMessage, field, env);
                     */
                    populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_INVALID_2ND_CUR_SETLMT_UPDT_ACCT_ENW_CR_SUS_ACCT, atmLocalMessage, field);
                }
                else {
                    /*
                     * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.CRITICAL, 7521, BankFusionMessages.MESSAGE_LEVEL,
                     * atmLocalMessage, field, env);
                     */
                    populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT, atmLocalMessage, field);
                }
                if (controlDetails != null) {
                    settlementAccount = atmHelper.getAccountIDfromPseudoName(controlDetails.getNetworkCrSuspenseAccount(),
                            atmLocalMessage.getCurrencyDestDispensed(), branchCode, env);
                }
            }
        }
    }

    /**
     * This method validates the local message details.
     * 
     * @param atmSparrowMessage
     * @param env
     */
    private void validateTransactionDetails(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

        transCode = atmHelper.getBankTransactionCode(atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType(),
                env);

        if (transCode.equals(CommonConstants.EMPTY_STRING)) {
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
                transCode = controlDetails.getAtmTransactionType();
            }
        }
        if (!isLOROTransaction) {
            if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                validateCardHoldersAccount((ATMLocalMessage) atmSparrowMessage, env);
            }
        }
        else {
            getLOROAccount((ATMLocalMessage) atmSparrowMessage, env);
        }
        if ((((ATMSparrowFinancialMessage) atmSparrowMessage).getAccount().equals(cardHoldersAccount))
                && (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))) {
            messageValidator.validateSourceCurrency((ATMSparrowFinancialMessage) atmSparrowMessage, env);
        }

        if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            messageValidator.validateDispensedCurrency((ATMSparrowFinancialMessage) atmSparrowMessage, env);
        }
        checkSecondCurrency((ATMLocalMessage) atmSparrowMessage);
    }

    /**
     * This method checks whether the message is of 2nd currency used for posting.
     * 
     * @param atmLocalMessage
     */
    private void checkSecondCurrency(ATMLocalMessage atmLocalMessage) {
        if (atmLocalMessage.getTransactionType().equals("85")) {
            isSecondCurrency = true;
        }
    }

    /**
     * This method populates the error details in the message
     * 
     * @returns String
     */
    private void populateErrorDetails(String authorisedFlag, String errorCode, int errorNo, ATMLocalMessage atmLocalMessage,
            Object[] fields) {
        atmLocalMessage.setAuthorisedFlag(authorisedFlag);
        atmLocalMessage.setErrorCode(errorCode);
        atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorNo, fields));
    }

    /**
     * This method validates the Source Account in the ATM Sparrow message.
     * 
     * @param atmLocalMessage
     * @param env
     */

    private void validateCardHoldersAccount(ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

        Object[] field = new Object[] { atmLocalMessage.getAccount() };
        IBOAttributeCollectionFeature accountItem = null;
        // check for closed or stopped accounts
        try {
            accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
                    atmLocalMessage.getAccount());
            cardHoldersAccount = atmLocalMessage.getAccount();
        }
        catch (FinderException fe) {
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7516,
                // BankFusionMessages.ERROR_LEVEL, atmLocalMessage, field, env);
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_ACCOUNT, atmLocalMessage, field);
            }
            else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
                /*
                 * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                 * 7517, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
                 */
                populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, atmLocalMessage, field);
                if (controlDetails != null) {
                    String psesudoName = controlDetails.getAtmDrSuspenseAccount();
                    cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencyDestDispensed(), CommonConstants.EMPTY_STRING, env);
                    return;
                }
            }
        }
        // updation as per the latest Use case for account closed starts.
        if (accountItem != null) {
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                /*
                 * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.WARNING, 7566, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                 * field, env);
                 */
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        CommonsEventCodes.E_ACCOUNT_CLOSED, atmLocalMessage, field);
                if (logger.isDebugEnabled()) {
                    logger.error("Account : " + atmLocalMessage.getAccount() + " is Closed !");
                }
            }
            // updation as per the latest Use case for account closed ends.
            else if (validatorBean.validateAccountStopped(accountItem, env)) {
                if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                    /*
                     * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.WARNING, 7567, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                     * field, env);
                     */
                    populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                            CommonsEventCodes.E_ACCOUNT_STOPPED, atmLocalMessage, field);
                }
                else if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                    /*
                     * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG,
                     * ATMConstants.CRITICAL, 7517, BankFusionMessages.MESSAGE_LEVEL,
                     * atmLocalMessage, field, env);
                     */
                    populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                            ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, atmLocalMessage, field);
                    if (controlDetails != null) {
                        String psesudoName = controlDetails.getAtmDrSuspenseAccount();
                        cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                                atmLocalMessage.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
                        return;
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.error("Account : " + atmLocalMessage.getAccount() + " is Stopped !");
                }
            }
        }
        // Check ATMCardAccMap to see whether the card no and the account are mapped.

        if (!messageValidator.isAccountMappedToCard(atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount(), env)) {
            field = new Object[] { atmLocalMessage.getCardNumber(), atmLocalMessage.getAccount() };
            if (atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmLocalMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                /*
                 * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.WARNING, 7537, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                 * field, env);
                 */
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED, atmLocalMessage, field);
            }
            else {
                /*
                 * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.CRITICAL, 7511, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                 * field, env);
                 */
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_CARD_FORCE_POST_NOT_POSTED, atmLocalMessage, field);
                if (controlDetails != null) {
                    String psesudoName = controlDetails.getAtmDrSuspenseAccount();
                    cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName,
                            atmLocalMessage.getCurrencySourceAccount(), CommonConstants.EMPTY_STRING, env);
                    return;
                }
            }
        }
        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Checking for Password protection Flag.

        if (atmLocalMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        boolean result = atmHelper.isAccountValid(atmLocalMessage, PasswordProtectedConstants.OPERATION_DEBIT, env);
        if (!result && controlDetails != null) {
            String psesudoName = controlDetails.getAtmDrSuspenseAccount();
            cardHoldersAccount = atmHelper.getAccountIDfromPseudoName(psesudoName, atmLocalMessage.getCurrencySourceAccount(),
                    CommonConstants.EMPTY_STRING, env);
            return;
        }
    }

    /**
     * This method is used to get the transaction reference
     * 
     * @param atmPosMessage
     * @return
     */
    private String getTransactionReference(ATMSparrowFinancialMessage atmPosMessage) {

        ATMHelper atmHelper = new ATMHelper();
        return atmHelper.getTransactionReference(atmPosMessage);
    }

    /**
     * This method is used to check whether the transaction is already reversed or not
     * 
     * @param message
     * @param env
     * @return
     */
    private String getTransactionID(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
        String txnID = CommonConstants.EMPTY_STRING;
        ArrayList params = new ArrayList();
        params.add(getTransactionReference(message));
        try {
            List transactionList = env.getFactory().findByQuery(IBOTransaction.BONAME, txnHistoryWhereClause, params, null);
            if (transactionList.size() == 0) {
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                message.setErrorCode(ATMConstants.ERROR);
                message.setErrorDescription("Original Transaction Not Posted");
                return txnID;
            }

            Iterator iterator = transactionList.iterator();
            while (iterator.hasNext()) {
                IBOTransaction transaction = (IBOTransaction) iterator.next();
                if (transaction.getF_REVERSALINDICATOR() == 1 || transaction.getF_REVERSALINDICATOR() == 2) {
                    isTransactionReversed = true;
                    message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.ERROR);
                    message.setErrorDescription("Transaction Already Reversed");
                    break;
                }
                else {
                    txnID = transaction.getF_TRANSACTIONID();
                    break;
                }
            }
        }
        catch (BankFusionException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception occured" + exception);
            }
        }
        return txnID;
    }

    /**
     * This method is used for reversing the transaction
     * 
     * @param message
     * @param txnID
     * @param env
     */

    private void reverseTransactions(ATMSparrowFinancialMessage message, String txnID, BankFusionEnvironment env) {
        try {
            HashMap inParams = new HashMap();
            inParams.put("TRANSACTIONID", txnID);
            inParams.put("AUTHORIZATIONREQUIRED", Boolean.FALSE);
            inParams.put("TRANSACTIONREFERENCE", getTransactionReference(message));
            String messagenumber = message.getMessageType() + message.getTransactionType();
            if (messagenumber.equals("720") || messagenumber.equals("780") || (messagenumber.charAt(0) == '0')
                    || (messagenumber.charAt(0) == '8')) {
                inParams.put("CHANNELID", "ATM");
            }
            else {
                inParams.put("CHANNELID", "POS");
            }
            inParams.put("FORCEPOST", Boolean.TRUE);
            inParams.put("AMOUNT4", message.getAmount4().abs());
            inParams.put("MAINACCOUNTID", message.getAccount());
            HashMap outParams = MFExecuter.executeMF("ATM_SPA_Reversals", env, inParams);
            boolean result = ((Boolean) outParams.get("RESULT")).booleanValue();
            if (!result) {
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                message.setErrorCode(ATMConstants.CRITICAL);
                message.setErrorDescription(outParams.get("MESSAGE").toString());
            }
        }
        catch (BankFusionException exception) {
            message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            message.setErrorCode(ATMConstants.CRITICAL);
            message.setErrorDescription(exception.getLocalisedMessage());
        }
    }

    /**
     * This method is used to validate forcepost for reversal
     * 
     * @param message
     */
    private void validateForcePost(ATMSparrowFinancialMessage message) {

        if (!(message.getForcePost().equals(ATMConstants.FORCEPOST_1) || message.getForcePost().equals(ATMConstants.FORCEPOST_2) || message
                .getForcePost().equals(ATMConstants.FORCEPOST_3))) {
            message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            message.setErrorCode(ATMConstants.CRITICAL);
            message.setErrorDescription("Invalid Force Post Value");
            return;
        }

    }

    /**
     * This method is used to check whether the transaction is LORO transaction
     * 
     * @param message
     * @return
     */
    private boolean isLOROTransaction(ATMSparrowFinancialMessage message) {
        boolean result = false;
        if (message.getTransactionType().equals(LORO_TRANCATION_TYPE)) {
            result = true;
            return result;
        }
        else {
            result = false;
            return result;

        }
    }

    /**
     * This method is used to check whether the transaction is Travellers cheque transaction
     * 
     * @param message
     * @return
     */
    private boolean isTravellersChequeTransaction(ATMSparrowFinancialMessage message) {
        boolean result = false;
        if (message.getTransactionType().equals(TRAVELLERS_CHEQUE_TRANSACTION_TYPE)) {
            result = true;
            return result;
        }
        else {
            result = false;
            return result;

        }
    }

    /**
     * This method is used to check whether the transaction is Local transaction or external
     * transaction
     * 
     * @param message
     * @return
     */
    private String getTransactionType(ATMSparrowFinancialMessage message) {
        String result = ATMMessageValidator.LOCAL_MESSGE_TYPE;
        if (message.getVariableDataType().equalsIgnoreCase("A")) {
            result = ATMMessageValidator.LOCAL_MESSGE_TYPE;
        }
        else {
            result = ATMMessageValidator.EXTERNAL_MESSAGE_TYPE;
        }
        return result;
    }

    /**
     * This method is used to perform unblocking incase the blocking transaction exists
     * 
     * @param atmPosMessage
     * @param env
     */

    private void performUnBlocking(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
        BigDecimal amount = atmPosMessage.getAmount1().abs();
        String blockingCategory = "ATM";
        String transactionCode = getTransCode(env);
        String narrative = getTransactionNarration(env);
        Date postingDate = SystemInformationManager.getInstance().getBFBusinessDate();
        HashMap paramsforTellerBlocking = new HashMap();
        paramsforTellerBlocking.put("ACCOUNTID", atmPosMessage.getAccount());
        paramsforTellerBlocking.put("AMOUNT", amount.abs());
        paramsforTellerBlocking.put("BLOCKINGCATEGORY", blockingCategory);
        paramsforTellerBlocking.put("NARRATIVE", narrative);
        paramsforTellerBlocking.put("POSTINGDATE", postingDate);
        paramsforTellerBlocking.put("TRANSACTIONCODE", transactionCode);
        paramsforTellerBlocking.put("TRANSACTIONREFERENCE", getTransactionReference(atmPosMessage));
        paramsforTellerBlocking.put("ISBLOCKING", Boolean.FALSE);
        paramsforTellerBlocking.put("POSTINGACTION", "C");

        try {
            MFExecuter.executeMF(ATMConstants.BLOCKING_TRANSACTION, env, paramsforTellerBlocking);
        }
        catch (BankFusionException exception) {
            String message = exception.getLocalisedMessage();
            atmPosMessage.setErrorCode(ATMConstants.CRITICAL);
            atmPosMessage.setErrorDescription(message);
        }
    }

    /**
     * This method is used to get the MIS transaction code
     * 
     * @param message
     * @param env
     * @return
     */
    private String getTransCode(BankFusionEnvironment env) {
        String transactionCode = CommonConstants.EMPTY_STRING;
        try {
            ArrayList params = new ArrayList();
            params.add(transCode);
            IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes) env.getFactory().findFirstByQuery(
                    IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);

            /*
             * IBOATMTransactionCodes codes = (IBOATMTransactionCodes)
             * env.getFactory().findByPrimaryKey( IBOATMTransactionCodes.BONAME, transactionCode);
             */
            transactionCode = atmTransactionCodes.getF_MISTRANSACTIONCODE();
        }
        catch (BankFusionException exception) {
            try {
                transactionCode = ATMConfigCache.getInstance().getInformation(env).getPosTxnType();
            }
            catch (BankFusionException innerException) {
                if (logger.isErrorEnabled()) {
                    logger.error("Exception occured" + innerException);
                }
            }
        }
        return transactionCode;
    }

    /**
     * This method is used to get the transaction narration
     * 
     * @param env
     * @return
     */
    private String getTransactionNarration(BankFusionEnvironment env) {
        String narration = CommonConstants.EMPTY_STRING;
        try {
            narration = ATMConfigCache.getInstance().getInformation(env).getSuspectRevTxnNarr();
        }
        catch (BankFusionException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("Exception" + exception);
            }
        }
        return narration;
    }

    /**
     * This method is used to check for preauthorisation
     * 
     * @param atmfinancialMessage
     * @param env
     * @return
     */
    private boolean checkForPreAuthorization(ATMSparrowFinancialMessage atmfinancialMessage, BankFusionEnvironment env) {
        boolean result = false;
        BigDecimal blockedAmount = CommonConstants.BIGDECIMAL_ZERO;
        ArrayList params = new ArrayList();
        params.add(atmfinancialMessage.getAccount());
        params.add(getTransactionReference(atmfinancialMessage));
        params.add(Boolean.FALSE);
        try {
            IBOBlockingTransactions transactioDetails = (IBOBlockingTransactions) env.getFactory().findFirstByQuery(
                    IBOBlockingTransactions.BONAME, findBlockingTransQuerry, params);
            blockedAmount = transactioDetails.getF_AMOUNT().subtract(transactioDetails.getF_UNBLOCKEDAMOUNT());
            if (blockedAmount.compareTo(BigDecimal.ZERO) > 0) {
                result = true;
            }
            else {
                result = false;
            }
        }
        catch (BankFusionException exception) {
            result = false;
        }
        return result;
    }

    /**
     * this method is used to look for blocking transaction
     * 
     * @param atmfinancialMessage
     * @param env
     * @return
     */
    private boolean isBlockingTransaction(ATMSparrowFinancialMessage atmfinancialMessage, BankFusionEnvironment env) {
        boolean result = true;
        ArrayList params = new ArrayList();
        params.add(getTransactionReference(atmfinancialMessage));
        params.add("Z");
        try {
            List list = env.getFactory().findByQuery(IBOTransaction.BONAME, QUERRY_FIND_BLOCKING_TRANS, params, null);
            if (list.size() > 0) {
                result = true;
            }
            else {
                result = false;

            }
        }
        catch (BankFusionException exception) {
            result = true;
        }
        return result;
    }
}
