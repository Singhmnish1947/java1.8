package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT210 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public UB_MT210() {
	}

	public String transactionReferenceNumber = CommonConstants.EMPTY_STRING;
	public String accountIdentification = CommonConstants.EMPTY_STRING;
	public String valueDate = CommonConstants.EMPTY_STRING;
	public String relatedReference = CommonConstants.EMPTY_STRING;
	public String currencyCodeAmount = CommonConstants.EMPTY_STRING;
	public String orderingCustomer = CommonConstants.EMPTY_STRING;
	public String orderingCustomerOption = CommonConstants.EMPTY_STRING;
	public String orderingInstitution = CommonConstants.EMPTY_STRING;
	public String orderingInstitutionOption = CommonConstants.EMPTY_STRING;
	public String intermediary = CommonConstants.EMPTY_STRING;
	public String intermediaryOption = CommonConstants.EMPTY_STRING;

	public String getAccountIdentification() {
		return accountIdentification;
	}

	public void setAccountIdentification(String accountIdentification) {
		this.accountIdentification = accountIdentification;
	}

	public String getCurrencyCodeAmount() {
		return currencyCodeAmount;
	}

	public void setCurrencyCodeAmount(String currencyCodeAmount) {
		this.currencyCodeAmount = currencyCodeAmount;
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

	public String getOrderingInstitutionOption() {
		return orderingInstitutionOption;
	}

	public void setOrderingInstitutionOption(String orderingInstitutionOption) {
		this.orderingInstitutionOption = orderingInstitutionOption;
	}

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReference(String relatedReference) {
		this.relatedReference = relatedReference;
	}

	public String getTransactionReferenceNumber() {
		return transactionReferenceNumber;
	}

	public void setTransactionReferenceNumber(String transactionReferenceNumber) {
		this.transactionReferenceNumber = transactionReferenceNumber;
	}

	public String getValueDate() {
		return valueDate;
	}

	public void setValueDate(String valueDate) {
		this.valueDate = valueDate;
	}

}
