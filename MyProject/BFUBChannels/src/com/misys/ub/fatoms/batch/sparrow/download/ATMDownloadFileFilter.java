/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * $Id: ATMDownloadFileFilter.java,v 1.1 2008/11/26 09:00:40 bhavyag Exp $
 *
 * $Log: ATMDownloadFileFilter.java,v $
 * Revision 1.1  2008/11/26 09:00:40  bhavyag
 * merging 3-3B changes for bug 12581.
 *
 * Revision 1.1.4.2  2008/09/23 08:09:50  mangesh
 * BUGID - 12581 - new Batch process for processing ATM Balance Download.
 *
 *
 */
package com.misys.ub.fatoms.batch.sparrow.download;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This class is used for listing out the ATM download File which will be created
 * as part of ATM download process.
 * 
 * @author mangesh Hagargi
 *
 */
public class ATMDownloadFileFilter implements FilenameFilter {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */
    private static final String fileExt = ".dwn";
	/**
	 * 
	 */
	public ATMDownloadFileFilter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String name) {
		// TODO Auto-generated method stub
		return (name.endsWith(fileExt));
	}

}
