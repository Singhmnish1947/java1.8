package com.misys.ub.swift.tellerRemittance.notification;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.finastra.api.gppNotification.FndtMsgType;
import com.finastra.api.gppNotification.MsgType;
import com.finastra.api.gppNotification.ProcessingPersistentInfoType;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceMessageDao;

public class NotificationMsgConsumer {

	private transient final static Log LOGGER = LogFactory.getLog(NotificationMsgConsumer.class.getName());

	public static void consumeMsg(Exchange exchange) throws SAXException {
		LOGGER.info("NotificationMsgConsumer from TLR.TO.TLR.NOTIFY");

		Message params = exchange.getIn();
		String notificationMsg = params.getBody().toString();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("GPP Notification Msg ::::::: " + notificationMsg);
		}
	
		try {
			FndtMsgType model = convertXMLToObject(notificationMsg);
			if (null != model && null != model.getMsg()) {
				MsgType msgValue = model.getMsg();
				ProcessingPersistentInfoType value = msgValue.getExtn().getProcessingPersistentInfo();
				String remittanceStatus = StringUtils.isNotBlank(value.getPMSGSTS()) ? value.getPMSGSTS()
						: StringUtils.EMPTY;
				String senderReference = StringUtils.isNotBlank(value.getPINSTRID()) ? value.getPINSTRID()
						: StringUtils.EMPTY;
				String uetr = StringUtils.isNotBlank(value.getPUNIQUEE2EREF()) ? value.getPUNIQUEE2EREF()
						: StringUtils.EMPTY;
				String txId = StringUtils.isNotBlank(value.getPTXID()) ? value.getPTXID() : StringUtils.EMPTY;
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("senderReference ::::P_TX_ID:::  " + txId);
					LOGGER.info("senderReference ::::P_INSTR_ID::: " + senderReference);
					LOGGER.info("remittanceStatus ::::P_MSG_STS::: " + remittanceStatus);
				}

				BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				RemittanceMessageDao.updateRemittanceStatus(remittanceStatus, senderReference, uetr);
				BankFusionThreadLocal.getPersistanceFactory().commitTransaction();

			}
		} catch (Exception ex) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(ex));
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			throw ex;
		}

		LOGGER.info("END of NotificationMsgConsumer");
	}

	/**
	 * @param message
	 * @return
	 * @throws SAXException
	 */
	private static FndtMsgType convertXMLToObject(String message) throws SAXException {
		JAXBContext jaxbContext;
		StringReader stringReader = new StringReader(message);
		FndtMsgType notificationMessage = new FndtMsgType();

		try {
			jaxbContext = JAXBContext.newInstance(FndtMsgType.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Object object = jaxbUnmarshaller.unmarshal(stringReader);
			notificationMessage = (FndtMsgType) JAXBIntrospector.getValue(object);
		} catch (JAXBException e) {
			LOGGER.error("Error while converting xml to object::" + ExceptionUtil.getExceptionAsString(e));
			return null;
		}

		return notificationMessage;
	}
}
