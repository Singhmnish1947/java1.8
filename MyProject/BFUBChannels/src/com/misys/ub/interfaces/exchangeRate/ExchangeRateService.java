/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.interfaces.exchangeRate;

import org.springframework.web.bind.annotation.RequestBody;

import com.misys.fbp.common.util.FBPService;

@FBPService(serviceId = "exchangeRateUploadService")
public class ExchangeRateService {

	
	public ExchangeRateResponse uploadExchangeRates(@RequestBody ExchangeRateRequest exchangeRateRequest) {
		ExchangeRateServiceHelper helper = new ExchangeRateServiceHelper();
		return helper.uploadExchangeRates(exchangeRateRequest);
	}
}
