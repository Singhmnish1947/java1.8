package com.misys.ub.swift;

import java.util.ArrayList;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT110 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String sendersReference = CommonConstants.EMPTY_STRING;
	private String sendersCorrespondent = CommonConstants.EMPTY_STRING;
	private String sendersCorrespOption = CommonConstants.EMPTY_STRING;
	private String receiversCorrespondent = CommonConstants.EMPTY_STRING;
	private String receiversCorrespOption = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInformation = CommonConstants.EMPTY_STRING;
	private String orderingCustomer = CommonConstants.EMPTY_STRING;
	private String orderingCustomerOption = CommonConstants.EMPTY_STRING;
	private String payeeOption = CommonConstants.EMPTY_STRING;
	
	
	
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

	public String getPayeeOption() {
		return payeeOption;
	}

	public void setPayeeOption(String payeeOption) {
		this.payeeOption = payeeOption;
	}

	private ArrayList chequeDetails = new ArrayList();

	public UB_MT110() {

	}

	public void addDetails(ChequeInfo details) {
		this.chequeDetails.add(details);

	}

	public String getReceiversCorrespondent() {
		return receiversCorrespondent;
	}

	public void setReceiversCorrespondent(String receiversCorrespondent) {
		this.receiversCorrespondent = receiversCorrespondent;
	}

	public String getSendersCorrespondent() {
		return sendersCorrespondent;
	}

	public void setSendersCorrespondent(String sendersCorrespondent) {
		this.sendersCorrespondent = sendersCorrespondent;
	}

	public String getSendersReference() {
		return sendersReference;
	}

	public void setSendersReference(String sendersReference) {
		this.sendersReference = sendersReference;
	}

	public String getSenderToReceiverInformation() {
		return senderToReceiverInformation;
	}

	public void setSenderToReceiverInformation(String senderToReceiverInformation) {
		this.senderToReceiverInformation = senderToReceiverInformation;
	}

	public ArrayList getChequeDetails() {
		return chequeDetails;
	}

	public void setChequeDetails(ArrayList chequeDetails) {
		this.chequeDetails = chequeDetails;
	}

	public String getReceiversCorrespOption() {
		return receiversCorrespOption;
	}

	public void setReceiversCorrespOption(String receiversCorrespOption) {
		this.receiversCorrespOption = receiversCorrespOption;
	}

	public String getSendersCorrespOption() {
		return sendersCorrespOption;
	}

	public void setSendersCorrespOption(String sendersCorrespOption) {
		this.sendersCorrespOption = sendersCorrespOption;
	}

}
