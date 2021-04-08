/* ***********************************************************************************
 * Copyright (c) 2012 Misys Solutions Services Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Log: BPRefreshConstants.java,v $
 * Revision 1.1.2.3  2012/11/27 00:26:20  Vipul.Sharma
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.stopCheque;

import com.trapedza.bankfusion.batch.process.properties.BatchPropertyLoader;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;

/**
 * @author Vipul.Sharma
 * @date 28 Nov 2012
 * @project Universal Banking
 * @Description This class file is used to load the number of pages property for Stpo Cheque Batch
 *              Process
 * 
 */
public class BranchPowerStopChequePropertyLoader extends BatchPropertyLoader {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    
    public long getNumberOfPages(IPersistenceObjectsFactory factory) {
        // This should always return 1
        return 1;

    }
}
