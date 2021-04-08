/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.swift.ForwardBalanceInfo;
import com.misys.ub.swift.SWT_MT940950Constants;
import com.misys.ub.swift.SWT_StatementDateHelper;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT940950;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBankStatementFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOMovementHistoryFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_AccountStatementConfig;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_Generate_MT940950;

public class SWT_Generate_MT940950 extends AbstractSWT_Generate_MT940950 {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(SWT_Generate_MT940950.class.getName());

    private SimplePersistentObject simplePersistentObject;
    Properties messageMicroflowMap = new Properties();
    private HashMap Branch_BICCodeMap = new HashMap();
    private SWT_Util util = new SWT_Util();
    private IBOUBTB_AccountStatementConfig accountStatementConfig;
    private IPersistenceObjectsFactory factory;
    private final static  String RECORD_EXISTS ="SELECT UBBICCODE FROM UBTB_SWTMSGBICMAP WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";
	private IPersistenceObjectsFactory factory1 = BankFusionThreadLocal.getPersistanceFactory();	


    public SWT_Generate_MT940950(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {

        Branch_BICCodeMap = util.populateBranch_BICCodeMap(env);
        /*
         * String whereClause = "WHERE " + IBOBankStatementFeature.SWTMESSAGETYPE +
         * " IN ('940','950')" + "AND " + IBOBankStatementFeature.FREQUENCYPERIODCODE + " <>  'N' ";
         */
        List accStmtList = new ArrayList();
        try {
            /*
             * accStmtList = env.getFactory().findByQuery(IBOBankStatementFeature.BONAME,
             * whereClause, null);
             */
        }
        catch (BankFusionException ex) {
            logger.error("Error occured while trying to fetch AccountStatement list for SWIFT 940 & 950 messages", ex);
            // new BankFusionException(9303, null, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_SWIFT_FORMAT, new Object[] {}, new HashMap(), env);
        }

        if (accStmtList == null) {
            new BankFusionException(ChannelsEventCodes.E_THE_MUST_BE_ENTERED_, null, logger, env);
            // EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_SWIFT_FORMAT, new Object[]{}, new
            // HashMap(), env);
        }
        else {
            // ArrayList containing LastStatementDate column name in AccountStatement
            // table.
            for (int i = 0; i < accStmtList.size(); i++) {
                IBOBankStatementFeature accStmtObj = (IBOBankStatementFeature) accStmtList.get(i);
                SWT_StatementDateHelper dateHelper = new SWT_StatementDateHelper();

                accountStatementConfig = (IBOUBTB_AccountStatementConfig) factory.findByPrimaryKey(
                        IBOUBTB_AccountStatementConfig.BONAME, accStmtObj.getF_UBACCSTMTCFGID(), false);
                boolean msgGenRequired = dateHelper.CheckGenerateMessage(SystemInformationManager.getInstance().getBankName(),
                        accStmtObj.getF_ACCOUNTID(), accountStatementConfig.getF_UBSTMTFREQUENCY(),
                        accStmtObj.getF_FREQUENCYPERIODUNIT(), accountStatementConfig.getF_UBSTMTDAY(),
                        accountStatementConfig.getF_UBSTMTMONTH(), accStmtObj.getF_LASTSTMTDATE(), env);
                if (msgGenRequired) {
                    msgGenRequired = stmtRequired(accStmtObj, env);
                }

                if (msgGenRequired) {
                    boolean msgGenerated = createMessage(accStmtObj, env);
                    if (msgGenerated) {
                        try {
                            accStmtObj.setF_LASTSTMTDATE(new Timestamp(SystemInformationManager.getInstance()
                                    .getBFBusinessDateTime().getTime()));
                            accStmtObj.setF_LASTSTMTNUMBER(accStmtObj.getF_LASTSTMTNUMBER() + 1);
                        }
                        catch (Exception ex) {
                                logger.error("Exception occured", ex);
                        }
                    }
                }

            }
        }
    }

    private boolean createMessage(IBOBankStatementFeature accStmtObj, BankFusionEnvironment env) {

        List transList = null;

        ArrayList params = new ArrayList();
        String custCode = null;

        IBOSwtCustomerDetail swtCustObj = null;

        String transWhereClause = "where " + IBOTransaction.POSTINGDATE + " > ? and " + IBOTransaction.POSTINGDATE + " <= ? and "
                + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = ?";
      //************
      		String BICCODE_25P = null;
      		Connection connection = factory1.getJDBCConnection();
      		ResultSet rs = null;
      	    PreparedStatement preparedStatement = null;
      	  try {   
      	       preparedStatement = connection.prepareStatement(RECORD_EXISTS);
      	       preparedStatement.setString(1, accStmtObj.getF_ACCOUNTID());
      		   preparedStatement.setString(2,"940");
      		   rs = preparedStatement.executeQuery();
      	while(rs.next())
             	  BICCODE_25P = rs.getString("UBBICCODE"); 
      	preparedStatement.close();
      	rs.close();
      	  }catch(SQLException e){
               logger.error(ExceptionUtil.getExceptionAsString(e));
               }
      	  finally {
      		  try {
      			  if(preparedStatement != null) {
      				  preparedStatement.close();
      			  }
      			  if(rs != null) {
      				  rs.close();
      			  }
      		  } catch(SQLException ex) {
      			logger.error(ExceptionUtil.getExceptionAsString(ex));
      		  }
      	  }
      		//************
      		
        if (accStmtObj != null) {
            try {
                params.add(accStmtObj.getF_LASTSTMTDATE());
                params.add(SystemInformationManager.getInstance().getBFBusinessDateTime());
                params.add(accStmtObj.getF_ACCOUNTID());
                transList = env.getFactory().findByQuery(IBOTransaction.BONAME, transWhereClause, params, null);
                custCode = FinderMethods.findCustomerCodeByAccount(accStmtObj.getF_ACCOUNTID());

                swtCustObj = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME, custCode);
            }
            catch (Exception ex) {
                /*logger.error("Error occured while fetching the transaction details for the account : "
                        + accStmtObj.getF_ACCOUNTID() + "\n" + ex.getMessage());*/
            	logger.error(ExceptionUtil.getExceptionAsString(ex));
                return false;
            }
        }
        else {
            return false;
        }

        if (transList == null)
            return false;
        Date OpeningDate = null;
        BigDecimal openingBalance = BigDecimal.ZERO;
        BigDecimal closingBalance = BigDecimal.ZERO;
        IBOAccount accObj = null;
        if (swtCustObj.getF_SWTACTIVE().compareTo("Y") == 0 && swtCustObj.getF_STATEMENTMSGREQUIRED().compareTo("Y") == 0) {
            accObj = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accStmtObj.getF_ACCOUNTID());
            OpeningDate = accStmtObj.getF_LASTSTMTDATE();

            params.clear();
            params.add(accStmtObj.getF_LASTSTMTDATE());
            params.add(accStmtObj.getF_ACCOUNTID());

            openingBalance = getOpeningBalance(params, env);

        }
        closingBalance = BigDecimal.ZERO;
        String crdrMark = SWT_MT940950Constants.CREDITMARK;
        UB_MT940950 statements = new UB_MT940950();
        ArrayList msgList = new ArrayList();
        /* statements.setMessageType("MT" + accStmtObj.getF_SWTMESSAGETYPE()); */
        String senderBICCode = Branch_BICCodeMap.get(getF_IN_BranchSortCode()).toString();
        if (senderBICCode != null) {
            statements.setSender(senderBICCode);
        }
        statements.setReceiver(swtCustObj.getF_BICCODE());
        statements.setTransactionReferenceNumber(accStmtObj.getF_ACCOUNTID());
               
        if(BICCODE_25P != null)     
        statements.setAccountIdentificationP(accStmtObj.getF_ACCOUNTID()+"$"+BICCODE_25P);
        else
        statements.setAccountIdentification(accStmtObj.getF_ACCOUNTID());
        
        if (closingBalance.compareTo(BigDecimal.ZERO) >= 0) {
            crdrMark = SWT_MT940950Constants.CREDITMARK;
        }

        String amount = CommonConstants.EMPTY_STRING;
        amount = openingBalance.setScale(util.noDecimalPlaces(accObj.getF_ISOCURRENCYCODE(), env), BigDecimal.ROUND_HALF_UP).abs()
                .toString();
        String amount1 = crdrMark + new java.sql.Date(OpeningDate.getTime()).toString() + accObj.getF_ISOCURRENCYCODE() + amount;
        String closingamount = crdrMark
                + new java.sql.Date(SystemInformationManager.getInstance().getBFBusinessDate().getTime()).toString()
                + accObj.getF_ISOCURRENCYCODE() + amount;
        statements.setOpeningBalance(amount1);
        statements.setClosingBalance(closingamount);

        statements.setStatementNumber(accStmtObj.getF_LASTSTMTNUMBER() + 1 + "/" + 1);
        statements.setOpeningBalanceOption(SWT_MT940950Constants.TERMINALBALANCEMARK);
        statements.setClosingBalanceOption(SWT_MT940950Constants.TERMINALBALANCEMARK);

        /*
         * if (transList.size() == 0) {
         * 
         * Check if statment is required when there are no transactions
         * 
         * 
         * if (accStmtObj.getF_SWTTRANSSTMTFLAG().equalsIgnoreCase("Y")) { // Create empty tags for
         * repeating values ForwardBalanceInfo forwardBalanceInfo = new ForwardBalanceInfo();
         * statements.addForwardBalance(forwardBalanceInfo); StatementSingleInfo oneLine = new
         * StatementSingleInfo(); statements.addSingleLine(oneLine); StatementInfo statementInfo =
         * new StatementInfo(); statements.addStatement(statementInfo); msgList.clear();
         * msgList.add(statements); messagePublisher(msgList, env); } else / return false to signify
         * that no statement were generated
         * 
         * return false; }
         */
        Date ClossingDate = null;
        int msgSize = 0;
        int i = 0;
        int msgNo = 0;
        for (i = 0; i < transList.size(); i++) {
            msgSize = 0;

            /*
             * statements.setMessageType("MT" + accStmtObj.getF_SWTMESSAGETYPE()); msgSize +=
             * accStmtObj.getF_SWTMESSAGETYPE().length();
             */
            senderBICCode = Branch_BICCodeMap.get(getF_IN_BranchSortCode()).toString();
            if (senderBICCode != null) {
                statements.setSender(senderBICCode);
                msgSize += senderBICCode.length();
            }
            statements.setReceiver(swtCustObj.getF_BICCODE());
            msgSize += swtCustObj.getF_BICCODE().length();

            statements.setTransactionReferenceNumber(accStmtObj.getF_ACCOUNTID());
            msgSize += accStmtObj.getF_ACCOUNTID().length();

            if(BICCODE_25P != null)     
                statements.setAccountIdentificationP(accStmtObj.getF_ACCOUNTID()+"$"+BICCODE_25P);
            else
                statements.setAccountIdentification(accStmtObj.getF_ACCOUNTID());
                      
            msgSize += accStmtObj.getF_ACCOUNTID().length();

            // increment the number of messsages counter on this account
            msgNo += 1;

            String stmtNumber1 = (accStmtObj.getF_LASTSTMTNUMBER() + 1) + "/" + msgNo;
            statements.setStatementNumber(stmtNumber1);

            msgSize += stmtNumber1.length();

            closingBalance = openingBalance;
            crdrMark = SWT_MT940950Constants.DEBITMARK;
            if (openingBalance.compareTo(BigDecimal.ZERO) >= 0) {
                crdrMark = SWT_MT940950Constants.CREDITMARK;
            }

            if (i > 0) {
                OpeningDate = ClossingDate;
            }

            amount = openingBalance.setScale(util.noDecimalPlaces(accObj.getF_ISOCURRENCYCODE(), env), BigDecimal.ROUND_HALF_UP)
                    .abs().toString();
            if(OpeningDate != null) {
            	amount = crdrMark.concat(new java.sql.Date(OpeningDate.getTime()).toString()).concat(accObj.getF_ISOCURRENCYCODE())
                        .concat(amount);
            }            
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

            IBOTransaction transObj = null;
            ForwardBalanceInfo forwardBalanceInfo = new ForwardBalanceInfo();
            statements.addForwardBalance(forwardBalanceInfo);

            /*
             * if (accStmtObj.getF_SWTMESSAGETYPE().equals(SWT_MT940950Constants.MESSAGENUMBER940))
             * { statements.addSingleLine(oneLine); } else { statements.addStatement(statementInfo);
             * }
             */

            do {
                transObj = (IBOTransaction) transList.get(i);

                closingBalance = checkClosingBalance(accStmtObj, closingBalance, transObj);
                String statementLine = createStatement(transObj, env, accObj.getF_ISOCURRENCYCODE());
                statementLine = statementLine.concat(transObj.getF_REFERENCE()).concat("//").concat(getNumber(i));
                statementLine = statementLine.replaceAll(":", CommonConstants.EMPTY_STRING);
                statementLine = statementLine.replaceAll("%", "PCT.");
                statementLine = statementLine.replaceAll("!", ".");

                openingBalance = closingBalance;
                transObj.getF_VALUEDATE();
                msgSize += statementLine.length();

                /*
                 * if
                 * (accStmtObj.getF_SWTMESSAGETYPE().equals(SWT_MT940950Constants.MESSAGENUMBER940))
                 * { statementInfo = new StatementInfo();
                 * statementInfo.setStatementLine(statementLine);
                 * 
                 * String narration = transObj.getF_NARRATION(); if (narration.length() > 65) {
                 * narration = narration.substring(0, 65) + "$" + narration.substring(65,
                 * narration.length()); } narration = narration.replaceAll(":",
                 * CommonConstants.EMPTY_STRING); narration = narration.replaceAll("%", "PCT.");
                 * narration = narration.replaceAll("!", ".");
                 * statementInfo.setInfoToOwner(narration); statements.addStatement(statementInfo);
                 * msgSize += narration.length(); } else { oneLine = new StatementSingleInfo();
                 * oneLine.setStatementLine(statementLine); statements.addSingleLine(oneLine);
                 * 
                 * }
                 */

                // increment index to read next transaction object
                i += 1;

            }
            while (i < transList.size() && (SWT_MT940950Constants.MESSAGESIZE - msgSize) > SWT_MT940950Constants.MINSIZETOPROCEED);
            i--;
            // populate TAG-62
            String closingBalanceString = null;
            // populate CLOSINGBALANCE_62
            crdrMark = null;
            if (closingBalance.compareTo(BigDecimal.ZERO) >= 0) {
                crdrMark = SWT_MT940950Constants.CREDITMARK;
            }
            else {
                crdrMark = SWT_MT940950Constants.DEBITMARK;
            }
            ClossingDate = transObj.getF_VALUEDATE();
            if (i + 1 == transList.size()) {
                statements.setClosingBalanceOption(SWT_MT940950Constants.TERMINALBALANCEMARK);
                closingBalanceString = crdrMark
                        + new java.sql.Date(SystemInformationManager.getInstance().getBFBusinessDate().getTime()).toString()
                        + accObj.getF_ISOCURRENCYCODE()
                        + closingBalance
                                .setScale(util.noDecimalPlaces(accObj.getF_ISOCURRENCYCODE(), env), BigDecimal.ROUND_HALF_UP).abs()
                                .toString();
            }
            else {
                statements.setClosingBalanceOption(SWT_MT940950Constants.INTERMEDIATEBALANCEMARK);
                closingBalanceString = crdrMark
                        + new java.sql.Date(transObj.getF_VALUEDATE().getTime()).toString()
                        + accObj.getF_ISOCURRENCYCODE()
                        + closingBalance
                                .setScale(util.noDecimalPlaces(accObj.getF_ISOCURRENCYCODE(), env), BigDecimal.ROUND_HALF_UP).abs()
                                .toString();
            }

            statements.setClosingBalance(closingBalanceString);

            msgList.add(statements);
            messagePublisher(msgList, env);
            msgList.clear();
        }
        return true;
    }

    private String getNumber(int i) {
        String result = CommonConstants.EMPTY_STRING + i;
        int length = result.length();
        for (int j = length; j < 5; j++)
            result = "0".concat(result);
        return result;
    }

    private String createStatement(IBOTransaction transObj, BankFusionEnvironment env, String currency) {
        StringBuffer statementLine = new StringBuffer();
        String DisposalWhere = "Where " + IBOSWTDisposal.DEALNO + "=?";
        statementLine.append(new java.sql.Date(transObj.getF_VALUEDATE().getTime()).toString());
        statementLine.append(statementLine.substring(2));

        // populate debit/credit mark & the respective amount fields
        String tranAmount = CommonConstants.EMPTY_STRING;
        if (transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.DEBITFLAG)
                && transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.NONREVERSALTRANSACTION) {
            statementLine.append(SWT_MT940950Constants.DEBITMARK);
            tranAmount = transObj.getF_AMOUNTDEBIT().setScale(util.noDecimalPlaces(currency, env), BigDecimal.ROUND_HALF_UP).abs()
                    .toString();
        }
        else if (transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.CREDITFLAG)
                && transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.NONREVERSALTRANSACTION) {
            statementLine.append(SWT_MT940950Constants.CREDITMARK);
            tranAmount = transObj.getF_AMOUNTCREDIT().setScale(util.noDecimalPlaces(currency, env), BigDecimal.ROUND_HALF_UP).abs()
                    .toString();
        }
        else if (transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.DEBITFLAG)
                && transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.REVERSALTRANSACTION) {
            statementLine.append(SWT_MT940950Constants.DEBITREVERSALMARK);
            tranAmount = transObj.getF_AMOUNTDEBIT().setScale(util.noDecimalPlaces(currency, env), BigDecimal.ROUND_HALF_UP).abs()
                    .toString();
        }
        else if (transObj.getF_DEBITCREDITFLAG().equalsIgnoreCase(SWT_MT940950Constants.CREDITFLAG)
                && transObj.getF_REVERSALINDICATOR() == SWT_MT940950Constants.REVERSALTRANSACTION) {
            statementLine.append(SWT_MT940950Constants.CREDITREVERSALMARK);
            tranAmount = transObj.getF_AMOUNTCREDIT().setScale(util.noDecimalPlaces(currency, env), BigDecimal.ROUND_HALF_UP).abs()
                    .toString();
        }
        statementLine.append(currency.substring(2));
        if (tranAmount.indexOf(".") == -1)
            tranAmount = tranAmount.concat(".");
        statementLine.append(tranAmount);
        ArrayList params = new ArrayList();
        String reference = transObj.getF_REFERENCE();
        if (reference.length() > 20)
            reference = reference.substring(0, 19);
        params.add(reference);
        String dealType = "N";
        List disposalList = env.getFactory().findByQuery(IBOSWTDisposal.BONAME, DisposalWhere, params, null);
        if (disposalList != null && disposalList.size() > 0)
            dealType = "S";
        params.clear();
        statementLine.append(dealType);
        // populate swift transaction code
        // Using the Cache of TransactionScreenControl Table for fetching the details.
        MISTransactionCodeDetails mistransDetails;
        try {
            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo()).getMisTransactionCodeDetails(transObj
                    .getF_CODE());

            IBOMisTransactionCodes swtTransConfObj = mistransDetails.getMisTransactionCodes();
            if (swtTransConfObj.getF_SWTELEMSGMNEMONIC().length() <= 0)
                statementLine.append("MSC");
            else statementLine.append(swtTransConfObj.getF_SWTELEMSGMNEMONIC());
        }
        catch (BankFusionException ex) {
            statementLine.append(transObj.getF_CODE());
            logger.error(ExceptionUtil.getExceptionAsString(ex));
        }

        return statementLine.toString();
    }

    private void messagePublisher(ArrayList messageList, BankFusionEnvironment env) {
        SWT_MessagePublisher messagePublisher = new SWT_MessagePublisher(env);
		messagePublisher.setF_IN_MessageMap(messageList);
		logger.info("Publishing XML Message");
		messagePublisher.process(env);
    }

    private boolean stmtRequired(IBOBankStatementFeature accStmtObj, BankFusionEnvironment env) {
        String custCode = null;
        IBOSwtCustomerDetail swtCustObj = null;
        if (accStmtObj != null) {
            try {
                custCode = FinderMethods.findCustomerCodeByAccount(accStmtObj.getF_ACCOUNTID());
                swtCustObj = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME, custCode);
            }
            catch (Exception ex) {
              /*  logger.error("Error occured while fetching the customer details for the account : " + accStmtObj.getF_ACCOUNTID()
                        + "\n" + ex.getMessage());*/
            	logger.error(ExceptionUtil.getExceptionAsString(ex));
                return false;
            }
            if (swtCustObj.getF_SWTACTIVE().compareTo("Y") == 0 && swtCustObj.getF_STATEMENTMSGREQUIRED().compareTo("Y") == 0) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /*
	 *
	 *
	 */

    private BigDecimal getOpeningBalance(ArrayList params, BankFusionEnvironment env) {
        String queryOpeningBalance = "SELECT " + IPersistenceObjectsFactory.SUM_FUNCTION_CODE + "("
                + IBOMovementHistoryFeature.CREDITAMOUNTSSAMEDAY + ")+" + IPersistenceObjectsFactory.SUM_FUNCTION_CODE + "("
                + IBOMovementHistoryFeature.DEBITAMOUNTSSAMEDAY + ") as openingBalance FROM " + IBOMovementHistoryFeature.BONAME
                + " WHERE " + IBOMovementHistoryFeature.MOVEMENTDATE + " <= ? AND " + IBOMovementHistoryFeature.ACCOUNTID + "=?";
        BigDecimal openingBalance = CommonConstants.BIGDECIMAL_ZERO;

        List openingBalanceList;
        openingBalanceList = env.getFactory().executeGenericQuery(queryOpeningBalance, params, null);

        if (!openingBalanceList.isEmpty() && openingBalanceList.get(0) != null) {
            simplePersistentObject = (SimplePersistentObject) openingBalanceList.get(0);
            openingBalance = (BigDecimal) simplePersistentObject.getDataMap().get("openingBalance");
        }
        return openingBalance;
    }

    private BigDecimal checkClosingBalance(IBOBankStatementFeature accStmtObj, BigDecimal closingBalance, IBOTransaction transObj) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        try {
            if ((sdf.parse(sdf.format(new Date(transObj.getF_VALUEDATE().getTime()))).after(sdf.parse(sdf.format(new Date(
                    accStmtObj.getF_LASTSTMTDATE().getTime())))))) {
                closingBalance = closingBalance.add(transObj.getF_AMOUNT());
            }
        }
        catch (ParseException e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        return closingBalance;
    }
}
