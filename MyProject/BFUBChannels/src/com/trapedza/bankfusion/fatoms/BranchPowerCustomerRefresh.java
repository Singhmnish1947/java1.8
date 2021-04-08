/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.misys.ub.fatoms.batch.bpRefresh.account.BranchPowerAccountRefreshFatomContext;
import com.misys.ub.fatoms.batch.bpRefresh.customer.BranchPowerCustomerRefreshFatomContext;
import com.misys.ub.financial.events.FinancialEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractBranchPowerCustomerRefresh;

public class BranchPowerCustomerRefresh extends AbstractBranchPowerCustomerRefresh {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /* Parameter's for Reports - Start */
    static Properties fileProp = null;
    String fromBranch = "";
    String toBranch = "";
    String custRefFlag = "";
    /* Parameter's for Reports - End */
    Properties bfRefreshProperty = null;

    private transient final static Log logger = LogFactory.getLog(BranchPowerCustomerRefresh.class.getName());

    public BranchPowerCustomerRefresh(BankFusionEnvironment env) {
        super(env);
    }

    private static final String BATCH_PROCESS_NAME = "BranchPowerCustomerRefresh";

    /*
     * (non-Javadoc)
     *
     * @see com.trapedza.bankfusion.batch.fatom.AbstractBatchFatom#getFatomContext()
     */
    protected AbstractFatomContext getFatomContext() {
        return (new BranchPowerCustomerRefreshFatomContext(BATCH_PROCESS_NAME));

    }

    public void processBatch(BankFusionEnvironment env, AbstractFatomContext ctx) throws BankFusionException {
       
    	Connection connection = null;
    	PreparedStatement ps =null;
    	try {

            IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

            BatchUtil.truncateTagTableUsingProc("UBTB_BPWCUSTREFRESHTAG", factory);

            readRefreshFile();


            String INSERT_SQL = "INSERT INTO UBTB_BPWCUSTREFRESHTAG (UBIDPK,UBCUSTOMERCODE,UBSHORTNAME,UBALPHACODE,"
                    + "UBREPORTINGCURRENCY,UBBRANCHSORTCODE,UBLIMITINDICATOR,UBADDRESSLINE1,UBADDRESSLINE2,UBADDRESSLINE3,"
                    + "UBADDRESSLINE4,UBADDRESSLINE5,UBADDRESSLINE6,UBADDRESSLINE7,UBPOSTZIPCODE,UBROWSEQ,VERSIONNUM) "
                    + "SELECT C.IDPK,C.CUSTOMERCODE,C.SHORTNAME,C.ALPHACODE,C.REPORTINGCURRENCY,C.BRANCHSORTCODE,C.LIMITINDICATOR,C.ADDRESSLINE1,"
                    + "C.ADDRESSLINE2,C.ADDRESSLINE3,C.ADDRESSLINE4,C.ADDRESSLINE5,C.ADDRESSLINE6,C.ADDRESSLINE7,C.POSTZIPCODE,C.UBROWSEQ,C.VERSIONNUM "
                    + "FROM UBVW_CUSTOMERDTL c, BRANCH b  where c.BRANCHSORTCODE = b.BRANCHSORTCODE and b.BMBRANCH between ? AND ? ";

            connection = factory.getJDBCConnection();
            try {
                ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, fromBranch);
                ps.setString(2, toBranch);
                int totalRecordsodUpdated = ps.executeUpdate();
                factory.commitTransaction();
                factory.beginTransaction();
                if (logger.isDebugEnabled()) {
                    logger.debug("Records Inserted: " + totalRecordsodUpdated);
                }
            }
            catch (SQLException exception) {
                logger.error("Error occured :" + exception.getLocalizedMessage());
                logger.error(exception);
                factory.rollbackTransaction();   //
                factory.beginTransaction();      //
            }
            if (logger.isDebugEnabled()) {
                logger.debug("process(): starting service Exception Event Batch Process");

            }

            Object obj = null;
            logger.debug("BranchPowerCustomerRefresh");
            Map outputDataMap = ctx.getOutputTagDataMap();
            ctx.setOutputTagDataMap(outputDataMap);
            if (logger.isDebugEnabled()) {
                logger.debug("process(): starting service");
            }
            BatchService service = (BatchService) ServiceManager.getService(ServiceManager.BATCH_SERVICE);
            service.runBatch(BankFusionThreadLocal.getBankFusionEnvironment(), ctx);
            obj = (Object) ctx.getAdditionalProcessParams()[0];
            if (obj instanceof BankFusionException) {

                BankFusionException e = (BankFusionException) obj;
                logger.debug(e.getLocalisedMessage());
                BranchPowerCustomerRefreshFatomContext.Status = Boolean.FALSE;
            }
            else {
                bfRefreshProperty = (Properties) ctx.getAdditionalProcessParams()[0];
                BranchPowerAccountRefreshFatomContext.Status = Boolean.TRUE;
            }
            setOutputTags(ctx);
        }
        catch (BankFusionException bfe) {
            throw bfe;
        }
        catch (Exception ex) {
            // throw new BankFusionException(5031, null, logger, null);
            EventsHelper.handleEvent(FinancialEventCodes.E_UNEXPECTED_ERROR, new Object[] {},
                    new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
            logger.error(ex);
        }
    	finally{
    		if(ps!=null){
    		try{
    		ps.close();
    		connection.close();
    		}
    		catch(Exception e){
   			logger.error(e);
    		}
    		}
    	}
    }

    private void readRefreshFile() {
    	String path = GetUBConfigLocation.getUBConfigLocation();
        //String path = System.getProperty("BFconfigLocation");
        Properties propertiesObj = new Properties();
        try {

            propertiesObj = loadInfoFromLocal(path + "/conf/bpRefresh/BPRefresh.properties");

        }
        catch (Exception fnfExcpn) {
            propertiesObj = loadInfoFromJar("bpRefresh/BPRefresh.properties");
            // propertiesObj[1] = loadInfoFromJar("bpRefresh/Refresh.properties");
            logger.error(fnfExcpn);
        }

        if ((propertiesObj!=null && propertiesObj.size() != 0)) {
            fromBranch = propertiesObj.get("FROMBRANCH").toString();
            toBranch = propertiesObj.get("TOBRANCH").toString();
        }

    }

    /**
     * loads information available in (conf)\refresh.properties file to memory
     *
     * @param string
     */
    private Properties loadInfoFromLocal(String string) throws BankFusionException {
        Properties fileProp = null;
        InputStream input = null;
        try {
            input = new FileInputStream(string);
            // InputStream input = this.getClass().getClassLoader().getResourceAsStream(string);
            fileProp = new java.util.Properties();
            fileProp.load(input);
            return fileProp;
        }
        catch (Exception e) {
            fileProp = new Properties();
            logger.error(e);
        }
        finally{
        	if(input!=null){
        	try {
        		input.close();
			} catch (Exception e) {
				logger.error(e);
				
			}
        	}
        }
        return fileProp;
    }

    private Properties loadInfoFromJar(String string) {

        Properties fileProp = null;

        try {
            // InputStream input = new FileInputStream(string);
            InputStream input = this.getClass().getClassLoader().getResourceAsStream(string);
            fileProp = new java.util.Properties();
            fileProp.load(input);
            input.close();
        }
        catch (Exception e) {
            fileProp = new Properties();

        }
        return fileProp;
    }

    protected void setOutputTags(AbstractFatomContext ctx1) {

        if (BranchPowerCustomerRefreshFatomContext.Status == Boolean.TRUE) {
            setF_OUT_Message("BranchPowerCustomerRefresh Files are Generated Successfully");
            populateReportParams();
        }
        else {
            setF_OUT_Message("Process Failed");
        }
        setF_OUT_Batch_Status(Boolean.TRUE);

    }

    private String getProperty(String key) {
        return (String) bfRefreshProperty.get(key);
    }

    protected void populateReportParams() {

        /* Getting input from the BPRefresh.properties,that is used for BranchPowerRefresh report */
        fromBranch = getProperty("FROMBRANCH");
        toBranch = getProperty("TOBRANCH");
        custRefFlag = getProperty("CUSTOMER-REFRESH");
        setFatomOutputTagsForReports();
    }

    protected void setFatomOutputTagsForReports() {
        // TODO Auto-generated method stub

        /* Setting output tags of BranchPowerRefreshFatom,that is used for BranchPowerRefresh report */

        setF_OUT_FROMBRANCH(fromBranch);
        setF_OUT_TOBRANCH(toBranch);
        setF_OUT_CUSTOMERREFRESH(custRefFlag);
    }
}
