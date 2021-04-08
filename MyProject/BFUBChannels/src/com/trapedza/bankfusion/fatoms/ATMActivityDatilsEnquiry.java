package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.misys.bankfusion.calendar.ICalendarService;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOISOATM_ActivityUpdate;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState;
import com.trapedza.bankfusion.servercommon.fatoms.PagingHelper;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_ISO8583_ATMActivityDatilsEnquiry;

/**
 * 
 * 
 */

public class ATMActivityDatilsEnquiry extends AbstractUB_ATM_ISO8583_ATMActivityDatilsEnquiry {

	private BankFusionEnvironment environment;
	private static final String ORDERBY_CONSTANT = " ORDER BY ";
	private static final String PARAMINPUT = " = ? ";
	private static final String LIKEPARAMINPUT = " LIKE ? ";
	private static final String AND = " AND ";
	private static final String AS = " AS ";	
	private static int year;
	private static final int BLANKDATE = 70;
	private static final int EVENT_NUMBER_FOR_INVALID_DATE = 40209116;
	private static final int EVENT_NUMBER_FOR_FROM_TO_DATE = 40180070;
	private static String query;

	private ArrayList<Object> paramsForValues = new ArrayList<Object>();
	private static int totalPages;
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	private transient final static Log LOGGER = LogFactory.getLog(ATMActivityDatilsEnquiry.class.getName());

	@SuppressWarnings("deprecation")
	public ATMActivityDatilsEnquiry(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		// run the query
		// get the total number of records & pages
		// set the current page number to 1
		// pass data for 1 page as output

		this.environment = env;
		validateDate();
		setF_OUT_Result_HASMOREPAGES(true);
		setF_OUT_Result_NOOFROWS(getF_IN_Result_NUMBEROFROWS());

		query = getQuery();
		VectorTable resultGrid = runQuery(query, 1);

		Object pageData[] = new Object[4];
		/** Code to Test Pagination **/

		pageData[0] = getF_IN_Result_PAGENUMBER(); // getRequestedPage()
		pageData[1] = getF_IN_Result_NUMBEROFROWS(); // getNumberOfRows();
		pageData[2] = totalPages; // getTotalPages()
		pageData[3] = query;
		resultGrid.setPagingData(pageData);
		setF_OUT_Result_TOTALPAGES(totalPages);
		setF_OUT_Result(resultGrid);
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(getF_OUT_Result());
		}

	}

	/** Adding Code for Pagination **/
	/**
	 * This method will be called for all subsequent paging requests after the
	 * first paged results are shown.
	 * 
	 * @param env
	 *            - an instance of BankFusionEnvironment
	 * @param obj
	 *            - an instance of ActivityStepPagingState that will contain the
	 *            current paging state.
	 * @param supportData
	 *            - a Map that contains various data relating to the data being
	 *            paged
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public Object processPagingState(BankFusionEnvironment env,ActivityStepPagingState pagingState, Map supportData) {
		PagingHelper pagingHelper = pagingState.getPagingHelper();
		Map pagingModel = pagingHelper.getPagingModel();

		query = (String) pagingModel.get("PAGING_QUERY");
		paramsForValues = (ArrayList) pagingModel.get("PAGING_PARAMS");
		int totalPages = ((Integer) supportData.get(CommonConstants.TOTALPAGES))
				.intValue();
		int pageNo = ((Integer) supportData.get(CommonConstants.PAGENO))
				.intValue();
		if (totalPages > pageNo)
			setF_OUT_Result_HASMOREPAGES(Boolean.TRUE);
		else {
			setF_OUT_Result_HASMOREPAGES(Boolean.FALSE);
		}

		// String tableResultModel = (String)
		// supportData.get(CommonConstants.TABLERESULTMODEL);
		// PagingHelper pagingStateModel = pagingState.getPagingHelper();
		setF_OUT_Result_NOOFROWS((Integer) supportData
				.get(CommonConstants.NUMBEROFROWS));

		VectorTable resultGrid = runQuery(query, pageNo);
		Object pageData[] = new Object[4];
		/** Code for Pagination **/

		pageData[0] = pageNo; // getRequestedPage()
		pageData[1] = getF_IN_Result_NUMBEROFROWS(); // getNumberOfRows();
		pageData[2] = totalPages; // getTotalPages()
		resultGrid.setPagingData(pageData);

		setF_OUT_Result_TOTALPAGES(totalPages);
		setF_OUT_Result(resultGrid);
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(getF_OUT_Result());
		}
		return (resultGrid);
	}

	@SuppressWarnings( { "deprecation", "unchecked" })
	private void validateDate() {

		if (getF_IN_from() == null || getF_IN_from().toString() == CommonConstants.EMPTY_STRING) {
			EventsHelper.handleEvent(EVENT_NUMBER_FOR_INVALID_DATE,new Object[] {}, new HashMap(), environment);
		}
		if (getF_IN_to() == null || getF_IN_to().toString() == CommonConstants.EMPTY_STRING) {

			EventsHelper.handleEvent(EVENT_NUMBER_FOR_INVALID_DATE,	new Object[] {}, new HashMap(), environment);
		}
		if (getF_IN_from().after(getF_IN_to())) {
			EventsHelper.handleEvent(EVENT_NUMBER_FOR_FROM_TO_DATE,	new Object[] {}, new HashMap(), environment);
		}
	}

	/**
	 * method to get the query by concatenating select, where and order-by part
	 * of it.
	 * 
	 * @return
	 */
	private String getQuery() {
		String query = CommonConstants.EMPTY_STRING;

		String selectQuery = getSelectQuery();
		String whereClause = getWhereClause();
		String orderByClause = getOrderByClause();
		query = selectQuery + whereClause + orderByClause;
		return query;
	}

	/**
	 * returns the select part of the query.
	 * 
	 * @return
	 */
	private String getSelectQuery() {

		String selectQuery = CommonConstants.SELECT
				+ " TXN."
				+ IBOATMActivityDetail.ATMTRANSACTIONCODE
				+ ", TXN."
				+ IBOATMActivityDetail.ACCOUNTID
				+ ", TXN."
				+ IBOATMActivityDetail.ATMCARDNUMBER
				+ ", TXN."
				+ IBOATMActivityDetail.ATMDEVICEID
				+ ", TXN."
				+ IBOATMActivityDetail.SOURCECIB
				+ ", TXN."
				+ IBOATMActivityDetail.TRANSACTIONREFERENCE
				+ ", TXN."
				+ IBOATMActivityDetail.POSTDATETIME
				+", TXN."
				+ IBOATMActivityDetail.TRANSACTIONAMOUNT
				+", TXN."
				+ IBOATMActivityDetail.CHEQUEREFERENCE
				+", TXN."
				+ IBOATMActivityDetail.ACCOUNTCURRENCY
				+", TXN."
				+ IBOATMActivityDetail.TRANSACTIONID
				+", TXN."
				+ IBOATMActivityDetail.ERRORSTATUS
				+", TXN."
				+ IBOATMActivityDetail.ERRORDESC
				+", ACC."
				+ IBOISOATM_ActivityUpdate.UBATMACTIVITYIDPK
				+", ACC."
				+ IBOISOATM_ActivityUpdate.UBCARDACCEPTORID
				+ " " + CommonConstants.FROM + " "
				+ IBOATMActivityDetail.BONAME + AS + " TXN," 
				+ IBOISOATM_ActivityUpdate.BONAME + AS + " ACC  "
						+ CommonConstants.WHERE + "  TXN."
						+ IBOATMActivityDetail.ID + "= ACC."
						+ IBOISOATM_ActivityUpdate.UBATMACTIVITYIDPK;

		return selectQuery;
	}

	/**
	 * creates the where clause by checking whether the value for the
	 * corresponding field exists or not. if the value is not there it simply
	 * skips and does not add it into where clause.
	 * 
	 * @return
	 */
	private String getWhereClause() {
		String whereClause = CommonConstants.EMPTY_STRING;
		paramsForValues.clear();

		/**
		 * ATM Transaction Code 
		 */
		if (isValueAvailable(getF_IN_ATMTransactionCode())) {
			whereClause = AND + "TXN."
					+ IBOATMActivityDetail.ATMTRANSACTIONCODE
					+ PARAMINPUT;
			paramsForValues.add(getF_IN_ATMTransactionCode());
		}

		/**
		 * Account Number
		 */
		if (isValueAvailable(getF_IN_AccountNumber())) {

			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.ACCOUNTID + PARAMINPUT);
			paramsForValues.add(getF_IN_AccountNumber());
		}

		/**
		 * Aquirer ID
		 */
		if (isValueAvailable(getF_IN_AquirerID())) {

			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.SOURCECIB + LIKEPARAMINPUT);
			paramsForValues.add(getF_IN_AquirerID());
		}
		
		/**
		 * Issuer ID
		 */
		if (isValueAvailable(getF_IN_IssuerID())) {

			whereClause = whereClause.concat(AND + "ACC."
					+ IBOISOATM_ActivityUpdate.UBCARDACCEPTORID+ LIKEPARAMINPUT);
			paramsForValues.add(getF_IN_IssuerID());
		}
		
		
		/**
		 * Card Number
		 */
		if (isValueAvailable(getF_IN_CardNumber())) {
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.ATMCARDNUMBER + PARAMINPUT);
			paramsForValues.add(getF_IN_CardNumber());
		}

		/**
		 * DestAccount Number
		 */
		if (isValueAvailable(getF_IN_DestAccountNumber())) {
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.DESTACCOUNTID + LIKEPARAMINPUT);
			paramsForValues.add(getF_IN_DestAccountNumber());
		}
		/**
		 * Reference
		 */
		if (isValueAvailable(getF_IN_Reference())) {
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.TRANSACTIONREFERENCE + LIKEPARAMINPUT);
			paramsForValues.add(getF_IN_Reference());
		}

		/**
		 * Device ID
		 */
		if (isValueAvailable(getF_IN_DeviceID())) {
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.ATMDEVICEID + PARAMINPUT);
			paramsForValues.add(getF_IN_DeviceID());
		}
		
		/**
		 * Status
		 */
		if (isF_IN_FailureStatus()) {
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.ERRORSTATUS + "> ?");
			paramsForValues.add(0);
		}
		if (isF_IN_SuccessStatus()) {
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.ERRORSTATUS + "= ?");
			paramsForValues.add(0);
		}
		
		
		year = getF_IN_from().getYear();

		if (year > BLANKDATE) {
			ICalendarService calendarService = (ICalendarService) ServiceManagerFactory.getInstance().getServiceManager()
					.getServiceForName(ICalendarService.SERVICE_NAME);
			Timestamp fromDate = new Timestamp(calendarService.getBusinessDate(getF_IN_from())
					.withoutTimeParts().getMillis());
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.TRANSACTIONDTTM + " >= ?");
			paramsForValues.add(fromDate);
		}

		year = getF_IN_to().getYear();

		if (year > BLANKDATE) {
			ICalendarService calendarService = (ICalendarService) ServiceManagerFactory.getInstance().getServiceManager()
					.getServiceForName(ICalendarService.SERVICE_NAME);
			Timestamp toDate = new Timestamp(calendarService.getBusinessDate(getF_IN_to())
					.withoutTimeParts().plusDays(1).getMillis());
			whereClause = whereClause.concat(AND + "TXN."
					+ IBOATMActivityDetail.TRANSACTIONDTTM + " < ?");
			paramsForValues.add(toDate);
		}

		return whereClause;
	}

	/**
	 * creates the order By clause and returns the string
	 * 
	 * @return
	 */
	private String getOrderByClause() {

		// String orderByClause = ORDERBY_CONSTANT + "TXN." +
		// IBOUBVW_TRANSACTIONLIST.POSTINGDATE;
		String orderByClause = ORDERBY_CONSTANT + "TXN."
				+ IBOATMActivityDetail.TRANSACTIONDTTM + " DESC";
		return orderByClause;
	}

	/**
	 * runs the dynamic query
	 * 
	 * @param dynamicQuery
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private VectorTable runQuery(String dynamicQuery, int pageNumber) {

		VectorTable newResultVector = new VectorTable();
//		pageNumber = getF_IN_Result_PAGENUMBER();

		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Query used to fetch the data :" + dynamicQuery);
				LOGGER.info("Parameter Values for Query : " + paramsForValues);
				LOGGER.info("Result Page Number : " + pageNumber);

			}
			IPagingData pagingData = new PagingData(pageNumber,
					getF_IN_Result_NUMBEROFROWS());
			pagingData.setCurrentPageNumber(pageNumber);
			pagingData.setRequiresTotalPages(true);
			pagingData.setPageSize(getF_IN_Result_NUMBEROFROWS());
			// pagingData.setTotalPages();

			List<SimplePersistentObject> resultSet = factory
					.executeGenericQuery(dynamicQuery, paramsForValues,
							pagingData, false);
			/**
			 * setting the number of records
			 */
			setF_OUT_NUMBEROFROWS(resultSet.size());

			/**
			 * setting the result set in a vector table.
			 */
			newResultVector = getVectorTableFromList(resultSet);
			totalPages = pagingData.getTotalPages();
			pagingData.getCurrentPageNumber();
		} catch (Exception e) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(e));
		}
		return newResultVector;
	}

	/**
	 * checks whether the value is passed or not.
	 * 
	 * @param valueForField
	 * @return
	 */
	private boolean isValueAvailable(String valueForField) {

		if (valueForField.compareTo(CommonConstants.EMPTY_STRING) == 0	|| valueForField == null) {
			return false;
		} else
			return true;
	}

	/**
	 * converts the list to vector table so that it can be set into the output
	 * result grid tag
	 * 
	 * @param list
	 * @return
	 */
	private VectorTable getVectorTableFromList(List<SimplePersistentObject> list) {
		VectorTable vectorTable = new VectorTable();
		if (list != null && !list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				SimplePersistentObject simplePersistentObject = (SimplePersistentObject) list.get(i);
				String txnCode = (String) simplePersistentObject.getDataMap().get("0");
				String accID = (String) simplePersistentObject.getDataMap().get("1");
				String atmCardNum = (String) simplePersistentObject.getDataMap().get("2");
				String atmDeviceID = (String) simplePersistentObject.getDataMap().get("3");
				String sourceCIB = (String) simplePersistentObject.getDataMap().get("4");
				String txnRef = (String) simplePersistentObject.getDataMap().get("5");
				Timestamp posDate = (Timestamp) simplePersistentObject.getDataMap().get("6");
				BigDecimal amtDis = (BigDecimal) simplePersistentObject.getDataMap().get("7");
				String chqRef = (String) simplePersistentObject.getDataMap().get("8");
				String accCUR = (String) simplePersistentObject.getDataMap().get("9");
				String txnID = (String) simplePersistentObject.getDataMap().get("10");
				String errStatus = (String) simplePersistentObject.getDataMap().get("11");
				String errDes = (String) simplePersistentObject.getDataMap().get("12");
				String pk = (String) simplePersistentObject.getDataMap().get("13");
				String issuer = (String) simplePersistentObject.getDataMap().get("14");
				BigDecimal amtDisRounded = roundAmount(amtDis, accCUR);
				HashMap attributes = new HashMap();
				attributes.put("ATMTRANSACTIONCODE", txnCode);
				attributes.put("ACCOUNTID", accID);
				attributes.put("ATMCARDNUMBER", atmCardNum);
				attributes.put("ATMDEVICEID", atmDeviceID);
				attributes.put("SOURCECIB", sourceCIB);
				attributes.put("TRANSACTIONREFERENCE", txnRef);
				attributes.put("POSTDATETIME", posDate);
				attributes.put("TRANSACTIONAMOUNT", amtDisRounded);
				attributes.put("CHEQUEREFERENCE", chqRef);
				attributes.put("ACCOUNTCURRENCY", accCUR);
				attributes.put("TRANSACTIONID", txnID);
				attributes.put("ERRORSTATUS", errStatus);
				attributes.put("ERRORDESC", errDes);
				attributes.put("UBATMACTIVITYIDPK", pk);
				attributes.put("UBCARDACCEPTORID", issuer);
				vectorTable.addAll(new VectorTable(attributes));
			}
		}
		return vectorTable;
	}

	private BigDecimal roundAmount(BigDecimal amtDis, String accCUR) {
		if("".equals(accCUR)||null==accCUR){
			return amtDis;
		}
		HashMap<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("currency",accCUR);
		inputParams.put("inputAmount",amtDis);
		FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("UB_ATM_AmtCcyRounding_SRV");
		HashMap outputChargeParams = invoker.invokeMicroflow(inputParams, false);
		BigDecimal outputAmount = (BigDecimal) outputChargeParams.get("outAmount");
		return outputAmount;
	}

	/**
	 * stores the session data for paging support
	 */
	@SuppressWarnings("unchecked")
	public ActivityStepPagingState createActivityStepPagingState() {
		ActivityStepPagingState pagingState = super.createActivityStepPagingState();
		Map supportedData = pagingState.getPagingHelper().getPagingModel();
		supportedData.put("PAGING_QUERY", query);
		supportedData.put("PAGING_PARAMS", paramsForValues);
		return pagingState;
	}

}
