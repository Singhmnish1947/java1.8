package com.trapedza.bankfusion.fatoms;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ValidateBankToBankInfo;

public class UB_SWT_ValidateBankToBankInfo extends AbstractUB_SWT_ValidateBankToBankInfo{
	
	
	private transient final static Log logger = LogFactory.getLog(UB_SWT_ValidateBankToBankInfo.class.getClass());
	public UB_SWT_ValidateBankToBankInfo(BankFusionEnvironment env) {
		super(env);
	}

	public UB_SWT_ValidateBankToBankInfo() {
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		Boolean validationSuccess = true;
		String bankToBankInfo1 = getF_IN_remittanceProcessRq().getBANKTOBANKINFO().getBANKTOBANKINFO1();
		String bankToBankInfo2 = getF_IN_remittanceProcessRq().getBANKTOBANKINFO().getBANKTOBANKINFO2();
		String bankToBankInfo3 = getF_IN_remittanceProcessRq().getBANKTOBANKINFO().getBANKTOBANKINFO3();
		String bankToBankInfo4 = getF_IN_remittanceProcessRq().getBANKTOBANKINFO().getBANKTOBANKINFO4();
		String bankToBankInfo5 = getF_IN_remittanceProcessRq().getBANKTOBANKINFO().getBANKTOBANKINFO5();
		String bankToBankInfo6 = getF_IN_remittanceProcessRq().getBANKTOBANKINFO().getBANKTOBANKINFO6();
		
		if(null!=bankToBankInfo1 && (bankToBankInfo1.contains("/n") || bankToBankInfo1.contains("//n"))) {
			validationSuccess = false;
		}
		if(null!=bankToBankInfo2 && (bankToBankInfo2.contains("/n") || bankToBankInfo2.contains("//n"))) {
			validationSuccess = false;
		}
		if(null!=bankToBankInfo3 && (bankToBankInfo3.contains("/n") || bankToBankInfo3.contains("//n"))) {
			validationSuccess = false;
		}
		if(null!=bankToBankInfo4 && (bankToBankInfo4.contains("/n") || bankToBankInfo4.contains("//n"))) {
			validationSuccess = false;
		}
		if(null!=bankToBankInfo5 && (bankToBankInfo5.contains("/n") || bankToBankInfo5.contains("//n"))) {
			validationSuccess = false;
		}
		if(null!=bankToBankInfo6 && (bankToBankInfo6.contains("/n") || bankToBankInfo6.contains("//n"))) {
			validationSuccess = false;
		}
		
		if(!validationSuccess) {
			EventsHelper.handleEvent(40507113, new String[] {"Bank to Bank Info cannot have /n or //n"}, null, env);
		}
		
		
		
		
	}
	
	private String checkNullValue(String str) {
		String output = StringUtils.EMPTY;
		if (str != null && !str.isEmpty()) {
			output = str;
		}
		return output;
	}

}
