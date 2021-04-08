package com.misys.ub.atm;

import java.util.ArrayList;
import java.util.List;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOCODETYPES;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.cache.ICacheService;
import com.trapedza.bankfusion.steps.refimpl.AbstractISO8583ProcCodeTxnNarrative;

/**
 * @author Gaurav Aggarwal
 * @date 08 Apr 2015
 * @project FBE
 * @Description This class file is used to get the transaction narrative for the processing codes.
 * 
 */

public class ISO8583ProcCodeTxnNarrative extends AbstractISO8583ProcCodeTxnNarrative {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2891534884599543355L;
	private static final String PREFIX = "ATMPOS_NARR_";
    private static final String CODE = "ATMPOS_NARR";
    private static final String UNDERSCORE = "_";
    private static final String COMMISIONTYPE = "C";
    private static final String MAINTYPE = "M";
    private static final String NOTAPPLICABLE = "NA";
    private static final String NARRATIVECLAUSE = " WHERE " + IBOCODETYPES.CODETYPE + " = ? AND " + IBOCODETYPES.LOCALEID + " = ?";
    private final ICacheService cache = (ICacheService) ServiceManager.getService(ServiceManager.CACHE_SERVICE);
    public static final String NARRATIVE_ID_KEY = "NARRATIVE_ID_KEY";
    public static final String NARRATIVE_ID_CACHE_KEY = "NARRATIVE_ID_CACHE_KEY";

    public ISO8583ProcCodeTxnNarrative(BankFusionEnvironment env) {
        super(env);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractISO8583ProcCodeTxnNarrative#process(com.trapedza
     * .bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        if (isF_IN_isPOSRequest()) {
            processPOSRequest();
        }
        else {
            processATMrequest();
        }
    }

    /**
     * This method is use to provide the narrative for main transaction and commission transaction
     * based on the message type and transaction type of ATM Messages
     */
    private void processATMrequest() {
        String commissionNarrative = getNarrative(getKey(getF_IN_ATMFinDetails().getFinancialDetails().getProcessingCode()
                .getTransactionType(), COMMISIONTYPE));
        String mainNarrative = getNarrative(getKey(getF_IN_ATMFinDetails().getFinancialDetails().getProcessingCode()
                .getTransactionType(), MAINTYPE));
        setF_OUT_CommNarrative(NOTAPPLICABLE.equals(commissionNarrative) ? CommonConstants.EMPTY_STRING : commissionNarrative);
        setF_OUT_MainNarrative(NOTAPPLICABLE.equals(mainNarrative) ? CommonConstants.EMPTY_STRING : mainNarrative);
    }

    /**
     * This method is use to provide the narrative for main transaction and commission transaction
     * based on the message type and transaction type of POS Messages
     */
    private void processPOSRequest() {
        String commissionNarrative = getNarrative(getKey(getF_IN_POSFinDetails().getFinancialDetails().getProcessingCode()
                .getTransactionType(), COMMISIONTYPE));
        String mainNarrative = getNarrative(getKey(getF_IN_POSFinDetails().getFinancialDetails().getProcessingCode()
                .getTransactionType(), MAINTYPE));
        setF_OUT_CommNarrative(NOTAPPLICABLE.equals(commissionNarrative) ? CommonConstants.EMPTY_STRING : commissionNarrative);
        setF_OUT_MainNarrative(NOTAPPLICABLE.equals(mainNarrative) ? CommonConstants.EMPTY_STRING : mainNarrative);
    }

    /**
     * This method will get the narrative from the cached generic codes and in case the values are
     * not available in cache service then it will call the method to load the defined narratives.
     * 
     * This method will store a default value 'NA' for the requested key if the key is not defined
     * in generic codes and this default value will be ignored while setting up the output values.
     * 
     * @param key
     * @return
     */
    private String getNarrative(String key) {
        String narrative = (String) cache.cacheGet(NARRATIVE_ID_CACHE_KEY, key);
        if (narrative == null || narrative.equals(CommonConstants.EMPTY_STRING) || narrative.equals(NOTAPPLICABLE)) {
            synchronized (ISO8583ProcCodeTxnNarrative.class) {
                narrative = (String) cache.cacheGet(NARRATIVE_ID_CACHE_KEY, key);
                if (narrative == null || narrative.equals(CommonConstants.EMPTY_STRING)|| narrative.equals(NOTAPPLICABLE)) {
                    loadNarrativeValues();
                    narrative = (String) cache.cacheGet(NARRATIVE_ID_CACHE_KEY, key);
                    if (narrative == null || narrative.equals(CommonConstants.EMPTY_STRING)|| narrative.equals(NOTAPPLICABLE)) {
                        cache.cachePut(NARRATIVE_ID_CACHE_KEY, key, NOTAPPLICABLE);
                    }
                }
            }
        }
        return narrative;
    }

    /**
     * This method will prepare the key.
     * 
     * @param processingCode
     * @param narrativeType
     * @return
     */
    private String getKey(String processingCode, String narrativeType) {
        return NARRATIVE_ID_KEY + PREFIX + processingCode + UNDERSCORE + narrativeType;
    }

    /**
     * This method is used to load all the defined narratives in cache.
     */
    
  
    private void loadNarrativeValues() {
        ArrayList params = new ArrayList();
        params.clear();
        params.add(CODE);
        params.add(FinderMethods.getDefaultLocale(BankFusionThreadLocal.getBankFusionEnvironment()));    
        @SuppressWarnings("FBPE")
        List<IBOCODETYPES> narrativeList = (BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOCODETYPES.BONAME, NARRATIVECLAUSE, params,
                null, false));
        if (narrativeList != null && narrativeList.size() > 0) {
            for (IBOCODETYPES codeValue : narrativeList) {
                cache.cachePut(NARRATIVE_ID_CACHE_KEY,NARRATIVE_ID_KEY.concat(codeValue.getF_SUBCODETYPE()),
                        codeValue.getF_DESCRIPTION());
            }
        }
    }
}
