/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AccountDetailsRequest {

	private String customerId;
	
	private String[] accounts;
	
	public String getCustomerId() {
		return customerId;
	}
		
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
		
	public String[] getAccounts() {
		return accounts;
	}
	
	public void setAccounts(String[] accounts) {
		this.accounts = accounts;
	}
}
