package com.misys.ub.fatoms.batch.CardTechBalanceDownload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
public class CardTechBalanceDownloadAccumulator extends AbstractProcessAccumulator{

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	
	/**
	 * @param args
	 */
	public CardTechBalanceDownloadAccumulator(Object[] args) {
		super(args);
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#acceptChanges()
	 */
	public void acceptChanges() {
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#accumulateTotals(java.lang.Object[])
	 */
	public void accumulateTotals(Object[] arg0) {
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#addAccumulatorForMerging(com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator)
	 */
	public void addAccumulatorForMerging(AbstractProcessAccumulator accumulator) {

	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#getMergedTotals()
	 */
	public Object[] getMergedTotals() {
	      return null;
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#getProcessWorkerTotals()
	 */
	public Object[] getProcessWorkerTotals() {
        return null;
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#mergeAccumulatedTotals(java.lang.Object[])
	 */
	public void mergeAccumulatedTotals(Object[] arg0) {
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#restoreState()
	 */
	public void restoreState() {
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator#storeState()
	 */
	public void storeState() {
	}

}

