package com.misys.ub.payment.swift.posting;

import java.math.BigDecimal;
import java.util.Date;

import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessage;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessageInput;

import com.trapedza.bankfusion.core.VectorTable;

public class PostingDto {
	/**
	 * @param args
	 */
	private String BranchCode;
	private String channelID;
	private String originalTxnReference;
	private boolean payReceiveFlag;
	private Date manualValueDate;
	private boolean offlineMode;
	private boolean postChargeSeparate;
	private String transactionID;
	private PostingEngineMessageInput postingMsgInput;
	private PostingEngineMessage[] postingMsgArray;
	private RsHeader rsHeader;
	private VectorTable ChargesList;
	private String localAmountAccountID;
	private BigDecimal localAmountAmount;
	private String localAmountAmount_CurrCode;
	private String localAmountISOCurrencyCode;
	private String localAmountTransactionNarrative;
	private String localChargeAmountAccountID;
	private BigDecimal localChargeAmountAmount;
	private String localChargeAmountAmount_CurrCode;
	private String localChargeAmountISOCurrecny;
	private String localChargeAmountTransactionNarrative;
	private String localAmountTransactionCode;

	public String getLocalAmountTransactionCode() {
        return localAmountTransactionCode;
    }

    public void setLocalAmountTransactionCode(String localAmountTransactionCode) {
        this.localAmountTransactionCode = localAmountTransactionCode;
    }

    public PostingEngineMessage[] getPostingMsgArray() {
		return postingMsgArray;
	}

	public void setPostingMsgArray(PostingEngineMessage[] postingMsgArray) {
		this.postingMsgArray = postingMsgArray;
	}

	public VectorTable getChargesList() {
		return ChargesList;
	}

	public void setChargesList(VectorTable chargesList) {
		ChargesList = chargesList;
	}

	public String getBranchCode() {
		return BranchCode;
	}

	public void setBranchCode(String branchCode) {
		BranchCode = branchCode;
	}

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getOriginalTxnReference() {
		return originalTxnReference;
	}

	public void setOriginalTxnReference(String originalTxnReference) {
		this.originalTxnReference = originalTxnReference;
	}

	public boolean isPayReceiveFlag() {
		return payReceiveFlag;
	}

	public void setPayReceiveFlag(boolean payReceiveFlag) {
		this.payReceiveFlag = payReceiveFlag;
	}

	public Date getManualValueDate() {
		return manualValueDate;
	}

	public void setManualValueDate(Date manualValueDate) {
		this.manualValueDate = manualValueDate;
	}

	public boolean isOfflineMode() {
		return offlineMode;
	}

	public void setOfflineMode(boolean offlineMode) {
		this.offlineMode = offlineMode;
	}

	public boolean isPostChargeSeparate() {
		return postChargeSeparate;
	}

	public void setPostChargeSeparate(boolean postChargeSeparate) {
		this.postChargeSeparate = postChargeSeparate;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public PostingEngineMessageInput getPostingMsgInput() {
		return postingMsgInput;
	}

	public void setPostingMsgInput(PostingEngineMessageInput postingMsgInput) {
		this.postingMsgInput = postingMsgInput;
	}

	public RsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(RsHeader rsHeader) {
		this.rsHeader = rsHeader;
	}

	public String getLocalAmountAccountID() {
		return localAmountAccountID;
	}

	public void setLocalAmountAccountID(String localAmountAccountID) {
		this.localAmountAccountID = localAmountAccountID;
	}

	public BigDecimal getLocalAmountAmount() {
		return localAmountAmount;
	}

	public void setLocalAmountAmount(BigDecimal localAmountAmount) {
		this.localAmountAmount = localAmountAmount;
	}

	public String getLocalAmountAmount_CurrCode() {
		return localAmountAmount_CurrCode;
	}

	public void setLocalAmountAmount_CurrCode(String localAmountAmount_CurrCode) {
		this.localAmountAmount_CurrCode = localAmountAmount_CurrCode;
	}

	public String getLocalAmountISOCurrencyCode() {
		return localAmountISOCurrencyCode;
	}

	public void setLocalAmountISOCurrencyCode(String localAmountISOCurrencyCode) {
		this.localAmountISOCurrencyCode = localAmountISOCurrencyCode;
	}

	public String getLocalAmountTransactionNarrative() {
		return localAmountTransactionNarrative;
	}

	public void setLocalAmountTransactionNarrative(String localAmountTransactionNarrative) {
		this.localAmountTransactionNarrative = localAmountTransactionNarrative;
	}

	public String getLocalChargeAmountAccountID() {
		return localChargeAmountAccountID;
	}

	public void setLocalChargeAmountAccountID(String localChargeAmountAccountID) {
		this.localChargeAmountAccountID = localChargeAmountAccountID;
	}

	public BigDecimal getLocalChargeAmountAmount() {
		return localChargeAmountAmount;
	}

	public void setLocalChargeAmountAmount(BigDecimal localChargeAmountAmount) {
		this.localChargeAmountAmount = localChargeAmountAmount;
	}

	public String getLocalChargeAmountAmount_CurrCode() {
		return localChargeAmountAmount_CurrCode;
	}

	public void setLocalChargeAmountAmount_CurrCode(String localChargeAmountAmount_CurrCode) {
		this.localChargeAmountAmount_CurrCode = localChargeAmountAmount_CurrCode;
	}

	public String getLocalChargeAmountISOCurrecny() {
		return localChargeAmountISOCurrecny;
	}

	public void setLocalChargeAmountISOCurrecny(String localChargeAmountISOCurrecny) {
		this.localChargeAmountISOCurrecny = localChargeAmountISOCurrecny;
	}

	public String getLocalChargeAmountTransactionNarrative() {
		return localChargeAmountTransactionNarrative;
	}

	public void setLocalChargeAmountTransactionNarrative(String localChargeAmountTransactionNarrative) {
		this.localChargeAmountTransactionNarrative = localChargeAmountTransactionNarrative;
	}
}
