package com.misys.ub.cc.restServices;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.bankfusion.attributes.PagingRequest;
import bf.com.misys.cbs.msgs.v1r0.ReadAccountRq;
import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;
import bf.com.misys.cbs.msgs.v1r0.SearchAccountRq;
import bf.com.misys.cbs.msgs.v1r0.SearchAcctRs;
import bf.com.misys.cbs.types.AccountCharacteristics;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.AccountSearch;
import bf.com.misys.cbs.types.AcctCharacteristics;
import bf.com.misys.cbs.types.AcctInfo;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.ListAccountDtls;
import bf.com.misys.cbs.types.ListAcct;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.fbp.common.util.FBPService;
import com.misys.ub.cc.types.AccountDetailsOverview;
import com.misys.ub.cc.types.SearchAccountInterfaceRq;
import com.misys.ub.cc.types.SearchAccountInterfacesRs;
import com.misys.ub.cc.types.SearchAccountListRq;
import com.misys.ub.cc.utils.SearchAccountInterfaceConstants;
import com.misys.ub.cc.utils.SearchAccountInterfaceUtils;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

@Transactional
@FBPService(serviceId = "SearchAccountInterface", applicationId = "")
public class SearchAccountInterfaceService {
	
	private static final String GET_COUNTRYCODE = 
												"SELECT COUNTRYOFRESIDENCE FROM PERSONDETAILS WHERE CUSTOMERCODE=?" +
												"UNION " +
												"SELECT COUNTRYOFINCORPORATION FROM ORGDETAILS WHERE CUSTOMERCODE=?" +
												"UNION " +
												"SELECT UBCOUNTRYID FROM ACCOUNT WHERE ACCOUNTID=?";
	private static final transient Log logger = LogFactory.getLog(SearchAccountInterfaceService.class);
	
	@Transactional
	@SuppressWarnings({ "deprecation", "rawtypes" })
	final public SearchAccountInterfacesRs update(SearchAccountInterfaceRq request) {
		SearchAccountInterfacesRs searchAccountInterfacesRs = new SearchAccountInterfacesRs();
		SearchAccountRq searchAccountRq = new SearchAccountRq();
		SearchAcctRs searchAcctRs = new SearchAcctRs();
    	PagedQuery pagedQuery = new PagedQuery();
    	PagingRequest pagingRequest = new PagingRequest();
    	Date openFrom = new Date(70, 0, 1);
    	Date openTo = new Date(1000, 1, 1);
    	pagingRequest.setNumberOfRows(60);
    	pagingRequest.setRequestedPage(1);
    	pagingRequest.setTotalPages(10);
    
    	pagedQuery.setPagingRequest(pagingRequest);
    	
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		IBOAccount account = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, request.getAccountId(), false);
		HashMap<String, Object> custparams = new HashMap<String, Object>();
		custparams.put("CustomerCode", account.getF_CUSTOMERCODE());
		FBPMicroflowServiceInvoker inv = new FBPMicroflowServiceInvoker("UB_CNF_IsInternalCustomer_SRV");
		
		HashMap<String, Object> resParams = inv.invokeMicroflow(custparams, false);
		boolean isInternalCustomer = (boolean) resParams.get("IsInternalCustomer");
		if(isInternalCustomer) {
			ReadAccountRq readAccountRq = new ReadAccountRq();
			AccountKeys accountKeys = new AccountKeys();
			accountKeys.setStandardAccountId(request.getAccountId());
			accountKeys.setExternalAccountId("");
			InputAccount inputAccount = new InputAccount();
			inputAccount.setAccountFormatType("ST");
			accountKeys.setInputAccount(inputAccount);
			readAccountRq.setAccountKeys(accountKeys);
			HashMap<String, Object> inparams = new HashMap<String, Object>();
			inparams.put("ReadAccountRq", readAccountRq);// parent
			FBPMicroflowServiceInvoker in = new FBPMicroflowServiceInvoker("UB_R_CB_ACC_ReadAccount_SRV");
			HashMap<String, Object> opParams =in.invokeMicroflow(inparams, false);
			ReadAccountRs readAccountRs = (ReadAccountRs)opParams.get("ReadAccountRs");
			
			ListAcct accListAcct = new ListAcct();
			ListAccountDtls listAccountDetails = new ListAccountDtls();
			AcctInfo acctInfo = new AcctInfo();
			acctInfo.setAcctBalances(readAccountRs.getAccountDetails().getAccountInfo().getAcctBalances());
			acctInfo.setAccountCharacteristics(getAcctCharacteristics(readAccountRs.getAccountDetails().getAccountInfo().getAcctCharacteristics()));
			acctInfo.setAcctBasicDetails(readAccountRs.getAccountDetails().getAccountInfo().getAcctBasicDetails());
			listAccountDetails.setAcctInfo(acctInfo);
			accListAcct.addListAccountDtls(listAccountDetails);
			searchAcctRs.setSearchAccountDetails(accListAcct);
		} else {
			AccountSearch accountSearch = new AccountSearch();
			accountSearch.setAccountId(request.getAccountId());
			accountSearch.setCustomerId(account.getF_CUSTOMERCODE());
	    	accountSearch.setDateAccountOpenedFrom(openFrom);
	    	accountSearch.setDateAccountOpenedTo(openTo);
	    	
			searchAccountRq.setAccountSearch(accountSearch);
	    	searchAccountRq.setPagedQuery(pagedQuery);
			HashMap<String, Object> inputParams = new HashMap<String, Object>();
			inputParams.put(SearchAccountInterfaceConstants.SEARCH_ACCOUNT_REQUEST, searchAccountRq);
			FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker(SearchAccountInterfaceConstants.SEARCH_ACCOUNT_SERVICE);
			HashMap outputParams = invoker.invokeMicroflow(inputParams, false);
			searchAcctRs = (SearchAcctRs) outputParams.get(SearchAccountInterfaceConstants.SEARCH_ACCOUNT_RESPONSE);
		}
		searchAccountInterfacesRs = SearchAccountInterfaceUtils.getSearchAccountInterfacesRs(searchAcctRs, request.getAccountId());
		String customerId = searchAccountInterfacesRs.getAccountDetailsOverviewList().get(0).getExtensiveAccountDetails().getAccountBasicDetails().getCustomerShortDetails().getCustomerId();
		String accountId = searchAccountInterfacesRs.getAccountDetailsOverviewList().get(0).getExtensiveAccountDetails().getAccountBasicDetails().getAccountKeys().getStandardAccountId();
		String countryCode = fetchCountryCode(customerId, accountId);
		searchAccountInterfacesRs.getAccountDetailsOverviewList().get(0).getExtensiveAccountDetails().setCountryCode(countryCode);
		return searchAccountInterfacesRs;
	}
	
	@Transactional
	@SuppressWarnings("deprecation")
	private String fetchCountryCode(String customerId, String accountId) {	
		logger.info("Start of fetchCountryCode");
		String countryCode = null;
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		Connection connection = factory.getJDBCConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = connection.prepareStatement(GET_COUNTRYCODE);
			pstmt.setString(1, customerId);
			pstmt.setString(2, customerId);
			pstmt.setString(3, accountId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				if (!(rs.getString(1)==null || rs.getString(1).equalsIgnoreCase("")))
					countryCode = rs.getString(1);
			}
		} catch (SQLException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				}catch (SQLException e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
		}
		
		logger.info("End of fetchCountryCode");
		return countryCode;
	}

	@Transactional
	@SuppressWarnings({ "deprecation", "rawtypes" })
	final public SearchAccountInterfacesRs updateList(SearchAccountListRq requestList) {
		SearchAccountInterfacesRs searchAccountInterfacesRs = new SearchAccountInterfacesRs();
		SearchAccountInterfacesRs searchAccountInterfacesRsFinal = new SearchAccountInterfacesRs();
		ArrayList<AccountDetailsOverview> completeAccountDetailsList = new ArrayList<AccountDetailsOverview>();
		SearchAccountRq searchAccountRq = new SearchAccountRq();
    	PagedQuery pagedQuery = new PagedQuery();
    	PagingRequest pagingRequest = new PagingRequest();
    	Date openFrom = new Date(70, 0, 1);
    	Date openTo = new Date(1000, 1, 1);
    	pagingRequest.setNumberOfRows(60); 
    	pagingRequest.setRequestedPage(1);  
    	pagingRequest.setTotalPages(10);
    
    	pagedQuery.setPagingRequest(pagingRequest);
    	List<String> accountList = requestList.getAccountIdList();
    	
    	for(String s: accountList){
    		
    	
		AccountSearch accountSearch = new AccountSearch();
		accountSearch.setAccountId(s);
    	accountSearch.setDateAccountOpenedFrom(openFrom);
    	accountSearch.setDateAccountOpenedTo(openTo);
    	
		searchAccountRq.setAccountSearch(accountSearch);
    	searchAccountRq.setPagedQuery(pagedQuery);
		HashMap<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put(SearchAccountInterfaceConstants.SEARCH_ACCOUNT_REQUEST, searchAccountRq);
		FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker(SearchAccountInterfaceConstants.SEARCH_ACCOUNT_SERVICE);
		HashMap outputParams = invoker.invokeMicroflow(inputParams, false);
		SearchAcctRs searchAcctRs = (SearchAcctRs) outputParams.get(SearchAccountInterfaceConstants.SEARCH_ACCOUNT_RESPONSE);
		searchAccountInterfacesRs = SearchAccountInterfaceUtils.getSearchAccountInterfacesRs(searchAcctRs, s);
		completeAccountDetailsList.add(searchAccountInterfacesRs.getAccountDetailsOverviewList().get(0));
		
    	}
    	searchAccountInterfacesRsFinal.setAccountDetailsOverviewList(completeAccountDetailsList);
		return searchAccountInterfacesRsFinal;
	}
	
	@Transactional
	private AcctCharacteristics getAcctCharacteristics(AccountCharacteristics accountCharacteristics) {
		AcctCharacteristics acctCharacteristics = new AcctCharacteristics();
		acctCharacteristics.setAllCreditsBlocked(accountCharacteristics.getAllCreditsBlocked());
		acctCharacteristics.setAllCreditsReferred(accountCharacteristics.getAllCreditsReferred());
		acctCharacteristics.setAllDebitsBlocked(accountCharacteristics.getAllDebitsBlocked());
		acctCharacteristics.setAllDebitsReferred(accountCharacteristics.getAllDebitsReferred());
		acctCharacteristics.setAllTransactionsBlocked(accountCharacteristics.getAllTransactionsBlocked());
		acctCharacteristics.setDeceasedLiquidaatedDt(accountCharacteristics.getDeceasedLiquidaatedDt());
		acctCharacteristics.setDormancyDate(accountCharacteristics.getDormancyDate());
		acctCharacteristics.setEnquiryallowed(accountCharacteristics.getEnquiryallowed());
		acctCharacteristics.setIsChargeWaived(accountCharacteristics.getIsChargeWaived());
		acctCharacteristics.setIsChequeBookAvailable(accountCharacteristics.getIsChequeBookAvailable());
		acctCharacteristics.setIsClosed(accountCharacteristics.getIsClosed());
		acctCharacteristics.setIsDeceasedLiquidated(accountCharacteristics.getIsDeceasedLiquidated());
		acctCharacteristics.setIsDormant(accountCharacteristics.getIsDormant());
		acctCharacteristics.setIsInternalAccount(accountCharacteristics.getIsInternalAccount());
		acctCharacteristics.setIsJoint(accountCharacteristics.getIsJoint());
		acctCharacteristics.setIsMinor(accountCharacteristics.getIsMinor());
		acctCharacteristics.setIsNarrativeMandatory(accountCharacteristics.getIsNarrativeMandatory());
		acctCharacteristics.setIsPassbook(accountCharacteristics.getIsPassbook());
		acctCharacteristics.setIsStatementAvailable(accountCharacteristics.getIsStatementAvailable());
		acctCharacteristics.setIsStoped(accountCharacteristics.getIsStoped());
		//acctCharacteristics.setModeOfOperation();
		acctCharacteristics.setPswdForCredit(accountCharacteristics.getPswdForCredit());
		acctCharacteristics.setPswdForDebit(accountCharacteristics.getPswdForDebit());
		acctCharacteristics.setPswdForEnquiry(accountCharacteristics.getPswdForEnquiry());
		acctCharacteristics.setPswdForPosting(accountCharacteristics.getPswdForPosting());
		
		
		return acctCharacteristics;
	}
}
