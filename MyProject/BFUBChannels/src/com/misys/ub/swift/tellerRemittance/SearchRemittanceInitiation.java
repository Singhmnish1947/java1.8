package com.misys.ub.swift.tellerRemittance;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.common.util.ApiUtil;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.cbs.common.util.DateUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.payment.swift.DBUtils.DBUtils;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceMessage;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.RoundToScale;
import com.trapedza.bankfusion.steps.refimpl.AbstractSearchRemittanceInitiation;

import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.bankfusion.attributes.PagingRequest;
import bf.com.misys.cbs.msgs.v1r0.SearchRemittanceDtlsRq;
import bf.com.misys.cbs.msgs.v1r0.SearchRemittanceDtlsRs;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.swift.SearchRemittanceDetail;
import bf.com.misys.cbs.types.swift.SearchRemittanceDtlsOutput;

/**
 * @author Machamma.Devaiah
 *
 */
public class SearchRemittanceInitiation extends AbstractSearchRemittanceInitiation {

    private transient static final Log LOGGER = LogFactory.getLog(SearchRemittanceInitiation.class);
    private RsHeader rsHeaderObj = null;
    private MessageStatus msgStatus = null;
    private SearchRemittanceDtlsRs resultObject = null;
    private boolean status = Boolean.TRUE;
    private static final String LIKE_CLAUSE = " LIKE ?";
    private static final String REMITTANCEID_WILDCARD_SEARCH = DBUtils.AND + " a." + IBOUB_SWT_RemittanceMessage.UBREMITTANCEIDPK
            + LIKE_CLAUSE;
    private static final String REMITTANCEID_EXACT_SEARCH = DBUtils.AND + " a." + IBOUB_SWT_RemittanceMessage.UBREMITTANCEIDPK
            + DBUtils.QUERY_PARAM;

    private static final String UBDEBITACCOUNT_WILDCARD_SEARCH = DBUtils.AND + " a." + IBOUB_SWT_RemittanceMessage.UBDEBITACCOUNT
            + LIKE_CLAUSE;
    private static final String UBDEBITACCOUNT_EXACT_SEARCH = DBUtils.AND + " a." + IBOUB_SWT_RemittanceMessage.UBDEBITACCOUNT
            + DBUtils.QUERY_PARAM;

    private static final String UBSETTLEMENTAMTCURRENCY_EXACT_SEARCH = DBUtils.AND + " a."
            + IBOUB_SWT_RemittanceMessage.UBSETTLEMENTAMTCURRENCY + DBUtils.QUERY_PARAM;

    private static final String UBSENDERREFERENCE_WILDCARD_SEARCH = DBUtils.AND + " a."
            + IBOUB_SWT_RemittanceMessage.UBSENDERREFERENCE + LIKE_CLAUSE;
    private static final String UBSENDERREFERENCE_EXACT_SEARCH = DBUtils.AND + " a." + IBOUB_SWT_RemittanceMessage.UBSENDERREFERENCE
            + DBUtils.QUERY_PARAM;

    private static final String VALUE_DATE_ORDER_BY_CLAUSE = " ORDER BY a." + IBOUB_SWT_RemittanceMessage.UBREMITTANCEIDPK
            + " DESC";

    private String genericQuery = DBUtils.SELECT + " a." + IBOUB_SWT_RemittanceMessage.UBREMITTANCEIDPK + " as remittanceId, a."
            + IBOUB_SWT_RemittanceMessage.UBPAYMENTMETHOD + " as paymentMethod, a."
            + IBOUB_SWT_RemittanceMessage.UBMESSAGEPREFERENCE + " as messagePreference, a."
            + IBOUB_SWT_RemittanceMessage.UBDEBITACCOUNT + " as accountId, a." + IBOUB_SWT_RemittanceMessage.UBINSTRUCTEDAMT
            + " as amount, a." + IBOUB_SWT_RemittanceMessage.UBINSTRUCTEDAMTCURRENCY + " as currency, a."
            + IBOUB_SWT_RemittanceMessage.UBREMITTANCESTATUS + " as remittanceStatus, a." + IBOUB_SWT_RemittanceMessage.UBVALUEDATE
            + " as valueDate " + DBUtils.FROM + IBOUB_SWT_RemittanceMessage.BONAME + "  a  " + DBUtils.WHERE + "UB_TODATE(a."
            + IBOUB_SWT_RemittanceMessage.UBVALUEDATE + ") >=  ?  " + DBUtils.AND + "UB_TODATE(a."
            + IBOUB_SWT_RemittanceMessage.UBVALUEDATE + ") <= ? ";

    @SuppressWarnings("deprecation")
    public SearchRemittanceInitiation(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) {
        SearchRemittanceDtlsRq searchRq = getF_IN_searchRemittanceRq();

        PagingData pagingData = new PagingData(searchRq.getPagedQuery().getPagingRequest().getRequestedPage(),
                searchRq.getPagedQuery().getPagingRequest().getNumberOfRows());
        pagingData.setRequiresTotalPages(true);

        if (null != searchRq.getSearchRemittanceDtlsInput()) {
            executeQuery(searchRq, pagingData);
        }
        else {
            status = false;
        }

        if (status) {
            rsHeaderObj = new RsHeader();
            msgStatus = new MessageStatus();
            msgStatus.setOverallStatus("S");
            rsHeaderObj.setStatus(msgStatus);
        }
        else {
            rsHeaderObj = new RsHeader();
            msgStatus = new MessageStatus();
            msgStatus.setOverallStatus("E");
            rsHeaderObj.setStatus(msgStatus);
        }

        if (resultObject != null) {
            resultObject.setRsHeader(rsHeaderObj);
        }
        else {
            resultObject = getF_OUT_searchRemittanceRs();
            resultObject.setRsHeader(rsHeaderObj);
        }

        PagingRequest pagingRequest = new PagingRequest();
        pagingRequest.setNumberOfRows(searchRq.getPagedQuery().getPagingRequest().getNumberOfRows());
        pagingRequest.setRequestedPage(searchRq.getPagedQuery().getPagingRequest().getRequestedPage());
        pagingRequest.setTotalPages(pagingData.getTotalPages());
        PagedQuery pagedQuery = searchRq.getPagedQuery();
        pagedQuery.setPagingRequest(pagingRequest);
        resultObject.setPagedQuery(pagedQuery);
        setF_OUT_searchRemittanceRs(resultObject);
    }

    /**
     * Method Description:Execute the generic query
     * 
     * @param searchRq
     * @param pagingData
     */
    private void executeQuery(SearchRemittanceDtlsRq searchRq, PagingData pagingData) {
        String remittanceId = searchRq.getSearchRemittanceDtlsInput().getRemittanceID();
        String accountNumber = searchRq.getSearchRemittanceDtlsInput().getAccountNumber().getStandardAccountId();
        String remittanceCurrency = searchRq.getSearchRemittanceDtlsInput().getRemittanceCurrency();
        String senderReference = searchRq.getSearchRemittanceDtlsInput().getSenderReference();
        Date fromDate = searchRq.getSearchRemittanceDtlsInput().getFromDate();
        Date toDate = searchRq.getSearchRemittanceDtlsInput().getToDate();
        List<SimplePersistentObject> resultSet = null;

        validateDate(fromDate, toDate);

        // query parameters
        ArrayList<Object> queryparams = new ArrayList<>();
        queryparams.add(fromDate);
        queryparams.add(toDate);

        if (!StringUtils.isBlank(remittanceId)) {
            queryparams.add(remittanceId);
            if (remittanceId.contains("%")) {
                genericQuery = genericQuery + REMITTANCEID_WILDCARD_SEARCH;
            }
            else {
                genericQuery = genericQuery + REMITTANCEID_EXACT_SEARCH;
            }
        }

        if (!StringUtils.isBlank(accountNumber)) {
            queryparams.add(accountNumber);
            if (accountNumber.contains("%")) {
                genericQuery = genericQuery + UBDEBITACCOUNT_WILDCARD_SEARCH;
            }
            else {
                genericQuery = genericQuery + UBDEBITACCOUNT_EXACT_SEARCH;
            }
        }

        if (!StringUtils.isBlank(senderReference)) {
            queryparams.add(senderReference);
            if (senderReference.contains("%")) {
                genericQuery = genericQuery + UBSENDERREFERENCE_WILDCARD_SEARCH;
            }
            else {
                genericQuery = genericQuery + UBSENDERREFERENCE_EXACT_SEARCH;
            }
        }

        if (!StringUtils.isBlank(remittanceCurrency)) {
            queryparams.add(remittanceCurrency);
            genericQuery = genericQuery + UBSETTLEMENTAMTCURRENCY_EXACT_SEARCH;
        }

        genericQuery = genericQuery + VALUE_DATE_ORDER_BY_CLAUSE;

        if (LOGGER.isInfoEnabled())
            LOGGER.info("Generic query::: " + genericQuery);

        // exceute the generic query
        try {
            IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
            resultSet = factory.executeGenericQuery(genericQuery, queryparams, pagingData, false);

            int count = 0;
            resultObject = getF_OUT_searchRemittanceRs();
            SearchRemittanceDtlsOutput searchOutput = new SearchRemittanceDtlsOutput();
            for (SimplePersistentObject currentrow : resultSet) {
                SearchRemittanceDetail vSearchRemittanceDetails = new SearchRemittanceDetail();
                vSearchRemittanceDetails.setRemittanceId(currentrow.getDataMap().get("remittanceId").toString());
                vSearchRemittanceDetails.setPaymentMethod(ApiUtil.getGenericCodeDesc(RemittanceConstants.TELREM_PAYMETHOD_GC,
                        currentrow.getDataMap().get("paymentMethod").toString()));
                vSearchRemittanceDetails.setMessagePreference(ApiUtil.getGenericCodeDesc(RemittanceConstants.MSGPREFERENCE_GC,
                        currentrow.getDataMap().get("messagePreference").toString()));
                vSearchRemittanceDetails.setAccountId(currentrow.getDataMap().get("accountId").toString());
                Currency amount = new Currency();
                String instructedCurrency = currentrow.getDataMap().get("currency").toString();
                BigDecimal instructedAmount = new BigDecimal(currentrow.getDataMap().get("amount").toString());
                amount.setAmount(RoundToScale.run(instructedAmount, instructedCurrency));
                amount.setIsoCurrencyCode(instructedCurrency);
                vSearchRemittanceDetails.setAmount(amount);
                vSearchRemittanceDetails.setRemittanceStatus(ApiUtil.getGenericCodeDesc(RemittanceConstants.TELREM_REMIT_STATUS_GC,
                        currentrow.getDataMap().get("remittanceStatus").toString()));

                java.sql.Date valueDate = (java.sql.Date) currentrow.getDataMap().get("valueDate");
                vSearchRemittanceDetails.setValueDate(valueDate);
                vSearchRemittanceDetails.setSelect(Boolean.FALSE);
                searchOutput.addSearchRemittanceDetails(vSearchRemittanceDetails);
                count++;
            }

            resultObject.setSearchRemittanceDtlsOutput(searchOutput);

            if (count == 0) {
                status = false;
            }

        }
        catch (Exception e) {
            status = false;
            LOGGER.error(ExceptionUtil.getExceptionAsString(e));
            CommonUtil.handleParameterizedEvent(471000011, new String[] {});
        }
    }

    /**
     * Method Description: Validate Date
     * 
     * @param fromDate
     * @param toDate
     */
    private void validateDate(Date fromDate, Date toDate) {
        Timestamp businessDate = SystemInformationManager.getInstance().getBFBusinessDateTime();

        if (DateUtil.getStaticDateForDate(fromDate).after(DateUtil.getStaticDateForDate(toDate))) {
            CommonUtil.handleParameterizedEvent(40000304, new String[] {});
        }
       /* else if (DateUtil.getStaticDateForDate(fromDate).after(DateUtil.getStaticDateForDate(businessDate))) {
            CommonUtil.handleParameterizedEvent(40000401, new String[] {});
        }
        else if (DateUtil.getStaticDateForDate(toDate).after(DateUtil.getStaticDateForDate(businessDate))) {
            CommonUtil.handleParameterizedEvent(40180104, new String[] {});
        }*/
    }
}
