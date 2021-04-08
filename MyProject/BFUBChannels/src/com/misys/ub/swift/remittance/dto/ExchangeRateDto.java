package com.misys.ub.swift.remittance.dto;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

public class ExchangeRateDto {
    BigDecimal debitExchangeRate = BigDecimal.ZERO;
    BigDecimal creditExchangeRate = BigDecimal.ZERO;
    BigDecimal debitAmount = BigDecimal.ZERO;
    BigDecimal creditAmount = BigDecimal.ZERO;
    String creditExchangeType = StringUtils.EMPTY;
    String debitExchangeType = StringUtils.EMPTY;
    String instructedCcy = StringUtils.EMPTY;
    String debitCcy = StringUtils.EMPTY;
    String creditCcy = StringUtils.EMPTY;

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getDebitExchangeRate() {
        return debitExchangeRate;
    }

    public void setDebitExchangeRate(BigDecimal debitExchangeRate) {
        this.debitExchangeRate = debitExchangeRate;
    }

    public BigDecimal getCreditExchangeRate() {
        return creditExchangeRate;
    }

    public void setCreditExchangeRate(BigDecimal creditExchangeRate) {
        this.creditExchangeRate = creditExchangeRate;
    }

    public String getCreditExchangeType() {
        return creditExchangeType;
    }

    public void setCreditExchangeType(String creditExchangeType) {
        this.creditExchangeType = creditExchangeType;
    }

    public String getDebitExchangeType() {
        return debitExchangeType;
    }

    public void setDebitExchangeType(String debitExchangeType) {
        this.debitExchangeType = debitExchangeType;
    }

    public String getInstructedCcy() {
        return instructedCcy;
    }

    public void setInstructedCcy(String instructedCcy) {
        this.instructedCcy = instructedCcy;
    }

    public String getDebitCcy() {
        return debitCcy;
    }

    public void setDebitCcy(String debitCcy) {
        this.debitCcy = debitCcy;
    }

    public String getCreditCcy() {
        return creditCcy;
    }

    public void setCreditCcy(String creditCcy) {
        this.creditCcy = creditCcy;
    }

}
