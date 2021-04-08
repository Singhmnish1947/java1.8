package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.common.runtime.toolkit.expression.function.RoundToScale;
import com.misys.ub.payment.swift.utils.ChargeNonStpProcess;
import com.misys.ub.payment.swift.utils.ChargesDto;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.GetExchangeRateSwiftRemittance;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GetExchangeRateDetails;

import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.Currency;

public class GetExchangeRateDetails extends AbstractUB_SWT_GetExchangeRateDetails {

    public GetExchangeRateDetails(BankFusionEnvironment env) {
        super(env);
    }

    public GetExchangeRateDetails() {
    }

    private transient final static Log LOGGER = LogFactory.getLog(GetExchangeRateDetails.class.getName());

    public void process(BankFusionEnvironment env) throws BankFusionException {
        BigDecimal exchangeRate = BigDecimal.ZERO;
        BigDecimal convertedAmt = BigDecimal.ZERO;
        String exchangeRateType = !StringUtils.isBlank(getF_IN_ExchangeRateType()) ? getF_IN_ExchangeRateType() : StringUtils.EMPTY;
        String msgRefId = !StringUtils.isBlank(getF_IN_msgRefId()) ? getF_IN_msgRefId() : StringUtils.EMPTY;
        GetExchangeRateSwiftRemittance generateExchRateRemittance = new GetExchangeRateSwiftRemittance();
        PaymentSwiftUtils utils = new PaymentSwiftUtils();
        String direction = getF_IN_direction();
        String fromCcyCode = getF_IN_FromCurrency();
        String toCcyCode = getF_IN_ToCurrency();
        BigDecimal inputAmount = getF_IN_Amount();
        if (direction.equals(PaymentSwiftConstants.INWARD)) {
            if (isF_IN_FromExchTab()) {
                checkTolerance(env);
                setF_OUT_ExchangeRate(exchangeRate);
                setF_OUT_ExchangeRateType(exchangeRateType);
            }
            else {
                Map<String, Object> outMap = generateExchRateRemittance.getExchangeRate(fromCcyCode, toCcyCode, inputAmount, "N");
                exchangeRate = (BigDecimal) outMap.get("exchangeRate");
                logInfo("Inward exchangeRate:: " + exchangeRate);
                convertedAmt = (BigDecimal) outMap.get("convertedAmount");
                logInfo("Inward convertedAmt:: " + convertedAmt);
                exchangeRateType = (String) outMap.get("exchangeRateType");
                logInfo("Inward exchangeRateType:: " + exchangeRateType);

                setF_OUT_ConvertedAmount(convertedAmt);
                setF_OUT_ExchangeRate(exchangeRate);
                setF_OUT_ExchangeRateType(exchangeRateType);
                setF_OUT_Success(true);
            }

        }
        else {
            if (exchangeRateType.equals(CommonConstants.EMPTY_STRING) && (msgRefId != null && !msgRefId.isEmpty())) {
                exchangeRateType = utils.getExchangeRateType(msgRefId);
            }
            CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(exchangeRateType, inputAmount, fromCcyCode, toCcyCode,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            if (calcExchgRateRs != null) {
                if (isF_IN_FromExchTab()) {
                    exchangeRate = getF_IN_ExchangeRate() != null ? getF_IN_ExchangeRate() : BigDecimal.ONE;
                }
                else {
                    exchangeRate = calcExchgRateRs.getCalcExchRateResults().getExchangeRateDetails().getExchangeRate();
                }
                logInfo("Outward exchangeRate:: " + exchangeRate);
                exchangeRateType = calcExchgRateRs.getCalcExchRateResults().getExchangeRateDetails().getExchangeRateType();
                logInfo("Outward exchangeRateType:: " + exchangeRateType);
                convertedAmt = getAmtBasedOnChargeOption(fromCcyCode, toCcyCode, exchangeRate);
                logInfo("Outward convertedAmt:: " + convertedAmt);
            }

            setF_OUT_ConvertedAmount(convertedAmt);
            setF_OUT_ExchangeRate(exchangeRate);
            setF_OUT_ExchangeRateType(exchangeRateType);
            setF_OUT_Success(true);
        }
        

    }

    public void checkTolerance(BankFusionEnvironment env) {
        BigDecimal amount, exchangeRate, origExchangeRate;
        String exchangeRateType, currency1, currency2;
        Boolean result = Boolean.TRUE;
        BigDecimal convertedAmount = BigDecimal.ZERO;
        amount = getF_IN_Amount();
        exchangeRate = getF_IN_ExchangeRate();
        origExchangeRate = getF_IN_OrigExchangeRate();
        currency1 = getF_IN_FromCurrency();
        exchangeRateType = getF_IN_ExchangeRateType();
        currency2 = getF_IN_ToCurrency();

        // cross currency
        if (!(currency1.equals(currency2)) && (!(currency2.equals(CommonConstants.EMPTY_STRING)))) {
            result = isExchangeRateWithinCurrencyTolerance(origExchangeRate, exchangeRate, exchangeRateType, currency1, currency2,
                    amount, env);
            convertedAmount = exchangeRate.multiply(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        else {
            convertedAmount = RoundToScale.run(exchangeRate.multiply(amount), currency2);
        }
        setF_OUT_ConvertedAmount(convertedAmount);
        setF_OUT_Success(result);
    }

    public boolean isExchangeRateWithinCurrencyTolerance(BigDecimal origExchangeRate, BigDecimal exchangeRate,
            String exchangeRateType, String fromCurrency, String toCurrency, BigDecimal amount, BankFusionEnvironment env) {
        if (amount == null) {
            amount = CommonConstants.BIGDECIMAL_ZERO;
        }
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        BigDecimal ubExchangeRate = origExchangeRate;

        double currencyTolerancePercentage = bizInfo.getCurrencyTolerancePercentage(fromCurrency, env);
        double currencyTolerance = currencyTolerancePercentage / 100.0D;
        BigDecimal tolerance = ubExchangeRate.multiply(BigDecimal.valueOf(currencyTolerance)).setScale(8, 0);
        BigDecimal lowTolerance = ubExchangeRate.subtract(tolerance);
        BigDecimal highTolerance = ubExchangeRate.add(tolerance);

        return ((exchangeRate.compareTo(lowTolerance) >= 0) && (exchangeRate.compareTo(highTolerance) <= 0));
    }

    private void logInfo(String param) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(param);
        }
    }

    /**
     * @param fromCcyCode
     * @param toCcyCode
     * @param exchangeRate
     * @return
     */
    private BigDecimal getAmtBasedOnChargeOption(String fromCcyCode, String toCcyCode, BigDecimal exchangeRate) {
        ChargeNonStpProcess nonStpCharge = new ChargeNonStpProcess();
        BigDecimal convertedAmount = BigDecimal.ZERO;
        ChargesDto chargesDto = new ChargesDto();
        // initailRemittanceAmount from teller
        Currency debitAmount = new Currency();
        Currency ubCharge = new Currency();
        Currency payingBankChg = new Currency();
        Currency creditAmount = new Currency();
        Currency instructedAmt = new Currency();

        BigDecimal instructedAmount = getF_IN_InstructedAmount();
        String instrucetAmtccy = fromCcyCode;

        instructedAmt.setAmount(instructedAmount);
        instructedAmt.setIsoCurrencyCode(instrucetAmtccy);
        chargesDto.setInstructedAmount(instructedAmt);

        debitAmount.setAmount(instructedAmount);
        debitAmount.setIsoCurrencyCode(instrucetAmtccy);
        chargesDto.setDebitAmount(debitAmount);

        ubCharge.setAmount(getF_IN_ubCharge());
        ubCharge.setIsoCurrencyCode(getF_IN_ubChargeCcy());
        chargesDto.setUbCharges(ubCharge);

        payingBankChg.setAmount(getF_IN_payingBankChargeAmt());
        payingBankChg.setIsoCurrencyCode(toCcyCode);
        chargesDto.setPayingBankChg(payingBankChg);

        creditAmount.setAmount(instructedAmount);
        creditAmount.setIsoCurrencyCode(toCcyCode);
        chargesDto.setCreditAmount(creditAmount);

        chargesDto.setExchangeRate(exchangeRate);
        chargesDto.setExchangeRateType(getF_IN_ExchangeRateType());
        chargesDto.setDebitExchangeRateType(getF_IN_ExchangeRateType());
        chargesDto.setChargeType(getF_IN_chargeTypeCode());

        chargesDto = nonStpCharge.getAmountBasedOnChargeOption(chargesDto);
        convertedAmount = RoundToScale.run(chargesDto.getCreditAmount().getAmount(),
                chargesDto.getCreditAmount().getIsoCurrencyCode());
        return convertedAmount;
    }

}
