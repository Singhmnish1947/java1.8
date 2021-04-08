package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT900910 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String transactionReference = CommonConstants.EMPTY_STRING;
	private String relatedReference = CommonConstants.EMPTY_STRING;
	private String accountIdentification = CommonConstants.EMPTY_STRING;
	private String accountIdentificationP = CommonConstants.EMPTY_STRING;
	private String orderingInstitution = CommonConstants.EMPTY_STRING;
	private String orderingInstOption = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInformation = CommonConstants.EMPTY_STRING;
	private String tdValueDate = CommonConstants.EMPTY_STRING;
	private String tdCurrencyCode = CommonConstants.EMPTY_STRING;
	private String tdAmount = CommonConstants.EMPTY_STRING;
	private String orderingCustomer = CommonConstants.EMPTY_STRING;
	private String orderingCustOption = CommonConstants.EMPTY_STRING;
	private String intermediary = CommonConstants.EMPTY_STRING;
	private String intermediaryOption = CommonConstants.EMPTY_STRING;
	private String tdDateTime = CommonConstants.EMPTY_STRING;

	public UB_MT900910() {
		// TODO Auto-generated constructor stub
	}

	public String getAccountIdentification() {
		return accountIdentification;
	}

	public void setAccountIdentification(String accountIdentification) {
		this.accountIdentification = accountIdentification;
	}

	public String getAccountIdentificationP() {
		return accountIdentificationP;
	}

	public void setAccountIdentificationP(String accountIdentificationP) {
		this.accountIdentificationP = accountIdentificationP;
	}
	
	public String getIntermediary() {
		return intermediary;
	}

	public void setIntermediary(String intermediary) {
		this.intermediary = intermediary;
	}

	public String getIntermediaryOption() {
		return intermediaryOption;
	}

	public void setIntermediaryOption(String intermediaryOption) {
		this.intermediaryOption = intermediaryOption;
	}

	public String getOrderingCustomer() {
		return orderingCustomer;
	}

	public void setOrderingCustomer(String orderingCustomer) {
		this.orderingCustomer = orderingCustomer;
	}

	public String getOrderingCustOption() {
		return orderingCustOption;
	}

	public void setOrderingCustOption(String orderingCustOption) {
		this.orderingCustOption = orderingCustOption;
	}

	public String getOrderingInstitution() {
		return orderingInstitution;
	}

	public void setOrderingInstitution(String orderingInstitution) {
		this.orderingInstitution = orderingInstitution;
	}

	public String getOrderingInstOption() {
		return orderingInstOption;
	}

	public void setOrderingInstOption(String orderingInstOption) {
		this.orderingInstOption = orderingInstOption;
	}

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReference(String relatedReference) {
		this.relatedReference = relatedReference;
	}

	public String getSenderToReceiverInformation() {
		return senderToReceiverInformation;
	}

	public void setSenderToReceiverInformation(String senderToReceiverInformation) {
		this.senderToReceiverInformation = senderToReceiverInformation;
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

	public String getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(String transactionReference) {
		this.transactionReference = transactionReference;
	}

	public String getTdDateTime() {
		return tdDateTime;
	}

	public void setTdDateTime(String tdDateTime) {
		this.tdDateTime = tdDateTime;
	}

}
