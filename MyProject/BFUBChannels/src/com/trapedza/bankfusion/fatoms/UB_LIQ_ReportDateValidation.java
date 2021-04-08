package com.trapedza.bankfusion.fatoms;

import java.util.Date;

import com.misys.bankfusion.calendar.functions.SubtractDateFromDate;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.cbs.config.ModuleConfiguration;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_LIQ_ReportDateValidation;

import bf.com.misys.cbs.types.events.Event;

public class UB_LIQ_ReportDateValidation extends AbstractUB_LIQ_ReportDateValidation {

	private static final long serialVersionUID = 4994047019301716251L;
	private static final String MODULE_NAME = "CorpLending_Interface";
	private static final String DAY_RANGE = "REPORT_BUFFER_DAYS";
	private static final int DAY_ERROR_CODE = 40430065;
	private static final int INCORRECT_DATE_RANGE = 40200108;

	public UB_LIQ_ReportDateValidation(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {
		
		Date fromDate = getF_IN_fromDate(); 	// from date and to date is swapped in screen do not get confused
		Date toDate = getF_IN_toDate();			// from=to and to=from
		int inputRange = SubtractDateFromDate.run(fromDate, toDate);
		Integer configuredRange = (Integer) ModuleConfiguration.getInstance().getModuleConfigurationValue(MODULE_NAME,
				DAY_RANGE);
		int difference = inputRange - configuredRange;
		if (inputRange < 0) {
			handleEvent(INCORRECT_DATE_RANGE, new String[] {});
		} else if (difference > 0) {
			handleEvent(DAY_ERROR_CODE, new String[] { configuredRange.toString() });
		} else if (difference == 0) {
			if ((fromDate.compareTo(toDate) < 0) ^ (fromDate.compareTo(toDate) > 0)) {
				handleEvent(DAY_ERROR_CODE, new String[] { configuredRange.toString() });
			}
		}
	}

	public static void handleEvent(Integer eventNumber, String[] args) {
		if (args == null) {
			args = new String[] { CommonConstants.EMPTY_STRING };
		}
		Event event = new Event();
		event.setEventNumber(eventNumber);
		event.setMessageArguments(args);
		IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance()
				.getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
		businessEventsService.handleEvent(event);
	}
}
