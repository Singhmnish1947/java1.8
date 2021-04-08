package com.misys.ub.payment.swift.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.msgs.v1r0.DeleteAccountHoldRq;
import bf.com.misys.cbs.msgs.v1r0.DeleteAccountHoldRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.AcctHoldDeleteDtls;
import bf.com.misys.cbs.types.AcctHoldDeleteFull;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.payment.posting.SWTPostingUtils;

/**
 * @author machamma.devaiah
 *
 */
public class UnblockNonStpTransactions {
	private static final transient Log LOGGER = LogFactory.getLog(UnblockNonStpTransactions.class.getName());

	/**
	 * @param holdReference
	 * @param accountId
	 * @return
	 */
	public RsHeader unblockNonStpTransaction(String holdReference, String accountId) {
		DeleteAccountHoldRq deleteAccHoldRq = new DeleteAccountHoldRq();
		AccountKeys accountKeys = new AccountKeys();
		MessageStatus txnStatus = new MessageStatus();
		RsHeader rsHeader = new RsHeader();
		txnStatus.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
		
		
		AcctHoldDeleteFull acctHoldDeleteFull = new AcctHoldDeleteFull();
		DeleteAccountHoldRs deleteAccRs = new DeleteAccountHoldRs();
		InputAccount inputAccount = new InputAccount();
		inputAccount.setAccountFormatType(StringUtils.EMPTY);
		inputAccount.setInputAccountId(StringUtils.EMPTY);
		accountKeys.setStandardAccountId(accountId);
		accountKeys.setInputAccount(inputAccount);
		acctHoldDeleteFull.setAccountKeys(accountKeys);
		AcctHoldDeleteDtls acctHoldDeleteDtls = new AcctHoldDeleteDtls();
		acctHoldDeleteFull.setAcctHoldDeleteDtls(acctHoldDeleteDtls);
		acctHoldDeleteDtls.setDeleteReason("Swift NonStp Unblocking");
		acctHoldDeleteFull.setTransactionId(holdReference);
		acctHoldDeleteFull.setHoldReference(holdReference);
		deleteAccHoldRq.setAcctHoldDeleteFull(acctHoldDeleteFull);
		HashMap<String, Object> startFatomData = new HashMap<>();
		deleteAccHoldRq.setRqHeader(SWTPostingUtils.rqHeaderInput());
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Blocking reference:::: " + holdReference);
		}
		startFatomData.put(MFInputOutPutKeys.DELETE_ACCT_HOLDER_REQ, deleteAccHoldRq);
		Map outputParams = MFExecuter.executeMF(PaymentSwiftConstants.CB_ACC_DEL_ACCOUNT_BLOCKS, BankFusionThreadLocal.getBankFusionEnvironment(), startFatomData);
		if (outputParams != null) {
			deleteAccRs = (DeleteAccountHoldRs) outputParams.get(MFInputOutPutKeys.DELETE_ACCT_HOLDER_RES);
			rsHeader = deleteAccRs.getRsHeader();
		}
		if (!StringUtils.isBlank(rsHeader.getStatus().getOverallStatus()) && !PaymentSwiftConstants.SUCCESS.equalsIgnoreCase(rsHeader.getStatus().getOverallStatus())) {
			LOGGER.info("Unblocking failed for Blocking reference:::: " + holdReference);
			txnStatus.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
		}
		else {
			txnStatus.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
		}
		rsHeader.setStatus(txnStatus);
		return rsHeader;
	}
}
