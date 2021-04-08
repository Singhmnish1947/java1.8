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

import com.trapedza.bankfusion.core.CommonConstants;

/**
 * @author amanjuna
 *
 */
public class AccountRightsIndicatorMsgCorrection implements
IUB_TXN_DCPostingMsgCorrection {

	String flagIndicator = CommonConstants.EMPTY_STRING;
	String value = CommonConstants.EMPTY_STRING;
	String eventCode = CommonConstants.EMPTY_STRING;
	DCTxnPostingLogger logDetails = new DCTxnPostingLogger();

	/* (non-Javadoc)
	 * @see com.misys.ub.datacenter.IUB_TXN_DCPostingMsgCorrection#msgCorrection(bf.com.misys.cbs.msgs.v1r0.ReadAccountRs, java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.Integer, java.math.BigDecimal)
	 */
	public boolean msgCorrection(ReadAccountRs rs, String hostTxnCode,
			String postingAction, String txnRef, Timestamp postingDateTime,
			String sourceBranchId, String channelId, Integer serialNo,
			Boolean writeToLog, Long chqNumber,BigDecimal chqAmount ) {

		String accountId = rs.getAccountDetails().getAccountInfo()
				.getAcctBasicDetails().getAccountKeys().getStandardAccountId();
		String acctsRightsInd=DataCenterCommonUtils.readAccountsRightsIndicator(accountId);
		int acctsRightsIndInInteger = Integer.parseInt(acctsRightsInd);
		boolean postToSuspense;
		postToSuspense = Boolean.FALSE;
		
		switch (acctsRightsIndInInteger) {
		case -1:
		{
			// ARI -1
			{
				postToSuspense = Boolean.FALSE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_MINUS_ONE;
				eventCode = DataCenterCommonConstants.E_PASSWORD_REQD_FOR_POSTING_AND_ENQUIRY;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId,
							channelId, serialNo);
				}
			}
			break;
		}

		case 0:
		{
			// ARI 0
			{
				postToSuspense = Boolean.FALSE;
			}
			break;
		}

		case 1:		
		{
			// ARI 1
			{
				postToSuspense = Boolean.FALSE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_1;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_PASSWORD_REQUIRED_FOR_POSTING;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId,
							channelId, serialNo);
				}
			}

			break;
		}

		case 2:
		{
			// ARI 2
			{
				postToSuspense = Boolean.TRUE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_2;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_STOPPED_NO_POSTING_IS_ALLOWED;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId, channelId,
							serialNo);
				}
			}
			break;
		}

		case 3:
		{
			// ARI 3
			{
				postToSuspense = Boolean.TRUE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_3;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_STOPPED_ENQUIRY_ALLOWED_WITH_PASSWORD;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId, channelId,
							serialNo);
				}
			}
			break;

		}

		case 4:
		{
			// ARI 4 for Credit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_CREDIT)) {
				postToSuspense = Boolean.FALSE;
			}

			// ARI 4 for debit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_DEBIT)) {
				postToSuspense = Boolean.TRUE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_4;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_NO_DEBITS_ALLOWED;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId, channelId,
							serialNo);
				}
			}
			break;

		}

		case 5:
		{
			// ARI 5 for credit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_CREDIT)) {
				postToSuspense = Boolean.FALSE;
			}

			// ARI 5 for debit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_DEBIT)) {
				postToSuspense = Boolean.FALSE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_5;
				eventCode = DataCenterCommonConstants.R_ACCOUNT_NO_DEBITS_REFERRAL_REQUIRED_OVERRIDING;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId, channelId,
							serialNo);
				}
			}
			break;

		}

		case 6:
		{
			// ARI 6 for credit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_CREDIT)) {
				postToSuspense = Boolean.TRUE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_6;
				eventCode = DataCenterCommonConstants.E_ACCOUNT_NO_CREDITS_ALLOWED;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId, channelId,
							serialNo);
				}

			}

			// ARI 6 for debit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_DEBIT)) {
				postToSuspense = Boolean.FALSE;
			}
			break;
		}

		case 7:
		{
			// ARI 7 for credit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_CREDIT)) {
				postToSuspense = Boolean.FALSE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_7;
				eventCode = DataCenterCommonConstants.R_ACCOUNT_NO_CREDITS_REFERRAL_REQUIRED_OVERRIDING;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId, channelId,
							serialNo);
				}
			}

			// ARI 7 for Debit
			if (postingAction.equals(DataCenterCommonConstants.POSTING_ACTION_DEBIT)) {
				postToSuspense = Boolean.FALSE;
			}
			break;
		}

		case 8:
		{
			// ARI 8
			{
				postToSuspense = Boolean.FALSE;
			}
			break;
		}

		case 9:
		{
			// ARI 9
			{
				postToSuspense = Boolean.FALSE;
				// log in table
				flagIndicator = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR;
				value = DataCenterCommonConstants.ACCOUNT_RIGHTS_INDICATOR_9;
				eventCode = DataCenterCommonConstants.R_REFEERAL_REQ_POSTING_NO_REFERRAL_ENQUIRY;
				if (writeToLog) {
					logDetails.setLogStatus(accountId, txnRef, postingDateTime,
							flagIndicator, value, eventCode, sourceBranchId, channelId,
							serialNo);
				}
			}
			break;
		}	

		default:
			break;
		}

		return postToSuspense;
	}
}
