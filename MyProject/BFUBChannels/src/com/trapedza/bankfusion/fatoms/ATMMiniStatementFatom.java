/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * $Log: ATMMiniStatementFatom.java,v $
 * Revision 1.8  2008/08/12 20:14:25  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.6.4.2  2008/07/16 16:13:01  varap
 * Code cleanup - CVS revision tag added.
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.services.CalcEventChargeRq;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.EventChgInputDtls;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.cbs.common.functions.GetBankFusionMessage;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.ConvertToCurrency;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMMiniStatementFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;

public class ATMMiniStatementFatom extends AbstractATMMiniStatementFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */
	private transient final static Log logger = LogFactory
			.getLog(ATMMiniStatementFatom.class.getName());

	private static final String getTransactionDetailssql = "where "
			+ IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = ? and "
			+ IBOTransaction.STATEMENTFLAG + " = ? " + "ORDER BY "
			+ IBOTransaction.VALUEDATE + "  DESC";

	private static final int MAX_TRANSACTIONS_REQUIRED = 10;
	private static final int SIZE_OF_AMOUNT = 14;
	private BankFusionEnvironment environment = null;
	/*
	 * List of all Input Tags.
	 */

	private String sourceIMD = CommonConstants.EMPTY_STRING;
	private String sourceBranch = CommonConstants.EMPTY_STRING;
	private String sourceCountry = CommonConstants.EMPTY_STRING;
	private String deviceID = CommonConstants.EMPTY_STRING;
	private int transSequenceNumber = 0;
	private int forcePost = 0;
	private Timestamp dateTimeOfTxn = null;
	private String destCountryCode = CommonConstants.EMPTY_STRING;
	private String destIMDCode = CommonConstants.EMPTY_STRING;
	private String destBranchCode = CommonConstants.EMPTY_STRING;
	private String cardNumber = CommonConstants.EMPTY_STRING;
	private int cardSequenceNumber = 0;
	private String authorizedFlag = CommonConstants.EMPTY_STRING;
	private String accountID = CommonConstants.EMPTY_STRING;
	private String ATMTransactionCode = CommonConstants.EMPTY_STRING;
	private String transactionReference = CommonConstants.EMPTY_STRING;
	private String Id = null;
	private String errorStatus = CommonConstants.EMPTY_STRING;
	private String errorMessage = CommonConstants.EMPTY_STRING;
	BankFusionEnvironment env1;
	private VectorTable transactionDetails;
	private static final String FindBytransactionCode = "WHERE "
			+ IBOATMTransactionCodes.ATMTRANSACTIONCODE + "=?";

	int transCounter = 0;
	
	public ATMMiniStatementFatom(BankFusionEnvironment env) {

		super(env);
		transactionDetails = new VectorTable();
	}

	public void process(BankFusionEnvironment env) {

		environment = env;
		getInputTagsDetails();

		String authorizedFlag = ValidateMessage(env);
		if (authorizedFlag.equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
			transactionDetails = getTransDetails();
		}
		// UpdateATMActivityLog();
		UpdateATMActivityLog();
		generateResponseTags();
	}

	public VectorTable getTransDetails() {
		ATMHelper atmHelper = new ATMHelper();
		VectorTable vectorTable = new VectorTable();
		ArrayList params = new ArrayList();
		params.add(accountID);
		params.add(new Integer(0));
		Iterator transIterator = environment.getFactory().findByQuery(
				IBOTransaction.BONAME, getTransactionDetailssql, params,
				MAX_TRANSACTIONS_REQUIRED);
		// Iterator transIterator = transactionList.iterator();
		transCounter = 0;
		while (transIterator.hasNext()
				&& transCounter < MAX_TRANSACTIONS_REQUIRED) {
			IBOTransaction transaction = (IBOTransaction) transIterator.next();
			HashMap attributes = new HashMap();
			Timestamp transDate = transaction.getF_TRANSACTIONDATE();
			String formattedDate = getFormattedDate(transDate);

			BigDecimal amount = ConvertToCurrency.run(transaction.getF_AMOUNT()
					.abs(), transaction.getF_ISOCURRENCYCODE());
			String transAmount = amount.toString();
			transAmount = atmHelper.leftPad(transAmount, " ", SIZE_OF_AMOUNT);
			String drcrIndicator = transaction.getF_DEBITCREDITFLAG();
			attributes.put("TRANSDATE", formattedDate);
			attributes.put("NARRATION", transaction.getF_NARRATION());
			attributes.put("TRANSAMOUNT", transAmount);
			attributes.put("DRCRINDICATOR", drcrIndicator);
			attributes.put("FILLER", " ");
			attributes.put("FILLER1", " ");
			vectorTable.addAll(new VectorTable(attributes));
			transCounter++;
		}
		return vectorTable;
	}

	private void getInputTagsDetails() {
		ATMHelper atmHelper = new ATMHelper();
		sourceIMD = getF_IN_SOURCEIMDCODE();
		sourceBranch = getF_IN_SOURCEBRANCHCODE();
		sourceCountry = getF_IN_SOURCECOUNTRYCODE();
		deviceID = getF_IN_DEVICEID();
		transSequenceNumber = Integer.parseInt(getF_IN_TXNSEQUENCENUMBER());
		forcePost = Integer.parseInt(getF_IN_FORCEPOST());
		dateTimeOfTxn = getF_IN_POSTDATETIME();
		destCountryCode = getF_IN_DESTCOUNTRYCODE();
		destIMDCode = getF_IN_DESTIMDCODE();
		destBranchCode = getF_IN_DESTBRANCHCODE();
		cardNumber = getF_IN_CARDNUMBER();
		cardSequenceNumber = Integer.parseInt(getF_IN_CARDSEQUENCENUMBER());
		authorizedFlag = getF_IN_AUTHORIZEDFLAG();
		accountID = getF_IN_ACCOUNTID();
		ATMTransactionCode = getF_IN_MESSAGETYPE() + getF_IN_TRANSACTIONTYPE();
		transactionReference = sourceCountry + sourceIMD + sourceBranch
				+ deviceID + transSequenceNumber
				+ atmHelper.getDateinStringFormat(dateTimeOfTxn);
	}

	private String ValidateMessage(BankFusionEnvironment env) {

		if (logger.isDebugEnabled()) {
			logger.debug("Message Validator Called for Mini Statement Request");
			logger.debug("Source Country: " + sourceCountry);
			logger.debug("Source IMD: " + sourceIMD);
			logger.debug("Source Branch: " + sourceBranch);
			logger.debug("CardNumber: " + cardNumber);
			logger.debug("Account ID: " + accountID);
		}
		ATMMessageValidator messageValidator = new ATMMessageValidator();
		if (!messageValidator.validateCIB(sourceBranch, sourceCountry,
				sourceIMD, environment)) {
			authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
			/*
			 * errorMessage = BankFusionMessages.getFormattedMessage( 7500,
			 * environment, new Object[] { sourceCountry, sourceIMD,
			 * sourceBranch });
			 */
			errorMessage = BankFusionMessages.getFormattedMessage(
					ChannelsEventCodes.E_SRCE_COUNTRY_IMD_BRANCH_NOT_MAPPED,
					new Object[] { sourceCountry, sourceIMD, sourceBranch });
			errorStatus = ATMConstants.WARNING;
			logger.error(errorStatus + ": " + errorMessage);
		} else if (!messageValidator.isTransactionSupported(ATMTransactionCode,
				environment)) {
			authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
			/*
			 * errorMessage =
			 * BankFusionMessages.getFormattedMessage(BankFusionMessages
			 * .ERROR_LEVEL, 7504, environment, new Object[] {
			 * ATMTransactionCode });
			 */
			errorMessage = BankFusionMessages.getFormattedMessage(
					ChannelsEventCodes.E_ATM_TRANSACTION_NOT_SUPPORTED,
					new Object[] { ATMTransactionCode });
			errorStatus = ATMConstants.WARNING;
			logger.error(errorStatus + ": " + errorMessage);
		} else if (!messageValidator.isCardNumberValid(cardNumber, environment)) {
			authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
			/*
			 * errorMessage =
			 * BankFusionMessages.getFormattedMessage(BankFusionMessages
			 * .ERROR_LEVEL, 7510, environment, new Object[] { cardNumber });
			 */
			errorMessage = BankFusionMessages.getFormattedMessage(
					ChannelsEventCodes.E_INVALID_CARD,
					new Object[] { cardNumber });
			errorStatus = ATMConstants.WARNING;
			logger.error(errorStatus + ": " + errorMessage);
		} else if (!messageValidator.isAccountValid(accountID, environment)) {
		    //Below changes is for account in stop,closed and other invalid status.
			//Changes done for artf753453.
			IBOAttributeCollectionFeature accValues = (IBOAttributeCollectionFeature) env
			.getFactory()
			.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
					accountID);

			if (accValues.isF_STOPPED()) {
				authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
				errorMessage= BankFusionMessages.getInstance().getFormattedEventMessage(CommonsEventCodes.E_ACCOUNT_STOPPED, new Object[]{accountID}, BankFusionThreadLocal.getUserSession().getUserLocale());
				errorStatus = ATMConstants.ERROR;
				logger.error(errorStatus + ": " + errorMessage);
				return authorizedFlag;
				
			}
			if (accValues.isF_CLOSED()) {
				authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
				errorMessage= BankFusionMessages.getInstance().getFormattedEventMessage(CommonsEventCodes.E_ACCOUNT_CLOSED, new Object[]{accountID}, BankFusionThreadLocal.getUserSession().getUserLocale());
				errorStatus = ATMConstants.ERROR;
				logger.error(errorStatus + ": " + errorMessage);
				return authorizedFlag;
			}
	           
			else {
				authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
				errorMessage= BankFusionMessages.getInstance().getFormattedEventMessage(ChannelsEventCodes.E_INVALID_ACCOUNT, new Object[]{accountID}, BankFusionThreadLocal.getUserSession().getUserLocale());
				errorStatus = ATMConstants.ERROR;
			    logger.error(errorStatus + ": " + errorMessage);
			    return authorizedFlag;
		}
	}

		else if (!messageValidator.areCardandAccountMapped(cardNumber,
				accountID, environment)) {
			authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
			/*
			 * errorMessage =
			 * BankFusionMessages.getFormattedMessage(BankFusionMessages
			 * .ERROR_LEVEL, 7537, environment, new Object[] { cardNumber,
			 * accountID, });
			 */
			errorMessage = BankFusionMessages
					.getFormattedMessage(
							ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
							new Object[] { cardNumber, accountID, });
			errorStatus = ATMConstants.WARNING;
			logger.error(errorStatus + ": " + errorMessage);
		} else {
			authorizedFlag = ATMConstants.AUTHORIZED_MESSAGE_FLAG;
			errorMessage = "Mini Statement Transaction Authorized";
			errorStatus = ATMConstants.INFORMATION;
			logger.info(errorStatus + ": " + errorMessage);
		}

		return authorizedFlag;
	}

	private void UpdateATMActivityLog() {
		String transDesc = CommonConstants.EMPTY_STRING;
		String transNarartion = CommonConstants.EMPTY_STRING;
		String transactionCode = CommonConstants.EMPTY_STRING;
		try {
			ArrayList params = new ArrayList();
			params.add(ATMTransactionCode);
			IBOATMTransactionCodes transDetails = (IBOATMTransactionCodes) environment
					.getFactory().findFirstByQuery(
							IBOATMTransactionCodes.BONAME,
							FindBytransactionCode, params, false);
			transDesc = transDetails.getF_DESCRIPTION();
			transNarartion = transDetails.getF_NARRATIVE();
			transactionCode = transDetails.getF_MISTRANSACTIONCODE();

		} catch (Exception exception) {
			logger.error(ExceptionUtil.getExceptionAsString(exception));

		}

		HashMap params = new HashMap();
		params.put("ACCOUNTCURRENCY", CommonConstants.EMPTY_STRING);
		params.put("ACCOUNTID", accountID);
		params.put("AMOUNTDISPENSED", new BigDecimal(0));
		params.put("ATMCARDNUMBER", cardNumber);
		params.put("ATMDEVICEID", deviceID);
		params.put("ATMTRANSACTIONCODE", ATMTransactionCode);
		params.put("ATMTRANDESC", transDesc);
		params.put("BASEEQUIVALENT", new BigDecimal("0"));
		params.put("CARDSEQUENCENUMBER", new Integer(cardSequenceNumber));
		params.put("COMMAMOUNT", new BigDecimal(0));
		params.put("DESTACCOUNTID", CommonConstants.EMPTY_STRING);
		params.put("DESTCOUNTRY", destCountryCode);
		params.put("DESTIMD", destIMDCode);
		params.put("DESTBRANCH", destBranchCode);
		params.put("DESTCIB", destCountryCode + destIMDCode + destBranchCode);
		params.put("ERRORDESCRIPTION", errorMessage);
		params.put("ERRORSTATUS", errorStatus);
		params.put("FORCEPOST", new Integer(forcePost));
		params.put("MISTRANSACTIONCODE", transactionCode);
		params.put("POSTDATETIME", dateTimeOfTxn);
		params.put("SOURCEBRANCH", sourceBranch);
		params.put("SOURCECOUNTRY", sourceCountry);
		params.put("SOURCEIMD", sourceIMD);
		params.put("SOURCECIB", sourceCountry + sourceIMD + sourceBranch);
		params.put("TRANSNARRATION", transNarartion);
		params.put("TRANSSEQ", new Integer(transSequenceNumber));
		params.put("AUTHORIZEDFLAG", authorizedFlag);
		params.put("TRANSACTIONREFERENCE", transactionReference);

		try {
			Map<String, Object> response = new HashMap<String, Object>();
			response = MFExecuter.executeMF(
					ATMConstants.ACTIVITY_LOG_UPDATE_MICROFLOW_NAME,
					environment, params);
			Id = response.get("ID").toString();
			
			//Start - Update for Event Based Charge
			if(ATMConstants.INFORMATION.equals(errorStatus))
			{
				CalcEventChargeRq calcEventChargeRq = new CalcEventChargeRq();
				IBOAttributeCollectionFeature accountBO = FinderMethods.getAccountBO(accountID);
				params = new HashMap<String, String>();
				EventChgInputDtls eventChgInputDtls = new EventChgInputDtls();
				AccountKeys accountKeys =null;
				accountKeys = new AccountKeys();
				accountKeys.setStandardAccountId(accountID);
				eventChgInputDtls.setAccountId(accountKeys);
				
				if(null!=accountBO)
				{
					accountKeys = new AccountKeys();
					eventChgInputDtls.setProductId(accountBO.getF_PRODUCTCONTEXTCODE());
					eventChgInputDtls.setProductCategory(accountBO.getF_PRODUCTID());
					if(null!=accountBO.getF_CHARGEFUNDINGACCOUNTID() || !accountBO.getF_CHARGEFUNDINGACCOUNTID().equals(""))
					{
						accountKeys.setStandardAccountId(accountBO.getF_CHARGEFUNDINGACCOUNTID());
					    eventChgInputDtls.setChgFundingAccount(accountKeys);
					}
					else
					{
						accountKeys.setStandardAccountId(accountID);
					    eventChgInputDtls.setChgFundingAccount(accountKeys);
					}
				}
				if(BankFusionThreadLocal.getSourceId()==null || BankFusionThreadLocal.getSourceId() == CommonConstants.EMPTY_STRING){
		        	BankFusionThreadLocal.setSourceId(BankFusionThreadLocal.getChannel());
		        }
				eventChgInputDtls.setChannelId(BankFusionThreadLocal.getSourceId());
				eventChgInputDtls.setEventSubCategory(ATMConstants.MINI_STATE_EVENT_ID_VAL);
				calcEventChargeRq.setEventChgInputDtls(eventChgInputDtls);
				params.put(ATMConstants.CALC_EVENT_CHARGE_REQ, calcEventChargeRq);
				
				MFExecuter.executeMF(ATMConstants.APPLY_EVENT_CHARGES_MICROFLOW_NAME, environment, params);
			}
			//End - Update for Event Based Charge
		} catch (Exception exception) {
			/*
			 * String localErrormessage =
			 * BankFusionMessages.getFormattedMessage(
			 * BankFusionMessages.ERROR_LEVEL, 7537, environment, new Object[] {
			 * CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING,
			 * CommonConstants.EMPTY_STRING });
			 */
			String localErrormessage = BankFusionMessages
					.getFormattedMessage(
							ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
							new Object[] { CommonConstants.EMPTY_STRING,
									CommonConstants.EMPTY_STRING,
									CommonConstants.EMPTY_STRING });
			logger.error(localErrormessage);
			logger.error(ExceptionUtil.getExceptionAsString(exception));
	
			
			
		}

	}

	private void generateResponseTags() {
		setF_OUT_AUTHORIZEDFLAG(authorizedFlag);
		setF_OUT_TRANSACTIONDETAILS(transactionDetails);
		setF_OUT_NOOFTRANS(new Integer(transCounter));
		setF_OUT_ID(Id);
		if (authorizedFlag.equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG)) {
			setF_OUT_FILESTATUS("F");
		} else {
			setF_OUT_FILESTATUS("0");

		}
	}

	private String getFormattedDate(Timestamp date) {
		String format = "dd/MM/yy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		String formatedDate = simpleDateFormat.format(date);
		return formatedDate;
	}
}
