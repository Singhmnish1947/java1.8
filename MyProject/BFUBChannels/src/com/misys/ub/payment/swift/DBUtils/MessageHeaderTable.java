package com.misys.ub.payment.swift.DBUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.fbe.common.constant.QueryConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;

/**
 * UBINTERFACE.INFTB_MESSAGEHEADER
 *
 */
public class MessageHeaderTable {
    private transient final static Log logger = LogFactory.getLog(MessageHeaderTable.class.getName());

    public static final String QUERY_TO_FIND_USING_MESSAGE_NUMBER = QueryConstants.WHERE + IBOUB_INF_MessageHeader.MESSAGEID2
            + QueryConstants.EQUALS_PARAM;
    public static final String QUERY_TO_FIND_BY_REMITTANCE_ID = QueryConstants.WHERE + IBOUB_INF_MessageHeader.REMITTANCEID
            + QueryConstants.EQUALS_PARAM;

    /**
     * @param outwardMsg
     * @param messageId
     * @param isNonSTP
     * @param ruleId
     * @param remittanceId
     */
    public void insertMessageHeader(OutwardSwtRemittanceRq outwardMsg, String messageId, boolean isNonSTP, String ruleId,
            String remittanceId) {
        if (logger.isInfoEnabled())
            logger.info("Start of insertMessageHeader");
        try {
            IBOUB_INF_MessageHeader messageHeader = (IBOUB_INF_MessageHeader) BankFusionThreadLocal.getPersistanceFactory()
                    .getStatelessNewInstance(IBOUB_INF_MessageHeader.BONAME);
            messageHeader.setBoID(messageId);
            String channel = outwardMsg.getRqHeader().getOrig().getChannelId();
            // save the entire message.
            messageHeader.setF_DATAMESSAGE(serializeComplexTypeToByteArray(outwardMsg));
            messageHeader.setF_MESSAGEID2(outwardMsg.getIntlPmtInputRq().getIntlPmtDetails().getPmtReference());
            messageHeader.setF_MESSAGETYPE(PaymentSwiftConstants.IPAY);
            messageHeader.setF_CHANNELID(channel);
            if (isNonSTP) {
                messageHeader.setF_MESSAGESTATUS(PaymentSwiftConstants.REMITTER_WAIT);
            }
            else {
                messageHeader.setF_MESSAGESTATUS(PaymentSwiftConstants.PROCESSED);
                messageHeader.setF_REMITTANCEID(remittanceId);
            }
            // Fix added for FBPY-2752
            messageHeader.setF_RULEID(ruleId);
            messageHeader.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
            messageHeader.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
            // Search was not working in "Message Information Console", so changing the direction
            messageHeader.setF_DIRECTION(PaymentSwiftConstants.INWARD);
            // Storing the "REFERENCE" received from different channels: Teller/IBI/CCI
            messageHeader.setF_REFERENCE(getReference(outwardMsg));
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_MessageHeader.BONAME, messageHeader);
        }
        catch (BankFusionException e) {
            logger.error("Error Message during insertion into INFTB_MESSAGEHEADER", e);
            PaymentSwiftUtils.handleEvent(Integer.parseInt("20600092"), new String[] {});
        }
        if (logger.isInfoEnabled())
            logger.info("End of insertMessageHeader");
    }

    /**
     * @param ruleId
     * @param messageId
     * @param status
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateMessageHeader(String messageId, String status, String remittanceIdPk) {
        logger.info("Start of updateMessageHeader");
        IBOUB_INF_MessageHeader msgHeader = findByMessageId(messageId);
        if (msgHeader != null) {
            msgHeader.setF_MESSAGESTATUS(status);
            msgHeader.setF_REMITTANCEID(get35CharacterTextLine(remittanceIdPk));
            msgHeader.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
        }
        logger.info("End of updateMessageHeader");
    }

    /**
     * @param messageId
     * @return IBOUB_INF_MessageHeader
     */
    public static IBOUB_INF_MessageHeader findByMessageId(String messageId) {
        return (IBOUB_INF_MessageHeader) BankFusionThreadLocal.getPersistanceFactory()
                .findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, messageId, true);
    }

    /**
     * @param outwardMsg
     * @return
     */
    private byte[] serializeComplexTypeToByteArray(OutwardSwtRemittanceRq outwardMsg) {
        return BankFusionIOSupport.convertToBytes(outwardMsg);
    }

    /**
     * @param outwardMsg
     * @param channelId
     * @return
     */
    private String getReference(OutwardSwtRemittanceRq outwardMsg) {
        return outwardMsg.getIntlPmtInputRq().getIntlPmtDetails().getPmtReference() != null
                ? outwardMsg.getIntlPmtInputRq().getIntlPmtDetails().getPmtReference()
                : StringUtils.EMPTY;
    }

    /**
     * @param str
     * @return
     */
    private String get35CharacterTextLine(String str) {
        String spaceConstant = " ";
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str) && !str.equals(spaceConstant) && str.length() <= 35) {
            output = str.substring(0, str.length());
        }
        return output;
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
     * Method Description:Find by the MessageId2. This is the paymentInfoId from openApi
     * 
     * @param messageNumber
     * @return
     */
    public static IBOUB_INF_MessageHeader findByRemittanceId(String remittanceId) {

        ArrayList params = new ArrayList();
        params.add(remittanceId);
        List result = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOUB_INF_MessageHeader.BONAME,
                QUERY_TO_FIND_BY_REMITTANCE_ID, params,
                null, true);

        return CommonUtil.checkIfNotNullOrEmpty(result) ? (IBOUB_INF_MessageHeader) result.get(0) : null;
    }

}
