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
 *         file to transform MT203
 */

public class MT203_SWIFT_UB {
	private static String strResult;
	private transient final static Log LOGGER = LogFactory
			.getLog(MT203_SWIFT_UB.class.getName());

	public static String MT203_Transform(String requestMsg) {
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
		} catch (SAXException | ParserConfigurationException | IOException
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

		// tag : messageId
		Node messageID = document.getElementsByTagName("MessageID").item(0);
		Element messageIDNew = resultDocument.createElement("messageId1");
		messageIDNew.setTextContent(Double.toString(Double
				.parseDouble(messageID.getTextContent())));
		headerNew.appendChild(messageIDNew);

		// tag : messageType

		Element externalMessageTypeNew = resultDocument
				.createElement("messageType");
		externalMessageTypeNew.setTextContent(externalMessageType
				.getTextContent());
		headerNew.appendChild(externalMessageTypeNew);

		// tag : details
		Element detailsNew = resultDocument.createElement("details");
		detailsNew.setAttribute(PaymentSwiftConstants.XMLNS,
				PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
		resultRootElement.appendChild(detailsNew);

		// tag : sumOfAmounts
		Node sumOfAmounts = document.getElementsByTagName("SumOfAmounts").item(
				0);
		if (sumOfAmounts != null) {
			Element sumOfAmountsNew = resultDocument
					.createElement("sumOfAmounts");
			sumOfAmountsNew.setTextContent(sumOfAmounts.getTextContent()
					.replaceAll(",", "."));
			detailsNew.appendChild(sumOfAmountsNew);
		}

		// tag : valueDate
		Node valueDate2 = document.getElementsByTagName("ValueDate2").item(0);
		if (valueDate2 != null) {
			Element valueDateNew = resultDocument.createElement("valueDate");
			valueDateNew.setTextContent(SWT_Outgoing_Globals
					.formatDateForUB(valueDate2.getTextContent()));
			detailsNew.appendChild(valueDateNew);
		}
		// tag : sender
		Node senderAddress = document.getElementsByTagName("SenderAddress")
				.item(0);
		if (senderAddress != null) {
			Element senderNew = resultDocument.createElement("sender");
			senderNew.setTextContent(SWT_Outgoing_Globals
					.convertBIC(senderAddress.getTextContent()));
			detailsNew.appendChild(senderNew);
		}

		// tag : receiver
		Node receiverAddress = document.getElementsByTagName(
				"DestinationAddress").item(0);
		if (receiverAddress != null) {
			Element receiverNew = resultDocument.createElement("receiver");
			receiverNew.setTextContent(SWT_Outgoing_Globals
					.convertBIC(receiverAddress.getTextContent()));
			detailsNew.appendChild(receiverNew);
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
					.createElement("orderingInstituteOption");
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
					.createElement("orderingInstituteOption");
			orderingInstituteOptionNew.setTextContent("D");
			detailsNew.appendChild(orderingInstituteOptionNew);
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
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT_OPTION);
			sendersCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendersCorrespondentAList.item(0)
							.getTextContent()));
			detailsNew.appendChild(sendersCorrespondentNew);

			Element sendersCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
			sendersCorrespondentOptionNew.setTextContent("A");
			detailsNew.appendChild(sendersCorrespondentOptionNew);
		} else if (sendersCorrespondentBList.getLength() > 0) {
			Element sendersCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT_OPTION);
			sendersCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendersCorrespondentBList.item(0)
							.getTextContent()));
			detailsNew.appendChild(sendersCorrespondentNew);

			Element sendersCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
			sendersCorrespondentOptionNew.setTextContent("B");
			detailsNew.appendChild(sendersCorrespondentOptionNew);
		} else if (sendersCorrespondentDList.getLength() > 0) {
			Element sendersCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT_OPTION);
			sendersCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendersCorrespondentDList.item(0)
							.getTextContent()));
			detailsNew.appendChild(sendersCorrespondentNew);

			Element sendersCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.SENDERS_CORRESPONDENT);
			sendersCorrespondentOptionNew.setTextContent("D");
			detailsNew.appendChild(sendersCorrespondentOptionNew);
		}

		// tag : receiversCorrespondent and receiversCorrespondentOption
		NodeList receiversCorrespondentAList = document
				.getElementsByTagName("ReceiversCorrespondentA");
		NodeList receiversCorrespondentBList = document
				.getElementsByTagName("ReceiversCorrespondentB");
		NodeList receiversCorrespondentDList = document
				.getElementsByTagName("ReceiversCorrespondentD");

		if (receiversCorrespondentAList.getLength() > 0) {
			Element receiversCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT);
			receiversCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(receiversCorrespondentAList.item(0)
							.getTextContent()));
			detailsNew.appendChild(receiversCorrespondentNew);

			Element receiversCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT_OPTION);
			receiversCorrespondentOptionNew.setTextContent("A");
			detailsNew.appendChild(receiversCorrespondentOptionNew);
		} else if (receiversCorrespondentBList.getLength() > 0) {
			Element receiversCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT);
			receiversCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(receiversCorrespondentBList.item(0)
							.getTextContent()));
			detailsNew.appendChild(receiversCorrespondentNew);

			Element receiversCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT_OPTION);
			receiversCorrespondentOptionNew.setTextContent("B");
			detailsNew.appendChild(receiversCorrespondentNew);
		} else if (receiversCorrespondentDList.getLength() > 0) {
			Element receiversCorrespondentNew = resultDocument
					.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT);
			receiversCorrespondentNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(receiversCorrespondentDList.item(0)
							.getTextContent()));
			detailsNew.appendChild(receiversCorrespondentNew);

			Element receiversCorrespondentOptionNew = resultDocument
					.createElement(PaymentSwiftConstants.RECEIVERS_CORRESPONDENT_OPTION);
			receiversCorrespondentOptionNew.setTextContent("D");
			detailsNew.appendChild(receiversCorrespondentOptionNew);
		}

		// tag : sendertoReceiverInformation
		Node sendertoReceiverInfo = document.getElementsByTagName(
				"SendertoReceiverInfo").item(0);
		Element sendertoReceiverInformationNew = null;
		if (sendertoReceiverInfo != null) {
			sendertoReceiverInformationNew = resultDocument
					.createElement("sendertoReceiverInformation");
			sendertoReceiverInformationNew.setTextContent(SWT_Outgoing_Globals
					.AddUBDelimiter(sendertoReceiverInfo.getTextContent()));
			detailsNew.appendChild(sendertoReceiverInformationNew);
		}

		// tag : mt203Detail
		NodeList messageDetailsList = document
				.getElementsByTagName("MessageDetails");
		for (int i = 0; i < messageDetailsList.getLength(); ++i) {
			Element messageDetails = (Element) messageDetailsList.item(i);
			Element mt203DetailNew = resultDocument
					.createElement("mt203Detail");
			mt203DetailNew.setAttribute(PaymentSwiftConstants.XMLNS,
					PaymentSwiftConstants.ROOT_UB_TYPES_INTERFACES);
			// tags : currencyCode and amount
			Node ccyAmount = messageDetails.getElementsByTagName("CcyAmount")
					.item(0);
			if (ccyAmount != null) {
				Element currencyCodeNew = resultDocument
						.createElement("currencyCode");
				Element amountNew = resultDocument.createElement("amount");
				String currency = ccyAmount.getTextContent().substring(0, 3);
				String amount = ccyAmount.getTextContent().substring(3);
				currencyCodeNew.setTextContent(currency);
				amountNew.setTextContent(amount.replaceAll(",", "."));
				mt203DetailNew.appendChild(amountNew);
				mt203DetailNew.appendChild(currencyCodeNew);
			}

			// tag : accountWithInstitution and accountWithInstitutionOption
			NodeList accountWithInstitutionAList = messageDetails
					.getElementsByTagName("AccountWithInstitutionA");
			NodeList accountWithInstitutionBList = messageDetails
					.getElementsByTagName("AccountWithInstitutionB");
			NodeList accountWithInstitutionDList = messageDetails
					.getElementsByTagName("AccountWithInstitutionD");

			if (accountWithInstitutionAList.getLength() > 0) {
				Element accountWithInstitutionNew = resultDocument
						.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
				accountWithInstitutionNew.setTextContent(SWT_Outgoing_Globals
						.AddUBDelimiter(accountWithInstitutionAList.item(0)
								.getTextContent()));
				mt203DetailNew.appendChild(accountWithInstitutionNew);

				Element accountWithInstitutionOptionNew = resultDocument
						.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION_OPTION);
				accountWithInstitutionOptionNew.setTextContent("A");
				mt203DetailNew.appendChild(accountWithInstitutionOptionNew);
			} else if (accountWithInstitutionBList.getLength() > 0) {
				Element accountWithInstitutionNew = resultDocument
						.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
				accountWithInstitutionNew.setTextContent(SWT_Outgoing_Globals
						.AddUBDelimiter(accountWithInstitutionBList.item(0)
								.getTextContent()));
				mt203DetailNew.appendChild(accountWithInstitutionNew);

				Element accountWithInstitutionOptionNew = resultDocument
						.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION_OPTION);
				accountWithInstitutionOptionNew.setTextContent("B");
				mt203DetailNew.appendChild(accountWithInstitutionOptionNew);
			} else if (accountWithInstitutionDList.getLength() > 0) {
				Element accountWithInstitutionNew = resultDocument
						.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION);
				accountWithInstitutionNew.setTextContent(SWT_Outgoing_Globals
						.AddUBDelimiter(accountWithInstitutionDList.item(0)
								.getTextContent()));
				mt203DetailNew.appendChild(accountWithInstitutionNew);

				Element accountWithInstitutionOptionNew = resultDocument
						.createElement(PaymentSwiftConstants.ACCOUNT_WITH_INSTITUTION_OPTION);
				accountWithInstitutionOptionNew.setTextContent("D");
				mt203DetailNew.appendChild(accountWithInstitutionOptionNew);
			}

			// tag : beneficiary and beneficiaryOption
			NodeList beneficiaryAList = messageDetails
					.getElementsByTagName("BeneficiaryA");
			NodeList beneficiaryDList = messageDetails
					.getElementsByTagName("BeneficiaryD");

			if (beneficiaryAList.getLength() > 0) {
				Element beneficiaryNew = resultDocument
						.createElement("beneficiary");
				beneficiaryNew.setTextContent(SWT_Outgoing_Globals
						.AddUBDelimiter(beneficiaryAList.item(0)
								.getTextContent()));
				mt203DetailNew.appendChild(beneficiaryNew);

				Element beneficiaryOptionNew = resultDocument
						.createElement("beneficiaryOption");
				beneficiaryOptionNew.setTextContent("A");
				mt203DetailNew.appendChild(beneficiaryOptionNew);
			} else if (beneficiaryDList.getLength() > 0) {
				Element beneficiaryNew = resultDocument
						.createElement("beneficiary");
				beneficiaryNew.setTextContent(SWT_Outgoing_Globals
						.AddUBDelimiter(beneficiaryDList.item(0)
								.getTextContent()));
				mt203DetailNew.appendChild(beneficiaryNew);

				Element beneficiaryOptionNew = resultDocument
						.createElement("beneficiaryOption");
				beneficiaryOptionNew.setTextContent("D");
				mt203DetailNew.appendChild(beneficiaryOptionNew);
			}

			// tag : sendertoReceiverInformation
			sendertoReceiverInfo = messageDetails.getElementsByTagName(
					"SendertoReceiverInfo").item(0);
			if (sendertoReceiverInfo != null) {
				sendertoReceiverInformationNew = resultDocument
						.createElement("sendertoReceiverInformation");
				sendertoReceiverInformationNew
						.setTextContent(SWT_Outgoing_Globals
								.AddUBDelimiter(sendertoReceiverInfo
										.getTextContent()));
				mt203DetailNew.appendChild(sendertoReceiverInformationNew);
			}

			// tag : transactionReferenceNumber
			Node tRN = messageDetails.getElementsByTagName("TRN").item(0);
			if (tRN != null) {
				Element transactionReferenceNumberNew = resultDocument
						.createElement("transactionReferenceNumber");
				transactionReferenceNumberNew.setTextContent(tRN
						.getTextContent());
				mt203DetailNew.appendChild(transactionReferenceNumberNew);
			}

			// tag : relatedReference
			Node relatedReference = messageDetails.getElementsByTagName(
					"RelatedReference").item(0);
			if (relatedReference != null) {
				Element relatedReferenceNew = resultDocument
						.createElement("relatedReference");
				relatedReferenceNew.setTextContent(relatedReference
						.getTextContent());
				mt203DetailNew.appendChild(relatedReferenceNew);
			}

			// tag : action
			Node cancellationAction = messageDetails.getElementsByTagName(
					"CancellationAction").item(0);
			if (cancellationAction != null) {
				Element actionNew = resultDocument.createElement("action");
				actionNew.setTextContent(cancellationAction.getTextContent());
				mt203DetailNew.appendChild(actionNew);
			}

			detailsNew.appendChild(mt203DetailNew);
		}

		return resultDocument;
	}

}
