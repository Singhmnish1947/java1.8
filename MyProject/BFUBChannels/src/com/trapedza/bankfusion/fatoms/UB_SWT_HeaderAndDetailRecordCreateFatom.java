package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_HeaderAndDetailRecordCreate;

public class UB_SWT_HeaderAndDetailRecordCreateFatom extends
		AbstractUB_SWT_HeaderAndDetailRecordCreate {

	public UB_SWT_HeaderAndDetailRecordCreateFatom(BankFusionEnvironment env) {
		super(env);
	}

	public UB_SWT_HeaderAndDetailRecordCreateFatom() {
	}
	
	private static final String  DIRECTION_INWARDS = "I";

	public void process(BankFusionEnvironment env) throws BankFusionException {
			
		String currency = getF_IN_currency();
		String msgType = getF_IN_remMessageType();
		String partyIdentifier = getF_IN_partyIdentifier();
		if(!msgType.startsWith("MT")){
			msgType = "MT" + msgType;
		}
		
		BigDecimal contraAmt = getF_IN_contraAmount();
		if(contraAmt.signum() < 0){
			contraAmt = contraAmt.negate();
		}
		BigDecimal transactionAmt = getF_IN_transactionAmount();
		if(transactionAmt.signum() < 0){
			transactionAmt = transactionAmt.negate();
		}
		
		
		String code = "";
		String country = "";
		String partIden = "";
		if(!"".equalsIgnoreCase(partyIdentifier) && partyIdentifier != null && !partyIdentifier.isEmpty()){
			code = partyIdentifier.substring(0,4);
			country = partyIdentifier.substring(5,7);
			partIden = partyIdentifier.substring(8);
		}
		int var = 10;
		String remittanceID = PaymentSwiftUtils.getRemittanceId(currency, PaymentSwiftConstants.REMITTACNCE_OUTWARD);
		setF_OUT_remittanceID(remittanceID);
		setF_OUT_dateTime(new Timestamp(SystemInformationManager.getInstance().getBFBusinessDate().getTime()));
		setF_OUT_remMessageType(msgType);
		setF_OUT_partyIdentifierCode(code);
		setF_OUT_partyIdentifierCountry(country);
		setF_OUT_partyIdentifier(partIden);
		setF_OUT_contraAmount(contraAmt);
		setF_OUT_transactionAmount(transactionAmt);
	}

}