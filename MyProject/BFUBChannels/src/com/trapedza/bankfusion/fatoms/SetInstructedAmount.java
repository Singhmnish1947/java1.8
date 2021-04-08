package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_SetInstructedAmount;
/**
 * 
 * @author shmathu1
 *
 */
public class SetInstructedAmount extends AbstractUB_SWT_SetInstructedAmount {
    private static final transient Log LOGGER = LogFactory.getLog(SetInstructedAmount.class.getName());

    /**
     * @param env
     */
    public SetInstructedAmount(BankFusionEnvironment env) {
        super(env);
    }
    

    /*
     * 
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_SetInstructedAmount#process(com.trapedza
     * .bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        BigDecimal fundingAmount = null;
        BigDecimal chargeDetails = (null != getF_IN_remittanceProcessRq().getRemittanceINFO().getChargeDetailAmount())
                ? getF_IN_remittanceProcessRq().getRemittanceINFO().getChargeDetailAmount()
                : BigDecimal.ZERO;
        boolean suppressInstructedAmt = getF_IN_remittanceProcessRq().getTRANSACTIONDETAISINFO().getShowAsInstructed();
        String chargeCodeType = getF_IN_remittanceProcessRq().getRemittanceINFO().getCHARGECODE();
        BigDecimal instructedAmt = getF_IN_remittanceProcessRq().getInstructedAmount();

        // for SHA
        if (chargeCodeType.equals(PaymentSwiftConstants.CHARGE_CODE_SHA)) {

            IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            String field71f = (String) bizInfo.getModuleConfigurationValue(PaymentSwiftConstants.MODULE_ID,
                    PaymentSwiftConstants.SHOW_SENDER_CHARGE_SHA, BankFusionThreadLocal.getBankFusionEnvironment());

            if (field71f.equalsIgnoreCase(PaymentSwiftConstants.NO)) {
                fundingAmount = fundingAmtforSHA_N_Case(suppressInstructedAmt, instructedAmt);
            }
            else {
                fundingAmount = fundingAmtforSHA_Y_Case(suppressInstructedAmt, chargeDetails, instructedAmt);
            }
        }
        else if (suppressInstructedAmt && chargeDetails.compareTo(BigDecimal.ZERO) == 0) {
            fundingAmount = BigDecimal.ZERO;
        }
        else {
            fundingAmount = getF_IN_remittanceProcessRq().getInstructedAmount();
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("FUNDING AMOUNT:::: " + fundingAmount);
        }

        setF_OUT_FundingAmount(fundingAmount);
    }

    /**
     * Method Description:Funding amount when the module configuration under SWIFT module for
     * SHOWSENDERCHARGESHA = "N"
     * 
     * @param suppressInstructedAmt
     * @param swtRemitanceReq
     * @return
     */
    private BigDecimal fundingAmtforSHA_N_Case(Boolean suppressInstructedAmt, BigDecimal instructedAmount) {
        BigDecimal fundingAmount = BigDecimal.ZERO;
        if (suppressInstructedAmt) {
            fundingAmount = BigDecimal.ZERO;
        }
        else {
            fundingAmount = instructedAmount;
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
            BigDecimal instructedAmount) {
        BigDecimal fundingAmount = BigDecimal.ZERO;
        if (suppressInstructedAmt && chargeDetailAmount.compareTo(BigDecimal.ZERO) == 0) {
            fundingAmount = BigDecimal.ZERO;
        }
        else if (suppressInstructedAmt && chargeDetailAmount.compareTo(BigDecimal.ZERO) > 0) {
            fundingAmount = instructedAmount;
        }
        else {
            fundingAmount = instructedAmount;

        }
        return fundingAmount;
    }
}
