/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.fatoms.UB_SWT_ValidateBankToBankInfo;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.ub.types.remittanceprocess.BANKTOBANKINFO;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

/**
 * @author hargupta
 *
 */

public class MT103Validator implements ISwiftMessageValidator {
    private transient final static Log LOGGER = LogFactory.getLog(MT103Validator.class);
    ValidationHelper helper = new ValidationHelper();

    /*
     * (non-Javadoc)
     * 
     * @see com.misys.ub.swift.remittance.validation.ISwiftMessageValidator#validate(bf.
     * com.misys.cbs.msgs.v1r0.SwiftRemittanceRq)
     */
    @Override
    public RsHeader validate(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("START MT103Validator");
        RsHeader rsHeader = new RsHeader();

        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
        rsHeader.setStatus(status);

        // validateOrderingCustomerAndInstitutionDetails
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateOrderingCustomerAndInstitutionDetails(swiftRemittanceRq, rsHeader);
        }
        // validateRemittanceDetails
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateRemittanceDetails(swiftRemittanceRq, rsHeader);
        }
        // validateBankToBankInfo
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateBankToBankInfo(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        // validateRemittance103Plus
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)
                && swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().isGenerate103PlusInd()) {
            rsHeader = validateRemittance103Plus(swiftRemittanceRq, rsHeader);
        }

        if (LOGGER.isInfoEnabled())
            LOGGER.info("End MT103Validator");
        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     * @return
     */
    public RsHeader validateRemittanceDetails(SwiftRemittanceRq swiftRemittanceRq, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateRemittanceDetails");

        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Charge Code ");
        }

        // if charge code type BEN and charge detail is zero raise error
        String chargeCodeType = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode();
        BigDecimal chargeDetailAmt = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails()
                .getAmount();
        if (chargeCodeType.equals(PaymentSwiftConstants.CHARGE_CODE_BEN) && chargeDetailAmt.compareTo(BigDecimal.ZERO) <= 0) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_SWIFT_CHARGE_MANDTRY_WITH_BEN, StringUtils.EMPTY);
        }

        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo()
                .getBankOperationCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Bank OperationCode ");
        }
        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     * @return
     */
    public RsHeader validateOrderingCustomerAndInstitutionDetails(SwiftRemittanceRq swiftRemittanceRq, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateOrderingCustomerAndInstitutionDetails");

        if (StringUtils
                .isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                        .getBeneficiaryCustomer().getBeneficiaryCustIdentifierCode())
                && StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                        .getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine1())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Beneficiary Customer Details");

        }

        if (StringUtils
                .isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution().getOrderingCustomer()
                        .getOrderingCustIdentifierCode())
                && StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                        .getOrderingCustomer().getOrderingCustDetails().getTextLine1())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Ordering Customer Details");
        }

        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     */
    private RsHeader validateBankToBankInfo(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateBankToBankInfo");

        try {
            UB_SWT_ValidateBankToBankInfo validatebankToBankInfo = new UB_SWT_ValidateBankToBankInfo();
            UB_SWT_RemittanceProcessRq remittanceProcessRq = new UB_SWT_RemittanceProcessRq();
            BANKTOBANKINFO bankToBankInfo = new BANKTOBANKINFO();
            bankToBankInfo.setBANKINSTRUCTIONCODE(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankInstructionCode());
            bankToBankInfo.setBANKINSTRUCTIONCODE2(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankAddlInstrCode());
            bankToBankInfo.setBANKOPERATIONCODE(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankOperationCode());
            bankToBankInfo.setBANKTOBANKINFO1(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo1());
            bankToBankInfo.setBANKTOBANKINFO2(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo2());
            bankToBankInfo.setBANKTOBANKINFO3(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo3());
            bankToBankInfo.setBANKTOBANKINFO4(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo4());
            bankToBankInfo.setBANKTOBANKINFO5(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo5());
            bankToBankInfo.setBANKTOBANKINFO6(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo6());
            remittanceProcessRq.setBANKTOBANKINFO(bankToBankInfo);
            validatebankToBankInfo.setF_IN_remittanceProcessRq(remittanceProcessRq);
            validatebankToBankInfo.process(BankFusionThreadLocal.getBankFusionEnvironment());
        }
        catch (BankFusionException collectedEventexc) {
            SubCode subCode = new SubCode();
            MessageStatus status = new MessageStatus();
            subCode.setCode(String.valueOf(collectedEventexc.getEvents().iterator().next().getEventNumber()));
            Object param;
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
    public RsHeader validateRemittance103Plus(SwiftRemittanceRq swiftRemittanceRq, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateRemittanceDetails");
        String instructionCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo()
                .getBankInstructionCode();
        String senderToReceiverInfo = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo()
                .getBankToBankInfo1();
        if (!StringUtils.isBlank(instructionCode)
                && !(instructionCode.equalsIgnoreCase("CORT") || instructionCode.equalsIgnoreCase("INTC")
                        || instructionCode.equalsIgnoreCase("SDVA") || instructionCode.equalsIgnoreCase("REPA"))) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_SWIFT_REMITTANCE_5, instructionCode);
        }
        if (StringUtils.containsIgnoreCase(senderToReceiverInfo, "/REJT/")
                || StringUtils.containsIgnoreCase(senderToReceiverInfo, "/RETN/")) {
                rsHeader = helper.setErrorResponse(SwiftEventCodes.E_SWIFT_REMITTANCE_6, senderToReceiverInfo);
            }
        
        if(StringUtils.startsWithIgnoreCase(senderToReceiverInfo, "/INS/"))
        {
            String enteredBicCode = senderToReceiverInfo.substring(5);
            enteredBicCode = (!StringUtils.isBlank(enteredBicCode) && enteredBicCode.length() == 8)?(enteredBicCode+"XXX"):enteredBicCode;
            HashMap<String, Object> map = new HashMap<>();
            map.put("IdentifierCode", enteredBicCode);
            HashMap output = MFExecuter.executeMF("UB_SWT_IdentifierCodeRead_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), map);
            int noOfRows = (int) output.get("NOOFROWSGENERAL");
            boolean isDeleted = "Y".equalsIgnoreCase((String) output.get("IsDeleted"))?true:false;
            if(CommonConstants.INTEGER_ZERO == noOfRows || isDeleted)
            {
                rsHeader = helper.setErrorResponse(SwiftEventCodes.E_SWIFT_REMITTANCE_6, senderToReceiverInfo);
            }
        }
        return rsHeader;

    }

}