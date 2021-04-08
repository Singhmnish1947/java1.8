package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_INCOMINGPOSTINGDETAILS;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState;
import com.trapedza.bankfusion.servercommon.fatoms.PagingHelper;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_IncomingPostingInquiryActivity;

public class UB_INF_IncomingPostingInquiry extends AbstractUB_INF_IncomingPostingInquiryActivity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient final static Log logger = LogFactory.getLog(UB_INF_IncomingPostingInquiry.class);

	private String reconFetchQuery = CommonConstants.EMPTY_STRING;
	private static ArrayList<Object> paramList = new ArrayList<>();

	public UB_INF_IncomingPostingInquiry() {
		super();
	}

	@SuppressWarnings("deprecation")
	public UB_INF_IncomingPostingInquiry(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		super.process(env);

		// IBOUB_INF_INCOMINGPOSTINGDETAILS

		logger.info("****************************Inquiry Incoming Posting Details*******************::");
		logger.info("FromDate::" + getF_IN_fromDate() + "::ToDate::" + getF_IN_toDate() + "::Status::"
				+ getF_IN_status() + "::Owner ID::" + getF_IN_ownerId() + "::Order By::" + getF_IN_orderBy()
				+ ":: Txn Type::" + getF_IN_TxnType());

		/** Receiving Inputs from UI **/

		Date fromDate = getF_IN_fromDate();
		Date toDate = getF_IN_toDate();
		String status = getF_IN_status();
		String ownerId = getF_IN_ownerId();
		String orderBy = getF_IN_orderBy();
		String TxnType = getF_IN_TxnType();

		if (fromDate == null) {
            fromDate = SystemInformationManager.getInstance().getBFSystemDate();
		}
		if (toDate == null) {
            toDate = SystemInformationManager.getInstance().getBFSystemDate();
		} else {
			// Changing Value to next day since query will be <
			toDate = new Date(toDate.getTime() + 24 * 60 * 60 * 1000);
		}
		if (ownerId == null || ownerId.isEmpty()) {
			ownerId = "%";
		}

		/** Generating Query Start **/
		StringBuffer finalQuery = new StringBuffer("SELECT " + IBOUB_INF_INCOMINGPOSTINGDETAILS.OWNERRID + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.REFERENCE + "," + IBOUB_INF_INCOMINGPOSTINGDETAILS.ACCOUNTID + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.TRANSACTIONCURRENCY + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.TRANSACTIONAMOUNT + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.CREDITDEBITFLAG + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.TRANSACTIONCODE + "," + IBOUB_INF_INCOMINGPOSTINGDETAILS.STATUS + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.MSGRECEIVEDTTM + "," + IBOUB_INF_INCOMINGPOSTINGDETAILS.VALUEDATE
				+ "," + IBOUB_INF_INCOMINGPOSTINGDETAILS.SOURCEBRANCH + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.EXPENSECODE + "," + IBOUB_INF_INCOMINGPOSTINGDETAILS.ERRORCODE + ","
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.ERRORDESCRIPTION + "," + IBOUB_INF_INCOMINGPOSTINGDETAILS.CHANNELID
				+ "," + IBOUB_INF_INCOMINGPOSTINGDETAILS.TRANSACTIONTYPE + " FROM "
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.BONAME);

		/** Creating Query with Parameter List **/
		paramList = new ArrayList<>();
		StringBuffer filterQuery = new StringBuffer(" WHERE " + IBOUB_INF_INCOMINGPOSTINGDETAILS.MSGRECEIVEDTTM
				+ " > ? AND " + IBOUB_INF_INCOMINGPOSTINGDETAILS.MSGRECEIVEDTTM + " < ?");
		paramList.add(fromDate);
		paramList.add(toDate);

		if (!status.trim().isEmpty() && !status.equals("NA")) {
			filterQuery.append(" AND " + IBOUB_INF_INCOMINGPOSTINGDETAILS.STATUS + " = ?");
			paramList.add(status);
		}
		if (!TxnType.trim().isEmpty() && !TxnType.equals("NA")) {
			filterQuery.append(" AND " + IBOUB_INF_INCOMINGPOSTINGDETAILS.TRANSACTIONTYPE + " = ?");
			paramList.add(TxnType);
		}
		filterQuery.append(" AND " + IBOUB_INF_INCOMINGPOSTINGDETAILS.OWNERRID + " like ?");
		paramList.add(ownerId);
		finalQuery.append(filterQuery);
		String orderByClause;
		if (orderBy.equals("INOWNERRID")) {
			orderByClause = " ORDER BY " + IBOUB_INF_INCOMINGPOSTINGDETAILS.OWNERRID;
		} else if (orderBy.equals("INVALUEDATE")) {
			orderByClause = " ORDER BY " + IBOUB_INF_INCOMINGPOSTINGDETAILS.VALUEDATE + " DESC";
		} else if (orderBy.equals("INTRANSACTIONAMOUNT")) {
			orderByClause = " ORDER BY " + IBOUB_INF_INCOMINGPOSTINGDETAILS.TRANSACTIONAMOUNT + " DESC";
		} else {
			orderByClause = " ORDER BY " + IBOUB_INF_INCOMINGPOSTINGDETAILS.MSGRECEIVEDTTM + " DESC";
		}

		finalQuery.append(orderByClause);
		/** Generating Query End **/

		logger.info("Query::" + finalQuery + ":::with list::" + paramList);
		reconFetchQuery = finalQuery.toString();

		/** Pagination Properties Set Start **/
		setF_OUT_incomingPostingDataCollection_HASMOREPAGES(true);

		setF_OUT_incomingPostingDataCollection_NOOFROWS(getF_IN_incomingPostingDataCollection_NUMBEROFROWS());

		/** Firing New Query Start **/

		List<SimplePersistentObject> resultSet = new ArrayList<>();
		Object pageData[] = new Object[4];

		try {
			resultSet = runQuery(finalQuery.toString(), 1);
			setF_IN_incomingPostingDataCollection_NUMBEROFROWS(resultSet.size());

			logger.info(" Total Pages:::" + getF_OUT_incomingPostingDataCollection_TOTALPAGES());

			pageData[0] = getF_IN_incomingPostingDataCollection_PAGENUMBER();
			pageData[1] = getF_IN_incomingPostingDataCollection_NUMBEROFROWS();
			pageData[2] = getF_OUT_incomingPostingDataCollection_TOTALPAGES();
			pageData[3] = finalQuery;
		} catch (Exception e) {
			logger.info("Exception while executing Query....." + e);
			e.printStackTrace();
		}

		setF_OUT_incomingPostingDataCollection(getResponseVector(resultSet, pageData));
	}

	@SuppressWarnings("unchecked")
	private List<SimplePersistentObject> runQuery(String finalQuery, int pageNumber) {
		logger.debug("*********************** In runQuery() Start ******************************");
		IPagingData pagingData = new PagingData(pageNumber, getF_IN_incomingPostingDataCollection_NUMBEROFROWS());
		pagingData.setRequiresTotalPages(true);

		pagingData.setCurrentPageNumber(pageNumber);
		pagingData.setPageSize(getF_IN_incomingPostingDataCollection_NUMBEROFROWS());

		List<SimplePersistentObject> result = BankFusionThreadLocal.getPersistanceFactory()
				.executeGenericQuery(finalQuery.toString(), paramList, pagingData, false);

		setF_OUT_incomingPostingDataCollection_TOTALPAGES(pagingData.getTotalPages());

		logger.debug("*********************** In runQuery() End ******************************");
		return result;

	}

	private VectorTable getResponseVector(List<SimplePersistentObject> incomingPostingList, Object[] pagingData) {
		logger.debug("*********************** In getResponseVector() Start ******************************");
		VectorTable ouputVector = new VectorTable();
		ouputVector.setPagingData(pagingData);

		if (incomingPostingList != null) {
			for (SimplePersistentObject simplePersistentObject : incomingPostingList) {
				HashMap<String, Object> mapData = new HashMap<>();
				mapData.put("OWNERRID", simplePersistentObject.getDataMap().get("0"));
				mapData.put("REFERENCE", simplePersistentObject.getDataMap().get("1"));
				mapData.put("ACCOUNTID", simplePersistentObject.getDataMap().get("2"));
				mapData.put("TRANSACTIONCURRENCY", simplePersistentObject.getDataMap().get("3"));
				mapData.put("TRANSACTIONAMOUNT", ((BigDecimal) simplePersistentObject.getDataMap().get("4")).setScale(2,
						BigDecimal.ROUND_HALF_UP));
				mapData.put("CREDITDEBITFLAG", simplePersistentObject.getDataMap().get("5"));
				mapData.put("TRANSACTIONCODE", simplePersistentObject.getDataMap().get("6"));
				mapData.put("STATUS", simplePersistentObject.getDataMap().get("7"));
				mapData.put("MSGRECEIVEDTTM", simplePersistentObject.getDataMap().get("8"));
				mapData.put("VALUEDATE",
						new Date(((Timestamp) simplePersistentObject.getDataMap().get("9")).getTime()));
				mapData.put("SOURCEBRANCH", simplePersistentObject.getDataMap().get("10"));
				mapData.put("EXPENSECODE", simplePersistentObject.getDataMap().get("11"));
				mapData.put("ERRORCODE", simplePersistentObject.getDataMap().get("12"));
				mapData.put("ERRORDESCRIPTION", simplePersistentObject.getDataMap().get("13"));
				mapData.put("CHANNELID", simplePersistentObject.getDataMap().get("14"));
				mapData.put("TRANSACTIONTYPE", simplePersistentObject.getDataMap().get("15"));

				ouputVector.addAll(new VectorTable(mapData));
			}

		} else {
			logger.info("Null Result::" + incomingPostingList);
		}
		logger.debug("*********************** In getResponseVector() End ******************************");
		return ouputVector;
	}

	@Override
	public ActivityStepPagingState createActivityStepPagingState() {

		logger.debug("*********************** In createActivityStepPagingState() Start ******************************");
		ActivityStepPagingState pagingState = super.createActivityStepPagingState();
		@SuppressWarnings("unchecked")
		Map<String, Object> supportedData = pagingState.getPagingHelper().getPagingModel();
		supportedData.put("PAGING_QUERY", reconFetchQuery);
		supportedData.put("PAGING_PARAMS", paramList);
		logger.info("*********************** In createActivityStepPagingState() End ******************************");
		return pagingState;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object processPagingState(BankFusionEnvironment env, ActivityStepPagingState pagingState, Map supportData) {
		logger.debug("*********************** In processPagingState() Start ******************************");
		PagingHelper pagingHelper = pagingState.getPagingHelper();
		Map pagingModel = pagingHelper.getPagingModel();
		String reconFetchQuery = (String) pagingModel.get("PAGING_QUERY");
		paramList = (ArrayList) pagingModel.get("PAGING_PARAMS");
		int totalPages = ((Integer) supportData.get(CommonConstants.TOTALPAGES)).intValue();
		int pageNo = ((Integer) supportData.get(CommonConstants.PAGENO)).intValue();
		if (totalPages > pageNo)
			setF_OUT_incomingPostingDataCollection_HASMOREPAGES(Boolean.TRUE);
		else {
			setF_OUT_incomingPostingDataCollection_HASMOREPAGES(Boolean.FALSE);
		}
		setF_OUT_incomingPostingDataCollection_NOOFROWS((Integer) supportData.get(CommonConstants.NUMBEROFROWS));

		Object pageData[] = new Object[4];
		pageData[0] = pageNo; // getRequestedPage()
		pageData[1] = getF_IN_incomingPostingDataCollection_NUMBEROFROWS(); // getNumberOfRows()
		pageData[2] = totalPages; // getTotalPages()

		VectorTable resultGrid = getResponseVector(runQuery(reconFetchQuery, pageNo), pageData);

		resultGrid.setPagingData(pageData);
		setF_OUT_incomingPostingDataCollection_TOTALPAGES(totalPages);
		setF_OUT_incomingPostingDataCollection(resultGrid);
		logger.debug("*********************** In processPagingState() End ******************************");
		return (resultGrid);

	}

}
