package com.misys.ub.swift.remittance.process;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

public class RemittancePosting implements Command {
    private static final transient Log LOGGER = LogFactory.getLog(RemittancePosting.class.getName());
    private transient final ComplexTypeConvertor complexConverter = new ComplexTypeConvertor(this.getClass().getClassLoader());

    @SuppressWarnings("unchecked")
    @Override
    public boolean execute(Context context) throws Exception {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN RemittancePosting");

        boolean endofChain = Boolean.FALSE;
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        SwiftRemittanceRq swtRemitanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");

        if (LOGGER.isDebugEnabled()) {
            String outRsString = complexConverter.getXmlFromJava(swtRemitanceReq.getClass().getName(), swtRemitanceReq);
            LOGGER.debug("In Posting:::::: " + outRsString);
        }

        if (remittanceDto.isStp()) {
            // TODO: Posting
            swtRemitterResp = postAccountTransaction(swtRemitanceReq, swtRemitterResp, remittanceDto);

        }
        else {
            // block the transcation
            swtRemitterResp = blockAccountTransaction(swtRemitanceReq, swtRemitterResp, remittanceDto);
        }

        if (swtRemitterResp.getRsHeader() != null && swtRemitterResp.getRsHeader().getStatus() != null) {
            MessageStatus txnLogStatus = swtRemitterResp.getRsHeader().getStatus();
            if (txnLogStatus != null && txnLogStatus.getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
                endofChain = Boolean.TRUE;
            }
        }
        context.put("swtRemitterResp", swtRemitterResp);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("END RemittancePosting");

        return endofChain;
    }

    /**
     * @param outwardRq
     * @return
     */
    private SwiftRemittanceRs blockAccountTransaction(SwiftRemittanceRq swtRemitanceReq, SwiftRemittanceRs swtRemitterResp,
            RemittanceProcessDto remittanceDto) {
        BlockSwiftTransactions blockTxn = new BlockSwiftTransactions();
        RsHeader rsHeader = swtRemitterResp.getRsHeader();
        rsHeader = blockTxn.createBlockingAmount(swtRemitanceReq, remittanceDto);
        swtRemitterResp.getInitiateSwiftMessageRsDtls().setHostTxnId(rsHeader.getOrigCtxtId());
        swtRemitterResp.setRsHeader(rsHeader);
        return swtRemitterResp;
    }

    /**
     * @param outwardRq
     * @return
     */
    private SwiftRemittanceRs postAccountTransaction(SwiftRemittanceRq swtRemitanceReq, SwiftRemittanceRs swtRemitterResp,
            RemittanceProcessDto remittanceDto) {

        PostSwiftTranscation postAcctTxn = new PostSwiftTranscation();
        swtRemitterResp = postAcctTxn.postAccountTxn(swtRemitanceReq, swtRemitterResp, remittanceDto);
        RsHeader rsHeader = swtRemitterResp.getRsHeader();
        swtRemitterResp.getInitiateSwiftMessageRsDtls().setHostTxnId(rsHeader.getOrigCtxtId());
        swtRemitterResp.setRsHeader(rsHeader);
        return swtRemitterResp;
    }

}
