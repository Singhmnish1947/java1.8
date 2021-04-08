package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.fbe.common.helper.ResourceArtifactHelper;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.fatoms.ExecuteRuleThrough;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.RuleExecRsData;
import bf.com.misys.cbs.types.RuleInputData;
import bf.com.misys.cbs.types.RuleInputRq;
import bf.com.misys.cbs.types.SystemObjectOutputDetail;
import bf.com.misys.ub.types.interfaces.SWIFTOutgoingRule;

public class CheckNonStpOutwardRules {

	private transient final static Log logger = LogFactory.getLog(CheckNonStpOutwardRules.class.getName());
    private PaymentSwiftUtils paymentSWTUtils = new PaymentSwiftUtils();
    
	private List<String> ruleIds = null;
	private String ruleCategory = null;
	private SWIFTOutgoingRule swiftOutoingRule = null;

	public Map<String, Object> checkNonSTPRules(SwiftRemittanceRq swtRemitanceReq, RemittanceProcessDto remittanceDto){

        if (logger.isInfoEnabled())
            logger.info("IN CheckNonStpOutwardRules");
		Boolean output = false;
		Map<String, Object> returnMap = new HashMap<>();

		SystemObjectOutputDetail ubMTObject = new SystemObjectOutputDetail();
		List<Object> mtObjectList = new ArrayList<Object>();
		
		String channelId = swtRemitanceReq.getRqHeader().getOrig().getChannelId();
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
			ubMTObject.setSystemObjectName("SWIFTOut"+channelId+"Rule");
			ruleCategory = "SWTO"+channelId;
		}
	    //get list of rules
        ruleIds = ResourceArtifactHelper.getRuleIdList(ruleCategory, CommonConstants.Y);
		
		swiftOutoingRule = getSWIFTOutgoingRuleObj(swtRemitanceReq, remittanceDto);
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

	/**
	 * @param swtRemitanceReq
	 * @return
	 */
	private SWIFTOutgoingRule getSWIFTOutgoingRuleObj(SwiftRemittanceRq swtRemitanceReq, RemittanceProcessDto remittanceDto){
		swiftOutoingRule = new SWIFTOutgoingRule();
		String accountId = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId();
		String currency = swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode();
		swiftOutoingRule.setAri(paymentSWTUtils.getAccountRightsInd(accountId));
		swiftOutoingRule.setAvailableBalance(paymentSWTUtils.getAvailableBalance(accountId, currency));
		swiftOutoingRule.setCharges(BigDecimal.ZERO);
		swiftOutoingRule.setTransactionAmount(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount().getAmount());
		swiftOutoingRule.setTransactionCurrency(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode());
		swiftOutoingRule.setInstructedAmount(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getAmount());
		swiftOutoingRule.setInstructedCurrency(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount().getIsoCurrencyCode());
		swiftOutoingRule.setBeneInstitutionIdentifierCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstIdentifierCode());
		swiftOutoingRule.setBeneInstitutionNameAddressLine01(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine1());
		swiftOutoingRule.setBeneInstitutionNameAddressLine02(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine2());
		swiftOutoingRule.setBeneInstitutionNameAddressLine03(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine3());
		swiftOutoingRule.setBeneInstitutionNameAddressLine04(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution().getBeneficiaryInstitution().getBeneficiaryInstDetails().getTextLine4());
		swiftOutoingRule.setIntermediaryIdentifierCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary().getIntermediaryIdentiferCode());
		swiftOutoingRule.setIntermediaryNameAddressLine01(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine1());
		swiftOutoingRule.setIntermediaryNameAddressLine02(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine2());
		swiftOutoingRule.setIntermediaryNameAddressLine03(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine3());
		swiftOutoingRule.setIntermediaryNameAddressLine04(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getIntermediary().getIntermediaryDetails().getTextLine4());
		swiftOutoingRule.setPayToIdentifierCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToIdentifierCode());
		swiftOutoingRule.setPayToNameAddressLine01(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls1());
		swiftOutoingRule.setPayToNameAddressLine02(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls2());
		swiftOutoingRule.setPayToNameAddressLine03(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls3());
		swiftOutoingRule.setPayToNameAddressLine04(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls4());
		swiftOutoingRule.setDebitTransactionMISCode(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode());
		swiftOutoingRule.setCreditTransactionMISCode(remittanceDto.getCreditTransactionCode());
		
		return swiftOutoingRule;
	}
	
}
