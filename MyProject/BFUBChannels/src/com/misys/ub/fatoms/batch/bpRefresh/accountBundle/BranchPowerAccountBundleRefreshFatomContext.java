/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerAccountBundleRefreshFatomContext.java,v.1.1.2.1,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerAccountBundleRefreshFatomContext.java,v $
 * Revision 1.1.2.2  2008/08/22 00:26:19  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.accountBundle;

import java.util.HashMap;
import java.util.Map;

import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.core.SimplePersistentObject;

public class BranchPowerAccountBundleRefreshFatomContext extends AbstractFatomContext {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final String PROCESS_CLASSNAME = loadProcessClassName("BranchPowerAccountBundleRefresh",
            "com.misys.ub.fatoms.batch.bpRefresh.accountBundle.BranchPowerAccountBundleRefreshProcess");

    private Map inputTagDataMap;

    private Map outputTagDataMap;

    private Map simplePersistentObjectMap;

    private String serviceName;

    private String batchProcessName;

    private Object[] additionalParams;

    public static Boolean Status = Boolean.TRUE;

    /**
     * BalanceSheetCollectionFatomContext
     * 
     * @param batchProcessName
     *            Name of the process
     */
    public BranchPowerAccountBundleRefreshFatomContext(String batchProcessName) {
        this.batchProcessName = batchProcessName;
        additionalParams = new Object[2];
        inputTagDataMap = java.util.Collections.synchronizedMap(new HashMap());
        outputTagDataMap = java.util.Collections.synchronizedMap(new HashMap());
    }

    /**
     * Add a BO to the Persistent Object Map
     * 
     * @param name
     *            BO name
     * @param BO
     *            BO object
     */
    public void addIBOS(String name, SimplePersistentObject BO) {
        simplePersistentObjectMap.put(name, BO);
    }

    /**
     * Returns the full name of the processing class that workers will load.
     * 
     * @return The full name of the processing class that workers will load.
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getProcessClassName()
     */
    public String getProcessClassName() {
        return PROCESS_CLASSNAME;
    }

    /**
     * Set of parameters gathered during BalanceSheetCollection pre-process for use in process, post
     * process or accumulator classes. (Convienience method for generic Master/Worker handling).
     * 
     * @return Set of parameters gathered duing preprocess
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getAdditionalProcessParams()
     */
    public Object[] getAdditionalProcessParams() {
        return additionalParams;
    }

    /**
     * Allows look up of parameters such as BO Names for pre/post and process classes.
     * 
     * @return The name of the BalanceSheetCollection batch process
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getBOName()
     */
    public String getBatchProcessName() {
        return batchProcessName;
    }

    /**
     * Returns InputTag data from a Fatom
     * 
     * @return InputTag data from a Fatom
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getInputTagDataMap()
     */
    public Map getInputTagDataMap() {
        return inputTagDataMap;
    }

    /**
     * Returns OutputTag data from a Fatom
     * 
     * @return OutputTag data from a Fatom
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getOutputTagDataMap()
     */
    public Map getOutputTagDataMap() {
        return outputTagDataMap;
    }

    /**
     * Gets the name of the Service (Process) to be invoked.
     * 
     * @return The name of the Service (Process) to be invoked.
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getServiceName()
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setAdditionalProcessParams(java.lang.Object[])
     * 
     * The additionalParams attribute for the BSCtx are: additionalParams[0] =
     * (Integer)numberOfReportingCurrencies additionalParams[1] = (String[])reportingCurrencies
     * additionalParams[2] = (String)balanceSelection additionalParams[3] = (String)exchangeRateType
     * 
     * These are all set up in the Preprocess class.
     * 
     * @param additionalParams
     *            Additional parameters
     */
    public void setAdditionalProcessParams(Object[] additionalParams) {
        this.additionalParams = additionalParams;
    }

    /**
     * Sets the name of the batch process
     * 
     * @param batchProcessName
     *            The name of the batch process
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setBOName(java.lang.String)
     */
    public void setBatchProcessName(String batchProcessName) {
        this.batchProcessName = batchProcessName;
    }

    /**
     * InputTag data set by a Fatom
     * 
     * @param inDataMap
     *            InputTag data set by a Fatom
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setInputTagDataMap(java.util.Map)
     */
    public void setInputTagDataMap(Map inDataMap) {
        this.inputTagDataMap = inDataMap;
    }

    /**
     * OutputTag data set by a Fatom
     * 
     * @param outDataMap
     *            OutputTag data set by a Fatom
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setOutputTagDataMap(java.util.Map)
     */
    public void setOutputTagDataMap(Map outDataMap) {
        this.outputTagDataMap = outDataMap;
    }

    /**
     * Sets the name of the Service (Process) to be invoked.
     * 
     * @param serviceName
     *            The name of the Service (Process) to be invoked.
     * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setServiceName(java.lang.String)
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
