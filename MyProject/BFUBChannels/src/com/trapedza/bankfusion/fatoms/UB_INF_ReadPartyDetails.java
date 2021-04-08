package com.trapedza.bankfusion.fatoms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bf.com.misys.party.ws.RqPayload;
import bf.com.misys.party.ws.RsPayload;

import com.trapedza.bankfusion.utils.GUIDGen;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_ReadPartyDetails;

public class UB_INF_ReadPartyDetails extends AbstractUB_INF_ReadPartyDetails {

	
	public UB_INF_ReadPartyDetails() {
		super();
	}
	
	public UB_INF_ReadPartyDetails(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		String customerCode = this.getF_IN_CustomerCode();
		String uniqueID = GUIDGen.getNewGUID();
		Map<String,Object> inputParams = new ConcurrentHashMap<String,Object>();
		Map<String,Object> outputParams = new ConcurrentHashMap<String,Object>();
		
		RqPayload rqPayload = new RqPayload();
		String rqParam = "UNIQUE_ID=" + uniqueID + ";PARTYID=" + customerCode + ";PT_PFN_Party";
		rqPayload.setControlParam("CODE_DESC_REQD=Y;");
		rqPayload.setRqParam(rqParam);
		
		inputParams.put("requestPayload", rqPayload);
		
		outputParams = MFExecuter.executeMF("CB_PTY_ReadPartyDetailsWS_SRV", env, inputParams);
	
		RsPayload response = (RsPayload) outputParams.get("responsePayload");
		String[] responseParams = response.getRsParam().split(";");
		String[] result = null;
		
		for(String param : responseParams) {
			if(param.startsWith("PT_PFN_Party#PARTYTYPE_DESC")) {
				result = param.split("=");
				this.setF_OUT_PartyType(result[1]);
			}
			if(param.startsWith("PT_PFN_Party#PARTYSUBTYPE_DESC")) {
				result = param.split("=");
				this.setF_OUT_PartySubType(result[1]);
			}
			if(param.startsWith("PT_PFN_Party#PARTYCATEGORY_DESC")) {
				result = param.split("=");
				this.setF_OUT_PartyCategory(result[1]);
			}
		}
	}
	
}
