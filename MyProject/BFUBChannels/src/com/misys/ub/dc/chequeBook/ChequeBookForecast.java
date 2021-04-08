package com.misys.ub.dc.chequeBook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.msgs.v1r0.ReadProductSummaryDtlsRq;
import bf.com.misys.cbs.msgs.v1r0.ReadProductSummaryDtlsRs;
import bf.com.misys.cbs.services.CalcEventChargeRq;
import bf.com.misys.cbs.services.CalcEventChargeRs;
import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.EventChgInputDtls;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.ReadProductSummaryDtlsReq;
import bf.com.misys.cbs.types.dc.ChqBookForecastRes;
import bf.com.misys.cbs.types.dc.ChqBookForecastRs;
import bf.com.misys.cbs.types.dc.ChqBookRq;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.serviceinvocation.IUserExitInvokerService;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOChequeBookType;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_ACCTMANDATE;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_ChequeBookForecast;
import com.trapedza.bankfusion.steps.refimpl.IUB_IBI_ChequeBookForecast;

public class ChequeBookForecast extends AbstractUB_IBI_ChequeBookForecast
		implements IUB_IBI_ChequeBookForecast {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5450336896225810355L;

	private transient final static Log logger = LogFactory
			.getLog(ChequeBookForecast.class.getName());

	private IPersistenceObjectsFactory factory = BankFusionThreadLocal
			.getPersistanceFactory();

	final String MODULEID = "TAX";
	final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";

	public ChequeBookForecast(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		ChqBookRq chqBookRq = getF_IN_ChqBookRq();
		String accID = chqBookRq.getChqBookReq().getAccountID();
		
		String formattedMessage;


		ArrayList param = new ArrayList();
		param.add(accID);
		IBOAccount mainAccount = (IBOAccount) factory.findByPrimaryKey(
				IBOAccount.BONAME,IBOAccount.ACCOUNTID);

		if (mainAccount == null) {
			formattedMessage = BankFusionMessages.getInstance()
					.getFormattedEventMessage(
							Integer.parseInt("40410029"),
							null,
							BankFusionThreadLocal.getUserSession()
									.getUserLocale(), true);
			String BalanceCheck = "false";
			setErrorResponse("40410029", formattedMessage, "", BalanceCheck);
			return;
		}
		 ArrayList<String> param1 = new ArrayList<String>();
	        
	 		param1.add(accID);
	 		List<IBOUB_CNF_ACCTMANDATE> jointAcct = (List<IBOUB_CNF_ACCTMANDATE>) factory.findByQuery(IBOUB_CNF_ACCTMANDATE.BONAME, "where " + IBOUB_CNF_ACCTMANDATE.UBACCOUNTID + " =  ? ", param, null);
	 		
	 		ArrayList<String> acctholders = new ArrayList<String>();
	 		for(IBOUB_CNF_ACCTMANDATE rec: jointAcct){
	 			acctholders.add(rec.getF_UBCUSTOMERCODE());
	 		}
	 		
			String custID = chqBookRq.getChqBookReq().getCustomerId();
	        String customerID = mainAccount.getF_CUSTOMERCODE();
	        Boolean isJointCust = false;
	        
	        for(String jointcust: acctholders){
	    		if(jointcust.equalsIgnoreCase(custID))
	    		{isJointCust = true;
	    		break;}
	    		}
	    			
	    	
	        
	        if(!customerID.equalsIgnoreCase(custID) && !isJointCust){
	        
			formattedMessage = BankFusionMessages.getInstance()
					.getFormattedEventMessage(
							Integer.parseInt("40209136"),
							null,
							BankFusionThreadLocal.getUserSession()
									.getUserLocale(), true);
			setErrorResponse("40209136", formattedMessage, "", "false");
			return;
		}
		
		boolean accountAndCustomerStatus = validateAccountAndCustomer(
				mainAccount, custID);
		if (accountAndCustomerStatus == false) {
			return;
		}

		boolean typeAndNumberOfLeaves = validateChequeBookType(mainAccount, env);
		if (typeAndNumberOfLeaves == false) {
			return;
		}

		param.clear();
		param.add(mainAccount.getF_PRODUCTID());
		param.add(getF_IN_ChqBookRq().getChqBookReq().getChequeBookType());
		param.add(getF_IN_ChqBookRq().getChqBookReq().getNumberOfLeaves());
		IBOChequeBookType chequeBookType = (IBOChequeBookType) factory
				.findFirstByQuery(IBOChequeBookType.BONAME, "where "
						+ IBOChequeBookType.CHEQUETYPEID + "= ? and "
						+ IBOChequeBookType.CHEQUETYPECODE + "= ? and "
						+ IBOChequeBookType.NUMBEROFLEAVES + "= ?", param);
		if (chequeBookType == null) {
			formattedMessage = BankFusionMessages.getInstance()
					.getFormattedEventMessage(
							Integer.parseInt("40421573"),
							null,
							BankFusionThreadLocal.getUserSession()
									.getUserLocale(), true);
			setErrorResponse("40421573", formattedMessage, "", "false");
			return;
		}

		ReadProductSummaryDtlsRs productResponse = readProductSummaryDetails(mainAccount
				.getF_PRODUCTCONTEXTCODE());

		CalcEventChargeRs charges = null;
		charges = calculateCharge(chqBookRq, productResponse);
		Currency charge = new Currency();
		Currency tax = new Currency();

		if (!charges.getChargeResults().getIsWaived()
				&& charges.getChargeResults().getNoOfChgTaxAmtDetails() > 0) {
			charge.setAmount(charges.getChargeResults()
					.getTotalChgTaxAmtTxnCur().getAmount());
			charge.setIsoCurrencyCode(charges.getChargeResults()
					.getTotalChgAmtTxnCur().getIsoCurrencyCode());
		} else {
			charge.setAmount(BigDecimal.ZERO);
			charge.setIsoCurrencyCode(mainAccount.getF_ISOCURRENCYCODE());
		}
		BigDecimal finalTax = CommonConstants.BIGDECIMAL_ZERO;
		if (getModuleConfigValue("IS_CHQ_TAX_PER_LEAF")
				.equalsIgnoreCase("true")) {
			String taxCurrency = getModuleConfigValue("CHQ_TAX_CURRENCY");
			String taxTransactionCode = getModuleConfigValue("CHQ_TAX_TRANSACTION_CODE");
			finalTax = calcTax(chequeBookType, taxCurrency, taxTransactionCode,
					mainAccount.getF_ISOCURRENCYCODE(), env);

		}
		tax.setAmount(finalTax);
		tax.setIsoCurrencyCode(mainAccount.getF_ISOCURRENCYCODE());

		boolean validateAccount = validateAccountForAmount(mainAccount, env,
				charges.getChargeResults()
				.getTotalChgTaxAmtTxnCur().getAmount(), finalTax);
		if (validateAccount == false) {
			return;
		}
		
		IUserExitInvokerService userExitInvokerService = (IUserExitInvokerService) ServiceManagerFactory.getInstance()

                .getServiceManager().getServiceForName(IUserExitInvokerService.SERVICE_NAME);

        userExitInvokerService.invokeService("chequeBookForecastAndIssue", getF_IN_ChqBookRq());

		setChqBookRs(charge, tax);

	}

	private BigDecimal calcTax(IBOChequeBookType chequeBookType,
			String taxCurrency, String taxTransactionCode,
			String f_ISOCURRENCYCODE, BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		BigDecimal finaltax = CommonConstants.BIGDECIMAL_ZERO;
		if (!taxCurrency.equalsIgnoreCase(f_ISOCURRENCYCODE)) {
			IBusinessInformation bizInformation = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
          .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
      IBOMisTransactionCodes transactionCode =
          (IBOMisTransactionCodes) bizInformation.getMisTransactionCodeDetails(taxTransactionCode);

			String exchangeRateType = transactionCode.getF_EXCHANGERATETYPE();
			Double taxPerLeaf = chequeBookType.getF_TAXPERLEAF().doubleValue();
			Double totTax = taxPerLeaf * chequeBookType.getF_NUMBEROFLEAVES();
			BigDecimal totalTax = new BigDecimal(totTax);
			BigDecimal exchangeRate = computeExchangeRate(taxCurrency,
					f_ISOCURRENCYCODE, exchangeRateType, totalTax, env);
			finaltax = calculateExchRateAmt(taxCurrency, f_ISOCURRENCYCODE,
					exchangeRate, totalTax);

			return finaltax;
		} else {
			Double taxPerLeaf = chequeBookType.getF_TAXPERLEAF().doubleValue();
			Double totTax = taxPerLeaf * chequeBookType.getF_NUMBEROFLEAVES();
			finaltax = new BigDecimal(totTax);
			return finaltax;
		}

	}

	private void setChqBookRs(Currency charge, Currency tax) {
		ChqBookForecastRs chqBookForecastRs = new ChqBookForecastRs();
		ChqBookForecastRes chqBookForecastRes = new ChqBookForecastRes();
		RsHeader rsHeader = new RsHeader();
		MessageStatus messageStatus = new MessageStatus();
		SubCode subCode = new SubCode();
		subCode.setDescription("");
		messageStatus.addCodes(subCode);
		messageStatus.setOverallStatus("S");
		rsHeader.setStatus(messageStatus);
		rsHeader.setOrigCtxtId(getF_IN_ChqBookRq().getRqHeader().getOrig()
				.getChannelId());
		chqBookForecastRs.setRsHeader(rsHeader);
		chqBookForecastRes.setAccountStatus("Normal");
		chqBookForecastRes.setBalanceCheckStatus("true");
		chqBookForecastRes.setCharge(charge);
		chqBookForecastRes.setTax(tax);
		chqBookForecastRs.setChqBookForecastRes(chqBookForecastRes);
		setF_OUT_ChqBookForecastRs(chqBookForecastRs);

	}

	private boolean validateAccountForAmount(IBOAccount mainAccount,
			BankFusionEnvironment env, BigDecimal finalCharge, BigDecimal finalTax) {

		HashMap<String, Object> inputparam = new HashMap<String, Object>();
		HashMap<String, Object> outputparam = new HashMap<String, Object>();

		String BalanceCheck = "false";
		String formattedMessage;

		inputparam.put("AccountID", mainAccount.getBoID());

		outputparam = MFExecuter.executeMF("GetAvailableBalance", env,
				inputparam);

		BigDecimal availbleBalance = (BigDecimal) outputparam
				.get("AvailableBalance");

		finalCharge = finalCharge.add(finalTax);
		if (availbleBalance.compareTo(finalCharge) != -1) // available Balance <= eventCharge
		{
			BalanceCheck = "true";
			return true;
		} else {

			formattedMessage = BankFusionMessages.getInstance()
					.getFormattedEventMessage(                           //insufficient available balance
							Integer.parseInt("40009269"),
							null,
							BankFusionThreadLocal.getUserSession()
									.getUserLocale(), true);
			setErrorResponse("40009269", formattedMessage, "", BalanceCheck);
			return false;
		}

	}

	private BigDecimal computeExchangeRate(String fromCurrency,
			String toCurrency, String rateType, BigDecimal amount,
			BankFusionEnvironment env) {
		BigDecimal exchangeRate = null;
		try {
			IBusinessInformation bizInformation = ((IBusinessInformationService) ServiceManagerFactory
					.getInstance()
					.getServiceManager()
					.getServiceForName(
							BusinessInformationService.BUSINESS_INFORMATION_SERVICE))
					.getBizInfo();
			exchangeRate = bizInformation.getExchangeRateDetail(fromCurrency,
					toCurrency, rateType, amount, env);

		} catch (BankFusionException bankFusionException) {
			logger.info(bankFusionException);
		}

		return exchangeRate;
	}

	private BigDecimal calculateExchRateAmt(String buyCurrency,
			String sellCurrency, BigDecimal exchangeRate, BigDecimal buyAmount) {

		RqHeader rqHeader = new RqHeader();
		Orig orig = new Orig();
		orig.setChannelId("SWIFT");
		rqHeader.setOrig(orig);
		CalcExchangeRateRq exchRq = new CalcExchangeRateRq();
		CalcExchRateDetails exchangeDtls = new CalcExchRateDetails();
		exchangeDtls.setSellAmount(BigDecimal.ZERO);
		if (buyAmount.signum() < 0) {
			exchangeDtls.setBuyAmount(buyAmount.abs());
		} else {
			exchangeDtls.setBuyAmount(buyAmount);
		}

		exchangeDtls.setBuyCurrency(buyCurrency);
		exchangeDtls.setSellCurrency(sellCurrency);
		exchRq.setCalcExchRateDetails(exchangeDtls);
		ExchangeRateDetails exchangeRateDetails = new ExchangeRateDetails();
		exchangeRateDetails.setExchangeRate(exchangeRate);
		exchangeRateDetails.setExchangeRateType("SPOT");
		exchangeDtls.setExchangeRateDetails(exchangeRateDetails);
		exchRq.setRqHeader(rqHeader);
		BankFusionEnvironment env = new BankFusionEnvironment(null);
		HashMap inputMap = new HashMap();
		inputMap.put("CalcExchangeRateRq", exchRq);
		env.setData(new HashMap());
		HashMap outputParams = MFExecuter.executeMF(
				"CB_FEX_CalculateExchangeRateAmount_SRV", env, inputMap);

		CalcExchangeRateRs calcExchangeRateRs = (CalcExchangeRateRs) outputParams
				.get("CalcExchangeRateRs");
		BigDecimal equivalentAmount = calcExchangeRateRs
				.getCalcExchRateResults().getSellAmountDetails().getAmount();
		if (buyAmount.signum() < 0) {
			equivalentAmount = BigDecimal.ZERO.subtract(equivalentAmount);
		}
		return equivalentAmount;
	}

	private boolean validateChequeBookType(IBOAccount mainAccount,
			BankFusionEnvironment env) {

		HashMap<String, Object> inputparam = new HashMap<String, Object>();
		HashMap<String, Object> outputparam = new HashMap<String, Object>();
		inputparam.put("ACCOUNTID", mainAccount.getBoID());

		String formattedMessage;
		String BalanceCheck = "false";


		outputparam = MFExecuter.executeMF("UB_IBI_ValChqBookTypeToAcc_SRV",env, inputparam);

		Integer errorCode = (Integer) outputparam.get("EventCode");
		if (errorCode != 0) {
			formattedMessage = BankFusionMessages.getInstance()
					.getFormattedEventMessage(
							errorCode,
							null,
							BankFusionThreadLocal.getUserSession()
									.getUserLocale(), true);
			setErrorResponse(errorCode.toString(), formattedMessage, "",
					BalanceCheck);
			return false;
		}
		return true;
	}

	private boolean validateAccountAndCustomer(IBOAccount mainAccount,
			String customerId) {

		Boolean isClosed = mainAccount.isF_CLOSED();
		Boolean isStopped = mainAccount.isF_STOPPED();
		Boolean isDormant = mainAccount.isF_DORMANTSTATUS();
		String errorCode;
		String status;
		String formattedMessage;
		Object[] params = { mainAccount.getBoID() };
		if (isClosed == true || isStopped == true || isDormant == true) {
			if (isClosed) {
				errorCode = "40407566";
				status = "Closed";
			} else if (isStopped) {
				errorCode = "40400055";
				status = "Stopped";
			} else {
				errorCode = "40400057";
				status = "Dormant";
			}
			formattedMessage = BankFusionMessages.getInstance()
					.getFormattedEventMessage(
							Integer.parseInt(errorCode),
							params,
							BankFusionThreadLocal.getUserSession()
									.getUserLocale(), true);
			String BalanceCheck = "false";
			setErrorResponse(errorCode, formattedMessage, status, BalanceCheck);
			return false;
		}
		ArrayList param = new ArrayList();
		param.clear();
		param.add(customerId);
		IBOCustomer customer = (IBOCustomer) factory.findByPrimaryKey(
				IBOCustomer.BONAME,IBOCustomer.CUSTOMERCODE);
		if (customer != null) {
			String customerStatus = customer.getF_CUSTOMERSTATUS();
			if (!customerStatus.equals("001")) {
				errorCode = "40411056";
				status = "";
				formattedMessage = BankFusionMessages.getInstance()
						.getFormattedEventMessage(
								Integer.parseInt(errorCode),
								params,
								BankFusionThreadLocal.getUserSession()
										.getUserLocale(), true);
				String BalanceCheck = "false";
				setErrorResponse(errorCode, formattedMessage, status,
						BalanceCheck);
				return false;
			}
			return true;
		}
	else {
		errorCode = "40401080";
		status = "";
		formattedMessage = BankFusionMessages.getInstance()
				.getFormattedEventMessage(
						Integer.parseInt(errorCode),
						params,
						BankFusionThreadLocal.getUserSession()
								.getUserLocale(), true);
		String BalanceCheck = "false";
		setErrorResponse(errorCode, formattedMessage, status, BalanceCheck);
		return false;
		}
	}

	@SuppressWarnings("unchecked")
	private ReadProductSummaryDtlsRs readProductSummaryDetails(String productId) {
		HashMap<String, Object> MfParams = new HashMap<String, Object>();
		ReadProductSummaryDtlsRq readProductSummaryDtlsRq = new ReadProductSummaryDtlsRq();
		ReadProductSummaryDtlsReq readProductSummaryDtlsReq = new ReadProductSummaryDtlsReq();
		readProductSummaryDtlsReq.setProductID(productId);
		readProductSummaryDtlsRq
				.setReadProductSummaryDtlsReq(readProductSummaryDtlsReq);
		MfParams.put("readProductSummaryDtlsRq", readProductSummaryDtlsRq);
		HashMap outputParams = MFExecuter.executeMF(
				"CB_PRD_ReadProductSummaryDetails_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), MfParams);
		ReadProductSummaryDtlsRs response = (ReadProductSummaryDtlsRs) outputParams
				.get("readProductSummaryDtlsRs");
		return response;
	}

	private CalcEventChargeRs calculateCharge(ChqBookRq chqBookRq,
			ReadProductSummaryDtlsRs productResponse) {
		CalcEventChargeRq claceEventChargeRq = new CalcEventChargeRq();
		EventChgInputDtls eventChgInputDtls = new EventChgInputDtls();
		AccountKeys accountKeys = new AccountKeys();
		InputAccount inputAcct = new InputAccount();
		inputAcct.setInputAccountId(chqBookRq.getChqBookReq().getAccountID());
		accountKeys.setStandardAccountId(chqBookRq.getChqBookReq()
				.getAccountID());
		accountKeys.setInputAccount(inputAcct);
		eventChgInputDtls.setAccountId(accountKeys);
		eventChgInputDtls.setEventCategory("CORE");
		eventChgInputDtls.setEventSubCategory("40112127");
		eventChgInputDtls.setProductCategory(productResponse
				.getReadProductSummaryDtlsRes().getProductSummaryDtls()
				.getProductCategory());
		eventChgInputDtls.setProductId(productResponse
				.getReadProductSummaryDtlsRes().getProductSummaryDtls()
				.getProductID());
		eventChgInputDtls.setChannelId("IBI");
		eventChgInputDtls.setChgFundingAccount(accountKeys);
		claceEventChargeRq.setEventChgInputDtls(eventChgInputDtls);
		HashMap<String, Object> MfParams = new HashMap<String, Object>();
		MfParams.put("CalcEventChargeRq", claceEventChargeRq);
		HashMap outputChargeParams = MFExecuter.executeMF(
				"CB_CHG_CalculateEventCharges_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), MfParams);
		CalcEventChargeRs calcEventChargesRs = (CalcEventChargeRs) outputChargeParams
				.get("CalcEventChargesRs");
		return calcEventChargesRs;
	}

	private void setErrorResponse(String errorCode, String formattedMsg,
			String status, String balanceCheckStatus) {
	    logger.warn("CorrelationId: "+BankFusionThreadLocal.getCorrelationID());
		ChqBookForecastRs chqBookForecastRs = new ChqBookForecastRs();
		ChqBookForecastRes chqForecastRes = new ChqBookForecastRes();
		RsHeader rsHeader = new RsHeader();
		MessageStatus messageStatus = new MessageStatus();
		SubCode subCode = new SubCode();
		subCode.setCode(errorCode);
		subCode.setDescription(formattedMsg);
		messageStatus.addCodes(subCode);
		messageStatus.setOverallStatus("F");
		rsHeader.setStatus(messageStatus);
		rsHeader.setOrigCtxtId(getF_IN_ChqBookRq().getRqHeader().getOrig()
				.getChannelId());
		chqBookForecastRs.setRsHeader(rsHeader);
		chqForecastRes.setAccountStatus(status);
		chqForecastRes.setBalanceCheckStatus(balanceCheckStatus);
		Currency charge = new Currency();
		charge.setAmount(BigDecimal.ZERO);
		charge.setIsoCurrencyCode(null);
		chqForecastRes.setCharge(charge);
		chqBookForecastRs.setChqBookForecastRes(chqForecastRes);
		setF_OUT_ChqBookForecastRs(chqBookForecastRs);
	}

	


	private String getModuleConfigValue(String Value) {
		String value = "";
		HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
		ModuleKeyRq module = new ModuleKeyRq();
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		module.setModuleId(MODULEID);
		module.setKey(Value);
		read.setModuleKeyRq(module);
		moduleParams.put("ReadModuleConfigurationRq", read);
		HashMap valueFromModuleConfiguration = MFExecuter.executeMF(
				READ_MODULE_CONFIGURATION,
				BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
		if (valueFromModuleConfiguration != null) {
			ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration
					.get("ReadModuleConfigurationRs");
			value = rs.getModuleConfigDetails().getValue().toString();
		}
		return value;
	}

}
