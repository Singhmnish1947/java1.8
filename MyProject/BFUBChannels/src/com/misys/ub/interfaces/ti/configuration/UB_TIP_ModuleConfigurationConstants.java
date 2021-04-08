package com.misys.ub.interfaces.ti.configuration;


import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

/**
 * @author ravir
 * @date Apr 29, 2009
 * @project Universal Banking
 * @Description: ModuleConfiguration constants for TIPlus
 */

public class UB_TIP_ModuleConfigurationConstants {
    private static IBusinessInformation bizInfo = null;
    static {
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        bizInfo = ubInformationService.getBizInfo();
    }

    /** <code>MODULE_CONFIG_KEY</code> = "TIP". */
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

    /** <code>UBTI_SUSPENSE_CONTEXT</code> = "UBTI Context". */
    static final String UBTI_SUSPENSE_CONTEXT = "UBTIContext";
    static final String CONTRA_SUSPENSE_ACCOUNT = "CONTRA_SUSPENSE_ACCOUNT";
    public static final int totalTxnLegs = 1;

    /** <code>SYSTEM SUSPENSE ACCOUNT MODULE NAME</code> = "SYSTEM SUSPENSE ACCOUNT MODULE NAME". */
    static final String SYSTEM_SUSPENSE_MODULE_NAME = "SYS";;

    /** <code>SYSTEM SUSPENSE ACCOUNT KEY</code> = "SYSTEM SUSPENSE ACCOUNT KEY". */
    static final String SYSTEM_SUSPENCE_ACCOUNT_KEY = "SUSPENSEACCOUNT";;

    public static String getUBTICreditSuspensePsyNym() {
        return ((String) bizInfo.getModuleConfigurationValue(TI_MODULE_CONFIG_KEY, CREDIT_SUSPENSE_PSEUDONYM, null));
    }

    public static String getUBTICreditTxnCode() {
        return ((String) bizInfo.getModuleConfigurationValue(TI_MODULE_CONFIG_KEY, CREDIT_TRANSACTION_CODE, null));
    }

    public static String getUBTIDebitSuspensePsyNym() {
        return ((String) bizInfo.getModuleConfigurationValue(TI_MODULE_CONFIG_KEY, DEBIT_SUSPENSE_PSEUDONYM, null));
    }

    public static String getUBTIDebitTxnCode() {
        return ((String) bizInfo.getModuleConfigurationValue(TI_MODULE_CONFIG_KEY, DEBIT_TRANSACTION_CODE, null));
    }

    public static String getUBTISuspenseContext() {
        return ((String) bizInfo.getModuleConfigurationValue(TI_MODULE_CONFIG_KEY, UBTI_SUSPENSE_CONTEXT, null));
    }

    public static String getUBTIContraSuspensePsyNym() {
        return ((String) bizInfo.getModuleConfigurationValue(TI_MODULE_CONFIG_KEY, CONTRA_SUSPENSE_ACCOUNT, null));
    }

    public static String getSystemSuspensePsyNym() {
        return ((String) bizInfo.getModuleConfigurationValue(SYSTEM_SUSPENSE_MODULE_NAME, SYSTEM_SUSPENCE_ACCOUNT_KEY, null));
    }
}
