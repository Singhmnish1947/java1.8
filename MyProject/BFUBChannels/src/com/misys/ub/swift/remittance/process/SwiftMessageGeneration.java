package com.misys.ub.swift.remittance.process;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.RemittanceProcessManager;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.RsHeader;

public class SwiftMessageGeneration implements Command {
    private transient final static Log LOGGER = LogFactory.getLog(SwiftMessageGeneration.class);
    @Override
    public boolean execute(Context context) throws Exception {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN SwiftMessageGeneration");
        
        boolean endOfChain = Boolean.FALSE;
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        SwiftRemittanceRq swtRemitanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");
        if (remittanceDto.isStp()) {
	        GenerateSwiftMessage genMsg = new GenerateSwiftMessage();
	        RsHeader rsHeader = genMsg.generateSwiftMsg(swtRemitanceReq, swtRemitterResp, remittanceDto);
	
	        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
	            endOfChain = Boolean.TRUE;
	        }
	        swtRemitterResp.setRsHeader(rsHeader);
        }
        else {
        	swtRemitterResp.getInitiateSwiftMessageRsDtls().setRemittanceId(StringUtils.EMPTY);
        }
        context.put("swtRemitterResp", swtRemitterResp);
        
        if (LOGGER.isInfoEnabled())
            LOGGER.info("END of  SwiftMessageGeneration" + endOfChain);

        return endOfChain;
    }

}
