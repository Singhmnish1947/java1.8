/* ***********************************************************************************
 * Copyright (c) 2003,2006 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Trapedza Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: CreditCardFeature.java,v 1.19 2008/08/12 20:13:22 vivekr Exp $
 *
 * $Log: CreditCardFeature.java,v $
 * Revision 1.19  2008/08/12 20:13:22  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.17.10.1  2008/07/03 17:56:12  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.7  2008/06/16 15:19:22  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.6  2008/06/12 10:49:25  arun
 *  RIO on Head
 *
 * Revision 1.17  2007/09/05 06:12:32  lokeshn
 * Implemented Review comments.
 *
 * Lokesh N.
 *
 * Revision 1.16  2007/08/31 07:20:07  lokeshn
 * added comment.
 *
 * Lokesh N.
 *
 * Revision 1.15  2007/08/29 10:11:48  lokeshn
 * added code for implementing Non-Working days solution
 *
 * Lokesh N.
 *
 * Revision 1.14  2006/12/15 15:47:33  ruslans
 * changes for Date, Timestamp and Time
 *
 * Revision 1.13  2006/11/15 14:39:40  ruslans
 * publishing products as wrappers, product code gen
 *
 * Revision 1.12  2006/11/14 16:11:23  nitin
 * Removed Attribute references whereever possible
 *
 * Revision 1.11  2006/11/09 12:51:55  dmcevoy
 * Runtime enhancements for Features
 *
 * Revision 1.10  2006/11/02 10:09:43  ruslans
 * an update for code gen for externalisation of activity steps
 *
 * Revision 1.9  2006/05/16 18:04:55  ronnie
 * Implementation of new Persistence Support almost throughout the Server and Business Projects.
 *
 * Revision 1.8  2006/05/02 20:51:04  ronnie
 * Implementation of Hibernate support throughout the server infrastructure.
 *
 * Revision 1.7  2006/03/30 14:37:18  ronnie
 * Internationalisation support; Cache as Service; Performance enhancements.
 *
 * Revision 1.6  2006/02/08 15:39:56  ronnie
 * Bug fix where the fully paid date was being checked incorrectly thus causing the scheduled transaction not to be invoked.
 *
 * Revision 1.5  2006/01/26 20:30:02  ronnie
 * Bug fix 2441 - All balances are now correctly computed.  In addition, any overpayment is fully attributed to
 * the purchases balance.  Any cash withdrawal is firstly taken from any purchases overpayment before impacting
 * the cash balance.  Also, a zero-balance scheduled txn is no longer fired if the account had previously been
 * fully paid in the current period.
 *
 * Revision 1.4  2006/01/23 22:02:47  ronnie
 * Moved BO-related functionality from System Information into the BOFatory and moved the GlobalTag
 * functionality into a new class FrameworkInformation in the Business project.
 *
 * Revision 1.3  2006/01/18 23:17:12  ronnie
 * Now ignores zero-value transactions and includes a fixed reference.
 *
 * Revision 1.2  2006/01/18 22:35:24  ronnie
 * Bug fix where the credit card details were not being picked up in all cases.
 *
 * Revision 1.1  2006/01/18 22:14:33  ronnie
 * Credit Card mods plus further separation of business and framework.
 *
 */
package com.trapedza.bankfusion.features;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOCreditCardFeature;
import com.trapedza.bankfusion.bo.refimpl.IBODebitInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.boundary.outward.BankFusionPropertySupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.PostingEngineConstants;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.refimpl.AbstractCreditCardFeature;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingRouter;
import com.trapedza.bankfusion.postingengine.services.IPostingEngine;
import com.trapedza.bankfusion.scheduler.core.ScheduledBPHandler;
import com.trapedza.bankfusion.scheduler.core.ScheduledItem;
import com.trapedza.bankfusion.scheduler.gateway.interfaces.ISchedulerManager;
import com.trapedza.bankfusion.scheduler.services.ISchedulerService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerHolder;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AddWorkingDaysToDate;
import com.trapedza.bankfusion.servercommon.products.SimpleRuntimeProduct;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.utils.FatomUtils;
import com.trapedza.bankfusion.utils.GUIDGen;
import com.trapedza.bankfusion.utils.Utils;

/**
 * The Credit Card feature
 */
public class CreditCardFeature extends AbstractCreditCardFeature {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
	 */

    private transient final static Log logger = LogFactory.getLog(CreditCardFeature.class.getName());

    /** <code>CREDITCARDACCOUNTID</code> Parameter to the scheduled BP */
    public static final String CREDITCARDACCOUNTID = "CREDITCARDACCOUNTID";

    private IBODebitInterestFeature cashItem = null;
    private IBODebitInterestFeature purchaseItem = null;
    private IBOCreditCardFeature crCardSuper = null;
    private static int postBPDelayInMS = 0;
    private final static int DEFAULT_POST_REPAY_DELAY = 60000; // 1 Minute
    private String runtimeMFID = CommonConstants.EMPTY_STRING;

    static {
        postBPDelayInMS = DEFAULT_POST_REPAY_DELAY;

        String postDelay = BankFusionPropertySupport.getProperty("LendingFeature.PostRepaymentProcessDelayInSecs",
                CommonConstants.EMPTY_STRING);
        if (postDelay != null) {
            try {
                postBPDelayInMS = Integer.parseInt(postDelay) * 1000;
            }
            catch (NumberFormatException nfe) {
                logger.error("Can not parse the setting for LendingFeature.PostRepaymentProcessDelayInSecs : " + postDelay
                        + ", using default !", nfe);
            }
        }
    }

    /**
     * @param env
     */
    public CreditCardFeature(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * This method is expected to be called by the Statement Feature only. Performs the following
     * updates: STATEMENTBALANCE with the supplied Cleared Running Balance. PAYMENTBYDATE with the
     * supplied Statement Date plus the number of days to pay MINAMOUNTTOPAYLASTSTMT as the
     * STATEMENTBALANCE times the MINAMOUNTPERCENTAGE AMOUNTPAIDTHISPERIOD by zeroising it
     * CASHBALANCEINTEREST is set to the DEBITACCDINTEREST from the penalty interest feature
     * CASHBALANCE is set to the LASTPENALTYBALANCE from the penalty interest feature FULLYPAIDDATE
     * is reset to a "zero" date (1970-01-01) MINIMUMPAIDDATE is reset to a "zero" date (1970-01-01)
     * 
     * @param env
     * @param accountID
     * @param scpimpl
     * @param clearedRunningBalance
     * @param lastStmtDate
     */
    public void updateForStatement(BankFusionEnvironment env, String accountID, SimpleRuntimeProduct scpimpl,
            BigDecimal clearedRunningBalance, Timestamp lastStmtDate) {
        try {
            /*
             * Firstly, before attempting to update this Feature, ensure that there is a DR Int
             * Feature with a penalty interest entry
             */
            DebitInterestFeature drIntFeature = (DebitInterestFeature) scpimpl.getFeature(env, IBODebitInterestFeature.BONAME);
            if (drIntFeature == null) {
                logger.error("There should be a Debit Interest Feature associated with this product: " + scpimpl.getProductID());
                return;
            }
            /**
             * TODO - is it really required??? The penalty interest details have been moved to
             * penalty interest feature. - Ramesh T
             * 
             * Iterator drIntPersistedEnum =
             * drIntFeature.findPenaltyByAccountId(accountID).iterator(); if
             * (!drIntPersistedEnum.hasNext()) {
             * logger.error("A Debit Interest Penalty row has not been found for account: " +
             * accountID); return; } IBODebitInterestFeature cashItem = (IBODebitInterestFeature)
             * drIntPersistedEnum.next();
             */

            /*
             * Now attempt to locate the row on the credit card table for this account
             */
            Iterator crCardPersistedEnum = findByAccountId(accountID, getEnv(), null).iterator();
            if (!crCardPersistedEnum.hasNext()) {
                logger.error("Credit Card details cannot be found for Account: " + accountID);
                return;
            }
            IBOCreditCardFeature crCardPersist = (IBOCreditCardFeature) crCardPersistedEnum.next();
            /*
             * Update the Statement Balance
             */
            crCardPersist.setF_STATEMENTBALANCE(clearedRunningBalance);

            /*
             * Update the Payment By Date
             */
            Calendar cal = Calendar.getInstance();
            cal.setLenient(true);
            cal.setTime(lastStmtDate);
            // cal.add(Calendar.DAY_OF_MONTH, crCardPersist.getF_NUMBEROFDAYSTOPAY());
            int noOfDaystoPay = crCardPersist.getF_NUMBEROFDAYSTOPAY();
            java.sql.Date newDate = new java.sql.Date(cal.getTime().getTime());

            // Working Day Solution--add no of holidays between LASTSTMTDATE and NUMBEROFDAYSTOPAY
            newDate = new java.sql.Date(cal.getTime().getTime());
            newDate = AddWorkingDaysToDate.run("BRANCH", BankFusionThreadLocal.getUserSession().getBranchSortCode(),
                    new Integer(0), newDate, new Integer(noOfDaystoPay), env);

            // PAYMENTBYDATE = LASTSTMTDATE+NUMBEROFDAYSTOPAY+no of Non-Working days between
            // LASTSTMTDATE and NUMBEROFDAYSTOPAY
            crCardPersist.setF_PAYMENTBYDATE(newDate);

            /*
             * Update the Minimum Amount To Pay
             */
            BigDecimal percentage = crCardPersist.getF_MINAMOUNTPERCENTAGE();
            BigDecimal minAmountToPay = (clearedRunningBalance.multiply(percentage)).divide(new BigDecimal(100),
                    BigDecimal.ROUND_HALF_UP);
            String currencyCode = crCardPersist.getF_ISOCURRENCYCODE();
            IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            int currencyScale = bizInfo.getCurrencyScale(currencyCode, env);
            int roundingUnits = bizInfo.getCurrencyRoundingUnits(currencyCode, env);
            crCardPersist.setF_MINAMOUNTTOPAYLASTSTMT(Utils.roundCurrency(minAmountToPay, roundingUnits, currencyScale));

            /*
             * Update the Amount Paid This Period
             */
            crCardPersist.setF_AMOUNTPAIDTHISPERIOD(CommonConstants.BIGDECIMAL_ZERO);
            crCardPersist.setF_CASHPAIDTHISPERIOD(CommonConstants.BIGDECIMAL_ZERO);

            /*
             * Update the Cash Balance and the Cash Balance Interest from the Debit Interest Feature
             */
            crCardPersist.setF_CASHBALANCE(cashItem.getF_LASTPENALTYBALANCE());
            crCardPersist.setF_CASHBALANCEINTEREST(cashItem.getF_DEBITACCDINTEREST());

            /*
             * Update the Fully Paid Date and the Minimum Paid Date to a "zero" Date (1970-01-01)
             */
            crCardPersist.setF_FULLYPAIDDATE(new java.sql.Date(0L));
            crCardPersist.setF_MINIMUMPAIDDATE(new java.sql.Date(0L));

            /*
             * Now post a zero value posting to this account to ensure an entry appears on the
             * interest history
             */
            postZeroBalance(env, accountID, scpimpl.getProductID(), currencyCode);
        }
        catch (BankFusionException e) {
            logger.error("Unanticipated error while updating Credit Card information: " + accountID);
        }
    }

    private boolean postZeroBalance(BankFusionEnvironment env, String accountID, String productID, String currencyCode) {

        IBOFinancialPostingMessage postingMessage = (IBOFinancialPostingMessage) getFactory().getStatelessNewInstance(
                IBOFinancialPostingMessage.BONAME);

        FatomUtils.createStandardItemsMessage(postingMessage, env);
        PostingHelper.setDefaultValuesForFinPosting(postingMessage, env);
        // postingMessage.setNarrative(getF_IN_ACCRUALNARRATIVE());
        postingMessage.setSign('+');
        postingMessage.setTransactionRef("Statement");
        postingMessage.setSerialNo(1);
        postingMessage.setBranchID(BankFusionThreadLocal.getUserSession().getBranchSortCode());
        postingMessage.setTransCode(getF_IN_ACCRUALTXNCODE());
        postingMessage.setPrimaryID(accountID);
        postingMessage.setProductID(productID);
        Date toDay = SystemInformationManager.getInstance().getBFBusinessDateTime(env.getRuntimeMicroflowID());
        postingMessage.setValueDate(toDay);
        postingMessage.setTransactionDate(toDay);
        postingMessage.setShortName(CommonConstants.EMPTY_STRING);
        postingMessage.setPERouterProfileID(CommonConstants.EMPTY_STRING);
        postingMessage.setAcctCurrencyCode(currencyCode);
        postingMessage.setTransactionID(GUIDGen.getNewGUID());
        postingMessage.setNarrative(PostingHelper.getBuildedNarrative(postingMessage, CommonConstants.EMPTY_STRING));

        ArrayList postingMessages = new ArrayList();
        postingMessages.add(postingMessage);
        return postTransaction(env, postingMessages);
    }

    /**
     * 
     * @param env
     * @param postingMessages
     * @return
     */
    private boolean postTransaction(BankFusionEnvironment env, ArrayList postingMessages) {

        IPostingEngine pe = null;
        try {

            /*
             * Perform Postings Get the PostingEngine Service first, and from that get a reference
             * to the router. Post the messages through the router, then indicate that the posting
             * has completed.
             */
            pe = (IPostingEngine) ServiceManager.getService(ServiceManager.POSTING_ENGINE_SERVICE);
            IPostingRouter pr = (IPostingRouter) pe.getNewInstance();
            pr.post(postingMessages, env);
            return true;
        }
        catch (Exception e) {
            logger.error("Exception: Occured at the time of Posting", e);
            return false;
        }finally {
            if(pe!=null)
        	pe.postingComplete();
        }
    }

    /**
     * @see com.trapedza.bankfusion.features.AbstractFeature#postingEngineUpdate(com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage)
     */
    public void postingEngineUpdate(IPostingMessage message) {

        if (message.getMessageType() != 'N')
            return;

        env = BankFusionThreadLocal.getBankFusionEnvironment();

        boolean isForwardValued = message.isForwardValued();
        boolean isForwardValuedIntoValue = message.isForwardValuedIntoValue();
        if (isForwardValued && !isForwardValuedIntoValue)
            return;

        String transCode = message.getTransCode();
        char sign = message.getSign();
        String accountID = message.getPrimaryID();
        BigDecimal txnAmount = ((IBOFinancialPostingMessage) message).getF_AMOUNT();
        if (txnAmount.signum() == 0)
            return;
        Date postingDate = message.getTransactionDate();
        runtimeMFID = message.getRunTimeBPID();

        DebitInterestFeature drFeature = (DebitInterestFeature) message.getObject(PostingEngineConstants.DRINTFEATURE);
        if (drFeature == null) {
            // throw getBankFusionException(397, new Object[] { accountID }, logger);
            EventsHelper.handleEvent(CommonsEventCodes.E_CREDIT_FEATURE_SHOULD_HAVE_DEBIT_INTEREST, new Object[] { accountID },
                    new HashMap(), env);

        }
        
        getInterestDetails(drFeature, message, accountID);
        
        getCRCardDetails(message, accountID);

        // A Debit Transaction
        if (sign == '-') {

            // A DR Cash or Fee transaction
            if (isCashOrFeeTransaction(transCode, env)) {
                updatePurchaseAndCashBalance(txnAmount.negate(), true);
                crCardSuper.setF_LASTCASHBALANCETRANSDATE(new java.sql.Date(postingDate.getTime()));
            }

            // A Purchase
            else updatePurchaseAndCashBalance(txnAmount.negate(), false);
        }

        // A Credit transaction (A Repayment)
        else {

            Date paymentByDate = crCardSuper.getF_PAYMENTBYDATE();

            // We are paying on time
            if (PostingHelper.isValuedBefore(postingDate, paymentByDate)) {

                BigDecimal statementBalance = crCardSuper.getF_STATEMENTBALANCE();
                BigDecimal amountPaidThisPeriod = crCardSuper.getF_AMOUNTPAIDTHISPERIOD();
                BigDecimal minAmountToPay = crCardSuper.getF_MINAMOUNTTOPAYLASTSTMT();

                // If this is a full or even an over payment then apply certain updates.
                if (txnAmount.compareTo(statementBalance.negate().subtract(amountPaidThisPeriod)) >= 0) {

                    Date fullyPaidDate = crCardSuper.getF_FULLYPAIDDATE();
                    /*
                     * If this is already fully paid and we're just paying in more, then we don't
                     * schedule another transaction
                     */
                    if (fullyPaidDate.getTime() == 0L)
                        scheduleMicroflow(message.getPrimaryID(), env);

                    BigDecimal stmntCashBalanceInterest = crCardSuper.getF_CASHBALANCEINTEREST();
                    BigDecimal currentCashBalanceInterest = cashItem.getF_DEBITACCDINTEREST();
                    cashItem.setF_DEBITACCDINTEREST(currentCashBalanceInterest.subtract(stmntCashBalanceInterest));

                    crCardSuper.setF_FULLYPAIDDATE(new java.sql.Date(postingDate.getTime()));
                    crCardSuper.setF_MINIMUMPAIDDATE(new java.sql.Date(postingDate.getTime()));

                    updatePurchaseAndCashBalance(txnAmount, false);
                }

                // Paying at least the minimum amount on time but not the full amount
                else if (txnAmount.compareTo(minAmountToPay.negate()) >= 0 && txnAmount.compareTo(statementBalance.negate()) < 0) {

                    crCardSuper.setF_MINIMUMPAIDDATE(new java.sql.Date(postingDate.getTime()));
                    updatePurchaseAndCashBalance(txnAmount, false);
                }

                // We're paying on time but not even the minimum amount
                else updatePurchaseAndCashBalance(txnAmount, false);
            }

            // Not paying on time
            else updatePurchaseAndCashBalance(txnAmount, false);
        }
    }

    /**
     * 
     * @param amount
     * @param isCashWithdrawal
     */
    private void updatePurchaseAndCashBalance(BigDecimal amount, boolean isCashWithdrawal) {

        BigDecimal purchaseBalance = purchaseItem.getF_NOMINALDISCOUNTEDPRINCIPALDR();
        BigDecimal cashBalance = cashItem.getF_LASTPENALTYBALANCE();

        // A Repayment
        if (amount.signum() > 0) {
            // We repay the cash amount first. However, we can't overpay the cash balance.
            // Any overpayment must be attributed to the purchases side.
            BigDecimal cashAdjustment;
            // Full or underpayment of cash so the full amount goes towards cash repayment
            if (amount.compareTo(cashBalance.abs()) <= 0)
                cashAdjustment = amount.negate();
            // Overpayment, so the adjustment amount is the full cash balance
            else cashAdjustment = cashBalance;

            BigDecimal purchaseAdjustment = cashAdjustment.add(amount).negate();

            cashItem.setF_LASTPENALTYBALANCE(cashBalance.subtract(cashAdjustment));
            purchaseItem.setF_NOMINALDISCOUNTEDPRINCIPALDR(purchaseBalance.subtract(purchaseAdjustment));

            BigDecimal cashPaidThisPeriod = crCardSuper.getF_CASHPAIDTHISPERIOD();
            crCardSuper.setF_CASHPAIDTHISPERIOD(cashPaidThisPeriod.subtract(cashAdjustment));

            BigDecimal amountPaidThisPeriod = crCardSuper.getF_AMOUNTPAIDTHISPERIOD();
            crCardSuper.setF_AMOUNTPAIDTHISPERIOD(amountPaidThisPeriod.add(amount));
        }
        // Either a purchase or a cash withdrawal
        else {
            if (isCashWithdrawal) {
                if (purchaseBalance.signum() > 0) {
                    // We have an overpayment on the account and it will always be attributed to the
                    // purchase balance so we need to reduce this first before touching the cash
                    // balance
                    BigDecimal purchaseAdjustment;
                    // Full or underwithdrawal of purchases so the full amount goes towards purchase
                    // balance
                    if (amount.abs().compareTo(purchaseBalance) <= 0)
                        purchaseAdjustment = amount;
                    // Overwithdrawal, so the adjustment amount is the full purchase balance
                    else purchaseAdjustment = purchaseBalance.negate();

                    BigDecimal cashAdjustment = amount.subtract(purchaseAdjustment);

                    cashItem.setF_LASTPENALTYBALANCE(cashBalance.add(cashAdjustment));
                    purchaseItem.setF_NOMINALDISCOUNTEDPRINCIPALDR(purchaseBalance.add(purchaseAdjustment));
                }
                else cashItem.setF_LASTPENALTYBALANCE(cashBalance.add(amount));
            }
            else purchaseItem.setF_NOMINALDISCOUNTEDPRINCIPALDR(purchaseBalance.add(amount));
        }
    }

    private IPersistenceObjectsFactory getFactory() {
        return BankFusionThreadLocal.getPersistanceFactory();
    }

    /**
     * 
     * @param transCode
     * @param env
     * @return
     */
    private boolean isCashOrFeeTransaction(String transCode, BankFusionEnvironment env) {
        try {
            // If this is a fee transaction then no need to read the MIS table
            if (transCode.equals(getF_IN_FEETRANSACTIONCODE()))
                return true;

            /*
             * It's not a fee txn, so get the appropriate row from the MIS table and see if it is a
             * cash txn
             */
            // Using the Cache of TransactionScreenControl Table for fetching the details.
            MISTransactionCodeDetails mistransDetails;
            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo()).getMisTransactionCodeDetails(transCode);

            IBOMisTransactionCodes misPersisted = mistransDetails.getMisTransactionCodes();
            return misPersisted.isF_CASHTRANS();
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * 
     * @param message
     * @param accountID
     */
    private void getCRCardDetails(IPostingMessage message, String accountID) {
        Iterator crCardPersists = findByAccountId(accountID, getEnv(), null).iterator();
        if (crCardPersists.hasNext()) {
            crCardSuper = (IBOCreditCardFeature) crCardPersists.next();
        }
        else
        // throw getBankFusionException(398, new Object[] { accountID }, logger);
        EventsHelper.handleEvent(CommonsEventCodes.E_UNABLE_TO_LOCATE_CREDIT_CARD_INFORMATION, new Object[] { accountID },
                new HashMap(), env);

    }

    /**
     * 
     * @param drFeature
     * @param message
     * @param accountID
     */
    private void getInterestDetails(DebitInterestFeature drFeature, IPostingMessage message, String accountID) {

        /*
         * TODO - is it really required??? The penalty interest details have moved to penalty
         * interest feature. - Ramesh T
         * 
         * Iterator drIntPerists = drFeature.findPenaltyByAccountId(accountID).iterator(); if
         * (drIntPerists.hasNext()) { cashItem = (IBODebitInterestFeature) drIntPerists.next(); }
         * else // throw getBankFusionException(399, new Object[] { accountID }, logger);
         * EventsHelper.handleEvent(CommonsEventCodes.E_UNABLE_TO_FIND_A_DEBIT_INTEREST, new
         * Object[] { accountID }, new HashMap(), env);
         */
        purchaseItem = drFeature.getDebitIntFeature(accountID);
        if (purchaseItem == null) {
            EventsHelper.handleEvent(CommonsEventCodes.E_UNABLE_TO_FIND_A_DEBIT_INTEREST_ACCOUNT, new Object[] { accountID },
                    new HashMap(), env);
        }

    }

    /**
     * 
     * @param accountID
     * @param env
     */
    private void scheduleMicroflow(String accountID, BankFusionEnvironment env) {

        ScheduledItem scheduledItem = new ScheduledItem();
        scheduledItem.setBpID(getF_IN_ADJUSTMENTMICROFLOWID());

        Hashtable mfParams = new Hashtable(2);
        scheduledItem.setBpProperties(mfParams);
        scheduledItem.setStartDateTime(new Date(SystemInformationManager.getInstance().getBFBusinessDateTime(runtimeMFID).getTime()
                + postBPDelayInMS));
        scheduledItem.setExpiryDate(new Date(SystemInformationManager.getInstance().getBFBusinessDateTime(runtimeMFID).getTime()
                + (60000 * 60 * 24 * 365)));
        scheduledItem.setStartNotEarlierThan(scheduledItem.getStartDateTime());
        mfParams.put(CREDITCARDACCOUNTID, accountID);

        ISchedulerManager schedulerManager = (ISchedulerManager) ((ISchedulerService) ServiceManager
                .getService(ServiceManager.SCHEDULER_SERVICE)).getNewInstance();
        schedulerManager.scheduleItem(scheduledItem, ScheduledBPHandler.ID, env);
    }

    /**
     * @param manager
     * @see com.trapedza.bankfusion.features.AbstractFeature#registerWithUpdateLoggerManager(com.trapedza.bankfusion.core.UpdateAuditLoggerManager)
     */
    public void registerWithUpdateLoggerManager(UpdateAuditLoggerManager manager) {
        if (!manager.isTransactionLoggingEnabled())
            return;
        super.registerWithUpdateLoggerManager(manager);
        manager.addNewUpdateHolder(new UpdateAuditLoggerHolder(CreditCardFeature.class.getName(), svnRevision));

    }

}
