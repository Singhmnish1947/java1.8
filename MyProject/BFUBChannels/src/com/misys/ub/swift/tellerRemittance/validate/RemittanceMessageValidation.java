package com.misys.ub.swift.tellerRemittance.validate;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.audit.runtime.impl.BankFusionThreadLocal;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.compliance.persistence.ComplianceFinderMethods;
import com.misys.ub.datacenter.DataCenterCommonConstants;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.financial.events.FinancialEventCodes;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.misys.ub.swift.remittance.validation.ValidationHelper;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceConstants;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.fatoms.UB_SWT_MaskFormatValidation;
import com.trapedza.bankfusion.features.ChequeDetailsFeature;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractRemittanceMessageValidation;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.header.RsHeader;

public class RemittanceMessageValidation extends AbstractRemittanceMessageValidation {

	private transient final static Log LOGGER = LogFactory.getLog(RemittanceMessageValidation.class);

	@SuppressWarnings("deprecation")
	public RemittanceMessageValidation(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {
		TellerRemittanceRq remittanceRq = getF_IN_tellerRemittanceRq();
		String fundingMode = remittanceRq.getTxnAdditionalDtls().getFundingMode();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("IN RemittanceMessageValidation" + RemittanceHelper.getXmlFromComplexType(remittanceRq));
		}

		switch (fundingMode) {
		case "CASH":
			validateCustomer(remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID());
			break;
		case "ACCOUNT":
			validateAccount(remittanceRq, env);
			break;
		case "CHEQUE":
			validateAccount(remittanceRq, env);
			validateCheque(remittanceRq, env);
			break;
		default:
			break;

		}

		//remittance field validation
		validateRemittanceFields(remittanceRq, env);

	}

	private void validateRemittanceFields(TellerRemittanceRq remittanceRq, BankFusionEnvironment env) {

		// beneficiary Customer Identifier code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
				.getBeneficiaryCustomer().getBeneficiaryCustIdentifierCode())) {
			String benBic = remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
					.getBeneficiaryCustomer().getBeneficiaryCustIdentifierCode();
			IBOBicCodes beneficiaryCustomerBic = ComplianceFinderMethods
					.findBicCodeDetailsByBiccodeId(benBic);
			if (beneficiaryCustomerBic == null) {
				CommonUtil.handleParameterizedEvent(RemittanceConstants.E_BIC_NOT_FOUND,
						new String[] { "Beneficairy Customer Identifier " + benBic });
			}
		}

		// beneficiary Institution Identifier code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
				.getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode())) {
			String benInstBic = remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
					.getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode();
			IBOBicCodes beneficiaryInstBic = ComplianceFinderMethods.findBicCodeDetailsByBiccodeId(benInstBic);
			if (beneficiaryInstBic == null) {
				CommonUtil.handleParameterizedEvent(RemittanceConstants.E_BIC_NOT_FOUND,
						new String[] { "Beneficairy Institution Identifier " + benInstBic });
			}
		}
			
		String benInstPartyIdentifierCode = !StringUtils.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstPartyIdentifier())
						? remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
								.getBeneficiaryInstitution().getBeneficiaryInstPartyIdentifier()
						: StringUtils.EMPTY;
		String benInstPartyClearingCode = !StringUtils
				.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
						.getBeneficiaryInstitution().getBeneficiaryInstPartyClearingCode())
								? remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
										.getBeneficiaryInstitution().getBeneficiaryInstPartyClearingCode()
								: StringUtils.EMPTY;

		validateNCCCodes(benInstPartyIdentifierCode, benInstPartyClearingCode,
				 MFInputOutPutKeys.BEN_INST_PARTY_PARAMETER, env);
		

		// ordering customer Identifier code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
				.getOrderingCustomer().getOrderingCustIdentifierCode())) {
			String ordCustBic = remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
					.getOrderingCustomer().getOrderingCustIdentifierCode();
			IBOBicCodes bicDetails = ComplianceFinderMethods.findBicCodeDetailsByBiccodeId(ordCustBic);
			if (bicDetails == null) {
				CommonUtil.handleParameterizedEvent(RemittanceConstants.E_BIC_NOT_FOUND,
						new String[] { "Ordering Customer Identifier " + ordCustBic });
			}
		}

		// ordering institution Identifier code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
				.getOrderingInstitution().getOrderingInstIdentifierCode())) {
			String ordInsBic = remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
					.getOrderingInstitution().getOrderingInstIdentifierCode();
			IBOBicCodes ordBic =  ComplianceFinderMethods.findBicCodeDetailsByBiccodeId(ordInsBic);
			if (ordBic == null) {
				CommonUtil.handleParameterizedEvent(RemittanceConstants.E_BIC_NOT_FOUND,
						new String[] { "Ordering Institution Identifier " + ordInsBic });
			}
		}
			
		String ordInstPartyIdentifierCode = !StringUtils.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getOrderingCustomerAndInstitution().getOrderingInstitution().getOrderingInstPartyIdentifierCode())
						? remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
								.getOrderingInstitution().getOrderingInstPartyIdentifierCode()
						: StringUtils.EMPTY;
		String ordInstPartyClearingCode = !StringUtils.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getOrderingCustomerAndInstitution().getOrderingInstitution().getOrderingInstPartyClearingCode())
						? remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
								.getOrderingInstitution().getOrderingInstPartyClearingCode()
						: StringUtils.EMPTY;

		validateNCCCodes(ordInstPartyIdentifierCode, ordInstPartyClearingCode, MFInputOutPutKeys.ORD_INST_PARTY_PARAMETER,
				env);				
		

		// intermediary identfier code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
				.getIntermediary().getIntermediaryIdentiferCode())) {
			String intrBicCode = remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
					.getIntermediaryIdentiferCode();
			IBOBicCodes intermediaryBic =  ComplianceFinderMethods.findBicCodeDetailsByBiccodeId(intrBicCode);
			if (intermediaryBic == null) {
				CommonUtil.handleParameterizedEvent(RemittanceConstants.E_BIC_NOT_FOUND,
						new String[] { "Intermediary Identifier " + intrBicCode });
			}
		}
		
		String intermediaryPartyIdentifierCode = !StringUtils.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getIntermediaryDetails().getIntermediary().getIntermediaryPartyIdentifier())
						? remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary().getIntermediaryPartyIdentifier()
						: StringUtils.EMPTY;
		String intermediaryPartyClearingCode = !StringUtils.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls().
				getIntermediaryDetails().getIntermediary().getIntermediaryPartyIdfrClrngCode())
						? remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary().getIntermediaryPartyIdfrClrngCode()
						: StringUtils.EMPTY;

		validateNCCCodes(intermediaryPartyIdentifierCode, intermediaryPartyClearingCode,MFInputOutPutKeys.INTERMEDIARY_PARTY_PARAMETER ,
				env);	

		// payTo identfier code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
				.getPayToIdentifierCode())) {
			String payToBic = remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
					.getPayToIdentifierCode();
			IBOBicCodes payToBicBo =  ComplianceFinderMethods.findBicCodeDetailsByBiccodeId(payToBic);
			if (payToBicBo == null) {
				CommonUtil.handleParameterizedEvent(RemittanceConstants.E_BIC_NOT_FOUND,
						new String[] { "Pay To Identifier " + payToBic });
			}
		}
		
		String payToPartyIdentifierCode = !StringUtils.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifier())
						? remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifier()
						: StringUtils.EMPTY;
		String payToPartyClearingCode = !StringUtils.isBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifierClearingCode())
						? remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifierClearingCode()
						: StringUtils.EMPTY;

		validateNCCCodes(payToPartyIdentifierCode, payToPartyClearingCode, MFInputOutPutKeys.PAY_TO_PARTY_PARAMETER,
				env);	

	}

	/**
	 * @param identifierCode
	 * @param nccClearingCode
	 * @param parameter
	 * @param remittanceDto
	 * @return
	 */
	private void validateNCCCodes(String identifierCode, String nccClearingCode, String parameter, BankFusionEnvironment env) {

		ValidationHelper validatehelper = new ValidationHelper();
		if (LOGGER.isInfoEnabled())
			LOGGER.info("IN  validate nccClearingCode:::" + nccClearingCode + " identifierCode:::" + identifierCode);

		UB_SWT_MaskFormatValidation maskVal = new UB_SWT_MaskFormatValidation(env);
		maskVal.setF_IN_MaskInput(validatehelper.appendNccCode(identifierCode, nccClearingCode));
		maskVal.process(env);
		if (!maskVal.isF_OUT_IsValidFormat()) {
			CommonUtil.handleParameterizedEvent(SwiftEventCodes.E_SWT_MASK_VALIDATION_UB,
					new String[] { parameter, nccClearingCode });
		}

	}

	private void validateAccount(TellerRemittanceRq remittanceRq, BankFusionEnvironment env) {
		// account validation
		String debitAccountId = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getDebitPostingDtls().getDebitAccountId();
		BigDecimal transactionAmount = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getDebitPostingDtls().getDebitAmount().getAmount();

		Long chequeNumber = StringUtils.isNotBlank(remittanceRq.getTxnAdditionalDtls().getChequeNumber())
				? Long.parseLong(remittanceRq.getTxnAdditionalDtls().getChequeNumber())
				: 0L;

		IBOAttributeCollectionFeature accValues = (IBOAttributeCollectionFeature) env.getFactory()
				.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, debitAccountId);

		BusinessValidatorBean validatorBean = new BusinessValidatorBean();

		if (accValues == null) {
			CommonUtil.handleParameterizedEvent(Integer.valueOf(DataCenterCommonConstants.E_ACCOUNT_NOT_FOUND),
					new String[] { debitAccountId });
		} else if (validatorBean.validateAccountClosed(accValues, transactionAmount, chequeNumber, env)) {
			LOGGER.error("Account : " + debitAccountId + " is Closed !");
			CommonUtil.handleParameterizedEvent(CommonsEventCodes.E_ACCOUNT_CLOSED, new String[] { debitAccountId });
		} else if (validatorBean.validateAccountStopped(accValues, transactionAmount, chequeNumber, env)) {
			LOGGER.error("Account : " + debitAccountId + " is Stopped !");
			CommonUtil.handleParameterizedEvent(CommonsEventCodes.E_ACCOUNT_STOPPED, new String[] { debitAccountId });
		} else if (validatorBean.validateAccountWrittenOff(debitAccountId, env)) {
			LOGGER.error("Account : " + debitAccountId + " is Written Off !");

			CommonUtil.handleParameterizedEvent(FinancialEventCodes.E_ACCOUNT_HAS_BEEN_WRITTEN_OFF,
					new String[] { debitAccountId });
		} else if (validatorBean.validateAccountStatus(accValues, env)) {
			LOGGER.error("Account Number : " + debitAccountId + " is Deactivated !");
			CommonUtil.handleParameterizedEvent(CommonsEventCodes.E_ACCOUNT_DEACTIVATED,
					new String[] { debitAccountId });
		}

		if (null != accValues) {
			validateCustomer(accValues.getF_CUSTOMERCODE());
		}

	}

	/**
	 * Method Description:
	 * 
	 * @param customerCode
	 */
	private void validateCustomer(String customerCode) {
		// customer validation
		if (DataCenterCommonUtils.readKYCStatusOfCustomer(customerCode)) {
			CommonUtil.handleParameterizedEvent(Integer.valueOf(DataCenterCommonConstants.E_CUSTOMER_IS_BLACKLISTED),
					new String[] { customerCode });
		}
	}

	/**
	 * Method Description:
	 * 
	 * @param remittanceRq
	 * @param env
	 */
	private void validateCheque(TellerRemittanceRq remittanceRq, BankFusionEnvironment env) {
		if (StringUtils.isNotBlank(remittanceRq.getTxnAdditionalDtls().getChequeNumber())) {
			// cheque validation
			ChequeDetailsFeature chqFeature = new ChequeDetailsFeature(env);
			chqFeature.setF_IN_ACCOUNTID(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getDebitPostingDtls().getDebitAccountId());
			chqFeature.setF_IN_CHEQUEAMOUNT(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getDebitPostingDtls().getDebitAmount().getAmount());
			chqFeature.setF_IN_CHEQUEDRAFTNUMBER(Long.parseLong(remittanceRq.getTxnAdditionalDtls().getChequeNumber()));
			chqFeature.setF_IN_ISSUEBANKCHEQUE(0);
			chqFeature.setF_IN_NOTONUSCHEQUES(0);
			chqFeature.process(env);
		}
	}

}
