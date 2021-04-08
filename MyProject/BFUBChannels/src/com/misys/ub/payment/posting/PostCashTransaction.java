package com.misys.ub.payment.posting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.core.CommonConstants;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.Posting;
import bf.com.misys.cbs.types.Pseudonym;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

/**
 * @author machamma.devaiah
 *
 */
public class PostCashTransaction {
    /**
     * @param args
     */
    private static final Log LOGGER = LogFactory.getLog(PostCashTransaction.class.getName());

    /**
     * @param outwardRq
     * @return
     */
    public RsHeader postTransaction(OutwardSwtRemittanceRq outwardRq) {
        HashMap<String, Object> map = new HashMap<>();
        Posting postingArr = new Posting();
        RsHeader rsHeader = new RsHeader();
        map.put(MFInputOutPutKeys.postingMessageAccountId_1,
                outwardRq.getIntlPmtInputRq().getFundingPosting().getAccount().getStandardAccountId());
        // D Bcash in fund posting leg
        map.put(MFInputOutPutKeys.postingMessageAccountPostingAction_1,
                outwardRq.getIntlPmtInputRq().getFundingPosting().getPostingAction());
        map.put(MFInputOutPutKeys.postingMessageExchangeRateType_1,
                outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType());
        map.put(MFInputOutPutKeys.postingMessageISOCurrencyCode_1,
                outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode());
        map.put(MFInputOutPutKeys.postingMessageTransactionAmount_1,
                outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getAmount());
        map.put(MFInputOutPutKeys.postingMessageTransactionAmount_CurrCode_1,
                outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode());
        // Dleg hostTxnCode
        map.put(MFInputOutPutKeys.postingMessageTransactionCode_1, outwardRq.getSwftAdditionalDetails().getDebitTxnCode());
        // padd narrative 3 and 4
        map.put(MFInputOutPutKeys.postingMessageTransactionNarrative_1, getCreditNarrative(outwardRq));
        map.put(MFInputOutPutKeys.postingMessageTransactionReference_1,
                outwardRq.getIntlPmtInputRq().getTxnInputData().getTellerTxnReference());
        // C internal acccoount from module config payment posting leg
        String txnCurrencyCode = outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode();
        postingArr = getSuspenseAccount(txnCurrencyCode,
                outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getAmount());
        map.put(MFInputOutPutKeys.postingMessageAccountId_2, postingArr.getAccount().getStandardAccountId());
        map.put(MFInputOutPutKeys.postingMessageAccountPostingAction_2,
                outwardRq.getIntlPmtInputRq().getPaymentPosting().getPostingAction());
        map.put(MFInputOutPutKeys.postingMessageISOCurrencyCode_2, postingArr.getCurrency().getIsoCurrencyCode());
        map.put(MFInputOutPutKeys.postingMessageTransactionAmount_2, postingArr.getCurrency().getAmount());
        map.put(MFInputOutPutKeys.postingMessageTransactionAmount_CurrCode_2, postingArr.getCurrency().getIsoCurrencyCode());
        map.put(MFInputOutPutKeys.postingMessageTransactionCode_2, outwardRq.getSwftAdditionalDetails().getCreditTxnCode());
        map.put(MFInputOutPutKeys.postingMessageTransactionNarrative_2, getDebitNarrative(outwardRq));
        map.put(MFInputOutPutKeys.postingMessageTransactionReference_2,
                outwardRq.getIntlPmtInputRq().getTxnInputData().getTellerTxnReference());
        map.put(MFInputOutPutKeys.branchCode, BankFusionThreadLocal.getUserSession().getBranchSortCode());
        map.put(MFInputOutPutKeys.channelID, outwardRq.getRqHeader().getOrig().getChannelId());
        map.put(MFInputOutPutKeys.compensate, outwardRq.getIntlPmtInputRq().getTxnInputData().getCompensate());
        map.put(MFInputOutPutKeys.originalTxnReference, outwardRq.getIntlPmtInputRq().getTxnInputData().getOriginalTxnRef());
        map.put(MFInputOutPutKeys.PayReceiveFlag,
                outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLocalCashDetails().getLclCashReceived());
        map.put(MFInputOutPutKeys.manualValueDate, outwardRq.getIntlPmtInputRq().getPaymentPosting().getValueDate());
        map.put(MFInputOutPutKeys.postChargeSeparate, outwardRq.getIntlPmtInputRq().getTxnInputData().getPostChargeSep());
        map.put(MFInputOutPutKeys.transactionID_mfKey, outwardRq.getIntlPmtInputRq().getTxnInputData().getOriginalHostTxnRef());
        Map outputParams = MFExecuter.executeMF("UB_TXN_PostNonSTPCashTxn_SRV", BankFusionThreadLocal.getBankFusionEnvironment(),
                map);
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
        return rsHeader;
    }

    /**
     * @param outwardRq
     * @return
     */
    private String getDebitNarrative(OutwardSwtRemittanceRq outwardRq) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine1())) {
            sb.append(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine1());
        }
        if (!StringUtils.isEmpty(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine2())) {
            sb.append(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine2());
        }
        return !sb.toString().isEmpty() ? sb.toString() : StringUtils.EMPTY;
    }

    /**
     * @param outwardRq
     * @return
     */
    private String getCreditNarrative(OutwardSwtRemittanceRq outwardRq) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine3())) {
            sb.append(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine3());
        }
        if (!StringUtils.isEmpty(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine4())) {
            sb.append(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine4());
        }
        return !sb.toString().isEmpty() ? sb.toString() : StringUtils.EMPTY;
    }

    /**
     * @param txnCurrencyCode
     * @param fundPostingAmt
     * @return
     */
    private Posting getSuspenseAccount(String txnCurrencyCode, BigDecimal fundPostingAmt) {
        Posting payPosting = new Posting();
        // consider bcash account currency for deriving the internal account pseudonym
        String branchCode = BankFusionThreadLocal.getUserSession().getBranchSortCode();
        String suspenseAcctId = SWTPostingUtils.getSuspenseAccountFromModuleConfig(txnCurrencyCode, branchCode);
        Pseudonym pseudonym = new Pseudonym();
        Currency currency = new Currency();
        AccountKeys accountKeys = new AccountKeys();
        InputAccount inputAccount = new InputAccount();
        if (payPosting.getAccount() == null) {
            payPosting.setAccount(new AccountKeys());
        }
        if (!StringUtils.isBlank(suspenseAcctId)) {
            // credit internal account from module configuration
            if (accountKeys.getStandardAccountId() == null) {
                accountKeys.setStandardAccountId(suspenseAcctId);
            }
            inputAccount.setInputAccountId(suspenseAcctId);
            inputAccount.setAccountFormatType("ST");
            accountKeys.setInputAccount(inputAccount);
        }
        accountKeys.setPseudonym(pseudonym);
        payPosting.setAccount(accountKeys);
        currency.setAmount(fundPostingAmt);
        currency.setIsoCurrencyCode(txnCurrencyCode);
        payPosting.setCurrency(currency);
        return payPosting;
    }
}
