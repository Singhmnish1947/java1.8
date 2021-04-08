package com.misys.ub.dc.payment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.fbe.compliance.IKYCAckResponseHandler;
import com.misys.fbe.compliance.types.FircoAckResponseType;
import com.trapedza.bankfusion.fatoms.CustomerBlockerKYC;
import com.trapedza.bankfusion.fatoms.UB_IND_PaymentPostingFatom;

public class UB_IBI_KYCAckResponseHandler implements IKYCAckResponseHandler {

	private static final transient Log LOGGER = LogFactory
			.getLog(UB_IBI_KYCAckResponseHandler.class.getName());

	
	private static String successStatus = "PROCESS_SUCCESSFULLY";
	private static String rejectStatus = "REJECTED";
	private static String reasonCode = "40421005";
	private static String postingErrorCode = "40000127";
	private static String responseEndPoint = "QM_BFDC_UB_Response";
	
	private static String responseMsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"\n<cbsmsg:transferResponse xmlns:cbsmsg=\"http://www.misys.com/cbs/msgs/v1r0\">"
			+"\n	<reqPayload>REQUEST_PAYLOAD</reqPayload>"
			+"\n	<cbsmsg:rsHeader>"
			+"\n		<ns1:version xmlns:ns1=\"http://www.misys.com/cbs/types/header\"/>"
			+"\n		<ns2:messageType xmlns:ns2=\"http://www.misys.com/cbs/types/header\"/>"
			+"\n		<ns3:origCtxtId xmlns:ns3=\"http://www.misys.com/cbs/types/header\">CHANNEL_ID</ns3:origCtxtId>"
			+"\n		<ns4:status xmlns:ns4=\"http://www.misys.com/cbs/types/header\">"
			+"\n			<ns4:overallStatus/>"
			+"\n			<ns4:codes>"
			+"\n				<ns4:code/>"
			+"\n				<ns4:fieldName> </ns4:fieldName>"
			+"\n				<ns4:severity/>"
			+"\n				<ns4:description/>"
			+"\n				<ns4:parameters>"
			+"\n					<ns4:eventParameterValue/>"
			+"\n				</ns4:parameters>"
			+"\n			</ns4:codes>"
			+"\n			<ns4:subStatus/>"
			+"\n		</ns4:status>"
			+"\n	</cbsmsg:rsHeader>"
			+"\n	<cbsmsg:instructionStatusUpdateNotification>"
			+"\n		<ns5:instructionUpdateItem xmlns:ns5=\"http://www.misys.com/cbs/types\">"
			+"\n			<ns5:transactionalItem>TRANSACTIONAL_ITEM_REPLACE_STR</ns5:transactionalItem>"
			+"\n			<ns5:newStatus>MSG_STATUS_REPLACE_STR</ns5:newStatus>"
			+"\n			<ns5:notificationSequence>0</ns5:notificationSequence>"
			+"\n			<ns5:transactionEvent>"
			+"\n				<ns5:reasonCode>REASON_CODE_REPLACE_STR</ns5:reasonCode>"
			+"\n				<ns5:defaultMessage>REASON_MESSAGE_REPLACE_STR</ns5:defaultMessage>"
			+"\n				<ns5:formattedMessage>REASON_MESSAGE_REPLACE_STR</ns5:formattedMessage>"
			+"\n				<ns5:eventParameters/>"
			+"\n			</ns5:transactionEvent>"
			+"\n			<userExtension xmlns:java=\"http://java.sun.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"java:java.lang.String\"/>"
			+"\n			<hostExtension xmlns:java=\"http://java.sun.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"java:java.lang.String\"/>"
			+"\n		</ns5:instructionUpdateItem>"
			+"\n	</cbsmsg:instructionStatusUpdateNotification>"
			+"\n</cbsmsg:transferResponse>";
	@Override
	public void handleResponse(String msgId, String msgType,
			FircoAckResponseType fircoAckResponse) {
		
		String dataStr = UB_IBI_KYCPostingHandler.readDataFromTable(msgId);
		JsonParser parser = new JsonParser();
		JsonObject data = (JsonObject) parser.parse(dataStr);
		JsonObject responseMessageData = data.getAsJsonObject("responseMessageData");
//		String customerId = responseMessageData.get("customerId").getAsString();
		String transactionalItem = responseMessageData.get("transactionalItem").getAsString();
		String channelId = responseMessageData.get("channelId").getAsString();
		
		if(fircoAckResponse == FircoAckResponseType.CONFIRMED_HIT){
			LOGGER.warn("Got a hit in the ACk response");
			replyWithErrorMsg(transactionalItem, channelId);
//			blockCustomer(customerId);
		} else if(fircoAckResponse == FircoAckResponseType.NO_HIT){
			if(UB_IBI_KYCPostingHandler.doPostings(msgId, dataStr)){
				UB_IBI_KYCAckResponseHandler.replyWithSuccessMsg(transactionalItem, channelId);				
			} 
			else {
				UB_IBI_KYCAckResponseHandler.replyWithPostingErrorMsg(transactionalItem, channelId);
			}
		}
	}

	public static void blockCustomer(String customerId) {
      CustomerBlockerKYC custBlocker = new CustomerBlockerKYC();
      if (custBlocker.blockCustomer(customerId, UB_IND_PaymentPostingFatom.MODULE_CONFIG_CATEGORY_FOR_KYC, UB_IND_PaymentPostingFatom.MODULE_CONFIG_NAME_FOR_CUSTOMER_BLOCKING)) {
    	  LOGGER.warn(customerId + " Blocked successfully");
      }
	}
	
	public static void replyWithSuccessMsg(String transactionalItem, String channelId) {
		String replyMsg = responseMsg.replace("MSG_STATUS_REPLACE_STR", successStatus);
		replyMsg = replyMsg.replace("TRANSACTIONAL_ITEM_REPLACE_STR", transactionalItem);
		replyMsg = replyMsg.replace("REASON_CODE_REPLACE_STR", "");
		replyMsg = replyMsg.replace("REASON_MESSAGE_REPLACE_STR", "");
		replyMsg = replyMsg.replace("CHANNEL_ID", channelId);
		postToQueue(replyMsg,responseEndPoint);
	}


	@SuppressWarnings("deprecation")
	public static void replyWithErrorMsg(String transactionalItem, String channelId) {
		String replyMsg = responseMsg.replace("MSG_STATUS_REPLACE_STR", rejectStatus);		
		replyMsg = replyMsg.replace("TRANSACTIONAL_ITEM_REPLACE_STR", transactionalItem);
		replyMsg = replyMsg.replace("REASON_CODE_REPLACE_STR", reasonCode);
		replyMsg = replyMsg.replace("REASON_MESSAGE_REPLACE_STR", BankFusionMessages.getFormattedMessage(Integer.parseInt(reasonCode), new String[] {}));
		replyMsg = replyMsg.replace("CHANNEL_ID", channelId);
		postToQueue(replyMsg,responseEndPoint);
	}

	public static void postToQueue(String replyMsg, String queueEndpointName) {
        MessageProducerUtil.sendMessage(replyMsg, queueEndpointName);		
	}

	public static void replyWithPostingErrorMsg(String transactionalItem, String channelId) {
		String replyMsg = responseMsg.replace("MSG_STATUS_REPLACE_STR", rejectStatus);		
		replyMsg = replyMsg.replace("TRANSACTIONAL_ITEM_REPLACE_STR", transactionalItem);
		replyMsg = replyMsg.replace("REASON_CODE_REPLACE_STR", postingErrorCode);
		replyMsg = replyMsg.replace("REASON_MESSAGE_REPLACE_STR", BankFusionMessages.getFormattedMessage(Integer.parseInt(postingErrorCode), new String[] {}));
		replyMsg = replyMsg.replace("CHANNEL_ID", channelId);
		postToQueue(replyMsg,responseEndPoint);
	}
	
}
