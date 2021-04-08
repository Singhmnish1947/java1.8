/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMExNwMessage.java,v $
 * Revision 1.5  2008/08/12 20:14:52  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.3.4.1  2008/07/03 17:55:26  vivekr
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
 *
 *
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message;

/**
 * The ATMExNwMessage stores the ATM Sparrow External Network Messages.
 */
public class ATMExNwMessage extends ATMSparrowFinancialMessage {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public ATMExNwMessage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 */

	/**
	 * Holds the reference for logger object
	 */

	private String acquiringInstitutionID;
	private String actualBalance;
	private String authorisationCode;
	private String availableBalance;
	private String cardAcceptorID;
	private String cardAcceptorName;
	private String cardAcceptorTerminalID;
	private String cashBackAccount;
	private String cashBackAmount;
	private String cashBackDevice;
	private String conversionRate;
	private String currencyCode;
	private String extensionNumber;
	private String externalNetworkID;
	private String forwardingInstitutionID;
	private String merchantCategoryCode;
	private String settlementCurrency;
	private String settlementAmount;
	private String settlementConvRate;
	private String transactionAmount;

	public String getAcquiringInstitutionID() {
		return acquiringInstitutionID;
	}

	public void setAcquiringInstitutionID(String acquiringInstitutionID) {
		this.acquiringInstitutionID = acquiringInstitutionID;
	}

	public String getActualBalance() {
		return actualBalance;
	}

	public void setActualBalance(String actualBalance) {
		this.actualBalance = actualBalance;
	}

	public String getAuthorisationCode() {
		return authorisationCode;
	}

	public void setAuthorisationCode(String authorisationCode) {
		this.authorisationCode = authorisationCode;
	}

	public String getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(String availableBalance) {
		this.availableBalance = availableBalance;
	}

	public String getCardAcceptorID() {
		return cardAcceptorID;
	}

	public void setCardAcceptorID(String cardAcceptorID) {
		this.cardAcceptorID = cardAcceptorID;
	}

	public String getCardAcceptorName() {
		return cardAcceptorName;
	}

	public void setCardAcceptorName(String cardAcceptorName) {
		this.cardAcceptorName = cardAcceptorName;
	}

	public String getCardAcceptorTerminalID() {
		return cardAcceptorTerminalID;
	}

	public void setCardAcceptorTerminalID(String cardAcceptorTerminalID) {
		this.cardAcceptorTerminalID = cardAcceptorTerminalID;
	}

	public String getCashBackAccount() {
		return cashBackAccount;
	}

	public void setCashBackAccount(String cashBackAccount) {
		this.cashBackAccount = cashBackAccount;
	}

	public String getCashBackAmount() {
		return cashBackAmount;
	}

	public void setCashBackAmount(String cashBackAmount) {
		this.cashBackAmount = cashBackAmount;
	}

	public String getCashBackDevice() {
		return cashBackDevice;
	}

	public void setCashBackDevice(String cashBackDevice) {
		this.cashBackDevice = cashBackDevice;
	}

	public String getConversionRate() {
		return conversionRate;
	}

	public void setConversionRate(String conversionRate) {
		this.conversionRate = conversionRate;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getExtensionNumber() {
		return extensionNumber;
	}

	public void setExtensionNumber(String extensionNumber) {
		this.extensionNumber = extensionNumber;
	}

	public String getExternalNetworkID() {
		return externalNetworkID;
	}

	public void setExternalNetworkID(String externalNetworkID) {
		this.externalNetworkID = externalNetworkID;
	}

	public String getForwardingInstitutionID() {
		return forwardingInstitutionID;
	}

	public void setForwardingInstitutionID(String forwardingInstitutionID) {
		this.forwardingInstitutionID = forwardingInstitutionID;
	}

	public String getMerchantCategoryCode() {
		return merchantCategoryCode;
	}

	public void setMerchantCategoryCode(String merchantCategoryCode) {
		this.merchantCategoryCode = merchantCategoryCode;
	}

	public String getSettlementAmount() {
		return settlementAmount;
	}

	public void setSettlementAmount(String settlementAmount) {
		this.settlementAmount = settlementAmount;
	}

	public String getSettlementConvRate() {
		return settlementConvRate;
	}

	public void setSettlementConvRate(String settlementConvRate) {
		this.settlementConvRate = settlementConvRate;
	}

	public String getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(String transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public String getSettlementCurrency() {
		return settlementCurrency;
	}

	public void setSettlementCurrency(String settlementCurrency) {
		this.settlementCurrency = settlementCurrency;
	}

}
