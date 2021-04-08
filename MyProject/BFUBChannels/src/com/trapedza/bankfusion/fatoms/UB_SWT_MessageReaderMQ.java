/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_MT200;
import com.misys.ub.swift.UB_MT201;
import com.misys.ub.swift.UB_MT202;
import com.misys.ub.swift.UB_MT203;
import com.misys.ub.swift.UB_MT205;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.StringToDate;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MessageReaderMQ;

/**
 * @author Gaurav Aggarwal
 *
 */
public class UB_SWT_MessageReaderMQ extends AbstractUB_SWT_MessageReaderMQ {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private final static String SWIFT_PROPERTY_FILE_NAME = "SWIFT.properties";
    private final static String CONF = "conf/swift/";

    Properties swiftProperties = new Properties();

    private transient final static Log logger = LogFactory.getLog(UB_SWT_MessageReaderMQ.class.getName());

    public UB_SWT_MessageReaderMQ(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    public void process(BankFusionEnvironment env) {
        HashMap receivedMap = receiveMessageFromQueue(env);
        // TODO Auto-generated method stub
        super.process(env);
    }

    /**
     *
     * @return
     */
    private HashMap receiveMessageFromQueue(BankFusionEnvironment env) {
        String configLocation = null;
        InputStream is = null;
        try {
            /*
             * configLocation = System.getProperty("BFconfigLocation",
             * CommonConstants.EMPTY_STRING);
             */
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    CONF + UB_SWT_MessageReaderMQ.SWIFT_PROPERTY_FILE_NAME, configLocation, BankFusionThreadLocal.getUserZone());
            swiftProperties.load(is);
        }
        catch (Exception ex) {
            if (is == null) {
                if (logger.isDebugEnabled())
                    logger.debug(configLocation + CONF + UB_SWT_MessageReaderMQ.SWIFT_PROPERTY_FILE_NAME
                            + " not found as file, trying as resource");
                is = this.getClass().getClassLoader().getResourceAsStream(CONF + UB_SWT_MessageReaderMQ.SWIFT_PROPERTY_FILE_NAME);
                try {
                    swiftProperties.load(is);
                }
                catch (IOException e) {
                    logger.equals("Error while loading the file : " + CONF + SWIFT_PROPERTY_FILE_NAME);
                }
            }
        }
        HashMap messageMap = null;
        Queue incomingqueue = null;
        QueueSession incomingsession = null;
        QueueConnection connection = null;
        try {
            logger.info("Receiving message from queue");
            String mqserver = swiftProperties.getProperty(SWT_Constants.MQSERVER_IP);
            String port = swiftProperties.getProperty(SWT_Constants.MQSERVER_PORT);
            String channel = swiftProperties.getProperty(SWT_Constants.MQSERVER_CHANNEL);
            String queuemgr = swiftProperties.getProperty(SWT_Constants.MQSERVER_QUEUEMGR);
            String incomingqueueName = swiftProperties.getProperty(SWT_Constants.MQSERVER_INCOMINGQUEUENAME);
            String connectionFactory = swiftProperties.getProperty(SWT_Constants.JMS_CONNECTION_FACTORY);
            Class.forName(connectionFactory);
            QueueConnectionFactory factory = new MQQueueConnectionFactory();
            ((MQQueueConnectionFactory) factory).setTransportType(1); // TCPIP
            ((MQQueueConnectionFactory) factory).setHostName(mqserver);
            ((MQQueueConnectionFactory) factory).setQueueManager(queuemgr);
            ((MQQueueConnectionFactory) factory).setChannel(channel);
            ((MQQueueConnectionFactory) factory).setPort(Integer.parseInt(port));
            connection = factory.createQueueConnection("", "");
            connection.start();
            incomingsession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            incomingqueue = incomingsession.createQueue(incomingqueueName);

            QueueReceiver queueReceiver = (QueueReceiver) incomingsession.createReceiver(incomingqueue);
            TextMessage receivedMessage = (TextMessage) queueReceiver.receiveNoWait();
            Mapping mapping = new Mapping(getClass().getClassLoader());
            /*
             * configLocation = System.getProperty("BFconfigLocation",
             * CommonConstants.EMPTY_STRING);
             */
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            mapping.loadMapping(configLocation + CONF + "SwiftMessageMapping.xml");
            Unmarshaller unmarshaller = new Unmarshaller();
            unmarshaller.setMapping(mapping);
            String messgaetype = null;
            int j = 0;
            ArrayList list = new ArrayList();
            logger.info(receivedMessage.getText().toString());
            while (receivedMessage != null && j < 2 && receivedMessage.getText().length() > 0) {
                Object Objects = unmarshaller.unmarshal(new InputSource(new StringReader(receivedMessage.getText().toString())));

                if (Objects instanceof UB_MT103) {
                    UB_MT103 MessageObject = (UB_MT103) unmarshaller
                            .unmarshal(new InputSource(new StringReader(receivedMessage.getText().toString())));
                    logger.info(MessageObject.getMessageType());
                    HashMap hashmap = new HashMap();
                    hashmap.put("AccountWith", MessageObject.getAccountWithInstitution());
                    hashmap.put("AccountWithOption", MessageObject.getAccountWithInstOption());
                    hashmap.put("BeneficiaryCustomer", MessageObject.getBeneficiaryCustomer());
                    hashmap.put("BeneficiaryCustomerOption", MessageObject.getBeneficiaryCustOption());
                    hashmap.put("DealReference", MessageObject.getSendersReference());
                    hashmap.put("DetailsOfCharge", MessageObject.getDetailsOfCharges());
                    hashmap.put("TransactionReferenceNumber", MessageObject.getDisposalRef());
                    String InterBankSettledAmount = MessageObject.getTdAmount();
                    InterBankSettledAmount = InterBankSettledAmount.replaceAll(",", ".");
                    hashmap.put("InterBankSettledAmount", new BigDecimal(InterBankSettledAmount));
                    hashmap.put("InterBankSettledCurrency", MessageObject.getTdCurrencyCode());
                    hashmap.put("Intermediary", MessageObject.getIntermediaryInstitution());
                    hashmap.put("IntermediaryOption", MessageObject.getIntermediaryInstOption());
                    hashmap.put("MessageType", MessageObject.getMessageType());
                    String ReceiversCharges = MessageObject.getReceiversCharges();
                    if (ReceiversCharges != null && ReceiversCharges.length() > 0) {
                        ReceiversCharges = ReceiversCharges.substring(3);
                        ReceiversCharges = ReceiversCharges.replaceAll(",", ".");
                    }
                    else {
                        ReceiversCharges = "0.00";
                    }
                    hashmap.put("ReceiversCharges", new BigDecimal(ReceiversCharges));
                    hashmap.put("ReceiversCorrespondent", MessageObject.getReceiversCorrespondent());
                    hashmap.put("ReceiversCorrespondentOption", MessageObject.getReceiversCorrespOption());
                    hashmap.put("Sender", MessageObject.getSender());
                    hashmap.put("SendersCorrespondent", MessageObject.getSendersCorrespondent());

                    hashmap.put("SendersCorrespondentOption", MessageObject.getSendersCorrespOption());
                    hashmap.put("ThirdReimbursementInstitution", MessageObject.getThirdReimbursementInstitution());
                    hashmap.put("ThirdReimbursementInstitutionOption", MessageObject.getThirdReimbursementInstOption());
                    Date valueDate = StringToDate.run(MessageObject.getTdValueDate());
                    hashmap.put("ValueDate", valueDate);
                    list.add(Objects);
                    hashmap.put("Object", list);
                    HashMap hashmapout = new HashMap();
                    hashmapout = MFExecuter.executeMF("UB_SWT_Incoming_MT103_Process_SRV", env, hashmap);
                    logger.info("HashMapout" + hashmapout);

                }
                else if (Objects instanceof UB_MT203) {

                    HashMap hashmapout = new HashMap();
                    HashMap hashmap = new HashMap();
                    UB_MT203 MessageObject = (UB_MT203) unmarshaller
                            .unmarshal(new InputSource(new StringReader(receivedMessage.getText().toString())));
                    list.add(Objects);
                    hashmap.put("MessageObject", list);
                    hashmapout = MFExecuter.executeMF("UB_SWT_PopulateMT203_SRV", env, hashmap);
                    logger.info("HashMapout" + hashmapout);
                }
                else if (Objects instanceof UB_MT201) {

                    HashMap hashmapout = new HashMap();
                    HashMap hashmap = new HashMap();
                    UB_MT201 MessageObject = (UB_MT201) unmarshaller
                            .unmarshal(new InputSource(new StringReader(receivedMessage.getText().toString())));
                    list.add(Objects);

                    logger.info(MessageObject.getMessageType());
                    hashmap.put("MessageObject", list);
                    hashmapout = MFExecuter.executeMF("UB_SWT_PopulateMT201_SRV", env, hashmap);
                    logger.info("HashMapout" + hashmapout);
                }
                else if (Objects instanceof UB_MT205) {
                    UB_MT205 MessageObject = (UB_MT205) unmarshaller
                            .unmarshal(new InputSource(new StringReader(receivedMessage.getText().toString())));
                    HashMap hashmap = new HashMap();
                    String InterBankSettledAmount = MessageObject.getTdamount();
                    InterBankSettledAmount = InterBankSettledAmount.replaceAll(",", ".");
                    hashmap.put("AccountWith", MessageObject.getAccountWithInstitution());

                    hashmap.put("AccountWithOption", MessageObject.getAccountWithInstOption());
                    hashmap.put("BeneficiaryInstitution", MessageObject.getBeneficiaryInstitute());
                    hashmap.put("BeneficiaryInstitutionOption", MessageObject.getBeneficiaryInstOption());
                    hashmap.put("DealReference", MessageObject.getTransactionReferenceNumber());
                    hashmap.put("InterBankSettledAmount", new BigDecimal(InterBankSettledAmount));
                    hashmap.put("InterBankSettledCurrency", MessageObject.getTdcurrencyCode());
                    hashmap.put("Intermediary", MessageObject.getIntermediary());
                    hashmap.put("IntermediaryOption", MessageObject.getIntermediaryOption());
                    hashmap.put("MessageType", MessageObject.getMessageType());
                    hashmap.put("Sender", MessageObject.getSender());
                    hashmap.put("SendersCorrespondent", MessageObject.getSendersCorrespondent());
                    hashmap.put("SendersCorrespondentOption", MessageObject.getSendersCorresOption());
                    hashmap.put("relatedReference", MessageObject.getRelatedReference());
                    hashmap.put("sendertoReceiverInformation", MessageObject.getSenderToReceiverInformation());
                    Date valueDate = StringToDate.run(MessageObject.getTdvalueDate());
                    hashmap.put("ValueDate", valueDate);
                    hashmap.put("transactionReferenceNumber", MessageObject.getTransactionReferenceNumber());
                    list.add(Objects);
                    hashmap.put("Object", list);
                    HashMap hashmapout = new HashMap();
                    logger.info(hashmap);
                    hashmapout = MFExecuter.executeMF("UB_SWT_Incoming_MT205_Process_SRV", env, hashmap);
                    logger.info("HashMapout" + hashmapout);
                }
                else if (Objects instanceof UB_MT200) {
                    HashMap hashmapout = new HashMap();
                    HashMap hashmap = new HashMap();
                    UB_MT200 MessageObject = (UB_MT200) unmarshaller
                            .unmarshal(new InputSource(new StringReader(receivedMessage.getText().toString())));
                    String InterBankSettledAmount = MessageObject.getTdamount();
                    InterBankSettledAmount = InterBankSettledAmount.replaceAll(",", ".");
                    hashmap.put("AccountWith", MessageObject.getAccountWithInstitution());
                    hashmap.put("AccountWithOption", MessageObject.getAccountWithInstOption());
                    hashmap.put("DealReference", MessageObject.getTransactionReferenceNumber());
                    hashmap.put("InterBankSettledAmount", new BigDecimal(InterBankSettledAmount));
                    hashmap.put("InterBankSettledCurrency", MessageObject.getTdcurrencyCode());
                    hashmap.put("Intermediary", MessageObject.getIntermediary());
                    hashmap.put("IntermediaryOption", MessageObject.getIntermediaryOption());
                    hashmap.put("MessageType", MessageObject.getMessageType());
                    hashmap.put("Sender", MessageObject.getSender());
                    hashmap.put("SendersCorrespondent", MessageObject.getSendersCorrespondent());
                    hashmap.put("SendersCorrespondentOption", MessageObject.getSendersCorresOption());
                    hashmap.put("sendertoReceiverInformation", MessageObject.getSenderToReceiverInformation());
                    Date valueDate = StringToDate.run(MessageObject.getTdvalueDate());
                    hashmap.put("ValueDate", valueDate);
                    hashmap.put("transactionReferenceNumber", MessageObject.getTransactionReferenceNumber());
                    hashmap.put("relatedReference", MessageObject.getDisposalRef());
                    list.add(Objects);
                    hashmap.put("Object", list);
                    hashmapout = MFExecuter.executeMF("UB_SWT_Incoming_MT200_Process_SRV", env, hashmap);
                    logger.info("HashMapout" + hashmapout);
                }
                else if (Objects instanceof UB_MT202) {

                    UB_MT202 MessageObject = (UB_MT202) unmarshaller
                            .unmarshal(new InputSource(new StringReader(receivedMessage.getText().toString())));
                    HashMap hashmap = new HashMap();
                    String InterBankSettledAmount = MessageObject.getTdAmount();
                    InterBankSettledAmount = InterBankSettledAmount.replaceAll(",", ".");
                    hashmap.put("AccountWith", MessageObject.getAccountWithInstitution());
                    hashmap.put("AccountWithOption", MessageObject.getAccountWithInstitutionOption());
                    hashmap.put("BeneficiaryCustomer", MessageObject.getBeneficiary());
                    hashmap.put("BeneficiaryCustomerOption", MessageObject.getBeneficiaryOption());
                    hashmap.put("DealReference", MessageObject.getTransactionReferenceNumber());
                    hashmap.put("InterBankSettledAmount", new BigDecimal(InterBankSettledAmount));
                    hashmap.put("InterBankSettledCurrency", MessageObject.getTdCurrencyCode());
                    hashmap.put("Intermediary", MessageObject.getIntermediary());
                    hashmap.put("IntermediaryOption", MessageObject.getIntermediaryOption());
                    hashmap.put("MessageType", MessageObject.getMessageType());
                    hashmap.put("ReceiversCorrespondent", MessageObject.getReceiversCorrespondent());
                    hashmap.put("ReceiversCorrespondentOption", MessageObject.getReceiversCorrespondentOption());
                    hashmap.put("Sender", MessageObject.getSender());
                    hashmap.put("SendersCorrespondent", MessageObject.getSendersCorrespondent());
                    hashmap.put("SendersCorrespondentOption", MessageObject.getSendersCorrespondentOption());
                    hashmap.put("relatedReference", MessageObject.getRelatedReference());
                    hashmap.put("sendertoReceiverInformation", MessageObject.getSendertoReceiverInformation());
                    Date valueDate = StringToDate.run(MessageObject.getTdValueDate());
                    hashmap.put("ValueDate", valueDate);
                    hashmap.put("transactionReferenceNumber", MessageObject.getTransactionReferenceNumber());
                    list.add(Objects);
                    hashmap.put("Object", list);
                    HashMap hashmapout = new HashMap();
                    hashmapout = MFExecuter.executeMF("UB_SWT_Incoming_MT202_Process_SRV", env, hashmap);
                    logger.info("HashMapout" + hashmapout);

                }

                receivedMessage = (TextMessage) queueReceiver.receiveNoWait();
                j++;
            }

        }
        catch (Exception e1) {
            logger.error("Error while receiving message from queue", e1);
        }
        return messageMap;
    }

}
