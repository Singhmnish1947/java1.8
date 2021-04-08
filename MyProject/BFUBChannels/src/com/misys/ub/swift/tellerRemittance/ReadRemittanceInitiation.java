package com.misys.ub.swift.tellerRemittance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.common.util.ApiUtil;
import com.google.common.base.Joiner;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.toolkit.expression.function.RoundToScale;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.DBUtils.DBUtils;
import com.misys.ub.swift.tellerRemittance.charges.ViewRemittanceFees;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOCountry;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_DOCUPLOADDTLS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOUDFEXTUB_SWT_RemittanceMessage;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractReadRemittanceInitiation;

import bf.com.misys.cbs.msgs.v1r0.ReadCustomerRs;
import bf.com.misys.cbs.msgs.v1r0.ReadRemittanceDtlsRq;
import bf.com.misys.cbs.msgs.v1r0.ReadRemittanceDtlsRs;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.swift.AdditionalFields;
import bf.com.misys.cbs.types.swift.BankToBankInfoDtls;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomer;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.BeneficiaryInstitution;
import bf.com.misys.cbs.types.swift.CreditPostingDtls;
import bf.com.misys.cbs.types.swift.DebitPostingDtls;
import bf.com.misys.cbs.types.swift.DocumentUploadDtls;
import bf.com.misys.cbs.types.swift.InitiateSwiftMessage;
import bf.com.misys.cbs.types.swift.IntermediaryDetails;
import bf.com.misys.cbs.types.swift.IntermediaryDtls;
import bf.com.misys.cbs.types.swift.MessageDetails;
import bf.com.misys.cbs.types.swift.OrderingCustomer;
import bf.com.misys.cbs.types.swift.OrderingCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.OrderingInstitution;
import bf.com.misys.cbs.types.swift.OrderingInstitutionDtl;
import bf.com.misys.cbs.types.swift.PayDtlsText;
import bf.com.misys.cbs.types.swift.PayToDtls;
import bf.com.misys.cbs.types.swift.ReadRemittanceDtlsOutput;
import bf.com.misys.cbs.types.swift.RegulatoryInformation;
import bf.com.misys.cbs.types.swift.RemittanceDetails;
import bf.com.misys.cbs.types.swift.TermsAndConditionsInfo;
import bf.com.misys.cbs.types.swift.TextLines4;
import bf.com.misys.cbs.types.swift.TextLines6;
import bf.com.misys.cbs.types.swift.TransactionDetails;
import bf.com.misys.cbs.types.swift.TxnAdditionalDtls;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;
import bf.com.misys.cbs.types.swift.UserDefFields;

@SuppressWarnings("serial")
public class ReadRemittanceInitiation extends AbstractReadRemittanceInitiation {

	private transient static final Log LOGGER = LogFactory.getLog(ReadRemittanceInitiation.class);

	private static final String documentListQuery = " WHERE " + IBOUB_SWT_DOCUPLOADDTLS.UBMESSAGEREFID + " = ?";

	private transient IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	@SuppressWarnings("deprecation")
	public ReadRemittanceInitiation(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {
		String remittanceId = StringUtils.EMPTY;
		AdditionalFields additionalFields = new AdditionalFields();
		boolean disableChargeButton = Boolean.FALSE;
		// input
		ReadRemittanceDtlsRq readRemittanceRq = getF_IN_readRemittanceRq();
		if (StringUtils.isNotBlank(readRemittanceRq.getReadRemittanceDtlsInput().getRemittanceID())) {
			remittanceId = readRemittanceRq.getReadRemittanceDtlsInput().getRemittanceID();
		} else {
			CommonUtil.handleParameterizedEvent(RemittanceConstants.E_CB_CMN_MANDATORY_ENTRY_CB05,
					new String[] { "Remittance Id " });
		}

		try {

			ReadRemittanceDtlsRs readRemittRs = new ReadRemittanceDtlsRs();
			ReadRemittanceDtlsOutput readRemittanceDtlsOutput = new ReadRemittanceDtlsOutput();
			readRemittRs.setReadRemittanceDtlsOutput(readRemittanceDtlsOutput);

			InitiateSwiftMessage initiateSwiftMessage = new InitiateSwiftMessage();
			TxnAdditionalDtls txnAdditionalDtls = new TxnAdditionalDtls();

			IBOUB_SWT_RemittanceMessage remObj = (IBOUB_SWT_RemittanceMessage) factory
					.findByPrimaryKey(IBOUB_SWT_RemittanceMessage.BONAME, remittanceId, true);

			// remittance id mandatory
			txnAdditionalDtls.setRemittanceId(remObj.getBoID());

			// beneficiary Customer and institution
			BeneficiaryCustomerAndInstitution beneficiaryCustomerAndInstitution = new BeneficiaryCustomerAndInstitution();

			// beneficiary Customer
			BeneficiaryCustomer beneficiaryCustomer = new BeneficiaryCustomer();
			beneficiaryCustomer
					.setBeneficiaryCustIdentifierCode(StringUtils.isNotBlank(remObj.getF_UBBENCUSTIDENTIFIERCODE())
							? remObj.getF_UBBENCUSTIDENTIFIERCODE()
							: StringUtils.EMPTY);
			beneficiaryCustomer
					.setBeneficiaryCustPartyIdentifier(StringUtils.isNotBlank(remObj.getF_UBBENCUSTPARTYIDENTIFIER())
							? remObj.getF_UBBENCUSTPARTYIDENTIFIER()
							: StringUtils.EMPTY);
			beneficiaryCustomer.setBeneficiaryCustPartyIdentifierCode(
					StringUtils.isNotBlank(remObj.getF_UBBENCUSTPARTYIDENCLCODE())
							? ApiUtil.getGenericCodeDesc(RemittanceConstants.BENEFICIARYCUSTOMER_GC,
									remObj.getF_UBBENCUSTPARTYIDENCLCODE())
							: StringUtils.EMPTY);

			TextLines4 beneficiaryCustDetails = new TextLines4();

			beneficiaryCustDetails.setTextLine1(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYCUSTINFO1()) ? remObj.getF_UBBENEFICIARYCUSTINFO1()
							: StringUtils.EMPTY);
			beneficiaryCustDetails.setTextLine2(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYCUSTINFO2()) ? remObj.getF_UBBENEFICIARYCUSTINFO2()
							: StringUtils.EMPTY);
			beneficiaryCustDetails.setTextLine3(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYCUSTINFO3()) ? remObj.getF_UBBENEFICIARYCUSTINFO3()
							: StringUtils.EMPTY);
			beneficiaryCustDetails.setTextLine4(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYCUSTINFO4()) ? remObj.getF_UBBENEFICIARYCUSTINFO4()
							: StringUtils.EMPTY);
			beneficiaryCustomer.setBeneficiaryCustDetails(beneficiaryCustDetails);
			beneficiaryCustomerAndInstitution.setBeneficiaryCustomer(beneficiaryCustomer);

			// beneficiary Institution

			BeneficiaryInstitution beneficiaryInstitution = new BeneficiaryInstitution();
			beneficiaryInstitution
					.setBeneficiaryInstIdentifierCode(StringUtils.isNotBlank(remObj.getF_UBBENINSTIDENTIFIERCODE())
							? remObj.getF_UBBENINSTIDENTIFIERCODE()
							: StringUtils.EMPTY);
			beneficiaryInstitution
					.setBeneficiaryInstPartyIdentifier(StringUtils.isNotBlank(remObj.getF_UBBENINSTPARTYIDENTIFIER())
							? remObj.getF_UBBENINSTPARTYIDENTIFIER()
							: StringUtils.EMPTY);
			beneficiaryInstitution
					.setBeneficiaryInstPartyClearingCode(StringUtils.isNotBlank(remObj.getF_UBBENINSTPARTYIDENCLCODE())
							? remObj.getF_UBBENINSTPARTYIDENCLCODE()
							: StringUtils.EMPTY);

			TextLines4 beneficiaryInstDetails = new TextLines4();

			beneficiaryInstDetails.setTextLine1(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYINSTINFO1()) ? remObj.getF_UBBENEFICIARYINSTINFO1()
							: StringUtils.EMPTY);
			beneficiaryInstDetails.setTextLine2(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYINSTINFO2()) ? remObj.getF_UBBENEFICIARYINSTINFO2()
							: StringUtils.EMPTY);
			beneficiaryInstDetails.setTextLine3(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYINSTINFO3()) ? remObj.getF_UBBENEFICIARYINSTINFO3()
							: StringUtils.EMPTY);
			beneficiaryInstDetails.setTextLine4(
					StringUtils.isNotBlank(remObj.getF_UBBENEFICIARYINSTINFO4()) ? remObj.getF_UBBENEFICIARYINSTINFO4()
							: StringUtils.EMPTY);

			beneficiaryInstitution.setBeneficiaryInstDetails(beneficiaryInstDetails);
			beneficiaryCustomerAndInstitution.setBeneficiaryInstitution(beneficiaryInstitution);
			initiateSwiftMessage.setBeneficiaryCustomerAndInstitution(beneficiaryCustomerAndInstitution);

			// intermediary and payto details

			IntermediaryDetails interPayToDetails = new IntermediaryDetails();

			// Pay to details
			PayToDtls payTo = new PayToDtls();
			payTo.setPayToIdentifierCode(
					StringUtils.isNotBlank(remObj.getF_UBPAYTOIDENTIFIERCODE()) ? remObj.getF_UBPAYTOIDENTIFIERCODE()
							: StringUtils.EMPTY);
			payTo.setPayToPartyIdentifier(
					StringUtils.isNotBlank(remObj.getF_UBPAYTOPARTYIDENTIFIER()) ? remObj.getF_UBPAYTOPARTYIDENTIFIER()
							: StringUtils.EMPTY);
			payTo.setPayToPartyIdentifierClearingCode(
					StringUtils.isNotBlank(remObj.getF_UBPAYTOPARTYIDENCLCODE()) ? remObj.getF_UBPAYTOPARTYIDENCLCODE()
							: StringUtils.EMPTY);

			PayDtlsText payToDetails = new PayDtlsText();
			payToDetails.setPayDtls1(StringUtils.isNotBlank(remObj.getF_UBPAYTOINFO1()) ? remObj.getF_UBPAYTOINFO1()
					: StringUtils.EMPTY);
			payToDetails.setPayDtls2(StringUtils.isNotBlank(remObj.getF_UBPAYTOINFO2()) ? remObj.getF_UBPAYTOINFO2()
					: StringUtils.EMPTY);
			payToDetails.setPayDtls3(StringUtils.isNotBlank(remObj.getF_UBPAYTOINFO3()) ? remObj.getF_UBPAYTOINFO3()
					: StringUtils.EMPTY);
			payToDetails.setPayDtls4(StringUtils.isNotBlank(remObj.getF_UBPAYTOINFO4()) ? remObj.getF_UBPAYTOINFO4()
					: StringUtils.EMPTY);
			payTo.setPayToDetails(payToDetails);
			interPayToDetails.setPayTo(payTo);

			// intermediary details
			IntermediaryDtls intermediary = new IntermediaryDtls();

			intermediary.setIntermediaryIdentiferCode(
					StringUtils.isNotBlank(remObj.getF_UBINTERMDRYIDENTCODE()) ? remObj.getF_UBINTERMDRYIDENTCODE()
							: StringUtils.EMPTY);
			intermediary.setIntermediaryPartyIdentifier(StringUtils.isNotBlank(remObj.getF_UBINTERMDRYPARTYIDENTCODE())
					? remObj.getF_UBINTERMDRYPARTYIDENTCODE()
					: StringUtils.EMPTY);
			intermediary
					.setIntermediaryPartyIdfrClrngCode(StringUtils.isNotBlank(remObj.getF_UBINTERMDRYPARTYIDENTCLCODE())
							? remObj.getF_UBINTERMDRYPARTYIDENTCLCODE()
							: StringUtils.EMPTY);
			TextLines4 textline = new TextLines4();

			textline.setTextLine1(
					StringUtils.isNotBlank(remObj.getF_UBINTERMEDIARYINFO1()) ? remObj.getF_UBINTERMEDIARYINFO1()
							: StringUtils.EMPTY);
			textline.setTextLine2(
					StringUtils.isNotBlank(remObj.getF_UBINTERMEDIARYINFO2()) ? remObj.getF_UBINTERMEDIARYINFO2()
							: StringUtils.EMPTY);
			textline.setTextLine3(
					StringUtils.isNotBlank(remObj.getF_UBINTERMEDIARYINFO3()) ? remObj.getF_UBINTERMEDIARYINFO3()
							: StringUtils.EMPTY);
			textline.setTextLine4(
					StringUtils.isNotBlank(remObj.getF_UBINTERMEDIARYINFO4()) ? remObj.getF_UBINTERMEDIARYINFO4()
							: StringUtils.EMPTY);

			intermediary.setIntermediaryDetails(textline);
			interPayToDetails.setIntermediary(intermediary);
			initiateSwiftMessage.setIntermediaryDetails(interPayToDetails);
			// ordering customer and institution

			OrderingCustomerAndInstitution orderingCustomerAndInstitution = new OrderingCustomerAndInstitution();
			// ordering customer
			OrderingCustomer orderingCustomer = new OrderingCustomer();

			// Identifier Code
			orderingCustomer.setOrderingCustIdentifierCode(
					StringUtils.isNotBlank(remObj.getF_UBORDCUSTINDENTIFER()) ? remObj.getF_UBORDCUSTINDENTIFER()
							: StringUtils.EMPTY);

			// Party Identifier (Account)
			orderingCustomer
					.setOrderingCustPartyIdentifierAcct(StringUtils.isNotBlank(remObj.getF_UBORDCUSTPARTYIDENTACCTYPE())
							? ApiUtil.getGenericCodeDesc(RemittanceConstants.ORDERINGCUSTOMER_GC,
									remObj.getF_UBORDCUSTPARTYIDENTACCTYPE())
							: StringUtils.EMPTY);
			// Party Identifier (Account) value
			orderingCustomer.setOrderingCustPartyIdentiferAcctValue(
					StringUtils.isNotBlank(remObj.getF_UBORDCUSTPARTYIDENTACC()) ? remObj.getF_UBORDCUSTPARTYIDENTACC()
							: StringUtils.EMPTY);

			// Ordering customer party identifier code
			orderingCustomer
					.setOrderingCustPartyIdentifierCode(StringUtils.isNotBlank(remObj.getF_UBORDCUSTPARTYIDENTCLCODE())
							? remObj.getF_UBORDCUSTPARTYIDENTCLCODE()
							: StringUtils.EMPTY);
			orderingCustomer
					.setOrderingCustPartyIdentifierCountry(StringUtils.isNotBlank(remObj.getF_UBORDCUSTPARTYCOUNTRY())
							? findCountryByCode(remObj.getF_UBORDCUSTPARTYCOUNTRY())
							: StringUtils.EMPTY);

			orderingCustomer
					.setOrderingCustPartyIdentiferValue(StringUtils.isNotBlank(remObj.getF_UBORDCUSTPARTYIDENTIFIER())
							? remObj.getF_UBORDCUSTPARTYIDENTIFIER()
							: StringUtils.EMPTY);

			TextLines4 orderingCustDetails = new TextLines4();
			orderingCustDetails.setTextLine1(
					StringUtils.isNotBlank(remObj.getF_UBORDCUSTOMERINFO1()) ? remObj.getF_UBORDCUSTOMERINFO1()
							: StringUtils.EMPTY);
			orderingCustDetails.setTextLine2(
					StringUtils.isNotBlank(remObj.getF_UBORDCUSTOMERINFO2()) ? remObj.getF_UBORDCUSTOMERINFO2()
							: StringUtils.EMPTY);
			orderingCustDetails.setTextLine3(
					StringUtils.isNotBlank(remObj.getF_UBORDCUSTOMERINFO3()) ? remObj.getF_UBORDCUSTOMERINFO3()
							: StringUtils.EMPTY);
			orderingCustDetails.setTextLine4(
					StringUtils.isNotBlank(remObj.getF_UBORDCUSTOMERINFO4()) ? remObj.getF_UBORDCUSTOMERINFO4()
							: StringUtils.EMPTY);

			orderingCustomer.setOrderingCustDetails(orderingCustDetails);

			orderingCustomerAndInstitution.setOrderingCustomer(orderingCustomer);

			// ordering institution

			OrderingInstitution orderingInstitution = new OrderingInstitution();

			orderingInstitution.setOrderingInstIdentifierCode(
					StringUtils.isNotBlank(remObj.getF_UBORDINSTIDENTIFIER()) ? remObj.getF_UBORDINSTIDENTIFIER()
							: StringUtils.EMPTY);
			orderingInstitution
					.setOrderingInstPartyClearingCode(StringUtils.isNotBlank(remObj.getF_UBORDINSTPARTYIDENTCLCODE())
							? remObj.getF_UBORDINSTPARTYIDENTCLCODE()
							: StringUtils.EMPTY);
			orderingInstitution
					.setOrderingInstPartyIdentifierCode(StringUtils.isNotBlank(remObj.getF_UBORDINSTPARTYIDENTIFIER())
							? remObj.getF_UBORDINSTPARTYIDENTIFIER()
							: StringUtils.EMPTY);

			OrderingInstitutionDtl orderingInstitutionDtl = new OrderingInstitutionDtl();
			orderingInstitutionDtl.setOrderingInstitutionDtl1(
					StringUtils.isNotBlank(remObj.getF_UBORDINSTITUTEINFO1()) ? remObj.getF_UBORDINSTITUTEINFO1()
							: StringUtils.EMPTY);
			orderingInstitutionDtl.setOrderingInstitutionDtl2(
					StringUtils.isNotBlank(remObj.getF_UBORDINSTITUTEINFO2()) ? remObj.getF_UBORDINSTITUTEINFO2()
							: StringUtils.EMPTY);
			orderingInstitutionDtl.setOrderingInstitutionDtl3(
					StringUtils.isNotBlank(remObj.getF_UBORDINSTITUTEINFO3()) ? remObj.getF_UBORDINSTITUTEINFO3()
							: StringUtils.EMPTY);
			orderingInstitutionDtl.setOrderingInstitutionDtl4(
					StringUtils.isNotBlank(remObj.getF_UBORDINSTITUTEINFO4()) ? remObj.getF_UBORDINSTITUTEINFO4()
							: StringUtils.EMPTY);
			orderingInstitution.setOrderingInstitutionDtl(orderingInstitutionDtl);
			orderingCustomerAndInstitution.setOrderingInstitution(orderingInstitution);
			initiateSwiftMessage.setOrderingCustomerAndInstitution(orderingCustomerAndInstitution);

			// remittance details
			RemittanceDetails remittanceDetails = new RemittanceDetails();

			Currency chargeDetails = new Currency();
			chargeDetails.setIsoCurrencyCode(StringUtils.isNotBlank(remObj.getF_UBPAYINGBANKCHARGECURRENCY())
					? remObj.getF_UBPAYINGBANKCHARGECURRENCY()
					: StringUtils.EMPTY);
			chargeDetails.setAmount(remObj.getF_UBPAYINGBANKCHARGE() != null
					? RoundToScale.run(remObj.getF_UBPAYINGBANKCHARGE(), chargeDetails.getIsoCurrencyCode())
					: BigDecimal.ZERO);

			remittanceDetails.setChargeDetails(chargeDetails);
			remittanceDetails.setChargeCode(remObj.getF_UBCHARGECODETYPE());
			remittanceDetails.setDescription(StringUtils.isNotBlank(remObj.getF_UBREMITTANCEDESCRIPTION())
					? remObj.getF_UBREMITTANCEDESCRIPTION()
					: StringUtils.EMPTY);
			remittanceDetails.setTxnTypeCode_tag26(
					StringUtils.isNotBlank(remObj.getF_UBTXNTYPECODETAG26()) ? remObj.getF_UBTXNTYPECODETAG26()
							: StringUtils.EMPTY);

			// bank to bank info details
			BankToBankInfoDtls bankToBankInfo = new BankToBankInfoDtls();
			bankToBankInfo.setBankInstructionCode(
					StringUtils.isNotBlank(remObj.getF_UBBANKINSTRUCTIONCODE1()) ? remObj.getF_UBBANKINSTRUCTIONCODE1()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankOperationCode(
					StringUtils.isNotBlank(remObj.getF_UBBANKOPERATIONCODE()) ? remObj.getF_UBBANKOPERATIONCODE()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankAddlInstrCode(
					StringUtils.isNotBlank(remObj.getF_UBBANKINSTRUCTIONCODE2()) ? remObj.getF_UBBANKINSTRUCTIONCODE2()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankToBankInfo1(
					StringUtils.isNotBlank(remObj.getF_UBBANKTOBANKINFO1()) ? remObj.getF_UBBANKTOBANKINFO1()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankToBankInfo2(
					StringUtils.isNotBlank(remObj.getF_UBBANKTOBANKINFO2()) ? remObj.getF_UBBANKTOBANKINFO2()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankToBankInfo3(
					StringUtils.isNotBlank(remObj.getF_UBBANKTOBANKINFO3()) ? remObj.getF_UBBANKTOBANKINFO3()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankToBankInfo4(
					StringUtils.isNotBlank(remObj.getF_UBBANKTOBANKINFO4()) ? remObj.getF_UBBANKTOBANKINFO4()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankToBankInfo5(
					StringUtils.isNotBlank(remObj.getF_UBBANKTOBANKINFO5()) ? remObj.getF_UBBANKTOBANKINFO5()
							: StringUtils.EMPTY);
			bankToBankInfo.setBankToBankInfo6(
					StringUtils.isNotBlank(remObj.getF_UBBANKTOBANKINFO6()) ? remObj.getF_UBBANKTOBANKINFO6()
							: StringUtils.EMPTY);
			remittanceDetails.setBankToBankInfo(bankToBankInfo);

			// remittance info details
			TextLines4 remittanceInfo = new TextLines4();
			remittanceInfo.setTextLine1(
					StringUtils.isNotBlank(remObj.getF_UBREMITTANCEINFO1()) ? remObj.getF_UBREMITTANCEINFO1()
							: StringUtils.EMPTY);
			remittanceInfo.setTextLine2(
					StringUtils.isNotBlank(remObj.getF_UBREMITTANCEINFO2()) ? remObj.getF_UBREMITTANCEINFO2()
							: StringUtils.EMPTY);
			remittanceInfo.setTextLine3(
					StringUtils.isNotBlank(remObj.getF_UBREMITTANCEINFO3()) ? remObj.getF_UBREMITTANCEINFO3()
							: StringUtils.EMPTY);
			remittanceInfo.setTextLine4(
					StringUtils.isNotBlank(remObj.getF_UBREMITTANCEINFO4()) ? remObj.getF_UBREMITTANCEINFO4()
							: StringUtils.EMPTY);
			remittanceDetails.setRemittanceInfo(remittanceInfo);

			// sender to receiver info details
			TextLines6 senderToReceiverInfo = new TextLines6();
			senderToReceiverInfo.setTextLine1(StringUtils.isNotBlank(remObj.getF_UBSENDERTORECIEVERINFO1())
					? remObj.getF_UBSENDERTORECIEVERINFO1()
					: StringUtils.EMPTY);
			senderToReceiverInfo.setTextLine2(StringUtils.isNotBlank(remObj.getF_UBSENDERTORECIEVERINFO2())
					? remObj.getF_UBSENDERTORECIEVERINFO2()
					: StringUtils.EMPTY);
			senderToReceiverInfo.setTextLine3(StringUtils.isNotBlank(remObj.getF_UBSENDERTORECIEVERINFO3())
					? remObj.getF_UBSENDERTORECIEVERINFO3()
					: StringUtils.EMPTY);
			senderToReceiverInfo.setTextLine4(StringUtils.isNotBlank(remObj.getF_UBSENDERTORECIEVERINFO4())
					? remObj.getF_UBSENDERTORECIEVERINFO4()
					: StringUtils.EMPTY);
			senderToReceiverInfo.setTextLine5(StringUtils.isNotBlank(remObj.getF_UBSENDERTORECIEVERINFO5())
					? remObj.getF_UBSENDERTORECIEVERINFO5()
					: StringUtils.EMPTY);
			senderToReceiverInfo.setTextLine6(StringUtils.isNotBlank(remObj.getF_UBSENDERTORECIEVERINFO6())
					? remObj.getF_UBSENDERTORECIEVERINFO6()
					: StringUtils.EMPTY);
			remittanceDetails.setSenderToReceiverInfo(senderToReceiverInfo);

			// terms and condition info details
			TermsAndConditionsInfo termsAndConditionsInfo = new TermsAndConditionsInfo();
			termsAndConditionsInfo.setTAndCInfoLine1(StringUtils.isNotBlank(remObj.getF_UBTERMANDCONDITIONINF01())
					? remObj.getF_UBTERMANDCONDITIONINF01()
					: StringUtils.EMPTY);
			termsAndConditionsInfo.setTAndCInfoLine2(StringUtils.isNotBlank(remObj.getF_UBTERMANDCONDITIONINF02())
					? remObj.getF_UBTERMANDCONDITIONINF02()
					: StringUtils.EMPTY);
			termsAndConditionsInfo.setTAndCInfoLine3(StringUtils.isNotBlank(remObj.getF_UBTERMANDCONDITIONINF03())
					? remObj.getF_UBTERMANDCONDITIONINF03()
					: StringUtils.EMPTY);
			termsAndConditionsInfo.setTAndCInfoLine4(StringUtils.isNotBlank(remObj.getF_UBTERMANDCONDITIONINF04())
					? remObj.getF_UBTERMANDCONDITIONINF04()
					: StringUtils.EMPTY);
			termsAndConditionsInfo.setTAndCInfoLine5(StringUtils.isNotBlank(remObj.getF_UBTERMANDCONDITIONINF05())
					? remObj.getF_UBTERMANDCONDITIONINF05()
					: StringUtils.EMPTY);
			termsAndConditionsInfo.setTAndCInfoLine6(StringUtils.isNotBlank(remObj.getF_UBTERMANDCONDITIONINF06())
					? remObj.getF_UBTERMANDCONDITIONINF06()
					: StringUtils.EMPTY);
			remittanceDetails.setTermsAndConditionsInfo(termsAndConditionsInfo);
			initiateSwiftMessage.setRemittanceDetails(remittanceDetails);

			// transaction details

			TransactionDetails transactionDetails = new TransactionDetails();

			// debit posting details
			DebitPostingDtls debitPostingDtls = new DebitPostingDtls();

			Currency debitAmount = new Currency();
			debitAmount.setIsoCurrencyCode(remObj.getF_UBDRACCCURRENCY());
			debitAmount.setAmount(RoundToScale.run(remObj.getF_UBDEBITAMOUNT(), debitAmount.getIsoCurrencyCode()));
			debitPostingDtls.setDebitAmount(debitAmount);
			debitPostingDtls.setDebitAccountId(
					StringUtils.isNotBlank(remObj.getF_UBDEBITACCOUNT()) ? remObj.getF_UBDEBITACCOUNT()
							: StringUtils.EMPTY);
			debitPostingDtls.setDebitExchangeRate(remObj.getF_UBEXCHANGERATEDR());
			debitPostingDtls.setDebitExchangeRateType(
					StringUtils.isNotBlank(remObj.getF_UBEXCHANGERATETYPEDR()) ? remObj.getF_UBEXCHANGERATETYPEDR()
							: StringUtils.EMPTY);
			transactionDetails.setDebitPostingDtls(debitPostingDtls);

			// credit posting details
			CreditPostingDtls creditPostingDtls = new CreditPostingDtls();
			creditPostingDtls.setCreditAccountId(
					StringUtils.isNotBlank(remObj.getF_UBSETTLEMENTACCOUNT()) ? remObj.getF_UBSETTLEMENTACCOUNT()
							: StringUtils.EMPTY);
			Currency creditAmount = new Currency();
			creditAmount.setIsoCurrencyCode(remObj.getF_UBSETTLEMENTAMTCURRENCY());
			creditAmount
					.setAmount(RoundToScale.run(remObj.getF_UBSETTLEMENTAMOUNT(), creditAmount.getIsoCurrencyCode()));
			creditPostingDtls.setCreditAmount(creditAmount);
			creditPostingDtls.setCreditExchangeRate(remObj.getF_UBEXCHANGERATECR());
			creditPostingDtls.setCreditExchangeRateType(remObj.getF_UBEXCHANGERATETYPECR());
			transactionDetails.setCreditPostingDtls(creditPostingDtls);

			Currency instructedAmount = new Currency();
			instructedAmount.setIsoCurrencyCode(remObj.getF_UBINSTRUCTEDAMTCURRENCY());
			instructedAmount
					.setAmount(RoundToScale.run(remObj.getF_UBINSTRUCTEDAMT(), instructedAmount.getIsoCurrencyCode()));
			transactionDetails.setInstructedAmount(instructedAmount);

			transactionDetails.setNarration(
					StringUtils.isNotBlank(remObj.getF_UBDEBITNARRATION()) ? remObj.getF_UBDEBITNARRATION()
							: StringUtils.EMPTY);

			transactionDetails.setSenderReference(
					StringUtils.isNotBlank(remObj.getF_UBSENDERREFERENCE()) ? remObj.getF_UBSENDERREFERENCE()
							: StringUtils.EMPTY);

			transactionDetails.setTransactionCode(
					StringUtils.isNotBlank(remObj.getF_UBDRTXNCODE()) ? remObj.getF_UBDRTXNCODE() : StringUtils.EMPTY);
			transactionDetails.setCreditTransactionCode(
					StringUtils.isNotBlank(remObj.getF_UBCRTXNCODE()) ? remObj.getF_UBCRTXNCODE() : StringUtils.EMPTY);
			initiateSwiftMessage.setTransactionDetails(transactionDetails);

			// message details

			MessageDetails messageDetails = new MessageDetails();
			messageDetails
					.setCustomerID(StringUtils.isNotBlank(remObj.getF_UBFROMCUSTOMER()) ? remObj.getF_UBFROMCUSTOMER()
							: StringUtils.EMPTY);

			ReadCustomerRs custResponse = DataCenterCommonUtils.readCustomerDetails(remObj.getF_UBFROMCUSTOMER());
			if (custResponse != null) {
				messageDetails.setCustomerName(custResponse.getCustomerDetails().getCustBasicDetails().getShortName());
			}

			messageDetails
					.setMessagePreference(StringUtils.isNotBlank(remObj.getF_UBMESSAGEPREFERENCE())
							? ApiUtil.getGenericCodeDesc(RemittanceConstants.MSGPREFERENCE_GC,
									remObj.getF_UBMESSAGEPREFERENCE())
							: StringUtils.EMPTY);
			messageDetails.setValueDate(remObj.getF_UBVALUEDATE());

			initiateSwiftMessage.setMessageDetails(messageDetails);

			// regulatory details

			RegulatoryInformation regulator = new RegulatoryInformation();

			regulator.setKycDetails(StringUtils.isNotBlank(remObj.getF_UBKYCDETAILS()) ? remObj.getF_UBKYCDETAILS()
					: StringUtils.EMPTY);
			// Purpose Of Remittance
			regulator.setPurposeOfRemittance(StringUtils.isNotBlank(remObj.getF_UBPURPOSEOFREMITTANCE())
					? ApiUtil.getGenericCodeDesc(RemittanceConstants.PURPOSEOFREMITTANCE_GC,
							remObj.getF_UBPURPOSEOFREMITTANCE())
					: StringUtils.EMPTY);
			// Regulatory Documents
			regulator.setRegulatoryDocuments(StringUtils.isNotBlank(remObj.getF_UBREGULATORYREQDOCUMENT())
					? ApiUtil.getGenericCodeDesc(RemittanceConstants.DOCTYPE_GC, remObj.getF_UBREGULATORYREQDOCUMENT())
					: StringUtils.EMPTY);
			regulator.setTradeDetails(
					StringUtils.isNotBlank(remObj.getF_UBTRADEDETAILINFO()) ? remObj.getF_UBTRADEDETAILINFO()
							: StringUtils.EMPTY);
			initiateSwiftMessage.setRegulatoryInformation(regulator);

			// txn additonal details
			txnAdditionalDtls
					.setChequeNumber(remObj.getF_UBCHEQUENUMBER() != null ? String.valueOf(remObj.getF_UBCHEQUENUMBER())
							: StringUtils.EMPTY);

			Currency consolidatedcharge = new Currency();
			consolidatedcharge.setIsoCurrencyCode(remObj.getF_UBCHARGECURRENCY());
			consolidatedcharge
					.setAmount(RoundToScale.run(remObj.getF_UBCHARGES(), consolidatedcharge.getIsoCurrencyCode()));
			txnAdditionalDtls.setConsolidatedChargeAmount(consolidatedcharge);
			txnAdditionalDtls.setDealReference(
					StringUtils.isNotBlank(remObj.getF_UBDEALREFERENCE()) ? remObj.getF_UBDEALREFERENCE()
							: StringUtils.EMPTY);

			txnAdditionalDtls.setMonetaryInstrumentId(
					StringUtils.isNotBlank(remObj.getF_UBMONETARYINSTRUMENTID()) ? remObj.getF_UBMONETARYINSTRUMENTID()
							: StringUtils.EMPTY);

			txnAdditionalDtls.setFundingMode(StringUtils.isNotBlank(remObj.getF_UBFUNDINGMODE())
					? ApiUtil.getGenericCodeDesc(RemittanceConstants.TELREM_FUNDMODE_GC, remObj.getF_UBFUNDINGMODE())
					: StringUtils.EMPTY);

			txnAdditionalDtls.setHoldingLocationId(
					StringUtils.isNotBlank(remObj.getF_UBHOLDINGLOCATIONID()) ? remObj.getF_UBHOLDINGLOCATIONID()
							: StringUtils.EMPTY);

			txnAdditionalDtls.setPaymentMethod(StringUtils.isNotBlank(remObj.getF_UBPAYMENTMETHOD())
					? ApiUtil.getGenericCodeDesc(RemittanceConstants.TELREM_PAYMETHOD_GC, remObj.getF_UBPAYMENTMETHOD())
					: StringUtils.EMPTY);

			txnAdditionalDtls.setRemittanceStatus(StringUtils.isNotBlank(remObj.getF_UBREMITTANCESTATUS()) ? ApiUtil
					.getGenericCodeDesc(RemittanceConstants.TELREM_REMIT_STATUS_GC, remObj.getF_UBREMITTANCESTATUS())
					: StringUtils.EMPTY);
			txnAdditionalDtls.setSameAsSettlementCcy(
					"Y".equals(remObj.getF_UBSAMEASSETTLEMENTCCY()) ? Boolean.TRUE : Boolean.FALSE);

			txnAdditionalDtls
					.setUetr(StringUtils.isNotBlank(remObj.getF_UBUETR()) ? remObj.getF_UBUETR() : StringUtils.EMPTY);

			// charges
			TxnfeesInformation txnFeeInfo = ViewRemittanceFees.getTxnfeesInformation(remittanceId);
			if (txnFeeInfo != null) {
				readRemittanceDtlsOutput.setTxnfeesInformation(txnFeeInfo);
				if (!txnFeeInfo.getIsWaived() && txnAdditionalDtls.getConsolidatedChargeAmount().getAmount()
						.compareTo(BigDecimal.ZERO) == 0) {
					disableChargeButton = Boolean.TRUE;
				}
			}
			additionalFields.setAdditionalBoolean1(disableChargeButton);
			initiateSwiftMessage.setAdditionalFields(additionalFields);

			// read UDF
			readRemittanceDtlsOutput.setUserDefinedFields(readRemittanceMessageUDF(remittanceId));

			// document upload
			List<DocumentUploadDtls> documentSavedIdList = readDocumentDetails(remittanceId);
			if (null != documentSavedIdList && !documentSavedIdList.isEmpty()) {
				for (DocumentUploadDtls vDocumentUpload : documentSavedIdList) {
					initiateSwiftMessage.addDocumentUpload(vDocumentUpload);
				}
			}

			readRemittanceDtlsOutput.setInitiateSwiftMessage(initiateSwiftMessage);
			readRemittanceDtlsOutput.setTxnAdditionalDtls(txnAdditionalDtls);
			readRemittRs.setReadRemittanceDtlsOutput(readRemittanceDtlsOutput);
			setF_OUT_readRemittanceRs(readRemittRs);
		} catch (Exception bfe) {
			LOGGER.error("ReadRemittanceInitiation : " + ExceptionUtil.getExceptionAsString(bfe));
			CommonUtil.handleParameterizedEvent(40000160, new String[] { " UBTB_REMITTANCEMESSAGE " });
		}

	}

	private UserDefFields readRemittanceMessageUDF(String remittanceIDPK) {
		UserDefFields userDefinedFields = new UserDefFields();
		IBOUDFEXTUB_SWT_RemittanceMessage remittanceTableUDF = (IBOUDFEXTUB_SWT_RemittanceMessage) factory
				.findByPrimaryKey(IBOUDFEXTUB_SWT_RemittanceMessage.BONAME, remittanceIDPK, true);
		if (null != remittanceTableUDF.getUserDefinedFields()) {
			userDefinedFields.setUserDefFields1(remittanceTableUDF.getUserDefinedFields());
		}
		return userDefinedFields;

	}

	/**
	 * Method Description:Read Document upload details
	 * 
	 * @param remittanceId
	 * @return
	 */
	private List readDocumentDetails(String remittanceId) {
		List<DocumentUploadDtls> documentSavedIdList = new ArrayList<>();
		ArrayList param = new ArrayList();
		param.add(remittanceId);
		List<IBOUB_SWT_DOCUPLOADDTLS> documentDtlList = factory.findByQuery(IBOUB_SWT_DOCUPLOADDTLS.BONAME,
				documentListQuery, param, null);
		if (null != documentDtlList && !documentDtlList.isEmpty()) {
			for (IBOUB_SWT_DOCUPLOADDTLS docDetails : documentDtlList) {
				DocumentUploadDtls vDocumentUpload = new DocumentUploadDtls();
				vDocumentUpload.setDocumentSavedId(docDetails.getF_UBDOCUMENTSAVEDID());
				vDocumentUpload.setDocumentType(StringUtils.isNotBlank(docDetails.getF_UBDOCUMENTTYPE())
						? ApiUtil.getGenericCodeDesc(RemittanceConstants.DOCTYPE_GC, docDetails.getF_UBDOCUMENTTYPE())
						: StringUtils.EMPTY);
				vDocumentUpload.setReferenceNumber(docDetails.getF_UBREFERENCENUMBER());
				vDocumentUpload.setDescription(docDetails.getF_UBDESCRIPTION());
				documentSavedIdList.add(vDocumentUpload);
			}
		}

		return documentSavedIdList;

	}

	/**
	 * @param countryCode
	 * @return
	 */
	private String findCountryByCode(String countryCode) {
		StringBuilder query = new StringBuilder();
		if (countryCode.length() == 2) {
			String ZeroConstant = "0";
			countryCode = ZeroConstant.concat(countryCode);
		}
		String countryCode3Char = StringUtils.EMPTY;
		String countryName = StringUtils.EMPTY;
		final String[] input = new String[] { DBUtils.WHERE, IBOCountry.ISOCOUNTRYCODE, DBUtils.QUERY_PARAM };
		query = Joiner.on(DBUtils.SPACE).appendTo(query, input);
		ArrayList queryParams = new ArrayList();
		queryParams.add(countryCode);
		IBOCountry result = (IBOCountry) BankFusionThreadLocal.getPersistanceFactory()
				.findFirstByQuery(IBOCountry.BONAME, query.toString(), queryParams, true);
		if (result != null) {
			countryCode3Char = result.getF_SHORTCOUNTRY3CHR();
			countryName = result.getF_COUNTRYNAME();
		}
		return countryName;
	}

}
