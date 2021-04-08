package com.misys.ub.fatoms.batch.bpwFailedTrans;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWHEADERTAG;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWRFAILEDTXNS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CMN_BatchProcessLog;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.ConvertToBigDecimal;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_BPW_FailedTransactionBatchProcess extends AbstractBatchProcess {
    private static final  Log logger = LogFactory.getLog(UB_BPW_FailedTransactionBatchProcess.class.getName());

    private UB_BPW_FailedTransactionBatchProcessAccumulator accumulator;
    private IPersistenceObjectsFactory factory;

    private static final String whereClause = "WHERE " + IBOUBTB_BPWHEADERTAG.UBROWSEQ + " between ? and ? ";

    // query for getting blob for IN using bfheaderid
    private static final String selectQueryForIN = "select * from bankfusion.bftb_auditdetailblob where bfdetailid ="
            + " (select bfauditdetailidpk from bankfusion.bftb_auditdetail where bfheaderid ="
            + " (select bfauditheaderidpk from bankfusion.bftb_auditheader where bfauditheaderidpk in"
            + " (select bfheaderid from bankfusion.bftb_auditdetail where cast(bfoldvalue as varchar(100)) in"
            + " (select cast(bfoldvalue as varchar(100)) from bankfusion.bftb_auditdetail where bfheaderid = ?"
            + " and bfkeyname = 'MessageGUID'))and bfeventtype = 'IN' and bfclientid  = 'BPWR') and bfkeyname = 'Message')";

    // query for getting blob for OUT using bfheaderid
    private static final String selectQueryForOUT = "select * from bankfusion.bftb_auditdetailblob where bfdetailid ="
            + " (select bfauditdetailidpk from bankfusion.bftb_auditdetail where bfheaderid ="
            + " (select bfauditheaderidpk from bankfusion.bftb_auditheader where bfauditheaderidpk in"
            + " (select bfheaderid from bankfusion.bftb_auditdetail where cast(bfoldvalue as varchar(100)) in"
            + " (select cast(bfoldvalue as varchar(100)) from bankfusion.bftb_auditdetail where bfheaderid = ?"
            + " and bfkeyname = 'MessageGUID'))and bfeventtype = 'OUT' and bfclientid  = 'BPWR') and bfkeyname = 'Message')";

    public UB_BPW_FailedTransactionBatchProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
        super(environment, context, priority);
    }

    public AbstractProcessAccumulator getAccumulator() {
        return accumulator;
    }

    public void init() {
        initialiseAccumulator();
    }

    protected void initialiseAccumulator() {
        Object[] accumulatorArgs = new Object[1];
        accumulatorArgs[0] = null;
        accumulator = new UB_BPW_FailedTransactionBatchProcessAccumulator(accumulatorArgs);
    }

    public AbstractProcessAccumulator process(int pageToProcess) {
        pagingData.setCurrentPageNumber(pageToProcess);

        factory = BankFusionThreadLocal.getPersistanceFactory();

        int pageSize = context.getPageSize();
        int fromValue = ((pageToProcess - 1) * pageSize) + 1;
        int toValue = pageToProcess * pageSize;

        ArrayList paramsForHeaderId = new ArrayList();
        paramsForHeaderId.add(fromValue);
        paramsForHeaderId.add(toValue);

        List headerIdNumbers = factory.findByQuery(IBOUBTB_BPWHEADERTAG.BONAME, whereClause, paramsForHeaderId, null, false);
        // Set curent page records in thread local
        BankFusionThreadLocal.setCurrentPageRecordIDs(headerIdNumbers);
        Iterator pageData = headerIdNumbers.iterator();

        // fields for report table
        String sourceBranchId = null;
        String ubTransCode = null;
        String msgForcePost = null;
        String msg = null;
        String userId = null;
        String date = null;
        String time = null;
        String transSeqNo = null;
        String hostTaskId = null;
        String reasons = null;
        String mainAccNo = null;
        String currency = null;
        BigDecimal txnAmount = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal transAmount = CommonConstants.BIGDECIMAL_ZERO;
        int year = 0;
        int month = 0;
        int day = 0;
        String hour = null;
        String minutes = null;
        String seconds = null;
        String txnType = null;
        String forcePost = null;

        Connection connection = factory.getJDBCConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;

        try {
            while (pageData.hasNext()) {
                // get headerId
                IBOUBTB_BPWHEADERTAG headerIdTagRecord = (IBOUBTB_BPWHEADERTAG) pageData.next();
                String headerId = headerIdTagRecord.getBoID();
                // Set current record id in the thread local
                BankFusionThreadLocal.setCurrentRecordID(headerId);
                String reasonDsc = "";
                if (headerIdTagRecord.getF_UBREASON() != null) {
                    reasonDsc = headerIdTagRecord.getF_UBREASON();
                }

                // get blob for IN
                ps = connection.prepareStatement(selectQueryForIN);
                ps.setString(1, headerId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    byte[] bytesInBlob = rs.getBlob(4).getBytes(1, (int) rs.getBlob(4).length());

                    mainAccNo = getStringValue(bytesInBlob, 139, 20);
                    currency = getStringValue(bytesInBlob, 216, 3);
                    int scale = SystemInformationManager.getInstance().getCurrencyScale(currency);
                    transAmount = ConvertToBigDecimal.run(getStringValue(bytesInBlob, 220, 18));
                    txnAmount = transAmount.movePointLeft(scale);

                    date = getStringValue(bytesInBlob, 102, 8);
                    if (null != date && !date.equals("")) {
                        year = Integer.parseInt(date.substring(0, 4));
                        month = Integer.parseInt(date.substring(4, 6));
                        day = Integer.parseInt(date.substring(6, 8));
                    }
                    time = getStringValue(bytesInBlob, 39, 6);
                    if (null != time && !time.equals("")) {
                        hour = time.substring(0, 2);
                        minutes = time.substring(2, 4);
                        seconds = time.substring(4, 6);
                        time = hour + ":" + minutes + ":" + seconds;
                    }
                }

                // get blob for OUT
                ps2 = connection.prepareStatement(selectQueryForOUT);
                ps2.setString(1, headerId);
                rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    byte[] bytesInBlob = rs2.getBlob(4).getBytes(1, (int) rs2.getBlob(4).length());

                    // String authorisedFlag = getStringValue(bytesInBlob, 127,
                    // 1);
                    sourceBranchId = getStringValue(bytesInBlob, 24, 4);
                    ubTransCode = getStringValue(bytesInBlob, 125, 2);
                    msg = getStringValue(bytesInBlob, 98, 1);
                    forcePost = getStringValue(bytesInBlob, 101, 1);
                    msgForcePost = msg + "-" + forcePost;
                    userId = getStringValue(bytesInBlob, 110, 4);
                    transSeqNo = getStringValue(bytesInBlob, 114, 7);
                    hostTaskId = getStringValue(bytesInBlob, 121, 4);
                    reasons = getStringValue(bytesInBlob, 138, 4);
                    txnType = getStringValue(bytesInBlob, 121, 2);
                    if (reasons != null) {

                        reasonDsc = reasons.concat(" : ").concat(reasonDsc);
                    }
                }

                // insert values into report table
                IBOUBTB_BPWRFAILEDTXNS failedTrns = (IBOUBTB_BPWRFAILEDTXNS) factory
                        .getStatelessNewInstance(IBOUBTB_BPWRFAILEDTXNS.BONAME);

                failedTrns.setBoID(GUIDGen.getNewGUID());

                failedTrns.setF_UBSOURCEBRANCHID(sourceBranchId);
                failedTrns.setF_UBMISTXNCODE(ubTransCode);
                failedTrns.setF_UBMSGFUNCFORCEPOST(msgForcePost);
                failedTrns.setF_UBUSERID(userId);
                failedTrns.setF_UBTXNSEQNO(transSeqNo);
                failedTrns.setF_UBHOSTTASKID(hostTaskId);
                failedTrns.setF_UBREASON(reasonDsc);
                failedTrns.setF_UBACCOUNTID(mainAccNo);
                failedTrns.setF_UBISPROCESSED("N");
                failedTrns.setF_UBTXNTYPE(txnType);
                failedTrns.setF_UBISOCURRENCYCODE(currency);
                if (null != transAmount) {

                    failedTrns.setF_UBTXNAMT(txnAmount);

                }
                year = year - 1900;
                month = month - 1;
                failedTrns.setF_UBTXNDT(new Date(year, month, day));
                failedTrns.setF_UBTXNTIME(time);
                factory.create(IBOUBTB_BPWRFAILEDTXNS.BONAME, failedTrns);
            }

        }
        catch (SQLException sqlException) {
            logger.error(sqlException.getLocalizedMessage());
        }
        finally {
                if (null != rs)
				try {
					rs.close();
				}
                catch (SQLException e) {
                	logger.error(e.getStackTrace());                }
                if (null != ps)
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error(e.getStackTrace()); 				}
                if (null != rs2)
				try {
					rs2.close();
				} catch (SQLException e) {
					logger.error(e.getStackTrace()); 				}
                if (null != ps2)
				try {
					ps2.close();
				} catch (SQLException e) {
					logger.error(e.getStackTrace()); 				}
            }
        return accumulator;
    }

    /**
     * 
     * @param data
     * @param from
     * @param length
     * @return
     */
    private String getStringValue(byte[] data, int from, int length) {

        String returnValue = null;

        byte[] outData;
        outData = new byte[length];
        System.arraycopy(data, from, outData, 0, length);
        returnValue = new String(outData);
        return returnValue;
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
            IBOUBTB_BPWHEADERTAG failedBPWTransactionItem = (IBOUBTB_BPWHEADERTAG) unprocessedIter.next();

            String key = failedBPWTransactionItem.getBoID();
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
