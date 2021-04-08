/* ********************************************************************************
 *  Copyright(c)2019  Finastra. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */

package com.misys.ub.swift.remittance.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.CheckNonStpSWIFTModuleConfig;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.trapedza.bankfusion.core.CommonConstants;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;

public class StpNonStpCheck implements Command {
    private static final Log LOGGER = LogFactory.getLog(StpNonStpCheck.class.getName());
    private Map<String, Object> nonSTPOut = new HashMap<>();

    @Override
    public boolean execute(Context context) throws Exception {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("START STP and Non STP check  ");
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        SwiftRemittanceRq swtRemitanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");

        Boolean isNonStp = isNonStpModConfig(swtRemitanceReq);
        isNonStp = isNonStpRule(isNonStp, swtRemitanceReq, remittanceDto);
        String ruleId = (String) (nonSTPOut.get("NonSTPRule") == null ? CommonConstants.EMPTY_STRING : nonSTPOut.get("NonSTPRule"));
        remittanceDto.setRuleId(ruleId);
        remittanceDto.setStp(!isNonStp);

        context.put("remittanceDto", remittanceDto);
        context.put("swtRemitanceReq", swtRemitanceReq);
        context.put("swtRemitterResp", swtRemitterResp);

        return false;
    }

    /**
     * @return
     */
    private Boolean isNonStpModConfig(SwiftRemittanceRq swtRemitanceReq) {
        CheckNonStpSWIFTModuleConfig checkOutwardModConfig = new CheckNonStpSWIFTModuleConfig();
        return checkOutwardModConfig.checkNonStpModuleConfig(swtRemitanceReq.getRqHeader().getOrig().getChannelId());
    }

    /**
     * @param nonStp
     * @return
     */
    private Boolean isNonStpRule(Boolean nonStp, SwiftRemittanceRq swtRemitanceReq, RemittanceProcessDto remittanceDto) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Start isNonStpRule::::" + nonStp);
        }
        Boolean nonSTPRule = Boolean.FALSE;
        if (nonStp) {
            CheckNonStpOutwardRules checkOutwardRules = new CheckNonStpOutwardRules();
            nonSTPOut = checkOutwardRules.checkNonSTPRules(swtRemitanceReq, remittanceDto);
            if (nonSTPOut != null && nonSTPOut.size() != 0) {
                nonSTPRule = (Boolean) nonSTPOut.get("NonSTPStatus");
            }
            else {
                nonSTPRule = Boolean.FALSE;
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("END isNonStpRule::::" + nonStp);
        }
        return (nonStp && !nonSTPRule);
    }

}
