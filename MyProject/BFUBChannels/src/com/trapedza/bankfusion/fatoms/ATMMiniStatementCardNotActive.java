package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import bf.com.misys.cbs.types.AtmMiniStatementDtl;
import bf.com.misys.cbs.types.AtmMiniStatementOutput;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmMiniStatementRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmMiniStatementRs;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMMiniStatementCardNotActive;
import com.trapedza.bankfusion.steps.refimpl.IATMMiniStatementCardNotActive;

public class ATMMiniStatementCardNotActive extends
		AbstractATMMiniStatementCardNotActive implements
		IATMMiniStatementCardNotActive {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2357028461518760206L;
	
	public ATMMiniStatementCardNotActive() {
		super();
	}

	public ATMMiniStatementCardNotActive(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		AtmMiniStatementRq atmMiniStatementRq = getF_IN_atmMiniStatementRq();
		AtmMiniStatementRs atmMiniStatementRs = new AtmMiniStatementRs();
		String errorStatus = getF_IN_errorStatus();
		AtmMiniStatementOutput atmMiniStatementOutput = new AtmMiniStatementOutput();
		AtmMiniStatementDtl atmMiniStatementDtl = new AtmMiniStatementDtl();
		ArrayList <AtmMiniStatementDtl> atmMiniStatementDtls = new ArrayList<AtmMiniStatementDtl>();
		atmMiniStatementDtl.setAtmDetails(atmMiniStatementRq.getAtmMiniStatementInput().getAtmDetails());
		atmMiniStatementDtl.getAtmDetails().setAtmActionCd(errorStatus);
		atmMiniStatementDtls.add(atmMiniStatementDtl);
		atmMiniStatementOutput.setAtmMiniStatementDtl(atmMiniStatementDtls.toArray(new AtmMiniStatementDtl[atmMiniStatementDtls.size()]));
		atmMiniStatementRs.setAtmMiniStatementOutput(atmMiniStatementOutput);
		setF_OUT_atmMiniStatementRs(atmMiniStatementRs);
	}
}
