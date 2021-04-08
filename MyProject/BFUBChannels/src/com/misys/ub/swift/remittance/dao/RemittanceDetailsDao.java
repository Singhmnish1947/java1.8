package com.misys.ub.swift.remittance.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.posting.SWTPostingUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.bo.refimpl.IBOUDFEXTUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.bankfusion.attributes.UserDefinedFields;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.CreditPostingDtls;
import bf.com.misys.cbs.types.swift.DebitPostingDtls;
import bf.com.misys.cbs.types.swift.OrderingCustomerAndInstitution;

/**
 * UBTB_SWTREMITTANCEDETAILS
 *
 */
public class RemittanceDetailsDao {
    private transient final static Log logger = LogFactory.getLog(RemittanceDetailsDao.class.getName());
    private String chargeFundingAccountId = StringUtils.EMPTY;

    /**
     * @param outwardRq
     * @param messageId
     * @param remittanceIDPK
     * @param isNonSTP
     */
    @SuppressWarnings("unused")
    public SwiftRemittanceRs insertRemittanceDetails(SwiftRemittanceRq swtRemitanceReq, SwiftRemittanceRs swtRemitterResp, String messageId,
            String remittanceIDPK, boolean isSTP, String uetr, RemittanceProcessDto remittanceDto) {
        logger.info("START IBOUB_SWT_RemittanceTable");
        RsHeader rsHeader = new RsHeader();
        MessageStatus txnStatus = new MessageStatus();
        txnStatus.setOverallStatus(PaymentSwiftConstants.SUCCESS);
        rsHeader.setStatus(txnStatus);
        IPersistenceObjectsFactory factory = remittanceDto.getEnv().getFactory();

        try {
            IBOUB_SWT_RemittanceTable remittanceDetails = (IBOUB_SWT_RemittanceTable) factory
                    .getStatelessNewInstance(IBOUB_SWT_RemittanceTable.BONAME);
            remittanceDetails.setBoID(remittanceIDPK);
            remittanceDetails.setF_UBMESSAGEREFID(messageId);
            if(remittanceDto.isStp()) {
                // HOSTREFEREMCE
                remittanceDetails.setF_UBTRANSACTIONID(checkNullValue(swtRemitterResp.getInitiateSwiftMessageRsDtls().getHostTxnId()));
            }else {
                // set blocking reference
                remittanceDetails = setBlockingReference(remittanceDetails,checkNullValue(swtRemitterResp.getInitiateSwiftMessageRsDtls().getHostTxnId()));
            }
            remittanceDetails.setF_UBCUSTOMERID(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID()));
            // UBTRANSACTIONREFERENCE is paymentReference
            String ubTransactionReference = StringUtils.EMPTY;
            if (StringUtils.isBlank(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference())) {
                ubTransactionReference = checkNullValue(
                        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference());
            }
            else {
                ubTransactionReference = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                        .getSenderReference();
            }
            remittanceDetails.setF_UBTRANSACTIONREFERENCE(ubTransactionReference);
            // messageNumber is
            // outwardRq.getIntlPmtInputRq().getTxnInputData().getTellerTxnReference()
            remittanceDetails.setF_UBMESSAGENUMBER(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference()));
            remittanceDetails.setF_UBMESSAGETYPE("MT"+swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType());
            // customerName
            remittanceDetails.setF_UBCUSTOMERNAME(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerName()));
            // credit account is nostro account
            remittanceDetails.setF_UBCREDITACCOUNT(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getCreditPostingDtls().getCreditAccountId()));
            // prepareAmount
            remittanceDetails = prepareAmounts(swtRemitanceReq, remittanceDetails);
            // exchangeRate
            remittanceDetails.setF_UBEXCHANGERATECR(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditExchangeRate());
            // exchangeRatetype
            remittanceDetails.setF_UBEXCHANGERATETYPECR(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditExchangeRateType());
            // credit currency code
            remittanceDetails.setF_UBCRACCCURRENCY(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode()));
            // nostro/remittance currency
            remittanceDetails.setF_UBCURRENCY(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCurrencyCode()));
            // debit Account is customer account
            remittanceDetails.setF_UBDEBITACCOUNT(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getDebitPostingDtls().getDebitAccountId()));
            // exchangeRate
            remittanceDetails.setF_UBEXCHANGERATEDR(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitExchangeRate());
            // exchangeRatetype
            remittanceDetails.setF_UBEXCHANGERATETYPEDR(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitExchangeRateType());
            // debit currency code
            remittanceDetails.setF_UBDRACCCURRENCY(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode()));
            remittanceDetails.setF_UBTRANSACTIONTYPE(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType()));
            remittanceDetails.setF_UBTXNTYPECODETAG26(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getTxnTypeCode_tag26()));
            remittanceDetails
            .setF_UBREMITTANCEDESCRIPTION(get100CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getDescription()));
            remittanceDetails.setF_UBDIRECTION(PaymentSwiftConstants.OUTWARD);
            // channel id
            remittanceDetails = setChannelId(swtRemitanceReq.getRqHeader().getOrig().getChannelId(), remittanceDetails);
            remittanceDetails.setF_UBPAYRECEIVEFLAG("N");
            remittanceDetails.setF_UBBANKOPERATIONCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankOperationCode()));
            remittanceDetails.setF_UBBANKINSTRUCTIONCODE1(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankAddlInstrCode()));
            remittanceDetails.setF_UBBANKINSTRUCTIONCODE2(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankInstructionCode()));
            // beneficiary customer details
            remittanceDetails.setF_UBBENCUSTIDENTIFIERCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustIdentifierCode()));
            remittanceDetails.setF_UBBENCUSTPARTYIDENTIFIER(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifier()));
            remittanceDetails.setF_UBBENCUSTPARTYIDENCLCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifierCode()));
            //
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO1(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine1()));
            //
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO2(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine2()));
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO3(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine3()));
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO4(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine4()));
            // beneficiary institution details
            remittanceDetails.setF_UBBENINSTIDENTIFIERCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode()));
            remittanceDetails.setF_UBBENINSTPARTYIDENTIFIER(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstPartyIdentifier()));
            remittanceDetails.setF_UBBENINSTPARTYIDENCLCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstPartyClearingCode()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO1(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                            .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine1()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO2(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                            .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine2()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO3(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                            .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine3()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO4(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                            .getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine4()));
            // ordering customer details
            remittanceDetails = getOrderingCustomerDetails(swtRemitanceReq, remittanceDetails);
            remittanceDetails.setF_UBORDCUSTOMERINFO1(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine1()));
            remittanceDetails.setF_UBORDCUSTOMERINFO2(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine2()));
            remittanceDetails.setF_UBORDCUSTOMERINFO3(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine3()));
            remittanceDetails.setF_UBORDCUSTOMERINFO4(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine4()));
            // ordering institue identifier
            remittanceDetails.setF_UBORDINSTIDENTIFIER(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingInstitution().getOrderingInstIdentifierCode()));
            remittanceDetails.setF_UBORDINSTPARTYIDENTIFIER(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingInstitution().getOrderingInstPartyIdentifierCode()));
            // outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstituteIdentifierType()
            remittanceDetails.setF_UBORDINSTPARTYIDENTCLCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingInstitution().getOrderingInstPartyClearingCode()));
            remittanceDetails.setF_UBORDINSTITUTEINFO1(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                            .getOrderingInstitution().getOrderingInstitutionDtl().getOrderingInstitutionDtl1()));
            remittanceDetails.setF_UBORDINSTITUTEINFO2(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                            .getOrderingInstitution().getOrderingInstitutionDtl().getOrderingInstitutionDtl2()));
            remittanceDetails.setF_UBORDINSTITUTEINFO3(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                            .getOrderingInstitution().getOrderingInstitutionDtl().getOrderingInstitutionDtl3()));
            remittanceDetails.setF_UBORDINSTITUTEINFO4(
                    get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                            .getOrderingInstitution().getOrderingInstitutionDtl().getOrderingInstitutionDtl4()));
            // pay to identifier
            remittanceDetails.setF_UBPAYTOIDENTIFIERCODE(checkNullValue(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToIdentifierCode()));
            remittanceDetails.setF_UBPAYTOPARTYIDENTIFIER(checkNullValue(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifier()));
            // party identifier country code
            remittanceDetails.setF_UBPAYTOPARTYIDENCLCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getPayTo().getPayToPartyIdentifierClearingCode()));
            // remittanceDetails.setF_UBPAYTOPARTYIDENTIFIER(arg0);
            remittanceDetails.setF_UBPAYTOINFO1(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls1()));
            remittanceDetails.setF_UBPAYTOINFO2(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls2()));
            remittanceDetails.setF_UBPAYTOINFO3(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls3()));
            remittanceDetails.setF_UBPAYTOINFO4(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls4()));
            // intermediate identifier codes
            remittanceDetails.setF_UBINTERMDRYIDENTCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryIdentiferCode()));
            // outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getIntInstrDtls().getIdentifierType()
            remittanceDetails.setF_UBINTERMDRYPARTYIDENTCLCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryPartyIdfrClrngCode()));
            remittanceDetails.setF_UBINTERMDRYPARTYIDENTCODE(checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryPartyIdentifier()));
            remittanceDetails.setF_UBINTERMEDIARYINFO1(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine1()));
            remittanceDetails.setF_UBINTERMEDIARYINFO2(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine2()));
            remittanceDetails.setF_UBINTERMEDIARYINFO3(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine3()));
            remittanceDetails.setF_UBINTERMEDIARYINFO4(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine4()));
            // banktobank info
            remittanceDetails.setF_UBBANKTOBANKINFO1(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankToBankInfo1()));
            remittanceDetails.setF_UBBANKTOBANKINFO2(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankToBankInfo2()));
            remittanceDetails.setF_UBBANKTOBANKINFO3(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankToBankInfo3()));
            remittanceDetails.setF_UBBANKTOBANKINFO4(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankToBankInfo4()));
            remittanceDetails.setF_UBBANKTOBANKINFO5(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankToBankInfo5()));
            remittanceDetails.setF_UBBANKTOBANKINFO6(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankToBankInfo6()));
            // sender to reciever info
            remittanceDetails.setF_UBSENDERTORECIEVERINFO1(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine1()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO2(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine2()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO3(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine3()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO4(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine4()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO5(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine5()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO6(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine6()));
            // terms and conditionInfo
            remittanceDetails.setF_UBTERMANDCONDITIONINF01(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getTermsAndConditionsInfo().getTAndCInfoLine1()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF02(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getTermsAndConditionsInfo().getTAndCInfoLine2()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF03(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getTermsAndConditionsInfo().getTAndCInfoLine3()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF04(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getTermsAndConditionsInfo().getTAndCInfoLine4()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF05(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getTermsAndConditionsInfo().getTAndCInfoLine5()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF06(get35CharacterTextLine(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getTermsAndConditionsInfo().getTAndCInfoLine6()));
            // remittance info
            remittanceDetails.setF_UBREMITTANCEINFO1(get35CharacterTextLine(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine1()));
            remittanceDetails.setF_UBREMITTANCEINFO2(get35CharacterTextLine(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine2()));
            remittanceDetails.setF_UBREMITTANCEINFO3(get35CharacterTextLine(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine3()));
            remittanceDetails.setF_UBREMITTANCEINFO4(get35CharacterTextLine(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine4()));
            remittanceDetails.setF_UBPURPOSEOFREMITTANCE(checkNullValue(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRegulatoryInformation().getPurposeOfRemittance()));
            remittanceDetails.setF_UBINSTRUCTEDAMT(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getAmount());
            remittanceDetails.setF_UBINSTRUCTEDAMTCURRENCY(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getIsoCurrencyCode());
            remittanceDetails.setF_UBSETTLEMENTINSTRUCTIONSID(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSettlementInstrId());
            remittanceDetails.setF_UBVALUEDATE(
                    new Date(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getValueDate().getTime()));
            // set remittance status
            remittanceDetails = setRemittanceStatus(isSTP, remittanceDetails);
            swtRemitterResp.getInitiateSwiftMessageRsDtls().setRemittanceStatus(remittanceDetails.getF_UBREMITTANCESTATUS());
            remittanceDetails.setF_UBNARRATION(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration()));
            remittanceDetails.setF_UBCRTXNCODE(checkNullValue(remittanceDto.getCreditTransactionCode()));
            remittanceDetails.setF_UBDRTXNCODE(
                    checkNullValue(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode()));
            remittanceDetails.setF_UBCHARGEFUNDINGACCOUNT(chargeFundingAccountId);
            remittanceDetails.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
            remittanceDetails.setF_UBSHOWASINSTRUCTEDAMT(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSuppressInstructedAmt()? "Y":"N");
            remittanceDetails.setF_UBISSSISAVED(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSaveSSI()? "Y":"N");
            remittanceDetails.setF_UBISGEN103PLUS(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().isGenerate103PlusInd() == true ? "Y" : "N");
            remittanceDetails.setF_UBEND2ENDTXNREF(uetr);
            remittanceDetails.setF_UBMESSAGEPREFERENCE(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessagePreference());
            factory.create(IBOUB_SWT_RemittanceTable.BONAME, remittanceDetails);

            // udf
            updateRemittanceUDF(remittanceIDPK);
        }
        catch (BankFusionException e) {
            logger.info("Error Message during insertion into UBTB_SWTREMITTANCEDETAILS" + e.getLocalisedMessage());
            SubCode subCode = new SubCode();
            String errorCode = Integer.toString(SwiftEventCodes.E_INVALID_DATA_MP);
            EventParameters parameter = new EventParameters();
            parameter.setEventParameterValue("UBTB_SWTREMITTANCEDETAILS");
            subCode.addParameters(parameter);
            subCode.setCode(errorCode);
            subCode.setDescription(e.getEvents().iterator().next().getMessage());
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            txnStatus.addCodes(subCode);
            txnStatus.setOverallStatus("E");
            rsHeader.setStatus(txnStatus);
        }
        logger.info("END IBOUB_SWT_RemittanceTable");
        
        swtRemitterResp.setRsHeader(rsHeader);
        return swtRemitterResp;
    }

    /**
     * @param str
     * @return
     */
    private String get35CharacterTextLine(String str) {
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str)) {
            if (str.length() <= 35) {
                output = str.substring(0, str.length());
            }
            else {
                output = str.substring(0, 35);
            }
        }
        return output;
    }

    /**
     * @param isNonSTP
     * @param remittanceDetails
     * @return
     */
    private IBOUB_SWT_RemittanceTable setRemittanceStatus(boolean isSTP, IBOUB_SWT_RemittanceTable remittanceDetails) {
        if (isSTP) {
            remittanceDetails.setF_UBREMITTANCESTATUS(PaymentSwiftConstants.PROCESSED);
        }
        else {
            remittanceDetails.setF_UBREMITTANCESTATUS(PaymentSwiftConstants.REMITTER_WAIT);
        }
        return remittanceDetails;
    }

    /**
     * @param isCashTxn
     * @param remittanceDetails
     * @param hostReference
     * @return remittanceDetails
     */
    private IBOUB_SWT_RemittanceTable setBlockingReference(IBOUB_SWT_RemittanceTable remittanceDetails, String hostReference) {
        remittanceDetails.setF_UBBLOCKINGREFERENCE(hostReference);
        remittanceDetails.setF_UBISCASH("N");

        return remittanceDetails;
    }

    /**
     * @param channelId
     * @param remittanceDetails
     * @return
     */
    private IBOUB_SWT_RemittanceTable setChannelId(String channelId, IBOUB_SWT_RemittanceTable remittanceDetails) {
        if (channelId.equals(PaymentSwiftConstants.CHANNELID_TELLER)) {
            remittanceDetails.setF_UBCHANNELID(PaymentSwiftConstants.CHANNELID_TELLER);
        }
        else {
            remittanceDetails.setF_UBCHANNELID(channelId);
        }
        return remittanceDetails;
    }

    /**
     * @param str
     * @return
     */
    private String checkNullValue(String str) {
        String output = StringUtils.EMPTY;
        if (str != null && !str.isEmpty()) {
            output = str;
        }
        return output;
    }

    /**
     * @param remittanceDetails
     * @param outwardRq
     * @return
     */
    @SuppressWarnings("unchecked")
    private Currency getCharges(IBOUB_SWT_RemittanceTable remittanceDetails, SwiftRemittanceRq swtRemitanceReq) {
        Currency charges = new Currency();
        BigDecimal chgAmountInFundingAccCurrency = BigDecimal.ZERO;
        BigDecimal taxAmountInFundingAccCurrency = BigDecimal.ZERO;
        String chargefundingAccCurrency = StringUtils.EMPTY;
        BigDecimal totalCharge = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAndCharge = BigDecimal.ZERO;
        VectorTable chargeVector = SWTPostingUtils.getChargeDetails(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getCharges());
        if (chargeVector.size() > 0) {
            for (int i = 0; i < chargeVector.size(); i++) {
                Map<String, Object> map = chargeVector.getRowTags(i);
                chgAmountInFundingAccCurrency = (BigDecimal) map
                        .get(PaymentSwiftConstants.OCV_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY) != null
                                ? (BigDecimal) map.get(PaymentSwiftConstants.OCV_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY)
                                : BigDecimal.ZERO;
                taxAmountInFundingAccCurrency = (BigDecimal) map
                        .get(PaymentSwiftConstants.OCV_TAXAMOUNT_IN_FUND_ACC_CURRENCY) != null
                                ? (BigDecimal) map.get(PaymentSwiftConstants.OCV_TAXAMOUNT_IN_FUND_ACC_CURRENCY)
                                : BigDecimal.ZERO;
                chargefundingAccCurrency = (String) map.get(PaymentSwiftConstants.OCV_FUND_ACC_CURRENCY) != null
                        ? (String) map.get(PaymentSwiftConstants.OCV_FUND_ACC_CURRENCY)
                        : StringUtils.EMPTY;
                chargeFundingAccountId = (String) map.get(PaymentSwiftConstants.OCV_FUNDINGACCOUNTID) != null
                        ? (String) map.get(PaymentSwiftConstants.OCV_FUNDINGACCOUNTID)
                        : StringUtils.EMPTY;
                totalCharge = chgAmountInFundingAccCurrency.add(totalCharge);
                totalTaxAmount = taxAmountInFundingAccCurrency.add(totalTaxAmount);
            }
            totalTaxAndCharge = totalCharge.add(totalTaxAmount);
        }
        charges.setAmount(totalTaxAndCharge);
        charges.setIsoCurrencyCode(chargefundingAccCurrency);
        return charges;
    }

 

    /**
     * @param outwardRq
     * @param remittanceDetails
     * @return
     */
    private IBOUB_SWT_RemittanceTable prepareAmounts(SwiftRemittanceRq swtRemitanceReq,
            IBOUB_SWT_RemittanceTable remittanceDetails) {
        // prepare data
        // ubCharges
        Currency ubCharges = getCharges(remittanceDetails, swtRemitanceReq);
        // charge option OUR/SHA/BEN
        remittanceDetails
                .setF_UBCHARGECODETYPE(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode());
        remittanceDetails.setF_UBCHARGES(ubCharges.getAmount());
        remittanceDetails.setF_UBCHARGECURRENCY(ubCharges.getIsoCurrencyCode());
        // debit amount
        DebitPostingDtls dbPostingDtls=swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls();
        remittanceDetails.setF_UBDEBITAMOUNT(dbPostingDtls.getDebitAmount().getAmount() != null ? dbPostingDtls.getDebitAmount().getAmount() : BigDecimal.ZERO);
        // expected debitAmount
        remittanceDetails.setF_UBEXPECTEDDEBITAMOUNT(
                dbPostingDtls.getDebitAmount().getAmount() != null ? dbPostingDtls.getDebitAmount().getAmount() : BigDecimal.ZERO);
       
        CreditPostingDtls creditPostingDtls=swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls();
        // credit amount
        remittanceDetails.setF_UBCREDITAMOUNT(
                creditPostingDtls.getCreditAmount().getAmount() != null ? creditPostingDtls.getCreditAmount().getAmount() : BigDecimal.ZERO);
        // expected credit amount
        remittanceDetails.setF_UBEXPECTEDCREDITAMOUNT(
                creditPostingDtls.getCreditAmount().getAmount() != null ? creditPostingDtls.getCreditAmount().getAmount() : BigDecimal.ZERO);
        
        // charge details
        remittanceDetails.setF_UBPAYINGBANKCHARGE(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getAmount());
        remittanceDetails.setF_UBPAYINGBANKCHARGECURRENCY(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getIsoCurrencyCode());


        return remittanceDetails;
    }

    /**
     * @param outwardRq
     * @param remittanceDetails
     * @return
     */
    private IBOUB_SWT_RemittanceTable getOrderingCustomerDetails(SwiftRemittanceRq swtRemitanceReq,
            IBOUB_SWT_RemittanceTable remittanceDetails) {
        OrderingCustomerAndInstitution orderingCust = swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                .getOrderingCustomerAndInstitution();
        // ordering customer identifier
        remittanceDetails
                .setF_UBORDCUSTINDENTIFER(checkNullValue(orderingCust.getOrderingCustomer().getOrderingCustIdentifierCode()));
        // Party Identifier (Account)
        remittanceDetails.setF_UBORDCUSTPARTYIDENTACCTYPE(
                checkNullValue(orderingCust.getOrderingCustomer().getOrderingCustPartyIdentifierAcct()));
        remittanceDetails.setF_UBORDCUSTPARTYIDENTACC(
                checkNullValue(orderingCust.getOrderingCustomer().getOrderingCustPartyIdentiferAcctValue()));
        remittanceDetails.setF_UBORDCUSTPARTYIDENTCLCODE(
                checkNullValue(orderingCust.getOrderingCustomer().getOrderingCustPartyIdentifierCode()));
        remittanceDetails.setF_UBORDCUSTPARTYIDENTIFIER(
                checkNullValue(orderingCust.getOrderingCustomer().getOrderingCustPartyIdentiferValue()));
        remittanceDetails.setF_UBORDCUSTPARTYCOUNTRY(orderingCust.getOrderingCustomer().getOrderingCustPartyIdentifierCountry());
        return remittanceDetails;
    }
    
    /**
     * @param remittanceRq
     * @param remittanceIDPK
     */
    private void updateRemittanceUDF( String remittanceIDPK) {
        IBOUDFEXTUB_SWT_RemittanceTable remittanceTableUDF = (IBOUDFEXTUB_SWT_RemittanceTable) BankFusionThreadLocal
                .getPersistanceFactory().findByPrimaryKey(IBOUDFEXTUB_SWT_RemittanceTable.BONAME, remittanceIDPK, true);
        UserDefinedFields udfs = (UserDefinedFields) BankFusionThreadLocal.getBankFusionEnvironment().getData()
                .get("UzserExtension");
        if (null == remittanceTableUDF) {
            remittanceTableUDF = (IBOUDFEXTUB_SWT_RemittanceTable) BankFusionThreadLocal.getPersistanceFactory()
                    .getStatelessNewInstance(IBOUDFEXTUB_SWT_RemittanceTable.BONAME);
            remittanceTableUDF.setBoID(remittanceIDPK);
        }
        remittanceTableUDF.setUserDefinedFields(udfs);
        BankFusionThreadLocal.getPersistanceFactory().create(IBOUDFEXTUB_SWT_RemittanceTable.BONAME, remittanceTableUDF);
    }

    /**
     * @param str
     * @return
     */
    private String get100CharacterTextLine(String str) {
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str)) {
            if (str.length() <= 100) {
                output = str.substring(0, str.length());
            }
            else {
                output = str.substring(0, 100);
            }
        }
        return output;
    }

}
