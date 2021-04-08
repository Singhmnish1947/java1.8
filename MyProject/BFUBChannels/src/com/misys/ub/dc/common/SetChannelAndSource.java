package com.misys.ub.dc.common;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSetChannelAndSource;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class SetChannelAndSource extends AbstractSetChannelAndSource {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetChannelAndSource()
	{
		
	}
	@SuppressWarnings("deprecation")
	public SetChannelAndSource(BankFusionEnvironment env) {
		super(env);
	}
	@Override
	public void process(BankFusionEnvironment env) 
	{
	BankFusionThreadLocal.setSourceId(getF_IN_CHANNELID());
	BankFusionThreadLocal.setChannel(getF_IN_CHANNELID());
	}
}
	