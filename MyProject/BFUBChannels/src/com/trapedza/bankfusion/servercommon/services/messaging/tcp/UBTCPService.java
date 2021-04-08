/* ***********************************************************************************
 * Copyright (c) 2003,2009 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Trapedza Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: UBTCPService.java,v 1.1 18 Jun 2009 13:37:25 rnolan Exp $
 *
 * $Log: UBTCPService.java,v $
 */
package com.trapedza.bankfusion.servercommon.services.messaging.tcp;

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
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.TechnicalEventSupport;
import com.trapedza.bankfusion.messaging.config.ITransportProfile;
import com.trapedza.bankfusion.messaging.config.MessagingConfig;
import com.trapedza.bankfusion.messaging.config.TCPProfile;
import com.trapedza.bankfusion.messaging.gateway.interfaces.IInvocationMode;
import com.trapedza.bankfusion.messaging.tcp.ITCPService;
import com.trapedza.bankfusion.messaging.tcp.TCPMessagingManager;
import com.trapedza.bankfusion.services.ServiceStatus;
import com.trapedza.bankfusion.utils.CommonEventCodes;

public class UBTCPService extends TCPService implements DynamicMBean {

    /**
     * <code>cvsRevision</code> = $Revision: 1.1 $.
     */
    public static final String cvsRevision = "$Revision: 1.1 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(cvsRevision);
    }

    private  static final  Log logger = LogFactory.getLog(UBTCPService.class.getName());
    private static final transient Log auditLogger = LogFactory.getLog("BankFusion.audit.events.services");

    private final String dClassName = this.getClass().getName();
    private final String dDescription = "UB TCP Service permitting selective transport reset";

    // internal variables for describing MBean elements
    private MBeanAttributeInfo[] dAttributes;
    private final MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
    private MBeanInfo mbInfo;
    private TCPProfile[] profiles;
    private boolean[] profilesStatus;

    private MBeanOperationInfo[] opers = { new MBeanOperationInfo("start", "start", null, "void", MBeanOperationInfo.ACTION),
            new MBeanOperationInfo("stop", "stop", null, "void", MBeanOperationInfo.ACTION),
            new MBeanOperationInfo("restart", "restart", null, "void", MBeanOperationInfo.ACTION),
            new MBeanOperationInfo("pause", "pause", null, "void", MBeanOperationInfo.ACTION),
            new MBeanOperationInfo("resume", "resume", null, "void", MBeanOperationInfo.ACTION) };

    public UBTCPService() {

        MessagingConfig.loadConfig(IInvocationMode.SERVER_START);
        buildDynamicMBeanInfo();
    }

    public void start() {

        if (!status.equals(ServiceStatus.STOPPED)) {
            TechnicalEventSupport.getInstance().raiseTechnicalErrorEvent(CommonEventCodes.E_SERVICE_CANT_START,
                    new Object[] { getServiceName(), status.toString() }, auditLogger);
        }

        status = ServiceStatus.RUNNING;

        TCPMessagingManager tcpManager = new TCPMessagingManager(MessagingConfig.getTransportList());
        MessagingConfig.setTCPManager(tcpManager);
    }

    /**
     * Stops TCP messaging manager, which in turn stops all TCP services
     * 
     * @see com.trapedza.bankfusion.services.Service#stop()
     */
    public void stop() {

        if (!status.equals(ServiceStatus.RUNNING)) {
            TechnicalEventSupport.getInstance().raiseTechnicalErrorEvent(CommonEventCodes.E_SERVICE_CANT_STOP,
                    new Object[] { getServiceName(), status.toString() }, auditLogger);
        }

        status = ServiceStatus.STOPPED;

        TCPMessagingManager m = MessagingConfig.getTCPManager();
        if (m != null) {
            m.stop();
        }
    }

    public void restart() {
        stop();
        start();
    }

    /**
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attribute) {

        for (int item = 0; item < profiles.length; item++) {
            if (attribute.equalsIgnoreCase(profiles[item].getName())) {
                return profilesStatus[item];
            }
        }
        return null;
    }

    /**
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    public AttributeList getAttributes(String[] attributes) {

        AttributeList attrs = new AttributeList();
        for (int item = 0; item < attributes.length; item++) {
            Object attr = getAttribute(attributes[item]);
            if (attr != null) {
                attrs.add(new Attribute(attributes[item], attr));
            }
        }
        return attrs;
    }

    /**
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo() {
        return mbInfo;
    }

    /**
     * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[],
     *      java.lang.String[])
     */
    public Object invoke(String actionName, Object[] params, String[] signature) {
        return null;
    }

    /**
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        String attributeName = getFormattedAttribute(attribute.getName());
        for (int item = 0; item < profiles.length; item++) {

            if (profiles[item].getName().equals(attributeName)) {
                switchStatus(item, attribute);
                return;
            }
        }
    }

    /**
     * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    public AttributeList setAttributes(AttributeList attributes) {

        AttributeList resultList = new AttributeList();

        // if attributeNames is empty, nothing more to do
        if (attributes.isEmpty()) {
            return resultList;
        }

        // try to set each attribute and add to result list if successful
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Attribute attr = (Attribute) i.next();
            try {
                setAttribute(attr);
                String name = attr.getName();
                resultList.add(new Attribute(name, getAttribute(name)));
            }
            catch (Exception e) {
            	logger.error(ExceptionUtil.getExceptionAsString(e));            }
        }
        return resultList;
    }

    private void switchStatus(int item, Attribute attribute) {
        boolean priorValue = profilesStatus[item];
        profilesStatus[item] = (Boolean) attribute.getValue();

        if (priorValue && !profilesStatus[item]) {
            removeTCPService(item);
        }
        else if (!priorValue && profilesStatus[item]) {
            startTCPService(item);
        }
    }

    private void removeTCPService(int item) {
        int port = profiles[item].getLocalPort();

        MessagingConfig.getTCPManager().removeServiceAndConnections(TCPMessagingManager.LOCAL_HOST_SERVICE + CommonConstants.COLON + Integer.toString(port));

    }

    private void startTCPService(int item) {

        TCPProfile tcpProfile = profiles[item];

        // start a named service on the specified host and port.
        // dynamically load and instantiate ITCPService class
        if (TCPProfile.INVALID_PORT != tcpProfile.getLocalPort()) {
            try {
                ITCPService service = MessagingConfig.getTCPManager().createService(tcpProfile,
                        TCPMessagingManager.LOCAL_HOST_SERVICE, tcpProfile.getLocalPort());
                MessagingConfig.getTCPManager().startService(service, tcpProfile.getLocalPort());
            }
            catch (RuntimeException e) {
                logger.error("Failed to start TCP service for TCP profile named" + tcpProfile.getName() + "\"", e);
            }
        }
    }

    private void buildDynamicMBeanInfo() {

        profiles = getTCPProfileList();
        dAttributes = new MBeanAttributeInfo[profiles.length];
        profilesStatus = new boolean[profiles.length];

        for (int item = 0; item < profiles.length; item++) {

            dAttributes[item] = new MBeanAttributeInfo(profiles[item].getName(), "java.lang.Boolean", profiles[item].toString(),
                    true, true, false);
            profilesStatus[item] = true;
        }

        // use reflection to get constructor signatures
        dConstructors[0] = new MBeanConstructorInfo("UBTCPService(): No-parameter constructor", // description
                this.getClass().getConstructors()[0]); // the constructor
        // object

        mbInfo = new MBeanInfo(dClassName, dDescription, dAttributes, dConstructors, opers, new MBeanNotificationInfo[0]);
    }

    private TCPProfile[] getTCPProfileList() {

        List<TCPProfile> transportList = new ArrayList<TCPProfile>();

        Map<String, ITransportProfile> profileMap = MessagingConfig.getTransportList().getProfileMap();
        Iterator<ITransportProfile> it = profileMap.values().iterator();
        ITransportProfile profile = null;
        while (it.hasNext()) {
            profile = it.next();
            if (!(profile instanceof TCPProfile)) {
                continue;
            }

            transportList.add((TCPProfile) profile);
        }

        TCPProfile[] tcpProfiles = new TCPProfile[transportList.size()];
        for (int item = 0; item < transportList.size(); item++) {
            tcpProfiles[item] = transportList.get(item);
        }
        return tcpProfiles;
    }

    private String getFormattedAttribute(String attribte) {
        return attribte.replace('+', ' ');
    }
}
