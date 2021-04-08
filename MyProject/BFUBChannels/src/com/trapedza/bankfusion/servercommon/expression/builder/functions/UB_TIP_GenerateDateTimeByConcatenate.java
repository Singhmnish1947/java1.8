package com.trapedza.bankfusion.servercommon.expression.builder.functions;

/* ***********************************************************************************
 * Copyright (c) 2003,2008 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Trapedza Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 */

	
	import java.sql.Timestamp;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
	/**
	 * @description This Function will expose Sting RemoveSpaceFromString method to BPD.
	 * @author Raja.rajan
	 */
	public class UB_TIP_GenerateDateTimeByConcatenate {

		/**
		 * <code>svnRevision</code> = $Revision: 1.0 $
		 */
		public static final String svnRevision = "$Revision: 1.0 $";
		static {
			com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
		}

		 /**
	     */
	    /**
	     * This method will remove the white spaces from the String inputted, and return.
	     * 
	     * @param String with spaces
	     * @return String without spaces
	     */
	    public static Timestamp run(Timestamp timeStampArg1,Timestamp timeStampArg2) {
	    	int date=0;
	    	int month=0;
	    	int year=0;
	    	int hour=0;
	    	int min=0;
	    	int sec=0;
	    	date = timeStampArg1.getDate();
	    	month = timeStampArg1.getMonth();
	    	year = timeStampArg1.getYear();
	    	hour = timeStampArg2.getHours();
	    	min = timeStampArg2.getMinutes();
	    	sec = timeStampArg2.getSeconds();
        Timestamp resultTimeStamp = SystemInformationManager.getInstance().getBFSystemDateTime();
	    	resultTimeStamp.setDate(date);
	    	resultTimeStamp.setMonth(month);
	    	resultTimeStamp.setYear(year);
	    	resultTimeStamp.setHours(hour);
	    	resultTimeStamp.setMinutes(min);
	    	resultTimeStamp.setSeconds(sec);
	    	return resultTimeStamp;
	    }
	}
