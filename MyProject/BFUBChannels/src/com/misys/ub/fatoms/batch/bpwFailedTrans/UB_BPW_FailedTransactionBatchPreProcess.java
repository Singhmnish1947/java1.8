package com.misys.ub.fatoms.batch.bpwFailedTrans;

import com.ibm.disthubmq.client.Factory;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.IBatchPreProcess;
import com.trapedza.bankfusion.bo.refimpl.IBOAuditDetailsBlob;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWHEADERTAG;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWRFAILEDTXNS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_FIN_STANDINGORDERTAG;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class UB_BPW_FailedTransactionBatchPreProcess implements IBatchPreProcess {
	public void init(BankFusionEnvironment arg0) {
		// TODO Auto-generated method stub		
	}

	public void process(AbstractFatomContext arg0) {
	    
		//.getStatelessNewInstance(IBOUBTB_BPWRFAILEDTXNS.BONAME);
		// TODO Auto-generated method stub		
	}
}
