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

public class UB_MT192_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT192_FileCreator.class.getName());
    private String strResult;
    private String FinalXml;

    public String MT192_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            ClassCastException, SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(requestMsg));
            Document document = dBuilder.parse(is);

            document.getDocumentElement().normalize();
            // update Element value
            Document resultDocument = updateElementValue(dBuilder, document);

            // write the updated document to forward request
            resultDocument.getDocumentElement().normalize();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DOMSource source = new DOMSource(resultDocument);
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

    public Document updateElementValue(DocumentBuilder dBuilder, Document document) {
        Document resultDocument = dBuilder.newDocument();

        Element resultRootElement = resultDocument.createElement("MeridianMessage");
        resultDocument.appendChild(resultRootElement);

        resultRootElement.setAttribute("MessageType", "SWIFT_MTn92");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        resultRootElement.setAttribute("Timestamp", timestamp.toString());
        resultRootElement.setAttribute("MessageFormat", "StandardXML");
        resultRootElement.setAttribute("System", "SWIFT");
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
        Element meridianMessagetype = resultDocument.createElement("MeridianMessageType");
        meridianMessagetype.setTextContent("SWIFT_MTn92");
        resultRootElement.appendChild(meridianMessagetype);

        // tag : ExternalMessageType
        Node externalMessageType = document.getElementsByTagName("ExternalMessageType").item(0);
        if (externalMessageType != null) {
            Element externalMessagetypeNew = resultDocument.createElement("ExternalMessageType");
            externalMessagetypeNew.setTextContent(externalMessageType.getTextContent());
            resultRootElement.appendChild(externalMessagetypeNew);
        }

        // tag : InternalMessageType
        Element internalMessageType = resultDocument.createElement("InternalMessageType");
        internalMessageType.setTextContent("P");
        resultRootElement.appendChild(internalMessageType);

        // tag : HostType
        Element hostType = resultDocument.createElement("HostType");
        hostType.setTextContent("UB");
        resultRootElement.appendChild(hostType);

        // tag : HostID
        Element hostID = resultDocument.createElement("HostID");
        hostID.setTextContent(swiftProperties.getProperty("HostId"));
        resultRootElement.appendChild(hostID);

        // tag : HostReference
        Node hostReference = document.getElementsByTagName("HostReference").item(0);
        if (hostReference != null) {
            Element hostReferenceNew = resultDocument.createElement("HostReference");
            hostReferenceNew.setTextContent(hostReference.getTextContent());
            resultRootElement.appendChild(hostReferenceNew);
        }

        // tag : Direction
        Element direction = resultDocument.createElement("Direction");
        direction.setTextContent("O");
        resultRootElement.appendChild(direction);

        // tag : Priority
        Element priority = resultDocument.createElement("Priority");
        priority.setTextContent("N");
        resultRootElement.appendChild(priority);

        // tag : Cancel
        Element cancel = resultDocument.createElement("Cancel");
        cancel.setTextContent("N");
        resultRootElement.appendChild(cancel);

        // tag : SystemArrivalTime
        Element systemArrivalTime = resultDocument.createElement("SystemArrivalTime");
        SimpleDateFormat systemArrivalTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        systemArrivalTime.setTextContent(systemArrivalTimeFormat.format(date));
        resultRootElement.appendChild(systemArrivalTime);

        // tag : Network
        Element network = resultDocument.createElement("Network");
        network.setTextContent("SWIFT");
        resultRootElement.appendChild(network);

        // tag : SenderAddress
        Node senderAddress = document.getElementsByTagName("SenderAddress").item(0);
        if (senderAddress != null) {
            Element senderAddressNew = resultDocument.createElement("SenderAddress");
            senderAddressNew.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(senderAddress.getTextContent()));
            resultRootElement.appendChild(senderAddressNew);
        }

        // tag : DestinationAddress
        Node destinationAddress = document.getElementsByTagName("DestinationAddress").item(0);
        if (destinationAddress != null) {
            Element destinationAddressNew = resultDocument.createElement("DestinationAddress");
            destinationAddressNew.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(destinationAddress.getTextContent()));
            resultRootElement.appendChild(destinationAddressNew);
        }

        // tag : LineOfBusiness
        Element lineOfBusinessNew = resultDocument.createElement("LineOfBusiness");
        lineOfBusinessNew.setTextContent("UB");
        resultRootElement.appendChild(lineOfBusinessNew);

        // tag : MultipleMessageStatus
        Element multipleMessageStatusNew = resultDocument.createElement("MultipleMessageStatus");
        multipleMessageStatusNew.setTextContent("I");
        resultRootElement.appendChild(multipleMessageStatusNew);

        // tag : CancellationAction
        Node cancellationAction = document.getElementsByTagName("CancellationAction").item(0);
        if (cancellationAction != null) {
            Element cancellationActionNew = resultDocument.createElement("CancellationAction");
            cancellationActionNew.setTextContent(cancellationAction.getTextContent());
            resultRootElement.appendChild(cancellationActionNew);
        }

        // tags : B2IDeliveryMonitoring & B2IObsolescencePeriod
        Node priorityTemp = resultDocument.getElementsByTagName("Priority").item(0);
        if (priorityTemp.getTextContent().equals("N")) {
            Element b2IDeliveryMonitoringNew = resultDocument.createElement("B2IDeliveryMonitoring");
            Element b2IObsolescencePeriod = resultDocument.createElement("B2IObsolescencePeriod");
            if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                b2IDeliveryMonitoringNew.setTextContent(swiftProperties.getProperty("B2IDeliveryMonitoring"));
                b2IObsolescencePeriod.setTextContent("020");
            }
            resultRootElement.appendChild(b2IDeliveryMonitoringNew);
            resultRootElement.appendChild(b2IObsolescencePeriod);
        }

        // tag : B3ServiceId
        Node serviceIdentifierId = document.getElementsByTagName("serviceIdentifierId").item(0);
        if (serviceIdentifierId != null) {
            Element b3ServiceIdNew = resultDocument.createElement("B3ServiceId");
            b3ServiceIdNew.setTextContent(serviceIdentifierId.getTextContent());
            resultRootElement.appendChild(b3ServiceIdNew);
        }

        // tag : TRN
        Node transactionReferenceNumber_20 = document.getElementsByTagName("TransactionReferenceNumber_20").item(0);
        if (transactionReferenceNumber_20 != null) {
            Element tRNNew = resultDocument.createElement("TRN");
            tRNNew.setTextContent(transactionReferenceNumber_20.getTextContent());
            resultRootElement.appendChild(tRNNew);
        }

        // tag : RelatedReference
        Node relatedReference_21 = document.getElementsByTagName("RelatedReference_21").item(0);
        if (relatedReference_21 != null) {
            Element relatedReferenceNew = resultDocument.createElement("RelatedReference");
            relatedReferenceNew.setTextContent(relatedReference_21.getTextContent());
            resultRootElement.appendChild(relatedReferenceNew);
        }

        // tag : MT_Date_OriginalMsgS
        Node tag_11S = document.getElementsByTagName("Tag_11S").item(0);
        if (tag_11S != null) {
            Element mt_Date_OriginalMsgSNew = resultDocument.createElement("MT_Date_OriginalMsgS");
            mt_Date_OriginalMsgSNew.setTextContent(tag_11S.getTextContent());
            resultRootElement.appendChild(mt_Date_OriginalMsgSNew);
        }

        // tag : Narrative_OriginalMsg
        Node narration_79 = document.getElementsByTagName("Narration_79").item(0);
        if (narration_79 != null) {
            Element narrative_OriginalMsgNew = resultDocument.createElement("Narrative_OriginalMsg");
            narrative_OriginalMsgNew.setTextContent(narration_79.getTextContent());
            resultRootElement.appendChild(narrative_OriginalMsgNew);
        }

        // tag : Fields_OriginalMsg
        Node bankOperationCode_23B = document.getElementsByTagName("BankOperationCode_23B").item(0);
        Node currencyCode = document.getElementsByTagName("CurrencyCode").item(0);
        Node amount = document.getElementsByTagName("Amount").item(0);
        Node valueDate = document.getElementsByTagName("ValueDate").item(0);
        Node tranDet_32A = document.getElementsByTagName("TranDet_32A").item(0);
        Node orderingCustomer_50 = document.getElementsByTagName("OrderingCustomer_50").item(0);
        Node tag_50 = document.getElementsByTagName("Tag_50").item(0);
        Node beneficiaryCustomer_59 = document.getElementsByTagName("BeneficiaryCustomer_59").item(0);
        Node detailsofCharges_71A = document.getElementsByTagName("DetailsofCharges_71A").item(0);

        if ((transactionReferenceNumber_20 != null) && (bankOperationCode_23B != null) && (currencyCode != null) && (amount != null)
                && (valueDate != null) && (tranDet_32A != null) && (orderingCustomer_50 != null) && (tag_50 != null)
                && (beneficiaryCustomer_59 != null) && (detailsofCharges_71A != null)) {

            Element fields_OriginalMsgNew = resultDocument.createElement("Fields_OriginalMsg");
            String fields_OriginalMsgValue = transactionReferenceNumber_20.getTextContent() + bankOperationCode_23B.getTextContent()
                    + currencyCode.getTextContent() + SWT_Outgoing_Globals.ReplacePeriodWithComma(amount.getTextContent())
                    + SWT_Outgoing_Globals.formatDate(valueDate.getTextContent()) + tranDet_32A.getTextContent()
                    + orderingCustomer_50.getTextContent() + tag_50.getTextContent() + beneficiaryCustomer_59.getTextContent()
                    + detailsofCharges_71A.getTextContent();
            fields_OriginalMsgNew.setTextContent(fields_OriginalMsgValue);
            resultRootElement.appendChild(fields_OriginalMsgNew);
        }

        return resultDocument;

    }

}
