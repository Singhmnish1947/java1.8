package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.fbe.common.helper.ResourceArtifactHelper;
import com.misys.ub.swift.SwiftRuleObjects;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_CheckNonSTPRules;

import bf.com.misys.cbs.types.RuleExecRsData;
import bf.com.misys.cbs.types.RuleInputData;
import bf.com.misys.cbs.types.RuleInputRq;
import bf.com.misys.cbs.types.SystemObjectOutputDetail;
import bf.com.misys.ub.types.interfaces.SWIFT_MT103_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT200_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT202Cov_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT202_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT205Cov_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT205_Rule;

public class CheckNonSTPRules extends AbstractUB_SWT_CheckNonSTPRules {
	
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	
	private transient final static Log logger = LogFactory.getLog(CheckNonSTPRules.class.getName());
	
	private List<String> ruleIds = null;
	private SWIFT_MT103_Rule swiftMT103Rule = null;
	private SWIFT_MT200_Rule swiftMT200Rule = null;
	private SWIFT_MT202_Rule swiftMT202Rule = null;
	private SWIFT_MT205_Rule swiftMT205Rule = null;
	private SWIFT_MT202Cov_Rule swiftMT202CovRule = null;
	private SWIFT_MT205Cov_Rule swiftMT205CovRule = null;
	private String ruleCategory = null;
		
	public CheckNonSTPRules(BankFusionEnvironment env)
	{
		super(env);
	}
	
	
	public void process(BankFusionEnvironment env)
	{
		Boolean output = false;
		
		SystemObjectOutputDetail ubMTObject = new SystemObjectOutputDetail();
		List<Object> mtObjectList = new ArrayList<Object>();
		
		if("MT202".equals(getF_IN_category()) && (null!=getF_IN_SWIFT_MT202().getCover() && "COV".equals(getF_IN_SWIFT_MT202().getCover()))){
			ruleCategory = getF_IN_category().concat("Cov");
		}
		else if("MT205".equals(getF_IN_category()) && (null!=getF_IN_SWIFT_MT205().getCover()  && "COV".equals(getF_IN_SWIFT_MT205().getCover()))){
			ruleCategory = getF_IN_category().concat("Cov");
		}
		else
			ruleCategory = getF_IN_category();
	
        ruleIds = ResourceArtifactHelper.getRuleIdList(ruleCategory, CommonConstants.Y);
		SwiftRuleObjects swiftRuleObjects  = new SwiftRuleObjects();
		if("MT103".equalsIgnoreCase(ruleCategory)){
			ubMTObject.setSystemObjectName("MT103Rule");
			swiftMT103Rule = swiftRuleObjects.generateMT103RuleObject(getF_IN_SWIFT_MT103());
			mtObjectList.add(swiftMT103Rule);
		}
		else if("MT200".equalsIgnoreCase(ruleCategory)){
			ubMTObject.setSystemObjectName("MT200Rule");
			swiftMT200Rule = swiftRuleObjects.generateMT200RuleObject(getF_IN_SWIFT_MT200());
			mtObjectList.add(swiftMT200Rule);
		}
		else if("MT202".equalsIgnoreCase(ruleCategory)){
			ubMTObject.setSystemObjectName("MT202Rule");
			swiftMT202Rule = swiftRuleObjects.generateMT202RuleObject(getF_IN_SWIFT_MT202());
			mtObjectList.add(swiftMT202Rule);
		}
		else if("MT202Cov".equalsIgnoreCase(ruleCategory)){
			ubMTObject.setSystemObjectName("MT202CovRule");
			swiftMT202CovRule = swiftRuleObjects.generateMT202CovRuleObject(getF_IN_SWIFT_MT202());
			mtObjectList.add(swiftMT202CovRule);
		}
		else if("MT205".equalsIgnoreCase(ruleCategory)){
			ubMTObject.setSystemObjectName("MT205Rule");
			swiftMT205Rule = swiftRuleObjects.generateMT205RuleObject(getF_IN_SWIFT_MT205());
			mtObjectList.add(swiftMT205Rule);
		}
		else if("MT205Cov".equalsIgnoreCase(ruleCategory)){
			ubMTObject.setSystemObjectName("MT205CovRule");
			swiftMT205CovRule = swiftRuleObjects.generateMT205CovRuleObject(getF_IN_SWIFT_MT205());
			mtObjectList.add(swiftMT205CovRule);
		}
		ubMTObject.setSystemObjectList(mtObjectList);
		
		for(String ruleId : ruleIds){
			RuleInputRq ruleInputRq = new RuleInputRq ();
			RuleInputData ruleInputData = new RuleInputData();
			ruleInputData.setRuleId(ruleId);
			ruleInputData.addInputData(ubMTObject);
		    
			ruleInputRq.setRuleInputList(ruleInputData);

			ExecuteRuleThrough executeRuleThrough = new ExecuteRuleThrough();
		    executeRuleThrough.setF_IN_inputReq(ruleInputRq);
			executeRuleThrough.process(env);
	        RuleExecRsData response = executeRuleThrough.getF_OUT_ruleResp();
	        List<Boolean> outputFlagList = (List<Boolean>) response.getRuleData();
	        
	        for(Boolean outputFlag : outputFlagList){
	        	if(outputFlag){
	        		output = outputFlag;
	        		logger.info("The Rule Id: "+ruleId);
	        		setF_OUT_ruleId(ruleId);
	        		logger.info("The Rule Execution output is: "+output);
	        		setF_OUT_ruleOutput(output);
	        		break;
	        	}
	        }
	        if(output)
	        	break;
		}
		
	}
	
}
