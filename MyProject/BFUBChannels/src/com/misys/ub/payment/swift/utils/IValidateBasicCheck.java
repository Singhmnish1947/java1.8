package com.misys.ub.payment.swift.utils;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.header.RsHeader;


public interface IValidateBasicCheck {
	
	/**
	 * @param outwardRq
	 * @return
	 */
	public RsHeader validate(OutwardSwtRemittanceRq outwardRq,RsHeader rsHeader);

}
