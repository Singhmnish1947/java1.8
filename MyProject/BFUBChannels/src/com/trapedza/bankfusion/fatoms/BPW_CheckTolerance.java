/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.misys.ub.treasury.events.TreasuryEventCodes;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_CheckTolerance;
import com.trapedza.bankfusion.steps.refimpl.IBPW_CheckTolerance;

public class BPW_CheckTolerance extends AbstractBPW_CheckTolerance implements IBPW_CheckTolerance {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public BPW_CheckTolerance(BankFusionEnvironment env) {
        super(env);
    }

    private transient final static Log logger = LogFactory.getLog(BPW_CheckTolerance.class.getName());

    public void process(BankFusionEnvironment env) {
        BigDecimal Amount, ExchangeRate;
        String ExchangeRateType, Currency1, Currency2, MultiplyDivide;
        boolean result;
        Amount = getF_IN_Amount();
        ExchangeRate = getF_IN_ExchangeRate();
        Currency1 = getF_IN_Currency1();
        ExchangeRateType = getF_IN_ExchangeRateType();
        Currency2 = getF_IN_Currency2();
        MultiplyDivide = getF_IN_MultiplyDivide();

        logger.info("Currency1: " + Currency1);
        logger.info("Currency2: " + Currency2);
        logger.info("Amount: " + Amount);
        logger.info("ExchangeRate: " + ExchangeRate);
        logger.info("ExchangeRateType: " + ExchangeRateType);
        logger.info("MultiplyDivide: " + MultiplyDivide);
        if ((Currency1 != Currency2) && (!(Currency2.equals(CommonConstants.EMPTY_STRING)))) {
            IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            result = bizInfo.isExchangeRateWithinCurrencyTolerance(ExchangeRate, MultiplyDivide, ExchangeRateType, Currency1,
                    Currency2, Amount, env);
            if (result) {
                logger.info("valid tolerance");
            }
            else {

                // throw new BankFusionException(7024, new Object[] {}, logger, env);
                EventsHelper.handleEvent(TreasuryEventCodes.E_EXCHANGE_RATE_GIVEN_IS_BELOW_OR_ABOVE_TOLERANCE, new Object[] {},
                        new HashMap(), env);

            }

        }

    }
}
