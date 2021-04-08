package com.trapedza.bankfusion.fatoms;

import bf.com.misys.fbe.types.ModeOfOperationConrtol;
import bf.com.misys.fbe.types.ModeOfOperationCtrlDtls;

import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_ISO8583_ValidateModeOfOperationCtrl;

public class ValidateModeOfOperationCtrl extends AbstractUB_ATM_ISO8583_ValidateModeOfOperationCtrl {
    /**
     *
     */
    private static final long serialVersionUID = -5720180562238189317L;

    private static final int E_DUPLICATE_MODE_OF_OPERATION = 40421562;

    private static final int E_AUTH_REQ_FALSE = 40421564;

    private static final String GC_TRUE = "true";

    private static final String GC_FALSE = "false";

    @SuppressWarnings("deprecation")
    public ValidateModeOfOperationCtrl(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        // Read Inputs and setting variables

        ModeOfOperationCtrlDtls modeOfOperationCtrlDtls = getF_IN_modeOfOperationCtrlDtls();
        ModeOfOperationConrtol newModeOfOperationControl = getF_IN_modeOfOperationCtrl();
        ModeOfOperationConrtol[] listModeOfOperation = modeOfOperationCtrlDtls.getModeOfOperationControl();
        int numSameModeOfOperation = 0;
        int maxSameModeOfOperation = 0;

        for (ModeOfOperationConrtol modeOfOperation : listModeOfOperation) {
            if (!modeOfOperation.getSelect()
                    && (newModeOfOperationControl.getModeOfOperation().equals(modeOfOperation.getModeOfOperation()))) {
                // Customer already has a role in other rows.
                numSameModeOfOperation++;
            }

        }

        if (numSameModeOfOperation > maxSameModeOfOperation) {
            // Error customer has already been assigned a role.
            CommonUtil.handleUnParameterizedEvent(E_DUPLICATE_MODE_OF_OPERATION);
        }
        if (GC_FALSE.equals(newModeOfOperationControl.getIsCardAllowed())
                && GC_TRUE.equals(newModeOfOperationControl.getIsAuthReq())) {
            CommonUtil.handleUnParameterizedEvent(E_AUTH_REQ_FALSE);
        }

    }
}
