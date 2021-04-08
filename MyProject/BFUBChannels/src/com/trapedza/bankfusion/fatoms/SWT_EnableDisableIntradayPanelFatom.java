/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_EnableDisableIntradayPanel;
import com.trapedza.bankfusion.steps.refimpl.ISWT_EnableDisableIntradayPanel;

public class SWT_EnableDisableIntradayPanelFatom extends
		AbstractSWT_EnableDisableIntradayPanel implements
		ISWT_EnableDisableIntradayPanel {
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public SWT_EnableDisableIntradayPanelFatom(BankFusionEnvironment env) {
		super(env);

		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {

		String statementType = getF_IN_statementType();
		if(statementType.equalsIgnoreCase("I")){
			setF_OUT_isIntraday(true);
		}else{
			setF_OUT_isIntraday(false);
		}
	}
}
