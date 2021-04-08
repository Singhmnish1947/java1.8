package com.trapedza.bankfusion.fatoms;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.fircosoft.HitDetail;
import com.misys.ub.fircosoft.HitDetails;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_KYC_FIC_StringSplit;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_KYC_FIC_StringSplit extends AbstractUB_KYC_FIC_StringSplit {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(UB_KYC_FIC_StringSplit.class.getName());

	public UB_KYC_FIC_StringSplit(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		super.process(env);

		try {
			// This process will receive the string message from the server of
			// the fircosoft and convert it into the XML
			// Basically in it we are getting the values by checking the Tag
			// Name with :
			StringBuffer responseMessage = new StringBuffer(getF_IN_Message());
			String[] hits = new String[20];
			String checkString = "";
			String tempString = "";
			String matchString = "";
			String none = "none";
			int maxNoOfHits = 20;
			int noOfHits = 0;
			int flag = 0;
			// Main Processing Start

			if ((responseMessage.indexOf("Suspect detected #")) >= 0) {
				// remove the header from the response
				responseMessage.replace(0, (responseMessage.indexOf("Suspect detected #")), "");
				responseMessage = responseMessage.replace(0, (responseMessage.indexOf("Suspect detected #")), "");
				// Substrings of the hits start
				while ((responseMessage.indexOf("Suspect detected #")) != -1 && maxNoOfHits <= 20) {
					hits[noOfHits] = responseMessage.substring(0, (responseMessage.indexOf("=============================")));
					responseMessage = responseMessage.replace(0, (responseMessage.indexOf("Suspect detected #") + 19), "");
					noOfHits = noOfHits + 1;
				}
				hits[noOfHits] = responseMessage.substring(0, (responseMessage.indexOf("=============================")));
				int j = 1;
				while (j <= noOfHits) {
					j = j + 1;
				}
				// Substrings of the hits end

				// to get the details of the hit
				HitDetails hitdetails = new HitDetails();
				for (int i = 1; i <= noOfHits; i++) {
					String ofacID = CommonConstants.EMPTY_STRING;
					String match = CommonConstants.EMPTY_STRING;
					String tag = CommonConstants.EMPTY_STRING;
					String matchingText = CommonConstants.EMPTY_STRING;
					String name = CommonConstants.EMPTY_STRING;
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// To get ihe Ofac id
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("MATCH:") == 0) {
							ofacID = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (ofacID.compareTo(none) == 0) {
								ofacID = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the match details
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("TAG:") == 0) {
							match = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (match.compareTo(none) == 0) {
								match = "";
							}
						}
						hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					}
					// To get the Tag details
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("MATCHINGTEXT:") == 0) {
							tag = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (tag.compareTo(none) == 0) {
								tag = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the matching details
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("NAME:") == 0) {
							matchingText = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (matchingText.compareTo(none) == 0) {
								matchingText = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Name details
					name = hits[i].substring(0, hits[i].indexOf("Synonyms:")).trim();
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// Name details ends
					// Name Synonyms start
					String[] nameSynonyms = new String[40];
					String synonymsName = CommonConstants.EMPTY_STRING;
					int maxNoOfNameSynonyms = 40;
					int noOfNameSynonyms = 0;
					String temp = hits[i].substring(0, hits[i].indexOf("ADDRESS:")).trim();
					if ((temp.indexOf("-")) >= 0) {
						temp = temp.substring(temp.indexOf("-") + 1, temp.length());
						while ((temp.indexOf("-")) != -1 && maxNoOfNameSynonyms <= 40) {
							nameSynonyms[noOfNameSynonyms] = temp.substring(0, temp.indexOf("-")).trim();
							temp = temp.substring(temp.indexOf("-") + 1, temp.length());
							noOfNameSynonyms = noOfNameSynonyms + 1;
						}
						nameSynonyms[noOfNameSynonyms] = temp.substring(0, temp.length()).trim();
						noOfNameSynonyms = noOfNameSynonyms + 1;
						j = 0;
						while (j < noOfNameSynonyms) {
							synonymsName = synonymsName + "%" + nameSynonyms[j];
							j = j + 1;
						}
					}
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// Name Synonym Ends
					// Address Start
					String address = CommonConstants.EMPTY_STRING;
					address = hits[i].substring(0, hits[i].indexOf("Synonyms:")).trim();
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// Address End
					// Address Synonyms start
					String[] addressSynonyms = new String[40];
					String synonymsAddress = CommonConstants.EMPTY_STRING;
					int maxNoOfAddressSynonyms = 40;
					int noOfAddressSynonyms = 0;
					temp = hits[i].substring(0, hits[i].indexOf("CITY:")).trim();
					if ((temp.indexOf("-")) >= 0) {
						temp = temp.substring(temp.indexOf("-") + 1, temp.length());
						while ((temp.indexOf("-")) != -1 && maxNoOfAddressSynonyms <= 40) {
							addressSynonyms[noOfAddressSynonyms] = temp.substring(0, temp.indexOf("-")).trim();
							temp = temp.substring(temp.indexOf("-") + 1, temp.length());
							noOfAddressSynonyms = noOfAddressSynonyms + 1;
						}
						addressSynonyms[noOfAddressSynonyms] = temp.substring(0, temp.length()).trim();
						noOfAddressSynonyms = noOfAddressSynonyms + 1;
						j = 0;
						while (j < noOfAddressSynonyms) {
							synonymsAddress = synonymsAddress + "%" + addressSynonyms[j];
							j = j + 1;
						}
					}
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// Address Synonyms Ends
					// City Start
					String city = CommonConstants.EMPTY_STRING;
					city = hits[i].substring(0, hits[i].indexOf("Synonyms:")).trim();
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// City End
					// City Synonyms start
					String[] citySynonyms = new String[40];
					String synonymsCity = CommonConstants.EMPTY_STRING;
					int maxNoOfCitySynonyms = 40;
					int noOfCitySynonyms = 0;
					temp = hits[i].substring(0, hits[i].indexOf("COUNTRY:")).trim();
					if ((temp.indexOf("-")) >= 0) {
						temp = temp.substring(temp.indexOf("-") + 1, temp.length());
						while ((temp.indexOf("-")) != -1 && maxNoOfCitySynonyms <= 40) {
							citySynonyms[noOfCitySynonyms] = temp.substring(0, temp.indexOf("-")).trim();
							temp = temp.substring(temp.indexOf("-") + 1, temp.length());
							noOfCitySynonyms = noOfCitySynonyms + 1;
						}
						citySynonyms[noOfCitySynonyms] = temp.substring(0, temp.length()).trim();
						noOfCitySynonyms = noOfCitySynonyms + 1;
						j = 0;
						while (j < noOfCitySynonyms) {
							synonymsCity = synonymsCity + "%" + citySynonyms[j];
							j = j + 1;
						}
					}
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// City Synonyms Ends
					// Country Synonyms Start
					String country = CommonConstants.EMPTY_STRING;
					country = hits[i].substring(0, hits[i].indexOf("Synonyms:")).trim();
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// Country End
					// Country Synonyms start
					String[] countrySynonyms = new String[40];
					String synonymsCountry = CommonConstants.EMPTY_STRING;
					int maxNoOfCountrySynonyms = 40;
					int noOfCountrySynonyms = 0;
					temp = hits[i].substring(0, hits[i].indexOf("STATE:")).trim();
					if ((temp.indexOf("-")) >= 0) {
						temp = temp.substring(temp.indexOf("-") + 1, temp.length());
						while ((temp.indexOf("-")) != -1 && maxNoOfCountrySynonyms <= 40) {
							countrySynonyms[noOfCountrySynonyms] = temp.substring(0, temp.indexOf("-")).trim();
							temp = temp.substring(temp.indexOf("-") + 1, temp.length());
							noOfCountrySynonyms = noOfCountrySynonyms + 1;
						}
						countrySynonyms[noOfCountrySynonyms] = temp.substring(0, temp.length()).trim();
						noOfCountrySynonyms = noOfCountrySynonyms + 1;
						j = 0;
						while (j < noOfCountrySynonyms) {
							synonymsCountry = synonymsCountry + "%" + countrySynonyms[j];
							j = j + 1;
						}
					}
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// Country Synonyms Ends
					// State Start
					String state = CommonConstants.EMPTY_STRING;
					state = hits[i].substring(0, hits[i].indexOf("Synonyms:")).trim();
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// State End
					// State Synonyms start
					String[] stateSynonyms = new String[40];
					String synonymsState = CommonConstants.EMPTY_STRING;
					int maxNoOfStateSynonyms = 40;
					int noOfStateSynonyms = 0;
					checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
					matchString = check(checkString);
					temp = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
					if ((temp.indexOf("-")) >= 0) {
						temp = temp.substring(temp.indexOf("-") + 1, temp.length());
						while ((temp.indexOf("-")) != -1 && maxNoOfStateSynonyms <= 40) {
							stateSynonyms[noOfStateSynonyms] = temp.substring(0, temp.indexOf("-")).trim();
							temp = temp.substring(temp.indexOf("-") + 1, temp.length());
							noOfStateSynonyms = noOfStateSynonyms + 1;
						}
						stateSynonyms[noOfStateSynonyms] = temp.substring(0, temp.length()).trim();
						noOfStateSynonyms = noOfStateSynonyms + 1;
						j = 0;
						while (j < noOfStateSynonyms) {
							synonymsState = synonymsState + "%" + stateSynonyms[j];
							j = j + 1;
						}
					}
					hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					// State Synonyms Ends
					// To get the orgin
					String origin = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("DESIGNATION:") == 0) {
							origin = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (origin.compareTo(none) == 0) {
								origin = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the designation
					String designation = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("TYPE:") == 0) {
							designation = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (designation.compareTo(none) == 0) {
								designation = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Type
					String type = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("SEARCH CODES:") == 0) {
							type = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (type.compareTo(none) == 0) {
								type = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the search codes
					String searchCodes = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("USER DATA 1:") == 0) {
							searchCodes = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (searchCodes.compareTo(none) == 0) {
								searchCodes = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the User data 1
					String userData1 = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("USER DATA 2:") == 0) {
							userData1 = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (userData1.compareTo(none) == 0) {
								userData1 = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the user data 2
					String userData2 = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("OFFICIAL REF:") == 0) {
							userData2 = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (userData2.compareTo(none) == 0) {
								userData2 = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the official ref
					String officialRef = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("PASSPORT:") == 0) {
							officialRef = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (officialRef.compareTo(none) == 0) {
								officialRef = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the paassport details
					String passport = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("BIC CODES:") == 0) {
							passport = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (passport.compareTo(none) == 0) {
								passport = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the BicCodes
					String biccodes = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("NATID:") == 0) {
							biccodes = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (biccodes.compareTo(none) == 0) {
								biccodes = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the NatId
					String natID = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("PLACE OF BIRTH:") == 0) {
							natID = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (natID.compareTo(none) == 0) {
								natID = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Place of Birth
					String placeOfBirth = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("DATE OF BIRTH:") == 0) {
							placeOfBirth = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (placeOfBirth.compareTo(none) == 0) {
								placeOfBirth = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Date of Birth
					String dateOfBirth = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
						}
						if (matchString.compareTo("ADDITIONAL INFOS:") == 0) {
							dateOfBirth = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (dateOfBirth.compareTo(none) == 0) {
								dateOfBirth = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the additional Info
					String additionalInfos = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("NATIONALITY:") == 0) {
							additionalInfos = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (additionalInfos.compareTo(none) == 0) {
								additionalInfos = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Nationality
					String nationality = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("FML TYPE:") == 0) {
							nationality = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (nationality.compareTo(none) == 0) {
								nationality = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Fml Type
					String fmlType = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("FML PRIORITY:") == 0) {
							fmlType = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (fmlType.compareTo(none) == 0) {
								fmlType = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the FMl Priority
					String fmlPriority = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("FML CONFIDENTIALITY:") == 0) {
							fmlPriority = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (fmlPriority.compareTo(none) == 0) {
								fmlPriority = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the FML Confidentiality
					String fmlConfidentiality = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("FML INFO:") == 0) {
							fmlConfidentiality = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (fmlConfidentiality.compareTo(none) == 0) {
								fmlConfidentiality = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the FML Info
					String fmlInfo = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("PEP-FEP:") == 0) {
							fmlInfo = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (fmlInfo.compareTo(none) == 0) {
								fmlInfo = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Pep-Fep
					String pepFep = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("KEYWORDS:") == 0) {
							pepFep = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (pepFep.compareTo(none) == 0) {
								pepFep = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Keywords
					String keywords = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("HYPERLINKS:") == 0) {
							keywords = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (keywords.compareTo(none) == 0) {
								keywords = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the Hyperlink
					String hyperlink = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("TYS:") == 0) {
							hyperlink = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (hyperlink.compareTo(none) == 0) {
								hyperlink = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the TYS
					String tys = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0 && hits[i].indexOf(":") != -1) {
						checkString = hits[i].substring(0, hits[i].indexOf(":") + 1).trim();
						matchString = check(checkString);
						tempString = hits[i];
						while (matchString.endsWith(":") != true) {
							tempString = tempString.substring((tempString.indexOf(":") + 1), tempString.length());
							checkString = tempString.substring(0, tempString.indexOf(":") + 1);
							matchString = check(checkString);
							flag = 1;
						}
						if (flag == 1) {
							hits[i] = tempString;
							flag = 0;
						}
						if (matchString.compareTo("ISN:") == 0) {
							tys = hits[i].substring(0, hits[i].indexOf(matchString)).trim();
							if (tys.compareTo(none) == 0) {
								tys = "";
							}
							hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
						}
					}
					// To get the ISN
					String isn = CommonConstants.EMPTY_STRING;
					if (hits[i].length() > 0) {
						isn = hits[i].substring(0, hits[i].length()).trim();
						if (isn.compareTo(none) == 0) {
							isn = "";
						}
						hits[i] = hits[i].substring((hits[i].indexOf(":") + 1), hits[i].length());
					}
					// XML Generation Part
					HitDetail hitdetail = new HitDetail();
					hitdetail.setAddress(address);
					hitdetail.setAddresssynonyms(synonymsAddress);
					hitdetail.setAddressSynonymsNoOfHits(Integer.toString(noOfAddressSynonyms));
					hitdetail.setBiccodes(biccodes);
					hitdetail.setCity(city);
					hitdetail.setCitysynonyms(synonymsCity);
					hitdetail.setCitySynonymsNoOfHits(Integer.toString(noOfCitySynonyms));
					hitdetail.setCountry(country);
					hitdetail.setCountrysynonyms(synonymsCountry);
					hitdetail.setCountrySynonymsNoOfHits(Integer.toString(noOfCountrySynonyms));
					hitdetail.setDateofbirth(dateOfBirth);
					hitdetail.setDesignation(designation);
					hitdetail.setMatch(match);
					hitdetail.setMatchingtext(matchingText);
					hitdetail.setName(name);
					hitdetail.setNamesynonyms(synonymsName);
					hitdetail.setNameSynonymsNoOfHits(Integer.toString(noOfNameSynonyms));
					hitdetail.setNatid(natID);
					hitdetail.setNationality(nationality);
					hitdetail.setOfacid(ofacID);
					hitdetail.setOfficialref(officialRef);
					hitdetail.setOrigin(origin);
					hitdetail.setPassport(passport);
					hitdetail.setPlaceofbirth(placeOfBirth);
					hitdetail.setType(type);
					String messageXML = generateXML(hitdetail);
					String newStr = null;
					// TODO Auto-generated method stub
					Pattern p = Pattern.compile("\\n");
					Matcher m = p.matcher(messageXML);
					if (m.find(0)) {
						newStr = m.replaceAll("");
					} else {
						newStr = messageXML;
					}
					hitdetails.addHitDetail(hitdetail);
					String Message = null;
					String reference = null;
					String source = null;
					String customerCode = null;
					String hit = null;
					String guID = GUIDGen.getNewGUID();
					if (guID.length() < 25) {
						String x = "";
						int len = 0;
						len = guID.length();
						len = 25 - len;
						for (int k = 0; k <= len; k++) {
							x = x + 'x';
						}
						guID = guID + x;
					}
					reference = getF_IN_Reference();
					source = getF_IN_Source();
					source = "fircosoft";
					customerCode = getF_IN_CustomerCode();
					hit = Integer.toString(noOfHits);
					if (hit.length() < 2) {
						hit = '0' + hit;
					}
					if (customerCode.length() < 10) {
						String x = "";
						int len = 0;
						len = customerCode.length();
						len = 10 - len;
						for (int k = 0; k <= len; k++) {
							x = x + ' ';
						}
						customerCode = customerCode + x;
					}
					if (reference.length() < 10) {
						String x = "";
						int len = 0;
						len = reference.length();
						len = 10 - len;
						for (int k = 0; k <= len; k++) {
							x = x + ' ';
						}
						reference = reference + x;
					}
					Message = guID + reference + source + customerCode + hit + newStr;
					if (logger.isInfoEnabled()) {
						logger.info(Message);

					}
				}
				// Store all the info in the Main Details Object

				hitdetails.setRequestMessage(getF_IN_RequestMessage());
				hitdetails.setNoOfHits(Integer.toString(noOfHits));

				String messageXML = generateXML(hitdetails);
			}
		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}

	}

	// To generate the XML
	public String generateXML(Object messageObject) {
		StringWriter xmlWriter = new StringWriter();
		String configLocation = null;
		try {
			// Properties props =
			// LocalConfiguration.getInstance().getProperties();
			// props.setProperty("org.exolab.castor.indent", "false");
			XMLContext xmlContext = new XMLContext();
			xmlContext.setProperty("org.exolab.castor.indent", "false");

			Marshaller marshaller = new Marshaller(xmlWriter);

			Mapping mapping = new Mapping(getClass().getClassLoader());
			//configLocation = System.getProperty("BFconfigLocation", CommonConstants.EMPTY_STRING);
			configLocation = GetUBConfigLocation.getUBConfigLocation();
			mapping.loadMapping(configLocation + "conf/interface/" + "FircosoftMessageMapping.xml");
			marshaller.setMapping(mapping);
			marshaller.marshal(messageObject);

		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
		return xmlWriter.toString();

	}

	// To check the Tag of the Information
	public String check(String checkStr) {
		String str = "";
		if (checkStr.contains("MATCH:")) {
			if (checkStr.contains("MATCHINGTEXT:")) {
				str = "MATCHINGTEXT:";
			} else {
				str = "MATCH:";
			}
		} else if (checkStr.contains("TAG:")) {
			str = "TAG:";
		} else if (checkStr.contains("MATCHINGTEXT:")) {
			str = "MATCHINGTEXT:";
		} else if (checkStr.contains("NAME:")) {
			str = "NAME:";
		} else if (checkStr.contains("ADDRESS:")) {
			str = "ADDRESS:";
		} else if (checkStr.contains("CITY:")) {
			str = "CITY:";
		} else if (checkStr.contains("COUNTRY:")) {
			str = "COUNTRY:";
		} else if (checkStr.contains("STATE")) {
			str = "STATE:";
		} else if (checkStr.contains("ORIGIN:")) {
			str = "ORIGIN:";
		} else if (checkStr.contains("DESIGNATION:")) {
			str = "DESIGNATION:";
		} else if (checkStr.contains("TYPE:")) {
			if (checkStr.contains("FML TYPE:")) {
				str = "FML TYPE:";
			} else {
				str = "TYPE:";
			}
		} else if (checkStr.contains("SEARCH CODES:")) {
			str = "SEARCH CODES:";
		} else if (checkStr.contains("USER DATA 1:")) {
			str = "USER DATA 1:";
		} else if (checkStr.contains("USER DATA 2:")) {
			str = "USER DATA 2:";
		} else if (checkStr.contains("OFFICIAL REF:")) {
			str = "OFFICIAL REF:";
		} else if (checkStr.contains("PASSPORT:")) {
			str = "PASSPORT:";
		} else if (checkStr.contains("BIC CODES:")) {
			str = "BIC CODES:";
		} else if (checkStr.contains("NATID:")) {
			str = "NATID:";
		} else if (checkStr.contains("PLACE OF BIRTH:")) {
			str = "PLACE OF BIRTH:";
		} else if (checkStr.contains("DATE OF BIRTH:")) {
			str = "DATE OF BIRTH:";
		} else if (checkStr.contains("ADDITIONAL INFOS:")) {
			str = "ADDITIONAL INFOS:";
		} else if (checkStr.contains("NATIONALITY:")) {
			str = "NATIONALITY:";
		} else if (checkStr.contains("FML TYPE:")) {
			str = "FML TYPE:";
		} else if (checkStr.contains("FML PRIORITY:")) {
			str = "FML PRIORITY:";
		} else if (checkStr.contains("FML CONFIDENTIALITY:")) {
			str = "FML CONFIDENTIALITY:";
		} else if (checkStr.contains("FML INFO:")) {
			str = "FML INFO:";
		} else if (checkStr.contains("PEP-FEP:")) {
			str = "PEP-FEP:";
		} else if (checkStr.contains("KEYWORDS:")) {
			str = "KEYWORDS:";
		} else if (checkStr.contains("HYPERLINKS:")) {
			str = "HYPERLINKS:";
		} else if (checkStr.contains("TYS:")) {
			str = "TYS:";
		} else if (checkStr.contains("ISN:")) {
			str = "ISN:";
		} else {
			// It is the symbol of the Tag not found
			str = "NF";
		}
		return str;
	}

}
