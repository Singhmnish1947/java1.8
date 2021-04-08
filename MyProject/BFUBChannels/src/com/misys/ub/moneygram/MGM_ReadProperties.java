/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: MGM_ReadProperties.java,v 1.5 2008/08/12 20:15:26 vivekr Exp $
 *
 */

package com.misys.ub.moneygram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * This class is used for reading the moneygram.properties file.
 * 
 * @author vinayachandrakanta and nileshk
 * 
 */
public class MGM_ReadProperties {

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
    private transient final static Log logger = LogFactory.getLog(MGM_ReadProperties.class.getName());
    public static final String MONEYGRAM_PROPERTY_FILENAME = "conf/moneygram/moneygram.properties";
    public static final String DESTINATION_PATH = "FileOutputDir";
    public static final String TIMEOUT = "TimeOut";

    /**
     * @param env
     * @return @
     */
    public String getTimeOut(BankFusionEnvironment env) {
        String timeOut = "0.0";
        String configLocation = null;
        InputStream is = null;
        Properties moneyGramProperties = new Properties();
        try {

            // configLocation = System.getProperty("BFconfigLocation",
            // CommonConstants.EMPTY_STRING);
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            is = new FileInputStream(configLocation + MGM_ReadProperties.MONEYGRAM_PROPERTY_FILENAME);
            moneyGramProperties.load(is);

        }
        catch (Exception ex) {
            // if moneygram.properties file is not found at client conf , check at config resource
            // folder
            if (is == null) {
                if (logger.isDebugEnabled())
                    logger.debug(configLocation + MGM_ReadProperties.MONEYGRAM_PROPERTY_FILENAME
                            + " not found as file, trying as resource");
                is = this.getClass().getClassLoader().getResourceAsStream(MGM_ReadProperties.MONEYGRAM_PROPERTY_FILENAME);
            }
            try {
                moneyGramProperties.load(is);
            }
            catch (IOException e) {
                // throw new BankFusionException(9294, null, logger, env);
                EventsHelper.handleEvent(ChannelsEventCodes.E_EXCEPTION_OCCURED_WHILE_READING_PROPERTY_FILE, new Object[] {},
                        new HashMap(), env);
            }

        }
        timeOut = moneyGramProperties.getProperty(MGM_ReadProperties.TIMEOUT).trim();
        return timeOut;
    }

    public String getDestinationPath(BankFusionEnvironment env) {
        String path = CommonConstants.EMPTY_STRING;
        String configLocation = null;
        InputStream is = null;
        Properties moneyGramProperties = new Properties();
        try {

            // configLocation = System.getProperty("BFconfigLocation",
            // CommonConstants.EMPTY_STRING);
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            is = new FileInputStream(configLocation + MGM_ReadProperties.MONEYGRAM_PROPERTY_FILENAME);
            moneyGramProperties.load(is);

        }
        catch (Exception ex) {
            // if moneygram.properties file is not found at client conf , check at config resource
            // folder
            if (is == null) {
                if (logger.isDebugEnabled())
                    logger.debug(configLocation + MGM_ReadProperties.MONEYGRAM_PROPERTY_FILENAME
                            + " not found as file, trying as resource");
                is = this.getClass().getClassLoader().getResourceAsStream(MGM_ReadProperties.MONEYGRAM_PROPERTY_FILENAME);
            }
            try {
                moneyGramProperties.load(is);
            }
            catch (IOException e) {
                // throw new BankFusionException(9294, null, logger, env);
                EventsHelper.handleEvent(ChannelsEventCodes.E_EXCEPTION_OCCURED_WHILE_READING_PROPERTY_FILE, new Object[] {},
                        new HashMap(), env);
            }

        }

        path = moneyGramProperties.getProperty(MGM_ReadProperties.DESTINATION_PATH).trim();

        while (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.equals(null) || path.trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
            // throw new BankFusionException(9293, null, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_DESTINATION_PATH_IS_NOT_SPECIFIED, new Object[] {}, new HashMap(), env);
        }
        else if (!((new File(path)).mkdirs()) && !((new File(path)).exists())) {
            // throw new BankFusionException(9295, null, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_DESTINATION_PATH, new Object[] {}, new HashMap(), env);
        }
        else {
            logger.info("Writing files to the directory :" + path);
        }

        return path;
    }
}
