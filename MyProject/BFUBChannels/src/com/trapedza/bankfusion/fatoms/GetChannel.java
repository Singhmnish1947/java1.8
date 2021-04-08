package com.trapedza.bankfusion.fatoms;
/*
 * This process is used by ATM/POS for fetching channel based on device id's which comes as part of message. 
 * This channel id is stored in transaction table during posting.
 */

import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_GetChannel;

public class GetChannel extends AbstractUB_ATM_GetChannel {
	
	public GetChannel() {
        super(null);
    }
	
	public GetChannel(BankFusionEnvironment env) {
        super(env);
    }
	
	
	private final IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	
	public void process(BankFusionEnvironment env)
	{
		String deviceId = getF_IN_deviceId();
		IBOATMSettlementAccount resultSet = (IBOATMSettlementAccount) factory.findByPrimaryKey(IBOATMSettlementAccount.BONAME, deviceId, true);
		String channel = "ATM";
		if(resultSet != null)
		{
			channel = resultSet.getF_UBCHANNELID();
		}
		setF_OUT_channel(channel);
	}
}
