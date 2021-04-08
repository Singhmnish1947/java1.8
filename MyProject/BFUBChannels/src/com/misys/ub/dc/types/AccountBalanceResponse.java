/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AccountBalanceResponse {

	private String customerId;
	
	private AccountBalance[] balances;
	
	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public AccountBalance[] getBalances() {
		return balances;
	}
	
	public void setBalances(AccountBalance[] balances) {
		this.balances = balances;
	}
}
