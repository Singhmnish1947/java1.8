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
public class UB_MT201 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	public String sumOfAmounts = CommonConstants.EMPTY_STRING;
	public String sendersCorrespondent = CommonConstants.EMPTY_STRING;
	private String valueDate = CommonConstants.EMPTY_STRING;
	private String senderToRecvInfo = CommonConstants.EMPTY_STRING;
	private ArrayList MessageDetails = new ArrayList();
		  
	public UB_MT201() {

	}

	
	
	public String getSendersCorrespondent() {
		return sendersCorrespondent;
	}

	public void setSendersCorrespondent(String sendersCorrespondent) {
		this.sendersCorrespondent = sendersCorrespondent;
	}

	

	
	public String getValueDate() {
		return valueDate;
	}

	public void setValueDate(String valueDate) {
		this.valueDate = valueDate;
	}

	
	public void addMT201MessageDetails(UB_201Message_Details message_Details) {
		 MessageDetails.add(message_Details);
	   }

	public String getSenderToRecvInfo() {
		return senderToRecvInfo;
	}



	public void setSenderToRecvInfo(String senderToRecvInfo) {
		this.senderToRecvInfo = senderToRecvInfo;
	}


	public String getSumOfAmounts() {
		return sumOfAmounts;
	}



	public void setSumOfAmounts(String sumOfAmounts) {
		this.sumOfAmounts = sumOfAmounts;
	}



	public ArrayList getMessageDetails() {
		return MessageDetails;
	}



	public void setMessageDetails(ArrayList messageDetails) {
		MessageDetails = messageDetails;
	}



	

	
	
}
