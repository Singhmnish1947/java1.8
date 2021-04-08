/**
 * * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 
 */
package com.misys.ub.fircosoft;

/**
 * @author Gaurav.Aggarwal
 *
 */
import java.util.ArrayList;

import com.trapedza.bankfusion.core.CommonConstants;


public class HitDetail {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	String Ofacid = CommonConstants.EMPTY_STRING; 
	String Match = CommonConstants.EMPTY_STRING; 
	String Matchingtext = CommonConstants.EMPTY_STRING; 
	String Name = CommonConstants.EMPTY_STRING; 
	String Namesynonyms = CommonConstants.EMPTY_STRING; 
	String Address = CommonConstants.EMPTY_STRING; 
	String Addresssynonyms = CommonConstants.EMPTY_STRING; 
	String City = CommonConstants.EMPTY_STRING; 
	String Citysynonyms = CommonConstants.EMPTY_STRING; 
	String Country = CommonConstants.EMPTY_STRING; 
	String Countrysynonyms = CommonConstants.EMPTY_STRING; 
	String Origin = CommonConstants.EMPTY_STRING; 
	String Designation = CommonConstants.EMPTY_STRING; 
	String Type = CommonConstants.EMPTY_STRING; 
	String Officialref = CommonConstants.EMPTY_STRING; 
	String Passport = CommonConstants.EMPTY_STRING; 
	String Biccodes = CommonConstants.EMPTY_STRING; 
	String Natid = CommonConstants.EMPTY_STRING; 
	String Placeofbirth = CommonConstants.EMPTY_STRING; 
	String Dateofbirth = CommonConstants.EMPTY_STRING; 
	String Nationality = CommonConstants.EMPTY_STRING; 
	String Keywords = CommonConstants.EMPTY_STRING; 
	String NameSynonymsNoOfHits = CommonConstants.EMPTY_STRING; 
	String CitySynonymsNoOfHits = CommonConstants.EMPTY_STRING; 
	String CountrySynonymsNoOfHits = CommonConstants.EMPTY_STRING; 
	String AddressSynonymsNoOfHits = CommonConstants.EMPTY_STRING; 
	
	public String getAddress() {
		return Address;
	}
	public void setAddress(String address) {
		Address = address;
	}
	public String getAddresssynonyms() {
		return Addresssynonyms;
	}
	public void setAddresssynonyms(String addresssynonyms) {
		Addresssynonyms = addresssynonyms;
	}
	public String getBiccodes() {
		return Biccodes;
	}
	public void setBiccodes(String biccodes) {
		Biccodes = biccodes;
	}
	public String getCity() {
		return City;
	}
	public void setCity(String city) {
		City = city;
	}
	public String getCitysynonyms() {
		return Citysynonyms;
	}
	public void setCitysynonyms(String citysynonyms) {
		Citysynonyms = citysynonyms;
	}
	public String getCountry() {
		return Country;
	}
	public void setCountry(String country) {
		Country = country;
	}
	public String getCountrysynonyms() {
		return Countrysynonyms;
	}
	public void setCountrysynonyms(String countrysynonyms) {
		Countrysynonyms = countrysynonyms;
	}
	public String getDateofbirth() {
		return Dateofbirth;
	}
	public void setDateofbirth(String dateofbirth) {
		Dateofbirth = dateofbirth;
	}
	public String getDesignation() {
		return Designation;
	}
	public void setDesignation(String designation) {
		Designation = designation;
	}
	public String getKeywords() {
		return Keywords;
	}
	public void setKeywords(String keywords) {
		Keywords = keywords;
	}
	public String getMatch() {
		return Match;
	}
	public void setMatch(String match) {
		Match = match;
	}
	public String getMatchingtext() {
		return Matchingtext;
	}
	public void setMatchingtext(String matchingtext) {
		Matchingtext = matchingtext;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getNamesynonyms() {
		return Namesynonyms;
	}
	public void setNamesynonyms(String namesynonyms) {
		Namesynonyms = namesynonyms;
	}
	public String getNatid() {
		return Natid;
	}
	public void setNatid(String natid) {
		Natid = natid;
	}
	public String getNationality() {
		return Nationality;
	}
	public void setNationality(String nationality) {
		Nationality = nationality;
	}
	public String getOfacid() {
		return Ofacid;
	}
	public void setOfacid(String ofacid) {
		Ofacid = ofacid;
	}
	public String getOfficialref() {
		return Officialref;
	}
	public void setOfficialref(String officialref) {
		Officialref = officialref;
	}
	public String getOrigin() {
		return Origin;
	}
	public void setOrigin(String origin) {
		Origin = origin;
	}
	public String getPassport() {
		return Passport;
	}
	public void setPassport(String passport) {
		Passport = passport;
	}
	public String getPlaceofbirth() {
		return Placeofbirth;
	}
	public void setPlaceofbirth(String placeofbirth) {
		Placeofbirth = placeofbirth;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getAddressSynonymsNoOfHits() {
		return AddressSynonymsNoOfHits;
	}
	public void setAddressSynonymsNoOfHits(String addressSynonymsNoOfHits) {
		AddressSynonymsNoOfHits = addressSynonymsNoOfHits;
	}
	public String getCitySynonymsNoOfHits() {
		return CitySynonymsNoOfHits;
	}
	public void setCitySynonymsNoOfHits(String citySynonymsNoOfHits) {
		CitySynonymsNoOfHits = citySynonymsNoOfHits;
	}
	public String getCountrySynonymsNoOfHits() {
		return CountrySynonymsNoOfHits;
	}
	public void setCountrySynonymsNoOfHits(String countrySynonymsNoOfHits) {
		CountrySynonymsNoOfHits = countrySynonymsNoOfHits;
	}
	public String getNameSynonymsNoOfHits() {
		return NameSynonymsNoOfHits;
	}
	public void setNameSynonymsNoOfHits(String nameSynonymsNoOfHits) {
		NameSynonymsNoOfHits = nameSynonymsNoOfHits;
	}
	
}
