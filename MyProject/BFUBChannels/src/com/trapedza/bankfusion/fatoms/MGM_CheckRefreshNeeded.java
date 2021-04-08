package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_CheckRefreshNeeded;

public class MGM_CheckRefreshNeeded extends AbstractMGM_CheckRefreshNeeded {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	String codeTable = CommonConstants.EMPTY_STRING;
	String currentProfile = CommonConstants.EMPTY_STRING;
	String recCountry = CommonConstants.EMPTY_STRING;

	public MGM_CheckRefreshNeeded(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment environment) {
		//11,3,23,13,5,25,15,7,27,17,9,29,19,1,31,21

		String flag = getF_IN_flag();
		if ((flag.equals("11")) || (flag.equals("3")) || (flag.equals("23")) || (flag.equals("13"))
				|| (flag.equals("5")) || (flag.equals("25")) || (flag.equals("15")) || (flag.equals("7"))
				|| (flag.equals("27")) || (flag.equals("17")) || (flag.equals("9")) || (flag.equals("29"))
				|| (flag.equals("19")) || (flag.equals("1")) || (flag.equals("31")) || (flag.equals("21"))) {
			codeTable = "YES";
		}

		if ((flag.equals("22")) || (flag.equals("11")) || (flag.equals("19")) || (flag.equals("2"))
				|| (flag.equals("23")) || (flag.equals("18")) || (flag.equals("26")) || (flag.equals("14"))
				|| (flag.equals("3")) || (flag.equals("10")) || (flag.equals("6")) || (flag.equals("15"))
				|| (flag.equals("7")) || (flag.equals("30")) || (flag.equals("27")) || (flag.equals("31"))) {
			currentProfile = "YES";
		}

		if ((flag.equals("13")) || (flag.equals("20")) || (flag.equals("28")) || (flag.equals("22"))
				|| (flag.equals("23")) || (flag.equals("5")) || (flag.equals("7")) || (flag.equals("14"))
				|| (flag.equals("6")) || (flag.equals("15")) || (flag.equals("12")) || (flag.equals("4"))
				|| (flag.equals("29")) || (flag.equals("30")) || (flag.equals("31")) || (flag.equals("21"))) {
			recCountry = "YES";
		}

		setF_OUT_codeFlag(codeTable);
		setF_OUT_CurrentProfile(currentProfile);
		setF_OUT_recCountryFlag(recCountry);

	}
}
