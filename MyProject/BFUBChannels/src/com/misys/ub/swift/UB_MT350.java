package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT350 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public UB_MT350() {

	}

	// Following Tags comes under SequenceB	

	private String sendersReference = CommonConstants.EMPTY_STRING;
	private String relatedReference = CommonConstants.EMPTY_STRING;
	private String typeOfOperation = CommonConstants.EMPTY_STRING;
	private String commonReference = CommonConstants.EMPTY_STRING;
	private String partyA = CommonConstants.EMPTY_STRING;
	private String partyAOption = CommonConstants.EMPTY_STRING;
	private String partyB = CommonConstants.EMPTY_STRING;
	private String partyBOption = CommonConstants.EMPTY_STRING;
	private String condition = CommonConstants.EMPTY_STRING;
	private String fundOrInstructingParty = CommonConstants.EMPTY_STRING;
	private String fundOrInstructingPartyOption = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInfo = CommonConstants.EMPTY_STRING;

	// Following Tags comes under SequenceB	

	private String interestPeriod = CommonConstants.EMPTY_STRING;
	private String ccyPrincipalAmount = CommonConstants.EMPTY_STRING;
	private String valueDate = CommonConstants.EMPTY_STRING;
	private String amountSettled = CommonConstants.EMPTY_STRING;
	private String ccyInterestAmount = CommonConstants.EMPTY_STRING;
	private String interestRate = CommonConstants.EMPTY_STRING;
	private String dayCountFraction = CommonConstants.EMPTY_STRING;
	private String nextInterestPayDate = CommonConstants.EMPTY_STRING;

	// Following Tags comes under SequenceC	

	private String deliveryAgent = CommonConstants.EMPTY_STRING;
	private String deliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String interMediary = CommonConstants.EMPTY_STRING;
	private String interMediaryOption = CommonConstants.EMPTY_STRING;
	private String interMediary2 = CommonConstants.EMPTY_STRING;
	private String interMediary2Option = CommonConstants.EMPTY_STRING;
	private String receivingAgent = CommonConstants.EMPTY_STRING;
	private String receivingAgentOption = CommonConstants.EMPTY_STRING;
	private String beneficiary = CommonConstants.EMPTY_STRING;
	private String beneficiaryOption = CommonConstants.EMPTY_STRING;

	// Following Tags comes under SequenceD	

	private String transactionCcyAndIntAmt = CommonConstants.EMPTY_STRING;
	private String transactionCcyAndNetIntAmt = CommonConstants.EMPTY_STRING;
	private String exchangeRate = CommonConstants.EMPTY_STRING;
	private String taxRate = CommonConstants.EMPTY_STRING;
	private String reportingCcyTaxAmount = CommonConstants.EMPTY_STRING;
	private String brokersCommission = CommonConstants.EMPTY_STRING;
	private String brokersCommissionTaxRate = CommonConstants.EMPTY_STRING;
	private String brokersCommissionCurrencyTaxAmt = CommonConstants.EMPTY_STRING;

	public String getAmountSettled() {
		return amountSettled;
	}

	public void setAmountSettled(String amountSettled) {
		this.amountSettled = amountSettled;
	}

	public String getBeneficiary() {
		return beneficiary;
	}

	public void setBeneficiary(String beneficiary) {
		this.beneficiary = beneficiary;
	}

	public String getBeneficiaryOption() {
		return beneficiaryOption;
	}

	public void setBeneficiaryOption(String beneficiaryOption) {
		this.beneficiaryOption = beneficiaryOption;
	}

	public String getBrokersCommission() {
		return brokersCommission;
	}

	public void setBrokersCommission(String brokersCommission) {
		this.brokersCommission = brokersCommission;
	}

	public String getBrokersCommissionCurrencyTaxAmt() {
		return brokersCommissionCurrencyTaxAmt;
	}

	public void setBrokersCommissionCurrencyTaxAmt(String brokersCommissionCurrencyTaxAmt) {
		this.brokersCommissionCurrencyTaxAmt = brokersCommissionCurrencyTaxAmt;
	}

	public String getBrokersCommissionTaxRate() {
		return brokersCommissionTaxRate;
	}

	public void setBrokersCommissionTaxRate(String brokersCommissionTaxRate) {
		this.brokersCommissionTaxRate = brokersCommissionTaxRate;
	}

	public String getCommonReference() {
		return commonReference;
	}

	public void setCommonReference(String commonReference) {
		this.commonReference = commonReference;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getDayCountFraction() {
		return dayCountFraction;
	}

	public void setDayCountFraction(String dayCountFraction) {
		this.dayCountFraction = dayCountFraction;
	}

	public String getDeliveryAgent() {
		return deliveryAgent;
	}

	public void setDeliveryAgent(String deliveryAgent) {
		this.deliveryAgent = deliveryAgent;
	}

	public String getDeliveryAgentOption() {
		return deliveryAgentOption;
	}

	public void setDeliveryAgentOption(String deliveryAgentOption) {
		this.deliveryAgentOption = deliveryAgentOption;
	}

	public String getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(String exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getFundOrInstructingParty() {
		return fundOrInstructingParty;
	}

	public void setFundOrInstructingParty(String fundOrInstructingParty) {
		this.fundOrInstructingParty = fundOrInstructingParty;
	}

	public String getFundOrInstructingPartyOption() {
		return fundOrInstructingPartyOption;
	}

	public void setFundOrInstructingPartyOption(String fundOrInstructingPartyOption) {
		this.fundOrInstructingPartyOption = fundOrInstructingPartyOption;
	}

	public String getInterestPeriod() {
		return interestPeriod;
	}

	public void setInterestPeriod(String interestPeriod) {
		this.interestPeriod = interestPeriod;
	}

	public String getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(String interestRate) {
		this.interestRate = interestRate;
	}

	public String getInterMediary() {
		return interMediary;
	}

	public void setInterMediary(String interMediary) {
		this.interMediary = interMediary;
	}

	public String getInterMediary2() {
		return interMediary2;
	}

	public void setInterMediary2(String interMediary2) {
		this.interMediary2 = interMediary2;
	}

	public String getInterMediary2Option() {
		return interMediary2Option;
	}

	public void setInterMediary2Option(String interMediary2Option) {
		this.interMediary2Option = interMediary2Option;
	}

	public String getInterMediaryOption() {
		return interMediaryOption;
	}

	public void setInterMediaryOption(String interMediaryOption) {
		this.interMediaryOption = interMediaryOption;
	}

	public String getNextInterestPayDate() {
		return nextInterestPayDate;
	}

	public void setNextInterestPayDate(String nextInterestPayDate) {
		this.nextInterestPayDate = nextInterestPayDate;
	}

	public String getPartyA() {
		return partyA;
	}

	public void setPartyA(String partyA) {
		this.partyA = partyA;
	}

	public String getPartyAOption() {
		return partyAOption;
	}

	public void setPartyAOption(String partyAOption) {
		this.partyAOption = partyAOption;
	}

	public String getPartyB() {
		return partyB;
	}

	public void setPartyB(String partyB) {
		this.partyB = partyB;
	}

	public String getPartyBOption() {
		return partyBOption;
	}

	public void setPartyBOption(String partyBOption) {
		this.partyBOption = partyBOption;
	}

	public String getReceivingAgent() {
		return receivingAgent;
	}

	public void setReceivingAgent(String receivingAgent) {
		this.receivingAgent = receivingAgent;
	}

	public String getReceivingAgentOption() {
		return receivingAgentOption;
	}

	public void setReceivingAgentOption(String receivingAgentOption) {
		this.receivingAgentOption = receivingAgentOption;
	}

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReference(String relatedReference) {
		this.relatedReference = relatedReference;
	}

	public String getReportingCcyTaxAmount() {
		return reportingCcyTaxAmount;
	}

	public void setReportingCcyTaxAmount(String reportingCcyTaxAmount) {
		this.reportingCcyTaxAmount = reportingCcyTaxAmount;
	}

	public String getSendersReference() {
		return sendersReference;
	}

	public void setSendersReference(String sendersReference) {
		this.sendersReference = sendersReference;
	}

	public String getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(String taxRate) {
		this.taxRate = taxRate;
	}

	public String getTransactionCcyAndIntAmt() {
		return transactionCcyAndIntAmt;
	}

	public void setTransactionCcyAndIntAmt(String transactionCcyAndIntAmt) {
		this.transactionCcyAndIntAmt = transactionCcyAndIntAmt;
	}

	public String getTransactionCcyAndNetIntAmt() {
		return transactionCcyAndNetIntAmt;
	}

	public void setTransactionCcyAndNetIntAmt(String transactionCcyAndNetIntAmt) {
		this.transactionCcyAndNetIntAmt = transactionCcyAndNetIntAmt;
	}

	public String getTypeOfOperation() {
		return typeOfOperation;
	}

	public void setTypeOfOperation(String typeOfOperation) {
		this.typeOfOperation = typeOfOperation;
	}

	public String getValueDate() {
		return valueDate;
	}

	public void setValueDate(String valueDate) {
		this.valueDate = valueDate;
	}

	public String getSenderToReceiverInfo() {
		return senderToReceiverInfo;
	}

	public void setSenderToReceiverInfo(String senderToReceiverInfo) {
		this.senderToReceiverInfo = senderToReceiverInfo;
	}

	public String getCcyInterestAmount() {
		return ccyInterestAmount;
	}

	public void setCcyInterestAmount(String ccyInterestAmount) {
		this.ccyInterestAmount = ccyInterestAmount;
	}

	public String getCcyPrincipalAmount() {
		return ccyPrincipalAmount;
	}

	public void setCcyPrincipalAmount(String ccyPrincipalAmount) {
		this.ccyPrincipalAmount = ccyPrincipalAmount;
	}

}
