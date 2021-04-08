package com.misys.ub.dc.handler.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.ub.dc.common.ErrorCodes;
import com.misys.ub.dc.common.OfferJsonReader;
import com.misys.ub.dc.common.OfferJsonTuner;
import com.misys.ub.dc.common.QueueName;
import com.misys.ub.dc.common.RequestResponseConstants;
import com.misys.ub.dc.sql.constants.SqlSelectStatements;
import com.misys.ub.fatoms.OpenAccountEventHandler;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

import bf.com.misys.cbs.types.CreateAccountRq;
import bf.com.misys.cbs.types.CreateAccountRs;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

public class OpenAccountHelper {
    
    private static final long serialVersionUID = 1L;

    private static final transient Log logger = LogFactory.getLog(OpenAccountEventHandler.class.getName());

    public String createCasaAccount(JsonObject jsonCasaAccountFields, String partyId, String msgId) {
        String accOpenStatus = null;
        try {
            Gson gson = new Gson();
            
            fetchAccOpeningParams(jsonCasaAccountFields);
            
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
            accOpenStatus = status.getOverallStatus();

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
                postToQueue(successRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
            }
            else {
                logger.info("Account is not created ");
                sendRejectedOfferReply(nullCheckForJsonElement(jsonCasaAccountFields.get(RequestResponseConstants.MESSAGE_ID)),
                        partyId, ErrorCodes.EVT_ACCOUNT_CREATION);
            }
        }
        catch (Exception e) {
            logger.error("Error during Account creation", e);
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            logger.error("Account is not created ");
            logger.error("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
            sendRejectedOfferReply(nullCheckForJsonElement(jsonCasaAccountFields.get(RequestResponseConstants.MESSAGE_ID)), partyId,
                    ErrorCodes.EVT_ACCOUNT_CREATION);
        }
        return accOpenStatus;
    }
    
    @SuppressWarnings("unchecked")
    private void fetchAccOpeningParams(JsonObject jsonCasaAccountFields) {
        List<SimplePersistentObject> subProductList = Lists.newArrayList();
        OfferJsonTuner offerJsonTuner = new OfferJsonTuner();
        OfferJsonReader jsonReader = new OfferJsonReader();
        ListIterator<SimplePersistentObject> subProducts = null;
        ArrayList<Object> params = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        String subProd = StringUtils.EMPTY;
        String subProdCur = StringUtils.EMPTY;

        String subProductId = jsonReader.getSubProductIdForCasa(jsonCasaAccountFields);
        params.add(subProductId);
        subProductList = fetchDataFromDB(params, SqlSelectStatements.SUB_PRODUCT_QUERY);
        if (subProductList != null) {
            subProducts = subProductList.listIterator();
            while (subProducts.hasNext()) {
                SimplePersistentObject subProdDetails = (SimplePersistentObject) subProducts.next();
                dataMap = subProdDetails.getDataMap();
                if (dataMap.get(IBOProductInheritance.UBSUBPRODUCTID) != null) {
                    subProd = (String) dataMap.get(IBOProductInheritance.UBSUBPRODUCTID);
                    subProdCur = (String) dataMap.get(IBOProductInheritance.ACC_ISOCURRENCYCODE);
                }

                logger.info("SubProduct is " + subProd + " Sub Prod Currency " + subProdCur);

            }
        }
        offerJsonTuner.updateSubProdCurrToCasaAcc(jsonCasaAccountFields, subProdCur);
        offerJsonTuner.updateSubProductIdToCasaAcc(jsonCasaAccountFields, subProd);
        offerJsonTuner.updateAccountOpeningDateToCasaAcc(jsonCasaAccountFields);
    }
    
    @SuppressWarnings("unchecked")
    private List<SimplePersistentObject> fetchDataFromDB(ArrayList<Object> params, String query) {
        List<SimplePersistentObject> resultSet = null;
        try {
            IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
            resultSet = factory.executeGenericQuery(query, params, null, false);
            logger.info("resultSet is " + resultSet);
        }
        catch (BankFusionException bfException) {
            logger.info(bfException.getMessageNumber() + " : " + bfException.getLocalizedMessage());
            return null;
        }
        catch (Exception unexpectedError) {
            logger.error("Error occured while fetching subproduct Records ", unexpectedError);
            return null;
        }
        return resultSet;
    }

    private void sendRejectedOfferReply(String msgId, String customerId, String reasonCode) {
        JsonObject errRes = new JsonObject();
        errRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_FAILURE);
        errRes.addProperty(RequestResponseConstants.MESSAGE_ID, msgId);
        errRes.addProperty(RequestResponseConstants.MESSAGE_TYPE, RequestResponseConstants.MESSAGE_TYPE_VALUE);
        errRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, RequestResponseConstants.CHANNEL_FOR_CREATE_ACCOUNT);
        errRes.addProperty(RequestResponseConstants.PARTY_ID, customerId);
        errRes.addProperty(RequestResponseConstants.REASON_CODE, reasonCode);
        postToQueue(errRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
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
