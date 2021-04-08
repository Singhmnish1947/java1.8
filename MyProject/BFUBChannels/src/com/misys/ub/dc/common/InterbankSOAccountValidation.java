package com.misys.ub.dc.common;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
//import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.events.Event;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
//import com.misys.fbp.common.event.CommonEventSupport;
import com.misys.ub.dc.types.InterbankSORq;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;


public class InterbankSOAccountValidation {
	
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	private final static Log LOGGER = LogFactory.getLog(InterbankSOAccountValidation.class.getName());
	
	
	public InterbankSOAccountValidation() {

	}
	
	public boolean validate(InterbankSORq interbankSORq){

		String accID = interbankSORq.getCustAcctNo();
		String customerId = "";
		customerId = interbankSORq.getCustomerId();
		ArrayList param = new ArrayList();
		param.add(accID);
		IBOAccount mainAccount = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME,IBOAccount.ACCOUNTID);
		if (mainAccount == null) {
	
		String error = BankFusionMessages.getInstance()
					.getFormattedEventMessage(
							Integer.parseInt("40410029"),
							null,
							BankFusionThreadLocal.getUserSession()
									.getUserLocale(), true);
		LOGGER.info(error); 
		raiseEvent("40410029", null);
			return false;
		}
		validateAccountAndCustomer(mainAccount, customerId);
	return true;	
	}
	
	private boolean validateAccountAndCustomer(IBOAccount mainAccount,String customerId) {

		Boolean isClosed = mainAccount.isF_CLOSED();
		Boolean isStopped = mainAccount.isF_STOPPED();
		Boolean isDormant = mainAccount.isF_DORMANTSTATUS();
		String errorCode;
		if (isClosed == true || isStopped == true || isDormant == true) {
			if (isClosed) {
				errorCode = "40407566";
			} else if (isStopped) {
				errorCode = "40400055";
			} else {
				errorCode = "40400057";
			}
			raiseEvent(errorCode,null);
			return false;
		}
		
		ArrayList param = new ArrayList();
		param.clear();
		param.add(customerId);
		IBOCustomer customer = (IBOCustomer) factory.findByPrimaryKey(IBOCustomer.BONAME, customerId);
		if (customer != null) {
			String customerStatus = customer.getF_CUSTOMERSTATUS();
			if (!customerStatus.equals("001")) {
				errorCode = "40411056";
				raiseEvent(errorCode,null);
				return false;
			}
			return true;
		} else {
			errorCode = "40401080";
			raiseEvent(errorCode,null);
			return false;
		}
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
