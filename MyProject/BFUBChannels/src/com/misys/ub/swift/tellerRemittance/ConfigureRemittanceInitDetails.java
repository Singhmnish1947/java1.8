package com.misys.ub.swift.tellerRemittance;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.misys.ub.swift.remittance.process.SwiftRemittanceMessageHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.RoundToScale;
import com.trapedza.bankfusion.steps.refimpl.AbstractConfigureRemittanceInitDetails;

import bf.com.misys.cbs.msgs.v1r0.ReadRemittanceDtlsRs;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.swift.AdditionalFields;
import bf.com.misys.cbs.types.swift.CreditPostingDtls;
import bf.com.misys.cbs.types.swift.DebitPostingDtls;
import bf.com.misys.cbs.types.swift.InitiateSwiftMessage;
import bf.com.misys.cbs.types.swift.MessageDetails;
import bf.com.misys.cbs.types.swift.ReadRemittanceDtlsOutput;
import bf.com.misys.cbs.types.swift.RemittanceDetails;
import bf.com.misys.cbs.types.swift.TransactionDetails;
import bf.com.misys.cbs.types.swift.TxnAdditionalDtls;

public class ConfigureRemittanceInitDetails extends AbstractConfigureRemittanceInitDetails {

	@SuppressWarnings("deprecation")
	public ConfigureRemittanceInitDetails(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {

		ReadRemittanceDtlsRs remittanceRs = new ReadRemittanceDtlsRs();
		ReadRemittanceDtlsOutput readRemittanceDtlsOutput = new ReadRemittanceDtlsOutput();
	
		InitiateSwiftMessage initiateSwiftMessageRqDtls = new InitiateSwiftMessage();
		TransactionDetails transactionDetails = new TransactionDetails();
		CreditPostingDtls creditPostingDtls = new CreditPostingDtls();
		DebitPostingDtls debitPostingDtls = new DebitPostingDtls();

		// set Message Preference
		MessageDetails messageDetails = new MessageDetails();
		messageDetails.setMessagePreference("SERIAL");
		messageDetails.setValueDate(SystemInformationManager.getInstance().getBFBusinessDate());
		initiateSwiftMessageRqDtls.setMessageDetails(messageDetails);

		AdditionalFields additionalFields = new AdditionalFields();
		// set readOnlyAccount flag to true
		additionalFields.setAdditionalBoolean1(Boolean.TRUE);

		initiateSwiftMessageRqDtls.setAdditionalFields(additionalFields);

		// getting the debit MIS txn code
		String debitTransactionCode = SwiftRemittanceMessageHelper.getTransactioncodeFromModuleConfig("103");
		String creditTransactionCode = StringUtils.EMPTY;
		String drExRateType = CommonConstants.EMPTY_STRING;

		if (!StringUtils.isBlank(debitTransactionCode)) {
			drExRateType = getExchangeRateType(debitTransactionCode);
		} else {
			Object[] eventParams = new Object[] { "Transaction code for MT103" };
			EventsHelper.handleEvent(20020354, eventParams, null, env);
		}
		debitPostingDtls.setDebitExchangeRateType(StringUtils.isEmpty(drExRateType) ? "SPOT" : drExRateType);
		debitPostingDtls.setDebitExchangeRate(BigDecimal.ONE);

		// getting the credit MIS txn code
		IBOTransactionScreenControl txnScreenCtrl = SwiftRemittanceMessageHelper
				.getTransactionScreenControl(debitTransactionCode);
		if (txnScreenCtrl != null && !StringUtils.isBlank(txnScreenCtrl.getF_CONTRATRANSACTIONCODE())) {
			creditTransactionCode = txnScreenCtrl.getF_CONTRATRANSACTIONCODE();
		} else {
			EventsHelper.handleEvent(SwiftEventCodes.E_INVALID_CREDIT_TRANSACTION_CODE, new Object[] {}, null, env);
		}

		// setting the credit exchange rate type
		String crExRateType = getExchangeRateType(creditTransactionCode);
		Currency creditAmount = new Currency();

		creditAmount.setIsoCurrencyCode(SystemInformationManager.getInstance().getBaseCurrencyCode());
		creditAmount.setAmount(RoundToScale.run(BigDecimal.ZERO, creditAmount.getIsoCurrencyCode()));
		creditPostingDtls.setCreditAmount(creditAmount);
		creditPostingDtls.setCreditExchangeRateType(StringUtils.isEmpty(crExRateType) ? "SPOT" : crExRateType);
		creditPostingDtls.setCreditExchangeRate(BigDecimal.ONE);
		transactionDetails.setCreditPostingDtls(creditPostingDtls);

		Currency debitAmount = new Currency();
		debitAmount.setAmount(BigDecimal.ZERO);
		debitAmount.setIsoCurrencyCode(SystemInformationManager.getInstance().getBaseCurrencyCode());
		debitAmount.setAmount(RoundToScale.run(BigDecimal.ZERO, debitAmount.getIsoCurrencyCode()));
		debitPostingDtls.setDebitAmount(debitAmount);

		transactionDetails.setDebitPostingDtls(debitPostingDtls);

		Currency instructedAmount = new Currency();
		instructedAmount.setIsoCurrencyCode(SystemInformationManager.getInstance().getBaseCurrencyCode());
		instructedAmount.setAmount(RoundToScale.run(BigDecimal.ZERO, instructedAmount.getIsoCurrencyCode()));
		transactionDetails.setInstructedAmount(instructedAmount);

		RemittanceDetails remittanceDetails = new RemittanceDetails();
		remittanceDetails.setChargeCode(PaymentSwiftConstants.CHARGE_CODE_SHA);
		Currency chargeDetails = new Currency();
		chargeDetails.setIsoCurrencyCode(SystemInformationManager.getInstance().getBaseCurrencyCode());
		chargeDetails.setAmount(RoundToScale.run(BigDecimal.ZERO, chargeDetails.getIsoCurrencyCode()));
		remittanceDetails.setChargeDetails(chargeDetails);
		initiateSwiftMessageRqDtls.setRemittanceDetails(remittanceDetails);

		transactionDetails.setTransactionCode(debitTransactionCode);
		transactionDetails.setCreditTransactionCode(creditTransactionCode);

		// generating the sender reference
		long time = SystemInformationManager.getInstance().getBFBusinessDateTime().getTime();
		transactionDetails.setSenderReference("SW" + time);

		initiateSwiftMessageRqDtls.setTransactionDetails(transactionDetails);

		TxnAdditionalDtls txnAdditionalDtls = new TxnAdditionalDtls();
		// set Payment Method in txnData_paymentReference
		// set Funding Mode in txnData_remittanceMethod
		txnAdditionalDtls.setFundingMode("ACCOUNT");
		txnAdditionalDtls.setPaymentMethod("SWIFT");

		Currency consolidatedChargeAmount = new Currency();
		consolidatedChargeAmount.setIsoCurrencyCode(SystemInformationManager.getInstance().getBaseCurrencyCode());
		consolidatedChargeAmount.setAmount(RoundToScale.run(BigDecimal.ZERO, chargeDetails.getIsoCurrencyCode()));
		txnAdditionalDtls.setConsolidatedChargeAmount(consolidatedChargeAmount);

		readRemittanceDtlsOutput.setInitiateSwiftMessage(initiateSwiftMessageRqDtls);
		readRemittanceDtlsOutput.setTxnAdditionalDtls(txnAdditionalDtls);
		remittanceRs.setReadRemittanceDtlsOutput(readRemittanceDtlsOutput);
		setF_OUT_remittanceDetailsRs(remittanceRs);
	}

	/**
	 * @param transactionCode
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getExchangeRateType(String transactionCode) {
		String exchangeRateType = StringUtils.EMPTY;
		IBOMisTransactionCodes misTransactionCodes = SwiftRemittanceMessageHelper
				.getMisTransactionCodes(transactionCode);
		if (misTransactionCodes != null && !StringUtils.isBlank(misTransactionCodes.getF_EXCHANGERATETYPE())) {
			exchangeRateType = misTransactionCodes.getF_EXCHANGERATETYPE();
		} else {
			EventsHelper.handleEvent(SwiftEventCodes.E_BT_FEX_EXGRATE_TYPE_NOTFOUND_CB05, new Object[] {}, null,
					BankFusionThreadLocal.getBankFusionEnvironment());
		}

		return exchangeRateType;
	}

}
