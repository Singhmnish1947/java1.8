package com.misys.ub.swift;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

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

import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;

import org.apache.commons.logging.Log;

/**
 * 
 * @author Arsath 4154
 * 
 *         file to transform MT103
 * 
 * 
 */

public class MT103_SWIFT_UB {
	private static final String THIRD_REIMBURSEMENT_INSTITUTION = "thirdReimbursementInstitution";

	private static final String THIRD_REIMBURSEMENT_INST_OPTION = "thirdReimbursementInstOption";

	private static final String SENDERS_CORRESP_OPTION = "sendersCorrespOption";

	private static final String RECEIVERS_CORRESP_OPTION = "receiversCorrespOption";

	private static final String ORDERING_CUSTOMER_OPTION = "orderingCustomerOption";

	private static final String ORDERING_CUSTOMER = "orderingCustomer";

	private static final String INTERMEDIARY_INSTITUTION = "intermediaryInstitution";

	private static final String INTERMEDIARY_INST_OPTION = "intermediaryInstOption";

	private static final String BENEFICIARY_CUSTOMER = "beneficiaryCustomer";

	private static final String INSTRUCTION = "Instruction";

	private static final String CHARGES = "Charges";

	private static final String ACCOUNT_WITH_INST_OPTION = "accountWithInstOption";

	private static final String THIRD_REIMBURSEMENT_INSTITUTION_B = "ThirdReimbursementInstitutionB";

	private static final String THIRD_REIMBURSEMENT_INSTITUTION_D = "ThirdReimbursementInstitutionD";

	private static final String THIRD_REIMBURSEMENT_INSTITUTION_A = "ThirdReimbursementInstitutionA";

	private static final String SENDERS_CORRESPONDENT_D = "SendersCorrespondentD";

	private static final String SENDERS_CORRESPONDENT_B = "SendersCorrespondentB";

	private static final String SENDERS_CORRESPONDENT_A = "SendersCorrespondentA";

	private static final String RECEIVERS_CORRESPONDENT_D = "ReceiversCorrespondentD";

	private static final String RECEIVERS_CORRESPONDENT_B = "ReceiversCorrespondentB";

	private static final String RECEIVERS_CORRESPONDENT_A = "ReceiversCorrespondentA";

	private static final String ORDERING_INSTITUTION_D = "OrderingInstitutionD";

	private static final String ORDERING_INSTITUTION_A = "OrderingInstitutionA";

	private static final String ORDERING_CUSTOMER_K = "OrderingCustomerK";

	private static final String ORDERING_CUSTOMER_F = "OrderingCustomerF";

	private static final String ORDERING_CUSTOMER_A = "OrderingCustomerA";

	private static final String INTERMEDIARY_INSTITUTION_D = "IntermediaryInstitutionD";

	private static final String INTERMEDIARY_INSTITUTION_C = "IntermediaryInstitutionC";

	private static final String INTERMEDIARY_INSTITUTION_A = "IntermediaryInstitutionA";

	private static final String BENEFICIARY_CUSTOMER_F = "BeneficiaryCustomerF";

	private static final String ACCOUNT_WITH_INSTITUTION_D = "AccountWithInstitutionD";

	private static final String BENEFICIARY_CUSTOMER_A = "BeneficiaryCustomerA";

	private static final String ACCOUNT_WITH_INSTITUTION_C = "AccountWithInstitutionC";

	private static final String ACCOUNT_WITH_INSTITUTION_B = "AccountWithInstitutionB";

	private static final String ACCOUNT_WITH_INSTITUTION_A = "AccountWithInstitutionA";

	private transient final static Log LOGGER = LogFactory.getLog(MT103_SWIFT_UB.class.getName());

	private static String strResult;
	public static final String XMLFILEPATH = "response_switf.xml";
	public static String Temp_context;
	public static String Temp_name;
	public static NodeList Temp_Node;
	public static Double Temp_Double;
	public static Element rootElement;

	public static Element Temp_Element;
	public static SimpleDateFormat format_date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static String MT103_SWIFT_Transform(String requestMsg) {
		String requestMsg1 = requestMsg.replaceAll("/n", "");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {

			dBuilder = dbFactory.newDocumentBuilder();
			Document res = dBuilder.newDocument();
			InputSource is = new InputSource(new StringReader(requestMsg1));
			Document doc = dBuilder.parse(is);
			rootElement = res.createElement("UB_MT103");
			res.appendChild(rootElement);
			rootElement.setAttribute(PaymentSwiftConstants.XMLNS, PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);

			doc.getDocumentElement().normalize();

			// update Element value
			updateElementValue(doc, res);

			res.getDocumentElement().normalize();

			// write the updated document to forward request
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
			DOMSource source = new DOMSource(res);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			strResult = writer.toString();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(strResult);
				LOGGER.info("XML file updated successfully");
			}
		} catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) {
			e1.printStackTrace();
		}
		return strResult;
	}

	private static void updateElementValue(Document doc, Document res) {

		Temp_Element = res.createElement("header");
		Temp_Element.setAttribute(PaymentSwiftConstants.XMLNS, PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		rootElement.appendChild(Temp_Element);
		rootElement = Temp_Element;

		if (doc.getElementsByTagName("MessageID").item(0) != null) {
			Temp_context = doc.getElementsByTagName("MessageID").item(0).getTextContent();
			Temp_Element = res.createElement("messageId1");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		if (doc.getElementsByTagName("ExternalMessageType").item(0) != null) {
			Temp_context = doc.getElementsByTagName("ExternalMessageType").item(0).getTextContent();
			Temp_Element = res.createElement("messageType");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		Temp_Element = res.createElement("details");
		Temp_Element.setAttribute(PaymentSwiftConstants.XMLNS, PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		res.getElementsByTagName("UB_MT103").item(0).appendChild(Temp_Element);
		rootElement = Temp_Element;

		// SenderAddress tag
		if (doc.getElementsByTagName("SenderAddress").item(0) != null) {
			Temp_context = doc.getElementsByTagName("SenderAddress").item(0).getTextContent();
			Temp_Element = res.createElement("sender");
			Temp_Element.setTextContent(SWT_Outgoing_Globals.convertBIC(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// DestinationAddress Tag
		if (doc.getElementsByTagName("DestinationAddress").item(0) != null) {
			Temp_context = doc.getElementsByTagName("DestinationAddress").item(0).getTextContent();
			Temp_Element = res.createElement("receiver");
			Temp_Element.setTextContent(SWT_Outgoing_Globals.convertBIC(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// CancellationAction Tag
		if (doc.getElementsByTagName("CancellationAction").item(0) != null) {
			Temp_context = doc.getElementsByTagName("CancellationAction").item(0).getTextContent();
			Temp_Element = res.createElement("action");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// SendersRef Tag
		if (doc.getElementsByTagName("SendersRef").item(0) != null) {
			Temp_context = doc.getElementsByTagName("SendersRef").item(0).getTextContent();
			Temp_Element = res.createElement("sendersReference");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// BankOperationCode Tag
		if (doc.getElementsByTagName("BankOperationCode").item(0) != null) {
			Temp_context = doc.getElementsByTagName("BankOperationCode").item(0).getTextContent();
			Temp_Element = res.createElement("bankOperationCode");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// Instruction Tag - Collection
		if (doc.getElementsByTagName(INSTRUCTION).item(0) != null) {
			Temp_Element = res.createElement("instruction");
			rootElement.appendChild(Temp_Element);
			for (int j = 0; j < doc.getElementsByTagName(INSTRUCTION).getLength(); j++) {
				Temp_Node = doc.getElementsByTagName(INSTRUCTION).item(j).getChildNodes();
				for (int i = 0; i < Temp_Node.getLength(); i++) {
					if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Temp_context = Temp_Node.item(i).getTextContent();
						Temp_Element = res.createElement("instructionCode");
						Temp_Element.setTextContent(Temp_context);
						res.getElementsByTagName("instruction").item(0).appendChild(Temp_Element);
					}
				}
			}
		}
		Temp_Node = null;

		// TransactionTypeCode Tag
		if (doc.getElementsByTagName("TransactionTypeCode").item(0) != null) {
			Temp_context = doc.getElementsByTagName("TransactionTypeCode").item(0).getTextContent();
			Temp_Element = res.createElement("transactionTypeCode");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// tdValueDate Tag and tdCurrencyCode Tag and tdAmount Tag
		if (doc.getElementsByTagName("ValueDateCcyInterbankSettledAmt").item(0) != null) {
			Temp_name = doc.getElementsByTagName("ValueDateCcyInterbankSettledAmt").item(0).getTextContent();
			Temp_context = Temp_name.substring(0, 6);
			Temp_Element = res.createElement("tdValueDate");
			Temp_Element.setTextContent(SWT_Outgoing_Globals.formatDateForUB(Temp_context));
			rootElement.appendChild(Temp_Element);

			Temp_context = Temp_name.substring(6, 9);
			Temp_Element = res.createElement("tdCurrencyCode");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);

			Temp_context = Temp_name.substring(9, Temp_name.length());
			Temp_Element = res.createElement("tdAmount");
			Temp_Element.setTextContent(Temp_context.replaceAll(",", "."));
			rootElement.appendChild(Temp_Element);
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// instructedAmount Tag and instructedCurrency Tag
		if (doc.getElementsByTagName("CurrencyInstructedAmount").item(0) != null) {
			Temp_name = doc.getElementsByTagName("CurrencyInstructedAmount").item(0).getTextContent();
			if (Temp_name != "") {
				Temp_context = Temp_name.substring(0, 3);
				Temp_Element = res.createElement("instructedCurrency");
				Temp_Element.setTextContent(Temp_context);
				rootElement.appendChild(Temp_Element);

				Temp_context = Temp_name.substring(3, Temp_name.length());
				Temp_Element = res.createElement("instructedAmount");
				Temp_Element.setTextContent(Temp_context.replaceAll(",", "."));
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// ExchangeRate Tag
		if (doc.getElementsByTagName("ExchangeRate").item(0) != null) {
			Temp_context = doc.getElementsByTagName("ExchangeRate").item(0).getTextContent();
			Temp_Element = res.createElement("exchangeRate");
			Temp_Element.setTextContent(Temp_context.replaceAll(",", "."));
			rootElement.appendChild(Temp_Element);
		}

		if (doc.getElementsByTagName(ORDERING_CUSTOMER_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ORDERING_CUSTOMER_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ORDERING_CUSTOMER_A).item(0).getNodeName();
		} else if (doc.getElementsByTagName(ORDERING_CUSTOMER_K).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ORDERING_CUSTOMER_K).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ORDERING_CUSTOMER_K).item(0).getNodeName();
		} else if (doc.getElementsByTagName(ORDERING_CUSTOMER_F).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ORDERING_CUSTOMER_F).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ORDERING_CUSTOMER_F).item(0).getNodeName();
		}

		if (Temp_name != null) {
			if (Temp_context != "" && Temp_name.equals(ORDERING_CUSTOMER_A)) {

				Temp_Element = res.createElement(ORDERING_CUSTOMER);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(ORDERING_CUSTOMER_OPTION);
				Temp_Element.setTextContent("A");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(ORDERING_CUSTOMER_K)) {

				Temp_Element = res.createElement(ORDERING_CUSTOMER);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(ORDERING_CUSTOMER_OPTION);
				Temp_Element.setTextContent("K");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(ORDERING_CUSTOMER_F)) {

				Temp_Element = res.createElement(ORDERING_CUSTOMER);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(ORDERING_CUSTOMER_OPTION);
				Temp_Element.setTextContent("F");
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// sendingInstitution Tag
		if (doc.getElementsByTagName("SendingInstitution").item(0) != null) {
			Temp_context = doc.getElementsByTagName("SendingInstitution").item(0).getTextContent();
			Temp_Element = res.createElement("sendingInstitution");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// orderingInstitution Tag and orderInstitutionOption Tag
		if (doc.getElementsByTagName(ORDERING_INSTITUTION_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ORDERING_INSTITUTION_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ORDERING_INSTITUTION_A).item(0).getNodeName();
		} else if (doc.getElementsByTagName(ORDERING_INSTITUTION_D).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ORDERING_INSTITUTION_D).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ORDERING_INSTITUTION_D).item(0).getNodeName();
		}

		if (Temp_name != null) {
			if (Temp_context != "" && Temp_name.equals(ORDERING_INSTITUTION_A)) {

				Temp_Element = res.createElement("orderingInstitution");
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement("orderInstitutionOption");
				Temp_Element.setTextContent("A");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(ORDERING_INSTITUTION_D)) {

				Temp_Element = res.createElement("orderingInstitution");
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement("orderInstitutionOption");
				Temp_Element.setTextContent("D");
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}
		// sendersCorrespondent Tag and sendersCorrespOption Tag
		if (doc.getElementsByTagName(SENDERS_CORRESPONDENT_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(SENDERS_CORRESPONDENT_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(SENDERS_CORRESPONDENT_A).item(0).getNodeName();
		} else if (doc.getElementsByTagName(SENDERS_CORRESPONDENT_B).item(0) != null) {
			Temp_context = doc.getElementsByTagName(SENDERS_CORRESPONDENT_B).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(SENDERS_CORRESPONDENT_B).item(0).getNodeName();
		} else if (doc.getElementsByTagName(SENDERS_CORRESPONDENT_D).item(0) != null) {
			Temp_context = doc.getElementsByTagName(SENDERS_CORRESPONDENT_D).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(SENDERS_CORRESPONDENT_D).item(0).getNodeName();
		}

		if (Temp_name != null) {
			if (Temp_context != "" && Temp_name.equals(SENDERS_CORRESPONDENT_A)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(SENDERS_CORRESP_OPTION);
				Temp_Element.setTextContent("A");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(SENDERS_CORRESPONDENT_B)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(SENDERS_CORRESP_OPTION);
				Temp_Element.setTextContent("B");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(SENDERS_CORRESPONDENT_D)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(SENDERS_CORRESP_OPTION);
				Temp_Element.setTextContent("D");
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// receiversCorrespondent Tag and receiversCorrespOption tag
		if (doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_A).item(0).getNodeName();
		}
		if (doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_B).item(0) != null) {
			Temp_context = doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_B).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_B).item(0).getNodeName();
		}
		if (doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_D).item(0) != null) {
			Temp_context = doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_D).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(RECEIVERS_CORRESPONDENT_D).item(0).getNodeName();
		}

		if (Temp_name != null) {
			if (Temp_context != "" && Temp_name.equals(RECEIVERS_CORRESPONDENT_A)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(RECEIVERS_CORRESP_OPTION);
				Temp_Element.setTextContent("A");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(RECEIVERS_CORRESPONDENT_B)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(RECEIVERS_CORRESP_OPTION);
				Temp_Element.setTextContent("B");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(RECEIVERS_CORRESPONDENT_D)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(RECEIVERS_CORRESP_OPTION);
				Temp_Element.setTextContent("D");
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// thirdReimbursementInstitution Tag and thirdReimbursementInstOption
		// tag
		if (doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_A).item(0).getNodeName();
		} else if (doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_B).item(0) != null) {
			Temp_context = doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_B).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_B).item(0).getNodeName();
		} else if (doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_D).item(0) != null) {
			Temp_context = doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_D).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(THIRD_REIMBURSEMENT_INSTITUTION_D).item(0).getNodeName();
		}

		if (Temp_name != null) {
			if (Temp_context != "" && Temp_name.equals(THIRD_REIMBURSEMENT_INSTITUTION_A)) {

				Temp_Element = res.createElement(THIRD_REIMBURSEMENT_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(THIRD_REIMBURSEMENT_INST_OPTION);
				Temp_Element.setTextContent("A");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(THIRD_REIMBURSEMENT_INSTITUTION_B)) {

				Temp_Element = res.createElement(THIRD_REIMBURSEMENT_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(THIRD_REIMBURSEMENT_INST_OPTION);
				Temp_Element.setTextContent("B");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(THIRD_REIMBURSEMENT_INSTITUTION_D)) {

				Temp_Element = res.createElement(THIRD_REIMBURSEMENT_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(THIRD_REIMBURSEMENT_INST_OPTION);
				Temp_Element.setTextContent("D");
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// intermediaryInstitution Tag and intermediaryInstOption tag
		if (doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_A).item(0).getNodeName();
		}
		if (doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_C).item(0) != null) {
			Temp_context = doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_C).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_C).item(0).getNodeName();
		}
		if (doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_D).item(0) != null) {
			Temp_context = doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_D).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(INTERMEDIARY_INSTITUTION_D).item(0).getNodeName();
		}

		if (Temp_name != null) {
			if (Temp_context != "" && Temp_name.equals(INTERMEDIARY_INSTITUTION_A)) {

				Temp_Element = res.createElement(INTERMEDIARY_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(INTERMEDIARY_INST_OPTION);
				Temp_Element.setTextContent("A");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(INTERMEDIARY_INSTITUTION_C)) {

				Temp_Element = res.createElement(INTERMEDIARY_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(INTERMEDIARY_INST_OPTION);
				Temp_Element.setTextContent("C");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(INTERMEDIARY_INSTITUTION_D)) {

				Temp_Element = res.createElement(INTERMEDIARY_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(INTERMEDIARY_INST_OPTION);
				Temp_Element.setTextContent("D");
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// accountWithInstitution Tag and accountWithInstOption tag
		if (doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_A).item(0).getNodeName();
		}
		if (doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_B).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_B).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_B).item(0).getNodeName();
		}
		if (doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_C).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_C).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_C).item(0).getNodeName();
		}
		if (doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_D).item(0) != null) {
			Temp_context = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_D).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(ACCOUNT_WITH_INSTITUTION_D).item(0).getNodeName();
		}

		if (Temp_name != null) {
			if (Temp_context != "" && Temp_name.equals(ACCOUNT_WITH_INSTITUTION_A)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(ACCOUNT_WITH_INST_OPTION);
				Temp_Element.setTextContent("A");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(ACCOUNT_WITH_INSTITUTION_B)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(ACCOUNT_WITH_INST_OPTION);
				Temp_Element.setTextContent("B");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(ACCOUNT_WITH_INSTITUTION_C)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(ACCOUNT_WITH_INST_OPTION);
				Temp_Element.setTextContent("C");
				rootElement.appendChild(Temp_Element);
			} else if (Temp_context != "" && Temp_name.equals(ACCOUNT_WITH_INSTITUTION_D)) {

				Temp_Element = res.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
				Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
				rootElement.appendChild(Temp_Element);

				Temp_Element = res.createElement(ACCOUNT_WITH_INST_OPTION);
				Temp_Element.setTextContent("D");
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// beneficiaryCustomer Tag and beneficiaryCustOption Tag
		if (doc.getElementsByTagName("BeneficiaryCustomer").item(0) != null) {
			Temp_Element = res.createElement(BENEFICIARY_CUSTOMER);
			Temp_Element.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(doc.getElementsByTagName("BeneficiaryCustomer").item(0).getTextContent()));
			rootElement.appendChild(Temp_Element);
		}
		if (doc.getElementsByTagName(BENEFICIARY_CUSTOMER_A).item(0) != null) {
			Temp_context = doc.getElementsByTagName(BENEFICIARY_CUSTOMER_A).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(BENEFICIARY_CUSTOMER_A).item(0).getNodeName();
			Temp_Element = res.createElement(BENEFICIARY_CUSTOMER);
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		if (doc.getElementsByTagName(BENEFICIARY_CUSTOMER_F).item(0) != null) {
			Temp_context = doc.getElementsByTagName(BENEFICIARY_CUSTOMER_F).item(0).getTextContent();
			Temp_name = doc.getElementsByTagName(BENEFICIARY_CUSTOMER_F).item(0).getNodeName();
			Temp_Element = res.createElement(BENEFICIARY_CUSTOMER);
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		if (Temp_name != null) {
			if (Temp_context != "") {

				if (res.getElementsByTagName(BENEFICIARY_CUSTOMER).item(0) != null)

					res.getElementsByTagName(BENEFICIARY_CUSTOMER).item(0)
							.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));

				Temp_Element = res.createElement("beneficiaryCustOption");
				if (BENEFICIARY_CUSTOMER_A.equals(Temp_name)) {
					Temp_Element.setTextContent("A");
				} else if (BENEFICIARY_CUSTOMER_F.equals(Temp_name)) {
					Temp_Element.setTextContent("F");
				}
				rootElement.appendChild(Temp_Element);
			}
			Temp_Element = null;
			Temp_context = null;
			Temp_name = null;
		}

		// RemittanceInfo Tag
		if (doc.getElementsByTagName("RemittanceInfo").item(0) != null) {
			Temp_context = doc.getElementsByTagName("RemittanceInfo").item(0).getTextContent();
			Temp_Element = res.createElement("remittanceInfo");
			Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// DetailsOfCharges Tag
		if (doc.getElementsByTagName("DetailsOfCharges").item(0) != null) {
			Temp_context = doc.getElementsByTagName("DetailsOfCharges").item(0).getTextContent();
			Temp_Element = res.createElement("detailsOfCharges");
			Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// Charges Tag - Collection
		if (doc.getElementsByTagName(CHARGES).item(0) != null) {
			Temp_Element = res.createElement("charges");
			rootElement.appendChild(Temp_Element);
			for (int j = 0; j < doc.getElementsByTagName(CHARGES).getLength(); j++) {
				Temp_Node = doc.getElementsByTagName(CHARGES).item(j).getChildNodes();
				for (int i = 0; i < Temp_Node.getLength(); i++) {
					if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Temp_context = Temp_Node.item(i).getTextContent();
						Temp_Element = res.createElement("senderCharge");
						Temp_Element.setTextContent(Temp_context.replaceAll(",", "."));
						res.getElementsByTagName("charges").item(0).appendChild(Temp_Element);
					}
				}
			}
		}
		Temp_Node = null;

		// ReceiversCharges Tag
		if (doc.getElementsByTagName("ReceiversCharges").item(0) != null) {
			Temp_context = doc.getElementsByTagName("ReceiversCharges").item(0).getTextContent();
			Temp_Element = res.createElement("receiversCharges");
			Temp_Element.setTextContent(Temp_context.replaceAll(",", "."));
			rootElement.appendChild(Temp_Element);
		}

		// SenderToReceiverInfo Tag
		if (doc.getElementsByTagName("SenderToReceiverInfo").item(0) != null) {
			Temp_context = doc.getElementsByTagName("SenderToReceiverInfo").item(0).getTextContent();
			Temp_Element = res.createElement("senderToReceiverInfo");
			Temp_Element.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// B3Validation Tag
		if (doc.getElementsByTagName("B3Validation").item(0) != null) {
			Temp_context = doc.getElementsByTagName("B3Validation").item(0).getTextContent();
			if (Temp_context != "") {
				Temp_Element = res.createElement("stp");
				Temp_Element.setTextContent("Y");
				rootElement.appendChild(Temp_Element);
			}
		} else {
			Temp_Element = res.createElement("stp");
			Temp_Element.setTextContent("N");
			rootElement.appendChild(Temp_Element);
		}
		// End2EndTxnRef Tag
		if (doc.getElementsByTagName("B3End2EndTxnRef").item(0) != null) {
			Temp_context = doc.getElementsByTagName("B3End2EndTxnRef").item(0).getTextContent();
			if (Temp_context != "") {
				Temp_Element = res.createElement("end2EndTxnRef");
				Temp_Element.setTextContent(Temp_context);
				rootElement.appendChild(Temp_Element);
			}
		}

		// ServiceTypeId Tag
		if (doc.getElementsByTagName("B3ServiceTypeId").item(0) != null) {
			Temp_context = doc.getElementsByTagName("B3ServiceTypeId").item(0).getTextContent();
			if (Temp_context != "") {
				Temp_Element = res.createElement("serviceTypeId");
				Temp_Element.setTextContent(Temp_context);
				rootElement.appendChild(Temp_Element);
			}
		}

	}

}
