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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.fatoms.batch.bpRefresh.account.BranchPowerAccountRefreshFatomContext;
import com.misys.ub.fatoms.batch.bpRefresh.customer.BranchPowerCustomerRefreshFatomContext;
import com.misys.ub.fatoms.batch.bpRefresh.customerLimit.BranchPowerCustomerLimitRefreshFatomContext;
import com.misys.ub.financial.events.FinancialEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractBranchPowerCustomerLimitRefresh;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class BranchPowerCustomerLimitRefresh extends AbstractBranchPowerCustomerLimitRefresh {

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
    String custExtRefFlag = "";
    /* Parameter's for Reports - End */
    Properties bfRefreshProperty = null;

    private transient final static Log logger = LogFactory.getLog(BranchPowerCustomerLimitRefresh.class.getName());

    public BranchPowerCustomerLimitRefresh(BankFusionEnvironment env) {
        super(env);
    }

    private static final String BATCH_PROCESS_NAME = "BranchPowerCustomerLimitRefresh";

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.batch.fatom.AbstractBatchFatom#getFatomContext()
     */
    protected AbstractFatomContext getFatomContext() {
        return (new BranchPowerCustomerLimitRefreshFatomContext(BATCH_PROCESS_NAME));
    }

    public void processBatch(BankFusionEnvironment env, AbstractFatomContext ctx) throws BankFusionException {
        Object obj = null;
        try {
            logger.debug("BranchPowerCustomerLimitRefresh");
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
                BranchPowerCustomerRefreshFatomContext.Status = Boolean.FALSE;
            }
            else {
                bfRefreshProperty = (Properties) ctx.getAdditionalProcessParams()[0];
                BranchPowerAccountRefreshFatomContext.Status = Boolean.TRUE;
            }
            setOutputTags(ctx);

            /* Added for Branch-Power Refresh Report */
            bfRefreshProperty = (Properties) ctx.getAdditionalProcessParams()[0];

            setOutputTags(ctx);
        }
        catch (BankFusionException bfe) {
            throw bfe;
        }
        catch (Exception ex) {
            //throw new BankFusionException(5031, null, logger, null);
            EventsHelper.handleEvent(FinancialEventCodes.E_ERR_OCURED_WHILE_RUNNING_BUNDLE_CHRGE_PROCESS,  new Object[]{}, new HashMap(), env);
        logger.error(ex);
        }
    }

    protected void setOutputTags(AbstractFatomContext ctx1) {

        if (BranchPowerCustomerLimitRefreshFatomContext.Status == Boolean.TRUE) {
            setF_OUT_Message("BranchPowerCustomerLimitRefresh Files are Generated Successfully");
            populateReportParams();
        }
        else {
            setF_OUT_Message("Process Failed");
        }
        setF_OUT_Batch_Status(new Boolean(true));

    }

    private String getProperty(String key) {
        return (String) bfRefreshProperty.get(key);
    }

    protected void populateReportParams() {

        /* Getting input from the BPRefresh.properties,that is used for BranchPowerRefresh report */
        fromBranch = getProperty("FROMBRANCH");
        toBranch = getProperty("TOBRANCH");
        custExtRefFlag = getProperty("CUSTOMER-EXT-REFRESH");
        setFatomOutputTagsForReports();
    }

    protected void setFatomOutputTagsForReports() {
        // TODO Auto-generated method stub

        /* Setting output tags of BranchPowerRefreshFatom,that is used for BranchPowerRefresh report */

        setF_OUT_FROMBRANCH(fromBranch);
        setF_OUT_TOBRANCH(toBranch);
        setF_OUT_CUSTOMEREXTREFRESH(custExtRefFlag);
    }
}
