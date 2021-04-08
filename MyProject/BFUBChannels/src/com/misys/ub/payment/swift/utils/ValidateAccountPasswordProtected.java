package com.misys.ub.payment.swift.utils;

import java.util.HashMap;
import java.util.Map;

import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;

public class ValidateAccountPasswordProtected {

	public String accRightIndicatorCheck(int rightIndicator) {
		switch (rightIndicator) {
		case 1:
		case 9:
			return "40007319";
		case -1:
			return "40007318";
		case 2:
			return "40007321";
		case 3:
			return "40112172";
		case 4:
			return "40007322";
		case 5:
			return "40007323";
		case 6:
			return "40409356";
		case 7:
			return "40007325";
		default:
			return "";
		}
	}

	public String isAccountPasswordProtected(String accountId,
			String accountType) {
		int accRightIndicator;
		Map<String, String> map = new HashMap();
		Map outPutMap;
		map.put("AccountID", accountId);
		outPutMap = UB_IBI_PaymentsHelper.getAccountDetails(accountId);
		if (outPutMap.get("ACCRIGHTSINDICATOR") != null) {
			accRightIndicator = (Integer) outPutMap.get("ACCRIGHTSINDICATOR");
			return accRightIndicatorCheck(accRightIndicator);
		} else {
			return "";
		}
	}
}
