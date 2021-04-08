package com.misys.ub.swift.remittance.dto;

import bf.com.misys.cbs.types.Charge;
import bf.com.misys.cbs.types.Currency;

public class PreCalculateChargeDto {
    Charge preChargeAlias;
    Currency convertedPreChargeAmt;

    public Charge getPreChargeAlias() {
        return preChargeAlias;
    }

    public void setPreChargeAlias(Charge preChargeAlias) {
        this.preChargeAlias = preChargeAlias;
    }

    public Currency getConvertedPreChargeAmt() {
        return convertedPreChargeAmt;
    }

    public void setConvertedPreChargeAmt(Currency convertedPreChargeAmt) {
        this.convertedPreChargeAmt = convertedPreChargeAmt;
    }

}
