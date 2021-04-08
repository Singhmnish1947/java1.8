package com.misys.ub.fatoms.batch.bpwFailedTrans;

import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;

public class UB_BPW_FailedTransactionBatchProcessAccumulator extends AbstractProcessAccumulator {

	public UB_BPW_FailedTransactionBatchProcessAccumulator(Object[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void acceptChanges() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void accumulateTotals(Object[] arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void addAccumulatorForMerging(AbstractProcessAccumulator arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public Object[] getMergedTotals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getProcessWorkerTotals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mergeAccumulatedTotals(Object[] arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void restoreState() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void storeState() {
		// TODO Auto-generated method stub		
	}
}
