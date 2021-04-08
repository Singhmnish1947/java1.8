package com.misys.ub.swift.remittance.process;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.MessageStatus;

public class SaveSSI implements Command {
    private static final transient Log LOGGER = LogFactory.getLog(SaveSSI.class.getName());
    @Override
    public boolean execute(Context context) throws Exception {
         if (LOGGER.isInfoEnabled())
             LOGGER.info("IN SaveSSI");
         
        //boolean to decide whether the chain needs to be stopped. will be set to true if any
        //exception encountered
        boolean endofChain = Boolean.FALSE;

        
        ProcessSaveSSI processSaveSSI = new ProcessSaveSSI();
        
        //calling the method to save the SSI, which returns the response object with update
        //rsHeader is any exception encountered
        processSaveSSI.saveSSI(context);
        
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");

         //checking the overAll status in the rsHeader if, E, setting the endOfChain to true which
         //will be returned, and in turn decides whether the chain needs to broken
        if (swtRemitterResp.getRsHeader() != null && swtRemitterResp.getRsHeader().getStatus() != null) {
            MessageStatus txnLogStatus = swtRemitterResp.getRsHeader().getStatus();
            if (txnLogStatus != null && txnLogStatus.getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
                endofChain = Boolean.TRUE;
            }
        }

        if (LOGGER.isInfoEnabled())
            LOGGER.info("END SaveSSI");
        
        return endofChain;
    }

}
