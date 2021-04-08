package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.ExchangeRateDto;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.validation.ValidationHelper;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

/**
 * @author machamma.devaiah
 *
 */
public class GenerateSwiftMessage {
    private static final transient Log LOGGER = LogFactory.getLog(GenerateSwiftMessage.class.getName());

    /**
     * @param swtRemitanceReq
     * @param swtRemitterResp
     * @param remittanceDto
     * @return
     */
    @SuppressWarnings("unchecked")
    public RsHeader generateSwiftMsg(SwiftRemittanceRq swtRemitanceReq, SwiftRemittanceRs swtRemitterResp,
            RemittanceProcessDto remittanceDto) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("START ____GenerateSwiftMessage");
        }
        MessageStatus txnStatus = new MessageStatus();
        RsHeader rsHeader = new RsHeader();
        try {
            HashMap result = new HashMap();
            HashMap map = new HashMap();
            map.put(MFInputOutPutKeys.BankInstructionCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankInstructionCode());
            map.put(MFInputOutPutKeys.BankInstructionCodeText, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getBankToBankInfo().getBankAddlInstrCode());
            map.put(MFInputOutPutKeys.BankOperationcCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankOperationCode());
            map.put(MFInputOutPutKeys.BankToBankInformation1, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo1());
            map.put(MFInputOutPutKeys.BankToBankInformation2, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo2());
            map.put(MFInputOutPutKeys.BankToBankInformation3, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo3());
            map.put(MFInputOutPutKeys.BankToBankInformation4, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo4());
            map.put(MFInputOutPutKeys.BankToBankInformation5, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo5());
            map.put(MFInputOutPutKeys.BankToBankInformation6, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getBankToBankInfo().getBankToBankInfo6());
            map.put(MFInputOutPutKeys.BeneficairyCustomerIdentifierCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustIdentifierCode());
            map.put(MFInputOutPutKeys.BeneficairyCustomerPartyIdentifier, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustPartyIdentifier());
            map.put(MFInputOutPutKeys.BeneficairyInstituteIdentifierCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode());

            map.put(MFInputOutPutKeys.BeneficairyInstitutePartyIdentifier,
                    appendNccCode(
                            swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                                    .getBeneficiaryInstitution().getBeneficiaryInstPartyClearingCode(),
                            swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                                    .getBeneficiaryInstitution().getBeneficiaryInstPartyIdentifier()));
            map.put(MFInputOutPutKeys.BeneficiaryCustomerText1, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine1());
            map.put(MFInputOutPutKeys.BeneficiaryCustomerText2, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine2());
            map.put(MFInputOutPutKeys.BeneficiaryCustomerText3, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine3());
            map.put(MFInputOutPutKeys.BeneficiaryCustomerText4, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryCustomer().getBeneficiaryCustDetails().getTextLine4());
            map.put(MFInputOutPutKeys.BeneficiaryInstituteText1, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine1());
            map.put(MFInputOutPutKeys.BeneficiaryInstituteText2, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine2());
            map.put(MFInputOutPutKeys.BeneficiaryInstituteText3, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine3());
            map.put(MFInputOutPutKeys.BeneficiaryInstituteText4, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine4());
            map.put(MFInputOutPutKeys.CURRENCY, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode());
            map.put(MFInputOutPutKeys.ChargeAmount,
                    getChargeAmtForChargeOptionSHA(swtRemitanceReq, remittanceDto.getUbChargeAmt()));
            map.put(MFInputOutPutKeys.ChargeCode,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode());
            map.put(MFInputOutPutKeys.ChargeCurrency, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode());
            // contra amount
            BigDecimal contraAmount = getContraAmount(swtRemitanceReq);
            map.put(MFInputOutPutKeys.ContraAmount, contraAmount);
            map.put(MFInputOutPutKeys.Transaction_Amount, contraAmount);

            map.put(MFInputOutPutKeys.ContraAmount_CurrCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode());
            map.put(MFInputOutPutKeys.Contra_Account, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAccountId());
            map.put(MFInputOutPutKeys.CreditAccountNumber, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAccountId());
            map.put(MFInputOutPutKeys.Customer_Number,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID());
            // changed from message reference to sender reference becuase in openapi senderReference
            // is mangatory
            map.put(MFInputOutPutKeys.Deal_Number,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
            // map.put(MFInputOutPutKeys.DebitAccountNumber,swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId());
            map.put(MFInputOutPutKeys.EndToEndRef, swtRemitterResp.getInitiateSwiftMessageRsDtls().getUETR());
            map.put(MFInputOutPutKeys.ExchangeRate, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditExchangeRate());
            map.put(MFInputOutPutKeys.FXTransaction, 7);
            // suppress intructed amount based on logic
            map.put(MFInputOutPutKeys.FundingAmount, getSuppressInstructedAmt(swtRemitanceReq));
            map.put(MFInputOutPutKeys.Generate103Plus,
                    (swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getGenerate103PlusInd()) ? "Y" : "N");
            map.put(MFInputOutPutKeys.IntermediaryIdentifierCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getIntermediaryDetails().getIntermediary().getIntermediaryIdentiferCode());
            map.put(MFInputOutPutKeys.IntermediaryPartyIdentifier,
                    appendNccCode(
                            swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                                    .getIntermediaryPartyIdfrClrngCode(),
                            swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary()
                                    .getIntermediaryPartyIdentifier()));
            map.put(MFInputOutPutKeys.IntermediaryText1, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getIntermediary().getIntermediaryDetails().getTextLine1());
            map.put(MFInputOutPutKeys.IntermediaryText2, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getIntermediary().getIntermediaryDetails().getTextLine2());
            map.put(MFInputOutPutKeys.IntermediaryText3, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getIntermediary().getIntermediaryDetails().getTextLine3());
            map.put(MFInputOutPutKeys.IntermediaryText4, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getIntermediary().getIntermediaryDetails().getTextLine4());
            map.put(MFInputOutPutKeys.KeyCurrency, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode());
            map.put(MFInputOutPutKeys.Main_account, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitAccountId());
            map.put(MFInputOutPutKeys.MessageType,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType());
            map.put(MFInputOutPutKeys.Narrative,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration());

            // Ordering customer details (line 1,2,3,4) mapped to PARTYADDRESSLINE1 and so on.
            map.put(MFInputOutPutKeys.PartyIdentifierAddress1, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine1());
            map.put(MFInputOutPutKeys.PartyIdentifierAddress2, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine2());
            map.put(MFInputOutPutKeys.PartyIdentifierAddress3, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine3());
            map.put(MFInputOutPutKeys.PartyIdentifierAddress4, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustDetails().getTextLine4());

            map.put(MFInputOutPutKeys.OrderingCustomerIdentifierCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustIdentifierCode());
            map.put(MFInputOutPutKeys.OrderingCustomerPartyIdentifier, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustPartyIdentiferAcctValue());
            map.put(MFInputOutPutKeys.OrderingCustomeridentifiercombo, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingCustomer().getOrderingCustPartyIdentifierAcct());
            map.put(MFInputOutPutKeys.OrderingInstitueIdentifierCode, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getOrderingCustomerAndInstitution().getOrderingInstitution().getOrderingInstIdentifierCode());
            map.put(MFInputOutPutKeys.OrderingInstitueNameAndAddress1,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution().getOrderingInstitution()
                            .getOrderingInstitutionDtl().getOrderingInstitutionDtl1());
            map.put(MFInputOutPutKeys.OrderingInstitueNameAndAddress2,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution().getOrderingInstitution()
                            .getOrderingInstitutionDtl().getOrderingInstitutionDtl2());
            map.put(MFInputOutPutKeys.OrderingInstitueNameAndAddress3,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution().getOrderingInstitution()
                            .getOrderingInstitutionDtl().getOrderingInstitutionDtl3());
            map.put(MFInputOutPutKeys.OrderingInstitueNameAndAddress4,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution().getOrderingInstitution()
                            .getOrderingInstitutionDtl().getOrderingInstitutionDtl4());
            map.put(MFInputOutPutKeys.OrderingInstituePartyIdentifier,
                    appendNccCode(
                            swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                                    .getOrderingInstitution().getOrderingInstPartyClearingCode(),
                            swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                                    .getOrderingInstitution().getOrderingInstPartyIdentifierCode()));
            map.put(MFInputOutPutKeys.OrigChannelId, swtRemitanceReq.getRqHeader().getOrig().getChannelId());
            map.put(MFInputOutPutKeys.PayToIdentifierCode,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToIdentifierCode());
            map.put(MFInputOutPutKeys.PayToPartyIdentifier, appendNccCode(
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                            .getPayToPartyIdentifierClearingCode(),
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifier()));
            map.put(MFInputOutPutKeys.PayToText1, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getPayTo().getPayToDetails().getPayDtls1());
            map.put(MFInputOutPutKeys.PayToText2, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getPayTo().getPayToDetails().getPayDtls2());
            map.put(MFInputOutPutKeys.PayToText3, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getPayTo().getPayToDetails().getPayDtls3());
            map.put(MFInputOutPutKeys.PayToText4, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    .getPayTo().getPayToDetails().getPayDtls4());
            map.put(MFInputOutPutKeys.Pay_Receive_Flag, "1");
            map.put(MFInputOutPutKeys.PaymentDetails1,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine1());
            map.put(MFInputOutPutKeys.PaymentDetails2,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine2());
            map.put(MFInputOutPutKeys.PaymentDetails3,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine3());
            map.put(MFInputOutPutKeys.PaymentDetails4,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo().getTextLine4());
            map.put(MFInputOutPutKeys.Post_Date, SystemInformationManager.getInstance().getBFBusinessDate());
            map.put(MFInputOutPutKeys.PurchaseCurrency, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode());

            map.put(MFInputOutPutKeys.ReceiverChargeAmount,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getAmount());

            map.put(MFInputOutPutKeys.SenderToReceiverInformation1, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine1());
            map.put(MFInputOutPutKeys.SenderToReceiverInformation2, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine2());
            map.put(MFInputOutPutKeys.SenderToReceiverInformation3, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine3());
            map.put(MFInputOutPutKeys.SenderToReceiverInformation4, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine4());
            map.put(MFInputOutPutKeys.SenderToReceiverInformation5, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine5());
            map.put(MFInputOutPutKeys.SenderToReceiverInformation6, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getRemittanceDetails().getSenderToReceiverInfo().getTextLine6());
            map.put(MFInputOutPutKeys.Settl_Instruction_Number, 1);
            map.put(MFInputOutPutKeys.SoldCurrency, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode());
            map.put(MFInputOutPutKeys.TermsAndConditions1, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getTermsAndConditionsInfo().getTAndCInfoLine1());
            map.put(MFInputOutPutKeys.TermsAndConditions2, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getTermsAndConditionsInfo().getTAndCInfoLine2());
            map.put(MFInputOutPutKeys.TermsAndConditions3, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getTermsAndConditionsInfo().getTAndCInfoLine3());
            map.put(MFInputOutPutKeys.TermsAndConditions4, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getTermsAndConditionsInfo().getTAndCInfoLine4());
            map.put(MFInputOutPutKeys.TermsAndConditions5, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getTermsAndConditionsInfo().getTAndCInfoLine5());
            map.put(MFInputOutPutKeys.TermsAndConditions6, swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                    .getTermsAndConditionsInfo().getTAndCInfoLine6());
            map.put(MFInputOutPutKeys.TransactionID, swtRemitterResp.getInitiateSwiftMessageRsDtls().getHostTxnId());
            // TxnType_26 Tag
            map.put(MFInputOutPutKeys.TrasnactionCode,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getTxnTypeCode_tag26());
            map.put(MFInputOutPutKeys.Value_Date,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getValueDate());
            map.put(MFInputOutPutKeys.MESSAGE_PREFERENCE,
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessagePreference());
            map.put(MFInputOutPutKeys.WalkInCustomer, Boolean.TRUE);
            map.put(MFInputOutPutKeys.code_word, "NEW");
            map.put(MFInputOutPutKeys.isNonSTP, Boolean.TRUE);
            map.put(MFInputOutPutKeys.INSTRUCTED_AMT_CURRENCY, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getInstructedAmount().getIsoCurrencyCode());
            map.put(MFInputOutPutKeys.Credit_ExchangeRate_Type, swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getCreditPostingDtls().getCreditExchangeRateType());

            map.put(MFInputOutPutKeys.BeneficiaryInstituteText5, StringUtils.EMPTY);
            map.put(MFInputOutPutKeys.IntermediaryText5, StringUtils.EMPTY);
            map.put(MFInputOutPutKeys.OrderingInstitueNameAndAddress5, StringUtils.EMPTY);
            map.put(MFInputOutPutKeys.PayToText5, StringUtils.EMPTY);

            String contraCustId = PaymentSwiftUtils.getCustomerCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getCreditPostingDtls().getCreditAccountId());
            map.put(MFInputOutPutKeys.DRCRConfirmFlag, generate910(remittanceDto.getCreditTransactionCode(), contraCustId));

            result = MFExecuter.executeMF(MFInputOutPutKeys.UB_SWT_MessageValidator_SRV, remittanceDto.getEnv(), map);
            if (result != null) {
                txnStatus.setOverallStatus("S");
            }

        }
        catch (BankFusionException e) {
            SubCode subCode = new SubCode();
            LOGGER.error("Error in swift message validator", e);
            IEvent errors = e.getEvents().iterator().next();
            int error = e.getEvents().iterator().next().getEventNumber();
            if (error == 0) {
                error = 40409731;
            }
            String errorCode = Integer.toString(error);
            Object parameterList = new Object();
            if (errors.getDetails() != null && errors.getDetails().length != 0) {
                for (int i = 0; i < errors.getDetails().length; i++) {
                    EventParameters parameter = new EventParameters();
                    parameterList = errors.getDetails()[i];
                    parameter.setEventParameterValue(parameterList.toString());
                    subCode.addParameters(parameter);
                }
            }
            subCode.setCode(errorCode);
            subCode.setDescription(e.getEvents().iterator().next().getMessage());
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            txnStatus.addCodes(subCode);
            txnStatus.setOverallStatus("E");
        }

        rsHeader.setStatus(txnStatus);

        LOGGER.info("END ____PostSwiftTranscation");
        return rsHeader;
    }

    /**
     * @param swtRemitanceReq
     * @return
     */
    private BigDecimal getSuppressInstructedAmt(SwiftRemittanceRq swtRemitanceReq) {
        BigDecimal fundingAmount = null;
        BigDecimal chargeDetails = null;
        boolean suppressInstructedAmt = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getSuppressInstructedAmt();

        chargeDetails = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getAmount();

        // for SHA
        if (swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode()
                .equals(PaymentSwiftConstants.CHARGE_CODE_SHA)) {

            IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            String field71f = (String) bizInfo.getModuleConfigurationValue(PaymentSwiftConstants.MODULE_ID,
                    PaymentSwiftConstants.SHOW_SENDER_CHARGE_SHA, BankFusionThreadLocal.getBankFusionEnvironment());

            if (field71f.equalsIgnoreCase(PaymentSwiftConstants.NO)) {
                fundingAmount = fundingAmtforSHA_N_Case(suppressInstructedAmt, swtRemitanceReq);
            }
            else {
                fundingAmount = fundingAmtforSHA_Y_Case(suppressInstructedAmt, chargeDetails, swtRemitanceReq);
            }
        }
        else if (suppressInstructedAmt && chargeDetails.compareTo(BigDecimal.ZERO) == 0) {
            fundingAmount = BigDecimal.ZERO;
        }
        else {
            fundingAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount()
                    .getAmount();
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("FUNDING AMOUNT:::: " + fundingAmount);
        }

        return fundingAmount;

    }

    /**
     * Method Description:Funding amount when the module configuration under SWIFT module for
     * SHOWSENDERCHARGESHA = "N"
     * 
     * @param suppressInstructedAmt
     * @param swtRemitanceReq
     * @return
     */
    private BigDecimal fundingAmtforSHA_N_Case(Boolean suppressInstructedAmt, SwiftRemittanceRq swtRemitanceReq) {
        BigDecimal fundingAmount = BigDecimal.ZERO;
        if (suppressInstructedAmt) {
            fundingAmount = BigDecimal.ZERO;
        }
        else {
            fundingAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount()
                    .getAmount();
        }
        return fundingAmount;
    }

    /**
     * Method Description:Funding amount when the module configuration under SWIFT module for
     * SHOWSENDERCHARGESHA = "Y"
     * 
     * @param suppressInstructedAmt
     * @param chargeDetailAmount
     * @param swtRemitanceReq
     * @return
     */
    private BigDecimal fundingAmtforSHA_Y_Case(Boolean suppressInstructedAmt, BigDecimal chargeDetailAmount,
            SwiftRemittanceRq swtRemitanceReq) {
        BigDecimal fundingAmount = BigDecimal.ZERO;
        if (suppressInstructedAmt && chargeDetailAmount.compareTo(BigDecimal.ZERO) == 0) {
            fundingAmount = BigDecimal.ZERO;
        }
        else if (suppressInstructedAmt && chargeDetailAmount.compareTo(BigDecimal.ZERO) > 0) {
            fundingAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount()
                    .getAmount();
        }
        else {
            fundingAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount()
                    .getAmount();

        }
        return fundingAmount;
    }

    /**
     * @param swtRemitanceReq
     * @param chargeAmount
     * @return
     */
    private BigDecimal getChargeAmtForChargeOptionSHA(SwiftRemittanceRq swtRemitanceReq, BigDecimal chargeAmount) {
        if (swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode()
                .equals(PaymentSwiftConstants.CHARGE_CODE_SHA)) {
            chargeAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getAmount();
        }
        return chargeAmount;
    }

    /**
     * @param swtRemitanceReq
     * @return
     */
    private BigDecimal getContraAmount(SwiftRemittanceRq swtRemitanceReq) {
        OutRemScreenEventHandler utils = new OutRemScreenEventHandler();
        ExchangeRateDto creditExchDtls = utils.getCreditExchRateDetails(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getIsoCurrencyCode(),
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount()
                        .getIsoCurrencyCode(),
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                        .getCreditExchangeRateType(),
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getAmount(),
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                        .getCreditExchangeRate());
        return creditExchDtls.getCreditAmount();

    }

    /**
     * Method Description:Append NCC clearing codes
     * 
     * @param nccCode
     * @param IdentifierCodeValue
     * @return
     */
    private String appendNccCode(String nccCode, String identifierCodeValue) {
        ValidationHelper helper = new ValidationHelper();
        String appendedNccCode = helper.appendNccCode(identifierCodeValue, nccCode);
        return appendedNccCode;
    }

    private String generate910(String crTxnCode, String custCode) {
        String drCrValue = null;
        String crConfirm = null;
        IBOMisTransactionCodes iboMisCode = PaymentSwiftUtils.getMisTransactionCodes(crTxnCode);
        IBOSwtCustomerDetail iboSwtCustDet = PaymentSwiftUtils.getSwtCustomerDetail(custCode);

        if (iboMisCode != null) {

            drCrValue = iboMisCode.getF_SWTDRCRCONFIRMATION();
        }

        if (iboSwtCustDet != null) {

            crConfirm = iboSwtCustDet.getF_CRCONFIRMREQUIRED();
        }

        if ("Y".equalsIgnoreCase(crConfirm) && "1".equalsIgnoreCase(drCrValue)) {
            return drCrValue;
        }
        else {
            return "9";
        }
    }

}
