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

public class UB_MT942_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT942_FileCreator.class.getName());
    private String FinalXml;
    private String strResult;
    public static final String xmlFilePath = "request.xml";
    public String Temp_context;
    public String Temp_name;
    public NodeList Temp_Node;
    public Double Temp_Double;
    public Element rootElement;
    public Element Temp_Element;
    public static SimpleDateFormat format_date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public String MT942_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
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

        Node rootNode = doc.getFirstChild();
        doc.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        rootElement.setAttribute("MessageType", "SWIFT_MT942"); // change as per
                                                                // message type
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
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        // MeridianMessageType Tag
        Temp_Element = doc.createElement("MeridianMessageType");
        Temp_Element.setTextContent("SWIFT_MT942");
        rootElement.appendChild(Temp_Element);

        // InternalMessageType Tag
        Temp_Element = doc.createElement("InternalMessageType");
        Temp_Element.setTextContent("P");
        rootElement.appendChild(Temp_Element);

        // HostType Tag
        Temp_Element = doc.createElement("HostType");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // InternalMessageType Tag
        Temp_Element = doc.createElement("HostID");
        Temp_Element.setTextContent(swiftProperties.getProperty("HostId"));
        rootElement.appendChild(Temp_Element);

        // Direction Tag
        Temp_Element = doc.createElement("Direction");
        Temp_Element.setTextContent("O");
        rootElement.appendChild(Temp_Element);

        // TRN Tag
        Temp_Element = doc.createElement("TRN");
        Temp_Element.setTextContent(doc.getElementsByTagName("TransactionReference").item(0).getTextContent());
        rootElement.appendChild(Temp_Element);

        // Priority Tag
        Temp_Element = doc.createElement("Priority");
        Temp_Element.setTextContent("N");
        rootElement.appendChild(Temp_Element);
        
        //Reconcile Tag
        Node deliveryChannel = doc.getElementsByTagName("deliveryChannel").item(0);
        if (deliveryChannel != null) {
        	Temp_Element = doc.createElement("Reconcile");
            if (deliveryChannel.getTextContent().equals("RECON")) {
            	Temp_Element.setTextContent("Y");
            }
            else if(deliveryChannel.getTextContent().equals("BOTH")){
            	Temp_Element.setTextContent("B");
            }
            else{
            	Temp_Element.setTextContent("N");
            }
            rootNode.appendChild(Temp_Element);
        }
        
        
        // B2IDeliveryMonitoring Tag and B2IObsolescencePeriod Tag
        Temp_context = doc.getElementsByTagName("Priority").item(0).getTextContent();
        if ("N".equals(Temp_context)) {
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
        Temp_Element = doc.createElement("SystemArrivalTime");
        Temp_Element.setTextContent(format_date.format(systemArrivalTime));
        rootElement.appendChild(Temp_Element);

        // Network Tag
        Temp_Element = doc.createElement("Network");
        Temp_Element.setTextContent("SWIFT");
        rootElement.appendChild(Temp_Element);

        // sender Tag
        if (doc.getElementsByTagName("SenderAddress").item(0) != null) {
            doc.getElementsByTagName("SenderAddress").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));
        }

        // LineOfBusiness Tag
        Temp_Element = doc.createElement("LineOfBusiness");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // MultipleMessageStatus Tag
        Temp_Element = doc.createElement("MultipleMessageStatus");
        Temp_Element.setTextContent("I");
        rootElement.appendChild(Temp_Element);

        if (doc.getElementsByTagName("FloorLimitIndicator1").item(0) != null) {
            doc.getElementsByTagName("FloorLimitIndicator1").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("FloorLimitIndicator1").item(0).getTextContent()));
        }
        if (doc.getElementsByTagName("AccountIdP").item(0) != null) {
            doc.getElementsByTagName("AccountIdP").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("AccountIdP").item(0).getTextContent()));
        }

        // floorLimitIndicator2 Tag
        if (doc.getElementsByTagName("FloorLimitIndicator2").item(0) != null) {
            doc.getElementsByTagName("FloorLimitIndicator2").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("FloorLimitIndicator2").item(0).getTextContent()));
        }

        // StmtLines change it
        for (int j = 0; j < doc.getElementsByTagName("StmtLines").getLength(); j++) {
            Temp_Node = doc.getElementsByTagName("StmtLines").item(j).getChildNodes();
            for (int i = 0; i < Temp_Node.getLength(); i++) {
                if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE
                        && "StatementLine".equals(Temp_Node.item(i).getNodeName())) {
                    Temp_Node.item(i)
                            .setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(Temp_Node.item(i).getTextContent()));
                }
                else if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE
                        && "InfoAccOwner".equals(Temp_Node.item(i).getNodeName())) {
                    Temp_Node.item(i)
                            .setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(Temp_Node.item(i).getTextContent()));
                }
            }
        }
        Temp_Node = null;
        // numberSumEntriesDebit Tag
        if (doc.getElementsByTagName("NumAndSumOfEntries1").item(0) != null) {
            doc.getElementsByTagName("NumAndSumOfEntries1").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("NumAndSumOfEntries1").item(0).getTextContent()));
        }

        // numberSumEntriesCredit Tag
        if (doc.getElementsByTagName("NumAndSumOfEntries2").item(0) != null) {
            doc.getElementsByTagName("NumAndSumOfEntries2").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("NumAndSumOfEntries2").item(0).getTextContent()));
        }

    }

}
