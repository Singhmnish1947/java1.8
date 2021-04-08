package com.misys.ub.swift;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

/**
 * @author Rishav
 * 
 *         file to transform MT110
 */

public class UB_MT110_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT110_FileCreator.class.getName());

    private String strResult;
    private String FinalXml;
    public String Temp_context;
    public String Temp_name;

    public String MT110_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            ClassCastException, SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(requestMsg));
            Document document = dBuilder.parse(is);

            document.getDocumentElement().normalize();
            // update Element value
            updateElementValue(document);

            // write the updated document to forward request
            document.getDocumentElement().normalize();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

            strResult = writer.toString();
        }
        catch (SAXException | ParserConfigurationException | IOException | TransformerException e) {
            e.printStackTrace();
        }

        String[] patterns = new String[] { "<(\\w+)>\\s*</\\1>|<\\w+/>" };
        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(strResult);
            FinalXml = matcher.replaceAll("");
        }
        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(FinalXml);
            FinalXml = matcher.replaceAll("");
        }

        XmlFormatter obj = new XmlFormatter();
        FinalXml = obj.format(FinalXml);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(FinalXml);
        }
        return FinalXml;

    }

    public void updateElementValue(Document document) {
        Node rootNode = document.getFirstChild();
        document.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        rootElement.setAttribute("MessageType", "SWIFT_MT110");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");
        Properties swiftProperties = new Properties();
        String configLocation = GetUBConfigLocation.getUBConfigLocation();
        try {
            InputStream inputStream = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    FromEssence_To_UBMMM.CONF + FromEssence_To_UBMMM.SWIFT_PROPERTY_FILENAME, configLocation,
                    BankFusionThreadLocal.getUserZone());
            swiftProperties.load(inputStream);
        }
        catch (IOException ex) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_SWIFT_PROPERTIES_FILE_NOT_FOUND, new Object[] {}, new HashMap(),
                    BankFusionThreadLocal.getBankFusionEnvironment());
        }

        // tag : MeridianMessageType
        Element meridianMessagetype = document.createElement("MeridianMessageType");
        meridianMessagetype.setTextContent("SWIFT_MT110");
        rootNode.appendChild(meridianMessagetype);

        // tag : InternalMessageType
        Element internalMessageType = document.createElement("InternalMessageType");
        internalMessageType.setTextContent("A");
        rootNode.appendChild(internalMessageType);

        // tag : HostType
        Element hostType = document.createElement("HostType");
        hostType.setTextContent("UB");
        rootNode.appendChild(hostType);

        // tag : HostID
        Element hostID = document.createElement("HostID");
        hostID.setTextContent(swiftProperties.getProperty("HostId"));
        rootNode.appendChild(hostID);

        // tag : TransactionReference
        Node senderReference = document.getElementsByTagName("SenderReference").item(0);
        if (senderReference != null) {
            Element transactionReferenceNew = document.createElement("TransactionReference");
            transactionReferenceNew.appendChild(document.createTextNode(senderReference.getTextContent()));
            rootNode.appendChild(transactionReferenceNew);
        }

        // tag : Priority
        Element priority = document.createElement("Priority");
        priority.setTextContent("N");
        rootNode.appendChild(priority);

        // tag : Cancel
        Element cancel = document.createElement("Cancel");
        cancel.setTextContent("N");
        rootNode.appendChild(cancel);

        // tag : SystemArrivalTime
        Element systemArrivalTime = document.createElement("SystemArrivalTime");
        SimpleDateFormat systemArrivalTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        systemArrivalTime.setTextContent(systemArrivalTimeFormat.format(date));
        rootNode.appendChild(systemArrivalTime);

        // tag : ValueDate
        // valueDate = document.createElement("ValueDate");
        // SimpleDateFormat valueDateFormat = new SimpleDateFormat("yyyy-mm-dd");
        // valueDate.setTextContent(SWT_Outgoing_Globals.formatDate(valueDateFormat.format(date)));
        // rootNode.appendChild(valueDate);

        String DateOfIssueAsValueDate = document.getElementsByTagName("DateOfIssue").item(0).getTextContent();
        Element valueDate = document.createElement("ValueDate");
        SimpleDateFormat valueDateFormat = new SimpleDateFormat("yyyy-mm-dd");
        try {
            Date newDATE = valueDateFormat.parse(DateOfIssueAsValueDate);
            valueDate.setTextContent(SWT_Outgoing_Globals.formatDate(valueDateFormat.format(newDATE)));
            rootNode.appendChild(valueDate);
        }
        catch (ParseException exception) {
            exception.printStackTrace();
        }

        // tag : Network
        Element network = document.createElement("Network");
        network.setTextContent("SWIFT");
        rootNode.appendChild(network);

        // tag : SenderAddress
        Node senderAddress = document.getElementsByTagName("SenderAddress").item(0);
        if (senderAddress != null) {
            senderAddress.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(senderAddress.getTextContent()));
        }

        // tag : LineOfBusiness
        Element lineOfBusinessNew = document.createElement("LineOfBusiness");
        lineOfBusinessNew.setTextContent("UB");
        rootNode.appendChild(lineOfBusinessNew);

        // tag : MultipleMessageStatus
        Element multipleMessageStatusNew = document.createElement("MultipleMessageStatus");
        multipleMessageStatusNew.setTextContent("I");
        rootNode.appendChild(multipleMessageStatusNew);

        // tags : InternalReference, MessageID, Direction
        Node internalRef = document.getElementsByTagName("internalRef").item(0);
        if (internalRef != null) {
            Element direction = document.createElement("Direction");
            if (!internalRef.getTextContent().equals("")) {
                direction.setTextContent("I");
                document.renameNode(internalRef, null, "InternalReference");
                Node messageId = document.getElementsByTagName("messageId").item(0);
                double messageIDValue = Double.parseDouble(messageId.getTextContent());
                if (messageIDValue != 0.0) {
                    document.renameNode(messageId, null, "MessageID");
                }
            }
            else {
                direction.setTextContent("O");
            }
            rootNode.appendChild(direction);
        }

        // tag : PaymentPriority
        Properties properties = new Properties();
        try {
            FileInputStream fIS = new FileInputStream(configLocation + "conf//swift//" + "SWTCommonStub.properties");
            properties.load(fIS);
            Element paymentPriority = document.createElement("PaymentPriority");
            paymentPriority.setTextContent(properties.getProperty("PaymentPriority"));
            rootNode.appendChild(paymentPriority);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * Element paymentPriority = document.createElement("PaymentPriority");
         * paymentPriority.setTextContent("999"); rootNode.appendChild(paymentPriority);
         */
        // tags : B2IDeliveryMonitoring & B2IObsolescencePeriod
        Node priorityTemp = document.getElementsByTagName("Priority").item(0);
        if (priorityTemp.getTextContent().equals("N")) {
            Element b2IDeliveryMonitoringNew = document.createElement("B2IDeliveryMonitoring");
            Element b2IObsolescencePeriod = document.createElement("B2IObsolescencePeriod");
            if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                b2IDeliveryMonitoringNew.setTextContent(swiftProperties.getProperty("B2IDeliveryMonitoring"));
                b2IObsolescencePeriod.setTextContent("020");
            }
            rootNode.appendChild(b2IDeliveryMonitoringNew);
            rootNode.appendChild(b2IObsolescencePeriod);
        }

        // tag : SenderReference

        // tag : SenderCorrespondentA/B/D
        Node sendersCorrespOption = document.getElementsByTagName("sendersCorrespOption").item(0);
        Node sendersCorrespondent = document.getElementsByTagName("sendersCorrespondent").item(0);

        if (sendersCorrespondent != null && sendersCorrespOption != null) {
            String sendersCorrespOptionValue = sendersCorrespOption.getTextContent();
            if (sendersCorrespOptionValue.equals("A")) {
                sendersCorrespondent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(sendersCorrespondent.getTextContent()));
                document.renameNode(sendersCorrespondent, null, "SenderCorrespondentA");
                rootNode.removeChild(sendersCorrespOption);
            }
            else if (sendersCorrespOptionValue.equals("B")) {
                sendersCorrespondent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(sendersCorrespondent.getTextContent()));
                document.renameNode(sendersCorrespondent, null, "SenderCorrespondentB");
                rootNode.removeChild(sendersCorrespOption);
            }
            else if (sendersCorrespOptionValue.equals("D")) {
                sendersCorrespondent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(sendersCorrespondent.getTextContent()));
                document.renameNode(sendersCorrespondent, null, "SenderCorrespondentD");
                rootNode.removeChild(sendersCorrespOption);
            }
        }

        // tag : ReceiverCorrespondentA/B/D
        Node receiversCorrespOption = document.getElementsByTagName("receiversCorrespOption").item(0);
        Node receiversCorrespondent = document.getElementsByTagName("receiversCorrespondent").item(0);

        if (receiversCorrespondent != null && receiversCorrespOption != null) {
            String receiversCorrespOptionValue = receiversCorrespOption.getTextContent();
            if (receiversCorrespOptionValue.equals("A")) {
                receiversCorrespondent
                        .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(receiversCorrespondent.getTextContent()));
                document.renameNode(receiversCorrespondent, null, "ReceiverCorrespondentA");
                rootNode.removeChild(receiversCorrespOption);
            }
            else if (receiversCorrespOptionValue.equals("B")) {
                receiversCorrespondent
                        .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(receiversCorrespondent.getTextContent()));
                document.renameNode(receiversCorrespondent, null, "ReceiverCorrespondentB");
                rootNode.removeChild(receiversCorrespOption);
            }
            else if (receiversCorrespOptionValue.equals("D")) {
                receiversCorrespondent
                        .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(receiversCorrespondent.getTextContent()));
                document.renameNode(receiversCorrespondent, null, "ReceiverCorrespondentD");
                rootNode.removeChild(receiversCorrespOption);
            }
        }

        // tag : ChequeDetails
        NodeList chequeList = document.getElementsByTagName("Cheque");

        for (int i = 0; i < chequeList.getLength(); ++i) {
            Element cheque = (Element) chequeList.item(i);

            // child tag : DateOfIssue
            Node dateOfIssue = cheque.getElementsByTagName("DateOfIssue").item(0);
            if (dateOfIssue != null) {
                dateOfIssue.setTextContent(SWT_Outgoing_Globals.formatDateTo6Digits(dateOfIssue.getTextContent()));
            }

            // child tag : AmountB
            Node amountOption = cheque.getElementsByTagName("amountOption").item(0);
            Node amount = cheque.getElementsByTagName("amount").item(0);
            if (amountOption != null) {
                String amountOptionValue = amountOption.getTextContent();
                if (amountOptionValue.equals("A")) {
                    amount.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(amount.getTextContent()));
                    document.renameNode(amount, null, "AmountA");
                    cheque.removeChild(amountOption);
                }
                else if (amountOptionValue.equals("B")) {
                    amount.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(amount.getTextContent()));
                    document.renameNode(amount, null, "AmountB");
                    cheque.removeChild(amountOption);
                }
            }

            // child tag : DrawerBankA/B/D
            Node drawerBankOption = cheque.getElementsByTagName("drawerBankOption").item(0);
            Node drawerBank = cheque.getElementsByTagName("drawerBank").item(0);
            if (drawerBank != null && drawerBankOption != null) {
                String drawerBankOptionValue = drawerBankOption.getTextContent();

                if (drawerBankOptionValue.equals("A")) {
                    drawerBank.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(drawerBank.getTextContent()));
                    document.renameNode(drawerBank, null, "DrawerBankA");
                    rootNode.removeChild(drawerBankOption);
                }
                else if (drawerBankOptionValue.equals("B")) {
                    drawerBank.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(drawerBank.getTextContent()));
                    document.renameNode(drawerBank, null, "DrawerBankB");
                    rootNode.removeChild(drawerBankOption);
                }
                else if (drawerBankOptionValue.equals("D")) {
                    drawerBank.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(drawerBank.getTextContent()));
                    document.renameNode(drawerBank, null, "DrawerBankD");
                    rootNode.removeChild(drawerBankOption);
                }

            }
        }

        // child tag : Payee
        Node payee = document.getElementsByTagName("Payee").item(0);
        Node payeeOption = document.getElementsByTagName("payeeOption").item(0);
        if (payee != null) {
            String payeeoption = "not_F";
            if (payeeOption.getTextContent() != null)
                payeeoption = payeeOption.getTextContent();
            if (payeeOption.getTextContent().trim().equals("F")) {
                payee.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(payee.getTextContent()));
                document.renameNode(payee, null, "PayeeF");
                rootNode.removeChild(payeeOption);
            }
            else {
                payee.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(payee.getTextContent()));
            }
        }

        // orderingCustomerOption Tag and orderingCustomer Tag
        Temp_context = null;
        Temp_name = null;
        Node OrdCust = document.getElementsByTagName("orderingCustomer").item(0);
        Node OrdCustOp = document.getElementsByTagName("orderingCustomerOption").item(0);

        if (OrdCust != null && OrdCustOp != null) {
            Temp_context = OrdCustOp.getTextContent();
            if ("A".equals(Temp_context)) {
                Element payer = document.createElement("PayerA");
                payer.setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(document.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
                document.getElementsByTagName("Cheque").item(0).appendChild(payer);
            }
            if ("K".equals(Temp_context)) {
                Element payer = document.createElement("PayerK");
                payer.setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(document.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
                document.getElementsByTagName("Cheque").item(0).appendChild(payer);
            }
            if ("F".equals(Temp_context)) {
                Element payer = document.createElement("PayerF");
                payer.setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(document.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
                document.getElementsByTagName("Cheque").item(0).appendChild(payer);
            }
            rootNode.removeChild(document.getElementsByTagName("orderingCustomer").item(0));
            rootNode.removeChild(document.getElementsByTagName("orderingCustomerOption").item(0));
        }
        // tag : senderToReceiverInformation (in request, not in response)
        Node senderToReceiverInfo = document.getElementsByTagName("SenderToReceiverInfo").item(0);
        if (senderToReceiverInfo != null) {
            senderToReceiverInfo.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(senderToReceiverInfo.getTextContent()));
        }
    }

}
