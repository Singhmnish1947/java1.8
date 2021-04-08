package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT200 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String transactionReferenceNumber = CommonConstants.EMPTY_STRING;
	private String sendersCorrespondent = CommonConstants.EMPTY_STRING;
	private String sendersCorresOption = CommonConstants.EMPTY_STRING;
	private String intermediary = CommonConstants.EMPTY_STRING;
	private String intermediaryOption = CommonConstants.EMPTY_STRING;
	private String accountWithInstitution = CommonConstants.EMPTY_STRING;
	private String accountWithInstOption = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInformation = CommonConstants.EMPTY_STRING;
	private String tdvalueDate = CommonConstants.EMPTY_STRING;
	private String tdcurrencyCode = CommonConstants.EMPTY_STRING;
	private String tdamount = CommonConstants.EMPTY_STRING;

	public UB_MT200() {
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

	public String getSendersCorresOption() {
		return sendersCorresOption;
	}

	public void setSendersCorresOption(String sendersCorresOption) {
		this.sendersCorresOption = sendersCorresOption;
	}

	public String getSendersCorrespondent() {
		return sendersCorrespondent;
	}

	public void setSendersCorrespondent(String sendersCorrespondent) {
		this.sendersCorrespondent = sendersCorrespondent;
	}

	public String getSenderToReceiverInformation() {
		return senderToReceiverInformation;
	}

	public void setSenderToReceiverInformation(String senderToReceiverInformation) {
		this.senderToReceiverInformation = senderToReceiverInformation;
	}

	public String getTdamount() {
		return tdamount;
	}

	public void setTdamount(String tdamount) {
		this.tdamount = tdamount;
	}

	public String getTdcurrencyCode() {
		return tdcurrencyCode;
	}

	public void setTdcurrencyCode(String tdcurrencyCode) {
		this.tdcurrencyCode = tdcurrencyCode;
	}

	public String getTdvalueDate() {
		return tdvalueDate;
	}

	public void setTdvalueDate(String tdvalueDate) {
		this.tdvalueDate = tdvalueDate;
	}

	public String getTransactionReferenceNumber() {
		return transactionReferenceNumber;
	}

	public void setTransactionReferenceNumber(String transactionReferenceNumber) {
		this.transactionReferenceNumber = transactionReferenceNumber;
	}

}
