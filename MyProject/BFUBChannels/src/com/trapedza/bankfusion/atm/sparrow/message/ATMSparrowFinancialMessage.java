/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMSparrowFinancialMessage.java,v $
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
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message;

import java.math.BigDecimal;

/**
 * The ATMSparrowFinancialMessage stores the ATM Sparrow Financial Messages.
 */
public class ATMSparrowFinancialMessage extends ATMSparrowMessage {

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

	private String account;
	private String actionCode;
	private BigDecimal amount1;
	private BigDecimal amount2;
	private BigDecimal amount3;
	private BigDecimal amount4;
	private String currencyDestDispensed;
	private String currencySourceAccount;
	private String descSourceAcc;
	private String loroMailbox;
	private String descDestAcc;
	private String destAccountNumber;
	private String localCurrencyCode;
	private String subIndex;
	private String variableDataType;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public BigDecimal getAmount1() {
		return amount1;
	}

	public void setAmount1(BigDecimal amount1) {
		this.amount1 = amount1;
	}

	public BigDecimal getAmount2() {
		return amount2;
	}

	public void setAmount2(BigDecimal amount2) {
		this.amount2 = amount2;
	}

	public BigDecimal getAmount3() {
		return amount3;
	}

	public void setAmount3(BigDecimal amount3) {
		this.amount3 = amount3;
	}

	public BigDecimal getAmount4() {
		return amount4;
	}

	public void setAmount4(BigDecimal amount4) {
		this.amount4 = amount4;
	}

	public String getCurrencyDestDispensed() {
		return currencyDestDispensed;
	}

	public void setCurrencyDestDispensed(String currencyDestDispensed) {
		this.currencyDestDispensed = currencyDestDispensed;
	}

	public String getCurrencySourceAccount() {
		return currencySourceAccount;
	}

	public void setCurrencySourceAccount(String currencySourceAccount) {
		this.currencySourceAccount = currencySourceAccount;
	}

	public String getDescSourceAcc() {
		return descSourceAcc;
	}

	public void setDescSourceAcc(String descSourceAcc) {
		this.descSourceAcc = descSourceAcc;
	}

	public String getDestAccountNumber() {
		return destAccountNumber;
	}

	public void setDestAccountNumber(String destAccountNumber) {
		this.destAccountNumber = destAccountNumber;
	}

	public String getLocalCurrencyCode() {
		return localCurrencyCode;
	}

	public void setLocalCurrencyCode(String localCurrencyCode) {
		this.localCurrencyCode = localCurrencyCode;
	}

	public String getSubIndex() {
		return subIndex;
	}

	public void setSubIndex(String subIndex) {
		this.subIndex = subIndex;
	}

	public String getVariableDataType() {
		return variableDataType;
	}

	public void setVariableDataType(String variableDataType) {
		this.variableDataType = variableDataType;
	}

	public String getDescDestAcc() {
		return descDestAcc;
	}

	public void setDescDestAcc(String descDestAcc) {
		this.descDestAcc = descDestAcc;
	}

	public String getLoroMailbox() {
		return loroMailbox;
	}

	public void setLoroMailbox(String loroMailbox) {
		this.loroMailbox = loroMailbox;
	}

}
