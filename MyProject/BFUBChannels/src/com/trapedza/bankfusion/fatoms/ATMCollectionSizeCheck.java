package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_IsCollectionSizeZero;
import com.trapedza.bankfusion.steps.refimpl.IUB_ATM_IsCollectionSizeZero;

public class ATMCollectionSizeCheck extends AbstractUB_ATM_IsCollectionSizeZero implements IUB_ATM_IsCollectionSizeZero{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ATMCollectionSizeCheck() {
		super();
	}

	public ATMCollectionSizeCheck(BankFusionEnvironment env) {
		super(env);
	}
	@Override
	public void process (BankFusionEnvironment env) throws BankFusionException {
	
		VectorTable TransactionDetail= getF_IN_TransactionDetail();
		if(TransactionDetail.size() == 0)
		{
			setF_OUT_isEtmpy(true);
			setF_OUT_isNotEtmpy(false);
		} else {
			setF_OUT_isEtmpy(false);
			setF_OUT_isNotEtmpy(true);
		}
	}
	
}
