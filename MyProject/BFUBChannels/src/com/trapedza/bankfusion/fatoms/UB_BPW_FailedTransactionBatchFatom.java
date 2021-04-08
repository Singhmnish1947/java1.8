package com.trapedza.bankfusion.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.fatoms.batch.bpwFailedTrans.UB_BPW_FailedTransactionBatchFatomContext;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWHEADERTAG;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWRFAILEDTXNS;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_BPW_FailedTransactionBatchFatom;

public class UB_BPW_FailedTransactionBatchFatom extends AbstractUB_BPW_FailedTransactionBatchFatom {
    private transient final static Log logger = LogFactory.getLog(UB_BPW_FailedTransactionBatchFatom.class.getName());

    private static final String BATCH_PROCESS_NAME = "BPWFailedTransBatch";
    private IPersistenceObjectsFactory factory;

    public UB_BPW_FailedTransactionBatchFatom(BankFusionEnvironment env) {
        super(env);
    }

    protected AbstractFatomContext getFatomContext() {
        return (new UB_BPW_FailedTransactionBatchFatomContext(BATCH_PROCESS_NAME));
    }

    public void processBatch(BankFusionEnvironment environment, AbstractFatomContext context) {
        Map inputDataMap = getInDataMap();
        Map outputDataMap = getOutDataMap();
        context.setInputTagDataMap(inputDataMap);
        context.setOutputTagDataMap(outputDataMap);

        try {
            factory = BankFusionThreadLocal.getPersistanceFactory();
            factory.beginTransaction();
            factory.bulkDeleteAll(IBOUBTB_BPWHEADERTAG.BONAME);
            factory.bulkDeleteAll(IBOUBTB_BPWRFAILEDTXNS.BONAME);
            factory.commitTransaction();
            factory.beginTransaction();

            java.sql.Date repDate = getF_IN_REPDATE();


            String INSERT_SQL_QUERRY = " INSERT INTO UBTB_BPWHEADERTAG(UBHEADERID,UBREASON,UBROWSEQ, VERSIONNUM)  "
                    + "  SELECT BFAUDITHEADERIDPK,BFSECONDARYDATA, ROW_NUMBER() OVER (ORDER BY BFAUDITHEADERIDPK) UBROWSEQ, 0 VERSIONNUM FROM "
                    + "(SELECT BFAUDITHEADERIDPK, CAST(BFSECONDARYDATA AS VARCHAR(100)) AS BFSECONDARYDATA FROM  BANKFUSION.BFTB_AUDITHEADER "
                    + "WHERE (UB_TODATE(BFAUDITTIME) = ? AND BFOPERATION = 'FAILED' AND BFCLIENTID = 'BPWR')) BPWLIST";

            if (logger.isDebugEnabled()) {
                logger.debug("SQL Query -->" + INSERT_SQL_QUERRY);
            }

            Connection connection = factory.getJDBCConnection();
            PreparedStatement ps = null;

            try {
                ps = connection.prepareStatement(INSERT_SQL_QUERRY);
                ps.setDate(1, repDate);
                ps.execute();
            }
            catch (SQLException sqlException) {
                //logger.error(sqlException.getLocalizedMessage());
            	logger.error(sqlException);
            }
            finally {
                try {
                    if (null != ps)
                        ps.close();
                }
                catch (SQLException e) {
                	logger.error(e);
                }
            }
            factory.commitTransaction();
            factory.beginTransaction(); //


            BatchService service = (BatchService) ServiceManager.getService(ServiceManager.BATCH_SERVICE);
            service.runBatch(environment, context);
        }
        catch (BankFusionException e) {
            logger.error(e);
            factory.rollbackTransaction(); //
            factory.beginTransaction(); //

        }
    }

    protected void setOutputTags(AbstractFatomContext fatomContext) {
    }
}
