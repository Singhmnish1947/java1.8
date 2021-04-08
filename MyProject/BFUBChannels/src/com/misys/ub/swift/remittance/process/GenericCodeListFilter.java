package com.misys.ub.swift.remittance.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.misys.cbs.common.util.CBSCommonUtils;
import com.misys.cbs.gcd.types.GcDetails;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GenericCodeListFilter;

public class GenericCodeListFilter extends AbstractUB_SWT_GenericCodeListFilter {

    /**
     * 
     */
    private static final long serialVersionUID = -4939645563958789873L;
    private static final String REFERENCE = "REFERENCE";
    private static final String STRDESCRIPTION = "STRDESCRIPTION";
    private static final String STRVALUE = "STRVALUE";

    public GenericCodeListFilter() {

    }

    public GenericCodeListFilter(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) {
        String cbParentRef = getF_IN_cbParentRef();
        VectorTable filteredCodeList = new VectorTable();
        ArrayList strValueList = new ArrayList();
        ArrayList strDescriptionList = new ArrayList();
        ArrayList referenceList = new ArrayList();

        if (!StringUtils.isBlank(cbParentRef)) {

            List<GcDetails> genericCodes = CBSCommonUtils.getGenricCodes(cbParentRef);

            if (null != genericCodes && "233".equals(cbParentRef)) {
                HashSet<String> toBeIncludedRefs = new HashSet<>();
                toBeIncludedRefs.add("103");
                toBeIncludedRefs.add("202");

                for (GcDetails genericCodeDetail : genericCodes) {
                    if (toBeIncludedRefs.contains(genericCodeDetail.getRef())) {
                        strValueList.add(genericCodeDetail.getValue());
                        strDescriptionList.add(genericCodeDetail.getDescription());
                        referenceList.add(genericCodeDetail.getRef());
                    }
                }

                HashMap vectDataMap = new HashMap();
                vectDataMap.put(STRVALUE, strValueList);
                vectDataMap.put(STRDESCRIPTION, strDescriptionList);
                vectDataMap.put(REFERENCE, referenceList);
                filteredCodeList.populateAllRows(vectDataMap);

            }
        }

        setF_OUT_codeList(filteredCodeList);
    }
}
