package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class ChequeInfo {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private String chequeNumber = CommonConstants.EMPTY_STRING;
	private String dateOfIssue = CommonConstants.EMPTY_STRING;
	private String amount = CommonConstants.EMPTY_STRING;
	private String amountOption = CommonConstants.EMPTY_STRING;
	private String drawerBank = CommonConstants.EMPTY_STRING;
	private String drawerBankOption = CommonConstants.EMPTY_STRING;
	private String payee = CommonConstants.EMPTY_STRING;

	public ChequeInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getChequeNumber() {
		return chequeNumber;
	}

	public void setChequeNumber(String chequeNumber) {
		this.chequeNumber = chequeNumber;
	}

	public String getDateOfIssue() {
		return dateOfIssue;
	}

	public void setDateOfIssue(String dateOfIssue) {
		this.dateOfIssue = dateOfIssue;
	}

	public String getDrawerBank() {
		return drawerBank;
	}

	public void setDrawerBank(String drawerBank) {
		this.drawerBank = drawerBank;
	}

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	/**
	 * @return the amountOption
	 */
	public String getAmountOption() {
		return amountOption;
	}

	/**
	 * @param amountOption
	 *            the amountOption to set
	 */
	public void setAmountOption(String amountOption) {
		this.amountOption = amountOption;
	}

	/**
	 * @return the drawerBankOption
	 */
	public String getDrawerBankOption() {
		return drawerBankOption;
	}

	/**
	 * @param drawerBankOption
	 *            the drawerBankOption to set
	 */
	public void setDrawerBankOption(String drawerBankOption) {
		this.drawerBankOption = drawerBankOption;
	}

}
