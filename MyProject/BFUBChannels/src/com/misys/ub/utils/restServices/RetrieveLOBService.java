package com.misys.ub.utils.restServices;

import java.util.ArrayList;

import org.springframework.transaction.annotation.Transactional;

import com.misys.fbp.common.util.FBPService;
import com.misys.ub.utils.types.LineOfBusinessListRq;
import com.misys.ub.utils.types.LineOfBusinessListRs;
@Transactional
@FBPService(serviceId = "RetrieveLOB", applicationId = "")
public class RetrieveLOBService {
	
	@Transactional
	public LineOfBusinessListRs fetchLOBList(LineOfBusinessListRq inputRq) {
		
		RetrieveLOBServiceHelper helper = new RetrieveLOBServiceHelper();
		return helper.fetchLOBList(inputRq);
	}
	
	@Transactional
	public ArrayList<String> fetchCusLvlLobList(String customerId) {
		RetrieveLOBServiceHelper helper = new RetrieveLOBServiceHelper();
		return helper.fetchCusLvlLobList(customerId);
	}
}
