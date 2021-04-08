/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Trapedza Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************/
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_CustomerExternalReferences;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_IBI_IfmCurrency;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_EnableProductCurrencyFatom;

/** */
public class UB_IBI_EnableProductCurrencyFatom extends AbstractUB_IBI_EnableProductCurrencyFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

    /**
     */

    private transient final static Log logger = LogFactory.getLog(UB_IBI_EnableProductCurrencyFatom.class.getName());

    /**
     * @param env
     */
    public UB_IBI_EnableProductCurrencyFatom(BankFusionEnvironment env) {
        super(env);
    }

    // fetches the list of IBI enabled customers
    private static final String ibiEnabledCustomers = "WHERE " + IBOUB_CNF_CustomerExternalReferences.UBISACTIVE + " = 'Y' ";

    // fetches the list of IBI enabled currencies
    private static final String ifmEnabledCurrency = "WHERE " + IBOUB_IBI_IfmCurrency.ISIFMENABLED + " = 'Y' ";

    @Override
    public void process(BankFusionEnvironment env) {
        HashMap<String, String> params = null;
        HashMap<String, Object> params1 = null;
        String newlyAddedProducts = getF_IN_NEWLYADDEDPRODUCTS();
        String newlyAddedCurrencies = getF_IN_NEWLYADDEDCURRENCIES();

        // ArrayList<String> iBIEnabledCurrencies = (ArrayList)
        // env.getFactory().findByQuery(IBOUB_CNF_CustomerExternalReferences.BONAME,
        // ibiEnabledCustomers, null, false);
        ArrayList iBIEnabledCurrencies = (ArrayList) env.getFactory().findByQuery(IBOUB_IBI_IfmCurrency.BONAME, ifmEnabledCurrency,
                null, false);
        ArrayList iBIEnabledCustomersList = (ArrayList) env.getFactory().findByQuery(IBOUB_CNF_CustomerExternalReferences.BONAME,
                ibiEnabledCustomers, null, false);
        Iterator<IBOUB_CNF_CustomerExternalReferences> iBIEnabledCustomersListIterator = iBIEnabledCustomersList.iterator();
        while (iBIEnabledCustomersListIterator.hasNext()) {

            IBOUB_CNF_CustomerExternalReferences customerExternalReferencesObject = iBIEnabledCustomersListIterator.next();
            String customerNo = customerExternalReferencesObject.getF_CUSTOMERCODE();
            // Check whether the customer is blacklisted or not UB_CNF_ReadKYCStatus_SRV
            params = new HashMap<String, String>();
            params.put("CustomerCode", customerNo);
            HashMap<String, Object> kycStatusResult = MFExecuter.executeMF("UB_CNF_ReadKYCStatus_SRV", env, params);
            if ((Boolean) kycStatusResult.get("continue")) { // ADD
                // Fetch the list of Accounts for the customer
                params = new HashMap<String, String>();
                params.put("CUSTOMERCODE", customerNo);
                HashMap<String, VectorTable> result = MFExecuter.executeMF("UB_GetCustomerAccounts_SRV", env, params);
                VectorTable accountsVectorTable = result.get("CUSTOMER_ACCOUNTS");
                for (int i = 0; i < accountsVectorTable.size(); i++) {
                    Map<String, String> accountMap = accountsVectorTable.getRowTags(i);
                    String accountid = accountMap.get("ACCOUNT_ACCOUNTID");
                    // Check whether the account is active
                    params = new HashMap<String, String>();
                    params.put("ACCOUNTNUM", accountid);
                    HashMap<String, Object> accountsResult = MFExecuter.executeMF("UB_CMN_FetchAccountService", env, params);
                    if (((Boolean) accountsResult.get("CLOSED")).booleanValue()
                            || ((Boolean) accountsResult.get("STOPPED")).booleanValue()) {
                        continue;
                    }
                    // Check the account exists in IBIAccounts table
                    params = new HashMap<String, String>();
                    params.put("ACCOUNTID", accountid);
                    HashMap<String, Object> ibiAccountResult = MFExecuter.executeMF("UB_IBI_ReadIBAccount_SRV", env, params);

                    if (ibiAccountResult.get("ACCOUNTID") == "" || ibiAccountResult.get("ACCOUNTID") == null || ((Boolean)ibiAccountResult.get("ISACTIVE")).booleanValue()== false) {
                        // checked whether product IFM enable? WhatProductDefaults
                        params = new HashMap<String, String>();
                        String productId = (String) accountsResult.get("PRODUCTID");
                        params.put("productid", productId);
                        HashMap<String, Boolean> featureExistanceResult = MFExecuter.executeMF("WhatProductDefaults", env, params);
                        if (featureExistanceResult.get("HASIBIFEATURE")) {
                            // Fetch the List of IBI Enabled
                            // CurrenciesUB_IBI_FindAllIfmCurrencies_SRV
                            /*
                             * params1 = new HashMap<String, Object>(); params1.put("ISIFMENABLED",
                             * (Boolean)true); HashMap<String, VectorTable> ibiEnabledCurrencies =
                             * MFExecuter.executeMF("UB_IBI_FindAllIfmCurrencies_SRV", env, params);
                             */
                            Iterator<IBOUB_IBI_IfmCurrency> itrUB_IBI_IfmCurrency = iBIEnabledCurrencies.iterator();
                            List<String> ibiEnabledCurrencyList = new ArrayList<String>();
                            while (itrUB_IBI_IfmCurrency.hasNext()) {
                                ibiEnabledCurrencyList.add(itrUB_IBI_IfmCurrency.next().getBoID());
                            }
                            if (ibiEnabledCurrencyList.contains((String) accountsResult.get("ISOCURRENCYCODE")))
                            // if(isIBIEnabledCurrency((String)accountsResult.get("ISOCURRENCYCODE"),iBIEnabledCurrencies))
                            {
                                // if (isProductExists(productId, newlyAddedProducts) ||
                                // isCurrencyExists((String)accountsResult.get("ISOCURRENCYCODE"),
                                // newlyAddedCurrencies)) {
                                if (productId.equals(newlyAddedProducts)
                                        || ((String) accountsResult.get("ISOCURRENCYCODE")).equals(newlyAddedCurrencies)) {
                                    // Raise event.......... UB_IBI_Raise_AccountEvent
                                    params = new HashMap<String, String>();
                                    params.put("ACCOUNTID", accountid);
                                    params.put("CURRENCY", (String) accountsResult.get("ISOCURRENCYCODE"));
                                    params.put("CURRENCYCODE", (String) accountsResult.get("ISOCURRENCYCODE"));
                                    params.put("PRODUCTID", productId);
                                    params.put("EVENTNUMBER", "40412008");
                                    params.put("ACCOUNTNAME", (String) accountsResult.get("ACCOUNTNAME"));
                                    params.put("CUSTOMERNUMBER", customerNo);
                                    MFExecuter.executeMF("UB_IBI_Raise_AccountEvent", env, params);

                                }
                            }
                            else {
                                params1 = new HashMap<String, Object>();
                                params1.put("ISOCURRENCYCODE", (String) accountsResult.get("ISOCURRENCYCODE"));
                                EventsHelper.handleEvent(ChannelsEventCodes.I_NOTIBICURRENCY,
                                        new Object[] { (String) accountsResult.get("ISOCURRENCYCODE") }, params1, env);
                            }

                        }
                        else {
                            params1 = new HashMap<String, Object>();
                            params1.put("productid", productId);
                            EventsHelper.handleEvent(ChannelsEventCodes.I_NOTIBIPRODUCT, new Object[] { productId }, params1, env);
                        }
                    }
                }
            }
        }

    }

    /**
     * 
     * @param productId
     * @return
     */
    private boolean isProductExists(Object productId, VectorTable newlyAddedProducts) {
        if (!newlyAddedProducts.hasData()) {
            return false;
        }
        List<Object> list = Arrays.asList(newlyAddedProducts.getColumn("PRODUCT_PRODUCTNAME"));
        return list.contains(productId);
    }

    /**
     * 
     * @param productId
     * @return
     */
    private boolean isCurrencyExists(Object currency, VectorTable newlyAddedCurrenies) {

        if (!newlyAddedCurrenies.hasData()) {
            return false;
        }
        List<Object> list = Arrays.asList(newlyAddedCurrenies.getColumn("BOID"));
        return list.contains(currency);
    }
    /**
     * 
     * @param productId
     * @return
     */
    /*
     * private boolean isIBIEnabledCurrency(Object currency, VectorTable enabledCurrencies) {
     * List<Object> list = Arrays.asList(enabledCurrencies.getColumn("ISOCURRENCYCODE")); return
     * list.contains(currency); }
     */
}
