package com.trapedza.bankfusion.fatoms;

import java.util.List;

import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GetBeneficiaryAccFromMsg;

public class GetBeneficiaryAccFromMsg extends
		AbstractUB_SWT_GetBeneficiaryAccFromMsg {
	@SuppressWarnings({ "deprecation", "unchecked" })
	public GetBeneficiaryAccFromMsg(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		String account = getF_IN_accountFromMsg();
		String accountMsg;
		List AccountList = FinderMethods.findAccountInfoByAccountID(account,
				env, null);
		IBOAttributeCollectionFeature accountInfo = (IBOAttributeCollectionFeature) AccountList
				.get(0);
		accountMsg = accountInfo.getBoID();
		setF_OUT_actualAccount(accountMsg);
	}
}
