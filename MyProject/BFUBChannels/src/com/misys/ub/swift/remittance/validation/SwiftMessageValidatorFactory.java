/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;

/**
 * @author hargupta
 *
 */
public class SwiftMessageValidatorFactory {

    private transient final static Log LOGGER = LogFactory.getLog(SwiftMessageValidatorFactory.class);

    /**
     * @param swiftRemittanceRq
     * @return
     */
    public ISwiftMessageValidator validateSwiftMessage(SwiftRemittanceRq swiftRemittanceRq) {
        ISwiftMessageValidator swiftMessageValidator;

        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateSwiftMessage MessageType:::::"
                    + swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType());

        switch (swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType()) {
            case PaymentSwiftConstants.MT103:
                swiftMessageValidator = new MT103Validator();
                break;
            case PaymentSwiftConstants.MT202:
                swiftMessageValidator =  new MT202Validator();
                break;
            default:
                swiftMessageValidator = null;
                break;
        }
        return swiftMessageValidator;
    }
}
