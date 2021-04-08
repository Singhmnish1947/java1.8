package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import bf.com.misys.ub.types.swtremittanceexchangerate.*;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.serviceinvocation.IUserExitInvokerService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_RemittanceExternalExchangeRates;

public class SearchExternalExchangeRates extends
AbstractUB_SWT_RemittanceExternalExchangeRates {



	public SearchExternalExchangeRates(BankFusionEnvironment env) {
		super(env);
	}

	public SearchExternalExchangeRates() {
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		RemittanceExchangeRate remittanceExchangeRate = getF_IN_RemittanceExchangeRateRq();
		RemittanceExchangeRateList remittanceExchangeRateList = new RemittanceExchangeRateList();
		IUserExitInvokerService userExitInvokerService = (IUserExitInvokerService) ServiceManagerFactory.getInstance()
				.getServiceManager().getServiceForName(IUserExitInvokerService.SERVICE_NAME);
		ArrayList<RemittanceExchangeRate> params = new ArrayList<RemittanceExchangeRate>();
		params.add(remittanceExchangeRate);
		Object response = null;
		if(userExitInvokerService.isValidBeanId("searchExternalExchangeRate")){
			response = userExitInvokerService.invokeService("searchExternalExchangeRate", params);
			remittanceExchangeRateList = (RemittanceExchangeRateList)response;
		} else{
			//No User Exit Implementation
		}
		setF_OUT_RemittanceExchangeRateListRs(remittanceExchangeRateList);
		
	}

}
