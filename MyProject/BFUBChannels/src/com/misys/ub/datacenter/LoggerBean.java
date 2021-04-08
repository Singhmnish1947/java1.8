package com.misys.ub.datacenter;

import java.sql.Timestamp;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;

public class LoggerBean {
	String txnRef = CommonConstants.EMPTY_STRING;
	String accountId = CommonConstants.EMPTY_STRING;
	Timestamp postingDateTime = null;
	String flagIndicator = CommonConstants.EMPTY_STRING;
	String value = CommonConstants.EMPTY_STRING;
	String eventCode = CommonConstants.EMPTY_STRING;
	String sourceBranchId = CommonConstants.EMPTY_STRING;
	String channelId = CommonConstants.EMPTY_STRING;

	Integer serialNo = 0;

	
	public Integer getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}

	public String getTxnRef() {
		return txnRef;
	}

	public void setTxnRef(String txnRef) {
		this.txnRef = txnRef;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public Timestamp getPostingDateTime() {
		return postingDateTime;
	}

	public void setPostingDateTime(Timestamp postingDateTime) {
		this.postingDateTime = postingDateTime;
	}

	public String getFlagIndicator() {
		return flagIndicator;
	}

	public void setFlagIndicator(String flagIndicator) {
		this.flagIndicator = flagIndicator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public String getSourceBranchId() {
		return sourceBranchId;
	}

	public void setSourceBranchId(String sourceBranchId) {
		this.sourceBranchId = sourceBranchId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

}
