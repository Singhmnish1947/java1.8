package com.misys.ub.swift;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;

/**
 * 
 * @author Binit
 * 
 *         file to route incoming swift message to multiple queues
 * 
 * 
 */

public class FromUBMMM_To_Essence {
	private transient final static Log LOGGER = LogFactory
			.getLog(FromUBMMM_To_Essence.class.getName());

	public static void executeFiles(Exchange exchange) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException, SAXException, IOException, ParserConfigurationException {
		Message params = exchange.getIn();
		String requestMsg = params.getBody().toString();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(requestMsg);
		}
		if (requestMsg.contains("<MeridianMessageType>SWIFT_MT103</MeridianMessageType>")) {

			MessageProducerUtil.sendMessage(
					MT103_SWIFT_UB.MT103_SWIFT_Transform(requestMsg),
					"UBSWIFT_Incoming_103");
		} else if (requestMsg.contains("<MeridianMessageType>SWIFT_MT200</MeridianMessageType>")) {
			MessageProducerUtil.sendMessage(MT200_SWIFT_UB.MT200_Transform(requestMsg), "UBSWIFT_Incoming_200");
		} else if (requestMsg.contains("<MeridianMessageType>SWIFT_MT201</MeridianMessageType>")) {
			MessageProducerUtil.sendMessage(MT201_SWIFT_UB.MT201_SWIFT_Transform(requestMsg), "UBSWIFT_Incoming_201");
		} else if (requestMsg.contains("<MeridianMessageType>SWIFT_MT202</MeridianMessageType>")) {
			MessageProducerUtil.sendMessage(MT202_SWIFT_UB.MT202_Transform(requestMsg),
					 "UBSWIFT_Incoming_202");
		} else if (requestMsg.contains("<MeridianMessageType>SWIFT_MT203</MeridianMessageType>")) {
			MessageProducerUtil.sendMessage(MT203_SWIFT_UB.MT203_Transform(requestMsg), "UBSWIFT_Incoming_203");
		} else if (requestMsg.contains("<MeridianMessageType>SWIFT_MT205</MeridianMessageType>")) {
			MessageProducerUtil.sendMessage(MT205_SWIFT_UB.MT205_Transform(requestMsg), "UBSWIFT_Incoming_205");
		}
	}

}
