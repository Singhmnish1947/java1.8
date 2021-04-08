package com.misys.ub.fatoms.batch.bpRefresh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.IBatchPostProcess;
import com.trapedza.bankfusion.batch.process.PostProcessException;
import com.trapedza.bankfusion.batch.process.engine.IBatchStatus;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * ForexDealsPnLPostProcess will get accumulated totals and post to relevant accounts.
 */
public class BranchPowerRefreshPostProcess implements IBatchPostProcess {
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final transient Log logger = LogFactory.getLog(BranchPowerRefreshPostProcess.class.getName());

	private BankFusionEnvironment env;
	private AbstractFatomContext context;
	private IBatchStatus status;

	/**
	 * @param environment
	 *            Used to get a handle on the BankFusion environment
	 * @param context
	 *            A set of data passed to the PreProcess, Process and PostProcess classes
	 * @see com.trapedza.bankfusion.batch.process.IBatchPostProcess#
	 *      init(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment,
	 *      com.trapedza.bankfusion.batch.fatom.AbstractFatomContext)
	 */
	public void init(BankFusionEnvironment environment, AbstractFatomContext context) {
		env = environment;
	}

	/**
	 * Writes the results to the database with no business processing
	 *
	 * @param accumulator
	 *            The accumulator
	 * @
	 *             Thrown if a BankFusionFusionException, ServiceException or ErrorOnCommitException is caught
	 * @see com.trapedza.bankfusion.batch.process.IBatchPostProcess#
	 *      process(com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator)
	 */
	public IBatchStatus process(AbstractProcessAccumulator accumulator) {

		//   Passing the EOD status

		status.setStatus(true);
		return status;

	}

	public void init(BankFusionEnvironment env, AbstractFatomContext ctx, IBatchStatus status) {
		this.env = env;
		this.context = ctx;
		this.status = status;

	}

}
