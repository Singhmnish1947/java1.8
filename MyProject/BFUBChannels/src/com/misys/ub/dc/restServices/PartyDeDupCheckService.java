package com.misys.ub.dc.restServices;

import java.util.HashMap;
import java.util.logging.Logger;

import bf.com.misys.cbs.msgs.party.v1r0.ValidatePtyDedupRq;
import bf.com.misys.cbs.msgs.party.v1r0.ValidatePtyDedupRs;
import bf.com.misys.cbs.types.ValidatePtyDedupInp;
import bf.com.misys.cbs.types.ValidatePtyDedupOp;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;

import com.misys.ub.dc.types.DeDupRq;
import com.misys.ub.dc.types.DeDupRs;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

public class PartyDeDupCheckService {
	private final static Logger logger = Logger.getLogger(PartyDeDupCheckService.class.getName());
	
	public DeDupRs update(DeDupRq deDupRq) {
		
		logger.info("DeDupRq is " + deDupRq);
		logger.info("DeDupRq email id is " + deDupRq.getEmail());
		logger.info("DeDupRq phone num is " + deDupRq.getPhoneNumber());
		String paContactValue1 = deDupRq.getEmail();
		String paContactValue2 = deDupRq.getPhoneNumber();

	    ValidatePtyDedupRq validatePtyDedupRq = new ValidatePtyDedupRq();
		boolean partyDedupCheckFlag=false;
		boolean	partyDedupCheckFlag1 = false;
		HashMap<String, Object> params = new HashMap<String, Object>();
		RqHeader rqHeader = new RqHeader();
		ValidatePtyDedupInp validatePtyDedupInp = new ValidatePtyDedupInp();
		Orig orig = new Orig();
		orig.setChannelId("IBI");
		rqHeader.setOrig(orig);
		logger.info("paContactValue1 is " + paContactValue1);
		
		logger.info("rqHeader is " + rqHeader.toString());
		
		validatePtyDedupInp.setPaContactValue(paContactValue1);
		validatePtyDedupInp.setPartyType("1062");
 		validatePtyDedupRq.setRqHeader(rqHeader);
		validatePtyDedupRq.setValidatePtyDedupInp(validatePtyDedupInp);
		
		
		logger.info("validatePtyDedupInp is " + validatePtyDedupInp.toString());
		params.put("validatePtyDedupRq", validatePtyDedupRq);
		partyDedupCheckFlag = checkPartyDeDup(paContactValue1,params);
		
	  validatePtyDedupInp.setPaContactValue(paContactValue2);
		validatePtyDedupRq.setValidatePtyDedupInp(validatePtyDedupInp);
		params.put("validatePtyDedupRq", validatePtyDedupRq);
		
		partyDedupCheckFlag1 = checkPartyDeDup(paContactValue2,params);
		boolean resultFlag=false;
		if(partyDedupCheckFlag == true || partyDedupCheckFlag1 == true){
			resultFlag=true;
		} else {
			resultFlag=false;
		}
		DeDupRs partyResponse = new DeDupRs();
		partyResponse.setPartyFound(resultFlag);
		return partyResponse;	
	}
	
	public boolean checkPartyDeDup(String contactValue,HashMap<String, Object> params){
		boolean partyCheckFlag=false;
		
		FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("CB_PTY_ValidatePartyDeDup_SRV");
		HashMap outputMap = invoker.invokeMicroflow(params, false);
		ValidatePtyDedupRs rs = (ValidatePtyDedupRs) outputMap.get("validatePtyDedupRs");
		ValidatePtyDedupOp res = rs.getValidatePtyDedupOp();
		if(res != null){
			if(res.getValidatePtyDedupOutputCount()==0) //false
					{
				partyCheckFlag=false;
				logger.info("partyDedupCheckFlag is " + partyCheckFlag);
				logger.info("getValidatePtyDedupOutputCount is " + res.getValidatePtyDedupOutputCount());
					}
			else if (res.getValidatePtyDedupOutputCount()>0)//Party Found
					{
				partyCheckFlag=true;
				logger.info("partyDedupCheckFlag is " + partyCheckFlag);
				logger.info("getValidatePtyDedupOutputCount is " + res.getValidatePtyDedupOutputCount());
					}
		}
		
		return partyCheckFlag;
	}
}
