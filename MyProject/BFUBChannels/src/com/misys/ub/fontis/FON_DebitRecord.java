/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: FON_DebitRecord.java,v 1.6 2008/08/12 20:15:34 vivekr Exp $
 *
 */
package com.misys.ub.fontis;

import java.math.BigDecimal;

public class FON_DebitRecord {

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
	 * The number of this debit record within the instruction
	 */
	private int debitNo;

	/**
	 * Debit Account Source Code
	 */
	private String debitSourceCode;

	/**
	 * Debit Account Number.
	 */
	private String debitAccountNo;

	/**
	 * Debit Account BIC Code
	 */
	private String debitBICCode;

	/**
	 * Debit Account Name
	 */
	private String debitAccountName;

	/**
	 * Debit Default Name
	 */
	private String debitDefaultName;

	/**
	 * Code for Account Type
	 */
	private String debitAccountType;

	/**
	 * Code for account currency
	 */
	private String debitCurrencyCode;

	/**
	 * Position account for debit currency
	 */
	private String debitCurrencyPositionAccount;

	/**
	 * Exchange rate for debit currency
	 */
	private BigDecimal debitExchangeRate;

	/**
	 * Multiply-Divide flag for debit currency
	 */
	private char debitMultiplyDevideFlag;

	/**
	 * Dealers rate
	 */
	private String debitDealersRate;

	/**
	 * Dealer’s Name
	 */
	private String debitDealersName;

	/**
	 * Debit date quoted
	 */
	private String debitDateQuoted;

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
	 * Transaction amount no. decimals (calculated)
	 */
	private int noOfDecimalsInTransactionAmount;

	/**
	 * exchange rate for transaction currency
	 */
	private BigDecimal transactionExchangeRate;

	/**
	 * Equivalent Payment Amount
	 */
	private BigDecimal equivalentAmount;

	/**
	 * no. of decimals for Base currency (read from CURRENCY table)
	 */
	private int noOfDecimalsInEquivalentAmount;

	/**
	 * Amount in Banks Base Currency
	 */
	private BigDecimal amountInBaseCurrency;

	/**
	 * Indicates which amount was entered.
	 * ‘A’ - means the amount was entered,
	 * ‘E’ - means the equivalent amount was entered
	 */
	private char amountUsed;

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
	 * Flag to indicate this debit leg should be posted to fontis suspense account
	 */
	private boolean debitFromFontisSuspenseAccount = false;

	/**
	 * Flag to indicate this debit leg should be posted to currency suspense account
	 */
	private boolean debitFromCurrSuspenseAccount = false;

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
	 *  TRUE : Force post this transaction leg (DR)
	 *  FALSE : Do not force post this transaction leg (DR)
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

	public int getStatusFlag() {
		return statusFlag;
	}

	public void setStatusFlag(int statusFlag) {
		this.statusFlag = statusFlag;
	}

	public char getAmountUsed() {
		return amountUsed;
	}

	public void setAmountUsed(char amountUsed) {
		this.amountUsed = amountUsed;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDebitAccountName() {
		return debitAccountName;
	}

	public void setDebitAccountName(String debitAccountName) {
		this.debitAccountName = debitAccountName;
	}

	public String getDebitAccountNo() {
		return debitAccountNo;
	}

	public void setDebitAccountNo(String debitAccountNo) {
		this.debitAccountNo = debitAccountNo;
	}

	public String getDebitAccountType() {
		return debitAccountType;
	}

	public void setDebitAccountType(String debitAccountType) {
		this.debitAccountType = debitAccountType;
	}

	public String getDebitBICCode() {
		return debitBICCode;
	}

	public void setDebitBICCode(String debitBICCode) {
		this.debitBICCode = debitBICCode;
	}

	public String getDebitCurrencyCode() {
		return debitCurrencyCode;
	}

	public void setDebitCurrencyCode(String debitCurrencyCode) {
		this.debitCurrencyCode = debitCurrencyCode;
	}

	public String getDebitCurrencyPositionAccount() {
		return debitCurrencyPositionAccount;
	}

	public void setDebitCurrencyPositionAccount(String debitCurrencyPositionAccount) {
		this.debitCurrencyPositionAccount = debitCurrencyPositionAccount;
	}

	public String getDebitDateQuoted() {
		return debitDateQuoted;
	}

	public void setDebitDateQuoted(String debitDateQuoted) {
		this.debitDateQuoted = debitDateQuoted;
	}

	public String getDebitDealersName() {
		return debitDealersName;
	}

	public void setDebitDealersName(String debitDealersName) {
		this.debitDealersName = debitDealersName;
	}

	public String getDebitDealersRate() {
		return debitDealersRate;
	}

	public void setDebitDealersRate(String debitDealersRate) {
		this.debitDealersRate = debitDealersRate;
	}

	public String getDebitDefaultName() {
		return debitDefaultName;
	}

	public void setDebitDefaultName(String debitDefaultName) {
		this.debitDefaultName = debitDefaultName;
	}

	public BigDecimal getDebitExchangeRate() {
		return debitExchangeRate;
	}

	public void setDebitExchangeRate(BigDecimal debitExchangeRate) {
		this.debitExchangeRate = debitExchangeRate;
	}

	public char getDebitMultiplyDevideFlag() {
		return debitMultiplyDevideFlag;
	}

	public void setDebitMultiplyDevideFlag(char debitMultiplyDevideFlag) {
		this.debitMultiplyDevideFlag = debitMultiplyDevideFlag;
	}

	public int getDebitNo() {
		return debitNo;
	}

	public void setDebitNo(int debitNo) {
		this.debitNo = debitNo;
	}

	public String getDebitSourceCode() {
		return debitSourceCode;
	}

	public void setDebitSourceCode(String debitSourceCode) {
		this.debitSourceCode = debitSourceCode;
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

	public void setPerticulars(String particulars) {
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

	public boolean isDebitFromFontisSuspenseAccount() {
		return debitFromFontisSuspenseAccount;
	}

	public void setDebitFromFontisSuspenseAccount(boolean debitFromFontisSuspenseAccount) {
		this.debitFromFontisSuspenseAccount = debitFromFontisSuspenseAccount;
	}

	public boolean isDebitFromCurrSuspenseAccount() {
		return debitFromCurrSuspenseAccount;
	}

	public void setDebitFromCurrSuspenseAccount(boolean debitFromCurrSuspenseAccount) {
		this.debitFromCurrSuspenseAccount = debitFromCurrSuspenseAccount;
	}

	public boolean isForcePost() {
		return forcePost;
	}

	public void setForcePost(boolean forcePost) {
		this.forcePost = forcePost;
	}
}
