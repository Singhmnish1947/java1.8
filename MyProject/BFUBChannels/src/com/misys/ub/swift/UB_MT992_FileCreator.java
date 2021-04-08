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

public class UB_MT992_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT992_FileCreator.class.getName());
    private String FinalXml;
    private String strResult;

    public String MT992_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
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
        rootElement.setAttribute("MessageType", "SWIFT_MTn92");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        newTag = doc.createElement("MeridianMessageType");
        newTag.setTextContent("SWIFT_MTn92");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("ExternalMessageType");
        newTag.setTextContent("MT992");
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

        newTag = doc.createElement("Priority");
        newTag.setTextContent("N");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("Cancel");
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
        else {
            tag = doc.getElementsByTagName("sender").item(0);
            if (tag != null) {
                doc.renameNode(tag, null, "SenderAddress");
            }
        }

        tempTag = doc.getElementsByTagName("receiver").item(0);
        if (tempTag != null) {
            doc.renameNode(tempTag, null, "DestinationAddress");
        }

        newTag = doc.createElement("LineOfBusiness");
        newTag.setTextContent("UB");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("MultipleMessageStatus");
        newTag.setTextContent("I");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("CancellationAction");
        newTag.setTextContent("I");
        rootElement.appendChild(newTag);

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

        tempTag = doc.getElementsByTagName("serviceIdentifierId").item(0);
        if (tempTag != null) {
            doc.renameNode(tempTag, null, "B3ServiceId");
        }

        newTag = doc.createElement("TRN");
        tempTag = doc.getElementsByTagName("TransactionReference").item(0);
        if (tempTag != null) {
            newTag.setTextContent(doc.getElementsByTagName("TransactionReference").item(0).getTextContent());
            rootElement.appendChild(newTag);
        }

        tempTag = doc.getElementsByTagName("ReleatedReference").item(0);
        if (tempTag != null) {
            doc.renameNode(tempTag, null, "RelatedReference");
        }

        tempTag = doc.getElementsByTagName("TAG_11S").item(0);
        if (tempTag != null) {
            String source_TAG_11S = doc.getElementsByTagName("TAG_11S").item(0).getTextContent();
            String resultString = "";
            source_TAG_11S = source_TAG_11S.replaceAll("-", "");
            resultString = source_TAG_11S.substring(0, 3) + source_TAG_11S.substring(5);
            newTag = doc.createElement("MT_Date_OriginalMsgS");
            newTag.setTextContent(resultString);
            rootElement.appendChild(newTag);
        }

        if (doc.getElementsByTagName("TransactionReference").item(0) != null
                && doc.getElementsByTagName("RelatedReference").item(0) != null
                && doc.getElementsByTagName("AccountIdentification").item(0) != null
                && doc.getElementsByTagName("ValueDate").item(0) != null && doc.getElementsByTagName("CurrencyCode").item(0) != null
                && doc.getElementsByTagName("Amount").item(0) != null) {

            newTag = doc.createElement("Fields_OriginalMsg");
            String resultString = "";
            resultString = doc.getElementsByTagName("TransactionReference").item(0).getTextContent()
                    + doc.getElementsByTagName("RelatedReference").item(0).getTextContent()
                    + doc.getElementsByTagName("AccountIdentification").item(0).getTextContent()
                    + SWT_Outgoing_Globals.formatDate(doc.getElementsByTagName("ValueDate").item(0).getTextContent())
                    + doc.getElementsByTagName("CurrencyCode").item(0).getTextContent()
                    + SWT_Outgoing_Globals.ReplacePeriodWithComma(doc.getElementsByTagName("Amount").item(0).getTextContent());
            newTag.setTextContent(resultString);
            rootElement.appendChild(newTag);
        }
    }
}