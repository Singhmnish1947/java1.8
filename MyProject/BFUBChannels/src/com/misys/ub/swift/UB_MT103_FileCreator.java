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

public class UB_MT103_FileCreator {
    private transient final static Log LOGGER = LogFactory.getLog(UB_MT103_FileCreator.class.getName());
    private String FinalXml;
    private String strResult;
    public static final String xmlFilePath = "request.xml";
    public String Temp_context;
    public String Temp_name;
    public NodeList Temp_Node;
    public Double Temp_Double;
    public Element rootElement;
    public Element Temp_Element;
    public String source_instructedAmount = "Empty";
    public static SimpleDateFormat format_date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public String MT103_Transform(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
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
            LOGGER.info("XML file updated successfully");
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
        Node rootNode = doc.getFirstChild();
        doc.renameNode(rootNode, null, "MeridianMessage");
        Element rootElement = (Element) rootNode;
        rootElement.setAttribute("MessageType", "SWIFT_MT103"); // change as per
                                                                // message type
        Date date = SystemInformationManager.getInstance().getBFSystemDate();
        Timestamp timestamp = new Timestamp(date.getTime());
        rootElement.setAttribute("Timestamp", timestamp.toString());
        rootElement.setAttribute("MessageFormat", "StandardXML");
        rootElement.setAttribute("System", "SWIFT");

        // accountWithInstOption Tag and accountWithInstitution Tag
        if (doc.getElementsByTagName("accountWithInstOption").item(0) != null
                && doc.getElementsByTagName("accountWithInstitution").item(0) != null) {
            Temp_context = doc.getElementsByTagName("accountWithInstOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("accountWithInstOption").item(0), null, "AccountWithInstitutionA");
                doc.getElementsByTagName("AccountWithInstitutionA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("accountWithInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("accountWithInstitution").item(0));
            }
            else if ("B".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("accountWithInstOption").item(0), null, "AccountWithInstitutionB");
                doc.getElementsByTagName("AccountWithInstitutionB").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("accountWithInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("accountWithInstitution").item(0));
            }
            else if ("C".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("accountWithInstOption").item(0), null, "AccountWithInstitutionC");
                doc.getElementsByTagName("AccountWithInstitutionC").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("accountWithInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("accountWithInstitution").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("accountWithInstOption").item(0), null, "AccountWithInstitutionD");
                doc.getElementsByTagName("AccountWithInstitutionD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("accountWithInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("accountWithInstitution").item(0));
            }
        }
        Temp_context = null;

        // instructedAmount Tag and instructedCurrency Tag
        if (doc.getElementsByTagName("instructedAmount").item(0) != null) {
            source_instructedAmount = doc.getElementsByTagName("instructedAmount").item(0).getTextContent();
            if (source_instructedAmount == null || source_instructedAmount.equals("")) {
                doc.renameNode(doc.getElementsByTagName("instructedAmount").item(0), null, "CurrencyInstructedAmount");
                // doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("instructedCurrency").item(0));
            }
            else if (doc.getElementsByTagName("instructedCurrency").item(0) != null) {

                doc.getElementsByTagName("instructedAmount").item(0).setTextContent(
                        doc.getElementsByTagName("instructedCurrency").item(0).getTextContent() + SWT_Outgoing_Globals
                                .ReplacePeriodWithComma(doc.getElementsByTagName("instructedAmount").item(0).getTextContent()));
                doc.renameNode(doc.getElementsByTagName("instructedAmount").item(0), null, "CurrencyInstructedAmount");
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("instructedCurrency").item(0));
            }
        }

        // tdValueDate Tag and tdCurrencyCode Tag and tdAmount Tag
        if (doc.getElementsByTagName("tdValueDate").item(0) != null && doc.getElementsByTagName("tdCurrencyCode").item(0) != null
                && doc.getElementsByTagName("tdAmount").item(0) != null) {
            doc.getElementsByTagName("tdValueDate").item(0).setTextContent(
                    SWT_Outgoing_Globals.formatDateTo6Digits(doc.getElementsByTagName("tdValueDate").item(0).getTextContent())
                            + doc.getElementsByTagName("tdCurrencyCode").item(0).getTextContent() + SWT_Outgoing_Globals
                                    .ReplacePeriodWithComma(doc.getElementsByTagName("tdAmount").item(0).getTextContent()));
            doc.renameNode(doc.getElementsByTagName("tdValueDate").item(0), null, "ValueDateCcyInterbankSettledAmt");
            doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("tdCurrencyCode").item(0));
            doc.getElementsByTagName("MeridianMessage").item(0).removeChild(doc.getElementsByTagName("tdAmount").item(0));
        }

        // thirdReimbursementInstOption Tag and thirdReimbursementInstitution
        // Tag
        if (doc.getElementsByTagName("thirdReimbursementInstOption").item(0) != null
                && doc.getElementsByTagName("thirdReimbursementInstitution").item(0) != null) {
            Temp_context = doc.getElementsByTagName("thirdReimbursementInstOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("thirdReimbursementInstOption").item(0), null,
                        "ThirdReimbursementInstitutionA");
                doc.getElementsByTagName("ThirdReimbursementInstitutionA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("thirdReimbursementInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("thirdReimbursementInstitution").item(0));
            }
            else if ("B".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("thirdReimbursementInstOption").item(0), null,
                        "ThirdReimbursementInstitutionB");
                doc.getElementsByTagName("ThirdReimbursementInstitutionB").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("thirdReimbursementInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("thirdReimbursementInstitution").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("thirdReimbursementInstOption").item(0), null,
                        "ThirdReimbursementInstitutionD");
                doc.getElementsByTagName("ThirdReimbursementInstitutionD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("thirdReimbursementInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("thirdReimbursementInstitution").item(0));
            }
        }
        Temp_context = null;

        // sendersReference Tag
        if (doc.getElementsByTagName("SendersRef").item(0) != null) {
            doc.getElementsByTagName("SendersRef").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SendersRef").item(0).getTextContent()));

            Element TransactionReference = doc.createElement("TransactionReference");
            TransactionReference.setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SendersRef").item(0).getTextContent()));
            rootElement.appendChild(TransactionReference);

        }

        // intermediaryInstOption Tag and intermediaryInstitution Tag
        if (doc.getElementsByTagName("intermediaryInstOption").item(0) != null
                && doc.getElementsByTagName("intermediaryInstitution").item(0) != null) {
            Temp_context = doc.getElementsByTagName("intermediaryInstOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("intermediaryInstOption").item(0), null, "IntermediaryInstitutionA");
                doc.getElementsByTagName("IntermediaryInstitutionA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("intermediaryInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("intermediaryInstitution").item(0));
            }
            else if ("B".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("intermediaryInstOption").item(0), null, "IntermediaryInstitutionB");
                doc.getElementsByTagName("IntermediaryInstitutionB").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("intermediaryInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("intermediaryInstitution").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("intermediaryInstOption").item(0), null, "IntermediaryInstitutionD");
                doc.getElementsByTagName("IntermediaryInstitutionD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("intermediaryInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("intermediaryInstitution").item(0));
            }
        }
        Temp_context = null;

        // senderToReceiverInfo Tag
        if (doc.getElementsByTagName("SenderToReceiverInfo").item(0) != null) {
            doc.getElementsByTagName("SenderToReceiverInfo").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("SenderToReceiverInfo").item(0).getTextContent()));
        }

        // orderingCustomerOption Tag and orderingCustomer Tag
        if (doc.getElementsByTagName("orderingCustomerOption").item(0) != null
                && doc.getElementsByTagName("orderingCustomer").item(0) != null) {
            Temp_context = doc.getElementsByTagName("orderingCustomerOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "OrderingCustomerA";
                doc.renameNode(doc.getElementsByTagName("orderingCustomerOption").item(0), null, "OrderingCustomerA");
                doc.getElementsByTagName("OrderingCustomerA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("orderingCustomer").item(0));
            }
            else if ("K".equals(Temp_context)) {
                Temp_name = "OrderingCustomerK";
                doc.renameNode(doc.getElementsByTagName("orderingCustomerOption").item(0), null, "OrderingCustomerK");
                doc.getElementsByTagName("OrderingCustomerK").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("orderingCustomer").item(0));
            }
            else if ("F".equals(Temp_context)) {
                Temp_name = "OrderingCustomerF";
                doc.renameNode(doc.getElementsByTagName("orderingCustomerOption").item(0), null, "OrderingCustomerF");
                doc.getElementsByTagName("OrderingCustomerF").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("orderingCustomer").item(0));
            }
        }
        Temp_context = null;

        // sendersCorrespOption Tag and sendersCorrespondent Tag
        if (doc.getElementsByTagName("sendersCorrespOption").item(0) != null
                && doc.getElementsByTagName("sendersCorrespondent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("sendersCorrespOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("sendersCorrespOption").item(0), null, "SendersCorrespondentA");
                doc.getElementsByTagName("SendersCorrespondentA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("sendersCorrespondent").item(0));
            }
            else if ("B".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("sendersCorrespOption").item(0), null, "SendersCorrespondentB");
                doc.getElementsByTagName("SendersCorrespondentB").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("sendersCorrespondent").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("sendersCorrespOption").item(0), null, "SendersCorrespondentD");
                doc.getElementsByTagName("SendersCorrespondentD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("sendersCorrespondent").item(0));
            }
        }
        Temp_context = null;

        // charge Tag
        for (int j = 0; j < doc.getElementsByTagName("Charges").getLength(); j++) {
            Temp_Node = doc.getElementsByTagName("Charges").item(j).getChildNodes();
            for (int i = 0; i < Temp_Node.getLength(); i++) {
                if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Temp_Node.item(i)
                            .setTextContent(SWT_Outgoing_Globals.ReplacePeriodWithComma(Temp_Node.item(i).getTextContent()));
                }
            }
        }
        Temp_Node = null;

        // receiversCorrespOption Tag and receiversCorrespondent Tag
        if (doc.getElementsByTagName("receiversCorrespOption").item(0) != null
                && doc.getElementsByTagName("receiversCorrespondent").item(0) != null) {
            Temp_context = doc.getElementsByTagName("receiversCorrespOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                Temp_name = "ReceiversCorrespondentA";
                doc.renameNode(doc.getElementsByTagName("receiversCorrespOption").item(0), null, "ReceiversCorrespondentA");
                doc.getElementsByTagName("ReceiversCorrespondentA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("receiversCorrespondent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("receiversCorrespondent").item(0));
            }
            else if ("B".equals(Temp_context)) {
                Temp_name = "ReceiversCorrespondentB";
                doc.renameNode(doc.getElementsByTagName("receiversCorrespOption").item(0), null, "ReceiversCorrespondentB");
                doc.getElementsByTagName("ReceiversCorrespondentB").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("receiversCorrespondent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("receiversCorrespondent").item(0));
            }
            else if ("D".equals(Temp_context)) {
                Temp_name = "ReceiversCorrespondentD";
                doc.renameNode(doc.getElementsByTagName("receiversCorrespOption").item(0), null, "ReceiversCorrespondentD");
                doc.getElementsByTagName("ReceiversCorrespondentD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("receiversCorrespondent").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("receiversCorrespondent").item(0));
            }
        }
        Temp_context = null;

        // remittanceInfo Tag
        if (doc.getElementsByTagName("RemittanceInfo").item(0) != null) {
            doc.getElementsByTagName("RemittanceInfo").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("RemittanceInfo").item(0).getTextContent()));
        }

        // beneficiaryCustOption Tag and beneficiaryCustomer Tag
        if (doc.getElementsByTagName("beneficiaryCustOption").item(0) != null
                && doc.getElementsByTagName("beneficiaryCustomer").item(0) != null) {
            Temp_context = doc.getElementsByTagName("beneficiaryCustOption").item(0).getTextContent();

            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("beneficiaryCustOption").item(0), null, "BeneficiaryCustomerA");
                doc.getElementsByTagName("BeneficiaryCustomerA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("beneficiaryCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("beneficiaryCustomer").item(0));
            }
            else if ("F".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("beneficiaryCustOption").item(0), null, "BeneficiaryCustomerF");
                doc.getElementsByTagName("BeneficiaryCustomerF").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("beneficiaryCustomer").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("beneficiaryCustomer").item(0));
            }
            else {
                doc.renameNode(doc.getElementsByTagName("beneficiaryCustomer").item(0), null, "BeneficiaryCustomer");
                doc.getElementsByTagName("BeneficiaryCustomer").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("BeneficiaryCustomer").item(0).getTextContent()));

            }

        }
        else {
            doc.renameNode(doc.getElementsByTagName("beneficiaryCustomer").item(0), null, "BeneficiaryCustomer");
            doc.getElementsByTagName("BeneficiaryCustomer").item(0).setTextContent(SWT_Outgoing_Globals
                    .RemoveUBDelimiter(doc.getElementsByTagName("BeneficiaryCustomer").item(0).getTextContent()));

        }
        Temp_context = null;

        // receiversCharges Tag
        if (doc.getElementsByTagName("ReceiversCharges").item(0) != null) {
            doc.getElementsByTagName("ReceiversCharges").item(0).setTextContent(SWT_Outgoing_Globals
                    .ReplacePeriodWithComma(doc.getElementsByTagName("ReceiversCharges").item(0).getTextContent()));
        }

        // orderInstitutionOption Tag and orderingInstitution Tag
        if (doc.getElementsByTagName("orderInstitutionOption").item(0) != null
                && doc.getElementsByTagName("orderingInstitution").item(0) != null) {
            Temp_context = doc.getElementsByTagName("orderInstitutionOption").item(0).getTextContent();
            if ("A".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("orderInstitutionOption").item(0), null, "OrderingInstitutionA");
                doc.getElementsByTagName("OrderingInstitutionA").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("orderingInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("orderingInstitution").item(0));
            }
            else if ("D".equals(Temp_context)) {
                doc.renameNode(doc.getElementsByTagName("orderInstitutionOption").item(0), null, "OrderingInstitutionD");
                doc.getElementsByTagName("OrderingInstitutionD").item(0).setTextContent(SWT_Outgoing_Globals
                        .RemoveUBDelimiter(doc.getElementsByTagName("orderingInstitution").item(0).getTextContent()));
                doc.getElementsByTagName("MeridianMessage").item(0)
                        .removeChild(doc.getElementsByTagName("orderingInstitution").item(0));
            }
        }
        Temp_context = null;

        // exchangeRate Tag
        if (doc.getElementsByTagName("ExchangeRate").item(0) != null) {
            doc.getElementsByTagName("ExchangeRate").item(0).setTextContent(
                    SWT_Outgoing_Globals.ReplacePeriodWithComma(doc.getElementsByTagName("ExchangeRate").item(0).getTextContent()));
        }

        // instruction Tag
        for (int j = 0; j < doc.getElementsByTagName("Instruction").getLength(); j++) {
            Temp_Node = doc.getElementsByTagName("Instruction").item(j).getChildNodes();
            for (int i = 0; i < Temp_Node.getLength(); i++) {
                if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE)
                    doc.renameNode(Temp_Node.item(i), null, "InstructionCode");
                Temp_Node.item(i).setTextContent(SWT_Outgoing_Globals.RemoveUBDelimiter(Temp_Node.item(i).getTextContent()));
            }
        }
        Temp_Node = null;
        if (doc.getElementsByTagName("SenderAddress").item(0) != null) {
            doc.getElementsByTagName("SenderAddress").item(0).setTextContent(
                    SWT_Outgoing_Globals.RemoveUBDelimiter(doc.getElementsByTagName("SenderAddress").item(0).getTextContent()));
        }

        // internalRef Tag and messageId Tag
        if (doc.getElementsByTagName("messageId").item(0) != null && doc.getElementsByTagName("internalRef").item(0) != null) {
            Temp_Double = Double.parseDouble(doc.getElementsByTagName("messageId").item(0).getTextContent());
            Temp_context = doc.getElementsByTagName("internalRef").item(0).getTextContent();
            Temp_Element = doc.createElement("Direction");
            if (!Temp_context.equals("")) {
                Temp_Element.setTextContent("I");
                rootElement.appendChild(Temp_Element);
                doc.renameNode(doc.getElementsByTagName("internalRef").item(0), null, "InternalReference");
                if (Temp_Double != 0.0) {
                    doc.renameNode(doc.getElementsByTagName("messageId").item(0), null, "MessageID");
                }
            }
            else {

                Temp_Element.setTextContent("O");
                rootElement.appendChild(Temp_Element);

            }
        }
        Temp_Double = 0.0;
        Temp_context = null;
        Temp_Element = null;

        if (doc.getElementsByTagName("stp").item(0) != null
                && "Y".equals(doc.getElementsByTagName("stp").item(0).getTextContent())) {
            doc.renameNode(doc.getElementsByTagName("stp").item(0), null, "B3Validation");
            doc.getElementsByTagName("B3Validation").item(0).setTextContent("STP");

        }

        // serviceIdentifierId Tag

        // MeridianMessageType Tag
        Temp_Element = doc.createElement("MeridianMessageType");
        Temp_Element.setTextContent("SWIFT_MT103");
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

        // Priority Tag
        Temp_Element = doc.createElement("Priority");
        Temp_Element.setTextContent("N");
        rootElement.appendChild(Temp_Element);

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

        // LineOfBusiness Tag
        Temp_Element = doc.createElement("LineOfBusiness");
        Temp_Element.setTextContent("UB");
        rootElement.appendChild(Temp_Element);

        // MultipleMessageStatus Tag
        Temp_Element = doc.createElement("MultipleMessageStatus");
        Temp_Element.setTextContent("I");
        rootElement.appendChild(Temp_Element);

    }

}
