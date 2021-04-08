/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerAccountBundleRefreshProcess.java,v.1.1.2.1,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerAccountBundleRefreshProcess.java,v $
 * Revision 1.1.2.2  2008/08/22 00:26:19  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.accountBundle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.fatoms.batch.bpRefresh.BranchPowerRefreshAccumulator;
import com.misys.ub.fatoms.batch.bpRefresh.BranchPowerRefreshFatomContext;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.BatchProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountBundle;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOBundleDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CMN_BatchProcessLog;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

public class BranchPowerAccountBundleRefreshProcess extends AbstractBatchProcess {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final transient Log logger = LogFactory.getLog(BranchPowerAccountBundleRefreshProcess.class.getName());

    private static final String BranchPowerWhereClause = " WHERE " + IBOBranch.BMBRANCH + " BETWEEN ? AND ? ";

    // Initialize variables to be read from properties file
    public String fromBranch = "";
    public String toBranch = "";
    public String extractPath = "";
    public String AccBundleFlag = "";
    String strHdrDt = "";

    private int pageSize = 0;
    private ArrayList branchRange;
    Hashtable CurrencyHash = new Hashtable();
    private BankFusionEnvironment env = null;
    private AbstractProcessAccumulator accumulator;
    static Properties fileProp;

    final String queryCurrency = "SELECT (T1." + IBOCurrency.ISOCURRENCYCODE + ") AS " + IBOCurrency.ISOCURRENCYCODE + ",(T1."
            + IBOCurrency.CURRENCYSCALE + ") AS " + IBOCurrency.CURRENCYSCALE + " FROM " + IBOCurrency.BONAME + " T1";

    final String queryAccountBundle = "SELECT (T2." + IBOBundleDetails.BUNDLECODE + ") AS " + IBOBundleDetails.BUNDLECODE + ",(T1."
            + IBOAccountBundle.BUNDLECODE + ") AS " + IBOAccountBundle.BUNDLECODE + ",(T1." + IBOAccountBundle.ACCOUNTID + ") AS "
            + IBOAccountBundle.ACCOUNTID + ",(T2." + IBOBundleDetails.ACCOUNTSTYLE + ") AS " + IBOBundleDetails.ACCOUNTSTYLE
            + ",(T2." + IBOBundleDetails.THRESHOLDTXNCOUNT + ") AS " + IBOBundleDetails.THRESHOLDTXNCOUNT + " FROM "
            + IBOAccountBundle.BONAME + " T1," + IBOBundleDetails.BONAME + " T2," + IBOAccount.BONAME + " T3" + " WHERE " + " T1."
            + IBOAccountBundle.BUNDLECODE + "= T2." + IBOBundleDetails.BUNDLECODE + " AND T3." + IBOAccount.ACCOUNTID + "= T1."
            + IBOAccountBundle.ACCOUNTID;

    TreeMap accHash = new TreeMap();
    TreeMap limitHash = new TreeMap();

    StringBuffer fileData = new StringBuffer();

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
    public BranchPowerAccountBundleRefreshProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
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
        accumulator = new BranchPowerRefreshAccumulator(accumulatorArgs);
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

        logger.debug("Invoking Page: " + pageToProcess);
        Object[] additionalParameters = context.getAdditionalProcessParams();
        fileProp = (Properties) additionalParameters[0];
        if (fileProp == null || fileProp.size() == 0) {
            //throw new BankFusionException(127, new Object[] { "Error Reading Properties File" }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "Error Reading Properties File" }, new HashMap(), env);
        }

        if ((getProperty("FROMBRANCH").equalsIgnoreCase("")) || (getProperty("TOBRANCH").equalsIgnoreCase(""))
                || (getProperty("EXTRACTPATH").equalsIgnoreCase(""))
                || (getProperty("ACCOUNT-BUNDLE-REFRESH").equalsIgnoreCase("")))

        {
           // throw new BankFusionException(127, new Object[] { "Invalid Parameters passed" }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "Invalid Parameters passed" }, new HashMap(), env);
        }
        else {
            fromBranch = getProperty("FROMBRANCH");
            toBranch = getProperty("TOBRANCH");
        }
        toBranch = getProperty("TOBRANCH");
        branchRange = new ArrayList();
        branchRange.add(0, fromBranch);
        branchRange.add(1, toBranch);
        extractPath = getProperty("EXTRACTPATH");
        AccBundleFlag = getProperty("ACCOUNT-BUNDLE-REFRESH");

        int pageSize = Integer.parseInt(getProperty("PAGE-SIZE").toString());
        /* If PAGE-SIZE property in BPRefresh.properties is not set then assign default value 100 */
        if (pageSize == 0) {
            pageSize = 100;
        }

        Date hdrDate = null; // date format = 2006-04-09
        try {
            hdrDate = new SimpleDateFormat("yyyy-MM-dd").parse(SystemInformationManager.getInstance().getBFBusinessDate()
                    .toString());
        }
        catch (ParseException pExcpn) {
            //throw new BankFusionException(127, new Object[] { pExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { pExcpn.getLocalizedMessage() }, new HashMap(), env);
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
                ArrayList list = new ArrayList();
                list.add(fromBranch);
                list.add(toBranch);
                List branchList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOBranch.BONAME, BranchPowerWhereClause, list, pagingData, false);
                BankFusionThreadLocal.setCurrentPageRecordIDs(branchList);
                Branch = branchList.iterator();
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
//              Set current record id in the thread local
                BankFusionThreadLocal.setCurrentRecordID(branchCode);
                if ((branchCode != null))
                    fileProp = (Properties) additionalParameters[1];
                if (AccBundleFlag.equals("1")) {
                    refreshAccountBundle(env);
                }
                else {
                    logger
                            .debug("BranchPowerAccountBundleRefresh cannot be processed as branch does not belong to specified branchRange");
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
     * Performs the Account bundle file refresh
     * 
     * @param env
     * @throws BankFusionException
     */
    private void refreshAccountBundle(BankFusionEnvironment env) throws BankFusionException {

        List AccountBundleDetails = null;
        AccountBundleDetails = env.getFactory().executeGenericQuery(queryAccountBundle, null, null);
        SimplePersistentObject accBunPO = null;
        FileOutputStream fout = null;
        if (AccountBundleDetails.size() == 0)
            return;
        int accbundCtr = 0;
        try {
            fout = new FileOutputStream(extractPath + "mcfbacct.dat");
            for (int i = 0; i < AccountBundleDetails.size(); i++) {
                accBunPO = (SimplePersistentObject) AccountBundleDetails.get(i);
                if (i == 0) {
                    formatAccountBundleHeader(fout, env);
                }
                if (accBunPO.getDataMap().get(IBOAccountBundle.ACCOUNTID) != null) {

                    fileData = new StringBuffer();
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-DTL-REC-TYPE")).intValue(), "02", 'N'));
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACCOUNT-NUM")).intValue(), (String) accBunPO
                            .getDataMap().get(IBOAccountBundle.ACCOUNTID), 'A'));
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-ACC-STYLE")).intValue(), (String) accBunPO
                            .getDataMap().get(IBOBundleDetails.ACCOUNTSTYLE), 'A'));
                    if (accBunPO.getDataMap().get(IBOAccountBundle.BUNDLECODE).toString().length() > 4)
                        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-BUND-CODE")).intValue(), (String) accBunPO
                                .getDataMap().get(IBOAccountBundle.BUNDLECODE).toString().substring(0, 3), 'A'));
                    else fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-BUND-CODE")).intValue(), (String) accBunPO
                            .getDataMap().get(IBOAccountBundle.BUNDLECODE), 'A'));
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-UPPER-LIMIT")).intValue(), accBunPO.getDataMap()
                            .get(IBOBundleDetails.THRESHOLDTXNCOUNT).toString(), 'N'));
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-ACTIVE-FLAG")).intValue(), "", 'N'));
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-DTL-ACTION")).intValue(), "C", 'A'));
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-DTL-FILLER")).intValue(), "", 'A'));
                    BigDecimal checksum = new BigDecimal(accBunPO.getDataMap().get(IBOBundleDetails.THRESHOLDTXNCOUNT).toString());
                    checksum = checksum.setScale(0, BigDecimal.ROUND_CEILING);
                    fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-DTL-CHECKSUM")).intValue(), checksum.abs()
                            .toString(), 'N'));
                    fileData.append("\r\n");
                    fout.write(fileData.toString().getBytes());
                    fout.flush();
                    accbundCtr++;
                }
            }
            formatAccountBundleTrail(String.valueOf(accbundCtr), fout, env);
            fout.close();
        }
        catch (FileNotFoundException fnfExcpn) {
            BranchPowerRefreshFatomContext.Status = Boolean.FALSE;
            //throw new BankFusionException(127, new Object[] { fnfExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (IOException ioExcpn) {
            BranchPowerRefreshFatomContext.Status = Boolean.FALSE;
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (NumberFormatException nfExcpn) {
            BranchPowerRefreshFatomContext.Status = Boolean.FALSE;
            //throw new BankFusionException(127, new Object[] { nfExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { nfExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (Exception Excpn) {
            BranchPowerRefreshFatomContext.Status = Boolean.FALSE;
            //throw new BankFusionException(127, new Object[] { Excpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
        }

    }

    /**
     * formats and writes the Account Bundle file header to mcfbacct.dat
     * 
     * @param fout
     */
    private void formatAccountBundleHeader(FileOutputStream fout, BankFusionEnvironment env) throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-HDR-REC-TYPE")).intValue(), "01", 'A'));
        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-HDR-FILE-ACTION")).intValue(), "A", 'A'));
        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-HDR-FILE-ID")).intValue(), "BA", 'A'));
        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-HDR-FILLER")).intValue(), "", 'A'));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
        }
        catch (IOException ioExcpn) {
           // throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
    }

    /**
     * formats and writes the Account Bundle file trailer to mcfbacct.dat
     * 
     * @param accbundCtr
     * @param fout
     */
    private void formatAccountBundleTrail(String accbundCtr, FileOutputStream fout, BankFusionEnvironment env)
            throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-TRL-REC-TYPE")).intValue(), "99", 'A'));
        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-TRL-REC-COUNT")).intValue(), accbundCtr, 'N'));
        fileData.append(setField(new Integer(getProperty("FS-BUN-ACC-TRL-REC-FILLER")).intValue(), "", 'A'));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
            fout.close();
        }
        catch (NullPointerException npExcpn) {
            return;
        }
        catch (IOException ioExcpn) {
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
    }

    /**
     * reads the property for the key passed
     * 
     * @param sKey
     * @return
     */
    private static String getProperty(String sKey) {
        String sValue = null;
        sValue = fileProp.get(sKey).toString();
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
