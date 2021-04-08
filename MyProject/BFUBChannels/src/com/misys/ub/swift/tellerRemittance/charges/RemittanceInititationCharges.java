package com.misys.ub.swift.tellerRemittance.charges;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.paymentInititation.FeeCalculationRequest;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractRemittanceInititationCharges;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;

public class RemittanceInititationCharges extends AbstractRemittanceInititationCharges {

	private transient static final Log LOGGER = LogFactory.getLog(RemittanceInititationCharges.class);

	@SuppressWarnings("deprecation")
	public RemittanceInititationCharges(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {
		TellerRemittanceRq remittanceRq = getF_IN_tellerRemittanceRq();
		TxnfeesInformation txnFeeInfo = new TxnfeesInformation();
		String remittanceId = remittanceRq.getTxnAdditionalDtls().getRemittanceId();
		PrepareFeeCalculationRequest prepareRquestMsg = new PrepareFeeCalculationRequest();
		FeeCalculationRequest apiRequest = prepareRquestMsg.prepareFeeCalculationRequest(remittanceRq);
		if (StringUtils.isBlank(remittanceId)) {
			// REST API call
			txnFeeInfo = RemittanceChargesRestClient
					.feeCalculation(FeeRequestMapper.mapFeeCalculationRequest(apiRequest));
		} else {
			// View Charge
			txnFeeInfo = ViewRemittanceFees.getTxnfeesInformation(remittanceId);
		}

		setF_OUT_txnFeesInformation(txnFeeInfo);
	}


}
