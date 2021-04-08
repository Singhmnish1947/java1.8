/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.interfaces.exchangeRate;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.fbp.common.util.FBPServiceAppContext;

@RestController
@RequestMapping("/exchangeRateUpload")
public class ExchangeRateUploadController {
	
	private static final String EXCHANGERATE_SERVICE_BEAN_ID = "exchangeRateService";
	
	@RequestMapping(value = "/uploadRates", method = RequestMethod.POST, headers = "Accept=application/json", produces=MediaType.APPLICATION_JSON_VALUE)
	public ExchangeRateResponse uploadExchangeRates(@RequestBody ExchangeRateRequest exchangeRateRequest) {
		
		ExchangeRateService service = (ExchangeRateService) FBPServiceAppContext.getInstance().getApplicationContext().getBean(EXCHANGERATE_SERVICE_BEAN_ID);
		return service.uploadExchangeRates(exchangeRateRequest);
	}
}
