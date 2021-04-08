/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.validation;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

/**
 * @author hargupta
 *
 */
public class MT202Validator implements ISwiftMessageValidator {
    private transient final static Log LOGGER = LogFactory.getLog(MT202Validator.class);
    private ValidationHelper helper = new ValidationHelper();

    @Override
    public RsHeader validate(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
        rsHeader.setStatus(status);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("START MT202Validator");

        rsHeader=validateBeneficiaryInstitution(swiftRemittanceRq, rsHeader);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("End MT202Validator");

        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     * @return
     */
    public RsHeader validateBeneficiaryInstitution(SwiftRemittanceRq swiftRemittanceRq, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateBeneficiaryInstitution");

        if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryInstitution()) {
            if (StringUtils
                    .isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                            .getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode())
                    && (StringUtils
                            .isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                                    .getBeneficiaryInstitution().getBeneficiaryInstPartyIdentifier())
                            || StringUtils.isEmpty(
                                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                                            .getBeneficiaryInstitution().getBeneficiaryInstPartyClearingCode()))) {
                rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Beneficiary Institution " );
            }
        }
        else {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Beneficiary Institution ");
        }
        return rsHeader;

    }

}