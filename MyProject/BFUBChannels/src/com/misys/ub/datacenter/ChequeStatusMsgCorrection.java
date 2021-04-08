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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;

import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOChequeBookDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOChequeDetailsFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPresentedCheques;
import com.trapedza.bankfusion.bo.refimpl.IBOStoppedChq;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

public class ChequeStatusMsgCorrection implements IUB_TXN_DCPostingMsgCorrection {
	private static final String firstStoppedChqWhereClause = " WHERE " + IBOStoppedChq.ACCOUNTID + " = ? and " + " ((? between " + IBOStoppedChq.FROMSTOPCHQREF + " AND " + IBOStoppedChq.TOSTOPCHQREF + " AND (" + IBOStoppedChq.AMOUNT + " =0 OR " + IBOStoppedChq.AMOUNT + "= ?)) OR (" + IBOStoppedChq.AMOUNT + " = ? AND " + IBOStoppedChq.FROMSTOPCHQREF + " =0 AND " + IBOStoppedChq.TOSTOPCHQREF + "=0) OR (" + IBOStoppedChq.FROMSTOPCHQREF + "=" + IBOStoppedChq.TOSTOPCHQREF + " and " + IBOStoppedChq.FROMSTOPCHQREF + "=?))";
	private static final String thirdWhereClause = "WHERE " + IBOChequeBookDetails.ACCOUNTID + " = ? order by " + IBOChequeBookDetails.DATEISSUED + " desc";
	public static final String presentedChequeWhrClause = " WHERE " + IBOPresentedCheques.CHEQUEDRAFTNUMBER + " = ?  AND " + IBOPresentedCheques.ACCOUNTID + " = ? ";
	//public static final String cancelledChequeWhrClause = "WHERE " + IBOChequeBookDetails.ACCOUNTID + " = ? and " + IBOChequeBookDetails.UBISDELETE + " =Y";

	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	String flagIndicator = CommonConstants.EMPTY_STRING;
	String value = CommonConstants.EMPTY_STRING;
	String eventCode = CommonConstants.EMPTY_STRING;
	DCTxnPostingLogger logDetails = new DCTxnPostingLogger();
	private static String locale;
	private static final String REQUESTED_STATUS = "REQUESTED";

	@Override
	public boolean msgCorrection(ReadAccountRs rs, String hostTxnCode, String postingAction, String txnRef, Timestamp postingDateTime, String sourceBranchId, String channelId, Integer serialNo, Boolean writeToLog,Long chequeDraftNumber, BigDecimal chequeAmount) {
		boolean postToSuspense = false;
		String accountId = rs.getAccountDetails().getAccountInfo().getAcctBasicDetails().getAccountKeys().getStandardAccountId();
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(accountId);
		IBOChequeDetailsFeature chqFeatSuper = null;
		chqFeatSuper = (IBOChequeDetailsFeature) factory.findFirstByQuery(IBOChequeDetailsFeature.BONAME, "WHERE " + IBOChequeDetailsFeature.ACCOUNTID + " = ?", params, false);
		locale = MFExecuter.executeMF("CB_GCD_UserLocaleForTranslate_SRV", BankFusionThreadLocal.getBankFusionEnvironment()).get("localeId").toString();
		//if cheque feature attached to account
		if (chqFeatSuper != null) {
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("chequeDraftNumber", chequeDraftNumber);
			ArrayList values = new ArrayList();
			values.add(accountId);
			values.add(chequeDraftNumber);
			values.add(chequeAmount);
			values.add(chequeAmount);
			values.add(chequeDraftNumber);
			Iterator stoppedChqEnumStop = null;
			stoppedChqEnumStop = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOStoppedChq.BONAME, firstStoppedChqWhereClause, values, 5, true);
			// if the ChequeDraftNumber is stopped then a check is done to see if the ChequeDraftNumber
			// is unstopped
			if (stoppedChqEnumStop.hasNext()) {
				IBOStoppedChq superStoppedChqEnumStop = (IBOStoppedChq) stoppedChqEnumStop.next();
				if (superStoppedChqEnumStop.isF_STOPPEDSTATUS() == true && !postToSuspense) {
					postToSuspense = Boolean.TRUE;
					//log in table
					flagIndicator = DataCenterCommonConstants.CHEQUE_STATUS;
					value = DataCenterCommonConstants.CHEQUE_STOPPED;
					eventCode = String.valueOf(CommonsEventCodes.E_THE_CHEQUE_DRAFT_IS_STOPPED);
					logDetails.setLogStatus(accountId, txnRef, postingDateTime, flagIndicator, value, eventCode, sourceBranchId, channelId, serialNo);
				}
				else if (!postToSuspense) {
					//cheque has  been cancelled
					postToSuspense = Boolean.TRUE;
					//log in table
					flagIndicator = DataCenterCommonConstants.CHEQUE_STATUS;
					value = DataCenterCommonConstants.CHEQUE_CANCELLED;
					eventCode = String.valueOf(CommonsEventCodes.E_THE_CHEQUE_DRAFT_IS_STOPPED);
					//eventCode = String.valueOf(CommonsEventCodes.E_THE_CHEQUE_DRAFT_IS_CANCELLED_UB);
					logDetails.setLogStatus(accountId, txnRef, postingDateTime, flagIndicator, value, eventCode, sourceBranchId, channelId, serialNo);
				}
			}
			// get chequeBookDetailBO object to call findbyPrimaryKey
			// function.
			values.clear();
			values.add(accountId);
			Iterator chequeBookDetailEnum = factory.findByQuery(IBOChequeBookDetails.BONAME, thirdWhereClause, values, 5, false);
			// if the cheque is not issued
			if (!chequeBookDetailEnum.hasNext() && !postToSuspense) {
				postToSuspense = Boolean.TRUE;
				//log in table
				flagIndicator = DataCenterCommonConstants.CHEQUE_STATUS;
				value = DataCenterCommonConstants.CHEQUE_HAS_NOT_BEEN_ISSUED;
				eventCode = String.valueOf(CommonsEventCodes.E_CHEQUE_DRAFT_HAS_NOT_BEEN_ISSUED);
				logDetails.setLogStatus(accountId, txnRef, postingDateTime, flagIndicator, value, eventCode, sourceBranchId, channelId, serialNo);
			}
			while (chequeBookDetailEnum.hasNext()) {
				IBOChequeBookDetails superChequeBookDetail = (IBOChequeBookDetails) chequeBookDetailEnum.next();
				//cheque book not yet activated
				if (!postToSuspense && (REQUESTED_STATUS + locale).equals(superChequeBookDetail.getF_STATUS() + locale) || (superChequeBookDetail.isF_UBISDELETE() == Boolean.TRUE)) {
					postToSuspense = Boolean.TRUE;
					//log in table
					flagIndicator = DataCenterCommonConstants.CHEQUEBOOK_ISSUE_STATUS;
					value = REQUESTED_STATUS;
					eventCode = String.valueOf(CommonsEventCodes.E_CHEQUEBOOK_IS_NOT_YET_ACTIVATED_UB);
					logDetails.setLogStatus(accountId, txnRef, postingDateTime, flagIndicator, value, eventCode, sourceBranchId, channelId, serialNo);
				}
				//cheque not within a valid range
				if (Boolean.FALSE == (chequeDraftNumber.intValue() >= superChequeBookDetail.getF_FROMCHEQUENUMBER() && chequeDraftNumber.intValue() <= superChequeBookDetail.getF_TOCHEQUENUMBER()) && !postToSuspense) {
					postToSuspense = Boolean.TRUE;
					//log in table
					flagIndicator = DataCenterCommonConstants.CHEQUE_STATUS;
					value = DataCenterCommonConstants.CHEQUE_DRAFT_NOT_WITHIN_A_VALID_RANGE;
					eventCode = String.valueOf(CommonsEventCodes.E_CHEQUE_DRAFT_NOT_WITHIN_A_VALID_RANGE);
					logDetails.setLogStatus(accountId, txnRef, postingDateTime, flagIndicator, value, eventCode, sourceBranchId, channelId, serialNo);
				}
			}
			//cheque has already been presented
			Iterator enu = FinderMethods.findPresentedChqByAccountIDAndChequeDraftNumber(accountId, chequeDraftNumber, BankFusionThreadLocal.getBankFusionEnvironment(), null).iterator();
			if (enu.hasNext() && !postToSuspense) {
				postToSuspense = Boolean.TRUE;
				//log in table
				flagIndicator = DataCenterCommonConstants.CHEQUE_STATUS;
				value = DataCenterCommonConstants.CHEQUE_PRESENTED;
				eventCode = String.valueOf(CommonsEventCodes.E_CHEQUE_DRAFT_HAS_ALREADY_BEEN_PRESENTED);
				logDetails.setLogStatus(accountId, txnRef, postingDateTime, flagIndicator, value, eventCode, sourceBranchId, channelId, serialNo);
			}
		}
		return postToSuspense;
	}
}
