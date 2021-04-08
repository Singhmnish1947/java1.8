package com.misys.ub.payment.swift.utils;

import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class ValidateCurrencyCode implements IValidateBasicCheck {
	/* (non-Javadoc)
	 * @see com.misys.ub.payment.swift.utils.IValidateBasicCheck#validate(bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq)
	 */
	@Override
	public RsHeader validate(OutwardSwtRemittanceRq outwardRq,RsHeader rsHeader) {
		String currencyCode = outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode();
        IBOCurrency CurrencyDetails = CurrencyUtil.getCurrencyDetailsOfCurrentZone(currencyCode);
		MessageStatus status = rsHeader.getStatus();
		SubCode subcode = new SubCode();
		EventParameters vParameters = new EventParameters();
        if (null != CurrencyDetails) {
            boolean Active = CurrencyDetails.isF_ISACTIVE();
			if (!Active && status.getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
				status.setOverallStatus(PaymentSwiftConstants.ERROR);
				vParameters.setEventParameterValue(currencyCode);
				subcode.addParameters(vParameters);
				status.addCodes(PaymentSwiftUtils.addEventCode(String.valueOf(ChannelsEventCodes.E_INACTIVE_CURRENCY),subcode));
			}
		}
		else {
			status.setOverallStatus(PaymentSwiftConstants.ERROR);
			vParameters.setEventParameterValue(currencyCode);
			subcode.addParameters(vParameters);
			status.addCodes(PaymentSwiftUtils.addEventCode(String.valueOf(ChannelsEventCodes.E_INVALID_CURRENCY_CODE),subcode));
		}
		rsHeader.setStatus(status);
		return rsHeader;
	}
}
