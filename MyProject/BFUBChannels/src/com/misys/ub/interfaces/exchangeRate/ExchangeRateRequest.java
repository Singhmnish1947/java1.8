/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.interfaces.exchangeRate;

import java.util.List;

public class ExchangeRateRequest {

	private List<ExchangeRateDetails> details;
	
	public List<ExchangeRateDetails> getDetails() {
		return details;
	}
	
	public void setDetails(List<ExchangeRateDetails> details) {
		this.details = details;
	}
}
