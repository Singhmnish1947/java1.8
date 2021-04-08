/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerAccountRefreshProcess.java,v.1.1.2.2,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerAccountRefreshProcess.java,v $
 * Revision 1.1.2.5  2008/09/15 20:17:25  deepac
 * BUG# 12227
 *
 * Revision 1.1.2.4  2008/08/25 23:06:46  krishnanr
 * Branch Power Refresh Changes
 *
 * Revision 1.1.2.3  2008/08/22 00:26:14  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.account;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.BatchProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOAccPortMap;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCreditInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBODebitInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWACCREFRESHTAG;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

public class BranchPowerAccountRefreshProcess extends AbstractBatchProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    private static final IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
            .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
    private static final transient Log logger = LogFactory.getLog(BranchPowerAccountRefreshProcess.class.getName());
    private int branchCode = 0;
    private int fromBranchCode = 0;
    private int toBranchCode = 0;
    private int pageSize = 0;

    private String extractPath = CommonConstants.EMPTY_STRING;
    private String branchSortCode = CommonConstants.EMPTY_STRING;
    private String hdrActionFlag = CommonConstants.EMPTY_STRING;

    private static final String queryAccount = CommonConstants.SELECT + CommonConstants.SPACE + "ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.ACCDCRINTEREST + " AS " + IBOUBTB_BPWACCREFRESHTAG.ACCDCRINTEREST + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.ACCOUNTDESCRIPTION + " AS " + IBOUBTB_BPWACCREFRESHTAG.ACCOUNTDESCRIPTION + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.ACCOUNTID + " AS " + IBOUBTB_BPWACCREFRESHTAG.ACCOUNTID + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.ACCOUNTNAME + " AS " + IBOUBTB_BPWACCREFRESHTAG.ACCOUNTNAME + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.ACCRIGHTSINDICATOR + " AS " + IBOUBTB_BPWACCREFRESHTAG.ACCRIGHTSINDICATOR + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.BLOCKEDBALANCE + " AS " + IBOUBTB_BPWACCREFRESHTAG.BLOCKEDBALANCE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.BOOKEDBALANCE + " AS " + IBOUBTB_BPWACCREFRESHTAG.BOOKEDBALANCE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.BRANCHSORTCODE + " AS " + IBOUBTB_BPWACCREFRESHTAG.BRANCHSORTCODE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.CHEQUEDEPOSITBALANCE + " AS " + IBOUBTB_BPWACCREFRESHTAG.CHEQUEDEPOSITBALANCE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.CLEAREDBALANCE + " AS " + IBOUBTB_BPWACCREFRESHTAG.CLEAREDBALANCE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.CLOSED + " AS " + IBOUBTB_BPWACCREFRESHTAG.CLOSED + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.CREDITLIMIT + " AS " + IBOUBTB_BPWACCREFRESHTAG.CREDITLIMIT + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.DEBITACCDINTEREST + " AS " + IBOUBTB_BPWACCREFRESHTAG.DEBITACCDINTEREST + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.DEBITLIMIT + " AS " + IBOUBTB_BPWACCREFRESHTAG.DEBITLIMIT + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.ISOCURRENCYCODE + " AS " + IBOUBTB_BPWACCREFRESHTAG.ISOCURRENCYCODE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.LASTTRANSACTIONDATE + " AS " + IBOUBTB_BPWACCREFRESHTAG.LASTTRANSACTIONDATE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.LIMITINDICATOR + " AS " + IBOUBTB_BPWACCREFRESHTAG.LIMITINDICATOR + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF1 + " AS " + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF1 + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF2 + " AS " + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF2 + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF3 + " AS " + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF3 + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF4 + " AS " + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF4 + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF5 + " AS " + IBOUBTB_BPWACCREFRESHTAG.LIM_LIMITREF5 + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.PORTFOLIOID + " AS " + IBOUBTB_BPWACCREFRESHTAG.PORTFOLIOID + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.PRODUCT_NUMERICCODE + " AS " + IBOUBTB_BPWACCREFRESHTAG.PRODUCT_NUMERICCODE + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.STOPPED + " AS " + IBOUBTB_BPWACCREFRESHTAG.STOPPED + ", ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.UBROWSEQ + " AS " + IBOUBTB_BPWACCREFRESHTAG.UBROWSEQ + " FROM "
            + IBOUBTB_BPWACCREFRESHTAG.BONAME + " AS ACCDTL WHERE " + " ACCDTL." + IBOUBTB_BPWACCREFRESHTAG.BRANCHSORTCODE
            + " =? AND  ACCDTL." + IBOUBTB_BPWACCREFRESHTAG.UBROWSEQ + " Between ? and ? ORDER BY ACCDTL."
            + IBOUBTB_BPWACCREFRESHTAG.ACCOUNTID;
    private static final String branchSortCodeQuery = " WHERE " + IBOUBTB_BPWACCREFRESHTAG.BRANCHSORTCODE + "=? ";
    String strBusinessDate = null;

    StringBuffer fileData = new StringBuffer();

    IPagingData pageData = null;

    private ArrayList params = new ArrayList();
    private List list = new ArrayList();
    private ArrayList branchRange = new ArrayList();
    private static Properties fileProp;
    private static Properties filePropBPRef;

    private Date businessDate = null;
    // artf574077-To maximize I/O performance, BufferedOutPutStream is introduced.
    private BufferedOutputStream fout = null;
    private BankFusionEnvironment env = null;
    private AbstractProcessAccumulator accumulator;

    private int accPageSize = 1000;

    int totalCount = 0;

    mcfalen mcfalenObj = new mcfalen();
    private Object currencyHash;

    private static Map<String, String> bpRefreshPropertiesMap = new HashMap<String, String>();

    private static Map<String, String> refreshPropertiesMap = new HashMap<String, String>();

    /**
     * <code>mcfaData</code> Account Detail Record Structure class
     */
    private class mcfaData {

        public String FA_DTL_REC_TYPE = "";
        public String FA_DTL_ACC_ID = "";
        public String FA_DTL_SHORTNAME1 = "";
        public String FA_DTL_SHORTNAME2 = "";
        public String FA_DTL_LED_SUBLED = "";
        public String FA_DTL_CURR = "";
        public String FA_DTL_ACC_TYPE = "";
        public String FA_DTL_ACC_TYPE_DESC = "";
        public String FA_DTL_CLIENT_NUMBER = "";
        public String FA_DTL_STOP_BLK_IND = "";
        public String FA_DTL_DEBIT_LIMIT = "0";
        public String FA_DTL_DEBIT_LIMIT_EXP = "0";
        public String FA_DTL_FILLER1 = "";
        public String FA_DTL_CLEARED_BAL = "0";
        public String FA_DTL_BOOK_BAL = "0";
        public String FA_DTL_GARN_HOLD = "0";
        public String FA_DTL_LIEN_HOLD = "0";
        public String FA_DTL_BLOCK_BAL = "0";
        public String FA_DTL_SEC_RATING = "0";
        public String FA_DTL_STAFF_IND = "";
        public String FA_DTL_CREDIT_LIMIT = "0";
        public String FA_DTL_CREDIT_LIMIT_EXP = "0";
        public String FA_DTL_ARREARS_BAL = "0";
        public String FA_DTL_DATE_LAST_EXT = "0";
        public String FA_DTL_ACCRUED_INT = "0";
        public String FA_DTL_1_DAYS_ACCD_INT = "";
        public String FA_DTL_ACCD_INT_DATE = "0";
        public String FA_DTL_RATE_BASIS = "";
        public String FA_DTL_TOTAL_LOAN = "0";
        public String FA_DTL_TOTAL_LOAN_DRAW = "0";
        public String FA_DTL_ALT_IDENTIFIER = "";
        public String FA_DTL_ALT_ID_FILLER = "";
        public String FA_DTL_PASS_PROT_FLAG = "";
        public String FA_DTL_LIMIT_CHK_FLAG = "";
        public String FA_DTL_LIMIT_NUMB_1 = "";
        public String FA_DTL_LIMIT_NUMB_2 = "";
        public String FA_DTL_LIMIT_NUMB_3 = "";
        public String FA_DTL_LIMIT_NUMB_4 = "";
        public String FA_DTL_LIMIT_NUMB_5 = "";
        public String FA_DTL_SUM_CHQ_DEP_BAL = "";
        public String FA_DTL_FILLER_2 = "";
        public String FA_DTL_BRANCH = "";
        public String FA_DTL_ACTION = "";
        public String FA_DTL_CHECKSUM = "";
        public String FA_DTL_FILLER_3 = "";

    }

    protected static Properties prop = new Properties();
    static {
        String path = GetUBConfigLocation.getUBConfigLocation();
        try {
            InputStream input = new FileInputStream(path + "/conf/bpRefresh/Refresh.properties");
            prop = new java.util.Properties();

            prop.load(input);
            input.close();

        }
        catch (Exception e) {
            prop = new Properties();
        }

    }

    // artf574077
    // refreshAccount() method was creating wrapper class object ( new Integer(getProperty())for
    // setting the field length in the file. Which is an overhead, therefore a new inner
    // class-mcfalen()which
    // will hold the values of each field length and then this value is further used to get the
    // field length.
    private class mcfalen {

        final int FA_DTL_REC_TYPE = new Integer((String) prop.get("FA-DTL-REC-TYPE")).intValue();
        final int FA_DTL_ACC_ID = new Integer((String) prop.get("FA-DTL-ACC-ID")).intValue();
        final int FA_DTL_SHORTNAME1 = new Integer((String) prop.get("FA-DTL-SHORTNAME1")).intValue();
        final int FA_DTL_SHORTNAME2 = new Integer((String) prop.get("FA-DTL-SHORTNAME2")).intValue();
        final int FA_DTL_LED_SUBLED = new Integer((String) prop.get("FA-DTL-LED-SUBLED")).intValue();
        final int FA_DTL_CURR = new Integer((String) prop.get("FA-DTL-CURR")).intValue();
        final int FA_DTL_ACC_TYPE = new Integer((String) prop.get("FA-DTL-ACC-TYPE")).intValue();
        final int FA_DTL_ACC_TYPE_DESC = new Integer((String) prop.get("FA-DTL-ACC-TYPE-DESC")).intValue();
        final int FA_DTL_CLIENT_NUMBER = new Integer((String) prop.get("FA-DTL-CLIENT-NUMBER")).intValue();
        final int FA_DTL_STOP_BLK_IND = new Integer((String) prop.get("FA-DTL-STOP-BLK-IND")).intValue();
        final int FA_DTL_DEBIT_LIMIT = new Integer((String) prop.get("FA-DTL-DEBIT-LIMIT")).intValue();
        final int FA_DTL_DEBIT_LIMIT_EXP = new Integer((String) prop.get("FA-DTL-DEBIT-LIMIT-EXP")).intValue();
        final int FA_DTL_FILLER1 = new Integer((String) prop.get("FA-DTL-FILLER1")).intValue();
        final int FA_DTL_CLEARED_BAL = new Integer((String) prop.get("FA-DTL-CLEARED-BAL")).intValue();
        final int FA_DTL_BOOK_BAL = new Integer((String) prop.get("FA-DTL-BOOK-BAL")).intValue();
        final int FA_DTL_GARN_HOLD = new Integer((String) prop.get("FA-DTL-GARN-HOLD")).intValue();
        final int FA_DTL_LIEN_HOLD = new Integer((String) prop.get("FA-DTL-LIEN-HOLD")).intValue();
        final int FA_DTL_BLOCK_BAL = new Integer((String) prop.get("FA-DTL-BLOCK-BAL")).intValue();
        final int FA_DTL_SEC_RATING = new Integer((String) prop.get("FA-DTL-SEC-RATING")).intValue();
        final int FA_DTL_STAFF_IND = new Integer((String) prop.get("FA-DTL-STAFF-IND")).intValue();
        final int FA_DTL_CREDIT_LIMIT = new Integer((String) prop.get("FA-DTL-CREDIT-LIMIT")).intValue();
        final int FA_DTL_CREDIT_LIMIT_EXP = new Integer((String) prop.get("FA-DTL-CREDIT-LIMIT-EXP")).intValue();
        final int FA_DTL_ARREARS_BAL = new Integer((String) prop.get("FA-DTL-ARREARS-BAL")).intValue();
        final int FA_DTL_DATE_LAST_EXT = new Integer((String) prop.get("FA-DTL-DATE-LAST-EXT")).intValue();
        final int FA_DTL_ACCRUED_INT = new Integer((String) prop.get("FA-DTL-ACCRUED-INT")).intValue();
        final int FA_DTL_1_DAYS_ACCD_INT = new Integer((String) prop.get("FA-DTL-1-DAYS-ACCD-INT")).intValue();
        final int FA_DTL_ACCD_INT_DATE = new Integer((String) prop.get("FA-DTL-ACCD-INT-DATE")).intValue();
        final int FA_DTL_RATE_BASIS = new Integer((String) prop.get("FA-DTL-RATE-BASIS")).intValue();
        final int FA_DTL_TOTAL_LOAN = new Integer((String) prop.get("FA-DTL-TOTAL-LOAN")).intValue();
        final int FA_DTL_TOTAL_LOAN_DRAW = new Integer((String) prop.get("FA-DTL-TOTAL-LOAN-DRAW")).intValue();
        final int FA_DTL_ALT_IDENTIFIER = new Integer((String) prop.get("FA-DTL-ALT-IDENTIFIER")).intValue();
        final int FA_DTL_ALT_ID_FILLER = new Integer((String) prop.get("FA-DTL-ALT-ID-FILLER")).intValue();
        final int FA_DTL_PASS_PROT_FLAG = new Integer((String) prop.get("FA-DTL-PASS-PROT-FLAG")).intValue();
        final int FA_DTL_LIMIT_CHK_FLAG = new Integer((String) prop.get("FA-DTL-LIMIT-CHK-FLAG")).intValue();
        final int FA_DTL_LIMIT_NUMB_1 = new Integer((String) prop.get("FA-DTL-LIMIT-NUMB-1")).intValue();
        final int FA_DTL_LIMIT_NUMB_2 = new Integer((String) prop.get("FA-DTL-LIMIT-NUMB-2")).intValue();
        final int FA_DTL_LIMIT_NUMB_3 = new Integer((String) prop.get("FA-DTL-LIMIT-NUMB-3")).intValue();
        final int FA_DTL_LIMIT_NUMB_4 = new Integer((String) prop.get("FA-DTL-LIMIT-NUMB-4")).intValue();
        final int FA_DTL_LIMIT_NUMB_5 = new Integer((String) prop.get("FA-DTL-LIMIT-NUMB-5")).intValue();
        final int FA_DTL_SUM_CHQ_DEP_BAL = new Integer((String) prop.get("FA-DTL-SUM-CHQ-DEP-BAL")).intValue();
        final int FA_DTL_FILLER_2 = new Integer((String) prop.get("FA-DTL-FILLER-2")).intValue();
        final int FA_DTL_BRANCH = new Integer((String) prop.get("FA-DTL-BRANCH")).intValue();
        final int FA_DTL_ACTION = new Integer((String) prop.get("FA-DTL-ACTION")).intValue();
        final int FA_DTL_CHECKSUM = new Integer((String) prop.get("FA-DTL-CHECKSUM")).intValue();
        final int FA_DTL_FILLER_3 = new Integer((String) prop.get("FA-DTL-FILLER-3")).intValue();

    }

    /**
     * @param environment
     *            Used to get a handle on the BankFusion environment
     * @param context
     *            A set of data passed to the PreProcess, Process and PostProcess classes
     * @param priority
     *            Thread priority
     */
    public BranchPowerAccountRefreshProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
        super(environment, context, priority);
        this.context = context;
        env = environment;

        Object[] additionalParameters = context.getAdditionalProcessParams();
        filePropBPRef = (Properties) additionalParameters[0];
        bpRefreshPropertiesMap = (HashMap<String, String>) additionalParameters[3];
        fromBranchCode = Integer.parseInt(bpRefreshPropertiesMap.get("FROMBRANCH"));
        toBranchCode = Integer.parseInt(bpRefreshPropertiesMap.get("TOBRANCH"));
        extractPath = bpRefreshPropertiesMap.get("EXTRACTPATH");
        accPageSize = Integer.parseInt(bpRefreshPropertiesMap.get("PAGE-SIZE"));
        fileProp = (Properties) additionalParameters[1];
        currencyHash = (Hashtable) additionalParameters[2];
        refreshPropertiesMap = (HashMap<String, String>) additionalParameters[4];
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
        accumulator = new BranchPowerAccountRefreshAccumulator(accumulatorArgs);
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

        List list = null;
        IBOBranch branchBO = null;

        if (logger.isDebugEnabled())
            logger.debug("Invoking Page: " + pageToProcess);

        pagingData.setCurrentPageNumber(pageToProcess);
        list = env.getFactory().findAll(IBOBranch.BONAME, pagingData);

        Iterator branchIterator = list.iterator();
        while (branchIterator.hasNext()) {
            int accTotalNoPages = CommonConstants.INTEGER_ZERO;
            Iterator<IBOUBTB_BPWACCREFRESHTAG> accountDetailsResultSet = null;
            branchBO = (IBOBranch) branchIterator.next();
            branchCode = Integer.parseInt(branchBO.getF_BMBRANCH());
            params.clear();
            branchSortCode = branchBO.getBoID();
            params.add(branchSortCode);

            try {
                if (branchCode >= fromBranchCode && branchCode <= toBranchCode) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(" Branch Num -> " + branchCode + " From Branch -> " + fromBranchCode + " To branch -> "
                                + toBranchCode);

                    }

                    businessDate = new SimpleDateFormat("yyyy-MM-dd").parse(SystemInformationManager.getInstance()
                            .getBFBusinessDate().toString());
                    strBusinessDate = new SimpleDateFormat("yyyyMMdd").format(businessDate);

                    pageData = new PagingData(1, pageSize);

                    accTotalNoPages = getTotalNumberOfPages(accPageSize, branchSortCode);
                    openRefreshFile(extractPath + "mcfa" + branchBO.getF_BMBRANCH().toString() + ".dat");
                    formatAccountHeader(branchCode + "", fout, env);
                    totalCount = 0;
                    for (int i = 1; i <= accTotalNoPages; i++) {
                        // artf574077-passing the second parameter for pagination
                        populateAccount(env, i);

                    }

                    formatAccountTrail(String.valueOf(totalCount), fout, env);
                    fout.close();
                }
            }
            catch (BankFusionException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(logException(e));
                }

            }
            catch (ParseException textException) {
                if (logger.isErrorEnabled()) {
                    logger.error(logException(textException));
                }

            }
            catch (IOException ioExp) {
                if (logger.isErrorEnabled()) {
                    logger.error(logException(ioExp));
                }

            }
        }
        return accumulator;
    }

    /**
     * performs the main customer refresh outputs branchwise data to mcfc9999.dat
     * 
     * @param env
     * @throws BankFusionException
     */
    private void populateAccount(BankFusionEnvironment env, int currentPage) throws BankFusionException {

        PagingData accPagingData = new PagingData(0, accPageSize);
        accPagingData.setCurrentPageNumber(currentPage);
        int fromValue = ((currentPage - 1) * accPageSize) + 1;
        int toValue = currentPage * accPageSize;

        ArrayList params = new ArrayList();
        params.add(branchSortCode);
        params.add(fromValue);
        params.add(toValue);

        List accountDetails = null;

        SimplePersistentObject accountView = null;
        BigDecimal intAmount = CommonConstants.BIGDECIMAL_ZERO;
        try {

            accountDetails = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(queryAccount, params, null, true);

            if (accountDetails == null) {

                return;
            }

            Iterator accountIter = accountDetails.iterator();

            while (accountIter.hasNext()) {
                try {
                    accountView = (SimplePersistentObject) accountIter.next();
                    fileData = new StringBuffer();
                    intAmount = CommonConstants.BIGDECIMAL_ZERO;
                    mcfaData mcfa = new mcfaData();
                    mcfa.FA_DTL_REC_TYPE = "02";
                    mcfa.FA_DTL_ACC_ID = (String) accountView.getDataMap().get(IBOAccount.ACCOUNTID);
                    // artf574077
                    // modified to log the error if the null or empty value is encountered for a
                    // particular account (using catch block)and continue further with extraction.
                    if (null != (String) accountView.getDataMap().get(IBOAccount.ACCOUNTNAME)) {
                        if (accountView.getDataMap().get(IBOAccount.ACCOUNTNAME).toString().length() > 30)
                            mcfa.FA_DTL_SHORTNAME1 = accountView.getDataMap().get(IBOAccount.ACCOUNTNAME).toString()
                                    .substring(0, 29).trim();
                        else mcfa.FA_DTL_SHORTNAME1 = ((String) accountView.getDataMap().get(IBOAccount.ACCOUNTNAME)).trim();
                    }
                    mcfa.FA_DTL_LED_SUBLED = mcfa.FA_DTL_ACC_ID.substring(2, 5);
                    mcfa.FA_DTL_CURR = (String) accountView.getDataMap().get(IBOAccount.ISOCURRENCYCODE);
                    if (accountView.getDataMap().get(IBOAccPortMap.PORTFOLIOID).toString().length() > 9)
                        mcfa.FA_DTL_CLIENT_NUMBER = accountView.getDataMap().get(IBOAccPortMap.PORTFOLIOID).toString()
                                .substring(0, 8);
                    else mcfa.FA_DTL_CLIENT_NUMBER = (String) accountView.getDataMap().get(IBOAccPortMap.PORTFOLIOID);

                    mcfa.FA_DTL_STOP_BLK_IND = "0";
                    mcfa.FA_DTL_STOP_BLK_IND = (accountView.getDataMap().get(IBOUBTB_BPWACCREFRESHTAG.STOPPED)).toString() == "true" ? "S"
                            : "0";

                    if (!(mcfa.FA_DTL_STOP_BLK_IND.equals("S"))) {
                        mcfa.FA_DTL_STOP_BLK_IND = (accountView.getDataMap().get(IBOUBTB_BPWACCREFRESHTAG.CLOSED)).toString() == "true" ? "S"
                                : "0";
                    }

                    if (accountView.getDataMap().get(IBOUBTB_BPWACCREFRESHTAG.ACCRIGHTSINDICATOR).equals("-1")) {
                        mcfa.FA_DTL_PASS_PROT_FLAG = "P";
                    }
                    else {
                        mcfa.FA_DTL_PASS_PROT_FLAG = accountView.getDataMap().get(IBOUBTB_BPWACCREFRESHTAG.ACCRIGHTSINDICATOR)
                                .toString();
                    }

                    /*
                     * Truncation of amount is removed in case of more than 18 digits and modified
                     * it to set 13 nines in case of more than 14 digit balances.
                     */
                    BigDecimal clearedBalance = (BigDecimal) accountView.getDataMap().get(IBOAccount.CLEAREDBALANCE);
                    if (clearedBalance.unscaledValue().abs().toString().length() > 14) {

                        mcfa.FA_DTL_CLEARED_BAL = setNines(clearedBalance);

                    }
                    else mcfa.FA_DTL_CLEARED_BAL = accountView.getDataMap().get(IBOAccount.CLEAREDBALANCE).toString();

                    BigDecimal bookedBalance = (BigDecimal) accountView.getDataMap().get(IBOAccount.BOOKEDBALANCE);
                    if (bookedBalance.unscaledValue().abs().toString().length() > 14) {

                        mcfa.FA_DTL_BOOK_BAL = setNines(bookedBalance);
                    }
                    else mcfa.FA_DTL_BOOK_BAL = accountView.getDataMap().get(IBOAccount.BOOKEDBALANCE).toString();

                    mcfa.FA_DTL_ACCRUED_INT = "0";
                    mcfa.FA_DTL_ALT_IDENTIFIER = (String) accountView.getDataMap().get(IBOAccount.PSEUDONAME);
                    mcfa.FA_DTL_LIMIT_CHK_FLAG = "";

                    if (null != (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF1)) {
                        if (accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF1).toString().length() > 2)
                            mcfa.FA_DTL_LIMIT_NUMB_1 = accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF1).toString()
                                    .substring(0, 2);
                        else mcfa.FA_DTL_LIMIT_NUMB_1 = (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF1);
                    }
                    if (null != (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF2)) {
                        if (accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF2).toString().length() > 2)
                            mcfa.FA_DTL_LIMIT_NUMB_2 = accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF2).toString()
                                    .substring(0, 2);
                        else mcfa.FA_DTL_LIMIT_NUMB_2 = (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF2);
                    }
                    if (null != (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF3)) {
                        if (accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF3).toString().length() > 2)
                            mcfa.FA_DTL_LIMIT_NUMB_3 = accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF3).toString()
                                    .substring(0, 2);
                        else mcfa.FA_DTL_LIMIT_NUMB_3 = (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF3);
                    }

                    if (null != (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF4)) {
                        if (accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF4).toString().length() > 2)

                            mcfa.FA_DTL_LIMIT_NUMB_4 = accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF4).toString()
                                    .substring(0, 2);
                        else mcfa.FA_DTL_LIMIT_NUMB_4 = (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF4);
                    }
                    if (null != (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF5)) {
                        if (accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF5).toString().length() > 2)
                            mcfa.FA_DTL_LIMIT_NUMB_5 = accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF5).toString()
                                    .substring(0, 2);
                        else mcfa.FA_DTL_LIMIT_NUMB_5 = (String) accountView.getDataMap().get(IBOProductInheritance.LIM_LIMITREF5);
                    }
                    BigDecimal chqDepBal = (BigDecimal) accountView.getDataMap().get(IBOAccount.CHEQUEDEPOSITBALANCE);
                    if (chqDepBal.unscaledValue().abs().toString().length() > 14) {

                        mcfa.FA_DTL_SUM_CHQ_DEP_BAL = setNines(chqDepBal);

                    }
                    else mcfa.FA_DTL_SUM_CHQ_DEP_BAL = accountView.getDataMap().get(IBOAccount.CHEQUEDEPOSITBALANCE).toString();

                    if (null != (String) accountView.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION)) {
                        if (accountView.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION).toString().length() > 30)
                            mcfa.FA_DTL_ACC_TYPE_DESC = accountView.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION).toString()
                                    .substring(0, 29).trim();
                        else mcfa.FA_DTL_ACC_TYPE_DESC = ((String) accountView.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION)).trim();
                    }
                    mcfa.FA_DTL_ACC_TYPE = accountView.getDataMap().get(IBOProductInheritance.PRODUCT_NUMERICCODE).toString();

                    BigDecimal debitLimit = (BigDecimal) accountView.getDataMap().get(IBOAccount.DEBITLIMIT);
                    if (debitLimit.unscaledValue().abs().toString().length() > 14) {

                        mcfa.FA_DTL_DEBIT_LIMIT = setNines(debitLimit);
                    }
                    else mcfa.FA_DTL_DEBIT_LIMIT = accountView.getDataMap().get(IBOAccount.DEBITLIMIT).toString();

                    mcfa.FA_DTL_DEBIT_LIMIT_EXP = "";

                    BigDecimal creditLimit = (BigDecimal) accountView.getDataMap().get(IBOAccount.CREDITLIMIT);
                    if (creditLimit.unscaledValue().abs().toString().length() > 14) {

                        mcfa.FA_DTL_CREDIT_LIMIT = setNines(creditLimit);
                    }
                    else mcfa.FA_DTL_CREDIT_LIMIT = accountView.getDataMap().get(IBOAccount.CREDITLIMIT).toString();

                    mcfa.FA_DTL_CREDIT_LIMIT_EXP = "";
                    mcfa.FA_DTL_LIMIT_CHK_FLAG = accountView.getDataMap().get(IBOAccount.LIMITINDICATOR).toString();

                    Date dtTmp = null; // date format = 2006-04-09 20:17:55449

                    mcfa.FA_DTL_BRANCH = branchCode + "";
                    mcfa.FA_DTL_ACTION = "A";

                    intAmount = intAmount.add((BigDecimal) accountView.getDataMap().get(IBOCreditInterestFeature.ACCDCRINTEREST));
                    intAmount = intAmount
                            .add(((BigDecimal) accountView.getDataMap().get(IBODebitInterestFeature.DEBITACCDINTEREST)).negate());

                    if (intAmount.unscaledValue().abs().toString().length() > 14) {

                        mcfa.FA_DTL_ACCRUED_INT = setNines(intAmount);

                    }
                    else mcfa.FA_DTL_ACCRUED_INT = intAmount.toString();

                    // accHash.put(accountView.getDataMap().get(IBOAccount.BRANCHSORTCODE).toString()
                    // +
                    // mcfa.FA_DTL_ACC_ID, mcfa);
                    // artf574077- deleted HashTable, TreeMap which is not required at all.
                    refreshAccount(env, mcfa);
                    // = new TreeMap();

                    totalCount++;

                }
                // artf574077- The exception handling and error logging modification
                catch (NullPointerException exp) {

                    if (logger.isErrorEnabled()) {
                        logger.error("For Account No :--> " + accountView.getDataMap().get(IBOAccount.ACCOUNTID)
                                + " data is not available");
                        logger.error(logException(exp));
                    }
                }

            }
            fout.flush();

        }
        catch (FileNotFoundException fnfExcpn) {
            if (logger.isErrorEnabled()) {
                logger.error(logException(fnfExcpn));
            }
            BranchPowerAccountRefreshFatomContext.Status = Boolean.FALSE;
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (IOException ioExcpn) {
            if (logger.isErrorEnabled()) {
                logger.error(logException(ioExcpn));
            }
            BranchPowerAccountRefreshFatomContext.Status = Boolean.FALSE;
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (Exception Excpn) {
            if (logger.isErrorEnabled()) {
                logger.error(logException(Excpn));
            }
            BranchPowerAccountRefreshFatomContext.Status = Boolean.FALSE;
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
        }

    }

    /**
     * Method to return all 14 nines with sign included depends on debit/credit amount.
     * 
     * @param amount
     * @return
     */
    private String setNines(BigDecimal amount) {

        String nines = "9999999999999";

        if (amount.signum() == -1)
            nines = "-" + nines;

        return nines;
    }

    private void refreshAccount(BankFusionEnvironment env, mcfaData mcfdet) throws Exception {
        // artf574077-refreshAccount() method was creating wrapper class object ( new
        // Integer(getProperty())for
        // setting the field length in the file. Which is an overhead. Deleted and implemented same
        // using new inner
        // class-mcfalen()
        int scale = bizInfo.getCurrencyScale(mcfdet.FA_DTL_CURR, BankFusionThreadLocal.getBankFusionEnvironment());

        fileData = new StringBuffer();
        fileData.append(setField(mcfalenObj.FA_DTL_REC_TYPE, "02", 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_ACC_ID, mcfdet.FA_DTL_ACC_ID, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_SHORTNAME1, mcfdet.FA_DTL_SHORTNAME1, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_SHORTNAME2, mcfdet.FA_DTL_SHORTNAME2, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_LED_SUBLED, mcfdet.FA_DTL_LED_SUBLED, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_CURR, mcfdet.FA_DTL_CURR, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_ACC_TYPE, mcfdet.FA_DTL_ACC_TYPE, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_ACC_TYPE_DESC, mcfdet.FA_DTL_ACC_TYPE_DESC, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_CLIENT_NUMBER, mcfdet.FA_DTL_CLIENT_NUMBER, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_STOP_BLK_IND, mcfdet.FA_DTL_STOP_BLK_IND, 'A'));
        setAmount(new BigDecimal(mcfdet.FA_DTL_DEBIT_LIMIT), mcfalenObj.FA_DTL_DEBIT_LIMIT, fileData, scale);
        fileData.append(setField(mcfalenObj.FA_DTL_DEBIT_LIMIT_EXP, mcfdet.FA_DTL_DEBIT_LIMIT_EXP, 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_FILLER1, "", 'A'));
        setAmount(new BigDecimal(mcfdet.FA_DTL_CLEARED_BAL), mcfalenObj.FA_DTL_CLEARED_BAL, fileData, scale);
        setAmount(new BigDecimal(mcfdet.FA_DTL_BOOK_BAL), mcfalenObj.FA_DTL_BOOK_BAL, fileData, scale);
        fileData.append(setField(mcfalenObj.FA_DTL_GARN_HOLD, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_LIEN_HOLD, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_BLOCK_BAL, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_SEC_RATING, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_STAFF_IND, "", 'A'));
        setAmount(new BigDecimal(mcfdet.FA_DTL_CREDIT_LIMIT), mcfalenObj.FA_DTL_CREDIT_LIMIT, fileData, scale);
        fileData.append(setField(mcfalenObj.FA_DTL_CREDIT_LIMIT_EXP, mcfdet.FA_DTL_CREDIT_LIMIT_EXP, 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_ARREARS_BAL, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_DATE_LAST_EXT, mcfdet.FA_DTL_DATE_LAST_EXT, 'N'));
        setAmount(new BigDecimal(mcfdet.FA_DTL_ACCRUED_INT), mcfalenObj.FA_DTL_ACCRUED_INT, fileData, scale);
        fileData.append(setField(mcfalenObj.FA_DTL_1_DAYS_ACCD_INT, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_RATE_BASIS, "", 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_ACCD_INT_DATE, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_TOTAL_LOAN, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_TOTAL_LOAN_DRAW, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_ALT_IDENTIFIER, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_ALT_ID_FILLER, "", 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_PASS_PROT_FLAG, mcfdet.FA_DTL_PASS_PROT_FLAG, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_LIMIT_CHK_FLAG, mcfdet.FA_DTL_LIMIT_CHK_FLAG, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_LIMIT_NUMB_1, mcfdet.FA_DTL_LIMIT_NUMB_1, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_LIMIT_NUMB_2, mcfdet.FA_DTL_LIMIT_NUMB_2, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_LIMIT_NUMB_3, mcfdet.FA_DTL_LIMIT_NUMB_3, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_LIMIT_NUMB_4, mcfdet.FA_DTL_LIMIT_NUMB_4, 'A'));
        fileData.append(setField(mcfalenObj.FA_DTL_LIMIT_NUMB_5, mcfdet.FA_DTL_LIMIT_NUMB_5, 'A'));
        setAmount(new BigDecimal(mcfdet.FA_DTL_SUM_CHQ_DEP_BAL), mcfalenObj.FA_DTL_SUM_CHQ_DEP_BAL, fileData, scale);
        fileData.append(setField(mcfalenObj.FA_DTL_FILLER_2, "", 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_BRANCH, mcfdet.FA_DTL_BRANCH, 'N'));
        fileData.append(setField(mcfalenObj.FA_DTL_ACTION, "A", 'A'));
        BigDecimal checksum = new BigDecimal(0).setScale(0);
        try {
            checksum = new BigDecimal(mcfdet.FA_DTL_BRANCH)
                    .add(new BigDecimal(mcfdet.FA_DTL_DEBIT_LIMIT).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_CREDIT_LIMIT).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_CLEARED_BAL).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_BOOK_BAL).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_ARREARS_BAL).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_DATE_LAST_EXT))
                    .add(new BigDecimal(mcfdet.FA_DTL_ACCRUED_INT).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_ACCD_INT_DATE))
                    .add(new BigDecimal(mcfdet.FA_DTL_TOTAL_LOAN).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_TOTAL_LOAN_DRAW))
                    .add(new BigDecimal(mcfdet.FA_DTL_GARN_HOLD).movePointRight(scale).abs())
                    .add(new BigDecimal(mcfdet.FA_DTL_LIEN_HOLD)).add(new BigDecimal(mcfdet.FA_DTL_SEC_RATING))
                    .add(new BigDecimal(mcfdet.FA_DTL_SUM_CHQ_DEP_BAL).movePointRight(scale).abs());

        }
        catch (NumberFormatException nfException) {
            // continue;
            fout.flush();
            return;
        }
        BigInteger cksum = checksum.toBigInteger();
        fileData.append(setField(new Integer(getProperty("FA-DTL-CHECKSUM")).intValue(), cksum.toString(), 'N'));
        fileData.append(setField(new Integer(getProperty("FA-DTL-FILLER-3")).intValue(), "", 'A'));
        fileData.append("\r\n");
        fout.write(fileData.toString().getBytes());
        // fout.flush();

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

        /*
         * Added Rounding Method to avoid ArithmeticException:Rouding Necessary
         */

        bdAmt = bdAmt.setScale(scale, BigDecimal.ROUND_HALF_UP);
        String Amount = bdAmt.unscaledValue().abs().toString();

        if (bdAmt.signum() == -1) {
            fileData.append(setField(new Integer(getProperty(fldName)).intValue() - 1, Amount.substring(0, Amount.length() - 1),
                    'N'));
            char str = Amount.charAt(Amount.length() - 1);
            fileData.append(getProperty(String.valueOf(str)));
        }
        else {
            fileData.append(setField(new Integer(getProperty(fldName)).intValue(), Amount, 'N'));
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
    private static void setAmount(BigDecimal bdAmt, int fldLen, StringBuffer fileData, int scale) {

        /*
         * Added Rounding Method to avoid ArithmeticException:Rouding Necessary
         */

        bdAmt = bdAmt.setScale(scale, BigDecimal.ROUND_HALF_UP);
        String Amount = bdAmt.unscaledValue().abs().toString();

        if (bdAmt.signum() == -1) {
            fileData.append(setField(fldLen - 1, Amount.substring(0, Amount.length() - 1), 'N'));
            char str = Amount.charAt(Amount.length() - 1);
            fileData.append(getProperty(String.valueOf(str)));
        }
        else {
            fileData.append(setField(fldLen, Amount, 'N'));
        }
    }

    /**
     * formats and writes the account header record to mcfc9999.dat
     * 
     * @param Branch
     * @param fout
     * @throws ParseException
     */
    private void formatAccountHeader(String Branch, BufferedOutputStream fout, BankFusionEnvironment env)
            throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        hdrActionFlag = bpRefreshPropertiesMap.get("HDRACTIONFLAG");
        fileData.append(setField(new Integer(getProperty("FA-HDR-REC-TYPE")).intValue(), "01", 'A'));
        fileData.append(setField(new Integer(getProperty("FA-HDR-ACTION")).intValue(), hdrActionFlag, 'A'));
        fileData.append(setField(new Integer(getProperty("FA-HDR-SOURCE-SYSTEM")).intValue(), "MCAS", 'A'));
        fileData.append(setField(new Integer(getProperty("FA-HDR-DEST-SYSTEM")).intValue(), "BPWR", 'A'));
        fileData.append(setField(new Integer(getProperty("FA-HDR-BRANCH-CODE")).intValue(), Branch, 'N'));
        fileData.append(setField(new Integer(getProperty("FA-HDR-FILE-ID")).intValue(), "AC", 'A'));
        fileData.append(setField(new Integer(getProperty("FA-HDR-PROCESS-DATE")).intValue(), strBusinessDate, 'N'));
        fileData.append(setField(new Integer(getProperty("FA-HDR-FILLER")).intValue(), "0", 'N'));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
        }
        catch (IOException ioExcpn) {

            if (logger.isInfoEnabled()) {
                logger.info(logException(ioExcpn));
            }
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
    }

    /**
     * formats and writes the mcfa9999.dat trailer record
     * 
     * @param Branchctr
     * @param fout
     */
    private void formatAccountTrail(String Accountctr, BufferedOutputStream fout, BankFusionEnvironment env)
            throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getProperty("FA-TRL-REC-TYPE")).intValue(), "99", 'A'));
        fileData.append(setField(new Integer(getProperty("FA-TRL-REC-COUNT")).intValue(), Accountctr, 'N'));
        fileData.append(setField(new Integer(getProperty("FA-TRL-FILLER")).intValue(), "0", 'N'));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();

        }
        catch (NullPointerException npExcpn) {
            return;
        }
        catch (IOException ioExcpn) {

            if (logger.isInfoEnabled()) {
                logger.info(logException(ioExcpn));
            }

            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
    }

    /**
     * reads the property for the key passed
     * 
     * @param sKey
     * @return
     */
    private static String getProperty(String sKey) {
        return refreshPropertiesMap.get(sKey);
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

    // artf574077- Total no of pages is derived to enable paging support to refresh account per
    // branch.
    private int getTotalNumberOfPages(int accPageSize, String branchSortCode) {

        int count = 0;

        int totalPage = 0;

        ArrayList params = new ArrayList();

        params.add(branchSortCode);

        ArrayList columnList = new ArrayList();

        columnList.add(IBOUBTB_BPWACCREFRESHTAG.BRANCHSORTCODE);

        List list = BankFusionThreadLocal.getPersistanceFactory().aggregateFunction(IBOUBTB_BPWACCREFRESHTAG.BONAME,
                branchSortCodeQuery, params, null,

                BankFusionThreadLocal.getPersistanceFactory().COUNT_FUNCTION_CODE, columnList, false);

        count = ((Integer) ((SimplePersistentObject) list.get(0)).getDataMap().get(IBOUBTB_BPWACCREFRESHTAG.BRANCHSORTCODE))
                .intValue();

        totalPage = (count % accPageSize == 0) ? (count / accPageSize) : (count / accPageSize) + 1;

        return totalPage;

    }

    private void openRefreshFile(String fName) {
        // Get the file name from the properties file

        File foutLocal = new File(fName);
        logger.info(fName);
        if (foutLocal.exists()) {
            foutLocal.delete();
        }
        BankFusionIOSupport.createNewFile(foutLocal);
        fout = new BufferedOutputStream(BankFusionIOSupport.createBufferedOutputStream(foutLocal, true));
    }

    private void closeRefreshFile() {
        if (fout != null) {
            try {
                fout.close();
            }
            catch (Exception e) {
                // ignore exception if unable to close.

            }
        }

    }

    public static String logException(Throwable ex) {
        StringWriter exWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(exWriter));
        return exWriter.toString();

    }

}