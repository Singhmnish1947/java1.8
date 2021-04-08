package com.misys.ub.swift.remittance.validation;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import com.trapedza.bankfusion.fatoms.UB_SWT_BeneficiaryCustomerAddressValidation;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.OrderingCustomer;
import bf.com.misys.cbs.types.swift.OrderingInstitution;

public class FirstValidation implements ISwiftMessageValidator {
    private transient final static Log LOGGER = LogFactory.getLog(FirstValidation.class);
    private ValidationHelper helper = new ValidationHelper();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.misys.ub.swift.remittance.validation.ISwiftMessageValidator#validate(bf.com.misys.cbs.
     * msgs.v1r0.SwiftRemittanceRq, com.misys.ub.swift.remittance.dto.RemittanceProcessDto)
     */
    @SuppressWarnings("unchecked")
    @Override
    public RsHeader validate(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
        rsHeader.setStatus(status);
 
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Inside SWIFT Message First Validation");
        Map map = Maps.newHashMap();
        map.put("BankInstructionCodeCombo", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getBankToBankInfo().getBankInstructionCode());
        map.put("BankInstructionCodeText", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getBankToBankInfo().getBankAddlInstrCode());
        map.put("BankOperationCode", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo()
                .getBankOperationCode());
        map.put("Generate103Plus",
                Boolean.TRUE.equals(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getGenerate103PlusInd())
                        ? PaymentSwiftConstants.YES
                        : PaymentSwiftConstants.NO);
        map.put("MessageType", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType());
        map.put("Narrative", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration());

        // OrderingCustomer
        OrderingCustomer ordgCustomer = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                .getOrderingCustomer();
        if (null != ordgCustomer) {
            map.put("OrderingCustomerIdentifierCode", ordgCustomer.getOrderingCustIdentifierCode());
            map.put("OrderingCustomerPartyIdentifier", ordgCustomer.getOrderingCustPartyIdentiferAcctValue());
            map.put("OrderingCustomeridentifiercombo", ordgCustomer.getOrderingCustPartyIdentifierAcct());
            map.put("PartyIdentifierCombo", ordgCustomer.getOrderingCustPartyIdentifierCode());
            map.put("PartyIdentifierCountryCode", ordgCustomer.getOrderingCustPartyIdentifierCountry());
            map.put("PartyIdentifierText", ordgCustomer.getOrderingCustPartyIdentiferValue());
            map.put("PartyIdentifierAddress1", ordgCustomer.getOrderingCustDetails().getTextLine1());
            map.put("PartyIdentifierAddress2", ordgCustomer.getOrderingCustDetails().getTextLine2());
            map.put("PartyIdentifierAddress3", ordgCustomer.getOrderingCustDetails().getTextLine3());
            map.put("PartyIdentifierAddress4", ordgCustomer.getOrderingCustDetails().getTextLine4());
        }

        // OrderingInstitution
        OrderingInstitution ordgInstitution = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                .getOrderingInstitution();
        if (null != ordgInstitution) {
            map.put("OrderingInstitueNameAndAddress1", ordgInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl1());
            map.put("OrderingInstitueNameAndAddress2", ordgInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl2());
            map.put("OrderingInstitueNameAndAddress3", ordgInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl3());
            map.put("OrderingInstitueNameAndAddress4", ordgInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl4());
            map.put("OrderingInstitueIdentifierCode", ordgInstitution.getOrderingInstIdentifierCode());
            String orderingInstPartyIdentifierCode = !StringUtils.isBlank(ordgInstitution.getOrderingInstPartyIdentifierCode())
                    ? ordgInstitution.getOrderingInstPartyIdentifierCode()
                    : StringUtils.EMPTY;
            String orderingInstPartyClearingCode = !StringUtils.isBlank(ordgInstitution.getOrderingInstPartyClearingCode())
                    ? ordgInstitution.getOrderingInstPartyClearingCode()
                    : StringUtils.EMPTY;
            // validate NCC codes
            if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)
                    && !orderingInstPartyClearingCode.isEmpty()) {
                rsHeader = helper.validateNCCCodes(orderingInstPartyIdentifierCode, orderingInstPartyClearingCode,
                        MFInputOutPutKeys.ORD_INST_PARTY_PARAMETER, remittanceDto, rsHeader);
                if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR)) {
                    return rsHeader;
                }
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("OrderingInstPartyIdentifierCode ::::" + orderingInstPartyIdentifierCode);
                LOGGER.info("OrderingInstPartyClearingCode :::: " + orderingInstPartyClearingCode);
            }
            // append NCC codes
            map.put("OrderingInstituePartyIdentifier",
                    helper.appendNccCode(orderingInstPartyIdentifierCode, orderingInstPartyClearingCode));
        }
        map.put("isRemittance", true);
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            Map<String, Object> result = MFExecuter.executeMF(PaymentSwiftConstants.UB_SWT_SETTLEMENTINSTRUCTIONFIRSTVALIDATION_SRV,
                    remittanceDto.getEnv(), map);

            if (null != result && !result.get(MFInputOutPutKeys.VALIDATION_STATUS).equals("true")) {
                    LOGGER.error("First  VALIDATION FAILED!!");
                rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_SWIFT_VALIDATION_EVENT_CODE,
                        (String) result.get(MFInputOutPutKeys.ERROR_MESSAGE));
            }
        }
        // validateBeneficiaryCustomerDetails
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            rsHeader = validateBeneficiaryCustomerDetails(swiftRemittanceRq, remittanceDto, rsHeader);
        }
        // ssiNameAndAddressValidation
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            rsHeader = ssiNameAndAddressValidation(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     * @param rsHeader
     */
    private RsHeader validateBeneficiaryCustomerDetails(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateBeneficiaryCustomerDetails");
        try {
            UB_SWT_BeneficiaryCustomerAddressValidation benCustomerValidation = new UB_SWT_BeneficiaryCustomerAddressValidation(
                    remittanceDto.getEnv());
            benCustomerValidation.setF_IN_beneCustomerAddress1(swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine1());
            benCustomerValidation.setF_IN_beneCustomerAddress2(swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine2());
            benCustomerValidation.setF_IN_beneCustomerAddress3(swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine3());
            benCustomerValidation.setF_IN_beneCustomerAddress4(swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine4());
            benCustomerValidation.process(remittanceDto.getEnv());
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

    /**
     * @param swiftRemittanceRq
     */
    private RsHeader ssiNameAndAddressValidation(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN ssiNameAndAddressValidation");
        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("BenCustDesc1", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine1());
            map.put("BenCustDesc2", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine2());
            map.put("BenCustDesc3", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine3());
            map.put("BenCustDesc4", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine4());
            map.put("BenInsIdentifierCode", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode());
            map.put("BenCustIdenCode", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryCustomer().getBeneficiaryCustIdentifierCode());
            map.put("BenInsText1", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine1());
            map.put("BenInsText2", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine2());
            map.put("BenInsText3", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine3());
            map.put("BenInsText4", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                    .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine4());
            map.put("InterDesc1", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                    .getIntermediaryDetails().getTextLine1());
            map.put("InterDesc2", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                    .getIntermediaryDetails().getTextLine2());
            map.put("InterDesc3", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                    .getIntermediaryDetails().getTextLine3());
            map.put("InterDesc4", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                    .getIntermediaryDetails().getTextLine4());
            map.put("IntermediataryIdentifierCode", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getIntermediary().getIntermediaryIdentiferCode());
            map.put("PayToIdentifierCode",
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToIdentifierCode());
            map.put("PayToText1", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                    .getPayToDetails().getPayDtls1());
            map.put("PayToText2", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                    .getPayToDetails().getPayDtls2());
            map.put("PayToText3", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                    .getPayToDetails().getPayDtls3());
            map.put("PayToText4", swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                    .getPayToDetails().getPayDtls4());
            Map<String, Object> result = MFExecuter.executeMF(PaymentSwiftConstants.UB_SWT_SINAMEANDADDRESSVALIDATION_SRV,
                    remittanceDto.getEnv(), map);
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
