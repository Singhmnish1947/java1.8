package com.misys.ub.swift.tellerRemittance.utils;

public class RemittanceStatusDto {
    String hostTransactionId;
    String gppPaymentStatusId;
    String gppTransactionIndividualStatus;
    String originalEndToEndId;
    String uetr;
    String gppLocationUrl;

    // posting placeholders

    String gppInternalChargeAccountId;
    String gppInternalTaxAccountId;
    String gppInternalCashAccountId;
    String gppMisTxnCodeForChargeAndTax;
    String gppNostroAccountId;
    Boolean isGppConnected;

    public String getGppLocationUrl() {
        return gppLocationUrl;
    }

    public void setGppLocationUrl(String gppLocationUrl) {
        this.gppLocationUrl = gppLocationUrl;
    }


    public Boolean getIsGppConnected() {
        return isGppConnected;
    }

    public void setIsGppConnected(Boolean isGppConnected) {
        this.isGppConnected = isGppConnected;
    }

    public String getGppNostroAccountId() {
        return gppNostroAccountId;
    }

    public void setGppNostroAccountId(String gppNostroAccountId) {
        this.gppNostroAccountId = gppNostroAccountId;
    }

    public String getGppInternalChargeAccountId() {
        return gppInternalChargeAccountId;
    }

    public void setGppInternalChargeAccountId(String gppInternalChargeAccountId) {
        this.gppInternalChargeAccountId = gppInternalChargeAccountId;
    }

    public String getGppInternalTaxAccountId() {
        return gppInternalTaxAccountId;
    }

    public void setGppInternalTaxAccountId(String gppInternalTaxAccountId) {
        this.gppInternalTaxAccountId = gppInternalTaxAccountId;
    }

    public String getGppInternalCashAccountId() {
        return gppInternalCashAccountId;
    }

    public void setGppInternalCashAccountId(String gppInternalCashAccountId) {
        this.gppInternalCashAccountId = gppInternalCashAccountId;
    }

    public String getGppMisTxnCodeForChargeAndTax() {
        return gppMisTxnCodeForChargeAndTax;
    }

    public void setGppMisTxnCodeForChargeAndTax(String gppMisTxnCodeForChargeAndTax) {
        this.gppMisTxnCodeForChargeAndTax = gppMisTxnCodeForChargeAndTax;
    }

    public String getUetr() {
        return uetr;
    }

    public void setUetr(String uetr) {
        this.uetr = uetr;
    }

    public String getGppTransactionIndividualStatus() {
        return gppTransactionIndividualStatus;
    }

    public void setGppTransactionIndividualStatus(String gppTransactionIndividualStatus) {
        this.gppTransactionIndividualStatus = gppTransactionIndividualStatus;
    }

    public String getOriginalEndToEndId() {
        return originalEndToEndId;
    }

    public void setOriginalEndToEndId(String originalEndToEndId) {
        this.originalEndToEndId = originalEndToEndId;
    }


    public String getHostTransactionId() {
        return hostTransactionId;
    }

    public void setHostTransactionId(String hostTransactionId) {
        this.hostTransactionId = hostTransactionId;
    }

    public String getGppPaymentStatusId() {
        return gppPaymentStatusId;
    }

    public void setGppPaymentStatusId(String gppPaymentStatusId) {
        this.gppPaymentStatusId = gppPaymentStatusId;
    }

}
