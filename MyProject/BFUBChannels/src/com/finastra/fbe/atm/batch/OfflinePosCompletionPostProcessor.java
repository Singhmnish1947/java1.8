package com.finastra.fbe.atm.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.IBatchPostProcess;
import com.trapedza.bankfusion.batch.process.engine.BatchStatus;
import com.trapedza.bankfusion.batch.process.engine.IBatchStatus;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class OfflinePosCompletionPostProcessor implements IBatchPostProcess {

    private String INSERT_QUERY =
        "INSERT INTO UBTB_POSOPERATIONDETAILSHIST(  UBTRANSACTIONTYPE,  UBORIGINATORREFNUM,  UBFEECURRENCY,  VERSIONNUM,  UBAMOUNTRECON,  UBCARDHOLDERBILLINGAMT,  UBTRANDATE,  UBTRANSACTIONCURRENCY,  UBACCOUNTID,  UBTRANSACTIONFEEAMOUNT,  UBRECONCURRENCY,  UBTERIMANALID,  UBTRANSACTIONAMOUNT,  UBDIRECTION,  UBACQUIRINGINSTITUTIONID,  UBPOSOPERATIONDETAILSHISTIDPK,  UBCARDNUMBER,  UBCARDHOLDERBILLINGCURRENCY,UBCMSUNIQUEENDTXNREF,UBISMATCHEDTXN,UBISSUSPOSTED,UBCREDITACCOUNTID,UBFILENAME,UBFILEID,UBTRANSACTIONID)SELECT S.UBTRANSACTIONTYPE,  S.UBORIGINATORREFNUM,  S.UBFEECURRENCY,  S.VERSIONNUM,  S.UBAMOUNTRECON,  S.UBCARDHOLDERBILLINGAMT,  S.UBTRANDATE,  S.UBTRANSACTIONCURRENCY,  S.UBACCOUNTID,  S.UBTRANSACTIONFEEAMOUNT,  S.UBRECONCURRENCY,  S.UBTERIMANALID,  S.UBTRANSACTIONAMOUNT,  S.UBDIRECTION,  S.UBACQUIRINGINSTITUTIONID,  S.UBPOSOPERATIONDETAILSIDPK,  S.UBCARDNUMBER,  S.UBCARDHOLDERBILLINGCURRENCY, S.UBCMSUNIQUEENDTXNREF,S.UBISMATCHEDTXN,S.UBISSUSPOSTED,S.UBCREDITACCOUNTID,S.UBFILENAME,S.UBFILEID,S.UBTRANSACTIONID FROM UBTB_POSOPERATIONDETAILS S join UBTB_POSOPERATIONDETAILSTAG T on S.UBPOSOPERATIONDETAILSIDPK = T.UBPOSOPERATIONDETAILSIDPK  where T.UBPROCESSSTATE = 'P'";

    private final IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    private static final Log LOGGER = LogFactory.getLog(OfflinePosCompletionPostProcessor.class.getName());

    private String DELETEQUERY =
        "delete FROM UBTB_POSOPERATIONDETAILS where UBPOSOPERATIONDETAILSIDPK in ( select UBPOSOPERATIONDETAILSIDPK from UBTB_POSOPERATIONDETAILSTAG where UBPROCESSSTATE = 'P')";

    private String DELETEQUERY1 = "delete FROM UBTB_POSOPERATIONDETAILSTAG";

    private String UPDATEQUERY = "update UBTB_POSOPERATIONDETAILSTAG set UBROWSEQ = 0 where UBPROCESSSTATE = 'F' OR UBPROCESSSTATE IS NULL";

    @Override
    public void init(BankFusionEnvironment arg0, AbstractFatomContext arg1, IBatchStatus arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public IBatchStatus process(AbstractProcessAccumulator arg0) {
        // TODO Auto-generated method stub
        Connection connection;
        PreparedStatement ps = null;
        BatchStatus batchStatus = new BatchStatus();
        try {
            LOGGER.info("OfflinePosCompletionPostProcessor STARTED :::::");
            connection = factory.getJDBCConnection();
            ps = connection.prepareStatement(INSERT_QUERY);
            ps.execute();
            ps = connection.prepareStatement(DELETEQUERY);
            ps.execute();
            ps = connection.prepareStatement(DELETEQUERY1);
            ps.execute();
            ps = connection.prepareStatement(UPDATEQUERY);
            ps.execute();
            factory.commitTransaction();
            LOGGER.info("OfflinePosCompletionPostProcessor ENDED :::::");
            // List<Integer> mergedAccumulator=(List<Integer>)arg0.getMergedTotals()[0];
        } catch (Exception e) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(e));
            factory.rollbackTransaction();
            batchStatus.setStatus(false);
            batchStatus.setBatchFailureMessage(ExceptionUtil.getExceptionAsString(e));
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException sqlException) {
                LOGGER.error(ExceptionUtil.getExceptionAsString(sqlException));
            }
        }
        batchStatus.setStatus(true);
        return batchStatus;
    }

}
