package com.misys.ub.dc.sepaForecast;

import java.math.BigDecimal;
import java.util.Date;

import bf.com.misys.bf.attributes.ErrorResponse;
import bf.com.misys.bf.attributes.Event;
import bf.com.misys.cbs.types.Currency;

public class SepaForecastResponse {

	public BigDecimal chargeAmt;
	public String chargeCC;
	public String chargeFundingAcc;
	public BigDecimal chargeTaxAmt;
	public String chargeTaxCC;
	public BigDecimal exchangeRate;
	public String fromCCY;
	public String toCCY;
	public String settelmentDate;

	
	public String getSettelmentDate() {
		return settelmentDate;
	}

	public void setSettelmentDate(String settlmtdate) {
		this.settelmentDate = settlmtdate;
	}

	public BigDecimal getChargeAmt() {
		return chargeAmt;
	}

	public void setChargeAmt(BigDecimal chargeAmt) {
		this.chargeAmt = chargeAmt;
	}

	public String getChargeCC() {
		return chargeCC;
	}

	public void setChargeCC(String chargeCC) {
		this.chargeCC = chargeCC;
	}

	public String getChargeFundingAcc() {
		return chargeFundingAcc;
	}

	public void setChargeFundingAcc(String chargeFundingAcc) {
		this.chargeFundingAcc = chargeFundingAcc;
	}

	public BigDecimal getChargeTaxAmt() {
		return chargeTaxAmt;
	}

	public void setChargeTaxAmt(BigDecimal chargeTaxAmt) {
		this.chargeTaxAmt = chargeTaxAmt;
	}

	public String getChargeTaxCC() {
		return chargeTaxCC;
	}

	public void setChargeTaxCC(String chargeTaxCC) {
		this.chargeTaxCC = chargeTaxCC;
	}

	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getFromCCY() {
		return fromCCY;
	}

	public void setFromCCY(String fromCCY) {
		this.fromCCY = fromCCY;
	}

	public String getToCCY() {
		return toCCY;
	}

	public void setToCCY(String toCCY) {
		this.toCCY = toCCY;
	}

	ErrorResponse errorResponse = new ErrorResponse();

	public ErrorResponse getErrorResponse() {
		return errorResponse;
	}

	public void setErrorResponse(ErrorResponse errorResponse) {
		this.errorResponse = errorResponse;
	}

}