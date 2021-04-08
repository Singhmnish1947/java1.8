/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.restServices;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.fbp.common.util.FBPServiceAppContext;
import com.misys.ub.dc.service.GetTransactionsService;
import com.misys.ub.dc.types.TransactionsRequest;
import com.misys.ub.dc.types.TransactionsResponse;

@RestController
@RequestMapping("/TransactionService")
public class GetTransactionsController {

	private static final String HEADER = "Accept=application/json";
	private static final String BEAN = "GetTransactionsService";
	
	private GetTransactionsService getTransactionsService;
	
	@RequestMapping(value = "/TransactionDetails", method = RequestMethod.POST, headers = HEADER)
	public TransactionsResponse getTransactionList(@RequestBody TransactionsRequest transactionsRequest) {
		
		getTransactionsService = (GetTransactionsService) FBPServiceAppContext
									.getInstance().getApplicationContext()
									.getBean(BEAN);
		return getTransactionsService.getTransactionList(transactionsRequest);
	}
}
