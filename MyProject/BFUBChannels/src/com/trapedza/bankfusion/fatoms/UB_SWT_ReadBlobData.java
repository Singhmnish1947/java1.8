package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import bf.com.misys.ub.types.interfaces.Ub_MT103;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_SWTMessageDetail;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ReadBlobData;

public class UB_SWT_ReadBlobData extends AbstractUB_SWT_ReadBlobData{

	static final String query = " WHERE " + IBOUB_INF_SWTMessageDetail.MESSAGEID + " = ?";
	public UB_SWT_ReadBlobData(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public UB_SWT_ReadBlobData() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		// TODO Auto-generated method stub
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		
		ArrayList param = new ArrayList();
		param.add(getF_IN_messageID());
		
		IBOUB_INF_SWTMessageDetail msgDtl = (IBOUB_INF_SWTMessageDetail)(factory.findByQuery(IBOUB_INF_SWTMessageDetail.BONAME, query, param, null)).get(0);
		
		Ub_MT103 msg = (Ub_MT103)BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());
		
		setF_OUT_MT103_Msg(msg);
	}

}
