/**
 * * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 
 */
package com.misys.ub.swift;
/**
 * @author Gaurav.Aggarwal
 *
 */
import java.util.ArrayList;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT942 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String transactionReferenceNumber = CommonConstants.EMPTY_STRING;
	private String relatedReference = CommonConstants.EMPTY_STRING;
	private String accountIdentification = CommonConstants.EMPTY_STRING;
	private String accountIdentificationP = CommonConstants.EMPTY_STRING;
	private String statementNumber = CommonConstants.EMPTY_STRING;
	private String floorLimitIndicator1 = CommonConstants.EMPTY_STRING;
	private String floorLimitIndicator2 = CommonConstants.EMPTY_STRING;
	private String dateTimeIndicator = CommonConstants.EMPTY_STRING;
	private ArrayList transactionDetails = new ArrayList();
	private String numberSumEntriesDebit = CommonConstants.EMPTY_STRING;
	private String numberSumEntriesCredit = CommonConstants.EMPTY_STRING;
	private String infoToAccountOwner = CommonConstants.EMPTY_STRING;
    private String deliveryChannel = CommonConstants.EMPTY_STRING;

	public UB_MT942() {
		// TODO Auto-generated constructor stub
	}

	public void addDetails(StatementInfo details) {
		this.transactionDetails.add(details);

	}

	public String getTransactionReferenceNumber() {
		return transactionReferenceNumber;
	}

	public void setTransactionReferenceNumber(String transactionReferenceNumber) {
		this.transactionReferenceNumber = transactionReferenceNumber;
	}

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReference(String relatedReference) {
		this.relatedReference = relatedReference;
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
	

	public String getDateTimeIndicator() {
		return dateTimeIndicator;
	}

	public void setDateTimeIndicator(String dateTimeIndicator) {
		this.dateTimeIndicator = dateTimeIndicator;
	}

	public String getNumberSumEntriesCredit() {
		return numberSumEntriesCredit;
	}

	public void setNumberSumEntriesCredit(String numberSumEntriesCredit) {
		this.numberSumEntriesCredit = numberSumEntriesCredit;
	}

	public String getNumberSumEntriesDebit() {
		return numberSumEntriesDebit;
	}

	public void setNumberSumEntriesDebit(String numberSumEntriesDebit) {
		this.numberSumEntriesDebit = numberSumEntriesDebit;
	}

	public String getStatementNumber() {
		return statementNumber;
	}

	public void setStatementNumber(String statementNumber) {
		this.statementNumber = statementNumber;
	}

	public String getFloorLimitIndicator1() {
		return floorLimitIndicator1;
	}

	public void setFloorLimitIndicator1(String floorLimitIndicator1) {
		this.floorLimitIndicator1 = floorLimitIndicator1;
	}

	public String getFloorLimitIndicator2() {
		return floorLimitIndicator2;
	}

	public void setFloorLimitIndicator2(String floorLimitIndicator2) {
		this.floorLimitIndicator2 = floorLimitIndicator2;
	}

	public String getInfoToAccountOwner() {
		return infoToAccountOwner;
	}

	public void setInfoToAccountOwner(String infoToAccountOwner) {
		this.infoToAccountOwner = infoToAccountOwner;
	}

	public ArrayList getTransactionDetails() {
		return transactionDetails;
	}

	public void setTransactionDetails(ArrayList transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

	public String getDeliveryChannel() {
		return deliveryChannel;
	}

	public void setDeliveryChannel(String deliveryChannel) {
		this.deliveryChannel = deliveryChannel;
	}

}
