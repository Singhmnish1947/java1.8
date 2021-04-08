package com.trapedza.bankfusion.fatoms;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.task.runtime.exception.AbstractDialogException;
import com.misys.bankfusion.subsystem.task.runtime.exception.ReferralDialogException;
import com.misys.bankfusion.subsystem.task.runtime.impl.CommunicableTask;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.datacenter.DataCenterCommonConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TXN_DCPOSTINGMSGLOG;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TXN_TransactionPosting;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class UB_TXN_TransactionPosting extends AbstractUB_TXN_TransactionPosting {
	private static final long serialVersionUID = 1L;
	private transient static final Log LOG = LogFactory.getLog(UB_TXN_TransactionPosting.class);
	protected IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	static final String Query = "SELECT  A." + IBOUB_TXN_DCPOSTINGMSGLOG.EVENTCODE + " AS EVENTCODE FROM " + IBOUB_TXN_DCPOSTINGMSGLOG.BONAME + " A  WHERE A." + IBOUB_TXN_DCPOSTINGMSGLOG.TXNREF + " = ? ";

	/**
	 * default constructor
	 *
	 *
	 */
	public UB_TXN_TransactionPosting() {
		super();
	}

	public UB_TXN_TransactionPosting(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		super.process(env);
		Boolean offlineMode = this.isF_IN_offlineMode();
		MessageStatus txnStatus = new MessageStatus();
		RsHeader rsHeader = new RsHeader();
		try {
			VectorTable postingMessageVector = this.getF_IN_PostingMessages();
			String postingRequestMessageType = this.getF_IN_PostingRequestMessageType();
			Boolean isBlocking = this.isF_IN_isBlocking();
			Date manualValueDate = this.getF_IN_manualValueDate();
			Time manualValueTime = this.getF_IN_manualValueTime();
			String settlementInstructionId = this.getF_IN_settlementInstructionId();
			Boolean suppressSchedulerIfForwardValued = this.isF_IN_suppressSchedulerIfForwardValued();
			String transactionID = this.getF_IN_transactionID();
			if(transactionID == null || transactionID.isEmpty()){
				transactionID = GUIDGen.getNewGUID();
			}
			String transactionReference = (String) postingMessageVector.getRowTags(0).get("TRANSACTIONREF");
			HashMap result = new HashMap();
			HashMap map = new HashMap();
			map.put("PostingMessages", postingMessageVector);
			map.put("PostingRequestMessageType", postingRequestMessageType);
			map.put("isBlocking", isBlocking);
			map.put("manualValueDate", manualValueDate);
			map.put("manualValueTime", manualValueTime);
			map.put("settlementInstructionId", settlementInstructionId);
			map.put("suppressSchedulerIfForwardValued", suppressSchedulerIfForwardValued);
			map.put("transactionID", transactionID);
			String srv = DataCenterCommonConstants.POSTING_MESSAGE_SERVICE;
			result = MFExecuter.executeMF(srv, BankFusionThreadLocal.getBankFusionEnvironment(), map);
			if (result != null) {
				setF_OUT_TransactionID(result.get("TransactionID").toString());
				txnStatus.setOverallStatus("S");
			}
			if (offlineMode) {
				txnStatus = getLogDetails(transactionReference);
			}
		}
		catch (BankFusionException e) {
			String eventCode = CommonConstants.EMPTY_STRING;
			SubCode subCode = new SubCode();
			if (e instanceof ReferralDialogException) {
				CommunicableTask task = null;
				ReferralDialogException rde = ((ReferralDialogException) e);
				Collection<IEvent> events = rde.getObjects();
				if (!events.toString().isEmpty()) {
					Iterator it = rde.getObjects().iterator();
					while (it.hasNext()) {
						task = (CommunicableTask) it.next();
						eventCode = (String) task.getDetails().get("EventNumber");
						subCode.setCode(eventCode);
						subCode.setDescription(CommonConstants.EMPTY_STRING);
						subCode.setFieldName(CommonConstants.EMPTY_STRING);
						subCode.setSeverity("E");
					}
					txnStatus.addCodes(subCode);
					txnStatus.setOverallStatus("E");
				}
			}
			 else if (e instanceof AbstractDialogException) {
					AbstractDialogException ade = (AbstractDialogException) e;
					Object parameterList = new Object();

					Collection<IEvent> events = ade.getEvents();
					if (!events.isEmpty()) {
						for (IEvent event : events) {
							String code = String.valueOf(event.getEventNumber());
							if (!code.isEmpty()) {
								eventCode = code;
								for (int j = 0; j < event.getDetails().length; j++) {
									EventParameters parameter = new EventParameters();
									parameterList = event.getDetails()[j];
									parameter.setEventParameterValue(parameterList
											.toString());
									subCode.addParameters(parameter);
								}
								break;
							}

						}
						subCode.setCode(eventCode);
						subCode.setDescription(ade.getEvents().iterator().next()
								.getMessage());
						subCode.setFieldName(CommonConstants.EMPTY_STRING);
						subCode.setSeverity(CBSConstants.ERROR);
						txnStatus.addCodes(subCode);
						txnStatus.setOverallStatus("E");
					}
			}
			else {
				LOG.error("Error while posting", e);
				IEvent errors = e.getEvents().iterator().next();
				int error = e.getEvents().iterator().next().getEventNumber();
				String errorCode = Integer.toString(error);
				LOG.error("Error While configuring the response Message" + " Error Code [" + errorCode + "] and Error Message is [" + e.getEvents().iterator().next().getMessage() + "]", e);
				Object parameterList = new Object();
				if(errors.getDetails()!= null && errors.getDetails().length !=0 ){
					for (int i = 0; i < errors.getDetails().length; i++) {
						EventParameters parameter = new EventParameters();
						parameterList = errors.getDetails()[i];
						parameter.setEventParameterValue(parameterList.toString());
						subCode.addParameters(parameter);
					}
				}
				subCode.setCode(errorCode);
				subCode.setDescription(e.getEvents().iterator().next().getMessage());
				subCode.setFieldName(CommonConstants.EMPTY_STRING);
				subCode.setSeverity(CBSConstants.ERROR);
				txnStatus.addCodes(subCode);
				txnStatus.setOverallStatus("E");
			}

            throw e;
		}
		finally {
			if (offlineMode) {
				txnStatus.setOverallStatus("S");
			}
			rsHeader.setStatus(txnStatus);
			setF_OUT_rsHeader(rsHeader);
		}
	}

	private MessageStatus getLogDetails(String transactionReference) {
		String eventCode = CommonConstants.EMPTY_STRING;
		MessageStatus txnLogStatus = new MessageStatus();
		txnLogStatus.setOverallStatus(CommonConstants.EMPTY_STRING);
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		ArrayList<String> queryparams = new ArrayList<String>();
		queryparams.add(transactionReference);
		List<SimplePersistentObject> logDetails = factory.executeGenericQuery(Query, queryparams, null, true);
		for (SimplePersistentObject obj : logDetails) {
			eventCode = obj.getDataMap().get("EVENTCODE").toString();
			SubCode subCode = new SubCode();
			if (eventCode != null) {
				subCode.setCode(eventCode);
				// txnLogStatus.setOverallStatus("E");
			}
			txnLogStatus.addCodes(subCode);
		}
		return txnLogStatus;
	}
}
