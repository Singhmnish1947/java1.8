package com.misys.ub.atm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.boundary.outward.BankFusionPropertySupport;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMPOS_TxnMatchValidation;

public class ATMPOS_TxnMatchValidation extends AbstractATMPOS_TxnMatchValidation {

	private static final long serialVersionUID = -703591357156226737L;

	private static final String fetchOrgMsgDetails = " WHERE " + IBOATMActivityDetail.TRANSACTIONREFERENCE + "= ? "
			+ "AND " + IBOATMActivityDetail.SOURCECIB + "= ? " + "AND " + IBOATMActivityDetail.TRANSACTIONDTTM + "= ? "
			+ "AND " + IBOATMActivityDetail.ACCOUNTID + "= ? " + "AND " + IBOATMActivityDetail.TRANSACTIONID + " IS NOT NULL";

	private static final String fetchRevMsgDetails = " WHERE " + IBOATMActivityDetail.TRANSACTIONREFERENCE + "= ? "
	// + "AND " + IBOATMActivityDetail.ACCOUNTID + "= ? "
			+ "AND " + IBOATMActivityDetail.TRANSACTIONID + " IS NOT NULL ";

	private static final String fetchOrgMsgDetailsForChqBkRq = " WHERE " + IBOATMActivityDetail.TRANSACTIONREFERENCE
			+ "= ? " + "AND " + IBOATMActivityDetail.SOURCECIB + "= ? " + "AND " + IBOATMActivityDetail.TRANSACTIONDTTM
			+ "= ? ";

	IPersistenceObjectsFactory factory;

	public ATMPOS_TxnMatchValidation(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		String retrievalReferenceNumber_37 = getF_IN_RetrievalReferenceNumber_37();
		String acquirerIdentificationCode_32 = getF_IN_acquiredIdentificationCode_32();
		;
		Timestamp dateTime = getF_IN_dateAndTime_12();
		Timestamp orgDateTime = getF_IN_orgDateTime_56_3();
		String messageFunction = getF_IN_messageFunction();
		String uniqueEndTransactionReference = getF_IN_uniqueEndTransactionReference_48_031();
		String txnType = getF_IN_txnType();
		String orgAcIDC = getF_IN_orgAcIDC_56_4();
		String atmTxnCode = getF_IN_atmTransactionCode();
		;
		String msgType = getF_IN_msgType();
		String accountId = getF_IN_AccountId();
		boolean flag = isOriginalMessage(messageFunction, txnType, msgType, env);

		if (flag && msgType.equals("ATMChqBookRq")) {
			validateOriginalMsgForChqBkRq(retrievalReferenceNumber_37, acquirerIdentificationCode_32, dateTime, env);
		} else if (flag) {
			validateOriginalMsg(retrievalReferenceNumber_37, acquirerIdentificationCode_32, accountId,
					dateTime, uniqueEndTransactionReference,  env);
		} else {
			validateReversalMsg(retrievalReferenceNumber_37, accountId, orgAcIDC, orgDateTime, atmTxnCode,
					uniqueEndTransactionReference, env);
		}
	}

	public void validateReversalMsg(String retrievalReferenceNumber_37, String accountId, String orgAcIDC,
			Timestamp orgDateTime, String atmTxnCode, String uniqueEndTransactionReference, BankFusionEnvironment env) {
		ArrayList params = new ArrayList();
		factory = BankFusionThreadLocal.getPersistanceFactory();
		List<IBOATMActivityDetail> result;
		StringBuffer query = new StringBuffer(fetchRevMsgDetails);
		params.add(retrievalReferenceNumber_37);
		// params.add(accountId);
		if (!orgAcIDC.isEmpty()) {
			/*
			 * if (!query.toString().isEmpty()) { query = query.append(" AND "); }
			 */
			query = query.append("AND " + IBOATMActivityDetail.SOURCECIB + " = ? ");
			params.add(orgAcIDC);
		}
		if (null != orgDateTime && !orgDateTime.toString().isEmpty()) {
			/*
			 * if (!query.toString().isEmpty()) { query = query.append(" AND "); }
			 */
			query = query.append("AND " + IBOATMActivityDetail.TRANSACTIONDTTM + " = ? ");
			params.add(orgDateTime);
		}

		if (null != uniqueEndTransactionReference && !uniqueEndTransactionReference.toString().isEmpty()) {
			query = query.append("AND " + IBOATMActivityDetail.UBCMSUNIQUEENDTXNREF + " = ? ");
			params.add(uniqueEndTransactionReference);
		}
		if (accountId != null && !accountId.equals("")) {
			query = query.append("AND " + IBOATMActivityDetail.ACCOUNTID + "= ? ");
			params.add(accountId);
		}
		// query = query.append("AND " + IBOATMActivityDetail.ATMTRANSACTIONCODE + " =
		// '" + atmTxnCode + "'");
		result = factory.findByQuery(IBOATMActivityDetail.BONAME, query.toString(), params, null, true);
		params.clear();
		if (result == null || result.isEmpty()) {
			setF_OUT_errorCode("40430037");
			EventsHelper.handleEvent(40430037, new Object[] {}, new HashMap(), env);
		}

	}

	public boolean isOriginalMessage(String messageFunction, String txnType, String msgType,
			BankFusionEnvironment env) {
		txnType = BankFusionPropertySupport.getProperty("ATMPOSMAP_" + txnType, CommonConstants.EMPTY_STRING);
		if (messageFunction == null || messageFunction.equals(CommonConstants.EMPTY_STRING)) {
			if (msgType.equals("UpdateAccountHold"))
				return false;
			else
				return true;
		} else if ((txnType.equals("01") || txnType.equals("21") || txnType.equals("40") || txnType.equals("50")
				|| txnType.equals("00") || txnType.equals("20"))
				&& ((msgType.equals("UpdateAccountHold") || messageFunction.contains("Reversal")
						|| messageFunction.contains("Cancel") || messageFunction.contains("Replacement")))) {

			return false;
		} else {
			return true;
		}
	}

	public void validateOriginalMsg(String retrievalReferenceNumber_37, String acquirerIdentificationCode_32,
			String accountId, Timestamp dateTime, String uniqueEndTransactionReference, BankFusionEnvironment env) {
		ArrayList params = new ArrayList();
		StringBuffer query = new StringBuffer(fetchOrgMsgDetails);
		factory = BankFusionThreadLocal.getPersistanceFactory();
		List<IBOATMActivityDetail> result;
		params.add(retrievalReferenceNumber_37);
		params.add(acquirerIdentificationCode_32);
		params.add(dateTime);
		params.add(accountId);
		if (null != uniqueEndTransactionReference && !uniqueEndTransactionReference.toString().isEmpty()) {
			query = query.append(" AND " + IBOATMActivityDetail.UBCMSUNIQUEENDTXNREF + " = ? ");
			params.add(uniqueEndTransactionReference);
		}

		result = factory.findByQuery(IBOATMActivityDetail.BONAME, query.toString(), params, null, true);
		params.clear();
		if (result != null && !result.isEmpty()) {
			EventsHelper.handleEvent(40430036, new Object[] {}, new HashMap(), env);
		}
	}

	public void validateOriginalMsgForChqBkRq(String retrievalReferenceNumber_37, String acquirerIdentificationCode_32,
			Timestamp dateTime, BankFusionEnvironment env) {
		ArrayList params = new ArrayList();
		factory = BankFusionThreadLocal.getPersistanceFactory();
		List<IBOATMActivityDetail> result;
		params.add(retrievalReferenceNumber_37);
		params.add(acquirerIdentificationCode_32);
		params.add(dateTime);
		result = factory.findByQuery(IBOATMActivityDetail.BONAME, fetchOrgMsgDetailsForChqBkRq, params, null, true);
		params.clear();
		if (result != null && !result.isEmpty()) {
			EventsHelper.handleEvent(40430036, new Object[] {}, new HashMap(), env);
		}
	}

}