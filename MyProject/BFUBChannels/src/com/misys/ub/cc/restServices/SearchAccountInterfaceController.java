package com.misys.ub.cc.restServices;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.ub.cc.types.SearchAccountInterfaceRq;
import com.misys.ub.cc.types.SearchAccountInterfacesRs;
import com.misys.ub.cc.types.SearchAccountListRq;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

@RestController
@RequestMapping("/SearchAccountInterfaces")	
public class SearchAccountInterfaceController {

	private static final String HEADER = "Accept=application/json";

	@RequestMapping(value = "/RQ", method = RequestMethod.POST, headers = HEADER)
	public SearchAccountInterfacesRs post(@RequestBody SearchAccountInterfaceRq request) {
	
		BankFusionThreadLocal.setFbpService(false);
		
		IPersistenceObjectsFactory			factory		= BankFusionThreadLocal.getPersistanceFactory();
		SearchAccountInterfacesRs			response	= null;
		SearchAccountInterfaceService searchAccountInterfaceService = new SearchAccountInterfaceService();
		
		try {
			factory.beginTransaction();
			
			response = searchAccountInterfaceService.update(request);
			
			factory.commitTransaction();
		} catch (Exception ex) {
			ex.printStackTrace();
			factory.rollbackTransaction();
		}
		
		return response;
	}
	
	@RequestMapping(value = "/getDetailsList", method = RequestMethod.POST, headers = "Accept=application/json")
	public SearchAccountInterfacesRs post(@RequestBody SearchAccountListRq requestList){
		
	
		BankFusionThreadLocal.setFbpService(false);
		
		IPersistenceObjectsFactory			factory		= BankFusionThreadLocal.getPersistanceFactory();
		SearchAccountInterfacesRs			response	= null;
		
		SearchAccountInterfaceService searchAccountServiceWithList = new SearchAccountInterfaceService();
		
		try {
			factory.beginTransaction();
			
			response = searchAccountServiceWithList.updateList(requestList);
			
			factory.commitTransaction();
		} catch (Exception ex) {
			ex.printStackTrace();
			factory.rollbackTransaction();
		}
		
		return response;
	}
}
