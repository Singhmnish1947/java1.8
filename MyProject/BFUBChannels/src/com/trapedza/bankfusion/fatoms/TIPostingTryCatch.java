/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import bf.com.misys.cbs.types.events.Event;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.service.ServiceManager;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.subsystem.persistence.IPersistenceService;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_ERRORMSGMAP;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIUBPOSTINGMSG;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_PostingTryCatch;
import com.trapedza.bankfusion.steps.refimpl.IUB_TIP_PostingTryCatch;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * @author ayumital
 *
 */
public class TIPostingTryCatch extends AbstractUB_TIP_PostingTryCatch implements
		IUB_TIP_PostingTryCatch {
	
	private static final transient Log logger = LogFactory.getLog(PostingEngineWithTryCatch.class.getName());
	private static final String getMessageIdWhereClause = "WHERE " + IBOUB_INF_MessageHeader.MESSAGEID2 + " = ?";
	//private BankFusionEnvironment env;
	private static final Integer EVENT_NUMBER_CONFIGURATION_NOT_FOUND = 20020473;
	private static final String getUBPostingWhereClause = "WHERE " + IBOUB_TIP_TIUBPOSTINGMSG.TITRANSACTIONID + " = ?";
	private static final String getMessageIdErrorWhereClause = "WHERE " + IBOUB_INF_MessageHeader.MESSAGEID2 + " = ? AND " + IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ?" ;
	private static final String getMessageIdUniqueWhereClause = "WHERE " + IBOUB_INF_MessageHeader.MESSAGEID1 + " = ?";
	private IPersistenceObjectsFactory factory;
	public TIPostingTryCatch() {
		super();
	}
	private static int errorCode;// = 0;
    private static boolean locked;// = false;

	public TIPostingTryCatch(BankFusionEnvironment env) {
		super(env);
		//factory = BankFusionThreadLocal.getPersistanceFactory();
		factory = getFactory(false);
	}
	/*
	 * getting a new session factory
	 */
	private IPersistenceObjectsFactory getFactory(boolean readOnly) {

        IServiceManager smgr = ServiceManagerFactory.getInstance().getServiceManager();

        IPersistenceService pService = (IPersistenceService) smgr.getServiceForName(ServiceManager.PERSISTENCE_SERVICE);

        return pService.getPrivatePersistenceFactory(readOnly);

  }


	/**
	 *  This fatom is written to catch all possible
	 *  events that are created during the posting of
	 *  TI messages.
	 */
	public void process(BankFusionEnvironment env) throws BankFusionException {
		//this.env = env;
	
		ArrayList params = new ArrayList();
		String microFlowId = getF_IN_microflowId();
		HashMap inputParams = new HashMap();
		String status = "P";
		try
		{
			getLock();
			inputParams.put("ACCOUNTTYPE", getF_IN_ACCOUNTTYPE());
			inputParams.put("AMOUNT", getF_IN_AMOUNT());
			inputParams.put("AMOUNTSIGN", getF_IN_AMOUNTSIGN());
			inputParams.put("BASECURRENCYCODE", getF_IN_BASECURRENCYCODE());
			inputParams.put("BASEEQUIVALENT", getF_IN_BASEEQUIVALENT());
			inputParams.put("BranchSortCode", getF_IN_BranchSortCode());
			inputParams.put("CustomerCode", getF_IN_CustomerCode());
			inputParams.put("Header_messageType", getF_IN_Header_messageType());
			inputParams.put("MESSAGEID", getF_IN_MESSAGEID());
			inputParams.put("MESSAGETYPE", getF_IN_MESSAGETYPE());
			inputParams.put("NARRATIVE", getF_IN_NARRATIVE());
			inputParams.put("origCtxID", getF_IN_origCtxID());
			inputParams.put("PRIMARYID", getF_IN_PRIMARYID());
			inputParams.put("SERIALNO", getF_IN_SERIALNO());
			inputParams.put("SIGN", getF_IN_SIGN());
			inputParams.put("TOTALTXN", getF_IN_TOTALTXN());
			inputParams.put("TRANSACTIONCODE", getF_IN_TRANSACTIONCODE());
			inputParams.put("TRANSACTIONDATE", getF_IN_TRANSACTIONDATE());
			inputParams.put("TRANSACTIONID", getF_IN_TRANSACTIONID());
			inputParams.put("TRANSACTIONREF", getF_IN_TRANSACTIONREF());
			inputParams.put("TXNCURRENCYCODE", getF_IN_TXNCURRENCYCODE());
			inputParams.put("VALUEDATE", getF_IN_VALUEDATE());
			params.add(getF_IN_TRANSACTIONID());
			params.add("F");
			//factory.beginTransaction();
			List<IBOUB_INF_MessageHeader> errorInPreviousLeg = factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, getMessageIdErrorWhereClause, params, null, false);
			int errorSize = errorInPreviousLeg.size();
			if(errorSize==0){
			HashMap outputParams = MFExecuter.executeMF(microFlowId, env, inputParams);
			Integer errorCodeMF = (Integer)outputParams.get("errorCode");
            String desc = (String)outputParams.get("errorDesc");
            if(errorCodeMF.intValue() > 0)
                status = "F";
			params.clear();
			params.add(getF_IN_MESSAGEID());
			
			List<IBOUB_INF_MessageHeader> lstMessageHeader = factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, getMessageIdUniqueWhereClause, params, null, false);
			int parentListSize = lstMessageHeader.size();
			if (parentListSize < 1) {
				//raise event that parent does not exist
				String messageDisplay  = getF_IN_MESSAGEID();
				String[] args = {messageDisplay};
				Event event = new Event();
				event.setEventNumber(EVENT_NUMBER_CONFIGURATION_NOT_FOUND);
				event.setMessageArguments(args);
				EventsHelper.handleEvent(event, env);
			}
			
			Iterator<IBOUB_INF_MessageHeader> iListHeader= lstMessageHeader.iterator();
			
			while(iListHeader.hasNext()){
			IBOUB_INF_MessageHeader messageRow = iListHeader.next();
			messageRow.setF_MESSAGESTATUS(status);
            if(status.equals("F"))
                messageRow.setF_ERRORCODE(errorCodeMF.intValue());
			}
			if(status.equals("F"))
            {	
				params.clear();
				params.add(getF_IN_TRANSACTIONID());
				//going to set the status of all the legs as F in message header
				//List<IBOUB_INF_MessageHeader> lstMessageHeaderAll = factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, getMessageIdWhereClause, params, null, false);
                IBOUB_INF_ERRORMSGMAP insertErrorMsg = (IBOUB_INF_ERRORMSGMAP)factory.getStatelessNewInstance("UB_INF_ERRORMSGMAP");
                insertErrorMsg.setBoID(getF_IN_MESSAGEID());
                insertErrorMsg.setF_EVENTCODENUMBER(errorCode);
                insertErrorMsg.setF_IFMNAKMESSAGE(desc);
                factory.create("UB_INF_ERRORMSGMAP", insertErrorMsg);
            }
			factory.beginTransaction();
			factory.commitTransaction();
			factory.beginTransaction();
			BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
			}}
		catch (BankFusionException bfException) {
			if(logger.isDebugEnabled()){
				logger.debug(bfException.getMessageNumber() + " : " + bfException.getLocalizedMessage());
				logger.debug(ExceptionUtil.getExceptionAsString(bfException));
			}
			
			setF_OUT_update_ERRORDESCRIPTION((bfException.getLocalizedMessage()!= null)? bfException.getLocalizedMessage():bfException.getLocalisedMessage());
			setF_OUT_update_ERRORSTATUS(Integer.toString(bfException.getMessageNumber()));
			int exceptionSize = bfException.getEvents().size();
			if(exceptionSize>=1){
				Iterator<IEvent> exception = bfException.getEvents().iterator();
				while(exception.hasNext()){
					IEvent singleException = exception.next();
					errorCode = singleException.getEventNumber();
				}
			}
				String errorDesc = ((bfException.getLocalizedMessage()!=null)?bfException.getLocalizedMessage():bfException.getLocalisedMessage());
			
			params.clear();
			
			if(errorDesc.length() < 100)
	        {
			IBOUB_INF_ERRORMSGMAP insertErrorMsg = (IBOUB_INF_ERRORMSGMAP)factory.getStatelessNewInstance(IBOUB_INF_ERRORMSGMAP.BONAME); 
			insertErrorMsg.setBoID(getF_IN_MESSAGEID());
			insertErrorMsg.setF_EVENTCODENUMBER(errorCode);
			insertErrorMsg.setF_IFMNAKMESSAGE(errorDesc);
			factory.create(IBOUB_INF_ERRORMSGMAP.BONAME, insertErrorMsg);
	        }
			//	only going to update the error code in the leg that got failed 
			// 	and not the entire batch of transaction
			params.clear();
			params.add(getF_IN_TRANSACTIONID());
			List<IBOUB_INF_MessageHeader> lstMessageHeader2 = factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, getMessageIdWhereClause, params, null, false);
			int parentListSize = lstMessageHeader2.size();
			if (parentListSize < 1) {
				//raise event that parent does not exist
				String messageDisplay  = getF_IN_MESSAGEID();
				String[] args = {messageDisplay};
				Event event = new Event();
				event.setEventNumber(EVENT_NUMBER_CONFIGURATION_NOT_FOUND);
				event.setMessageArguments(args);
				EventsHelper.handleEvent(event, env);
			}
			//but all the legs will have status as F
			Iterator<IBOUB_INF_MessageHeader> iListHeader= lstMessageHeader2.iterator();
			
			while(iListHeader.hasNext()){
			IBOUB_INF_MessageHeader messageRow = iListHeader.next();
			if(messageRow.getBoID().equalsIgnoreCase(getF_IN_MESSAGEID())){
				messageRow.setF_ERRORCODE(errorCode);
			}
			messageRow.setF_MESSAGESTATUS("F");
		}
				//delete enteries from ti ub posting message table if any
			params.clear();	
			params.add(getF_IN_TRANSACTIONID());
			List<IBOUB_TIP_TIUBPOSTINGMSG> lstPostingMsg = factory.findByQuery(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, getUBPostingWhereClause, params, null, false);
			parentListSize = lstPostingMsg.size();	
			if (parentListSize > 1) {
			Iterator<IBOUB_TIP_TIUBPOSTINGMSG> iPostingMessage= lstPostingMsg.iterator();
			
			while(iPostingMessage.hasNext()){
				IBOUB_TIP_TIUBPOSTINGMSG postingRow = iPostingMessage.next();
				factory.remove(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, postingRow.getBoID(), false);
			}
			}
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
		}
		catch (Exception e) {
			setF_OUT_update_ERRORDESCRIPTION(BankFusionMessages.getFormattedMessage(40000127, new Object[]{CommonConstants.EMPTY_STRING}));
			setF_OUT_update_ERRORSTATUS(Integer.toString(40000127));
			errorCode = 40000127;
			if(logger.isDebugEnabled()){
				logger.info(e.getStackTrace());
			}
			params.clear();
			
			IBOUB_INF_ERRORMSGMAP insertErrorMsg = (IBOUB_INF_ERRORMSGMAP)factory.getStatelessNewInstance(IBOUB_INF_ERRORMSGMAP.BONAME); 
			insertErrorMsg.setBoID(getF_IN_MESSAGEID());
			insertErrorMsg.setF_EVENTCODENUMBER(errorCode);
			insertErrorMsg.setF_IFMNAKMESSAGE(BankFusionMessages.getFormattedMessage(40000127, new Object[]{CommonConstants.EMPTY_STRING}));
			factory.create(IBOUB_INF_ERRORMSGMAP.BONAME, insertErrorMsg);
			//	only going to update the error code in the leg that got failed 
			// 	and not the entire batch of transaction
			params.clear();
			params.add(getF_IN_TRANSACTIONID());
			List<IBOUB_INF_MessageHeader> lstMessageHeader2 = factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, getMessageIdWhereClause, params, null, false);
			int parentListSize = lstMessageHeader2.size();
			if (parentListSize < 1) {
				//raise event that parent does not exist
				String messageDisplay  = getF_IN_MESSAGEID();
				String[] args = {messageDisplay};
				Event event = new Event();
				event.setEventNumber(EVENT_NUMBER_CONFIGURATION_NOT_FOUND);
				event.setMessageArguments(args);
				EventsHelper.handleEvent(event, env);
			}
			//but all the legs will have status as F
			Iterator<IBOUB_INF_MessageHeader> iListHeader= lstMessageHeader2.iterator();
			
			while(iListHeader.hasNext()){
				
			IBOUB_INF_MessageHeader messageRow = iListHeader.next();
			if(messageRow.getBoID().equalsIgnoreCase(getF_IN_MESSAGEID())){
				messageRow.setF_ERRORCODE(errorCode);
			}
			messageRow.setF_MESSAGESTATUS("F");
		}
		
			params.clear();	
			
			params.add(getF_IN_TRANSACTIONID());
			List<IBOUB_TIP_TIUBPOSTINGMSG> lstPostingMsg = factory.findByQuery(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, getUBPostingWhereClause, params, null, false);
			parentListSize = lstPostingMsg.size();	
			if (parentListSize > 1) {
			Iterator<IBOUB_TIP_TIUBPOSTINGMSG> iPostingMessage= lstPostingMsg.iterator();
			
			while(iPostingMessage.hasNext()){
				IBOUB_TIP_TIUBPOSTINGMSG postingRow = iPostingMessage.next();
			factory.remove(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, postingRow.getBoID(), false);
			}	
		}
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
		logger.error(ExceptionUtil.getExceptionAsString(e));
		}
		finally{
			factory.closePrivateSession();
			 releaseLock();
		} 
	}
	public synchronized void getLock() {

        locked = false;
        if (!locked) {
            locked = true;
            return;
        }

        try {
            wait();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        locked = true;

    }

    /**
     * @see com.trapedza.bankfusion.scheduler.gateway.interfaces.ISchedulerManager#releaseLock()
     */
    public synchronized void releaseLock() {
        locked = false;
        notify();
    }

}
