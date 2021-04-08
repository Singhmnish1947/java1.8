package com.misys.ub.swift.tellerRemittance.utils;

import java.util.ArrayList;
import java.util.List;

import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.swift.FeesInformation;
import bf.com.misys.cbs.types.swift.TaxInformation;
import bf.com.misys.cbs.types.swift.TaxOnTaxInformation;

public class FeesDto {
	List<FeesInformation> feelist = new ArrayList<>();
	List<TaxInformation> taxlist = new ArrayList<>();
	List<TaxOnTaxInformation> taxOnTaxlist = new ArrayList<>();

	Currency totalFees = new Currency();
	Currency totalTax = new Currency();
	Currency totalTaxOnTax = new Currency();
	Currency totalChargeDebitAmount = new Currency();
	boolean isChargeWaived = Boolean.FALSE;

	public List<FeesInformation> getFeelist() {
		return feelist;
	}

	public void setFeelist(List<FeesInformation> feelist) {
		this.feelist = feelist;
	}

	public List<TaxInformation> getTaxlist() {
		return taxlist;
	}

	public void setTaxlist(List<TaxInformation> taxlist) {
		this.taxlist = taxlist;
	}

	public Currency getTotalFees() {
		return totalFees;
	}

	public void setTotalFees(Currency totalFees) {
		this.totalFees = totalFees;
	}

	public Currency getTotalTax() {
		return totalTax;
	}

	public void setTotalTax(Currency totalTax) {
		this.totalTax = totalTax;
	}

	public Currency getTotalTaxOnTax() {
		return totalTaxOnTax;
	}

	public void setTotalTaxOnTax(Currency totalTaxOnTax) {
		this.totalTaxOnTax = totalTaxOnTax;
	}

	public Currency getTotalChargeDebitAmount() {
		return totalChargeDebitAmount;
	}

	public void setTotalChargeDebitAmount(Currency totalChargeDebitAmount) {
		this.totalChargeDebitAmount = totalChargeDebitAmount;
	}

	public boolean isChargeWaived() {
		return isChargeWaived;
	}

	public void setChargeWaived(boolean isChargeWaived) {
		this.isChargeWaived = isChargeWaived;
	}

	public List<TaxOnTaxInformation> getTaxOnTaxlist() {
		return taxOnTaxlist;
	}

	public void setTaxOnTaxlist(List<TaxOnTaxInformation> taxOnTaxlist) {
		this.taxOnTaxlist = taxOnTaxlist;
	}

}
