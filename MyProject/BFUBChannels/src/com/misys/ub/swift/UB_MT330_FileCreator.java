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

import org.apache.commons.lang.StringUtils;
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

/**
 * 
 * @author Waseem
 * 
 *         file to transform MT330
 * 
 * 
 */

public class UB_MT330_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT330_FileCreator.class.getName());

    private String FinalXml;
    private String strResult;

    public String MT330_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
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
        rootElement.setAttribute("MessageType", "SWIFT_MT330");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        newTag = doc.createElement("MeridianMessageType");
        newTag.setTextContent("SWIFT_MT330");
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
        tempTag = doc.getElementsByTagName("SendersReference").item(0);
        if (tempTag != null) {
            newTag.setTextContent(doc.getElementsByTagName("SendersReference").item(0).getTextContent());
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

        tempTag = doc.getElementsByTagName("ValueDate").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("ValueDate").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.formatDate(tag.getTextContent()));
        }

        newTag = doc.createElement("Network");
        newTag.setTextContent("SWIFT");
        rootElement.appendChild(newTag);

        tempTag = doc.getElementsByTagName("sender").item(0);
        if (tempTag != null) {
            String source_Sender = doc.getElementsByTagName("sender").item(0).getTextContent();
            newTag = doc.createElement("SenderAddress");
            newTag.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(source_Sender));
            rootElement.appendChild(newTag);
            rootElement.removeChild(doc.getElementsByTagName("sender").item(0));
        }

        newTag = doc.createElement("LineOfBusiness");
        newTag.setTextContent("UB");
        rootElement.appendChild(newTag);

        newTag = doc.createElement("MultipleMessageStatus");
        newTag.setTextContent("I");
        rootElement.appendChild(newTag);

        // PaymentClearingCentre
        tempTag = doc.getElementsByTagName("PaymentClearingCentre").item(0);
        if (tempTag != null) {
            String paymentClearingCentre = doc.getElementsByTagName("PaymentClearingCentre").item(0).getTextContent();
            if (!StringUtils.isBlank(paymentClearingCentre)) {
                newTag = doc.createElement("PaymentClearingCentre");
                newTag.setTextContent(paymentClearingCentre);
                rootElement.appendChild(newTag);
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

        newTag = doc.createElement("B3ServiceId");
        tempTag = doc.getElementsByTagName("serviceIdentifierId").item(0);
        if (tempTag != null) {
            newTag.setTextContent(doc.getElementsByTagName("serviceIdentifierId").item(0).getTextContent());
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqA = false;

        tempTag = doc.getElementsByTagName("SendersReference").item(0);

        if (tempTag != null && doc.getElementsByTagName("SendersReference").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("RelatedReference").item(0);

        if (tempTag != null && doc.getElementsByTagName("RelatedReference").item(0).getTextContent() != "") {
            flag_SeqA = true;

        }

        tempTag = doc.getElementsByTagName("TypeOfOperation").item(0);

        if (tempTag != null && doc.getElementsByTagName("TypeOfOperation").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("ScopeOfOperation").item(0);

        if (tempTag != null && doc.getElementsByTagName("ScopeOfOperation").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("TypeOfEvent").item(0);

        if (tempTag != null && doc.getElementsByTagName("TypeOfEvent").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("CommonReference").item(0);

        if (tempTag != null && doc.getElementsByTagName("CommonReference").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("ContractNumberPartyA").item(0);

        if (tempTag != null && doc.getElementsByTagName("ContractNumberPartyA").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("partyA").item(0);

        if (tempTag != null && doc.getElementsByTagName("partyA").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("partyAOption").item(0);
        if (tempTag != null) {
            String source_partyAOption = doc.getElementsByTagName("partyAOption").item(0).getTextContent();
            if (source_partyAOption != "") {
                if ("A".equals(source_partyAOption)) {
                    temp_name = "PartyA_A";
                }
                else if ("D".equals(source_partyAOption)) {
                    temp_name = "PartyA_D";
                }
                else if ("J".equals(source_partyAOption)) {
                    temp_name = "PartyA_J";
                }
                doc.renameNode(doc.getElementsByTagName("partyAOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("partyA").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0)
                            .setTextContent(doc.getElementsByTagName("partyA").item(0).getTextContent());
                    rootElement.removeChild(doc.getElementsByTagName("partyA").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("partyB").item(0);

        if (tempTag != null && doc.getElementsByTagName("partyB").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("partyBOption").item(0);
        if (tempTag != null) {
            String source_partyBOption = doc.getElementsByTagName("partyBOption").item(0).getTextContent();
            if (source_partyBOption != "") {
                if ("A".equals(source_partyBOption)) {
                    temp_name = "PartyB_A";
                }
                else if ("D".equals(source_partyBOption)) {
                    temp_name = "PartyB_D";
                }
                else if ("J".equals(source_partyBOption)) {
                    temp_name = "PartyB_J";
                }
                doc.renameNode(doc.getElementsByTagName("partyBOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("partyB").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0)
                            .setTextContent(doc.getElementsByTagName("partyB").item(0).getTextContent());
                    rootElement.removeChild(doc.getElementsByTagName("partyB").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("fundOrInstructingParty").item(0);

        if (tempTag != null && doc.getElementsByTagName("fundOrInstructingParty").item(0).getTextContent() != "") {
            flag_SeqA = true;
        }

        tempTag = doc.getElementsByTagName("fundOrInstPartyOption").item(0);
        if (tempTag != null) {
            String source_fundOrInstPartyOption = doc.getElementsByTagName("fundOrInstPartyOption").item(0).getTextContent();
            if (source_fundOrInstPartyOption != "") {
                if ("A".equals(source_fundOrInstPartyOption)) {
                    temp_name = "FundOrInstructingPartyA";
                }
                else if ("D".equals(source_fundOrInstPartyOption)) {
                    temp_name = "FundOrInstructingPartyD";
                }
                else if ("J".equals(source_fundOrInstPartyOption)) {
                    temp_name = "FundOrInstructingPartyJ";
                }
                doc.renameNode(doc.getElementsByTagName("fundOrInstPartyOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("fundOrInstructingParty").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0)
                            .setTextContent(doc.getElementsByTagName("fundOrInstructingParty").item(0).getTextContent());
                    rootElement.removeChild(doc.getElementsByTagName("fundOrInstructingParty").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("TermsAndConditions").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("TermsAndConditions").item(0).getTextContent() != "") {
                flag_SeqA = true;
            }
            tag = doc.getElementsByTagName("TermsAndConditions").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(tag.getTextContent()));
        }

        if (flag_SeqA) {
            newTag = doc.createElement("NewSequenceA");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqB = false;

        tempTag = doc.getElementsByTagName("PartyARole").item(0);

        if (tempTag != null && doc.getElementsByTagName("PartyARole").item(0).getTextContent() != "") {
            flag_SeqB = true;
        }

        tempTag = doc.getElementsByTagName("TradeDate").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("TradeDate").item(0).getTextContent() != "") {
                flag_SeqB = true;
            }
            tag = doc.getElementsByTagName("TradeDate").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.formatDate(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("ValueDate").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("ValueDate").item(0).getTextContent() != "") {
                flag_SeqB = true;
            }
            tag = doc.createElement("B4ValueDate");
            tag.setTextContent(tempTag.getTextContent());
            rootElement.appendChild(tag);
        }

        tempTag = doc.getElementsByTagName("PeriodOfNotice").item(0);

        if (tempTag != null && doc.getElementsByTagName("PeriodOfNotice").item(0).getTextContent() != "") {
            flag_SeqB = true;
        }

        tempTag = doc.getElementsByTagName("CurrencyBalance").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("CurrencyBalance").item(0).getTextContent() != "") {
                flag_SeqB = true;
            }
            tag = doc.getElementsByTagName("CurrencyBalance").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("AmountSettled").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("AmountSettled").item(0).getTextContent() != "") {
                flag_SeqB = true;
            }
            tag = doc.getElementsByTagName("AmountSettled").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("InterestDueDate").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("InterestDueDate").item(0).getTextContent() != "") {
                flag_SeqB = true;
            }
            tag = doc.getElementsByTagName("InterestDueDate").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.formatDate(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("CcyAndInterestAmount").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("CcyAndInterestAmount").item(0).getTextContent() != "") {
                flag_SeqB = true;
            }
            tag = doc.getElementsByTagName("CcyAndInterestAmount").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("InterestRate").item(0);
        if (tempTag != null) {
            if (doc.getElementsByTagName("InterestRate").item(0).getTextContent() != "") {
                flag_SeqB = true;
            }
            tag = doc.getElementsByTagName("InterestRate").item(0);
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("DayCountFraction").item(0);

        if (tempTag != null && doc.getElementsByTagName("DayCountFraction").item(0).getTextContent() != "") {
            flag_SeqB = true;
        }

        tempTag = doc.getElementsByTagName("LastDayNextInterestPeriod").item(0);

        if (tempTag != null && doc.getElementsByTagName("LastDayNextInterestPeriod").item(0).getTextContent() != "") {
            flag_SeqB = true;
        }

        tempTag = doc.getElementsByTagName("NumberOfDays").item(0);

        if (tempTag != null && doc.getElementsByTagName("NumberOfDays").item(0).getTextContent() != "") {
            flag_SeqB = true;
        }

        if (flag_SeqB) {
            newTag = doc.createElement("NewSequenceB");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqC = false;

        tempTag = doc.getElementsByTagName("cDeliveryAgent").item(0);

        if (tempTag != null && doc.getElementsByTagName("cDeliveryAgent").item(0).getTextContent() != "") {
            flag_SeqC = true;
        }

        tempTag = doc.getElementsByTagName("cDeliveryAgentOption").item(0);
        if (tempTag != null) {
            String source_cDeliveryAgentOption = doc.getElementsByTagName("cDeliveryAgentOption").item(0).getTextContent();
            if (source_cDeliveryAgentOption != "") {
                if ("A".equals(source_cDeliveryAgentOption)) {
                    temp_name = "CDeliveryAgentA";
                }
                else if ("D".equals(source_cDeliveryAgentOption)) {
                    temp_name = "CDeliveryAgentD";
                }
                else if ("J".equals(source_cDeliveryAgentOption)) {
                    temp_name = "CDeliveryAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("cDeliveryAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("cDeliveryAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("cDeliveryAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("cDeliveryAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("cIntermediary2").item(0);

        if (tempTag != null && doc.getElementsByTagName("cIntermediary2").item(0).getTextContent() != "") {
            flag_SeqC = true;
        }

        tempTag = doc.getElementsByTagName("cIntermediary2Option").item(0);
        if (tempTag != null) {
            String source_cIntermediary2Option = doc.getElementsByTagName("cIntermediary2Option").item(0).getTextContent();
            if (source_cIntermediary2Option != "") {
                if ("A".equals(source_cIntermediary2Option)) {
                    temp_name = "CIntermediary2A";
                }
                else if ("D".equals(source_cIntermediary2Option)) {
                    temp_name = "CIntermediary2D";
                }
                else if ("J".equals(source_cIntermediary2Option)) {
                    temp_name = "CIntermediary2J";
                }
                doc.renameNode(doc.getElementsByTagName("cIntermediary2Option").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("cIntermediary2").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("cIntermediary2").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("cIntermediary2").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("cIntermediary").item(0);

        if (tempTag != null && doc.getElementsByTagName("cIntermediary").item(0).getTextContent() != "") {
            flag_SeqC = true;
        }

        tempTag = doc.getElementsByTagName("cIntermediary").item(0);
        if (tempTag != null) {
            String source_cIntermediaryOption = doc.getElementsByTagName("cIntermediaryOption").item(0).getTextContent();
            if (source_cIntermediaryOption != "") {
                if ("A".equals(source_cIntermediaryOption)) {
                    temp_name = "CIntermediaryA";
                }
                else if ("D".equals(source_cIntermediaryOption)) {
                    temp_name = "CIntermediaryD";
                }
                else if ("J".equals(source_cIntermediaryOption)) {
                    temp_name = "CIntermediaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("cIntermediary").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("cIntermediary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("cIntermediary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("cIntermediary").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("cReceivingAgent").item(0);

        if (tempTag != null && doc.getElementsByTagName("cReceivingAgent").item(0).getTextContent() != "") {
            flag_SeqC = true;
        }

        tempTag = doc.getElementsByTagName("cReceivingAgent").item(0);
        if (tempTag != null) {
            String source_cReceivingAgentOption = doc.getElementsByTagName("cReceivingAgentOption").item(0).getTextContent();
            if (source_cReceivingAgentOption != "") {
                if ("A".equals(source_cReceivingAgentOption)) {
                    temp_name = "CReceivingAgentA";
                }
                else if ("D".equals(source_cReceivingAgentOption)) {
                    temp_name = "CReceivingAgentD";
                }
                else if ("J".equals(source_cReceivingAgentOption)) {
                    temp_name = "CReceivingAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("cReceivingAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("cReceivingAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("cReceivingAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("cReceivingAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("cBeneficiary").item(0);

        if (tempTag != null && doc.getElementsByTagName("cBeneficiary").item(0).getTextContent() != "") {
            flag_SeqC = true;
        }

        tempTag = doc.getElementsByTagName("cBeneficiaryOption").item(0);
        if (tempTag != null) {
            String source_cBeneficiaryOption = doc.getElementsByTagName("cBeneficiaryOption").item(0).getTextContent();
            if (source_cBeneficiaryOption != "") {
                if ("A".equals(source_cBeneficiaryOption)) {
                    temp_name = "CBeneficiaryA";
                }
                else if ("D".equals(source_cBeneficiaryOption)) {
                    temp_name = "CBeneficiaryD";
                }
                else if ("J".equals(source_cBeneficiaryOption)) {
                    temp_name = "CBeneficiaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("cBeneficiaryOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("cBeneficiary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("cBeneficiary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("cBeneficiary").item(0));
                }
            }
        }

        if (flag_SeqC) {
            newTag = doc.createElement("NewSequenceC");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqD = false;

        tempTag = doc.getElementsByTagName("dDeliveryAgent").item(0);

        if (tempTag != null && doc.getElementsByTagName("dDeliveryAgent").item(0).getTextContent() != "") {
            flag_SeqD = true;

        }
        tempTag = doc.getElementsByTagName("dDeliveryAgentOption").item(0);
        if (tempTag != null) {
            String source_dDeliveryAgentOption = doc.getElementsByTagName("dDeliveryAgentOption").item(0).getTextContent();
            if (source_dDeliveryAgentOption != "") {
                if ("A".equals(source_dDeliveryAgentOption)) {
                    temp_name = "DDeliveryAgentA";
                }
                else if ("D".equals(source_dDeliveryAgentOption)) {
                    temp_name = "DDeliveryAgentD";
                }
                else if ("J".equals(source_dDeliveryAgentOption)) {
                    temp_name = "DDeliveryAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("dDeliveryAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("dDeliveryAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("dDeliveryAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("dDeliveryAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("dIntermediary2").item(0);

        if (tempTag != null && doc.getElementsByTagName("dIntermediary2").item(0).getTextContent() != "") {
            flag_SeqD = true;
        }

        tempTag = doc.getElementsByTagName("dIntermediary2Option").item(0);
        if (tempTag != null) {
            String source_dIntermediary2Option = doc.getElementsByTagName("dIntermediary2Option").item(0).getTextContent();
            if (source_dIntermediary2Option != "") {
                if ("A".equals(source_dIntermediary2Option)) {
                    temp_name = "DIntermediary2A";
                }
                else if ("D".equals(source_dIntermediary2Option)) {
                    temp_name = "DIntermediary2D";
                }
                else if ("J".equals(source_dIntermediary2Option)) {
                    temp_name = "DIntermediary2J";
                }
                doc.renameNode(doc.getElementsByTagName("dIntermediary2Option").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("dIntermediary2").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("dIntermediary2").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("dIntermediary2").item(0));
                }
            }
        }
        tempTag = doc.getElementsByTagName("dIntermediary").item(0);

        if (tempTag != null && doc.getElementsByTagName("dIntermediary").item(0).getTextContent() != "") {
            flag_SeqD = true;
        }

        tempTag = doc.getElementsByTagName("dIntermediaryOption").item(0);
        if (tempTag != null) {
            String source_dIntermediaryOption = doc.getElementsByTagName("dIntermediaryOption").item(0).getTextContent();
            if (source_dIntermediaryOption != "") {
                if ("A".equals(source_dIntermediaryOption)) {
                    temp_name = "DIntermediaryA";
                }
                else if ("D".equals(source_dIntermediaryOption)) {
                    temp_name = "DIntermediaryD";
                }
                else if ("J".equals(source_dIntermediaryOption)) {
                    temp_name = "DIntermediaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("dIntermediaryOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("dIntermediary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("dIntermediary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("dIntermediary").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("dReceivingAgent").item(0);

        if (tempTag != null && doc.getElementsByTagName("dReceivingAgent").item(0).getTextContent() != "") {
            flag_SeqD = true;
        }

        tempTag = doc.getElementsByTagName("dReceivingAgentOption").item(0);
        if (tempTag != null) {
            String source_dReceivingAgentOption = doc.getElementsByTagName("dReceivingAgentOption").item(0).getTextContent();
            if (source_dReceivingAgentOption != "") {
                if ("A".equals(source_dReceivingAgentOption)) {
                    temp_name = "DReceivingAgentA";
                }
                else if ("D".equals(source_dReceivingAgentOption)) {
                    temp_name = "DReceivingAgentD";
                }
                else if ("J".equals(source_dReceivingAgentOption)) {
                    temp_name = "DReceivingAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("dReceivingAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("dReceivingAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("dReceivingAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("dReceivingAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("dBeneficiary").item(0);

        if (tempTag != null && doc.getElementsByTagName("dBeneficiary").item(0).getTextContent() != "") {
            flag_SeqD = true;
        }

        tempTag = doc.getElementsByTagName("dBeneficiaryOption").item(0);
        if (tempTag != null) {
            String source_dBeneficiaryOption = doc.getElementsByTagName("dBeneficiaryOption").item(0).getTextContent();
            if (source_dBeneficiaryOption != "") {
                if ("A".equals(source_dBeneficiaryOption)) {
                    temp_name = "DBeneficiaryA";
                }
                else if ("D".equals(source_dBeneficiaryOption)) {
                    temp_name = "DBeneficiaryD";
                }
                else if ("J".equals(source_dBeneficiaryOption)) {
                    temp_name = "DBeneficiaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("dBeneficiaryOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("dBeneficiary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("dBeneficiary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("dBeneficiary").item(0));
                }
            }
        }

        if (flag_SeqD) {
            newTag = doc.createElement("NewSequenceD");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqE = false;

        tempTag = doc.getElementsByTagName("eDeliveryAgent").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("eDeliveryAgent").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqE = true;
            }
        }
        tempTag = doc.getElementsByTagName("eDeliveryAgentOption").item(0);
        if (tempTag != null) {
            String source_eDeliveryAgentOption = doc.getElementsByTagName("eDeliveryAgentOption").item(0).getTextContent();
            if (source_eDeliveryAgentOption != "") {
                if ("A".equals(source_eDeliveryAgentOption)) {
                    temp_name = "EDeliveryAgentA";
                }
                else if ("D".equals(source_eDeliveryAgentOption)) {
                    temp_name = "EDeliveryAgentD";
                }
                else if ("J".equals(source_eDeliveryAgentOption)) {
                    temp_name = "EDeliveryAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("eDeliveryAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("eDeliveryAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("eDeliveryAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("eDeliveryAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("eIntermediary2").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("eIntermediary2").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqE = true;
            }
        }
        tempTag = doc.getElementsByTagName("eIntermediary2Option").item(0);
        if (tempTag != null) {
            String source_eIntermediary2Option = doc.getElementsByTagName("eIntermediary2Option").item(0).getTextContent();
            if (source_eIntermediary2Option != "") {
                if ("A".equals(source_eIntermediary2Option)) {
                    temp_name = "EIntermediary2A";
                }
                else if ("D".equals(source_eIntermediary2Option)) {
                    temp_name = "EIntermediary2D";
                }
                else if ("J".equals(source_eIntermediary2Option)) {
                    temp_name = "EIntermediary2J";
                }
                doc.renameNode(doc.getElementsByTagName("eIntermediary2Option").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("eIntermediary2").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("eIntermediary2").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("eIntermediary2").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("eIntermediary").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("eIntermediary").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqE = true;
            }
        }
        tempTag = doc.getElementsByTagName("eIntermediaryOption").item(0);
        if (tempTag != null) {
            String source_eIntermediaryOption = doc.getElementsByTagName("eIntermediaryOption").item(0).getTextContent();
            if (source_eIntermediaryOption != "") {
                if ("A".equals(source_eIntermediaryOption)) {
                    temp_name = "EIntermediaryA";
                }
                else if ("D".equals(source_eIntermediaryOption)) {
                    temp_name = "EIntermediaryD";
                }
                else if ("J".equals(source_eIntermediaryOption)) {
                    temp_name = "EIntermediaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("eIntermediaryOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("eIntermediary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("eIntermediary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("eIntermediary").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("eReceivingAgent").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("eReceivingAgent").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqE = true;
            }
        }
        tempTag = doc.getElementsByTagName("eReceivingAgentOption").item(0);
        if (tempTag != null) {
            String source_eReceivingAgentOption = doc.getElementsByTagName("eReceivingAgentOption").item(0).getTextContent();
            if (source_eReceivingAgentOption != "") {
                if ("A".equals(source_eReceivingAgentOption)) {
                    temp_name = "EReceivingAgentA";
                }
                else if ("D".equals(source_eReceivingAgentOption)) {
                    temp_name = "EReceivingAgentD";
                }
                else if ("J".equals(source_eReceivingAgentOption)) {
                    temp_name = "EReceivingAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("eReceivingAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("eReceivingAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("eReceivingAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("eReceivingAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("eBeneficiary").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("eBeneficiary").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqE = true;
            }
        }
        tempTag = doc.getElementsByTagName("eBeneficiaryOption").item(0);
        if (tempTag != null) {
            String source_eBeneficiaryOption = doc.getElementsByTagName("eBeneficiaryOption").item(0).getTextContent();
            if (source_eBeneficiaryOption != "") {
                if ("A".equals(source_eBeneficiaryOption)) {
                    temp_name = "EBeneficiaryA";
                }
                else if ("D".equals(source_eBeneficiaryOption)) {
                    temp_name = "EBeneficiaryD";
                }
                else if ("J".equals(source_eBeneficiaryOption)) {
                    temp_name = "EBeneficiaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("eBeneficiaryOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("eBeneficiary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("eBeneficiary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("eBeneficiary").item(0));
                }
            }
        }
        if (flag_SeqE) {
            newTag = doc.createElement("NewSequenceE");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqF = false;

        tempTag = doc.getElementsByTagName("fDeliveryAgent").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("fDeliveryAgent").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqF = true;
            }
        }
        tempTag = doc.getElementsByTagName("fDeliveryAgentOption").item(0);
        if (tempTag != null) {
            String source_fDeliveryAgentOption = doc.getElementsByTagName("fDeliveryAgentOption").item(0).getTextContent();
            if (source_fDeliveryAgentOption != "") {
                if ("A".equals(source_fDeliveryAgentOption)) {
                    temp_name = "FDeliveryAgentA";
                }
                else if ("D".equals(source_fDeliveryAgentOption)) {
                    temp_name = "FDeliveryAgentD";
                }
                else if ("J".equals(source_fDeliveryAgentOption)) {
                    temp_name = "FDeliveryAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("fDeliveryAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("fDeliveryAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("fDeliveryAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("fDeliveryAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("fIntermediary2").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("fIntermediary2").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqF = true;
            }
        }
        tempTag = doc.getElementsByTagName("fIntermediary2Option").item(0);
        if (tempTag != null) {
            String source_fIntermediary2Option = doc.getElementsByTagName("fIntermediary2Option").item(0).getTextContent();
            if (source_fIntermediary2Option != "") {
                if ("A".equals(source_fIntermediary2Option)) {
                    temp_name = "FIntermediary2A";
                }
                else if ("D".equals(source_fIntermediary2Option)) {
                    temp_name = "FIntermediary2D";
                }
                else if ("J".equals(source_fIntermediary2Option)) {
                    temp_name = "FIntermediary2J";
                }
                doc.renameNode(doc.getElementsByTagName("fIntermediary2Option").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("fIntermediary2").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("fIntermediary2").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("fIntermediary2").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("fIntermediary").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("fIntermediary").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqF = true;
            }
        }
        tempTag = doc.getElementsByTagName("fIntermediaryOption").item(0);
        if (tempTag != null) {
            String source_fIntermediaryOption = doc.getElementsByTagName("fIntermediaryOption").item(0).getTextContent();
            if (source_fIntermediaryOption != "") {
                if ("A".equals(source_fIntermediaryOption)) {
                    temp_name = "FIntermediaryA";
                }
                else if ("D".equals(source_fIntermediaryOption)) {
                    temp_name = "FIntermediaryD";
                }
                else if ("J".equals(source_fIntermediaryOption)) {
                    temp_name = "FIntermediaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("fIntermediaryOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("fIntermediary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("fIntermediary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("fIntermediary").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("fReceivingAgent").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("fReceivingAgent").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqF = true;
            }
        }
        tempTag = doc.getElementsByTagName("fReceivingAgentOption").item(0);
        if (tempTag != null) {
            String source_fReceivingAgentOption = doc.getElementsByTagName("fReceivingAgentOption").item(0).getTextContent();
            if (source_fReceivingAgentOption != "") {
                if ("A".equals(source_fReceivingAgentOption)) {
                    temp_name = "FReceivingAgentA";
                }
                else if ("D".equals(source_fReceivingAgentOption)) {
                    temp_name = "FReceivingAgentD";
                }
                else if ("J".equals(source_fReceivingAgentOption)) {
                    temp_name = "FReceivingAgentJ";
                }
                doc.renameNode(doc.getElementsByTagName("fReceivingAgentOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("fReceivingAgent").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("fReceivingAgent").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("fReceivingAgent").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("fBeneficiary").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("fBeneficiary").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqF = true;
            }
        }
        tempTag = doc.getElementsByTagName("fBeneficiaryOption").item(0);
        if (tempTag != null) {
            String source_fBeneficiaryOption = doc.getElementsByTagName("fBeneficiaryOption").item(0).getTextContent();
            if (source_fBeneficiaryOption != "") {
                if ("A".equals(source_fBeneficiaryOption)) {
                    temp_name = "FBeneficiaryA";
                }
                else if ("D".equals(source_fBeneficiaryOption)) {
                    temp_name = "FBeneficiaryD";
                }
                else if ("J".equals(source_fBeneficiaryOption)) {
                    temp_name = "FBeneficiaryJ";
                }
                doc.renameNode(doc.getElementsByTagName("fBeneficiaryOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("fBeneficiary").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0).setTextContent(SWT_Outgoing_Globals
                            .RemoveUBDelimiter(doc.getElementsByTagName("fBeneficiary").item(0).getTextContent()));
                    rootElement.removeChild(doc.getElementsByTagName("fBeneficiary").item(0));
                }
            }
        }

        if (flag_SeqF) {
            newTag = doc.createElement("NewSequenceF");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqG = false;

        tempTag = doc.getElementsByTagName("TaxRate").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("TaxRate").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqG = true;
            }
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("TransactionCcyAndNetIntAmt").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("TransactionCcyAndNetIntAmt").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqG = true;
            }
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("ReportingCcyTaxAmount").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("ReportingCcyTaxAmount").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqG = true;
            }
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        if (flag_SeqG) {
            newTag = doc.createElement("NewSequenceG");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }

        boolean flag_SeqH = false;

        tempTag = doc.getElementsByTagName("ContactInformation").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("ContactInformation").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqH = true;
            }
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("DealingMethod").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("DealingMethod").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqH = true;
            }
            tag.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("dealingBranchPartyA").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("dealingBranchPartyA").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqH = true;
            }
        }
        tempTag = doc.getElementsByTagName("dealingBranchPartyAOption").item(0);
        if (tempTag != null) {
            String source_dealingBranchPartyAOption = doc.getElementsByTagName("dealingBranchPartyAOption").item(0)
                    .getTextContent();
            if (source_dealingBranchPartyAOption != "") {
                if ("A".equals(source_dealingBranchPartyAOption)) {
                    temp_name = "DealingBranchPartyA_A";
                }
                else if ("B".equals(source_dealingBranchPartyAOption)) {
                    temp_name = "DealingBranchPartyA_B";
                }
                else if ("D".equals(source_dealingBranchPartyAOption)) {
                    temp_name = "DealingBranchPartyA_D";
                }
                else if ("J".equals(source_dealingBranchPartyAOption)) {
                    temp_name = "DealingBranchPartyA_J";
                }
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyAOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("dealingBranchPartyA").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0)
                            .setTextContent(doc.getElementsByTagName("dealingBranchPartyA").item(0).getTextContent());
                    rootElement.removeChild(doc.getElementsByTagName("dealingBranchPartyA").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("dealingBranchPartyB").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("dealingBranchPartyB").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqH = true;
            }
        }
        tempTag = doc.getElementsByTagName("dealingBranchPartyBOption").item(0);
        if (tempTag != null) {
            String source_dealingBranchPartyBOption = doc.getElementsByTagName("dealingBranchPartyBOption").item(0)
                    .getTextContent();
            if (source_dealingBranchPartyBOption != "") {
                if ("A".equals(source_dealingBranchPartyBOption)) {
                    temp_name = "DealingBranchPartyB_A";
                }
                else if ("B".equals(source_dealingBranchPartyBOption)) {
                    temp_name = "DealingBranchPartyB_B";
                }
                else if ("D".equals(source_dealingBranchPartyBOption)) {
                    temp_name = "DealingBranchPartyB_D";
                }
                else if ("J".equals(source_dealingBranchPartyBOption)) {
                    temp_name = "DealingBranchPartyB_J";
                }
                doc.renameNode(doc.getElementsByTagName("dealingBranchPartyBOption").item(0), null, temp_name);
                doc.getElementsByTagName(temp_name).item(0).setTextContent("");
                if (doc.getElementsByTagName("dealingBranchPartyB").item(0) != null) {
                    doc.getElementsByTagName(temp_name).item(0)
                            .setTextContent(doc.getElementsByTagName("dealingBranchPartyB").item(0).getTextContent());
                    rootElement.removeChild(doc.getElementsByTagName("dealingBranchPartyB").item(0));
                }
            }
        }

        tempTag = doc.getElementsByTagName("CounterpartysRef").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("CounterpartysRef").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqH = true;
            }
            tag.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(tag.getTextContent()));
        }

        tempTag = doc.getElementsByTagName("SenderToReceiverInfo").item(0);
        if (tempTag != null) {
            tag = doc.getElementsByTagName("SenderToReceiverInfo").item(0);
            if (!tag.getTextContent().isEmpty() && tag.getTextContent() != null) {
                flag_SeqH = true;
            }
            tag.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(tag.getTextContent()));
        }

        if (flag_SeqH) {
            newTag = doc.createElement("NewSequenceH");
            newTag.setTextContent("@");
            rootElement.appendChild(newTag);
        }
    }
}