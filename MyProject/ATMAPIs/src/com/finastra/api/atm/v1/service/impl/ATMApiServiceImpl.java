package com.finastra.api.atm.v1.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.mapping.ATMBalanceEnquiryMapping;
import com.finastra.api.atm.v1.mapping.ATMCashDepositMapping;
import com.finastra.api.atm.v1.mapping.ATMCashWithdrawalMapping;
import com.finastra.api.atm.v1.mapping.ATMMiniStatementMapping;
import com.finastra.api.atm.v1.mapping.POSTransactionMapping;
import com.finastra.api.atm.v1.model.AccountBlockResponse;
import com.finastra.api.atm.v1.model.Amount;
import com.finastra.api.atm.v1.model.BalanceEnquiryRequest;
import com.finastra.api.atm.v1.model.BlockedTransaction;
import com.finastra.api.atm.v1.model.CashDepositRequest;
import com.finastra.api.atm.v1.model.CashWithdrawalRequest;
import com.finastra.api.atm.v1.model.MiniStatementRequest;
import com.finastra.api.atm.v1.model.PosRequest;
import com.finastra.api.atm.v1.service.IATMApiService;
import com.finastra.api.utils.ATMTransactionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbp.common.util.FBPService;
import com.trapedza.bankfusion.core.EventsHelper;

@FBPService
public class ATMApiServiceImpl implements IATMApiService {

	String emptyString = "";
	String zero = "0";
	String UPDATE_ERROR_STATUS = "update_ERRORSTATUS";
	private static final String GET_ACCOUNT_BLOCKS = " SELECT VALUEDATE,TRANSACTIONREF,BLOCKINGAMOUNT,BLKMSG.UNBLOCKINGDATETIME,NARRATIVE,TRANSACTIONREF FROM BLOCKINGMESSAGES BLKMSG INNER JOIN BLOCKINGTRANSACTIONS BLKTXN ON BLKMSG.BLOCKINGID=BLKTXN.BLOCKINGID WHERE BLKTXN.ACCOUNTID= ? AND STATUS =0 AND BLKMSG.UBCHANNELID in ('ATM', 'POS')";

	public HashMap<String, Object> processCashWithdrawal(CashWithdrawalRequest cashWithdrawalRq) {

		HashMap<String, Object> essenceRs = null;
		HashMap<String, Object> essenceRq = null;

		BankFusionThreadLocal.setApplicationID("ATM");

		ATMCashWithdrawalMapping atmMappingService = new ATMCashWithdrawalMapping();

		essenceRq = atmMappingService.prepareEssenceRequest(cashWithdrawalRq);
		essenceRs = MFExecuter.executeMF(ATMAPIConstant.ATM_API_MF, essenceRq,
				BankFusionThreadLocal.getUserLocator().toString());

		if (emptyString.equals(essenceRs.get(UPDATE_ERROR_STATUS)) || zero.equals(essenceRs.get(UPDATE_ERROR_STATUS))) {
			essenceRs.put(UPDATE_ERROR_STATUS, "0");
			essenceRs.put("AccountId", cashWithdrawalRq.getAccountIdentification1());
		}

		return essenceRs;
	}

	public HashMap<String, Object> processCashDeposit(CashDepositRequest cashDepositRq) {

		HashMap<String, Object> essenceRs = null;
		HashMap<String, Object> essenceRq = null;

		BankFusionThreadLocal.setApplicationID("ATM");

		ATMCashDepositMapping atmMappingService = new ATMCashDepositMapping();

		essenceRq = atmMappingService.prepareEssenceRequest(cashDepositRq);
		essenceRs = MFExecuter.executeMF(ATMAPIConstant.ATM_API_MF, essenceRq,
				BankFusionThreadLocal.getUserLocator().toString());

		if (emptyString.equals(essenceRs.get(UPDATE_ERROR_STATUS)) || zero.equals(essenceRs.get(UPDATE_ERROR_STATUS))) {
			essenceRs.put(UPDATE_ERROR_STATUS, "0");
			essenceRs.put("AccountId", cashDepositRq.getAccountIdentification2());
		}
		return essenceRs;
	}

	public HashMap<String, Object> processBalanceEnquiry(BalanceEnquiryRequest balanceEnquiryRq) {

		HashMap<String, Object> essenceRs = null;
		HashMap<String, Object> essenceRq = null;

		BankFusionThreadLocal.setApplicationID("ATM");

		ATMBalanceEnquiryMapping atmMappingService = new ATMBalanceEnquiryMapping();

		essenceRq = atmMappingService.prepareEssenceRequest(balanceEnquiryRq);
		essenceRs = MFExecuter.executeMF(ATMAPIConstant.ATM_API_MF, essenceRq,
				BankFusionThreadLocal.getUserLocator().toString());

		if (emptyString.equals(essenceRs.get(UPDATE_ERROR_STATUS)) || zero.equals(essenceRs.get(UPDATE_ERROR_STATUS))) {
			essenceRs.put(UPDATE_ERROR_STATUS, "0");
			essenceRs.put("AccountId", balanceEnquiryRq.getAccountIdentification1());
		}

		return essenceRs;
	}

	public HashMap<String, Object> processMiniStatement(MiniStatementRequest miniStatementRq) {

		HashMap<String, Object> essenceRs = null;
		HashMap<String, Object> essenceRq = null;

		BankFusionThreadLocal.setApplicationID("ATM");

		ATMMiniStatementMapping atmMappingService = new ATMMiniStatementMapping();

		essenceRq = atmMappingService.prepareEssenceRequest(miniStatementRq);
		essenceRs = MFExecuter.executeMF(ATMAPIConstant.ATM_API_MF, essenceRq,
				BankFusionThreadLocal.getUserLocator().toString());

		if (emptyString.equals(essenceRs.get(UPDATE_ERROR_STATUS)) || zero.equals(essenceRs.get(UPDATE_ERROR_STATUS))) {
			essenceRs.put(UPDATE_ERROR_STATUS, "0");
			essenceRs.put("AccountId", miniStatementRq.getAccountIdentification1());
		}

		return essenceRs;
	}

	public HashMap<String, Object> processPosTransaction(PosRequest posRq) {

		HashMap<String, Object> essenceRs = null;
		HashMap<String, Object> essenceRq = null;
		String responderCode = "Responder_Code";
		BankFusionThreadLocal.setApplicationID("ATM");
		POSTransactionMapping posMappingService = new POSTransactionMapping();

		essenceRq = posMappingService.prepareEssenceRequest(posRq);
		essenceRs = MFExecuter.executeMF(ATMAPIConstant.ATM_POS_MF, essenceRq,
				BankFusionThreadLocal.getUserLocator().toString());

		if (emptyString.equals(essenceRs.get(responderCode)) || zero.equals(essenceRs.get(responderCode))) {
			essenceRs.put(responderCode, "0");
		}

		return essenceRs;
	}

	public AccountBlockResponse getAtmPosBlockedTransaction(String accountID, int limit, int offset) {
		int errorCode = ATMTransactionUtil.validateAccount(accountID);

		if (0 != errorCode) {
			EventsHelper.handleEvent(errorCode, new Object[] {accountID}, new HashMap(),
					BankFusionThreadLocal.getBankFusionEnvironment());
		}

		String currencyCode = ATMTransactionUtil.getCurrencyForAccount(accountID);
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		AccountBlockResponse blockResponse = new AccountBlockResponse();
		List<BlockedTransaction> blockedTransactions = new ArrayList<>();
		BlockedTransaction blockedTransactionObject = null;
		double sumOfBlockerAmount = 0;

		// validate Account Details

		try {
			statement = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection()
					.prepareStatement(GET_ACCOUNT_BLOCKS);
			statement.setString(1, accountID);
			resultSet = statement.executeQuery();
			blockResponse.setAccountId(accountID);
			int countLimit = 0;
			int offsetCounter = 1;
			Amount value = new Amount();
			double sum = 0;
			if (null != resultSet) {
				while (countLimit < limit && resultSet.next()) {

					if (offsetCounter > offset) {
						blockedTransactionObject = new BlockedTransaction();

						value.setCurrency(currencyCode);
						value.setValue(resultSet.getDouble("BLOCKINGAMOUNT"));

						blockedTransactionObject.setBlockedAmount(value);
						blockedTransactionObject.setStartDate(resultSet.getTimestamp("VALUEDATE"));
						blockedTransactionObject.setEndDate(resultSet.getTimestamp("UNBLOCKINGDATETIME"));
						blockedTransactionObject.setNarrative(resultSet.getString("NARRATIVE"));
						blockedTransactionObject.setBlockingReference(resultSet.getString("TRANSACTIONREF"));
						sum = sum + resultSet.getDouble("BLOCKINGAMOUNT");

						blockedTransactions.add(blockedTransactionObject);
						countLimit++;
					}
					offsetCounter++;
				}
				value.setCurrency(currencyCode);
				value.setValue(sum);
				blockResponse.setTotalBlockAmount(value);
				blockResponse.accountCurrency(currencyCode);
				blockResponse.setBlockedTransaction(blockedTransactions);
			}

		} catch (SQLException e) {
			try {
				throw e;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return blockResponse;
	}

}