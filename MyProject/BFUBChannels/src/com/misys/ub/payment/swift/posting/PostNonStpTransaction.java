package com.misys.ub.payment.swift.posting;

import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;

public class PostNonStpTransaction {
	/**
	 * @param args
	 */
	public RsHeader postNonStpTransaction(UB_SWT_RemittanceProcessRq remittanceRq, IBOUB_SWT_RemittanceTable remittanceDtls) {
	    //if cash txn, then post the cash transaction by debiting the internal acct from module config and credit the nostro account
		//if account transaction then unblock the txn, then post the transcation by debiting the customer account and crediting the nostro account
		RsHeader rsHeader = new RsHeader();
		PostNonStpAccountTranscation postAcctTxn = new PostNonStpAccountTranscation();
		PostNonStpCashTransaction postCashTxn = new PostNonStpCashTransaction();
		remittanceRq.setCHANNELID(remittanceDtls.getF_UBCHANNELID()!=null? remittanceDtls.getF_UBCHANNELID(): "UXP");
		if (remittanceDtls.getF_UBISCASH() != null && remittanceDtls.getF_UBISCASH().equals(PaymentSwiftConstants.YES)) {
			rsHeader = postCashTxn.postCashTxn(remittanceRq,remittanceDtls);
		}
		else {
			rsHeader = postAcctTxn.postAccountTxn(remittanceRq,remittanceDtls);
		}
		return rsHeader;
	}
}
