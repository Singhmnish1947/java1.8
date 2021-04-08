/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.StatementInfo;
import com.misys.ub.swift.UB_MT942;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOMovementHistoryFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_InterimStatementFatom;
import com.trapedza.bankfusion.steps.refimpl.ISWT_InterimStatementFatom;

/**
 * This fatom is used to create the SWIFT Interim Statement(942) message.
 *
 * @author nileshk
 *
 */
public class SWT_InterimStatementFatom extends AbstractSWT_InterimStatementFatom implements ISWT_InterimStatementFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     * Logger and constants defined.
     */
    private transient final static Log logger = LogFactory.getLog(SWT_InterimStatementFatom.class.getName());

    public static final String MESSAGE_TYPE_NO = "MT942";

    public static final String MESSAGE_TYPE = "MessageType";

    public static final String SENDER = "Sender";

    public static final String RECEIVER = "Receiver";

    public static final String TRANS_REFERENCE_NO = "TransactionReferenceNumber-20";

    public static final String ACCOUNT_ID = "AccountIdentification-25";

    public static final String STATEMENT_NUMBER = "StatementNumber-28C";

    public static final String FLOOR_LIMIT_INDICATOR = "FloorLimitIndi-34";

    // public static final String DATE_TIME_INDICATOR = "DateTimeIndi-13D";

    public static final String STATEMENT_LINE = "StatementLine-61";

    public static final String INFO_TO_ACCOUNT_OWNER = "InformationToAccountOwner-86";

    public static final String NUMSERIES_CREDIT = "NumSeries-90C";

    public static final String NUMSERIES_DEBIT = "NumSeries-90D";

    public static final String TRANSACTION_DETAILS = "Tran_Details";

    public static final int MESSAGE_LENGTH = 1536;// max length of the message

    public static final int INITIAL_MESSAGE_LENGTH = 189;// length of the
    // header and
    // trailor

    // as
    // in
    // SWT_MessagePublisher.java

    private HashMap Branch_BICCodeMap = new HashMap();
    private SWT_Util util = new SWT_Util();

    /**
     * Constructor
     *
     * @param env
     */
    public SWT_InterimStatementFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_InterimStatementFatom#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     * @param env
     *            The BankFusion Environment @
     */
    public void process(BankFusionEnvironment env) {

        String fromAccountId = this.getF_IN_fromAccountId();
        String toAccountId = this.getF_IN_toAccountId();
        Timestamp fromDate = this.getF_IN_FromDate();
        Timestamp toDate = this.getF_IN_ToDate();
        // Get the account existing within the specified range
        String accountStatementWhereClause = " WHERE " + IBOAccount.ACCOUNTID + " BETWEEN ? AND ? ";
        ArrayList accountStatementQueryParams = new ArrayList();
        accountStatementQueryParams.add(fromAccountId);
        accountStatementQueryParams.add(toAccountId);
        Branch_BICCodeMap = util.populateBranch_BICCodeMap(env);

        ArrayList accountStatementList = new ArrayList();
        accountStatementList = (ArrayList) env.getFactory().findByQuery(IBOAccount.BONAME, accountStatementWhereClause,
                accountStatementQueryParams, null);
        if (accountStatementList.size() > 0) {
            for (int i = 0; i < accountStatementList.size(); i++) {
                SimplePersistentObject accountStatementListSimpleObject = (SimplePersistentObject) accountStatementList.get(i);
                String accountId = (String) accountStatementListSimpleObject.getDataMap().get(IBOAccount.ACCOUNTID);

                generateMessage(accountId, fromDate, toDate, env);
            }
        }
        else {
            // throw new BankFusionException(9417, new Object[] {}, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_NO_ACCOUNT_EXIST_WITHIN_THE_SPECIFIED_RANGE, new Object[] {},
                    new HashMap(), env);
        }

    }

    /**
     * @param accountNo
     * @param fromDt
     * @param toDt
     * @param env
     * @ * @ @
     */
    private void generateMessage(String accountNo, Timestamp fromDt, Timestamp toDt, BankFusionEnvironment env) {
        String accountId = accountNo;
        Timestamp fromDate = fromDt;
        Timestamp toDate = toDt;

        String messageType = MESSAGE_TYPE_NO;
        String sender = null;
        String receiver = null;
        String transactionReferenceNumber = null;
        String accountIdentification = null;
        String statementNumber = null;
        String floorLimitIndi = null;
        String dateTimeIndi = null;
        String statementLine = null;
        String informationToAccountOwner = null;
        String numSeries90D = null;
        String numSeries90C = null;
        String credit = null;
        String debit = null;
        int noOfDebit = 0;
        int noOfCredit = 0;
        BigDecimal creditAmount = new BigDecimal(0.00);
        BigDecimal debitAmount = new BigDecimal(0.00);
        int messageLength = INITIAL_MESSAGE_LENGTH;
        String isoCurrency = CommonConstants.EMPTY_STRING;
        HashMap message942Map = new HashMap();
        HashMap message942Transaction = new HashMap();
        ArrayList message942List = new ArrayList();

        // added ORDER BY + IBOTransaction.POSTINGDATE

        String transactionQueryWhereClause = "WHERE " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
                + IBOTransaction.VALUEDATE + " >= ? AND " + IBOTransaction.VALUEDATE + " <= ? ORDER BY "
                + IBOTransaction.POSTINGDATE;

        // String movementHistoryWhereClause=" WHERE
        // "+IBOMovementHistoryFeature.ACCOUNTID+" = ? ";
        String movementHistoryQuery = "SELECT " + IBOMovementHistoryFeature.CLOSINGCLEAREDBALANCE + " FROM "
                + IBOMovementHistoryFeature.BONAME + " WHERE " + IBOMovementHistoryFeature.ACCOUNTID + "=?" + " AND "
                + IBOMovementHistoryFeature.MOVEMENTDATE + "<? ORDER BY " + IBOMovementHistoryFeature.MOVEMENTDATE + " DESC";

        String swtCustomerConfigQuery1 = "SELECT T1." + IBOSwtCustomerDetail.STATEMENTMSGREQUIRED + " AS "
                + IBOSwtCustomerDetail.STATEMENTMSGREQUIRED + ", T1." + IBOSwtCustomerDetail.SWTACTIVE + " AS "
                + IBOSwtCustomerDetail.SWTACTIVE + " FROM " + IBOSwtCustomerDetail.BONAME + " T1 , " + IBOAccount.BONAME;
        @SuppressWarnings("FBPE")
        String swtCustomerConfigQuery2 = " T2 WHERE T2." + IBOAccount.ACCOUNTID + " = ? " + " AND T2." + IBOAccount.CUSTOMERCODE
                + " = T1."
                + IBOSwtCustomerDetail.CUSTOMERCODE;

        String disposalWhere = " WHERE " + IBOSWTDisposal.DEALNO + "=?";

        ArrayList transactionQueryParams = new ArrayList();
        ArrayList movementHistoryQueryParams = new ArrayList();
        ArrayList swtCustomerConfigQueryParams = new ArrayList();
        ArrayList paramlist = new ArrayList();

        ArrayList transactionQueryList = new ArrayList();
        ArrayList senderQueryList = new ArrayList();
        ArrayList movementHistoryQueryList = new ArrayList();
        ArrayList swtCustomerConfigQueryList = new ArrayList();

        transactionQueryParams.add(accountId);
        Calendar calFromDate = Calendar.getInstance();
        calFromDate.setTime(fromDate);
        calFromDate.set(Calendar.HOUR, 0);
        calFromDate.set(Calendar.MINUTE, 0);
        calFromDate.set(Calendar.SECOND, 1);
        fromDate = new Timestamp(calFromDate.getTimeInMillis());
        transactionQueryParams.add(fromDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(toDate);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        toDate = new Timestamp(cal.getTimeInMillis());
        transactionQueryParams.add(toDate);

        movementHistoryQueryParams.add(accountId);
        swtCustomerConfigQueryParams.add(accountId);

        transactionQueryList = (ArrayList) env.getFactory().findByQuery(IBOTransaction.BONAME, transactionQueryWhereClause,
                transactionQueryParams, null);
        IBOAccount accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountId, false);
        IBOSwtCustomerDetail receiverCustBOObj = (IBOSwtCustomerDetail) env.getFactory()
                .findByPrimaryKey(IBOSwtCustomerDetail.BONAME, accountBO.getF_CUSTOMERCODE(), true);
        senderQueryList = (ArrayList) BranchUtil.getListOfBranchDetailsInCurrentZone();
        // movementHistoryQueryList=(ArrayList)
        // env.getFactory().findByQuery(IBOMovementHistoryFeature.BONAME,
        // movementHistoryQuery, movementHistoryQueryParams,null);

        swtCustomerConfigQueryList = (ArrayList) env.getFactory()
                .executeGenericQuery(swtCustomerConfigQuery1 + swtCustomerConfigQuery2,
                swtCustomerConfigQueryParams, null);
        IBOAccount account = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountId, true);

        String statementGenRequired = CommonConstants.EMPTY_STRING;
        String isCustomerSwiftActive = CommonConstants.EMPTY_STRING;
        if (swtCustomerConfigQueryList.size() > 0) {
            SimplePersistentObject swtCustomerConfigSimpleObject = (SimplePersistentObject) swtCustomerConfigQueryList.get(0);
            statementGenRequired = (String) swtCustomerConfigSimpleObject.getDataMap()
                    .get(IBOSwtCustomerDetail.STATEMENTMSGREQUIRED);
            isCustomerSwiftActive = ((String) swtCustomerConfigSimpleObject.getDataMap().get(IBOSwtCustomerDetail.SWTACTIVE));
        }
        // check for statement generation
        if (statementGenRequired.equals("Y") && isCustomerSwiftActive.equalsIgnoreCase("Y")) {
            logger.info("Account No: " + accountId + " is eligible for statement generation");

            if (senderQueryList.size() > 0) {
                SimplePersistentObject senderSimpleObject = (SimplePersistentObject) senderQueryList.get(0);
                sender = (String) senderSimpleObject.getDataMap().get(IBOBranch.BICCODE);
            }
            if (null != receiverCustBOObj) {
                receiver = receiverCustBOObj.getF_BICCODE();
            }
            BigDecimal amount = new BigDecimal(0.00);
            /*
             * if(movementHistoryQueryList.size()>0){ SimplePersistentObject
             * movementHistorySimpleObject = (SimplePersistentObject)
             * movementHistoryQueryList.get(0);
             * amount=(BigDecimal)movementHistorySimpleObject.getDataMap
             * ().get(IBOMovementHistoryFeature.CLOSINGCLEAREDBALANCE); SimplePersistentObject
             * accountSimpleObject=(SimplePersistentObject) accountQueryList.get(0); isoCurrency =
             * (String)accountSimpleObject.getDataMap().get(IBOAccount.ISOCURRENCYCODE); }
             */
            paramlist.clear();
            paramlist.add(accountId);
            paramlist.add(fromDt);
            List clearedBalance = env.getFactory().executeGenericQuery(movementHistoryQuery, paramlist, null);
            isoCurrency = account.getF_ISOCURRENCYCODE();

            if (clearedBalance != null && clearedBalance.size() > 0) {
                SimplePersistentObject simplePersistentObject = (SimplePersistentObject) clearedBalance.get(0);
                amount = (BigDecimal) simplePersistentObject.getDataMap().get("0");

            }

            String floorLimitamount = util.DecimalRounding(amount.abs().toString(), util.noDecimalPlaces(isoCurrency, env));
            floorLimitIndi = isoCurrency + floorLimitamount;

            // set the date and time as of UB
            dateTimeIndi = SystemInformationManager.getInstance().getBFBusinessDateTimeAsString()
                    .replaceAll("-", CommonConstants.EMPTY_STRING).replaceAll(":", CommonConstants.EMPTY_STRING)
                    .replaceAll(" ", CommonConstants.EMPTY_STRING).substring(2, 12) + getDateTimeIndic();

            transactionReferenceNumber = accountNo;
            accountIdentification = accountNo;
            UB_MT942 interimStatement = new UB_MT942();

            // check if any transaction exist for the given account
            if (transactionQueryList.size() > 0) {
                SimplePersistentObject transactionSimpleObject = (SimplePersistentObject) transactionQueryList.get(0);
                // isoCurrency = (String)
                // transactionSimpleObject.getDataMap().get(IBOTransaction.ISOCURRENCYCODE);
                isoCurrency = account.getF_ISOCURRENCYCODE();
                // int intermediatemessageLength=messageLength;
                int stmtNo = 0;

                // get the details of all the transaction
                for (int i = 0; i < transactionQueryList.size(); i++) {
                    StatementInfo statementDetails = new StatementInfo();
                    transactionSimpleObject = (SimplePersistentObject) transactionQueryList.get(i);
                    Timestamp valueDate = (Timestamp) transactionSimpleObject.getDataMap().get(IBOTransaction.VALUEDATE);
                    Timestamp postingDate = (Timestamp) transactionSimpleObject.getDataMap().get(IBOTransaction.POSTINGDATE);
                    String reference = (String) transactionSimpleObject.getDataMap().get(IBOTransaction.REFERENCE);
                    String narration = (String) transactionSimpleObject.getDataMap().get(IBOTransaction.NARRATION);
                    // isoCurrency =
                    // (String)accountSimpleObject.getDataMap().get(IBOAccount.ISOCURRENCYCODE);
                    // String isoCurrencyCode = (String)
                    // transactionSimpleObject.getDataMap().get(IBOTransaction.ISOCURRENCYCODE);
                    String debitCreditFlag = (String) transactionSimpleObject.getDataMap().get(IBOTransaction.DEBITCREDITFLAG);
                    String type = (String) transactionSimpleObject.getDataMap().get(IBOTransaction.TYPE);
                    BigDecimal transactionAmount = (BigDecimal) transactionSimpleObject.getDataMap().get(IBOTransaction.AMOUNT);
                    String code = (String) transactionSimpleObject.getDataMap().get(IBOTransaction.CODE);
                    /*
                     * for the type of transaction 'R' for Reversal 'E' for expected transaction i.e
                     * valuedate is greater than posting date default is 'C' or 'D' as indicated by
                     * credit/debit flag
                     */

                    // Transaction 'R' for Reversal is not supported now (bug 9982)
                    // if (type.equals("R"))
                    // type = type + debitCreditFlag;
                    if (valueDate.after(postingDate))
                        type = "E" + debitCreditFlag;
                    else type = debitCreditFlag;

                    // If narration lenght is more than 65, infoNarration will take only first 65
                    // characters.

                    String statementNarration = null;
                    String infoNarration = null;

                    if (narration.length() > 65) {
                        // statementNarration = narration.substring(0, 16);
                        infoNarration = narration.substring(0, 64);
                    }
                    // else if (narration.length() <= 65
                    // && narration.length() > 16) {
                    // statementNarration = narration.substring(0, 16);
                    // infoNarration = narration.substring(0, narration
                    // .length());
                    // }
                    else {
                        // statementNarration = narration;
                        infoNarration = narration;
                    }

                    // statementNarration =
                    // statementNarration.replaceAll(":",CommonConstants.EMPTY_STRING);

                    String transAmount = util.DecimalRounding(transactionAmount.abs().toString(),
                            util.noDecimalPlaces(isoCurrency, env));
                    if (transAmount.indexOf(".") == -1) {
                        transAmount = transAmount + ".";
                    }
                    ArrayList params = new ArrayList();

                    /*
                     * The reference field is populated from the transaction table and the
                     * transaction table contains the reference as size of 40 . While picking the
                     * record from disposal table which has the deal no has 20 , the program fails
                     * with SQL -302 . Hence The code is being modified to take only 20 character
                     * for now.
                     */
                    if (reference.length() > 20)
                        params.add(reference.substring(0, 19));
                    else params.add(reference);
                    String dealType = "N";
                    List disposalList = env.getFactory().findByQuery(IBOSWTDisposal.BONAME, disposalWhere, params, null);
                    if (disposalList != null && disposalList.size() > 0)
                        dealType = "S";
                    params.clear();

                    // ArrayList val = new ArrayList();
                    String msgMnemonic = CommonConstants.EMPTY_STRING;

                    // try{
                    // Using the Cache of TransactionScreenControl Table for fetching the details.
                    MISTransactionCodeDetails mistransDetails;
                    try {
                        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
                                .getInstance().getServiceManager()
                                .getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
                        mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo())
                                .getMisTransactionCodeDetails(code);

                        IBOMisTransactionCodes swtTransConfObj = mistransDetails.getMisTransactionCodes();
                        if (swtTransConfObj.getF_SWTELEMSGMNEMONIC().trim().length() == 0)
                            msgMnemonic = "MSC";
                        else msgMnemonic = (swtTransConfObj.getF_SWTELEMSGMNEMONIC());
                    }
                    catch (BankFusionException ex) {
                        msgMnemonic = code;
                        logger.error(ExceptionUtil.getExceptionAsString(ex));
                    }

                    // as per the swift standards, REFERENCE FOR THE ACCOUNTOWNER (Field 61:
                    // Statement Line, Subfield 7) for msg 942
                    // should be of 16 digits only. so here only first 16 characters are taken for
                    // the reference.

                    if (reference.length() > 16)
                        reference = reference.substring(0, 15);
                    reference = reference.replaceAll(":", CommonConstants.EMPTY_STRING);

                    statementLine = valueDate.toString().replaceAll("-", CommonConstants.EMPTY_STRING).substring(2, 8)
                            + valueDate.toString().replaceAll("-", CommonConstants.EMPTY_STRING).substring(4, 8) + type
                            + isoCurrency.substring(2) + transAmount
                            // + "$"
                            + dealType + msgMnemonic + reference + "//" + getNumber(i);
                    // statementNarration.replaceAll("%", "PCT.");
                    messageLength = messageLength + statementLine.length();

                    informationToAccountOwner = infoNarration.replaceAll("%", "PCT.");
                    informationToAccountOwner = informationToAccountOwner.replaceAll("!", ".");
                    informationToAccountOwner = informationToAccountOwner.replaceAll(":", CommonConstants.EMPTY_STRING);

                    statementDetails.setInfoToOwner(informationToAccountOwner);
                    statementDetails.setStatementLine(statementLine);
                    interimStatement.addDetails(statementDetails);

                    messageLength = messageLength + informationToAccountOwner.length();
                    /*
                     * message942Transaction .put(STATEMENT_LINE + i, statementLine);
                     * message942Map.put(INFO_TO_ACCOUNT_OWNER + i, informationToAccountOwner);
                     */

                    if (debitCreditFlag.equals("C")) {
                        noOfCredit++;
                        creditAmount = creditAmount.add(transactionAmount);
                    }
                    else {
                        noOfDebit++;
                        debitAmount = debitAmount.add(transactionAmount);
                    }

                    /*
                     * check for message length ang generate the message if the length of message is
                     * greater than the specified length all the transaction details are within the
                     * specified message length
                     */

                    if ((messageLength + (message942Map.size() * 5) + 100) > MESSAGE_LENGTH
                            || (i + 1) == transactionQueryList.size()) {
                        logger.info("Generated message length is " + messageLength);
                        messageLength = INITIAL_MESSAGE_LENGTH;// re-initialized
                        // to the length
                        // of header and
                        // trailor
                        credit = util.DecimalRounding(creditAmount.abs().toString(), util.noDecimalPlaces(isoCurrency, env));
                        debit = util.DecimalRounding(debitAmount.abs().toString(), util.noDecimalPlaces(isoCurrency, env));

                        numSeries90D = new Integer(noOfDebit).toString() + isoCurrency + debit;
                        numSeries90C = new Integer(noOfCredit).toString() + isoCurrency + credit;
                        // reset the debit and credit counter and amount for new
                        // message
                        noOfCredit = 0;
                        noOfDebit = 0;
                        creditAmount = new BigDecimal(0.00);
                        debitAmount = new BigDecimal(0.00);

                        stmtNo++;
                        // TODO - "1/" to be changed once the project for MT942 is taken up for
                        // stroing info
                        // on MT942
                        statementNumber = "1/" + stmtNo;
                        messageLength = messageLength + statementNumber.length();
                        String senderBICCode = Branch_BICCodeMap.get(getF_IN_branchShortCode()).toString();

                        // add message details to Map
                        interimStatement.setMessageType(messageType);
                        interimStatement.setReceiver(receiver);
                        interimStatement.setSender(senderBICCode);
                        interimStatement.setTransactionReferenceNumber(transactionReferenceNumber);
                        interimStatement.setAccountIdentification(accountIdentification);
                        interimStatement.setStatementNumber(statementNumber);
                        interimStatement.setFloorLimitIndicator1(floorLimitIndi);
                        interimStatement.setDateTimeIndicator(dateTimeIndi);
                        interimStatement.setNumberSumEntriesCredit(numSeries90C);
                        interimStatement.setNumberSumEntriesDebit(numSeries90D);

                        /*
                         * message942Map.put(MESSAGE_TYPE, messageType); message942Map.put(SENDER,
                         * senderBICCode); message942Map.put(RECEIVER, receiver);
                         * message942Map.put(TRANS_REFERENCE_NO, transactionReferenceNumber);
                         * message942Map.put(ACCOUNT_ID, accountIdentification);
                         * message942Map.put(STATEMENT_NUMBER, statementNumber); message942Map
                         * .put(FLOOR_LIMIT_INDICATOR, floorLimitIndi);
                         * message942Map.put(DATE_TIME_INDICATOR, dateTimeIndi);
                         * message942Map.put(NUMSERIES_DEBIT, numSeries90D);
                         * message942Map.put(NUMSERIES_CREDIT, numSeries90C);
                         * message942Map.put(TRANSACTION_DETAILS, message942Transaction);
                         */
                        // clear the list for next iteration
                        message942List.clear();
                        message942List.add(interimStatement);
                        messagePublisher(message942List, env);
                        // clear the map for next iteration
                        // message942Map.clear();
                        // messagePublisher(interimStatement,env);
                    }
                }
                logger.info("Interim Statement generated successfully for Account No " + accountId);
            }
            else {
                logger.info("No transaction exist for Account No " + accountId + " within the specified duration");
                // if there is no transaction for the specified account
                statementNumber = "1/1";
                credit = util.DecimalRounding(creditAmount.abs().toString(), util.noDecimalPlaces(isoCurrency, env));
                debit = util.DecimalRounding(debitAmount.abs().toString(), util.noDecimalPlaces(isoCurrency, env));
                String senderBICCode = Branch_BICCodeMap.get(getF_IN_branchShortCode()).toString();

                numSeries90D = new Integer(noOfDebit).toString() + isoCurrency + debit;
                numSeries90C = new Integer(noOfCredit).toString() + isoCurrency + credit;

                // add message details to Map

                interimStatement.setMessageType(messageType);
                interimStatement.setReceiver(receiver);
                interimStatement.setSender(senderBICCode);
                interimStatement.setTransactionReferenceNumber(transactionReferenceNumber);
                interimStatement.setAccountIdentification(accountIdentification);
                interimStatement.setStatementNumber(statementNumber);
                interimStatement.setFloorLimitIndicator1(floorLimitIndi);
                interimStatement.setDateTimeIndicator(dateTimeIndi);
                interimStatement.setNumberSumEntriesCredit(numSeries90C);
                interimStatement.setNumberSumEntriesDebit(numSeries90D);

                /*
                 * message942Map.put(MESSAGE_TYPE, messageType); message942Map.put(SENDER,
                 * senderBICCode); message942Map.put(RECEIVER, receiver);
                 * message942Map.put(TRANS_REFERENCE_NO, transactionReferenceNumber);
                 * message942Map.put(ACCOUNT_ID, accountIdentification);
                 * message942Map.put(STATEMENT_NUMBER, statementNumber);
                 * message942Map.put(FLOOR_LIMIT_INDICATOR, floorLimitIndi);
                 * message942Map.put(DATE_TIME_INDICATOR, dateTimeIndi);
                 * message942Map.put(NUMSERIES_DEBIT, numSeries90D);
                 * message942Map.put(NUMSERIES_CREDIT, numSeries90C);
                 */

                message942List.clear();
                message942List.add(interimStatement);
                messagePublisher(message942List, env);
                message942Map.clear();
                logger.info("Interim Statement generated successfully for Account No " + accountId);
            }
        }
        else {
            logger.info("Account No: " + accountId + " is not eligible of statement generation");
        }
    }

    private String getNumber(int i) {
        String result = CommonConstants.EMPTY_STRING + i;
        int length = result.length();
        for (int j = length; j < 5; j++)
            result = "0" + result;
        return result;
    }

    /**
     *
     * @param messageList
     * @param env
     * @
     */
    private void messagePublisher(ArrayList messageList, BankFusionEnvironment env) {
        SWT_MessagePublisher messagePublisher = new SWT_MessagePublisher(env);
        messagePublisher.setF_IN_MessageMap(messageList);
        logger.info("Publishing XML Message");

        messagePublisher.process(env);

    }

    private String getDateTimeIndic() {
        int minutes = 0;
        int hours = 0;
        String timezoneHours = CommonConstants.EMPTY_STRING;
        String timezoneMins = CommonConstants.EMPTY_STRING;

        char sign = ' ';
        TimeZone t1 = TimeZone.getDefault();
        hours = ((t1.getRawOffset() / 1000) / 60) / 60;
        minutes = ((t1.getRawOffset() / 1000) / 60) % 60;
        if (hours < 0) {
            sign = '-';
            if (hours > -10)
                timezoneHours = "0" + String.valueOf(hours).substring(1);
            else timezoneHours = String.valueOf(hours).substring(1);
            if (minutes > -10)
                timezoneMins = "0" + String.valueOf(minutes).substring(1);
            else timezoneMins = String.valueOf(minutes).substring(1);
        }
        else {
            sign = '+';
            if (hours < 10)
                timezoneHours = "0" + String.valueOf(hours).substring(0);
            else timezoneHours = String.valueOf(hours).substring(0);
            if (minutes < 10)
                timezoneMins = "0" + String.valueOf(minutes).substring(0);
            else timezoneMins = String.valueOf(minutes).substring(0);
        }
        return sign + timezoneHours + timezoneMins;
    }
}
