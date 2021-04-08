package com.misys.ub.dc.handler.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.util.BankFusionPropertySupport; 

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.dc.common.QueueName;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

import bf.com.misys.party.ws.RqPayload;
import bf.com.misys.party.ws.RsPayload;

public class UpdateFatcaDetailsHelper {

    private final static Logger logger = Logger.getLogger(UpdateFatcaDetailsHelper.class.getName());
    
    private static final String COLUMN_SEPARATOR = BankFusionPropertySupport.getProperty("PARTY_COLUMN_SEPARATOR",CommonConstants.EMPTY_STRING);
    
    private StringBuilder fatcaRqParam;
    
    private StringBuilder fatcaRqParamRollback;

    private JsonObject createPartyAndAccountRq;

    private RqPayload rqPayload;

    private RsPayload rsPayload;

    private String partyId;

    private Object[] arguments;

    private String errorCode;

    private String errorMessage;

    private boolean isPartyUpdated;

    public String getPartyId() {
        return partyId;
    }

    public Object[] getArguments() {
        return arguments;
    }
    
    public RqPayload getRqPayload() {
        return rqPayload;
    }
    
    public void setRqPayload(RqPayload rqPayload) {
        this.rqPayload = rqPayload;
    }
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public StringBuilder getFatcaRqParamRollback() {
        return fatcaRqParamRollback;
    }

    public boolean isPartyUpdated() {
        return isPartyUpdated;
    }

    public UpdateFatcaDetailsHelper(JsonObject createPartyAndAccountRq) {
        this.createPartyAndAccountRq = createPartyAndAccountRq;
        this.rqPayload = new RqPayload();
        setRqPayload();
    }

    private void setRqPayload() {
        StringBuilder rqParam = formRqParam();
        if (rqParam == null) {
            this.rqPayload = null;
            return;
        }

        String controlParam = "DEDUP_REQD=N;TXN_COMMIT_LEVEL=A;GEN_CODE_VALDN_REQ=N;PARTY_ACTION=A";

        this.rqPayload.setRqParam(rqParam.toString());
        logger.info("\n\nPayload for party amendment : \n" + rqParam);
        this.rqPayload.setControlParam(controlParam);
        logger.info("\n\nControl param for party amendment : \n" + controlParam);

    }

    private StringBuilder formRqParam() {
        JsonArray rptBlockArray = null;
        StringBuilder rqParam = new StringBuilder();
        fatcaRqParam = new StringBuilder();
        fatcaRqParamRollback = new StringBuilder();
        JsonObject jsonObject = this.createPartyAndAccountRq;

        JsonObject ptyFldsObj = jsonObject.getAsJsonObject(PartyCreateHandlerHelper.PARTY_FIELD_KEY);
        if (ptyFldsObj == null) {
            return null;
        }

        rptBlockArray = extractAndValidateArray(ptyFldsObj);
        if (rptBlockArray != null) {
            ptyFldsObj.remove(PartyCreateHandlerHelper.INPUT_REPETEABLE_BLOCK_IDENTIFIER);
        }

        appendPartyIdToRqParam(jsonObject, rqParam);
        appendPartyIdToRqParam(jsonObject, fatcaRqParam);
        appendPartyIdToRqParam(jsonObject, fatcaRqParamRollback);
        appendUniqueIdToRqParam(ptyFldsObj, rqParam);
        appendUniqueIdToRqParam(ptyFldsObj, fatcaRqParam);
        appendUniqueIdToRqParam(ptyFldsObj, fatcaRqParamRollback);
        appendFatcaBlockToRqParam(rptBlockArray, rqParam);

        return rqParam;
    }

    private void appendPartyIdToRqParam(JsonObject jsonObject, StringBuilder rqParam) {
        String partyIdKey = PartyCreateHandlerHelper.KEY_PARTYID_IDENTIFIER;
        JsonElement partyIdValue = jsonObject.get(partyIdKey);
        rqParam.append(formReq(partyIdKey, partyIdValue).toString());
    }

    private void appendUniqueIdToRqParam(JsonObject ptyFldsObj, StringBuilder rqParam) {
        String uniqueIdKey = PartyCreateHandlerHelper.KEY_UNIQUEID_IDENTIFIER;
        JsonElement uniqueIdValue = ptyFldsObj.get(uniqueIdKey);
        rqParam.append(formReq(uniqueIdKey, uniqueIdValue).toString());
    }

    private void appendFatcaBlockToRqParam(JsonArray rptBlockArray, StringBuilder rqParam) {
    	boolean fatcaBlockPresent = false;
        if ((rptBlockArray == null) || (rptBlockArray.size() == 0)) {
            return;
        }
        for (JsonElement rptBlockElement : rptBlockArray) {
            JsonObject block = rptBlockElement.getAsJsonObject();

            if (block.get(PartyCreateHandlerHelper.KEY_FATCA_IDENTIFIER) != null) {
            	fatcaBlockPresent = true;
                rqParam.append(PartyCreateHandlerHelper.REPETEABLE_BLOCK_OPEN_SEPARATOR);
                JsonObject fatcaBlock = block;
                appendBlockToRqParam(fatcaBlock, rqParam);  
                rqParam.append(PartyCreateHandlerHelper.REPETEABLE_BLOCK_CLOSE_SEPARATOR);
            }
        }
        if(fatcaBlockPresent) {
        	prepareFatcaRollBlock();
        }
    }
    
    private void prepareFatcaRollBlock() {
        
        HashMap<String, Object> params = new HashMap<String, Object>();
        RqPayload rqPayload = new RqPayload();
        fatcaRqParam.append("PT_PFN_FatcaDetails;");
        rqPayload.setRqParam(fatcaRqParam.toString());
        params.put("requestPayload", rqPayload);
        FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("CB_PTY_ReadPartyDetailsWS_SRV");
        HashMap outputMap = invoker.invokeMicroflow(params, false);
        RsPayload rs = (RsPayload) outputMap.get("responsePayload");
        String rsParam = rs.getRsParam();
        fatcaRqParamRollback.append(getFatcaBlockRollback(rsParam));
        
    }
    
    private String getResidentCountry(String json) {
        int startIndex = json.indexOf(PartyCreateHandlerHelper.KEY_RESIDENTCOUNTRY);
        int endIndex = json.indexOf(COLUMN_SEPARATOR, startIndex);
        int resKey_length = PartyCreateHandlerHelper.KEY_RESIDENTCOUNTRY.length();
        
        String res_country = json.substring(startIndex+resKey_length+1,endIndex);
        return res_country;
    }
    
    private String getFatcaBlockRollback(String json) {
        int startIndex = json.indexOf(PartyCreateHandlerHelper.DEFAULT_SUB_RECORD_SEPARATOR_START);
        int endIndex = json.lastIndexOf(PartyCreateHandlerHelper.DEFAULT_SUB_RECORD_SEPARATOR_END);
        String fatcablock = "";
        if(startIndex >= 0 ){
            fatcablock = json.substring(startIndex, endIndex+1);            
        }
        
        return fatcablock;
    }
    
    private boolean isNullOrEmpty(JsonElement value) {
        if(value == null || value.toString().length() == 0) {
            return true;
          }
          return false;
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

    private JsonArray extractAndValidateArray(JsonObject rptBlckObj) {
        JsonArray rptBlockArray = null;
        rptBlockArray = rptBlckObj.getAsJsonArray(PartyCreateHandlerHelper.INPUT_REPETEABLE_BLOCK_IDENTIFIER);
        if (isArrayValid(rptBlockArray)) {
            return rptBlockArray;
        } else
            return null;
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

    private StringBuilder formReq(String key, JsonElement value) {
        StringBuilder msgString = new StringBuilder();
        if (!value.isJsonNull()) {
            msgString.append(key).append(PartyCreateHandlerHelper.PARTY_KEY_VALUE_SEPARATOR).append(value.getAsString())
                .append(PartyCreateHandlerHelper.PARTY_COLUMN_SEPARATOR);
        }

        return msgString;
    }

    public void amendFatcaDetails() {
        if (this.rqPayload == null) {
            logger.warning("Incorrect Request message passed to Essence to amend Party. Sending Failed Response");
            this.isPartyUpdated = false;
            return;
        }

        HashMap<String, Object> ptyParams = new HashMap<String, Object>();
        ptyParams.put("requestPayload", this.rqPayload);
        BankFusionThreadLocal.setChannel("IBI");
        BankFusionThreadLocal.setMFId("CB_PTY_MaintainPartyWS_SRV");
        FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("CB_PTY_MaintainPartyWS_SRV");
        try {
            logger.info("--------Amending the Fatca details for customer---------- ");
            HashMap<String, Object> outputMap = invoker.invokeMicroflow(ptyParams, false);
            this.rsPayload = (RsPayload) outputMap.get("responsePayload");
            logger.info("\n\nResponse from Amend Party API : \n"
                + PartyCreateHandlerHelper.getXML(this.rsPayload, "bf.com.misys.party.ws.RsPayload"));
            setResFields();

        } catch (BankFusionException e) {
            logger.log(Level.SEVERE,"Party cannot be amended, Bankfusion exception occured !!!");
            e.printStackTrace();
            setResFields(e);
            return;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Party cannot be amended, exception occured !!");
            e.printStackTrace();
            setResFields(e);
        }
    }

    private void setResFields(Exception ex) {
        this.partyId = "";
        this.isPartyUpdated = false;
        if (BankFusionException.class.isInstance(ex)) {
            BankFusionException bf = (BankFusionException) ex;
            Collection<IEvent> errors = bf.getEvents();
            Iterator<IEvent> errorIterator = errors.iterator();
            IEvent event = errorIterator.next();
            this.errorCode = (Integer.toString((event.getEventNumber())));
            this.errorMessage = event.getDescription();
            this.arguments = event.getDetails();
        } else {
            this.errorCode = "40000127";
            this.errorMessage = "E_AN_UNEXPECTED_ERROR_OCCURRED";
            this.arguments = null;
        }
    }

    private void setResFields() {
        String res = this.rsPayload.getRsParam();
        String customerStatus = getData("STATUS", res);
        if (customerStatus != null && "Success".equalsIgnoreCase(customerStatus)) {
            this.partyId = getData("PARTYID", res);
            logger.info("Party amended successfully with Party ID : " + this.partyId);
            this.isPartyUpdated = true;
            this.errorCode = "";
            this.errorMessage = "";
            this.arguments = null;
        } else {
            logger.info("Party amendment failed");
            this.partyId = "";
            this.isPartyUpdated = false;
            this.errorCode = getData("ERRORCODE", res);
            this.errorMessage = getData("ERRORMSG", res);
            ;
            this.arguments = null;
        }
    }
    
    public void sendFailedPartyCreationRs() {
        
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
        postToQueue(errRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        logger.warning("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }
    
    private void postToQueue(String message, String queueEndpoint) {
        logger.info("message sent from Essence is \n" + message);
        logger.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

}
