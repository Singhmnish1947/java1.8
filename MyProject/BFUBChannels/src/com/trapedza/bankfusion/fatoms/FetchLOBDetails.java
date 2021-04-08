package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;

import bf.com.misys.cbs.services.ListGenericCodeRq;
import bf.com.misys.cbs.services.ListGenericCodeRs;
import bf.com.misys.cbs.types.GcCodeDetail;
import bf.com.misys.cbs.types.InputListHostGCRq;
import com.misys.ub.utils.restServices.RetrieveLOBService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_FetchLOBDetails;

public class FetchLOBDetails extends AbstractUB_INF_FetchLOBDetails {

	private static final String LINEOFBUSINESS = "LINEOFBUSINESS";
	private static final String INTERNAL_CUST_MF_NAME = "UB_CNF_IsInternalCustomer_SRV";

	public FetchLOBDetails(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		//String accountId = null;
		String customerId = null;
		bf.com.misys.ub.types.ubintfc.LineOfBusinessListRq input = new bf.com.misys.ub.types.ubintfc.LineOfBusinessListRq();
		input = getF_IN_LineOfBusinessListRq();
		//accountId = input.getAccountId();
		customerId = input.getCustomerId();
		/*LineOfBusinessListRq inputRq = new LineOfBusinessListRq();
		inputRq.setAccountId(accountId);
		inputRq.setCustomerId(customerId);*/
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("CustomerCode", customerId);
		HashMap<String, Object> outputParams = MFExecuter.executeMF(
				INTERNAL_CUST_MF_NAME, env, params);
		boolean isInternalCustomer = (boolean) outputParams.get("IsInternalCustomer");
		ListGenericCodeRs listGenericCodeRs = getF_OUT_ListLOB();
		if(isInternalCustomer) {
			for(GcCodeDetail gcCodeDetails : getGCList(LINEOFBUSINESS).getGcCodeDetails()) {
				if(gcCodeDetails.getCodeReference().equals("TREASURYFO")) {
					gcCodeDetails.setCodeReference(gcCodeDetails.getCodeReference());
					gcCodeDetails
							.setCodeDescription(gcCodeDetails.getCodeDescription());
					listGenericCodeRs.addGcCodeDetails(gcCodeDetails);
				}
			}
			//setF_OUT_ListLOB(getGCList(LINEOFBUSINESS));
		} else {
		RetrieveLOBService fetchLOBList = new RetrieveLOBService();
		/*com.misys.ub.utils.types.LineOfBusinessListRs res = fetchLOBList
				.fetchLOBList(inputRq);*/
		ArrayList<String> listOfLOBs = fetchLOBList
				.fetchCusLvlLobList(customerId);
		
		if (listOfLOBs != null) {
			for (String lob : listOfLOBs) {
				GcCodeDetail gcCodeDetails = new GcCodeDetail();
				if(lob.equals("TREASURYFO")) {
					gcCodeDetails.setCodeReference(lob);
					gcCodeDetails
							.setCodeDescription(getGCChildDesc(LINEOFBUSINESS, lob));
					listGenericCodeRs.addGcCodeDetails(gcCodeDetails);
				}
			}
		}
		/*LineOfBusinessListRs res1 = new LineOfBusinessListRs();
		res1.setId(res.getId());
		res1.setLisOfBusinesses(res.getLisOfBusinesses().toArray(
				new String[res.getLisOfBusinesses().size()]));
		setF_OUT_lineOfBusiness(fetchLOB(res));*/
		}

	}

	/*private VectorTable fetchLOB(
			com.misys.ub.utils.types.LineOfBusinessListRs res) {
		VectorTable listLOB = getF_OUT_lineOfBusiness();
		Map lob = null;
		ArrayList<String> arr = res.getLisOfBusinesses();
		for (String res1 : arr) {
			lob = new Hashtable();
			lob.put(LINEOFBUSINESS, res1);
			listLOB.addAll((new VectorTable(lob)));
		}
		return listLOB;
	}*/

	private static ListGenericCodeRs getGCList(String cbReference) {
        final String CB_GCD_LISTGENERICCODES_SRV = "CB_GCD_ListGenericCodes_SRV";
        HashMap<String, Object> paramsargupdate = new HashMap<String, Object>();
        ListGenericCodeRq listGenericCodeRq = new ListGenericCodeRq();
        InputListHostGCRq inputListHostGCRq = new InputListHostGCRq();
        inputListHostGCRq.setCbReference(cbReference);
        listGenericCodeRq.setInputListCodeValueRq(inputListHostGCRq);
        paramsargupdate.put("listGenericCodeRq", listGenericCodeRq);
        HashMap readCollateralDetailsRqMap = MFExecuter.executeMF(CB_GCD_LISTGENERICCODES_SRV,
                BankFusionThreadLocal.getBankFusionEnvironment(), paramsargupdate);
        ListGenericCodeRs listGenericCodeRs = (ListGenericCodeRs) readCollateralDetailsRqMap.get("listGenericCodeRs");
        return listGenericCodeRs;
    }
	private String getGCChildDesc(String parentCode, String childCode) {
		String childDesc = "";

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("CodeDefReference", parentCode);// parent
		params.put("reference", childCode);// child

		HashMap<String, Object> outputParams = MFExecuter.executeMF(
				"CB_GCD_GetCode_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), params);
		childDesc = (String) outputParams.get("strDesc");
		return childDesc;
	}
}
