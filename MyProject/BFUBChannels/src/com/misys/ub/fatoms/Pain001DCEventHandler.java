package com.misys.ub.fatoms;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.misys.bankfusion.paymentmessaging.dd.helper.ErrorVO;
import com.misys.bankfusion.subsystem.messaging.MessageSenderUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.trapedza.bankfusion.utils.BankFusionMessages;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_Pain001DCEventHandler;


public class Pain001DCEventHandler extends AbstractUB_CMN_Pain001DCEventHandler {
	
	private static String SUCCESS_STATUS = "ACSP";
	private static String FAILED_STATUS = "RJCT";
	

	public Pain001DCEventHandler(BankFusionEnvironment env) {
		super(env);
	}

	public Pain001DCEventHandler() {
		super();
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		String fileName = getF_IN_fileName();
		List<ErrorVO> failureReasonCodes = new ArrayList<ErrorVO>();
		if(!(getF_IN_failureReason() instanceof String)) {
			failureReasonCodes = (ArrayList<ErrorVO>) getF_IN_failureReason();
		}
		String fileStatus = getF_IN_fileStatus();
		String[] fileNameTokens = fileName.split("_");
		String channel = fileNameTokens[0];
		if(channel.equals("DC")) {
			JsonObject responseMessageData = new JsonObject();
			responseMessageData.addProperty("msgType", "BULK_PAYMENT_NOTIFICATION");
			responseMessageData.addProperty("inputFileName", fileName);
			responseMessageData.addProperty("transactionReference", fileNameTokens[1]);
			
			if(fileStatus.equals(SUCCESS_STATUS)) {
				responseMessageData.addProperty("status", "PROCESSING");
			} else {
				responseMessageData.addProperty("status", "REJECTED");
				
				JsonArray reason = new JsonArray();
				for(ErrorVO errorVO : failureReasonCodes) {
					JsonObject reasonElement = new JsonObject();
					reasonElement.addProperty("eventCode", errorVO.getErrCode());
					reasonElement.addProperty("message", errorVO.getErrorDescription());
					reasonElement.addProperty("param1", errorVO.getErrParam1());
					reasonElement.addProperty("param2", errorVO.getErrParam2());
					reasonElement.addProperty("param3", errorVO.getErrParam3());
					reason.add(reasonElement);
				}
				responseMessageData.add("reasons", reason);
			}
			MessageProducerUtil.sendMessage(responseMessageData.toString(),"QM_BFDC_UB_DC_BulkPymtNtfctn");
		}
	}
	

}
