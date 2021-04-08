package com.misys.ub.atm.batch.model;

public class Transaction {
	String transaction_type;
	Debit_Entry debit_entry;
	Credit_Entry credit_entry;

	public Debit_Entry getDebit_entry() {
		return debit_entry;
	}

	public void setDebit_entry(Debit_Entry debit_entry) {
		this.debit_entry = debit_entry;
	}

	public Credit_Entry getCredit_entry() {
		return credit_entry;
	}

	public void setCredit_entry(Credit_Entry credit_entry) {
		this.credit_entry = credit_entry;
	}

	public String getTransaction_type() {
		return transaction_type;
	}

	public void setTransaction_type(String transaction_type) {
		this.transaction_type = transaction_type;
	}

}
