package com.misys.ub.dc.restServices;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.ub.dc.types.DeDupRq;
import com.misys.ub.dc.types.DeDupRs;

@RestController
@RequestMapping("/Party")
public class PartyDeDupCheckController {

	private static final String HEADER = "Accept=application/json";

	PartyDeDupCheckService soService = new PartyDeDupCheckService();

	@RequestMapping(value = "/Dedup", method = RequestMethod.POST, headers = HEADER)
	public DeDupRs post(@RequestBody DeDupRq request) {
	
		return soService.update(request);
	
	}
}
