package com.misys.ub.swift.tellerRemittance.utils;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.cbs.common.util.DateUtil;
import com.misys.ub.payment.swift.posting.AbstractPostTransaction;
import com.misys.ub.payment.swift.posting.PostingDto;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.LocalCashDetails;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessage;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessageInput;

public class PostRemittanceMessage extends AbstractPostTransaction {
    /**
     * @param args
     */
    public RsHeader postCashTxn(TellerRemittanceRq req, RemittanceStatusDto statusDto) {
        RsHeader rsHeader = new RsHeader();
        PostingDto postingDto = new PostingDto();
        String hostTransactionId = GUIDGen.getNewGUID();
        // debit BCASH account and credit Internal Cash account for GPP configured in module config
        postingDto.setManualValueDate(DateUtil.getStaticDateForDate(SystemInformationManager.getInstance().getBFBusinessDate()));
        postingDto.setChannelID(RemittanceConstants.BRANCH_TELLER_CHANNEL_ID);
        postingDto.setBranchCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
        postingDto.setPayReceiveFlag(Boolean.FALSE);
        postingDto.setTransactionID(hostTransactionId);
        postingDto.setOriginalTxnReference(req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        PostingEngineMessageInput postingMsgInput = new PostingEngineMessageInput();
        postingMsgInput.setChannelID(BankFusionThreadLocal.getUserSession().getChannelID());
        // set empty charge list
        postingDto.setChargesList(new VectorTable());
        // prepare posting array
        postingMsgInput.setPostingEngineMessage(preparePostingArray(req, statusDto));
        postingDto.setPostingMsgInput(postingMsgInput);
        // post Cash transaction by calling posting engine
        rsHeader = postTxn(postingDto);
        return rsHeader;
    }

    /**
     * Method Description:Credit leg posting
     * 
     * @param req
     * @return
     */
    private PostingEngineMessage prepareCreditLegMessage(TellerRemittanceRq req) {
        PostingEngineMessage crLeg = new PostingEngineMessage();
        // credit Nostro account
        // credit leg
        crLeg.setPostingLegNumber(1);
        crLeg.setPostingMessageAccountId(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAccountId());
        crLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.CREDIT);
        crLeg.setPostingMessageExchangeRateType(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditExchangeRateType());
        crLeg.setPostingMessageISOCurrencyCode(req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditAmount().getIsoCurrencyCode());
        crLeg.setPostingMessageExchangeRate(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditExchangeRate());
        crLeg.setPostingMessageTransactionAmount(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount().getAmount());
        crLeg.setPostingMessageTransactionCode(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditTransactionCode());
        crLeg.setPostingMessageTransactionNarrative(
                StringUtils.isNotBlank(req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration())
                        ? req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditNarration()
                        : StringUtils.EMPTY);
        crLeg.setPostingMessageTransactionReference(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        return crLeg;
    }

    /**
     * Method Description:Debit leg posting
     * 
     * @param req
     * @return
     */
    private PostingEngineMessage prepareDebitLegMessage(TellerRemittanceRq req) {
        PostingEngineMessage drLeg = new PostingEngineMessage();
        drLeg.setPostingLegNumber(2);
        drLeg.setPostingMessageAccountId(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId());
        drLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.DEBIT);
        drLeg.setPostingMessageExchangeRateType(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRateType());
        drLeg.setPostingMessageISOCurrencyCode(req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAmount().getIsoCurrencyCode());
        drLeg.setPostingMessageExchangeRate(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRate());
        drLeg.setPostingMessageTransactionAmount(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAmount().getAmount());
        drLeg.setPostingMessageTransactionCode(req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode());
        drLeg.setPostingMessageTransactionNarrative(req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration());
        drLeg.setPostingMessageTransactionReference(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        return drLeg;
    }

    /**
     * Method Description:Prepare posting array
     * 
     * @param req
     * @param statusDto
     * @return
     */
    private PostingEngineMessage[] preparePostingArray(TellerRemittanceRq req, RemittanceStatusDto statusDto) {
        int array_size = 2;

        // local charge amount array
        if (req.getTxnAdditionalDtls().getLocalCashDetails() != null && req.getTxnAdditionalDtls().getLocalCashDetails()
                .getLclCashAmtDetails().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            array_size = array_size + 1;
        }

        BigDecimal gppChargeAmount = req.getTxnAdditionalDtls().getConsolidatedChargeAmount().getAmount();
        // TODO: Assuming gpp Charge same as debit amount currency
        if (gppChargeAmount.compareTo(BigDecimal.ZERO) != 0) {
            array_size = array_size + 1;
        }

        // credit internal cash account from module config
        req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .setCreditAccountId(statusDto.getGppInternalCashAccountId());

        PostingEngineMessage[] postingArray = new PostingEngineMessage[array_size];
        postingArray[0] = prepareCreditLegMessage(req);
        postingArray[1] = prepareDebitLegMessage(req);
        int count = 2;

        // local charge amount array
        if (req.getTxnAdditionalDtls().getLocalCashDetails() != null && req.getTxnAdditionalDtls().getLocalCashDetails()
                .getLclCashAmtDetails().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            // set local cash details
            postingArray[count] = prepareLocalChargeLegMessage(req, count);
            count++;
        }

        // charges array
        if (null != req.getTxnAdditionalDtls().getConsolidatedChargeAmount()
                && req.getTxnAdditionalDtls().getConsolidatedChargeAmount().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            postingArray[count] = prepareCreditGppChargeLegMessage(req, statusDto, count);
        }

        return postingArray;
    }

    /**
     * Method Description:Prepare local charge
     * 
     * @param req
     * @param postingDto
     * @return
     */
    private PostingEngineMessage prepareLocalChargeLegMessage(TellerRemittanceRq req, int legNumber) {
        PostingEngineMessage localCashLeg = new PostingEngineMessage();
        LocalCashDetails localCashtls = getLocalCashDetails(req);
        if (localCashtls != null) {
            localCashLeg.setPostingLegNumber(legNumber + 1);
            localCashLeg.setPostingMessageAccountId(localCashtls.getLclCashAcctDetails().getStandardAccountId());
            localCashLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.CREDIT);
            localCashLeg.setPostingMessageExchangeRateType(
                    req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRateType());
            localCashLeg.setPostingMessageISOCurrencyCode(localCashtls.getLclCashAmtDetails().getIsoCurrencyCode());
            // TODO: check the exchange rate
            localCashLeg.setPostingMessageExchangeRate(
                    req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRate());
            localCashLeg.setPostingMessageTransactionAmount(localCashtls.getLclCashAmtDetails().getAmount());
            localCashLeg.setPostingMessageTransactionCode(
                    req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode());
            localCashLeg.setPostingMessageTransactionNarrative("Local Cash Narrative");
            localCashLeg.setPostingMessageTransactionReference(
                    req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        }

        return localCashLeg;
    }

    /**
     * Method Description:Prepare local cash
     * 
     * @param req
     * @return
     */
    private LocalCashDetails getLocalCashDetails(TellerRemittanceRq req) {
        return (req != null && req.getTxnAdditionalDtls().getLocalCashDetails() != null)
                ? req.getTxnAdditionalDtls().getLocalCashDetails()
                : null;
    }

    /**
     * Method Description:Charge leg posting
     * 
     * @param req
     * @return
     */
    private PostingEngineMessage prepareCreditGppChargeLegMessage(TellerRemittanceRq req, RemittanceStatusDto statusDto,
            int legNumber) {
        PostingEngineMessage chargeLeg = new PostingEngineMessage();
        chargeLeg.setPostingLegNumber(legNumber + 1);
        chargeLeg.setPostingMessageAccountId(statusDto.getGppInternalChargeAccountId());
        chargeLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.CREDIT);
        chargeLeg.setPostingMessageExchangeRateType(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRateType());
        chargeLeg.setPostingMessageISOCurrencyCode(req.getTxnAdditionalDtls().getConsolidatedChargeAmount().getIsoCurrencyCode());
        chargeLeg.setPostingMessageExchangeRate(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRate());
        chargeLeg.setPostingMessageTransactionAmount(req.getTxnAdditionalDtls().getConsolidatedChargeAmount().getAmount());
        chargeLeg.setPostingMessageTransactionCode(statusDto.getGppMisTxnCodeForChargeAndTax());
        chargeLeg.setPostingMessageTransactionNarrative("GPP Charges");
        chargeLeg.setPostingMessageTransactionReference(
                req.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        return chargeLeg;
    }

}
