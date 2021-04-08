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

/**
 * @author Rishav
 * 
 *         file to transform MT320
 */

public class UB_MT320_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT320_FileCreator.class.getName());
    private String strResult;
    private String FinalXml;

    public String MT320_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            ClassCastException, SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
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

    public void updateElementValue(Document document) {
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
        Node rootNode = document.getFirstChild();
        document.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        rootElement.setAttribute("MessageType", "SWIFT_MT320");
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        // tag : MeridianMessageType
        Element meridianMessagetype = document.createElement("MeridianMessageType");
        meridianMessagetype.setTextContent("SWIFT_MT320");
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
        Node sendersReference = document.getElementsByTagName("SendersReference").item(0);
        if (sendersReference != null) {
            Element transactionReferenceNew = document.createElement("TransactionReference");
            transactionReferenceNew.appendChild(document.createTextNode(sendersReference.getTextContent()));
            rootNode.appendChild(transactionReferenceNew);
        }

        // tag : Priority
        Element priority = document.createElement("Priority");
        priority.setTextContent("N");
        rootNode.appendChild(priority);

        // tag : SystemArrivalTime
        Element systemArrivalTime = document.createElement("SystemArrivalTime");
        SimpleDateFormat systemArrivalTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        systemArrivalTime.setTextContent(systemArrivalTimeFormat.format(date));
        rootNode.appendChild(systemArrivalTime);

        // tag : ValueDate
        Node valueDate = document.getElementsByTagName("ValueDate").item(0);
        if (valueDate != null) {
            valueDate.setTextContent(SWT_Outgoing_Globals.formatDate(valueDate.getTextContent()));
        }

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

        // tags : B2IDeliveryMonitoring & B2IObsolescencePeriod
        Node priorityTemp = document.getElementsByTagName("Priority").item(0);
        if (priorityTemp.getTextContent().equals("N")) {
            Element b2IDeliveryMonitoringNew = document.createElement("B2IDeliveryMonitoring");
            Element b2IObsolescencePeriod = document.createElement("B2IObsolescencePeriod");
            if ("2".equals(swiftProperties.getProperty("B2IDeliveryMonitoring"))) {
                b2IDeliveryMonitoringNew.setTextContent(swiftProperties.getProperty("B2IDeliveryMonitoring"));
                b2IObsolescencePeriod.setTextContent("020");
            }
            rootNode.appendChild(b2IDeliveryMonitoringNew);
            rootNode.appendChild(b2IObsolescencePeriod);
        }

        // starting Sequence A
        boolean flag_SeqA = false;

        // tag : SenderReference
        if (sendersReference != null && !sendersReference.getTextContent().equals("")) {
            flag_SeqA = true;
        }

        // tag : RelatedReference
        Node relatedReference = document.getElementsByTagName("RelatedReference").item(0);
        if (relatedReference != null && !relatedReference.getTextContent().equals("")) {
            flag_SeqA = true;
        }

        // tag : TypeOfOperation
        Node typeOfOperation = document.getElementsByTagName("TypeOfOperation").item(0);
        if (typeOfOperation != null && !typeOfOperation.getTextContent().equals("")) {
            flag_SeqA = true;
        }

        // tag : ScopeOfOperation
        Node scopeOfOperation = document.getElementsByTagName("ScopeOfOperation").item(0);
        if (scopeOfOperation != null && !scopeOfOperation.getTextContent().equals("")) {
            flag_SeqA = true;
        }

        // tag : TypeOfEvent
        Node typeOfEvent = document.getElementsByTagName("TypeOfEvent").item(0);
        if (typeOfEvent != null && !typeOfEvent.getTextContent().equals("")) {
            flag_SeqA = true;
        }

        // tag : CommonReference
        Node commonReference = document.getElementsByTagName("CommonReference").item(0);
        if (commonReference != null && !commonReference.getTextContent().equals("")) {
            flag_SeqA = true;
        }

        // tag : ContractNumberPartyA
        Node contractNumberPartyA = document.getElementsByTagName("ContractNumberPartyA").item(0);
        if (contractNumberPartyA != null && !contractNumberPartyA.getTextContent().equals("")) {
            flag_SeqA = true;
        }

        // tag : PartyA
        Node partyA = document.getElementsByTagName("partyA").item(0);
        Node partyAOption = document.getElementsByTagName("partyAOption").item(0);
        if (partyA != null && !partyA.getTextContent().equals("")) {
            flag_SeqA = true;
            if (partyAOption != null) {
                String partyAOptionValue = partyAOption.getTextContent();
                if (partyAOptionValue.equals("A")) {
                    document.renameNode(partyA, null, "PartyA_A");
                    rootNode.removeChild(partyAOption);
                }
                else if (partyAOptionValue.equals("D")) {
                    document.renameNode(partyA, null, "PartyA_D");
                    rootNode.removeChild(partyAOption);
                }
                else if (partyAOptionValue.equals("J")) {
                    document.renameNode(partyA, null, "PartyA_J");
                    rootNode.removeChild(partyAOption);
                }
            }
        }
        // tag : PartyB
        Node partyB = document.getElementsByTagName("partyB").item(0);
        Node partyBOption = document.getElementsByTagName("partyBOption").item(0);
        if (partyB != null && !partyB.getTextContent().equals("")) {
            flag_SeqA = true;
            if (partyBOption != null) {
                String partyBOptionValue = partyBOption.getTextContent();

                if (partyBOptionValue.equals("A")) {
                    document.renameNode(partyB, null, "PartyB_A");
                    rootNode.removeChild(partyBOption);
                }
                else if (partyBOptionValue.equals("D")) {
                    document.renameNode(partyB, null, "PartyB_D");
                    rootNode.removeChild(partyBOption);
                }
                else if (partyBOptionValue.equals("J")) {
                    document.renameNode(partyB, null, "PartyB_J");
                    rootNode.removeChild(partyBOption);
                }
            }
        }

        // tag : FundOrInstructingParty
        Node fundOrInstructingParty = document.getElementsByTagName("fundOrInstructingParty").item(0);
        Node fundOrInstPartyOption = document.getElementsByTagName("fundOrInstPartyOption").item(0);
        if (fundOrInstructingParty != null && !fundOrInstructingParty.getTextContent().equals("")) {
            flag_SeqA = true;
            if (fundOrInstPartyOption != null) {
                String fundOrInstPartyOptionValue = fundOrInstPartyOption.getTextContent();

                if (fundOrInstPartyOptionValue.equals("A")) {
                    document.renameNode(fundOrInstructingParty, null, "FundOrInstructingPartyA");
                    rootNode.removeChild(fundOrInstPartyOption);
                }
                else if (fundOrInstPartyOptionValue.equals("D")) {
                    document.renameNode(fundOrInstructingParty, null, "FundOrInstructingPartyD");
                    rootNode.removeChild(fundOrInstPartyOption);
                }
                else if (fundOrInstPartyOptionValue.equals("J")) {
                    document.renameNode(fundOrInstructingParty, null, "FundOrInstructingPartyJ");
                    rootNode.removeChild(fundOrInstPartyOption);
                }
            }
        }

        // tag : TermsAndConditions
        Node termsAndConditions = document.getElementsByTagName("TermsAndConditions").item(0);
        if (termsAndConditions != null && !termsAndConditions.getTextContent().equals("")) {
            flag_SeqA = true;
        }
        termsAndConditions.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(termsAndConditions.getTextContent()));

        // ending Sequence A
        if (flag_SeqA) {
            Element newSequenceA = document.createElement("NewSequenceA");
            newSequenceA.setTextContent("@");
            rootNode.appendChild(newSequenceA);
        }

        // starting Sequence B
        boolean flag_SeqB = false;

        // tag : PartyARole
        Node partyARole = document.getElementsByTagName("PartyARole").item(0);
        if (partyARole != null && !partyARole.getTextContent().equals("")) {
            flag_SeqB = true;
        }

        // tag : TradeDate
        Node tradeDate = document.getElementsByTagName("TradeDate").item(0);
        if (tradeDate != null && !tradeDate.getTextContent().equals("")) {
            flag_SeqB = true;
        }
        tradeDate.setTextContent(SWT_Outgoing_Globals.formatDate(tradeDate.getTextContent()));

        // tag : ValueDate
        if (valueDate != null && !valueDate.getTextContent().equals("")) {
            flag_SeqB = true;
            Element b4ValueDateNew = document.createElement("B4ValueDate");
            b4ValueDateNew.appendChild(document.createTextNode(valueDate.getTextContent()));
            rootNode.appendChild(b4ValueDateNew);
        }

        // tag : MaturityDate
        Node maturityDate = document.getElementsByTagName("MaturityDate").item(0);
        if (maturityDate != null && !maturityDate.getTextContent().equals("")) {
            flag_SeqB = true;
            maturityDate.setTextContent(SWT_Outgoing_Globals.formatDate(maturityDate.getTextContent()));
        }

        // tag : CcyPrincipalAmount
        Node ccyPrincipalAmount = document.getElementsByTagName("CcyPrincipalAmount").item(0);
        if (ccyPrincipalAmount != null && !ccyPrincipalAmount.getTextContent().equals("")) {
            flag_SeqB = true;
            ccyPrincipalAmount.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(ccyPrincipalAmount.getTextContent()));
        }

        // tag : AmountSettled
        Node amountSettled = document.getElementsByTagName("AmountSettled").item(0);
        if (amountSettled != null && !amountSettled.getTextContent().equals("")) {
            flag_SeqB = true;
            amountSettled.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(amountSettled.getTextContent()));
        }

        // tag : NextInterestDueDate
        Node nextInterestDueDate = document.getElementsByTagName("NextInterestDueDate").item(0);
        if (nextInterestDueDate != null && !nextInterestDueDate.getTextContent().equals("")) {
            flag_SeqB = true;
            nextInterestDueDate.setTextContent(SWT_Outgoing_Globals.formatDate(nextInterestDueDate.getTextContent()));
        }

        // tag : CcyAndInterestAmount
        Node ccyAndInterestAmount = document.getElementsByTagName("CcyAndInterestAmount").item(0);
        if (ccyAndInterestAmount != null && !ccyAndInterestAmount.getTextContent().equals("")) {
            flag_SeqB = true;
            ccyAndInterestAmount.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(ccyAndInterestAmount.getTextContent()));
        }

        // tag : InterestRate
        Node interestRate = document.getElementsByTagName("InterestRate").item(0);
        if (interestRate != null && !interestRate.getTextContent().equals("")) {
            flag_SeqB = true;
            interestRate.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(interestRate.getTextContent()));
        }

        // tag : DayCountFraction
        Node dayCountFraction = document.getElementsByTagName("DayCountFraction").item(0);
        if (dayCountFraction != null && !dayCountFraction.getTextContent().equals("")) {
            flag_SeqB = true;
        }

        // tag : LastDayFirstInterestPeriod
        Node lastDayFirstInterestPeriod = document.getElementsByTagName("LastDayFirstInterestPeriod").item(0);
        if (lastDayFirstInterestPeriod != null && !lastDayFirstInterestPeriod.getTextContent().equals("")) {
            flag_SeqB = true;
        }

        // tag : NumberOfDays
        Node numberOfDays = document.getElementsByTagName("NumberOfDays").item(0);
        if (numberOfDays != null && !numberOfDays.getTextContent().equals("")) {
            flag_SeqB = true;
        }

        // ending Sequence B
        if (flag_SeqB) {
            Element newSequenceB = document.createElement("NewSequenceB");
            newSequenceB.setTextContent("@");
            rootNode.appendChild(newSequenceB);
        }

        // starting Sequence C
        boolean flag_SeqC = false;

        // tag : CDeliveryAgent
        Node cDeliveryAgent = document.getElementsByTagName("cDeliveryAgent").item(0);
        Node cDeliveryAgentOption = document.getElementsByTagName("cDeliveryAgentOption").item(0);
        if (cDeliveryAgent != null && !cDeliveryAgent.getTextContent().equals("")) {
            flag_SeqC = true;

            if (cDeliveryAgentOption != null) {
                String cDeliveryAgentOptionValue = cDeliveryAgentOption.getTextContent();

                if (cDeliveryAgentOptionValue.equals("A")) {
                    cDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cDeliveryAgent.getTextContent()));
                    document.renameNode(cDeliveryAgent, null, "CDeliveryAgentA");
                    rootNode.removeChild(cDeliveryAgentOption);
                }
                else if (cDeliveryAgentOptionValue.equals("D")) {
                    cDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cDeliveryAgent.getTextContent()));
                    document.renameNode(cDeliveryAgent, null, "CDeliveryAgentD");
                    rootNode.removeChild(cDeliveryAgentOption);
                }
                else if (cDeliveryAgentOptionValue.equals("J")) {
                    cDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cDeliveryAgent.getTextContent()));
                    document.renameNode(cDeliveryAgent, null, "CDeliveryAgentJ");
                    rootNode.removeChild(cDeliveryAgentOption);
                }
            }
        }

        // tag : CIntermediary2
        Node cIntermediary2 = document.getElementsByTagName("cIntermediary2").item(0);
        Node cIntermediary2Option = document.getElementsByTagName("cIntermediary2Option").item(0);
        if (cIntermediary2 != null && !cIntermediary2.getTextContent().equals("")) {
            flag_SeqC = true;
            if (cIntermediary2Option != null) {
                String cIntermediary2OptionValue = cIntermediary2Option.getTextContent();

                if (cIntermediary2OptionValue.equals("A")) {
                    cIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cIntermediary2.getTextContent()));
                    document.renameNode(cIntermediary2, null, "CIntermediary2A");
                    rootNode.removeChild(cIntermediary2Option);
                }
                else if (cIntermediary2OptionValue.equals("D")) {
                    cIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cIntermediary2.getTextContent()));
                    document.renameNode(cIntermediary2, null, "CIntermediary2D");
                    rootNode.removeChild(cIntermediary2Option);
                }
                else if (cIntermediary2OptionValue.equals("J")) {
                    cIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cIntermediary2.getTextContent()));
                    document.renameNode(cIntermediary2, null, "CIntermediary2J");
                    rootNode.removeChild(cIntermediary2Option);
                }
            }
        }

        // tag : CIntermediary
        Node cIntermediary = document.getElementsByTagName("cIntermediary").item(0);
        Node cIntermediaryOption = document.getElementsByTagName("cIntermediaryOption").item(0);
        if (cIntermediary != null && !cIntermediary.getTextContent().equals("")) {
            flag_SeqC = true;
            if (cIntermediaryOption != null) {
                String cIntermediaryOptionValue = cIntermediaryOption.getTextContent();

                if (cIntermediaryOptionValue.equals("A")) {
                    cIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cIntermediary.getTextContent()));
                    document.renameNode(cIntermediary, null, "CIntermediaryA");
                    rootNode.removeChild(cIntermediaryOption);
                }
                else if (cIntermediaryOptionValue.equals("D")) {
                    cIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cIntermediary.getTextContent()));
                    document.renameNode(cIntermediary, null, "CIntermediaryD");
                    rootNode.removeChild(cIntermediaryOption);
                }
                else if (cIntermediaryOptionValue.equals("J")) {
                    cIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cIntermediary.getTextContent()));
                    document.renameNode(cIntermediary, null, "CIntermediaryJ");
                    rootNode.removeChild(cIntermediaryOption);
                }
            }
        }

        // tag : CReceivingAgent
        Node cReceivingAgent = document.getElementsByTagName("cReceivingAgent").item(0);
        Node cReceivingAgentOption = document.getElementsByTagName("cReceivingAgentOption").item(0);
        if (cReceivingAgent != null && !cReceivingAgent.getTextContent().equals("")) {
            flag_SeqC = true;
            if (cReceivingAgentOption != null) {
                String cReceivingAgentOptionValue = cReceivingAgentOption.getTextContent();

                if (cReceivingAgentOptionValue.equals("A")) {
                    cReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cReceivingAgent.getTextContent()));
                    document.renameNode(cReceivingAgent, null, "CReceivingAgentA");
                    rootNode.removeChild(cReceivingAgentOption);
                }
                else if (cReceivingAgentOptionValue.equals("D")) {
                    cReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cReceivingAgent.getTextContent()));
                    document.renameNode(cReceivingAgent, null, "CReceivingAgentD");
                    rootNode.removeChild(cReceivingAgentOption);
                }
                else if (cReceivingAgentOptionValue.equals("J")) {
                    cReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cReceivingAgent.getTextContent()));
                    document.renameNode(cReceivingAgent, null, "CReceivingAgentJ");
                    rootNode.removeChild(cReceivingAgentOption);
                }
            }
        }

        // tag : CBeneficiary
        Node cBeneficiary = document.getElementsByTagName("cBeneficiary").item(0);
        Node cBeneficiaryOption = document.getElementsByTagName("cBeneficiaryOption").item(0);
        if (cBeneficiary != null && !cBeneficiary.getTextContent().equals("")) {
            flag_SeqC = true;
            if (cBeneficiaryOption != null) {
                String cBeneficiaryOptionValue = cBeneficiaryOption.getTextContent();

                if (cBeneficiaryOptionValue.equals("A")) {
                    cBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cBeneficiary.getTextContent()));
                    document.renameNode(cBeneficiary, null, "CBeneficiaryA");
                    rootNode.removeChild(cBeneficiaryOption);
                }
                else if (cBeneficiaryOptionValue.equals("D")) {
                    cBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cBeneficiary.getTextContent()));
                    document.renameNode(cBeneficiary, null, "CBeneficiaryD");
                    rootNode.removeChild(cBeneficiaryOption);
                }
                else if (cBeneficiaryOptionValue.equals("J")) {
                    cBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(cBeneficiary.getTextContent()));
                    document.renameNode(cBeneficiary, null, "CBeneficiaryJ");
                    rootNode.removeChild(cBeneficiaryOption);
                }
            }
        }

        // ending Sequence C
        if (flag_SeqC) {
            Element newSequenceC = document.createElement("NewSequenceC");
            newSequenceC.setTextContent("@");
            rootNode.appendChild(newSequenceC);
        }

        // starting Sequence D
        boolean flag_SeqD = false;

        // tag : DDeliveryAgent
        Node dDeliveryAgent = document.getElementsByTagName("dDeliveryAgent").item(0);
        Node dDeliveryAgentOption = document.getElementsByTagName("dDeliveryAgentOption").item(0);
        if (dDeliveryAgent != null && !dDeliveryAgent.getTextContent().equals("")) {
            flag_SeqD = true;
            if (dDeliveryAgentOption != null) {
                String dDeliveryAgentOptionValue = dDeliveryAgentOption.getTextContent();

                if (dDeliveryAgentOptionValue.equals("A")) {
                    dDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dDeliveryAgent.getTextContent()));
                    document.renameNode(dDeliveryAgent, null, "DDeliveryAgentA");
                    rootNode.removeChild(dDeliveryAgentOption);
                }
                else if (dDeliveryAgentOptionValue.equals("D")) {
                    dDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dDeliveryAgent.getTextContent()));
                    document.renameNode(dDeliveryAgent, null, "DDeliveryAgentD");
                    rootNode.removeChild(dDeliveryAgentOption);
                }
                else if (dDeliveryAgentOptionValue.equals("J")) {
                    dDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dDeliveryAgent.getTextContent()));
                    document.renameNode(dDeliveryAgent, null, "DDeliveryAgentJ");
                    rootNode.removeChild(dDeliveryAgentOption);
                }
            }
        }

        // tag : DIntermediary2
        Node dIntermediary2 = document.getElementsByTagName("dIntermediary2").item(0);
        Node dIntermediary2Option = document.getElementsByTagName("dIntermediary2Option").item(0);
        if (dIntermediary2 != null && !dIntermediary2.getTextContent().equals("")) {
            flag_SeqD = true;
            if (dIntermediary2Option != null) {
                String dIntermediary2OptionValue = dIntermediary2Option.getTextContent();

                if (dIntermediary2OptionValue.equals("A")) {
                    dIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dIntermediary2.getTextContent()));
                    document.renameNode(dIntermediary2, null, "DIntermediary2A");
                    rootNode.removeChild(dIntermediary2Option);
                }
                else if (dIntermediary2OptionValue.equals("D")) {
                    dIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dIntermediary2.getTextContent()));
                    document.renameNode(dIntermediary2, null, "DIntermediary2D");
                    rootNode.removeChild(dIntermediary2Option);
                }
                else if (dIntermediary2OptionValue.equals("J")) {
                    dIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dIntermediary2.getTextContent()));
                    document.renameNode(dIntermediary2, null, "DIntermediary2J");
                    rootNode.removeChild(dIntermediary2Option);
                }
            }
        }

        // tag : DIntermediary
        Node dIntermediary = document.getElementsByTagName("dIntermediary").item(0);
        Node dIntermediaryOption = document.getElementsByTagName("dIntermediaryOption").item(0);
        if (dIntermediary != null && !dIntermediary.getTextContent().equals("")) {
            flag_SeqD = true;
            if (dIntermediaryOption != null) {
                String dIntermediaryOptionValue = dIntermediaryOption.getTextContent();

                if (dIntermediaryOptionValue.equals("A")) {
                    dIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dIntermediary.getTextContent()));
                    document.renameNode(dIntermediary, null, "DIntermediaryA");
                    rootNode.removeChild(dIntermediaryOption);
                }
                else if (dIntermediaryOptionValue.equals("D")) {
                    dIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dIntermediary.getTextContent()));
                    document.renameNode(dIntermediary, null, "DIntermediaryD");
                    rootNode.removeChild(dIntermediaryOption);
                }
                else if (dIntermediaryOptionValue.equals("J")) {
                    dIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dIntermediary.getTextContent()));
                    document.renameNode(dIntermediary, null, "DIntermediaryJ");
                    rootNode.removeChild(dIntermediaryOption);
                }
            }
        }

        // tag : DReceivingAgent
        Node dReceivingAgent = document.getElementsByTagName("dReceivingAgent").item(0);
        Node dReceivingAgentOption = document.getElementsByTagName("dReceivingAgentOption").item(0);
        if (dReceivingAgent != null && !dReceivingAgent.getTextContent().equals("")) {
            flag_SeqD = true;
            if (dReceivingAgentOption != null) {
                String dReceivingAgentOptionValue = dReceivingAgentOption.getTextContent();

                if (dReceivingAgentOptionValue.equals("A")) {
                    dReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dReceivingAgent.getTextContent()));
                    document.renameNode(dReceivingAgent, null, "DReceivingAgentA");
                    rootNode.removeChild(dReceivingAgentOption);
                }
                else if (dReceivingAgentOptionValue.equals("D")) {
                    dReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dReceivingAgent.getTextContent()));
                    document.renameNode(dReceivingAgent, null, "DReceivingAgentD");
                    rootNode.removeChild(dReceivingAgentOption);
                }
                else if (dReceivingAgentOptionValue.equals("J")) {
                    dReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dReceivingAgent.getTextContent()));
                    document.renameNode(dReceivingAgent, null, "DReceivingAgentJ");
                    rootNode.removeChild(dReceivingAgentOption);
                }
            }
        }

        // tag : DBeneficiary
        Node dBeneficiary = document.getElementsByTagName("dBeneficiary").item(0);
        Node dBeneficiaryOption = document.getElementsByTagName("dBeneficiaryOption").item(0);
        if (dBeneficiary != null && !dBeneficiary.getTextContent().equals("")) {
            flag_SeqD = true;
            if (dBeneficiaryOption != null) {
                String dBeneficiaryOptionValue = dBeneficiaryOption.getTextContent();

                if (dBeneficiaryOptionValue.equals("A")) {
                    dBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dBeneficiary.getTextContent()));
                    document.renameNode(dBeneficiary, null, "DBeneficiaryA");
                    rootNode.removeChild(dBeneficiaryOption);
                }
                else if (dBeneficiaryOptionValue.equals("D")) {
                    dBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dBeneficiary.getTextContent()));
                    document.renameNode(dBeneficiary, null, "DBeneficiaryD");
                    rootNode.removeChild(dBeneficiaryOption);
                }
                else if (dBeneficiaryOptionValue.equals("J")) {
                    dBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dBeneficiary.getTextContent()));
                    document.renameNode(dBeneficiary, null, "DBeneficiaryJ");
                    rootNode.removeChild(dBeneficiaryOption);
                }
            }
        }

        // ending Sequence D
        if (flag_SeqD) {
            Element newSequenceD = document.createElement("NewSequenceD");
            newSequenceD.setTextContent("@");
            rootNode.appendChild(newSequenceD);
        }

        // starting Sequence E
        boolean flag_SeqE = false;

        // tag : EDeliveryAgent
        Node eDeliveryAgent = document.getElementsByTagName("eDeliveryAgent").item(0);
        Node eDeliveryAgentOption = document.getElementsByTagName("eDeliveryAgentOption").item(0);
        if (eDeliveryAgent != null && !eDeliveryAgent.getTextContent().equals("")) {
            flag_SeqE = true;
            if (eDeliveryAgentOption != null) {
                String eDeliveryAgentOptionValue = eDeliveryAgentOption.getTextContent();

                if (eDeliveryAgentOptionValue.equals("A")) {
                    eDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eDeliveryAgent.getTextContent()));
                    document.renameNode(eDeliveryAgent, null, "EDeliveryAgentA");
                    rootNode.removeChild(eDeliveryAgentOption);
                }
                else if (eDeliveryAgentOptionValue.equals("D")) {
                    eDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eDeliveryAgent.getTextContent()));
                    document.renameNode(eDeliveryAgent, null, "EDeliveryAgentD");
                    rootNode.removeChild(eDeliveryAgentOption);
                }
                else if (eDeliveryAgentOptionValue.equals("J")) {
                    eDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eDeliveryAgent.getTextContent()));
                    document.renameNode(eDeliveryAgent, null, "EDeliveryAgentJ");
                    rootNode.removeChild(eDeliveryAgentOption);
                }
            }
        }

        // tag : EIntermediary2
        Node eIntermediary2 = document.getElementsByTagName("eIntermediary2").item(0);
        Node eIntermediary2Option = document.getElementsByTagName("eIntermediary2Option").item(0);
        if (eIntermediary2 != null && !eIntermediary2.getTextContent().equals("")) {
            flag_SeqE = true;
            if (eIntermediary2Option != null) {
                String eIntermediary2OptionValue = eIntermediary2Option.getTextContent();

                if (eIntermediary2OptionValue.equals("A")) {
                    eIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eIntermediary2.getTextContent()));
                    document.renameNode(eIntermediary2, null, "EIntermediary2A");
                    rootNode.removeChild(eIntermediary2Option);
                }
                else if (eIntermediary2OptionValue.equals("D")) {
                    eIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eIntermediary2.getTextContent()));
                    document.renameNode(eIntermediary2, null, "EIntermediary2D");
                    rootNode.removeChild(eIntermediary2Option);
                }
                else if (eIntermediary2OptionValue.equals("J")) {
                    eIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eIntermediary2.getTextContent()));
                    document.renameNode(eIntermediary2, null, "EIntermediary2J");
                    rootNode.removeChild(eIntermediary2Option);
                }
            }
        }

        // tag : EIntermediary
        Node eIntermediary = document.getElementsByTagName("eIntermediary").item(0);
        Node eIntermediaryOption = document.getElementsByTagName("eIntermediaryOption").item(0);
        if (eIntermediary != null && !eIntermediary.getTextContent().equals("")) {
            flag_SeqE = true;
            if (eIntermediaryOption != null) {
                String eIntermediaryOptionValue = eIntermediaryOption.getTextContent();

                if (eIntermediaryOptionValue.equals("A")) {
                    eIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eIntermediary.getTextContent()));
                    document.renameNode(eIntermediary, null, "EIntermediaryA");
                    rootNode.removeChild(eIntermediaryOption);
                }
                else if (eIntermediaryOptionValue.equals("D")) {
                    eIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eIntermediary.getTextContent()));
                    document.renameNode(eIntermediary, null, "EIntermediaryD");
                    rootNode.removeChild(eIntermediaryOption);
                }
                else if (eIntermediaryOptionValue.equals("J")) {
                    eIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eIntermediary.getTextContent()));
                    document.renameNode(eIntermediary, null, "EIntermediaryJ");
                    rootNode.removeChild(eIntermediaryOption);
                }
            }
        }

        // tag : EReceivingAgent
        Node eReceivingAgent = document.getElementsByTagName("eReceivingAgent").item(0);
        Node eReceivingAgentOption = document.getElementsByTagName("eReceivingAgentOption").item(0);
        if (eReceivingAgent != null && !eReceivingAgent.getTextContent().equals("")) {
            flag_SeqE = true;
            if (eReceivingAgentOption != null) {
                String eReceivingAgentOptionValue = eReceivingAgentOption.getTextContent();

                if (eReceivingAgentOptionValue.equals("A")) {
                    eReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eReceivingAgent.getTextContent()));
                    document.renameNode(eReceivingAgent, null, "EReceivingAgentA");
                    rootNode.removeChild(eReceivingAgentOption);
                }
                else if (eReceivingAgentOptionValue.equals("D")) {
                    eReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eReceivingAgent.getTextContent()));
                    document.renameNode(eReceivingAgent, null, "EReceivingAgentD");
                    rootNode.removeChild(eReceivingAgentOption);
                }
                else if (eReceivingAgentOptionValue.equals("J")) {
                    eReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eReceivingAgent.getTextContent()));
                    document.renameNode(eReceivingAgent, null, "EReceivingAgentJ");
                    rootNode.removeChild(eReceivingAgentOption);
                }
            }
        }

        // tag : EBeneficiary
        Node eBeneficiary = document.getElementsByTagName("eBeneficiary").item(0);
        Node eBeneficiaryOption = document.getElementsByTagName("eBeneficiaryOption").item(0);
        if (eBeneficiary != null && !eBeneficiary.getTextContent().equals("")) {
            flag_SeqE = true;
            if (eBeneficiaryOption != null) {
                String eBeneficiaryOptionValue = eBeneficiaryOption.getTextContent();

                if (eBeneficiaryOptionValue.equals("A")) {
                    eBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eBeneficiary.getTextContent()));
                    document.renameNode(eBeneficiary, null, "EBeneficiaryA");
                    rootNode.removeChild(eBeneficiaryOption);
                }
                else if (eBeneficiaryOptionValue.equals("D")) {
                    eBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eBeneficiary.getTextContent()));
                    document.renameNode(eBeneficiary, null, "EBeneficiaryD");
                    rootNode.removeChild(eBeneficiaryOption);
                }
                else if (eBeneficiaryOptionValue.equals("J")) {
                    eBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(eBeneficiary.getTextContent()));
                    document.renameNode(eBeneficiary, null, "EBeneficiaryJ");
                    rootNode.removeChild(eBeneficiaryOption);
                }
            }
        }

        // ending Sequence E
        if (flag_SeqE) {
            Element newSequenceE = document.createElement("NewSequenceE");
            newSequenceE.setTextContent("@");
            rootNode.appendChild(newSequenceE);
        }

        // starting Sequence F
        boolean flag_SeqF = false;

        // tag : FDeliveryAgent
        Node fDeliveryAgent = document.getElementsByTagName("fDeliveryAgent").item(0);
        Node fDeliveryAgentOption = document.getElementsByTagName("fDeliveryAgentOption").item(0);
        if (fDeliveryAgent != null && !fDeliveryAgent.getTextContent().equals("")) {
            flag_SeqF = true;
            if (fDeliveryAgentOption != null) {
                String fDeliveryAgentOptionValue = fDeliveryAgentOption.getTextContent();

                if (fDeliveryAgentOptionValue.equals("A")) {
                    fDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fDeliveryAgent.getTextContent()));
                    document.renameNode(fDeliveryAgent, null, "FDeliveryAgentA");
                    rootNode.removeChild(fDeliveryAgentOption);
                }
                else if (fDeliveryAgentOptionValue.equals("D")) {
                    fDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fDeliveryAgent.getTextContent()));
                    document.renameNode(fDeliveryAgent, null, "FDeliveryAgentD");
                    rootNode.removeChild(fDeliveryAgentOption);
                }
                else if (fDeliveryAgentOptionValue.equals("J")) {
                    fDeliveryAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fDeliveryAgent.getTextContent()));
                    document.renameNode(fDeliveryAgent, null, "FDeliveryAgentJ");
                    rootNode.removeChild(fDeliveryAgentOption);
                }
            }
        }

        // tag : FIntermediary2
        Node fIntermediary2 = document.getElementsByTagName("fIntermediary2").item(0);
        Node fIntermediary2Option = document.getElementsByTagName("fIntermediary2Option").item(0);
        if (fIntermediary2 != null && !fIntermediary2.getTextContent().equals("")) {
            flag_SeqF = true;
            if (fIntermediary2Option != null) {
                String fIntermediary2OptionValue = fIntermediary2Option.getTextContent();

                if (fIntermediary2OptionValue.equals("A")) {
                    fIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fIntermediary2.getTextContent()));
                    document.renameNode(fIntermediary2, null, "FIntermediary2A");
                    rootNode.removeChild(fIntermediary2Option);
                }
                else if (fIntermediary2OptionValue.equals("D")) {
                    fIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fIntermediary2.getTextContent()));
                    document.renameNode(fIntermediary2, null, "FIntermediary2D");
                    rootNode.removeChild(fIntermediary2Option);
                }
                else if (fIntermediary2OptionValue.equals("J")) {
                    fIntermediary2.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fIntermediary2.getTextContent()));
                    document.renameNode(fIntermediary2, null, "FIntermediary2J");
                    rootNode.removeChild(fIntermediary2Option);
                }
            }
        }

        // tag : FIntermediary
        Node fIntermediary = document.getElementsByTagName("fIntermediary").item(0);
        Node fIntermediaryOption = document.getElementsByTagName("fIntermediaryOption").item(0);
        if (fIntermediary != null && !fIntermediary.getTextContent().equals("")) {
            flag_SeqF = true;
            if (fIntermediaryOption != null) {
                String fIntermediaryOptionValue = fIntermediaryOption.getTextContent();

                if (fIntermediaryOptionValue.equals("A")) {
                    fIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fIntermediary.getTextContent()));
                    document.renameNode(fIntermediary, null, "FIntermediaryA");
                    rootNode.removeChild(fIntermediaryOption);
                }
                else if (fIntermediaryOptionValue.equals("D")) {
                    fIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fIntermediary.getTextContent()));
                    document.renameNode(fIntermediary, null, "FIntermediaryD");
                    rootNode.removeChild(fIntermediaryOption);
                }
                else if (fIntermediaryOptionValue.equals("J")) {
                    fIntermediary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fIntermediary.getTextContent()));
                    document.renameNode(fIntermediary, null, "FIntermediaryJ");
                    rootNode.removeChild(fIntermediaryOption);
                }
            }
        }

        // tag : FReceivingAgent
        Node fReceivingAgent = document.getElementsByTagName("fReceivingAgent").item(0);
        Node fReceivingAgentOption = document.getElementsByTagName("fReceivingAgentOption").item(0);
        if (fReceivingAgent != null && !fReceivingAgent.getTextContent().equals("")) {
            flag_SeqF = true;
            if (fReceivingAgentOption != null) {
                String fReceivingAgentOptionValue = fReceivingAgentOption.getTextContent();

                if (fReceivingAgentOptionValue.equals("A")) {
                    fReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fReceivingAgent.getTextContent()));
                    document.renameNode(fReceivingAgent, null, "FReceivingAgentA");
                    rootNode.removeChild(fReceivingAgentOption);
                }
                else if (fReceivingAgentOptionValue.equals("D")) {
                    fReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fReceivingAgent.getTextContent()));
                    document.renameNode(fReceivingAgent, null, "FReceivingAgentD");
                    rootNode.removeChild(fReceivingAgentOption);
                }
                else if (fReceivingAgentOptionValue.equals("J")) {
                    fReceivingAgent.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fReceivingAgent.getTextContent()));
                    document.renameNode(fReceivingAgent, null, "FReceivingAgentJ");
                    rootNode.removeChild(fReceivingAgentOption);
                }
            }
        }

        // tag : FBeneficiary
        Node fBeneficiary = document.getElementsByTagName("fBeneficiary").item(0);
        Node fBeneficiaryOption = document.getElementsByTagName("fBeneficiaryOption").item(0);
        if (fBeneficiary != null && !fBeneficiary.getTextContent().equals("")) {
            flag_SeqF = true;
            if (fBeneficiaryOption != null) {
                String fBeneficiaryOptionValue = fBeneficiaryOption.getTextContent();

                if (fBeneficiaryOptionValue.equals("A")) {
                    fBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fBeneficiary.getTextContent()));
                    document.renameNode(fBeneficiary, null, "FBeneficiaryA");
                    rootNode.removeChild(fBeneficiaryOption);
                }
                else if (fBeneficiaryOptionValue.equals("D")) {
                    fBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fBeneficiary.getTextContent()));
                    document.renameNode(fBeneficiary, null, "FBeneficiaryD");
                    rootNode.removeChild(fBeneficiaryOption);
                }
                else if (fBeneficiaryOptionValue.equals("J")) {
                    fBeneficiary.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(fBeneficiary.getTextContent()));
                    document.renameNode(fBeneficiary, null, "FBeneficiaryJ");
                    rootNode.removeChild(fBeneficiaryOption);
                }
            }
        }

        // ending Sequence F
        if (flag_SeqF) {
            Element newSequenceF = document.createElement("NewSequenceF");
            newSequenceF.setTextContent("@");
            rootNode.appendChild(newSequenceF);
        }

        // starting Sequence G
        boolean flag_SeqG = false;

        // tag : TaxRate
        Node taxRate = document.getElementsByTagName("TaxRate").item(0);
        if (taxRate != null && !taxRate.getTextContent().equals("")) {
            flag_SeqG = true;
            taxRate.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(taxRate.getTextContent()));
        }

        // tag : TransactionCcyAndNetIntAmt
        Node transactionCcyAndNetIntAmt = document.getElementsByTagName("TransactionCcyAndNetIntAmt").item(0);
        if (transactionCcyAndNetIntAmt != null && !transactionCcyAndNetIntAmt.getTextContent().equals("")) {
            flag_SeqG = true;
            transactionCcyAndNetIntAmt
                    .setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(transactionCcyAndNetIntAmt.getTextContent()));
        }

        // tag : ExchangeRate
        Node exchangeRate = document.getElementsByTagName("ExchangeRate").item(0);
        if (exchangeRate != null && !exchangeRate.getTextContent().equals("")) {
            flag_SeqG = true;
            exchangeRate.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(exchangeRate.getTextContent()));
        }

        // tag : ReportingCcyTaxAmount
        Node reportingCcyTaxAmount = document.getElementsByTagName("ReportingCcyTaxAmount").item(0);
        if (reportingCcyTaxAmount != null && !reportingCcyTaxAmount.getTextContent().equals("")) {
            flag_SeqG = true;
            reportingCcyTaxAmount
                    .setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(reportingCcyTaxAmount.getTextContent()));
        }

        // ending Sequence G
        if (flag_SeqG) {
            Element newSequenceG = document.createElement("NewSequenceG");
            newSequenceG.setTextContent("@");
            rootNode.appendChild(newSequenceG);
        }

        // starting Sequence H
        boolean flag_SeqH = false;

        // tag : ContactInformation
        Node contactInformation = document.getElementsByTagName("ContactInformation").item(0);
        if (contactInformation != null && !contactInformation.getTextContent().equals("")) {
            flag_SeqH = true;
            contactInformation.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(contactInformation.getTextContent()));
        }

        // tag : DealingMethod
        Node dealingMethod = document.getElementsByTagName("DealingMethod").item(0);
        if (dealingMethod != null && !dealingMethod.getTextContent().equals("")) {
            flag_SeqH = true;
            dealingMethod.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(dealingMethod.getTextContent()));
        }

        // tag : DealingBranchPartyA
        Node dealingBranchPartyA = document.getElementsByTagName("dealingBranchPartyA").item(0);
        Node dealingBranchPartyAOption = document.getElementsByTagName("dealingBranchPartyAOption").item(0);
        if (dealingBranchPartyA != null && !dealingBranchPartyA.getTextContent().equals("")) {
            flag_SeqH = true;
            if (dealingBranchPartyAOption != null) {
                String dealingBranchPartyAOptionValue = dealingBranchPartyAOption.getTextContent();

                if (dealingBranchPartyAOptionValue.equals("A")) {
                    document.renameNode(dealingBranchPartyA, null, "DealingBranchPartyA_A");
                    rootNode.removeChild(dealingBranchPartyAOption);
                }
                else if (dealingBranchPartyAOptionValue.equals("B")) {
                    document.renameNode(dealingBranchPartyA, null, "DealingBranchPartyA_B");
                    rootNode.removeChild(dealingBranchPartyAOption);
                }
                else if (dealingBranchPartyAOptionValue.equals("D")) {
                    document.renameNode(dealingBranchPartyA, null, "DealingBranchPartyA_D");
                    rootNode.removeChild(dealingBranchPartyAOption);
                }
                else if (dealingBranchPartyAOptionValue.equals("J")) {
                    document.renameNode(dealingBranchPartyA, null, "DealingBranchPartyA_J");
                    rootNode.removeChild(dealingBranchPartyAOption);
                }
            }
        }

        // tag : DealingBranchPartyB
        Node dealingBranchPartyB = document.getElementsByTagName("dealingBranchPartyB").item(0);
        Node dealingBranchPartyBOption = document.getElementsByTagName("dealingBranchPartyBOption").item(0);
        if (dealingBranchPartyB != null && !dealingBranchPartyB.getTextContent().equals("")) {
            flag_SeqH = true;
            if (dealingBranchPartyBOption != null) {
                String dealingBranchPartyBOptionValue = dealingBranchPartyBOption.getTextContent();

                if (dealingBranchPartyBOptionValue.equals("A")) {
                    document.renameNode(dealingBranchPartyB, null, "DealingBranchPartyB_A");
                    rootNode.removeChild(dealingBranchPartyBOption);
                }
                else if (dealingBranchPartyBOptionValue.equals("B")) {
                    document.renameNode(dealingBranchPartyB, null, "DealingBranchPartyB_B");
                    rootNode.removeChild(dealingBranchPartyBOption);
                }
                else if (dealingBranchPartyBOptionValue.equals("D")) {
                    document.renameNode(dealingBranchPartyB, null, "DealingBranchPartyB_D");
                    rootNode.removeChild(dealingBranchPartyBOption);
                }
                else if (dealingBranchPartyBOptionValue.equals("J")) {
                    document.renameNode(dealingBranchPartyB, null, "DealingBranchPartyB_J");
                    rootNode.removeChild(dealingBranchPartyBOption);
                }
            }
        }

        // tag : BrokerID
        Node brokerID = document.getElementsByTagName("brokerID").item(0);
        Node brokerIDOption = document.getElementsByTagName("brokerIDOption").item(0);
        if (brokerID != null && !brokerID.getTextContent().equals("")) {
            flag_SeqH = true;
            if (brokerIDOption != null) {
                String brokerIDOptionValue = dealingBranchPartyBOption.getTextContent();

                if (brokerIDOptionValue.equals("A")) {
                    document.renameNode(brokerID, null, "BrokerIdA");
                    rootNode.removeChild(brokerIDOption);
                }
                else if (brokerIDOptionValue.equals("D")) {
                    document.renameNode(brokerID, null, "BrokerIdD");
                    rootNode.removeChild(brokerIDOption);
                }
                else if (brokerIDOptionValue.equals("J")) {
                    document.renameNode(brokerID, null, "BrokerIdJ");
                    rootNode.removeChild(brokerIDOption);
                }
            }
        }

        // tag : BrokersCommission
        Node brokersCommission = document.getElementsByTagName("BrokersCommission").item(0);
        if (brokersCommission != null && !brokersCommission.getTextContent().equals("")) {
            flag_SeqH = true;
            brokersCommission.setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(brokersCommission.getTextContent()));
        }

        // tag : CounterpartysRef
        Node counterpartysRef = document.getElementsByTagName("CounterpartysRef").item(0);
        if (counterpartysRef != null && !counterpartysRef.getTextContent().equals("")) {
            flag_SeqH = true;
            counterpartysRef.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(counterpartysRef.getTextContent()));
        }

        // tag : BrokersRef
        Node brokersRef = document.getElementsByTagName("BrokersRef").item(0);
        if (brokersRef != null && !brokersRef.getTextContent().equals("")) {
            flag_SeqH = true;
            brokersRef.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(brokersRef.getTextContent()));
        }

        // tag : SenderToReceiverInfo
        Node senderToReceiverInfo = document.getElementsByTagName("SenderToReceiverInfo").item(0);
        if (senderToReceiverInfo != null && !senderToReceiverInfo.getTextContent().equals("")) {
            flag_SeqH = true;
            senderToReceiverInfo.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(senderToReceiverInfo.getTextContent()));
        }

        // ending Sequence H
        if (flag_SeqH) {
            Element newSequenceH = document.createElement("NewSequenceH");
            newSequenceH.setTextContent("@");
            rootNode.appendChild(newSequenceH);
        }

        // tag : NumberOfRepetitions

        // tag : Payment_Currency
        NodeList paymentCurrencyList = document.getElementsByTagName("Payment_Currency");
        for (int i = 0; i < paymentCurrencyList.getLength(); ++i) {
            Element paymentCurrency = (Element) paymentCurrencyList.item(i);

            // child tag : PaymentDate
            Node paymentDate = paymentCurrency.getElementsByTagName("PaymentDate").item(0);
            if (paymentDate != null) {
                paymentDate.setTextContent(SWT_Outgoing_Globals.formatDate(paymentDate.getTextContent()));
            }

            // child tag : Currency_PaymentAmount

        }

        // tag : PaymentClearingCentre
        Node paymentClearingCentre = document.getElementsByTagName("PaymentClearingCentre").item(0);
        if (paymentClearingCentre != null && !paymentClearingCentre.getTextContent().equals("")) {
            paymentClearingCentre.setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(paymentClearingCentre.getTextContent()));
        }
    }

}
