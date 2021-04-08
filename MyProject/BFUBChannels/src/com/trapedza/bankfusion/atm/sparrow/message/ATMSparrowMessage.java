/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMSparrowMessage.java,v $
 * Revision 1.5  2008/08/12 20:14:52  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.3.4.1  2008/07/03 17:55:27  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.4  2008/06/12 10:50:11  arun
 *  RIO on Head
 *
 * Revision 1.3  2007/11/30 12:49:11  prashantk
 * Removed Warnings
 *
 * Revision 1.2  2007/11/14 11:05:29  prashantk
 * ATM Financial Messages
 */
package com.trapedza.bankfusion.atm.sparrow.message;

import java.sql.Timestamp;

/**
 * The ATMSparrowMessage stores the ATM Sparrow header messages.
 */
public class ATMSparrowMessage {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */

	/**
	 * Holds the reference for logger object
	 */

	private String authorisedFlag;
	private String cardDestBranchCode;
	private String cardDestCountryCode;
	private String cardDestinationIMD;
	private String cardNumber;
	private String cardSequenceNo;
	//private String date;
	//	private String time;

	private Timestamp dateTimeofTxn;
	private String destinationMailBox;
	private String deviceId;
	private String errorCode;
	private String errorDescription;
	private String forcePost;
	private String messageType;
	private String sourceBranchCode;
	private String sourceCountryCode;
	private String sourceIMD;
	private String sourceMailBox;

	private String transactionType;
	private String txnSequenceNo;
	private String txnDescription;
	private String txnCustomerNarrative;
	private String txnContraNarrative;
	private String extVersion;
	
	public String getTxnCustomerNarrative() {
		return txnCustomerNarrative;
	}

	public void setTxnCustomerNarrative(String txnCustomerNarrative) {
		this.txnCustomerNarrative = txnCustomerNarrative;
	}

	public String getTxnContraNarrative() {
		return txnContraNarrative;
	}

	public void setTxnContraNarrative(String txnContraNarrative) {
		this.txnContraNarrative = txnContraNarrative;
	}

	public String getAuthorisedFlag() {
		return authorisedFlag;
	}

	public void setAuthorisedFlag(String authorisedFlag) {
		this.authorisedFlag = authorisedFlag;
	}

	public String getCardDestBranchCode() {
		return cardDestBranchCode;
	}

	public void setCardDestBranchCode(String cardDestBranchCode) {
		this.cardDestBranchCode = cardDestBranchCode;
	}

	public String getCardDestCountryCode() {
		return cardDestCountryCode;
	}

	public void setCardDestCountryCode(String cardDestCountryCode) {
		this.cardDestCountryCode = cardDestCountryCode;
	}

	public String getCardDestinationIMD() {
		return cardDestinationIMD;
	}

	public void setCardDestinationIMD(String cardDestinationIMD) {
		this.cardDestinationIMD = cardDestinationIMD;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCardSequenceNo() {
		return cardSequenceNo;
	}

	public void setCardSequenceNo(String cardSequenceNo) {
		this.cardSequenceNo = cardSequenceNo;
	}

	public String getDestinationMailBox() {
		return destinationMailBox;
	}

	public void setDestinationMailBox(String destinationMailBox) {
		this.destinationMailBox = destinationMailBox;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getForcePost() {
		return forcePost;
	}

	public void setForcePost(String forcePost) {
		this.forcePost = forcePost;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getSourceBranchCode() {
		return sourceBranchCode;
	}

	public void setSourceBranchCode(String sourceBranchCode) {
		this.sourceBranchCode = sourceBranchCode;
	}

	public String getSourceCountryCode() {
		return sourceCountryCode;
	}

	public void setSourceCountryCode(String sourceCountryCode) {
		this.sourceCountryCode = sourceCountryCode;
	}

	public String getSourceIMD() {
		return sourceIMD;
	}

	public void setSourceIMD(String sourceIMD) {
		this.sourceIMD = sourceIMD;
	}

	public String getSourceMailBox() {
		return sourceMailBox;
	}

	public void setSourceMailBox(String sourceMailBox) {
		this.sourceMailBox = sourceMailBox;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getTxnSequenceNo() {
		return txnSequenceNo;
	}

	public void setTxnSequenceNo(String txnSequenceNo) {
		this.txnSequenceNo = txnSequenceNo;
	}

	public String getTxnDescription() {
		return txnDescription;
	}

	public void setTxnDescription(String txnDescription) {
		this.txnDescription = txnDescription;
	}

	public Timestamp getDateTimeofTxn() {
		return dateTimeofTxn;

	}
	
	public void setExtVersion(String extVersion) {
		this.extVersion = extVersion;

	}
	public String getExtVersion() {
		return extVersion;
	}
	public void setDateTimeofTxn(Timestamp dateTimeofTxn) {
		this.dateTimeofTxn = dateTimeofTxn;
	}
	/*public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}*/
}
