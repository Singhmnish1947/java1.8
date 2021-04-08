/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_BICCodeFilter;
import com.trapedza.bankfusion.steps.refimpl.ISWT_BICCodeFilter;

public class SWT_BICCodeFilter extends AbstractSWT_BICCodeFilter implements ISWT_BICCodeFilter {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public SWT_BICCodeFilter(BankFusionEnvironment env) {
		super(env);
	}

	private transient final static Log logger = LogFactory.getLog(SWT_BICCodeFilter.class.getName());
	public void process(BankFusionEnvironment env) {

		String Biccode = getF_IN_BICCode();
		int result;
		if (Biccode != null) {
			result = Biccode.indexOf("%");
			if (result == -1) {
				setF_OUT_Result("N");
			}
			else {
				setF_OUT_Result("Y");
			}

		}
	}
}
