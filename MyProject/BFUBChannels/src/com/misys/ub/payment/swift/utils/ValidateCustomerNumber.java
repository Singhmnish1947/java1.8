package com.misys.ub.payment.swift.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

public class ValidateCustomerNumber implements IValidateBasicCheck {
	/* (non-Javadoc)
	 * @see com.misys.ub.payment.swift.utils.IValidateBasicCheck#validate(bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq)
	 */
	String KYC_SERVICE_MF_NAME = "UB_CNF_ReadKYCStatus_SRV";
	@Override
	public RsHeader validate(OutwardSwtRemittanceRq outwardRq,RsHeader rsHeader) {
		MessageStatus status = rsHeader.getStatus();
		String customerId = outwardRq.getIntlPmtInputRq().getIntlPmtDetails().getRemitterId();
		status = swiftActive(customerId,status);
		rsHeader.setStatus(status);
		return rsHeader;
	}
	
	private MessageStatus swiftActive(String customerId, MessageStatus status)
	{
		if (status.getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
			String whereClause = "where " + IBOSwtCustomerDetail.CUSTOMERCODE
					+ " = ? ";
			ArrayList params1 = new ArrayList();
			params1.add((String) customerId);
			SimplePersistentObject CustomerDetails;
			BankFusionEnvironment env = BankFusionThreadLocal
					.getBankFusionEnvironment();
			Iterator sourceIt = env
					.getFactory()
					.findByQuery(IBOSwtCustomerDetail.BONAME, whereClause,
							params1, null, true).iterator();
			if (sourceIt != null && sourceIt.hasNext()) {
				CustomerDetails = (SimplePersistentObject) sourceIt.next();
				String active = (String) CustomerDetails.getDataMap().get(
						IBOSwtCustomerDetail.SWTACTIVE);
				if (active.equalsIgnoreCase(PaymentSwiftConstants.NO)) {
					SubCode subcode = new SubCode();
					EventParameters vParameters = new EventParameters();
					status.setOverallStatus(PaymentSwiftConstants.ERROR);
					// add parameters
					vParameters.setEventParameterValue(customerId);
					subcode.addParameters(vParameters);
					status.addCodes(PaymentSwiftUtils.addEventCode(
							String.valueOf(ChannelsEventCodes.E_INACTIVE_SWIFT_CUSTOMER),
							subcode));
				}
			}
		}
		return status;
	}
	
}
