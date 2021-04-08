package com.misys.ub.dc.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.dc.common.InstructionStatusUpdateNotification;
import com.misys.ub.dc.common.QueueName;
import com.misys.ub.dc.handler.helper.PartyCreateHandlerHelper;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.party.ws.RqPayload;
import bf.com.misys.party.ws.RsPayload;

public class PartyCreateHandler {
    private final static Logger logger = Logger.getLogger(PartyCreateHandler.class.getName());
    private final static String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
    private String partyId;
    private boolean isPartyCreated;
    private JsonObject createPartyAndAccountRq;
    private boolean isKYCEnabled;
    private String errorCode;
    private String errorMessage;
    private Object[] arguments;
    private RqPayload rqPayload;
    private RsPayload rsPayload;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Object[] getArguments() {
        return arguments;
    }

    
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public PartyCreateHandler(JsonObject createPartyAndAccountRq) {
        this.createPartyAndAccountRq = createPartyAndAccountRq;
        this.rqPayload = new RqPayload();
        setKYCFlag();
        setRqPayload();
    }

    public boolean isPartyCreated() {
        return this.isPartyCreated;
    }

    public String getPartyId() {
        return this.partyId;
    }

    public boolean isKYCenabled() {
        return this.isKYCEnabled;
    }

    private void setKYCFlag() {
        // Code to set KYC flag once procedure is determined
        boolean isSysKYCEnabled = getModuleConfigValue("SYSTEM_KYC_CHECK", "KYC");
        boolean isPtyCreateKYCEnabled = getModuleConfigValue("CUSTOMERCREATE_KYC_CHECK", "KYC");

        if (isPtyCreateKYCEnabled && isSysKYCEnabled)
            this.isKYCEnabled = true;
        else this.isKYCEnabled = false;
    }

    private void setRqPayload() {
        StringBuilder rqParam = formRqParam();
        if (rqParam == null) {
            this.rqPayload = null;
            return;
        }

        String controlParam = formControlParam();
        this.rqPayload.setRqParam(rqParam.toString());
        logger.info("\n\nPayload for party creation : \n" + rqParam);
        this.rqPayload.setControlParam(controlParam);
        logger.info("\n\nControl param for party creation : \n" + controlParam);

    }

    private String formControlParam() {
        String controlParam = "";

        if (this.isKYCEnabled) {
            controlParam = "DEDUP_REQD=N;TXN_COMMIT_LEVEL=A;GEN_CODE_VALDN_REQ=N;PARTY_ACTION=C;HOST_UPDATION_REQD=N";
        }
        else {
            controlParam = "DEDUP_REQD=N;TXN_COMMIT_LEVEL=A;GEN_CODE_VALDN_REQ=N;PARTY_ACTION=C";
        }
        return controlParam;
    }

    private StringBuilder formRqParam() {
        JsonArray rptBlockArray, docArray = null;
        JsonElement docElement = null;
        StringBuilder rqParam = new StringBuilder();

        JsonObject ptyFldsObj = this.createPartyAndAccountRq.getAsJsonObject(PartyCreateHandlerHelper.PARTY_FIELD_KEY);
        if (ptyFldsObj == null) {
            return null;
        }

        rptBlockArray = extractAndValidateArray(ptyFldsObj);
        if (rptBlockArray != null) {
            ptyFldsObj.remove(PartyCreateHandlerHelper.INPUT_REPETEABLE_BLOCK_IDENTIFIER);
        }

        docElement = this.createPartyAndAccountRq.get(PartyCreateHandlerHelper.INPUT_DOC_IDENTIFIER);
        if (docElement != null) {
            docArray = extractAndValidateArray(docElement.getAsJsonObject());
        }
        
        appendBlockToRqParam(ptyFldsObj, rqParam);
        appendRptBlocksToRqParam(rptBlockArray, rqParam);
        appenDocBlocksToRqParam(docArray, rqParam);

        return rqParam;
    }

    private void appendRptBlocksToRqParam(JsonArray rptBlockArray, StringBuilder rqParam) {
        if((rptBlockArray==null)||(rptBlockArray.size()==0)) {
            return;
        }
        for (JsonElement rptBlockElement : rptBlockArray) {
            JsonObject block = rptBlockElement.getAsJsonObject();
            rqParam.append(PartyCreateHandlerHelper.REPETEABLE_BLOCK_OPEN_SEPARATOR);
            appendBlockToRqParam(block, rqParam);
            rqParam.append(PartyCreateHandlerHelper.REPETEABLE_BLOCK_CLOSE_SEPARATOR);
        }
    }

    private JsonArray extractAndValidateArray(JsonObject rptBlckObj) {
        JsonArray rptBlockArray = null;

        rptBlockArray = rptBlckObj.getAsJsonArray(PartyCreateHandlerHelper.INPUT_REPETEABLE_BLOCK_IDENTIFIER);
        if (isArrayValid(rptBlockArray)) {
            return rptBlockArray;
        }
        else return null;
    }

    private void appendBlockToRqParam(JsonObject block, StringBuilder rqParam) {
        Iterator<Entry<String, JsonElement>> blockItr = block.entrySet().iterator();
        while (blockItr.hasNext()) {
            Entry<String, JsonElement> entry = blockItr.next();
            String key = entry.getKey().toString();
            JsonElement value = entry.getValue();
            rqParam.append(formReq(key, value).toString());
        }
    }

    private void appenDocBlocksToRqParam(JsonArray docBlockArray, StringBuilder rqParam) {
        if((docBlockArray==null)||(docBlockArray.size()==0)) {
            return;
        }
        for (JsonElement docElement : docBlockArray) {
            JsonObject docObj = docElement.getAsJsonObject();
            Iterator<Entry<String, JsonElement>> docItr = docObj.entrySet().iterator();
            rqParam.append(PartyCreateHandlerHelper.REPETEABLE_BLOCK_OPEN_SEPARATOR);
            while (docItr.hasNext()) {
                Entry<String, JsonElement> entry = docItr.next();
                String key = entry.getKey().toString();
                if (key.contains(PartyCreateHandlerHelper.PARTY_DOC_IDENTIFIER)) {
                    JsonElement value = entry.getValue();
                    rqParam.append(formReq(key, value));
                }
            }
            rqParam.append(PartyCreateHandlerHelper.REPETEABLE_BLOCK_CLOSE_SEPARATOR);
        }
    }

    private StringBuilder formReq(String key, JsonElement value) {
        StringBuilder msgString = new StringBuilder();
        if (!value.isJsonNull()) {
            msgString.append(key).append(PartyCreateHandlerHelper.PARTY_KEY_VALUE_SEPARATOR).append(value.getAsString()
                    )
                    .append(PartyCreateHandlerHelper.PARTY_COLUMN_SEPARATOR);

        }
        return msgString;
    }

    public void createParty() throws Exception {
        if (this.rqPayload == null) {
            logger.warning("Incorrect Request message passed to Essence to create Party. Sending Failed Response");
            this.isPartyCreated = false;
            return;
        }

        HashMap<String, Object> ptyParams = new HashMap<String, Object>();
        ptyParams.put("requestPayload", this.rqPayload);
        BankFusionThreadLocal.setChannel("IBI");
        BankFusionThreadLocal.setMFId("CB_PTY_MaintainPartyWS_SRV");
        FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("CB_PTY_MaintainPartyWS_SRV");
        try {
            logger.info("--------Creating the Customer---------- ");
            HashMap<String, Object> outputMap = invoker.invokeMicroflow(ptyParams, false);
            this.rsPayload = (RsPayload) outputMap.get("responsePayload");
            logger.info("\n\nResponse from Create Party API : \n"
                    + PartyCreateHandlerHelper.getXML(this.rsPayload, "bf.com.misys.party.ws.RsPayload"));
            setResFields();

        }
        catch (BankFusionException e) {
            logger.info("Bankfusion exception during Open Party");
            e.printStackTrace();
            setResFields(e);
            return;
        }
        catch (Exception e) {
            logger.info(" Error during Open Party ");

            e.printStackTrace();
            setResFields(e);
        }

    }

    @SuppressWarnings("rawtypes")
    private static boolean getModuleConfigValue(String param, String moduleId) {
        String value = "";
        HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
        ModuleKeyRq module = new ModuleKeyRq();
        ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
        module.setModuleId(moduleId);
        module.setKey(param);
        read.setModuleKeyRq(module);
        moduleParams.put("ReadModuleConfigurationRq", read);
        HashMap valueFromModuleConfiguration = MFExecuter.executeMF(PartyCreateHandler.READ_MODULE_CONFIGURATION,
                BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
        if (valueFromModuleConfiguration != null) {
            ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration
                    .get("ReadModuleConfigurationRs");
            value = rs.getModuleConfigDetails().getValue().toString();
        }
        if ("true".equalsIgnoreCase(value))
            return true;
        else return false;

    }

    private void setResFields(Exception ex) throws Exception {
        this.partyId = "";
        this.isPartyCreated = false;
        InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
        if (BankFusionException.class.isInstance(ex)) {
            BankFusionException bf = (BankFusionException) ex;
            Collection<IEvent> errors = bf.getEvents();
            Iterator<IEvent> errorIterator = errors.iterator();
            IEvent event = errorIterator.next();
            this.errorCode = (Integer.toString((event.getEventNumber())));
            this.errorMessage = event.getDescription();
            this.arguments = event.getDetails();
        }
        else {
            this.errorCode = "40000127";
            this.errorMessage = "E_AN_UNEXPECTED_ERROR_OCCURRED";
            this.arguments = null;
        }
    }

    private void setResFields() throws Exception {
        String res = this.rsPayload.getRsParam();
        String customerStatus = getData("STATUS", res);
        InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
        if (customerStatus != null && "Success".equalsIgnoreCase(customerStatus)) {
            this.partyId = getData("PARTYID", res);
            logger.info("Party creaion sucessful- Party created is " + this.partyId);
            this.isPartyCreated = true;
            this.errorCode = "";
            this.errorMessage = "";
            this.arguments = null;
        }
        else {
            logger.info("Party creation failed");
            this.partyId = "";
            this.isPartyCreated = false;
            this.errorCode = getData("ERRORCODE", res);
            this.errorMessage = getData("ERRORMSG", res);;
            this.arguments = null;
        }
    }

    public void sendFailedPartyCreationRs() throws Exception {
        
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray argumentList = null;
        String argJson = gson.toJson(arguments);
        if(arguments != null)
            argumentList = parser.parse(argJson).getAsJsonArray();
        
        JsonObject errRes = new JsonObject();
        
        errRes.addProperty("status", "E");
        errRes.addProperty("reasonCode", errorCode);
        errRes.addProperty("errorMessage", errorMessage);
        errRes.add("arguments", argumentList);
        errRes.addProperty("msgId", this.createPartyAndAccountRq.get("msgId").getAsString());
        errRes.addProperty("msgType", "PARTY_ONBOARD_ACCOUNT_OPEN_RES");
        errRes.addProperty("origCtxtId", "IBI");
        InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
        instr.sendFailOfferResponse(this.createPartyAndAccountRq.get("msgId").getAsString(), "E", errorCode, errorMessage, argJson,
                "",null);
        // postToQueue(errRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        logger.warning("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private boolean isArrayValid(JsonArray array) {
        boolean isArrayValid = false;
        if ((array != null) && (array.size() > 0))
            isArrayValid = true;
        return isArrayValid;
    }

    private String getData(String key, String input) {
        String valueString = null;
        String[] resultSet = input.split(";");
        for (int i = 0; i < resultSet.length; i++) {
            if (resultSet[i].contains(key)) {
                valueString = resultSet[i].split("=")[1].trim();
            }
        }
        return valueString;
    }

    private void postToQueue(String message, String queueEndpoint) {
        logger.info("message sent from Essence is \n" + message);
        logger.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

}
