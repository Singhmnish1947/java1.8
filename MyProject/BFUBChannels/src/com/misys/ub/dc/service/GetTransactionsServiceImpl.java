/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.dc.service;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.joda.time.DateTime;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbp.common.util.FBPService;
import com.misys.fbp.common.util.FBPServiceAppContext;
import com.misys.ub.dc.common.ChannelConstants;
import com.misys.ub.dc.restServices.GetAccountDetailsService;
import com.misys.ub.dc.sql.constants.TransactionHistoryQuery;
import com.misys.ub.dc.types.AccountDetails;
import com.misys.ub.dc.types.AccountDetailsRequest;
import com.misys.ub.dc.types.AccountDetailsResponse;
import com.misys.ub.dc.types.TransactionsRequest;
import com.misys.ub.dc.types.TransactionsResponse;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import jxl.common.Logger;


@FBPService(serviceId = "TransactionListService", applicationId = "")
public class GetTransactionsServiceImpl implements GetTransactionsService {

	private IBusinessInformationService BIZ_INFO_SERVICE = (IBusinessInformationService) ServiceManagerFactory
			.getInstance().getServiceManager().getServiceForName("BusinessInformationService");

	private static final String QUERY = "TransactionHistoryQuery";
	private static final String ACCOUNTS = "GetAccountDetailsService";

	private static final Logger logger = Logger.getLogger(GetTransactionsServiceImpl.class);
	
	private static final String SYS = "ENQUIRY_GLOBAL_DATE_RANGE";

	private TransactionHistoryQuery transactionHistoryQuery;
	private GetAccountDetailsService getAccountDetailsService;
	
	@SuppressWarnings("deprecation")
	@Override
	public TransactionsResponse getTransactionList(TransactionsRequest transactionsRequest) {

		TransactionsResponse response = new TransactionsResponse();
		AccountDetailsRequest accRequest = null;
		AccountDetailsResponse accResponse = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ResultSetMetaData metaData = null;
		List<String> transactions = null;
		List<String> columns = null;
		List<String> accounts = null;
		int paramIndex = 1;
		int rowLimiter = 1;
		int EVENT_CODE_GENERIC = Integer.parseInt(ChannelConstants.EVENT_CODE_GENERIC);
		int NO_DETAILS_FOUND = Integer.parseInt(ChannelConstants.EVENT_CODE_NO_DETAILS);
		Integer dateRange = Integer.parseInt(getModuleConfigValue("ENQUIRY_GLOBAL_DATE_RANGE", "SYS"));
		
		//Date Range Validation for Transactions
			if(transactionsRequest.getFromDate()!= null && transactionsRequest.getToDate()!= null &&
					ChronoUnit.DAYS.between(transactionsRequest.getFromDate().toInstant(),transactionsRequest.getToDate().toInstant()) > dateRange) {
				addErrorResponse(response, ChannelConstants.E_CB_DATE_RANGE_VALIDATION,
						null, new String[] {Integer.toString(dateRange)});
				return response;
			}
		
		// Check if accounts are present. If empty fetch accounts for customer.
		if (transactionsRequest.getAccountId() == null || transactionsRequest.getAccountId().size() == 0) {
			accRequest = new AccountDetailsRequest();
			accRequest.setCustomerId(transactionsRequest.getCustomerId());
				
			accResponse = getAccountDetailsService.getAccountDetails(accRequest);

			accounts = new ArrayList<String>();
			for (AccountDetails details : accResponse.getAccountDetails()) {
				accounts.add(details.getAccountId());
			}

			transactionsRequest.setAccountId(accounts);
			
		}
		
		// If accounts is empty, send no details found response
		if (transactionsRequest.getAccountId().size() == 0) {
			addErrorResponse(response, NO_DETAILS_FOUND, ChannelConstants.EVENT_CODE_NO_DETAILS,  null);
			return response;
		
		}
		StringBuffer buffer;
		String ATMMode = getModuleConfigValue("ATM_REQUEST_MODE", "ATM");
		if (ATMMode.equals("API")) {
			// Fetch query and form query string
			buffer = new StringBuffer(transactionHistoryQuery.TRANSACTION_LIST_QUERY_WITH_ATM_API);

			buffer.append("(");
			for (int i = 0; i < transactionsRequest.getAccountId().size(); i++) {
				buffer.append(" ?,");
			}
			int index = buffer.length();
			buffer.replace(index - 1, index, ")");

			buffer.append(transactionHistoryQuery.OUTER_JOIN_QUERY_WITH_ATM_API);

		} else {

			// Fetch query and form query string
			buffer = new StringBuffer(transactionHistoryQuery.TRANSACTION_LIST_QUERY);

			buffer.append("(");
			for (int i = 0; i < transactionsRequest.getAccountId().size(); i++) {
				buffer.append(" ?,");
			}
			int index = buffer.length();
			buffer.replace(index - 1, index, ")");

			buffer.append(transactionHistoryQuery.OUTER_JOIN_QUERY);
		}
		// Form prepared statement and execute
		try {
			rowLimiter = Integer.parseInt(getModuleConfigValue("TXNCOUNT", "INTBANKING"));
			Integer numberOfDays = Integer
					.parseInt(getModuleConfigValue("DURATION_TRANSACTION_HISTORY", "IBI"));

			statement = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection()
					.prepareStatement(buffer.toString());
			statement.setMaxRows(rowLimiter);
			
			if (transactionsRequest.getFromDate() == null
					|| transactionsRequest.getFromDate().equals(CommonConstants.EMPTY_STRING)) {
				DateTime fromDate = new DateTime().minusDays(numberOfDays);

				statement.setTimestamp(paramIndex++, new java.sql.Timestamp(fromDate.getMillis()));
				statement.setTimestamp(paramIndex++, SystemInformationManager.getInstance().getBFSystemDateTime());
			} else {
				statement.setTimestamp(paramIndex++,transactionsRequest.getFromDate());
				statement.setTimestamp(paramIndex++,transactionsRequest.getToDate());
			}
			for (String account : transactionsRequest.getAccountId()) {
				statement.setString(paramIndex++, account);
			}
			resultSet = statement.executeQuery();

			metaData = resultSet.getMetaData();
			
			columns = new ArrayList<String>();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				columns.add(metaData.getColumnLabel(i));
			}

			// Parse results and generate response
			transactions = new ArrayList<String>();

			while (resultSet.next()) {
				transactions.add(getDetails(resultSet, columns));
			}

			// If transactions is empty, send no details found response
			if (transactions.size() == 0) {
				
				addErrorResponse(response, NO_DETAILS_FOUND, ChannelConstants.EVENT_CODE_NO_DETAILS,  null);
				return response;
			}

			response.setTransactions(transactions);
			response.setStatus(ChannelConstants.STATUS_SUCCESS);

		} catch (BankFusionException e) {

			// Handle errors and set response
			logger.error(e.getMessage(), e);

			Collection<IEvent> errors = e.getEvents();
			Iterator<IEvent> errorIterator = errors.iterator();
			IEvent event = errorIterator.next();
			String errorCode = (Integer.toString((event.getEventNumber())));

			addErrorResponse(response, NO_DETAILS_FOUND, errorCode, event.getDetails());

		} catch (SQLException e) {

			logger.error(e.getMessage(), e);
			addErrorResponse(response, EVENT_CODE_GENERIC, ChannelConstants.EVENT_CODE_GENERIC,
					 new Object[] { transactionsRequest.getCustomerId() });

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				if (!statement.isClosed()) {
					statement.close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return response;
	}

	/**
	 * Parse the resultset and form a string with column names as key and result set
	 * as values.
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	private String getDetails(ResultSet resultSet, List<String> columns) throws SQLException {

		StringBuffer transaction = new StringBuffer();

		for (String columnName : columns) {
			transaction.append(columnName).append(ChannelConstants.STRING_EQUALS)
					.append(resultSet.getString(columnName)).append(ChannelConstants.COLUMN_SEPARTOR);
		}

		return transaction.toString();
	}

	private void addErrorResponse(TransactionsResponse response, int eventCode, String eventMessage, Object[] details) {
		String message = BankFusionMessages.getInstance().getFormattedEventMessage(eventCode, details,
                BankFusionThreadLocal.getUserSession().getUserLocale());
		response.setStatus(ChannelConstants.STATUS_FAILURE);
		response.setEventCode(String.valueOf(eventCode));
		response.setEventMessage(message);
		response.setArguments(details);
	}

	public GetTransactionsServiceImpl() {

		transactionHistoryQuery = (TransactionHistoryQuery) FBPServiceAppContext.getInstance().getApplicationContext()
				.getBean(QUERY);
		getAccountDetailsService = (GetAccountDetailsService) FBPServiceAppContext.getInstance().getApplicationContext()
				.getBean(ACCOUNTS);
	}

    private String getModuleConfigValue(String param, String moduleId) {
        return String.valueOf(this.BIZ_INFO_SERVICE.getBizInfo().getModuleConfigurationValue(moduleId, param, null));
    }
}