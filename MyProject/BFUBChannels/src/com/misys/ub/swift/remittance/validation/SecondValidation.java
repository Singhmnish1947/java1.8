package com.misys.ub.swift.remittance.validation;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.fatoms.UB_SWT_SecondPartyIdentifierValidation;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class SecondValidation implements ISwiftMessageValidator {
    private transient final static Log LOGGER = LogFactory.getLog(SecondValidation.class);
    private ValidationHelper helper = new ValidationHelper();

    @SuppressWarnings("unchecked")
    @Override
    public RsHeader validate(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
        rsHeader.setStatus(status);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("Inside SWIFT Message Second Validation");

        Map map = Maps.newHashMap();
        map.put("BeneficairyCustomerIdentifierCode", swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustIdentifierCode());
        map.put("BeneficairyCustomerPartyIdentifier", swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifier());
        map.put("BeneficairyInstituteIdentifierCode", swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode());
        String benPartyIdentifierCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryInstitution().getBeneficiaryInstPartyIdentifier();
        String benNccClearingCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryInstitution().getBeneficiaryInstPartyClearingCode();
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)
                && !benNccClearingCode.isEmpty()) {
            rsHeader = helper.validateNCCCodes(benPartyIdentifierCode, benNccClearingCode,
                    MFInputOutPutKeys.BEN_INST_PARTY_PARAMETER, remittanceDto, rsHeader);
            if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR)) {
                return rsHeader;
            }
        }
        map.put("BeneficairyInstitutePartyIdentifier",
                helper.appendNccCode(
                        swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                                .getBeneficiaryInstitution().getBeneficiaryInstPartyIdentifier(),
                        swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                                .getBeneficiaryInstitution().getBeneficiaryInstPartyClearingCode()));
        map.put("BeneficiaryCustomerText1", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine1());
        map.put("BeneficiaryCustomerText2", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine2());
        map.put("BeneficiaryCustomerText3", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine3());
        map.put("BeneficiaryCustomerText4", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine4());
        map.put("BeneficiaryInstituteText1", swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine1());
        map.put("BeneficiaryInstituteText2", swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine2());
        map.put("BeneficiaryInstituteText3", swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine3());
        map.put("BeneficiaryInstituteText4", swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine4());
        map.put("Generate103Plus",
                Boolean.TRUE.equals(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getGenerate103PlusInd())
                        ? PaymentSwiftConstants.YES
                        : PaymentSwiftConstants.NO);
        map.put("IntermediaryIdentifierCode", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                .getIntermediary().getIntermediaryIdentiferCode());
        String intermediatePartyIdentifierCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                .getIntermediary().getIntermediaryPartyIdentifier();
        String intermediatePartyNCCClearingCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                .getIntermediary().getIntermediaryPartyIdfrClrngCode();
        // validate NCC codes
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)
                && !intermediatePartyNCCClearingCode.isEmpty()) {
            rsHeader = helper.validateNCCCodes(intermediatePartyIdentifierCode, intermediatePartyNCCClearingCode,
                    MFInputOutPutKeys.INTERMEDIARY_PARTY_PARAMETER, remittanceDto, rsHeader);
            if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR)) {
                return rsHeader;
            }
        }
        // append NCC codes
        map.put("IntermediaryPartyIdentifier",
                helper.appendNccCode(intermediatePartyIdentifierCode, intermediatePartyNCCClearingCode));
        map.put("IntermediaryText1", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                .getIntermediaryDetails().getTextLine1());
        map.put("IntermediaryText2", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                .getIntermediaryDetails().getTextLine2());
        map.put("IntermediaryText3", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                .getIntermediaryDetails().getTextLine3());
        map.put("IntermediaryText4", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                .getIntermediaryDetails().getTextLine4());
        map.put("PayToIdentifierCode",
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToIdentifierCode());

        String payToPartyIdentifierCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                .getPayToPartyIdentifier();
        String payToPartyNccClearingCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                .getPayToPartyIdentifierClearingCode();
        // validate NCC codes
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)
                && !payToPartyNccClearingCode.isEmpty()) {
            rsHeader = helper.validateNCCCodes(payToPartyIdentifierCode, payToPartyNccClearingCode,
                    MFInputOutPutKeys.PAY_TO_PARTY_PARAMETER, remittanceDto, rsHeader);
            if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR)) {
                return rsHeader;
            }
        }
        // append NCC codes
        map.put("PayToPartyIdentifier", helper.appendNccCode(payToPartyIdentifierCode, payToPartyNccClearingCode));
        map.put(MFInputOutPutKeys.PayToText1, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                .getPayToDetails().getPayDtls1());
        map.put(MFInputOutPutKeys.PayToText2, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                .getPayToDetails().getPayDtls2());
        map.put(MFInputOutPutKeys.PayToText3, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                .getPayToDetails().getPayDtls3());
        map.put(MFInputOutPutKeys.PayToText4, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                .getPayToDetails().getPayDtls4());
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {  
            Map<String, Object> result = MFExecuter.executeMF(
                    PaymentSwiftConstants.UB_SWT_SETTLEMENTINSTRUCTIONSECONDVALIDATION_SRV,
                    BankFusionThreadLocal.getBankFusionEnvironment(), map);

            if (null != result && !result.get(MFInputOutPutKeys.VALIDATION_STATUS).equals("true")) {
                    LOGGER.error("Second  VALIDATION FAILED!!");
                rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_SWIFT_VALIDATION_EVENT_CODE,
                        (String) result.get(MFInputOutPutKeys.ERROR_MESSAGE));
            }
        }

        //siSecondPartyValidation
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            rsHeader = siSecondPartyValidation(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     * @param rsHeader
     * @return
     */
    private RsHeader siSecondPartyValidation(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN siSecondPartyValidation");
        try {
            UB_SWT_SecondPartyIdentifierValidation secondPartyValidation = new UB_SWT_SecondPartyIdentifierValidation(
                    BankFusionThreadLocal.getBankFusionEnvironment());
            secondPartyValidation.setF_IN_BeneficiaryCustomerIdentifierCombo(swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifierCode());
            secondPartyValidation.setF_IN_BeneficiaryCustomerPartyIdentifier(swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifier());
            secondPartyValidation.process(remittanceDto.getEnv());
        }
        catch (CollectedEventsDialogException collectedEventexc) {
            SubCode subCode = new SubCode();
            MessageStatus status = new MessageStatus();
            subCode.setCode(String.valueOf(collectedEventexc.getEvents().iterator().next().getEventNumber()));
            Object param = new Object();
            ArrayList<Object> paramsList = new ArrayList<>();
            for (int j = 0; j < collectedEventexc.getEvents().iterator().next().getDetails().length; j++) {
                EventParameters parameter = new EventParameters();
                param = collectedEventexc.getEvents().iterator().next().getDetails()[j];
                if (null != param) {
                    parameter.setEventParameterValue(param.toString());
                    paramsList.add(param.toString());
                }
                subCode.addParameters(parameter);
            }
            subCode.setDescription(helper.getErrorDescription(subCode.getCode(), paramsList.toArray(), remittanceDto.getEnv()));
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            status.addCodes(subCode);
            status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
            rsHeader.setStatus(status);
        }

        return rsHeader;
    }

}
