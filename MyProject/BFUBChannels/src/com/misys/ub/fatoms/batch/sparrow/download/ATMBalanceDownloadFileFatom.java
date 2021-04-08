/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * $Id: ATMBalanceDownloadFileFatom.java,v 1.1 2008/11/26 09:00:07 bhavyag Exp $
 *
 * $Log: ATMBalanceDownloadFileFatom.java,v $
 * Revision 1.1  2008/11/26 09:00:07  bhavyag
 * merging 3-3B changes for bug 13192 and 12581
 *
 * Revision 1.1.4.3  2008/10/14 01:25:28  mangesh
 * BUG ID - 13192 - Modified to set the number records down loaded / processed to the output tag.
 *
 * Revision 1.1.4.2  2008/09/23 08:09:49  mangesh
 * BUGID - 12581 - new Batch process for processing ATM Balance Download.
 *
 *
 */
package com.misys.ub.fatoms.batch.sparrow.download;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.cbs.common.util.log.CBSLogger;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.bo.refimpl.IBOATMAccountDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ATMCIBTAG;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CMN_BatchProcessLog;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMBalanceDownloadFileFatom;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * @description This class is used to call the Batch service for ATM Balance Download.
 *
 * @author Mangesh Hagargi
 *
 */
public class ATMBalanceDownloadFileFatom extends AbstractATMBalanceDownloadFileFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
	 */
	private static final String CLASS_NAME = ATMBalanceDownloadFileFatom.class.getName();
	private static final transient CBSLogger logger = new CBSLogger(CLASS_NAME);
	private static final String FILEDIR = "ATMDownload.FileDir";
    private boolean status = false;

    private IPersistenceObjectsFactory factory = null;

    private boolean isFullDownload = false;

	/**
	 * Stores the number of records for balance download
	 */
	private int recordsUpdated = 0;

	/*
	 * This Sql will be Used to Initilize Balance Download for Moved Balance Download.
	 */
	private static final String initilizeMovedBalanceDownloadsql = "where " + IBOATMAccountDetails.BALANCEMOVEDFLAG
			+ " = ?";

    // Inserting records from UBTB_ATMCIB table to temporary UBTB_ATMCIBTAG table

    private static String INSERT_SQL_QUERRY = "INSERT INTO "
            + "UBTB_ATMCIBTAG(UBCIBIDPK, UBIMDCODE, UBBMBRANCH, UBROWSEQ, VERSIONNUM) SELECT UBCIBID, UBIMDCODE, UBBMBRANCH, ROW_NUMBER() "
            + "OVER (ORDER BY UBCIBID) UBROWSEQ, 0 VERSIONNUM  FROM (SELECT UBCIBID, UBIMDCODE, UBBMBRANCH FROM UBTB_ATMCIB)ATMCIBIDLIST";

    public ATMBalanceDownloadFileFatom(BankFusionEnvironment env) {
        super(env);

    }

    /**
     * Set up the Batch Process Context class.
     */
    protected AbstractFatomContext getFatomContext() {
        return new ATMBalanceDownloadFileContext();

    }


	public boolean validateBatchProcess(BankFusionEnvironment env) {
    	 if(!validateFilePath(env))
         {	status = false;
         	setF_OUT_Status(status);
         	return false;
         }
    	 return true;
    }
    /**
     * This method will initiate the execution of ATM balance download batch process. The method
     * gets the configuration details like down load type Moved or Full Balance download. The
     * methods called are 1. getATMconfiguration 2. intialiseATMAccountDetails
     *
     * Creates the handle for down load file which is picked from the server.properties file. This
     * handle is then passed on thru the context. Every worker process will then use this handle to
     * generate the message format and append to the file.
     *
     *
    */


    protected void processBatch(BankFusionEnvironment env, AbstractFatomContext context) {
        env.putObject("ATMdownloadRecord", new Integer(0));
		PreparedStatement ps = null;
        factory = BankFusionThreadLocal.getPersistanceFactory();
        initilizeBalanceDownload(env);
        factory.beginTransaction();
        factory.bulkDeleteAll(IBOUB_ATMCIBTAG.BONAME);
        factory.commitTransaction();
        factory.beginTransaction();

		if (logger.isDebugEnabled())
		{
			logger.debug("processBatch()","Schema Name:" +  ",SQL Querry:"+ INSERT_SQL_QUERRY);
		}

        Connection connectin = factory.getJDBCConnection();
        boolean updated = false;
        try {
			ps = connectin.prepareStatement(INSERT_SQL_QUERRY);
            updated = ps.execute();
        }
        catch(SQLException sqlException)	{
        	logger.error("processBatch()","SQL Exception:"+sqlException.getLocalizedMessage());
        	updated = false;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("processBatch()", "SQL Exception:"
 + e.getLocalizedMessage());
				}
			}
        }
        factory.commitTransaction();
        factory.beginTransaction();

        BatchService service = (BatchService) ServiceManager.getService(ServiceManager.BATCH_SERVICE);
        status = service.runBatch(env, context);
        Integer totRecord = (Integer) env.getObject("ATMdownloadRecord");
        if (logger.isInfoEnabled()){
		    logger.info("processBatch()","No Of Records processed: "+totRecord);
		}
		setF_OUT_TotalNumRecords(String.valueOf(totRecord.intValue()));
		env.removeObject("ATMdownloadRecord");

	}

    /**
     * This Function will Initilize the Balance Download Process. It will First Set the
     * BalanceDownloadFlag to true and will then populate the list of accounts for which Balance
     * Download needs to be performed.
     *
     * In ATMAccountDetails, check for the BalanceMovedFlag and BalanceDownloadFlag.
     *
     * For Full Download, Set the BalanceDownloadFlag = 'Y' then populate the list of accounts for
     * which Balance Download needs to be performed. ie. all accounts
     *
     * For Moved Download, Update BalanceDownloadFlag = ‘Y’ for all accounts for which balance has
     * moved (BalanceMovedFlag = 'Y'). Update BalanceDownloadFlag = ‘N’ for all accounts for which
     * balance has not moved (BalanceMovedFlag = 'N').
     */
    public void initilizeBalanceDownload(BankFusionEnvironment env) {

        ATMControlDetails controlDetails = ATMConfigCache.getInstance().getInformation(env);
        if (controlDetails.getBalanceDownloadType().equals("0")) {
            isFullDownload = true;
        }

        factory.beginTransaction();
        if (isFullDownload) {
            ArrayList columnList = new ArrayList();
            columnList.add(IBOATMAccountDetails.BALANCEDOWNLOADFLAG);

            ArrayList valuesList = new ArrayList();
            valuesList.add(new Boolean(true));
            recordsUpdated = factory.bulkUpdate(IBOATMAccountDetails.BONAME, columnList, valuesList);
        }
        else {
            ArrayList params = null;
            List valuesList = null;
            List columnList = null;

            // Updating Downloaded Status as Yes for all balances for which Balance has not Moved
            params = new ArrayList();
            valuesList = new ArrayList();
            columnList = new ArrayList();

            columnList.add(IBOATMAccountDetails.BALANCEDOWNLOADFLAG);
            params.add(new Boolean(true));
            valuesList.add(new Boolean(true));
            recordsUpdated = factory.bulkUpdate(IBOATMAccountDetails.BONAME, initilizeMovedBalanceDownloadsql, params, columnList,
                    valuesList);
            // Updating Downloaded Status as No for all balances for which Balance has Moved

            params = new ArrayList();
            valuesList = new ArrayList();
            columnList = new ArrayList();

            columnList.add(IBOATMAccountDetails.BALANCEDOWNLOADFLAG);
            params.add(new Boolean(false));
            valuesList.add(new Boolean(false));

            factory.bulkUpdate(IBOATMAccountDetails.BONAME, initilizeMovedBalanceDownloadsql, params, columnList, valuesList);
        }
        factory.commitTransaction();
        factory.beginTransaction();             ///
    }

    /**
     * This method sets the output tags for the fatom.
     */
    protected void setOutputTags(AbstractFatomContext context) {
        setF_OUT_Status(status);
    }
    // - @ayush :atrf964377 merger
    private void createBatchErrorLog(String key, String message, String status) {
        IBOUB_CMN_BatchProcessLog batchException = (IBOUB_CMN_BatchProcessLog)
                factory.getStatelessNewInstance(IBOUB_CMN_BatchProcessLog.BONAME);
        batchException.setBoID(GUIDGen.getNewGUID());
        batchException.setF_PROCESSNAME(key);
        //batchException.setF_RUNDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime(environment.getRuntimeMicroflowID()));
        batchException.setF_RUNDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime(BankFusionThreadLocal.getBankFusionEnvironment().getRuntimeMicroflowID()));
        batchException.setF_RECORDID(key);
        if (status.equalsIgnoreCase("E")) {
            if (null == message) {
                message = CommonConstants.EMPTY_STRING;
            }
            message = message.replaceAll(",", "");
            message = message.replaceAll(":", "");
            message = message.replaceAll("':", "");
            if (message.length() > 256) {
                batchException.setF_ERRORMESSAGE(message.substring(0, 255));
            } else {
                batchException.setF_ERRORMESSAGE(message);
            }
            batchException.setF_STATUS(status);
        } else {
        	if(logger.isInfoEnabled()){
                logger.info("createLogMessage()","Unprocessed Account [ " + key + " ] ");
        	 }
            batchException.setF_STATUS(status);
            batchException.setF_ERRORMESSAGE(message);
        }
        factory.create(IBOUB_CMN_BatchProcessLog.BONAME, batchException);
        factory.commitTransaction();
        factory.beginTransaction();
    }
    // - @ayush :atrf964377 merger
    public boolean validateFilePath(BankFusionEnvironment env){
    	String path = BankFusionPropertySupport.getProperty(BankFusionPropertySupport.UB_PROPERTY_FILE_NAME, FILEDIR, "");
    	File dir = new File(path);

    	if(!dir.isDirectory()){
    		createBatchErrorLog("ATMBalanceDownload", "File Directory is invalid", "E");
    		return false;
    	}

    	return true;



    }
}
