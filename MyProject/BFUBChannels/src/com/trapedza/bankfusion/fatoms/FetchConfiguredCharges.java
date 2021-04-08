/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.systeminformation.BusinessInformation;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractFetchConfiguredCharges;

import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.Charge;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.ExchangeRateDetails;

/**
 * @author summahto
 *
 */
public class FetchConfiguredCharges extends AbstractFetchConfiguredCharges{
	
	private transient final static Log logger = LogFactory.getLog(FetchConfiguredCharges.class.getName());

    public FetchConfiguredCharges(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
    	
    	VectorTable chargeVector = new VectorTable();
    	chargeVector = getF_IN_ChargeVector();
    	String detailsOfCharge = getF_IN_DetailsOfCharge();
    	String debitAccount = getF_IN_DebitAccount();
    	String creditAccount = getF_IN_CreditAccount();
    	String sendersBIC = getF_IN_SendersBIC();
    	String txnCurrency = getF_IN_TxnCurrencyCode();
    	BigDecimal receiversCharge = getF_IN_ReceiversCharge();
    	BigDecimal interBankSettlementAmt = getF_IN_InterbankSettledAmount();
    	
    	BigDecimal ourChargeConfigAmount = BigDecimal.ZERO;
    	BigDecimal actualChargeAmount = BigDecimal.ZERO;
    	BigDecimal transactionAmount = interBankSettlementAmt;
    	String chargeCode = StringUtils.EMPTY ;
    	String chargeDebitAccount = StringUtils.EMPTY;
    	String errorCode = StringUtils.EMPTY;
    	
    	//OUR Charge Details
    	
    	BigDecimal ourTaxAmount = BigDecimal.ZERO;
    	BigDecimal totalCharge = BigDecimal.ZERO;
    	int countForOurCharge = 0;
    	
		String ourChargeCode = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.MODULE_ID,
				PaymentSwiftConstants.CHARGE_CODE_USED_FOR_OUR_71G_CHARGE_POSTING);
				
		
		if(!ourChargeCode.equals(StringUtils.EMPTY) && ourChargeCode != null) {
		  	
        for(int i=0; i < chargeVector.size(); i++) {
        	HashMap chargeMap = chargeVector.getRowTags(i);
        	chargeCode = (String)chargeMap.get("CHARGECODE");
        	    		
        		if (chargeCode.equals(ourChargeCode)) {
        			countForOurCharge++;
	        		ourChargeConfigAmount = (BigDecimal) chargeMap.get("CHARGEAMOUNT");
	        		ourTaxAmount = (BigDecimal) chargeMap.get("TAXAMOUNT") != null ? (BigDecimal) chargeMap.get("TAXAMOUNT") : BigDecimal.ZERO;
	        		BigDecimal taxPercent = (ourTaxAmount.multiply(BigDecimal.valueOf(100.00))).divide(ourChargeConfigAmount);
	        		BigDecimal taxAmt = (receiversCharge.multiply(taxPercent)).divide(taxPercent.add(BigDecimal.valueOf(100.00)));
	        		
	        		
	        		BigDecimal finalChargeAmt = receiversCharge.subtract(taxAmt);
	        		actualChargeAmount = finalChargeAmt; 
	        		totalCharge = totalCharge.add(actualChargeAmount);
	        		chargeMap.put("CHARGEAMOUNT", actualChargeAmount);
	        		chargeMap.put("TAXAMOUNT", taxAmt);
	        		transactionAmount = interBankSettlementAmt.subtract(receiversCharge);
	        		interBankSettlementAmt = transactionAmount;
	        		
	        		chargeDebitAccount = findChargeDebitAccount(creditAccount,debitAccount,
	        				detailsOfCharge,receiversCharge,sendersBIC,txnCurrency);
	        		        		
	        		chargeMap.put("FUNDINGACCOUNTID", chargeDebitAccount);
	        		chargeVector.populateRow(chargeMap, i);
	        		
        		}
        	else {

        			totalCharge = totalCharge.add((BigDecimal) chargeMap.get("CHARGEAMOUNT_IN_TXN_CURRENCY"));
            	    interBankSettlementAmt = transactionAmount;
            	   
            	    chargeVector.populateRow(chargeMap, i);
        		}        
        		errorCode = validateAccount(chargeDebitAccount);
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
	}else {
		
		errorCode = "09718";
	}
		
		if (countForOurCharge == 0) {
			errorCode = "09718";
		}
        
        setF_OUT_FinalChargeVector(chargeVector);
        setF_OUT_TransactionAmount(transactionAmount);
        setF_OUT_InterbankSettledAmount(interBankSettlementAmt);
        setF_OUT_FinalChargeVector_NOOFROWS(chargeVector.size());
        setF_OUT_ChargeAmount(totalCharge);
        setF_OUT_ErrorNumber(errorCode);
        
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

	private String findChargeDebitAccount(String creditAccount, String debitAccount, String detailsOfCharge,
			BigDecimal receiversCharge, String sendersBIC, String txnCurrency) {
		HashMap inputMap = new HashMap();
		HashMap OutputMap = new HashMap();
		
		String chargeDebitAccount = StringUtils.EMPTY;
		inputMap.put("CreditAccount", creditAccount);
		inputMap.put("DebitAccount", debitAccount);
		inputMap.put("DetailsOfCharge", detailsOfCharge);
		inputMap.put("SendersBIC", sendersBIC);
		inputMap.put("ReceiversCharge", receiversCharge);
		inputMap.put("TxnCurrencyCode", txnCurrency);
		
		OutputMap = MFExecuter.executeMF("UB_SWT_IncomingFindChargeDebitAccount_SRV",
		        				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		if(OutputMap.get("ChargeDebitAccount") != null) {
			chargeDebitAccount = (String) OutputMap.get("ChargeDebitAccount");
			}
		
		return chargeDebitAccount;
	}
    	
}


