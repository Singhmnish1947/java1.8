package com.finastra.fbe.atm.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.eventcode.CommonEventCodes;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.misys.ub.fatoms.batch.EODConstants;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.fatom.AbstractPersistableFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.bo.refimpl.IBOPosOperationDetailsTag;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.exceptions.RetriableException;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

public class OfflinePosCompletionProcessor extends AbstractBatchProcess {

	public OfflinePosCompletionProcessor(AbstractPersistableFatomContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	String WHERE_CLAUSE = "WHERE UBROWSEQ BETWEEN ? AND ? AND UBPROCESSSTATE IS NULL OR UBPROCESSSTATE = ?";

	private static final Log LOGGER = LogFactory.getLog(OfflinePosCompletionProcessor.class.getName());

	private OfflinePosCompletionAccumulator accumulator;

	public OfflinePosCompletionProcessor(BankFusionEnvironment environment, AbstractFatomContext context,
			Integer priority) {
		super(environment, context, priority);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractProcessAccumulator getAccumulator() {
		// TODO Auto-generated method stub
		return accumulator;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		initialiseAccumulator();
	}

	@Override
	protected void initialiseAccumulator() {
		// TODO Auto-generated method stub
		Object[] accumulatorArgs = new Object[1];
		accumulatorArgs[0] = null;
		accumulator = new OfflinePosCompletionAccumulator(accumulatorArgs);

	}

	@Override
	public AbstractProcessAccumulator process(int pageToProcess) {
		// TODO Auto-generated method stub
		int pageSize = context.getPageSize();
		int fromValue = ((pageToProcess - 1) * pageSize) + 1;
		int toValue = pageToProcess * pageSize;
		LOGGER.info("OfflinePosCompletionProcessor STARTED ::::: from " + fromValue + " to " + toValue);
		ArrayList params = new ArrayList();
		params.add(fromValue);
		params.add(toValue);
		params.add("F");

		IPersistenceObjectsFactory persistanceFactory = BankFusionThreadLocal.getPersistanceFactory();
		List<IBOPosOperationDetailsTag> posOperationlist = persistanceFactory
				.findByQuery(IBOPosOperationDetailsTag.BONAME, WHERE_CLAUSE, params, null, false);

		boolean isAnyExceptionCaught = false;
		if (CommonUtil.checkIfNotNullOrEmpty(posOperationlist)) {
			BankFusionThreadLocal.setCurrentPageRecordIDs(new ArrayList(posOperationlist));
			for (IBOPosOperationDetailsTag posOperationTAg : posOperationlist) {
				try {
					BankFusionThreadLocal.setCurrentRecordID(posOperationTAg.getBoID());
					new PosOperationHelper().unblockPost(posOperationTAg.getBoID());
					// Object[] obj = new Object[1];
					// obj[0] = Integer.valueOf(fromValue);
					// getAccumulator().accumulateTotals(obj);
				} catch (Exception exception) {
					persistanceFactory.rollbackTransaction();
					persistanceFactory.beginTransaction();
					posOperationTAg.setF_PROCESSSTATE("F");
					LOGGER.error("Error occured while processing the record with account "
							+ posOperationTAg.getF_ACCOUNTID() + " with block reference " + posOperationTAg.getBoID()
							+ "\nError message : " + ExceptionUtil.getExceptionAsString(exception));
					updateFailedRecordProcessState(posOperationTAg.getBoID(), exception);
				}
				posOperationTAg.setF_PROCESSSTATE("P");
			}
			LOGGER.info("OfflinePosCompletionProcessor ENDED ::::: from " + fromValue + " to " + toValue);
			if (isAnyExceptionCaught) {
				LOGGER.error(
						"Retriable Exception is thrown intentionally to handle unprocessed records for page number : "
								+ pageToProcess);
				throw new RetriableException(CommonEventCodes.E_STALE_STATE_EXCEPTION, CommonConstants.EMPTY_STRING);
			}
		}

		return accumulator;
	}

	private void updateFailedRecordProcessState(String boId, Exception ex) {
		LOGGER.error(":::::::Updating FailedRecordProcessState:::::");
		IPersistenceObjectsFactory privateFactory = null;
		try {
			IPersistenceService pService = (IPersistenceService) ServiceManagerFactory.getInstance().getServiceManager()
					.getServiceForName(ServiceManager.PERSISTENCE_SERVICE);
			privateFactory = pService.getPrivatePersistenceFactory(false);
			privateFactory.beginTransaction();
			BatchUtil.createLogMessage(boId, ex.getLocalizedMessage(), EODConstants.STATUS_ERROR,
					context.getBatchProcessName(), privateFactory);
			IBOPosOperationDetailsTag posOpTag = (IBOPosOperationDetailsTag) privateFactory
					.findByPrimaryKey(IBOPosOperationDetailsTag.BONAME, boId, false);
			posOpTag.setF_PROCESSSTATE("F");
			privateFactory.commitTransaction();
		} catch (Exception exception) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
			if (privateFactory != null) {
				privateFactory.rollbackTransaction();
			}

		} finally {
			if (privateFactory != null)
				privateFactory.closePrivateSession();
		}
	}

}
