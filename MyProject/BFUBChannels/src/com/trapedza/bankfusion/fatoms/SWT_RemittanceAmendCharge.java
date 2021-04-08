package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import bf.com.misys.ub.types.interfaces.SwiftMT103;
import bf.com.misys.ub.types.remittanceprocess.CHARGERELATEDINFO;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_RemittanceAmendCharge;

public class SWT_RemittanceAmendCharge extends AbstractSWT_RemittanceAmendCharge{


	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public SWT_RemittanceAmendCharge(BankFusionEnvironment env) {
		super(env);
	}

	public SWT_RemittanceAmendCharge() {
	}

public void process(BankFusionEnvironment env) throws BankFusionException {
	
	
	CHARGERELATEDINFO chargeDetails = new CHARGERELATEDINFO();
	
	chargeDetails = getChargeDetails(getF_IN_CreditAccount(), getF_IN_DetailsOfCharges(), getF_IN_InterbankSettlAmt(), getF_IN_IntrBankSettlAmtCurrency(), getF_IN_ReceiversCharges());
	
	BigDecimal creditAmt = getF_IN_InterbankSettlAmt().subtract(getF_IN_ReceiversCharges());
	setF_OUT_CreditAmount(creditAmt);
	
	setF_OUT_ExpCreditAmount(creditAmt.multiply(getF_IN_ExchangeRate()).setScale(2,BigDecimal.ROUND_HALF_UP));
	
	
	
	setF_OUT_TaxAmt(getF_IN_ConsolidatedTaxAmount());
	setF_OUT_ChargeAmt(getF_IN_ConsolidatedChargeAmount());
	
	if(!getF_IN_DetailsOfCharges().equals("OUR")){
		setF_OUT_IntrBnSettldAmount(getF_IN_InterbankSettlAmt().subtract(getF_IN_ConsolidatedTaxAmount().add(getF_IN_ConsolidatedChargeAmount())));
		setF_OUT_transationAmt(getF_IN_InterbankSettlAmt());
	}
	else{
		setF_OUT_IntrBnSettldAmount(getF_IN_InterbankSettlAmt().subtract(getF_IN_ConsolidatedTaxAmount().add(getF_IN_ConsolidatedChargeAmount())));
		setF_OUT_transationAmt(getF_IN_InterbankSettlAmt().subtract(getF_IN_ConsolidatedTaxAmount().add(getF_IN_ConsolidatedChargeAmount())));
	}
	
//	setF_OUT_ExpCreditAmount(getF_IN_InterbankSettlAmt().subtract(chargeDetails.getChargeAmount()).multiply(getF_IN_ExchangeRate()));
//	setF_OUT_CreditAmount(getF_IN_InterbankSettlAmt().subtract(chargeDetails.getChargeAmount()));
	setF_OUT_ChargeCalCode(chargeDetails.getChargeCalculationCode());
	setF_OUT_ChargeReciAcc(chargeDetails.getChargeReceivingAccount());
	setF_OUT_ChargeCode(chargeDetails.getChargeCode());
	setF_OUT_ReceiversCharge(chargeDetails.getReceiversCharge());
	setF_OUT_SerdersCharge(chargeDetails.getSendersCharge());
	
	setF_OUT_TaxCode(chargeDetails.getTaxCode());
	setF_OUT_TaxNarrative(chargeDetails.getTaxNarrative());
	setF_OUT_TransactionCode(chargeDetails.getTransactionCode());
}
	

public CHARGERELATEDINFO getChargeDetails(String CreditAccount, String DetailsOfCharge, BigDecimal InterBankSettledAmount, String InterBankSettledAmountCurrency, BigDecimal ReceiversCharges){
	CHARGERELATEDINFO chargeDetails = new CHARGERELATEDINFO();
	HashMap inputMap = new HashMap();
	HashMap OutputMap = new HashMap();
	inputMap.put("CreditAccount",CreditAccount);
	inputMap.put("DetailsOfCharge", DetailsOfCharge);
	inputMap.put("InterBankSettledAmount", InterBankSettledAmount);
	inputMap.put("InterBankSettledAmountCurrency",InterBankSettledAmountCurrency );
	inputMap.put("ReceiversCharge", ReceiversCharges);
	
	OutputMap = MFExecuter.executeMF("UB_SWT_IncomingAmountCalculation_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
	//BigDecimal ChargeAmount = new BigDecimal(OutputMap.get("ChargeAmount").toString()); ; // = new BigDecimal();
	//ChargeAmount= 1000;
	if(OutputMap.get("ChargeAmount") != null){
	BigDecimal chargeAmount = new BigDecimal(OutputMap.get("ChargeAmount").toString());
	chargeDetails.setChargeAmount(chargeAmount);
	}
	if(OutputMap.get("TransactionAmount") != null){
	BigDecimal TransactionAmount = new BigDecimal(OutputMap.get("TransactionAmount").toString());
	chargeDetails.setTransactionAmount(TransactionAmount);
	}
	if(OutputMap.get("InterBankSettledAmount") != null){
	BigDecimal InterBankSettledAmt = new BigDecimal(OutputMap.get("InterBankSettledAmount").toString());
	chargeDetails.setInterBankSettledAmount(InterBankSettledAmt);
	}
	if(OutputMap.get("ReceiversCharge") != null){
	BigDecimal ReceiversCharge = new BigDecimal(OutputMap.get("ReceiversCharge").toString());
	chargeDetails.setReceiversCharge(ReceiversCharge);
	}
	if(OutputMap.get("SendersCharge") != null){
	BigDecimal SendersCharge = new BigDecimal(OutputMap.get("SendersCharge").toString());
	chargeDetails.setSendersCharge(SendersCharge);
	}
	if(OutputMap.get("taxAmount") != null){
	BigDecimal TaxAmount = new BigDecimal(OutputMap.get("taxAmount").toString());
	chargeDetails.setTaxAmount(TaxAmount);
	}
	if(OutputMap.get("ChargeCalculationCode") != null){
	String ChargeCalculationCode = (String)OutputMap.get("ChargeCalculationCode");
	chargeDetails.setChargeCalculationCode(ChargeCalculationCode);
	}
	if(OutputMap.get("ChargeCode") != null){
	String ChargeCode = (String)OutputMap.get("ChargeCode");
	chargeDetails.setChargeCode(ChargeCode);
	}
	if(OutputMap.get("ChargeReceivingAccount") != null){
	String ChargeReceivingAccount = (String)OutputMap.get("ChargeReceivingAccount");
	chargeDetails.setChargeReceivingAccount(ChargeReceivingAccount);
	}
	if(OutputMap.get("TaxCode") != null){
	String TaxCode = (String)OutputMap.get("TaxCode");
	chargeDetails.setTaxCode(TaxCode);
	}
	if(OutputMap.get("TaxNarrative") != null){		
	String TaxNarrative = (String)OutputMap.get("TaxNarrative");
	chargeDetails.setTaxNarrative(TaxNarrative);
	}
	if(OutputMap.get("TaxReceivingAccount") != null){
	String TaxReceivingAccount = (String)OutputMap.get("TaxReceivingAccount");
	chargeDetails.setTaxReceivingAccount(TaxReceivingAccount);
	}
	if(OutputMap.get("TransactionCode") != null){
	String TransactionCode = (String)OutputMap.get("TransactionCode");
	chargeDetails.setTransactionCode(TransactionCode);
	}	
	return chargeDetails;
}


}	





