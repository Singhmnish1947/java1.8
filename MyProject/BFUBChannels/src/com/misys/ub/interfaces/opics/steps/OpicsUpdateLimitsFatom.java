/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UB_OPX_UpdateLimitsFatom,v.1.0,April 20, 2012 11:35:34 AM Ayyappa
 *
 */
package com.misys.ub.interfaces.opics.steps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_UpdateLimitsFatom;

public class OpicsUpdateLimitsFatom extends AbstractUB_OPX_UpdateLimitsFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final transient Log logger = LogFactory
			.getLog(OpicsUpdateLimitsFatom.class.getName());

	String microflowID = CommonConstants.EMPTY_STRING;

	public OpicsUpdateLimitsFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {

		microflowID = getF_IN_microflowID();

		HashMap inputParams = new HashMap();

		try {
			inputParams.put("AMOUNT", getF_IN_AMOUNT());
			inputParams.put("AMOUNTSIGN", getF_IN_AMOUNTSIGN());
			inputParams.put("CURRENCYCODE", getF_IN_CURRENCYCODE());
			inputParams.put("ChannelID", getF_IN_ChannelID());
			inputParams.put("CUSTOMERCODE", getF_IN_CUSTOMERCODE());

			HashMap outputParams = MFExecuter.executeMF(microflowID, env,
					inputParams);

			setF_OUT_ProcessStatus((Boolean) outputParams.get("ProcessStatus"));
			setF_OUT_ProcessStatusMessage((String) outputParams
					.get("ProcessStatusMessage"));

		} catch (Exception e) {
			logger.error(">>>>>>>>>>>" + Thread.currentThread().getName()
					+ ": " + " Error while executing. "
					+ ExceptionUtil.getExceptionAsString(e));

			int eventNumber = 0;

			if (e instanceof BankFusionException) {
				Collection<IEvent> events = ((BankFusionException) e)
						.getEvents();
				Iterator<IEvent> itr = events.iterator();
				while (itr.hasNext()) {
					IEvent e1 = itr.next();
					eventNumber = e1.getEventNumber();
					break;
				}

			}

			setF_OUT_ProcessStatus(Boolean.valueOf(CommonConstants.FALSE));
			setF_OUT_ProcessStatusMessage(String.valueOf(eventNumber));

		}

		finally {

		}

	}
}
