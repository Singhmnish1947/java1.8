package com.misys.ub.swift.remittance;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.RemittanceProcessPipeline;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_RemittanceProcessManager;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.InitiateSwiftMessageRs;

/**
 * @author Machamma.Devaiah Main class for the New Swift Remittance process
 */
public class RemittanceProcessManager extends AbstractUB_SWT_RemittanceProcessManager {
    private transient final static Log LOGGER = LogFactory.getLog(RemittanceProcessManager.class);
    private transient final ComplexTypeConvertor complexConverter = new ComplexTypeConvertor(this.getClass().getClassLoader());
    XmlBeanFactory factory = RemittanceBeanFactory.getFactory();

    @SuppressWarnings("deprecation")
    public RemittanceProcessManager(BankFusionEnvironment env) {
        super(env);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_RemittanceProcessManager#process(com.
     * trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    @Override
    public void process(BankFusionEnvironment env) {
        SwiftRemittanceRq swtRemitanceReq = getF_IN_SwiftRemittanceRq();
        SwiftRemittanceRs swtRemitterResp = new SwiftRemittanceRs();
        RemittanceProcessDto remittanceDto = new RemittanceProcessDto();
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN RemittanceProcessManager START ");

        if (LOGGER.isDebugEnabled()) {
            String outRsString = complexConverter.getXmlFromJava(swtRemitanceReq.getClass().getName(), swtRemitanceReq);
            LOGGER.debug("swtRemitanceReq complex type::::::: " + outRsString);
        }
        InitiateSwiftMessageRs initiateSwiftMessageRsDtls = new InitiateSwiftMessageRs();
        initiateSwiftMessageRsDtls.setSettInstrId(StringUtils.EMPTY);
        initiateSwiftMessageRsDtls.setSenderReference(StringUtils.EMPTY);
        swtRemitterResp.setInitiateSwiftMessageRsDtls(initiateSwiftMessageRsDtls);
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        status.setOverallStatus("S");
        rsHeader.setStatus(status);
        swtRemitterResp.setRsHeader(rsHeader);

        RemittanceProcessPipeline remittancePipelineBean = (RemittanceProcessPipeline) factory.getBean("remittancePipeline");
        remittancePipelineBean.setBeanFactory(factory);
        remittancePipelineBean.setUserId(BankFusionThreadLocal.getUserId());
        remittancePipelineBean.setUserLocale(BankFusionThreadLocal.getUserSession().getZone());
        remittancePipelineBean.setEnv(env);
        remittancePipelineBean.setBranchSortCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
        remittancePipelineBean.setSwtRemitanceReq(swtRemitanceReq);
        remittancePipelineBean.setSwtRemitterResp(swtRemitterResp);
        remittanceDto.setEnv(env);
        remittancePipelineBean.setRemittanceDto(remittanceDto);
        swtRemitterResp = remittancePipelineBean.runChain("remittanceChain");

        // if remittance id is empty
        if (remittanceDto.isStp() && StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getRemittanceId())
                && !swtRemitterResp.getRsHeader().getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
            MessageStatus msgStatus = swtRemitterResp.getRsHeader().getStatus();
            SubCode subCode = new SubCode();
            subCode.setCode(SwiftEventCodes.E_REM_INITITATION_FAILED);
            String errorDescription = BankFusionMessages.getInstance().getFormattedEventMessage(
                    Integer.parseInt(SwiftEventCodes.E_REM_INITITATION_FAILED), new Object[] {},
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            subCode.setDescription(errorDescription);
            subCode.setFieldName(StringUtils.EMPTY);
            subCode.setSeverity(PaymentSwiftConstants.ERROR_STATUS);
            msgStatus.addCodes(subCode);
            msgStatus.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
            swtRemitterResp.getRsHeader().setStatus(msgStatus);
        }

        printResponse(swtRemitterResp);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN RemittanceProcessManager END ");

        setF_OUT_SwiftRemittanceRs(swtRemitterResp);

    }

    /**
     * @param swtRemitterResp
     */
    private void printResponse(SwiftRemittanceRs swtRemitterResp) {
        if (null != swtRemitterResp && LOGGER.isInfoEnabled()) {
            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getSenderReference()))
                LOGGER.info("SenderReference::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getSenderReference());

            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getMessageReference()))
                LOGGER.info("MessageReference::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getMessageReference());

            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getMessageId()))
                LOGGER.info("Message Id::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getMessageId());

            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getUETR()))
                LOGGER.info("UETR::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getUETR());

            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getHostTxnId()))
                LOGGER.info("HostTxnId::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getHostTxnId());

            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getRemittanceId()))
                LOGGER.info("RemittanceId::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getRemittanceId());

            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getRemittanceStatus()))
                LOGGER.info("RemittanceStatus::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getRemittanceStatus());

            if (!StringUtils.isBlank(swtRemitterResp.getInitiateSwiftMessageRsDtls().getSettInstrId()))
                LOGGER.info("SSIId::::" + swtRemitterResp.getInitiateSwiftMessageRsDtls().getSettInstrId());

        }
    }
}
