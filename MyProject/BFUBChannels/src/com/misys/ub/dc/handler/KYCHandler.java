package com.misys.ub.dc.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.dc.common.InstructionStatusUpdateNotification;
import com.misys.ub.dc.common.QueueName;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

public class KYCHandler {
    private final static Logger logger = Logger.getLogger(KYCHandler.class.getName());
    private static final String MICROFLOW_FOR_KYC = "CB_PTY_KYCCheckForChannel_SRV";
    private static final String Channel = "IBI";

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private String errorCode = "";
    private String errorMessage = "";
    private String kycExecutionStatus = "";

    public String getKycExecutionStatus() {
        return kycExecutionStatus;
    }

    public void doKYC(String partyId) {
        try {
            logger.info("Calling KYC microflow");
            HashMap<String, String> inpMap = new HashMap<>();
            inpMap.put("partyID", partyId);
            MFExecuter.executeMF(KYCHandler.MICROFLOW_FOR_KYC, fetchEnvironment(), inpMap);
            this.kycExecutionStatus = "Success";
        }
        catch (BankFusionException e) {
            logger.info("Bankfusion exception during execution of KYC microflow");
            e.printStackTrace();
            setResFields(e);
            return;
        }
        catch (Exception e) {
            logger.info(" Error during execution of KYC microflow");
            e.printStackTrace();
            setResFields(e);
        }

    }

    private BankFusionEnvironment fetchEnvironment() {
        prepareEnvironment();
        return BankFusionThreadLocal.getBankFusionEnvironment();
    }

    private void prepareEnvironment() {
        BankFusionThreadLocal.setChannel(KYCHandler.Channel);
        BankFusionThreadLocal.setMFId(KYCHandler.MICROFLOW_FOR_KYC);
    }

    private void setResFields(Exception ex) {
        this.kycExecutionStatus = "Failure";
        if (BankFusionException.class.isInstance(ex)) {
            BankFusionException bf = (BankFusionException) ex;
            Collection<IEvent> errors = bf.getEvents();
            Iterator<IEvent> errorIterator = errors.iterator();
            IEvent event = errorIterator.next();
            this.errorCode = (Integer.toString((event.getEventNumber())));
            this.errorMessage = event.getDescription();

        }
        else {
            this.errorCode = "40000127";
            this.errorMessage = "E_AN_UNEXPECTED_ERROR_OCCURRED";
        }
    } 
    
    public void sendFailedPartyCreationRs(String msgId, String customerId) {
        JsonObject errRes = new JsonObject();
        errRes.addProperty("status", "E");
        errRes.addProperty("reasonCode", "40109361");
        errRes.addProperty("msgId", msgId);
        errRes.addProperty("msgType", "PARTY_ONBOARD_ACCOUNT_OPEN_RES");
        errRes.addProperty("origCtxtId", "IBI");
        errRes.addProperty("partyId", customerId);
        //postToQueue(errRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        try {
        	InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
            response.sendFailOfferResponse(msgId, "E", "40109361", "", "", customerId, null);
        }catch (Exception e) {
			// TODO: handle exception
        	//logger.error("Error occoured while parsing message"+errRes.toString()+" exception is "+ExceptionUtil.getExceptionAsString(e));
		}
        logger.warning("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }
    
    private void postToQueue(String message, String queueEndpoint) {
        logger.info("message sent from Essence is \n" + message);
        logger.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }
}
