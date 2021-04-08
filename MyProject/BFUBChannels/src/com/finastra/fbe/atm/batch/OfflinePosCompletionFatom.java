package com.finastra.fbe.atm.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.bo.refimpl.IBOPosClearFileDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOPosOperationDetailsTag;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.fatoms.ValidateExchangeRateFileDetails;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_BAT_OfflinePosCompletionFatom;

public class OfflinePosCompletionFatom extends AbstractUB_BAT_OfflinePosCompletionFatom {

    private static final Log LOGGER = LogFactory.getLog(OfflinePosCompletionFatom.class.getName());

    private static final Integer UNHANDLED_EXCEPTION = 40112471;

    private static IPersistenceService pService =
        (IPersistenceService) ServiceManagerFactory.getInstance().getServiceManager().getServiceForName(ServiceManager.PERSISTENCE_SERVICE);

    private static String fileId;

    public OfflinePosCompletionFatom(BankFusionEnvironment env) {
        super(env);
    }

    public OfflinePosCompletionFatom() {
        super();
    }

    @Override
    protected AbstractFatomContext getFatomContext() {
        return (new OfflinePosCompletionContext("OfflinePosCompletion"));
    }

    @Override
    public Map getInDataMap() {
        // TODO Auto-generated method stub
        Map dataInMap = new HashMap();
        dataInMap.put("DO_NOT_PARK", Boolean.TRUE);
        dataInMap.put("BATCH_CODE", "");
        dataInMap.put("PARK_ON_UI", Boolean.FALSE);
        dataInMap.put("SYNCHRONISED", Boolean.TRUE);
        return dataInMap;
    }

    @Override
    protected void processBatch(BankFusionEnvironment paramBankFusionEnvironment, AbstractFatomContext paramAbstractFatomContext) {
        fileId = getF_IN_FileId();
        String fileName = getF_IN_FileName();
        String UPDATE_QUERY =
            "insert into UBTB_POSOPERATIONDETAILSTAG(UBPOSOPERATIONDETAILSIDPK ,UBACCOUNTID , UBROWSEQ , UBFILEID , VERSIONNUM ) select  UBPOSOPERATIONDETAILSIDPK, UBACCOUNTID, DENSE_RANK() OVER (order by UBACCOUNTID) UBROWSEQ, UBFILEID, VERSIONNUM from UBTB_POSOPERATIONDETAILS where UBFILEID = ?";
        Connection connection = null;
        PreparedStatement ps = null;
        boolean batchStatus = false;

        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

        try {
            LOGGER.info("OfflinePosCompletionFatom STARTED :::::");
            checkForRetry(fileName);
            connection = factory.getJDBCConnection();
            ps = connection.prepareStatement(UPDATE_QUERY);
            ps.setString(1, fileId);
            ps.execute();
            factory.commitTransaction();
            factory.beginTransaction();
            batchStatus = runBatch(paramBankFusionEnvironment, paramAbstractFatomContext);
            setF_OUT_status(batchStatus);
            LOGGER.info("OfflinePosCompletionFatom ENDED :::::");
        } catch (Exception exception) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
            factory.rollbackTransaction();
            factory.beginTransaction();
            throw new BankFusionException(UNHANDLED_EXCEPTION, exception.getLocalizedMessage());
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException sqlException) {
                LOGGER.error(ExceptionUtil.getExceptionAsString(sqlException));
            }
        }
    }

    public static boolean checkForRetry(String fileName) throws Exception {
        // TODO Auto-generated method stub
        IPersistenceObjectsFactory privateFactory = null;
        try {
            if (fileName.contains("RETRYFILE")) {
                fileName = fileName.replaceFirst("RETRYFILE(\\d)*", "");
                privateFactory = pService.getPrivatePersistenceFactory(false);
                privateFactory.beginTransaction();
                ArrayList param = new ArrayList();
                param.add(fileName);
                @SuppressWarnings("deprecation")
                List<IBOPosClearFileDetails> clearFileRetry = (List<IBOPosClearFileDetails>) privateFactory
                    .findByQuery(IBOPosClearFileDetails.BONAME, "WHERE " + IBOPosClearFileDetails.FILENAME + " = ?", param, null);
                if (clearFileRetry != null && clearFileRetry.size() != 0) {
                    fileId = clearFileRetry.get(0).getBoID();
                    // validateFile(fileBoid);
                    return true;
                }
                privateFactory.commitTransaction();
            }
            return false;
        } catch (Exception exception) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
            if (privateFactory != null) {
                privateFactory.rollbackTransaction();
            }
            throw exception;
        } finally {
            if (privateFactory != null)
                privateFactory.closePrivateSession();
        }
    }

    private void validateFile(String fileBoid) throws Exception {
        // TODO Auto-generated method stub
        IPersistenceObjectsFactory privateFactory = null;
        try {
            privateFactory = pService.getPrivatePersistenceFactory(false);
            privateFactory.beginTransaction();
            ArrayList param = new ArrayList();
            param.add(fileBoid);
            IBOPosOperationDetailsTag clearFile = (IBOPosOperationDetailsTag) privateFactory.findByQuery(IBOPosOperationDetailsTag.BONAME,
                "WHERE " + IBOPosOperationDetailsTag.FILEID + " = ?", param, null);
            if (clearFile == null) {
                LOGGER.error("Retry File does not exist");
                throw new Exception();
            }
            privateFactory.commitTransaction();
        } catch (Exception exception) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
            if (privateFactory != null) {
                privateFactory.rollbackTransaction();
            }
            throw exception;
        } finally {
            if (privateFactory != null)
                privateFactory.closePrivateSession();
        }
    }

    private boolean runBatch(BankFusionEnvironment paramBankFusionEnvironment, AbstractFatomContext paramAbstractFatomContext) {
        LOGGER.info(":::::starting batch for Pos clearing:::::");
        boolean batchStatus;
        IServiceManager serviceManager = ServiceManagerFactory.getInstance().getServiceManager();
        BatchService service = (BatchService) serviceManager.getServiceForName(ServiceManager.BATCH_SERVICE);
        OfflinePosCompletionContext offlinePosCompletionContext = (OfflinePosCompletionContext) paramAbstractFatomContext;
        offlinePosCompletionContext.setBatchProcessName("OfflinePosCompletion");
        batchStatus = service.runBatch(paramBankFusionEnvironment, paramAbstractFatomContext);
        return batchStatus;
    }
    
    @Override
    protected void setOutputTags(AbstractFatomContext paramAbstractFatomContext) {
        // TODO Auto-generated method stub

    }

}
