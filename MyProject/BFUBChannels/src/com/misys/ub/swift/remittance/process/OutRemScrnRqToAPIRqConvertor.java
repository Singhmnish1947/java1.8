package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.charges.ChargeConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_OutRemScrnRqToAPIRqConvertor;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.Charge;
import bf.com.misys.cbs.types.Charges;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.swift.DocumentUploadDtls;
import bf.com.misys.cbs.types.swift.InitiateSwiftMessage;
import bf.com.misys.ub.types.remittanceprocess.DocumentUpload;
import bf.com.misys.ub.types.remittanceprocess.DocumentUploadList;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

public class OutRemScrnRqToAPIRqConvertor extends AbstractUB_SWT_OutRemScrnRqToAPIRqConvertor {
    /**
     * 
     */
    private static final long serialVersionUID = 1416368914387626814L;
    private transient final static Log LOGGER = LogFactory.getLog(OutRemScrnRqToAPIRqConvertor.class);

    public OutRemScrnRqToAPIRqConvertor() {

    }

    public OutRemScrnRqToAPIRqConvertor(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) {
        UB_SWT_RemittanceProcessRq swiftRemittanceRqScreen = getF_IN_remittanceScreenRq();
        SwiftRemittanceRq swiftRemittanceAPIRq = new SwiftRemittanceRq();
        // get DocumentUpload Details
        swiftRemittanceAPIRq = getDocumentUploadDetails(swiftRemittanceRqScreen, swiftRemittanceAPIRq);

        // getChargeDetails
        VectorTable chargeVector = (null != getF_IN_ChargeVector()) ? getF_IN_ChargeVector() : new VectorTable();
        swiftRemittanceAPIRq = convertChargeVectorToComplexType(chargeVector, swiftRemittanceAPIRq);

        setF_OUT_swiftRemittanceAPIRq(swiftRemittanceAPIRq);

    }

    /**
     * @param swiftRemittanceRqScreen
     * @param swiftRemittanceAPIRq
     * @return
     */
    private SwiftRemittanceRq getDocumentUploadDetails(UB_SWT_RemittanceProcessRq swiftRemittanceRqScreen,
            SwiftRemittanceRq swiftRemittanceAPIRq) {
        ArrayList<DocumentUploadDtls> docDtlsAPI = new ArrayList<>();
        InitiateSwiftMessage switdetail = new InitiateSwiftMessage();
        DocumentUploadList docDtls = swiftRemittanceRqScreen.getDOCUPLOAD();

        // piece of code to convert the screen document dtls object to api understandable document
        // dtls
        if (null != docDtls && null != docDtls.getDocumentUpload()) {
            for (DocumentUpload docDtl : docDtls.getDocumentUpload()) {
                DocumentUploadDtls documentUploadDtls = new DocumentUploadDtls();
                documentUploadDtls.setAttachedDate(docDtl.getAttachedDate());
                documentUploadDtls.setDescription(docDtl.getDescription());
                documentUploadDtls.setDocumentSavedId(docDtl.getDocumentSavedId());
                documentUploadDtls.setDocumentType(docDtl.getDocumentType());
                documentUploadDtls.setReferenceNumber(docDtl.getDocumentReference());
                docDtlsAPI.add(documentUploadDtls);
            }
            switdetail.setDocumentUpload(docDtlsAPI.toArray((DocumentUploadDtls[]) new DocumentUploadDtls[docDtlsAPI.size()]));
        }
        swiftRemittanceAPIRq.setInitiateSwiftMessageRqDtls(switdetail);
        return swiftRemittanceAPIRq;

    }

    /**
     * @param chargeVector
     * @param swiftRemittanceAPIRq
     * @return
     */
    public SwiftRemittanceRq convertChargeVectorToComplexType(VectorTable chargeVector, SwiftRemittanceRq swiftRemittanceAPIRq) {

        InitiateSwiftMessage switdetail = swiftRemittanceAPIRq.getInitiateSwiftMessageRqDtls();
        int vSize = chargeVector.size();
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("chargeVector:[" + chargeVector + "]");
            LOGGER.info("chargeVectorSize:[" + vSize + "]");
        }

        Map chargeRow;
        for (int j = 0; j < vSize; j++) {
            chargeRow = chargeVector.getRowTags(j);
            Charges charges = new Charges();
            Charge charge = new Charge();
            String cCode = (String) chargeRow.get(ChargeConstants.OCV_CHARGECODE);
            if (!StringUtils.isBlank(cCode)) {
                // ChargeCcyAmtDetails
                Currency chargeCcyAmtDetails = new Currency();
                chargeCcyAmtDetails.setAmount((BigDecimal) chargeRow.get(ChargeConstants.OCV_CHARGEAMOUNT));
                chargeCcyAmtDetails.setIsoCurrencyCode((String) chargeRow.get(ChargeConstants.OCV_CHARGECURRENCY));
                charge.setChargeCcyAmtDetails(chargeCcyAmtDetails);

                // chgCalculationCode
                charge.setChargeCalculationCode((String) chargeRow.get(ChargeConstants.OCV_CHARGECALCULATIONCODE));

                // chgCode
                charge.setChargeCode((String) chargeRow.get(ChargeConstants.OCV_CHARGECODE));

                // chgDescription
                // chgNarrative
                charge.setChargeNarrative((String) chargeRow.get(ChargeConstants.OCV_CHARGENARRATIVE));

                // chgRecAcc
                AccountKeys chargeRecAcctDetails = new AccountKeys();
                chargeRecAcctDetails.setStandardAccountId((String) chargeRow.get(ChargeConstants.OCV_CHARGERECIEVINGACCOUNT));
                charge.setChargeRecAcctDetails(chargeRecAcctDetails);

                // chgPosTxnCode
                charge.setChargePostingTxnCode((String) chargeRow.get(ChargeConstants.OCV_CHARGEPOSTINGTXNCODE));

                // chgAmountInAccCurr
                Currency chargeFundingAcctCcyDetails = new Currency();
                chargeFundingAcctCcyDetails
                        .setAmount((BigDecimal) chargeRow.get(ChargeConstants.OCV_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY));
                chargeFundingAcctCcyDetails.setIsoCurrencyCode((String) chargeRow.get(ChargeConstants.OCV_FUND_ACC_CURRENCY));
                charge.setFundingAcctCcyDetails(chargeFundingAcctCcyDetails);

                // taxTxnCode=CDP
                charge.setTaxTxnCode((String) chargeRow.get(ChargeConstants.OCV_TAXCODE));

                // TAXCODEVALUE=SCTAX
                charge.setTaxCode((String) chargeRow.get(ChargeConstants.OCV_TAXCODEVALUE));

                // set tax amount and currency
                Currency taxCcyAmtDetails = new Currency();
                taxCcyAmtDetails.setAmount((BigDecimal) chargeRow.get(ChargeConstants.OCV_TAXAMOUNT));
                taxCcyAmtDetails.setIsoCurrencyCode((String) chargeRow.get(ChargeConstants.OCV_TAXCURRENCY));
                charge.setTaxCcyAmtDetails(taxCcyAmtDetails);

                // TaxNarrative
                charge.setTaxNarrative((String) chargeRow.get(ChargeConstants.OCV_TAXNARRATIVE));

                // TaxRecAcctDetails
                AccountKeys taxRecAcct = new AccountKeys();
                taxRecAcct.setStandardAccountId((String) chargeRow.get(ChargeConstants.OCV_TAXRECIEVINGACCOUNT));
                charge.setTaxRecAcct(taxRecAcct);

                // ChargeExRateDetails
                ExchangeRateDetails chargeExRateDetails = new ExchangeRateDetails();
                chargeExRateDetails.setExchangeRate((BigDecimal) chargeRow.get(ChargeConstants.OCV_CHARGEEXCHANGERATE));
                chargeExRateDetails.setExchangeRateType((String) chargeRow.get(ChargeConstants.OCV_CHARGEEXCHANGERATETYPE));
                charge.setChargeExRateDetails(chargeExRateDetails);

                // taxExRateDetails
                ExchangeRateDetails taxExchangeRateDetails = new ExchangeRateDetails();
                taxExchangeRateDetails.setExchangeRate((BigDecimal) chargeRow.get(ChargeConstants.OCV_TAXEXCHANGERATE));
                taxExchangeRateDetails.setExchangeRateType((String) chargeRow.get(ChargeConstants.OCV_TAXEXCHANGERATETYPE));
                charge.setTaxExchangeRateDetails(taxExchangeRateDetails);

                // TaxFndAcctAmt
                Currency taxFndAcctAmtDetails = new Currency();
                taxFndAcctAmtDetails.setAmount((BigDecimal) chargeRow.get(ChargeConstants.OCV_TAXAMOUNT_IN_FUND_ACC_CURRENCY));
                taxFndAcctAmtDetails.setIsoCurrencyCode((String) chargeRow.get(ChargeConstants.OCV_FUND_ACC_CURRENCY));
                charge.setTaxFndAcctAmtDetails(taxFndAcctAmtDetails);

                // charge funding accountid
                AccountKeys fundingAccount = new AccountKeys();
                fundingAccount.setStandardAccountId((String) chargeRow.get(ChargeConstants.OCV_FUNDINGACCOUNTID));
                charge.setFundingAccount(fundingAccount);
            }
            // set the charges
            charges.setCharge(charge);
            switdetail.addCharges(charges);

        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("End of charges vector to complextype");
        }

        swiftRemittanceAPIRq.setInitiateSwiftMessageRqDtls(switdetail);
        return swiftRemittanceAPIRq;
    }
}
