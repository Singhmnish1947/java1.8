package com.misys.ub.swift.remittance.dto;

import java.math.BigDecimal;

import com.finastra.openapi.creditTransfer.v1.model.PreCalculatedCharges;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

public class RemittanceProcessDto {

    private boolean isStp = Boolean.FALSE;
    private boolean isCash = Boolean.FALSE;
    private String ruleId;
    private String creditTransactionCode;
    private String branchSortCode;
    private BankFusionEnvironment env;
    private BigDecimal ubChargeAmt=BigDecimal.ZERO;
    // open api
    private PreCalculatedCharges preCalculateCharge;
    private boolean isFromApi = Boolean.FALSE;
    private BigDecimal equivalentAmount = BigDecimal.ZERO;

    public boolean isFromApi() {
        return isFromApi;
    }

    public void setFromApi(boolean isFromApi) {
        this.isFromApi = isFromApi;
    }

    public BigDecimal getEquivalentAmount() {
        return equivalentAmount;
    }

    public void setEquivalentAmount(BigDecimal equivalentAmount) {
        this.equivalentAmount = equivalentAmount;
    }

    public PreCalculatedCharges getPreCalculateCharge() {
        return preCalculateCharge;
    }

    public void setPreCalculateCharge(PreCalculatedCharges preCalculateCharge) {
        this.preCalculateCharge = preCalculateCharge;
    }

    public BigDecimal getUbChargeAmt() {
        return ubChargeAmt;
    }

    public void setUbChargeAmt(BigDecimal ubChargeAmt) {
        this.ubChargeAmt = ubChargeAmt;
    }

    public String getCreditTransactionCode() {
        return creditTransactionCode;
    }

    public void setCreditTransactionCode(String creditTransactionCode) {
        this.creditTransactionCode = creditTransactionCode;
    }

    public boolean isStp() {
        return isStp;
    }

    public void setStp(boolean isStp) {
        this.isStp = isStp;
    }

    public boolean isCash() {
        return isCash;
    }

    public void setCash(boolean isCash) {
        this.isCash = isCash;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getBranchSortCode() {
        return branchSortCode;
    }

    public void setBranchSortCode(String branchSortCode) {
        this.branchSortCode = branchSortCode;
    }

    public BankFusionEnvironment getEnv() {
        return env;
    }

    public void setEnv(BankFusionEnvironment env) {
        this.env = env;
    }


}
