/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractCalculateTransactionAmount;
import com.trapedza.bankfusion.steps.refimpl.AbstractFetchConfiguredCharges;

/**
 * @author summahto
 *
 */
public class CalculateTransactionAmount extends AbstractCalculateTransactionAmount {
	
	private transient final static Log logger = LogFactory.getLog(CalculateTransactionAmount.class.getName());

    public CalculateTransactionAmount(BankFusionEnvironment env) {
        super(env);
    }
    
public void process(BankFusionEnvironment env) {
    	
    	VectorTable chargeVector = new VectorTable();
    	chargeVector = getF_IN_ChargeVector();
    	VectorTable newChargeVector = new VectorTable();
    	
    	BigDecimal interBankSettlementAmt = getF_IN_InterbankSettledAmount();
    	BigDecimal transactionAmount = interBankSettlementAmt;
    	
    	BigDecimal totalCharge = BigDecimal.ZERO;
    	String errorCode = StringUtils.EMPTY;
    	String chargeCode = StringUtils.EMPTY;
    	
    	String ourChargeCode = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.MODULE_ID,
				PaymentSwiftConstants.CHARGE_CODE_USED_FOR_OUR_71G_CHARGE_POSTING);
    
    	for(int i=0; i < chargeVector.size(); i++) {
    		HashMap chargeMap = chargeVector.getRowTags(i);
    		chargeCode = (String)chargeMap.get("CHARGECODE");
    		
    		if(!ourChargeCode.equals(chargeCode)){
    			newChargeVector.addAll(new VectorTable(chargeMap));
    			totalCharge = totalCharge.add((BigDecimal) chargeMap.get("CHARGEAMOUNT_IN_TXN_CURRENCY"));
    			errorCode = validateAccount((String) chargeMap.get("FUNDINGACCOUNTID"));
        		if(StringUtils.isNotEmpty(errorCode)) {
        			break;
        		}
        		
        		errorCode = validateAccount((String) chargeMap.get("CHARGERECIEVINGACCOUNT"));
        		if(StringUtils.isNotEmpty(errorCode)) {
        			break;
        		}
        		
        		errorCode = validateAccount((String) chargeMap.get("TAXRECIEVINGACCOUNT"));
        		if(StringUtils.isNotEmpty(errorCode)) {
        			break;
        		}
    		
    		}     
    	}
    	interBankSettlementAmt = transactionAmount;
    	
    	setF_OUT_FinalChargeVector(newChargeVector);
        setF_OUT_TransactionAmount(transactionAmount);
        setF_OUT_InterBankSettlementAmount(interBankSettlementAmt);
        setF_OUT_FinalChargeVector_NOOFROWS(chargeVector.size());
        setF_OUT_ChargeAmount(totalCharge);
        setF_OUT_ErrorCode(errorCode);
    	
}	

	private String validateAccount(String accountId) {
		HashMap inputMap = new HashMap();
		HashMap outputMap = new HashMap();
		
		String errorCode = StringUtils.EMPTY;
		inputMap.put("ACCOUNTID", accountId);
		
		outputMap = MFExecuter.executeMF("UB_SWT_AccountValidationForPosting_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		String error = (String) outputMap.get("ERRORNUMBER");
		return errorCode;
	}

}
