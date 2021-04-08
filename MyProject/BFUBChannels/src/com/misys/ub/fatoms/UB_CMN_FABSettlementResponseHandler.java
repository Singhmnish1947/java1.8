package com.misys.ub.fatoms;

import bf.com.misys.fbe.types.FabSettlementAckEvent;
import bf.com.misys.fbe.types.FabSettlementProcessingStatus;

import com.misys.bankfusion.subsystem.messaging.MessageSenderUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_FABSettlementResponseHandler;

public class UB_CMN_FABSettlementResponseHandler extends AbstractUB_CMN_FABSettlementResponseHandler {

	public UB_CMN_FABSettlementResponseHandler() {
		super();
	}

	public UB_CMN_FABSettlementResponseHandler(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		FabSettlementAckEvent fabSettlementAckEvent = getF_IN_FabSettlementAckEvent();
		FabSettlementProcessingStatus[] fabSettlementProcessingStatus = fabSettlementAckEvent.getFabSettlementAckEvent();
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<Acknowledgements>");
		for(FabSettlementProcessingStatus fabProcessingStatus : fabSettlementProcessingStatus) {
			sb.append("<Acknowledgement>")
			.append("<SettlementId>").append(fabProcessingStatus.getSettlementId()).append("</SettlementId>")
			.append("<Status>").append(fabProcessingStatus.getStatus()).append("</Status>")
			.append("</Acknowledgement>");
		}
		sb.append("</Acknowledgements>");
		MessageProducerUtil.sendMessage(sb.toString(),"FBFB_SETTLEMENT_ACK");
	}

}
