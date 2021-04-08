package com.misys.ub.atm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCIB;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;

import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;

public class ExternalLoroIndicator {

    private IPersistenceObjectsFactory factory;

    private final String fetchCIB = "where " + IBOATMCIB.IMDCODE + " = ?";

    public HashMap getLoroIndicator(UB_ATM_Financial_Details finDetails) {
        HashMap result = new HashMap();
        factory = BankFusionThreadLocal.getPersistanceFactory();
        String cardIssuerFIID = finDetails.getCardIssuerData().getCardIssuerFIID();
        String acquiringInstId = finDetails.getFinancialDetails().getAcquiringInstitutionId();
        if (acquirerIdBelongstoOwnBank(acquiringInstId)) {
            if (cardIssuerIdBelongstoOwnBank(cardIssuerFIID)) {
                result.put("atmTxnType", "01");
                result.put("externalLoroIndicator", "LOCALTXN");
            } else {
                result.put("atmTxnType", "03");
                result.put("externalLoroIndicator", "LOROTXN");
            }
        } else {
            result.put("atmTxnType", "02");
            result.put("externalLoroIndicator", "EXTTXN");
        }
        return result;
    }

    private boolean acquirerIdBelongstoOwnBank(String acId) {
        return getIndicator(acId);
    }

    private boolean getIndicator(String cib) {
        ArrayList params = new ArrayList<>();
        params.add(cib);
        @SuppressWarnings("deprecation")
        List<IBOATMCIB> atmCib = factory.findByQuery(IBOATMCIB.BONAME, fetchCIB, params, null);
        if (atmCib.size() > 0) {
            return true;
        }
        return false;
    }

    private boolean cardIssuerIdBelongstoOwnBank(String cardIssuerFIID) {
        return getIndicator(cardIssuerFIID);
    }

}
