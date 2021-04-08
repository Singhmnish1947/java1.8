/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.fatoms;

import java.util.HashMap;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_RaisePositionUpdateEvent;

public class UB_CMN_RaisePositionUpdateEvent extends AbstractUB_CMN_RaisePositionUpdateEvent {

	public UB_CMN_RaisePositionUpdateEvent() {
		super();
	}
	
	public UB_CMN_RaisePositionUpdateEvent(BankFusionEnvironment env) {
		super(env);
	}
	
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		String 					transactionId 			= getF_IN_transactionSrId();
		HashMap<String, Object> paramsForAcctAmendEvent = new HashMap<String, Object>();
        
		paramsForAcctAmendEvent.put("txnBO", transactionId);
		
		EventsHelper.handleEvent(	ChannelsEventCodes.I_TRANSACTION_CURRENCY_POSITION
								,	new Object[] {}
								,	paramsForAcctAmendEvent
								,	BankFusionThreadLocal.getBankFusionEnvironment()
								);
	}
}
