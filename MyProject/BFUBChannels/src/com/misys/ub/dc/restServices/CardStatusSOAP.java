package com.misys.ub.dc.restServices;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.dc.CardStatusRq;
import bf.com.misys.cbs.types.dc.CardStatusRqBody;
import bf.com.misys.cbs.types.dc.CardStatusRs;
import bf.com.misys.cbs.types.dc.CardStatusRsBody;
import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.subsystem.task.runtime.exception.CollectedEventsDialogException;
import com.misys.cbs.gcd.types.GcDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_GCD_Locale;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.fatoms.GenericCodes;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_CardStatus;

public class CardStatusSOAP extends AbstractUB_IBI_CardStatus {

	private static Log log = LogFactory
			.getLog(CardStatusSOAP.class.getName());
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean success;
	private IBOATMCardDetails cardDetail;
	private int errorCode;
	private static final String GET_CARDSTATUS_WHERE_CLAUSE = " WHERE "
			+ IBOATMCardDetails.ATMCARDNUMBER + " = ? ";

	
	public CardStatusSOAP(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) throws BankFusionException {
    	log.info("=+= Entering Card Status API");
    	CardStatusRqBody request = getF_IN_CardStatusRq().getCardStatusRqBody();
    	CardStatusRs responseWrapper = new CardStatusRs();
    	CardStatusRsBody response = new CardStatusRsBody();
    	String method = request.getMethod(); 
    	String cardNumber = request.getCardNumber();
    	String newStatus = request.getCardStatus();
    	
    	String status = "";
    	
    	try{
	    	log.info("=+= Card status API called for");
	    	log.info("CardNumber : " + cardNumber);
	    	log.info("Method : " + method);
	    	log.info("Status : " + request.getCardStatus());
	    	
	    	status = getCardStatus(cardNumber);
	    	
	    	if(!success)
	    		handleEvent(40400005, null);
	    		
			if("POST".equalsIgnoreCase(method)){
				
				if(status == null || "".equalsIgnoreCase(status.trim()))
					handleEvent(40422501, null);

				success = amendCardStatus(newStatus);
				if(!success)
					handleEvent(40422501, null);
				
				status = newStatus;
			}
	    		
	    	
    	}catch(CollectedEventsDialogException eventException){
    		log.warn("=+= Caught Exception -> "+ errorCode);
    		responseWrapper.setRsHeader( setErrorTag(eventException , getF_IN_CardStatusRq()) );
    	}
    	
    	response.setCardNumber(cardNumber);
    	response.setCardStatus(status);
    	response.setSuccess(success);
    	responseWrapper.setCardStatusRsBody(response);
    	log.info("Exiting Card Status API");    	
    	setF_OUT_CardStatusRs(responseWrapper);
    }

    
	private RsHeader setErrorTag(CollectedEventsDialogException eventException, CardStatusRq cardStatusRq) {
		RsHeader rsHeader = new RsHeader();
		MessageStatus messageStatus = new MessageStatus();
		SubCode subCode = new SubCode();
		subCode.setCode(String.valueOf(errorCode));
		subCode.setDescription(eventException.getLocalizedMessage());
		messageStatus.addCodes(subCode);
		messageStatus.setOverallStatus("F");
		rsHeader.setStatus(messageStatus);
		rsHeader.setOrigCtxtId(cardStatusRq.getRqHeader().getOrig().getChannelId());
		return rsHeader;
	}

	private String getCardStatus(String cardNumber) {
		String status = "";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(cardNumber);
		List<IBOATMCardDetails> lstCardDetail = BankFusionThreadLocal
				.getPersistanceFactory().findByQuery(IBOATMCardDetails.BONAME,
							GET_CARDSTATUS_WHERE_CLAUSE, params, null, false);
		
		if(lstCardDetail.isEmpty()){
			log.warn("=+= Card Number doesn't exist");
			success = false;
		}else{
			success = true;
			cardDetail = lstCardDetail.get(0);
			status = cardDetail.getF_UBCARDSTATUS();
			log.info("Card currentStatus : " + status);
		}
		return status;
	}

	private boolean amendCardStatus(String status) {
		boolean success = false;
		Map<String,String> validStatusMap = findCodeTypesAsMap("ATMCARDSTATUS");
		if(validStatusMap.containsKey(status.trim())){
			success = true;
			cardDetail.setF_UBCARDSTATUS(status);
			log.info("card Status updated to : "+status);
		}
		return success;
	}
	
	public Map<String, String> findCodeTypesAsMap(String codeType) {
		Map<String, String> codeTypesAsMap = new HashMap<String, String>();
		List<GcDetails> codes = GenericCodes.getInstance().getGenricCodes(
				codeType, getDefaultLocale());
		for (GcDetails code : codes) {
			codeTypesAsMap.put(code.getValue(), code.getDescription());
		}
		return Collections.unmodifiableMap(codeTypesAsMap);
	}

	
	public Integer getDefaultLocale() {
		IBOCB_GCD_Locale locale = (IBOCB_GCD_Locale) BankFusionThreadLocal
				.getPersistanceFactory().findByPrimaryKey(IBOCB_GCD_Locale.BONAME, BankFusionThreadLocal.getUserLocale().toString());
		if (locale == null) {
			return new Integer(1);
		} else {
			return locale.getF_LOCALEIDPK();
		}
	}

    private void handleEvent(Integer eventNumber, String[] args) {
		if (args == null) {
			args = new String[] { CommonConstants.EMPTY_STRING };
		}
		errorCode = eventNumber;
		Event event = new Event();
		event.setEventNumber(eventNumber);
		event.setMessageArguments(args);
		IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance()
				.getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
		businessEventsService.handleEvent(event);    		
    }

}
