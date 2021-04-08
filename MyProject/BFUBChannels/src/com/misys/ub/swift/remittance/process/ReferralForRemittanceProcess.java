/* ********************************************************************************
 *  Copyright(c)2019  Finastra. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */

package com.misys.ub.swift.remittance.process;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.misys.fbe.common.util.CommonUtil;

public class ReferralForRemittanceProcess implements Command {

    @Override
    public boolean execute(Context context) throws Exception {

        CommonUtil.handleParameterizedEvent(SwiftEventCodes.REMITTANCE_PROSESSING_REFERRAL, new String[] {});

        return false;
    }

}
