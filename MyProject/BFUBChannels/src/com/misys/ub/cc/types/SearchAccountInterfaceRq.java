package com.misys.ub.cc.types;

public class SearchAccountInterfaceRq{
	
	/**
	 * 
	 */
	private String accountId;
	
	private String customerId;

	public String getAccountId() {
		return accountId;
	}
	
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
}
