package com.misys.ub.interfaces.opics.steps;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractFE_OPX_Handoff;

public class FE_OpxHandoff extends AbstractFE_OPX_Handoff {
	private static final long serialVersionUID = 6562239959459403705L;

	@SuppressWarnings("deprecation")
	public FE_OpxHandoff(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		String isNostroScheduled = getF_IN_NOSTROSCHEDULED();
		String isPositionScheduled = getF_IN_POSITIONSCHEDULED();
		
		if (isNostroScheduled.equalsIgnoreCase("Y")) {
			NostroUpdateToOpics.processNostro(env);
		}
		if (isPositionScheduled.equalsIgnoreCase("Y")) {
			PositionUpdateToOpics.processPosition(env);
		}
	}
}
