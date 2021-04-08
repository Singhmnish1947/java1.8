package com.trapedza.bankfusion.features;


import com.trapedza.bankfusion.features.refimpl.AbstractUB_TIP_TradeFinanceFeature;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerHolder;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;

/**
 *
 * 
 * @AUTHOR Rubalin
 * @DATE July 10, 2009
 * @PROJECT TI Interface 
 */
public class TradeFinanceFeature  extends AbstractUB_TIP_TradeFinanceFeature {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public TradeFinanceFeature (BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	 public TradeFinanceFeature () {
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
