package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.cbs.config.ModuleConfiguration;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.fbe.compliance.KYCDataCache;
import com.misys.fbe.compliance.KYCNonScreenHandler;
import com.misys.fbe.compliance.exceptions.KYCException;
import com.misys.fbe.compliance.types.FircoAckResponseType;
import com.misys.ub.batchgateway.persistence.PrivatePersistenceFactory;
import com.misys.ub.charges.ChargeConstants;
import com.misys.ub.compliance.persistence.ComplianceFinderMethods;
import com.misys.ub.dc.payment.UB_IBI_PaymentPersistence;
import com.misys.ub.forex.configuration.ForexModuleConfiguration;
import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.misys.ub.paymentManagement.util.NONSTPCheckerUtil;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_BIL_BILLERINFO;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CNF_Bank;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CNF_UtilityCompany;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOOrgDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOPersonDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTSettlementInstructionDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_IBI_PAYMENT;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_IBI_SWFTPAYMENT;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.events.ErrorEvent;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.exceptions.RetriableException;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.services.autonumber.IAutoNumberService;
import com.trapedza.bankfusion.steps.refimpl.AbstractINDPaymentPostingFatom;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.bankfusion.attributes.PagingRequest;
import bf.com.misys.cbs.msgs.v1r0.CreateSettlementInstructionsRq;
import bf.com.misys.cbs.msgs.v1r0.CreateSettlementInstructionsRs;
import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRs;
import bf.com.misys.cbs.msgs.v1r0.SearchAccountRq;
import bf.com.misys.cbs.msgs.v1r0.SearchAcctRs;
import bf.com.misys.cbs.msgs.v1r0.TransferForecastOrCreateRequest;
import bf.com.misys.cbs.msgs.v1r0.TransferResponse;
import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.AccountMandateDetails;
import bf.com.misys.cbs.types.AccountSearch;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.CreateSettlementInstructionsInputRq;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.InstructionUpdateItem;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.NameAndAddress;
import bf.com.misys.cbs.types.TransactionEvent;
import bf.com.misys.cbs.types.TransferForecastInpDetails;
import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.compliance.types.SwiftPaymentDetails;

@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public class UB_IND_PaymentPostingFatom extends AbstractINDPaymentPostingFatom {

	/**
	 *
	 */
	private static final long serialVersionUID = 5728847938564766467L;

	public UB_IND_PaymentPostingFatom(BankFusionEnvironment env) {
		super(env);
	}

	private static IBusinessInformation bizInfo;
	static {
		IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
				.getInstance().getServiceManager()
				.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
		bizInfo = ubInformationService.getBizInfo();
	}

	public static final String MODULE_CONFIG_CATEGORY_FOR_KYC = "IBI";
	public static final String MODULE_CONFIG_NAME_FOR_CUSTOMER_BLOCKING = "BLOCK_CUSTOMER_ON_AML_FAILURE";

	public static final String INCOMING_IDENTIFIER = "I";
	public static final String OUTGOING_IDENTIFIER = "O";

	public static final String MODULEID = "IBI";
	public static final String MODULEID_CCI = "CCI";
	public static final String INTERNALPYMT = "INTERNAL_MISDR";
	public static final String INTRAPYMT = "INTRA_MISDR";
	public static final String DOMESTICPYMT = "DOMESTIC_MISDR";
	public static final String FOREIGNPYMT = "FOREIGN_MISDR";
	public static final String LOANREPYMT = "LO_RPMNT_MIS_DR";
	HashMap<String, String> addressLines = new HashMap<>();
	public static final String INTERNALPYMTCR = "INTERNAL_MISCR";
	public static final String INTRAPYMTCR = "INTRA_MISCR";
	public static final String DOMESTICPYMTCR = "DOMESTIC_MISCR";
	public static final String FOREIGNPYMTCR = "FOREIGN_MISCR";
	public static final String LOANREPYMTCR = "LO_RPMNT_MIS_CR";
	Boolean error = false;
	Boolean isLoanPayment = false;
	public static final String MODULE_NAME = "FEX";
	public static final String MODULE_SYSNAME = "SYS";
	public static final String DFLNOSTRO = "NostroBranchCode";
	public static final String NOSTROACCT_CONTEXT_KEY = "DEFAULT_NOSTRO";
	public static final String DOM_PAY_SETTLEMENT_ACC = "DOM_PAY_SETTLEMENT_ACC";
	public static final String BRANCH = "BRANCH";
	public static final String CURRENCY = "CURRENCY";
	final static String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
	private String creditCurrency = CommonConstants.EMPTY_STRING;
	private String creditAccountNumber = CommonConstants.EMPTY_STRING;
	public static final String UPDATE_CHARGES_MFID = "UB_CHG_UpdateChargeHistory_SRV";
	private static final String KYC_SERVICE_MF_NAME = "UB_CNF_ReadKYCStatus_SRV";
	private transient final static Log logger = LogFactory.getLog(UB_IND_PaymentPostingFatom.class.getName());
	private String transactionID = CommonConstants.EMPTY_STRING;
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	public static String TAXAMOUNT_IN_FUND_ACC_CURRENCY = "TAXAMOUNT_IN_FUND_ACC_CURRENCY";
	String txnReference = CommonConstants.EMPTY_STRING;
	IAutoNumberService autoNumService = (IAutoNumberService) ServiceManager
			.getService(ServiceManager.AUTO_NUMBER_SERVICE);
	String context = CommonConstants.EMPTY_STRING;
	public static final String taxCode = "TAXAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode";
	public static final String chargeCode = "CHARGEAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode";
	String creditTxnCode = CommonConstants.EMPTY_STRING;
	String debitTxnCode = CommonConstants.EMPTY_STRING;
	String spotPseudonym = ForexModuleConfiguration.getSpotPositionPseudonym();
	String positionAccountContext = (String) bizInfo.getModuleConfigurationValue(IfmConstants.SYS_MODULE_CONFIG_KEY,
			IfmConstants.SYS_POSITION_CONTEXT, null);
	int retryLeft = 5;
	boolean retry;
	public static final String CCI_KYC_MODULE_NAME = "IS_KYC_CHECK_REQD_FOR_CCI";
	String dormantAccount = CommonConstants.EMPTY_STRING;
	String invalidAccount = CommonConstants.EMPTY_STRING;
	String closedOrStoppedAccount = CommonConstants.EMPTY_STRING;
	public static TransferForecastOrCreateRequest trfForeCastRq;

	// Changes for FBIT-2434: Mobile Top UP :Start
	private static final String MOB_TOP_UP_MIS_DR = "MOB_TOP_UP_MIS_DR";
	private static final String MOB_TOP_UP_MIS_CR = "MOB_TOP_UP_MIS_CR";
	private static final String FIND_BEN_FIC_ACC = "WHERE " + IBOCB_CNF_UtilityCompany.UTLCOMPDESCREF + " = ?";
	// Changes for FBIT-2434: Mobile Top UP :End

	private String responseEventParam_STPNONSTP = CommonConstants.EMPTY_STRING;

	public void process(BankFusionEnvironment env) {

		TransferForecastOrCreateRequest transferForecastOrCreateRequest = getF_IN_transferForecastOrCreateRequest();
		trfForeCastRq = getF_IN_transferForecastOrCreateRequest();
		Date transferDt = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getTransferDate();
		transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.setTransferDate(SystemInformationManager.getInstance().getBFBusinessDate());
		RqHeader rqHeader = transferForecastOrCreateRequest.getRqHeader();
		String customerId = rqHeader.getOrig().getAppId();
		String channelId = rqHeader.getOrig().getChannelId();

		logger.info(" \n\n ============ Entered into Payment Posting ============ \n" + " MessageId: "
				+ transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem()
				+ "\n ======================================================\n\n");
		isLoanPayment = false;
		IBOAccount accountDtl = getAccountCust(transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp().getFromMyAccount());
		// customer block check
		String messageId = GUIDGen.getNewGUID();
		HashMap inputMap = new HashMap();
		if (!validateTxnAmount(
				transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp().getAmount()
						.getAmount(),
				transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
						.getTransferCurrency().getIsoCurrencyCode())) {
			handleEvent(40009263, new String[] {});
		}

		String trxType = CommonConstants.EMPTY_STRING;
		String status = CommonConstants.EMPTY_STRING;

		trxType = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType();

		// Change made as part of FBIT-6900
		if (transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getTransferrecipientDtls() != null
				&& transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
						.getTransferrecipientDtls().getBeneficiaryName() != null
				&& transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
						.getTransferrecipientDtls().getBeneficiaryName().length() > 35) {
			insertMessageHeader(messageId, trxType);
			setF_OUT_transferResponse(postResponse(messageId,
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType(),
					"40430066", transferForecastOrCreateRequest, new HashMap(), customerId,
					new CreateSettlementInstructionsRs(), "", "",
					BankFusionMessages.getFormattedMessage(Integer.parseInt("40430066"), new String[] {})));
			return;
		}

		getCreditAccountNumber(transferForecastOrCreateRequest, env);
		HashMap onlineCharges = null;
		context = bizInfo.getModuleConfigurationValue(MODULE_SYSNAME, DOM_PAY_SETTLEMENT_ACC, env).toString();
		creditTxnCode = getTransactionCode(transferForecastOrCreateRequest, "credit");
		debitTxnCode = getTransactionCode(transferForecastOrCreateRequest, "debit");
		CreateSettlementInstructionsRs createSettlementInstructionRs = new CreateSettlementInstructionsRs();

		String bankName = null;
		String INDBankName = null;

		if (!trxType.equalsIgnoreCase("INTNAT")) {
			bankName = getHomeBankName(env);
			INDBankName = CommonConstants.EMPTY_STRING;
			if (transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
					.getBeneficiaryBank() != null
					&& transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
							.getBeneficiaryBank().getBankNameAndAddress() != null
					&& transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
							.getBeneficiaryBank().getBankNameAndAddress().getBankName() != null) {
				INDBankName = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
						.getTransferForecastInp().getBeneficiaryBank().getBankNameAndAddress().getBankName();
			}
		}
		if ((bankName != null && INDBankName != null && !(trxType.equalsIgnoreCase("INTNAT")))
				&& bankName.equalsIgnoreCase(INDBankName)) {
			trxType = "INTRAPYMT";
		}

		/*
		 * else { trxType =
		 * transferForecastOrCreateRequest.getTransferForecastOrCreateInput
		 * ().getTransactionalType (); }
		 */
		try {
			// Added NONSTP Check for FBPY-1925
			if (!trxType.equalsIgnoreCase("INTNAT")) {
				insertMessageHeader(messageId, trxType);
			}
		}

		catch (Exception e) {

			logger.error("Error occured during insertMessageHeader operation ", e);
		}

		inputMap.put("CustomerCode", customerId);
		logger.info("Calling UB_CNF_ReadKYCStatus_SRV - Microflow");
		HashMap outputMap = MFExecuter.executeMF(KYC_SERVICE_MF_NAME, env, inputMap);
		if ("003".equalsIgnoreCase((String) outputMap.get("UBKYCSTATUS"))) {
			setF_OUT_transferResponse(postResponse(messageId,
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType(),
					"40410028", transferForecastOrCreateRequest, new HashMap(), customerId,
					new CreateSettlementInstructionsRs(), "", "",
					BankFusionMessages.getFormattedMessage(Integer.parseInt("40410028"), new String[] {})));
			return;
		}

		do {

			try {
				onlineCharges = fetchOnlinecharges(transferForecastOrCreateRequest, env);

				VectorTable vector = (VectorTable) onlineCharges.get("RESULT");

				if (retry) {
					logger.info(" \n\n ======== Retring due to retriable exception ======== \n Retries Left : "
							+ retryLeft + "\n " + "MessageId: "
							+ transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem()
							+ "\n ======================================================\n\n");

					retryLeft--;
					retry = false;
					error = false;

				}

				String accCustomerId = accountDtl.getF_CUSTOMERCODE();
				if (customerId == null || customerId.isEmpty()) {
					customerId = accCustomerId;

					transferForecastOrCreateRequest.getRqHeader().getOrig().setAppId(accCustomerId);

				} else if (!isAccountCustomer(transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
						.getTransferForecastInp().getFromMyAccount(), customerId, channelId)) {

					handleEvent(40180221, new String[] {});

				}
				if (trxType.equalsIgnoreCase("INTNAT")) {
					if (StringUtils.isBlank(transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
							.getTransferForecastInp().getCharges())) {
						handleEvent(40311686, new String[] { "Charge Code" });
					}
					// Added NONSTP Check for FBPY-1925
					// FBPY-3109: For ChannelID "CCI/FBCC" txnref should be retained as it is
					if (transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals(MODULEID_CCI)) {
						txnReference = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
								.getTransactionalItem();
					} else {
						txnReference = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
								.getTransactionalItem()
								+ ":"
								+ autoNumService.getNextAutoRef(
										getTransactionCode(transferForecastOrCreateRequest, "debit"),
										CommonConstants.EMPTY_STRING);
					}
					BankFusionThreadLocal.getUserSession().setChannelID(channelId);
					BankFusionThreadLocal.setSourceId(channelId);
					OutwardSwtRemittanceRs outwardSwtRemittanceRs = NONSTPCheckerUtil
							.handleNonSTPMode(transferForecastOrCreateRequest, env, onlineCharges, txnReference);
					messageId = outwardSwtRemittanceRs.getOutputParams().getTransactionId();
					String endToEndReference = outwardSwtRemittanceRs.getOutputParams().getUetr();
					if (!outwardSwtRemittanceRs.getOutputParams().getIsNonStp() && outwardSwtRemittanceRs.getRsHeader()
							.getStatus().getOverallStatus().equalsIgnoreCase("S")) {
						createSettlementInstructionRs = getSSIDtls(transferForecastOrCreateRequest, env);
						// UETR set in rsHeader
						createSettlementInstructionRs.getRsHeader().setOrigCtxtId(endToEndReference);

						IBOSwtCustomerDetail swtCustDtl = (IBOSwtCustomerDetail) BankFusionThreadLocal
								.getPersistanceFactory()
								.findByPrimaryKey(IBOSwtCustomerDetail.BONAME, customerId, true);
						if (swtCustDtl == null || swtCustDtl.getF_SWTACTIVE().equals("N"))
							handleEvent(40407064, new String[] { customerId });

						if (createSettlementInstructionRs.getCreateSettlementInstructionsInputRs()
								.getCustomerID() != null
								&& !createSettlementInstructionRs.getCreateSettlementInstructionsInputRs()
										.getCustomerID().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
							verifyAndPostInternationalPosting(transferForecastOrCreateRequest, env, onlineCharges,
									customerId, createSettlementInstructionRs.getCreateSettlementInstructionsInputRs()
											.getSettInstrDtlId());
							BigDecimal debitAmount = getDebitAmount(transferForecastOrCreateRequest,
									accountDtl.getF_ISOCURRENCYCODE());
							generateSwiftMessage(transferForecastOrCreateRequest, onlineCharges, customerId,
									createSettlementInstructionRs, debitAmount);
						} else {
							if (createSettlementInstructionRs.getRsHeader() != null) {
								if (createSettlementInstructionRs.getRsHeader().getStatus() != null
										&& createSettlementInstructionRs.getRsHeader().getStatus()
												.getSubStatus() != null) {
									if (StringUtils.isNotEmpty(
											createSettlementInstructionRs.getRsHeader().getStatus().getSubStatus())) {
										handleEvent(Integer.parseInt(
												createSettlementInstructionRs.getRsHeader().getStatus().getSubStatus()),
												new String[] {});
									} else {
										error = true;
										setF_OUT_transferResponse(postResponse(messageId, trxType,
												createSettlementInstructionRs.getRsHeader().getStatus().getSubStatus()
														.toString(),
												transferForecastOrCreateRequest, onlineCharges, customerId,
												createSettlementInstructionRs, accountDtl.getBoID(),
												accountDtl.getF_ISOCURRENCYCODE()));
									}
								}
							} else {
								error = true;
								setF_OUT_transferResponse(
										postResponse(messageId, trxType, "40422013", transferForecastOrCreateRequest,
												onlineCharges, customerId, createSettlementInstructionRs,
												accountDtl.getBoID(), accountDtl.getF_ISOCURRENCYCODE()));
							}
						}
					} else {
						// Prepare Response for NON STP
						if (outwardSwtRemittanceRs.getRsHeader().getStatus().getOverallStatus().equalsIgnoreCase("S")) {
							// Else finally block will be executed, we need to restrict it for NON STP
							error = true;
							setF_OUT_transferResponse(postResponse(messageId, trxType, "MANUAL_INTERVENTION",
									transferForecastOrCreateRequest, onlineCharges, customerId,
									createSettlementInstructionRs, accountDtl.getBoID(),
									accountDtl.getF_ISOCURRENCYCODE()));
						} else {
							error = true;
							// TODO: Temporary way of handling error with parameters till we figure out a
							// better way
							responseEventParam_STPNONSTP = outwardSwtRemittanceRs.getRsHeader().getStatus().getCodes(0)
									.getParameters(0).getEventParameterValue();
							invalidAccount = responseEventParam_STPNONSTP;
							dormantAccount = responseEventParam_STPNONSTP;
							closedOrStoppedAccount = responseEventParam_STPNONSTP;
							setF_OUT_transferResponse(postResponse(messageId, trxType,
									outwardSwtRemittanceRs.getRsHeader().getStatus().getCodes(0).getCode(),
									transferForecastOrCreateRequest, onlineCharges, customerId,
									createSettlementInstructionRs, accountDtl.getBoID(),
									accountDtl.getF_ISOCURRENCYCODE()));

						}
					}
				}
				// Changes for FBIT-2434: Mobile Top UP
				else if (trxType.equalsIgnoreCase("INTERNALPYMT") || trxType.equalsIgnoreCase("INTRAPYMT")
						|| trxType.equalsIgnoreCase("INTERNALSOPYMT") || trxType.equalsIgnoreCase("MOBILETOPUP")) {

					verifyAndPostInternalOrIntraPosting(transferForecastOrCreateRequest, env, vector, customerId,
							transferDt);

				}
			} catch (CollectedEventsDialogException collectedEventsDialogException) {
				error = true;
				List<ErrorEvent> errors = collectedEventsDialogException.getErrors();
				BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
				BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				for (ErrorEvent runTimeError : errors) {
					status = String.valueOf(runTimeError.getEventNumber());
					// These event codes are same as inside isAccountPasswordProtected()
					List<Integer> eventNumbers = Arrays.asList(40007319, 40007318, 40007321, 40112172, 40007322,
							40007323, 40409356, 40007325, 40010070, 40430061);

					if (eventNumbers.contains(runTimeError.getEventNumber()) && runTimeError.getDetails() != null
							&& runTimeError.getDetails().length != 0 && runTimeError.getDetails().length >= 2) {
						setF_OUT_transferResponse(postResponse(messageId, trxType, status,
								transferForecastOrCreateRequest, onlineCharges, customerId,
								createSettlementInstructionRs, runTimeError.getDetails()[0].toString(),
								runTimeError.getDetails()[1].toString(), runTimeError.getMessage()));
					} else {
						setF_OUT_transferResponse(
								postResponse(messageId, trxType, status, transferForecastOrCreateRequest, onlineCharges,
										customerId, createSettlementInstructionRs, accountDtl.getBoID(),
										accountDtl.getF_ISOCURRENCYCODE(), runTimeError.getMessage()));
					}
				}
			} catch (KYCException e) {
				BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
				BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				error = true;
				logger.warn(" -+- Message send to Firco for KYC check POSTING on hold -=> " + customerId);
			} catch (RetriableException exception) {
				logger.warn("\n ==========================\n" + "retriable exception : Retring again after 5 sec\n"
						+ "MessageId: "
						+ transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem()
						+ "\n==========================");
				logger.error(ExceptionUtil.getExceptionAsString(exception));
				error = true;
				retry = true;

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.error("Error while delaying the thread for 5 sec ", e);

				}

				if (retryLeft == 0) {
					if (null == accountDtl) {
						invalidAccount = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
								.getTransferForecastInp().getFromMyAccount();
						setF_OUT_transferResponse(
								postResponse(messageId, trxType, "40407516", transferForecastOrCreateRequest,
										onlineCharges, customerId, createSettlementInstructionRs, null, null));

					} else {
						setF_OUT_transferResponse(
								postResponse(messageId, trxType, "40000127", transferForecastOrCreateRequest,
										onlineCharges, customerId, createSettlementInstructionRs, accountDtl.getBoID(),
										accountDtl.getF_ISOCURRENCYCODE()));

					}
				}

				BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
				BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
			} catch (Exception exception) {
				logger.warn("Some Exception : Reversing the transaction ");
				logger.error(ExceptionUtil.getExceptionAsString(exception));
				error = true;
				BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
				BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				if (null == accountDtl) {
					invalidAccount = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
							.getTransferForecastInp().getFromMyAccount();
					setF_OUT_transferResponse(postResponse(
							messageId, trxType, "40407516", transferForecastOrCreateRequest, onlineCharges, customerId,
							createSettlementInstructionRs, transferForecastOrCreateRequest
									.getTransferForecastOrCreateInput().getTransferForecastInp().getFromMyAccount(),
							null));
				} else {
					setF_OUT_transferResponse(postResponse(messageId, trxType, "40000127",
							transferForecastOrCreateRequest, onlineCharges, customerId, createSettlementInstructionRs,
							accountDtl.getBoID(), accountDtl.getF_ISOCURRENCYCODE()));
				}
			} finally {
				if (!error) {
					BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
					BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

					setF_OUT_transferResponse(postResponse(messageId, trxType, "SUCCESS",
							transferForecastOrCreateRequest, onlineCharges, customerId, createSettlementInstructionRs,
							accountDtl.getBoID(), accountDtl.getF_ISOCURRENCYCODE()));
					// Changes for FBIT-2434: Mobile Top UP : Start
					if (trxType.equalsIgnoreCase("MOBILETOPUP")) {
						String responseMsg = buildResMsg(transferForecastOrCreateRequest);
						postToServiceProviderQueue(responseMsg, "QM_UB_MOBILE_TOP_UP");
					}
					// Changes for FBIT-2434: Mobile Top UP : End
				}
				if (retry && retryLeft > 0)
					logger.info(" \n ======== Will retry ======== \n");
				else
					logger.info(" \n\n ======== Responded Back after Payment Posting ======== \n Payment_Success: "
							+ !error + "\n " + "MessageId: "
							+ transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem()
							+ "\n ======================================================\n\n");
			}

		} while (retry && retryLeft > 0);
	}

	private String getCreditAccountNumber(TransferForecastOrCreateRequest transferForecastOrCreateRequest,
			BankFusionEnvironment env) {
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();
		String trxType = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType();

		if (trxType.equalsIgnoreCase("INTERNALPYMT") || trxType.equalsIgnoreCase("INTRAPYMT")
				|| trxType.equalsIgnoreCase("INTERNALSOPYMT") || trxType.equalsIgnoreCase("MOBILETOPUP")) {

			if (transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType()
					.equalsIgnoreCase("INTERNALPYMT")
					|| transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType()
							.equalsIgnoreCase("INTERNALSOPYMT")) {
				creditAccountNumber = txnInput.getToMyAccount();

			} else {
				creditAccountNumber = txnInput.getTransferrecipientDtls().getOtherAccount();

				if (transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType()
						.equalsIgnoreCase("MOBILETOPUP")) {

					String providerID = txnInput.getTransferrecipientDtls().getOtherAccount();
					creditAccountNumber = getBenFicAccNo(providerID);
				}
				if (null == creditAccountNumber || creditAccountNumber.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
					String IBANAcc = txnInput.getTransferrecipientDtls().getIBANAccount();
					creditAccountNumber = getIBANAcc(env, IBANAcc);

				}

			}
		}

		else {

			String txnCurrency = txnInput.getTransferCurrency().getIsoCurrencyCode();
			Map debitAccDetails = new HashMap();
			debitAccDetails = UB_IBI_PaymentsHelper.getAccountDetails(txnInput.getFromMyAccount());
			String branchSortCode = (String) debitAccDetails.get("BRANCHSORTCODE");
			creditCurrency = txnInput.getTransferCurrency().getIsoCurrencyCode();

			/*
			 * Getting settlement/suspense account if credit account is not found
			 */
			String creditAccBranchCode = bizInfo.getModuleConfigurationValue(IfmConstants.MODULE_NAME, DFLNOSTRO, env)
					.toString();
			logger.info("NostroBranchCode configured for IBI module = " + creditAccBranchCode);
			if (creditAccBranchCode == null || creditAccBranchCode.equals(CommonConstants.EMPTY_STRING)) {
				creditAccBranchCode = branchSortCode;
			}
			String internationalContext = bizInfo.getModuleConfigurationValue(MODULE_NAME, NOSTROACCT_CONTEXT_KEY, env)
					.toString();
			logger.info("DEFAULT_NOSTRO configured for IBI module = " + internationalContext);

			creditAccountNumber = UB_IBI_PaymentsHelper.getNostroAcc(env, creditAccBranchCode, txnCurrency,
					creditCurrency, internationalContext);

			if (creditAccountNumber == null || creditAccountNumber.equals(CommonConstants.EMPTY_STRING)) {
				// return "40009261";
				logger.info("Credit Account not found for Branch Sort Code = " + creditAccBranchCode + ", Currency = "
						+ creditCurrency + ", Context = " + internationalContext);

			}
		}

		return creditAccountNumber;
	}

	public boolean isAccountCustomer(String account, String customerId, String channelId) {
		SearchAccountRq rq = new SearchAccountRq();
		AccountSearch accSearch = new AccountSearch();
		PagedQuery pagedQuery = new PagedQuery();
		PagingRequest pagingRequest = new PagingRequest();

		RqHeader rqHeader = new RqHeader();
		Orig orig = new Orig();
		orig.setChannelId(channelId);
		rqHeader.setOrig(orig);

		Date openFrom = new Date(70, 0, 1);
		Date openTo = new Date(1000, 1, 1);

		accSearch.setAccountId(account);
		accSearch.setCustomerId(customerId);
		accSearch.setAccountFormatType("ST");
		accSearch.setDateAccountOpenedFrom(openFrom);
		accSearch.setDateAccountOpenedTo(openTo);

		pagingRequest.setNumberOfRows(60);
		pagingRequest.setRequestedPage(1);
		pagingRequest.setTotalPages(10);

		pagedQuery.setPagingRequest(pagingRequest);

		rq.setAccountSearch(accSearch);
		rq.setPagedQuery(pagedQuery);
		rq.setRqHeader(rqHeader);

		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("searchAccountRq", rq);

		Map outputMap = MFExecuter.executeMF("UB_R_CB_ACC_SearchAccountWithRoles_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		SearchAcctRs rs = (SearchAcctRs) outputMap.get("searchAcctRs");

		if (rs.getSearchAccountDetails().getListAccountDtls().length >= 0) {
			AccountMandateDetails[] accMandateDetails = rs.getSearchAccountDetails().getListAccountDtls()[0]
					.getAcctInfo().getListMandateDetails().getAccountMandateDtls();
			for (int i = 0; i < accMandateDetails.length; i++) {
				if (customerId.equalsIgnoreCase(accMandateDetails[i].getCustID()))
					return true;
			}
		} else
			return false;

		return false;
	}

	private void insertMessageHeader(String messageId, String trxType) {
		logger.info(" Start of insertMessageHeader ");
		IBOUB_INF_MessageHeader messageHeader = (IBOUB_INF_MessageHeader) factory
				.getStatelessNewInstance(IBOUB_INF_MessageHeader.BONAME);
		messageHeader.setBoID(messageId);
		ComplexTypeConvertor complexConverter = new ComplexTypeConvertor(this.getClass().getClassLoader());
		String blobSaveValue = (String) bizInfo.getModuleConfigurationValue(IfmConstants.SYS_MODULE_CONFIG_KEY,
				"TROUBLESHOOTING_JMS_CHANNELS", null);
		if (blobSaveValue != null
				&& (blobSaveValue.equalsIgnoreCase("yes") || blobSaveValue.equalsIgnoreCase("failure"))) {
			String datamessage = complexConverter.getXmlFromJava(
					getF_IN_transferForecastOrCreateRequest().getClass().getName(),
					getF_IN_transferForecastOrCreateRequest());
			messageHeader.setF_DATAMESSAGE(getOBJECTValue(datamessage));
		}
		String type = "ITR";
		// Changes for FBIT-2434: Mobile Top UP
		if (trxType.equalsIgnoreCase("INTRAPYMT") || trxType.equalsIgnoreCase("MOBILETOPUP")) {
			type = "IBTR";
		} else if (trxType.equalsIgnoreCase("DOMESTICPYMT")) {
			type = "DPAY";
		} else if (trxType.equalsIgnoreCase("INTNAT")) {
			type = "IPAY";
		}

		messageHeader.setF_MESSAGETYPE(type);
		messageHeader.setF_CHANNELID(getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId());
		messageHeader.setF_MESSAGESTATUS("R");
		messageHeader.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
		messageHeader.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
		messageHeader.setF_DIRECTION("I");
		BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_MessageHeader.BONAME, messageHeader);
		BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
		BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
		logger.info(" End of insertMessageHeader ");
	}

	public TransferResponse postResponse(String messageId, String trxType, String status,
			TransferForecastOrCreateRequest transferForecastOrCreateRequest, HashMap onlineCharges, String customerId,
			CreateSettlementInstructionsRs createSettlementInstructionRs, String accountID, String debitCurrency) {

		return postResponse(messageId, trxType, status, transferForecastOrCreateRequest, onlineCharges, customerId,
				createSettlementInstructionRs, accountID, debitCurrency, "");
	}

	public TransferResponse postResponse(String messageId, String trxType, String status,
			TransferForecastOrCreateRequest transferForecastOrCreateRequest, HashMap onlineCharges, String customerId,
			CreateSettlementInstructionsRs createSettlementInstructionRs, String accountID, String debitCurrency,
			String eventMessage) {
		logger.info("Start of postResponse method and the status is " + status);
		TransferResponse transferResponse = getF_OUT_transferResponse();
		String reqPayload = "REQUEST_PAYLOAD";
		if (getF_IN_transferForecastOrCreateRequest().getReqPayload() != null
				&& !getF_IN_transferForecastOrCreateRequest().getReqPayload().equals(""))
			reqPayload = getF_IN_transferForecastOrCreateRequest().getReqPayload();
		transferResponse.setReqPayload(reqPayload);
		transferResponse.getRsHeader()
				.setOrigCtxtId(getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getOrigCtxtId());
		InstructionUpdateItem[] insItem = transferResponse.getInstructionStatusUpdateNotification()
				.getInstructionUpdateItem();
		boolean isEssenceTxnIdReq = false;
		if (transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getEssenceTxnIdRequired() != null)
			isEssenceTxnIdReq = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
					.getTransferForecastInp().getEssenceTxnIdRequired();
		if (status.equalsIgnoreCase("SUCCESS")) {
			for (int i = 0; i < 1; i++) {
				insItem[i].setTransactionalItem(
						transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
				insItem[i].setSoReference(
						transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
				insItem[i].setNewStatus("PROCESS_SUCCESSFULLY");
				if (isEssenceTxnIdReq == true) {
					insItem[i].setTransactionId(transactionID);
				}
			}
			insertPaymentMessae(messageId, "SUCCESS", transferResponse);
		}
		// Added NONSTP Check for FBPY-1925: Start
		else if (trxType.equals("INTNAT") && status.equalsIgnoreCase("MANUAL_INTERVENTION")) {
			for (int i = 0; i < 1; i++) {
				insItem[i].setTransactionalItem(
						transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
				insItem[i].setSoReference(
						transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
				insItem[i].setNewStatus("MANUAL_INTERVENTION");
				if (isEssenceTxnIdReq == true) {
					insItem[i].setTransactionId(transactionID);
				}
			}
		}
		// Added NONSTP Check for FBPY-1925: End
		else {
			for (int i = 0; i < 1; i++) {
				insItem[i].setTransactionalItem(
						transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
				insItem[i].setSoReference(
						transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
				insItem[i].setNewStatus("REJECTED");
				TransactionEvent transactionEvent = new TransactionEvent();
				String eventMsg = CommonConstants.EMPTY_STRING;

				transactionEvent.setReasonCode(status);
				if (CommonUtil.checkIfNullOrEmpty(eventMessage)) {
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), new String[] {});
				} else {
					eventMsg = eventMessage;
				}
				String[] StringArrayParameter = new String[2];
				StringArrayParameter[0] = accountID;
				boolean isErrorInCase = true;
				switch (status) {
				case "40507020":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				case "40000408":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				case "40007321":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				case "40007322":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				case "40007323":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				case "40112172":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				case "40407064":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { customerId });
					StringArrayParameter[0] = customerId;
					break;
				case "40407516":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { invalidAccount });
					StringArrayParameter[0] = invalidAccount;
					break;
				case "40409528":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { dormantAccount });
					StringArrayParameter[0] = dormantAccount;
					break;
				case "40000132":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { closedOrStoppedAccount });
					StringArrayParameter[0] = closedOrStoppedAccount;
					break;
				case "40000133":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { closedOrStoppedAccount });
					StringArrayParameter[0] = closedOrStoppedAccount;
					break;
				case "40409437":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { responseEventParam_STPNONSTP });
					StringArrayParameter[0] = responseEventParam_STPNONSTP;
					break;
				case "40407512":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { responseEventParam_STPNONSTP });
					StringArrayParameter[0] = responseEventParam_STPNONSTP;
					break;
				case "40409439":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { responseEventParam_STPNONSTP });
					StringArrayParameter[0] = responseEventParam_STPNONSTP;
					break;
				case "40410028":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { responseEventParam_STPNONSTP });
					StringArrayParameter[0] = responseEventParam_STPNONSTP;
					break;
				case "40010070":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { responseEventParam_STPNONSTP });
					StringArrayParameter[0] = responseEventParam_STPNONSTP;
					break;
				case "40430061":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { eventMessage });
					break;
				case "40007318":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status),
							new String[] { eventMessage });
					break;
				case "40007319":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				case "40007325":
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), StringArrayParameter);
					break;
				default:
					eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), new String[] {});
					isErrorInCase = false;
					break;

				}

				transactionEvent.setFormattedMessage(eventMsg);
				transactionEvent.setDefaultMessage(eventMsg);
				if (isErrorInCase) {
					String[] argumentList = new String[StringArrayParameter.length + 1];
					argumentList[0] = StringArrayParameter[0];
					argumentList[1] = StringArrayParameter[1];
					argumentList[2] = BankFusionMessages.getFormattedMessage(Integer.parseInt(status), new String[] {});
					transactionEvent.setEventParameters(argumentList);
				}
				insItem[i].setTransactionEvent(transactionEvent);
			}
			logger.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
			insertPaymentMessae(messageId, "FAILURE", transferResponse);
		}
		logger.info("End of postResponse");
		return transferResponse;
	}

	private void insertSwiftMessageDtls(String custBICCode) {
		logger.info("Inside insertSwiftMessageDtls method");
		TransferForecastInpDetails txnInput = getF_IN_transferForecastOrCreateRequest()
				.getTransferForecastOrCreateInput().getTransferForecastInp();
		IBOUB_IBI_SWFTPAYMENT swiftMessage = (IBOUB_IBI_SWFTPAYMENT) factory
				.getStatelessNewInstance(IBOUB_IBI_SWFTPAYMENT.BONAME);
		swiftMessage.setBoID(GUIDGen.getNewGUID());
		swiftMessage.setF_TRANSACTIONREF(txnInput.getPaymentReference());
		swiftMessage.setF_INSTRUCTEDAMT(txnInput.getAmount().getAmount().toString());
		if (getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId().equals(MODULEID_CCI)) {
			String beneName = txnInput.getTransferrecipientDtls().getAddressLine1();
			String beneAddress1 = txnInput.getTransferrecipientDtls().getAddressLine2();
			String beneAddress2 = txnInput.getTransferrecipientDtls().getAddressLine3();
			String beneAddress3 = txnInput.getTransferrecipientDtls().getAddressLine4();
			swiftMessage.setF_BENFCYENTITYNAME(beneName);
			swiftMessage.setF_BENFCYADDRESSLINE1(beneAddress1);
			swiftMessage.setF_BENFCYADDRESSLINE2(beneAddress2);
			swiftMessage.setF_BENFCYADDRESSLINE3(beneAddress3);
		} else {
			String beneName = txnInput.getTransferrecipientDtls().getBeneficiaryName();
			String beneAddress = txnInput.getTransferrecipientDtls().getBeneficiaryAddress();
			String beneCountry = txnInput.getTransferrecipientDtls().getBeneficiaryCountry();
			StringBuilder st = new StringBuilder();
			if (SettlementInstructionsUpdator.isValueAvailable(beneName))
				st.append(beneName);
			if (SettlementInstructionsUpdator.isValueAvailable(beneAddress))
				st.append(" " + beneAddress);
			if (SettlementInstructionsUpdator.isValueAvailable(beneCountry))
				st.append(" " + beneCountry);
			addressLines = SettlementInstructionsUpdator.getBenDetails(st);

			swiftMessage.setF_BENFCYADDRESSLINE1(addressLines.get("Line1"));
			swiftMessage.setF_BENFCYADDRESSLINE2(addressLines.get("Line2"));
			swiftMessage.setF_BENFCYADDRESSLINE3(addressLines.get("Line3"));
		}
		swiftMessage.setF_BENFCYCUSTBENFCYACCNO(txnInput.getFromMyAccount());
		swiftMessage.setF_INSTRUCTEDCURRCODE(txnInput.getTransferCurrency().getIsoCurrencyCode());
		swiftMessage.setF_BENFCYBICCODE(custBICCode);
		factory.create(IBOUB_IBI_SWFTPAYMENT.BONAME, swiftMessage);
	}

	private void insertPaymentMessae(String messageId, String status, TransferResponse transferResponse) {
		logger.info("Inside insertPaymentMessae method");
		IBOUB_IBI_PAYMENT paymentMessage = (IBOUB_IBI_PAYMENT) factory
				.getStatelessNewInstance(IBOUB_IBI_PAYMENT.BONAME);
		paymentMessage.setBoID(GUIDGen.getNewGUID());
		paymentMessage.setF_MESSAGEID(messageId);
		paymentMessage.setF_TXNENTRYDT(
				SystemInformationManager.getInstance().getBFBusinessDateTime().toString().substring(0, 10));
		paymentMessage.setF_TXNVALDT(getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput()
				.getTransferForecastInp().getTransferDate().toString());
		paymentMessage.setF_CRDRFLAG("D");
		paymentMessage.setF_TRANSACTIONID(transactionID);
		paymentMessage.setF_ACCOUNTNO(getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput()
				.getTransferForecastInp().getFromMyAccount());
		paymentMessage.setF_AMOUNTVALUE(getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput()
				.getTransferForecastInp().getAmount().getAmount().toString());
		if (getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput().getTransferForecastInp()
				.getTransferrecipientDtls() != null
				&& getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput().getTransferForecastInp()
						.getTransferrecipientDtls().getBeneficiaryName() != null) {
			if (getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput().getTransferForecastInp()
					.getTransferrecipientDtls().getBeneficiaryName().length() > 35) {
				paymentMessage.setF_PAYEENAME(getF_IN_transferForecastOrCreateRequest()
						.getTransferForecastOrCreateInput().getTransferForecastInp().getTransferrecipientDtls()
						.getBeneficiaryName().toString().substring(0, 35));
			} else {
				paymentMessage
						.setF_PAYEENAME(getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput()
								.getTransferForecastInp().getTransferrecipientDtls().getBeneficiaryName().toString());
			}
		}
		paymentMessage.setF_FXTRANSFERAMT(getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput()
				.getTransferForecastInp().getAmount().getAmount().toString());
		paymentMessage.setF_FXTRANSFERCURR(getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput()
				.getTransferForecastInp().getAmount().getIsoCurrencyCode().toString());
		if (getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput().getTransferForecastInp()
				.getPaymentReference() != null) {
			paymentMessage.setF_BANKMEMO(getF_IN_transferForecastOrCreateRequest().getTransferForecastOrCreateInput()
					.getTransferForecastInp().getPaymentReference().toString());
		}
		factory.create(IBOUB_IBI_PAYMENT.BONAME, paymentMessage);

		String messageStatus = "P";
		if (status.equalsIgnoreCase("failure")) {
			messageStatus = "F";
		}
		String blobSaveValue = (String) bizInfo.getModuleConfigurationValue(IfmConstants.SYS_MODULE_CONFIG_KEY,
				"TROUBLESHOOTING_JMS_CHANNELS", null);
		PrivatePersistenceFactory factoryNew = new PrivatePersistenceFactory();
		IBOUB_INF_MessageHeader existingHeaderBOItem = (IBOUB_INF_MessageHeader) factory
				.findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, messageId, true);
		if (existingHeaderBOItem != null) {
			IBOUB_INF_MessageHeader headerBOItem = (IBOUB_INF_MessageHeader) factory
					.getStatelessNewInstance(IBOUB_INF_MessageHeader.BONAME);
			headerBOItem.updateFromMap(existingHeaderBOItem.getDataMap());
			headerBOItem.setF_MESSAGESTATUS(messageStatus);
			if (messageStatus.equalsIgnoreCase("F")) {
				headerBOItem.setF_REMITTANCEID(CommonConstants.EMPTY_STRING);
			}
			ComplexTypeConvertor complexConverter = new ComplexTypeConvertor(this.getClass().getClassLoader());
			if (blobSaveValue.equalsIgnoreCase("yes")) {
				String datamessage = complexConverter.getXmlFromJava(transferResponse.getClass().getName(),
						transferResponse);
				headerBOItem.setF_RESPONSEMESSAGE(getOBJECTValue(datamessage));
			} else if (blobSaveValue.equalsIgnoreCase("failure") && messageStatus.equalsIgnoreCase("F")) {
				String datamessage = complexConverter.getXmlFromJava(transferResponse.getClass().getName(),
						transferResponse);
				headerBOItem.setF_RESPONSEMESSAGE(getOBJECTValue(datamessage));
			}
			headerBOItem.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
			factoryNew.update(headerBOItem);

		}
	}

	private String generateSwiftMessage(TransferForecastOrCreateRequest transferForecastOrCreateRequest,
			HashMap onlineCharges, String customerId, CreateSettlementInstructionsRs createSettlementInstructionRs,
			BigDecimal debitAmount) {
		logger.info("Start of generateSwiftMessage");
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();
		HashMap inputParams = new HashMap();
		String IbanOrAcc = txnInput.getTransferrecipientDtls().getOtherAccount();
		if (txnInput.getTransferrecipientDtls().getIBANAccount() != null
				&& !txnInput.getTransferrecipientDtls().getIBANAccount().equalsIgnoreCase("")) {
			IbanOrAcc = txnInput.getTransferrecipientDtls().getIBANAccount();
		}
		StringBuilder iBanOrOtherAcc = new StringBuilder();
		iBanOrOtherAcc.append("/").append(IbanOrAcc);
		BigDecimal totalCharge = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal totalTax = CommonConstants.BIGDECIMAL_ZERO;
		String chargeCurrencyCode = CommonConstants.EMPTY_STRING;
		if (onlineCharges != null) {
			totalCharge = (BigDecimal) onlineCharges.get("CONSOLIDATEDCHARGEAMT");
			totalTax = (BigDecimal) onlineCharges.get("CONSOLIDATEDTAXAMT");
			chargeCurrencyCode = (String) onlineCharges.get("FUNDINGACCCURRENCY");
		}
		BigDecimal TotalCharge = CommonConstants.BIGDECIMAL_ZERO;
		TotalCharge = totalCharge.add(totalTax);
		SimplePersistentObject custDtls = factory.findByPrimaryKey(IBOCustomer.BONAME, customerId, false);
		SimplePersistentObject swtCustDtls = null;
		if (StringUtils.isNotEmpty(custDtls.getDataMap().get("f_BRANCHSORTCODE").toString())) {
			swtCustDtls = BranchUtil
					.getBranchDetailsInCurrentZone(custDtls.getDataMap().get("f_BRANCHSORTCODE").toString());
		}
		String custBICCode = CommonConstants.EMPTY_STRING;
		if (swtCustDtls != null && swtCustDtls.getDataMap().size() > 0) {
			custBICCode = swtCustDtls.getDataMap().get("f_BICCODE").toString();
		}
		String settlDtlId = createSettlementInstructionRs.getCreateSettlementInstructionsInputRs().getSettInstrDtlId();
		String paymentRef = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getPaymentReference();
		String tablePayRef = UB_IBI_PaymentsHelper.actionSettlemInsDtlPayDtls(settlDtlId, paymentRef);
		IBOSWTSettlementInstructionDetail settlDtl = (IBOSWTSettlementInstructionDetail) BankFusionThreadLocal
				.getPersistanceFactory().findByPrimaryKey(IBOSWTSettlementInstructionDetail.BONAME, settlDtlId, true);
		int messageNumber = settlDtl.getF_MESSAGE_NUMBER();
		// String exchangeRateType = getMisCode(transferForecastOrCreateRequest);
		Map accDetails = new HashMap();
		accDetails = UB_IBI_PaymentsHelper.getAccountDetails(txnInput.getFromMyAccount());
		String debitCurrency = (String) accDetails.get("ISOCURRENCYCODE");
		String exchangeRateType = getMisCode(transferForecastOrCreateRequest, debitCurrency,
				txnInput.getTransferCurrency().getIsoCurrencyCode());
		BigDecimal exchangeRate = UB_IBI_PaymentsHelper.getExchangeRate(exchangeRateType, debitCurrency,
				txnInput.getTransferCurrency().getIsoCurrencyCode(), txnInput.getAmount().getAmount());
		inputParams.put("BeneficairyCustomerPartyIdentifier", iBanOrOtherAcc.toString());
		inputParams.put("BankPostingCurrencyCode", txnInput.getTransferCurrency().getIsoCurrencyCode().toString());
		// for Swift the deal_number should be maximum of 16 characters.
		if ((transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals("CCI")
				&& txnReference.length() > 16)) {
			inputParams.put("Deal_Number", txnReference.substring(0, 16));
		} else {
			inputParams.put("Deal_Number", txnReference);
		}
		inputParams.put("FXTransaction", 7);
		inputParams.put("TransactionID", transactionID);
		inputParams.put("Generate103Plus", "N");
		String generate900910 = "9";
		String MICCode = getTransactionCode(transferForecastOrCreateRequest, "debit");
		SimplePersistentObject generate900Or910 = factory.findByPrimaryKey(IBOMisTransactionCodes.BONAME, MICCode,
				false);
		if (generate900Or910.getDataMap().get("f_SWTDRCRCONFIRMATION") != null
				&& !generate900Or910.getDataMap().get("f_SWTDRCRCONFIRMATION").equals(CommonConstants.EMPTY_STRING)) {
			generate900910 = generate900Or910.getDataMap().get("f_SWTDRCRCONFIRMATION").toString();
		}
		inputParams.put("DRCRConfirmFlag", generate900910);
		inputParams.put("OrderingCustomerIdentifierCode", custBICCode);
		inputParams.put("Settl_Instruction_Number", messageNumber);
		if (txnInput.getBeneficiaryBank().getBankSWIFTorBIC() != null) {
			inputParams.put("BeneficairyInstituteIdentifierCode", txnInput.getBeneficiaryBank().getBankSWIFTorBIC());
		}
		String beneName = txnInput.getTransferrecipientDtls().getBeneficiaryName();
		String beneAddress = txnInput.getTransferrecipientDtls().getBeneficiaryAddress();
		String beneCountry = txnInput.getTransferrecipientDtls().getBeneficiaryCountry();
		StringBuilder st = new StringBuilder();
		if (SettlementInstructionsUpdator.isValueAvailable(beneName))
			st.append(beneName);
		if (SettlementInstructionsUpdator.isValueAvailable(beneAddress))
			st.append(" " + beneAddress);
		if (SettlementInstructionsUpdator.isValueAvailable(beneCountry))
			st.append(" " + beneCountry);
		addressLines = SettlementInstructionsUpdator.getBenDetails(st);

		inputParams.put("BeneficiaryCustomerText1", addressLines.get("Line1"));
		inputParams.put("BeneficiaryCustomerText2", addressLines.get("Line2"));
		inputParams.put("BeneficiaryCustomerText3", addressLines.get("Line3"));
		if (txnInput.getBeneficiaryBank().getBankNameAndAddress() != null) {
			inputParams.put("BeneficiaryInstituteText1",
					txnInput.getBeneficiaryBank().getBankNameAndAddress().getBankName());
			inputParams.put("BeneficiaryInstituteText2",
					txnInput.getBeneficiaryBank().getBankNameAndAddress().getCity());
			inputParams.put("BeneficiaryInstituteText3",
					txnInput.getBeneficiaryBank().getBankNameAndAddress().getCountry());
		}

		BigDecimal TotalChargeInMainAccCurr = CommonConstants.BIGDECIMAL_ZERO;
		VectorTable nvector = (VectorTable) onlineCharges.get("RESULT");
		String chgAmtInFundingAccountCurrency = null, chgAmtInMainAccountCurrency = null, chargeExchangeRate = null;
		HashMap nmap = new HashMap();
		for (int i = 0; i < nvector.size(); i++) {
			nmap = nvector.getRowTags(i);
			chgAmtInFundingAccountCurrency = (String) nmap.get("CHARGEAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode");
			chgAmtInMainAccountCurrency = (String) nmap.get("CHARGEAMOUNT_IN_ACC_CURRENCY_CurrCode");
			chargeExchangeRate = (String) nmap.get("CHARGEEXCHANGERATETYPE");
		}
		TotalChargeInMainAccCurr = UB_IBI_PaymentsHelper.currencyConversion(chgAmtInFundingAccountCurrency,
				chgAmtInMainAccountCurrency, TotalCharge, chargeExchangeRate, true);

		String uetr = !StringUtils.isBlank(createSettlementInstructionRs.getRsHeader().getOrigCtxtId())
				? createSettlementInstructionRs.getRsHeader().getOrigCtxtId()
				: StringUtils.EMPTY;
		inputParams.put("ChargeAmount", TotalChargeInMainAccCurr);
		inputParams.put("ChargeCode", txnInput.getCharges());
		inputParams.put("ChargeCurrency", chargeCurrencyCode);
		inputParams.put("ContraAmount", txnInput.getAmount().getAmount());
		inputParams.put("Contra_Account", creditAccountNumber);
		inputParams.put("Customer_Number", customerId);
		inputParams.put("ExchangeRate", exchangeRate);
		inputParams.put("FundingAmount", debitAmount);
		inputParams.put("ReceiverChargeAmount", CommonConstants.BIGDECIMAL_ZERO);
		inputParams.put("Main_account", txnInput.getFromMyAccount());
		inputParams.put("MessageType", "103");
		inputParams.put("Post_Date", txnInput.getTransferDate());
		inputParams.put("Transaction_Amount", txnInput.getAmount().getAmount());
		inputParams.put("Value_Date", txnInput.getTransferDate());
		inputParams.put("WalkInCustomer", false);
		inputParams.put("code_word", "NEW");
		inputParams.put("DraftNumber", 0L);
		inputParams.put("EndToEndRef", uetr);
		logger.info("Calling UB_SWT_MessageValidator_SRV - Microflow");
		HashMap outputParams = MFExecuter.executeMF("UB_SWT_MessageValidator_SRV", inputParams,
				BankFusionThreadLocal.getUserLocator().toString());
		insertSwiftMessageDtls(custBICCode);
		tablePayRef = UB_IBI_PaymentsHelper.actionSettlemInsDtlPayDtls(settlDtlId, tablePayRef);
		logger.info("====Remittance information sent is " + tablePayRef + " =====");
		logger.info("End of generateSwiftMessage");
		return outputParams.get("Status_Flag").toString();
	}

	public static void posting(String debitTxnCode, String exgRateType, String creditAccNum, BigDecimal debitTxnAmt,
			String creditTxnCode, String debitAccount, String debitTxnNarrative, String creditTxnNarrative,
			String debitTxnCurrencyCode, String creditTxnCurrencyCode, BigDecimal creditTxnAmount, String txnRef,
			String transactionID, String debitAccBranchSortCode, String channelId, Date transferDt) {
		logger.info("Inside posting method");
		if (channelId.equals(MODULEID_CCI)) {
			debitTxnNarrative = trfForeCastRq.getTransferForecastOrCreateInput().getTransferForecastInp()
					.getDebitNarrative();
			creditTxnNarrative = trfForeCastRq.getTransferForecastOrCreateInput().getTransferForecastInp()
					.getCreditNarrative();
		}
		Date valueDate = null;
		if (transferDt != null) {
			valueDate = transferDt;

		} else {
			valueDate = SystemInformationManager.getInstance().getBFSystemDate();
		}

		Time valueDateTime = SystemInformationManager.getInstance().getBFSystemTime();
		Map<String, Object> inputMap = new HashMap<String, Object>();

		inputMap.put(IfmConstants.DEBITTRANSACTIONCODE, debitTxnCode);
		inputMap.put(IfmConstants.EXCHANGERATETYPE, exgRateType);
		inputMap.put(IfmConstants.SETTLEMENTACCOUNTID, creditAccNum);
		inputMap.put(IfmConstants.DEBITTRANSACTIONAMOUNT, debitTxnAmt);
		inputMap.put(IfmConstants.CREDITPOSTINGACTION, IfmConstants.CREDIT);
		inputMap.put(IfmConstants.DEBITPOSTINGACTION, IfmConstants.DEBIT);
		inputMap.put(IfmConstants.CREDITTRANSACTIONCODE, creditTxnCode);
		inputMap.put(IfmConstants.MAINACCOUNTID, debitAccount);
		inputMap.put(IfmConstants.DEBITTRANSACTIONNARRATIVE, debitTxnNarrative);
		inputMap.put(IfmConstants.CREDITTRANSACTIONNARRATIVE, creditTxnNarrative);
		inputMap.put(IfmConstants.DEBITTXNCURRENCYCODE, debitTxnCurrencyCode);
		inputMap.put(IfmConstants.CREDITTXNCURRENCYCODE, creditTxnCurrencyCode);
		inputMap.put(IfmConstants.CREDITTRANSACTIONAMOUNT, creditTxnAmount);
		inputMap.put(IfmConstants.CHANNELID, channelId);
		inputMap.put(IfmConstants.TRANSACTION_REFERENCE, txnRef);
		inputMap.put(IfmConstants.TRANSACTION_ID, transactionID);
		inputMap.put("BRANCHSORTCODE", debitAccBranchSortCode);
		inputMap.put("branchCode", debitAccBranchSortCode);
		inputMap.put("manualValueDate", valueDate);
		inputMap.put("manualValueTime", valueDateTime);

		logger.info(" -+- Posting data -+- ");
		for (String key : inputMap.keySet()) {
			logger.warn(key + " :: " + inputMap.get(key));
		}

		if (debitTxnAmt.compareTo(CommonConstants.BIGDECIMAL_ZERO) > 0) {
			try {
				logger.info("Calling UB_CMN_FinancialPosting_SRV - Microflow");
				MFExecuter.executeMF("UB_CMN_FinancialPosting_SRV", BankFusionThreadLocal.getBankFusionEnvironment(),
						inputMap);
			} catch (CollectedEventsDialogException collectedEventsDialogException) {
				List<ErrorEvent> errors = collectedEventsDialogException.getErrors();
				for (ErrorEvent runTimeError : errors) {
					if (runTimeError.getEventNumber() == 40112712) {
						handleEvent(40430058, new String[] {});
						return;
					}
				}
				throw collectedEventsDialogException;
			}
		}
	}

	private String getHomeBankName(BankFusionEnvironment env) {
		ArrayList params = new ArrayList();
		params.add(true);
		String bankName = CommonConstants.EMPTY_STRING;
		final String findHomeBankName = " WHERE " + IBOCB_CNF_Bank.HOMEBANKINDICATOR + " = ? ";
		@SuppressWarnings("FBPE")
		List<SimplePersistentObject> dbRows = env.getFactory().findByQuery(IBOCB_CNF_Bank.BONAME, findHomeBankName,
				params, null, false);
		if (dbRows != null && dbRows.size() > 0) {
			bankName = dbRows.get(0).getDataMap().get("f_BANKNAME").toString();
		}
		return bankName;
	}

	private String getIBANAcc(BankFusionEnvironment env, String context) {
		ArrayList params = new ArrayList();
		params.add(false);
		params.add(context);
		final String findAccountByPseudonameCurrencyAndContext = " WHERE " + IBOPseudonymAccountMap.ISDELETED
				+ " = ? AND " + IBOPseudonymAccountMap.PSEUDONAME + " = ?";
		List<SimplePersistentObject> dbRows = env.getFactory().findByQuery(IBOPseudonymAccountMap.BONAME,
				findAccountByPseudonameCurrencyAndContext, params, null, false);
		if (dbRows != null && dbRows.size() > 0) {
			creditAccountNumber = dbRows.get(0).getDataMap().get("f_ACCOUNTID").toString();
		}
		return creditAccountNumber;
	}

	private String verifyAndPostInternalOrIntraPosting(TransferForecastOrCreateRequest transferForecastOrCreateRequest,
			BankFusionEnvironment env, VectorTable vector, String customerId, Date transferDt) {
		logger.info("Start of verifyAndPostInternalOrIntraPosting");
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();
		String chgFundingAccountCurrency = CommonConstants.EMPTY_STRING;
		String txnCurrency = txnInput.getTransferCurrency().getIsoCurrencyCode();
		String ifmId = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem();
		String debitCurrency = CommonConstants.EMPTY_STRING;
		String debitAccBranchSortCode = CommonConstants.EMPTY_STRING;
		String creditAccBranchSortCode = CommonConstants.EMPTY_STRING;
		BigDecimal txmAmtInCreditAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal txnAmount = txnInput.getAmount().getAmount();
		BigDecimal debitAmount = CommonConstants.BIGDECIMAL_ZERO;
		String errorMessage = CommonConstants.EMPTY_STRING;

		Map debitAccDetails = null;
		String chgFundingAccount = CommonConstants.EMPTY_STRING;
		debitAccDetails = UB_IBI_PaymentsHelper.getAccountDetails(txnInput.getFromMyAccount());
		debitCurrency = (String) debitAccDetails.get("ISOCURRENCYCODE");
		// Debit account
		debitAccBranchSortCode = (String) debitAccDetails.get("BRANCHSORTCODE");
		chgFundingAccount = (String) debitAccDetails.get("CHARGEFUNDINGACCOUNT");
		/*
		 * Finding charge funding account and charge funding account currency
		 */
		logger.info("----  Validation starts   -----");
		if (!validateTxnAmount(txnAmount, txnCurrency)) {
			// return ("40009263");
			handleEvent(40009263, new String[] {});
		}

		if (chgFundingAccount == null || chgFundingAccount.equals(CommonConstants.EMPTY_STRING)) {
			chgFundingAccount = txnInput.getFromMyAccount();
			chgFundingAccountCurrency = debitCurrency;
		} else {
			chgFundingAccountCurrency = (String) UB_IBI_PaymentsHelper.getAccountDetails(chgFundingAccount)
					.get("ISOCURRENCYCODE");
		}
		if (txnInput.getFromMyAccount().equalsIgnoreCase(creditAccountNumber)) {
			// return "40226018";
			handleEvent(40226018, new String[] {});
		}
		if (null == creditAccountNumber || creditAccountNumber.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
			// return "40421541";
			handleEvent(40421541, new String[] {});
		}

		/*
		 * String creditCustomerCode = (String)
		 * UB_IBI_PaymentsHelper.getAccountDetails(creditAccountNumber).get(
		 * "CUSTOMERCODE"); String debitCustomerCode = (String)
		 * debitAccDetails.get("CUSTOMERCODE");
		 * if(creditCustomerCode.equalsIgnoreCase(debitCustomerCode)) {
		 * handleEvent(40280126, new String[] {}); }
		 */

		creditAccBranchSortCode = (String) UB_IBI_PaymentsHelper.getAccountDetails(creditAccountNumber)
				.get("BRANCHSORTCODE");
		if (creditAccBranchSortCode == null || creditAccBranchSortCode == "") {
			invalidAccount = creditAccountNumber;
			handleEvent(40407516, new String[] { creditAccountNumber });
		}
		// fix for FBIT-6914
		if ("IBI".equalsIgnoreCase(transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId())
				|| "MOB".equalsIgnoreCase(transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId())
				|| "CCI".equalsIgnoreCase(transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId())) {
			transferDt = SystemInformationManager.getInstance().getBFSystemDate();
		}
		if (transferDt == null || !UB_IBI_PaymentsHelper.validateDate(transferDt.toString().replaceAll("-", ""))) {
			// return "40000174";
			handleEvent(40000174, new String[] {});
		}

		// restrict empty or backDated date
		if (!DateUtils.isSameDay(transferDt, SystemInformationManager.getInstance().getBFSystemDate())) {
			if (transferDt.before(SystemInformationManager.getInstance().getBFSystemDate())
					|| "".equals(transferDt.toString().trim())) {
				if ("IBI".equalsIgnoreCase(transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId())
						|| "MOB".equalsIgnoreCase(
								transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId())) {
					transferDt = SystemInformationManager.getInstance().getBFSystemDate();
				} else {
					handleEvent(40000174, new String[] {});
				}
			}
		}

		/* Validating transaction amount */
		if (!UB_IBI_PaymentsHelper.validateAmount(String.valueOf(txnInput.getAmount().getAmount()))) {
			// return "40507025";
			handleEvent(40507025, new String[] {});
		}
		Map creditAccDetails = null;
		creditAccDetails = UB_IBI_PaymentsHelper.getAccountDetails(creditAccountNumber);

		creditCurrency = (String) creditAccDetails.get("ISOCURRENCYCODE");

		/*
		 * Validating debit account, its should not be closed/dormant/password protected
		 */
		String debitAccount = txnInput.getFromMyAccount();
		String debitAccStatus = isAccountPasswordProtected(debitAccount, IfmConstants.DR);
		if (debitAccStatus != CommonConstants.EMPTY_STRING) {
			// return debitAccStatus;
			int debitAccError = Integer.parseInt(debitAccStatus);
			handleEvent(debitAccError, new String[] { debitAccount, debitCurrency });
		}
		// Validating debit account, it should not be stopped,closed
		errorMessage = UB_IBI_PaymentsHelper.validateAccount(debitAccount, IfmConstants.DR);
		if (!errorMessage.equalsIgnoreCase("OK")) {
			// return errorMessage;
			closedOrStoppedAccount = debitAccount;
			int invalidAccError = Integer.parseInt(errorMessage);
			handleEvent(invalidAccError, new String[] { closedOrStoppedAccount });
		}
		if (UB_IBI_PaymentsHelper.isAccountDormant(debitAccount, debitTxnCode)) {
			// return "40409528";
			dormantAccount = debitAccount;
			handleEvent(40409528, new String[] { debitAccount });
		}
		/*
		 * Validating credit account, its should not be closed/dormant/password
		 * protected
		 */
		String creditAcc = isAccountPasswordProtected(creditAccountNumber, IfmConstants.CR);
		if (creditAcc != CommonConstants.EMPTY_STRING) {
			// return creditAcc;
			int creditAccError = Integer.parseInt(creditAcc);
			handleEvent(creditAccError, new String[] { creditAccountNumber, creditCurrency });
		}
		// Validating credit account,it should not be stopped,closed
		errorMessage = UB_IBI_PaymentsHelper.validateAccount(creditAccountNumber, IfmConstants.CR);
		if (!errorMessage.equalsIgnoreCase("OK")) {
			// return errorMessage;
			closedOrStoppedAccount = creditAccountNumber;
			int invalidAccError = Integer.parseInt(errorMessage);
			handleEvent(invalidAccError, new String[] { closedOrStoppedAccount });
		}

		if (UB_IBI_PaymentsHelper.isAccountDormant(creditAccountNumber, creditTxnCode)) {
			// return "40409528";
			dormantAccount = creditAccountNumber;
			handleEvent(40409528, new String[] { creditAccountNumber });
		}
		/*
		 * Validating charge funding account if charge is applicable and charge funding
		 * account is not same as main account
		 */
		if (vector.size() > 0 && !(chgFundingAccount.equalsIgnoreCase(debitAccount))) {
			String chargeAcc = isAccountPasswordProtected(chgFundingAccount, IfmConstants.DR);
			if (chargeAcc != CommonConstants.EMPTY_STRING) {
				// return chargeAcc;
				int chargeAccError = Integer.parseInt(chargeAcc);
				handleEvent(chargeAccError, new String[] { chgFundingAccount, chgFundingAccountCurrency });
			}
			// Validating credit account,it should not be stopped,closed
			errorMessage = UB_IBI_PaymentsHelper.validateAccount(chgFundingAccount, IfmConstants.DR);
			if (!errorMessage.equalsIgnoreCase("OK")) {
				// return errorMessage;
				closedOrStoppedAccount = chgFundingAccount;
				int invalidAccError = Integer.parseInt(errorMessage);
				handleEvent(invalidAccError, new String[] { closedOrStoppedAccount });
			}
			if (UB_IBI_PaymentsHelper.isAccountDormant(chgFundingAccount, debitTxnCode)) {
				dormantAccount = chgFundingAccount;
				handleEvent(40409528, new String[] { chgFundingAccount });
			}
		}

		logger.info("Cross currency stuff starts");
		/*
		 * Cross currency stuff between transaction currency and debit account currency
		 */
		String exchangeRate = getMisCode(transferForecastOrCreateRequest, debitCurrency, creditCurrency);
		if (!(debitCurrency.equalsIgnoreCase(txnCurrency))) {
			if (UB_IBI_PaymentsHelper.getExchangeRate(exchangeRate, txnCurrency, debitCurrency,
					txnInput.getAmount().getAmount()) == null) {
				// return "20020167";
				handleEvent(20020167, new String[] {});
			}
			debitAmount = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, debitCurrency, txnAmount, exchangeRate,
					true);
		} else {
			debitAmount = txnAmount;
		}
		/*
		 * Cross currency stuff between transaction currency and credit account currency
		 */

		if (txnCurrency.equalsIgnoreCase(creditCurrency)) {
			txmAmtInCreditAccCurrency = txnAmount;
		} else {
			if (UB_IBI_PaymentsHelper.getExchangeRate(exchangeRate, txnCurrency, creditCurrency,
					txnInput.getAmount().getAmount()) == null) {
				// return "20020167";
				handleEvent(20020167, new String[] {});
			}
			txmAmtInCreditAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, creditCurrency, txnAmount,
					exchangeRate, true);
		}
		/*
		 * Available balance check for debit account and charge funding account
		 */
		if (!(UB_IBI_PaymentsHelper.availableBalanceCheck(vector, debitAmount, debitAccount, chgFundingAccount))) {
			// return "33000074";
			handleEvent(33000074, new String[] {});
		}
		/* Checking the availability of position accounts */
		if (!(debitCurrency.equalsIgnoreCase(creditCurrency))
				&& (UB_IBI_PaymentsHelper.arePositionAccountsAvailable(debitCurrency, creditCurrency, spotPseudonym,
						positionAccountContext, debitAccBranchSortCode, creditAccBranchSortCode) == false)) {
			// return "40580005";
			handleEvent(40580005, new String[] {});
		}

		if (transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals("CCI")) {
			txnReference = ifmId;

		} else {
			txnReference = ifmId + ":" + autoNumService.getNextAutoRef(debitTxnCode, debitAccBranchSortCode);
		}

		transactionID = GUIDGen.getNewGUID();

		if (isKYCCheckRequired(transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId())) {
			logger.info("-------      KYC check starts     --------");
			KYCDataCache kycData = new KYCDataCache();
			JsonObject data = new JsonObject();
			JsonObject transactionPostingData = new JsonObject();
			JsonObject chargePostingData = new JsonObject();
			JsonObject responseMessageData = new JsonObject();

			responseMessageData.addProperty("customerId", customerId);
			responseMessageData.addProperty("transactionalItem",
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
			responseMessageData.addProperty("channelId",
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getOrigCtxtId());

			String messageId = checkKYC(transferForecastOrCreateRequest, debitAccDetails, creditAccDetails, null);

			transactionPostingData.addProperty("exchangeRate", exchangeRate);
			transactionPostingData.addProperty("channelId",
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId());
			transactionPostingData.addProperty("txnReference", txnReference);
			transactionPostingData.addProperty("transactionID", transactionID);
			transactionPostingData.addProperty("inputMsg_TransactionReference", txnInput.getPaymentReference());
			transactionPostingData.addProperty("debitAccBranchSortCode", debitAccBranchSortCode);
			transactionPostingData.addProperty("debitAmount", debitAmount.toString());
			transactionPostingData.addProperty("txmAmtInCreditAccCurrency", txmAmtInCreditAccCurrency.toString());
			transactionPostingData.addProperty("dr_Account", debitAccount);
			transactionPostingData.addProperty("dr_Accountname", (String) debitAccDetails.get("ACCOUNTNAME"));
			transactionPostingData.addProperty("dr_Currency", debitCurrency);
			transactionPostingData.addProperty("dr_TxnCode", debitTxnCode);
			transactionPostingData.addProperty("cr_Account", creditAccountNumber);
			transactionPostingData.addProperty("cr_AccountName", (String) creditAccDetails.get("ACCOUNTNAME"));
			transactionPostingData.addProperty("cr_TxnCode", creditTxnCode);
			transactionPostingData.addProperty("cr_Currency", creditCurrency);
			transactionPostingData.addProperty("updateCounterPartyInfo", true);
			transactionPostingData.addProperty("txnType",
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType());
			transactionPostingData.addProperty("isLoanPayment", isLoanPayment);
			transactionPostingData.addProperty("txnAmount", txnAmount.toString());
			transactionPostingData.addProperty("txnCurrency", txnCurrency);

			chargePostingData.addProperty("txnType",
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType());
			chargePostingData.addProperty("txnReference", txnReference);
			chargePostingData.addProperty("transactionID", transactionID);
			chargePostingData.addProperty("spotPseudonym", spotPseudonym);
			chargePostingData.addProperty("positionAccountContext", positionAccountContext);
			chargePostingData.addProperty("chargeIndicator", CommonConstants.INTEGER_ZERO.intValue());
			chargePostingData.addProperty("channelId",
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId());
			chargePostingData.addProperty("txnAmount", txnAmount.toString());
			chargePostingData.addProperty("fromAccount", debitAccount);
			chargePostingData.addProperty("chargeFundingAccount", chgFundingAccount);
			chargePostingData.addProperty("trasnferCurrency", txnInput.getTransferCurrency().getIsoCurrencyCode());
			chargePostingData.addProperty("fromAccountCurrency", debitCurrency);
			chargePostingData.addProperty("chargeFundingAccountCurrency", chgFundingAccountCurrency);
			chargePostingData.addProperty("dr_TxnCode", debitTxnCode);
			chargePostingData.addProperty("dr_AccountBranchSortCode", debitAccBranchSortCode);
			chargePostingData.addProperty("cr_TxnCode", creditTxnCode);
			chargePostingData.addProperty("cr_AccountBranchSortCode", creditAccBranchSortCode);
			chargePostingData.addProperty("creditAccountNumber", creditAccountNumber);
			chargePostingData.addProperty("error", error);

			data.add("transactionPostingData", transactionPostingData);
			data.add("chargePostingData", chargePostingData);
			data.add("responseMessageData", responseMessageData);

			kycData.putData(messageId, data.toString());

			throw new KYCException("Done a KYC check", FircoAckResponseType.PROBABLE_HIT);
		}

		else {
			logger.info("-------      Posting transactions     --------");
			UB_IBI_PaymentPersistence.postTransactions(exchangeRate,
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId(), txnReference,
					transactionID, txnInput.getPaymentReference(), debitAccBranchSortCode, debitAmount,
					txmAmtInCreditAccCurrency, txnAmount, txnCurrency,

					debitAccount, (String) debitAccDetails.get("ACCOUNTNAME"), debitCurrency, debitTxnCode,

					creditAccountNumber, (String) creditAccDetails.get("ACCOUNTNAME"), creditTxnCode, creditCurrency,

					true, transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType(),
					isLoanPayment, null, null, transferDt);
			if (!isLoanPayment)
				UB_IBI_PaymentPersistence.postCharges(
						transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType(),
						txnReference, transactionID, spotPseudonym, positionAccountContext,
						CommonConstants.INTEGER_ZERO.intValue(),
						getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId(), txnAmount,

						debitAccount, chgFundingAccount, txnInput.getTransferCurrency().getIsoCurrencyCode(),

						debitCurrency, chgFundingAccountCurrency, debitTxnCode, debitAccBranchSortCode, creditTxnCode,
						creditAccBranchSortCode, creditAccountNumber, error);
		}
		if (error) {
			// return "40422013";
			handleEvent(40422013, new String[] {});
		}
		logger.info("End of verifyAndPostInternalOrIntraPosting");
		return "SUCCESS";
	}

	private boolean isKYCCheckRequired(String channelID) {

		IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
				.getInstance().getServiceManager()
				.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
		Boolean isSystemKYCCheckEnabled = (Boolean) ((IBusinessInformation) ubInformationService.getBizInfo())
				.getModuleConfigurationValue(IfmConstants.MODULE_KYC, "SYSTEM_KYC_CHECK",
						BankFusionThreadLocal.getBankFusionEnvironment());
		if (channelID.equals("CCI")) {

			Boolean isCCI_KYC_CheckEnabled = (Boolean) ((IBusinessInformation) ubInformationService.getBizInfo())
					.getModuleConfigurationValue(IfmConstants.MODULE_KYC, CCI_KYC_MODULE_NAME,
							BankFusionThreadLocal.getBankFusionEnvironment());

			return isSystemKYCCheckEnabled.booleanValue() && isCCI_KYC_CheckEnabled.booleanValue();
		} else {
			Boolean isIBIKYCCheckEnabled = (Boolean) ((IBusinessInformation) ubInformationService.getBizInfo())
					.getModuleConfigurationValue(IfmConstants.MODULE_KYC, IfmConstants.IBI_PAYMENTS_PARAM,
							BankFusionThreadLocal.getBankFusionEnvironment());

			return isSystemKYCCheckEnabled.booleanValue() && isIBIKYCCheckEnabled.booleanValue();
		}
	}

	public static void postCounterPartyDetails(String transactionID, String counterAccountNumber,
			String counterAccountName, String direction, String channel) {

		UB_TXN_CounterPartyUpdate counterPartyUpdate = new UB_TXN_CounterPartyUpdate();
		counterPartyUpdate.setF_IN_TRANSACTIONID(transactionID);
		counterPartyUpdate.setF_IN_CONTRAACCNUM(counterAccountNumber);
		counterPartyUpdate.setF_IN_CUSTOMERNAME(counterAccountName);
		counterPartyUpdate.setF_IN_TRANSACTIONDIRECTION(direction);
		counterPartyUpdate.setF_IN_CHANNELNAME(channel);
		counterPartyUpdate.populateTable();

	}

	public static void postCounterPartyDetails(String transactionID, String counterAccountNumber,
			String counterAccountName, String direction, String channel, String txnSubType) {

		UB_TXN_CounterPartyUpdate counterPartyUpdate = new UB_TXN_CounterPartyUpdate();
		counterPartyUpdate.setF_IN_TRANSACTIONID(transactionID);
		counterPartyUpdate.setF_IN_CONTRAACCNUM(counterAccountNumber);
		counterPartyUpdate.setF_IN_CUSTOMERNAME(counterAccountName);
		counterPartyUpdate.setF_IN_TRANSACTIONDIRECTION(direction);
		counterPartyUpdate.setF_IN_CHANNELNAME(channel);
		counterPartyUpdate.setF_IN_TRANSACTIONSUBTYPE(txnSubType);

		counterPartyUpdate.populateTable();

	}

	private void verifyAndPostInternationalPosting(TransferForecastOrCreateRequest transferForecastOrCreateRequest,
			BankFusionEnvironment env, HashMap onlineCharges, String customerId, String settlDtlId) {
		logger.info("Start of verifyAndPostInternationalPosting");
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();
		Map debitAccDetails = new HashMap();
		String counterPartyAccount = null;
		String counterPartyName = null;
		String chgFundingAccountCurrency = CommonConstants.EMPTY_STRING;
		String debitCurrency = CommonConstants.EMPTY_STRING;
		String txnCurrency = CommonConstants.EMPTY_STRING;
		String branchSortCode = CommonConstants.EMPTY_STRING;
		String spotPseudonym = CommonConstants.EMPTY_STRING;
		String positionAccountContext = CommonConstants.EMPTY_STRING;
		String debitAccBranchSortCode = CommonConstants.EMPTY_STRING;
		String creditAccBranchSortCode = CommonConstants.EMPTY_STRING;
		BigDecimal txnAmtInTxnCurrency = txnInput.getAmount().getAmount();
		BigDecimal txnAmtInCreditAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal debitAmount = CommonConstants.BIGDECIMAL_ZERO;
		String errorMessage = CommonConstants.EMPTY_STRING;
		String indId = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem();
		String debitTxnCode = getTransactionCode(transferForecastOrCreateRequest, "debit");
		String chgFundingAccount = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp().getChargeFundingAccount();
		if (txnReference.isEmpty()) {
			txnReference = indId + ":" + autoNumService.getNextAutoRef(debitTxnCode, branchSortCode);
		}
		spotPseudonym = ForexModuleConfiguration.getSpotPositionPseudonym();
		positionAccountContext = (String) bizInfo.getModuleConfigurationValue(IfmConstants.SYS_MODULE_CONFIG_KEY,
				IfmConstants.SYS_POSITION_CONTEXT, null);
		/* Fetch debit account details */
		debitAccDetails = UB_IBI_PaymentsHelper.getAccountDetails(txnInput.getFromMyAccount());
		branchSortCode = (String) debitAccDetails.get("BRANCHSORTCODE");
		String configuredChgFundingAccount = (String) debitAccDetails.get("CHARGEFUNDINGACCOUNT");
		debitCurrency = (String) debitAccDetails.get("ISOCURRENCYCODE");
		creditCurrency = txnInput.getTransferCurrency().getIsoCurrencyCode();
		txnCurrency = txnInput.getTransferCurrency().getIsoCurrencyCode();
		debitAccBranchSortCode = (String) debitAccDetails.get("BRANCHSORTCODE");
		logger.info("------  Validation starts  ------");
		if (!validateTxnAmount(txnAmtInTxnCurrency, creditCurrency)) {
			// return "40009263";
			handleEvent(40009263, new String[] {});
		}
		/*
		 * Finding charge funding account and charge funding account currency
		 */
		if (chgFundingAccount != null && !chgFundingAccount.equals(CommonConstants.EMPTY_STRING)) {
			chgFundingAccountCurrency = (String) UB_IBI_PaymentsHelper.getAccountDetails(chgFundingAccount)
					.get("ISOCURRENCYCODE");
		} else if ((chgFundingAccount == null || chgFundingAccount.equals(CommonConstants.EMPTY_STRING))
				&& (configuredChgFundingAccount == null
						|| configuredChgFundingAccount.equals(CommonConstants.EMPTY_STRING))) {
			chgFundingAccount = txnInput.getFromMyAccount();
			chgFundingAccountCurrency = debitCurrency;
		} else if (configuredChgFundingAccount != null
				&& !configuredChgFundingAccount.equals(CommonConstants.EMPTY_STRING)) {
			chgFundingAccount = configuredChgFundingAccount;
			chgFundingAccountCurrency = (String) UB_IBI_PaymentsHelper.getAccountDetails(configuredChgFundingAccount)
					.get("ISOCURRENCYCODE");
		}

		/* Validating transaction amount */
		if (!UB_IBI_PaymentsHelper.validateAmount(String.valueOf(txnInput.getAmount().getAmount()))) {
			// return "40507025";
			handleEvent(40507025, new String[] {});
		}

		if (creditAccountNumber == null || creditAccountNumber.equals(CommonConstants.EMPTY_STRING)) {
			// return "40009261";
			handleEvent(40009261, new String[] {});
		}

		String value = isAccountPasswordProtected(creditAccountNumber, IfmConstants.CR);
		if (value != CommonConstants.EMPTY_STRING) {
			// return value;
			int errorEvent = Integer.parseInt(value);
			handleEvent(errorEvent, new String[] { creditAccountNumber, creditCurrency });
		}
		// Validates account,it should not be closed or stopped
		errorMessage = UB_IBI_PaymentsHelper.validateAccount(creditAccountNumber, IfmConstants.CR);
		if (!errorMessage.equalsIgnoreCase("OK")) {
			// return errorMessage;
			closedOrStoppedAccount = creditAccountNumber;
			int errorInvalidAccount = Integer.parseInt(errorMessage);
			handleEvent(errorInvalidAccount, new String[] { closedOrStoppedAccount });
		}
		if (UB_IBI_PaymentsHelper.isAccountDormant(creditAccountNumber, creditTxnCode)) {
			// return "40409528";
			dormantAccount = creditAccountNumber;
			handleEvent(40409528, new String[] {});
		}
		/* Validating credit account */
		if ((!UB_IBI_PaymentsHelper.validateAccount(creditAccountNumber, IfmConstants.CR).equalsIgnoreCase("OK"))
				|| UB_IBI_PaymentsHelper.isAccountDormant(creditAccountNumber, creditTxnCode)) {
			// return "40009261";
			closedOrStoppedAccount = creditAccountNumber;
			handleEvent(40009261, new String[] { closedOrStoppedAccount });
		}
		creditAccBranchSortCode = (String) UB_IBI_PaymentsHelper.getAccountDetails(creditAccountNumber)
				.get("BRANCHSORTCODE");
		String exchangeRate = getMisCode(transferForecastOrCreateRequest, debitCurrency, creditCurrency);
		/*
		 * Cross currency stuff between transaction currency and credit account currency
		 */
		logger.info("------  Cross currency stuff  ------");
		if (!(creditCurrency.equalsIgnoreCase(txnCurrency))) {
			if ((UB_IBI_PaymentsHelper.getExchangeRate(exchangeRate, txnCurrency, creditCurrency,
					txnAmtInTxnCurrency)) == null) {
				// return "20020167";
				handleEvent(20020167, new String[] {});
			}
			txnAmtInCreditAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, creditCurrency,
					txnAmtInTxnCurrency, exchangeRate, true);
		} else {
			txnAmtInCreditAccCurrency = txnAmtInTxnCurrency;
		}
		/* Validating debit account for dormancy/close/password flag */
		if (UB_IBI_PaymentsHelper.isAccountDormant(txnInput.getFromMyAccount(), debitTxnCode) == true) {
			// return "40409528";
			dormantAccount = txnInput.getFromMyAccount();
			handleEvent(40409528, new String[] { dormantAccount });
		}
		String debitAccrights = isAccountPasswordProtected(txnInput.getFromMyAccount(), IfmConstants.DR);
		if (debitAccrights != CommonConstants.EMPTY_STRING) {
			// return debitAccrights;
			int debitAccRights = Integer.parseInt(debitAccrights);
			handleEvent(debitAccRights, new String[] { txnInput.getFromMyAccount(), debitCurrency });
		}
		// Validates debit account, it should not be stopped or closed
		errorMessage = UB_IBI_PaymentsHelper.validateAccount(txnInput.getFromMyAccount(), IfmConstants.DR);
		if (!errorMessage.equalsIgnoreCase("OK")) {
			// return errorMessage;
			closedOrStoppedAccount = txnInput.getFromMyAccount();
			int errorInvalidAccount = Integer.parseInt(errorMessage);
			handleEvent(errorInvalidAccount, new String[] {});
		}

		/*
		 * Cross currency stuff between transaction currency and debit account currency
		 * but debit currency and transaction currency will be same in this case
		 */
		if (!(debitCurrency.equalsIgnoreCase(txnCurrency))) {
			if (UB_IBI_PaymentsHelper.getExchangeRate(exchangeRate, debitCurrency, debitCurrency,
					txnAmtInTxnCurrency) == null) {
				// return "20020167";
				handleEvent(20020167, new String[] {});
			}
			debitAmount = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, debitCurrency, txnAmtInTxnCurrency,
					exchangeRate, true);
		} else {
			debitAmount = txnAmtInTxnCurrency;
		}
		/*
		 * Validating charge funding account if charges are applicable and charge
		 * funding account is not same as main account
		 */
		VectorTable vector = (VectorTable) onlineCharges.get("RESULT");
		if (!isLoanPayment && vector.size() > 0 && !(chgFundingAccount.equalsIgnoreCase(txnInput.getFromMyAccount()))) {
			String debitChargeAccrights = isAccountPasswordProtected(chgFundingAccount, IfmConstants.DR);
			if (debitChargeAccrights != CommonConstants.EMPTY_STRING) {
				int debitChargeAccRight = Integer.parseInt(debitChargeAccrights);
				// return debitChargeAccrights;
				handleEvent(debitChargeAccRight, new String[] { chgFundingAccount, chgFundingAccountCurrency });
			}
			errorMessage = UB_IBI_PaymentsHelper.validateAccount(chgFundingAccount, IfmConstants.DR);
			if (!errorMessage.equalsIgnoreCase("OK")) {
				closedOrStoppedAccount = chgFundingAccount;
				int eventInvalidAccount = Integer.parseInt(errorMessage);
				// return errorMessage;
				handleEvent(eventInvalidAccount, new String[] {});
			}

			if (UB_IBI_PaymentsHelper.isAccountDormant(chgFundingAccount, debitTxnCode)) {
				// return "40409528";
				dormantAccount = chgFundingAccount;
				handleEvent(40409528, new String[] { dormantAccount });
			}
		}
		/* Checking the availability of position accounts */
		if (!(debitCurrency.equalsIgnoreCase(creditCurrency))
				&& (UB_IBI_PaymentsHelper.arePositionAccountsAvailable(debitCurrency, creditCurrency, spotPseudonym,
						positionAccountContext, debitAccBranchSortCode, creditAccBranchSortCode) == false)) {
			// return "40580005";
			handleEvent(40580005, new String[] {});
		}

		/*
		 * check if chgFundingAccount is valid account .
		 */
		if (chgFundingAccount != null && !chgFundingAccount.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {

			String accountID = (String) UB_IBI_PaymentsHelper.getAccountDetails(chgFundingAccount).get("ACCOUNTID");
			if ((accountID == null) || (accountID == "")) {
				invalidAccount = chgFundingAccount;
				handleEvent(40407516, new String[] { chgFundingAccount });
			}
		}

		/*
		 * Available balance check for debit account and charge funding account
		 */
		if (!(UB_IBI_PaymentsHelper.availableBalanceCheck(vector, debitAmount, txnInput.getFromMyAccount(),
				chgFundingAccount))) {
			handleEvent(33000074, new String[] {});
		}

		if (transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getCharges() != null
				&& transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
						.getCharges().equalsIgnoreCase("BEN")) {
			BigDecimal TotalChargeAndTax = BigDecimal.ZERO;
			TotalChargeAndTax = ((BigDecimal) onlineCharges.get("CONSOLIDATEDCHARGEAMT"))
					.add((BigDecimal) onlineCharges.get("CONSOLIDATEDTAXAMT"));
			BigDecimal totalChargeDebitAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
			BigDecimal totalChargeCreditAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
			if (!(chgFundingAccountCurrency.equals(debitCurrency))) {
				totalChargeDebitAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(chgFundingAccountCurrency,
						debitCurrency, TotalChargeAndTax, exchangeRate, true);
				debitAmount = debitAmount.subtract(totalChargeDebitAccCurrency);
			} else {
				debitAmount = debitAmount.subtract(TotalChargeAndTax);
			}
			if (!(chgFundingAccountCurrency.equals(txnCurrency))) {
				totalChargeCreditAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(chgFundingAccountCurrency,
						txnCurrency, TotalChargeAndTax, exchangeRate, true);
				txnAmtInCreditAccCurrency = txnAmtInCreditAccCurrency.subtract(totalChargeCreditAccCurrency);
				TotalChargeAndTax = totalChargeCreditAccCurrency;
			} else {
				txnAmtInCreditAccCurrency = txnAmtInCreditAccCurrency.subtract(TotalChargeAndTax);
			}
		}
		counterPartyAccount = txnInput.getTransferrecipientDtls().getOtherAccount();
		if (txnInput.getTransferrecipientDtls().getIBANAccount() != null
				&& !"".equalsIgnoreCase(txnInput.getTransferrecipientDtls().getIBANAccount())) {
			counterPartyAccount = txnInput.getTransferrecipientDtls().getIBANAccount();
		}
		counterPartyName = txnInput.getTransferrecipientDtls().getBeneficiaryName();

		transactionID = GUIDGen.getNewGUID();

		if (isKYCCheckRequired(transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId())) {
			logger.info("------  KYC check starts   -----");
			KYCDataCache kycData = new KYCDataCache();
			JsonObject data = new JsonObject();
			JsonObject transactionPostingData = new JsonObject();
			JsonObject chargePostingData = new JsonObject();
			JsonObject swiftMessageData = new JsonObject();
			JsonObject txnInputData = new JsonObject();
			JsonObject responseMessageData = new JsonObject();
			JsonObject counterPartyData = new JsonObject();

			responseMessageData.addProperty("customerId", customerId);
			responseMessageData.addProperty("transactionalItem",
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalItem());
			responseMessageData.addProperty("channelId",
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getOrigCtxtId());

			String messageId = checkKYC(transferForecastOrCreateRequest, debitAccDetails, null, settlDtlId);

			transactionPostingData.addProperty("exchangeRate", exchangeRate);
			transactionPostingData.addProperty("channelId",
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId());
			transactionPostingData.addProperty("txnReference", txnReference);
			transactionPostingData.addProperty("transactionID", transactionID);
			transactionPostingData.addProperty("inputMsg_TransactionReference", txnInput.getPaymentReference());
			transactionPostingData.addProperty("debitAccBranchSortCode", debitAccBranchSortCode);
			transactionPostingData.addProperty("debitAmount", debitAmount.toString());
			transactionPostingData.addProperty("txmAmtInCreditAccCurrency", txnAmtInCreditAccCurrency.toString());
			transactionPostingData.addProperty("dr_Account", txnInput.getFromMyAccount());
			transactionPostingData.addProperty("dr_Accountname", (String) debitAccDetails.get("ACCOUNTNAME"));
			transactionPostingData.addProperty("dr_Currency", debitCurrency);
			transactionPostingData.addProperty("dr_TxnCode", debitTxnCode);
			transactionPostingData.addProperty("cr_Account", creditAccountNumber);
			transactionPostingData.addProperty("cr_AccountName", "");
			transactionPostingData.addProperty("cr_TxnCode", creditTxnCode);
			transactionPostingData.addProperty("cr_Currency", creditCurrency);
			transactionPostingData.addProperty("updateCounterPartyInfo", true);
			transactionPostingData.addProperty("txnType",
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType());
			transactionPostingData.addProperty("isLoanPayment", isLoanPayment);
			transactionPostingData.addProperty("txnAmount", txnAmtInTxnCurrency.toString());
			transactionPostingData.addProperty("txnCurrency", txnCurrency);

			chargePostingData.addProperty("txnType",
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType());
			chargePostingData.addProperty("txnReference", txnReference);
			chargePostingData.addProperty("transactionID", transactionID);
			chargePostingData.addProperty("spotPseudonym", spotPseudonym);
			chargePostingData.addProperty("positionAccountContext", positionAccountContext);
			chargePostingData.addProperty("chargeIndicator", ChargeConstants.ONLINE_CHARGE_INDICATOR.intValue());
			chargePostingData.addProperty("channelId",
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId());
			chargePostingData.addProperty("txnAmount", txnAmtInTxnCurrency.toString());
			chargePostingData.addProperty("fromAccount", txnInput.getFromMyAccount());
			chargePostingData.addProperty("chargeFundingAccount", chgFundingAccount);
			chargePostingData.addProperty("trasnferCurrency", txnInput.getTransferCurrency().getIsoCurrencyCode());
			chargePostingData.addProperty("fromAccountCurrency", debitCurrency);
			chargePostingData.addProperty("chargeFundingAccountCurrency", chgFundingAccountCurrency);
			chargePostingData.addProperty("dr_TxnCode", debitTxnCode);
			chargePostingData.addProperty("dr_AccountBranchSortCode", debitAccBranchSortCode);
			chargePostingData.addProperty("cr_TxnCode", creditTxnCode);
			chargePostingData.addProperty("cr_AccountBranchSortCode", creditAccBranchSortCode);
			chargePostingData.addProperty("creditAccountNumber", creditAccountNumber);
			chargePostingData.addProperty("error", error);

			// Adding transferrecipientDtls -> otherAccount, iBANAccount and beneficiary
			// details
			JsonObject transferrecipientDtls = new JsonObject();
			transferrecipientDtls.addProperty("otherAccount", txnInput.getTransferrecipientDtls().getOtherAccount());
			if (txnInput.getTransferrecipientDtls().getIBANAccount() != null
					&& !"".equalsIgnoreCase(txnInput.getTransferrecipientDtls().getIBANAccount())) {
				transferrecipientDtls.addProperty("iBANAccount", txnInput.getTransferrecipientDtls().getIBANAccount());
			}

			// beneficiary details
			String beneName = txnInput.getTransferrecipientDtls().getBeneficiaryName();
			String beneAddress = txnInput.getTransferrecipientDtls().getBeneficiaryAddress();
			String beneCountry = txnInput.getTransferrecipientDtls().getBeneficiaryCountry();
			StringBuilder st = new StringBuilder();
			if (SettlementInstructionsUpdator.isValueAvailable(beneName))
				st.append(beneName);
			if (SettlementInstructionsUpdator.isValueAvailable(beneAddress))
				st.append(" " + beneAddress);
			if (SettlementInstructionsUpdator.isValueAvailable(beneCountry))
				st.append(" " + beneCountry);
			addressLines = SettlementInstructionsUpdator.getBenDetails(st);

			transferrecipientDtls.addProperty("beneficiaryName", addressLines.get("Line1"));
			transferrecipientDtls.addProperty("beneficiaryAddress", addressLines.get("Line2"));
			transferrecipientDtls.addProperty("beneficiaryCountry", addressLines.get("Line3"));
			counterPartyName = txnInput.getTransferrecipientDtls().getBeneficiaryName();

			// Adding TransferCurrency and Amount
			JsonObject transferCurrency = new JsonObject();
			transferCurrency.addProperty("isoCurrencyCode", txnInput.getTransferCurrency().getIsoCurrencyCode());
			JsonObject amount = new JsonObject();
			amount.addProperty("amount", txnInput.getAmount().getAmount().toString());

			// Adding BIC
			JsonObject beneficiaryBank = new JsonObject();
			if (txnInput.getBeneficiaryBank().getBankSWIFTorBIC() != null) {
				beneficiaryBank.addProperty("bankSWIFTorBIC", txnInput.getBeneficiaryBank().getBankSWIFTorBIC());
			}

			if (txnInput.getBeneficiaryBank().getBankNameAndAddress() != null) {
				JsonObject bankNameAndAddress = new JsonObject();
				bankNameAndAddress.addProperty("bankName",
						txnInput.getBeneficiaryBank().getBankNameAndAddress().getBankName());
				bankNameAndAddress.addProperty("city", txnInput.getBeneficiaryBank().getBankNameAndAddress().getCity());
				bankNameAndAddress.addProperty("country",
						txnInput.getBeneficiaryBank().getBankNameAndAddress().getCountry());
				beneficiaryBank.add("bankNameAndAddress", bankNameAndAddress);
			}

			txnInputData.add("transferrecipientDtls", transferrecipientDtls);
			txnInputData.add("beneficiaryBank", beneficiaryBank);
			txnInputData.add("transferCurrency", transferCurrency);
			txnInputData.add("amount", amount);
			txnInputData.addProperty("debitTxnCode", debitTxnCode);
			txnInputData.addProperty("creditTxnCode", creditTxnCode);
			txnInputData.addProperty("fromMyAccount", txnInput.getFromMyAccount());
			txnInputData.addProperty("charges", txnInput.getCharges());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String dateStr = df.format(txnInput.getTransferDate());
			txnInputData.addProperty("transferDate", dateStr);

			swiftMessageData.add("txnInput", txnInputData);
			swiftMessageData.addProperty("customerId", customerId);
			swiftMessageData.addProperty("settlDtlId", settlDtlId);
			swiftMessageData.addProperty("debitAmount", debitAmount.toString());
			swiftMessageData.addProperty("txnReference", txnReference);
			swiftMessageData.addProperty("transactionID", transactionID);
			swiftMessageData.addProperty("creditAccountNumber", creditAccountNumber);

			counterPartyData.addProperty("account", counterPartyAccount);
			counterPartyData.addProperty("name", counterPartyName);

			data.add("transactionPostingData", transactionPostingData);
			data.add("chargePostingData", chargePostingData);
			data.add("swiftMessageData", swiftMessageData);
			data.add("responseMessageData", responseMessageData);
			data.add("counterPartyData", counterPartyData);

			kycData.putData(messageId, data.toString());

			throw new KYCException("Done a KYC check", FircoAckResponseType.PROBABLE_HIT);
		}

		else {
			logger.info("-------      Posting transactions & Charges    --------");
			UB_IBI_PaymentPersistence.postTransactions(exchangeRate,

					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId(), txnReference,
					transactionID, txnInput.getPaymentReference(), debitAccBranchSortCode, debitAmount,
					txnAmtInCreditAccCurrency, txnAmtInTxnCurrency, txnCurrency,

					txnInput.getFromMyAccount(), (String) debitAccDetails.get("ACCOUNTNAME"), debitCurrency,
					debitTxnCode,

					creditAccountNumber, null, creditTxnCode, creditCurrency,

					true, transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType(),
					isLoanPayment, counterPartyAccount, counterPartyName, null);

			UB_IBI_PaymentPersistence.postCharges(
					transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType(),
					txnReference, transactionID, spotPseudonym, positionAccountContext,
					ChargeConstants.ONLINE_CHARGE_INDICATOR.intValue(),
					getF_IN_transferForecastOrCreateRequest().getRqHeader().getOrig().getChannelId(),
					txnAmtInTxnCurrency,

					txnInput.getFromMyAccount(), chgFundingAccount, txnInput.getTransferCurrency().getIsoCurrencyCode(),

					debitCurrency, chgFundingAccountCurrency, debitTxnCode, debitAccBranchSortCode, creditTxnCode,
					creditAccBranchSortCode, creditAccountNumber, error);
		}
		if (error)
			handleEvent(40422013, new String[] {});
		logger.info("End of verifyAndPostInternationalPosting");
	}

	private String checkKYC(TransferForecastOrCreateRequest transferForecastOrCreateRequest, Map debitAccDetails,
			Map creditAccDetails, String settlementInstructionId) {
		logger.info("Inside checkKYC method");
		KYCNonScreenHandler kyc = new KYCNonScreenHandler();
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();

		JsonObject data = new JsonObject();
		JsonObject beneficiary_details = new JsonObject();
		JsonObject transactionDetails = new JsonObject();

		data.add("debitor_details", getCustomerDetails((String) debitAccDetails.get("ACCOUNTID")));
		if (creditAccDetails != null && creditAccDetails.get("ACCOUNTID") != null)
			data.add("creditor_details", getCustomerDetails((String) creditAccDetails.get("ACCOUNTID")));

		String beneName = txnInput.getTransferrecipientDtls().getBeneficiaryName();
		String beneAddress = txnInput.getTransferrecipientDtls().getBeneficiaryAddress();
		String beneCountry = txnInput.getTransferrecipientDtls().getBeneficiaryCountry();
		StringBuilder st = new StringBuilder();
		if (SettlementInstructionsUpdator.isValueAvailable(beneName))
			st.append(beneName);
		if (SettlementInstructionsUpdator.isValueAvailable(beneAddress))
			st.append(" " + beneAddress);
		if (SettlementInstructionsUpdator.isValueAvailable(beneCountry))
			st.append(" " + beneCountry);
		addressLines = SettlementInstructionsUpdator.getBenDetails(st);

		beneficiary_details.addProperty("beneficiaryName", addressLines.get("Line1"));
		beneficiary_details.addProperty("beneficiaryAddress", addressLines.get("Line2"));
		beneficiary_details.addProperty("beneficiaryCountry", addressLines.get("Line3"));

		if (txnInput.getBeneficiaryBank().getBankNameAndAddress() != null) {
			beneficiary_details.addProperty("beneficiaryBankCity",
					txnInput.getBeneficiaryBank().getBankNameAndAddress().getCity());
			beneficiary_details.addProperty("beneficiaryBankName",
					txnInput.getBeneficiaryBank().getBankNameAndAddress().getBankName());
			beneficiary_details.addProperty("beneficiaryBankCountry",
					txnInput.getBeneficiaryBank().getBankNameAndAddress().getCountry());
		} else {
			beneficiary_details.addProperty("beneficiaryBankCity", "");
			beneficiary_details.addProperty("beneficiaryBankName", "");
			beneficiary_details.addProperty("beneficiaryBankCountry", "");
		}

		if (txnInput.getBeneficiaryBank().getBankSWIFTorBIC() != null) {
			beneficiary_details.addProperty("beneficiaryBankSWIFTorBIC",
					txnInput.getBeneficiaryBank().getBankSWIFTorBIC());
		} else {
			beneficiary_details.addProperty("beneficiaryBankSWIFTorBIC", "");
		}

		transactionDetails.addProperty("transactionAmount", txnInput.getAmount().getAmount());
		transactionDetails.addProperty("transactionCurrency", txnInput.getAmount().getIsoCurrencyCode());
		transactionDetails.addProperty("branch", BankFusionThreadLocal.getUserSession().getBranchSortCode());

		if (!StringUtils.isEmpty(settlementInstructionId))
			data.add("settlementInstruction_details", getSIDetails(settlementInstructionId));

		data.add("beneficiary_details", beneficiary_details);
		data.add("transactionDetails", transactionDetails);

		String messageId = kyc.callFirco(data.toString(), "IBI_PMT");
		logger.info("=+=  Did Firco call using New Handler  got msg Id -=> " + messageId);
		logger.info("=+=  data sent -=> " + data);
		return messageId;
	}

	private JsonElement getSIDetails(String settlementInstructionId) {
		logger.info("Inside getSIDetails");
		SwiftPaymentDetails spd = getSWIFTDetails(
				ComplianceFinderMethods.findSWIFTSettlementDetailsByInstructionID(settlementInstructionId));

		JsonObject si_Detail = new JsonObject();
		si_Detail.addProperty("identifierCode", spd.getIdentifierCode());
		si_Detail.addProperty("partyIdentifierAccount", spd.getPartyIdentifierAccount());
		si_Detail.addProperty("partyAddressDetails1", spd.getPartyAddressDetails1());
		si_Detail.addProperty("partyAddressDetails2", spd.getPartyAddressDetails2());
		si_Detail.addProperty("partyAddressDetails3", spd.getPartyAddressDetails3());
		si_Detail.addProperty("partyAddressDetails4", spd.getPartyAddressDetails4());
		si_Detail.addProperty("partyIdentifier", spd.getPartyIdentifier());
		si_Detail.addProperty("orderingInstitution", spd.getOrderingInstitution());
		si_Detail.addProperty("orderingInstitutionDetails1", spd.getOrderingInstitutionDetails1());
		si_Detail.addProperty("orderingInstitutionDetails2", spd.getOrderingInstitutionDetails2());
		si_Detail.addProperty("orderingInstitutionDetails3", spd.getOrderingInstitutionDetails3());
		si_Detail.addProperty("payToPartyIdentifier", spd.getPayToPartyIdentifier());
		si_Detail.addProperty("payToPartyIdentifierCode", spd.getPayToPartyIdentifierCode());
		si_Detail.addProperty("payToDetails1", spd.getPayToDetails1());
		si_Detail.addProperty("payToDetails2", spd.getPayToDetails2());
		si_Detail.addProperty("payToDetails3", spd.getPayToDetails3());
		si_Detail.addProperty("payToDetails4", spd.getPayToDetails4());
		si_Detail.addProperty("intermediaryPartyIdentifier", spd.getIntermediaryPartyIdentifier());
		si_Detail.addProperty("intermediaryIdentifierCode", spd.getIntermediaryIdentifierCode());
		si_Detail.addProperty("intermediaryDetails1", spd.getIntermediaryDetails1());
		si_Detail.addProperty("intermediaryDetails2", spd.getIntermediaryDetails2());
		si_Detail.addProperty("intermediaryDetails3", spd.getIntermediaryDetails3());
		si_Detail.addProperty("intermediaryDetails4", spd.getIntermediaryDetails4());
		si_Detail.addProperty("beneficiaryInstitutionPartyIdentifier", spd.getBeneficiaryInstitutionPartyIdentifier());
		si_Detail.addProperty("beneficiaryInstitutionIdentifierCode", spd.getBeneficiaryInstitutionIdentifierCode());
		si_Detail.addProperty("beneficiaryInstitutionDetails1", spd.getBeneficiaryInstitutionDetails1());
		si_Detail.addProperty("beneficiaryCustomerPartyIdentifier", spd.getBeneficiaryCustomerPartyIdentifier());
		si_Detail.addProperty("beneficiaryCustomerIdentifierCode", spd.getBeneficiaryCustomerIdentifierCode());
		si_Detail.addProperty("beneficiaryCustomerDetails1", spd.getBeneficiaryCustomerDetails1());
		si_Detail.addProperty("beneficiaryCustomerDetails2", spd.getBeneficiaryCustomerDetails2());
		si_Detail.addProperty("beneficiaryCustomerDetails3", spd.getBeneficiaryCustomerDetails3());
		si_Detail.addProperty("beneficiaryCustomerDetails4", spd.getBeneficiaryCustomerDetails4());
		si_Detail.addProperty("bankToBankInfoDetails1", spd.getBankToBankInfoDetails1());
		si_Detail.addProperty("bankToBankInfoDetails2", spd.getBankToBankInfoDetails2());
		si_Detail.addProperty("bankToBankInfoDetails3", spd.getBankToBankInfoDetails3());
		si_Detail.addProperty("bankToBankInfoDetails4", spd.getBankToBankInfoDetails4());
		si_Detail.addProperty("bankToBankInfoDetails5", spd.getBankToBankInfoDetails5());
		si_Detail.addProperty("bankToBankInfoDetails6", spd.getBankToBankInfoDetails6());

		return si_Detail;
	}

	public static JsonElement getCustomerDetails(String accountId) {
		logger.info("Inside getCustomerDetails");
		JsonObject customer_Detail = new JsonObject();

		if (accountId == null || "".equalsIgnoreCase(accountId)) {
			return customer_Detail;
		}

		JsonObject personal_Detail = new JsonObject();
		JsonObject enterpise_Detail = new JsonObject();
		String customerCode = FinderMethods.findCustomerCodeByAccount(accountId);
		IBOPersonDetails personDetails = null;
		IBOOrgDetails orgDetails = null;
		HashMap addressDetails = null;
		try {
			personDetails = ComplianceFinderMethods.findPersonDetailsByCustomerId(customerCode);
			orgDetails = ComplianceFinderMethods.findOrgDetailsByCustomerId(customerCode);
			addressDetails = getPersonalCustomerConcatedAddress(customerCode);
		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}

		if (null != personDetails) {
			personal_Detail.addProperty("PassportOrID", getPassportOrID(customerCode));
			personal_Detail.addProperty("Customercode", customerCode);
			personal_Detail.addProperty("Firstname", personDetails.getF_FORENAME());
			personal_Detail.addProperty("Surname", personDetails.getF_SURNAME());
			personal_Detail.addProperty("MiddleName", personDetails.getF_MIDDLENAME());
			personal_Detail.addProperty("MaidenName", personDetails.getF_UBMOTHERMAIDENNAME());
			personal_Detail.addProperty("Nationality", personDetails.getF_NATIONALITY());
			personal_Detail.addProperty("CountryOfResidence", personDetails.getF_COUNTRYOFRESIDENCE());
			personal_Detail.addProperty("DateOfBirth", handleNull(personDetails.getF_DATEOFBIRTH().toString()));
			personal_Detail.addProperty("CountryOfOrigin", personDetails.getF_UBCOUNTRYOFBIRTH());
			personal_Detail.addProperty("CountryOfCitizenship", personDetails.getF_UBCITIZENSHIP());
		}
		try {
			if (null != addressDetails) {
				personal_Detail.addProperty("Address1",
						addressDetails.size() > 0 ? addressDetails.get("ADDRESS0").toString()
								: CommonConstants.EMPTY_STRING);
				personal_Detail.addProperty("Address2",
						addressDetails.size() > 1 ? addressDetails.get("ADDRESS1").toString()
								: CommonConstants.EMPTY_STRING);
				personal_Detail.addProperty("Address3",
						addressDetails.size() > 2 ? addressDetails.get("ADDRESS2").toString()
								: CommonConstants.EMPTY_STRING);
			}
		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}

		if (orgDetails != null)
			enterpise_Detail.addProperty("EmployerName", orgDetails.getF_ORGNAME());

		try {
			enterpise_Detail.addProperty("Tradename", getTradingName(customerCode));
		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}

		customer_Detail.addProperty("accountId", accountId);
		customer_Detail.add("personal_Detail", personal_Detail);
		customer_Detail.add("enterpise_Detail", enterpise_Detail);

		return customer_Detail;
	}

	public static String getPassportOrID(String customerCode) {
		String id = "";
		// To get ID number
		HashMap<String, String> inputMap = null;
		inputMap = new HashMap();
		inputMap.put("custcode", customerCode);
		logger.info("Calling UB_CNF_ConcatTradingNames_SRV - Microflow");
		HashMap CustResults = MFExecuter.executeMF("UB_CNF_ConcatTradingNames_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);

		if (CustResults.get("TradingNames") != null)
			id = CustResults.get("TradingNames").toString();

		return id;
	}

	public static String getTradingName(String customerCode) {
		HashMap<String, String> inputMap = new HashMap();
		VectorTable tradingdetailVec = null;
		String tradingName = "";
		inputMap = new HashMap();

		inputMap.put("CustomerCode", customerCode);
		inputMap.put("Trading", "Trading");
		logger.info("Calling UB_CNF_ReadCustomerDocDetail_SRV - Microflow");
		HashMap CustResults = MFExecuter.executeMF("UB_CNF_ReadCustomerDocDetail_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);

		if (CustResults.get("TradingDetails") != null)
			tradingdetailVec = (VectorTable) CustResults.get("TradingDetails");

		if (tradingdetailVec != null) {
			Object[] TradeNameDesc = (Object[]) tradingdetailVec.getColumn("UBDOCUNIQUEREF");
			StringBuffer tradingDetailBuffer = new StringBuffer();
			if (TradeNameDesc != null) {
				for (Object tradeName : TradeNameDesc) {
					if (tradeName != null) {
						tradingDetailBuffer.append(tradeName.toString());
						tradingDetailBuffer.append(" ");
					}
				}
			}
			tradingName = tradingDetailBuffer.toString();
		}
		return tradingName;
	}

	public static String handleNull(String inputStr) {
		if (inputStr != null)
			return inputStr;
		else
			return "";
	}

	public static HashMap<String, String> getPersonalCustomerConcatedAddress(String customerNo) {
		HashMap<String, String> addressMap = new HashMap<String, String>();
		HashMap<String, String> inputMap = new HashMap<String, String>();
		inputMap.put("CustomerCode", customerNo);
		logger.info("Calling UB_CNF_ReadAddressForEnquiry_SRV - Microflow");
		HashMap results = MFExecuter.executeMF("UB_CNF_ReadAddressForEnquiry_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		VectorTable addressRsVect = (VectorTable) results.get("ResultAddress");
		Object[] addressLine1 = (Object[]) addressRsVect.getColumn("ADDRESS_ADDRESSLINE1");
		Object[] addressLine2 = (Object[]) addressRsVect.getColumn("ADDRESS_ADDRESSLINE2");
		Object[] addressLine3 = (Object[]) addressRsVect.getColumn("ADDRESS_ADDRESSLINE3");
		Object[] addressLine4 = (Object[]) addressRsVect.getColumn("ADDRESS_ADDRESSLINE4");
		Object[] addressLine5 = (Object[]) addressRsVect.getColumn("ADDRESS_ADDRESSLINE5");
		StringBuffer addLineBf = null;
		for (int i = 0; i < addressRsVect.size(); i++) {
			addLineBf = new StringBuffer();
			if (addressLine1[i] != null && addressLine1.length > i) {
				addLineBf.append(addressLine1[i].toString());
				addLineBf.append(" ");
			}
			if (addressLine2[i] != null && addressLine2.length > i) {
				addLineBf.append(addressLine2[i].toString());
				addLineBf.append(" ");
			}
			if (addressLine3[i] != null && addressLine1.length > i) {
				addLineBf.append(addressLine3[i].toString());
				addLineBf.append(" ");
			}
			if (addressLine4[i] != null && addressLine1.length > i) {
				addLineBf.append(addressLine4[i].toString());
				addLineBf.append(" ");
			}
			if (addressLine5[i] != null && addressLine1.length > i) {
				addLineBf.append(addressLine5[i].toString());
				addLineBf.append(" ");
			}
			addressMap.put("ADDRESS" + i, addLineBf.toString());

		}
		return addressMap;
	}

	private String getMisCode(TransferForecastOrCreateRequest transferForecastOrCreateRequest, String dr_Cur,
			String cr_Cur) {
		logger.info("Start of getMisCode");
		String transactionType = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransactionalType();
		String txn_Cur = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getTransferCurrency().getIsoCurrencyCode();
		String transactionCodeForExchangeRate = "";

		logger.debug("transactionType = " + transactionType);
		logger.debug("txn_Cur = " + txn_Cur);
		logger.debug("dr_Cur = " + dr_Cur);
		logger.debug("cr_Cur = " + cr_Cur);

		String dbtTxnCode = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getDebitTxnCode();
		String cdtTxnCode = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getCreditTxnCode();
		//

		if (transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals(MODULEID_CCI)) {
			// INTERNALSOPYMT : Corporate Channel Internal Payment

			if (transactionType.equalsIgnoreCase("INTERNALSOPYMT")) {
				if (txn_Cur.equalsIgnoreCase(dr_Cur)) {
					transactionCodeForExchangeRate = getModuleConfigValue("INTERNAL_MISD",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else if (txn_Cur.equalsIgnoreCase(cr_Cur)) {
					transactionCodeForExchangeRate = getModuleConfigValue("INTERNAL_MISC",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else {
					handleEvent(Integer.parseInt("40009242"), new String[] { " => Transfer Cur - " + txn_Cur
							+ " should be either Debit Currency - " + dr_Cur + " or Credit Currency - " + cr_Cur });
				}
			}
			// INTRAPYMT : Corporate Channel IntraBank Payment
			else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
				if (txn_Cur.equalsIgnoreCase(dr_Cur))
					transactionCodeForExchangeRate = getModuleConfigValue("INTRA_MISD",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				else if (txn_Cur.equalsIgnoreCase(cr_Cur)) {
					transactionCodeForExchangeRate = getModuleConfigValue("INTRA_MISC",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else {
					handleEvent(Integer.parseInt("40009242"), new String[] { " => Transfer Cur - " + txn_Cur
							+ " should be either Debit Currency - " + dr_Cur + " or Credit Currency - " + cr_Cur });
				}
			}
			// INTNAT : Corporate Channel SWIFT Payment
			else if (transactionType.equalsIgnoreCase("INTNAT")) {
				transactionCodeForExchangeRate = getModuleConfigValue("FOREIGN_MISD",
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			}
		} else if (dbtTxnCode != null && dbtTxnCode.trim().length() > 0 && txn_Cur.equalsIgnoreCase(dr_Cur)) {
			transactionCodeForExchangeRate = dbtTxnCode;
		} else if (cdtTxnCode != null && cdtTxnCode.trim().length() > 0 && txn_Cur.equalsIgnoreCase(cr_Cur)) {
			transactionCodeForExchangeRate = cdtTxnCode;
		} else if (transactionType.equalsIgnoreCase("INTNAT")) {
			transactionCodeForExchangeRate = getModuleConfigValue(FOREIGNPYMT,
					transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
		} else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
			if (txn_Cur.equalsIgnoreCase(dr_Cur))
				transactionCodeForExchangeRate = getModuleConfigValue(INTRAPYMT,
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			else if (txn_Cur.equalsIgnoreCase(cr_Cur)) {
				transactionCodeForExchangeRate = getModuleConfigValue(INTRAPYMTCR,
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			} else {
				handleEvent(Integer.parseInt("40009242"), new String[] { " => Transfer Cur - " + txn_Cur
						+ " should be either Debit Currency - " + dr_Cur + " or Credit Currency - " + cr_Cur });
			}
		}
		// Changes for FBIT-2434: Mobile Top UP :Start
		else if (transactionType.equalsIgnoreCase("MOBILETOPUP")) {
			if (txn_Cur.equalsIgnoreCase(dr_Cur))
				transactionCodeForExchangeRate = getModuleConfigValue(MOB_TOP_UP_MIS_DR,
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			else if (txn_Cur.equalsIgnoreCase(cr_Cur)) {
				transactionCodeForExchangeRate = getModuleConfigValue(MOB_TOP_UP_MIS_CR,
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			} else {
				handleEvent(Integer.parseInt("40009242"), new String[] { " => Transfer Cur - " + txn_Cur
						+ " should be either Debit Currency - " + dr_Cur + " or Credit Currency - " + cr_Cur });
			}
		}
		// Changes for FBIT-2434: Mobile Top UP :End
		else {
			if (txn_Cur.equalsIgnoreCase(dr_Cur)) {
				if (isLoanPayment) {
					transactionCodeForExchangeRate = getModuleConfigValue(LOANREPYMT,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else {
					transactionCodeForExchangeRate = getModuleConfigValue(INTERNALPYMT,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
			} else if (txn_Cur.equalsIgnoreCase(cr_Cur)) {
				if (isLoanPayment) {
					transactionCodeForExchangeRate = getModuleConfigValue(LOANREPYMTCR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else {
					transactionCodeForExchangeRate = getModuleConfigValue(INTERNALPYMTCR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
			} else {
				handleEvent(Integer.parseInt("40009242"), new String[] { " => Transfer Cur - " + txn_Cur
						+ " should be either Debit Currency - " + dr_Cur + " or Credit Currency - " + cr_Cur });
			}
		}

		HashMap inputParams = new HashMap();
		inputParams.put("miscode", transactionCodeForExchangeRate);
		logger.info("Calling 100_CheckMISTransCode - Microflow");
		HashMap outputParams = MFExecuter.executeMF("100_CheckMISTransCode",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
		logger.info("End of getMisCode");
		return outputParams.get("ExchangeRateType").toString();
	}

	@Deprecated
	private String getMisCode(TransferForecastOrCreateRequest transferForecastOrCreateRequest) {
		String transactionType = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransactionalType();
		String transactionCodeForExchangeRate = "";
		if (transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals(MODULEID_CCI)) {
			// INTERNALSOPYMT : Corporate Channel Internal Payment

			if (transactionType.equalsIgnoreCase("INTERNALSOPYMT")) {
				transactionCodeForExchangeRate = getModuleConfigValue("INTERNAL_MISD",
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			}
			// INTRAPYMT : Corporate Channel IntraBank Payment
			else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
				transactionCodeForExchangeRate = getModuleConfigValue("INTRA_MISD",
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			}
			// INTNAT : Corporate Channel SWIFT Payment
			else if (transactionType.equalsIgnoreCase("INTNAT")) {
				transactionCodeForExchangeRate = getModuleConfigValue("FOREIGN_MISD",
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			}
		} else {

			if (transactionType.equalsIgnoreCase("INTNAT")) {
				transactionCodeForExchangeRate = getModuleConfigValue("FOREIGN_MISDR",
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			} else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
				transactionCodeForExchangeRate = getModuleConfigValue("INTRA_MISDR",
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			} else {
				transactionCodeForExchangeRate = getModuleConfigValue("INTERNAL_MISDR",
						transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
			}
		}
		HashMap inputParams = new HashMap();
		inputParams.put("miscode", transactionCodeForExchangeRate);
		logger.info("Calling 100_CheckMISTransCode - Microflow");
		HashMap outputParams = MFExecuter.executeMF("100_CheckMISTransCode",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
		return outputParams.get("ExchangeRateType").toString();
	}

	private HashMap fetchOnlinecharges(TransferForecastOrCreateRequest transferForecastOrCreateRequest,
			BankFusionEnvironment env) {
		logger.info(" Start of fetchOnlinecharges ");
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();
		String chgFundingAccount = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp().getChargeFundingAccount();

		String transactionCode = getTransactionCode(transferForecastOrCreateRequest, "debit");
		HashMap inputParams = new HashMap();
		Map accDetails = new HashMap();
		accDetails = UB_IBI_PaymentsHelper.getAccountDetails(txnInput.getFromMyAccount());
		String acctCurrency = (String) accDetails.get("ISOCURRENCYCODE");
		BigDecimal exchangeAmountValue = getExchangeAmount(env, txnInput.getTransferCurrency().getIsoCurrencyCode(),
				acctCurrency, txnInput.getAmount().getAmount());
		if (null != chgFundingAccount && !chgFundingAccount.isEmpty())
			inputParams.put("FUNDINGACCOUNT", chgFundingAccount);
		inputParams.put("1_postingMessageAccountId", txnInput.getFromMyAccount());
		inputParams.put("1_postingMessageISOCurrencyCode", acctCurrency);
		inputParams.put("1_postingMessageTransactionAmount", exchangeAmountValue);
		inputParams.put("TxnCurrency", txnInput.getTransferCurrency().getIsoCurrencyCode());
		inputParams.put("1_postingMessageTransactionCode", transactionCode);
		inputParams.put("1_contraAccountNumber", creditAccountNumber);
		logger.info("Calling UB_CHG_CalculateOnlineCharges_SRV - Microflow");
		HashMap outputParams = MFExecuter.executeMF("UB_CHG_CalculateOnlineCharges_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
		logger.info(" End of fetchOnlinecharges ");
		return outputParams;
	}

	public BigDecimal getExchangeAmount(BankFusionEnvironment env, String fromCurr, String toCurr, BigDecimal txnAmt) {
		BigDecimal exchangeAmount = BigDecimal.ZERO;
		HashMap inputParams = new HashMap();
		String DEFAULT_EXCHANGE_RATE_TYPE = "DefaultExRateType";
		String MODULE_NAME_CBS = "CBS";
		String chargeExchangeRateTypeVal = ModuleConfiguration.getInstance()
				.getModuleConfigurationValue(MODULE_NAME_CBS, DEFAULT_EXCHANGE_RATE_TYPE).toString();
		CalcExchangeRateRq rq = new CalcExchangeRateRq();
		CalcExchRateDetails calcExchRateDetails = new CalcExchRateDetails();
		calcExchRateDetails.setBuyAmount(txnAmt);
		calcExchRateDetails.setBuyCurrency(fromCurr);
		calcExchRateDetails.setSellCurrency(toCurr);
		ExchangeRateDetails exchangeRateDetails = new ExchangeRateDetails();
		exchangeRateDetails.setExchangeRateType(chargeExchangeRateTypeVal);
		calcExchRateDetails.setExchangeRateDetails(exchangeRateDetails);
		rq.setCalcExchRateDetails(calcExchRateDetails);
		inputParams.put("CalcExchangeRateRq", rq);
		logger.info(" Calling CB_FEX_CalculateExchangeRateAmount_SRV - Microflow ");
		HashMap outputParams = MFExecuter.executeMF("CB_FEX_CalculateExchangeRateAmount_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
		CalcExchangeRateRs response = (CalcExchangeRateRs) outputParams.get("CalcExchangeRateRs");
		exchangeAmount = response.getCalcExchRateResults().getSellAmountDetails().getAmount();
		return exchangeAmount;
	}

	private String getTransactionCode(TransferForecastOrCreateRequest transferForecastOrCreateRequest, String type) {
		logger.info(" Start of getTransactionCode ");
		String transactionType = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransactionalType();
		String transactionCode = "";
		if (type.equalsIgnoreCase("debit")) {
			String debitTxnCode = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
					.getTransferForecastInp().getDebitTxnCode();
			if (debitTxnCode != null && debitTxnCode != CommonConstants.EMPTY_STRING) {
				transactionCode = debitTxnCode;
			} else if (transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals(MODULEID_CCI)) {
				// INTERNALSOPYMT : Corporate Channel Internal Payment
				if (transactionType.equalsIgnoreCase("INTERNALSOPYMT")) {
					transactionCode = getModuleConfigValue("INTERNAL_MISD",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// INTRAPYMT : Corporate Channel IntraBank Payment
				else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
					transactionCode = getModuleConfigValue("INTRA_MISD",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// INTNAT : Corporate Channel SWIFT Payment
				else if (transactionType.equalsIgnoreCase("INTNAT")) {
					transactionCode = getModuleConfigValue("FOREIGN_MISD",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else {
					transactionCode = getModuleConfigValue(INTERNALPYMT,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
			} else {
				if (transactionType.equalsIgnoreCase("INTNAT")) {
					transactionCode = getModuleConfigValue(FOREIGNPYMT,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
					transactionCode = getModuleConfigValue(INTRAPYMT,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else if (transactionType.equalsIgnoreCase("DOMESTICPYMT")) {
					transactionCode = getModuleConfigValue(DOMESTICPYMT,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// Changes for FBIT-2434: Mobile Top UP : Start
				else if (transactionType.equalsIgnoreCase("MOBILETOPUP")) {
					transactionCode = getModuleConfigValue(MOB_TOP_UP_MIS_DR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// Changes for FBIT-2434: Mobile Top UP : End
				else {
					String creditAccountId = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
							.getTransferForecastInp().getToMyAccount();
					if (UB_IBI_PaymentsHelper.isLoanAccount(creditAccountId)) {
						isLoanPayment = true;
						transactionCode = getModuleConfigValue(LOANREPYMT,
								transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
					} else {
						transactionCode = getModuleConfigValue(INTERNALPYMT,
								transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
					}
				}
			}
		} else if (type.equalsIgnoreCase("credit")) {
			String creditTxnCode = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
					.getTransferForecastInp().getCreditTxnCode();
			if (creditTxnCode != null && creditTxnCode != CommonConstants.EMPTY_STRING) {
				transactionCode = creditTxnCode;
			} else if (transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals(MODULEID_CCI)) {
				// INTERNALSOPYMT : Corporate Channel Internal Payment
				if (transactionType.equalsIgnoreCase("INTERNALSOPYMT")) {
					transactionCode = getModuleConfigValue("INTERNAL_MISC",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// INTRAPYMT : Corporate Channel IntraBank Payment
				else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
					transactionCode = getModuleConfigValue("INTRA_MISC",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// INTNAT : Corporate Channel SWIFT Payment
				else if (transactionType.equalsIgnoreCase("INTNAT")) {
					transactionCode = getModuleConfigValue("FOREIGN_MISC",
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else {
					transactionCode = getModuleConfigValue(INTERNALPYMTCR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
			} else {
				if (transactionType.equalsIgnoreCase("INTNAT")) {
					transactionCode = getModuleConfigValue(FOREIGNPYMTCR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else if (transactionType.equalsIgnoreCase("INTRAPYMT")) {
					transactionCode = getModuleConfigValue(INTRAPYMTCR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				} else if (transactionType.equalsIgnoreCase("DOMESTICPYMT")) {
					transactionCode = getModuleConfigValue(DOMESTICPYMTCR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// Changes for FBIT-2434: Mobile Top UP : Start
				else if (transactionType.equalsIgnoreCase("MOBILETOPUP")) {
					transactionCode = getModuleConfigValue(MOB_TOP_UP_MIS_CR,
							transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
				}
				// Changes for FBIT-2434: Mobile Top UP : End
				else {
					if (isLoanPayment) {
						transactionCode = getModuleConfigValue(LOANREPYMTCR,
								transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());

					} else {
						transactionCode = getModuleConfigValue(INTERNALPYMTCR,
								transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId());
					}
				}
			}
		}
		logger.info(" End of getTransactionCode ");
		return transactionCode;
	}

	public static byte[] getOBJECTValue(Object value) {
		if (value instanceof String && ((String) value).equals("")) {
			return null;
		}
		try {
			String values = value.toString();
			byte[] bytes = values.getBytes();
			return bytes;
		} catch (ClassCastException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
			return null;
			// LOG.warn(value + " " + e.getMessage());
			// return BankFusionIOSupport.convertToBytes(value);
		}
	}

	private CreateSettlementInstructionsRs getSSIDtls(TransferForecastOrCreateRequest transferForecastOrCreateRequest,
			BankFusionEnvironment env) {
		logger.info("Start of getSSIDtls method");
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();
		RqHeader rqHeader = transferForecastOrCreateRequest.getRqHeader();
		String CustomerId = rqHeader.getOrig().getAppId();
		String charges = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp()
				.getCharges();

		/*
		 * Calling SSI to create SSI if not available or sent the existing SSI details
		 */
		CreateSettlementInstructionsRq ssiDetails = new CreateSettlementInstructionsRq();
		CreateSettlementInstructionsInputRq createSettlementInstructionsInputRq = new CreateSettlementInstructionsInputRq();
		AccountKeys acctDtls = new AccountKeys();
		acctDtls.setExternalAccountId(txnInput.getTransferrecipientDtls().getOtherAccount());
		acctDtls.setIBAN(txnInput.getTransferrecipientDtls().getIBANAccount());

		/* Bank Dtls */
		NameAndAddress nameAndAddressDtls = new NameAndAddress();
		if (txnInput.getBeneficiaryBank().getBankNameAndAddress() != null) {
			String benbankName = txnInput.getBeneficiaryBank().getBankNameAndAddress().getBankName();
			String benbankCity = txnInput.getBeneficiaryBank().getBankNameAndAddress().getCity();
			String benbankcountry = txnInput.getBeneficiaryBank().getBankNameAndAddress().getCountry();
			nameAndAddressDtls = UB_IBI_PaymentsHelper.formatBenBankDtls(benbankName, benbankCity, benbankcountry);
		}
		/* Beneficiary Dtls */
		NameAndAddress beneNameAndAddressDtls = new NameAndAddress();
		if (transferForecastOrCreateRequest.getRqHeader().getOrig().getChannelId().equals(MODULEID_CCI)) {
			String beneName = txnInput.getTransferrecipientDtls().getAddressLine1();
			String beneAddress1 = txnInput.getTransferrecipientDtls().getAddressLine2();
			String beneAddress2 = txnInput.getTransferrecipientDtls().getAddressLine3();
			String beneAddress3 = txnInput.getTransferrecipientDtls().getAddressLine4();
			beneNameAndAddressDtls.setName(beneName);
			beneNameAndAddressDtls.setTextLine1(beneAddress1);
			beneNameAndAddressDtls.setTextLine2(beneAddress2);
			beneNameAndAddressDtls.setTextLine3(beneAddress3);
		} else {
			String beneName = txnInput.getTransferrecipientDtls().getBeneficiaryName();
			String beneAddress = txnInput.getTransferrecipientDtls().getBeneficiaryAddress();
			String beneCountry = txnInput.getTransferrecipientDtls().getBeneficiaryCountry();
			StringBuilder st = new StringBuilder();
			if (SettlementInstructionsUpdator.isValueAvailable(beneName))
				st.append(beneName);
			if (SettlementInstructionsUpdator.isValueAvailable(beneAddress))
				st.append(" " + beneAddress);
			if (SettlementInstructionsUpdator.isValueAvailable(beneCountry))
				st.append(" " + beneCountry);
			addressLines = SettlementInstructionsUpdator.getBenDetails(st);
			beneNameAndAddressDtls.setName(addressLines.get("Line1"));
			beneNameAndAddressDtls.setTextLine1(addressLines.get("Line2"));
			beneNameAndAddressDtls.setTextLine2(addressLines.get("Line3"));
		}
		createSettlementInstructionsInputRq.setFromAccount(txnInput.getFromMyAccount());
		createSettlementInstructionsInputRq.setCustomerID(CustomerId);
		createSettlementInstructionsInputRq.setBeneAccountOrIban(acctDtls);
		createSettlementInstructionsInputRq.setBeneBankNameAndAddress(nameAndAddressDtls);
		if (txnInput.getBeneficiaryBank().getBankSWIFTorBIC() != null) {
			createSettlementInstructionsInputRq
					.setBeneBankSwiftOrBicCode(txnInput.getBeneficiaryBank().getBankSWIFTorBIC());
		}
		createSettlementInstructionsInputRq.setBeneNameAndAddress(beneNameAndAddressDtls);
		createSettlementInstructionsInputRq.setIsoCurrencyCode(txnInput.getTransferCurrency().getIsoCurrencyCode());
		createSettlementInstructionsInputRq.setPaymentRef(txnInput.getPaymentReference());
		ssiDetails.setCreateSettlementInstructionsInputRq(createSettlementInstructionsInputRq);
		CreateorEnquireSettlementInstruction createOrFetchSSIDtls = new CreateorEnquireSettlementInstruction(env);
		createOrFetchSSIDtls.setChargeCode(charges);
		createOrFetchSSIDtls.setF_IN_createSettlementInstructionRq(ssiDetails);
		createOrFetchSSIDtls.setTransferForecastOrCreateRequest(transferForecastOrCreateRequest);
		createOrFetchSSIDtls.process(env);
		CreateSettlementInstructionsRs createSettlementInstructionRs = createOrFetchSSIDtls
				.getF_OUT_createSettlementInstructionRs();
		logger.info("End of getSSIDtls method");
		return createSettlementInstructionRs;
	}

	public String isAccountPasswordProtected(String accountId, String accountType) {
		int accRightIndicator = 0;
		Map<String, String> map = new HashMap<String, String>();
		Map outPutMap = new HashMap();
		map.put("AccountID", accountId);
		outPutMap = UB_IBI_PaymentsHelper.getAccountDetails(accountId);
		if (accountType.equalsIgnoreCase(IfmConstants.DR)) {
			if (outPutMap.get("ACCRIGHTSINDICATOR") != null) {
				accRightIndicator = (Integer) outPutMap.get("ACCRIGHTSINDICATOR");
				if (accRightIndicator == 1 || accRightIndicator == 9)
					return "40007319";
				if (accRightIndicator == -1)
					return "40007318";
				if (accRightIndicator == 2)
					return "40007321";
				if (accRightIndicator == 3)
					return "40112172";
				if (accRightIndicator == 4)
					return "40007322";
				if (accRightIndicator == 5)
					return "40007323";
				else {
					return "";
				}
			} else {
				return "";
			}
		} else {
			if (outPutMap.get("ACCRIGHTSINDICATOR") != null) {
				accRightIndicator = (Integer) outPutMap.get("ACCRIGHTSINDICATOR");
				if (accRightIndicator == 1 || accRightIndicator == 9)
					return "40007319";
				if (accRightIndicator == -1)
					return "40007318";
				if (accRightIndicator == 2)
					return "40007321";
				if (accRightIndicator == 3)
					return "40112172";
				if (accRightIndicator == 6)
					return "40409356";
				if (accRightIndicator == 7)
					return "40007325";
				else {
					return "";
				}
			} else {
				return "";
			}
		}
	}

	public static String getModuleConfigValue(String Value, String channelID) {
		String value = "";
		HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
		ModuleKeyRq module = new ModuleKeyRq();
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		if (channelID.equals(MODULEID_CCI)) {
			module.setModuleId(MODULEID_CCI);
		} else {
			module.setModuleId(MODULEID);

		}
		module.setKey(Value);
		read.setModuleKeyRq(module);
		moduleParams.put("ReadModuleConfigurationRq", read);
		logger.info("Calling CB_CMN_ReadModuleConfiguration_SRV - Microflow");
		HashMap valueFromModuleConfiguration = MFExecuter.executeMF(READ_MODULE_CONFIGURATION,
				BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
		if (valueFromModuleConfiguration != null) {
			ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration
					.get("ReadModuleConfigurationRs");
			value = rs.getModuleConfigDetails().getValue().toString();
		}
		return value;
	}

	private Boolean validateTxnAmount(BigDecimal amount, String currency) {
		int scaleVal = 0;
		int fractionalVal = 0;
		if (amount == null || amount.compareTo(new BigDecimal(0)) == 0) {
			return false;
		}
		String txnAmtString = amount.toString();
		int index = txnAmtString.indexOf(".");
		if (index == -1) {
			fractionalVal = 0;
		} else {
			fractionalVal = txnAmtString.substring(index + 1).length();
		}

		scaleVal = bizInfo.getCurrencyScale(currency, BankFusionThreadLocal.getBankFusionEnvironment());

		if (scaleVal != fractionalVal) {
			return false;
		}
		return true;
	}

	public static void handleEvent(Integer eventNumber, String[] args) {
		logger.info("Inside handleEvent - " + eventNumber);
		if (args == null) {
			args = new String[] { CommonConstants.EMPTY_STRING };
		}
		Event event = new Event();
		event.setEventNumber(eventNumber);
		event.setMessageArguments(args);
		IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance()
				.getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
		businessEventsService.handleEvent(event);
	}

	public IBOAccount getAccountCust(String accountNo) {
		IBOAccount accountDtl = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountNo, true);
		return accountDtl;
	}

	private BigDecimal getDebitAmount(TransferForecastOrCreateRequest transferForecastOrCreateRequest,
			String debitCurrency) {
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();
		String txnCurrency = txnInput.getTransferCurrency().getIsoCurrencyCode();
		BigDecimal txnAmtInTxnCurrency = txnInput.getAmount().getAmount();
		BigDecimal debitAmount = CommonConstants.BIGDECIMAL_ZERO;
		// String exchangeRate = getMisCode(transferForecastOrCreateRequest);
		String exchangeRate = getMisCode(transferForecastOrCreateRequest, debitCurrency, txnCurrency);
		if (!(debitCurrency.equalsIgnoreCase(txnCurrency))) {
			debitAmount = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, debitCurrency, txnAmtInTxnCurrency,
					exchangeRate, true);
			return debitAmount;
		} else {
			debitAmount = txnAmtInTxnCurrency;
			return debitAmount;
		}
	}

	public static SwiftPaymentDetails getSWIFTDetails(IBOSWTSettlementInstructionDetail swiftInstructionDetail) {
		logger.info("Start of getSWIFTDetails");
		SwiftPaymentDetails swiftPaymentDetails = new SwiftPaymentDetails();
		IBOBicCodes bicDetaials = null;

		// bank to bank
		swiftPaymentDetails.setBankToBankInfoDetails1(swiftInstructionDetail.getF_BANK_TO_BANK_INFO1());
		swiftPaymentDetails.setBankToBankInfoDetails2(swiftInstructionDetail.getF_BANK_TO_BANK_INFO2());
		swiftPaymentDetails.setBankToBankInfoDetails3(swiftInstructionDetail.getF_BANK_TO_BANK_INFO3());
		swiftPaymentDetails.setBankToBankInfoDetails4(swiftInstructionDetail.getF_BANK_TO_BANK_INFO4());
		swiftPaymentDetails.setBankToBankInfoDetails5(swiftInstructionDetail.getF_BANK_TO_BANK_INFO5());
		swiftPaymentDetails.setBankToBankInfoDetails6(swiftInstructionDetail.getF_BANK_TO_BANK_INFO6());
		// ben inst

		bicDetaials = ComplianceFinderMethods
				.findBicCodeDetailsByBiccodeId(swiftInstructionDetail.getF_BENEFICIARY_CODE());
		if (bicDetaials != null) {
			swiftPaymentDetails.setBeneficiaryInstitutionDetails1(bicDetaials.getF_NAME());
			swiftPaymentDetails.setBeneficiaryInstitutionDetails1(bicDetaials.getF_CITY());
			swiftPaymentDetails.setBeneficiaryInstitutionDetails1(bicDetaials.getF_LOCATION());
		} else {
			swiftPaymentDetails.setBeneficiaryInstitutionDetails1(swiftInstructionDetail.getF_BENEFICIARY_TEXT1());
			swiftPaymentDetails.setBeneficiaryInstitutionDetails2(swiftInstructionDetail.getF_BENEFICIARY_TEXT2());
			swiftPaymentDetails.setBeneficiaryInstitutionDetails3(swiftInstructionDetail.getF_BENEFICIARY_TEXT3());
			swiftPaymentDetails.setBeneficiaryInstitutionDetails4(swiftInstructionDetail.getF_BENEFICIARY_TEXT4());
		}
		swiftPaymentDetails.setBeneficiaryInstitutionIdentifierCode(swiftInstructionDetail.getF_BENEFICIARY_CODE());
		swiftPaymentDetails
				.setBeneficiaryInstitutionPartyIdentifier(swiftInstructionDetail.getF_BENEFICIARY_PARTY_IDENTIFIER());

		// ben cust

		bicDetaials = ComplianceFinderMethods
				.findBicCodeDetailsByBiccodeId(swiftInstructionDetail.getF_FOR_ACC_IDENTIFIERCODE());
		if (bicDetaials != null) {
			swiftPaymentDetails.setBeneficiaryCustomerDetails1(bicDetaials.getF_NAME());
			swiftPaymentDetails.setBeneficiaryCustomerDetails2(bicDetaials.getF_CITY());
			swiftPaymentDetails.setBeneficiaryCustomerDetails3(bicDetaials.getF_LOCATION());

		} else {

			swiftPaymentDetails.setBeneficiaryCustomerDetails1(swiftInstructionDetail.getF_FOR_ACCOUNT_TEXT1());
			swiftPaymentDetails.setBeneficiaryCustomerDetails2(swiftInstructionDetail.getF_FOR_ACCOUNT_TEXT2());
			swiftPaymentDetails.setBeneficiaryCustomerDetails3(swiftInstructionDetail.getF_FOR_ACCOUNT_TEXT3());
			swiftPaymentDetails.setBeneficiaryCustomerDetails4(swiftInstructionDetail.getF_FOR_ACCOUNT_TEXT4());
		}

		swiftPaymentDetails.setBeneficiaryCustomerIdentifierCode(swiftInstructionDetail.getF_FOR_ACC_IDENTIFIERCODE());
		swiftPaymentDetails
				.setBeneficiaryCustomerPartyIdentifier(swiftInstructionDetail.getF_FOR_ACCOUNT_PARTY_IDENTIFIER());

		// inter

		bicDetaials = ComplianceFinderMethods
				.findBicCodeDetailsByBiccodeId(swiftInstructionDetail.getF_INTERMEDIARY_CODE());
		if (bicDetaials != null) {
			swiftPaymentDetails.setIntermediaryDetails1(bicDetaials.getF_NAME());
			swiftPaymentDetails.setIntermediaryDetails2(bicDetaials.getF_CITY());
			swiftPaymentDetails.setIntermediaryDetails3(bicDetaials.getF_LOCATION());

		} else {
			swiftPaymentDetails.setIntermediaryDetails1(swiftInstructionDetail.getF_INTERMEDIARY_TEXT1());
			swiftPaymentDetails.setIntermediaryDetails2(swiftInstructionDetail.getF_INTERMEDIARY_TEXT2());
			swiftPaymentDetails.setIntermediaryDetails3(swiftInstructionDetail.getF_INTERMEDIARY_TEXT3());
			swiftPaymentDetails.setIntermediaryDetails4(swiftInstructionDetail.getF_INTERMEDIARY_TEXT4());
		}

		swiftPaymentDetails.setIntermediaryIdentifierCode(swiftInstructionDetail.getF_INTERMEDIARY_CODE());
		swiftPaymentDetails.setIntermediaryPartyIdentifier(swiftInstructionDetail.getF_INTERMEDIARY_PARTY_IDENTIFIER());

		// pay to
		bicDetaials = ComplianceFinderMethods.findBicCodeDetailsByBiccodeId(swiftInstructionDetail.getF_PAY_TO_CODE());
		if (bicDetaials != null) {
			swiftPaymentDetails.setPayToDetails1(bicDetaials.getF_NAME());
			swiftPaymentDetails.setPayToDetails2(bicDetaials.getF_CITY());
			swiftPaymentDetails.setPayToDetails3(bicDetaials.getF_LOCATION());
		} else {
			swiftPaymentDetails.setPayToDetails1(swiftInstructionDetail.getF_PAY_TO_TEXT1());
			swiftPaymentDetails.setPayToDetails2(swiftInstructionDetail.getF_PAY_TO_TEXT2());
			swiftPaymentDetails.setPayToDetails3(swiftInstructionDetail.getF_PAY_TO_TEXT3());
			swiftPaymentDetails.setPayToDetails4(swiftInstructionDetail.getF_PAY_TO_TEXT4());
		}
		swiftPaymentDetails.setPayToPartyIdentifierCode(swiftInstructionDetail.getF_PAY_TO_CODE());
		swiftPaymentDetails.setPayToPartyIdentifier(swiftInstructionDetail.getF_PAY_TO_PARTY_IDENTIFIER());

		// ordering institution

		swiftPaymentDetails.setOrderingInstitution(swiftInstructionDetail.getF_ORDERINGINSTITUTION());

		bicDetaials = ComplianceFinderMethods
				.findBicCodeDetailsByBiccodeId(swiftInstructionDetail.getF_ORDERINGINSTITUTION());
		if (bicDetaials != null) {
			swiftPaymentDetails.setOrderingInstitutionDetails1(bicDetaials.getF_NAME());
			swiftPaymentDetails.setOrderingInstitutionDetails2(bicDetaials.getF_CITY());
			swiftPaymentDetails.setOrderingInstitutionDetails3(bicDetaials.getF_LOCATION());

		} else {
			swiftPaymentDetails.setOrderingInstitutionDetails1(swiftInstructionDetail.getF_ORDERINGINSTITUTIONDTL1());
			swiftPaymentDetails.setOrderingInstitutionDetails2(swiftInstructionDetail.getF_ORDERINGINSTITUTIONDTL2());
			swiftPaymentDetails.setOrderingInstitutionDetails3(swiftInstructionDetail.getF_ORDERINGINSTITUTIONDTL3());
			swiftPaymentDetails.setOrderingInstitutionDetails4(swiftInstructionDetail.getF_ORDERINGINSTITUTIONDTL4());
		}

		// party

		swiftPaymentDetails.setPartyIdentifierAccount(swiftInstructionDetail.getF_ORDERINGCUSTOMERACCID());
		swiftPaymentDetails.setIdentifierCode(swiftInstructionDetail.getF_ORDERINGCUST_IDENTIFIERCODE());

		swiftPaymentDetails.setPartyIdentifier(swiftInstructionDetail.getF_PARTYIDENTIFIER());
		bicDetaials = ComplianceFinderMethods
				.findBicCodeDetailsByBiccodeId(swiftInstructionDetail.getF_ORDERINGCUST_IDENTIFIERCODE());
		if (bicDetaials != null) {
			swiftPaymentDetails.setPartyAddressDetails1(bicDetaials.getF_NAME());
			swiftPaymentDetails.setPartyAddressDetails2(bicDetaials.getF_CITY());
			swiftPaymentDetails.setPartyAddressDetails3(bicDetaials.getF_LOCATION());
		} else {

			swiftPaymentDetails.setPartyAddressDetails1(swiftInstructionDetail.getF_PARTYADDRESSLINE1());
			swiftPaymentDetails.setPartyAddressDetails2(swiftInstructionDetail.getF_PARTYADDRESSLINE2());
			swiftPaymentDetails.setPartyAddressDetails3(swiftInstructionDetail.getF_PARTYADDRESSLINE3());
			swiftPaymentDetails.setPartyAddressDetails4(swiftInstructionDetail.getF_PARTYADDRESSLINE4());
		}
		logger.info("End of getSWIFTDetails");
		return swiftPaymentDetails;
	}

	private String buildResMsg(TransferForecastOrCreateRequest transferForecastOrCreateRequest) {
		logger.info("Inside buildResMsg method");
		TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput()
				.getTransferForecastInp();

		String transactionId = transactionID;
		// IBANAccount= Mobile No (FFC populated tag value)
		String mobileNo = txnInput.getTransferrecipientDtls().getIBANAccount();
		String amount = txnInput.getAmount().getAmount().toString();
		String transferCurrency = txnInput.getTransferCurrency().getIsoCurrencyCode();
		String serviceProviderId = txnInput.getTransferrecipientDtls().getOtherAccount();
		String transferDate = txnInput.getTransferDate().toString();

		StringBuilder responseMsgBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
				.append("\n <v1r0:MobileTopUpRequest xmlns:v1r0=\"http://www.misys.com/cbs/msgs/v1r0\"")
				.append(" xmlns:typ=\"http://www.misys.com/cbs/types\">").append("\n 	<v1r0:mobileTopUpRequest>")
				.append("\n  		<typ:transactionId>TRANSACTION_ID_REPLACE_STR</typ:transactionId>")
				.append("\n  		<typ:mobileNumber>MOBILE_NO_REPLACE_STR</typ:mobileNumber>")
				.append("\n  		<typ:amount>AMOUNT_REPLACE_STR</typ:amount>")
				.append("\n  		<typ:transferCurrency>TRANSFER_CUR_REPLACE_STR</typ:transferCurrency>")
				.append("\n  		<typ:serviceProvider>SERVICE_PROVIDER_REPLACE_STR</typ:serviceProvider>")
				.append("\n  		<typ:transferDate>TRANSFER_DATE_REPLACE_STR</typ:transferDate>")
				.append("\n 	</v1r0:mobileTopUpRequest>").append("\n </v1r0:MobileTopUpRequest>");

		String responseMsg = responseMsgBuilder.toString();
		responseMsg = responseMsg.replace("TRANSACTION_ID_REPLACE_STR", transactionId);
		responseMsg = responseMsg.replace("MOBILE_NO_REPLACE_STR", mobileNo);
		responseMsg = responseMsg.replace("AMOUNT_REPLACE_STR", amount);
		responseMsg = responseMsg.replace("TRANSFER_CUR_REPLACE_STR", transferCurrency);
		responseMsg = responseMsg.replace("SERVICE_PROVIDER_REPLACE_STR", serviceProviderId);
		responseMsg = responseMsg.replace("TRANSFER_DATE_REPLACE_STR", transferDate);
		return responseMsg;
	}

	private void postToServiceProviderQueue(String message, String queueEndpoint) {
		logger.info("message sent from Essence is \n" + message);
		logger.info("---- Posting the message in the following queue " + queueEndpoint);
		MessageProducerUtil.sendMessage(message, queueEndpoint);
	}
	// Changes for FBIT-2434: Mobile Top UP : End

	/**
	 * @param channelId
	 * @param messageType
	 * @param txnReference
	 * @return
	 */
	private String getUETR(String channelId, String messageType, String txnReference) {
		UB_SWT_GenerateUETR uetrFatom = new UB_SWT_GenerateUETR();
		uetrFatom.setF_IN_Channel(channelId);
		uetrFatom.setF_IN_MessageType(messageType);
		uetrFatom.setF_IN_TxnReference(txnReference);
		uetrFatom.process(BankFusionThreadLocal.getBankFusionEnvironment());
		return uetrFatom.getF_OUT_UETR();
	}

	private String getBenFicAccNo(String providerID) {
		String benFicAccNo = CommonConstants.EMPTY_STRING;
		IBOCB_BIL_BILLERINFO dbRow = getDBRow(providerID);
		if (dbRow != null) {
			benFicAccNo = dbRow.getF_BENEFICIARYACCNO();
		}
		return benFicAccNo;
	}

	private IBOCB_BIL_BILLERINFO getDBRow(String providerID) {
		ArrayList<String> params = new ArrayList<String>();
		params.add(providerID);
		IBOCB_BIL_BILLERINFO dbRow = (IBOCB_BIL_BILLERINFO) BankFusionThreadLocal.getPersistanceFactory()
				.findByPrimaryKey(IBOCB_BIL_BILLERINFO.BONAME, providerID, Boolean.FALSE);
		return dbRow;
	}
}