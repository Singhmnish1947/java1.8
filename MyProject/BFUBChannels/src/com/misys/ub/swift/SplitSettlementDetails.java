/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.misys.ub.swift;

import java.io.Serializable;

import com.trapedza.bankfusion.core.CommonConstants;
/**
 * @author Vipesh
 * 
 */
public class SplitSettlementDetails   implements Serializable {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	//The following details are part of sequence D
	private String beneficiary = CommonConstants.EMPTY_STRING;
	private String buySellIndicator = CommonConstants.EMPTY_STRING;
	private String currencyAmount = CommonConstants.EMPTY_STRING;
	private String deliveryAgent = CommonConstants.EMPTY_STRING;
	private String deliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String intermediary = CommonConstants.EMPTY_STRING;
	private String intermediaryOption = CommonConstants.EMPTY_STRING;
	private String receivingAgent = CommonConstants.EMPTY_STRING;
	private String receivingAgentOption = CommonConstants.EMPTY_STRING;
	private String dbeneficiary = CommonConstants.EMPTY_STRING;
	private String dBeneficiaryOption = CommonConstants.EMPTY_STRING;

	public SplitSettlementDetails() {

	}

	public String getBeneficiary() {
		return beneficiary;
	}

	public void setBeneficiary(String beneficiary) {
		this.beneficiary = beneficiary;
	}

	public String getBuySellIndicator() {
		return buySellIndicator;
	}

	public void setBuySellIndicator(String buySellIndicator) {
		this.buySellIndicator = buySellIndicator;
	}

	public String getCurrencyAmount() {
		return currencyAmount;
	}

	public void setCurrencyAmount(String currencyAmount) {
		this.currencyAmount = currencyAmount;
	}

	public String getDbeneficiary() {
		return dbeneficiary;
	}

	public void setDbeneficiary(String dbeneficiary) {
		this.dbeneficiary = dbeneficiary;
	}

	public String getDBeneficiaryOption() {
		return dBeneficiaryOption;
	}

	public void setDBeneficiaryOption(String beneficiaryOption) {
		dBeneficiaryOption = beneficiaryOption;
	}

	public String getDeliveryAgent() {
		return deliveryAgent;
	}

	public void setDeliveryAgent(String deliveryAgent) {
		this.deliveryAgent = deliveryAgent;
	}

	public String getDeliveryAgentOption() {
		return deliveryAgentOption;
	}

	public void setDeliveryAgentOption(String deliveryAgentOption) {
		this.deliveryAgentOption = deliveryAgentOption;
	}

	public String getIntermediary() {
		return intermediary;
	}

	public void setIntermediary(String intermediary) {
		this.intermediary = intermediary;
	}

	public String getIntermediaryOption() {
		return intermediaryOption;
	}

	public void setIntermediaryOption(String intermediaryOption) {
		this.intermediaryOption = intermediaryOption;
	}

	public String getReceivingAgent() {
		return receivingAgent;
	}

	public void setReceivingAgent(String receivingAgent) {
		this.receivingAgent = receivingAgent;
	}

	public String getReceivingAgentOption() {
		return receivingAgentOption;
	}

	public void setReceivingAgentOption(String receivingAgentOption) {
		this.receivingAgentOption = receivingAgentOption;
	}

}
