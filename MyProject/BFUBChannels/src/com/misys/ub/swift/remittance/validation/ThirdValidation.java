package com.misys.ub.swift.remittance.validation;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

public class ThirdValidation {
    private transient final static Log LOGGER = LogFactory.getLog(ThirdValidation.class);

    @SuppressWarnings("unchecked")
    public RsHeader validate(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Inside SWIFT Message Third Validation");

        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
        rsHeader.setStatus(status);
        ValidationHelper helper = new ValidationHelper();
        Map map = Maps.newHashMap();

        map.put(MFInputOutPutKeys.BankToBankInformation1,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo().getBankToBankInfo1());
        map.put(MFInputOutPutKeys.BankToBankInformation2,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo().getBankToBankInfo2());
        map.put(MFInputOutPutKeys.BankToBankInformation3,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo().getBankToBankInfo3());
        map.put(MFInputOutPutKeys.BankToBankInformation4,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo().getBankToBankInfo4());
        map.put(MFInputOutPutKeys.BankToBankInformation5,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo().getBankToBankInfo5());
        map.put(MFInputOutPutKeys.BankToBankInformation6,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo().getBankToBankInfo6());
        map.put(MFInputOutPutKeys.ChargeCode,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode());
        map.put(MFInputOutPutKeys.Generate103Plus,
                Boolean.TRUE.equals(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getGenerate103PlusInd())
                        ? PaymentSwiftConstants.YES
                        : PaymentSwiftConstants.NO);
        map.put(MFInputOutPutKeys.PaymentDetails1,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine1());
        map.put(MFInputOutPutKeys.PaymentDetails2,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine2());
        map.put(MFInputOutPutKeys.PaymentDetails3,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine3());
        map.put(MFInputOutPutKeys.PaymentDetails4,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine4());
        map.put(MFInputOutPutKeys.SenderToReceiverInformation1,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSenderToReceiverInfo().getTextLine1());
        map.put(MFInputOutPutKeys.SenderToReceiverInformation2,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSenderToReceiverInfo().getTextLine2());
        map.put(MFInputOutPutKeys.SenderToReceiverInformation3,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSenderToReceiverInfo().getTextLine3());
        map.put(MFInputOutPutKeys.SenderToReceiverInformation4,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSenderToReceiverInfo().getTextLine4());
        map.put(MFInputOutPutKeys.SenderToReceiverInformation5,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSenderToReceiverInfo().getTextLine5());
        map.put(MFInputOutPutKeys.SenderToReceiverInformation6,
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSenderToReceiverInfo().getTextLine6());
        map.put(MFInputOutPutKeys.TermsAndConditions1, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getTermsAndConditionsInfo().getTAndCInfoLine1());
        map.put(MFInputOutPutKeys.TermsAndConditions2, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getTermsAndConditionsInfo().getTAndCInfoLine2());
        map.put(MFInputOutPutKeys.TermsAndConditions3, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getTermsAndConditionsInfo().getTAndCInfoLine3());
        map.put(MFInputOutPutKeys.TermsAndConditions4, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getTermsAndConditionsInfo().getTAndCInfoLine4());
        map.put(MFInputOutPutKeys.TermsAndConditions5, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getTermsAndConditionsInfo().getTAndCInfoLine5());
        map.put(MFInputOutPutKeys.TermsAndConditions6, swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getTermsAndConditionsInfo().getTAndCInfoLine6());

        Map<String, Object> result = MFExecuter.executeMF(PaymentSwiftConstants.UB_SWT_SETTLEMENTINSTRUCTIONTHIRDVALIDATION_SRV,
                remittanceDto.getEnv(), map);

        if (null != result && !result.get(MFInputOutPutKeys.VALIDATION_STATUS).equals("true")) {
                LOGGER.error("Third  VALIDATION FAILED!!");
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_SWIFT_VALIDATION_EVENT_CODE,
                    (String) result.get(MFInputOutPutKeys.ERROR_MESSAGE));
        }

        return rsHeader;
    }
}
