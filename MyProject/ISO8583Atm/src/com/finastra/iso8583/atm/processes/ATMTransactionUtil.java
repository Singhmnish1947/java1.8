package com.finastra.iso8583.atm.processes;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbe.dc.common.persistence.UtilityFinderMethods;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.utils.BankFusionMessages;

import bf.com.misys.cbs.types.CurrencyCodeDetails;
import bf.com.misys.cbs.types.CurrencyDetails;

public class ATMTransactionUtil {

	private static final IBusinessInformationService BIZ_INFO_SERVICE;
	final static Log LOGGER = LogFactory.getLog(ATMTransactionUtil.class.getName());
	private static final String loggerHandleEvent = " Handled event ";
	static String currencyCode = "";

	static {
		BIZ_INFO_SERVICE = (IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
				.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

	}

	public static String getModuleConfigurationValue(String module, String paramName, BankFusionEnvironment env) {
		return (String) BIZ_INFO_SERVICE.getBizInfo().getModuleConfigurationValue(module, paramName, env);
	}

	public static void handleEvent(Integer eventNumber, String[] args) {
		LOGGER.info(loggerHandleEvent + eventNumber);
		if (args == null) {
			args = new String[] { CommonConstants.EMPTY_STRING };
		}
		bf.com.misys.cbs.types.events.Event event = new bf.com.misys.cbs.types.events.Event();
		event.setEventNumber(eventNumber);
		event.setMessageArguments(args);
		IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance()
				.getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
		businessEventsService.handleEvent(event);
	}

	public static String getCurrencyFromNumericCurrency(String numericCurrencyCode) {
		if (StringUtils.isEmpty(numericCurrencyCode))
			return numericCurrencyCode;
		CurrencyDetails currencyDetails = new CurrencyDetails();
		CurrencyCodeDetails currencyCodeDetails = new CurrencyCodeDetails();
		if (StringUtils.isNumeric(numericCurrencyCode)) {
			currencyCodeDetails.setIsoCurrencyCode(
					SystemInformationManager.getInstance().transformCurrencyCode(numericCurrencyCode, true));
			currencyCodeDetails.setNumericCode(Integer.parseInt(numericCurrencyCode));
			currencyDetails.setCurrencyCodeDetails(currencyCodeDetails);
		} else {
			currencyCodeDetails.setIsoCurrencyCode(numericCurrencyCode);
			currencyCodeDetails.setNumericCode(Integer.parseInt(
					SystemInformationManager.getInstance().transformCurrencyCode(numericCurrencyCode, false)));
			currencyDetails.setCurrencyCodeDetails(currencyCodeDetails);
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Currency:" + currencyDetails.getCurrencyCodeDetails().getIsoCurrencyCode()
					+ " NumericISO Currency Code:" + currencyDetails.getCurrencyCodeDetails().getNumericCode());

		return currencyDetails.getCurrencyCodeDetails().getIsoCurrencyCode();
	}

	public static String getCurrencyForAccount(String accountNo) {
		currencyCode = UtilityFinderMethods.getAccountDetails(accountNo).getF_ISOCURRENCYCODE();
		return currencyCode;
	}
	public static String getErrorResponseTitle() {
		return BankFusionMessages.getInstance().getFormattedEventMessage(ATMAPIConstant.ERROR_TITLE, new Object[] {},
				BankFusionThreadLocal.getUserSession().getUserLocale());
	}

}
