/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_Constants.java,v 1.5 2008/08/12 20:15:25 vivekr Exp $
 *  
 * $Log: MGM_Constants.java,v $
 * Revision 1.5  2008/08/12 20:15:25  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.3.4.1  2008/07/03 17:55:18  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/12 10:50:10  arun
 *  RIO on Head
 *
 * Revision 1.3  2007/09/28 12:18:05  nileshk
 * Constant added: AC_FEELOOKUPRESPONSE
 *
 * Revision 1.2  2007/09/28 04:59:25  nileshk
 * Constants added.
 *
 * Revision 1.1  2007/09/27 11:58:46  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */
package com.misys.ub.moneygram;

/**
 * This class contains the constants used for MoneyGram related fatoms.
 * @author nileshk
 *
 */
public class MGM_Constants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	//  Constants for MoneyGram xml response tags.
	public static final String AC_ACKNOWLEDGERESPONSE = "ac:acknowledgeResponse";
	public static final String AC_AGENTINFO = "ac:agentInfo";
	public static final String AC_STOREHOURS = "ac:storeHours";
	public static final String AC_ADDRESS = "ac:address";
	public static final String AC_AGENTNAME = "ac:agentName";
	public static final String AC_AGENTPHONE = "ac:agentPhone";
	public static final String AC_BANCOMERCONFIRMATIONNUMBER = "ac:bancomerConfirmationNumber";
	public static final String AC_CITY = "ac:city";
	public static final String AC_CLOSED = "ac:closed";
	public static final String AC_CLOSETIME = "ac:closeTime";
	public static final String AC_DATETIMESENT = "ac:dateTimeSent";
	public static final String AC_DELIVERYOPTION = "ac:deliveryOption";
	public static final String AC_DIRECTION1 = "ac:direction1";
	public static final String AC_DIRECTION2 = "ac:direction2";
	public static final String AC_DIRECTION3 = "ac:direction3";
	public static final String AC_ESTIMATEDEXCHANGERATE = "ac:estimatedExchangeRate";
	public static final String AC_ESTIMATEDRECEIVEAMOUNT = "ac:estimatedReceiveAmount";
	public static final String AC_ESTIMATEDRECEIVECURRENCY = "ac:estimatedReceiveCurrency";
	public static final String AC_EXCHANGERATEAPPLIED = "ac:exchangeRateApplied";
	public static final String AC_FEEAMOUNT = "ac:feeAmount";
	public static final String AC_FREEPHONECALLPIN = "ac:freePhoneCallPIN";
	public static final String AC_MESSAGEFIELD1 = "ac:messageField1";
	public static final String AC_MESSAGEFIELD2 = "ac:messageField2";
	public static final String AC_MILESDIRECTIONS = "ac:milesDirections";
	public static final String AC_OKFORAGENT = "ac:okForAgent";
	public static final String AC_OPENTIME = "ac:openTime";
	public static final String AC_ORIGINALRECEIVEAMOUNT = "ac:originalReceiveAmount";
	public static final String AC_ORIGINALRECEIVECOUNTRY = "ac:originalReceiveCountry";
	public static final String AC_ORIGINALSENDAMOUNT = "ac:originalSendAmount";
	public static final String AC_ORIGINALSENDCURRENCY = "ac:originalSendCurrency";
	public static final String AC_ORIGINATINGCOUNTRY = "ac:originatingCountry";
	public static final String AC_PRODUCTTYPE = "ac:productType";
	public static final String AC_RECEIVEAMOUNT = "ac:receiveAmount";
	public static final String AC_RECEIVEAMOUNTALTERED = "ac:receiveAmountAltered";
	public static final String AC_RECEIVECAPABILITY = "ac:receiveCapability";
	public static final String AC_RECEIVECOUNTRY = "ac:receiveCountry";
	public static final String AC_RECEIVECURRENCY = "ac:receiveCurrency";
	public static final String AC_RECEIVERADDRESS = "ac:receiverAddress";
	public static final String AC_RECEIVERCITY = "ac:receiverCity";
	public static final String AC_RECEIVERCOLONIA = "ac:receiverColonia";
	public static final String AC_RECEIVERCOUNTRY = "ac:receiverCountry";
	public static final String AC_RECEIVERFIRSTNAME = "ac:receiverFirstName";
	public static final String AC_RECEIVERLASTNAME = "ac:receiverLastName";
	public static final String AC_RECEIVERLASTNAME2 = "ac:receiverLastName2";
	public static final String AC_RECEIVERMIDDLENAME = "ac:receiverMiddleName";
	public static final String AC_RECEIVERMUNICIPIO = "ac:receiverMunicipio";
	public static final String AC_RECEIVERPHONE = "ac:receiverPhone";
	public static final String AC_RECEIVERSTATE = "ac:receiverState";
	public static final String AC_RECEIVERZIPCODE = "ac:receiverZipCode";
	public static final String AC_REDIRECTINDICATOR = "ac:redirectIndicator";
	public static final String AC_REFERENCENUMBER = "ac:referenceNumber";
	public static final String AC_SENDAMOUNT = "ac:sendAmount";
	public static final String AC_SENDCAPABILITY = "ac:sendCapability";
	public static final String AC_SENDCURRENCY = "ac:sendCurrency";
	public static final String AC_SENDERFIRSTNAME = "ac:senderFirstName";
	public static final String AC_SENDERHOMEPHONE = "ac:senderHomePhone";
	public static final String AC_SENDERLASTNAME = "ac:senderLastName";
	public static final String AC_SENDERMIDDLEINITIAL = "ac:senderMiddleInitial";
	public static final String AC_STATE = "ac:state";
	public static final String AC_TESTANSWER = "ac:testAnswer";
	public static final String AC_TESTQUESTION = "ac:testQuestion";
	public static final String AC_TOKEN = "ac:token";
	public static final String AC_TOTALAMOUNT = "ac:totalAmount";
	public static final String AC_TOTALSENDAMOUNT = "ac:totalSendAmount";
	public static final String AC_TRANSACTIONSTATUS = "ac:transactionStatus";
	public static final String AC_VALIDEXCHANGERATE = "ac:validExchangeRate";
	public static final String AC_VALIDINDICATOR = "ac:validIndicator";
	public static final String AC_VALIDRECEIVEAMOUNT = "ac:validReceiveAmount";
	public static final String AC_VALIDRECEIVECURRENCY = "ac:validReceiveCurrency";
	public static final String AC_DOCHECKIN = "ac:doCheckIn";
	public static final String AC_TIMESTAMP = "ac:timeStamp";
	public static final String AC_FLAGS = "ac:flags";
	public static final String AC_DIRECTORYOFAGENTSBYCITYRESPONSE = "ac:directoryOfAgentsByCityResponse";
	public static final String AC_MONEYGRAMSENDRESPONSE = "ac:moneyGramSendResponse";
	public static final String AC_MONEYGRAMRECEIVERESPONSE = "ac:moneyGramReceiveResponse";
	public static final String AC_TRANSACTIONDATETIME = "ac:transactionDateTime";
	public static final String AC_AGENTCHECKAUTHORIZATIONNUMBER = "ac:agentCheckAuthorizationNumber";
	public static final String AC_CITYLISTRESPONSE = "ac:cityListResponse";
	public static final String AC_REFERENCENUMBERRESPONSE = "ac:referenceNumberResponse";
	public static final String AC_FREQCUSTCARDNUMBER = "ac:freqCustCardNumber";
	public static final String AC_NONDISCOUNTEDFEE = "ac:nonDiscountedFee";
	public static final String AC_TOLLFREEPHONENUMBER = "ac:tollFreePhoneNumber";
	public static final String AC_PAYOUTCURRENCY = "ac:payoutCurrency";
	public static final String SOAPENV_FAULT = "soapenv:Fault";
	public static final String AC_STATEPROVINCEINFO = "ac:stateProvinceInfo";
	public static final String AC_COUNTRYCODE = "ac:countryCode";
	public static final String AC_STATEPROVINCECODE = "ac:stateProvinceCode";
	public static final String AC_STATEPROVINCENAME = "ac:stateProvinceName";
	public static final String AC_COUNTRYINFO = "ac:countryInfo";
	public static final String AC_COUNTRYNAME = "ac:countryName";
	public static final String AC_COUNTRYLEGACYCODE = "ac:countryLegacyCode";
	public static final String AC_SENDACTIVE = "ac:sendActive";
	public static final String AC_RECEIVEACTIVE = "ac:receiveActive";
	public static final String AC_CURRENCYINFO = "ac:currencyInfo";
	public static final String AC_CURRENCYCODE = "ac:currencyCode";
	public static final String AC_CURRENCYNAME = "ac:currencyName";
	public static final String AC_CURRENCYPRECISION = "ac:currencyPrecision";
	public static final String AC_COUNTRYCURRENCYINFO = "ac:countryCurrencyInfo";
	public static final String AC_TRANSACTIONCURRENCY = "ac:transactionCurrency";
	public static final String AC_INDICATIVERATEAVAILABLE = "ac:indicativeRateAvailable";
	public static final String AC_PROFILEITEM = "ac:profileItem";
	public static final String AC_PRODUCTPROFILEITEM = "ac:productProfileItem";
	public static final String AC_INDEX = "ac:index";
	public static final String AC_KEY = "ac:key";
	public static final String AC_VALUE = "ac:value";
	public static final String AC_PRODUCTID = "ac:productID";
	public static final String AC_RECEIVECOUNTRYREQUIREMENTSINFO = "ac:receiveCountryRequirementsInfo";
	public static final String AC_RECEIVERADDRESSREQUIRED = "ac:receiverAddressRequired";
	public static final String AC_RECEIVER2NDLASTNAMEREQUIRED = "ac:receiver2ndLastNameRequired";
	public static final String AC_QUESTIONRESTRICTED = "ac:questionRestricted";
	public static final String AC_QUESTIONREQUIRED = "ac:questionRequired";
	public static final String AC_RECEIVEACTIVEFORAGENT = "ac:receiveActiveForAgent";
	public static final String AC_FEELOOKUPRESPONSE = "ac:feeLookupResponse";
	//  Constants for MGM_Refresh .sav file names.
	public static final String RECIEVE_COUNTRY_REFRESH_FILE = "breccou.sav";
	public static final String CURRENT_PROFILE_REFRESH_FILE_NAME = "bcurpro";
	public static final String CODE_COUNTRY_CURR_REFRESH_FILE = "bcodccu.sav";
	public static final String CODE_COUNTRY_REFRESH_FILE = "bcodcou.sav";
	public static final String CODE_CURRENCY_REFRESH_FILE = "bcodcur.sav";
	public static final String CODE_STATE_REFRESH_FILE = "bcodstat.sav";
	//  Constants for MoneyGram message type.
	public static final String MESSAGE_CITY_LIST = "MG07";
	public static final String MESSAGE_AGENT_LIST = "MG08";

}
