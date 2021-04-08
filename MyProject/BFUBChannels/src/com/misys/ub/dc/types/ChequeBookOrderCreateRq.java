package com.misys.ub.dc.types;

public class ChequeBookOrderCreateRq {
	
	String customerId;
	String channelId;
	String account;
	String chequeBookType;
	String numberOfLeaves; 
	String collectAtBranch;
	String TxnReferenceID;
	
	public String getTxnReferenceID() {
		return TxnReferenceID;
	}

	public void setTxnReferenceID(String txnReferenceID) {
		TxnReferenceID = txnReferenceID;
	}

	public String getCollectAtBranch() {
		return collectAtBranch;
	}

	public void setCollectAtBranch(String collectAtBranch) {
		this.collectAtBranch = collectAtBranch;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}


	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getChequeBookType() {
		return chequeBookType;
	}

	public void setChequeBookType(String chequeBookType) {
		this.chequeBookType = chequeBookType;
	}

	public String getNumberOfLeaves() {
		return numberOfLeaves;
	}

	public void setNumberOfLeaves(String numberOfLeaves) {
		this.numberOfLeaves = numberOfLeaves;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}





}
