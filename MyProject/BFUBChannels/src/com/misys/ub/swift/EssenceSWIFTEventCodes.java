/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 */
package com.misys.ub.swift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.eventcode.AbstractEventCodes;


/**
 * @author 
 *
 */
public class EssenceSWIFTEventCodes extends AbstractEventCodes {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     */
    transient final static Log logger = LogFactory.getLog(EssenceSWIFTEventCodes.class.getName());

    private static final int SUB_SYSTEM_ID = 700;

    /**
     * Private constructor
     */
    public EssenceSWIFTEventCodes() {
        subsystemId = SUB_SYSTEM_ID;
        baseName = CommonConstants.EMPTY_STRING;
    }

}
