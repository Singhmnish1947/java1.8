/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.events.Event;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTSettlementInstructionDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTSettlementInstructions;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_AutoNumberGeneration;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class SWT_AutoNumberGenerationFatom extends AbstractSWT_AutoNumberGeneration {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */


	private static IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory
            .getInstance().getServiceManager().getServiceForName(
                         IBusinessEventsService.SERVICE_NAME);

	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(SWT_AutoNumberGenerationFatom.class.getName());
	public SWT_AutoNumberGenerationFatom(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}


	public void process(BankFusionEnvironment environment) {
		String SettlementWhere = " WHERE " + IBOSWTSettlementInstructions.CUSTOMERCODE + "=? AND "
				+ IBOSWTSettlementInstructions.ISOCURRENCYCODE + "=? AND " + IBOSWTSettlementInstructions.MESSAGETYPE
				+ "=?";
		String Query = "SELECT MAX(" + IBOSWTSettlementInstructionDetail.MESSAGE_NUMBER + ") AS MESSAGENUMBER FROM "
				+ IBOSWTSettlementInstructionDetail.BONAME + " WHERE "
				+ IBOSWTSettlementInstructionDetail.SETTLEMENTINSTRUCTIONSID + "=? AND "
				+ IBOSWTSettlementInstructionDetail.UBSITYPE + "!=?";
		String QueryWhere = " AND " + IBOSWTSettlementInstructionDetail.PAY_RECEIVE_FLAG + "=?";
		ArrayList list = new ArrayList();
		ArrayList params = new ArrayList();
		params.add(getF_IN_custNo());//getF_IN_currencyCode()
		params.add(getF_IN_currencyCode());
		params.add(getF_IN_MessageType());
		int Maxvalue = 1;

		list = (ArrayList) environment.getFactory().findByQuery(IBOSWTSettlementInstructions.BONAME, SettlementWhere,
				params, null, true);
		if (list != null && list.size() > 0) {
			IBOSWTSettlementInstructions SWTSettlementInstructions = (IBOSWTSettlementInstructions) list.get(0);
			params.clear();
			String settId = CommonConstants.EMPTY_STRING;
			params.add(SWTSettlementInstructions.getBoID());
			params.add("A");
			if (getF_IN_MessageType().equalsIgnoreCase("320") || getF_IN_MessageType().equalsIgnoreCase("330")) {
				Query = Query + QueryWhere;
				params.add(getF_IN_payorRecieve());
				list = (ArrayList) environment.getFactory().executeGenericQuery(Query, params, null, true);
			}
			else {
				list = (ArrayList) environment.getFactory().executeGenericQuery(Query, params, null, true);
			}
			if (list != null && list.size() > 0) {
				SimplePersistentObject SWTSettlementInstructionDetail = (SimplePersistentObject) list.get(0);
				Maxvalue = ((Integer) SWTSettlementInstructionDetail.getDataMap().get("MESSAGENUMBER")).intValue() + 1;
			}

		}
		else {
			Maxvalue = 1;
		}
		if (getF_IN_messageNumber().intValue() != -1) {
			if (getF_IN_messageNumber().intValue() > Maxvalue) {
//				String eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409446,
//						environment, new Object[] { new Integer(Maxvalue) });
				//throw new BankFusionException(9446, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_MESSAGE_NUMBER_NOT_EQUAL_OR_GREATER, new Object[] { new Integer(Maxvalue) }, new HashMap(), environment);
				Event validateEvent = new Event();
                validateEvent.setEventNumber(ChannelsEventCodes.E_MESSAGE_NUMBER_NOT_EQUAL_OR_GREATER);
                validateEvent.addMessageArguments(0, new Integer(Maxvalue).toString());
                businessEventsService.handleEvent(validateEvent);

			}
		}
		else
			setF_OUT_messageNumber(new Integer(Maxvalue));
	}
}
