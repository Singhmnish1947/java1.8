package com.misys.ub.fatoms;

import java.util.Map;

import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_GetATMTxnCodeDetails;



public class UB_ATM_GetATMTxnCodeDetails extends AbstractUB_ATM_GetATMTxnCodeDetails {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5425448640136847736L;
	/**
	 * 
	 * @param env
	 */
	
	public static final String UBTRANSCODE_KEY = "UBTRANSCODE";
	public static final String ATMTRANSACTIONCODE_KEY = "ATMTRANSACTIONCODE";
	public static final String DESCRIPTION_KEY = "DESCRIPTION";
	public static final String LEVELOFSUPPORT_KEY = "LEVELOFSUPPORT";
	public static final String NARRATIVE_KEY = "NARRATIVE";
	public static final String NARRATIVE_GENERATOR_KEY = "NARRATIVE_GENERATOR";
	public static final String UBATMTRANSACTIONTYPE_KEY = "UBATMTRANSACTIONTYPE";
	
	public UB_ATM_GetATMTxnCodeDetails(BankFusionEnvironment env) {
		super(env);
	}
	
	ATMHelper atmHelper = new ATMHelper();
	public void process(BankFusionEnvironment env){
		String atmTxnCode = getF_IN_ATMTransCode();
		String atmTransType= getF_IN_AtmTransType();
		
		Map atmTxnCodeDetails = atmHelper.getATMTransactionCodeDetails(atmTxnCode, atmTransType, env);
		setF_OUT_ATMTRANSACTIONCODE((String) atmTxnCodeDetails.get(ATMTRANSACTIONCODE_KEY));
		setF_OUT_DESCRIPTION((String) atmTxnCodeDetails.get(DESCRIPTION_KEY));
		setF_OUT_LEVELOFSUPPORT((Integer) atmTxnCodeDetails.get(LEVELOFSUPPORT_KEY));
		setF_OUT_NARRATIVE((String) atmTxnCodeDetails.get(NARRATIVE_KEY));
		setF_OUT_NARRATIVE_GENERATOR((String) atmTxnCodeDetails.get(NARRATIVE_GENERATOR_KEY));
		setF_OUT_UBATMTRANSACTIONTYPE((String) atmTxnCodeDetails.get(UBATMTRANSACTIONTYPE_KEY));
		setF_OUT_UBTRANSCODE((String) atmTxnCodeDetails.get(UBTRANSCODE_KEY));
		
	}
}
