/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: ErrorLoger.java,v.1.0,Apr 22, 2009 1:57:38 PM ayerla
 *
 */
package com.misys.ub.common.almonde.helper;

import java.sql.Date;
import java.sql.Timestamp;

import com.trapedza.bankfusion.bo.refimpl.IBOBatchFileLog;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_BATCHERRORLOG;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_BATCHFILELOG;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * @author ayerla
 * @date Apr 22, 2009
 * @project Universal Banking
 * @Description: BatchLogger logs the details of the Batch process.
 */

public class BatchLogger {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    
    /**
     * Method Description: Logs the processes file details in to UBTB_ALDBATCHFILELOG Table.
     * @param batchRef
     * @param newFileName
     * @param oldFileName
     * @param description
     * @param processesTime
     * @param numUploaded
     * @param numFailed
     */
    public void createBatchFileLog(String batchRef,String newFileName, String oldFileName, String description, Timestamp processesTime,Integer numUploaded, Integer numFailed) {
        
        IBOUB_ALD_BATCHFILELOG batchFileLogBO = (IBOUB_ALD_BATCHFILELOG) factory.getStatelessNewInstance(IBOUB_ALD_BATCHFILELOG.BONAME);
        batchFileLogBO.setF_UBALDORIGFILENAME(oldFileName);
        batchFileLogBO.setF_UBALDNEWFILENAME(newFileName);
        batchFileLogBO.setF_UBALDFILEDESC(description);
        batchFileLogBO.setF_UBALDFILEPROCESSDTTM(processesTime);
        batchFileLogBO.setF_UBALDNOOFUPLOADS(numUploaded);
        batchFileLogBO.setF_UBALDNOOFFAILURES(numFailed);
        batchFileLogBO.setBoID(batchRef);
        factory.create(IBOUB_ALD_BATCHFILELOG.BONAME, batchFileLogBO);
        factory.commitTransaction();
        factory.beginTransaction();
    }

    /**
     * Method Description: Logs the batch errors.
     * @param batchRef
     * @param entityType
     * @param entityCode
     * @param ratingCode
     * @param ratingTerm
     * @param ratingValue
     * @param currencyCode
     * @param errorMessage
     */
    public void createBatchErrorLog(String batchRef, String entityType, String entityCode, String ratingCode, String ratingTerm,
            String ratingValue, String currencyCode, String errorMessage) {
        
        IBOUB_ALD_BATCHERRORLOG batchErrorLogBO = (IBOUB_ALD_BATCHERRORLOG) factory
                .getStatelessNewInstance(IBOUB_ALD_BATCHERRORLOG.BONAME);
        
        batchErrorLogBO.setF_UBALDBATCHREFERENCE(batchRef);
        batchErrorLogBO.setF_UBALDCURRENCYCODE(currencyCode);
        batchErrorLogBO.setF_UBALDENTITYCODE(entityCode);
        batchErrorLogBO.setF_UBALDENTITYTYPE(entityType);
        batchErrorLogBO.setF_UBALDRATINGCODE(ratingCode);
        batchErrorLogBO.setF_UBALDRATINGTERM(ratingTerm);
        batchErrorLogBO.setF_UBALDRATINGVALUE(ratingValue);
        batchErrorLogBO.setF_UBALDERRORMSG(errorMessage);
        
        factory.create(IBOUB_ALD_BATCHERRORLOG.BONAME, batchErrorLogBO);
        factory.commitTransaction();
        factory.beginTransaction();
    }
}
