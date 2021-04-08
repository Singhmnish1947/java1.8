/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */

package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_StringManipulation;
import com.trapedza.bankfusion.steps.refimpl.IBPW_StringManipulation;

/**
 * This class Manipulates the PostName field and Generates two Strings aout of it
 */

public class BPW_StringManipulation extends AbstractBPW_StringManipulation implements IBPW_StringManipulation {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public BPW_StringManipulation(BankFusionEnvironment env) {
		super(env);
	}


	public void process(BankFusionEnvironment env) {
		String strMain = getF_IN_MainPostName();
		String postName1;
		String postName2;
		int len = strMain.length();

		if (strMain.indexOf(":") > 0) {
			postName1 = strMain.substring(0, strMain.indexOf(":"));
			postName2 = strMain.substring(strMain.indexOf(":") + 1, len);
			setF_OUT_PostName1(postName1);
			setF_OUT_PostName2(postName2);
		}
		else {
			setF_OUT_PostName1(strMain);
			setF_OUT_PostName2(" ");
		}

	}
}
