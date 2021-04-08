package com.misys.ub.swift.remittance.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class SearchSSI implements Command {
    private static final transient Log LOGGER = LogFactory.getLog(SearchSSI.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public boolean execute(Context context) throws Exception {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN SearchSSI");

        boolean endofChain = Boolean.FALSE;

        // getting the request, response and otherRequiredProcessDtl object from the context
        SwiftRemittanceRq swtRemitanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");

        // null not checked as the txn details are mandatory
        String ssiDetailID = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSettlementInstrId();
        if (!StringUtils.isBlank(ssiDetailID)) {
            SearchSSIService searchSSI = new SearchSSIService();
            HashMap<String, Object> map = new HashMap<>();
            map.put("DETAILID", ssiDetailID);
            map.put("MessageNo", CommonConstants.INTEGER_ZERO);
            try {
                Map outputParams = MFExecuter.executeMF(MFInputOutPutKeys.FETCH_SSI_SRV, remittanceDto.getEnv(), map);
                if (null != outputParams) {
                    swtRemitanceReq = searchSSI.searchSSIFromDetailID(outputParams, swtRemitanceReq, swtRemitterResp,
                            remittanceDto);
                    if (null != swtRemitterResp.getRsHeader() && null != swtRemitterResp.getRsHeader().getStatus()
                            && PaymentSwiftConstants.ERROR_STATUS
                                    .equals(swtRemitterResp.getRsHeader().getStatus().getOverallStatus())) {
                        endofChain = true;
                    }
                }

            }
            catch (BankFusionException e) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("IN SearchSSI" + e.getLocalisedMessage());
                endofChain = Boolean.TRUE;
                RsHeader rsHeader = new RsHeader();
                MessageStatus status = new MessageStatus();
                SubCode subCode = new SubCode();
                String eventCode ;
                if (null != e.getEvents() && String.valueOf(e.getEvents().iterator().next().getEventNumber()).isEmpty()) {
                    eventCode = SwiftEventCodes.E_SETTLEMENT_INSTRUCTION_NOT_CONFIGURED_PROPERLY;
                    subCode.setDescription("searchSSI");
                }
                else {
                    eventCode = String.valueOf(e.getEvents().iterator().next().getEventNumber());
                    subCode.setDescription(e.getEvents().iterator().next().getMessage());
                }
                subCode.setCode(eventCode);
                Object parameterList ;
                for (int j = 0; j < e.getEvents().iterator().next().getDetails().length; j++) {
                    EventParameters parameter = new EventParameters();
                    parameterList = e.getEvents().iterator().next().getDetails()[j];
                    parameter.setEventParameterValue(parameterList.toString());
                    subCode.addParameters(parameter);
                }

                subCode.setFieldName(StringUtils.EMPTY);
                subCode.setSeverity(PaymentSwiftConstants.ERROR_STATUS);
                status.addCodes(subCode);
                status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
                rsHeader.setStatus(status);
                swtRemitterResp.setRsHeader(rsHeader);
            }
        }

        context.put("swtRemitanceReq", swtRemitanceReq);
        context.put("swtRemitterResp", swtRemitterResp);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("END SearchSSI");

        return endofChain;
    }

}
