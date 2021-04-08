package com.misys.ub.payment.swift.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.fbe.common.helper.ResourceArtifactHelper;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.fatoms.ExecuteRuleThrough;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.RuleExecRsData;
import bf.com.misys.cbs.types.RuleInputData;
import bf.com.misys.cbs.types.RuleInputRq;
import bf.com.misys.cbs.types.SystemObjectOutputDetail;
import bf.com.misys.ub.types.interfaces.SWIFTOutgoingRule;

public class CheckNonStpSWIFTOutwardRules {

	private transient final static Log logger = LogFactory.getLog(CheckNonStpSWIFTOutwardRules.class.getName());
    private PaymentSwiftUtils paymentSWTUtils = new PaymentSwiftUtils();

	private List<String> ruleIds = null;
	private String ruleCategory = null;
	private SWIFTOutgoingRule swiftOutoingRule = null;

	public Map<String, Object> checkNonSTPRules(OutwardSwtRemittanceRq outwardSwtRemittance){
        if(logger.isInfoEnabled()) 
            logger.info("IN CheckNonStpSWIFTOutwardRules");
		Boolean output = false;
		Map<String, Object> returnMap = new HashMap<>();

		SystemObjectOutputDetail ubMTObject = new SystemObjectOutputDetail();
		List<Object> mtObjectList = new ArrayList<Object>();
		
		String channelId = outwardSwtRemittance.getRqHeader().getOrig().getChannelId();
		switch(channelId){
		case PaymentSwiftConstants.CHANNELID_IBI:
			ubMTObject.setSystemObjectName("SWIFTOutIBIRule");
			ruleCategory = PaymentSwiftConstants.RULECATEGORY_IBI;
			break;
		case PaymentSwiftConstants.CHANNELID_CCI:
			ubMTObject.setSystemObjectName("SWIFTOutCCIRule");
			ruleCategory = PaymentSwiftConstants.RULECATEGORY_CCI;
			break;
		case PaymentSwiftConstants.CHANNELID_TELLER:
			ubMTObject.setSystemObjectName("SWIFTOutTELLERRule");
			ruleCategory = PaymentSwiftConstants.RULECATEGORY_TELLER;
			break;
		default:
			logger.info("No Rule configuration for the Channel Id: "+channelId);

		}
	      //get list of rules
        ruleIds = ResourceArtifactHelper.getRuleIdList(ruleCategory, CommonConstants.Y);
		String currency = outwardSwtRemittance.getIntlPmtInputRq().getPaymentPosting().getCurrency().getIsoCurrencyCode();
		BigDecimal amount = outwardSwtRemittance.getIntlPmtInputRq().getPaymentPosting().getCurrency().getAmount();
		BigDecimal charges = BigDecimal.ZERO;
		String accountId = outwardSwtRemittance.getIntlPmtInputRq().getFundingPosting().getAccount().getStandardAccountId();
		if(outwardSwtRemittance.getIntlPmtInputRq().getChargesCount()>0)
			charges = outwardSwtRemittance.getIntlPmtInputRq().getCharges(0).getCharge().getChargeCcyAmtDetails().getAmount();
		
		//swiftOutoingRule = getSWIFTOutgoingRuleObj(currency, amount, charges, accountId);
		swiftOutoingRule = getSWIFTOutgoingRuleObj(outwardSwtRemittance);
		mtObjectList.add(swiftOutoingRule);
		ubMTObject.setSystemObjectList(mtObjectList);

		for(String ruleId : ruleIds){
			RuleInputRq ruleInputRq = new RuleInputRq ();
			RuleInputData ruleInputData = new RuleInputData();
			ruleInputData.setRuleId(ruleId);
			ruleInputData.addInputData(ubMTObject);
			ruleInputRq.setRuleInputList(ruleInputData);
			ExecuteRuleThrough executeRuleThrough = new ExecuteRuleThrough();
			executeRuleThrough.setF_IN_inputReq(ruleInputRq);
			executeRuleThrough.process(BankFusionThreadLocal.getBankFusionEnvironment());
			RuleExecRsData response = executeRuleThrough.getF_OUT_ruleResp();
			List<Boolean> outputFlagList = (List<Boolean>) response.getRuleData();
			returnMap.put("NonSTPStatus", Boolean.FALSE);
			for(Boolean outputFlag : outputFlagList){
				if(outputFlag){
					output = outputFlag;					
					returnMap.put("NonSTPStatus", output);
					returnMap.put("NonSTPRule", ruleId);
					break;
				}
			}
			if(output)
				break;
		}
		return returnMap;
	}


	private SWIFTOutgoingRule getSWIFTOutgoingRuleObj(OutwardSwtRemittanceRq outwardSwtRemittance){
		swiftOutoingRule = new SWIFTOutgoingRule();

		swiftOutoingRule.setAri(paymentSWTUtils.getAccountRightsInd(outwardSwtRemittance.getIntlPmtInputRq().getFundingPosting().getAccount().getStandardAccountId()));
		swiftOutoingRule.setAvailableBalance(paymentSWTUtils.getAvailableBalance(outwardSwtRemittance.getIntlPmtInputRq().getFundingPosting().getAccount().getStandardAccountId(), outwardSwtRemittance.getIntlPmtInputRq().getPaymentPosting().getCurrency().getIsoCurrencyCode())); //UB_CMN_GetAvailableBalance_SRV.bfg
		swiftOutoingRule.setCharges(BigDecimal.ZERO);
		swiftOutoingRule.setTransactionAmount(outwardSwtRemittance.getIntlPmtInputRq().getPaymentPosting().getCurrency().getAmount());
		swiftOutoingRule.setTransactionCurrency(outwardSwtRemittance.getIntlPmtInputRq().getPaymentPosting().getCurrency().getIsoCurrencyCode());
		
		
		swiftOutoingRule.setBeneInstitutionIdentifierCode(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getBenInstInstrDtls().getBicCode());
		swiftOutoingRule.setBeneInstitutionNameAddressLine01(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
				.getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine1());
		swiftOutoingRule.setBeneInstitutionNameAddressLine02(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
				.getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine2());
		swiftOutoingRule.setBeneInstitutionNameAddressLine03(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
				.getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine3());
		swiftOutoingRule.setBeneInstitutionNameAddressLine04(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                .getSettInstrBasic().getBenInstInstrDtls().getText().getTextLine4());
		
		
		swiftOutoingRule.setPayToIdentifierCode(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getPayToInstrDtls().getBicCode());
		swiftOutoingRule.setPayToNameAddressLine01(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
				.getSettInstrBasic().getPayToInstrDtls().getText().getTextLine1());
		swiftOutoingRule.setPayToNameAddressLine02(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                .getSettInstrBasic().getPayToInstrDtls().getText().getTextLine2());
		swiftOutoingRule.setPayToNameAddressLine03(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                .getSettInstrBasic().getPayToInstrDtls().getText().getTextLine3());
		swiftOutoingRule.setPayToNameAddressLine04(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                .getSettInstrBasic().getPayToInstrDtls().getText().getTextLine4());
		
		
		swiftOutoingRule.setIntermediaryIdentifierCode(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls().getSettInstrBasic().getIntInstrDtls().getBicCode());
		swiftOutoingRule.setIntermediaryNameAddressLine01(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getIntInstrDtls().getText().getTextLine1());
		swiftOutoingRule.setIntermediaryNameAddressLine02(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                    .getSettInstrBasic().getIntInstrDtls().getText().getTextLine2());
		swiftOutoingRule.setIntermediaryNameAddressLine03(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                .getSettInstrBasic().getIntInstrDtls().getText().getTextLine3());
		swiftOutoingRule.setIntermediaryNameAddressLine04(outwardSwtRemittance.getIntlPmtInputRq().getSettInstrDtls()
                .getSettInstrBasic().getIntInstrDtls().getText().getTextLine4());
		
		swiftOutoingRule.setDebitTransactionMISCode(outwardSwtRemittance.getSwftAdditionalDetails().getDebitTxnCode());
		swiftOutoingRule.setCreditTransactionMISCode(outwardSwtRemittance.getSwftAdditionalDetails().getCreditTxnCode());
		
		
		
		return swiftOutoingRule;
	}

}
