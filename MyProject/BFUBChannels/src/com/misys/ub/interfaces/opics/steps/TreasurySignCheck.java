package com.misys.ub.interfaces.opics.steps;

import java.util.Map;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.microflow.ActivityStep;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractTreasurySignCheck;

public class TreasurySignCheck extends AbstractTreasurySignCheck {

	/**
	 * Fatom to decide sign of amount for intraday accounting entries coming from
	 * Opics
	 */
	private static final long serialVersionUID = -4385877029356396309L;
	private String flag = null;

	public TreasurySignCheck(BankFusionEnvironment env) {
		super(env);
	}

	@SuppressWarnings("unchecked")
	public void process(BankFusionEnvironment env) {
		
		String swiftMD = getF_IN_SWIFTMD();
		String payRecInd = getF_IN_PayRecieveindicator();
		if (swiftMD.equalsIgnoreCase("100D") && payRecInd.equalsIgnoreCase("P"))
			flag = "C";
		else if (swiftMD.equalsIgnoreCase("100D") && payRecInd.equalsIgnoreCase("R"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("202") && payRecInd.equalsIgnoreCase("P"))
			flag = "C";
		else if (swiftMD.equalsIgnoreCase("202") && payRecInd.equalsIgnoreCase("R"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("202D") && payRecInd.equalsIgnoreCase("P"))
			flag = "C";
		else if (swiftMD.equalsIgnoreCase("202D") && payRecInd.equalsIgnoreCase("R"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("210") && payRecInd.equalsIgnoreCase("R"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("521") && payRecInd.equalsIgnoreCase("R"))
			flag = "C";
		else if (swiftMD.equalsIgnoreCase("523") && payRecInd.equalsIgnoreCase("D"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("192D") && payRecInd.equalsIgnoreCase("P"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("192D") && payRecInd.equalsIgnoreCase("R"))
			flag = "C";
		else if (swiftMD.equalsIgnoreCase("292") && payRecInd.equalsIgnoreCase("P"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("292") && payRecInd.equalsIgnoreCase("R"))
			flag = "C";
		else if (swiftMD.equalsIgnoreCase("592") && payRecInd.equalsIgnoreCase("R"))
			flag = "D";
		else if (swiftMD.equalsIgnoreCase("592") && payRecInd.equalsIgnoreCase("D"))
			flag = "C";

		setF_OUT_DrCrFlag(flag);
	}
}
