/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerCustomerLimitRefreshProcess.java,v.1.1.2.2,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerCustomerLimitRefreshProcess.java,v $
 * Revision 1.1.2.3  2008/08/22 00:26:18  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.customerLimit;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.BatchProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOLimit;
import com.trapedza.bankfusion.bo.refimpl.IBOLimitDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CMN_BatchProcessLog;
import com.trapedza.bankfusion.bo.refimpl.IBOVW_ACCOUNTDTL;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

public class BranchPowerCustomerLimitRefreshProcess extends AbstractBatchProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final transient Log logger = LogFactory.getLog(BranchPowerCustomerLimitRefreshProcess.class.getName());

    private static final String BranchPowerWhereClause = " WHERE " + IBOBranch.BMBRANCH + " BETWEEN ? AND ? ";
    private int branchCode = 0;
    // Initialize variables to be read from properties file

    public String fromBranch = "";
    public String toBranch = "";
    public String extractPath = "";
    public String custCategory = "";
    public String branchCategory = "";
    public String CustExtRefFlag = "";
    private String hdrActionFlag = CommonConstants.EMPTY_STRING;
    IPagingData pageData = null;
    String strHdrDt = "";
    ArrayList branchRange;
    Hashtable CurrencyHash = new Hashtable();
    private BankFusionEnvironment env = null;
    // private AbstractFatomContext context=null;

    private AbstractProcessAccumulator accumulator;
    private int pageSize = 0;
    static Properties fileBPRefreshProp;
    static Properties fileRefreshProp;

//  SQL Query 1 for Customer Limits (limit details)
    final String queryLimit = "SELECT (T5." + IBOLimitDetails.LIMITREF + ") AS " + IBOLimitDetails.LIMITREF + ",  (T5."
            + IBOLimitDetails.CURRENCY + ") AS " + IBOLimitDetails.CURRENCY + ",  (T5." + IBOLimitDetails.LIMIT + ") AS "
            + IBOLimitDetails.LIMIT + ",  (T5." + IBOLimitDetails.EXPOSURE + ") AS " + IBOLimitDetails.EXPOSURE + ",  (T6."
            + IBOLimit.LIMITINDICATOR + ") AS " + IBOLimit.LIMITINDICATOR + " FROM " + IBOLimitDetails.BONAME + " T5, "
            + IBOLimit.BONAME + " T6 where T5." + IBOLimitDetails.LIMITREF + "= T6." + IBOLimit.LIMITREF + " AND T6."
            + IBOLimit.LIMITCATEGORY + " = ? order by T6." + IBOLimit.LIMITREF;

    // SQL Query 2 for Customer Limits (customer details)
    final String queryCustLimit = "SELECT (T1." + IBOCustomer.CUSTOMERCODE + ") AS " + IBOCustomer.CUSTOMERCODE + ", (T2."
            + IBOBranch.BMBRANCH + ") AS " + IBOBranch.BMBRANCH + " FROM " + IBOCustomer.BONAME + " T1, " + IBOBranch.BONAME + " T2"
            + " WHERE " + " T2." + IBOBranch.BMBRANCH + " BETWEEN ? AND ? AND T1." + IBOCustomer.BRANCHSORTCODE +" = ? "  + " AND T1." + IBOCustomer.BRANCHSORTCODE + "= T2."
            + IBOBranch.BRANCHSORTCODE;

    // SQL Query to read currency Scale for amount formatting
    final String queryCurrency = "SELECT (T1." + IBOCurrency.ISOCURRENCYCODE + ") AS " + IBOCurrency.ISOCURRENCYCODE + ", (T1."
            + IBOCurrency.CURRENCYSCALE + ") AS " + IBOCurrency.CURRENCYSCALE + " FROM " + IBOCurrency.BONAME + " T1";

    TreeMap accHash = new TreeMap();
    TreeMap limitHash = new TreeMap();

    StringBuffer fileData = new StringBuffer();

    /**
     * <code>mcflData</code> Limit Detail Record Structure class
     */
    private class mcflData {
        public String FL_DT2_RECORD_TYPE = "";
        public String FL_DT2_CLT_NUMBER = "";
        public String FL_DT2_CLT_LIMIT_CURR = "";
        public String FL_DT2_LIMIT_CHK_FLAG = "";
        public String FL_DT2_LMT_AMT1 = "";
        public String FL_DT2_EXP_AMT1 = "";
        public String FL_DT2_LMT_AMT2 = "";
        public String FL_DT2_EXP_AMT2 = "";
        public String FL_DT2_LMT_AMT3 = "";
        public String FL_DT2_EXP_AMT3 = "";
        public String FL_DT2_LMT_AMT4 = "0";
        public String FL_DT2_EXP_AMT4 = "0";
        public String FL_DT2_LMT_AMT5 = "0";
        public String FL_DT2_EXP_AMT5 = "0";
        public String FL_DT2_LMT_AMT6 = "0";
        public String FL_DT2_EXP_AMT6 = "0";
        public String FL_DT2_LMT_AMT7 = "0";
        public String FL_DT2_EXP_AMT7 = "0";
        public String FL_DT2_LMT_AMT8 = "0";
        public String FL_DT2_EXP_AMT8 = "0";
        public String FL_DT2_LMT_AMT9 = "0";
        public String FL_DT2_EXP_AMT9 = "0";
        public String FL_DT2_LMT_AMT10 = "0";
        public String FL_DT2_EXP_AMT10 = "0";
        public String FL_DT2_CLT_LMT_INFO = "";
        public String FL_DT2_FILLER1 = "";
        public String FL_DT2_BRANCH_CODE = "";
        public String FL_DT2_ACTION = "A";
        public String FL_DT2_CHECKSUM = "";
        public String FL_DT2_FILLER2 = "";

    }

    String MSG1 = CommonConstants.EMPTY_STRING;;
    Boolean Status;

    /**
     * @param environment
     *            Used to get a handle on the BankFusion environment
     * @param context
     *            A set of data passed to the PreProcess, Process and PostProcess classes
     * @param priority
     *            Thread priority
     */
    public BranchPowerCustomerLimitRefreshProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
        super(environment, context, priority);
        this.context = context;
        env = environment;
    }

    /**
     * Initialise parameters and the accumulator for the BalanceSheetCollection process
     * 
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#init()
     */
    public void init() {

        initialiseAccumulator();
    }

    /**
     * Gets a reference to the accumulator
     * 
     * @return A reference to the accumulator
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#getAccumulator()
     */
    public AbstractProcessAccumulator getAccumulator() {
        return accumulator;
    }

    /**
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#initialiseAccumulator()
     */
    protected void initialiseAccumulator() {
        Object[] accumulatorArgs = new Object[0];
        accumulator = new BranchPowerCustomerLimitRefreshAccumulator(accumulatorArgs);
    }

    /**
     * Processes the branchpowerRefresh on the specified page, and accumulates the totals.
     * 
     * @param pageToProcess
     *            Page number of the page to be processed
     * @return The accumulator
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#process(int)
     * @throws BatchProcessException
     *             Thrown if a BankFusionException occurs when processing the balance sheets and
     *             accumulating the totals, or if ServiceException or ErrorOnCommitException occur
     *             when commit or rolling back the transaction.
     */
    public AbstractProcessAccumulator process(int pageToProcess) throws IllegalArgumentException, BatchProcessException,
            BankFusionException {
    	fileData = new StringBuffer();
        logger.debug("Invoking Page: " + pageToProcess);
        Object[] additionalParameters = context.getAdditionalProcessParams();
        fileBPRefreshProp = (Properties) additionalParameters[0];
        if (fileBPRefreshProp == null || fileBPRefreshProp.size() == 0) {
            //throw new BankFusionException(127, new Object[] { "Error Reading Properties File" }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "Error Reading Properties File" }, new HashMap(), env);
        }

        if ((getBPRereshProperty("FROMBRANCH").equalsIgnoreCase("")) || (getBPRereshProperty("TOBRANCH").equalsIgnoreCase(""))
                || (getBPRereshProperty("EXTRACTPATH").equalsIgnoreCase("")) || (getBPRereshProperty("CUST-LIM-CATEGORY").equalsIgnoreCase(""))
                || (getBPRereshProperty("CUSTOMER-EXT-REFRESH").equalsIgnoreCase(""))) {
           // throw new BankFusionException(127, new Object[] { "Invalid Parameters passed" }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "Invalid Parameters passed" }, new HashMap(), env);
        }
        else {
            fromBranch = getBPRereshProperty("FROMBRANCH");
            toBranch = getBPRereshProperty("TOBRANCH");
        }
        toBranch = getBPRereshProperty("TOBRANCH");

        branchRange = new ArrayList();
        branchRange.add(0, fromBranch);
        branchRange.add(1, toBranch);
        extractPath = getBPRereshProperty("EXTRACTPATH");
        custCategory = getBPRereshProperty("CUST-LIM-CATEGORY");
        pageSize = getBPRereshProperty("PAGE-SIZE") == null ? 100 : Integer.parseInt(getBPRereshProperty("PAGE-SIZE"));
        // branchCategory = getProperty("BRCH-LIM-CATEGORY");
        CustExtRefFlag = getBPRereshProperty("CUSTOMER-EXT-REFRESH");
        List list = null;
        IBOBranch branchBO = null;
        logger.debug("Invoking Page: " + pageToProcess);
        pagingData.setCurrentPageNumber(pageToProcess);
        list = env.getFactory().findAll(IBOBranch.BONAME, pagingData);
        branchBO = (IBOBranch) list.get(0);
        branchCode = Integer.parseInt(branchBO.getF_BMBRANCH());
        Date hdrDate = null; // date format = 2006-04-09
        try {
            hdrDate = new SimpleDateFormat("yyyy-MM-dd").parse(SystemInformationManager.getInstance().getBFBusinessDate()
                    .toString());
            pageData = new PagingData(0, pageSize);
        }
        catch (ParseException pExcpn) {
            //throw new BankFusionException(127, new Object[] { pExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { pExcpn.getLocalizedMessage()  }, new HashMap(), env);
        }
        strHdrDt = new SimpleDateFormat("yyyyMMdd").format(hdrDate);
        // Load Currncy details
        List currencyDetails = null;
        currencyDetails = env.getFactory().executeGenericQuery(queryCurrency, null, null);
        SimplePersistentObject currencyPO = null;
        for (int i = 0; i < currencyDetails.size(); i++) {
            currencyPO = (SimplePersistentObject) currencyDetails.get(i);
            CurrencyHash.put(currencyPO.getDataMap().get(IBOCurrency.ISOCURRENCYCODE), currencyPO.getDataMap().get(
                    IBOCurrency.CURRENCYSCALE));
        }
        Iterator Branch = null;

        pagingData.setCurrentPageNumber(pageToProcess);
        {
            try {
                ArrayList list1 = new ArrayList();
                list1.add(fromBranch);
                list1.add(toBranch);
                List branchList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOBranch.BONAME, BranchPowerWhereClause, list1, pagingData, false);
                BankFusionThreadLocal.setCurrentPageRecordIDs(branchList);
                Branch = branchList.iterator();
                // Branch = environment.getFactory().findAll(IBOBranch.BONAME,
                // pagingData).iterator();
            }
            catch (BankFusionException exception) {
                logger.error("Serious Error in processing Page Number: " + pageToProcess);
                return accumulator;
            }
        }

        while (Branch.hasNext()) {
            String branchCode = CommonConstants.EMPTY_STRING;
            try {
                IBOBranch branch = (IBOBranch) Branch.next();
                branchCode = branch.getBoID();
                BankFusionThreadLocal.setCurrentRecordID(branchCode);

                if ((branchCode != null)) {
                	fileRefreshProp = (Properties) additionalParameters[1];
                    if (CustExtRefFlag.equals("1")) {
                        refreshLimit(env, branchCode);
                    }
                }
                else {
                    logger
                            .debug("BranchPowerCustomerLimitRefresh cannot be processed as branch does not belong to specified branchRange");
                }

            }

            catch (BankFusionException exception) {
                exception.printStackTrace();
                logger.error(exception.getMessage());
            }
        }

        return accumulator;

    }

    /**
     * populate customer limits for limits refresh
     * 
     * @param env
     * @throws BankFusionException
     */

    private void populateLimits(BankFusionEnvironment env) throws BankFusionException {
    }

    /**
     * Method to perform customer limits refresh
     * 
     * @param env
     * @throws BankFusionException
     */
    private void refreshLimit(BankFusionEnvironment env, String branchSortCode) throws BankFusionException {
    	fileData = new StringBuffer();
    	ArrayList params = new ArrayList();
    	params.addAll(branchRange);
    	params.add(2, branchSortCode);
    	FileOutputStream fout = null;
        List custlimDetails = null;
       // int totalNoOfPageCount = getNumberOfPages(branchSortCode);
        SimplePersistentObject customerView = null;
        int Branchctr = 0;
        boolean notfirstTime = false;
        try {
        	  limitHash = new TreeMap();
          //  for (int index = 1; index <= totalNoOfPageCount; index++) {
                pageData.setCurrentPageNumber(1);
                custlimDetails = env.getFactory().executeGenericQuery(queryCustLimit, params, pageData);
                if (custlimDetails.size() == 0)
                    return;
                SimplePersistentObject custPO = null;
                Hashtable tmpCustHash = new Hashtable();
                for (int i = 0; i < custlimDetails.size(); i++) {
                    custPO = (SimplePersistentObject) custlimDetails.get(i);
                    tmpCustHash.put(custPO.getDataMap().get(IBOCustomer.CUSTOMERCODE), custPO.getDataMap().get(IBOBranch.BMBRANCH));
                }
                List limitDetails = null;
                ArrayList cusLimit = new ArrayList();
                cusLimit.add(0, custCategory);
                limitDetails = env.getFactory().executeGenericQuery(queryLimit, cusLimit, null);
                SimplePersistentObject cuslPO = null;
                mcflData mcfl = null;
                String cust = "";
                int limitCtr = 0;
                String tempCustLimitRef = CommonConstants.EMPTY_STRING;
              
                for (int i = 0; i < limitDetails.size(); i++) {
                    cuslPO = (SimplePersistentObject) limitDetails.get(i);
                    tempCustLimitRef=(String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF);
                    if(!tmpCustHash.containsKey(tempCustLimitRef)){
                        continue;
                    }
                    if (!cust.equalsIgnoreCase((String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF))) {
                        limitCtr = 0;
                        cust = "";
                    }
                    if (limitCtr == 0) {
                        mcfl = new mcflData();
                        mcfl.FL_DT2_RECORD_TYPE = "03";
                        if (cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().length() > 9) {
                            mcfl.FL_DT2_CLT_NUMBER = cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().substring(0, 8);
                        }
                        else {
                            mcfl.FL_DT2_CLT_NUMBER = (String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF);
                        }
                        mcfl.FL_DT2_CLT_LIMIT_CURR = (String) cuslPO.getDataMap().get(IBOLimitDetails.CURRENCY);
                        mcfl.FL_DT2_LIMIT_CHK_FLAG = cuslPO.getDataMap().get(IBOLimit.LIMITINDICATOR).toString();
                        mcfl.FL_DT2_LMT_AMT1 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT1 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        mcfl.FL_DT2_BRANCH_CODE = (String) tmpCustHash.get(cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF));
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 1) {
                        if (cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().length() > 9) {
                            mcfl.FL_DT2_CLT_NUMBER = cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().substring(0, 8);
                        }
                        else {
                            mcfl.FL_DT2_CLT_NUMBER = (String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF);
                        }
                        mcfl.FL_DT2_BRANCH_CODE = (String) tmpCustHash.get(cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF));
                        mcfl.FL_DT2_LMT_AMT2 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT2 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 2) {
                        mcfl.FL_DT2_LMT_AMT3 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT3 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 3) {
                        mcfl.FL_DT2_LMT_AMT4 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT4 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 4) {
                        mcfl.FL_DT2_LMT_AMT5 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT5 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 5) {
                        mcfl.FL_DT2_LMT_AMT6 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT6 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 6) {
                        mcfl.FL_DT2_LMT_AMT7 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT7 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 7) {
                        mcfl.FL_DT2_LMT_AMT8 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT8 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 8) {
                        mcfl.FL_DT2_LMT_AMT9 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT9 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                    else if (limitCtr == 9) {
                        mcfl.FL_DT2_LMT_AMT10 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
                        mcfl.FL_DT2_EXP_AMT10 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
                        limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
                        cust = mcfl.FL_DT2_CLT_NUMBER;
                        limitCtr++;
                    }
                }
           // }
                String Branch = "";
                
                if (limitHash.size() == 0)
                	return;
                Iterator itr = limitHash.keySet().iterator();
                fileData = new StringBuffer();
                int ctr =0;
                Set<String> keySet = limitHash.keySet();
//                while (itr.hasNext() ) 
                	for(String key: keySet)
                	{
                		mcfl = new mcflData();
//                    String key = (String) itr.next();
                    mcfl = (mcflData) limitHash.get(key);
                    if (mcfl.FL_DT2_BRANCH_CODE == null) {
                        break;
                    }

                    if (!Branch.equalsIgnoreCase(mcfl.FL_DT2_BRANCH_CODE)) {
                        if (notfirstTime) {
                        	formatLimitTrail(String.valueOf(++Branchctr), fout, env);
                            Branchctr = 0;
                        }
                        fileData = new StringBuffer();
                        fout = new FileOutputStream(extractPath + "mcfl" + mcfl.FL_DT2_BRANCH_CODE.toString() + ".dat",false);
                        formatLimitHeader(mcfl.FL_DT2_BRANCH_CODE, fout, env);
                        Branch = (String) mcfl.FL_DT2_BRANCH_CODE;
                        formatLimitBranch(Branch, fout, env);
                        notfirstTime = true;
                    }
                    fileData = new StringBuffer();
                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-RECORD-TYPE")).intValue(), mcfl.FL_DT2_RECORD_TYPE,
                            'A'));
                    fileData
                            .append(setField(new Integer(getRereshProperty("FL-DT2-CLT-NUMBER")).intValue(), mcfl.FL_DT2_CLT_NUMBER, 'A'));
                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-CLT-LIMIT-CURR")).intValue(),
                            mcfl.FL_DT2_CLT_LIMIT_CURR, 'A'));
                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-LIMIT-CHK-FLAG")).intValue(),
                            mcfl.FL_DT2_LIMIT_CHK_FLAG, 'A'));

                    if (mcfl.FL_DT2_LMT_AMT1 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT1), "FL-DT2-LMT-AMT1", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT1 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT1), "FL-DT2-LMT-AMT1", fileData, 0);
                    }

                    if (mcfl.FL_DT2_EXP_AMT1 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT1), "FL-DT2-EXP-AMT1", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT1 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT1), "FL-DT2-EXP-AMT1", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT2 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT2), "FL-DT2-LMT-AMT2", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT2 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT2), "FL-DT2-LMT-AMT2", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT2 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT2), "FL-DT2-EXP-AMT2", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT2 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT2), "FL-DT2-EXP-AMT2", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT3 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT3), "FL-DT2-LMT-AMT3", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT3 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT3), "FL-DT2-LMT-AMT3", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT3 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT3), "FL-DT2-EXP-AMT3", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT3 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT3), "FL-DT2-EXP-AMT3", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT4 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT4), "FL-DT2-LMT-AMT4", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT4 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT4), "FL-DT2-LMT-AMT4", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT4 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT4), "FL-DT2-EXP-AMT4", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT4 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT4), "FL-DT2-EXP-AMT4", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT5 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT5), "FL-DT2-LMT-AMT5", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT5 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT5), "FL-DT2-LMT-AMT5", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT5 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT5), "FL-DT2-EXP-AMT5", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT5 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT5), "FL-DT2-EXP-AMT5", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT6 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT6), "FL-DT2-LMT-AMT6", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT6 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT6), "FL-DT2-LMT-AMT6", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT6 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT6), "FL-DT2-EXP-AMT6", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT6 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT6), "FL-DT2-EXP-AMT6", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT7 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT7), "FL-DT2-LMT-AMT7", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT7 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT7), "FL-DT2-LMT-AMT7", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT7 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT7), "FL-DT2-EXP-AMT7", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT7 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT7), "FL-DT2-EXP-AMT7", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT8 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT8), "FL-DT2-LMT-AMT8", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT8 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT8), "FL-DT2-LMT-AMT8", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT8 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT8), "FL-DT2-EXP-AMT8", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT8 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT8), "FL-DT2-EXP-AMT8", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT9 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT9), "FL-DT2-LMT-AMT9", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT9 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT9), "FL-DT2-LMT-AMT9", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT9 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT9), "FL-DT2-EXP-AMT9", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT9 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT9), "FL-DT2-EXP-AMT9", fileData, 0);
                    }
                    if (mcfl.FL_DT2_LMT_AMT10 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT10), "FL-DT2-LMT-AMT10", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_LMT_AMT10 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT10), "FL-DT2-LMT-AMT10", fileData, 0);
                    }
                    if (mcfl.FL_DT2_EXP_AMT10 != "") {
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT10), "FL-DT2-EXP-AMT10", fileData, 0);
                    }
                    else {
                        mcfl.FL_DT2_EXP_AMT10 = "000000000000000000";
                        setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT10), "FL-DT2-EXP-AMT10", fileData, 0);
                    }

                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-CLT-LMT-INFO")).intValue(), mcfl.FL_DT2_CLT_LMT_INFO,' '));
                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-FILLER1")).intValue(), mcfl.FL_DT2_FILLER1, ' '));
                    if (mcfl.FL_DT2_BRANCH_CODE != null) {
                        fileData.append(setField(new Integer(getRereshProperty("FL-DT2-BRANCH-CODE")).intValue(),
                                mcfl.FL_DT2_BRANCH_CODE, 'A'));
                    }
                    else {
                        break;
                    }

                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-ACTION")).intValue(), mcfl.FL_DT2_ACTION, 'A'));
                    BigDecimal checksum = new BigDecimal(0).setScale(0);
                    checksum = (new BigDecimal(mcfl.FL_DT2_LMT_AMT1).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT2).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_LMT_AMT3).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT4).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_LMT_AMT5).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT6).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_LMT_AMT7).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT8).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_LMT_AMT9).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT10).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_EXP_AMT1).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT2).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_EXP_AMT3).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT4).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_EXP_AMT5).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT6).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_EXP_AMT7).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT8).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_EXP_AMT9).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT10).abs()).add(
                            new BigDecimal(mcfl.FL_DT2_BRANCH_CODE).abs());
                    BigInteger cksum = checksum.toBigInteger().abs();
                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-CHECKSUM")).intValue(), cksum.toString(), 'N'));
                    fileData.append(setField(new Integer(getRereshProperty("FL-DT2-FILLER2")).intValue(), mcfl.FL_DT2_FILLER2, ' '));
                    Branchctr++;
                    fileData.append("\r\n");
                    fout.write(fileData.toString().getBytes());
                    fout.flush();
                    Branch = mcfl.FL_DT2_BRANCH_CODE;
                }
            
            if (fout != null) {
	            formatLimitTrail(String.valueOf(++Branchctr), fout, env);
	            fout.close();
            }
        }
        catch (FileNotFoundException fnfExcpn) {
            BranchPowerCustomerLimitRefreshFatomContext.Status = Boolean.FALSE;
            //throw new BankFusionException(127, new Object[] { fnfExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { fnfExcpn.getLocalizedMessage()  }, new HashMap(), env);
        }
        catch (IOException ioExcpn) {
            BranchPowerCustomerLimitRefreshFatomContext.Status = Boolean.FALSE;
            // setF_OUT_Batch_Status(new Boolean (false));
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage()  }, new HashMap(), env);
        }
        catch (Exception Excpn) {
            BranchPowerCustomerLimitRefreshFatomContext.Status = Boolean.FALSE;
            //throw new BankFusionException(127, new Object[] { Excpn.getLocalizedMessage() }, logger, env);
            
            // setF_OUT_Batch_Status(new Boolean (false));
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { Excpn.getLocalizedMessage()  }, new HashMap(), env);

        }
    }

    /**
     * formats Customer branch limit record
     * 
     * @param Branch
     *            takes Branch as input to fetch,format the branch record
     * @param fout
     */
    private void formatLimitBranch(String Branch, FileOutputStream fout, BankFusionEnvironment env) throws BankFusionException {

        Integer scale1 = (Integer) (CurrencyHash.get(SystemInformationManager.getInstance().getBaseCurrencyCode()));
        int scale = scale1.intValue();

        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-RECORD-TYPE")).intValue(), "02", 'A'));
        setAmount(new BigDecimal("0"), "FL-DT1-BRCH-NON-ZERO-1", fileData, scale);
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-BRCH-NON-ZERO-2")).intValue(), "", 'A'));
        setAmount(new BigDecimal("0"), "FL-DT1-BRCH-ZERO-LMT-1", fileData, scale);
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-BRCH-ZERO-LMT-2")).intValue(), "", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-BRCH-PERCENT")).intValue(), "", 'A'));
        setAmount(new BigDecimal("0"), "FL-DT1-BRCH-PER-FILLER", fileData, scale);
        setAmount(new BigDecimal("0"), "FL-DT1-SYS-LIM-BAL", fileData, scale);
        setAmount(new BigDecimal("0"), "FL-DT1-SYS-LIM-EXP-CHK", fileData, scale);
        setAmount(new BigDecimal("0"), "FL-DT1-SYS-CUST-EXT-LMTS", fileData, scale);
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-FILLER1")).intValue(), "", ' '));
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-BRANCH-CODE")).intValue(), Branch, 'N'));
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-ACTION")).intValue(), "A", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-CHECKSUM")).intValue(), Branch, 'N'));
        fileData.append(setField(new Integer(getRereshProperty("FL-DT1-FILLER2")).intValue(), "", ' '));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
        }
        catch (IOException ioExcpn) {
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage()  }, new HashMap(), env);
        }
    }

    /**
     * formats and writes the Customer Limit Header record for the passed branch parameter
     * 
     * @param Branch
     * @param fout
     */
    private void formatLimitHeader(String Branch, FileOutputStream fout, BankFusionEnvironment env) throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        hdrActionFlag = getBPRereshProperty("HDRACTIONFLAG"); 
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-RECORD-TYPE")).intValue(), "01", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-ACTION-FLAG")).intValue(), hdrActionFlag, 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-SOURCE-SYSTEM")).intValue(), "MCAS", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-DEST-SYSTEM")).intValue(), "BPWR", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-BRANCH-CODE")).intValue(), Branch, 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-FILE-ID")).intValue(), "LM", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-PROCESS-DATE")).intValue(), strHdrDt, 'N'));
        fileData.append(setField(new Integer(getRereshProperty("FL-HDR-FILLER")).intValue(), "", ' '));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
        }
        catch (IOException ioExcpn) {
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage()  }, new HashMap(), env);
        }
    }

    /**
     * Method to change amount from Bankmaster to regular format formats and populates the string
     * buffer with the formatted amount takes bdAmt,fldName as input
     * 
     * @param bdAmt
     * @param fldName
     * @param fileData
     */
    private static void setAmount(BigDecimal bdAmt, String fldName, StringBuffer fileData, int scale) {
        bdAmt = bdAmt.setScale(scale,BigDecimal.ROUND_DOWN);
        bdAmt = bdAmt.setScale(scale);
        String Amount = bdAmt.unscaledValue().abs().toString();

        if (bdAmt.signum() == -1) {
            fileData.append(setField(new Integer(getRereshProperty(fldName)).intValue() - 1, Amount.substring(0, Amount.length() - 1),
                    'N'));
            char str = Amount.charAt(Amount.length() - 1);
            fileData.append(getRereshProperty(String.valueOf(str)));
        }
        else {
            fileData.append(setField(new Integer(getRereshProperty(fldName)).intValue(), Amount, 'N'));
        }
    }

    /**
     * formats and wrtes customer limit trailer record
     * 
     * @param Branchctr
     * @param fout
     */
    private void formatLimitTrail(String Branchctr, FileOutputStream fout, BankFusionEnvironment env) throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getRereshProperty("FL-TRL-RECORD-TYPE")).intValue(), "99", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FL-TRL-RECORD-COUNT")).intValue(), Branchctr, 'N'));
        fileData.append(setField(new Integer(getRereshProperty("FL-TRL-FILLER")).intValue(), "", ' '));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
            fout.close();
        }
        catch (NullPointerException ioExcpn) {
            return;
        }
        catch (IOException ioExcpn) {
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage()  }, new HashMap(), env);
        }
    }

    // throw new BankFusionException(127, new Object[] {"Error Reading Properties File"+string },
    // logger, env);

    /**
     * reads the property for the key passed
     * 
     * @param sKey
     * @return
     */
    private static String getBPRereshProperty(String sKey) {
    	String sValue = fileBPRefreshProp.get(sKey).toString();
        return sValue;
    }
    
    /**
     * reads the property for the key passed
     * 
     * @param sKey
     * @return
     */
    private static String getRereshProperty(String sKey) {
    	String sValue = fileRefreshProp.get(sKey).toString();
        return sValue;
    }

    /**
     * This method formats fields input using the type ('A' or 'N') and length values passed.
     * returns the formatted string back to calling method
     * 
     * @param ind
     * @param string
     * @param type
     * @return
     */
    private static String setField(int ind, String string, char type) {
        int count = 0;
        if (null != string) {
            count = string.length();
        }
        final StringBuffer sbuff = new StringBuffer();
        if (type == 'A') {
            sbuff.append(string);
        }
        for (int index = count; index < ind; index++) {
            if (type == 'A') {
                sbuff.append(" ");
            }
            else {
                sbuff.append("0");
            }
        }
        if (type == 'N') {
            sbuff.append(string);
        }
        return sbuff.toString();
    }

    private int getNumberOfPages(String branchSortCode) {
        int totalCount = 0;
        String countQuery = "SELECT COUNT(*) AS COUNT FROM " + IBOVW_ACCOUNTDTL.BONAME + " WHERE "
                + IBOVW_ACCOUNTDTL.BRANCHSORTCODE + " = ?";
        ArrayList countBranchSortCode = new ArrayList();
        countBranchSortCode.add(branchSortCode);
        List totalNoOfPagesList = env.getFactory().executeGenericQuery(countQuery, countBranchSortCode, null);
        SimplePersistentObject simplePersistentObject = (SimplePersistentObject) totalNoOfPagesList.get(0);
        totalCount = (new Integer(simplePersistentObject.getDataMap().get("COUNT").toString())).intValue();
        totalCount = (totalCount % pageSize) == 0 ? (totalCount / pageSize) : (totalCount / pageSize) + 1;
        return totalCount;
    }
    
    /**
     * This method will be called by the Batch Framework when there is an exception caught and the process page is marked as 
     * failed. The Batch Framework creates a private Session and the factory is passed to this method so that the values get committed.
     * 
     * @param unprocessedIds - List of Unprocessed IDs
     * @param failedId - Failed Account Number
     * @param exception - Exception Object
     * @param factory - Instance of persistenceObjectFactory created as a private Session.
     *  
     */
    public void logException(List<String> unprocessedIds, String failedId, Exception exception,IPersistenceObjectsFactory factory){
    	
    	Iterator unprocessedIter = unprocessedIds.iterator();
    	String status = CommonConstants.EMPTY_STRING;
        String message = CommonConstants.EMPTY_STRING;
    	while(unprocessedIter.hasNext()){
    		IBOBranch branchItem = (IBOBranch) unprocessedIter.next();
            
            String key = branchItem.getBoID();            
            if(key.equalsIgnoreCase(failedId)){
            	status = "E";
            	message= exception.getMessage();
            }else{
            	status = "U";
            }
            createLogMessage(key, message, status, factory);
    	}
    } 
    
    /**
     * This method is used to create log error message
     *
     * @param key
     * @param message
     * @param status
     */
    private void createLogMessage(String key, String message, String status, IPersistenceObjectsFactory factory) {
    		IBOUB_CMN_BatchProcessLog batchException = (IBOUB_CMN_BatchProcessLog)
    		factory.getStatelessNewInstance(IBOUB_CMN_BatchProcessLog.BONAME);
    		batchException.setBoID(GUIDGen.getNewGUID());
    		batchException.setF_PROCESSNAME(this.context.getBatchProcessName());
    		
    		batchException.setF_RUNDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime(
                          environment.getRuntimeMicroflowID()));
    		batchException.setF_RECORDID(key);

    		if (status.equalsIgnoreCase("E") || status.equalsIgnoreCase("W")) {
    			if (logger.isErrorEnabled()){
    				logger.error("Error processing for Account [ "+key+" ] Reason :- "+message);
    			}
    			if (null == message){
    				message = CommonConstants.EMPTY_STRING;
    			}
    			message = message.replaceAll(",", "");
    			message = message.replaceAll(":", "");
    			message = message.replaceAll("':", "");

    			batchException.setF_ERRORMESSAGE(message);
    			batchException.setF_STATUS(status);
    		}else{
    			if (logger.isInfoEnabled()){
    				logger.info("Unprocessed Account [ "+key+" ] ");
    			}
    			batchException.setF_STATUS(status);
    		}
    		factory.create(IBOUB_CMN_BatchProcessLog.BONAME, batchException);
    }
}
