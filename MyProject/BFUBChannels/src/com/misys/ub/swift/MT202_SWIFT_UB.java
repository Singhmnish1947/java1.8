package com.misys.ub.swift;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;

public class MT202_SWIFT_UB {
	private transient final static Log LOGGER = LogFactory
			.getLog(MT202_SWIFT_UB.class.getName());

	private static String strResult;

	public static String MT202_Transform(String requestMsg)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException, SAXException,
			IOException, ParserConfigurationException {
		String requestMsg1 = requestMsg.replaceAll("/n", "");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {

			dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(requestMsg1));
			Document doc = dBuilder.parse(is);

			doc.getDocumentElement().normalize();

			// update Element value
			Document respDoc = updateElementValue(dBuilder, doc);

			// write the updated document to forward request
			respDoc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(respDoc);
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

	private static Document updateElementValue(DocumentBuilder dBuilder,
			Document doc) {
		String temp_option = "";
		Node tag;
		Element newTag;

		Document respDoc = dBuilder.newDocument();
		Element detailsTag = respDoc.createElement("details");
		Element headerTag = respDoc.createElement("header");
		Element respRootElement = respDoc.createElement("UB_MT202");

		respRootElement.appendChild(headerTag);
		respRootElement.appendChild(detailsTag);
		respDoc.appendChild(respRootElement);

		respRootElement.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		headerTag.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		detailsTag.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);

		tag = doc.getElementsByTagName("ExternalMessageType").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("messageType");
			newTag.setTextContent(tag.getTextContent());
			headerTag.appendChild(newTag);
		}

		tag = doc.getElementsByTagName("MessageID").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("messageId1");
			newTag.setTextContent(tag.getTextContent());
			headerTag.appendChild(newTag);
		}

		tag = doc.getElementsByTagName("SenderAddress").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("sender");
			newTag.setTextContent(SWT_Outgoing_Globals.convertBIC(tag
					.getTextContent()));
			detailsTag.appendChild(newTag);
		}

		tag = doc.getElementsByTagName("DestinationAddress").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("receiver");
			newTag.setTextContent(SWT_Outgoing_Globals.convertBIC(tag
					.getTextContent()));
			detailsTag.appendChild(newTag);
		}

		tag = doc.getElementsByTagName("CancellationAction").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("action");
			newTag.setTextContent(tag.getTextContent());
			detailsTag.appendChild(newTag);
		}

		tag = doc.getElementsByTagName("TRN").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("transactionReferenceNumber");
			newTag.setTextContent(tag.getTextContent());
			detailsTag.appendChild(newTag);
		}
		tag = doc.getElementsByTagName("RelatedReference").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("relatedReference");
			newTag.setTextContent(tag.getTextContent());
			detailsTag.appendChild(newTag);
		}

		tag = doc.getElementsByTagName("ValueDateCcyAmount").item(0);
		if (tag != null && tag.getTextContent() != "") {

			String source_ValueDateCcyAmount = tag.getTextContent();
			String date_MT202 = source_ValueDateCcyAmount.substring(0, 6);
			String currency_MT202 = source_ValueDateCcyAmount.substring(6, 9);
			String amount_MT202 = source_ValueDateCcyAmount.substring(9,
					source_ValueDateCcyAmount.length());
			tag = respDoc.createElement("tdValueDate");
			tag.setTextContent(SWT_Outgoing_Globals.formatDateForUB(date_MT202));
			detailsTag.appendChild(tag);
			tag = respDoc.createElement("tdCurrencyCode");
			tag.setTextContent(currency_MT202);
			detailsTag.appendChild(tag);
			tag = respDoc.createElement("tdAmount");
			tag.setTextContent(amount_MT202.replaceAll(",", "."));
			detailsTag.appendChild(tag);
		}

		// OrderingInstitution
		temp_option = "";
		tag = doc.getElementsByTagName("OrderingInstitutionA").item(0);
		if (tag == null) {
			tag = doc.getElementsByTagName("OrderingInstitutionD").item(0);
			if (tag == null) {
				temp_option = "";
			} else {
				temp_option = "D";
			}
		} else {
			temp_option = "A";
		}

		if (temp_option != "") {
			tag = doc.getElementsByTagName("OrderingInstitution" + temp_option)
					.item(0);
			if (tag.getTextContent() != "") {
				newTag = respDoc.createElement("orderingInstitution");
				newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
						.getTextContent()));
				detailsTag.appendChild(newTag);

				newTag = respDoc.createElement("orderingInstitutionOption");
				newTag.setTextContent(temp_option);
				detailsTag.appendChild(newTag);
			}
		}

		// SendersCorrespondent
		temp_option = "";
		tag = doc.getElementsByTagName("SendersCorrespondentA").item(0);
		if (tag == null) {
			tag = doc.getElementsByTagName("SendersCorrespondentB").item(0);
			if (tag == null) {
				tag = doc.getElementsByTagName("SendersCorrespondentD").item(0);
				if (tag == null) {
					temp_option = "";
				} else {
					temp_option = "D";
				}
			} else {
				temp_option = "B";
			}
		} else {
			temp_option = "A";
		}

		if (temp_option != "") {
			tag = doc
					.getElementsByTagName("SendersCorrespondent" + temp_option)
					.item(0);
			if (tag.getTextContent() != "") {
				newTag = respDoc.createElement("sendersCorrespondent");
				newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
						.getTextContent()));
				detailsTag.appendChild(newTag);

				newTag = respDoc.createElement("sendersCorrespondentOption");
				newTag.setTextContent(temp_option);
				detailsTag.appendChild(newTag);
			}
		}

		// ReceiversCorrespondent
		temp_option = "";
		tag = doc.getElementsByTagName("ReceiversCorrespondentA").item(0);
		if (tag == null) {
			tag = doc.getElementsByTagName("ReceiversCorrespondentB").item(0);
			if (tag == null) {
				tag = doc.getElementsByTagName("ReceiversCorrespondentD").item(
						0);
				if (tag == null) {
					temp_option = "";
				} else {
					temp_option = "D";
				}
			} else {
				temp_option = "B";
			}
		} else {
			temp_option = "A";
		}

		if (temp_option != "") {
			tag = doc.getElementsByTagName(
					"ReceiversCorrespondent" + temp_option).item(0);
			if (tag.getTextContent() != "") {
				newTag = respDoc.createElement("receiversCorrespondent");
				newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
						.getTextContent()));
				detailsTag.appendChild(newTag);

				newTag = respDoc.createElement("receiversCorrespondentOption");
				newTag.setTextContent(temp_option);
				detailsTag.appendChild(newTag);
			}
		}

		// IntermediaryInstitution
		temp_option = "";
		tag = doc.getElementsByTagName("IntermediaryInstitutionA").item(0);
		if (tag == null) {
			tag = doc.getElementsByTagName("IntermediaryInstitutionD").item(0);
			if (tag == null) {
				temp_option = "";
			} else {
				temp_option = "D";
			}
		} else {
			temp_option = "A";
		}

		if (temp_option != "") {
			tag = doc.getElementsByTagName(
					"IntermediaryInstitution" + temp_option).item(0);
			if (tag.getTextContent() != "") {
				newTag = respDoc.createElement("intermediary");
				newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
						.getTextContent()));
				detailsTag.appendChild(newTag);

				newTag = respDoc.createElement("intermediaryOption");
				newTag.setTextContent(temp_option);
				detailsTag.appendChild(newTag);
			}
		}

		// AccountWithInstitution
		temp_option = "";
		tag = doc.getElementsByTagName("AccountWithInstitutionA").item(0);
		if (tag == null) {
			tag = doc.getElementsByTagName("AccountWithInstitutionB").item(0);
			if (tag == null) {
				tag = doc.getElementsByTagName("AccountWithInstitutionD").item(
						0);
				if (tag == null) {
					temp_option = "";
				} else {
					temp_option = "D";
				}
			} else {
				temp_option = "B";
			}
		} else {
			temp_option = "A";
		}

		if (temp_option != "") {
			tag = doc.getElementsByTagName(
					"AccountWithInstitution" + temp_option).item(0);
			if (tag.getTextContent() != "") {
				newTag = respDoc.createElement("accountWithInstitution");
				newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
						.getTextContent()));
				detailsTag.appendChild(newTag);

				newTag = respDoc.createElement("accountWithInstitutionOption");
				newTag.setTextContent(temp_option);
				detailsTag.appendChild(newTag);
			}
		}

		// Beneficiary
		temp_option = "";
		tag = doc.getElementsByTagName("BeneficiaryA").item(0);
		if (tag == null) {
			tag = doc.getElementsByTagName("BeneficiaryD").item(0);
			if (tag == null) {
				temp_option = "";
			} else {
				temp_option = "D";
			}
		} else {
			temp_option = "A";
		}

		if (temp_option != "") {
			tag = doc.getElementsByTagName("Beneficiary" + temp_option).item(0);
			if (tag.getTextContent() != "") {
				newTag = respDoc.createElement("beneficiary");
				newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
						.getTextContent()));
				detailsTag.appendChild(newTag);

				newTag = respDoc.createElement("beneficiaryOption");
				newTag.setTextContent(temp_option);
				detailsTag.appendChild(newTag);
			}
		}

		tag = doc.getElementsByTagName("SenderToReceiverInfo").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("sendertoReceiverInformation");
			newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
					.getTextContent()));
			detailsTag.appendChild(newTag);
		}
		
		//Added as part of Non STP
		tag = doc.getElementsByTagName("B3Validation").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("cover");
			newTag.setTextContent(tag.getTextContent());
			detailsTag.appendChild(newTag);
		}
		
		//Added as part SWIFT 2018 changes
		tag = doc.getElementsByTagName("B3ServiceTypeId").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("serviceTypeId");
			newTag.setTextContent(tag.getTextContent());
			detailsTag.appendChild(newTag);
		}
		tag = doc.getElementsByTagName("B3End2EndTxnRef").item(0);
		if (tag != null) {
			newTag = respDoc.createElement("end2EndTxnRef");
			newTag.setTextContent(tag.getTextContent());
			detailsTag.appendChild(newTag);
		}

		return respDoc;
	}
}