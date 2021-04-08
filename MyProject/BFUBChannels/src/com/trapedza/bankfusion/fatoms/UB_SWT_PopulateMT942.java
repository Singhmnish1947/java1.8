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

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.StatementInfo;
import com.misys.ub.swift.UB_MT942;
import com.misys.ub.swift.UB_SWT_TransformUBtoMeridian;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOMovementHistoryFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_SWTACCOUNTSTMT;
import com.trapedza.bankfusion.bo.refimpl.IBOUBVW_SWTTRANSDISP;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PopulateMT942;

/**
 * This fatom is used to create the SWIFT Interim Statement(942) message.
 *
 * @author Bhavya.Gupta
 *
 */

public class UB_SWT_PopulateMT942 extends AbstractUB_SWT_PopulateMT942 {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     * Logger and constants defined.
     *
     */
    
    private transient final static Log logger = LogFactory.getLog(UB_SWT_PopulateMT942.class.getName());

    public static final String MESSAGE_TYPE_NO = "MT942";

    public static final String MESSAGE_TYPE = "MessageType";

    public static final String TRANS_REFERENCE_NO = "TransactionReferenceNumber-20";

    public static final String ACCOUNT_ID = "AccountIdentification-25";

    public static final String STATEMENT_NUMBER = "StatementNumber-28C";

    public static final String FLOOR_LIMIT_INDICATOR = "FloorLimitIndi-34";

    public static final String STATEMENT_LINE = "StatementLine-61";

    public static final String INFO_TO_ACCOUNT_OWNER = "InformationToAccountOwner-86";

    public static final String NUMSERIES_CREDIT = "NumSeries-90C";

    public static final String NUMSERIES_DEBIT = "NumSeries-90D";

    public static final String TRANSACTION_DETAILS = "Tran_Details";

    public static final int MESSAGE_LENGTH = 1536;// max length of the message

    public static final int INITIAL_MESSAGE_LENGTH = 189;

    // length of the header and trailer as in UB_SWT_MessagePublisher.java

    private HashMap Branch_BICCodeMap = new HashMap();

    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    private static final String RECORD_EXISTS = "SELECT UBBICCODE FROM UBTB_SWTMSGBICMAP WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";

    // MT942 changes starts

    Marshaller marshaller;

    StringWriter xmlWriter;

    List disposalList;

    List swtTransConfList;

    List openingBalanceList;

    HashMap MISMap;
    HashMap MISMap1;

    String senderBICCode;

    Queue queue;

    QueueSession session;

    QueueConnection connection;

    QueueSender queueSender;

    String meridianMessage;
    private static final String ENDPOINT_OUT = "TO_UBMMM_OUTGOING";

    private final static String MSC = "MSC";
    @SuppressWarnings("FBPE")
    private final static String SELECT_MIS_NOTMNEMONIC = "SELECT "+IBOMisTransactionCodes.CODE+" , "+IBOMisTransactionCodes.SWTELEMSGMNEMONIC+" , "+IBOMisTransactionCodes.SWTMESSAGETYPE+ " FROM "+IBOMisTransactionCodes.BONAME +" where " + IBOMisTransactionCodes.SWTELEMSGMNEMONIC + " <> ''";

    ArrayList params = new ArrayList();

    List acctList;

    private UB_SWT_Util util = new UB_SWT_Util();

    private final static String accountStatementQuery = "SELECT DISTINCT (ast." + IBOUBTB_SWTACCOUNTSTMT.UBACCOUNTID
            + ") AS ACCOUNTID  ,s." + IBOSwtCustomerDetail.CUSTOMERCODE + " AS CUSTOMERCODE	,s." + IBOSwtCustomerDetail.BICCODE
            + " AS " + IBOSwtCustomerDetail.BICCODE + ",a." + IBOAccount.ISOCURRENCYCODE + " AS " + IBOAccount.ISOCURRENCYCODE
            + ",s." + IBOSwtCustomerDetail.UBSTMTBIC + " AS CUSTUBSTMTBIC ,ast." + IBOUBTB_SWTACCOUNTSTMT.UBSTMTBIC
            + " AS ACCUBSTMTBIC FROM  " + IBOSwtCustomerDetail.BONAME + " s," + IBOAccount.BONAME + " a,"
            + IBOUBTB_SWTACCOUNTSTMT.BONAME + " ast" + " WHERE ast." + IBOUBTB_SWTACCOUNTSTMT.UBACCOUNTID + "= a."
            + IBOAccount.ACCOUNTID + " AND s." + IBOSwtCustomerDetail.CUSTOMERCODE + " = a." + IBOAccount.CUSTOMERCODE + " AND  s."
            + IBOSwtCustomerDetail.SWTACTIVE + " = 'Y' AND " + " s." + IBOSwtCustomerDetail.STATEMENTMSGREQUIRED + " = 'Y' AND"
            + " ast." + IBOUBTB_SWTACCOUNTSTMT.UBMESSAGETYPE + " = '942' AND ast." + IBOUBTB_SWTACCOUNTSTMT.UBFREQPERIODCODE
            + " <> 'I' AND ast." + IBOUBTB_SWTACCOUNTSTMT.UBACCOUNTID + " = ?";

    private final static String transactionQueryWhereClause = "WHERE " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
            + IBOTransaction.VALUEDATE + " >= ? AND " + IBOTransaction.VALUEDATE + " <= ? ORDER BY " + IBOTransaction.POSTINGDATE;

    private final static String movementHistoryQuery = "SELECT " + IBOMovementHistoryFeature.CLOSINGCLEAREDBALANCE + " FROM "
            + IBOMovementHistoryFeature.BONAME + " WHERE " + IBOMovementHistoryFeature.ACCOUNTID + "=?" + " AND "
            + IBOMovementHistoryFeature.MOVEMENTDATE + "<? ORDER BY " + IBOMovementHistoryFeature.MOVEMENTDATE + " DESC";

    /* private final static String swtTransCodewhereClause = " WHERE boID =?"; */

    /**
     * Constructor
     *
     * @param env
     */
    public UB_SWT_PopulateMT942(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * Method to create a MQ connection
     *
     * @param env
     */
    private void initialise(BankFusionEnvironment env) {

        /*
         * String configLocation = System.getProperty("BFconfigLocation",
         * CommonConstants.EMPTY_STRING);
         */
    	
    	
        String configLocation = GetUBConfigLocation.getUBConfigLocation();
        xmlWriter = new StringWriter();
        try {
            marshaller = new Marshaller(xmlWriter);
            Mapping mapping = new Mapping(getClass().getClassLoader());
            // configLocation = System.getProperty("BFconfigLocation",
            // CommonConstants.EMPTY_STRING);
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            mapping.loadMapping(configLocation + "conf/swift/" + "SwiftMessageMapping.xml");
            marshaller.setMapping(mapping);

            try {
                params.clear();
                swtTransConfList = env.getFactory().executeGenericQuery(SELECT_MIS_NOTMNEMONIC, params, null);
            }
            catch (BankFusionException ex) {
                logger.error(ExceptionUtil.getExceptionAsString(ex));
            }
            int swtTransConfListSize = swtTransConfList.size();
            if (swtTransConfListSize > 0) {
                MISMap = new HashMap();
                MISMap1 = new HashMap();
            }
            for (int i = 0; i < swtTransConfListSize; i++) {
  
            	SimplePersistentObject simplePersistentObject = (SimplePersistentObject) swtTransConfList.get(i);
                if (simplePersistentObject.getDataMap().get("1") != null) {
                    MISMap.put(simplePersistentObject.getDataMap().get("0"), simplePersistentObject.getDataMap().get("1"));
                }
                if (simplePersistentObject.getDataMap().get("2") != null) {
                	MISMap1.put(simplePersistentObject.getDataMap().get("0"), simplePersistentObject.getDataMap().get("2"));
                }
            }

        }
  
        catch (Exception ex) {
            logger.error(ExceptionUtil.getExceptionAsString(ex));
            EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_MQ, new Object[] {},
                    new HashMap(), env);
        }
    }

    public static String traxRefNumber() {

        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
        double randomNum = Math.random() * 1000000 + 10000000;
        String lastEight = String.valueOf(randomNum);
        return (format.format(SystemInformationManager.getInstance().getBFSystemDate()).trim() + lastEight.substring(2, 10).trim());
    }

    /**
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_InterimStatementFatom#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     * @param env
     *            The BankFusion Environment @
     */
    public void process(BankFusionEnvironment env) {

        String fromAccountId = this.getF_IN_fromAccountId();
        // String toAccountId = this.getF_IN_toAccountId();
        Timestamp fromDate = this.getF_IN_FromDate();
        Timestamp toDate = this.getF_IN_ToDate();
        Branch_BICCodeMap = util.populateBranch_BICCodeMap(env);
        ArrayList Params = new ArrayList();
        Params.add(fromAccountId);
        // Params.add(toAccountId);
        initialise(env);
        ArrayList accountStatementList = new ArrayList();
        accountStatementList = (ArrayList) env.getFactory().executeGenericQuery(accountStatementQuery, Params, null);
        int accStmtListSize = accountStatementList.size();
        if (accStmtListSize > 0) {
            for (int i = 0; i < accStmtListSize; i++) {
                SimplePersistentObject accountStatementListSimpleObject = (SimplePersistentObject) accountStatementList.get(i);
                generateMessage(accountStatementListSimpleObject, fromDate, toDate, env);
            }
        }
        else {
            // throw new BankFusionException(9417, new Object[] {}, logger,
            // env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_NO_ACCOUNT_EXIST_WITHIN_THE_SPECIFIED_RANGE, new Object[] {},
                    new HashMap(), env);
        }

    }

    /**
     *
     * @param accountStatementListSimpleObject
     * @param fromDt
     * @param toDt
     * @param env
     */
    private void generateMessage(SimplePersistentObject accountStatementListSimpleObject, Timestamp fromDt, Timestamp toDt,
            BankFusionEnvironment env) {
        String accountId = (String) accountStatementListSimpleObject.getDataMap().get("ACCOUNTID");
        Timestamp fromDate = fromDt;
        Timestamp toDate = toDt;

        String messageType = MESSAGE_TYPE_NO;
        String receiver = null;
        String transactionReferenceNumber = traxRefNumber();
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
        String accUBStmtBIC = CommonConstants.EMPTY_STRING;
        String custUBStmtBIC = CommonConstants.EMPTY_STRING;
        int noOfDebit = 0;
        int noOfCredit = 0;
        BigDecimal creditAmount = BigDecimal.ZERO;
        BigDecimal debitAmount = BigDecimal.ZERO;
        int messageLength = INITIAL_MESSAGE_LENGTH;
        String isoCurrency = CommonConstants.EMPTY_STRING;
        HashMap message942Map = new HashMap();
        ArrayList message942List = new ArrayList();

        ArrayList transactionQueryParams = new ArrayList();
        ArrayList movementHistoryQueryParams = new ArrayList();
        ArrayList swtCustomerConfigQueryParams = new ArrayList();
        ArrayList accountQueryParams = new ArrayList();
        ArrayList paramlist = new ArrayList();

        ArrayList transactionQueryList = new ArrayList();

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
        accountQueryParams.add(accountId);
        transactionQueryList = (ArrayList) env.getFactory().findByQuery(IBOUBVW_SWTTRANSDISP.BONAME, transactionQueryWhereClause,
                transactionQueryParams, null);
        accUBStmtBIC = (String) accountStatementListSimpleObject.getDataMap().get("ACCUBSTMTBIC");
        custUBStmtBIC = (String) accountStatementListSimpleObject.getDataMap().get("CUSTUBSTMTBIC");
        if (accUBStmtBIC.length() != 0) {
            receiver = accUBStmtBIC;
        }
        else if (custUBStmtBIC.length() != 0) {
            receiver = custUBStmtBIC;
        }
        else {

            receiver = (String) accountStatementListSimpleObject.getDataMap().get(IBOSwtCustomerDetail.BICCODE);
        }
        BigDecimal amount = BigDecimal.ZERO;

        paramlist.clear();
        paramlist.add(accountId);
        paramlist.add(fromDt);
        List clearedBalance = env.getFactory().executeGenericQuery(movementHistoryQuery, paramlist, null);

        isoCurrency = (String) accountStatementListSimpleObject.getDataMap().get(IBOAccount.ISOCURRENCYCODE);

        if (clearedBalance != null && clearedBalance.size() > 0) {
            SimplePersistentObject simplePersistentObject = (SimplePersistentObject) clearedBalance.get(0);
            amount = (BigDecimal) simplePersistentObject.getDataMap().get("0");
        }
        int NoOfDecimals = SystemInformationManager.getInstance().getCurrencyScale(isoCurrency);
        String floorLimitamount = util.DecimalRounding(amount.abs().toString(), NoOfDecimals);
        floorLimitIndi = isoCurrency + floorLimitamount;

        // set the date and time as of UB
        dateTimeIndi = SystemInformationManager.getInstance().getBFBusinessDateTimeAsString()
                .replaceAll("-", CommonConstants.EMPTY_STRING).replaceAll(":", CommonConstants.EMPTY_STRING)
                .replaceAll(" ", CommonConstants.EMPTY_STRING).substring(2, 12) + getDateTimeIndic();

        transactionReferenceNumber = UB_SWT_PopulateMT942.traxRefNumber();
        accountIdentification = accountId;
        UB_MT942 interimStatement = new UB_MT942();

        // check if any transaction exist for the given account
        IBOUBVW_SWTTRANSDISP transObj = null;
        int transQueryListSize = transactionQueryList.size();

        // **** SWIFT 2016 changes
        String BICCODE_25P = null;
        Connection connection = factory.getJDBCConnection();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(RECORD_EXISTS);
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, "942");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
                BICCODE_25P = resultSet.getString("UBBICCODE");
            preparedStatement.close();
            resultSet.close();
        }
        catch (SQLException e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            closeConnection(resultSet, preparedStatement, connection);
        }
        // **** SWIFT 2016 changes END
        if (transQueryListSize > 0) {
            int stmtNo = 0;
            String reference = null;
            String narration = null;
            String debitCreditFlag = null;
            String type = null;
            String code = null;
            String infoNarration = null;
            // get the details of all the transaction
            for (int i = 0; i < transQueryListSize; i++) {
                StatementInfo statementDetails = new StatementInfo();
                transObj = (IBOUBVW_SWTTRANSDISP) transactionQueryList.get(i);
                Timestamp valueDate = transObj.getF_VALUEDATE();
                Timestamp postingDate = transObj.getF_POSTINGDATE();
                reference = transObj.getF_REFERENCE();
                narration = util.replaceSpecialChars(transObj.getF_NARRATION());
                debitCreditFlag = transObj.getF_DEBITCREDITFLAG();
                type = transObj.getF_transType();
                BigDecimal transactionAmount = transObj.getF_AMOUNT();
                code = transObj.getF_CODE();
                if (valueDate.after(postingDate))
                    type = "E" + (debitCreditFlag.equals("-") ? "D" : "C");
                else type = debitCreditFlag;

                // If narration lenght is more than 65, infoNarration will take
                // only first 65 characters.

                if (narration.length() > 65) {
                    infoNarration = narration.substring(0, 64);
                }
                else {
                    infoNarration = narration;
                }

                StringBuffer transAmount = new StringBuffer(util.DecimalRounding(transactionAmount.abs().toString(), NoOfDecimals));
                if (transAmount.indexOf(".") == -1) {
                    transAmount = transAmount.append(".");
                }

                /*
                 * The reference field is populated from the transaction table and the transaction
                 * table contains the reference as size of 40 . While picking the record from
                 * disposal table which has the deal no has 20 , the program fails with SQL -302 .
                 * Hence The code is being modified to take only 20 character for now.
                 */
                if (reference.length() > 20)
                    params.add(reference.substring(0, 19));
                else params.add(reference);
                String dealType = "N";
                if (!transObj.getF_transType().equalsIgnoreCase("NonSWT"))
                    dealType = "S";
                params.clear();

                // List disposalList = env.getFactory().findByQuery(
                // IBOSWTDisposal.BONAME, disposalWhere, params, null);
                // if (disposalList != null && disposalList.size() > 0)
                // dealType = "S";
                // params.clear();

                ArrayList val = new ArrayList();
                String msgMnemonic = CommonConstants.EMPTY_STRING;
                val.add(code);
                String messageMnemonic = null;
                if (MISMap != null && !MISMap.isEmpty()) {
                    Object MapValue = MISMap.get(transObj.getF_CODE());

                    if (MapValue != null) {
                        String MapValue1 = MapValue.toString();
                        if (MapValue1.trim().length() > 0)
                            messageMnemonic = MapValue1;
                        else messageMnemonic = MSC;
                    }
                    else messageMnemonic = MSC;
                }
                else {
                    messageMnemonic = MSC;
                }
                if (dealType.equals("S")) {
                    if (MISMap1 != null && !MISMap1.isEmpty()) {
                        Object MapValue = MISMap1.get(transObj.getF_CODE());
                        if (MapValue != null) {
                            msgMnemonic = MapValue.toString();
                        }
                        else {
                            logger.error("Message Type not defined at transaction code will result in invalid MT942 Tag 61");
                        }
                    }
                }
                else {
                    msgMnemonic = messageMnemonic;
                }

                // as per the swift standards, REFERENCE FOR THE ACCOUNTOWNER
                // (Field 61: Statement Line, Subfield 7) for msg 942
                // should be of 16 digits only. so here only first 16 characters
                // are taken for the reference.

                if (reference.length() > 16)
                    reference = reference.substring(0, 15);
                reference = reference.replaceAll(":", CommonConstants.EMPTY_STRING);

                statementLine = valueDate.toString().replaceAll("-", CommonConstants.EMPTY_STRING).substring(2, 8)
                        + valueDate.toString().replaceAll("-", CommonConstants.EMPTY_STRING).substring(4, 8) + type
                        + isoCurrency.substring(2) + transAmount.toString()
                        // + "$"
                        + dealType + msgMnemonic + reference + "//" + getNumber(i);
                messageLength = messageLength + statementLine.length();

                informationToAccountOwner = infoNarration.replaceAll("%", "PCT.");
                informationToAccountOwner = informationToAccountOwner.replaceAll("!", ".");
                informationToAccountOwner = informationToAccountOwner.replaceAll(":", CommonConstants.EMPTY_STRING);

                statementDetails.setInfoToOwner(informationToAccountOwner);
                statementDetails.setStatementLine(statementLine);
                interimStatement.addDetails(statementDetails);

                messageLength = messageLength + informationToAccountOwner.length();

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

                if ((messageLength + (message942Map.size() * 5) + 100) > MESSAGE_LENGTH || (i + 1) == transQueryListSize) {
                    if (logger.isInfoEnabled())
                        logger.info("Generated message length is " + messageLength);
                    messageLength = INITIAL_MESSAGE_LENGTH;// re-initialized
                    // to the length of header and trailer
                    credit = util.DecimalRounding(creditAmount.abs().toString(), NoOfDecimals);
                    debit = util.DecimalRounding(debitAmount.abs().toString(), NoOfDecimals);

                    numSeries90D = new Integer(noOfDebit).toString() + isoCurrency + debit;
                    numSeries90C = new Integer(noOfCredit).toString() + isoCurrency + credit;
                    // reset the debit and credit counter and amount for new
                    // message
                    noOfCredit = 0;
                    noOfDebit = 0;
                    creditAmount = BigDecimal.ZERO;
                    debitAmount = BigDecimal.ZERO;

                    stmtNo++;
                    // TODO - "1/" to be changed once the project for MT942 is
                    // taken up for storing info
                    // on MT942
                    statementNumber = "1/" + stmtNo;
                    messageLength = messageLength + statementNumber.length();
                    String senderBICCode = Branch_BICCodeMap.get(getF_IN_branchSortCode()).toString();

                    // add message details to Map
                    interimStatement.setMessageType(messageType);
                    interimStatement.setReceiver(receiver);
                    interimStatement.setSender(senderBICCode);
                    interimStatement.setTransactionReferenceNumber(transactionReferenceNumber);
                    // swift changes 2016
                    if (BICCODE_25P != null)
                        interimStatement.setAccountIdentificationP(accountIdentification + "$" + BICCODE_25P);
                    else 
                    	interimStatement.setAccountIdentification(accountIdentification);
                    
                    interimStatement.setStatementNumber(statementNumber);
                    interimStatement.setFloorLimitIndicator1(floorLimitIndi);
                    interimStatement.setDateTimeIndicator(dateTimeIndi);
                    interimStatement.setNumberSumEntriesCredit(numSeries90C);
                    interimStatement.setNumberSumEntriesDebit(numSeries90D);
                    //fetching details of Delivery Channel from IBOUBTB_SWTACCOUNTSTMT
                    List<IBOUBTB_SWTACCOUNTSTMT> acctStmtData = new ArrayList<IBOUBTB_SWTACCOUNTSTMT>();
                    ArrayList params = new ArrayList<>();
                    params.add((String)accountId);
                    params.add("942");
                    String QUERY = "WHERE "+ IBOUBTB_SWTACCOUNTSTMT.UBACCOUNTID + "= ? AND " + IBOUBTB_SWTACCOUNTSTMT.UBMESSAGETYPE + "=?";
                    acctStmtData =  BankFusionThreadLocal.getPersistanceFactory()
                            .findByQuery(IBOUBTB_SWTACCOUNTSTMT.BONAME, QUERY, params, null, true);     
                    if (null != acctStmtData && !acctStmtData.isEmpty()) {
                    	for (IBOUBTB_SWTACCOUNTSTMT details : acctStmtData) {
                        	interimStatement.setDeliveryChannel(details.getF_UBDELIVERYCHANNEL());
                        }
                       }
                    message942List.clear();
                    message942List.add(interimStatement);
                    messagePublisher(message942List);
                    interimStatement = null;
                    interimStatement = new UB_MT942();

                }
            }
            if (logger.isInfoEnabled())
                logger.info("Interim Statement generated successfully for Account No " + accountId);
        }
        else {
            if (logger.isInfoEnabled())
                logger.info("No transaction exist for Account No " + accountId + " within the specified duration");
            // if there is no transaction for the specified account
            statementNumber = "1/1";
            credit = util.DecimalRounding(creditAmount.abs().toString(), NoOfDecimals);
            debit = util.DecimalRounding(debitAmount.abs().toString(), NoOfDecimals);
            String senderBICCode = Branch_BICCodeMap.get(getF_IN_branchSortCode()).toString();

            numSeries90D = new Integer(noOfDebit).toString() + isoCurrency + debit;
            numSeries90C = new Integer(noOfCredit).toString() + isoCurrency + credit;

            // add message details to Map

            interimStatement.setMessageType(messageType);
            interimStatement.setReceiver(receiver);
            interimStatement.setSender(senderBICCode);
            interimStatement.setTransactionReferenceNumber(transactionReferenceNumber);

            if (BICCODE_25P != null)
                interimStatement.setAccountIdentificationP(accountIdentification + "$" + BICCODE_25P);
            else 
            	interimStatement.setAccountIdentification(accountIdentification);
            
            interimStatement.setStatementNumber(statementNumber);
            interimStatement.setFloorLimitIndicator1(floorLimitIndi);
            interimStatement.setDateTimeIndicator(dateTimeIndi);
            interimStatement.setNumberSumEntriesCredit(numSeries90C);
            interimStatement.setNumberSumEntriesDebit(numSeries90D);
            //fetching details of Delivery Channel from IBOUBTB_SWTACCOUNTSTMT
            List<IBOUBTB_SWTACCOUNTSTMT> acctStmtData = new ArrayList<IBOUBTB_SWTACCOUNTSTMT>();
            ArrayList params = new ArrayList<>();
            params.add((String)accountId);
            params.add("942");
            String QUERY = "WHERE "+ IBOUBTB_SWTACCOUNTSTMT.UBACCOUNTID + "= ? AND " + IBOUBTB_SWTACCOUNTSTMT.UBMESSAGETYPE + "=?";
            acctStmtData =  BankFusionThreadLocal.getPersistanceFactory()
                    .findByQuery(IBOUBTB_SWTACCOUNTSTMT.BONAME, QUERY, params, null, true);     
            if (null != acctStmtData && !acctStmtData.isEmpty()) {
            	for (IBOUBTB_SWTACCOUNTSTMT details : acctStmtData) {
                	interimStatement.setDeliveryChannel(details.getF_UBDELIVERYCHANNEL());
                }
               } 
            message942List.clear();
            message942List.add(interimStatement);
            messagePublisher(message942List);
            message942Map.clear();
            if (logger.isInfoEnabled())
                logger.info("Interim Statement generated successfully for Account No " + accountId);
        }

    }

    /**
     *
     * @return date and time
     */
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
            if (hours > -10) {
                timezoneHours = "0" + String.valueOf(hours).substring(1);
            }
            else timezoneHours = String.valueOf(hours).substring(1);
            if (minutes > -10) {
                if (minutes == 0)
                    timezoneMins = "00";
                else timezoneMins = "0" + String.valueOf(minutes).substring(1);
            }
            else timezoneMins = String.valueOf(minutes).substring(1);
        }
        else {
            sign = '+';
            if (hours < 10) {
                if (hours == 0)
                    timezoneHours = "00";
                else timezoneHours = "0" + String.valueOf(hours).substring(0);
            }
            else timezoneHours = String.valueOf(hours).substring(0);
            if (minutes < 10) {
                if (minutes == 0)
                    timezoneMins = "00";
                else timezoneMins = "0" + String.valueOf(minutes).substring(0);
            }
            else timezoneMins = String.valueOf(minutes).substring(0);
        }
        return sign + timezoneHours + timezoneMins;
    }

    /**
     *
     * @param i
     * @return result
     */
    private String getNumber(int i) {
        StringBuffer result = new StringBuffer(CommonConstants.EMPTY_STRING + i);
        int length = result.length();
        for (int j = length; j < 5; j++)
            result.insert(0, "0");
        return result.toString();
    }

    /**
     *
     * @param messageList
     * @param env
     */
    private void messagePublisher(ArrayList messageList) {
        try {
            xmlWriter = new StringWriter();
            marshaller.setWriter(xmlWriter);
            Object messageObject = messageList.get(0);
            marshaller.marshal(messageObject);

            UB_SWT_TransformUBtoMeridian transformUBtoMeridian = new UB_SWT_TransformUBtoMeridian();
            meridianMessage = transformUBtoMeridian.executeFiles(xmlWriter.toString());
            logger.info("Meridian Message:  " + meridianMessage);
            MessageProducerUtil.sendMessage(meridianMessage, ENDPOINT_OUT);
        }
        catch (MarshalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * <code>closeConnection</code> method closes the given ResultSet/PreparedStatement/Connection
     * objects gracefully.
     * 
     * @param resultSet
     * @param preparedStatement
     * @param connection
     * 
     * @author Chethan.ST
     */
    private void closeConnection(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
        try {
            if (null != resultSet) {
                resultSet.close();
            }
            if (null != preparedStatement) {
                preparedStatement.close();
            }
        }
        catch (SQLException e) {
            logger.debug("Failed to close the connection rs/ps/con properly:: " + e);
        }
    }

}