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
import com.misys.ub.financial.events.FinancialEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractBranchPowerAccountRefresh;

public class BranchPowerAccountRefresh extends AbstractBranchPowerAccountRefresh {

    /* Parameter's for Reports - Start */
    static Properties fileProp = null;
    static String fromBranch = CommonConstants.EMPTY_STRING;
    static String toBranch = CommonConstants.EMPTY_STRING;
    String accRefFlag = "";
    /* Parameter's for Reports - End */
    Properties bfRefreshProperty = null;

    private transient final static Log logger = LogFactory.getLog(BranchPowerAccountRefresh.class.getName());

    public BranchPowerAccountRefresh(BankFusionEnvironment env) {
        super(env);
    }

    private static final String BATCH_PROCESS_NAME = "BranchPowerAccountRefresh";

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.batch.fatom.AbstractBatchFatom#getFatomContext()
     */
    protected AbstractFatomContext getFatomContext() {
        return (new BranchPowerAccountRefreshFatomContext(BATCH_PROCESS_NAME));

    }

    public void processBatch(BankFusionEnvironment env, AbstractFatomContext ctx) throws BankFusionException {
        try {

            readRefreshFile();
            IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

            BatchUtil.truncateTagTableUsingProc("UBTB_BPWACCREFRESHTAG", factory);

            String INSERT_SQL = "INSERT INTO UBTB_BPWACCREFRESHTAG (UBACCOUNTID,UBROWSEQ,UBACCOUNTNAME,UBBOOKEDBALANCE,"
                    + "UBCLEAREDBALANCE,UBISOCURRENCYCODE,UBACCSTOPPED,UBBRANCHSORTCODE,UBCREDITLIMIT,UBDEBITLIMIT,UBLIMITINDICATOR,UBCHEQUEDEPOSITBALANCE,"
                    + "UBACCOUNTDESCRIPTION,UBBLOCKEDBALANCE,UBLASTTRANSACTIONDATE,UBPORTFOLIOID,UBPRODUCTNUMERICCODE,UBLIMILIMITREF1,UBLIMLIMITREF2,"
                    + "UBLIMLIMITREF3,UBLIMLIMITREF4,UBLIMLIMITREF5,UBACCDCRINTEREST,UBDEBITACCDINTEREST,VERSIONNUM,UBCLOSED,UBACCRIGHTSINDICATOR)"
                    + " SELECT c.ACCOUNTID,c.UBROWSEQ ,c.ACCOUNTNAME,c.BOOKEDBALANCE,c.CLEAREDBALANCE,c.ISOCURRENCYCODE,c.STOPPED,c.BRANCHSORTCODE,"
                    + "c.CREDITLIMIT,c.DEBITLIMIT,c.LIMITINDICATOR,c.CHEQUEDEPOSITBALANCE, c.ACCOUNTDESCRIPTION,c.BLOCKEDBALANCE,c.LASTTRANSACTIONDATE,"
                    + "c.PORTFOLIOID,c.PRODUCT_NUMERICCODE,c.LIM_LIMITREF1,c.LIM_LIMITREF2,c.LIM_LIMITREF3,c.LIM_LIMITREF4,c.LIM_LIMITREF5,c.ACCDCRINTEREST,"
                    + "c.DEBITACCDINTEREST,c.VERSIONNUM,c.CLOSED,c.ACCRIGHTSINDICATOR "
                    + " FROM VW_ACCOUNTDTL c, BRANCH b  where c.BRANCHSORTCODE = b.BRANCHSORTCODE and b.BMBRANCH between ? AND ? ";

            Connection connection = factory.getJDBCConnection();
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, fromBranch);
                ps.setString(2, toBranch);

                int totalRecordsodUpdated = ps.executeUpdate();
                if (logger.isDebugEnabled()) {
                    logger.debug("Records Inserted: " + totalRecordsodUpdated);
                }
                factory.commitTransaction();
                factory.beginTransaction();
            }
            catch (SQLException exception) {
            	logger.error(exception);
            	  factory.rollbackTransaction();  //
                   factory.beginTransaction();     //

            }
            finally {
                    if (ps != null)
                        ps.close();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("process(): starting service Exception Event Batch Process");

            }

            Object obj = null;
            logger.debug("BranchPowerAccountRefresh");
            Map outputDataMap = ctx.getOutputTagDataMap();
            ctx.setOutputTagDataMap(outputDataMap);
            if (logger.isDebugEnabled()) {
                logger.debug("process(): starting service");
            }
            BatchService service = (BatchService) ServiceManager.getService(ServiceManager.BATCH_SERVICE);
            service.runBatch(env, ctx);
            obj = (Object) ctx.getAdditionalProcessParams()[0];
            if (obj instanceof BankFusionException) {

                BankFusionException e = (BankFusionException) obj;
                logger.debug(e.getLocalisedMessage());
                BranchPowerAccountRefreshFatomContext.Status = Boolean.FALSE;
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

            logger.error(ex);
            EventsHelper.handleEvent(FinancialEventCodes.E_UNEXPECTED_ERROR, new Object[] {}, new HashMap(), env);
        }
    }

    /**
     *
     */
    private void readRefreshFile() {
        String path = GetUBConfigLocation.getUBConfigLocation();
        // String path = System.getProperty("BFconfigLocation");
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
	private Properties loadInfoFromLocal(String string)
			throws BankFusionException {
		Properties fileProp = null;
		InputStream input = null;
		try {
			input = new FileInputStream(string);
			// InputStream input =
			// this.getClass().getClassLoader().getResourceAsStream(string);
			fileProp = new java.util.Properties();
			fileProp.load(input);
			return fileProp;
		} catch (Exception e) {
			fileProp = new Properties();
			logger.error(e);
		} finally {
			if (input != null) {
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
            logger.error(e);

        }
        return fileProp;
    }

    protected void setOutputTags(AbstractFatomContext ctx1) {

        if (BranchPowerAccountRefreshFatomContext.Status == Boolean.TRUE) {
            setF_OUT_Message("BranchPowerAccountRefresh Files are Generated Successfully");
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
        accRefFlag = getProperty("ACCOUNT-REFRESH");
        setFatomOutputTagsForReports();
    }

    protected void setFatomOutputTagsForReports() {
        // TODO Auto-generated method stub

        /* Setting output tags of BranchPowerRefreshFatom,that is used for BranchPowerRefresh report */

        setF_OUT_FROMBRANCH(fromBranch);
        setF_OUT_TOBRANCH(toBranch);
        setF_OUT_ACCOUNTREFRESH(accRefFlag);
    }
}
