package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;


public class LIQPostingResponse {

	
	String LoanIQGLId;
    String statusCode;
    BigDecimal availablebalance;
	public String getLoanIQGLId() {
		return LoanIQGLId;
	}
	public void setLoanIQGLId(String loanIQGLId) {
		LoanIQGLId = loanIQGLId;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public BigDecimal getAvailablebalance() {
		return availablebalance;
	}
	public void setAvailablebalance(BigDecimal availablebalance) {
		this.availablebalance = availablebalance;
	}

}
