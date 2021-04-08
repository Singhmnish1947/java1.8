package com.misys.ub.cc.standingOrder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.msgs.v1r0.CreateStandingOrderRq;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.fbe.compliance.IKYCAckResponseHandler;
import com.misys.fbe.compliance.types.FircoAckResponseType;

public class SOKYCAckResponseHandler implements IKYCAckResponseHandler {

	private static final transient Log LOGGER = LogFactory
			.getLog(SOKYCAckResponseHandler.class.getName());

	
	private static String reasonCode = "40421574";
	
	@Override
	public void handleResponse(String msgId, String msgType,
			FircoAckResponseType fircoAckResponse) {
		
		String dataStr = KYCSOCreationHandler.readDataFromTable(msgId);
		JsonParser parser = new JsonParser();
		JsonObject data = (JsonObject) parser.parse(dataStr);
		JsonElement createSOJsonObj = data.get("CreateSORq");
		String messageId = data.get("messageId").getAsString();
		String transactionalItem = data.get("transactionalItem").getAsString();
		String channelId = data.get("channelId").getAsString();
		Gson gson = new Gson();
		CreateStandingOrderRq createSORq = null;
		createSORq = gson.fromJson(createSOJsonObj.getAsString(), CreateStandingOrderRq.class);
		KYCSOCreationHandler kycsoCreationHandler = new KYCSOCreationHandler();
		if(createSORq != null) {
			if(fircoAckResponse == FircoAckResponseType.CONFIRMED_HIT){
				LOGGER.warn("Got a hit in the ACk response");
				kycsoCreationHandler.setResponse(messageId, "", reasonCode, transactionalItem, channelId);
	
			} else if(fircoAckResponse == FircoAckResponseType.NO_HIT){
				kycsoCreationHandler.createStandingOrder(createSORq, messageId, transactionalItem, channelId);
			}
		}
	}
	
}
