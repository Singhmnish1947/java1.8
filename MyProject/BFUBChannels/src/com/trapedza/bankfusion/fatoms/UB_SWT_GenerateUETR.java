package com.trapedza.bankfusion.fatoms;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GenerateUETR;

public class UB_SWT_GenerateUETR extends AbstractUB_SWT_GenerateUETR {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param args
     */
    private static final transient Log LOGGER = LogFactory.getLog(UB_SWT_GenerateUETR.class.getName());


    public UB_SWT_GenerateUETR() {
        super();
    }

    @SuppressWarnings("deprecation")
    public UB_SWT_GenerateUETR(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {

        String txnReference = !StringUtils.isBlank(getF_IN_TxnReference()) ? getF_IN_TxnReference() : CommonConstants.EMPTY_STRING;
        String messageType = !StringUtils.isBlank(getF_IN_MessageType()) ? getF_IN_MessageType() : CommonConstants.EMPTY_STRING;
        String channel = !StringUtils.isBlank(getF_IN_Channel()) ? getF_IN_Channel() : CommonConstants.EMPTY_STRING;

        String uetr = CommonConstants.EMPTY_STRING;

        if (PaymentSwiftConstants.MT103.equals(messageType) || PaymentSwiftConstants.MT202.equals(messageType)
                || PaymentSwiftConstants.MT205.equals(messageType)) {
            uetr = UUID.randomUUID().toString();
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Txn Refernce: " + txnReference + "  channel : " + channel + " UETR: " + uetr);
        }

        setF_OUT_UETR(uetr);
    }

}
