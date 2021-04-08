package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class UB_MT205 extends UB_MT200 {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String relatedReference = CommonConstants.EMPTY_STRING;
	private String orderingInstitution = CommonConstants.EMPTY_STRING;
	private String orderingInstOption = CommonConstants.EMPTY_STRING;
	private String BeneficiaryInstitute = CommonConstants.EMPTY_STRING;
	private String BeneficiaryInstOption = CommonConstants.EMPTY_STRING;
	private String end2EndTxnRef = CommonConstants.EMPTY_STRING;
	private String serviceTypeId = CommonConstants.EMPTY_STRING;

	public UB_MT205() {
		// TODO Auto-generated constructor stub
	}

	public String getBeneficiaryInstitute() {
		return BeneficiaryInstitute;
	}

	public void setBeneficiaryInstitute(String beneficiaryInstitute) {
		BeneficiaryInstitute = beneficiaryInstitute;
	}

	public String getBeneficiaryInstOption() {
		return BeneficiaryInstOption;
	}

	public void setBeneficiaryInstOption(String beneficiaryInstOption) {
		BeneficiaryInstOption = beneficiaryInstOption;
	}

	public String getOrderingInstitution() {
		return orderingInstitution;
	}

	public void setOrderingInstitution(String orderingInstitution) {
		this.orderingInstitution = orderingInstitution;
	}

	public String getOrderingInstOption() {
		return orderingInstOption;
	}

	public void setOrderingInstOption(String orderingInstOption) {
		this.orderingInstOption = orderingInstOption;
	}

	public String getRelatedReference() {
		return relatedReference;
	}

	public void setRelatedReference(String relatedReference) {
		this.relatedReference = relatedReference;
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
