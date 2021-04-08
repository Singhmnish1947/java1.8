package com.finastra.api.atm.v1.service;

import java.util.HashMap;

import com.finastra.api.atm.v1.model.AccountBlockResponse;
import com.finastra.api.atm.v1.model.BalanceEnquiryRequest;
import com.finastra.api.atm.v1.model.CashDepositRequest;
import com.finastra.api.atm.v1.model.CashWithdrawalRequest;
import com.finastra.api.atm.v1.model.MiniStatementRequest;
import com.finastra.api.atm.v1.model.PosRequest;

public interface IATMApiService {

	public HashMap<String, Object> processCashWithdrawal(CashWithdrawalRequest cashWithdrawalRq);

	public HashMap<String, Object> processCashDeposit(CashDepositRequest cashDepositRq);

	public HashMap<String, Object> processBalanceEnquiry(BalanceEnquiryRequest balanceEnquiry);

	public HashMap<String, Object> processMiniStatement(MiniStatementRequest miniStatement);

	public HashMap<String, Object> processPosTransaction(PosRequest posTransaction);
	
	public AccountBlockResponse getAtmPosBlockedTransaction( String accountId, int limit, int offset);

}
