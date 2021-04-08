package com.trapedza.bankfusion.fatoms;	

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.ub.types.ubintfc.AccAndLobDtlsListRq;
import bf.com.misys.ub.types.ubintfc.AccIdList;

import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_AccountEnableDisable;

public class UB_INF_AccountEnableDisable extends AbstractUB_INF_AccountEnableDisable {
	private static final long serialVersionUID = -2737631093052873221L;
	private static final transient Log logger = LogFactory.getLog(UB_INF_AccountEnableDisable.class.getName());
	protected IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	public UB_INF_AccountEnableDisable() {
		super();
	}

	public UB_INF_AccountEnableDisable(BankFusionEnvironment env) {
		super(env);
	}
	
	public void process(BankFusionEnvironment env) throws BankFusionException {
		logger.info("Start of AccountEnableDisable");
		AccAndLobDtlsListRq accAndLobDtlsListRq = this.getF_IN_accAndLobDtlsListRq();
		AccIdList[] accIdList = accAndLobDtlsListRq.getAccIdList();
		String lob = accAndLobDtlsListRq.getLob();
		
		StringBuffer sb =new StringBuffer( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		sb.append("<ubintcf:AccAndLobDtlsListRq xmlns:ubintcf=\"http://www.misys.com/ub/types/ubintfc\">");
		for(int i=0 ; i<(accIdList.length/2) ; ++i) {
			if(accIdList[i].getAccOperation() != null) {
				sb.append("<ubintcf:accIdList>");
				sb.append("<ubintcf:accId>");
				logger.info("Account ID : " + accIdList[i].getAccId());
				sb.append(accIdList[i].getAccId());
				sb.append("</ubintcf:accId>");
				sb.append("<ubintcf:accOperation>");
				sb.append(accIdList[i].getAccOperation());
				logger.info("Account ID : " + accIdList[i].getAccId() + "\tAccountOperation New : " + accIdList[i].getAccOperation());
				sb.append("</ubintcf:accOperation>");
				sb.append("</ubintcf:accIdList>");				
			}
		}
		sb.append("<ubintcf:lob>");
		sb.append(lob);
		logger.info("LOB : " + accAndLobDtlsListRq.getLob());
		sb.append("</ubintcf:lob>");
		sb.append("</ubintcf:AccAndLobDtlsListRq>");
		String xmlString = sb.toString();
		logger.info("XML String: "+xmlString);
		MessageProducerUtil.sendMessage(xmlString, "ACCOUNT_DETAIL_REQUEST");
		
		logger.info("End of AccountEnableDisable");
	}
	
}
