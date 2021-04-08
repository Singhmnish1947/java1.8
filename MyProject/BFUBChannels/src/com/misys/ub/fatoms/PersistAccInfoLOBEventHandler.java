package com.misys.ub.fatoms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_CUSTLINEOFBUSINESS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_AccountInfMap;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractPersistAccInfoLOBEventHandler;
import com.trapedza.bankfusion.utils.GUIDGen;


public class PersistAccInfoLOBEventHandler extends AbstractPersistAccInfoLOBEventHandler {
	private static final transient Log logger = LogFactory
			.getLog(PersistAccInfoLOBEventHandler.class.getName());
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	public PersistAccInfoLOBEventHandler() {
		super();
	}

	public PersistAccInfoLOBEventHandler(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		logger.info("Start of PersistAccInfoLOBEventHandler");
		String accountNumber = getF_IN_accountNumber();
		if(accountNumber==null || "".equals(accountNumber) ) {
			accountNumber = getF_IN_ACCOUNTNO();
		}
		if(accountNumber!=null && !"".equals(accountNumber) ) {
		IBOAccount accrow = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountNumber);
		if(accrow!=null) {
			ArrayList<String> param = new ArrayList<String>();
			
			param.add(accrow.getF_CUSTOMERCODE());
			List<IBOUB_CNF_CUSTLINEOFBUSINESS> custLobInfMapRows = (List<IBOUB_CNF_CUSTLINEOFBUSINESS>) factory
			.findByQuery(IBOUB_CNF_CUSTLINEOFBUSINESS.BONAME, "where "+ IBOUB_CNF_CUSTLINEOFBUSINESS.UBCUSTOMERCODE + " =  ? ", param,null);
			
				ArrayList<String> custLobList = new ArrayList<String>();
				for (int i = 0; i < custLobInfMapRows.size(); i++) {
					custLobList.add(custLobInfMapRows.get(i).getF_UBLINEOFBUSINESS());
				}
				IBOUB_INF_AccountInfMap accLevelLobBO = null;
				if(custLobList.contains("DIGICHANNELS")) {
					accLevelLobBO = null;
					accLevelLobBO= (IBOUB_INF_AccountInfMap) factory.getStatelessNewInstance(IBOUB_INF_AccountInfMap.BONAME);
					accLevelLobBO.setF_ACCOUNTID(accountNumber);
					accLevelLobBO.setF_INTERFACEID("DIGICHANNELS");
					accLevelLobBO.setF_ISACTIVE(true);
				} else {
					accLevelLobBO = null;
					accLevelLobBO= (IBOUB_INF_AccountInfMap) factory.getStatelessNewInstance(IBOUB_INF_AccountInfMap.BONAME);
					accLevelLobBO.setF_ACCOUNTID(accountNumber);
					accLevelLobBO.setF_INTERFACEID("DIGICHANNELS");
					accLevelLobBO.setF_ISACTIVE(false);
				}
				accLevelLobBO.setBoID(GUIDGen.getNewGUID());
				factory.create(IBOUB_INF_AccountInfMap.BONAME, accLevelLobBO);
				if(custLobList.contains("CORPCHANNELS")) {
					accLevelLobBO = null;
					accLevelLobBO= (IBOUB_INF_AccountInfMap) factory.getStatelessNewInstance(IBOUB_INF_AccountInfMap.BONAME);
					accLevelLobBO.setF_ACCOUNTID(accountNumber);
					accLevelLobBO.setF_INTERFACEID("CORPCHANNELS");
					accLevelLobBO.setF_ISACTIVE(true);
				} else {
					accLevelLobBO = null;
					accLevelLobBO= (IBOUB_INF_AccountInfMap) factory.getStatelessNewInstance(IBOUB_INF_AccountInfMap.BONAME);
					accLevelLobBO.setF_ACCOUNTID(accountNumber);
					accLevelLobBO.setF_INTERFACEID("CORPCHANNELS");
					accLevelLobBO.setF_ISACTIVE(false);
				}
				accLevelLobBO.setBoID(GUIDGen.getNewGUID());
				factory.create(IBOUB_INF_AccountInfMap.BONAME, accLevelLobBO);
				if(custLobList.contains("FEESBILLING") && accrow.getF_UBFABAPPLICABLE().equals("Y")) {
					accLevelLobBO = null;
					accLevelLobBO= (IBOUB_INF_AccountInfMap) factory.getStatelessNewInstance(IBOUB_INF_AccountInfMap.BONAME);
					accLevelLobBO.setF_ACCOUNTID(accountNumber);
					accLevelLobBO.setF_INTERFACEID("FEESBILLING");
					accLevelLobBO.setF_ISACTIVE(true);
					accLevelLobBO.setBoID(GUIDGen.getNewGUID());
					factory.create(IBOUB_INF_AccountInfMap.BONAME, accLevelLobBO);
				}
				
		}
		logger.info("End of PersistAccInfoLOBEventHandler");
		}
	}
}