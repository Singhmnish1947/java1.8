package com.misys.ub.dc.sepaForecast;

import java.text.ParseException;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.misys.fbp.common.util.FBPServiceAppContext;
import com.misys.ub.dc.restServices.GetAccountDetailsService;
import com.misys.ub.dc.sepaForecast.*;

@RestController
@RequestMapping("/SepaForecast")
public class SepaForecastController {

	private static final String HEADER = "Accept=application/json";
	private static final String BEAN = "SepaForecastService";

	private SepaForecastService sepaForecastService;

	@RequestMapping(value = "/SepaCharge", method = RequestMethod.POST, headers = HEADER)

	public SepaForecastResponse getPaymentForecast(@RequestBody SepaForecastRequest sepaForecasteRequest) {

		sepaForecastService = (SepaForecastService) FBPServiceAppContext.getInstance().getApplicationContext()
				.getBean(BEAN);

		return sepaForecastService.getPaymentForecast(sepaForecasteRequest);
	}

}
