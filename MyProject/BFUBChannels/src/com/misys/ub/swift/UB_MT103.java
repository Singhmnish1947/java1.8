package com.misys.ub.swift;

import java.util.ArrayList;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT103 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String sendersReference = CommonConstants.EMPTY_STRING;
	private String bankOperationCode = CommonConstants.EMPTY_STRING;
	private ArrayList instruction = new ArrayList();

	private String transactionTypeCode = CommonConstants.EMPTY_STRING;
	private String tdValueDate = CommonConstants.EMPTY_STRING;
	private String tdCurrencyCode = CommonConstants.EMPTY_STRING;
	private String tdAmount = CommonConstants.EMPTY_STRING;
	private String instructedAmount = CommonConstants.EMPTY_STRING;
	private String instructedCurrency = CommonConstants.EMPTY_STRING;
	private String exchangeRate = CommonConstants.EMPTY_STRING;
	private String orderingCustomer = CommonConstants.EMPTY_STRING;
	private String orderingCustomerOption = CommonConstants.EMPTY_STRING;
	private String sendingInstitution = CommonConstants.EMPTY_STRING;
	private String orderingInstitution = CommonConstants.EMPTY_STRING;
	private String orderInstitutionOption = CommonConstants.EMPTY_STRING;
	private String sendersCorrespondent = CommonConstants.EMPTY_STRING;
	private String sendersCorrespOption = CommonConstants.EMPTY_STRING;
	private String receiversCorrespondent = CommonConstants.EMPTY_STRING;
	private String receiversCorrespOption = CommonConstants.EMPTY_STRING;
	private String thirdReimbursementInstitution = CommonConstants.EMPTY_STRING;
	private String thirdReimbursementInstOption = CommonConstants.EMPTY_STRING;
	private String intermediaryInstitution = CommonConstants.EMPTY_STRING;
	private String intermediaryInstOption = CommonConstants.EMPTY_STRING;
	private String accountWithInstitution = CommonConstants.EMPTY_STRING;
	private String accountWithInstOption = CommonConstants.EMPTY_STRING;
	private String beneficiaryCustomer = CommonConstants.EMPTY_STRING;
	private String beneficiaryCustOption = CommonConstants.EMPTY_STRING;
	private String remittanceInfo = CommonConstants.EMPTY_STRING;
	private String detailsOfCharges = CommonConstants.EMPTY_STRING;
	private ArrayList charges = new ArrayList();
	private String receiversCharges = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInfo = CommonConstants.EMPTY_STRING;
	private String regulatoryReporting = CommonConstants.EMPTY_STRING;
	private String envelopeContents = CommonConstants.EMPTY_STRING;
	private String Stp = CommonConstants.EMPTY_STRING;
	private String transactionID = CommonConstants.EMPTY_STRING;
	private String serviceTypeId = CommonConstants.EMPTY_STRING;
	private String endtoendTxnRef = CommonConstants.EMPTY_STRING;
	
	public UB_MT103() {
		// TODO Auto-generated constructor stub
	}

	public String getAccountWithInstitution() {
		return accountWithInstitution;
	}

	public void setAccountWithInstitution(String accountWithInstitution) {
		this.accountWithInstitution = accountWithInstitution;
	}

	public String getAccountWithInstOption() {
		return accountWithInstOption;
	}

	public void setAccountWithInstOption(String accountWithInstOption) {
		this.accountWithInstOption = accountWithInstOption;
	}

	public String getBankOperationCode() {
		return bankOperationCode;
	}

	public void setBankOperationCode(String bankOperationCode) {
		this.bankOperationCode = bankOperationCode;
	}

	public String getBeneficiaryCustomer() {
		return beneficiaryCustomer;
	}

	public void setBeneficiaryCustomer(String beneficiaryCustomer) {
		this.beneficiaryCustomer = beneficiaryCustomer;
	}

	public String getBeneficiaryCustOption() {
		return beneficiaryCustOption;
	}

	public void setBeneficiaryCustOption(String beneficiaryCustOption) {
		this.beneficiaryCustOption = beneficiaryCustOption;
	}

	public ArrayList getCharges() {
		return charges;
	}

	public void setCharges(ArrayList charges) {
		this.charges = charges;
	}

	public String getDetailsOfCharges() {
		return detailsOfCharges;
	}

	public void setDetailsOfCharges(String detailsOfCharges) {
		this.detailsOfCharges = detailsOfCharges;
	}

	public String getEnvelopeContents() {
		return envelopeContents;
	}

	public void setEnvelopeContents(String envelopeContents) {
		this.envelopeContents = envelopeContents;
	}

	public String getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(String exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getInstructedAmount() {
		return instructedAmount;
	}

	public void setInstructedAmount(String instructedAmount) {
		this.instructedAmount = instructedAmount;
	}

	public String getInstructedCurrency() {
		return instructedCurrency;
	}

	public void setInstructedCurrency(String instructedCurrency) {
		this.instructedCurrency = instructedCurrency;
	}

	public ArrayList getInstruction() {
		return instruction;
	}

	public void setInstruction(ArrayList instruction) {
		this.instruction = instruction;
	}

	public String getIntermediaryInstitution() {
		return intermediaryInstitution;
	}

	public void setIntermediaryInstitution(String intermediaryInstitution) {
		this.intermediaryInstitution = intermediaryInstitution;
	}

	public String getIntermediaryInstOption() {
		return intermediaryInstOption;
	}

	public void setIntermediaryInstOption(String intermediaryInstOption) {
		this.intermediaryInstOption = intermediaryInstOption;
	}

	public String getOrderingCustomer() {
		return orderingCustomer;
	}

	public void setOrderingCustomer(String orderingCustomer) {
		this.orderingCustomer = orderingCustomer;
	}

	public String getOrderingCustomerOption() {
		return orderingCustomerOption;
	}

	public void setOrderingCustomerOption(String orderingCustomerOption) {
		this.orderingCustomerOption = orderingCustomerOption;
	}

	public String getOrderingInstitution() {
		return orderingInstitution;
	}

	public void setOrderingInstitution(String orderingInstitution) {
		this.orderingInstitution = orderingInstitution;
	}

	public String getOrderInstitutionOption() {
		return orderInstitutionOption;
	}

	public void setOrderInstitutionOption(String orderInstitutionOption) {
		this.orderInstitutionOption = orderInstitutionOption;
	}

	public String getReceiversCharges() {
		return receiversCharges;
	}

	public void setReceiversCharges(String receiversCharges) {
		this.receiversCharges = receiversCharges;
	}

	public String getReceiversCorrespondent() {
		return receiversCorrespondent;
	}

	public void setReceiversCorrespondent(String receiversCorrespondent) {
		this.receiversCorrespondent = receiversCorrespondent;
	}

	public String getReceiversCorrespOption() {
		return receiversCorrespOption;
	}

	public void setReceiversCorrespOption(String receiversCorrespOption) {
		this.receiversCorrespOption = receiversCorrespOption;
	}

	public String getRegulatoryReporting() {
		return regulatoryReporting;
	}

	public void setRegulatoryReporting(String regulatoryReporting) {
		this.regulatoryReporting = regulatoryReporting;
	}

	public String getRemittanceInfo() {
		return remittanceInfo;
	}

	public void setRemittanceInfo(String remittanceInfo) {
		this.remittanceInfo = remittanceInfo;
	}

	public String getSendersCorrespondent() {
		return sendersCorrespondent;
	}

	public void setSendersCorrespondent(String sendersCorrespondent) {
		this.sendersCorrespondent = sendersCorrespondent;
	}

	public String getSendersCorrespOption() {
		return sendersCorrespOption;
	}

	public void setSendersCorrespOption(String sendersCorrespOption) {
		this.sendersCorrespOption = sendersCorrespOption;
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

	public String getSendingInstitution() {
		return sendingInstitution;
	}

	public void setSendingInstitution(String sendingInstitution) {
		this.sendingInstitution = sendingInstitution;
	}

	public String getTdAmount() {
		return tdAmount;
	}

	public void setTdAmount(String tdAmount) {
		this.tdAmount = tdAmount;
	}

	public String getTdCurrencyCode() {
		return tdCurrencyCode;
	}

	public void setTdCurrencyCode(String tdCurrencyCode) {
		this.tdCurrencyCode = tdCurrencyCode;
	}

	public String getTdValueDate() {
		return tdValueDate;
	}

	public void setTdValueDate(String tdValueDate) {
		this.tdValueDate = tdValueDate;
	}

	public String getThirdReimbursementInstitution() {
		return thirdReimbursementInstitution;
	}

	public void setThirdReimbursementInstitution(String thirdReimbursementInstitution) {
		this.thirdReimbursementInstitution = thirdReimbursementInstitution;
	}

	public String getThirdReimbursementInstOption() {
		return thirdReimbursementInstOption;
	}

	public void setThirdReimbursementInstOption(String thirdReimbursementInstOption) {
		this.thirdReimbursementInstOption = thirdReimbursementInstOption;
	}

	public String getTransactionTypeCode() {
		return transactionTypeCode;
	}

	public void setTransactionTypeCode(String transactionTypeCode) {
		this.transactionTypeCode = transactionTypeCode;
	}

	public void addCharges(SendersCharges charge) {
		this.charges.add(charge);
	}

	public void addInstruction(InstructionCode instructionCode) {
		this.instruction.add(instructionCode);
	}
	public String getStp() {
		return Stp;
	}

	public void setStp(String stp) {
		this.Stp = stp;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}
	
	public String getServiceTypeId() {
		return serviceTypeId;
	}

	public void setServiceTypeId(String serviceTypeId) {
		this.serviceTypeId = serviceTypeId;
	}

	public String getEndtoendTxnRef() {
		return endtoendTxnRef;
	}

	public void setEndtoendTxnRef(String endtoendTxnRef) {
		this.endtoendTxnRef = endtoendTxnRef;
	}
	
}
