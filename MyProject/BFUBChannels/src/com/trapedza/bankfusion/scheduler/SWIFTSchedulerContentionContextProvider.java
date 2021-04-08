/* ***********************************************************************************
 * Copyright (c) 2003,2009 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Trapedza Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 */

package com.trapedza.bankfusion.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;
import com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint;

/**
 * 
 * 
 * @author Zubin
 * 
 */
public class SWIFTSchedulerContentionContextProvider implements ExtensionPoint {

    private static final transient Log logger = LogFactory.getLog(SWIFTSchedulerContentionContextProvider.class.getName());

    private Map<String, Object> attributes;

    /**
     * Default constructor
     */
    public SWIFTSchedulerContentionContextProvider() {
        attributes = new HashMap<String, Object>();
        attributes.put(CommonConstants.CONTENTION_CONTEXT, "SWIFT");
    }

    /**
     * @see com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint#continueProcess()
     */
    @Override
    public void continueProcess() {
        // dummy method added for the interface
    }

    /**
     * @see com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint#getAttributes()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map getAttributes() {
        return attributes;
    }

    /**
     * @see com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint#isComplete()
     */
    @Override
    public boolean isComplete() {
        // dummy method added for the interface
        return true;
    }

    /**
     * @see com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    @Override
    public void process(BankFusionEnvironment env) {
    }

    /**
     * @see com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint#registerWithUpdateLoggerManager(com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager)
     */
    @Override
    public void registerWithUpdateLoggerManager(UpdateAuditLoggerManager manager) {
        // dummy method added for the interface
    }

    /**
     * @see com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint#setAttributes(java.util.Map)
     */
    @Override
    public void setAttributes(Map attributes) {
        this.attributes.putAll(attributes);
    }
}
