package com.misys.ub.dc.restServices;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.ub.dc.types.CreatePartyAndAccountRq;
import com.misys.ub.dc.types.CreatePartyAndAccountRs;

@RestController
@RequestMapping("/Party")
public class PartyCreateController {

	private static final String HEADER = "Accept=application/json";

	PartyCreateService createService = new PartyCreateService();

	@RequestMapping(value = "/Create", method = RequestMethod.POST, headers = HEADER)
	public CreatePartyAndAccountRs post(@RequestBody CreatePartyAndAccountRq request) {
	
		return createService.update(request);
	 
	}
}
