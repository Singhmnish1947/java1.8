package com.misys.ub.swift;

import java.util.ArrayList;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT111 extends SwiftHeader {
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String sendersReference = CommonConstants.EMPTY_STRING;
	private String chequeNumber = CommonConstants.EMPTY_STRING;
	private String dateOfIssue = CommonConstants.EMPTY_STRING;
	private String amount = CommonConstants.EMPTY_STRING;
	private String amountOption = CommonConstants.EMPTY_STRING;
	private String drawerBank = CommonConstants.EMPTY_STRING;
	private String drawerBankOption = CommonConstants.EMPTY_STRING;
	private String payee = CommonConstants.EMPTY_STRING;
	private String Queries = CommonConstants.EMPTY_STRING;
	

	public String getQueries() {
		return Queries;
	}




	public void setQueries(String queries) {
		Queries = queries;
	}




	public UB_MT111() {

	}

	
	

	public String getSendersReference() {
		return sendersReference;
	}

	public void setSendersReference(String sendersReference) {
		this.sendersReference = sendersReference;
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




	public String getAmount() {
		return amount;
	}




	public void setAmount(String amount) {
		this.amount = amount;
	}




	public String getAmountOption() {
		return amountOption;
	}




	public void setAmountOption(String amountOption) {
		this.amountOption = amountOption;
	}




	public String getDrawerBank() {
		return drawerBank;
	}




	public void setDrawerBank(String drawerBank) {
		this.drawerBank = drawerBank;
	}




	public String getDrawerBankOption() {
		return drawerBankOption;
	}




	public void setDrawerBankOption(String drawerBankOption) {
		this.drawerBankOption = drawerBankOption;
	}




	public String getPayee() {
		return payee;
	}




	public void setPayee(String payee) {
		this.payee = payee;
	}

	

	

	

	
}
