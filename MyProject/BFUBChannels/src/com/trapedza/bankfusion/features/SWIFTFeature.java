/* ***********************************************************************************
 * Copyright (c) 2003,2004 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: SWIFTFeature.java,v 1.12 2008/10/25 04:13:29 vipesha Exp $
 *
 * $Log: SWIFTFeature.java,v $
 * Revision 1.12  2008/10/25 04:13:29  vipesha
 * bug 13132
 *
 * Revision 1.2  2008/10/17 09:05:48  iteshk
 * Updated for Error no.09465
 *
 * Revision 1.1  2008/10/17 08:51:47  iteshk
 * Fixed for 13132
 *
 * Revision 1.1  2008/09/05 01:04:55  saurabhg
 * aa raha hoo
 *
 * Revision 1.8.4.10  2008/07/29 21:32:26  zubink
 * fix for bug 9634
 *
 * Revision 1.8.4.9  2008/07/23 20:29:15  ravir
 * Now sending the original Buy Amount to SWIFT instead of FX Deal Buy Amount
 *
 * Revision 1.8.4.8  2008/07/21 16:40:34  amardeepc
 * Fix for bug 9577.
 *
 * Revision 1.8.4.7  2008/07/03 17:56:17  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.8  2008/06/26 18:09:19  arun
 * Merged with 3-3B after Code freeze
 *
 * Revision 1.8.4.6  2008/06/18 19:44:45  vipesha
 * changed for bug no 10654
 *
 * Revision 1.8.4.4  2008/06/05 16:52:02  saurabhg
 * Disposal ID have been added for reversal for swift
 *
 * Revision 1.8.4.3  2008/06/04 13:21:14  saurabhg
 * Pay_Receive_Flag have been removed for swift
 *
 * Revision 1.8.4.2  2008/05/30 20:34:27  saurabhg
 * Checked in from FX-WIP to 3.3b
 *
 * Revision 1.8  2008/05/21 17:51:37  saurabhg
 * Updated for option take down msg generation
 *
 * Revision 1.4  2008/05/17 15:44:45  saurabhg
 * Changes for settlementment
 *
 * Revision 1.3  2008/04/26 14:40:17  ravir
 * Fix for Bug Id 9631 & 9632
 *
 * Revision 1.1  2008/04/02 15:00:24  rajeevana
 * Checked in to 3.3b WIP
 *
 * Revision 1.3  2008/03/19 15:04:48  ravir
 * Calling Invoke Message Generator
 *
 * Revision 1.8  2008/03/10 15:04:37  mallikarjunas
 * FX Sprint 8 - Bug Fixes
 *
 * Revision 1.26  2008/03/01 09:03:36  ravir
 * Fix for BugID 8423
 *
 * Revision 1.25  2008/02/26 06:23:50  zubink
 * changes for swap deal settlement and reversal - the child deal settleing wwill now call up a posting for the parent also
 *
 * Revision 1.24  2008/02/15 04:29:58  ravir
 * While Deal reversal Contract Amount & Transaction amount are swapped as per suggested by Swift module
 *
 * Revision 1.23  2008/02/13 10:36:44  ravir
 * Calling Message generator for Deal Reversal
 *
 * Revision 1.22  2008/02/13 10:29:13  ravir
 * ValueDate & Maturity Dates values are swapped as per suggested by Swift Module
 *
 * Revision 1.21  2008/02/12 06:38:09  ravir
 * swapped the Buy and Sell amount.
 *
 * Revision 1.20  2008/02/09 11:04:54  ravir
 * For FX-Swift Integration. Now Sending Spot/Forward both exchange Rates
 *
 * Revision 1.19  2008/02/08 14:38:16  ravir
 * Code added for Deal Reversal
 *
 * Revision 1.18  2008/02/08 10:54:31  ravir
 * Updated for FX-Swift integration.Changed Value Date to OptionStartorEndDate
 *
 * Revision 1.17  2008/02/07 12:15:09  ravir
 * Added Verify_Flag as parameter. As suggested by SWIFT team
 *
 * Revision 1.16  2008/02/07 08:03:28  ravir
 * Changed  Sell currency Account to Main Account and Buy Currency Account to Contra Account. As suggested by SWIFT team
 *
 * Revision 1.15  2008/01/31 09:25:08  zubink
 * moved N&A Code
 *
 * Revision 1.14  2008/01/25 18:24:49  ahilandeswaris
 * Option Take Down fix
 *
 * Revision 1.13  2008/01/25 15:08:17  mallikarjunas
 * Fix for 7034
 *
 * Revision 1.12  2008/01/24 17:36:05  ahilandeswaris
 * Option deal amendment for amounts
 *
 * Revision 1.11  2008/01/22 08:38:40  zubink
 * SWIFT
 *
 * Revision 1.10  2008/01/21 17:50:52  zubink
 * for ravi
 *
 * Revision 1.9  2008/01/21 14:47:30  mallikarjunas
 * Changes for SWAP
 *
 * Revision 1.8  2008/01/18 12:18:47  ravir
 * For FX-Swift integration
 *
 * Revision 1.7  2008/01/14 18:37:39  mallikarjunas
 * Changes for SWIFT integration
 *
 * Revision 1.6  2008/01/14 15:06:21  mallikarjunas
 * Modified to use correct param names for invoking SWIFT Processes
 *
 * Revision 1.5  2008/01/11 06:56:23  ravir
 * Message Type changed from 103 to 300
 *
 * Revision 1.4  2008/01/10 15:14:38  mallikarjunas
 * Resolved compilation errors
 *
 * Revision 1.2  2008/01/07 13:20:07  zubink
 * merging this with code already in WIP
 *
 * Revision 1.1  2008/01/07 13:04:30  ravir
 * Java class for FX-SWIFT integration
 *
 * Revision 1.2  2007/07/09 14:51:09  zubink
 * Cleaned up header comments.
 *
 * Revision 1.1  2007/07/09 14:45:52  zubink
 * Forex Module: the swift feature is both account based and deal based. On a Forex Posting it should (if present on the product) identify the details needed for sending a swift message of a certain type and call a swift process. There is no functionality as of now as this will be taken up by the SWIFT team.
 *
 */
package com.trapedza.bankfusion.features;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.forex.core.ForexConstants;
import com.misys.ub.forex.core.ForexHelper;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOFXPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOForexDeals;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTSettlementInstructionDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.refimpl.AbstractSWIFTFeature;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerHolder;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.systeminformation.PostingMessageConstants;

import bf.com.misys.cbs.types.events.Event;

/**
 * The Class SWIFTFeature.
 *
 * @AUTHOR Zubin Kavarana
 * @PROJECT FX
 */
public class SWIFTFeature extends AbstractSWIFTFeature {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /** The logger instance for this feature. */
    private transient final static Log logger = LogFactory.getLog(SWIFTFeature.class.getName());

    private static String MessageType = CommonConstants.EMPTY_STRING;

    private static final String Originate_Deal = "NEW";

    private static final String Cancel_Deal = "CANCEL";

    private static final String Amend_Deal = "AMEND";

    private static String Code_Word = null;

    private static final String Verify_Flag = String.valueOf(ForexConstants.STATUS_AUTHORISED);

    private static final String settlementInstructionDetailQuery = "where " + IBOSWTSettlementInstructionDetail.DETAILID + " = ? ";

    private static IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory
            .getInstance().getServiceManager().getServiceForName(
                         IBusinessEventsService.SERVICE_NAME);

    private static IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
            .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);;
    // private static final String transactionCodeWhereClause = "where " +
    // IBOMisTransactionCodes.CODE + " = ?";

    /**
     * Default Constructor - will use a null BankFusionEnvironment.
     */
    public SWIFTFeature() {
        super(null);
    }

    /**
     * Constructor.
     *
     * @param env
     *            the env
     */
    public SWIFTFeature(BankFusionEnvironment env) {
        super(env);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.trapedza.bankfusion.features.refimpl.AbstractSWIFTFeature#process(com.trapedza.bankfusion
     * .commands.core.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.trapedza.bankfusion.features.AccountBasedFeature#registerWithUpdateLoggerManager(com.
     * trapedza.bankfusion.core.UpdateAuditLoggerManager)
     */
    public void registerWithUpdateLoggerManager(UpdateAuditLoggerManager manager) {
        if (!manager.isTransactionLoggingEnabled()) {
            return;
        }
        super.registerWithUpdateLoggerManager(manager);
        manager.addNewUpdateHolder(new UpdateAuditLoggerHolder(SWIFTFeature.class.getName(), svnRevision));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.trapedza.bankfusion.features.AbstractFeature#postingEngineUpdate(com.trapedza.bankfusion
     * .postingengine.messages.IPostingMessage)
     */
    public void postingEngineUpdate(IPostingMessage message) {
        if (message.getMessageType() == PostingMessageConstants.FINANCIAL_POSTING_MESSAGE) {
            processSWIFTforAccount(message,false);
        }
        if (message.getMessageType() == PostingMessageConstants.FOREX_DEAL_MESSAGE) {
            processSWIFTforForexDeal((IBOFXPostingMessage) message);
        }
    }

    private void processSWIFTforAccount(IPostingMessage message,   boolean productHasSWIFTSettings ) {

        String productID = message.getProductID();
        // hard coded stub - basically we need to get swift settings for this product
        boolean productIsAccountBased = true;

        if (productHasSWIFTSettings) {
            if (productID.equals("fixdep")) {
                // send 320 - get that from produvt swift settings
            }
            else if (productID.equals("fixloan")) {
                // send 330 - get that from produvt swift settings
            }
            ArrayList params = new ArrayList();
            params.add(message.getTransCode());
            IBOMisTransactionCodes misTransactionCodes = null;
            /*
             * misTransactionCodes = (IBOMisTransactionCodes)
             * env.getFactory().findFirstByQuery(IBOMisTransactionCodes.BONAME,
             * transactionCodeWhereClause, params);
             */
            // Using the Cache of TransactionScreenControl Table for fetching the details.
            MISTransactionCodeDetails mistransDetails;
            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo()).getMisTransactionCodeDetails(message
                    .getTransCode());

            misTransactionCodes = mistransDetails.getMisTransactionCodes();

            if (misTransactionCodes.getF_SWTDRCRCONFIRMATION().equalsIgnoreCase("0")
                    || misTransactionCodes.getF_SWTDRCRCONFIRMATION().equalsIgnoreCase("1")
                    || misTransactionCodes.getF_SWTDRCRCONFIRMATION().equalsIgnoreCase("2")) {

                IBOCustomer customerItem = (IBOCustomer) FinderMethods.findCustomerByAccount(message.getPrimaryID(), env, null)
                        .get(0);
                validateCustomer(customerItem);

                if (message.getSign() == '-') {
                    // call 900
                    MessageType = "900";
                }
                else {
                    // call 910
                    MessageType = "910";
                }
            }
        }
    }

    private void processSWIFTforForexDeal(IBOFXPostingMessage fxmessage) {
        if (fxmessage.getF_DEALCODE() == ForexConstants.SWAP_MODE || fxmessage.getF_DEALCODE() == ForexConstants.ARBITRAGE_MODE) {
            return;
        }

        if (fxmessage.isF_BUYSETTLEMENT() || fxmessage.isF_SELLSETTLEMENT()) {
            if (fxmessage.getF_DEALCODE() != 3) {
                processSWIFTforForexDealSettlement(fxmessage);
            }
        }
        else if (fxmessage.isF_AMENDMENT()) {
            processSWIFTforForexDealAmendment(fxmessage);
        }
        else if (fxmessage.isF_OPTIONTAKEDOWN()) {
            processSWIFTforForexOptionDealTakeDown(fxmessage);
        }
        else if (fxmessage.isReversal()) {
            String fxDealId = fxmessage.getF_DEALID();
            IBOForexDeals forexDeal = ForexHelper.getForexDeal(fxDealId, env);
            if (forexDeal.getF_DEALSTATUS() != ForexConstants.STATUS_ENTERED) {
                processSWIFTforForexDealReversal(fxmessage);
            }
        }
        else {
            /**
             * This feature will be called if deal is in single stage deal origination or in the
             * second stage of multistage deal origination if the deal is in stage1 of multistage
             * deal origination ,the call will be skipped
             */
            switch (fxmessage.getF_ORIGINATIONSTAGE()) {
                case ForexConstants.DEAL_ORIG_SINGLE_STAGE:
                case ForexConstants.DEAL_ORIG_MULTI_STAGE_STAGE2:
                    processSWIFTforForexDealOrigination(fxmessage);
                    break;
                case ForexConstants.DEAL_ORIG_MULTI_STAGE_STAGE1:
                    break;
            }
        }
    }

    private void processSWIFTforForexDealOrigination(IBOFXPostingMessage fxmessage) {
        // ORIGINATION
        if (fxmessage.getF_SETTLEMENTROUTER().equalsIgnoreCase(FeatureIDs.SWIFTFTR)) {
            // This is a forex deal origination
            // 0. only if this is swift customer

            IBOForexDeals fxDeal = ForexHelper.getForexDeal(fxmessage.getF_DEALID(), env);

            IBOCustomer customerItem = (IBOCustomer) FinderMethods.findCustomerByDealProduct(fxmessage.getPrimaryID(), env, null)
                    .get(0);
            IBOSwtCustomerDetail swtCust = validateCustomer(customerItem);
            // 1. get the data
            String settlementInstructionID = CommonConstants.EMPTY_STRING;
            String debitAccountID = CommonConstants.EMPTY_STRING;
            BigDecimal exchangeRate = CommonConstants.BIGDECIMAL_ZERO;
            int dealCode = fxmessage.getF_DEALCODE();
            String creditAccountID = fxmessage.getF_CREDITACCOUNTID();
            settlementInstructionID = fxmessage.getF_SETTLEMENTINSTRUCTIONID();
            debitAccountID = fxmessage.getF_DEBITACCOUNTID();
            exchangeRate = fxmessage.getF_EXCHANGERATE();
            ArrayList params = new ArrayList();
            params.add(settlementInstructionID);
            IBOSWTSettlementInstructionDetail settlementInstr = (IBOSWTSettlementInstructionDetail) env.getFactory().findByQuery(
                    IBOSWTSettlementInstructionDetail.BONAME, settlementInstructionDetailQuery, params, null).get(0);

            // 2. get module settings

            // 3. call swift
            Code_Word = Originate_Deal;
            MessageType = "300";
            Map results = callSWIFTInterfaces_DealOrigination(fxmessage, debitAccountID, creditAccountID, settlementInstr
                    .getF_MESSAGE_NUMBER(), Code_Word, exchangeRate, MessageType);
            String swiftMessageLinkID = (String) results.get("DisposalID");
            String swiftDealReference = (String) results.get("DealNumber");
            if (swiftMessageLinkID == null || swiftMessageLinkID.trim().length() == 0) {
                // Throws an BankFusionException if the BIC Code is not attached to the customer and
                // The Credit Account customer
                // is not swift enabled.
                if (swtCust != null && swtCust.getF_BICCODE().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                    IBOCustomer crAccCustomer = (IBOCustomer) FinderMethods.findCustomerByAccount(creditAccountID, env, null)
                            .get(0);
                    /* changes done for artf1055842 against the changes
                     * done for artf1015817
                     */
                    if (validateCustomer(crAccCustomer) == null){
                    	EventsHelper.handleEvent(ChannelsEventCodes.E_BIC_NOT_AVAILABLE_UB16, new Object[] {}, new HashMap(),
        						env);
                	}

                }
                // throw new BankFusionException(9419, BankFusionMessages.getFormattedMessage(9419,
                // new Object[] {}));
               /* EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_PUBLISHING_THE_MESSAGE, new Object[] {}, new HashMap(),
                        env);*/
             // artf986422- Changing error message according to SWIFT2012
				// enhancements.


            }

            int messageStatus = Integer.parseInt((String) results.get("Status_Flag"));

            fxDeal.setF_SETTLEMENTINSTRUCTIONSID(swiftMessageLinkID);
            fxDeal.setF_SETTLEMENTROUTERID(FeatureIDs.SWIFTFTR);

            /* Call Swift Message Generator */
            if (messageStatus == 0) {
                callMessageGenerator(swiftDealReference, swiftMessageLinkID);
            }
            ForexHelper.updateForexDeal(fxmessage.getF_DEALID(), fxDeal, env);
        }
    }

    private void processSWIFTforForexDealReversal(IBOFXPostingMessage fxmessage) {
        // get message type
        IBOForexDeals fxDeal = ForexHelper.getForexDeal(fxmessage.getF_DEALID(), env);
        // 0. only if this is swift customer
        if (fxDeal.getF_SETTLEMENTROUTERID().equalsIgnoreCase(FeatureIDs.SWIFTFTR)) {
            String settlementInstructionID = CommonConstants.EMPTY_STRING;
            String debitAccountID = CommonConstants.EMPTY_STRING;
            String creditAccountID = CommonConstants.EMPTY_STRING;
            settlementInstructionID = fxDeal.getF_SETTLEMENTINSTRUCTIONSID();
            creditAccountID = fxDeal.getF_CREDITACCOUNTID();
            debitAccountID = fxDeal.getF_SETTLEMENTACCOUNTID();
            Code_Word = Cancel_Deal;
            MessageType = "300";

            Map result = callSWIFTInterfaces_DealReversal(fxDeal, debitAccountID, creditAccountID, Code_Word, MessageType);
            String swiftMessageLinkID = (String) result.get("DisposalID");
            String dealReference = (String) result.get("DealNumber");
            if (swiftMessageLinkID == null) {
                swiftMessageLinkID = CommonConstants.EMPTY_STRING;
            }

            int messageStatus = Integer.parseInt((String) result.get("Status_Flag"));

            fxDeal.setF_SETTLEMENTINSTRUCTIONSID(swiftMessageLinkID);
            fxDeal.setF_SETTLEMENTROUTERID(FeatureIDs.SWIFTFTR);

            /* Call Swift Message Generator */
            if (messageStatus == 0) {
                callMessageGenerator(dealReference, swiftMessageLinkID);
            }
            ForexHelper.updateForexDeal(fxmessage.getF_DEALID(), fxDeal, env);
        }
    }

    private void processSWIFTforForexDealAmendment(IBOFXPostingMessage fxmessage) {
        return;
    }

    private void processSWIFTforForexDealSettlement(IBOFXPostingMessage fxmessage) {

        IBOForexDeals fxDealDetails = ForexHelper.getForexDeal(fxmessage.getF_DEALID(), env);

        if (fxDealDetails.getF_SETTLEMENTROUTERID().equalsIgnoreCase(FeatureIDs.SWIFTFTR)) {
            String disposalID = fxDealDetails.getF_SETTLEMENTINSTRUCTIONSID();
            Code_Word = Originate_Deal;
            if (fxmessage.isF_BUYSETTLEMENT()) {
                IBOCustomer customerItem = (IBOCustomer) FinderMethods.findCustomerByAccount(
                        fxDealDetails.getF_SETTLEMENTACCOUNTID(), env, null).get(0);
                validateCustomer(customerItem);
                MessageType = "900"; // send 900
                Map result = callSWIFTInterfaces_DealSettlement(fxDealDetails, fxDealDetails.getF_SETTLEMENTACCOUNTID(),
                        fxDealDetails.getF_CREDITACCOUNTID(), Code_Word, MessageType, disposalID);
                String swiftMessageLinkID = (String) result.get("DisposalID");
                String dealReference = (String) result.get("DealNumber");
                if (swiftMessageLinkID == null) {
                    swiftMessageLinkID = CommonConstants.EMPTY_STRING;
                }

                int messageStatus = Integer.parseInt((String) result.get("Status_Flag"));

                if (messageStatus == 0) {
                    callMessageGenerator(dealReference, swiftMessageLinkID);
                }

            }
            if (fxmessage.isF_SELLSETTLEMENT()) {
                IBOCustomer customerItem = (IBOCustomer) FinderMethods.findCustomerByAccount(fxDealDetails.getF_CREDITACCOUNTID(),
                        env, null).get(0);
                validateCustomer(customerItem);
                MessageType = "910"; // send 910
                Map result = callSWIFTInterfaces_DealSettlement(fxDealDetails, fxDealDetails.getF_SETTLEMENTACCOUNTID(),
                        fxDealDetails.getF_CREDITACCOUNTID(), Code_Word, MessageType, disposalID);
                String swiftMessageLinkID = (String) result.get("DisposalID");
                String dealReference = (String) result.get("DealNumber");
                if (swiftMessageLinkID == null) {
                    swiftMessageLinkID = CommonConstants.EMPTY_STRING;
                }

                int messageStatus = Integer.parseInt((String) result.get("Status_Flag"));

                if (messageStatus == 0) {
                    callMessageGenerator(dealReference, swiftMessageLinkID);
                }
            }
        }
    }

    private void processSWIFTforForexOptionDealTakeDown(IBOFXPostingMessage fxmessage) {

        IBOForexDeals parentDeal = (IBOForexDeals) FinderMethods.findForexDealsByDealReference(fxmessage.getTransactionRef(),
                new Integer(ForexConstants.OPTIONS_MODE), env, null).get(0);
        if (parentDeal.getF_SETTLEMENTROUTERID().equalsIgnoreCase(FeatureIDs.SWIFTFTR)) {
            // This is a forex deal origination
            // 0. only if this is swift customer
            IBOForexDeals fxDeal = ForexHelper.getForexDeal(fxmessage.getF_DEALID(), env);

            IBOCustomer customerItem = (IBOCustomer) FinderMethods
                    .findCustomerByDealProduct(fxDeal.getF_DEALPRODUCTID(), env, null).get(0);
            validateCustomer(customerItem);
            // 1. get the data
            String settlementInstructionID = CommonConstants.EMPTY_STRING;
            String debitAccountID = CommonConstants.EMPTY_STRING;
            BigDecimal exchangeRate = CommonConstants.BIGDECIMAL_ZERO;
            int dealCode = fxmessage.getF_DEALCODE();
            String creditAccountID = fxmessage.getF_CREDITACCOUNTID();
            if (fxDeal.getF_SETTLEMENTINSTRUCTIONSID() != null
                    && !fxDeal.getF_SETTLEMENTINSTRUCTIONSID().equals(CommonConstants.EMPTY_STRING)) {
                Code_Word = Amend_Deal;
            }
            else {
                Code_Word = Originate_Deal;
            }

            MessageType = "300";

            settlementInstructionID = fxmessage.getF_SETTLEMENTINSTRUCTIONID();
            debitAccountID = fxmessage.getF_DEBITACCOUNTID();
            exchangeRate = fxmessage.getF_EXCHANGERATE();

            ArrayList params = new ArrayList();
            params.add(settlementInstructionID);
            IBOSWTSettlementInstructionDetail settlementInstr = null;
            try {
                settlementInstr = (IBOSWTSettlementInstructionDetail) env.getFactory().findByQuery(
                        IBOSWTSettlementInstructionDetail.BONAME, settlementInstructionDetailQuery, params, null).get(0);
            }
            catch (BankFusionException exception) {
                Event validateEvent = new Event();
                validateEvent.setEventNumber(ChannelsEventCodes.E_SETTLEMENT_INSTRUCTION_NOT_CONFIGURED_PROPERLY);
                businessEventsService.handleEvent(validateEvent);
            }
            // 2. get module settings
            try{
            Map results = callSWIFTInterfaces_OptionDealTakeDown(fxmessage, parentDeal, debitAccountID, creditAccountID,
						(settlementInstr != null) ? settlementInstr.getF_MESSAGE_NUMBER() : null, Code_Word, exchangeRate, MessageType);
            String swiftMessageLinkID = (String) results.get("DisposalID");
            String swiftDealReference = (String) results.get("DealNumber");
            if (swiftMessageLinkID == null) {
                swiftMessageLinkID = CommonConstants.EMPTY_STRING;
            }

            int messageStatus = Integer.parseInt((String) results.get("Status_Flag"));

            fxDeal.setF_SETTLEMENTINSTRUCTIONSID(swiftMessageLinkID);
            fxDeal.setF_SETTLEMENTROUTERID(FeatureIDs.SWIFTFTR);

            /* Call Swift Message Generator */
            if (messageStatus == 0) {
                callMessageGenerator(swiftDealReference, swiftMessageLinkID);
            }
            ForexHelper.updateForexDeal(fxmessage.getF_DEALID(), fxDeal, env);
        }catch (Exception e){
        	logger.error(ExceptionUtil.getExceptionAsString(e));
        }
            
        }
    }

    private Map callSWIFTInterfaces_DealOrigination(IBOFXPostingMessage fxmessage, String debitAccountID, String creditAccountID,
            int messageNumber, String Code_Word, BigDecimal exchangeRate, String MessageType) {

        String dealProductID = fxmessage.getPrimaryID();
        String customerNumber = null;
        Integer FXTransaction = CommonConstants.INTEGER_ZERO;
        List customerNumberList = FinderMethods.findCustomerByDealProduct(dealProductID, env, null);
        Date MaturityDate = SystemInformationManager.getInstance().getBFSystemDate();
        Date Value_Date = SystemInformationManager.getInstance().getBFSystemDate();
        IBOCustomer customerDetail = (IBOCustomer) customerNumberList.get(0);
        customerNumber = customerDetail.getBoID();
        if (fxmessage.getF_DEALCODE() == 3) {
            FXTransaction = new Integer(2);
            MaturityDate = fxmessage.getF_OPTIONENDORSELLMATURITYDATE();
            Value_Date = fxmessage.getF_OPTIONSTARTORBUYMATURITYDATE();
        }
        else {

            FXTransaction = new Integer(1);
            MaturityDate = fxmessage.getF_OPTIONSTARTORBUYMATURITYDATE();
            Value_Date = fxmessage.getF_OPTIONENDORSELLMATURITYDATE();
        }

        Map inputParams = new HashMap();
        // fix for FBEC-71234 forwardport of FBEC-52191
        BFCurrencyValue buyAmount = new BFCurrencyValue(fxmessage.getF_BUYCURRENCYCODE(), fxmessage.getF_BUYAMOUNT(), null);
	    BigDecimal roundedBuyAmount=buyAmount.getRoundedAmount();	
        BFCurrencyValue sellAmount = new BFCurrencyValue(fxmessage.getF_SELLCURRENCYCODE(), fxmessage.getF_SELLAMOUNT(), null);
	    BigDecimal roundedSellAmount=sellAmount.getRoundedAmount();
        inputParams.put("Broker", fxmessage.getF_BROKERID());
        inputParams.put("ChargeAmount", fxmessage.getF_CHARGES());
        inputParams.put("ChargeCurrency", fxmessage.getF_CHARGECURRENCYCODE());
        inputParams.put("Code_Word", Code_Word);
        inputParams.put("Contra_Account", debitAccountID);
        inputParams.put("Contract_Amount", roundedSellAmount);
        inputParams.put("Customer_Number", customerNumber);
        inputParams.put("Deal_Number", fxmessage.getF_TRANSACTIONREF());
        inputParams.put("Interest_Rate", exchangeRate);
        inputParams.put("FXTransaction", FXTransaction);
        inputParams.put("Main_account", creditAccountID);
        inputParams.put("Maturity_Date", MaturityDate);
        inputParams.put("MessageType", MessageType);
        inputParams.put("Post_Date", fxmessage.getF_DEALSTARTDATE());
        inputParams.put("PurchaseCurrency", fxmessage.getF_BUYCURRENCYCODE());
        inputParams.put("RelatedDealNumber", fxmessage.getF_TRANSACTIONREF());
        inputParams.put("SoldCurrency", fxmessage.getF_SELLCURRENCYCODE());
        inputParams.put("Settl_Instruction_Number", new Integer(messageNumber));
        inputParams.put("Transaction_Amount", roundedBuyAmount);
        inputParams.put("Value_Date", Value_Date);
        inputParams.put("Verify_Flag", Verify_Flag);

        String swiftDealOriginationBPID = (String) ubInformationService.getBizInfo().getModuleConfigurationValue("FEX", "SWIFTDEALORIGINATIONBPID", env);
        return MFExecuter.executeMF(swiftDealOriginationBPID.trim(), env, inputParams);
    }

    private Map callSWIFTInterfaces_DealReversal(IBOForexDeals fxDeals, String debitAccountID, String creditAccountID,
            String Code_Word, String MessageType) {
        String dealProductID = fxDeals.getF_DEALPRODUCTID();
        String customerNumber = null;
        Integer FXTransaction = CommonConstants.INTEGER_ZERO;
        List customerNumberList = FinderMethods.findCustomerByDealProduct(dealProductID, env, null);

        IBOCustomer customerDetail = (IBOCustomer) customerNumberList.get(0);
        customerNumber = customerDetail.getBoID();

        if (fxDeals.getF_DEALCODE() == 3) {
            FXTransaction = new Integer(2);
        }
        else {

            FXTransaction = new Integer(1);
        }

        Map inputParams = new HashMap();
        inputParams.put("Broker", fxDeals.getF_BROKERID());
        inputParams.put("ChargeAmount", fxDeals.getF_CHARGES());
        inputParams.put("ChargeCurrency", fxDeals.getF_CHARGECURRENCYCODE());
        inputParams.put("Code_Word", Code_Word);
        inputParams.put("Contra_Account", debitAccountID);
        inputParams.put("Contract_Amount", fxDeals.getF_SELLAMOUNT());
        inputParams.put("Customer_Number", customerNumber);
        inputParams.put("Deal_Number", fxDeals.getF_DEALREFERENCE());
        inputParams.put("DisposalID", fxDeals.getF_SETTLEMENTINSTRUCTIONSID());
        inputParams.put("Interest_Rate", fxDeals.getF_EXCHANGERATE());
        inputParams.put("FXTransaction", FXTransaction);
        inputParams.put("Main_account", creditAccountID);
        inputParams.put("Maturity_Date", fxDeals.getF_OPTIONSTARTORBUYMATURITYDATE());
        inputParams.put("MessageType", MessageType);
        inputParams.put("Post_Date", fxDeals.getF_DEALSTARTDATE());
        inputParams.put("PurchaseCurrency", fxDeals.getF_BUYCURRENCYCODE());
        inputParams.put("RelatedDealNumber", fxDeals.getF_DEALREFERENCE());
        inputParams.put("SoldCurrency", fxDeals.getF_SELLCURRENCYCODE());
        inputParams.put("Settl_Instruction_Number", CommonConstants.INTEGER_ZERO);
        inputParams.put("Transaction_Amount", fxDeals.getF_BUYAMOUNT());
        inputParams.put("Value_Date", fxDeals.getF_OPTIONENDORSELLMATURITYDATE());
        inputParams.put("Verify_Flag", Verify_Flag);

        String swiftDealOriginationBPID = (String) ubInformationService.getBizInfo()
                .getModuleConfigurationValue("FEX", "SWIFTDEALORIGINATIONBPID", env);

        return MFExecuter.executeMF(swiftDealOriginationBPID.trim(), env, inputParams);
    }

    private Map callSWIFTInterfaces_OptionDealTakeDown(IBOFXPostingMessage fxmessage, IBOForexDeals parentDeal,
            String debitAccountID, String creditAccountID, int messageNumber, String Code_Word, BigDecimal exchangeRate,
            String MessageType) {

        BigDecimal newBuyAmount = parentDeal.getF_BUYAMOUNT();
        BigDecimal newSellAmount = parentDeal.getF_SELLAMOUNT();

        String dealProductID = fxmessage.getPrimaryID();
        String customerNumber = null;
        Integer FXTransaction = CommonConstants.INTEGER_ZERO;
        List customerNumberList = FinderMethods.findCustomerByDealProduct(dealProductID, env, null);
        Date MaturityDate = SystemInformationManager.getInstance().getBFSystemDate();
        Date Value_Date = SystemInformationManager.getInstance().getBFSystemDate();
        IBOCustomer customerDetail = (IBOCustomer) customerNumberList.get(0);
        customerNumber = customerDetail.getBoID();
        if (fxmessage.getF_DEALCODE() == 3) {
            FXTransaction = new Integer(2);
            MaturityDate = parentDeal.getF_OPTIONENDORSELLMATURITYDATE();
            Value_Date = parentDeal.getF_OPTIONSTARTORBUYMATURITYDATE();
        }
        else {

            FXTransaction = new Integer(1);
            MaturityDate = parentDeal.getF_OPTIONSTARTORBUYMATURITYDATE();
            Value_Date = parentDeal.getF_OPTIONENDORSELLMATURITYDATE();
        }

        Map inputParams = new HashMap();
        inputParams.put("Broker", fxmessage.getF_BROKERID());
        inputParams.put("ChargeAmount", newBuyAmount);
        inputParams.put("ChargeCurrency", fxmessage.getF_CHARGECURRENCYCODE());
        inputParams.put("Code_Word", Code_Word);
        inputParams.put("Contra_Account", debitAccountID);
        inputParams.put("Contract_Amount", newSellAmount);
        inputParams.put("Customer_Number", customerNumber);
        inputParams.put("Deal_Number", fxmessage.getF_TRANSACTIONREF());
        inputParams.put("Interest_Rate", exchangeRate);
        inputParams.put("FXTransaction", FXTransaction);
        inputParams.put("Main_account", creditAccountID);
        inputParams.put("Maturity_Date", MaturityDate);
        inputParams.put("MessageType", MessageType);
        inputParams.put("Post_Date", fxmessage.getF_DEALSTARTDATE());
        inputParams.put("PurchaseCurrency", fxmessage.getF_BUYCURRENCYCODE());
        inputParams.put("RelatedDealNumber", fxmessage.getF_TRANSACTIONREF());
        inputParams.put("SoldCurrency", fxmessage.getF_SELLCURRENCYCODE());
        inputParams.put("Settl_Instruction_Number", new Integer(messageNumber));
        inputParams.put("Transaction_Amount", newBuyAmount);
        inputParams.put("Value_Date", Value_Date);
        inputParams.put("Verify_Flag", Verify_Flag);

        String swiftDealOriginationBPID = (String) ubInformationService.getBizInfo()
                .getModuleConfigurationValue("FEX", "SWIFTDEALORIGINATIONBPID", env);
        return MFExecuter.executeMF(swiftDealOriginationBPID.trim(), env, inputParams);
    }

    private Map callSWIFTInterfaces_DealSettlement(IBOForexDeals fxDeals, String debitAccountID, String creditAccountID,
            String Code_Word, String MessageType, String disposalID) {
        String dealProductID = fxDeals.getF_DEALPRODUCTID();
        String customerNumber = null;
        Integer FXTransaction = CommonConstants.INTEGER_ZERO;
        List customerNumberList = FinderMethods.findCustomerByDealProduct(dealProductID, env, null);
        BFCurrencyValue bfCurrencyTransactionAmount = new BFCurrencyValue(fxDeals.getF_BUYCURRENCYCODE(), fxDeals.getF_BUYAMOUNT(),
                null);
        BigDecimal transactionAmount = bfCurrencyTransactionAmount.getRoundedAmount();

        BFCurrencyValue bfCurrencyContractAmount = new BFCurrencyValue(fxDeals.getF_SELLCURRENCYCODE(), fxDeals.getF_SELLAMOUNT(),
                null);
        BigDecimal contractAmount = bfCurrencyContractAmount.getRoundedAmount();

        IBOCustomer customerDetail = (IBOCustomer) customerNumberList.get(0);
        customerNumber = customerDetail.getBoID();

        if (fxDeals.getF_DEALCODE() == 3) {
            FXTransaction = new Integer(2);
        }
        else {

            FXTransaction = new Integer(1);
        }

        Map inputParams = new HashMap();
        inputParams.put("Broker", fxDeals.getF_BROKERID());
        inputParams.put("ChargeAmount", fxDeals.getF_CHARGES());
        inputParams.put("ChargeCurrency", fxDeals.getF_CHARGECURRENCYCODE());
        inputParams.put("Code_Word", Code_Word);
        inputParams.put("Contra_Account", debitAccountID);
        inputParams.put("Contract_Amount", contractAmount);
        inputParams.put("Customer_Number", customerNumber);
        inputParams.put("Deal_Number", fxDeals.getF_DEALREFERENCE());
        inputParams.put("Interest_Rate", fxDeals.getF_EXCHANGERATE());
        inputParams.put("FXTransaction", FXTransaction);
        inputParams.put("Main_account", creditAccountID);
        inputParams.put("Maturity_Date", fxDeals.getF_OPTIONSTARTORBUYMATURITYDATE());
        inputParams.put("MessageType", MessageType);
        inputParams.put("Post_Date", fxDeals.getF_OPTIONENDORSELLMATURITYDATE());
        inputParams.put("PurchaseCurrency", fxDeals.getF_BUYCURRENCYCODE());
        inputParams.put("RelatedDealNumber", fxDeals.getF_DEALREFERENCE());
        inputParams.put("SoldCurrency", fxDeals.getF_SELLCURRENCYCODE());
        inputParams.put("Settl_Instruction_Number", CommonConstants.INTEGER_ZERO);
        inputParams.put("Transaction_Amount", transactionAmount);
        inputParams.put("Value_Date", fxDeals.getF_OPTIONENDORSELLMATURITYDATE());
        inputParams.put("DisposalID", disposalID);
        inputParams.put("Verify_Flag", Verify_Flag);

        String swiftDealOriginationBPID = (String) ubInformationService.getBizInfo()
                .getModuleConfigurationValue("FEX", "SWIFTDEALORIGINATIONBPID", env);

        return MFExecuter.executeMF(swiftDealOriginationBPID.trim(), env, inputParams);
    }

    private void callMessageGenerator(String dealReferenceNumber, String swiftDisposalId) {

        Map params = new HashMap();
        params.put("DealNumber", dealReferenceNumber);
        params.put("swiftDisposalId", swiftDisposalId);
        String swiftMessageOrigination = (String) ubInformationService.getBizInfo()
                .getModuleConfigurationValue("FEX", "SWIFTMESSAGEORIGINATIONBPID", env);

        MFExecuter.executeMF(swiftMessageOrigination.trim(), env, params);
    }

    private IBOSwtCustomerDetail validateCustomer(IBOCustomer customerItem) {
        try {
            return (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME, customerItem.getBoID());
        }
        catch (BankFusionException exception) {
            /*
             * EventsHelper.handleEvent(7064, BankFusionMessages.ERROR_LEVEL, new Object[] {
             * customerItem.getBoID() }, new HashMap(), getEnv());
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_THE_CUSTOMER_DOES_NOT_HAVE_A_SWIFT_SETUP, new Object[] { customerItem
                    .getBoID() }, new HashMap(), getEnv());
            return null;
        }
    }

}
