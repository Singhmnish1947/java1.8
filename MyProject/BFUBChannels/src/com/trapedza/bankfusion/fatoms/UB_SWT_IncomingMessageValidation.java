package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_IncomingMessageValidation;

import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.SubCode;

public class UB_SWT_IncomingMessageValidation extends AbstractUB_SWT_IncomingMessageValidation {
    private static final long serialVersionUID = 1L;
    private static final Log LOGGER = LogFactory.getLog(UB_SWT_IncomingMessageValidation.class);

    public UB_SWT_IncomingMessageValidation() {
        super();
    }

    public UB_SWT_IncomingMessageValidation(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        Boolean isStraightThroughProcess = Boolean.FALSE;
        HashMap input = new HashMap();
        input.put(MFInputOutPutKeys.Ub_MT103_Key, getF_IN_Ub_MT103());
        input.put(MFInputOutPutKeys.messageID_Key, getF_IN_messageID());
        try {
            HashMap output = MFExecuter.executeMF(MFInputOutPutKeys.UB_SWT_ValidateThresholdAmount_SRV, env, input);
            if (output != null) {
                isStraightThroughProcess = (Boolean) output.get("isStraightThroughProcess");
            }

        }
        catch (BankFusionException e) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(e));
            buildErrorResponse(e);
        }

        setF_OUT_isStraightThroughProcess(isStraightThroughProcess);

    }

    private void buildErrorResponse(BankFusionException e) {
        IEvent errors = e.getEvents().iterator().next();
        int error = e.getEvents().iterator().next().getEventNumber();
        String errorCode = Integer.toString(error);
        SubCode subCode = new SubCode();
        MessageStatus txnStatus = new MessageStatus();
        Object parameterList = null;
        if (errors.getDetails() != null && errors.getDetails().length != 0) {
            for (int i = 0; i < errors.getDetails().length; i++) {
                EventParameters parameter = new EventParameters();
                parameterList = errors.getDetails()[i];
                parameter.setEventParameterValue(parameterList.toString());
                subCode.addParameters(parameter);
            }
        }
        subCode.setCode(errorCode);
        subCode.setDescription(e.getEvents().iterator().next().getMessage());
        subCode.setFieldName(CommonConstants.EMPTY_STRING);
        subCode.setSeverity(CBSConstants.ERROR);
        txnStatus.addCodes(subCode);
        txnStatus.setOverallStatus("E");
        setF_OUT_ErrorNumber(errorCode);
        setF_OUT_MessageStatus(txnStatus);
    }
}
