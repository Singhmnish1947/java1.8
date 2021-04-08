/* ********************************************************************************
 *  Copyright(c)2019  Finastra. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */
package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.openapi.creditTransfer.v1.model.PreCalculatedCharges;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.PreCalculateChargeDto;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.Charge;
import bf.com.misys.cbs.types.Charges;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.ExchangeRateDetails;

public class PreCalculatedChargeCalculation {

    private static final Log LOGGER = LogFactory.getLog(PreCalculatedChargeCalculation.class);

    private PreCalculatedChargeCalculation() {
        throw new IllegalStateException("Utility class");
    }

    public static SwiftRemittanceRq addPreCalculateChargeToSwiftRq(SwiftRemittanceRq swtRemitanceReq,
            PreCalculatedCharges preCalCharge) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(" START PreCalculatedChargeCalculation");
        }
        
        if (swtRemitanceReq.getInitiateSwiftMessageRqDtls().getCharges() != null) {
            Charges[] txnCharges = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getCharges();

            if (txnCharges != null) {
                // if isSuppressAdditionalCharges=true exclude essence charge
                if (preCalCharge.isSuppressAdditionalCharges()) {
                    excludeEssenceCharge(swtRemitanceReq, preCalCharge);
                }
                else {
                    // if isSuppressAdditionalCharges=false then include essence charge
                    List<Charges> cbsChargesList = includeEssenceCharge(txnCharges, preCalCharge, swtRemitanceReq);

                    // add the list back to the swtRemitanceReq charge array
                    int cbsChargesCount = cbsChargesList.size();
                    Charges[] cbsChargesAray = cbsChargesList.toArray(new Charges[cbsChargesCount]);
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls().setCharges(cbsChargesAray);
                }
            }
            else {
                // if essence charge not configured then pre-claculated charge will be considered.
                if (preCalCharge != null && preCalCharge.getAmount().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                            .addCharges(excludeEssenceCharge(swtRemitanceReq, preCalCharge));
                }
            }

        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("END  PreCalculatedChargeCalculation");
        }

        return swtRemitanceReq;
    }

    /**
     * Method Description:Include both essence charge and pre-calculated charge
     * 
     * @param essenceTxnCharges
     * @param preCalCharge
     * @return
     */
    private static List<Charges> includeEssenceCharge(Charges[] essenceTxnCharges, PreCalculatedCharges preCalCharge,
            SwiftRemittanceRq swtRemitanceReq) {
        List<Charges> cbsChargesList = new ArrayList<>();

        // collect existing charge in a list
        for (int i = 0, n = essenceTxnCharges.length; i < n; i++) {
            Charges vCharges = essenceTxnCharges[i];
            cbsChargesList.add(vCharges);
        }

        // add pre calculated charge to existing charge list
        Charges cbsCharges = new Charges();
        PreCalculateChargeDto preChargeDto = getPreCalcuatedChargeAggregate(preCalCharge, swtRemitanceReq);
        cbsCharges.setCharge(preChargeDto.getPreChargeAlias());
        cbsChargesList.add(cbsCharges);
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getAdditionalFields()
                .setAdditionalAmount1(preChargeDto.getConvertedPreChargeAmt());

        return cbsChargesList;

    }

    /**
     * Method Description:Exclude essence charges from swtRemitanceReq
     * 
     * @param swtRemitanceReq
     * @param preCalCharge
     * @return
     */
    private static Charges excludeEssenceCharge(SwiftRemittanceRq swtRemitanceReq, PreCalculatedCharges preCalCharge) {
        Charges newChargeArry = new Charges();
        PreCalculateChargeDto preChargeDto = getPreCalcuatedChargeAggregate(preCalCharge, swtRemitanceReq);
        newChargeArry.setCharge(preChargeDto.getPreChargeAlias());
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().removeAllCharges();
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().addCharges(newChargeArry);
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getAdditionalFields()
                .setAdditionalAmount1(preChargeDto.getConvertedPreChargeAmt());
        return newChargeArry;
    }

    /**
     * Method Description:Get the Pre-Calculated Charge Agrregate
     * 
     * @param preCalCharge
     * @return
     */
    private static PreCalculateChargeDto getPreCalcuatedChargeAggregate(PreCalculatedCharges preCalCharge,
            SwiftRemittanceRq swtRemitanceReq) {
        Charge charge = new Charge();
        PreCalculateChargeDto preChargeDto = new PreCalculateChargeDto();
        String exchangeRateType = StringUtils.EMPTY;
        BigDecimal chargeExchangeRate = BigDecimal.ZERO;
        BigDecimal chargeFundingAmount = BigDecimal.ZERO;
        String contraAccountPsuedonym = StringUtils.EMPTY;

        // debit account id
        String chrgFundingAccountId = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAccountId();
        String chrgFundingAccountCcy = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAmount().getIsoCurrencyCode();
        String preCalculatedChrgCcy=preCalCharge.getAmount().getCurrency();

        String chargePostingTxnCode = SwiftRemittanceMessageHelper.readModuleConfiguration(
                PaymentSwiftConstants.MODULE_VALUE_PRE_CALCULATE_CHARGE,
                PaymentSwiftConstants.CHANNELID_SWIFT, BankFusionThreadLocal.getBankFusionEnvironment());
        IBOMisTransactionCodes misTransactionCodes = SwiftRemittanceMessageHelper.getMisTransactionCodes(chargePostingTxnCode);
        if (misTransactionCodes != null && !StringUtils.isBlank(misTransactionCodes.getF_EXCHANGERATETYPE())) {
            exchangeRateType = misTransactionCodes.getF_EXCHANGERATETYPE();
        }
        
        // getting the mis transaction code details
        IBOTransactionScreenControl txnScreenCtrl = SwiftRemittanceMessageHelper
                .getTransactionScreenControl(chargePostingTxnCode);
        if (txnScreenCtrl != null && !StringUtils.isBlank(txnScreenCtrl.getF_CONTRATRANSACTIONCODE())) {
            contraAccountPsuedonym = txnScreenCtrl.getF_CONTRAFIRSTPSEUDONYM();
        }

        // contra accountid from MIS transaction code configuration
        String chargeRecievingAccountId = findAccountUsingPseudonym(contraAccountPsuedonym, preCalculatedChrgCcy);
        PaymentSwiftUtils utils = new PaymentSwiftUtils();
        CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(exchangeRateType, preCalCharge.getAmount().getAmount(),
                preCalculatedChrgCcy, chrgFundingAccountCcy, BigDecimal.ZERO, chargeExchangeRate);
        if (calcExchgRateRs != null) {
            chargeExchangeRate = calcExchgRateRs.getCalcExchRateResults().getExchangeRateDetails().getExchangeRate();
            chargeFundingAmount = calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
            // convereted pre-calculated charge amount
            Currency convertedPreChargeAmt = new Currency();
            convertedPreChargeAmt.setAmount(chargeFundingAmount);
            convertedPreChargeAmt.setIsoCurrencyCode(chrgFundingAccountCcy);
            preChargeDto.setConvertedPreChargeAmt(convertedPreChargeAmt);
        }

        // ChargeCcyAmtDetails
        Currency chargeCcyAmtDetails = new Currency();
        chargeCcyAmtDetails.setAmount(preCalCharge.getAmount().getAmount());
        chargeCcyAmtDetails.setIsoCurrencyCode(preCalCharge.getAmount().getCurrency());
        charge.setChargeCcyAmtDetails(chargeCcyAmtDetails);

        // chgCalculationCode
        charge.setChargeCalculationCode("CALCODE");

        // chgCode
        charge.setChargeCode("CHGCODE");

        // chgDescription
        // chgNarrative
        charge.setChargeNarrative(StringUtils.EMPTY);

        // chgRecAcc
        AccountKeys chargeRecAcctDetails = new AccountKeys();
        chargeRecAcctDetails.setStandardAccountId(chargeRecievingAccountId);
        charge.setChargeRecAcctDetails(chargeRecAcctDetails);

        // chgPosTxnCode
        charge.setChargePostingTxnCode(chargePostingTxnCode);

        // chgAmountInAccCurr
        Currency chargeFundingAcctCcyDetails = new Currency();
        chargeFundingAcctCcyDetails.setAmount(chargeFundingAmount);
        chargeFundingAcctCcyDetails.setIsoCurrencyCode(chrgFundingAccountCcy);
        charge.setFundingAcctCcyDetails(chargeFundingAcctCcyDetails);

        // taxTxnCode=CDP
        charge.setTaxTxnCode(StringUtils.EMPTY);

        // TAXCODEVALUE=SCTAX
        charge.setTaxCode(StringUtils.EMPTY);

        // set tax amount and currency
        Currency taxCcyAmtDetails = new Currency();
        taxCcyAmtDetails.setAmount(BigDecimal.ZERO);
        taxCcyAmtDetails.setIsoCurrencyCode(StringUtils.EMPTY);
        charge.setTaxCcyAmtDetails(taxCcyAmtDetails);

        // TaxNarrative
        charge.setTaxNarrative(StringUtils.EMPTY);

        // TaxRecAcctDetails
        AccountKeys taxRecAcct = new AccountKeys();
        taxRecAcct.setStandardAccountId(StringUtils.EMPTY);
        charge.setTaxRecAcct(taxRecAcct);

        // ChargeExRateDetails
        ExchangeRateDetails chargeExRateDetails = new ExchangeRateDetails();
        chargeExRateDetails.setExchangeRate(chargeExchangeRate);
        chargeExRateDetails.setExchangeRateType(exchangeRateType);
        charge.setChargeExRateDetails(chargeExRateDetails);

        // taxExRateDetails
        ExchangeRateDetails taxExchangeRateDetails = new ExchangeRateDetails();
        taxExchangeRateDetails.setExchangeRate(BigDecimal.ZERO);
        taxExchangeRateDetails.setExchangeRateType(StringUtils.EMPTY);
        charge.setTaxExchangeRateDetails(taxExchangeRateDetails);

        // TaxFndAcctAmt
        Currency taxFndAcctAmtDetails = new Currency();
        taxFndAcctAmtDetails.setAmount(BigDecimal.ZERO);
        taxFndAcctAmtDetails.setIsoCurrencyCode(StringUtils.EMPTY);
        charge.setTaxFndAcctAmtDetails(taxFndAcctAmtDetails);

        // charge funding accountid
        AccountKeys fundingAccount = new AccountKeys();
        fundingAccount.setStandardAccountId(chrgFundingAccountId);
        charge.setFundingAccount(fundingAccount);

        preChargeDto.setPreChargeAlias(charge);

        return preChargeDto;
    }


    /**
     * Method Description:
     * 
     * @param pseudonymName
     * @param preCalculatedChrgCcy
     * @return
     */
    private static String findAccountUsingPseudonym(String pseudonymName, String preCalculatedChrgCcy) {
        String accountNumber = StringUtils.EMPTY;
        List<SimplePersistentObject> dbRows = SwiftRemittanceMessageHelper.findAccountByPseudonameAndContext(
                BankFusionThreadLocal.getBankFusionEnvironment(),
                "CURRENCY", preCalculatedChrgCcy, preCalculatedChrgCcy, pseudonymName);
        if (dbRows != null && !dbRows.isEmpty()) {
            accountNumber = dbRows.get(0).getDataMap().get("f_ACCOUNTID").toString();
        }
        else {
            CommonUtil.handleParameterizedEvent(SwiftEventCodes.E_ACCT_FOR_PSEUDONYM_WITH_ISOCURRCODE_NOT_FOUND,
                    new String[] { pseudonymName, preCalculatedChrgCcy, "CURRENCY", preCalculatedChrgCcy });
        }

        return accountNumber;
    }
}
