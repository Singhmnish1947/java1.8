package com.misys.ub.dc.types;

public class CreatePartyAndAccountRq {
    String uniqueID;
    String msgId;

    String partyID;
    String partyCategory;
    String partyType;
    String partySubtype;
    String partyTitle;
    String partyFirstname;
    String partyMiddlename;
    String partyLastname;
    String nationalId;
    String dateOfBirth;
    String citizenship;
    String fatherName;
    String grandFatherName;
    String motherMaidenName;
    String civilStatus;
    String employmentStatus;
    String gender;
    String birthTown;
    String residentCountry;
    String birthCountry;
    String shortName;
    String isTaxPayer;
    String branchCode;
    String isInternet;
    String isMobile;
    String altID;
    String contactMethod;
    String contactType;
    String contactValue;
    String fromDate;
    String addressType;
    String addressLine1;
    String addressLine2;
    String addressLine3;
    String addressLine4;
    String addressLine5;
    String townorCity;
    String residentState;
    String addressFromDate;
    String religion;
    String postalCode;
    String countryCode;
    String taxRegId;
    String email;
    String contactMethodEmail;
    String contactTypeEmail;
    String residentStatus;
    String isdCode;
    String isDefaultAddress;
    String docCategory;
    String docType;
    String validFromDate;
    String validToDate;

    String issueAuthority;
    String kycExpDate;
    String docVerified;
    String issueCountry;
    String foreignCountryTax;
    String taxClassification;
    String taxSubClassification;
    String reportingType;
    String reportingStatus;
    String isReportingReq;
    String docRef;

    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }

    public String getIsReportingReq() {
        return isReportingReq;
    }

    public void setIsReportingReq(String isReportingReq) {
        this.isReportingReq = isReportingReq;
    }

    String tin;
    String nationalTypeID;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getNationalTypeID() {
        return nationalTypeID;
    }

    public void setNationalTypeID(String nationalTypeID) {
        this.nationalTypeID = nationalTypeID;
    }

    public String getResidentStatus() {
        return residentStatus;
    }

    public void setResidentStatus(String residentStatus) {
        this.residentStatus = residentStatus;
    }

    public String getIsdCode() {
        return isdCode;
    }

    public void setIsdCode(String isdCode) {
        this.isdCode = isdCode;
    }

    public String getIsDefaultAddress() {
        return isDefaultAddress;
    }

    public void setIsDefaultAddress(String isDefaultAddress) {
        this.isDefaultAddress = isDefaultAddress;
    }

    public String getDocCategory() {
        return docCategory;
    }

    public void setDocCategory(String docCategory) {
        this.docCategory = docCategory;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public String getValidToDate() {
        return validToDate;
    }

    public void setValidToDate(String validToDate) {
        this.validToDate = validToDate;
    }

    public String getIssueAuthority() {
        return issueAuthority;
    }

    public void setIssueAuthority(String issueAuthority) {
        this.issueAuthority = issueAuthority;
    }

    public String getKycExpDate() {
        return kycExpDate;
    }

    public void setKycExpDate(String kycExpDate) {
        this.kycExpDate = kycExpDate;
    }

    public String getDocVerified() {
        return docVerified;
    }

    public void setDocVerified(String docVerified) {
        this.docVerified = docVerified;
    }

    public String getIssueCountry() {
        return issueCountry;
    }

    public void setIssueCountry(String issueCountry) {
        this.issueCountry = issueCountry;
    }

    public String getForeignCountryTax() {
        return foreignCountryTax;
    }

    public void setForeignCountryTax(String foreignCountryTax) {
        this.foreignCountryTax = foreignCountryTax;
    }

    public String getTaxClassification() {
        return taxClassification;
    }

    public void setTaxClassification(String taxClassification) {
        this.taxClassification = taxClassification;
    }

    public String getTaxSubClassification() {
        return taxSubClassification;
    }

    public void setTaxSubClassification(String taxSubClassification) {
        this.taxSubClassification = taxSubClassification;
    }

    public String getReportingType() {
        return reportingType;
    }

    public void setReportingType(String reportingType) {
        this.reportingType = reportingType;
    }

    public String getReportingStatus() {
        return reportingStatus;
    }

    public void setReportingStatus(String reportingStatus) {
        this.reportingStatus = reportingStatus;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    boolean isWhtLiable;
    String taxCountry;
    String taxId;
    String lineOfBusiness;
    // AccountRq
    String accCust;
    String accCurr;
    String accBranchcode;
    String accOpenDate;
    String subProdID;

    public String getAddressFromDate() {
        return addressFromDate;
    }

    public void setAddressFromDate(String addressFromDate) {
        this.addressFromDate = addressFromDate;
    }

    public String getAddressLine5() {
        return addressLine5;
    }

    public void setAddressLine5(String addressLine5) {
        this.addressLine5 = addressLine5;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getAccCust() {
        return accCust;
    }

    public void setAccCust(String accCust) {
        this.accCust = accCust;
    }

    public String getAccCurr() {
        return accCurr;
    }

    public void setAccCurr(String accCurr) {
        this.accCurr = accCurr;
    }

    public String getAccBranchcode() {
        return accBranchcode;
    }

    public void setAccBranchcode(String accBranchcode) {
        this.accBranchcode = accBranchcode;
    }

    public String getAccOpenDate() {
        return accOpenDate;
    }

    public void setAccOpenDate(String accOpenDate) {
        this.accOpenDate = accOpenDate;
    }

    public String getSubProdID() {
        return subProdID;
    }

    public void setSubProdID(String subProdID) {
        this.subProdID = subProdID;
    }

    public String getPartyID() {
        return partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

    public String getPartyCategory() {
        return partyCategory;
    }

    public void setPartyCategory(String partyCategory) {
        this.partyCategory = partyCategory;
    }

    public String getPartyType() {
        return partyType;
    }

    public void setPartyType(String partyType) {
        this.partyType = partyType;
    }

    public String getPartySubtype() {
        return partySubtype;
    }

    public void setPartySubtype(String partySubtype) {
        this.partySubtype = partySubtype;
    }

    public String getPartyTitle() {
        return partyTitle;
    }

    public void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

    public String getPartyFirstname() {
        return partyFirstname;
    }

    public void setPartyFirstname(String partyFirstname) {
        this.partyFirstname = partyFirstname;
    }

    public String getPartyMiddlename() {
        return partyMiddlename;
    }

    public void setPartyMiddlename(String partyMiddlename) {
        this.partyMiddlename = partyMiddlename;
    }

    public String getPartyLastname() {
        return partyLastname;
    }

    public void setPartyLastname(String partyLastname) {
        this.partyLastname = partyLastname;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getGrandFatherName() {
        return grandFatherName;
    }

    public void setGrandFatherName(String grandFatherName) {
        this.grandFatherName = grandFatherName;
    }

    public String getMotherMaidenName() {
        return motherMaidenName;
    }

    public void setMotherMaidenName(String motherMaidenName) {
        this.motherMaidenName = motherMaidenName;
    }

    public String getCivilStatus() {
        return civilStatus;
    }

    public void setCivilStatus(String civilStatus) {
        this.civilStatus = civilStatus;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthTown() {
        return birthTown;
    }

    public void setBirthTown(String birthTown) {
        this.birthTown = birthTown;
    }

    public String getResidentCountry() {
        return residentCountry;
    }

    public void setResidentCountry(String residentCountry) {
        this.residentCountry = residentCountry;
    }

    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getAltID() {
        return altID;
    }

    public void setAltID(String altID) {
        this.altID = altID;
    }

    public String getContactMethod() {
        return contactMethod;
    }

    public void setContactMethod(String contactMethod) {
        this.contactMethod = contactMethod;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getContactValue() {
        return contactValue;
    }

    public void setContactValue(String contactValue) {
        this.contactValue = contactValue;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String addressLine4) {
        this.addressLine4 = addressLine4;
    }

    public String getTownorCity() {
        return townorCity;
    }

    public void setTownorCity(String townorCity) {
        this.townorCity = townorCity;
    }

    public String getResidentState() {
        return residentState;
    }

    public void setResidentState(String residentState) {
        this.residentState = residentState;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getIsTaxPayer() {
        return isTaxPayer;
    }

    public void setIsTaxPayer(String isTaxPayer) {
        this.isTaxPayer = isTaxPayer;
    }

    public String getIsInternet() {
        return isInternet;
    }

    public void setIsInternet(String isInternet) {
        this.isInternet = isInternet;
    }

    public String getIsMobile() {
        return isMobile;
    }

    public void setIsMobile(String isMobile) {
        this.isMobile = isMobile;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isWhtLiable() {
        return isWhtLiable;
    }

    public void setWhtLiable(boolean isWhtLiable) {
        this.isWhtLiable = isWhtLiable;
    }

    public String getTaxCountry() {
        return taxCountry;
    }

    public void setTaxCountry(String taxCountry) {
        this.taxCountry = taxCountry;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getLineOfBusiness() {
        return lineOfBusiness;
    }

    public void setLineOfBusiness(String lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactMethodEmail() {
        return contactMethodEmail;
    }

    public void setContactMethodEmail(String contactMethodEmail) {
        this.contactMethodEmail = contactMethodEmail;
    }

    public String getContactTypeEmail() {
        return contactTypeEmail;
    }

    public void setContactTypeEmail(String contactTypeEmail) {
        this.contactTypeEmail = contactTypeEmail;
    }

    public String getTaxRegId() {
        return taxRegId;
    }

    public void setTaxRegId(String taxRegId) {
        this.taxRegId = taxRegId;
    }

}
