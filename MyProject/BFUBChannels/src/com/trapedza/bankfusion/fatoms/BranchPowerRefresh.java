package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.fatoms.batch.bpRefresh.BranchPowerRefreshFatomContext;
import com.misys.ub.financial.events.FinancialEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractBranchPowerRefresh;

public class BranchPowerRefresh extends AbstractBranchPowerRefresh {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/*Parameter's for Reports - Start*/
	static Properties fileProp = null;
	String fromBranch = CommonConstants.EMPTY_STRING;
	String toBranch = CommonConstants.EMPTY_STRING;
	String custRefFlag = CommonConstants.EMPTY_STRING;
	String accRefFlag = CommonConstants.EMPTY_STRING;
	String custExtRefFlag = CommonConstants.EMPTY_STRING;
	String stopChequeRefFlag = CommonConstants.EMPTY_STRING;
	String accBundleFlag = CommonConstants.EMPTY_STRING;
	String transBundleFlag = CommonConstants.EMPTY_STRING;
	/*Parameter's for Reports - End */
	Properties bfRefreshProperty = null;

	private transient final static Log logger = LogFactory.getLog(BranchPowerRefresh.class.getName());

	public BranchPowerRefresh(BankFusionEnvironment env) {
		super(env);
	}

	private static final String BATCH_PROCESS_NAME = "BranchPowerRefresh";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.trapedza.bankfusion.batch.fatom.AbstractBatchFatom#getFatomContext()
	 */
	protected AbstractFatomContext getFatomContext() {
		return (new BranchPowerRefreshFatomContext(BATCH_PROCESS_NAME));

	}

	public void processBatch(BankFusionEnvironment env, AbstractFatomContext ctx) {
		try {
			logger.debug("BranchPowerRefresh");
			Map outputDataMap = ctx.getOutputTagDataMap();
			ctx.setOutputTagDataMap(outputDataMap);
			if (logger.isDebugEnabled()) {
				logger.debug("process(): starting service");
			}
			BatchService service = (BatchService) ServiceManager.getService(ServiceManager.BATCH_SERVICE);
			service.runBatch(env, ctx);

			/* Added for Branch-Power Refresh Report */
			bfRefreshProperty = (Properties) ctx.getAdditionalProcessParams()[0];

			setOutputTags(ctx);
		}
		catch (BankFusionException bfe) {
			throw bfe;
		}
		catch (Exception ex) {
			//throw new BankFusionException(5031, null, logger, null);
			EventsHelper.handleEvent(FinancialEventCodes.E_ERR_OCURED_WHILE_RUNNING_BUNDLE_CHRGE_PROCESS, new Object[]{}, new HashMap(), env);
		logger.error(ex);
		}
	}

	protected void setOutputTags(AbstractFatomContext ctx1) {

		if (BranchPowerRefreshFatomContext.Status == Boolean.TRUE) {
			setF_OUT_Message("BranchPowerRefresh Files are Generated Successfully");
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

		/* Getting input from the BPRefresh.properties,that is used for BranchPowerRefresh report*/
		fromBranch = getProperty("FROMBRANCH");
		toBranch = getProperty("TOBRANCH");
		custRefFlag = getProperty("CUSTOMER-REFRESH");
		accRefFlag = getProperty("ACCOUNT-REFRESH");
		custExtRefFlag = getProperty("CUSTOMER-EXT-REFRESH");
		stopChequeRefFlag = getProperty("STOP-CHEQUE-REFRESH");
		accBundleFlag = getProperty("ACCOUNT-BUNDLE-REFRESH");
		transBundleFlag = getProperty("TRANS-BUNDLE-REFRESH");
		setFatomOutputTagsForReports();
	}

	protected void setFatomOutputTagsForReports() {
		// TODO Auto-generated method stub

		/* Setting output tags of BranchPowerRefreshFatom,that is used for BranchPowerRefresh report*/

		setF_OUT_FROMBRANCH(fromBranch);
		setF_OUT_TOBRANCH(toBranch);
		setF_OUT_CUSTOMERREFRESH(custRefFlag);
		setF_OUT_ACCOUNTREFRESH(accRefFlag);
		setF_OUT_CUSTEXTREFRESH(custExtRefFlag);
		setF_OUT_STOPCHQREFRESH(stopChequeRefFlag);
		setF_OUT_ACCOUNTBUNDLEREFRESH(accBundleFlag);
		setF_OUT_TXNBUNDLEREFRESH(transBundleFlag);
	}
}
