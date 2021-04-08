package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.payment.swift.DBUtils.MessageHeaderTable;
import com.misys.ub.payment.swift.DBUtils.RemittanceDetailsTable;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.payment.swift.utils.UnblockNonStpTransactions;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_CancelRemittance;

/**
 * Class file for SWIFT remittance Cancellation
 * @author schowdh4
 *
 */
public class UB_SWT_CancelRemittance extends AbstractUB_SWT_CancelRemittance{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public UB_SWT_CancelRemittance(BankFusionEnvironment env) {
		super(env);
	}

	public UB_SWT_CancelRemittance() {
	}
	private transient final static Log logger = LogFactory.getLog(UB_SWT_CancelRemittance.class.getName());
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		String messageId = getF_IN_messageId();
		MessageHeaderTable messageHeaderTable = new MessageHeaderTable();
		RemittanceDetailsTable remittanceDetailsTable  = new RemittanceDetailsTable();
		IBOUB_SWT_RemittanceTable remittanceBO = remittanceDetailsTable.findByMessageId(messageId);
		if(PaymentSwiftConstants.CANCELLED.equalsIgnoreCase(remittanceBO.getF_UBREMITTANCESTATUS())){
			EventsHelper.handleEvent(PaymentSwiftConstants.REMITTANCE_ALREADY_CANCELLED, new Object[] {},
					new HashMap(), env);
		}else {
			messageHeaderTable.updateMessageHeader(messageId, PaymentSwiftConstants.CANCELLED, CommonConstants.EMPTY_STRING);
			if(PaymentSwiftConstants.NO.equals(remittanceBO.getF_UBISCASH()) && !PaymentSwiftConstants.PROCESSED.equals(remittanceBO.getF_UBREMITTANCESTATUS())){
				UnblockNonStpTransactions unblockNonStpTransactions  = new UnblockNonStpTransactions();
				unblockNonStpTransactions.unblockNonStpTransaction(remittanceBO.getF_UBBLOCKINGREFERENCE(), remittanceBO.getF_UBDEBITACCOUNT());
			}
			remittanceDetailsTable.updateRemittanceStatus(remittanceBO, PaymentSwiftConstants.CANCELLED);
			PaymentSwiftUtils paymentSwiftUtils = new PaymentSwiftUtils();
			if(isF_IN_raiseEventFlag())
				paymentSwiftUtils.raiseEvent(prepareEventMap(remittanceBO), PaymentSwiftConstants.EVT_CANCELLATION_IPAY);
		}
	}
	
	/**
	 * Prepare data for Raise event
	 * @param remittanceBO
	 * @return
	 */
	private Map<String, Object> prepareEventMap(IBOUB_SWT_RemittanceTable remittanceBO){
		Map<String, Object> eventMap = new HashMap<>();
		eventMap.put(PaymentSwiftConstants.EVENTCODE_FLD, PaymentSwiftConstants.EVENTCODEDESC_FLD);
		eventMap.put(PaymentSwiftConstants.EVENTHOSTTRANSID_FLD, remittanceBO.getF_UBTRANSACTIONID());
		eventMap.put(PaymentSwiftConstants.EVENTCHANNEL_FLD, remittanceBO.getF_UBCHANNELID());
		eventMap.put(PaymentSwiftConstants.EVENTCHANNELREF_FLD, remittanceBO.getF_UBMESSAGENUMBER());
		eventMap.put(PaymentSwiftConstants.EVENTPAYMENTREF_FLD, remittanceBO.getF_UBTRANSACTIONREFERENCE()); 
		if(PaymentSwiftConstants.PROCESSED.equals(remittanceBO.getF_UBREMITTANCESTATUS())){
			eventMap.put(PaymentSwiftConstants.EVENTREMITTANCEID, remittanceBO.getBoID());
		}
		else{
			eventMap.put(PaymentSwiftConstants.EVENTREMITTANCEID, CommonConstants.EMPTY_STRING);
		}
		logger.info("The Event Map for SWIFT Cancellation: "+eventMap);
		return eventMap;
	}
}
