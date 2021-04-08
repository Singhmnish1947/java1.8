/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: UB_SWT_IdentifierCodeUploadRecord.java,v 1.1 2008/10/30 23:58:18 iteshk Exp $
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

public class UB_SWT_IdentifierCodeUploadRecord {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String recordType;
	private String modificationFlag;
	private String bicCode;
	private String institutionName1;
	private String accountLineNum;
	private String branchName1;
	private String cityHeading;
	private String isdeleted;
	public String getIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(String isdeleted) {
		this.isdeleted = isdeleted;
	}

	private ArrayList bicRecords = new ArrayList();

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getModificationFlag() {
		return modificationFlag;
	}

	public void setModificationFlag(String modificationFlag) {
		this.modificationFlag = modificationFlag;
	}

	public ArrayList getBicRecords() {
		return bicRecords;
	}

	public void setBicRecords(ArrayList bicRecords) {
		this.bicRecords = bicRecords;
	}

	public String getBicCode() {
		return bicCode;
	}

	public void setBicCode(String bicCode) {
		this.bicCode = bicCode;
	}

	public String getInstitutionName1() {
		return institutionName1;
	}

	public void setInstitutionName1(String institutionName1) {
		this.institutionName1 = institutionName1;
	}

	public String getAccountLineNum() {
		return accountLineNum;
	}

	public void setAccountLineNum(String accountLineNum) {
		this.accountLineNum = accountLineNum;
	}

	public String getBranchName1() {
		return branchName1;
	}

	public void setBranchName1(String branchName1) {
		this.branchName1 = branchName1;
	}

	public String getCityHeading() {
		return cityHeading;
	}

	public void setCityHeading(String cityHeading) {
		this.cityHeading = cityHeading;
	}
}
