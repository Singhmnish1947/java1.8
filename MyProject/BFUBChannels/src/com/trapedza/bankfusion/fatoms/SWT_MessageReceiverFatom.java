/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.SWT_Constants;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MessageReceiverFatom;
import com.trapedza.bankfusion.steps.refimpl.ISWT_MessageReceiverFatom;

/**
 * This fatom is used to receive message from Queue and update the MessageStatus in SWTDisposal
 * table.
 * 
 * @author nileshk
 *
 */
public class SWT_MessageReceiverFatom extends AbstractSWT_MessageReceiverFatom implements ISWT_MessageReceiverFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     * Logger and constants defined.
     */
    private static final String DISPOSAL_REFERENCE_TAG = "DisposalReference";
    private static final String SWIFT_MESSAGE_TAG = "SwiftMessage";
    private static final String STATUS_TAG = "Status";
    private final static String SWIFT_PROPERTY_FILE_NAME = "SWIFT.properties";
    private transient final static Log logger = LogFactory.getLog(SWT_MessageReceiverFatom.class.getName());
    Properties swiftProperties = new Properties();

    private final static String RECEIVED_MESSAGE = "Received Message";
    private final static String NUM_ZERO = "0";
    private final static String MESSAGE_STATUS_UPDATED_SUCCESSFULLY = "Message status updated successfully";
    private final static String NO_MESSAGE_EXIST_OR_ERROR_WHILE_READING_QUEUE = "No message exist or error while reading queue";
    private final static String BF_CONFIG_LOCATION = "BFconfigLocation";
    private final static String CONF = "conf/swift/";
    private final static String NOT_FOUND_FILE_TRYING_RESOURCE = " not found as file, trying as resource";
    private final static String ERROR_WHILE_LOADING_FILE = "Error while loading the file : ";
    private final static String RECEIVING_MSG_FROM_QUEUE = "Receiving message from queue";
    private final static String MSG_RECEIVED_SUCCESSFULLY = "Message received successfully";
    private final static String ERROR_WHILE_READING_QUEUE_MSG = "Error while reading queue message";
    private final static String ERROR_WHILE_RECEIVING_MSG_FROM_QUEUE = "Error while receiving message from queue";
    private final static String ERROR_WHILE_PARSING_RECEIVED_MSG_FROM_QUEUE = "Error while parsing received message from queue";
    private final static String ERROR_READING_RECEIVED_MSG_FROM_QUEUE_AS_INPUT = "Error while reading received message from queue as Input Source";
    private final static String DOC_BUILDER_FACTORY_ERROR = "Document builder factory error ";

    /**
     * @param env
     */
    public SWT_MessageReceiverFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MessageReceiverFatom#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     * @param env
     *            The BankFusion Environment @
     */
    public void process(BankFusionEnvironment env) {

        HashMap receivedMap = receiveMessageFromQueue();
        if (receivedMap != null) {
            logger.info(RECEIVED_MESSAGE + receivedMap.toString());
            String dealNo = (String) receivedMap.get(DISPOSAL_REFERENCE_TAG);
            String status = (String) receivedMap.get(STATUS_TAG);
            int messageStatus = 0;
            final String whereByBOID = "WHERE " + IBOSWTDisposal.SWTDISPOSALID + " = ? ";

            final String fetchDisposal = "SELECT T1." + IBOSWTDisposal.SWTDISPOSALID + " AS " + IBOSWTDisposal.SWTDISPOSALID
                    + ", T1." + IBOSWTDisposal.PAYMENTFLAG + " AS " + IBOSWTDisposal.PAYMENTFLAG + ", T1."
                    + IBOSWTDisposal.CANCELFLAG + " AS " + IBOSWTDisposal.CANCELFLAG + ", T1." + IBOSWTDisposal.CONFIRMATIONFLAG
                    + " AS " + IBOSWTDisposal.CONFIRMATIONFLAG + ", T1." + IBOSWTDisposal.RECEIPTFLAG + " AS "
                    + IBOSWTDisposal.RECEIPTFLAG + ", T1." + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " AS "
                    + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " FROM " + IBOSWTDisposal.BONAME + " T1 WHERE T1."
                    + IBOSWTDisposal.SWTDISPOSALID + " NOT IN (SELECT T2." + IBOSWTDisposal.PREVSWTDISPOSALID + " AS "
                    + IBOSWTDisposal.PREVSWTDISPOSALID + " FROM " + IBOSWTDisposal.BONAME + " T2 WHERE T2." + IBOSWTDisposal.DEALNO
                    + "= ?) and T1." + IBOSWTDisposal.DEALNO + " = ?";

            ArrayList disposalParams = new ArrayList();
            disposalParams.add(dealNo);
            disposalParams.add(dealNo);

            ArrayList disposalQueryList = new ArrayList();
            disposalQueryList = (ArrayList) env.getFactory().executeGenericQuery(fetchDisposal, disposalParams, null);
            int paymentFlag = 0;
            int confirmFlag = 0;
            String swtDisposalId = null;
            int cancelFlag = 0;
            int receiptFlag = 0;
            int crdrConfirmationFlag = 0;
            if (disposalQueryList.size() > 0) {
                SimplePersistentObject disposalSimpleObject = (SimplePersistentObject) disposalQueryList.get(0);
                swtDisposalId = (String) disposalSimpleObject.getDataMap().get(IBOSWTDisposal.SWTDISPOSALID);
                paymentFlag = ((Integer) disposalSimpleObject.getDataMap().get(IBOSWTDisposal.PAYMENTFLAG)).intValue();
                confirmFlag = ((Integer) disposalSimpleObject.getDataMap().get(IBOSWTDisposal.CONFIRMATIONFLAG)).intValue();
                cancelFlag = ((Integer) disposalSimpleObject.getDataMap().get(IBOSWTDisposal.CANCELFLAG)).intValue();
                receiptFlag = ((Integer) disposalSimpleObject.getDataMap().get(IBOSWTDisposal.RECEIPTFLAG)).intValue();
                crdrConfirmationFlag = ((Integer) disposalSimpleObject.getDataMap().get(IBOSWTDisposal.CRDRCONFIRMATIONFLAG))
                        .intValue();
            }
            if (status.equals(NUM_ZERO)) {
                if (paymentFlag == 0 || confirmFlag == 0 || cancelFlag == 0 || receiptFlag == 0 || crdrConfirmationFlag == 0) {
                    if (paymentFlag == 1 || confirmFlag == 1 || cancelFlag == 1 || receiptFlag == 1 || crdrConfirmationFlag == 1) {
                        messageStatus = 1;
                    }
                }
                else messageStatus = 2;
            }

            else messageStatus = 3;

            ArrayList primaryKeyValueList = new ArrayList();
            primaryKeyValueList.add(swtDisposalId);

            ArrayList columnList = new ArrayList();
            columnList.add(IBOSWTDisposal.MESSAGESTATUS);

            ArrayList valueList = new ArrayList();
            valueList.add(new Integer(messageStatus));

            int updateResult = env.getFactory().bulkUpdate(IBOSWTDisposal.BONAME, whereByBOID, primaryKeyValueList, columnList,
                    valueList);
            if (updateResult > 0)
                if (logger.isInfoEnabled()) {
                    logger.info(MESSAGE_STATUS_UPDATED_SUCCESSFULLY);
                }
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info(NO_MESSAGE_EXIST_OR_ERROR_WHILE_READING_QUEUE);
            }
        }

    }

    /**
     * This method will recieve message from queue
     * 
     * @return
     */
    private HashMap receiveMessageFromQueue() {
        String configLocation = null;
        InputStream is = null;
        try {
            // configLocation = System.getProperty(BF_CONFIG_LOCATION,
            // CommonConstants.EMPTY_STRING);
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    CONF + SWT_MessageReceiverFatom.SWIFT_PROPERTY_FILE_NAME, configLocation, BankFusionThreadLocal.getUserZone());
            swiftProperties.load(is);
        }
        catch (Exception ex) {
            if (is == null) {
                if (logger.isDebugEnabled())
                    logger.debug(configLocation + CONF + SWT_MessageReceiverFatom.SWIFT_PROPERTY_FILE_NAME
                            + NOT_FOUND_FILE_TRYING_RESOURCE);
                is = this.getClass().getClassLoader().getResourceAsStream(CONF + SWT_MessageReceiverFatom.SWIFT_PROPERTY_FILE_NAME);
                try {
                    swiftProperties.load(is);
                }
                catch (IOException e) {
                    logger.equals(ERROR_WHILE_LOADING_FILE + CONF + SWIFT_PROPERTY_FILE_NAME);
                }
            }
        }
        HashMap messageMap = null;
        Queue queue = null;
        QueueSession session = null;
        QueueConnection connection = null;
        try {
            if (logger.isInfoEnabled()) {
                logger.info(RECEIVING_MSG_FROM_QUEUE);
            }
            String mqserver = swiftProperties.getProperty(SWT_Constants.MQSERVER_IP);
            String port = swiftProperties.getProperty(SWT_Constants.MQSERVER_PORT);
            String channel = swiftProperties.getProperty(SWT_Constants.MQSERVER_CHANNEL);
            String queuemgr = swiftProperties.getProperty(SWT_Constants.MQSERVER_QUEUEMGR);
            String queueName = swiftProperties.getProperty(SWT_Constants.MQSERVER_QUEUENAME);
            String connectionFactory = swiftProperties.getProperty(SWT_Constants.JMS_CONNECTION_FACTORY);
            // load connection factory
            Class.forName(connectionFactory);
            QueueConnectionFactory factory = new MQQueueConnectionFactory();
            ((MQQueueConnectionFactory) factory).setTransportType(1); // TCPIP
            ((MQQueueConnectionFactory) factory).setHostName(mqserver);
            connection = factory.createQueueConnection();
            ((MQQueueConnectionFactory) factory).setQueueManager(queuemgr);
            ((MQQueueConnectionFactory) factory).setChannel(channel);
            ((MQQueueConnectionFactory) factory).setPort(Integer.parseInt(port));

            connection.start();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue(queueName);
            QueueReceiver queueReceiver = (QueueReceiver) session.createReceiver(queue);
            TextMessage receivedMessage = (TextMessage) queueReceiver.receiveNoWait();

            if (receivedMessage != null) {
                messageMap = convertMessageToHashMap(receivedMessage.getText());
                if (logger.isInfoEnabled()) {
                    logger.info(MSG_RECEIVED_SUCCESSFULLY);
                }
            }
            else {
                logger.error(ERROR_WHILE_READING_QUEUE_MSG);
            }
        }
        catch (Exception e1) {
            logger.error(ERROR_WHILE_RECEIVING_MSG_FROM_QUEUE);
        }
        return messageMap;
    }

    /**
     * This method will convert the message to HashMap
     * 
     * @param receivedMessage
     * @return
     */
    private HashMap convertMessageToHashMap(String receivedMessage) {
        HashMap messageMap = new HashMap();
        try {
            // loading and parsing the xml response
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(receivedMessage)));

            NodeList disposalRef = document.getElementsByTagName(DISPOSAL_REFERENCE_TAG);
            Node disposalRefNode = disposalRef.item(0);
            String disposalRefValue = disposalRefNode.getFirstChild().getNodeValue();

            NodeList swiftMessageNodeList = document.getElementsByTagName(SWIFT_MESSAGE_TAG);
            Node swiftMessageNode = swiftMessageNodeList.item(0);
            String swiftMessageValue = swiftMessageNode.getFirstChild().getNodeValue();

            NodeList statusNodeList = document.getElementsByTagName(STATUS_TAG);
            Node statusNode = statusNodeList.item(0);
            String statusValue = statusNode.getFirstChild().getNodeValue();

            messageMap.put(DISPOSAL_REFERENCE_TAG, disposalRefValue);
            messageMap.put(SWIFT_MESSAGE_TAG, swiftMessageValue);
            messageMap.put(STATUS_TAG, statusValue);
            logger.debug(messageMap.toString());

        }
        catch (SAXException e) {
            logger.error(ERROR_WHILE_PARSING_RECEIVED_MSG_FROM_QUEUE);
        }
        catch (IOException e) {
            logger.error(ERROR_READING_RECEIVED_MSG_FROM_QUEUE_AS_INPUT);
        }
        catch (ParserConfigurationException e) {
            logger.error(DOC_BUILDER_FACTORY_ERROR);
        }
        return messageMap;
    }

}
