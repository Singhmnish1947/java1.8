package com.misys.ub.swift.remittance.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.fbe.common.constant.QueryConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class MessageHeaderDao {

    private transient final static Log logger = LogFactory.getLog(MessageHeaderDao.class.getName());

    private static final String QUERY_TO_FIND_USING_MESSAGE_NUMBER = QueryConstants.WHERE + IBOUB_INF_MessageHeader.MESSAGEID2
            + QueryConstants.EQUALS_PARAM;

    public static RsHeader insertMessageHeader(SwiftRemittanceRq swtRemitanceReq, String messageId,
            RemittanceProcessDto remittanceDto,
            String ruleId, String remittanceId) {
        RsHeader rsHeader = new RsHeader();
        MessageStatus txnStatus = new MessageStatus();
        txnStatus.setOverallStatus(PaymentSwiftConstants.SUCCESS);
        rsHeader.setStatus(txnStatus);
        if (logger.isInfoEnabled())
            logger.info("Start of insertMessageHeader");
        try {

            // normal flow create
            IBOUB_INF_MessageHeader messageHeader = (IBOUB_INF_MessageHeader) remittanceDto.getEnv().getFactory()
                    .getStatelessNewInstance(IBOUB_INF_MessageHeader.BONAME);
            messageHeader.setBoID(messageId);
            String channel = swtRemitanceReq.getRqHeader().getOrig().getChannelId();
            // save the entire message.
            messageHeader.setF_DATAMESSAGE(serializeComplexTypeToByteArray(swtRemitanceReq));
            messageHeader
                    .setF_MESSAGEID2(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
            messageHeader.setF_MESSAGETYPE(PaymentSwiftConstants.IPAY);
            messageHeader.setF_CHANNELID(channel);
            if (remittanceDto.isStp()) {
                messageHeader.setF_MESSAGESTATUS(PaymentSwiftConstants.PROCESSED);
                messageHeader.setF_REMITTANCEID(remittanceId);
            }
            else {
                messageHeader.setF_MESSAGESTATUS(PaymentSwiftConstants.REMITTER_WAIT);
            }
            messageHeader.setF_RULEID(ruleId);
            messageHeader.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
            messageHeader.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
            // Search was not working in "Message Information Console", so changing the
            // direction
            messageHeader.setF_DIRECTION(PaymentSwiftConstants.INWARD);
            // Storing the "REFERENCE" received from different channels: Teller/IBI/CCI
            messageHeader.setF_REFERENCE(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
            remittanceDto.getEnv().getFactory().create(IBOUB_INF_MessageHeader.BONAME, messageHeader);

        }
        catch (BankFusionException e) {
            logger.info("Error Message during insertion/update into INFTB_MESSAGEHEADER" + e);
            SubCode subCode = new SubCode();
            int error = 20600092;
            String errorCode = Integer.toString(error);
            EventParameters parameter = new EventParameters();
            parameter.setEventParameterValue("INFTB_MESSAGEHEADER");
            subCode.addParameters(parameter);
            subCode.setCode(errorCode);
            subCode.setDescription(e.getEvents().iterator().next().getMessage());
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            txnStatus.addCodes(subCode);
            txnStatus.setOverallStatus("E");
            rsHeader.setStatus(txnStatus);
        }
        if (logger.isInfoEnabled())
            logger.info("End of insertMessageHeader");

        return rsHeader;
    }

    /**
     * @param outwardMsg
     * @return
     */
    private static byte[] serializeComplexTypeToByteArray(SwiftRemittanceRq swtRemitanceReq) {
        return BankFusionIOSupport.convertToBytes(swtRemitanceReq);
    }

    /**
     * Method Description:Find by the MessageId2. This is the paymentInfoId from openApi
     * 
     * @param messageNumber
     * @return
     */
    public static IBOUB_INF_MessageHeader findByMESSAGEID2(String messageNumber) {

        ArrayList params = new ArrayList();
        params.add(messageNumber);
        List result = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOUB_INF_MessageHeader.BONAME,
                QUERY_TO_FIND_USING_MESSAGE_NUMBER, params, null, true);

        return CommonUtil.checkIfNotNullOrEmpty(result) ? (IBOUB_INF_MessageHeader) result.get(0) : null;
    }

    /**
     * Method Description:Update the Message Header table
     *
     * @param messageId
     * @param remittanceDto
     * @param ruleId
     * @param remittanceId
     */
    public static void updateMsgHeader(IBOUB_INF_MessageHeader msgHeader, RemittanceProcessDto remittanceDto, String ruleId,
            String remittanceId) {
        // open api update
        msgHeader.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
        if (remittanceDto.isStp()) {
            msgHeader.setF_MESSAGESTATUS(PaymentSwiftConstants.PROCESSED);
            msgHeader.setF_REMITTANCEID(remittanceId);
        }
        else {
            msgHeader.setF_MESSAGESTATUS(PaymentSwiftConstants.REMITTER_WAIT);
        }
        msgHeader.setF_RULEID(ruleId);
        msgHeader.setF_ERRORCODE(0);
        msgHeader.setF_ERRORCODEPARAM(StringUtils.EMPTY);
    }

}
