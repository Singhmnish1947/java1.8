/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * $Id: ATMBalanceDownloadFileContext.java,v 1.1 2008/11/26 09:00:40 bhavyag Exp $
 *
 * $Log: ATMBalanceDownloadFileContext.java,v $
 * Revision 1.1  2008/11/26 09:00:40  bhavyag
 * merging 3-3B changes for bug 12581.
 *
 * Revision 1.1.4.2  2008/09/23 08:09:50  mangesh
 * BUGID - 12581 - new Batch process for processing ATM Balance Download.
 *
 *
 */
package com.misys.ub.fatoms.batch.sparrow.download;

import java.util.HashMap;
import java.util.Map;

import com.trapedza.bankfusion.batch.fatom.AbstractPersistableFatomContext;

/**
 * 
 * @author Mangesh Hagargi
 * 
 */
public class ATMBalanceDownloadFileContext extends AbstractPersistableFatomContext  {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */

	private static final String PROCESS_CLASSNAME = loadProcessClassName("ATMBalanceDownload",
			"com.misys.ub.fatoms.batch.sparrow.download.ATMBalanceDownloadFileProcess");

	private String batchProcessName = "ATMBalanceDownload";
	
   
    private Object[] additionalProcessParams;

    private Map inputTagDataMap;

    private Map outputTagDataMap;
    
    private Integer lengthOfAccount = 0;
    
    private Integer numRecordsProcessed = 0;   

    private Map accountStatusForAccountIndicator ;


	/**
	 * 
	 */
	public ATMBalanceDownloadFileContext() {
		this.additionalProcessParams = new Object[2];
        this.inputTagDataMap = java.util.Collections.synchronizedMap(new HashMap());
        this.outputTagDataMap = java.util.Collections.synchronizedMap(new HashMap());
 
 
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.trapedza.bankfusion.batch.fatom.AbstractFatomContext#
	 * getAdditionalProcessParams()
	 */
	@Override
	public Object[] getAdditionalProcessParams() {
		// TODO Auto-generated method stub
		return this.additionalProcessParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getBatchProcessName
	 * ()
	 */
	@Override
	public String getBatchProcessName() {
		
		return batchProcessName;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.trapedza.bankfusion.batch.fatom.AbstractFatomContext#
	 * setAdditionalProcessParams(java.lang.Object[])
	 */
	@Override
	public void setAdditionalProcessParams(Object[] additionalParams) {
		this.additionalProcessParams = additionalParams;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setBatchProcessName
	 * (java.lang.String)
	 */
	@Override
	public void setBatchProcessName(String processName) {
		this.batchProcessName = processName;

	}


	/**
	 * Returns the full name of the processing class that workers will load.
	 * @return PROCESS_CLASSNAME.
	 * @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getProcessClassName()
	 */
	public String getProcessClassName() {
		return loadProcessClassName(getBatchProcessName(), PROCESS_CLASSNAME);
	}

	/**
	 * @return the inputTagDataMap
	 */
	public Map getInputTagDataMap() {
		return inputTagDataMap;
	}

	/**
	 * @param inputTagDataMap the inputTagDataMap to set
	 */
	public void setInputTagDataMap(Map inputTagDataMap) {
		this.inputTagDataMap = inputTagDataMap;
	}

	/**
	 * @return the outputTagDataMap
	 */
	public Map getOutputTagDataMap() {
		return outputTagDataMap;
	}

	/**
	 * @param outputTagDataMap the outputTagDataMap to set
	 */
	public void setOutputTagDataMap(Map outputTagDataMap) {
		this.outputTagDataMap = outputTagDataMap;
	}



	/**
	 * @return the lengthOfAccount
	 */
	public Integer getLengthOfAccount() {
		return lengthOfAccount;
	}



	/**
	 * @param lengthOfAccount the lengthOfAccount to set
	 */
	public void setLengthOfAccount(Integer lengthOfAccount) {
		this.lengthOfAccount = lengthOfAccount;
	}

	/**
	 * @return the numRecordsProcessed
	 */
	public Integer getNumRecordsProcessed() {
		return numRecordsProcessed;
	}



	/**
	 * @param numRecordsProcessed the numRecordsProcessed to set
	 */
	public void setNumRecordsProcessed(Integer numRecordsProcessed) {
		this.numRecordsProcessed = numRecordsProcessed;
	}



	public Map getAccountStatusForAccountIndicator() {
		return accountStatusForAccountIndicator;
	}



	public void setAccountStatusForAccountIndicator(
			Map accountStatusForAccountIndicator) {
		this.accountStatusForAccountIndicator = accountStatusForAccountIndicator;
	}



	@Override
	public boolean isMultiNodeSupported() {
		// TODO Auto-generated method stub
		return true;
	}



}
