/* ********************************************************************************
 *  Copyright(c)2018  Finastra. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */package com.misys.ub.dc.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.cbs.party.constants.PartyConstants;
import com.misys.fbe.mmk.fd.fatoms.OpenFixedDeposit;
import com.misys.ub.dc.common.ErrorCodes;
import com.misys.ub.dc.common.InstructionStatusUpdateNotification;
import com.misys.ub.dc.common.QueueName;
import com.misys.ub.dc.common.RequestResponseConstants;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import bf.com.misys.ub.types.mmk.OpenFixedDepositRq;
import bf.com.misys.ub.types.mmk.OpenFixedDepositRs;

/**
 * This class is used for creating the Term Deposit
 * 
 * @author datiwari
 *
 */
public class TermDepositService {

    private JsonObject routeJsonObject;

    public TermDepositService(JsonObject routeJsonObject) {
        this.routeJsonObject = routeJsonObject;

    }

    private static final Log LOGGER = LogFactory.getLog(TermDepositService.class.getName());

    /**
     * Method Description: This method is used to create a Term Deposit
     * 
     * @return <code>true</code> if Term Deposit is successfully created otherwise
     *         <code>false</code>
     */

    public boolean createTermDeposit(JsonObject termDepositJsonObject) {
        boolean istermDepositCreated = false;
        Gson gson = new GsonBuilder().setDateFormat(RequestResponseConstants.DATE_FORMAT).create();
        try {
            OpenFixedDepositRq openFixedDepositRq = gson.fromJson(termDepositJsonObject.getAsJsonObject(),
                    OpenFixedDepositRq.class);
            openFixedDepositRq.setCustomerId(routeJsonObject.get(RequestResponseConstants.PARTY_ID).getAsString());
            OpenFixedDepositRs openFixedDepositRs = openFixedDeposit(openFixedDepositRq);
            if (openFixedDepositRs != null && openFixedDepositRs.getRsHeader() != null
                    && openFixedDepositRs.getRsHeader().getStatus() != null) {
                String responseStatus = openFixedDepositRs.getRsHeader().getStatus().getOverallStatus();

                if (RequestResponseConstants.STATUS_SUCCESS.equalsIgnoreCase(responseStatus)) {
                    sendSuccessResposnse(openFixedDepositRs.getAccountNumber());
                    istermDepositCreated = true;
                }
                else if (RequestResponseConstants.STATUS_FAILURE.equalsIgnoreCase(responseStatus)) {
                    sendFailureResponse(openFixedDepositRs.getRsHeader().getStatus().getCodes(0).getCode());
                }
            }

        }
        catch (Exception exception) {
            LOGGER.error("Exception occurred while creating the Term Deposit : " + BatchUtil.getExceptionAsString(exception));
            sendFailureResponse(ErrorCodes.UNEXPECTED_ERROR);
        }
        return istermDepositCreated;
    }

    private OpenFixedDepositRs openFixedDeposit(OpenFixedDepositRq openFixedDepositRq) {
        OpenFixedDeposit openFD = new OpenFixedDeposit();
        openFD.setF_IN_openFixedDepositRq(openFixedDepositRq);
        BankFusionThreadLocal.setChannel(PartyConstants.CHANNEL_ID);
        openFD.process(BankFusionThreadLocal.getBankFusionEnvironment());
        return openFD.getF_OUT_openFixedDepositRs();
    }

    private void sendSuccessResposnse(String accountId) {
        LOGGER.info("Sending success response for Term Deposit creation");
        JsonObject successRes = new JsonObject();
        String msgId =  routeJsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString();
        String partyId =  routeJsonObject.get(RequestResponseConstants.PARTY_ID).getAsString();
        successRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_SUCCESS);
        successRes.addProperty(RequestResponseConstants.MESSAGE_ID,
                routeJsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString());
        successRes.addProperty(RequestResponseConstants.MESSAGE_TYPE,RequestResponseConstants.MESSAGE_TYPE_VALUE); 
        successRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, PartyConstants.CHANNEL_ID);
        successRes.addProperty(RequestResponseConstants.TD_ACCOUNT_NUMBER, accountId);
        successRes.addProperty(RequestResponseConstants.PARTY_ID,
                routeJsonObject.get(RequestResponseConstants.PARTY_ID).getAsString());
        if (routeJsonObject.get(RequestResponseConstants.DOCUMENTS) != null
                && routeJsonObject.get(RequestResponseConstants.DOCUMENTS).getAsJsonObject() != null) {
            successRes.add(RequestResponseConstants.DOCUMENTS,
                    routeJsonObject.get(RequestResponseConstants.DOCUMENTS));
        }

       // postToQueue(successRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        try {
        	InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
            response.sendFailOfferResponse(msgId, "S", "", "", "", partyId , routeJsonObject);
        }catch (Exception e) {
			// TODO: handle exception
        	LOGGER.error("Error occoured while parsing message"+successRes.toString()+" exception is "+ExceptionUtil.getExceptionAsString(e));
		}
        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private void sendFailureResponse(String reasonCode) {
        LOGGER.info("Sending failure response for Term Deposit creation");
        JsonObject errorRes = new JsonObject();
        String msgId =  routeJsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString();
        errorRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_FAILURE);
        errorRes.addProperty(RequestResponseConstants.MESSAGE_ID,
        		msgId);
        errorRes.addProperty(RequestResponseConstants.MESSAGE_TYPE,RequestResponseConstants.MESSAGE_TYPE_VALUE);
        errorRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, PartyConstants.CHANNEL_ID);
        errorRes.addProperty(RequestResponseConstants.REASON_CODE, reasonCode);

        if (routeJsonObject.get(RequestResponseConstants.DOCUMENTS) != null
                && routeJsonObject.get(RequestResponseConstants.DOCUMENTS).getAsJsonObject() != null) {
            errorRes.add(RequestResponseConstants.DOCUMENTS,
                    routeJsonObject.get(RequestResponseConstants.DOCUMENTS));
        }
        
        //postToQueue(errorRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        try {
        	InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
            response.sendFailOfferResponse(msgId, "E", reasonCode, "", "", "",routeJsonObject);
        }catch (Exception e) {
			// TODO: handle exception
        	LOGGER.error("Error occoured while parsing message"+errorRes.toString()+" exception is "+ExceptionUtil.getExceptionAsString(e));
		}
        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private void postToQueue(String message, String queueEndpoint) {
        LOGGER.info("Message sent from Essence is \n" + message);
        LOGGER.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);

    }
}
