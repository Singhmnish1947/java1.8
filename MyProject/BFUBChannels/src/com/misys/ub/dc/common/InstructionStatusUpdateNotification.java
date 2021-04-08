package com.misys.ub.dc.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;

import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;

public class InstructionStatusUpdateNotification {
    private transient final static Log logger = LogFactory.getLog(InstructionStatusUpdateNotification.class.getName());
    public static final String CAMEL_RESPONSE_ENDPOINT = "QM_BFDC_UB_Response";
    public static final String IS_FFC_JMS_CONFIGURED = "IS_FFC_JMS_CONFIGURED";

    public void sendFailOfferResponse(String msgId, String status, String errorCode, String errorMessage, String argumentList,
            String partyId, JsonObject routeJsonObject) throws Exception {
        
            prepareOfferResponse(msgId, status, errorCode, errorMessage, argumentList, partyId,routeJsonObject);
       

    }

    public void sendChangePersonalResponse(String msgId, String customerId, String status) throws Exception {
        
            prepareChangePersonalResponse(msgId, customerId, status);
        
    }

    private void prepareChangePersonalResponse(String msgId, String customerId, String status) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder loader = factory.newDocumentBuilder();
        Document document = loader.newDocument();

        String docNS = "http://pegasus/integration/tfw";

        Element order = null;
        if (status == "E") {
            order = document.createElementNS(docNS, "ns:InstructionStatusUpdateNotification");
            document.appendChild(order);
            order.setAttribute("xmlns:ns", docNS);
        }
        else {
            order = document.createElementNS(docNS, "BFO:InstructionStatusUpdateNotification");
            document.appendChild(order);
            order.setAttribute("xmlns:BFO", docNS);
        }

        Element instructionUpdate = document.createElement("InstructionUpdate");
        order.appendChild(instructionUpdate);

        Element txnReferenceID = document.createElement("TxnReferenceID");
        txnReferenceID.appendChild(document.createTextNode(msgId));
        instructionUpdate.appendChild(txnReferenceID);
        if (status == "E") {
            Element newStatus = document.createElement("NewStatus");
            newStatus.appendChild(document.createTextNode("REJECTED"));
            instructionUpdate.appendChild(newStatus);

            Element notificationSequence = document.createElement("NotificationSequence");
            notificationSequence.appendChild(document.createTextNode("0"));
            instructionUpdate.appendChild(notificationSequence);

        }
        else {
            Element newStatus = document.createElement("NewStatus");
            newStatus.appendChild(document.createTextNode("PROCESS_SUCCESSFULLY"));
            instructionUpdate.appendChild(newStatus);

        }

        postToQueue(documentToString(document), "RECIEVEQUEUE");
        logger.info("************ Sending ChangePersonalDetail Response to RECIEVEQUEUE ***********");

    }

    private void prepareOfferResponse(String msgId, String status, String errorCode, String errorMessage, String argumentList,
            String partyId,JsonObject routeJsonObject) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder loader = factory.newDocumentBuilder();
        Document document = loader.newDocument();
        
        
        String docNS = "http://pegasus/integration/tfw";
        Element order = null;
        String docNS1 = "http://pegasus/integration/offer";
        String docNS2 = "http://www.w3.org/2001/XMLSchema-instance";
        String docNS3 = "nste:offerIntegrationReply";
        if (status == "E") {
            order = document.createElementNS(docNS, "ns:InstructionStatusUpdateNotification");
            document.appendChild(order);
            order.setAttribute("xmlns:ns", docNS);
        }
        else {
            order = document.createElementNS(docNS, "BFO:InstructionStatusUpdateNotification");
            document.appendChild(order);
            order.setAttribute("xmlns:BFO", docNS);
        }

        Element instructionUpdate = document.createElement("InstructionUpdate");
        order.appendChild(instructionUpdate);

        Element txnReferenceID = document.createElement("TxnReferenceID");
        txnReferenceID.appendChild(document.createTextNode(msgId));
        instructionUpdate.appendChild(txnReferenceID);

        if (status == "E") {
            Element newStatus = document.createElement("NewStatus");
            newStatus.appendChild(document.createTextNode("REJECTED"));
            instructionUpdate.appendChild(newStatus);

            Element notificationSequence = document.createElement("NotificationSequence");
            notificationSequence.appendChild(document.createTextNode("0"));
            instructionUpdate.appendChild(notificationSequence);

            if (errorCode != "" && errorCode != null) {
                instructionUpdate.appendChild(getReason(document, errorCode, errorMessage, argumentList));
            }
            
        }
        else {
            Element newStatus = document.createElement("NewStatus");
            newStatus.appendChild(document.createTextNode("PROCESS_SUCCESSFULLY"));
            instructionUpdate.appendChild(newStatus);

            Element reply = document.createElement("Reply");
            instructionUpdate.appendChild(reply);
            reply.setAttribute("xmlns:nste", docNS1);
            reply.setAttribute("xmlns:xsi", docNS2);
            reply.setAttribute("xsi:type", docNS3);

            Element createProducts = document.createElement("CreatedProducts");
            reply.appendChild(createProducts);

            Element termDepositid = document.createElement("ProductId");
            termDepositid.appendChild(document.createTextNode(partyId));
            createProducts.appendChild(termDepositid);

            Element orginalAmount = document.createElement("Type");
            orginalAmount.appendChild(document.createTextNode("CUSTOMER"));
            createProducts.appendChild(orginalAmount);
            
            if(routeJsonObject!=null)
            {
            	if (routeJsonObject.get(RequestResponseConstants.DOCUMENTS) != null
                        && routeJsonObject.get(RequestResponseConstants.DOCUMENTS).getAsJsonObject() != null) {
           		 Element backenedDoc = document.createElement("BackendDocuments");
           		 
           		 JsonObject obj = routeJsonObject.get(RequestResponseConstants.DOCUMENTS).getAsJsonObject();
           		JsonArray elem = obj.get("REPEATABLE_BLOCKS").getAsJsonArray();
           		for(int i=0;i<elem.size();i++)
           		{
           			Gson gson = new Gson();
           			JsonObject obj1 = elem.get(i).getAsJsonObject();
           			String	imageId = obj1.get("PT_PFN_PartyDocumentData#IMAGEID").getAsString();
           			String docName = obj1.get("DocumentName").getAsString();
           			docName = docName.substring(0,docName.indexOf("#"));
           			Element docDesc = document.createElement("DocumentDesc");
           			docDesc.appendChild(document.createTextNode(docName));
           			backenedDoc.appendChild(docDesc);
           			Element docId = document.createElement("DocumentId");
           			docId.appendChild(document.createTextNode(imageId));
           			backenedDoc.appendChild(docId);         		 
           		}
           		reply.appendChild(backenedDoc);
           	 }
            }
        }
        logger.info("************ Sending Offer Response to RECIEVEQUEUE ***********");
        postToQueue(documentToString(document), "RECIEVEQUEUE");

    }

    public String sendResponse(String message) throws Exception {
        
        	String defaultEndPoint = "RECIEVEQUEUE";
            message = prepareResponse(message);
       
        postToQueue(message, defaultEndPoint);
        logger.info("************ Send to Queue  ***********" + defaultEndPoint);

        return message;
    }


    private String prepareResponse(String message) throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputSource inputsource = new InputSource(new StringReader(message));
        Document doc = dBuilder.parse(inputsource);
        doc.getDocumentElement().normalize();

        NodeList instructionUpdateList = getCurrentList(doc, "ns5:instructionUpdateItem");
        NodeList transactionEventList = getCurrentList(doc, "ns5:transactionEvent");

        int instructlength = instructionUpdateList.getLength();
        String transItem = "";
        String status = "";
        String notifySeq = "";
        for (int i = 0; i < instructlength; i++) {
            Element el = (Element) instructionUpdateList.item(i);
            if (el.getNodeName().contains("ns5:instructionUpdateItem")) {
                transItem = el.getElementsByTagName("ns5:transactionalItem").item(0).getTextContent();
                status = el.getElementsByTagName("ns5:newStatus").item(0).getTextContent();
                notifySeq = el.getElementsByTagName("ns5:notificationSequence").item(0).getTextContent();
            }
        }

        String reasonCode = "";
        String argumentList = "";
        String defaultmessage = "";
        if (transactionEventList.getLength() >= 0) {

            Element el = (Element) transactionEventList.item(0);
            if (el.getNodeName().contains("ns5:transactionEvent")) {
                reasonCode = el.getElementsByTagName("ns5:reasonCode").item(0).getTextContent();
                argumentList = el.getElementsByTagName("ns5:eventParameters").item(0).getTextContent();
                defaultmessage = el.getElementsByTagName("ns5:defaultMessage").item(0).getTextContent();
                // defaultMessage.replaceAll(/\{\d\}/, "");
            }

        }
        int errorlength = transactionEventList.getLength();

        if (status.equalsIgnoreCase("PROCESS_SUCCESSFULLY") || status.equalsIgnoreCase("PROCESSED")) {
            status = "PROCESS_SUCCESSFULLY";
        }
        else {
            status = "REJECTED";
        }
        if (notifySeq == "" || notifySeq == null)
            notifySeq = "0";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder loader = factory.newDocumentBuilder();
        Document document = loader.newDocument();

        String docNS = "http://pegasus/integration/tfw";

        Element order = document.createElementNS(docNS, "tfw:InstructionStatusUpdateNotification");
        document.appendChild(order);
        order.setAttribute("xmlns:tfw", docNS);

        Element instructionUpdate = document.createElement("InstructionUpdate");
        order.appendChild(instructionUpdate);

        Element txnReferenceID = document.createElement("TxnReferenceID");
        txnReferenceID.appendChild(document.createTextNode(transItem));
        instructionUpdate.appendChild(txnReferenceID);

        Element newStatus = document.createElement("NewStatus");
        newStatus.appendChild(document.createTextNode(status));
        instructionUpdate.appendChild(newStatus);

        Element notificationSequence = document.createElement("NotificationSequence");
        notificationSequence.appendChild(document.createTextNode(notifySeq));
        instructionUpdate.appendChild(notificationSequence);

        if (reasonCode != "" && reasonCode != null) {
            instructionUpdate.appendChild(getReason(document, reasonCode, defaultmessage, argumentList));
        }

        return documentToString(document);
    }

    private String documentToString(Document document) {
        try {
        	TransformerFactory factory = TransformerFactory.newInstance();
        	factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        	Transformer transformer = factory.newTransformer();
            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString();
        }
        catch (TransformerException tEx) {
            tEx.printStackTrace();
        }
        return null;
    }

    private NodeList getCurrentList(Document doc, String tag) {
        NodeList nList = doc.getElementsByTagName(tag);

        return nList;
    }

    private String getNodeValue(Node node, String nodeName) {

        NodeList list = node.getChildNodes();
        String nodeValue = null;

        for (int i = 0; i < list.getLength(); i++) {

            if (list.item(i).getChildNodes().getLength() > 1) {

                nodeValue = getNodeValue(list.item(i), nodeName);

                if (nodeValue != null) {
                    return nodeValue;
                }

            }
            else {

                if ((list.item(i).getNodeName().equals(nodeName)) && (list.item(i).getFirstChild() != null)) {

                    nodeValue = list.item(i).getFirstChild().getTextContent();
                }
            }
        }
        return nodeValue;
    }

    private Node getReason(Document document, String reasonCode, String defaultmessage, String argumentList) {
        Element reason = document.createElement("Reason");

        // set id attribute
        reason.setAttribute("IssueCode", reasonCode);

        // create DefaultMessage element
        reason.appendChild(getReasonElements(document, reason, "DefaultMessage", defaultmessage));

        // create Argument element
        reason.appendChild(getReasonElements(document, reason, "Argument", argumentList));

        return reason;
    }

    private Node getReasonElements(Document document, Element element, String name, String value) {
        Element node = document.createElement(name);
        node.appendChild(document.createTextNode(value));
        return node;
    }


    public static boolean getModuleConfigurationValue(String paramName, String moduleName, BankFusionEnvironment env) {
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        Object value = bizInfo.getModuleConfigurationValue(moduleName, paramName, env);
        if (value != null)
            return (boolean) value;
        else return false;

    }

    public void postResponseToQueue(Object obj, String objType) {
        ComplexTypeConvertor converter = new ComplexTypeConvertor();
        String responseToDC = converter.getXmlFromJava(objType, obj);
        postToQueue(responseToDC, CAMEL_RESPONSE_ENDPOINT);
    }

    private void postToQueue(String message, String queueEndpoint) {
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

}