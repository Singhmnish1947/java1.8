package com.misys.ub.dc.restServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;

import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.party.ws.RqPayload;
import bf.com.misys.party.ws.RsPayload;

import com.google.gson.JsonObject;
import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.dc.common.LocaleHelp;
import com.misys.ub.dc.types.CreatePartyAndAccountRq;
import com.misys.ub.dc.types.CreatePartyAndAccountRs;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

public class PartyCreateService {
	public static final String CAMEL_RESPONSE_ENDPOINT = "QM_BFDC_UB_Response";
	private final static Logger logger = Logger.getLogger(PartyCreateService.class.getName());
	public static final String DATE_FORMAT_SEPARATOR = "PARTY_DATE_FORMAT_SEPARATOR";
	public static final String COLUMN_SEPARATOR = "PARTY_COLUMN_SEPARATOR";
	public static final String DEFAULT_DATE_FORMAT_SEPARATOR = "�";
	public static final String DEFAULT_COLUMN_SEPARATOR = ";";
	
	public static final String PARTY_DATE_FORMAT_SEPARTOR = BankFusionPropertySupport.getProperty(DATE_FORMAT_SEPARATOR, DEFAULT_DATE_FORMAT_SEPARATOR);
	public static final String PARTY_COLUMN_SEPARATOR = BankFusionPropertySupport.getProperty(COLUMN_SEPARATOR, DEFAULT_COLUMN_SEPARATOR);
	
	private static final String PROCESS_ERROR = "E";
    private static final String PROCESS_SUCCESS = "S";
    private static final String PROCESS_FAILURE = "F";
    
    private SubCode subCode = new SubCode();
    private MessageStatus status = new MessageStatus();
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	  
	@SuppressWarnings("unchecked")
	public CreatePartyAndAccountRs update(CreatePartyAndAccountRq createRq) {
		CreatePartyAndAccountRs createRs = new CreatePartyAndAccountRs();
		StringBuilder rqParam = new StringBuilder(); 
		String subProd = null;
		String subProdCur = null;
		boolean isCustomerCreated = false;
		try {
			// Creating Party
			HashMap<String, Object> ptyParams = new HashMap<String, Object>();
			HashMap<String, Object> accParams = new HashMap<String, Object>();
			Map<String, Object> dataMap = new HashMap<String, Object>();
			
			ListIterator<SimplePersistentObject> resultIt;
			RqPayload rqPayload = new RqPayload();
			LocaleHelp.getInstance();
	        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	        IBOProductInheritance productInheritance = (IBOProductInheritance)factory.findByPrimaryKey(IBOProductInheritance.BONAME, createRq.getSubProdID());
	        if(productInheritance != null) {
	            subProd = productInheritance.getF_UBSUBPRODUCTID();
	            subProdCur = productInheritance.getF_ACC_ISOCURRENCYCODE();
	        }
	        logger.info("SubProduct is "+ subProd + " Sub Prod Currency " + subProdCur);

			rqParam.append(formReq("UNIQUE_ID=", createRq.getUniqueID()));		

			rqParam.append(formReq("PT_PFN_Party#PARTYCATEGORY=", createRq.getPartyCategory()));
			rqParam.append(formReq("PT_PFN_Party#PARTYTYPE=", createRq.getPartyType()));
			rqParam.append(formReq("PT_PFN_Party#PARTYSUBTYPE=", createRq.getPartySubtype()));
			createRq.setIsTaxPayer("false");
			if(createRq.getTaxId()!=null){
				createRq.setIsTaxPayer("true");
				rqParam.append(formReq("PT_PFN_Party#ISTAXPAYER=", createRq.getIsTaxPayer()));
			}
			rqParam.append(formReq("PT_PFN_Party#ASSIGNEDBRANCHCODE=", createRq.getBranchCode()));
			rqParam.append(formReq("PT_PFN_Party#isInternet=", createRq.getIsInternet()));
			rqParam.append(formReq("PT_PFN_Party#isMobile=", createRq.getIsMobile()));
			
			rqParam.append(formReq("PT_PFN_PersonNames#TITLE=", createRq.getPartyTitle()));
			rqParam.append(formReq("PT_PFN_PersonNames#FIRSTNAME=", createRq.getPartyFirstname()));
			rqParam.append(formReq("PT_PFN_PersonNames#MIDDLENAME=", createRq.getPartyMiddlename()));
			rqParam.append(formReq("PT_PFN_PersonNames#LASTNAME=", createRq.getPartyLastname()));

			rqParam.append(formReq("PT_PFN_PersonalDetails#DATEOFBIRTH=", createRq.getDateOfBirth(), true));
			rqParam.append(formReq("PT_PFN_PersonalDetails#COUNTRYCITIZENSHIP=",LocaleHelp.getIso3CountryFromIso2Country(createRq.getCitizenship())));
			rqParam.append(formReq("PT_PFN_PersonalDetails#FATHERSNAME=", createRq.getFatherName()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#GRANDFATHERSNAME=", createRq.getGrandFatherName()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#MOTHERSMAIDENNAME=", createRq.getMotherMaidenName()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#EMPLOYMENTSTATUS=", createRq.getEmploymentStatus()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#GENDER=", createRq.getGender()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#NATIONALIDTYPEID=", createRq.getNationalTypeID()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#NATIONALID=", createRq.getNationalId()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#BIRTHTOWN=", createRq.getBirthTown()));
			rqParam.append(formReq("PT_PFN_PersonalDetails#CIVILSTATUS=", createRq.getCivilStatus()));			
			rqParam.append(formReq("PT_PFN_PersonalDetails#RESIDENTCOUNTRY=", LocaleHelp.getIso3CountryFromIso2Country(createRq.getResidentCountry())));
			//rqParam.append(formReq("PT_PFN_PersonalDetails#COUNTRYBIRTH=", LocaleHelp.getIso3CountryFromIso2Country(createRq.getBirthCountry())));
			rqParam.append(formReq("PT_PFN_PersonalDetails#RESIDENTSTATUS=", createRq.getResidentStatus()));
			
			rqParam.append("{");
			rqParam.append(formReq("PT_PFN_PartyContactDetails#CONTACTMETHOD=", createRq.getContactMethod()));
			rqParam.append(formReq("PT_PFN_PartyContactDetails#CONTACTTYPE=", createRq.getContactType()));
			rqParam.append(formReq("PT_PFN_PartyContactDetails#CONTACTVALUE=", createRq.getContactValue()));
		//	rqParam.append(formReq("PT_PFN_PartyContactDetails#FROMDATE=", createRq.getFromDate(), true));
			rqParam.append(formReq("PT_PFN_PartyContactDetails#ISDCODE=", createRq.getIsdCode()));
			rqParam.append("}");

			rqParam.append("{");
			rqParam.append(formReq("PT_PFN_AddressLink#ADDRESSTYPE=", createRq.getAddressType()));
			rqParam.append(formReq("PT_PFN_Address#ADDRESSLINE1=", createRq.getAddressLine1()));
			rqParam.append(formReq("PT_PFN_Address#ADDRESSLINE2=", createRq.getAddressLine2()));
			rqParam.append(formReq("PT_PFN_Address#ADDRESSLINE3=", createRq.getAddressLine3()));
			rqParam.append(formReq("PT_PFN_Address#ADDRESSLINE4=", createRq.getAddressLine4()));
			rqParam.append(formReq("PT_PFN_Address#ADDRESSLINE5=", createRq.getAddressLine5()));
			rqParam.append(formReq("PT_PFN_Address#TOWNORCITY=", createRq.getTownorCity()));
			rqParam.append(formReq("PT_PFN_Address#POSTALCODE=", createRq.getPostalCode()));
			rqParam.append(formReq("PT_PFN_Address#COUNTRYCODE=", LocaleHelp.getIso3CountryFromIso2Country(createRq.getCountryCode())));
	//		rqParam.append(formReq("PT_PFN_AddressLink#FROMDATE=", createRq.getAddressFromDate(), true));
			rqParam.append(formReq("PT_PFN_AddressLink#ISDEAFULTADDRESS=", createRq.getIsDefaultAddress()));
			rqParam.append("}");
			
			rqParam.append("{");
			rqParam.append(formReq("PT_PFN_PartyDocumentData#DOCCATEGORY=", createRq.getDocCategory()));
			rqParam.append(formReq("PT_PFN_PartyDocumentData#DOCTYPE=", createRq.getDocType()));
			rqParam.append(formReq("PT_PFN_PartyDocumentData#DOCREF=", createRq.getDocRef()));
	//		rqParam.append(formReq("PT_PFN_PartyDocumentData#VALIDFROMDT=", createRq.getValidFromDate(), true));
	//		rqParam.append(formReq("PT_PFN_PartyDocumentData#VALIDTODT=", createRq.getValidToDate(), true));
			rqParam.append(formReq("PT_PFN_PartyDocumentData#ISSUEAUTHORITY=", createRq.getIssueAuthority()));
			rqParam.append(formReq("PT_PFN_PartyDocumentData#KYCEXPIRYDT=", createRq.getKycExpDate(), true));
			rqParam.append(formReq("PT_PFN_PartyDocumentData#DOCVERIFIED=", createRq.getDocVerified()));
			rqParam.append(formReq("PT_PFN_PartyDocumentData#COUNTRYOFISSUE=", LocaleHelp.getIso3CountryFromIso2Country(createRq.getIssueCountry())));
			rqParam.append("}");
			
			rqParam.append("{");
			rqParam.append(formReq("PT_PFN_PartyLineOfBusiness#LINEOFBUSINESS=", "DIGICHANNELS"));
			rqParam.append("}");
			rqParam.append("{");
			rqParam.append(formReq("PT_PFN_PartyLineOfBusiness#LINEOFBUSINESS=", "COREBANKING"));
			rqParam.append("}");
			rqParam.append("{");
			rqParam.append(formReq("PT_PFN_PartyTaxDetails#TAXIDNUMBER=", createRq.getTaxId()));
			rqParam.append("}");
			
			if(createRq.getForeignCountryTax()!=null && createRq.getForeignCountryTax()!=""){
				rqParam.append("{");
				rqParam.append(formReq("PT_PFN_FatcaDetails#FOREIGNCOUNTRYOFTAX=", LocaleHelp.getIso3CountryFromIso2Country(createRq.getForeignCountryTax())));
				rqParam.append(formReq("PT_PFN_FatcaDetails#TAXCLASSIFICATION=", createRq.getTaxClassification()));
				rqParam.append(formReq("PT_PFN_FatcaDetails#TAXSUBCLASSIFICATION=", createRq.getTaxSubClassification()));
				rqParam.append(formReq("PT_PFN_FatcaDetails#REPORTINGTYPE=", createRq.getReportingType()));
				rqParam.append(formReq("PT_PFN_FatcaDetails#REPORTINGSTATUS=", createRq.getReportingStatus()));
				rqParam.append(formReq("PT_PFN_FatcaDetails#ISREPORTINGREQUIRED=", createRq.getIsReportingReq()));
				rqParam.append(formReq("PT_PFN_FatcaDetails#TIN=", createRq.getTaxClassification()));
				rqParam.append("}");
			}
			
			String controlParam = "DEDUP_REQD=N;TXN_COMMIT_LEVEL=A;GEN_CODE_VALDN_REQ=N;PARTY_ACTION=C";
			
			rqPayload.setRqParam(rqParam.toString());
			rqPayload.setControlParam(controlParam);
			ptyParams.put("requestPayload", rqPayload);
			logger.info("--------Creating the Customer---------- ");
			logger.info("\n\nPayload for party creation : \n" + rqParam);
			logger.info("\n\nControl param for party creation : \n" + controlParam);
			BankFusionThreadLocal.setChannel("IBI"); 			
			BankFusionThreadLocal.setMFId("CB_PTY_MaintainPartyWS_SRV");    
			FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("CB_PTY_MaintainPartyWS_SRV");
			HashMap<String, Object> outputMap = invoker.invokeMicroflow(ptyParams, false);

			RsPayload rs = (RsPayload) outputMap.get("responsePayload");
			String res = rs.getRsParam();			
			//Sample res UNIQUE_ID=first123;PARTYID=PTYJON3;STATUS=Success
			String customerId = getData("PARTYID", res);
			String customerStatus = getData("STATUS", res);
			
			logger.info("\n\nResponse from Create Party API : \n" + getXML(rs, "bf.com.misys.party.ws.RsPayload"));
			
			if (customerStatus != null) {
				if (customerStatus.equalsIgnoreCase("Success")) {
					isCustomerCreated = true;
					logger.info("Customer created successfully with customer ID is " + customerId + "Status == " + customerStatus);
				} else {
					logger.info("Customer not created !!! Customer Status is " + customerStatus);
				}
			} else {
				logger.info("Customer Status is Null !!!" + customerStatus);
			}

			// -------------------Creating Account----------------------
			// IFF Customer is created

			if (isCustomerCreated) {
		        JsonObject accCreationReq = new JsonObject();
		        accCreationReq.addProperty("msgId", createRq.getMsgId());
		        accCreationReq.addProperty("msgType", "CUSTOMER_CHECK_ACCOUNT_OPEN");
		        accCreationReq.addProperty("customerId", customerId);
		        accCreationReq.addProperty("subProdCur", subProdCur);
		        accCreationReq.addProperty("accBranchCode", createRq.getAccBranchcode());
		        accCreationReq.addProperty("subProd", subProd);
		        postToQueue(accCreationReq.toString(), "QM_Interface_Receive");
			} 

		} catch (BankFusionException e) {
			logger.info(" Error during Open Party and Create account");
			e.printStackTrace();
		} catch (Exception e) {
			logger.info(" Error during Open Party and Create account");
			e.printStackTrace();
		}

		if(!isCustomerCreated){
	        JsonObject errRes = new JsonObject();
	        errRes.addProperty("status", "E");
	        errRes.addProperty("msgId", createRq.getMsgId());
	        errRes.addProperty("msgType", "PARTY_ONBOARD_ACCOUNT_OPEN_RES");
	        errRes.addProperty("origCtxtId", "IBI");
	        postToQueue(errRes.toString(), "QM_BFDC_UB_Response");
	        logger.warning("CorrelationId: "+BankFusionThreadLocal.getCorrelationID());
		}
		return createRs;
	}

	  private void postToQueue(String message, String queueEndpoint) {
		    logger.info("message sent from Essence is \n" + message);
		    logger.info("---- Posting the message in the following queue " + queueEndpoint);
		    MessageProducerUtil.sendMessage(message, queueEndpoint);
	  }

	private String getXML(Object obj, String objType) {
		@SuppressWarnings("deprecation")
		ComplexTypeConvertor converter = new ComplexTypeConvertor(this
				.getClass().getClassLoader());
		return converter.getXmlFromJava(objType, obj);
	}
	
	private String getData(String key,String input){
    	String valueString=null;
    	String[] resultSet = input.split(";");
    	for(int i=0;i<resultSet.length;i++){
    		if(resultSet[i].contains(key)){
    			valueString = resultSet[i].split("=")[1].trim();
    		}
    	}
    	return valueString;
    }

	private StringBuilder formReq(String key,String value){
		return formReq(key, value, false);
	}

	private StringBuilder formReq(String key,String value,boolean dateField){
		StringBuilder msgString = new StringBuilder();
		if(value!=null || value !="") {
			msgString.append(key).append(value);
			if(dateField){
				msgString.append(PARTY_DATE_FORMAT_SEPARTOR).append("yyyy-MM-dd");
			}
			msgString.append(PARTY_COLUMN_SEPARATOR);	
		}
		return msgString;
	}
}
 