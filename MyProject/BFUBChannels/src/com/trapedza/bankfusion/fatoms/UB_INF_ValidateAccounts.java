/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 * $Log: UB_INF_ValidateAccounts.java,v $
 * Revision 1.0  2009/05/05 02:00:35  Jays
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.interfaces.opics.OpicsInterfaceConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_AccountInfMap;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.features.FeatureIDs;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.repository.IRepositoryService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.products.SimpleRuntimeProduct;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_ValidateAccounts;

/**
 * @author Jay
 * @date May 18, 2009
 * @project Universal Banking
 * @Description:to Validate customer's accounts for eligible for interface.
 */
public class UB_INF_ValidateAccounts extends AbstractUB_INF_ValidateAccounts {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}



    private IPersistenceObjectsFactory factory;

    /**
     * Key names of Input and output vector tags
     */
    private static final String ACCOUNT = "ACCOUNT";

    private static final String SELECT = "SELECT";

    private static final String DESCRIPTION = "DESCRIPTION";

    private static final String ACCOUNTID = "ACCOUNTID";

    /**
     * Where clause for UB_INF_AccountInfMap BO
     */
    private static final String whereACCOUNTINTERFACE = " WHERE " + IBOUB_INF_AccountInfMap.ACCOUNTID + " = ? and "
            + IBOUB_INF_AccountInfMap.INTERFACEID + "= ? ";

    /**
     * Holds the reference for logger object
     */
    private static final transient Log logger = LogFactory.getLog(UB_INF_ValidateAccounts.class.getName());

    //private IBOUB_INF_AccountInfMap interfaceAccount = null;

    /**
    *
    * Method Description: Constructor (Implementation of  super class)
    *    
    * @param env   
    */
    public UB_INF_ValidateAccounts(BankFusionEnvironment env) {
        super(env);
    }

    private String extApplication = CommonConstants.EMPTY_STRING;

    /**
    *
    * Method Description: Implementation of abstract method from interface
    *    
    * @param env   
    */
    public void process(BankFusionEnvironment env) throws BankFusionException {

        VectorTable accounts = getF_IN_ACCOUNTS();
        VectorTable vectorTable = getF_OUT_ACCOUNTS();
        extApplication = getF_IN_INTERFACE();
        Object[] accountDetail = null;
        HashMap<String,Object> acc = null;
        HashMap<String,Object> account = null;
        boolean select = false;
        boolean isCreate = isF_IN_IsCreate();
        boolean isBypass = isF_IN_IsBypass();
        boolean isInternalCust = isF_IN_IsInternalCust();
        for (int i = 0; i < accounts.size(); i++) {
            acc = new HashMap<String,Object>();
            account = new HashMap<String,Object>();
            acc.putAll(accounts.getRowTags(i));
            String accountID = (String) acc.get(ACCOUNTID);
            accountDetail = getAccountDetail(accountID, isInternalCust, env);
            select = isActive(accountID, extApplication);
            if(!isBypass) {
	            if (((Boolean) accountDetail[1]).booleanValue()) {
	            	account.put(ACCOUNT, accountID);
	                account.put(SELECT, select);
	                account.put(DESCRIPTION, (String) accountDetail[0]);
	                vectorTable.addAll(new VectorTable(account));
	            }
            } else {
            	if(isCreate && !select && ((Boolean) accountDetail[1]).booleanValue()) {
            		
            			account.put(ACCOUNT, accountID);
    	                account.put(SELECT, select);
    	                account.put(DESCRIPTION, (String) accountDetail[0]);
    	                vectorTable.addAll(new VectorTable(account));
            		
            	} else if(!isCreate && select && ((Boolean) accountDetail[1]).booleanValue()) {
            		
            			account.put(ACCOUNT, accountID);
    	                account.put(SELECT, false);
    	                account.put(DESCRIPTION, (String) accountDetail[0]);
    	                vectorTable.addAll(new VectorTable(account));
            		
            	}
            	
            }
        }
        if(!vectorTable.hasData()){
        	EventsHelper.handleEvent(ChannelsEventCodes.W_NO_ACCOUNT_OF_THIS_CUSTOMER,new Object[]{}, new HashMap(), env); 
        }
        setF_OUT_ACCOUNTS(vectorTable);
    }

    /**
     *
     * Method Description: Get the account description and is account eligible for interface.
     *
     * @param accountID
     * @param env
     * @return Object[]
     */
    private Object[] getAccountDetail(String accountID, boolean isInternalCust, BankFusionEnvironment env) {
        if (logger.isInfoEnabled()) {
            logger.info(ACCOUNT + ":" + accountID);
        }
        Object[] accountDetail = new Object[2];      
        IBOAccount account = null;
        factory = BankFusionThreadLocal.getPersistanceFactory();
        account = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountID, false);
        accountDetail[0] = account.getF_ACCOUNTDESCRIPTION();
        accountDetail[1] = validateAccount(account, isInternalCust, env);
        return accountDetail;

    }

    /**
     *
     * Method Description: Checks the account is eligible for interface.
     *
     * @param IBOAccount
     * @param env
     * @return Boolean
     */
    private Boolean validateAccount(IBOAccount account, boolean isInternalCust, BankFusionEnvironment env) {

        boolean isValidAccount = false;
        String productID = null;
        productID = account.getF_PRODUCTID();
        boolean isClosed = account.isF_CLOSED();
        boolean isStopped = account.isF_STOPPED();
        boolean isDormant = account.isF_DORMANTSTATUS();
        int pwdFlag = account.getF_ACCRIGHTSINDICATOR();
        boolean validPwdflag = (pwdFlag == CommonConstants.INTEGER_ZERO.intValue());        
        IServiceManager sm = ServiceManagerFactory.getInstance().getServiceManager();
        IRepositoryService repositoryService = (IRepositoryService) sm.getServiceForName(ServiceManager.REPOSITORY_SERVICE);
        SimpleRuntimeProduct product = repositoryService.getProduct(productID, env);        
      // SimpleRuntimeProduct product = ProductFactoryProvider.getInstance().getProductFactory().getRuntimeProduct(productID, env);
        Map<String,String> features = product.getAllFeatures(env);
        Boolean hasFeature = features.containsKey(FeatureIDs.INTERFACEACCOUNTFEATURE) ? Boolean.TRUE : Boolean.FALSE;
        Boolean hasNostroFeature = features.containsKey(FeatureIDs.NOSTROFEATURE) ? Boolean.TRUE : Boolean.FALSE;
        if (extApplication.equals(TIPlusModuleConfigurationConstants.TI_SUBCODE_TYPE)) {
            isValidAccount = hasFeature && (!isClosed) && (!isStopped) && validPwdflag;
        }else if(extApplication.equals(OpicsInterfaceConstants.OPICS)){
            isValidAccount = hasFeature && (!isClosed)&&(!isStopped)&&(!isDormant)&& validPwdflag;
        }
        else if(extApplication.equals("TREASURYFO") && isInternalCust) {
        	isValidAccount = hasFeature && hasNostroFeature && (!isClosed)&&(!isStopped)&&(!isDormant)&& validPwdflag;
        } else if(extApplication.equals("TREASURYFO") && !isInternalCust) {
        	isValidAccount = hasFeature && (!isClosed)&&(!isStopped)&&(!isDormant)&& validPwdflag;
        }
        else {
        	isValidAccount = hasFeature && (!isClosed);
        }
        return isValidAccount;
    }

    /**
     *
     * Method Description: this method checks the account is active for interface or not.
     *
     * @param account
     * @param app
     * @return boolean
     */
    private boolean isActive(String account, String app) {

        boolean isActive = false;
        factory = BankFusionThreadLocal.getPersistanceFactory();
        ArrayList<String> params = new ArrayList<String>();
        params.add(account);
        params.add(app);

        List<SimplePersistentObject> records = factory.findByQuery(IBOUB_INF_AccountInfMap.BONAME, whereACCOUNTINTERFACE, params, null, false);
        Iterator<SimplePersistentObject> recordIterator = records.iterator();
        if (recordIterator.hasNext()) {
        	IBOUB_INF_AccountInfMap interfaceAccount = (IBOUB_INF_AccountInfMap) recordIterator.next();
            isActive = interfaceAccount.isF_ISACTIVE();
        }
        return isActive;
    }
}
