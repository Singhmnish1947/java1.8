/* ********************************************************************************
 *  Copyright(c)2011  Misys Solution for Banking Ltd. 
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Solution for Banking Ltd.
 *  Use is subject to license terms.
 */
/**
 * @author Rajesh.Ravi
 * @date May 23, 2011
 * @project Universal Banking
 * @Description: Class file for the fatom to read the SWIFT messages from 
 * meridian database using a web service deployed at the meridian server.
 */
package com.misys.ub.swift.meridianenquiry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub;
import com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.LookupMessage;
import com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.LookupMessageE;
import com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.LookupMessageResponse;
import com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.LookupMessageResponseE;
import com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.MessageDto;
import com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.StringArray;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MeridianMessageEnquiry;

public class MeridianMessageEnquiry extends AbstractUB_SWT_MeridianMessageEnquiry{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6985472469572441782L;
	private final String SELECT = "SELECT";
	private final String MESSAGENUMBER = "MessageNumber";
	private final String CREATIONDATETIME = "CreationDateTime";
	private final String MESSAGETYPE = "MessageType";
	private final String MESSAGESTATUS = "MessageStatus";
	private final String NACKMESSAGE = "NACKMessage";
	private final String TAG = "Tag";
	private final String TAGVALUE = "TagValue";
	private final String TAGNAME = "TagName";
	private final String SYSTEMARRIVALTIME = "SystemArrivalTime";
	private final String MERIDIANEXTERNALMESSAGETYPE = "ExternalMessageType";
	private final String UBMM_WORKFLOW_ADDRESS_KEY = "UBMM_Workflow_address";
	private final String SENDERADDRESS = "SenderAddress";
	private final String RECEIVERADDRESS = "ReceiverAddress";
	private final String MERIDIANSENDERADDRESS = "SenderAddress";
	private final String MERIDIANDESTINATIONADDRESS = "DestinationAddress";
	private final String MERIDIANQUEUE = "Queue";
	
	private transient final static Log logger = LogFactory.getLog(MeridianMessageEnquiry.class.getName());

	private final String WEB_SERVICE_PROPERTIES_FILE_LOCATION = "conf/swift/SWT_MeridianWebService.properties";

	public MeridianMessageEnquiry() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MeridianMessageEnquiry(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MeridianMessageEnquiry#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {
		
		//Send request
		LookupMessageResponse responseMessages = getResponseMessages(getF_IN_TransactionNumber(), env);
		
		// Read response
		processResponseMessage(responseMessages);
	}

	/**Processes the response messages received from the web service and populates the output tags.
	 * @param responseMessages response message from the meridian web service.
	 */
	private void processResponseMessage(LookupMessageResponse responseMessages) {
		
		//Gets the reference of output tags.
		VectorTable swiftMessageDetails = getF_OUT_SwiftMessageDetails();
		VectorTable swiftMessages = getF_OUT_SwiftMessages();
		VectorTable swiftNACKDetails = getF_OUT_SwiftNACKMessages();
		if(responseMessages!=null){
			
			//Induced key to identify the messages uniquely.
			Integer messageNumber = CommonConstants.INTEGER_ZERO;
			MessageDto[] messageDto=responseMessages.get_return();
			if(messageDto!=null){
			for(MessageDto responseMessage:messageDto){
				
				String meridianMessage = responseMessage.getMeridianMessage();
				
				//Populating SWIFT messages vector.
				swiftMessages.addAll(new VectorTable(getSWIFTMessage(meridianMessage, messageNumber)));
				
				//Populating SWIFT message details for each message in SWIFT message details vector.
				swiftMessageDetails.addAll(getSwiftTagDetailsFromMessage(responseMessage.getNetworkDependantFormat(),messageNumber));
				
				//Populating SWIFT failure message details for each message(if exists) in SWIFT NACK message details vector.
				swiftNACKDetails.addAll(getSWIFTNACKMessages(responseMessage.getErrorDescriptions(),meridianMessage,messageNumber));
				
				//Incrementing Message number.
				messageNumber+=1;
			}
			}
		}
		
		//Populating Number of Rows for each vector.
		setF_OUT_SwiftMessageDetails_NOOFROWS(swiftMessageDetails.size());
		setF_OUT_SwiftMessages_NOOFROWS(swiftMessages.size());
		setF_OUT_SwiftNACKMessages_NOOFROWS(swiftNACKDetails.size());
	}

	/**Gets the String between the distinct start and end strings present in the source string.
	 * Only the first possible occurrence. 
	 * @param sourceString
	 * @param startString
	 * @param endString
	 * @return	String between two distinct strings
	 */
	private String getStringBetweenStrings(String sourceString,String startString,String endString){
		return sourceString.substring(sourceString.indexOf(startString)+startString.length(), sourceString.indexOf(endString));

	}

	/**Gets the first possible value of given XMLtag within the  XML string.
	 * @param xml	XML string
	 * @param xmlTag	XML tag to be extracted.
	 * @return	String value of the extracted XML.
	 * Returns Empty string if the XML tag exist without any value(i.e. <xmlTag/>).
	 */
	private String getXMLTagValue(String xml,String xmlTag){
		if(!xml.contains(xmlTag) || xml.contains("<"+xmlTag+"/>"))
			return CommonConstants.EMPTY_STRING;

		return getStringBetweenStrings(xml,"<"+xmlTag+">","</"+xmlTag+">");
	}

	/**Prepares SWIFT message for the response message.
	 * @param meridianMessage
	 * @param messageNumber
	 * @return	Hash Map contains SWIFT message information.
	 */
	private HashMap getSWIFTMessage(String meridianMessage,Integer messageNumber){
		HashMap<String,Object> swiftMessage = new HashMap<String,Object>();
		
		//Marks the first record as selected.
		if(messageNumber.equals(CommonConstants.INTEGER_ZERO)){
			swiftMessage.put(SELECT, Boolean.TRUE);
		}else{
			swiftMessage.put(SELECT, Boolean.FALSE);
		}
		swiftMessage.put(MESSAGENUMBER, messageNumber);
		swiftMessage.put(CREATIONDATETIME,getXMLTagValue(meridianMessage, SYSTEMARRIVALTIME));
		swiftMessage.put(MESSAGETYPE,getXMLTagValue(meridianMessage, MERIDIANEXTERNALMESSAGETYPE));
		swiftMessage.put(SENDERADDRESS,getXMLTagValue(meridianMessage, MERIDIANSENDERADDRESS));
		swiftMessage.put(RECEIVERADDRESS,getXMLTagValue(meridianMessage, MERIDIANDESTINATIONADDRESS));
		swiftMessage.put(MESSAGESTATUS, getXMLTagValue(meridianMessage, MERIDIANQUEUE));
		return swiftMessage;
	}
	
	/**Prepares SWIFT message details for each message meridian response.
	 * @param networkDependentMessage
	 * @param messageNumber
	 * @return	Vector containing SWIFT message details. 
	 */
	private VectorTable getSwiftTagDetailsFromMessage(String networkDependentMessage,Integer messageNumber){
		String tagValue[];
		VectorTable swiftMessageDetails = new VectorTable();
		HashMap<String,Object> swiftDetails = new HashMap<String,Object>();
		
		//The start and end string patterns have been done by observing the pattern in the received message.
		String tagValueArray[] = getStringBetweenStrings(networkDependentMessage, "{4:", "-}").split("\n:");
		
		//Looping starts from index 1 since it was observed the first value was always empty.
		for(int i=1; i<tagValueArray.length ; i++)
		{
			if(!tagValueArray[i].equals(CommonConstants.EMPTY_STRING)){
				tagValue = tagValueArray[i].split(":");
				int len = tagValue.length;
				if(len > 1){
					String[] subFileds = tagValue[1].split("\n");
					for(int j=0;j< subFileds.length;j++){
						if(j==0){
							swiftDetails.put(TAG,tagValue[0]);
						}else{
							swiftDetails.put(TAG,CommonConstants.EMPTY_STRING);
						}
						swiftDetails.put(TAGVALUE,subFileds[j]);
						swiftDetails.put(MESSAGENUMBER,messageNumber);
						swiftDetails.put(TAGNAME,CommonConstants.EMPTY_STRING);
						swiftMessageDetails.addAll(new VectorTable(swiftDetails));
					}
				}else{
					swiftDetails.put(TAG,tagValue[0]);
					swiftDetails.put(TAGVALUE,CommonConstants.EMPTY_STRING);
					swiftDetails.put(MESSAGENUMBER,messageNumber);
					swiftDetails.put(TAGNAME,CommonConstants.EMPTY_STRING);
					swiftMessageDetails.addAll(new VectorTable(swiftDetails));
				}

			}
		}
		return swiftMessageDetails;	
	}

	/**Prepare SWIFT failure message details for each message(if exists) in response message.
	 * @param stringArrays
	 * @param meridianMessage
	 * @param messageNumber
	 * @return	Vector containing SWIFT Failure message details.
	 */
	private VectorTable getSWIFTNACKMessages(StringArray[] stringArrays,String meridianMessage,Integer messageNumber){
		HashMap<String,String> errorDescriptionsMap = new HashMap<String, String>();
		HashMap<String,Object> swiftNACK = new HashMap<String,Object>();
		VectorTable swiftNACKDetails = new VectorTable();
		String errorKeyValueArray[];
		String errorKeyValue[];
		if(stringArrays!=null && null!=stringArrays[0].getItem()){
			
			errorDescriptionsMap.put(stringArrays[0].getItem()[0], stringArrays[0].getItem()[1]);
			
			/*for( String errorDescription :stringArrays[0].getItem()){
				errorDescriptionsMap.put(errorDescription.toString(), errorDescription.toString());
			}*/
			errorKeyValueArray = getXMLTagValue(meridianMessage, "ErrorDetails").split(",");
			for(String errorKeyValuePair:errorKeyValueArray){
				errorKeyValue = errorKeyValuePair.split(" ");
				swiftNACK.put(MESSAGENUMBER, messageNumber);
				swiftNACK.put(TAG, errorKeyValue[1].replace(":",""));
				swiftNACK.put(NACKMESSAGE,errorDescriptionsMap.get(errorKeyValue[0]));
				
				swiftNACKDetails.addAll(new VectorTable(swiftNACK));
			}
		}
		return swiftNACKDetails;
	}
	
	/**Gets the work flow address for meridian web service which is configured in a file in server configuration.
	 * @param env
	 * @return	work flow address for meridian web service
	 */
	private String getWorkFlowAddress(BankFusionEnvironment env){
		StringBuilder path=new StringBuilder(GetUBConfigLocation.getUBConfigLocation());	
		path.append(WEB_SERVICE_PROPERTIES_FILE_LOCATION);
		Properties prop=fetchProperties(path.toString());

		if(prop==null){
			handleError(CommonsEventCodes.E_FILE_NOT_FOUND_UB14,new Object[]{path.toString()},env);
		}
		return (prop != null ? prop.getProperty(UBMM_WORKFLOW_ADDRESS_KEY) : null);
	}
	
	/**Gets the Response messages for the given transaction reference by communicating with the meridian web service.
	 * @param transactionReference
	 * @param env
	 * @return	Response messages for the given transaction reference
	 */
	private LookupMessageResponse  getResponseMessages(String transactionReference,BankFusionEnvironment env){
		
		LookupMessageResponse responseMessages = new LookupMessageResponse();
		String UBMM_Workflow_address = getWorkFlowAddress(env);
		LookupMessageResponseE lookupMessageResponse;
		
		try {
            URL endpoint = new URL(UBMM_Workflow_address);
            LookupMessageE lookupMessage = new LookupMessageE();
            LookupMessage lookupMsg = new LookupMessage();
            lookupMsg.setArg0(transactionReference);
            lookupMessage.setLookupMessage(lookupMsg);
            MeridianMessageEnquiryWebServiceStub stub = new MeridianMessageEnquiryWebServiceStub(endpoint.toString());
            lookupMessageResponse = stub.lookupMessage(lookupMessage);
            responseMessages = lookupMessageResponse.getLookupMessageResponse();
		} catch (Exception e) {
			/**
			 * Raised when an exception is caught for accessing a web service which is not available. 
			 */
			handleError(ChannelsEventCodes.E_CONNECTION_MERIDIAN_SERVER_FAILED_UB,null,env);
		}
		return responseMessages;
	}
	
	/**Raises the error for the given error code and parameters.
	 * @param eventCode
	 * @param params
	 * @param env
	 */
	private void handleError(int eventCode,Object[] params,BankFusionEnvironment env){
		EventsHelper.handleEvent(eventCode, params,
				new HashMap(), env);
	}
	
	/**Reads the Properties from the given file path
	 * @param path	file path
	 * @return	Properties object with properties fetch from a properties file
	 */
	private Properties fetchProperties(String path){
		return loadProperties(getInputStreamResource(path.toString()));

	}
	
	/**Gives the InputStream object for the given file
	 * @param file
	 * @return
	 */
	private InputStream getInputStreamResource(String file) {
		InputStream is = null;

		try {
			is = new FileInputStream(file);

		}
		catch (FileNotFoundException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));

		}
		return is;
	}

	/**Loads properties  stored in the properties file
	 * @param inputStream
	 * @return
	 */
	private Properties loadProperties(InputStream inputStream) {

		Properties prop = null;
		if(inputStream==null){
			return null;
		}
		try{
			prop = new Properties();
			prop.load(inputStream);
		}
		catch (IOException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
		return prop;
	}
}