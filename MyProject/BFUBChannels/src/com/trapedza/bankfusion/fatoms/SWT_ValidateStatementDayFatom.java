/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import bf.com.misys.cbs.msgs.v1r0.CreateAccountHoldRq;
import bf.com.misys.cbs.msgs.v1r0.CreateAccountHoldRs;
import bf.com.misys.cbs.services.IdGenerationRq;
import bf.com.misys.cbs.services.IdGenerationRs;
import bf.com.misys.cbs.types.AccountHoldBasicDtls;
import bf.com.misys.cbs.types.AccountHoldDetails;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.IdGenBusData;
import bf.com.misys.cbs.types.IdGenControl;
import bf.com.misys.cbs.types.IdGenerationInput;
import bf.com.misys.cbs.types.InputAccount;

import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateStatementDayActivityStep;
import com.trapedza.bankfusion.steps.refimpl.ISWT_ValidateStatementDayActivityStep;

public class SWT_ValidateStatementDayFatom extends AbstractSWT_ValidateStatementDayActivityStep implements
		ISWT_ValidateStatementDayActivityStep {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	
	private static final String EMPTY_STRING = "";
	
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public SWT_ValidateStatementDayFatom(BankFusionEnvironment env) {
		super(env);

		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {
		UB_SWT_Util util = new UB_SWT_Util();
       
        String[] err = new String[6];
        String acntNumber = getF_IN_accountNumber();
		String frequency = getF_IN_Frequency();
		Integer numofrecords = getF_IN_NumofRecords();
		int numOfStmnts = getF_IN_NumberOfStatements();
		int NumOfRecords = numofrecords.intValue();
		int Statement = Integer.parseInt(getF_IN_StatementDay());
		setF_OUT_lastStatementDate(new Timestamp(SystemInformationManager.getInstance().getBFBusinessDate().getTime()));
		String time = getF_IN_StartTime();
        int hours = 0, minutes = 0;
        if(frequency.equals("I") && !time.equalsIgnoreCase(CommonConstants.EMPTY_STRING)){
        	hours = Integer.parseInt(time.substring(0, 2));
            minutes = Integer.parseInt(time.substring(3, 5));
        }
        Timestamp dtTm = (new Timestamp(getF_IN_nextStmtDate().getTime()));
        dtTm.setHours(hours);
        dtTm.setMinutes(minutes);
        setF_OUT_NextStatementDate(dtTm);
        int total = getF_IN_Interval()*getF_IN_NumberOfStatements();
        int hrs= total/60;
        int mnts = total%60;
        hrs = ((hrs+hours)+((minutes+mnts)/60));
        if(getF_IN_nextStmtDate().before(SystemInformationManager.getInstance().getBFBusinessDate())
        		|| getF_IN_nextStmtDate().equals(SystemInformationManager.getInstance().getBFBusinessDate())){
        	EventsHelper.handleEvent(ChannelsEventCodes.E_PROPOSEDDATE_CANNOT_BE_PAST_OR_CURRENT_DATE, new Object[]{"Proposed Date"}, new HashMap(), env);
        }
        if(acntNumber.isEmpty() && EMPTY_STRING.equalsIgnoreCase(acntNumber)){
        	EventsHelper.handleEvent(40311686, new Object[]{"Customer Account"}, new HashMap(), env);
        }
        
        if(frequency.equals("I")){
        	getF_OUT_NextStatementDate().setHours(hours);
        	getF_OUT_NextStatementDate().setMinutes(minutes);
        }
        if(frequency.equals("I") && (hrs>= 18)) {
        	EventsHelper.handleEvent(ChannelsEventCodes.E_INTRADAY_SCHEDULE_CROSSING_BUSINESSDAY, new Object[]{}, new HashMap(), env);
        }
        if(frequency.equals("I") && numOfStmnts == 0 && getF_IN_Interval() == 0 
        		&& CommonConstants.EMPTY_STRING.equalsIgnoreCase(getF_IN_StartTime())){
        	EventsHelper.handleEvent(ChannelsEventCodes.E_INTRADAY_SCHEDULE_CANNOT_BE_EMPTY, new Object[]{}, new HashMap(), env);
        }
		if (frequency.equals("D") && !(Statement == 0)) {

			//Object[] params = new Object[]{frequency};
			//throw new BankFusionException(9422, null, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_STATEMENT_DAY_FOR_DAILY_SHOULD_BE, new Object[]{}, new HashMap(), env);


		}
		if ((frequency.equals("W")) && !((Statement >= 1) && (Statement <= 7))) {
			//Object[] params = new Object[]{frequency};
			//throw new BankFusionException(9423, null, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_STATEMENT_DAY_FOR_WEEKLY_SHOULD_FALL_IN_RANGE, new Object[]{}, new HashMap(), env);


		}

		if ((frequency.equals("M")) && !((Statement >= 1) && (Statement <= 31))) {
			//err = new String[] { "Monthly" };
			//throw new BankFusionException(9424, err, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_STMT_DAY_MONTHLY_SHOULD_BE_BTW_1_AND_31_UB, new Object[]{}, new HashMap(), env);

		}
		if ((frequency.equals("Q")) && !((Statement >= 1) && (Statement <= 31))) {
			//err = new String[] { "Quaterly" };
			//throw new BankFusionException(9424, err, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_STMT_DAY_QUARTERLY_SHOULD_BE_BTW_1_AND_31_UB, new Object[]{}, new HashMap(), env);

		}
		if ((frequency.equals("H")) && !((Statement >= 1) && (Statement <= 31))) {
			//err = new String[] { "HalfYearly" };
			//throw new BankFusionException(9424, err, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_STMT_DAY_HALF_YEARLY_SHOULD_BE_BTW_1_AND_31_UB, new Object[]{}, new HashMap(), env);

		}
		if ((frequency.equals("Y")) && !((Statement >= 1) && (Statement <= 31))) {
			//err = new String[] { "Yearly" };
			//throw new BankFusionException(9424, err, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_STMT_DAY_YEARLY_SHOULD_BE_BTW_1_AND_31_UB, new Object[]{}, new HashMap(), env);

			
		}
        if ((frequency.equals("T")) && (Statement <30)) {
            //throw new BankFusionException(9467, err, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_FREQUENCY_IN_MINUTES_TO_BE_GREATER_THAN_30_UB, new Object[]{}, new HashMap(), env);

        }

		if ((NumOfRecords <= 0) && (frequency.equals("N"))) {
			//throw new BankFusionException(9425, err, null, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_RECORD_WILL_NOT_BE_STORED_TILL_VALID_FREQUENCY, new Object[]{err}, new HashMap(), env);


		}
	}
	
}
