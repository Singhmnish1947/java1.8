package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;

import com.misys.bankfusion.common.runtime.toolkit.expression.function.RoundToScale;
import com.misys.ub.payment.swift.utils.ChargeNonStpProcess;
import com.misys.ub.payment.swift.utils.ChargesDto;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GetAmountBasedOnChargeOption;

import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.Currency;

public class UB_SWT_GetAmountBasedOnChargeOption extends AbstractUB_SWT_GetAmountBasedOnChargeOption {

    public UB_SWT_GetAmountBasedOnChargeOption() {
        super();
    }

    /**
     * @param env
     */
    @SuppressWarnings("deprecation")
    public UB_SWT_GetAmountBasedOnChargeOption(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        ChargeNonStpProcess nonStpCharge = new ChargeNonStpProcess();
        PaymentSwiftUtils utils = new PaymentSwiftUtils();
        ChargesDto chargesDto = new ChargesDto();
        // initailRemittanceAmount from teller
        Currency debitAmount = new Currency();
        Currency ubCharge = new Currency();
        Currency payingBankChg = new Currency();
        Currency creditAmount = new Currency();
        Currency instructedAmt = new Currency();

        // instructed amount
        BigDecimal instructedAmount = getF_IN_RemittanceRq().getInstructedAmount();
        String instrucetAmtccy = getF_IN_RemittanceRq().getInstructedAmountCcy();
        instructedAmt.setAmount(instructedAmount);
        instructedAmt.setIsoCurrencyCode(instrucetAmtccy);
        chargesDto.setInstructedAmount(instructedAmt);

        // if instructed amt currency same as debit account currency
        if (instrucetAmtccy.equals(getF_IN_RemittanceRq().getDrAccountCurrency())) {
            debitAmount.setAmount(instructedAmount);
            debitAmount.setIsoCurrencyCode(instrucetAmtccy);
        }
        else {
            CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(getF_IN_RemittanceRq().getExchangeRateTypeOUT(),
                    instructedAmount, instrucetAmtccy, getF_IN_RemittanceRq().getDrAccountCurrency(), BigDecimal.ZERO,
                    getF_IN_RemittanceRq().getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING());
            if (calcExchgRateRs != null) {
                debitAmount.setAmount(calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount());
                debitAmount
                        .setIsoCurrencyCode(calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getIsoCurrencyCode());
            }
        }
        chargesDto.setDebitAmount(debitAmount);
        //debitExchangeRateType
        chargesDto.setDebitExchangeRateType(getF_IN_RemittanceRq().getExchangeRateTypeOUT());

        ubCharge.setAmount(getF_IN_RemittanceRq().getTRANSACTIONDETAISINFO().getAPPLIEDCHARGES());
        ubCharge.setIsoCurrencyCode(debitAmount.getIsoCurrencyCode());
        chargesDto.setUbCharges(ubCharge);

        creditAmount.setAmount(instructedAmount);
        creditAmount.setIsoCurrencyCode(getF_IN_RemittanceRq().getExpctCrAmountCurrency());
        chargesDto.setCreditAmount(creditAmount);

        chargesDto.setExchangeRate(getF_IN_RemittanceRq().getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING());
        //credit exchangeRateType
        chargesDto.setExchangeRateType(getF_IN_RemittanceRq().getExchangeRateTypeIN());
        chargesDto.setChargeType(getF_IN_RemittanceRq().getRemittanceINFO().getCHARGECODE());

        payingBankChg.setAmount(getF_IN_RemittanceRq().getRemittanceINFO().getChargeDetailAmount());
        payingBankChg.setIsoCurrencyCode(utils.getChargeDetailCcy(chargesDto));
        chargesDto.setPayingBankChg(payingBankChg);

        chargesDto = nonStpCharge.getAmountBasedOnChargeOption(chargesDto);
        getF_OUT_RemittanceRs().getDEBITORDTL().setEXPECTEDDEBITAMOUNT(
                RoundToScale.run(chargesDto.getDebitAmount().getAmount(), chargesDto.getDebitAmount().getIsoCurrencyCode()));
        getF_OUT_RemittanceRs().getCREDITORDTL().setEXPECTEDCREDITAMOUNT(
                RoundToScale.run(chargesDto.getCreditAmount().getAmount(), chargesDto.getCreditAmount().getIsoCurrencyCode()));
        getF_OUT_RemittanceRs().getRemittanceINFO().setChargeDetailAmount(
                RoundToScale.run(chargesDto.getPayingBankChg().getAmount(), chargesDto.getPayingBankChg().getIsoCurrencyCode()));
        // here the paying bank charge currency will be set to the field ChargeCurrency
        getF_OUT_RemittanceRs().setChargeCurrency(chargesDto.getPayingBankChg().getIsoCurrencyCode());
    }

}
