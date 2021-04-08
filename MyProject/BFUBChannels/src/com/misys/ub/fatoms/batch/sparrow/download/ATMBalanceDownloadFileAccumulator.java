/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * $Id: ATMBalanceDownloadFileAccumulator.java,v 1.1 2008/11/26 09:00:40 bhavyag Exp $
 *
 * $Log: ATMBalanceDownloadFileAccumulator.java,v $
 * Revision 1.1  2008/11/26 09:00:40  bhavyag
 * merging 3-3B changes for bug 12581.
 *
 * Revision 1.1.4.2  2008/09/23 08:09:50  mangesh
 * BUGID - 12581 - new Batch process for processing ATM Balance Download.
 *
 *
 *
 */
package com.misys.ub.fatoms.batch.sparrow.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.misys.cbs.common.util.log.CBSLogger;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;

/**
 * Accumulator to add up number of records processed by each batch worker.
 * @author Mangesh Hagargi
 *
 */
public class ATMBalanceDownloadFileAccumulator extends AbstractProcessAccumulator {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */
	
	private static final String CLASS_NAME = ATMBalanceDownloadFileAccumulator.class.getName();
	private static final transient CBSLogger logger = new CBSLogger(CLASS_NAME);
	
    private List merged  = new ArrayList();
    
    private Map collectionTable = java.util.Collections.synchronizedMap(new HashMap());
    
    private Map mergedCollectionTable = java.util.Collections.synchronizedMap(new HashMap());
    
    private static final String TOTAL_RECORD = "TotalRecord";
    
   	public ATMBalanceDownloadFileAccumulator(Object[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void acceptChanges() {
		// TODO Auto-generated method stub

	}

	@Override
	public void accumulateTotals(Object[] data) {
		
       if (collectionTable.containsKey(TOTAL_RECORD)){
    	   Integer mergeTotal = (Integer)collectionTable.get(TOTAL_RECORD);
           if (mergeTotal == null){
        	   mergeTotal = new Integer(0);
           }
    	   Integer total = (Integer) data[0];
           if (total == null){
        	   total = new Integer(0);
           }
    	   mergeTotal = mergeTotal + total;
    	  
    		   StringBuffer sbuff = new StringBuffer();
    		   sbuff.append("Accumulated Total Record ");
    		   sbuff.append(mergeTotal);
    		   sbuff.append("Process Total ");
    		   sbuff.append(total);
    		      		   
    	   collectionTable.put(TOTAL_RECORD, mergeTotal);
       }else{
    	   Integer total = (Integer) data[0];
    	   if (logger.isInfoEnabled())
    	   {
    		   logger.debug("accumulateTotals()", "Accumulated Total:"+total);
    	   }
           if (total == null){
        	   total = new Integer(0);
           }
    	   collectionTable.put(TOTAL_RECORD, total);
       }
       
	}

	@Override
	public void addAccumulatorForMerging(AbstractProcessAccumulator accumulator) {
	      if (merged == null)
	            merged = new ArrayList();
	        merged.add(accumulator);

	}

	@Override
	public Object[] getMergedTotals() {
		
		Object[] processWorkerTotals = null;
		Object[] returnData = new Object[1];
		
		Iterator iterator = merged.iterator();
		mergedCollectionTable.clear();
		while (iterator.hasNext()) {
			AbstractProcessAccumulator accumulator = (AbstractProcessAccumulator) iterator.next();
			processWorkerTotals = accumulator.getProcessWorkerTotals();
			mergeAccumulatedTotals(processWorkerTotals);
		}
		// Don't forget to add this one's totals as well.
		mergeAccumulatedTotals(getProcessWorkerTotals());
		returnData[0] = mergedCollectionTable;
		
		return returnData;

	}

	@Override
	public Object[] getProcessWorkerTotals() {
		
	        Object[] processWorkerTotals = new Object[1];
	        processWorkerTotals[0] = collectionTable;
        
	    return processWorkerTotals;
	}

	@Override
	public void mergeAccumulatedTotals(Object[] accumulatorTotals) {
		
		Map accumulatorCollectionTable = (Map) accumulatorTotals[0];
		if (mergedCollectionTable.containsKey(TOTAL_RECORD)){
		 	   Integer mergeTotal = (Integer)mergedCollectionTable.get(TOTAL_RECORD);
	    	   Integer total = (Integer) accumulatorCollectionTable.get(TOTAL_RECORD);
	    	   mergeTotal = mergeTotal + total;
	    	   mergedCollectionTable.put(TOTAL_RECORD, mergeTotal);
	   		
		}else{
			mergedCollectionTable.put(TOTAL_RECORD, accumulatorCollectionTable.get(TOTAL_RECORD));
		}
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
