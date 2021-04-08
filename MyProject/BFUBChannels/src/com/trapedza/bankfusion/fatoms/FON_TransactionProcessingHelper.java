/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_TransactionProcessingHelper.java,v 1.19.4.2 2008/08/23 00:17:18 venugopalp Exp $
 * **********************************************************************************
 * 
 * Revision 1.14  2008/02/16 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 */

package com.trapedza.bankfusion.fatoms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import com.trapedza.bankfusion.core.EventsHelper;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.fontis.FON_BatchRecord;
import com.misys.ub.fontis.FON_CreditRecord;
import com.misys.ub.fontis.FON_DebitRecord;
import com.trapedza.bankfusion.bo.refimpl.IBOCreditFontis;
import com.trapedza.bankfusion.bo.refimpl.IBODebitFontis;
import com.trapedza.bankfusion.bo.refimpl.IBOGeneralFontis;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * This class has methods to read & rename Fontis files. And also to store processed
 * records & fetch approved records from the database.
 * @author Vinaya
 *
 */
public class FON_TransactionProcessingHelper {

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
	private  final static Log logger = LogFactory.getLog(FON_TransactionProcessingHelper.class.getName());

	/**
	 * constant for TPP file type
	 */
	private final static String tppFileConstant = "TPP";

	/**
	 * constant for IAT file type
	 */
	private final static String iatFileConstant = "IAT";

	/**
	 * constant for file starting with .TPP
	 */
	private final static String dotTPPFileConstant = ".TPP";

	/**
	 * constant for file starting with .IAT
	 */
	private final static String dotIATFileConstant = ".IAT";

	/**
	 * this Vector holds all fontis batch record objects - IATBarchRecord and
	 * TPPBatchRecord
	 */
	private ArrayList batchRecords = new ArrayList();// vector of all batch records

	/**
	 * FontisBatchRecord Object
	 */
	private FON_BatchRecord fontisBatchRecord;// batchRecord object

	/**
	 * ArrayList which contains all IATDebitRecords
	 */
	private ArrayList fontisDebitRecords;// debit record object

	/**
	 * ArrayList which contains all IATCreditRecords
	 */
	private ArrayList fontisCreditRecords;// credit record object

	/**
	 * Field separator delimeter for file reading
	 */
	private static String fieldSaperatorDelim = new Character('\u001C').toString();

	/**
	 * empry string
	 */
	private static String EMPTY_STRING = CommonConstants.EMPTY_STRING;

	/**
	 * Status Flag value for approved fontis batch
	 */
	private static final int approvedBatchStatus = 2;

	/**
	 * where clause to fetch approved batch records
	 */
	private static final String findByStatusFlag = " WHERE " + IBOGeneralFontis.STATUS + " = ?";

	/**
	 * where clause to fetch credit records by batch number
	 */
	private static final String findCreditRecordByBatchNoAndStatus = " WHERE " + IBOCreditFontis.FONTIS_BATCH_ID
			+ " = ?";

	/**
	 * where clause to fetch debit records by batch number
	 */
	private static final String findDebitRecordByBatchNoAndStatus = " WHERE " + IBODebitFontis.FONTIS_BATCH_ID + " = ?";

	/**
	 * Reads the input file and processes the record for batch and detail records.
	 * 
	 * @param env
	 * @
	 */
	public List readFontisFile(File fontisFile, BankFusionEnvironment env) {
		logger.debug("Inside readFontisFile() method");
		ArrayList batchRecordList = null;

		//			If an IAT file
		if (fontisFile.getName().startsWith(FON_TransactionProcessingHelper.dotIATFileConstant)) {
			//				Call IAT File reader
			batchRecordList = (ArrayList) iatFileReader(fontisFile, env);
		}
		//			If TPP file
		else if (fontisFile.getName().startsWith(FON_TransactionProcessingHelper.dotTPPFileConstant)) {
			//				Call TPP file reader
			batchRecordList = (ArrayList) tppFileReader(fontisFile, env);
		}

		logger.debug("END readFontisFile() method");
		return batchRecordList;
	}

	/**
	 * Getting filename and renaming the given file
	 * 
	 * @param fileName - Name of the input file
	 * @param readFlag - TRUE if no error has occured while reading the input file
	 * 					 FALSE otherwise
	 * @
	 */
	public void renameProcessedFile(File fontisFile, boolean readFlag) {
		logger.debug("Inside renameProcessedFile() method");

		//		If end of File is reached without encountering any errors
		if (readFlag) {
			//			Get the current business date & time
			String time = (SystemInformationManager.getInstance().getBFBusinessDateTimeAsString()).replace(':', '-')
					.replace(' ', '_');

			//			Append '_SAV_' to the file name
			String newFileName = "_" + fontisFile.getName() + "_SAV_" + time;
			logger.debug("NEW FILE NAME :: " + newFileName);
			File oldFile = fontisFile;

			File newFile = new File(oldFile.getParent(), newFileName);

			//			Rename the File
			oldFile.renameTo(newFile);
		}

		//		If errors occured while reading
		else {
			//			Get the current business date & time
			String time = (SystemInformationManager.getInstance().getBFBusinessDateTimeAsString()).replace(':', '-')
					.replace(' ', '_');

			//			Append '_ERR_' to the file name
			String newFileName = "_" + fontisFile.getName() + "_ERR_" + time;

			File oldFile = fontisFile;

			File newFile = new File(oldFile.getParent(), newFileName);

			//			Rename the File
			oldFile.renameTo(newFile);
		}
		logger.debug("END renameProcessedFile() method");
	}

	/**
	 * This method reads the GENERALFONTIS table & fetches all the approved batches.
	 * Using each batch's batch Id, respective CREDITFONTIS & DEBITFONTIS details are populated for each batch.
	 * 
	 * @param env
	 * @return List of Approved Batches & its details
	 * @
	 */
	public List getApprovedBatches(BankFusionEnvironment env) {
		logger.info("Inside getApprovedBatches()...");
		ArrayList params = new ArrayList();
		ArrayList batchList = new ArrayList();
		params.add(new Integer(approvedBatchStatus));
		try {
			logger.info("General Fontis Read :: ");
			//			Read from GENERALFONTIS table
			ArrayList approvedBatchList = (ArrayList) env.getFactory().findByQuery(IBOGeneralFontis.BONAME,
					findByStatusFlag, params, null);
			Iterator generalIterator = approvedBatchList.iterator();

			while (generalIterator.hasNext()) {
				//				gettting general fontis data from db
				logger.info("General Fontis Read :: Iterator -> Object");
				IBOGeneralFontis generalFontis = (IBOGeneralFontis) generalIterator.next();

				//				getting debit record data
				ArrayList debitParams = new ArrayList();
				debitParams.add(generalFontis.getBoID());
				logger.info("Debit Fontis Read for Batch :: " + generalFontis.getBoID());
				ArrayList approvedDebitList = (ArrayList) env.getFactory().findByQuery(IBODebitFontis.BONAME,
						findDebitRecordByBatchNoAndStatus, debitParams, null);
				logger.info("Calling getApprovedDebitRecordList()");
				ArrayList debitRecordList = getApprovedDebitRecordList(approvedDebitList);

				//				getting credit record data
				ArrayList creditParams = new ArrayList();
				creditParams.add(generalFontis.getBoID());
				logger.info("Credit Fontis Read for Batch :: " + generalFontis.getBoID());
				ArrayList approvedCreditList = (ArrayList) env.getFactory().findByQuery(IBOCreditFontis.BONAME,
						findCreditRecordByBatchNoAndStatus, creditParams, null);
				logger.info("Calling getApprovedCreditRecordList()");
				ArrayList creditRecordList = getApprovedCreditRecordList(approvedCreditList);
				FON_BatchRecord approvedBatchRecord = new FON_BatchRecord();

				approvedBatchRecord.setFontisBatchId(generalFontis.getBoID());
				approvedBatchRecord.setAddress(generalFontis.getF_ADDRESS());
				approvedBatchRecord.setBankCharges(new String("'" + generalFontis.getF_BANK_CHARGES() + "'").charAt(0));
				approvedBatchRecord.setBankToBankInfo(generalFontis.getF_BANK_TO_BANK_INFO());
				approvedBatchRecord.setBatchNo(generalFontis.getF_BATCH_NO());
				approvedBatchRecord.setCreditTotal(generalFontis.getF_CREDIT_TOTALS());
				approvedBatchRecord.setDateCreated(generalFontis.getF_DT_CREATED().toString());
				approvedBatchRecord.setDateProcessed(generalFontis.getF_DT_PROCESSED().toString());
				approvedBatchRecord.setDebitTotal(generalFontis.getF_DEBIT_TOTALS());
				approvedBatchRecord.setFontisRecordType(generalFontis.getF_FONTIS_RECORD_TYPE());
				approvedBatchRecord.setForeignBankCharges(new String("'" + generalFontis.getF_FOR_BANK_CHARGES() + "'")
						.charAt(0));
				approvedBatchRecord.setGeneralComments(generalFontis.getF_GENERAL_COMMENTS());
				approvedBatchRecord.setInstructionNo(generalFontis.getF_INSTRUCTION_NO());
				approvedBatchRecord.setNarrative(generalFontis.getF_NARRATIVE());
				approvedBatchRecord.setNo_Of_Credits(generalFontis.getF_NUMBER_CREDITS());
				approvedBatchRecord.setNo_Of_Debits(generalFontis.getF_NUMBER_DEBITS());
				approvedBatchRecord.setNo_Of_Decs(generalFontis.getF_NUMBER_BASE_DECS());
				approvedBatchRecord.setOne_None_Both(generalFontis.getF_ONE_NONE_BOTH());
				approvedBatchRecord.setOne_Picked_Option(generalFontis.getF_ONE_PICKED_OPTION());
				approvedBatchRecord.setStatusFlag(generalFontis.getF_STATUS());
				approvedBatchRecord.setSwiftcharges(new String("'" + generalFontis.getF_SWIFT_CHARGES() + "'")
						.charAt(0));
				approvedBatchRecord.setText1(generalFontis.getF_TEXT_1());
				approvedBatchRecord.setText2(generalFontis.getF_TEXT_2());
				approvedBatchRecord.setUserID(generalFontis.getF_USER_ID());
				approvedBatchRecord.setUserName(generalFontis.getF_USER_NAME());
				approvedBatchRecord.setValueDate(generalFontis.getF_VALUE_DATE());

				approvedBatchRecord.setErrMessage(generalFontis.getF_ERR_MESSAGE());
				approvedBatchRecord.setMV_Failed(generalFontis.isF_MV_FAILED());
				if (generalFontis.getF_FORGN_CURR_BATCH().equalsIgnoreCase("Y"))
					approvedBatchRecord.setForgnCurrBatch(true);
				else
					approvedBatchRecord.setForgnCurrBatch(false);
				//				Required for tracking the transactions & also for reporting purposes, strictly in & after UB-3A release
				approvedBatchRecord.setTransactionReference(generalFontis.getF_TRANSACTION_REFERENCE());
				approvedBatchRecord.setCreditRecords(creditRecordList);
				approvedBatchRecord.setDebitRecords(debitRecordList);
				//adding to list
				batchList.add(approvedBatchRecord);
			}
			logger.info("Total Batches::" + batchList.size());
			logger.info("End getApprovedBatches()...");
		}
		catch (BankFusionException e) {
			/*throw new BankFusionException(9053, new Object[] { e.getLocalizedMessage() }, logger, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_BATCH_FILE_OR_FOLDER_IS_MAY_NOT_EXIST, new Object[] { e.getLocalisedMessage() }, new HashMap(), env);
		logger.error(e);
		}

		return batchList;
	}

	/**
	 * Store the FontisBatch Record values to database
	 * 
	 * @param FON_BatchRecord
	 * @param env
	 * @
	 * @
	 * @
	 * @
	 */
	public void storeFontisBatch(FON_BatchRecord fontisBatch, String branchSortCode, BankFusionEnvironment env) {
		/**
		 * BO Object for Fontis general record.
		 */
		IBOGeneralFontis generalRecordBO = null;

		/**
		 * BO Object for Fontis credit record..
		 */
		IBOCreditFontis creditRecordBO = null;

		/**
		 * BO Object for Fontis debit record.
		 */
		IBODebitFontis debitRecordBO = null;

		logger.info("Inside Store Fontis...");
		generalRecordBO = (IBOGeneralFontis) env.getFactory().getStatelessNewInstance(IBOGeneralFontis.BONAME);

		FON_BatchRecord fontisBatchRecord = fontisBatch;
		String fontisBatchType = fontisBatchRecord.getFontisRecordType();

		//		Fix for Bug # 4603 : Now Primary key value will have GUID instead of auto generated sequence number.
		String pkID = GUIDGen.getNewGUID();

		generalRecordBO.setBoID(pkID);
		generalRecordBO.setF_FONTIS_RECORD_TYPE(fontisBatchRecord.getFontisRecordType());
		generalRecordBO.setF_USER_ID(fontisBatchRecord.getUserID());
		generalRecordBO.setF_BATCH_NO(fontisBatchRecord.getBatchNo());
		generalRecordBO.setF_INSTRUCTION_NO(fontisBatchRecord.getInstructionNo());
		generalRecordBO.setF_USER_NAME(fontisBatchRecord.getUserName());
		generalRecordBO.setF_ADDRESS(fontisBatchRecord.getAddress());
		generalRecordBO.setF_DT_CREATED(getDateFromDateTimeString(fontisBatchRecord.getDateCreated()));
		generalRecordBO.setF_DT_PROCESSED(getDateFromDateTimeString(fontisBatchRecord.getDateProcessed()));
		generalRecordBO.setF_NUMBER_DEBITS(fontisBatchRecord.getNo_Of_Debits());
		generalRecordBO.setF_DEBIT_TOTALS(fontisBatchRecord.getDebitTotal());
		generalRecordBO.setF_NUMBER_CREDITS(fontisBatchRecord.getNo_Of_Credits());
		generalRecordBO.setF_CREDIT_TOTALS(fontisBatchRecord.getCreditTotal());
		if (fontisBatchType.equals(iatFileConstant)) {
			generalRecordBO.setF_GENERAL_COMMENTS(fontisBatchRecord.getGeneralComments());
			generalRecordBO.setF_NUMBER_BASE_DECS(fontisBatchRecord.getNo_Of_Decs());
		}
		if (fontisBatchType.equals(tppFileConstant)) {
			generalRecordBO.setF_NARRATIVE(fontisBatchRecord.getNarrative());
			generalRecordBO.setF_BANK_CHARGES(fontisBatchRecord.getBankCharges());
			generalRecordBO.setF_FOR_BANK_CHARGES(fontisBatchRecord.getForeignBankCharges());
			generalRecordBO.setF_SWIFT_CHARGES(fontisBatchRecord.getSwiftcharges());
			generalRecordBO.setF_BANK_TO_BANK_INFO(fontisBatchRecord.getBankToBankInfo());
		}
		generalRecordBO.setF_VALUE_DATE(fontisBatchRecord.getValueDate());
		generalRecordBO.setF_ONE_PICKED_OPTION(fontisBatchRecord.getOne_Picked_Option());
		generalRecordBO.setF_TEXT_1(fontisBatchRecord.getText1());
		generalRecordBO.setF_TEXT_2(fontisBatchRecord.getText2());
		generalRecordBO.setF_ONE_NONE_BOTH(fontisBatchRecord.getOne_None_Both());
		generalRecordBO.setF_STATUS(fontisBatchRecord.getStatusFlag());
		generalRecordBO.setF_ERR_MESSAGE(fontisBatchRecord.getErrMessage());
		if (fontisBatchRecord.isForgnCurrBatch())
			generalRecordBO.setF_FORGN_CURR_BATCH("1");
		else
			generalRecordBO.setF_FORGN_CURR_BATCH("0");
		generalRecordBO.setF_MV_FAILED(fontisBatchRecord.isMV_Failed());
		//		Required for tracking the transactions & also for reporting purposes, strictly in & after UB-3.3A release
		if (fontisBatchRecord.getTransactionReference() != null)
			generalRecordBO.setF_TRANSACTION_REFERENCE(fontisBatchRecord.getTransactionReference());
		//		Save the GENERALFONTIS record
		env.getFactory().create(IBOGeneralFontis.BONAME, generalRecordBO);

		//		Create DEBITFONTIS records
		ArrayList debitRecords = fontisBatchRecord.getDebitRecords();
		logger.info("Debit Records::" + debitRecords.size());
		for (int i = 0; i < debitRecords.size(); i++) {
			debitRecordBO = (IBODebitFontis) env.getFactory().getStatelessNewInstance(IBODebitFontis.BONAME);

			FON_DebitRecord debitRecord = (FON_DebitRecord) debitRecords.get(i);
			//			Fix for Bug # 4603 : Now Primary key value will have GUID instead of auto generated sequence number.
			debitRecordBO.setBoID(GUIDGen.getNewGUID());
			debitRecordBO.setF_FONTIS_BATCH_ID(pkID);
			debitRecordBO.setF_BATCH_NO(debitRecord.getBatchNo());
			debitRecordBO.setF_DEBIT_NO(debitRecord.getDebitNo());
			debitRecordBO.setF_DEBIT_SOURCE_CODE(debitRecord.getDebitSourceCode());
			debitRecordBO.setF_DEBIT_ACCOUNT_NUMBER(debitRecord.getDebitAccountNo());
			debitRecordBO.setF_DEBIT_BIC_CODE(debitRecord.getDebitBICCode());
			debitRecordBO.setF_DEBIT_ACCOUNT_NAME(debitRecord.getDebitAccountName());
			debitRecordBO.setF_DEBIT_DEFAULT_NAME(debitRecord.getDebitDefaultName());
			debitRecordBO.setF_DEBIT_ACCOUNT_TYPE(debitRecord.getDebitAccountType());
			debitRecordBO.setF_CURRECNY_CODE(debitRecord.getDebitCurrencyCode());
			if (fontisBatchType.equals(iatFileConstant)) {
				debitRecordBO.setF_POSITION_ACCOUNT(debitRecord.getDebitCurrencyPositionAccount());
				if (debitRecord.getDebitExchangeRate() != null) {
					debitRecordBO.setF_DEBIT_EXCHANGE_RATE(debitRecord.getDebitExchangeRate());
				}
				debitRecordBO.setF_MULTIPLY_DIVIDE_FLAG(Character.toString(debitRecord.getDebitMultiplyDevideFlag()));
				debitRecordBO.setF_TRANSACION_MULTIPLY_DIVIDE(Character.toString(debitRecord
						.getTransactionMultiplyDevideFlag()));
				debitRecordBO.setF_NUMBER_OF_DECIMAL(debitRecord.getNoOfDecimalsInTransactionAmount());
				if (debitRecord.getTransactionExchangeRate() != null) {
					debitRecordBO.setF_TRANSACTION_EXCHANGE_RATE(debitRecord.getTransactionExchangeRate());
				}
				debitRecordBO.setF_EQUA_NUMBER_OF_DECIMAL(debitRecord.getNoOfDecimalsInEquivalentAmount());
			}
			debitRecordBO.setF_DELEARS_NAME(debitRecord.getDebitDealersName());
			if (debitRecord.getDebitDealersRate() != null
					&& !debitRecord.getDebitDealersRate().equals(CommonConstants.EMPTY_STRING)) {
				debitRecordBO.setF_DELEARS_RATE(new BigDecimal(debitRecord.getDebitDealersRate()));
			}
			if (debitRecord.getDebitDateQuoted() != null
					&& !debitRecord.getDebitDateQuoted().equals(CommonConstants.EMPTY_STRING)) {
				debitRecordBO.setF_DATE_QUOTED(getDateFromDateTimeString(debitRecord.getDebitDateQuoted()));
			}
			else {
				debitRecordBO.setF_DATE_QUOTED(SystemInformationManager.getInstance().getBFSystemDate());
			}
			debitRecordBO.setF_TRANSACTION_CURRECNY_CODE(debitRecord.getTransactionCurrencyCode());
			debitRecordBO.setF_AMOUNT(debitRecord.getAmount());
			debitRecordBO.setF_EQUALIENT_AMOUNT(debitRecord.getEquivalentAmount());
			debitRecordBO.setF_AMOUNT_BASE_CURRENCY(debitRecord.getAmountInBaseCurrency());
			if (fontisBatchType.equals(tppFileConstant))
				debitRecordBO.setF_AMOUNT_USED(Character.toString(debitRecord.getAmountUsed()));
			debitRecordBO.setF_REFERENCE(debitRecord.getReference());
			debitRecordBO.setF_CODE(debitRecord.getCode());
			debitRecordBO.setF_PARTICULARS(debitRecord.getParticulars());
			debitRecordBO.setF_TRANSACTION_CODE(debitRecord.getTransactionCode());
			debitRecordBO.setF_STATUS(debitRecord.getStatusFlag());
			debitRecordBO.setF_ERR_MESSAGE(debitRecord.getErrMessage());
			try {
				env.getFactory().create(IBODebitFontis.BONAME, debitRecordBO);
			}
			catch (Exception e) {
				logger.info("Exception in storeFontis()");
				/*throw new BankFusionException(9010, new Object[] { e.getLocalizedMessage() }, logger, env);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_CANNOT_CREATE_DEBIT_RECORD_OF_FONTIS_TRANSACTION, new Object[] {  e.getLocalizedMessage() }, new HashMap(), env);
			logger.error(e);
			}
		}

		//		Create CREDITFONTIS records
		ArrayList creditRecords = fontisBatchRecord.getCreditRecords();
		logger.info("Credit Records::" + creditRecords.size());
		for (int i = 0; i < creditRecords.size(); i++) {
			creditRecordBO = (IBOCreditFontis) env.getFactory().getStatelessNewInstance(IBOCreditFontis.BONAME);

			FON_CreditRecord creditRecord = (FON_CreditRecord) creditRecords.get(i);
			//			Fix for Bug # 4603 : Now Primary key value will have GUID instead of auto generated sequence number.
			creditRecordBO.setBoID(GUIDGen.getNewGUID());
			creditRecordBO.setF_FONTIS_BATCH_ID(pkID);
			creditRecordBO.setF_BATCH_NO(creditRecord.getBatchNo());
			creditRecordBO.setF_CREDIT_NO(creditRecord.getCreditNo());
			if (fontisBatchType.equals(iatFileConstant)) {
				creditRecordBO.setF_CREDIT_SOURCE_CODE(creditRecord.getCreditSourceCode());
				creditRecordBO.setF_CREDIT_ACCOUNT_NUMBER(creditRecord.getCreditAccountNo());
				creditRecordBO.setF_CREDIT_BIC_CODE(creditRecord.getCreditBICCode());
				creditRecordBO.setF_CREDIT_ACCOUNT_NAME(creditRecord.getCreditAccountName());
				creditRecordBO.setF_CREDIT_DEFAULT_NAME(creditRecord.getCreditDefaultName());
				creditRecordBO.setF_CREDIT_ACCOUNT_TYPE(creditRecord.getCreditAccountType());
				creditRecordBO.setF_CURRECNY_CODE(creditRecord.getCreditCurrencyCode());
				creditRecordBO.setF_POSITION_ACCOUNT(creditRecord.getCreditCurrencyPositionAccount());
				if (creditRecord.getCreditExchangeRate() != null) {
					creditRecordBO.setF_CREDIT_EXCHANGE_RATE(creditRecord.getCreditExchangeRate());
				}
				creditRecordBO
						.setF_MULTIPLY_DIVIDE_FLAG(Character.toString(creditRecord.getCreditMultiplyDevideFlag()));
				creditRecordBO.setF_NUMBER_OF_DECIMAL(creditRecord.getNoOfDecimalsInTransactionAmount());
				if (creditRecord.getTransactionExchangeRate() != null) {
					creditRecordBO.setF_TRANSACTION_EXCHANGE_RATE(creditRecord.getTransactionExchangeRate());
				}
				creditRecordBO.setF_EQUA_NUMBER_OF_DECIMAL(creditRecord.getNoOfDecimalsInEquivalentAmount());
			}
			if (fontisBatchType.equals(tppFileConstant)) {
				creditRecordBO.setF_BENEFICIARY_REFERENCE_NO(creditRecord.getBeneficiaryRefNo());
				creditRecordBO.setF_BENEFICIARY_NAME(creditRecord.getBeneficiaryName());
				creditRecordBO.setF_BENEFICIARY_ADDRESS(creditRecord.getBeneficiaryAddress());
				creditRecordBO.setF_BENEFICIARY_BANK_CODE(creditRecord.getBeneficiaryBankCode());
				creditRecordBO.setF_BENEFICIARY_BANK_NAME(creditRecord.getBeneficiaryBankName());
				creditRecordBO.setF_BENEFICIARY_BANK_ADDRESS(creditRecord.getBeneficiaryBankAddress());
				creditRecordBO.setF_BENEFICIARY_ACCOUNT_CODE(creditRecord.getBeneficiaryAccountCode());
				creditRecordBO.setF_CREDIT_ACCOUNT_NUMBER(creditRecord.getBeneficiaryAccountCode());
				creditRecordBO.setF_BENEFICIARY_BANK_TYPE(creditRecord.getBeneficiaryBankType());
				creditRecordBO.setF_INTER_BANK_NAME(creditRecord.getIntermediaryBankName());
				creditRecordBO.setF_INTER_BANK_CITY(creditRecord.getIntermediaryBankCity());
				creditRecordBO.setF_INTER_ACCOUNT_NUMBER(creditRecord.getIntermediaryBankAccountNo());
				creditRecordBO.setF_INTER_BANK_CODE(creditRecord.getIntermediaryBankCode());
				creditRecordBO.setF_INTER_BANK_TYPE(creditRecord.getIntermediaryBankType());
			}
			creditRecordBO.setF_DELEARS_NAME(creditRecord.getCreditDealersName());
			if (creditRecord.getCreditDealersRate() != null
					&& !creditRecord.getCreditDealersRate().equals(CommonConstants.EMPTY_STRING)) {
				creditRecordBO.setF_DELEARS_RATE(new BigDecimal(creditRecord.getCreditDealersRate()));
			}

			if (creditRecord.getCreditDateQuoted() != null
					&& !creditRecord.getCreditDateQuoted().equals(CommonConstants.EMPTY_STRING)) {
				creditRecordBO.setF_DATE_QUOTED(getDateFromDateTimeString(creditRecord.getCreditDateQuoted()));
			}
			else {
				creditRecordBO.setF_DATE_QUOTED(SystemInformationManager.getInstance().getBFSystemDate());
			}
			creditRecordBO.setF_TRANSACTION_CURRECNY_CODE(creditRecord.getTransactionCurrencyCode());
			creditRecordBO.setF_TRANSACION_MULTIPLY_DIVIDE(Character.toString(creditRecord
					.getTransactionMultiplyDevideFlag()));
			creditRecordBO.setF_AMOUNT(creditRecord.getAmount());
			creditRecordBO.setF_EQUALIENT_AMOUNT(creditRecord.getEquivalentAmount());
			if (creditRecord.getAmountInBaseCurrency() != null) {
				creditRecordBO.setF_AMOUNT_BASE_CURRENCY(creditRecord.getAmountInBaseCurrency());
			}
			else {
				creditRecordBO.setF_AMOUNT_BASE_CURRENCY(new BigDecimal("0"));
			}
			creditRecordBO.setF_REFERENCE(creditRecord.getReference());
			creditRecordBO.setF_CODE(creditRecord.getCode());
			creditRecordBO.setF_PARTICULARS(creditRecord.getParticulars());
			creditRecordBO.setF_TRANSACTION_CODE(creditRecord.getTransactionCode());
			creditRecordBO.setF_STATUS(creditRecord.getStatusFlag());
			creditRecordBO.setF_ERR_MESSAGE(creditRecord.getErrMessage());
			//			Save Credit fontis record
			env.getFactory().create(IBOCreditFontis.BONAME, creditRecordBO);
		}
		logger.info("End Store Fontis...");
	}

	/**
	 * Reads a TPP file from given path and returns ArrayList of all batches
	 * from given TPP file.
	 * 
	 * @param filepath
	 * @param env
	 * @
	 * 
	 */
	private List tppFileReader(File fontisFile, BankFusionEnvironment env) {
		logger.debug("TPP File Path" + fontisFile.getAbsolutePath());
		ArrayList tppFontisBatchRecords = null;
		String tempString = CommonConstants.EMPTY_STRING;
		int batchCounter = 0;
		FileReader input = null;
		BufferedReader fileReader =null;
		try {
			input = new FileReader(fontisFile);
			fileReader = new BufferedReader(input);
			tppFontisBatchRecords = new ArrayList();

			while ((tempString = fileReader.readLine()) != null) {
				if (tempString.startsWith("G")) {
					batchCounter++;
					if (batchCounter > 1) {
						fontisBatchRecord.setCreditRecords(fontisCreditRecords);
						fontisBatchRecord.setDebitRecords(fontisDebitRecords);
						tppFontisBatchRecords.add(fontisBatchRecord);
						batchRecords.add(fontisBatchRecord);
						fontisBatchRecord = new FON_BatchRecord();
						fontisBatchRecord = getTPPBatchDetailsFromString(tempString, fontisBatchRecord);
						fontisBatchRecord.setFontisRecordType(FON_TransactionProcessingHelper.tppFileConstant);
						fontisDebitRecords = new ArrayList();
						fontisCreditRecords = new ArrayList();
					}
					else {
						fontisBatchRecord = new FON_BatchRecord();
						fontisBatchRecord = getTPPBatchDetailsFromString(tempString, fontisBatchRecord);
						fontisBatchRecord.setFontisRecordType(FON_TransactionProcessingHelper.tppFileConstant);
						fontisDebitRecords = new ArrayList();
						fontisCreditRecords = new ArrayList();
					}
				}
				else if (tempString.startsWith("D")) {
					FON_DebitRecord fontisDebitRecord = createTPPDebitRecordFromString(tempString);
					fontisDebitRecord.setBatchNo(fontisBatchRecord.getBatchNo());
					fontisDebitRecords.add(fontisDebitRecord);
				}
				else if (tempString.startsWith("C")) {
					FON_CreditRecord fontisCreditRecord = createTPPCreditRecordFromString(tempString);
					fontisCreditRecord.setBatchNo(fontisBatchRecord.getBatchNo());
					fontisCreditRecords.add(fontisCreditRecord);
				}
			}
			fontisBatchRecord.setCreditRecords(fontisCreditRecords);
			fontisBatchRecord.setDebitRecords(fontisDebitRecords);
			tppFontisBatchRecords.add(fontisBatchRecord);
			batchRecords.add(fontisBatchRecord);
			input.close();
		}
		catch (Exception e) {
			logger.error(e);
			try {
				if (input != null) {
					input.close();
				}
			}
			catch (IOException ioe) {
				/*throw new BankFusionException(127, new Object[] { ioe.getLocalizedMessage() }, logger, env);*/
				EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioe.getLocalizedMessage() }, new HashMap(), env);
			}
			renameProcessedFile(fontisFile, false);
			/*throw new BankFusionException(9052, new Object[] { e.getLocalizedMessage() }, logger, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_READING_TPP_BATCH_FILE, new Object[] {  e.getLocalizedMessage() }, new HashMap(), env);
		    logger.error(e);
		}
		finally {
			try {
				if (input != null) {
					input.close();
				}
			}
			catch (IOException ioe) {
				EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioe.getLocalizedMessage() }, new HashMap(), env);
				logger.error(ioe);
			}			

				try {
					if (fileReader != null) 
					fileReader.close();
				} catch (IOException e) {
					logger.error(e.getStackTrace());
				}
		}
		return tppFontisBatchRecords;
	}

	/**
	 * Reads a IAT file from given path returns ArrayList of all batches from
	 * given IAT file.
	 * 
	 * @param filepath
	 * @param env
	 * @
	 */
	private List iatFileReader(File fontisFile, BankFusionEnvironment env) {
		logger.debug("IAT File Path" + fontisFile.getAbsolutePath());
		String tempString = CommonConstants.EMPTY_STRING;
		int batchCounter = 0;
		int lineCounter = 0;
		ArrayList iatFontisBatchRecords = null;
		FileReader input = null;
		BufferedReader fileReader =null;
		try {
			input = new FileReader(fontisFile);
			fileReader = new BufferedReader(input);
			iatFontisBatchRecords = new ArrayList();

			while ((tempString = fileReader.readLine()) != null) {
				lineCounter++;
				if (lineCounter == 1 && !tempString.startsWith("G")) {
					logger.error("Problem while reading file : " + fontisFile.getAbsolutePath());
					logger.error("No header record in file" + lineCounter);
					/*throw new BankFusionException(9051,
							new Object[] { "Not a fontis file format..file reading failed" }, logger, env);*/
					EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_READING_IAT_BATCH_FILE, new Object[] {"Not a fontis file format..file reading failed"}, new HashMap(), env);
				}
				else {
					if (tempString.startsWith("G")) {
						batchCounter++;
						if (batchCounter > 1) {
							fontisBatchRecord.setCreditRecords(fontisCreditRecords);
							fontisBatchRecord.setDebitRecords(fontisDebitRecords);
							iatFontisBatchRecords.add(fontisBatchRecord);
							batchRecords.add(fontisBatchRecord);
							fontisBatchRecord = new FON_BatchRecord();
							fontisBatchRecord = getIATBatchDetailsFromString(tempString, fontisBatchRecord);
							fontisBatchRecord.setFontisRecordType(FON_TransactionProcessingHelper.iatFileConstant);
							fontisDebitRecords = new ArrayList();
							fontisCreditRecords = new ArrayList();
						}
						else {
							fontisBatchRecord = new FON_BatchRecord();
							fontisBatchRecord = getIATBatchDetailsFromString(tempString, fontisBatchRecord);
							fontisBatchRecord.setFontisRecordType(FON_TransactionProcessingHelper.iatFileConstant);
							fontisDebitRecords = new ArrayList();
							fontisCreditRecords = new ArrayList();
						}
					}
					else if (tempString.startsWith("D")) {
						FON_DebitRecord fontisDebitRecord = createIATDebitRecordFromString(tempString);
						fontisDebitRecord.setBatchNo(fontisBatchRecord.getBatchNo());
						fontisDebitRecords.add(fontisDebitRecord);
					}
					else if (tempString.startsWith("C")) {
						FON_CreditRecord fontisCreditRecord = createIATCreditRecordFromString(tempString);
						fontisCreditRecord.setBatchNo(fontisBatchRecord.getBatchNo());
						fontisCreditRecords.add(fontisCreditRecord);
					}
					else {
						logger.error("Problem while reading file : " + fontisFile.getAbsolutePath());
						logger.error("Read error at line number : " + lineCounter);
						/*throw new BankFusionException(9051,
								new Object[] { "Not a fontis file format..file reading failed" }, logger, env);*/
						EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_READING_IAT_BATCH_FILE, new Object[] {"Not a fontis file format..file reading failed"}, new HashMap(), env);
					}
				}
			}
			fontisBatchRecord.setCreditRecords(fontisCreditRecords);
			fontisBatchRecord.setDebitRecords(fontisDebitRecords);

			iatFontisBatchRecords.add(fontisBatchRecord);
			batchRecords.add(fontisBatchRecord);
			input.close();
		}
		catch (IOException e) {
			
			renameProcessedFile(fontisFile, false);
			logger.error("Problem while reading file : " + fontisFile.getAbsolutePath(), e);
			logger.error("Read error at line number : " + lineCounter);
			/*throw new BankFusionException(9051, new Object[] { e.getLocalizedMessage() }, logger, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_READING_IAT_BATCH_FILE, new Object[] { e.getLocalizedMessage()}, new HashMap(), env);
		}
		catch (Exception e) {
			
			renameProcessedFile(fontisFile, false);
			logger.error("Problem while reading file : " + fontisFile.getAbsolutePath(), e);
			logger.error("Read error at line number : " + lineCounter);
			/*throw new BankFusionException(9051, new Object[] { e.getLocalizedMessage() }, logger, env);*/
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] {e.getLocalizedMessage()}, new HashMap(), env);
		}
		finally {
			try {
				if (input != null) {
					input.close();
				}
			}
			catch (IOException ioe) {
				/*throw new BankFusionException(127, new Object[] { ioe.getLocalizedMessage() }, logger, env);*/
				logger.error(ioe);
			}
				try {
					if (fileReader != null) 
					fileReader.close();
				} catch (IOException e) {
					logger.error(e);
				}

		}

		return iatFontisBatchRecords;
	}

	/**
	 * Getiing batchrecord as a string from IAT file and set the general
	 * Instruction for FontisBatchRecord
	 * 
	 * @param inputstring
	 * @param fontisBatchRecord
	 * @
	 */
	private FON_BatchRecord getIATBatchDetailsFromString(String inputString, FON_BatchRecord fontisBatchRecord) {
		StringTokenizer tempTok = new StringTokenizer(inputString, fieldSaperatorDelim);

		// to skip Record type start
		tempTok.nextToken();
		// end
		fontisBatchRecord.setUserID(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getUserID());

		fontisBatchRecord.setBatchNo(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getBatchNo());

		fontisBatchRecord.setInstructionNo(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getInstructionNo());

		fontisBatchRecord.setUserName(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getUserName());

		fontisBatchRecord.setAddress(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getAddress());

		fontisBatchRecord.setDateCreated(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getDateCreated());

		fontisBatchRecord.setDateProcessed(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getDateProcessed());

		String No_Of_Debits = tempTok.nextToken().trim();
		if (No_Of_Debits != null && !EMPTY_STRING.equals(No_Of_Debits))
			fontisBatchRecord.setNo_Of_Debits(Integer.parseInt(No_Of_Debits));
		else
			fontisBatchRecord.setNo_Of_Debits(0);
		logger.debug(new Integer(fontisBatchRecord.getNo_Of_Debits()));

		String debitTotal = tempTok.nextToken().trim();
		if (debitTotal != null && !EMPTY_STRING.equals(debitTotal))
			fontisBatchRecord.setDebitTotal(new BigDecimal(debitTotal));
		else
			fontisBatchRecord.setDebitTotal(null);
		logger.debug(fontisBatchRecord.getDebitTotal());

		String No_Of_Credits = tempTok.nextToken().trim();
		if (No_Of_Credits != null && !EMPTY_STRING.equals(No_Of_Credits))
			fontisBatchRecord.setNo_Of_Credits(Integer.parseInt(No_Of_Credits));
		else
			fontisBatchRecord.setNo_Of_Credits(0);
		logger.debug(new Integer(fontisBatchRecord.getNo_Of_Credits()));

		String creditTotal = tempTok.nextToken().trim();
		if (creditTotal != null && !EMPTY_STRING.equals(creditTotal))
			fontisBatchRecord.setCreditTotal(new BigDecimal(creditTotal));
		else
			fontisBatchRecord.setCreditTotal(null);
		logger.debug(fontisBatchRecord.getCreditTotal());

		fontisBatchRecord.setGeneralComments(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getGeneralComments());
		String valueDate = tempTok.nextToken().trim();
		if (valueDate != null) {
			StringTokenizer st1 = new StringTokenizer(valueDate, "-");
			int year = Integer.parseInt(st1.nextToken().trim());
			int month = Integer.parseInt(st1.nextToken().trim());
			int date = Integer.parseInt(st1.nextToken().trim());
			Calendar cal = Calendar.getInstance();
			cal.setLenient(false);

			cal.set(year, month - 1, date, 0, 0, 0);
			fontisBatchRecord.setValueDate(new Date(cal.getTimeInMillis()));

		}
		logger.debug(fontisBatchRecord.getValueDate());

		String NoOfDecs = tempTok.nextToken().trim();
		if (NoOfDecs != null && !EMPTY_STRING.equals(NoOfDecs))
			fontisBatchRecord.setNo_Of_Decs(Integer.parseInt(NoOfDecs));
		logger.debug(new Integer(fontisBatchRecord.getNo_Of_Decs()));
		String onePickedOption = tempTok.nextToken().trim();
		if (onePickedOption != null && !EMPTY_STRING.equals(onePickedOption))
			fontisBatchRecord.setOne_Picked_Option(Integer.parseInt(onePickedOption));
		logger.debug(new Integer(fontisBatchRecord.getOne_Picked_Option()));
		fontisBatchRecord.setText1(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getText1());
		fontisBatchRecord.setText2(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getText2());
		String oneNoneBoth = tempTok.nextToken().trim();
		if (oneNoneBoth != null && !EMPTY_STRING.equals(oneNoneBoth))
			fontisBatchRecord.setOne_None_Both(Integer.parseInt(oneNoneBoth));
		logger.debug(new Integer(fontisBatchRecord.getOne_None_Both()));
		return fontisBatchRecord;
	}

	/**
	 * Getiing batchrecord as a string from TPP file and set the general
	 * Instruction for FontisBatchRecord
	 * 
	 * @param inputstring
	 * @param fontisBatchRecord
	 * @
	 */
	private FON_BatchRecord getTPPBatchDetailsFromString(String inputString, FON_BatchRecord fontisBatchRecord) {
		StringTokenizer tempTok = new StringTokenizer(inputString, fieldSaperatorDelim);

		//to skip record type 
		tempTok.nextToken();
		//end
		fontisBatchRecord.setUserID(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getUserID());
		fontisBatchRecord.setBatchNo(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getBatchNo());
		fontisBatchRecord.setInstructionNo(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getInstructionNo());
		fontisBatchRecord.setUserName(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getUserName());
		fontisBatchRecord.setAddress(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getAddress());
		fontisBatchRecord.setDateCreated(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getDateCreated());
		fontisBatchRecord.setDateProcessed(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getDateProcessed());
		String No_Of_Debits = tempTok.nextToken().trim();
		if (No_Of_Debits != null && !EMPTY_STRING.equals(No_Of_Debits))
			fontisBatchRecord.setNo_Of_Debits(Integer.parseInt(No_Of_Debits));
		else
			fontisBatchRecord.setNo_Of_Debits(0);
		logger.debug(new Integer(fontisBatchRecord.getNo_Of_Debits()));

		String debitTotal = tempTok.nextToken().trim();
		if (debitTotal != null && !EMPTY_STRING.equals(debitTotal))
			fontisBatchRecord.setDebitTotal(new BigDecimal(debitTotal));
		else
			fontisBatchRecord.setDebitTotal(null);
		logger.debug(fontisBatchRecord.getDebitTotal());

		String No_Of_Credits = tempTok.nextToken().trim();
		if (No_Of_Credits != null && !EMPTY_STRING.equals(No_Of_Credits))
			fontisBatchRecord.setNo_Of_Credits(Integer.parseInt(No_Of_Credits));
		else
			fontisBatchRecord.setNo_Of_Credits(0);
		logger.debug(new Integer(fontisBatchRecord.getNo_Of_Credits()));

		String creditTotal = tempTok.nextToken().trim();
		if (creditTotal != null && !EMPTY_STRING.equals(creditTotal))
			fontisBatchRecord.setCreditTotal(new BigDecimal(creditTotal));
		else
			fontisBatchRecord.setCreditTotal(null);
		logger.debug(fontisBatchRecord.getCreditTotal());
		fontisBatchRecord.setNarrative(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getNarrative());
		// setting up value date
		//		New code written below to handle possible exceptions
		/*StringTokenizer st1 = new StringTokenizer(tempTok.nextToken().trim(),
				"-");
		int year = Integer.parseInt(st1.nextToken().trim());
		int month = Integer.parseInt(st1.nextToken().trim());
		int date = Integer.parseInt(st1.nextToken().trim());
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, date);
		fontisBatchRecord.setValueDate(new Date(cal.getTimeInMillis()));*/

		String valueDate = tempTok.nextToken().trim();
		if (valueDate != null) {
			StringTokenizer st1 = new StringTokenizer(valueDate, "-");
			int year = Integer.parseInt(st1.nextToken().trim());
			int month = Integer.parseInt(st1.nextToken().trim());
			int date = Integer.parseInt(st1.nextToken().trim());
			Calendar cal = Calendar.getInstance();
			cal.setLenient(false);

			cal.set(year, month - 1, date, 0, 0, 0);
			fontisBatchRecord.setValueDate(new Date(cal.getTimeInMillis()));

		}

		logger.debug(fontisBatchRecord.getValueDate());
		fontisBatchRecord.setBankCharges(tempTok.nextToken().charAt(0));
		logger.debug(new Character(fontisBatchRecord.getBankCharges()));
		fontisBatchRecord.setForeignBankCharges(tempTok.nextToken().charAt(0));
		logger.debug(new Character(fontisBatchRecord.getForeignBankCharges()));
		fontisBatchRecord.setSwiftcharges(tempTok.nextToken().charAt(0));
		logger.debug(new Character(fontisBatchRecord.getSwiftcharges()));
		fontisBatchRecord.setBankToBankInfo(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getBankToBankInfo());
		String onePickedOption = tempTok.nextToken().trim();
		if (onePickedOption != null && !EMPTY_STRING.equals(onePickedOption))
			fontisBatchRecord.setOne_Picked_Option(Integer.parseInt(onePickedOption));
		logger.debug(new Integer(fontisBatchRecord.getOne_Picked_Option()));
		fontisBatchRecord.setText1(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getText1());
		fontisBatchRecord.setText2(tempTok.nextToken().trim());
		logger.debug(fontisBatchRecord.getText2());

		return fontisBatchRecord;
	}

	/**
	 * Getiing batchrecord as a string andfrom IAT file generates debitRecord
	 * 
	 * @param inputstring
	 * @param fontisBatchRecord
	 * @
	 */
	private FON_DebitRecord createIATDebitRecordFromString(String inputString) {
		StringTokenizer tempTok = new StringTokenizer(inputString, fieldSaperatorDelim);
		FON_DebitRecord dRecord = new FON_DebitRecord();
		// to skip record type 
		tempTok.nextToken();
		//end
		String debitNo = tempTok.nextToken().trim();
		if (debitNo != null && !debitNo.equals(EMPTY_STRING))
			dRecord.setDebitNo(Integer.parseInt(debitNo));
		logger.debug(new Integer(dRecord.getDebitNo()));
		//debit source code
		dRecord.setDebitSourceCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitSourceCode());
		//debit accno
		dRecord.setDebitAccountNo(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitAccountNo());
		//denit BIC code
		dRecord.setDebitBICCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitBICCode());
		//debit account name
		dRecord.setDebitAccountName(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitAccountName());
		//debit defult name
		dRecord.setDebitDefaultName(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDefaultName());
		//debit account type
		dRecord.setDebitAccountType(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitAccountType());
		//debit currency code
		dRecord.setDebitCurrencyCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitCurrencyCode());
		//debit curr pos account
		dRecord.setDebitCurrencyPositionAccount(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitCurrencyPositionAccount());
		//debit exchange rate
		String debExchangeRate = tempTok.nextToken().trim();
		if (debExchangeRate != null && !EMPTY_STRING.equals(debExchangeRate))
			dRecord.setDebitExchangeRate(new BigDecimal(debExchangeRate));
		logger.debug(dRecord.getDebitExchangeRate());
		//debit M/D flag
		dRecord.setDebitMultiplyDevideFlag(tempTok.nextToken().charAt(0));
		logger.debug(new Character(dRecord.getDebitMultiplyDevideFlag()));
		//debit dealers rate
		dRecord.setDebitDealersRate(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDealersRate());
		//debit dealers name
		dRecord.setDebitDealersName(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDealersName());
		//debit date quoted
		dRecord.setDebitDateQuoted(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDateQuoted());
		//transcurrency code
		dRecord.setTransactionCurrencyCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getTransactionCurrencyCode());
		// trans M/D Flag
		dRecord.setTransactionMultiplyDevideFlag(tempTok.nextToken().charAt(0));
		logger.debug(new Character(dRecord.getTransactionMultiplyDevideFlag()));
		//amount
		String amount = tempTok.nextToken().trim();
		if (amount != null && !amount.equals(EMPTY_STRING))
			dRecord.setAmount(new BigDecimal(amount));
		logger.debug(dRecord.getAmount());
		//no of decimals in trans amount
		String NoOfDecimalsInTransAmt = tempTok.nextToken().trim();
		if (NoOfDecimalsInTransAmt != null && !NoOfDecimalsInTransAmt.equals(EMPTY_STRING))
			dRecord.setNoOfDecimalsInTransactionAmount(Integer.parseInt(NoOfDecimalsInTransAmt));
		logger.debug(new Integer(dRecord.getNoOfDecimalsInTransactionAmount()));
		//trans exhange rate
		String transExchangeRate = tempTok.nextToken().trim();
		if (transExchangeRate != null && !EMPTY_STRING.equals(transExchangeRate))
			dRecord.setTransactionExchangeRate(new BigDecimal(transExchangeRate));
		logger.debug(dRecord.getTransactionExchangeRate());
		//Equiv amount
		String equivAmount = tempTok.nextToken().trim();
		if (equivAmount != null && !EMPTY_STRING.equals(equivAmount))
			dRecord.setEquivalentAmount(new BigDecimal(equivAmount));
		logger.debug(dRecord.getEquivalentAmount());
		//no of decimals in equiv amount
		String NoOFDecimalsInEquivAmt = tempTok.nextToken().trim();
		if (NoOFDecimalsInEquivAmt != null && !EMPTY_STRING.equals(NoOFDecimalsInEquivAmt))
			dRecord.setNoOfDecimalsInEquivalentAmount(Integer.parseInt(NoOFDecimalsInEquivAmt));
		logger.debug(new Integer(dRecord.getNoOfDecimalsInEquivalentAmount()));
		//amount in base currency
		String amtInBaseCurr = tempTok.nextToken().trim();
		if (amtInBaseCurr != null && !amtInBaseCurr.equals(EMPTY_STRING))
			dRecord.setAmountInBaseCurrency(new BigDecimal(amtInBaseCurr));
		logger.debug(dRecord.getAmountInBaseCurrency());

		dRecord.setReference(tempTok.nextToken().trim());
		logger.debug(dRecord.getReference());
		dRecord.setCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getCode());
		dRecord.setPerticulars(tempTok.nextToken().trim());
		logger.debug(dRecord.getParticulars());
		dRecord.setTransactionCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getTransactionCode());

		return dRecord;
	}

	/**
	 * Getiing batchrecord as a string andfrom TPP file generates debitRecord
	 * 
	 * @param inputstring
	 * @
	 */
	private FON_DebitRecord createTPPDebitRecordFromString(String inputString) {
		StringTokenizer tempTok = new StringTokenizer(inputString, fieldSaperatorDelim);
		FON_DebitRecord dRecord = new FON_DebitRecord();

		//to skip record type 
		tempTok.nextToken();
		//end
		String debitNo = tempTok.nextToken().trim();
		if (debitNo != null && !debitNo.equals(EMPTY_STRING))
			dRecord.setDebitNo(Integer.parseInt(debitNo));
		logger.debug(new Integer(dRecord.getDebitNo()));
		dRecord.setDebitSourceCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitSourceCode());
		dRecord.setDebitAccountNo(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitAccountNo());
		dRecord.setDebitBICCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitBICCode());
		dRecord.setDebitAccountName(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitAccountName());
		dRecord.setDebitDefaultName(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDefaultName());
		dRecord.setDebitAccountType(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitAccountType());
		dRecord.setDebitCurrencyCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitCurrencyCode());
		dRecord.setDebitDealersRate(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDealersRate());
		dRecord.setDebitDealersName(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDealersName());
		dRecord.setDebitDateQuoted(tempTok.nextToken().trim());
		logger.debug(dRecord.getDebitDateQuoted());
		dRecord.setTransactionCurrencyCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getTransactionCurrencyCode());

		String amount = tempTok.nextToken().trim();
		if (amount != null && !amount.equals(EMPTY_STRING))
			dRecord.setAmount(new BigDecimal(amount));
		logger.debug(dRecord.getAmount());
		String equivAmount = tempTok.nextToken().trim();
		if (equivAmount != null && !EMPTY_STRING.equals(equivAmount))
			dRecord.setEquivalentAmount(new BigDecimal(equivAmount));
		logger.debug(dRecord.getEquivalentAmount());
		String amtInBaseCurr = tempTok.nextToken().trim();
		if (amtInBaseCurr != null && !amtInBaseCurr.equals(EMPTY_STRING))
			dRecord.setAmountInBaseCurrency(new BigDecimal(amtInBaseCurr));
		logger.debug(dRecord.getAmountInBaseCurrency());
		dRecord.setAmountUsed(tempTok.nextToken().charAt(0));
		logger.debug(new Character(dRecord.getAmountUsed()));
		dRecord.setReference(tempTok.nextToken().trim());
		logger.debug(dRecord.getReference());
		dRecord.setCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getCode());
		dRecord.setPerticulars(tempTok.nextToken().trim());
		logger.debug(dRecord.getParticulars());
		dRecord.setTransactionCode(tempTok.nextToken().trim());
		logger.debug(dRecord.getTransactionCode());

		return dRecord;
	}

	/**
	 * Getiing batchrecord as a string andfrom IAT file generates creditRecord
	 * 
	 * @param inputstring
	 * @
	 */
	private FON_CreditRecord createIATCreditRecordFromString(String inputString) {
		StringTokenizer tempTok = new StringTokenizer(inputString, fieldSaperatorDelim);
		FON_CreditRecord cRecord = new FON_CreditRecord();

		// to skip record type 
		tempTok.nextToken();
		//end
		String creditNo = tempTok.nextToken().trim();
		if (creditNo != null && !EMPTY_STRING.equals(creditNo))
			cRecord.setCreditNo(Integer.parseInt(creditNo));
		logger.debug(new Integer(cRecord.getCreditNo()));
		cRecord.setCreditSourceCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditSourceCode());
		cRecord.setCreditAccountNo(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditAccountNo());
		cRecord.setCreditBICCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditBICCode());
		cRecord.setCreditAccountName(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditAccountName());
		cRecord.setCreditDefaultName(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditDefaultName());
		cRecord.setCreditAccountType(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditAccountType());
		cRecord.setCreditCurrencyCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditCurrencyCode());
		cRecord.setCreditCurrencyPositionAccount(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditCurrencyPositionAccount());
		String creditExchangeRate = tempTok.nextToken().trim();
		if (creditExchangeRate != null && !EMPTY_STRING.equals(creditExchangeRate))
			cRecord.setCreditExchangeRate(new BigDecimal(creditExchangeRate));
		logger.debug(cRecord.getCreditExchangeRate());
		cRecord.setCreditMultiplyDevideFlag(tempTok.nextToken().charAt(0));
		logger.debug(new Character(cRecord.getCreditMultiplyDevideFlag()));
		cRecord.setCreditDealersRate(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditDealersRate());
		cRecord.setCreditDealersName(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditDealersName());
		cRecord.setCreditDateQuoted(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditDateQuoted());
		cRecord.setTransactionCurrencyCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getTransactionCurrencyCode());
		cRecord.setTransactionMultiplyDevideFlag(tempTok.nextToken().charAt(0));
		logger.debug(new Character(cRecord.getTransactionMultiplyDevideFlag()));

		String amount = tempTok.nextToken().trim();
		if (amount != null && !EMPTY_STRING.equals(amount))
			cRecord.setAmount(new BigDecimal(amount));
		logger.debug(cRecord.getAmount());
		String noOfDecsInTransAmount = tempTok.nextToken().trim();
		if (noOfDecsInTransAmount != null && !EMPTY_STRING.equals(noOfDecsInTransAmount))
			cRecord.setNoOfDecimalsInTransactionAmount(Integer.parseInt(noOfDecsInTransAmount));
		logger.debug(new Integer(cRecord.getNoOfDecimalsInTransactionAmount()));
		String transExchangeRate = tempTok.nextToken().trim();
		if (transExchangeRate != null && !EMPTY_STRING.equals(transExchangeRate))
			cRecord.setTransactionExchangeRate(new BigDecimal(transExchangeRate));
		logger.debug(cRecord.getTransactionExchangeRate());
		String equivAmount = tempTok.nextToken().trim();
		if (equivAmount != null && !EMPTY_STRING.equals(equivAmount))
			cRecord.setEquivalentAmount(new BigDecimal(equivAmount));
		logger.debug(cRecord.getEquivalentAmount());
		String noOfDecsInEquvAmount = tempTok.nextToken().trim();
		if (noOfDecsInEquvAmount != null && !EMPTY_STRING.equals(noOfDecsInEquvAmount))
			cRecord.setNoOfDecimalsInEquivalentAmount(Integer.parseInt(noOfDecsInEquvAmount));
		logger.debug(new Integer(cRecord.getNoOfDecimalsInEquivalentAmount()));
		String amtInBaseCurr = tempTok.nextToken().trim();
		if (amtInBaseCurr != null && !EMPTY_STRING.equals(amtInBaseCurr))
			cRecord.setAmountInBaseCurrency(new BigDecimal(amtInBaseCurr));
		logger.debug(cRecord.getAmountInBaseCurrency());

		cRecord.setReference(tempTok.nextToken().trim());
		logger.debug(cRecord.getReference());
		cRecord.setCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getCode());
		cRecord.setParticulars(tempTok.nextToken().trim());
		logger.debug(cRecord.getParticulars());
		cRecord.setTransactionCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getTransactionCode());
		return cRecord;
	}

	/**
	 * Getiing batchrecord as a string andfrom TPP file generates creditRecord
	 * 
	 * @param inputstring
	 * @param fontisBatchRecord
	 * @
	 */
	private FON_CreditRecord createTPPCreditRecordFromString(String inputString) {
		StringTokenizer tempTok = new StringTokenizer(inputString, fieldSaperatorDelim);
		FON_CreditRecord cRecord = new FON_CreditRecord();

		// to skip record type 
		tempTok.nextToken();
		//end
		String creditNo = tempTok.nextToken().trim();
		if (creditNo != null && !EMPTY_STRING.equals(creditNo))
			cRecord.setCreditNo(Integer.parseInt(creditNo));
		logger.debug(new Integer(cRecord.getCreditNo()));

		cRecord.setBeneficiaryRefNo(tempTok.nextToken().trim());
		logger.debug(cRecord.getBeneficiaryRefNo());
		cRecord.setBeneficiaryName(tempTok.nextToken().trim());
		logger.debug(cRecord.getBeneficiaryName());
		cRecord.setBeneficiaryAddress(tempTok.nextToken().trim());
		logger.debug(cRecord.getBeneficiaryAddress());
		String benBankCode = tempTok.nextToken().trim();
		cRecord.setBeneficiaryBankCode(benBankCode);
		cRecord.setCreditBICCode(benBankCode);
		logger.debug(cRecord.getBeneficiaryBankCode());
		cRecord.setBeneficiaryBankName(tempTok.nextToken().trim());
		logger.debug(cRecord.getBeneficiaryBankName());
		cRecord.setBeneficiaryBankAddress(tempTok.nextToken().trim());
		logger.debug(cRecord.getBeneficiaryBankAddress());
		String benAccCode = tempTok.nextToken().trim();
		cRecord.setBeneficiaryAccountCode(benAccCode);
		cRecord.setCreditAccountNo(benAccCode);
		logger.debug(cRecord.getBeneficiaryAccountCode());
		cRecord.setBeneficiaryBankType(tempTok.nextToken().trim());
		logger.debug(cRecord.getBeneficiaryBankType());

		cRecord.setIntermediaryBankName(tempTok.nextToken().trim());
		logger.debug(cRecord.getIntermediaryBankName());
		cRecord.setIntermediaryBankCity(tempTok.nextToken().trim());
		logger.debug(cRecord.getIntermediaryBankCity());
		cRecord.setIntermediaryBankAccountNo(tempTok.nextToken().trim());
		logger.debug(cRecord.getIntermediaryBankAccountNo());
		cRecord.setIntermediaryBankCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getIntermediaryBankCode());
		cRecord.setIntermediaryBankType(tempTok.nextToken().trim());
		logger.debug(cRecord.getIntermediaryBankType());

		cRecord.setCreditDealersRate(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditDealersRate());
		cRecord.setCreditDealersName(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditDealersName());
		cRecord.setCreditDateQuoted(tempTok.nextToken().trim());
		logger.debug(cRecord.getCreditDateQuoted());
		cRecord.setTransactionCurrencyCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getTransactionCurrencyCode());

		String amount = tempTok.nextToken().trim();
		if (amount != null && !EMPTY_STRING.equals(amount))
			cRecord.setAmount(new BigDecimal(amount));
		logger.debug(cRecord.getAmount());
		/*String equivAmount=tempTok.nextToken().trim();
		if(equivAmount != null && ! EMPTY_STRING.equals(equivAmount))*/

		//		Storing 0 as this amount is not coming as part of TPP files Credit record
		cRecord.setEquivalentAmount(new BigDecimal(0));
		logger.debug(cRecord.getEquivalentAmount());

		/*if(cRecord.getAmount().compareTo(cRecord.getEquivalentAmount())==0)
		{
			cRecord.setAmountInBaseCurrency(new BigDecimal(amount));
			logger.debug(cRecord.getAmountInBaseCurrency());
		}*/
		/*if(cRecord.getAmount().compareTo(cRecord.getEquivalentAmount())!=0)
		{*/
		// amt in base curr cannot be retrived..it is not in the tpp file
		String amtInBaseCurr = tempTok.nextToken().trim();
		if (amtInBaseCurr != null && !EMPTY_STRING.equals(amtInBaseCurr))
			cRecord.setAmountInBaseCurrency(new BigDecimal(amtInBaseCurr));
		logger.debug(cRecord.getAmountInBaseCurrency());
		/*}*/
		cRecord.setReference(tempTok.nextToken().trim());
		logger.debug(cRecord.getReference());
		cRecord.setCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getCode());
		cRecord.setParticulars(tempTok.nextToken().trim());
		logger.debug(cRecord.getParticulars());
		cRecord.setTransactionCode(tempTok.nextToken().trim());
		logger.debug(cRecord.getTransactionCode());
		return cRecord;
	}

	/**
	 * Moves credit details obtained from database into FON_CreditRecord objects to be used in transaction processing
	 * @param approvedCreditList
	 * @return ArrayList of FON_CreditRecord
	 */
	private ArrayList getApprovedCreditRecordList(ArrayList approvedCreditList) {
		// gettting general fontis data from db
		ArrayList creditRecordList = new ArrayList();
		if (approvedCreditList != null && approvedCreditList.size() > 0) {
			Iterator creditIterator = approvedCreditList.iterator();
			while (creditIterator.hasNext()) {
				IBOCreditFontis creditFontis = (IBOCreditFontis) creditIterator.next();
				FON_CreditRecord creditRecord = new FON_CreditRecord();

				creditRecord.setAmount(creditFontis.getF_AMOUNT());
				creditRecord.setAmountInBaseCurrency(creditFontis.getF_AMOUNT_BASE_CURRENCY());
				creditRecord.setBatchNo(creditFontis.getF_BATCH_NO());
				creditRecord.setBeneficiaryAccountCode(creditFontis.getF_BENEFICIARY_ACCOUNT_CODE());
				creditRecord.setBeneficiaryAddress(creditFontis.getF_BENEFICIARY_ADDRESS());
				creditRecord.setBeneficiaryBankAddress(creditFontis.getF_BENEFICIARY_BANK_ADDRESS());
				creditRecord.setBeneficiaryBankCode(creditFontis.getF_BENEFICIARY_BANK_CODE());
				creditRecord.setBeneficiaryBankName(creditFontis.getF_BENEFICIARY_BANK_NAME());
				creditRecord.setBeneficiaryBankType(creditFontis.getF_BENEFICIARY_BANK_TYPE());
				creditRecord.setBeneficiaryName(creditFontis.getF_BENEFICIARY_NAME());
				creditRecord.setBeneficiaryRefNo(creditFontis.getF_BENEFICIARY_REFERENCE_NO());
				creditRecord.setCode(creditFontis.getF_CODE());
				creditRecord.setCreditAccountName(creditFontis.getF_CREDIT_ACCOUNT_NAME());
				creditRecord.setCreditAccountNo(creditFontis.getF_CREDIT_ACCOUNT_NUMBER());
				creditRecord.setCreditAccountType(creditFontis.getF_CREDIT_ACCOUNT_TYPE());
				creditRecord.setCreditBICCode(creditFontis.getF_CREDIT_BIC_CODE());
				creditRecord.setCreditCurrencyCode(creditFontis.getF_CURRECNY_CODE());
				creditRecord.setCreditCurrencyPositionAccount(creditFontis.getF_POSITION_ACCOUNT());
				creditRecord.setCreditDateQuoted(creditFontis.getF_DATE_QUOTED().toString());
				creditRecord.setCreditDealersName(creditFontis.getF_DELEARS_NAME());
				creditRecord.setCreditDealersRate(new String(CommonConstants.EMPTY_STRING
						+ creditFontis.getF_DELEARS_RATE().doubleValue() + CommonConstants.EMPTY_STRING));
				creditRecord.setCreditDefaultName(creditFontis.getF_CREDIT_DEFAULT_NAME());
				creditRecord.setCreditExchangeRate(creditFontis.getF_CREDIT_EXCHANGE_RATE());
				if (creditFontis.getF_MULTIPLY_DIVIDE_FLAG() != null
						&& !creditFontis.getF_MULTIPLY_DIVIDE_FLAG().equalsIgnoreCase(CommonConstants.EMPTY_STRING))
					creditRecord.setCreditMultiplyDevideFlag(creditFontis.getF_MULTIPLY_DIVIDE_FLAG().charAt(0));
				creditRecord.setCreditNo(creditFontis.getF_CREDIT_NO());
				creditRecord.setCreditSourceCode(creditFontis.getF_CREDIT_SOURCE_CODE());
				creditRecord.setEquivalentAmount(creditFontis.getF_EQUALIENT_AMOUNT());
				creditRecord.setIntermediaryBankAccountNo(creditFontis.getF_INTER_ACCOUNT_NUMBER());
				creditRecord.setIntermediaryBankCity(creditFontis.getF_INTER_BANK_CITY());
				creditRecord.setIntermediaryBankCode(creditFontis.getF_INTER_BANK_CODE());
				creditRecord.setIntermediaryBankName(creditFontis.getF_INTER_BANK_NAME());
				creditRecord.setIntermediaryBankType(creditFontis.getF_INTER_BANK_TYPE());
				creditRecord.setNoOfDecimalsInEquivalentAmount(creditFontis.getF_EQUA_NUMBER_OF_DECIMAL());
				creditRecord.setNoOfDecimalsInTransactionAmount(creditFontis.getF_NUMBER_OF_DECIMAL());
				creditRecord.setParticulars(creditFontis.getF_PARTICULARS());
				creditRecord.setReference(creditFontis.getF_REFERENCE());
				creditRecord.setStatusFlag(creditFontis.getF_STATUS());
				creditRecord.setTransactionCode(creditFontis.getF_TRANSACTION_CODE());
				creditRecord.setTransactionCurrencyCode(creditFontis.getF_TRANSACTION_CURRECNY_CODE());
				creditRecord.setTransactionExchangeRate(creditFontis.getF_TRANSACTION_EXCHANGE_RATE());
				creditRecord.setTransactionMultiplyDevideFlag(creditFontis.getF_TRANSACION_MULTIPLY_DIVIDE().charAt(0));
				creditRecord.setErrMessage(creditFontis.getF_ERR_MESSAGE());
				creditRecordList.add(creditRecord);
			}
		}
		return creditRecordList;
	}

	/**
	 * Moves credit details obtained from database into FON_CreditRecord objects to be used in transaction processing
	 * @param approvedDebitList
	 * @return ArrayList of FON_CreditRecord
	 */
	private ArrayList getApprovedDebitRecordList(ArrayList approvedDebitList) {
		ArrayList debitRecordList = new ArrayList();
		if (approvedDebitList != null && approvedDebitList.size() > 0) {
			Iterator debitIterator = approvedDebitList.iterator();
			while (debitIterator.hasNext()) {
				IBODebitFontis debitFontis = (IBODebitFontis) debitIterator.next();
				FON_DebitRecord debitRecord = new FON_DebitRecord();
				debitRecord.setAmount(debitFontis.getF_AMOUNT());
				debitRecord.setAmountInBaseCurrency(debitFontis.getF_AMOUNT_BASE_CURRENCY());
				if (debitFontis.getF_AMOUNT_USED() != null && !EMPTY_STRING.equals(debitFontis.getF_AMOUNT_USED()))
					debitRecord.setAmountUsed(debitFontis.getF_AMOUNT_USED().charAt(0));
				debitRecord.setBatchNo(debitFontis.getF_BATCH_NO());
				debitRecord.setCode(debitFontis.getF_CODE());
				debitRecord.setDebitAccountName(debitFontis.getF_DEBIT_ACCOUNT_NAME());
				debitRecord.setDebitAccountNo(debitFontis.getF_DEBIT_ACCOUNT_NUMBER());
				debitRecord.setDebitAccountType(debitFontis.getF_DEBIT_ACCOUNT_TYPE());
				debitRecord.setDebitBICCode(debitFontis.getF_DEBIT_BIC_CODE());
				debitRecord.setDebitCurrencyCode(debitFontis.getF_CURRECNY_CODE());
				debitRecord.setDebitCurrencyPositionAccount(debitFontis.getF_POSITION_ACCOUNT());
				debitRecord.setDebitDateQuoted(debitFontis.getF_DATE_QUOTED().toString());
				debitRecord.setDebitDealersName(debitFontis.getF_DELEARS_NAME());
				debitRecord.setDebitDealersRate(new String(CommonConstants.EMPTY_STRING
						+ debitFontis.getF_DELEARS_RATE().doubleValue() + CommonConstants.EMPTY_STRING));
				debitRecord.setDebitDefaultName(debitFontis.getF_DEBIT_DEFAULT_NAME());
				debitRecord.setDebitExchangeRate(debitFontis.getF_DEBIT_EXCHANGE_RATE());
				if (debitFontis.getF_MULTIPLY_DIVIDE_FLAG() != null
						&& !EMPTY_STRING.equals(debitFontis.getF_MULTIPLY_DIVIDE_FLAG()))
					debitRecord.setDebitMultiplyDevideFlag(debitFontis.getF_MULTIPLY_DIVIDE_FLAG().charAt(0));
				debitRecord.setDebitNo(debitFontis.getF_DEBIT_NO());
				debitRecord.setDebitSourceCode(debitFontis.getF_DEBIT_SOURCE_CODE());
				debitRecord.setEquivalentAmount(debitFontis.getF_EQUALIENT_AMOUNT());
				debitRecord.setNoOfDecimalsInEquivalentAmount(debitFontis.getF_EQUA_NUMBER_OF_DECIMAL());
				debitRecord.setNoOfDecimalsInTransactionAmount(debitFontis.getF_NUMBER_OF_DECIMAL());
				debitRecord.setPerticulars(debitFontis.getF_PARTICULARS());
				debitRecord.setReference(debitFontis.getF_REFERENCE());
				debitRecord.setStatusFlag(debitFontis.getF_STATUS());
				debitRecord.setTransactionCode(debitFontis.getF_TRANSACTION_CODE());
				debitRecord.setTransactionCurrencyCode(debitFontis.getF_TRANSACTION_CURRECNY_CODE());
				debitRecord.setTransactionExchangeRate(debitFontis.getF_TRANSACTION_EXCHANGE_RATE());
				if (debitFontis.getF_TRANSACION_MULTIPLY_DIVIDE() != null
						&& !EMPTY_STRING.equals(debitFontis.getF_TRANSACION_MULTIPLY_DIVIDE()))
					debitRecord.setTransactionMultiplyDevideFlag(debitFontis.getF_TRANSACION_MULTIPLY_DIVIDE()
							.charAt(0));
				debitRecord.setErrMessage(debitFontis.getF_ERR_MESSAGE());
				debitRecordList.add(debitRecord);
			}
		}
		return debitRecordList;
	}

	/**
	 * Converts string to Date field
	 * @param dateTime
	 * @return Date
	 */
	public Date getDateFromDateTimeString(String dateTime) {
		Date date = null;

		if (dateTime != null && !dateTime.equals(CommonConstants.EMPTY_STRING)) {
			StringTokenizer dateTimeTok = new StringTokenizer(dateTime);
			String dateString = dateTimeTok.nextToken();
			String timeString = null;
			if (dateTimeTok.countTokens() == 2)
				timeString = dateTimeTok.nextToken();
			StringTokenizer dateTok = new StringTokenizer(dateString, "-");
			int year = Integer.parseInt(dateTok.nextToken());
			int mon = Integer.parseInt(dateTok.nextToken());
			int dat = Integer.parseInt(dateTok.nextToken());
			int hh = 0;
			int mm = 0;
			int sec = 0;
			if (timeString != null) {
				StringTokenizer timeTok = new StringTokenizer(timeString, ":");
				hh = Integer.parseInt(timeTok.nextToken());
				mm = Integer.parseInt(timeTok.nextToken());
				sec = Integer.parseInt(timeTok.nextToken());
			}
			Calendar cal = Calendar.getInstance();
			cal.set(year, mon - 1, dat, hh, mm, sec);
			date = new Date(cal.getTimeInMillis());
		}

		return date;
	}
}
