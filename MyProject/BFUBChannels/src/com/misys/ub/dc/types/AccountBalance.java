/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.types;

import java.util.HashMap;

public class AccountBalance {

	private String accountId;
	
	@SuppressWarnings("rawtypes")
	private HashMap balances;
		
	public String getAccountId() {
		return accountId;
	}
	
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	@SuppressWarnings("rawtypes")
	public HashMap getBalances() {
		return balances;
	}
	
	@SuppressWarnings("rawtypes")
	public void setBalances(HashMap balances) {
		this.balances = balances;
	}
}
