/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Trapedza Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: UB_IBI_InternetBankingFeature.java,v 1.31 2009/05/06 10:11:01 akhileshp Exp $
 *
 * $Log: BankStatementFeature.java,v $
 *
 *
 */
package com.trapedza.bankfusion.features;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOInterestApplicationMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_IBI_INFTB_IBIAccount;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.CurrencyValue;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.ExtensionPointHelper;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.features.refimpl.AbstractUB_IBI_InternetBankingFeature;
import com.trapedza.bankfusion.features.refimpl.IUB_IBI_InternetBankingFeature;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.systeminformation.PostingMessageConstants;

public class UB_IBI_InternetBankingFeature extends AbstractUB_IBI_InternetBankingFeature implements IUB_IBI_InternetBankingFeature {

    /**
     * The logger instance for this feature
     */
    private transient final static Log logger = LogFactory.getLog(UB_IBI_InternetBankingFeature.class.getName());

    public UB_IBI_InternetBankingFeature(BankFusionEnvironment env) {
        super(env);
    }

    static {
        com.trapedza.bankfusion.utils.Tracer.register(cvsRevision);
    }

    /**
     * Returns an ExtensionPoint associated with the name
     */
    public ExtensionPointHelper getExtensionPoint(String name) {
        return null;
    }

    /**
     * Accepts a posting message and checks if the amount is a non zero amount. If amount is a non
     * zero amount then it checks whether Account is eligible to send details to Internet Banking
     * Then it checks whether EOD is running or Not. If EOD is running it updates the table
     * INFTB_InternetBankingAccount column INEODBALANCECHANGE as true and come out of the method IF
     * EOD is not running then raise business Events 1.BALANCE AFFECTED 2.TRANSACTION OCCURRED
     * 
     * @param message
     */
    public void postingEngineUpdate(IPostingMessage message) {

        char messageType = message.getMessageType();

        /*
         * Checks if the MessageType is a financial Posting Message or Interest Posting Message or
         * Base Code Rate Change Message or Account Rate Change Message or Tiered Rate Change
         * Message
         */

        if (messageType == PostingMessageConstants.FINANCIAL_POSTING_MESSAGE) {
            financialPosting(message);
        }
        else if (messageType == PostingMessageConstants.INTEREST_APPLICATION_MESSAGE) {
            interestPosting(message);
        }
        else if (PostingHelper.isAccountRateChangeMessage(message)) { // artf789199:Issue Fix done
                                                                      // to send either account or
                                                                      // base code or Tiered rate
                                                                      // changes details.
            baseCodeRateChange(message);
        }
        else {
            return;
        }
    }

    /**
     * process
     */
    public void process(BankFusionEnvironment env) {
        super.process(env);
    }

    /**
     * returns the feature implementation type.
     */
    public String toString() {
        return "com.trapedza.bankfusion.features.UB_IBI_InternetBankingFeature";
    }

    public static BigDecimal RoundBasedOnCurrency(BigDecimal unRoundedAmount, String currencyCode) {
        CurrencyValue curr = new CurrencyValue(currencyCode, unRoundedAmount);
        return (new CurrencyValue(curr.getCurrencyCode(), curr.roundCurrency()));

    }

    public void baseCodeRateChange(IPostingMessage message) {
        IBOUB_IBI_INFTB_IBIAccount accDetails = (IBOUB_IBI_INFTB_IBIAccount) BankFusionThreadLocal.getPersistanceFactory()
                .findByPrimaryKey(IBOUB_IBI_INFTB_IBIAccount.BONAME, message.getPrimaryID(), true);
        if (null != accDetails) {
            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            IBusinessInformation bizzInfo = ((IBusinessInformation) ubInformationService.getBizInfo());
            Boolean isEODRunning = bizzInfo.isEODInProgress();
            if (isEODRunning.equals(true)) {
                accDetails.setF_ISACCOUNTAMENDED(true);
            }
            else {
                /*
                 * This will raise an Account Amendment event and sends the to the IFM
                 */
                IBOAttributeCollectionFeature accountBO = null;
                String productId = message.getProductID();
                accountBO = PostingHelper.getAccountBO(message);
                HashMap paramsToRaiseEvent = new HashMap();
                paramsToRaiseEvent.put("ACCOUNTNAME", accountBO.getF_ACCOUNTNAME().toString());
                paramsToRaiseEvent.put("ACCOUNTNO", (String) message.getPrimaryID());
                paramsToRaiseEvent.put("PRODUCTID", productId);
                paramsToRaiseEvent.put("CURRENCYCODE", message.getAcctCurrencyCode());
                EventsHelper.handleEvent(ChannelsEventCodes.I_ACCOUNT_AMEND_BUSINESS_EVENT, new Object[] {}, paramsToRaiseEvent,
                        env);

                if (logger.isInfoEnabled()) {
                    logger.info("raising event block");
                }
            }
        }
        else {
            logger.info("Internet Banking not enabled");
        }
    }
    public void interestPosting(IPostingMessage message) {
        IBOAttributeCollectionFeature accountBO = null;
        String productId = message.getProductID();
        boolean forwardValued = false;
        boolean forwardValuedIntoValue = false;
        String currencyCode = null;
        BigDecimal txnAmount = CommonConstants.BIGDECIMAL_ZERO;

        IBOInterestApplicationMessage intPostMessage = (IBOInterestApplicationMessage) message;
        forwardValued = intPostMessage.isForwardValued();
        forwardValuedIntoValue = intPostMessage.isForwardValuedIntoValue();
        accountBO = PostingHelper.retrieveAccount(intPostMessage.getPrimaryID(), intPostMessage.getBranchID(),
                intPostMessage.getAcctCurrencyCode(), env);
        currencyCode = accountBO.getF_ISOCURRENCYCODE();
        txnAmount = RoundBasedOnCurrency(intPostMessage.getF_AMOUNTAsCurrency(), currencyCode);
        txnAmountExists(txnAmount, message, accountBO, productId, forwardValued, forwardValuedIntoValue);
    }

    public void financialPosting(IPostingMessage message) {
        IBOAttributeCollectionFeature accountBO = null;
        String productId = message.getProductID();
        boolean forwardValued = false;
        boolean forwardValuedIntoValue = false;
        String currencyCode = null;
        BigDecimal txnAmount = CommonConstants.BIGDECIMAL_ZERO;

        IBOFinancialPostingMessage finPostMessage = (IBOFinancialPostingMessage) message;
        forwardValued = finPostMessage.isForwardValued();
        forwardValuedIntoValue = finPostMessage.isForwardValuedIntoValue();
        accountBO = PostingHelper.retrieveAccount(finPostMessage.getPrimaryID(), finPostMessage.getBranchID(),
                finPostMessage.getAcctCurrencyCode(), env);
        currencyCode = accountBO.getF_ISOCURRENCYCODE();
        txnAmount = RoundBasedOnCurrency(finPostMessage.getF_AMOUNTAsCurrency(), currencyCode);
        txnAmountExists(txnAmount, message, accountBO, productId, forwardValued, forwardValuedIntoValue);
    }

    public void txnAmountExists(BigDecimal txnAmount, IPostingMessage message, IBOAttributeCollectionFeature accountBO,
            String productId, boolean forwardValued, boolean forwardValuedIntoValue) {

        String accountID = message.getPrimaryID();
        Long txnCounter = (long) message.getTransactionCounter();

        if (txnAmount.compareTo(BigDecimal.valueOf(0)) > 0) {
            IBOUB_IBI_INFTB_IBIAccount accDetails = (IBOUB_IBI_INFTB_IBIAccount) BankFusionThreadLocal.getPersistanceFactory()
                    .findByPrimaryKey(IBOUB_IBI_INFTB_IBIAccount.BONAME, message.getPrimaryID(), true);

            if (accDetails != null) {
                IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
                        .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE))
                        .getBizInfo();
                if (bizInfo.isEODInProgress()) {
                    accDetails.setF_ISBALANCECHANGED(true);
                }
                else {
                    // Raise Events for a.BALANCE AFFECTED
                    // b.TRANSACTION OCCURRED

                    if (logger.isInfoEnabled()) {
                        logger.info("Raising the Business Events by Calling the Microflow ");
                    }
                    HashMap paramsForWhatProDef = new HashMap();
                    paramsForWhatProDef.put("productid", productId);
                    HashMap featuresAttToProduct = MFExecuter.executeMF(IfmConstants.WHAT_PRODUCT_DEFAULTS, env,
                            paramsForWhatProDef);
                    HashMap paramsForTransactionOccured = new HashMap();
                    HashMap paramsForAcctAmendEvent = new HashMap();
                    // Raise Event for TRANSACTION OCCURRED
                    paramsForTransactionOccured.put("ACCID", (String) message.getPrimaryID());
                    paramsForTransactionOccured.put("UBACCTRANSCOUNTER", (String) txnCounter.toString());
                    if (forwardValued && !forwardValuedIntoValue) {
                        paramsForTransactionOccured.put(IfmConstants.IS_FWD_VALUE_TXN, Boolean.TRUE.toString());
                    }
                    else {
                        paramsForTransactionOccured.put(IfmConstants.IS_FWD_VALUE_TXN, Boolean.FALSE.toString());
                    }
                    if (forwardValuedIntoValue) {
                        paramsForTransactionOccured.put(IfmConstants.FWD_DATED_INTO_VALUE, Boolean.TRUE.toString());
                    }
                    else {
                        paramsForTransactionOccured.put(IfmConstants.FWD_DATED_INTO_VALUE, Boolean.FALSE.toString());
                    }
                    EventsHelper.handleEvent(ChannelsEventCodes.EVT_TRANSACTION_DETAILS, new Object[] { accountID },
                            paramsForTransactionOccured, env);
                    /*
                     * This condition checks whether the product has Lending Feature and if it has
                     * then it will raise an Account Amendment to Send the Next Repayment Date and
                     * other details to the IFM
                     */
                    if ((Boolean) featuresAttToProduct.get(IfmConstants.HAS_LEN_FEATURE)) {
                        paramsForAcctAmendEvent.put("ACCOUNTNAME", accountBO.getF_ACCOUNTNAME());
                        paramsForAcctAmendEvent.put("ACCOUNTNO", (String) message.getPrimaryID());
                        paramsForAcctAmendEvent.put("CURRENCYCODE", message.getAcctCurrencyCode());
                        paramsForAcctAmendEvent.put("PRODUCTID", productId);
                        EventsHelper.handleEvent(ChannelsEventCodes.I_ACCOUNT_AMEND_BUSINESS_EVENT, new Object[] { accountID },
                                paramsForAcctAmendEvent, env);

                    }

                }

            }
        }
    }

}
