package com.misys.ub.datacenter;

import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.cbs.config.ModuleConfiguration;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TXN_DCPOSTINGMSGLOG;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;

public class DCTxnPostingLogger {

	private transient static Log logger = LogFactory.getLog(DCTxnPostingLogger.class);
	public void insertIntoLog(LoggerBean logDetails) {

		String currentUser = BankFusionThreadLocal.getUserId();
		Timestamp lastUpdatedDateTime = SystemInformationManager.getInstance()
				.getBFBusinessDateTime();
		String lastModifiedBy = currentUser;
		IPersistenceObjectsFactory factory;
		factory = BankFusionThreadLocal.getPersistanceFactory();
		IBOUB_TXN_DCPOSTINGMSGLOG postingLogBO = (IBOUB_TXN_DCPOSTINGMSGLOG) factory
				.getStatelessNewInstance(IBOUB_TXN_DCPOSTINGMSGLOG.BONAME);
		postingLogBO.setBoID(GUIDGen.getNewGUID());
		postingLogBO.setF_TXNREF(logDetails.getTxnRef());
		postingLogBO.setF_SRNO(logDetails.getSerialNo());
		postingLogBO.setF_ACCOUNTID(logDetails.getAccountId());
		postingLogBO.setF_POSTINGDTTM(logDetails.getPostingDateTime());
		postingLogBO.setF_INDICATOR(logDetails.getFlagIndicator());
		postingLogBO.setF_INDICATORVALUE(logDetails.getValue());
		postingLogBO.setF_EVENTCODE(logDetails.getEventCode());
		postingLogBO.setF_SOURCEBRANCHID(logDetails.getSourceBranchId());
		postingLogBO.setF_CHANNELID(logDetails.getChannelId());
		postingLogBO.setF_LASTUPDATEDDTTM(lastUpdatedDateTime);
		postingLogBO.setF_LASTMODIFIEDBY(lastModifiedBy);

		factory.create(IBOUB_TXN_DCPOSTINGMSGLOG.BONAME, postingLogBO);

	}

	public void setLogStatus(String accountId, String txnRef,
			Timestamp postingDateTime, String flagIndicator, String value,
			String eventCode, String sourceBranchId, String channelId,
			Integer serialNo) {
		LoggerBean logDetails = new LoggerBean();
		logDetails.setSerialNo(serialNo);
		logDetails.setAccountId(accountId);
		logDetails.setTxnRef(txnRef);
		logDetails.setPostingDateTime(postingDateTime);
		logDetails.setFlagIndicator(flagIndicator);
		logDetails.setValue(value);
		logDetails.setEventCode(eventCode);
		logDetails.setSourceBranchId(sourceBranchId);
		logDetails.setChannelId(channelId);
		try {
			insertIntoLog(logDetails);
		} catch (BankFusionException bfe) {
			logger.error("Entry for UB_TXN_DCPOSTINGMSGLOG table could not be created", bfe);
		}

	}

}
