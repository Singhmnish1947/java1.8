/* ********************************************************************************
 *  Copyright(c)2019  Finastra. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */
package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.posting.SWTPostingUtils;
import com.misys.ub.payment.swift.posting.PostingDto;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.trapedza.bankfusion.core.CommonConstants;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessage;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessageInput;

/**
 * @author machamma.devaiah
 *
 */
public class PostSwiftTranscation {
    /**
     * @param remittanceRq
     * @param remittanceDtls
     * @return
     */
    private static final transient Log LOGGER = LogFactory.getLog(PostSwiftTranscation.class.getName());

    public SwiftRemittanceRs postAccountTxn(SwiftRemittanceRq swtRemitanceReq, SwiftRemittanceRs swtRemitterResp,
            RemittanceProcessDto remittanceDto) {
        LOGGER.info("START ____PostNonStpAccountTranscation");
        RsHeader rsHeader = new RsHeader();
        PostingDto postingDto = new PostingDto();
        // post the account transaction
        postingDto.setManualValueDate(SystemInformationManager.getInstance().getBFBusinessDate());
        postingDto.setChannelID(swtRemitanceReq.getRqHeader().getOrig().getChannelId());
        postingDto.setBranchCode(remittanceDto.getBranchSortCode());
        postingDto.setPayReceiveFlag(Boolean.FALSE);
        postingDto.setTransactionID(StringUtils.EMPTY);
        postingDto.setOriginalTxnReference(StringUtils.EMPTY);
        PostingEngineMessageInput postingMsgInput = new PostingEngineMessageInput();
        postingMsgInput.setChannelID(swtRemitanceReq.getRqHeader().getOrig().getChannelId());
        postingMsgInput.setPostingEngineMessage(preparePostingArray(swtRemitanceReq, remittanceDto));
        postingDto.setPostingMsgInput(postingMsgInput);
        // get ChargeDetails
        postingDto.setChargesList(SWTPostingUtils.getChargeDetails(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getCharges()));
        // call to posting engine
        rsHeader = postTxn(postingDto, remittanceDto);
        swtRemitterResp.setRsHeader(rsHeader);
        LOGGER.info("END ____PostSwiftTranscation");
        return swtRemitterResp;
    }

    /**
     * Credit Posting Leg
     * 
     * @param remittanceRq
     * @param remittanceDtls
     * @return
     */
    private PostingEngineMessage prepareCreditLegMessage(SwiftRemittanceRq swtRemitanceReq, RemittanceProcessDto remittanceDto) {
        PostingEngineMessage crLeg = new PostingEngineMessage();
        // credit Nostro account
        // credit leg
        crLeg.setPostingLegNumber(1);
        crLeg.setPostingMessageAccountId(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditAccountId());
        crLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.CREDIT);
        crLeg.setPostingMessageExchangeRateType(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditExchangeRateType());
        crLeg.setPostingMessageISOCurrencyCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode());
        crLeg.setPostingMessageExchangeRate(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditExchangeRate());
        crLeg.setPostingMessageTransactionAmount(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditAmount().getAmount());
        crLeg.setPostingMessageTransactionCode(remittanceDto.getCreditTransactionCode());
        crLeg.setPostingMessageTransactionNarrative(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration());
        crLeg.setPostingMessageTransactionReference(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
        return crLeg;
    }

    /**
     * Debit Posting Leg
     * 
     * @param remittanceRq
     * @param remittanceDtls
     * @return
     */
    private PostingEngineMessage prepareDebitLegMessage(SwiftRemittanceRq swtRemitanceReq) {
        // debit customerAccount
        PostingEngineMessage drLeg = new PostingEngineMessage();
        String debitAmtCcy = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAmount().getIsoCurrencyCode();
        drLeg.setPostingLegNumber(2);
        // debit account
        drLeg.setPostingMessageAccountId(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId());
        drLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.DEBIT);
        // cheque number
        drLeg.setPostingMessageExchangeRateType(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getDebitPostingDtls().getDebitExchangeRateType());
        drLeg.setPostingMessageISOCurrencyCode(debitAmtCcy);
        drLeg.setPostingMessageExchangeRate(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getDebitPostingDtls().getDebitExchangeRate());
        // debit amount
        drLeg.setPostingMessageTransactionAmount(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getDebitPostingDtls().getDebitAmount().getAmount());
        drLeg.setPostingMessageTransactionCode(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode());
        drLeg.setPostingMessageTransactionNarrative(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration());
        drLeg.setPostingMessageTransactionReference(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
        return drLeg;
    }

    /**
     * @param remittanceRq
     * @param remittanceDtls
     * @return
     */
    private PostingEngineMessage[] preparePostingArray(SwiftRemittanceRq swtRemitanceReq, RemittanceProcessDto remittanceDto) {
        BigDecimal differenceAmount = BigDecimal.ZERO;
        String debitAccountCcy = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAmount().getIsoCurrencyCode();
        String creditAccountCcy = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditAmount().getIsoCurrencyCode();
        String instructedAmtCcy = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount()
                .getIsoCurrencyCode();
        int array_size = 2;

        if (debitAccountCcy.equals(creditAccountCcy) && !instructedAmtCcy.equals(debitAccountCcy)) {
            BigDecimal debitAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                    .getDebitAmount().getAmount();
            BigDecimal creditAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                    .getCreditAmount().getAmount();
            BigDecimal ubChargeAmount = remittanceDto.getUbChargeAmt();
            differenceAmount = debitAmount.subtract(creditAmount.add(ubChargeAmount));
            if (differenceAmount.compareTo(BigDecimal.ZERO) != 0) {
                array_size = 3;
            }
        }
        PostingEngineMessage[] postingArray = new PostingEngineMessage[array_size];

        postingArray[0] = prepareCreditLegMessage(swtRemitanceReq, remittanceDto);
        postingArray[1] = prepareDebitLegMessage(swtRemitanceReq);
        // if debitAccountCcy and creditAccountCcy are same and instructedAmtCcy is different
        if (debitAccountCcy.equals(creditAccountCcy) && !instructedAmtCcy.equals(debitAccountCcy)) {
            if (differenceAmount.compareTo(BigDecimal.ZERO) > 0) {
                postingArray[2] = prepareSuspenseLegMessageCredit(swtRemitanceReq, remittanceDto, differenceAmount);
            }
            else if (differenceAmount.compareTo(BigDecimal.ZERO) < 0) {
                postingArray[2] = prepareSuspenseLegMessageDebit(swtRemitanceReq, remittanceDto, differenceAmount);
            }
        }
        return postingArray;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.misys.ub.payment.swift.posting.AbstractPostTransaction#postTxn(com.misys.ub.payment.swift
     * .posting.PostingDto)
     */
    public RsHeader postTxn(PostingDto postingdto, RemittanceProcessDto remittanceDto) {
        HashMap<String, Object> map = new HashMap<>();
        RsHeader rsHeader = new RsHeader();
        map.put(MFInputOutPutKeys.PostingEngineMessageInput, postingdto.getPostingMsgInput());
        map.put(MFInputOutPutKeys.branchCode, BankFusionThreadLocal.getUserSession().getBranchSortCode());
        map.put(MFInputOutPutKeys.channelID, postingdto.getChannelID());
        map.put(MFInputOutPutKeys.originalTxnReference, postingdto.getOriginalTxnReference());
        map.put(MFInputOutPutKeys.PayReceiveFlag, postingdto.isPayReceiveFlag());
        map.put(MFInputOutPutKeys.manualValueDate, postingdto.getManualValueDate());
        map.put(MFInputOutPutKeys.postChargeSeparate, postingdto.isPostChargeSeparate());
        map.put(MFInputOutPutKeys.ChargesList, postingdto.getChargesList());
        // local amount details
        map.put(MFInputOutPutKeys.LocalAmountAccountID,
                postingdto.getLocalAmountAccountID() != null ? postingdto.getLocalAmountAccountID() : StringUtils.EMPTY);
        map.put(MFInputOutPutKeys.LocalAmountAmount,
                postingdto.getLocalAmountAmount() != null ? postingdto.getLocalAmountAmount() : BigDecimal.ZERO);
        map.put(MFInputOutPutKeys.LocalAmountAmount_CurrCode,
                postingdto.getLocalAmountAmount_CurrCode() != null ? postingdto.getLocalAmountAmount_CurrCode()
                        : StringUtils.EMPTY);
        map.put(MFInputOutPutKeys.LocalAmountISOCurrencyCode,
                postingdto.getLocalAmountISOCurrencyCode() != null ? postingdto.getLocalAmountISOCurrencyCode()
                        : StringUtils.EMPTY);
        map.put(MFInputOutPutKeys.postingMessageTransactionCode_1,
                postingdto.getLocalAmountTransactionCode() != null ? postingdto.getLocalAmountTransactionCode()
                        : StringUtils.EMPTY);
        map.put(MFInputOutPutKeys.LocalAmountTransactionNarrative,
                postingdto.getLocalAmountTransactionNarrative() != null ? postingdto.getLocalAmountTransactionNarrative()
                        : StringUtils.EMPTY);
        // local charge amount details
        map.put(MFInputOutPutKeys.LocalChargeAmountAccountID,
                postingdto.getLocalChargeAmountAccountID() != null ? postingdto.getLocalChargeAmountAccountID()
                        : StringUtils.EMPTY);
        map.put(MFInputOutPutKeys.LocalChargeAmountAmount,
                postingdto.getLocalChargeAmountAmount() != null ? postingdto.getLocalChargeAmountAmount() : BigDecimal.ZERO);
        map.put(MFInputOutPutKeys.LocalChargeAmountAmount_CurrCode,
                postingdto.getLocalChargeAmountAmount_CurrCode() != null ? postingdto.getLocalChargeAmountAmount_CurrCode()
                        : StringUtils.EMPTY);
        map.put(MFInputOutPutKeys.LocalChargeAmountISOCurrecny,
                postingdto.getLocalChargeAmountISOCurrecny() != null ? postingdto.getLocalChargeAmountISOCurrecny()
                        : StringUtils.EMPTY);
        // generate host reference
        map.put(MFInputOutPutKeys.transactionID_mfKey, postingdto.getTransactionID());
        Map outputParams = MFExecuter.executeMF(MFInputOutPutKeys.UB_TXN_PostNonSTPCashTxn_SRV, remittanceDto.getEnv(), map);
        if (outputParams != null) {
            rsHeader = (RsHeader) outputParams.get("RsHeader");
            if (StringUtils.isBlank(outputParams.get("TransactionID").toString())) {
                MessageStatus status = rsHeader.getStatus();
                SubCode subCode = new SubCode();
                subCode.setCode(PaymentSwiftConstants.EVT_POSTING_FAILED);
                subCode.setDescription(CommonConstants.EMPTY_STRING);
                subCode.setFieldName(CommonConstants.EMPTY_STRING);
                subCode.setSeverity(PaymentSwiftConstants.ERROR_STATUS);
                status.addCodes(subCode);
                status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
                rsHeader.setStatus(status);
            }
            else {
                rsHeader.setOrigCtxtId(outputParams.get("TransactionID").toString());
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Transcationid ::::" + rsHeader.getOrigCtxtId());
            }
        }
        postingdto.setRsHeader(rsHeader);
        return postingdto.getRsHeader();
    }

    /**
     * ExtraAmount Posting Leg when debitAccountCcy and creditAccountCcy are same and
     * instructedAmtCcy is different
     * 
     * @param remittanceRq
     * @param remittanceDtls
     * @return
     */
    private PostingEngineMessage prepareSuspenseLegMessageCredit(SwiftRemittanceRq swtRemitanceReq,
            RemittanceProcessDto remittanceDto,
            BigDecimal differenceAmount) {
        PostingEngineMessage crLeg = new PostingEngineMessage();
        String branchCode = remittanceDto.getEnv().getUserSession().getBranchSortCode();
        // suspense account from module config
        String psuedonymName = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.CHANNELID_SWIFT,
                PaymentSwiftConstants.SUSPENSE_ACCT_REMITTANCE);
        String suspenseAcctId = PaymentSwiftUtils
                .retrievePsuedonymAcctId(
                        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                                .getDebitAmount().getIsoCurrencyCode(),
                        branchCode, PaymentSwiftConstants.CURRENCY_PSEDONYM_CONTEXT, psuedonymName);

        if (differenceAmount.compareTo(BigDecimal.ZERO) > 0) {
            crLeg.setPostingLegNumber(3);
            crLeg.setPostingMessageAccountId(suspenseAcctId);
            crLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.CREDIT);
            crLeg.setPostingMessageExchangeRateType(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditExchangeRateType());
            crLeg.setPostingMessageISOCurrencyCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode());
            crLeg.setPostingMessageExchangeRate(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditExchangeRate());
            crLeg.setPostingMessageTransactionAmount(differenceAmount);
            crLeg.setPostingMessageTransactionCode(remittanceDto.getCreditTransactionCode());
            crLeg.setPostingMessageTransactionNarrative(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration());
            crLeg.setPostingMessageTransactionReference(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
        }

        return crLeg;
    }

    /**
     * ExtraAmount Posting Leg when debitAccountCcy and creditAccountCcy are same and
     * instructedAmtCcy is different
     * 
     * @param remittanceRq
     * @param remittanceDtls
     * @return
     */
    private PostingEngineMessage prepareSuspenseLegMessageDebit(SwiftRemittanceRq swtRemitanceReq,
            RemittanceProcessDto remittanceDto, BigDecimal differenceAmount) {
        PostingEngineMessage dbLeg = new PostingEngineMessage();
        String branchCode = remittanceDto.getEnv().getUserSession().getBranchSortCode();
        // suspense account from module config
        String psuedonymName = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.CHANNELID_SWIFT,
                PaymentSwiftConstants.SUSPENSE_ACCT_REMITTANCE);
        String suspenseAcctId = PaymentSwiftUtils
                .retrievePsuedonymAcctId(
                        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                                .getDebitAmount().getIsoCurrencyCode(),
                        branchCode, PaymentSwiftConstants.CURRENCY_PSEDONYM_CONTEXT, psuedonymName);

        if (differenceAmount.compareTo(BigDecimal.ZERO) < 0) {
            dbLeg.setPostingLegNumber(3);
            dbLeg.setPostingMessageAccountId(suspenseAcctId);
            dbLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.DEBIT);
            dbLeg.setPostingMessageExchangeRateType(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditExchangeRateType());
            dbLeg.setPostingMessageISOCurrencyCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode());
            dbLeg.setPostingMessageExchangeRate(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditExchangeRate());
            dbLeg.setPostingMessageTransactionAmount(differenceAmount.abs());
            dbLeg.setPostingMessageTransactionCode(remittanceDto.getCreditTransactionCode());
            dbLeg.setPostingMessageTransactionNarrative(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration());
            dbLeg.setPostingMessageTransactionReference(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
        }

        return dbLeg;
    }
}
