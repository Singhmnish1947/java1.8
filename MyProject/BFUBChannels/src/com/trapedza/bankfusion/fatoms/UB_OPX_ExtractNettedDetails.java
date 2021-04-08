package com.trapedza.bankfusion.fatoms;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.misys.bankfusion.common.runtime.service.*;    
import com.misys.bankfusion.common.runtime.service.ServiceManager;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.common.service.IServiceManager;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuterWrapper;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.IPersistenceService;
import com.misys.bankfusion.subsystem.task.runtime.impl.BankFusionThreadLocalWrapper;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;

import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;


import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_ExtractNettedDetails;


public class UB_OPX_ExtractNettedDetails extends AbstractUB_OPX_ExtractNettedDetails {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String svnRevision = "$Revision: 1.0 $";

	private final IPersistenceObjectsFactory factory;
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}	
	String queryForNettedDealAccountDetails = "SELECT  acc." +IBOAccount.ISOCURRENCYCODE
	+ " AS ISOCURRENCYCODE,  acc."+IBOAccount.CUSTOMERCODE
	+ " AS CUSTOMERCODE, acc." + IBOAccount.PRODUCTID + " AS PRODUCTID  FROM "
	+ IBOAccount.BONAME +" acc WHERE acc." + IBOAccount.ACCOUNTID + " = ? ";
	
	String queryForNettedDealTransationDetails = "SELECT  txn." + IBOTransaction.USERID  + " AS USERID, txn."
	+IBOTransaction.SOURCEBRANCH + " AS SOURCEBRANCH, txn."
	+IBOTransaction.TRANSACTIONDATE + " AS TRANSACTIONDATE, txn."
	+IBOTransaction.VALUEDATE + " AS VALUEDATE, txn."
	+IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " AS ACCOUNTID, txn."
	+IBOTransaction.REFERENCE + " AS REFERENCE, txn."
	+IBOTransaction.ISOCURRENCYCODE + " AS ISOCURRENCYCODE, txn."
	+IBOTransaction.EXCHANGERATE + " AS EXCHANGERATE FROM " + IBOTransaction.BONAME + " txn WHERE txn." + IBOTransaction.TRANSACTIONSRID + " = ? "; 

	private static String microflowNameOne = "UB_OPX_NettedNostroTXNDetails_SRV";
	private static	String microflowNameTwo = "UB_OPX_NettedPositionTXNDetails_SRV";
	private static	String microflowNameThree = "UB_OPX_RetrieveNostroTXNDetails_SRV";
	private static	String microflowNameFour = "UB_OPX_RetrievePositionTXNDetails_SRV";
	private static	String microflowNameFive = "getcustomerinfo";
	private static	String Code = "custnum";
	private static	String nettedTXNID = "NettedTXNID";
	
	private ArrayList<Object> params = new ArrayList<Object>();


	public UB_OPX_ExtractNettedDetails(BankFusionEnvironment env){
		super(env);
		factory = getFactory(true);
		
	}
	
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		Boolean returnNetted = isF_IN_returnDummyDeals();
		
		if(returnNetted.equals(true)){
			
		MFExecuterWrapper mfExec = new MFExecuterWrapper();
		BankFusionThreadLocalWrapper bfThreadLocal = new BankFusionThreadLocalWrapper();
		HashMap resultOne = mfExec.executeMF(microflowNameOne, bfThreadLocal
				.getBankFusionEnvironment(), null, true);
		
		HashMap resultTwo = mfExec.executeMF(microflowNameTwo, bfThreadLocal
				.getBankFusionEnvironment(), null, true);
        VectorTable nettedDetailsOne = (VectorTable)resultOne.get("NettedTXNDetails");	
        VectorTable nettedDetailsTwo = (VectorTable)resultTwo.get("PositionNettedTXN");
        VectorTable result =new VectorTable();
        int countOne = nettedDetailsOne.size();
        int countTwo = nettedDetailsTwo.size();
        resultOne.clear();
        resultTwo.clear();

        for (int i = 0; i < countOne; i++) {
        	
        	HashMap rowTag = new HashMap();
            HashMap rowTags = nettedDetailsOne.getRowTags(i);
            String paramName = (String) rowTags.get("OPICSDEALNO").toString();
            rowTag.put("TRANSACTIONREFERENCE", paramName); 
    		rowTag.put("SELECT", false);
    		result.addAll(new VectorTable(rowTag));
    		rowTag.clear();
       }
       
        for (int i = 0; i< countTwo; i++) {
        	
        	HashMap rowTag = new HashMap();
            HashMap rowTags = nettedDetailsTwo.getRowTags(i);
            String paramName = (String) rowTags.get("OPICSDEALNO").toString();
    		rowTag.put("TRANSACTIONREFERENCE", paramName); 
    		rowTag.put("SELECT", false);
    		result.addAll(new VectorTable(rowTag));
    		rowTag.clear();
    		  
       }
        Integer rows = result.size();
        setF_OUT_NettedDealDetails_NOOFROWS(rows);
        setF_OUT_NettedDealDetails(result);
       
        
		} else 
		{
			String nettedID = getF_IN_NettedTXNID();
			VectorTable result =new VectorTable();
			Integer length = nettedID.length();
			boolean number = false;
			
			  try  
			  {  
			    double d = Double.parseDouble(nettedID);  
			  }  
			  catch(NumberFormatException nfe)  
			  {  
			      number = true;
			  }  
			 
			
			if(!(nettedID==null || length.equals(new Integer(0))|| number)){
			
			HashMap param = new HashMap();
			param.put(nettedTXNID, nettedID);
			
			MFExecuterWrapper mfExec = new MFExecuterWrapper();
			BankFusionThreadLocalWrapper bfThreadLocal = new BankFusionThreadLocalWrapper();
			HashMap resultOne = mfExec.executeMF(microflowNameThree, bfThreadLocal
					.getBankFusionEnvironment(), param, true);
			VectorTable nettedDetailsOne = (VectorTable)resultOne.get("NostroDetails");	
			int countOne = nettedDetailsOne.size();
			
	        if(countOne > 0) {   
	        	
	        for (int i = 0; i < countOne; i++) {
	        	
	        	HashMap rowTag = new HashMap();
	            HashMap rowTags = nettedDetailsOne.getRowTags(i);
	            String paramName = (String) rowTags.get("BOID");
	            BigDecimal dealAmount = (BigDecimal) rowTags.get("AMOUNT");
	            Date   processDate = null;
	            Date   valueDate = null;
	            String branchCode = null;
	            String currencyOne= null;
	            String currencyTwo = null;
	           	String customerCode= null;
	        	String custCode= null;
	    		String cutomerName= null;
	    		String productType= null;
	    		String user= null;
	    		String transactionId = null;
	    		String dealFlag = null;
	    		BigDecimal exchangeRate = null;	    		
	    		params.add(paramName);
	    		 
	            List<SimplePersistentObject> resultSet = factory.executeGenericQuery(queryForNettedDealTransationDetails, params, null, false);
	            params.clear();
	           if(resultSet.size()!=0){
	            

	            for(SimplePersistentObject obj : resultSet ){
		            Map rowData = null;
	            	rowData = obj.getDataMap();	
	            	if (rowData != null) {
	            		
	    				 currencyOne = (String) rowData.get("ISOCURRENCYCODE");
 	            		 branchCode = (String) rowData.get("SOURCEBRANCH");
 	            		 customerCode =  (String) rowData.get("ACCOUNTID");
 	            		 user = (String) rowData.get("USERID");
	    				 exchangeRate = (BigDecimal)rowData.get("EXCHANGERATE");
	    		         processDate = (Date) rowData.get("TRANSACTIONDATE");
	    		         valueDate = (Date) rowData.get("VALUEDATE");
	    				 transactionId = (String) rowData.get("REFERENCE");
	    				 rowData.clear();
	    				params.add(customerCode);
	    		        List<SimplePersistentObject> resultSetTxn = factory.executeGenericQuery(queryForNettedDealAccountDetails, params, null, false);	
	    		        params.clear();
	    		        
	    		        
	    		        
	    	            for(SimplePersistentObject txn : resultSetTxn ){
	    	            	rowData = txn.getDataMap();	
	    	            	if (rowData != null) {
	   	    				 	currencyTwo =  (String) rowData.get("ISOCURRENCYCODE");
	   	    				 	productType = (String) rowData.get("PRODUCTID");
	   	    				 	custCode=     (String) rowData.get("CUSTOMERCODE");
	
	   	    				 	rowData.clear();
	     	            	}
	    	            }
	    	            resultSetTxn.clear();
	            	}
	            }
	            resultSet.clear();
	            param.put( Code, custCode);
				HashMap customerDetails = mfExec.executeMF(microflowNameFive, bfThreadLocal
						.getBankFusionEnvironment(), param, true);
				String customerName = (String)customerDetails.get("SHORTNAME");
				customerDetails.clear();
	            rowTag.put("BRANCHCODE", branchCode); 
	           	rowTag.put("CURRENCY1", currencyOne);
	            rowTag.put("CURRENCY2", currencyTwo);
	            rowTag.put("CUSTOMERCODE", custCode);
	            rowTag.put("CUSTOMERNAME", customerName);
	            rowTag.put("DEALPROCESSDATE", processDate);
	            rowTag.put("DEALVALUEDATE", valueDate);
	            rowTag.put("PRODUCTTYPE", productType);
	            rowTag.put("TRANSACTIONREFERENCE", paramName);
	            rowTag.put("USER", user);
	            rowTag.put("DEALAMOUNT", dealAmount);
	            rowTag.put("EXCHANGERATE", exchangeRate);
	    		rowTag.put("SELECT", false);
	    		rowTag.put("TRANSACTIONID", transactionId);
	    		rowTag.put("DEALFLAG", "");
	    		
	    		result.addAll(new VectorTable(rowTag));
	    		rowTag.clear();
	    		setF_OUT_nostro(true);
		        setF_OUT_NettedDealDetails(result);
		        Integer rows = result.size();
		        setF_OUT_NettedDealDetails_NOOFROWS(rows);

	    		
	       }
	        } 
	        }else {
	        
			HashMap resultTwo = mfExec.executeMF(microflowNameFour, bfThreadLocal
					.getBankFusionEnvironment(), param, true);
	        param.clear();
	        VectorTable nettedDetailsTwo = (VectorTable)resultTwo.get("PositionDetails");
	        
	        
	        int countTwo = nettedDetailsTwo.size();
	        
	        for (int i = 0; i< countTwo; i++) {
	        	
	        	
	        	HashMap rowTag = new HashMap();
	            String branchCode = null;
	           	String customerCode= null;
	           	String accountID= null;
	    		String cutomerName= null;
	    		String productType= null;
	    		String user= null;
	            Date   processDate = null;
	            Date   valueDate = null;
	    		String transactionId = null;
	    		String dealFlag = null;


	            HashMap rowTags = nettedDetailsTwo.getRowTags(i);
	            String paramName = (String) rowTags.get("BOID");
	            String currncyOne = (String) rowTags.get("CURRENCYCODE1");
	            String currencyTwo = (String) rowTags.get("CURRENCYCODE2");
	            BigDecimal exchangeRate = (BigDecimal) rowTags.get("EXCHANGERATE");
	            BigDecimal dealAmount = (BigDecimal) rowTags.get("AMOUNT1");
	            
	            dealFlag = (String) rowTags.get("DEALFLAG");	            
	            params.add(paramName);
	            List<SimplePersistentObject> resultSet = factory.executeGenericQuery(queryForNettedDealTransationDetails, params, null, false);
	            params.clear();
	            
	            if(resultSet.size()!=0){
	            for(SimplePersistentObject obj : resultSet ){
		            Map rowData = null;
	            	rowData = obj.getDataMap();	
	            	if (rowData != null) {
	    				 user = (String) rowData.get("USERID");
 	            		 branchCode = (String) rowData.get("SOURCEBRANCH");
 	            		 accountID = (String) rowData.get("ACCOUNTID");
	    		         processDate = (Date) rowData.get("TRANSACTIONDATE");
	    		         valueDate = (Date) rowData.get("VALUEDATE");
	    				 transactionId = (String) rowData.get("REFERENCE");   		         
	    				 rowData.clear();
	    				 
	    				params.add(accountID);
	    		        List<SimplePersistentObject> resultSetTxn = factory.executeGenericQuery(queryForNettedDealAccountDetails, params, null, false);	
	    		        params.clear();
	    		        
	    	            for(SimplePersistentObject txn : resultSetTxn ){
	    	            	rowData = txn.getDataMap();	
	    	            	if (rowData != null) {
	   	    				 customerCode = (String) rowData.get("CUSTOMERCODE");
		    				 productType = (String) rowData.get("PRODUCTID");
	   	    			     //transactionId = (String) rowData.get("REFERENCE");    				 
		    				 rowData.clear();
	     	            	}
	    	            }
	    	            resultSetTxn.clear();
	            	}
	            }


	            resultSet.clear();
	            param.put(Code, customerCode);
				HashMap customerDetails = mfExec.executeMF(microflowNameFive, bfThreadLocal
						.getBankFusionEnvironment(), param, true);
				String customerName = (String)customerDetails.get("SHORTNAME");
				customerDetails.clear();
	            rowTag.put("BRANCHCODE", branchCode); 
	            rowTag.put("CURRENCY1", currncyOne);
	            rowTag.put("CURRENCY2", currencyTwo);
	            rowTag.put("CUSTOMERCODE", customerCode);
	            rowTag.put("CUSTOMERNAME", customerName);
	            rowTag.put("DEALPROCESSDATE", processDate);
	            rowTag.put("DEALVALUEDATE", valueDate);
	            rowTag.put("PRODUCTTYPE", productType);
	            rowTag.put("TRANSACTIONREFERENCE", paramName);
	            rowTag.put("USER", user);
	            rowTag.put("DEALAMOUNT", dealAmount);
	            rowTag.put("EXCHANGERATE", exchangeRate);
	    		rowTag.put("SELECT", false);
	    		rowTag.put("TRANSACTIONID", transactionId);
	    		rowTag.put("DEALFLAG", dealFlag);
	    		result.addAll(new VectorTable(rowTag));
	    		rowTag.clear();
	    		setF_OUT_position(true);
		        setF_OUT_NettedDealDetails(result);
		        Integer rows = result.size();
		        setF_OUT_NettedDealDetails_NOOFROWS(rows);

	    		
	       }
	        
      } 
	      
	        
	        

			}
			}
				
			
			else{
				
		          Integer rows = result.size();
		          setF_OUT_NettedDealDetails_NOOFROWS(rows);
		          setF_OUT_NettedDealDetails(result);
		         
			     }
		
			
		}
        
	}

	private IPersistenceObjectsFactory getFactory(boolean readOnly) {
		IServiceManager smgr = ServiceManagerFactory.getInstance()
				.getServiceManager();
		IPersistenceService pService = (IPersistenceService) smgr
				.getServiceForName(ServiceManager.PERSISTENCE_SERVICE);
		return pService.getPrivatePersistenceFactory(readOnly);
	}

}
