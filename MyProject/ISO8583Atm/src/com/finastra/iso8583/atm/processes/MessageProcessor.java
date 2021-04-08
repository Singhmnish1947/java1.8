/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.processes;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.messaging.runtime.impl.MessagingAuditHelper;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;

/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public class MessageProcessor {
	private static Log logger = LogFactory.getLog(MessageProcessor.class.getName());
	static final String line = "==================";
	static final String logger_ERROR_DESCRIPTION = "  ErrorDescription : ";
	static final String logger_ERROR_CODE = "   ErrorCode : ";

	String incMsg;
	ATMISOEssenceMapping atmISOEssenceMapping = new ATMISOEssenceMapping();

	public void executeFiles(Exchange exchange) {

		org.apache.camel.Message request = exchange.getIn();
		incMsg = request.getBody().toString();
		if (logger.isInfoEnabled()) {
			logger.info(request);
		}
		ParsingEngine pe = new ParsingEngine();
		Message message = new Message();

		String correlationID = BankFusionThreadLocal.getCorrelationID();
		String userName = BankFusionThreadLocal.getUserId();
		MessagingAuditHelper.audit((Object) null, incMsg, correlationID, "IN", "ATMTCP", userName);

		try {
			message = new ParsingEngine().parse(incMsg);
		} catch (Exception e) {
			logger.error(line + logger_ERROR_CODE + "40112468" + logger_ERROR_DESCRIPTION
					+ " Invalid request message format " + line);
			logger.error(incMsg);

			ATMTransactionUtil.handleEvent(40112468, new String[] {});
		}

		HashMap<String, Object> essenceRes = new HashMap<String, Object>();
		essenceRes = atmISOEssenceMapping.processAtmTransaction(message);
		message.setMessageFields(essenceRes);

		Message message1 = new Message(message.getMessageFields());
		message1.setMessageTypeIdentifier(message.getMessageTypeIdentifier());
		message1 = new ParsingEngine().prepareResponseMessage(message1);
		if (null == message1.getSecondaryBitMap()) {
			message1.setSecondaryBitMap("");
		}

		StringBuilder finalResponse = new StringBuilder();
		finalResponse.append(message1.getMessageHeader());
		finalResponse.append(message1.getPrimaryBitMap());
		finalResponse.append(message1.getSecondaryBitMap());
		finalResponse.append(message1.getMessageData());

		MessagingAuditHelper.audit((Object) null, finalResponse.toString(), correlationID, "OUT", "ATMTCP", userName);
		if (logger.isInfoEnabled()) {
			logger.info(finalResponse);
		}
		exchange.getIn().setBody(finalResponse.toString());
		if (logger.isInfoEnabled()) {
			logger.info(exchange.getIn().getBody());
		}

	}

}
