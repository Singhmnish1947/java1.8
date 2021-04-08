package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.ub.datacenter.AccountRightsIndicatorMsgCorrection;
import com.misys.ub.datacenter.AccountStatusMsgCorrection;
import com.misys.ub.datacenter.CustomerStatusMsgCorrection;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TXN_ValidateAccountPostingFatom;

public class UB_TXN_ValidateAccountPostingFatom extends
		AbstractUB_TXN_ValidateAccountPostingFatom {

	private static final long serialVersionUID = 1L;

	//private transient static final Log LOG = LogFactory.getLog(UB_TXN_ValidateAccountPostingFatom.class);

	protected IPersistenceObjectsFactory factory = BankFusionThreadLocal
			.getPersistanceFactory();

	/**
	 * Default constructor
	 */
	public UB_TXN_ValidateAccountPostingFatom() {
		super();
	}

	/**
	 * Parameterized constructor
	 * 
	 * @param env
	 */
	public UB_TXN_ValidateAccountPostingFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		super.process(env);

		String sourceBranchId = this.getF_IN_branchCode();
		String transactionReference = this.getF_IN_transactionReference();
		Timestamp transactionDate = this.getF_IN_transactionDate();
		String channelId = this.getF_IN_channelId();
		Integer serialNo = 1;
		String hostTxnCode = this.getF_IN_hostTxnCode();
		Long chqNumber = CommonConstants.LONG_ZERO;
		BigDecimal chqAmount = CommonConstants.BIGDECIMAL_ZERO;
		String postingAction = this.getF_IN_postingAction();		
		String accountId = this.getF_IN_accountId();
		Boolean writeToLog = this.isF_IN_writeToLog();

		ReadAccountRs acctResponse = DataCenterCommonUtils
				.readAccount(accountId);
		Boolean postToSuspense = false;

		if (acctResponse.getAccountDetails().getAccountInfo()
				.getAcctBasicDetails().getAccountKeys().getStandardAccountId()
				.isEmpty()) {
			postToSuspense = Boolean.TRUE;

		}

		if (!postToSuspense
				&& DataCenterCommonUtils
						.checkForExternalProductFeature(accountId)) {

			// account status validated here
			AccountStatusMsgCorrection acctStatusMsg = new AccountStatusMsgCorrection();
			postToSuspense = acctStatusMsg.msgCorrection(acctResponse,
					hostTxnCode, postingAction, transactionReference,
					transactionDate, sourceBranchId, channelId, serialNo,
					writeToLog,chqNumber,chqAmount);

			if (!postToSuspense) {
				// customer status validated here
				CustomerStatusMsgCorrection custMsg = new CustomerStatusMsgCorrection();
				postToSuspense = custMsg.msgCorrection(acctResponse,
						hostTxnCode, postingAction, transactionReference,
						transactionDate, sourceBranchId, channelId, serialNo,
						writeToLog,chqNumber,chqAmount);
			}

			if (!postToSuspense) {
				// account right indicator validated here
				AccountRightsIndicatorMsgCorrection ariMsg = new AccountRightsIndicatorMsgCorrection();
				postToSuspense = ariMsg.msgCorrection(acctResponse,
						hostTxnCode, postingAction, transactionReference,
						transactionDate, sourceBranchId, channelId, serialNo,
						writeToLog,chqNumber,chqAmount);
			}

		}
		setF_OUT_postToSuspense(postToSuspense);
	}

}
