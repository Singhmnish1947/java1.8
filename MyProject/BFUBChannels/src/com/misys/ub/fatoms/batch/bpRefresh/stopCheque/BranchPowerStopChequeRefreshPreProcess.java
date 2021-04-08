/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerStopChequeRefreshPreProcess.java,v.1.1.2.2,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerStopChequeRefreshPreProcess.java,v $
 * Revision 1.1.2.3  2008/08/22 00:26:20  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.stopCheque;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.IBatchPreProcess;
import com.trapedza.bankfusion.batch.process.PreProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class BranchPowerStopChequeRefreshPreProcess implements IBatchPreProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

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

    // SQL Query to read currency Scale for amount formatting
    final String queryCurrency = "SELECT (T1." + IBOCurrency.ISOCURRENCYCODE + ") AS " + IBOCurrency.ISOCURRENCYCODE + ",(T1."
            + IBOCurrency.CURRENCYSCALE + ") AS " + IBOCurrency.CURRENCYSCALE + " FROM " + IBOCurrency.BONAME + " T1";
    private static final transient Log logger = LogFactory.getLog(BranchPowerStopChequeRefreshPreProcess.class.getName());

    public void init(BankFusionEnvironment environment) throws PreProcessException {
    }

    public void process(AbstractFatomContext context) throws PreProcessException {

        Object[] propertiesObj = new Object[5];
        String path = GetUBConfigLocation.getUBConfigLocation();
        try {
            propertiesObj[0] = loadInfoFromLocal(path + "/conf/bpRefresh/BPRefresh.properties");
            propertiesObj[1] = loadInfoFromLocal(path + "/conf/bpRefresh/Refresh.properties");
            Properties bpRefreshProperties = (Properties) propertiesObj[0];
            if (bpRefreshProperties.size() > 0) {
             Map<String, String> bpRefreshPropertiesMap = new HashMap<>((Map) bpRefreshProperties);
                propertiesObj[2] = bpRefreshPropertiesMap;
            }
            Properties refreshProperties = (Properties) propertiesObj[1];
            if (refreshProperties.size() > 0) {
                Map<String, String> refreshPropertiesMap = new HashMap<>((Map) refreshProperties);
                propertiesObj[3] = refreshPropertiesMap;
            }
        }
        catch (Exception fnfExcpn) {
            propertiesObj[0] = loadInfoFromJar("bpRefresh/BPRefresh.properties");
        }
        List currencyDetails = new ArrayList();
        HashMap<String, Integer> currencyMap = new HashMap<String, Integer>();
        currencyDetails = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(queryCurrency, null, null, true);
        SimplePersistentObject currencyPO = null;
        for (int i = 0; i < currencyDetails.size(); i++) {
            currencyPO = (SimplePersistentObject) currencyDetails.get(i);

            currencyMap.put((String)currencyPO.getDataMap().get(IBOCurrency.ISOCURRENCYCODE), (Integer) currencyPO.getDataMap().get(
                    IBOCurrency.CURRENCYSCALE));
        }

        propertiesObj[4] = currencyMap;
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
            fileProp = new java.util.Properties();
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
            fileProp = new java.util.Properties();
            fileProp.load(input);
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
}
