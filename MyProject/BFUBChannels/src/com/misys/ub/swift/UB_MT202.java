package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT202 extends SwiftHeader {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public UB_MT202() {

    }

    public String transactionReferenceNumber = CommonConstants.EMPTY_STRING;
    public String relatedReference = CommonConstants.EMPTY_STRING;
    public String tdValueDate = CommonConstants.EMPTY_STRING;
    public String tdCurrencyCode = CommonConstants.EMPTY_STRING;
    public String tdAmount = CommonConstants.EMPTY_STRING;
    public String orderingInstitution = CommonConstants.EMPTY_STRING;
    public String orderingInstitutionOption = CommonConstants.EMPTY_STRING;
    public String sendersCorrespondent = CommonConstants.EMPTY_STRING;
    public String sendersCorrespondentOption = CommonConstants.EMPTY_STRING;
    public String receiversCorrespondent = CommonConstants.EMPTY_STRING;
    public String receiversCorrespondentOption = CommonConstants.EMPTY_STRING;
    public String intermediary = CommonConstants.EMPTY_STRING;
    public String intermediaryOption = CommonConstants.EMPTY_STRING;
    public String accountWithInstitution = CommonConstants.EMPTY_STRING;
    public String accountWithInstitutionOption = CommonConstants.EMPTY_STRING;
    public String beneficiary = CommonConstants.EMPTY_STRING;
    public String beneficiaryOption = CommonConstants.EMPTY_STRING;
    public String sendertoReceiverInformation = CommonConstants.EMPTY_STRING;
    public String coverMessage = CommonConstants.EMPTY_STRING;
    public String end2EndTxnRef = CommonConstants.EMPTY_STRING;
    public String serviceTypeId = CommonConstants.EMPTY_STRING;


	public String getCoverMessage() {
        return coverMessage;
    }

    public void setCoverMessage(String coverMessage) {
        this.coverMessage = coverMessage;
    }

    public String getAccountWithInstitution() {
        return accountWithInstitution;
    }

    public void setAccountWithInstitution(String accountWithInstitution) {
        this.accountWithInstitution = accountWithInstitution;
    }

    public String getAccountWithInstitutionOption() {
        return accountWithInstitutionOption;
    }

    public void setAccountWithInstitutionOption(String accountWithInstitutionOption) {
        this.accountWithInstitutionOption = accountWithInstitutionOption;
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

    public String getOrderingInstitution() {
        return orderingInstitution;
    }

    public void setOrderingInstitution(String orderingInstitution) {
        this.orderingInstitution = orderingInstitution;
    }

    public String getOrderingInstitutionOption() {
        return orderingInstitutionOption;
    }

    public void setOrderingInstitutionOption(String orderingInstitutionOption) {
        this.orderingInstitutionOption = orderingInstitutionOption;
    }

    public String getReceiversCorrespondent() {
        return receiversCorrespondent;
    }

    public void setReceiversCorrespondent(String receiversCorrespondent) {
        this.receiversCorrespondent = receiversCorrespondent;
    }

    public String getReceiversCorrespondentOption() {
        return receiversCorrespondentOption;
    }

    public void setReceiversCorrespondentOption(String receiversCorrespondentOption) {
        this.receiversCorrespondentOption = receiversCorrespondentOption;
    }

    public String getRelatedReference() {
        return relatedReference;
    }

    public void setRelatedReference(String relatedReference) {
        this.relatedReference = relatedReference;
    }

    public String getSendersCorrespondent() {
        return sendersCorrespondent;
    }

    public void setSendersCorrespondent(String sendersCorrespondent) {
        this.sendersCorrespondent = sendersCorrespondent;
    }

    public String getSendersCorrespondentOption() {
        return sendersCorrespondentOption;
    }

    public void setSendersCorrespondentOption(String sendersCorrespondentOption) {
        this.sendersCorrespondentOption = sendersCorrespondentOption;
    }

    public String getSendertoReceiverInformation() {
        return sendertoReceiverInformation;
    }

    public void setSendertoReceiverInformation(String sendertoReceiverInformation) {
        this.sendertoReceiverInformation = sendertoReceiverInformation;
    }

    public String getTdAmount() {
        return tdAmount;
    }

    public void setTdAmount(String tdAmount) {
        this.tdAmount = tdAmount;
    }

    public String getTdCurrencyCode() {
        return tdCurrencyCode;
    }

    public void setTdCurrencyCode(String tdCurrencyCode) {
        this.tdCurrencyCode = tdCurrencyCode;
    }

    public String getTdValueDate() {
        return tdValueDate;
    }

    public void setTdValueDate(String tdValueDate) {
        this.tdValueDate = tdValueDate;
    }

    public String getTransactionReferenceNumber() {
        return transactionReferenceNumber;
    }

    public void setTransactionReferenceNumber(String transactionReferenceNumber) {
        this.transactionReferenceNumber = transactionReferenceNumber;
    }
    
    public String getEnd2EndTxnRef() {
		return end2EndTxnRef;
	}

	public void setEnd2EndTxnRef(String end2EndTxnRef) {
		this.end2EndTxnRef = end2EndTxnRef;
	}

	public String getServiceTypeId() {
		return serviceTypeId;
	}
	
	public void setServiceTypeId(String serviceTypeId) {
		this.serviceTypeId = serviceTypeId;
	}

}