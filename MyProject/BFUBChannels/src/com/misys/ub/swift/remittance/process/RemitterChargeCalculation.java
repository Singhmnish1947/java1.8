package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.openapi.creditTransfer.v1.model.PreCalculatedCharges;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.payment.swift.utils.ChargeNonStpProcess;
import com.misys.ub.payment.swift.utils.ChargesDto;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.validation.ValidationHelper;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.Charges;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.DebitPostingDtls;

public class RemitterChargeCalculation implements Command {
    private static final transient Log LOGGER = LogFactory.getLog(RemitterChargeCalculation.class.getName());
    private PaymentSwiftUtils utils = new PaymentSwiftUtils();

    @SuppressWarnings("unchecked")
    @Override
    public boolean execute(Context context) throws Exception {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN RemitterChargeCalculation ::::" +      BankFusionThreadLocal.getUserSession().getUserId());
        boolean endofChain = Boolean.FALSE;
        OutRemScrnRqToAPIRqConvertor apiConverter = new OutRemScrnRqToAPIRqConvertor();
        // getting the request, response and otherRequiredProcessDtl object from the context
        SwiftRemittanceRq swtRemitanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");
        String debitAccountCcy = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAmount().getIsoCurrencyCode();
        ValidationHelper helper = new ValidationHelper();
        RsHeader rsHeader = new RsHeader();
   
        BigDecimal essenceConfiguredChargeAmt = BigDecimal.ZERO;
        Charges[] txnCharges = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getCharges();
        if (null == txnCharges || txnCharges.length == 0 || (null != txnCharges[0] && null != txnCharges[0].getCharge()
                && StringUtils.isEmpty(txnCharges[0].getCharge().getChargeCode()))) {
            String debitAcctId = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                    .getDebitAccountId();
            String creditAcctCcy = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                    .getCreditAmount().getIsoCurrencyCode();
            BigDecimal debitAmount = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                    .getDebitAmount().getAmount();
            String debitTxnCode = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode();

            try {
                HashMap onlineCharges = SwiftRemittanceMessageHelper.fetchOnlinecharges(debitAcctId, creditAcctCcy, debitTxnCode,
                        debitAmount, remittanceDto.getEnv());

                if (onlineCharges != null) {
                    // charge amount
                    essenceConfiguredChargeAmt = (BigDecimal) onlineCharges.get("CONSOLIDATEDCHARGEAMT");
                    BigDecimal consolidateTaxAmount = (BigDecimal) onlineCharges.get("CONSOLIDATEDTAXAMT");
                    essenceConfiguredChargeAmt = essenceConfiguredChargeAmt.add(consolidateTaxAmount);
                    VectorTable chargeVector = (VectorTable) onlineCharges.get("RESULT");
                    if (chargeVector.size() != 0) {
                        swtRemitanceReq = apiConverter.convertChargeVectorToComplexType(chargeVector, swtRemitanceReq);
                    }
                }

            }
            catch (BankFusionException e) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("IN RemitterChargeCalculation" + e.getLocalisedMessage());
                endofChain = Boolean.TRUE;
                MessageStatus status = new MessageStatus();
                SubCode subCode = new SubCode();
                String eventCode ;
                if (null != e.getEvents() && String.valueOf(e.getEvents().iterator().next().getEventNumber()).isEmpty()) {
                    eventCode = SwiftEventCodes.E_AN_ERROR_EXECUTING_THE_PROCESS_EXECUTION_FAILED;
                }
                else {
                    eventCode = String.valueOf(e.getEvents().iterator().next().getEventNumber());
                }
                subCode.setCode(eventCode);
                Object parameterList ;
                for (int j = 0; j < e.getEvents().iterator().next().getDetails().length; j++) {
                    EventParameters parameter = new EventParameters();
                    parameterList = e.getEvents().iterator().next().getDetails()[j];
                    parameter.setEventParameterValue(parameterList.toString());
                    subCode.addParameters(parameter);
                }
                subCode.setDescription("RemitterChargeCalculation");
                subCode.setFieldName(StringUtils.EMPTY);
                subCode.setSeverity(PaymentSwiftConstants.ERROR_STATUS);
                status.addCodes(subCode);
                status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
                rsHeader.setStatus(status);
                swtRemitterResp.setRsHeader(rsHeader);
            }

        }
        else {
            // consolidated charge amount in array
            essenceConfiguredChargeAmt = utils.getUBChargesFromChargeArray(swtRemitanceReq).getUbCharges().getAmount();
        }

        // open api changes
        if (remittanceDto.getPreCalculateCharge() != null) {
            BigDecimal preCalculatedChargeAmt = BigDecimal.ZERO;
            swtRemitanceReq = PreCalculatedChargeCalculation.addPreCalculateChargeToSwiftRq(swtRemitanceReq,
                    remittanceDto.getPreCalculateCharge());

            PreCalculatedCharges preCalCharge = remittanceDto.getPreCalculateCharge();
            if (debitAccountCcy.equals(preCalCharge.getAmount().getCurrency())) {
                preCalculatedChargeAmt = preCalCharge.getAmount().getAmount();
            }
            else {
                // get converted pre-calculated charge
                preCalculatedChargeAmt = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getAdditionalFields()
                        .getAdditionalAmount1().getAmount();
            }

            // If suppressAdditionalCharges is true then Essence charges should not be included
            if (preCalCharge != null && preCalCharge.isSuppressAdditionalCharges()) {
                essenceConfiguredChargeAmt = preCalculatedChargeAmt;
            }
            else {
                // If suppressAdditionalCharges is false then Essence charges to be included
                // alongwith the pre-calculated chages
                essenceConfiguredChargeAmt = essenceConfiguredChargeAmt.add(preCalculatedChargeAmt);
            }
        }

        remittanceDto.setUbChargeAmt(essenceConfiguredChargeAmt);
        
        String chargeCode = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode();
        if(!StringUtils.isBlank(chargeCode) && PaymentSwiftConstants.CHARGE_CODE_BEN.equalsIgnoreCase(chargeCode) && 
                CommonConstants.BIGDECIMAL_ZERO.compareTo(essenceConfiguredChargeAmt) >= 0)
        {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_ES_CHARGE_MANDATORY_FOR_BEN, CommonConstants.EMPTY_STRING);
            swtRemitterResp.setRsHeader(rsHeader);
            return true;
        }

        // is the essenceCharges greater than transaction amount
        if (essenceConfiguredChargeAmt.compareTo(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAmount()
                        .getAmount()) >= 1) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CHARGE_AMT_LESS_THAN_TRANSACTION_AMOUNT_UB, CommonConstants.EMPTY_STRING);
            swtRemitterResp.setRsHeader(rsHeader);
            return true;
        }

        // from openapi
        if (remittanceDto.isFromApi()) {
            computeOtherAmount(swtRemitanceReq, essenceConfiguredChargeAmt);
            // check if the equivalent amount is greater than the calculated credit amount based on
            // the charge option
            if (remittanceDto.getEquivalentAmount().compareTo(BigDecimal.ZERO) > 0
                    && remittanceDto.getEquivalentAmount().compareTo(swtRemitanceReq.getInitiateSwiftMessageRqDtls()
                            .getTransactionDetails().getCreditPostingDtls().getCreditAmount().getAmount()) != 0) {
                rsHeader = helper.setErrorResponse(SwiftEventCodes.E_EQUIVALENT_AMT_DOES_NOT_MATCH_CALCULATED_VALUE,
                        CommonConstants.EMPTY_STRING);
                swtRemitterResp.setRsHeader(rsHeader);
                return true;
            }
        }
        else if (swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount()
                .getAmount().compareTo(BigDecimal.ZERO) == 0) {
            computeOtherAmount(swtRemitanceReq, essenceConfiguredChargeAmt);
        }

        context.put("swtRemitanceReq", swtRemitanceReq);
        context.put("swtRemitterResp", swtRemitterResp);
        context.put("remittanceDto", remittanceDto);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("END RemitterChargeCalculation");
        return endofChain;
    }

    /**
     * Method Description:Compute the credit amount by applying the formulae for the charge option
     * SHA/BEN/OUR
     * 
     * @param swtRemitanceReq
     * @param essenceConfiguredChargeAmt
     */
    private void computeOtherAmount(SwiftRemittanceRq swtRemitanceReq, BigDecimal essenceConfiguredChargeAmt) {
        ChargesDto chargeDto = prepareChargeData(swtRemitanceReq, essenceConfiguredChargeAmt);
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAmount()
                .setAmount(chargeDto.getDebitAmount().getAmount());
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount()
                .setAmount(chargeDto.getCreditAmount().getAmount());
        BigDecimal payingBankChgrAmt = chargeDto.getPayingBankChg().getAmount();
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().setAmount(payingBankChgrAmt);
        swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails()
                .setIsoCurrencyCode(utils.getChargeDetailCcy(chargeDto));
    }
    

    /**
     * Method Description:Preparing the charge Data
     * @param swtRemitanceReq
     * @param ubCharges
     * @return
     */
    private ChargesDto prepareChargeData(SwiftRemittanceRq swtRemitanceReq, BigDecimal ubCharges) {
        ChargeNonStpProcess nonStpCharge = new ChargeNonStpProcess();
        ChargesDto chargesDto = new ChargesDto();

        // instructed amount
        Currency instructedAmt = new Currency();
        instructedAmt.setAmount(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getAmount());
        instructedAmt.setIsoCurrencyCode(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getIsoCurrencyCode());
        chargesDto.setInstructedAmount(instructedAmt);
        
        // debit amount
        Currency debitAmount = new Currency();
        DebitPostingDtls dbPostDtls = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls();
        // if instructed amt currency same as debit account currency
        if (instructedAmt.getIsoCurrencyCode().equals(dbPostDtls.getDebitAmount().getIsoCurrencyCode())) {
            debitAmount.setAmount(instructedAmt.getAmount());
            debitAmount.setIsoCurrencyCode(instructedAmt.getIsoCurrencyCode());
        }
        else {
            CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(dbPostDtls.getDebitExchangeRateType(),
                    instructedAmt.getAmount(), instructedAmt.getIsoCurrencyCode(), dbPostDtls.getDebitAmount().getIsoCurrencyCode(),
                    BigDecimal.ZERO, dbPostDtls.getDebitExchangeRate());
            if (calcExchgRateRs != null) {
                debitAmount.setAmount(calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount());
                debitAmount.setIsoCurrencyCode(calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getIsoCurrencyCode());
            }
        }
        chargesDto.setDebitAmount(debitAmount);

        // credit amount
        Currency creditAmount = new Currency();
        creditAmount.setAmount(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditAmount().getAmount().compareTo(BigDecimal.ZERO) > 0
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                                .getCreditAmount().getAmount()
                        : BigDecimal.ZERO);
        creditAmount.setIsoCurrencyCode(!StringUtils.isBlank(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode())
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                                .getCreditAmount().getIsoCurrencyCode()
                        : StringUtils.EMPTY);
        chargesDto.setCreditAmount(creditAmount);

        // exchnage rate
        chargesDto.setExchangeRate(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditExchangeRate().compareTo(BigDecimal.ZERO) > 0
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                                .getCreditExchangeRate()
                        : BigDecimal.ZERO);
        chargesDto.setExchangeRateType(!StringUtils.isBlank(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditExchangeRateType())
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                                .getCreditExchangeRateType()
                        : StringUtils.EMPTY);
        chargesDto.setDebitExchangeRateType(!StringUtils.isBlank(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getDebitPostingDtls().getDebitExchangeRateType())
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRateType()
                        : StringUtils.EMPTY);
        // channelId
        chargesDto.setChannelId(PaymentSwiftConstants.CHANNEL_UXP);

        // paying bank charge
        Currency payingBankChg = new Currency();
        payingBankChg.setAmount(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails()
                .getAmount().compareTo(BigDecimal.ZERO) > 0
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getAmount()
                        : BigDecimal.ZERO);
        payingBankChg.setIsoCurrencyCode(!StringUtils.isBlank(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails().getIsoCurrencyCode())
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeDetails()
                                .getIsoCurrencyCode()
                        : StringUtils.EMPTY);
        chargesDto.setPayingBankChg(payingBankChg);

        // SHA BEN OUR
        chargesDto.setChargeType(
                !StringUtils.isBlank(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode())
                        ? swtRemitanceReq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().getChargeCode()
                        : StringUtils.EMPTY);
        // ChargeFunding Account Id
        chargesDto.setChargeFundingAccountId(
                swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId());

        // ubCharges
        Currency ubCharge = new Currency();
        ubCharge.setAmount(ubCharges.compareTo(BigDecimal.ZERO) > 0 ? ubCharges : BigDecimal.ZERO);
        ubCharge.setIsoCurrencyCode(debitAmount.getIsoCurrencyCode());
        chargesDto.setUbCharges(ubCharge);
        return nonStpCharge.getAmountBasedOnChargeOption(chargesDto);
    }

}
