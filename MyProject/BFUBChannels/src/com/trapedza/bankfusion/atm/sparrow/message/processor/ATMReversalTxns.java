/*Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 **/

package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBlockingTransactions;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

public class ATMReversalTxns extends ATMFinancialProcessor {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";
	private static final String LORO_TRANCATION_TYPE = "99";
	private static final String QUERRY_FIND_BLOCKING_TRANS = "WHERE " + IBOTransaction.REFERENCE + " = ? AND "
			+ IBOTransaction.TYPE + " = ?";
	private static final String findBlockingTransQuerry = "WHERE " + IBOBlockingTransactions.ACCOUNTID + "=?" + " AND "
			+ IBOBlockingTransactions.BLOCKINGREFERENCE + "=?" + " AND " + IBOBlockingTransactions.UNBLOCKING + " = ?";
	private static final String FindBytransactionCode="WHERE " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + "=?";

	private transient final static Log logger = LogFactory.getLog(ATMReversalTxns.class.getName());

	String transCode = CommonConstants.EMPTY_STRING;
	String narration = CommonConstants.EMPTY_STRING;
	String transactionReference = CommonConstants.EMPTY_STRING;
	boolean isBlockingTransaction = false;
	ArrayList transactionList = null;
	ATMHelper atmHelper = new ATMHelper();
	boolean isTransactionReversed = false;
	boolean isLOROTransaction = false;
	String transactionType = ATMMessageValidator.LOCAL_MESSGE_TYPE;

	/*
	 * This Function will be Called by the ATMFinancialFatom.
	 * 
	 * @see com.trapedza.bankfusion.atm.sparrow.message.processor.ATMFinancialProcessor#execute(com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage, com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void execute(ATMSparrowMessage message, BankFusionEnvironment env) {
		ATMSparrowFinancialMessage financialMessage = (ATMSparrowFinancialMessage) message;
		ATMMessageValidator messageValidator = new ATMMessageValidator();
		validateForcePost(financialMessage);
		if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		isLOROTransaction = isLOROTransaction(financialMessage);
		transactionType = getTransactionType(financialMessage);

		messageValidator.validateMessage(financialMessage, env, transactionType);
		if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		if (!isLOROTransaction) {
			if (!messageValidator.doesCardExist(financialMessage.getCardNumber(), env)) {
				financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				financialMessage.setErrorCode(ATMConstants.CRITICAL);
				financialMessage.setErrorDescription("Invalid Card " + financialMessage.getCardNumber());
				logger.error("Invalid Card " + financialMessage.getCardNumber());
				return;
			}
			if (!messageValidator.areCardandAccountMapped(financialMessage.getCardNumber(), financialMessage
					.getAccount(), env)) {
				financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				financialMessage.setErrorCode(ATMConstants.CRITICAL);
				financialMessage.setErrorDescription("Card and Account Not Mapped");
				return;
			}
		}
		isBlockingTransaction = isBlockingTransaction(financialMessage, env);

		if (isBlockingTransaction) {
			boolean isTransactionReversed = checkForPreAuthorization(financialMessage, env);
			if (isTransactionReversed) {
				performUnBlocking(financialMessage, env);
				return;
			}
			else {
				financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				financialMessage.setErrorCode(ATMConstants.INFORMATION);
				financialMessage.setErrorDescription("Transaction Already Reversed");
				return;
			}
		}
		String txnID = getTransactionID(financialMessage, env);
		if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}
		reverseTransactions(financialMessage, txnID, env);
	}

	/*
	 * @retuns Transaction Reference.
	 */
	private String getTransactionReference(ATMSparrowFinancialMessage atmPosMessage) {

		ATMHelper atmHelper = new ATMHelper();
		return atmHelper.getTransactionReference(atmPosMessage);
	}

	private String getTransactionID(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
		String txnID = CommonConstants.EMPTY_STRING;
		ArrayList params = new ArrayList();
		params.add(getTransactionReference(message));
		try {
			List transactionList = env.getFactory().findByQuery(IBOTransaction.BONAME, txnHistoryWhereClause, params,
					null);
			if (transactionList.size() == 0) {
				message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				message.setErrorCode(ATMConstants.ERROR);
				message.setErrorDescription("Original Transaction Not Posted");
				return txnID;
			}

			Iterator iterator = transactionList.iterator();
			while (iterator.hasNext()) {
				IBOTransaction transaction = (IBOTransaction) iterator.next();
				if (transaction.getF_REVERSALINDICATOR() == 1 || transaction.getF_REVERSALINDICATOR() == 2) {
					isTransactionReversed = true;
					message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
					message.setErrorCode(ATMConstants.ERROR);
					message.setErrorDescription("Transaction Already Reversed");
					break;
				}
				else {
					txnID = transaction.getF_TRANSACTIONID();
					break;
				}
			}
		}
		catch (BankFusionException exception) {

		}
		return txnID;
	}

	private void reverseTransactions(ATMSparrowFinancialMessage message, String txnID, BankFusionEnvironment env) {
		try {
			HashMap inParams = new HashMap();
			inParams.put("TRANSACTIONID", txnID);
			inParams.put("AUTHORIZATIONREQUIRED", new Boolean(false));
			inParams.put("TRANSACTIONREFERENCE", getTransactionReference(message));
			inParams.put("CHANNELID", "ATM");
			inParams.put("FORCEPOST", new Boolean(true));
			inParams.put("AMOUNT4",message.getAmount4().abs());
			inParams.put("MAINACCOUNTID",message.getAccount());
			HashMap outParams = MFExecuter.executeMF("ATM_SPA_Reversals", env, inParams);
			boolean result = ((Boolean) outParams.get("RESULT")).booleanValue();
			if (!result) {
				message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				message.setErrorCode(ATMConstants.CRITICAL);
				message.setErrorDescription(outParams.get("MESSAGE").toString());
			}
		}
		catch (BankFusionException exception) {
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorCode(ATMConstants.CRITICAL);
			message.setErrorDescription(exception.getLocalisedMessage());
		}
	}

	private void validateForcePost(ATMSparrowFinancialMessage message) {
		if (!(message.getForcePost().equals(ATMConstants.FORCEPOST_1)|| message.getForcePost().equals(ATMConstants.FORCEPOST_2) || message.getForcePost().equals(
				ATMConstants.FORCEPOST_3))) {
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorCode(ATMConstants.CRITICAL);
			message.setErrorDescription("Invalid Force Post Value for Reversal");
			return;
		}
	}

	private boolean isLOROTransaction(ATMSparrowFinancialMessage message) {
		boolean result = false;
		if (message.getTransactionType().equals(LORO_TRANCATION_TYPE)) {
			result = true;
			return result;
		}
		else {
			result = false;
			return result;

		}
	}

	private String getTransactionType(ATMSparrowFinancialMessage message) {
		String result = ATMMessageValidator.LOCAL_MESSGE_TYPE;
		if (message.getVariableDataType().equalsIgnoreCase("A")) {
			result = ATMMessageValidator.LOCAL_MESSGE_TYPE;
		}
		else {
			result = ATMMessageValidator.EXTERNAL_MESSAGE_TYPE;
		}
		return result;
	}

	private void performUnBlocking(ATMSparrowFinancialMessage atmPosMessage, BankFusionEnvironment env) {
		BigDecimal amount = atmPosMessage.getAmount1().abs();
		String blockingCategory = "ATM";
		String transactionCode = getTransCode(atmPosMessage, env);
		String narrative = getTransactionNarration(env);
		Date postingDate = SystemInformationManager.getInstance().getBFBusinessDate();
		HashMap paramsforTellerBlocking = new HashMap();
		paramsforTellerBlocking.put("ACCOUNTID", atmPosMessage.getAccount());
		paramsforTellerBlocking.put("AMOUNT", amount.abs());
		paramsforTellerBlocking.put("BLOCKINGCATEGORY", blockingCategory);
		paramsforTellerBlocking.put("NARRATIVE", narrative);
		paramsforTellerBlocking.put("POSTINGDATE", postingDate);
		paramsforTellerBlocking.put("TRANSACTIONCODE", transactionCode);
		paramsforTellerBlocking.put("TRANSACTIONREFERENCE", getTransactionReference(atmPosMessage));
		paramsforTellerBlocking.put("ISBLOCKING", new Boolean(false));
		paramsforTellerBlocking.put("POSTINGACTION", "C");

		try {
			MFExecuter.executeMF(ATMConstants.BLOCKING_TRANSACTION, env, paramsforTellerBlocking);
		}
		catch (BankFusionException exception) {
			String message = exception.getMessage();
			atmPosMessage.setErrorCode(ATMConstants.CRITICAL);
			atmPosMessage.setErrorDescription(message);
		}
	}

	private String getTransCode(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
		String transactionCode = CommonConstants.EMPTY_STRING;
		try {
			ArrayList params = new ArrayList();
			params.add(transactionCode);
            IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);
			//IBOATMTransactionCodes codes = (IBOATMTransactionCodes) env.getFactory().findByPrimaryKey(
					//IBOATMTransactionCodes.BONAME, transactionCode);
			transactionCode = atmTransactionCodes.getF_MISTRANSACTIONCODE();
		}
		catch (BankFusionException exception) {
			try {
				transactionCode = ATMConfigCache.getInstance().getInformation(env).getPosTxnType();
			}
			catch (BankFusionException innerException) {
			}
		}
		return transactionCode;
	}

	private String getTransactionNarration(BankFusionEnvironment env) {
		String narration = CommonConstants.EMPTY_STRING;
		try {
			narration = ATMConfigCache.getInstance().getInformation(env).getSuspectRevTxnNarr();
		}
		catch (BankFusionException exception) {

		}
		return narration;
	}

	private boolean checkForPreAuthorization(ATMSparrowFinancialMessage atmfinancialMessage, BankFusionEnvironment env) {
		boolean result = false;
		BigDecimal blockedAmount = CommonConstants.BIGDECIMAL_ZERO;
		ArrayList params = new ArrayList();
		params.add(atmfinancialMessage.getAccount());
		params.add(getTransactionReference(atmfinancialMessage));
		params.add(new Boolean(false));
		try {
			IBOBlockingTransactions transactioDetails = (IBOBlockingTransactions) env.getFactory().findFirstByQuery(
					IBOBlockingTransactions.BONAME, findBlockingTransQuerry, params);
			blockedAmount = transactioDetails.getF_AMOUNT().subtract(transactioDetails.getF_UNBLOCKEDAMOUNT());
			if (blockedAmount.compareTo(new BigDecimal(0)) > 0) {
				result = true;
			}
			else {
				result = false;
			}
		}
		catch (BankFusionException exception) {
			result = false;
		}
		return result;
	}

	private boolean isBlockingTransaction(ATMSparrowFinancialMessage atmfinancialMessage, BankFusionEnvironment env) {
		boolean result = true;
		ArrayList params = new ArrayList();
		params.add(getTransactionReference(atmfinancialMessage));
		params.add("Z");
		try {
			List list = env.getFactory().findByQuery(IBOTransaction.BONAME, QUERRY_FIND_BLOCKING_TRANS, params, null);
			if (list.size() > 0) {
				result = true;
			}
			else {
				result = false;

			}
		}
		catch (BankFusionException exception) {
			result = true;
		}
		return result;
	}
}
