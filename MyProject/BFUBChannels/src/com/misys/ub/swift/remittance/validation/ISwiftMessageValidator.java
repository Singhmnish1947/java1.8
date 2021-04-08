/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.validation;

import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.RsHeader;

public interface ISwiftMessageValidator {
		public RsHeader validate(SwiftRemittanceRq swiftRemittanceRq,RemittanceProcessDto remittanceDto);
}
