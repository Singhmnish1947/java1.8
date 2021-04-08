package com.misys.ub.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOCollateralType;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomerCollateral;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomerCollaterallink;
import com.trapedza.bankfusion.bo.refimpl.IBODebitInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INT_OffsetPoolAccounts;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState;
import com.trapedza.bankfusion.servercommon.fatoms.PagingHelper;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INT_FetchLiabilityAccountsNotPartOfPool;
import com.trapedza.bankfusion.steps.refimpl.IUB_INT_FetchLiabilityAccountsNotPartOfPool;

public class UB_INT_FetchLiabilityAccountsNotPartOfPool extends
		AbstractUB_INT_FetchLiabilityAccountsNotPartOfPool implements
		IUB_INT_FetchLiabilityAccountsNotPartOfPool {
	private transient final static Log logger = LogFactory.getLog(UB_INT_FetchLiabilityAccountsNotPartOfPool.class.getName());
	  private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	StringBuffer queryToBeExecuted = new StringBuffer();
	private static final String PAGING_QUERY = "PAGING_QUERY";
	private static final String PAGING_PARAMS = "PAGING_PARAMS";
	public UB_INT_FetchLiabilityAccountsNotPartOfPool(BankFusionEnvironment env) {
      super(env);
  }

	private static int totalPages;
	ArrayList params = new ArrayList();
	
	public void process(BankFusionEnvironment env) {
		setF_OUT_LiabilityAccountsVector_HASMOREPAGES(true);
		 setF_OUT_LiabilityAccountsVector_NOOFROWS(getF_OUT_LiabilityAccountsVector_NOOFROWS());
	        queryToBeExecuted = constructQuery();
	        if(null==queryToBeExecuted){
	        	return;
	        }
	        VectorTable resultGrid = executeQuery(queryToBeExecuted, 1);
	        Object pageData[] = new Object[4];
	        pageData[0] = 1; // getRequestedPage()
	        pageData[1] = getF_IN_LiabilityAccountsVector_NUMBEROFROWS(); // getNumberOfRows()
	        pageData[2] = totalPages; // getTotalPages()
	        pageData[3] = queryToBeExecuted;
	        resultGrid.setPagingData(pageData);
	        setF_OUT_LiabilityAccountsVector_TOTALPAGES(totalPages);
	        setF_OUT_LiabilityAccountsVector(resultGrid);
	        if (logger.isInfoEnabled()) {
	            logger.info(getF_OUT_LiabilityAccountsVector());
	        }
	}
	
	private StringBuffer constructQuery() {
		 String columnNames = makeStringOfAllColumns();
	        StringBuffer queryForWhere = new StringBuffer();
	        StringBuffer query = new StringBuffer();
	        

			 final String customerCode="T1."+IBOAccount.CUSTOMERCODE+" = "+" ? " ;
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(customerCode);
			 params.add(getF_IN_CustomerCode());
		
			 final String subProductID="T1."+IBOAccount.PRODUCTCONTEXTCODE+" = T2."+IBOProductInheritance.PRODUCTCONTEXTCODE ;
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(subProductID);
			
			 final String productID="T1."+IBOAccount.PRODUCTID+" != "+" ? " ;
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(productID);
			 params.add(getF_IN_ProductID());
	
			 final String hasOffBalFeature="T2."+IBOProductInheritance.HAS_OFFBAL +" = "+" ? " ;
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(hasOffBalFeature);
			 params.add(true);
			 
			 final String hasOffsetFeature="T2."+IBOProductInheritance.HAS_OFFSET +" != "+" ? " ;
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(hasOffsetFeature);
			 params.add(true);
			 
			 final String hasLoan="T2."+IBOProductInheritance.HAS_LOAN +" != "+" ? " ;
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(hasLoan);
			 params.add(true);
			 

			 final String isoCurrencyCode="T1."+IBOAccount.ISOCURRENCYCODE+" = "+" ? " ;
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(isoCurrencyCode);
			 params.add(getF_IN_ISOCurrencyCode());
			 
			 final String accountNotInPool="T1."+IBOAccount.ACCOUNTID+" NOT IN" +
			 		" ( SELECT "+IBOUB_INT_OffsetPoolAccounts.ACCOUNTID+" FROM "+IBOUB_INT_OffsetPoolAccounts.BONAME+
			 		" WHERE "+IBOUB_INT_OffsetPoolAccounts.ISDELETE+" = "+" ? ) ";
			 if (!queryForWhere.toString().equals("")) {
	                queryForWhere.append(" AND ");
	            }
			 queryForWhere.append(accountNotInPool);
			 params.add(false);
				 // Form the dynamic Query
			        // Start the SQL statement

			        query.append(columnNames);
			        if (!queryForWhere.equals("")) {
			            query.append(" WHERE " + queryForWhere);

			        }
			        return query;
			
	        
	        
		
	}
	 public String makeStringOfAllColumns() {
	        final String QueryString = " SELECT T1." + IBOAccount.ACCOUNTID+" AS " + IBOAccount.ACCOUNTID
	        							+",T1."+IBOAccount.BOOKEDBALANCE+" AS "+IBOAccount.BOOKEDBALANCE
	        							+",T1."+IBOAccount.CLEAREDBALANCE+" AS "+IBOAccount.CLEAREDBALANCE
	        							+",T1."+IBOAccount.CUSTOMERCODE+" AS "+IBOAccount.CUSTOMERCODE
	        							+",T1."+IBOAccount.ISOCURRENCYCODE+" AS "+IBOAccount.ISOCURRENCYCODE
	        							+",T1."+IBOAccount.PRODUCTID+" AS "+IBOAccount.PRODUCTID
	        							+ " FROM "
	        							+IBOAccount.BONAME+" T1,"+IBOProductInheritance.BONAME+" T2 ";
	        return QueryString;
	 }
	
	 /**This method is used to excute the query and to set all the values in a vector table.
	     * @param queryToBeExecuted
	     * @param pageNumber
	     * @return vector table contains paged records
	     */
	    private VectorTable executeQuery(StringBuffer queryToBeExecuted, int pageNumber) {
	        VectorTable accountDetails = new VectorTable();
	        //try {
	            IPagingData pagingData = new PagingData(pageNumber, getF_IN_LiabilityAccountsVector_NUMBEROFROWS());
	            pagingData.setCurrentPageNumber(pageNumber);
	            pagingData.setRequiresTotalPages(true);
	            pagingData.setPageSize(getF_IN_LiabilityAccountsVector_NUMBEROFROWS());
	            List<SimplePersistentObject> results = factory.executeGenericQuery(queryToBeExecuted.toString(), params, pagingData,
	                    false);
	            Map accountDetailsMap = new HashMap();
	            // Iterator iterator = results.iterator();
	            int count=1+((pageNumber-1)*getF_IN_LiabilityAccountsVector_NUMBEROFROWS());
	            if (results.size() != 0) {
	                for (SimplePersistentObject sp : results) {
	                    
	                	accountDetailsMap.put(CommonConstants.SRNO, count);
	                    accountDetailsMap.put(CommonConstants.SELECT, (Boolean.TRUE));
	                    accountDetailsMap.put("ACCOUNT_ACCOUNTID", (sp.getDataMap().get(IBOAccount.ACCOUNTID)));
	                    accountDetailsMap.put("ACCOUNT_BOOKEDBALANCE", (sp.getDataMap().get(IBOAccount.BOOKEDBALANCE)));
	                    accountDetailsMap.put("ACCOUNT_CLEAREDBALANCE", (sp.getDataMap().get(IBOAccount.CLEAREDBALANCE)));
	                    accountDetailsMap.put("ACCOUNT_CUSTOMERCODE", (sp.getDataMap().get(IBOAccount.CUSTOMERCODE)));
	                    accountDetailsMap.put("ACCOUNT_ISOCURRENCYCODE", (sp.getDataMap().get(IBOAccount.ISOCURRENCYCODE)));
	                    accountDetailsMap.put("ACCOUNT_PRODUCTID", (sp.getDataMap().get(IBOAccount.PRODUCTID)));
	                    accountDetails.addAll(new VectorTable(accountDetailsMap));
	                    count++;
	                }

	                totalPages = pagingData.getTotalPages();
	            }
	    //    }
	        //catch (Exception e) {
	         //   logger.error(e.getLocalizedMessage());
	        //}
	        return accountDetails;
	    }
	    /*
	     * (non-Javadoc)
	     * 
	     * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_CNF_CheckPreExistence
	     * #processPagingState (com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment,
	     * com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState, java.util.Map)
	     */

	    public Object processPagingState(BankFusionEnvironment env, ActivityStepPagingState pagingState, Map supportData) {
	        PagingHelper pagingHelper = pagingState.getPagingHelper();
	        Map pagingModel = pagingHelper.getPagingModel();
	        queryToBeExecuted = (StringBuffer) pagingModel.get(PAGING_QUERY);
	        params = (ArrayList) pagingModel.get(PAGING_PARAMS);
	        int totalPages = ((Integer) supportData.get(CommonConstants.TOTALPAGES)).intValue();
	        int pageNo = ((Integer) supportData.get(CommonConstants.PAGENO)).intValue();
	        if (totalPages > pageNo)
	            setF_OUT_LiabilityAccountsVector_HASMOREPAGES(Boolean.TRUE);
	        else {
	        	setF_OUT_LiabilityAccountsVector_HASMOREPAGES(Boolean.FALSE);
	        }
	        setF_OUT_LiabilityAccountsVector_NOOFROWS((Integer) supportData.get(CommonConstants.NUMBEROFROWS));
	        VectorTable resultGrid = executeQuery(queryToBeExecuted, pageNo);
	        Object pageData[] = new Object[4];
	        pageData[0] = pageNo; // getRequestedPage()
	        pageData[1] = getF_IN_LiabilityAccountsVector_NUMBEROFROWS(); // getNumberOfRows()
	        pageData[2] = totalPages; // getTotalPages()
	        resultGrid.setPagingData(pageData);
	        setF_OUT_LiabilityAccountsVector_TOTALPAGES(totalPages);
	        setF_OUT_LiabilityAccountsVector(resultGrid);
	        if (logger.isInfoEnabled()) {
	            logger.info(getF_OUT_LiabilityAccountsVector());
	        }
	        return (resultGrid);
	    }

	    /**
	     * stores the session data for paging support
	     */

	    public ActivityStepPagingState createActivityStepPagingState() {
	        ActivityStepPagingState pagingState = super.createActivityStepPagingState();
	        Map supportedData = pagingState.getPagingHelper().getPagingModel();
	        supportedData.put(PAGING_QUERY, queryToBeExecuted);
	        supportedData.put(PAGING_PARAMS, params);
	        return pagingState;
	    }
}
