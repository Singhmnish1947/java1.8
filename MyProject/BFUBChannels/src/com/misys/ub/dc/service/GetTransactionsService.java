/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.service;

import com.misys.ub.dc.types.TransactionsRequest;
import com.misys.ub.dc.types.TransactionsResponse;

public interface GetTransactionsService {

	public TransactionsResponse getTransactionList(TransactionsRequest getTransactionsRequest);
}
