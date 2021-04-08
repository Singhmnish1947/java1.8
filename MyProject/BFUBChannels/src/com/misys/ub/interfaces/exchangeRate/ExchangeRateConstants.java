/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.interfaces.exchangeRate;

import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRates;
import com.trapedza.bankfusion.bo.refimpl.IBOForwardPoints;

public interface ExchangeRateConstants {

	String successCode = "0";
	String failureCode = "1";
	String successMessage = "Success";
	String failureMessage = "Failure";
	
	String exchangeRateForward = "FORWARD";
	
	String multiplyDivideFlagM = "M";
	String multiplyDivideFlagD = "D";
	String discountPremiumFlagP = "P";
	String discountPremiumFlagD = "D";
	
	String exchangeRateEndpoint = "OPX_INCOMING_JMS1";
	
	Integer EXCHG_RATE_ZERO = 40015117;
	Integer FROM_TO_CURR_SAME = 20020166;
	Integer E_CB_CMN_CURRENCY_DOES_NOT_EXIST = 20020012;
	Integer E_EXCHGRATETYPE_DOES_NOT_EXIST = 40580179;
	Integer E_CB_INVALID_FIELDS_CB05 = 20020048;
	Integer E_GENERIC_ERROR_UB15 = 40507113;
	
	String whereClause = " WHERE " + IBOForwardPoints.FROMCURRENCYCODE + " = ? AND "
						+ IBOForwardPoints.TOCURRENCYCODE + " = ? AND " + IBOForwardPoints.RANGE + " = ? ";
	String whereClauseType = " WHERE " + IBOExchangeRates.FROMCURRENCYCODE + " = ? AND "
						+ IBOExchangeRates.TOCURRENCYCODE + " = ? AND "
						+ IBOExchangeRates.EXCHANGERATETYPE + " = ? ";
}
