package com.trapedza.bankfusion.fatoms;

import java.util.Comparator;

import com.trapedza.bankfusion.persistence.core.GenericPersistentObject;

public class UB_IBI_TransCounterComparator implements Comparator {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public static final String TRANSACTION_COUNTER = "TRANSACTIONCOUNTER";
	public static final int CONSTANT_ZERO = 0;
	public static final int CONSTANT_ONE = 1;
	
	public int compare(Object o1, Object o2) {
		
		String counter1 = ((GenericPersistentObject) o1).getDataMap().get(TRANSACTION_COUNTER).toString();
		String counter2 = ((GenericPersistentObject) o2).getDataMap().get(TRANSACTION_COUNTER).toString();
		
		int intCounter1 = Integer.valueOf(counter1);
		int intCounter2 = Integer.valueOf(counter2);
		
		if (intCounter1 < intCounter2)
			return -1;
		else if (intCounter1 > intCounter2)
			return CONSTANT_ONE;
		
		return CONSTANT_ZERO;
	}
}
