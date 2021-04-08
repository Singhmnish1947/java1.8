package com.misys.bankfusion.paymentmessaging.fic.microservices;


public interface InterBankPmntMicroService {
	
	
	
	/**
	 * @param ControlParams
	 * @param rqParam
	 * @return
	 */
	public String create(String controlParams, String rqParams);
	
	/**
	 * @param controlParams
	 * @param rqParams
	 * @return
	 */
	public String amendInterBankPayment(String controlParams, String rqParams);
	
	/**
	 * @param controlParams
	 * @param rqParams
	 * @return
	 */
	public String listInterBankPayment(String controlParams, String rqParams);

}
