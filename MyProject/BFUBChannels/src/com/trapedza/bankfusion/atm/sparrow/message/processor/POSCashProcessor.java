/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: POSCashProcessor.java,v $
 * 
 * Revision 1.5  2008/11/20 09:40:37  bhavyag
 * updated for bug 14513
 *
 * Revision 1.3  2008/11/01 14:33:03  bhavyag
 * updated during the fix of bug13963.
 *
 * Revision 1.2  2008/10/16 11:26:56  bhavyag
 * Added a condition for setting the isfees and is commission flags.
 *
 * Revision 1.1  2008/10/10 06:07:49  debjitb
 * updated version after added Amount4
 *
 * Revision 1.12  2008/08/12 20:15:03  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.10.4.5  2008/08/08 22:40:26  sushmax
 * Bug fix for 11659
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
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMExNwMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardIssuersDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOATMPOSBLOCKINGCONF;
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
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AddDaysToDate;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class POSCashProcessor extends ATMFinancialProcessor {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(POSCashProcessor.class.getName());

    private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";

    private static final String ATMTransactionQuery = "WHERE " + IBOATMTransactionCodes.BONAME + "=?";

    private static final String posCompletionWhereClause = "WHERE " + IBOTransaction.REFERENCE + " = ? and " + IBOTransaction.TYPE
            + " = ?";

    private static final String findBlockingTransQuerry = "WHERE " + IBOBlockingTransactions.ACCOUNTID + "=?" + " AND "
            + IBOBlockingTransactions.BLOCKINGREFERENCE + "=?" + " AND " + IBOBlockingTransactions.UNBLOCKING + " = ?";

    private static final String getPOSBlockingDetails = "where " + IBOATMPOSBLOCKINGCONF.IMDCODE + "= ?" + " AND "
            + IBOATMPOSBLOCKINGCONF.MISTRANSACTIONCODE + "=?";

    private static final String getCardIssuerDetails = "where " + IBOATMCardIssuersDetail.IMDCODE + "= ?" + " AND "
            + IBOATMCardIssuersDetail.ISOCOUNTRYCODE + "=?";

    private static final String FindBytransactionCode = "WHERE " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + "=?";

    private static final String getNetworkID = "where " + IBOExternalLoroSettlementAccount.ID + " = ?";
    private static final String External_Sale = "610";
    private static final String External_CASHBACK = "611";
    private static final String External_QUASICASH = "612";
    private static final String External_POSCASH = "613";

    private static final String MERCHANT_DEBIT_TRANSACTION = "626";
    private static final String MERCHANT_CREDIT_TRANSACTION = "625";
    private static final String SMART_CARD_FUNDS_TRANSFER = "627";
    /**
     * Final variable for Message 623
     */
    private static final String POS_REFUND = "623";
    /**
     * Final variable for Message 614
     */
    private static final String EXTERNAL_POS_REFUND = "614";

    ATMControlDetails controlDetails = null;
    Integer sharedSwitch = null;
    String mainAccount = CommonConstants.EMPTY_STRING;
    String atmBaseCurency = null;
    String contraAccount = null;
    boolean isPosTransacion = false;
    boolean isMessageSale = false;
    String transReference = CommonConstants.EMPTY_STRING;
    String transactionType = CommonConstants.EMPTY_STRING;
    boolean doesBlockingTransactionExist = false;
    ATMHelper atmHelper = new ATMHelper();
    String transactionCode = CommonConstants.EMPTY_STRING;
    String customerTransactionNarration = CommonConstants.EMPTY_STRING;
    String contraTransactionNarration = CommonConstants.EMPTY_STRING;
    BigDecimal blockedAmount = CommonConstants.BIGDECIMAL_ZERO;
    // Charges & commissions starts
    boolean isFees = false;
    boolean isCommission = false;
    boolean ifDispenseCurrencyToBeUsed = false;

    // Charges & commissions ends

    /*
     * This Class will be called for all POS Messages.
     */
    public void execute(ATMSparrowMessage atmMessage, BankFusionEnvironment env) {
        ATMSparrowFinancialMessage financialMessage = (ATMSparrowFinancialMessage) atmMessage;
        // This is Done so that We do not face errors if Sparrow sends the
        // Authorized flag as 1.
        /*
         * The dispenseCurrency should not be used if the message is external network and the
         * message type is 610 ,611 , 612 , 613 , 614 , 620 and 680
         */
        transactionType = financialMessage.getMessageType() + financialMessage.getTransactionType();
        if (transactionType.equals("621") || transactionType.equals("622") || transactionType.equals("623")
                || transactionType.equals("624") || transactionType.equals("629") || transactionType.equals("625")
                || transactionType.equals("626"))

            ifDispenseCurrencyToBeUsed = true;
        else ifDispenseCurrencyToBeUsed = false;

        controlDetails = ATMConfigCache.getInstance().getInformation(env);
        financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
        ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
        boolean isForcepostValid = isForcePostValid(financialMessage);
        if (!isForcepostValid) {
            return;
        }
        isPosTransacion = isPosTransaction(financialMessage);
        // transactionType = financialMessage.getMessageType() +
        // financialMessage.getTransactionType();
        transReference = getTransactionReference(financialMessage);

        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        if ((financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)
                || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5))) {
            doesBlockingTransactionExist = checkForPreAuthorization(financialMessage, env);
        }
        mainAccount = getMainAccount(financialMessage, env);// changed for bug artf33456
        if (mainAccount == "") {
            return;
        }
        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        validateTransaction(financialMessage, env);
        // artf45119, artf45120 changes start
        // artf46925 changes Isaccountmappedtocard added
        if (((financialMessage.getMessageType() + financialMessage.getTransactionType()).equals(MERCHANT_CREDIT_TRANSACTION)
                || (financialMessage.getMessageType() + financialMessage.getTransactionType()).equals(SMART_CARD_FUNDS_TRANSFER))
                && (!atmMessageValidator.isAccountMappedToCard(financialMessage.getCardNumber(), financialMessage.getAccount(),
                        env)))
            mainAccount = financialMessage.getAccount();
        // artf45119, artf45120 changes end
        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Check for Currency Validations Only if the account is Valid. ie: the
        // account sent is the mainAccount.
        if (financialMessage.getAccount().equals(mainAccount)) {
            boolean isCurrencyValid = atmMessageValidator.isCurrencyValid(financialMessage.getAccount(),
                    financialMessage.getCurrencySourceAccount(), env);
            if (!isCurrencyValid) {
                if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                        || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                    financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    financialMessage.setErrorCode("Warning");
                    financialMessage.setErrorDescription("Invalid Currency for Account");
                }
                else if (atmMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                        || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                        || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                        || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                    String errorMessge = "Invalid Currency for Account. Force Post Not Posted";
                    financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    financialMessage.setErrorCode(ATMConstants.CRITICAL);
                    financialMessage.setErrorDescription(errorMessge);

                }
                return;
            }
        }
        if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
            performBlocking(financialMessage, env);
            return;
        }
        if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
            if (doesBlockingTransactionExist) {
                String errorMessage = CommonConstants.EMPTY_STRING;
                errorMessage = "Transaction Already Posted.";
                financialMessage.setErrorCode(ATMConstants.INFORMATION);
                financialMessage.setErrorDescription(errorMessage);
                logger.info(errorMessage);
                financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                return;
            }
            performBlocking(financialMessage, env);
            return;
        }
        contraAccount = getContraAccount(financialMessage, env);
        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
            postTransactions(financialMessage, env);
            if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                return;
            }
            financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
            return;
        }
        if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
            boolean isTransactionAlreadyPosted = checkForDuplicates(financialMessage, env);
            if (!isTransactionAlreadyPosted) {
                postTransactions(financialMessage, env);
                if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                    return;
                }
                financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
            }
            return;
        }
        if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
            boolean isCompletionMessagePosted = isTransactionCompleted(env);
            if (isCompletionMessagePosted) {
                financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                financialMessage.setErrorCode(ATMConstants.INFORMATION);
                financialMessage.setErrorDescription("Completion Message Already Posted");
                return;
            }
        }
        if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
            if (doesBlockingTransactionExist) {
                isCompletionAmountWithinGivenRange(financialMessage, env);
                performUnBlocking(financialMessage, env);
            }
            postTransactions(financialMessage, env);
        }

        if (atmMessage.getMessageType().equals(625) || atmMessage.getMessageType().equals(626))
            isFees = true;
        else isCommission = true;

    }

    /**
     * @returns void
     * 
     *          This function will call a microflow which will in turn post the transactions.
     */
    protected void postTransactions(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
        HashMap map = new HashMap();
        String accountCurrencyCode = CommonConstants.EMPTY_STRING;
        String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;

        try {
            if (ifDispenseCurrencyToBeUsed) {
                dispensedCurrencyCode = SystemInformationManager.getInstance()
                        .transformCurrencyCode(message.getCurrencyDestDispensed(), true);
            }
            accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(message.getCurrencySourceAccount(),
                    true);
            if (message.getVariableDataType().equals("E")) {
                dispensedCurrencyCode = accountCurrencyCode;
            }
            else {
                dispensedCurrencyCode = SystemInformationManager.getInstance()
                        .transformCurrencyCode(message.getCurrencyDestDispensed(), true);
            }
        }
        catch (BankFusionException exception) {

        }
        map.put("TRANSACTIONSOURCE", "POS");
        map.put("ACCOUNT1_ACCOUNTID", mainAccount);
        map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
        map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
        map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
        map.put("ACCOUNT1_TRANSCODE", transactionCode);

        map.put("ACCOUNT2_ACCOUNTID", contraAccount);
        if (message.getVariableDataType().equals("E")) {
            map.put("ACCOUNT2_AMOUNT", message.getAmount1().abs());
        }
        else {
            map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
        }
        if (ifDispenseCurrencyToBeUsed) {
            map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
            map.put("ACCOUNT2_AMOUNT_CurrCode", dispensedCurrencyCode);
        }
        else {
            map.put("ACCOUNT2_AMOUNT", message.getAmount1().abs());
            map.put("ACCOUNT2_AMOUNT_CurrCode", accountCurrencyCode);

        }
        map.put("ACCOUNT2_NARRATIVE", contraTransactionNarration);
        map.put("ACCOUNT2_TRANSCODE", transactionCode);
        map.put("AMOUNT4", message.getAmount4().abs());
        map.put("MAINACCOUNTID", message.getAccount());
        map.put("MESSAGENUMBER", transactionType);
        String messagenumber = message.getMessageType() + message.getTransactionType();
        if (messagenumber.equals("620") || messagenumber.equals("680")) { // Updated for artf34792.
            map.put("CHANNELID", "ATM");
        }
        else {
            map.put("CHANNELID", "POS");
        }
        // Check if the Transaction type is not 614 && 623 && 625 then set
        // Account1 as "C" Account2 as "D"
        if (!(MERCHANT_CREDIT_TRANSACTION.equals(transactionType)) && !(transactionType.equals(EXTERNAL_POS_REFUND))
                && !(transactionType.equals(POS_REFUND))) {
            map.put("ACCOUNT1_POSTINGACTION", "D");
            map.put("ACCOUNT2_POSTINGACTION", "C");
        }
        else {
            map.put("ACCOUNT1_POSTINGACTION", "C");
            map.put("ACCOUNT2_POSTINGACTION", "D");

        }
        // External branch code changes starts
        if ((transactionType.equals("610")) || (transactionType.equals("611")) || (transactionType.equals("612"))
                || (transactionType.equals("613")) || (transactionType.equals("614")) || (transactionType.equals("620"))
                || (transactionType.equals("680"))) {
            map.put("EXTERNAL_BRANCH_CODE", controlDetails.getEXTERNAL_BRANCH_CODE());
        }
        // External branch code changes ends
        // Start: Fix for artf949632

        // map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
        // map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
        map.put("MANUALVALUEDATE", atmHelper.checkForwardValuedTime(message));
        map.put("MANUALVALUETIME", new Time(atmHelper.checkForwardValuedTime(message).getTime()));

        // End: Fix for artf949632

        map.put("TRANSACTIONREFERENCE", getTransactionReference(message));

        if (ATMConstants.FORCEPOST_0.equals(message.getForcePost()) || ATMConstants.FORCEPOST_6.equals(message.getForcePost())) {
            map.put("FORCEPOST", new Boolean(false));
        }
        else {
            map.put("FORCEPOST", new Boolean(true));
        }
        map.put("CARDNUMBER", message.getCardNumber());
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
                    env.getFactory().beginTransaction(); //
                }
                catch (Exception ignored) {
                }
                return;
            }
            env.getFactory().commitTransaction();
            env.getFactory().beginTransaction(); //
        }
        catch (BankFusionException exception) {
            logger.info(exception.getMessage());
            message.setErrorCode(ATMConstants.ERROR);
            message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            message.setErrorDescription(exception.getMessage());
            try {
                env.getFactory().rollbackTransaction();
                env.getFactory().beginTransaction(); //
            }
            catch (Exception ignored) {
                logger.info("Failed to rollback the transaction");
            }
        }
        finally {
            try {
                env.getFactory().beginTransaction();
            }
            catch (Exception ignored) {
                logger.info("Failed to begin the transaction");
            }
        }
    }

    protected void performBlocking(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
        BigDecimal amount = atmPosMessage.getAmount1().abs();
        BigDecimal availableBalance = atmHelper.getAvailableBalance(atmPosMessage.getAccount(), env);
        String message = CommonConstants.EMPTY_STRING;
        Boolean forcePost = new Boolean(false);
        if (amount.equals(CommonConstants.BIGDECIMAL_ZERO)) {
            message = "Zero Amount Blocking Not Allowed";
            atmPosMessage.setErrorCode(ATMConstants.WARNING);
            atmPosMessage.setErrorDescription(message);
            atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            return;
        }
        if ((availableBalance.compareTo(amount) < 0) && !atmPosMessage.getForcePost().equals("7")) {
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
        HashMap paramsforTellerBlocking = new HashMap();
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
        paramsforTellerBlocking.put("CHANNELID", "POS");
        if (atmPosMessage.getForcePost().equals("7")) {
            forcePost = new Boolean(true);
        }
        paramsforTellerBlocking.put("FORCEPOST", forcePost);

        try {
            MFExecuter.executeMF(ATMConstants.BLOCKING_TRANSACTION, env, paramsforTellerBlocking);
            env.getFactory().commitTransaction();
            env.getFactory().beginTransaction();//
        }
        catch (BankFusionException exception) {
            message = exception.getMessage();
            atmPosMessage.setErrorCode(ATMConstants.WARNING);
            atmPosMessage.setErrorDescription(message);
            atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            try {
                env.getFactory().rollbackTransaction();
                env.getFactory().beginTransaction();//
            }
            catch (Exception ignored) {
                logger.info("Failed to rollback the transaction");
            }
            return;
        }
        finally {
            try {
                env.getFactory().beginTransaction();
            }
            catch (Exception ignored) {
                logger.info("Failed to begin the transaction");
            }
        }
    }

    protected Timestamp getUnBlockingDate(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
        int unBlockingDays = 0;
        Date unBlockingDate = null;
        ArrayList params = new ArrayList();
        ArrayList params1 = new ArrayList();
        ArrayList params2 = new ArrayList();
        params.add(atmPosMessage.getCardDestinationIMD());
        String ATMTransCode = atmPosMessage.getMessageType() + atmPosMessage.getTransactionType();

        params2.add(ATMTransCode);
        IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes) env.getFactory()
                .findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params2, false);
        // SimplePersistentObject ATMTransactionCodesList =
        // env.getFactory().findByPrimaryKey(IBOATMTransactionCodes.BONAME, ATMTransCode);

        IBOATMTransactionCodes misTransCodes = (IBOATMTransactionCodes) atmTransactionCodes;
        params.add(misTransCodes.getF_MISTRANSACTIONCODE());

        /*
         * Removed this line as part of Issue artf321287
         */

        List cardIssuersList = env.getFactory().findByQuery(IBOATMPOSBLOCKINGCONF.BONAME, getPOSBlockingDetails, params, null);
        if (cardIssuersList.size() == 1) {
            IBOATMPOSBLOCKINGCONF cardIssuersDetails = (IBOATMPOSBLOCKINGCONF) cardIssuersList.get(0);
            unBlockingDays = cardIssuersDetails.getF_BLOCKINGPERIOD();
            if (unBlockingDays == 0) {
                unBlockingDays = ATMConfigCache.getInstance().getInformation(env).getDefaultBlockingPeriod().intValue();
            }
        }
        else {
            unBlockingDays = ATMConfigCache.getInstance().getInformation(env).getDefaultBlockingPeriod().intValue();
        }
        unBlockingDate = AddDaysToDate.run(atmPosMessage.getDateTimeofTxn(), unBlockingDays);
        Timestamp unBlockingTimestamp = new Timestamp(unBlockingDate.getTime());
        return unBlockingTimestamp;
    }

    protected void validateTransaction(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
        boolean isAccountMappedtoCard = false;
        ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
        if (transactionType.equals(POS_REFUND)) {
            atmMessageValidator.validateMessage(atmPosMessage, env, "local");
        }
        else {
            atmMessageValidator.validateMessage(atmPosMessage, env, "external");
        }

        if (atmPosMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))
            return;

        if (!(MERCHANT_CREDIT_TRANSACTION.equals(transactionType) || MERCHANT_DEBIT_TRANSACTION.equals(transactionType))) {
            isAccountMappedtoCard = atmMessageValidator.isAccountMappedToCard(atmPosMessage.getCardNumber(),
                    atmPosMessage.getAccount(), env);
            if (!isAccountMappedtoCard) {
                if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                        || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)
                        || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                    atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    atmPosMessage.setErrorCode(ATMConstants.WARNING);
                    atmPosMessage.setErrorDescription("Card Not Mapped to Account");
                    return;
                }

                if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                        || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                        || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                        || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)) {
                    String pseudoName = CommonConstants.EMPTY_STRING;
                    pseudoName = controlDetails.getPosDrSuspenseAccount();
                    mainAccount = atmHelper.getAccountIDfromPseudoName(pseudoName, atmPosMessage.getCurrencySourceAccount(), null,
                            env);
                    String errorMessage = BankFusionMessages.getFormattedMessage(
                            ChannelsEventCodes.W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC,
                            new Object[] { atmPosMessage.getCardNumber(), atmPosMessage.getAccount() });
                    atmPosMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                    atmPosMessage.setErrorCode("Warning");
                    atmPosMessage.setErrorDescription(errorMessage);
                }
            }
        }
        transactionCode = atmHelper.getBankTransactionCode(atmPosMessage.getMessageType() + atmPosMessage.getTransactionType(),
                env);
        atmHelper.updateTransactionNarration(atmPosMessage, env);
        customerTransactionNarration = atmPosMessage.getTxnCustomerNarrative();
        contraTransactionNarration = atmPosMessage.getTxnContraNarrative();
        if (transactionCode.equals(CommonConstants.EMPTY_STRING)) {
            if (atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmPosMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                String errorMessage = "Transaction Not Mapped";
                atmPosMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                atmPosMessage.setErrorCode(ATMConstants.WARNING);
                atmPosMessage.setErrorDescription(errorMessage);
                logger.error(errorMessage);
                return;
            }
            else {
                String errorMessage = "Transaction Not Mapped. Using Default Transaction Type";
                atmPosMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                atmPosMessage.setErrorCode(ATMConstants.ERROR);
                atmPosMessage.setErrorDescription(errorMessage);
                logger.error(errorMessage);
                transactionCode = controlDetails.getPosTxnType();
            }
        }
    }

    /*
     * To Check for Pre Authorization.
     */
    private boolean checkForPreAuthorization(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
        boolean result = false;
        ArrayList params = new ArrayList();
        params.add(atmPosMessage.getAccount());
        params.add(transReference);
        params.add(new Boolean(false));
        try {
            IBOBlockingTransactions transactioDetails = (IBOBlockingTransactions) env.getFactory()
                    .findFirstByQuery(IBOBlockingTransactions.BONAME, findBlockingTransQuerry, params);
            blockedAmount = transactioDetails.getF_AMOUNT().subtract(transactioDetails.getF_UNBLOCKEDAMOUNT());
            if (blockedAmount.compareTo(new BigDecimal(0)) > 0) {
                result = true;
            }
            else {
                result = false;
            }
        }
        catch (Exception exception) {
            result = false;
        }
        return result;
    }

    /*
     * This function will get The Cardholders Account. if the Card holders account does not exist we
     * will get The Network Dr Suspense account if the force post is 1 or 7.
     */
    protected String getMainAccount(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env) {
        ATMControlDetails atmControlDetails = ATMConfigCache.getInstance().getInformation(env);
        ATMMessageValidator messageValidator = new ATMMessageValidator();
        String debitAccount = CommonConstants.EMPTY_STRING;
        String accountNumber = financialMessage.getAccount();
        String forcePost = financialMessage.getForcePost();
        // changes for Account closed starts
        Object[] field = new Object[] { financialMessage.getAccount() };
        IBOAttributeCollectionFeature accountItem = null;
        try {
            accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
                    financialMessage.getAccount());
            accountNumber = financialMessage.getAccount();
        }
        catch (FinderException fe) {

        }

        BusinessValidatorBean validatorBean = new BusinessValidatorBean();
        if (accountItem != null) {
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        CommonsEventCodes.E_ACCOUNT_CLOSED, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
                if (logger.isDebugEnabled()) {
                    logger.error("Account : " + financialMessage.getAccount() + " is Closed !");
                }
                accountNumber = "";
                return accountNumber;
            }

        }
        // changes for Account closed ends
        if (validatorBean.validateAccountStopped(accountItem, env)) {
            if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                /*
                 * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                 * ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL, atmLocalMessage,
                 * field, env);
                 */
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        CommonsEventCodes.E_ACCOUNT_STOPPED, BankFusionMessages.ERROR_LEVEL, financialMessage, field, env);
                debitAccount = "";
                return debitAccount;
            }
            else if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)) {
                /*
                 * populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                 * 7517, BankFusionMessages.MESSAGE_LEVEL, atmLocalMessage, field, env);
                 */
                populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED, BankFusionMessages.MESSAGE_LEVEL,
                        financialMessage, field, env);
                if (controlDetails != null) {
                    String pName = CommonConstants.EMPTY_STRING;
                    if (isPosTransacion) {

                        if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
                            pName = atmControlDetails.getPosCrSuspenseAccount();
                        }
                        else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
                            pName = atmControlDetails.getPosDrSuspenseAccount();
                            // Added check for POS refund
                        }
                        else if (transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND)) {
                            pName = atmControlDetails.getCardHolderSuspenseAccount();
                        }
                        else {
                            pName = atmControlDetails.getPosDrSuspenseAccount();
                        }

                    }
                    else {
                        pName = atmControlDetails.getNetworkDrSuspenseAccount();
                    }
                    if (financialMessage.getVariableDataType().equals("E")) {
                        debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(),
                                null, env);
                    }
                    else {
                        debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(),
                                null, env);
                    }
                    // debitAccount = atmHelper.getAccountIDfromPseudoName(pName,
                    // financialMessage.getCurrencySourceAccount(),
                    // CommonConstants.EMPTY_STRING, env);
                    return debitAccount;
                }
            }
            logger.error("Account : " + financialMessage.getAccount() + " is Stopped !");
        }
        int optionDebitCredit;
        if (isPosTransacion) {
            if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
                optionDebitCredit = PasswordProtectedConstants.OPERATION_CREDIT;
            }
            else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
                optionDebitCredit = PasswordProtectedConstants.OPERATION_DEBIT;
            }
            else if (transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND)) {
                optionDebitCredit = PasswordProtectedConstants.OPERATION_CREDIT;
            }
            else {
                optionDebitCredit = PasswordProtectedConstants.OPERATION_DEBIT;
            }

        }
        else {
            optionDebitCredit = PasswordProtectedConstants.OPERATION_DEBIT;
        }
        boolean isAccountValid = atmHelper.isAccountValid(financialMessage, optionDebitCredit, env);
        if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            debitAccount = financialMessage.getAccount();
        }
        if (!isAccountValid) {
            financialMessage.setErrorCode(ATMConstants.CRITICAL);
            financialMessage.setErrorDescription("Invalid Main Account Suspense Account will be Updated");
            String pName = CommonConstants.EMPTY_STRING;
            if (isPosTransacion) {

                if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
                    pName = atmControlDetails.getPosCrSuspenseAccount();
                }
                else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
                    pName = atmControlDetails.getPosDrSuspenseAccount();
                    // Added check for POS refund
                }
                else if (transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND)) {
                    pName = atmControlDetails.getCardHolderSuspenseAccount();
                }
                else {
                    pName = atmControlDetails.getPosDrSuspenseAccount();
                }

            }
            else {
                pName = atmControlDetails.getNetworkDrSuspenseAccount();
            }

            if (financialMessage.getVariableDataType().equals("E")) {
                debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null, env);
            }
            else {
                debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null, env);
            }
            // debitAccount = atmHelper.getAccountIDfromPseudoName(pName,
            // financialMessage.getCurrencySourceAccount(),
            // CommonConstants.EMPTY_STRING, env);
            return debitAccount;
        }
        boolean isPasswordProtected = atmHelper.isAccountPasswordProtected(financialMessage, optionDebitCredit,
                ATMConstants.SOURCEACCOUNTTYPE, env);
        if (isPasswordProtected) {
            debitAccount = financialMessage.getAccount();
        }
        if (!isPasswordProtected) {
            if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.ERROR,
                        ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, BankFusionMessages.ERROR_LEVEL, financialMessage,
                        field, env);
            }
            else if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)) {
                populateErrorDetails(ATMConstants.AUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED, BankFusionMessages.ERROR_LEVEL,
                        financialMessage, field, env);
                String pName = CommonConstants.EMPTY_STRING;
                if (isPosTransacion) {

                    if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
                        pName = atmControlDetails.getPosCrSuspenseAccount();
                    }
                    else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
                        pName = atmControlDetails.getPosDrSuspenseAccount();
                        // Added check for POS refund
                    }
                    else if (transactionType.equals(EXTERNAL_POS_REFUND) || transactionType.equals(POS_REFUND)) {
                        pName = atmControlDetails.getCardHolderSuspenseAccount();
                    }
                    else {
                        pName = atmControlDetails.getPosDrSuspenseAccount();
                    }

                }
                else {
                    pName = atmControlDetails.getNetworkDrSuspenseAccount();
                }
                // debitAccount = atmHelper.getAccountIDfromPseudoName(pName,
                // financialMessage.getCurrencySourceAccount(),
                // CommonConstants.EMPTY_STRING, env);
                if (financialMessage.getVariableDataType().equals("E")) {
                    debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null,
                            env);
                }
                else {
                    debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null,
                            env);
                }
                return debitAccount;

            }
        }
        if (!doesBlockingTransactionExist) {
            if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)) {
                String errorMessage = "Pre Authorization Does Not Exist";
                financialMessage.setErrorCode(ATMConstants.WARNING);
                financialMessage.setErrorDescription(errorMessage);
                logger.warn(errorMessage);
            }
            else if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                String errorMessage = "Pre Auth Does Not Exist. Posting to Suspense Account";
                financialMessage.setErrorCode(ATMConstants.CRITICAL);
                financialMessage.setErrorDescription(errorMessage);
                logger.warn(errorMessage);
                String pName = CommonConstants.EMPTY_STRING;
                if (isPosTransacion) {
                    if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION)) {
                        pName = atmControlDetails.getPosCrSuspenseAccount();
                    }
                    else if (transactionType.equals(MERCHANT_CREDIT_TRANSACTION)) {
                        pName = atmControlDetails.getPosDrSuspenseAccount();
                    }
                    else {
                        pName = atmControlDetails.getPosDrSuspenseAccount();
                    }
                }
                else {
                    pName = atmControlDetails.getNetworkDrSuspenseAccount();
                }
                // debitAccount = atmHelper.getAccountIDfromPseudoName(pName,
                // financialMessage.getCurrencySourceAccount(), null, env);
                if (financialMessage.getVariableDataType().equals("E")) {
                    debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null,
                            env);
                }
                else {
                    debitAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null,
                            env);
                }
            }
        }
        return debitAccount;

    }

    /*
     * @ retuns TransactionReference. returns the transaction Reference based on the Source CIB +
     * DeviceID + DateTimeofTransaction + TransctionSequenceNumber
     */
    protected String getTransactionReference(ATMSparrowFinancialMessage financialMessage) {
        return atmHelper.getTransactionReference(financialMessage);
    }

    /*
     * @returns boolean. Checks if the Transaction has already been posted based on the transaction
     * Reference.
     */
    private boolean checkForDuplicates(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env) {
        boolean isDuplicate = false;
        ArrayList params = new ArrayList();
        List transactionDetails = null;
        // params.add(atmPosMessage.getAccount());
        params.add(getTransactionReference(financialMessage));

        // finds original transaction
        transactionDetails = env.getFactory().findByQuery(IBOTransaction.BONAME, txnHistoryWhereClause, params, null);
        if (transactionDetails.size() > 0 && (financialMessage.getForcePost().equals("3"))) {
            isDuplicate = true;
            financialMessage.setErrorCode(ATMConstants.INFORMATION);
            financialMessage.setErrorDescription(
                    "Transaction already Posted" + financialMessage.getAccount() + " " + getTransactionReference(financialMessage));
            financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
        }
        return isDuplicate;
    }

    private boolean isPosTransaction(ATMSparrowFinancialMessage message) {
        String messageType = message.getVariableDataType();
        String transactionType = message.getMessageType() + message.getTransactionType();
        if (("P").equalsIgnoreCase(messageType)
                || (transactionType.equals(EXTERNAL_POS_REFUND) && (!message.getForcePost().equals(ATMConstants.FORCEPOST_5)))) {
            return true;
        }
        else {
            return false;
        }

    }

    protected String getContraAccount(ATMSparrowFinancialMessage financialMessage, BankFusionEnvironment env) {
        String contraAccount = CommonConstants.EMPTY_STRING;
        String pName = CommonConstants.EMPTY_STRING;
        ATMControlDetails atmControlDetails = ATMConfigCache.getInstance().getInformation(env);
        if (financialMessage.getVariableDataType().equals("E")
                && (transactionType.equals(External_Sale) || transactionType.equals(External_CASHBACK)
                        || transactionType.equals(External_QUASICASH) || transactionType.equals(External_POSCASH))
                || transactionType.equals(EXTERNAL_POS_REFUND)) {
            ATMExNwMessage message = (ATMExNwMessage) financialMessage;
            pName = getExternalNetworkSuspenceAccount(message.getExternalNetworkID(), financialMessage.getSourceCountryCode(),
                    financialMessage.getSourceIMD(), env);

        }
        else if (isPosTransacion) {
            pName = atmControlDetails.getPosHoldingAccount();

        }
        else {
            ATMExNwMessage message = (ATMExNwMessage) financialMessage;
            pName = getExternalNetworkSuspenceAccount(message.getExternalNetworkID(), financialMessage.getSourceCountryCode(),
                    financialMessage.getSourceIMD(), env);
        }
        if (ifDispenseCurrencyToBeUsed) {
            contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null, env);
        }
        else {
            contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null, env);
        }
        if (contraAccount.equals(CommonConstants.EMPTY_STRING)
                && financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
            financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            financialMessage.setErrorCode(ATMConstants.CRITICAL);
            financialMessage.setErrorDescription("Invalid POSHOLD account");
        }
        if (!contraAccount.equals(CommonConstants.EMPTY_STRING)) {

            return contraAccount;
        }
        pName = CommonConstants.EMPTY_STRING;
        if (financialMessage.getVariableDataType().equals("E")
                && (transactionType.equals(External_Sale) || transactionType.equals(External_CASHBACK)
                        || transactionType.equals(External_QUASICASH) || transactionType.equals(External_POSCASH))
                || transactionType.equals(EXTERNAL_POS_REFUND)) {
            pName = atmControlDetails.getNetworkCrSuspenseAccount();
        }
        else if (isPosTransacion) {
            // Added check for POS refund
            if (transactionType.equals(MERCHANT_DEBIT_TRANSACTION) || transactionType.equals(EXTERNAL_POS_REFUND)
                    || transactionType.equals(POS_REFUND)) {
                pName = atmControlDetails.getPosDrSuspenseAccount();
            }
            else {
                pName = atmControlDetails.getPosCrSuspenseAccount();
            }
        }
        else {
            pName = atmControlDetails.getNetworkCrSuspenseAccount();
        }
        if (ifDispenseCurrencyToBeUsed) {
            contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null, env);
        }
        else {
            contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null, env);
        }
        if (financialMessage.getVariableDataType().equals("E"))
            contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencySourceAccount(), null, env);
        else contraAccount = atmHelper.getAccountIDfromPseudoName(pName, financialMessage.getCurrencyDestDispensed(), null, env);

        return contraAccount;
    }

    protected String getExternalNetworkSuspenceAccount(String ntWorkId, String sourceCountry, String sourceIMD,
            BankFusionEnvironment env) {
        String pName = CommonConstants.EMPTY_STRING;
        ArrayList params = new ArrayList();
        params.add(ntWorkId);
        try {
            IBOExternalLoroSettlementAccount settlementDetails = (IBOExternalLoroSettlementAccount) env.getFactory()
                    .findFirstByQuery(IBOExternalLoroSettlementAccount.BONAME, getNetworkID, params, false);
            pName = settlementDetails.getF_SETTLEMENTACCOUNT();
        }
        catch (BankFusionException exception) {
            pName = CommonConstants.EMPTY_STRING;
        }
        if (!(pName.equals(CommonConstants.EMPTY_STRING))) {
            return pName;
        }
        params = new ArrayList();
        params.add(sourceIMD);
        params.add(sourceCountry);
        try {
            IBOATMCardIssuersDetail cardIssuerDetails = (IBOATMCardIssuersDetail) env.getFactory()
                    .findFirstByQuery(IBOATMCardIssuersDetail.BONAME, getCardIssuerDetails, params, false);
            pName = cardIssuerDetails.getF_SETTLEMENTACCOUNT();
        }
        catch (BankFusionException exception) {
            pName = CommonConstants.EMPTY_STRING;
        }
        return pName;
    }

    /**
     * @retuns boolean. Checks if The completion Amount is within the range provided.
     */
    private boolean isCompletionAmountWithinGivenRange(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
        boolean result = false;
        double percentageAllowed = controlDetails.getAuthAllowedPercentage().doubleValue();
        IBOBlockingTransactions transactioDetails = null;
        ArrayList params = new ArrayList();
        params.add(message.getAccount());
        params.add(getTransactionReference(message));
        params.add(new Boolean(false));
        try {

            transactioDetails = (IBOBlockingTransactions) env.getFactory().findFirstByQuery(IBOBlockingTransactions.BONAME,
                    findBlockingTransQuerry, params);
            BigDecimal blockedAmount = transactioDetails.getF_AMOUNT();
            logger.info("Blocked Amount: " + blockedAmount);
            BigDecimal lowerRange = new BigDecimal("0");
            BigDecimal upperRange = new BigDecimal("0");
            double lowerRangePercentage = (100 - percentageAllowed) / 100.00;
            double upperRangePercentage = (100 + percentageAllowed) / 100.00;
            lowerRange = blockedAmount.multiply(new BigDecimal(lowerRangePercentage));
            upperRange = blockedAmount.multiply(new BigDecimal(upperRangePercentage));
            BigDecimal amount = message.getAmount1().abs();
            if ((amount.compareTo(lowerRange) > 0) && (amount.compareTo(upperRange) < 0)) {
                result = true;
            }
            else {
                result = false;
                String errorMessage = "Amount Given is not Within the Range";
                message.setErrorCode(ATMConstants.WARNING);
                message.setErrorDescription(errorMessage);
                logger.info(errorMessage);
            }
        }
        catch (BankFusionException exception) {
            result = false;
        }
        return result;
    }

    /*
     * This performs unblocking for all Force Post 5 & 8.
     */
    private void performUnBlocking(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
        String blockingCategory = "ATM";
        String transCode = atmHelper.getBankTransactionCode(transactionType, env);
        Date postingDate = SystemInformationManager.getInstance().getBFBusinessDate();
        Timestamp unBlockingDateTime = getUnBlockingDate(atmPosMessage, env);
        HashMap paramsforTellerBlocking = new HashMap();
        paramsforTellerBlocking.put("ACCOUNTID", mainAccount);
        paramsforTellerBlocking.put("AMOUNT", blockedAmount);
        paramsforTellerBlocking.put("BLOCKINGCATEGORY", blockingCategory);
        paramsforTellerBlocking.put("NARRATIVE", customerTransactionNarration);
        paramsforTellerBlocking.put("POSTINGDATE", postingDate);
        paramsforTellerBlocking.put("TRANSACTIONCODE", transCode);
        paramsforTellerBlocking.put("TRANSACTIONREFERENCE", transReference);
        paramsforTellerBlocking.put("UNBLOCKINGDATETIME", unBlockingDateTime);
        paramsforTellerBlocking.put("ISBLOCKING", new Boolean(false));
        paramsforTellerBlocking.put("POSTINGACTION", "C");
        paramsforTellerBlocking.put("CHANNELID", "POS");
        try {
            MFExecuter.executeMF(ATMConstants.BLOCKING_TRANSACTION, env, paramsforTellerBlocking);
            env.getFactory().commitTransaction();
            env.getFactory().beginTransaction();//
        }
        catch (BankFusionException exception) {
            String message = exception.getMessage();
            logger.error(message);
            atmPosMessage.setErrorCode(ATMConstants.CRITICAL);
            atmPosMessage.setErrorDescription(message);
            try {
                env.getFactory().rollbackTransaction();
                env.getFactory().beginTransaction();//
            }
            catch (Exception ignored) {
                logger.info("Failed to rollback the transaction");
            }
            return;
        }
        finally {
            try {
                env.getFactory().beginTransaction();
            }
            catch (Exception ignored) {
                logger.info("Failed to begin the transaction");
            }
        }
    }

    /**
     * @ returns boolean. This Function validates if the Given force Post is valid for POS
     * Transactions.
     */
    private boolean isForcePostValid(ATMSparrowFinancialMessage financialMessage) {
        boolean isForcePostValid = true;
        if (financialMessage.getForcePost().equals("2") || financialMessage.getForcePost().equals("4")
                || financialMessage.getForcePost().equals("9")) {
            isForcePostValid = false;
            financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            financialMessage.setErrorCode(ATMConstants.CRITICAL);
            financialMessage.setErrorDescription("Invalid Force Post Values");
        }
        return isForcePostValid;
    }

    /**
     * @ returns boolean. This Function validates if the Completion Message has already been posted.
     * If for the given Reference the transaction has already been posted then we do'not Reject the
     * transacton.
     */
    private boolean isTransactionCompleted(BankFusionEnvironment env) {
        boolean result = false;
        ArrayList params = new ArrayList();
        params.add(transReference);
        params.add("N");
        try {
            List txnList = env.getFactory().findByQuery(IBOTransaction.BONAME, posCompletionWhereClause, params, null);
            if (txnList.size() != 0) {
                result = true;
            }
        }
        catch (BankFusionException exxception) {

        }
        return result;
    }

    /**
     * This method populates the error details in the message
     * 
     * @returns String
     */
    // artf46925 method made as public instead of private
    public void populateErrorDetails(String authorisedFlag, String errorCode, int errorNo, String errorLevel,
            ATMSparrowFinancialMessage financialMessage, Object[] fields, BankFusionEnvironment env) {
        financialMessage.setAuthorisedFlag(authorisedFlag);
        financialMessage.setErrorCode(errorCode);
        financialMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorNo, fields));
    }
}
