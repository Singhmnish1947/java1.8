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

public class UB_MT940950 extends SwiftHeader {

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
	private String openingBalance = CommonConstants.EMPTY_STRING;
	private String openingBalanceOption = CommonConstants.EMPTY_STRING;
	private String closingBalance = CommonConstants.EMPTY_STRING;
	private String closingBalanceOption = CommonConstants.EMPTY_STRING;
	private String closingAvailableBalance = CommonConstants.EMPTY_STRING;
	private String infoToAccountOwner = CommonConstants.EMPTY_STRING;
	private ArrayList forwardAvailableBalance = new ArrayList();
	private ArrayList statementDetails = new ArrayList();
	private ArrayList statementSingleLine = new ArrayList();
    private String deliveryChannel = CommonConstants.EMPTY_STRING;


	public UB_MT940950() {
		// TODO Auto-generated constructor stub
	}

	public void addForwardBalance(ForwardBalanceInfo details) {
		this.forwardAvailableBalance.add(details);

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
	
	public String getClosingAvailableBalance() {
		return closingAvailableBalance;
	}

	public void setClosingAvailableBalance(String closingAvailableBalance) {
		this.closingAvailableBalance = closingAvailableBalance;
	}

	public String getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(String closingBalance) {
		this.closingBalance = closingBalance;
	}

	public String getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(String openingBalance) {
		this.openingBalance = openingBalance;
	}

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReference(String relatedReference) {
		this.relatedReference = relatedReference;
	}

	public ArrayList getStatementDetails() {
		return statementDetails;
	}

	public void setStatementDetails(ArrayList statementDetails) {
		this.statementDetails = statementDetails;
	}

	public String getStatementNumber() {
		return statementNumber;
	}

	public void setStatementNumber(String statementNumber) {
		this.statementNumber = statementNumber;
	}

	public ArrayList getStatementSingleLine() {
		return statementSingleLine;
	}

	public void setStatementSingleLine(ArrayList statementSingleLine) {
		this.statementSingleLine = statementSingleLine;
	}

	public String getTransactionReferenceNumber() {
		return transactionReferenceNumber;
	}

	public void setTransactionReferenceNumber(String transactionReferenceNumber) {
		this.transactionReferenceNumber = transactionReferenceNumber;
	}

	public void addStatement(StatementInfo statementInfo) {
		statementDetails.add(statementInfo);
	}

	public void addSingleLine(StatementSingleInfo oneLine) {
		statementSingleLine.add(oneLine);
	}

	public ArrayList getForwardAvailableBalance() {
		return forwardAvailableBalance;
	}

	public void setForwardAvailableBalance(ArrayList forwardAvailableBalance) {
		this.forwardAvailableBalance = forwardAvailableBalance;
	}

	public String getInfoToAccountOwner() {
		return infoToAccountOwner;
	}

	public void setInfoToAccountOwner(String infoToAccountOwner) {
		this.infoToAccountOwner = infoToAccountOwner;
	}

	public String getClosingBalanceOption() {
		return closingBalanceOption;
	}

	public void setClosingBalanceOption(String closingBalanceOption) {
		this.closingBalanceOption = closingBalanceOption;
	}

	public String getOpeningBalanceOption() {
		return openingBalanceOption;
	}

	public void setOpeningBalanceOption(String openingBalanceOption) {
		this.openingBalanceOption = openingBalanceOption;
	}

	public String getDeliveryChannel() {
		return deliveryChannel;
	}

	public void setDeliveryChannel(String deliveryChannel) {
		this.deliveryChannel = deliveryChannel;
	}
}
