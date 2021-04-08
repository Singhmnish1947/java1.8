package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import bf.com.misys.cbs.msgs.cards.v1r0.MaintainCustCardRq;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.CustCrdBasicDtls;
import bf.com.misys.cbs.types.CustCrdBasicInput;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMS_ProcessCMSRequest;

public class UB_CMS_ProcessCMSRequest extends AbstractUB_CMS_ProcessCMSRequest{

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	
	
	private BankFusionEnvironment environment;
    
    public UB_CMS_ProcessCMSRequest(){
    	
    }
    
    public UB_CMS_ProcessCMSRequest(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param env Environment
     * 
     * 
     */ArrayList nameOfTheFields ;
      @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
    	  this.environment = env;
    	  
    	  ConvertFileDataToVector convFileDataToVect = new ConvertFileDataToVector();
    	  convFileDataToVect.setFileName(getF_IN_fileName());
    	  UB_CMS_ReadPropertyFile readPropertyFile = new UB_CMS_ReadPropertyFile();
  		  
    	  ArrayList fieldList = readPropertyFile.createFieldsList();
    	  VectorTable  resultVector =convFileDataToVect.readFile(fieldList); 
    	  MaintainCustCardRq request = convertVectorToComplexType(resultVector);
    	  setF_OUT_maintainCustCardRq(request);
    }

	private MaintainCustCardRq convertVectorToComplexType(VectorTable resultVector) {
		MaintainCustCardRq request = new MaintainCustCardRq();
		CustCrdBasicInput requestInput = new CustCrdBasicInput();
		int size = resultVector.size();
		
		
		CustCrdBasicDtls[] arrCardBasicDetails = new CustCrdBasicDtls[size];
		HashMap columnMap = new HashMap();
		for (int index = 0; index < size; index++) {
			CustCrdBasicDtls cardDetails = new CustCrdBasicDtls();
			columnMap = resultVector.getRowTags(index);
			cardDetails.setCardNumber(String.valueOf(columnMap.get("ATMCARDNUMBER")).trim());
			AccountKeys accountKeys = new AccountKeys();
			accountKeys.setStandardAccountId(String.valueOf(columnMap.get("ACCOUNTNO")).trim());
			cardDetails.setAccountID(accountKeys);
			cardDetails.setCardStatus(String.valueOf(columnMap.get("UBCARDSTATUS")).trim());
			cardDetails.setCardType(String.valueOf(columnMap.get("UBCARDTYPE")).trim());
			cardDetails.setExpiryDate(new java.sql.Date(((Date)columnMap.get("UBCARDSTATUSDTTM")).getTime()));
			cardDetails.setImdCode(String.valueOf(columnMap.get("IMDCODE")).trim());
			cardDetails.setPartyId(String.valueOf(columnMap.get("UBCUSTOMERCODE")).trim());
			cardDetails.setReason(String.valueOf(columnMap.get("UBREASONFORSTATUS")).trim());
			cardDetails.setCardAction(String.valueOf(columnMap.get("CARDACTION")).trim());
			cardDetails.setCardID(String.valueOf(columnMap.get("ATMCARDID"))
					.trim());
			arrCardBasicDetails[index] = cardDetails;
		}
		requestInput.setCustCrdBasicDtls(arrCardBasicDetails);
		request.setCustCrdBasicInput(requestInput);
		return request;
	}
      
    


	/*
	 * Load  the  
	 */

      
      
}

