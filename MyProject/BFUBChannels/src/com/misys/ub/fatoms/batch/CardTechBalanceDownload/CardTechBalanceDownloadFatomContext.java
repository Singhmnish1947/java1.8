package com.misys.ub.fatoms.batch.CardTechBalanceDownload;
import java.util.HashMap;
import java.util.Map;

import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;

public class CardTechBalanceDownloadFatomContext extends AbstractFatomContext {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final String PROCESS_CLASSNAME = loadProcessClassName("CardTechBalanceDownloadProcess",
    "com.misys.ub.fatoms.batch.CardTechBalanceDownload.CardTechBalanceDownloadProcess");

private String batchProcessName;
private String serviceName;
private Map inputTagDataMap;
private Map outputTagDataMap;
private Object[] additionalParams;

/**
* 
*/
public CardTechBalanceDownloadFatomContext(String batchProcessName) {
	this.batchProcessName = batchProcessName;
	inputTagDataMap = new HashMap();
	outputTagDataMap = new HashMap();
	
}

/**
* Returns the Process Class name
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getProcessClassName()
* @return The Process Class name
*/
public String getProcessClassName() {
return PROCESS_CLASSNAME;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getAdditionalProcessParams()
*/
public Object[] getAdditionalProcessParams() {
return additionalParams;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getBatchProcessName()
*/
public String getBatchProcessName() {
return batchProcessName;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getInputTagDataMap()
*/
public Map getInputTagDataMap() {
return inputTagDataMap;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#getOutputTagDataMap()
*/
public Map getOutputTagDataMap() {
return outputTagDataMap;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setAdditionalProcessParams(java.lang.Object[])
*/
public void setAdditionalProcessParams(Object[] additionalParams) {
this.additionalParams = additionalParams;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setBatchProcessName(java.lang.String)
*/
public void setBatchProcessName(String batchProcessName) {
this.batchProcessName = batchProcessName;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setInputTagDataMap(java.util.Map)
*/
public void setInputTagDataMap(Map inDataMap) {
this.inputTagDataMap = inDataMap;
}

/*
* (non-Javadoc)
* 
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setOutputTagDataMap(java.util.Map)
*/
public void setOutputTagDataMap(Map outDataMap) {
this.outputTagDataMap = outDataMap;
}

/**
* Sets the output tag data map
* @param serviceName The service name
* @see com.trapedza.bankfusion.batch.fatom.AbstractFatomContext#setServiceName(java.lang.String)
*/
public void setServiceName(String serviceName) {
this.serviceName = serviceName;
}

}

