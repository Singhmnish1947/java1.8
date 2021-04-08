/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/package com.misys.ub.swift;

import java.util.ArrayList;

import com.trapedza.bankfusion.core.CommonConstants;
/**
 * @author Gaurav Aggarwal
 * 
 */
public class UB_MT203 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	public String sumOfAmounts = CommonConstants.EMPTY_STRING;
	public String sendersCorrespondent = CommonConstants.EMPTY_STRING;
	public String sendersCorrespondentOption = CommonConstants.EMPTY_STRING;
	public String orderingInstitute = CommonConstants.EMPTY_STRING;
	public String orderingInstituteOption = CommonConstants.EMPTY_STRING;
	public String receiversCorrespondent = CommonConstants.EMPTY_STRING;
	public String receiversCorrespondentOption = CommonConstants.EMPTY_STRING;
	private String valueDate = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInformation = CommonConstants.EMPTY_STRING;
	private ArrayList MessageDetails = new ArrayList();
	
	
	  
	public UB_MT203() {

	}

	
	
	public String getReceiversCorrespondent() {
		return receiversCorrespondent;
	}

	public void setReceiversCorrespondent(String receiversCorrespondent) {
		this.receiversCorrespondent = receiversCorrespondent;
	}

	public String getReceiversCorrespondentOption() {
		return receiversCorrespondentOption;
	}

	public void setReceiversCorrespondentOption(String receiversCorrespondentOption) {
		this.receiversCorrespondentOption = receiversCorrespondentOption;
	}

	
	public String getSendersCorrespondent() {
		return sendersCorrespondent;
	}

	public void setSendersCorrespondent(String sendersCorrespondent) {
		this.sendersCorrespondent = sendersCorrespondent;
	}

	public String getSendersCorrespondentOption() {
		return sendersCorrespondentOption;
	}

	public void setSendersCorrespondentOption(String sendersCorrespondentOption) {
		this.sendersCorrespondentOption = sendersCorrespondentOption;
	}

	public String getSenderToReceiverInformation() {
		return senderToReceiverInformation;
	}

	public void setSenderToReceiverInformation(String senderToReceiverInformation) {
		this.senderToReceiverInformation = senderToReceiverInformation;
	}

	public String getValueDate() {
		return valueDate;
	}

	public void setValueDate(String valueDate) {
		this.valueDate = valueDate;
	}

	
	public ArrayList getMessageDetails() {
		return MessageDetails;
	}

	public void setMessageDetails(ArrayList messageDetails) {
		MessageDetails = messageDetails;
	}
	
	 public void addMessageDetails(UB_203Message_Details message_Details) {
		 MessageDetails.add(message_Details);
	   }
	 

	public String getOrderingInstitute() {
		return orderingInstitute;
	}



	public void setOrderingInstitute(String orderingInstitute) {
		this.orderingInstitute = orderingInstitute;
	}



	public String getOrderingInstituteOption() {
		return orderingInstituteOption;
	}



	public void setOrderingInstituteOption(String orderingInstituteOption) {
		this.orderingInstituteOption = orderingInstituteOption;
	}

	public String getSumOfAmounts() {
		return sumOfAmounts;
	}



	public void setSumOfAmounts(String sumOfAmounts) {
		this.sumOfAmounts = sumOfAmounts;
	}



	
	
}
