
package com.misys.ub.dc.common;

import java.math.BigDecimal;
import java.util.HashMap;

/*import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;*/

import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.ub.dc.restServices.InterbankSOConstants;
import com.misys.ub.dc.types.Amount;
import com.misys.ub.dc.types.InterbankSORq;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import bf.com.misys.cbs.services.CalculatePaymentChargeRq;
import bf.com.misys.cbs.services.CalculatePaymentChargeRs;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.events.Event;

public class InterbankSOChargeValidation {
		
	
	final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
//	private transient final static Log logger = LogFactory.getLog(InterbankSOChargeValidation.class.getName());

	public InterbankSOChargeValidation(){
	}
	
	
	public boolean validateAccountForChargeAmount(InterbankSORq interSoRq){
	
		CalculatePaymentChargeRq calculatePaymentChargeRq = new CalculatePaymentChargeRq();
		calculatePaymentChargeRq = getCalculatePaymentChargeRq(interSoRq);
		BigDecimal finalCharge = BigDecimal.ZERO;
		BigDecimal finalTax = BigDecimal.ZERO;
		CalculatePaymentChargeRs calculatePaymentChargeRs = new CalculatePaymentChargeRs();
		Amount charges = new Amount();
		Amount tax = new Amount();
		calculatePaymentChargeRs  = getChargeRs(calculatePaymentChargeRq); 
		charges.setCurrency(calculatePaymentChargeRs.getChargeAmount().getIsoCurrencyCode());
		charges.setValue(calculatePaymentChargeRs.getChargeAmount().getAmount());
		tax.setCurrency(calculatePaymentChargeRs.getChargeTaxAmount().getIsoCurrencyCode());
		tax.setValue(calculatePaymentChargeRs.getChargeTaxAmount().getAmount());
		finalCharge = charges.getValue();
		finalTax = charges.getValue();
		return validateAccountForChargeAmount(interSoRq.getCustAcctNo(), finalCharge, finalTax);
	}
	
	public CalculatePaymentChargeRs getChargeRs(CalculatePaymentChargeRq calcChargeReq) {
		
		CalculatePaymentChargeRs chargeRs = new CalculatePaymentChargeRs();
		HashMap<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("calculatePaymentChargeRq",calcChargeReq);
		FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("MP_R_CB_MPM_PaymentChargeForecast_SRV");
		HashMap outputChargeParams = invoker.invokeMicroflow(inputParams, false);
		chargeRs = (CalculatePaymentChargeRs) outputChargeParams.get("calculatePaymentChargeRs");
		return chargeRs;
		
	}
	
	private boolean validateAccountForChargeAmount(String mainAccount, BigDecimal finalCharge, BigDecimal finalTax) {

		HashMap<String, Object> inputparam = new HashMap<String, Object>();
		HashMap<String, Object> outputparam = new HashMap<String, Object>();

		inputparam.put("AccountID", mainAccount);
		
		FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("GetAvailableBalance");
		outputparam = invoker.invokeMicroflow(inputparam, false);
		
		BigDecimal availbleBalance = (BigDecimal) outputparam.get("AvailableBalance");
		finalCharge = finalCharge.add(finalTax);
		if (availbleBalance.compareTo(finalCharge) != -1) // available Balance <= eventCharge
		{
			return true;
		} else {

			raiseEvent("40009269", null);
			return false;
		}

	}

	public CalculatePaymentChargeRq getCalculatePaymentChargeRq(InterbankSORq interSoRq) {

		CalculatePaymentChargeRq chargeRq = new CalculatePaymentChargeRq();
		Currency currency = new Currency();
		//TO DO null Check
		currency.setAmount(new BigDecimal(interSoRq.getAmount()));
		currency.setIsoCurrencyCode(interSoRq.getCurrency());
		chargeRq.setAccount(interSoRq.getCustAcctNo());
		chargeRq.setChannel(interSoRq.getChannelId());
		chargeRq.setPaymentSystem(interSoRq.getPaySys());
		chargeRq.setPaymentType(interSoRq.getPayType());
		
		chargeRq.setAccountType(InterbankSOConstants.OPERATIVE_ACCOUNT);
		chargeRq.setEventSubCategory("20600259");
		chargeRq.setAmount(currency);
		
		
		return chargeRq;

	}

	   public static void raiseEvent(String errorCode, String[] args) {
	    	Integer eventNumber = Integer.parseInt(errorCode);
	        if (args == null) {
	            args = new String[] { CommonConstants.EMPTY_STRING };
	        }
	        Event event = new Event();
	        event.setEventNumber(eventNumber);
	        event.setMessageArguments(args);
	        IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance().getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
	        businessEventsService.handleEvent(event);
	    }
	   
}
