/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
import com.misys.cbs.common.util.DateUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.ForwardBalanceInfo;
import com.misys.ub.swift.SWT_MT940950Constants;
import com.misys.ub.swift.StatementInfo;
import com.misys.ub.swift.StatementSingleInfo;
import com.misys.ub.swift.UB_MT940950;
import com.misys.ub.swift.UB_SWT_TransformUBtoMeridian;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSWIFTSTMTDETAILS;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_SWTACCOUNTSTMT;
import com.trapedza.bankfusion.bo.refimpl.IBOUBVW_SWTTRANSDISP;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PopulateMT940950;

/**
 * @author Shaileja Ravi
 * 
 */

public class UB_SWT_PopulateMT940950 extends AbstractUB_SWT_PopulateMT940950 {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private IBOSWIFTSTMTDETAILS accountstatement;
    private IPersistenceObjectsFactory factory;
    private static final String BOOKBALANCE = "BOOKBALANCE";
    private static final String MESSAGETYPE = "MESSAGETYPE";

    private transient final static Log logger = LogFactory.getLog(UB_SWT_PopulateMT940950.class.getName());

    private final static String transWhereClause = "WHERE " + IBOUBVW_SWTTRANSDISP.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
            + IBOUBVW_SWTTRANSDISP.UBACCTRANSCOUNTER + " > ? ORDER BY " + IBOUBVW_SWTTRANSDISP.UBACCTRANSCOUNTER + " ASC";

    private final static String openingBalanceWhereClause = " SELECT " + IBOUBVW_SWTTRANSDISP.BOOKBALANCE + " AS " + BOOKBALANCE
            + " FROM " + IBOUBVW_SWTTRANSDISP.BONAME + " WHERE " + IBOUBVW_SWTTRANSDISP.ACCOUNTPRODUCT_ACCPRODID + "= ? AND "
            + IBOUBVW_SWTTRANSDISP.UBACCTRANSCOUNTER + " = ?";

    private final static String messageTypeWhereClause = " SELECT " + IBOSWTDisposal.MESSAGETYPE + " AS " + MESSAGETYPE + " FROM "
            + IBOSWTDisposal.BONAME + " WHERE " + IBOSWTDisposal.CONTRAACCOUNTID + " = ?  OR " + IBOSWTDisposal.CUSTACCOUNTID
            + " = ? ";

    private final static String swtTransCodewhereClause2 = "WHERE " + IBOMisTransactionCodes.SWTELEMSGMNEMONIC + " <> ''";

    private final static String RECORD_EXISTS = "SELECT UBBICCODE FROM UBTB_SWTMSGBICMAP WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";

    private final static String Y = "Y";
    private final static String MT = "MT";
    private final static String SINGLESLASH = "/";
    private final static String DOUBLESLASH = "//";
    private final static String MSC = "MSC";
    private final static String DOT = ".";
    private final static String DOLLER = "$";
    private final static String PERCENTAGE = "%";
    private final static String COLAN = ":";
    private final static String EXC = "!";
    private final static String UNDER_SCORE = "_";
    private final static String ZERO = "0";
    private final static String N = "N";
    private final static String S = "S";
    private final static String SWTMSGTYPE = "950";
    private static HashMap branch_BICCodeMap = new HashMap();
    private UB_SWT_Util util = new UB_SWT_Util();

    Marshaller marshaller;
    StringWriter xmlWriter;
    List disposalList;
    List swtTransConfList;

    HashMap misTxnCodeMap;
    ArrayList params = new ArrayList();
    Properties messageMicroflowMap = new Properties();
    String senderBICCode;
    Queue queue;
    QueueSession session;
    QueueConnection connection;
    QueueSender queueSender;
    Integer closingTransCounter = 0;
    Integer statementClosingTransCounter = 0;
    String meridianMessage;
    private static final String ENDPOINT_OUT = "TO_UBMMM_OUTGOING";

    /**
     * 
     * @param env
     */

    public UB_SWT_PopulateMT940950(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) {
        factory = BankFusionThreadLocal.getPersistanceFactory();
        branch_BICCodeMap = util.populateBranch_BICCodeMap(env);
        senderBICCode = branch_BICCodeMap.get(getF_IN_BranchSortCode()).toString();
        List accStmtIterator = new ArrayList();
        initialise();

        try {
            
            accStmtIterator = BankFusionThreadLocal.getPersistanceFactory().findAll(IBOSWIFTSTMTDETAILS.BONAME, null, true);

        }
        catch (BankFusionException ex) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_SWIFT_FORMAT, new Object[] {}, new HashMap(), env);
        }

        if (accStmtIterator == null) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_THE_MUST_BE_ENTERED_, new Object[] {}, new HashMap(), env);
        }
        else {
            // ArrayList containing LastStatementDate column name in
            // AccountStatement
            // table.
            Date todayDate = SystemInformationManager.getInstance().getBFBusinessDate();

            for (int x = 0; x < accStmtIterator.size(); x++) {
                params.clear();
                String statementId = ((IBOSWIFTSTMTDETAILS) accStmtIterator.get(x)).getBoID();
                IBOUBTB_SWTACCOUNTSTMT swtAcctStatment = (IBOUBTB_SWTACCOUNTSTMT) factory
                        .findByPrimaryKey(IBOUBTB_SWTACCOUNTSTMT.BONAME, statementId, true);
                Date nextStmtDate = swtAcctStatment.getF_UBNEXTSTMTDT();
                if (DateUtil.getStaticDateForDate(nextStmtDate).after(todayDate)) {
                    accStmtIterator.remove(x);
                    x = x - 1;
                }
            }

            int accStmtIteratorSize = accStmtIterator.size();
            for (int i = 0; i < accStmtIteratorSize; i++) {
                accountstatement = (IBOSWIFTSTMTDETAILS) accStmtIterator.get(i);
                params.clear();
                params.add(new Timestamp(SystemInformationManager.getInstance().getBFBusinessDateTime().getTime()));
                boolean msgGenerated = createMessage(env);

                IBOUBTB_SWTACCOUNTSTMT accStmtObj = null;
                if (msgGenerated) {
                    try {
                        params.clear();
                        accStmtObj = (IBOUBTB_SWTACCOUNTSTMT) factory
                                .findByPrimaryKey(IBOUBTB_SWTACCOUNTSTMT.BONAME, accountstatement.getBoID(), true);
                        boolean noOfstmtCompleted = false;
                        if (null != accStmtObj) {
                            if ("I".equals(accStmtObj.getF_UBFREQPERIODCODE())) {
                                if ((accStmtObj.getF_UBLASTSTMTNUMBER() + 2) > accStmtObj.getF_UBNUMBEROFSTATEMENTS()) {
                                    noOfstmtCompleted = true;
                                }
                            }
                            accStmtObj.setF_UBLASTSTMTDTTTM(
                                    new Timestamp(SystemInformationManager.getInstance().getBFBusinessDateTime().getTime()));
                            Timestamp nextDateTime = new Timestamp(
                                    util.GetNextStatementDay(SystemInformationManager.getInstance().getBFBusinessDateTime(),
                                            accStmtObj.getF_UBFREQPERIODCODE(), accStmtObj.getF_UBSTMTDAY(), env,
                                            accStmtObj.getF_UBINTERVAL(), noOfstmtCompleted).getTime());
                            accStmtObj.setF_UBNEXTSTMTDT(nextDateTime);

                            if (noOfstmtCompleted) {
                                accStmtObj.setF_UBNEXTSTMTDT(DateUtil.getStaticTimestampForTimestamp(nextDateTime));
                                accStmtObj.setF_UBLASTSTMTNUMBER(0);
                                accStmtObj.setF_UBLASTSTMTTXNCOUNTER(closingTransCounter);
                            }
                            else {
                                accStmtObj.setF_UBLASTSTMTNUMBER(accStmtObj.getF_UBLASTSTMTNUMBER() + 1);
                                if (statementClosingTransCounter != 0) {
                                    accStmtObj.setF_UBLASTSTMTTXNCOUNTER(statementClosingTransCounter);
                                }
                            }
                        }

                    }
                    catch (Exception ex) {
                        logger.info("Exception In Database Updation" + ExceptionUtil.getExceptionAsString(ex));
                    }
                }
            }

        }

        try {
            connection.close();
        }
        catch (Exception ex) {
            logger.info(ex.getLocalizedMessage());
        }
    }

    /**
     * 
     * @param env
     */
    private void initialise() {
        String configLocation = GetUBConfigLocation.getUBConfigLocation();
        xmlWriter = new StringWriter();
        try {
            marshaller = new Marshaller(xmlWriter);
            Mapping mapping = new Mapping(getClass().getClassLoader());
            mapping.loadMapping(configLocation + "conf/swift/" + "SwiftMessageMapping.xml");
            marshaller.setMapping(mapping);

            params.clear();
            @SuppressWarnings("FBPE")
            List swtTransConfList1 = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOMisTransactionCodes.BONAME,
                    swtTransCodewhereClause2, params, null, true);

            swtTransConfList = swtTransConfList1;
            int swtTransConfListSize = swtTransConfList.size();
            if (swtTransConfListSize > 0)
                misTxnCodeMap = new HashMap();

            for (int i = 0; i < swtTransConfListSize; i++) {
                IBOMisTransactionCodes misTransactionCode = (IBOMisTransactionCodes) swtTransConfList.get(i);
                if (misTransactionCode.getF_SWTELEMSGMNEMONIC() != null) {
                    misTxnCodeMap.put(misTransactionCode.getBoID(), misTransactionCode.getF_SWTELEMSGMNEMONIC());
                }
            }
        }
        catch (Exception ex) {
            logger.error("Error During Intiailization Of Queue" + ExceptionUtil.getExceptionAsString(ex));
        }

    }

    /**
     * Method Description:Create the 950 or 940 Message
     * 
     * @param env
     * @return
     */
    private boolean createMessage(BankFusionEnvironment env) {

        if (accountstatement == null)
            return false;
        List<IBOUBVW_SWTTRANSDISP> transList = null;
        BigDecimal openingBalance = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal closingBalance = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal closingAvailableBalance = CommonConstants.BIGDECIMAL_ZERO;
        String AccountID = CommonConstants.EMPTY_STRING;
        Date openingDate = null;
        openingDate = accountstatement.getF_UBLASTSTMTDTTTM();

        if (accountstatement != null) {
            try {
                IBOUBTB_SWTACCOUNTSTMT swtAccStmtObj = (IBOUBTB_SWTACCOUNTSTMT) factory
                        .findByPrimaryKey(IBOUBTB_SWTACCOUNTSTMT.BONAME, accountstatement.getBoID(), true);
                AccountID = accountstatement.getF_ACCOUNTID();
                params.clear();
                params.add(AccountID);
                params.add(swtAccStmtObj.getF_UBLASTSTMTTXNCOUNTER());
                transList = (ArrayList) factory.findByQuery(IBOUBVW_SWTTRANSDISP.BONAME, transWhereClause, params, null, true);
                if (transList != null && !transList.isEmpty()) {
                    closingTransCounter = ((IBOUBVW_SWTTRANSDISP) transList.get(transList.size() - 1)).getF_UBACCTRANSCOUNTER();
                    params.clear();
                    params.add(AccountID);
                    params.add(swtAccStmtObj.getF_UBLASTSTMTTXNCOUNTER());
                    List transactionList = factory.executeGenericQuery(openingBalanceWhereClause, params, null, true);
                    if (transactionList != null && !transactionList.isEmpty()) {
                        SimplePersistentObject spo = (SimplePersistentObject) transactionList.get(0);
                        openingBalance = (BigDecimal) (spo.getDataMap().get(BOOKBALANCE));
                    }
                }
                else {
                    /**
                     * Get the Opening Balance from the Account.
                     */
                    IBOAttributeCollectionFeature accountInfo = getAccountInfo(accountstatement.getF_ACCOUNTID());
                    if (accountInfo != null) {
                        openingBalance = accountInfo.getF_BOOKEDBALANCE();
                    }
                    else {
                        openingBalance = BigDecimal.ZERO;
                    }
                }

                openingDate = swtAccStmtObj.getF_UBLASTSTMTTXNCOUNTER() == 0 ? getAccountOpenDate(accountstatement.getF_ACCOUNTID())
                        : accountstatement.getF_UBLASTSTMTDTTTM();

                closingBalance = openingBalance;
                HashMap hashmap = new HashMap();
                hashmap.put("AccountId", AccountID);
                HashMap hashmapout = new HashMap();
                hashmapout = MFExecuter.executeMF("ACC_AccountAvailableBalanceDisplay", env, hashmap);
                closingAvailableBalance = (BigDecimal) hashmapout.get("ActualAvailableBalance");
            }
            catch (Exception ex) {
                logger.error("Error occured while fetching the transaction details for the account : "
                        + accountstatement.getF_ACCOUNTID() + "\n" + ExceptionUtil.getExceptionAsString(ex));
                return false;
            }
        }

        ArrayList msgList = new ArrayList();
        String currency = accountstatement.getF_ISOCURRENCYCODE();
        int noOfDecimals = SystemInformationManager.getInstance().getCurrencyScale(currency);
        ForwardBalanceInfo forwardBalanceInfo = new ForwardBalanceInfo();
        String crdrMark = SWT_MT940950Constants.DEBITMARK;

        params.clear();
        params.add(accountstatement.getF_UBLASTSTMTDTTTM());
        params.add(accountstatement.getF_ACCOUNTID());

        closingBalance = CommonConstants.BIGDECIMAL_ZERO;
        UB_MT940950 statements = new UB_MT940950();
        statements.setMessageType(MT + accountstatement.getF_UBMESSAGETYPE());

        // ***************************************************

        String BICCODE_25P = null;
        Connection connection = factory.getJDBCConnection();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(RECORD_EXISTS);
            preparedStatement.setString(1, accountstatement.getF_ACCOUNTID());
            preparedStatement.setString(2, "940");
            rs = preparedStatement.executeQuery();
            while (rs.next())
                BICCODE_25P = rs.getString("UBBICCODE");
        }
        catch (SQLException e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (rs != null)
                    rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // ***************************************************

        if (senderBICCode != null) {
            statements.setSender(senderBICCode);
        }

        if (accountstatement.getF_ACCTSTATBIC().length() != 0) {
            statements.setReceiver(accountstatement.getF_ACCTSTATBIC());
        }

        else if (accountstatement.getF_CUSTSTMTBIC().length() != 0) {
            statements.setReceiver(accountstatement.getF_CUSTSTMTBIC());
        }

        else {
            statements.setReceiver(accountstatement.getF_BICCODE());
        }

        statements.setTransactionReferenceNumber(UB_SWT_PopulateMT940950.traxRefNumber());

        if ((accountstatement.getF_UBMESSAGETYPE().equals("940") || accountstatement.getF_UBMESSAGETYPE().equals("MT940"))
                && BICCODE_25P != null)
            statements.setAccountIdentificationP(accountstatement.getF_ACCOUNTID() + "$" + BICCODE_25P);
        else statements.setAccountIdentification(accountstatement.getF_ACCOUNTID());

        if (closingBalance.compareTo(CommonConstants.BIGDECIMAL_ZERO) >= 0) {
            crdrMark = SWT_MT940950Constants.CREDITMARK;
        }

        String amount = CommonConstants.EMPTY_STRING;

        amount = openingBalance.setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();

        String amount1 = crdrMark + new java.sql.Date(openingDate.getTime()).toString() + currency + amount;

        String closingamount = crdrMark
                + new java.sql.Date(SystemInformationManager.getInstance().getBFBusinessDate().getTime()).toString() + currency
                + amount;

        statements.setOpeningBalance(amount1);

        statements.setClosingBalance(closingamount);

        statements.setStatementNumber(accountstatement.getF_UBLASTSTMTNUMBER() + 1 + SINGLESLASH + 1);

        statements.setOpeningBalanceOption(SWT_MT940950Constants.TERMINALBALANCEMARK);

        statements.setClosingBalanceOption(SWT_MT940950Constants.TERMINALBALANCEMARK);

        /**
         * If there are any statements.
         */
        if (transList.size() == 0) {
            /*
             * Check if statment is required when there are no transactions
             */
            if (accountstatement.getF_UBTRANSSTMTFLAG().equalsIgnoreCase(Y)) {
                // Create empty tags for repeating values

                statements.addForwardBalance(forwardBalanceInfo);
                StatementSingleInfo oneLine = new StatementSingleInfo();
                statements.addSingleLine(oneLine);
                StatementInfo statementInfo = new StatementInfo();
                statements.addStatement(statementInfo);
                statements.setDeliveryChannel(accountstatement.getF_UBDELIVERYCHANNEL());
                msgList.clear();
                msgList.add(statements);
                messagePublisher(msgList);
            }
            else
                /*
                 * return false to signify that no statement were generated
                 */
                return false;
        }

        Date closingDate = null;
        String stmtNumber1 = null;
        String statementLine = null;

        int transListSize = transList.size();
        int msgNo = 0;
        String messageRefNumber = traxRefNumber();
        for (int i = 0; i < transListSize; i++) {
            int msgSize = 0;

            statements.setMessageType(MT + accountstatement.getF_UBMESSAGETYPE());

            msgSize += accountstatement.getF_UBMESSAGETYPE().length();

            senderBICCode = branch_BICCodeMap.get(getF_IN_BranchSortCode()).toString();

            if (senderBICCode != null) {
                statements.setSender(senderBICCode);
                msgSize += senderBICCode.length();
            }
            if (accountstatement.getF_ACCTSTATBIC().length() != 0) {
                statements.setReceiver(accountstatement.getF_ACCTSTATBIC());
            }

            else if (accountstatement.getF_CUSTSTMTBIC().length() != 0) {
                statements.setReceiver(accountstatement.getF_CUSTSTMTBIC());
            }

            else {
                statements.setReceiver(accountstatement.getF_BICCODE());
            }

            msgSize += accountstatement.getF_BICCODE().length();

            statements.setTransactionReferenceNumber(messageRefNumber);
            msgSize += accountstatement.getF_ACCOUNTID().length();

            if ((accountstatement.getF_UBMESSAGETYPE().equals("940") || accountstatement.getF_UBMESSAGETYPE().equals("MT940"))
                    && BICCODE_25P != null)
                statements.setAccountIdentificationP(accountstatement.getF_ACCOUNTID() + "$" + BICCODE_25P);
            else statements.setAccountIdentification(accountstatement.getF_ACCOUNTID());
            msgSize += accountstatement.getF_ACCOUNTID().length();

            // increment the number of messsages counter on this account
            msgNo += 1;

            stmtNumber1 = (accountstatement.getF_UBLASTSTMTNUMBER() + 1) + SINGLESLASH + msgNo;
            statements.setStatementNumber(stmtNumber1);

            msgSize += stmtNumber1.length();

            closingBalance = openingBalance;

            if (openingBalance.compareTo(CommonConstants.BIGDECIMAL_ZERO) >= 0) {
                crdrMark = SWT_MT940950Constants.CREDITMARK;
            }
            else {
            	crdrMark =SWT_MT940950Constants.DEBITMARK;
            }

            if (i > 0) {
                openingDate = closingDate;
            }

            amount = openingBalance.setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();

            /*
             * amount = crdrMark + new java.sql.Date(openingDate.getTime()).toString() + currency +
             * amount;
             */

            StringBuffer sbCrdrMark = new StringBuffer(crdrMark);
            amount = (sbCrdrMark.append(new java.sql.Date(openingDate.getTime()).toString()).append(currency + amount)).toString();

            statements.setOpeningBalance(amount);

            msgSize += amount.length();

            if (i == 0) {
                statements.setOpeningBalance(amount);
                statements.setOpeningBalanceOption(SWT_MT940950Constants.TERMINALBALANCEMARK);
            }
            else {
                statements.setOpeningBalanceOption(SWT_MT940950Constants.INTERMEDIATEBALANCEMARK);
            }
            msgSize += 1;

            IBOUBVW_SWTTRANSDISP transObj = null;

            statements.addForwardBalance(forwardBalanceInfo);

            StatementInfo statementInfo = new StatementInfo();
            StatementSingleInfo oneLine = new StatementSingleInfo();

            if (accountstatement.getF_UBMESSAGETYPE().equals(SWT_MT940950Constants.MESSAGENUMBER940)) {
                statements.addSingleLine(oneLine);
            }
            else {
                statements.addStatement(statementInfo);
            }

            do {
                transObj = (IBOUBVW_SWTTRANSDISP) transList.get(i);
                closingBalance = closingBalance.add(calculateClosingBalnce(transObj));
                statementLine = createStatement(transObj, currency);
                String reference = transObj.getF_REFERENCE();
                if (reference.trim().length() > 16)
                    reference = reference.substring(0, 16);
                else if (reference.equalsIgnoreCase(""))
                    reference = "NONREF";

                StringBuffer sbStment = new StringBuffer(statementLine);
                statementLine = (sbStment.append(reference).append(DOUBLESLASH)
                        .append(get16CharNarration(transObj.getF_NARRATION()))).toString();
                statementLine = statementLine.replaceAll(COLAN, CommonConstants.EMPTY_STRING);
                statementLine = statementLine.replaceAll(PERCENTAGE, "PCT.");
                statementLine = statementLine.replaceAll(EXC, DOT);

                openingBalance = closingBalance;
                msgSize += statementLine.length();

                if (accountstatement.getF_UBMESSAGETYPE().equals(SWT_MT940950Constants.MESSAGENUMBER940)) {
                    statementInfo = new StatementInfo();
                    statementInfo.setStatementLine(statementLine);
                    String narration = transObj.getF_NARRATION();
                    narration = narration.replaceAll(PERCENTAGE, "PCT.");
                    narration = narration.replaceAll(EXC, DOT);
                    narration = UB_SWT_Util.replaceSpecialChars(narration);
                    if (narration.length() > 65) {
                        narration = narration.substring(0, 65) + DOLLER + narration.substring(65, narration.length());
                    }
                    statementInfo.setInfoToOwner(narration);
                    statements.addStatement(statementInfo);
                    msgSize += narration.length();
                }
                else {
                    oneLine = new StatementSingleInfo();
                    oneLine.setStatementLine(statementLine);
                    statements.addSingleLine(oneLine);

                }

                // increment index to read next transaction object
                i += 1;

            }
            while (i < transListSize && (SWT_MT940950Constants.MESSAGESIZE - msgSize) > SWT_MT940950Constants.MINSIZETOPROCEED);
            i--;
            // populate TAG-62
            String closingBalanceString = null;
            String closingAvailableBalanceString = null;
            // populate CLOSINGBALANCE_62
            crdrMark = null;
            if (closingBalance.compareTo(CommonConstants.BIGDECIMAL_ZERO) >= 0) {
                crdrMark = SWT_MT940950Constants.CREDITMARK;
            }
            else {
                crdrMark = SWT_MT940950Constants.DEBITMARK;
            }
            closingDate = transObj.getF_VALUEDATE();
            // To maintain counter value after each page of statement is generated.
            statementClosingTransCounter = ((IBOUBVW_SWTTRANSDISP) transList.get(i)).getF_UBACCTRANSCOUNTER();
            if (i + 1 == transListSize) {
                statements.setClosingBalanceOption(SWT_MT940950Constants.TERMINALBALANCEMARK);
                closingBalanceString = crdrMark
                        + new java.sql.Date(SystemInformationManager.getInstance().getBFBusinessDate().getTime()).toString()
                        + currency + closingBalance.setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();

                if (closingAvailableBalance != null) {
                    closingAvailableBalanceString = crdrMark
                            + new java.sql.Date(SystemInformationManager.getInstance().getBFBusinessDate().getTime()).toString()
                            + currency + closingAvailableBalance.setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();
                }
            }
            else {
                statements.setClosingBalanceOption(SWT_MT940950Constants.INTERMEDIATEBALANCEMARK);
                closingBalanceString = crdrMark + new java.sql.Date(transObj.getF_VALUEDATE().getTime()).toString() + currency
                        + closingBalance.setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();

                if (closingAvailableBalance != null) {
                    closingAvailableBalanceString = crdrMark
                            + new java.sql.Date(SystemInformationManager.getInstance().getBFBusinessDate().getTime()).toString()
                            + currency + closingAvailableBalance.setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();
                }
            }
            statements.setClosingBalance(closingBalanceString);
            statements.setClosingAvailableBalance(closingAvailableBalanceString);
            statements.setDeliveryChannel(accountstatement.getF_UBDELIVERYCHANNEL());
            msgList.add(statements);
            messagePublisher(msgList);
            msgList.clear();
            statements = null;
            statements = new UB_MT940950();
        }
        return true;
    }

    /**
     * 
     * @param accountid
     * @return
     */
    private IBOAttributeCollectionFeature getAccountInfo(String accountid) {
        IBOAttributeCollectionFeature accountInfo = (IBOAttributeCollectionFeature) BankFusionThreadLocal.getPersistanceFactory()
                .findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, accountid, true);
        return accountInfo;
    }

    /**
     * 
     * @param accountid
     * @return
     */
    private Date getAccountOpenDate(String accountid) {
        IBOAttributeCollectionFeature accountInfo = getAccountInfo(accountid);
        return accountInfo != null ? accountInfo.getF_OPENDATE() : new Date();

    }

    /**
     * Method Description:Create of statement Line Tag 61
     * 
     * @param transObj
     * @param currency
     * @return
     */
    private String createStatement(IBOUBVW_SWTTRANSDISP transObj, String currency) {
        StringBuffer statementLine = new StringBuffer();
        int noOfDecimals = SystemInformationManager.getInstance().getCurrencyScale(currency);
        String PostingDate = new java.sql.Date(transObj.getF_POSTINGDATE().getTime()).toString();
        statementLine.append(new java.sql.Date(transObj.getF_VALUEDATE().getTime()).toString());
        statementLine.append(PostingDate.substring(2));

        // populate debit/credit mark & the respective amount fields
        String tranAmount = CommonConstants.EMPTY_STRING;
        String dealType = N;

        if ((transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.DEBITFLAG)
                || transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase("-"))
                && ((transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.NONREVERSALTRANSACTION)
                        || (transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.ORGINALREVERSEDTRANSACTION))) {
            statementLine.append(SWT_MT940950Constants.DEBITMARK);
            tranAmount = transObj.getF_AMOUNTDEBIT().setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();
        }
        else if ((transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.CREDITFLAG)
                || transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase("+"))
                && ((transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.NONREVERSALTRANSACTION)
                        || (transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.ORGINALREVERSEDTRANSACTION))) {
            statementLine.append(SWT_MT940950Constants.CREDITMARK);
            tranAmount = transObj.getF_AMOUNTCREDIT().setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();
        }
        else if ((transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.DEBITFLAG)
                || transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase("-"))
                && transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.REVERSALTRANSACTION) {
            statementLine.append(SWT_MT940950Constants.DEBITREVERSALMARK);
            tranAmount = transObj.getF_AMOUNTDEBIT().setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();
        }
        else if ((transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.CREDITFLAG)
                || transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase("+"))
                && transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.REVERSALTRANSACTION) {
            statementLine.append(SWT_MT940950Constants.CREDITREVERSALMARK);
            tranAmount = transObj.getF_AMOUNTCREDIT().setScale(noOfDecimals, BigDecimal.ROUND_HALF_UP).abs().toString();
        }
        statementLine.append(currency.substring(2));

        if (tranAmount.indexOf(DOT) == -1) {
            StringBuffer trxAmnt = new StringBuffer(tranAmount);
            tranAmount = (trxAmnt.append(DOT)).toString(); // tranAmount = tranAmount + DOT;
        }
        statementLine.append(tranAmount);
        ArrayList params = new ArrayList();

        params.add(transObj.getF_REFERENCE());

        if (!transObj.getF_transType().equalsIgnoreCase("NonSWT"))
            dealType = S;

        params.clear();

        statementLine.append(dealType);
        if (misTxnCodeMap != null && !misTxnCodeMap.isEmpty()) {

            Object mapValue = misTxnCodeMap.get(transObj.getF_CODE());

            // artf732319 merging
            if (dealType.equalsIgnoreCase("S")) {

                params.clear();
                params.add(transObj.getF_ACCOUNTPRODUCT_ACCPRODID());
                params.add(transObj.getF_ACCOUNTPRODUCT_ACCPRODID());
                List msgList = factory.executeGenericQuery(messageTypeWhereClause, params, null, true);
                if (msgList != null && !msgList.isEmpty()) {
                    SimplePersistentObject spo = (SimplePersistentObject) msgList.get(0);
                    mapValue = (spo.getDataMap().get(MESSAGETYPE));
                }
                if (mapValue == null) {
                    mapValue = SWTMSGTYPE;
                }
            }
            // Ends
            if (mapValue != null) {
                String mapValue1 = mapValue.toString();
                if (mapValue1.trim().length() > 0)
                    statementLine.append(mapValue1);
                else statementLine.append(MSC);
            }
            else statementLine.append(MSC);
        }
        else {

            statementLine.append(MSC);
        }

        return statementLine.toString();
    }

    /**
     * Method Description:Publish the Message the endpoint
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
            if (logger.isInfoEnabled())
                logger.info(xmlWriter.toString());
            UB_SWT_TransformUBtoMeridian transformUBtoMeridian = new UB_SWT_TransformUBtoMeridian();
            meridianMessage = transformUBtoMeridian.executeFiles(xmlWriter.toString());
            logger.info("Meridian Message:  " + meridianMessage);
            MessageProducerUtil.sendMessage(meridianMessage, ENDPOINT_OUT);
        }
        catch (MarshalException e) {
            logger.error("Marshalling Exception During Publising the Message", e);
        }
        catch (ValidationException e) {
            logger.error("Validation Exception During Publising the Message", e);
        }
        catch (Exception e) {
            logger.error("Exception During Publising the Message", e);

        }
    }

    /**
     * Method Description:Calculate the closing Balance
     * 
     * @param closingBalance
     * @param transObj
     * @return
     */
    private BigDecimal calculateClosingBalnce(IBOUBVW_SWTTRANSDISP transObj) {
        BigDecimal debitAmt = BigDecimal.ZERO, creditAmt = BigDecimal.ZERO;
        if ("D".equalsIgnoreCase(transObj.getF_DEBITCREDITFLAG())) {
            debitAmt = (debitAmt.abs()).add(transObj.getF_AMOUNTDEBIT().abs());
        }
        else {
            creditAmt = (creditAmt.abs()).add(transObj.getF_AMOUNT().abs());
        }
        return creditAmt.subtract(debitAmt);
    }

    /**
     * Method Description:Generating Unique Transaction Ref. Number to use instead AccountID
     * 
     * @return
     */
    private static String traxRefNumber() {

        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
        double randomNum = Math.random() * 1000000 + 10000000;
        String lastEight = String.valueOf(randomNum);
        return (format.format(new Date()).trim() + lastEight.substring(2, 10).trim());
    }

    /**
     * Method Description:Append the transaction narrative in the Tag61 StatementLine
     * 
     * @param narration
     * @return
     */
    private String get16CharNarration(String narration) {
        if (null != narration && !narration.isEmpty()) {
            narration = narration.replaceAll(PERCENTAGE, "PCT.");
            narration = narration.replaceAll(EXC, DOT);
            narration = narration.replaceAll(UNDER_SCORE, DOT);
            narration = UB_SWT_Util.replaceSpecialChars(narration);
            if (narration.length() > 16) {
                narration = narration.substring(0, 16);
            }
        }
        return narration;
    }
}
