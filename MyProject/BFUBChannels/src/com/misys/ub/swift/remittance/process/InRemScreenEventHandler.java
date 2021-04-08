package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_InRemScreenEventHandler;

public class InRemScreenEventHandler extends AbstractUB_SWT_InRemScreenEventHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -1461818310898888012L;

    public InRemScreenEventHandler() {

    }

    public InRemScreenEventHandler(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {

        String context = getF_IN_context();
        switch(context)
        {
            case "INST_AMT_DISPLAY":
                populateInstAmtAndCcy(env);
                break;
                
            default:
                break;
        }
    }

    private void populateInstAmtAndCcy(BankFusionEnvironment env) {
        if (null == getF_IN_instructedAmt() || BigDecimal.ZERO.compareTo(getF_IN_instructedAmt()) >= 0) {
            setF_OUT_instructedAmt(null);
            setF_OUT_instrucedAmtCcy(CommonConstants.EMPTY_STRING);
        }
        else {
            setF_OUT_instrucedAmtCcy(getF_IN_instructedAmtCcy());
            setF_OUT_instructedAmt(getF_IN_instructedAmt());
        }
    }

}
