package com.misys.ub.fatoms;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Exchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class Pain002DCFileHandler {
	
	public void sendNotificationToDC(Exchange exchange) throws IOException {
		File file = exchange.getIn().getBody(File.class);
		String fileName = file.getName();
		String[] fileNameTokens = fileName.split("_");
		StringBuffer inputFileName = new StringBuffer();
		inputFileName = inputFileName.append(fileNameTokens[0]).append("_").append(fileNameTokens[1]).append("_").append(fileNameTokens[2]).append("_").append(fileNameTokens[3]).append(".xml");
		String channel = fileNameTokens[0];
		if(channel.equals("DC")) {
			JsonObject responseMessageData = new JsonObject();
			responseMessageData.addProperty("msgType", "BULK_PAYMENT_NOTIFICATION");
			responseMessageData.addProperty("inputFileName", inputFileName.toString());
			responseMessageData.addProperty("transactionReference", fileNameTokens[1]);
			responseMessageData.addProperty("status", "PROCESS_SUCCESSFULLY");
			responseMessageData.addProperty("pain002FileName", fileName);		
			MessageProducerUtil.sendMessage(responseMessageData.toString(),"QM_BFDC_UB_DC_BulkPymtNtfctn");
		}
	}

}
