package com.misys.ub.fatoms;

import java.sql.Date;
import java.time.temporal.ChronoUnit;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractIncomingRepairReportDateValidation;

public class IncomingRepairReportDateValidation extends AbstractIncomingRepairReportDateValidation {
	private IBusinessInformationService BIZ_INFO_SERVICE = (IBusinessInformationService) ServiceManagerFactory
			.getInstance().getServiceManager().getServiceForName("BusinessInformationService");  
	
	 private String getModuleConfigValue(String param, String moduleId) {
	        return String.valueOf(this.BIZ_INFO_SERVICE.getBizInfo().getModuleConfigurationValue(moduleId, param, null)); 
	}
	 
	public IncomingRepairReportDateValidation(BankFusionEnvironment env) {
		super(env);
	}
	
	public IncomingRepairReportDateValidation() {
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		Date fromDate = getF_IN_FromBusinessDate();
		Date toDate = getF_IN_ToBusinessDate();
	    Integer dateRange = Integer.parseInt(getModuleConfigValue("REPORT_CAL_DAYS", "CBS"));
	    
	    if(fromDate.after(toDate)) {
	    	CommonUtil.handleUnParameterizedEvent(SwiftEventCodes.I_SWT_FROMDATE_GREATER_TODATE_UB15);}
       
	    if(ChronoUnit.DAYS.between(fromDate.toLocalDate(),toDate.toLocalDate()) > dateRange) {
	    	CommonUtil.handleParameterizedEvent(SwiftEventCodes.E_CB_DATE_RANGE_VALIDATION, new String[] {"90"});}
	}
	
}
