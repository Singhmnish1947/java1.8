/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.restServices;

import com.misys.ub.dc.types.AccountBalanceRequest;
import com.misys.ub.dc.types.AccountBalanceResponse;
import com.misys.ub.dc.types.AccountDetailsRequest;
import com.misys.ub.dc.types.AccountDetailsResponse;

public interface GetAccountDetailsService {

	public AccountDetailsResponse getAccountDetails(AccountDetailsRequest accountDetailsRequest);
	
	public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest);
	
	public AccountDetailsResponse getAccountRelations(AccountDetailsRequest accountDetailsRequest);
	
	public AccountDetailsResponse getAccountAndAccountRelations(AccountDetailsRequest accountDetailsRequest);
}
