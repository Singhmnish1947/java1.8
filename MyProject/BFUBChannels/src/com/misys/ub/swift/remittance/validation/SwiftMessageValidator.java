/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.validation;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.RsHeader;

/**
 * @author hargupta
 *
 */

public class SwiftMessageValidator implements Command {

    private transient final static Log LOGGER = LogFactory.getLog(SwiftMessageValidator.class);
    private transient final ComplexTypeConvertor complexConverter = new ComplexTypeConvertor(this.getClass().getClassLoader());

    @Override
    public boolean execute(Context context) throws Exception {
        boolean endOfChain = false;
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN SwiftMessageValidator");

        SwiftRemittanceRq swiftRemittanceRq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        CommonSwiftMessageValidator commonSwiftMessageValidator = new CommonSwiftMessageValidator();
        if (LOGGER.isDebugEnabled()) {
            String outRsString = complexConverter.getXmlFromJava(swiftRemittanceRq.getClass().getName(), swiftRemittanceRq);
            LOGGER.debug("In Validation Stage::::::: " + outRsString);
        }

        RsHeader rsHeader = commonSwiftMessageValidator.validateSwiftRemittanceRquest(swiftRemittanceRq, remittanceDto);
        // first Validation
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            FirstValidation fistValidation = new FirstValidation();
            rsHeader = fistValidation.validate(swiftRemittanceRq, remittanceDto);
        }
        // second validation
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            SecondValidation secondValidation = new SecondValidation();
            rsHeader = secondValidation.validate(swiftRemittanceRq, remittanceDto);
        }
        // third validation
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            ThirdValidation thirdValidation = new ThirdValidation();
            rsHeader = thirdValidation.validate(swiftRemittanceRq, remittanceDto);
        }

        // validate by Message Type like 103,202
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateSwiftMessageDetailsByTransactionType(swiftRemittanceRq, remittanceDto);
        }

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
            endOfChain = Boolean.TRUE;
        }

        swtRemitterResp.setRsHeader(rsHeader);
        context.put("swtRemitterResp", swtRemitterResp);
        
        if (LOGGER.isInfoEnabled())
            LOGGER.info("END of  SwiftMessageValidator");

        return endOfChain;
    }

    /**
     * @param swiftRemittanceRq
     * @return
     */
    public RsHeader validateSwiftMessageDetailsByTransactionType(SwiftRemittanceRq swiftRemittanceRq,
            RemittanceProcessDto remittanceDto) {

        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN validateSwiftMessageDetailsByTransactionType");

        SwiftMessageValidatorFactory swiftMessageFactory = new SwiftMessageValidatorFactory();
        ISwiftMessageValidator swISwiftMessageValidator = swiftMessageFactory.validateSwiftMessage(swiftRemittanceRq);
        RsHeader rsHeader = swISwiftMessageValidator.validate(swiftRemittanceRq, remittanceDto);
        return rsHeader;

    }
}
