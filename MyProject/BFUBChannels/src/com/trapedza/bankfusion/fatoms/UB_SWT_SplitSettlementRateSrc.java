package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_SplitSettlementRateSrc;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

public class UB_SWT_SplitSettlementRateSrc extends
		AbstractUB_SWT_SplitSettlementRateSrc {

	private static final long serialVersionUID = 8990000889607219615L;
	private static final String FORWARD_SLASH = "/";
	private static final String DOLLER = "\\$";

	public UB_SWT_SplitSettlementRateSrc(BankFusionEnvironment env) {
		super(env);
	}

	/*
	 * Split Settlement Rate Source to it's three parts, if available separated
	 * by "/"
	 */
	public void process(BankFusionEnvironment env) {
		String input = getF_IN_input();
		String[] SettlementRateSource = input.split(DOLLER);
		
		if (SettlementRateSource.length<=0 || null == SettlementRateSource[0] || SettlementRateSource[0].isEmpty()) {
			return;
		}
		if (!SettlementRateSource[0].contains(FORWARD_SLASH)) {
			setF_OUT_string_1(SettlementRateSource[0]);
		} else {
			String[] inputArray = SettlementRateSource[0].split(FORWARD_SLASH);
			setF_OUT_string_1(inputArray[0]);
			setF_OUT_string_2(FORWARD_SLASH + inputArray[1]);
			setF_OUT_string_3(FORWARD_SLASH + inputArray[2]);
		}
		
		if(SettlementRateSource.length > 1)
		{
			if (null == SettlementRateSource[1] || SettlementRateSource[1].isEmpty()) {
				return;
			}
			if (!SettlementRateSource[1].contains(FORWARD_SLASH)) {
				setF_OUT_string_4(SettlementRateSource[1]);
			} else {
				String[] inputArray = SettlementRateSource[1].split(FORWARD_SLASH);
				setF_OUT_string_4(inputArray[0]);
				setF_OUT_string_5(FORWARD_SLASH + inputArray[1]);
				setF_OUT_string_6(FORWARD_SLASH + inputArray[2]);
			}
		}
	}
}
