package com.misys.ub.fatoms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.misys.bankfusion.serviceinvocation.ISPIInvoker;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_ACCTMANDATE;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class GetJointAccountsForCustomer implements ISPIInvoker {

    private static final String FIND_JOINT_ACCOUNTS = "SELECT Acc." + IBOAttributeCollectionFeature.ACCOUNTDESCRIPTION + " AS "
            + IBOAttributeCollectionFeature.ACCOUNTDESCRIPTION + ", Acc." + IBOAttributeCollectionFeature.ACCOUNTID + " AS "
            + IBOAttributeCollectionFeature.ACCOUNTID + ", Acc." + IBOAttributeCollectionFeature.ACCOUNTNAME + " AS "
            + IBOAttributeCollectionFeature.ACCOUNTNAME + ", Acc." + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " AS "
            + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " FROM " + IBOAttributeCollectionFeature.BONAME + " Acc, "
			+ IBOUB_CNF_ACCTMANDATE.BONAME + " M, "
			+ IBOProductInheritance.BONAME + " P " 
			+ "WHERE M." + IBOUB_CNF_ACCTMANDATE.UBACCOUNTID + " = Acc." + IBOAttributeCollectionFeature.ACCOUNTID + " AND Acc." 
			+ IBOAttributeCollectionFeature.PRODUCTCONTEXTCODE + "= P." + IBOProductInheritance.PRODUCTCONTEXTCODE +
			" AND  M." + IBOUB_CNF_ACCTMANDATE.UBCUSTOMERCODE + " = ? AND Acc."
			+ IBOAttributeCollectionFeature.CLOSED + " = 'N'"
			+" AND Acc." + IBOAttributeCollectionFeature.JOINTACCOUNT + " = 'Y' "
            + " AND Acc." + IBOAttributeCollectionFeature.STOPPED + " = 'N' ";
    private static final Log LOGGER = LogFactory.getLog(GetJointAccountsForCustomer.class);
    
    
    

	@SuppressWarnings("unchecked")
	@Override
	public Object invokeService(Object input) {
		ArrayList<String> accountNumbers = new ArrayList<String>();
        String customerCode = (String)input;
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Fetching joint accounts for the customer::"+customerCode);
		}
        @SuppressWarnings("rawtypes")
		ArrayList params = new ArrayList();
        params.add(customerCode);
        List<SimplePersistentObject> accountList = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(FIND_JOINT_ACCOUNTS, params, null, false);
        for (SimplePersistentObject accountlist : accountList) {
        	accountNumbers.add(accountlist.getDataMap().get(IBOAttributeCollectionFeature.ACCOUNTID).toString());
          
        }
        return accountNumbers;
    }
    
	
    
}
