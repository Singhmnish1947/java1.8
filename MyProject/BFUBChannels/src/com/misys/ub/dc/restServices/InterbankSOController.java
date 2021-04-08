package com.misys.ub.dc.restServices;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.ub.dc.types.InterbankSORq;
import com.misys.ub.dc.types.InterbankSORs;

@RestController
@RequestMapping("/Interbank")
public class InterbankSOController {

	private static final String HEADER = "Accept=application/json";

	InterbankSOService soService = new InterbankSOService();

	@RequestMapping(value = "/SO", method = RequestMethod.POST, headers = HEADER)
	public InterbankSORs post(@RequestBody InterbankSORq request) {
	
		return soService.update(request);
	
	}

}
