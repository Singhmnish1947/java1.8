/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 */
package com.trapedza.bankfusion.fatoms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractIBSNetMessageFinancialFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * 
 * This class calling the posting engine service and setting some of the IBSNet status codes.
 * 
 * @author Anand Khamitkar
 * @created Feb 05, 2008 2:15:12 PM
 * 
 */
public class IBSNetMessageFinancialFatom extends AbstractIBSNetMessageFinancialFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     */

    /**
     * logger instance
     */
    private transient final static Log logger = LogFactory.getLog(IBSNetMessageFinancialFatom.class.getName());

    /**
     * BankFusionEnvironment instance
     */
    public BankFusionEnvironment env;

    /**
     * String instance
     */
    private final static String CONTRA = "CONTRA";

    /**
     * String instance
     */
    private String SUSPENSE = "SUSPENSE";

    /**
     * boolean type
     */
    private boolean ContraOrSusp = true;

    /**
     * String instance
     */
    public static final String FINANCIAL_POSTING_MICROFLOW_NAME = "IBSNetFinancialPostingEngine";

    /**
     * This holds the list of transactions to be posted.
     */
    private ArrayList postingMessages = new ArrayList();

    /**
     * String instance
     */
    private String postingMessageStatus = CommonConstants.EMPTY_STRING;

    /**
     * IBSNetMessageFinancialFatom consturactor
     * 
     * @param env
     */
    public IBSNetMessageFinancialFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * main process mathed
     * 
     * @param env
     */
    public void process(BankFusionEnvironment env) {
        logger.debug(" Process " + BankFusionThreadLocal.getPersistanceFactory().getFactoryID());
        setF_OUT_PE_AUTHORISED_FLG("0");
        execute(env);
    }

    /**
     * This method validates the message received for cashwithdrawal, creates messages for posting
     * and calls postTransactions() for posting.
     */
    public void execute(BankFusionEnvironment env) {
        boolean proceed = true;
        setF_OUT_PE_AUTHORISED_FLG("0");
        if (proceed) {
            try {
                postTransactions(postingMessages, env);
            }
            catch (Exception e) {
                postingMessageStatus = "MessageFailed";
                setF_OUT_postingMessageStatus(postingMessageStatus);
                setF_OUT_PE_AUTHORISED_FLG("4");
                logger.error(e);
            }
        }
    }

    /**
     * post transaction main method
     * 
     * @param postingMessages
     * @param env
     */
    public void postTransactions(ArrayList postingMessages, BankFusionEnvironment env) {

        HashMap map = new HashMap();
        String currencyCode = getF_IN_ISO_CURRENCY_CODE();
        try {
            {
                SUSPENSE = getF_IN_SUSPENSEFLAG();
                String account = checkForAccount(env, getF_IN_ACCOUNT_NUMBER(), SUSPENSE);
                map.put("ACCOUNT1_ACCOUNTID", account);
                map.put("ACCOUNT1_AMOUNT", getF_IN_TRANSACTION_AMOUNT());
                map.put("ACCOUNT1_AMOUNT_CurrCode", currencyCode);
                map.put("ACCOUNT1_NARRATIVE", getF_IN_TRANSACTION_NARRATIVE());
                map.put("ACCOUNT1_POSTINGACTION", getF_IN_SIGN_OF_AMOUNT());
                map.put("ACCOUNT1_TRANSCODE", getF_IN_TRANSACTON_CODE());

                map.put("ACCOUNT2_AMOUNT", getF_IN_TRANSACTION_AMOUNT());
                map.put("ACCOUNT2_AMOUNT_CurrCode", currencyCode);
                map.put("ACCOUNT2_NARRATIVE", getF_IN_TRANSACTION_NARRATIVE());
                map.put("ACCOUNT2_POSTINGACTION", getF_IN_SIGN_FOR_POSTINGACTION());
                map.put("ACCOUNT2_TRANSCODE", getF_IN_TRANSACTON_CODE());

                map.put("BASE_EQUIVALENT", getF_IN_BASE_EQUIVALENT());
                map.put("EXCHRATE_RATE", getF_IN_EXCHANGE_RATE());
                map.put("TRANSACTION_REFERENCE", getF_IN_TRANSACTION_REFERENCE());
                map.put("VALUE_DATE", getF_IN_VALUE_DATE());

                logger.info("IBSNet Value Date:" + getF_IN_VALUE_DATE());
                map.put("MANUALVALUEDATE", getF_IN_VALUE_DATE());
                map.put("MANUALVALUETIME", getF_IN_VALUE_DATE());

                Timestamp maturityDate = getF_IN_MATURITY_DATE();
                if (maturityDate != null) {
                    map.put("MATURITY_DATE", maturityDate);
                }

                /**
                 * This block is synchronized to avoid the stale-state exception The scenario - when
                 * IBSNet sends across the same set of account transactions. TODO - This
                 * (synchronized block) could be a performance issue. The contention context should
                 * be implemented in the future by posting the transactions delayed.
                 **/

                callMFExecute(map, getF_IN_CONTRA_ACCOUNT());

            }

        }
        catch (BankFusionException exception) {
            logger.info(exception.getLocalisedMessage());
            if (postingMessageStatus.equals("MessageFailed")) {
                setF_OUT_postingMessageStatus(postingMessageStatus);
                setF_OUT_PE_AUTHORISED_FLG("4");
                logger.error(exception);
            }
            if (!postingMessageStatus.equals("MessageFailed") && ContraOrSusp) {
                setF_OUT_postingMessageStatus(postingMessageStatus);
                setF_OUT_PE_AUTHORISED_FLG("4");
            }
            try {
                env.getFactory().rollbackTransaction();
                env.getFactory().beginTransaction(); //
            }
            catch (Exception ignored) {
                if (postingMessageStatus.equals("MessageFailed")) {
                    setF_OUT_postingMessageStatus(postingMessageStatus);
                    setF_OUT_PE_AUTHORISED_FLG("4");
                    logger.error(ignored);
                }
                if (!postingMessageStatus.equals("MessageFailed") && ContraOrSusp) {
                    setF_OUT_postingMessageStatus(postingMessageStatus);
                    setF_OUT_PE_AUTHORISED_FLG("4");
                }
            }
        }

    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    private void callMFExecute(HashMap map, String contraAccountID) {
        synchronized (IBSNetMessageFinancialFatom.class) {
            try {
                PostTransaction transaction = new PostTransaction();
                String contraAccount = CommonConstants.EMPTY_STRING;
                contraAccount = checkForAccount(BankFusionThreadLocal.getBankFusionEnvironment(), contraAccountID, CONTRA);
                map.put("ACCOUNT2_ACCOUNTID", contraAccount);

                transaction.setFactory(BankFusionThreadLocal.getPersistanceFactory());
                transaction.setInputMap(map);
                transaction.setContraAccountID(contraAccountID);
                boolean txnPosted = transaction.postTransaction();
                logger.info("Posted for Transacion " + contraAccountID + " Status " + txnPosted);
            }
            catch (Exception e) {
                logger.error(e);
            }
        }
    }

    /**
     * Check for tha account existnance
     * 
     * @param env
     * @param account
     * @param type
     * @return @
     */
    public String checkForAccount(BankFusionEnvironment env, String account, String type) {
        // IBOAccount accountObj = null;
        IBOAttributeCollectionFeature accountObj = null;
        BusinessValidatorBean validatorBean = null;
        try {
            accountObj = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
                    account);

            validatorBean = new BusinessValidatorBean();

            if ((accountObj == null) || (accountObj != null && accountObj.isF_DORMANTSTATUS()
                    || validatorBean.validateAccountStopped(accountObj, env))) {
                throw new BankFusionException();
            }

        }
        catch (BankFusionException e) {
            if (type.equals("CONTRA")) {
                setF_OUT_PE_AUTHORISED_FLG("8");
                ContraOrSusp = false;
                String eMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40409310, new Object[] { "CONTRA" },
                        BankFusionThreadLocal.getUserSession().getUserLocale());
                logger.error(eMessage);
                throw e;
            }
            if (type.equals("SUSPENSE")) {
                setF_OUT_PE_AUTHORISED_FLG("8");
                setF_IN_SUSPENSEFLAG("SUSPENSE");
                ContraOrSusp = false;
                String eMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40409310, new Object[] { "SUSPENSE" },
                        BankFusionThreadLocal.getUserSession().getUserLocale());
                logger.error(eMessage);
                throw e;
            }
            if (ContraOrSusp) {
                postingMessageStatus = "MessageFailed";
                setF_OUT_postingMessageStatus(postingMessageStatus);
                setF_OUT_PE_AUTHORISED_FLG("4");
                throw e;
            }
            if (accountObj == null) {
                postingMessageStatus = "MessageFailed, Account Not Found";
                setF_OUT_postingMessageStatus(postingMessageStatus);
                setF_OUT_PE_AUTHORISED_FLG("4");
                throw e;
            }
        }
        return accountObj.getBoID();
    }
}

class PostTransaction {
    private IPersistenceObjectsFactory factory;
    private Map inputMap;
    private String contraAccountID;
    /**
     * logger instance
     */
    private transient final static Log logger = LogFactory.getLog(IBSNetMessageFinancialFatom.class.getName());

    public String getContraAccountID() {
        return contraAccountID;
    }

    public void setContraAccountID(String contraAccountID) {
        this.contraAccountID = contraAccountID;
    }

    /**
     * String instance
     */

    public IPersistenceObjectsFactory getFactory() {
        return factory;
    }

    public void setFactory(IPersistenceObjectsFactory factory) {

        this.factory = factory;

    }

    public Map getInputMap() {
        return inputMap;
    }

    public void setInputMap(Map inputMap) {
        this.inputMap = inputMap;
    }

    public boolean postTransaction() {
        try {
            try {
                getFactory().beginTransaction();
            }
            catch (Exception e) {
                logger.error(e);

            }

            BankFusionThreadLocal.getBankFusionEnvironment().setFactory(getFactory());
            BankFusionThreadLocal.setPersistanceFactory(getFactory());
            MFExecuter.executeMF(IBSNetMessageFinancialFatom.FINANCIAL_POSTING_MICROFLOW_NAME,
                    BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
            getFactory().commitTransaction();
            getFactory().beginTransaction(); //

        }
        catch (Exception e) {
            logger.error(e);
            getFactory().rollbackTransaction();
            getFactory().beginTransaction(); //
            return false;

        }
        return true;
    }

}
