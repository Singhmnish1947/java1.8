/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UBJMSService.java,v.1.0,Jul 8, 2009 4:32:08 PM harishrao
 *
 */
package com.trapedza.bankfusion.messaging.jms.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.messaging.config.ITransportProfile;
import com.trapedza.bankfusion.messaging.config.JMSProfile;
import com.trapedza.bankfusion.messaging.config.MessagingConfig;
import com.trapedza.bankfusion.messaging.gateway.interfaces.IInvocationMode;
import com.trapedza.bankfusion.messaging.listeners.JMSListener;

/**
 * @author harishrao
 * @date Jul 8, 2009
 * @project Universal Banking
 * @Description: A Service that starts, stops and manages listeners for messages on JMS as defined
 *               in transportProfile.xml
 */
public class UBJMSService extends JMSService implements DynamicMBean, JMSServiceMBean {

    /**
     * <code>cvsRevision</code> = $Revision: 1.1 $
     */
    public static final String cvsRevision = "$Revision: 1.1 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(cvsRevision);
    }
    private static final  Log logger = LogFactory.getLog(UBJMSService.class.getName());

    private static final  Log auditLogger = LogFactory.getLog("BankFusion.audit.events.services");

    private final String dClassName = this.getClass().getName();
    private final String dDescription = "UB JMS Service permitting selective transport reset";

    /** <code>JMS_SERVICE</code> "UBJMSService" */
    public static final String JMS_SERVICE = "UBJMSService";

    private MBeanAttributeInfo[] dAttributes;
    private final MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
    private MBeanInfo mbInfo;
    private JMSProfile[] profiles;
    private boolean[] profilesStatus;

    public UBJMSService() {

        MessagingConfig.loadConfig(IInvocationMode.SERVER_START);
        buildDynamicMBeanInfo();
    }

    /**
     * @see com.trapedza.bankfusion.services.AbstractService#getServiceName()
     */
    protected String getServiceName() {
        return UBJMSService.JMS_SERVICE;
    }

    private void buildDynamicMBeanInfo() {
        profiles = getJMSProfileList();
        dAttributes = new MBeanAttributeInfo[profiles.length];
        profilesStatus = new boolean[profiles.length];

        for (int item = 0; item < profiles.length; item++) {
            dAttributes[item] = new MBeanAttributeInfo(((JMSProfile) profiles[item]).getName(), "java.lang.Boolean", profiles[item]
                    .toString(), true, true, false);
            profilesStatus[item] = true;
        }
        // use reflection to get constructor signatures
        dConstructors[0] = new MBeanConstructorInfo("UBJMSService(): No-parameter constructor", // description
                this.getClass().getConstructors()[0]); // the constructor
        // object
        mbInfo = new MBeanInfo(dClassName, dDescription, dAttributes, dConstructors, null, new MBeanNotificationInfo[0]);
    }

    /**
     * @see com.trapedza.bankfusion.services.jms.JMSServiceMBean#getJMSListeners()
     */
    public String[] getJMSListeners() {
        return super.getJMSListeners();
    }

    private JMSProfile[] getJMSProfileList() {
        List<JMSProfile> transportList = new ArrayList<JMSProfile>();

        Map<String, ITransportProfile> profileMap = MessagingConfig.getTransportList().getProfileMap();
        Iterator<ITransportProfile> it = profileMap.values().iterator();
        ITransportProfile profile = null;
        while (it.hasNext()) {
            profile = it.next();
            if (profile instanceof JMSProfile && ((JMSProfile) profile).getListen().booleanValue()) {// We want only listeners
            	 transportList.add((JMSProfile) profile); 
            }
                  }
        JMSProfile[] jmsProfiles = new JMSProfile[transportList.size()];
        for (int item = 0; item < transportList.size(); item++) {
            jmsProfiles[item] = transportList.get(item);
        }
        return jmsProfiles;
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        for (int item = 0; item < profiles.length; item++) {
            if (attribute.equalsIgnoreCase(profiles[item].getName())) {
                return profilesStatus[item];
            }
        }
        throw new AttributeNotFoundException();
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList attrs = new AttributeList();
        for (int item = 0; item < attributes.length; item++) {
            Object attr = null;
            try {
                attr = getAttribute(attributes[item]);
            }
            catch (AttributeNotFoundException |MBeanException |ReflectionException e) { 
            	logger.error(ExceptionUtil.getExceptionAsString(e));            
            	}
            

            if (attr != null) {
                attrs.add(new Attribute(attributes[item], attr));
            }
        }
        return attrs;
    }

    public MBeanInfo getMBeanInfo() {
        return mbInfo;
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        String attributeName = attribute.getName();
        for (int item = 0; item < profiles.length; item++) {
            if (profiles[item].getName().equals(attributeName)) {
                boolean priorValue = profilesStatus[item];
                profilesStatus[item] = (Boolean) attribute.getValue();
                if (priorValue && !profilesStatus[item]) {// We're switching off
                    if (logger.isDebugEnabled()) {
                        logger.debug("Stopping " + profiles[item].getName() + " current " + profilesStatus[item]);
                    }
                    profiles[item].closedown();

                    profilesStatus[item] = false;
                    auditLogger.info("Stopped JMS Listener transport profile " + profiles[item].getName());
                }
                else if (!priorValue && profilesStatus[item]) {// We're switching on
                    if (logger.isDebugEnabled()) {
                        logger.debug("Starting " + profiles[item].getName() + " current " + profilesStatus[item]);
                    }
                    Iterator<ITransportProfile> it = MessagingConfig.getTransportList().getProfileMap().values().iterator();
                    while (it.hasNext()) {
                        ITransportProfile profile = it.next();
                        if (profile instanceof JMSProfile) {
                            if (profiles[item].getName().equals(profile.getName())) {
                                JMSListener.createNewListener((JMSProfile) profile);
                                ((JMSProfile) profile).getJMSProvider().startConnection();
                                profilesStatus[item] = true;
                                auditLogger.info("Started JMS Listener transport profile " + profiles[item].getName());
                            }
                        }
                    }
                }
                return;
            }
        }
    }

    public AttributeList setAttributes(final AttributeList attributes) {
        AttributeList resultList = new AttributeList();
        if (attributes.isEmpty()) {
            return resultList;
        }
        // try to set each attribute and add to result list if successful
        for (Iterator<Object> i = attributes.iterator(); i.hasNext();) {
            Attribute attr = (Attribute) i.next();
            try {
                setAttribute(attr);
                String name = attr.getName();
                resultList.add(new Attribute(name, getAttribute(name)));
            }
            catch (Exception e) {
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }
        }
        return resultList;
    }
}
