package com.misys.ub.fatoms;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.misys.bankfusion.serviceinvocation.IUserExitInvokerService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMFindCommisionAccount;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.features.PseudoNameFeature;

public class ATMFindCommisionAccount extends AbstractATMFindCommisionAccount {

	public ATMFindCommisionAccount(BankFusionEnvironment env) {
		super(env);
	}

	private static final transient Log logger = LogFactory
			.getLog(ATMFindCommisionAccount.class.getName());

	public void process(BankFusionEnvironment env) throws BankFusionException {
		logger.info("ATMFindCommisionAccount");

		IUserExitInvokerService userExitInvokerService = (IUserExitInvokerService) ServiceManagerFactory
				.getInstance().getServiceManager()
				.getServiceForName(IUserExitInvokerService.SERVICE_NAME);

		if (userExitInvokerService.isValidBeanId("ATMCommissionAccount")) {

			ArrayList params = new ArrayList();
			params.add(getF_IN_UB_ATM_Financial_Details());
			params.add(getF_IN_UB_POS_Financial_Details());
			params.add(getF_IN_CHANNELID());

			Object accountID = userExitInvokerService.invokeService(
					"ATMCommissionAccount", params);
			logger.info("ATMFindCommisionAccount :accountID " + accountID);
			setF_OUT_ACCOUNTID((String) accountID);

		} else {

			PseudoNameFeature pseudoNameFeature = new PseudoNameFeature(env);
			pseudoNameFeature.setF_IN_BRANCH(getF_IN_BRANCH());
			pseudoNameFeature.setF_IN_tryAlternatives(isF_IN_tryAlternatives());
			pseudoNameFeature.setF_IN_context(getF_IN_CONTEXT());
			pseudoNameFeature.setF_IN_PSEUDONAME(getF_IN_PSEUDONAME());
			pseudoNameFeature.setF_IN_stopOnError(isF_IN_stopInError());
			pseudoNameFeature.setF_IN_CURRENCY(getF_IN_CURRENCY());
			pseudoNameFeature.process(env);
			logger.info("ATMFindCommisionAccount :accountID "
					+ getF_OUT_ACCOUNTID());
			setF_OUT_ACCOUNTID(pseudoNameFeature.getF_OUT_ACCOUNTID());

		}

	}
}
