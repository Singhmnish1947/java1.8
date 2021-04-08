package com.misys.ub.swift.tellerRemittance.charges;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.paymentInititation.AccountIdentification4Choice;
import com.finastra.api.paymentInititation.ActiveOrHistoricCurrencyAndAmount;
import com.finastra.api.paymentInititation.AmountType4Choice;
import com.finastra.api.paymentInititation.BranchAndFinancialInstitutionIdentification5;
import com.finastra.api.paymentInititation.CashAccount24;
import com.finastra.api.paymentInititation.ChargeBearerType1Code;
import com.finastra.api.paymentInititation.Cheque7;
import com.finastra.api.paymentInititation.ClearingSystemIdentification2Choice;
import com.finastra.api.paymentInititation.ClearingSystemMemberIdentification2;
import com.finastra.api.paymentInititation.CreditTransferTransaction20;
import com.finastra.api.paymentInititation.CustomerCreditTransferInitiationV06;
import com.finastra.api.paymentInititation.EquivalentAmount2;
import com.finastra.api.paymentInititation.ExchangeRate1;
import com.finastra.api.paymentInititation.ExchangeRateType1Code;
import com.finastra.api.paymentInititation.FeeCalculationRequest;
import com.finastra.api.paymentInititation.FinancialInstitutionIdentification8;
import com.finastra.api.paymentInititation.GenericAccountIdentification1;
import com.finastra.api.paymentInititation.GenericPersonIdentification1;
import com.finastra.api.paymentInititation.GroupHeader48;
import com.finastra.api.paymentInititation.InitiationContext;
import com.finastra.api.paymentInititation.InitiationContext.SchmeNmEnum;
import com.finastra.api.paymentInititation.InstructionForCreditorAgent1;
import com.finastra.api.paymentInititation.OrganisationIdentification8;
import com.finastra.api.paymentInititation.Party11Choice;
import com.finastra.api.paymentInititation.PartyIdentification43;
import com.finastra.api.paymentInititation.PaymentIdentification1;
import com.finastra.api.paymentInititation.PaymentInstruction16;
import com.finastra.api.paymentInititation.PaymentMethod3Code;
import com.finastra.api.paymentInititation.PaymentTypeInformation19;
import com.finastra.api.paymentInititation.PersonIdentification5;
import com.finastra.api.paymentInititation.PostalAddress6;
import com.finastra.api.paymentInititation.Purpose2Choice;
import com.finastra.api.paymentInititation.ReferredDocumentInformation6;
import com.finastra.api.paymentInititation.RemittanceInformation10;
import com.finastra.api.paymentInititation.ServiceLevel8Choice;
import com.finastra.api.paymentInititation.StructuredRemittanceInformation12;
import com.finastra.api.paymentInititation.SupplementaryData1;
import com.finastra.api.paymentInititation.SupplementaryDataEnvelope1;
import com.finastra.api.paymentInititation.TellerCashIndSupplementaryData;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceHelper;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.ReadCustomerRs;
import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomer;
import bf.com.misys.cbs.types.swift.BeneficiaryInstitution;
import bf.com.misys.cbs.types.swift.DocumentUploadDtls;
import bf.com.misys.cbs.types.swift.IntermediaryDtls;
import bf.com.misys.cbs.types.swift.OrderingCustomer;
import bf.com.misys.cbs.types.swift.OrderingInstitution;
import bf.com.misys.cbs.types.swift.OrderingInstitutionDtl;
import bf.com.misys.cbs.types.swift.TextLines4;
import bf.com.misys.cbs.types.swift.TextLines6;

/**
 * @author Machamma.Devaiah
 *
 */
public class PrepareFeeCalculationRequest {
	private transient final static Log LOGGER = LogFactory.getLog(PrepareFeeCalculationRequest.class);

	public FeeCalculationRequest prepareFeeCalculationRequest(TellerRemittanceRq remittanceRq) {
		FeeCalculationRequest payInitRq = new FeeCalculationRequest();
		// 1st part
		payInitRq.setInitiationContext(prepareInitiationContext(remittanceRq));

		// 2nd part
		payInitRq.setCstmrCdtTrfInitn(prepareCustomerCreditTransfer(remittanceRq));

		return payInitRq;

	}

	/**
	 * Method Description:Prepare the InitiationContext
	 * 
	 * @param remittanceRq
	 * @return
	 */
	private InitiationContext prepareInitiationContext(TellerRemittanceRq remittanceRq) {
		InitiationContext initiationContext = new InitiationContext();
		// id
		initiationContext
				.setId(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
		// paymentMethod
		initiationContext.setSchmeNm(SchmeNmEnum.valueOf(remittanceRq.getTxnAdditionalDtls().getPaymentMethod()));
		initiationContext.setSaveOnError(Boolean.TRUE);
		// channelId
		initiationContext.setSourceId(BankFusionThreadLocal.getUserSession().getChannelID());

		return initiationContext;
	}

	private CustomerCreditTransferInitiationV06 prepareCustomerCreditTransfer(TellerRemittanceRq remittanceRq) {

		CustomerCreditTransferInitiationV06 cstmrCdtTrfInitn = new CustomerCreditTransferInitiationV06();

		// GroupHeader
		cstmrCdtTrfInitn.setGrpHdr(prepareGroupHeader(remittanceRq));

		List<PaymentInstruction16> pmtInf = new ArrayList<>();
		pmtInf.add(preparePaymentInstruction(remittanceRq));
		cstmrCdtTrfInitn.setPmtInf(pmtInf);

		return cstmrCdtTrfInitn;

	}

	/**
	 * Method Description:
	 * 
	 * @param remittanceRq
	 * @return
	 */
	private GroupHeader48 prepareGroupHeader(TellerRemittanceRq remittanceRq) {
		GroupHeader48 grpHdr = new GroupHeader48();

		grpHdr.setMsgId(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
			public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
				StringBuffer toFix = super.format(date, toAppendTo, pos);
				return toFix.insert(toFix.length() - 2, ':');
			};
		};
		grpHdr.setCreDtTm(dateFormat.format(new Date()));
		grpHdr.setNbOfTxs("1");
		// instructed amount
		grpHdr.setCtrlSum(
				remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getAmount());

		PartyIdentification43 initgPty = new PartyIdentification43();
		Party11Choice id = new Party11Choice();
		// customer Id
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID())) {
			PersonIdentification5 prvtId = new PersonIdentification5();
			List<GenericPersonIdentification1> othr = new ArrayList<>();
			GenericPersonIdentification1 genericPerson = new GenericPersonIdentification1();
			genericPerson.setId(RemittanceHelper
					.checkNullValue(remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID()));
			othr.add(genericPerson);
			prvtId.setOthr(othr);
			id.setPrvtId(prvtId);
		}

		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
				.getOrderingCustomer().getOrderingCustIdentifierCode())) {
			OrganisationIdentification8 orgId = new OrganisationIdentification8();
			// ordering customer bic
			orgId.setAnyBIC(remittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
					.getOrderingCustomer().getOrderingCustIdentifierCode());
			id.setOrgId(orgId);
		}

		// customer Id
		initgPty.setId(id);

		grpHdr.setInitgPty(initgPty);

		ReadCustomerRs custResponse = DataCenterCommonUtils
				.readCustomerDetails(remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID());
		if (custResponse != null
				&& StringUtils.isNotBlank(custResponse.getCustomerDetails().getCustBasicDetails().getShortName())) {
			initgPty.setNm(custResponse.getCustomerDetails().getCustBasicDetails().getShortName());
		}

		return grpHdr;
	}

	/**
	 * Method Description:
	 * 
	 * @param remittanceRq
	 * @return
	 */
	/**
	 * Method Description:
	 * 
	 * @param remittanceRq
	 * @return
	 */
	private PaymentInstruction16 preparePaymentInstruction(TellerRemittanceRq remittanceRq) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// PmtInf
		PaymentInstruction16 pmtInstruction = new PaymentInstruction16();
		pmtInstruction.setPmtInfId(StringUtils.isNotBlank(remittanceRq.getTxnAdditionalDtls().getRemittanceId())
				? remittanceRq.getTxnAdditionalDtls().getRemittanceId()
				: remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());

		pmtInstruction.setReqdExctnDt(
				sdf.format(remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getValueDate()));
		pmtInstruction.setPmtMtd(PaymentMethod3Code.TRF);
		pmtInstruction.setBtchBookg(Boolean.FALSE);

		if (StringUtils
				.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode())) {
			pmtInstruction.setChrgBr(getChargeCodeBearer(
					remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode()));
		}

		PaymentTypeInformation19 pmtTpInf = new PaymentTypeInformation19();
		ServiceLevel8Choice svcLvl = new ServiceLevel8Choice();
		// Bank Operation Code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
				.getBankToBankInfo().getBankOperationCode())) {
			svcLvl.setPrtry(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo()
					.getBankOperationCode());
		}
		// Bank Instruction Code
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
				.getBankToBankInfo().getBankInstructionCode())) {
			svcLvl.setCd(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getBankToBankInfo()
					.getBankInstructionCode());
		}
		pmtTpInf.setSvcLvl(svcLvl);

		pmtInstruction.setPmtTpInf(pmtTpInf);
		// debtorAccountId
		CashAccount24 dbtrAcct = new CashAccount24();
		AccountIdentification4Choice debitAccountId = new AccountIdentification4Choice();
		GenericAccountIdentification1 othr = new GenericAccountIdentification1();
		othr.setId(RemittanceHelper.checkNullValue(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getDebitPostingDtls().getDebitAccountId()));
		debitAccountId.setOthr(othr);
		dbtrAcct.setId(debitAccountId);

		// debitAccount Currency
		dbtrAcct.setCcy(RemittanceHelper.checkNullValue(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getTransactionDetails().getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode()));
		// account name
		String accountName = DataCenterCommonUtils
				.readAccount(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
						.getDebitAccountId())
				.getAccountDetails().getAccountInfo().getAcctBasicDetails().getAccountName();
		dbtrAcct.setNm(accountName);

		pmtInstruction.setDbtrAcct(dbtrAcct);

		// Ordering Customer and Institution 50A and 50F
		pmtInstruction.setDbtr(prepareOrderingCustomer(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getOrderingCustomerAndInstitution().getOrderingCustomer()));

		// ordering Institution 52A
		pmtInstruction.setDbtrAgt(prepareOrderingInstitution(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getOrderingCustomerAndInstitution().getOrderingInstitution()));

		// CdtTrfTxInf
		List<CreditTransferTransaction20> cdtTrfTxInf = new ArrayList<>();
		CreditTransferTransaction20 creditTransferTxn = new CreditTransferTransaction20();

		PaymentIdentification1 pmtId = new PaymentIdentification1();
		// messageReference
		pmtId.setEndToEndId(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
		// senderReference
		pmtId.setInstrId(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
		creditTransferTxn.setPmtId(pmtId);

		// instructed amount
		creditTransferTxn.setAmt(prepareAmountsBasedOnCurrency(remittanceRq));

		
		if(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditExchangeRate().compareTo(BigDecimal.ONE) > 0 ) {
			// TODO: Logic to be applied from User story
			// exchange Rate info
			ExchangeRate1 xchgRateInf = new ExchangeRate1();
			// deal reference
			if (StringUtils.isNotBlank(remittanceRq.getTxnAdditionalDtls().getDealReference())) {
				xchgRateInf.setCtrctId(remittanceRq.getTxnAdditionalDtls().getDealReference());
			}
			// TODO:LOGIC to derive this
			xchgRateInf.setRateTp(ExchangeRateType1Code.SPOT);
			xchgRateInf.setUnitCcy(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode());
			xchgRateInf.setXchgRate(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getCreditPostingDtls().getCreditExchangeRate());
			creditTransferTxn.setXchgRateInf(xchgRateInf);
		}

		// chequeNumber
		Cheque7 chqInstr = new Cheque7();
		chqInstr.setChqNb(remittanceRq.getTxnAdditionalDtls().getChequeNumber());
		creditTransferTxn.setChqInstr(chqInstr);

		// BeneficiaryCustomer
		// Account: CdtrAcct/Id/Othr/Id
		CashAccount24 cdtrAcct = new CashAccount24();
		AccountIdentification4Choice cdtrAcctId = new AccountIdentification4Choice();
		GenericAccountIdentification1 cdtrAcctOthr = new GenericAccountIdentification1();
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
				.getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifier())) {
			cdtrAcctOthr.setId(remittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
					.getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifier());
			cdtrAcctId.setOthr(cdtrAcctOthr);
			cdtrAcct.setId(cdtrAcctId);
			creditTransferTxn.setCdtrAcct(cdtrAcct);
			creditTransferTxn.setCdtr(prepareBeneficiaryCustomerDetails(remittanceRq.getInitiateSwiftMessageRqDtls()
					.getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer()));
		}

		// BeneficiaryInstitution
		creditTransferTxn.setCdtrAgt(prepareBeneficiaryInstitutionDetails(remittanceRq.getInitiateSwiftMessageRqDtls()
				.getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution()));

		// Intermediary Details
		creditTransferTxn.setIntrmyAgt1(prepareIntermediaryDetails(
				remittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()));

		// remittance Information
		creditTransferTxn.setRmtInf(prepareRemittanceInformation(remittanceRq));

		// Transaction Type Code (Tag 26) ____ /CdtTrfTxInf/Purp/Prtry
		if (StringUtils.isNotBlank(
				remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getTxnTypeCode_tag26())) {
			Purpose2Choice purp = new Purpose2Choice();
			purp.setPrtry(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getTxnTypeCode_tag26());
			creditTransferTxn.setPurp(purp);
		}

		// Sender to Receiver Information
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
				.getSenderToReceiverInfo().getTextLine1())) {
			creditTransferTxn.setInstrForCdtrAgt(concatSenderToRecieverInfo(
					remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSenderToReceiverInfo()));
		}

		// supplementatry data
		if (remittanceRq.getTxnAdditionalDtls().getFundingMode().equals("CASH")) {
			List<SupplementaryData1> splmtryDataList = new ArrayList<>();
			splmtryDataList.add(prepareSupplementaryData());
			creditTransferTxn.setSplmtryData(splmtryDataList);
		}

		// end
		cdtTrfTxInf.add(creditTransferTxn);
		pmtInstruction.setCdtTrfTxInf(cdtTrfTxInf);

		return pmtInstruction;
	}

	/**
	 * Method Description:
	 * 
	 * @param chargeCodeType
	 * @return
	 */
	private ChargeBearerType1Code getChargeCodeBearer(String chargeCodeType) {

		if (chargeCodeType.equals("OUR")) {
			return ChargeBearerType1Code.DEBT;
		} else if (chargeCodeType.equals("BEN")) {
			return ChargeBearerType1Code.CRED;

		} else if (chargeCodeType.equals("SHA")) {
			return ChargeBearerType1Code.SHAR;
		}

		return null;
	}

	/**
	 * Method Description:Remittance Information and Document Reference Number
	 * 
	 * @return
	 */
	private RemittanceInformation10 prepareRemittanceInformation(TellerRemittanceRq remittanceRq) {
		// Remittance information
		RemittanceInformation10 rmtInf = new RemittanceInformation10();
		if (StringUtils.isNotBlank(remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
				.getRemittanceInfo().getTextLine1())) {
			rmtInf.setUstrd(concatRemittanceInfo(
					remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo()));
		}

		// document reference number
		// CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/Nb
		if (null != remittanceRq.getInitiateSwiftMessageRqDtls().getDocumentUpload()) {
			DocumentUploadDtls[] vDocumentUploadArray = remittanceRq.getInitiateSwiftMessageRqDtls()
					.getDocumentUpload();
			if (vDocumentUploadArray.length > 0) {
				List<StructuredRemittanceInformation12> strd = new ArrayList<>();
				StructuredRemittanceInformation12 refNumber = new StructuredRemittanceInformation12();
				List<ReferredDocumentInformation6> rfrdDocInf = new ArrayList<>();

				for (DocumentUploadDtls doc : vDocumentUploadArray) {
					ReferredDocumentInformation6 docRefNumber = new ReferredDocumentInformation6();
					docRefNumber.setNb(doc.getReferenceNumber());
					rfrdDocInf.add(docRefNumber);
				}
				refNumber.setRfrdDocInf(rfrdDocInf);
				strd.add(refNumber);
				rmtInf.setStrd(strd);
			}
		}
		return rmtInf;
	}

	/**
	 * Method Description:Sender to Receiver information
	 * 
	 * @param remittanceInfo
	 * @return
	 */
	private List<InstructionForCreditorAgent1> concatSenderToRecieverInfo(TextLines6 senderToReceiverInfo) {
		List<InstructionForCreditorAgent1> instrForCdtrAgt = new ArrayList<>();

		if (StringUtils.isNotBlank(senderToReceiverInfo.getTextLine1())) {
			instrForCdtrAgt.add(getInstructionForCreditorAgent(senderToReceiverInfo.getTextLine1()));
		}

		if (StringUtils.isNotBlank(senderToReceiverInfo.getTextLine2())) {
			instrForCdtrAgt.add(getInstructionForCreditorAgent(senderToReceiverInfo.getTextLine2()));
		}
		if (StringUtils.isNotBlank(senderToReceiverInfo.getTextLine3())) {
			instrForCdtrAgt.add(getInstructionForCreditorAgent(senderToReceiverInfo.getTextLine3()));
		}
		if (StringUtils.isNotBlank(senderToReceiverInfo.getTextLine4())) {
			instrForCdtrAgt.add(getInstructionForCreditorAgent(senderToReceiverInfo.getTextLine4()));
		}
		if (StringUtils.isNotBlank(senderToReceiverInfo.getTextLine5())) {
			instrForCdtrAgt.add(getInstructionForCreditorAgent(senderToReceiverInfo.getTextLine5()));
		}
		if (StringUtils.isNotBlank(senderToReceiverInfo.getTextLine6())) {
			instrForCdtrAgt.add(getInstructionForCreditorAgent(senderToReceiverInfo.getTextLine6()));
		}
		return instrForCdtrAgt;
	}

	/**
	 * Method Description:
	 * 
	 * @param textLine
	 * @return
	 */
	private InstructionForCreditorAgent1 getInstructionForCreditorAgent(String textLine) {
		InstructionForCreditorAgent1 sendReceiveInfo = new InstructionForCreditorAgent1();
		sendReceiveInfo.setInstrInf(textLine);
		LOGGER.info("senderToReceiverInfo from request::" + textLine);
		return sendReceiveInfo;
	}

	/**
	 * Method Description:Concat Remittance Info 1,2 ,3 and 4
	 * 
	 * @param remittanceInfo
	 * @return
	 */
	private List<String> concatRemittanceInfo(TextLines4 remittanceInfo) {
		List<String> ustrd = new ArrayList<>();

		if (StringUtils.isNotBlank(remittanceInfo.getTextLine1())) {
			ustrd.add(remittanceInfo.getTextLine1());
		}
		if (StringUtils.isNotBlank(remittanceInfo.getTextLine2())) {
			ustrd.add(remittanceInfo.getTextLine2());
		}
		if (StringUtils.isNotBlank(remittanceInfo.getTextLine3())) {
			ustrd.add(remittanceInfo.getTextLine3());
		}
		if (StringUtils.isNotBlank(remittanceInfo.getTextLine4())) {
			ustrd.add(remittanceInfo.getTextLine4());
		}
		return ustrd;

	}

	/**
	 * Method Description: Ordering Customer and Institution 50A and 50F
	 *
	 * // BIC/BEI: Dbtr/Id/OrgId/AnyBIC /* Account No: DbtrAcct/Id/Othr/Id Name:
	 * Dbtr/Nm Address: Dbtr/PstlAdr/AdrLine
	 */

	private PartyIdentification43 prepareOrderingCustomer(OrderingCustomer orderingCustomer) {
		PartyIdentification43 dbtr = new PartyIdentification43();
		Party11Choice id = new Party11Choice();
		OrganisationIdentification8 orgId = new OrganisationIdentification8();
		if (StringUtils.isNotBlank(orderingCustomer.getOrderingCustIdentifierCode())) {
			orgId.setAnyBIC(RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustIdentifierCode()));
			id.setOrgId(orgId);
			dbtr.setId(id);
		}

		if (null != orderingCustomer.getOrderingCustDetails()
				&& StringUtils.isNotBlank(orderingCustomer.getOrderingCustDetails().getTextLine1())) {
			dbtr.setNm(orderingCustomer.getOrderingCustDetails().getTextLine1());
			dbtr.setPstlAdr(buildPostalAddress(orderingCustomer.getOrderingCustDetails()));
		}
		return dbtr;

	}

	/**
	 * Method Description: ordering Institution 52A ____BIC:
	 * DbtrAgt/FinInstnId/BICFI______________NCC Code (Clearing code):
	 * DbtrAgt/FinInstnId/ClrSysMmbId/ClrSysId/Cd_________NCC (Clearing Code
	 * value):DbtrAgt/FinInstnId/ClrSysMmbId/MmbId _____Name:
	 * DbtrAgt/FinInstnId/Nm___________ Address:DbtrAgt/FinInstnId/PstlAdr/AdrLine
	 * 
	 * @param orderingInstitution
	 * @return
	 */
	private BranchAndFinancialInstitutionIdentification5 prepareOrderingInstitution(
			OrderingInstitution orderingInstitution) {
		BranchAndFinancialInstitutionIdentification5 dbtrAgt = new BranchAndFinancialInstitutionIdentification5();
		FinancialInstitutionIdentification8 finInstnId = new FinancialInstitutionIdentification8();
		// BIC
		if (StringUtils.isNotBlank(orderingInstitution.getOrderingInstIdentifierCode())) {
			finInstnId.setBICFI(orderingInstitution.getOrderingInstIdentifierCode());
			dbtrAgt.setFinInstnId(finInstnId);
		}

		// NCC clearing code
		if (StringUtils.isNotBlank(orderingInstitution.getOrderingInstPartyClearingCode())) {
			ClearingSystemMemberIdentification2 clrSysMmbId = new ClearingSystemMemberIdentification2();
			ClearingSystemIdentification2Choice clrSysId = new ClearingSystemIdentification2Choice();
			clrSysId.setCd(RemittanceHelper.checkNullValue(orderingInstitution.getOrderingInstPartyClearingCode()));
			clrSysMmbId.setMmbId(
					RemittanceHelper.checkNullValue(orderingInstitution.getOrderingInstPartyIdentifierCode()));
			clrSysMmbId.setClrSysId(clrSysId);
			finInstnId.setClrSysMmbId(clrSysMmbId);
		}
		// set name and address
		if (null != orderingInstitution.getOrderingInstitutionDtl() && StringUtils
				.isNotBlank(orderingInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl1())) {
			finInstnId.setNm(orderingInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl1());
			finInstnId.setPstlAdr(buildOrderingInstitutionDtl(orderingInstitution.getOrderingInstitutionDtl()));
		}
		dbtrAgt.setFinInstnId(finInstnId);
		return dbtrAgt;
	}

	private PostalAddress6 buildOrderingInstitutionDtl(OrderingInstitutionDtl orderingInstitutionDtl) {
		/*
		 * <Nm>Creditor Name of Full length 35 chars</Nm> <PstlAdr> <TwnNm>TOWN</TwnNm>
		 * <Ctry>SG</Ctry> <AdrLine>Creditor Address line 1 full 35 chars</AdrLine>
		 * </PstlAdr>
		 * 
		 */
		PostalAddress6 pstlAdr = new PostalAddress6();
		if (StringUtils.isNotBlank(orderingInstitutionDtl.getOrderingInstitutionDtl2())) {
			List<String> adrLine = new ArrayList<>();
			adrLine.add(RemittanceHelper.get35CharacterTextLine(orderingInstitutionDtl.getOrderingInstitutionDtl2()));
			adrLine.add(RemittanceHelper.get35CharacterTextLine(orderingInstitutionDtl.getOrderingInstitutionDtl3()));
			adrLine.add(RemittanceHelper.get35CharacterTextLine(orderingInstitutionDtl.getOrderingInstitutionDtl4()));
			pstlAdr.setAdrLine(adrLine);
		}
		return pstlAdr;
	}

	/**
	 * Method Description: Beneficiary Customer BIC/BEI: Cdtr/Id/OrgId/AnyBIC____
	 * Name: Cdtr/Nm _____ Address: Cdtr/PstlAdr/AdrLine
	 * 
	 * @param beneficiaryCustomer
	 */
	private PartyIdentification43 prepareBeneficiaryCustomerDetails(BeneficiaryCustomer beneficiaryCustomer) {
		PartyIdentification43 cdtr = new PartyIdentification43();
		Party11Choice id = new Party11Choice();
		OrganisationIdentification8 orgId = new OrganisationIdentification8();
		if (StringUtils.isNotBlank(beneficiaryCustomer.getBeneficiaryCustIdentifierCode())) {
			orgId.setAnyBIC(beneficiaryCustomer.getBeneficiaryCustIdentifierCode());
			id.setOrgId(orgId);
			cdtr.setId(id);
		}
		// Name and address
		if (StringUtils.isNotBlank(beneficiaryCustomer.getBeneficiaryCustDetails().getTextLine1())) {
			cdtr.setNm(beneficiaryCustomer.getBeneficiaryCustDetails().getTextLine1());
			if (null != beneficiaryCustomer.getBeneficiaryCustDetails()) {
				cdtr.setPstlAdr(buildPostalAddress(beneficiaryCustomer.getBeneficiaryCustDetails()));
			}
		}
		return cdtr;
	}

	/**
	 * Method Description: Account With Institution A, C and D
	 * ____________________________________NCC
	 * code:CdtrAgt/FinInstnId/ClrSysMmbId/ClrSysId/Cd ____ NCC:
	 * CdtrAgt/FinInstnId/ClrSysMmbId/MmbId________ Name:CdtrAgt/FinInstnId/Nm
	 * _______Address: CdtrAgt/FinInstnId/PstlAdr/AdrLine ___BIC:
	 * CdtrAgt/FinInstnId/BICFI
	 * 
	 * 
	 * @param beneficiaryInstitution
	 */
	private BranchAndFinancialInstitutionIdentification5 prepareBeneficiaryInstitutionDetails(
			BeneficiaryInstitution beneficiaryInstitution) {
		BranchAndFinancialInstitutionIdentification5 cdtrAgt = new BranchAndFinancialInstitutionIdentification5();
		FinancialInstitutionIdentification8 finInstnId = new FinancialInstitutionIdentification8();
		ClearingSystemMemberIdentification2 clrSysMmbId = new ClearingSystemMemberIdentification2();
		ClearingSystemIdentification2Choice clrSysId = new ClearingSystemIdentification2Choice();
		if (StringUtils.isNotBlank(beneficiaryInstitution.getBeneficiaryInstPartyClearingCode())) {
			// NCC clearing code
			clrSysId.setCd(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstPartyClearingCode()));
			clrSysMmbId.setClrSysId(clrSysId);
		}

		// NCC code value
		if (StringUtils.isNotBlank(beneficiaryInstitution.getBeneficiaryInstPartyIdentifier())) {
			clrSysMmbId.setMmbId(beneficiaryInstitution.getBeneficiaryInstPartyIdentifier());
		}
		// BIC
		if (StringUtils.isNotBlank(beneficiaryInstitution.getBeneficiaryInstIdentifierCode())) {
			finInstnId.setBICFI(beneficiaryInstitution.getBeneficiaryInstIdentifierCode());
		}
		// name
		if (StringUtils.isNotBlank(RemittanceHelper
				.get35CharacterTextLine(beneficiaryInstitution.getBeneficiaryInstDetails().getTextLine1()))) {
			finInstnId.setNm(RemittanceHelper
					.get35CharacterTextLine(beneficiaryInstitution.getBeneficiaryInstDetails().getTextLine1()));
			// address
			if (null != beneficiaryInstitution.getBeneficiaryInstDetails()) {
				finInstnId.setPstlAdr(buildPostalAddress(beneficiaryInstitution.getBeneficiaryInstDetails()));
			}
		}
		finInstnId.setClrSysMmbId(clrSysMmbId);
		cdtrAgt.setFinInstnId(finInstnId);
		return cdtrAgt;

	}

	private PostalAddress6 buildPostalAddress(TextLines4 textLine) {
		/*
		 * <PstlAdr> <AdrLine>Creditor Address line 1 full 35 chars</AdrLine> </PstlAdr>
		 * 
		 */
		PostalAddress6 pstlAdr = new PostalAddress6();
		if (StringUtils.isNotBlank(textLine.getTextLine2())) {
			List<String> adrLine = new ArrayList<>();
			adrLine.add(RemittanceHelper.get35CharacterTextLine(textLine.getTextLine2()));
			adrLine.add(RemittanceHelper.get35CharacterTextLine(textLine.getTextLine3()));
			adrLine.add(RemittanceHelper.get35CharacterTextLine(textLine.getTextLine4()));
			pstlAdr.setAdrLine(adrLine);
		}
		return pstlAdr;
	}

	/**
	 * Method Description: IntermediaryInstitutionA 56A 56C 56D________NCC
	 * code:IntrmyAgt1/FinInstnId/ClrSysMmbId/ClrSysId/Cd ____
	 * NCC:IntrmyAgt1/FinInstnId/ClrSysMmbId/MmbId ______
	 * Name:IntrmyAgt1/FinInstnId/Nm _____ Address
	 * :IntrmyAgt1/FinInstnId/PstlAdr/AdrLine ______ BIC:
	 * IntrmyAgt1/FinInstnId/BICFI
	 * 
	 * @param intermediary
	 * @return
	 */
	private BranchAndFinancialInstitutionIdentification5 prepareIntermediaryDetails(IntermediaryDtls intermediary) {

		BranchAndFinancialInstitutionIdentification5 intrmyAgt1 = new BranchAndFinancialInstitutionIdentification5();
		FinancialInstitutionIdentification8 intrmyAgtfinInstnId = new FinancialInstitutionIdentification8();
		ClearingSystemMemberIdentification2 intrmyAgtClrSysMmbId = new ClearingSystemMemberIdentification2();
		ClearingSystemIdentification2Choice intrmyAgtClrSysId = new ClearingSystemIdentification2Choice();
		// clearing code
		if (StringUtils.isNotBlank(intermediary.getIntermediaryPartyIdfrClrngCode())) {
			intrmyAgtClrSysId.setCd(RemittanceHelper.checkNullValue(intermediary.getIntermediaryPartyIdfrClrngCode()));
			intrmyAgtClrSysMmbId.setClrSysId(intrmyAgtClrSysId);
		}
		// clearing code value
		if (StringUtils.isNotBlank(intermediary.getIntermediaryPartyIdentifier())) {
			intrmyAgtClrSysMmbId
					.setMmbId(RemittanceHelper.checkNullValue(intermediary.getIntermediaryPartyIdentifier()));
		}

		// BIC code
		if (StringUtils.isNotBlank(intermediary.getIntermediaryIdentiferCode())) {
			intrmyAgtfinInstnId.setBICFI(intermediary.getIntermediaryIdentiferCode());
		}

		// name and address
		if (StringUtils.isNotBlank(intermediary.getIntermediaryDetails().getTextLine1())) {
			intrmyAgtfinInstnId.setNm(
					RemittanceHelper.get35CharacterTextLine(intermediary.getIntermediaryDetails().getTextLine1()));
			if (null != intermediary.getIntermediaryDetails()) {
				intrmyAgtfinInstnId.setPstlAdr(buildPostalAddress(intermediary.getIntermediaryDetails()));
			}
		}
		intrmyAgtfinInstnId.setClrSysMmbId(intrmyAgtClrSysMmbId);
		intrmyAgt1.setFinInstnId(intrmyAgtfinInstnId);

		return intrmyAgt1;
	}

	/**
	 * Method Description:
	 * 
	 * @param remittanceRq
	 * @return
	 */
	private SupplementaryData1 prepareSupplementaryData() {
		SupplementaryData1 supplementaryData = new SupplementaryData1();
		// if CASH set cash Indication to true
		// TODO:TellerCashIndSupplementaryData is a class generated by you by modifiying
		// teh swagger
		// defn
		SupplementaryDataEnvelope1 envlp = new SupplementaryDataEnvelope1();
		TellerCashIndSupplementaryData document = new TellerCashIndSupplementaryData();
		document.setCashIndication("true");
		envlp.setDocument(document);
		supplementaryData.setEnvlp(envlp);

		return supplementaryData;
	}

	public static String encode(String s) {
		try {
			return java.net.URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);

		}
	}

	/**
	 * Method Description:
	 * 
	 * @param remittanceRq
	 * @return
	 */
	private AmountType4Choice prepareAmountsBasedOnCurrency(TellerRemittanceRq remittanceRq) {
		AmountType4Choice amount4Choice = new AmountType4Choice();
		ActiveOrHistoricCurrencyAndAmount instdAmt = new ActiveOrHistoricCurrencyAndAmount();
		EquivalentAmount2 eqvtAmt = new EquivalentAmount2();
		ActiveOrHistoricCurrencyAndAmount equivalentAmt = new ActiveOrHistoricCurrencyAndAmount();

		Currency instructedAmt = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getInstructedAmount();
		Currency debitAmount = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getDebitPostingDtls().getDebitAmount();
		Currency creditAmount = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getCreditPostingDtls().getCreditAmount();

		// if same as settement ccy
		if (remittanceRq.getTxnAdditionalDtls().getSameAsSettlementCcy()) {
			instdAmt.setAmt(instructedAmt.getAmount());
			instdAmt.setCcy(instructedAmt.getIsoCurrencyCode());
		} else {
			equivalentAmt.setAmt(instructedAmt.getAmount());
			equivalentAmt.setCcy(instructedAmt.getIsoCurrencyCode());
			eqvtAmt.setAmt(equivalentAmt);
			eqvtAmt.setCcyOfTrf(creditAmount.getIsoCurrencyCode());
		}

		// Instructed CCY = Settlement CCY
		// Instructed CCY= Debit account CCY
		if (instructedAmt.getIsoCurrencyCode().equals(creditAmount.getIsoCurrencyCode())
				&& instructedAmt.getIsoCurrencyCode().equals(debitAmount.getIsoCurrencyCode())) {
			instdAmt.setAmt(instructedAmt.getAmount());
			instdAmt.setCcy(instructedAmt.getIsoCurrencyCode());
		}

		// Instructed CCY != Settlement CCY
		// Instructed CCY= Debit account CCY
		if (!instructedAmt.getIsoCurrencyCode().equals(creditAmount.getIsoCurrencyCode())
				&& instructedAmt.getIsoCurrencyCode().equals(debitAmount.getIsoCurrencyCode())) {
			equivalentAmt.setAmt(instructedAmt.getAmount());
			equivalentAmt.setCcy(instructedAmt.getIsoCurrencyCode());

			eqvtAmt.setAmt(equivalentAmt);
			eqvtAmt.setCcyOfTrf(creditAmount.getIsoCurrencyCode());
		}

		// Instructed CCY = Settlement CCY
		// Instructed CCY != Debit account CCY
		if (instructedAmt.getIsoCurrencyCode().equals(creditAmount.getIsoCurrencyCode())
				&& !instructedAmt.getIsoCurrencyCode().equals(debitAmount.getIsoCurrencyCode())) {
			instdAmt.setAmt(instructedAmt.getAmount());
			instdAmt.setCcy(instructedAmt.getIsoCurrencyCode());
		}

		if (eqvtAmt.getAmt() != null && eqvtAmt.getAmt().getAmt().compareTo(BigDecimal.ZERO) > 0) {
			amount4Choice.setEqvtAmt(eqvtAmt);
		}

		if (instdAmt != null && instdAmt.getAmt() != null && instdAmt.getAmt().compareTo(BigDecimal.ZERO) > 0) {
			amount4Choice.setInstdAmt(instdAmt);
		}
		return amount4Choice;
	}

}
