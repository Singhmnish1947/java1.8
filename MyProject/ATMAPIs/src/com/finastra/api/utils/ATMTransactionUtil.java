package com.finastra.api.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.model.Causes;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbe.dc.common.persistence.UtilityFinderMethods;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class ATMTransactionUtil {
	private static final IBusinessInformationService BIZ_INFO_SERVICE;
	final static Log LOGGER = LogFactory.getLog(ATMTransactionUtil.class.getName());
	static {
		BIZ_INFO_SERVICE = (IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
				.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

	}
	

	public static String getModuleConfigurationValue(String module, String paramName, BankFusionEnvironment env) {
		return (String) BIZ_INFO_SERVICE.getBizInfo().getModuleConfigurationValue(module, paramName, env);
	}

	public static void handleEvent(Integer eventNumber, String[] args) {
		LOGGER.info(" Handled event " + eventNumber);
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

	public static String getErrorResponseTitle() {
		return BankFusionMessages.getInstance().getFormattedEventMessage(ATMAPIConstant.ERROR_TITLE, new Object[] {},
				BankFusionThreadLocal.getUserSession().getUserLocale());
	}
	public static com.finastra.api.atm.v1.model.Error createErrorResponse(String errorCd, String errorCodeMsg) {
		List<Causes> causes = new ArrayList<Causes>();
		Causes errorCode = new Causes();

		String badRequestTitle ="The request is invalid and cannot be processed.";
		String badRequestDetail = "Ensure that the request is valid.";
		
		errorCode.setCode(errorCd);
		errorCode.setMessage(errorCodeMsg);
		errorCode.setSeverity("ERROR");
		causes.add(errorCode);

		com.finastra.api.atm.v1.model.Error errorResponse = new com.finastra.api.atm.v1.model.Error();

		errorResponse.setType(ATMAPIConstant.ERROR_TYPE);
		errorResponse.setTitle(badRequestTitle);
		errorResponse.setStatus(400);
		errorResponse.setDetail(badRequestDetail);
		errorResponse.setCauses(causes);
		return errorResponse;
	}
	
	public static com.finastra.api.atm.v1.model.Error createBusinessErrorResponse(String errorCd, String errorCodeMsg) {
		List<Causes> causes = new ArrayList<Causes>();
		Causes errorCode = new Causes();
		
		String businessValidationTitle = "The request could not be processed due to applicable business validation.";
		String businessValidationDetail = "Ensure that the request is valid.";
		
		errorCode.setCode(errorCd);
		errorCode.setMessage(errorCodeMsg);
		errorCode.setSeverity("ERROR");
		causes.add(errorCode);
		com.finastra.api.atm.v1.model.Error errorResponse = new com.finastra.api.atm.v1.model.Error();
		errorResponse.setType(ATMAPIConstant.ERROR_TYPE);
		errorResponse.setTitle(businessValidationTitle);
		errorResponse.setStatus(400);
		errorResponse.setDetail(businessValidationDetail);
		errorResponse.setCauses(causes);
		return errorResponse;
	}
	public static int validateAccount(String accountId1) {
		int errorCode = 0;

		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		IBOAccount account = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountId1, true);

		if (account == null) {
			errorCode = 20020000;// noAccountError
			return errorCode;
		} else {

			if (account.isF_CLOSED()) {
				return 40200284;
			} else if (account.isF_DORMANTSTATUS()) {
				return 40409528;
			} else if (account.isF_STOPPED()) {
				return 40200485;
			}
			if (account.getF_ACCRIGHTSINDICATOR() != 0)
				errorCode = validateAccountRightIndicator(accountId1, account.getF_ACCRIGHTSINDICATOR(), "");
		}
		return errorCode;
	}

	private static int validateAccountRightIndicator(String toAccount, int accountRightIndicator, String operationType) {

		int errorCode = 0;

		switch (accountRightIndicator) {
		case -1:
			errorCode = 40112171;
			break;
		case 1:
			errorCode = 40007319;
			break;
		case 2:
			errorCode = 40007321;
			break;
		case 3:
			errorCode = 40112172;
			break;
		case 4:
			if ("OPTP0000".equals(operationType) || "OPTP0119".equals(operationType) || "OPTP0001".equals(operationType)) {
				errorCode = 40180194;
			}
			break;
		case 5:
			if ("OPTP0000".equals(operationType) || "OPTP0119".equals(operationType) || "OPTP0001".equals(operationType)) {
				errorCode = 40007323;
			}
			break;
		case 6:
			if ("OPTP0020".equals(operationType) || "OPTP0422".equals(operationType)) {
				errorCode = 40205204;
			}
			break;
		case 7:
			if ("OPTP0020".equals(operationType) || "OPTP0422".equals(operationType)) {
				errorCode = 40007325;
			}
			break;
		case 8:
			errorCode = 0;
		case 9:
			errorCode = 40007327;
			break;

		default:
			errorCode = 0;
			break;
		}
		return errorCode;

	}
	public static String getCurrencyForAccount(String accountNo) {
		String currencyCode = UtilityFinderMethods.getAccountDetails(accountNo).getF_ISOCURRENCYCODE();
		return currencyCode;
	}

}	
