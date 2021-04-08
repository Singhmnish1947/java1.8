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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class UB_MT205_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT205_FileCreator.class.getName());

    public static final String xmlFilePath = "205Req.xml";
    private String strResult;
    private String FinalXml;
    public String Temp_context;
    public String Temp_name;
    public NodeList Temp_Node;
    public Double Temp_Double;
    public Element rootElement;
    public Element Temp_Element;

    public String MT205_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            ClassCastException, SAXException, IOException, ParserConfigurationException {

        String requestMsg1 = requestMsg.replaceAll("/n", "");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {

            dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(requestMsg1));
            Document doc = dBuilder.parse(is);
            rootElement = doc.getDocumentElement();

            doc.getDocumentElement().normalize();

            // update Element value
            updateElementValue(doc);

            // write the updated document to forward request
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbFactory.setIgnoringElementContentWhitespace(true);
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            strResult = writer.toString();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                @SuppressWarnings("unused")
                Document document = builder.parse(new InputSource(new StringReader(strResult)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.info("XML file updated successfully");

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
        // MeridianMessageType Tag
        Temp_Element = doc.createElement("MeridianMessageType");
        Temp_Element.setTextContent("SWIFT_MT205");
        rootElement.appendChild(Temp_Element);
        // rootnode logic for -<MeridianMessage System="SWIFT"
        // MessageFormat="StandardXML" Timestamp="2016-11-14 14:26:12.698"
        // MessageType="SWIFT_MT205">
        Node rootNode = doc.getFirstChild();
        doc.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        rootElement.setAttribute("MessageType", "SWIFT_MT205");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        // messageType Tag
        // doc.renameNode(doc.getElementsByTagName("messageType").item(0), null,
        // "ExternalMessageType");

        // InternalMessageType Tag
        Temp_Element = doc.createElement("InternalMessageType");
        Temp_Element.setTextContent("P");
        rootElement.appendChild(Temp_Element);

        // HostType Tag
        Temp_Element = doc.createElement("HostType");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // HostID Tag
        Temp_Element = doc.createElement("HostID");
        Temp_Element.setTextContent(swiftProperties.getProperty("HostId"));
        rootElement.appendChild(Temp_Element);

        // disposalRef Tag
        // doc.renameNode(doc.getElementsByTagName("disposalRef").item(0), null,
        // "HostReference");

        // TransactionReference Tag
        // TRN Tag

        // doc.renameNode(doc.getElementsByTagName("transactionReference").item(0),
        // null, "TRN"); --making change in the request message for unit testing
        Temp_Element = doc.createElement("TRN");
        Temp_Element.setTextContent(doc.getElementsByTagName("TransactionReference").item(0).getTextContent());
        rootElement.appendChild(Temp_Element);

        // Priority Tag

        Temp_Element = doc.createElement("Priority");
        Temp_Element.setTextContent("N");
        rootElement.appendChild(Temp_Element);

        // B2IDeliveryMonitoring Tag and B2IObsolescencePeriod Tag

        Temp_context = doc.getElementsByTagName("Priority").item(0).getTextContent();

        if (Temp_context != null && "N".equals(Temp_context)) {
            Temp_Element = doc.createElement("B2IDeliveryMonitoring");
            if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                Temp_Element.setTextContent(swiftProperties.getProperty("B2IDeliveryMonitoring"));
            }
            rootElement.appendChild(Temp_Element);
            Temp_Element = doc.createElement("B2IObsolescencePeriod");
            if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                Temp_Element.setTextContent("020");
            }
            rootElement.appendChild(Temp_Element);
        }

        // System Arrival Time
        Date systemArrivalTime = SystemInformationManager.getInstance().getBFSystemDate();
        String dateString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(systemArrivalTime);
        Temp_Element = doc.createElement("SystemArrivalTime");
        Temp_Element.setTextContent(dateString);
        rootElement.appendChild(Temp_Element);

        // Network Tag
        Temp_Element = doc.createElement("Network");
        Temp_Element.setTextContent("SWIFT");
        rootElement.appendChild(Temp_Element);

        // Sender Tag
        // doc.renameNode(doc.getElementsByTagName("sender").item(0), null,
        // "SenderAddress");
        doc.getElementsByTagName("SenderAddress").item(0).setTextContent(
                SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));

        // DestinationAddress Tag
        // doc.renameNode(doc.getElementsByTagName("receiver").item(0), null,
        // "DestinationAddress");

        // LineOfBusiness Tag
        Temp_Element = doc.createElement("LineOfBusiness");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // MultipleMeassage status Tag

        Temp_Element = doc.createElement("MultipleMessageStatus");
        Temp_Element.setTextContent("I");
        rootElement.appendChild(Temp_Element);
        // B3ServiceId
        // doc.renameNode(doc.getElementsByTagName("serviceIdentifierId").item(0),
        // null, "B3ServiceId");

        // RelatedReference
        // doc.renameNode(doc.getElementsByTagName("relatedReference").item(0),
        // null, "RelatedReference");

        // ValueDateCcyamount
        // tdValueDate Tag and tdCurrencyCode Tag and tdAmount Tag
        doc.getElementsByTagName("tdvalueDate").item(0).setTextContent(
                SWT_Outgoing_Globals.formatDateTo6Digits(doc.getElementsByTagName("tdvalueDate").item(0).getTextContent())
                        + doc.getElementsByTagName("tdcurrencyCode").item(0).getTextContent() + SWT_Outgoing_Globals
                                .ReplacePeriodWithComma(doc.getElementsByTagName("tdamount").item(0).getTextContent()));
        doc.renameNode(doc.getElementsByTagName("tdvalueDate").item(0), null, "ValueDateCcyAmount");
        doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("tdcurrencyCode").item(0));
        doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("tdamount").item(0));

        // //orderingInstOption Option

        if (doc.getElementsByTagName("orderingInstOption").item(0) != null
                && doc.getElementsByTagName("orderingInstOption").getLength() > 0) {

            Temp_context = doc.getElementsByTagName("orderingInstOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "OrderingInstitutionA";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "OrderingInstitutionD";
            }
            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("orderingInstOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("orderingInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("orderingInstitution").item(0));
            }
            Temp_context = null;
            Temp_name = null;
        }

        /*
         * //Not available in reference file but request xml has a tag if(doc.getElementsByTagName
         * ("orderingInstitutionOption").item(0)!=null){ Temp_context = doc.getElementsByTagName
         * ("orderingInstitutionOption").item(0).getTextContent(); if ("A".equals(Temp_context)){
         * Temp_name = "OrderingInstitutionA"; } else if ("D".equals(Temp_context)){ Temp_name =
         * "OrderingInstitutionD"; } if(Temp_name!=null){ doc.renameNode(doc.getElementsByTagName
         * ("orderingInstitutionOption").item(0), null, Temp_name); doc.getElementsByTagName
         * (Temp_name).item(0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter
         * (doc.getElementsByTagName("orderingInstitute").item( 0).getTextContent()));
         * doc.getElementsByTagName("UB_MT205").item(0).removeChild
         * (doc.getElementsByTagName("orderingInstitute").item(0));
         * 
         * }
         * 
         * } Temp_context=""; Temp_name="";
         */
        // sendersCorresOption sendersCorrespondent

        if (doc.getElementsByTagName("sendersCorresOption").item(0) != null
                && doc.getElementsByTagName("sendersCorresOption").getLength() > 0) {

            Temp_context = doc.getElementsByTagName("sendersCorresOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "SenderCorrespondentA";
            }
            else if ("B".equals(Temp_context)) {
                Temp_name = "SenderCorrespondentB";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "SenderCorrespondentD";
            }
            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("sendersCorresOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("sendersCorrespondent").item(0));

            }

            Temp_context = null;
            Temp_name = null;
        }

        // Intermediary option Tag

        if (doc.getElementsByTagName("intermediaryOption").item(0) != null
                && doc.getElementsByTagName("intermediaryOption").getLength() > 0) {
            Temp_context = doc.getElementsByTagName("intermediaryOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "IntermediaryA";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "IntermediaryD";
            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("intermediaryOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("intermediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("intermediary").item(0));

            }
            Temp_context = null;
            Temp_name = null;
        }
        // // accountWithInstOption accountWithInstitution
        if (doc.getElementsByTagName("accountWithInstOption").item(0) != null
                && doc.getElementsByTagName("accountWithInstOption").getLength() > 0) {

            Temp_context = doc.getElementsByTagName("accountWithInstOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "AccountWithInstA";
            }
            else if ("B".equals(Temp_context)) {
                Temp_name = "AccountWithInstB";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "AccountWithInstD";
            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("accountWithInstOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("accountWithInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("accountWithInstitution").item(0));

            }
            Temp_context = null;
            Temp_name = null;
        }

        // // beneficiaryInstOption beneficiaryInstitute
        if (doc.getElementsByTagName("beneficiaryInstOption").item(0) != null
                && doc.getElementsByTagName("beneficiaryInstOption").getLength() > 0) {

            Temp_context = doc.getElementsByTagName("beneficiaryInstOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "BeneficiaryA";
            }

            else if ("D".equals(Temp_context)) {
                Temp_name = "BeneficiaryD";
            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("beneficiaryInstOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("beneficiaryInstitute").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("beneficiaryInstitute").item(0));

            }
            Temp_context = null;
            Temp_name = null;
        }

        String source_messageId = (doc.getElementsByTagName("MessageId").item(0).getTextContent());
        double messageid = Double.parseDouble(source_messageId);
        String source_internalRef = (doc.getElementsByTagName("InternalReference").item(0).getTextContent());

        if (!source_internalRef.equals("")) {
            // Direction Tag with value I
            Temp_Element = doc.createElement("Direction");
            Temp_Element.setTextContent("I");
            rootElement.appendChild(Temp_Element);

            // have to handle null check for the node-- (i.e)--in request the
            // Tag is not there then no input for internalref
            // InternalReference
            if (doc.getElementsByTagName("internalRef").item(0) != null) {
                doc.renameNode(doc.getElementsByTagName("internalRef").item(0), null, "InternalReference");
            }

            // MessageId Tag
            if (messageid != 0.0) {
                doc.renameNode(doc.getElementsByTagName("MessageId").item(0), null, "MessageId");
            }
        }
        else {
            // Direction Tag
            Temp_Element = doc.createElement("Direction");
            Temp_Element.setTextContent("O");
            rootElement.appendChild(Temp_Element);
        }

        // senderToReceiverInformation
        if (doc.getElementsByTagName("SenderToReceiverInfo").item(0) != null) {
            doc.getElementsByTagName("SenderToReceiverInfo").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("SenderToReceiverInfo").item(0).getTextContent()));
            // doc.getElementsByTagName("SenderAddress").item(0).setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));
        }

    }

}
