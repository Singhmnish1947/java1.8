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

import com.trapedza.bankfusion.core.CommonConstants;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;

/**
 * @author amanjuna
 *
 */
public class CustomerStatusMsgCorrection implements
		IUB_TXN_DCPostingMsgCorrection {

	String flagIndicator=CommonConstants.EMPTY_STRING;;
	String value=CommonConstants.EMPTY_STRING;;
	String eventCode=CommonConstants.EMPTY_STRING;;
	DCTxnPostingLogger logDetails=new DCTxnPostingLogger();
	

	/* (non-Javadoc)
	 * @see com.misys.ub.datacenter.IUB_TXN_DCPostingMsgCorrection#msgCorrection(bf.com.misys.cbs.msgs.v1r0.ReadAccountRs, java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.Integer, java.math.BigDecimal)
	 */
	public  boolean msgCorrection(ReadAccountRs rs, String hostTxnCode,
			String postingAction,String txnRef,Timestamp postingDateTime,String sourceBranchId,String channelId,Integer serialNo,Boolean writeToLog,Long chqNumber,BigDecimal chqAmount) {
		String accountId=rs.getAccountDetails().getAccountInfo().getAcctBasicDetails().getAccountKeys().getStandardAccountId();
		String customerCode=rs.getAccountDetails().getAccountInfo().getAcctBasicDetails().getCustomerShortDetails().getCustomerId();
		Boolean isCustomerBlackListed=DataCenterCommonUtils.readKYCStatusOfCustomer(customerCode);
		boolean postToSuspense = false;
		if(isCustomerBlackListed){
			postToSuspense=Boolean.TRUE;
			//log in table
			flagIndicator=DataCenterCommonConstants.PARTY_STATUS;
			value=DataCenterCommonConstants.BLACK_LISTED;
			eventCode=DataCenterCommonConstants.E_CUSTOMER_IS_BLACKLISTED;
			if(writeToLog){
			logDetails.setLogStatus(accountId, txnRef, postingDateTime,flagIndicator, value, eventCode,sourceBranchId, channelId,serialNo);
			}
		}
		return postToSuspense;
	}

}
