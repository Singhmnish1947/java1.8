package com.misys.ub.interfaces.ti.steps;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.common.moneymarket.TxnDetails;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_SyncSingleLegUBTxnFatom;


public class SyncSingleLegUBTxnFatom extends AbstractUB_TIP_SyncSingleLegUBTxnFatom {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    
    private static String INMESSAGEID1PK = "MESSAGEID1";
    private static String INTRANSACTIONID = "TRANSACTIONID";
    private static String INTITRANSACTIONID = "TITRANSACTIONID";
    private static String INSERIALNO = "SERIALNO";
    private static String INTRANSACTIONREF = "TRANSACTIONREF";
    private static String INACCOUNTID = "ACCOUNTID";
    private static String INTRANSACTIONCODE = "TRANSACTIONCODE";
    private static String INTRANSACTIONDTTM = "TRANSACTIONDTTM";
    private static String INVALUEDTTM = "VALUEDTTM";
    private static String INISFORCEPOST = "ISFORCEPOST";
    private static String INNARRATIVE = "NARRATIVE";
    private static String INAMOUNT = "AMOUNT";
    private static String INAMOUNTSIGN = "AMOUNTSIGN";
    private static String INTXNCURRENCYCODE = "TXNCURRENCYCODE";
    private static String INISACCSUSPPOSTED = "ISACCSUSPPOSTED";
    private static String INREASONFORSUSPFORCEPOST = "REASONFORSUSPFORCEPOST";
    private static String INBRANCHNO = "BRANCHNO";
    private static String INCUSTOMERCODE = "CUSTOMERCODE";
    private static String INTOTALTXNLEGS = "TOTALTXNLEGS";


    public SyncSingleLegUBTxnFatom(BankFusionEnvironment env) {
        // TODO Auto-generated constructor stub
        super(env);
    }
    
    private static final transient Log logger = LogFactory.getLog(SyncSingleLegUBTxnFatom.class.getName());
    private static Map<String, String> lockMap = java.util.Collections.synchronizedMap(new HashMap<String, String>());
    private static boolean locked = false;
    
    public void process(BankFusionEnvironment env) {
    	
    	 Map<String, Object> txnDetails = new HashMap();
         txnDetails.put(INMESSAGEID1PK, getF_IN_INMESSAGEID1PK());
         txnDetails.put(INTRANSACTIONID, getF_IN_INTRANSACTIONID());
         txnDetails.put(INTITRANSACTIONID, getF_IN_INTITRANSACTIONID());
         txnDetails.put(INSERIALNO, getF_IN_INSERIALNO());
         txnDetails.put(INTRANSACTIONREF, getF_IN_INTRANSACTIONREF());
         txnDetails.put(INACCOUNTID, getF_IN_INACCOUNTID());
         txnDetails.put(INTRANSACTIONCODE, getF_IN_INTRANSACTIONCODE());
         txnDetails.put(INTRANSACTIONDTTM, getF_IN_INTRANSACTIONDTTM());
         txnDetails.put(INVALUEDTTM, getF_IN_INVALUEDTTM());
         txnDetails.put(INISFORCEPOST, isF_IN_INISFORCEPOST());
         txnDetails.put(INNARRATIVE, getF_IN_INNARRATIVE());
         txnDetails.put(INAMOUNT, getF_IN_INAMOUNT());
         txnDetails.put(INAMOUNTSIGN, getF_IN_INAMOUNTSIGN());
         txnDetails.put(INTXNCURRENCYCODE, getF_IN_INTXNCURRENCYCODE());
         txnDetails.put(INISACCSUSPPOSTED, isF_IN_INISACCSUSPPOSTED());
         txnDetails.put(INREASONFORSUSPFORCEPOST, getF_IN_INREASONFORSUSPFORCEPOST());
         txnDetails.put(INBRANCHNO, getF_IN_INBRANCHNO());
         txnDetails.put(INCUSTOMERCODE, getF_IN_INCUSTOMERCODE());
         txnDetails.put(INTOTALTXNLEGS, getF_IN_INTOTALTXNLEGS());
         
    	
    	try {
    		lockCurrency(getF_IN_INTXNCURRENCYCODE());

        		
        		MFExecuter.executeMF("UB_TIP_SingleLeg_Posting_SRV", env, txnDetails);        	}

        catch (Exception e) {
            logger.error(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + " Error while executing. "
                    + ExceptionUtil.getExceptionAsString(e));
        }
        finally {
        	unlockCurrency(getF_IN_INTXNCURRENCYCODE());
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
