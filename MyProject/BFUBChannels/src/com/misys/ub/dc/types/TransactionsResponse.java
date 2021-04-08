/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.types;

import java.util.List;

public class TransactionsResponse {

	private List<String> transactions;
	
	/**
	 * Response Code for the request
	 * S - Success
	 * F - Failure
	 */
	private String status;
	
	/**
	 * eventCode and arguments will be populated only in case of failure to 
	 * denote failure code and parameters for failure message.
	 */
	private String eventCode;
	
	private String eventMessage;
	
	private Object[] arguments;

	public List<String> getTransactions() {
		return transactions;
	}
	
	public void setTransactions(List<String> transactions) {
		this.transactions = transactions;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}
	
	public Object[] getArguments() {
		return arguments;
	}
	
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}
	public String getEventMessage() {
		return eventMessage;
	}

	public void setEventMessage(String eventMessage) {
		this.eventMessage = eventMessage;
	}
}
