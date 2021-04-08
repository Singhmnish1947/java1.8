package com.misys.ub.fatoms;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.utils.restServices.RetrieveLOBServiceHelper;
import com.misys.ub.utils.types.LineOfBusinessListRq;
import com.misys.ub.utils.types.LineOfBusinessListRs;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractAccountPushEventHandler;

//import com.misys.ub.utils.restServices.RetrieveLOBServiceHelper;

public class AccountPushEventHandler extends AbstractAccountPushEventHandler {
	private static final long serialVersionUID = 7770133426343627728L;
	private static final transient Log logger = LogFactory
			.getLog(AccountPushEventHandler.class.getName());

	public AccountPushEventHandler() {
		super();
	}

	public AccountPushEventHandler(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		logger.info("Start of AccountPushEventHandler");
		String accountNumber = getF_IN_accountNumber();
		if(accountNumber==null || "".equals(accountNumber) )
		accountNumber = getF_IN_ACCOUNTNO();
		String accountOperation = getF_IN_ACCOUNTOPERATION();
		sendLobDetails(accountNumber, accountOperation);
		logger.info("Start of AccountPushEventHandler");
	}

	private void sendLobDetails(String accountNumber, String accountOperation) {
		RetrieveLOBServiceHelper lobListRetriever = new RetrieveLOBServiceHelper();
		LineOfBusinessListRs resp = null;
		LineOfBusinessListRq inputRq = new LineOfBusinessListRq();
		logger.info("Account Number: " + accountNumber);
		inputRq.setAccountId(accountNumber);
		resp = lobListRetriever.fetchLOBList(inputRq);
		ArrayList<String> lobs = resp.getLisOfBusinesses();
		if (accountOperation == null || "".equals(accountOperation)) {
			accountOperation = "U";
		}
		if ( "C".equals(accountOperation)) {
			lobs.remove("TREASURYFO");
		}
		for (String s : lobs) {
			StringBuffer sb = new StringBuffer(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
					.append("<ubintcf:AccAndLobDtlsListRq xmlns:ubintcf=\"http://www.misys.com/ub/types/ubintfc\">")
					.append("<ubintcf:accIdList>").append("<ubintcf:accId>")
					.append(resp.getId()).append("</ubintcf:accId>")
					.append("<ubintcf:accOperation>").append(accountOperation)
					.append("</ubintcf:accOperation>")
					.append("</ubintcf:accIdList>").append("<ubintcf:lob>")
					.append(s).append("</ubintcf:lob>")
					.append("</ubintcf:AccAndLobDtlsListRq>");
			String xmlString = sb.toString();
			logger.info("XML String: " + xmlString);
			MessageProducerUtil
					.sendMessage(xmlString, "ACCOUNT_DETAIL_REQUEST");
		}
	}
}