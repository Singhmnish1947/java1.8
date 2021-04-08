package com.misys.ub.dc.common;

import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.misys.bankfusion.commonutils.GetCBSConfigLocation;
import com.misys.bankfusion.paymentmessaging.fic.microservices.InterBankPmntMicroService;
import com.misys.ub.dc.restServices.InterbankSOConstants;
import com.misys.ub.dc.types.InterbankSOCRq;

public class InternationalSEPASOCancelRouting 
{
	private static InterBankPmntMicroService interbankMicroservice ;
	private static ApplicationContext PAYMENTS_FIC_ApplicationContext;
	public static final String INTERBANKSO_MICROSERVICE = "interBankPmntMicroServiceImpl";
	private static final String PAYMENTS_FIC_CTX_FILE = "conf/business/FIC/PAYMENTS/PAYMENTSFICApplicationContext.xml";
	private final String PAYMENT_INSTRUCTION_CANCELAPI_CONTROLPARAMS = "Ver=1.0; service=soamend; operation =amend";
	
	
	private String interbankPaymentId;
	private final static Logger LOGGER = Logger.getLogger(InternationalSEPASOCancelRouting.class.getName());
	public String cancelRequest(InterbankSOCRq interbankSOCRq)
	{
		int i=0;
		 LOGGER.info("Calling Standing Order SEPA Cancel" + this.getClass());
		 StringBuilder uglyFormat = new StringBuilder("");
		 
		 uglyFormat.append(addValue("InterbankPaymentId",interbankSOCRq.getInterbankPaymentId()));
		 uglyFormat.append(addValue("CustomerId",""));
		 uglyFormat.append(addValue("CustAcctType",""));
		 uglyFormat.append(addValue("CustAcctNo",""));
		 uglyFormat.append(addValue("Amt",""));
		 uglyFormat.append(addValue("Ccy",""));
		 uglyFormat.append(addValue("Freq",""));
		 uglyFormat.append(addValue("EndOfMonth",""));
		 uglyFormat.append(addValue("NumPayments",""));
		 uglyFormat.append(addValue("FrDt",""));
		 uglyFormat.append(addValue("ToDt",""));
		 uglyFormat.append(addValue("Time",""));
		 uglyFormat.append(addValue("HolidayTreatment",""));
		 uglyFormat.append(addValue("Retry",""));
		 uglyFormat.append(addValue("RetryCount",""));
		 uglyFormat.append(addValue("SuspendOnFailure",""));
		 uglyFormat.append(addValue("NumberOfFailures",""));
		 uglyFormat.append(addValue("InstructionId",""));
		 uglyFormat.append(addValue("Setup",""));
		 uglyFormat.append(addValue("Success",""));
		 uglyFormat.append(addValue("Failure",""));
		 uglyFormat.append(addValue("ChannelID",""));
		 String sepasoRequestParams = uglyFormat.toString();
		 		 
		 interbankMicroservice = (InterBankPmntMicroService) getBean(INTERBANKSO_MICROSERVICE);
		// LOGGER.info("Calling SEPA Standing Order MicroService" + this.getClass());
		String response = interbankMicroservice == null ? null
				: interbankMicroservice.amendInterBankPayment(
						PAYMENT_INSTRUCTION_CANCELAPI_CONTROLPARAMS,
						sepasoRequestParams);
		 LOGGER.info("Response is" +response);	
	 	 return response;
	}
	
	
	private String addValue(String key, String value) {

		if(value == null){
			return "";
		}				
		return new StringBuilder(key).append(InterbankSOConstants.KEY_VALUE_SEPARATOR).append("\"").append(value).append("\"").append(InterbankSOConstants.ITEM_SEPARTOR).toString();
	}
	
	private static Object getBean(String beanId) {
		loadApplicationContext();
		if(PAYMENTS_FIC_ApplicationContext==null){
			return null;			
		}
		return PAYMENTS_FIC_ApplicationContext.getBean(beanId);
	}

	private static void loadApplicationContext() {
		if (PAYMENTS_FIC_ApplicationContext == null) {

			String cbsConfPath = GetCBSConfigLocation.getCBSConfigLocation();
			try {
				PAYMENTS_FIC_ApplicationContext = new FileSystemXmlApplicationContext("file:" + cbsConfPath.concat(PAYMENTS_FIC_CTX_FILE));
			} catch (BeansException e) {
				LOGGER.warning("ERROR: Could not find PAYMENTINSTRFICApplicationContext.xml at " + cbsConfPath);
			}
		}
	}

}
