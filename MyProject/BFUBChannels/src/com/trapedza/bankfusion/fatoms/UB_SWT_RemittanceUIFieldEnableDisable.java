package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_RemittanceUIFieldEnableDisable;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;

public class UB_SWT_RemittanceUIFieldEnableDisable extends AbstractUB_SWT_RemittanceUIFieldEnableDisable {

	public UB_SWT_RemittanceUIFieldEnableDisable(BankFusionEnvironment env) {
		super(env);
	}
	public UB_SWT_RemittanceUIFieldEnableDisable(){
	}
	
	public void process(BankFusionEnvironment env) throws BankFusionException {

	String payto = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_PAYTO_GRID", "true");
	setF_OUT_setPayTo(new Boolean(payto));
	
	String Intemediary = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_INTERMEDIARY_GRID", "true");
	setF_OUT_setIntermediary(new Boolean(Intemediary));
	
	String BeneCustomer = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_BENEFICARYCUSTOMER_GRID", "true");
	setF_OUT_setBeneCustomer(new Boolean(BeneCustomer));
	
	String BeneInstitute = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_BENEFICARYINSTITUTE_GRID", "true");
	setF_OUT_setBeneInstitute(new Boolean(BeneInstitute));
	
	String OrderingCust = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_ORDERINGCUSTOMER_GRID", "true");
	setF_OUT_setOrderingCust(new Boolean(OrderingCust));
	
	String OrderingInst = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_ORDERINGINSTITUTE_GRID", "true");
	setF_OUT_setOrderingInst(new Boolean(OrderingInst));
	
	String banktoBankInfo = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_BANKTOBANKINFO_GRID", "true");
	setF_OUT_setBankToBankInfo(new Boolean(banktoBankInfo));
	
	String sendrToRecInfo = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_SENDERTORECEIVERINFO_GRID", "true");
	setF_OUT_setSendrToRecevrInfo(new Boolean(sendrToRecInfo));
	
	String remittanceInfo = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_REMITTANCEINFO_GRID", "true");
	setF_OUT_setRemittanceInfo(new Boolean(remittanceInfo));
	
	String bankOpertnCode = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_BANKOPERATIONCODE_GRID", "true");
	setF_OUT_setBankOperntCode(new Boolean(bankOpertnCode));
	
	String bankInstrcCode = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_BANKINSTRUCTIONCODE_GRID", "true");
	setF_OUT_setBankInstrctCode(new Boolean(bankInstrcCode));
	
	String regulatoryInfo = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_REGULATORYINFO_GRID", "true");
	setF_OUT_setRegulatoryInfo(new Boolean(regulatoryInfo));
	
	String chargeCode 	  = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_CHARGE_CODE_TYPE", "true");
	setF_OUT_setchargeCode(new Boolean(chargeCode));
	
	String chargeCodetype = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_TRANSACTIONTYPECODE_GRID", "true");
	setF_OUT_setchargeCodeType(new Boolean(chargeCodetype));
	
	String termAndCondition = BankFusionPropertySupport.getProperty("REMMITANCESCREEN.ENABLE.DISABLE_TERMANDCONDITION_GRID", "true");
	//setF_OUT_set

		
	
	}
	
	
}
