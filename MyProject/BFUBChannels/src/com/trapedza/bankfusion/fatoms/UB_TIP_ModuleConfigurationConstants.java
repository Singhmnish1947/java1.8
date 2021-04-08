package com.trapedza.bankfusion.fatoms;

/**
 * @author apoorva
 * @date Apr 29, 2009
 * @project Universal Banking
 * @Description: ModuleConfiguration constants for TIPlus
 */

public class UB_TIP_ModuleConfigurationConstants {

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
    
    /** <code>ALLOW_FORCE_POST</code> = "ForcePost". */
    static final String ALLOW_FORCE_POST = "ForcePost";
    
    /** <code>SUPERVISOR_AUTHORIZATION_REQUIRED</code> = "SupervisorAuth". */
    static final String SUPERVISOR_AUTHORIZATION_REQUIRED = "SupervisorAuth";
      
}
