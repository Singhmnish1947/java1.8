package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_SWTMessageDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState;
import com.trapedza.bankfusion.servercommon.fatoms.PagingHelper;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_SearchSwiftMsgDetailts;


public class UB_SWT_SearchSwiftMsgDetailts extends AbstractUB_SWT_SearchSwiftMsgDetailts {


	public UB_SWT_SearchSwiftMsgDetailts(BankFusionEnvironment env) {
		super(env);
	}

	public UB_SWT_SearchSwiftMsgDetailts() {
	}

	private static int totalPages;
	private BankFusionEnvironment environment;
	private String query;
	int numberOfRecords = 25;
	private static final String PERCENTAGE = "%";
	private ArrayList<Object> paramsForValues = new ArrayList<Object>();
	@SuppressWarnings("FBPE")
	private static final String MESSAGE_ID = " AND t1."+ IBOUB_INF_SWTMessageDetail.MESSAGEID + " = ? ";

	public void process(BankFusionEnvironment env) throws BankFusionException {

		requestScreenData();

	}

	private BigDecimal roundAmount(BigDecimal amtDis, String accCUR) {
		if(CommonConstants.EMPTY_STRING.equals(accCUR)||null==accCUR){
			return BigDecimal.ZERO;
		}
		HashMap<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("currency",accCUR);
		inputParams.put("inputAmount",amtDis);
		FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("UB_ATM_AmtCcyRounding_SRV");
		HashMap outputChargeParams = invoker.invokeMicroflow(inputParams, false);
		BigDecimal outputAmount = (BigDecimal) outputChargeParams.get("outAmount");
		return outputAmount;
	}


	@Override
	public Object processPagingState(BankFusionEnvironment env, ActivityStepPagingState pagingState, Map supportData) {
		PagingHelper pagingHelper = pagingState.getPagingHelper();
		Map pagingModel = pagingHelper.getPagingModel();
		query = (String) pagingModel.get("PAGING_QUERY");
		paramsForValues = (ArrayList) pagingModel.get("PAGING_PARAMS");
		numberOfRecords = (Integer) pagingModel.get("NO_OF_RECORDS");
		int totalPages = ((Integer) supportData.get(CommonConstants.TOTALPAGES)).intValue();
		int pageNo = ((Integer) supportData.get(CommonConstants.PAGENO)).intValue();
		if (totalPages > pageNo)
			setF_OUT_OutputVector_HASMOREPAGES(Boolean.TRUE);
		else {
			setF_OUT_OutputVector_HASMOREPAGES(Boolean.FALSE);
		}
		setF_OUT_OutputVector_NOOFROWS(numberOfRecords);
		VectorTable resultGrid = executeQuery(query, pageNo);
		Object pageData[] = new Object[4];
		pageData[0] = pageNo; // getRequestedPage()
		pageData[1] = numberOfRecords; // getNumberOfRows()
		pageData[2] = totalPages; // getTotalPages()
		resultGrid.setPagingData(pageData);
		setF_OUT_OutputVector_TOTALPAGES(totalPages);
		setF_OUT_OutputVector(resultGrid);

		return (resultGrid);
	}


	public ActivityStepPagingState createActivityStepPagingState() {
		ActivityStepPagingState pagingState = super.createActivityStepPagingState();
		Map supportedData = pagingState.getPagingHelper().getPagingModel();
		supportedData.put("PAGING_QUERY", query);
		supportedData.put("PAGING_PARAMS", paramsForValues);
		supportedData.put("NO_OF_RECORDS", numberOfRecords);
		return pagingState;
	}





	private void requestScreenData() {
		setF_OUT_OutputVector_HASMOREPAGES(Boolean.TRUE);
		setF_OUT_OutputVector_NOOFROWS(numberOfRecords);
		query = constructQuery();
		VectorTable resultGrid = executeQuery(query, 1);
		Object pageData[] = new Object[4];
		pageData[0] = 1; // getRequestedPage()
		pageData[1] = numberOfRecords; // getNumberOfRows()
		pageData[2] = totalPages; // getTotalPages()
		pageData[3] = query;
		resultGrid.setPagingData(pageData);
		setF_OUT_OutputVector_TOTALPAGES(totalPages);
		setF_OUT_OutputVector(resultGrid);
	}



	private VectorTable executeQuery(String queryToBeExecuted, int pageNumber) {
		VectorTable resultVector = new VectorTable();
		List resultSet = null;
		try {
			PagingData pagingData = new PagingData(pageNumber, numberOfRecords);
			// if (pageNumber == 1)
			pagingData.setRequiresTotalPages(true);
			pagingData.setCurrentPageNumber(pageNumber);
			pagingData.setPageSize(numberOfRecords);
			resultSet = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(queryToBeExecuted, paramsForValues, pagingData);

			/**
			 * setting the result set in a vector table.
			 */

			resultVector = constructVector(resultSet, pageNumber);
			int jj=0;
			//if (pageNumber == 1)
			totalPages = pagingData.getTotalPages();
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
		if ((resultSet != null && resultSet.size() == 0) || getF_OUT_OutputVector_NOOFROWS() == 0) {
			EventsHelper.handleEvent(40600208, new Object[] { "" }, new HashMap<String, Object>(), environment);
		}
		return resultVector;
	}




	private VectorTable constructVector(List<SimplePersistentObject> resultSet, int pageNumber) {
		VectorTable resultVector = new VectorTable();
		int size = 0;
		ArrayList params = new ArrayList();
		if (resultSet != null && !resultSet.isEmpty()) {
			size = resultSet.size();
			Map details = null;
			int record = 1;
			PagingData pagingData = new PagingData(1, 1);
			for (int i = 0; i < resultSet.size(); i++) {
				SimplePersistentObject persistentObject = (SimplePersistentObject) resultSet.get(i);
				String messageId = (String) persistentObject.getDataMap().get("MESSAGEREFID");
				String messageType = (String) persistentObject.getDataMap().get("MESSAGETYPE");
				String messageStatus = 	(String) persistentObject.getDataMap().get("MESSAGESTATUS");
				String currency = 	(String) persistentObject.getDataMap().get("CURRENCY");
				String reference = 	(String) persistentObject.getDataMap().get("REFERENCE");
				String amount = 	persistentObject.getDataMap().get("AMOUNT").toString();
				BigDecimal convAmount = roundAmount(new BigDecimal(amount), currency);
				String valueDate = persistentObject.getDataMap().get("VALUEDATE").toString();

				details = new HashMap();
				details.put("UB_INF_SWTMESSAGEDETAIL_MESSAGEID", messageId);
				details.put("UB_INF_MESSAGEHEADER_MESSAGETYPE", messageType);
				details.put("UB_INF_MESSAGEHEADER_MESSAGESTATUS", messageStatus);
				details.put("UB_INF_SWTMESSAGEDETAIL_AMOUNT", convAmount);
				details.put("UB_INF_SWTMESSAGEDETAIL_CURRENCYCODE", currency);
				details.put("UB_INF_SWTMESSAGEDETAIL_VALUEDT", valueDate);
				details.put("UB_INF_SWTMESSAGEDETAIL_REFERENCE", reference);

				if (record == 1) {
					details.put(CommonConstants.SELECT, (Boolean) true);
				}
				else {
					details.put(CommonConstants.SELECT, (Boolean) false);
				}
				details.put(CommonConstants.SRNO, (Integer) ((numberOfRecords * (pageNumber - 1)) + record));
				record++;
				resultVector.addAll(new VectorTable(details));
			}
		}
		setF_OUT_OutputVector_NOOFROWS(size);
		return resultVector;
	}

	private String constructQuery() {
		boolean pass = false;
		String query = null;

		if(null!=getF_IN_FromDate() && null!=getF_IN_ToDate()) {
			pass = true;
		}
		else {
			EventsHelper.handleEvent(20020872, new Object[] {},
					new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
			//raiseEvent("20020872");
		}
		if(pass && getF_IN_ToDate().compareTo(getF_IN_FromDate())>=0) {
			pass = true;
		}
		else {
			EventsHelper.handleEvent(40401040, new Object[] {},
					new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
			//raiseEvent("40401040");
		}
		if(null!=getF_IN_MessageType() && !CommonConstants.EMPTY_STRING.equals(getF_IN_MessageType())) {
			pass = true;
		}
		else {
			EventsHelper.handleEvent(20020872, new Object[] {},
					new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
			//raiseEvent("20020872");
		}

		if(pass && PaymentSwiftConstants.OUTWARD.equals(getF_IN_direction())) {
			query = "SELECT t1."+IBOUB_SWT_RemittanceTable.UBMESSAGEREFID+" AS MESSAGEREFID, t1."
					+ IBOUB_SWT_RemittanceTable.UBMESSAGETYPE+" AS MESSAGETYPE, t2."
					+ IBOUB_INF_MessageHeader.MESSAGESTATUS+" AS MESSAGESTATUS, t1."
					+ IBOUB_SWT_RemittanceTable.UBDEBITAMOUNT+" AS AMOUNT, t1."
					+ IBOUB_SWT_RemittanceTable.UBCURRENCY+" AS CURRENCY, t1."
					+ IBOUB_SWT_RemittanceTable.UBVALUEDATE+" AS VALUEDATE, t2."
					+ IBOUB_INF_MessageHeader.REFERENCE+" AS REFERENCE "
					+ " FROM "
					+ IBOUB_SWT_RemittanceTable.BONAME +" t1, "+ IBOUB_INF_MessageHeader.BONAME + " t2 WHERE t1."
					+ IBOUB_SWT_RemittanceTable.UBMESSAGEREFID + " = t2."+ IBOUB_INF_MessageHeader.MESSAGEID1 + " AND t2."
					+ IBOUB_INF_MessageHeader.CHANNELID + " != '"+ PaymentSwiftConstants.CHANNELID_SWIFT + "' AND t2."
					+ IBOUB_INF_MessageHeader.MESSAGESTATUS + " != '"+ PaymentSwiftConstants.PROCESSED + "' AND t2."
					+ IBOUB_INF_MessageHeader.MESSAGESTATUS + " != '"+ PaymentSwiftConstants.CANCELLED + "' AND t1."
					+ IBOUB_SWT_RemittanceTable.UBMESSAGETYPE +" = ? AND "
					+ "UB_TODATE(t1."+ IBOUB_SWT_RemittanceTable.UBVALUEDATE + ") >=  ? AND "
					+ "UB_TODATE(t1."+ IBOUB_SWT_RemittanceTable.UBVALUEDATE + ") <= ? ";

			paramsForValues.add(getF_IN_MessageType());
			Date fromDate = new Date(getF_IN_FromDate().getTime());
			paramsForValues.add(fromDate);
			Date toDate = new Date(getF_IN_ToDate().getTime());
			paramsForValues.add(toDate);

			if(!PERCENTAGE.equals(getF_IN_Currency())) {
				query = query+"AND t1."+ IBOUB_SWT_RemittanceTable.UBCURRENCY +" = ? ";
				paramsForValues.add(getF_IN_Currency());
			}
			if(!PERCENTAGE.equals(getF_IN_MessageID())) {
				query = query+" AND t1."+ IBOUB_SWT_RemittanceTable.UBMESSAGEREFID + " = ? ";
				paramsForValues.add(getF_IN_MessageID());
			}
			if(!PERCENTAGE.equals(getF_IN_messageReference())) {
				query = query+"AND t2."+ IBOUB_INF_MessageHeader.REFERENCE + " = ? ";
				paramsForValues.add(getF_IN_messageReference());
			}
			if(!BigDecimal.ZERO.equals(getF_IN_Amount())) {
				query = query+"AND t1."+ IBOUB_SWT_RemittanceTable.UBCREDITAMOUNT + " = ? ";
				paramsForValues.add(getF_IN_Amount());
			}
			if(!PERCENTAGE.equals(getF_IN_MessageStatus())) {
				query = query+"AND t2."+ IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ? ";
				paramsForValues.add(getF_IN_MessageStatus());
			}

			query = query+" order by t2."+IBOUB_INF_MessageHeader.MSGRECEIVEDTTM+" asc";

		}
		else {
			query = "SELECT t1."+IBOUB_INF_SWTMessageDetail.MESSAGEID+" AS MESSAGEREFID, t2."
					+ IBOUB_INF_MessageHeader.MESSAGETYPE+" AS MESSAGETYPE, t2."
					+ IBOUB_INF_MessageHeader.MESSAGESTATUS+" AS MESSAGESTATUS, t1. "
					+ IBOUB_INF_SWTMessageDetail.AMOUNT+" AS AMOUNT, t1."
					+ IBOUB_INF_SWTMessageDetail.CURRENCYCODE+" AS CURRENCY, t1."
					+ IBOUB_INF_SWTMessageDetail.VALUEDT+" AS VALUEDATE, t2."
					+ IBOUB_INF_MessageHeader.REFERENCE+" AS REFERENCE "
					+ " FROM "
					+ IBOUB_INF_SWTMessageDetail.BONAME +" t1, "+ IBOUB_INF_MessageHeader.BONAME + " t2 WHERE t1."
					+ IBOUB_INF_SWTMessageDetail.MESSAGEID + " = t2."+ IBOUB_INF_MessageHeader.MESSAGEID1 + " AND t2."
					+ IBOUB_INF_MessageHeader.CHANNELID + " = '"+ PaymentSwiftConstants.CHANNELID_SWIFT + "' AND t2."
					+ IBOUB_INF_MessageHeader.MESSAGESTATUS + " != '"+ PaymentSwiftConstants.PROCESSED + "' AND t2."
					+ IBOUB_INF_MessageHeader.MESSAGESTATUS +" != '"+ PaymentSwiftConstants.CANCELLED + "' AND t2."
					+ IBOUB_INF_MessageHeader.MESSAGETYPE +" = ? AND "
					+ "UB_TODATE(t1."+ IBOUB_INF_SWTMessageDetail.VALUEDT + ") >=  ? AND "
					+ "UB_TODATE(t1."+ IBOUB_INF_SWTMessageDetail.VALUEDT + ") <= ? ";

			paramsForValues.add(getF_IN_MessageType());
			Date fromDate = new Date(getF_IN_FromDate().getTime());
			paramsForValues.add(fromDate);
			Date toDate = new Date(getF_IN_ToDate().getTime());
			paramsForValues.add(toDate);

			if(!PERCENTAGE.equals(getF_IN_Currency())) {
				query = query+"AND t1."+ IBOUB_INF_SWTMessageDetail.CURRENCYCODE +" = ? ";
				paramsForValues.add(getF_IN_Currency());
			}
			if(!PERCENTAGE.equals(getF_IN_MessageID())) {				
				query = query+ MESSAGE_ID;
				paramsForValues.add(getF_IN_MessageID());
			}
			if(!PERCENTAGE.equals(getF_IN_messageReference())) {
				query = query+"AND t2."+ IBOUB_INF_MessageHeader.REFERENCE + " = ? ";
				paramsForValues.add(getF_IN_messageReference());
			}
			if(!BigDecimal.ZERO.equals(getF_IN_Amount())) {
				query = query+"AND t1."+ IBOUB_INF_SWTMessageDetail.AMOUNT + " = ? ";
				paramsForValues.add(getF_IN_Amount());
			}
			if(!PERCENTAGE.equals(getF_IN_MessageStatus())) {
				query = query+"AND t2."+ IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ? ";
				paramsForValues.add(getF_IN_MessageStatus());
			}

			query = query+" order by t2."+IBOUB_INF_MessageHeader.MSGRECEIVEDTTM+" asc";

		}

		return query;
	}

}