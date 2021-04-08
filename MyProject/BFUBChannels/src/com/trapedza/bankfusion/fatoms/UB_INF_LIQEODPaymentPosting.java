package com.trapedza.bankfusion.fatoms;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.PropertyValueException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.ub.types.treasurycollectionpayement.IBOFinancialPostingMsg;
import bf.com.misys.ub.types.treasurycollectionpayement.IBOFinancialPostingMsgList;

import com.google.gson.Gson;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.exception.RetriableException;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.batchgateway.persistence.PrivatePersistenceFactory;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_CONTROLFILEDETAILS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_INCOMINGPOSTINGDETAILS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_LIQMessageDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_TRANSACTIONFILEDETAILS;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.events.ErrorEvent;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.utils.FatomUtils;
import com.trapedza.bankfusion.utils.GUIDGen;

import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.features.FeatureIDs;
import com.trapedza.bankfusion.servercommon.products.ProductFactoryProvider;
import com.trapedza.bankfusion.servercommon.products.SimpleRuntimeProduct;

/**
 * @author Prateek File to post the message received from Loan IQ
 */
public class UB_INF_LIQEODPaymentPosting {

	private static final transient Log logger = LogFactory.getLog(UB_INF_LIQEODPaymentPosting.class.getName());

	static final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";

	private static final String CB_CMN_COLLECTIONPOSTING_MESSAGE_SRV = "CB_CMN_CollectionPostingMessage_SRV";

	private static final String FAILURE_POSTING_ACTION_FORCE_POST = "Force Post";

	private static final String FAILURE_POSTING_ACTION_REJECT = "Reject";

	private static final String FAILURE_POSTING_ACTION_POST_TO_SUSP = "Post to Suspense";

	private final IServiceManager SERVICE_MANAGER = ServiceManagerFactory.getInstance().getServiceManager();

	private final IPersistenceService PERSISTENCE_SERVICE = (IPersistenceService) SERVICE_MANAGER
			.getServiceForName(ServiceManager.PERSISTENCE_SERVICE);

	private final IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
			.getInstance().getServiceManager()
			.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

	private boolean forcePostFlag = false;
	int errorCode;
	int statusCode;
	String fileName;
	String transactionFileName;
	String flag = "N";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void postMessage(Exchange exchange) {
		
		String messageStatus = null;
		int msgCode = 0;
		boolean error = false;
		errorCode = 0;
		statusCode = 0;
		fileName = "";
		IBOFinancialPostingMsgList postingMsgIBOList = new IBOFinancialPostingMsgList();
		IBOFinancialPostingMsg postingMsg = new IBOFinancialPostingMsg();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		LIQPostingResponse liqReponse = new LIQPostingResponse();
		List<LIQPostingResponse> responseList = new ArrayList<>();
		LIQDDAPostingResponse liqDdaResponse = new LIQDDAPostingResponse();
		ArrayList<IBOFinancialPostingMessage> postingMsgList = new ArrayList<>();
		String transactionID = GUIDGen.getNewGUID();
		String messageId = GUIDGen.getNewGUID();
		List<SimplePersistentObject> listIncomingPostingDetails = null;
		HashMap allAccountsDetails = new HashMap<String, HashMap<String, Object>>();

		logger.info("Start of UB_INF_LIQEODPaymentPosting :::");

		try {

			Message params = exchange.getIn();
			String requestMsg = params.getBody().toString();
			dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource inputsource = new InputSource(new StringReader(requestMsg));
			Document doc = dBuilder.parse(inputsource);

			doc.getDocumentElement().normalize();
			logger.info("Start of LIQPaymentPosting ");

			NodeList list = doc.getElementsByTagName("IBOFinancialPostingMsg");
			String fileStatus = getNodeValue(list.item(0), "FileStatus");
			if (fileStatus.equalsIgnoreCase("NotCorrupted")) {
				insertMessageHeader(messageId, "LIQ");
				transactionFileName = getNodeValue(list.item(0), "FileName");
				Boolean cust_maintain = (Boolean) ubInformationService.getBizInfo().getModuleConfigurationValue(
						"CorpLending_Interface", "Customer account maintained in essence",
						BankFusionThreadLocal.getBankFusionEnvironment());

				for (int i = 0; i < list.getLength(); i++) {

					postingMsg = new IBOFinancialPostingMsg();
					logger.info("AccountId in Node " + (i + 1) + " = " + getNodeValue(list.item(i), "AccountID"));
					logger.info("Pseudonym in Node " + (i + 1) + " = " + getNodeValue(list.item(i), "glAccountID"));
					fileName = getNodeValue(list.item(i), "FileName");
					liqDdaResponse.setFileName(fileName);

					HashMap accountDetails = new HashMap<String, Object>();
					String accountId = CommonConstants.EMPTY_STRING;
					if (cust_maintain != null) {
						if (cust_maintain) {
							accountId = getNodeValue(list.item(i), "AccountID");
							if (StringUtils.isEmpty(accountId)) {
								accountId = getNodeValue(list.item(i), "glAccountID");
							}
						} else {
							accountId = getNodeValue(list.item(i), "glAccountID");
						}
					}
					accountDetails = (HashMap) getAccountDetails(accountId,
							getNodeValue(list.item(i), "AccountCurrency"), getNodeValue(list.item(i), "BranchID"));
					allAccountsDetails.put(accountId, accountDetails);
					postingMsg.setAccountID(accountId);
					postingMsg.setAccountCurrency(getNodeValue(list.item(i), "AccountCurrency"));
					postingMsg.setTransactionCurrency(getNodeValue(list.item(i), "TransactionCurrency"));
					postingMsg.setTransactionAmount(new BigDecimal(getNodeValue(list.item(i), "TransactionAmount")));
					postingMsg.setCreditDebitFlag(getNodeValue(list.item(i), "CreditDebitFlag"));
					postingMsg.setTransactionCode(getNodeValue(list.item(i), "TransactionCode"));
					postingMsg.setBranchID(getNodeValue(list.item(i), "BranchID"));
					postingMsg.setTransactionReference(getNodeValue(list.item(i), "TransactionReference"));

					DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					java.util.Date sourceDate = sourceFormat.parse(getNodeValue(list.item(i), "ValueDate"));
					java.sql.Date sqlDate = new java.sql.Date(sourceDate.getTime());
					postingMsg.setValueDate(sqlDate);

					postingMsg.setNarration(getNodeValue(list.item(i), "Narration"));
					postingMsg.setExchangeRate(new BigDecimal(getNodeValue(list.item(i), "ExchangeRate")));
					postingMsg.setBaseEquivalent(new BigDecimal(getNodeValue(list.item(i), "BaseEquivalent")));
					postingMsg.setChannelID(getNodeValue(list.item(i), "ChannelID"));

					if (getNodeValue(list.item(i), "FailurePostingAction") != null) {
						postingMsg.setFailurePostingAction(getNodeValue(list.item(i), "FailurePostingAction"));
					} else {
						postingMsg.setFailurePostingAction("");
					}
					postingMsg.setOwnerRID(getNodeValue(list.item(i), "OwnerRID"));
					postingMsg.setGlRID(getNodeValue(list.item(i), "GlRID"));
					postingMsg.setAffiliateCode(getNodeValue(list.item(i), "AffiliateCode"));
					postingMsg.setExpenseCode(getNodeValue(list.item(i), "ExpenseCode"));
					postingMsg.setLIQTransactionCode(getNodeValue(list.item(i), "LIQTransactionCode"));
					postingMsg.setCustomerID(getNodeValue(list.item(i), "CustomerID"));
					postingMsg.setBatchReference(getNodeValue(list.item(i), "BatchReference"));
					postingMsg.setSecurityType(getNodeValue(list.item(i), "SecurityType"));
					postingMsg.setSecurityID(getNodeValue(list.item(i), "SecurityID"));
					postingMsg.setLIQSourceBranch(getNodeValue(list.item(i), "LIQSourceBranch"));
					postingMsg.setTransactionType(getNodeValue(list.item(i), "TransactionType"));
					postingMsgIBOList.addIBOFinancialPostingMsg(postingMsg);
				}
				listIncomingPostingDetails = insertIncomingPostingDetails(postingMsgIBOList);
				insertMessageDetail(postingMsgIBOList, messageId);
				if (errorCode != 0) {
					for (IBOFinancialPostingMsg iboFinancialPostingMsg : postingMsgIBOList
							.getIBOFinancialPostingMsg()) {
						liqReponse = new LIQPostingResponse();
						liqReponse.setLoanIQGLId(iboFinancialPostingMsg.getTransactionReference());
						if (errorCode == 70009472) {
							flag = "Y";
							statusCode = 0;
						} else {
							statusCode = errorCode;
						}
						liqReponse.setStatusCode(Integer.toString(statusCode));
						responseList.add(liqReponse);
					}
					liqDdaResponse.setLiqPostingResponse(responseList.toArray(new LIQPostingResponse[] {}));
					sendResponse(liqDdaResponse);
					throw new PropertyValueException(
							BankFusionMessages.getInstance().getFormattedEventMessage(40112076, new Object[] {},
									BankFusionThreadLocal.getUserSession().getUserLocale()),
							IBOUB_INF_LIQMessageDetails.BONAME, IBOUB_INF_LIQMessageDetails.ACCOUNTID);
				}
			}
			if (fileStatus.equalsIgnoreCase("Corrupted")) {
				insertTransactionFileDetails(getNodeValue(list.item(0), "FileName"), "F", "CORRUPTED");
				checkAndNotifyControlFile();
			}

		} catch (PropertyValueException pve) {
			if (flag == "Y") {
				insertTransactionFileDetails(transactionFileName, "F", "DUPLICATE");
				checkAndNotifyControlFile();
			} else {
				insertTransactionFileDetails(transactionFileName, "S", "");
			}
			updateMessageHeader(messageId, "F", errorCode);
			String eventMsg = BankFusionMessages.getFormattedMessage(errorCode, new String[] {});
			updateIncomingPostingDetails(listIncomingPostingDetails, "F", errorCode, eventMsg);
			logger.error(pve);
			return;
		} catch (ParseException e) {
			insertTransactionFileDetails(transactionFileName, "S", "");
			updateMessageHeader(messageId, "F", 40409291);
			String eventMsg = BankFusionMessages.getFormattedMessage(40409291, new String[] {});
			updateIncomingPostingDetails(listIncomingPostingDetails, "F", 40409291, eventMsg);
			liqDdaResponse = prepareResponse(listIncomingPostingDetails, 40409291);
			logger.error(e);
			sendResponse(liqDdaResponse);
			return;
		} catch (BankFusionException bfEx) {

			messageStatus = "F";
			error = true;
			Collection<IEvent> errors = bfEx.getEvents();
			Iterator<IEvent> errorIterator = errors.iterator();
			IEvent event = errorIterator.next();
			msgCode = event.getEventNumber();
			if (msgCode == 0) {
				msgCode = 40000127;
			}
			logger.error(bfEx);
			insertTransactionFileDetails(transactionFileName, "S", "");
			updateMessageHeader(messageId, messageStatus, msgCode);
			updateIncomingPostingDetails(listIncomingPostingDetails, messageStatus, msgCode,
					bfEx.getLocalizedMessage());
			liqDdaResponse = prepareResponse(listIncomingPostingDetails, msgCode);
			sendResponse(liqDdaResponse);

			return;
		}

		catch (Exception e) {

			if ("Duplicate message".equals(e.getMessage())) {
				msgCode = 0;
				updateMessageHeader(messageId, "F", 70009472);
				insertTransactionFileDetails(transactionFileName, "F", "CORRUPTED");
				updateIncomingPostingDetails(listIncomingPostingDetails, "F", 70009472, e.getMessage());
			} else {
				msgCode = 40000127;
				updateMessageHeader(messageId, "F", 40000127);
				insertTransactionFileDetails(transactionFileName, "S", "");
				String eventMsg = BankFusionMessages.getFormattedMessage(40000127, new String[] {});
				updateIncomingPostingDetails(listIncomingPostingDetails, "F", 40000127, eventMsg);
			}
			logger.error(e);
			liqDdaResponse = prepareResponse(listIncomingPostingDetails, msgCode);
			sendResponse(liqDdaResponse);
			return;
		}

		BankFusionEnvironment env = BankFusionThreadLocal.getBankFusionEnvironment();
		BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
		for (IBOFinancialPostingMsg iboFinancialPostingMsg : postingMsgIBOList.getIBOFinancialPostingMsg()) {

			if (validatePostingLeg(iboFinancialPostingMsg, env, messageId, listIncomingPostingDetails,
					(HashMap) allAccountsDetails.get(iboFinancialPostingMsg.getAccountID()))) {

				error = true;
				BankFusionThreadLocal.cleanUp();
				BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
				BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				return;
			}
		}
		BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
		BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
		HashMap paramsForPosting = new HashMap();
		HashMap outputParamsFromPosting = null;
		try {
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

			postingMsgList = createPostingMessages(postingMsgIBOList, env);

			VectorTable postingMessagesVector = new VectorTable();

			for (IPostingMessage postingMessage : postingMsgList) {

				IBOFinancialPostingMessage financialPostingMessage = (IBOFinancialPostingMessage) postingMessage;
				postingMessagesVector.addAll(new VectorTable(financialPostingMessage.getDataMap()));
			}

			if (postingMessagesVector.size() > 0) {

				BankFusionThreadLocal.setChannel("LIQ");
				BankFusionThreadLocal.setApplicationID("COMRLENDING");

				paramsForPosting.put("PostingMessages", postingMessagesVector);
				paramsForPosting.put("transactionID", transactionID);
				paramsForPosting.put("suppressSchedulerIfForwardValued", true);
				paramsForPosting.put("isBlocking", false);
				paramsForPosting.put("manualValueTime", SystemInformationManager.getInstance().getBFBusinessTime());
				Integer tryPostCount = (Integer) ubInformationService.getBizInfo().getModuleConfigurationValue(
						"CorpLending_Interface", "POSTRETRY", BankFusionThreadLocal.getBankFusionEnvironment());
				for (int i = 0; i < tryPostCount; i++) {
					outputParamsFromPosting = tryPosting(paramsForPosting);
					if (outputParamsFromPosting != null) {
						i = tryPostCount;
					} else {
						logger.info("failed posting retrying...");
					}
				}
				if (outputParamsFromPosting == null) {
					throw new Exception("failed posting");
				}
			}

			String errorDescription;
			if (error) {
				messageStatus = "F";
				errorDescription = BankFusionMessages.getFormattedMessage(msgCode, new String[] {});
			} else {
				messageStatus = "P";
				errorDescription = "";
				insertTransactionFileDetails(transactionFileName, "S", "");
			}
			updateMessageHeader(messageId, messageStatus, msgCode);
			updateIncomingPostingDetails(listIncomingPostingDetails, messageStatus, msgCode, errorDescription);

		} catch (BankFusionException bfEx) {

			messageStatus = "F";
			error = true;
			Collection<IEvent> errors = bfEx.getEvents();
			Iterator<IEvent> errorIterator = errors.iterator();
			IEvent event = errorIterator.next();
			msgCode = event.getEventNumber();
			if (msgCode == 0) {
				msgCode = 40000127;
			}
			logger.error(bfEx);
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
			insertTransactionFileDetails(transactionFileName, "S", "");
			updateMessageHeader(messageId, messageStatus, msgCode);
			updateIncomingPostingDetails(listIncomingPostingDetails, messageStatus, msgCode,
					bfEx.getLocalizedMessage());

		} catch (Exception ex) {

			messageStatus = "F";
			error = true;
			msgCode = 40000127;
			logger.error(ex);

			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

			updateMessageHeader(messageId, messageStatus, msgCode);
			String eventMsg = BankFusionMessages.getFormattedMessage(msgCode, new String[] {});
			updateIncomingPostingDetails(listIncomingPostingDetails, messageStatus, msgCode, eventMsg);

		}
		try {
			for (IBOFinancialPostingMsg message : postingMsgIBOList.getIBOFinancialPostingMsg()) {

				String messageCode = Integer.toString(msgCode);

				liqReponse = new LIQPostingResponse();
				liqReponse.setLoanIQGLId(message.getTransactionReference());
				liqReponse.setStatusCode(messageCode);

				responseList.add(liqReponse);
			}

			liqDdaResponse.setLiqPostingResponse(responseList.toArray(new LIQPostingResponse[] {}));
			sendResponse(liqDdaResponse);
			BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
		} catch (Exception e) {
			messageStatus = "F";
			error = true;
			msgCode = 40000127;
			logger.error(e);
			insertTransactionFileDetails(transactionFileName, "S", "");
			updateMessageHeader(messageId, messageStatus, msgCode);
			String eventMsg = BankFusionMessages.getFormattedMessage(msgCode, new String[] {});
			updateIncomingPostingDetails(listIncomingPostingDetails, messageStatus, msgCode, eventMsg);
			BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
			liqDdaResponse = prepareResponse(listIncomingPostingDetails, msgCode);
			sendResponse(liqDdaResponse);
		} finally {

			BankFusionThreadLocal.cleanUp();
		}
		
	}

	private Map getAccountDetails(String accountId, String accountCurrency, String branchID) throws Exception {
		logger.info("Start of GetAccountDetails");
		Map accountDetails = getBasicAccountDetails(accountId);
		accountDetails.put("isCustAccount", true);
		if (!accountDetails.containsKey("ACCOUNTID")) {
			String pseudoAcc = UB_IBI_PaymentsHelper.getAccountFromPuedoNymes(accountId, accountCurrency, "BRANCH",
					branchID);
			accountDetails = getBasicAccountDetails(pseudoAcc);
			accountDetails.put("isCustAccount", false);
		}
		if (StringUtils.isBlank(((String) accountDetails.get("ACCOUNTID")))) {
			errorCode = 40180290;
		}

		return accountDetails;
	}

	public static Map getBasicAccountDetails(String accountID) {
		ArrayList<String> params = new ArrayList<String>();
		List<SimplePersistentObject> result = null;
		@SuppressWarnings("FBPE")
		String QUERY_BASIC_ACOOUNDETAILS = "SELECT ACC." + IBOAccount.STOPPED + " AS " + IBOAccount.STOPPED + ", ACC."
				+ IBOAccount.CLOSED + " AS " + IBOAccount.CLOSED + ",ACC." + IBOAccount.DORMANTSTATUS + " AS "
				+ IBOAccount.DORMANTSTATUS + ", ACC." + IBOAccount.ACCRIGHTSINDICATOR + " AS "
				+ IBOAccount.ACCRIGHTSINDICATOR + ", ACC." + IBOAccount.ACCOUNTID + " AS " + "ACCOUNTID" + ", ACC."
				+ IBOAccount.PRODUCTID + " AS " + IBOAccount.PRODUCTID + " FROM " + IBOAccount.BONAME
				+ " ACC  WHERE ACC." + IBOAccount.ACCOUNTID + " = ? ";
		params.add(accountID);

		result = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(QUERY_BASIC_ACOOUNDETAILS, params,
				null, true);
		if (result != null && !result.isEmpty()) {
			return result.get(0).getDataMap();
		} else {
			return new HashMap<>();
		}
	}

	HashMap tryPosting(HashMap paramsForPosting) {
		try {
			return MFExecuter.executeMF(CB_CMN_COLLECTIONPOSTING_MESSAGE_SRV, paramsForPosting,
					BankFusionThreadLocal.getUserLocator().getStringRepresentation());
		} catch (RetriableException re) {
			logger.info("Retry failed");
			return null;
		}
	}

	private List<SimplePersistentObject> insertIncomingPostingDetails(IBOFinancialPostingMsgList postingMsgList) {
		logger.info("start of InsertIncomingPostingDetails");
		String messageId = null;

		List<SimplePersistentObject> incomingpostingdetailsList = new ArrayList<>();
		if (postingMsgList == null || postingMsgList.getIBOFinancialPostingMsg().length <= 0) {
			return null;
		}
		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
		PrivatePersistenceFactory persistancefactory = new PrivatePersistenceFactory();
		for (IBOFinancialPostingMsg iboFinancialPostingMsg: postingMsgList.getIBOFinancialPostingMsg()) {
			IBOUB_INF_INCOMINGPOSTINGDETAILS incomingPostingDetails = (IBOUB_INF_INCOMINGPOSTINGDETAILS) privateFactory
					.getStatelessNewInstance(IBOUB_INF_INCOMINGPOSTINGDETAILS.BONAME);
			if (incomingPostingDetails != null) {
				messageId = GUIDGen.getNewGUID();
				incomingPostingDetails.setF_TRANSACTIONTYPE(iboFinancialPostingMsg.getTransactionType());
				incomingPostingDetails
						.setF_VALUEDATE(new Timestamp(iboFinancialPostingMsg.getValueDate().getTime()));
				incomingPostingDetails.setF_REFERENCE(iboFinancialPostingMsg.getTransactionReference());
				incomingPostingDetails.setF_STATUS("R");
				incomingPostingDetails.setF_OWNERRID(iboFinancialPostingMsg.getOwnerRID());
				incomingPostingDetails.setF_AFFILIATECODE(iboFinancialPostingMsg.getAffiliateCode());
				incomingPostingDetails.setBoID(messageId);
				incomingPostingDetails.setF_TRANSACTIONCURRENCY(iboFinancialPostingMsg.getTransactionCurrency());
				incomingPostingDetails.setF_ACCOUNTID(iboFinancialPostingMsg.getAccountID());
				incomingPostingDetails
						.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
				incomingPostingDetails
						.setF_TRANSACTIONAMOUNT(iboFinancialPostingMsg.getTransactionAmount());
				incomingPostingDetails.setF_CHANNELID(iboFinancialPostingMsg.getChannelID());
				incomingPostingDetails.setF_GLRID(iboFinancialPostingMsg.getGlRID());
				incomingPostingDetails.setF_CREDITDEBITFLAG(iboFinancialPostingMsg.getCreditDebitFlag());
				incomingPostingDetails.setF_EXPENSECODE(iboFinancialPostingMsg.getExpenseCode());
				incomingPostingDetails.setF_TRANSACTIONCODE(iboFinancialPostingMsg.getLIQTransactionCode());
				incomingPostingDetails.setF_CUSTOMERID(iboFinancialPostingMsg.getCustomerID());
				incomingPostingDetails.setF_BATCHREFERENCE(iboFinancialPostingMsg.getBatchReference());
				incomingPostingDetails.setF_SECURITYTYPE(iboFinancialPostingMsg.getSecurityType());
				incomingPostingDetails.setF_SECURITYID(iboFinancialPostingMsg.getSecurityID());
				incomingPostingDetails.setF_SOURCEBRANCH(iboFinancialPostingMsg.getLIQSourceBranch());
				incomingpostingdetailsList.add(incomingPostingDetails);
			}

		}
		persistancefactory.create(incomingpostingdetailsList);
		logger.info("End of InsertIncomingPostingDetails");
		return incomingpostingdetailsList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void checkAndNotifyControlFile() {
		logger.info("Start of checkAndNotifyControlFile");
		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
		privateFactory.beginTransaction();

		String whereClauseForTransaction = "where " + IBOUB_INF_TRANSACTIONFILEDETAILS.PROCESSINGDATE + " = ? ";
		String whereClauseForControl = "where " + IBOUB_INF_CONTROLFILEDETAILS.PROCESSINGDATE + " = ? ";
		ArrayList params = null;
		params = new ArrayList();
		try {
			List<IBOUB_INF_TRANSACTIONFILEDETAILS> transactionFileCount = null;
			params.add(SystemInformationManager.getInstance().getBFBusinessDate());
			transactionFileCount = privateFactory.findByQuery(IBOUB_INF_TRANSACTIONFILEDETAILS.BONAME,
					whereClauseForTransaction, params, null, true);
			int transactionCount = transactionFileCount.size();
			logger.info("No of transaction files are " + transactionCount);

			params.clear();

			List<IBOUB_INF_CONTROLFILEDETAILS> controlFileCount = null;
			params.add(SystemInformationManager.getInstance().getBFBusinessDate());
			controlFileCount = privateFactory.findByQuery(IBOUB_INF_CONTROLFILEDETAILS.BONAME, whereClauseForControl,
					params, null, true);
			int controlCount = controlFileCount.size();
			logger.info("No of Control files are " + controlCount);
			params.clear();
			int branchCount = Integer.parseInt(
					UB_IBI_PaymentsHelper.getModuleConfigValue("NUMBEROF_LIQBRANCH", "CorpLending_Interface"));

			if ((controlCount == branchCount) && (branchCount > 0)) {
				String res = "";
				int count = 0;

				if ((controlFileCount != null) && (controlCount > 0)) {

					for (int i = 0; i < controlCount; i++) {
						count = count + controlFileCount.get(i).getF_FILESCOUNT();
						logger.info("Count of file in control table is ----->" + count);
					}

				}

				if (transactionCount == count) {
					logger.info("No of trnsaction file and file count in control table are equal");
					params.add(SystemInformationManager.getInstance().getBFBusinessDate());
					params.add("F");

					String whereClauseForTransactionFind = "where " + IBOUB_INF_TRANSACTIONFILEDETAILS.PROCESSINGDATE
							+ " = ? AND " + IBOUB_INF_TRANSACTIONFILEDETAILS.STATUS + " = ?";
					List<IBOUB_INF_TRANSACTIONFILEDETAILS> transactionFileList = null;
					transactionFileList = privateFactory.findByQuery(IBOUB_INF_TRANSACTIONFILEDETAILS.BONAME,
							whereClauseForTransactionFind, params, null, true);
					int failed = transactionFileList.size();
					int sucess = count - failed;
					String dateTime = SystemInformationManager.getInstance().getBFBusinessDateTimeAsString();
					res = res + "Processed=" + count + ";" + "Sucess=" + sucess + ";" + "Failed=" + failed + ";"
							+ "DateTime=" + dateTime;
					logger.info("Response send is " + res);
					String fileNames = "";
					if ((transactionFileList != null) && (failed > 0)) {
						for (int i = 0; i < failed; i++) {
							fileNames = fileNames + transactionFileList.get(i).getF_TRANSACTIONFILENAME() + ";";
						}

					}
					res = res + System.lineSeparator() + "FailedFileName=" + fileNames;
					logger.info("Response send is " + res);
					sendControlResponse(res);

				}

			}
		} catch (Exception e) {
			logger.error(e);

		} finally {
			privateFactory.closePrivateSession();
		}

		logger.info("End of checkAndNotifyControlFile");
	}

	private void sendControlResponse(String controlReponse) {
		String response = controlReponse;
		String endpoint = "LOANIQ_CONTROL_RESPONSE_GL";
		logger.info("Sending message to Loan IQ");
		MessageProducerUtil.sendMessage(response, endpoint);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void insertMessageDetail(IBOFinancialPostingMsgList postingMsgIBOList, String messageHeaderId)
			throws Exception {

		logger.info("Start of InsertMessageDetail");

		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
		privateFactory.beginTransaction();

		IBOUB_INF_LIQMessageDetails liqMessageDetails = null;
		String messageId = null;
		String accountId = null;
		String whereClause = "where " + IBOUB_INF_INCOMINGPOSTINGDETAILS.REFERENCE + " = ? and "
				+ IBOUB_INF_INCOMINGPOSTINGDETAILS.STATUS + " = ?";
		ArrayList params = null;

		List<IBOUB_INF_LIQMessageDetails> existingMessages = null;

		try {
			for (IBOFinancialPostingMsg iboFinancialPostingMsg : postingMsgIBOList.getIBOFinancialPostingMsg()) {

				params = new ArrayList();
				params.add(iboFinancialPostingMsg.getTransactionReference());
				params.add("P");

				existingMessages = privateFactory.findByQuery(IBOUB_INF_INCOMINGPOSTINGDETAILS.BONAME, whereClause,
						params, null, true);

				if (existingMessages != null && existingMessages.size() > 0) {
					errorCode = 70009472;
				}

				liqMessageDetails = (IBOUB_INF_LIQMessageDetails) privateFactory
						.getStatelessNewInstance(IBOUB_INF_LIQMessageDetails.BONAME);

				accountId = iboFinancialPostingMsg.getAccountID();

				messageId = GUIDGen.getNewGUID();
				liqMessageDetails.setBoID(messageId);
				liqMessageDetails.setF_MESSAGEHEADERID(messageHeaderId);
				liqMessageDetails.setF_ACCOUNTID(accountId);
				liqMessageDetails.setF_ACCOUNTCURRENCY(iboFinancialPostingMsg.getAccountCurrency());
				liqMessageDetails.setF_TRANSACTIONCURRENCY(iboFinancialPostingMsg.getTransactionCurrency());
				liqMessageDetails.setF_TRANSACTIONAMOUNT(iboFinancialPostingMsg.getTransactionAmount());
				liqMessageDetails.setF_CREDITDEBITFLAG(iboFinancialPostingMsg.getCreditDebitFlag());
				liqMessageDetails.setF_BRANCHID(iboFinancialPostingMsg.getBranchID());
				liqMessageDetails.setF_TRANSACTIONREFERENCE(iboFinancialPostingMsg.getTransactionReference());
				liqMessageDetails.setF_VALUEDATE(iboFinancialPostingMsg.getValueDate());
				liqMessageDetails.setF_NARRATION(iboFinancialPostingMsg.getNarration());
				liqMessageDetails.setF_EXCHANGERATE(iboFinancialPostingMsg.getExchangeRate());
				liqMessageDetails.setF_BASEEQUIVALENT(iboFinancialPostingMsg.getBaseEquivalent());
				liqMessageDetails.setF_SRTRANSACTIONREFERENCE(iboFinancialPostingMsg.getTransactionReference());
				liqMessageDetails.setF_TRANSACTIONCODE(iboFinancialPostingMsg.getTransactionCode());

				privateFactory.create(IBOUB_INF_LIQMessageDetails.BONAME, liqMessageDetails);
			}
			privateFactory.commitTransaction();

		} finally {
			privateFactory.closePrivateSession();
		}
		logger.info("End of InsertMessageDetail");
	}

	private void insertMessageHeader(String messageId, String channelId) {

		logger.info("Start of insertMessageHeader");

		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
		privateFactory.beginTransaction();

		try {
			IBOUB_INF_MessageHeader messageHeader = (IBOUB_INF_MessageHeader) privateFactory
					.getStatelessNewInstance(IBOUB_INF_MessageHeader.BONAME);

			messageHeader.setBoID(messageId);
			// leaving blobSaveValue for now
			messageHeader.setF_MESSAGETYPE("POST");
			messageHeader.setF_CHANNELID(channelId);
			messageHeader.setF_MESSAGESTATUS("R");
			messageHeader.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
			messageHeader.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
			messageHeader.setF_DIRECTION("I");

			privateFactory.create(IBOUB_INF_MessageHeader.BONAME, messageHeader);
			privateFactory.commitTransaction();
		} catch (Exception e) {

			privateFactory.rollbackTransaction();
			logger.error(e);
		} finally {
			privateFactory.closePrivateSession();
		}

		logger.info("End of insertMessageHeader");
	}

	private void insertTransactionFileDetails(String filename, String filestatus, String error) {

		logger.info("Start of insertTransactionFileDetails");

		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
		privateFactory.beginTransaction();

		try {
			IBOUB_INF_TRANSACTIONFILEDETAILS transactionFileDetails = (IBOUB_INF_TRANSACTIONFILEDETAILS) privateFactory
					.getStatelessNewInstance(IBOUB_INF_TRANSACTIONFILEDETAILS.BONAME);

			transactionFileDetails.setBoID(GUIDGen.getNewGUID());
			transactionFileDetails.setF_TRANSACTIONFILENAME(filename);
			transactionFileDetails.setF_STATUS(filestatus);
			transactionFileDetails.setF_REASONFORFAILURE(error);
			transactionFileDetails.setF_PROCESSINGDATE(SystemInformationManager.getInstance().getBFBusinessDate());
			// leaving blobSaveValue for now
			/*
			 * messageHeader.setF_MESSAGETYPE("POST");
			 * messageHeader.setF_CHANNELID(channelId);
			 * messageHeader.setF_MESSAGESTATUS("R");
			 * messageHeader.setF_MSGRECEIVEDTTM(SystemInformationManager.getInstance().
			 * getBFBusinessDateTime());
			 * messageHeader.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().
			 * getBFBusinessDateTime()); messageHeader.setF_DIRECTION("I");
			 */

			privateFactory.create(IBOUB_INF_TRANSACTIONFILEDETAILS.BONAME, transactionFileDetails);
			privateFactory.commitTransaction();
		} catch (Exception e) {

			privateFactory.rollbackTransaction();
			logger.error(e);
		} finally {
			privateFactory.closePrivateSession();
		}

		logger.info("End of insertTransactionFileDetails");
	}

	private void updateIncomingPostingDetails(List<SimplePersistentObject> incomingPostDtlsList, String messageStatus,
			int msgCode, String errorDescription) {

		logger.info("start of updateIncomingPostingDetails");
		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);

		try {
			for (SimplePersistentObject so : incomingPostDtlsList) {
				if (so != null) {
					privateFactory.beginTransaction();
					IBOUB_INF_INCOMINGPOSTINGDETAILS incomingPostDtlDao = (IBOUB_INF_INCOMINGPOSTINGDETAILS) privateFactory
							.findByPrimaryKey(IBOUB_INF_INCOMINGPOSTINGDETAILS.BONAME, so.getBoID(), true);
					if (incomingPostDtlDao != null) {

						incomingPostDtlDao.setBoID(so.getBoID());
						incomingPostDtlDao.setF_STATUS(messageStatus);
						incomingPostDtlDao.setF_ERRORCODE(Integer.toString(msgCode));
						incomingPostDtlDao.setF_ERRORDESCRIPTION(errorDescription);
						privateFactory.commitTransaction();
					}
				}
			}
		} catch (Exception e) {
			privateFactory.rollbackTransaction();
			logger.error(e);
		} finally {
			privateFactory.closePrivateSession();
		}
		logger.info("end of updateIncomingPostingDetails");
	}

	private void updateMessageHeader(String messageId, String messageStatus, int msgCode) {

		logger.info("Start of updateMessageHeader");

		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
		privateFactory.beginTransaction();

		try {
			IBOUB_INF_MessageHeader existingHeaderBOItem = (IBOUB_INF_MessageHeader) privateFactory
					.findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, messageId, true);

			if (existingHeaderBOItem != null) {

				existingHeaderBOItem.setF_MESSAGESTATUS(messageStatus);
				existingHeaderBOItem.setF_ERRORCODE(msgCode);
				existingHeaderBOItem
						.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
				existingHeaderBOItem.setBoID(messageId);

			}

			privateFactory.commitTransaction();
		} catch (Exception e) {

			privateFactory.rollbackTransaction();
			logger.error(e);
		} finally {
			privateFactory.closePrivateSession();
		}

		logger.info("End of updateMessageHeader.");
	}

	public ArrayList<IBOFinancialPostingMessage> createPostingMessages(IBOFinancialPostingMsgList postingContext,
			BankFusionEnvironment env) {

		logger.info("Start of createPostingMessages");

		int msgCnt = postingContext.getIBOFinancialPostingMsgCount();
		ArrayList<IBOFinancialPostingMessage> FINPostingMsges = new ArrayList<IBOFinancialPostingMessage>();

		for (int i = 0; i < msgCnt; i++) {

			IBOFinancialPostingMessage postingMessage = (IBOFinancialPostingMessage) BankFusionThreadLocal
					.getPersistanceFactory().getStatelessNewInstance(IBOFinancialPostingMessage.BONAME);

			FatomUtils.createStandardItemsMessage(postingMessage, env);
			PostingHelper.setDefaultValuesForFinPosting(postingMessage, env);

			BigDecimal txnAmount = postingContext.getIBOFinancialPostingMsg(i).getTransactionAmount();

			postingMessage.setPrimaryID(postingContext.getIBOFinancialPostingMsg(i).getAccountID());// account
			postingMessage.setAcctCurrencyCode(postingContext.getIBOFinancialPostingMsg(i).getAccountCurrency());
			postingMessage.setF_AMOUNT(txnAmount);
			postingMessage.setTransCode(postingContext.getIBOFinancialPostingMsg(i).getTransactionCode());
			postingMessage.setTransactionRef(postingContext.getIBOFinancialPostingMsg(i).getTransactionReference());
			postingMessage.setBranchID(postingContext.getIBOFinancialPostingMsg(i).getBranchID());
			postingMessage.setTransactionDate(SystemInformationManager.getInstance().getBFBusinessDate());
			postingMessage.setF_ACTUALAMOUNT(txnAmount);
			postingMessage.setF_TXNCURRENCYCODE(postingContext.getIBOFinancialPostingMsg(i).getTransactionCurrency());
			postingMessage.setF_CHANNELID(postingContext.getIBOFinancialPostingMsg(i).getChannelID());

			if (forcePostFlag || getFailurePostingAction(postingContext.getIBOFinancialPostingMsg(i))
					.equalsIgnoreCase(FAILURE_POSTING_ACTION_FORCE_POST)) {

				postingMessage.setForcePost(true);
			} else {

				postingMessage.setForcePost(false);
			}

			postingMessage.setNarrative(postingContext.getIBOFinancialPostingMsg(i).getNarration());

			if (postingContext.getIBOFinancialPostingMsg(i).getCreditDebitFlag().equalsIgnoreCase("C")) {

				postingMessage.setSign('+');
				postingMessage.setF_AMOUNTCREDIT(txnAmount);
			}

			if (postingContext.getIBOFinancialPostingMsg(i).getCreditDebitFlag().equalsIgnoreCase("D")) {

				postingMessage.setSign('-');
				postingMessage.setF_AMOUNTDEBIT(txnAmount);
			}

			if (postingContext.getIBOFinancialPostingMsg(i).getValueDate() != null) {

				postingMessage.setValueDate(postingContext.getIBOFinancialPostingMsg(i).getValueDate());
			} else {

				postingMessage.setValueDate(SystemInformationManager.getInstance().getBFBusinessDate());
			}

			FINPostingMsges.add(postingMessage);
		}
		logger.info("End of createPostingMessages");

		return FINPostingMsges;
	}

	public String getMisTranxCode(String CrDtFlag) {

		String value = "";

		if (CrDtFlag.equalsIgnoreCase("D")) {

			return UB_IBI_PaymentsHelper.getModuleConfigValue("DEFAULT_DR_TXN_CODE_LQ", "CorpLending_Interface");
		}

		if (CrDtFlag.equalsIgnoreCase("C")) {

			return UB_IBI_PaymentsHelper.getModuleConfigValue("DEFAULT_CR_TXN_CODE_LQ", "CorpLending_Interface");
		}

		return value;

	}

	private String getFailurePostingAction(IBOFinancialPostingMsg iboFinancialPostingMsg) {

		if (iboFinancialPostingMsg.getFailurePostingAction().isEmpty()
				|| !isFailurePostingActionValid(iboFinancialPostingMsg.getFailurePostingAction())) {

			return FAILURE_POSTING_ACTION_REJECT;
		} else {

			return iboFinancialPostingMsg.getFailurePostingAction();
		}
	}

	private boolean isFailurePostingActionValid(String failurePostingAction) {

		if (failurePostingAction.equalsIgnoreCase(FAILURE_POSTING_ACTION_FORCE_POST)
				|| failurePostingAction.equalsIgnoreCase(FAILURE_POSTING_ACTION_POST_TO_SUSP)
				|| failurePostingAction.equalsIgnoreCase(FAILURE_POSTING_ACTION_REJECT)) {

			return true;
		} else {

			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	private boolean validatePostingLeg(IBOFinancialPostingMsg iboFinancialPostingMsg, BankFusionEnvironment env1,
			String messageId, List<SimplePersistentObject> listIncomingPostingDetails, HashMap accountDetails) {
		boolean error = false;
		String eventCode = CommonConstants.EMPTY_STRING;
		LIQDDAPostingResponse liqDdaResponse;

		logger.info("Start of validatePostingLeg ");
		try {
			if ("Y".equals(accountDetails.get("f_CLOSED"))) {
				handleEvent(40000132, new String[] {});
			}
			if (!getFailurePostingAction(iboFinancialPostingMsg).equalsIgnoreCase(FAILURE_POSTING_ACTION_FORCE_POST)) {
				validateStoppedAccount(accountDetails);
				validateDormantAccount(accountDetails, iboFinancialPostingMsg);
				validateARIIndicators(accountDetails, iboFinancialPostingMsg);
				if (iboFinancialPostingMsg.getCreditDebitFlag().equals("D")
						&& (boolean) accountDetails.get("isCustAccount")) {
					validateAvailableBalance(accountDetails, iboFinancialPostingMsg);
				}
			}
			logger.info("End of validatePostingLeg ");
			return error;
		} catch (CollectedEventsDialogException collectedEventsDialogException) {
			logger.error(collectedEventsDialogException);
			error = true;
			String eventMsg = "";
			List<ErrorEvent> errors = collectedEventsDialogException.getErrors();

			for (ErrorEvent runTimeError : errors) {

				eventCode = String.valueOf(runTimeError.getEventNumber());
			}

			if (eventCode.equals("40430040") || eventCode.equals("40000133") || eventCode.equals("40007319")
					|| eventCode.equals("40007321") || eventCode.equals("40112172") || eventCode.equals("40007322")
					|| eventCode.equals("40007323") || eventCode.equals("40007325") || eventCode.equals("40409528")
					|| eventCode.equals("40000132") || eventCode.equals("40507020")) {
				eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventCode),
						new String[] { (String) accountDetails.get("boID") });

			} else if (eventCode.equals("40507181")) {
				eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventCode),
						new String[] { getSuspensePseudonym(iboFinancialPostingMsg.getCreditDebitFlag()),
								iboFinancialPostingMsg.getAccountCurrency(), "BRANCH",
								iboFinancialPostingMsg.getBranchID() });
			} else {
				eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventCode), new String[] {});
			}
			liqDdaResponse = prepareResponse(listIncomingPostingDetails, Integer.parseInt(eventCode));

			sendResponse(liqDdaResponse);
			updateMessageHeader(messageId, "F", Integer.parseInt(eventCode));
			updateIncomingPostingDetails(listIncomingPostingDetails, "F", Integer.parseInt(eventCode), eventMsg);
			return error;
		} catch (BankFusionException bfEx) {
			error = true;
			String eventMsg = "";
			Collection<IEvent> errors = bfEx.getEvents();
			Iterator<IEvent> errorIterator = errors.iterator();
			IEvent event = errorIterator.next();
			int msgCode = event.getEventNumber();
			eventCode = Integer.toString(msgCode);
			if (eventCode.equals("40430040") || eventCode.equals("40000133") || eventCode.equals("40007319")
					|| eventCode.equals("40007321") || eventCode.equals("40112172") || eventCode.equals("40007322")
					|| eventCode.equals("40007323") || eventCode.equals("40007325") || eventCode.equals("40409528")
					|| eventCode.equals("40000132") || eventCode.equals("40507020")) {
				eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventCode),
						new String[] { (String) accountDetails.get("boID") });

			} else if (eventCode.equals("40507181")) {
				eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventCode),
						new String[] { getSuspensePseudonym(iboFinancialPostingMsg.getCreditDebitFlag()),
								iboFinancialPostingMsg.getAccountCurrency(), "BRANCH",
								iboFinancialPostingMsg.getBranchID() });
			} else {
				eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt(eventCode), new String[] {});
			}
			logger.error(bfEx);

			updateMessageHeader(messageId, "F", msgCode);
			updateIncomingPostingDetails(listIncomingPostingDetails, "F", msgCode, eventMsg);
			liqDdaResponse = prepareResponse(listIncomingPostingDetails, msgCode);
			sendResponse(liqDdaResponse);
			return error;
		} catch (Exception e) {
			logger.error(e);

			error = true;
			liqDdaResponse = prepareResponse(listIncomingPostingDetails, 40000127);

			insertTransactionFileDetails(transactionFileName, "S", "");
			updateMessageHeader(messageId, "F", 40000127);
			String eventMsg = BankFusionMessages.getFormattedMessage(40000127, new String[] {});
			updateIncomingPostingDetails(listIncomingPostingDetails, "F", 40000127, eventMsg);
			sendResponse(liqDdaResponse);
			return error;
		}
	}

	private void sendResponse(LIQDDAPostingResponse liqReponse) {
		checkAndNotifyControlFile();
		Gson gson = new Gson();
		String response = gson.toJson(liqReponse);
		String endpoint = "LOANIQ_POSTINGS_RESPONSE_GL";
		logger.info("Sending message to Loan IQ");
		MessageProducerUtil.sendMessage(response, endpoint);
	}

	@SuppressWarnings("rawtypes")
	private void validateAvailableBalance(Map accountDetails, IBOFinancialPostingMsg iboFinancialPostingMsg) {
		logger.info("Start of validateAvailableBalance");

		Map features = new HashMap();
		String productId = (String) accountDetails.get("f_PRODUCTID");

		SimpleRuntimeProduct product = ProductFactoryProvider.getInstance().getProductFactory()
				.getRuntimeProduct(productId, BankFusionThreadLocal.getBankFusionEnvironment());

		if (product == null) {
			EventsHelper.handleEvent(CommonsEventCodes.E_THE_PRODUCT_COULD_NOT_BE_FOUND, new Object[] { productId },
					new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
		} else {
			features = product.getAllFeatures(BankFusionThreadLocal.getBankFusionEnvironment());
		}

		if (features.containsKey(FeatureIDs.NOSTROFEATURE)) {
			forcePostFlag = true;
			return;
		}

		HashMap hashmapout = AvailableBalanceFunction.run((String) accountDetails.get("ACCOUNTID"));
		BigDecimal availableBalance = (BigDecimal) hashmapout.get("AvailableBalance");

		boolean limitExcessAction = (Boolean) hashmapout.get("IgnoreAvailableBalance");

		if (iboFinancialPostingMsg.getCreditDebitFlag().equals("D")
				&& availableBalance.compareTo(iboFinancialPostingMsg.getTransactionAmount()) < 0
				&& limitExcessAction == false) {

			handleEvent(40507020, new String[] {});
		}
		logger.info("End of validateAvailableBalance");
	}

	@SuppressWarnings("rawtypes")
	private void validateARIIndicators(Map accountDetails, IBOFinancialPostingMsg iboFinancialPostingMsg) {

		logger.info("Start of validateARIIndicators: ");
		logger.info("Account Details: " + accountDetails);
		logger.info("Start of validateARIIndicators: " + accountDetails.get("f_ACCRIGHTSINDICATOR"));
		logger.info("Start of validateARIIndicators: " + accountDetails.get("f_ACCRIGHTSINDICATOR").toString());

		Integer ariIndicator = Integer.valueOf(accountDetails.get("f_ACCRIGHTSINDICATOR").toString());

		if (ariIndicator != null) {

			if (ariIndicator == 2) {
				handleEvent(40007321, new String[] {});
			}

			if (ariIndicator == 3) {
				handleEvent(40112172, new String[] {});
			}

			if (iboFinancialPostingMsg.getCreditDebitFlag().equals("D")
					|| getFailurePostingAction(iboFinancialPostingMsg)
							.equalsIgnoreCase(FAILURE_POSTING_ACTION_REJECT)) {

				if (ariIndicator == 1 || ariIndicator == 9) {
					handleEvent(40007319, new String[] {});
				}

				if (ariIndicator == -1) {
					handleEvent(40007318, new String[] {});
				}

				if (iboFinancialPostingMsg.getCreditDebitFlag().equals("D") && ariIndicator == 4) {
					handleEvent(40007322, new String[] {});
				}

				if (iboFinancialPostingMsg.getCreditDebitFlag().equals("D") && ariIndicator == 5) {
					handleEvent(40007323, new String[] {});
				}

				if (iboFinancialPostingMsg.getCreditDebitFlag().equals("C") && ariIndicator == 6) {
					handleEvent(40409356, new String[] {});
				}

				if (iboFinancialPostingMsg.getCreditDebitFlag().equals("C") && ariIndicator == 7) {
					handleEvent(40007325, new String[] {});
				}
			} else if (getFailurePostingAction(iboFinancialPostingMsg)
					.equalsIgnoreCase(FAILURE_POSTING_ACTION_POST_TO_SUSP)) {

				if (ariIndicator == 1 || ariIndicator == 9 || ariIndicator == -1 || ariIndicator == 2
						|| ariIndicator == 3 || ariIndicator == 4 || ariIndicator == 5 || ariIndicator == 6
						|| ariIndicator == 7) {

					String accountId = UB_IBI_PaymentsHelper.getAccountFromPuedoNymes(
							getSuspensePseudonym(iboFinancialPostingMsg.getCreditDebitFlag()),
							iboFinancialPostingMsg.getAccountCurrency(), "BRANCH",
							iboFinancialPostingMsg.getBranchID());

					if (accountId.isEmpty()) {

						handleEvent(40507181, new String[] {});
					} else {

						iboFinancialPostingMsg.setAccountID(accountId);
					}
				}
			}

		}
		logger.info("End of validateARIIndicators");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validateDormantAccount(Map accountDetails, IBOFinancialPostingMsg iboFinancialPostingMsg) {

		logger.info("End of validateDormantAccount");

		Boolean dormancyFlag = "Y".equals(accountDetails.get("f_DORMANTSTATUS"));

		if (dormancyFlag != null && dormancyFlag == true) {

			Map misMap = new HashMap();
			misMap.put("TXNCODE", iboFinancialPostingMsg.getTransactionCode());

			Map hashmapout = MFExecuter.executeMF("UB_IBI_EnquireMISTxnCode_SRV",
					BankFusionThreadLocal.getBankFusionEnvironment(), misMap);

			if (((String) hashmapout.get("DORMANCYPOSTINGACTION")).equals("1")
					|| ((String) hashmapout.get("DORMANCYPOSTINGACTION")).equals("2")) {

				if (iboFinancialPostingMsg.getCreditDebitFlag().equals("D")
						|| getFailurePostingAction(iboFinancialPostingMsg)
								.equalsIgnoreCase(FAILURE_POSTING_ACTION_REJECT)) {

					handleEvent(40409528, new String[] {});
				} else if (getFailurePostingAction(iboFinancialPostingMsg)
						.equalsIgnoreCase(FAILURE_POSTING_ACTION_POST_TO_SUSP)) {

					String accountId = UB_IBI_PaymentsHelper.getAccountFromPuedoNymes(
							getSuspensePseudonym(iboFinancialPostingMsg.getCreditDebitFlag()),
							iboFinancialPostingMsg.getAccountCurrency(), "BRANCH",
							iboFinancialPostingMsg.getBranchID());

					if (accountId.isEmpty()) {

						handleEvent(40507181, new String[] {});
					} else {

						iboFinancialPostingMsg.setAccountID(accountId);
					}
				}
			}
		}
		logger.info("End of validateDormantAccount");
	}

	@SuppressWarnings("rawtypes")
	private void validateStoppedAccount(Map accountDetails) {

		logger.info("End of validateStoppedAccount");

		if (accountDetails.get("f_STOPPED") != null && "Y".equals(accountDetails.get("f_STOPPED"))) {

			handleEvent(40000133, new String[] {});
		}
		logger.info("End of validateStoppedAccount");
	}

	private String getSuspensePseudonym(String creditDebitFlag) {

		if (creditDebitFlag.equals("C")) {

			return UB_IBI_PaymentsHelper.getModuleConfigValue("CREDIT_SUSPENSE_ACCOUNT", "OPX");
		} else {
			return UB_IBI_PaymentsHelper.getModuleConfigValue("DEBIT_SUSPENSE_ACCOUNT", "OPX");
		}
	}

	private void handleEvent(Integer eventNumber, String[] args) {

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

	private static String getNodeValue(Node node, String nodeName) {

		NodeList list = node.getChildNodes();
		String nodeValue = null;

		for (int i = 0; i < list.getLength(); i++) {

			if (list.item(i).getChildNodes().getLength() > 1) {

				nodeValue = getNodeValue(list.item(i), nodeName);

				if (nodeValue != null) {
					return nodeValue;
				}

			} else {

				if ((list.item(i).getNodeName().equals(nodeName)) && (list.item(i).getFirstChild() != null)) {

					nodeValue = list.item(i).getFirstChild().getTextContent();
				}
			}
		}

		return nodeValue;
	}

	private LIQDDAPostingResponse prepareResponse(List<SimplePersistentObject> listIncomingPostingDetails,
			int msgCode) {
		LIQPostingResponse liqReponse;
		List<LIQPostingResponse> responseList = new ArrayList<>();
		LIQDDAPostingResponse liqDdaResponse = new LIQDDAPostingResponse();
		liqDdaResponse.setFileName(fileName);
		for (SimplePersistentObject so : listIncomingPostingDetails) {
			liqReponse = new LIQPostingResponse();
			IBOUB_INF_INCOMINGPOSTINGDETAILS incomigList = (IBOUB_INF_INCOMINGPOSTINGDETAILS) so;
			liqReponse.setLoanIQGLId(incomigList.getF_GLRID());
			liqReponse.setStatusCode(Integer.toString(msgCode));
			responseList.add(liqReponse);
		}
		liqDdaResponse.setLiqPostingResponse(responseList.toArray(new LIQPostingResponse[] {}));
		return liqDdaResponse;
	}
}
