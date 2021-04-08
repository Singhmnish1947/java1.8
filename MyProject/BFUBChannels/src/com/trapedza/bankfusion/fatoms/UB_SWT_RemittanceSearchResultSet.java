package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_RemittanceSearchResultSet;

public class UB_SWT_RemittanceSearchResultSet extends AbstractUB_SWT_RemittanceSearchResultSet {
    private transient final static Log logger = LogFactory.getLog(UB_SWT_RemittanceSearchResultSet.class.getClass());

    public UB_SWT_RemittanceSearchResultSet(BankFusionEnvironment env) {
        super(env);
    }

    public UB_SWT_RemittanceSearchResultSet() {
    }

    public void process(BankFusionEnvironment env) throws BankFusionException {

        VectorTable InputVectorTable = getF_IN_InptResultSet();
        VectorTable OUTputVectorTable = new VectorTable();

        BigDecimal debitAmt = getF_IN_DebitAmount();

        BigDecimal ExpCreditAmt = getF_IN_ExpCreditAmount();
        BigDecimal ExpDebitAmt = getF_IN_ExpDebitAmount();

        String CrAcctCurr = getF_IN_CrAccCurrency();
        String DrAcctCurr = getF_IN_DrAccCurrency();
        String TnxCurr = getF_IN_TnxCurrency();

        if (getF_IN_SwiftMessageType() != null && getF_IN_SwiftMessageType().length() > 2)
            setF_OUT_MessageTypeDecs(getSwifttransactionDesc(getF_IN_SwiftMessageType().substring(2)));

        if (getF_IN_CreditAmount() != null)
            setF_OUT_CreditAmt(roundAmount(getF_IN_CreditAmount(), CrAcctCurr));

        if (getF_IN_DebitAmount() != null)
            setF_OUT_DebitAmt(roundAmount(getF_IN_DebitAmount(), DrAcctCurr));

        if (getF_IN_ExpCreditAmount() != null)
            setF_OUT_ExpCreditAmt(roundAmount(getF_IN_ExpCreditAmount(), CrAcctCurr));
        if (getF_IN_ExpDebitAmount() != null)
            setF_OUT_ExpDebitAmt(roundAmount(getF_IN_ExpDebitAmount(), DrAcctCurr));

        if (InputVectorTable != null && InputVectorTable.hasData()) {
            int Size = InputVectorTable.size();
            for (int i = 0; i < Size; i++) {
                HashMap attributes = (HashMap) InputVectorTable.getRowTagsAsFields(i);
                HashMap OUTattributes = new HashMap();
                if (attributes != null) {
                    String CrAmount = attributes.get("f_UBCREDITAMOUNT").toString();
                    String UBCurrency = attributes.get("f_UBCURRENCY").toString();
                    OUTattributes.put("UBCREDITAMOUNT", roundAmount(new BigDecimal(CrAmount), UBCurrency));
                    OUTattributes.put("boID", attributes.get("boID"));
                    OUTattributes.put("UBMESSAGEREFID", attributes.get("f_UBMESSAGEREFID"));
                    OUTattributes.put("UBMESSAGETYPE", attributes.get("f_UBMESSAGETYPE"));
                    OUTattributes.put("UBCREDITACCOUNT", attributes.get("f_UBCREDITACCOUNT"));
                    OUTattributes.put("UBCURRENCY", attributes.get("f_UBCURRENCY"));
                    OUTattributes.put("UBVALUEDATE", attributes.get("f_UBVALUEDATE"));

                    OUTputVectorTable.addAll(new VectorTable(OUTattributes));
                    // OUTvectorTable.addAll(attributes);
                }
            }
        }

        setF_OUT_OutptResultSet(OUTputVectorTable);

    }

    private BigDecimal roundAmount(BigDecimal amtDis, String accCUR) {
        if ("".equals(accCUR) || null == accCUR) {
            return BigDecimal.ZERO;
        }
        HashMap<String, Object> inputParams = new HashMap<String, Object>();
        inputParams.put("currency", accCUR);
        inputParams.put("inputAmount", amtDis);
        FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("UB_ATM_AmtCcyRounding_SRV");
        HashMap outputChargeParams = invoker.invokeMicroflow(inputParams, false);
        BigDecimal outputAmount = (BigDecimal) outputChargeParams.get("outAmount");
        return outputAmount;
    }

    public String getSwifttransactionDesc(String transactionType) {
        HashMap moduleParams = new HashMap<>();
        moduleParams.put("reference", "233");
        String transactionDescription = null;
        try {
            HashMap outMap = MFExecuter.executeMF("CB_GCD_GetCodeList_SRV", BankFusionThreadLocal.getBankFusionEnvironment(),
                    moduleParams);
            // debitAccountCurr = (String)outMap.get("ISOCURRENCYCODE");
            VectorTable InputVectorTable = (VectorTable) outMap.get("CodeList");
            if (InputVectorTable != null && InputVectorTable.hasData()) {
                int Size = InputVectorTable.size();
                for (int i = 0; i < Size; i++) {
                    HashMap attributes = (HashMap) InputVectorTable.getRowTagsAsFields(i);

                    if (attributes != null) {
                        String messageType = attributes.get("f_STRVALUE").toString();
                        if (transactionType.equals(messageType)) {
                            transactionDescription = attributes.get("f_STRDESCRIPTION").toString();
                        }
                        // String messageDescription =
                        // attributes.get("f_STRDESCRIPTION").toString();

                    }
                }
            }

        }
        catch (Exception E) {
            logger.error(ExceptionUtil.getExceptionAsString(E));
        }

        return transactionDescription;
    }

}