package com.misys.ub.dc.restServices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import bf.com.misys.cbs.services.CalculatePaymentChargeRq;
import bf.com.misys.cbs.services.CalculatePaymentChargeRs;
import bf.com.misys.cbs.types.events.Event;

import com.misys.bankfusion.commonutils.GetCBSConfigLocation;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.paymentmessaging.fic.microservices.InterBankPmntMicroService;
import com.misys.bankfusion.paymentmessaging.fic.microservices.PaymentInstructionMicroService;
import com.misys.fbp.common.util.FBPService;
import com.misys.ub.dc.common.InterbankSOAccountValidation;
import com.misys.ub.dc.common.InterbankSOChargeValidation;
import com.misys.ub.dc.types.Amount;
import com.misys.ub.dc.types.InterbankSORq;
import com.misys.ub.dc.types.InterbankSORs;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

@FBPService(serviceId = "InterbankSO", applicationId = "")
@Transactional
public class InterbankSOService {

	private final static Logger LOGGER = Logger.getLogger(InterbankSOService.class.getName());
	private final String PAYMENT_INSTRUCTION_CREATEPI_CONTROLPARAMS = "Ver=1.0; service=PaymentInstrCreate; operation =create";
	private final String SOCREATE_CONTROLPARAMS = "Ver=1.0; service=socreate; operation =create";
	public static final String PAYMENTINSTR_MICROSERVICE = "paymentInstructionMicroServiceImpl";
	public static final String INTERBANKSO_MICROSERVICE = "interBankPmntMicroServiceImpl";
	private static ApplicationContext PAYMENTS_FIC_ApplicationContext;
	private static final String PAYMENTS_FIC_CTX_FILE = "conf/business/FIC/PAYMENTS/PAYMENTSFICApplicationContext.xml";
	private static PaymentInstructionMicroService payfic ;
	private static InterBankPmntMicroService interbankMicroservice ;

	public InterbankSORs update(InterbankSORq interBankSORq) {

		InterbankSORs minterbankSORs = new InterbankSORs();
		
		boolean isSOForecast = interBankSORq.getMsgType().equals(InterbankSOConstants.INTERBANK_SO_FORECAST);
		boolean isSOCreate = false;
		isSOCreate = interBankSORq.getMsgType().equals(InterbankSOConstants.INTERBANK_SO_CREATION);
		Boolean forecastPassed = false;
		
		InterbankSOAccountValidation accountValidation = new InterbankSOAccountValidation();
		InterbankSOChargeValidation chargeValidation = new InterbankSOChargeValidation();
		
		forecastPassed = accountValidation.validate(interBankSORq);
		forecastPassed = chargeValidation.validateAccountForChargeAmount(interBankSORq);
		
		// only for charge calculation
		CalculatePaymentChargeRq chargeRq = chargeValidation.getCalculatePaymentChargeRq(interBankSORq);
		CalculatePaymentChargeRs chargeRs = chargeValidation.getChargeRs(chargeRq);
	
		if(isSOForecast && forecastPassed){
				minterbankSORs = setForecastRs(chargeRs, interBankSORq.getMsgId());
				return minterbankSORs;
		}
		
		String createPIrequestParam = getPaymentInstructionRequestParams(interBankSORq,chargeRs);	
		payfic = (PaymentInstructionMicroService) getBean(PAYMENTINSTR_MICROSERVICE);
		interbankMicroservice = (InterBankPmntMicroService) getBean(INTERBANKSO_MICROSERVICE);
		if(payfic==null|| interbankMicroservice == null){
			raiseEvent("40002028", new String[]{ PAYMENTINSTR_MICROSERVICE +" AND/OR "+ INTERBANKSO_MICROSERVICE });
		}
		String paymentInstructionId = getPaymentInstructionId(PAYMENT_INSTRUCTION_CREATEPI_CONTROLPARAMS, createPIrequestParam, payfic);
		String soRequestParams = getSoRequestParams(interBankSORq, paymentInstructionId);
		String finalResponse = getSOResponse(SOCREATE_CONTROLPARAMS, soRequestParams, interbankMicroservice);
		
		if(isSOCreate){
		
			minterbankSORs = setCreateRs(chargeRs, paymentInstructionId, finalResponse, interBankSORq.getMsgId());
			return minterbankSORs;
		}
		
		if(!isSOCreate && !isSOForecast){
			raiseEvent("40209126", null);
		}
		
		return minterbankSORs;
	}

	private InterbankSORs setForecastRs(CalculatePaymentChargeRs chargeRs, String msgId) {
		InterbankSORs forecastSoRs = new InterbankSORs();
		Amount charges = new Amount();
		Amount tax = new Amount();
		charges.setCurrency(chargeRs.getChargeAmount().getIsoCurrencyCode());
		charges.setValue(chargeRs.getChargeAmount().getAmount());
		tax.setCurrency(chargeRs.getChargeAmount().getIsoCurrencyCode());
		tax.setValue(chargeRs.getChargeAmount().getAmount());
		
		forecastSoRs.setStatus("S");
		forecastSoRs.setMsgType("IBSOF");
		forecastSoRs.setCharges(charges);
		forecastSoRs.setTax(tax);
		forecastSoRs.setInstructionId("NA");
		forecastSoRs.setSoId("NA");
		forecastSoRs.setMsgId(msgId);

		return forecastSoRs;
	}

	private InterbankSORs setCreateRs(CalculatePaymentChargeRs chargeRs, String instructionId, String SoId, String msgId) {
		Amount charges = new Amount();
		Amount tax = new Amount();
		InterbankSORs forecastSoRs = new InterbankSORs();
		charges.setCurrency(chargeRs.getChargeAmount().getIsoCurrencyCode());
		charges.setValue(chargeRs.getChargeAmount().getAmount());
		tax.setCurrency(chargeRs.getChargeTaxAmount().getIsoCurrencyCode());
		tax.setValue(chargeRs.getChargeTaxAmount().getAmount());
		
		forecastSoRs.setStatus("S");
		forecastSoRs.setMsgType("IBSOC");
		forecastSoRs.setMsgId(msgId);
		forecastSoRs.setCharges(charges);
		forecastSoRs.setTax(tax);
		forecastSoRs.setInstructionId(instructionId);
		forecastSoRs.setSoId(SoId);

		return forecastSoRs;
	}

	private String getSOResponse(String soCreateparams, String soRequestParams, InterBankPmntMicroService pmntMicroService) {
		
		LOGGER.info("Calling Standing Order MicroService" + this.getClass());
		
		String response = pmntMicroService.create(soCreateparams, soRequestParams);
		
		String standingOrderId = "";
		if (response != null) {
			checkForErrorResponse(response);
			String[] responseArray = response.split(InterbankSOConstants.ITEM_SEPARTOR);
			for (int i = 0; i < responseArray.length; i++) {
				if (responseArray[i].trim().startsWith("Standing Order ID", 0)) {
					standingOrderId = responseArray[i].split(InterbankSOConstants.KEY_VALUE_SEPARATOR)[1].trim();		
				}
			}
		}
		
		return standingOrderId;
	}

	private String getPaymentInstructionId(String controlParam, String requestParam, PaymentInstructionMicroService paymentInstructionMicroService) {

		String paymentInstructionId = "";
		
		LOGGER.info("Calling Payment Instruction MicroService" + this.getClass());

		String response = paymentInstructionMicroService.create(controlParam, requestParam);
		
		LOGGER.info("\n\n" + paymentInstructionId + "\n\n");
		
		if (response != null) {
			checkForErrorResponse(response);
			String[] responseArray = response.split(InterbankSOConstants.ITEM_SEPARTOR);
			for (int i = 0; i < responseArray.length; i++) {
				if (responseArray[i].trim().startsWith("PaymentInstrcutionId", 0)) {
					paymentInstructionId = responseArray[i].split(InterbankSOConstants.KEY_VALUE_SEPARATOR)[1].trim();		
				}
			}
		}
		return paymentInstructionId;
	}

	private void checkForErrorResponse(String response) {
		String errorCode = "";
		if(response.contains("EventCode")){
			LOGGER.info("ERROR occured \n \n****** "+response+"********\n \n");
			String[] responseArray = response.split(";");
			for (int i = 0; i < responseArray.length; i++) {
				if (responseArray[i].trim().startsWith("EventCode", 0)) {
					errorCode = responseArray[i].split("=")[1].trim();		
				}
			}
		raiseEvent(errorCode, null);
	  }
		
	}

	private Object getBean(String beanId) {
		loadApplicationContext();
		if(PAYMENTS_FIC_ApplicationContext==null){
			return null;			
		}
		return PAYMENTS_FIC_ApplicationContext.getBean(beanId);
	}

	private void loadApplicationContext() {
		if (PAYMENTS_FIC_ApplicationContext == null) {

			String cbsConfPath = GetCBSConfigLocation.getCBSConfigLocation();
			try {
				PAYMENTS_FIC_ApplicationContext = new FileSystemXmlApplicationContext("file:" + cbsConfPath.concat(PAYMENTS_FIC_CTX_FILE));
			} catch (BeansException e) {
				LOGGER.warning("ERROR: Could not find PAYMENTINSTRFICApplicationContext.xml at " + cbsConfPath);
			}
		}
	}
	
	private String getPaymentInstructionRequestParams(InterbankSORq interbankSORq, CalculatePaymentChargeRs chargeRs ){
		StringBuilder uglyFormat = new StringBuilder("");
		Amount charges = new Amount();
		Amount tax = new Amount();
		charges.setCurrency(chargeRs.getChargeAmount().getIsoCurrencyCode());
		charges.setValue(chargeRs.getChargeAmount().getAmount());
		tax.setCurrency(chargeRs.getChargeAmount().getIsoCurrencyCode());
		tax.setValue(chargeRs.getChargeAmount().getAmount());
		Map debitAccDetails = null;
		debitAccDetails = UB_IBI_PaymentsHelper.getAccountDetails(interbankSORq.getCustAcctNo());
		String debitAccBranchSortCode = (String) debitAccDetails.get("BRANCHSORTCODE");
		Connection connection = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        String debtorBankIdentifier = CommonConstants.EMPTY_STRING;
        // To get BICCODE from Branch Code
        try {
            preparedStatement = connection.prepareStatement("SELECT MPBANKIDENTIFIER FROM MPTB_PAYMENBANKIDENTIFIERCFG WHERE MPBRANCHCODE = ? AND MPPAYMENTNETWORK = 'STEP2'");
            preparedStatement.setString(1, debitAccBranchSortCode);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
            	debtorBankIdentifier = rs.getString(1);
            }
        }
        catch (SQLException e) {
        	LOGGER.warning(e.getMessage());
        }finally {
        	if (rs != null)
        		try {  rs.close();}
        	catch (SQLException sqlException) {
        		LOGGER.warning(sqlException.getLocalizedMessage());
        	}
        	if (preparedStatement != null) {
        		try {preparedStatement.close();}
        		catch (SQLException sqlException) {
        			LOGGER.warning(sqlException.getLocalizedMessage());
        		}
        	}
        }
        if("".equals(charges.getCurrency().trim())){
        	charges.setCurrency(interbankSORq.getCurrency());
        }
        if("0".equals(charges.getValue().toString())){
        	uglyFormat.append(addValue(InterbankSOConstants.CHARGEAMT, "0.00"));
        	uglyFormat.append(addValue(InterbankSOConstants.TAXAMT, "0.00"));
        } else {        	
        	uglyFormat.append(addValue(InterbankSOConstants.CHARGEAMT, charges.getValue().toString()));
        	uglyFormat.append(addValue(InterbankSOConstants.TAXAMT, tax.getValue().toString()));
        }
		
		uglyFormat.append(addValue(InterbankSOConstants.CUSTOMERID,interbankSORq.getCustomerId()));
		uglyFormat.append(addValue(InterbankSOConstants.PAYSYS, interbankSORq.getPaySys()));
		uglyFormat.append(addValue(InterbankSOConstants.PAYTYPE, interbankSORq.getPayType()));
		uglyFormat.append(addValue(InterbankSOConstants.DESTINATIONCOUNTRY, interbankSORq.getCdtrPstlAdrCtry()));
		uglyFormat.append(addValue(InterbankSOConstants.AMOUNT, interbankSORq.getAmount()));
		uglyFormat.append(addValue(InterbankSOConstants.CURRENCY, interbankSORq.getCurrency()));
		uglyFormat.append(addValue(InterbankSOConstants.DEBTORCUSTREF, interbankSORq.getDebtorCustRef()));
		uglyFormat.append(addValue(InterbankSOConstants.DEBTORACCOUNTTYPE, interbankSORq.getDebtorAccountType()));
		uglyFormat.append(addValue(InterbankSOConstants.DEBTORACCOUNT, interbankSORq.getCustAcctNo()));
		uglyFormat.append(addValue(InterbankSOConstants.DebtorPriority, interbankSORq.getDebtorPriority()));
		uglyFormat.append(addValue(InterbankSOConstants.DebtorRemittanceInfo, interbankSORq.getDebtorRemittanceInfoCtgyPurp()));
		uglyFormat.append(addValue(InterbankSOConstants.DebtorRemittanceInfoCtgyPurp, interbankSORq.getDebtorRemittanceInfoCtgyPurp()));
		uglyFormat.append(addValue(InterbankSOConstants.DEBTORBANKIDENTIFIER, debtorBankIdentifier));
		uglyFormat.append(addValue(InterbankSOConstants.DebtorBankName, interbankSORq.getDebtorBankName()));
		uglyFormat.append(addValue(InterbankSOConstants.DebtorBranchAddress, interbankSORq.getDebtorBranchAddress()));
		uglyFormat.append(addValue(InterbankSOConstants.CDTRNAME, interbankSORq.getCdtrName()));
		uglyFormat.append(addValue(InterbankSOConstants.CDTRACCOUNTFORMATTYPE, interbankSORq.getCdtrAccountFormatType()));
		uglyFormat.append(addValue(InterbankSOConstants.CDTRACCOUNT, interbankSORq.getCdtrAccount()));
		uglyFormat.append(addValue(InterbankSOConstants.CDTRPSTLADRADRTP, interbankSORq.getCdtrPstlAdrAdrTp()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrBldgNb, interbankSORq.getCdtrPstlAdrBldgNb()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrStrtNm, interbankSORq.getCdtrPstlAdrStrtNm()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrSubDept, interbankSORq.getCdtrPstlAdrSubDept()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrDept, interbankSORq.getCdtrPstlAdrStrtNm()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrPostCd, interbankSORq.getCdtrPstlAdrPostCd()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrTownNm, interbankSORq.getCdtrPstlAdrTownNm()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrCtrySubDvsn, interbankSORq.getCdtrPstlAdrCtrySubDvsn()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrCtry, interbankSORq.getCdtrPstlAdrCtry()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrPstlAdrAdrLine, interbankSORq.getCdtrPstlAdrAdrLine()));
		uglyFormat.append(addValue(InterbankSOConstants.CDTRBANKIDENTIFIER, interbankSORq.getCdtrBankIdentifier()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrBankName, interbankSORq.getCdtrBankName()));
		uglyFormat.append(addValue(InterbankSOConstants.CdtrBranchAddress, interbankSORq.getCdtrBranchAddress()));
		uglyFormat.append(addValue(InterbankSOConstants.ModuleID, interbankSORq.getModuleID()));
		uglyFormat.append(addValue(InterbankSOConstants.ModuleUniqueReference, interbankSORq.getModuleUniqueReference()));
		uglyFormat.append(addValue(InterbankSOConstants.Chargeswaived, interbankSORq.getChargeswaived()));
		uglyFormat.append(addValue(InterbankSOConstants.CHARGESCCY, charges.getCurrency()));
		uglyFormat.append(addValue(InterbankSOConstants.TAXCCY, charges.getCurrency()));
		
		LOGGER.info("Payment Instruction Creation Request: "+uglyFormat.toString());
		return uglyFormat.toString();
	}
	
	private String getSoRequestParams(InterbankSORq interbankSORq, String instructionId){
		
		StringBuilder uglyFormat = new StringBuilder("");
		uglyFormat.append(addValue(InterbankSOConstants.CUSTOMERID, interbankSORq.getCustomerId()));
		uglyFormat.append(addValue(InterbankSOConstants.CustAcctType, "1"));
		uglyFormat.append(addValue(InterbankSOConstants.CustAcctNo, interbankSORq.getCustAcctNo()));
		uglyFormat.append(addValue(InterbankSOConstants.Amt, interbankSORq.getAmount()));
		uglyFormat.append(addValue(InterbankSOConstants.Ccy, interbankSORq.getCurrency()));
		uglyFormat.append(addValue(InterbankSOConstants.Freq, interbankSORq.getFreq()));
		uglyFormat.append(addValue(InterbankSOConstants.EndOfMonth, interbankSORq.getEndOfMonth()));
		uglyFormat.append(addValue(InterbankSOConstants.NumPayments, interbankSORq.getNumPayments()));
		uglyFormat.append(addValue(InterbankSOConstants.FrDt, interbankSORq.getFrDt()));
		uglyFormat.append(addValue(InterbankSOConstants.ToDt, interbankSORq.getToDt()));
		uglyFormat.append(addValue(InterbankSOConstants.Time, interbankSORq.getTime()));
		uglyFormat.append(addValue(InterbankSOConstants.HolidayTreatment, interbankSORq.getHolidayTreatment()));
		uglyFormat.append(addValue(InterbankSOConstants.Retry, interbankSORq.getRetry()));
		uglyFormat.append(addValue(InterbankSOConstants.RetryCount, interbankSORq.getRetryCount()));
		uglyFormat.append(addValue(InterbankSOConstants.SuspendOnFailure, interbankSORq.getSuspendOnFailure()));
		uglyFormat.append(addValue(InterbankSOConstants.NumberOfFailures, interbankSORq.getNumberOfFailures()));
		uglyFormat.append(addValue(InterbankSOConstants.Description, interbankSORq.getDescription()));
		uglyFormat.append(addValue(InterbankSOConstants.InstructionId, instructionId));
		uglyFormat.append(addValue(InterbankSOConstants.Setup, interbankSORq.getSetUp()));
		uglyFormat.append(addValue(InterbankSOConstants.Success, interbankSORq.getSuccess()));
		uglyFormat.append(addValue(InterbankSOConstants.Failure, interbankSORq.getFailure()));
		uglyFormat.append(addValue(InterbankSOConstants.ChannelID, interbankSORq.getChannelId()));
		
		LOGGER.info("InterbankSO Request: "+ uglyFormat.toString());
		return uglyFormat.toString();
	}

	private String addValue(String key, String value) {

		if(value == null){
			return "";
		}
				
		return new StringBuilder(key).append(InterbankSOConstants.KEY_VALUE_SEPARATOR).append("\"").append(value).append("\"").append(InterbankSOConstants.ITEM_SEPARTOR).toString();
	}
	
	public void raiseEvent(String errorCode, String[] args) {
    	Integer eventNumber = Integer.parseInt(errorCode);
        if (args == null) {
            args = new String[] { CommonConstants.EMPTY_STRING };
        }
        Event event = new Event();
        event.setEventNumber(eventNumber);
        event.setMessageArguments(args);
        IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance().getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
        businessEventsService.handleEvent(event);
   }
}