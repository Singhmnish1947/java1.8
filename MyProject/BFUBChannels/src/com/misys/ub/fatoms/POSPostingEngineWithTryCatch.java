package com.misys.ub.fatoms;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.ErrorEvent;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_POS_PostingEngineWithTryCatch;
/*
 * To catch the exceptions thrown during online POS Blockings
 * 
 */
public class POSPostingEngineWithTryCatch extends AbstractUB_POS_PostingEngineWithTryCatch{
	 /**
     * <code>svnRevision</code> = "$Revision: 1.0 $"
     */
	 public static final String svnRevision = "$Revision: 1.0 $";
	    static {
	        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	    }
	    
	 private static final transient Log logger = LogFactory.getLog(POSPostingEngineWithTryCatch.class.getName());

	 public  POSPostingEngineWithTryCatch(){
		 super(null);
	 }
	 
	 public POSPostingEngineWithTryCatch(BankFusionEnvironment env){
		 super(env);
	 }
	 
	 public void process(BankFusionEnvironment env){
		 
		 HashMap<String, Object> params = new HashMap<String, Object>();
		 params.put("forcePost", isF_IN_forcePost());
		 params.put("postingRequestMessageType", getF_IN_postingRequestMessageType());
		 params.put("bankTransactionCode", getF_IN_bankTransactionCode());
		 params.put("blockingCategory", getF_IN_blockingCategory());
		 params.put("blockingOriginator", getF_IN_blockingOriginator());
		 params.put("blockingType", getF_IN_blockingType());
		 params.put("branchSortcode", getF_IN_branchSortcode());
		 params.put("channelID", getF_IN_channelID());
		 params.put("isBlocking", isF_IN_isBlocking());
		 params.put("manualValueDateTime", getF_IN_manualValueDateTime());
		 params.put("postingMessageAccountPostingAction", getF_IN_postingMessageAccountPostingAction());
		 params.put("postingMessageBlockingAmount", getF_IN_postingMessageBlockingAmount());
		 params.put("postingmessageExchangeRateType", getF_IN_postingmessageExchangeRateType());
		 params.put("postingmessageISOCurrencycode", getF_IN_postingmessageISOCurrencycode());
		 params.put("postingmessageMainAccountID", getF_IN_postingmessageMainAccountID());
		 params.put("postingmessageMainTransactionNarrative", getF_IN_postingmessageMainTransactionNarrative());
		 params.put("postingmessageMainTransactionReference", getF_IN_postingmessageMainTransactionReference());
		 params.put("prefixOrSuffix", getF_IN_prefixOrSuffix());
		 params.put("sameAutoReference", getF_IN_sameAutoReference());
		 params.put("transactionRefPrefixorSuffix", getF_IN_transactionRefPrefixorSuffix());
		 params.put("transactionShortName", getF_IN_transactionShortName());
		 params.put("transactionID", getF_IN_transactionID());
		 params.put("unblockingDateTime", getF_IN_unblockingDateTime());
		 HashMap<String , Object> outputParams = MFExecuter.executeMF("UB_POS_BlockingPosting_SRV", env, params);
		
		}
	 
}
