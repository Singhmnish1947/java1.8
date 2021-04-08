/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerAccountRefreshPreProcess.java,v.1.1.2.3,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerAccountRefreshPreProcess.java,v $
 * Revision 1.1.2.4  2008/08/22 00:26:15  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.account;

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
public class BranchPowerAccountRefreshPreProcess implements IBatchPreProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final Log logger = LogFactory.getLog(BranchPowerAccountRefreshProcess.class.getName());

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
        this.env = environment;
    }

    public void process(AbstractFatomContext context) throws PreProcessException {

        Object[] propertiesObj = new Object[5];
        String path = GetUBConfigLocation.getUBConfigLocation();
        try {
            propertiesObj[0] = loadInfoFromLocal(path + "/conf/bpRefresh/BPRefresh.properties");
            propertiesObj[1] = loadInfoFromLocal(path + "/conf/bpRefresh/Refresh.properties");

        }
        catch (Exception fnfExcpn) {
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                    new Object[] { "Error Reading Properties File" }, new HashMap(), env);

        }

        Object obj[] = null;
        obj = (Object[]) propertiesObj;
        String fromBranch = null;
        String toBranch = null;
        String extractPath = null;
        String AccRefFlag = null;

        // artf574077-Deleted 'Hashtable currencyHash'.The pre-process class was storing currency
        // and its scale
        // into Hashtable, so that it can
        // fetch currency scale in the process class.This is not required at all as this is now
        // fetch using SystemInformation when required.
        Properties prop1 = (Properties) obj[0];
        Properties prop2 = (Properties) obj[1];

        try {
            if ((prop1 == null || prop2 == null || prop1.size() == 0 || prop2.size() == 0)) {
                propertiesObj[0] = new BankFusionException(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                        new Object[] { "Error Reading Properties File" }, logger, env);
                context.setAdditionalProcessParams(propertiesObj);
                EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                        new Object[] { "Error Reading Properties File" }, new HashMap(), env);
            }
            else {
                fromBranch = prop1.get("FROMBRANCH").toString();
                toBranch = prop1.get("TOBRANCH").toString();
                extractPath = prop1.get("EXTRACTPATH").toString();
                AccRefFlag = prop1.get("ACCOUNT-REFRESH").toString();

                if (AccRefFlag.equals("") || fromBranch.equals("") || toBranch.equals("") || extractPath.equals("")) {
                    propertiesObj[0] = new BankFusionException(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                            new Object[] { "Invalid Parameters passed" }, logger, env);
                    context.setAdditionalProcessParams(propertiesObj);
                    EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                            new Object[] { "Invalid Parameters passed" }, new HashMap(), env);
                }
            }

            Properties bpRefreshProperties = (Properties) propertiesObj[0];
            if (bpRefreshProperties.size() > 0) {
                Map<String, String> bpRefreshPropertiesMap = new HashMap<String, String>((Map) bpRefreshProperties);
                propertiesObj[3] = bpRefreshPropertiesMap;
            }

            Properties refreshProperties = (Properties) propertiesObj[1];
            if (refreshProperties.size() > 0) {
                Map<String, String> refreshPropertiesMap = new HashMap<String, String>((Map) refreshProperties);
                propertiesObj[4] = refreshPropertiesMap;
            }

        }
        catch (Exception e) {
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "Currency data empty" },
                    new HashMap(), env);
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
				if(input!=null)
				input.close();
			} catch (IOException e) {
				logger.error(e.getStackTrace());
			}
        }
        return fileProp;
    }

   

}
