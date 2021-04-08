package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.util.logging.resources.logging;

import bf.com.misys.cbs.types.header.RsHeader;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TXN_CompensateTransaction;

public class UB_TXN_CompensateTransaction extends
		AbstractUB_TXN_CompensateTransaction {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/** */
	private static final long serialVersionUID = -2747463279771529245L;
	private BankFusionEnvironment environment;
	private static final String INPUTTAG_TRANSACTIONID = "TransactionID";
	private static final String INPUTTAG_TRANSACTIONREFERENCE = "TransactionReference";
	private static final String REVERSETRANSACTIONMF = "UB_TXN_ReverseTransaction_SRV";
	private static final String OUTTAG_REHEADER = "RsHeader";
	private static final String OUTTAG_TRANSACTIONID = "TransactionID";
	private static final String INVAID_TXN_ID = "Invalid Transaction ID";
	private static final String INPUTTAG_FORCEDPOST = "ForcedPost";
	private static final String SUCCESSSTATUS = "S";
	private static final String FAILURESTATUS = "E";
	public static final String finanWhrClause = " WHERE " + IBOFinancialPostingMessage.TRANSACTIONREF + " = ?";

	private transient final static Log LOG = LogFactory
			.getLog(UB_TXN_CompensateTransaction.class.getName());
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal
			.getPersistanceFactory();

	public static final String trnsnWhrClause = " WHERE "
			+ IBOTransaction.REFERENCE + " = ?";
	public static final String deleteTrnsnWhrClause = " WHERE "
			+ IBOTransaction.TRANSACTIONID + " = ?";

	public UB_TXN_CompensateTransaction() {
		super();
	}

	public UB_TXN_CompensateTransaction(BankFusionEnvironment env) {
		super();
	}

	public void process(BankFusionEnvironment env) {

		this.environment = env;

		String transID = CommonConstants.EMPTY_STRING;
		String reference = getF_IN_OriginalTransactionReference();
		RsHeader rsheader = new RsHeader();
		String transactionID = CommonConstants.EMPTY_STRING;
		List finposmsgList = null;
		ArrayList finamsgParams = new ArrayList();


		// Transaction id is NULL
		if (reference == null || reference.length() == 0) {

			rsheader.getStatus().setOverallStatus(FAILURESTATUS);
			setF_OUT_RsHeader(rsheader);
				LOG.error(INVAID_TXN_ID);

			return;
		}

		ArrayList transactionParams = new ArrayList();
		transactionParams.add(reference);
		List listoftransactions = null;
		String narrative = CommonConstants.EMPTY_STRING;
		try {
			listoftransactions = factory.findByQuery(IBOTransaction.BONAME,
					trnsnWhrClause, transactionParams, null, true);  

		} catch (BankFusionException bfe) {
			LOG.error(ExceptionUtil.getExceptionAsString(bfe));
			// set flag to failure

		}

		if (listoftransactions!= null && listoftransactions.size() > 0) {
			IBOTransaction transactionItem = (IBOTransaction) listoftransactions
					.get(0);
			transactionID = transactionItem.getF_TRANSACTIONID();

		} else {
			finamsgParams.add(reference);
			finposmsgList = environment.getFactory().findByQuery(IBOFinancialPostingMessage.BONAME, finanWhrClause, finamsgParams,
					null, true);
				if (finposmsgList!= null && finposmsgList.size() > 0) {
					IBOFinancialPostingMessage finpostingMessages = (IBOFinancialPostingMessage) finposmsgList.get(0);
					transactionID = finpostingMessages.getF_TRANSACTIONID();
				}
		}
		
		if(!transactionID.isEmpty()){
			
			HashMap inputParamsMap = new HashMap();
			inputParamsMap.put(INPUTTAG_TRANSACTIONID, transactionID);
			inputParamsMap.put(INPUTTAG_TRANSACTIONREFERENCE, reference);
			inputParamsMap.put(INPUTTAG_FORCEDPOST, true);

			HashMap mfOutput = MFExecuter.executeMF(
					REVERSETRANSACTIONMF, environment,
					inputParamsMap);

			rsheader = (RsHeader) mfOutput.get(OUTTAG_REHEADER);
			transID = (String) mfOutput.get(OUT_TransactionID);
		} else{
			// set flag to Success
			rsheader.getStatus().setOverallStatus(SUCCESSSTATUS);
		}


		setF_OUT_RsHeader(rsheader);
		setF_OUT_TransactionID(transID);
	}

}
