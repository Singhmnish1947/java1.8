package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_CustomerExternalReferences;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_IBI_EnabledSubProducts;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_EnableDisableSubProductsSave;

public class UB_IBI_EnableDisableSubProductsSave extends
		AbstractUB_IBI_EnableDisableSubProductsSave {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
     */

	private transient final static Log logger = LogFactory
			.getLog(UB_IBI_EnableProductCurrencyFatom.class.getName());

	private static String ENABLED_SUB_PRODUCTS_WHERE_CLAUSE = " WHERE "
			+ IBOUB_IBI_EnabledSubProducts.PRODUCTCONTEXTCODE + " = ? AND "
			+ IBOUB_IBI_EnabledSubProducts.ISIBIENABLED + " = ?";

	private static String ACCOUNT_WHERE_CLAUSE = " WHERE "
			+ IBOAccount.PRODUCTCONTEXTCODE + "=? AND " + IBOAccount.ACCOUNTID
			+ "=? ";

	private static String IBI_ENABLED_CUSTOMER_QUERY = "WHERE "
			+ IBOUB_CNF_CustomerExternalReferences.UBISACTIVE + " = 'Y' ";
	
	
	private static String READ_KYC_STATUS_SRV="UB_CNF_ReadKYCStatus_SRV";
	private static String GET_CUSTOMER_ACCOUNTS_SRV="UB_IBI_GetCustomerAccounts_SRV";
	private static String FETCH_ACCOUNT_SERVICE_SRV="UB_CMN_FetchAccountService";
	private static String READ_IB_Account_SRV="UB_IBI_ReadIBAccount_SRV";
	private static String RAISE_ENABLE_ACCOUNY_EVENT_SRV="UB_IBI_RaiseEnableAccountEvent_SRV";
	private static String RAISE_EVENT_SRV="UB_IBI_RaiseEvent_SRV";

	/**
	 * @param env
	 */
	public UB_IBI_EnableDisableSubProductsSave(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {

		VectorTable selectedSubProducts = getF_IN_SelectedSubProducts();
		Boolean isEnable = isF_IN_enableDisable();
		String productContextCode = CommonConstants.EMPTY_STRING;
		ArrayList paramList = null;
		HashMap<String, String> params = null;
		String customerNo = CommonConstants.EMPTY_STRING;
		IPersistenceObjectsFactory factory = BankFusionThreadLocal
				.getPersistanceFactory();
		for (int i = 0; i < selectedSubProducts.size(); i++) {

			productContextCode = (String) (selectedSubProducts.getRowTags(i))
					.get("PRODUCTINHERITENCE_PRODUCTCONTEXTCODE");

			ArrayList iBIEnabledCustomersList = (ArrayList) factory
					.findByQuery(IBOUB_CNF_CustomerExternalReferences.BONAME,
							IBI_ENABLED_CUSTOMER_QUERY, null, false);
			Iterator<IBOUB_CNF_CustomerExternalReferences> iBIEnabledCustomersListIterator = iBIEnabledCustomersList
					.iterator();
			while (iBIEnabledCustomersListIterator.hasNext()) {

				IBOUB_CNF_CustomerExternalReferences customerExternalReferencesObject = iBIEnabledCustomersListIterator
						.next();
				customerNo = customerExternalReferencesObject
						.getF_CUSTOMERCODE();
				// Check whether the customer is blacklisted or not
				// UB_CNF_ReadKYCStatus_SRV
				HashMap<String, Object> kycStatusResult = readKYCStatus(env,
						customerNo);

				if ((Boolean) kycStatusResult.get("continue")) {
					// Fetch the list of Accounts for the customer
					HashMap<String, VectorTable> result = getCustomerAccounts(
							env, customerNo);

					VectorTable accountsVectorTable = result.get("FinalResult");
					if (accountsVectorTable != null) {
						for (int k = 0; k < accountsVectorTable.size(); k++) {
							paramList = new ArrayList();
							Map<String, String> accountMap = accountsVectorTable
									.getRowTags(i);
							String accountid = accountMap
									.get("ACCOUNT_ACCOUNTID");
							// Check whether the account is active
							if (accountid != null) {
								HashMap<String, Object> accountsResult = getAccountService(
										env, accountid);

								if (accountsResult != null
										&& (((Boolean) accountsResult
												.get("CLOSED")).booleanValue() || ((Boolean) accountsResult
												.get("STOPPED")).booleanValue())) {
									continue;
								}
								// Check the account exists in IBIAccounts table

								HashMap<String, Object> ibiAccountResult = readIBAccount(
										env, accountid);

								paramList.add(productContextCode);
								paramList.add(accountid);

								List<IBOAccount> accountList = factory
										.findByQuery(IBOAccount.BONAME,
												ACCOUNT_WHERE_CLAUSE,
												paramList, null, false);

								IBOAccount objAccount = null;

								if (!accountList.isEmpty()) {
									objAccount = accountList.get(0);
									accountid = objAccount.getBoID();// accountMap.get("ACCOUNT_ACCOUNTID");
									raiseEvent(env, accountid, customerNo,
											objAccount, isEnable);

								}

							}
						}
					}
				}
			}
		}

		enableDisableSubProducts(factory, selectedSubProducts, isEnable);

	}

	private HashMap<String, Object> readKYCStatus(BankFusionEnvironment env,
			String customerNo) {

		HashMap params = new HashMap<String, String>();
		params.put("CustomerCode", customerNo);

		return MFExecuter.executeMF(READ_KYC_STATUS_SRV, env, params);
	}

	private HashMap<String, VectorTable> getCustomerAccounts(
			BankFusionEnvironment env, String customerNo) {

		HashMap params = new HashMap<String, String>();
		params.put("CUSTOMERCODE", customerNo);

		return MFExecuter.executeMF(GET_CUSTOMER_ACCOUNTS_SRV, env,
				params);
	}

	private HashMap<String, Object> getAccountService(
			BankFusionEnvironment env, String accountid) {

		HashMap params = new HashMap<String, String>();
		params.put("ACCOUNTNUM", accountid);
		return MFExecuter.executeMF(FETCH_ACCOUNT_SERVICE_SRV, env, params);
	}

	private HashMap<String, Object> readIBAccount(BankFusionEnvironment env,
			String accountid) {

		HashMap params = new HashMap<String, String>();
		params.put("ACCOUNTID", accountid);
		return MFExecuter.executeMF(READ_IB_Account_SRV, env, params);
	}

	private void raiseEvent(BankFusionEnvironment env, String accountid,
			String customerNo, IBOAccount objAccount, Boolean isEnable) {
		HashMap<String, String> paramsEnableSubProductEvents = new HashMap<String, String>();
		HashMap<String, String> paramsDisableSubProductEvents = new HashMap<String, String>();
		paramsEnableSubProductEvents.put("ACCOUNTID", accountid);
		paramsEnableSubProductEvents.put("CURRENCY", objAccount
				.getF_ISOCURRENCYCODE());
		paramsEnableSubProductEvents.put("CURRENCYCODE", objAccount
				.getF_ISOCURRENCYCODE());
		paramsEnableSubProductEvents.put("PRODUCTID", objAccount
				.getF_PRODUCTID());
		paramsEnableSubProductEvents.put("ACCOUNTNAME", objAccount
				.getF_ACCOUNTNAME());
		paramsEnableSubProductEvents.put("CUSTOMERNUMBER", customerNo);
		paramsDisableSubProductEvents.put("KEY1", "ACCOUNTNO");
		paramsDisableSubProductEvents.put("VALUE1", accountid);

		if (isEnable) {
			/*
			 * EVENTNUMBER 40412008 is for enablement EVENTNUMBER 40411039 is
			 * for disablement
			 */
			paramsEnableSubProductEvents.put("EVENTNUMBER", "40412008");
			MFExecuter.executeMF(RAISE_ENABLE_ACCOUNY_EVENT_SRV, env,
					paramsEnableSubProductEvents);
		} else {
			paramsDisableSubProductEvents.put("EVENTNUMBER", "40411039");
			MFExecuter.executeMF(RAISE_EVENT_SRV, env,
					paramsDisableSubProductEvents);
		}
	}

	private void enableDisableSubProducts(IPersistenceObjectsFactory factory,
			VectorTable selectedSubProducts, Boolean isEnable) {
		IBOUB_IBI_EnabledSubProducts iBIEnableDisableSubProductsObject = null;

		Iterator<IBOUB_IBI_EnabledSubProducts> iBIEnableDisableSubProductsListIterator = null;
		for (int i = 0; i < selectedSubProducts.size(); i++) {
			ArrayList paramList = new ArrayList();
			String productContextCode = (String) (selectedSubProducts
					.getRowTags(i))
					.get("PRODUCTINHERITENCE_PRODUCTCONTEXTCODE");
			paramList.add(productContextCode);
			if (isEnable) {
				paramList.add(Boolean.FALSE);
			} else {
				paramList.add(Boolean.TRUE);
			}
			ArrayList iBIEnableDisableSubProductsList = (ArrayList) factory
					.findByQuery(IBOUB_IBI_EnabledSubProducts.BONAME,
							ENABLED_SUB_PRODUCTS_WHERE_CLAUSE, paramList, null,
							false);
			iBIEnableDisableSubProductsListIterator = iBIEnableDisableSubProductsList
					.iterator();
			if (iBIEnableDisableSubProductsListIterator.hasNext()) {

				iBIEnableDisableSubProductsObject = iBIEnableDisableSubProductsListIterator
						.next();
				if (isEnable) {
					iBIEnableDisableSubProductsObject
							.setF_ISIBIENABLED(Boolean.TRUE);
				} else {
					iBIEnableDisableSubProductsObject
							.setF_ISIBIENABLED(Boolean.FALSE);
				}
			} else {
				if (isEnable) {
					IBOUB_IBI_EnabledSubProducts ibiEnableDisableSubProducts = (IBOUB_IBI_EnabledSubProducts) factory
							.getStatelessNewInstance(IBOUB_IBI_EnabledSubProducts.BONAME);
					ibiEnableDisableSubProducts.setBoID(productContextCode);
					ibiEnableDisableSubProducts.setF_ISIBIENABLED(Boolean.TRUE);
					ibiEnableDisableSubProducts.setVersionNum(0);

					factory.create(IBOUB_IBI_EnabledSubProducts.BONAME,
							ibiEnableDisableSubProducts);
				}
			}
		}
	}
}