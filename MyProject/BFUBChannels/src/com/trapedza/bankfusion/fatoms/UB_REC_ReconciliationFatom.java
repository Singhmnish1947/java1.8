/**
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.toolkit.expression.function.ConvertToTimestamp;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.cbs.common.util.log.CBSLogger;
import com.misys.ub.common.GetUBConfigLocation;
import com.sun.jmx.snmp.Timestamp;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_REC_RECONTRANDETAILS;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_REC_Reconciliation;

import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.ModuleKeyRq;

/**
 * @author Shreyas.MR
 *
 */
public class UB_REC_ReconciliationFatom extends AbstractUB_REC_Reconciliation {

    /**
    																																		 *
    																																		 */
    private static final long serialVersionUID = 1L;

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    private transient final static Log LOGGER = LogFactory.getLog(UB_REC_ReconciliationFatom.class.getName());
    private static String getTransactionDetailForRecon = " SELECT   T1." + IBOTransaction.TRANSACTIONSRID + " AS "
            + CommonConstants.getTagName(IBOTransaction.TRANSACTIONSRID) + ",T1." + IBOTransaction.TRANSACTIONID + " AS "
            + CommonConstants.getTagName(IBOTransaction.TRANSACTIONID) + ",T1." + IBOTransaction.NARRATION + " AS "
            + CommonConstants.getTagName(IBOTransaction.NARRATION) + ",T1." + IBOTransaction.COINSAMOUNT + " AS "
            + CommonConstants.getTagName(IBOTransaction.COINSAMOUNT) + ",T1." + IBOTransaction.NOTESAMOUNT + " AS "
            + CommonConstants.getTagName(IBOTransaction.NOTESAMOUNT) + ",T1." + IBOTransaction.DEBITCREDITFLAG + " AS "
            + CommonConstants.getTagName(IBOTransaction.DEBITCREDITFLAG) + ",T1." + IBOTransaction.USERID + " AS "
            + CommonConstants.getTagName(IBOTransaction.USERID) + ",T1." + IBOTransaction.SRNO + " AS "
            + CommonConstants.getTagName(IBOTransaction.SRNO) + ",T1." + IBOTransaction.REFERENCE + " AS "
            + CommonConstants.getTagName(IBOTransaction.REFERENCE) + ",T1." + IBOTransaction.CODE + " AS "
            + CommonConstants.getTagName(IBOTransaction.CODE) + ",T1." + IBOTransaction.PASSBOOKFLAG + " AS "
            + CommonConstants.getTagName(IBOTransaction.PASSBOOKFLAG) + ",T1." + IBOTransaction.AUTHORISEDUSERID + " AS "
            + CommonConstants.getTagName(IBOTransaction.AUTHORISEDUSERID) + ",T1." + IBOTransaction.CHEQUESCOUNT + " AS "
            + CommonConstants.getTagName(IBOTransaction.CHEQUESCOUNT) + ",T1." + IBOTransaction.REVERSALINDICATOR + " AS "
            + CommonConstants.getTagName(IBOTransaction.REVERSALINDICATOR) + ",T1." + IBOTransaction.NEWMARGINE + " AS "
            + CommonConstants.getTagName(IBOTransaction.NEWMARGINE) + ",T1." + IBOTransaction.POSTINGDATE + " AS "
            + CommonConstants.getTagName(IBOTransaction.POSTINGDATE) + ",T1." + IBOTransaction.AMOUNT + " AS "
            + CommonConstants.getTagName(IBOTransaction.AMOUNT) + ",T1." + IBOTransaction.TRANSACTIONDATE + " AS "
            + CommonConstants.getTagName(IBOTransaction.TRANSACTIONDATE) + ",T1." + IBOTransaction.NEWBASECODE + " AS "
            + CommonConstants.getTagName(IBOTransaction.NEWBASECODE) + ",T1." + IBOTransaction.STATEMENTFLAG + " AS "
            + CommonConstants.getTagName(IBOTransaction.STATEMENTFLAG) + ",T1." + IBOTransaction.NEWINTRATE + " AS "
            + CommonConstants.getTagName(IBOTransaction.NEWINTRATE) + ",T1." + IBOTransaction.SOURCEBRANCH + " AS "
            + CommonConstants.getTagName(IBOTransaction.SOURCEBRANCH) + ",T1." + IBOTransaction.TYPE + " AS "
            + CommonConstants.getTagName(IBOTransaction.TYPE) + ",T1." + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " AS "
            + CommonConstants.getTagName(IBOTransaction.ACCOUNTPRODUCT_ACCPRODID) + ",T1." + IBOTransaction.ITEMID + " AS "
            + CommonConstants.getTagName(IBOTransaction.ITEMID) + ",T1." + IBOTransaction.ISOCURRENCYCODE + " AS "
            + CommonConstants.getTagName(IBOTransaction.ISOCURRENCYCODE) + ",T1." + IBOTransaction.EXCHANGERATE + " AS "
            + CommonConstants.getTagName(IBOTransaction.EXCHANGERATE) + ",T1." + IBOTransaction.OPPOSITECURRENCYCODE + " AS "
            + CommonConstants.getTagName(IBOTransaction.OPPOSITECURRENCYCODE) + ",T1." + IBOTransaction.EXCHANGERATETYPE + " AS "
            + CommonConstants.getTagName(IBOTransaction.EXCHANGERATETYPE) + ",T1." + IBOTransaction.VALUEDATE + " AS "
            + CommonConstants.getTagName(IBOTransaction.VALUEDATE) + ",T1." + IBOTransaction.SHORTNAME + " AS "
            + CommonConstants.getTagName(IBOTransaction.SHORTNAME) + ",T1." + IBOTransaction.INTACCDTODATECR + " AS "
            + CommonConstants.getTagName(IBOTransaction.INTACCDTODATECR) + ",T1." + IBOTransaction.INTACCDTODATEDR + " AS "
            + CommonConstants.getTagName(IBOTransaction.INTACCDTODATEDR) + ",T1." + IBOTransaction.INTADJAMOUNTCR + " AS "
            + CommonConstants.getTagName(IBOTransaction.INTADJAMOUNTCR) + ",T1." + IBOTransaction.INTADJAMOUNTDR + " AS "
            + CommonConstants.getTagName(IBOTransaction.INTADJAMOUNTDR) + ",T1." + IBOTransaction.CLEAREDRUNNINGBALANCE + " AS "
            + CommonConstants.getTagName(IBOTransaction.CLEAREDRUNNINGBALANCE) + ",T1." + IBOTransaction.PAGENUMBER + " AS "
            + CommonConstants.getTagName(IBOTransaction.PAGENUMBER) + ",T1." + IBOTransaction.PAGESRNUMBER + " AS "
            + CommonConstants.getTagName(IBOTransaction.PAGESRNUMBER) + ",T1." + IBOTransaction.CHEQUEDRAFTNUMBER + " AS "
            + CommonConstants.getTagName(IBOTransaction.CHEQUEDRAFTNUMBER) + ",T1." + IBOTransaction.BOOKBALANCE + " AS "
            + CommonConstants.getTagName(IBOTransaction.BOOKBALANCE) + ",T1." + IBOTransaction.AMOUNTCREDIT + " AS "
            + CommonConstants.getTagName(IBOTransaction.AMOUNTCREDIT) + ",T1." + IBOTransaction.AMOUNTDEBIT + " AS "
            + CommonConstants.getTagName(IBOTransaction.AMOUNTDEBIT) + ",T1." + IBOTransaction.ORIGINALAMOUNT + " AS "
            + CommonConstants.getTagName(IBOTransaction.ORIGINALAMOUNT) + ",T1." + IBOTransaction.BASEEQUIVALENT + " AS "
            + CommonConstants.getTagName(IBOTransaction.BASEEQUIVALENT) + ",T1." + IBOTransaction.VERSIONNUM + " AS "
            + CommonConstants.getTagName(IBOTransaction.VERSIONNUM) + ",T1." + IBOTransaction.SYSTEMTRANS + " AS "
            + CommonConstants.getTagName(IBOTransaction.SYSTEMTRANS) + ",T1." + IBOTransaction.INCLUDEFORSTATISTICS + " AS "
            + CommonConstants.getTagName(IBOTransaction.INCLUDEFORSTATISTICS) + ",T1." + IBOTransaction.TRANSACTIONCROSSREFID
            + " AS " + CommonConstants.getTagName(IBOTransaction.TRANSACTIONCROSSREFID) + ",T1." + IBOTransaction.UBCHANNELID
            + " AS " + CommonConstants.getTagName(IBOTransaction.UBCHANNELID) + ",T1." + IBOTransaction.TRANSACTIONCOUNTER + " AS "
            + CommonConstants.getTagName(IBOTransaction.TRANSACTIONCOUNTER) + ",T2." + IBOUB_REC_RECONTRANDETAILS.ISARCHIVE + " AS "
            + CommonConstants.getTagName(IBOUB_REC_RECONTRANDETAILS.ISARCHIVE) + ",T1." + IBOTransaction.PIEVENTTYPE + " AS "
            + CommonConstants.getTagName(IBOTransaction.PIEVENTTYPE) + ",T2." + IBOUB_REC_RECONTRANDETAILS.ISDELETE + " AS "
            + CommonConstants.getTagName(IBOUB_REC_RECONTRANDETAILS.ISDELETE) + ",T1." + IBOTransaction.LIMITAMT + " AS "
            + CommonConstants.getTagName(IBOTransaction.LIMITAMT) + ",T1." + IBOTransaction.TEMPLIMITAMT + " AS "
            + CommonConstants.getTagName(IBOTransaction.TEMPLIMITAMT) + ",T1." + IBOTransaction.UBORIGINATIONDATETIME + " AS "
            + CommonConstants.getTagName(IBOTransaction.UBORIGINATIONDATETIME) + ",T1." + IBOTransaction.UBORIGINATIONTIMEZONE
            + " AS " + CommonConstants.getTagName(IBOTransaction.UBORIGINATIONTIMEZONE) + ",T1." + IBOTransaction.FORCEPOSTFLAG
            + " AS " + CommonConstants.getTagName(IBOTransaction.FORCEPOSTFLAG) + ",T2."
            + IBOUB_REC_RECONTRANDETAILS.TRANSACTIONSRID + " AS "
            + CommonConstants.getTagName(IBOUB_REC_RECONTRANDETAILS.TRANSACTIONSRID) + " " + CommonConstants.FROM + " "
            + IBOTransaction.BONAME + " T1," + IBOUB_REC_RECONTRANDETAILS.BONAME + " T2 " + "WHERE T1."
            + IBOTransaction.TRANSACTIONSRID + "= T2." + IBOUB_REC_RECONTRANDETAILS.TRANSACTIONSRID + " AND T2."
            + IBOUB_REC_RECONTRANDETAILS.RECONSTATUS + "='Not Processed'";

    private IPersistenceObjectsFactory factory;

    private static final String CLASS_NAME = UB_REC_ReconciliationFatom.class.getName();
    private static final transient CBSLogger CBS_LOGGER = new CBSLogger(CLASS_NAME);

    private static final String RECON_MODULE_NAME = "REC";
    private static final String RECON_EXTRACT_PARAM_NAME = "SmartStreamExtractLoc";
    private static final String READ_MODULE_CONFIGURATION_SERVICE = "CB_CMN_ReadModuleConfiguration_SRV";

    private static final String RECONSTATUS_NOTPROCESSED = "Not Processed";

    private static final String RECONSTATUS_RECONPENDING = "Recon Pending";
    boolean debugEnabled = CBS_LOGGER.isDebugEnabled();
    boolean infoEnabled = CBS_LOGGER.isInfoEnabled();

    @SuppressWarnings("unused")
    private List<SimplePersistentObject> reconTranDetails = null;

    // private IBOUB_REC_RECONTRANDETAILS reconTranDetails = null;

    /**
     * The constructor that indicates we're in a runtime environment and we should initialise the
     * Fatom with only those attributes necessary.
     *
     * @param env
     *            The BankFusion Environment
     */
    @SuppressWarnings("deprecation")
    public UB_REC_ReconciliationFatom(BankFusionEnvironment env) {
        super(env);
    }

    @SuppressWarnings("unchecked")
    private List<SimplePersistentObject> fetchAllReconTransactions() {
        ArrayList params = new ArrayList();
        return reconTranDetails = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(getTransactionDetailForRecon,
                params, null, false);
    }

    @SuppressWarnings("unchecked")
    public void process(BankFusionEnvironment env) {

        List<SimplePersistentObject> reconTranDetails = fetchAllReconTransactions();

        if (reconTranDetails.size() != 0) {

            try {
                // Get & Location
                ReadModuleConfigurationRq readModuleConfRq = new ReadModuleConfigurationRq();
                ModuleKeyRq moduleKeyRq = new ModuleKeyRq();
                moduleKeyRq.setModuleId(RECON_MODULE_NAME);
                moduleKeyRq.setKey(RECON_EXTRACT_PARAM_NAME);
                readModuleConfRq.setModuleKeyRq(moduleKeyRq);
                HashMap inputParams = new HashMap();
                inputParams.put("ReadModuleConfigurationRq", readModuleConfRq);
                HashMap outputParams = MFExecuter.executeMF(READ_MODULE_CONFIGURATION_SERVICE, env, inputParams);
                ReadModuleConfigurationRs readModuleConfRs = (ReadModuleConfigurationRs) (outputParams
                        .get("ReadModuleConfigurationRs"));
                String smartStreamStaticExtractLocation = readModuleConfRs.getModuleConfigDetails().getValue();
                this.writeRaw(reconTranDetails, smartStreamStaticExtractLocation);
                // this.writeBuffered(reconTranDetails, 8192);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                LOGGER.error(ExceptionUtil.getExceptionAsString(e));
            }

        }

        setF_OUT_Status("success");
    }

    @SuppressWarnings("unchecked")
    private void writeRaw(List<SimplePersistentObject> reconTranDetails, String fileLocation) throws IOException {
        List<String> records = new ArrayList<String>();
        // String configLocation = System.getProperty("BFconfigLocation",
        // CommonConstants.EMPTY_STRING);
        String configLocation = GetUBConfigLocation.getUBConfigLocation();
        Timestamp t = new Timestamp(SystemInformationManager.getInstance().getBFBusinessDate().getTime());
        String fileName = "TransactionDump_" + t.getDateTime() + ".csv";

        File file = null;

        try {
            file = new File(fileLocation, fileName);
        }
        catch (Exception e) {
            CBS_LOGGER.info("processwriteRaw",
                    "Module Configuration Location for SmartStreamExtracts doesn't exist" + "Creating extract in BFConfigLocation");
            file = new File(configLocation, fileName);
            LOGGER.error(ExceptionUtil.getExceptionAsString(e));
        }

        try {
            try {
                file.createNewFile();
            }
            catch (Exception e) {
                // String path = System.getProperty("BFconfigLocation");
                String path = GetUBConfigLocation.getUBConfigLocation();
                file = new File(path, fileName);
                file.createNewFile();
                LOGGER.error(ExceptionUtil.getExceptionAsString(e));
            }
            FileWriter writer = new FileWriter(file);
            Iterator<SimplePersistentObject> i = reconTranDetails.iterator();
            String HeaderRecord = (CommonConstants.getTagName(IBOTransaction.TRANSACTIONSRID)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.TRANSACTIONID)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.NARRATION)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.COINSAMOUNT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.NOTESAMOUNT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.DEBITCREDITFLAG)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.USERID)) + "," + (CommonConstants.getTagName(IBOTransaction.SRNO))
                    + "," + (CommonConstants.getTagName(IBOTransaction.REFERENCE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.CODE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.PASSBOOKFLAG)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.AUTHORISEDUSERID)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.CHEQUESCOUNT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.REVERSALINDICATOR)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.NEWMARGINE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.POSTINGDATE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.AMOUNT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.TRANSACTIONDATE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.NEWBASECODE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.STATEMENTFLAG)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.NEWINTRATE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.SOURCEBRANCH)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.TYPE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.ACCOUNTPRODUCT_ACCPRODID)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.ITEMID)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.ISOCURRENCYCODE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.EXCHANGERATE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.OPPOSITECURRENCYCODE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.EXCHANGERATETYPE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.VALUEDATE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.SHORTNAME)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.INTACCDTODATECR)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.INTACCDTODATEDR)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.INTADJAMOUNTCR)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.INTADJAMOUNTDR)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.CLEAREDRUNNINGBALANCE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.PAGENUMBER)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.PAGESRNUMBER)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.CHEQUEDRAFTNUMBER)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.BOOKBALANCE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.AMOUNTCREDIT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.AMOUNTDEBIT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.ORIGINALAMOUNT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.BASEEQUIVALENT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.VERSIONNUM)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.SYSTEMTRANS)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.INCLUDEFORSTATISTICS)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.TRANSACTIONCROSSREFID)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.UBCHANNELID)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.TRANSACTIONCOUNTER)) + ","
                    + (CommonConstants.getTagName(IBOUB_REC_RECONTRANDETAILS.ISARCHIVE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.PIEVENTTYPE)) + ","
                    + (CommonConstants.getTagName(IBOUB_REC_RECONTRANDETAILS.ISDELETE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.LIMITAMT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.TEMPLIMITAMT)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.UBORIGINATIONDATETIME)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.UBORIGINATIONTIMEZONE)) + ","
                    + (CommonConstants.getTagName(IBOTransaction.FORCEPOSTFLAG));
            records.add(HeaderRecord);
            while (i.hasNext()) {

                SimplePersistentObject simplePersistentObject = (SimplePersistentObject) i.next();
                Map data = simplePersistentObject.getDataMap();

                // get the list of attributes from the map that need to be part
                // of the csv being created

                String record = data.get(CommonConstants.getTagName(IBOTransaction.TRANSACTIONSRID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.TRANSACTIONID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.NARRATION)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.COINSAMOUNT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.NOTESAMOUNT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.DEBITCREDITFLAG)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.USERID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.SRNO)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.REFERENCE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.CODE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.PASSBOOKFLAG)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.AUTHORISEDUSERID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.CHEQUESCOUNT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.REVERSALINDICATOR)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.NEWMARGINE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.POSTINGDATE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.AMOUNT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.TRANSACTIONDATE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.NEWBASECODE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.STATEMENTFLAG)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.NEWINTRATE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.SOURCEBRANCH)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.TYPE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.ACCOUNTPRODUCT_ACCPRODID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.ITEMID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.ISOCURRENCYCODE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.EXCHANGERATE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.OPPOSITECURRENCYCODE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.EXCHANGERATETYPE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.VALUEDATE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.SHORTNAME)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.INTACCDTODATECR)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.INTACCDTODATEDR)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.INTADJAMOUNTCR)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.INTADJAMOUNTDR)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.CLEAREDRUNNINGBALANCE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.PAGENUMBER)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.PAGESRNUMBER)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.CHEQUEDRAFTNUMBER)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.BOOKBALANCE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.AMOUNTCREDIT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.AMOUNTDEBIT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.ORIGINALAMOUNT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.BASEEQUIVALENT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.VERSIONNUM)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.SYSTEMTRANS)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.INCLUDEFORSTATISTICS)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.TRANSACTIONCROSSREFID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.UBCHANNELID)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.TRANSACTIONCOUNTER)) + ","
                        + data.get(CommonConstants.getTagName(IBOUB_REC_RECONTRANDETAILS.ISARCHIVE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.PIEVENTTYPE)) + ","
                        + data.get(CommonConstants.getTagName(IBOUB_REC_RECONTRANDETAILS.ISDELETE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.LIMITAMT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.TEMPLIMITAMT)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.UBORIGINATIONDATETIME)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.UBORIGINATIONTIMEZONE)) + ","
                        + data.get(CommonConstants.getTagName(IBOTransaction.FORCEPOSTFLAG));
                records.add(record);

            }
            this.write(records, writer);
            this.update();

        }
        catch (IOException e) {
            // comment this out if you want to inspect the files afterward
            LOGGER.error(ExceptionUtil.getExceptionAsString(e));

        }
    }

    private void write(List<String> records, Writer writer) throws IOException {

        long start = SystemInformationManager.getInstance().getBFSystemDateTime().getTime();
        for (String record : records) {
            writer.write(record);
            writer.append('\n');
        }
        writer.flush();
        writer.close();
        long end = SystemInformationManager.getInstance().getBFSystemDateTime().getTime();
        CBS_LOGGER.debug((end - start) / 1000f + " seconds");
    }

    @SuppressWarnings("unchecked")
    private void update() {

        java.sql.Timestamp t1 = ConvertToTimestamp.run(SystemInformationManager.getInstance().getBFBusinessDate());
        String selectClause = "WHERE " + IBOUB_REC_RECONTRANDETAILS.RECONSTATUS + " = ?";

        if (CBS_LOGGER.isDebugEnabled()) {
            CBS_LOGGER.debug("processUpdate()", "SQL Querry:" + selectClause);
        }

        ArrayList param = new ArrayList();
        param.add(RECONSTATUS_NOTPROCESSED);

        factory = BankFusionThreadLocal.getPersistanceFactory();

        List<IBOUB_REC_RECONTRANDETAILS> listOfReconTrans = factory.findByQuery(IBOUB_REC_RECONTRANDETAILS.BONAME, selectClause,
                param, null, false);

        if (listOfReconTrans.size() == 0) {
            CBS_LOGGER.info("process update()", "No rows to update");
        }
        else {
            Iterator iRecon = listOfReconTrans.iterator();
            while (iRecon.hasNext()) {
                IBOUB_REC_RECONTRANDETAILS reconTransDetail = (IBOUB_REC_RECONTRANDETAILS) iRecon.next();
                reconTransDetail.setF_RECONSTATUS(RECONSTATUS_RECONPENDING);
                reconTransDetail.setF_RECONDTTM(t1);
            }
        }

    }
}
