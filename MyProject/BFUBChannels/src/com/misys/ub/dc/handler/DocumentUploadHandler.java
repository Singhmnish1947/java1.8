/* ********************************************************************************
 *  Copyright(c)2018  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */
package com.misys.ub.dc.handler;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.cbs.party.constants.PartyConstants;
import com.misys.dms.AbstractDocumentManagementSystem;
import com.misys.dms.DocumentManagementSystemImpl;
import com.misys.dms.IDocumentManagementSystem;
import com.misys.dms.attributes.AttachDocInput;
import com.misys.dms.attributes.AttachDocumentRq;
import com.misys.dms.attributes.AttachDocumentRs;
import com.misys.dms.attributes.AuthenticationDetails;
import com.misys.dms.attributes.DeleteDocInput;
import com.misys.dms.attributes.DeleteDocumentRq;
import com.misys.dms.attributes.DeleteDocumentRs;
import com.misys.dms.attributes.RqHeader;
import com.misys.ub.dc.common.ErrorCodes;
import com.misys.ub.dc.common.InstructionStatusUpdateNotification;
import com.misys.ub.dc.common.QueueName;
import com.misys.ub.dc.common.RequestResponseConstants;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

/**
 * This class is used as a handler to upload party related documents to OpenKM
 * 
 * @author Shoaib
 *
 */
public class DocumentUploadHandler {

    private static final Log LOGGER = LogFactory.getLog(DocumentUploadHandler.class.getName());

    /**
     * Method Description: This method is used to upload document to OpenKM
     * 
     * @return Document id if document is uploaded successfully to OpenKM otherwise null value
     */
    public String uploadDocument(JsonObject jsonObject, String branchCode) {

        LOGGER.info("Entering uploadDocument() method");
        String documentId = null;

        try {
            AttachDocumentRq attachDocumentRq = getAttachDocumentRequest(jsonObject, branchCode);
            LOGGER.info("Uploading the document " + attachDocumentRq.getDocumentName() + " to OpenKM ");

            IDocumentManagementSystem dmsImplTest = DocumentManagementSystemImpl.getInstance();
            AttachDocumentRs attachmentDocRs = ((AbstractDocumentManagementSystem) dmsImplTest)
                    .uploadDocumentToDMS(attachDocumentRq);

            if (attachmentDocRs != null) {
                if (attachmentDocRs.getUploadDocumentResult()) {
                    documentId = attachmentDocRs.getDocumentID();
                }
                else {
                    LOGGER.error("Error occured while uploading the document to OpenKM : " + attachmentDocRs.getErrorMessage());
                }
            }
        }
        catch (Exception exception) {
            LOGGER.error(
                    "Exception occurred while uploading the document to OpenKM : " + BatchUtil.getExceptionAsString(exception));
        }

        LOGGER.info("Exiting uploadDocument() method with document id value :" + documentId);

        return documentId;
    }

    /**
     * Method Description: This method is used to delete document from OpenKM
     * 
     * @return void
     */
    public void deleteDocument(String documentId, String branchCode) {

        LOGGER.info("Entering deleteDocument() method with document id value :" + documentId);

        try {
            DeleteDocumentRq deleteDocumentRq = getDeleteDocumentRequest(documentId, branchCode);
            IDocumentManagementSystem dmsImplTest = DocumentManagementSystemImpl.getInstance();
            DeleteDocumentRs deleteDocRs = ((AbstractDocumentManagementSystem) dmsImplTest).deleteDocumentFromDMS(deleteDocumentRq);

            if (deleteDocRs != null) {
                if (deleteDocRs.getDeleteDocOutput() != null && deleteDocRs.getDeleteDocOutput().getDocumentID() != null) {
                    LOGGER.info("Successfully deleted the document from OpenKM with document Id : " + documentId);
                }
                else {
                    LOGGER.info("Error occured while deleting the document from OpenKM with document Id : "
                            + deleteDocRs.getDeleteDocOutput().getDocumentID());
                }
            }

        }
        catch (Exception exception) {
            LOGGER.error("Exception occurred while deleting the document from OpenKM with document Id : " + documentId + "\n"
                    + BatchUtil.getExceptionAsString(exception));
        }

        LOGGER.info("Exiting deleteDocument() method with document id value : " + documentId);
    }

    private AttachDocumentRq getAttachDocumentRequest(JsonObject jsonObject, String branchCode) {
        RqHeader rqHeader = getReqHeader(branchCode);
        AttachDocumentRq attachDocumentRq = new AttachDocumentRq();
        attachDocumentRq.setRqHeader(rqHeader);

        AttachDocInput attachDocInput = new AttachDocInput();
        attachDocInput.setDocumentDtl(new HashMap<String, Object>());
        attachDocumentRq.setAttachDocInput(attachDocInput);

        byte[] decodedDocContent = decodeDocumentContentUsingBase64(jsonObject);
        attachDocumentRq.setFileStream(decodedDocContent);
        attachDocumentRq.setDocumentName(jsonObject.get(RequestResponseConstants.DOCUMENT_NAME).getAsString());

        return attachDocumentRq;
    }

    private DeleteDocumentRq getDeleteDocumentRequest(String documentId, String branchCode) {
        DeleteDocumentRq deleteDocumentRq = new DeleteDocumentRq();

        DeleteDocInput deleteDocInput = new DeleteDocInput();
        deleteDocInput.setDocumentID(documentId);
        deleteDocumentRq.setRqHeader(getReqHeader(branchCode));
        deleteDocumentRq.setDeleteDocInput(deleteDocInput);

        return deleteDocumentRq;
    }

    private byte[] decodeDocumentContentUsingBase64(JsonObject jsonObject) {
        byte[] encodedDocContent = jsonObject.get(RequestResponseConstants.DOCUMENT_CONTENTS).getAsString().getBytes();
        Base64 codec = new Base64();
        return codec.decode(encodedDocContent);
    }

    private RqHeader getReqHeader(String branchCode) {
        RqHeader rqHeader = new RqHeader();
        AuthenticationDetails authenticationDetails = new AuthenticationDetails();
        authenticationDetails.setToken(BankFusionThreadLocal.getUserLocator().getStringRepresentation());
        rqHeader.setauthenticationDetails(authenticationDetails);
        rqHeader.setBranchSortCode(branchCode);
        rqHeader.setApplicationName(PartyConstants.CHANNEL_ID);
        rqHeader.setEntity(BankFusionThreadLocal.getUserZone());
        return rqHeader;
    }

    public void sendFailedResposnse(JsonObject jsonObject) {
        LOGGER.info("Sending failed upload service response");
        JsonObject errorRes = new JsonObject();
        errorRes.addProperty(RequestResponseConstants.STATUS, RequestResponseConstants.STATUS_FAILURE);
        errorRes.addProperty(RequestResponseConstants.MESSAGE_ID,
                jsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString());
        errorRes.addProperty(RequestResponseConstants.MESSAGE_TYPE,
                jsonObject.get(RequestResponseConstants.MESSAGE_TYPE).getAsString());
        errorRes.addProperty(RequestResponseConstants.ORIG_CONTEXT_ID, PartyConstants.CHANNEL_ID);
        errorRes.addProperty(RequestResponseConstants.REASON_CODE, ErrorCodes.DOCUMENT_UPLOAD_FAILURE);
        //postToQueue(errorRes.toString(), QueueName.QM_BFDC_UB_RESPONSE);
        String msgId =  jsonObject.get(RequestResponseConstants.MESSAGE_ID).getAsString();
        String reasonCode =  ErrorCodes.DOCUMENT_UPLOAD_FAILURE;
        try {
        	InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
            response.sendFailOfferResponse(msgId, "E", reasonCode, "", "", "",null);
        }catch (Exception e) {
        	LOGGER.error("Error occoured while parsing message"+errorRes.toString());
        }
        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
    }

    private void postToQueue(String message, String queueEndpoint) {
        LOGGER.info("Message sent from Essence is \n" + message);
        LOGGER.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }
}
