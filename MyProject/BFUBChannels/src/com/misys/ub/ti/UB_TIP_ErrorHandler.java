package com.misys.ub.ti;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.batchgateway.persistence.PrivatePersistenceFactory;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

import bf.com.misys.cbs.msgs.v1r0.AccountPostingRq;
import bf.com.misys.cbs.msgs.v1r0.AccountPostingRs;
import bf.com.misys.cbs.types.AccountPostingResponse;
import bf.com.misys.cbs.types.PostingResponseData;

public class UB_TIP_ErrorHandler {
	private transient final static Log LOGGER = LogFactory.getLog(UB_TIP_ErrorHandler.class.getName());
	private final IServiceManager SERVICE_MANAGER = ServiceManagerFactory.getInstance().getServiceManager();
	private final IPersistenceService PERSISTENCE_SERVICE = (IPersistenceService) SERVICE_MANAGER
			.getServiceForName(ServiceManager.PERSISTENCE_SERVICE);

	public void valiateMessage(Exchange exchange) {
		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);

		LOGGER.info("============Entered into TIP Error Handler=========");

		String inMsg = (String) exchange.getIn().getBody();
		AccountPostingRq accountPostingRq = convertXMLToJavaObject(inMsg);
		AccountPostingRs resp = new AccountPostingRs();
		AccountPostingResponse accPosRes = new AccountPostingResponse();
		ArrayList<PostingResponseData> list = new ArrayList<>();
		for (int i = 0; i <= accountPostingRq.getAccountPosting().getPostingItemList().length-1; i++) {
			String lCorelationId = accountPostingRq.getAccountPosting().getPostingItemList(i).getCorrelationId();
			if (accountPostingRq.getAccountPosting().getPostingItemList(i).getPostingReference() != null) {
				String ref = accountPostingRq.getAccountPosting().getPostingItemList(i).getNarrative().getReference();
				updateMessageHeader(lCorelationId, "F", 40000127, ref);
		
			PostingResponseData lPostingResponseData = new PostingResponseData();
			lPostingResponseData.setCorrelationId(lCorelationId);
			lPostingResponseData.setErrorDetails("F");
			list.add(lPostingResponseData);
			}
			
		}
		accPosRes.setPostingResponseDataList(list.toArray(new PostingResponseData[list.size()]));
		resp.setAccountPostingResponse(accPosRes);
		MessageProducerUtil.sendMessage(convertObjectToXML(resp),"UB_TIP_POSTING_RES");
	}


public void updateMessageHeader(String corelationId, String status, int eventCode, String ref)
{
	LOGGER.info("Start of updateMessageHeader");
	
	IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
	PrivatePersistenceFactory factory = new PrivatePersistenceFactory();
	IBOUB_INF_MessageHeader headerBOItem = (IBOUB_INF_MessageHeader) localFactory().getStatelessNewInstance(IBOUB_INF_MessageHeader.BONAME);
   try
   {
   if(corelationId!=null) {
	
    IBOUB_INF_MessageHeader existingCorelationId = (IBOUB_INF_MessageHeader)privateFactory.findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME,corelationId,true);
     if(existingCorelationId!=null ) {
     headerBOItem.updateFromMap(existingCorelationId.getDataMap());
     privateFactory.beginTransaction();
	 headerBOItem.setF_ERRORCODE(eventCode);
	 headerBOItem.setF_MESSAGESTATUS(status);
	 factory.update(headerBOItem);
	  privateFactory.commitTransaction(); 
	 }
     else
     {
    	 privateFactory.beginTransaction();
    	 headerBOItem.setBoID(corelationId);
    	 headerBOItem.setF_DIRECTION("I");
    	 headerBOItem.setF_CHANNELID("TI");
    	 headerBOItem.setF_ERRORCODE(eventCode);
    	 headerBOItem.setF_MESSAGESTATUS(status);
    	 headerBOItem.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
    	 headerBOItem.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
    	 headerBOItem.setF_MESSAGETYPE(ref);
    	 headerBOItem.setF_MESSAGEID2(corelationId.substring(0, corelationId.length()-3));
    	 factory.create(headerBOItem);
    	 privateFactory.commitTransaction(); 
     }
   }  
}
   finally
   {
	   privateFactory.closePrivateSession();
   }
   
   }



	public AccountPostingRq convertXMLToJavaObject(String reqMsg) {
		ComplexTypeConvertor converter = new ComplexTypeConvertor();
		AccountPostingRq accountPostingRq = (AccountPostingRq) converter.getJavaFromXml(AccountPostingRq.class.getName(), reqMsg);
		return accountPostingRq;

	}
	public static String convertObjectToXML(AccountPostingRs resp) {
        ComplexTypeConvertor converter = new ComplexTypeConvertor();
        String accountPostingRs = (String)converter.getXmlFromJava(AccountPostingRs.class.getName(), resp);
		return accountPostingRs;
    }

	private IPersistenceObjectsFactory localFactory() {
		return BankFusionThreadLocal.getPersistanceFactory();
	}
}