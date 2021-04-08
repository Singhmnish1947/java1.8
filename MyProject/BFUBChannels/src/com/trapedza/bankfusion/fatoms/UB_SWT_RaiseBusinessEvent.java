/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_RaiseBusinessEvent;

import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

/**
 * Class that raises events for Swift NON STP transactions
 * 
 */
public class UB_SWT_RaiseBusinessEvent extends AbstractUB_SWT_RaiseBusinessEvent {

	private static final long serialVersionUID = 1L;
	private static final transient Log LOGGER = LogFactory.getLog(UB_SWT_RaiseBusinessEvent.class.getName());

	/**
	 * 
	 */
	public UB_SWT_RaiseBusinessEvent() {
		super();
	}

	/**
	 * @param env
	 */
	@SuppressWarnings("deprecation")
	public UB_SWT_RaiseBusinessEvent(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		String UETR = getF_IN_UETR();
		String eventCode = getF_IN_EventCode();
		String hostTxnId = getF_IN_HostTxnId() != null ? getF_IN_HostTxnId() : CommonConstants.EMPTY_STRING;
		UB_SWT_RemittanceProcessRq remDetails = getF_IN_RemittanceDtls();
		PaymentSwiftUtils paymentSwiftUtils = new PaymentSwiftUtils();
		paymentSwiftUtils.raiseEvent(prepareEventMap(UETR, hostTxnId, remDetails, eventCode), eventCode);

	}

	/**
	 * @param UETR
	 * @param hostTxnId
	 * @param remDtls
	 * @param eventCode
	 * @return
	 */
	private Map<String, Object> prepareEventMap(String UETR, String hostTxnId, UB_SWT_RemittanceProcessRq remDtls,
			String eventCode) {
		Map<String, Object> eventMap = new HashMap<>();
		eventMap.put("EventCode", eventCode);
		eventMap.put("HostTransactionId", hostTxnId);
		eventMap.put("OrigChannelId", remDtls.getCHANNELID());
		eventMap.put("ChannelRef", remDtls.getMESSAGENUMBER());
		if (remDtls.getTRANSACTIONDETAISINFO() != null) {
			eventMap.put("PaymentReference", remDtls.getTRANSACTIONDETAISINFO().getTRANSACTIONREFERENCE());
			eventMap.put("PostingDate", remDtls.getTRANSACTIONDETAISINFO().getDATEOFPROCESSING());
		}
		eventMap.put("RemittanceId", remDtls.getREMITTANCEIDPK());
		eventMap.put("UETR", UETR);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Event Payload for SWIFT Remittance Processed:::EventCode::"
					+ (String) eventMap.get("EventCode") + "HostTxnId::" + (String) eventMap.get("HostTransactionId")
					+ " ChannelID::" + (String) eventMap.get("OrigChannelId") + " ChannelRef::"
					+ (String) eventMap.get("ChannelRef") + " PaymentRef::" + (String) eventMap.get("PaymentReference")
					+ " RemittanceId::" + (String) eventMap.get("RemittanceId") + " Uetr::"
					+ (String) eventMap.get("UETR") + " PostingDate::" + (Date) eventMap.get("PostingDate"));
		}

		return eventMap;

	}

}
