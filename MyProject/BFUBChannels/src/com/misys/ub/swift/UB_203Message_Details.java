/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;
/**
 * @author Gaurav Aggarwal
 * 
 */
public class UB_203Message_Details {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String TRN = CommonConstants.EMPTY_STRING;
	private String currency = CommonConstants.EMPTY_STRING;
	private String amount = CommonConstants.EMPTY_STRING;
	private String intermediary = CommonConstants.EMPTY_STRING;
	private String intermediaryOption = CommonConstants.EMPTY_STRING;
	private String relatedReference = CommonConstants.EMPTY_STRING;
	private String accountWithInstitution = CommonConstants.EMPTY_STRING;
	private String accountWithInstitutionOption = CommonConstants.EMPTY_STRING;
	private String beneficiaryInstitution = CommonConstants.EMPTY_STRING;
	private String beneficiaryInstitutionOption = CommonConstants.EMPTY_STRING;
	private String senderToReceiverInformation = CommonConstants.EMPTY_STRING;

    public UB_203Message_Details(){

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

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBeneficiaryInstitution() {
		return beneficiaryInstitution;
	}

	public void setBeneficiaryInstitution(String beneficiaryInstitution) {
		this.beneficiaryInstitution = beneficiaryInstitution;
	}

	public String getBeneficiaryInstitutionOption() {
		return beneficiaryInstitutionOption;
	}

	public void setBeneficiaryInstitutionOption(String beneficiaryInstitutionOption) {
		this.beneficiaryInstitutionOption = beneficiaryInstitutionOption;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReferenceNumber(String relatedReference) {
		this.relatedReference = relatedReference;
	}

	public String getSenderToReceiverInformation() {
		return senderToReceiverInformation;
	}

	public void setSenderToReceiverInformation(String senderToReceiverInformation) {
		this.senderToReceiverInformation = senderToReceiverInformation;
	}

	public String getTRN() {
		return TRN;
	}

	public void setTRN(String trn) {
		TRN = trn;
	}

	

    
    
}

       
