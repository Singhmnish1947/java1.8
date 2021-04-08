package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ResultSetFormat;

public class SWT_ResultSetFormat extends AbstractSWT_ResultSetFormat {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("deprecation")
    public SWT_ResultSetFormat(BankFusionEnvironment env) {
        super(env);
    }

    public SWT_ResultSetFormat() {
    }

    private transient final static Log logger = LogFactory.getLog(SWT_ResultSetFormat.class.getName());

    public void process(BankFusionEnvironment env) throws BankFusionException {
		
		if(getF_IN_creditAccCurrency() != null && !getF_IN_creditAccCurrency().equals(""))	
			if(getF_IN_creditAmount() != null && getF_IN_expCreditAmount() != null){
				setF_OUT_CrAmt(roundAmount(getF_IN_creditAmount(), getF_IN_creditAccCurrency()));
				setF_OUT_ExpCrAmt(roundAmount(getF_IN_expCreditAmount(), getF_IN_creditAccCurrency()));
			}
		if(getF_IN_debitAccCurrency() != null && !getF_IN_debitAccCurrency().equals(""))	
			if(getF_IN_debitAmount() != null && getF_IN_expDebitAmount() != null){
				setF_OUT_DrAmt(roundAmount(getF_IN_debitAmount(), getF_IN_debitAccCurrency()));
				setF_OUT_ExpDrAmt(roundAmount(getF_IN_expDebitAmount(), getF_IN_debitAccCurrency()));
				setF_OUT_chargeAmount(roundAmount(getF_IN_chargeAmount(), getF_IN_chargeCurrency()));
			}
		
        String instructedAmtCcy = !StringUtils.isBlank(getF_IN_instructedAmtCcy()) ? getF_IN_instructedAmtCcy()
                : getF_IN_debitAccCurrency();
        if (null != getF_IN_instructedAMT()) {
            setF_OUT_instructedAMT(roundAmount(getF_IN_instructedAMT(), instructedAmtCcy));
            setF_OUT_instructedAmtCcy(instructedAmtCcy);
		}
		
		if(getF_IN_chargeCurrency() != null && !getF_IN_chargeCurrency().equals(""))	
			if(getF_OUT_chargeAmount() != null){
				setF_OUT_chargeAmount(roundAmount(getF_IN_chargeAmount(), getF_IN_chargeCurrency()));
			}
		
		if(getF_IN_senderChargeCur() != null && !getF_IN_senderChargeCur().equals(""))	
			if(getF_IN_senderChargeAmount() != null){
				setF_OUT_senderChargeAmount(roundAmount(getF_IN_senderChargeAmount(), getF_IN_senderChargeCur()));
			}
		
		String messageType = getF_IN_MessageType();
		if(messageType != null && (messageType.equals("MT103") ||(messageType.equals("MT200") ||  messageType.equals("MT202") || messageType.equals("MT205") ))){
			setF_OUT_trancsactionType(getSwifttransactionDesc(messageType.substring(2)));			
		}

		setF_OUT_outputVector(getF_IN_InputVector());
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
            VectorTable inputVectorTable = (VectorTable) outMap.get("CodeList");
            if (inputVectorTable != null && inputVectorTable.hasData()) {
                int Size = inputVectorTable.size();
                for (int i = 0; i < Size; i++) {
                    HashMap attributes = (HashMap) inputVectorTable.getRowTagsAsFields(i);
                    if (attributes != null) {
                        String messageType = attributes.get("f_STRVALUE").toString();
                        if (transactionType.equals(messageType)) {
                            transactionDescription = attributes.get("f_STRVALUE").toString();
                        }
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
