package com.misys.ub.payment.swift.utils;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import bf.com.misys.cbs.types.Currency;

public class ChargesDto {
	private Currency debitAmount;
	private Currency creditAmount;
	private Currency payingBankChg;
	private String chargeFundingAccountId;
	private Currency ubCharges;
	private String chargeType;
	//credit exchangeRateType
	private String exchangeRateType;
	//debit exchangeRateType
    private String debitExchangeRateType;
    private String channelId=StringUtils.EMPTY;
	private BigDecimal exchangeRate;
	private Currency instructedAmount;

    public String getDebitExchangeRateType() {
        return debitExchangeRateType;
    }

    public void setDebitExchangeRateType(String debitExchangeRateType) {
        this.debitExchangeRateType = debitExchangeRateType;
    }

	public Currency getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(Currency instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getExchangeRateType() {
		return exchangeRateType;
	}

	public void setExchangeRateType(String exchangeRateType) {
		this.exchangeRateType = exchangeRateType;
	}

	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	public Currency getUbCharges() {
		return ubCharges;
	}

	public void setUbCharges(Currency ubCharges) {
		this.ubCharges = ubCharges;
	}

	public Currency getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(Currency debitAmount) {
		this.debitAmount = debitAmount;
	}

	public Currency getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(Currency creditAmount) {
		this.creditAmount = creditAmount;
	}

	public Currency getPayingBankChg() {
		return payingBankChg;
	}

	public void setPayingBankChg(Currency payingBankChg) {
		this.payingBankChg = payingBankChg;
	}

	public String getChargeFundingAccountId() {
		return chargeFundingAccountId;
	}

	public void setChargeFundingAccountId(String chargeFundingAccountId) {
		this.chargeFundingAccountId = chargeFundingAccountId;
	}
}
