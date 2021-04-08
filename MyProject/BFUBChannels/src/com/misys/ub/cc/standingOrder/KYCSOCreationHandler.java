package com.misys.ub.cc.standingOrder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.ComplexTypeConvertorFactory;
import com.misys.bankfusion.common.IComplexTypeConvertor;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.microflow.IMFManager;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbe.compliance.IKYCFinalResponseHandler;
import com.misys.fbe.compliance.KYCDataCache;
import com.misys.fbe.compliance.types.FircoFinalResponseType;
import com.trapedza.bankfusion.fatoms.UB_IND_CreateStandingOrderFatom;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.IMicroflowHelper;
import com.trapedza.bankfusion.servercommon.microflow.MicroflowHelper;

import bf.com.misys.cbs.msgs.v1r0.CreateStandingOrderRq;
import bf.com.misys.cbs.msgs.v1r0.ReadProductSummaryDtlsRs;
import bf.com.misys.cbs.msgs.v1r0.TransferResponse;
import bf.com.misys.cbs.services.ValidateAccountRs;
import bf.com.misys.cbs.types.InstructionUpdate;
import bf.com.misys.cbs.types.InstructionUpdateItem;
import bf.com.misys.cbs.types.TransactionEvent;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

public class KYCSOCreationHandler implements IKYCFinalResponseHandler {

	private static final transient Log LOGGER = LogFactory
			.getLog(KYCSOCreationHandler.class.getName());
	private static String reasonCode = "40421574";
	private String errorMsgParameter = "";
	@Override
	public void handleResponse(String msgId,String msgType,
			FircoFinalResponseType fircoFinalResponse) {
		
		String dataStr = KYCSOCreationHandler.readDataFromTable(msgId);
		JsonParser parser = new JsonParser();
		JsonObject data = (JsonObject) parser.parse(dataStr);
		JsonElement createSOJsonObj = data.get("CreateSORq");
		String messageId = data.get("messageId").getAsString();
		String transactionalItem = data.get("transactionalItem").getAsString();
		String channelId = data.get("channelId").getAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		Gson gson = new Gson();
		CreateStandingOrderRq createSORq = null;
		createSORq = gson.fromJson(createSOJsonObj.getAsString(), CreateStandingOrderRq.class);
		
		if(createSORq != null) {
			if(fircoFinalResponse == FircoFinalResponseType.TRUE_HIT){
				setResponse(messageId, "", reasonCode, transactionalItem, channelId);
				LOGGER.warn(" =+= Got a True_hit as final response");
			}
			else if(fircoFinalResponse == FircoFinalResponseType.FALSE_HIT){
				createStandingOrder(createSORq, messageId, transactionalItem, channelId);
			}
		}
	}

	
	public static String readDataFromTable(String msgId) {
		KYCDataCache kycData = new KYCDataCache();
		String dataStr = kycData.getData(msgId);
		return dataStr;
	}
	
	public void createStandingOrder(CreateStandingOrderRq createSORq, String messageId, String transactionalItem, String channelId) {
		BankFusionThreadLocal.setChannel(channelId);
		UB_IND_CreateStandingOrderFatom createStandingOrderFatom = new UB_IND_CreateStandingOrderFatom(BankFusionThreadLocal.getBankFusionEnvironment());
		String soreference = createStandingOrderFatom.createStandingOrder(createSORq);
		ValidateAccountRs mainAcc = createStandingOrderFatom.validateMainAccount(createSORq.getInputCreateStandingOrderRq()
                .getSoKeyDtls().getSoMainAcc().getStandardAccountId());
        ReadProductSummaryDtlsRs productResponse = createStandingOrderFatom.readProductSummaryDetails(mainAcc.getAccountInfo().getAcctBasicDetails().getProductId());
        createStandingOrderFatom.calculateAndPostCharges(createSORq.getInputCreateStandingOrderRq(), productResponse, "40200471");
        createStandingOrderFatom.insertSOMessage(messageId, createSORq.getInputCreateStandingOrderRq());
        setResponse(messageId, soreference, "S", transactionalItem, channelId);
	}
	
	public void setResponse(String messageId, String soReference, String resp, String transactionalItem, String channelId) {
		UB_IND_CreateStandingOrderFatom createStandingOrderFatom = new UB_IND_CreateStandingOrderFatom(BankFusionThreadLocal.getBankFusionEnvironment());
        TransferResponse transferResponse = new TransferResponse();
        TransactionEvent transactionEvent = new TransactionEvent();
        InstructionUpdate instructionUpdate = new InstructionUpdate();
        transferResponse.setReqPayload("REQUEST_PAYLOAD");
        InstructionUpdateItem[] instructionUpdateItems = new InstructionUpdateItem[1];
        instructionUpdateItems[0] = new InstructionUpdateItem();
        instructionUpdate.setInstructionUpdateItem(instructionUpdateItems);
        transferResponse.setInstructionStatusUpdateNotification(instructionUpdate);
        RsHeader rsHeader = new RsHeader();
        MessageStatus messageStatus = new MessageStatus();
        rsHeader.setMessageType("");
        rsHeader.setVersion("");
        rsHeader.setStatus(messageStatus);
        rsHeader.setOrigCtxtId(channelId);
        transferResponse.setRsHeader(rsHeader);	
        InstructionUpdateItem[] insItem = transferResponse.getInstructionStatusUpdateNotification().getInstructionUpdateItem();
        if (resp.equalsIgnoreCase("S")) {
            for (int i = 0; i < 1; i++) {
                insItem[i].setTransactionalItem(transactionalItem);
                insItem[i].setSoReference(soReference);
                insItem[i].setNewStatus("PROCESS_SUCCESSFULLY");
            }
            createStandingOrderFatom.updateMessageheader(messageId, "P", transferResponse);
        }
        else if (resp.equalsIgnoreCase("DBTBLOCKED")) {
            for (int i = 0; i < 1; i++) {
                insItem[i].setTransactionalItem(transactionalItem);
                insItem[i].setNewStatus("REJECTED");
                transactionEvent.setReasonCode("20020014");
                String eventMsg = BankFusionMessages.getFormattedMessage(20020014, new String[] {});
                transactionEvent.setFormattedMessage(eventMsg);
                transactionEvent.setDefaultMessage(eventMsg);
                insItem[i].setTransactionEvent(transactionEvent);
            }
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            createStandingOrderFatom.updateMessageheader(messageId, "F", transferResponse);
        }
        else {
        	String eventMsg="";  
            for (int i = 0; i < 1; i++) {
                insItem[i].setTransactionalItem(transactionalItem);
                insItem[i].setNewStatus("REJECTED");
                transactionEvent.setReasonCode(resp);
                if(errorMsgParameter == null ||CommonConstants.EMPTY_STRING.equals(errorMsgParameter))
                {
                   eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(resp), new String[] {});
                }else{
                   eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(resp), new String[] {errorMsgParameter});
                }
                transactionEvent.setFormattedMessage(eventMsg);
                transactionEvent.setDefaultMessage(eventMsg);
                insItem[i].setTransactionEvent(transactionEvent);
            }
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            createStandingOrderFatom.updateMessageheader(messageId, "F", transferResponse);
        }
        
       	MessageProducerUtil.sendMessage(convertObjectToXMLString(BankFusionThreadLocal.getBankFusionEnvironment(), transferResponse), "QM_BFDC_UB_Response");
       
    }
	
	public static String convertObjectToXMLString(BankFusionEnvironment env, Object obj) {
        IMicroflowHelper microflowHelper = new MicroflowHelper(env);
        IMFManager mfManager = microflowHelper.getMFManager();
        ClassLoader cl = mfManager.getDynamicClassLoader();
        IComplexTypeConvertor complexTypeConvertor = ComplexTypeConvertorFactory.getComplexTypeConvertor(cl);
        return complexTypeConvertor.getXmlFromJava(obj.getClass().getName(), obj);
    }
}
