/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.types;


public class AccountDetails {

	private String accountId;
	
	private String productId;
	
	private String currencyCode;
	
	private String openDate;
	
	private String stopped;
	
	private String closed;
	
	private String dormantStatus;
	
	private String ariIndicator;
	
	private String productContextCode;
	
	private String accountStatus;
	
	private String customerCode;
	
	private String modeOfOperation;
	
	private String ownerId;
	
	private String role;
	
	private String pseudoname;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}
	
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	
	public String getOpenDate() {
		return openDate;
	}
	
	public void setOpenDate(String openDate) {
		this.openDate = openDate;
	}
	
	public String getStopped() {
		return stopped;
	}
	
	public void setStopped(String stopped) {
		this.stopped = stopped;
	}
	
	public String getClosed() {
		return closed;
	}
	
	public void setClosed(String closed) {
		this.closed = closed;
	}
	
	public String getDormantStatus() {
		return dormantStatus;
	}
	
	public void setDormantStatus(String dormantStatus) {
		this.dormantStatus = dormantStatus;
	}

	public String getAriIndicator() {
		return ariIndicator;
	}

	public void setAriIndicator(String ariIndicator) {
		this.ariIndicator = ariIndicator;
	}

	public String getProductContextCode() {
		return productContextCode;
	}
	
	public void setProductContextCode(String productContextCode) {
		this.productContextCode = productContextCode;
	}

	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getModeOfOperation() {
		return modeOfOperation;
	}

	public void setModeOfOperation(String modeOfOperation) {
		this.modeOfOperation = modeOfOperation;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPseudoname() {
		return pseudoname;
	}
	
	public void setPseudoname(String pseudoname) {
		this.pseudoname = pseudoname;
	}
}
