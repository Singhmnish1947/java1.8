package com.misys.ub.swift;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class UB_MT111_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT111_FileCreator.class.getName());
    private String FinalXml;
    private String strResult;

    public String MT111_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            ClassCastException, SAXException, IOException, ParserConfigurationException {
        String requestMsg1 = requestMsg.replaceAll("/n", "");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {

            dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(requestMsg1));
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();

            // update Element value
            updateElementValue(doc);

            // write the updated document to forward request
            doc.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            strResult = writer.toString();

        }
        catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) {
            e1.printStackTrace();
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

    private void updateElementValue(Document doc) {
        String temp_name = "";
        Node tag;
        Node tempTag;
        Element newTag;
        Element rootElement = doc.getDocumentElement();
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

        doc.renameNode(rootElement, null, "MeridianMessage");
        rootElement.setAttribute("MessageType", "SWIFT_MT111");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        newTag = doc.createElement("MeridianMessageType");
        newTag.setTextContent("SWIFT_MT111");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("InternalMessageType");
        newTag.setTextContent("P");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("HostType");
        newTag.setTextContent("UB");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("HostID");
        newTag.setTextContent(swiftProperties.getProperty("HostId"));
        rootElement.appendChild(newTag);

        newTag = doc.createElement("Direction");
        newTag.setTextContent("O");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("TransactionReference");
        tempTag = doc.getElementsByTagName("SenderReference").item(0);
        if (tempTag != null) {
            newTag.setTextContent(doc.getElementsByTagName("SenderReference").item(0).getTextContent());
            rootElement.appendChild(newTag);
        }

        newTag = doc.createElement("Priority");
        newTag.setTextContent("N");
        rootElement.appendChild(newTag);

        Date systemArrivalTime = SystemInformationManager.getInstance().getBFSystemDate();
        String dateString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(systemArrivalTime);
        newTag = doc.createElement("SystemArrivalTime");
        newTag.setTextContent(dateString);
        rootElement.appendChild(newTag);

        newTag = doc.createElement("Network");
        newTag.setTextContent("SWIFT");
        rootElement.appendChild(newTag);

        tempTag = doc.getElementsByTagName("SenderAddress").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("SenderAddress").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(tag.getTextContent()));
        }

        newTag = doc.createElement("LineOfBusiness");
        newTag.setTextContent("UB");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("MultipleMessageStatus");
        newTag.setTextContent("I");
        rootElement.appendChild(newTag);

        tempTag = doc.getElementsByTagName("messageId").item(0);
        if (tempTag != null) {
            double source_messageId = Double.parseDouble(doc.getElementsByTagName("messageId").item(0).getTextContent());
            tempTag = doc.getElementsByTagName("internalRef").item(0);
            if (tempTag != null) {
                String source_internalRef = doc.getElementsByTagName("internalRef").item(0).getTextContent();
                if (!source_internalRef.equals("")) {
                    newTag = doc.createElement("Direction");
                    newTag.setTextContent("I");
                    rootElement.appendChild(newTag);

                    tag = doc.getElementsByTagName("internalRef").item(0);
                    doc.renameNode(tag, null, "InternalReference");

                    if (source_messageId != 0.0) {
                        tag = doc.getElementsByTagName("messageId").item(0);
                        doc.renameNode(tag, null, "MessageID");
                    }
                }
                else {
                    newTag = doc.createElement("Direction");
                    newTag.setTextContent("O");
                    rootElement.appendChild(newTag);
                }
            }
        }

        tempTag = doc.getElementsByTagName("Priority").item(0);
        if (tempTag != null) {
            String target_Priority = doc.getElementsByTagName("Priority").item(0).getTextContent();
            if (target_Priority.equals("N")) {
                newTag = doc.createElement("B2IDeliveryMonitoring");
                if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                    newTag.setTextContent(swiftProperties.getProperty("B2IDeliveryMonitoring"));
                }
                rootElement.appendChild(newTag);
                newTag = doc.createElement("B2IObsolescencePeriod");
                if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                    newTag.setTextContent("020");
                }
                rootElement.appendChild(newTag);
            }
        }

        newTag = doc.createElement("B3Validation");
        tempTag = doc.getElementsByTagName("serviceIdentifierId").item(0);
        if (tempTag != null) {
            newTag.setTextContent(doc.getElementsByTagName("serviceIdentifierId").item(0).getTextContent());
            rootElement.appendChild(newTag);
        }

        tempTag = doc.getElementsByTagName("DateOfIssue").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("DateOfIssue").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.formatDateTo6Digits(tag.getTextContent()));
        }

        if (doc.getElementsByTagName("amountOption").item(0) != null) {
            String source_amountOption = doc.getElementsByTagName("amountOption").item(0).getTextContent();
            if (source_amountOption != "") {
                if ("A".equals(source_amountOption)) {
                    temp_name = "AmountA";
                }
                else if ("B".equals(source_amountOption)) {
                    temp_name = "AmountB";
                }
                doc.renameNode(doc.getElementsByTagName("amountOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("amount").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .ReplacePeriodWithComma(doc.getElementsByTagName("amount").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("amount").item(0));
                }
            }
        }

        if (doc.getElementsByTagName("drawerBankOption").item(0) != null) {
            String source_drawerBankOption = doc.getElementsByTagName("drawerBankOption").item(0).getTextContent();
            if (source_drawerBankOption != "") {
                if ("A".equals(source_drawerBankOption)) {
                    temp_name = "DrawerBankA";
                }
                else if ("B".equals(source_drawerBankOption)) {
                    temp_name = "DrawerBankB";
                }
                else if ("D".equals(source_drawerBankOption)) {
                    temp_name = "DrawerBankD";
                }
                doc.renameNode(doc.getElementsByTagName("drawerBankOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("drawerBank").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("drawerBank").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("drawerBank").item(0));
                }
            }
        }
    }
}