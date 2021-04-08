/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UB_ALD_CustomerGroupValidations,v.1.0,Jun 15, 2009 5:06:48 PM Venugopal Pamidipati
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.almonde.AlmondeConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_CUSTOMERGROUPMAP;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CustomerGroupValidations;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * @author Venugopal Pamidipati
 * @date Jun 15, 2009
 * @project Universal Banking
 * @Description: 
 */

public class UB_ALD_CustomerGroupValidations extends AbstractUB_ALD_CustomerGroupValidations {
    public static final String cvsRevision = "$Revision: 1.1 $";
    private transient final static Log logger = LogFactory.getLog(UB_ALD_CustomerGroupValidations.class.getName());
    
    
     
    private boolean hasErrors = false;

    private BankFusionEnvironment env  = null;
    
    private boolean selectAll = false;

    static {
        com.trapedza.bankfusion.utils.Tracer.register(cvsRevision);
    }
    private IPersistenceObjectsFactory factory;
    /**
     * @param env
     */
    public UB_ALD_CustomerGroupValidations(BankFusionEnvironment env)
    {
    super(env);
    }
    
    
    /** (non-Javadoc)
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CustomerGroupValidations#process
     * (com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    
    
    public void process(BankFusionEnvironment environment)
    {
        VectorTable mainVector = getF_IN_mainGrid();
        VectorTable mainGridCustomers=new VectorTable();
        VectorTable mainGridGrps = new VectorTable();
        boolean isCustRef = isF_IN_addCustomers();
        VectorTable availableList = getF_IN_availableList();
        String grpID = getF_IN_grpID();
        env=environment;
        factory = BankFusionThreadLocal.getPersistanceFactory();
        boolean findingCycle = isF_IN_findingCycles();
        boolean removingElements = isF_IN_removingElements();
        
        selectAll = isF_IN_selectAll();
        
        
        if(!removingElements){
            //Sepperation of Customers and Groups From Main List
            for(int i=0 ;  i<mainVector.size() ; i++){
                Map record = mainVector.getRowTags(i);
                if((Boolean)record.get(AlmondeConstants.ISCUSTOMER))
                mainGridCustomers.addAll(new VectorTable(record));
                
                else
                mainGridGrps.addAll(new VectorTable(record));
            }
            if(!findingCycle){
                
                //For Customers
                if(isCustRef){
                    VectorTable resultVector = resultCustomerList(availableList,mainGridCustomers);
                    if(mainGridGrps.size() != 0)
                    resultVector.addAll(mainGridGrps);
                    setF_OUT_associatedList(resultVector);
                }
                //For Groups
                else{
                    VectorTable resultVector = new VectorTable();
                        
                    if(mainGridCustomers.size() != 0)
                    resultVector.addAll(mainGridCustomers);
                        
                    resultVector.addAll(resultGroupList(availableList,mainGridGrps,grpID));
                    setF_OUT_associatedList(resultVector);
                }
            }
            else{
                Object parentGrpId=findRootNodes(grpID,grpID);
        
                for(int i=0;i<mainGridGrps.size();i++){
                    Map record = mainGridGrps.getRowTags(i);
                    if(parentGrpId.equals(record.get(AlmondeConstants.CUSTOMERGROUPMAP_REFERENCEID))){
                        hasErrors = true;
                            
                    }
                }
                setF_OUT_hasErrors(hasErrors);
                setF_OUT_parentGrpId((String)parentGrpId);
        
            }
        }
        else{
            Object[] selectedRows = mainVector.getColumn(CommonConstants.SELECT);
            VectorTable tempRemainigList = new VectorTable();
            VectorTable tempRemovingList = new VectorTable();
            for(int i=0;i<mainVector.size();i++){
                Map record = mainVector.getRowTags(i);
                if((Boolean)selectedRows[i]){
                    tempRemovingList.addAll(new VectorTable(record));
                    hasErrors = true;
                }
                else{
                    record.put(CommonConstants.SELECT, false);
                    tempRemainigList.addAll(new VectorTable(record));
                }
            }
            setF_OUT_removingList(tempRemovingList);
            setF_OUT_associatedList(tempRemainigList);
            setF_OUT_hasErrors(hasErrors);
        }
        
    }
     
    /**
     * Method Description:Methode for Adding Available Customers and Validations
     * @param tempRightVector
     * @param tempMainCustList
     * @return
     */
    private VectorTable resultCustomerList(VectorTable tempRightVector,VectorTable tempMainCustList)
    {
        Object[] selectedRows = tempRightVector.getColumn(CommonConstants.SELECT);
	    String cust_Type=BankFusionMessages.getInstance().getFormattedEventMessage(40413059, new Object[]{}, BankFusionThreadLocal.getUserSession().getUserLocale());
	    String cust_BlackListed=BankFusionMessages.getInstance().getFormattedEventMessage(40413062, new Object[]{}, BankFusionThreadLocal.getUserSession().getUserLocale());
	    String cust_Duplicate=BankFusionMessages.getInstance().getFormattedEventMessage(40413061, new Object[]{}, BankFusionThreadLocal.getUserSession().getUserLocale());

        VectorTable errorListCuatomers = new VectorTable();
        try{
            for(int i=0;i < tempRightVector.size();i++){
                Map <String,Object> record = tempRightVector.getRowTags(i);
            
                if((Boolean) selectedRows[i] || selectAll){
                    //Finding Customer Already Associated
                    if(tempMainCustList.size()>0 && isReferenceAssociated(record.get(AlmondeConstants.BOID),tempMainCustList)){
                        hasErrors = true;
                        record.put(AlmondeConstants.COMMENTS,cust_Duplicate);
                        errorListCuatomers.addAll(new VectorTable(record));
                    }
                    else{   
                        //Finding Block listed Customers in Associated Customers list
                        Map fircoSoftRec = new HashMap();
                        fircoSoftRec.put(AlmondeConstants.CUSTOMERCODE, record.get(AlmondeConstants.BOID));
                        //fircoSoftRec.put(KYCCHECK,new Boolean(true));
                        Map blockListMap=MFExecuter.executeMF(AlmondeConstants.UB_CNF_ReadKYCStatus_SRV, env, fircoSoftRec);
            
                        if((Boolean)blockListMap.get(AlmondeConstants.CONTINUE)){           
                            Map record1 = new HashMap<String,Object>();
                            record1.put(CommonConstants.SELECT, false);
                            record1.put(CommonConstants.SRNO, i+1);
                            record1.put(AlmondeConstants.CUSTOMERGROUPMAP_REFERENCEID,record.get(AlmondeConstants.BOID) );
                            record1.put(AlmondeConstants.GROUPS_DESC, record.get(AlmondeConstants.CUSTOMERNAME));
                            record1.put(AlmondeConstants.ISCUSTOMER, new Boolean(true));
                            record1.put(AlmondeConstants.TYPE,cust_Type);
                            tempMainCustList.addAll(new VectorTable(record1));
                        }
            
                        else
                        {
                            hasErrors = true;
                            record.put(AlmondeConstants.COMMENTS,cust_BlackListed);
                            errorListCuatomers.addAll(new VectorTable(record));
                        }
                    }
                }
            }
        }
        catch(ArrayIndexOutOfBoundsException a)
        {
           logger.error(a); 
        }
        setF_OUT_hasErrors(hasErrors);
        setF_OUT_errorsList(errorListCuatomers);
        return tempMainCustList;
    }
    
     /**
     * Method Description:Methode for finding Item (Customer/Group)already associated or not
     * @param referenceId
     * @param tempMainReferecesList
     * @return
     */
    private boolean isReferenceAssociated(Object referenceId,VectorTable tempMainReferecesList ){
        
        Object[] list = tempMainReferecesList.getColumn(AlmondeConstants.CUSTOMERGROUPMAP_REFERENCEID);
        for(int i=0;i<list.length;i++){
            if(referenceId.equals(list[i]))
                return true;
        }
        return false;
    }
    
    
    /**
     * Method Description:Methode for adding Available Groups and validations
     * @param tempRightVector
     * @param tempMainGrpList
     * @param grpID
     * @return
     */
    private VectorTable resultGroupList(VectorTable tempRightVector,VectorTable tempMainGrpList,String grpID)
    {
        Object[] selectedRows = tempRightVector.getColumn(CommonConstants.SELECT);
        VectorTable errorListGrps = new VectorTable();
	     String grp_Type=BankFusionMessages.getInstance().getFormattedEventMessage(40413060, new Object[]{}, BankFusionThreadLocal.getUserSession().getUserLocale());
		 String grp_Duplicate=BankFusionMessages.getInstance().getFormattedEventMessage(40413063, new Object[]{}, BankFusionThreadLocal.getUserSession().getUserLocale());

                
        try{
            for(int i=0;i < tempRightVector.size();i++){
                Map <String,Object> record = tempRightVector.getRowTags(i);
            
                if((Boolean) selectedRows[i] || selectAll){
                    if(tempMainGrpList.size()>0 && isReferenceAssociated(record.get(AlmondeConstants.BOID),tempMainGrpList))
                    {
                        hasErrors = true;
                        record.put(AlmondeConstants.COMMENTS,grp_Duplicate);
                        errorListGrps.addAll(new VectorTable(record));
                    }
                    else{                   
                        Map record1 = new HashMap<String,Object>();
                        record1.put(CommonConstants.SELECT,false);
                        record1.put(CommonConstants.SRNO, i+1);
                        record1.put(AlmondeConstants.CUSTOMERGROUPMAP_REFERENCEID,record.get(AlmondeConstants.BOID) );
                        record1.put(AlmondeConstants.GROUPS_DESC, record.get(AlmondeConstants.DESC));
                        record1.put(AlmondeConstants.ISCUSTOMER, new Boolean(false));
                        record1.put(AlmondeConstants.TYPE,grp_Type);
                        tempMainGrpList.addAll(new VectorTable(record1));
                    }
                }
            }
        }
        catch(ArrayIndexOutOfBoundsException a){
            logger.info("One of the coloumn in the table doesnt have values upto table size");
            logger.error(a);
        }
        setF_OUT_hasErrors(hasErrors);
        setF_OUT_errorsList(errorListGrps);
        return tempMainGrpList;
    }
    
      
    /**
     * Method Description:This is the Methode for finding Cycles and Root node of the Selected Group
     * @param grp
     * @param grpID
     * @return
     */
    private Object findRootNodes(Object grp,String grpID){
        
        Iterator  immediateParents= null;
        ArrayList params = new ArrayList();
        IBOUB_ALD_CUSTOMERGROUPMAP parentGrp=null;
        try{
                params.add(grp);
                immediateParents = factory.findByQuery(IBOUB_ALD_CUSTOMERGROUPMAP.BONAME,AlmondeConstants.WHEREFORFINDINGPARENT, params, null).iterator();
                
                if(immediateParents.hasNext()) {
                    parentGrp = (IBOUB_ALD_CUSTOMERGROUPMAP)immediateParents.next();
                    if(grpID.equals(parentGrp.getF_UBGROUPID())){
                        hasErrors = true;
                        return grpID;
                        }
                    return findRootNodes((Object)parentGrp.getF_UBGROUPID(),grpID);
                }
                                
            }
        catch(NullPointerException n){
        	logger.error(n);
            return grp;
        }
        catch(StackOverflowError e){
        	logger.error(e);
            hasErrors = true; 
            }
        return grp; 
    }
    
    



}

