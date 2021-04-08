package com.misys.ub.swift.tellerRemittance.charges;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.common.runtime.toolkit.expression.function.RoundToScale;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceFeeDao;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceMessageDao;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceTaxDao;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceTaxOnTaxDao;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceFee;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTax;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTaxOnTax;

import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.swift.FeesInformation;
import bf.com.misys.cbs.types.swift.TaxInformation;
import bf.com.misys.cbs.types.swift.TaxOnTaxInformation;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;

public class ViewRemittanceFees {
	public static TxnfeesInformation getTxnfeesInformation(String remittanceId) {
		TxnfeesInformation txnFeeInfo = new TxnfeesInformation();
		List<FeesInformation> newFeeList = new ArrayList<>();
		List<TaxInformation> newTaxList = new ArrayList<>();
		List<TaxOnTaxInformation> newTaxOnTaxList = new ArrayList<>();
		BigDecimal totalFeeAmount = BigDecimal.ZERO;
		BigDecimal totalTaxAmount = BigDecimal.ZERO;
		BigDecimal totalTaxOnTaxAmount = BigDecimal.ZERO;
		String feeCurrency = StringUtils.EMPTY;

		List<IBOUB_SWT_RemittanceFee> feeList = RemittanceFeeDao.findByRemittanceId(remittanceId);
		if (feeList != null && !feeList.isEmpty()) {
			FeesInformation[] vFeesInformationArray = new FeesInformation[feeList.size()];
			for (IBOUB_SWT_RemittanceFee fee : feeList) {
				FeesInformation feeInfo = new FeesInformation();
				Currency feeAmount = new Currency();
				feeCurrency=fee.getF_UBFEECURRENCY();
				feeAmount.setIsoCurrencyCode(feeCurrency);
				feeAmount.setAmount(RoundToScale.run(fee.getF_UBFEEAMOUNT(), feeCurrency));
				totalFeeAmount = totalFeeAmount.add(fee.getF_UBFEEAMOUNT());
				feeInfo.setFeeAmount(feeAmount);
				feeInfo.setFeeCategory(fee.getF_UBFEECATEGORY());
				feeInfo.setFeeName(fee.getF_UBFEENAME());
				newFeeList.add(feeInfo);
			}
			txnFeeInfo.setFeesInformation(newFeeList.toArray(vFeesInformationArray));
			Currency totalFee = new Currency();
			totalFee.setAmount(RoundToScale.run(totalFeeAmount, feeCurrency));
			totalFee.setIsoCurrencyCode(feeCurrency);
			txnFeeInfo.setTotalFeeAmount(totalFee);

		}

		List<IBOUB_SWT_RemittanceTax> taxList = RemittanceTaxDao.findByRemittanceId(remittanceId);
		if (taxList != null && !taxList.isEmpty()) {
			TaxInformation[] vTaxInformationArray = new TaxInformation[taxList.size()];
			for (IBOUB_SWT_RemittanceTax tax : taxList) {
				TaxInformation taxInfo = new TaxInformation();
				taxInfo.setDescription(tax.getF_UBTAXDESCRIPTION());
				taxInfo.setTaxPercentage(tax.getF_UBTAXPERCENTAGE());
				Currency taxAmount = new Currency();
				taxAmount.setAmount(RoundToScale.run(tax.getF_UBTAXAMOUNT(),feeCurrency));
				taxAmount.setIsoCurrencyCode(tax.getF_UBTAXCURRENCY());
				totalTaxAmount = totalTaxAmount.add(tax.getF_UBTAXAMOUNT());
				taxInfo.setTaxAmount(taxAmount);
				newTaxList.add(taxInfo);
			}
			txnFeeInfo.setTaxInformation(newTaxList.toArray(vTaxInformationArray));
			Currency totalTax = new Currency();
			totalTax.setAmount(RoundToScale.run(totalTaxAmount,feeCurrency));
			totalTax.setIsoCurrencyCode(feeCurrency);
			txnFeeInfo.setTotalTaxAmount(totalTax);
		}

		List<IBOUB_SWT_RemittanceTaxOnTax> taxOnTaxList = RemittanceTaxOnTaxDao.findByRemittanceId(remittanceId);
		if (taxOnTaxList != null && !taxOnTaxList.isEmpty()) {
			TaxOnTaxInformation[] vTaxOnTaxInformationArray = new TaxOnTaxInformation[taxOnTaxList.size()];
			for (IBOUB_SWT_RemittanceTaxOnTax taxOnTax : taxOnTaxList) {
				TaxOnTaxInformation taxOnTaxInfo = new TaxOnTaxInformation();
				taxOnTaxInfo.setDescription(taxOnTax.getF_UBTAXONTAXDESCRIPTION());
				Currency taxOnTaxAmount = new Currency();
				taxOnTaxAmount.setAmount(RoundToScale.run(taxOnTax.getF_UBTAXONTAXAMOUNT(),feeCurrency));
				taxOnTaxAmount.setIsoCurrencyCode(taxOnTax.getF_UBTAXONTAXCURRENCY());
				totalTaxOnTaxAmount = totalTaxOnTaxAmount.add(taxOnTax.getF_UBTAXONTAXAMOUNT());
				taxOnTaxInfo.setTaxOnTaxAmount(taxOnTaxAmount);
				taxOnTaxInfo.setTaxOnTaxPercentage(taxOnTax.getF_UBTAXONTAXPERCENTAGE());
				newTaxOnTaxList.add(taxOnTaxInfo);
			}
			txnFeeInfo.setTaxOnTaxInformation(newTaxOnTaxList.toArray(vTaxOnTaxInformationArray));
			Currency totalTaxOnTax = new Currency();
			totalTaxOnTax.setAmount(RoundToScale.run(totalTaxOnTaxAmount,feeCurrency));
			totalTaxOnTax.setIsoCurrencyCode(feeCurrency);
			txnFeeInfo.setTotalTaxOnTaxAmount(totalTaxOnTax);
		}

		BigDecimal totalChargeDebitAmount = totalFeeAmount.add(totalTaxAmount);
		
		IBOUB_SWT_RemittanceMessage remittanceMsg = RemittanceMessageDao.findByRemittanceId(remittanceId);
		if (remittanceMsg != null) {
			txnFeeInfo.setIsWaived(remittanceMsg.getF_UBISCHARGEWAIVED().equals("Y") ? Boolean.TRUE : Boolean.FALSE);
			totalChargeDebitAmount=BigDecimal.ZERO;
		}
		
		Currency totalChargeDebitAmt = new Currency();
		totalChargeDebitAmt.setAmount(RoundToScale.run(totalChargeDebitAmount,feeCurrency));
		totalChargeDebitAmt.setIsoCurrencyCode(feeCurrency);
		txnFeeInfo.setTotalChargeDebitAmount(totalChargeDebitAmt);

		return txnFeeInfo;
	}

}
