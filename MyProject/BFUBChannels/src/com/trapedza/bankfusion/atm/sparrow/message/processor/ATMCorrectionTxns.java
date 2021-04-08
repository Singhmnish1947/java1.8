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

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.utils.FatomUtils;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * The ATMCorrectionTxns class processes the messages for ATM corrections(Messages - 820, 880, 885 and 899).
 * This class calls the ATMMessageValidator methods for the commomn message validations, performs validations 
 * that are specific to the Local Cash Withdrawal, Fast Cash Message, Local 2nd Currency Withdrawal and loro transactions
 * (Messages - 520, 580, 585  and 599 respectively).
 * The transactions are then posted using the postingEngine. 
 */
public class ATMCorrectionTxns extends ATMFinancialProcessor {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	/**
	 */

	private transient final static Log logger = LogFactory.getLog(ATMCorrectionTxns.class.getName());

	/**
	 * Where clause for transaction history record retrieval using transaction reference
	 */

	private static final String refHistoryWhereClause = "WHERE " + IBOTransaction.REFERENCE + "=?";
	
	private static final String txnHistoryWhereClause = "WHERE " + IBOTransaction.TRANSACTIONID + "=?";
	
	

	/**
	 * Where clause for transaction record retrieval using transaction Id 
	 */

	public static final String trnsnWhrClause = " WHERE " + IBOTransaction.TRANSACTIONID + " = ?";

	private static final String LORO_TRANCATION_TYPE = "99";
	private static final int TRANCATION_REVERSAL_INDICATOR = 3;

	String transCode = CommonConstants.EMPTY_STRING;
	String customerTransactionNarration = CommonConstants.EMPTY_STRING;
	String contraTransactionNarration = CommonConstants.EMPTY_STRING;
	String transactionReference = CommonConstants.EMPTY_STRING;
	String transactionType = ATMMessageValidator.LOCAL_MESSGE_TYPE;

	boolean isBlockingTransaction = false;
	boolean isTransactionCorrected = false;
	boolean isLOROTransaction = false;

	/**  	
	 * This holds the list of transactions to be posted. 
	 */
	private ArrayList postingMessages = new ArrayList();
	private ArrayList transactionIds = new ArrayList();

	/**
	 * Holds the configuration details
	 */
	private ATMControlDetails controlDetails = null;
	/**
	 * Instance of messageValidator
	 */
	ATMMessageValidator messageValidator = new ATMMessageValidator();

	/**
	 * Instance of ATMHelper
	 */

	ATMHelper atmHelper = new ATMHelper();

	/**
	 * Instance for financial posting message
	 */
	private IBOFinancialPostingMessage postingMessage = null;
	/**
	 * Instance for attribute collection feature.
	 */
	private IBOAttributeCollectionFeature accountValues = null;

	/**
	 * This method validates the message received for cashwithdrawal, creates messages for posting
	 * and calls postTransactions() for posting.
	 */
	public void execute(ATMSparrowMessage message, BankFusionEnvironment env) {
		//get ATM configuration details
		controlDetails = ATMConfigCache.getInstance().getInformation(env);

		ATMSparrowFinancialMessage financialMessage = (ATMSparrowFinancialMessage) message;

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
		transCode = atmHelper.getBankTransactionCode(financialMessage.getMessageType()
				+ financialMessage.getTransactionType(), env);
		atmHelper.updateTransactionNarration(financialMessage, env);
		customerTransactionNarration = financialMessage.getTxnCustomerNarrative();
		contraTransactionNarration = financialMessage.getTxnContraNarrative();

		if (transCode.equals(CommonConstants.EMPTY_STRING)) {
			if (financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
					|| financialMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
				String errorMessage = "Transaction Not Mapped";
				financialMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				financialMessage.setErrorCode(ATMConstants.WARNING);
				financialMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				return;
			}
			else {
				String errorMessage = "Transaction Not Mapped. Using Default Transaction Type";
				financialMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
				financialMessage.setErrorCode(ATMConstants.ERROR);
				financialMessage.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				transCode = controlDetails.getAtmTransactionType();
			}
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
		List originalTransactions = checkOriginalTransaction(financialMessage, env);
		if (financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			return;
		}

		if (!financialMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
			if (message.getForcePost().equals(ATMConstants.FORCEPOST_3)
					|| message.getForcePost().equals(ATMConstants.FORCEPOST_2)) {
				if (!isTransactionCorrected) {
					//postTransactions(originalTransactions, financialMessage, env);
					//					create posting messages and call post transaction in ATMFinancialProcessor
					if(originalTransactions!=null)
					createPostingMessages(originalTransactions, (ATMLocalMessage) financialMessage, env);
					postTransactions((ATMLocalMessage) financialMessage, postingMessages, env);
					updateTransactionTable(transactionIds, env);
				}
			}
			else {
				//postTransactions(originalTransactions, financialMessage, env);
				if(originalTransactions!=null)
				createPostingMessages(originalTransactions, (ATMLocalMessage) financialMessage, env);
				postTransactions((ATMLocalMessage) financialMessage, postingMessages, env);
				updateTransactionTable(transactionIds, env);
			}
		}
	}

	/**
	 * This method returns the transaction reference
	 * @retuns Transaction Reference.
	 */
	private String getTransactionReference(ATMSparrowFinancialMessage atmFinancialMessage) {

		ATMHelper atmHelper = new ATMHelper();
		return atmHelper.getTransactionReference(atmFinancialMessage);
	}

	/**
	 * This method gets the list of original transactions using the transactionID.
	 * @retuns List.
	 */
	private List checkOriginalTransaction(ATMSparrowFinancialMessage message, BankFusionEnvironment env) {
		String reference = getTransactionReference(message);
		ArrayList params = new ArrayList();
		ArrayList param1 = new ArrayList();
		params.add(reference);
		List transactionList = null;
		try {
			transactionList = env.getFactory().findByQuery(IBOTransaction.BONAME, refHistoryWhereClause, params, null);
			//fix for bug 14739 starts
			if (transactionList.size() != 0) {
			IBOTransaction transactionValues = (IBOTransaction) transactionList.get(0);
			String txnId=transactionValues.getF_TRANSACTIONID();
			param1.add(txnId);
			transactionList = env.getFactory().findByQuery(IBOTransaction.BONAME, txnHistoryWhereClause, param1, null);
			}
			else if (transactionList.size() == 0) {
				message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				message.setErrorCode(ATMConstants.ERROR);
				message.setErrorDescription("Original Transaction Not Posted");
			}
			//fix for bug 14739 ends
			else if (transactionList.size() > 0
					&& (message.getForcePost().equals(ATMConstants.FORCEPOST_3) || message.getForcePost().equals(
							ATMConstants.FORCEPOST_2))) {
				Iterator iterator = transactionList.iterator();
				while (iterator.hasNext()) {
					IBOTransaction transactioDetails = (IBOTransaction) iterator.next();
					if (transactioDetails.getF_REVERSALINDICATOR() == TRANCATION_REVERSAL_INDICATOR) {
						isTransactionCorrected = true;
						message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
						message.setErrorCode(ATMConstants.ERROR);
						message.setErrorDescription("Transaction Already Corrected");
						break;

					}
				}

			}
		}
		catch (BankFusionException exception) {
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorCode(ATMConstants.ERROR);
			message.setErrorDescription("Original Transaction Not Posted");
		}
		return transactionList;
	}

	/**
	 * This method posts the transactions using ATM financial posting.
	 */
	private void postCorrectionTransactions(List transactionList, ATMSparrowFinancialMessage message,
			BankFusionEnvironment env) {

		HashMap map = new HashMap();
		String accountCurrencyCode = CommonConstants.EMPTY_STRING;
		String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
		try {
			accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
					message.getCurrencySourceAccount(), true);
			dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
					message.getCurrencyDestDispensed(), true);
		}
		catch (BankFusionException exception) {

		}
		Iterator iterator = transactionList.iterator();
		while (iterator.hasNext()) {
			IBOTransaction transaction = (IBOTransaction) iterator.next();
			String drCrIndicator = transaction.getF_DEBITCREDITFLAG();
			if (("D").equals(drCrIndicator)) {
				map.put("ACCOUNT1_ACCOUNTID", transaction.getF_ACCOUNTPRODUCT_ACCPRODID());
				map.put("ACCOUNT1_AMOUNT", message.getAmount1().abs());
				map.put("ACCOUNT1_AMOUNT_CurrCode", accountCurrencyCode);
				map.put("ACCOUNT1_NARRATIVE", customerTransactionNarration);
				map.put("ACCOUNT1_TRANSCODE", transCode);
				map.put("ACCOUNT1_POSTINGACTION", "C");
				map.put("AMOUNT4",message.getAmount4().abs());
				map.put("MAINACCOUNTID",message.getAccount());
			}
			else {
				map.put("ACCOUNT2_ACCOUNTID", transaction.getF_ACCOUNTPRODUCT_ACCPRODID());
				map.put("ACCOUNT2_AMOUNT", message.getAmount2().abs());
				map.put("ACCOUNT2_AMOUNT_CurrCode", dispensedCurrencyCode);
				map.put("ACCOUNT2_NARRATIVE", contraTransactionNarration);
				map.put("ACCOUNT2_POSTINGACTION", "D");
				map.put("ACCOUNT2_TRANSCODE", transCode);
			}
		}
		map.put("BASEEQUIVALENT", message.getAmount3().abs());
		map.put("TRANSACTIONREFERENCE", atmHelper.getTransactionReference(message));
		map.put("MANUALVALUEDATE", new Date(message.getDateTimeofTxn().getTime()));
		map.put("MANUALVALUETIME", new Time(message.getDateTimeofTxn().getTime()));
		map.put("FORCEPOST", new Boolean(true));

		//Post the Transactions.
		try {
			HashMap outputParams = MFExecuter.executeMF(ATMConstants.FINANCIAL_POSTING_MICROFLOW_NAME, env, map);
			String authorizedFlag = outputParams.get("AUTHORIZEDFLAG").toString();
			if (authorizedFlag.equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG))	{
				String errorMessage = outputParams.get("ERRORMESSAGE").toString();
				message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
				message.setErrorCode(ATMConstants.ERROR);
				message.setErrorDescription(errorMessage);
				logger.error(errorMessage);
				try {
					env.getFactory().rollbackTransaction();
					env.getFactory().beginTransaction();            //
				}
				catch (Exception ignored) {
				}
				return;
			}
			env.getFactory().commitTransaction();
			env.getFactory().beginTransaction();            //
		}
		catch (BankFusionException exception) {
			logger.info(exception.getLocalisedMessage());
			
			message.setErrorCode(ATMConstants.ERROR);
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorDescription(exception.getMessage());
			env.getFactory().rollbackTransaction();
			env.getFactory().beginTransaction();            //
			try {
				env.getFactory().rollbackTransaction();
				
				env.getFactory().beginTransaction();            //
			}
			catch (Exception ignored) {
                env.getFactory().rollbackTransaction();
				
				env.getFactory().beginTransaction();            //

			}
		}
		finally {
			try {
				env.getFactory().beginTransaction();
			}
			catch (Exception ignored) {

			}
		}
	}

	/**
	 * This method posts the correction transactions using Posting Engine. The messages for the posting engine are created using the 
	 * values from the finacial message and the original transactions.
	 * 
	 */

	private void createPostingMessages(List transactionList, ATMLocalMessage atmLocalMessage, BankFusionEnvironment env) {

		String accountCurrencyCode = CommonConstants.EMPTY_STRING;
		String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
		try {
			accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
					atmLocalMessage.getCurrencySourceAccount(), true);
			dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
					atmLocalMessage.getCurrencyDestDispensed(), true);
		}
		catch (BankFusionException exception) {

		}
		Iterator iterator = transactionList.iterator();
		int count = 0;
		String txnID = GUIDGen.getNewGUID();
		transactionIds.add(txnID);
		while (iterator.hasNext()) {

			IBOTransaction transaction = (IBOTransaction) iterator.next();
			String drCrIndicator = transaction.getF_DEBITCREDITFLAG();

			try {
				accountValues = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
						IBOAttributeCollectionFeature.BONAME, transaction.getF_ACCOUNTPRODUCT_ACCPRODID());
			}
			catch (BankFusionException e) {
				throw e;
			}

			postingMessage = (IBOFinancialPostingMessage) env.getFactory().getStatelessNewInstance(
					IBOFinancialPostingMessage.BONAME);
			FatomUtils.createStandardItemsMessage(postingMessage, env);
			PostingHelper.setDefaultValuesForFinPosting(postingMessage, env);
			if (("D").equals(drCrIndicator)) {
				postingMessage.setSerialNo(++count);
				postingMessage.setTransactionID(txnID);
				postingMessage.setPrimaryID(transaction.getF_ACCOUNTPRODUCT_ACCPRODID());
				postingMessage.setF_AMOUNT(atmLocalMessage.getAmount1().abs());
				postingMessage.setAcctCurrencyCode(accountCurrencyCode);
				postingMessage.setTransCode(transCode);
				postingMessage.setNarrative(customerTransactionNarration);
				postingMessage.setSign('+');
			}
			else {
				postingMessage.setSerialNo(++count);
				postingMessage.setTransactionID(txnID);
				postingMessage.setPrimaryID(transaction.getF_ACCOUNTPRODUCT_ACCPRODID());
				postingMessage.setF_AMOUNT(atmLocalMessage.getAmount2().abs());
				postingMessage.setAcctCurrencyCode(dispensedCurrencyCode);
				postingMessage.setTransCode(transCode);
				postingMessage.setNarrative(contraTransactionNarration);
				postingMessage.setSign('-');
			}
			postingMessage.setF_BASEEQUIVALENT(atmLocalMessage.getAmount3().abs());
			postingMessage.setF_TRANSACTIONREF(atmHelper.getTransactionReference(atmLocalMessage));
			postingMessage.setF_TRANSACTIONDATE(atmLocalMessage.getDateTimeofTxn());
			postingMessage.setF_FORCEPOST(true);
			postingMessage.setProductID(accountValues.getF_PRODUCTID());
			postingMessage.setPERouterProfileID(accountValues.getF_PEROUTERPROFILEID());
			postingMessage.setBranchID(transaction.getF_SOURCEBRANCH());
			postingMessage.setShortName(transaction.getF_SHORTNAME());
			postingMessage.setF_VALUEDATE(transaction.getF_VALUEDATE());
			postingMessage.setNarrative(PostingHelper.getBuildedNarrative(postingMessage, CommonConstants.EMPTY_STRING));
			postingMessages.add(postingMessage);
		}
	}

	/**
	 * This method updates the Transaction table with Reversal Indicator value 3 for correction txns 
	 * 
	 */
	private void updateTransactionTable(ArrayList txnIds, BankFusionEnvironment env) {
		List listoftransactions = new ArrayList();
		ArrayList params = new ArrayList();
		params.add(txnIds.get(0));
		try {
			listoftransactions = env.getFactory().findByQuery(IBOTransaction.BONAME, trnsnWhrClause, params, null);

		}
		catch (BankFusionException bfe) {

		}
		try {
			for (ListIterator i = listoftransactions.listIterator(); i.hasNext();) {

				IBOTransaction transaction = (IBOTransaction) i.next();
				transaction.setF_REVERSALINDICATOR(TRANCATION_REVERSAL_INDICATOR);
				//transaction.setF_TRANSACTIONCROSSREFID(reversalTxnID);
			}

			env.getFactory().commitTransaction();
			
			env.getFactory().beginTransaction();            //
		}
		catch (BankFusionException exception) {
			env.getFactory().rollbackTransaction();
		
			
			env.getFactory().beginTransaction();            //
		}
		finally {
			env.getFactory().beginTransaction();
		}
	}

	/**
	 * This method checks whether the message contains a valid force post value for correction.
	 */
	private void validateForcePost(ATMSparrowFinancialMessage message) {
		if (!(message.getForcePost().equals(ATMConstants.FORCEPOST_1)
				|| message.getForcePost().equals(ATMConstants.FORCEPOST_2) || message.getForcePost().equals(
				ATMConstants.FORCEPOST_3))) {
			message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
			message.setErrorCode(ATMConstants.CRITICAL);
			message.setErrorDescription("Invalid Force Post Value for Reversal");
			return;
		}
	}

	/**
	 * This method checks whether the message is a for a LORO transaction. 
	 * @return boolean  
	 */
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

	/**
	 * This method checks whether the message is Local or External.
	 * @return String 
	 */
	private String getTransactionType(ATMSparrowFinancialMessage message) {
		String result = ATMMessageValidator.LOCAL_MESSGE_TYPE;
		if (message.getMessageType().equals("8")) {
			result = ATMMessageValidator.LOCAL_MESSGE_TYPE;
		}
		else {
			result = ATMMessageValidator.EXTERNAL_MESSAGE_TYPE;
		}
		return result;
	}

}
