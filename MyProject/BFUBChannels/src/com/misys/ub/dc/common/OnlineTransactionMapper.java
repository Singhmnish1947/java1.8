/*
 * **********************************************************
 * Copyright (c) 2005,2008 Finastra Software Solutions. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions.
 * Use is subject to license terms.
 *
 *************************************************************************
 * Modification History
 * ************************************************************************
 */
package com.misys.ub.dc.common;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;

public class OnlineTransactionMapper {
	private transient final static Log logger = LogFactory.getLog(OnlineTransactionMapper.class.getName());
	/**
	 * @param msg
	 */
	public void mapTransactionData(String msg) throws Exception {
		JsonParser parser = new JsonParser();
		JsonObject data = (JsonObject) parser.parse(msg);
		if (logger.isDebugEnabled()) {
		logger.debug("data is  "+data);
		}
		if (!data.isJsonNull()) {
			if(data.get("f_TYPE").getAsCharacter()!='V')
			{
			String partnerName = data.get("UBCUSTOMERNAME") != null ? data.get("UBCUSTOMERNAME").getAsString() : "";
			String dcBulkPmtReference = data.get("DCBULKPMTREFERENCE") != null
					? data.get("DCBULKPMTREFERENCE").getAsString()
					: "";
			String transactionReference = data.get("f_REFERENCE") != null ? data.get("f_REFERENCE").getAsString() : "";
			String channelID = data.get("f_UBCHANNELID") != null ? data.get("f_UBCHANNELID").getAsString() : "";

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			doc.setStrictErrorChecking(false);
			// setting namespace
			Element pushTransactionsRequestRoot = doc.createElementNS("http://pegasus/integration/transaction",
					"PushTransactionsRequest");
			pushTransactionsRequestRoot.setPrefix("tran");
			Element transactionListTAG = doc.createElement("TransactionList");
			Element transactionTAG = doc.createElement("Transaction");
			if (dcBulkPmtReference != null && !dcBulkPmtReference.isEmpty()) {
				Element txnRefIdTAG = doc.createElement("TxnReferenceID");
				txnRefIdTAG.setTextContent(dcBulkPmtReference);
				transactionTAG.appendChild(txnRefIdTAG);
			} else if ("IBI".equals(channelID) || "MOB".equals(channelID)) {
				String txnIds[] = transactionReference.split(":");
				if (txnIds[0] != null && Pattern.matches("[0-9]*", txnIds[0])) {
					Element txnRefIdTAG = doc.createElement("TxnReferenceID");
					txnRefIdTAG.setTextContent(txnIds[0]);
					transactionTAG.appendChild(txnRefIdTAG);
				}
			}
			// TransactionId
			Element txnIdTAG = doc.createElement("TransactionId");
			txnIdTAG.setTextContent(data.get("boID").getAsString());
			transactionTAG.appendChild(txnIdTAG);

			// AccountId
			Element acctIdTAG = doc.createElement("AccountId");
			acctIdTAG.setTextContent(data.get("f_ACCOUNTPRODUCT_ACCPRODID").getAsString());
			transactionTAG.appendChild(acctIdTAG);

			// CardId
			if (data.get("UBCARDNUMBER") != null) {
				Element cardIdTAG = doc.createElement("CardId");
				cardIdTAG.setTextContent(data.get("UBCARDNUMBER").getAsString());
				transactionTAG.appendChild(cardIdTAG);
			}

			// Amount
			Element amtTAG = doc.createElement("Amount");
			amtTAG.setTextContent(Double.valueOf(data.get("f_AMOUNT").toString()).toString());
			transactionTAG.appendChild(amtTAG);

			// AmountInLocalCurrency

			if ((data.get("f_BASEEQUIVALENT")!=null) && (!data.get("f_BASEEQUIVALENT").isJsonNull())) {
				Element amtinLocalCurrTAG = doc.createElement("AmountInLocalCurrency");
				amtinLocalCurrTAG.setTextContent(Double.valueOf(data.get("f_BASEEQUIVALENT").toString()).toString());
				transactionTAG.appendChild(amtinLocalCurrTAG);
			}

			// AmountInOriginalCurrency
			if ((data.get("f_ORIGINALAMOUNT")!=null) && (!data.get("f_ORIGINALAMOUNT").isJsonNull())) {
				Element amtInOrgCurrTAG = doc.createElement("AmountInOriginalCurrency");
				if ((data.get("f_DEBITCREDITFLAG").toString().equals("D"))) {

					amtInOrgCurrTAG
							.setTextContent(Double.valueOf("-" + data.get("f_ORIGINALAMOUNT").toString()).toString());
					transactionTAG.appendChild(amtInOrgCurrTAG);
				} else {
					amtInOrgCurrTAG.setTextContent(Double.valueOf(data.get("f_ORIGINALAMOUNT").toString()).toString());
					transactionTAG.appendChild(amtInOrgCurrTAG);
				}
			}

			// OriginalCurrency
			if ((data.get("f_ISOCURRENCYCODE")!=null) && (!data.get("f_ISOCURRENCYCODE").isJsonNull())) {
				Element orgCurrTAG = doc.createElement("OriginalCurrency");
				orgCurrTAG.setTextContent(data.get("f_ISOCURRENCYCODE").getAsString());
				transactionTAG.appendChild(orgCurrTAG);
			}

			// PartnerName
			if (!partnerName.isEmpty()) {
				Element partnerNameTAG = doc.createElement("PartnerName");
				partnerNameTAG.setTextContent(partnerName);
				transactionTAG.appendChild(partnerNameTAG);
			}

			// PartnerAcc
			if ((data.get("UBCONTRAACCNUM")!=null) && (!data.get("UBCONTRAACCNUM").isJsonNull())) {
				Element partnerAccTAG = doc.createElement("PartnerAccount");
				partnerAccTAG.setTextContent(data.get("UBCONTRAACCNUM").getAsString());
				transactionTAG.appendChild(partnerAccTAG);
			}

			// Description
			Element descriptionTAG = doc.createElement("Description");
			descriptionTAG.setTextContent(data.get("f_NARRATION").getAsString());
			transactionTAG.appendChild(descriptionTAG);
			SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			DateFormat inFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
			// TransactionDate
			Element txnDateTAG = doc.createElement("TransactionDate");
			// System.out.println(sdf.parse(data.get("f_TRANSACTIONDATE").toString()));
			Date inDate = inFormat.parse(data.get("f_TRANSACTIONDATE").getAsString());
			String outDate = outFormat.format(inDate);
			
			txnDateTAG.setTextContent(outDate);
			transactionTAG.appendChild(txnDateTAG);

			// BookingDate

			inDate = inFormat.parse(data.get("f_POSTINGDATE").getAsString());
			outDate = outFormat.format(inDate);

			Element bookingDateTAG = doc.createElement("BookingDate");
			bookingDateTAG.setTextContent(outDate);
			transactionTAG.appendChild(bookingDateTAG);

			// TransactionType
			Element txnTypeTAG = doc.createElement("TransactionType");
			txnTypeTAG.setTextContent(data.get("f_CODE").getAsString());
			transactionTAG.appendChild(txnTypeTAG);

			// MerchantCode
			if ((data.get("UBMERCHANTNAME")!=null) && (!data.get("UBMERCHANTNAME").isJsonNull())) {
				Element merchantCodeTAG = doc.createElement("MerchantCode");
				merchantCodeTAG.setTextContent(data.get("UBMERCHANTNAME").getAsString());
				transactionTAG.appendChild(merchantCodeTAG);
			}

			// CreditDebitIndicator
			Element creditDebitIndTAG = doc.createElement("CreditDebitIndicator");
			if ("D".equals(data.get("f_DEBITCREDITFLAG").getAsString())) {
				creditDebitIndTAG.setTextContent("DEBIT");
			} else {
				creditDebitIndTAG.setTextContent("CREDIT");
			}

			transactionTAG.appendChild(creditDebitIndTAG);

			// mandateId
			if (data.get("MPDDTMRIMNDTID")!=null) {
				Element mandateIdTAG = doc.createElement("MandateId");
				mandateIdTAG.setTextContent(data.get("MPDDTMRIMNDTID").getAsString());
				transactionTAG.appendChild(mandateIdTAG);
			}

			// TransactionStatus

			Element txnStatusTAG = doc.createElement("TransactionStatus");
			txnStatusTAG.setTextContent("BOOKED");
			transactionTAG.appendChild(txnStatusTAG);

			// ReversalStatus
			if ((data.get("f_REVERSALINDICATOR")!=null) && (!data.get("f_REVERSALINDICATOR").isJsonNull())) {
				Element revStatusTAG = doc.createElement("ReversalStatus");
				revStatusTAG.setTextContent(data.get("f_REVERSALINDICATOR").getAsString());
				transactionTAG.appendChild(revStatusTAG);
			}

			// DirectDebitReference
			if (data.get("MPPMTIDENDTOENDID")!=null) {
				Element directDebitReferenceTAG = doc.createElement("DirectDebitReference");
				directDebitReferenceTAG.setTextContent(data.get("MPPMTIDENDTOENDID").getAsString());
				transactionTAG.appendChild(directDebitReferenceTAG);
			}

			// CreditorID
			if (data.get("MPDDTMRIAIDORGCDSCHMID")!=null) {
				Element creditorIdTAG = doc.createElement("CreditorID");
				creditorIdTAG.setTextContent(data.get("MPDDTMRIAIDORGCDSCHMID").getAsString());
				transactionTAG.appendChild(creditorIdTAG);
			}

			transactionListTAG.appendChild(transactionTAG);

			pushTransactionsRequestRoot.appendChild(transactionListTAG);
			doc.appendChild(pushTransactionsRequestRoot);

			String XMLMsg = toString(doc);
			if (logger.isDebugEnabled()) {
				logger.debug("Txn Push msg sent to DC::" + XMLMsg);
			}
			postToQueue(XMLMsg, "RECIEVEQUEUE");
			}
		}
		logger.info("OnlineTransactionMapper::END");
		}

	
	/**
	 * @param doc
	 */
	public String toString(Document doc) throws Exception {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}
	/**
	 * @param message
	 * @param queueEndpoint
	 */
	private void postToQueue(String message, String queueEndpoint) {
	    MessageProducerUtil.sendMessage(message, queueEndpoint);
}}
