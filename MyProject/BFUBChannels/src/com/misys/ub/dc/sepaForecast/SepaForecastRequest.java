package com.misys.ub.dc.sepaForecast;

import java.math.BigDecimal;
import java.util.Date;

public class SepaForecastRequest {

	public String channelId;
	public String applicationId;
	public String customerId;
	public String userId;
	public String fromMyAccount;
	public String fromMyAccountCurrency;
	public String transferCurrency;
	public BigDecimal amount;
	public String paymentReference;
	public String beneficiaryName;
	public String ibanAccount;
	public String benBankSWIFTorBIC;
	public String bankName;
	public String benAddr;
	public String benCountry;
	public String charges;
	public String transferMethod;
	public String chargeAccount;
	public String paymentsys;
	public String paymenttyp;
	public String mpmHostId;
	public String hostGroupDesc;
	public Date transDate;

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFromMyAccount() {
		return fromMyAccount;
	}

	public void setFromMyAccount(String fromMyAccount) {
		this.fromMyAccount = fromMyAccount;
	}

	public String getFromMyAccountCurrency() {
		return fromMyAccountCurrency;
	}

	public void setFromMyAccountCurrency(String fromMyAccountCurrency) {
		this.fromMyAccountCurrency = fromMyAccountCurrency;
	}

	public String getTransferCurrency() {
		return transferCurrency;
	}

	public void setTransferCurrency(String transferCurrency) {
		this.transferCurrency = transferCurrency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getPaymentReference() {
		return paymentReference;
	}

	public void setPaymentReference(String paymentReference) {
		this.paymentReference = paymentReference;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getIbanAccount() {
		return ibanAccount;
	}

	public void setIbanAccount(String ibanAccount) {
		this.ibanAccount = ibanAccount;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getCharges() {
		return charges;
	}

	public void setCharges(String charges) {
		this.charges = charges;
	}

	public String getTransferMethod() {
		return transferMethod;
	}

	public void setTransferMethod(String transferMethod) {
		this.transferMethod = transferMethod;
	}

	public String getChargeAccount() {
		return chargeAccount;
	}

	public void setChargeAccount(String chargeAccount) {
		this.chargeAccount = chargeAccount;
	}

	public String getPaymentsys() {
		return paymentsys;
	}

	public void setPaymentsys(String paymentsys) {
		this.paymentsys = paymentsys;
	}

	public String getPaymenttyp() {
		return paymenttyp;
	}

	public void setPaymenttyp(String paymenttyp) {
		this.paymenttyp = paymenttyp;
	}

	public String getMpmHostId() {
		return mpmHostId;
	}

	public void setMpmHostId(String mpmHostId) {
		this.mpmHostId = mpmHostId;
	}

	public String getHostGroupDesc() {
		return hostGroupDesc;
	}

	public void setHostGroupDesc(String hostGroupDesc) {
		this.hostGroupDesc = hostGroupDesc;
	}

	public Date getTransDate() {
		return transDate;
	}

	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}

	public String getBenBankSWIFTorBIC() {
		return benBankSWIFTorBIC;
	}

	public void setBenBankSWIFTorBIC(String benBankSWIFTorBIC) {
		this.benBankSWIFTorBIC = benBankSWIFTorBIC;
	}

	public String getBenAddr() {
		return benAddr;
	}

	public void setBenAddr(String benAddr) {
		this.benAddr = benAddr;
	}

	public String getBenCountry() {
		return benCountry;
	}

	public void setBenCountry(String benCountry) {
		this.benCountry = benCountry;
	}

}
