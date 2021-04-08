package com.trapedza.bankfusion.fatoms;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;

import org.apache.commons.jxpath.JXPathContext;

import bf.com.misys.ub.types.interfaces.MessageHeader;
import bf.com.misys.ub.types.interfaces.SwiftMT103;
import bf.com.misys.ub.types.interfaces.Ub_MT103;

import com.misys.ub.swift.InstructionCode;
import com.misys.ub.swift.SendersCharges;
import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.StringToDate;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ConvertMT103ComplexType;


public class UB_SWT_ConvertMT103ComplexType extends AbstractUB_SWT_ConvertMT103ComplexType{

	public UB_SWT_ConvertMT103ComplexType() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public UB_SWT_ConvertMT103ComplexType(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		// TODO Auto-generated method stub
		/*
		 * FOR MT103
		 */

		HashMap hashmap = new HashMap();
		if (getF_IN_MessageType().equals("MT103")) {
			Object subType = null;
			Object subType1 = null;
			Ub_MT103 MT103 = getF_IN_Ub_MT103();
			SwiftMT103 MT103Details = MT103.getDetails();
			MessageHeader MT103header = MT103.getHeader();
			String BeneficiaryCustomemerOption = " ";

			UB_MT103 Object103 = new UB_MT103();
			Object103.setStp(MT103Details.getStp());
			Object103.setAccountWithInstitution(MT103Details
					.getAccountWithInstitution());
			Object103.setAccountWithInstOption(MT103Details
					.getAccountWithInstOption());
			Object103.setIntermediaryInstitution(MT103Details
					.getIntermediaryInstitution());
			Object103.setIntermediaryInstOption(MT103Details
					.getIntermediaryInstOption());
			Object103.setInternalRef(MT103Details.getInternalRef());
			Object103.setMessageId(MT103header.getMessageId1());
			Object103.setReceiver(MT103Details.getReceiver());
			Object103.setSender(MT103Details.getSender());
			Object103.setSendersCorrespOption(MT103Details
					.getSendersCorrespOption());
			Object103.setSendersCorrespondent(MT103Details
					.getSendersCorrespondent());
			Object103.setTdAmount(MT103Details.getTdAmount());
			Object103.setTdCurrencyCode(MT103Details.getTdCurrencyCode());
			Object103.setTdValueDate(MT103Details.getTdValueDate());
			Object103.setAction((MT103Details.getAction()));
			Object103.setBankOperationCode((MT103Details.getBankOperationCode()));
			Object103.setBeneficiaryCustomer((MT103Details.getBeneficiaryCustomer()));
			Object103.setBeneficiaryCustOption((MT103Details.getBeneficiaryCustOption()));
			Object103.setBranch((MT103Details.getBranch()));
			Object103.setDetailsOfCharges((MT103Details.getDetailsOfCharges()));
			Object103.setDisposalRef((MT103Details.getDisposalRef()));
			Object103.setExchangeRate((MT103Details.getExchangeRate()));
			Object103.setMessageType((MT103header.getMessageType()));
			Object103.setOrderingCustomer((MT103Details.getOrderingCustomer()));
			Object103.setInstructedAmount((MT103Details.getInstructedAmount()));
			Object103.setInstructedCurrency((MT103Details.getInstructedCurrency()));
			Object103.setMultipleHold((MT103Details.getMultipleHold()));
			Object103.setVerificationRequired(((MT103Details.getVerificationRequired())));
			Object103.setOrderingCustomerOption(((MT103Details.getOrderingCustomerOption())));
			Object103.setOrderingInstitution(((MT103Details.getOrderingInstitution())));
			Object103.setOrderInstitutionOption(((MT103Details.getOrderInstitutionOption())));
			Object103.setThirdReimbursementInstitution(((MT103Details.getThirdReimbursementInstitution())));
			Object103.setThirdReimbursementInstOption(((MT103Details.getThirdReimbursementInstOption())));
			Object103.setSendersReference(MT103Details.getSendersReference());
			Object103.setTransactionTypeCode(MT103Details.getTransactionTypeCode());
			Object103.setSendingInstitution(MT103Details.getSendingInstitution());
			Object103.setReceiversCorrespondent(MT103Details.getReceiversCorrespondent());
			Object103.setReceiversCorrespOption(MT103Details.getReceiversCorrespOption());
			Object103.setRemittanceInfo(MT103Details.getRemittanceInfo());
			Object103.setReceiversCharges(MT103Details.getReceiversCharges());
			Object103.setSenderToReceiverInfo(MT103Details.getSenderToReceiverInfo());
			Object103.setRegulatoryReporting(MT103Details.getRegulatoryReporting());
			Object103.setEnvelopeContents(MT103Details.getEnvelopeContents());


			JXPathContext context = JXPathContext.newContext(MT103);

			subType = context.getValue("/details/charges");
			if(subType!=null){
				subType = context.getValue("/details/charges/senderCharge");

				if (subType instanceof Object[]) {
					Object[] array = (Object[]) subType;
					for (int counter = 0; counter < array.length; counter++) {
						SendersCharges senderChargeDetails = new SendersCharges();
						senderChargeDetails.setSenderCharge((array[counter]).toString());
						Object103.addCharges(senderChargeDetails);
					}
				}
			}

			subType1=null;
			subType1 = context.getValue("/details/instruction");
			if(subType1!=null){
				subType1 = context.getValue("/details/instruction/instructionCode");

				if (subType1 instanceof Object[]) {
					Object[] array = (Object[]) subType1;
					for (int counter = 0; counter < array.length; counter++) {
						InstructionCode instructionCode = new InstructionCode();
						instructionCode.setInstructionCode(array[counter].toString());
						Object103.addInstruction(instructionCode);
					}

				}
			}

			
			
			setF_OUT_AccountWith(MT103Details
					.getAccountWithInstitution());
			setF_OUT_AccountWithOption(MT103Details
					.getAccountWithInstOption());
			setF_OUT_BeneficiaryCustomer(MT103Details
					.getBeneficiaryCustomer());

			if (MT103Details.getBeneficiaryCustOption()!= null && MT103Details.getBeneficiaryCustOption()!="") {
				setF_OUT_BeneficiaryCustomerOption(MT103Details
						.getBeneficiaryCustOption());
			}else {
				setF_OUT_BeneficiaryCustomerOption(CommonConstants.EMPTY_STRING);
			}

			setF_OUT_Intermediary(MT103Details
					.getIntermediaryInstitution());
			setF_OUT_IntermediaryOption(MT103Details
					.getIntermediaryInstOption());
			setF_OUT_MessageType(getF_IN_MessageType());
			setF_OUT_OrderingCustomer(MT103Details.getOrderingCustomer());
			setF_OUT_OrderingCustomerOption(MT103Details.getOrderingCustomerOption());
			setF_OUT_OrderingInstitution(MT103Details.getOrderingInstitution());
			setF_OUT_OrderInstitutionOption(MT103Details.getOrderInstitutionOption());
			setF_OUT_ReceiversCorrespondent(MT103Details
					.getReceiversCorrespondent());
			setF_OUT_ReceiversCorrespondentOption(MT103Details
					.getReceiversCorrespOption());
			setF_OUT_Sender(MT103Details.getSender());
			setF_OUT_SendersCorrespondent(MT103Details
					.getSendersCorrespondent());
			setF_OUT_SendersCorrespondentOption(MT103Details
					.getSendersCorrespOption());
			setF_OUT_ThirdReimbursementInstitution(MT103Details
					.getThirdReimbursementInstitution());
			setF_OUT_ThirdReimbursementInstitutionOption(MT103Details
					.getThirdReimbursementInstOption());
			setF_OUT_MT103NarrativeCodes(UB_SWT_Util.generateMT103ComplexType(Object103));
			setF_OUT_DetailsOfCharge(MT103Details.getDetailsOfCharges());
			setF_OUT_InterBankSettledAmount(MT103Details.getTdAmount());
			setF_OUT_InterBankSettledCurrency(MT103Details.getTdCurrencyCode());
			
			String ReceiversCharges = MT103Details.getReceiversCharges();
			if (ReceiversCharges != null
					&& ReceiversCharges.length() > 0) {
				ReceiversCharges = ReceiversCharges.substring(3);
				ReceiversCharges = ReceiversCharges
						.replaceAll(",", ".");
			}else{
		  		ReceiversCharges="0.00";
			}
			setF_OUT_ReceiversCharges(ReceiversCharges);
			setF_OUT_DealReference(MT103Details.getSendersReference());


		}
	}

}
