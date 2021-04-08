package com.trapedza.bankfusion.features;


import com.trapedza.bankfusion.features.refimpl.AbstractUB_External_Interface_SettlementAccountFeature;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerHolder;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;

public class UB_External_Interface_SettlementAccountFeature  extends AbstractUB_External_Interface_SettlementAccountFeature  {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public UB_External_Interface_SettlementAccountFeature (BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	 public UB_External_Interface_SettlementAccountFeature () {
	        super(null);
	    }
	
	public void process(BankFusionEnvironment env) {

    }
	public void registerWithUpdateLoggerManager(UpdateAuditLoggerManager manager) {
        if (!manager.isTransactionLoggingEnabled()) {
            return;
        }
        super.registerWithUpdateLoggerManager(manager);
	}
        

}
