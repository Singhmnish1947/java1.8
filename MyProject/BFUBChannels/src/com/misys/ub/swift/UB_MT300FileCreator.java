package com.misys.ub.swift;

import java.io.IOException;
import java.io.InputStream;
//import java.io.File;
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

public class UB_MT300FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT300FileCreator.class.getName());
    private String FinalXml;
    private String strResult;
    public static final String xmlFilePath = "request.xml";
    public String Temp_context;
    public String Temp_name;
    public NodeList Temp_Node;
    public Double Temp_Double;
    public Element rootElement;
    public Element Temp_Element;
    public boolean flag_SeqA;
    public boolean flag_SeqB;
    public boolean flag_SeqC;
    public boolean flag_SeqD;
    public String Delivery_Temp_option;
    public String Delivery_Temp_context;
    public String Intermediary_Temp_option;
    public String Intermediary_Temp_context;
    public String Receiving_Temp_option;
    public String Receiving_Temp_context;
    public String Beneficiary_Temp_option;
    public String Beneficiary_Temp_context;
    public static SimpleDateFormat format_date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public String MT300_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            ClassCastException, SAXException, IOException, ParserConfigurationException {
        String requestMsg1 = requestMsg.replaceAll("/n", "");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setIgnoringElementContentWhitespace(true);
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
        Node rootNode = doc.getFirstChild();
        doc.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        rootElement.setAttribute("MessageType", "SWIFT_MT300"); // change as per
                                                                // message type
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        // MeridianMessageType Tag
        Temp_Element = doc.createElement("MeridianMessageType");
        Temp_Element.setTextContent("SWIFT_MT300");
        rootElement.appendChild(Temp_Element);

        // messageType Tag
        // doc.renameNode(doc.getElementsByTagName("messageType").item(0), null,
        // "ExternalMessageType");

        // InternalMessageType Tag
        Temp_Element = doc.createElement("InternalMessageType");
        Temp_Element.setTextContent("C");
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

        // Direction Tag
        Temp_Element = doc.createElement("Direction");
        Temp_Element.setTextContent("O");
        rootElement.appendChild(Temp_Element);

        // senderReference Tag not required

        // Priority Tag
        Temp_Element = doc.createElement("Priority");
        Temp_Element.setTextContent("N");
        rootElement.appendChild(Temp_Element);

        // B2IDeliveryMonitoring Tag and B2IObsolescencePeriod Tag
        Temp_context = doc.getElementsByTagName("Priority").item(0).getTextContent();
        if ("N".equals(Temp_context)) {
            Temp_Element = doc.createElement("B2IDeliveryMonitoring");
            Temp_Element.setTextContent("");
            rootElement.appendChild(Temp_Element);

            Temp_Element = doc.createElement("B2IObsolescencePeriod");
            Temp_Element.setTextContent("");
            rootElement.appendChild(Temp_Element);
        }

        // Priority Tag
        Temp_Element = doc.createElement("Cancel");
        Temp_Element.setTextContent("N");
        rootElement.appendChild(Temp_Element);

        // SystemArrivalTime Tag
        Date systemArrivalTime = SystemInformationManager.getInstance().getBFSystemDate();
        Temp_Element = doc.createElement("SystemArrivalTime");
        Temp_Element.setTextContent(format_date.format(systemArrivalTime));
        rootElement.appendChild(Temp_Element);

        // Network Tag
        Temp_Element = doc.createElement("Network");
        Temp_Element.setTextContent("SWIFT");
        rootElement.appendChild(Temp_Element);

        // sender Tag
        // doc.renameNode(doc.getElementsByTagName("sender").item(0), null,
        // "SenderAddress");
        if (doc.getElementsByTagName("SenderAddress").item(0) != null) {
            doc.getElementsByTagName("SenderAddress").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));
        }

        // receiver Tag
        // doc.renameNode(doc.getElementsByTagName("receiver").item(0), null,
        // "DestinationAddress");

        // branch Tag
        // doc.renameNode(doc.getElementsByTagName("branch").item(0), null,
        // "BusinessEntity");

        // LineOfBusiness Tag
        Temp_Element = doc.createElement("LineOfBusiness");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // MultipleMessageStatus Tag
        Temp_Element = doc.createElement("MultipleMessageStatus");
        Temp_Element.setTextContent("I");
        rootElement.appendChild(Temp_Element);

        // action Tag
        // doc.renameNode(doc.getElementsByTagName("action").item(0), null,
        // "CancellationAction");

        // Query
        // serviceIdentifierId Tag
        // doc.renameNode(doc.getElementsByTagName("serviceIdentifierId").item(0),
        // null, "B3ServiceId");

        // TransactionReference Tag
        if (doc.getElementsByTagName("SenderReference").item(0) != null) {
            Temp_context = doc.getElementsByTagName("SenderReference").item(0).getTextContent();
            Temp_Element = doc.createElement("TransactionReference");
            Temp_Element.setTextContent(Temp_context);
            rootElement.appendChild(Temp_Element);
        }

        // SenderReference Tag
        if (doc.getElementsByTagName("SenderReference").item(0) != null) {
            Temp_context = doc.getElementsByTagName("SenderReference").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
        }
        // doc.renameNode(doc.getElementsByTagName("senderReference").item(0),
        // null, "SenderReference");

        // relatedReference Tag
        if (doc.getElementsByTagName("RelatedReference").item(0) != null) {
            Temp_context = doc.getElementsByTagName("RelatedReference").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
        }
        // doc.renameNode(doc.getElementsByTagName("relatedReference").item(0),
        // null, "RelatedReference");

        // TypeOfOperation Tag
        if (doc.getElementsByTagName("TypeOfOperation").item(0) != null) {
            Temp_context = doc.getElementsByTagName("TypeOfOperation").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
        }
        // doc.renameNode(doc.getElementsByTagName("typeOfOperation").item(0),
        // null, "TypeOfOperation");

        // ScopeOfOperation Tag
        if (doc.getElementsByTagName("ScopeOfOperation").item(0) != null) {
            Temp_context = doc.getElementsByTagName("ScopeOfOperation").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
        }
        // doc.renameNode(doc.getElementsByTagName("scopeOfOperation").item(0),
        // null, "ScopeOfOperation");

        // CommonReference Tag
        if (doc.getElementsByTagName("CommonReference").item(0) != null) {
            Temp_context = doc.getElementsByTagName("CommonReference").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
        }
        // doc.renameNode(doc.getElementsByTagName("CommonReference").item(0),
        // null, "commonReference");

        // BlockTradeIndicator Tag
        if (doc.getElementsByTagName("BlockTradeIndicator").item(0) != null) {
            Temp_context = doc.getElementsByTagName("BlockTradeIndicator").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
        }
        // doc.renameNode(doc.getElementsByTagName("BlockTradeIndicator").item(0),
        // null, "blockTradeIndicator");

        // SplitSettlementIndicator Tag
        if (doc.getElementsByTagName("SplitSettlementIndicator").item(0) != null) {
            Temp_context = doc.getElementsByTagName("SplitSettlementIndicator").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
        }
        // doc.renameNode(doc.getElementsByTagName("SplitSettlementIndicator").item(0),
        // null, "spiltSettlementIndicator");

        // partyA Tag
        if (doc.getElementsByTagName("partyA").item(0) != null) {
            Temp_context = doc.getElementsByTagName("partyA").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
        }

        // partyAOption Tag
        if (doc.getElementsByTagName("partyAOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("partyAOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("partyAOption").item(0), null, "PartyA_A");
                doc.getElementsByTagName("PartyA_A").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyA").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyA").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("partyAOption").item(0), null, "PartyA_D");
                doc.getElementsByTagName("PartyA_D").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyA").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyA").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("partyAOption").item(0), null, "PartyA_J");
                doc.getElementsByTagName("PartyA_J").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyA").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyA").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("partyAOption").item(0),
         * null, Temp_name); doc.getElementsByTagName(Temp_name).item(0).setTextContent
         * (SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName("partyA"
         * ).item(0).getTextContent())); doc.getElementsByTagName("MeridianMessage"
         * ).item(0).removeChild(doc.getElementsByTagName("partyA").item(0)); }
         */
        Temp_context = null;

        // partyB Tag
        if (doc.getElementsByTagName("partyB").item(0) != null) {
            Temp_context = doc.getElementsByTagName("partyB").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
        }

        // partyBOption Tag
        if (doc.getElementsByTagName("partyBOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("partyBOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("partyBOption").item(0), null, "PartyB_A");
                doc.getElementsByTagName("PartyB_A").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyB").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyB").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("partyBOption").item(0), null, "PartyB_D");
                doc.getElementsByTagName("PartyB_D").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyB").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyB").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("partyBOption").item(0), null, "PartyB_J");
                doc.getElementsByTagName("PartyB_J").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("partyB").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("partyB").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("partyBOption").item(0),
         * null, Temp_name); doc.getElementsByTagName(Temp_name).item(0).setTextContent
         * (SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName("partyB"
         * ).item(0).getTextContent())); doc.getElementsByTagName("MeridianMessage"
         * ).item(0).removeChild(doc.getElementsByTagName("partyB").item(0)); }
         */
        Temp_context = null;

        // fundOrBeneficaryCustomer Tag
        if (doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0) != null) {
            Temp_context = doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
        }

        //
        // fundOrBenCustOption Tag
        if (doc.getElementsByTagName("fundOrBenCustOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("fundOrBenCustOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("fundOrBenCustOption").item(0), null, "FundOrBeneficiaryCustomerA");
                doc.getElementsByTagName("FundOrBeneficiaryCustomerA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("fundOrBenCustOption").item(0), null, "FundOrBeneficiaryCustomerD");
                doc.getElementsByTagName("FundOrBeneficiaryCustomerD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("fundOrBenCustOption").item(0), null, "FundOrBeneficiaryCustomerJ");
                doc.getElementsByTagName("FundOrBeneficiaryCustomerJ").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("fundOrBeneficaryCustomer").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("fundOrBenCustOption"
         * ).item(0), null, Temp_name); doc.getElementsByTagName(Temp_name).item(
         * 0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName
         * ("fundOrBeneficaryCustomer").item(0).getTextContent())); doc.getElementsByTagName
         * ("MeridianMessage").item(0).removeChild(doc.getElementsByTagName
         * ("fundOrBeneficaryCustomer").item(0)); }
         */
        Temp_context = null;

        // termsAndConditions Tag
        if (doc.getElementsByTagName("TermsAndConditions").item(0) != null) {
            Temp_context = doc.getElementsByTagName("TermsAndConditions").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqA = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("TermsAndConditions").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("TermsAndConditions").item(0).getTextContent()));
        }

        // NewSequenceA Tag
        if (flag_SeqA) {
            Temp_Element = doc.createElement("NewSequenceA");
            Temp_Element.setTextContent("@");
            rootElement.appendChild(Temp_Element);
        }

        // TradeDate Tag
        if (doc.getElementsByTagName("TradeDate").item(0) != null) {
            Temp_context = doc.getElementsByTagName("TradeDate").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("TradeDate").item(0).setTextContent(
                    SWT_Outgoing_Globals.formatDate(doc.getElementsByTagName("TradeDate").item(0).getTextContent()));
        }

        // B4ValueDate Tag
        if (doc.getElementsByTagName("B4ValueDate").item(0) != null) {
            doc.getElementsByTagName("B4ValueDate").item(0).setTextContent(
                    SWT_Outgoing_Globals.formatDate(doc.getElementsByTagName("B4ValueDate").item(0).getTextContent()));
        }

        // ExchangeRate Tag
        if (doc.getElementsByTagName("ExchangeRate").item(0) != null) {
            Temp_context = doc.getElementsByTagName("ExchangeRate").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("ExchangeRate").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiterWithBlank(doc.getElementsByTagName("ExchangeRate").item(0).getTextContent()));
        }

        // B1CurrencyAmount Tag
        if (doc.getElementsByTagName("B1CurrencyAmount").item(0) != null) {
            Temp_context = doc.getElementsByTagName("B1CurrencyAmount").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("B1CurrencyAmount").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("B1CurrencyAmount").item(0).getTextContent()));
        }

        // b1DeliveryAgent Tag
        if (doc.getElementsByTagName("b1DeliveryAgent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("b1DeliveryAgent").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // b1DeliveryAgentOption Tag
        if (doc.getElementsByTagName("b1DeliveryAgentOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("b1DeliveryAgentOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1DeliveryAgentOption").item(0), null, "B1DeliveryAgentA");
                doc.getElementsByTagName("B1DeliveryAgentA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1DeliveryAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b1DeliveryAgent").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1DeliveryAgentOption").item(0), null, "B1DeliveryAgentD");
                doc.getElementsByTagName("B1DeliveryAgentD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1DeliveryAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b1DeliveryAgent").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1DeliveryAgentOption").item(0), null, "B1DeliveryAgentJ");
                doc.getElementsByTagName("B1DeliveryAgentJ").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1DeliveryAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b1DeliveryAgent").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("b1DeliveryAgentOption"
         * ).item(0), null, Temp_name); doc.getElementsByTagName(Temp_name).item(
         * 0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName
         * ("b1DeliveryAgent").item(0).getTextContent())); doc.getElementsByTagName
         * ("MeridianMessage").item(0).removeChild(doc.getElementsByTagName
         * ("b1DeliveryAgent").item(0)); }
         */
        Temp_context = null;

        // b1Intermediary Tag
        if (doc.getElementsByTagName("b1Intermediary").item(0) != null) {
            Temp_context = doc.getElementsByTagName("b1Intermediary").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // b1IntermediaryOption Tag
        if (doc.getElementsByTagName("b1IntermediaryOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("b1IntermediaryOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1IntermediaryOption").item(0), null, "B1IntermediaryA");
                doc.getElementsByTagName("B1IntermediaryA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1Intermediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("b1Intermediary").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1IntermediaryOption").item(0), null, "B1IntermediaryD");
                doc.getElementsByTagName("B1IntermediaryD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1Intermediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("b1Intermediary").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1IntermediaryOption").item(0), null, "B1IntermediaryJ");
                doc.getElementsByTagName("B1IntermediaryJ").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1Intermediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("b1Intermediary").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("b1IntermediaryOption"
         * ).item(0), null, Temp_name); doc.getElementsByTagName(Temp_name).item(
         * 0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName
         * ("b1Intermediary").item(0).getTextContent())); doc.getElementsByTagName
         * ("MeridianMessage").item(0).removeChild(doc.getElementsByTagName
         * ("b1Intermediary").item(0)); }
         */
        Temp_context = null;

        // b1ReceivingAgent Tag
        if (doc.getElementsByTagName("b1ReceivingAgent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("b1ReceivingAgent").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // b1ReceivingAgentOption Tag
        if (doc.getElementsByTagName("b1ReceivingAgentOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("b1ReceivingAgentOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1ReceivingAgentOption").item(0), null, "B1ReceivingAgentA");
                doc.getElementsByTagName("B1ReceivingAgentA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1ReceivingAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b1ReceivingAgent").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1ReceivingAgentOption").item(0), null, "B1ReceivingAgentD");
                doc.getElementsByTagName("B1ReceivingAgentD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1ReceivingAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b1ReceivingAgent").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b1ReceivingAgentOption").item(0), null, "B1ReceivingAgentJ");
                doc.getElementsByTagName("B1ReceivingAgentJ").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b1ReceivingAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b1ReceivingAgent").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("b1ReceivingAgentOption"
         * ).item(0), null, Temp_name); doc.getElementsByTagName(Temp_name).item(
         * 0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName
         * ("b1ReceivingAgent").item(0).getTextContent())); doc.getElementsByTagName
         * ("MeridianMessage").item(0).removeChild(doc.getElementsByTagName
         * ("b1ReceivingAgent").item(0)); }
         */
        Temp_context = null;

        // B2CurrencyAmount Tag
        if (doc.getElementsByTagName("B2CurrencyAmount").item(0) != null) {
            Temp_context = doc.getElementsByTagName("B2CurrencyAmount").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("B2CurrencyAmount").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("B2CurrencyAmount").item(0).getTextContent()));
        }

        // b2DeliveryAgent Tag
        if (doc.getElementsByTagName("b2DeliveryAgent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("b2DeliveryAgent").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // b2DeliveryAgentOption Tag
        if (doc.getElementsByTagName("b2DeliveryAgentOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("b2DeliveryAgentOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2DeliveryAgentOption").item(0), null, "B2DeliveryAgentA");
                doc.getElementsByTagName("B2DeliveryAgentA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2DeliveryAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b2DeliveryAgent").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2DeliveryAgentOption").item(0), null, "B2DeliveryAgentD");
                doc.getElementsByTagName("B2DeliveryAgentD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2DeliveryAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b2DeliveryAgent").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2DeliveryAgentOption").item(0), null, "B2DeliveryAgentJ");
                doc.getElementsByTagName("B2DeliveryAgentJ").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2DeliveryAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b2DeliveryAgent").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("b2DeliveryAgentOption"
         * ).item(0), null, Temp_name); doc.getElementsByTagName(Temp_name).item(
         * 0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName
         * ("b2DeliveryAgent").item(0).getTextContent())); doc.getElementsByTagName
         * ("MeridianMessage").item(0).removeChild(doc.getElementsByTagName
         * ("b2DeliveryAgent").item(0)); }
         */
        Temp_context = null;

        // b2Intermediary Tag
        if (doc.getElementsByTagName("b2Intermediary").item(0) != null) {
            Temp_context = doc.getElementsByTagName("b2Intermediary").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // b2IntermediaryOption Tag
        if (doc.getElementsByTagName("b2IntermediaryOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("b2IntermediaryOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2IntermediaryOption").item(0), null, "B2IntermediaryA");
                doc.getElementsByTagName("B2IntermediaryA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2Intermediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("b2Intermediary").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2IntermediaryOption").item(0), null, "B2IntermediaryD");
                doc.getElementsByTagName("B2IntermediaryD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2Intermediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("b2Intermediary").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2IntermediaryOption").item(0), null, "B2IntermediaryJ");
                doc.getElementsByTagName("B2IntermediaryJ").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2Intermediary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("b2Intermediary").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("b2IntermediaryOption"
         * ).item(0), null, Temp_name); doc.getElementsByTagName(Temp_name).item(
         * 0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName
         * ("b2Intermediary").item(0).getTextContent())); doc.getElementsByTagName
         * ("MeridianMessage").item(0).removeChild(doc.getElementsByTagName
         * ("b2Intermediary").item(0)); }
         */
        Temp_context = null;

        // b2ReceivingAgent Tag
        if (doc.getElementsByTagName("b2ReceivingAgent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("b2ReceivingAgent").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // b2ReceivingAgentOption Tag
        if (doc.getElementsByTagName("b2ReceivingAgentOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("b2ReceivingAgentOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2ReceivingAgentOption").item(0), null, "B2ReceivingAgentA");
                doc.getElementsByTagName("B2ReceivingAgentA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2ReceivingAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b2ReceivingAgent").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2ReceivingAgentOption").item(0), null, "B2ReceivingAgentD");
                doc.getElementsByTagName("B2ReceivingAgentD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2ReceivingAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b2ReceivingAgent").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("b2ReceivingAgentOption").item(0), null, "B2ReceivingAgentJ");
                doc.getElementsByTagName("B2ReceivingAgentJ").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("b2ReceivingAgent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("b2ReceivingAgent").item(0));
            }
        }

        /*
         * if (Temp_name != null) { doc.renameNode(doc.getElementsByTagName("b2ReceivingAgentOption"
         * ).item(0), null, Temp_name); doc.getElementsByTagName(Temp_name).item(
         * 0).setTextContent(SWT_Outgoing_Globals .RemoveUBDelimiter(doc.getElementsByTagName
         * ("b2ReceivingAgent").item(0).getTextContent())); doc.getElementsByTagName
         * ("MeridianMessage").item(0).removeChild(doc.getElementsByTagName
         * ("b2ReceivingAgent").item(0)); }
         */
        Temp_context = null;

        // beneficiary Tag
        if (doc.getElementsByTagName("beneficiary").item(0) != null) {
            Temp_context = doc.getElementsByTagName("beneficiary").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqB = true;
            }
        }

        // beneficiaryOption Tag
        if (doc.getElementsByTagName("beneficiaryOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("beneficiaryOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("beneficiaryOption").item(0), null, "BeneficiaryA");
                doc.getElementsByTagName("BeneficiaryA").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("beneficiary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("beneficiary").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("beneficiaryOption").item(0), null, "BeneficiaryD");
                doc.getElementsByTagName("BeneficiaryD").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("beneficiary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("beneficiary").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("beneficiaryOption").item(0), null, "BeneficiaryJ");
                doc.getElementsByTagName("BeneficiaryJ").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("beneficiary").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("beneficiary").item(0));
            }
        }

        /*
         * if (Temp_name != null) {
         * doc.renameNode(doc.getElementsByTagName("beneficiaryOption").item(0), null, Temp_name);
         * doc.getElementsByTagName(Temp_name).item(0).setTextContent (SWT_Outgoing_Globals
         * .RemoveUBDelimiter(doc.getElementsByTagName("beneficiary" ).item(0).getTextContent()));
         * doc.getElementsByTagName("MeridianMessage" ).item(0).removeChild(doc.getElementsByTagName
         * ("beneficiary").item(0)); }
         */
        Temp_context = null;

        // NewSequenceB Tag
        if (flag_SeqB) {
            Temp_Element = doc.createElement("NewSequenceB");
            Temp_Element.setTextContent("@");
            rootElement.appendChild(Temp_Element);
        }

        // ContactInformation Tag
        if (doc.getElementsByTagName("ContactInformation").item(0) != null) {
            Temp_context = doc.getElementsByTagName("ContactInformation").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("ContactInformation").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("ContactInformation").item(0).getTextContent()));
        }

        // DealingMethod Tag
        if (doc.getElementsByTagName("DealingMethod").item(0) != null) {
            Temp_context = doc.getElementsByTagName("DealingMethod").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = null;
        }

        // dealingBranchPartyA Tag
        if (doc.getElementsByTagName("dealingBranchPartyA").item(0) != null) {
            Temp_context = doc.getElementsByTagName("dealingBranchPartyA").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
        }

        // dealingBranchPartyAOption Tag
        if (doc.getElementsByTagName("dealingBranchPartyAOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("dealingBranchPartyAOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyAOption").item(0), null, "DealingBranchPartyA_A");
                doc.getElementsByTagName("DealingBranchPartyA_A").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyA").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyA").item(0));
            }
            else if ("B".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyAOption").item(0), null, "DealingBranchPartyA_B");
                doc.getElementsByTagName("DealingBranchPartyA_B").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyA").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyA").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyAOption").item(0), null, "DealingBranchPartyA_D");
                doc.getElementsByTagName("DealingBranchPartyA_D").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyA").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyA").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyAOption").item(0), null, "DealingBranchPartyA_J");
                doc.getElementsByTagName("DealingBranchPartyA_J").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyA").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyA").item(0));
            }
        }
        Temp_context = null;

        // dealingBranchPartyB Tag
        if (doc.getElementsByTagName("dealingBranchPartyB").item(0) != null) {
            Temp_context = doc.getElementsByTagName("dealingBranchPartyB").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
        }

        // dealingBranchPartyBOption Tag
        if (doc.getElementsByTagName("dealingBranchPartyBOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("dealingBranchPartyBOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyBOption").item(0), null, "DealingBranchPartyB_A");
                doc.getElementsByTagName("DealingBranchPartyB_A").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyB").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyB").item(0));
            }
            else if ("B".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyBOption").item(0), null, "DealingBranchPartyB_B");
                doc.getElementsByTagName("DealingBranchPartyB_B").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyB").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyB").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyBOption").item(0), null, "DealingBranchPartyB_D");
                doc.getElementsByTagName("DealingBranchPartyB_D").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyB").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyB").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyBOption").item(0), null, "DealingBranchPartyB_J");
                doc.getElementsByTagName("DealingBranchPartyB_J").item(0)
                        .setTextContent(doc.getElementsByTagName("dealingBranchPartyB").item(0).getTextContent());
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("dealingBranchPartyB").item(0));
            }
        }
        Temp_context = null;

        // brokerID Tag
        if (doc.getElementsByTagName("brokerID").item(0) != null) {
            Temp_context = doc.getElementsByTagName("brokerID").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
        }

        // brokerIDOption Tag
        if (doc.getElementsByTagName("brokerIDOption").item(0) != null && Temp_context != null) {
            Temp_context = doc.getElementsByTagName("brokerIDOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("brokerIDOption").item(0), null, "BrokerIdA");
                doc.getElementsByTagName("BrokerIdA").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("brokerID").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("brokerID").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("brokerIDOption").item(0), null, "BrokerIdD");
                doc.getElementsByTagName("BrokerIdD").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("brokerID").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("brokerID").item(0));
            }
            else if ("J".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("brokerIDOption").item(0), null, "BrokerIdJ");
                doc.getElementsByTagName("BrokerIdJ").item(0).setTextContent(
                        SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("brokerID").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("brokerID").item(0));
            }
        }
        Temp_context = null;

        // BrokersCommission Tag
        if (doc.getElementsByTagName("BrokersCommission").item(0) != null) {
            Temp_context = doc.getElementsByTagName("BrokersCommission").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = null;
        }

        // CounterPartysReference Tag
        if (doc.getElementsByTagName("CounterPartysReference").item(0) != null) {
            Temp_context = doc.getElementsByTagName("CounterPartysReference").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("CounterPartysReference").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("CounterPartysReference").item(0).getTextContent()));
        }

        // BrokersReference Tag
        if (doc.getElementsByTagName("BrokersReference").item(0) != null) {
            Temp_context = doc.getElementsByTagName("BrokersReference").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("BrokersReference").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("BrokersReference").item(0).getTextContent()));
        }

        // SendersToReceiversInfo Tag
        if (doc.getElementsByTagName("SendersToReceiversInfo").item(0) != null) {
            Temp_context = doc.getElementsByTagName("SendersToReceiversInfo").item(0).getTextContent();
            if (Temp_context != "") {
                flag_SeqC = true;
            }
            Temp_context = null;
            doc.getElementsByTagName("SendersToReceiversInfo").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("SendersToReceiversInfo").item(0).getTextContent()));
        }

        // NewSequenceC Tag
        if (flag_SeqC) {
            Temp_Element = doc.createElement("NewSequenceC");
            Temp_Element.setTextContent("@");
            rootElement.appendChild(Temp_Element);
        }

        // SplitSettlement Tag
        for (int j = 0; j < doc.getElementsByTagName("SplitSettlement").getLength(); j++) {
            // doc.renameNode(doc.getElementsByTagName("splitSettlement").item(j),
            // null, "SplitSettlement");
            Temp_Node = doc.getElementsByTagName("SplitSettlement").item(j).getChildNodes();
            for (int i = 0; i < Temp_Node.getLength(); i++) {
                if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE) {

                    switch (Temp_Node.item(i).getNodeName()) {
                        case "DBeneficiaryD":
                            if (!Temp_Node.item(i).getTextContent().isEmpty() && Temp_Node.item(i).getTextContent() != null) {
                                flag_SeqD = true;
                            }
                            // doc.renameNode(Temp_Node.item(i), null,
                            // "DBeneficiaryD");
                            break;
                        case "BuySellIndicator":
                            if (!Temp_Node.item(i).getTextContent().isEmpty() && Temp_Node.item(i).getTextContent() != null) {
                                flag_SeqD = true;
                            }
                            // doc.renameNode(Temp_Node.item(i), null,
                            // "BuySellIndicator");
                            break;
                        case "DCurrencyAmount":
                            if (!Temp_Node.item(i).getTextContent().isEmpty() && Temp_Node.item(i).getTextContent() != null) {
                                flag_SeqD = true;
                            }
                            // doc.renameNode(Temp_Node.item(i), null,
                            // "DCurrencyAmount");
                            break;
                        case "deliveryAgent":
                            if (!Temp_Node.item(i).getTextContent().isEmpty() && Temp_Node.item(i).getTextContent() != null) {
                                flag_SeqD = true;
                            }
                            // doc.renameNode(Temp_Node.item(i), null,
                            // "DCurrencyAmount");
                            if (Delivery_Temp_option != null) {
                                if ("A".equals(Delivery_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DDeliveryAgentA");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("deliveryAgentOption").item(0));
                                }
                                else if ("D".equals(Delivery_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DDeliveryAgentD");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("deliveryAgentOption").item(0));
                                }
                                else if ("J".equals(Delivery_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DDeliveryAgentJ");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("deliveryAgentOption").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Delivery_Temp_context = Temp_Node.item(i).getTextContent();
                            }
                            break;

                        case "deliveryAgentOption":
                            if (Delivery_Temp_context != null) {
                                Temp_context = Temp_Node.item(i).getTextContent();
                                if ("A".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DDeliveryAgentA");
                                    Temp_Node.item(i).setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Delivery_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("deliveryAgent").item(0));
                                }
                                else if ("D".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DDeliveryAgentD");
                                    Temp_Node.item(i).setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Delivery_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("deliveryAgent").item(0));
                                }
                                else if ("J".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DDeliveryAgentJ");
                                    Temp_Node.item(i).setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Delivery_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("deliveryAgent").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Delivery_Temp_option = Temp_Node.item(i).getTextContent();
                            }
                            break;
                        case "intermediary":
                            if (!Temp_Node.item(i).getTextContent().isEmpty() && Temp_Node.item(i).getTextContent() != null) {
                                flag_SeqD = true;
                            }
                            // doc.renameNode(Temp_Node.item(i), null,
                            // "DCurrencyAmount");
                            if (Intermediary_Temp_option != null) {
                                if ("A".equals(Intermediary_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DIntermediaryA");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("intermediaryOption").item(0));
                                }
                                else if ("D".equals(Intermediary_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DIntermediaryD");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("intermediaryOption").item(0));
                                }
                                else if ("J".equals(Intermediary_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DIntermediaryJ");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("intermediaryOption").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Intermediary_Temp_context = Temp_Node.item(i).getTextContent();
                            }
                            break;

                        case "intermediaryOption":
                            if (Intermediary_Temp_context != null) {
                                Temp_context = Temp_Node.item(i).getTextContent();
                                if ("A".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DIntermediaryA");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Intermediary_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("intermediary").item(0));
                                }
                                else if ("D".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DIntermediaryD");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Intermediary_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("intermediary").item(0));
                                }
                                else if ("J".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DIntermediaryJ");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Intermediary_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("intermediary").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Intermediary_Temp_option = Temp_Node.item(i).getTextContent();
                            }
                            break;
                        case "receivingAgent":
                            if (!Temp_Node.item(i).getTextContent().isEmpty() && Temp_Node.item(i).getTextContent() != null) {
                                flag_SeqD = true;
                            }
                            // doc.renameNode(Temp_Node.item(i), null,
                            // "DCurrencyAmount");
                            if (Receiving_Temp_option != null) {
                                if ("A".equals(Receiving_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DReceivingAgentA");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("receivingAgentOption").item(0));
                                }
                                else if ("D".equals(Receiving_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DReceivingAgentD");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("receivingAgentOption").item(0));
                                }
                                else if ("J".equals(Receiving_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DReceivingAgentJ");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("receivingAgentOption").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Receiving_Temp_context = Temp_Node.item(i).getTextContent();
                            }
                            break;

                        case "receivingAgentOption":
                            if (Receiving_Temp_context != null) {
                                Temp_context = Temp_Node.item(i).getTextContent();
                                if ("A".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DReceivingAgentA");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Receiving_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("receivingAgent").item(0));
                                }
                                else if ("D".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DReceivingAgentD");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Receiving_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("receivingAgent").item(0));
                                }
                                else if ("J".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DReceivingAgentJ");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Receiving_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("receivingAgent").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Receiving_Temp_option = Temp_Node.item(i).getTextContent();
                            }
                            break;
                        case "dbeneficiary":
                            if (!Temp_Node.item(i).getTextContent().isEmpty() && Temp_Node.item(i).getTextContent() != null) {
                                flag_SeqD = true;
                            }
                            // doc.renameNode(Temp_Node.item(i), null,
                            // "DCurrencyAmount");
                            if (Beneficiary_Temp_option != null) {
                                if ("A".equals(Beneficiary_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DBeneficiaryA");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("dBeneficiaryOption").item(0));
                                }
                                else if ("J".equals(Beneficiary_Temp_option)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DBeneficiaryJ");
                                    Temp_Node.item(i).setTextContent(
                                            SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("dBeneficiaryOption").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Beneficiary_Temp_context = Temp_Node.item(i).getTextContent();
                            }
                            break;

                        case "dBeneficiaryOption":
                            if (Beneficiary_Temp_context != null) {
                                Temp_context = Temp_Node.item(i).getTextContent();
                                if ("A".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DBeneficiaryA");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Beneficiary_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("dbeneficiary").item(0));
                                }
                                else if ("J".equals(Temp_context)) {
                                    doc.renameNode(Temp_Node.item(i), null, "DBeneficiaryJ");
                                    Temp_Node.item(i)
                                            .setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Beneficiary_Temp_context));
                                    doc.getElementsByTagName("SplitSettlement").item(0)
                                            .removeChild(doc.getElementsByTagName("dbeneficiary").item(0));
                                }
                                Temp_context = null;
                            }
                            else {
                                Beneficiary_Temp_option = Temp_Node.item(i).getTextContent();
                            }
                            break;

                    }
                }
            }
        }
        Temp_Node = null;

        // Swift 2017 changes
        // <nonDeliverableIndicator>Y</nonDeliverableIndicator>
        if ((doc.getElementsByTagName("NonDeliverableIndicator").item(0) != null)) {
            Temp_Element = doc.createElement("NonDeliverableIndicator");
            Temp_Element.setTextContent(doc.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);
            // doc.removeChild((doc.getElementsByTagName("NonDeliverableIndicator").item(0)));
            doc.getElementsByTagName("MeridianMessage").item(0)
                    .removeChild(doc.getElementsByTagName("NonDeliverableIndicator").item(0));

        }

        if ((doc.getElementsByTagName("valuationDate").item(0) != null)) {
            Temp_Element = doc.createElement("ValuationDate");
            Temp_Element.setTextContent(
                    SWT_Outgoing_Globals.formatDate(doc.getElementsByTagName("valuationDate").item(0).getTextContent()));
            rootElement.appendChild(Temp_Element);
            doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("valuationDate").item(0));

        }

        if ((doc.getElementsByTagName("NDFOpenIndicator").item(0) != null)) {
            Temp_Element = doc.createElement("NDFOpenIndicator");
            Temp_Element.setTextContent(doc.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);
            doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("NDFOpenIndicator").item(0));

        }
        if ((doc.getElementsByTagName("settlementCurrency").item(0) != null)) {
            Temp_Element = doc.createElement("SettlementCurrency");
            Temp_Element.setTextContent(doc.getElementsByTagName("settlementCurrency").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);
            doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("settlementCurrency").item(0));

        }
        if ((doc.getElementsByTagName("ASettlementRateSource").item(0) != null)) {

            String temp2values = doc.getElementsByTagName("ASettlementRateSource").item(0).getTextContent();
            String[] settmtRateSorce2 = new String[3];
            settmtRateSorce2 = temp2values.split("[$]");// split("[$]")

            if (temp2values.contains("$") && settmtRateSorce2.length >= 2) {
                Temp_Element = doc.createElement("ASettlementRateSource");
                Temp_Element.setTextContent(settmtRateSorce2[0]);
                Element temp = doc.createElement("SettlementRateSource");
                temp.appendChild(Temp_Element);
                rootElement.appendChild(temp);
                Element Temp_Element2;
                Temp_Element2 = doc.createElement("ASettlementRateSource");
                Temp_Element2.setTextContent(settmtRateSorce2[1]);
                Element temp2 = doc.createElement("SettlementRateSource");
                temp2.appendChild(Temp_Element2);
                rootElement.appendChild(temp2);

                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("ASettlementRateSource").item(0));

            }
            else if (settmtRateSorce2.length != 0) {

                Temp_Element = doc.createElement("ASettlementRateSource");
                Temp_Element.setTextContent(settmtRateSorce2[0]);
                Element temp = doc.createElement("SettlementRateSource");
                temp.appendChild(Temp_Element);
                rootElement.appendChild(temp);
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("ASettlementRateSource").item(0));
            }

        }
        if ((doc.getElementsByTagName("refOpeningConfirmation").item(0) != null)) {
            Temp_Element = doc.createElement("RefOpeningConfirmation");
            Temp_Element.setTextContent(doc.getElementsByTagName("refOpeningConfirmation").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);
            doc.getElementsByTagName("MeridianMessage").item(0)
                    .removeChild(doc.getElementsByTagName("refOpeningConfirmation").item(0));

        }
        if ((doc.getElementsByTagName("clearingSettlementSession").item(0) != null)) {
            Temp_Element = doc.createElement("ClearingSettlementSession");
            Temp_Element.setTextContent(doc.getElementsByTagName("clearingSettlementSession").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);
            doc.getElementsByTagName("MeridianMessage").item(0)
                    .removeChild(doc.getElementsByTagName("clearingSettlementSession").item(0));

        }

        // paymentClearingCenter
        if ((doc.getElementsByTagName("PaymentClearingCentre").item(0) != null)) {
            Temp_Element = doc.createElement("PaymentClearingCentre");
            Temp_Element.setTextContent(doc.getElementsByTagName("PaymentClearingCentre").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);
            doc.getElementsByTagName("MeridianMessage").item(0)
                    .removeChild(doc.getElementsByTagName("PaymentClearingCentre").item(0));

        }

        // end
        // NumberOfSettlements Tag

        if ((doc.getElementsByTagName("NumberOfSettlements").item(0) != null)
                && !doc.getElementsByTagName("NumberOfSettlements").item(0).getTextContent().isEmpty()
                && doc.getElementsByTagName("NumberOfSettlements").item(0).getTextContent() != null) {
            flag_SeqD = true;
        }

        // NewSequenceD Tag
        if (flag_SeqD) {
            Temp_Element = doc.createElement("NewSequenceD");
            Temp_Element.setTextContent("@");
            rootElement.appendChild(Temp_Element);
        }

    }

}
