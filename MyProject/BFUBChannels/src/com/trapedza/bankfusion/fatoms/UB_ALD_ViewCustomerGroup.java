package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.almonde.AlmondeConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_CUSTOMERGROUPMAP;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_GROUPS;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_ViewCustomerGroup;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * @author Venugopal Pamidipati
 *
 */
public class UB_ALD_ViewCustomerGroup extends AbstractUB_ALD_ViewCustomerGroup{
	
	public static final String cvsRevision = "$Revision: 1.1 $";
    private transient final static Log logger = LogFactory.getLog(UB_ALD_ViewCustomerGroup.class.getName());


    static {
        com.trapedza.bankfusion.utils.Tracer.register(cvsRevision);
    }
    private IPersistenceObjectsFactory factory=null;
    
   
    
    /**
     * @param env
     */
    public UB_ALD_ViewCustomerGroup(BankFusionEnvironment env){
    	super(env);
    }
    /** (non-Javadoc)
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_ViewCustomerGroup#process
     * (com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env){
	
	 factory = BankFusionThreadLocal.getPersistanceFactory();
	 
	 VectorTable resultVector		  = new VectorTable();
	 VectorTable tempCustResultVector = new VectorTable();
	 VectorTable tempGrpResultVector  = new VectorTable();
	 String orderBy                   = CommonConstants.EMPTY_STRING;
	 String whereClauseForReferenceIDs= CommonConstants.EMPTY_STRING;
	 StringBuffer whereClauseForCustomers = null;
	 boolean areCustInGrp = false;
	 boolean areGrpsInGrp = false;
	 Iterator custRefIDs = null;
	// String reqCust                   = CommonConstants.EMPTY_STRING;
	 
	 String grpID= getF_IN_groupID();
	 
	
	 
	 whereClauseForReferenceIDs = AlmondeConstants.WHERE+ IBOUB_ALD_CUSTOMERGROUPMAP.UBGROUPID +" =? " + 
	                              " and "+IBOUB_ALD_CUSTOMERGROUPMAP.UBISCUSTOMER+" =? ";
	 
	 ArrayList params = new ArrayList();
	 
	 
	 //For Retrieving all associated customers of current group
	 
	 params.add(grpID);
	 
	 params.add(new Boolean(true));
	 
	     try {	 
		 	custRefIDs = factory.findByQuery(IBOUB_ALD_CUSTOMERGROUPMAP.BONAME,whereClauseForReferenceIDs,params,null).iterator();
	 	 
		 	whereClauseForCustomers = new StringBuffer(AlmondeConstants.WHERE+IBOCustomer.CUSTOMERCODE + " =? ");
		 	
	
		 	ArrayList params1 = new ArrayList();
	 
	
		 	    while(custRefIDs.hasNext()){
		 	        IBOUB_ALD_CUSTOMERGROUPMAP custList =(IBOUB_ALD_CUSTOMERGROUPMAP) custRefIDs.next();
		 	        params1.add((Object)custList.getF_UBREFERENCEID());
		 	    }
		 	    if(params1.size()>0){
		 	        for(int i=0;i<params1.size()-1;i++){
		 	            whereClauseForCustomers=whereClauseForCustomers.append(AlmondeConstants.reqCust);
		 	        }
		 	        orderBy= AlmondeConstants.ORDERBY+IBOCustomer.CUSTOMERCODE;
		 	        whereClauseForCustomers = whereClauseForCustomers.append(orderBy);
		 	        Iterator<IBOCustomer> custIterator =factory.findByQuery(IBOCustomer.BONAME,whereClauseForCustomers.toString(),params1,null).iterator();
	
		 	       String cust_Type=BankFusionMessages.getInstance().getFormattedEventMessage(40413059, new Object[]{}, BankFusionThreadLocal.getUserSession().getUserLocale());

		 	        while(custIterator.hasNext()){
		 	            IBOCustomer cust = (IBOCustomer)custIterator.next();
		 	            Map record1 = new HashMap();
		 	            record1.put(CommonConstants.SELECT, false);
		 	            record1.put(CommonConstants.SRNO, 1);
		 	            record1.put(AlmondeConstants.CUSTOMERGROUPMAP_REFERENCEID,cust.getBoID() );
		 	            record1.put(AlmondeConstants.GROUPS_DESC,cust.getF_SHORTNAME());
		 	            record1.put(AlmondeConstants.ISCUSTOMER, new Boolean(true));
		 	            record1.put(AlmondeConstants.TYPE,cust_Type );
		 	            tempCustResultVector.addAll(new VectorTable(record1));
		 	        }
		 	    }
	     }
	 
	     catch(NullPointerException e){
		 if(logger.isErrorEnabled())
		 logger.error(e);
	 }
	 params.remove(1);
	 
	 //Retrieving all groups associated with Current Group
	 params.add(new Boolean(false));
	 
	try{
	 	 
	 Iterator grpRefIDs = factory.findByQuery(IBOUB_ALD_CUSTOMERGROUPMAP.BONAME,whereClauseForReferenceIDs,params,null).iterator();
	 
	 StringBuffer whereClauseForGrps = new StringBuffer(AlmondeConstants.WHERE+IBOUB_ALD_GROUPS.UBGROUPIDPK + " =? ");
	
	
	 ArrayList params2 = new ArrayList();
	
	 while(grpRefIDs.hasNext()){
		IBOUB_ALD_CUSTOMERGROUPMAP grpList = (IBOUB_ALD_CUSTOMERGROUPMAP) grpRefIDs.next();
		params2.add(grpList.getF_UBREFERENCEID());
	 }
	 if(params2.size()>0){
	     for(int i=0;i<params2.size()-1;i++){
		whereClauseForGrps=whereClauseForGrps.append(AlmondeConstants.reqGrp);
	     }
	     orderBy= AlmondeConstants.ORDERBY+IBOUB_ALD_GROUPS.UBGROUPIDPK;
	     whereClauseForGrps = whereClauseForGrps.append(orderBy);
	     Iterator grpIterator = factory.findByQuery(IBOUB_ALD_GROUPS.BONAME,whereClauseForGrps.toString(), params2, null).iterator();
	
	       String grp_Type=BankFusionMessages.getInstance().getFormattedEventMessage(40413060, new Object[]{}, BankFusionThreadLocal.getUserSession().getUserLocale());

	
	
	     while(grpIterator.hasNext())
	     {
	         IBOUB_ALD_GROUPS grp = (IBOUB_ALD_GROUPS)grpIterator.next();
	         Map record1 = new HashMap();
	         record1.put(CommonConstants.SELECT, false);
	         record1.put(CommonConstants.SRNO, 1);
	         record1.put(AlmondeConstants.CUSTOMERGROUPMAP_REFERENCEID,grp.getBoID() );
	         record1.put(AlmondeConstants.GROUPS_DESC,grp.getF_UBGROUPDESC());
	         record1.put(AlmondeConstants.ISCUSTOMER, new Boolean(false));
	         record1.put(AlmondeConstants.TYPE,grp_Type);
	         tempGrpResultVector.addAll(new VectorTable(record1));
	     }
	 }
	
	}
	
	catch(NullPointerException e){
		if(logger.isErrorEnabled())
		logger.error(e);
	}
	resultVector.addAll(tempCustResultVector);
	resultVector.addAll(tempGrpResultVector);
	setF_OUT_resultList(resultVector);
 }
 
 
}
