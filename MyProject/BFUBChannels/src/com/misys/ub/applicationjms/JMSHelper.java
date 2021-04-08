/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: JMSHelper.java,v.1.0,Dec 10, 2008 3:33:37 PM zubin
 *
 */
package com.misys.ub.applicationjms;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.fircosoft.InterfaceConstants;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * @author zubin
 * @date Dec 10, 2008
 * @project Universal Banking
 * @Description:
 */

public class JMSHelper {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final transient Log logger = LogFactory.getLog(JMSHelper.class.getName());
    private BankFusionEnvironment environment;
    private ConnectionFactory connectionFactory = null;
    private Session session = null;
    private static boolean connectionFlag = true;
    private Context jndiContext = null;
    Destination requestQueue = null;
    Destination replyQueue = null;
    Destination invalidQueue = null;
    InputStream jndiProp = BankFusionResourceSupport.getResourceLoader().getInputStreamResource("conf/messaging/jndi.properties");
    Properties props = new Properties();
    private static final String BF_RESPONSE_JNDI = "queue.BFResponse";
    private static final String BF_REQUEST_JNDI = "queue.BFRequest";
    private static final String BF_INVALIDQ_JNDI = "queue.BFInvalidQ";

    public JMSHelper(BankFusionEnvironment environment) {
        this.environment = environment;
        try {
            initialise();
        }
        catch (NamingException namingException) {
            /*
             * throw new BankFusionException(7328, namingException .getLocalizedMessage());
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT,
                    new Object[] { namingException.getLocalizedMessage() }, new HashMap(), getEnvironment());
        }
        catch (IOException ioException) {
            connectionFlag = false;
            /*
             * throw new BankFusionException(7328, ioException .getLocalizedMessage());
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT,
                    new Object[] { ioException.getLocalizedMessage() }, new HashMap(), getEnvironment());

        }
        catch (JMSException jmsException) {
            connectionFlag = false;
            /*
             * throw new BankFusionException(7328, jmsException .getLocalizedMessage());
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT,
                    new Object[] { jmsException.getLocalizedMessage() }, new HashMap(), getEnvironment());

        }
        catch (ClassNotFoundException classnotfoundExp) {
            connectionFlag = false;
            /*
             * throw new BankFusionException(7328, classnotfoundExp .getLocalizedMessage());
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT,
                    new Object[] { classnotfoundExp.getLocalizedMessage() }, new HashMap(), getEnvironment());

        }
        catch (Exception e) {
            /*
             * throw new BankFusionException(7328, e .getLocalizedMessage());
             */
            connectionFlag = false;
            EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT,
                    new Object[] { e.getLocalizedMessage() }, new HashMap(), getEnvironment());

        }
    }

    private void initialise() throws NamingException, IOException, JMSException, ClassNotFoundException {

        Connection connection = null;
        try {
            connection = getConnection();

        }
        catch (Exception exception) {
            logger.info(exception.getLocalizedMessage());
            try {
                connection = getConnectionIBM();
                connection.start();
            }
            catch (Exception e) {
                throw new JMSException("JMS Exception");
            }

        }

    }

    /*
     * Method: getConnection returns: generic connection for any JMS destination. Currently not
     * working
     */
    private Connection getConnection() throws IOException, NamingException, JMSException {
        InputStream jndiProp = BankFusionResourceSupport.getResourceLoader()
                .getInputStreamResource("conf/messaging/jndi.properties");
        Properties props = new Properties();
        props.load(jndiProp);
        jndiContext = new InitialContext(props);
        connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
        Connection connection = connectionFactory.createConnection();

        requestQueue = (Destination) jndiContext.lookup(props.getProperty(BF_REQUEST_JNDI));
        replyQueue = (Destination) jndiContext.lookup(props.getProperty(BF_RESPONSE_JNDI));
        invalidQueue = (Destination) jndiContext.lookup(props.getProperty(BF_INVALIDQ_JNDI));
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        return connection;
    }

    /*
     * Mehtod Name: getConnection() returns: Connection specific to IBM websphere
     */
    private Connection getConnectionIBM() throws NamingException, IOException, JMSException, ClassNotFoundException {

        props.load(jndiProp);

        String mqserver = props.getProperty(InterfaceConstants.FIRCOSOFTSERVER_IP);
        String port = props.getProperty(InterfaceConstants.FIRCOSOFTSERVER_PORT);
        String queuemgr = props.getProperty(InterfaceConstants.FIRCOSOFTSERVER_QUEUEMGR);
        String connectionFactoryName = props.getProperty(InterfaceConstants.JMS_CONNECTION_FACTORY);
        Class.forName(connectionFactoryName);
        QueueConnectionFactory factory = new MQQueueConnectionFactory();
        ((MQQueueConnectionFactory) factory).setQueueManager(queuemgr);

        ((MQQueueConnectionFactory) factory).setHostName(mqserver);
        ((MQQueueConnectionFactory) factory).setPort(Integer.parseInt(port));
        ((MQQueueConnectionFactory) factory).setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);

        Connection connection = factory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        requestQueue = (Destination) session.createQueue(props.getProperty(InterfaceConstants.UB_TO_FIRCOSOFTQ));
        ((MQQueue) requestQueue).setTargetClient(JMSC.MQJMS_CLIENT_NONJMS_MQ);// Setting
        // the
        // Message
        // to
        // NON
        // JMS
        // so
        // that
        // the
        // message
        // that
        // we
        // put
        // on
        // MQ
        // does
        // not
        // have
        // the
        // header
        replyQueue = (Destination) session.createQueue(props.getProperty(InterfaceConstants.FIRCOSOFT_TO_UBQ));
        ((MQQueue) replyQueue).setTargetClient(JMSC.MQJMS_CLIENT_NONJMS_MQ);
        invalidQueue = (Destination) session.createQueue(props.getProperty(InterfaceConstants.INVALIDQ));
        ((MQQueue) invalidQueue).setTargetClient(JMSC.MQJMS_CLIENT_NONJMS_MQ);

        return connection;
    }

    /*
     * Method Name: sendRequest parameter: (String) Message that needs to be sent to the destination
     * , (String) Unique corelation ID to identify the message returnd MessageID
     */
    public String sendRequest(String outGoingMessage, String correlationID) throws BankFusionException {
        if (connectionFlag) {
            try {
                MessageProducer requestProducer = session.createProducer(requestQueue);
                TextMessage message = session.createTextMessage();
                message.setText(outGoingMessage);
                message.setJMSReplyTo(replyQueue);

                requestProducer.send(message);
                if (logger.isInfoEnabled()) {
                    logger.info("sent " + outGoingMessage);
                    logger.info("outgoing message id " + message.getJMSMessageID());
                    logger.info("outgoing byte message id " + message.getJMSMessageID().getBytes());
                }
                return message.getJMSMessageID();
            }
            catch (JMSException jmsException) {
                /*
                 * throw new BankFusionException(7328, jmsException .getLocalizedMessage());
                 */
                EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT,
                        new Object[] { jmsException.getLocalizedMessage() }, new HashMap(), getEnvironment());
                return null;
            }
        }
        else {
            return null;
        }
    }

    /*
    * 
    */
    public String sendInvalid(String outGoingMessage, String correlationID) throws JMSException {
        MessageProducer invalidProducer = session.createProducer(invalidQueue);
        TextMessage message = session.createTextMessage();
        message.setText(outGoingMessage);
        message.setJMSReplyTo(invalidQueue);
        message.setJMSCorrelationID(correlationID);
        invalidProducer.send(message);
        if (logger.isInfoEnabled()) {
            logger.info("invalid " + outGoingMessage);
        }
        return message.getJMSMessageID();
    }

    public String sendInvalid(Message message) throws JMSException {
        MessageProducer invalidProducer = session.createProducer(invalidQueue);
        invalidProducer.send(message);
        if (logger.isInfoEnabled()) {
            logger.info("invalid " + message.toString());
        }
        return message.getJMSMessageID();
    }

    public String receiveResponse(String selector) throws BankFusionException {
        if (connectionFlag) {
            try {
                MessageConsumer replyConsumer = session.createConsumer(replyQueue, selector);

                Message msg = replyConsumer.receive(Integer.parseInt(props.getProperty(InterfaceConstants.TIMEINTERVAL)));

                if (msg == null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Null message detected");
                    }
                    return CommonConstants.EMPTY_STRING;
                }
                else if (msg instanceof TextMessage) {
                    TextMessage replyMessage = (TextMessage) msg;
                    logger.debug(msg.getJMSType());
                    if (logger.isInfoEnabled()) {
                        logger.info("\tContents:   " + replyMessage.getText());
                    }
                    return replyMessage.getText();
                }
                else if (msg instanceof BytesMessage) {
                    byte[] resMsg = new byte[10000];
                    BytesMessage replyMessage = (BytesMessage) msg;

                    logger.debug(msg.getJMSType());
                    if (logger.isInfoEnabled()) {
                        logger.info("\tContents:   " + replyMessage.readBytes(resMsg));
                    }
                    String respStr = new String(resMsg);
                    return respStr;

                }
                else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Invalid message detected");
                    }
                    sendInvalid(msg);
                    return null;
                }
            }
            catch (JMSException jmsException) {
                /*
                 * throw new BankFusionException(7328, jmsException .getLocalizedMessage());
                 */
                EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT, new Object[] {},
                        new HashMap(), getEnvironment());
                return null;
            }
        }
        else {
            return null;
        }
    }

    public void addListener(MessageListener messageListener) throws JMSException {
        MessageConsumer requestConsumer = session.createConsumer(requestQueue);
        requestConsumer.setMessageListener(messageListener);
    }

    /*
     * Method Name: consumeMessage input Parameter: (String) selector throws: JMSException
     * 
     * @description: THis Method is used to consume all the response message that Fircosoft will
     * generate
     */
    public void consumeDummyMessage(String selector) throws BankFusionException {
        if (connectionFlag) {
            try {
                MessageConsumer replyConsumer = session.createConsumer(replyQueue, selector);
                if (replyConsumer != null) {
                    Message msg = replyConsumer.receive(50);
                    replyConsumer = session.createConsumer(replyQueue, selector);
                }
            }
            catch (JMSException jmsException) {
                /*
                 * throw new BankFusionException(7328, jmsException .getLocalizedMessage());
                 */
                EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT, new Object[] {},
                        new HashMap(), getEnvironment());
            }
        }
    }

    public BankFusionEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(BankFusionEnvironment environment) {
        this.environment = environment;
    }

}
