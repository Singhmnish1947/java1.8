package com.misys.ub.interfaces;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.ub.batchgateway.persistence.PrivatePersistenceFactory;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_MaintainMessageHeader;

import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.ub.types.UB_IBI_Header;

public class MaintainMessageHeader extends AbstractUB_INF_MaintainMessageHeader {

	public MaintainMessageHeader() {
		super();
	}

	@SuppressWarnings("deprecation")
	public MaintainMessageHeader(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		UB_IBI_Header header =  getF_IN_messageHeader();
		if(header != null){
			PrivatePersistenceFactory factory = new PrivatePersistenceFactory();
			IBOUB_INF_MessageHeader existingHeaderBOItem = (IBOUB_INF_MessageHeader)localFactory().findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, header.getMsgID().getIFMid(), true);
			IBOUB_INF_MessageHeader headerBOItem = (IBOUB_INF_MessageHeader) localFactory().getStatelessNewInstance(IBOUB_INF_MessageHeader.BONAME);
			if(getF_IN_operationName().equalsIgnoreCase("create")){				
				headerBOItem.setF_CHANNELID(getF_IN_channelId());
				headerBOItem.setF_ERRORCODE(getF_IN_errorCode());
				headerBOItem.setF_DIRECTION(getF_IN_direction());
				headerBOItem.setBoID(header.getMsgID().getIFMid());
				headerBOItem.setF_MESSAGEID2(header.getMsgID().getBOINGid());
				headerBOItem.setF_MESSAGESTATUS(header.getMsgStatus());
				headerBOItem.setF_MESSAGETYPE(header.getMsgType());
				headerBOItem.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
				headerBOItem.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
				headerBOItem.setF_RUNTIMEMICROFLOWID(getMicroflowId());
				headerBOItem.setF_REFERENCE(getF_IN_senderRef());
                if (getF_IN_ErrorMessageStatus() != null && getF_IN_ErrorMessageStatus().getCodesCount() != 0) {
                    headerBOItem.setF_ERRORCODEPARAM(getErrorParamters());
                }
				if(existingHeaderBOItem == null){
					factory.create(headerBOItem);
				}else{
					if(!existingHeaderBOItem.getF_RUNTIMEMICROFLOWID().equals(getMicroflowId())){
						IBusinessEventsService businessEventsService = (IBusinessEventsService)ServiceManagerFactory.getInstance().getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
						Event duplicateMessageEvent = new Event();
						duplicateMessageEvent.setEventNumber(ChannelsEventCodes.E_DUPLICATE_MESSAGES_AND_CAN_T_BE_PROCESSED);
						businessEventsService.handleEvent(duplicateMessageEvent);
					}else{
						setF_OUT_isReexecution(true);
					}
				}
			}
			if(getF_IN_operationName().equalsIgnoreCase("update")){
				if (existingHeaderBOItem != null) {
					headerBOItem.updateFromMap(existingHeaderBOItem.getDataMap());
				}					
				headerBOItem.setF_ERRORCODE(getF_IN_errorCode());
				headerBOItem.setF_MESSAGESTATUS(header.getMsgStatus());
				headerBOItem.setBoID(header.getMsgID().getIFMid());
				headerBOItem.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
                if (getF_IN_ErrorMessageStatus() != null && getF_IN_ErrorMessageStatus().getCodesCount() != 0) {
                    headerBOItem.setF_ERRORCODEPARAM(getErrorParamters());
                }
				if(null!=getF_IN_ruleId() && getF_IN_ruleId().length()>0){
					headerBOItem.setF_RULEID(getF_IN_ruleId());
				}
				headerBOItem.setF_REMITTANCEID(getF_IN_RemittanceID());
				factory.update(headerBOItem);
			}
		}
	}
	
    private String getMicroflowId() {
        return BankFusionThreadLocal.getRuntimeMFId().substring(0, 16);
    }
	
	private IPersistenceObjectsFactory localFactory(){
		return BankFusionThreadLocal.getPersistanceFactory();
	}

    /**
     * Method Description:Get Error Parameters
     * 
     * @return
     */
    private String getErrorParamters() {
        MessageStatus msgStatus = getF_IN_ErrorMessageStatus();
        StringBuilder sb = new StringBuilder();
        SubCode subCode = msgStatus.getCodes(0);
        if (StringUtils.isNotBlank(subCode.getCode())) {
            int count = 0;
            if (null != subCode.getParameters()) {
                int totalParameters = subCode.getParametersCount();
                for (EventParameters param : subCode.getParameters()) {
                    if (count == 0) {
                        sb.append(param.getEventParameterValue());
                    }

                    if (count > 1 && count < totalParameters) {
                        sb.append(",").append(param.getEventParameterValue());
                    }

                    count++;
                }
            }

        }
        return sb.toString();
    }
}
