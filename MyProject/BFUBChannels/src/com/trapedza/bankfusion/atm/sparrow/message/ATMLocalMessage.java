/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMLocalMessage.java,v $
 * Revision 1.5  2008/08/12 20:14:53  vivekr
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
 * Revision 1.2  2007/11/14 11:05:28  prashantk
 * ATM Financial Messages
 *
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message;

/**
 * The ATMLocalMessage stores the ATM Sparrow Local Messages.
 */
public class ATMLocalMessage extends ATMSparrowFinancialMessage {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public ATMLocalMessage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 */

	/**
	 * Holds the reference for logger object
	 */

	private String actualBalance;
	private String availableBalance;
	private String branchName;
	private String extensionVersion;

	public String getActualBalance() {
		return actualBalance;
	}

	public void setActualBalance(String actualBalance) {
		this.actualBalance = actualBalance;
	}

	public String getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(String availableBalance) {
		this.availableBalance = availableBalance;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getExtensionVersion() {
		return extensionVersion;
	}

	public void setExtensionVersion(String extensionVersion) {
		this.extensionVersion = extensionVersion;
	}

}
