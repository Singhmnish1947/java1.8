package com.trapedza.bankfusion.fatoms;

import java.sql.Date;
import java.util.Map;

import bf.com.misys.cbs.types.NonWorkingDay;
import bf.com.misys.cbs.types.NonWorkingDayList;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_AddNonWorkingDayToList;

public class UB_CMN_AddNonWorkingDayToList extends
		AbstractUB_CMN_AddNonWorkingDayToList {
	private static final long serialVersionUID = 1L;

	public UB_CMN_AddNonWorkingDayToList() {
	}

	public UB_CMN_AddNonWorkingDayToList(BankFusionEnvironment env) {
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		NonWorkingDayList nonWorkingDayList = new NonWorkingDayList();

		VectorTable result = getF_IN_queryResult();
		for (int i = 0; i < result.size(); i++) {
			@SuppressWarnings("unchecked")
			Map<String, Object> row = result.getRowTags(i);
			if (row != null) {
				String id = row.get("NONWORKINGDAYID").toString();
				String description = row.get("DESCRIPTION").toString();
				String context = row.get("CONTEXT").toString();
				String value = row.get("VALUE").toString();

				Object objDate = row.get("SPECIFICDATE");
				Date date = null;
				if (objDate != null) {
					date = (Date) objDate;
				}

				NonWorkingDay nonWorkingDay = new NonWorkingDay();
				nonWorkingDay.setId(id);
				nonWorkingDay.setDescription(description);
				nonWorkingDay.setContext(context);
				nonWorkingDay.setContextValue(value);
				nonWorkingDay.setSpecificDate(date);

				nonWorkingDayList.addNonWorkingDate(nonWorkingDay);
			}
		}
		this.setF_OUT_nonWorkingDayList(nonWorkingDayList);
	}
}
