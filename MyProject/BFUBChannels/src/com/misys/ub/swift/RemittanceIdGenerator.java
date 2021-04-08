package com.misys.ub.swift;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.common.constants.GeneralConstants;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.services.autonumber.IAutoNumberService;

public class RemittanceIdGenerator {
	private transient final static Log logger = LogFactory.getLog(RemittanceIdGenerator.class.getClass());
	public String getRemittanceId(String currency){
		String remittanceId = null;
		SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMYYYYHHmmss");
        Date now = SystemInformationManager.getInstance().getBFSystemDate();
	    String strDate = sdfDate.format(now);
		remittanceId = "INWR".concat(currency).concat(strDate)
				.concat(String.valueOf(generateRateTypeSequenceId(GeneralConstants.SWIFT_INWR_REMITTANCE_ID)));
		logger.debug(" Generated remittanceId :: "+remittanceId);
		return remittanceId;
	}
	
	/**
     * <code>generateRateTypeSequenceId</code> method uses the autonumber generation service and
     * generates the unique sequence number to maintain the band rate information.
     * 
     * 
     * @return Unique number
     * @author Chethan.ST
     */
    public static Integer generateRateTypeSequenceId(String autoNumberString) {
        IServiceManager sm = ServiceManagerFactory.getInstance().getServiceManager();
        IAutoNumberService autoNumSrvc = (IAutoNumberService) sm.getServiceForName(ServiceManager.AUTO_NUMBER_SERVICE);
        return Integer.parseInt(autoNumSrvc.getNextId(autoNumberString, "", ""));

    }
    
}
