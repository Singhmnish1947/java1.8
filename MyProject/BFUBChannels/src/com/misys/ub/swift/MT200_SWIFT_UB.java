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

public class MT200_SWIFT_UB {
              private transient final static Log LOGGER = LogFactory
                                           .getLog(MT200_SWIFT_UB.class.getName());

              private static String strResult;

              public static String MT200_Transform(String requestMsg)
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
                             Element respRootElement = respDoc.createElement("UB_MT200");

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

                             tag = doc.getElementsByTagName("TRN").item(0);
                             if (tag != null) {
                                           newTag = respDoc.createElement("transactionReferenceNumber");
                                           newTag.setTextContent(tag.getTextContent());
                                           detailsTag.appendChild(newTag);
                             }

                             // Intermediary
                             temp_option = "";
                             tag = doc.getElementsByTagName("IntermediaryA").item(0);
                             if (tag == null) {
                                           tag = doc.getElementsByTagName("IntermediaryD").item(0);
                                           if (tag == null) {
                                                          temp_option = "";
                                           } else {
                                                          temp_option = "D";
                                           }
                             } else {
                                           temp_option = "A";
                             }

                             if (temp_option != "") {
                                           tag = doc.getElementsByTagName("Intermediary" + temp_option)
                                                                        .item(0);
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

                             // SendersCorrespondent
                             temp_option = "";
                             tag = doc.getElementsByTagName("SendersCorrespondentB").item(0);
                             if (tag == null) {
                                           temp_option = "";
                             } else {
                                           temp_option = "B";
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

                                                          newTag = respDoc.createElement("sendersCorresOption");
                                                          newTag.setTextContent(temp_option);
                                                          detailsTag.appendChild(newTag);
                                           }
                             }

                             tag = doc.getElementsByTagName("SenderToReceiverInfo").item(0);
                             if (tag != null) {
                                           newTag = respDoc.createElement("senderToReceiverInformation");
                                           newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
                                                                        .getTextContent()));
                                           detailsTag.appendChild(newTag);
                             }

                             tag = doc.getElementsByTagName("ValueDateCcyAmount").item(0);
                             if (tag != null&&tag.getTextContent() != "") {
                                           
                                                          String source_ValueDateCcyAmount = tag.getTextContent();
                                                          String date_MT200 = source_ValueDateCcyAmount.substring(0, 6);
                                                          String currency_MT200 = source_ValueDateCcyAmount.substring(6,
                                                                                      9);
                                                          String amount_MT200 = source_ValueDateCcyAmount.substring(9,
                                                                                      source_ValueDateCcyAmount.length());
                                                          tag = respDoc.createElement("tdvalueDate");
                                                          tag.setTextContent(SWT_Outgoing_Globals
                                                                                      .formatDateForUB(date_MT200));
                                                          detailsTag.appendChild(tag);
                                                          tag = respDoc.createElement("tdcurrencyCode");
                                                          tag.setTextContent(currency_MT200);
                                                          detailsTag.appendChild(tag);
                                                          tag = respDoc.createElement("tdamount");
                                                          tag.setTextContent(amount_MT200.replaceAll(",", "."));
                                                          detailsTag.appendChild(tag);
                                           
                             }
                             // AccountWithInstitution
                             temp_option = "";
                             tag = doc.getElementsByTagName("AccountWithInstA").item(0);
                             if (tag == null) {
                                           tag = doc.getElementsByTagName("AccountWithInstB").item(0);
                                           if (tag == null) {
                                                          tag = doc.getElementsByTagName("AccountWithInstD").item(0);
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
                                           tag = doc.getElementsByTagName("AccountWithInst" + temp_option)
                                                                        .item(0);
                                           if (tag.getTextContent() != "") {
                                                          newTag = respDoc.createElement("accountWithInstitution");
                                                         newTag.setTextContent(SWT_Outgoing_Globals.AddUBDelimiter(tag
                                                                                      .getTextContent()));
                                                          detailsTag.appendChild(newTag);

                                                          newTag = respDoc.createElement("accountWithInstOption");
                                                          newTag.setTextContent(temp_option);
                                                          detailsTag.appendChild(newTag);
                                           }
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
                             return respDoc;
              }
}
