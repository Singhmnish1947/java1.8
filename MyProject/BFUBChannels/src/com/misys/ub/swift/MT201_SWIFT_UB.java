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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;

public class MT201_SWIFT_UB {
	private static final String SUM_OF_AMOUNTS = "SumOfAmounts";

	private static final String SENDER_TO_RECEIVER_INFO = "SenderToReceiverInfo";

	private static final String MT201_DETAIL = "mt201Detail";

	private transient final static Log LOGGER = LogFactory
			.getLog(MT201_SWIFT_UB.class.getName());

	private static String strResult;
	public static final String XMLFILEPATH = "response_switf.xml";
	public static String Temp_context;
	public static String Temp_name;
	public static NodeList Temp_Node;
	public static Double Temp_Double;
	public static Element rootElement;
	public static Element Temp_Element;
	public static SimpleDateFormat format_date = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	public static String MT201_SWIFT_Transform(String requestMsg) {
		String requestMsg1 = requestMsg.replaceAll("/n", "");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {

			dBuilder = dbFactory.newDocumentBuilder();
			Document res = dBuilder.newDocument();
			InputSource is = new InputSource(new StringReader(requestMsg1));
			Document doc = dBuilder.parse(is);
			rootElement = res.createElement("UB_MT201");
			res.appendChild(rootElement);
			rootElement.setAttribute(PaymentSwiftConstants.XMLNS,
					PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);

			doc.getDocumentElement().normalize();

			// update Element value
			updateElementValue(doc, res);

			res.getDocumentElement().normalize();

			// write the updated document to forward request
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "5");
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

		} catch (SAXException | ParserConfigurationException | IOException
				| TransformerException e1) {
			e1.printStackTrace();
		}
		return strResult;
	}

	private static void updateElementValue(Document doc, Document res) {

		Temp_Element = res.createElement("header");
		Temp_Element.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		rootElement.appendChild(Temp_Element);
		rootElement = Temp_Element;

		// MessageID Tag
		if (doc.getElementsByTagName("MessageID").item(0) != null) {
			Temp_context = doc.getElementsByTagName("MessageID").item(0)
					.getTextContent();
			Temp_Element = res.createElement("messageId1");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// ExternalMessageType Tag
		if (doc.getElementsByTagName("ExternalMessageType").item(0) != null) {
			Temp_context = doc.getElementsByTagName("ExternalMessageType")
					.item(0).getTextContent();
			Temp_Element = res.createElement("messageType");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		Temp_Element = res.createElement("details");
		Temp_Element.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		res.getElementsByTagName("UB_MT201").item(0).appendChild(Temp_Element);
		rootElement = Temp_Element;

		// CancellationAction Tag
		if (doc.getElementsByTagName("CancellationAction").item(0) != null) {
			Temp_context = doc.getElementsByTagName("CancellationAction")
					.item(0).getTextContent();
			Temp_Element = res.createElement("action");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// DestinationAddress Tag
		if (doc.getElementsByTagName("DestinationAddress").item(0) != null) {
			Temp_context = doc.getElementsByTagName("DestinationAddress")
					.item(0).getTextContent();
			Temp_Element = res.createElement("receiver");
			Temp_Element.setTextContent(SWT_Outgoing_Globals
					.convertBIC(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// SenderAddress tag
		if (doc.getElementsByTagName("SenderAddress").item(0) != null) {
			Temp_context = doc.getElementsByTagName("SenderAddress").item(0)
					.getTextContent();
			Temp_Element = res.createElement("sender");
			Temp_Element.setTextContent(SWT_Outgoing_Globals
					.convertBIC(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// SenderToReceiverInfo Tag
		if (doc.getElementsByTagName(SENDER_TO_RECEIVER_INFO).item(0) != null) {
			Temp_context = doc.getElementsByTagName(SENDER_TO_RECEIVER_INFO)
					.item(0).getTextContent();
			Temp_Element = res.createElement("senderToReceiverInfo");
			Temp_Element.setTextContent(Temp_context);
			rootElement.appendChild(Temp_Element);
		}

		// SendersCorrespondent
		if (doc.getElementsByTagName("SendersCorrespondent").item(0) != null) {
			Temp_context = doc.getElementsByTagName("SendersCorrespondent")
					.item(0).getTextContent();
			if (Temp_context != null) {
				Temp_Element = res.createElement("sendersCorrespondent");
				Temp_Element.setTextContent(SWT_Outgoing_Globals
						.convertBIC(Temp_context));
				rootElement.appendChild(Temp_Element);
			}
		}

		// TransactionValueDate Tag
		if (doc.getElementsByTagName("TransactionValueDate").item(0) != null) {
			Temp_context = doc.getElementsByTagName("TransactionValueDate")
					.item(0).getTextContent();
			Temp_Element = res.createElement("valueDate");
			Temp_Element.setTextContent(SWT_Outgoing_Globals
					.formatDateForUB(Temp_context));
			rootElement.appendChild(Temp_Element);
		}

		// SumOfAmounts Tag
		if (doc.getElementsByTagName(SUM_OF_AMOUNTS).item(0) != null) {
			Temp_context = doc.getElementsByTagName(SUM_OF_AMOUNTS).item(0)
					.getTextContent();
			Temp_Element = res.createElement("sumOfAmounts");
			Temp_Element.setTextContent(Temp_context.replaceAll(",", "."));
			rootElement.appendChild(Temp_Element);
		}

		// Instruction Tag - Collection
		if (doc.getElementsByTagName(SUM_OF_AMOUNTS).item(0) != null) {
			for (int j = 0; j < doc.getElementsByTagName("MessageDetails")
					.getLength(); j++) {
				Temp_Element = res.createElement(MT201_DETAIL);
				Temp_Element.setAttribute(PaymentSwiftConstants.XMLNS,
						PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
				rootElement.appendChild(Temp_Element);
				Temp_Node = doc.getElementsByTagName("MessageDetails").item(j)
						.getChildNodes();
				for (int i = 0; i < Temp_Node.getLength(); i++) {
					if (Temp_Node.item(i).getNodeType() == Node.ELEMENT_NODE) {
						if (Temp_Node.item(i).getTextContent() != "") {
							if (Temp_Node.item(i).getNodeName()
									.equals("IntermediaryA")) {
								Temp_context = Temp_Node.item(i)
										.getTextContent();
								Temp_Element = res
										.createElement("intermediary");
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter(Temp_context));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);

								Temp_Element = res
										.createElement("intermediaryOption");
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter("A"));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);
							} else if (Temp_Node.item(i).getNodeName()
									.equals("IntermediaryD")) {
								Temp_context = Temp_Node.item(i)
										.getTextContent();
								Temp_Element = res
										.createElement("intermediary");
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter(Temp_context));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);

								Temp_Element = res
										.createElement("intermediaryOption");
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter("D"));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);
							}

							if (Temp_Node.item(i).getNodeName()
									.equals("AccountWithInstitutionA")) {
								Temp_context = Temp_Node.item(i)
										.getTextContent();
								Temp_Element = res
										.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter(Temp_context));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);

								Temp_Element = res
										.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION_OPTION);
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter("A"));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);
							} else if (Temp_Node.item(i).getNodeName()
									.equals("AccountWithInstitutionB")) {
								Temp_context = Temp_Node.item(i)
										.getTextContent();
								Temp_Element = res
										.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter(Temp_context));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);

								Temp_Element = res
										.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION_OPTION);
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter("B"));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);
							} else if (Temp_Node.item(i).getNodeName()
									.equals("AccountWithInstitutionD")) {
								Temp_context = Temp_Node.item(i)
										.getTextContent();
								Temp_Element = res
										.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter(Temp_context));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);

								Temp_Element = res
										.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION_OPTION);
								Temp_Element
										.setTextContent(SWT_Outgoing_Globals
												.AddUBDelimiter("D"));
								res.getElementsByTagName(MT201_DETAIL).item(j)
										.appendChild(Temp_Element);
							}
						}
						if (Temp_Node.item(i).getNodeName().equals("TRN")) {
							Temp_context = Temp_Node.item(i).getTextContent();
							Temp_Element = res
									.createElement("transactionReferenceNumber");
							Temp_Element.setTextContent(Temp_context);
							res.getElementsByTagName(MT201_DETAIL).item(j)
									.appendChild(Temp_Element);
						}

						if (Temp_Node.item(i).getNodeName().equals("CcyAmount")) {
							Temp_context = Temp_Node.item(i).getTextContent()
									.substring(0, 3);
							Temp_Element = res.createElement("currency");
							Temp_Element.setTextContent(Temp_context);
							res.getElementsByTagName(MT201_DETAIL).item(j)
									.appendChild(Temp_Element);

							Temp_context = Temp_Node
									.item(i)
									.getTextContent()
									.substring(
											3,
											Temp_Node.item(i).getTextContent()
													.length());
							Temp_Element = res.createElement("amount");
							Temp_Element.setTextContent(Temp_context
									.replaceAll(",", "."));
							res.getElementsByTagName(MT201_DETAIL).item(j)
									.appendChild(Temp_Element);
						}

						// SenderToReceiverInfo Tag
						if (Temp_Node.item(i).getNodeName()
								.equals(SENDER_TO_RECEIVER_INFO)) {
							Temp_context = Temp_Node.item(i).getTextContent();
							Temp_Element = res
									.createElement("senderToReceiverInfo");
							Temp_Element.setTextContent(SWT_Outgoing_Globals
									.AddUBDelimiter(Temp_context));
							res.getElementsByTagName(MT201_DETAIL).item(j)
									.appendChild(Temp_Element);
						}
					}
				}
			}
		}
		Temp_Node = null;

	}

}
