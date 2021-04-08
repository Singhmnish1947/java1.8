package com.misys.ub.dc.sepaForecast;

import java.text.ParseException;

import com.misys.ub.dc.sepaForecast.*;

public interface SepaForecastService {

	SepaForecastResponse getPaymentForecast(SepaForecastRequest sepaForecasteRequest);

}
