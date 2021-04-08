package com.misys.ub.cc.types;

import bf.com.misys.cbs.msgs.v1r0.ReadLoanDetailsRs;
import bf.com.misys.cbs.types.AccountBasicDetails;
import bf.com.misys.cbs.types.AcctBalances;
import bf.com.misys.cbs.types.AcctCharacteristics;
import bf.com.misys.cbs.types.ListMandateDetails;
import bf.com.misys.cbs.types.TermDepositOverview;

public class ExtensiveAccountDetails {

	private AccountBasicDetails accountBasicDetails;

	private String accountOperation;
	
	private String countryCode;
	
	private AcctBalances acctBalances;

	private AcctCharacteristics acctCharacteristics;

	private ListMandateDetails listMandateDetails;

	private ReadLoanDetailsRs readLoanDetailsRs;
	
	private TermDepositOverview termDepositOverview;

	
	public AccountBasicDetails getAccountBasicDetails() {
		return accountBasicDetails;
	}

	public void setAccountBasicDetails(AccountBasicDetails accountBasicDetails) {
		this.accountBasicDetails = accountBasicDetails;
	}

	public String getAccountOperation() {
		return accountOperation;
	}

	public void setAccountOperation(String accountOperation) {
		this.accountOperation = accountOperation;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public AcctBalances getAcctBalances() {
		return acctBalances;
	}

	public void setAcctBalances(AcctBalances acctBalances) {
		this.acctBalances = acctBalances;
	}

	public AcctCharacteristics getAcctCharacteristics() {
		return acctCharacteristics;
	}

	public void setAcctCharacteristics(AcctCharacteristics acctCharacteristics) {
		this.acctCharacteristics = acctCharacteristics;
	}

	public ListMandateDetails getListMandateDetails() {
		return listMandateDetails;
	}

	public void setListMandateDetails(ListMandateDetails listMandateDetails) {
		this.listMandateDetails = listMandateDetails;
	}

	public ReadLoanDetailsRs getReadLoanDetailsRs() {
		return readLoanDetailsRs;
	}

	public void setReadLoanDetailsRs(ReadLoanDetailsRs readLoanDetailsRs) {
		this.readLoanDetailsRs = readLoanDetailsRs;
	}
	
	public TermDepositOverview getTermDepositOverview() {
		return termDepositOverview;
	}

	public void setTermDepositOverview(TermDepositOverview termDepositOverview) {
		this.termDepositOverview = termDepositOverview;
	}


}
