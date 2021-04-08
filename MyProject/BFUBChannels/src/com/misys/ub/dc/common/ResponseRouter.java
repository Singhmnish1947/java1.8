package com.misys.ub.dc.common;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.cc.utils.CCFinancialResponsePrep;

public class ResponseRouter {

	private transient final static Log LOGGER = LogFactory.getLog(ResponseRouter.class);

	public void routeResponse(Exchange exchange) {
		String incMsg = (String) exchange.getIn().getBody();
		String appID = getTagValue("origCtxtId", incMsg);
		if (appID == null) {
			JsonParser parser = new JsonParser();
			JsonObject jmsg = new JsonObject();
			try {
				jmsg = parser.parse(incMsg).getAsJsonObject();
				appID = jmsg.get("origCtxtId").getAsString();
			} catch (JsonParseException e) {
				e.printStackTrace();
			}

		}
		if (appID != null && !appID.isEmpty()) {
			if (appID.equalsIgnoreCase("MOB"))
				appID = "IBI";
			if (appID.equalsIgnoreCase("CCI") && getTagValue("reqPayload", incMsg) !=null && getTagValue("reqPayload", incMsg).equals("REQUEST_PAYLOAD")) {
				CCFinancialResponsePrep responsePrep = new CCFinancialResponsePrep();
				String channelRef = getTagValue("transactionalItem", incMsg);
				incMsg = responsePrep.prepareResponse(incMsg, channelRef);

			}
			postToQueue(incMsg, "QM_" + appID + "_UB_Response");
		}
	}

	private void postToQueue(String message, String queueEndpoint) {
		LOGGER.info("\n\n--------Response sent by Essence --------\n\n" + message);
		LOGGER.info("---- Posting the message in the following queue => " + queueEndpoint);
		MessageProducerUtil.sendMessage(message, queueEndpoint);
	}

	public static String getTagValue(String tagName, String xmlString) {
		XMLEventReader xmlEventReader = null;
		try {
			byte[] byteArray = xmlString.getBytes("UTF-8");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			xmlEventReader = inputFactory.createXMLEventReader(inputStream);

			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals(tagName) && xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						if (xmlEvent.isCharacters())
							return xmlEvent.asCharacters().getData().trim();
					}
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			try {
				if (xmlEventReader != null)
					xmlEventReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
