/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * $Id: ATMBalanceDownloadFilePreProcess.java,v 1.1 2008/11/26 09:00:40 bhavyag Exp $
 *
 * $Log: ATMBalanceDownloadFilePreProcess.java,v $
 * Revision 1.1  2008/11/26 09:00:40  bhavyag
 * merging 3-3B changes for bug 12581.
 *
 * Revision 1.1.4.2  2008/09/23 08:09:49  mangesh
 * BUGID - 12581 - new Batch process for processing ATM Balance Download.
 *
 *
 */
package com.misys.ub.fatoms.batch.sparrow.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.misys.cbs.common.util.log.CBSLogger;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.IBatchPreProcess;
import com.trapedza.bankfusion.boundary.outward.BankFusionPropertySupport;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
/**
 * This class will delete all the temporary download file which would have been created by the previous run of batch process.
 * If these files are not removed then during the post process incorrect files will get merged.
 * @author Mangesh Hagargi
 *
 */
public class ATMBalanceDownloadFilePreProcess implements IBatchPreProcess {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */
	private static final String CLASS_NAME = ATMBalanceDownloadFilePreProcess.class.getName();
		
	private static final transient CBSLogger logger = new CBSLogger(CLASS_NAME);
	
	private BankFusionEnvironment environment = null;
	
	private File atmDownloadDir = null;
	
	private File[] fileList = null;
	
	private static final String FILEDIR = "ATMDownload.FileDir";
	
	public ATMBalanceDownloadFilePreProcess() {
		// TODO Auto-generated constructor stub
	}

	public void init(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		this.environment = env;

	}
    /**
     * The method will create the handle for Balance download file and set the object in the context.
     */
	public void process(AbstractFatomContext context) {
		
		// TODO Auto-generated method stub
		ATMBalanceDownloadFileContext downloadContext = (ATMBalanceDownloadFileContext)context;
		// Create the download file;
		clearDownloadFile();
		// Get Length of account and set on the context.
		ATMControlDetails controlDetails = ATMConfigCache.getInstance().getInformation(environment);
		Map accountStatusForIndicator = storeAccountIndicator(controlDetails);
		downloadContext.setAccountStatusForAccountIndicator(accountStatusForIndicator);
		downloadContext.setLengthOfAccount(controlDetails.getDestAccountLength());
		
	}
	
	/**
	 * 
	 * @param controlDetails
	 * @return
	 */
	private Map storeAccountIndicator(ATMControlDetails controlDetails) {
		
		Map<Integer, String> accountStatusIndicatorMap = new HashMap<Integer, String>();
		accountStatusIndicatorMap.put(PasswordProtectedConstants.PASSWD_NOT_REQ, controlDetails.getNoPasswordRequired());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY, controlDetails.getStopped());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY, controlDetails.getStoppedPwdReqForPosAndEnq());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.CREDITS_NOT_ALLOWED, controlDetails.getNoCrTxnAllowed());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE, controlDetails.getPasswordRequiredForCrTxn());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.DEBITS_NOT_ALLOWED, controlDetails.getNoDrTxnAllowed());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE, controlDetails.getPasswordRequiredForDrTxn());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.PASSWD_REQ_FOR_ENQUIRY, controlDetails.getPasswordRequiredForEnq());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING, controlDetails.getPasswordRequiredForPosting());
		accountStatusIndicatorMap.put(PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY, controlDetails.getPasswordRequiredForAllTxn());
		accountStatusIndicatorMap.put(-9999, controlDetails.getStopped());
		accountStatusIndicatorMap.put(-99991, controlDetails.getInactiveAccount());
				
		return accountStatusIndicatorMap;
	}

	/**
	 * 
	 */
	private void clearDownloadFile() {
		// Get the file name from the properties file
		String path = BankFusionPropertySupport.getProperty(BankFusionPropertySupport.UB_PROPERTY_FILE_NAME, FILEDIR, "");
		atmDownloadDir = new File(path);
		
		fileList = atmDownloadDir.listFiles(new ATMDownloadFileFilter());
		
		for (File downloadfile : fileList){
			if (downloadfile.exists()){
			    downloadfile.delete();
			}
		}
	}


}
