package com.misys.ub.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAddress;
import com.trapedza.bankfusion.bo.refimpl.IBOAddressLinks;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRq;
import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;
import bf.com.misys.cbs.msgs.v1r0.ReadCustomerRq;
import bf.com.misys.cbs.msgs.v1r0.ReadCustomerRs;
import bf.com.misys.cbs.msgs.v1r0.RetrievePsydnymAcctIdRq;
import bf.com.misys.cbs.msgs.v1r0.RetrievePsydnymAcctIdRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.Address;
import bf.com.misys.cbs.types.CustomerRq;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.PseudonymBasicDetails;

/**
 * @author Machamma.Devaiah
 *
 */
public class DataCenterCommonUtils {

	/**
	 * This method returns the paramValue for the module id and key passed to
	 * the module configuration service.
	 * 
	 * @param moduleId
	 * @param key
	 */

	public static String readModuleConfiguration(String moduleId, String key) {
		String paramValue = null;
		HashMap result = null;
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		ModuleKeyRq module = new ModuleKeyRq();
		HashMap map = new HashMap();
		module.setModuleId(moduleId);
		module.setKey(key);
		read.setModuleKeyRq(module);
		map.put("ReadModuleConfigurationRq", read);
		result = MFExecuter.executeMF(
				DataCenterCommonConstants.READ_MODULE_CONFIG,
				BankFusionThreadLocal.getBankFusionEnvironment(), map);
		if (result != null) {
			ReadModuleConfigurationRs rs = null;
			rs = (ReadModuleConfigurationRs) result
					.get("ReadModuleConfigurationRs");
			paramValue = rs.getModuleConfigDetails().getValue();
		}
		return paramValue;
	}

	public static ReadAccountRs readAccount(String accountId) {
		ReadAccountRs rs = null;
		HashMap result = null;
		ReadAccountRq rq = new ReadAccountRq();
		AccountKeys acctKeys = new AccountKeys();
		InputAccount inpt = new InputAccount();
		inpt
				.setAccountFormatType(DataCenterCommonConstants.ACCOUNT_FORMAT_TYPE);
		acctKeys.setInputAccount(inpt);
		acctKeys.setStandardAccountId(accountId);
		rq.setAccountKeys(acctKeys);
		HashMap map = new HashMap();
		map.put("ReadAccountRq", rq);
		result = MFExecuter.executeMF(
				DataCenterCommonConstants.READ_ACCOUNT_DETAILS,
				BankFusionThreadLocal.getBankFusionEnvironment(), map);
		if (result != null) {
			rs = (ReadAccountRs) result.get("ReadAccountRs");
		}
		return rs;
	}

	public static boolean checkForExternalProductFeature(String accountID) {
		Boolean hasExternalProductFeature = false;
		String productId = CommonConstants.EMPTY_STRING;
		HashMap result = null;
		HashMap map = new HashMap();
		IPersistenceObjectsFactory factory = BankFusionThreadLocal
				.getPersistanceFactory();
		IBOAttributeCollectionFeature accountBO = (IBOAttributeCollectionFeature) factory
				.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
						accountID, true);

		if (accountBO != null) {
			productId = accountBO.getF_PRODUCTID();
		}

		map.put("productid", productId);
		result = MFExecuter.executeMF(
				DataCenterCommonConstants.WHAT_PRODUCT_DEFAULTS,
				BankFusionThreadLocal.getBankFusionEnvironment(), map);
		if (result != null) {
			hasExternalProductFeature = (Boolean) result
					.get("HASEXTERNALPRODFTR");
		}

		return hasExternalProductFeature;

	}

	public static String retrievePsuedonymAcctId(String isoCcyCode,
			String branchCode, String Context, String pseudonymId) {
		String accountId = CommonConstants.EMPTY_STRING;
		HashMap result = null;
		HashMap map = new HashMap();
		RetrievePsydnymAcctIdRq pseudonymRq = new RetrievePsydnymAcctIdRq();
		PseudonymBasicDetails pseudonymDetails = new PseudonymBasicDetails();
		pseudonymDetails.setBranchCode(branchCode);
		pseudonymDetails.setContextType(Context);
		pseudonymDetails.setIsoCurrencyCode(isoCcyCode);
		pseudonymDetails.setPseudonymID(pseudonymId);
		pseudonymRq.setPseudonymBasicDetails(pseudonymDetails);

		map.put("retrievePseudonymRq", pseudonymRq);

		result = MFExecuter.executeMF(
				DataCenterCommonConstants.RETRIEVE_PSEUDONYM_ACCT_ID,
				BankFusionThreadLocal.getBankFusionEnvironment(), map);
		if (result != null) {
			RetrievePsydnymAcctIdRs psedonymRs = null;
			psedonymRs = (RetrievePsydnymAcctIdRs) result
					.get("retrievePseudonymRs");
			accountId = psedonymRs.getPseudonymDetails().getPseudonymAcctId()
					.getStandardAccountId();
		}

		return accountId;
	}

	public static boolean readKYCStatusOfCustomer(String customerCode) {
		final String ONE = "001";
		final String FIVE = "005";
		final String PASSIVE = "PASSIVE";
        final String WALKIN = "010";
		Boolean isBlackListedCustomer = Boolean.FALSE;

		IPersistenceObjectsFactory factory = BankFusionThreadLocal
				.getPersistanceFactory();
		IBOCustomer customerBO = (IBOCustomer) factory.findByPrimaryKey(
				IBOCustomer.BONAME, customerCode, true);

		if (customerBO != null) {
			String customerStatus = customerBO.getF_CUSTOMERSTATUS().toString();
			if (customerStatus.compareToIgnoreCase(ONE) == 0
					|| customerStatus.compareToIgnoreCase(FIVE) == 0
                    || customerStatus.compareToIgnoreCase(WALKIN) == 0
					|| customerStatus.compareToIgnoreCase(PASSIVE) == 0) {
				isBlackListedCustomer = Boolean.FALSE;
			}else{
				 isBlackListedCustomer = Boolean.TRUE;
			}
		}

		return isBlackListedCustomer;
	}

	
	public static String readAccountsRightsIndicator(String accountID) {
		String accountsRightsIndicator = CommonConstants.EMPTY_STRING;

		IPersistenceObjectsFactory factory = BankFusionThreadLocal
				.getPersistanceFactory();
		IBOAccount accountBO = (IBOAccount) factory.findByPrimaryKey(
				IBOAccount.BONAME, accountID, true);

		if (accountBO != null) {
			Integer accountRightsInd=accountBO.getF_ACCRIGHTSINDICATOR();
			accountsRightsIndicator=String.valueOf(accountRightsInd);
			}

		return accountsRightsIndicator;
	}
	
	
	
    /**
     * @param customerId
     * @return
     */
    public static ReadCustomerRs readCustomerDetails(String customerId) {
        ReadCustomerRq rq = new ReadCustomerRq();
        ReadCustomerRs rs = new ReadCustomerRs();
        CustomerRq customerRq = new CustomerRq();
        HashMap result = null;
        customerRq.setCustomerId(customerId);
        rq.setCustomerRq(customerRq);
        HashMap map = new HashMap();
        map.put("readCustomerRq", rq);
        result = MFExecuter.executeMF(DataCenterCommonConstants.READ_CUSTOMER_DETAILS,
                BankFusionThreadLocal.getBankFusionEnvironment(), map);
        if (result != null) {
            rs = (ReadCustomerRs) result.get("readCustomerRs");
        }
        return rs;
    }
	
	public static Address readAddressDetails(String customerId, String addressType) {
		Address addressDtls = new Address();
		String FIND_ADDRESS_BASED_ON_ADDRESSTYPE = "SELECT "+ IBOAddress.ADDRESSLINE1+ " AS ADDR1 "+ ", "+IBOAddress.ADDRESSLINE2+ " AS ADDR2 "+ " , "+IBOAddress.ADDRESSLINE3+ " AS ADDR3 "+" , "+IBOAddress.ADDRESSLINE4+ " AS ADDR4 "+ " FROM " + IBOAddress.BONAME + " WHERE "
				+ IBOAddress.ADDRESSID + " = ( SELECT " + IBOAddressLinks.ADDRESSID + " FROM " + IBOAddressLinks.BONAME
				+ " WHERE " + IBOAddressLinks.CUSTACC_KEY + " =  ? AND " + IBOAddressLinks.ADDRESSTYPE + " = ? )";

		ArrayList<String> params = new ArrayList<String>();
		params.add(customerId);
		params.add(addressType);
		List<SimplePersistentObject> addressList = (ArrayList) BankFusionThreadLocal.getPersistanceFactory()
				.executeGenericQuery(FIND_ADDRESS_BASED_ON_ADDRESSTYPE, params, null, false);
		if (addressList != null && addressList.size() > 0) {
			addressDtls.setAddressLine1((String)addressList.get(0).getDataMap().get("ADDR1"));
			addressDtls.setAddressLine2((String)addressList.get(0).getDataMap().get("ADDR2"));
			addressDtls.setAddressLine3((String)addressList.get(0).getDataMap().get("ADDR3"));
			addressDtls.setAddressLine4((String)addressList.get(0).getDataMap().get("ADDR4"));

		}

		return addressDtls;

	}

	
}
