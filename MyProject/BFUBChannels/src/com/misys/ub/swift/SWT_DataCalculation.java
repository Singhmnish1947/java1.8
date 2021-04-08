package com.misys.ub.swift;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.IsWorkingDay;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.PreviousWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

public class SWT_DataCalculation {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(SWT_DataCalculation.class.getName());

    static Integer ParamValue_LookAheadDays;
    static Boolean ParamValue_AdviceDays;
    static String ParamValue_Date;

    static int LookAheadDays;
    static int AdviceDays;
    static Date FinalDate;

    // Query for fetching records from MODULECONFIGURATION.


    public static boolean generateCategory2Message(java.sql.Date ValueDate, java.sql.Date EventDate, BankFusionEnvironment env,
            String currency, java.sql.Date bankfusionSystemDate, String messageType) {
        /*
         * Checkes whether newDate is Valuedate or EventDate and that date is working day or not.
         * Return true if new calculated date is equal to or before the bankfusionSystemDate.
         */

        Date newDate = null;
        messageType = "MT" + messageType;

        try {
            LookAheadDays = calDate(currency, env, messageType);

            if ("V".equals(ParamValue_Date)) {
                newDate = ValueDate;
            }
            else {
                newDate = EventDate;
            }

            Boolean isCurrentDayWorking = IsWorkingDay.run("CURRENCY", currency, new Integer(0), newDate, env);
            if (!isCurrentDayWorking.booleanValue()) {
                newDate = PreviousWorkingDateForDate.run("CURRENCY", currency, new Integer(0), newDate, env);
            }

            for (int i = 0; i < Math.abs(LookAheadDays); i++) {

                newDate = previousDate(newDate.toString(), currency, env, messageType);
                Boolean isWorking = IsWorkingDay.run("CURRENCY", currency, new Integer(0), newDate, env);
                if (!isWorking.booleanValue()) {
                    newDate = PreviousWorkingDateForDate.run("CURRENCY", currency, new Integer(0), newDate, env);
                }

            }

        }
        catch (BankFusionException e) {
            logger.info("Exception in checkWorkingDay method: ", e);
        }
        logger.info("New Calculated Date: " + newDate);
        if (newDate == bankfusionSystemDate || (null != newDate && newDate.before(bankfusionSystemDate)))
            return true;
        else return false;
    }

    private static int calDate(String Currency, BankFusionEnvironment env, String messageType) {

        /*
         * Execute the Query for MODULECONFIGURATION and fetch the records for ADVICEDAYS,
         * LOOKAHEADDAYS and DATETYPE. returns the LookAheadDays.
         */
        IBOCurrency contraCurrency = CurrencyUtil.getCurrencyDetailsOfCurrentZone(Currency);
        int aDays = 0;
        if(null != contraCurrency)
        {
            aDays = contraCurrency.getF_SWTADVICEDAYS();

        }
        else {
            logger.error("Currency not available on this " + Currency);
            // throw new BankFusionException(9455, new Object[] { params.get(0) }, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
                    new Object[] { Currency }, new HashMap(), env);

        }

        Map resultMap =null;
        SimplePersistentObject simpleObject = null;
        ArrayList params = new ArrayList();
        params.add(messageType);

        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        resultMap = (Map) bizInfo.getModuleConfigurationValue(messageType, env);

        ParamValue_AdviceDays = (Boolean) resultMap.get("ADVICEDAYS"); 
        ParamValue_LookAheadDays = (Integer) resultMap.get("LOOKAHEADDAYS");
        ParamValue_Date = (String) resultMap.get("DATETYPE");

        if (!ParamValue_AdviceDays.booleanValue()) {
            AdviceDays = 0;
        }
        else {
            AdviceDays = aDays;
        }

        LookAheadDays = AdviceDays + ParamValue_LookAheadDays.intValue();
        return (LookAheadDays);

    }

    public static Date previousDate(String aDate, String Currency, BankFusionEnvironment env, String messageType) {

        /*
         * returns the previous day of the passed date.
         */

        int previousDay = -1;
        Calendar cal = null;
        String[] date;

        cal = Calendar.getInstance();
        date = aDate.split("-");
        cal.clear();
        cal.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);
        cal.add(cal.DATE, previousDay);
        return new Date(cal.getTimeInMillis());
    }
}
