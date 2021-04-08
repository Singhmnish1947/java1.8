package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.posting.PostCashTransaction;
import com.misys.ub.payment.swift.DBUtils.MessageHeaderTable;
import com.misys.ub.payment.swift.DBUtils.RemittanceDetailsTable;
import com.misys.ub.payment.swift.DBUtils.SwiftNonStpChargeTable;
import com.misys.ub.payment.swift.utils.BlockNonStpTransactions;
import com.misys.ub.payment.swift.utils.CheckNonStpSWIFTModuleConfig;
import com.misys.ub.payment.swift.utils.CheckNonStpSWIFTOutwardRules;
import com.misys.ub.payment.swift.utils.IValidateBasicCheck;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.payment.swift.utils.ValidateCurrencyCode;
import com.misys.ub.payment.swift.utils.ValidateCustomerNumber;
import com.misys.ub.payment.swift.utils.ValidationAccountStatus;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_OutwardRemittance;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRs;
import bf.com.misys.cbs.msgs.v1r0.SwftAdditionalDtls;
import bf.com.misys.cbs.msgs.v1r0.SwtOutputParams;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

public class UB_SWT_OutwardRemittance extends AbstractUB_SWT_OutwardRemittance {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private RsHeader rsHeader = new RsHeader();
    private OutwardSwtRemittanceRq outwardRq = new OutwardSwtRemittanceRq();
    /**
     * @param args
     */
    private static final transient Log LOGGER = LogFactory.getLog(UB_SWT_OutwardRemittance.class.getName());
    private transient Map<String, Object> nonSTPOut = new HashMap<>();

    public UB_SWT_OutwardRemittance() {
        super();
    }

    @SuppressWarnings("deprecation")
    public UB_SWT_OutwardRemittance(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.SUCCESS);
        rsHeader.setStatus(status);
        outwardRq = getF_IN_OutwardSwtRemittanceRq();
        String channel = outwardRq.getRqHeader().getOrig().getChannelId();
        OutwardSwtRemittanceRs outwardRs = new OutwardSwtRemittanceRs();
        MessageHeaderTable headerTable = new MessageHeaderTable();
        RemittanceDetailsTable remittanceTable = new RemittanceDetailsTable();
        SwiftNonStpChargeTable nonStpCharge = new SwiftNonStpChargeTable();
        SwtOutputParams outputParams = new SwtOutputParams();
        Boolean isNonStp = isNonStpModConfig();
        String hostReference = GUIDGen.getNewGUID();
        String uetr = StringUtils.EMPTY;
        // generated header Message Id
        String headerMessageId = GUIDGen.getNewGUID();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("START UB_SWT_OutwardRemittance" + channel + " UETR::::" + uetr);
        }
        rsHeader = validateCustomerDetails(outwardRq, rsHeader);
        isNonStp = isNonStpRule(isNonStp);
        outwardRq.getIntlPmtInputRq().getTxnInputData().setOriginalHostTxnRef(hostReference);
        if (isNonStp) {
            // posting or blocking based on TransactionType
            LOGGER.info(":::::::::::BEFORE POSTING OR BLOCKING:::::::::::");
            rsHeader = postingBasedOnTxnType(outwardRq);
            LOGGER.info(":::::::::AFTER POSTING OR BLOCKING:::::::::::::");
        }
        else {
            uetr = getUETR(channel, PaymentSwiftConstants.MT103,
                    outwardRq.getIntlPmtInputRq().getIntlPmtDetails().getPmtReference());
        }
        // Fix added for FBPY-2752
        String ruleId = (String) (nonSTPOut.get("NonSTPRule") == null ? CommonConstants.EMPTY_STRING : nonSTPOut.get("NonSTPRule"));
        String remittanceIDPK = PaymentSwiftUtils.getRemittanceId(
                outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode(),
                PaymentSwiftConstants.REMITTACNCE_OUTWARD);
        headerTable.insertMessageHeader(outwardRq, headerMessageId, isNonStp, ruleId, remittanceIDPK);
        remittanceTable.insertRemittanceDetails(outwardRq, headerMessageId, remittanceIDPK, isNonStp, uetr);
        if (outwardRq.getIntlPmtInputRq().getCharges() != null && isNonStp) {
            nonStpCharge.insertSwtNonStpCharge(outwardRq.getIntlPmtInputRq().getCharges(), headerMessageId, isNonStp);
        }
        outputParams.setIsNonStp(isNonStp);
        outputParams.setTransactionId(getTransactionId(channel, headerMessageId, hostReference));
        outputParams.setUetr(uetr);

        outwardRs.setOutputParams(outputParams);
        outwardRs.setRsHeader(rsHeader);
        setF_OUT_OutwardSwtRemittanceRs(outwardRs);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("END UB_SWT_OutwardRemittance" + hostReference);
        }
    }

    /**
     * @return
     */
    private Boolean isNonStpModConfig() {
        CheckNonStpSWIFTModuleConfig checkOutwardModConfig = new CheckNonStpSWIFTModuleConfig();
        return checkOutwardModConfig.checkNonStpModuleConfig(outwardRq.getRqHeader().getOrig().getChannelId());
    }

    /**
     * @param nonStp
     * @return
     */
    private Boolean isNonStpRule(Boolean nonStp) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Start isNonStpRule::::" + nonStp);
        }
        Boolean nonSTPRule = Boolean.FALSE;
        if (nonStp) {
            CheckNonStpSWIFTOutwardRules checkOutwardRules = new CheckNonStpSWIFTOutwardRules();
            nonSTPOut = checkOutwardRules.checkNonSTPRules(outwardRq);
            if (nonSTPOut != null && nonSTPOut.size() != 0) {
                nonSTPRule = (Boolean) nonSTPOut.get("NonSTPStatus");
            }
            else {
                nonSTPRule = Boolean.FALSE;
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("END isNonStpRule::::" + nonStp);
        }
        return (nonStp && !nonSTPRule);
    }

    /**
     * @param outwardRq
     * @return
     */
    private RsHeader blockAccountTransaction(OutwardSwtRemittanceRq outwardRq) {
        BlockNonStpTransactions blockTxn = new BlockNonStpTransactions();
        rsHeader = blockTxn.createBlockingAmount(outwardRq);
        return rsHeader;
    }

    /**
     * @param outwardRq
     * @return
     */
    private RsHeader postCashTransaction(OutwardSwtRemittanceRq outwardRq) {
        PostCashTransaction postMsg = new PostCashTransaction();
        rsHeader = postMsg.postTransaction(outwardRq);
        return rsHeader;
    }

    /**
     * @param outwardRq
     * @return
     */
    private RsHeader postingBasedOnTxnType(OutwardSwtRemittanceRq outwardRq) {
        SwftAdditionalDtls swtAddtlDtls = outwardRq.getSwftAdditionalDetails();
        outwardRq.setSwftAdditionalDetails(getTxnCodeBasedOnChannel(outwardRq));
        if (swtAddtlDtls.isIsCashTxn()) {
            // if intpay cash transaction
            rsHeader = postCashTransaction(outwardRq);
        }
        else {
            // if intapy account transaction
            rsHeader = blockAccountTransaction(outwardRq);
        }
        return rsHeader;
    }

    /**
     * @param outwardRq
     * @return
     */
    private SwftAdditionalDtls getTxnCodeBasedOnChannel(OutwardSwtRemittanceRq outwardRq) {
        if (outwardRq.getRqHeader().getOrig().getChannelId().equals(PaymentSwiftConstants.CHANNELID_TELLER)) {
            outwardRq.getSwftAdditionalDetails().setCreditTxnCode(outwardRq.getIntlPmtInputRq().getTxnInputData().getHostTxnCode());
            outwardRq.getSwftAdditionalDetails().setDebitTxnCode(outwardRq.getIntlPmtInputRq().getTxnInputData().getHostTxnCode());
        }
        return outwardRq.getSwftAdditionalDetails();
    }

    /**
     * @param outwardRq
     */
    private RsHeader validateCustomerDetails(OutwardSwtRemittanceRq outwardRq, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Start validateCustomerDetails");
        }
        IValidateBasicCheck validate;
        validate = new ValidationAccountStatus();
        rsHeader = validate.validate(outwardRq, rsHeader);
        validate = new ValidateCustomerNumber();
        rsHeader = validate.validate(outwardRq, rsHeader);
        validate = new ValidateCurrencyCode();
        rsHeader = validate.validate(outwardRq, rsHeader);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("END  validateCustomerDetails");
        }
        return rsHeader;
    }

    /**
     * Method Description: IBI/CCI will look up for "headerMessageId" in their respective flow
     * 
     * @param channel
     * @param headerMessageId
     * @param hostReference
     * @return
     */
    private String getTransactionId(String channel, String headerMessageId, String hostReference) {
        String transactionId = headerMessageId;
        if (channel.equalsIgnoreCase(PaymentSwiftConstants.CHANNELID_TELLER)) {
            transactionId = hostReference;
        }
        return transactionId;
    }

    /**
     * @param channelId
     * @param messageType
     * @param txnReference
     * @return
     */
    public String getUETR(String channelId, String messageType, String txnReference) {
        UB_SWT_GenerateUETR uetrFatom = new UB_SWT_GenerateUETR();
        uetrFatom.setF_IN_Channel(channelId);
        uetrFatom.setF_IN_MessageType(messageType);
        uetrFatom.setF_IN_TxnReference(txnReference);
        uetrFatom.process(BankFusionThreadLocal.getBankFusionEnvironment());
        return uetrFatom.getF_OUT_UETR();
    }
}
