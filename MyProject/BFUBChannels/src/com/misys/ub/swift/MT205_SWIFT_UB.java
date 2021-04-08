package com.misys.ub.swift;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;

/**
 * @author Rishav
 * 
 *         file to transform SWIFT_MT205
 */

public class MT205_SWIFT_UB {

	private transient final static Log LOGGER = LogFactory
			.getLog(MT205_SWIFT_UB.class.getName());

	private static String strResult;

	public static String MT205_Transform(String requestMsg) {
		String requestMsg1 = requestMsg.replaceAll("\\r|\\n|\\t", "");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(requestMsg1));
			Document document = dBuilder.parse(is);

			document.getDocumentElement().normalize();
			// update Element value
			Document resultDocument = updateElementValue(dBuilder, document);

			// write the updated document to forward request
			resultDocument.getDocumentElement().normalize();

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(resultDocument);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			strResult = writer.toString();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(strResult);
				LOGGER.info("XML file updated successfully");
			}
		}

		catch (SAXException | ParserConfigurationException | IOException
				| TransformerException e) {
			e.printStackTrace();
		}
		return strResult;
	}

	public static Document updateElementValue(DocumentBuilder dBuilder,
			Document document) {
		Document resultDocument = dBuilder.newDocument();

		Node externalMessageType = document.getElementsByTagName(
				"ExternalMessageType").item(0);
		Element resultRootElement = resultDocument.createElement("UB_"
				+ externalMessageType.getTextContent());
		resultDocument.appendChild(resultRootElement);
		resultRootElement.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);

		// tag : header
		Element headerNew = resultDocument.createElement("header");
		headerNew.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		resultRootElement.appendChild(headerNew);

		// tag : messageType
		Element externalMessageTypeNew = resultDocument
				.createElement("messageType");
		if (!externalMessageType.getTextContent().equals("")) {
			externalMessageTypeNew.setTextContent(externalMessageType
					.getTextContent());
		}
		headerNew.appendChild(externalMessageTypeNew);

		// tag : messageId
		Node messageID = document.getElementsByTagName("MessageID").item(0);
		Element messageIDNew = resultDocument.createElement("messageId1");
		messageIDNew.setTextContent(Double.toString(Double
				.parseDouble(messageID.getTextContent())));
		headerNew.appendChild(messageIDNew);

		// tag : details
		Element detailsNew = resultDocument.createElement("details");
		detailsNew.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		resultRootElement.appendChild(detailsNew);

		// tag : sender
		try {
			Node senderAddress = document.getElementsByTagName("SenderAddress")
					.item(0);
			Element senderNew = resultDocument.createElement("sender");
			senderNew.setTextContent(SWT_Outgoing_Globals
					.convertBIC(senderAddress.getTextContent()));
			detailsNew.appendChild(senderNew);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		// tag : receiver
		Node receiverAddress = document.getElementsByTagName("ReceiverAddress")
				.item(0);
		if (receiverAddress != null) {
			Element receiverNew = resultDocument.createElement("receiver");
			receiverNew.setTextContent(SWT_Outgoing_Globals
					.convertBIC(receiverAddress.getTextContent()));
			detailsNew.appendChild(receiverNew);
		}

		// tag : action
		Node cancellationAction = document.getElementsByTagName(
				"CancellationAction").item(0);
		if (cancellationAction != null) {
			Element actionNew = resultDocument.createElement("action");
			actionNew.setTextContent(cancellationAction.getTextContent());
			detailsNew.appendChild(actionNew);
		}

		// tag : disposalRef
		Node hostReference = document.getElementsByTagName("HostReference")
				.item(0);
		if (hostReference != null) {
			Element disposalRefNew = resultDocument
					.createElement("disposalRef");
			disposalRefNew.setTextContent(hostReference.getTextContent());
			detailsNew.appendChild(disposalRefNew);
		}

		// tag : transactionReferenceNumber
		Node tRN = document.getElementsByTagName("TRN").item(0);
		if (tRN != null) {
			Element transactionReferenceNumberNew = resultDocument
					.createElement("transactionReferenceNumber");
			transactionReferenceNumberNew.setTextContent(tRN.getTextContent());
			detailsNew.appendChild(transactionReferenceNumberNew);
		}

		// tag : sendersCorrespondent and sendersCorrespondentOption
		NodeList sendersCorrespondentAList = document
				.getElementsByTagName("SendersCorrespondentA");
		NodeList sendersCorrespondentBList = document
				.getElementsByTagName("SendersCorrespondentB");
		NodeList sendersCorrespondentDList = document
				.getElementsByTagName("SendersCorrespondentD");

		if (sendersCorrespondentAList.getLength() > 0) {
			Element sendersCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
			sendersCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendersCorrespondentAList.item(0)
							.getTextContent()));
			detailsNew.appendChild(sendersCorrespondentNew);

			Element sendersCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT_OPTION);
			sendersCorrespondentOptionNew.setTextContent("A");
			detailsNew.appendChild(sendersCorrespondentOptionNew);
		} else if (sendersCorrespondentBList.getLength() > 0) {
			Element sendersCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
			sendersCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendersCorrespondentBList.item(0)
							.getTextContent()));
			detailsNew.appendChild(sendersCorrespondentNew);

			Element sendersCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT_OPTION);
			sendersCorrespondentOptionNew.setTextContent("B");
			detailsNew.appendChild(sendersCorrespondentOptionNew);
		} else if (sendersCorrespondentDList.getLength() > 0) {
			Element sendersCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
			sendersCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendersCorrespondentDList.item(0)
							.getTextContent()));
			detailsNew.appendChild(sendersCorrespondentNew);

			Element sendersCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT_OPTION);
			sendersCorrespondentOptionNew.setTextContent("D");
			detailsNew.appendChild(sendersCorrespondentOptionNew);
		}

		// tag : intermediary and intermediaryOption
		NodeList intermediaryAList = document
				.getElementsByTagName("IntermediaryA");
		NodeList intermediaryDList = document
				.getElementsByTagName("IntermediaryD");

		if (intermediaryAList.getLength() > 0) {
			Element intermediaryNew = resultDocument
					.createElement("intermediary");
			intermediaryNew
					.setTextContent(SWT_Outgoing_Globals
							.AddUBDelimiter(intermediaryAList.item(0)
									.getTextContent()));
			detailsNew.appendChild(intermediaryNew);

			Element intermediaryOptionNew = resultDocument
					.createElement("intermediaryOption");
			intermediaryOptionNew.setTextContent("A");
			detailsNew.appendChild(intermediaryOptionNew);
		} else if (intermediaryDList.getLength() > 0) {
			Element intermediaryNew = resultDocument
					.createElement("intermediary");
			intermediaryNew
					.setTextContent(SWT_Outgoing_Globals
							.AddUBDelimiter(intermediaryDList.item(0)
									.getTextContent()));
			detailsNew.appendChild(intermediaryNew);

			Element intermediaryOptionNew = resultDocument
					.createElement("intermediaryOption");
			intermediaryOptionNew.setTextContent("D");
			detailsNew.appendChild(intermediaryOptionNew);
		}

		// tag : accountWithInstitution and accountWithInstitutionOption
		NodeList accountWithInstAList = document
				.getElementsByTagName("AccountWithInstA");
		NodeList accountWithInstBList = document
				.getElementsByTagName("AccountWithInstB");
		NodeList accountWithInstDList = document
				.getElementsByTagName("AccountWithInstD");

		if (accountWithInstAList.getLength() > 0) {
			Element accountWithInstitutionNew = resultDocument
					.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
			accountWithInstitutionNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(accountWithInstAList.item(0)
							.getTextContent()));
			detailsNew.appendChild(accountWithInstitutionNew);

			Element accountWithInstOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INST_OPTION);
			accountWithInstOptionNew.setTextContent("A");
			detailsNew.appendChild(accountWithInstOptionNew);
		} else if (accountWithInstBList.getLength() > 0) {
			Element accountWithInstitutionNew = resultDocument
					.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
			accountWithInstitutionNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(accountWithInstBList.item(0)
							.getTextContent()));
			detailsNew.appendChild(accountWithInstitutionNew);

			Element accountWithInstOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INST_OPTION);
			accountWithInstOptionNew.setTextContent("B");
			detailsNew.appendChild(accountWithInstOptionNew);
		} else if (accountWithInstDList.getLength() > 0) {
			Element accountWithInstitutionNew = resultDocument
					.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
			accountWithInstitutionNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(accountWithInstDList.item(0)
							.getTextContent()));
			detailsNew.appendChild(accountWithInstitutionNew);

			Element accountWithInstOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INST_OPTION);
			accountWithInstOptionNew.setTextContent("D");
			detailsNew.appendChild(accountWithInstOptionNew);
		}

		// tag : sendertoReceiverInformation
		Node sendertoReceiverInfo = document.getElementsByTagName(
				"SendertoReceiverInfo").item(0);
		if (sendertoReceiverInfo != null) {
			Element sendertoReceiverInformationNew = resultDocument
					.createElement("sendertoReceiverInformation");
			sendertoReceiverInformationNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendertoReceiverInfo.getTextContent()));
			detailsNew.appendChild(sendertoReceiverInformationNew);
		}

		// tags : tdvalueDate, tdcurrencyCode and tdamount
		Node valueDateCcyAmount = document.getElementsByTagName(
				"ValueDateCcyAmount").item(0);
		if (valueDateCcyAmount != null) {
			Element tdvalueDateNew = resultDocument
					.createElement("tdvalueDate");
			Element tdcurrencyCodeNew = resultDocument
					.createElement("tdcurrencyCode");
			Element tdamountNew = resultDocument.createElement("tdamount");
			String date = valueDateCcyAmount.getTextContent().substring(0, 6);
			String currency = valueDateCcyAmount.getTextContent().substring(6,
					9);
			String amount = valueDateCcyAmount.getTextContent().substring(9);
			tdvalueDateNew.setTextContent(SWT_Outgoing_Globals
					.formatDateForUB(date));
			tdcurrencyCodeNew.setTextContent(currency);
			tdamountNew.setTextContent(amount.replaceAll(",", "."));
			detailsNew.appendChild(tdvalueDateNew);
			detailsNew.appendChild(tdamountNew);
			detailsNew.appendChild(tdcurrencyCodeNew);
		}

		// tag : relatedReference
		Node relatedReference = document.getElementsByTagName(
				"RelatedReference").item(0);
		if (relatedReference != null) {
			Element relatedReferenceNew = resultDocument
					.createElement("relatedReference");
			relatedReferenceNew.setTextContent(relatedReference
					.getTextContent());
			detailsNew.appendChild(relatedReferenceNew);
		}

		// tag : orderingInstitute and orderingInstituteOption
		NodeList orderingInstitutionAList = document
				.getElementsByTagName("OrderingInstitutionA");
		NodeList orderingInstitutionDList = document
				.getElementsByTagName("OrderingInstitutionD");

		if (orderingInstitutionAList.getLength() > 0) {
			Element orderingInstituteNew = resultDocument
					.createElement("orderingInstitute");
			orderingInstituteNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(orderingInstitutionAList.item(0)
							.getTextContent()));
			detailsNew.appendChild(orderingInstituteNew);

			Element orderingInstituteOptionNew = resultDocument
					.createElement("orderingInstitutionOption");
			orderingInstituteOptionNew.setTextContent("A");
			detailsNew.appendChild(orderingInstituteOptionNew);
		} else if (orderingInstitutionDList.getLength() > 0) {
			Element orderingInstituteNew = resultDocument
					.createElement("orderingInstitute");
			orderingInstituteNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(orderingInstitutionDList.item(0)
							.getTextContent()));
			detailsNew.appendChild(orderingInstituteNew);

			Element orderingInstituteOptionNew = resultDocument
					.createElement("orderingInstitutionOption");
			orderingInstituteOptionNew.setTextContent("D");
			detailsNew.appendChild(orderingInstituteOptionNew);
		}

		// tag : beneficiary and beneficiaryOption
		NodeList beneficiaryAList = document
				.getElementsByTagName("BeneficiaryA");
		NodeList beneficiaryDList = document
				.getElementsByTagName("BeneficiaryD");

		if (beneficiaryAList.getLength() > 0) {
			Element beneficiaryInstituteNew = resultDocument
					.createElement("beneficiaryInstitute");
			beneficiaryInstituteNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(beneficiaryAList.item(0).getTextContent()));
			detailsNew.appendChild(beneficiaryInstituteNew);

			Element beneficiaryInstOptionNew = resultDocument
					.createElement("beneficiaryInstOption");
			beneficiaryInstOptionNew.setTextContent("A");
			detailsNew.appendChild(beneficiaryInstOptionNew);
		} else if (beneficiaryDList.getLength() > 0) {
			Element beneficiaryInstituteNew = resultDocument
					.createElement("beneficiaryInstitute");
			beneficiaryInstituteNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(beneficiaryDList.item(0).getTextContent()));
			detailsNew.appendChild(beneficiaryInstituteNew);

			Element beneficiaryInstOptionNew = resultDocument
					.createElement("beneficiaryInstOption");
			beneficiaryInstOptionNew.setTextContent("D");
			detailsNew.appendChild(beneficiaryInstOptionNew);
		}
		
		//Added as part of Non STP
		Node b3Validation = document.getElementsByTagName("B3Validation").item(0);
		if (b3Validation != null) {
			Element cover = resultDocument
					.createElement("cover");
			cover.setTextContent(b3Validation.getTextContent());
			detailsNew.appendChild(cover);
		}
		
		// End2EndTxnRef Tag
		Node B3End2EndTxnRef = document.getElementsByTagName(
				"B3End2EndTxnRef").item(0);
		if (B3End2EndTxnRef != null) {
			Element B3End2EndTxnRefNew = resultDocument
					.createElement("end2EndTxnRef");
			B3End2EndTxnRefNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(B3End2EndTxnRef.getTextContent()));
			detailsNew.appendChild(B3End2EndTxnRefNew);
		}
	
        
        //ServiceTypeId Tag
		Node B3ServiceTypeId = document.getElementsByTagName(
				"B3ServiceTypeId").item(0);
		if (B3ServiceTypeId != null) {
			Element B3ServiceTypeIdNew = resultDocument
					.createElement("serviceTypeId");
			B3ServiceTypeIdNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(B3ServiceTypeId.getTextContent()));
			detailsNew.appendChild(B3ServiceTypeIdNew);
		}


		return resultDocument;
	}

}
