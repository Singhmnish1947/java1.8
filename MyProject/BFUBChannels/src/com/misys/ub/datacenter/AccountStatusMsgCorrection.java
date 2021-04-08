/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerAccountRefreshAccumulator.java,v.1.1.2.3,Aug 21, 2008 2:58:06 PM KrishnanRR
 * 
 * $Log: BranchPowerAccountRefreshAccumulator.java,v $
 * Revision 1.1.2.4  2008/08/21 23:24:57  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.datacenter;

import java.math.BigDecimal;
import java.sql.Timestamp;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;

import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

/**
 * @author amanjuna
 *
 */
public class AccountStatusMsgCorrection implements
		IUB_TXN_DCPostingMsgCorrection {

	String flagIndicator = CommonConstants.EMPTY_STRING;;
	String value = CommonConstants.EMPTY_STRING;;
	String eventCode = CommonConstants.EMPTY_STRING;;
	DCTxnPostingLogger logDetails = new DCTxnPostingLogger();

	/* (non-Javadoc)
	 * @see com.misys.ub.datacenter.IUB_TXN_DCPostingMsgCorrection#msgCorrection(bf.com.misys.cbs.msgs.v1r0.ReadAccountRs, java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.Integer, java.math.BigDecimal)
	 */
	public boolean msgCorrection(ReadAccountRs rs, String hostTxnCode,
			String postingAction, String txnRef, Timestamp postingDateTime,
			String sourceBranchId, String channelId, Integer serialNo,Boolean writeToLog, Long chqNumber,BigDecimal chqAmount) {
		boolean postToSuspense = false;
		String accountId = rs.getAccountDetails().getAccountInfo()
				.getAcctBasicDetails().getAccountKeys().getStandardAccountId();
		Boolean isAccountClosed = rs.getAccountDetails().getAccountInfo()
				.getAcctCharacteristics().getIsClosed();
		Boolean isAccountDormant = rs.getAccountDetails().getAccountInfo()
				.getAcctCharacteristics().getIsDormant();
		MISTransactionCodeDetails mistransDetails;
		IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
				.getInstance()
				.getServiceManager()
				.getServiceForName(
						IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
		mistransDetails = ((IBusinessInformation) ubInformationService
				.getBizInfo()).getMisTransactionCodeDetails(hostTxnCode);

		String dormancyPostingAction = mistransDetails.getMisTransactionCodes()
				.getF_DORMANCYPOSTINGACTION();
		// dormancy posting action 1
		if (isAccountDormant.equals(Boolean.TRUE)) {
			if (dormancyPostingAction.equals(DataCenterCommonConstants.REJECT_TRANSACTION)) {
				postToSuspense = Boolean.TRUE;
				// log in table
				flagIndicator = DataCenterCommonConstants.DORMANCY_POSTING_ACTION;
				value = DataCenterCommonConstants.REJECT_TRANSACTION;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_IS_DORMANT;
				// correct the error code
				if(writeToLog){
				logDetails.setLogStatus(accountId, txnRef, postingDateTime,
						flagIndicator, value, eventCode, sourceBranchId,
						channelId, serialNo);
				}
			}
			// dormancy posting action 0
			if (dormancyPostingAction.equals(DataCenterCommonConstants.ALLOW_WITHOUT_CHECKING_FOR_DORMANCY)) {
				postToSuspense = Boolean.FALSE;
			}
			// dormancy posting action 2
			if (dormancyPostingAction.equals(DataCenterCommonConstants.REFER_TO_SUPERVISOR_AND_RECATIVATE)){
				postToSuspense = Boolean.FALSE;
				// log in table
				flagIndicator = DataCenterCommonConstants.DORMANCY_POSTING_ACTION;
				value = DataCenterCommonConstants.REFER_TO_SUPERVISOR_AND_RECATIVATE;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_IS_DORMANT;
				if(writeToLog){
				logDetails.setLogStatus(accountId, txnRef, postingDateTime,
						flagIndicator, value, eventCode, sourceBranchId,
						channelId, serialNo);
				}
			}
			// dormancy posting action 3 debit
			if ((dormancyPostingAction.equals(DataCenterCommonConstants.ALLOW_AND_LOG_EXCEPTION))
					&& postingAction
							.equals(DataCenterCommonConstants.POSTING_ACTION_DEBIT)) {
				postToSuspense = Boolean.FALSE;
				// log in table
				flagIndicator = DataCenterCommonConstants.DORMANCY_POSTING_ACTION;
				value = DataCenterCommonConstants.ALLOW_AND_LOG_EXCEPTION;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_IS_DORMANT;
				if(writeToLog){
				logDetails.setLogStatus(accountId, txnRef, postingDateTime,
						flagIndicator, value, eventCode, sourceBranchId,
						channelId, serialNo);
				}
			}
			// dormancy posting action 3 credit
			if ((dormancyPostingAction.equals(DataCenterCommonConstants.ALLOW_AND_LOG_EXCEPTION))
					&& postingAction
							.equals(DataCenterCommonConstants.POSTING_ACTION_CREDIT)) {
				postToSuspense = Boolean.FALSE;
			}
		}

		if (isAccountClosed.equals(Boolean.TRUE)) {
			postToSuspense = Boolean.TRUE;
			// log in table
			flagIndicator = DataCenterCommonConstants.ACCOUNT_STATUS;
			value = DataCenterCommonConstants.ACCOUNT_CLOSED;
			eventCode = DataCenterCommonConstants.E_ACCOUNT_CLOSED;
			logDetails.setLogStatus(accountId, txnRef, postingDateTime,
					flagIndicator, value, eventCode, sourceBranchId, channelId,
					serialNo);
		}

		return postToSuspense;
	}

}
