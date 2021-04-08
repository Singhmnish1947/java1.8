/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.restServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.common.Logger;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbp.common.util.FBPService;
import com.misys.ub.dc.types.AccountBalance;
import com.misys.ub.dc.types.AccountBalanceRequest;
import com.misys.ub.dc.types.AccountBalanceResponse;
import com.misys.ub.dc.types.AccountDetails;
import com.misys.ub.dc.types.AccountDetailsRequest;
import com.misys.ub.dc.types.AccountDetailsResponse;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;

@FBPService(serviceId = "AccountDetailsService", applicationId = "")
public class GetAccountDetailsServiceImpl implements GetAccountDetailsService {

	private static final String FIND_QUERY = "SELECT a.ACCOUNTID, a.PRODUCTID, a.ISOCURRENCYCODE, a.OPENDATE, a.STOPPED, "
											+ "a.CLOSED, a.DORMANTSTATUS, a.ACCRIGHTSINDICATOR, a.PRODUCTCONTEXTCODE, "
											+ "a.UBACCOUNTSTATUS, a.UBCUSTOMERCODE AS CUSTOMER, COALESCE(a.UBMODEOFOPERATION,'SINGLE') AS UBMODEOFOPERATION, "
											+ "b.UBCUSTOMERCODE AS OWNER, b.UBROLE, c.PSEUDONAME "
											+ "FROM ACCOUNT a "
											+ "JOIN UBTB_ACCTMANDATE b ON a.ACCOUNTID = b.UBACCOUNTID "
											+ "LEFT JOIN PSEUDONYMACCOUNTMAP c ON c.ACCOUNTID = a.ACCOUNTID AND c.SORTCONTEXTVALUE IN ('IBANACCOUNT', 'BT_ALTERNATE') "
											+ "WHERE b.UBCUSTOMERCODE = ? ";
	
	private static final String ACCT_QUERY = "SELECT a.ACCOUNTID, a.PRODUCTID, a.ISOCURRENCYCODE, a.OPENDATE, a.STOPPED, "
											+ "a.CLOSED, a.DORMANTSTATUS, a.ACCRIGHTSINDICATOR, a.PRODUCTCONTEXTCODE, "
											+ "a.UBACCOUNTSTATUS, a.UBCUSTOMERCODE CUSTOMER, COALESCE(a.UBMODEOFOPERATION,'SINGLE') AS UBMODEOFOPERATION, "
											+ "b.UBCUSTOMERCODE OWNER, b.UBROLE, c.PSEUDONAME "
											+ "FROM ACCOUNT a "
											+ "JOIN UBTB_ACCTMANDATE b ON  a.ACCOUNTID = b.UBACCOUNTID "
											+ "LEFT JOIN PSEUDONYMACCOUNTMAP c ON c.ACCOUNTID = a.ACCOUNTID AND c.SORTCONTEXTVALUE IN ('IBANACCOUNT', 'BT_ALTERNATE') "
											+ "WHERE a.ACCOUNTID IN (?) "
											+ "AND (b.UBCUSTOMERCODE = ? OR a.UBCUSTOMERCODE = ?) ";
	
	private static final String BAL_QUERY = "SELECT a.ACCOUNTID, a.BOOKEDBALANCE, a.CLEAREDBALANCE, a.BLOCKEDBALANCE, a.CREDITLIMIT, a.ISOCURRENCYCODE, c.BFCURRENCYSCALE "
											+ "FROM ACCOUNT a "
											+ "JOIN BFTB_CURRENCY c ON a.ISOCURRENCYCODE = c.BFISOCURRENCYCODEPK "
											+ "WHERE a.ACCOUNTID IN (?) ";
	
	private static final String ACCT_AND_ACCT_REL_QUERY = "SELECT a.ACCOUNTID, a.PRODUCTID, a.ISOCURRENCYCODE, a.OPENDATE, a.STOPPED, "
														+ "a.CLOSED, a.DORMANTSTATUS, a.ACCRIGHTSINDICATOR, a.PRODUCTCONTEXTCODE, "
														+ "a.UBACCOUNTSTATUS, a.UBCUSTOMERCODE CUSTOMER, COALESCE(a.UBMODEOFOPERATION,'SINGLE') AS UBMODEOFOPERATION, "
														+ "b.UBCUSTOMERCODE OWNER, b.UBROLE, c.PSEUDONAME "
														+ "FROM ACCOUNT a "
														+ "JOIN UBTB_ACCTMANDATE b ON  a.ACCOUNTID = b.UBACCOUNTID "
														+ "LEFT JOIN PSEUDONYMACCOUNTMAP c ON c.ACCOUNTID = a.ACCOUNTID AND c.SORTCONTEXTVALUE IN ('IBANACCOUNT', 'BT_ALTERNATE') "
														+ "WHERE a.ACCOUNTID IN (SELECT UBACCOUNTID from UBTB_ACCTMANDATE WHERE UBCUSTOMERCODE = ?) ";
	
	private static final Logger logger = Logger.getLogger(GetAccountDetailsServiceImpl.class);
	
	@SuppressWarnings({ "deprecation" })
	@Override
	public AccountDetailsResponse getAccountDetails(AccountDetailsRequest accountDetailsRequest) {
		
		AccountDetailsResponse			accountDetailsResponse 	= new AccountDetailsResponse();
		List<AccountDetails>			accounts				= new ArrayList<AccountDetails>();
		PreparedStatement				statement				= null;
		ResultSet						resultSet				= null;
		
		if(isNullOrEmpty(accountDetailsRequest.getCustomerId())) {
			return accountDetailsResponse;
		}
		
		try {
			statement = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(FIND_QUERY);
			statement.setString(1, accountDetailsRequest.getCustomerId());
			
			resultSet = statement.executeQuery();
			
			while(resultSet.next()) {
				
				AccountDetails details = new AccountDetails();
				
				details.setAccountId(resultSet.getString("ACCOUNTID"));
				details.setProductId(resultSet.getString("PRODUCTID"));
				details.setCurrencyCode(resultSet.getString("ISOCURRENCYCODE"));
				details.setOpenDate(resultSet.getString("OPENDATE"));
				details.setStopped(resultSet.getString("STOPPED"));
				details.setClosed(resultSet.getString("CLOSED"));
				details.setDormantStatus(resultSet.getString("DORMANTSTATUS"));
				details.setAriIndicator(resultSet.getString("ACCRIGHTSINDICATOR"));
				details.setProductContextCode(resultSet.getString("PRODUCTCONTEXTCODE"));
				details.setAccountStatus(resultSet.getString("UBACCOUNTSTATUS"));
				details.setCustomerCode(resultSet.getString("CUSTOMER"));
				details.setModeOfOperation(resultSet.getString("UBMODEOFOPERATION"));
				details.setOwnerId(resultSet.getString("OWNER"));
				details.setRole(resultSet.getString("UBROLE"));
				details.setPseudoname(resultSet.getString("PSEUDONAME"));
				
				accounts.add(details);
			}
		} catch (SQLException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
			try {
				if(!statement.isClosed()) {
					statement.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
		
		accountDetailsResponse.setCustomerId(accountDetailsRequest.getCustomerId());
		accountDetailsResponse.setAccountDetails(accounts.toArray(new AccountDetails[accounts.size()]));
		
		return accountDetailsResponse;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	@Override
	public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest) {
		
		AccountBalanceResponse		accountBalanceResponse	= new AccountBalanceResponse();
		List<AccountBalance>		accountBalances			= new ArrayList<AccountBalance>();
		PreparedStatement			statement				= null;
		ResultSet					resultSet				= null;
		int 						scaleVal 				= 0;
		BigDecimal					amount					= null;
		
		if(accountBalanceRequest.getAccounts() == null || accountBalanceRequest.getAccounts().length == 0) {
			return accountBalanceResponse;
		}
		
		int size = accountBalanceRequest.getAccounts().length;
		StringBuffer buffer = new StringBuffer();
		
		for(int i = 0; i < size; i++) {
			buffer.append("?, ");
		}
		
		String query = BAL_QUERY.replace("?", buffer.substring(0, buffer.length() - 2));
		
		try {
			statement = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(query);
			for(int i = 0; i < size; i++) {
				statement.setString((i + 1), accountBalanceRequest.getAccounts()[i]);
			}
			
			resultSet = statement.executeQuery();
			
			while(resultSet.next()) {
				
				AccountBalance details = new AccountBalance();
				HashMap balances = null;
				
				details.setAccountId(resultSet.getString("ACCOUNTID"));
				
				balances = AvailableBalanceFunction.run(details.getAccountId());
				
				scaleVal = resultSet.getInt("BFCURRENCYSCALE");
				
				amount = resultSet.getBigDecimal("BOOKEDBALANCE");
				balances.put("BOOKEDBALANCE", amount.setScale(scaleVal, RoundingMode.DOWN).toString());
				
				amount = resultSet.getBigDecimal("CLEAREDBALANCE");
				balances.put("CLEAREDBALANCE", amount.setScale(scaleVal, RoundingMode.DOWN).toString());
				
				amount = resultSet.getBigDecimal("BLOCKEDBALANCE");
				balances.put("BLOCKEDBALANCE", amount.setScale(scaleVal, RoundingMode.DOWN).toString());
				
				amount = resultSet.getBigDecimal("CREDITLIMIT");
				balances.put("CREDITLIMIT", amount.setScale(scaleVal, RoundingMode.DOWN).toString());
				
				details.setBalances(balances);
				accountBalances.add(details);
			}
		} catch (SQLException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
			try {
				if(!statement.isClosed()) {
					statement.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
		
		accountBalanceResponse.setCustomerId(accountBalanceRequest.getCustomerId());
		accountBalanceResponse.setBalances(accountBalances.toArray(new AccountBalance[accountBalances.size()]));
		
		return accountBalanceResponse;
	}

	@SuppressWarnings({ "deprecation" })
	@Override
	public AccountDetailsResponse getAccountRelations(AccountDetailsRequest accountDetailsRequest) {

		AccountDetailsResponse			accountDetailsResponse 	= new AccountDetailsResponse();
		List<AccountDetails>			accounts				= new ArrayList<AccountDetails>();
		PreparedStatement				statement				= null;
		ResultSet						resultSet				= null;
		int								i						= 0;
		
		try {
			if(accountDetailsRequest.getAccounts() == null || accountDetailsRequest.getAccounts().length == 0) {
				
				if(isNullOrEmpty(accountDetailsRequest.getCustomerId())) {
					return accountDetailsResponse;
				}
				
				statement = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(FIND_QUERY);
				statement.setString(1, accountDetailsRequest.getCustomerId());
				
				resultSet = statement.executeQuery();
				
			} else {

				int size = accountDetailsRequest.getAccounts().length;
				StringBuffer buffer = new StringBuffer();
				
				for(i = 0; i < size; i++) {
					buffer.append("?, ");
				}
				
				String query = ACCT_QUERY.replace("(?)", "(" + buffer.substring(0, buffer.length() - 2) + ")");
				
				statement = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(query);
				for(i = 0; i < size; i++) {
					statement.setString((i + 1), accountDetailsRequest.getAccounts()[i]);
				}
				
				statement.setString((i + 1), accountDetailsRequest.getCustomerId());
				statement.setString((i + 2), accountDetailsRequest.getCustomerId());
				
				resultSet = statement.executeQuery();
			}
			
			while(resultSet.next()) {
				
				AccountDetails details = new AccountDetails();
				
				details.setAccountId(resultSet.getString("ACCOUNTID"));
				details.setProductId(resultSet.getString("PRODUCTID"));
				details.setCurrencyCode(resultSet.getString("ISOCURRENCYCODE"));
				details.setOpenDate(resultSet.getString("OPENDATE"));
				details.setStopped(resultSet.getString("STOPPED"));
				details.setClosed(resultSet.getString("CLOSED"));
				details.setDormantStatus(resultSet.getString("DORMANTSTATUS"));
				details.setAriIndicator(resultSet.getString("ACCRIGHTSINDICATOR"));
				details.setProductContextCode(resultSet.getString("PRODUCTCONTEXTCODE"));
				details.setAccountStatus(resultSet.getString("UBACCOUNTSTATUS"));
				details.setCustomerCode(resultSet.getString("CUSTOMER"));
				details.setModeOfOperation(resultSet.getString("UBMODEOFOPERATION"));
				details.setOwnerId(resultSet.getString("OWNER"));
				details.setRole(resultSet.getString("UBROLE"));
				details.setPseudoname(resultSet.getString("PSEUDONAME"));
				
				accounts.add(details);
			}
		} catch (SQLException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
			try {
				if(statement != null && !statement.isClosed()) {
					statement.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
		
		accountDetailsResponse.setCustomerId(accountDetailsRequest.getCustomerId());
		accountDetailsResponse.setAccountDetails(accounts.toArray(new AccountDetails[accounts.size()]));
		
		return accountDetailsResponse;
	}
	
	@SuppressWarnings({ "deprecation" })
	@Override
	public AccountDetailsResponse getAccountAndAccountRelations(AccountDetailsRequest accountDetailsRequest) {

		AccountDetailsResponse			accountDetailsResponse 	= new AccountDetailsResponse();
		List<AccountDetails>			accounts				= new ArrayList<AccountDetails>();
		PreparedStatement				statement				= null;
		ResultSet						resultSet				= null;
		int								i						= 0;
		
		if(isNullOrEmpty(accountDetailsRequest.getCustomerId())) {
			return accountDetailsResponse;
		}
		try {
			statement = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(ACCT_AND_ACCT_REL_QUERY);
			statement.setString(1, accountDetailsRequest.getCustomerId());
			
			resultSet = statement.executeQuery();
			
			while(resultSet.next()) {
				
				AccountDetails details = new AccountDetails();
				
				details.setAccountId(resultSet.getString("ACCOUNTID"));
				details.setProductId(resultSet.getString("PRODUCTID"));
				details.setCurrencyCode(resultSet.getString("ISOCURRENCYCODE"));
				details.setOpenDate(resultSet.getString("OPENDATE"));
				details.setStopped(resultSet.getString("STOPPED"));
				details.setClosed(resultSet.getString("CLOSED"));
				details.setDormantStatus(resultSet.getString("DORMANTSTATUS"));
				details.setAriIndicator(resultSet.getString("ACCRIGHTSINDICATOR"));
				details.setProductContextCode(resultSet.getString("PRODUCTCONTEXTCODE"));
				details.setAccountStatus(resultSet.getString("UBACCOUNTSTATUS"));
				details.setCustomerCode(resultSet.getString("CUSTOMER"));
				details.setModeOfOperation(resultSet.getString("UBMODEOFOPERATION"));
				details.setOwnerId(resultSet.getString("OWNER"));
				details.setRole(resultSet.getString("UBROLE"));
				details.setPseudoname(resultSet.getString("PSEUDONAME"));
				
				accounts.add(details);
			}
			
		} catch (SQLException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
			try {
				if(statement != null && !statement.isClosed()) {
					statement.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
		
		accountDetailsResponse.setCustomerId(accountDetailsRequest.getCustomerId());
		accountDetailsResponse.setAccountDetails(accounts.toArray(new AccountDetails[accounts.size()]));
		
		return accountDetailsResponse;
	}


	private boolean isNullOrEmpty(String param) {
		if(param == null || param.trim().length() == 0) {
			return true;
		}
		return false;
	}
}
