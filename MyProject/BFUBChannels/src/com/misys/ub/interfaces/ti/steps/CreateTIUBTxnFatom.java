/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UB_TIP_SyncTxnFatom.java,v.1.0,Jul 8, 2009 10:14:03 AM ravir
 *
 */
package com.misys.ub.interfaces.ti.steps;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIUBPOSTINGMSG;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_CreateUBTxnFatom;

/**
 * @author ravir
 * @date Jul 8, 2009
 * @project Universal Banking
 * @Description:
 */

public class CreateTIUBTxnFatom extends AbstractUB_TIP_CreateUBTxnFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static String INMESSAGEID1PK = "INMESSAGEID1PK";
    private static String INTRANSACTIONID = "INTRANSACTIONID";
    private static String INTITRANSACTIONID = "INTITRANSACTIONID";
    private static String INSERIALNO = "INSERIALNO";
    private static String INTRANSACTIONREF = "INTRANSACTIONREF";
    private static String INACCOUNTID = "INACCOUNTID";
    private static String INTRANSACTIONCODE = "INTRANSACTIONCODE";
    private static String INTRANSACTIONDTTM = "INTRANSACTIONDTTM";
    private static String INVALUEDTTM = "INVALUEDTTM";
    private static String INISFORCEPOST = "INISFORCEPOST";
    private static String INNARRATIVE = "INNARRATIVE";
    private static String INAMOUNT = "INAMOUNT";
    private static String INAMOUNTSIGN = "INAMOUNTSIGN";
    private static String INBASEEQUIVAMOUNT = "INBASEEQUIVAMOUNT";
    private static String INBASEEQUIVCURRENCYCODE = "INBASEEQUIVCURRENCYCODE";
    private static String INTXNCURRENCYCODE = "INTXNCURRENCYCODE";
    private static String INEXCHANGERATETYPE = "INEXCHANGERATETYPE";
    private static String INISACCSUSPPOSTED = "INISACCSUSPPOSTED";
    private static String INREASONFORSUSPFORCEPOST = "INREASONFORSUSPFORCEPOST";
    private static String INACTUALAMOUNT = "INACTUALAMOUNT";
    private static String INBRANCHNO = "INBRANCHNO";
    private static String INCUSTOMERCODE = "INCUSTOMERCODE";
    private static String INTOTALTXNLEGS = "INTOTALTXNLEGS";
    private static String CURRENCYSCALE = "CURRENCYSCALE";
    private static String SPOT_TYPE = "SPOT";

    public CreateTIUBTxnFatom(BankFusionEnvironment env) {
        // TODO Auto-generated constructor stub
        super(env);
    }

    private static final transient Log logger = LogFactory.getLog(CreateTIUBTxnFatom.class.getName());
    private static boolean locked = false;

    public void process(BankFusionEnvironment env) {
        if (isF_IN_CallMethodOnly()) {
            setF_OUT_CONVERTEDAMOUNT(getAmountForCurrency(getF_IN_INAMOUNT(), getF_IN_CurrencyScale()));
            return;
        }
        else if (!isF_IN_CallMethodOnly()) {
            Map<String, Object> txnTagDetails = new HashMap();
            txnTagDetails.put(INMESSAGEID1PK, getF_IN_INMESSAGEID1PK());
            txnTagDetails.put(INTRANSACTIONID, getF_IN_INTRANSACTIONID());
            txnTagDetails.put(INTITRANSACTIONID, getF_IN_INTITRANSACTIONID());
            txnTagDetails.put(INSERIALNO, getF_IN_INSERIALNO());
            txnTagDetails.put(INTRANSACTIONREF, getF_IN_INTRANSACTIONREF());
            txnTagDetails.put(INACCOUNTID, getF_IN_INACCOUNTID());
            txnTagDetails.put(INTRANSACTIONCODE, getF_IN_INTRANSACTIONCODE());
            txnTagDetails.put(INTRANSACTIONDTTM, getF_IN_INTRANSACTIONDTTM());
            txnTagDetails.put(INVALUEDTTM, getF_IN_INVALUEDTTM());
            txnTagDetails.put(INISFORCEPOST, isF_IN_INISFORCEPOST());
            txnTagDetails.put(INNARRATIVE, getF_IN_INNARRATIVE());
            txnTagDetails.put(INAMOUNT, getF_IN_INAMOUNT());
            txnTagDetails.put(INAMOUNTSIGN, getF_IN_INAMOUNTSIGN());
            txnTagDetails.put(INTXNCURRENCYCODE, getF_IN_INTXNCURRENCYCODE());
            txnTagDetails.put(INISACCSUSPPOSTED, isF_IN_INISACCSUSPPOSTED());
            txnTagDetails.put(INREASONFORSUSPFORCEPOST, getF_IN_INREASONFORSUSPFORCEPOST());
            txnTagDetails.put(INBRANCHNO, getF_IN_INBRANCHNO());
            txnTagDetails.put(INCUSTOMERCODE, getF_IN_INCUSTOMERCODE());
            txnTagDetails.put(INTOTALTXNLEGS, getF_IN_INTOTALTXNLEGS());
            txnTagDetails.put(CURRENCYSCALE, getF_IN_CurrencyScale());
			txnTagDetails.put(INBASEEQUIVAMOUNT, getF_IN_INBASEEQUIVAMOUNT());
			txnTagDetails.put(INBASEEQUIVCURRENCYCODE, getF_IN_INBASEEQUIVCURRENCYCODE());
            try {
                createTIUBPostingLegs(env, txnTagDetails);
                env.getFactory().commitTransaction();
                env.getFactory().beginTransaction();
                setF_OUT_TITransactionId((String) txnTagDetails.get(INTITRANSACTIONID));
            }
            catch (Exception e) {
                logger.error(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + " Error while executing. "
                        + ExceptionUtil.getExceptionAsString(e) + "txnId failed "+txnTagDetails.get(INTITRANSACTIONID));
                env.getFactory().rollbackTransaction();   //
                env.getFactory().beginTransaction();      //
                throw new BankFusionException(20020832,BankFusionMessages.getFormattedMessage(Integer.parseInt("20020832"),new String[] {}));
            }
            finally {
            }
        }
    }

    

	/**
     * @see com.trapedza.bankfusion.scheduler.gateway.interfaces.ISchedulerManager#getLock()
     */
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

    public void createTIUBPostingLegs(BankFusionEnvironment env, Map txnTagDetails) {

        IBOUB_TIP_TIUBPOSTINGMSG updateTItoUBtxn = (IBOUB_TIP_TIUBPOSTINGMSG) BankFusionThreadLocal.getPersistanceFactory()
                .getStatelessNewInstance(IBOUB_TIP_TIUBPOSTINGMSG.BONAME);

        String postingAction = (String) txnTagDetails.get(INAMOUNTSIGN);
        int currencyScale = (Integer) txnTagDetails.get(CURRENCYSCALE);
        String amount = (String) txnTagDetails.get(INAMOUNT);
        int baseCurrScale=SystemInformationManager.getInstance().getCurrencyScale((String) txnTagDetails.get(INBASEEQUIVCURRENCYCODE));
        BigDecimal scaledBaseEqual=getAmountForCurrency(String.valueOf(txnTagDetails.get(INBASEEQUIVAMOUNT)),baseCurrScale);
        BigDecimal scaledAmount = getAmountForCurrency(amount, currencyScale);

        updateTItoUBtxn.setBoID((String) txnTagDetails.get(INMESSAGEID1PK));
        updateTItoUBtxn.setF_ACTUALAMOUNT(scaledAmount);
        updateTItoUBtxn.setF_AMOUNT(scaledAmount);
		updateTItoUBtxn.setF_BASECURRENCYCODE((String) txnTagDetails.get(INBASEEQUIVCURRENCYCODE));
		updateTItoUBtxn.setF_BASEEQUIVALENT(scaledBaseEqual);
        updateTItoUBtxn.setF_BRANCHNO((String) txnTagDetails.get(INBRANCHNO));
        updateTItoUBtxn.setF_CUSTOMERCODE((String) txnTagDetails.get(INCUSTOMERCODE));
        updateTItoUBtxn.setF_EXCHRATETYPE(SPOT_TYPE);
        updateTItoUBtxn.setF_FORCEPOST((Boolean) txnTagDetails.get(INISFORCEPOST));
        updateTItoUBtxn.setF_ISACCSUSPPOSTED((Boolean) txnTagDetails.get(INISACCSUSPPOSTED));
        updateTItoUBtxn.setF_NARRATIVE((String) txnTagDetails.get(INNARRATIVE));
        updateTItoUBtxn.setF_PRIMARYID((String) txnTagDetails.get(INACCOUNTID));
        updateTItoUBtxn.setF_REASONFORSUSPFORCEPOST((Integer) txnTagDetails.get(INREASONFORSUSPFORCEPOST));
        updateTItoUBtxn.setF_SERIALNO(new Integer(txnTagDetails.get(INSERIALNO).toString()));
        updateTItoUBtxn.setF_SIGN(postingAction);
        updateTItoUBtxn.setF_TOTALTXNLEGS((Integer) txnTagDetails.get(INTOTALTXNLEGS));
        updateTItoUBtxn.setF_TRANSACTIONCODE((String) txnTagDetails.get(INTRANSACTIONCODE));
        updateTItoUBtxn.setF_TRANSACTIONDATE((Timestamp) (txnTagDetails.get(INTRANSACTIONDTTM)));
        updateTItoUBtxn.setF_TRANSACTIONID((String) txnTagDetails.get(INTRANSACTIONID));
        updateTItoUBtxn.setF_TITRANSACTIONID((String) txnTagDetails.get(INTITRANSACTIONID));
        updateTItoUBtxn.setF_TRANSACTIONREF((String) txnTagDetails.get(INTRANSACTIONREF));
        updateTItoUBtxn.setF_TXNCURRENCYCODE((String) txnTagDetails.get(INTXNCURRENCYCODE));
        if(logger.isInfoEnabled()){
        	logger.info("transaction date time TI: " + txnTagDetails.get(INTRANSACTIONDTTM));
        	logger.info("value date time TI: " + txnTagDetails.get(INVALUEDTTM));
        	logger.info("Message ID: " + txnTagDetails.get(INMESSAGEID1PK));
        }
        updateTItoUBtxn.setF_VALUEDATE((Timestamp) txnTagDetails.get(INVALUEDTTM));

        env.getFactory().create(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, updateTItoUBtxn);

    }

    private BigDecimal getAmountForCurrency(String amount, int currencyScale) {

        BigDecimal tempAmount = new BigDecimal(amount);
        BigDecimal amountForCurrency = tempAmount.movePointLeft(currencyScale);
        return amountForCurrency;
    }

}
