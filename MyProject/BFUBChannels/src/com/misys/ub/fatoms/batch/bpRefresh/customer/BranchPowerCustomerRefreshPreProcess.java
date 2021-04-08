/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerCustomerRefreshPreProcess.java,v.1.1.2.2,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerCustomerRefreshPreProcess.java,v $
 * Revision 1.1.2.3  2008/08/22 00:26:19  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.customer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.IBatchPreProcess;
import com.trapedza.bankfusion.batch.process.PreProcessException;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * BranchPowerRefreshPreProcess
 */
public class BranchPowerCustomerRefreshPreProcess implements IBatchPreProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final Log logger = LogFactory.getLog(BranchPowerCustomerRefreshProcess.class.getName());

    /**
     * Set environment variable
     * 
     * @param environment
     *            For getting a handle on the BankFusion environment
     * @see com.trapedza.bankfusion.batch.process.IBatchPreProcess#
     *      init(com.trapedza.bankfusion.commands.core.BankFusionEnvironment,
     *      com.trapedza.bankfusion.batch.fatom.AbstractFatomContext)
     */
    private BankFusionEnvironment env;
    public void init(BankFusionEnvironment environment) throws PreProcessException {
    }

    public void process(AbstractFatomContext context) throws PreProcessException {
        Object[] propertiesObj = new Object[4];
        String path = GetUBConfigLocation.getUBConfigLocation();
        try {
            propertiesObj[0] = loadInfoFromLocal(path + "/conf/bpRefresh/BPRefresh.properties");
            propertiesObj[1] = loadInfoFromLocal(path + "/conf/bpRefresh/Refresh.properties");
        }
        catch (Exception fnfExcpn) {
            propertiesObj[0] = loadInfoFromJar("bpRefresh/BPRefresh.properties");
        }
        String fromBranch = null;
        String toBranch = null;
        String extractPath = null;
        String CustRefFlag = null;
        Properties prop = (Properties) propertiesObj[0];

        if ((prop.size() == 0)) {
            propertiesObj[0] = new BankFusionException(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { "Error Reading Properties File" }, logger, env);
            context.setAdditionalProcessParams(propertiesObj);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { "Error Reading Properties File" }, new HashMap(), env);
        }
        else {
            fromBranch = prop.get("FROMBRANCH").toString();
            toBranch = prop.get("TOBRANCH").toString();
            extractPath = prop.get("EXTRACTPATH").toString();
            CustRefFlag = prop.get("CUSTOMER-REFRESH").toString();
            if (CustRefFlag.equals("") || fromBranch.equals("") || toBranch.equals("") || extractPath.equals("")) {
                propertiesObj[0] = new BankFusionException(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                        new Object[] { "Invalid Parameters passed" }, logger, env);
                context.setAdditionalProcessParams(propertiesObj);
                EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                        new Object[] { "Invalid Parameters passed" }, new HashMap(), env);
            }
            Map<String, String> bpRefreshPropertiesMap = new HashMap<String, String>((Map) prop);
            propertiesObj[2] = bpRefreshPropertiesMap;
        }
        Properties refreshProperties = (Properties) propertiesObj[1];
        if (refreshProperties.size() > 0) {
            Map<String, String> refreshPropertiesMap = new HashMap<String, String>((Map) refreshProperties);
            propertiesObj[3] = refreshPropertiesMap;
        }
        context.setAdditionalProcessParams(propertiesObj);
    }

    /**
     * loads information available in (conf)\refresh.properties file to memory
     * 
     * @param string
     */
    private Properties loadInfoFromLocal(String string) throws BankFusionException {
        Properties fileProp = null;
		InputStream input = null;
        try {
			input = new FileInputStream(string);
            fileProp = new Properties();
            fileProp.load(input);
            return fileProp;
        }
        catch (Exception e) {
            fileProp = new Properties();
		} finally {
			try {
				if (input != null)
				input.close();
			} catch (IOException e) {
				logger.error(e.getStackTrace());
			}
        }
        return fileProp;
    }

    private Properties loadInfoFromJar(String string) {
        Properties fileProp = null;
		InputStream input = null;
        try {
			input = this.getClass().getClassLoader()
					.getResourceAsStream(string);
            fileProp = new Properties();
            fileProp.load(input);
        }
        catch (Exception e) {
            fileProp = new Properties();
		} finally {
			try {
				if (input != null)
				input.close();
			} catch (IOException e) {
logger.error(e.getStackTrace());			}
        }
        return fileProp;
    }

}
