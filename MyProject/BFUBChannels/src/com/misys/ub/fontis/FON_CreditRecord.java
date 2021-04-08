/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_CreditRecord.java,v 1.7 2008/08/12 20:15:34 vivekr Exp $
 * **********************************************************************************
 *
 * * Revision 1.14  2008/02/06 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 * 
 */
package com.misys.ub.fontis;

import java.math.BigDecimal;

public class FON_CreditRecord {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Instruction Batch Number. This a 7 digit sequence number which identifies an instruction batch to which this
	 * transaction leg belongs to.
	 * It ranges from 0000000-9999999.
	 */
	private String batchNo;

	/**
	 * The number of this credit record within the instruction
	 */
	private int creditNo;

	/**
	 * Credit Account Source Code
	 */
	private String creditSourceCode;

	/**
	 * Credit Account Number.
	 */
	private String creditAccountNo;

	/**
	 * Credit Account BIC Code
	 */
	private String creditBICCode;

	/**
	 * Credit Account Name
	 */
	private String creditAccountName;

	/**
	 * Credit Default Name
	 */
	private String creditDefaultName;

	/**
	 * Code for Account Type
	 */
	private String creditAccountType;

	/**
	 * Code for account currency
	 */
	private String creditCurrencyCode;

	/**
	 * Position account for credit currency
	 */
	private String creditCurrencyPositionAccount;

	/**
	 * Exchange rate for credit currency
	 */
	private BigDecimal creditExchangeRate;

	/**
	 * Multiply-Divide flag for credit currency
	 */
	private char creditMultiplyDevideFlag;

	/**
	 * Beneficiary ref. no.
	 */
	private String beneficiaryRefNo;

	/**
	 * Beneficiary name
	 */
	private String beneficiaryName;

	/**
	 * Beneficiary address
	 */
	private String beneficiaryAddress;

	/**
	 * Beneficiary’s bank code
	 */
	private String beneficiaryBankCode;

	/**
	 * Beneficiaries bank name
	 */
	private String beneficiaryBankName;

	/**
	 * Beneficiaries bank address
	 */
	private String beneficiaryBankAddress;

	/**
	 * Beneficiaries account no.
	 */
	private String beneficiaryAccountCode;

	/**
	 * Beneficiary’s bank Account Type
	 */
	private String beneficiaryBankType;

	/**
	 * Intermediary bank name
	 */
	private String intermediaryBankName;

	/**
	 * Intermediary bank city
	 */
	private String intermediaryBankCity;

	/**
	 * Intermediary account no.
	 */
	private String intermediaryBankAccountNo;

	/**
	 * BIC code for Intermediary bank
	 */
	private String intermediaryBankCode;

	/**
	 * Account type code for Intermediary bank
	 */
	private String intermediaryBankType;

	/**
	 * Credit dealers rate
	 */
	private String creditDealersRate;

	/**
	 * Credit dealer’s Name
	 */
	private String creditDealersName;

	/**
	 * Credit Date quoted
	 */
	private String creditDateQuoted;

	/**
	 * Payment Currency Code
	 */
	private String transactionCurrencyCode;

	/**
	 * Multiply-Divide flag for transaction currency
	 */
	private char transactionMultiplyDevideFlag;

	/**
	 * Transfer Amount
	 */
	private BigDecimal amount;

	/**
	 * Number of decimals in transaction amount
	 */
	private int noOfDecimalsInTransactionAmount;

	/**
	 * Transaction currency exchange rate
	 */
	private BigDecimal transactionExchangeRate;

	/**
	 * Equivalent Payment Amount
	 */
	private BigDecimal equivalentAmount;

	/**
	 * Number of decimals in Base equivalent amount
	 */
	private int noOfDecimalsInEquivalentAmount;

	/**
	 * Amount in Banks Base Currency
	 */
	private BigDecimal amountInBaseCurrency;

	/**
	 * Reference Field
	 */
	private String reference;

	/**
	 * Code Field
	 */
	private String code;

	/**
	 * Particulars
	 */
	private String particulars;

	/**
	 * Transaction Code
	 */
	private String transactionCode;

	/**
	 * Processing Staus flag:
	 * 1 - Validation failed (debit transactions that have failed validation)
	 * 4 - Processed and posted succesfully 
	 */
	private int statusFlag = 0;

	/**
	 * Flag to indicate credit leg should post to currency suspense account
	 */
	private boolean creditToCurrSuspenseAccount = false;

	/**
	 * Flag to indicate whether to generate EFT message or not:
	 * TRUE - generate EFT message
	 * FALSE - do not generate EFT message
	 */
	private boolean EFTMessage = false;

	/**
	 * Flag to indicate whether to generate SWIFT message or not:
	 * TRUE - generate SWIFT message
	 * FALSE - do not generate SWIFT message
	 */
	private boolean SWIFTMessage = false;

	/**
	 *  Force post flag:
	 *  TRUE : Force post this transaction leg (CR)
	 *  FALSE : Do not force post this transaction leg (CR)
	 */
	private boolean forcePost = false;

	/**
	 * Error messages, if any.
	 */
	private String errMessage = null;

	public String getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}

	public boolean isEFTMessage() {
		return EFTMessage;
	}

	public void setEFTMessage(boolean message) {
		EFTMessage = message;
	}

	public boolean isSWIFTMessage() {
		return SWIFTMessage;
	}

	public void setSWIFTMessage(boolean message) {
		SWIFTMessage = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCreditAccountName() {
		return creditAccountName;
	}

	public void setCreditAccountName(String creditAccountName) {
		this.creditAccountName = creditAccountName;
	}

	public String getCreditAccountNo() {
		return creditAccountNo;
	}

	public void setCreditAccountNo(String creditAccountNo) {
		this.creditAccountNo = creditAccountNo;
	}

	public String getCreditBICCode() {
		return creditBICCode;
	}

	public void setCreditBICCode(String creditBICCode) {
		this.creditBICCode = creditBICCode;
	}

	public String getCreditCurrencyCode() {
		return creditCurrencyCode;
	}

	public void setCreditCurrencyCode(String creditCurrencyCode) {
		this.creditCurrencyCode = creditCurrencyCode;
	}

	public String getCreditCurrencyPositionAccount() {
		return creditCurrencyPositionAccount;
	}

	public void setCreditCurrencyPositionAccount(String creditCurrencyPositionAccount) {
		this.creditCurrencyPositionAccount = creditCurrencyPositionAccount;
	}

	public String getCreditDateQuoted() {
		return creditDateQuoted;
	}

	public void setCreditDateQuoted(String creditDateQuoted) {
		this.creditDateQuoted = creditDateQuoted;
	}

	public String getCreditDealersName() {
		return creditDealersName;
	}

	public void setCreditDealersName(String creditDealersName) {
		this.creditDealersName = creditDealersName;
	}

	public String getCreditDealersRate() {
		return creditDealersRate;
	}

	public void setCreditDealersRate(String creditDealersRate) {
		this.creditDealersRate = creditDealersRate;
	}

	public BigDecimal getCreditExchangeRate() {
		return creditExchangeRate;
	}

	public void setCreditExchangeRate(BigDecimal creditExchangeRate) {
		this.creditExchangeRate = creditExchangeRate;
	}

	public char getCreditMultiplyDevideFlag() {
		return creditMultiplyDevideFlag;
	}

	public void setCreditMultiplyDevideFlag(char creditMultiplyDevideFlag) {
		this.creditMultiplyDevideFlag = creditMultiplyDevideFlag;
	}

	public int getCreditNo() {
		return creditNo;
	}

	public void setCreditNo(int creditNo) {
		this.creditNo = creditNo;
	}

	public String getCreditSourceCode() {
		return creditSourceCode;
	}

	public void setCreditSourceCode(String creditSourceCode) {
		this.creditSourceCode = creditSourceCode;
	}

	public String getCreditAccountType() {
		return creditAccountType;
	}

	public void setCreditAccountType(String creditAccountType) {
		this.creditAccountType = creditAccountType;
	}

	public String getCreditDefaultName() {
		return creditDefaultName;
	}

	public void setCreditDefaultName(String creditDefaultName) {
		this.creditDefaultName = creditDefaultName;
	}

	public int getNoOfDecimalsInEquivalentAmount() {
		return noOfDecimalsInEquivalentAmount;
	}

	public void setNoOfDecimalsInEquivalentAmount(int noOfDecimalsInEquivalentAmount) {
		this.noOfDecimalsInEquivalentAmount = noOfDecimalsInEquivalentAmount;
	}

	public int getNoOfDecimalsInTransactionAmount() {
		return noOfDecimalsInTransactionAmount;
	}

	public void setNoOfDecimalsInTransactionAmount(int noOfDecimalsInTransactionAmount) {
		this.noOfDecimalsInTransactionAmount = noOfDecimalsInTransactionAmount;
	}

	public String getParticulars() {
		return particulars;
	}

	public void setParticulars(String particulars) {
		this.particulars = particulars;
	}

	/*public char getRecordType() {
		return recordType;
	}
	public void setRecordType(char recordType) {
		this.recordType = recordType;
	}*/
	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getTransactionCode() {
		return transactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public String getTransactionCurrencyCode() {
		return transactionCurrencyCode;
	}

	public void setTransactionCurrencyCode(String transactionCurrencyCode) {
		this.transactionCurrencyCode = transactionCurrencyCode;
	}

	public BigDecimal getTransactionExchangeRate() {
		return transactionExchangeRate;
	}

	public void setTransactionExchangeRate(BigDecimal transactionExchangeRate) {
		this.transactionExchangeRate = transactionExchangeRate;
	}

	public char getTransactionMultiplyDevideFlag() {
		return transactionMultiplyDevideFlag;
	}

	public void setTransactionMultiplyDevideFlag(char transactionMultiplyDevideFlag) {
		this.transactionMultiplyDevideFlag = transactionMultiplyDevideFlag;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getBeneficiaryAccountCode() {
		return beneficiaryAccountCode;
	}

	public void setBeneficiaryAccountCode(String beneficiaryAccountCode) {
		this.beneficiaryAccountCode = beneficiaryAccountCode;
	}

	public String getBeneficiaryAddress() {
		return beneficiaryAddress;
	}

	public void setBeneficiaryAddress(String beneficiaryAddress) {
		this.beneficiaryAddress = beneficiaryAddress;
	}

	public String getBeneficiaryBankAddress() {
		return beneficiaryBankAddress;
	}

	public void setBeneficiaryBankAddress(String beneficiaryBankAddress) {
		this.beneficiaryBankAddress = beneficiaryBankAddress;
	}

	public String getBeneficiaryBankCode() {
		return beneficiaryBankCode;
	}

	public void setBeneficiaryBankCode(String beneficiaryBankCode) {
		this.beneficiaryBankCode = beneficiaryBankCode;
	}

	public String getBeneficiaryBankName() {
		return beneficiaryBankName;
	}

	public void setBeneficiaryBankName(String beneficiaryBankName) {
		this.beneficiaryBankName = beneficiaryBankName;
	}

	public String getBeneficiaryBankType() {
		return beneficiaryBankType;
	}

	public void setBeneficiaryBankType(String beneficiaryBankType) {
		this.beneficiaryBankType = beneficiaryBankType;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getBeneficiaryRefNo() {
		return beneficiaryRefNo;
	}

	public void setBeneficiaryRefNo(String beneficiaryRefNo) {
		this.beneficiaryRefNo = beneficiaryRefNo;
	}

	public String getIntermediaryBankAccountNo() {
		return intermediaryBankAccountNo;
	}

	public void setIntermediaryBankAccountNo(String intermediaryBankAccountNo) {
		this.intermediaryBankAccountNo = intermediaryBankAccountNo;
	}

	public String getIntermediaryBankCity() {
		return intermediaryBankCity;
	}

	public void setIntermediaryBankCity(String intermediaryBankCity) {
		this.intermediaryBankCity = intermediaryBankCity;
	}

	public String getIntermediaryBankCode() {
		return intermediaryBankCode;
	}

	public void setIntermediaryBankCode(String intermediaryBankCode) {
		this.intermediaryBankCode = intermediaryBankCode;
	}

	public String getIntermediaryBankName() {
		return intermediaryBankName;
	}

	public void setIntermediaryBankName(String intermediaryBankName) {
		this.intermediaryBankName = intermediaryBankName;
	}

	public String getIntermediaryBankType() {
		return intermediaryBankType;
	}

	public void setIntermediaryBankType(String intermediaryBankType) {
		this.intermediaryBankType = intermediaryBankType;
	}

	public int getStatusFlag() {
		return statusFlag;
	}

	public void setStatusFlag(int statusFlag) {
		this.statusFlag = statusFlag;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getAmountInBaseCurrency() {
		return amountInBaseCurrency;
	}

	public void setAmountInBaseCurrency(BigDecimal amountInBaseCurrency) {
		this.amountInBaseCurrency = amountInBaseCurrency;
	}

	public BigDecimal getEquivalentAmount() {
		return equivalentAmount;
	}

	public void setEquivalentAmount(BigDecimal equivalentAmount) {
		this.equivalentAmount = equivalentAmount;
	}

	//	Modified from creditToPositionAccount to creditToSuspenseAccount
	/*public boolean isCreditToPositionAccount() {
		return creditToPositionAccount;
	}
	public void setCreditToPositionAccount(boolean creditToPositionAccount) {
		this.creditToPositionAccount = creditToPositionAccount;
	}*/

	public boolean isCreditToCurrSuspenseAccount() {
		return creditToCurrSuspenseAccount;
	}

	public void setCreditToCurrSuspenseAccount(boolean creditToSuspenseAccount) {
		this.creditToCurrSuspenseAccount = creditToSuspenseAccount;
	}

	public boolean isForcePost() {
		return forcePost;
	}

	public void setForcePost(boolean forcePost) {
		this.forcePost = forcePost;
	}
}
