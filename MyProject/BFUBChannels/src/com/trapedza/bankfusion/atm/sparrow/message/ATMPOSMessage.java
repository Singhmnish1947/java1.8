/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMPOSMessage.java,v $
 * Revision 1.5  2008/08/12 20:14:53  vivekr
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
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message;

/**
 * The ATMPOSMessage stores the ATM Sparrow POS Messages.
 */
public class ATMPOSMessage extends ATMSparrowFinancialMessage {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public ATMPOSMessage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 */

	/**
	 * Holds the reference for logger object
	 */

	private String authorisationCode;
	private String cashBackAmount;
	private String extensionVersion;
	private String externalTerminalID;
	private String merchantCategoryCode;
	private String merchantID;
	private String merchantLocation;
	private String merchantName;
	private String settlementIdentifier;

	public String getAuthorisationCode() {
		return authorisationCode;
	}

	public void setAuthorisationCode(String authorisationCode) {
		this.authorisationCode = authorisationCode;
	}

	public String getCashBackAmount() {
		return cashBackAmount;
	}

	public void setCashBackAmount(String cashBackAmount) {
		this.cashBackAmount = cashBackAmount;
	}

	public String getExtensionVersion() {
		return extensionVersion;
	}

	public void setExtensionVersion(String extensionVersion) {
		this.extensionVersion = extensionVersion;
	}

	public String getExternalTerminalID() {
		return externalTerminalID;
	}

	public void setExternalTerminalID(String externalTerminalID) {
		this.externalTerminalID = externalTerminalID;
	}

	public String getMerchantCategoryCode() {
		return merchantCategoryCode;
	}

	public void setMerchantCategoryCode(String merchantCategoryCode) {
		this.merchantCategoryCode = merchantCategoryCode;
	}

	public String getMerchantID() {
		return merchantID;
	}

	public void setMerchantID(String merchantID) {
		this.merchantID = merchantID;
	}

	public String getMerchantLocation() {
		return merchantLocation;
	}

	public void setMerchantLocation(String merchantLocation) {
		this.merchantLocation = merchantLocation;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getSettlementIdentifier() {
		return settlementIdentifier;
	}

	public void setSettlementIdentifier(String settlementIdentifier) {
		this.settlementIdentifier = settlementIdentifier;
	}

}
