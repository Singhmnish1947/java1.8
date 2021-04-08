/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UB_TIP_PostUnprocessedTxnFatom.java,v.1.0,Jun 17, 2009 11:40:12 AM ravir
 *
 */
package com.misys.ub.interfaces.ti.steps;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.lending.LendingUtils;
import com.misys.ub.forex.core.ForexConstants;
import com.misys.ub.interfaces.ti.configuration.UB_TIP_ModuleConfigurationConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_CustomerExternalReferences;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_BRANCH;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_PRODUCT;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIPOSTINGMSG;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIUBPOSTINGMSG;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_PostUnprocessedTxnFatom;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * @author ravir
 * @date Jun 17, 2009
 * @project Universal Banking
 * @Description:
 */

public class PostUnprocessedTIUBTxnFatom extends AbstractUB_TIP_PostUnprocessedTxnFatom {

    private final String MESSAGEID = "MESSAGEID";
    private final String BASEEQUIVALENT = "BASEEQUIVALENT";
    private final String TRANSACTIONCODE = "TRANSACTIONCODE";
    private final String TRANSACTIONDATE = "TRANSACTIONDATE";
    private final String BASECURRENCYCODE = "BASECURRENCYCODE";
    private final String TRANSACTIONREF = "TRANSACTIONREF";
    private final String TRANSACTIONID = "TRANSACTIONID";
    private final String TITRANSACTIONID = "TITRANSACTIONID";
    private final String FORCEPOST = "FORCEPOST";
    private final String PRIMARYID = "PRIMARYID";
    private final String NARRATIVE = "NARRATIVE";
    private final String AMOUNT = "AMOUNT";
    private final String ACTUALAMOUNT = "ACTUALAMOUNT";
    private final String TXNCURRENCYCODE = "TXNCURRENCYCODE";
    private final String SIGN = "SIGN";
    private final String EXCHRATETYPE = "EXCHRATETYPE";
    private final String SERIALNO = "SERIALNO";
    private final String VALUEDATE = "VALUEDATE";
    private final String CHANNELID = "CHANNELID";
    private final int SINGLELEGENTRY_SIZE = 1;
    private final String REASONFORSUSPFORCEPOST = "REASONFORSUSPFORCEPOST";
    private final String ISACCSUSPPOSTED = "ISACCSUSPPOSTED";
    private final String BRANCHNO = "BRANCHNO";
    private final String CUSTOMERCODE = "CUSTOMERCODE";
    private final String TOTALTXNLEGS = "TOTALTXNLEGS";

    private final String UB_TI_MESSAGETYPE = "GZH971";
    private final String UB_TI_PROCESSING_STATUS = "R";
    private final String UB_TI_CHANNEL_ID = "TI";
    private final String TXN_ID = "TXNID";
    private final String POSTINGDATE = "POSTINGDATE";
    private final String DEFAULT_EXCHANGE_RATE_TYPE = "SPOT";
    private boolean noOfRowsZero = false;

    private String GUID = CommonConstants.EMPTY_STRING;

    public PostUnprocessedTIUBTxnFatom(BankFusionEnvironment env) {
        // TODO Auto-generated constructor stub
        super(env);
    }

    private final String fetchHeaderTxnDetailsQuery = "SELECT DISTINCT " + IBOUB_INF_MessageHeader.MESSAGEID2 + " AS TXNID ,"
            + IBOUB_INF_MessageHeader.MSGRECEIVEDTTM + " AS POSTINGDATE  FROM "
            + IBOUB_INF_MessageHeader.BONAME + " WHERE " + IBOUB_INF_MessageHeader.MESSAGETYPE + "= ?" + " AND "
            + IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ?" + " AND " + IBOUB_INF_MessageHeader.CHANNELID + " = ?";

    private final String fetchMessageHeaderforTxnID = " WHERE " + IBOUB_INF_MessageHeader.MESSAGEID2 + " = ? AND "
            + IBOUB_INF_MessageHeader.MESSAGESTATUS + " = 'R' AND UB_TODATE( " + IBOUB_INF_MessageHeader.MSGRECEIVEDTTM
            + ") = ? ";

    private final String fetchUBBranchSortCodeForTIBranchCode = " WHERE " + IBOUB_INF_BRANCH.BRANCHCODE + " = ?  AND "
            + IBOUB_INF_BRANCH.CHANNELID + " = '" + UB_TI_CHANNEL_ID + "'";


    private final String fetchUBProductDetailsForTIAccountType = " WHERE " + IBOUB_INF_PRODUCT.TIPRODUCT + " = ? ";

    private final String fetchUBCustomerCodeFromTICustomerCode = " WHERE "
            + IBOUB_CNF_CustomerExternalReferences.EXTERNALREFERENCENUM + " =  ? AND "
            + IBOUB_CNF_CustomerExternalReferences.EXTERNALAPPNAME + " = '" + UB_TI_CHANNEL_ID + "'";


    private static final transient Log logger = LogFactory.getLog(PostUnprocessedTIUBTxnFatom.class.getName());

    public void process(BankFusionEnvironment env) throws BankFusionException {

        try {

            String creditTxnCode = UB_TIP_ModuleConfigurationConstants.getUBTICreditTxnCode();
            String debitTxnCode = UB_TIP_ModuleConfigurationConstants.getUBTIDebitTxnCode();

            ArrayList params = new ArrayList();

            params.add(UB_TI_MESSAGETYPE);
            params.add(UB_TI_PROCESSING_STATUS);
            params.add(UB_TI_CHANNEL_ID);
            IBOUB_TIP_TIUBPOSTINGMSG postingDetails = null;

            String debitSuspensePsyNym = UB_TIP_ModuleConfigurationConstants.getUBTIDebitSuspensePsyNym();
            String creditSuspensePsyNym = UB_TIP_ModuleConfigurationConstants.getUBTICreditSuspensePsyNym();
            String systemSuspensePsyNym = UB_TIP_ModuleConfigurationConstants.getSystemSuspensePsyNym();
            String suspenseAccountContext = UB_TIP_ModuleConfigurationConstants.getUBTISuspenseContext();

            List messageHeaderTxnList = getFactory().executeGenericQuery(fetchHeaderTxnDetailsQuery, params, null);
            params.clear();

            if (messageHeaderTxnList.size() != 0) {
                Iterator messageHeadertransactionItr = messageHeaderTxnList.iterator();
                while (messageHeadertransactionItr.hasNext()) {
                    boolean skipTransactionFlag = false;
                    String txnID = CommonConstants.EMPTY_STRING;
                    SimplePersistentObject simpleObject = null;
                    Date postingDate = SystemInformationManager.getInstance().getBFSystemDate();

                    Map resultMap = new HashMap();

                    SimplePersistentObject transactionItem = (SimplePersistentObject) messageHeadertransactionItr.next();
                    txnID = (String) transactionItem.getDataMap().get(TXN_ID);
                    postingDate = LendingUtils.GetSQLDateWithZeroTime((Date) transactionItem.getDataMap().get(POSTINGDATE));
                    

                    GUID = GUIDGen.getNewGUID();
                    ArrayList tiubpostingParams = new ArrayList();
                    tiubpostingParams.add(txnID);
                    tiubpostingParams.add(postingDate);

                    List messageHDRTXNListforTXNID = env.getFactory().findByQuery(IBOUB_INF_MessageHeader.BONAME,
                            fetchMessageHeaderforTxnID, tiubpostingParams, null, true);
                    String txnNarrative = CommonConstants.EMPTY_STRING;
                    String tiTransactionID = CommonConstants.EMPTY_STRING;
                    BigDecimal debitamount = CommonConstants.BIGDECIMAL_ZERO;
                    BigDecimal creditamount = CommonConstants.BIGDECIMAL_ZERO;
                    Iterator eachLegsMessageHeaderDetails = messageHDRTXNListforTXNID.iterator();
                    while (eachLegsMessageHeaderDetails.hasNext()) {

                        BigDecimal tiAmountForUB = CommonConstants.BIGDECIMAL_ZERO;
                        IBOUB_INF_MessageHeader currentMSGHeaderLeg = (IBOUB_INF_MessageHeader) eachLegsMessageHeaderDetails.next();
                        String messageID1PK = currentMSGHeaderLeg.getBoID();
                       
                        IBOUB_TIP_TIPOSTINGMSG getTIPostingtxnReference = (IBOUB_TIP_TIPOSTINGMSG) env.getFactory().findByPrimaryKey(IBOUB_TIP_TIPOSTINGMSG.BONAME, messageID1PK,true);
                        if(getTIPostingtxnReference!=null) {
                            txnNarrative = getTIPostingtxnReference.getF_NARRATIVE();
                            tiTransactionID = getTIPostingtxnReference.getF_TRANSACTIONID();
                        }

                        params.clear();
                        params.add(messageID1PK);
                        try {
                            IBOUB_TIP_TIUBPOSTINGMSG getTIUBPostingtxnReference = (IBOUB_TIP_TIUBPOSTINGMSG) env.getFactory()
                                    .findByPrimaryKey(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, messageID1PK, true);
                            resultMap.clear();
                            resultMap.put(TITRANSACTIONID, tiTransactionID);
                            resultMap.put(NARRATIVE, txnNarrative);
                            resultMap.put(TRANSACTIONID, GUID);
                            if(getTIUBPostingtxnReference!=null) {
                                if (getTIUBPostingtxnReference.getF_TOTALTXNLEGS() == UB_TIP_ModuleConfigurationConstants.totalTxnLegs) {
                                    
                                    postSingleLegTIEODTxn(getTIUBPostingtxnReference, BankFusionThreadLocal.getBankFusionEnvironment());
                                    resultMap.clear();
                                    skipTransactionFlag = true;
                                    break;
                                }
                            }

                        }
                        catch (Exception exp) {
                            if (logger.isInfoEnabled()) {
                                logger.info("TIUB Posting table does not have entries Create one ");
                            }

                            if (getTIPostingtxnReference.getF_TOTALTXNLEGS() == UB_TIP_ModuleConfigurationConstants.totalTxnLegs) {
                                skipTransactionFlag = true;
                                break;
                            }
                            Map createTIUBTxnLegs = new HashMap();
                            String accountID = CommonConstants.EMPTY_STRING;
                            String ubBranchSortCode = CommonConstants.EMPTY_STRING;
                            String ubCustomerCode = CommonConstants.EMPTY_STRING;
                            String sign = CommonConstants.EMPTY_STRING;
                            String txnCode = CommonConstants.EMPTY_STRING;
                            String susPensePseudoName = CommonConstants.EMPTY_STRING;
                            if (getTIPostingtxnReference.getF_CUSTOMERCODE().trim().length() > CommonConstants.INTEGER_ZERO) {
                                params.clear();
                                params.add(getTIPostingtxnReference.getF_CUSTOMERCODE());
                                ubCustomerCode = ((IBOUB_CNF_CustomerExternalReferences) (env.getFactory().findByQuery(
                                        IBOUB_CNF_CustomerExternalReferences.BONAME, fetchUBCustomerCodeFromTICustomerCode, params,
                                        null)).get(0)).getF_CUSTOMERCODE().toString();
                            }
                            else {
                                ubCustomerCode = CommonConstants.EMPTY_STRING;
                            }
                            params.clear();
                            params.add(getTIPostingtxnReference.getF_BRANCHNO());
                            @SuppressWarnings("FBPE")
                            List branchDetails = env.getFactory().findByQuery(IBOUB_INF_BRANCH.BONAME,
                                    fetchUBBranchSortCodeForTIBranchCode, params, null);
                            List<IBOCurrency> currencyDetails = new ArrayList<>();
                            IBOCurrency currencyDetail = null;
                            if(StringUtils.isNotEmpty(getTIPostingtxnReference.getF_TXNCURRENCYCODE())) {
                                
                                currencyDetail = CurrencyUtil.getCurrencyDetailsOfCurrentZone(getTIPostingtxnReference.getF_TXNCURRENCYCODE());
                            }
                            if(currencyDetail!=null) {
                                currencyDetails.add(currencyDetail);
                            }

                            if (branchDetails.size() == CommonConstants.INTEGER_ZERO
                                    || currencyDetails.size() == CommonConstants.INTEGER_ZERO) {

                                skipTransactionFlag = true;
                                break;
                            }

                            try {
                                ubBranchSortCode = ((IBOUB_INF_BRANCH) (branchDetails).get(0)).getF_UBBRANCHSORTCODE().toString();
                                sign = getSign(getTIPostingtxnReference);
                                susPensePseudoName = getSuspensePseudoNym(sign, debitSuspensePsyNym, creditSuspensePsyNym);
                                txnCode = getTransactionCode(sign, debitTxnCode, creditTxnCode);
                                Map contextValueMap = new HashMap();
                                contextValueMap.put(ForexConstants.BRANCH_CONTEXT, ubBranchSortCode);
                                contextValueMap.put(ForexConstants.CUSTOMER_CONTEXT, ubCustomerCode);
                                accountID = getAccountIDForTILeg(getTIPostingtxnReference, suspenseAccountContext,
                                        susPensePseudoName, contextValueMap, BankFusionThreadLocal.getBankFusionEnvironment());
                                if (accountID == null || accountID.trim().length() == CommonConstants.INTEGER_ZERO) {

                                    accountID = getSystemSuspenseAccount(systemSuspensePsyNym, suspenseAccountContext,
                                            getTIPostingtxnReference.getF_TXNCURRENCYCODE(), contextValueMap, BankFusionThreadLocal
                                                    .getBankFusionEnvironment());
                                }
                                tiAmountForUB = getAmountForCurrency(getTIPostingtxnReference, currencyDetails);
                                populateTIUBTxnLeg(createTIUBTxnLegs, getTIPostingtxnReference, accountID, sign, txnCode,
                                        tiAmountForUB, ubBranchSortCode, ubCustomerCode);
                                createTIUBPostingTxn(env, createTIUBTxnLegs);
                                resultMap.clear();
                                resultMap.put(TITRANSACTIONID, tiTransactionID);
                                resultMap.put(NARRATIVE, txnNarrative);
                                resultMap.put(TRANSACTIONID, GUID);
                            }
                            catch (Exception accountFinderException) {
                                logger.info("UB Branch Sort Code ->" + ubBranchSortCode);
                                logger.info("UB AccountID  ->" + accountID);
                                logger.info(" Transaction Sign ->" + sign);
                                logger.info("Transaction Code ->" + txnCode);
                                logger.info("UB Amount ->" + tiAmountForUB);
                                skipTransactionFlag = true;
                                break;

                            }
                        }

                    }

                    if (skipTransactionFlag == true) {

                        continue;
                    }

                    getF_OUT_PostingDetails().addAll(new VectorTable(resultMap));

                }
            }
            if (getF_OUT_PostingDetails().size() == 0) {
                noOfRowsZero = true;
                setF_OUT_isNoOfRowsZero(noOfRowsZero);
                logger.info("No Transaction Are Pending ForUB TI Recon");
            }

        }
        catch (Exception exp) {
            noOfRowsZero = true;
            setF_OUT_isNoOfRowsZero(noOfRowsZero);
            logger.info("UB TI Recon Process Failed");
        }
    }

    public void createTIUBPostingTxn(BankFusionEnvironment env, Map createTIUBPostingLegMap) {
        IBOUB_TIP_TIUBPOSTINGMSG updateTItoUBtxn = (IBOUB_TIP_TIUBPOSTINGMSG) BankFusionThreadLocal.getPersistanceFactory()
                .getStatelessNewInstance(IBOUB_TIP_TIUBPOSTINGMSG.BONAME);
        updateTItoUBtxn.setBoID((String) createTIUBPostingLegMap.get(MESSAGEID));
        updateTItoUBtxn.setF_AMOUNT(new BigDecimal(createTIUBPostingLegMap.get(AMOUNT).toString()));
        updateTItoUBtxn.setF_ACTUALAMOUNT(new BigDecimal(createTIUBPostingLegMap.get(ACTUALAMOUNT).toString()));
        updateTItoUBtxn.setF_BASECURRENCYCODE(CommonConstants.EMPTY_STRING);
        updateTItoUBtxn.setF_BASEEQUIVALENT(new BigDecimal(0));
        updateTItoUBtxn.setF_BRANCHNO((String) createTIUBPostingLegMap.get(BRANCHNO));
        updateTItoUBtxn.setF_CUSTOMERCODE((String) createTIUBPostingLegMap.get(CUSTOMERCODE));
        updateTItoUBtxn.setF_EXCHRATETYPE((String) createTIUBPostingLegMap.get(EXCHRATETYPE));
        updateTItoUBtxn.setF_FORCEPOST((Boolean) createTIUBPostingLegMap.get(FORCEPOST));
        updateTItoUBtxn.setF_ISACCSUSPPOSTED((Boolean) createTIUBPostingLegMap.get(ISACCSUSPPOSTED));
        updateTItoUBtxn.setF_NARRATIVE((String) createTIUBPostingLegMap.get(NARRATIVE));
        updateTItoUBtxn.setF_PRIMARYID((String) createTIUBPostingLegMap.get(PRIMARYID));
        updateTItoUBtxn.setF_REASONFORSUSPFORCEPOST(Integer
                .parseInt(createTIUBPostingLegMap.get(REASONFORSUSPFORCEPOST).toString()));
        updateTItoUBtxn.setF_SERIALNO(Integer.parseInt(createTIUBPostingLegMap.get(SERIALNO).toString()));
        updateTItoUBtxn.setF_SIGN((String) createTIUBPostingLegMap.get(SIGN));
        updateTItoUBtxn.setF_TOTALTXNLEGS(Integer.parseInt(createTIUBPostingLegMap.get(TOTALTXNLEGS).toString()));
        updateTItoUBtxn.setF_TRANSACTIONCODE((String) createTIUBPostingLegMap.get(TRANSACTIONCODE));
        updateTItoUBtxn.setF_TRANSACTIONDATE((Timestamp) (createTIUBPostingLegMap.get(TRANSACTIONDATE)));
        updateTItoUBtxn.setF_TRANSACTIONID((String) createTIUBPostingLegMap.get(TRANSACTIONID));
        updateTItoUBtxn.setF_TITRANSACTIONID((String) createTIUBPostingLegMap.get(TITRANSACTIONID));
        updateTItoUBtxn.setF_TRANSACTIONREF((String) createTIUBPostingLegMap.get(TRANSACTIONREF));
        updateTItoUBtxn.setF_TXNCURRENCYCODE((String) createTIUBPostingLegMap.get(TXNCURRENCYCODE));
        updateTItoUBtxn.setF_VALUEDATE((Timestamp) createTIUBPostingLegMap.get(VALUEDATE));
        BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, updateTItoUBtxn);
        BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
    }

    public void getSuspenseAccountTxnDetails(BigDecimal finalContraAmout, IBOUB_TIP_TIUBPOSTINGMSG postingDetails, Map contraMap,
            String currencyCode, String sign, String txnCode, String accountID, int sequenceNo) {

        contraMap.put(MESSAGEID, GUID);
        contraMap.put(BASEEQUIVALENT, postingDetails.getF_BASEEQUIVALENT());
        contraMap.put(TRANSACTIONCODE, txnCode);
        contraMap.put(TRANSACTIONDATE, postingDetails.getF_TRANSACTIONDATE());
        contraMap.put(BASECURRENCYCODE, postingDetails.getF_BASECURRENCYCODE());
        contraMap.put(TRANSACTIONREF, postingDetails.getF_TRANSACTIONREF());
        contraMap.put(TRANSACTIONID, GUID);
        contraMap.put(TITRANSACTIONID, postingDetails.getF_TITRANSACTIONID());
        contraMap.put(FORCEPOST, postingDetails.isF_FORCEPOST());
        contraMap.put(PRIMARYID, accountID);
        contraMap.put(NARRATIVE, postingDetails.getF_NARRATIVE());
        contraMap.put(AMOUNT, finalContraAmout);
        contraMap.put(TXNCURRENCYCODE, currencyCode);
        contraMap.put(SIGN, sign);
        contraMap.put(EXCHRATETYPE, DEFAULT_EXCHANGE_RATE_TYPE);
        contraMap.put(SERIALNO, new Integer(sequenceNo));
        contraMap.put(VALUEDATE, postingDetails.getF_VALUEDATE());
        contraMap.put(CHANNELID, UB_TI_CHANNEL_ID);

    }

    private IPersistenceObjectsFactory getFactory() {

        return BankFusionThreadLocal.getPersistanceFactory();
    }

    private Map setPseudonymContexts(IBOUB_TIP_TIUBPOSTINGMSG postingMsgLeg) {
        Map<String, String> contextValues = new HashMap<String, String>();
        contextValues.put(ForexConstants.BRANCH_CONTEXT, postingMsgLeg.getF_BRANCHNO());
        contextValues.put(ForexConstants.CUSTOMER_CONTEXT, postingMsgLeg.getF_CUSTOMERCODE());
        return contextValues;
    }

    private Map setPseudonymContextsForZeroLegs(String branchNO, String customerCode) {
        Map<String, String> contextValues = new HashMap<String, String>();
        contextValues.put(ForexConstants.BRANCH_CONTEXT, branchNO);
        contextValues.put(ForexConstants.CUSTOMER_CONTEXT, customerCode);
        return contextValues;
    }

    public static String getAccountonContext(String pseudonymToUse, String pseudonymContext, String isoCurrencyCode,
            Map contextValues, BankFusionEnvironment env) throws BankFusionException {

        String contextValue;
        List<IBOAttributeCollectionFeature> accountItems = null;

        if (pseudonymContext.equals(ForexConstants.CURRENCY_CONTEXT)) {
            contextValue = isoCurrencyCode;
        }
        else {
            contextValue = (String) contextValues.get(pseudonymContext);
        }
        accountItems = FinderMethods.findAccountByPseudoname(pseudonymToUse, isoCurrencyCode, pseudonymContext, contextValue, env,
                null);

        if (accountItems.size() != CommonConstants.INTEGER_ZERO) {
            return accountItems.get(0).getBoID();
        }
        else {

            return CommonConstants.EMPTY_STRING;
        }

    }

    public void populateTIUBTxnLeg(Map tiUBPostingLegMap, IBOUB_TIP_TIPOSTINGMSG tipostingLegs, String accountID, String sign,
            String txnCode, BigDecimal amount, String ubbranchSortCode, String ubCustomerCode) {
        tiUBPostingLegMap.put(MESSAGEID, tipostingLegs.getBoID());
        tiUBPostingLegMap.put(TRANSACTIONCODE, txnCode);
        tiUBPostingLegMap.put(TRANSACTIONDATE, tipostingLegs.getF_TRANSACTIONDTTM());
        tiUBPostingLegMap.put(TRANSACTIONREF, tipostingLegs.getF_TRANSACTIONREF());
        tiUBPostingLegMap.put(TRANSACTIONID, GUID);
        tiUBPostingLegMap.put(TITRANSACTIONID, tipostingLegs.getF_TRANSACTIONID());
        tiUBPostingLegMap.put(FORCEPOST, tipostingLegs.isF_ISFORCEPOST());
        tiUBPostingLegMap.put(PRIMARYID, accountID);
        tiUBPostingLegMap.put(NARRATIVE, tipostingLegs.getF_NARRATIVE());
        tiUBPostingLegMap.put(AMOUNT, amount);
        tiUBPostingLegMap.put(ACTUALAMOUNT, amount);
        tiUBPostingLegMap.put(TXNCURRENCYCODE, tipostingLegs.getF_TXNCURRENCYCODE());
        tiUBPostingLegMap.put(SIGN, sign);
        tiUBPostingLegMap.put(EXCHRATETYPE, DEFAULT_EXCHANGE_RATE_TYPE);
        tiUBPostingLegMap.put(SERIALNO, tipostingLegs.getF_SERIALNO());
        tiUBPostingLegMap.put(VALUEDATE, tipostingLegs.getF_VALUEDTTM());
        tiUBPostingLegMap.put(REASONFORSUSPFORCEPOST, ChannelsEventCodes.I_POSTED_TO_SUSPENSE_ACCOUNT);
        tiUBPostingLegMap.put(ISACCSUSPPOSTED, true);
        tiUBPostingLegMap.put(BRANCHNO, ubbranchSortCode);
        tiUBPostingLegMap.put(CUSTOMERCODE, ubCustomerCode);
        tiUBPostingLegMap.put(TOTALTXNLEGS, tipostingLegs.getF_TOTALTXNLEGS());

    }

    public String getAccountIDForTILeg(IBOUB_TIP_TIPOSTINGMSG tiLeg, String suspenseAccountContext, String suspensePsyNym,
            Map contextMap, BankFusionEnvironment env) throws BankFusionException {

        // String accountID = tiLeg.getF_PRIMARYID();
        //
        // try {
        // IBOAccount accountDetails = (IBOAccount) getFactory().findByPrimaryKey(IBOAccount.BONAME,
        // accountID);
        // }
        // catch (Exception exp) {
        // logger.info("The TI leg PRIMARYID is not an account its a PseudoName");
        //
        // accountID = getAccountonContext(suspensePsyNym, suspenseAccountContext,
        // tiLeg.getF_TXNCURRENCYCODE(), contextMap, env);
        // }

        return getAccountonContext(suspensePsyNym, suspenseAccountContext, tiLeg.getF_TXNCURRENCYCODE(), contextMap, env);
    }

    public String getTxnLegContext(String accountType, String suspenseAccountContext) {

        String context = CommonConstants.EMPTY_STRING;

        ArrayList params = new ArrayList();
        params.add(accountType);
        List productDetailsForTI = getFactory().findByQuery(IBOUB_INF_PRODUCT.BONAME, fetchUBProductDetailsForTIAccountType,
                params, null);
        if (productDetailsForTI.size() == 0) {

            context = suspenseAccountContext;

        }
        else {
            context = ((IBOUB_INF_PRODUCT) (productDetailsForTI.get(0))).getF_TISORTCONTEXT().toString();
        }

        return context;

    }

    public String getSign(IBOUB_TIP_TIPOSTINGMSG tiLeg) {

        String sign = CommonConstants.EMPTY_STRING;

        if (tiLeg.getF_AMOUNTSIGN().equals("-")) {

            sign = "-";

        }
        else {

            sign = "+";
        }

        return sign;

    }

    public String getSuspensePseudoNym(String sign, String debitSusPsyNym, String creditSusPsyNym) {

        String suspensePseudoName = CommonConstants.EMPTY_STRING;

        if (sign.equals("-")) {
            suspensePseudoName = debitSusPsyNym;
        }
        else {

            suspensePseudoName = creditSusPsyNym;
        }

        return suspensePseudoName;
    }

    public String getTransactionCode(String sign, String debitTransactionCode, String creditTransactionCode) {

        String txnCode = CommonConstants.EMPTY_STRING;

        if (sign.equals("-")) {
            txnCode = debitTransactionCode;
        }
        else {

            txnCode = creditTransactionCode;
        }

        return txnCode;
    }

    public BigDecimal getAmountForCurrency(IBOUB_TIP_TIPOSTINGMSG tiLeg, List currencyDetails) {

        BigDecimal amountForCurrency = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal tiAmount = tiLeg.getF_AMOUNT();
        int currencyScale = ((IBOCurrency) currencyDetails.get(0)).getF_CURRENCYSCALE();

        amountForCurrency = getAmountWithDecimal(tiAmount, currencyScale);

        return amountForCurrency;
    }

    public BigDecimal getAmountWithDecimal(BigDecimal tiAmount, int currencyScale) {

        return tiAmount.movePointLeft(currencyScale);
    }

    public String getSystemSuspenseAccount(String systemSuspPsyNym, String suspenseAccountContext, String isoCurrencyCode,
            Map contextMap, BankFusionEnvironment env) {

        return getAccountonContext(systemSuspPsyNym, suspenseAccountContext, isoCurrencyCode, contextMap, env);
    }

    private void postSingleLegTIEODTxn(IBOUB_TIP_TIUBPOSTINGMSG singleLegTIEODTxn, BankFusionEnvironment env) {

        SyncSingleLegUBTxnFatom singleLegUBTxnFatom = new SyncSingleLegUBTxnFatom(env);
        singleLegUBTxnFatom.setF_IN_INACCOUNTID(singleLegTIEODTxn.getF_PRIMARYID());
        singleLegUBTxnFatom.setF_IN_INAMOUNT(singleLegTIEODTxn.getF_AMOUNT().toString());
        singleLegUBTxnFatom.setF_IN_INAMOUNTSIGN(singleLegTIEODTxn.getF_SIGN());
        singleLegUBTxnFatom.setF_IN_INBASEEQUIVAMOUNT(singleLegTIEODTxn.getF_BASEEQUIVALENT());
        singleLegUBTxnFatom.setF_IN_INBASEEQUIVCURRENCYCODE(singleLegTIEODTxn.getF_BASECURRENCYCODE());
        singleLegUBTxnFatom.setF_IN_INBRANCHNO(singleLegTIEODTxn.getF_BRANCHNO());
        singleLegUBTxnFatom.setF_IN_INCUSTOMERCODE(singleLegTIEODTxn.getF_CUSTOMERCODE());
        singleLegUBTxnFatom.setF_IN_INISACCSUSPPOSTED(singleLegTIEODTxn.isF_ISACCSUSPPOSTED());
        singleLegUBTxnFatom.setF_IN_INISFORCEPOST(singleLegTIEODTxn.isF_FORCEPOST());
        singleLegUBTxnFatom.setF_IN_INMESSAGEID1PK(singleLegTIEODTxn.getBoID());
        singleLegUBTxnFatom.setF_IN_INNARRATIVE(singleLegTIEODTxn.getF_NARRATIVE());
        singleLegUBTxnFatom.setF_IN_INREASONFORSUSPFORCEPOST(singleLegTIEODTxn.getF_REASONFORSUSPFORCEPOST());
        singleLegUBTxnFatom.setF_IN_INSERIALNO(singleLegTIEODTxn.getF_SERIALNO());
        singleLegUBTxnFatom.setF_IN_INTITRANSACTIONID(singleLegTIEODTxn.getF_TITRANSACTIONID());
        singleLegUBTxnFatom.setF_IN_INTOTALTXNLEGS(singleLegTIEODTxn.getF_TOTALTXNLEGS());
        singleLegUBTxnFatom.setF_IN_INTRANSACTIONCODE(singleLegTIEODTxn.getF_TRANSACTIONCODE());
        singleLegUBTxnFatom.setF_IN_INTRANSACTIONDTTM(singleLegTIEODTxn.getF_TRANSACTIONDATE());
        singleLegUBTxnFatom.setF_IN_INTRANSACTIONREF(singleLegTIEODTxn.getF_TRANSACTIONREF());
        singleLegUBTxnFatom.setF_IN_INTXNCURRENCYCODE(singleLegTIEODTxn.getF_TXNCURRENCYCODE());
        singleLegUBTxnFatom.setF_IN_INVALUEDTTM(singleLegTIEODTxn.getF_VALUEDATE());
        singleLegUBTxnFatom.process(env);

    }
}
