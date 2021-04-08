/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.interfaces.exchangeRate;

import java.util.List;

public class ExchangeRateResponse {

	private String responseCode;
	private String responseMessage;
	private String overallStatus;
	private List<SubCode> codeList;
	
	public String getResponseCode() {
		return responseCode;
	}
	
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
	public String getResponseMessage() {
		return responseMessage;
	}
	
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getOverallStatus() {
		return overallStatus;
	}

	public void setOverallStatus(String overallStatus) {
		this.overallStatus = overallStatus;
	}
	
	public List<SubCode> getCodeList() {
		return codeList;
	}
	
	public void setCodeList(List<SubCode> codeList) {
		this.codeList = codeList;
	}
}
