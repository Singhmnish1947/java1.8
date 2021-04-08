package com.finastra.iso8583.atm.processes;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbp.common.event.FBPErrorResponseHandler;
import com.misys.fbp.events.model.Error;

public class ATMISOEssenceMapping {
	protected static final Log logger = LogFactory.getLog(ATMISOEssenceMapping.class.getName());

	static final String EssenceATMReq = "===============================Essence ATM Request====================================";
	static final String ISOParsedFields = "===============================ISO Parsed Fields====================================";
	static final String line = "==================";
	static final String UPDATE_ERROR_STATUS = "update_ERRORSTATUS";
	static final String UPDATE_ERROR_DESCRIPTION = "update_ERRORDESCRIPTION";
	static final String logger_ERROR_DESCRIPTION = "  ErrorDescription : ";
	static final String logger_ERROR_CODE = "   ErrorCode";
	static final String logger_TRANSACTION_REFERENCE = " Transaction Reference : ";
	static final String ISO8583ErrorCode_properties = "ISO8583ErrorCode.properties";
	static final String path_ISO8583ErrorCode_properties = "conf/business/atm/";
	static final String commitSuccessful = "Commit Successful";
	static final String commitFailure = "Commit Failure";

	public HashMap<String, Object> processAtmTransaction(Message isoMessage) {

		HashMap<String, Object> essenceRs = null;
		HashMap<String, Object> essenceRq = null;
		ATMTransactionHelper helper = new ATMTransactionHelper();
		HashMap<String, Object> rawResponse = new HashMap<String, Object>();
		HashMap<String, Object> isoMessageFields = isoMessage.getMessageFields();

		if (logger.isInfoEnabled()) {
			logger.info(ISOParsedFields);
			logger.info(isoMessageFields);
			logger.info(ISOParsedFields);
		}
		String messageTypeIdentifier = isoMessage.getMessageTypeIdentifier();

		if (ISOParsingConstants._1804.equals(messageTypeIdentifier)) {
			isoMessage.setMessageTypeIdentifier(ISOParsingConstants._1814);
			rawResponse = isoMessageFields;
			rawResponse.put(ISOParsingConstants.responseCode, ISOParsingConstants._000);
			BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
		} else {

			String field48 = (String) isoMessageFields.get(ISOParsingConstants.additionalData);
			HashMap<String, String> field48Parsed = ParsingEngine.parseField48and54(field48);

			ATMSettlementPseudonymResolver atmSettlementPseudonymResolver = new ATMSettlementPseudonymResolver();

			BankFusionThreadLocal.setApplicationID(ISOParsingConstants.ATM);
			String processingCode = (((HashMap<String, Object>) isoMessageFields
					.get(ISOParsingConstants.processingCode)).get(ISOParsingConstants.transactionTypeCode)).toString();
			if (ISOParsingConstants.ZERO_ZERO.equals(processingCode) || ISOParsingConstants._18.equals(processingCode)
					|| ISOParsingConstants._19.equals(processingCode) || ISOParsingConstants._20.equals(processingCode)
					|| ISOParsingConstants.POS.equals(ATMSettlementPseudonymResolver.getChanneId())) {
				POSISOEssenceMapping posIsoEssenceMapping = new POSISOEssenceMapping();
				rawResponse = posIsoEssenceMapping.processPosTransaction(isoMessage, processingCode,
						messageTypeIdentifier, field48Parsed);
			} else {
				try {

					String imdCode = atmSettlementPseudonymResolver.getImdCode(
							field48Parsed.get(ISOParsingConstants._026), field48Parsed.get(ISOParsingConstants._002));

					essenceRq = prepareEssenceRequest(isoMessageFields, processingCode, messageTypeIdentifier,
							field48Parsed, imdCode);

					if (logger.isInfoEnabled()) {
						logger.info(EssenceATMReq);
						logger.info(essenceRq);
						logger.info(EssenceATMReq);
					}

					essenceRs = MFExecuter.executeMF(ISOParsingConstants.ATM_TXN_MF, essenceRq,
							BankFusionThreadLocal.getUserLocator().toString());
					if (null != isoMessageFields.get(ISOParsingConstants.conversionRateCardHolderBilling)) {
						isoMessageFields.remove(ISOParsingConstants.conversionRateCardHolderBilling);
					}
					if (null != isoMessageFields.get(ISOParsingConstants.conversionRateAccount)) {
						isoMessageFields.remove(ISOParsingConstants.conversionRateAccount);
					}
					if (null != isoMessageFields.get(ISOParsingConstants.merchantType)) {
						isoMessageFields.remove(ISOParsingConstants.merchantType);
					}
					if (ISOParsingConstants.EMPTY_STRING.equals(essenceRs.get(UPDATE_ERROR_STATUS))
							|| ISOParsingConstants.ZERO.equals(essenceRs.get(UPDATE_ERROR_STATUS))) {
						essenceRs.put(UPDATE_ERROR_STATUS, ISOParsingConstants.ZERO);

						if (logger.isInfoEnabled()) {
							logger.info(line + logger_TRANSACTION_REFERENCE
									+ (String) essenceRq.get(ISOParsingConstants.retrievalReferenceNo_37) + line);
						}

						helper.updateATMActivity(essenceRs, essenceRq, field48Parsed);
						isoMessageFields.put(ISOParsingConstants.responseCode, ISOParsingConstants._000);

						if (ISOParsingConstants._39.equals(processingCode)) {
							rawResponse = ATMTransactionResponseMapping
									.prepareMiniStatementSuccessResponse(isoMessageFields, essenceRs);
						} else {
							rawResponse = ATMTransactionResponseMapping.prepareSuccessResponse(isoMessageFields);
						}
					} else {

						helper.updateATMActivityError(essenceRq, (String) essenceRs.get(UPDATE_ERROR_STATUS),
								(String) essenceRs.get(UPDATE_ERROR_DESCRIPTION), field48Parsed);

						if (logger.isErrorEnabled()) {
							logger.error(line + logger_TRANSACTION_REFERENCE
									+ (String) essenceRq.get(ISOParsingConstants.retrievalReferenceNo_37)
									+ logger_ERROR_CODE + (String) essenceRs.get(UPDATE_ERROR_STATUS)
									+ logger_ERROR_DESCRIPTION + (String) essenceRs.get(UPDATE_ERROR_DESCRIPTION)
									+ line);
						}

						rawResponse = essenceRs;
						String responseCode = ATMSettlementPseudonymResolver.getProperty(
								path_ISO8583ErrorCode_properties, ISO8583ErrorCode_properties,
								(String) essenceRs.get(UPDATE_ERROR_STATUS));
						isoMessageFields.put(ISOParsingConstants.responseCode, responseCode);
						rawResponse = isoMessageFields;
					}
				} catch (Exception e) {
					logger.error("Following exception occurred: " + e.toString(), e);
					BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
					BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

					Error errorResponse = FBPErrorResponseHandler.createErrorResponse(e, ISOParsingConstants.ERROR_TYPE,
							ATMTransactionUtil.getErrorResponseTitle(), null);
					String errorCode = errorResponse.getCauses().get(0).getCode();
					String errorCodeDescription = errorResponse.getCauses().get(0).getMessage();

					logger.error(line + logger_TRANSACTION_REFERENCE
							+ (String) essenceRq.get(ISOParsingConstants.retrievalReferenceNo_37) + logger_ERROR_CODE
							+ errorCode + logger_ERROR_DESCRIPTION + errorCodeDescription + line);
					helper.updateATMActivityError(essenceRq, errorCode, errorCodeDescription, field48Parsed);
					String responseCode = ATMSettlementPseudonymResolver.getProperty(path_ISO8583ErrorCode_properties,
							ISO8583ErrorCode_properties, errorCode);
					isoMessageFields.put(ISOParsingConstants.responseCode, responseCode);
					rawResponse = isoMessageFields;
				} finally {
					try {
						BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
					} catch (Exception e) {
						BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();

						logger.error(null != e.getCause() ? ExceptionUtil.getExceptionAsString(e.getCause())
								: ExceptionUtil.getExceptionAsString(e));
						logger.error(line + commitFailure + line);
					}
				}
			}
		}
		return rawResponse;
	}

	public HashMap<String, Object> prepareEssenceRequest(HashMap<String, Object> isoMessage, String processingCode,
			String isoMessageTypeIdentifier, HashMap<String, String> field48Parsed, String imdCode) {

		ATMPOSTransactionDetails atmPosTransactionDetails = new ATMPOSTransactionDetails();

		HashMap<String, Object> essenceRq = new HashMap<>();
		isoMessage.put(ISOParsingConstants.key, ISOParsingConstants.EMPTY_STRING);

		String messageTypeIdentifier;
		if (ISOParsingConstants._1100.equals(isoMessageTypeIdentifier)) {
			if ("DMS".equals(ATMSettlementPseudonymResolver.getSmsDms())) {
				messageTypeIdentifier = ISOParsingConstants._1100;
			} else {
				messageTypeIdentifier = ISOParsingConstants._1200;
			}
		} else {
			messageTypeIdentifier = isoMessageTypeIdentifier;
		}
		StringBuilder msgFunction = new StringBuilder();
		if (null != isoMessage.get(ISOParsingConstants.actionCode)) {
			if ("205".equals((String) isoMessage.get(ISOParsingConstants.actionCode))) {
				msgFunction.append(ISOParsingConstants.Refund);
			} else {
				msgFunction = atmPosTransactionDetails.getATMMessageFunction(messageTypeIdentifier,
						(String) isoMessage.get(ISOParsingConstants.actionCode));
			}
		} else {
			msgFunction = atmPosTransactionDetails.getATMMessageFunction(messageTypeIdentifier,
					ISOParsingConstants.EMPTY_STRING);
		}
		String dateTime = ParsingEngine.getRawToFormattedData(
				isoMessage.get(ISOParsingConstants.localTransactionDateTime), ISOParsingConstants.YYMMDDHHMMSS, 12);

		Date date = Date.valueOf(dateTime.substring(0, 10));
		Time time = Time.valueOf(dateTime.substring(11, 19));

		if (null != isoMessage.get(ISOParsingConstants.accountIdentification_1)) {
			essenceRq.put(ISOParsingConstants.accountNumber1_102_2,
					isoMessage.get(ISOParsingConstants.accountIdentification_1));
		}
		if (null != isoMessage.get(ISOParsingConstants.accountIdentification_2)) {
			essenceRq.put(ISOParsingConstants.accountNumber2_103_2,
					isoMessage.get(ISOParsingConstants.accountIdentification_2));
		}
		if (null != isoMessage.get(ISOParsingConstants.primaryAccountNumber)) {
			essenceRq.put(ISOParsingConstants.cardNumber_35, isoMessage.get(ISOParsingConstants.primaryAccountNumber));
			essenceRq.put(ISOParsingConstants.cardNumber_35_2,
					isoMessage.get(ISOParsingConstants.primaryAccountNumber));
		}
		essenceRq.put(ISOParsingConstants.transactionType_3_2, processingCode);
		essenceRq.put(ISOParsingConstants.Processing_Code_3, processingCode);

		if ((null != isoMessage.get(ISOParsingConstants.amountTransaction))
				&& !(ISOParsingConstants._31.equals(processingCode))) {
			essenceRq.put(ISOParsingConstants.transactionAmount_4,
					atmPosTransactionDetails.parseAmount((String) isoMessage.get(ISOParsingConstants.amountTransaction),
							(String) isoMessage.get(ISOParsingConstants.currencyCodeTransaction)));
		}

		if ((null != isoMessage.get(ISOParsingConstants.amountAccount))) {
			essenceRq.put(ISOParsingConstants.amountRecon,
					atmPosTransactionDetails.parseAmount((String) isoMessage.get(ISOParsingConstants.amountAccount),
							(String) isoMessage.get(ISOParsingConstants.currencyCodeAccount)));
		}
		if ((null != isoMessage.get(ISOParsingConstants.amountCardHolderBilling))) {
			essenceRq.put(ISOParsingConstants.CardHolderBillingAmt,
					atmPosTransactionDetails.parseAmount(
							(String) isoMessage.get(ISOParsingConstants.amountCardHolderBilling),
							(String) isoMessage.get(ISOParsingConstants.transactionCurrencyCode)));
		}
		if ((null != isoMessage.get(ISOParsingConstants.additionalAmounts))) {
			String field54 = (String) isoMessage.get(ISOParsingConstants.additionalAmounts);
			HashMap<String, String> field54Parsed = ParsingEngine.parseField48and54(field54);
			essenceRq.put(ISOParsingConstants.CardHolderFeeAmt,
					atmPosTransactionDetails.parseAmount((String) field54Parsed.get(ISOParsingConstants._001),
							(String) isoMessage.get(ISOParsingConstants.currencyCodeTransaction)));
			essenceRq.put(ISOParsingConstants.transactionFeeAmount_28,
					atmPosTransactionDetails.parseAmount((String) field54Parsed.get(ISOParsingConstants._001),
							(String) isoMessage.get(ISOParsingConstants.currencyCodeTransaction)));
		}

		if (null != isoMessage.get(ISOParsingConstants.conversionRateCardHolderBilling)) {
			String conversionRate = (String) isoMessage.get(ISOParsingConstants.conversionRateCardHolderBilling);

			int scale = Integer.parseInt(conversionRate.substring(0, 1));
			String rate = conversionRate.substring(1, 8);
			essenceRq.put(ISOParsingConstants.acquirerFee_95_2, atmPosTransactionDetails.parseRate(rate, scale));
		}
		if (null != isoMessage.get(ISOParsingConstants.cardAcceptorNameandLocation)) {
			essenceRq.put(ISOParsingConstants.cardAcceptorNameLoc_43,
					isoMessage.get(ISOParsingConstants.cardAcceptorNameandLocation));
		}

		if (null != isoMessage.get(ISOParsingConstants.systemsTraceAuditNumber)) {
			essenceRq.put(ISOParsingConstants.systemsTraceAuditNumber_11,
					isoMessage.get(ISOParsingConstants.systemsTraceAuditNumber));
		}
		essenceRq.put(ISOParsingConstants.retrievalReferenceNo_37,
				isoMessage.get(ISOParsingConstants.retrievalReferenceNumber));
		essenceRq.put(ISOParsingConstants.uniqueEndTransactionReference_48_031,
				field48Parsed.get(ISOParsingConstants._031));
		if (null != isoMessage.get(ISOParsingConstants.CardAcceptorIdentifactionCode)) {
			essenceRq.put(ISOParsingConstants.cardAcceptorId_42,
					isoMessage.get(ISOParsingConstants.CardAcceptorIdentifactionCode));
		}
		essenceRq.put(ISOParsingConstants.cardIssuerAuthoriser_61,
				isoMessage.get(ISOParsingConstants.issuerInstitutionIdentifier));
		essenceRq.put(ISOParsingConstants.cardIssuerFIID_61_2,
				isoMessage.get(ISOParsingConstants.issuerInstitutionIdentifier));
		essenceRq.put(ISOParsingConstants.receivingInstitutionId_100,
				isoMessage.get(ISOParsingConstants.issuerInstitutionIdentifier));
		essenceRq.put(ISOParsingConstants.currencyCode_49, isoMessage.get(ISOParsingConstants.currencyCodeTransaction));
		essenceRq.put(ISOParsingConstants.CommNumCurrencyCode,
				isoMessage.get(ISOParsingConstants.currencyCodeTransaction));
		essenceRq.put(ISOParsingConstants.acquiringInstitutionId_32, imdCode);

		if (null != isoMessage.get(ISOParsingConstants.transactionCurrencyCode)) {
			essenceRq.put(ISOParsingConstants.CardHldBillingCurr,
					isoMessage.get(ISOParsingConstants.transactionCurrencyCode));
		}
		essenceRq.put(ISOParsingConstants.additionalData1_48, isoMessage.get(ISOParsingConstants.additionalData));
		essenceRq.put(ISOParsingConstants.cardAcceptorTerminalId_41,
				isoMessage.get(ISOParsingConstants.cardAcceptorTerminalIdentification));
		essenceRq.put(ISOParsingConstants.transmissionDateTime_7, dateTime);
		essenceRq.put(ISOParsingConstants.LocalTransactionSqlDate_13, date);
		essenceRq.put(ISOParsingConstants.localTransactionSqlTime_12, time);
		if (null != isoMessage.get(ISOParsingConstants.ForwardingInstituionID_33)) {
			essenceRq.put(ISOParsingConstants.ForwardingInstituionID_33,
					isoMessage.get(ISOParsingConstants.ForwardingInstituionID_33));
		}
		essenceRq.put(ISOParsingConstants.MsgFunction, msgFunction.toString());
		essenceRq.put(ISOParsingConstants.Product_Indicator, ATMSettlementPseudonymResolver.getChanneId());

		essenceRq.put(ISOParsingConstants.Message_Type,
				(atmPosTransactionDetails.getATMMessageType(processingCode, messageTypeIdentifier)).toString());
		essenceRq.put(ISOParsingConstants.environment_60_2, ISOParsingConstants.ZERO);

		if ((ISOParsingConstants.Reversal).equals(msgFunction.toString())
				|| (ISOParsingConstants.RepeatReversal).equals(msgFunction.toString())
				|| (ISOParsingConstants.Replacement).equals(msgFunction.toString())) {
			essenceRq.put(ISOParsingConstants.originalSequenceNumber_90_2, isoMessage.get(ISOParsingConstants.key));
			essenceRq.put(ISOParsingConstants.originalTransactionDate_90_3, isoMessage.get(ISOParsingConstants.key));
			essenceRq.put(ISOParsingConstants.originalTransactionTime_90_4, isoMessage.get(ISOParsingConstants.key));
			essenceRq.put(ISOParsingConstants.originalTransactionType_90_1, isoMessage.get(ISOParsingConstants.key));
			essenceRq.put(ISOParsingConstants.OriginalTxnAmt, isoMessage.get(ISOParsingConstants.key));
		}

		if (null != isoMessage.get(ISOParsingConstants.currencyCodeAccount)) {
			essenceRq.put(ISOParsingConstants.amountReconCurrency,
					isoMessage.get(ISOParsingConstants.currencyCodeAccount));
		}

		if (null != isoMessage.get(ISOParsingConstants.approvalCode)) {
			essenceRq.put(ISOParsingConstants.approvalCode, isoMessage.get(ISOParsingConstants.approvalCode));
		}
		return essenceRq;
	}
}