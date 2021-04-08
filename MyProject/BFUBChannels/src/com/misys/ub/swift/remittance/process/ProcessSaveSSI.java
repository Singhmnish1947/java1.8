package com.misys.ub.swift.remittance.process;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTSettlementInstructionDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTSettlementInstructions;
import com.trapedza.bankfusion.bo.refimpl.IBOUDFEXTSWTSettlementInstructions;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.fatoms.UB_PAY_GetAdhocSINumber;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.bankfusion.attributes.UserDefinedFields;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.BankToBankInfoDtls;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomer;
import bf.com.misys.cbs.types.swift.BeneficiaryInstitution;
import bf.com.misys.cbs.types.swift.IntermediaryDtls;
import bf.com.misys.cbs.types.swift.OrderingCustomer;
import bf.com.misys.cbs.types.swift.OrderingInstitution;
import bf.com.misys.cbs.types.swift.PayToDtls;
import bf.com.misys.cbs.types.swift.TermsAndConditionsInfo;
import bf.com.misys.cbs.types.swift.TextLines4;
import bf.com.misys.cbs.types.swift.TextLines6;

public class ProcessSaveSSI {

    private static final transient Log LOGGER = LogFactory.getLog(ProcessSaveSSI.class.getName());
    public static final String PARTY_IDENTIFIER_PREFIX = "//";

    public void saveSSI(Context context) {

        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");

        SwiftRemittanceRq swtRemitanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");

        boolean isSSISaveRequired = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getSaveSSI();
        String ssiDetailIDInRq = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSettlementInstrId();
        RsHeader rsHeader = new RsHeader();
        if (LOGGER.isInfoEnabled())
            LOGGER.info("START ProcessSaveSSI  ");

        MessageStatus txnStatus = new MessageStatus();
        txnStatus.setOverallStatus(PaymentSwiftConstants.SUCCESS);
        // the save of ssi will be done only if the ssiDetailID in the request is empty and saveSSI
        // is true in the incoming request
        if (isSSISaveRequired && StringUtils.isBlank(ssiDetailIDInRq)) {
            try {
                HashMap<String, Object> map = new HashMap<>();
                String customerCode = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID();
                String ISOCurrencyCode = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCurrencyCode();
                String TransactionType = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType();
                String whereClauseSSIFind = "WHERE " + IBOSWTSettlementInstructions.CUSTOMERCODE + " = ? AND "
                        + IBOSWTSettlementInstructions.ISOCURRENCYCODE + " = ? AND " + IBOSWTSettlementInstructions.MESSAGETYPE
                        + " = ?";
                String ssiID, ssiDetailID;
                ArrayList args = new ArrayList<>();
                args.add(customerCode);
                args.add(ISOCurrencyCode);
                args.add(TransactionType);
                ArrayList<IBOSWTSettlementInstructions> settlementInst = (ArrayList<IBOSWTSettlementInstructions>) BankFusionThreadLocal
                        .getPersistanceFactory()
                        .findByQuery(IBOSWTSettlementInstructions.BONAME, whereClauseSSIFind, args, null, true);
                if (null == settlementInst || settlementInst.size() == 0) {
                    ssiID = getGenerateID(customerCode, ISOCurrencyCode, "SIID", remittanceDto.getEnv());
                    IBOSWTSettlementInstructions ssiNew = (IBOSWTSettlementInstructions) BankFusionThreadLocal
                            .getPersistanceFactory().getStatelessNewInstance(IBOSWTSettlementInstructions.BONAME);
                    ssiNew.setBoID(ssiID);
                    ssiNew.setF_CUSTOMERCODE(customerCode);
                    ssiNew.setF_ISOCURRENCYCODE(ISOCurrencyCode);
                    ssiNew.setF_MESSAGETYPE(TransactionType);
                    ssiNew.setF_MESSAGECHANNEL("SWIFT");
                    ssiNew.setF_INSTRUCTIONIDENTIFIER(ssiID);
                    ssiNew.setF_DEBITACCOUNTID(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                            .getDebitPostingDtls().getDebitAccountId());
                    // setting the maximum date = 9999-12-31 05:30:00.0
                    Timestamp expiryDttm = new Timestamp(253402214400000L);
                    ssiNew.setF_EXPIRYDATE(expiryDttm);
                    BankFusionThreadLocal.getPersistanceFactory().create(IBOSWTSettlementInstructions.BONAME, ssiNew);
                    IBOUDFEXTSWTSettlementInstructions ssiUDF = (IBOUDFEXTSWTSettlementInstructions) BankFusionThreadLocal
                            .getPersistanceFactory().getStatelessNewInstance(IBOUDFEXTSWTSettlementInstructions.BONAME);
                    ssiUDF.setBoID(ssiID);
                    UserDefinedFields extension = new UserDefinedFields();
                    ssiUDF.setUserDefinedFields(extension);
                    BankFusionThreadLocal.getPersistanceFactory().create(IBOUDFEXTSWTSettlementInstructions.BONAME, ssiUDF);
                }
                else {
                    ssiID = settlementInst.get(0).getBoID();
                }

                IBOSWTSettlementInstructionDetail ssiDetailNew = (IBOSWTSettlementInstructionDetail) BankFusionThreadLocal
                        .getPersistanceFactory().getStatelessNewInstance(IBOSWTSettlementInstructionDetail.BONAME);
                ssiDetailID = getGenerateID(customerCode, ISOCurrencyCode, "SIIDDETAIL", remittanceDto.getEnv());
                ssiDetailNew.setBoID(ssiDetailID);
                ssiDetailNew.setF_PAY_RECEIVE_FLAG(CommonConstants.EMPTY_STRING);
                ssiDetailNew.setF_MESSAGE_NUMBER(getMessageNumber(ssiID, remittanceDto.getEnv()));
                ssiDetailNew.setF_UBSITYPE("N");
                ssiDetailNew.setF_UBCREATEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
                ssiDetailNew.setF_UBVALUATIONDATE(SystemInformationManager.getInstance().getBFBusinessDate());
                ssiDetailNew.setF_SETTLEMENTINSTRUCTIONSID(ssiID);

                ssiDetailNew = setSSISetails(swtRemitanceReq, ssiDetailNew);

                BankFusionThreadLocal.getPersistanceFactory().create(IBOSWTSettlementInstructionDetail.BONAME, ssiDetailNew);

                swtRemitterResp.getInitiateSwiftMessageRsDtls().setSettInstrId(ssiDetailID);
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().setSettlementInstrId(ssiDetailID);
            }

            catch (BankFusionException e) {
                SubCode subCode = new SubCode();
                LOGGER.error("Error in SSI creation", e);
                IEvent errors = e.getEvents().iterator().next();
                int error = e.getEvents().iterator().next().getEventNumber();
                String errorCode = Integer.toString(error);
                LOGGER.error("Error While SSI creation" + " Error Code [" + errorCode + "] and Error Message is ["
                        + e.getEvents().iterator().next().getMessage() + "]", e);
                Object parameterList;
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
        }

        rsHeader.setStatus(txnStatus);
        swtRemitterResp.setRsHeader(rsHeader);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("END ProcessSaveSSI  ");

        // setting the updated response object in he context for further reference like
        // errorHandling
        context.put("swtRemitterResp", swtRemitterResp);
        context.put("swtRemitanceReq", swtRemitanceReq);

    }

    private int getMessageNumber(String ssiID, BankFusionEnvironment env) {
        // TODO Auto-generated method stub
        UB_PAY_GetAdhocSINumber adhocSINumber = new UB_PAY_GetAdhocSINumber();
        adhocSINumber.setF_IN_settlementInstId(ssiID);
        adhocSINumber.process(env);
        return adhocSINumber.getF_OUT_AdhocSINumber();
    }

    private IBOSWTSettlementInstructionDetail setSSISetails(SwiftRemittanceRq swtRemitanceReq,
            IBOSWTSettlementInstructionDetail ssiDetailNew) {

        // setting the values for bank to bank info
        BankToBankInfoDtls bankToBankInfo = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getBankToBankInfo();
        ssiDetailNew.setF_BANK_ADDL_INSTRUCTION_CODE(bankToBankInfo.getBankAddlInstrCode());
        ssiDetailNew.setF_BANK_INSTRUCTION_CODE(bankToBankInfo.getBankInstructionCode());
        ssiDetailNew.setF_BANK_OPERATION_CODE(bankToBankInfo.getBankOperationCode());
        ssiDetailNew.setF_BANK_TO_BANK_INFO1(bankToBankInfo.getBankToBankInfo1());
        ssiDetailNew.setF_BANK_TO_BANK_INFO2(bankToBankInfo.getBankToBankInfo2());
        ssiDetailNew.setF_BANK_TO_BANK_INFO3(bankToBankInfo.getBankToBankInfo3());
        ssiDetailNew.setF_BANK_TO_BANK_INFO4(bankToBankInfo.getBankToBankInfo4());
        ssiDetailNew.setF_BANK_TO_BANK_INFO5(bankToBankInfo.getBankToBankInfo5());
        ssiDetailNew.setF_BANK_TO_BANK_INFO6(bankToBankInfo.getBankToBankInfo6());

        // setting beneficiary institution details
        BeneficiaryInstitution beneInstDtl = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryInstitution();
        ssiDetailNew.setF_BENEFICIARY_CODE(beneInstDtl.getBeneficiaryInstIdentifierCode());
        // Concatenating the party identifier
        String beneInstPartyIdentifier = (null == beneInstDtl.getBeneficiaryInstPartyClearingCode()) ? CommonConstants.EMPTY_STRING
                : beneInstDtl.getBeneficiaryInstPartyClearingCode() + beneInstDtl.getBeneficiaryInstPartyIdentifier();
        if (!StringUtils.isBlank(beneInstPartyIdentifier)) {
            ssiDetailNew.setF_BENEFICIARY_PARTY_IDENTIFIER(PARTY_IDENTIFIER_PREFIX + beneInstPartyIdentifier);
        }
        ssiDetailNew.setF_BENEFICIARY_TEXT1(beneInstDtl.getBeneficiaryInstDetails().getTextLine1());
        ssiDetailNew.setF_BENEFICIARY_TEXT2(beneInstDtl.getBeneficiaryInstDetails().getTextLine2());
        ssiDetailNew.setF_BENEFICIARY_TEXT3(beneInstDtl.getBeneficiaryInstDetails().getTextLine3());
        ssiDetailNew.setF_BENEFICIARY_TEXT4(beneInstDtl.getBeneficiaryInstDetails().getTextLine4());

        // setting charge code
        ssiDetailNew.setF_CHARGECODE(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode());

        // setting dr and cr account details
        ssiDetailNew.setF_CREDITACCOUNTID(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditAccountId());
        ssiDetailNew.setF_DEBITACCOUNTID(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId());

        // setting beneficiary customer info
        BeneficiaryCustomer beneCustDtl = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .getBeneficiaryCustomer();
        String benCustIdentifierType = beneCustDtl.getBeneficiaryCustPartyIdentifierCode();
        ssiDetailNew.setF_BeneficiaryCustomerIdentifierType(benCustIdentifierType);
        ssiDetailNew.setF_FOR_ACC_IDENTIFIERCODE(beneCustDtl.getBeneficiaryCustIdentifierCode());
        if (!StringUtils.isBlank(benCustIdentifierType) && !StringUtils.isBlank(beneCustDtl.getBeneficiaryCustPartyIdentifier())) {
            ssiDetailNew.setF_FOR_ACCOUNT_PARTY_IDENTIFIER(
                    new StringBuilder().append("/").append(beneCustDtl.getBeneficiaryCustPartyIdentifier()).toString());
        }
        else {
            ssiDetailNew.setF_FOR_ACCOUNT_PARTY_IDENTIFIER(beneCustDtl.getBeneficiaryCustPartyIdentifier());
        }
        ssiDetailNew.setF_FOR_ACCOUNT_TEXT1(beneCustDtl.getBeneficiaryCustDetails().getTextLine1());
        ssiDetailNew.setF_FOR_ACCOUNT_TEXT2(beneCustDtl.getBeneficiaryCustDetails().getTextLine2());
        ssiDetailNew.setF_FOR_ACCOUNT_TEXT3(beneCustDtl.getBeneficiaryCustDetails().getTextLine3());
        ssiDetailNew.setF_FOR_ACCOUNT_TEXT4(beneCustDtl.getBeneficiaryCustDetails().getTextLine4());

        // setting intermediary details
        IntermediaryDtls intermediaryDtl = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                .getIntermediary();
        ssiDetailNew.setF_INTERMEDIARY_CODE(intermediaryDtl.getIntermediaryIdentiferCode());
        String intermediaryPartyIdentifier = (null == intermediaryDtl.getIntermediaryPartyIdfrClrngCode())
                ? CommonConstants.EMPTY_STRING
                : intermediaryDtl.getIntermediaryPartyIdfrClrngCode() + intermediaryDtl.getIntermediaryPartyIdentifier();
        if (!StringUtils.isBlank(intermediaryPartyIdentifier)) {
            ssiDetailNew.setF_INTERMEDIARY_PARTY_IDENTIFIER(PARTY_IDENTIFIER_PREFIX + intermediaryPartyIdentifier);
        }
        ssiDetailNew.setF_INTERMEDIARY_TEXT1(intermediaryDtl.getIntermediaryDetails().getTextLine1());
        ssiDetailNew.setF_INTERMEDIARY_TEXT2(intermediaryDtl.getIntermediaryDetails().getTextLine2());
        ssiDetailNew.setF_INTERMEDIARY_TEXT3(intermediaryDtl.getIntermediaryDetails().getTextLine3());
        ssiDetailNew.setF_INTERMEDIARY_TEXT4(intermediaryDtl.getIntermediaryDetails().getTextLine4());

        // setting the remittance info
        ssiDetailNew.setF_NARRATIVE(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getDescription());

        // setting ordering customer details
        OrderingCustomer orderCustDtl = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                .getOrderingCustomer();
        ssiDetailNew.setF_ORDERINGCUST_IDENTIFIERCODE(orderCustDtl.getOrderingCustPartyIdentifierCode());
        String ordCustIdentifierType = orderCustDtl.getOrderingCustPartyIdentifierAcct();
        if (!StringUtils.isBlank(ordCustIdentifierType)) {
            if (("A".equals(ordCustIdentifierType) || "I".equals(ordCustIdentifierType))
                    && !StringUtils.isBlank(orderCustDtl.getOrderingCustPartyIdentiferAcctValue())) {
                ssiDetailNew.setF_ORDERINGCUSTOMERACCID(
                        new StringBuilder().append("/").append(orderCustDtl.getOrderingCustPartyIdentiferAcctValue()).toString());
            }
            else if (("P".equals(ordCustIdentifierType))
                    && !StringUtils.isBlank(orderCustDtl.getOrderingCustPartyIdentiferAcctValue())) {
                ssiDetailNew.setF_ORDERINGCUSTOMERACCID(
                        new StringBuilder().append("//").append(orderCustDtl.getOrderingCustPartyIdentiferAcctValue()).toString());
            }
        }
        else {
            ssiDetailNew.setF_ORDERINGCUSTOMERACCID(orderCustDtl.getOrderingCustPartyIdentiferAcctValue());
        }
        ssiDetailNew.setF_PARTYADDRESSLINE1(orderCustDtl.getOrderingCustDetails().getTextLine1());
        ssiDetailNew.setF_PARTYADDRESSLINE2(orderCustDtl.getOrderingCustDetails().getTextLine2());
        ssiDetailNew.setF_PARTYADDRESSLINE3(orderCustDtl.getOrderingCustDetails().getTextLine3());
        ssiDetailNew.setF_PARTYADDRESSLINE4(orderCustDtl.getOrderingCustDetails().getTextLine4());
        ssiDetailNew.setF_OrderingCustomerIdentifierType(ordCustIdentifierType);

        ssiDetailNew.setF_GENERATE103PLUSIND(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getGenerate103PlusInd() ? "Y" : "N");

        // setting ordering institution details
        OrderingInstitution orderInstDtl = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                .getOrderingInstitution();
        String orderingInstPartyIdentifier = (null == orderInstDtl.getOrderingInstPartyClearingCode())
                ? CommonConstants.EMPTY_STRING
                : orderInstDtl.getOrderingInstPartyClearingCode() + orderInstDtl.getOrderingInstPartyIdentifierCode();
        if (!StringUtils.isBlank(orderingInstPartyIdentifier)) {
            ssiDetailNew.setF_ORDERINGINSTITUTE_ACCID(PARTY_IDENTIFIER_PREFIX + orderingInstPartyIdentifier);
        }
        ssiDetailNew.setF_ORDERINGINSTITUTION(orderInstDtl.getOrderingInstIdentifierCode());
        ssiDetailNew.setF_ORDERINGINSTITUTIONDTL1(orderInstDtl.getOrderingInstitutionDtl().getOrderingInstitutionDtl1());
        ssiDetailNew.setF_ORDERINGINSTITUTIONDTL2(orderInstDtl.getOrderingInstitutionDtl().getOrderingInstitutionDtl2());
        ssiDetailNew.setF_ORDERINGINSTITUTIONDTL3(orderInstDtl.getOrderingInstitutionDtl().getOrderingInstitutionDtl3());
        ssiDetailNew.setF_ORDERINGINSTITUTIONDTL4(orderInstDtl.getOrderingInstitutionDtl().getOrderingInstitutionDtl4());

        // setting pay to details
        PayToDtls payToDtl = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo();
        ssiDetailNew.setF_PAY_TO_CODE(payToDtl.getPayToIdentifierCode());
        String payToPartyIdentifier = (null == payToDtl.getPayToPartyIdentifierClearingCode()) ? CommonConstants.EMPTY_STRING
                : payToDtl.getPayToPartyIdentifierClearingCode() + payToDtl.getPayToPartyIdentifier();
        if (!StringUtils.isBlank(payToPartyIdentifier)) {
            ssiDetailNew.setF_PAY_TO_PARTY_IDENTIFIER(PARTY_IDENTIFIER_PREFIX + payToPartyIdentifier);
        }
        ssiDetailNew.setF_PAY_TO_TEXT1(payToDtl.getPayToDetails().getPayDtls1());
        ssiDetailNew.setF_PAY_TO_TEXT2(payToDtl.getPayToDetails().getPayDtls2());
        ssiDetailNew.setF_PAY_TO_TEXT3(payToDtl.getPayToDetails().getPayDtls3());
        ssiDetailNew.setF_PAY_TO_TEXT4(payToDtl.getPayToDetails().getPayDtls4());

        // setting sender to receiver info
        TextLines6 senderToReceiverInfo = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getSenderToReceiverInfo();
        ssiDetailNew.setF_SENDER_TO_RECEIVER_INFO1(senderToReceiverInfo.getTextLine1());
        ssiDetailNew.setF_SENDER_TO_RECEIVER_INFO2(senderToReceiverInfo.getTextLine2());
        ssiDetailNew.setF_SENDER_TO_RECEIVER_INFO3(senderToReceiverInfo.getTextLine3());
        ssiDetailNew.setF_SENDER_TO_RECEIVER_INFO4(senderToReceiverInfo.getTextLine4());
        ssiDetailNew.setF_SENDER_TO_RECEIVER_INFO5(senderToReceiverInfo.getTextLine5());
        ssiDetailNew.setF_SENDER_TO_RECEIVER_INFO6(senderToReceiverInfo.getTextLine6());

        // setting terms and conditions
        TermsAndConditionsInfo termsAndConditions = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                .getTermsAndConditionsInfo();
        ssiDetailNew.setF_TERMS_CONDITIONS_INFO1(termsAndConditions.getTAndCInfoLine1());
        ssiDetailNew.setF_TERMS_CONDITIONS_INFO2(termsAndConditions.getTAndCInfoLine2());
        ssiDetailNew.setF_TERMS_CONDITIONS_INFO3(termsAndConditions.getTAndCInfoLine3());
        ssiDetailNew.setF_TERMS_CONDITIONS_INFO4(termsAndConditions.getTAndCInfoLine4());
        ssiDetailNew.setF_TERMS_CONDITIONS_INFO5(termsAndConditions.getTAndCInfoLine5());
        ssiDetailNew.setF_TERMS_CONDITIONS_INFO6(termsAndConditions.getTAndCInfoLine6());

        ssiDetailNew
                .setF_TXNTYPECODE(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getTxnTypeCode_tag26());

        // setting remittance info
        TextLines4 remittanceInfo = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getRemittanceInfo();
        ssiDetailNew.setF_PAY_DETAILS1(remittanceInfo.getTextLine1());
        ssiDetailNew.setF_PAY_DETAILS2(remittanceInfo.getTextLine2());
        ssiDetailNew.setF_PAY_DETAILS3(remittanceInfo.getTextLine3());
        ssiDetailNew.setF_PAY_DETAILS4(remittanceInfo.getTextLine4());

        return ssiDetailNew;
    }

    private String getGenerateID(String customerCode, String isoCurrencyCode, String formula, BankFusionEnvironment env) {
        String generatedID;
        HashMap<String, Object> map = new HashMap<>();
        map.put("CurrencyCode", isoCurrencyCode);
        map.put("CustomerCode", customerCode);
        map.put("PaymentChannel", "SWT");
        map.put("Formula", formula);
        Map outputParams = MFExecuter.executeMF(MFInputOutPutKeys.UB_PAY_GenerateSIID_SRV, env, map);
        generatedID = (String) outputParams.get("UNIQUEID");
        return generatedID;
    }

}
