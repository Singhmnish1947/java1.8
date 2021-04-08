/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.types;

import java.sql.Timestamp;
import java.util.List;


public class TransactionsRequest {

	private String customerId;
	
	private List<String> accountId;
	
	private Timestamp fromDate;
	
	private Timestamp toDate;
	
	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public List<String> getAccountId() {
		return accountId;
	}
	
	public void setAccountId(List<String> accountId) {
		this.accountId = accountId;
	}
	
	public Timestamp getFromDate() {
		return fromDate;
	}

	public void setFromDate(Timestamp fromDate) {
		this.fromDate = fromDate;
	}
	
	public Timestamp getToDate() {
		return toDate;
	}

	public void setToDate(Timestamp toDate) {
		this.toDate = toDate;
	}
}
