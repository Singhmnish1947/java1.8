package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountCustomers;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPostingNarrativeConfig;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.fatoms.ActivityStepPagingState;
import com.trapedza.bankfusion.servercommon.fatoms.PagingHelper;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PostingNarrativeConfigCRUD;

public class UB_SWT_PostingNarrativeConfigCRUD extends
		AbstractUB_SWT_PostingNarrativeConfigCRUD {

	private static int totalPages;
	private static final String SRNO = "SRNO";
	private static final String SELECT = "SELECT";
	private ArrayList<Object> paramsForValues = new ArrayList<Object>();
	private BankFusionEnvironment environment;
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	int numberOfRecords = 10;
	private String query1 = CommonConstants.EMPTY_STRING;
	private String query = CommonConstants.EMPTY_STRING;
	static final String CREATE_OPERATON = "CREATE";
	private String customerCode = CommonConstants.EMPTY_STRING;
	static final String VIEW_OPERATON = "VIEW";

	protected VectorTable aprPaymentPlan = null;

	String accWhereClause = " WHERE " + IBOAccountCustomers.CUSTOMERCODE + " =?";
	
	String deleteAllAccountConfig = "DELETE FROM "
		+ IBOPostingNarrativeConfig.BONAME + " WHERE "
		+ IBOPostingNarrativeConfig.IDENTIFIERCODE + " IN "
		+ "(SELECT " + IBOAccount.ACCOUNTID + " FROM " + IBOAccount.BONAME
		+ " WHERE " + IBOAccount.CUSTOMERCODE + " =?)";

	String getAllAccounts = "WHERE "
			+ IBOAttributeCollectionFeature.CUSTOMERCODE + " =?";

	/*String deleteConfig = "DELETE FROM "
			+ IBOPostingNarrativeConfig.BONAME + " WHERE "
			+ IBOPostingNarrativeConfig.IDENTIFIERCODE + " =?";*/
	
	String deleteConfig = " WHERE "
		+ IBOPostingNarrativeConfig.IDENTIFIERCODE + " =?";

	String insertNarrativeConfig = "INSERT INTO "
			+ IBOPostingNarrativeConfig.BONAME + "("
			+ IBOPostingNarrativeConfig.IDENTIFIERCODE + ", "
			+ IBOPostingNarrativeConfig.CRNARRATIVEID + ", "
			+ IBOPostingNarrativeConfig.DRNARRATIVEID + ", "
			+ IBOPostingNarrativeConfig.LASTUPDATEDDTTM + ")"
			+ " VALUES ( ?, ?, ?, ? )";
	
	
	 /**
     * Query for getting all posting narratives
     * 
     */
    private static final String getPostingNarrativeDetails = "SELECT " + "B." + IBOPostingNarrativeConfig.CRNARRATIVEID + " AS "
                                                            + IBOPostingNarrativeConfig.CRNARRATIVEID + " , " + "B." + IBOPostingNarrativeConfig.DRNARRATIVEID + " AS "
                                                            + IBOPostingNarrativeConfig.DRNARRATIVEID + " , " + "B." + IBOPostingNarrativeConfig.IDENTIFIERCODE + " AS "
                                                            + IBOPostingNarrativeConfig.IDENTIFIERCODE + " , " + "B." + IBOPostingNarrativeConfig.LASTUPDATEDDTTM + " AS "
                                                            + IBOPostingNarrativeConfig.LASTUPDATEDDTTM +  " , " + "B." + IBOPostingNarrativeConfig.VERSIONNUM + " AS "
                                                            + IBOPostingNarrativeConfig.VERSIONNUM 
                                                            + " FROM " + IBOPostingNarrativeConfig.BONAME
                                                            + " B WHERE B." + IBOPostingNarrativeConfig.CRNARRATIVEID + " LIKE '%'";
    
    private static final String postingNarrativeConfigQuery = "SELECT P." + IBOPostingNarrativeConfig.IDENTIFIERCODE + " AS " + IBOPostingNarrativeConfig.IDENTIFIERCODE 
												+ ", P." + IBOPostingNarrativeConfig.CRNARRATIVEID + " AS " + IBOPostingNarrativeConfig.CRNARRATIVEID 
												+ ", P." + IBOPostingNarrativeConfig.DRNARRATIVEID + " AS " + IBOPostingNarrativeConfig.DRNARRATIVEID 
												+ ", P." + IBOPostingNarrativeConfig.LASTUPDATEDDTTM + " AS " + IBOPostingNarrativeConfig.LASTUPDATEDDTTM 
												+ " FROM " + IBOPostingNarrativeConfig.BONAME + " P, " + IBOAccountCustomers.BONAME + " A WHERE "
												+ "A." + IBOAccountCustomers.ACCOUNTID + " = P." + IBOPostingNarrativeConfig.IDENTIFIERCODE + " AND "
												+ "A." + IBOAccountCustomers.CUSTOMERCODE + " = ?";

	public UB_SWT_PostingNarrativeConfigCRUD() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		  this.environment = env;
		  setF_OUT_gridData_HASMOREPAGES(true);
		  setF_OUT_gridData_NOOFROWS(numberOfRecords);
		  setFactory();
		
		if (getF_IN_operationMode().equals(CREATE_OPERATON)) {
			if (isF_IN_isAllAccountsChecked()) {
				IBOPostingNarrativeConfig existingConfig = findPostingNarrativeConfig(getF_IN_identifierCode());
				if (existingConfig != null
						&& !existingConfig.equals(CommonConstants.EMPTY_STRING)) {
					EventsHelper
					.handleEvent(
							ChannelsEventCodes.E_CONFIGURATION_ALREADY_EXISTS_UB,
							new Object[] {}, new HashMap(), env);
				} else {
					// Delete all accounts of the customers configurations
					// Insert customer level configuration
					String customerCode=getF_IN_identifierCode();

					IBOAttributeCollectionFeature accountDetails = (IBOAttributeCollectionFeature) factory
					.findByPrimaryKey(
							IBOAttributeCollectionFeature.BONAME,
							getF_IN_identifierCode(), true);

					if(accountDetails!=null){
						customerCode= accountDetails.getF_CUSTOMERCODE();
					}

					ArrayList<String> accParams = new ArrayList();
					accParams.add(customerCode);

					List<IBOAccountCustomers> accList = factory.findByQuery(IBOAccountCustomers.BONAME, accWhereClause, accParams, null);

					String accNumber = CommonConstants.EMPTY_STRING;
					ArrayList<String> deleteParams = new ArrayList();

					boolean raiseWarning = false;
					for (IBOAccountCustomers account : accList) {
						accNumber = account.getF_ACCOUNTID();

						IBOPostingNarrativeConfig findConfig = findPostingNarrativeConfig(accNumber);
						if(findConfig != null){
							raiseWarning = true;
							if(raiseWarning){
								EventsHelper.handleEvent(40422033, new Object[] {}, new HashMap(), env);
							}
							deleteParams.add(accNumber);
							factory.bulkDelete(IBOPostingNarrativeConfig.BONAME, deleteConfig, deleteParams);
							deleteParams.clear();	

						}					

					}

					insertPostingConfigNarrative(getF_IN_identifierCode(),
							getF_IN_creditNarrative(), getF_IN_debitNarrative());
				}
			} else {
				IBOPostingNarrativeConfig existingConfig = findPostingNarrativeConfig(getF_IN_identifierCode());
				if (existingConfig != null
						&& !existingConfig.equals(CommonConstants.EMPTY_STRING)) {
					EventsHelper
					.handleEvent(
							ChannelsEventCodes.E_CONFIGURATION_ALREADY_EXISTS_UB,
							new Object[] {}, new HashMap(), env);
				} else {
					// If Customer level exists, delete the entry,Insert for all
					// other accounts,insert for the given account as well
					// If customer level does not exists then create an entry
					// for the given account only

					IBOAttributeCollectionFeature accountDetails = (IBOAttributeCollectionFeature) factory
					.findByPrimaryKey(
							IBOAttributeCollectionFeature.BONAME,
							getF_IN_identifierCode(), true);

					String customerCode = accountDetails.getF_CUSTOMERCODE();

					existingConfig = findPostingNarrativeConfig(customerCode);
					if (existingConfig != null
							&& !existingConfig
							.equals(CommonConstants.EMPTY_STRING)) {
						// TODO only for saving and current accounts; currently
						// for all accounts

						EventsHelper.handleEvent(40422032, new Object[] {}, new HashMap(), env);


						ArrayList params = new ArrayList();
						String getCustomerCode = null;
						params.add(customerCode);

						factory.bulkDelete(IBOPostingNarrativeConfig.BONAME, deleteConfig, params);

						List<SimplePersistentObject> result = factory
						.findByQuery(
								IBOAttributeCollectionFeature.BONAME,
								getAllAccounts, params, null, true);

						if (null != result && !result.isEmpty()) {
							Iterator AccItr = result.iterator();
							while (AccItr.hasNext()) {
								IBOAttributeCollectionFeature accDetails = (IBOAttributeCollectionFeature) AccItr
								.next();
								if (accDetails.getBoID().equals(
										getF_IN_identifierCode()))
									insertPostingConfigNarrative(
											getF_IN_identifierCode(),
											getF_IN_creditNarrative(),
											getF_IN_debitNarrative());
								else
									insertPostingConfigNarrative(accDetails
											.getBoID(), existingConfig
											.getF_CRNARRATIVEID(),
											existingConfig.getF_DRNARRATIVEID());
							}
						}
					} else {
						insertPostingConfigNarrative(getF_IN_identifierCode(),
								getF_IN_creditNarrative(),
								getF_IN_debitNarrative());
					}
				}
			}
		} else if (getF_IN_operationMode().equals(VIEW_OPERATON)) {
			VectorTable result = new VectorTable();
			HashMap resultMap = new HashMap();


			//When % or empty string is passed, all the configuration will be populated to the grid
			if(getF_IN_identifierCode().equals("%") || getF_IN_identifierCode().equals(CommonConstants.EMPTY_STRING)){
				 query1 = getPostingNarrativeDetails;
				 Object pageData[] = new Object[4];
	                VectorTable resultGrid = findAllPostingNarrativeConfig(query1, 1);                             
	                pageData[0] = 1; // getRequestedPage()
	                pageData[1] = numberOfRecords; // getNumberOfRows()
	                pageData[2] = totalPages; // getTotalPages()
	                pageData[3] = query1;
	                resultGrid.setPagingData(pageData);
	                setF_OUT_gridData_TOTALPAGES(totalPages);
	                setF_OUT_gridData(resultGrid);                          
			}else{

				// Find if configuration is present for the given identifier code - customer or account
				IBOPostingNarrativeConfig existingConfig = findPostingNarrativeConfig(getF_IN_identifierCode());
				Integer i = new Integer(1);
				if (existingConfig != null && !existingConfig.equals(CommonConstants.EMPTY_STRING)) {

					IBOAttributeCollectionFeature accountDetails = (IBOAttributeCollectionFeature) factory
					.findByPrimaryKey(
							IBOAttributeCollectionFeature.BONAME,
							getF_IN_identifierCode(), true);

					if(accountDetails!=null){
						setF_OUT_isAllAccountsConfigured(false);
					}else{
						setF_OUT_isAllAccountsConfigured(true);
					}

					String boID = (String)existingConfig.getDataMap().get(IBOPostingNarrativeConfig.IDENTIFIERCODE);
					String f_CRNARRATIVEID = (String)existingConfig.getDataMap().get(IBOPostingNarrativeConfig.CRNARRATIVEID);
					String f_DRNARRATIVEID = (String)existingConfig.getDataMap().get(IBOPostingNarrativeConfig.DRNARRATIVEID);
					java.util.Date f_LASTUPDATEDDTTM = (java.util.Date)existingConfig.getDataMap().get(IBOPostingNarrativeConfig.LASTUPDATEDDTTM);
					int versionNum = (Integer)existingConfig.getDataMap().get(IBOPostingNarrativeConfig.VERSIONNUM);
					

					Map itemList = new HashMap();
					itemList.put(SRNO, i);
		            if (i == 1) {
		            	itemList.put(SELECT, Boolean.TRUE);
		            }
		            else {
		            	itemList.put(SELECT, Boolean.FALSE);
		            }
					itemList.put(IBOPostingNarrativeConfig.IDENTIFIERCODE, boID);
					itemList.put(IBOPostingNarrativeConfig.CRNARRATIVEID, f_CRNARRATIVEID);
					itemList.put(IBOPostingNarrativeConfig.DRNARRATIVEID, f_DRNARRATIVEID);
					itemList.put(IBOPostingNarrativeConfig.LASTUPDATEDDTTM, f_LASTUPDATEDDTTM);
					itemList.put(IBOPostingNarrativeConfig.VERSIONNUM, versionNum);
					result.addAll(new VectorTable(itemList));
					setF_OUT_gridData(result);

				}else{
					//Find all configuration for the given customer code (if code reaches here, it does mean customer level config is not present
					query = postingNarrativeConfigQuery;
					customerCode =  getF_IN_identifierCode();
					Object pageData[] = new Object[4];
	                VectorTable resultGrid = fetchPostingNarrativeConfig(query, 1);                             
	                pageData[0] = 1; // getRequestedPage()
	                pageData[1] = numberOfRecords; // getNumberOfRows()
	                pageData[2] = totalPages; // getTotalPages()
	                pageData[3] = query;
	                resultGrid.setPagingData(pageData);
	                setF_OUT_gridData_TOTALPAGES(totalPages);
	                setF_OUT_gridData(resultGrid);           
				}
			}
		}
	}

	private IBOPostingNarrativeConfig findPostingNarrativeConfig(
			String identifierCode) {
		return (IBOPostingNarrativeConfig) BankFusionThreadLocal
				.getPersistanceFactory().findByPrimaryKey(
						IBOPostingNarrativeConfig.BONAME, identifierCode, true);
	}
	
	private VectorTable findAllPostingNarrativeConfig(String query,int pageNumber) {
		setF_OUT_gridData_HASMOREPAGES(true);
		setF_OUT_gridData_NOOFROWS(numberOfRecords);
		IPagingData pagingData = new PagingData(pageNumber, numberOfRecords);
	    pagingData.setCurrentPageNumber(pageNumber);
	    pagingData.setRequiresTotalPages(true);
	    pagingData.setPageSize(numberOfRecords);
		query1 = query;
		// Execute query
        List records = factory.executeGenericQuery(query1, paramsForValues, pagingData, true);
        VectorTable narratives = populateVectorTable(records);
        totalPages = pagingData.getTotalPages();
        pagingData.getCurrentPageNumber();
        return narratives;
        
	}

	private void insertPostingConfigNarrative(String identiferCode,
			String crNarr, String drNarr) {
		IBOPostingNarrativeConfig narrConfig = (IBOPostingNarrativeConfig) BankFusionThreadLocal
				.getPersistanceFactory().getStatelessNewInstance(
						IBOPostingNarrativeConfig.BONAME);

		narrConfig.setBoID(identiferCode);
		narrConfig.setF_CRNARRATIVEID(crNarr);
		narrConfig.setF_DRNARRATIVEID(drNarr);
		narrConfig.setF_LASTUPDATEDDTTM(SystemInformationManager.getInstance()
				.getBFBusinessDateTime());

		factory.create(IBOPostingNarrativeConfig.BONAME, narrConfig);
	}
	
	private VectorTable fetchPostingNarrativeConfig(String postingNarrativeConfigQuery,int pageNumber){
		
		setF_OUT_gridData_HASMOREPAGES(true);
		setF_OUT_gridData_NOOFROWS(numberOfRecords);
		IPagingData pagingData = new PagingData(pageNumber, numberOfRecords);
	    pagingData.setCurrentPageNumber(pageNumber);
	    pagingData.setRequiresTotalPages(true);
	    pagingData.setPageSize(numberOfRecords);
		query = postingNarrativeConfigQuery;
		paramsForValues.add(customerCode);
		// Execute query
        List records = factory.executeGenericQuery(query, paramsForValues, pagingData, true);
        VectorTable narratives = populateVectorTable(records);
        totalPages = pagingData.getTotalPages();
        pagingData.getCurrentPageNumber();
        return narratives;
		
	}

	public UB_SWT_PostingNarrativeConfigCRUD(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	private IPersistenceObjectsFactory setFactory() {
		return factory = BankFusionThreadLocal.getPersistanceFactory();

	}
	
	private VectorTable populateVectorTable(List config){
		SimplePersistentObject object = null;
		VectorTable resultConfig = new VectorTable();
		VectorTable result = new VectorTable();
		Map itemList = new HashMap();
		Integer i = new Integer(1);
		if(config != null && config.size() > 0){		
			for (int j=0;j<config.size();j++) {
				itemList.put(SRNO, i);
	            if (i == 1) {
	            	itemList.put(SELECT, Boolean.TRUE);
	            }
	            else {
	            	itemList.put(SELECT, Boolean.FALSE);
	            }
				object = (SimplePersistentObject)config.get(j);
				String boID = (String)object.getDataMap().get(IBOPostingNarrativeConfig.IDENTIFIERCODE);
				String f_CRNARRATIVEID = (String)object.getDataMap().get(IBOPostingNarrativeConfig.CRNARRATIVEID);
				String f_DRNARRATIVEID = (String)object.getDataMap().get(IBOPostingNarrativeConfig.DRNARRATIVEID);
				java.util.Date f_LASTUPDATEDDTTM = (java.util.Date)object.getDataMap().get(IBOPostingNarrativeConfig.LASTUPDATEDDTTM);
				int versionNum = (Integer)object.getDataMap().get(IBOPostingNarrativeConfig.VERSIONNUM);
				itemList.put(IBOPostingNarrativeConfig.IDENTIFIERCODE, boID);
				itemList.put(IBOPostingNarrativeConfig.CRNARRATIVEID, f_CRNARRATIVEID);
				itemList.put(IBOPostingNarrativeConfig.DRNARRATIVEID, f_DRNARRATIVEID);
				itemList.put(IBOPostingNarrativeConfig.LASTUPDATEDDTTM, f_LASTUPDATEDDTTM);
				itemList.put(IBOPostingNarrativeConfig.VERSIONNUM, versionNum);
				resultConfig.addAll(new VectorTable(itemList));				
				i++;	
			}
		}
		return resultConfig;
	}

	
	public ActivityStepPagingState createActivityStepPagingState() {
        ActivityStepPagingState pagingState = super.createActivityStepPagingState();
        Map supportedData = pagingState.getPagingHelper().getPagingModel();
        if(getF_IN_identifierCode().equals("%") || getF_IN_identifierCode().equals(CommonConstants.EMPTY_STRING)){
            supportedData.put("PAGING_QUERY", query1);
        }else{
            supportedData.put("PAGING_QUERY", query);
        }
        supportedData.put("PAGING_PARAMS", paramsForValues);
        return pagingState;
    }
	
	 public Object processPagingState(BankFusionEnvironment env, ActivityStepPagingState pagingState, Map supportData) {
	        PagingHelper pagingHelper = pagingState.getPagingHelper();
	        Map pagingModel = pagingHelper.getPagingModel();
	        
	        paramsForValues = (ArrayList) pagingModel.get("PAGING_PARAMS");
	        
	        int totalPages = ((Integer) supportData.get(CommonConstants.TOTALPAGES)).intValue();
	        int pageNo = ((Integer) supportData.get(CommonConstants.PAGENO)).intValue();
	        if (totalPages > pageNo)
	        	setF_OUT_gridData_HASMOREPAGES(Boolean.TRUE);
	        else {
	        	setF_OUT_gridData_HASMOREPAGES(Boolean.FALSE);
	        }
	        setF_OUT_gridData_NOOFROWS(numberOfRecords);
	        VectorTable resultGrid = new VectorTable();
	        if (getF_IN_identifierCode().equals("%") || getF_IN_identifierCode().equals(CommonConstants.EMPTY_STRING)) {
	            query1 = (String) pagingModel.get("PAGING_QUERY");
	            Object pageData[] = new Object[4];
                resultGrid = findAllPostingNarrativeConfig(query1, pageNo);                             
                pageData[0] = pageNo; // getRequestedPage()
                pageData[1] = numberOfRecords; // getNumberOfRows()
                pageData[2] = totalPages; // getTotalPages()
                pageData[3] = query1;
                resultGrid.setPagingData(pageData);
                setF_OUT_gridData_TOTALPAGES(totalPages);
                setF_OUT_gridData(resultGrid);    
	        }else{
	        	customerCode =  getF_IN_identifierCode();
	        	query1 = (String) pagingModel.get("PAGING_QUERY");
	        	Object pageData[] = new Object[4];
                resultGrid = fetchPostingNarrativeConfig(query, pageNo);                             
                pageData[0] = pageNo; // getRequestedPage()
                pageData[1] = numberOfRecords; // getNumberOfRows()
                pageData[2] = totalPages; // getTotalPages()
                pageData[3] = query;
                resultGrid.setPagingData(pageData);
                setF_OUT_gridData_TOTALPAGES(totalPages);
                setF_OUT_gridData(resultGrid);          
	            
	        }
	        return (resultGrid);
	    }
}
