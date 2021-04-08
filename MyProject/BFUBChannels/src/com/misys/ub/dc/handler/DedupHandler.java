package com.misys.ub.dc.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.cbs.party.constants.PartyConstants;
import com.misys.ub.dc.common.ErrorCodes;
import com.misys.ub.dc.common.InstructionStatusUpdateNotification;
import com.misys.ub.dc.common.QueueName;
import com.misys.ub.dc.common.RequestResponseConstants;
import com.misys.ub.dc.validator.PartyValidator;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

/**
 * Handler for part De-dup search on customer on-boarding from DC.
 * 
 * @author sdoddama, ullas
 *
 */
public class DedupHandler {
    private transient final static Log LOGGER = LogFactory.getLog(DedupHandler.class.getName());
    private JsonObject jsonObject;

    public DedupHandler(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public boolean isDuplicateParty() {
        boolean isDuplicateParty = true;
        try {
            PartyValidator validateParty = new PartyValidator();
            JsonObject jsonPartyFieldObj = null;

            if ((jsonObject.get(RequestResponseConstants.PARTY_FIELDS) != null)
                    && (!jsonObject.get(RequestResponseConstants.PARTY_FIELDS).isJsonNull())) {
                jsonPartyFieldObj = jsonObject.get(RequestResponseConstants.PARTY_FIELDS).getAsJsonObject();
                isDuplicateParty = validateParty.validateDeDupDetails(jsonPartyFieldObj);
            }
        }
        catch (Exception exception) {
            LOGGER.debug("Parse exception occured while calling dedup service : " + BatchUtil.getExceptionAsString(exception));
        }

        return isDuplicateParty;
    }

    // TODO : chethan; need to find a better option in handling Error responses
    public void sendFailedDedupRes() {
        LOGGER.info("Sending failed DeDup Response");
        JsonObject errRes = new JsonObject();
        String msgId = jsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString();
        errRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_FAILURE);
        errRes.addProperty(RequestResponseConstants.MESSAGE_ID, msgId);
        errRes.addProperty(RequestResponseConstants.MESSAGE_TYPE, RequestResponseConstants.MESSAGE_TYPE_VALUE);
        errRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, PartyConstants.CHANNEL_ID);
        errRes.addProperty(RequestResponseConstants.REASON_CODE, ErrorCodes.PARTY_DEDUP_MATCH_REJECTED);
        //postToQueue(errRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        try {
        InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
        instr.sendFailOfferResponse(msgId, "E", ErrorCodes.PARTY_DEDUP_MATCH_REJECTED, "", "",
                "",null);
        }
        catch (Exception e) {
			// TODO: handle exception
        	LOGGER.error("");
		}
        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private void postToQueue(String message, String queueEndpoint) {
        LOGGER.info("message sent from Essence is \n" + message);
        LOGGER.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

}
