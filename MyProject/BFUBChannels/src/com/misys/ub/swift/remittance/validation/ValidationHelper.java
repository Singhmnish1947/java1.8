package com.misys.ub.swift.remittance.validation;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.fatoms.UB_SWT_MaskFormatValidation;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class ValidationHelper {
    private transient final static Log LOGGER = LogFactory.getLog(ValidationHelper.class);

    /**
     * @param identifierCode
     * @param nccCode
     * @return
     */
    @SuppressWarnings("unchecked")
    public String appendNccCode(String identifierCode, String nccCode) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN appendNccCode");

        Map map = Maps.newHashMap();
        String partyCode = StringUtils.EMPTY;
        map.put(MFInputOutPutKeys.IDENTIFIER_CODE, identifierCode);
        map.put(MFInputOutPutKeys.NCC_CODE, nccCode);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = MFExecuter.executeMF(PaymentSwiftConstants.UB_SWT_APPENDNCCCODES_SRV,
                BankFusionThreadLocal.getBankFusionEnvironment(), map);
        if (result != null) {
            partyCode = (String) result.get(MFInputOutPutKeys.PARTY_CODE);
        }
        return partyCode;
    }

    /**
     * @param identifierCode
     * @param nccClearingCode
     * @param parameter
     * @param remittanceDto
     * @return
     */
    public RsHeader validateNCCCodes(String identifierCode, String nccClearingCode, String parameter,
            RemittanceProcessDto remittanceDto, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validate nccClearingCode:::" + nccClearingCode + " identifierCode:::" + identifierCode);

        UB_SWT_MaskFormatValidation maskVal = new UB_SWT_MaskFormatValidation(remittanceDto.getEnv());
        maskVal.setF_IN_MaskInput(appendNccCode(identifierCode, nccClearingCode));
        maskVal.process(remittanceDto.getEnv());
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)
                && !maskVal.isF_OUT_IsValidFormat()) {
            rsHeader = setErrorResponse(SwiftEventCodes.E_SWT_MASK_VALIDATION_UB, new String[] { parameter, nccClearingCode });
        }

        return rsHeader;
    }

    /**
     * @param eventCode
     * @param param1
     * @param env
     * @return
     */
    public String getErrorDescription(String eventCode, Object[] params, BankFusionEnvironment env) {
        String errorRsn = StringUtils.EMPTY;
        if (!StringUtils.isBlank(eventCode)) {
            errorRsn = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(eventCode), params,
                    env.getUserSession().getUserLocale());
        }
        return errorRsn;

    }

    /**
     * @param eventCode
     * @param parameter
     * @return
     */
    public RsHeader setErrorResponse(int eventCode, String parameter) {
        String eventCodeStr = String.valueOf(eventCode);
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        SubCode codes = new SubCode();
        EventParameters eventParams = new EventParameters();
        eventParams.setEventParameterValue(parameter);
        codes.setCode(eventCodeStr);
        codes.setDescription(
                getErrorDescription(eventCodeStr, new Object[] { parameter }, BankFusionThreadLocal.getBankFusionEnvironment()));
        codes.addParameters(eventParams);
        codes.setSeverity(PaymentSwiftConstants.ERROR_STATUS);
        status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
        status.addCodes(codes);
        rsHeader.setStatus(status);
        return rsHeader;
    }

    /**
     * @param eventCode
     * @param args
     * @return
     */
    public RsHeader setErrorResponse(int eventCode, String[] args) {
        String eventCodeStr = String.valueOf(eventCode);
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        SubCode codes = new SubCode();
        ArrayList<EventParameters> vParametersArray = new ArrayList<EventParameters>();
        for (String param : args) {
            EventParameters eParameters = new EventParameters();
            eParameters.setEventParameterValue(param);
            vParametersArray.add(eParameters);
        }
        codes.setParameters((EventParameters[]) vParametersArray.toArray(new EventParameters[vParametersArray.size()]));
        codes.setCode(eventCodeStr);
        codes.setDescription(getErrorDescription(eventCodeStr, args, BankFusionThreadLocal.getBankFusionEnvironment()));
        codes.setSeverity(PaymentSwiftConstants.ERROR_STATUS);
        status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
        status.addCodes(codes);
        rsHeader.setStatus(status);
        return rsHeader;
    }

}
