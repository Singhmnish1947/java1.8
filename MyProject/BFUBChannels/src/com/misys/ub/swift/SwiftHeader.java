/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.misys.ub.swift;

import java.io.Serializable;

import com.trapedza.bankfusion.core.CommonConstants;
/**
 * @author Gaurav Aggarwal
 * 
 */
public class SwiftHeader implements Serializable {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String MessageType = CommonConstants.EMPTY_STRING;
	private String DisposalRef = CommonConstants.EMPTY_STRING;
	private String Sender = CommonConstants.EMPTY_STRING;
	private String Receiver = CommonConstants.EMPTY_STRING;
	private String VerificationRequired = CommonConstants.EMPTY_STRING;
	private String MultipleHold = CommonConstants.EMPTY_STRING;
	private String Branch = CommonConstants.EMPTY_STRING;
	private String Action = CommonConstants.EMPTY_STRING;
	private String internalRef=CommonConstants.EMPTY_STRING;
	private String messageId="0";
	private String serviceIdentifierId =CommonConstants.EMPTY_STRING;
	
	public String getMessageType() {
		return MessageType;
	}
	public void setMessageType(String messageType) {
		MessageType = messageType;
	}
	public String getDisposalRef() {
		return DisposalRef;
	}
	public void setDisposalRef(String disposalRef) {
		DisposalRef = disposalRef;
	}
	public String getSender() {
		return Sender;
	}
	public void setSender(String sender) {
		Sender = sender;
	}
	public String getReceiver() {
		return Receiver;
	}
	public void setReceiver(String receiver) {
		Receiver = receiver;
	}
	public String getVerificationRequired() {
		return VerificationRequired;
	}
	public void setVerificationRequired(String verificationRequired) {
		VerificationRequired = verificationRequired;
	}
	public String getMultipleHold() {
		return MultipleHold;
	}
	public void setMultipleHold(String multipleHold) {
		MultipleHold = multipleHold;
	}
	public String getBranch() {
		return Branch;
	}
	public void setBranch(String branch) {
		Branch = branch;
	}
	public String getAction() {
		return Action;
	}
	public void setAction(String action) {
		Action = action;
	}
	public String getInternalRef() {
		return internalRef;
	}
	public void setInternalRef(String internalRef) {
		this.internalRef = internalRef;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getServiceIdentifierId() {
		return serviceIdentifierId;
	}
	public void setServiceIdentifierId(String serviceIdentifierId) {
		this.serviceIdentifierId = serviceIdentifierId;
	}
	
	
	

}
