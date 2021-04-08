package com.misys.ub.fatoms.batch.bpwFailedTrans;

import java.util.HashMap;
import java.util.Map;

import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;

public class UB_BPW_FailedTransactionBatchFatomContext extends AbstractFatomContext{
	private static final String PROCESS_CLASSNAME = loadProcessClassName(
			"BPWFailedTransBatch",
			"com.misys.ub.fatoms.batch.bpwFailedTrans.BPWFailedTransBatchProcess");
	
	private Map inputTagDataMap;
	private Map outputTagDataMap;
	private String batchProcessName;
	private String serviceName;
	private Object[] additionalParams;
	
	public UB_BPW_FailedTransactionBatchFatomContext(String batchProcessName) {
		this.batchProcessName = batchProcessName;
		additionalParams = new Object[2];
		inputTagDataMap = new HashMap();
		outputTagDataMap = new HashMap();
	}
	
	public Object[] getAdditionalProcessParams() {		
		return additionalParams;
	}

	public String getBatchProcessName() {
		return batchProcessName;
	}

	public Map getInputTagDataMap() {
		return inputTagDataMap;
	}

	public Map getOutputTagDataMap() {	
		return outputTagDataMap;
	}

	public void setAdditionalProcessParams(Object[] additionalParams) {
		this.additionalParams = additionalParams;

	}

	public void setBatchProcessName(String batchProcessName) {
		this.batchProcessName = batchProcessName;

	}

	public void setInputTagDataMap(Map inputTagDataMap) {
		this.inputTagDataMap = inputTagDataMap;

	}

	public void setOutputTagDataMap(Map outputTagDataMap) {
		this.outputTagDataMap = outputTagDataMap;

	}

	public String getProcessClassName() {
		return PROCESS_CLASSNAME;
	}

	public String getServiceName() {
		return serviceName;
	}
}
