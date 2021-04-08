/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.cc.restServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.cc.types.AccountDetailsOverview;
import com.misys.ub.cc.types.SearchAccountInterfaceRq;
import com.misys.ub.cc.types.SearchAccountInterfacesRs;
import com.misys.ub.cc.utils.SearchAccountInterfaceConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

import bf.com.misys.cbs.msgs.v1r0.TermDepositOverviewRequest;
import bf.com.misys.cbs.msgs.v1r0.TermDepositOverviewResponse;
import bf.com.misys.cbs.types.TermDepositOverview;
import bf.com.misys.cbs.types.TermDepositOverviewInput;
import jxl.common.Logger;

@RestController
@RequestMapping("/fetchAccountDetails")
public class FetchCustomerAccountDetails {

    private static final String FIND_BY_CUSTOMER_WHERE_CLAUSE = CommonConstants.WHERE + IBOAccount.CUSTOMERCODE + " = ? ";
    private static final String CUSTOMER_REFERENCE_TYPE = "CUSTOMER";
    private static final String ACCOUNT_REFERENCE_TYPE = "ACCOUNT";

    private static final Logger logger = Logger.getLogger(FetchCustomerAccountDetails.class);

    @SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
    @RequestMapping(value = "/getDetails", method = RequestMethod.POST, headers = "Accept=application/json")
    public SearchAccountInterfacesRs process(@RequestBody SearchAccountInterfaceRq req) {

        BankFusionThreadLocal.setFbpService(false);

        ArrayList<String> param = new ArrayList<>();

        List<IBOAccount> accounts = null;
        SearchAccountInterfacesRs response = new SearchAccountInterfacesRs();
        ArrayList<AccountDetailsOverview> accountList = new ArrayList<>();

        TermDepositOverviewResponse termDepositOverviewRes = null;
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

        try {

            factory.beginTransaction();
            String referenceType = (StringUtils.isBlank(req.getCustomerId()) && StringUtils.isNotBlank(req.getAccountId()))
                    ? ACCOUNT_REFERENCE_TYPE
                    : CUSTOMER_REFERENCE_TYPE;

            String customerId = StringUtils.isNotBlank(req.getCustomerId()) ? req.getCustomerId() : StringUtils.EMPTY;
            switch (referenceType) {
                case CUSTOMER_REFERENCE_TYPE:
                    // customer where clause
                    param.add(req.getCustomerId());
                    accounts = factory.findByQuery(IBOAccount.BONAME, FIND_BY_CUSTOMER_WHERE_CLAUSE, param, null);

                    for (IBOAccount account : accounts) {
                        accountList = updateAccountDetails(accountList, account.getBoID(), customerId);
                    }

                    break;

                case ACCOUNT_REFERENCE_TYPE:
                    // account where clause
                    IBOAccount accountBO = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, req.getAccountId(), true);
                  
                    if (accountBO!=null && StringUtils.isNotEmpty(accountBO.getBoID())) {
                        accountList = updateAccountDetails(accountList, accountBO.getBoID(), customerId);
                    }

                    break;

                default:
                    break;
            }
            TermDepositOverviewRequest termDepOverviewReq = new TermDepositOverviewRequest();
            TermDepositOverviewInput termDepOverviewInput = new TermDepositOverviewInput();
            termDepOverviewInput.setCustomerId(customerId);
            termDepOverviewReq.setTermDepositOverviewInput(termDepOverviewInput);

            HashMap<String, Object> inputParams1 = new HashMap<>();
            inputParams1.put(SearchAccountInterfaceConstants.TERM_DEP_OVERVIEW_REQUEST, termDepOverviewReq);
            FBPMicroflowServiceInvoker invoker1 = new FBPMicroflowServiceInvoker(
                    SearchAccountInterfaceConstants.TERM_DEP_OVERVIEW_SERVICE);
            HashMap outputParams1 = invoker1.invokeMicroflow(inputParams1, false);
            termDepositOverviewRes = (TermDepositOverviewResponse) outputParams1
                    .get(SearchAccountInterfaceConstants.TERM_DEP_OVERVIEW_RESPONSE);

            if (termDepositOverviewRes != null
                    && termDepositOverviewRes.getTermDepositOverviewResponse().getTermDepositOverview().length != 0) {
                for (TermDepositOverview termDepOverview : termDepositOverviewRes.getTermDepositOverviewResponse()
                        .getTermDepositOverview()) {
                    for (AccountDetailsOverview accDtlsOverview : accountList) {
                        if (termDepOverview.getTermDepositAccount().getExternalAccountNumber().equals(accDtlsOverview
                                .getExtensiveAccountDetails().getAccountBasicDetails().getAccountKeys().getStandardAccountId())) {
                            accDtlsOverview.getExtensiveAccountDetails().setTermDepositOverview(termDepOverview);
                        }
                    }
                }
            }

            factory.commitTransaction();

        }

        catch (Exception ex) {
            logger.error(ExceptionUtil.getExceptionAsString(ex));
            factory.rollbackTransaction();
        }

        response.setAccountDetailsOverviewList(accountList);
        return response;
    }

    /**
     * @param accounts
     * @param customerId
     * @return
     */
    private ArrayList<AccountDetailsOverview> updateAccountDetails( ArrayList<AccountDetailsOverview> accountList , String accountId, String customerId) {
        SearchAccountInterfacesRs res = null;
        SearchAccountInterfaceRq request = new SearchAccountInterfaceRq();
        SearchAccountInterfaceService service = new SearchAccountInterfaceService();
        request.setAccountId(accountId);
        request.setCustomerId(customerId);
        try {
            res = service.update(request);
            accountList.addAll(res.getAccountDetailsOverviewList());
        }
        catch (Exception e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
            throw e;
        }

        return accountList;
    }
}
