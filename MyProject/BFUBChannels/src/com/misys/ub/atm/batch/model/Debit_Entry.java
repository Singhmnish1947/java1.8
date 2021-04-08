package com.misys.ub.atm.batch.model;


public class Debit_Entry {
    Account account;
    Amount amount;
    
    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public Amount getAmount() {
        return amount;
    }
    
    public void setAmount(Amount amount) {
        this.amount = amount;
    }
}
