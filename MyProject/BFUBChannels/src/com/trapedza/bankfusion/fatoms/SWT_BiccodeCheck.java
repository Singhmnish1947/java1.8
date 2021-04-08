/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_BiccodeCheck;
import com.trapedza.bankfusion.steps.refimpl.ISWT_BiccodeCheck;

public class SWT_BiccodeCheck extends AbstractSWT_BiccodeCheck implements ISWT_BiccodeCheck {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public SWT_BiccodeCheck(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {

		String whereClause = null;
		String Biccode = getF_IN_BICCODE();
		whereClause = "where " + IBOBicCodes.BICCODE + " = ? ";
		ArrayList params = new ArrayList();
		ArrayList list = new ArrayList();

		params.add(Biccode);
		list = (ArrayList) env.getFactory().findByQuery(IBOBicCodes.BONAME, whereClause, params, null);
		if (list.isEmpty()) {

			//throw new BankFusionException(9433, null, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_IDENTIFIERCODE_DOES_NOT_EXIST, new Object[]{}, new HashMap(), env);

		}

	}
}
