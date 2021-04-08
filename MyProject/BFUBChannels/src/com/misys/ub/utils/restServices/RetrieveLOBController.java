package com.misys.ub.utils.restServices;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.ub.dc.types.InterbankSORq;
import com.misys.ub.dc.types.InterbankSORs;
import com.misys.ub.utils.types.LineOfBusinessListRq;
import com.misys.ub.utils.types.LineOfBusinessListRs;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

@RestController
@RequestMapping("/LOBIdentifier")
public class RetrieveLOBController {

	private static final String HEADER = "Accept=application/json";

	@RequestMapping(value = "/fetchLineOfBusinesses", method = RequestMethod.POST, headers = HEADER)
    public LineOfBusinessListRs fetchLineOfBusinesses(@RequestBody LineOfBusinessListRq inputRq){
		RetrieveLOBService lobListRetriever = new RetrieveLOBService();
		LineOfBusinessListRs res=lobListRetriever.fetchLOBList(inputRq);
		
	    return res;
	}
}
