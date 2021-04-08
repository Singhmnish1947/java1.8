/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AccountDetailsResponse {

	private String customerId;
	
	private AccountDetails[] accountDetails;
	
	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public AccountDetails[] getAccountDetails() {
		return accountDetails;
	}
	
	public void setAccountDetails(AccountDetails[] accountDetails) {
		this.accountDetails = accountDetails;
	}
}
