package com.misys.ub.swift.remittance.process;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.DBUtils.SwiftNonStpChargeTable;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dao.MessageHeaderDao;
import com.misys.ub.swift.remittance.dao.RemittanceDetailsDao;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.fatoms.UB_SWT_GenerateUETR;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.swift.InitiateSwiftMessageRs;

public class PersistRemittanceData implements Command {
    private static final transient Log LOGGER = LogFactory.getLog(PersistRemittanceData.class.getName());

    @Override
    public boolean execute(Context context) throws Exception {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("START PersistRemittanceData  ");
        boolean endofChain = Boolean.FALSE;
        String remittanceIDPK = StringUtils.EMPTY;
        String headerMessageId = StringUtils.EMPTY;
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        SwiftRemittanceRq swtRemitanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");
        InitiateSwiftMessageRs initiateSwiftMessageRsDtls = swtRemitterResp.getInitiateSwiftMessageRsDtls();
        RsHeader rsHeader = swtRemitterResp.getRsHeader();
        RemittanceDetailsDao remittanceDao = new RemittanceDetailsDao();
        String channel = swtRemitanceReq.getRqHeader().getOrig().getChannelId();

        String uetr = remittanceDto.isStp() ? getUETR(channel, PaymentSwiftConstants.MT103,swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference()): StringUtils.EMPTY;
        // open api changes
        IBOUB_INF_MessageHeader msgHeader = MessageHeaderDao
                .findByMESSAGEID2(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());

        remittanceIDPK = PaymentSwiftUtils.getRemittanceId(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode(), PaymentSwiftConstants.REMITTACNCE_OUTWARD);

        if (msgHeader != null) {
            // update the header table
            headerMessageId = msgHeader.getBoID();
            MessageHeaderDao.updateMsgHeader(msgHeader, remittanceDto, remittanceDto.getRuleId(), remittanceIDPK);
        }
        else {
            // generated header Message Id
            headerMessageId = GUIDGen.getNewGUID();
            rsHeader = MessageHeaderDao.insertMessageHeader(swtRemitanceReq, headerMessageId, remittanceDto,
                    remittanceDto.getRuleId(),
                    remittanceIDPK);
        }
 
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            swtRemitterResp = remittanceDao.insertRemittanceDetails(swtRemitanceReq, swtRemitterResp, headerMessageId,
                    remittanceIDPK, remittanceDto.isStp(), uetr, remittanceDto); 
            
            if(!remittanceDto.isStp()) {
                SwiftNonStpChargeTable nonStpCharge=new SwiftNonStpChargeTable();
                nonStpCharge.insertSwtNonStpCharge(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getCharges(), headerMessageId, !remittanceDto.isStp());
            }
        }
        
        

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
            endofChain = Boolean.TRUE;
        }

        swtRemitterResp.getInitiateSwiftMessageRsDtls().setUETR(uetr);
        swtRemitterResp.getInitiateSwiftMessageRsDtls()
                .setSenderReference(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        swtRemitterResp.getInitiateSwiftMessageRsDtls()
                .setMessageReference(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
        swtRemitterResp.getInitiateSwiftMessageRsDtls().setRemittanceId(remittanceIDPK);
        swtRemitterResp.getInitiateSwiftMessageRsDtls().setMessageId(headerMessageId);
        swtRemitterResp.setInitiateSwiftMessageRsDtls(initiateSwiftMessageRsDtls);
        swtRemitterResp.setRsHeader(rsHeader);

        context.put("swtRemitanceResp", swtRemitterResp);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("END PersistRemittanceData  ");

        return endofChain;
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
