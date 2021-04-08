/**
 * * Copyright (c) 2019 Finastra Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */

package com.misys.ub.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.fbe.readLoan.impl.ReadLoanBasicDetailsFatom;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_LendingEventsPushHandler;

import bf.com.misys.fbe.lending.enquiry.msgs.ReadLoanDetailsRq;
import bf.com.misys.fbe.lending.enquiry.msgs.ReadLoanDetailsRs;
import bf.com.misys.fbe.lending.enquiry.types.ReadLoanBasicDetails;
import bf.com.misys.fbe.lending.types.LoanBasicInfo;

public class LendingEventsHandler extends AbstractUB_CMN_LendingEventsPushHandler {

	/**
	 * 
	 */

	private static final long serialVersionUID = -212082472246753579L;
	private static final int ESTABLISHMENT_EVENT = 40312385;
	private static final transient Log logger = LogFactory.getLog(LendingEventsHandler.class.getName());

	public LendingEventsHandler() {
		super();
	}

	public LendingEventsHandler(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		if (null == getF_IN_eventData() || getF_IN_event() == CommonConstants.INTEGER_ZERO) {
			return;
		}
		logger.info("Start of LendingEvent Handler");
		Object eventData = getF_IN_eventData();
		int event = getF_IN_event().intValue();
		Gson gson = new Gson();
		JsonObject eventObj = null;
		String response = null;
		try {
			if (event == ESTABLISHMENT_EVENT) {
				eventObj = convertReadLoanBasicInfoToJson(eventData, true);
			} else {
				LoanBasicInfo basicLoanInfo = (LoanBasicInfo) eventData;
				response = gson.toJson(basicLoanInfo);
				eventObj = new JsonParser().parse(response).getAsJsonObject();
				eventObj.addProperty("_maturityDate", basicLoanInfo.getMaturityDate().toString());
				eventObj.addProperty("_loanStartDate", basicLoanInfo.getLoanStartDate().toString());
			}
		} catch (Exception ex) {
			logger.error(ExceptionUtil.getExceptionAsString(ex));
			return;
		}
		String endPoint = "LOAN_EVENT_DETAIL_PUSH";
		pushMessageToQueue(eventObj.toString(), endPoint);

	}

	/**
	 * Method to send messages to given endpoint
	 * 
	 * @param message
	 * @param endPoint
	 */
	private void pushMessageToQueue(String message, String endPoint) {
		// TODO Auto-generated method stub
			MessageProducerUtil.sendMessage(message, endPoint);
	}

	private ReadLoanDetailsRs readLoanBasicInfo(String loanAccountNumber, String loanReference,
			BankFusionEnvironment env) {
		ReadLoanDetailsRs readLoanRs = new ReadLoanDetailsRs();
		try {
			ReadLoanBasicDetailsFatom readLoanFatom = new ReadLoanBasicDetailsFatom(env);
			ReadLoanDetailsRq readLoanRq = new ReadLoanDetailsRq();
			readLoanRq.setLoanAccountNumber(loanAccountNumber);
			readLoanFatom.setF_IN_ReadLoanDetailsRq(readLoanRq);
			readLoanFatom.process(env);
			readLoanRs = readLoanFatom.getF_OUT_ReadLoanDetailsRs();
		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return readLoanRs;

	}

	/**
	 * Method that converts the LoanBasicInfo object to JsonObject.
	 * 
	 * @param loanInfoObj
	 * @param onlyBasicInfo
	 * @return
	 */
	private JsonObject convertReadLoanBasicInfoToJson(Object loanInfoObj, boolean onlyBasicInfo) {
		JsonObject loanBasicInfo = null;
		Gson gson = new Gson();
		ReadLoanBasicDetails basicLoanInfoObj = (ReadLoanBasicDetails) loanInfoObj;
		if (onlyBasicInfo) {
			loanBasicInfo = new JsonObject();
			loanBasicInfo.addProperty("_loanAccountNumber",
					basicLoanInfoObj.getLoanAccountDetails().getLoanAccountNumber().getAccountID());
			loanBasicInfo.addProperty("_accountName",
					basicLoanInfoObj.getLoanAccountDetails().getLoanAccountNumber().getAccountName());
			loanBasicInfo.addProperty("_ibanCode", basicLoanInfoObj.getLoanAccountDetails().getIbanCode());
			loanBasicInfo.addProperty("_customerId", basicLoanInfoObj.getCustomerDetails().getCustomerId());
			loanBasicInfo.addProperty("_abstractProductID",
					basicLoanInfoObj.getProductDetails().getAbstractProductID());
			loanBasicInfo.addProperty("_subProductID", basicLoanInfoObj.getProductDetails().getSubProductID());
			loanBasicInfo.addProperty("_productContextCode",
					basicLoanInfoObj.getProductDetails().getProductContextCode());
			loanBasicInfo.addProperty("_branchSortCode", basicLoanInfoObj.getBranchInfo().getBranchSortCode());
			loanBasicInfo.addProperty("_loanStatus",
					basicLoanInfoObj.getLoanAccountDetails().getLoanStatus().getDescription());
			loanBasicInfo.addProperty("_subCodeType",
					basicLoanInfoObj.getLoanAccountDetails().getLoanStatus().getSubCodeType());
			loanBasicInfo.addProperty("_outStandingPrincipal",
					basicLoanInfoObj.getLoanAmountDetails().getOutStandingPrincipal().getCurrencyAmount());
			loanBasicInfo.addProperty("_totalFacility",
					basicLoanInfoObj.getLoanAmountDetails().getTotalFacility().getCurrencyAmount());
			loanBasicInfo.addProperty("_currencyCode",
					basicLoanInfoObj.getLoanAmountDetails().getOutStandingPrincipal().getCurrencyCode());
			loanBasicInfo.addProperty("_loanTerm",
					basicLoanInfoObj.getLoanTermDetails().getLoanTerm().getPeriodNumber());
			loanBasicInfo.addProperty("_loanTermIn",
					basicLoanInfoObj.getLoanTermDetails().getLoanTerm().getPeriodCode());
			loanBasicInfo.addProperty("_finalMaturityDate",
					basicLoanInfoObj.getLoanAccountDetails().getFinalMaturityDate().toString());
			loanBasicInfo.addProperty("_loanStartDate",
					basicLoanInfoObj.getLoanAccountDetails().getLoanStartDate().toString());
			loanBasicInfo.addProperty("_repaymentType",
					basicLoanInfoObj.getRepaymentDetails().getRepaymentType().getDescription());
			loanBasicInfo.addProperty("_repaymentFrequency",
					basicLoanInfoObj.getRepaymentDetails().getRepaymentFrequency().getDescription());
			loanBasicInfo.addProperty("_interestFrequency",
					basicLoanInfoObj.getRepaymentDetails().getInterestFrequency().getDescription());
			loanBasicInfo.addProperty("_interestRateType",
					basicLoanInfoObj.getBasicInterestInfo().getInterestRateType());
			loanBasicInfo.addProperty("_effectiveInterestRate",
					basicLoanInfoObj.getBasicInterestInfo().getEffectiveInterestRate());
			loanBasicInfo.addProperty("_marginRate", basicLoanInfoObj.getBasicInterestInfo().getMarginRate());
			loanBasicInfo.addProperty("_interestFrequency",
					basicLoanInfoObj.getRepaymentDetails().getInterestFrequency().getDescription());
			loanBasicInfo.addProperty("_nextRepaymentDate",
					basicLoanInfoObj.getRepaymentDetails().getNextRepaymentDate().toString());
			if (basicLoanInfoObj.getDisbursementDetails().getDisbursementDetailCount() > 0) {
				loanBasicInfo.addProperty("_disbursementDate", basicLoanInfoObj.getDisbursementDetails()
						.getDisbursementDetail(0).getDisbursementDate().toString());
			}
			loanBasicInfo.addProperty("_accountOperation", "Loan Establishment");
		} else {
			String response = gson.toJson(basicLoanInfoObj);
			loanBasicInfo = new JsonParser().parse(response).getAsJsonObject();
		}
		return loanBasicInfo;
	}

}
