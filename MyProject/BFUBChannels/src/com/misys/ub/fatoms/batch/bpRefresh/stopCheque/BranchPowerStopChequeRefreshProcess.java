/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerStopChequeRefreshProcess.java,v.1.1.2.1,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerStopChequeRefreshProcess.java,v $
 * Revision 1.1.2.2  2008/08/22 00:26:20  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.stopCheque;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.fatoms.batch.bpRefresh.BPRefreshConstants;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.BatchProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOStoppedChq;
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

public class BranchPowerStopChequeRefreshProcess extends AbstractBatchProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final transient Log logger = LogFactory.getLog(BranchPowerStopChequeRefreshProcess.class.getName());

    private static final String BranchPowerWhereClause = " WHERE " + IBOBranch.BMBRANCH + " BETWEEN ? AND ? ";

    private static Map currencyMap = new HashMap();

    // Initialize variables to be read from properties file
    public String fromBranch = "";
    public String toBranch = "";
    public String extractPath = "";
    public String custCategory = "";
    public String branchCategory = "";
    public String StopChequeRefFlag = "";
    private String hdrActionFlag = CommonConstants.EMPTY_STRING;
    String strHdrDt = "";
    ArrayList branchRange;
    private BankFusionEnvironment env;
    // private AbstractFatomContext context=null;

    private AbstractProcessAccumulator accumulator;

    /*
     * static Properties fileBPRefreshProp; static Properties fileRefreshProp;
     */

    final String queryStoppedCheques = "SELECT" + " (T2." + IBOStoppedChq.STOPDATE + ") AS " + IBOStoppedChq.STOPDATE + " ,(T2."
            + IBOStoppedChq.STOPREASON + ") AS " + IBOStoppedChq.STOPREASON + " ,(T2." + IBOStoppedChq.ACCOUNTID + ") AS "
            + IBOStoppedChq.ACCOUNTID + " ,(T2." + IBOStoppedChq.TOSTOPCHQREF + ") AS " + IBOStoppedChq.TOSTOPCHQREF + " ,(T2."
            + IBOStoppedChq.AMOUNT + ") AS " + IBOStoppedChq.AMOUNT + " ,(T2." + IBOStoppedChq.STOPPEDSTATUS + ") AS "
            + IBOStoppedChq.STOPPEDSTATUS + " ,(T2." + IBOStoppedChq.FROMSTOPCHQREF + ") AS " + IBOStoppedChq.FROMSTOPCHQREF
            + " ,(T2." + IBOStoppedChq.ISOCURRENCYCODE + ") AS " + IBOStoppedChq.ISOCURRENCYCODE + " FROM " + IBOAccount.BONAME
            + " T1, " + IBOStoppedChq.BONAME + " T2" + " WHERE " + "T1." + IBOAccount.ACCOUNTID + " = T2."
            + IBOStoppedChq.ACCOUNTID;

    TreeMap accHash = new TreeMap();
    TreeMap limitHash = new TreeMap();

    StringBuffer fileData = new StringBuffer();
    String MSG1 = CommonConstants.EMPTY_STRING;;
    Boolean Status;

    private static Map<String, String> bpRefreshPropertiesMap = new HashMap<String, String>();

    private static Map<String, String> refreshPropertiesMap = new HashMap<String, String>();

    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    /**
     * Method to change amount from Bankmaster to regular format formats and populates the string
     * buffer with the formatted amount takes bdAmt,fldName as input
     * 
     * @param bdAmt
     * @param fldName
     * @param fileData
     */
    private static void setAmount(BigDecimal bdAmt, String fldName, StringBuffer fileData, int scale) {

        bdAmt = bdAmt.setScale(scale);
        String Amount = bdAmt.unscaledValue().abs().toString();

        if (bdAmt.signum() == -1) {
            fileData.append(setField(new Integer(getRereshProperty(fldName)).intValue() - 1, Amount.substring(0,
                    Amount.length() - 1), 'N'));
            char str = Amount.charAt(Amount.length() - 1);
            fileData.append(getRereshProperty(String.valueOf(str)));
        }
        else {
            fileData.append(setField(new Integer(getRereshProperty(fldName)).intValue(), Amount, 'N'));
        }
    }

    /**
     * @param environment
     *            Used to get a handle on the BankFusion environment
     * @param context
     *            A set of data passed to the PreProcess, Process and PostProcess classes
     * @param priority
     *            Thread priority
     */
    public BranchPowerStopChequeRefreshProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
        super(environment, context, priority);
        this.context = context;
        env = environment;
        Object[] additionalParameters = context.getAdditionalProcessParams();
        bpRefreshPropertiesMap = (HashMap<String, String>) additionalParameters[2];
        currencyMap = (HashMap<String, Integer>) additionalParameters[4];

        // refreshPropertiesMap = (HashMap<String, String>) additionalParameters[4];
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
        accumulator = new BranchPowerStopChequeRefreshAccumulator(accumulatorArgs);
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

        // fileBPRefreshProp = (Properties) additionalParameters[0];
        if (bpRefreshPropertiesMap == null || bpRefreshPropertiesMap.size() == 0) {
            // throw new BankFusionException(127, new Object[] { "Error Reading Properties File" },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { "Error Reading Properties File" }, new HashMap(), env);
        }
        if ((getBPRereshProperty(BPRefreshConstants.FROM_BRANCH).equalsIgnoreCase(""))
                || (getBPRereshProperty(BPRefreshConstants.TO_BRANCH).equalsIgnoreCase(""))
                || (getBPRereshProperty("EXTRACTPATH").equalsIgnoreCase(""))
                || (getBPRereshProperty("STOP-CHEQUE-REFRESH").equalsIgnoreCase("")))

        {
            // throw new BankFusionException(127, new Object[] { "Invalid Parameters passed" },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { "Invalid Parameters passed" }, new HashMap(), env);
        }
        else {
            fromBranch = getBPRereshProperty(BPRefreshConstants.FROM_BRANCH);
            toBranch = getBPRereshProperty(BPRefreshConstants.TO_BRANCH);
        }
        toBranch = getBPRereshProperty(BPRefreshConstants.TO_BRANCH);
        branchRange = new ArrayList();
        branchRange.add(0, fromBranch);
        branchRange.add(1, toBranch);
        extractPath = getBPRereshProperty("EXTRACTPATH");
        StopChequeRefFlag = getBPRereshProperty("STOP-CHEQUE-REFRESH");
        Date hdrDate = null; // date format = 2006-04-09
        try {
            hdrDate = new SimpleDateFormat("yyyy-MM-dd").parse(SystemInformationManager.getInstance().getBFBusinessDate()
                    .toString());
        }
        catch (ParseException pExcpn) {
            // throw new BankFusionException(127, new Object[] { pExcpn.getLocalizedMessage() },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { pExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        strHdrDt = new SimpleDateFormat("yyyyMMdd").format(hdrDate);

        Iterator Branch = null;

        pagingData.setCurrentPageNumber(pageToProcess);

        {
            try {

                ArrayList list = new ArrayList();
                list.add(fromBranch);
                list.add(toBranch);
                List branchList = factory.findByQuery(IBOBranch.BONAME, BranchPowerWhereClause, list, pagingData, false);
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
                Object[] additionalParameters = context.getAdditionalProcessParams();
                IBOBranch branch = (IBOBranch) Branch.next();
                branchCode = branch.getBoID();
                BankFusionThreadLocal.setCurrentRecordID(branchCode);
                if ((branchCode != null)) {
                    refreshPropertiesMap = (HashMap<String, String>) additionalParameters[3];

                    if (StopChequeRefFlag.equals("1")) {
                        refreshStoppedCheques(env);

                    }

                }
                else {
                    logger
                            .debug("BranchPowerStopChequeRefresh cannot be processed as branch does not belong to specified branchRange");
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
     * Performs the main stopped cheques file refresh
     * 
     * @param env
     * @throws BankFusionException
     */
    private void refreshStoppedCheques(BankFusionEnvironment env) throws BankFusionException {

        List StoppedChequesDetails = null;
        StoppedChequesDetails = factory.executeGenericQuery(queryStoppedCheques, null, null, true);



        

        
        if (StoppedChequesDetails.size() == 0)
            return;
        try {
            FileOutputStream fout = null;
            fout = new FileOutputStream(extractPath + "mcfstpch.dat");
            int stpchqCtr = 0;
            int scale = 0;
            SimplePersistentObject stpChqPO = null;
            Integer scale1 = null;
            String StopStatus;
            for (int i = 0; i < StoppedChequesDetails.size(); i++) {
                stpChqPO = (SimplePersistentObject) StoppedChequesDetails.get(i);
                if (i == 0) {
                    formatStoppedChequesHeader(fout, env);
                }
                if (stpChqPO.getDataMap().get(IBOStoppedChq.ACCOUNTID) != null) {
                    
                    scale1 = (Integer) (currencyMap.get(stpChqPO.getDataMap().get(IBOStoppedChq.ISOCURRENCYCODE)));
                    scale = scale1.intValue();

                    fileData = new StringBuffer();
                    fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-REC-TYPE")).intValue(), "02", 'N'));
                    fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-ACCOUNT-NUM")).intValue(),
                            (String) stpChqPO.getDataMap().get(IBOStoppedChq.ACCOUNTID), 'A'));
                    if (stpChqPO.getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString().length() > 12)
                        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-CHEQUE-NUM")).intValue(), stpChqPO
                                .getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString().substring(0, 11), 'N'));
                    else fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-CHEQUE-NUM")).intValue(), stpChqPO
                            .getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString(), 'N'));

                    if (stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString().length() > 17)
                        setAmount(new BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString().substring(0, 17)),
                                "FS-STP-CHQ-DTL-AMOUNT", fileData, scale);
                    else setAmount(new BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString()),
                            "FS-STP-CHQ-DTL-AMOUNT", fileData, scale);

                    Date dtTmp = null; // date format = 2006-04-09 20:17:55449
                    String dtStr = stpChqPO.getDataMap().get(IBOStoppedChq.STOPDATE).toString();
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                    dtTmp = sf.parse(dtStr);
                    
                    String strDtDrawn = new SimpleDateFormat("yyyyMMdd").format(dtTmp);
                    fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-DATE-DRAWN")).intValue(), strDtDrawn,
                            'N'));
                    if (stpChqPO.getDataMap().get(IBOStoppedChq.STOPREASON).toString().length() > 25)
                        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-PAYEE-NARR")).intValue(), stpChqPO
                                .getDataMap().get(IBOStoppedChq.STOPREASON).toString().substring(0, 24), 'A'));
                    else fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-PAYEE-NARR")).intValue(),
                            (String) stpChqPO.getDataMap().get(IBOStoppedChq.STOPREASON), 'A'));

                    if (stpChqPO.getDataMap().get(IBOStoppedChq.STOPPEDSTATUS).toString() == "true")
                        StopStatus = "1";
                    else StopStatus = "0";
                    fileData
                            .append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-STOP-FLAG")).intValue(), StopStatus, 'N'));
                    if (stpChqPO.getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString().length() > 12)
                        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-FROM-CHQ-NO")).intValue(), stpChqPO
                                .getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString().substring(0, 11), 'N'));
                    else fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-FROM-CHQ-NO")).intValue(), stpChqPO
                            .getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString(), 'N'));

                    fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-FILLER")).intValue(), "", 'A'));
                    fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-ACTION")).intValue(), "A", 'A'));
                    // BigDecimal checksum = new
                    // BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.STOPCHEQUEREF).toString()).add(new
                    // BigDecimal(stpChqPO.getDataMap().get(IBOTransaction.AMOUNT).toString()).abs())
                    BigDecimal checksum = new BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString()).add(
                            (new BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString()).abs())
                                    .movePointRight(scale)).add(new BigDecimal(strDtDrawn)).add(
                            new BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString()));
                    checksum = checksum.setScale(0, BigDecimal.ROUND_CEILING);
                    
                    fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-CHECKSUM")).intValue(), checksum.abs()
                            .toString(), 'N'));
                    fileData.append("\r\n");
                    fout.write(fileData.toString().getBytes());
                    fout.flush();
                    stpchqCtr++;
                }
            }
            formatStoppedChequesTrail(String.valueOf(stpchqCtr), fout, env);
            fout.close();
        }
        catch (FileNotFoundException fnfExcpn) {
            BranchPowerStopChequeRefreshFatomContext.Status = Boolean.FALSE;

            // setF_OUT_Batch_Status(new Boolean (false));
            // throw new BankFusionException(127, new Object[] { fnfExcpn.getLocalizedMessage() },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { fnfExcpn
                    .getLocalizedMessage() }, new HashMap(), env);
        }
        catch (IOException ioExcpn) {
            BranchPowerStopChequeRefreshFatomContext.Status = Boolean.FALSE;
            // setF_OUT_Batch_Status(new Boolean (false));
            // throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (NumberFormatException nfExcpn) {
            BranchPowerStopChequeRefreshFatomContext.Status = Boolean.FALSE;
            // setF_OUT_Batch_Status(new Boolean (false));
            // throw new BankFusionException(127, new Object[] { nfExcpn.getLocalizedMessage() },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { nfExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (ParseException pExcpn) {
            BranchPowerStopChequeRefreshFatomContext.Status = Boolean.FALSE;
            // setF_OUT_Batch_Status(new Boolean (false));
            // throw new BankFusionException(127, new Object[] { pExcpn.getLocalizedMessage() },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { pExcpn.getLocalizedMessage() }, new HashMap(), env);
        }
        catch (Exception Excpn) {

            BranchPowerStopChequeRefreshFatomContext.Status = Boolean.FALSE;
            // setF_OUT_Batch_Status(new Boolean (false));
            // throw new BankFusionException(127, new Object[] { Excpn.getLocalizedMessage() },
            // logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
        }
    }

    /**
     * formats and writes the stopped cheque file header to mcfstpch.dat
     * 
     * @param fout
     */
    private void formatStoppedChequesHeader(FileOutputStream fout, BankFusionEnvironment env) throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        hdrActionFlag = getBPRereshProperty("HDRACTIONFLAG");
        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-REC-TYPE")).intValue(), "01", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-FILE-ACTION")).intValue(), hdrActionFlag, 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-FILE-ID")).intValue(), "SC", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-FILLER")).intValue(), "", 'A'));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
        }
        catch (IOException ioExcpn) {
            // new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger,
            // env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { "Error Reading Properties File" }, new HashMap(), env);
        }
    }

    /**
     * formats and writes the stopped cheque file trailer to mcfstpch.dat
     * 
     * @param chqCtr
     * @param fout
     */
    private void formatStoppedChequesTrail(String chqCtr, FileOutputStream fout, BankFusionEnvironment env)
            throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-TRL-REC-TYPE")).intValue(), "99", 'A'));
        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-TRL-REC-COUNT")).intValue(), chqCtr, 'N'));
        fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-TRL-FILLER")).intValue(), "", 'A'));
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
            // throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() },
            // logger, env);
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
    private static String getBPRereshProperty(String sKey) {
        return bpRefreshPropertiesMap.get(sKey).toString();
    }

    /**
     * reads the property for the key passed
     * 
     * @param sKey
     * @return
     */
    private static String getRereshProperty(String sKey) {
        return refreshPropertiesMap.get(sKey).toString();
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
     * This method will be called by the Batch Framework when there is an exception caught and the
     * process page is marked as failed. The Batch Framework creates a private Session and the
     * factory is passed to this method so that the values get committed.
     * 
     * @param unprocessedIds
     *            - List of Unprocessed IDs
     * @param failedId
     *            - Failed Account Number
     * @param exception
     *            - Exception Object
     * @param factory
     *            - Instance of persistenceObjectFactory created as a private Session.
     * 
     */
    public void logException(List<String> unprocessedIds, String failedId, Exception exception, IPersistenceObjectsFactory factory) {

        Iterator unprocessedIter = unprocessedIds.iterator();
        String status = CommonConstants.EMPTY_STRING;
        String message = CommonConstants.EMPTY_STRING;
        while (unprocessedIter.hasNext()) {
            IBOBranch branchItem = (IBOBranch) unprocessedIter.next();

            String key = branchItem.getBoID();
            if (key.equalsIgnoreCase(failedId)) {
                status = "E";
                message = exception.getMessage();
            }
            else {
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
        IBOUB_CMN_BatchProcessLog batchException = (IBOUB_CMN_BatchProcessLog) factory
                .getStatelessNewInstance(IBOUB_CMN_BatchProcessLog.BONAME);
        batchException.setBoID(GUIDGen.getNewGUID());
        batchException.setF_PROCESSNAME(this.context.getBatchProcessName());

        batchException.setF_RUNDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime(
                environment.getRuntimeMicroflowID()));
        batchException.setF_RECORDID(key);

        if (status.equalsIgnoreCase("E") || status.equalsIgnoreCase("W")) {
            if (logger.isErrorEnabled()) {
                logger.error("Error processing for Account [ " + key + " ] Reason :- " + message);
            }
            if (null == message) {
                message = CommonConstants.EMPTY_STRING;
            }
            message = message.replaceAll(",", "");
            message = message.replaceAll(":", "");
            message = message.replaceAll("':", "");

            batchException.setF_ERRORMESSAGE(message);
            batchException.setF_STATUS(status);
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info("Unprocessed Account [ " + key + " ] ");
            }
            batchException.setF_STATUS(status);
        }
        factory.create(IBOUB_CMN_BatchProcessLog.BONAME, batchException);
    }

}
