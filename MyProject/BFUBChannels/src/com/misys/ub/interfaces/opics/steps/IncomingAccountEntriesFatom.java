/* **********************************************************
 * Copyright (c) 2009 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: IncomingAccountEntriesFatom.java,v 1.0 2009/07/17 abdrahim Exp $
 *
 */
package com.misys.ub.interfaces.opics.steps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.ub.types.UBTOOPICS_INC_EOD_ACC_ENTRIES;
import bf.com.misys.ub.types.UBTOOPICS_INC_STLMNT_ACC_ENTRIES;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.microflow.ActivityStep;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_IncAccEntriesFatom;

/**
 * 
 * @AUTHOR Abdul Rahim
 * @PROJECT Universal Banking
 * @description This will Lock currencies and unlock it after processing for same currency
 *              transaction.
 */
public class IncomingAccountEntriesFatom extends AbstractUB_OPX_IncAccEntriesFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    private static final String IN_microflowId = "microflowID";
    private static final String IN_currencyCode = "CURRENCYCODE";

    /** For logging/debug/error message. */
    private static final transient Log logger = LogFactory.getLog(IncomingAccountEntriesFatom.class.getName());

    private static Map<String, String> lockMap = java.util.Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Constructor
     * 
     * @param env
     */
    public IncomingAccountEntriesFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * Method Description:Lock currencies and unlock it after processing for same currency
     * transaction.
     * 
     * @param env
     * @param as
     * @throws BankFusionException
     */
    public void process(BankFusionEnvironment env, ActivityStep as) throws BankFusionException {
        Map<String, Object> inputTags = as.getInTags();
        String currencyCode = (String) inputTags.get(IN_currencyCode);
        String microflowId = (String) inputTags.get(IN_microflowId);
        String messageID = null;
        UBTOOPICS_INC_EOD_ACC_ENTRIES accEntries = (UBTOOPICS_INC_EOD_ACC_ENTRIES) inputTags.get("EodAccEntries");
        UBTOOPICS_INC_STLMNT_ACC_ENTRIES setlementEntries = (UBTOOPICS_INC_STLMNT_ACC_ENTRIES) inputTags.get("StlmntAccEntries");
       
        if(accEntries != null){
        	messageID = accEntries.getMessID();
        }else if(setlementEntries != null){
        	messageID = setlementEntries.getMessID();
        }
        
        if (currencyCode != null && currencyCode.trim().length() != 0) {
            try {
              //  lockCurrency(currencyCode); artf957419 : The transactions are not getting posted on few occasions and it is inconsistent.
                Map<String, Object> outputTags = MFExecuter.executeMF(microflowId, env, inputTags);
                as.getInTags().putAll(outputTags);
            }
            catch (Exception e) {
                logger.error(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + " Error while executing. "
                        + ExceptionUtil.getExceptionAsString(e));
                
       		 int eventNumber = 0;
       		 
       		 if(e instanceof BankFusionException){
       	         Collection<IEvent> events = ((BankFusionException)e).getEvents();
       	         Iterator<IEvent> itr = events.iterator();
       	         while (itr.hasNext()) {
       	             IEvent e1 = itr.next();
       	             eventNumber = e1.getEventNumber();
       	             break;	             
       	         }
       			 
       		 }	// Exception other than Bankfusion exception
       		 else{
       			 eventNumber = 40422013;
       		 }
       		Map<String, Object> inputT = new HashMap<String, Object>();
       		inputT.put("SUBCODETYPE", "F");
       		inputT.put("ERRORCODE", eventNumber);
       		inputT.put("MESSAGEID1", messageID);
       		MFExecuter.executeMF("UB_OPX_UpdateMessageHeader_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), inputT);
                
            }
                       finally {
              //  unlockCurrency(currencyCode); artf957419 : The transactions are not getting posted on few occasions and it is inconsistent.
               accEntries = null;
               setlementEntries = null;
            }
        }
        else {
            logger.error(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + "NO CURRENCY FOR LOCK. ");
        }
    }

    /**
     * LOCK CURRENCIES IN THE LOCK MAP IF THEY ARE OPEN, OR ELSE WAIT TILL A NOTIFY WAKES UP THIS
     * THREAD AND RETRY THE LOCK.
     * 
     * @param inputTags
     */
    private void lockCurrency(String currency) {
        synchronized (lockMap) {
            if (lockMap.containsKey(currency)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + "THE Currency " + currency
                            + " IS LOCKED -> SENDING TO RETRY QUEUE");
                }
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + "ENTERED LOCK FOR / " + currency);
                }
                lockMap.put(currency, null);
                return;
            }
        }

        synchronized (lockMap) {
            try {
                lockMap.wait();
            }
            catch (InterruptedException exception) {
                logger.error(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + " Error while executing. ");
            }
            lockCurrency(currency);
        }
    }

    /**
     * UNLOCK CURRENCIES FROM THE LOCK MAP AND NOTIFY WAITING THREADS
     * 
     * @param inputTags
     */
    private void unlockCurrency(String currency) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + "RELEASING LOCK FOR / " + currency);
        }
        synchronized (lockMap) {
            lockMap.remove(currency);
            lockMap.notifyAll();
        }
    }

}
