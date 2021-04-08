/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: CB_TIP_ExchangeRateTypes,v.1.0,April 20, 2012 11:35:34 AM Ayyappa
 *
 */
package com.misys.ub.extract;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBONonWorkingDay;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractCurrBranchCalendars;

import bf.com.misys.cbs.types.ExtractCalendarDetail;
import bf.com.misys.cbs.types.ExtractCalendarDetailsWeek;
import bf.com.misys.cbs.types.Week;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractCalendarRs;

public class ExtractCurrBranchCalendars extends
		AbstractUB_TIP_ExtractCurrBranchCalendars {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final Log logger = LogFactory
			.getLog(ExtractCurrBranchCalendars.class.getName());
	private IPersistenceObjectsFactory factory;

	String nonWorkingDay = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;

	public ExtractCurrBranchCalendars(BankFusionEnvironment env) {
		super(env);
	}

    public void process(BankFusionEnvironment env) throws BankFusionException {

        try {
            factory = BankFusionThreadLocal.getPersistanceFactory();

            ExtractCalendarRs extractCalendarRs = new ExtractCalendarRs();

            ExtractCalendarDetail[] extractCalendarDetail = new ExtractCalendarDetail[1];
            extractCalendarDetail[0] = new ExtractCalendarDetail();

            ExtractCalendarDetailsWeek[] extractCalendarDetailWeek = new ExtractCalendarDetailsWeek[1];
            extractCalendarDetailWeek[0] = new ExtractCalendarDetailsWeek();

            Week[] next7days = new Week[1];
            next7days[0] = new Week();

            Week[] standardWeek = new Week[1];
            standardWeek[0] = new Week();

            crudMode = getF_IN_mode();
            nonWorkingDay = getF_IN_extractCalendarRq().getExtractCalendarInput().getCalendar();

            IBONonWorkingDay extractCurrencyBranch = fetchExtractCurrencyBranch();
            if (extractCurrencyBranch != null) {

                next7days[0].setDayDate(extractCurrencyBranch.getF_SPECIFICDATE());
                /*
                 * next7days[0].setDayName(DayName); next7days[0].setDayNumber(dayNumber);
                 * next7days[0].setWorkingDay(workingDay);
                 */

                standardWeek[0].setDayDate(extractCurrencyBranch.getF_SPECIFICDATE());
                /*
                 * standardWeek[0].setDayName(dayName); standardWeek[0].setDayNumber(dayNumber);
                 * standardWeek[0].setWorkingDay(workingDay);
                 */
                extractCalendarDetailWeek[0].setDescription(extractCurrencyBranch.getF_DESCRIPTION());
                extractCalendarDetailWeek[0].setEntityType(extractCurrencyBranch.getF_CONTEXT());
                extractCalendarDetailWeek[0].setEntityTypeValue(extractCurrencyBranch.getF_VALUE());

                extractCalendarDetail[0].setDescription(extractCurrencyBranch.getF_DESCRIPTION());
                extractCalendarDetail[0].setEntityType(extractCurrencyBranch.getF_CONTEXT());
                extractCalendarDetail[0].setEntityTypeValue(extractCurrencyBranch.getF_VALUE());

            }

            extractCalendarDetailWeek[0].setCrudMode(crudMode);

            // extractCalendarDetailWeek[0].setHostExtension(hostExtension);

            extractCalendarDetailWeek[0].setNext7days(next7days);
            extractCalendarDetailWeek[0].setStandardWeek(standardWeek);

            extractCalendarDetail[0].setCrudMode(crudMode);

            /*
             * extractCalendarDetail[0].setCalendarDate(calendarDate);
             * extractCalendarDetail[0].setHostExtension(hostExtension);
             * extractCalendarDetail[0].setSpecialWorkingDay(specialWorkingDay);
             * extractCalendarDetail [0].setStandardWorkingDay(standardWorkingDay);
             * extractCalendarDetail[0].setUserExtension(userExtension);
             */

            extractCalendarRs.setExtractCalendarDetail(extractCalendarDetail[0]);
            extractCalendarRs.setExtractCalendarDetailWeek(extractCalendarDetailWeek[0]);
            extractCalendarRs.setRsHeader(new RsHeader());
            extractCalendarRs.getRsHeader().setMessageType("CurrencyCalendar");
            setF_OUT_extractCalendarRs(extractCalendarRs);
        }
        catch (Exception e) {
            logger.error("Error in ExtractCurrBranchCalendars.java for Primary Key " + nonWorkingDay + " Error is "
                    + ExceptionUtil.getExceptionAsString(e));

            throw new BankFusionException(e);
        }

    }

    private IBONonWorkingDay fetchExtractCurrencyBranch() {
        IBONonWorkingDay nonWorkingDate = (IBONonWorkingDay) factory.findByPrimaryKey(IBONonWorkingDay.BONAME, nonWorkingDay, true);
        if (nonWorkingDate != null && nonWorkingDate.getF_ZONE().equals(BankFusionThreadLocal.getUserZone())) {
            return nonWorkingDate;
        }
        return null;
    }
}
