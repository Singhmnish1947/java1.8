package com.misys.ub.payment.swift.utils;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.Currency;

/**
 * @author machamma.devaiah
 *
 */
public class ChargeNonStpProcess {
	private transient final static Log LOGGER = LogFactory.getLog(ChargeNonStpProcess.class.getName());
	private PaymentSwiftUtils utils = new PaymentSwiftUtils();

	/**
	 * @param chargeType
	 * @param debitAmount
	 * @param txnChargesInFundingAcctCcy
	 * @param payingBankCharge
	 * @param creditAmount
	 * @return
	 */
	public ChargesDto getAmountBasedOnChargeOption(ChargesDto chargesDto) {
		String chargeType = chargesDto.getChargeType();
		BigDecimal creditAmount=BigDecimal.ZERO;
        BigDecimal instructedAmount = BigDecimal.ZERO;
        String instructedAmountCcy = StringUtils.EMPTY;
        if (null != chargesDto.getInstructedAmount()
                && chargesDto.getInstructedAmount().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            instructedAmount = chargesDto.getInstructedAmount().getAmount();
            instructedAmountCcy = chargesDto.getInstructedAmount().getIsoCurrencyCode();
        }
        
		//debit amount
		Currency debitAmount = chargesDto.getDebitAmount();
		//default : ub charge in debit account currency
		Currency txnChargesInFundingAcctCcy = chargesDto.getUbCharges();
		//default : charge detail in credit Currency 
		Currency payingBankCharge = chargesDto.getPayingBankChg();
		String creditAmountccy = chargesDto.getCreditAmount().getIsoCurrencyCode();
		String creditExchangeRateType = !StringUtils.isBlank(chargesDto.getExchangeRateType()) ? chargesDto.getExchangeRateType() : "SPOT";
	    String debitExchangeRateType = !StringUtils.isBlank(chargesDto.getDebitExchangeRateType()) ? chargesDto.getDebitExchangeRateType() : "SPOT";
		BigDecimal exchageRate = chargesDto.getExchangeRate() != null ? chargesDto.getExchangeRate() : BigDecimal.ONE;
		//credit amount
		if(chargesDto.getChannelId().equals(PaymentSwiftConstants.CHANNELID_IBI)||chargesDto.getChannelId().equals(PaymentSwiftConstants.CHANNELID_CCI )){
			creditAmount=chargesDto.getCreditAmount().getAmount();
		}else{
            if (instructedAmountCcy.equals(debitAmount.getIsoCurrencyCode())) {
                creditAmount = getCreditAmount(chargesDto.getDebitAmount().getAmount(),
                        chargesDto.getDebitAmount().getIsoCurrencyCode(), creditAmountccy, creditExchangeRateType, exchageRate);
            }
            else {
                creditAmount = getCreditAmount(instructedAmount, instructedAmountCcy, creditAmountccy, creditExchangeRateType,
                        exchageRate);
            }
		}
		//charge detail in debit account currency
		BigDecimal payingBankChargeInDebitCurrency=getPayingBankChargeInDebitCurrency(payingBankCharge, debitAmount,debitExchangeRateType).getAmount();
		//ub charges in credit account currency
		BigDecimal ubChargeInCreditAcctCcy=getUBChargeInCreditAccCcy(txnChargesInFundingAcctCcy.getAmount(),txnChargesInFundingAcctCcy.getIsoCurrencyCode(),creditAmountccy, creditExchangeRateType);
		BigDecimal drAmount = BigDecimal.ZERO;
		BigDecimal crAmount = BigDecimal.ZERO;
		if(CommonConstants.EMPTY_STRING.equalsIgnoreCase(chargeType) || chargeType.isEmpty()){
			chargeType = PaymentSwiftConstants.CHARGE_CODE_OUR;
		}
		switch (chargeType) {
		case PaymentSwiftConstants.CHARGE_CODE_OUR:
		    //debit amount=instrucetd amount in debit ccy+ ub charges in debit currency+ charge detail in debit currency
		    //credit amount=instrucetd amount in credit ccy+ charge detail in creditAmountccy
			if (payingBankCharge.getAmount().compareTo(BigDecimal.ZERO) > 0) {
				drAmount = debitAmount.getAmount().add(txnChargesInFundingAcctCcy.getAmount()).add(payingBankChargeInDebitCurrency);
				crAmount= creditAmount.add(payingBankCharge.getAmount());
			}
			else {
				drAmount = debitAmount.getAmount().add(txnChargesInFundingAcctCcy.getAmount());
				crAmount = creditAmount.add(payingBankCharge.getAmount());
			}
			chargesDto.getPayingBankChg().setIsoCurrencyCode(creditAmountccy);
			break;
		case PaymentSwiftConstants.CHARGE_CODE_SHA:
	        //debit amount=instrucetd amount in debit ccy+ ub charges in debit currency
		    //credit amount=instrucetd amount in credit ccy
			if (chargeType.equals(PaymentSwiftConstants.CHARGE_CODE_SHA)) {
				drAmount = debitAmount.getAmount().add(txnChargesInFundingAcctCcy.getAmount());
				crAmount = creditAmount;
				//persisting for display purpose
				payingBankCharge.setAmount(chargesDto.getUbCharges().getAmount() != null ? chargesDto.getUbCharges().getAmount() : BigDecimal.ZERO);
				if(txnChargesInFundingAcctCcy.getAmount().compareTo(BigDecimal.ZERO) >0) {
				    chargesDto.getPayingBankChg().setIsoCurrencyCode(debitAmount.getIsoCurrencyCode());
				}else {
				    chargesDto.getPayingBankChg().setIsoCurrencyCode(creditAmountccy);
				}
			}
			break;
		case PaymentSwiftConstants.CHARGE_CODE_BEN:
	        //debit amount=instrucetd amount in debit ccy
            //credit amount=instrucetd amount in credit ccy - ub charge in credit currency
			if (chargeType.equals(PaymentSwiftConstants.CHARGE_CODE_BEN)) {
				drAmount = debitAmount.getAmount();
                crAmount = creditAmount.subtract(ubChargeInCreditAcctCcy != null ? ubChargeInCreditAcctCcy : BigDecimal.ZERO);
				//persisting for display purpose
				payingBankCharge.setAmount(chargesDto.getUbCharges().getAmount() != null ? chargesDto.getUbCharges().getAmount() : BigDecimal.ZERO);
				if(txnChargesInFundingAcctCcy.getAmount().compareTo(BigDecimal.ZERO) >0) {
                    chargesDto.getPayingBankChg().setIsoCurrencyCode(debitAmount.getIsoCurrencyCode());
                }else {
                    chargesDto.getPayingBankChg().setIsoCurrencyCode(creditAmountccy);
                }
			}
			break;
		default:
			LOGGER.info("::::NO charge option passed::::");
		}
		//creditAmount
		chargesDto.getCreditAmount().setAmount(crAmount);
		chargesDto.getCreditAmount().setIsoCurrencyCode(creditAmountccy);
		//debitAmount
		chargesDto.getDebitAmount().setAmount(drAmount);
		chargesDto.getDebitAmount().setIsoCurrencyCode(debitAmount.getIsoCurrencyCode());
		chargesDto.getPayingBankChg().setAmount(payingBankCharge.getAmount());
		return chargesDto;
	}

	/**
	 * @param payingBankCharge
	 * @param debitAmount
	 * @param exchangeRate
	 * @param exchangeRateType
	 * @return
	 */
	private Currency getPayingBankChargeInDebitCurrency(Currency payingBankCharge, Currency debitAmount, String exchangeRateType) {
		Currency payingBankChargeInDebitCurrency = new Currency();
		if (!debitAmount.getIsoCurrencyCode().equals(payingBankCharge.getIsoCurrencyCode())) {
			//convert payingbankCharge to debit ccy
			CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(exchangeRateType, payingBankCharge.getAmount(), payingBankCharge.getIsoCurrencyCode(), debitAmount.getIsoCurrencyCode(), BigDecimal.ZERO, BigDecimal.ZERO);
			BigDecimal payingBankChargeAmtInDebitCcy = calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
			payingBankChargeInDebitCurrency.setAmount(payingBankChargeAmtInDebitCcy);
			payingBankChargeInDebitCurrency.setIsoCurrencyCode(debitAmount.getIsoCurrencyCode());
		}
		else {
			payingBankChargeInDebitCurrency.setAmount(payingBankCharge.getAmount());
			payingBankChargeInDebitCurrency.setIsoCurrencyCode(payingBankCharge.getIsoCurrencyCode());
		}
		return payingBankChargeInDebitCurrency;
	}

	/**
	 * @param debitAmount
	 * @param creditAmountCcy
	 * @param exchangeRateType
	 * @param exchangeRate
	 * @return
	 */
	private BigDecimal getCreditAmount(BigDecimal debitAmount,String debitAmountCcy, String creditAmountCcy, String exchangeRateType, BigDecimal exchangeRate) {
		BigDecimal creditAmount = debitAmount;
		if (!debitAmountCcy.equals(creditAmountCcy)) {
			//convert
			CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(exchangeRateType, debitAmount, debitAmountCcy, creditAmountCcy, BigDecimal.ZERO, exchangeRate);
			BigDecimal debitAmtInCreditCcy = calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
			creditAmount = debitAmtInCreditCcy;
		}
		return creditAmount;
	}
	
	
	   /**
	 * @param ubChargeAmt
	 * @param ubChargeAmtCcy
	 * @param creditAmountCcy
	 * @param exchangeRateType
	 * @param exchangeRate
	 * @return
	 */
	private BigDecimal getUBChargeInCreditAccCcy(BigDecimal ubChargeAmt,String ubChargeAmtCcy, String creditAmountCcy, String exchangeRateType) {
	       BigDecimal ubChargeInCreditCcy=ubChargeAmt;
	        if (!ubChargeAmtCcy.equals(creditAmountCcy)) {
	            //convert
	            CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(exchangeRateType, ubChargeAmt, ubChargeAmtCcy, creditAmountCcy, BigDecimal.ZERO, BigDecimal.ZERO);
	            ubChargeInCreditCcy = calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
	        }
	        return ubChargeInCreditCcy;
	    }
}
