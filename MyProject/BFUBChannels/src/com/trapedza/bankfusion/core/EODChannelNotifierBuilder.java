package com.trapedza.bankfusion.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.system.types.v1r0.ChannelStatus;
import bf.com.misys.cbs.system.types.v1r0.ChannelStatusNotifier;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_EODChannelNotifierBuilder;
import com.trapedza.bankfusion.steps.refimpl.IUB_CMN_EODChannelNotifierBuilder;

public class EODChannelNotifierBuilder extends
		AbstractUB_CMN_EODChannelNotifierBuilder implements
		IUB_CMN_EODChannelNotifierBuilder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory
			.getLog(EODChannelNotifierBuilder.class.getName());

	private static final String CHANNEL_IBI = "IBI";

	private static final String JMS_CHANNEL = "JMS";

	private static final String CHANNEL_STATUS_ONLINE = "CHANNEL_ONLINE";

	private static final String CHANNEL_STATUS_OFFLINE = "CHANNEL_OFFLINE";

	public EODChannelNotifierBuilder(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {

		if (logger.isDebugEnabled()) {
			logger
					.debug("Preparing EOD Channel Notifier message for suspending JMS communication with IBI channel");
		}
		ChannelStatusNotifier channelStatusNotifier = new ChannelStatusNotifier();
		channelStatusNotifier.setNotificationDateTime(SystemInformationManager
				.getInstance().getBFBusinessDateTime());
		ChannelStatus channelStatus = new ChannelStatus();
		channelStatus.setChannelName(CHANNEL_IBI);
		channelStatus.setChannelType(JMS_CHANNEL);
		if (getF_IN_JMSchannelStatus().getStatus()
				.equals(CHANNEL_STATUS_ONLINE))
			channelStatus.setStatus(CHANNEL_STATUS_ONLINE);
		else
			channelStatus.setStatus(CHANNEL_STATUS_OFFLINE);
		channelStatusNotifier.addChannelStatuses(channelStatus);
		setF_OUT_channelStatusNotifier(channelStatusNotifier);

	}

}
