package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.ub.datacenter.AccountRightsIndicatorMsgCorrection;
import com.misys.ub.datacenter.AccountStatusMsgCorrection;
import com.misys.ub.datacenter.ChequeStatusMsgCorrection;
import com.misys.ub.datacenter.CustomerStatusMsgCorrection;
import com.misys.ub.datacenter.DCTxnPostingLogger;
import com.misys.ub.datacenter.DataCenterCommonConstants;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TXN_DCPostingMsgCorrection;

public class UB_TXN_DCPostingMsgCorrection extends
		AbstractUB_TXN_DCPostingMsgCorrection {

	private static final long serialVersionUID = 1L;

	//private transient static final Log LOG = LogFactory.getLog(UB_TXN_DCPostingMsgCorrection.class);

	protected IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	String customerAccountId = CommonConstants.EMPTY_STRING;
	String postingAction = CommonConstants.EMPTY_STRING;
	String hostTxnCode = CommonConstants.EMPTY_STRING;
	AccountStatusMsgCorrection acctStatusMsgCorrection;

	String flagIndicator = CommonConstants.EMPTY_STRING;
	String value = CommonConstants.EMPTY_STRING;
	String eventCode = CommonConstants.EMPTY_STRING;
	DCTxnPostingLogger logDetails = new DCTxnPostingLogger();
	DataCenterCommonUtils dcUtils;

	/**
	 * Default constructor
	 */
	public UB_TXN_DCPostingMsgCorrection() {
		super();
	}

	/**
	 * Parameterized constructor
	 * 
	 * @param env
	 */
	public UB_TXN_DCPostingMsgCorrection(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		super.process(env);
		VectorTable postingMessageVector = new VectorTable();
		VectorTable responseMessageVector = new VectorTable();
		postingMessageVector = this.getF_IN_PostingMessageVectorRq();
		String userId = this.getF_IN_UserId();
		String sourceBranchId = this.getF_IN_BranchCode();
		Boolean writeToLog=Boolean.TRUE;
		

		String transactionReference = (String) postingMessageVector.getRowTags(
				0).get("TRANSACTIONREF");
		Timestamp transactionDate = (Timestamp) postingMessageVector
				.getRowTags(0).get("TRANSACTIONDATE");

		String channelId = (String) postingMessageVector.getRowTags(0).get(
				"CHANNELID");

		int size = postingMessageVector.size();
		// start the for loop
		for (int i = 0; i < size; i++) {
			Map postingMessageMap = postingMessageVector.getRowTags(i);
			if (postingMessageMap != null) {
				Boolean postToSuspense = false;
				String accountId = (String) postingMessageMap.get("PRIMARYID");
				String postingSign = (String) postingMessageMap.get("SIGN");
				Integer serialNo = (Integer) postingMessageMap.get("SERIALNO");
				String txnCurrencycode = (String) postingMessageMap
						.get("TXNCURRENCYCODE");
				hostTxnCode = (String) postingMessageMap.get("TRANSACTIONCODE");
				Long chqNumber = (Long) postingMessageMap.get("CHEQUEDRAFTNUMBER");
				BigDecimal chqAmount = (BigDecimal) postingMessageMap.get("AMOUNT");
				ReadAccountRs acctResponse = DataCenterCommonUtils
						.readAccount(accountId);

				// set forcePost flag to true,teller user id,branch code
				postingMessageMap.remove("FORCEPOST");
				postingMessageMap.remove("USERID");
				postingMessageMap.remove("BRANCHSORTCODE");
				
				postingMessageMap.put("FORCEPOST", Boolean.TRUE);
				postingMessageMap.put("USERID", userId);
				postingMessageMap.put("BRANCHSORTCODE", sourceBranchId);

				// account not found
				if (acctResponse.getAccountDetails().getAccountInfo()
						.getAcctBasicDetails().getAccountKeys()
						.getStandardAccountId().isEmpty()) {
					postingMessageMap.remove("PRIMARYID");
					postingMessageMap.put("PRIMARYID",
							getSuspenseAccountFromModuleConfig(txnCurrencycode,
									sourceBranchId));
					postToSuspense = Boolean.TRUE;
					flagIndicator = DataCenterCommonConstants.ACCOUNT_STATUS;
					value = DataCenterCommonConstants.ACCOUNT_NOT_FOUND;
					eventCode = DataCenterCommonConstants.E_ACCOUNT_NOT_FOUND;
					logDetails.setLogStatus(accountId, transactionReference,
							transactionDate, flagIndicator, value, eventCode,
							sourceBranchId, channelId, serialNo);
					// break;
				}

				// check if account id is a customer account or internal account
				if (!postToSuspense
						&& DataCenterCommonUtils
								.checkForExternalProductFeature(accountId)) {
					customerAccountId = accountId;
					if (postingSign.equals("+")) {
						postingAction = DataCenterCommonConstants.POSTING_ACTION_CREDIT;
					} else if (postingSign.equals("-")) {
						postingAction = DataCenterCommonConstants.POSTING_ACTION_DEBIT;
					}

					// account status validated here
					AccountStatusMsgCorrection acctStatusMsg = new AccountStatusMsgCorrection();
					postToSuspense = acctStatusMsg.msgCorrection(acctResponse,
							hostTxnCode, postingAction, transactionReference,
							transactionDate, sourceBranchId, channelId,
							serialNo,writeToLog,chqNumber,chqAmount);
					if (postToSuspense) {
						postingMessageMap.remove("PRIMARYID");
						postingMessageMap.put("PRIMARYID",
								getSuspenseAccountFromModuleConfig(
										txnCurrencycode, sourceBranchId));
					}

					if (!postToSuspense) {
						// customer status validated here
						CustomerStatusMsgCorrection custMsg = new CustomerStatusMsgCorrection();
						postToSuspense = custMsg.msgCorrection(acctResponse,
								hostTxnCode, postingAction,
								transactionReference, transactionDate,
								sourceBranchId, channelId, serialNo,writeToLog,chqNumber,chqAmount);
						if (postToSuspense) {
							postingMessageMap.remove("PRIMARYID");
							postingMessageMap.put("PRIMARYID",
									getSuspenseAccountFromModuleConfig(
											txnCurrencycode, sourceBranchId));
						}
					}

					if (!postToSuspense) {
						// account right indicator validated here
						AccountRightsIndicatorMsgCorrection ariMsg = new AccountRightsIndicatorMsgCorrection();
						postToSuspense = ariMsg.msgCorrection(acctResponse,
								hostTxnCode, postingAction,
								transactionReference, transactionDate,
								sourceBranchId, channelId, serialNo,writeToLog,chqNumber,chqAmount);
						if (postToSuspense) {
							postingMessageMap.remove("PRIMARYID");
							postingMessageMap.put("PRIMARYID",
									getSuspenseAccountFromModuleConfig(
											txnCurrencycode, sourceBranchId));
						}
					}
					//cheque number validation 
					if (chqNumber.intValue() != 0) {
						ChequeStatusMsgCorrection chqMsg = new ChequeStatusMsgCorrection();
						postToSuspense = chqMsg.msgCorrection(acctResponse, hostTxnCode, postingAction, transactionReference, transactionDate, sourceBranchId, channelId, serialNo,writeToLog, chqNumber, chqAmount);
						if (postToSuspense) {
							postingMessageMap.remove("PRIMARYID");
							postingMessageMap.put("PRIMARYID", getSuspenseAccountFromModuleConfig(txnCurrencycode, sourceBranchId));
						}
					}
				}
				responseMessageVector
						.addAll(new VectorTable(postingMessageMap));
			}
		}// end of for loop

		// set output
		setF_OUT_PostingMessageVectorRs(responseMessageVector);

	}

	private String getSuspenseAccountFromModuleConfig(String txnCurrencyCode,
			String branchCode) {
		String suspenseAccount = CommonConstants.EMPTY_STRING;
		String psuedonymName = CommonConstants.EMPTY_STRING;
		String psuedonymContext = CommonConstants.EMPTY_STRING;

		String moduleId = DataCenterCommonConstants.MODULE_ID;
		String pseudonymKey = DataCenterCommonConstants.PSEDONYM_KEY;
		String pseudonymContext = DataCenterCommonConstants.PSEDONYM_CONTEXT_KEY;

		psuedonymName = DataCenterCommonUtils.readModuleConfiguration(moduleId,
				pseudonymKey);
		psuedonymContext = DataCenterCommonUtils.readModuleConfiguration(
				moduleId, pseudonymContext);

		suspenseAccount = DataCenterCommonUtils.retrievePsuedonymAcctId(
				txnCurrencyCode, branchCode, psuedonymContext, psuedonymName);

		return suspenseAccount;

	}

}
