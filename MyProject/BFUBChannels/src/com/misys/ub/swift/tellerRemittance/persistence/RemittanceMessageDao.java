package com.misys.ub.swift.tellerRemittance.persistence;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.fbe.common.constant.QueryConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceHelper;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceStatusDto;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOUDFEXTUB_SWT_RemittanceMessage;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.bankfusion.attributes.UserDefinedFields;
import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.swift.BankToBankInfoDtls;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomer;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.BeneficiaryInstitution;
import bf.com.misys.cbs.types.swift.CreditPostingDtls;
import bf.com.misys.cbs.types.swift.DebitPostingDtls;
import bf.com.misys.cbs.types.swift.IntermediaryDetails;
import bf.com.misys.cbs.types.swift.IntermediaryDtls;
import bf.com.misys.cbs.types.swift.OrderingCustomer;
import bf.com.misys.cbs.types.swift.OrderingCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.OrderingInstitution;
import bf.com.misys.cbs.types.swift.PayToDtls;
import bf.com.misys.cbs.types.swift.RegulatoryInformation;
import bf.com.misys.cbs.types.swift.TermsAndConditionsInfo;
import bf.com.misys.cbs.types.swift.TextLines6;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;
import bf.com.misys.cbs.types.swift.UserDefFields;

public class RemittanceMessageDao {

	private transient final static Log LOGGER = LogFactory.getLog(RemittanceMessageDao.class.getName());

	public static final String QUERY_TO_FIND_BY_SENDER_REFERENCE = QueryConstants.WHERE
			+ IBOUB_SWT_RemittanceMessage.UBSENDERREFERENCE + QueryConstants.EQUALS_PARAM;

	/**
	 * Method Description:
	 * 
	 * @param remittanceRq
	 * @param statusDto
	 */
	public static void insertData(TellerRemittanceRq remittanceRq, RemittanceStatusDto statusDto) {

		LOGGER.info("INSERT into UBTB_REMITTANCEMESSAGE");
		try {
			IBOUB_SWT_RemittanceMessage remittanceDetails = (IBOUB_SWT_RemittanceMessage) BankFusionThreadLocal
					.getPersistanceFactory().getStatelessNewInstance(IBOUB_SWT_RemittanceMessage.BONAME);

			remittanceDetails.setBoID(remittanceRq.getTxnAdditionalDtls().getRemittanceId());
			remittanceDetails.setF_UBSENDERREFERENCE(
					remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
			remittanceDetails
					.setF_UBVALUEDATE(remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getValueDate());
			remittanceDetails.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
			remittanceDetails.setF_UBSAMEASSETTLEMENTCCY(
					remittanceRq.getTxnAdditionalDtls().getSameAsSettlementCcy() ? "Y" : "N");

			DebitPostingDtls debitPostingDtls = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getDebitPostingDtls();
			if (null != debitPostingDtls) {
				remittanceDetails.setF_UBDEBITACCOUNT(debitPostingDtls.getDebitAccountId());
				remittanceDetails.setF_UBDEBITAMOUNT(debitPostingDtls.getDebitAmount().getAmount());
				remittanceDetails.setF_UBDRACCCURRENCY(debitPostingDtls.getDebitAmount().getIsoCurrencyCode());
				remittanceDetails.setF_UBEXCHANGERATEDR(debitPostingDtls.getDebitExchangeRate());
				remittanceDetails.setF_UBEXCHANGERATETYPEDR(debitPostingDtls.getDebitExchangeRateType());
			}

			CreditPostingDtls creditPostingDtls = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getCreditPostingDtls();
			if (null != creditPostingDtls) {
				remittanceDetails.setF_UBSETTLEMENTACCOUNT(creditPostingDtls.getCreditAccountId());
				remittanceDetails.setF_UBSETTLEMENTAMOUNT(creditPostingDtls.getCreditAmount().getAmount());
				remittanceDetails
						.setF_UBSETTLEMENTAMTCURRENCY(creditPostingDtls.getCreditAmount().getIsoCurrencyCode());
				remittanceDetails.setF_UBEXCHANGERATECR(creditPostingDtls.getCreditExchangeRate());
				remittanceDetails.setF_UBEXCHANGERATETYPECR(creditPostingDtls.getCreditExchangeRateType());
			}

			remittanceDetails.setF_UBINSTRUCTEDAMT(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getInstructedAmount().getAmount());
			remittanceDetails.setF_UBINSTRUCTEDAMTCURRENCY(remittanceRq.getInitiateSwiftMessageRqDtls()
					.getTransactionDetails().getInstructedAmount().getIsoCurrencyCode());

			remittanceDetails.setF_UBCHARGES(
					null != remittanceRq.getTxnAdditionalDtls().getConsolidatedChargeAmount().getAmount()
							? remittanceRq.getTxnAdditionalDtls().getConsolidatedChargeAmount().getAmount()
							: BigDecimal.ZERO);
			remittanceDetails.setF_UBCHARGECURRENCY(RemittanceHelper.checkNullValue(
					remittanceRq.getTxnAdditionalDtls().getConsolidatedChargeAmount().getIsoCurrencyCode()));

			remittanceDetails.setF_UBCHARGECODETYPE(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode()));
			remittanceDetails.setF_UBPAYINGBANKCHARGE(null != remittanceRq.getInitiateSwiftMessageRqDtls()
					.getRemittanceDetails().getChargeDetails().getAmount()
							? remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails()
									.getAmount()
							: BigDecimal.ZERO);
			remittanceDetails.setF_UBPAYINGBANKCHARGECURRENCY(RemittanceHelper.checkNullValue(remittanceRq
					.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getIsoCurrencyCode()));

			remittanceDetails.setF_UBTXNTYPECODETAG26(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getTxnTypeCode_tag26()));
			remittanceDetails.setF_UBCHANNELID(BankFusionThreadLocal.getUserSession().getChannelID());

			// bank to bank info
			BankToBankInfoDtls bankToBankInfo = remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
					.getBankToBankInfo();
			if (null != bankToBankInfo) {
				remittanceDetails.setF_UBBANKOPERATIONCODE(
						RemittanceHelper.checkNullValue(bankToBankInfo.getBankOperationCode()));
				remittanceDetails.setF_UBBANKINSTRUCTIONCODE1(
						RemittanceHelper.checkNullValue(bankToBankInfo.getBankInstructionCode()));
				remittanceDetails.setF_UBBANKINSTRUCTIONCODE2(
						RemittanceHelper.checkNullValue(bankToBankInfo.getBankAddlInstrCode()));
				remittanceDetails
						.setF_UBBANKTOBANKINFO1(RemittanceHelper.checkNullValue(bankToBankInfo.getBankToBankInfo1()));
				remittanceDetails
						.setF_UBBANKTOBANKINFO2(RemittanceHelper.checkNullValue(bankToBankInfo.getBankToBankInfo2()));
				remittanceDetails
						.setF_UBBANKTOBANKINFO3(RemittanceHelper.checkNullValue(bankToBankInfo.getBankToBankInfo3()));
				remittanceDetails
						.setF_UBBANKTOBANKINFO4(RemittanceHelper.checkNullValue(bankToBankInfo.getBankToBankInfo4()));
				remittanceDetails
						.setF_UBBANKTOBANKINFO5(RemittanceHelper.checkNullValue(bankToBankInfo.getBankToBankInfo5()));
				remittanceDetails
						.setF_UBBANKTOBANKINFO6(RemittanceHelper.checkNullValue(bankToBankInfo.getBankToBankInfo6()));
			}

			// beneficiary customer
			BeneficiaryCustomerAndInstitution beneficiaryCustomerAndInstitution = remittanceRq
					.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution();
			BeneficiaryCustomer beneficiaryCustomer = beneficiaryCustomerAndInstitution.getBeneficiaryCustomer();
			remittanceDetails.setF_UBBENCUSTIDENTIFIERCODE(
					RemittanceHelper.checkNullValue(beneficiaryCustomer.getBeneficiaryCustIdentifierCode()));
			remittanceDetails.setF_UBBENCUSTPARTYIDENTIFIER(
					RemittanceHelper.checkNullValue(beneficiaryCustomer.getBeneficiaryCustPartyIdentifier()));
			remittanceDetails.setF_UBBENCUSTPARTYIDENCLCODE(
					RemittanceHelper.checkNullValue(beneficiaryCustomer.getBeneficiaryCustPartyIdentifierCode()));
			remittanceDetails.setF_UBBENEFICIARYCUSTINFO1(
					RemittanceHelper.checkNullValue(beneficiaryCustomer.getBeneficiaryCustDetails().getTextLine1()));
			remittanceDetails.setF_UBBENEFICIARYCUSTINFO2(
					RemittanceHelper.checkNullValue(beneficiaryCustomer.getBeneficiaryCustDetails().getTextLine2()));
			remittanceDetails.setF_UBBENEFICIARYCUSTINFO3(
					RemittanceHelper.checkNullValue(beneficiaryCustomer.getBeneficiaryCustDetails().getTextLine3()));
			remittanceDetails.setF_UBBENEFICIARYCUSTINFO4(
					RemittanceHelper.checkNullValue(beneficiaryCustomer.getBeneficiaryCustDetails().getTextLine4()));

			// beneficiary institution
			BeneficiaryInstitution beneficiaryInstitution = beneficiaryCustomerAndInstitution
					.getBeneficiaryInstitution();
			remittanceDetails.setF_UBBENINSTIDENTIFIERCODE(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstIdentifierCode()));
			remittanceDetails.setF_UBBENINSTPARTYIDENTIFIER(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstPartyIdentifier()));
			remittanceDetails.setF_UBBENINSTPARTYIDENCLCODE(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstPartyClearingCode()));
			remittanceDetails.setF_UBBENEFICIARYINSTINFO1(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstDetails().getTextLine1()));
			remittanceDetails.setF_UBBENEFICIARYINSTINFO2(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstDetails().getTextLine2()));
			remittanceDetails.setF_UBBENEFICIARYINSTINFO3(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstDetails().getTextLine3()));
			remittanceDetails.setF_UBBENEFICIARYINSTINFO4(
					RemittanceHelper.checkNullValue(beneficiaryInstitution.getBeneficiaryInstDetails().getTextLine4()));

			// ordering customer
			OrderingCustomerAndInstitution orderingCustomerAndInstitution = remittanceRq.getInitiateSwiftMessageRqDtls()
					.getOrderingCustomerAndInstitution();
			OrderingCustomer orderingCustomer = orderingCustomerAndInstitution.getOrderingCustomer();
			remittanceDetails.setF_UBORDCUSTINDENTIFER(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustIdentifierCode()));
			remittanceDetails.setF_UBORDCUSTPARTYIDENTACCTYPE(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustPartyIdentifierAcct()));
			remittanceDetails.setF_UBORDCUSTPARTYIDENTACC(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustPartyIdentiferAcctValue()));
			remittanceDetails.setF_UBORDCUSTPARTYIDENTCLCODE(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustPartyIdentifierCode()));
			remittanceDetails.setF_UBORDCUSTPARTYCOUNTRY(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustPartyIdentifierCountry()));
			remittanceDetails.setF_UBORDCUSTPARTYIDENTIFIER(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustPartyIdentifierCode()));
			remittanceDetails.setF_UBORDCUSTOMERINFO1(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustDetails().getTextLine1()));
			remittanceDetails.setF_UBORDCUSTOMERINFO2(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustDetails().getTextLine2()));
			remittanceDetails.setF_UBORDCUSTOMERINFO3(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustDetails().getTextLine3()));
			remittanceDetails.setF_UBORDCUSTOMERINFO4(
					RemittanceHelper.checkNullValue(orderingCustomer.getOrderingCustDetails().getTextLine4()));

			// ordering inistitution
			OrderingInstitution orderingInstitution = orderingCustomerAndInstitution.getOrderingInstitution();
			remittanceDetails.setF_UBORDINSTIDENTIFIER(
					RemittanceHelper.checkNullValue(orderingInstitution.getOrderingInstIdentifierCode()));
			remittanceDetails.setF_UBORDINSTPARTYIDENTCLCODE(
					RemittanceHelper.checkNullValue(orderingInstitution.getOrderingInstPartyClearingCode()));
			remittanceDetails.setF_UBORDINSTPARTYIDENTIFIER(
					RemittanceHelper.checkNullValue(orderingInstitution.getOrderingInstPartyIdentifierCode()));
			remittanceDetails.setF_UBORDINSTITUTEINFO1(RemittanceHelper
					.checkNullValue(orderingInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl1()));
			remittanceDetails.setF_UBORDINSTITUTEINFO2(RemittanceHelper
					.checkNullValue(orderingInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl2()));
			remittanceDetails.setF_UBORDINSTITUTEINFO3(RemittanceHelper
					.checkNullValue(orderingInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl3()));
			remittanceDetails.setF_UBORDINSTITUTEINFO4(RemittanceHelper
					.checkNullValue(orderingInstitution.getOrderingInstitutionDtl().getOrderingInstitutionDtl4()));

			// pay to detail
			IntermediaryDetails intermediaryDetails = remittanceRq.getInitiateSwiftMessageRqDtls()
					.getIntermediaryDetails();
			PayToDtls payTo = intermediaryDetails.getPayTo();
			remittanceDetails
					.setF_UBPAYTOIDENTIFIERCODE(RemittanceHelper.checkNullValue(payTo.getPayToIdentifierCode()));
			remittanceDetails
					.setF_UBPAYTOPARTYIDENTIFIER(RemittanceHelper.checkNullValue(payTo.getPayToPartyIdentifier()));
			remittanceDetails.setF_UBPAYTOPARTYIDENCLCODE(
					RemittanceHelper.checkNullValue(payTo.getPayToPartyIdentifierClearingCode()));
			remittanceDetails.setF_UBPAYTOINFO1(RemittanceHelper.checkNullValue(payTo.getPayToDetails().getPayDtls1()));
			remittanceDetails.setF_UBPAYTOINFO2(RemittanceHelper.checkNullValue(payTo.getPayToDetails().getPayDtls2()));
			remittanceDetails.setF_UBPAYTOINFO3(RemittanceHelper.checkNullValue(payTo.getPayToDetails().getPayDtls3()));
			remittanceDetails.setF_UBPAYTOINFO4(RemittanceHelper.checkNullValue(payTo.getPayToDetails().getPayDtls4()));

			// internediary details
			IntermediaryDtls intermediary = intermediaryDetails.getIntermediary();
			remittanceDetails.setF_UBINTERMDRYIDENTCODE(
					RemittanceHelper.checkNullValue(intermediary.getIntermediaryIdentiferCode()));
			remittanceDetails.setF_UBINTERMDRYPARTYIDENTCODE(
					RemittanceHelper.checkNullValue(intermediary.getIntermediaryIdentiferCode()));
			remittanceDetails.setF_UBINTERMDRYPARTYIDENTCLCODE(
					RemittanceHelper.checkNullValue(intermediary.getIntermediaryPartyIdfrClrngCode()));
			remittanceDetails.setF_UBINTERMEDIARYINFO1(
					RemittanceHelper.checkNullValue(intermediary.getIntermediaryDetails().getTextLine1()));
			remittanceDetails.setF_UBINTERMEDIARYINFO2(
					RemittanceHelper.checkNullValue(intermediary.getIntermediaryDetails().getTextLine2()));
			remittanceDetails.setF_UBINTERMEDIARYINFO3(
					RemittanceHelper.checkNullValue(intermediary.getIntermediaryDetails().getTextLine3()));
			remittanceDetails.setF_UBINTERMEDIARYINFO4(
					RemittanceHelper.checkNullValue(intermediary.getIntermediaryDetails().getTextLine4()));

			// sender to receiver information
			TextLines6 senderToReceiverInfo = remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
					.getSenderToReceiverInfo();
			remittanceDetails
					.setF_UBSENDERTORECIEVERINFO1(RemittanceHelper.checkNullValue(senderToReceiverInfo.getTextLine1()));
			remittanceDetails
					.setF_UBSENDERTORECIEVERINFO2(RemittanceHelper.checkNullValue(senderToReceiverInfo.getTextLine2()));
			remittanceDetails
					.setF_UBSENDERTORECIEVERINFO3(RemittanceHelper.checkNullValue(senderToReceiverInfo.getTextLine3()));
			remittanceDetails
					.setF_UBSENDERTORECIEVERINFO4(RemittanceHelper.checkNullValue(senderToReceiverInfo.getTextLine4()));
			remittanceDetails
					.setF_UBSENDERTORECIEVERINFO5(RemittanceHelper.checkNullValue(senderToReceiverInfo.getTextLine5()));
			remittanceDetails
					.setF_UBSENDERTORECIEVERINFO6(RemittanceHelper.checkNullValue(senderToReceiverInfo.getTextLine6()));

			// terms and conditions
			TermsAndConditionsInfo termsAndConditionsInfo = remittanceRq.getInitiateSwiftMessageRqDtls()
					.getRemittanceDetails().getTermsAndConditionsInfo();
			remittanceDetails.setF_UBTERMANDCONDITIONINF01(
					RemittanceHelper.checkNullValue(termsAndConditionsInfo.getTAndCInfoLine1()));
			remittanceDetails.setF_UBTERMANDCONDITIONINF02(
					RemittanceHelper.checkNullValue(termsAndConditionsInfo.getTAndCInfoLine2()));
			remittanceDetails.setF_UBTERMANDCONDITIONINF03(
					RemittanceHelper.checkNullValue(termsAndConditionsInfo.getTAndCInfoLine3()));
			remittanceDetails.setF_UBTERMANDCONDITIONINF04(
					RemittanceHelper.checkNullValue(termsAndConditionsInfo.getTAndCInfoLine4()));
			remittanceDetails.setF_UBTERMANDCONDITIONINF05(
					RemittanceHelper.checkNullValue(termsAndConditionsInfo.getTAndCInfoLine5()));
			remittanceDetails.setF_UBTERMANDCONDITIONINF06(
					RemittanceHelper.checkNullValue(termsAndConditionsInfo.getTAndCInfoLine6()));

			// remittance details
			remittanceDetails.setF_UBREMITTANCEDESCRIPTION(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getDescription()));
			remittanceDetails.setF_UBREMITTANCEINFO1(RemittanceHelper.checkNullValue(remittanceRq
					.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine1()));
			remittanceDetails.setF_UBREMITTANCEINFO2(RemittanceHelper.checkNullValue(remittanceRq
					.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine2()));
			remittanceDetails.setF_UBREMITTANCEINFO3(RemittanceHelper.checkNullValue(remittanceRq
					.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine3()));
			remittanceDetails.setF_UBREMITTANCEINFO4(RemittanceHelper.checkNullValue(remittanceRq
					.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine4()));

			RegulatoryInformation regulatoryInformation = remittanceRq.getInitiateSwiftMessageRqDtls()
					.getRegulatoryInformation();
			remittanceDetails.setF_UBKYCDETAILS(RemittanceHelper.checkNullValue(regulatoryInformation.getKycDetails()));
			remittanceDetails.setF_UBPURPOSEOFREMITTANCE(
					RemittanceHelper.checkNullValue(regulatoryInformation.getPurposeOfRemittance()));
			remittanceDetails.setF_UBREGULATORYREQDOCUMENT(
					RemittanceHelper.checkNullValue(regulatoryInformation.getRegulatoryDocuments()));
			remittanceDetails
					.setF_UBTRADEDETAILINFO(RemittanceHelper.checkNullValue(regulatoryInformation.getTradeDetails()));
			remittanceDetails.setF_UBCREDITNARRATION(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditNarration()));
			remittanceDetails.setF_UBDEBITNARRATION(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration()));
			remittanceDetails.setF_UBREMITTANCESTATUS(
					RemittanceHelper.checkNullValue(statusDto.getGppTransactionIndividualStatus()));
			// set hostTxnId in
			// remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference()
			// tag
			remittanceDetails
					.setF_UBHOSTTRANSACTIONID(RemittanceHelper.checkNullValue(statusDto.getHostTransactionId()));
			remittanceDetails
					.setF_UBPAYMENTSTATUSID(RemittanceHelper.checkNullValue(statusDto.getGppPaymentStatusId()));

			remittanceDetails.setF_UBCRTXNCODE(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditTransactionCode()));
			remittanceDetails.setF_UBDRTXNCODE(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode()));

			remittanceDetails
					.setF_UBCHEQUENUMBER(getChequeNumber(remittanceRq.getTxnAdditionalDtls().getChequeNumber()));
			remittanceDetails.setF_UBMESSAGEPREFERENCE(RemittanceHelper.checkNullValue(
					remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessagePreference()));

			remittanceDetails.setF_UBMONETARYINSTRUMENTID(
					RemittanceHelper.checkNullValue(remittanceRq.getTxnAdditionalDtls().getMonetaryInstrumentId()));
			remittanceDetails.setF_UBHOLDINGLOCATIONID(
					RemittanceHelper.checkNullValue(remittanceRq.getTxnAdditionalDtls().getHoldingLocationId()));

			remittanceDetails.setF_UBPAYMENTMETHOD(
					RemittanceHelper.checkNullValue(remittanceRq.getTxnAdditionalDtls().getPaymentMethod()));
			remittanceDetails.setF_UBFUNDINGMODE(
					RemittanceHelper.checkNullValue(remittanceRq.getTxnAdditionalDtls().getFundingMode()));

			remittanceDetails.setF_UBDEALREFERENCE(
					RemittanceHelper.checkNullValue(remittanceRq.getTxnAdditionalDtls().getDealReference()));
			remittanceDetails.setF_UBFROMCUSTOMER(RemittanceHelper
					.checkNullValue(remittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID()));
			remittanceDetails.setF_UBUETR(statusDto.getUetr());

			if (remittanceRq.getTxnfeesInformation() != null) {
				TxnfeesInformation txnfeesInformation = remittanceRq.getTxnfeesInformation();
				remittanceDetails.setF_UBISCHARGEWAIVED(txnfeesInformation.getIsWaived() ? "Y" : "N");
			}

			// create
			BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_SWT_RemittanceMessage.BONAME, remittanceDetails);

			// Insert UDF
			insertRemittanceMessageUDF(remittanceRq.getTxnAdditionalDtls().getRemittanceId(),
					remittanceRq.getUserDefinedFields());
		} catch (BankFusionException e) {
			LOGGER.error("Error Message during insertion into UBTB_REMITTANCEMESSAGE "
					+ ExceptionUtil.getExceptionAsString(e));
			CommonUtil.handleParameterizedEvent(Integer.parseInt("20600092"),
					new String[] { "UBTB_REMITTANCEMESSAGE" });
		}

		LOGGER.info("END of UBTB_REMITTANCEMESSAGE");
	}

	/**
	 * Method Description:
	 * 
	 * @param remittanceIDPK
	 */
	private static void insertRemittanceMessageUDF(String remittanceIDPK, UserDefFields userDefinedFields) {
		Object udfFieldData = userDefinedFields.getUserDefFields1() != null ? userDefinedFields.getUserDefFields1()
				: null;
		boolean isUDFEnabled = udfFieldData != null ? validateUDFExists(udfFieldData) : Boolean.FALSE;
		IBOUDFEXTUB_SWT_RemittanceMessage remittanceTableUDF = (IBOUDFEXTUB_SWT_RemittanceMessage) BankFusionThreadLocal
				.getPersistanceFactory().getStatelessNewInstance(IBOUDFEXTUB_SWT_RemittanceMessage.BONAME);
		remittanceTableUDF.setBoID(remittanceIDPK);
		if (isUDFEnabled) {
			remittanceTableUDF.setUserDefinedFields((UserDefinedFields) udfFieldData);
		}

		try {
			BankFusionThreadLocal.getPersistanceFactory().create(IBOUDFEXTUB_SWT_RemittanceMessage.BONAME,
					remittanceTableUDF);
		} catch (Exception e) {
			LOGGER.error("Error in Creating UDF:::" + ExceptionUtil.getExceptionAsString(e));
			CommonUtil.handleParameterizedEvent(Integer.parseInt("20600092"),
					new String[] { "UBTB_REMITTANCEMESSAGE UDF" });
		}

	}

	/**
	 * This method has been used for validating if UDF data exists in the input.
	 * 
	 * @param udfFieldData
	 */
	private static boolean validateUDFExists(Object udfFieldData) {
		boolean isUDFExists = Boolean.FALSE;
		Class clazz = udfFieldData.getClass();
		String name = CommonConstants.EMPTY_STRING;
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			name = fields[i].getName();
			if (name.equals("_userDefinedFieldList")) {
				List value;
				try {
					value = (List) fields[i].get(udfFieldData);
					if (value.size() != 0) {
						isUDFExists = Boolean.TRUE;
					}
				} catch (IllegalAccessException e) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
		}
		return isUDFExists;
	}

	/**
	 * Method Description:
	 * 
	 * @param status
	 * @param remittanceDtls
	 */
	public static void updateRemittanceStatus(String status, String senderReference, String uetr) {

		IBOUB_SWT_RemittanceMessage remittanceDtls = findBySenderReference(senderReference);

		if (null != remittanceDtls) {
			remittanceDtls.setF_UBREMITTANCESTATUS(status);
			if (StringUtils.isBlank(remittanceDtls.getF_UBUETR())) {
				remittanceDtls.setF_UBUETR(uetr);
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("RemittanceStatus Updated");
		}

	}

	public static IBOUB_SWT_RemittanceMessage findBySenderReference(String senderReference) {
		ArrayList params = new ArrayList();
		params.add(senderReference);
		List result = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOUB_SWT_RemittanceMessage.BONAME,
				QUERY_TO_FIND_BY_SENDER_REFERENCE, params, null, true);

		return CommonUtil.checkIfNotNullOrEmpty(result) ? (IBOUB_SWT_RemittanceMessage) result.get(0) : null;
	}

	/**
	 * Method Description:
	 * 
	 * @param remittanceIDPK
	 * @return
	 */
	public static IBOUB_SWT_RemittanceMessage findByRemittanceId(String remittanceIDPK) {
		return (IBOUB_SWT_RemittanceMessage) BankFusionThreadLocal.getPersistanceFactory()
				.findByPrimaryKey(IBOUB_SWT_RemittanceMessage.BONAME, remittanceIDPK, false);
	}

	/**
	 * Method Description:
	 * 
	 * @param alphaChqNum
	 * @return
	 */
	private static Long getChequeNumber(String alphaChqNum) {
		Long chequeNumber = 0L;
		if (StringUtils.isNotBlank(alphaChqNum)) {
			chequeNumber = Long.parseLong(alphaChqNum);
		}
		return chequeNumber;
	}

}
