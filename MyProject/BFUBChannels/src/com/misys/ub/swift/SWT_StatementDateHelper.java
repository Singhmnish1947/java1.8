package com.misys.ub.swift;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CMN_ModuleConfiguration;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.PreviousWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

public class SWT_StatementDateHelper {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(SWT_StatementDateHelper.class.getName());

    public boolean CheckGenerateMessage(String bankName, String accountNumber, String frequencyPeriodCode, int frequencyPeriodUnit,
            int statementDay, int statementMonth, Date lastStatementDate, BankFusionEnvironment env) {
        boolean statementRequired = true;

        if (lastStatementDate.compareTo(SystemInformationManager.getInstance().getBFBusinessDateTime()) > 0) {
            logger.error("Last Statement Date is greater than the Business Date set for account : " + accountNumber);
            statementRequired = false;
            return statementRequired;
        }
        else if (lastStatementDate.compareTo(SystemInformationManager.getInstance().getBFBusinessDateTime()) == 0) {
            logger
                    .error("Last Statement Date is equal to current Business Date. Statement has been already generated on this account : "
                            + accountNumber);
            statementRequired = false;
            return statementRequired;
        }

        // Initialize next statement date to last statement date.
        Calendar nextStatementDate = Calendar.getInstance();
        nextStatementDate.setTime(lastStatementDate);
        nextStatementDate.set(Calendar.HOUR, 00);
        nextStatementDate.set(Calendar.MINUTE, 00);
        nextStatementDate.set(Calendar.SECOND, 00);
        nextStatementDate.set(Calendar.MILLISECOND, 00);
        
    	nextStatementDate.set(Calendar.AM_PM, Calendar.AM);
		nextStatementDate.set(Calendar.DST_OFFSET, 0);

        if (frequencyPeriodCode != null && frequencyPeriodCode.trim().length() > 0) {
            switch (frequencyPeriodCode.charAt(0)) {
                case 'D':
                    nextStatementDate.add(Calendar.DATE, frequencyPeriodUnit);
                    statementRequired = evaluateNextStatementDate(bankName, nextStatementDate, env);
                    break;

                case 'W':
                   // nextStatementDate.add(Calendar.DATE, (7 * frequencyPeriodUnit));
                	nextStatementDate.add(Calendar.DATE, (7 * 1));
                    nextStatementDate.set(Calendar.DAY_OF_WEEK, statementDay);
                    statementRequired = evaluateNextStatementDate(bankName, nextStatementDate, env);
                    break;

                case 'M':
                    //nextStatementDate.add(Calendar.MONTH, frequencyPeriodUnit);
                	nextStatementDate.add(Calendar.MONTH, 1);
                    nextStatementDate.set(Calendar.DATE, statementDay);
                    statementRequired = evaluateNextStatementDate(bankName, nextStatementDate, env);
                    break;

                case 'Q':
                    nextStatementDate.add(Calendar.MONTH, 3);
                    nextStatementDate.set(Calendar.DATE, statementDay);
                    statementRequired = evaluateNextStatementDate(bankName, nextStatementDate, env);
                    break;

                case 'H':
                    nextStatementDate.add(Calendar.MONTH, 6);
                    nextStatementDate.set(Calendar.DATE, statementDay);
                    statementRequired = evaluateNextStatementDate(bankName, nextStatementDate, env);
                    break;

                case 'Y':
                    //nextStatementDate.add(Calendar.YEAR, frequencyPeriodUnit);
                	nextStatementDate.add(Calendar.YEAR, 1);
                    //nextStatementDate.set(Calendar.MONTH, statementMonth);
                    nextStatementDate.set(Calendar.DATE, statementDay);
                    statementRequired = evaluateNextStatementDate(bankName, nextStatementDate, env);
                    break;

                default:
                    logger.error("Invalid Frequency Period Code : Given code " + frequencyPeriodCode + "does not exist.");
                    statementRequired = false;
                    break;
            }
        }
        else {
            logger.error("Invalid Frequency Period Code : Given code " + frequencyPeriodCode + "does not exist.");
            statementRequired = false;
        }

        return statementRequired;
    }

    private boolean evaluateNextStatementDate(String bankName, Calendar nextStatementDate, BankFusionEnvironment env) {
        boolean status = false;
        Calendar businessDate = Calendar.getInstance();
        businessDate.setTime(SystemInformationManager.getInstance().getBFBusinessDate());

        if (nextStatementDate.before(businessDate) || nextStatementDate.equals(businessDate)) {
            status = true;
        }
        else {
            String module = "SWIFT";
            String value = "NextStatementDate";

            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

            List moduleConfigList = null;

            try {
                moduleConfigList = (List<IBOCB_CMN_ModuleConfiguration>) ((IBusinessInformation) ubInformationService.getBizInfo())
                        .getModuleConfigurationValue(module, value, env);
            }
            catch (Exception ex) {
                logger.error("Exception occured while trying to access database for finding Working Day configuration.", ex);
                status = false;
                return status;
            }

            IBOCB_CMN_ModuleConfiguration moduleConfig = null;
            int mode = 0;

            // If configuration record is not present then through error
            if (moduleConfigList == null || moduleConfigList.size() <= 0) {
                logger
                        .error("Next Statement Date is greater than current Business Date. Cannot proceed, since Working Day configuration for SWIFT is not present.");
                status = false;
            }
            else {
                moduleConfig = (IBOCB_CMN_ModuleConfiguration) moduleConfigList.get(0);
                mode = Integer.parseInt(moduleConfig.getF_PARAMVALUE());
                switch (mode) {
                    case 5:
                        logger
                                .error("As per the Working Day configuration for SWIFT, the statement has to be generated on the next working day instead of current working day.");
                        status = false;
                        break;
                    case 4:
                        try {
                            Date previousWorkingDate = PreviousWorkingDateForDate.run("BANK", bankName, new Integer(0),
                                    nextStatementDate.getTime(), env);
                            if (previousWorkingDate.before(SystemInformationManager.getInstance().getBFBusinessDate())
                                    || previousWorkingDate.equals(SystemInformationManager.getInstance().getBFBusinessDate())) {
                                status = true;
                            }
                            else {
                                logger.error("Nearest Next Statement Date is greater than current Business Date.");
                                status = false;
                            }
                        }
                        catch (BankFusionException bfe) {
                            logger
                                    .error("Exception occured while calculating Previous Working Day for the generation of statement.");
                            status = false;
                        }
                        break;
                }
            }
        }

        return status;
    }
}
