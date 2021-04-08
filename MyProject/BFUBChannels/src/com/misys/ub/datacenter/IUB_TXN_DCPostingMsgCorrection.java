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

/**
 * @author amanjuna
 *
 */
public interface IUB_TXN_DCPostingMsgCorrection {
	
	boolean msgCorrection(ReadAccountRs rs, String hostTxnCode, String postingAction,String txnRef,Timestamp postingDateTime,
			String sourceBranchId,String channelId,Integer serialNo,Boolean writeToLog,Long chqNumber,BigDecimal chqAmount);
	
}