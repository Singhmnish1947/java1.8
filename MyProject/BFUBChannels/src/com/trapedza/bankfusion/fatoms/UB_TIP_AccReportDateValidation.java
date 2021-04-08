package com.trapedza.bankfusion.fatoms;

import java.util.Date;
import java.util.HashMap;

import com.misys.bankfusion.common.runtime.toolkit.expression.function.SubtractDateFromDateAsMonths;
import com.misys.cbs.config.ModuleConfiguration;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ValidateRangeOfMonths;

@SuppressWarnings("deprecation")
public class UB_TIP_AccReportDateValidation extends AbstractUB_TIP_ValidateRangeOfMonths {

	private static final long serialVersionUID = -7975501165109723945L;
	private static final String MODULE_NAME = "TIP";
	private static final String MONTH_RANGE = "MONTH_RANGE";
	private static final int MONTH_ERROR_CODE = 40280116;

	public UB_TIP_AccReportDateValidation(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		Date fromDate = getF_IN_fromDate();
		Date toDate = getF_IN_toDate();

		int enteredRange = SubtractDateFromDateAsMonths.run(fromDate, toDate);

		Integer configuredRange = (Integer) ModuleConfiguration.getInstance()
				.getModuleConfigurationValue(MODULE_NAME, MONTH_RANGE);

		int difference = enteredRange - configuredRange;

		if (difference > 0) {
			EventsHelper.handleEvent(MONTH_ERROR_CODE,
					new Object[] { configuredRange },
					new HashMap<String, Object>(), env);
		}

		else if (difference == 0) {
			if (((fromDate.getDate() - toDate.getDate()) < 0)
					^ ((fromDate.getDate() - toDate.getDate()) > 0)) {
				EventsHelper.handleEvent(MONTH_ERROR_CODE,
						new Object[] { configuredRange },
						new HashMap<String, Object>(), env);
			}

		}


	}
}
