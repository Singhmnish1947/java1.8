/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.accountNotes;

import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;

/**
 * ForexDealsPnLAccumulator is able to be merged into one class in order to derive amounts processed
 * by all process classes on their own threads.
 * 
 * 
 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator
 */
public class BranchPowerAccountNotesRefreshAccumulator extends AbstractProcessAccumulator {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     * @param args
     *            All Accumulator classes must first call the constructor on the super class
     *            <code>AbstractProcessAccumulator</code> to set arguments. In addition this
     *            extention sets up BigDecimal arrays for debit and credit amounts with zero.
     */
    public BranchPowerAccountNotesRefreshAccumulator(Object[] args) {
        super(args);
    }

    /**
     * This method accumulates totals for this single instance.
     * 
     * @param data
     *            Holds data to be accumulated
     * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#accumulateTotals(java.lang.Object[])
     */
    public void accumulateTotals(Object[] data) {

    }

    /**
     * This method merges totals for ALL accumulators involved in the Batch run.
     * 
     * @return Merged totals for ALL accumulators involved in the Batch run
     * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#getMergedTotals()
     */
    public Object[] getMergedTotals() {
        return null;
    }

    /**
     * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#getProcessWorkerTotals()
     * @return Object[] o, the totals for this single instance.
     */
    public Object[] getProcessWorkerTotals() {
        return null;
    }

    /**
     * Supporting a form of the Builder Pattern, this method adds other accumulators from the batch
     * run to this one so that merged totals can be gathered.
     * 
     * @param accumulator
     *            The accumulator
     * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#addAccumulatorForMerging(com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator)
     */
    public void addAccumulatorForMerging(AbstractProcessAccumulator accumulator) {

    }

    /**
     * This method processes totals from all accumulators in the batch run. See above method
     * addAccumulatorForMerging() for where it gets the accumulators from.
     * 
     * @param limitRefTotals
     *            Holds the accumulated totals of BatchProcessWorkers
     * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#mergeAccumulatedTotals(java.lang.Object[])
     */
    public void mergeAccumulatedTotals(Object[] accumulatorTotals) {

    }

    public void acceptChanges() {
        // TODO Auto-generated method stub

    }

    public void restoreState() {
        // TODO Auto-generated method stub

    }

    public void storeState() {
        // TODO Auto-generated method stub

    }

}
