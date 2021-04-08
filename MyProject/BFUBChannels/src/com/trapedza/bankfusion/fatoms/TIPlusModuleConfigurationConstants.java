/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: TIPlusModuleConfigurationConstants.java,v.1.0,April 29, 2009 11:35:36 AM Apoorva
 *
 */
package com.trapedza.bankfusion.fatoms;

/**
 * @author apoorva
 * @date Apr 29, 2009
 * @project Universal Banking
 * @Description: ModuleConfiguration constants for TIPlus
 */

public class TIPlusModuleConfigurationConstants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    /** <code>MODULE_CONFIG_KEY</code> = "FEX". */
    static final String TI_MODULE_CONFIG_KEY = "TIP";

    /** <code>CREDIT_TRANSACTION_CODE</code> = "CrTxnCode". */
    static final String CREDIT_TRANSACTION_CODE = "CrTxnCode";
    
    /** <code>DEBIT_TRANSACTION_CODE</code> = "DrTxnCode". */
    static final String DEBIT_TRANSACTION_CODE = "DrTxnCode";
    
    /** <code>CREDIT_SUSPENSE_PSEUDONYM</code> = "CrSusPsdym". */
    static final String CREDIT_SUSPENSE_PSEUDONYM = "CrSusPsdym";
    
    /** <code>DEBIT_SUSPENSE_PSEUDONYM</code> = "DrSusPsdym". */
    static final String DEBIT_SUSPENSE_PSEUDONYM = "DrSusPsdym";
    
    /** <code>ONTRA_SUSPENSE_ACCOUNT_PSEUDONYM</code> = "CONTRA_SUSPENSE_PSEUDONYM". */
    static final String CONTRA_SUSPENSE_ACCOUNT_PSEUDONYM = "CONTRA_SUSPENSE_ACCOUNT";
    
    /** <code>ALLOW_FORCE_POST</code> = "ForcePost". */
    static final String ALLOW_FORCE_POST = "ForcePost";
    
    /** <code>SUPERVISOR_AUTHORIZATION_REQUIRED</code> = "SupervisorAuth". */
    static final String SUPERVISOR_AUTHORIZATION_REQUIRED = "SupervisorAuth";

    static final String PARAMVALUE_YES = "yes";
    static final String PARAMVALUE_NO = "no";

    /** <code>TI_SUBCODE_TYPE</code> = "TI". */
    static final String TI_SUBCODE_TYPE = "TI";
  
    /** <code>SUPERVISOR_AUTHORIZATION_REQUIRED</code> = "SupervisorAuth". */
    static final String ALTERNATE_GENERATE_REQUIRED = "AlternateGenerate ";

}
