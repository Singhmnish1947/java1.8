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
import com.misys.ub.dc.types.AccountBalanceRequest;
import com.misys.ub.dc.types.AccountBalanceResponse;
import com.misys.ub.dc.types.AccountDetailsRequest;
import com.misys.ub.dc.types.AccountDetailsResponse;

@RestController
@RequestMapping("/AccountService")
public class GetAccountDetailsController {

	private static final String HEADER = "Accept=application/json";
	private static final String BEAN = "GetAccountDetailsService";
	
	private GetAccountDetailsService accountDetailsService;
	
	@RequestMapping(value = "/AccountDetails", method = RequestMethod.POST, headers = HEADER)
	public AccountDetailsResponse getAccountDetails(@RequestBody AccountDetailsRequest accountDetailsRequest) {
		
		accountDetailsService = (GetAccountDetailsService) 
								FBPServiceAppContext
								.getInstance().getApplicationContext()
								.getBean(BEAN);
		
		return accountDetailsService.getAccountDetails(accountDetailsRequest);
	}
	
	@RequestMapping(value = "/AccountBalance", method = RequestMethod.POST, headers = HEADER)
	public AccountBalanceResponse getAccountBalance(@RequestBody AccountBalanceRequest accountBalanceRequest) {
		
		accountDetailsService = (GetAccountDetailsService) 
								FBPServiceAppContext
								.getInstance().getApplicationContext()
								.getBean(BEAN);
		
		return accountDetailsService.getAccountBalance(accountBalanceRequest);
	}
	
	@RequestMapping(value = "/AccountRelations", method = RequestMethod.POST, headers = HEADER)
	public AccountDetailsResponse getAccountRelations(@RequestBody AccountDetailsRequest accountDetailsRequest) {
		
		accountDetailsService = (GetAccountDetailsService) 
								FBPServiceAppContext
								.getInstance().getApplicationContext()
								.getBean(BEAN);
		
		return accountDetailsService.getAccountRelations(accountDetailsRequest);
	}
	
	@RequestMapping(value = "/AccountAndAccountRelations", method = RequestMethod.POST, headers = HEADER)
	public AccountDetailsResponse getAccountAndAccountRelations(@RequestBody AccountDetailsRequest accountDetailsRequest) {
		
		accountDetailsService = (GetAccountDetailsService) 
								FBPServiceAppContext
								.getInstance().getApplicationContext()
								.getBean(BEAN);
		
		return accountDetailsService.getAccountAndAccountRelations(accountDetailsRequest);
	}
}
