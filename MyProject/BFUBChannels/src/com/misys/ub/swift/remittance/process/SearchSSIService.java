package com.misys.ub.swift.remittance.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.validation.ValidationHelper;
import com.trapedza.bankfusion.core.VectorTable;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.swift.BankToBankInfoDtls;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomer;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.BeneficiaryInstitution;
import bf.com.misys.cbs.types.swift.IntermediaryDetails;
import bf.com.misys.cbs.types.swift.IntermediaryDtls;
import bf.com.misys.cbs.types.swift.OrderingCustomer;
import bf.com.misys.cbs.types.swift.OrderingCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.OrderingInstitution;
import bf.com.misys.cbs.types.swift.OrderingInstitutionDtl;
import bf.com.misys.cbs.types.swift.PayDtlsText;
import bf.com.misys.cbs.types.swift.PayToDtls;
import bf.com.misys.cbs.types.swift.RemittanceDetails;
import bf.com.misys.cbs.types.swift.TermsAndConditionsInfo;
import bf.com.misys.cbs.types.swift.TextLines4;
import bf.com.misys.cbs.types.swift.TextLines6;

/**
 * Search SSI
 *
 */
public class SearchSSIService {

    /**
     * @param outputParams
     * @param swtRemitanceReq
     * @param swtRemitterResp
     * @param remittanceDto
     * @return
     */
    public SwiftRemittanceRq searchSSIFromDetailID(Map outputParams, SwiftRemittanceRq swtRemitanceReq,
            SwiftRemittanceRs swtRemitterResp, RemittanceProcessDto remittanceDto) {
        VectorTable result = (VectorTable) outputParams.get("Result");
        String txnType = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType();
        if (null != result && result.size() > 0) {
            Map resultMap = result.getSelectedRow(result.getSelectedRowIndex());
            if (null != resultMap) {
                OrderingCustomerAndInstitution orderingCustomerAndInstitution = (null != swtRemitanceReq
                        .getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution())
                                ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()
                                : new OrderingCustomerAndInstitution();
                if (OutRemScreenEventHandler.MT103.equals(txnType)) {
                    // setting the ordering customer values
                    OrderingCustomer orderingCustomer = null != orderingCustomerAndInstitution.getOrderingCustomer()
                            ? orderingCustomerAndInstitution.getOrderingCustomer()
                            : new OrderingCustomer();
                    TextLines4 orderingCustDetails = new TextLines4();
                    orderingCustDetails.setTextLine1((String) resultMap.get("PARTYADDRESSLINE1"));
                    orderingCustDetails.setTextLine2((String) resultMap.get("PARTYADDRESSLINE2"));
                    orderingCustDetails.setTextLine3((String) resultMap.get("PARTYADDRESSLINE3"));
                    orderingCustDetails.setTextLine4((String) resultMap.get("PARTYADDRESSLINE4"));
                    orderingCustomer.setOrderingCustDetails(orderingCustDetails);
                    orderingCustomer.setOrderingCustIdentifierCode((String) resultMap.get("ORDERINGCUST_IDENTIFIERCODE"));
                    String ordCustIdType = (String) resultMap.get("OrderingCustomerIdentifierType");
                    orderingCustomer.setOrderingCustPartyIdentifierAcct(ordCustIdType);
                    String ordCustPartyIdAcctValue = (String) resultMap.get("ORDERINGCUSTOMERACCID");
                    // this logic is obtained from the amend SSI screen data population service
                    if (!StringUtils.isBlank(ordCustPartyIdAcctValue) && ("A".equals(ordCustIdType) || "I".equals(ordCustIdType))) {
                        orderingCustomer.setOrderingCustPartyIdentiferAcctValue(ordCustPartyIdAcctValue.substring(1));
                    }
                    else if (!StringUtils.isBlank(ordCustPartyIdAcctValue) && "P".equals(ordCustIdType)) {
                        orderingCustomer.setOrderingCustPartyIdentiferAcctValue(ordCustPartyIdAcctValue.substring(2));
                    }
                    else {
                        orderingCustomer.setOrderingCustPartyIdentiferAcctValue(ordCustPartyIdAcctValue);
                    }
                    orderingCustomerAndInstitution.setOrderingCustomer(orderingCustomer);
                }

                // setting the ordering institution details
                OrderingInstitution orderingInstitution = new OrderingInstitution();
                orderingInstitution.setOrderingInstIdentifierCode((String) resultMap.get("ORDERINGINSTITUTION"));
                OrderingInstitutionDtl orderingInstitutionDtl = new OrderingInstitutionDtl();
                if (!StringUtils.isBlank(orderingInstitution.getOrderingInstIdentifierCode())) {
                    String[] codeDtls = getIdentifierCodeDtls(orderingInstitution.getOrderingInstIdentifierCode(), remittanceDto);
                    orderingInstitutionDtl.setOrderingInstitutionDtl1(codeDtls[0]);
                    orderingInstitutionDtl.setOrderingInstitutionDtl2(codeDtls[1]);
                    orderingInstitutionDtl.setOrderingInstitutionDtl3(codeDtls[2]);
                }
                // the below setter code can be removed
                orderingInstitutionDtl.setOrderingInstitutionDtl4((String) resultMap.get("ORDERINGINSTITUTIONDTL4"));
                orderingInstitution.setOrderingInstitutionDtl(orderingInstitutionDtl);

                String orderingInstPartyClearingCode = CommonConstants.EMPTY_STRING;
                String orderingInstPartyIdentifierCode = CommonConstants.EMPTY_STRING;
                String nccOrderingInstCode = (String) resultMap.get("ORDERINGINSTITUTE_ACCID");
                if (!StringUtils.isBlank(nccOrderingInstCode) && nccOrderingInstCode.length() >= 4) {
                    orderingInstPartyClearingCode = nccOrderingInstCode.substring(2, 4);
                    orderingInstPartyIdentifierCode = nccOrderingInstCode.substring(4);
                }
                orderingInstitution.setOrderingInstPartyClearingCode(orderingInstPartyClearingCode);
                orderingInstitution.setOrderingInstPartyIdentifierCode(orderingInstPartyIdentifierCode);

                orderingCustomerAndInstitution.setOrderingInstitution(orderingInstitution);

                // setting beneficiary customer details
                BeneficiaryCustomerAndInstitution beneficiaryCustomerAndInstitution = new BeneficiaryCustomerAndInstitution();
                if (OutRemScreenEventHandler.MT103.equals(txnType)) {
                    BeneficiaryCustomer beneficiaryCustomer = new BeneficiaryCustomer();
                    beneficiaryCustomer.setBeneficiaryCustIdentifierCode((String) resultMap.get("FOR_ACC_IDENTIFIERCODE"));
                    TextLines4 beneficiaryCustDetails = new TextLines4();
                    if (!StringUtils.isBlank(beneficiaryCustomer.getBeneficiaryCustIdentifierCode())) {
                        String[] codeDtls = getIdentifierCodeDtls(beneficiaryCustomer.getBeneficiaryCustIdentifierCode(),
                                remittanceDto);
                        beneficiaryCustDetails.setTextLine1(codeDtls[0]);
                        beneficiaryCustDetails.setTextLine2(codeDtls[1]);
                        beneficiaryCustDetails.setTextLine3(codeDtls[2]);
                    }
                    beneficiaryCustDetails.setTextLine4((String) resultMap.get("FOR_ACCOUNT_TEXT4"));
                    beneficiaryCustomer.setBeneficiaryCustDetails(beneficiaryCustDetails);
                    String beneCustIdType = (String) resultMap.get("BeneficiaryCustomerIdentifierType");
                    beneficiaryCustomer.setBeneficiaryCustPartyIdentifierCode(beneCustIdType);
                    String beneCustPartyIdentifier = (String) resultMap.get("FOR_ACCOUNT_PARTY_IDENTIFIER");
                    if (!StringUtils.isBlank(beneCustIdType) && !StringUtils.isBlank(beneCustPartyIdentifier)) {
                        beneficiaryCustomer.setBeneficiaryCustPartyIdentifier(beneCustPartyIdentifier.substring(1));
                    }
                    else {
                        beneficiaryCustomer.setBeneficiaryCustPartyIdentifier(beneCustPartyIdentifier);
                    }
                    beneficiaryCustomerAndInstitution.setBeneficiaryCustomer(beneficiaryCustomer);
                }

                // setting beneficiary institution details
                BeneficiaryInstitution beneficiaryInstitution = new BeneficiaryInstitution();
                beneficiaryInstitution.setBeneficiaryInstIdentifierCode((String) resultMap.get("BENEFICIARY_CODE"));
                TextLines4 beneficiaryInstDetails = new TextLines4();
                if (!StringUtils.isBlank(beneficiaryInstitution.getBeneficiaryInstIdentifierCode())) {
                    String[] codeDtls = getIdentifierCodeDtls(beneficiaryInstitution.getBeneficiaryInstIdentifierCode(),
                            remittanceDto);
                    beneficiaryInstDetails.setTextLine1(codeDtls[0]);
                    beneficiaryInstDetails.setTextLine2(codeDtls[1]);
                    beneficiaryInstDetails.setTextLine3(codeDtls[2]);
                }
                beneficiaryInstDetails.setTextLine4((String) resultMap.get("BENEFICIARY_TEXT4"));
                beneficiaryInstitution.setBeneficiaryInstDetails(beneficiaryInstDetails);
                String beneficiaryInstPartyClearingCode = CommonConstants.EMPTY_STRING;
                String beneficiaryInstPartyIdentifier = CommonConstants.EMPTY_STRING;
                String nccBeneInstCode = (String) resultMap.get("BENEFICIARY_PARTY_IDENTIFIER");
                if (!StringUtils.isBlank(nccBeneInstCode) && nccBeneInstCode.length() >= 4) {
                    beneficiaryInstPartyClearingCode = nccBeneInstCode.substring(2, 4);
                    beneficiaryInstPartyIdentifier = nccBeneInstCode.substring(4);
                }
                beneficiaryInstitution.setBeneficiaryInstPartyClearingCode(beneficiaryInstPartyClearingCode);
                beneficiaryInstitution.setBeneficiaryInstPartyIdentifier(beneficiaryInstPartyIdentifier);

                beneficiaryCustomerAndInstitution.setBeneficiaryInstitution(beneficiaryInstitution);
                swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                        .setBeneficiaryCustomerAndInstitution(beneficiaryCustomerAndInstitution);

                // setting pay to details
                PayToDtls payToDtls = new PayToDtls();
                payToDtls.setPayToIdentifierCode((String) resultMap.get("PAY_TO_CODE"));
                PayDtlsText payToDetails = new PayDtlsText();
                if (!StringUtils.isBlank(payToDtls.getPayToIdentifierCode())) {
                    String[] codeDtls = getIdentifierCodeDtls(payToDtls.getPayToIdentifierCode(), remittanceDto);
                    payToDetails.setPayDtls1(codeDtls[0]);
                    payToDetails.setPayDtls2(codeDtls[1]);
                    payToDetails.setPayDtls3(codeDtls[2]);
                }
                payToDetails.setPayDtls4((String) resultMap.get("PAY_TO_TEXT4"));
                payToDtls.setPayToDetails(payToDetails);
                String payToPartyIdentifierClearingCode = CommonConstants.EMPTY_STRING;
                String payToPartyIdentifier = CommonConstants.EMPTY_STRING;
                String nccPayToCode = (String) resultMap.get("PAY_TO_PARTY_IDENTIFIER");
                if (!StringUtils.isBlank(nccPayToCode) && nccPayToCode.length() >= 4) {
                    payToPartyIdentifierClearingCode = nccPayToCode.substring(2, 4);
                    payToPartyIdentifier = nccPayToCode.substring(4);
                }
                payToDtls.setPayToPartyIdentifierClearingCode(payToPartyIdentifierClearingCode);
                payToDtls.setPayToPartyIdentifier(payToPartyIdentifier);

                // setting intermediary details
                IntermediaryDtls intermediaryDtls = new IntermediaryDtls();
                intermediaryDtls.setIntermediaryIdentiferCode((String) resultMap.get("INTERMEDIARY_CODE"));
                TextLines4 intermediaryDetails = new TextLines4();
                if (!StringUtils.isBlank(intermediaryDtls.getIntermediaryIdentiferCode())) {
                    String[] codeDtls = getIdentifierCodeDtls(intermediaryDtls.getIntermediaryIdentiferCode(), remittanceDto);
                    intermediaryDetails.setTextLine1(codeDtls[0]);
                    intermediaryDetails.setTextLine2(codeDtls[1]);
                    intermediaryDetails.setTextLine3(codeDtls[2]);
                }
                intermediaryDetails.setTextLine4((String) resultMap.get("INTERMEDIARY_TEXT4"));
                intermediaryDtls.setIntermediaryDetails(intermediaryDetails);
                String intermediaryPartyIdfrClrngCode = CommonConstants.EMPTY_STRING;
                String intermediaryPartyIdentifier = CommonConstants.EMPTY_STRING;
                String nccIntermediaryCode = (String) resultMap.get("INTERMEDIARY_PARTY_IDENTIFIER");
                if (!StringUtils.isBlank(nccIntermediaryCode) && nccIntermediaryCode.length() >= 4) {
                    intermediaryPartyIdfrClrngCode = nccIntermediaryCode.substring(2, 4);
                    intermediaryPartyIdentifier = nccIntermediaryCode.substring(4);
                }
                intermediaryDtls.setIntermediaryPartyIdfrClrngCode(intermediaryPartyIdfrClrngCode);
                intermediaryDtls.setIntermediaryPartyIdentifier(intermediaryPartyIdentifier);

                IntermediaryDetails intermediaryPayToDetails = new IntermediaryDetails();
                intermediaryPayToDetails.setIntermediary(intermediaryDtls);
                intermediaryPayToDetails.setPayTo(payToDtls);
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().setIntermediaryDetails(intermediaryPayToDetails);

                // setting remittance details
                RemittanceDetails remittanceDetailsInput = (null != swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                        .getRemittanceDetails()) ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()
                                : new RemittanceDetails();

                remittanceDetailsInput.setDescription((String) resultMap.get("NARRATIVE"));
                if (OutRemScreenEventHandler.MT103.equals(txnType)) {
                    remittanceDetailsInput.setTxnTypeCode_tag26((String) resultMap.get("TXNTYPECODE"));
                    remittanceDetailsInput.setChargeCode((String) resultMap.get("CHARGECODE"));

                    // setting bank to bank info
                    BankToBankInfoDtls bankToBankInfo = new BankToBankInfoDtls();
                    bankToBankInfo.setBankToBankInfo1((String) resultMap.get("BANK_TO_BANK_INFO1"));
                    bankToBankInfo.setBankToBankInfo2((String) resultMap.get("BANK_TO_BANK_INFO2"));
                    bankToBankInfo.setBankToBankInfo3((String) resultMap.get("BANK_TO_BANK_INFO3"));
                    bankToBankInfo.setBankToBankInfo4((String) resultMap.get("BANK_TO_BANK_INFO4"));
                    bankToBankInfo.setBankToBankInfo5((String) resultMap.get("BANK_TO_BANK_INFO5"));
                    bankToBankInfo.setBankToBankInfo6((String) resultMap.get("BANK_TO_BANK_INFO6"));
                    bankToBankInfo.setBankOperationCode((String) resultMap.get("BANK_OPERATION_CODE"));
                    bankToBankInfo.setBankInstructionCode((String) resultMap.get("BANK_INSTRUCTION_CODE"));
                    bankToBankInfo.setBankAddlInstrCode((String) resultMap.get("BANK_ADDL_INSTRUCTION_CODE"));
                    remittanceDetailsInput.setBankToBankInfo(bankToBankInfo);

                    // setting remittance info
                    TextLines4 remittanceInfo = new TextLines4();
                    remittanceInfo.setTextLine1((String) resultMap.get("PAY_DETAILS1"));
                    remittanceInfo.setTextLine2((String) resultMap.get("PAY_DETAILS2"));
                    remittanceInfo.setTextLine3((String) resultMap.get("PAY_DETAILS3"));
                    remittanceInfo.setTextLine4((String) resultMap.get("PAY_DETAILS4"));
                    remittanceDetailsInput.setRemittanceInfo(remittanceInfo);

                    // terms and conditions
                    TermsAndConditionsInfo termsAndConditionsInfo = new TermsAndConditionsInfo();
                    termsAndConditionsInfo.setTAndCInfoLine1((String) resultMap.get("TERMS_CONDITIONS_INFO1"));
                    termsAndConditionsInfo.setTAndCInfoLine2((String) resultMap.get("TERMS_CONDITIONS_INFO2"));
                    termsAndConditionsInfo.setTAndCInfoLine3((String) resultMap.get("TERMS_CONDITIONS_INFO3"));
                    termsAndConditionsInfo.setTAndCInfoLine4((String) resultMap.get("TERMS_CONDITIONS_INFO4"));
                    termsAndConditionsInfo.setTAndCInfoLine5((String) resultMap.get("TERMS_CONDITIONS_INFO5"));
                    termsAndConditionsInfo.setTAndCInfoLine6((String) resultMap.get("TERMS_CONDITIONS_INFO6"));
                    remittanceDetailsInput.setTermsAndConditionsInfo(termsAndConditionsInfo);
                }

                // setting sender to receiver info
                TextLines6 senderToReceiverInfo = new TextLines6();
                senderToReceiverInfo.setTextLine1((String) resultMap.get("SENDER_TO_RECEIVER_INFO1"));
                senderToReceiverInfo.setTextLine2((String) resultMap.get("SENDER_TO_RECEIVER_INFO2"));
                senderToReceiverInfo.setTextLine3((String) resultMap.get("SENDER_TO_RECEIVER_INFO3"));
                senderToReceiverInfo.setTextLine4((String) resultMap.get("SENDER_TO_RECEIVER_INFO4"));
                senderToReceiverInfo.setTextLine5((String) resultMap.get("SENDER_TO_RECEIVER_INFO5"));
                senderToReceiverInfo.setTextLine6((String) resultMap.get("SENDER_TO_RECEIVER_INFO6"));
                remittanceDetailsInput.setSenderToReceiverInfo(senderToReceiverInfo);

                swtRemitanceReq.getInitiateSwiftMessageRqDtls().setRemittanceDetails(remittanceDetailsInput);
            }
        }

        else {
            ValidationHelper helper = new ValidationHelper();
            RsHeader rsHeader;
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_SSI_ID_NOT_CORRECT, CommonConstants.EMPTY_STRING);
            swtRemitterResp.setRsHeader(rsHeader);
        }
        return swtRemitanceReq;
    }

    private String[] getIdentifierCodeDtls(String identifier, RemittanceProcessDto remittanceDto) {
        HashMap<String, Object> inputMap = new HashMap<>();
        String[] idCodeDtls = new String[3];
        inputMap.put("IdentifierCode", identifier);
        // the unexpected error is not handled here.
        HashMap<String, Object> outputParams = MFExecuter.executeMF(MFInputOutPutKeys.ID_CODE_READ_SRV, remittanceDto.getEnv(),
                inputMap);
        if (null != outputParams) {
            idCodeDtls[0] = (String) outputParams.get("InstitutionName");
            idCodeDtls[1] = (String) outputParams.get("City");
            idCodeDtls[2] = (String) outputParams.get("Location");
        }
        return idCodeDtls;
    }
}
