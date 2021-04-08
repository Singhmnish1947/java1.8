package com.misys.ub.cc.types;

import com.misys.ub.utils.types.LineOfBusinessListRs;

public class AccountDetailsOverview {
	private ExtensiveAccountDetails extensiveAccountDetails;
	private String accStatus;
	private String relationshipManager;
	
	private LineOfBusinessListRs lineOfBusiness;

	public ExtensiveAccountDetails getExtensiveAccountDetails() {
		return extensiveAccountDetails;
	}

	public void setExtensiveAccountDetails(ExtensiveAccountDetails extensiveAccountDetails) {
		this.extensiveAccountDetails = extensiveAccountDetails;
	}

	public String getAccStatus() {
		return accStatus;
	}

	public void setAccStatus(String accStatus) {
		this.accStatus = accStatus;
	}

	public String getRelationshipManager() {
		return relationshipManager;
	}

	public void setRelationshipManager(String relationshipManager) {
		this.relationshipManager = relationshipManager;
	}
	
	public LineOfBusinessListRs getLineOfBusiness() {
		return lineOfBusiness;
	}
	
	public void setLineOfBusiness(LineOfBusinessListRs lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
	}
}
