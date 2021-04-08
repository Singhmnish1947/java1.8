package com.misys.ub.swift.remittance.process;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_SearchSSIScreenDisplayHelper;

import bf.com.misys.ub.types.remittanceprocess.BENEFICIARYCUSTOMERINFO;
import bf.com.misys.ub.types.remittanceprocess.BENEFICIARYINSTDETIALS;
import bf.com.misys.ub.types.remittanceprocess.INTERMEDIARYDETAILS;
import bf.com.misys.ub.types.remittanceprocess.ORDERINGICUSTINFO;
import bf.com.misys.ub.types.remittanceprocess.ORDERINGINSTITUTIONDTL;
import bf.com.misys.ub.types.remittanceprocess.PAYTOPARTYDETAILS;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

public class SearchSSIScreenDisplayHelper extends AbstractUB_SWT_SearchSSIScreenDisplayHelper {

    /**
     * 
     */
    private static final long serialVersionUID = 2721277023734761174L;

    public SearchSSIScreenDisplayHelper() {

    }

    public SearchSSIScreenDisplayHelper(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) {
        VectorTable result = getF_IN_inputSSIResultVector();
        if (null != result && result.size() > 0) {
            Map resultMap = result.getSelectedRow(result.getSelectedRowIndex());
            if (null != resultMap) {
                UB_SWT_RemittanceProcessRq remittanceProcessRq = new UB_SWT_RemittanceProcessRq();

                setOrderingCustDtls(resultMap, remittanceProcessRq);

                ORDERINGINSTITUTIONDTL orderinstDtls = new ORDERINGINSTITUTIONDTL();
                String orderingInstPartyClearingCode = CommonConstants.EMPTY_STRING;
                String orderingInstPartyIdentifierCode = CommonConstants.EMPTY_STRING;
                String nccOrderingInstCode = (String) resultMap.get("ORDERINGINSTITUTE_ACCID");
                if (!StringUtils.isBlank(nccOrderingInstCode) && nccOrderingInstCode.length() >= 4) {
                    orderingInstPartyClearingCode = nccOrderingInstCode.substring(2, 4);
                    orderingInstPartyIdentifierCode = nccOrderingInstCode.substring(4);
                }
                orderinstDtls.setORDERINGINSTPARTYIDENTCODE(orderingInstPartyIdentifierCode);
                orderinstDtls.setORDERINGINSTPRTYIDNTCLRCODE(orderingInstPartyClearingCode);
                remittanceProcessRq.setORDERINGINSTITUTIONDTL(orderinstDtls);

                setBeneCustDtls(resultMap, remittanceProcessRq);

                BENEFICIARYINSTDETIALS beneInstDtls = new BENEFICIARYINSTDETIALS();
                String beneficiaryInstPartyClearingCode = CommonConstants.EMPTY_STRING;
                String beneficiaryInstPartyIdentifier = CommonConstants.EMPTY_STRING;
                String nccBeneInstCode = (String) resultMap.get("BENEFICIARY_PARTY_IDENTIFIER");
                if (!StringUtils.isBlank(nccBeneInstCode) && nccBeneInstCode.length() >= 4) {
                    beneficiaryInstPartyClearingCode = nccBeneInstCode.substring(2, 4);
                    beneficiaryInstPartyIdentifier = nccBeneInstCode.substring(4);
                }
                beneInstDtls.setBENEFICIARINSTYPARTYIDENTIFIER(beneficiaryInstPartyIdentifier);
                beneInstDtls.setBENPSRTYIDENTCLRCODE(beneficiaryInstPartyClearingCode);
                remittanceProcessRq.setBENEFICIARYINSTDETIALS(beneInstDtls);

                PAYTOPARTYDETAILS payToDtls = new PAYTOPARTYDETAILS();
                String payToPartyIdentifierClearingCode = CommonConstants.EMPTY_STRING;
                String payToPartyIdentifier = CommonConstants.EMPTY_STRING;
                String nccPayToCode = (String) resultMap.get("PAY_TO_PARTY_IDENTIFIER");
                if (!StringUtils.isBlank(nccPayToCode) && nccPayToCode.length() >= 4) {
                    payToPartyIdentifierClearingCode = nccPayToCode.substring(2, 4);
                    payToPartyIdentifier = nccPayToCode.substring(4);
                }
                payToDtls.setPAYTOPARTYIDENTIFIER(payToPartyIdentifier);
                payToDtls.setPAYTOPRTYIDNTCLRCODE(payToPartyIdentifierClearingCode);
                remittanceProcessRq.setPAYTOPARTYDETAILS(payToDtls);

                INTERMEDIARYDETAILS intermediaryDtls = new INTERMEDIARYDETAILS();
                String intermediaryPartyIdfrClrngCode = CommonConstants.EMPTY_STRING;
                String intermediaryPartyIdentifier = CommonConstants.EMPTY_STRING;
                String nccIntermediaryCode = (String) resultMap.get("INTERMEDIARY_PARTY_IDENTIFIER");
                if (!StringUtils.isBlank(nccIntermediaryCode) && nccIntermediaryCode.length() >= 4) {
                    intermediaryPartyIdfrClrngCode = nccIntermediaryCode.substring(2, 4);
                    intermediaryPartyIdentifier = nccIntermediaryCode.substring(4);
                }
                intermediaryDtls.setINTMDPRTYIDNTCLRCODE(intermediaryPartyIdfrClrngCode);
                intermediaryDtls.setINTMDPRTYIDNTIFR(intermediaryPartyIdentifier);
                remittanceProcessRq.setINTERMEDIARYDETAILS(intermediaryDtls);

                setF_OUT_remittanceProcessRq(remittanceProcessRq);
            }
        }
    }

    private void setBeneCustDtls(Map resultMap, UB_SWT_RemittanceProcessRq remittanceProcessRq) {
        String beneCustIdType = (String) resultMap.get("BeneficiaryCustomerIdentifierType");
        String beneCustPartyIdentifier = (String) resultMap.get("FOR_ACCOUNT_PARTY_IDENTIFIER");
        BENEFICIARYCUSTOMERINFO beneCustDtls = new BENEFICIARYCUSTOMERINFO();
        if (!StringUtils.isBlank(beneCustIdType) && !StringUtils.isBlank(beneCustPartyIdentifier)) {
            beneCustDtls.setBENEFICIARYCUSTPARTYIDENTIFIER(beneCustPartyIdentifier.substring(1));
        }
        else {
            beneCustDtls.setBENEFICIARYCUSTPARTYIDENTIFIER(beneCustPartyIdentifier);
        }
        remittanceProcessRq.setBENEFICIARYCUSTOMERINFO(beneCustDtls);
    }

    private void setOrderingCustDtls(Map resultMap, UB_SWT_RemittanceProcessRq remittanceProcessRq) {
        String ordCustIdType = (String) resultMap.get("OrderingCustomerIdentifierType");
        String ordCustPartyIdAcctValue = (String) resultMap.get("ORDERINGCUSTOMERACCID");
        ORDERINGICUSTINFO orderCustDtls = new ORDERINGICUSTINFO();
        if (!StringUtils.isBlank(ordCustPartyIdAcctValue) && ("A".equals(ordCustIdType) || "I".equals(ordCustIdType))) {
            orderCustDtls.setORDCUSTPTYIDENACCVALUE(ordCustPartyIdAcctValue.substring(1));
        }
        else if (!StringUtils.isBlank(ordCustPartyIdAcctValue) && "P".equals(ordCustIdType)) {
            orderCustDtls.setORDCUSTPTYIDENACCVALUE(ordCustPartyIdAcctValue.substring(2));
        }
        else {
            orderCustDtls.setORDCUSTPTYIDENACCVALUE(ordCustPartyIdAcctValue);
        }

        remittanceProcessRq.setORDERINGICUSTINFO(orderCustDtls);
    }
}
