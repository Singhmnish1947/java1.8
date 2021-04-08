/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.utils.restServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.ub.utils.types.LineOfBusinessListRq;
import com.misys.ub.utils.types.LineOfBusinessListRs;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_CUSTLINEOFBUSINESS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_AccountInfMap;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class RetrieveLOBServiceHelper {

	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	private final static Log LOGGER = LogFactory.getLog(RetrieveLOBService.class.getName());

	@Transactional
	public LineOfBusinessListRs fetchLOBList(LineOfBusinessListRq inputRq) {
		String customerId;
		String accountId;
		ArrayList<String> customerLvlLobList = null, accountLvlLobList = null, applicationList = null;
		LineOfBusinessListRs res = null;

		customerId = inputRq.getCustomerId();
		accountId = inputRq.getAccountId();

		if (customerId != null && !"".equals(customerId)) {
			customerLvlLobList = fetchCusLvlLobList(customerId);
			if (accountId != null && !"".equals(accountId)) {
				accountLvlLobList = fetchAccLvlLobList(accountId);
			}
		} else if (accountId != null && !"".equals(accountId)) {
			accountLvlLobList = fetchAccLvlLobList(accountId);
			customerId = fetchCustomer(accountId);
			if (customerId != null) {
				customerLvlLobList = fetchCusLvlLobList(customerId);
			}
		}

		if (customerLvlLobList != null || accountLvlLobList != null) {
			applicationList = getApplicationList(customerLvlLobList,
					accountLvlLobList);
		}

		res = new LineOfBusinessListRs();

		if (applicationList != null) {

			res.setLisOfBusinesses(applicationList);
			if (accountId != null && !"".equals(accountId)) {
				res.setId(accountId);
				res.setCustomerId(customerId);
			} else {
				res.setId(customerId);
				res.setCustomerId(customerId);
			}
		} else {
			res.setId(accountId);
			res.setCustomerId(customerId);
		}

		return res;
	}

	@Transactional
	private ArrayList<String> getApplicationList(ArrayList<String> cusLvlList,
			ArrayList<String> accLvlList) {
		ArrayList<String> applicationList = null;
		if (cusLvlList != null) {
			if (accLvlList != null) {
				applicationList = new ArrayList<String>();
				HashSet<String> hashSet = new HashSet<String>();
				for (int i = 0; i < accLvlList.size(); i++) {
					hashSet.add(accLvlList.get(i));
				}
				for (int i = 0; i < cusLvlList.size(); i++) {
					String app = cusLvlList.get(i);
					if (hashSet.contains(app)) {
						applicationList.add(app);
					}
				}
				if (cusLvlList.contains("DIGICHANNELS") && !applicationList.contains("DIGICHANNELS")) {
					applicationList.add("DIGICHANNELS");
				}
				if (cusLvlList.contains("CORPCHANNELS") && !applicationList.contains("CORPCHANNELS")) {
					applicationList.add("CORPCHANNELS");
				}
				if (cusLvlList.contains("INSIGHT") && !applicationList.contains("INSIGHT")) {
					applicationList.add("INSIGHT");
				}
			} else {
				applicationList = cusLvlList;
			}
		} else if (accLvlList != null) {
			applicationList = accLvlList;
		}

		return applicationList;
	}

	@Transactional
	@SuppressWarnings("deprecation")
	private String fetchCustomer(String accountId) {
		
		IBOAccount accrow = (IBOAccount) factory.findByPrimaryKey(
				IBOAccount.BONAME, accountId);
		
		if (accrow == null || "".equals(accrow)) {
			logErrorMessage("40410029");
			
			return null;
		}

		return accrow.getF_CUSTOMERCODE();
	}

	@Transactional
	@SuppressWarnings("null")
	public ArrayList<String> fetchCusLvlLobList(String customerId) {
		ArrayList<String> param = new ArrayList<String>();
		ArrayList<String> custLobList = null;
		param.add(customerId);
		List<IBOUB_CNF_CUSTLINEOFBUSINESS> custLobInfMapRows = (List<IBOUB_CNF_CUSTLINEOFBUSINESS>) factory
		.findByQuery(IBOUB_CNF_CUSTLINEOFBUSINESS.BONAME, "where "+ IBOUB_CNF_CUSTLINEOFBUSINESS.UBCUSTOMERCODE + " =  ? ", param,null);
		
		custLobList = new ArrayList<String>();
		for (int i = 0; i < custLobInfMapRows.size(); i++) {
			custLobList.add(custLobInfMapRows.get(i).getF_UBLINEOFBUSINESS());
		}
		
		if (custLobList == null || custLobList.size() == 0) {
			// raiseEvent("40413033", null);
			return null;
		}

		return custLobList;
	}

	@Transactional
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	private ArrayList<String> fetchAccLvlLobList(String accountId) {
		ArrayList param = new ArrayList();
		param.add(accountId);
		ArrayList<String> interfaceidList = null;

		List<IBOUB_INF_AccountInfMap> accntinfMapRows = (List<IBOUB_INF_AccountInfMap>) factory
				.findByQuery(IBOUB_INF_AccountInfMap.BONAME, "where "
						+ IBOUB_INF_AccountInfMap.ACCOUNTID + " =  ? ", param,
						null);

		interfaceidList = new ArrayList<String>();
		for (int i = 0; i < accntinfMapRows.size(); i++) {
			interfaceidList.add(accntinfMapRows.get(i).getF_INTERFACEID());
		}
		return interfaceidList;
	}

	@Transactional
	private void logErrorMessage(String eventcode) {
		String error = BankFusionMessages.getInstance()
				.getFormattedEventMessage(Integer.parseInt(eventcode), null,
						BankFusionThreadLocal.getUserSession().getUserLocale(),
						true);
		LOGGER.info(error);
	}
}
