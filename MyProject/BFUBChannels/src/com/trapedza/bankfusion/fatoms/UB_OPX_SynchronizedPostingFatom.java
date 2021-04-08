package com.trapedza.bankfusion.fatoms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.microflow.ActivityStep;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_SynchronizedPostingFatom;

public class UB_OPX_SynchronizedPostingFatom extends
		AbstractUB_OPX_SynchronizedPostingFatom {

	private transient final static Log logger = LogFactory
			.getLog(UB_OPX_SynchronizedPostingFatom.class.getName());
	private static Map<String, String> lockMap = java.util.Collections
			.synchronizedMap(new HashMap<String, String>());
	boolean error;

	public UB_OPX_SynchronizedPostingFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env, ActivityStep as) {
		Map<String, Object> inputTags = as.getInTags();
		Map<String, Object> outputTags = new HashMap<String, Object>();
		String settlementAccID = (String) inputTags.get("SettlementAccountID");
		String contraAccID = (String) inputTags.get("ContraAccountID");
		try {
			if (null == settlementAccID || null == contraAccID
					|| settlementAccID.equals(CommonConstants.EMPTY_STRING)
					|| contraAccID.equals(CommonConstants.EMPTY_STRING)) {
				if (null == settlementAccID
						|| settlementAccID.equals(CommonConstants.EMPTY_STRING)) {
					outputTags.put("ErrorNumber", 40000126);
					outputTags.put("Status", "F");
					setOutputTags(outputTags);
				} else {
					outputTags.put("ErrorNumber", 40411081);
					outputTags.put("Status", "F");
					setOutputTags(outputTags);
				}

			} else {
				lockAccounts(settlementAccID, contraAccID);
				MFExecuter.executeMF("UB_OPX_Posting_SRV",
						BankFusionThreadLocal.getBankFusionEnvironment(),
						inputTags);

				outputTags.put("ErrorNumber", 0);
				outputTags.put("Status", "P");
				setOutputTags(outputTags);
			}

		} catch (Exception e) {
			error = true;
			int eventNumber = 0;

			if (e instanceof BankFusionException) {
				Collection<IEvent> events = ((BankFusionException) e)
						.getEvents();
				Iterator<IEvent> itr = events.iterator();
				while (itr.hasNext()) {
					IEvent e1 = itr.next();
					eventNumber = e1.getEventNumber();
				}

			}// Exception other than Bankfusion exception
			else {
				eventNumber = 40422013;
			}

			setOutputTags(outputTags);
			outputTags.put("ErrorNumber", eventNumber);
			outputTags.put("Status", "F");
		} finally {
			if (error) {
				BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
				unlockAccounts(settlementAccID, contraAccID);
			} else {

				BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
				unlockAccounts(settlementAccID, contraAccID);
			}
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

			error = false;
		}
	}

	/**
	 * 
	 * @param account1
	 * @param account2
	 */
	private void lockAccounts(String account1, String account2) {
		synchronized (lockMap) {

			if (lockMap.containsKey(account1) || lockMap.containsKey(account2)) {
				if (logger.isDebugEnabled()) {
					logger.debug(Thread.currentThread().getName() + ": "
							+ "THE Account " + account1
							+ " IS LOCKED -> SENDING TO RETRY QUEUE");
				}
				try {
					lockMap.wait();
				} catch (InterruptedException exception) {
					logger.error(Thread.currentThread().getName() + ": "
							+ " Error while executing. ");
					Thread.currentThread().interrupt();
				}
				lockAccounts(account1, account2);

			} else {
				if (logger.isDebugEnabled()) {
					logger.debug(Thread.currentThread().getName() + ": "
							+ "ENTERED LOCK FOR / " + account1 + " AND "
							+ account2);
				}
				lockMap.put(account1, null);
				lockMap.put(account2, null);
				return;
			}
		}

	}

	/**
	 * 
	 * @param account1
	 * @param account2
	 */
	private void unlockAccounts(String account1, String account2) {
		if (logger.isDebugEnabled()) {
			logger.debug(Thread.currentThread().getName() + ": "
					+ "RELEASING LOCK FOR / ");
		}
		synchronized (lockMap) {
			lockMap.remove(account1);
			lockMap.remove(account2);
			lockMap.notifyAll();
		}
	}

}
