package com.misys.ub.swift;

import java.util.ArrayList;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT300 extends SwiftHeader {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	// Following Fields are for Sequence A
	private String senderReference = CommonConstants.EMPTY_STRING;
	private String relatedReference = CommonConstants.EMPTY_STRING;
	private String typeOfOperation = CommonConstants.EMPTY_STRING;
	private String scopeOfOperation = CommonConstants.EMPTY_STRING;
	private String commonReference = CommonConstants.EMPTY_STRING;
	private String blockTradeIndicator = CommonConstants.EMPTY_STRING;
	private String spiltSettlementIndicator = CommonConstants.EMPTY_STRING;
	private String partyA = CommonConstants.EMPTY_STRING;
	private String partyAOption = CommonConstants.EMPTY_STRING;
	private String partyB = CommonConstants.EMPTY_STRING;
	private String partyBOption = CommonConstants.EMPTY_STRING;
	private String fundOrBeneficaryCustomer = CommonConstants.EMPTY_STRING;
	private String fundOrBenCustOption = CommonConstants.EMPTY_STRING;
	private String termsAndConditions = CommonConstants.EMPTY_STRING;
	// Following Fields are for Sequence B	    
	private String tradeDate = CommonConstants.EMPTY_STRING;
	private String valueDate = CommonConstants.EMPTY_STRING;
	private String exchangeRate = CommonConstants.EMPTY_STRING;
	private String b1CurrencyAmount = CommonConstants.EMPTY_STRING;
	private String b1DeliveryAgent = CommonConstants.EMPTY_STRING;
	private String b1DeliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String b1Intermediary = CommonConstants.EMPTY_STRING;
	private String b1IntermediaryOption = CommonConstants.EMPTY_STRING;
	private String b1ReceivingAgent = CommonConstants.EMPTY_STRING;
	private String b1ReceivingAgentOption = CommonConstants.EMPTY_STRING;
	private String b2CurrencyAmount = CommonConstants.EMPTY_STRING;
	private String b2DeliveryAgent = CommonConstants.EMPTY_STRING;
	private String b2DeliveryAgentOption = CommonConstants.EMPTY_STRING;
	private String b2Intermediary = CommonConstants.EMPTY_STRING;
	private String b2IntermediaryOption = CommonConstants.EMPTY_STRING;
	private String b2ReceivingAgent = CommonConstants.EMPTY_STRING;
	private String b2ReceivingAgentOption = CommonConstants.EMPTY_STRING;
	private String beneficiary = CommonConstants.EMPTY_STRING;
	private String beneficiaryOption = CommonConstants.EMPTY_STRING;
	// Following Fields are for Sequence C    	
	private String contactInformation = CommonConstants.EMPTY_STRING;
	private String dealingMethod = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyA = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyAOption = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyB = CommonConstants.EMPTY_STRING;
	private String dealingBranchPartyBOption = CommonConstants.EMPTY_STRING;
	private String brokerID = CommonConstants.EMPTY_STRING;
	private String brokerIDOption = CommonConstants.EMPTY_STRING;
	private String brokersCommission = CommonConstants.EMPTY_STRING;
	private String counterPartysReference = CommonConstants.EMPTY_STRING;
	private String brokersReference = CommonConstants.EMPTY_STRING;
	private String sendersToReceiversInfo = CommonConstants.EMPTY_STRING;
	// SWIFT 2017 added fields
	private String NonDeliverableIndicator = CommonConstants.EMPTY_STRING;
	private String valuationDate = CommonConstants.EMPTY_STRING;
	private String NDFOpenIndicator = CommonConstants.EMPTY_STRING;
	private String settlementCurrency = CommonConstants.EMPTY_STRING;
	private String ASettlementRateSource = CommonConstants.EMPTY_STRING;
	private String refOpeningConfirmation = CommonConstants.EMPTY_STRING;
	private String clearingSettlementSession = CommonConstants.EMPTY_STRING;
	private String paymentClearingCentre = CommonConstants.EMPTY_STRING;
		
    // Following Fields are for Sequence D    	
	private ArrayList splitSettlement = new ArrayList();
	private String numberOfSettlement = CommonConstants.EMPTY_STRING;

	public UB_MT300() {
		// TODO Auto-generated constructor stub
	}

	public String getNonDeliverableIndicator() {
		return NonDeliverableIndicator;
	}

	public void setNonDeliverableIndicator(String NonDeliverableIndicator) {
		this.NonDeliverableIndicator = NonDeliverableIndicator;
	}

	public String getValuationDate() {
		return valuationDate;
	}

	public void setValuationDate(String valuationDate) {
		this.valuationDate = valuationDate;
	}

	public String getNDFOpenIndicator() {
		return NDFOpenIndicator;
	}

	public void setNDFOpenIndicator(String nDFOpenIndicator) {
		NDFOpenIndicator = nDFOpenIndicator;
	}

	public String getSettlementCurrency() {
		return settlementCurrency;
	}

	public void setSettlementCurrency(String settlementCurrency) {
		this.settlementCurrency = settlementCurrency;
	}

	public String getASettlementRateSource() {
		return ASettlementRateSource;
	}

	public void setASettlementRateSource(String aSettlementRateSource) {
		ASettlementRateSource = aSettlementRateSource;
	}

	public String getRefOpeningConfirmation() {
		return refOpeningConfirmation;
	}

	public void setRefOpeningConfirmation(String refOpeningConfirmation) {
		this.refOpeningConfirmation = refOpeningConfirmation;
	}

	public String getClearingSettlementSession() {
		return clearingSettlementSession;
	}

	public void setClearingSettlementSession(String clearingSettlementSession) {
		this.clearingSettlementSession = clearingSettlementSession;
	}

	public String getB1CurrencyAmount() {
		return b1CurrencyAmount;
	}

	public void setB1CurrencyAmount(String currencyAmount) {
		b1CurrencyAmount = currencyAmount;
	}

	public String getB1DeliveryAgent() {
		return b1DeliveryAgent;
	}

	public void setB1DeliveryAgent(String deliveryAgent) {
		b1DeliveryAgent = deliveryAgent;
	}

	public String getB1DeliveryAgentOption() {
		return b1DeliveryAgentOption;
	}

	public void setB1DeliveryAgentOption(String deliveryAgentOption) {
		b1DeliveryAgentOption = deliveryAgentOption;
	}

	public String getB1Intermediary() {
		return b1Intermediary;
	}

	public void setB1Intermediary(String intermediary) {
		b1Intermediary = intermediary;
	}

	public String getB1IntermediaryOption() {
		return b1IntermediaryOption;
	}

	public void setB1IntermediaryOption(String intermediaryOption) {
		b1IntermediaryOption = intermediaryOption;
	}

	public String getB1ReceivingAgent() {
		return b1ReceivingAgent;
	}

	public void setB1ReceivingAgent(String receivingAgent) {
		b1ReceivingAgent = receivingAgent;
	}

	public String getB1ReceivingAgentOption() {
		return b1ReceivingAgentOption;
	}

	public void setB1ReceivingAgentOption(String receivingAgentOption) {
		b1ReceivingAgentOption = receivingAgentOption;
	}

	public String getB2CurrencyAmount() {
		return b2CurrencyAmount;
	}

	public void setB2CurrencyAmount(String currencyAmount) {
		b2CurrencyAmount = currencyAmount;
	}

	public String getB2DeliveryAgent() {
		return b2DeliveryAgent;
	}

	public void setB2DeliveryAgent(String deliveryAgent) {
		b2DeliveryAgent = deliveryAgent;
	}

	public String getB2DeliveryAgentOption() {
		return b2DeliveryAgentOption;
	}

	public void setB2DeliveryAgentOption(String deliveryAgentOption) {
		b2DeliveryAgentOption = deliveryAgentOption;
	}

	public String getB2Intermediary() {
		return b2Intermediary;
	}

	public void setB2Intermediary(String intermediary) {
		b2Intermediary = intermediary;
	}

	public String getB2IntermediaryOption() {
		return b2IntermediaryOption;
	}

	public void setB2IntermediaryOption(String intermediaryOption) {
		b2IntermediaryOption = intermediaryOption;
	}

	public String getB2ReceivingAgent() {
		return b2ReceivingAgent;
	}

	public void setB2ReceivingAgent(String receivingAgent) {
		b2ReceivingAgent = receivingAgent;
	}

	public String getB2ReceivingAgentOption() {
		return b2ReceivingAgentOption;
	}

	public void setB2ReceivingAgentOption(String receivingAgentOption) {
		b2ReceivingAgentOption = receivingAgentOption;
	}

	public String getBeneficiary() {
		return beneficiary;
	}

	public void setBeneficiary(String beneficiary) {
		this.beneficiary = beneficiary;
	}

	public String getBeneficiaryOption() {
		return beneficiaryOption;
	}

	public void setBeneficiaryOption(String beneficiaryOption) {
		this.beneficiaryOption = beneficiaryOption;
	}

	public String getBlockTradeIndicator() {
		return blockTradeIndicator;
	}

	public void setBlockTradeIndicator(String blockTradeIndicator) {
		this.blockTradeIndicator = blockTradeIndicator;
	}

	public String getBrokerID() {
		return brokerID;
	}

	public void setBrokerID(String brokerID) {
		this.brokerID = brokerID;
	}

	public String getBrokerIDOption() {
		return brokerIDOption;
	}

	public void setBrokerIDOption(String brokerIDOption) {
		this.brokerIDOption = brokerIDOption;
	}

	public String getBrokersCommission() {
		return brokersCommission;
	}

	public void setBrokersCommission(String brokersCommission) {
		this.brokersCommission = brokersCommission;
	}

	public String getBrokersReference() {
		return brokersReference;
	}

	public void setBrokersReference(String brokersReference) {
		this.brokersReference = brokersReference;
	}

	public String getCommonReference() {
		return commonReference;
	}

	public void setCommonReference(String commonReference) {
		this.commonReference = commonReference;
	}

	public String getContactInformation() {
		return contactInformation;
	}

	public void setContactInformation(String contactInformation) {
		this.contactInformation = contactInformation;
	}

	public String getCounterPartysReference() {
		return counterPartysReference;
	}

	public void setCounterPartysReference(String counterPartysReference) {
		this.counterPartysReference = counterPartysReference;
	}

	public String getDealingBranchPartyA() {
		return dealingBranchPartyA;
	}

	public void setDealingBranchPartyA(String dealingBranchPartyA) {
		this.dealingBranchPartyA = dealingBranchPartyA;
	}

	public String getDealingBranchPartyAOption() {
		return dealingBranchPartyAOption;
	}

	public void setDealingBranchPartyAOption(String dealingBranchPartyAOption) {
		this.dealingBranchPartyAOption = dealingBranchPartyAOption;
	}

	public String getDealingBranchPartyB() {
		return dealingBranchPartyB;
	}

	public void setDealingBranchPartyB(String dealingBranchPartyB) {
		this.dealingBranchPartyB = dealingBranchPartyB;
	}

	public String getDealingBranchPartyBOption() {
		return dealingBranchPartyBOption;
	}

	public void setDealingBranchPartyBOption(String dealingBranchPartyBOption) {
		this.dealingBranchPartyBOption = dealingBranchPartyBOption;
	}

	public String getDealingMethod() {
		return dealingMethod;
	}

	public void setDealingMethod(String dealingMethod) {
		this.dealingMethod = dealingMethod;
	}

	public String getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(String exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getFundOrBenCustOption() {
		return fundOrBenCustOption;
	}

	public void setFundOrBenCustOption(String fundOrBenCustOption) {
		this.fundOrBenCustOption = fundOrBenCustOption;
	}

	public String getFundOrBeneficaryCustomer() {
		return fundOrBeneficaryCustomer;
	}

	public void setFundOrBeneficaryCustomer(String fundOrBeneficaryCustomer) {
		this.fundOrBeneficaryCustomer = fundOrBeneficaryCustomer;
	}

	public String getNumberOfSettlement() {
		return numberOfSettlement;
	}

	public void setNumberOfSettlement(String numberOfSettlement) {
		this.numberOfSettlement = numberOfSettlement;
	}

	public String getPartyA() {
		return partyA;
	}

	public void setPartyA(String partyA) {
		this.partyA = partyA;
	}

	public String getPartyAOption() {
		return partyAOption;
	}

	public void setPartyAOption(String partyAOption) {
		this.partyAOption = partyAOption;
	}

	public String getPartyB() {
		return partyB;
	}

	public void setPartyB(String partyB) {
		this.partyB = partyB;
	}

	public String getPartyBOption() {
		return partyBOption;
	}

	public void setPartyBOption(String partyBOption) {
		this.partyBOption = partyBOption;
	}

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReference(String relatedReference) {
		this.relatedReference = relatedReference;
	}

	public String getScopeOfOperation() {
		return scopeOfOperation;
	}

	public void setScopeOfOperation(String scopeOfOperation) {
		this.scopeOfOperation = scopeOfOperation;
	}

	public String getSenderReference() {
		return senderReference;
	}

	public void setSenderReference(String senderReference) {
		this.senderReference = senderReference;
	}

	public String getSendersToReceiversInfo() {
		return sendersToReceiversInfo;
	}

	public void setSendersToReceiversInfo(String sendersToReceiversInfo) {
		this.sendersToReceiversInfo = sendersToReceiversInfo;
	}

	public String getSpiltSettlementIndicator() {
		return spiltSettlementIndicator;
	}

	public void setSpiltSettlementIndicator(String spiltSettlementIndicator) {
		this.spiltSettlementIndicator = spiltSettlementIndicator;
	}

	public ArrayList getSplitSettlement() {
		return splitSettlement;
	}

	public void setSplitSettlement(ArrayList splitSettlement) {
		this.splitSettlement = splitSettlement;
	}

	public String getTermsAndConditions() {
		return termsAndConditions;
	}

	public void setTermsAndConditions(String termsAndConditions) {
		this.termsAndConditions = termsAndConditions;
	}

	public String getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}

	public String getTypeOfOperation() {
		return typeOfOperation;
	}

	public void setTypeOfOperation(String typeOfOperation) {
		this.typeOfOperation = typeOfOperation;
	}

	public String getValueDate() {
		return valueDate;
	}

	public void setValueDate(String valueDate) {
		this.valueDate = valueDate;
	}
	
	   
    public String getPaymentClearingCentre() {
        return paymentClearingCentre;
    }

    public void setPaymentClearingCentre(String PaymentClearingCentre) {
        this.paymentClearingCentre = PaymentClearingCentre;
    }

	public void addSettlementDetails(SplitSettlementDetails details) {
		this.splitSettlement.add(details);
	}
	
	
}
