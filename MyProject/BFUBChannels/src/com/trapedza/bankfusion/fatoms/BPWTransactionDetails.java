/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BPWTransactionDetails.java,v.1.0,14 Feb 2012 17:05:07 Gaurav.Aggarwal
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOCreditInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBODebitInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOUBVW_FORWARDITEMS;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_BPW_TransactionDetails;

/**
 * @author Gaurav.Aggarwal
 * @date 14 Feb 2012
 * @project Universal Banking
 * @Description: This class is used to provide the transaction details from BPW.
 */

public class BPWTransactionDetails extends AbstractUB_BPW_TransactionDetails {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(BPWTransactionDetails.class.getName());

    public BPWTransactionDetails(BankFusionEnvironment env) {
        super(env);
    }

    private static String whereClause = "SELECT TXN." + IBOUBVW_FORWARDITEMS.ACCOUNTPRODUCT_ACCPRODID
            + " AS ACCOUNTPRODUCT_ACCPRODID , TXN." + IBOUBVW_FORWARDITEMS.REFERENCE + " AS REFERENCE , TXN."
            + IBOUBVW_FORWARDITEMS.NARRATION + " AS NARRATION , TXN." + IBOUBVW_FORWARDITEMS.CODE + " AS CODE , TXN."
            + IBOUBVW_FORWARDITEMS.ISOCURRENCYCODE + " AS ISOCURRENCYCODE , TXN." + IBOUBVW_FORWARDITEMS.VALUEDATE
            + " AS VALUEDATE , TXN." + IBOUBVW_FORWARDITEMS.POSTINGDATE + " AS POSTINGDATE , TXN." + IBOUBVW_FORWARDITEMS.AMOUNT
            + " AS AMOUNT, TXN." + IBOUBVW_FORWARDITEMS.USERID + " AS USERID, TXN." + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER
            + " AS TRANSACTIONCOUNTER FROM " + IBOUBVW_FORWARDITEMS.BONAME + " TXN WHERE TXN."
            + IBOUBVW_FORWARDITEMS.ACCOUNTPRODUCT_ACCPRODID + " = ? AND ";
    int minTransactionCounter = 0;
    int maxTransactionCounter = 0;
    private static final String minMaxClause = " SELECT MIN(" + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + ") AS MINIMUM , MAX("
            + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + ") AS MAXIMUM FROM " + IBOUBVW_FORWARDITEMS.BONAME + " WHERE "
            + IBOUBVW_FORWARDITEMS.ACCOUNTPRODUCT_ACCPRODID + " = ? ";
    private static final String creditInterestClause = " WHERE " + IBOCreditInterestFeature.ACCOUNTID + " = ?";
    private static final String debitInterestClause = " WHERE " + IBODebitInterestFeature.ACCOUNTID + " = ?";

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_BPW_TransactionDetails
     * #process(com.trapedza .bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {

        String accountId = getF_IN_AccountId();
        String query = CommonConstants.EMPTY_STRING;
        int transactionNumber = getF_IN_TransactionNumber().intValue();
        int nextOrPrev = getF_IN_NextOrPrev().intValue();
        ArrayList params = new ArrayList();
        IBOAccount accountDetails = (IBOAccount) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOAccount.BONAME,
                accountId, true);
        if (accountDetails.getBoID() == null || accountDetails.getBoID().equals(CommonConstants.EMPTY_STRING)) {
            EventsHelper.handleEvent(40205081, new Object[] {}, new HashMap(), env);
            return;
        }
        getMinMaxTransactionCounter(accountId);
        if (nextOrPrev == 3) {
            params.add(accountId);
            if (transactionNumber <= minTransactionCounter) {
                query = whereClause + " TXN." + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " = ? ORDER BY TXN."
                        + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " ASC";
                params.add(minTransactionCounter);
            }
            else {
                query = whereClause + " TXN." + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " < ? ORDER BY TXN."
                        + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " DESC";
                params.add(transactionNumber);
            }
        }
        else if (nextOrPrev == 0) {
            query = whereClause + " TXN." + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " = ? ORDER BY TXN."
                    + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " ASC";
            params.add(accountId);
            if (transactionNumber < minTransactionCounter)
                params.add(minTransactionCounter);
            else if (transactionNumber > maxTransactionCounter)
                params.add(maxTransactionCounter);
            else params.add(getF_IN_TransactionNumber());
        }
        else {
            params.add(accountId);
            if (transactionNumber >= maxTransactionCounter) {
                query = whereClause + " TXN." + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " = ? ORDER BY TXN."
                        + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " ASC";
                params.add(maxTransactionCounter);
            }
            else {
                query = whereClause + " TXN." + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " > ? ORDER BY TXN."
                        + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " ASC";
                params.add(transactionNumber);
            }
        }
        IPagingData pagingData = new PagingData(1, 1);
        pagingData.setCurrentPageNumber(1);
        pagingData.setRequiresTotalPages(true);
        pagingData.setPageSize(1);
        List<SimplePersistentObject> resultSet = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(query, params,
                pagingData, false);
        populateOutputTags(resultSet, accountDetails);
    }

    /**
     * Method Description: This method is used to find out the minimum and maximum transaction
     * counter of account.
     * 
     * @param accountId
     */
    private void getMinMaxTransactionCounter(String accountId) {
        ArrayList params = new ArrayList();
        params.add(accountId);
        List<SimplePersistentObject> resultSet = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(minMaxClause,
                params, null, false);
        Map rowData = null;
        SimplePersistentObject row = (SimplePersistentObject) resultSet.get(0);
        rowData = row.getDataMap();
        minTransactionCounter = (Integer) rowData.get("MINIMUM");
        maxTransactionCounter = (Integer) rowData.get("MAXIMUM");
    }

    /**
     * Method Description: This method is used to populate the output details.
     * 
     * @param list
     * @param accountDetails
     */
    private void populateOutputTags(List<SimplePersistentObject> list, IBOAccount accountDetails) {
        ArrayList params = new ArrayList();
        params.add(accountDetails.getBoID());
        Map rowData = null;
        SimplePersistentObject row = (SimplePersistentObject) list.get(0);
        rowData = row.getDataMap();
        MISTransactionCodeDetails mistransDetails;
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo()).getMisTransactionCodeDetails((String) rowData
                .get("CODE"));
        IBOMisTransactionCodes codeDetails = mistransDetails.getMisTransactionCodes();
        IBOCustomer customerDetails = (IBOCustomer) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
                IBOCustomer.BONAME, accountDetails.getF_CUSTOMERCODE(), true);
        List<IBOCreditInterestFeature> creditInterestList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBOCreditInterestFeature.BONAME, creditInterestClause, params, null, true);
        IBOCreditInterestFeature creditInterest = creditInterestList.get(0);
        List<IBODebitInterestFeature> debitInterestList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBODebitInterestFeature.BONAME, debitInterestClause, params, null, true);
        IBODebitInterestFeature debitInterest = debitInterestList.get(0);
        setF_OUT_TRANSACTIONTYPE(codeDetails.getF_PREFIXSUFFIX().substring(0, 2));
        setF_OUT_ACCOUNTDESCRIPTION(accountDetails.getF_ACCOUNTDESCRIPTION());
        setF_OUT_ACCOUNTID(accountDetails.getBoID());
        setF_OUT_ACCOUNTRIGHTSINDICATOR(accountDetails.getF_ACCRIGHTSINDICATOR());
        setF_OUT_ACCRUEDINTEREST(creditInterest.getF_ACCDCRINTEREST().subtract(debitInterest.getF_DEBITACCDINTEREST()));
        setF_OUT_BRANCHSORTCODE(accountDetails.getF_BRANCHSORTCODE());
        setF_OUT_CUSTOMERCODE(accountDetails.getF_CUSTOMERCODE());
        setF_OUT_ISOCURRENCYCODE(accountDetails.getF_ISOCURRENCYCODE());
        setF_OUT_NARRATION((String) rowData.get("NARRATION"));
        setF_OUT_POSTDATE(new java.sql.Date(((Timestamp) rowData.get("POSTINGDATE")).getTime()));
        setF_OUT_SHORTNAME(customerDetails.getF_SHORTNAME());
        setF_OUT_TRANSACTIONAMOUNT((BigDecimal) rowData.get("AMOUNT"));
        setF_OUT_TRANSACTIONAMOUNTCURRENCY((String) rowData.get("ISOCURRENCYCODE"));
        setF_OUT_TRANSACTIONNUMBER((Integer) rowData.get("TRANSACTIONCOUNTER"));
        setF_OUT_TRANSACTIONREFERENCE((String) rowData.get("REFERENCE"));
        setF_OUT_USERID(((String) rowData.get("USERID")).substring(0,3));
        setF_OUT_VALUEDATE(new java.sql.Date(((Timestamp) rowData.get("VALUEDATE")).getTime()));
    }
}
