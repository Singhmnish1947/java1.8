/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BPW_FN04_51_PostingEngine.java,v.1.0,Oct 27, 2009 7:52:36 PM Satish.KC
 *
 * $Log: BPW_FN04_51_PostingEngine.java,v $
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.cbs.common.functions.CB_CMN_SetProperty;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.forex.configuration.ForexModuleConfiguration;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBPW_OfflinePostings;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOInterestApplicationMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.CurrencyValue;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.PostingEngineConstants;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItemStatusCodes;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.repository.IRepositoryService;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.scheduler.BPWFailedOfflineItem;
import com.trapedza.bankfusion.scheduler.item.ItemStatusCodes;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.utils.FatomUtils;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_FN04_51_PostingEngine;
import com.trapedza.bankfusion.steps.refimpl.IBPW_FN04_51_PostingEngine;
import com.trapedza.bankfusion.systeminformation.PostingMessageConstants;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * @author Satish.KC
 * @date Oct 27, 2009
 * @project Universal Banking
 * @Description: BPW_FN04_51_PostingEngine will bipass the request to the posting engine and if
 *               posting engine fails to process, then BPW_FN04_51_PostingEngine will records the
 *               entry into the table.
 */
public class BPW_FN04_51_PostingEngine extends AbstractBPW_FN04_51_PostingEngine implements IBPW_FN04_51_PostingEngine {
    private static final IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
            .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
    /**
     * logger veriable
     */
    private transient final static Log logger = LogFactory.getLog(BPW_FN04_51_PostingEngine.class.getName());

    /**
     * event action constant for "stop"
     */

    /**
     * Where clause for transaction history record retrieval
     */

    /**
     * IRepositoryService instance.
     */
    IRepositoryService repositoryService = (IRepositoryService) ServiceManager.getService(ServiceManager.REPOSITORY_SERVICE);

    /**
     * Posting engine veriables for posting router.
     */

    private static String SYS_POSITION_CONTEXT = "CONTEXT_POSITIONACCOUNT";

    /** <code>SYS_MODULE_CONFIG_KEY</code> = SYS. */
    private static String SYS_MODULE_CONFIG_KEY = "SYS";
    private String acctCurrencyCode = CommonConstants.EMPTY_STRING;
    /** <code>CURRENCY_CONTEXT</code> = CURRENCY. */
    private static String CURRENCY_CONTEXT = "CURRENCY";
    /** <code>BRANCH_CONTEXT</code> = BRANCH. */
    private static String BRANCH_CONTEXT = "BRANCH";

    private IBOAttributeCollectionFeature accountDataValues;
    private boolean isSettlementRequired = true; 
    private String accountID = CommonConstants.EMPTY_STRING;
    private char messageType = 'N';
    private String peRouterProfileID = CommonConstants.EMPTY_STRING;
    private String productID = CommonConstants.EMPTY_STRING;
    private static BankFusionEnvironment environment;

    private boolean isForwardValued;

    private String branchID = CommonConstants.EMPTY_STRING;
    private String transactionReference = CommonConstants.EMPTY_STRING;
    private String runtimeBPID = CommonConstants.EMPTY_STRING;
    private String userName = CommonConstants.EMPTY_STRING;
    private String authenticatingUserName = CommonConstants.EMPTY_STRING;
    private String transCode = CommonConstants.EMPTY_STRING;
    private String shortName = CommonConstants.EMPTY_STRING;
    private Date today;
    private String createLoanRepay = " ";
    private String transactionID = CommonConstants.EMPTY_STRING;
    BigDecimal baseEquivalent = CommonConstants.BIGDECIMAL_ZERO;

    /**
     * @param env
     */
    public BPW_FN04_51_PostingEngine(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * @param env
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractBPW_FN04_51_PostingEngine.process(
     *      BankFusionEnvironment env) throws BankFusionException
     */
    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        super.process(env);
        environment = env;

        Map<String, Object> dataInMap = getInDataMap();

        // artf577794: Check for boolean whether Txn and Contra account currency are same. True when
        // both currency differs.
        if (isF_IN_iso_code1_Txn_Currecny()) {
            handleBPWFailures(CommonsEventCodes.E_THE_ISO_CURRENCY_CODE_GIVEN_DATA_IS_INVALID, dataInMap);
            return;
        }
        boolean debugEnabled = logger.isDebugEnabled();
        try {
            executePostingEngine(env);
        }
        catch (BankFusionException exception) {
            if (debugEnabled) {
                logger.warn("Force post failed with BankFusionException for BPW. Logging the record into BPW offline retry table.");
                logger.warn(exception);
            }
            int eventID = exception.getMessageNumber();
            // String eventAction = EventsHelper.getEventAction(eventID,
            // CommonConstants.EMPTY_STRING, env).trim();
            // if (eventAction.equalsIgnoreCase(EVENT_ACTION_STOP)) {
            handleBPWFailures(eventID, dataInMap);
            // }
        }
        catch (RuntimeException exception) {
            if (debugEnabled) {
                logger.warn("Force post failed with RuntimeException for BPW. Logging the record into BPW offline retry table.");
                logger.warn(exception);
            }
            handleBPWFailures(99, dataInMap);
        }
        catch (Exception exception) {
            if (debugEnabled) {
                logger.warn("Force post failed with Exception  for BPW. Logging the record into BPW offline retry table.");
                logger.warn(exception);
            }
            handleBPWFailures(99, dataInMap);
        }
    }

    /**
     * Based on the exception type return handleBPWFailures will handle the appropriate exceptions
     * and pass the entries to the BPW error handling
     * 
     * @param eventID
     * @param dataInMap
     */
    public static synchronized void handleBPWFailures(int eventID, Map dataInMap) {
        // track the failed records here.
        String txnRef = (String) dataInMap.get("1" + PostingEngineConstants.SEPERATOR
                + PostingEngineConstants.POSTINGMESSAGETRANSACTIONREFERENCE);
        IPersistenceObjectsFactory factory = getPrivateFactory();
        if (factory != null) {
            // rollback the financial posting happenings.
            factory.rollbackTransaction();
            factory.beginTransaction();  //
        }
        // If transactin alreay posted, ignore to populate into the table for item handler.
        if (!isDuplicateTransaction(txnRef)) {
            Hashtable<String, Object> startFatomData = new Hashtable<String, Object>();
            Iterator inputIter = dataInMap.keySet().iterator();
            while (inputIter.hasNext()) {
                String attrKey = inputIter.next().toString();
                Object value = dataInMap.get(attrKey);
                startFatomData.put(attrKey, value);
            }
            updateBPWOffline(startFatomData, txnRef, eventID);
        }
    }

    /**
     * 
     * @param env
     * @throws BankFusionException
     * @throws Exception
     */
    private void executePostingEngine(BankFusionEnvironment env) throws BankFusionException {
        ArrayList<IBOFinancialPostingMessage> financialPostings = getPostingMessage(env);
        VectorTable postingMessages = new VectorTable();
        IBOFinancialPostingMessage postingMessage = null;
        for (int i = 0; i < financialPostings.size(); i++) {
            postingMessage = (IBOFinancialPostingMessage) financialPostings.get(i);
            postingMessages.addAll(new VectorTable(postingMessage.getDataMap()));
        }
        HashMap params = new HashMap();
        params.put("PostingMessages", postingMessages);
        params.put("isBlocking", false);
        params.put("manualValueDate", getValueDate(env));
        params.put("transactionID", transactionID);
        HashMap result = MFExecuter.executeMF("CB_CMN_CollectionPostingMessage_SRV", env, params); 
    }

    /**
     * 
     * @param env
     * @return
     */

    private ArrayList<IBOFinancialPostingMessage> getPostingMessage(BankFusionEnvironment env) {
        ArrayList<IBOFinancialPostingMessage> financialPostings = new ArrayList<IBOFinancialPostingMessage>();
        int legs = ((Integer) getInDataMap().get(PostingEngineConstants.NUMBEROFACCS)).intValue();
        transactionID = GUIDGen.getNewGUID();
        int numberOfLegs = 0;
        for (int msgCount = 1; msgCount <= legs; msgCount++) {
            String messagePrefix = msgCount + PostingEngineConstants.SEPERATOR;
            BigDecimal txnAmount = getAttributeBigDecimal(messagePrefix + PostingEngineConstants.POSTINGMESSAGETRANSACTIONAMOUNT);
            String accountId = getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGEACCOUNTID);
            if (ignoreLeg(msgCount, txnAmount, accountId)) {
                continue;
            }
            IBOFinancialPostingMessage message = (IBOFinancialPostingMessage) BankFusionThreadLocal.getPersistanceFactory()
                    .getStatelessNewInstance(IBOFinancialPostingMessage.BONAME);

            FatomUtils.createStandardItemsMessage(message, env);
            PostingHelper.setDefaultValuesForFinPosting(message, env);

            message.setF_PRIMARYID(getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGEACCOUNTID));
            message.setF_ACTUALAMOUNT(txnAmount);
            message.setF_TRANSACTIONCODE(getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGETRANSACTIONCODE));
            message.setF_TXNCURRENCYCODE(getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGEISOCURRENCYCODE));
            message.setF_CROSSCURRENCY(false);
            message.setF_PAYEEDEPOSITACCOUNT(getAttributeString(PostingEngineConstants.POSTINGMESSAGEMAINACCOUNTPAYEEACCOUNTID));
            message.setF_PAYEEDEPOSITACTION(getAttributeString(PostingEngineConstants.POSTINGMESSAGEMAINACCOUNTPAYEEDEPOSITACTION));
            message.setF_DONTUPDATELIMIT(getAttributeBoolean(messagePrefix + PostingEngineConstants.DONTUPDATELIMITVALUE));
            message.setF_BOOKBALANCE(CommonConstants.BIGDECIMAL_ZERO);
            message.setF_BANKCHEQUEISSUE(getAttributeInteger(PostingEngineConstants.BANKCHEQUEISSUE) == 0 ? false : true);
            message.setF_FORCEPOST(getAttributeBoolean(PostingEngineConstants.FORCE_POST));
            message.setF_EXCHRATETYPE(getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGEEXCHANGERATETYPE));
            message.setF_EXCHRATE(getAttributeBigDecimal(messagePrefix + PostingEngineConstants.POSTINGMESSAGEEXCHANGERATE));
            message.setF_BASEEQUIVALENT(getAttributeBigDecimal(messagePrefix + PostingEngineConstants.POSTINGMESSAGEBASEEQUIVALENT));
            message.setF_FORCEDNOTICE(getAttributeInteger(PostingEngineConstants.FORCEDNOTICETRANSACTION).intValue() == 1);
            message.setF_MATURITYDATE(getAttributeTimestamp(messagePrefix + PostingEngineConstants.POSTINGMESSAGEMATURITYDATE));
            message.setF_PAYEEDEPOSITAMOUNT(getAttributeBigDecimal(PostingEngineConstants.POSTINGMESSAGEMAINACCOUNTPAYEEDEPOSITAMOUNT));
            message.setF_CHEQUEDRAFTNUMBER(getAttributeLong(messagePrefix
                    + PostingEngineConstants.POSTINGMESSAGECHEQUEDRAFTNUMBER));
            String postingAction = getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGEACCOUNTPOSTINGACTION);
            postingAction = postingAction.trim().toUpperCase();
            if (postingAction.equalsIgnoreCase("D")) {
                message.setSign('-');
                message.setF_AMOUNTDEBIT(txnAmount);
            }
            if (postingAction.equalsIgnoreCase("C")) {
                message.setSign('+');
                message.setF_AMOUNTCREDIT(txnAmount);
            }
            message.setF_CHANNELID("BPWR");
            message.setF_NARRATIVE(getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGETRANSACTIONNARRATION));
            message.setF_AMOUNT(txnAmount);
            message.setF_NOTESVALUE(null);
            message.setF_COINSVALUE(null);
            int numberOfCheques = ((Integer) getInDataMap().get(PostingEngineConstants.NUMBEROFCHEQUES)).intValue();
            message.setF_CHEQUECOUNT(numberOfCheques);

            IBOAttributeCollectionFeature account = (IBOAttributeCollectionFeature) BankFusionThreadLocal.getPersistanceFactory()
                    .findByPrimaryKey(IBOAccount.BONAME, message.getF_PRIMARYID(), true);
            message.setF_BOOKBALANCE(account.getF_BOOKEDBALANCE());
            message.setF_PEROUTERPROFILEID(account.getF_PEROUTERPROFILEID());
            message.setF_PRODUCTID(account.getF_PRODUCTID());
            message.setF_ACCTCURRENCYCODE(account.getF_ISOCURRENCYCODE());

            message.setF_BANKCHEQUEISSUE(getAttributeBoolean(messagePrefix + PostingEngineConstants.BANKCHEQUEISSUE));
            String branch = getAttributeString(getAttributeString(PostingEngineConstants.BRANCHSORTCODE));

            if (branch == null || branch.trim().equals(CommonConstants.EMPTY_STRING)) {
                branch = getF_IN_branchSortCode();
                if (branch == null || branch.trim().equals(CommonConstants.EMPTY_STRING)) {
                    branch = BankFusionThreadLocal.getUserSession().getBranchSortCode();
                }
            }
            message.setF_BRANCHSORTCODE(branch);
            message.setValueDate(getValueDate(env));

            message.setForwardValued(PostingHelper.isForwardDatedTransaction(message.getValueDate()));

            message.setTransactionDate(SystemInformationManager.getInstance().getBFBusinessDateTime(env.getRuntimeMicroflowID()));
            message.setF_NOTESVALUE(getAttributeBigDecimal(messagePrefix + PostingEngineConstants.AMOUNTOFNOTES));
            message.setF_COINSVALUE(getAttributeBigDecimal(messagePrefix + PostingEngineConstants.AMOUNTOFCOINS));

            message.setSerialNo(++numberOfLegs);
            message.setBranchID(branch);
            branchID = branch;
            message.setTransactionRef(getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGETRANSACTIONREFERENCE));
            transactionReference = getAttributeString(messagePrefix + PostingEngineConstants.POSTINGMESSAGETRANSACTIONREFERENCE);
            message.setTransactionID(transactionID);

            // transactin iniated by user set to branch power user.
            message.setInitiatedByuserID(getAttributeString("initiatedByUser"));
            message.setAuthenticatingUserID(getAttributeString("authorizedByUser"));
            /* Update the base equivivalent */
            String baseCurrCode = SystemInformationManager.getInstance().getBaseCurrencyCode();
            if (account.getF_ISOCURRENCYCODE().equals(baseCurrCode)) {
                message.setF_BASEEQUIVALENT(txnAmount);
            }
            else if (message.getF_TXNCURRENCYCODE().equals(baseCurrCode)) {
                message.setF_BASEEQUIVALENT(message.getF_ACTUALAMOUNT());
            }
            else if (baseEquivalent.signum() != 0) {
                message.setF_BASEEQUIVALENT(baseEquivalent);
            }
            else if (message.getF_BASEEQUIVALENT().signum() == 0) {
                /*
                 * Passsing the account currency and SPOT if the TXN currency and exchange ratetype
                 * is missing
                 */
                String fromCurrency = CommonConstants.EMPTY_STRING;
                String exchangeRateType = CommonConstants.EMPTY_STRING;
                if ((message.getF_TXNCURRENCYCODE() == null) || (message.getF_TXNCURRENCYCODE() == ""))
                    fromCurrency = message.getAcctCurrencyCode();
                else fromCurrency = message.getAcctCurrencyCode();

                if ((message.getF_EXCHRATETYPE() == null) || (message.getF_EXCHRATETYPE() == ""))
                    exchangeRateType = "SPOT";
                else exchangeRateType = message.getF_EXCHRATETYPE();

                BigDecimal baseExchRate = getExchRate(message.getF_ACTUALAMOUNT(), fromCurrency, baseCurrCode, exchangeRateType,
                        message.getF_USERID(), getEnvironment());
                message.setF_BASEEQUIVALENT(convertCurrencyAmount(message.getF_ACTUALAMOUNT(), baseExchRate, exchangeRateType,
                        fromCurrency, baseCurrCode));
            }
            baseEquivalent = message.getF_BASEEQUIVALENT();

            /* Update the base equivivalent */

            financialPostings.add(message);
        }

        return financialPostings;
    }

    /**
     * 
     * @param dataInMap
     * @param eventID
     * @param txnRef
     */
    private static void updateBPWOffline(Hashtable dataInMap, String txnRef, int eventID) {
        IPersistenceObjectsFactory factory = getPrivateFactory();
        if (factory != null) {
            // insert the failed transactions into UBTB_BPWOFFLINEPOSTINGS table.
            factory.beginTransaction();
            IBOBPW_OfflinePostings offlinePostings = (IBOBPW_OfflinePostings) factory
                    .getStatelessNewInstance(IBOBPW_OfflinePostings.BONAME);

            // artf577794: Cancel the txn, if Txn and Contra account currency are different.
            boolean isTxnAndContraCurrDiff = (Boolean) dataInMap.get(IN_iso_code1_Txn_Currecny);
            if (!isTxnAndContraCurrDiff) {
                offlinePostings.setF_UBSTATUS(IItemStatusCodes.SCHEDULED);
            }
            else {
                offlinePostings.setF_UBSTATUS(ItemStatusCodes.CANCELED);
            }
            offlinePostings.setF_UBLASTPROCESSEDDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
            offlinePostings.setF_UBNEXTPROCESSINGDTTM(getNextProcessingTime());
            offlinePostings.setF_UBERRORNUMBER(String.valueOf(eventID));
            offlinePostings.setF_UBPELEGS(BankFusionIOSupport.convertToBytes(dataInMap));
            offlinePostings.setF_UBTXNREF(txnRef);
            factory.create(IBOBPW_OfflinePostings.BONAME, offlinePostings);
            factory.commitTransaction();
            // go ahead with the actual transaction.
            factory.beginTransaction();
        }
    }

    /**
     * 
     * @param txnRef
     * @return
     */
    private static boolean isDuplicateTransaction(String txnRef) {

        VectorTable m = CB_CMN_SetProperty.run(new VectorTable(), "1", txnRef);

        // Check in TRANSACTION table
        QueryCountFatom queryCount = new QueryCountFatom(environment);
        queryCount.setF_IN_boName(IBOTransaction.BONAME);
        queryCount.setF_IN_whereClause(IBOTransaction.REFERENCE + " = ?");
        queryCount.setF_IN_Params(m);

        queryCount.process(environment);
        int txnCount = queryCount.getF_OUT_rowCount();

        if (txnCount > 0)
            return true;

        // Check in FINANCIALPOSTINGMSG table
        queryCount.setF_IN_boName(IBOFinancialPostingMessage.BONAME);
        queryCount.setF_IN_whereClause(IBOFinancialPostingMessage.TRANSACTIONREF + " = ? ");
        queryCount.setF_IN_Params(m);

        queryCount.process(environment);
        txnCount = queryCount.getF_OUT_rowCount();

        if (txnCount > 0)
            return true;

        return false;
    }

    /*
     * Get a Private Persistence Factory @return IPersistenceObjectsFactory Begins a new transaction
     * on the factory
     */
    private static IPersistenceObjectsFactory getPrivateFactory() {
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        return factory;
    }

    /**
     * Returns the next processing time for BPW offline records.
     * 
     * @return
     */
    private static Timestamp getNextProcessingTime() {
        return new Timestamp(SystemInformationManager.getInstance().getBFBusinessDateTime().getTime()
                + (BPWFailedOfflineItem.RETRY_ITEM_WITH_INTERVAL * 60 * 1000));
    }

    /**
     * 
     * @param attrName
     * @return
     */
    private Integer getAttributeInteger(String attrName) {
        Object obj = getInDataMap().get(attrName);
        if (obj != null) {
            return (Integer) obj;
        }
        else {
            return CommonConstants.INTEGER_ZERO;
        }
    }
    /**
     * 
     * @param attrName
     * @return
     */
    private Long getAttributeLong(String attrName) {
        Object obj = getInDataMap().get(attrName);
        if (obj != null) {
            return (Long) obj;
        }
        else {
            return CommonConstants.LONG_ZERO;
        }
    }

    /**
     * 
     * @param attrName
     * @return
     */
    private BigDecimal getAttributeBigDecimal(String attrName) {
        Object obj = getInDataMap().get(attrName);
        BigDecimal value = null;
        if (obj != null) {
            value = new BigDecimal(obj.toString());
        }
        else {
            value = CommonConstants.BIGDECIMAL_ZERO;
        }
        return value;
    }

    /**
     * 
     * @param attrName
     * @return
     */
    private String getAttributeString(String attrName) {
        Object obj = getInDataMap().get(attrName);
        if (null == obj) {
            return CommonConstants.EMPTY_STRING;
        }
        else {
            return obj.toString();
        }
    }

    /**
     * 
     * @param attrName
     * @return
     */
    private Boolean getAttributeBoolean(String attrName) {
        Object obj = getInDataMap().get(attrName);
        if (null == obj) {
            return Boolean.FALSE;
        }
        else {
            return Boolean.valueOf(obj.toString());
        }
    }

    /**
     * 
     * @param attrName
     * @return
     */
    private Timestamp getAttributeTimestamp(String attrName) {
        Object obj = getInDataMap().get(attrName);
        if (obj != null) {
            return (Timestamp) obj;
        }
        else {
            return new Timestamp(0);
        }
    }

    /**
     * 
     * @param env
     * @return
     */
    private Date getValueDate(BankFusionEnvironment env) {
        Date valueDate = (Date) getInDataMap().get(PostingEngineConstants.MANUALVALUEDATE);
        Time valueTime = new Time(((Timestamp) getInDataMap().get(PostingEngineConstants.MANUALVALUETIME)).getTime());
        
        /* Unreachable Code
        if (valueTime == null)
            valueTime = new Time(0);
		*/
        if (logger.isDebugEnabled()) {
            logger.debug("Value Date : " + valueDate);
            logger.debug("Value Time : " + valueTime);
        }

        if (valueDate == null || valueDate.getTime() == 0) {
            valueDate = SystemInformationManager.getInstance().getBFBusinessDateTime(env.getRuntimeMicroflowID());
        }
        else {
            // value date is getting calculated incorrectly
            // valueDate = new Date(valueDate.getTime() + valueTime.getTime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(valueDate);
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(valueTime);
            cal.set(Calendar.HOUR_OF_DAY, cal1.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal1.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal1.get(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, cal1.get(Calendar.MILLISECOND));
            valueDate = cal.getTime();

        }
        return valueDate;
    }

    /**
     * 
     * @param messagePrefix
     * @param txnAmount
     * @param accountid
     * @return
     */
    private boolean ignoreLeg(int messagePrefix, BigDecimal txnAmount, String accountid) {
        if (accountid == null || accountid.trim().length() == 0) {
            return true;
        }
        Object postingMsgValue = getInDataMap().get(messagePrefix + PostingEngineConstants.IGNORE_ZERO_AMOUNTS);
        boolean ignoreZeroAmount = Boolean.FALSE;
        if (postingMsgValue != null && postingMsgValue instanceof Boolean) {
            ignoreZeroAmount = ((Boolean) postingMsgValue).booleanValue();
        }
        else if (postingMsgValue != null && postingMsgValue instanceof String) {
            ignoreZeroAmount = new Boolean((String) postingMsgValue).booleanValue(); 
        }

        if (txnAmount.signum() == 0 && ignoreZeroAmount) {
            return true;
        }
        else {
            return false;
        }
    }

    protected ArrayList handleCrossCurrency(ArrayList messages) {
        if (logger.isInfoEnabled()) {
            logger.info("BPW_FN04_51: HandleCrossCurrency : BEGIN");
        }

        if (getAttributeInteger(PostingEngineConstants.NUMBEROFACCS).intValue() == 1) {
            return messages;
        }
        // If there is no cross currency or if there is cross currency and the
        // postings balance anyway
        // that will

        Map involvedCurrenciesMap = new HashMap();
        Map baseEquivalentsMap = new HashMap();
        Map exchangeRateMap = new HashMap();

        String spotPseudonym = ForexModuleConfiguration.getSpotPositionPseudonym();
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        String positionAccountContext = (String) ubInformationService.getBizInfo().getModuleConfigurationValue(
                SYS_MODULE_CONFIG_KEY, SYS_POSITION_CONTEXT, null);
        IPostingMessage message = null;
        IBOFinancialPostingMessage postingMessage = null;
        IBOTransactionScreenControl txnScreenControl = null;
        CurrencyValue amount = null;
        BFCurrencyValue currencyValue = null;
        BigDecimal baseEqv = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal equValue = CommonConstants.BIGDECIMAL_ZERO;
        String key = CommonConstants.EMPTY_STRING;
        String exchRateType = CommonConstants.EMPTY_STRING; 

        Map.Entry entryMap = null;
        HashMap contextMap = new HashMap();
        StringTokenizer tokenizer = null;
        String txnCode = null;
        String currency = CommonConstants.EMPTY_STRING;
        boolean isCrossCurrencyTxn = false;
        int status = IItemStatusCodes.SCHEDULED;
        for (int count = 0; count < messages.size(); count++) {
            message = (IPostingMessage) messages.get(count);
            status = message.getStatus();
            amount = new CurrencyValue(null, CommonConstants.BIGDECIMAL_ZERO);
            baseEqv = CommonConstants.BIGDECIMAL_ZERO;
            BigDecimal exchangeRate = CommonConstants.BIGDECIMAL_ZERO;
            if (message instanceof IBOFinancialPostingMessage) {
                postingMessage = (IBOFinancialPostingMessage) message;
                amount = postingMessage.getF_AMOUNTAsCurrency();
                baseEqv = postingMessage.getF_BASEEQUIVALENT();
                // Added this line to Caluculate Exchange Rate as fix for the
                // bug 12611
                exchRateType = postingMessage.getF_EXCHRATETYPE();
                exchangeRate = postingMessage.getF_EXCHRATE();
            }
            if (message instanceof IBOInterestApplicationMessage) {
                IBOInterestApplicationMessage intmessage = (IBOInterestApplicationMessage) message;
                amount = intmessage.getF_AMOUNTAsCurrency();
                baseEqv = intmessage.getF_BASEEQUIVALENT();
                // Added this line to Caluculate Exchange Rate as fix for the
                // bug 12611
                Object o = intmessage.getF_EXCHRATETYPE();
                if (o != null) {
                    exchRateType = intmessage.getF_EXCHRATETYPE();
                }
                o = intmessage.getF_EXCHRATE();
                if (o != null) {
                    exchangeRate = intmessage.getF_EXCHRATE();
                }
            }
            // set up the class level vars from the foll call

            validatePrimaryID(message.getPrimaryID());

            char postingAction = message.getSign();

            if (!(isCrossCurrencyTxn || currency.equals(CommonConstants.EMPTY_STRING) || currency.equals(getAcctCurrencyCode()))) {
                isCrossCurrencyTxn = true;
            }
            currency = getAcctCurrencyCode();
            key = positionAccountContext.equalsIgnoreCase(CURRENCY_CONTEXT) ? currency : currency + "&"
                    + accountDataValues.getF_BRANCHSORTCODE();

            if (involvedCurrenciesMap.containsKey(key)) {
                currencyValue = (BFCurrencyValue) involvedCurrenciesMap.get(key);
            }
            else {
                currencyValue = new BFCurrencyValue(currency, new CurrencyValue(currency, CommonConstants.BIGDECIMAL_ZERO),
                        getEnvironment().getUserID());
            }
            if (postingAction == '-') {
                involvedCurrenciesMap.put(key, currencyValue.subtract(amount));
            }
            else {
                involvedCurrenciesMap.put(key, currencyValue.add(amount));
            }

            if (baseEquivalentsMap.containsKey(currency)) {
                equValue = (BigDecimal) baseEquivalentsMap.get(currency);
                baseEquivalentsMap.put(currency, equValue.add(baseEqv));
            }
            else {
                baseEquivalentsMap.put(currency, baseEqv);
            }

            if (exchangeRateMap.containsKey(currency)) {
                List<BigDecimal> exchangeRateAvg = (List<BigDecimal>) exchangeRateMap.get(currency);
                BigDecimal divisor = exchangeRateAvg.get(0);
                BigDecimal newdivisor = divisor.add(amount);
                BigDecimal weightedDividend = exchangeRateAvg.get(1);
                BigDecimal newWeightedDividend = weightedDividend.add(amount.multiply(exchangeRate));
                exchangeRateAvg.set(0, newdivisor);
                exchangeRateAvg.set(1, newWeightedDividend);
                exchangeRateMap.put(currency, exchangeRateAvg);
            }
            else if (amount.signum() != 0) {
                List<BigDecimal> exchangeRateAvg = new ArrayList<BigDecimal>();
                exchangeRateAvg.add(amount);
                exchangeRateAvg.add(amount.multiply(exchangeRate));
                exchangeRateMap.put(currency, exchangeRateAvg);
            }
        }

        if (involvedCurrenciesMap.size() == 1 && messages.size() > 1) {
            // Mark Slyman bugz 2596 contd (to this new location)
            // The running total should be zero for all financial posting
            Object onlyKey = involvedCurrenciesMap.keySet().toArray()[0];
            BigDecimal txnTotal = (BigDecimal) involvedCurrenciesMap.get(onlyKey);
            if (txnTotal.signum() != 0) {
                // throw getBankFusionException(431, new Object[] {}, logger);
                EventsHelper.handleEvent(CommonsEventCodes.E_TRANSACTIONS_DO_NOT_ZERO_PROOF, new Object[] {}, new HashMap(),
                        getEnvironment());

            }
            return messages;
        }
        else if (isCrossCurrencyTxn) {
            if (logger.isInfoEnabled()) {
                logger.info("BPW_FN04_51: HandleCrossCurrency : after isCrossCurrencyTXN");
            }

            // Code Changes For Inter Cost Centre Settlement- Begin
            isSettlementRequired = false;
            // Code Changes For Inter Cost Centre Settlement- End
            Iterator iterator = involvedCurrenciesMap.entrySet().iterator();
            List<IBOFinancialPostingMessage> positionPostings = new ArrayList<IBOFinancialPostingMessage>();
            int SerialNo = messages.size() + 1;
            while (iterator.hasNext()) {
                entryMap = (Map.Entry) iterator.next();
                currencyValue = (BFCurrencyValue) entryMap.getValue();
                if (currencyValue.getAmount().signum() == 0)
                    continue;

                // CREATE A DEFAULT FINANCIAL MESSAGE
                postingMessage = (IBOFinancialPostingMessage) getEnvironment().getFactory().getStatelessNewInstance(
                        IBOFinancialPostingMessage.BONAME);
                createStandardItemsMessage(postingMessage);
                PostingHelper.setDefaultValuesForFinPosting(postingMessage, getEnvironment());
                postingMessage.setSerialNo(SerialNo);
                SerialNo++;
                // CREATE A DEFAULT FINANCIAL MESSAGE
                // GET THE POSITION ACCOUNT
                key = (String) entryMap.getKey();
                IBOAttributeCollectionFeature positionAccountItem = null;
                if (positionAccountContext.equalsIgnoreCase(CURRENCY_CONTEXT)) {
                    contextMap.put(CURRENCY_CONTEXT, key);
                }
                else {
                    tokenizer = new StringTokenizer(key, "&");
                    contextMap.put(CURRENCY_CONTEXT, tokenizer.nextToken());
                    contextMap.put(BRANCH_CONTEXT, tokenizer.nextToken());
                }
                positionAccountItem = FinderMethods.findAccountByPseudonameAndContextValue("%" + positionAccountContext + "%"
                        + contextMap.get(positionAccountContext) + "%" + spotPseudonym, currencyValue.getCurrencyCode(),
                        Boolean.TRUE, getEnvironment(), null);

                postingMessage.setStatus(status);
                postingMessage.setRunTimeBPID(getEnvironment().getRuntimeMicroflowID());
                postingMessage.setBranchID(branchID);
                postingMessage.setInitiatedByuserID(getAttributeString("initiatedByUser"));
                postingMessage.setInitiatedByuserID(getEnvironment().getUserID());
                postingMessage.setAuthenticatingUserID(getEnvironment().getUserID());
                postingMessage.setPrimaryID(positionAccountItem.getBoID());
                postingMessage.setProductID(positionAccountItem.getF_PRODUCTID());
                postingMessage.setMessageType(PostingMessageConstants.FINANCIAL_POSTING_MESSAGE);
                postingMessage.setValueDate((getValueDate(environment)));

                postingMessage.setForwardValued(PostingHelper.isForwardDatedTransaction(postingMessage.getValueDate()));

                postingMessage.setPERouterProfileID(positionAccountItem.getF_PEROUTERPROFILEID());
                postingMessage.setAcctCurrencyCode(positionAccountItem.getF_ISOCURRENCYCODE());
                postingMessage.setF_DRAWERNUMBER(getEnvironment().getDrawerNumber());
                postingMessage.setF_USERTYPE(getEnvironment().getUserType());
                postingMessage.setF_AMOUNT(currencyValue.abs());
                postingMessage.setF_ACTUALAMOUNT(currencyValue.abs());
                postingMessage.setF_BOOKBALANCE(positionAccountItem.getF_BOOKEDBALANCE());
                postingMessage.setF_BASEEQUIVALENT(baseEquivalent);
                if (currencyValue.getAmount().signum() == -1) {
                    txnCode = ForexModuleConfiguration.getPositionPostingCreditTxnCode();
                    // Using the Cache of TransactionScreenControl Table for fetching the details.
                    MISTransactionCodeDetails mistransDetails;
                    mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo())
                            .getMisTransactionCodeDetails(txnCode);
                    txnScreenControl = mistransDetails.getTransactionScreenControl();
                    postingMessage.setSign('+');
                    postingMessage.setTransCode(txnCode);
                    postingMessage.setF_AMOUNTCREDIT(currencyValue.abs());
                    postingMessage.setF_AMOUNTDEBIT(currencyValue.abs().negate());
                    postingMessage.setNarrative(txnScreenControl.getF_FIXEDNARRATIVE());
                }
                else {
                    txnCode = ForexModuleConfiguration.getPositionPostingDebitTxnCode();
                    // Using the Cache of TransactionScreenControl Table for fetching the details.
                    MISTransactionCodeDetails mistransDetails;
                    mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo())
                            .getMisTransactionCodeDetails(txnCode);

                    txnScreenControl = mistransDetails.getTransactionScreenControl();
                    postingMessage.setSign('-');
                    postingMessage.setTransCode(txnCode);
                    postingMessage.setF_AMOUNTCREDIT(currencyValue.abs().negate());
                    postingMessage.setF_AMOUNTDEBIT(currencyValue.abs());
                    postingMessage.setNarrative(txnScreenControl.getF_FIXEDNARRATIVE());
                }
                postingMessage.setTransactionRef(transactionReference);
                if (logger.isInfoEnabled()) {
                    logger.info("BPW_FN04_51: positionAccount : " + positionAccountItem.getBoID());
                    logger.info("BPW_FN04_51: txnCode : " + txnCode);
                }

                List<BigDecimal> exchangeRateAvg = (List<BigDecimal>) exchangeRateMap.get(currencyValue.getCurrencyCode());
                postingMessage.setF_EXCHRATE(exchangeRateAvg.get(1).divide(exchangeRateAvg.get(0), RoundingMode.HALF_UP));

                positionPostings.add(postingMessage);
            }
            if (positionPostings.size() > 1) {
                handleBaseEquivalentDeltaAmount(positionPostings);
            }
            messages.addAll(positionPostings);
        }

        if (logger.isInfoEnabled()) {
            logger.info("BPW_FN04_51: HandleCrossCurrency : END");
        }

        return messages;
    }

    /**
     * Validates the account for stopped, closed.
     * 
     * @param primaryID
     *            java.lang.String @ the bank fusion exception
     */
    public void validatePrimaryID(String primaryID) {
        if (logger.isInfoEnabled()) {
            logger.info("BPW_FN04_51: ValidatePrimaryID: Begin : " + primaryID);
        }

        setAccountID(primaryID);

        accountDataValues = PostingHelper.validateAccount(getAccountID(), branchID, null, messageType, getEnvironment());
        // The account id passed in could be a pseudo name so the
        // validateAccount method resolves this
        // to an actual account id. We now need to set this in the message.
        accountID = accountDataValues.getBoID();
        peRouterProfileID = accountDataValues.getF_PEROUTERPROFILEID();
        acctCurrencyCode = accountDataValues.getF_ISOCURRENCYCODE();
        productID = accountDataValues.getF_PRODUCTID();
    }

    protected String getAcctCurrencyCode() {
        return acctCurrencyCode;
    }

    public BankFusionEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Common values for all messages. This is called from a sub-class, so the sub-class has the
     * right to subsequently set these values to whatever it wishes. Of the common message values,
     * it doesn't set the following: Narrative, Sign, Reference and Serial Number. These are
     * expected to be set by each sub-class according to its own needs.
     * 
     * @param message
     *            the message
     * 
     * @see com.trapedza.bankfusion.postingengine.extensionpoints.PostingMessagePrep#createStandardItemsMessage(com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage)
     */
    public void createStandardItemsMessage(IPostingMessage message) {
        if (logger.isInfoEnabled()) {
            logger.info("BPW_FN04_51: createStandardItemsMessage: Begin : ");
        }

        message.setMessageID(GUIDGen.getNewGUID());
        message.setRunTimeBPID(getRuntimeBPID());
        message.setBranchID(getBranchID());
        // transactin iniated by user set to branch power user.
        message.setInitiatedByuserID(getUserName());

        message.setAuthenticatingUserID(getAuthenticatingUserName());
        message.setTransCode(getTransCode());
        message.setPrimaryID(getAccountID());
        message.setProductID(getProductID());
        message.setMessageType(getMessageType());
        message.setValueDate((getValueDate(environment)));
        today = SystemInformationManager.getInstance().getBFBusinessDateTime(runtimeBPID);
        message.setTransactionDate(getToday());
        message.setShortName(getShortName());
        message.setForcePost(getAttributeBoolean(PostingEngineConstants.FORCE_POST));
        message.setPERouterProfileID(getPeRouterProfileID());
        message.setStatus(0);
        message.setAcctCurrencyCode(getAcctCurrencyCode());
        message.setCreateLoanRepay(false);
        message.setForwardValued(isForwardValued());
        message.setForwardValuedIntoValue(false);
        message.setReversal(false);
        message.setTransactionID(transactionID);
        if (logger.isInfoEnabled()) {
            logger.info("BPW_FN04_51: createStandardItemsMessage: END : " + transactionID);
        }

    }

    /**
     * Method This method will Updated Base equivalent amount with Delta amount to make zero balance
     * with all credit and debit legs
     * 
     * @param positionPostings
     * @param baseEquivalentsMap
     * @return
     */
    protected void handleBaseEquivalentDeltaAmount(List<IBOFinancialPostingMessage> positionPostings) {
        String baseCurrCode = SystemInformationManager.getInstance().getBaseCurrencyCode();
        BigDecimal debitBaseAmount = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal creditBaseAmount = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal deltaBaseAmount = CommonConstants.BIGDECIMAL_ZERO;
        IBOFinancialPostingMessage finPostingMessage = null;

        for (int i = 0; i < positionPostings.size(); i++) {
            IBOFinancialPostingMessage finPostingMessageTemp = positionPostings.get(i);
            if (finPostingMessageTemp.getSign() == '+')
                creditBaseAmount = creditBaseAmount.add(finPostingMessageTemp.getF_BASEEQUIVALENT());
            else {
                debitBaseAmount = debitBaseAmount.add(finPostingMessageTemp.getF_BASEEQUIVALENT());
            }
            if (!baseCurrCode.equals(finPostingMessageTemp.getAcctCurrencyCode())) {
                finPostingMessage = finPostingMessageTemp;
            }
        }
        // if finPostingMessage is null updated delta amount in First record.
        if (finPostingMessage == null) {
            finPostingMessage = (IBOFinancialPostingMessage) positionPostings.get(0);
        }

        // Calculate Delta amount if any.
        if (debitBaseAmount.compareTo(creditBaseAmount) != 0) {
            deltaBaseAmount = debitBaseAmount.subtract(creditBaseAmount);
        }

        // Update message and map with the delta amount
        if (finPostingMessage != null && deltaBaseAmount.compareTo(CommonConstants.BIGDECIMAL_ZERO) != 0) {
            if (finPostingMessage.getSign() == '+') {
                finPostingMessage.setF_BASEEQUIVALENT(finPostingMessage.getF_BASEEQUIVALENT().add(deltaBaseAmount));
            }
            else {
                finPostingMessage.setF_BASEEQUIVALENT(finPostingMessage.getF_BASEEQUIVALENT().subtract(deltaBaseAmount));
            }
        }
    }

    protected void setAccountID(String newAccountID) {
        accountID = newAccountID;
    }

    protected String getAccountID() {
        return accountID;
    }

    protected String getBranchID() {
        return branchID;
    }

    protected String getRuntimeBPID() {
        return runtimeBPID;
    }

    protected String getUserName() {
        return userName;
    }

    protected String getAuthenticatingUserName() {
        return authenticatingUserName;
    }

    protected String getTransCode() {
        return transCode;
    }

    protected String getProductID() {
        return productID;
    }

    protected char getMessageType() {
        return 'N';
    }

    protected String getShortName() {
        return shortName;
    }

    protected Date getToday() {
        return today;
    }

    protected String getPeRouterProfileID() {
        return peRouterProfileID;
    }

    protected String getCreateLoanRepay() {
        return createLoanRepay;
    }

    protected boolean isForwardValued() {
        return isForwardValued;
    }

    protected BigDecimal getExchRate(BigDecimal amount, String fromCurrCode, String toCurrCode, String exchRateType,
            String userName, BankFusionEnvironment env) {

        BigDecimal exchRate = bizInfo.getExchangeRate(fromCurrCode, toCurrCode, exchRateType, amount, env);
        BigDecimal margin = bizInfo.getUserMargin(userName, fromCurrCode, toCurrCode, exchRateType, env);
        exchRate = exchRate.add(margin);

        return exchRate;
    }

    /**
     * Converts the provided amount from one currency to another.
     * 
     * @param amount
     *            the amount
     * @param exchRate
     *            the exch rate
     * @param exchRateType
     *            the exch rate type
     * @param fromCurrCode
     *            the from curr code
     * @param toCurrCode
     *            the to curr code
     * 
     * @return java.math.BigDecimal
     */
    protected BigDecimal convertCurrencyAmount(BigDecimal amount, BigDecimal exchRate, String exchRateType, String fromCurrCode,
            String toCurrCode) {
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        BigDecimal returnAmount = null;
        if (bizInfo.isMultiply(fromCurrCode, toCurrCode, exchRateType, getEnvironment()))
            returnAmount = amount.multiply(exchRate);
        else returnAmount = amount.divide(exchRate, 12, BigDecimal.ROUND_UP);

        BFCurrencyValue bfCurrencyReturnValue = new BFCurrencyValue(toCurrCode, returnAmount, null);

        returnAmount = bfCurrencyReturnValue.getRoundedAmount();
        return returnAmount;
    }

}
