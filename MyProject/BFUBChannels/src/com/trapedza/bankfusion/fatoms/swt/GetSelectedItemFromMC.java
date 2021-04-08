/**
 * 
 */
package com.trapedza.bankfusion.fatoms.swt;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GetSelectedItemFromMC;

/**
 * <code>GetSelectedItemFromMC</code> class is an utility class which helps to get
 * the selected item from the module configuration output collection.
 * 
 * @author Chethan.ST
 *
 */
public class GetSelectedItemFromMC extends AbstractUB_SWT_GetSelectedItemFromMC{

	 private static final long serialVersionUID = 1L;
	 private static final transient Log logger = LogFactory.getLog(GetSelectedItemFromMC.class.getName());
	 private VectorTable mcOutputVector = null;  


	 @SuppressWarnings("deprecation")
    public GetSelectedItemFromMC(BankFusionEnvironment bfEnv) {
        super(bfEnv);
    }

    /**
   	 * 
   	 */
    public GetSelectedItemFromMC() {
        super();
    }
	
	 @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        logger.info("-----InterestRateDetailsOperations: In process method ---");
        initializeIOParams();
        doPerform();
    }

    /**
     * <code>initializeIOParams</code> method initialize the input params which are required for
     * this class.
     * 
     * @author Chethan.ST
     */
    private void initializeIOParams() {
        if (null != getF_IN_mcCollection()) {
        	mcOutputVector = getF_IN_mcCollection();
        }
    }
    
    /**
     * <code>doPerform</code> method traverse through the collection and calls the setOutputParams to set the
     * selected row data.
     * 
     * @author Chethan.ST
     */
    private void doPerform() {
    	if(null != mcOutputVector) {
    		for (int i = 0; i < mcOutputVector.size(); i++) {
                HashMap<String, ?> dataMap = mcOutputVector.getRowTags(i);
                if(null != dataMap && (Boolean)dataMap.get("SELECT")) {
                	setOutputParams(dataMap.get("PARAMVALUE").toString(),dataMap.get("PARAMNAME").toString(),
                	dataMap.get("PARAMDATATYPE").toString(),dataMap.get("MODULENAME").toString(),dataMap.get("BOID").toString(),
                	dataMap.get("PARAMDESC").toString());
                	break;
                }
            }
    	}
    }
    
    /**
     * <code>setOutputParams</code> method sets output details.
     * 
     * 
     * @param paramValue
     * @param paramName
     * @param paramDataType
     * @param moduleName
     * @param ConfigurationId
     * @param paramDesc
     * 
     * @author Chethan.ST
     */

	private void setOutputParams(String paramValue, String paramName, String paramDataType, String moduleName,
			String ConfigurationId, String paramDesc) {
        setF_OUT_MODULECONFIGURATIONID(ConfigurationId);
        setF_OUT_MODULENAME(moduleName);
        setF_OUT_PARAMDATATYPE(paramDataType);
        setF_OUT_PARAMDESC(paramDesc);
        setF_OUT_PARAMNAME(paramName);
        setF_OUT_PARAMVALUE(paramValue);
    }
}
