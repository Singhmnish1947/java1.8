package com.trapedza.bankfusion.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ACC_InternalAccountSearch;

import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.bankfusion.attributes.PagingRequest;
import bf.com.misys.cbs.msgs.v1r0.SearchIntAccountRq;
import bf.com.misys.cbs.msgs.v1r0.SearchIntAccountRs;
import bf.com.misys.cbs.types.IntAccountResDetail;
import bf.com.misys.cbs.types.IntAccountResDetails;

@SuppressWarnings("deprecation")
public class InternalAccountSearch extends AbstractUB_ACC_InternalAccountSearch {

	private static final String PSEUDONAME_COLUMN = "PSEUDONAME";
	private static final String ACCOUNTID_COLUMN = "ACCOUNTID";
	private static final String ISOCURRENCYCODE_COLUMN = "ISOCURRENCYCODE";
	private static final String ACCOUNTNAME_COLUMN = "ACCOUNTNAME";
	private static final String SORTCONTEXT_COLUMN = "SORTCONTEXT";
	private static final String SORTCONTEXTVALUE_COLUMN = "SORTCONTEXTVALUE";
	private static final String BRANCHNAME_COLUMN = "BRANCHNAME";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String FETCH_INTERNAL_ACCOUNTS = " SELECT T1.BRANCHSORTCODE , T3.ACCOUNTID AS "
			+ ACCOUNTID_COLUMN + ", T3.ACCOUNTNAME AS " + ACCOUNTNAME_COLUMN + " , " + " T2.PSEUDONAME AS "
			+ PSEUDONAME_COLUMN + " , T2.SORTCONTEXT AS " + SORTCONTEXT_COLUMN + ",  T2.SORTCONTEXTVALUE AS "
			+ SORTCONTEXTVALUE_COLUMN + ", T3.ISOCURRENCYCODE AS " + ISOCURRENCYCODE_COLUMN + " , T1.BRANCHNAME AS "
			+ BRANCHNAME_COLUMN + ",ROW_NUMBER() OVER(ORDER BY T3.ACCOUNTID) AS ROWSEQ "
			+ " FROM BRANCH T1   INNER JOIN ACCOUNT T3 ON T1.BRANCHSORTCODE = T3.BRANCHSORTCODE "
			+ " LEFT OUTER JOIN PSEUDONYMACCOUNTMAP T2 ON  T2.ACCOUNTID = T3.ACCOUNTID "
			+ " WHERE  COALESCE(T2.PSEUDONAME,'%') LIKE ? AND COALESCE(T2.SORTCONTEXT,'%') LIKE ? AND COALESCE(T2.SORTCONTEXTVALUE,'%') LIKE ? AND T3.ACCOUNTID LIKE ?  AND UPPER(T3.ACCOUNTNAME) LIKE ? "
			+ " AND   T3.ISOCURRENCYCODE LIKE ?  AND T3.BRANCHSORTCODE LIKE ? AND   EXISTS (SELECT 1 "
			+ " FROM UBTB_PRODUCTFEATURE PF "
			+ " WHERE PF.UBPRODUCT = T3.PRODUCTID "
			+ " AND   ((PF.UBFEATURE = ? AND PF.UBISFEATUREAVAILABLE = 'N') OR (PF.UBFEATURE = ? AND PF.UBISFEATUREAVAILABLE = 'Y')))  ORDER BY T3.ACCOUNTID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

	private static final String COUNT_QUERY = "SELECT COUNT(*) AS TOTALPAGES   "
			+ " FROM BRANCH T1   INNER JOIN ACCOUNT T3 ON T1.BRANCHSORTCODE = T3.BRANCHSORTCODE "
			+ " LEFT OUTER JOIN PSEUDONYMACCOUNTMAP T2 ON  T2.ACCOUNTID = T3.ACCOUNTID "
			+ " WHERE  COALESCE(T2.PSEUDONAME,'%') LIKE ? AND COALESCE(T2.SORTCONTEXT,'%') LIKE ? AND COALESCE(T2.SORTCONTEXTVALUE,'%') LIKE ? AND T3.ACCOUNTID LIKE ?  AND UPPER(T3.ACCOUNTNAME) LIKE ? "
			+ " AND   T3.ISOCURRENCYCODE LIKE ?  AND T3.BRANCHSORTCODE LIKE ? AND   EXISTS (SELECT 1 "
			+ " FROM UBTB_PRODUCTFEATURE PF "
			+ " WHERE PF.UBPRODUCT = T3.PRODUCTID "
			+ " AND   ((PF.UBFEATURE = ? AND PF.UBISFEATUREAVAILABLE = 'N') OR (PF.UBFEATURE = ? AND PF.UBISFEATUREAVAILABLE = 'Y'))) ";


	private List<Object> params = new ArrayList<>();
	private static final Log LOGGER = LogFactory.getLog(InternalAccountSearch.class.getName());

	public InternalAccountSearch(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * 
	 */
	public void process(BankFusionEnvironment env) {

		SearchIntAccountRq searchIntAccountRq = getF_IN_searchIntAccountRq();
		SearchIntAccountRs searchIntAccountRs = new SearchIntAccountRs();
		IntAccountResDetails intAccountResDetails = new IntAccountResDetails();
		String query = prepareQuery(searchIntAccountRq);
		Connection con = getConnection();
		try (PreparedStatement ps = con.prepareStatement(query)) {

			setParams(ps);

			try (ResultSet rs = ps.executeQuery()) {

				int i = 0;
				while (rs.next()) {

					IntAccountResDetail intAccountResDetail = new IntAccountResDetail();
					intAccountResDetail.setPseudonymId(rs.getString(PSEUDONAME_COLUMN));
					intAccountResDetail.setAccountId(rs.getString(ACCOUNTID_COLUMN));
					intAccountResDetail.setCurrency(rs.getString(ISOCURRENCYCODE_COLUMN));
					intAccountResDetail.setAccountName(rs.getString(ACCOUNTNAME_COLUMN));
					intAccountResDetail.setContextType(rs.getString(SORTCONTEXT_COLUMN));
					intAccountResDetail.setContextValue(rs.getString(SORTCONTEXTVALUE_COLUMN));
					intAccountResDetail.setBranchCode(rs.getString(BRANCHNAME_COLUMN));
					intAccountResDetail.setSelect(i == 0);
					intAccountResDetails.addIntAccountResDetail(intAccountResDetail);
					i++;

				}

				if (intAccountResDetails.getIntAccountResDetailCount() > 0) {
					PagingRequest pagingRequest = new PagingRequest();
					pagingRequest
							.setNumberOfRows(searchIntAccountRq.getPagedQuery().getPagingRequest().getNumberOfRows());
					pagingRequest
							.setRequestedPage(searchIntAccountRq.getPagedQuery().getPagingRequest().getRequestedPage());
					pagingRequest.setTotalPages(searchIntAccountRq.getPagedQuery().getPagingRequest().getTotalPages());
					PagedQuery pagedQuery = new PagedQuery();
					pagedQuery.setPagingRequest(pagingRequest);
					searchIntAccountRs.setPagedQuery(pagedQuery);
				}

				searchIntAccountRs.setPagedQuery(searchIntAccountRs.getPagedQuery());
				searchIntAccountRs.setIntAccountResDetails(intAccountResDetails);
				setF_OUT_searchIntAccountRs(searchIntAccountRs);
			}

		} catch (Exception e) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(e));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Number of Records Returned  " + intAccountResDetails.getIntAccountResDetailCount());
		}
		if (intAccountResDetails.getIntAccountResDetailCount() <= 0) {
			CommonUtil.handleUnParameterizedEvent(20020040);
		}
	}

	private static final String EXTERNAL_FETURE_ID = "UB_CMN_ExternalProductFeature";
	private static final String NOSTRO_FETURE_ID = "NostroFeature";

	/**
	 * 
	 * @param searchIntAccountRq
	 * @return
	 */
	private String prepareQuery(SearchIntAccountRq searchIntAccountRq) {

		String pseudonymId = searchIntAccountRq.getIntAccountDetails().getPseudonymId();
		String accountId = searchIntAccountRq.getIntAccountDetails().getAccountId();
		String accountName = searchIntAccountRq.getIntAccountDetails().getAccountName();
		String currency = searchIntAccountRq.getIntAccountDetails().getCurrency();
		String contextType = searchIntAccountRq.getIntAccountDetails().getContextType();
		String contextValue = searchIntAccountRq.getIntAccountDetails().getContextValue();
		String branchCode = searchIntAccountRq.getIntAccountDetails().getBranchCode();

		if (!(pseudonymId.equals("%") && accountId.equals("%") && accountName.equals("%") && currency.equals("%")
				&& contextType.equals("%") && contextValue.equals("%") && branchCode.equals("%"))) {

			params.add(pseudonymId);
			params.add(contextType);
			params.add(contextValue);
			params.add(accountId);
			params.add(accountName.toUpperCase());
			params.add(currency);
			params.add(branchCode);
			params.add(EXTERNAL_FETURE_ID);
			params.add(NOSTRO_FETURE_ID);

		} else {
			EventsHelper.handleEvent(20020238, new Object[] {}, null, BankFusionThreadLocal.getBankFusionEnvironment());
		}

		return addRowSeqForPagination(searchIntAccountRq);

	}

	/**
	 * 
	 * @param reqObj
	 * @return
	 */
	private String addRowSeqForPagination(SearchIntAccountRq reqObj) {
		int pageToProcess = reqObj.getPagedQuery().getPagingRequest().getRequestedPage();
		int pageSize = reqObj.getPagedQuery().getPagingRequest().getNumberOfRows();
		int totalPages = reqObj.getPagedQuery().getPagingRequest().getTotalPages();

		if (totalPages == 0) {
			int count = getTotalPages();
			count = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
			reqObj.getPagedQuery().getPagingRequest().setTotalPages(count);
		}

		int fromValue = ((pageToProcess - 1) * pageSize) + 1;
		int toValue = pageSize;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing Page from {" + fromValue + "} , to  {" + toValue + "} ");
		}
		params.add(fromValue-1);
		params.add(toValue);

		return FETCH_INTERNAL_ACCOUNTS;
	}

	/**
	 * 
	 * @return
	 */
	private int getTotalPages() {
		int totalRecord = 0;

		Connection con = getConnection();
		try (PreparedStatement ps = con.prepareStatement(COUNT_QUERY)) {
			setParams(ps);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					totalRecord = Integer.valueOf(rs.getString("TOTALPAGES"));
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Number Of Pages " + totalRecord);
				}
			}
		} catch (SQLException e) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(e));
		}

		return totalRecord;
	}

	/**
	 * 
	 * @param ps
	 * @throws SQLException
	 */
	private void setParams(PreparedStatement ps) throws SQLException {
		for (int i = 1; i <= params.size(); i++) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Params Provided for Index  {" + i + "} is = " + params.get(i - 1));
			}
			ps.setObject(i, params.get(i - 1));
		}
	}

	/**
	 * 
	 * @return
	 */
	private Connection getConnection() {
		return BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection();

	}

}