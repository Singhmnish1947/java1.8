package com.misys.ub.cc.utils;

import java.util.ArrayList;
import java.util.HashMap;

import bf.com.misys.cbs.msgs.v1r0.ReadLoanDetailsRq;
import bf.com.misys.cbs.msgs.v1r0.ReadLoanDetailsRs;
import bf.com.misys.cbs.msgs.v1r0.SearchAcctRs;
import bf.com.misys.cbs.types.AcctInfo;
import bf.com.misys.cbs.types.ListAccountDtls;
import bf.com.misys.cbs.types.ListAcct;
import bf.com.misys.cbs.types.ReadLoanDetailsInput;

import com.misys.ub.cc.types.AccountDetailsOverview;
import com.misys.ub.cc.types.ExtensiveAccountDetails;
import com.misys.ub.cc.types.SearchAccountInterfacesRs;
import com.trapedza.bankfusion.bo.refimpl.IBOFixtureFeature;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

public class SearchAccountInterfaceUtils {

	public static final String whereClause = " WHERE " + IBOFixtureFeature.ACCOUNTID + " = ?";
	
	public static ExtensiveAccountDetails convertListAccountDetails(
			AcctInfo accountInfo, String accountId) {
		ExtensiveAccountDetails extensiveAccountDetails = new ExtensiveAccountDetails();
		
		extensiveAccountDetails.setAcctCharacteristics(accountInfo.getAccountCharacteristics());
		extensiveAccountDetails.setAcctBalances(accountInfo.getAcctBalances());
		extensiveAccountDetails.setListMandateDetails(accountInfo.getListMandateDetails());
		if (null != accountInfo.getAcctBasicDetails()) {
			extensiveAccountDetails.setAccountBasicDetails(accountInfo.getAcctBasicDetails());
		} else {
			return extensiveAccountDetails;
		}
		
//		getDepositDetails(extensiveAccountDetails, accountId);
		
		boolean isLoanEnabled = false;
		
		isLoanEnabled = lendingFeatureEnabled(accountInfo.getAcctBasicDetails().getProductId());
		if(!isLoanEnabled){
			return extensiveAccountDetails;
		}
		
		ReadLoanDetailsRq req = new ReadLoanDetailsRq();
		ReadLoanDetailsInput readLoanDetailsInput = new ReadLoanDetailsInput();
		readLoanDetailsInput.setLoanAccountNo(accountId);
		req.setReadLoanDetailsInput(readLoanDetailsInput);
		HashMap<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put(SearchAccountInterfaceConstants.READ_LOAN_DETAILS_REQUEST, req);
		try {
			FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker(SearchAccountInterfaceConstants.READ_LOAN_DETAILS_SERVICE);
			HashMap outputParams = invoker.invokeMicroflow(inputParams, false);
			ReadLoanDetailsRs readLoanDetailsRs = (ReadLoanDetailsRs) outputParams.get(SearchAccountInterfaceConstants.READ_LOAN_DETAILS_RESPONSE);
			extensiveAccountDetails.setReadLoanDetailsRs(readLoanDetailsRs);
		} catch (Exception e) {
			return extensiveAccountDetails;
		}
		
		return extensiveAccountDetails;
	}

	private static boolean lendingFeatureEnabled(String productId) {
		FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker(SearchAccountInterfaceConstants.CHECK_LENDING_FEATURE_SERVICE);
		HashMap inputParams = new HashMap();
		inputParams.put(SearchAccountInterfaceConstants.PRODUCT_ID, productId);
		HashMap outputParams = invoker.invokeMicroflow(inputParams, false);
		Boolean hasLendingFeature = (Boolean) outputParams.get(SearchAccountInterfaceConstants.HAS_LENDING_FEATURE);
		return hasLendingFeature;
	}
	
	/*private static void getDepositDetails(ExtensiveAccountDetails extensiveAccountDetails, String accountId) {
		ArrayList<String> fixparams = new ArrayList<String>();
		
		fixparams.add(accountId);
		List fixtureDBRows = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                					IBOFixtureFeature.BONAME
                				,	whereClause
                				,	fixparams
                				,	null
                				);
		
		if(fixtureDBRows != null && fixtureDBRows.size() > 0) {
			TermDepositOverview overview = new TermDepositOverview();
			SimplePersistentObject fixture = (SimplePersistentObject) fixtureDBRows.get(0);
			
			overview.setOriginalAmount((BigDecimal) fixture.getDataMap().get("f_ORIGINALPRINCIPAL"));
			overview.setInterestRate(new BigDecimal(0));
			overview.setInterestAtMaturity((BigDecimal) fixture.getDataMap().get("f_ORIGINALINTERESTATMATURITY"));
			overview.setMaturityAmount(new BigDecimal(0));
			
			extensiveAccountDetails.setDepositOverview(overview);
		}
	}*/

	public static SearchAccountInterfacesRs getSearchAccountInterfacesRs(SearchAcctRs searchAcctRs, String accountId) {
		SearchAccountInterfacesRs searchAccountInterfaceRs = new SearchAccountInterfacesRs();
		ArrayList<AccountDetailsOverview> completeAccountDetailsList = new ArrayList<AccountDetailsOverview>();
		ListAcct listAccounts = searchAcctRs.getSearchAccountDetails();
		ListAccountDtls[] listAccountDetailsList = listAccounts.getListAccountDtls();

		for (ListAccountDtls listAccountDetails : listAccountDetailsList) {
			AccountDetailsOverview completeAccountDetails = new AccountDetailsOverview();
			completeAccountDetails.setExtensiveAccountDetails(convertListAccountDetails(listAccountDetails.getAcctInfo(), accountId));
			completeAccountDetails.setAccStatus(listAccountDetails.getAccStatus());
			completeAccountDetails.setAccStatus(listAccountDetails.getRelationshipManager());
			completeAccountDetailsList.add(completeAccountDetails);
		}
		searchAccountInterfaceRs.setAccountDetailsOverviewList(completeAccountDetailsList);
		return searchAccountInterfaceRs;
	}
}
