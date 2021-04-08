package com.finastra.fbe.atm.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.iso8583.atm.processes.ATMSettlementPseudonymResolver;
import com.finastra.iso8583.atm.processes.ATMTransactionUtil;
import com.finastra.iso8583.atm.processes.ISOParsingConstants;
import com.finastra.openapi.common.utils.FBPErrorResponseHandler;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.eventcode.CommonEventCodes;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.exception.RetriableException;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.atm.ExternalLoroIndicator;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBlockingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOBlockingTransactions;
import com.trapedza.bankfusion.bo.refimpl.IBOExternalLoroSettlementAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOPosOperationDetails;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingRouter;
import com.trapedza.bankfusion.postingengine.router.PostingResponse;
import com.trapedza.bankfusion.postingengine.services.IPostingEngine;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.ub.types.atm.CardIssuerData;
import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessage;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessages;
import bf.com.misys.ub.types.iso8583.UB_Financial_Details;

public class PosOperationHelper {

	private static final Log LOGGER = LogFactory.getLog(OfflinePosCompletionProcessor.class.getName());
	private static final String POSREFUND = "Purchase return (Credit)";
	private static final String POSCASHBACK = "Account Credit Adjustment";
	private static final String ISSUERFEE = "Issuer Fee";
	static final String line = "==================";
	static final String logger_ERROR_DESCRIPTION = "  ErrorDescription : ";
	static final String logger_ERROR_CODE = "   ErrorCode";
	static final String logger_TRANSACTION_REFERENCE = " Transaction Reference : ";
	static final String logger_UETR = " Unique End Transaction Reference : ";

	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	private final IPersistenceService pService = (IPersistenceService) ServiceManagerFactory.getInstance()
			.getServiceManager().getServiceForName(ServiceManager.PERSISTENCE_SERVICE);

	private IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
			.getInstance().getServiceManager()
			.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
	private ChargeCollection chargeCollection = new ChargeCollection();

	String isSuspensePosted = "N";
	String suspAccount = "";

	@SuppressWarnings("deprecation")
	public void unblockPost(String boId) {
		LOGGER.info("Processing of processId started:::" + boId);
		IBOPosOperationDetails posOpDetail = (IBOPosOperationDetails) factory
				.findByPrimaryKey(IBOPosOperationDetails.BONAME, boId, true);
		HashMap<String, Object> params = new HashMap<>();
		String account = posOpDetail.getF_ACCOUNTID();
		String refNum = "";
		String endTxnRef = posOpDetail.getF_CMSUNIQUEENDTXNREF();
		String reconcurrency = posOpDetail.getF_RECONCURRENCY();
		BigDecimal reconAmount = posOpDetail.getF_AMOUNTRECON();
		String acqId = posOpDetail.getF_ACQUIRINGINSTITUTIONID();
		String cardIssuerFIID = posOpDetail.getF_ISSUERID();
		String terminalNumber = posOpDetail.getF_TERIMANALID();
		String cardNumber = posOpDetail.getF_CARDNUMBER();
		String terminalType = posOpDetail.getF_TERMINALTYPE();
		String operationType = posOpDetail.getF_OPERATIONTYPE();
		BigDecimal amountAccount = posOpDetail.getF_AMOUNTACCOUNT();

		boolean errorRaised = false;
		boolean isForcePosted = false;
		String status = "F";
		int errorCode = 0;
		String errorDesc = "";
		IBOATMActivityDetail atmAct = null;
		String isMatched = "N";
		String origTxnCode = null;
		String channelID = "POS";
		HashMap response = null;

		try {
			refNum = endTxnRef;
			factory.beginTransaction();

			if ("OPTP0020".equals(operationType) || "OPTP0422".equals(operationType)) {

				acqId = validateandGetImdCode(acqId, operationType);

				// ======================== Credit Transaction Starts ========================

				IBOAttributeCollectionFeature accountDetail = (IBOAttributeCollectionFeature) factory
						.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, account, true);
				if (null != accountDetail) {

					// cardIssuerFIID = validateandGetImdCode(cardIssuerFIID, operationType);
					if ("N".equals(isSuspensePosted) && null != accountDetail && (!(accountDetail.isF_CLOSED()))
							&& (!(accountDetail.isF_STOPPED())) && (!(accountDetail.isF_DORMANTSTATUS()))) {
						isSuspensePosted = "N";
						response = postFinancial(reconAmount, account, reconcurrency, terminalNumber, acqId,
								cardIssuerFIID, refNum, Boolean.FALSE, channelID, operationType);
					} else {
						isSuspensePosted = "Y";
						response = susPost(reconAmount, reconcurrency, terminalNumber, acqId, cardIssuerFIID, refNum,
								channelID, operationType);
					}

					List<PostingResponse> prResponse = ((List<PostingResponse>) response.get("prResponse"));
					if (prResponse != null && prResponse.size() > 0) {
						status = "P";
					} else {
						status = "F";
						errorCode = PosOperationEventCodes.TECHNICAL_EXCEPTION;
						errorRaised = true;
					}
				} else {
					isSuspensePosted = "Y";
					EventsHelper.handleEvent(PosOperationEventCodes.ACCOUNT_ID_DOES_NOT_EXIST, new Object[] {},
							new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
				}
			} else {

				// ======================== Debit Transaction Starts ========================
				acqId = validateandGetImdCode(acqId, operationType);
				// cardIssuerFIID = validateandGetImdCode(cardIssuerFIID, operationType);
				if ("N".equals(isSuspensePosted) && (atmAct = matchTransaction(endTxnRef)) != null) {
					isMatched = "Y";
					refNum = endTxnRef;

					if (StringUtils.isNotEmpty(atmAct.getF_ATMTRANSACTIONCODE())) {
						origTxnCode = atmAct.getF_ATMTRANSACTIONCODE();
						channelID = getChannelId(origTxnCode);
					}

					blockPosting(account, refNum, atmAct.getF_TRANSACTIONAMOUNT(), atmAct, acqId, cardIssuerFIID,
							channelID);
					if (isSufficientBalance(account, reconAmount, reconcurrency)) {
						isSuspensePosted = "N";
						response = postFinancial(reconAmount, account, reconcurrency, terminalNumber, acqId,
								cardIssuerFIID, refNum, Boolean.FALSE, channelID, operationType);
					} else {

						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Insufficient balance::::::");
						}

						String cModuleSuspenseOrCardH = getModuleConfig("ATM", "SUSPENSEORCARDHOLDER");

						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Module config SUSPENSEORCARDHOLDER:::" + cModuleSuspenseOrCardH);
						}
						if (cModuleSuspenseOrCardH.equals("Cardholder")) {
							isSuspensePosted = "N";
							isForcePosted = Boolean.TRUE;
							blockPosting(account, refNum, atmAct.getF_TRANSACTIONAMOUNT(), atmAct, acqId,
									cardIssuerFIID, channelID);
							response = postFinancial(reconAmount, account, reconcurrency, terminalNumber, acqId,
									cardIssuerFIID, refNum, isForcePosted, channelID, operationType);
						} else {
							isSuspensePosted = "Y";
							EventsHelper.handleEvent(PosOperationEventCodes.INSUFFICIENT_FUND, new Object[] {},
									new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
						}
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Is suspense Posted matched transaction:::" + isSuspensePosted);
							LOGGER.info("Is force Posted matched transaction:::" + isForcePosted);
						}
					}
				} else {
					isMatched = "N";
					if (StringUtils.isNotEmpty(account) && isSufficientBalance(account, reconAmount, reconcurrency)) {
						isSuspensePosted = "N";
						response = postFinancial(reconAmount, account, reconcurrency, terminalNumber, acqId,
								cardIssuerFIID, refNum, Boolean.FALSE, channelID, operationType);

					} else {
						isSuspensePosted = "Y";
						EventsHelper.handleEvent(PosOperationEventCodes.ACCOUNT_ID_DOES_NOT_EXIST, new Object[] {},
								new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
					}
				}

				status = "P";
			}
			refNum = posOpDetail.getF_ORIGINATORREFNUM();
			updateAtmActvity(account, refNum, acqId, terminalNumber, cardNumber, status, response, errorCode,
					reconcurrency, endTxnRef, reconAmount, terminalType, operationType, cardIssuerFIID, isMatched,
					amountAccount, cardIssuerFIID, origTxnCode, isForcePosted, errorDesc);
		} catch (RetriableException ex) {
			factory.rollbackTransaction();
			factory.beginTransaction();
			throw new RetriableException(CommonEventCodes.E_STALE_STATE_EXCEPTION, CommonConstants.EMPTY_STRING);
		} catch (BankFusionException bfEx) {
			factory.rollbackTransaction();
			factory.beginTransaction();
			LOGGER.error("Following exception occurred: " + bfEx.toString(), bfEx);
			com.misys.fbp.events.model.Error errorResponse = FBPErrorResponseHandler.createErrorResponse(bfEx,
					ISOParsingConstants.ERROR_TYPE, ATMTransactionUtil.getErrorResponseTitle(), null);
			errorCode = Integer.parseInt(errorResponse.getCauses().get(0).getCode());
			errorDesc = errorResponse.getCauses().get(0).getMessage();

			LOGGER.error(line + logger_TRANSACTION_REFERENCE + posOpDetail.getF_ORIGINATORREFNUM() + logger_UETR
					+ endTxnRef + logger_ERROR_CODE + errorCode + logger_ERROR_DESCRIPTION + errorDesc + line);

			response = susPost(reconAmount, reconcurrency, terminalNumber, acqId, cardIssuerFIID, refNum, channelID,
					operationType);
			status = "P";
			updateAtmActvity(suspAccount, refNum, acqId, terminalNumber, cardNumber, status, response, errorCode,
					reconcurrency, endTxnRef, reconAmount, terminalType, operationType, cardIssuerFIID, isMatched,
					amountAccount, cardIssuerFIID, origTxnCode, isForcePosted, errorDesc);
			updatePosOperationDetails(boId, status, errorCode, errorDesc, isMatched, isSuspensePosted, response);
			errorRaised = false;
		} catch (Exception e) {
			factory.rollbackTransaction();
			factory.beginTransaction();

			LOGGER.error("Following exception occurred: " + e.toString(), e);
			com.misys.fbp.events.model.Error errorResponse = FBPErrorResponseHandler.createErrorResponse(e,
					ISOParsingConstants.ERROR_TYPE, ATMTransactionUtil.getErrorResponseTitle(), null);
			errorCode = Integer.parseInt(errorResponse.getCauses().get(0).getCode());
			errorDesc = errorResponse.getCauses().get(0).getMessage();

			LOGGER.error(line + logger_TRANSACTION_REFERENCE + posOpDetail.getF_ORIGINATORREFNUM() + logger_UETR
					+ endTxnRef + logger_ERROR_CODE + errorCode + logger_ERROR_DESCRIPTION + errorDesc + line);

			response = susPost(reconAmount, reconcurrency, terminalNumber, acqId, cardIssuerFIID, refNum, channelID,
					operationType);
			status = "P";
			updateAtmActvity(suspAccount, refNum, acqId, terminalNumber, cardNumber, status, response, errorCode,
					reconcurrency, endTxnRef, reconAmount, terminalType, operationType, cardIssuerFIID, isMatched,
					amountAccount, cardIssuerFIID, origTxnCode, isForcePosted, errorDesc);
			updatePosOperationDetails(boId, status, errorCode, errorDesc, isMatched, isSuspensePosted, response);

			errorRaised = false;
		} finally {
			if (factory != null) {
				factory.commitTransaction();
				factory.closePrivateSession();
			}
		}
		if (errorCode == 0) {
			errorCode = PosOperationEventCodes.TECHNICAL_EXCEPTION;
		}
		updatePosOperationDetails(boId, status, errorCode, errorDesc, isMatched, isSuspensePosted, response);

		if (errorRaised) {
			EventsHelper.handleEvent(errorCode, new Object[] {}, new HashMap(),
					BankFusionThreadLocal.getBankFusionEnvironment());
		}
		LOGGER.info("Processing of processId ended:::" + boId);
	}

	private String validateandGetImdCode(String acqId, String operationType) throws Exception {

		if (StringUtils.isNotBlank(acqId) && StringUtils.isNotBlank(operationType)) {
			acqId = (new ATMSettlementPseudonymResolver()).getImdCode(acqId, operationType);
			if (StringUtils.isBlank(acqId)) {
				isSuspensePosted = "Y";
			}
		} else {
			isSuspensePosted = "Y";
		}

		return acqId;
	}

	private String getChannelId(String origTxnCode) {
		String channelID = null;
		if (origTxnCode.equals("CSHWTD")) {
			channelID = "ATM";
		} else {
			channelID = "POS";
		}
		return channelID;
	}

	private boolean isSufficientBalance(String account, BigDecimal reconAmount, String reconcurrency) {
		// TODO Auto-generated method stub

		IBOAttributeCollectionFeature accountDetail = (IBOAttributeCollectionFeature) factory
				.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, account, true);
		if (null != accountDetail && (!(accountDetail.isF_CLOSED())) && (!(accountDetail.isF_STOPPED()))
				&& (!(accountDetail.isF_DORMANTSTATUS())) && StringUtils.isNumeric(reconcurrency)) {
			reconcurrency = SystemInformationManager.getInstance().transformCurrencyCode(reconcurrency, true);
			if (accountDetail.getF_ISOCURRENCYCODE().equals(reconcurrency)) {
				if (reconAmount.compareTo(accountDetail.getF_CLEAREDBALANCE()) == -1) {
					return true;
				}
			} else {
				HashMap params = new HashMap();
				params.put("buyAmount", reconAmount);
				params.put("buyCurrency", reconcurrency);
				params.put("sellCurrency", accountDetail.getF_ISOCURRENCYCODE());
				HashMap outParam = MFExecuter.executeMF("CB_FEX_CalculateExchangeRateAmount_SRV",
						BankFusionThreadLocal.getBankFusionEnvironment(), params);
				CalcExchangeRateRs calcExRs = (CalcExchangeRateRs) outParam.get("CalcExchangeRateRs");
				BigDecimal sellAmount = calcExRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
				if (sellAmount.compareTo(accountDetail.getF_CLEAREDBALANCE()) == -1) {
					return true;
				}
			}
		}
		return false;
	}

	private void updatePosOperationDetails(String boId, String status, int errorCode, String errorDesc,
			String isMatched, String isSuspensePosted, HashMap response) {

		IPersistenceObjectsFactory privateFactory = null;
		try {
			privateFactory = pService.getPrivatePersistenceFactory(false);
			privateFactory.beginTransaction();

			IBOPosOperationDetails posOpDtl = (IBOPosOperationDetails) privateFactory
					.findByPrimaryKey(IBOPosOperationDetails.BONAME, boId);
			if (status.equals("F")) {
				posOpDtl.setF_ERRORCODE(Integer.toString(errorCode));
				if (null != errorDesc && !errorDesc.isEmpty()) {
					posOpDtl.setF_ERRORDESC(errorDesc);
				} else {
					try {
						posOpDtl.setF_ERRORDESC(BankFusionMessages.getFormattedMessage(errorCode, new String[] {}));
					} catch (Exception e) {
						LOGGER.info("Unable to resolve error message for :::" + errorCode);
					}
				}

			} else {
				posOpDtl.setF_CREDITACCOUNTID(((IBOAttributeCollectionFeature) response.get("creditAccId")).getBoID());
				posOpDtl.setF_ISMATCHEDTXN(isMatched);
				posOpDtl.setF_ISSUSPOSTED(isSuspensePosted);
				posOpDtl.setF_TRANSACTIONID((String) response.get("TransId"));
			}

		} catch (Exception exception) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
			if (privateFactory != null) {
				privateFactory.rollbackTransaction();
			}

		} finally {
			if (privateFactory != null) {
				privateFactory.commitTransaction();
				privateFactory.closePrivateSession();
			}
		}
	}

	private void blockPosting(String account, String refNum, BigDecimal reconAmount, IBOATMActivityDetail atmAct,
			String accIDC, String cardIssAuthData, String channelID) {
		String narrative = null;
		String misTxnCode = null;
		String indicator = getIndicator(accIDC, cardIssAuthData);
		String atmTxnCode = (String) ubInformationService.getBizInfo().getModuleConfigurationValue("UpdateAccountHold",
				"UpdateAccountHold", BankFusionThreadLocal.getBankFusionEnvironment());

		List<IBOATMTransactionCodes> ibotransactionCodes = getAtmTxnCodeDtls(indicator, atmTxnCode);
		if (ibotransactionCodes != null && ibotransactionCodes.size() > 0) {
			narrative = ibotransactionCodes.get(0).getF_NARRATIVE();
			misTxnCode = ibotransactionCodes.get(0).getF_MISTRANSACTIONCODE();
		}
		LOGGER.info("blockPosting narrative::::" + narrative);
		LOGGER.info("blockPosting misTxnCode::::" + misTxnCode);
		validateMisTxnCode(indicator, misTxnCode, atmTxnCode);
		IBOBlockingMessage unblockingMessg = createUnblockMessage(account, refNum, atmAct.getF_TRANSACTIONID(),
				narrative, misTxnCode, channelID);
		ArrayList<IPostingMessage> ubPostList = new ArrayList<>();
		ubPostList.add(unblockingMessg);
		postMessages(ubPostList, BankFusionThreadLocal.getBankFusionEnvironment());
	}

	private void updateAtmActvity(String account, String refNum, String acqId, String terminalNumber, String cardNumber,
			String status, HashMap response, int errorCode, String reconCurr, String uniqueTxnRef, BigDecimal txnAmount,
			String terminalType, String operationType, String issuerId, String isMatched, BigDecimal amountAccount,
			String cardIssuerFIID, String txnCode, Boolean isForcePosted, String errorDesc) {
		IPersistenceObjectsFactory privateFactory = null;
		try {
			privateFactory = pService.getPrivatePersistenceFactory(false);

			IBOATMActivityDetail atmActivityDetail = (IBOATMActivityDetail) factory
					.getStatelessNewInstance(IBOATMActivityDetail.BONAME);
			// IBOISOATM_ActivityUpdate atmActivityDetailISOExtn =
			// (IBOISOATM_ActivityUpdate) factory
			// .getStatelessNewInstance(IBOISOATM_ActivityUpdate.BONAME);
			String atmActivityPk = GUIDGen.getNewGUID();
			String processingCode = "19";
			int forcePost = 0;
			if (isForcePosted) {
				forcePost = 1;
			}

			if ("CSHWTD".equalsIgnoreCase(txnCode)) {
				processingCode = "01";
			}
			if ("OPTP0020".equals(operationType) || "OPTP0422".equals(operationType)) {
				processingCode = "20";
			}

			atmActivityDetail.setBoID(atmActivityPk);
			atmActivityDetail.setF_ATMDEVICEID(terminalNumber);
			atmActivityDetail.setF_SOURCECIB(acqId);
			atmActivityDetail.setF_TRANSACTIONDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
			atmActivityDetail.setF_MSGRECVDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
			atmActivityDetail.setF_POSTDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
			atmActivityDetail.setF_ERRORDESC(errorDesc);

			if (CommonUtil.checkIfNotNullOrEmpty(cardNumber)) {
				atmActivityDetail.setF_ATMCARDNUMBER(cardNumber);
			} else {
				atmActivityDetail.setF_ATMCARDNUMBER("NON-JMS");
			}
			atmActivityDetail.setF_ACCOUNTID(account);
			atmActivityDetail.setF_TRANSACTIONREFERENCE(refNum);
			/*
			 * atmActivityDetailISOExtn.setBoID(atmActivityPk);
			 * atmActivityDetailISOExtn.setF_UBAQUIRERID(acqId);
			 * atmActivityDetailISOExtn.setF_UBTERMINALDATA(terminalNumber);
			 * atmActivityDetailISOExtn.setF_UBCARDISSUERDATA(issuerId);
			 * atmActivityDetailISOExtn.setF_UBACTUALTXNAMOUNT(txnAmount);
			 * atmActivityDetailISOExtn.setF_UBTERMINALDATA(terminalType);
			 * atmActivityDetailISOExtn.setF_UBOPERATIONTYPE(operationType);
			 * atmActivityDetailISOExtn.setF_UBPROCESSINGCODE(processingCode); if
			 * ("Y".equals(isMatched)) { atmActivityDetailISOExtn.setF_UBISMATCHEDTXN(true);
			 * } else { atmActivityDetailISOExtn.setF_UBISMATCHEDTXN(false); }
			 * atmActivityDetailISOExtn.setF_UBAMOUNTACCOUNT(amountAccount);
			 * atmActivityDetailISOExtn.setF_UBNETWORK(cardIssuerFIID);
			 */
			atmActivityDetail.setF_UBCMSUNIQUEENDTXNREF(uniqueTxnRef);
			atmActivityDetail.setF_ACCOUNTCURRENCY(reconCurr);
			atmActivityDetail.setF_TRANSACTIONAMOUNT(txnAmount);
			atmActivityDetail.setF_ATMTRANSACTIONCODE((String) response.get("atmTxnCode"));
			atmActivityDetail.setF_MISTRANSACTIONCODE((String) response.get("misTxnCode"));
			// atmActivityDetail.setF_TRANSACTIONID(((List<PostingResponse>)
			// response.get("prResponse")).get(0).getTransactionId());
			atmActivityDetail
					.setF_ISOCURRENCYCODE((StringUtils.isNumeric(reconCurr)) ? Integer.parseInt(reconCurr) : 0);
			if ("OPTP0020".equals(operationType)) {
				atmActivityDetail.setF_ATMTRANDESC(POSREFUND);
			} else if ("OPTP0422".equals(operationType)) {
				atmActivityDetail.setF_ATMTRANDESC(POSCASHBACK);
			} else if ("OPTP0119".equals(operationType)) {
				atmActivityDetail.setF_ATMTRANDESC(ISSUERFEE);
			} else {
				if (null != response.get("TranDesc") && ((String) response.get("TranDesc")).length() > 27) {
					atmActivityDetail.setF_ATMTRANDESC(((String) response.get("TranDesc")).substring(0, 26));
				} else {
					atmActivityDetail.setF_ATMTRANDESC(((String) response.get("TranDesc")));
				}
			}
			if (null != response.get("TransId")) {
				atmActivityDetail.setF_TRANSACTIONID((String) response.get("TransId"));
			}
			atmActivityDetail.setF_DESTACCOUNTID((String) response.get("DestAccount"));
			atmActivityDetail.setF_ERRORSTATUS("0");
			atmActivityDetail.setF_FORCEPOST(forcePost);

			// factory.create(IBOISOATM_ActivityUpdate.BONAME, atmActivityDetailISOExtn);
			factory.create(IBOATMActivityDetail.BONAME, atmActivityDetail);

		} catch (Exception exception) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
		}
	}

	private HashMap<String, Object> postFinancial(BigDecimal reconAmount, String accountId, String reconcurrency,
			String terminalNumber, String acqId, String cardIssuerFIID, String txnRef, boolean isForcePost,
			String channelID, String operationType) {
		// TODO Auto-generated method stub
		HashMap<String, Object> postingResponse = new HashMap<>();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("::::Inside postFinancial:::");
			LOGGER.info("::reconcurrency,terminalNumber,acqId,cardIssuerFIID,txnRef:::::" + reconcurrency + "::"
					+ terminalNumber + "::" + acqId + "::" + cardIssuerFIID + "::" + txnRef);
		}

		if (StringUtils.isNumeric(reconcurrency) && StringUtils.isNotBlank(acqId)
				&& StringUtils.isNotBlank(cardIssuerFIID) && StringUtils.isNotBlank(txnRef)) {
			String indicator = getIndicator(acqId, cardIssuerFIID);
			reconcurrency = SystemInformationManager.getInstance().transformCurrencyCode(reconcurrency, true);
			IBOAttributeCollectionFeature creditAccount = null;
			IBOAttributeCollectionFeature debitAccount = null;
			if (StringUtils.isBlank(terminalNumber)) {
				indicator = "02";
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("ExternalLoro Indicator::::" + indicator);
			}
			if (indicator.equals("01")) {
				IBOATMSettlementAccount iboatmSettlementAccount = (IBOATMSettlementAccount) factory
						.findByPrimaryKey(IBOATMSettlementAccount.BONAME, terminalNumber);

				String crAccountPseudonym = iboatmSettlementAccount.getF_CASHSETTLEMENTACCOUNT();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("debit account and credit account Pseudonym::::::" + "na" + ":::" + crAccountPseudonym);
				}
				if (StringUtils.isNotBlank(crAccountPseudonym)) {
					debitAccount = (IBOAttributeCollectionFeature) factory
							.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, accountId, false);
					creditAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + crAccountPseudonym + "%", reconcurrency, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
				} else {
					throw new BankFusionException();
				}
			}
			if (indicator.equals("02")) {
				IBOExternalLoroSettlementAccount iboExtatmSettlementAccount = (IBOExternalLoroSettlementAccount) factory
						.findByPrimaryKey(IBOExternalLoroSettlementAccount.BONAME, acqId);
				String crAccountPseudonym = null;
				if (channelID.equals("POS")) {
					crAccountPseudonym = iboExtatmSettlementAccount.getF_POSSETTLEMENTACCOUNT();
				} else {
					crAccountPseudonym = iboExtatmSettlementAccount.getF_SETTLEMENTACCOUNT();
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("debit account and credit account Pseudonym::::::" + "na" + ":::" + crAccountPseudonym);
				}
				if (StringUtils.isNotBlank(crAccountPseudonym)) {
					debitAccount = (IBOAttributeCollectionFeature) factory
							.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, accountId, false);
					creditAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + crAccountPseudonym + "%", reconcurrency, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
				} else {
					throw new BankFusionException();
				}
			} else if (indicator.equals("03")) {
				IBOExternalLoroSettlementAccount iboExtatmSettlementAccount = (IBOExternalLoroSettlementAccount) factory
						.findByPrimaryKey(IBOExternalLoroSettlementAccount.BONAME, cardIssuerFIID);

				String dbAccountPseudo = null;
				if (channelID.equals("POS")) {
					dbAccountPseudo = iboExtatmSettlementAccount.getF_POSSETTLEMENTACCOUNT();
				} else {
					dbAccountPseudo = iboExtatmSettlementAccount.getF_SETTLEMENTACCOUNT();
				}

				IBOATMSettlementAccount iboatmSettlementAccount = (IBOATMSettlementAccount) factory
						.findByPrimaryKey(IBOATMSettlementAccount.BONAME, terminalNumber);
				String crAccountPseudonym = iboatmSettlementAccount.getF_CASHSETTLEMENTACCOUNT();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("debit account and credit account Pseudonym::::::" + dbAccountPseudo + ":::"
							+ crAccountPseudonym);
				}
				if (StringUtils.isNotBlank(dbAccountPseudo) && StringUtils.isNotBlank(crAccountPseudonym)) {
					debitAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + dbAccountPseudo + "%", reconcurrency, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
					creditAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + crAccountPseudonym + "%", reconcurrency, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
				}
			}
			if (creditAccount != null && debitAccount != null) {
				String narrative = null;
				String misTxnCode = null;
				BigDecimal exchRate = null;
				String exchRateType = null;
				String atmTxnCode = (String) ubInformationService.getBizInfo().getModuleConfigurationValue(
						"UpdateAccountHold", "UpdateAccountHold", BankFusionThreadLocal.getBankFusionEnvironment());

				List<IBOATMTransactionCodes> ibotransactionCodes = getAtmTxnCodeDtls(indicator, atmTxnCode);
				if (!ibotransactionCodes.isEmpty()) {
					narrative = ibotransactionCodes.get(0).getF_NARRATIVE();
					misTxnCode = ibotransactionCodes.get(0).getF_MISTRANSACTIONCODE();
				}
				validateMisTxnCode(indicator, misTxnCode, atmTxnCode);
				BigDecimal debitAmount = reconAmount;
				HashMap outParam = getExchangeRateDetail(reconcurrency, debitAccount, misTxnCode);
				if (outParam != null) {
					exchRate = (BigDecimal) outParam.get("ExchangeRate");
					exchRateType = (String) outParam.get("ExchangeRateType");
					debitAmount = debitAmount.multiply(exchRate);
				}
				ArrayList<IPostingMessage> iboFinancialPostingMessages = new ArrayList<>();
				String txnId = GUIDGen.getNewGUID();

				if ("OPTP0020".equals(operationType) || "OPTP0422".equals(operationType)) {

					if ("OPTP0422".equals(operationType)) {
						narrative = POSCASHBACK;
					} else {
						narrative = POSREFUND;
					}

					iboFinancialPostingMessages.add(createFinancialMessage(debitAmount,
							debitAccount.getF_ISOCURRENCYCODE(), txnRef, creditAccount, misTxnCode, narrative, '-',
							exchRate, exchRateType, txnId, isForcePost, channelID));
					iboFinancialPostingMessages
							.add(createFinancialMessage(reconAmount, reconcurrency, txnRef, debitAccount, misTxnCode,
									narrative, '+', exchRate, exchRateType, txnId, isForcePost, channelID));

				} else {

					if ("OPTP0119".equals(operationType)) {
						narrative = ISSUERFEE;
					}
					iboFinancialPostingMessages.add(createFinancialMessage(debitAmount,
							debitAccount.getF_ISOCURRENCYCODE(), txnRef, debitAccount, misTxnCode, narrative, '-',
							exchRate, exchRateType, txnId, isForcePost, channelID));
					iboFinancialPostingMessages
							.add(createFinancialMessage(reconAmount, reconcurrency, txnRef, creditAccount, misTxnCode,
									narrative, '+', exchRate, exchRateType, txnId, isForcePost, channelID));
				}
				UB_Atm_PostingMessage atmPostingMessage = chargeCollection.getCharges(debitAccount.getBoID(),
						misTxnCode, indicator, terminalNumber, creditAccount.getBoID(), reconAmount, reconcurrency,
						txnRef);

				if (atmPostingMessage != null) {
					UB_Atm_PostingMessages[] atmPostingMsgs = atmPostingMessage.getUB_Atm_PostingMessages();
					for (UB_Atm_PostingMessages ubatmPostMsg : atmPostingMsgs) {
						if (validateatmChargePost(ubatmPostMsg)) {
							IBOAttributeCollectionFeature account = (IBOAttributeCollectionFeature) factory
									.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, ubatmPostMsg.getPRIMARYID(),
											false);
							iboFinancialPostingMessages.add(createFinancialMessage(ubatmPostMsg.getAMOUNT(),
									ubatmPostMsg.getTXNCURRENCYCODE(), txnRef, account,
									ubatmPostMsg.getTRANSACTIONCODE(), ubatmPostMsg.getNARRATIVE(),
									(ubatmPostMsg.getSIGN().equals("+")) ? '+' : '-', ubatmPostMsg.getEXCHRATE(),
									ubatmPostMsg.getEXCHRATETYPE(), txnId, isForcePost, channelID));
						}
					}
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Posting Message:::" + iboFinancialPostingMessages);
				}
				postingResponse.put("accountCurrency", debitAccount.getF_ISOCURRENCYCODE());
				postingResponse.put("atmTxnCode", atmTxnCode);
				postingResponse.put("misTxnCode", misTxnCode);
				postingResponse.put("creditAccId", creditAccount);
				List<PostingResponse> postingResponses = postMessages(iboFinancialPostingMessages,
						BankFusionThreadLocal.getBankFusionEnvironment());
				if (postingResponses.size() > 0) {
					if (chargeCollection.getIsChargeWaivedBasedOnCounter()) {
						chargeCollection.updateChargeCounter(accountId, "I", misTxnCode,
								SystemInformationManager.getInstance().getBFBusinessDate());
					}
				}
				postingResponse.put("prResponse", postingResponses);
				postingResponse.put("TranDesc", narrative);
				postingResponse.put("TransId", txnId);
				postingResponse.put("DestAccount", creditAccount.getBoID());
			} else {
				LOGGER.error("Debit account or credit account is NULL");
				if (debitAccount == null) {
					EventsHelper.handleEvent(PosOperationEventCodes.DEBIT_ACCOUNT_NOT_FOUND, new Object[] {},
							new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
				} else {
					EventsHelper.handleEvent(PosOperationEventCodes.CREDIT_ACCOUNT_NOT_FOUND, new Object[] {},
							new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
				}
			}

		} else {
			LOGGER.error("Required parameters not available for posting");
			EventsHelper.handleEvent(PosOperationEventCodes.ERROR_OCCURED_WHILE_PREPARING_POST_MSG, new Object[] {},
					new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
		}
		return postingResponse;
	}

	private boolean validateatmChargePost(UB_Atm_PostingMessages ubatmPostMsg) {
		// TODO Auto-generated method stub
		if (StringUtils.isBlank(ubatmPostMsg.getPRIMARYID()) || StringUtils.isBlank(ubatmPostMsg.getTRANSACTIONCODE())
				|| StringUtils.isBlank(ubatmPostMsg.getTXNCURRENCYCODE())
				|| StringUtils.isBlank(ubatmPostMsg.getSIGN())) {
			return false;
		}
		return true;
	}

	private List<IBOATMTransactionCodes> getAtmTxnCodeDtls(String indicator, String atmTxnCode) {
		ArrayList<String> param = new ArrayList<>();
		param.add(atmTxnCode);
		param.add(indicator);
		List<IBOATMTransactionCodes> ibotransactionCodes = null;
		ibotransactionCodes = (List<IBOATMTransactionCodes>) factory.findByQuery(IBOATMTransactionCodes.BONAME,
				" where " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + " = ? and "
						+ IBOATMTransactionCodes.UBATMTRANSACTIONTYPE + " = ? ",
				param, null);
		return ibotransactionCodes;
	}

	private HashMap getExchangeRateDetail(String reconcurrency, IBOAttributeCollectionFeature debitAccount,
			String misTxnCode) {
		HashMap outParam = null;
		if (!debitAccount.getF_ISOCURRENCYCODE().equals(reconcurrency)) {
			HashMap params = new HashMap();
			params.put("FromCurrencyCode", debitAccount.getF_ISOCURRENCYCODE());
			params.put("ToCurrencyCode", reconcurrency);
			params.put("TxnCode", misTxnCode);
			outParam = MFExecuter.executeMF("UB_ATM_GetExcahgeRateDtlsOnTxnCode",
					BankFusionThreadLocal.getBankFusionEnvironment(), params);
		}
		return outParam;
	}

	/**
	 * @param reconAmount
	 * @param account
	 * @param txnRef
	 * @param narrative
	 *            TODO
	 * @param txnCode
	 *            TODO
	 * @param txnCode
	 * @param channelID
	 * @return
	 */
	private IBOBlockingMessage createUnblockMessage(String account, String txnRef, String transactionId,
			String narrative, String txnCode, String channelID) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Transaction Id for Unblocking::::" + transactionId);
		}
		IBOAttributeCollectionFeature accountValues = null;
		IBOBlockingMessage blockingMessage = null;
		try {
			accountValues = (IBOAttributeCollectionFeature) factory
					.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, account, false);
			if (accountValues == null) {
				throw new BankFusionException();
			}
		} catch (Exception e) {
			LOGGER.error("Account Not found for unblocking:::" + account);
			EventsHelper.handleEvent(PosOperationEventCodes.ACCOUNT_ID_DOES_NOT_EXIST, new Object[] {}, new HashMap(),
					BankFusionThreadLocal.getBankFusionEnvironment());
		}
		ArrayList<String> param = new ArrayList<>();
		param.add(transactionId);
		param.add("N");
		List<IBOBlockingTransactions> blockTransactions = (List<IBOBlockingTransactions>) factory
				.findByQuery(IBOBlockingTransactions.BONAME, "WHERE " + IBOBlockingTransactions.TRANSACTIONID
						+ " = ? AND " + IBOBlockingTransactions.UNBLOCKING + " = ?", param, null);
		if (blockTransactions != null && blockTransactions.size() > 0) {
			blockingMessage = (IBOBlockingMessage) BankFusionThreadLocal.getPersistanceFactory()
					.getStatelessNewInstance(IBOBlockingMessage.BONAME);
			blockingMessage.setF_PRIMARYID(account);
			blockingMessage.setProductID(accountValues.getF_PRODUCTID());
			blockingMessage.setMessageID(GUIDGen.getNewGUID());
			blockingMessage.setTransactionID(transactionId);
			blockingMessage.setF_BLOCKINGAMOUNT(blockTransactions.get(0).getF_AMOUNT());
			blockingMessage.setF_MESSAGETYPE("Z");
			blockingMessage.setF_BLOCKINGID(blockTransactions.get(0).getBoID());
			blockingMessage.setF_VALUEDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
			blockingMessage.setF_TRANSACTIONREF(txnRef);
			blockingMessage.setF_TRANSACTIONCODE(txnCode);
			blockingMessage.setF_NARRATIVE(narrative);
			blockingMessage.setF_BLOCKINGCATEGORY("Normal");
			blockingMessage.setF_UNBLOCKINGDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
			blockingMessage.setF_TRANSACTIONDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
			blockingMessage.setF_REVERSAL(Boolean.TRUE);
			blockingMessage.setF_SIGN("+");

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("blocking TransacitonId :" + blockingMessage.getF_BLOCKINGID());
			}
			blockingMessage.setF_CHANNELID(channelID);
		} else {
			LOGGER.error(
					"Blocking Id Not found for unblocking. account,transactionId:::" + account + ":::" + transactionId);
			EventsHelper.handleEvent(PosOperationEventCodes.BLOCK_ID_DOES_NOT_EXIST, new Object[] {}, new HashMap(),
					BankFusionThreadLocal.getBankFusionEnvironment());
		}
		return blockingMessage;
	}

	private HashMap<String, Object> susPost(BigDecimal reconAmount, String reconcurrency, String terminalNumber,
			String acqId, String cardIssuerFIID, String txnRef, String channelID, String operationType) {

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(":::::Inside unmatchedPost:::::");
			LOGGER.info("reconcurrency,terminalNumber,acqId,cardIssuerFIID,txnRef:::::" + reconcurrency + "::"
					+ terminalNumber + "::" + acqId + "::" + cardIssuerFIID + "::" + txnRef);
		}

		HashMap<String, Object> postingResponse = new HashMap<>();
		if (StringUtils.isNumeric(reconcurrency) && StringUtils.isNotBlank(acqId)
				&& StringUtils.isNotBlank(cardIssuerFIID) && StringUtils.isNotBlank(txnRef)) {
			String indicator = getIndicator(acqId, acqId);
			reconcurrency = SystemInformationManager.getInstance().transformCurrencyCode(reconcurrency, true);
			IBOAttributeCollectionFeature creditAccount = null;
			IBOAttributeCollectionFeature debitAccount = null;
			if (StringUtils.isBlank(terminalNumber)) {
				indicator = "02";
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("ExternalLoro Indicator::::" + indicator);
			}
			if (indicator.equals("01")) {

				IBOATMSettlementAccount iboatmSettlementAccount = (IBOATMSettlementAccount) factory
						.findByPrimaryKey(IBOATMSettlementAccount.BONAME, terminalNumber);
				String cModuleName = "BAT";
				String cParam = "suspenseAccount";
				String suspenseAccountPseudo = getModuleConfig(cModuleName, cParam);
				String craccountPseudonym = iboatmSettlementAccount.getF_CASHSETTLEMENTACCOUNT();

				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("debit account and credit account Pseudonym::::::" + suspenseAccountPseudo + ":::"
							+ craccountPseudonym);
				}
				if (StringUtils.isNotBlank(craccountPseudonym) && StringUtils.isNotBlank(suspenseAccountPseudo)) {
					debitAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + suspenseAccountPseudo + "%", reconcurrency,
							Boolean.TRUE, BankFusionThreadLocal.getBankFusionEnvironment(), null);
					creditAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + craccountPseudonym + "%", reconcurrency, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
				}
			} else if (indicator.equals("02")) {
				String suspenseAccountPseudo = (String) ubInformationService.getBizInfo().getModuleConfigurationValue(
						"BAT", "suspenseAccount", BankFusionThreadLocal.getBankFusionEnvironment());
				IBOExternalLoroSettlementAccount iboExtatmSettlementAccount = null;
				String crAccountPseudonym = "";
				if (StringUtils.isNotBlank(acqId)) {
					iboExtatmSettlementAccount = (IBOExternalLoroSettlementAccount) factory
							.findByPrimaryKey(IBOExternalLoroSettlementAccount.BONAME, acqId, true);
				}
				if (iboExtatmSettlementAccount != null) {
					if ("ATM".equals(channelID)) {
						crAccountPseudonym = iboExtatmSettlementAccount.getF_SETTLEMENTACCOUNT();
					} else {
						crAccountPseudonym = iboExtatmSettlementAccount.getF_POSSETTLEMENTACCOUNT();
					}
				} else {
					crAccountPseudonym = suspenseAccountPseudo;
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("debit account and credit account Pseudonym::::::" + suspenseAccountPseudo + ":::"
							+ crAccountPseudonym);
				}
				if (StringUtils.isNotBlank(suspenseAccountPseudo) && StringUtils.isNotBlank(crAccountPseudonym)) {
					debitAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + suspenseAccountPseudo + "%", reconcurrency,
							Boolean.TRUE, BankFusionThreadLocal.getBankFusionEnvironment(), null);
					creditAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + crAccountPseudonym + "%", reconcurrency, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
				}

			} else if (indicator.equals("03")) {
				IBOATMSettlementAccount iboatmSettlementAccount = (IBOATMSettlementAccount) factory
						.findByPrimaryKey(IBOATMSettlementAccount.BONAME, terminalNumber, true);

				String suspenseAccountPseudo = (String) ubInformationService.getBizInfo().getModuleConfigurationValue(
						"BAT", "suspenseAccount", BankFusionThreadLocal.getBankFusionEnvironment());
				String craccountPseudonym = iboatmSettlementAccount.getF_CASHSETTLEMENTACCOUNT();

				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("debit account and credit account Pseudonym::::::" + suspenseAccountPseudo + ":::"
							+ craccountPseudonym);
				}
				if (StringUtils.isNotBlank(craccountPseudonym) && StringUtils.isNotBlank(suspenseAccountPseudo)) {
					debitAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + suspenseAccountPseudo + "%", reconcurrency,
							Boolean.TRUE, BankFusionThreadLocal.getBankFusionEnvironment(), null);
					creditAccount = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + reconcurrency + "%" + craccountPseudonym + "%", reconcurrency, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
				}
			}
			if (creditAccount != null && debitAccount != null) {
				if (creditAccount.equals(debitAccount)) {
					EventsHelper.handleEvent(PosOperationEventCodes.SAME_ACCOUNT_ERROR, new Object[] {}, new HashMap(),
							BankFusionThreadLocal.getBankFusionEnvironment());
				}
				String narrative = null;
				String misTxnCode = null;
				BigDecimal exchRate = null;
				String exchRateType = null;
				String atmTxnCode = (String) ubInformationService.getBizInfo().getModuleConfigurationValue(
						"UpdateAccountHold", "UpdateAccountHold", BankFusionThreadLocal.getBankFusionEnvironment());

				List<IBOATMTransactionCodes> ibotransactionCodes = getAtmTxnCodeDtls(indicator, atmTxnCode);
				if (ibotransactionCodes != null && ibotransactionCodes.size() > 0) {
					narrative = ibotransactionCodes.get(0).getF_NARRATIVE();
					misTxnCode = ibotransactionCodes.get(0).getF_MISTRANSACTIONCODE();
				}
				validateMisTxnCode(indicator, misTxnCode, atmTxnCode);
				BigDecimal debitAmount = reconAmount;
				HashMap outParam = getExchangeRateDetail(reconcurrency, debitAccount, misTxnCode);
				if (outParam != null) {
					exchRate = (BigDecimal) outParam.get("ExchangeRate");
					exchRateType = (String) outParam.get("ExchangeRateType");
					debitAmount = debitAmount.multiply(exchRate);
				}
				ArrayList<IPostingMessage> iboFinancialPostingMessages = new ArrayList<>();
				String txnId = GUIDGen.getNewGUID();

				if ("OPTP0020".equals(operationType) || "OPTP0422".equals(operationType)) {

					if ("OPTP0422".equals(operationType)) {
						narrative = POSCASHBACK;
					} else {
						narrative = POSREFUND;
					}
					suspAccount = creditAccount.getBoID();

					iboFinancialPostingMessages.add(createFinancialMessage(debitAmount,
							debitAccount.getF_ISOCURRENCYCODE(), txnRef, creditAccount, misTxnCode, narrative, '-',
							exchRate, exchRateType, txnId, Boolean.TRUE, channelID));
					iboFinancialPostingMessages
							.add(createFinancialMessage(reconAmount, reconcurrency, txnRef, debitAccount, misTxnCode,
									narrative, '+', exchRate, exchRateType, txnId, Boolean.TRUE, channelID));

				} else {

					if ("OPTP0119".equals(operationType)) {
						narrative = ISSUERFEE;
					}
					suspAccount = debitAccount.getBoID();

					iboFinancialPostingMessages
							.add(createFinancialMessage(reconAmount, reconcurrency, txnRef, creditAccount, misTxnCode,
									narrative, '+', exchRate, exchRateType, txnId, Boolean.TRUE, channelID));
					iboFinancialPostingMessages
							.add(createFinancialMessage(debitAmount, reconcurrency, txnRef, debitAccount, misTxnCode,
									narrative, '-', exchRate, exchRateType, txnId, Boolean.TRUE, channelID));
				}
				postingResponse.put("creditAccId", creditAccount);
				postingResponse.put("TransId", txnId);
				postingResponse.put("prResponse",
						postMessages(iboFinancialPostingMessages, BankFusionThreadLocal.getBankFusionEnvironment()));
			} else {
				LOGGER.error("Debit account or credit account is NULL");
				if (debitAccount == null) {
					EventsHelper.handleEvent(PosOperationEventCodes.DEBIT_ACCOUNT_NOT_FOUND, new Object[] {},
							new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
				} else {
					EventsHelper.handleEvent(PosOperationEventCodes.CREDIT_ACCOUNT_NOT_FOUND, new Object[] {},
							new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
				}
			}

		} else {
			LOGGER.error("Required parameters not available for posting");
			EventsHelper.handleEvent(PosOperationEventCodes.ERROR_OCCURED_WHILE_PREPARING_POST_MSG, new Object[] {},
					new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
		}
		return postingResponse;
	}

	private String getModuleConfig(String cModuleName, String cParam) {
		return (String) ubInformationService.getBizInfo().getModuleConfigurationValue(cModuleName, cParam,
				BankFusionThreadLocal.getBankFusionEnvironment());
	}

	private void validateMisTxnCode(String indicator, String misTxnCode, String atmTxnCode) {
		if (StringUtils.isBlank(misTxnCode)) {
			LOGGER.error("Unable to find misTxnCode for atmTxnCode and txnType::::" + atmTxnCode + ":::" + indicator);
			EventsHelper.handleEvent(PosOperationEventCodes.ERROR_OCCURED_WHILE_PREPARING_POST_MSG, new Object[] {},
					new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
		}
	}

	@SuppressWarnings("unchecked")
	private List<PostingResponse> postMessages(ArrayList<IPostingMessage> postingMessages, BankFusionEnvironment env) {
		IServiceManager serviceManager = ServiceManagerFactory.getInstance().getServiceManager();
		IPostingEngine pe = (IPostingEngine) serviceManager.getServiceForName(ServiceManager.POSTING_ENGINE_SERVICE);
		IPostingRouter pr = (IPostingRouter) pe.getNewInstance();
		List<PostingResponse> postingResponses = pr.post(postingMessages, env);
		pe.postingComplete();
		return postingResponses;
	}

	private IBOFinancialPostingMessage createFinancialMessage(BigDecimal reconAmount, String reconcurrency,
			String txnRef, IBOAttributeCollectionFeature creditAccount, String atmTxnCode, String narrative, char sign,
			BigDecimal excRate, String excRateType, String txnId, boolean isForcePost, String channelID) {
		IBOFinancialPostingMessage finPostingMessage = (IBOFinancialPostingMessage) BankFusionThreadLocal
				.getPersistanceFactory().getStatelessNewInstance(IBOFinancialPostingMessage.BONAME);
		finPostingMessage.setMessageID(GUIDGen.getNewGUID());
		finPostingMessage.setPrimaryID(creditAccount.getBoID());
		finPostingMessage.setProductID(creditAccount.getF_PRODUCTID());
		finPostingMessage.setPERouterProfileID(creditAccount.getF_PEROUTERPROFILEID());
		finPostingMessage.setAcctCurrencyCode(creditAccount.getF_ISOCURRENCYCODE());
		finPostingMessage.setMessageType('N');
		finPostingMessage.setF_AMOUNT(reconAmount);
		finPostingMessage.setTransCode(atmTxnCode);
		finPostingMessage.setTransactionRef(txnRef);
		finPostingMessage.setTransactionID(txnId);
		finPostingMessage.setBranchID(BankFusionThreadLocal.getUserSession().getBranchSortCode());
		finPostingMessage.setTransactionDate((SystemInformationManager.getInstance().getBFBusinessDate()));
		finPostingMessage.setF_ACTUALAMOUNT(reconAmount);
		if (excRate != null && excRateType != null) {
			finPostingMessage.setF_EXCHRATE(excRate);
			finPostingMessage.setF_EXCHRATETYPE(excRateType);
		}
		finPostingMessage.setF_BASEEQUIVALENT(reconAmount);
		finPostingMessage.setF_TXNCURRENCYCODE(reconcurrency);
		finPostingMessage.setF_CHANNELID(channelID);
		finPostingMessage.setF_FORCEPOST(isForcePost);
		finPostingMessage.setNarrative(narrative);
		finPostingMessage.setSign(sign);
		if (sign == '+') {
			finPostingMessage.setF_AMOUNTCREDIT(reconAmount);
		} else {
			finPostingMessage.setF_AMOUNTDEBIT(reconAmount);
		}
		finPostingMessage.setValueDate((SystemInformationManager.getInstance().getBFBusinessDate()));
		return finPostingMessage;
	}

	public String getIndicator(String accIDC, String cardIssAuthData) {
		HashMap paramsForIndicator = new HashMap();
		HashMap result = null;
		UB_ATM_Financial_Details finDetails = new UB_ATM_Financial_Details();
		UB_Financial_Details acceptor = new UB_Financial_Details();
		CardIssuerData issuer = new CardIssuerData();
		acceptor.setAcquiringInstitutionId(accIDC);
		issuer.setCardIssuerFIID(cardIssAuthData);
		finDetails.setCardIssuerData(issuer);
		finDetails.setFinancialDetails(acceptor);

		/*
		 * paramsForIndicator.put("AtmPosting", finDetails); result =
		 * MFExecuter.executeMF("UB_ATM_ExternalLoroIndicator_SRV",
		 * BankFusionThreadLocal.getBankFusionEnvironment(), paramsForIndicator);
		 */
		result = new ExternalLoroIndicator().getLoroIndicator(finDetails);
		return (String) result.get("atmTxnType");
	}

	private IBOATMActivityDetail matchTransaction(String txnRef) {
		// TODO Auto-generated method stub
		List<IBOATMActivityDetail> posOp = null;
		if (StringUtils.isNotBlank(txnRef)) {
			IPersistenceObjectsFactory privateFactory = null;
			try {
				privateFactory = pService.getPrivatePersistenceFactory(false);

				privateFactory.beginTransaction();
				ArrayList param = new ArrayList();
				param.add(txnRef);

				posOp = (List<IBOATMActivityDetail>) privateFactory
						.findByQuery(
								IBOATMActivityDetail.BONAME, " WHERE " + IBOATMActivityDetail.UBCMSUNIQUEENDTXNREF
										+ " = ? AND " + IBOATMActivityDetail.TRANSACTIONID + " IS NOT NULL ",
								param, null);

			} catch (Exception exception) {
				LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
				if (privateFactory != null) {
					privateFactory.rollbackTransaction();
				}

			} finally {
				if (privateFactory != null)
					privateFactory.closePrivateSession();
			}
		}
		return ((posOp != null && posOp.size() != 0)) ? posOp.get(0) : null;
	}

}