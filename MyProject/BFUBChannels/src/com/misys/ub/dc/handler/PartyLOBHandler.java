/* ********************************************************************************
 *  Copyright(c)2018  Finastra Financial Software Solutions. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra Financial Software Solutions.
 *  Use is subject to license terms.
 * ********************************************************************************
 * 
 */

package com.misys.ub.dc.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.party.dto.EnableDisableLOBDtlsRsDTO;
import com.google.gson.JsonObject;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.cbs.party.constants.PartyConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.dc.api.client.PartyApiClient;
import com.misys.ub.dc.common.ErrorCodes;
import com.misys.ub.dc.common.QueueName;
import com.misys.ub.dc.common.RequestResponseConstants;
import com.misys.ub.dc.types.CreatePartyAndAccountRq;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

/**
 * This class is used as a handler to update line of business for a particular party
 * 
 * @author Nisha Kumari
 *
 */
public class PartyLOBHandler {

    private static final Log LOGGER = LogFactory.getLog(CreatePartyAndAccountRq.class.getName());

    private JsonObject jsonObject;

    public PartyLOBHandler(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Method Description: Method used to update line of business for a particular party
     * 
     * @return <code>true</code> if line of business is successfully updated otherwise
     *         <code>false</code>
     * 
     */
    public boolean updateLOB() {
        boolean isLOBUpdated = false;

        try {
            String enableLOB = jsonObject.get(RequestResponseConstants.ENABLE_LOB).getAsString();

            if (!(CommonConstants.TRUE.equalsIgnoreCase(enableLOB) || CommonConstants.FALSE.equalsIgnoreCase(enableLOB))) {
                CommonUtil.handleParameterizedEvent(ErrorCodes.INVALID_INPUT_VALUE,
                        new String[] { RequestResponseConstants.ENABLE_LOB });
            }

            EnableDisableLOBDtlsRsDTO lineOfBusinessRsDTO = PartyApiClient.enableLOB(
                    jsonObject.get(RequestResponseConstants.PARTY_ID).getAsString(),
                    jsonObject.get(RequestResponseConstants.LINE_OF_BUSINESS).getAsString(), Boolean.parseBoolean(enableLOB));

            if (lineOfBusinessRsDTO != null) {
                if (RequestResponseConstants.STATUS_SUCCESS.equals(lineOfBusinessRsDTO.getStatus())) {
                    sendSuccessResposnse();
                    isLOBUpdated = true;
                }
                else if (RequestResponseConstants.STATUS_FAILURE.equals(lineOfBusinessRsDTO.getStatus())) {
                    sendFailedResposnse(lineOfBusinessRsDTO.getErrorCode());
                }
            }
        }
        catch (Exception exception) {
            LOGGER.debug("Exception occurred while calling the update party line of business service : "
                    + BatchUtil.getExceptionAsString(exception));
            sendFailedResposnse(ErrorCodes.UNEXPECTED_ERROR);
        }

        return isLOBUpdated;
    }

    private void sendFailedResposnse(String reasonCode) {
        LOGGER.info("Sending failed enable line of business response");
        JsonObject erroorRes = new JsonObject();
        erroorRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_FAILURE);
        erroorRes.addProperty(RequestResponseConstants.MESSAGE_ID,
                jsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString());
        erroorRes.addProperty(RequestResponseConstants.MESSAGE_TYPE, RequestResponseConstants.MESSAGE_TYPE_VALUE);
        erroorRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, PartyConstants.CHANNEL_ID);
        erroorRes.addProperty(RequestResponseConstants.PARTY_ID, jsonObject.get(RequestResponseConstants.PARTY_ID).getAsString());
        erroorRes.addProperty(RequestResponseConstants.REASON_CODE, reasonCode);
        postToQueue(erroorRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private void sendSuccessResposnse() {
        LOGGER.info("Sending success enable line of business response");
        JsonObject successRes = new JsonObject();
        successRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_SUCCESS);
        successRes.addProperty(RequestResponseConstants.MESSAGE_ID,
                jsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString());
        successRes.addProperty(RequestResponseConstants.MESSAGE_TYPE, RequestResponseConstants.MESSAGE_TYPE_VALUE);
        successRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, PartyConstants.CHANNEL_ID);
        successRes.addProperty(RequestResponseConstants.PARTY_ID, jsonObject.get(RequestResponseConstants.PARTY_ID).getAsString());
        successRes.addProperty(RequestResponseConstants.ENABLE_LOB,
                jsonObject.get(RequestResponseConstants.ENABLE_LOB).getAsString());
        postToQueue(successRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private void postToQueue(String message, String queueEndpoint) {
        LOGGER.info("Message sent from Essence is \n" + message);
        LOGGER.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

}
