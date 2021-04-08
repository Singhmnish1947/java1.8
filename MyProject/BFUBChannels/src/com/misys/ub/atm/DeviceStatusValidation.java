package com.misys.ub.atm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.ub.types.atm.CardIssuerData;
import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;
import bf.com.misys.ub.types.iso8583.UB_Financial_Details;

import com.misys.bankfusion.events.IBusinessEventsService;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCIB;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOExternalLoroSettlementAccount;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractDeviceStatusValidation;
/**
 * @author Prateek 
 * @date 10 Nov 2016
 * @project FBE
 * @Description This class file is used to Validate the device status of CIB, LORO and IMDCODE.
 * 
 */
public class DeviceStatusValidation extends AbstractDeviceStatusValidation{

	private static final long serialVersionUID = -703591357156228737L;
	private String accIDC;
	private String cardIssAuthData;
	private String indicator;
	private String cardAccpTerId;
	private IPersistenceObjectsFactory factory;
	private final String fetchCIBDeviceStatus = " WHERE " + IBOATMCIB.IMDCODE + "= ?" + "AND " + IBOATMCIB.BRANCHCODE + "= ?";;
	private final String fetchATMPOSDeviceStatus = " WHERE " + IBOATMSettlementAccount.ATMDEVICEID + "= ?";
	private final String fetchExternalLORODeviceStatus= " WHERE " + IBOExternalLoroSettlementAccount.ID + "= ?";
	private final String READ_BRANCH_MF_NAME = "CB_BRN_ReadBranch_SRV";
	
	public DeviceStatusValidation(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env){
		 
		 accIDC=getF_IN_accIDC();
		 cardIssAuthData=getF_IN_cardIssAuthData();
		 cardAccpTerId=getF_IN_cardAccpTerId();
		 indicator =getIndicator(accIDC, cardIssAuthData);
		 validateDeviceStatusAndRaiseEvent(indicator, accIDC, cardIssAuthData, cardAccpTerId, env);
	}
	
	 public String getIndicator(String accIDC, String cardIssAuthData){
		 HashMap paramsForIndicator = new HashMap();
		 HashMap result = null;
		 UB_ATM_Financial_Details finDetails = new UB_ATM_Financial_Details();
		 UB_Financial_Details acceptor= new UB_Financial_Details();
		 CardIssuerData issuer = new CardIssuerData();
		 acceptor.setAcquiringInstitutionId(accIDC);
		 issuer.setCardIssuerFIID(cardIssAuthData);
		 finDetails.setCardIssuerData(issuer);
		 finDetails.setFinancialDetails(acceptor);
		
		 paramsForIndicator.put("AtmPosting", finDetails);
		 result = MFExecuter.executeMF("UB_ATM_ExternalLoroIndicator_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), paramsForIndicator);
		 indicator= (String)result.get("ExternalLoroTxnIndicator");
		
		 return indicator;
		
	} 
	 public void validateDeviceStatusAndRaiseEvent(String indicator, String accIDC, String cardIssAuthData, String cardAccpTerId, BankFusionEnvironment env){
		 boolean isCIBActive=false;
		 boolean isATMPOSActive=false;
		 boolean isExtLoroActive=false;
		 String branch = "";
		 factory = BankFusionThreadLocal.getPersistanceFactory();
		 if (indicator.equals("LOCALTXN")) {
			 
			 branch=getBranchforIMD(cardAccpTerId);
			 isATMPOSActive=fetchATMPOSDeviceStatus(cardAccpTerId, env);
			 
			 if(isATMPOSActive==false){
				 handleEvent(40430033, new String[] {});
			 }
			 
			 isCIBActive=fetchCIBDeviceStatus(accIDC, branch);
			 
			 if(isCIBActive==false){
				 handleEvent(40430035, new String[] {});
			 }
             isCIBActive=fetchCIBDeviceStatus(cardIssAuthData, branch);
			 
			 if(isCIBActive==false){
				 handleEvent(40430035, new String[] {});
			 }
		 }
		 else if (indicator.equals("EXTTXN")){
			 isExtLoroActive=fetchEXTLORODeviceStatus(accIDC);
			 if(isExtLoroActive==false){
				 handleEvent(40430034, new String[] {});
			 }
		 }
		 else if (indicator.equals("LOROTXN")){
			 branch=getBranchforIMD(cardAccpTerId);
			 isExtLoroActive=fetchEXTLORODeviceStatus(cardIssAuthData);
			 if(isExtLoroActive==false){
				 handleEvent(40430034, new String[] {});
			 }
              isCIBActive=fetchCIBDeviceStatus(accIDC, branch);
			 
			 if(isCIBActive==false){
				 handleEvent(40430035, new String[] {});
			 }
			
		 }
	 }
	 public boolean fetchCIBDeviceStatus(String id, String branch) {
		 ArrayList params = new ArrayList();
		 IBOATMCIB cibStatus;
		 params.add(id);
		 params.add(branch);
		 cibStatus=(IBOATMCIB) factory.findFirstByQuery(IBOATMCIB.BONAME, fetchCIBDeviceStatus, params);
		 params.clear();
		 if(cibStatus!= null){
			 return cibStatus.isF_UBACTIVE();
		 } else {
			 handleEvent(40407547, new String[] {id});
			 return false;
		 }
	 }
	 public boolean fetchATMPOSDeviceStatus(String id, BankFusionEnvironment env) {
		 ArrayList params = new ArrayList();
		 IBOATMSettlementAccount atmPOSStatus;
		 params.add(id);
		 atmPOSStatus=(IBOATMSettlementAccount) factory.findFirstByQuery(IBOATMSettlementAccount.BONAME, fetchATMPOSDeviceStatus, params);
		 params.clear();
		 if(atmPOSStatus!= null){
			 return atmPOSStatus.isF_UBACTIVE();
		 } else {
			 handleEvent(40407547, new String[] {id});
			 return false;
		 }
	 }
	 public boolean fetchEXTLORODeviceStatus(String id){
		 ArrayList params = new ArrayList();
		 IBOExternalLoroSettlementAccount atmExtLoroStatus;
		 params.add(id);
		 atmExtLoroStatus=(IBOExternalLoroSettlementAccount) factory.findFirstByQuery(IBOExternalLoroSettlementAccount.BONAME, fetchExternalLORODeviceStatus, params);
		 params.clear();
		 if(atmExtLoroStatus!= null){

			 return atmExtLoroStatus.isF_UBACTIVE();
		 } else {
			 handleEvent(40407547, new String[] {id});
			 return false;
		 }
	 }
	 
	 public String getBranchforIMD(String deviceID){
		 ArrayList params = new ArrayList();
		 IBOATMSettlementAccount atmDeviceDetails;
		 params.add(deviceID);
		 atmDeviceDetails=(IBOATMSettlementAccount) factory.findFirstByQuery(IBOATMSettlementAccount.BONAME, fetchATMPOSDeviceStatus, params);
		 params.clear();
		 String branch = "";
		 if(atmDeviceDetails != null){
			 branch=atmDeviceDetails.getF_UBBRANCH();
	     }
		 else{
			 branch=BankFusionThreadLocal.getBankFusionEnvironment().getUserBranch();
		 }
		 
		 Map inputParams = new HashMap();
	     inputParams.put("BranchBusinessKey",branch);
	     inputParams.put("BusinessKeyColumnName", "BranchSortCode");
	     Map outputParams = MFExecuter.executeMF(READ_BRANCH_MF_NAME, BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
	     return (String)outputParams.get("BMBranch");
	 }
	 
	 private void handleEvent(Integer eventNumber, String[] args) {
	        if (args == null) {
	            args = new String[] { CommonConstants.EMPTY_STRING };
	        }
	        Event event = new Event();
	        event.setEventNumber(eventNumber);
	        event.setMessageArguments(args);
	        IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance()
	                .getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
	        businessEventsService.handleEvent(event);
	 }
}
