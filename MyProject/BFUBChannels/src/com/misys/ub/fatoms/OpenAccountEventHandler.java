package com.misys.ub.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.dc.common.ErrorCodes;
import com.misys.ub.dc.common.InstructionStatusUpdateNotification;
import com.misys.ub.dc.common.QueueName;
import com.misys.ub.dc.common.RequestResponseConstants;
import com.misys.ub.dc.service.TermDepositService;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_OPENACCOUNTDETAILS;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_OpenAccountHandler;

import bf.com.misys.cbs.types.CreateAccountRq;
import bf.com.misys.cbs.types.CreateAccountRs;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

/**
 * @author susikdar <code>OpenAccountEventHandler</code> work as event handler for opening account
 *         based on the provided data from DC
 */
public class OpenAccountEventHandler extends AbstractUB_CMN_OpenAccountHandler {

    private static final long serialVersionUID = 1L;

    private static final transient Log logger = LogFactory.getLog(OpenAccountEventHandler.class.getName());
    
    private JsonObject jsonInputForOpenAccount;

    public OpenAccountEventHandler(BankFusionEnvironment env) {
        super(env);
    }

    public OpenAccountEventHandler() {
        super();
    }

    /**
     * <code>process</code> takes the partyId as input & invoke the open account API based on
     * different account types
     */
    @SuppressWarnings("unchecked")
    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        String partyId = getF_IN_partyId();
        /*List<IBOUB_INF_OPENACCOUNTDETAILS> openAccountDetails = null;

        String queryBasedOnPartyId = new StringBuilder().append("WHERE ").append(IBOUB_INF_OPENACCOUNTDETAILS.PARTYID)
                .append(" = ?").toString();
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

        ArrayList<String> queryParams = new ArrayList<>();
        queryParams.add(partyId);*/
        
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();


        try {
            /*openAccountDetails = (List<IBOUB_INF_OPENACCOUNTDETAILS>) factory.findByQuery(IBOUB_INF_OPENACCOUNTDETAILS.BONAME,
                    queryBasedOnPartyId, queryParams, null, true);

            if (null != openAccountDetails && openAccountDetails.size() >= 1) {
                jsonInputForOpenAccount = getJSON(openAccountDetails.get(0).getF_MESSAGE());
                IBOCustomer customer = (IBOCustomer) factory.findByPrimaryKey(IBOCustomer.BONAME, partyId, true);
                String kycStatus = customer.getF_UBKYCSTATUS();

                if (kycStatus.equalsIgnoreCase("001")) {
                    redirectOpenAccountMFBasedOnAccountType(jsonInputForOpenAccount, partyId);
                }
                else {
                    sendRejectedOfferReply(
                            nullCheckForJsonElement(jsonInputForOpenAccount.get(RequestResponseConstants.MESSAGE_ID)), partyId,
                            ErrorCodes.CUST_KYC_STATUS_NOT_COMPLIED_UB);
                }

                factory.bulkDelete(IBOUB_INF_OPENACCOUNTDETAILS.BONAME, queryBasedOnPartyId, queryParams);

            }
            else {
                logger.info("accountdetails are empty");
                return;
            }*/
            IBOCustomer customer = (IBOCustomer) factory.findByPrimaryKey(IBOCustomer.BONAME, partyId, true);
            String kycStatus = customer.getF_UBKYCSTATUS();

            if (kycStatus.equalsIgnoreCase("001")) {
                redirectOpenAccountMFBasedOnAccountType(jsonInputForOpenAccount, partyId);
            }
            else {
                sendRejectedOfferReply(
                        nullCheckForJsonElement(jsonInputForOpenAccount.get(RequestResponseConstants.MESSAGE_ID)), partyId,
                        ErrorCodes.CUST_KYC_STATUS_NOT_COMPLIED_UB);
            }
        }
        catch (BankFusionException bfe) {
            logger.info("BankFusion exception occured while fetching or deleting data from OpenAccountDetails or Customer model"
                    + BatchUtil.getExceptionAsString(bfe));
            if (jsonInputForOpenAccount != null) {
                sendRejectedOfferReply(nullCheckForJsonElement(jsonInputForOpenAccount.get(RequestResponseConstants.MESSAGE_ID)),
                        partyId, ErrorCodes.EVT_ACCOUNT_CREATION);
            }
        }
        catch (Exception e) {
            logger.info("Exception while fetching or deleting data from OpenAccountDetails or Customer model"
                    + BatchUtil.getExceptionAsString(e));
            if (jsonInputForOpenAccount != null) {
                sendRejectedOfferReply(nullCheckForJsonElement(jsonInputForOpenAccount.get(RequestResponseConstants.MESSAGE_ID)),
                        partyId, ErrorCodes.EVT_ACCOUNT_CREATION);
            }
        }
    }

    
    public JsonObject getJsonInputForOpenAccount() {
        return jsonInputForOpenAccount;
    }

    
    public void setJsonInputForOpenAccount(JsonObject jsonInputForOpenAccount) {
        this.jsonInputForOpenAccount = jsonInputForOpenAccount;
    }

    private void redirectOpenAccountMFBasedOnAccountType(JsonObject jsonInput, String partyId) {

        if (jsonInput.get(RequestResponseConstants.ACCOUNT_FIELDS) != null) {
            Iterator<JsonElement> jsonIterator = jsonInput.get(RequestResponseConstants.ACCOUNT_FIELDS).getAsJsonArray().iterator();
            JsonObject jsonAccountFields = new JsonObject();
            String msgId = jsonInput.get(RequestResponseConstants.MESSAGE_ID).getAsString();

            while (jsonIterator.hasNext()) {
                jsonAccountFields = jsonIterator.next().getAsJsonObject();

                if (jsonAccountFields.get(RequestResponseConstants.CASA_ACCOUNT_FIELDS) != null) {
                    createCasaAccount(jsonAccountFields.get(RequestResponseConstants.CASA_ACCOUNT_FIELDS).getAsJsonObject(),
                            partyId, msgId);
                }
                else if (jsonAccountFields.get(RequestResponseConstants.TD_ACCOUNT_FIELDS) != null
                        && jsonAccountFields.get(RequestResponseConstants.TD_ACCOUNT_FIELDS).getAsJsonObject() != null) {
                    jsonInput.addProperty(RequestResponseConstants.PARTY_ID, partyId);
                    createTermDeposit(jsonInput,
                            jsonAccountFields.get(RequestResponseConstants.TD_ACCOUNT_FIELDS).getAsJsonObject());
                }
            }
        }
    }

    private void createTermDeposit(JsonObject routeJsonObject, JsonObject termDepositFields) {
        TermDepositService termDepositService = new TermDepositService(routeJsonObject);
        termDepositService.createTermDeposit(termDepositFields.get(RequestResponseConstants.TERM_DEPOSIT_REQ).getAsJsonObject());
    }

    private void createCasaAccount(JsonObject jsonCasaAccountFields, String partyId, String msgId) {
        try {
            Gson gson = new Gson();
            CreateAccountRq createAccountRq = gson.fromJson(
                    jsonCasaAccountFields.get(RequestResponseConstants.CREATE_ACCOUNT_REQ).getAsJsonObject(),
                    CreateAccountRq.class);
            createAccountRq.setCustomerNo(partyId);

            HashMap<String, Object> accParams = new HashMap<String, Object>();
            accParams.put(RequestResponseConstants.CREATE_ACCOUNT_REQ, createAccountRq);

            BankFusionThreadLocal.setChannel(RequestResponseConstants.CHANNEL_FOR_CREATE_ACCOUNT);
            BankFusionThreadLocal.setMFId("CB_ACC_CreateAccount_SRV");

            logger.info("-----------Account opening started-------------------");

            FBPMicroflowServiceInvoker invoker2 = new FBPMicroflowServiceInvoker("CB_ACC_CreateAccount_SRV");

            HashMap<String, Object> outputMap2 = invoker2.invokeMicroflow(accParams, false);
            CreateAccountRs rs2 = (CreateAccountRs) outputMap2.get("createAccountRs");

            logger.info("\n\n Response from CreateAccount Service: \n" + getXML(rs2, "bf.com.misys.cbs.types.CreateAccountRs"));

            String accNum = rs2.getAccountNo();
            String accName = rs2.getAccountName();

            RsHeader resHeader = rs2.getRsHeader();
            MessageStatus status = resHeader.getStatus();
            String accOpenStatus = status.getOverallStatus();

            if (accOpenStatus != null && accOpenStatus.equalsIgnoreCase(RequestResponseConstants.STATUS_SUCCESS)) {
                logger.info("Account is created and the account Name is " + accName + " and account number is " + accNum);
                JsonObject successRes = new JsonObject();
                successRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_SUCCESS);
                successRes.addProperty(RequestResponseConstants.MESSAGE_ID, msgId);
                successRes.addProperty(RequestResponseConstants.MESSAGE_TYPE, RequestResponseConstants.MESSAGE_TYPE_VALUE);
                successRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID,
                        RequestResponseConstants.CHANNEL_FOR_CREATE_ACCOUNT);
                successRes.addProperty(RequestResponseConstants.PARTY_ID, partyId);
                successRes.addProperty(RequestResponseConstants.ACCOUNT_ID_JSON_KEY, accNum);
                successRes.addProperty(RequestResponseConstants.ACCOUNT_NAME_JSON_KEY, accName);
                successRes.add(RequestResponseConstants.DOCUMENTS, jsonCasaAccountFields.get(RequestResponseConstants.DOCUMENTS));
               // postToQueue(successRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
                try {
                	InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
                    response.sendFailOfferResponse(msgId, "S", "", "", "", partyId , jsonCasaAccountFields);
                }catch (Exception e) {
        			// TODO: handle exception
                	logger.error("Error occoured while parsing message"+successRes.toString()+" exception is "+ExceptionUtil.getExceptionAsString(e));
        		}
                
            }
            else {
                logger.info("Account is not created ");
                sendRejectedOfferReply(nullCheckForJsonElement(jsonCasaAccountFields.get(RequestResponseConstants.MESSAGE_ID)),
                        partyId, ErrorCodes.EVT_ACCOUNT_CREATION);
            }
        }
        catch (Exception e) {
            logger.info("Error during Account creation" + e.getMessage());
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            logger.info("Account is not created ");
            logger.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
            sendRejectedOfferReply(nullCheckForJsonElement(jsonCasaAccountFields.get(RequestResponseConstants.MESSAGE_ID)), partyId,
                    ErrorCodes.EVT_ACCOUNT_CREATION);
        }
    }

    private void sendRejectedOfferReply(String msgId, String customerId, String reasonCode) {
        JsonObject errRes = new JsonObject();
        errRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_FAILURE);
        errRes.addProperty(RequestResponseConstants.MESSAGE_ID, msgId);
        errRes.addProperty(RequestResponseConstants.MESSAGE_TYPE, RequestResponseConstants.MESSAGE_TYPE_VALUE);
        errRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, RequestResponseConstants.CHANNEL_FOR_CREATE_ACCOUNT);
        errRes.addProperty(RequestResponseConstants.PARTY_ID, customerId);
        errRes.addProperty(RequestResponseConstants.REASON_CODE, reasonCode);
        //postToQueue(errRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        try {
        	InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
            response.sendFailOfferResponse(msgId, "E", reasonCode, "", "", customerId, null);
        }catch (Exception e) {
			// TODO: handle exception
        	logger.error("Error occoured while parsing message"+errRes.toString());
		}
        logger.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private void postToQueue(String message, String queueEndpoint) {
        logger.info("message sent from Essence is \n" + message);
        logger.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

    private String getXML(Object obj, String objType) {
        @SuppressWarnings("deprecation")
        ComplexTypeConvertor converter = new ComplexTypeConvertor(this.getClass().getClassLoader());
        return converter.getXmlFromJava(objType, obj);
    }

    private JsonObject getJSON(String jsonString) {
        JsonParser parser = new JsonParser();
        try {
            return parser.parse(jsonString).getAsJsonObject();
        }
        catch (JsonParseException e) {
            logger.error("parsing error in json string to json object");
        }
        return null;
    }

    private String nullCheckForJsonElement(JsonElement jsonElement) {
        if (jsonElement == null) {
            return "";
        }
        if (jsonElement.isJsonArray()) {
            return jsonElement.toString();
        }
        return jsonElement.getAsString();
    }
}