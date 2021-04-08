package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT330 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String sendersReference = CommonConstants.EMPTY_STRING;
	private String relatedReference = CommonConstants.EMPTY_STRING;
	private String typeOfOperation = CommonConstants.EMPTY_STRING;
	private String scopeOfOperation = CommonConstants.EMPTY_STRING;
	private String typeOfEvent = CommonConstants.EMPTY_STRING;
	private String commonReference = CommonConstants.EMPTY_STRING;
	private String contractNumberPartyA = CommonConstants.EMPTY_STRING;
	private String partyA = CommonConstants.EMPTY_STRING;
	private String partyAOption = CommonConstants.EMPTY_STRING;
	private String partyB = CommonConstants.EMPTY_STRING;
	private String partyBOption = CommonConstants.EMPTY_STRING;
	private String fundOrInstructingParty = CommonConstants.EMPTY_STRING;
	private String fundOrInstPartyOption = CommonConstants.EMPTY_STRING;
	private String termsAndConditions = CommonConstants.EMPTY_STRING;
	private String partyARole = CommonConstants.EMPTY_STRING;
	private String tradeDate = CommonConstants.EMPTY_STRING;
	private String valueDate = CommonConstants.EMPTY_STRING;
	private String periodOfNotice = CommonConstants.EMPTY_STRING;
	private String currencyBalance = CommonConstants.EMPTY_STRING;
	private String amountSettled = CommonConstants.EMPTY_STRING;
	private String interestDueDate = CommonConstants.EMPTY_STRING;
	private String ccyAndInterestAmount = CommonConstants.EMPTY_STRING;
	private String interestRate = CommonConstants.EMPTY_STRING;
	private String dayCountFraction = CommonConstants.EMPTY_STRING;
	private String lastDayNextInterestPeriod = CommonConstants.EMPTY_STRING;
	private String numberOfDays = CommonConstants.EMPTY_STRING;
	private String cDeliveryAgent = CommonConstants.EMPTY_STRING;
	private String cDeliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String cIntermediary2 = CommonConstants.EMPTY_STRING;
	private String cIntermediary2Option = CommonConstants.EMPTY_STRING;
	private String cIntermediary = CommonConstants.EMPTY_STRING;
	private String cIntermediaryOption = CommonConstants.EMPTY_STRING;
	private String cReceivingAgent = CommonConstants.EMPTY_STRING;
	private String cReceivingAgentOption = CommonConstants.EMPTY_STRING;
	private String cBeneficiary = CommonConstants.EMPTY_STRING;
	private String cBeneficiaryOption = CommonConstants.EMPTY_STRING;

	private String dDeliveryAgent = CommonConstants.EMPTY_STRING;
	private String dDeliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String dIntermediary2 = CommonConstants.EMPTY_STRING;
	private String dIntermediary2Option = CommonConstants.EMPTY_STRING;
	private String dIntermediary = CommonConstants.EMPTY_STRING;
	private String dIntermediaryOption = CommonConstants.EMPTY_STRING;
	private String dReceivingAgent = CommonConstants.EMPTY_STRING;
	private String dReceivingAgentOption = CommonConstants.EMPTY_STRING;
	private String dBeneficiary = CommonConstants.EMPTY_STRING;
	private String dBeneficiaryOption = CommonConstants.EMPTY_STRING;

	private String eDeliveryAgent = CommonConstants.EMPTY_STRING;
	private String eDeliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String eIntermediary2 = CommonConstants.EMPTY_STRING;
	private String eIntermediary2Option = CommonConstants.EMPTY_STRING;
	private String eIntermediary = CommonConstants.EMPTY_STRING;
	private String eIntermediaryOption = CommonConstants.EMPTY_STRING;
	private String eReceivingAgent = CommonConstants.EMPTY_STRING;
	private String eReceivingAgentOption = CommonConstants.EMPTY_STRING;
	private String eBeneficiary = CommonConstants.EMPTY_STRING;
	private String eBeneficiaryOption = CommonConstants.EMPTY_STRING;

	private String fDeliveryAgent = CommonConstants.EMPTY_STRING;
	private String fDeliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String fIntermediary2 = CommonConstants.EMPTY_STRING;
	private String fIntermediary2Option = CommonConstants.EMPTY_STRING;
	private String fIntermediary = CommonConstants.EMPTY_STRING;
	private String fIntermediaryOption = CommonConstants.EMPTY_STRING;
	private String fReceivingAgent = CommonConstants.EMPTY_STRING;
	private String fReceivingAgentOption = CommonConstants.EMPTY_STRING;
	private String fBeneficiary = CommonConstants.EMPTY_STRING;
	private String fBeneficiaryOption = CommonConstants.EMPTY_STRING;

	private String taxRate = CommonConstants.EMPTY_STRING;
	private String transactionCcyAndNetIntAmt = CommonConstants.EMPTY_STRING;
	private String reportingCcyTaxAmount = CommonConstants.EMPTY_STRING;
	private String contactinformation = CommonConstants.EMPTY_STRING;
	private String dealingMethod = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyA = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyAOption = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyB = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyBOption = CommonConstants.EMPTY_STRING;
	private String counterPartysReference = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInfo = CommonConstants.EMPTY_STRING;
	private String paymentClearingCentre = CommonConstants.EMPTY_STRING;

	public String getPaymentClearingCentre() {
        return paymentClearingCentre;
    }

    public void setPaymentClearingCentre(String paymentClearingCentre) {
        this.paymentClearingCentre = paymentClearingCentre;
    }

    public UB_MT330() {

	}

	public String getAmountSettled() {
		return amountSettled;
	}

	public void setAmountSettled(String amountSettled) {
		this.amountSettled = amountSettled;
	}

	public String getCBeneficiary() {
		return cBeneficiary;
	}

	public void setCBeneficiary(String beneficiary) {
		cBeneficiary = beneficiary;
	}

	public String getCBeneficiaryOption() {
		return cBeneficiaryOption;
	}

	public void setCBeneficiaryOption(String beneficiaryOption) {
		cBeneficiaryOption = beneficiaryOption;
	}

	public String getCcyAndInterestAmount() {
		return ccyAndInterestAmount;
	}

	public void setCcyAndInterestAmount(String ccyAndInterestAmount) {
		this.ccyAndInterestAmount = ccyAndInterestAmount;
	}

	public String getCDeliveryAgent() {
		return cDeliveryAgent;
	}

	public void setCDeliveryAgent(String deliveryAgent) {
		cDeliveryAgent = deliveryAgent;
	}

	public String getCDeliveryAgentOption() {
		return cDeliveryAgentOption;
	}

	public void setCDeliveryAgentOption(String deliveryAgentOption) {
		cDeliveryAgentOption = deliveryAgentOption;
	}

	public String getCIntermediary() {
		return cIntermediary;
	}

	public void setCIntermediary(String intermediary) {
		cIntermediary = intermediary;
	}

	public String getCIntermediary2() {
		return cIntermediary2;
	}

	public void setCIntermediary2(String intermediary2) {
		cIntermediary2 = intermediary2;
	}

	public String getCIntermediary2Option() {
		return cIntermediary2Option;
	}

	public void setCIntermediary2Option(String intermediary2Option) {
		cIntermediary2Option = intermediary2Option;
	}

	public String getCIntermediaryOption() {
		return cIntermediaryOption;
	}

	public void setCIntermediaryOption(String intermediaryOption) {
		cIntermediaryOption = intermediaryOption;
	}

	public String getCommonReference() {
		return commonReference;
	}

	public void setCommonReference(String commonReference) {
		this.commonReference = commonReference;
	}

	public String getContactinformation() {
		return contactinformation;
	}

	public void setContactinformation(String contactinformation) {
		this.contactinformation = contactinformation;
	}

	public String getContractNumberPartyA() {
		return contractNumberPartyA;
	}

	public void setContractNumberPartyA(String contractNumberPartyA) {
		this.contractNumberPartyA = contractNumberPartyA;
	}

	public String getCounterPartysReference() {
		return counterPartysReference;
	}

	public void setCounterPartysReference(String counterPartysReference) {
		this.counterPartysReference = counterPartysReference;
	}

	public String getCReceivingAgent() {
		return cReceivingAgent;
	}

	public void setCReceivingAgent(String receivingAgent) {
		cReceivingAgent = receivingAgent;
	}

	public String getCReceivingAgentOption() {
		return cReceivingAgentOption;
	}

	public void setCReceivingAgentOption(String receivingAgentOption) {
		cReceivingAgentOption = receivingAgentOption;
	}

	public String getCurrencyBalance() {
		return currencyBalance;
	}

	public void setCurrencyBalance(String currencyBalance) {
		this.currencyBalance = currencyBalance;
	}

	public String getDayCountFraction() {
		return dayCountFraction;
	}

	public void setDayCountFraction(String dayCountFraction) {
		this.dayCountFraction = dayCountFraction;
	}

	public String getDBeneficiary() {
		return dBeneficiary;
	}

	public void setDBeneficiary(String beneficiary) {
		dBeneficiary = beneficiary;
	}

	public String getDBeneficiaryOption() {
		return dBeneficiaryOption;
	}

	public void setDBeneficiaryOption(String beneficiaryOption) {
		dBeneficiaryOption = beneficiaryOption;
	}

	public String getDDeliveryAgent() {
		return dDeliveryAgent;
	}

	public void setDDeliveryAgent(String deliveryAgent) {
		dDeliveryAgent = deliveryAgent;
	}

	public String getDDeliveryAgentOption() {
		return dDeliveryAgentOption;
	}

	public void setDDeliveryAgentOption(String deliveryAgentOption) {
		dDeliveryAgentOption = deliveryAgentOption;
	}

	public String getDealingBranchPartyA() {
		return dealingBranchPartyA;
	}

	public void setDealingBranchPartyA(String dealingBranchPartyA) {
		this.dealingBranchPartyA = dealingBranchPartyA;
	}

	public String getDealingBranchPartyAOption() {
		return dealingBranchPartyAOption;
	}

	public void setDealingBranchPartyAOption(String dealingBranchPartyAOption) {
		this.dealingBranchPartyAOption = dealingBranchPartyAOption;
	}

	public String getDealingBranchPartyB() {
		return dealingBranchPartyB;
	}

	public void setDealingBranchPartyB(String dealingBranchPartyB) {
		this.dealingBranchPartyB = dealingBranchPartyB;
	}

	public String getDealingBranchPartyBOption() {
		return dealingBranchPartyBOption;
	}

	public void setDealingBranchPartyBOption(String dealingBranchPartyBOption) {
		this.dealingBranchPartyBOption = dealingBranchPartyBOption;
	}

	public String getDealingMethod() {
		return dealingMethod;
	}

	public void setDealingMethod(String dealingMethod) {
		this.dealingMethod = dealingMethod;
	}

	public String getDIntermediary() {
		return dIntermediary;
	}

	public void setDIntermediary(String intermediary) {
		dIntermediary = intermediary;
	}

	public String getDIntermediary2() {
		return dIntermediary2;
	}

	public void setDIntermediary2(String intermediary2) {
		dIntermediary2 = intermediary2;
	}

	public String getDIntermediary2Option() {
		return dIntermediary2Option;
	}

	public void setDIntermediary2Option(String intermediary2Option) {
		dIntermediary2Option = intermediary2Option;
	}

	public String getDIntermediaryOption() {
		return dIntermediaryOption;
	}

	public void setDIntermediaryOption(String intermediaryOption) {
		dIntermediaryOption = intermediaryOption;
	}

	public String getDReceivingAgent() {
		return dReceivingAgent;
	}

	public void setDReceivingAgent(String receivingAgent) {
		dReceivingAgent = receivingAgent;
	}

	public String getDReceivingAgentOption() {
		return dReceivingAgentOption;
	}

	public void setDReceivingAgentOption(String receivingAgentOption) {
		dReceivingAgentOption = receivingAgentOption;
	}

	public String getEBeneficiary() {
		return eBeneficiary;
	}

	public void setEBeneficiary(String beneficiary) {
		eBeneficiary = beneficiary;
	}

	public String getEBeneficiaryOption() {
		return eBeneficiaryOption;
	}

	public void setEBeneficiaryOption(String beneficiaryOption) {
		eBeneficiaryOption = beneficiaryOption;
	}

	public String getEDeliveryAgent() {
		return eDeliveryAgent;
	}

	public void setEDeliveryAgent(String deliveryAgent) {
		eDeliveryAgent = deliveryAgent;
	}

	public String getEDeliveryAgentOption() {
		return eDeliveryAgentOption;
	}

	public void setEDeliveryAgentOption(String deliveryAgentOption) {
		eDeliveryAgentOption = deliveryAgentOption;
	}

	public String getEIntermediary() {
		return eIntermediary;
	}

	public void setEIntermediary(String intermediary) {
		eIntermediary = intermediary;
	}

	public String getEIntermediary2() {
		return eIntermediary2;
	}

	public void setEIntermediary2(String intermediary2) {
		eIntermediary2 = intermediary2;
	}

	public String getEIntermediary2Option() {
		return eIntermediary2Option;
	}

	public void setEIntermediary2Option(String intermediary2Option) {
		eIntermediary2Option = intermediary2Option;
	}

	public String getEIntermediaryOption() {
		return eIntermediaryOption;
	}

	public void setEIntermediaryOption(String intermediaryOption) {
		eIntermediaryOption = intermediaryOption;
	}

	public String getEReceivingAgent() {
		return eReceivingAgent;
	}

	public void setEReceivingAgent(String receivingAgent) {
		eReceivingAgent = receivingAgent;
	}

	public String getEReceivingAgentOption() {
		return eReceivingAgentOption;
	}

	public void setEReceivingAgentOption(String receivingAgentOption) {
		eReceivingAgentOption = receivingAgentOption;
	}

	public String getFBeneficiary() {
		return fBeneficiary;
	}

	public void setFBeneficiary(String beneficiary) {
		fBeneficiary = beneficiary;
	}

	public String getFBeneficiaryOption() {
		return fBeneficiaryOption;
	}

	public void setFBeneficiaryOption(String beneficiaryOption) {
		fBeneficiaryOption = beneficiaryOption;
	}

	public String getFDeliveryAgent() {
		return fDeliveryAgent;
	}

	public void setFDeliveryAgent(String deliveryAgent) {
		fDeliveryAgent = deliveryAgent;
	}

	public String getFDeliveryAgentOption() {
		return fDeliveryAgentOption;
	}

	public void setFDeliveryAgentOption(String deliveryAgentOption) {
		fDeliveryAgentOption = deliveryAgentOption;
	}

	public String getFIntermediary() {
		return fIntermediary;
	}

	public void setFIntermediary(String intermediary) {
		fIntermediary = intermediary;
	}

	public String getFIntermediary2() {
		return fIntermediary2;
	}

	public void setFIntermediary2(String intermediary2) {
		fIntermediary2 = intermediary2;
	}

	public String getFIntermediary2Option() {
		return fIntermediary2Option;
	}

	public void setFIntermediary2Option(String intermediary2Option) {
		fIntermediary2Option = intermediary2Option;
	}

	public String getFIntermediaryOption() {
		return fIntermediaryOption;
	}

	public void setFIntermediaryOption(String intermediaryOption) {
		fIntermediaryOption = intermediaryOption;
	}

	public String getFReceivingAgent() {
		return fReceivingAgent;
	}

	public void setFReceivingAgent(String receivingAgent) {
		fReceivingAgent = receivingAgent;
	}

	public String getFReceivingAgentOption() {
		return fReceivingAgentOption;
	}

	public void setFReceivingAgentOption(String receivingAgentOption) {
		fReceivingAgentOption = receivingAgentOption;
	}

	public String getFundOrInstPartyOption() {
		return fundOrInstPartyOption;
	}

	public void setFundOrInstPartyOption(String fundOrInstPartyOption) {
		this.fundOrInstPartyOption = fundOrInstPartyOption;
	}

	public String getFundOrInstructingParty() {
		return fundOrInstructingParty;
	}

	public void setFundOrInstructingParty(String fundOrInstructingParty) {
		this.fundOrInstructingParty = fundOrInstructingParty;
	}

	public String getInterestDueDate() {
		return interestDueDate;
	}

	public void setInterestDueDate(String interestDueDate) {
		this.interestDueDate = interestDueDate;
	}

	public String getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(String interestRate) {
		this.interestRate = interestRate;
	}

	public String getLastDayNextInterestPeriod() {
		return lastDayNextInterestPeriod;
	}

	public void setLastDayNextInterestPeriod(String lastDayNextInterestPeriod) {
		this.lastDayNextInterestPeriod = lastDayNextInterestPeriod;
	}

	public String getNumberOfDays() {
		return numberOfDays;
	}

	public void setNumberOfDays(String numberOfDays) {
		this.numberOfDays = numberOfDays;
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

	public String getPartyARole() {
		return partyARole;
	}

	public void setPartyARole(String partyARole) {
		this.partyARole = partyARole;
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

	public String getPeriodOfNotice() {
		return periodOfNotice;
	}

	public void setPeriodOfNotice(String periodOfNotice) {
		this.periodOfNotice = periodOfNotice;
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

	public String getScopeOfOperation() {
		return scopeOfOperation;
	}

	public void setScopeOfOperation(String scopeOfOperation) {
		this.scopeOfOperation = scopeOfOperation;
	}

	public String getSendersReference() {
		return sendersReference;
	}

	public void setSendersReference(String sendersReference) {
		this.sendersReference = sendersReference;
	}

	public String getSenderToReceiverInfo() {
		return senderToReceiverInfo;
	}

	public void setSenderToReceiverInfo(String senderToReceiverInfo) {
		this.senderToReceiverInfo = senderToReceiverInfo;
	}

	public String getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(String taxRate) {
		this.taxRate = taxRate;
	}

	public String getTermsAndConditions() {
		return termsAndConditions;
	}

	public void setTermsAndConditions(String termsAndConditions) {
		this.termsAndConditions = termsAndConditions;
	}

	public String getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}

	public String getTransactionCcyAndNetIntAmt() {
		return transactionCcyAndNetIntAmt;
	}

	public void setTransactionCcyAndNetIntAmt(String transactionCcyAndNetIntAmt) {
		this.transactionCcyAndNetIntAmt = transactionCcyAndNetIntAmt;
	}

	public String getTypeOfEvent() {
		return typeOfEvent;
	}

	public void setTypeOfEvent(String typeOfEvent) {
		this.typeOfEvent = typeOfEvent;
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
}
