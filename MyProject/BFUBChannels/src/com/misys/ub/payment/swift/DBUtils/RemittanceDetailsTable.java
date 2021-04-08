package com.misys.ub.payment.swift.DBUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.fbe.common.constant.QueryConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.payment.posting.SWTPostingUtils;
import com.misys.ub.payment.swift.utils.ChargeNonStpProcess;
import com.misys.ub.payment.swift.utils.ChargesDto;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.ExchangeRateDto;
import com.trapedza.bankfusion.bo.refimpl.IBOCountry;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.bo.refimpl.IBOUDFEXTUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.bankfusion.attributes.UserDefinedFields;
import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.TxnChqDtls;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

/**
 * UBTB_SWTREMITTANCEDETAILS
 *
 */
public class RemittanceDetailsTable {
    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    private transient final static Log logger = LogFactory.getLog(RemittanceDetailsTable.class.getName());
    private String chargeFundingAccountId = StringUtils.EMPTY;
    private static final String QUERY_TO_FIND_USING_MESSAGE_NUMBER = QueryConstants.WHERE
            + IBOUB_SWT_RemittanceTable.UBMESSAGENUMBER + QueryConstants.EQUALS_PARAM;
    /**
     * @param outwardRq
     * @param messageId
     * @param remittanceIDPK
     * @param isNonSTP
     */
    @SuppressWarnings("unused")
    public void insertRemittanceDetails(OutwardSwtRemittanceRq outwardRq, String messageId, String remittanceIDPK, boolean isNonSTP,
            String uetr) {
        PaymentSwiftUtils utils = new PaymentSwiftUtils();
        logger.info("START IBOUB_SWT_RemittanceTable");
        try {
            IBOUB_SWT_RemittanceTable remittanceDetails = (IBOUB_SWT_RemittanceTable) factory
                    .getStatelessNewInstance(IBOUB_SWT_RemittanceTable.BONAME);
            remittanceDetails.setBoID(remittanceIDPK);
            remittanceDetails.setF_UBMESSAGEREFID(messageId);
            // HOSTREFEREMCE
            remittanceDetails
                    .setF_UBTRANSACTIONID(checkNullValue(outwardRq.getIntlPmtInputRq().getTxnInputData().getOriginalHostTxnRef()));
            remittanceDetails.setF_UBCUSTOMERID(checkNullValue(outwardRq.getIntlPmtInputRq().getIntlPmtDetails().getRemitterId()));
            // UBTRANSACTIONREFERENCE is paymentReference
            remittanceDetails.setF_UBTRANSACTIONREFERENCE(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getIntlPmtDetails().getPmtReference()));
            // messageNumber is
            // outwardRq.getIntlPmtInputRq().getTxnInputData().getTellerTxnReference()
            remittanceDetails
                    .setF_UBMESSAGENUMBER(checkNullValue(outwardRq.getIntlPmtInputRq().getTxnInputData().getTellerTxnReference()));
            remittanceDetails.setF_UBMESSAGETYPE(PaymentSwiftConstants.MT_103_MESSAGE_TYPE);
            // customerName
            remittanceDetails
                    .setF_UBCUSTOMERNAME(checkNullValue(outwardRq.getIntlPmtInputRq().getOthPmtDetails().getRemitterName()));
            // credit account is nostro account
            remittanceDetails.setF_UBCREDITACCOUNT(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getPaymentPosting().getAccount().getStandardAccountId()));
            
            //exchangeRateDetails
            ExchangeRateDto exchageRateDto=getExchangeRateDetails(outwardRq);
            
            // debit exchangeRate
            remittanceDetails.setF_UBEXCHANGERATEDR(exchageRateDto.getDebitExchangeRate());
            // debit exchangeRatetype
            remittanceDetails.setF_UBEXCHANGERATETYPEDR(exchageRateDto.getDebitExchangeType());
            
            //credit exchangeRate
            remittanceDetails.setF_UBEXCHANGERATECR(exchageRateDto.getCreditExchangeRate());
            //credit exchangeRatetype
            remittanceDetails.setF_UBEXCHANGERATETYPECR(exchageRateDto.getCreditExchangeType());
            
            // prepareAmount
            remittanceDetails = prepareAmounts(outwardRq, remittanceDetails);

            // credit currency code
            remittanceDetails.setF_UBCRACCCURRENCY(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getPaymentPosting().getCurrency().getIsoCurrencyCode()));
            // nostro/remittance currency
            remittanceDetails.setF_UBCURRENCY(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getPaymentPosting().getCurrency().getIsoCurrencyCode()));
            // debit Account is customer account
            remittanceDetails.setF_UBDEBITACCOUNT(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getFundingPosting().getAccount().getStandardAccountId()));
  
            // debit currency code
            remittanceDetails.setF_UBDRACCCURRENCY(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode()));
            remittanceDetails
                    .setF_UBTRANSACTIONTYPE(checkNullValue(outwardRq.getIntlPmtInputRq().getTxnInputData().getTellerTxnType()));
            remittanceDetails
                    .setF_UBTXNTYPECODETAG26(checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getTranType()));
            remittanceDetails.setF_UBDIRECTION(PaymentSwiftConstants.OUTWARD);
            // channel id
            remittanceDetails = setChannelId(outwardRq.getRqHeader().getOrig().getChannelId(), remittanceDetails);
            // pay receive flag
            remittanceDetails.setF_UBPAYRECEIVEFLAG(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPayReceiveFlag() != null ? "Y" : "N");
            remittanceDetails.setF_UBBANKOPERATIONCODE(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankOperationCode()));
            remittanceDetails.setF_UBBANKINSTRUCTIONCODE1(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankAddlInstrCode()));
            remittanceDetails.setF_UBBANKINSTRUCTIONCODE2(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankInstructionCode()));
            // beneficiary customer details
            remittanceDetails.setF_UBBENCUSTIDENTIFIERCODE(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getForAccountDetails().getBicCode()));
            remittanceDetails.setF_UBBENCUSTPARTYIDENTIFIER(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getForAccountDetails().getPartyId()));
            remittanceDetails.setF_UBBENCUSTPARTYIDENCLCODE(checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getForAccountDetails().getIdentifierType()));
            //
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO1(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getForAccountDetails().getText().getTextLine1()));
            //
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO2(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getForAccountDetails().getText().getTextLine2()));
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO3(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getForAccountDetails().getText().getTextLine3()));
            remittanceDetails.setF_UBBENEFICIARYCUSTINFO4(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getForAccountDetails().getText().getTextLine4()));
            // beneficiary institution details
            remittanceDetails.setF_UBBENINSTIDENTIFIERCODE(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getBenInstInstrDtls().getBicCode()));
            remittanceDetails.setF_UBBENINSTPARTYIDENTIFIER(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getBenInstInstrDtls().getPartyId()));
            remittanceDetails.setF_UBBENINSTPARTYIDENCLCODE(checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getBenInstInstrDtls().getIdentifierType()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO1(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine1()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO2(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine2()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO3(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine3()));
            remittanceDetails.setF_UBBENEFICIARYINSTINFO4(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine4()));
            // ordering customer details
            remittanceDetails = getOrderingCustomerDetails(outwardRq, remittanceDetails);
            remittanceDetails.setF_UBORDCUSTOMERINFO1(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPartyAddressLine().getPartyAddressLine1()));
            remittanceDetails.setF_UBORDCUSTOMERINFO2(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPartyAddressLine().getPartyAddressLine2()));
            remittanceDetails.setF_UBORDCUSTOMERINFO3(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPartyAddressLine().getPartyAddressLine3()));
            remittanceDetails.setF_UBORDCUSTOMERINFO4(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPartyAddressLine().getPartyAddressLine4()));
            // ordering institue identifier
            remittanceDetails.setF_UBORDINSTIDENTIFIER(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstitution()));
            remittanceDetails.setF_UBORDINSTPARTYIDENTIFIER(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstituteAccountId()));
            // outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstituteIdentifierType()
            remittanceDetails.setF_UBORDINSTPARTYIDENTCLCODE(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstituteIdentifierType()));
            remittanceDetails.setF_UBORDINSTITUTEINFO1(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstitutionDtl().getOrderingInstitutionDtl1()));
            remittanceDetails.setF_UBORDINSTITUTEINFO2(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstitutionDtl().getOrderingInstitutionDtl2()));
            remittanceDetails.setF_UBORDINSTITUTEINFO3(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstitutionDtl().getOrderingInstitutionDtl3()));
            remittanceDetails.setF_UBORDINSTITUTEINFO4(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingInstitutionDtl().getOrderingInstitutionDtl4()));
            // pay to identifier
            remittanceDetails.setF_UBPAYTOIDENTIFIERCODE(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getPayToInstrDtls().getBicCode()));
            remittanceDetails.setF_UBPAYTOPARTYIDENTIFIER(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getPayToInstrDtls().getPartyId()));
            // party identifier country code
            remittanceDetails.setF_UBPAYTOPARTYIDENCLCODE(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getPayToInstrDtls().getIdentifierType()));
            // remittanceDetails.setF_UBPAYTOPARTYIDENTIFIER(arg0);
            remittanceDetails.setF_UBPAYTOINFO1(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getPayToInstrDtls().getText().getTextLine1()));
            remittanceDetails.setF_UBPAYTOINFO2(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getPayToInstrDtls().getText().getTextLine2()));
            remittanceDetails.setF_UBPAYTOINFO3(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getPayToInstrDtls().getText().getTextLine3()));
            remittanceDetails.setF_UBPAYTOINFO4(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getPayToInstrDtls().getText().getTextLine4()));
            // intermediate identifier codes
            remittanceDetails.setF_UBINTERMDRYIDENTCODE(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getIntInstrDtls().getBicCode()));
            // outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getIntInstrDtls().getIdentifierType()
            remittanceDetails.setF_UBINTERMDRYPARTYIDENTCLCODE(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getIntInstrDtls().getIdentifierType()));
            remittanceDetails.setF_UBINTERMDRYPARTYIDENTCODE(checkNullValue(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getIntInstrDtls().getPartyId()));
            remittanceDetails.setF_UBINTERMEDIARYINFO1(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getIntInstrDtls().getText().getTextLine1()));
            remittanceDetails.setF_UBINTERMEDIARYINFO2(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getIntInstrDtls().getText().getTextLine2()));
            remittanceDetails.setF_UBINTERMEDIARYINFO3(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getIntInstrDtls().getText().getTextLine3()));
            remittanceDetails.setF_UBINTERMEDIARYINFO4(get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getIntInstrDtls().getText().getTextLine4()));
            // banktobank info
            remittanceDetails.setF_UBBANKTOBANKINFO1(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankToBankInfo().getBankToBankInfo1()));
            remittanceDetails.setF_UBBANKTOBANKINFO2(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankToBankInfo().getBankToBankInfo2()));
            remittanceDetails.setF_UBBANKTOBANKINFO3(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankToBankInfo().getBankToBankInfo3()));
            remittanceDetails.setF_UBBANKTOBANKINFO4(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankToBankInfo().getBankToBankInfo4()));
            remittanceDetails.setF_UBBANKTOBANKINFO5(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankToBankInfo().getBankToBankInfo5()));
            String direct = get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getBankToBankInfo().getBankToBankInfo6());
            remittanceDetails.setF_UBBANKTOBANKINFO6(
                    ("DIRECT".equals(direct) || "CREDITACCOUNT".equals(direct)) ? StringUtils.EMPTY : direct);
            // sender to reciever info
            remittanceDetails.setF_UBSENDERTORECIEVERINFO1(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getSenderToReceiverInfo().getTextLine1()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO2(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getSenderToReceiverInfo().getTextLine2()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO3(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getSenderToReceiverInfo().getTextLine3()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO4(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getSenderToReceiverInfo().getTextLine4()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO5(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getSenderToReceiverInfo().getTextLine5()));
            remittanceDetails.setF_UBSENDERTORECIEVERINFO6(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getSenderToReceiverInfo().getTextLine6()));
            // terms and conditionInfo
            remittanceDetails.setF_UBTERMANDCONDITIONINF01(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getTermsAndConditionsInfo().getTAndCInfoLine1()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF02(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getTermsAndConditionsInfo().getTAndCInfoLine2()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF03(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getTermsAndConditionsInfo().getTAndCInfoLine3()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF04(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getTermsAndConditionsInfo().getTAndCInfoLine4()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF05(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getTermsAndConditionsInfo().getTAndCInfoLine5()));
            remittanceDetails.setF_UBTERMANDCONDITIONINF06(get35CharacterTextLine(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getTermsAndConditionsInfo().getTAndCInfoLine6()));
            // remittance info
            remittanceDetails.setF_UBREMITTANCEINFO1(
                    get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPayDetails().getPayDtls1()));
            remittanceDetails.setF_UBREMITTANCEINFO2(
                    get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPayDetails().getPayDtls2()));
            remittanceDetails.setF_UBREMITTANCEINFO3(
                    get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPayDetails().getPayDtls3()));
            remittanceDetails.setF_UBREMITTANCEINFO4(
                    get35CharacterTextLine(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPayDetails().getPayDtls4()));
            remittanceDetails.setF_UBPURPOSEOFREMITTANCE(
                    checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPymtPurposeDtls().getPurposeCode()));
            remittanceDetails.setF_UBINSTRUCTEDAMT(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount().getAmount() != null
                            ? outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount().getAmount()
                            : BigDecimal.ZERO);
            remittanceDetails.setF_UBINSTRUCTEDAMTCURRENCY(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount().getIsoCurrencyCode());
            remittanceDetails.setF_UBSETTLEMENTINSTRUCTIONSID(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getSettInstrId());
            remittanceDetails
                    .setF_UBVALUEDATE(new Date(outwardRq.getIntlPmtInputRq().getIntlPmtDetails().getPmtValueDate().getTime()));
            // set remittance status
            remittanceDetails = setRemittanceStatus(isNonSTP, remittanceDetails);
            remittanceDetails.setF_UBNARRATION(checkNullValue(outwardRq.getIntlPmtInputRq().getNarrative().getNarrativeLine1()));
            // set blocking reference
            remittanceDetails = setBlockingReference(outwardRq.getSwftAdditionalDetails().getIsCashTxn(), remittanceDetails,
                    outwardRq.getIntlPmtInputRq().getTxnInputData().getOriginalHostTxnRef());
            remittanceDetails.setF_UBCRTXNCODE(checkNullValue(outwardRq.getSwftAdditionalDetails().getCreditTxnCode()));
            remittanceDetails.setF_UBDRTXNCODE(checkNullValue(outwardRq.getSwftAdditionalDetails().getDebitTxnCode()));
            remittanceDetails.setF_UBCHARGEFUNDINGACCOUNT(chargeFundingAccountId);
            remittanceDetails.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
            remittanceDetails.setF_UBCHEQUENUMBER(getChequeNumber(outwardRq.getIntlPmtInputRq().getTxnChqDetails(),
                    outwardRq.getRqHeader().getOrig().getChannelId()));
            remittanceDetails.setF_UBSHOWASINSTRUCTEDAMT("N");
            remittanceDetails.setF_UBISSSISAVED("N");
            remittanceDetails.setF_UBISGEN103PLUS(
                    outwardRq.getIntlPmtInputRq().getSettInstrDtls().getGenerate103PlusInd() == true ? "Y" : "N");
            remittanceDetails.setF_UBEND2ENDTXNREF(uetr);
            
            // message preference
            String msgPreference = utils.getModuleConfigValue(PaymentSwiftConstants.DEFAULT_SWIFT_MSG_PREFERENCE,
                    PaymentSwiftConstants.CHANNELID_SWIFT);
            remittanceDetails.setF_UBMESSAGEPREFERENCE(msgPreference);
            
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_SWT_RemittanceTable.BONAME, remittanceDetails);
            insertDefaultRemittanceUDF(remittanceIDPK);
        }
        catch (BankFusionException e) {
            logger.info("Error Message during insertion into UBTB_SWTREMITTANCEDETAILS", e);
            PaymentSwiftUtils.handleEvent(Integer.parseInt("20600092"), new String[] {"UB_SWT_RemittanceTable"});
        }
        logger.info("END IBOUB_SWT_RemittanceTable");
    }

    /**
     * @param remittanceIDPK
     * @param status
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateRemittanceDetails(UB_SWT_RemittanceProcessRq remittanceRq, String status,
            IBOUB_SWT_RemittanceTable remittanceDtls) {
        logger.info("Start of updateRemittanceDetails");

        /********* TransactionDetails ********************/
        // remittance status
        remittanceDtls.setF_UBREMITTANCESTATUS(status);
        // last updated dateTime
        remittanceDtls.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
        remittanceDtls.setF_UBNARRATION(checkNullValue(remittanceRq.getTRANSACTIONDETAISINFO().getNARRATION()));
        // exchangeRateType outgoing debit side
        remittanceDtls.setF_UBEXCHANGERATEDR(remittanceRq.getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING() != null
                ? remittanceRq.getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING()
                : BigDecimal.ZERO);
        remittanceDtls.setF_UBEXCHANGERATETYPEDR(checkNullValue(remittanceRq.getExchangeRateTypeOUT()));
        // exchange rate incoming credit side
        remittanceDtls.setF_UBEXCHANGERATECR(remittanceRq.getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING() != null
                ? remittanceRq.getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING()
                : BigDecimal.ZERO);
        remittanceDtls.setF_UBEXCHANGERATETYPECR(checkNullValue(remittanceRq.getExchangeRateTypeIN()));
        remittanceDtls.setF_UBCRACCCURRENCY(checkNullValue(remittanceRq.getCrAccountCurrency()));
        remittanceDtls.setF_UBVALUEDATE(remittanceRq.getTRANSACTIONDETAISINFO().getDATEOFPROCESSING());
        remittanceDtls.setF_UBMESSAGETYPE(checkNullValue(remittanceRq.getMESSAGETYPE()));
        // ubCharges
        remittanceDtls.setF_UBCHARGES(remittanceRq.getTRANSACTIONDETAISINFO().getAPPLIEDCHARGES());
        remittanceDtls.setF_UBDRACCCURRENCY(checkNullValue(remittanceRq.getDrAccountCurrency()));
        remittanceDtls.setF_UBDIRECTION(checkNullValue(remittanceRq.getDIRECTION()));
        remittanceDtls.setF_UBSETTLEMENTINSTRUCTIONSID(checkNullValue(remittanceRq.getSettlementInstrId()));
        remittanceDtls.setF_UBSHOWASINSTRUCTEDAMT(remittanceRq.getTRANSACTIONDETAISINFO().getShowAsInstructed() ? "Y" : "N");
        remittanceDtls.setF_UBISSSISAVED(remittanceRq.getCreateSettlementFlag() ? "Y" : "N");
        /****** CREDITORDTL *******/
        remittanceDtls.setF_UBCREDITACCOUNT(checkNullValue(remittanceRq.getCREDITORDTL().getCREDITACCOUNTID()));
        remittanceDtls.setF_UBCREDITAMOUNT(
                remittanceRq.getCREDITORDTL().getCREDITAMOUNT() != null ? remittanceRq.getCREDITORDTL().getCREDITAMOUNT()
                        : BigDecimal.ZERO);
        remittanceDtls.setF_UBEXPECTEDCREDITAMOUNT(remittanceRq.getCREDITORDTL().getEXPECTEDCREDITAMOUNT() != null
                ? remittanceRq.getCREDITORDTL().getEXPECTEDCREDITAMOUNT()
                : BigDecimal.ZERO);
        /****** DEBITORDTL ******/
        remittanceDtls.setF_UBDEBITACCOUNT(checkNullValue(remittanceRq.getDEBITORDTL().getDEBITACCOUNTID()));
        remittanceDtls.setF_UBDEBITAMOUNT(
                remittanceRq.getDEBITORDTL().getDEBITAMOUNT() != null ? remittanceRq.getDEBITORDTL().getDEBITAMOUNT()
                        : BigDecimal.ZERO);
        remittanceDtls.setF_UBEXPECTEDDEBITAMOUNT(remittanceRq.getDEBITORDTL().getEXPECTEDDEBITAMOUNT() != null
                ? remittanceRq.getDEBITORDTL().getEXPECTEDDEBITAMOUNT()
                : BigDecimal.ZERO);
        /********* ordering customer details **************/
        // identifiercode
        remittanceDtls.setF_UBORDCUSTINDENTIFER(get20CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDCUSTIDENBIC()));
        // ordering customer party identifier account and value
        remittanceDtls.setF_UBORDCUSTPARTYIDENTACCTYPE(
                get20CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDCUSTPTYIDENACC()));
        remittanceDtls.setF_UBORDCUSTPARTYIDENTACC(
                get20CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDCUSTPTYIDENACCVALUE()));
        // ordering customer party identifier code,country,identifier
        remittanceDtls
                .setF_UBORDCUSTPARTYIDENTCLCODE(get4CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDCUSTPTYIDCODE()));
        remittanceDtls.setF_UBORDCUSTPARTYCOUNTRY(checkNullValue(remittanceRq.getORDERINGICUSTINFO().getORDCUSTPTYIDCONTRY()));
        remittanceDtls
                .setF_UBORDCUSTPARTYIDENTIFIER(get20CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDCUSTPTYIDVALUE()));
        // ordering customer name and address
        remittanceDtls.setF_UBORDCUSTOMERINFO1(get35CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO1()));
        remittanceDtls.setF_UBORDCUSTOMERINFO2(get35CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO2()));
        remittanceDtls.setF_UBORDCUSTOMERINFO3(get35CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO3()));
        remittanceDtls.setF_UBORDCUSTOMERINFO4(get35CharacterTextLine(remittanceRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO4()));
        /********* ordering customer details **************/
        /*************** ORDERINGINSTITUTIONDTL ***************************/
        remittanceDtls.setF_UBORDINSTPARTYIDENTIFIER(
                get35CharacterTextLine(remittanceRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE()));
        remittanceDtls.setF_UBORDINSTPARTYIDENTCLCODE(
                get4CharacterTextLine(remittanceRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPRTYIDNTCLRCODE()));
        // identifier code
        remittanceDtls.setF_UBORDINSTIDENTIFIER(
                get20CharacterTextLine(remittanceRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE()));
        remittanceDtls.setF_UBORDINSTITUTEINFO1(
                get35CharacterTextLine(remittanceRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL1()));
        remittanceDtls.setF_UBORDINSTITUTEINFO2(
                get35CharacterTextLine(remittanceRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL2()));
        remittanceDtls.setF_UBORDINSTITUTEINFO3(
                get35CharacterTextLine(remittanceRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL3()));
        remittanceDtls.setF_UBORDINSTITUTEINFO4(
                get35CharacterTextLine(remittanceRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL4()));
        /*************** ORDERINGINSTITUTIONDTL ***************************/
        /************************ BENEFICIARYCUSTOMERINFO ****************************/
        remittanceDtls.setF_UBBENCUSTPARTYIDENTIFIER(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTPARTYIDENTIFIER()));
        remittanceDtls.setF_UBBENCUSTPARTYIDENCLCODE(
                get4CharacterTextLine(remittanceRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTPARTYIDENCODE()));
        // identifier code
        remittanceDtls.setF_UBBENCUSTIDENTIFIERCODE(
                get20CharacterTextLine(remittanceRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTIDENTCODE()));
        remittanceDtls.setF_UBBENEFICIARYCUSTINFO1(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT1()));
        remittanceDtls.setF_UBBENEFICIARYCUSTINFO2(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT2()));
        remittanceDtls.setF_UBBENEFICIARYCUSTINFO3(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT3()));
        remittanceDtls.setF_UBBENEFICIARYCUSTINFO4(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT4()));
        /************************ BENEFICIARYCUSTOMERINFO ****************************/
        /*************** BENEFICIARYINSTDETIALS ******************/
        remittanceDtls.setF_UBBENINSTPARTYIDENTIFIER(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER()));
        remittanceDtls.setF_UBBENINSTPARTYIDENCLCODE(
                get4CharacterTextLine(remittanceRq.getBENEFICIARYINSTDETIALS().getBENPSRTYIDENTCLRCODE()));
        // identifier code
        remittanceDtls.setF_UBBENINSTIDENTIFIERCODE(
                get20CharacterTextLine(remittanceRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE()));
        remittanceDtls.setF_UBBENEFICIARYINSTINFO1(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT1()));
        remittanceDtls.setF_UBBENEFICIARYINSTINFO2(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT2()));
        remittanceDtls.setF_UBBENEFICIARYINSTINFO3(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT3()));
        remittanceDtls.setF_UBBENEFICIARYINSTINFO4(
                get35CharacterTextLine(remittanceRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT4()));
        /*************** BENEFICIARYINSTDETIALS ******************/
        /*************** PAYTOPARTYDETAILS ************************/
        remittanceDtls
                .setF_UBPAYTOPARTYIDENTIFIER(get35CharacterTextLine(remittanceRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER()));
        remittanceDtls
                .setF_UBPAYTOPARTYIDENCLCODE(get4CharacterTextLine(remittanceRq.getPAYTOPARTYDETAILS().getPAYTOPRTYIDNTCLRCODE()));
        // identifier code
        remittanceDtls.setF_UBPAYTOIDENTIFIERCODE(get20CharacterTextLine(remittanceRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE()));
        remittanceDtls.setF_UBPAYTOINFO1(get35CharacterTextLine(remittanceRq.getPAYTOPARTYDETAILS().getPAYTOTEXT1()));
        remittanceDtls.setF_UBPAYTOINFO2(get35CharacterTextLine(remittanceRq.getPAYTOPARTYDETAILS().getPAYTOTEXT2()));
        remittanceDtls.setF_UBPAYTOINFO3(get35CharacterTextLine(remittanceRq.getPAYTOPARTYDETAILS().getPAYTOTEXT3()));
        remittanceDtls.setF_UBPAYTOINFO4(get35CharacterTextLine(remittanceRq.getPAYTOPARTYDETAILS().getPAYTOTEXT4()));
        /*************** PAYTOPARTYDETAILS ***********************/
        /**************** INTERMEDIARYDETAILS ***************************************/
        remittanceDtls.setF_UBINTERMDRYPARTYIDENTCODE(
                get35CharacterTextLine(remittanceRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR()));
        remittanceDtls.setF_UBINTERMDRYPARTYIDENTCLCODE(
                get4CharacterTextLine(remittanceRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTCLRCODE()));
        // identifier code
        remittanceDtls.setF_UBINTERMDRYIDENTCODE(
                get20CharacterTextLine(remittanceRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()));
        remittanceDtls
                .setF_UBINTERMEDIARYINFO1(get35CharacterTextLine(remittanceRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1()));
        remittanceDtls
                .setF_UBINTERMEDIARYINFO2(get35CharacterTextLine(remittanceRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2()));
        remittanceDtls
                .setF_UBINTERMEDIARYINFO3(get35CharacterTextLine(remittanceRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3()));
        remittanceDtls
                .setF_UBINTERMEDIARYINFO4(get35CharacterTextLine(remittanceRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4()));
        /***************** INTERMEDIARYDETAILS **************************************/
        /************ RemittanceINFO **************/
        remittanceDtls
                .setF_UBREMITTANCEDESCRIPTION(get100CharacterTextLine(remittanceRq.getRemittanceINFO().getREMITTANCEDESCRIPTION()));
        // remittanceDtls.setF_UBTRANSACTIONTYPE(get10CharacterTextLine(remittanceRq.getRemittanceINFO().getTRANSACTIONTYPECODE()));
        remittanceDtls.setF_UBTXNTYPECODETAG26(get10CharacterTextLine(remittanceRq.getRemittanceINFO().getTRANSACTIONTYPECODE()));
        remittanceDtls.setF_UBREMITTANCEINFO1(get35CharacterTextLine(remittanceRq.getRemittanceINFO().getREMITTANCEINFO1()));
        remittanceDtls.setF_UBREMITTANCEINFO2(get35CharacterTextLine(remittanceRq.getRemittanceINFO().getREMITTANCEINFO2()));
        remittanceDtls.setF_UBREMITTANCEINFO3(get35CharacterTextLine(remittanceRq.getRemittanceINFO().getREMITTANCEINFO3()));
        remittanceDtls.setF_UBREMITTANCEINFO4(get35CharacterTextLine(remittanceRq.getRemittanceINFO().getREMITTANCEINFO4()));
        remittanceDtls.setF_UBCHARGECODETYPE(checkNullValue(remittanceRq.getRemittanceINFO().getCHARGECODE()));
        remittanceDtls.setF_UBPAYINGBANKCHARGE(remittanceRq.getRemittanceINFO().getChargeDetailAmount());
        /************ RemittanceINFO **************/
        /********** BANKTOBANKINFO *************************/
        remittanceDtls.setF_UBBANKTOBANKINFO1(get35CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKTOBANKINFO1()));
        remittanceDtls.setF_UBBANKTOBANKINFO2(get35CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKTOBANKINFO2()));
        remittanceDtls.setF_UBBANKTOBANKINFO3(get35CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKTOBANKINFO3()));
        remittanceDtls.setF_UBBANKTOBANKINFO4(get35CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKTOBANKINFO4()));
        remittanceDtls.setF_UBBANKTOBANKINFO5(get35CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKTOBANKINFO5()));
        remittanceDtls.setF_UBBANKTOBANKINFO6(get35CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKTOBANKINFO6()));
        remittanceDtls.setF_UBBANKOPERATIONCODE(get4CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKOPERATIONCODE()));
        remittanceDtls.setF_UBBANKINSTRUCTIONCODE1(remittanceRq.getBANKTOBANKINFO().getBANKINSTRUCTIONCODE2());
        remittanceDtls
                .setF_UBBANKINSTRUCTIONCODE2(get4CharacterTextLine(remittanceRq.getBANKTOBANKINFO().getBANKINSTRUCTIONCODE()));
        /*********** BANKTOBANKINFO ************************/
        /******** SENDERTORECEIVERINFO ************/
        remittanceDtls.setF_UBSENDERTORECIEVERINFO1(
                get35CharacterTextLine(remittanceRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO1()));
        remittanceDtls.setF_UBSENDERTORECIEVERINFO2(
                get35CharacterTextLine(remittanceRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO2()));
        remittanceDtls.setF_UBSENDERTORECIEVERINFO3(
                get35CharacterTextLine(remittanceRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO3()));
        remittanceDtls.setF_UBSENDERTORECIEVERINFO4(
                get35CharacterTextLine(remittanceRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO4()));
        remittanceDtls.setF_UBSENDERTORECIEVERINFO5(
                get35CharacterTextLine(remittanceRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO5()));
        remittanceDtls.setF_UBSENDERTORECIEVERINFO6(
                get35CharacterTextLine(remittanceRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO6()));
        /********* SENDERTORECEIVERINFO ***********/
        /******************* TERMSCONDITIONSINFO *********************/
        remittanceDtls.setF_UBTERMANDCONDITIONINF01(
                get35CharacterTextLine(remittanceRq.getTERMSCONDITIONSINFO().getTERMSCONDITIONSINFO1()));
        remittanceDtls.setF_UBTERMANDCONDITIONINF02(
                get35CharacterTextLine(remittanceRq.getTERMSCONDITIONSINFO().getTERMSCONDITIONSINFO2()));
        remittanceDtls.setF_UBTERMANDCONDITIONINF03(
                get35CharacterTextLine(remittanceRq.getTERMSCONDITIONSINFO().getTERMSCONDITIONSINFO3()));
        remittanceDtls.setF_UBTERMANDCONDITIONINF04(
                get35CharacterTextLine(remittanceRq.getTERMSCONDITIONSINFO().getTERMSCONDITIONSINFO4()));
        remittanceDtls.setF_UBTERMANDCONDITIONINF05(
                get35CharacterTextLine(remittanceRq.getTERMSCONDITIONSINFO().getTERMSCONDITIONSINFO5()));
        remittanceDtls.setF_UBTERMANDCONDITIONINF06(
                get35CharacterTextLine(remittanceRq.getTERMSCONDITIONSINFO().getTERMSCONDITIONSINFO6()));
        /******************* TERMSCONDITIONSINFO *********************/
        /***** KYCDETAILS *********/
        // kyc details
        remittanceDtls
                .setF_UBREGULATORYREQDOCUMENT(get100CharacterTextLine(remittanceRq.getKYCDETAILS().getREGULATORYREQDOCUMENT()));
        remittanceDtls.setF_UBTRADEDETAILINFO(get100CharacterTextLine(remittanceRq.getKYCDETAILS().getTRADEDETAILS()));
        remittanceDtls.setF_UBPURPOSEOFREMITTANCE(get100CharacterTextLine(remittanceRq.getKYCDETAILS().getPURPOSEOFREMITTANCE()));
        remittanceDtls.setF_UBKYCDETAILS(get100CharacterTextLine(remittanceRq.getKYCDETAILS().getKYCDETAILS()));
        /***** KYCDETAILS *********/

        remittanceDtls.setF_UBISGEN103PLUS(remittanceRq.getGENERATE103PLUSIND() == true ? "Y" : "N");
        remittanceDtls.setF_UBEND2ENDTXNREF(
                !StringUtils.isBlank(remittanceRq.getEnd2EndTxnRef()) ? remittanceRq.getEnd2EndTxnRef() : StringUtils.EMPTY);
        remittanceDtls.setF_UBMESSAGEPREFERENCE(remittanceRq.getTRANSACTIONDETAISINFO().getMessagePreference());
        /***** User Defined Fields *********/
        updateRemittanceUDF(remittanceRq, remittanceDtls.getBoID());
        logger.info("End of updateRemittanceDetails");
    }

    /**
     * @param remittanceRq
     * @param remittanceIDPK
     */
    private void updateRemittanceUDF(UB_SWT_RemittanceProcessRq remittanceRq, String remittanceIDPK) {
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
    private String get35CharacterTextLine(String str) {
        String spaceConstant = " ";
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str) && !str.equals(spaceConstant)) {
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

    /**
     * @param str
     * @return
     */
    private String get10CharacterTextLine(String str) {
        String spaceConstant = " ";
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str) && !str.equals(spaceConstant) && str.length() <= 10) {
            output = str.substring(0, str.length());
        }
        return output;
    }

    /**
     * @param str
     * @return
     */
    private String get20CharacterTextLine(String str) {
        String spaceConstant = " ";
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str) && !str.equals(spaceConstant) && str.length() <= 20) {
            output = str.substring(0, str.length());
        }
        return output;
    }

    /**
     * @param str
     * @return
     */
    private String get4CharacterTextLine(String str) {
        String spaceConstant = " ";
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str) && !str.equals(spaceConstant) && str.length() <= 4) {
            output = str.substring(0, str.length());
        }
        return output;
    }

    /**
     * @param isNonSTP
     * @param remittanceDetails
     * @return
     */
    private IBOUB_SWT_RemittanceTable setRemittanceStatus(boolean isNonSTP, IBOUB_SWT_RemittanceTable remittanceDetails) {
        if (isNonSTP) {
            remittanceDetails.setF_UBREMITTANCESTATUS(PaymentSwiftConstants.REMITTER_WAIT);
        }
        else {
            remittanceDetails.setF_UBREMITTANCESTATUS(PaymentSwiftConstants.PROCESSED);
        }
        return remittanceDetails;
    }

    /**
     * @param isCashTxn
     * @param remittanceDetails
     * @param hostReference
     * @return remittanceDetails
     */
    private IBOUB_SWT_RemittanceTable setBlockingReference(boolean isCashTxn, IBOUB_SWT_RemittanceTable remittanceDetails,
            String hostReference) {
        if (isCashTxn) {
            remittanceDetails.setF_UBBLOCKINGREFERENCE("");
            remittanceDetails.setF_UBISCASH("Y");
        }
        else {
            remittanceDetails.setF_UBBLOCKINGREFERENCE(hostReference);
            remittanceDetails.setF_UBISCASH("N");
        }
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
     * @param hostReference
     * @return IBOUB_SWT_RemittanceTable
     */
    public IBOUB_SWT_RemittanceTable findByMessageId(String remittanceId) {
        StringBuilder query = new StringBuilder();
        final String[] input = new String[] { DBUtils.WHERE, IBOUB_SWT_RemittanceTable.UBMESSAGEREFID, DBUtils.QUERY_PARAM };
        query = Joiner.on(DBUtils.SPACE).appendTo(query, input);
        ArrayList queryParams = new ArrayList();
        queryParams.add(remittanceId);
        return (IBOUB_SWT_RemittanceTable) factory.findFirstByQuery(IBOUB_SWT_RemittanceTable.BONAME, query.toString(), queryParams,
                true);
    }

    /**
     * @param outwardRq
     * @return
     */
    private Long getChequeNumber(TxnChqDtls txnChqDetails, String channelId) {
        Long chequeNumber = 0L;
        if (channelId.equals(PaymentSwiftConstants.CHANNELID_TELLER) && txnChqDetails != null
                && !StringUtils.isEmpty(txnChqDetails.getChqNum())) {
            chequeNumber = Long.parseLong(txnChqDetails.getChqNum());
        }
        return chequeNumber;
    }

    /**
     * @param remittanceDetails
     * @param outwardRq
     * @return
     */
    @SuppressWarnings("unchecked")
    private Currency getCharges(IBOUB_SWT_RemittanceTable remittanceDetails, OutwardSwtRemittanceRq outwardRq) {
        Currency charges = new Currency();
        BigDecimal chgAmountInFundingAccCurrency = BigDecimal.ZERO;
        BigDecimal taxAmountInFundingAccCurrency = BigDecimal.ZERO;
        String chargefundingAccCurrency = StringUtils.EMPTY;
        BigDecimal totalCharge = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAndCharge = BigDecimal.ZERO;
        VectorTable chargeVector = SWTPostingUtils.getChargeDetails(outwardRq.getIntlPmtInputRq().getCharges());
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
    private ChargesDto prepareChargeData(OutwardSwtRemittanceRq outwardRq, IBOUB_SWT_RemittanceTable remittanceDetails) {
        ChargeNonStpProcess nonStpCharge = new ChargeNonStpProcess();
        ChargesDto chargesDto = new ChargesDto();
        Currency instructedAmt = new Currency();
        // initailRemittanceAmount from teller or DC
        Currency debitAmount = new Currency();
        BigDecimal instructedAmount = outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount()
                .getAmount() != null
                        ? outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount().getAmount()
                        : outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getAmount();
        String instrucetAmtccy = outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount()
                .getIsoCurrencyCode() != null
                        ? outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount().getIsoCurrencyCode()
                        : outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode();
        debitAmount.setAmount(instructedAmount);
        debitAmount.setIsoCurrencyCode(instrucetAmtccy);
        instructedAmt.setAmount(instructedAmount);
        instructedAmt.setIsoCurrencyCode(instrucetAmtccy);
        chargesDto.setInstructedAmount(instructedAmt);
        chargesDto.setDebitAmount(debitAmount);
        chargesDto.setCreditAmount(outwardRq.getIntlPmtInputRq().getPaymentPosting().getCurrency());
        chargesDto.setExchangeRate(outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRate() != null
                ? outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRate()
                : BigDecimal.ONE);
        chargesDto.setExchangeRateType(
                outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType() != null
                        ? outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType()
                        : "SPOT");
        chargesDto.setDebitExchangeRateType(
                outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType() != null
                        ? outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType()
                        : "SPOT");
        // channelId
        chargesDto.setChannelId(outwardRq.getRqHeader().getOrig().getChannelId());
        if (outwardRq.getIntlPmtInputRq().getIntlPmtDetails().getPayingBankCharge() != null) {
            chargesDto.setPayingBankChg(outwardRq.getIntlPmtInputRq().getIntlPmtDetails().getPayingBankCharge());
        }
        chargesDto.setChargeType(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getChargeCode() != null
                ? outwardRq.getIntlPmtInputRq().getSettInstrDtls().getChargeCode()
                : StringUtils.EMPTY);
        chargesDto.setChargeFundingAccountId(chargeFundingAccountId);
        chargesDto.setUbCharges(getCharges(remittanceDetails, outwardRq));
        return nonStpCharge.getAmountBasedOnChargeOption(chargesDto);
    }

    /**
     * @param outwardRq
     * @param remittanceDetails
     * @return
     */
    private IBOUB_SWT_RemittanceTable prepareAmounts(OutwardSwtRemittanceRq outwardRq,
            IBOUB_SWT_RemittanceTable remittanceDetails) {
        // prepare data
        ChargesDto chargeDto = prepareChargeData(outwardRq, remittanceDetails);
        // ubCharges
        Currency ubCharges = getCharges(remittanceDetails, outwardRq);
        // charge option OUR/SHA/BEN
        remittanceDetails.setF_UBCHARGECODETYPE(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getChargeCode());
        remittanceDetails.setF_UBCHARGES(ubCharges.getAmount());
        remittanceDetails.setF_UBCHARGECURRENCY(ubCharges.getIsoCurrencyCode());
        // debit amount
        remittanceDetails.setF_UBDEBITAMOUNT(
                chargeDto.getDebitAmount().getAmount() != null ? chargeDto.getDebitAmount().getAmount() : BigDecimal.ZERO);
        // credit amount
        remittanceDetails.setF_UBCREDITAMOUNT(
                chargeDto.getCreditAmount().getAmount() != null ? chargeDto.getCreditAmount().getAmount() : BigDecimal.ZERO);
        // charge details
        remittanceDetails.setF_UBPAYINGBANKCHARGE(chargeDto.getPayingBankChg().getAmount());
        remittanceDetails.setF_UBPAYINGBANKCHARGECURRENCY(chargeDto.getPayingBankChg().getIsoCurrencyCode());
        // expected creditAmount
        remittanceDetails.setF_UBEXPECTEDDEBITAMOUNT(
                chargeDto.getDebitAmount().getAmount() != null ? chargeDto.getDebitAmount().getAmount() : BigDecimal.ZERO);
        // expected debitAmount
        remittanceDetails.setF_UBEXPECTEDCREDITAMOUNT(
                chargeDto.getCreditAmount().getAmount() != null ? chargeDto.getCreditAmount().getAmount() : BigDecimal.ZERO);
        return remittanceDetails;
    }

    /**
     * @param outwardRq
     * @param remittanceDetails
     * @return
     */
    private IBOUB_SWT_RemittanceTable getOrderingCustomerDetails(OutwardSwtRemittanceRq outwardRq,
            IBOUB_SWT_RemittanceTable remittanceDetails) {
        String orderingCustPartyCountryCode = StringUtils.EMPTY;
        String orderingCustCLCode = StringUtils.EMPTY;
        Object userExtn = outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingCustomerAccountId().getUserExtension();
        if (userExtn != null && !StringUtils.isBlank(userExtn.toString())) {
            String[] tokens = userExtn.toString().split("/");
            for (int i = 0; i <= tokens.length; i++) {
                if (i == 0) {
                    orderingCustCLCode = tokens[0] != null ? tokens[0] : StringUtils.EMPTY;
                }
                if (i == 1) {
                    orderingCustPartyCountryCode = tokens[1] != null ? tokens[1] : StringUtils.EMPTY;
                    orderingCustPartyCountryCode = findCountryByCode(orderingCustPartyCountryCode);
                }
            }
        }
        remittanceDetails.setF_UBORDCUSTPARTYCOUNTRY(orderingCustPartyCountryCode);
        remittanceDetails.setF_UBORDCUSTPARTYIDENTCLCODE(orderingCustCLCode);
        remittanceDetails.setF_UBORDCUSTPARTYIDENTIFIER(
                checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getPartyIdentifier()));
        // ordering customer identifier
        remittanceDetails.setF_UBORDCUSTINDENTIFER(
                checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingCustomerIdentifierCode()));
        remittanceDetails.setF_UBORDCUSTPARTYIDENTACC(checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls()
                .getOrderingCustomerAccountId().getInputAccount().getInputAccountId()));
        remittanceDetails.setF_UBORDCUSTPARTYIDENTACCTYPE(
                checkNullValue(outwardRq.getIntlPmtInputRq().getSettInstrDtls().getOrderingCustomerIdentifierType()));
        return remittanceDetails;
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
        final String[] input = new String[] { DBUtils.WHERE, IBOCountry.ISOCOUNTRYCODE, DBUtils.QUERY_PARAM };
        query = Joiner.on(DBUtils.SPACE).appendTo(query, input);
        ArrayList queryParams = new ArrayList();
        queryParams.add(countryCode);
        IBOCountry result = (IBOCountry) BankFusionThreadLocal.getPersistanceFactory().findFirstByQuery(IBOCountry.BONAME,
                query.toString(), queryParams, true);
        if (result != null) {
            countryCode3Char = result.getF_SHORTCOUNTRY3CHR();
        }
        return countryCode3Char;
    }

    /**
     * 
     * @param remittanceIdPK
     * @param status
     */
    public void updateRemittanceStatus(IBOUB_SWT_RemittanceTable remittanceBO, String status) {
        logger.info("Start of updateRemittanceStatus");
        remittanceBO.setF_UBREMITTANCESTATUS(status);
        remittanceBO.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
        logger.info("End of updateRemittanceStatus");
    }

    private void insertDefaultRemittanceUDF(String remittanceIDPK) {

        IBOUDFEXTUB_SWT_RemittanceTable remittanceTableUDF = (IBOUDFEXTUB_SWT_RemittanceTable) BankFusionThreadLocal
                .getPersistanceFactory().getStatelessNewInstance(IBOUDFEXTUB_SWT_RemittanceTable.BONAME);
        remittanceTableUDF.setBoID(remittanceIDPK);
        BankFusionThreadLocal.getPersistanceFactory().create(IBOUDFEXTUB_SWT_RemittanceTable.BONAME, remittanceTableUDF);
    }
    
    
    /**
     * @param outwardRq
     * @return
     */
    private ExchangeRateDto getExchangeRateDetails(OutwardSwtRemittanceRq outwardRq) {
        ExchangeRateDto exchageRateDto=new ExchangeRateDto();
        String instructedCcy=outwardRq.getIntlPmtInputRq().getSettInstrDtls().getInstructedCcyDtls().getAmount().getIsoCurrencyCode();
        String debitAcctccy=outwardRq.getIntlPmtInputRq().getFundingPosting().getCurrency().getIsoCurrencyCode();
        String creditAcctccy=outwardRq.getIntlPmtInputRq().getPaymentPosting().getCurrency().getIsoCurrencyCode();
        
        //if instructedccy same as debit ccy then set the debit exchange rate
        if(instructedCcy.equals(debitAcctccy)) {
            exchageRateDto.setDebitExchangeRate(BigDecimal.ONE);
        }else {
            exchageRateDto.setDebitExchangeRate(outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRate() != null
                    ? outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRate()
                    : BigDecimal.ZERO);
        }
        exchageRateDto.setDebitCcy(debitAcctccy);
        exchageRateDto.setDebitExchangeType(!StringUtils.isEmpty(outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType())
                ? outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType()
                : "SPOT");

        ////if instructedccy same as credit ccy then set the credit exchange rate
        if(instructedCcy.equals(creditAcctccy)) {
            exchageRateDto.setCreditExchangeRate(BigDecimal.ONE);
        }else {
            exchageRateDto.setCreditExchangeRate(outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRate() != null
                    ? outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRate()
                    : BigDecimal.ZERO);
        }
        exchageRateDto.setCreditCcy(creditAcctccy);
        exchageRateDto.setCreditExchangeType( outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType() != null
                ? outwardRq.getIntlPmtInputRq().getTxnFXData().getExhangeRateDetails().getExchangeRateType()
                : "SPOT");
        
        return exchageRateDto;
    }

    /**
     * Method Description:Find by the Message Number. This is the paymentInfoId from openApi
     * 
     * @param messageNumber
     * @return
     */
    public IBOUB_SWT_RemittanceTable findByMessageNumber(String messageNumber) {
        ArrayList params = new ArrayList();
        params.add(messageNumber);
        List result = factory.findByQuery(IBOUB_SWT_RemittanceTable.BONAME, QUERY_TO_FIND_USING_MESSAGE_NUMBER, params, null,
                true);

        return CommonUtil.checkIfNotNullOrEmpty(result) ? (IBOUB_SWT_RemittanceTable) result.get(0) : null;
    }
}
