package com.misys.ub.swift;

import java.io.FileInputStream;
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

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

/**
 * @author Rishav
 * 
 *         file to transform MT940950
 */

public class UB_MT940950_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT940950_FileCreator.class.getName());
    private String strResult;
    private String FinalXml;

    public String MT940950_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, ClassCastException, SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
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
            LOGGER.error(ExceptionUtil.getExceptionAsString( e));
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
        Properties swiftProperties = new Properties();
        String configLocation = GetUBConfigLocation.getUBConfigLocation();
        try {
            InputStream is = new FileInputStream(
                    configLocation + FromEssence_To_UBMMM.CONF + FromEssence_To_UBMMM.SWIFT_PROPERTY_FILENAME);
            swiftProperties.load(is);
        }
        catch (IOException ex) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_SWIFT_PROPERTIES_FILE_NOT_FOUND, new Object[] {}, new HashMap(),
                    BankFusionThreadLocal.getBankFusionEnvironment());
        }
        Node rootNode = document.getFirstChild();
        document.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        // tag : MeridianMessageType
        Node externalMessageType = document.getElementsByTagName("ExternalMessageType").item(0);
        Element meridianMessagetype = document.createElement("MeridianMessageType");
        if (externalMessageType.getTextContent().equals("MT940")) {
            meridianMessagetype.setTextContent("SWIFT_MT940");
            rootElement.setAttribute("MessageType", "SWIFT_MT940");
        }
        else if (externalMessageType.getTextContent().equals("MT950")) {
            meridianMessagetype.setTextContent("SWIFT_MT950");
            rootElement.setAttribute("MessageType", "SWIFT_MT950");
        }
        rootNode.appendChild(meridianMessagetype);

        // tag : ExternalMessageType

        // tag : InternalMessageType
        Element internalMessageType = document.createElement("InternalMessageType");
        internalMessageType.setTextContent("P");
        rootNode.appendChild(internalMessageType);

        // tag : HostType
        Element hostType = document.createElement("HostType");
        hostType.setTextContent("UB");
        rootNode.appendChild(hostType);

        // tag : HostID
        Element hostID = document.createElement("HostID");
        hostID.setTextContent(swiftProperties.getProperty("HostId"));
        rootNode.appendChild(hostID);

        // tag : HostReference

        // tag : Direction
        Element direction = document.createElement("Direction");
        direction.setTextContent("O");
        rootNode.appendChild(direction);

        // tag : TransactionReference

        // tag : Priority
        Element priority = document.createElement("Priority");
        if (externalMessageType.getTextContent().equals("MT940")) {
            priority.setTextContent("N");
        }
        else if (externalMessageType.getTextContent().equals("MT950")) {
            priority.setTextContent("U");
        }
        rootNode.appendChild(priority);

        // tag : SystemArrivalTime
        Element systemArrivalTime = document.createElement("SystemArrivalTime");
        SimpleDateFormat systemArrivalTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        systemArrivalTime.setTextContent(systemArrivalTimeFormat.format(date));
        rootNode.appendChild(systemArrivalTime);

        // tag : Network
        Element network = document.createElement("Network");
        network.setTextContent("SWIFT");
        rootNode.appendChild(network);

        // tag : SenderAddress
        Node senderAddress = document.getElementsByTagName("SenderAddress").item(0);
        if (senderAddress != null) {
            senderAddress.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(senderAddress.getTextContent()));
        }
        // tag : DestinationAddress

        // tag : LineOfBusiness
        Element lineOfBusinessNew = document.createElement("LineOfBusiness");
        lineOfBusinessNew.setTextContent("UB");
        rootNode.appendChild(lineOfBusinessNew);

        // tag : MultipleMessageStatus
        Element multipleMessageStatusNew = document.createElement("MultipleMessageStatus");
        multipleMessageStatusNew.setTextContent("I");
        rootNode.appendChild(multipleMessageStatusNew);

        // tag : CancellationAction

        // tag : Reconcile
        Node deliveryChannel = document.getElementsByTagName("DeliveryChannel").item(0);
        if (deliveryChannel != null) {
            Element reconcileNew = document.createElement("Reconcile");
            if (deliveryChannel.getTextContent().equals("RECON")) {
            	reconcileNew.setTextContent("Y");
            }
            else if(deliveryChannel.getTextContent().equals("BOTH")){
            	reconcileNew.setTextContent("B");
            }
            else{
            	reconcileNew.setTextContent("N");
            }
            rootNode.appendChild(reconcileNew);
        }

        // tags : B2IDeliveryMonitoring & B2IObsolescencePeriod
        Node priorityTemp = document.getElementsByTagName("Priority").item(0);
        if (externalMessageType.getTextContent().equals("MT940") && priorityTemp.getTextContent().equals("N")) {
            Element b2IDeliveryMonitoringNew = document.createElement("B2IDeliveryMonitoring");
            Element b2IObsolescencePeriod = document.createElement("B2IObsolescencePeriod");
            if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                b2IDeliveryMonitoringNew.setTextContent(swiftProperties.getProperty("B2IDeliveryMonitoring"));
                b2IObsolescencePeriod.setTextContent("020");
            }
            rootNode.appendChild(b2IDeliveryMonitoringNew);
            rootNode.appendChild(b2IObsolescencePeriod);
        }
        else if (externalMessageType.getTextContent().equals("MT950") && priorityTemp.getTextContent().equals("U")) {
            Element b2IDeliveryMonitoringNew = document.createElement("B2IDeliveryMonitoring");
            b2IDeliveryMonitoringNew.setTextContent("3");
            rootNode.appendChild(b2IDeliveryMonitoringNew);
            Element b2IObsolescencePeriod = document.createElement("B2IObsolescencePeriod");
            b2IObsolescencePeriod.setTextContent("003");
            rootNode.appendChild(b2IObsolescencePeriod);
        }
        if (document.getElementsByTagName("AccountIdP").item(0) != null) {
            document.getElementsByTagName("AccountIdP").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(document.getElementsByTagName("AccountIdP").item(0).getTextContent()));
        }
        // tag : B3ServiceId (from : serviceIdentifierId)

        // tag : TRN
        Node transactionReference = document.getElementsByTagName("TransactionReference").item(0);
        if (transactionReference != null) {
            Element tRN = document.createElement("TRN");
            tRN.setTextContent(transactionReference.getTextContent());
            rootNode.appendChild(tRN);
        }

        // tag : RelatedReference (appears only in 940)

        // tag : AccountId

        // tag : NumberSeq

        // tag : OpeningBalance and OpeningBalanceOption
        Node openingBalance = document.getElementsByTagName("openingBalance").item(0);
        Node openingBalanceOption = document.getElementsByTagName("openingBalanceOption").item(0);
        if (openingBalance != null && openingBalanceOption != null) {
            String openingBalanceOptionValue = openingBalanceOption.getTextContent();

            if (openingBalanceOptionValue.equals("F")) {
                openingBalance.setTextContent(SWT_Outgoing_Globals.formatDateForAmount(openingBalance.getTextContent()));
                document.renameNode(openingBalance, null, "OpBalF");
                rootNode.removeChild(openingBalanceOption);
            }
            else if (openingBalanceOptionValue.equals("M")) {
                openingBalance.setTextContent(SWT_Outgoing_Globals.formatDateForAmount(openingBalance.getTextContent()));
                document.renameNode(openingBalance, null, "OpBalM");
                rootNode.removeChild(openingBalanceOption);
            }
        }

        // tag : statementDetails for 940
        if (externalMessageType.getTextContent().equals("MT940")) {
            NodeList statementList = document.getElementsByTagName("Statement");
            for (int i = 0; i < statementList.getLength(); ++i) {
                Element statement = (Element) statementList.item(i);

                // child tag : StatementLine
                Node statementLine = statement.getElementsByTagName("StatementLine").item(0);
                if (statementLine != null) {
                    statementLine.setTextContent(SWT_Outgoing_Globals.formatDateForStatementLine(statementLine.getTextContent()));
                }

                // child tag : InfoAccOwner
                Node infoAccOwner = statement.getElementsByTagName("InfoAccOwner").item(0);
                if (infoAccOwner != null) {
                    infoAccOwner.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(infoAccOwner.getTextContent()));
                }
            }
        }

        // tag : StatementLine for 950
        if (externalMessageType.getTextContent().equals("MT950")) {
            NodeList statementList = document.getElementsByTagName("Statement");
            for (int i = 0; i < statementList.getLength(); ++i) {
                Element statement = (Element) statementList.item(i);

                // child tag : StatementLine
                Node statementLine = statement.getElementsByTagName("StatementLine").item(0);
                if (statementLine != null) {
                    statementLine.setTextContent(SWT_Outgoing_Globals.formatDateForStatementLine(statementLine.getTextContent()));
                }
            }
        }

        // tag : ClosingBalance and ClosingBalanceOption
        Node closingBalance = document.getElementsByTagName("closingBalance").item(0);
        Node closingBalanceOption = document.getElementsByTagName("closingBalanceOption").item(0);
        if (closingBalance != null && closingBalanceOption != null) {
            String closingBalanceOptionValue = closingBalanceOption.getTextContent();
            if (closingBalanceOptionValue.equals("F")) {
                closingBalance.setTextContent(SWT_Outgoing_Globals.formatDateForAmount(closingBalance.getTextContent()));
                document.renameNode(closingBalance, null, "CloBalF");
                rootNode.removeChild(closingBalanceOption);
            }
            else if (closingBalanceOptionValue.equals("M")) {
                closingBalance.setTextContent(SWT_Outgoing_Globals.formatDateForAmount(closingBalance.getTextContent()));
                document.renameNode(closingBalance, null, "CloBalM");
                rootNode.removeChild(closingBalanceOption);
            }
        }

        // tag : ClosingAvailableBalance
        Node cloAvailBal = document.getElementsByTagName("CloAvailBal").item(0);
        if (cloAvailBal != null) {
            cloAvailBal.setTextContent(SWT_Outgoing_Globals.formatDateForAmount(cloAvailBal.getTextContent()));
        }

        // tag : FwdAvailableBalance for 940

        // tag : InfoAccOwner for 940 only
        if (externalMessageType.getTextContent().equals("MT940")) {
            Node infoAccOwner = document.getElementsByTagName("InfoAccOwner").item(0);
            if (infoAccOwner != null) {
                infoAccOwner.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(infoAccOwner.getTextContent()));
            }
        }
    }

    /**
     * Method Description: Disallow the DTDs (doctypes) entirely.
     * @param dbf
     */
    private void setExternalEntity(DocumentBuilderFactory dbf) {

        try {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        }
        catch (ParserConfigurationException e) {
            LOGGER.error(ExceptionUtil.getExceptionAsString( e));
        }

    }

}
