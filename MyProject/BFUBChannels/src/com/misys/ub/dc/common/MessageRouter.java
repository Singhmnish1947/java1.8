package com.misys.ub.dc.common;

import java.util.HashMap;
import java.util.Map;
import com.misys.ub.dc.common.InstructionStatusUpdateNotification;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.constant.ESBConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.dc.handler.helper.UpdateFatcaDetailsHelper;
import com.misys.ub.dc.restServices.InterbankSOController;
import com.misys.ub.dc.types.ChequeBookOrderCreateRq;
import com.misys.ub.dc.types.InterbankSOCRq;
import com.misys.ub.dc.types.InterbankSORq;
import com.misys.ub.dc.types.InterbankSORs;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.types.dc.ChqBookCreateRes;
import bf.com.misys.cbs.types.dc.ChqBookCreateRs;
import bf.com.misys.cbs.types.dc.ChqBookReq;
import bf.com.misys.cbs.types.dc.ChqBookRq;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class MessageRouter {

    private String incMsg;

    private String eventcode;

    private String eventMsg;
    
    private Object[] eventParameters;

    private String decision;

    private boolean isTxnStarted;

    private transient final static Log logger = LogFactory.getLog(MessageRouter.class.getName());

    public void routeMessage(Exchange exchange) {
        isTxnStarted = false;
        Message response = exchange.getIn();
        eventParameters = null;
        
        logger.info("routeMessage Current Thread running:"+Thread.currentThread().getId());
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        logger.info("routeMessage Current Threads stackTrace");
		for(StackTraceElement st :stackTraceElements) {
			logger.info(st.toString()+Thread.currentThread().getId());
		}
        if (exchange.getProperty(ESBConstants.MAINTAIN_TXN) != null) {
            isTxnStarted = (boolean) exchange.getProperty(ESBConstants.MAINTAIN_TXN);
        }
        logger.info("routeMessage istxnstarted:"+isTxnStarted);
        incMsg = (String) response.getBody();
        if (incMsg != null) {
            logger.info("Message sent for routing" + incMsg);
            JsonObject jsonObject = getJSON();
            if (jsonObject != null) {
                routeJSON(jsonObject);
            }
            else {
                logger.error("Error in creating Json");
            }
        }
        else {
            logger.error("Error in receiving message");
        }

    }

    private JsonObject getJSON() {
        JsonParser parser = new JsonParser();
        try {
            return parser.parse(incMsg).getAsJsonObject();
        }
        catch (JsonParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void routeJSON(JsonObject jsonObject) {
        MessageRouterMethods msgRouterMethods = new MessageRouterMethods();
        isTxnStarted = true;
        logger.info("\n================================================================" + "\n\nEntered message Router \n\n"
                + "msgID => " + jsonObject.get("msgId").getAsString() + "\n" + "msgType => "
                + jsonObject.get("msgType").getAsString() + "\n\n"
                + "================================================================");

        String tag = "msgType";
        try {
            JsonElement element = jsonObject.get(tag);
            if (element != null) {
                decision = element.getAsString();
                if (decision != null) {
                    switch (decision) {
                        case "IBSOC":
                            createSEPASO(jsonObject);
                            break;

                        case "IBSSOC":
                            cancelSEPASOC(jsonObject);
                            break;

                        case "CBOC":
                            createChequeBookOrder(jsonObject);
                            break;

                        case "TerminateTermDepositRequest":
                            msgRouterMethods.terminateTermDeposit(jsonObject, isTxnStarted);
                            break;

                        case "PushPasswordRequest":
                            msgRouterMethods.pushPasswordRequest(jsonObject);
                            break;

                        case "SEPAPMT":
                            msgRouterMethods.initiateSepaPayment(jsonObject, isTxnStarted);
                            break;
                        case "PARTY_ONBOARD_ACCOUNT_OPEN":
                            if (jsonObject.get(RequestResponseConstants.USER_TYPE) != null) {
                                if (RequestResponseConstants.USER_TYPE_CONTRACT
                                        .equalsIgnoreCase(jsonObject.get(RequestResponseConstants.USER_TYPE).getAsString())) {
                                    msgRouterMethods.updatePartyLOB(jsonObject, isTxnStarted);
                                }
                            else if (RequestResponseConstants.EXISTING_CUSTOMER
                                .equalsIgnoreCase(jsonObject.get(RequestResponseConstants.USER_TYPE).getAsString())
                                && RequestResponseConstants.TERM_DEPOSIT_PRODUCT_GROUP
                                    .equalsIgnoreCase(jsonObject.get(RequestResponseConstants.PRODUCT_GROUP).getAsString())) {
                                /*
                                 * UpdateFatcaDetailsHelper fatcaHelper = new UpdateFatcaDetailsHelper(jsonObject);
                                 * fatcaHelper.amendFatcaDetails(); if (!fatcaHelper.isPartyUpdated()) {
                                 * fatcaHelper.sendFailedPartyCreationRs(); return; } if(!msgRouterMethods.createTermDeposit(jsonObject,
                                 * isTxnStarted)) { fatcaHelper.getRqPayload().setRqParam(fatcaHelper.getFatcaRqParamRollback().toString());
                                 * fatcaHelper.amendFatcaDetails(); if (!fatcaHelper.isPartyUpdated()) {
                                 * logger.error("Fatca details rollback failed !!!"); fatcaHelper.sendFailedPartyCreationRs(); return; } }
                                 */
                                UpdateFatcaDetailsHelper fatcaHelper = new UpdateFatcaDetailsHelper(jsonObject);
                                if (!msgRouterMethods.createTermDeposit(jsonObject, isTxnStarted)) {

                                    fatcaHelper.sendFailedPartyCreationRs();
                                    return;
                                }
                                fatcaHelper.amendFatcaDetails();
                                if (!fatcaHelper.isPartyUpdated()) {
                                    fatcaHelper.sendFailedPartyCreationRs();
                                    return;
                                }

                            }
                                else if (RequestResponseConstants.EXISTING_CUSTOMER
                                        .equalsIgnoreCase(jsonObject.get(RequestResponseConstants.USER_TYPE).getAsString())
                                        && (RequestResponseConstants.CA_PRODUCT_GROUP
                                        .equalsIgnoreCase(jsonObject.get(RequestResponseConstants.PRODUCT_GROUP).getAsString())
                                            || RequestResponseConstants.SA_PRODUCT_GROUP
                                            .equalsIgnoreCase(jsonObject.get(RequestResponseConstants.PRODUCT_GROUP).getAsString()))) {
                                    UpdateFatcaDetailsHelper fatcaHelper = new UpdateFatcaDetailsHelper(jsonObject);
                                    if(!msgRouterMethods.createCasa(jsonObject, isTxnStarted)) {
                                            fatcaHelper.sendFailedPartyCreationRs();                                            
                                            return;
                                    }
                                    fatcaHelper.amendFatcaDetails();
                                    if (!fatcaHelper.isPartyUpdated()) {
                                        fatcaHelper.sendFailedPartyCreationRs();
                                        return;
                                    }
                                }
                                else {
                                    msgRouterMethods.createPartyOpenAccount(jsonObject, isTxnStarted);
                                }
                            }

                            break;

                        case "CUSTOMER_CHECK_ACCOUNT_OPEN":
                            msgRouterMethods.checkCustomerAndOpenAccount(jsonObject, isTxnStarted);
                            break;

                        case "STANDALONE_ACCOUNT_OPEN":
                            msgRouterMethods.openAccount(jsonObject, isTxnStarted);
                            break;

                        case "CHANGE_PERSONAL_DETAILS":
                            msgRouterMethods.changePersonalPartyDtls(jsonObject, isTxnStarted);
                            break;
                        default:
                            logger.error("\n\nIncorrect message received\n\n");
                            break;

                    }
                }
            }
            else {
                logger.error("Incorrect message received");
            }
        }
        catch (Exception e) {
            logger.error("\n================================================================"
                    + "\n\nException while executing the process in message Router \n\n" + "msgID => "
                    + jsonObject.get("msgId").getAsString() + "\n" + "msgType => " + jsonObject.get("msgType").getAsString()
                    + "\n\n" + "================================================================");
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            logger.info("\n================================================================" + "\n\nMessage Router Exited\n\n"
                    + "msgID => " + jsonObject.get("msgId").getAsString() + "\n" + "msgType => "
                    + jsonObject.get("msgType").getAsString() + "\n\n"
                    + "================================================================");
        }

    }

        private void createSEPASO(JsonObject jsonObject) {

        Gson gson = new Gson();
        InterbankSOController sepaSO = new InterbankSOController();
        InterbankSORs soResponse = null;
        String soreference = "";
        String responseMsg = "";

        InterbankSORq soCreateRq = gson.fromJson(jsonObject, InterbankSORq.class);
        String msgId = soCreateRq.getMsgId();
        String status = "REJECTED";
        eventcode = "40000127";
        eventParameters = new Object[] {soCreateRq.getDebtorCustRef()};
        eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt("40000127"), eventParameters);
        if (("").equals(jsonObject.get("numPayments").getAsString().trim()) && ("").equals(jsonObject.get("toDt").getAsString().trim())) {
        	 responseMsg = getDCResponseMsg(msgId, soreference, status);
        	 try {
 				InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
 				instr.sendResponse(responseMsg);
 			} catch (Exception e) {
 				logger.error(ExceptionUtil.getExceptionAsString(e));
 			}
        	                        
        							}
									else
									{
        try {
            soResponse = sepaSO.post(soCreateRq);
            if (soResponse != null) {

                if ("S".equalsIgnoreCase(soResponse.getStatus())) {
                    status = "PROCESS_SUCCESSFULLY";
                    soreference = soResponse.getSoId();
                }
                logger.info("Response from service is" + new Gson().toJson(soResponse));
            }
        } catch (BankFusionException e) {
            if (e.getMessageNumber() != 0) {
                eventcode = String.valueOf(e.getMessageNumber());
                eventMsg = e.getMessage();
                eventParameters = e.getArguments();
            } else {
                logger.error("Bankfusion Exception  error has occured");
            }
            logger.error(ExceptionUtil.getExceptionAsString(e));
        } catch (Exception e) {
        	logger.error(ExceptionUtil.getExceptionAsString(e));
        } finally {
            /*
             * Status is set to PROCESS_SUCCESSFULLY if response message status is S in rest of the
             * cases status is set to REJECTED
             */
            responseMsg = getDCResponseMsg(msgId, soreference, status);
            try {
				InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
				instr.sendResponse(responseMsg);
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
        }
		}
    }

    private void cancelSEPASOC(JsonObject jsonObject) {
        Gson gson = new Gson();
        InternationalSEPASOCancelRouting cancelRouting = new InternationalSEPASOCancelRouting();
        String soreference = "";
        String responseMsg = "";

        InterbankSOCRq soCreateRq = gson.fromJson(jsonObject, InterbankSOCRq.class);
        String standingorderid = soCreateRq.getStandingOrderId();
        String transactionRefID = soCreateRq.getTxnReferenceID();
        String customerID = soCreateRq.getCustomerId();
        String paymentSOID = customerID + standingorderid;
        soCreateRq.setInterbankPaymentId(paymentSOID);
        String status = "REJECTED";
        eventcode = "40000127";
        String soid = "";
        eventParameters = new Object[] {standingorderid};
        eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt("40000127"), eventParameters);

        try {
            logger.info("\nEssence Retrive details from DC and merge for InterbankPaymentId  ");
            String interbankSOCRs = cancelRouting.cancelRequest(soCreateRq);
            if (interbankSOCRs != null) {

                String resArray[] = interbankSOCRs.split(";");
                String keyValPair[] = null;
                for (int i = 0; i < resArray.length; i++) {
                    keyValPair = resArray[i].split("=");
                    if ("Status".equalsIgnoreCase(keyValPair[0])) {
                        status = keyValPair[1];
                    }
                    if ("InterbankPaymentId".equalsIgnoreCase(keyValPair[0])) {
                        soid = keyValPair[1];
                        break;
                    }
                }

                if (status.equalsIgnoreCase("S")) {
                    status = "PROCESS_SUCCESSFULLY";
                    soreference = soid;
                }
                logger.info("Response from service is" + new Gson().toJson(interbankSOCRs));
            }

        }

        catch (BankFusionException e) {
            if (e.getMessageNumber() != 0) {
                eventcode = String.valueOf(e.getMessageNumber());
                eventMsg = e.getMessage();
            }
            else {
                logger.error("Bankfusion Exception  error has occured");
            }
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        catch (Exception e) {
        	logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            /*
             * Status is set to PROCESS_SUCCESSFULLY if response message status is S in rest of the
             * cases status is set to REJECTED
             */
            responseMsg = getDCResponseMsg(transactionRefID, soreference, status);
            try {
				InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
				instr.sendResponse(responseMsg);
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
        }
    }

    private void createChequeBookOrder(JsonObject jsonObject) {
        logger.info("\n createChequeBookOrder details from DC ");
        Gson gson = new Gson();
        ChequeBookOrderCreateRq cbr = new ChequeBookOrderCreateRq();
        cbr = gson.fromJson(jsonObject, ChequeBookOrderCreateRq.class);

        logger.info("\n cbr Request from interfaceReceiveQ " + cbr);
        logger.info("\n getCustomerID from interfaceReceiveQ " + cbr.getCustomerId());
        logger.info("\n getAccountNum from interfaceReceiveQ " + cbr.getAccount());
        logger.info("\n getChannelId from interfaceReceiveQ " + cbr.getChannelId());
        logger.info("\n getChequeBookType from interfaceReceiveQ " + cbr.getChequeBookType());
        logger.info("\n getNumberOfLeaves from interfaceReceiveQ " + cbr.getNumberOfLeaves());
        int numOfLeaves = Integer.parseInt(cbr.getNumberOfLeaves());
        Boolean collectAtBranch = Boolean.parseBoolean(cbr.getCollectAtBranch());

        ChqBookReq chkBookReq = new ChqBookReq();
        chkBookReq.setAccountID(cbr.getAccount());
        chkBookReq.setCustomerId(cbr.getCustomerId());
        chkBookReq.setChequeBookType(cbr.getChequeBookType());
        chkBookReq.setNumberOfLeaves(numOfLeaves);
        chkBookReq.setCollectAtBranch(collectAtBranch);
        chkBookReq.setTransactionId(jsonObject.get("msgId").getAsString());

        RqHeader rqHeader = new RqHeader();
        Orig orig = new Orig();
        orig.setChannelId(cbr.getChannelId());
        rqHeader.setOrig(orig);

        ChqBookRq chqBookrq = new ChqBookRq();
        chqBookrq.setRqHeader(rqHeader);
        chqBookrq.setChqBookReq(chkBookReq);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ChqBookRq", chqBookrq);

        // TODO proper data to be sent /need to check with Apoorva
        String transactionRefID = jsonObject.get("msgId").getAsString();
        String soreference = "";
        String status = "REJECTED";
        String responseMsg = "";
        eventcode = "40000127";
        eventParameters = new Object[] {cbr.getAccount()};
        eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt("40000127"), eventParameters);
        try {

            Map outputMap = MFExecuter.executeMF("CB_IBI_ChequeBookCreate_SRV", BankFusionThreadLocal.getBankFusionEnvironment(),
                    params);
            logger.info("OutputMap is " + outputMap.toString());
            ChqBookCreateRs rs = (ChqBookCreateRs) outputMap.get("chqBookCreateRs");
            ChqBookCreateRes res = rs.getChqBookCreateRes();
            logger.info("The order status response is " + res.getOrderStatus());
            status = res.getOrderStatus();
            String codes = rs.getRsHeader().getStatus().getCodes().toString();

            SubCode subCode = rs.getRsHeader().getStatus().getCodes(0);
            logger.info("The codes is " + codes);

            logger.info("The indCode is " + subCode);

            logger.info("The getStatus message is " + rs.getRsHeader().getStatus());
            eventcode = subCode.getCode();

            BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

            soreference = "soreference";

            if (status.equalsIgnoreCase("ordered")) {
                status = "PROCESS_SUCCESSFULLY";

            }
            else {
                if (status == null || status.trim().length() == 0) {
                    status = "REJECTED";
                }
                eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventcode), new String[] {});
            }

            // logger.info("The response from Create Chequebook Order " + new Gson().toJson(cbr));

        }
        catch (BankFusionException e) {
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

            e.printStackTrace();

        }
        catch (Exception e) {
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

            logger.info("CBOC error");
            e.printStackTrace();

        }
        finally {
            /*
             * Status is set to PROCESS_SUCCESSFULLY if response message status is S in rest of the
             * cases status is set to REJECTED
             */
            responseMsg = getDCResponseMsg(transactionRefID, soreference, status);
            try {
				InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
				instr.sendResponse(responseMsg);
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
        
        }

    }

    public String getDCResponseMsg(String transactionalItem, String reference, String status) {
        StringBuilder responseMsgBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("\n<cbsmsg:transferResponse xmlns:cbsmsg=\"http://www.misys.com/cbs/msgs/v1r0\">")
                .append("\n   <cbsmsg:rsHeader>")
                .append("\n          <ns1:version xmlns:ns1=\"http://www.misys.com/cbs/types/header\"/>")
                .append("\n          <ns2:messageType xmlns:ns2=\"http://www.misys.com/cbs/types/header\"/>")
                .append("\n          <ns3:origCtxtId xmlns:ns3=\"http://www.misys.com/cbs/types/header\">IBI</ns3:origCtxtId>")
                .append("\n          <ns4:status xmlns:ns4=\"http://www.misys.com/cbs/types/header\">")
                .append("\n                 <ns4:overallStatus/>").append("\n                 <ns4:codes>")
                .append("\n                       <ns4:code/>").append("\n                       <ns4:fieldName> </ns4:fieldName>")
                .append("\n                       <ns4:severity/>").append("\n                       <ns4:description/>")
                .append("\n                       <ns4:parameters>").append("\n                       <ns4:eventParameterValue/>")
                .append("\n                       </ns4:parameters>").append("\n                 </ns4:codes>")
                .append("\n                 <ns4:subStatus/>").append("\n          </ns4:status>").append("\n   </cbsmsg:rsHeader>")
                .append("\n   <cbsmsg:instructionStatusUpdateNotification>")
                .append("\n          <ns5:instructionUpdateItem xmlns:ns5=\"http://www.misys.com/cbs/types\">")
                .append("\n                 <ns5:transactionalItem>TRANSACTIONAL_ITEM_REPLACE_STR</ns5:transactionalItem>")
                .append("\n                 <ns5:soReference>SOREF_REPLACE_STR</ns5:soReference>")
                .append("\n                 <ns5:newStatus>MSG_STATUS_REPLACE_STR</ns5:newStatus>")
                .append("\n                 <ns5:notificationSequence>0</ns5:notificationSequence>")
                .append("\n                 <ns5:transactionEvent>")
                .append("\n                       <ns5:reasonCode>REASON_CODE_REPLACE_STR</ns5:reasonCode>")
                .append("\n                        <ns5:defaultMessage>REASON_MESSAGE_REPLACE_STR</ns5:defaultMessage>")
                .append("\n                        <ns5:formattedMessage>REASON_MESSAGE_REPLACE_STR</ns5:formattedMessage>")
                .append("\n                       <ns5:eventParameters/>").append("\n                 </ns5:transactionEvent>")
                .append("\n                 <userExtension xmlns:java=\"http://java.sun.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"java:java.lang.String\"/>")
                .append("\n                 <hostExtension xmlns:java=\"http://java.sun.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"java:java.lang.String\"/>")
                .append("\n          </ns5:instructionUpdateItem>").append("\n   </cbsmsg:instructionStatusUpdateNotification>")
                .append("\n</cbsmsg:transferResponse>");

        String responseMsg = responseMsgBuilder.toString();
        if ("PROCESS_SUCCESSFULLY".equalsIgnoreCase(status)) {
            responseMsg = responseMsg.replace("TRANSACTIONAL_ITEM_REPLACE_STR", transactionalItem);
            responseMsg = responseMsg.replace("SOREF_REPLACE_STR", reference);
            responseMsg = responseMsg.replace("MSG_STATUS_REPLACE_STR", status);
            responseMsg = responseMsg.replace("REASON_CODE_REPLACE_STR", "");
            responseMsg = responseMsg.replace("REASON_MESSAGE_REPLACE_STR", "");
        } else {
            responseMsg = responseMsg.replace("TRANSACTIONAL_ITEM_REPLACE_STR", transactionalItem);
            responseMsg = responseMsg.replace("SOREF_REPLACE_STR", "");
            responseMsg = responseMsg.replace("MSG_STATUS_REPLACE_STR", status);
            responseMsg = responseMsg.replace("REASON_CODE_REPLACE_STR", eventcode);
            responseMsg = responseMsg.replace("REASON_MESSAGE_REPLACE_STR", eventMsg);
            if(eventParameters != null && eventParameters.length > 0) {
            	StringBuilder builder = new StringBuilder();
            	for(Object parameter : eventParameters) {
            		builder.append("<ns5:eventParameters>")
            				.append(String.valueOf(parameter))
            				.append("</ns5:eventParameters>");
            	}
            	responseMsg = responseMsg.replace("<ns5:eventParameters/>", builder.toString());
            }
            logger.warn("CorrelationId: "+BankFusionThreadLocal.getCorrelationID());
            logger.error("----------------------Error in completing the transaction-----------------");
        }

        return responseMsg;
    }

    public String getDCResponseMsg(String transactionalItem, String reference, String status, String reasonCode,
            String defaultMsg) {
        if (!"PROCESS_SUCCESSFULLY".equalsIgnoreCase(status)) {
            this.eventcode = reasonCode;
            this.eventMsg = defaultMsg;
        }
        return getDCResponseMsg(transactionalItem, reference, status);

    }

}
