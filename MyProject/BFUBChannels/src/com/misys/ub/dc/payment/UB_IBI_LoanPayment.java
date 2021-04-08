package com.misys.ub.dc.payment;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.financialposting.types.BackOfficeAccountPostingRq;
import bf.com.misys.financialposting.types.PostingLeg;
import bf.com.misys.financialposting.types.TxnDetails;

import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

public class UB_IBI_LoanPayment {
	private static final transient Log LOGGER = LogFactory
			.getLog(UB_IBI_LoanPayment.class.getName());
	
    public static void loanPosting(BigDecimal txnAmount, String debitAccountId, String creditAccountId, String db_AccountName, String cr_AccountName, String debitTxnCode,
            String creditTxnCode,String debitNarrative, String creditNarrative, String txnReference, String transactionId, BigDecimal txnInDebitAmt, BigDecimal txnInCreditAmt,
            String txnCurrency,  String exchangeRateType, String debitBranchSortCode,
            String channelId){
    		LOGGER.info("Inside loanPosting method");
            HashMap<String, Object> inputParams = new HashMap<String, Object>();
            BackOfficeAccountPostingRq accountPostingRq = new BackOfficeAccountPostingRq();
            PostingLeg[] backOfficePostingLegs = new PostingLeg[2];
            TxnDetails txndetails = null;
            Timestamp valueDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
            
            backOfficePostingLegs[0] =
                UB_IBI_PaymentsHelper.getBackOfficePostingLeg(creditAccountId, txnCurrency, txnAmount, "C", creditTxnCode, creditNarrative,
                    exchangeRateType, txnInCreditAmt);
            backOfficePostingLegs[1] =
                UB_IBI_PaymentsHelper.getBackOfficePostingLeg(debitAccountId, txnCurrency, txnAmount, "D", debitTxnCode, debitNarrative,
                    exchangeRateType, txnInDebitAmt);
            txndetails = UB_IBI_PaymentsHelper.getTxnDetails(transactionId, txnReference, valueDate, debitBranchSortCode, channelId);

            accountPostingRq.setBackOfficePostingLegs(backOfficePostingLegs);
            accountPostingRq.setTxnDetails(txndetails);
            accountPostingRq.setSrvVersion(CommonConstants.EMPTY_STRING);

            inputParams.put(IfmConstants.LOAN_REPYMENT_RQ, accountPostingRq);
            LOGGER.info("Calling UB_R_UB_TXN_BackOfficeAccountPosting_SRV - Microflow");
            MFExecuter.executeMF(IfmConstants.LOAN_POSTING_ENGINE, BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
            
    
    }
    

}
