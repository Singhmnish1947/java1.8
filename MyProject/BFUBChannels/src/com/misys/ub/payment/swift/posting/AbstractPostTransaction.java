package com.misys.ub.payment.swift.posting;

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

import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

/**
 * @author machamma.devaiah
 *
 */
public abstract class AbstractPostTransaction implements IPostTransaction {
    private static final Log LOGGER = LogFactory.getLog(AbstractPostTransaction.class.getName());

    /**
     * @param args
     */
    public RsHeader postTxn(PostingDto postingdto) {
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
                postingdto.getLocalAmountAmount() != null ? postingdto.getLocalChargeAmountAmount() : BigDecimal.ZERO);
        map.put(MFInputOutPutKeys.LocalAmountAmount_CurrCode,
                postingdto.getLocalAmountAmount_CurrCode() != null ? postingdto.getLocalAmountAmount_CurrCode()
                        : StringUtils.EMPTY);
        map.put(MFInputOutPutKeys.LocalAmountISOCurrencyCode,
                postingdto.getLocalAmountISOCurrencyCode() != null ? postingdto.getLocalAmountISOCurrencyCode()
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
        Map outputParams = MFExecuter.executeMF(MFInputOutPutKeys.UB_TXN_PostNonSTPCashTxn_SRV,
                BankFusionThreadLocal.getBankFusionEnvironment(), map);
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
}
