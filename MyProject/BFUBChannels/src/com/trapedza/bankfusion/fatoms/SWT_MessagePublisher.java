/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;

import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.UB_SWT_TransformUBtoMeridian;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MessagePublisher;

/**
 * @author Girish
 *
 */
public class SWT_MessagePublisher extends AbstractSWT_MessagePublisher {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(SWT_MessagePublisher.class.getName());

    private static final String ENDPOINT_OUT = "TO_UBMMM_OUTGOING";

    UB_SWT_TransformUBtoMeridian transformUBToMeridian = new UB_SWT_TransformUBtoMeridian();

    public void process(BankFusionEnvironment env) {
        // TODO Auto-generated method stub
        super.process(env);
        if (this.getF_IN_MessageMap().size() > 0) {
            ArrayList messageList = this.getF_IN_MessageMap();
            Object messageObject = messageList.get(0);
            String message = generateXML(messageObject);

            if (getF_IN_ispublish().equalsIgnoreCase("Y")) {
                publishToQueue(message);
                logger.info("MESSAGE GENERATED AND PUBLISHED" + message);
            }
            else {
                logger.info("MESSAGE GENERATED AND NOT PUBLISHED" + message);
            }
            setF_OUT_PublishStatusCode("0");
            return;

        }

        if (this.getF_IN_MessageMap().size() < 0) {
            this.setF_OUT_PublishStatusCode("-1");
            return;
        }
        generateSWTMessage(this.getF_IN_MessageMap());

    }

    public void publishToQueue(String message) {
        try {
            String meridianMessage = transformUBToMeridian.executeFiles(message);
            logger.info("Meridian Message: " + meridianMessage);
            MessageProducerUtil.sendMessage(meridianMessage, ENDPOINT_OUT);
        }
        catch (Exception e1) {
            logger.error("Error: ", e1);
        }
    }

    /**
     * @param env
     */
    public String generateXML(Object messageObject) {
        StringWriter xmlWriter = new StringWriter();
        String configLocation = null;
        try {
            // Properties props =
            // LocalConfiguration.getInstance().getProperties();
            // props.setProperty("org.exolab.castor.indent", "false");

            XMLContext xmlContext = new XMLContext();
            xmlContext.setProperty("org.exolab.castor.indent", "false");

            Marshaller marshaller = new Marshaller(xmlWriter);

            Mapping mapping = new Mapping(getClass().getClassLoader());
            // configLocation = System.getProperty("BFconfigLocation",
            // CommonConstants.EMPTY_STRING);
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            // is = new FileInputStream(configLocation +
            // "SwiftMessageMapping.xml");
            mapping.loadMapping(configLocation + "conf/swift/" + "SwiftMessageMapping.xml");

            marshaller.setMapping(mapping);
            marshaller.marshal(messageObject);
            logger.info("XML Generated ->" + xmlWriter.toString());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return xmlWriter.toString();

    }

    public SWT_MessagePublisher(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    private void generateSWTMessage(ArrayList list) {
        // TODO Auto-generated method stub
        StringBuffer sbf = new StringBuffer();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            HashMap listMap = (HashMap) iter.next();
            listMap.put("Action", CommonConstants.EMPTY_STRING);
            listMap.put("Branch", CommonConstants.EMPTY_STRING);
            listMap.put("VerificationRequired", CommonConstants.EMPTY_STRING);
            listMap.put("MultipleHold", CommonConstants.EMPTY_STRING);

            HashMap headerList = new HashMap();
            headerList.put(SWT_Constants.MESSAGEHEADER_PREPEND + listMap.get(SWT_Constants.MESSAGETYPE), listMap);
            generateMessageFromMap(headerList, sbf);
            publishMessageToQueue(sbf.toString());

        }
        logger.info("generated XML::" + sbf.toString());
    }

    private void generateMessageFromMap(HashMap map, StringBuffer sbf) {
        Set keySet = map.keySet();
        for (Iterator iterMap = keySet.iterator(); iterMap.hasNext();) {
            String key = (String) iterMap.next();
            sbf.append(SWT_Constants.XML_PREPEND_START);
            sbf.append(key);
            sbf.append(SWT_Constants.XML_APPPEND_START);
            Object value = map.get(key);
            if (value instanceof HashMap) {
                generateMessageFromMap((HashMap) value, sbf);

            }
            else {
                if (value instanceof String) {
                    sbf.append((String) value);
                }
                else if (value instanceof Boolean) {
                    sbf.append((Boolean) value + CommonConstants.EMPTY_STRING);
                }
                else if (value instanceof BigDecimal) {
                    sbf.append(((BigDecimal) value).toString());
                }
                else {
                    sbf.append(value.toString());
                }

            }
            sbf.append(SWT_Constants.XML_PREPEND_END);
            sbf.append(key);
            sbf.append(SWT_Constants.XML_APPPEND_END);
        }

    }

    private void publishMessageToQueue(String message) {
        try {
            String meridianMessage = transformUBToMeridian.executeFiles(message);
            logger.info("Meridian Message: " + meridianMessage);
            MessageProducerUtil.sendMessage(meridianMessage, ENDPOINT_OUT);
            this.setF_OUT_PublishStatusCode("0");
        }
        catch (Exception e1) {
            e1.printStackTrace();
            this.setF_OUT_PublishStatusCode("-1");
        }
    }

}
