/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerTransBundleRefreshProcess.java,v.1.1.2.1,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerTransBundleRefreshProcess.java,v $
 * Revision 1.1.2.2  2008/08/22 00:26:17  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.transBundle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.BatchProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOBundleDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOBundleDetailsTxnCodeMap;
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

public class BranchPowerTransBundleRefreshProcess extends AbstractBatchProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final transient Log logger = LogFactory.getLog(BranchPowerTransBundleRefreshProcess.class.getName());

    private static final String BranchPowerWhereClause = " WHERE " + IBOBranch.BMBRANCH + " BETWEEN ? AND ? ";

    // Initialize variables to be read from properties file
    public String fromBranch = "";
    public String toBranch = "";
    public String extractPath = "";
    public String custCategory = "";
    public String branchCategory = "";
    public String TransBundleFlag = "";
    String strHdrDt = "";
    ArrayList branchRange;
    Hashtable CurrencyHash = new Hashtable();

    private BankFusionEnvironment env = null;
    // private AbstractFatomContext context=null;

    private AbstractProcessAccumulator accumulator;

    static Properties fileBPRefreshProp;
    static Properties fileRefreshProp;

    // SQL Query to read currency Scale for amount formatting
    final String queryCurrency = "SELECT (T1." + IBOCurrency.ISOCURRENCYCODE + ") AS " + IBOCurrency.ISOCURRENCYCODE + ",(T1."
            + IBOCurrency.CURRENCYSCALE + ") AS " + IBOCurrency.CURRENCYSCALE + " FROM " + IBOCurrency.BONAME + " T1";

    final String queryTransBundle = "SELECT (T1." + IBOBundleDetails.BUNDLECODE + ") AS " + IBOBundleDetails.BUNDLECODE + ",(T2."
            + IBOBundleDetailsTxnCodeMap.TXNCODE + ") AS " + IBOBundleDetailsTxnCodeMap.TXNCODE + ",(T2."
            + IBOBundleDetailsTxnCodeMap.BUNDLECODE + ") AS " + IBOBundleDetailsTxnCodeMap.BUNDLECODE + ",(T2."
            + IBOBundleDetailsTxnCodeMap.CHARGELEG + ") AS " + IBOBundleDetailsTxnCodeMap.CHARGELEG + " FROM "
            + IBOBundleDetails.BONAME + " T1," + IBOBundleDetailsTxnCodeMap.BONAME + " T2" + " WHERE " + " T1."
            + IBOBundleDetails.BUNDLECODE + "= T2." + IBOBundleDetailsTxnCodeMap.BUNDLECODE;

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
    public BranchPowerTransBundleRefreshProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
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
        accumulator = new BranchPowerTransBundleRefreshAccumulator(accumulatorArgs);
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
        fileBPRefreshProp = (Properties) additionalParameters[0];

        if (fileBPRefreshProp == null || fileBPRefreshProp.size() == 0) {
           // throw new BankFusionException(127, new Object[] { "Error Reading Properties File" }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "Error Reading Properties File" }, new HashMap(), env);
        }

        if ((getBPRereshProperty("FROMBRANCH").equalsIgnoreCase("")) || (getBPRereshProperty("TOBRANCH").equalsIgnoreCase(""))
                || (getBPRereshProperty("EXTRACTPATH").equalsIgnoreCase("")) || (getBPRereshProperty("TRANS-BUNDLE-REFRESH").equalsIgnoreCase("")))

        {
           // throw new BankFusionException(127, new Object[] { "Invalid Parameters passed" }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "Invalid Parameters passed"  }, new HashMap(), env);
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
        TransBundleFlag = getBPRereshProperty("TRANS-BUNDLE-REFRESH");

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

                    if (TransBundleFlag.equals("1")) {
                        refreshTransBundle(env);
                    }
                }
                else {
                    logger
                            .debug("BranchPowerTransBundleRefresh cannot be processed as branch does not belong to specified branchRange");
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
     * Performs the Transaction bundle file refresh
     * 
     * @param env
     * @throws BankFusionException
     */
    private void refreshTransBundle(BankFusionEnvironment env) throws BankFusionException {

        List TransBundleDetails = null;
        TransBundleDetails = env.getFactory().executeGenericQuery(queryTransBundle, null, null);
        SimplePersistentObject transBunPO = null;
        FileOutputStream fout = null;
        // 7222
        if (TransBundleDetails.size() == 0)
            return;
        // 7222
        int transbundCtr = 0;
        try {
            fout = new FileOutputStream(extractPath + "mcfbtrans.dat");
            for (int i = 0; i < TransBundleDetails.size(); i++) {
                transBunPO = (SimplePersistentObject) TransBundleDetails.get(i);
                if (i == 0) {
                    formatTransBundleHeader(fout, env);
                }
                if (transBunPO.getDataMap().get(IBOBundleDetails.BUNDLECODE) != null) {
                    // Start_0
                    if (transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.TXNCODE).toString().length() == 2) {
                        // End_0
                        fileData = new StringBuffer();
                        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-REC-TYPE")).intValue(), "02", 'N'));
                        // Start_0
                        if (transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE).toString().length() > 4)
                            // fileData.append(setField(new
                            // Integer(getProperty("FS-BUN-BUND-CODE")).intValue(), (String)
                            // transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE),
                            // 'A'));
                            fileData.append(setField(new Integer(getRereshProperty("FS-BUN-BUND-CODE")).intValue(), (String) transBunPO
                                    .getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE).toString().substring(0, 3), 'A'));
                        else fileData.append(setField(new Integer(getRereshProperty("FS-BUN-BUND-CODE")).intValue(), (String) transBunPO
                                .getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE), 'A'));
                        // End_0
                        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-TYPE")).intValue(), transBunPO.getDataMap()
                                .get(IBOBundleDetailsTxnCodeMap.TXNCODE).toString(), 'A'));

                        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-WTYPE")).intValue(), transBunPO.getDataMap()
                                .get(IBOBundleDetailsTxnCodeMap.CHARGELEG).toString(), 'A'));
                        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-ACTION")).intValue(), "C", 'A'));
                        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-FILLER")).intValue(), "", 'A'));
                        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-CHECKSUM")).intValue(), "", 'N'));

                        fileData.append("\r\n");
                        fout.write(fileData.toString().getBytes());
                        fout.flush();
                        transbundCtr++;
                    }
                }
            }
            if (fout != null) {
	            formatTransBundleTrail(String.valueOf(transbundCtr), fout, env);
	            fout.close();
            }
        }
        catch (FileNotFoundException fnfExcpn) {
            BranchPowerTransBundleRefreshFatomContext.Status = Boolean.FALSE;

            // setF_OUT_Batch_Status(new Boolean (false));
           // throw new BankFusionException(127, new Object[] { fnfExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (IOException ioExcpn) {
            BranchPowerTransBundleRefreshFatomContext.Status = Boolean.FALSE;

            // setF_OUT_Batch_Status(new Boolean (false));
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (NumberFormatException nfExcpn) {
            BranchPowerTransBundleRefreshFatomContext.Status = Boolean.FALSE;

            // setF_OUT_Batch_Status(new Boolean (false));
           // throw new BankFusionException(127, new Object[] { nfExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { nfExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (Exception Excpn) {

            BranchPowerTransBundleRefreshFatomContext.Status = Boolean.FALSE;
           // throw new BankFusionException(127, new Object[] { Excpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
            // ssetF_OUT_Batch_Status(new Boolean (false));
        }
    }

    /**
     * formats and writes the Transaction Bundle file header to mcfbtrans.dat
     * 
     * @param fout
     */
    private void formatTransBundleHeader(FileOutputStream fout, BankFusionEnvironment env) throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-REC-TYPE")).intValue(), "01", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-FILE-ACTION")).intValue(), "A", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-FILE-ID")).intValue(), "BT", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-FILLER")).intValue(), "", 'A'));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
        }
        catch (IOException ioExcpn) {
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
    }

    /**
     * formats and writes the Transaction Bundle file trailer to mcfbtrans.dat
     * 
     * @param transCtr
     * @param fout
     */
    private void formatTransBundleTrail(String transCtr, FileOutputStream fout, BankFusionEnvironment env)
            throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-TRL-REC-TYPE")).intValue(), "99", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-TRL-REC-COUNT")).intValue(), transCtr, 'N'));
        fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-TRL-FILLER")).intValue(), "", 'A'));
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
           // throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
        	EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
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
