package com.misys.ub.swift;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class UB_MT900910_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT900910_FileCreator.class.getName());

    // variables for creating input request message
    public static final String xmlFilePath = "900910inputReqmsg.xml";
    private String strResult;
    private String FinalXml;
    public String Temp_context;
    public String Temp_name;
    public NodeList Temp_Node;
    public Double Temp_Double;
    public Element rootElement;
    public Element Temp_Element;
    public static SimpleDateFormat format_date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public String MT900910_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, ClassCastException, SAXException, IOException, ParserConfigurationException {
        String requestMsg1 = requestMsg.replaceAll("/n", "");

        // File xmlFile = new File(xmlFilePath);
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

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
            InputStream inputStream = new FileInputStream(configLocation + FromEssence_To_UBMMM.CONF + FromEssence_To_UBMMM.SWIFT_PROPERTY_FILENAME);
            swiftProperties.load(inputStream);
        }
        catch (IOException ex) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_SWIFT_PROPERTIES_FILE_NOT_FOUND, new Object[] {}, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
        }
    	// MeridianMessageType Tag

        Temp_Element = doc.createElement("MeridianMessageType");
        if (Temp_Element.getTextContent().equals("MT910")) {
            Temp_Element.setTextContent("SWIFT_MT910");
        }
        else if (Temp_Element.getTextContent().equals("MT900")) {
            Temp_Element.setTextContent("SWIFT_MT900");
        }
        Node rootNode = doc.getFirstChild();
        doc.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        // rootElement.setAttribute("MessageType", "");
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        // MeridianMessageType
        // tag : MeridianMessageType
        Node externalMessageType = doc.getElementsByTagName("ExternalMessageType").item(0);
        if (externalMessageType.getTextContent().equals("MT900")) {
            rootElement.setAttribute("MessageType", "SWIFT_MT900");
        }
        else if (externalMessageType.getTextContent().equals("MT910")) {
            rootElement.setAttribute("MessageType", "SWIFT_MT910");
        }

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
        // doc.renameNode(doc.getElementsByTagName("disposalRef").item(0), null, "HostReference");

        // Direction Tag
        Temp_Element = doc.createElement("Direction");
        Temp_Element.setTextContent("O");
        rootElement.appendChild(Temp_Element);
        // doc.renameNode(doc.getElementsByTagName("O").item(0), null, "Direction");

        // TransactionReference Tag
        // doc.renameNode(doc.getElementsByTagName("transactionReference").item(0), null,
        // "TransactionReference");

        // Priority Tag
        Temp_Element = doc.createElement("Priority");
        Temp_Element.setTextContent("N");
        rootElement.appendChild(Temp_Element);

        // CurrencyCode Tag
        // doc.renameNode(doc.getElementsByTagName("tdCurrencyCode").item(0), null, "CurrencyCode");

        // Amount Tag

        // Temp_Double =
        // Double.parseDouble(doc.getElementsByTagName("tdAmount").item(0).getTextContent());
        String Amount = doc.getElementsByTagName("Amount").item(0).getTextContent();
        Amount = Amount.replace(".", "");
        double AmountDouble = Double.parseDouble(Amount);
        Double.toString(AmountDouble);

        /*
         * String headerAmount = sourceMsg.getString("tdAmount"); headerAmount =
         * headerAmount.replace(".", ""); targetMsg.setField("Amount",
         * Double.parseDouble(headerAmount));
         */

        // Network Tag
        Temp_Element = doc.createElement("Network");
        Temp_Element.setTextContent("SWIFT");
        rootElement.appendChild(Temp_Element);

        // Sender Tag
        if (doc.getElementsByTagName("SenderAddress").item(0) != null) {
            doc.getElementsByTagName("SenderAddress").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));
        }
        // doc.renameNode(doc.getElementsByTagName("sender").item(0), null, "SenderAddress");

        // DestinationAddress Tag
        // doc.renameNode(doc.getElementsByTagName("receiver").item(0), null, "DestinationAddress");

        // LineOfBusiness Tag
        Temp_Element = doc.createElement("LineOfBusiness");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // MultipleMeassage status Tag

        Temp_Element = doc.createElement("MultipleMessageStatus");
        Temp_Element.setTextContent("I");
        rootElement.appendChild(Temp_Element);

        // TRN Tag

        // doc.renameNode(doc.getElementsByTagName("transactionReference").item(0), null, "TRN");
        if (doc.getElementsByTagName("TransactionReference").item(0) != null) {
            Temp_Element = doc.createElement("TRN");
            Temp_Element.setTextContent(doc.getElementsByTagName("TransactionReference").item(0).getTextContent());
            rootElement.appendChild(Temp_Element);

        }

        // RelatedReference

        // doc.renameNode(doc.getElementsByTagName("relatedReference").item(0), null,
        // "RelatedReference");

        // AccountId Tag

        // doc.renameNode(doc.getElementsByTagName("accountIdentification").item(0), null,
        // "AccountId");

        // ValueDateCcyamount
        // tdValueDate Tag and tdCurrencyCode Tag and tdAmount Tag
        doc.getElementsByTagName("tdValueDate").item(0).setTextContent(
                SWT_Outgoing_Globals.formatDateTo6Digits(doc.getElementsByTagName("tdValueDate").item(0).getTextContent())
                        + doc.getElementsByTagName("CurrencyCode").item(0).getTextContent()
                        + SWT_Outgoing_Globals.ReplacePeriodWithComma(doc.getElementsByTagName("Amount").item(0).getTextContent()));
        doc.renameNode(doc.getElementsByTagName("tdValueDate").item(0), null, "ValueDateCcyAmount");
        doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("CurrencyCode").item(0));
        doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("Amount").item(0));

        // Ordering customer Option
        // have to handle null check

        if (doc.getElementsByTagName("orderingCustOption").item(0) != null) {

            Temp_context = doc.getElementsByTagName("orderingCustOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "OrderingCustomerA";
            }
            else if ("K".equals(Temp_context)) {
                Temp_name = "OrderingCustomerK";
            }
            else if ("F".equals(Temp_context)) {
                Temp_name = "OrderingCustomerF";

            }
            if (Temp_name != null) {
                doc.renameNode(doc.getElementsByTagName("orderingCustOption").item(0), null, Temp_name);
                doc.getElementsByTagName(Temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("orderingCustomer").item(0));

            }
            Temp_context = "";
            Temp_name = "";

        }

        // Temp_Double =
        // Double.parseDouble(doc.getElementsByTagName("ExternalMessageType").item(0).getTextContent());
        // doc.
        // if(Temp_Double){
        //// orderingInstOption Option have to be only for 900 check
        // have to handle null check
        if (doc.getElementsByTagName("orderingInstOption").item(0) != null) {
            Temp_context = doc.getElementsByTagName("orderingInstOption").item(0).getTextContent();
            if (Temp_context != "") {
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
            }
        }
        Temp_context = null;
        Temp_name = null;

        // Intermediary option Tag

        if (doc.getElementsByTagName("intermediaryOption").item(0) != null) {
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

        }
        Temp_context = null;
        Temp_name = null;
        if (doc.getElementsByTagName("AccountIdP").item(0) != null) {
            doc.getElementsByTagName("AccountIdP").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("AccountIdP").item(0).getTextContent()));
        }
        /*
         * //PaymentPriority Properties properties=new Properties(); try { properties.load(new
         * FileInputStream("SWTCommon.properties")); } catch (FileNotFoundException e1) {
         * e1.printStackTrace(); } catch (IOException e1) { e1.printStackTrace(); }
         */

        // Payment Property Tag
        Properties properties = new Properties();
        try {
            FileInputStream is = new FileInputStream(configLocation + "conf//swift//" + "SWTCommonStub.properties");
            properties.load(is);

        }
        catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        Temp_Element = doc.createElement("PaymentPriority");
        Temp_Element.setTextContent(properties.getProperty("PaymentPriority"));
        rootElement.appendChild(Temp_Element);

        /*
         * Temp_Element = doc.createElement("PaymentPriority");
         * Temp_Element.setTextContent(properties.getProperty("PaymentPriority"));
         * Temp_Element.setTextContent("9999"); rootElement.appendChild(Temp_Element);
         */

        // System Arrival Time
        Date systemArrivalTime = new Date();
        String dateString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(systemArrivalTime);
        Temp_Element = doc.createElement("SystemArrivalTime");
        Temp_Element.setTextContent(dateString);
        rootElement.appendChild(Temp_Element);

        // Query ValueDate

        Temp_Element = doc.createElement("ValueDate");
        String ValueDate = new SimpleDateFormat("yyyyMMdd").format(systemArrivalTime);
        Temp_Element.setTextContent(SWT_Outgoing_Globals.formatDate(ValueDate));
        rootElement.appendChild(Temp_Element);

        // senderToReceiverInformation
        if (doc.getElementsByTagName("SenderToReceiverInfo").item(0) != null) {
            doc.getElementsByTagName("SenderToReceiverInfo").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("SenderToReceiverInfo").item(0).getTextContent()));
            // doc.getElementsByTagName("SenderAddress").item(0).setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));
        }
    }
}
