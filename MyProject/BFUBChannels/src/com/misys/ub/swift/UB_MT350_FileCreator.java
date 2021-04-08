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

public class UB_MT350_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT350_FileCreator.class.getName());
    // variable for creating input request message
    public static final String xmlFilePath = "350Req.xml";

    private String strResult;
    private String FinalXml;
    public String Temp_context;
    public String Temp_name;
    public NodeList Temp_Node;
    public Double Temp_Double;
    public Element rootElement;
    public Element Temp_Element;

    // public static String Node;

    public String MT350_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            ClassCastException, SAXException, IOException, ParserConfigurationException {

        String requestMsg1 = requestMsg.replaceAll("/n", "");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder dBuilder;
        try {

            dBuilder = dbFactory.newDocumentBuilder();
            // Document doc = dBuilder.parse(xmlFile);
            InputSource is = new InputSource(new StringReader(requestMsg1));
            Document doc = dBuilder.parse(is);
            rootElement = doc.getDocumentElement();

            doc.getDocumentElement().normalize();

            // update Element value
            updateElementValue(doc);

            // write the updated document to forward request
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            strResult = writer.toString();

            FinalXml = writer.toString();

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

        }
        catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) {
            e1.printStackTrace();
        }
        // logic to remove empty content tags
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
            InputStream is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    FromEssence_To_UBMMM.CONF + FromEssence_To_UBMMM.SWIFT_PROPERTY_FILENAME, configLocation,
                    BankFusionThreadLocal.getUserZone());
            swiftProperties.load(is);
        }
        catch (IOException ex) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_SWIFT_PROPERTIES_FILE_NOT_FOUND, new Object[] {}, new HashMap(),
                    BankFusionThreadLocal.getBankFusionEnvironment());
        }

        // MeridianMessageType Tag

        Node rootNode = doc.getFirstChild();
        doc.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        rootElement.setAttribute("MessageType", "SWIFT_MT350");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

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

        // Direction Tag
        Temp_Element = doc.createElement("Direction");
        Temp_Element.setTextContent("O");
        rootElement.appendChild(Temp_Element);

        if (doc.getElementsByTagName("TransactionReference").item(0) != null) {
            Temp_Element = doc.createElement("SendersReference");
            Temp_Element.setTextContent(doc.getElementsByTagName("TransactionReference").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);

        }

        // Priority Tag
        Temp_Element = doc.createElement("Priority");
        Temp_Element.setTextContent("N");
        rootElement.appendChild(Temp_Element);
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
        // systemArrivalTime Tag
        Date systemArrivalTime = SystemInformationManager.getInstance().getBFSystemDate();
        String dateString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(systemArrivalTime);
        Temp_Element = doc.createElement("SystemArrivalTime");
        Temp_Element.setTextContent(dateString);
        rootElement.appendChild(Temp_Element);
        // Network Tag
        Temp_Element = doc.createElement("Network");
        Temp_Element.setTextContent("SWIFT");
        rootElement.appendChild(Temp_Element);

        doc.getElementsByTagName("SenderAddress").item(0).setTextContent(
                SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));

        // LineOfBusiness Tag
        Temp_Element = doc.createElement("LineOfBusiness");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // MultipleMeassage status Tag

        Temp_Element = doc.createElement("MultipleMessageStatus");
        Temp_Element.setTextContent("I");
        rootElement.appendChild(Temp_Element);
        // CancelAction Tag

        if (doc.getElementsByTagName("serviceIdentifierId").item(0) != null) {
            Temp_Element = doc.createElement("B3ServiceId");
            Temp_Element.setTextContent(doc.getElementsByTagName("serviceIdentifierId").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);

        }

        // SendersReference // have to handle empty validation
        @SuppressWarnings("unused")
        boolean flag_SeqA;
        if (doc.getElementsByTagName("TransactionReference").item(0) != null
                && doc.getElementsByTagName("TransactionReference").getLength() > 0) {

            flag_SeqA = true;

        }

        // RelatedReference// have to handle empty validation
        if (doc.getElementsByTagName("RelatedReference").item(0) != null
                && doc.getElementsByTagName("RelatedReference").getLength() > 0) {

            flag_SeqA = true;

        }

        // TypeOfOperation Tag//have to handle empty validation
        if (doc.getElementsByTagName("TypeOfOperation").item(0) != null
                && doc.getElementsByTagName("TypeOfOperation").getLength() > 0) {

            flag_SeqA = true;

        }

        if (doc.getElementsByTagName("partyA") != null) {
            Temp_context = doc.getElementsByTagName("partyA").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;

            Temp_context = doc.getElementsByTagName("partyAOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "PartyA_A";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "PartyA_D";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "PartyA_J";

            }
            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("partyAOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyA").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyA").item(0));
            }
        }

        Temp_context = null;
        Temp_name = null;

        // PartyB Tag//have to handle empty validation
        if (doc.getElementsByTagName("partyB") != null) {
            Temp_context = doc.getElementsByTagName("partyB").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
            Temp_context = doc.getElementsByTagName("partyBOption").item(0).getTextContent();

            if ("A".equals(Temp_context)) {
                Temp_name = "PartyB_A";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "PartyB_D";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "PartyB_J";

            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("partyBOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyB").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyB").item(0));
            }
        }

        Temp_context = null;
        Temp_name = null;

        // fundOrInstructingParty Tag//have to handle empty validation
        if (doc.getElementsByTagName("fundOrInstructingPartyOption") != null) {
            Temp_context = doc.getElementsByTagName("fundOrInstructingPartyOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "FundOrInstructingPartyA";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "FundOrInstructingPartyD";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "FundOrInstructingPartyJ";

            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("fundOrInstructingPartyOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("fundOrInstructingParty").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("fundOrInstructingParty").item(0));
            }
        }

        Temp_context = null;
        Temp_name = null;

        // Approach1//Verify//SenderToReceiverInfo //
        if (doc.getElementsByTagName("SenderToReceiverInfo").item(0) != null) {
            Temp_context = doc.getElementsByTagName("SenderToReceiverInfo").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;

            doc.getElementsByTagName("SenderToReceiverInfo").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("SenderToReceiverInfo").item(0).getTextContent()));

        }

        Temp_Element = doc.createElement("NewSequenceA");
        Temp_Element.setTextContent("@");
        rootElement.appendChild(Temp_Element);

        // CcyPrincipalAmount
        @SuppressWarnings("unused")
        boolean flag_SeqB = false;
        if (doc.getElementsByTagName("CcyPrincipalAmount").item(0) != null) {
            Temp_context = doc.getElementsByTagName("CcyPrincipalAmount").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }

            // doc.renameNode(doc.getElementsByTagName("Temp_context").item(0),null,"CcyPrincipalAmount");
            doc.getElementsByTagName("CcyPrincipalAmount").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("CcyPrincipalAmount").item(0).getTextContent()));

        }

        if (doc.getElementsByTagName("ValueDateofInterestPayment").item(0) != null) {
            doc.getElementsByTagName("ValueDateofInterestPayment").item(0).setTextContent(SWT_Outgoing_Globals
                    .formatDate(doc.getElementsByTagName("ValueDateofInterestPayment").item(0).getTextContent()));
        }

        if (doc.getElementsByTagName("CcyAndInterestAmount").item(0) != null) {
            doc.getElementsByTagName("CcyAndInterestAmount").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("CcyAndInterestAmount").item(0).getTextContent()));
        }

        // InterestRate Tag
        if (doc.getElementsByTagName("InterestRate").item(0) != null) {
            doc.getElementsByTagName("InterestRate").item(0).setTextContent(
                    SWT_Outgoing_Globals.ReplacePeriodWithComma(doc.getElementsByTagName("InterestRate").item(0).getTextContent()));
        }
        // DayCountFraction
        if (doc.getElementsByTagName("DayCountFraction") != null) {
            Temp_context = doc.getElementsByTagName("DayCountFraction").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // NextInterestPayDate
        if (doc.getElementsByTagName("NextInterestPayDate").item(0) != null) {
            Temp_context = doc.getElementsByTagName("NextInterestPayDate").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
            doc.getElementsByTagName("NextInterestPayDate").item(0).setTextContent(
                    SWT_Outgoing_Globals.formatDate(doc.getElementsByTagName("NextInterestPayDate").item(0).getTextContent()));

        }

        Temp_Element = doc.createElement("NewSequenceB");
        Temp_Element.setTextContent("@");
        rootElement.appendChild(Temp_Element);

        // deliveryAgent Tag
        @SuppressWarnings("unused")
        boolean flag_SeqC = false;
        if (doc.getElementsByTagName("deliveryAgent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("deliveryAgent").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }

            Temp_context = doc.getElementsByTagName("deliveryAgentOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "CDeliveryAgentA";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "CDeliveryAgentD";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "CDeliveryAgentJ";
            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("deliveryAgentOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("deliveryAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("deliveryAgent").item(0));
            }
        }

        Temp_context = null;
        Temp_name = null;

        // interMediary2 Tag
        if (doc.getElementsByTagName("interMediary2").item(0) != null) {
            Temp_context = doc.getElementsByTagName("interMediary2").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }

            Temp_context = doc.getElementsByTagName("interMediary2Option").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "CIntermediary2A";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "CIntermediary2D";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "CIntermediary2J";
            }

            if (Temp_name != null) {

                doc.renameNode(doc.getElementsByTagName("interMediary2Option").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("interMediary2").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("interMediary2").item(0));
            }
        }

        Temp_context = null;
        Temp_name = null;

        // interMediary Tag
        if (doc.getElementsByTagName("interMediary") != null) {
            Temp_context = doc.getElementsByTagName("interMediary").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = doc.getElementsByTagName("interMediaryOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "CIntermediaryA";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "CIntermediaryD";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "CIntermediaryJ";
            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("interMediaryOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("interMediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("interMediary").item(0));
            }
        }

        Temp_context = null;
        Temp_name = null;

        // receivingAgent Tag
        if (doc.getElementsByTagName("receivingAgent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("receivingAgent").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = doc.getElementsByTagName("receivingAgentOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "CReceivingAgentA";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "CReceivingAgentD";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "CReceivingAgentJ";
            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("receivingAgentOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("receivingAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("receivingAgent").item(0));
            }

        }

        Temp_context = null;
        Temp_name = null;

        // beneficiary Tag
        if (doc.getElementsByTagName("beneficiary").item(0) != null) {
            Temp_context = doc.getElementsByTagName("beneficiary").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = doc.getElementsByTagName("beneficiaryOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "CBeneficiaryA";
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "CBeneficiaryD";
            }
            else if ("J".equals(Temp_context)) {
                Temp_name = "CBeneficiaryJ";
            }

            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("beneficiaryOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("beneficiary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("beneficiary").item(0));
                Temp_context = null;
                Temp_name = null;
            }
        }

        Temp_Element = doc.createElement("NewSequenceC");
        Temp_Element.setTextContent("@");
        rootElement.appendChild(Temp_Element);

        // TransCuyAndIntAmount
        @SuppressWarnings("unused")
        boolean flag_SeqD = false;

        if (doc.getElementsByTagName("TransCuyAndIntAmount") != null) {
            Temp_context = doc.getElementsByTagName("TransCuyAndIntAmount").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqD = true;
            }
        }

        // TransactionCcyAndNetIntAmt
        if (doc.getElementsByTagName("TransactionCcyAndNetIntAmt").item(0) != null) {
            Temp_context = doc.getElementsByTagName("TransactionCcyAndNetIntAmt").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqD = true;
            }
            doc.getElementsByTagName("TransactionCcyAndNetIntAmt").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("TransactionCcyAndNetIntAmt").item(0).getTextContent()));

        }

        // ExchangeRate
        if (doc.getElementsByTagName("ExchangeRate").item(0) != null) {

            Temp_context = doc.getElementsByTagName("ExchangeRate").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqD = true;
            }
            doc.getElementsByTagName("ExchangeRate").item(0).setTextContent(
                    SWT_Outgoing_Globals.ReplacePeriodWithComma(doc.getElementsByTagName("ExchangeRate").item(0).getTextContent()));

        }

        // TaxRate
        if (doc.getElementsByTagName("TaxRate").item(0) != null) {
            Temp_context = doc.getElementsByTagName("TaxRate").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqD = true;
            }

        }

        // ReportingCcyTaxAmount
        if (doc.getElementsByTagName("ReportingCcyTaxAmount") != null) {

            Temp_context = doc.getElementsByTagName("ReportingCcyTaxAmount").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqD = true;
            }
            doc.getElementsByTagName("ReportingCcyTaxAmount").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("ReportingCcyTaxAmount").item(0).getTextContent()));

        }
        // approach2 //Verify //InterestPeriod Tag

        if (doc.getElementsByTagName("InterestPeriod") != null) {
            Temp_context = doc.getElementsByTagName("InterestPeriod").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
            Temp_context = null;

            doc.getElementsByTagName("InterestPeriod").item(0).setTextContent(
                    SWT_Outgoing_Globals.formatDate(doc.getElementsByTagName("InterestPeriod").item(0).getTextContent()));

        }

    }
}
