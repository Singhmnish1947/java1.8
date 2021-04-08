package com.misys.ub.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.bo.refimpl.IBOCODETYPES;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_RCN_ACCOUNTRECONCONF;
import com.trapedza.bankfusion.bo.refimpl.IBOpseudonyms;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AddDaysToDate;
import com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState;
import com.trapedza.bankfusion.servercommon.fatoms.PagingHelper;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_RCN_EnquireReconciliation;

/**
 * 
 * @author samehdi
 *
 */
public class EnquireAccountReconciliationFatom extends
		AbstractUB_RCN_EnquireReconciliation {

	private IPersistenceObjectsFactory factory = null;
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
//
//	private transient final static Log logger = LogFactory
//			.getLog(EnquireAccountReconciliationFatom.class.getName());

	/**
	 * 
	 * @param env
	 */
	public EnquireAccountReconciliationFatom(BankFusionEnvironment env) {
		super(env);
	}

	// Fecth the data for all .
	
	public static final String FETCH_FOR_ALL_CONFIGURATION = " SELECT T1."+IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " AS " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID
	+ " , T1." + IBOTransaction.AMOUNT + " AS " + IBOTransaction.AMOUNT + ",T1." + IBOTransaction.NARRATION + " AS " + IBOTransaction.NARRATION 
	+ " , T1." + IBOTransaction.REFERENCE + " AS " + IBOTransaction.REFERENCE + ", T1." + IBOTransaction.VALUEDATE + " AS " + IBOTransaction.VALUEDATE
	+ " , T1." + IBOTransaction.UBRECONSTATUS + " AS " + IBOTransaction.UBRECONSTATUS 
	+ " , T1." + IBOTransaction.TRANSACTIONID + " AS " + IBOTransaction.TRANSACTIONID 
	+ " , T1." + IBOTransaction.TRANSACTIONSRID + " AS " + IBOTransaction.TRANSACTIONSRID
	+ " , T1." + IBOTransaction.CODE  + " AS " + IBOTransaction.CODE
	+ " , T1." + IBOTransaction.ISOCURRENCYCODE  + " AS " + IBOTransaction.ISOCURRENCYCODE
	+ " , T1." + IBOTransaction.DEBITCREDITFLAG + " AS " + IBOTransaction.DEBITCREDITFLAG
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISAMOUNTMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISAMOUNTMATCH
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISNARRATIVEMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISNARRATIVEMATCH
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISTXNREFMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISTXNREFMATCH
	+ "  FROM " + IBOTransaction.BONAME + " T1, "+ IBOUB_RCN_ACCOUNTRECONCONF.BONAME + " T2 "
	+ "  WHERE T1." +IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = T2."+IBOUB_RCN_ACCOUNTRECONCONF.ACCOUNTID
	+ "  AND T1."+ IBOTransaction.UBRECONSTATUS + " =? AND T1."+IBOTransaction.VALUEDATE+" >= ? AND T1."+IBOTransaction.VALUEDATE+" < ? ";
	
	
	public static final String FETCH_BY_PSEUDONAME = " SELECT T1."+IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " AS " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID
	+ " , T1." + IBOTransaction.AMOUNT + " AS " + IBOTransaction.AMOUNT + ",T1." + IBOTransaction.NARRATION + " AS " + IBOTransaction.NARRATION 
	+ " , T1." + IBOTransaction.REFERENCE + " AS " + IBOTransaction.REFERENCE + ", T1." + IBOTransaction.VALUEDATE + " AS " + IBOTransaction.VALUEDATE
	+ " , T1." + IBOTransaction.UBRECONSTATUS + " AS " + IBOTransaction.UBRECONSTATUS
	+ " , T1." + IBOTransaction.TRANSACTIONID + " AS " + IBOTransaction.TRANSACTIONID 
	+ " , T1." + IBOTransaction.TRANSACTIONSRID + " AS " + IBOTransaction.TRANSACTIONSRID
	+ " , T1." + IBOTransaction.CODE  + " AS " + IBOTransaction.CODE
	+ " , T1." + IBOTransaction.ISOCURRENCYCODE  + " AS " + IBOTransaction.ISOCURRENCYCODE
	+ " , T1." + IBOTransaction.DEBITCREDITFLAG + " AS " + IBOTransaction.DEBITCREDITFLAG
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISAMOUNTMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISAMOUNTMATCH
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISNARRATIVEMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISNARRATIVEMATCH
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISTXNREFMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISTXNREFMATCH
	+ " FROM " + IBOTransaction.BONAME + " T1, "+ IBOUB_RCN_ACCOUNTRECONCONF.BONAME + " T2 "
	+ " WHERE T1." +IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = T2."+IBOUB_RCN_ACCOUNTRECONCONF.ACCOUNTID
	+ " AND T1."+ IBOTransaction.UBRECONSTATUS + " =? AND " + "T2." + IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE +" =? AND T1."+IBOTransaction.VALUEDATE+" >= ? AND T1."+IBOTransaction.VALUEDATE+" < ? ";
	
	/**
	 * 
	 */
	public static final String FETCH_BY_ACCOUNTID = " SELECT T1."+IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " AS " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID
	+"  , T1." + IBOTransaction.AMOUNT + " AS " + IBOTransaction.AMOUNT + ",T1." + IBOTransaction.NARRATION + " AS " + IBOTransaction.NARRATION 
	+"  , T1." + IBOTransaction.REFERENCE + " AS " + IBOTransaction.REFERENCE + ", T1." + IBOTransaction.VALUEDATE + " AS " + IBOTransaction.VALUEDATE
	+ " , T1." + IBOTransaction.UBRECONSTATUS + " AS " + IBOTransaction.UBRECONSTATUS
	+ " , T1." + IBOTransaction.TRANSACTIONID + " AS " + IBOTransaction.TRANSACTIONID 
	+ " , T1." + IBOTransaction.TRANSACTIONSRID + " AS " + IBOTransaction.TRANSACTIONSRID
	+ " , T1." + IBOTransaction.CODE  + " AS " + IBOTransaction.CODE
	+ " , T1." + IBOTransaction.ISOCURRENCYCODE  + " AS " + IBOTransaction.ISOCURRENCYCODE
	+ " , T1." + IBOTransaction.DEBITCREDITFLAG + " AS " + IBOTransaction.DEBITCREDITFLAG
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISAMOUNTMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISAMOUNTMATCH
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISNARRATIVEMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISNARRATIVEMATCH
	+ " , T2." + IBOUB_RCN_ACCOUNTRECONCONF.ISTXNREFMATCH + " AS " + IBOUB_RCN_ACCOUNTRECONCONF.ISTXNREFMATCH
	+ " FROM " + IBOTransaction.BONAME + " T1, "+ IBOUB_RCN_ACCOUNTRECONCONF.BONAME + " T2 "
	+ " WHERE T1." +IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = T2."+IBOUB_RCN_ACCOUNTRECONCONF.ACCOUNTID
	+ " AND T1."+ IBOTransaction.UBRECONSTATUS + " =? AND " + "T2." + IBOUB_RCN_ACCOUNTRECONCONF.ACCOUNTID +" =? AND T1."+IBOTransaction.VALUEDATE+" >= ? AND T1."+IBOTransaction.VALUEDATE+" < ? ";
	
	private static String VALUE_DATE_ORDER_BY_CLAUSE = " ORDER BY T1."+IBOTransaction.VALUEDATE+" DESC";
	
	private static String AMOUNT_WHERE_CLAUSE = " AND ((T1."+IBOTransaction.AMOUNT+" >= ? AND T1."+IBOTransaction.AMOUNT+" <= ?) OR (T1."+IBOTransaction.AMOUNT+" >= ? AND T1."+IBOTransaction.AMOUNT+" <= ?))";
	
	private static String NARRATIVE_WILDCARD_SEARCH = " AND T1."+IBOTransaction.NARRATION+" LIKE ?";
	
	private static String NARRATIVE_EXACT_SEARCH = " AND T1."+IBOTransaction.NARRATION+" = ?";

	private static int totalPages;
	/**
	 * 
	 */
	private final static String PSEUDONAME = "1";
	
	/**
	 * 
	 */
	private final static String ACCOUNT = "2";
	
	/**
	 * 
	 */
	private String reconFetchQuery = CommonConstants.EMPTY_STRING;
	
	private ArrayList<Object> paramsForValues = new ArrayList<Object>();
	
	private final static String VALIDATE_MODE = "Validate";
	
	private final static String FETCH_CONFIGURED_PSEUDONAME = "FETCHCONFIGURED";
	
	private final static String FETCH_NONCONFIGURED_PSEUDONAME = "FETCHNONCONFIGURED";
	
	private List<IBOCODETYPES> reconStatus;
	/**
	 * 
	 */
	public void process(BankFusionEnvironment env) {
		if(getF_IN_Mode().equals(VALIDATE_MODE))
		{
			boolean isTxnZeroProof = isTransactionZeroProof(getF_IN_In_TransactionDetails());
			if(!isTxnZeroProof)
			{
				EventsHelper.handleEvent(40112161, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), env);
			}
		}
		else if(getF_IN_Mode().equals(FETCH_CONFIGURED_PSEUDONAME))
		{
			fetchConfiguredPseudoName(true);
		}
		else if(getF_IN_Mode().equals(FETCH_NONCONFIGURED_PSEUDONAME))
		{
			fetchConfiguredPseudoName(false);
		}
		else
		{
			validateDate(getF_IN_FromDate(),getF_IN_ToDate());
			validateAmount(getF_IN_FromAmount(),getF_IN_ToAmount());
			
			factory = BankFusionThreadLocal.getPersistanceFactory();
			String reconFor = getF_IN_ReconciliationFor();
			
			reconFetchQuery = constructQuery(reconFor);
			
			VectorTable resultGrid;
			if(getF_IN_TransactionDetails_PAGINGSUPPORT().equals("Y")){
				resultGrid = runQuery(reconFetchQuery, 1);
				Object pageData[] = new Object[4];
				pageData[0] = 1; // getRequestedPage()
				pageData[1] = getF_IN_TransactionDetails_NUMBEROFROWS(); //getNumberOfRows()
				pageData[2] = totalPages; // getTotalPages()
				pageData[3] = reconFetchQuery;
				resultGrid.setPagingData(pageData);
				setF_OUT_TransactionDetails_TOTALPAGES(totalPages);
			}else
			{
				List<SimplePersistentObject> resultSet = BankFusionThreadLocal.getPersistanceFactory()
				.executeGenericQuery(reconFetchQuery, paramsForValues,
						null, false);
			/**
			 * setting the result set in a vector table.
			 */
				resultGrid = constructTransactionVector(resultSet, 1 , false) ;
			}
			setF_OUT_TransactionDetails(resultGrid);
		}
		
	}
	
	
	private void validateAmount(BigDecimal fromAmount, BigDecimal toAmount) {
		// TODO Auto-generated method stub
		if(fromAmount.compareTo(toAmount) == 1){
			EventsHelper.handleEvent(40015048, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), 
					BankFusionThreadLocal.getBankFusionEnvironment());
		}
		
	}


	private void validateDate(Timestamp fromDate, Timestamp toDate) {
		Timestamp businessDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
		if(getDateWithZeroTime(fromDate).after(getDateWithZeroTime(toDate)))
		{
			EventsHelper.handleEvent(40000304, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), 
					BankFusionThreadLocal.getBankFusionEnvironment());	
		}else 
			if(getDateWithZeroTime(fromDate).after(getDateWithZeroTime(businessDate))){
				EventsHelper.handleEvent(40000401, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), 
						BankFusionThreadLocal.getBankFusionEnvironment());
			}else
				if(getDateWithZeroTime(toDate).after(getDateWithZeroTime(businessDate))){
					EventsHelper.handleEvent(40180104, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), 
							BankFusionThreadLocal.getBankFusionEnvironment());
				}
	}
	
	
	private Date getDateWithZeroTime(Timestamp date)
	{
		Calendar cDate = Calendar.getInstance();
		cDate.setTime(date);
		 cDate.setTime(date);
	        cDate.set(Calendar.HOUR, 0);
	        cDate.set(Calendar.MINUTE, 0);
	        cDate.set(Calendar.SECOND, 0);
	        cDate.set(Calendar.MILLISECOND, 0);
	        cDate.set(Calendar.AM_PM, Calendar.AM);
	        cDate.set(Calendar.DST_OFFSET, 0);
	        return cDate.getTime();
		
	}

	private final static String FETCH_NON_CONFIGURED_PSEUDONAME_QRY =  " WHERE "
		+ IBOpseudonyms.PSEUDONYMCODE + " NOT IN ( SELECT "
		+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " FROM "
		+ IBOUB_RCN_ACCOUNTRECONCONF.BONAME + " WHERE "
		+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " IS NOT NULL)";
	
	private final static 	String FETCH_CONFIGURED_PSEUDONAME_QRY = " WHERE "
		+ IBOpseudonyms.PSEUDONYMCODE + "  IN ( SELECT "
		+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " FROM "
		+ IBOUB_RCN_ACCOUNTRECONCONF.BONAME + ")";

	

	/**
	 * 
	 * @param fetchConfiguredAccounts
	 */
	private void fetchConfiguredPseudoName(boolean fetchConfiguredAccounts)
	{
		
	
		factory = BankFusionThreadLocal.getPersistanceFactory();
		ArrayList params = new ArrayList();
		List<IBOpseudonyms> pseudoNameList = null;
		if (fetchConfiguredAccounts)
			 pseudoNameList = factory.findByQuery(IBOpseudonyms.BONAME, FETCH_CONFIGURED_PSEUDONAME_QRY, params, null, true);
		else
			 pseudoNameList = factory.findByQuery(IBOpseudonyms.BONAME, FETCH_NON_CONFIGURED_PSEUDONAME_QRY, params, null, true);
		
		
		VectorTable pseudoNameVector = new VectorTable();
		int rowCount = 1;
		
		/**
		 * 
		 */
		for(IBOpseudonyms pseudonyms : pseudoNameList)
		{
			Map rowData = new HashMap();
			rowData.putAll(pseudonyms.getDataMap());
			if(rowCount == 1 )
			{
				rowData.put(CommonConstants.SELECT, Boolean.TRUE);
			}
			else
			{
				rowData.put(CommonConstants.SELECT, Boolean.FALSE);
			}
			
			rowData.put(CommonConstants.SRNO, rowCount);
			rowCount ++;
			pseudoNameVector.addAll(new VectorTable(rowData));
		}
		VectorTable pseudoNameVector1 = new VectorTable();
		for(int i=0;i<pseudoNameVector.size();i++)
		{
			Map rowTag = pseudoNameVector.getRowTags(i);
			pseudoNameVector1.addAll(new VectorTable(rowTag));
			
		}
		
		setF_OUT_TransactionDetails(pseudoNameVector1);
		
		
	}
	/**
	 * 
	 * @param transactionDetails
	 * @return
	 */
	private boolean isTransactionZeroProof(VectorTable transactionDetails) 
	{
		
		BigDecimal totalAmount = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal txnAmount = CommonConstants.BIGDECIMAL_ZERO;
		String transactionSign = CommonConstants.EMPTY_STRING;
		boolean isRecordSelected = false;
		/**
		 * 
		 */
		for (int i = 0; i < transactionDetails.size(); i++)
		{
			Map rowTags = transactionDetails.getRowTags(i);
			boolean selected = (Boolean) rowTags.get(CommonConstants.SELECT);
			if(selected)
			{
				isRecordSelected = true;
				txnAmount = ((BigDecimal) rowTags.get(TRANSACTION_AMOUNT)).abs();
				transactionSign =  (String) rowTags.get(TRANSACTION_CREDITDEBIT);
				if(transactionSign.equals("C"))
				{
					totalAmount = totalAmount.add(txnAmount);
				}
				else
				{
					totalAmount = totalAmount.subtract(txnAmount);
				}
			}
		}
		
		/**
		 * If No Records are selected
		 */
		if(!isRecordSelected)
		{
			EventsHelper.handleEvent(40015088, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
		}
		
		if(totalAmount.compareTo(CommonConstants.BIGDECIMAL_ZERO) == 0)
			return true;
		
		return false;
	}

		// Cond Check for  PSEUDONYM
		

	/**
	 * 
	 */
	private VectorTable runQuery(String reconFetchQuery, int pageNumber) {
		VectorTable newResultVector = new VectorTable();
		
			IPagingData pagingData = new PagingData(pageNumber,
					getF_IN_TransactionDetails_NUMBEROFROWS());
			pagingData.setCurrentPageNumber(pageNumber);
			pagingData.setRequiresTotalPages(true);
			pagingData.setPageSize(getF_IN_TransactionDetails_NUMBEROFROWS());
			
			List<SimplePersistentObject> resultSet = BankFusionThreadLocal.getPersistanceFactory()
					.executeGenericQuery(reconFetchQuery, paramsForValues,
							pagingData, false);
			/**
			 * setting the result set in a vector table.
			 */
			newResultVector = constructTransactionVector(resultSet, pageNumber , true);
			totalPages = pagingData.getTotalPages();
	
		return newResultVector;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.trapedza.bankfusion.steps.refimpl.AbstractUB_CNF_CheckPreExistence
	 * #processPagingState
	 * (com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment,
	 * com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState,
	 * java.util.Map)
	 */

	public Object processPagingState(BankFusionEnvironment env,
			ActivityStepPagingState pagingState, Map supportData) {
		PagingHelper pagingHelper = pagingState.getPagingHelper();
		Map pagingModel = pagingHelper.getPagingModel();
		reconFetchQuery = (String) pagingModel.get("PAGING_QUERY");
		paramsForValues = (ArrayList) pagingModel.get("PAGING_PARAMS");
		int totalPages = ((Integer) supportData.get(CommonConstants.TOTALPAGES))
				.intValue();
		int pageNo = ((Integer) supportData.get(CommonConstants.PAGENO))
				.intValue();
		if (totalPages > pageNo)
			setF_OUT_TransactionDetails_HASMOREPAGES(Boolean.TRUE);
		else {
			setF_OUT_TransactionDetails_HASMOREPAGES(Boolean.FALSE);
		}
		setF_OUT_TransactionDetails_NOOFROWS((Integer) supportData
				.get(CommonConstants.NUMBEROFROWS));
		VectorTable resultGrid = runQuery(reconFetchQuery, pageNo);
		Object pageData[] = new Object[4];
		pageData[0] = pageNo; // getRequestedPage()
		pageData[1] = getF_IN_TransactionDetails_NUMBEROFROWS(); //getNumberOfRows()
		pageData[2] = totalPages; // getTotalPages()
		resultGrid.setPagingData(pageData);
		setF_OUT_TransactionDetails_TOTALPAGES(totalPages);
		setF_OUT_TransactionDetails(resultGrid);
		
		return (resultGrid);
	}

	/**
	 * stores the session data for paging support
	 */

	public ActivityStepPagingState createActivityStepPagingState() {
		ActivityStepPagingState pagingState = super
				.createActivityStepPagingState();
		Map supportedData = pagingState.getPagingHelper().getPagingModel();
		supportedData.put("PAGING_QUERY", reconFetchQuery);
		supportedData.put("PAGING_PARAMS", paramsForValues);
		return pagingState;
	}
	
	/**
	 * 
	 */
	private final static String ACCOUNT_ID = "ACCOUNT";
	private final static String TRANSACTION_ID = "TRANSACTIONID";
	private final static String TRANSACTION_SRID = "TRANSACTIONSRID";
	private final static String TRANSACTION_AMOUNT = "TRANSACTIONAMOUNT";
	private final static String TRANSACTION_CODE = "TRANSACTIONCODE";
	private final static String TRANSACTION_NARRATIVE = "TRANSACTIONNARRATIVE";
	private final static String TRANSACTION_REFERENCE = "TRANSACTIONREFERENCE";
	private final static String TRANSACTION_CREDITDEBIT = "TRANSACTIONSIGN";
	private final static String TRANSACTION_RECONSTATUS = "TRANSACTIONRECONSTATUS";
	private final static String CONFIGURATION_AMOUNTMATCH = "ISAMOUNTMATCH";
	private final static String CONFIGURATION_ISNARRATIVEMATCH = "ISNARRATIVEMATCH";
	private final static String CONFIGURATION_ISREFERENCEMATCH = "ISREFMATCH";
	private final static String CONFIGURATION_PSEUDONAME = "PSEUDONAME";
	private final static String TRANSACTION_VALUEDATE = "TRANSACTIONVALUEDATE";
	private final static String TRANSACTION_CURRENCY = "TRANSACTIONCURRENCY";
	
	
	/**
	 * 
	 * @param resultSet
	 * @param pageNumber
	 * @return
	 */
	private VectorTable constructTransactionVector(
			List<SimplePersistentObject> resultSet, int pageNumber , boolean shouldSelectFirstRow) {
		reconStatus = getReconStatusCode();
		VectorTable txnDetailVector = new VectorTable();
		int rownum =1;
		for(SimplePersistentObject txnDetails:resultSet)
		{
			Map rowData = new HashMap();
			rowData.put(ACCOUNT_ID, txnDetails.getDataMap().get(IBOTransaction.ACCOUNTPRODUCT_ACCPRODID));
			rowData.put(TRANSACTION_ID, txnDetails.getDataMap().get(IBOTransaction.TRANSACTIONID));
			rowData.put(TRANSACTION_SRID, txnDetails.getDataMap().get(IBOTransaction.TRANSACTIONSRID));
			rowData.put(TRANSACTION_AMOUNT, txnDetails.getDataMap().get(IBOTransaction.AMOUNT));
			rowData.put(TRANSACTION_CODE, txnDetails.getDataMap().get(IBOTransaction.CODE));
			rowData.put(TRANSACTION_NARRATIVE, txnDetails.getDataMap().get(IBOTransaction.NARRATION));
			rowData.put(TRANSACTION_REFERENCE, txnDetails.getDataMap().get(IBOTransaction.REFERENCE));
			rowData.put(TRANSACTION_CREDITDEBIT, txnDetails.getDataMap().get(IBOTransaction.DEBITCREDITFLAG));
			rowData.put(TRANSACTION_RECONSTATUS, getReconStatusDesc((String)txnDetails.getDataMap().get(IBOTransaction.UBRECONSTATUS),reconStatus));
			rowData.put(TRANSACTION_VALUEDATE, txnDetails.getDataMap().get(IBOTransaction.VALUEDATE));
			rowData.put(TRANSACTION_CURRENCY, txnDetails.getDataMap().get(IBOTransaction.ISOCURRENCYCODE));
			rowData.put(CONFIGURATION_AMOUNTMATCH, txnDetails.getDataMap().get(IBOUB_RCN_ACCOUNTRECONCONF.ISAMOUNTMATCH));
			rowData.put(CONFIGURATION_ISREFERENCEMATCH, txnDetails.getDataMap().get(IBOUB_RCN_ACCOUNTRECONCONF.ISTXNREFMATCH));
			rowData.put(CONFIGURATION_ISNARRATIVEMATCH, txnDetails.getDataMap().get(IBOUB_RCN_ACCOUNTRECONCONF.ISNARRATIVEMATCH));
			rowData.put(CONFIGURATION_PSEUDONAME, txnDetails.getDataMap().get(IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE));
			rowData.put(CommonConstants.SRNO, rownum);
			if (rownum == 1 && shouldSelectFirstRow)
				rowData.put(CommonConstants.SELECT, Boolean.TRUE);
			else
				rowData.put(CommonConstants.SELECT, Boolean.FALSE);
			
			VectorTable rowDataVector = new VectorTable(rowData);
			txnDetailVector.addAll(rowDataVector);
			rownum++;
		}
		return txnDetailVector;
	}

	/**
	 * 
	 * @param reconFor
	 * @return
	 */
	private String constructQuery(String reconFor) {
		if(reconFor.equals(PSEUDONAME))
		{
			
			paramsForValues.add(getF_IN_ReconcileStatus());
			paramsForValues.add(getF_IN_PseudoName());
			paramsForValues.add(getF_IN_FromDate());
			paramsForValues.add(AddDaysToDate.run(getF_IN_ToDate(),1));
			return getDynamicQueryBasedOnAmountAndNarrative(FETCH_BY_PSEUDONAME);
			 
		}
		else if(reconFor.equals(ACCOUNT))
		{
			
			paramsForValues.add(getF_IN_ReconcileStatus());
			paramsForValues.add(getF_IN_AccountID());
			paramsForValues.add(getF_IN_FromDate());
			paramsForValues.add(AddDaysToDate.run(getF_IN_ToDate(),1));
			return getDynamicQueryBasedOnAmountAndNarrative(FETCH_BY_ACCOUNTID);
		}
		else
		{
			paramsForValues.add(getF_IN_ReconcileStatus());
			paramsForValues.add(getF_IN_FromDate());
			paramsForValues.add(AddDaysToDate.run(getF_IN_ToDate(),1));
			return getDynamicQueryBasedOnAmountAndNarrative(FETCH_FOR_ALL_CONFIGURATION);
		}
		
	}

	private String getDynamicQueryBasedOnAmountAndNarrative(
			String genericQuery) {
		// TODO Auto-generated method stub
		int a;
		String narrative = getF_IN_Narrative();
		if((getF_IN_FromAmount().compareTo(CommonConstants.BIGDECIMAL_ZERO)!= 0) 
				|| (getF_IN_ToAmount().compareTo(CommonConstants.BIGDECIMAL_ZERO)!= 0)){
			genericQuery = genericQuery+AMOUNT_WHERE_CLAUSE;
			paramsForValues.add(getF_IN_FromAmount().abs());
			paramsForValues.add(getF_IN_ToAmount().abs());
			paramsForValues.add(getF_IN_ToAmount().abs().negate());
			paramsForValues.add(getF_IN_FromAmount().abs().negate());
		}
		
		if(!narrative.equals(CommonConstants.EMPTY_STRING)){
			paramsForValues.add(narrative);
			if(narrative.contains("%")){
				genericQuery = genericQuery+NARRATIVE_WILDCARD_SEARCH;
			}else
			{
				genericQuery = genericQuery+NARRATIVE_EXACT_SEARCH;
			}
		}
		return genericQuery;
	}


	/**
	 * 
	 */
	protected void setOutputTags() {

		
	}
	
	private  final static String whereClause =" WHERE " + IBOCODETYPES.CODETYPE
	+ " = ?  AND " + IBOCODETYPES.LOCALEID + " = ?";
	
	private final static String RECONSTATUS_CONSTANT = "RECONSTATUS";
	/**
	 * 
	 * @return
	 */
	private List<IBOCODETYPES> getReconStatusCode()
	{
		ArrayList params = new ArrayList();
		params.add(RECONSTATUS_CONSTANT);
		params.add(FinderMethods.getDefaultLocale(BankFusionThreadLocal.getBankFusionEnvironment()));
		List<IBOCODETYPES> reconStatus;
		reconStatus = (BankFusionThreadLocal.getPersistanceFactory()
				.findByQuery(IBOCODETYPES.BONAME, whereClause, params, null , false));
		return reconStatus;
	}
	
	
	
	/**
	 * 
	 * @param reconStatus
	 * @param reconCodeTypeList
	 * @return
	 */
	private String getReconStatusDesc(String reconStatus, List<IBOCODETYPES> reconCodeTypeList) {
		for(IBOCODETYPES codeType: reconCodeTypeList)
		{
				if(codeType.getF_SUBCODETYPE().equals(reconStatus))
				return codeType.getF_DESCRIPTION();
		}
		return CommonConstants.EMPTY_STRING;
	}
	
}
