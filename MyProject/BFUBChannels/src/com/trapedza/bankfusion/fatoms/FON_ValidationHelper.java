/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_ValidationHelper.java,v 1.17 2008/08/28 01:08:31 varap Exp $
 * **********************************************************************************
 *
 * Revision 1.14  2008/02/16 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 */

package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.fontis.FON_BatchRecord;
import com.misys.ub.fontis.FON_CreditRecord;
import com.misys.ub.fontis.FON_DebitRecord;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountLimitFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.AccountLimitFeature;
import com.trapedza.bankfusion.features.LimitsFeature;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

/**
 * This class contains methods for validating a fontis batch
 * 
 * @author hardikp,vinayac
 */
public class FON_ValidationHelper {
	private static IBusinessInformation bizInfo;
	static {
		IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
				.getInstance()
				.getServiceManager()
				.getServiceForName(
						IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
		bizInfo = ubInformationService.getBizInfo();
	}
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Logger instance
	 */
	private transient final static Log LOGGER = LogFactory
			.getLog(FON_ValidationHelper.class.getName());

	/**
	 * constant for TPP file type
	 */
	private static final String tppFileConstant = "TPP";

	/**
	 * constant for IAT file type
	 */
	private static final String iatFileConstant = "IAT";

	/**
	 * constant for failed validation
	 */
	private static final int statusFailed = 1;

	/**
	 * This Hash set will be populated with all the BIC codes available in the
	 * BICCODES table
	 */
	private static HashSet BICCodes = new HashSet();

	/**
	 * This Hash set will be populated with all the currency codes available in
	 * the currency table
	 */
	private static HashSet CurrencyCodes = new HashSet();

	/**
	 * Flag to indicate status 'false'
	 */
	private static boolean FALSEFLAG;

	/**
	 * Fontis configuration Product List (to be skipped in Insufficient funds
	 * check)
	 */
	private static HashSet ProductSet = new HashSet();

	/**
	 * Flag to indicate status 'true'
	 */
	private static boolean TRUEFLAG = true;

	/**
	 * Flag to indicate status Debit
	 */
	public static String signDR = "-";

	/**
	 * Flag to indicate status Credit
	 */
	public static String signCR = "+";

	/**
	 * Flag which indicates the action to be taken if the account is dormant
	 */
	public static String DormancyPostingAction = "-1";

	/**
	 * Numeric code of the transaction type to be used while generating EFT file
	 * records.
	 */
	public static int TransTypeNumericCode;

	/**
	 * Dormancy status name & value based on this value respective action is
	 * taken for IAT/TPP/Credit/Debit transactions
	 */
	public static String MIS_ALLOW_TRANSACTION = "0";
	public static String MIS_REJECT_TRANSACTION = "1";
	public static String MIS_ALLOW_AFTER_AUTH = "2";
	public static String MIS_ALLOW_AFTER_LOG = "3";

	/**
	 * flag to indicate whether the batch under processing is an off-us batch or
	 * an on-us batch. (All Debit legs must belong to this bank only. No off-us
	 * debit concept supported.) TRUE - transaction has a Credit leg which
	 * belongs to another bank. FALSE - All credit transaction legs belong to
	 * this bank only.
	 */
	private boolean offUsBatch;

	/**
	 * Validates fontis batch with against validations mentioned in the usecase.
	 * 
	 * @param batch
	 * @param env
	 * @return @
	 */
	public boolean validateFontisBatch(FON_BatchRecord batch,
			BankFusionEnvironment env) {
		/**
		 * Flag to indicate validity of the fontis batch. TRUE - no validation
		 * failed on this batch. FALSE - batch has failed 1 or more validations.
		 */
		boolean validBatch = TRUEFLAG;

		/**
		 * Flag to indicate whether this batch has failed any of the mandatory
		 * validations. TRUE - 1 or more mandatory validation has failed on this
		 * batch. FALSE - no mandatory has validation failed on this batch.
		 */
		boolean MV_Failed = FALSEFLAG;

		String valueDateValdationError = CommonConstants.EMPTY_STRING;
		/**
		 * StringBuffer object to store errors in the General record of the
		 * fontis batch
		 */
		StringBuffer generalErr = new StringBuffer();

		boolean valueDateError = false;

		boolean debitError = false;

		boolean creditError = false;
		// Checks whether this batch contains any off-us credit leg.
		// Required to waive non-mandatory validations on a batch containing
		// Off-us transactions
		checkOnUsTransaction(batch);

		// validations for general record (Header record)
		generalErr.append("General Record Error: ");

		// If On-us batch then check whether sum of amounts in debit entries and
		// debit total in
		// header are equal or not.
		// DR1+DR2+DR3+... = DR TOTAL in header
		if (!offUsBatch && !sumOfDebitEntriesEqualsDebitTotal(batch)) {// Mandatory
			validBatch = FALSEFLAG;
			MV_Failed = TRUEFLAG;
			generalErr
					.append("Sum of DebitEntries unequal to debit total in header(Mandatory)\n");
		}

		// If On-us batch then check whether sum of amounts in credit entries
		// and credit total in
		// header are equal or not.
		// CR1+CR2+CR3+... = CR TOTAL in header
		if (!offUsBatch && !sumOfCreditEntriesEqualsCreditTotal(batch)) {// Mandatory
			validBatch = FALSEFLAG;
			MV_Failed = TRUEFLAG;
			generalErr
					.append("Sum of CreditEntries unequal to credit total in header(Mandatory)\n");
		}

		
		if (!creditTotalEqualsDebitTotal(batch)) {// Mandatory
			validBatch = FALSEFLAG;
			MV_Failed = TRUEFLAG;
			generalErr
					.append("Total Credit Not Equal to Total Debit(Mandatory) \n");
		}

		
		if (!offUsBatch && !transRecordsEqualsNumberGivenInHeader(batch)) {
			validBatch = FALSEFLAG;
			generalErr
					.append("Transactions records are not equal to No of Records in batch\n");
		}

		
		if (!offUsBatch && noCurrencyTotalInHeader(batch)) {
			validBatch = FALSEFLAG;
			generalErr.append("No Credit/Debit Total in Header\n");
		}

		ArrayList cRecordList = batch.getCreditRecords();
		ArrayList dRecordList = batch.getDebitRecords();
		ArrayList tempCRList = new ArrayList();
		ArrayList tempDRList = new ArrayList();

		// ******************************************** BEGIN : VALIDATIONS ON
		// DEBIT RECORDS
		// ********************************************////

		// Counter to identify debit record number in batch
		int dCnt = 0;
		Iterator dIterator = dRecordList.iterator();

		// validations on debit records
		generalErr.append(":: In DebitRecord : ");

		while (dIterator.hasNext()) {
			dCnt++;
			StringBuffer debitErr = new StringBuffer();

			// Fetch the Fontis Debit record
			FON_DebitRecord dRecord = (FON_DebitRecord) dIterator.next();
			IBOAccount debitAccObj = null;
			IBOAccountLimitFeature accntLimtitObj = null;
			Boolean limitAllowExcess = false;

			try {
				// Find the debit account object
				debitAccObj = (IBOAccount) env.getFactory().findByPrimaryKey(
						IBOAccount.BONAME, dRecord.getDebitAccountNo());

				// checking limit allow excess or not
				accntLimtitObj = (IBOAccountLimitFeature) debitAccObj;
				Date accountOpenDate = debitAccObj.getF_OPENDATE();
				Date valueDate = batch.getValueDate();
				/*
				 * check if the value date is before the account open date;
				 */
				if (valueDate.before(accountOpenDate)) {
					valueDateError = true;
					validBatch = FALSEFLAG;
					LOGGER.error("Value Date of transaction is before the account open date ");

				}
				if (accntLimtitObj.getF_LIMITEXCESSACTION() == 1)
					limitAllowExcess = true;
			} catch (BankFusionException bfe) {
				/*
				 * Bug#9305 Fix : Modifying on-us & off-us account not found
				 * validation // If Off-us batch if (offUsBatch) { // Set debit
				 * from suspense account flag to TRUE
				 * dRecord.setDebitFromCurrSuspenseAccount(TRUEFLAG);
				 * logger.info
				 * ("TPP Debit Account not found. Posting to Suspense Account."
				 * ); } // If On-us batch else { // Send batch for authorization
				 * where it can be authorized validBatch = FALSEFLAG;
				 * logger.error("Failed Validations:\n" +
				 * "Account details not found" +
				 * "Error occured while trying to access account information of "
				 * + dRecord.getDebitAccountNo()); }
				 */

				// If not specified then mark the batch as invalid, set
				// mandatory validation failed
				// flag to TRUE & set the error messages.
				validBatch = FALSEFLAG;
				debitError = true;
				MV_Failed = FALSEFLAG;
				generalErr.append(" " + dCnt + " , ");
				debitErr.append("Failed Validations:\n"
						+ "Debit account number not found");
				dRecord.setStatusFlag(statusFailed);
				LOGGER.error("Debit account number not found(Mandatory)"
						+ ": Currency Code = " + dRecord.getDebitCurrencyCode()
						+ ": Batch Number = " + batch.getBatchNo()
						+ ": Account Number = " + dRecord.getDebitAccountNo()
						+ ": Transaction Amount = " + dRecord.getAmount()
						+ ": Credit/Debit Transaction = DR");

				// If Debit Account is INVALID ,posting can be done in Currency
				// Suspense Account
				dRecord.setDebitFromCurrSuspenseAccount(TRUEFLAG);
				LOGGER.error(bfe);
			}

			// Check whether a valid the BIC code is specified in the debit
			// record
			if (!validateBICCode(dRecord.getDebitBICCode())) {
				// If invalid - Mark the batch as invalid, set mandatory
				// validation failed flag to
				// TRUE & set the error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + dCnt + " , ");
				debitErr.append("Failed Validations:\n"
						+ "Invalid BIC Code(Mandatory)");
				dRecord.setStatusFlag(statusFailed);
				LOGGER.error("Invalid BIC Code " + ": Currency Code = "
						+ dRecord.getDebitCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ dRecord.getDebitAccountNo()
						+ ": Transaction Amount = " + dRecord.getAmount()
						+ ": Credit/Debit Transaction = DR");
			}

			// Check whether the Debit BIC code is equal to this banks BIC code
			// (On-us debit
			// account, since off-us debit is not supported)
			if (!FON_TransactionProcessing.systemBICCode
					.equalsIgnoreCase(dRecord.getDebitBICCode())) {
				// If not equal then mark the batch as invalid, set mandatory
				// validation failed flag
				// to TRUE & set the error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + dCnt + " , ");
				debitErr.append("Failed Validations:\n"
						+ "Debit BIC Code does not match with this bank's BIC Code");
				dRecord.setStatusFlag(statusFailed);
				LOGGER.error("Debit BIC Code does not match with this bank's BIC Code "
						+ ": Currency Code = "
						+ dRecord.getDebitCurrencyCode()
						+ ": Batch Number = "
						+ batch.getBatchNo()
						+ ": Account Number = "
						+ dRecord.getDebitAccountNo()
						+ ": Transaction Amount = "
						+ dRecord.getAmount()
						+ ": Debit BIC Code = "
						+ dRecord.getDebitBICCode()
						+ ": Credit/Debit Transaction = DR");
			}

			// Check if no account is specified in the debit record i.e
			// AccountNumber = NULL or
			// CommonConstants.EMPTY_STRING
			if (noAccSpecified(dRecord.getDebitAccountNo())) {
				// If not specified then mark the batch as invalid, set
				// mandatory validation failed
				// flag to TRUE & set the error messages.
				validBatch = FALSEFLAG;
				MV_Failed = FALSEFLAG;
				generalErr.append(" " + dCnt + " , ");
				debitErr.append("Failed Validations:\n"
						+ "No account Specified");
				dRecord.setStatusFlag(statusFailed);
				LOGGER.error("Account Not Specified " + ": Currency Code = "
						+ dRecord.getDebitCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ dRecord.getDebitAccountNo()
						+ ": Transaction Amount = " + dRecord.getAmount()
						+ ": Credit/Debit Transaction = DR");
				dRecord.setDebitFromCurrSuspenseAccount(TRUEFLAG);
			}

			// If On-us batch then check whether reference is specified or not
			// i.e Reference = NULL
			// or CommonConstants.EMPTY_STRING

			// FOR BUG:11771
			/*
			 * if (!offUsBatch && noReferenceSpecified(dRecord.getReference()))
			 * { // If not specified then mark the batch as invalid, set the
			 * error messages & send it for authorization. validBatch =
			 * FALSEFLAG; generalErr.append(" " + dCnt + " , ");
			 * debitErr.append("Failed Validations:\n" +
			 * "No reference specified"); dRecord.setStatusFlag(statusFailed);
			 * logger.error("No reference specified " +
			 * ": Transaction Currency Code = " +
			 * dRecord.getTransactionCurrencyCode() + ": Batch Number = " +
			 * batch.getBatchNo() + ": Account Number = " +
			 * dRecord.getDebitAccountNo() + ": Transaction Amount = " +
			 * dRecord.getAmount() + ": Reference = " + dRecord.getReference());
			 * }
			 */

			// Validate account password flag for posting IAT & TPP transactions
			if (debitAccObj != null) {
				// If On-us batch
				if (!offUsBatch) {
					// If password is required for posting
					if (debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY // -1
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING // 1
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE) // 5
					{
						// Mark the batch as invalid, set the error messages &
						// send it for
						// authorization.
						validBatch = FALSEFLAG;
						generalErr.append(" " + dCnt + " , ");
						debitErr.append("Failed Validations:\n"
								+ "Debit posting needs to be authorized for this account");
						dRecord.setStatusFlag(statusFailed);
						LOGGER.error("Debit posting needs to be authorized for this account "
								+ ": Currency Code = "
								+ dRecord.getDebitCurrencyCode()
								+ ": Batch Number = "
								+ batch.getBatchNo()
								+ ": Account Number = "
								+ dRecord.getDebitAccountNo()
								+ ": Transaction Amount = "
								+ dRecord.getAmount()
								+ ": Credit/Debit Transaction = DR");
					}
					// If account is stopped
					else if (debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY // 2
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY // 3
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.DEBITS_NOT_ALLOWED) // 4
					{
						// Mark the batch as invalid, set mandatory validation
						// failed flag to TRUE &
						// set the error messages.
						validBatch = FALSEFLAG;
						MV_Failed = TRUEFLAG;
						generalErr.append(" " + dCnt + " , ");
						debitErr.append("Failed Validations:\n"
								+ "Debit posting not allowed on the account(Mandatory)");
						dRecord.setStatusFlag(statusFailed);
						LOGGER.error("Debit posting not allowed on the account(Mandatory) "
								+ ": Currency Code = "
								+ dRecord.getDebitCurrencyCode()
								+ ": Batch Number = "
								+ batch.getBatchNo()
								+ ": Account Number = "
								+ dRecord.getDebitAccountNo()
								+ ": Transaction Amount = "
								+ dRecord.getAmount()
								+ ": Credit/Debit Transaction = DR");
					}
				}
				// If Off-us batch
				else {
					// If password is required for posting
					if (debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY // -1
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING // 1
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE) // 5
					{
						// FORCE POST the transaction on to the same account
						dRecord.setForcePost(TRUEFLAG);
						LOGGER.error("Use Currency Suspense account for Debit posting"
								+ ": Currency Code = "
								+ dRecord.getDebitCurrencyCode()
								+ ": Batch Number = "
								+ batch.getBatchNo()
								+ ": Account Number = "
								+ dRecord.getDebitAccountNo()
								+ ": Transaction Amount = "
								+ dRecord.getAmount()
								+ ": Credit/Debit Transaction = DR");
					}
					// If account is stopped
					else if (debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY // 2
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY // 3
							|| debitAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.DEBITS_NOT_ALLOWED) // 4
					{
						// Mark the batch as invalid, set mandatory validation
						// failed flag to TRUE &
						// set the error messages.
						validBatch = FALSEFLAG;
						MV_Failed = TRUEFLAG;
						generalErr.append(" " + dCnt + " , ");
						debitErr.append("Failed Validations:\n"
								+ "Debit posting not allowed on the account(Mandatory)");
						dRecord.setStatusFlag(statusFailed);
						LOGGER.error("Debit posting not allowed on the account(Mandatory) "
								+ ": Currency Code = "
								+ dRecord.getDebitCurrencyCode()
								+ ": Batch Number = "
								+ batch.getBatchNo()
								+ ": Account Number = "
								+ dRecord.getDebitAccountNo()
								+ ": Transaction Amount = "
								+ dRecord.getAmount()
								+ ": Credit/Debit Transaction = DR");
					}
				}
			}

			// If a not a valid account
			if (!validateAccount(debitAccObj)) {
				// If Off-Us batch
				if (offUsBatch) {
					// then set debit from suspense flag to TRUE
					dRecord.setDebitFromCurrSuspenseAccount(TRUEFLAG);
				} else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ " Not a valid account");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Not a valid account " + ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
				}
			}

			// If the debit account specified is Dormant
			if (debitAccObj != null && debitAccObj.isF_DORMANTSTATUS()) {

				// If the transaction dormancy is set as Allow if the account is
				// marked as dormant
				// after authorization
				if (DormancyPostingAction
						.equalsIgnoreCase(MIS_ALLOW_AFTER_AUTH)) {
					// If On-Us batch
					if (!offUsBatch) {
						// Mark the batch as invalid, set the error messages &
						// send it for
						// authorization.
						validBatch = FALSEFLAG;
						generalErr.append(" " + dCnt + " , ");
						dRecord.setStatusFlag(statusFailed);
						debitErr.append("Dormant Account : Debit Posting needs authorization\n");
						LOGGER.error("Debit Posting not allowed "
								+ ": Currency Code = "
								+ dRecord.getDebitCurrencyCode()
								+ ": Batch Number = " + batch.getBatchNo()
								+ ": Account Number = "
								+ dRecord.getDebitAccountNo()
								+ ": Transaction Amount = "
								+ dRecord.getAmount()
								+ ": Credit/Debit Transaction = DR");
					}
					// If Off-Us batch
					else {
						// FORCE POST the transaction on to the same account
						dRecord.setForcePost(TRUEFLAG);
						LOGGER.info("TPP File : Bypassing Authorization & Posting to the same account.");
					}
				}
				// If the transaction dormancy is set as Allow if the account is
				// marked as dormant
				// after logging information
				else if (DormancyPostingAction
						.equalsIgnoreCase(MIS_ALLOW_AFTER_LOG)) {
					// Allow after logging
					LOGGER.info("Fontis transaction posted "
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
				}
				// If the transaction dormancy is set as Reject if the account
				// is marked as dormant
				else if (DormancyPostingAction
						.equalsIgnoreCase(MIS_REJECT_TRANSACTION)) {
					// Mark the batch as invalid, set mandatory validation
					// failed flag to TRUE & set
					// the error messages.
					validBatch = FALSEFLAG;
					MV_Failed = TRUEFLAG;
					generalErr.append(" " + dCnt + " , ");
					dRecord.setStatusFlag(statusFailed);
					debitErr.append("Dormant Account : Debit Posting not allowed \n");
					LOGGER.error("Debit Posting not allowed "
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
				}
			}

			// Removed as new section has been introduced to achieve the
			// functionality
			/*
			 * if (debitAccObj!=null && !debitPostingAllowed(debitAccObj)) {
			 * validBatch = FALSEFLAG; generalErr.append(" " + dCnt + " , ");
			 * debitErr.append("Failed Validations:\n" +
			 * "Debit Posting Not Allowed\n");
			 * dRecord.setStatusFlag(statusFailed);
			 * logger.error("Debit Posting not allowed " + ": Currency Code = "
			 * + dRecord.getDebitCurrencyCode() + ": Batch Number = " +
			 * batch.getBatchNo() + ": Account Number = " +
			 * dRecord.getDebitAccountNo() + ": Transaction Amount = " +
			 * dRecord.getAmount() + ": Credit/Debit Transaction = DR"); }
			 */

			// If On-Us batch & Unrounded transfer amount is present
			if (!offUsBatch
					&& isUnroundedAmountPresent(
							dRecord.getTransactionCurrencyCode(),
							dRecord.getAmount(), env)) {
				// Mark the batch as invalid, set the error messages & send it
				// for authorization.
				validBatch = FALSEFLAG;
				generalErr.append(" " + dCnt + " , ");
				debitErr.append("Failed Validations:\n"
						+ "Unrounded amount present");
				dRecord.setStatusFlag(statusFailed);
				LOGGER.error("Unrounded amount present" + ": Currency Code = "
						+ dRecord.getDebitCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ dRecord.getDebitAccountNo()
						+ ": Transaction Amount = " + dRecord.getAmount()
						+ ": Credit/Debit Transaction = DR");
			}

			// If debit account has insufficient funds and limit do not allow
			// excess
			if (debitAccObj != null
					&& insufficientFunds(debitAccObj, dRecord.getAmount(), env)
					&& !(limitAllowExcess)) {
				// If the transaction is an IAT(Internal Account Transafer) &
				// account's sub-product
				// is specified in
				// fontis configuration to waive validation on having
				// insufficient funds
				if (batch.getFontisRecordType().equals(iatFileConstant)
						&& isFontisConfiguredAccount(debitAccObj)) {
					// Req#1050 : Begin : Set debit from Fontis suspense account
					// flag to TRUE
					batch.setInSuffIndPostAllCrDrToSuspense(TRUEFLAG);
					// Req#1050 : End
					dRecord.setDebitFromFontisSuspenseAccount(TRUEFLAG);
				}
				// If an Off-Us batch
				else if (offUsBatch) {
					// FORCE POST the transaction on to the same account
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ "Insufficient Fund for debit");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Insufficient Fund for debit "
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
					// dRecord.setForcePost(TRUEFLAG);
				}
				// If On-Us & not configured waiving insufficient funds check
				else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					// Posting to Currency Suspense after authorization(check
					// for insufficient
					// funds)
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ "Insufficient Fund for debit");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Insufficient Fund for debit "
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
				}
			}

			// Check Account Limit
			if (debitAccObj != null
					&& !limitAllowExcess
					&& !checkAccountLimits(debitAccObj.getF_ISOCURRENCYCODE(),
							debitAccObj.getBoID(), dRecord.getAmount(), signDR,
							env)) {

				// If the transaction is an IAT(Internal Account Transafer) &
				// account's
				// sub-product is specified in
				// fontis configuration to waive validation on having
				// insufficient funds
				if (batch.getFontisRecordType().equals(iatFileConstant)
						&& isFontisConfiguredAccount(debitAccObj)) {
					// Set debit from Fontis suspense account flag to TRUE
					dRecord.setDebitFromFontisSuspenseAccount(TRUEFLAG);
				}
				// If an Off-Us batch
				else if (offUsBatch) {
					dRecord.setForcePost(TRUEFLAG);
				}
				// If On-Us & not configured waiving insufficient funds check
				else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					// Posting to Currency Suspense after authorization(check
					// for insufficient
					// funds)
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					dRecord.setStatusFlag(statusFailed);
					debitErr.append("Account Limit exception : Transaction amount is not within the account limit specified on this account\n");
					LOGGER.error("Transaction amount is not within the account limit specified on this account "
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = "
							+ dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
				}

			}

			// Check Customer Limit
			if (debitAccObj != null
					&& !checkGroupLimits(debitAccObj.getF_ISOCURRENCYCODE(),
							debitAccObj.getBoID(), dRecord.getAmount(), signDR,
							env)) {
				// If an Off-Us batch
				if (offUsBatch) {
					dRecord.setForcePost(TRUEFLAG);
				}
				// If On-Us & not configured waiving insufficient funds check
				else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					// Posting to Currency Suspense after authorization(check
					// for insufficient
					// funds)
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					dRecord.setStatusFlag(statusFailed);
					debitErr.append("Group Limit exception : Transaction amount is not within the group limit specified on this account\n");
					LOGGER.error("Transaction amount is not within the group limit specified on this account "
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = "
							+ dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
				}
			}

			// If Invalid Debit account currency code
			if (isInvalidCurrency(dRecord.getDebitCurrencyCode())) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + dCnt + " , ");
				dRecord.setStatusFlag(statusFailed);
				generalErr
						.append("Mandatory Validation Failed :Invalid Debit Currency Code\n");
				debitErr.append("Invalid Debit Currency Code(Mandatory)\n");
				LOGGER.error("Currency Invalid " + ": Currency Code = "
						+ dRecord.getDebitCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ dRecord.getDebitAccountNo()
						+ ": Transaction Amount = " + dRecord.getAmount()
						+ ": Credit/Debit Transaction = DR");

			}
			// If valid Debit account currency code
			else {
				// Set Foreign currency transaction flag to TRUE. (Will be used
				// while creating
				// position account entries)
				/* if(batch.getFontisRecordType().equals(iatFileConstant)){ */
				if (!dRecord.getDebitCurrencyCode().equals(
						SystemInformationManager.getInstance()
								.getBaseCurrencyCode()))
					batch.setForgnCurrBatch(true);
				/*
				 * }else
				 * if(batch.getFontisRecordType().equals(tppFileConstant)){ if(!
				 * dRecord.getDebitCurrencyCode
				 * ().equals(SystemInformationManager
				 * .getInstance().getBaseCurrencyCode()))
				 * batch.setForgnCurrBatch(true); }
				 */
			}

			// If Actual & Given Debit account currency codes mismatch
			if (debitAccObj != null
					&& !(debitAccObj.getF_ISOCURRENCYCODE()
							.equalsIgnoreCase(dRecord.getDebitCurrencyCode()))) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + dCnt + " , ");
				dRecord.setStatusFlag(statusFailed);
				generalErr.append("Mandatory Validation Failed : Actual("
						+ dRecord.getDebitCurrencyCode() + ") & given("
						+ debitAccObj.getF_ISOCURRENCYCODE()
						+ ") Debit Account Currency Code mismatch \n");
				debitErr.append("Actual(" + dRecord.getDebitCurrencyCode()
						+ ") & given(" + debitAccObj.getF_ISOCURRENCYCODE()
						+ ") Debit Account Currency Code mismatch \n");
				LOGGER.error("Actual & given Debit account currency code mismatch(Mandatory) "
						+ ": Given Debit Account Currency Code = "
						+ dRecord.getDebitCurrencyCode()
						+ ": Actual Debit Account Currency Code = "
						+ debitAccObj.getF_ISOCURRENCYCODE()
						+ ": Batch Number = "
						+ batch.getBatchNo()
						+ ": Account Number = "
						+ dRecord.getDebitAccountNo()
						+ ": Transaction Amount = "
						+ dRecord.getAmount()
						+ ": Credit/Debit Transaction = DR");

			}

			// If the account is Closed
			if (debitAccObj != null && isAccountClosed(debitAccObj)) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + dCnt + " , ");
				dRecord.setStatusFlag(statusFailed);
				generalErr
						.append("Mandatory Validation Failed :Account Closed \n");
				debitErr.append("Mandatory Validation Failed :"
						+ "Account is closed for Debit \n");
				LOGGER.error("Account Closed " + ": Currency Code = "
						+ dRecord.getDebitCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ dRecord.getDebitAccountNo()
						+ ": Transaction Amount = " + dRecord.getAmount()
						+ ": Credit/Debit Transaction = DR");
			}

			// If IAT (Internal Account Transfer)
			if (batch.getFontisRecordType().equals(iatFileConstant)) {
				/*
				 * if (invalidExchangeRate(dRecord.getDebitExchangeRate())) {
				 * validBatch = FALSEFLAG; MV_Failed = TRUEFLAG;
				 * generalErr.append(" " + dCnt + " , ");
				 * debitErr.append("Failed Validations:\n" +
				 * "Invalid Exchange Rate(Mandatory)");
				 * dRecord.setStatusFlag(statusFailed);
				 * logger.error("Invalid Exchange Rate" + ": Currency Code = " +
				 * dRecord.getDebitCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " +
				 * dRecord.getDebitAccountNo() + ": Transaction Amount = " +
				 * dRecord.getAmount() + ": Credit/Debit Transaction = DR"); }
				 * if
				 * (invalidExchangeRate(dRecord.getTransactionExchangeRate())) {
				 * validBatch = FALSEFLAG; MV_Failed = TRUEFLAG;
				 * generalErr.append(" " + dCnt + " , ");
				 * debitErr.append("Failed Validations:\n" +
				 * "Invalid Exchange Rate(Mandatory)");
				 * dRecord.setStatusFlag(statusFailed);
				 * logger.error("Invalid Exchange Rate " + ": Currency Code = "
				 * + dRecord.getDebitCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " +
				 * dRecord.getDebitAccountNo() + ": Transaction Amount = " +
				 * dRecord.getAmount() + ": Credit/Debit Transaction = DR"); }
				 */

				// If On-us batch, Validate the given base equivalent against
				// the amount specified
				if (!offUsBatch
						&& !validateEquivalentAmount(dRecord.getAmount(),
								dRecord.getAmountInBaseCurrency(),
								dRecord.getDebitExchangeRate())) {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ "Given & Calculated base equivalent amounts do not match");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Given & Calculated base equivalent amounts do not match"
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Transaction Currency Code = "
							+ dRecord.getTransactionCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = "
							+ dRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ dRecord.getAmountInBaseCurrency()
							+ ": Transaction exchange rate = "
							+ dRecord.getTransactionExchangeRate()
							+ ": Credit/Debit Transaction = DR");
				}

				// Validate debit & transaction Multiply/Divide flag
				if (!validateMultDivFlag(dRecord.getDebitMultiplyDevideFlag(),
						dRecord.getTransactionMultiplyDevideFlag())) {
					// Mark the batch as invalid, set mandatory validation
					// failed flag to TRUE & set
					// the error messages.
					validBatch = FALSEFLAG;
					MV_Failed = TRUEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ "Invalid Multiply/Divide Flag(Mandatory)");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Invalid Multiply/Divide Flag "
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Credit/Debit Transaction = DR");
				}

				// If debit account currency is a non-base currency
				// Check whether the given exchange rate is within the tolerance
				// limit in UB
				if (!SystemInformationManager.getInstance()
						.getBaseCurrencyCode()
						.equalsIgnoreCase(dRecord.getDebitCurrencyCode())
						&& !exchangeRateUnreasonable(
								dRecord.getDebitCurrencyCode(),
								SystemInformationManager.getInstance()
										.getBaseCurrencyCode(),
								dRecord.getAmount(),
								dRecord.getAmountInBaseCurrency(),
								dRecord.getDebitExchangeRate(), env)) {

					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ "Unreasonable Debit Currency Exchange Rate");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Unreasonable Exchange Rate "
							+ ": Debit Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ dRecord.getAmountInBaseCurrency()
							+ ": Debit Currency Exchange Rate = "
							+ dRecord.getDebitExchangeRate()
							+ ": Credit/Debit Transaction = DR");
				}

				// Not required as per Discussion
				/*
				 * if(!SystemInformationManager.getInstance().getBaseCurrencyCode
				 * ().equalsIgnoreCase( dRecord.getTransactionCurrencyCode())) {
				 * if
				 * (!exchangeRateUnreasonable(dRecord.getTransactionCurrencyCode
				 * (),
				 * SystemInformationManager.getInstance().getBaseCurrencyCode(),
				 * dRecord.getAmount(), dRecord.getTransactionExchangeRate(),
				 * env)) { validBatch = FALSEFLAG; generalErr.append(" " + dCnt
				 * + " , "); debitErr.append("Failed Validations:\n" +
				 * "Unreasonable Transaction Currency Exchange Rate");
				 * dRecord.setStatusFlag(statusFailed);
				 * logger.error("Unreasonable Exchange Rate " +
				 * ": Transaction Currency Code = " +
				 * dRecord.getTransactionCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " +
				 * dRecord.getDebitAccountNo() + ": Transaction Amount = " +
				 * dRecord.getAmount() + ": Base Equivalent Amount = " +
				 * dRecord.getAmountInBaseCurrency() +
				 * ": Transaction Currency Exchange Rate = " +
				 * dRecord.getTransactionExchangeRate() +
				 * ": Credit/Debit Transaction = DR"); } }
				 */
			}

			// If TPP(Third Party Payment) file
			else {
				// Not required as per the discussion
				/*
				 * if (dRecord.getDebitDealersRate()==null ||
				 * dRecord.getDebitDealersRate().trim().equalsIgnoreCase
				 * (CommonConstants.EMPTY_STRING) || invalidExchangeRate(new
				 * BigDecimal(dRecord.getDebitDealersRate()))) { validBatch =
				 * FALSEFLAG; MV_Failed = TRUEFLAG; generalErr.append(" " + dCnt
				 * + " , "); debitErr.append("Failed Validations:\n" +
				 * "Invalid Exchange Rate(Mandatory)");
				 * dRecord.setStatusFlag(statusFailed);
				 * logger.error("Invalid Exchange Rate" + ": Currency Code = " +
				 * dRecord.getDebitCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " +
				 * dRecord.getDebitAccountNo() + ": Transaction Amount = " +
				 * dRecord.getAmount() + ": Credit/Debit Transaction = DR"); }
				 */

				// Create credit dealer's rate & set it to 0
				BigDecimal dealersRate = CommonConstants.BIGDECIMAL_ZERO;

				// If credit dealer's rate is mentioned in the input file
				if (dRecord.getDebitDealersRate() != null
						&& !dRecord.getDebitDealersRate().trim()
								.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
					try {
						// Use the given dealer's rate
						dealersRate = new BigDecimal(
								dRecord.getDebitDealersRate());
					} catch (Exception ex) {
						// If non-numeric format then dealer's rate will remain
						// as 0
						LOGGER.error(ex);
					}
				}

				// Validate the given base equivalent against the amount
				// specified
				if (!offUsBatch
						&& !validateEquivalentAmount(dRecord.getAmount(),
								dRecord.getAmountInBaseCurrency(), dealersRate)) {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ "Given & Calculated base equivalent amounts do not match");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Given & Calculated base equivalent amounts do not match"
							+ ": Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Transaction Currency Code = "
							+ dRecord.getTransactionCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = "
							+ dRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ dRecord.getAmountInBaseCurrency()
							+ ": Transaction exchange rate = "
							+ dRecord.getDebitDealersRate()
							+ ": Credit/Debit Transaction = DR");
				}

				// If On-Us batch & debit account currency is a non-base
				// currency
				// Check whether the given exchange rate is within the tolerance
				// limit in UB
				if (!offUsBatch
						&& !SystemInformationManager
								.getInstance()
								.getBaseCurrencyCode()
								.equalsIgnoreCase(
										dRecord.getDebitCurrencyCode())
						&& !exchangeRateUnreasonable(
								dRecord.getDebitCurrencyCode(),
								SystemInformationManager.getInstance()
										.getBaseCurrencyCode(),
								dRecord.getAmount(),
								dRecord.getAmountInBaseCurrency(), dealersRate,
								env)) {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					debitErr.append("Failed Validations:\n"
							+ "Unreasonable Exchange Rate");
					dRecord.setStatusFlag(statusFailed);
					LOGGER.error("Unreasonable Exchange Rate "
							+ ": Debit Currency Code = "
							+ dRecord.getDebitCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Debit Account Number = "
							+ dRecord.getDebitAccountNo()
							+ ": Transaction Amount = " + dRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ dRecord.getAmountInBaseCurrency()
							+ ": Debit Currency Dealers Rate = "
							+ dRecord.getDebitDealersRate()
							+ ": Credit/Debit Transaction = DR");

				}
			}

			// Consolidate : Debit validation details
			LOGGER.info("DebitErr: " + debitErr);
			if (valueDateError && (!debitError)) {
				valueDateValdationError = "Value Date of transaction is before the account open date for Account::"
						+ dRecord.getDebitAccountNo();
				debitErr = new StringBuffer(valueDateValdationError);
			}
			dRecord.setErrMessage(debitErr.toString());
			tempDRList.add(dRecord);
		}
		// ******************************************** END : VALIDATIONS ON
		// DEBIT RECORDS
		// ********************************************//

		// ******************************************** BEGIN : VALIDATIONS ON
		// CREDIT RECORDS
		// ********************************************//

		Iterator cIterator = cRecordList.iterator();
		// counter to identify credit record number in batch
		int cCnt = 0;
		generalErr.append("in CreditRecord: ");

		while (cIterator.hasNext()) {
			cCnt++;
			StringBuffer creditErr = new StringBuffer();
			// Fetch the Credit records from the fontis batch
			FON_CreditRecord cRecord = (FON_CreditRecord) cIterator.next();
			String cAccountNo = null;
			String cBICCode = null;
			IBOAccount creditAccObj = null;

			// If a TPP(Third Party Payments) file
			if (batch.getFontisRecordType().equals(tppFileConstant)) {
				cAccountNo = cRecord.getBeneficiaryAccountCode();
				cBICCode = cRecord.getBeneficiaryBankCode();
			}
			// If IAT(Internal Accounts Transfer) file
			else {
				cAccountNo = cRecord.getCreditAccountNo();
				cBICCode = cRecord.getCreditBICCode();

			}

			try {
				// Find the debit account object
				creditAccObj = (IBOAccount) env.getFactory().findByPrimaryKey(
						IBOAccount.BONAME, cAccountNo);
				Date accountOpenDate = creditAccObj.getF_OPENDATE();
				Date valueDate = batch.getValueDate();
				/*
				 * check if the value date is before the account open date;
				 */
				if (valueDate.before(accountOpenDate)) {
					valueDateError = true;
					validBatch = FALSEFLAG;
					LOGGER.error("Value Date of transaction is before the account open date ");
				}

			} catch (Exception ex) {
				LOGGER.info(ex.getMessage());
				creditError = true;
				// Bug#9305 Fix : Modifying on-us & off-us account not found
				// validation
				// If Off-us batch
				/*
				 * if(creditBicCode1.equalsIgnoreCase(FON_TransactionProcessing.
				 * systemBICCode)) { validBatch = FALSEFLAG; MV_Failed =
				 * TRUEFLAG; generalErr.append(" " + cCnt + " , ");
				 * creditErr.append("Failed Validations:\n" +
				 * "Credit account number not found(Mandatory)");
				 * cRecord.setStatusFlag(statusFailed);
				 * logger.error("Credit account number not found(Mandatory)" +
				 * ": Currency Code = " + cRecord.getCreditCurrencyCode() +
				 * ": Batch Number = " + batch.getBatchNo() +
				 * ": Account Number = " + cAccountNo +
				 * ": Transaction Amount = " + cRecord.getAmount() +
				 * ": Credit/Debit Transaction = CR"); } else
				 */
				if (!cBICCode
						.equalsIgnoreCase(FON_TransactionProcessing.systemBICCode))// offUsBatch)
				{
					// Set credit to suspense account flag to TRUE
					cRecord.setCreditToCurrSuspenseAccount(TRUEFLAG);
					LOGGER.info("TPP Credit Account not found. Posting to Suspense Account.");
				}
				// If On-us batch
				else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					MV_Failed = FALSEFLAG;
					generalErr.append(" " + cCnt + " , ");
					creditErr.append("Failed Validations:\n"
							+ "Invalid Account number");
					cRecord.setStatusFlag(statusFailed);
					LOGGER.error("Invalid Account Number "
							+ ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = " + cAccountNo
							+ ": Transaction Amount = " + cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");
					LOGGER.error("Failed Validations:\n"
							+ "Account details not found."
							+ " Error occured while trying to access account information of "
							+ cRecord.getCreditAccountNo());
				}
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				LOGGER.error(ex);
			}

			// Check whether a valid the BIC code is specified in the debit
			// record
			if (!validateBICCode(cBICCode)) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + cCnt + " , ");
				creditErr.append("Failed Validations:\n"
						+ "Invalid BICCode(Mandatory)");
				cRecord.setStatusFlag(statusFailed);
				LOGGER.error("Invalid BIC Code " + ": Currency Code = "
						+ cRecord.getCreditCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ cAccountNo + ": Transaction Amount = "
						+ cRecord.getAmount()
						+ ": Credit/Debit Transaction = CR");
			}

			// If an IAT file (IAT contains only this banks account numbers for
			// DR & CR transaction
			// legs)
			if (batch.getFontisRecordType().equalsIgnoreCase(iatFileConstant)) {
				// If Credit account currency != credit currency specified in
				// the IAT file
				if (creditAccObj != null
						&& !(creditAccObj.getF_ISOCURRENCYCODE()
								.equalsIgnoreCase(cRecord
										.getCreditCurrencyCode()))) {
					// Mark the batch as invalid, set mandatory validation
					// failed flag to TRUE & set
					// the error messages.
					validBatch = FALSEFLAG;
					MV_Failed = TRUEFLAG;
					generalErr.append(" " + dCnt + " , ");
					cRecord.setStatusFlag(statusFailed);
					generalErr.append("Mandatory Validation Failed : Actual("
							+ creditAccObj.getF_ISOCURRENCYCODE()
							+ ") & given(" + cRecord.getCreditCurrencyCode()
							+ ") Credit Account Currency Code mismatch \n");
					creditErr.append("Actual("
							+ creditAccObj.getF_ISOCURRENCYCODE()
							+ ") & given(" + cRecord.getCreditCurrencyCode()
							+ ") Credit Account Currency Code mismatch \n");
					LOGGER.error("Actual & given Credit account currency code mismatch(Mandatory) "
							+ ": Given Credit Account Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Actual Credit Account Currency Code = "
							+ creditAccObj.getF_ISOCURRENCYCODE()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = "
							+ cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");

				}
			}
			// If TPP file & Benificiary BIC Code is this bank's BIC Code
			// If If Credit account currency != transaction currency specified
			// in the IAT file
			else if (batch.getFontisRecordType().equalsIgnoreCase(
					tppFileConstant)
					&& FON_TransactionProcessing.systemBICCode
							.equalsIgnoreCase(cRecord.getBeneficiaryBankCode())
					&& creditAccObj != null
					&& !(creditAccObj.getF_ISOCURRENCYCODE()
							.equalsIgnoreCase(cRecord
									.getTransactionCurrencyCode()))) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set
				// the error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + dCnt + " , ");
				cRecord.setStatusFlag(statusFailed);
				generalErr.append("Mandatory Validation Failed : Account("
						+ creditAccObj.getF_ISOCURRENCYCODE() + ") & given("
						+ cRecord.getTransactionCurrencyCode()
						+ ") Transaction Currency Code mismatch \n");
				creditErr.append("Actual("
						+ creditAccObj.getF_ISOCURRENCYCODE() + ") & given("
						+ cRecord.getTransactionCurrencyCode()
						+ ") Credit Account Currency Code mismatch \n");
				LOGGER.error("Actual & given Credit account currency code mismatch(Mandatory) "
						+ ": Given Transaction Currency Code = "
						+ cRecord.getTransactionCurrencyCode()
						+ ": Actual Credit Account Currency Code = "
						+ creditAccObj.getF_ISOCURRENCYCODE()
						+ ": Batch Number = "
						+ batch.getBatchNo()
						+ ": Account Number = "
						+ cRecord.getCreditAccountNo()
						+ ": Transaction Amount = "
						+ cRecord.getAmount()
						+ ": Credit/Debit Transaction = CR");
			}

			// Check if no account is specified in the debit record i.e
			// AccountNumber = NULL or
			// CommonConstants.EMPTY_STRING
			if (noAccSpecified(cAccountNo)) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				validBatch = FALSEFLAG;
				MV_Failed = FALSEFLAG;
				generalErr.append(" " + cCnt + " , ");
				creditErr.append("Failed Validations:\n"
						+ "No account Specified");
				cRecord.setStatusFlag(statusFailed);
				LOGGER.error("No account Specified " + ": Currency Code = "
						+ cRecord.getCreditCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ cAccountNo + ": Transaction Amount = "
						+ cRecord.getAmount()
						+ ": Credit/Debit Transaction = CR");
				cRecord.setCreditToCurrSuspenseAccount(TRUEFLAG);
			}

			// If On-us batch then check whether reference is specified or not
			// i.e Reference = NULL
			// or CommonConstants.EMPTY_STRING
			// FOR BUG:11771
			/*
			 * if (!offUsBatch && noReferenceSpecified(cRecord.getReference()))
			 * { // Mark the batch as invalid, set the error messages & send it
			 * for authorization. validBatch = FALSEFLAG; generalErr.append(" "
			 * + dCnt + " , "); creditErr.append("Failed Validations:\n" +
			 * "No reference specified"); cRecord.setStatusFlag(statusFailed);
			 * logger.error("No reference specified " +
			 * ": Transaction Currency Code = " +
			 * cRecord.getTransactionCurrencyCode() + ": Batch Number = " +
			 * batch.getBatchNo() + ": Account Number = " +
			 * cRecord.getCreditAccountNo() + ": Transaction Amount = " +
			 * cRecord.getAmount() + ": Reference = " + cRecord.getReference());
			 * }
			 */

			// Validate account password flag for posting IAT & TPP transactions
			if (creditAccObj != null) {
				// If On-us batch
				if (!offUsBatch) {
					// If password is required for posting
					if (creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY // -1
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING // 1
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE) // 7
					{
						// Mark the batch as invalid, set the error messages &
						// send it for
						// authorization.
						validBatch = FALSEFLAG;
						generalErr.append(" " + dCnt + " , ");
						creditErr
								.append("Failed Validations:\n"
										+ "Credit posting needs to be authorized for this account");
						cRecord.setStatusFlag(statusFailed);
						LOGGER.error("Credit posting needs to be authorized for this account "
								+ ": Currency Code = "
								+ cRecord.getCreditCurrencyCode()
								+ ": Batch Number = "
								+ batch.getBatchNo()
								+ ": Account Number = "
								+ cRecord.getCreditAccountNo()
								+ ": Transaction Amount = "
								+ cRecord.getAmount()
								+ ": Credit/Debit Transaction = CR");
					}
					// If account is stopped
					else if (creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY // 2
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY // 3
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.CREDITS_NOT_ALLOWED) // 6
					{
						// Mark the batch as invalid, set the error messages &
						// send it for
						// authorization.
						validBatch = FALSEFLAG;
						generalErr.append(" " + dCnt + " , ");
						creditErr
								.append("Failed Validations:\n"
										+ "Credit posting not allowed on the account(Mandatory)");
						cRecord.setStatusFlag(statusFailed);
						LOGGER.error("Credit posting not allowed on the account(Mandatory) "
								+ ": Currency Code = "
								+ cRecord.getCreditCurrencyCode()
								+ ": Batch Number = "
								+ batch.getBatchNo()
								+ ": Account Number = "
								+ cRecord.getCreditAccountNo()
								+ ": Transaction Amount = "
								+ cRecord.getAmount()
								+ ": Credit/Debit Transaction = CR");
					}
				}
				// If Off-Us batch
				else {
					// If password is required for posting
					if (creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY // -1
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING // 1
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE) // 7
					{
						// FORCE POST TO THE SAME ACCOUNT WITHOUT AUTHORIZATION
						cRecord.setForcePost(TRUEFLAG);
						LOGGER.error("Force post to the same account "
								+ ": Currency Code = "
								+ cRecord.getCreditCurrencyCode()
								+ ": Batch Number = " + batch.getBatchNo()
								+ ": Account Number = "
								+ cRecord.getCreditAccountNo()
								+ ": Transaction Amount = "
								+ cRecord.getAmount()
								+ ": Credit/Debit Transaction = CR");
					}
					// If account is stopped
					else if (creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY // 2
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY // 3
							|| creditAccObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.CREDITS_NOT_ALLOWED) // 6
					{
						// Set credit to suspense account flag to TRUE
						cRecord.setCreditToCurrSuspenseAccount(TRUEFLAG);
					}
				}
			}

			// Not required:
			if (!offUsBatch
					&& isUnroundedAmountPresent(
							cRecord.getCreditCurrencyCode(),
							cRecord.getAmount(), env)) {
				// Mark the batch as invalid, set the error messages & send it
				// for authorization.
				validBatch = FALSEFLAG;
				generalErr.append(" " + dCnt + " , ");
				cRecord.setStatusFlag(statusFailed);
				generalErr
						.append("Validation Failed : Unrounded amount present\n");
				creditErr.append("Validation Failed :"
						+ " Unrounded amount present\n");
				LOGGER.error("Unrounded amount present " + ": Currency Code = "
						+ cRecord.getCreditCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ cRecord.getCreditAccountNo() + ": Amount = "
						+ cRecord.getAmount()
						+ ": Credit/Debit Transaction = CR");
			}

			// Check Account Limits
			if (creditAccObj != null
					&& !checkAccountLimits(creditAccObj.getF_ISOCURRENCYCODE(),
							creditAccObj.getBoID(), cRecord.getAmount(),
							signCR, env)) {
				// If an Off-Us batch
				if (offUsBatch) {
					cRecord.setForcePost(TRUEFLAG);
				}
				// If On-Us & not configured waiving insufficient funds check
				else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					// Posting to Currency Suspense after authorization(check
					// for insufficient
					// funds)
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					cRecord.setStatusFlag(statusFailed);
					creditErr
							.append("Account Limit exception : Transaction amount is not within the account limit specified on this account\n");
					LOGGER.error("Transaction amount is not within the account limit specified on this account "
							+ ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = "
							+ cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");
				}
			}

			// Check Account Limits
			if (creditAccObj != null
					&& !checkGroupLimits(creditAccObj.getF_ISOCURRENCYCODE(),
							creditAccObj.getBoID(), cRecord.getAmount(),
							signCR, env)) {
				// If an Off-Us batch
				if (offUsBatch) {
					cRecord.setForcePost(TRUEFLAG);
				}
				// If On-Us & not configured waiving insufficient funds check
				else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					// Posting to Currency Suspense after authorization(check
					// for insufficient
					// funds)
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					cRecord.setStatusFlag(statusFailed);
					creditErr
							.append("Group Limit exception : Transaction amount is not within the group limit specified on this account\n");
					LOGGER.error("Transaction amount is not within the group limit specified on this account "
							+ ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = "
							+ cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");
				}
			}

			// If the account is Closed
			if (creditAccObj != null && isAccountClosed(creditAccObj)) {
				// If Off-Us batch
				if (offUsBatch) {
					// Set credit to suspense account flag to TRUE
					cRecord.setCreditToCurrSuspenseAccount(TRUEFLAG);
				} else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					cRecord.setStatusFlag(statusFailed);
					generalErr.append("Validation Failed :Account Closed \n");
					creditErr.append("Validation Failed :"
							+ "Account is closed for Credit \n");
					LOGGER.error("Account Closed " + ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = " + cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");
				}
			}

			if (!validateAccount(creditAccObj)) {
				// If Off-Us batch
				if (offUsBatch) {
					// Set credit to suspense account flag to TRUE
					cRecord.setCreditToCurrSuspenseAccount(TRUEFLAG);
				} else {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					creditErr.append("Failed Validations:\n"
							+ " Not a valid account");
					cRecord.setStatusFlag(statusFailed);
					LOGGER.error("Not a valid account " + ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = " + cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");
				}
			}

			// If the debit account specified is Dormant
			if (creditAccObj != null && creditAccObj.isF_DORMANTSTATUS()) {

				// If the transaction dormancy is set as Allow if the account is
				// marked as dormant
				// after authorization
				if (DormancyPostingAction
						.equalsIgnoreCase(MIS_ALLOW_AFTER_AUTH)) {
					// If On-Us batch
					if (!offUsBatch) {
						// Mark the batch as invalid, set the error messages &
						// send it for
						// authorization.
						validBatch = FALSEFLAG;
						generalErr.append(" " + cCnt + " , ");
						cRecord.setStatusFlag(statusFailed);
						creditErr
								.append("Dormant Account : Credit Posting needs authorization\n");
						LOGGER.error("Credit Posting not allowed "
								+ ": Currency Code = "
								+ cRecord.getCreditCurrencyCode()
								+ ": Batch Number = " + batch.getBatchNo()
								+ ": Account Number = "
								+ cRecord.getCreditAccountNo()
								+ ": Transaction Amount = "
								+ cRecord.getAmount()
								+ ": Credit/Debit Transaction = CR");
					}
					/*
					 * else if(!offUsBatch) { validBatch = FALSEFLAG;
					 * generalErr.append(" " + cCnt + " , ");
					 * cRecord.setStatusFlag(statusFailed); creditErr.append(
					 * "Dormant Account : Credit Posting needs authorization\n"
					 * ); logger.error("Credit Posting not allowed " +
					 * ": Currency Code = " + cRecord.getCreditCurrencyCode() +
					 * ": Batch Number = " + batch.getBatchNo() +
					 * ": Account Number = " + cRecord.getCreditAccountNo() +
					 * ": Transaction Amount = " + cRecord.getAmount() +
					 * ": Credit/Debit Transaction = CR"); }
					 */
					// If Off-Us batch
					else {
						// Allow, if account not found while creating posting
						// message then post to
						// currency suspense account
						LOGGER.info("TPP File : Bypassing Authorization & Posting to the same account.");
					}
				}
				// If the transaction dormancy is set as Allow if the account is
				// marked as dormant
				// after logging information
				else if (DormancyPostingAction
						.equalsIgnoreCase(MIS_ALLOW_AFTER_LOG)) {
					// Allow after logging
					LOGGER.info("Fontis transaction posted : "
							+ ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = " + cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");
					;
				}
				// If the transaction dormancy is set as Reject if the account
				// is marked as dormant
				else if (DormancyPostingAction
						.equalsIgnoreCase(MIS_REJECT_TRANSACTION)) {
					// If On-Us batch
					if (!offUsBatch) {
						// Mark the batch as invalid, set the error messages &
						// send it for
						// authorization.
						validBatch = FALSEFLAG;
						generalErr.append(" " + cCnt + " , ");
						cRecord.setStatusFlag(statusFailed);
						creditErr
								.append("Dormant Account : Credit Posting not allowed \n");
						LOGGER.error("Credit Posting not allowed "
								+ ": Currency Code = "
								+ cRecord.getCreditCurrencyCode()
								+ ": Batch Number = " + batch.getBatchNo()
								+ ": Account Number = "
								+ cRecord.getCreditAccountNo()
								+ ": Transaction Amount = "
								+ cRecord.getAmount()
								+ ": Credit/Debit Transaction = CR");
					} else {
						// Set credit to suspense account flag to TRUE
						cRecord.setCreditToCurrSuspenseAccount(TRUEFLAG);
						LOGGER.info("TPP File : Bypassing Authorization & Posting to the same account.");
					}
				}
			}

			// If IAT batch & Invalid Credit account currency code
			if (batch.getFontisRecordType().equals(iatFileConstant)
					&& isInvalidCurrency(cRecord.getCreditCurrencyCode())) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + cCnt + " , ");
				cRecord.setStatusFlag(statusFailed);
				creditErr.append("Invalid Credit Currency Code(Mandatory)\n");
				LOGGER.error("Invalid Currency " + ": Currency Code = "
						+ cRecord.getCreditCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ cRecord.getCreditAccountNo()
						+ ": Transaction Amount = " + cRecord.getAmount()
						+ ": Credit/Debit Transaction = CR");
			}
			// If valid Credit account currency code
			else {
				// Set Foreign currency transaction flag to TRUE. (Will be used
				// while creating
				// position account entries)
				if (batch.getFontisRecordType().equals(iatFileConstant)) {
					if (!cRecord.getCreditCurrencyCode().equals(
							SystemInformationManager.getInstance()
									.getBaseCurrencyCode())) {
						batch.setForgnCurrBatch(true);
					}
				} else if (batch.getFontisRecordType().equals(tppFileConstant)
						&& !cRecord.getTransactionCurrencyCode().equals(
								SystemInformationManager.getInstance()
										.getBaseCurrencyCode())) {
					batch.setForgnCurrBatch(true);
				}
			}

			// If IAT batch & Invalid Transaction account currency code
			if (batch.getFontisRecordType().equals(tppFileConstant)
					&& isInvalidCurrency(cRecord.getTransactionCurrencyCode())) {
				// Mark the batch as invalid, set mandatory validation failed
				// flag to TRUE & set the
				// error messages.
				validBatch = FALSEFLAG;
				MV_Failed = TRUEFLAG;
				generalErr.append(" " + cCnt + " , ");
				cRecord.setStatusFlag(statusFailed);
				creditErr
						.append("Invalid Transaction Currency Code(Mandatory)\n");
				LOGGER.error("Invalid Currency " + ": Currency Code = "
						+ cRecord.getCreditCurrencyCode() + ": Batch Number = "
						+ batch.getBatchNo() + ": Account Number = "
						+ cRecord.getCreditAccountNo()
						+ ": Transaction Amount = " + cRecord.getAmount()
						+ ": Credit/Debit Transaction = CR");
			} else {
				// Set Foreign currency transaction flag to TRUE. (Will be used
				// while creating
				// position account entries)
				if (batch.getFontisRecordType().equals(iatFileConstant)) {
					if (!cRecord.getCreditCurrencyCode().equals(
							SystemInformationManager.getInstance()
									.getBaseCurrencyCode())) {
						batch.setForgnCurrBatch(true);
					}
				} else if (batch.getFontisRecordType().equals(tppFileConstant)
						&& !cRecord.getTransactionCurrencyCode().equals(
								SystemInformationManager.getInstance()
										.getBaseCurrencyCode())) {
					batch.setForgnCurrBatch(true);
				}
			}

			// If IAT(Internal Account Transfer) transaction
			if (batch.getFontisRecordType().equals(iatFileConstant)) {
				// Not required : As per the discussion
				/*
				 * if (invalidExchangeRate(cRecord.getCreditExchangeRate())) {
				 * validBatch = FALSEFLAG; MV_Failed = TRUEFLAG;
				 * generalErr.append(" " + cCnt + " , ");
				 * creditErr.append("Failed Validations:\n" +
				 * "Invalid Exchange Rate(Mandatory)");
				 * cRecord.setStatusFlag(statusFailed);
				 * logger.error("Invalid Exchange Rate " + ": Currency Code = "
				 * + cRecord.getCreditCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " + cAccountNo +
				 * ": Transaction Amount = " + cRecord.getAmount() +
				 * ": Credit/Debit Transaction = CR"); } if
				 * (invalidExchangeRate(cRecord.getTransactionExchangeRate())) {
				 * validBatch = FALSEFLAG; MV_Failed = TRUEFLAG;
				 * generalErr.append(" " + cCnt + " , ");
				 * creditErr.append("Failed Validations:\n" +
				 * "Invalid Exchange Rate(Mandatory)");
				 * cRecord.setStatusFlag(statusFailed);
				 * logger.error("Invalid Exchange Rate " + ": Currency Code = "
				 * + cRecord.getCreditCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " + cAccountNo +
				 * ": Transaction Amount = " + cRecord.getAmount() +
				 * ": Credit/Debit Transaction = CR"); }
				 */

				// Validate the given base equivalent against the amount
				// specified
				if (!validateEquivalentAmount(cRecord.getAmount(),
						cRecord.getAmountInBaseCurrency(),
						cRecord.getCreditExchangeRate())) {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					creditErr
							.append("Failed Validations:\n"
									+ "Given & Calculated base equivalent amounts do not match");
					cRecord.setStatusFlag(statusFailed);
					LOGGER.error("Given & Calculated base equivalent amounts do not match"
							+ ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Transaction Currency Code = "
							+ cRecord.getTransactionCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = "
							+ cRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ cRecord.getAmountInBaseCurrency()
							+ ": Transaction exchange rate = "
							+ cRecord.getTransactionExchangeRate()
							+ ": Credit/Debit Transaction = CR");
				}

				// Removed as new section has been introduced to implement the
				// functionality
				/*
				 * if (creditAccObj!=null &&
				 * !creditPostingAllowed(creditAccObj)) { validBatch =
				 * FALSEFLAG; generalErr.append(" " + cCnt + " , ");
				 * cRecord.setStatusFlag(statusFailed);
				 * creditErr.append("Credit Posting not allowed \n");
				 * logger.error("Credit Posting not allowed " +
				 * ": Currency Code = " + cRecord.getCreditCurrencyCode() +
				 * ": Batch Number = " + batch.getBatchNo() +
				 * ": Account Number = " + cRecord.getCreditAccountNo() +
				 * ": Transaction Amount = " + cRecord.getAmount() +
				 * ": Credit/Debit Transaction = CR"); }
				 */

				// Validate debit & transaction Multiply/Divide flag
				if (!validateMultDivFlag(cRecord.getCreditMultiplyDevideFlag(),
						cRecord.getTransactionMultiplyDevideFlag())) {
					// Mark the batch as invalid, set mandatory validation
					// failed flag to TRUE & set
					// the error messages.
					validBatch = FALSEFLAG;
					MV_Failed = TRUEFLAG;
					generalErr.append(" " + cCnt + " , ");
					cRecord.setStatusFlag(statusFailed);
					creditErr.append("Invalid M/D Flag(Mandatory) \n");
					LOGGER.error("Invalid Multiply/Divide Flag "
							+ ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = " + cRecord.getAmount()
							+ ": Credit/Debit Transaction = CR");
				}

				// If credit account currency is a non-base currency
				// Check whether the given exchange rate is within the tolerance
				// limit in UB
				if (!SystemInformationManager.getInstance()
						.getBaseCurrencyCode()
						.equalsIgnoreCase(cRecord.getCreditCurrencyCode())
						&& !exchangeRateUnreasonable(
								cRecord.getCreditCurrencyCode(),
								SystemInformationManager.getInstance()
										.getBaseCurrencyCode(),
								cRecord.getAmount(),
								cRecord.getAmountInBaseCurrency(),
								cRecord.getCreditExchangeRate(), env)) {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					creditErr.append("Failed Validations:\n"
							+ "Unreasonable Credit Currency Exchange Rate");
					cRecord.setStatusFlag(statusFailed);
					LOGGER.error("Unreasonable Exchange Rate "
							+ ": Credit Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = " + cRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ cRecord.getAmountInBaseCurrency()
							+ ": Credit Currency Exchange Rate = "
							+ cRecord.getCreditExchangeRate()
							+ ": Credit/Debit Transaction = DR");
				}
				// Not required as per Discussion
				/*
				 * if(!SystemInformationManager.getInstance().getBaseCurrencyCode
				 * ().equalsIgnoreCase( cRecord.getTransactionCurrencyCode())) {
				 * if
				 * (!exchangeRateUnreasonable(cRecord.getTransactionCurrencyCode
				 * (),
				 * SystemInformationManager.getInstance().getBaseCurrencyCode(),
				 * cRecord.getAmount(), cRecord.getTransactionExchangeRate(),
				 * env)) { validBatch = FALSEFLAG; generalErr.append(" " + dCnt
				 * + " , "); creditErr.append("Failed Validations:\n" +
				 * "Unreasonable Transaction Currency Exchange Rate");
				 * cRecord.setStatusFlag(statusFailed);
				 * logger.error("Unreasonable Exchange Rate " +
				 * ": Transaction Currency Code = " +
				 * cRecord.getTransactionCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " +
				 * cRecord.getCreditAccountNo() + ": Transaction Amount = " +
				 * cRecord.getAmount() + ": Base Equivalent Amount = " +
				 * cRecord.getAmountInBaseCurrency() +
				 * ": Transaction Currency Exchange Rate = " +
				 * cRecord.getTransactionExchangeRate() +
				 * ": Credit/Debit Transaction = CR"); } }
				 */
			}
			// If TPP(Third Party Payment) transaction
			else {
				// Not required as per Discussion
				/*
				 * if (cRecord.getCreditDealersRate()==null ||
				 * cRecord.getCreditDealersRate().trim().
				 * equalsIgnoreCase(CommonConstants.EMPTY_STRING) ||
				 * invalidExchangeRate(new
				 * BigDecimal(cRecord.getCreditDealersRate()))) { validBatch =
				 * FALSEFLAG; MV_Failed = TRUEFLAG; generalErr.append(" " + cCnt
				 * + " , "); creditErr.append("Failed Validations:\n" +
				 * "Invalid Exchange Rate(Mandatory)");
				 * cRecord.setStatusFlag(statusFailed);
				 * logger.error("Invalid Exchange Rate " + ": Currency Code = "
				 * + cRecord.getCreditCurrencyCode() + ": Batch Number = " +
				 * batch.getBatchNo() + ": Account Number = " + cAccountNo +
				 * ": Transaction Amount = " + cRecord.getAmount() +
				 * ": Credit/Debit Transaction = CR"); }
				 */

				// Create credit dealer's rate & set it to 0
				BigDecimal dealersRate = CommonConstants.BIGDECIMAL_ZERO;

				// If credit dealer's rate is mentioned in the input file
				if (cRecord.getCreditDealersRate() != null
						&& !cRecord.getCreditDealersRate().trim()
								.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
					try {
						// Use the given dealer's rate
						dealersRate = new BigDecimal(
								cRecord.getCreditDealersRate());
					} catch (Exception ex) {
						// If non-numeric format then dealer's rate will remain
						// as 0
						LOGGER.error(ex);
					}
				}

				// If On-Us batch, Validate the given base equivalent against
				// the amount specified
				if (!offUsBatch
						&& !validateEquivalentAmount(cRecord.getAmount(),
								cRecord.getAmountInBaseCurrency(), dealersRate)) {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					creditErr
							.append("Failed Validations:\n"
									+ "Given & Calculated base equivalent amounts do not match");
					cRecord.setStatusFlag(statusFailed);
					LOGGER.error("Given & Calculated base equivalent amounts do not match"
							+ ": Currency Code = "
							+ cRecord.getCreditCurrencyCode()
							+ ": Transaction Currency Code = "
							+ cRecord.getTransactionCurrencyCode()
							+ ": Batch Number = "
							+ batch.getBatchNo()
							+ ": Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = "
							+ cRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ cRecord.getAmountInBaseCurrency()
							+ ": Transaction exchange rate = "
							+ cRecord.getCreditDealersRate()
							+ ": Credit/Debit Transaction = CR");
				}

				// If On-Us batch & credit account currency is a non-base
				// currency
				// Check whether the given exchange rate is within the tolerance
				// limit in UB
				if (!offUsBatch
						&& !SystemInformationManager
								.getInstance()
								.getBaseCurrencyCode()
								.equalsIgnoreCase(
										cRecord.getTransactionCurrencyCode())
						&& !exchangeRateUnreasonable(
								cRecord.getTransactionCurrencyCode(),
								SystemInformationManager.getInstance()
										.getBaseCurrencyCode(),
								cRecord.getAmount(),
								cRecord.getAmountInBaseCurrency(), dealersRate,
								env)) {
					// Mark the batch as invalid, set the error messages & send
					// it for
					// authorization.
					validBatch = FALSEFLAG;
					generalErr.append(" " + dCnt + " , ");
					creditErr
							.append("Failed Validations:\n"
									+ "Unreasonable Transaction Currency Exchange Rate");
					cRecord.setStatusFlag(statusFailed);
					LOGGER.error("Unreasonable Exchange Rate "
							+ ": Transaction Currency Code = "
							+ cRecord.getTransactionCurrencyCode()
							+ ": Batch Number = " + batch.getBatchNo()
							+ ": Credit Account Number = "
							+ cRecord.getCreditAccountNo()
							+ ": Transaction Amount = " + cRecord.getAmount()
							+ ": Base Equivalent Amount = "
							+ cRecord.getAmountInBaseCurrency()
							+ ": Credit Dealers Exchange Rate = "
							+ cRecord.getCreditDealersRate()
							+ ": Credit/Debit Transaction = CR");
				}
			}

			// Consolidate : Credit validation details
			if (valueDateError && (!creditError)) {
				valueDateValdationError = "Value Date of transaction is before the account open date for Account::"
						+ cRecord.getCreditAccountNo();
				creditErr = new StringBuffer(valueDateValdationError);
			}
			LOGGER.debug("CreditErr:" + creditErr.toString());
			cRecord.setErrMessage(creditErr.toString());
			tempCRList.add(cRecord);
		}
		// ******************************************** END : VALIDATIONS ON
		// CREDIT RECORDS
		// ********************************************//

		// Consolidate : adding error messages for general record display
		/*
		 * Incase the valude date error Message Persist
		 */
		if (valueDateError) {
			generalErr = new StringBuffer(valueDateValdationError);
		}
		batch.setErrMessage(generalErr.toString());
		LOGGER.debug("General Errors: " + generalErr.toString());
		LOGGER.debug("IS valid Batch::" + validBatch);
		// bug fix # 3925 start
		// marking as invalid batch
		if (!validBatch) {
			// setting batch status if validation falied
			batch.setStatusFlag(statusFailed);
			if (MV_Failed) {
				batch.setMV_Failed(TRUEFLAG);
			}
		} else {
			// reset empty error message
			batch.setErrMessage(generalErr.delete(0, generalErr.length())
					.toString());
			// bug fix end
		}

		// return the Status back to FON_TransactionProcessing.java
		return validBatch;
	}

	/**
	 * Validating currency given in fontis record with availble currencies in UB
	 * System.
	 * 
	 * @param currCode
	 * @return TRUE - If invalid currency. FALSE - If valid currency.
	 */
	private boolean isInvalidCurrency(String currCode) {
		boolean valid = FALSEFLAG;

		if (!CurrencyCodes.contains(currCode)) {
			valid = TRUEFLAG;
			LOGGER.error("Invalid Currency Code :: " + currCode);
		}

		return valid;
	}

	/**
	 * Validates whether given amount is rounded or not
	 * 
	 * @param amountCurrency
	 * @param amount
	 * @param env
	 * @return TRUE - If Unrounded amount is present. FALSE - If rounded amount
	 *         is present.
	 */
	private boolean isUnroundedAmountPresent(String amountCurrency,
			BigDecimal amount, BankFusionEnvironment env) {
		int currencyRoundingUnits = bizInfo.getCurrencyScale(amountCurrency,
				env);

		int amountScale = amount.scale();
		if (currencyRoundingUnits != amountScale) {
			return TRUEFLAG;
		}

		return FALSEFLAG;
	}

	/**
	 * Validating whether transaction records are equal to number of debit and
	 * credit transaction in fontis header record
	 * 
	 * @param batch
	 * @return TRUE - transaction records is equal to the number mentioned in
	 *         the header. FALSE - otherwise.
	 */
	private boolean transRecordsEqualsNumberGivenInHeader(FON_BatchRecord batch) {
		boolean valid = TRUEFLAG;

		if (batch.getNo_Of_Credits() != batch.getCreditRecords().size()) {
			valid = FALSEFLAG;
			LOGGER.error("Actual credit transactions count & Header credit transaction count mis-match");
			LOGGER.error("Header Count = " + batch.getNo_Of_Credits()
					+ " Credit transactions");
			LOGGER.error("Actual Count = " + batch.getCreditRecords().size()
					+ " Credit transactions");
		}
		if (batch.getNo_Of_Debits() != batch.getDebitRecords().size()) {
			valid = FALSEFLAG;
			LOGGER.error("Actual debit transactions count & Header debit transaction count mis-match");
			LOGGER.error("Header Count = " + batch.getNo_Of_Debits()
					+ " debit transactions");
			LOGGER.error("Actual Count = " + batch.getDebitRecords().size()
					+ " debit transactions");
		}

		return valid;
	}

	/**
	 * Validation for no currency total available in Fontis header
	 * 
	 * @param batch
	 * @return TRUE - if no CR/DR totals specified or less than 0 FALSE - if
	 *         specified & greater than 0
	 */
	private boolean noCurrencyTotalInHeader(FON_BatchRecord batch) {
		boolean valid = FALSEFLAG;

		if (batch.getCreditTotal() == null
				|| batch.getCreditTotal().compareTo(
						CommonConstants.BIGDECIMAL_ZERO) <= 0
				|| batch.getDebitTotal() == null
				|| batch.getDebitTotal().compareTo(
						CommonConstants.BIGDECIMAL_ZERO) <= 0) {
			valid = TRUEFLAG;
		}

		return valid;
	}

	/**
	 * This method checks the sum of amounts in debit entries and debit total in
	 * header are equal or not.
	 * 
	 * @param batch
	 * @return TRUE - If DR1+DR2+DR3+... = DR TOTAL in header. FALSE -
	 *         otherwise.
	 */
	private boolean sumOfDebitEntriesEqualsDebitTotal(FON_BatchRecord batch) {
		boolean valid = TRUEFLAG;

		int sizeDR = batch.getDebitRecords().size();
		BigDecimal drTotal = new BigDecimal("0.0");
		double tempDrTotal = 0.0;
		for (int i = 0; i < sizeDR; i++) {
			BigDecimal baseEquvAmt = ((FON_DebitRecord) batch.getDebitRecords()
					.get(i)).getAmountInBaseCurrency();
			tempDrTotal = tempDrTotal + baseEquvAmt.doubleValue();
		}
		drTotal = new BigDecimal(Double.toString(tempDrTotal));
		if (batch.getDebitTotal() == null) {
			valid = FALSEFLAG;
			LOGGER.error("Sum of CreditEntries unequal to credit total in header "
					+ ": Batch Number = "
					+ batch.getBatchNo()
					+ ": Total i/p Debit Amount = "
					+ batch.getDebitTotal()
					+ ": Calculated Total Debit Amount = " + drTotal);
		} else if (batch.getDebitTotal().compareTo(drTotal) != 0) {
			valid = FALSEFLAG;
			LOGGER.error("Sum of DebitEntries unequal to debit total in header "
					+ ": Batch Number = "
					+ batch.getBatchNo()
					+ ": Total i/p Debit Amount = "
					+ batch.getDebitTotal()
					+ ": Calculated Total Debit Amount = " + drTotal);
		}
		return valid;
	}

	/**
	 * This method checks the sum of amounts in credit entries and credit total
	 * in header are equal or not.
	 * 
	 * @param batch
	 * @return TRUE - If CR1+CR2+CR3+... = CR TOTAL in header. FALSE -
	 *         otherwise.
	 */
	private boolean sumOfCreditEntriesEqualsCreditTotal(FON_BatchRecord batch) {
		boolean valid = TRUEFLAG;
		int sizeCR = batch.getCreditRecords().size();
		BigDecimal crTotal = new BigDecimal("0.0");

		double tempCrTotal = 0.0;
		for (int i = 0; i < sizeCR; i++) {
			BigDecimal baseEquvAmt = ((FON_CreditRecord) batch
					.getCreditRecords().get(i)).getAmountInBaseCurrency();
			if (baseEquvAmt == null)
				baseEquvAmt = ((FON_CreditRecord) batch.getCreditRecords().get(
						i)).getAmountInBaseCurrency();
			tempCrTotal = tempCrTotal + baseEquvAmt.doubleValue();
		}
		crTotal = new BigDecimal(Double.toString(tempCrTotal));

		if (batch.getCreditTotal() == null) {
			valid = FALSEFLAG;
			LOGGER.error("Sum of CreditEntries unequal to credit total in header "
					+ ": Batch Number = "
					+ batch.getBatchNo()
					+ ": Total i/p Credit Amount = "
					+ batch.getCreditTotal()
					+ ": Calculated Total Credit Amount = " + crTotal);
		} else if (batch.getCreditTotal().compareTo(crTotal) != 0) {
			valid = FALSEFLAG;
			LOGGER.error("Sum of CreditEntries unequal to credit total in header "
					+ ": Batch Number = "
					+ batch.getBatchNo()
					+ ": Total i/p Credit Amount = "
					+ batch.getCreditTotal()
					+ ": Calculated Total Credit Amount = " + crTotal);
		}
		return valid;
	}

	/**
	 * This method verifies credit and debit totals given in batch
	 * 
	 * @param batch
	 * @return TRUE - If DR1+DR2+DR3+... = CR1+CR2+CR3... FALSE - otherwise.
	 */
	private boolean creditTotalEqualsDebitTotal(FON_BatchRecord batch) {
		boolean valid = TRUEFLAG;

		// Calculate the Credit Total
		int sizeCR = batch.getCreditRecords().size();
		BigDecimal crTotal = new BigDecimal("0.0");
		double tempCrTotal = 0.0;
		for (int i = 0; i < sizeCR; i++) {
			BigDecimal baseEquvAmt = ((FON_CreditRecord) batch
					.getCreditRecords().get(i)).getAmountInBaseCurrency();
			if (baseEquvAmt == null)
				baseEquvAmt = ((FON_CreditRecord) batch.getCreditRecords().get(
						i)).getAmountInBaseCurrency();
			tempCrTotal = tempCrTotal + baseEquvAmt.doubleValue();
		}
		crTotal = new BigDecimal(Double.toString(tempCrTotal));

		// Calculate the Debit Total
		int sizeDR = batch.getDebitRecords().size();
		BigDecimal drTotal = new BigDecimal("0.0");
		double tempDrTotal = 0.0;
		for (int i = 0; i < sizeDR; i++) {

			BigDecimal baseEquvAmt = ((FON_DebitRecord) batch.getDebitRecords()
					.get(i)).getAmountInBaseCurrency();
			tempDrTotal = tempDrTotal + baseEquvAmt.doubleValue();
		}
		drTotal = new BigDecimal(Double.toString(tempDrTotal));

		// Check Credit Total == Debit Total
		if (crTotal.compareTo(drTotal) != 0)
			valid = FALSEFLAG;

		return valid;
	}

	/**
	 * Validation for account exists in system or not
	 * 
	 * @param accountNumber
	 * @param env
	 * @return TRUE - If Account object != NULL FALSE - otherwise.
	 */
	private boolean validateAccount(IBOAccount accountObj) {
		boolean valid = TRUEFLAG;

		if (accountObj == null) {
			valid = FALSEFLAG;
			LOGGER.error("The given account number is not found in the database");
		}

		return valid;
	}

	/**
	 * Validation for invalid exchange rate
	 * 
	 * @param batch
	 * @return boolean
	 */
	/*
	 * private boolean invalidExchangeRate(BigDecimal exchangeRate) { boolean
	 * valid = FALSEFLAG; // if (exchangeRate == null ||
	 * exchangeRate.equals(CommonConstants.EMPTY_STRING) ||
	 * exchangeRate.compareTo(CommonConstants.BIGDECIMAL_ZERO)<=0) if
	 * (exchangeRate.compareTo(CommonConstants.BIGDECIMAL_ZERO)<0) valid =
	 * TRUEFLAG; return valid; }
	 */

	/**
	 * This method validates that exchange rate given in batch is reasonable or
	 * not
	 * 
	 * @param fromCurr
	 * @param toCurr
	 * @param amount
	 * @param equiAmount
	 * @param exchRate
	 * @param env
	 * @return TRUE - If Exchange rate is within the tolerance limit FALSE -
	 *         otherwise.
	 */
	private boolean exchangeRateUnreasonable(String fromCurr, String toCurr,
			BigDecimal amount, BigDecimal equiAmount, BigDecimal exchRate,
			BankFusionEnvironment env) {
		boolean valid = TRUEFLAG;

		// If the given exchange rate is > 0
		if (exchRate.compareTo(CommonConstants.BIGDECIMAL_ZERO) > 0) {
			try {
				// Fetch UB exchange rate & tolerance for the given currency
				// combination
				IBusinessInformation ubBusinessInfo = ((IBusinessInformationService) ServiceManagerFactory
						.getInstance()
						.getServiceManager()
						.getServiceForName(
								IBusinessInformationService.BUSINESS_INFORMATION_SERVICE))
						.getBizInfo();
				BigDecimal ubExchRate = ubBusinessInfo
						.getEffectiveExchangeRate(fromCurr, toCurr, "SPOT",
								amount, env);
				BigDecimal exchRateTolerance = bizInfo.getExchangeTolerance(
						fromCurr, toCurr, "SPOT", env);

				// Calculate the difference b/w given & UB tolerance in
				// percentage
				BigDecimal diff = exchRate.subtract(ubExchRate);
				BigDecimal delta = (diff.divide(ubExchRate,
						SystemInformationManager.getInstance()
								.getCurrencyScale(toCurr))
						.multiply(new BigDecimal(100)));

				// if difference if > +/- of tolerance
				if (delta.abs().compareTo(exchRateTolerance) == 1) {
					// Set valid flag as false
					valid = FALSEFLAG;
				}
			} catch (Exception ex) {
				// Set valid flag as false on exception
				valid = FALSEFLAG;
				LOGGER.error(ex);
			}
		}
		// If the given exchange rate is <= 0
		else {
			BigDecimal baseCurrAmount = null;
			try {
				// Calculate the base currency equivalent as per UB exchange
				// rate
				BFCurrencyValue bfc = new BFCurrencyValue(fromCurr, amount,
						env.getUserID());
				baseCurrAmount = bfc.getRoundedAmount(SystemInformationManager
						.getInstance().getBaseCurrencyCode());

				// Calculate the difference b/w given & UB tolerance in
				// percentage using base
				// equivalent given & calculated
				BigDecimal exchRateTolerance = bizInfo.getExchangeTolerance(
						fromCurr, SystemInformationManager.getInstance()
								.getBaseCurrencyCode(), "SPOT", env);
				BigDecimal diff = equiAmount.subtract(baseCurrAmount);
				BigDecimal delta = (diff.divide(
						baseCurrAmount,
						SystemInformationManager.getInstance()
								.getCurrencyScale(
										SystemInformationManager.getInstance()
												.getBaseCurrencyCode()))
						.multiply(new BigDecimal(100)));

				// if difference if > +/- of tolerance
				if (delta.abs().compareTo(exchRateTolerance) == 1) {
					// Set valid flag as false
					valid = FALSEFLAG;
				}
			} catch (Exception ex) {
				// Set valid flag as false on exception
				valid = FALSEFLAG;
				/*LOGGER.error("Exception occured while finding the Base Equivalent : "
						+ ex.getMessage());*/
				LOGGER.error(ex);
			}
		}

		return valid;
	}

	/**
	 * Validates multiply/divide field in batch
	 * 
	 * @param debitMDFlag
	 * @param transactionMDFlag
	 * @return TRUE - If multiply/divide flag is 'M' or 'D' FALSE - otherwise.
	 */
	private boolean validateMultDivFlag(char debitMDFlag, char transactionMDFlag) {
		boolean valid = TRUEFLAG;

		char mulDivFlag = debitMDFlag;
		// bug fix # 3961 start
		// added check for transactionMultiDivFlag
		char transMultiDivFlag = transactionMDFlag;
		// bug fix end
		if ((mulDivFlag != 'M' && mulDivFlag != 'D')
				|| (transMultiDivFlag != 'M' && transMultiDivFlag != 'D'))
			valid = FALSEFLAG;

		return valid;
	}

	/**
	 * This method validates whether given debit account is dormant or not and
	 * can be reactivated or not
	 * 
	 * @param accountNumber
	 * @param env
	 * @return
	 */
	// Validations format changed
	/*
	 * private boolean validateDebitAccountDormancy(IBOAccount accountObj) {
	 * boolean valid = TRUEFLAG; boolean isDormant =
	 * accountObj.isF_DORMANTSTATUS(); if (isDormant) {
	 * logger.error("This account is Dormant. " + "Dormant Status = " +
	 * accountObj.getBoID()); if (DormancyCode != 0 && DormancyCode != 2) valid
	 * = FALSEFLAG; }
	 * 
	 * return valid; }
	 */

	/**
	 * This method validates whether given credit account is dormant or not and
	 * can be reactivated or not
	 * 
	 * @param accountNumber
	 * @param env
	 * @return
	 */
	// Validations format changed
	/*
	 * private boolean validateCreditAccountDormancy(IBOAccount accountObj) {
	 * boolean valid = TRUEFLAG;
	 * 
	 * boolean isDormant = accountObj.isF_DORMANTSTATUS(); if (isDormant) {
	 * logger.error("This account is Dormant. " + accountObj.getBoID() +
	 * "Dormant Status = " + isDormant); if (DormancyCode != 0 && DormancyCode
	 * != 3) valid = FALSEFLAG; }
	 * 
	 * return valid; }
	 */

	/**
	 * This method gives dormant account activation status - can be activated or
	 * not
	 * 
	 * @param accountNumber
	 * @param env
	 * @return
	 */
	protected static void accountNotOrToBeReactivated(BankFusionEnvironment env) {
		try {
			// Get the transaction code details from MISTransactionCodes table
			IBOMisTransactionCodes misTransObj = (IBOMisTransactionCodes) env
					.getFactory().findByPrimaryKey(
							IBOMisTransactionCodes.BONAME,
							FON_TransactionProcessing.fontisConfig
									.getF_TRANSACTION_TYPE());
			LOGGER.info("Reactivation Code = "
					+ misTransObj.getF_DORMANCYACTIVATIONCODE());
			LOGGER.info("Posting Action = "
					+ misTransObj.getF_DORMANCYPOSTINGACTION());

			// Populate DormancyPostingAction & TransTypeNumericCode
			DormancyPostingAction = misTransObj.getF_DORMANCYPOSTINGACTION();
			TransTypeNumericCode = misTransObj.getF_NUMERICTRANSCODE();
		} catch (BankFusionException bfe) {
			LOGGER.error(bfe);
			//bfe.printStackTrace();
			LOGGER.error("Error occured while trying to fetch Dormancy Status details from transaction table.");
			;
		}
	}

	/**
	 * This method checks that given account is closed or not
	 * 
	 * @param accountObj
	 * @return TRUE - if account is closed FALSE - otherwise.
	 */
	private boolean isAccountClosed(IBOAccount accountObj) {
		boolean valid = FALSEFLAG;

		if (accountObj.isF_CLOSED()) {
			valid = TRUEFLAG;
			LOGGER.error("This account is Closed. " + "Account Number = "
					+ accountObj.getBoID());
		}

		return valid;
	}

	/**
	 * This method checks whether the BIC COde supplied is valid or invalid by
	 * checking its presence in the HashSet populated with all supported BIC
	 * codes
	 * 
	 * @param BICCode
	 * @return TRUE - If given BIC Code is listed in UB. FALSE - otherwise.
	 */
	private boolean validateBICCode(String BICCode) {
		boolean valid = TRUEFLAG;

		// String bicCode = BICCode;
		LOGGER.debug("Batch BIC CODE = " + BICCode);
		if (!BICCodes.contains((String) BICCode)) {
			valid = FALSEFLAG;
		}
		return valid;
	}

	/**
	 * Checks whether acocunt is specified in the batch or not.
	 * 
	 * @param batch
	 * @return TRUE - no account is specified FALSE - otherwise.
	 */
	private boolean noAccSpecified(String accountNo) {
		boolean valid = FALSEFLAG;
		LOGGER.debug("ACCOUNT SPECIFIED = " + accountNo);
		if (accountNo != null
				&& accountNo.trim().equalsIgnoreCase(
						CommonConstants.EMPTY_STRING)) {
			valid = TRUEFLAG;
			LOGGER.debug("Inside if statement");
		}
		LOGGER.debug("Value=" + valid);
		return valid;
	}

	/**
	 * This method checks for insuficient funds in account
	 * 
	 * @param accountNumber
	 * @param transAmount
	 * @param env
	 * @return TRUE if account has insufficient funds FALSE if account has
	 *         sufficient funds
	 */
	public boolean insufficientFunds(IBOAccount accountObj,
			BigDecimal transAmount, BankFusionEnvironment env) {
		boolean valid = FALSEFLAG;
		BigDecimal availableBalance = CommonConstants.BIGDECIMAL_ZERO;
		Hashtable availBalBPParams = new Hashtable();
		availBalBPParams.put("AccountId", accountObj.getBoID());

		try {
			// Execute the Post Loan All Interest Microflow.
			Map result = MFExecuter
					.executeMF("ACC_AccountAvailableBalanceDisplay", env,
							availBalBPParams);
			availableBalance = (BigDecimal) result.get("AvailableBal");
		} catch (Exception ex) {
			LOGGER.error(ex);
			LOGGER.error("Exception occured while calculating Available balance on the account : "
					+ accountObj.getBoID());
		}

		/*
		 * BigDecimal clearedBalance = accountObj.getF_CLEAREDBALANCE();
		 * BigDecimal blockedBalance = accountObj.getF_BLOCKEDBALANCE();
		 * BigDecimal debitLimit = accountObj.getF_DEBITLIMIT(); BigDecimal
		 * availableBalance = (clearedBalance
		 * .subtract(blockedBalance)).add(debitLimit);
		 */

		if (availableBalance.compareTo(transAmount) == -1) {
			valid = TRUEFLAG;
			LOGGER.error("The account has insufficient funds");
			LOGGER.error("Account Number = " + accountObj.getBoID());
		}

		return valid;
	}

	/**
	 * This method checks whether supplied amount, equivalent amount & exchange
	 * rate are in sink with each other
	 * 
	 * @param account
	 *            currency code
	 * @param amount
	 * @param equiAmount
	 * @param exchRate
	 * @return TRUE - if valid FALSE - otherwise
	 */
	// private boolean validateEquivalentAmount(String fromCurr, BigDecimal
	// amount, BigDecimal
	// equiAmount, BigDecimal exchRate, BankFusionEnvironment env)
	private boolean validateEquivalentAmount(BigDecimal amount,
			BigDecimal equiBaseAmount, BigDecimal exchRate) {
		boolean valid = TRUEFLAG;

		if (exchRate.compareTo(CommonConstants.BIGDECIMAL_ZERO) > 0) {
			BigDecimal calcEquiAmount = amount.multiply(exchRate);

			if (calcEquiAmount.compareTo(equiBaseAmount) != 0) {
				valid = FALSEFLAG;
			}
		}

		/*
		 * else { BigDecimal baseCurrAmount = null; try { BFCurrencyValue bfc =
		 * new BFCurrencyValue(fromCurr, amount, env.getUserID());
		 * baseCurrAmount =
		 * bfc.getRoundedAmount(SystemInformationManager.getInstance
		 * ().getBaseCurrencyCode()); } catch(Exception ex) { valid = FALSEFLAG;
		 * logger.error("Exception occured while finding the Base Equivalent : "
		 * + ex.getMessage()); } BigDecimal exchRateTolerance =
		 * ((IServerBusinessInfo)
		 * SystemInformationManager.getInstance()).getExchangeTolerance
		 * (fromCurr,
		 * SystemInformationManager.getInstance().getBaseCurrencyCode(), "SPOT",
		 * env); BigDecimal diff = equiAmount.subtract(baseCurrAmount);
		 * BigDecimal delta = (diff.divide(baseCurrAmount,
		 * SystemInformationManager
		 * .getInstance().getCurrencyScale(SystemInformationManager
		 * .getInstance().getBaseCurrencyCode())).multiply(new
		 * BigDecimal(100)));
		 * 
		 * if(delta.abs().compareTo(exchRateTolerance)==1) { valid = FALSEFLAG;
		 * }
		 * 
		 * if(baseCurrAmount==null || baseCurrAmount.compareTo(equiAmount)!=0) {
		 * valid = FALSEFLAG; } }
		 */

		return valid;
	}

	/**
	 * This method checks for Limits on account/group. Internally calls
	 * checkAccountLimits(...) & checkGroupLimits(...)
	 * 
	 * @param accountNumber
	 * @param transAmount
	 * @param env
	 * @return TRUE if account has insufficient funds FALSE if account has
	 *         sufficient funds
	 */
	public boolean checkLimits(IBOAccount accountObj, BigDecimal transAmount,
			String signCRDR, BankFusionEnvironment env) {
		boolean valid = FALSEFLAG;

		try {
			// New Limit feature methods being used to check for limits
			valid = checkAccountLimits(accountObj.getF_ISOCURRENCYCODE(),
					accountObj.getBoID(), transAmount, signCRDR, env);
			if (valid) {
				LOGGER.info("Account limit verification passed");
				valid = checkGroupLimits(accountObj.getF_ISOCURRENCYCODE(),
						accountObj.getBoID(), transAmount, signCRDR, env);
			}
			if (valid) {
				LOGGER.info("Group limit verification passed");
			}
		} catch (Exception ex) {
			valid = FALSEFLAG;
			LOGGER.error("Exception occured while calculating Available balance on the account : "
					+ accountObj.getBoID());
			LOGGER.error(ex);
		}

		return valid;
	}

	/**
	 * Validate account limit
	 * 
	 * @param postingMsg
	 *            @
	 */
	private boolean checkAccountLimits(String accCurrCode, String accId,
			BigDecimal amount, String sign, BankFusionEnvironment env) {
		boolean validFlag = TRUEFLAG;
		// Call AccountLimitFeature to validate limits
		AccountLimitFeature accLimitFeature = new AccountLimitFeature(env);
		accLimitFeature.setF_IN_ACCOUNTID(accId);
		accLimitFeature.setF_IN_TRANSACTIONAMOUNT(amount);
		accLimitFeature.setF_IN_TRANSACTIONCURRENCY(accCurrCode);
		accLimitFeature.setF_IN_TRANSACTIONSIGN(sign);
		try {
			accLimitFeature.process(env);
			validFlag = accLimitFeature.isF_OUT_LIMITVALIDATIONSTATUS()
					.booleanValue();
		} catch (Exception bFExcp) {
			validFlag = FALSEFLAG;
			LOGGER.error("Account Limits Validation Exception :"
					+ bFExcp.getMessage());
			LOGGER.error(bFExcp);
		}

		return validFlag;
	}

	/**
	 * Validate group limit
	 * 
	 * @param postingMsg
	 *            @
	 */
	private boolean checkGroupLimits(String accCurrCode, String accId,
			BigDecimal amount, String sign, BankFusionEnvironment env) {
		boolean validFlag = TRUEFLAG;
		// Call LimitsFeature to validate limits
		LimitsFeature limitsFeature = new LimitsFeature(env);
		limitsFeature.setF_IN_AccountCurrencyIfPseudonym(accCurrCode);
		limitsFeature.setF_IN_AccountNo(accId);
		limitsFeature.setF_IN_Amount(amount);
		limitsFeature.setF_IN_Amount_Curr(accCurrCode);
		limitsFeature.setF_IN_AmountSign(sign);
		limitsFeature.setF_IN_PerformValidationsOnly(true);
		limitsFeature.setF_IN_ProcessAccountLimits(true);
		try {
			limitsFeature.process(env);
			validFlag = limitsFeature.isF_OUT_ProcessStatus().booleanValue();
		} catch (BankFusionException bFExcp) {
			validFlag = FALSEFLAG;
			LOGGER.error("Group Limits Validation Exception :"
					+ bFExcp.getMessage());
			LOGGER.error(bFExcp);
		}

		return validFlag;
	}

	/**
	 * This method checks that the product in which the account created, that
	 * product is configured in fontis configuration for insufficient fund check
	 * 
	 * @param accountNumber
	 * @param env
	 * @return TRUE - If account's sub-product is configured in fontis
	 *         configuration FALSE - otherwise.
	 */
	private boolean isFontisConfiguredAccount(IBOAccount accountObj) {
		boolean valid = FALSEFLAG;
		if (FON_TransactionProcessing.fontisConfig
				.isF_INSUFFICIENT_FUNDS_CHECK()
				&& ProductSet.contains(accountObj.getF_PRODUCTCONTEXTCODE())) {
			valid = TRUEFLAG;
		}

		return valid;
	}

	/**
	 * Checks whether the Debit postings are allowed on the given A/C
	 * 
	 * @param account
	 * @return boolean
	 */
	// Validations format changed
	/*
	 * private boolean debitPostingAllowed(IBOAccount accountObj) { boolean
	 * valid=TRUEFLAG;
	 * 
	 * if(accountObj.getF_ACCRIGHTSINDICATOR()==PasswordProtectedConstants.
	 * DEBITS_NOT_ALLOWED || accountObj
	 * .getF_ACCRIGHTSINDICATOR()==PasswordProtectedConstants
	 * .ACCOUNT_STOPPED_NO_POSTING_ENQUIRY) valid=FALSEFLAG;
	 * 
	 * return valid; }
	 */

	/**
	 * Checks whether the Credit postings are allowed on the given A/C
	 * 
	 * @param account
	 * @return boolean
	 */
	// Validations format changed
	/*
	 * private boolean creditPostingAllowed(IBOAccount accountObj) { boolean
	 * valid=TRUEFLAG;
	 * if(accountObj.getF_ACCRIGHTSINDICATOR()==PasswordProtectedConstants
	 * .CREDITS_NOT_ALLOWED || accountObj
	 * .getF_ACCRIGHTSINDICATOR()==PasswordProtectedConstants
	 * .ACCOUNT_STOPPED_NO_POSTING_ENQUIRY ) valid=FALSEFLAG;
	 * 
	 * return valid; }
	 */

	/**
	 * This method populates all BICCodes from system to HeshSet
	 * 
	 * @param env
	 */
	protected static void populateBICCode(BankFusionEnvironment env) {
		try {
			List bicCodesList = env.getFactory().findAll(IBOBicCodes.BONAME,
					null);

			for (int i = 0; i < bicCodesList.size(); i++) {
				IBOBicCodes bicCodeObj = (IBOBicCodes) bicCodesList.get(i);
				BICCodes.add(bicCodeObj.getBoID());
			}
		} catch (BankFusionException bfe) {
			/*
			 * throw new BankFusionException(9012, new Object[] {
			 * bfe.getMessage() }, logger, env);
			 */
			EventsHelper.handleEvent(
					ChannelsEventCodes.E_ERROR_IN_POPULATING_SYS_ID_CODE,
					new Object[] { bfe.getMessage() }, new HashMap(), env);
			LOGGER.error(bfe);
		}
	}

	/**
	 * This method populates all CurrencyCodes from system to HashSet
	 * 
	 * @param env
	 */
	protected static void populateCurrencyCode(BankFusionEnvironment env) {
		try {
			List currencyCodesList = env.getFactory().findAll(
					IBOCurrency.BONAME, null);

			for (int i = 0; i < currencyCodesList.size(); i++) {
				IBOCurrency currencyCodeObj = (IBOCurrency) currencyCodesList
						.get(i);
				CurrencyCodes.add(currencyCodeObj.getBoID());
			}
		} catch (BankFusionException bfe) {
			/*
			 * throw new BankFusionException(9013, new Object[] {
			 * bfe.getMessage() }, logger, env);
			 */
			EventsHelper.handleEvent(
					ChannelsEventCodes.E_VALUE_DATE_NOT_IN_PROPER_FORMAT,
					new Object[] { bfe.getMessage() }, new HashMap(), env);
			LOGGER.error(bfe);
		}
	}

	/**
	 * Checks whether batch is an On-Us or Off-Us.
	 * 
	 * @param fontisBatch
	 * @param env
	 */
	private void checkOnUsTransaction(FON_BatchRecord fontisBatch) {
		// String systemBICCode = this.getF_IN_BranchSortCode();
		LOGGER.info("This Bank's BICCODE = "
				+ FON_TransactionProcessing.systemBICCode);
		ArrayList creditRecordList = fontisBatch.getCreditRecords();
		for (int i = 0; i < creditRecordList.size(); i++) {
			FON_CreditRecord creditRecord = (FON_CreditRecord) creditRecordList
					.get(i);
			String creditBicCode = CommonConstants.EMPTY_STRING;
			if (fontisBatch.getFontisRecordType().equalsIgnoreCase(
					tppFileConstant)) {
				creditBicCode = creditRecord.getBeneficiaryBankCode();
			} else {
				creditBicCode = creditRecord.getCreditBICCode();
			}
			LOGGER.info("BICCODE==" + creditBicCode);
			if (creditBicCode != null
					&& creditBicCode.trim().length() > 8
					&& !creditBicCode
							.equalsIgnoreCase(FON_TransactionProcessing.systemBICCode)) {
				offUsBatch = TRUEFLAG;
			}
		}
		LOGGER.info("Is Off-Us batch = " + offUsBatch);
	}

	/**
	 * This method populates all Product Codes from Fontis Config object to
	 * HashSet
	 * 
	 * @param
	 */
	/*
	 * Bug # 5216 - Refactoring for performance : Being called from
	 * FON_TransactionProcessing's process() method.
	 */
	protected static void populateFontisProducts() {
		ProductSet.clear();
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_1());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_2());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_3());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_4());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_5());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_6());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_7());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_8());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_9());
		ProductSet.add(FON_TransactionProcessing.fontisConfig
				.getF_PRODUCTID_10());
	}
}
