package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.serviceinvocation.IUserExitInvokerService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_RemittancePrintAdvice;

import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;
import bf.com.misys.ub.types.swtremittanceexchangerate.RemittanceExchangeRate;

public class SWT_RemittancePrintAdvice extends AbstractSWT_RemittancePrintAdvice{



	public SWT_RemittancePrintAdvice() {
	}
	public SWT_RemittancePrintAdvice(BankFusionEnvironment env) {
		super(env);
	}
	private transient final static Log logger = LogFactory.getLog(SWT_RemittancePrintAdvice.class.getName());
	public void process(BankFusionEnvironment env) throws BankFusionException {


		ArrayList<UB_SWT_RemittanceProcessRq> params = new ArrayList<UB_SWT_RemittanceProcessRq>();
		
		params.add(getF_IN_remittanceInfo());
		IUserExitInvokerService userExitInvokerService = (IUserExitInvokerService) ServiceManagerFactory.getInstance()
				.getServiceManager().getServiceForName(IUserExitInvokerService.SERVICE_NAME);
		Object response = null;
		if(userExitInvokerService.isValidBeanId("swtRemittanceAdvicePrinting")){
			response = userExitInvokerService.invokeService("swtRemittanceAdvicePrinting", params);
			logger.info("SWT_RemittancePrintAdvice User Exit sucessfully executed");
		} else{
			logger.info("SWT_RemittancePrintAdvice User Exit not configured");
		}
	}



}
