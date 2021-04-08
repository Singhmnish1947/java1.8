/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_TransactionProcessing.java,v 1.28.4.12 2008/10/08 07:35:38 christopherj Exp $
 * **********************************************************************************
 * 
 * Revision 1.14  2008/02/16 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 */

package com.trapedza.bankfusion.fatoms;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.common.StringHelper;
import com.misys.ub.fontis.FON_BatchRecord;
import com.misys.ub.fontis.FON_CreditRecord;
import com.misys.ub.fontis.FON_DebitRecord;
import com.misys.ub.forex.configuration.ForexModuleConfiguration;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOFontisConfig;
import com.trapedza.bankfusion.bo.refimpl.IBOGeneralFontis;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.UB_ICS_SettleICTransaction;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingRouter;
import com.trapedza.bankfusion.postingengine.router.PostingResponse;
import com.trapedza.bankfusion.postingengine.services.IPostingEngine;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AddDaysToDate;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.IsWorkingDay;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.NextWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.utils.FatomUtils;
import com.trapedza.bankfusion.steps.refimpl.AbstractFON_TransactionProcessing;
import com.trapedza.bankfusion.steps.refimpl.IFON_TransactionProcessing;
import com.trapedza.bankfusion.systeminformation.PostingMessageConstants;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * This fatom is responsible for reading Fontis files from given location and creating the
 * BatchRecords for the given file. This also validated the batch and store the data into database.
 * 
 * @author hardikp
 * 
 */
public class FON_TransactionProcessing extends AbstractFON_TransactionProcessing implements IFON_TransactionProcessing {
    /**
     * Logger instance
     */
    private transient final static Log logger = LogFactory.getLog(FON_TransactionProcessing.class.getName());

    /**
     * constant for TPP file type
     */
    private static final String tppFileConstant = "TPP";

    /**
     * constant for IAT file type
     */
    private static final String iatFileConstant = "IAT";

    /**
     * constant for this bank's BIC code
     */
    public static String systemBICCode;

    /**
     * where clause to fetch system BIC code
     */
    String whereClauseSystemBIC = "where " + IBOBranch.BRANCHSORTCODE + " = ?";

    /**
     * Fontis batch status flag value on failure
     */
    private static final int failedBatchStatus = 1;

    /**
     * Fontis batch status flag value on success
     */
    private static final int postedBatchStatus = 4;

    /**
     * ArrayList which contains all IAT/TPP file names from the input directory
     */
    ArrayList listBatchFileNames = new ArrayList();

    /**
     * This HashMap contains psuedonames as key and accountNo as values
     */
    // private static HashMap psuedoAccMap = new HashMap();
    /**
     * FontisConfig BO object
     */
    protected static IBOFontisConfig fontisConfig;

    /**
     * Location of Fontis files [TPP /IAT] directory
     */
    private static String fontisDirLocation;

    /**
     * Transaction Reference for Transaction Processing
     */
    private int transactionReference;

    /**
     * Number of successfully posted batches
     */
    private int noOfPostedBatches;

    /**
     * Number of failed batches
     */
    private int noOfFailedBatches;

    /**
     * Credit total of all the records written into EFT File
     */
    private BigDecimal eftCreditTotal;

    /**
     * Number of credit records written into EFT file
     */
    private int eftCreditCount;

    /**
     * Number of debit records written into EFT file
     */
    private int eftDebitCount;

    /**
     * Date formatter
     */
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

    /**
     * Posting details
     */

    private String txnID;

    private int count;
    String accountID;
    private boolean isForwardDated;

    private ArrayList tempPostingMsg;

    private HashMap accountFromPseudoname = new HashMap();
    // This is vara's Latest modifications for pseudonym

    private static final String CONTEXT_NAME = "CURRENCY";
    // private static final String
    // CONTEXT_VALUE=SystemInformationManager.getInstance().getBaseCurrencyCode();

    private static final String POSTING_MESSAGES = "POSTING_MESSAGES";
    private static final String NO_OF_CREDIT_RECORDS = "NO_OF_CREDIT_RECORDS";
    private static final String NO_OF_DEBIT_RECORDS = "NO_OF_DEBIT_RECORDS";
    private static final String NO_OF_DEBITS_FROM_POS_ACC = "NO_OF_DEBITS_FROM_POS_ACC";
    private static final String NO_OF_CREDITS_TO_POS_ACC = "NO_OF_CREDITS_FROM_POS_ACC";

    /**
     * Batch List read from a fontis file(IAT/TPP)
     */
    ArrayList batchRecords = new ArrayList();

    /**
     * query string to populate accounts based on pseudoname & branch sort code
     */
    // String whereClausePsuedo = "where " + IBOAccount.BRANCHSORTCODE +
    // " = ? AND (" + IBOAccount.PSEUDONAME
    // + " LIKE ? OR " + IBOAccount.PSEUDONAME + " LIKE ?)";
    /**
     * EFT file path including the filename.
     */
    String eftFilePath;

    /**
     * HashMap containing net currency total to be posted against position accounts. Key - Currency
     * Code. Value - Net amount to be posted.
     */
    HashMap CurrencyPositionTotals = new HashMap();

    /**
     * Constructor
     * 
     * @param env
     */
    public FON_TransactionProcessing(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * Initialize the values
     * 
     * @param
     * @throws
     */
    private void init() {
        // Get input directory path
        fontisDirLocation = this.getF_IN_FontisFileDirLocation();
        // Initialize EFT Credit total to 0.00
        eftCreditTotal = new BigDecimal(BigInteger.ZERO, SystemInformationManager.getInstance().getCurrencyScale(
                SystemInformationManager.getInstance().getBaseCurrencyCode()));
    }

    /**
     * implements process(...) method in AbstractFON_TransactionProcessing
     */

    /*
     * private boolean isICSRequired(FON_BatchRecord fontisBatch) {
     * 
     * ArrayList creditRecords = fontisBatch.getCreditRecords();
     * 
     * for(int i=0 ; i < creditRecords.size(); i++) { if(((FON_CreditRecord)
     * creditRecords.get(i)).getCreditBICCode().equalsIgnoreCase(systemBICCode)) return true;
     * 
     * }
     * 
     * return false;
     * 
     * }
     */

    public void process(BankFusionEnvironment env) {
        // Call init() method
        init();

        // Loading fontis configuration. Status '0' record is being read since
        // Notification & Authorization (N&A)
        // is not yet being used in configuration. Once N&A is implemented
        // Status '1' record should be read.
        String whereClause = "WHERE " + IBOFontisConfig.STATUS + " = 0";

        // Fetch the fontis configuration records whose status is 0
        List fontisConfigList = env.getFactory().findByQuery(IBOFontisConfig.BONAME, whereClause, null);

        // If configuration record is not present then through error
        if (fontisConfigList == null || fontisConfigList.size() <= 0) {
            /* throw new BankFusionException(9009, null, logger, env); */
            EventsHelper.handleEvent(ChannelsEventCodes.E_FONTIS_CONFIGURATION_DOES_NOT_EXIST, new Object[] {}, new HashMap(), env);
        }
        else {
            fontisConfig = (IBOFontisConfig) fontisConfigList.get(0);
        }

        // Cache the values required for transaction processing into appropriate
        // data structures
        // logger.info("INSUFFICIENT SUSPENSE ACCOUNT: "+this.populateAccountsFromPseudoName(CONTEXT_NAME,
        // CONTEXT_VALUE, CURRENCY, fontisConfig.getF_SUSP_ACC_PSEUDONYM(),
        // env));//Populate Insuuficient Suspense Account
        // logger.info("CURRENCY SUSPENSE ACCOUNT: "+
        // this.populateAccountsFromPseudoName(CONTEXT_NAME,CONTEXT_VALUE,
        // CURRENCY,fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM(),env)); //
        // Populate accounts : fontis Currency Suspense Account
        // logger.info("EFT ACCOUNT: "+this.populateAccountsFromPseudoName(CONTEXT_NAME,
        // CONTEXT_VALUE, CURRENCY, fontisConfig.getF_EFT_ACC_PSEUDONYM(),
        // env));//Populate EFT Account
        this.populateSystemBICCode(env); // Populate this bank's BIC Code
        FON_ValidationHelper.populateBICCode(env); // Populate all bank's BIC
        // Code listed in UB
        FON_ValidationHelper.populateCurrencyCode(env); // Populate all the
        // currencies supported
        // in UB
        FON_ValidationHelper.populateFontisProducts(); // Populate all the
        // fontis configured
        // sub-products
        FON_ValidationHelper.accountNotOrToBeReactivated(env); // Populate
        // transaction
        // type dormancy
        // code &
        // numeric code

        // gettting transaction referreence from fontisCOnfig
        transactionReference = new Integer(fontisConfig.getF_TRANSACTION_REFERENCE().substring(2)).intValue();

        FON_TransactionProcessingHelper helper = new FON_TransactionProcessingHelper();

        // Fetch all approved records from database
        try {
            ArrayList approvedBatchList = (ArrayList) helper.getApprovedBatches(env);
            // Posting approved transactions bypassing most of the validations
            postApprovedBatches(approvedBatchList, env);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while posting approved batches..", bfe);
        }

        // accessing derectory where all Fontis file are stored
        File fontisDir = new File(this.getF_IN_FontisFileDirLocation());

        String[] fontisFileList = fontisDir.list();

        // If No files for reading
        if (fontisFileList == null || fontisFileList.length <= 0) {
            /*
             * throw new BankFusionException(9056, new Object[] { "Fontis Dir is Empty" }, logger,
             * env);
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_FONTIS_FILE_UNAVAILABLE_FOR_READING,
                    new Object[] { "Fontis Dir is Empty" }, new HashMap(), env);
        }
        else {
            // Iterate through all the files in the fontis input directory
            for (int i = 0; i < fontisFileList.length; i++) {
                String fileName = fontisFileList[i];
                logger.info("Reading Fontis file : " + fileName);
                // reading fontis file
                ArrayList batchRecordList = null;

                try {
                    // Read file & store all the batches into an array list
                    batchRecordList = (ArrayList) helper.readFontisFile(new File(fontisDirLocation, fileName), env);
                }
                catch (BankFusionException bfe) {
                    logger.error(bfe.getMessage(), bfe);
                    continue;
                }

                if (batchRecordList != null) {
                    Iterator batchIterator = batchRecordList.iterator();
                    // Validate each batch record read from the file
                    while (batchIterator.hasNext()) {
                        FON_BatchRecord fontisBatch = (FON_BatchRecord) batchIterator.next();

                        // env.getFactory().beginTransaction();
                        // validate fontis batch
                        logger.info("Transaction started for Batch : " + batchIterator.hashCode());

                        // Validate the batch
                        FON_ValidationHelper validator = new FON_ValidationHelper();
                        boolean isValidBatch = validator.validateFontisBatch(fontisBatch, env);

                        // If TPP file
                        if (isValidBatch && fontisBatch.getFontisRecordType().equals(tppFileConstant)) {
                            // checking for on-us / off-us transaction and
                            // setting EFT/SWIFT message flag
                            checkOnUsTransaction(fontisBatch);
                        }

                        // If a valid Batch
                        if (isValidBatch) {
                            // bug fix # 3936 start
                            // added batchwise direct posting functionality
                            boolean postingStatus;
                            try {
                                // Post the transaction & get the posting staus
                                postingStatus = postTransaction(fontisBatch, false, env);

                                // If posting successful
                                if (postingStatus) {
                                    // Increment the posted batch counter &
                                    // commit the transaction

                                    // if (isICSRequired(fontisBatch)) {
                                    UB_ICS_SettleICTransaction ICTransaction = new UB_ICS_SettleICTransaction();
                                    ICTransaction.settleICTransactions(tempPostingMsg, env);
                                    // }
                                    noOfPostedBatches++;
                                    // env.getFactory().commitTransaction();
                                    // env.getFactory().beginTransaction();
                                }
                                // If posting failed
                                else {
                                    // Increment the failed batch counter &
                                    // roll-back the transaction
                                    noOfFailedBatches++;
                                    // env.getFactory().rollbackTransaction();
                                    // env.getFactory().beginTransaction();
                                }
                            }
                            catch (BankFusionException bfe) {
                                logger.error("Error while posting fontis batch..", bfe);
                            }
                            catch (Exception ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                        }
                        // If any of the validation on the batch has failed
                        else {
                            // Increment the failed batch counter
                            noOfFailedBatches++;
                        }

                        try {
                            // Store the batch details into database
                            helper.storeFontisBatch(fontisBatch, this.getF_IN_BranchSortCode(), env);
                        }
                        catch (BankFusionException bfe) {
                            logger.error(bfe.getLocalisedMessage(), bfe);
                        }
                        catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                        // commit the transaction
                        // env.getFactory().commitTransaction();
                        logger.info("Transaction committed for Batch : " + batchIterator.hashCode());
                    }

                    // Rename the file once all batches in the file are
                    // processed
                    helper.renameProcessedFile(new File(fontisDirLocation, fileName), true);
                }
            }
        }

        // If EFT file has been generated during this run
        if (eftCreditCount > 0) {
            // Write Trailer record into EFT File.
            generateEFTTrailer(env);
            // If atleast 1 record is written into EFT file, then increment the
            // EFTFileId value in fontis configuration.
            // This should be done only once in a transaction processing run.
            updateEFTFileIdInFontisConfig(fontisConfig.getF_EFTFILEID(), env);
        }

        // Set the output details to be presented on to the screen
        this.setF_OUT_failedBatches(new Integer(noOfFailedBatches));
        this.setF_OUT_postedBatches(new Integer(noOfPostedBatches));

        // Required since the next step in the BFG, will throw a transaction
        // commit error if not present
        // env.getFactory().beginTransaction();
    }

    /**
     * Checks whether a batch consists of only Base currency transactions or foreign currency
     * transactions also. If found then sets the ForgnCurrBatch flag value to TRUE. Called only
     * while processing authorized transaction.
     */
    private void checkMultiCurrBatch(FON_BatchRecord fontisBatch) {
        for (int i = 0; i < fontisBatch.getDebitRecords().size(); i++) {
            FON_DebitRecord fontisDebitRecord = (FON_DebitRecord) fontisBatch.getDebitRecords().get(i);

            if (fontisBatch.getFontisRecordType().equals(iatFileConstant)) {
                if (!fontisDebitRecord.getDebitCurrencyCode().equals(SystemInformationManager.getInstance().getBaseCurrencyCode()))
                    fontisBatch.setForgnCurrBatch(true);
            }
            else if (fontisBatch.getFontisRecordType().equals(tppFileConstant)
                    && !fontisDebitRecord.getDebitCurrencyCode().equals(
                            SystemInformationManager.getInstance().getBaseCurrencyCode())) {
                fontisBatch.setForgnCurrBatch(true);
            }
        }
        for (int i = 0; i < fontisBatch.getCreditRecords().size(); i++) {
            FON_CreditRecord fontisCreditRecord = (FON_CreditRecord) fontisBatch.getCreditRecords().get(i);

            if (fontisBatch.getFontisRecordType().equals(iatFileConstant)) {
                if (!fontisCreditRecord.getCreditCurrencyCode()
                        .equals(SystemInformationManager.getInstance().getBaseCurrencyCode()))
                    fontisBatch.setForgnCurrBatch(true);
            }
            else if (fontisBatch.getFontisRecordType().equals(tppFileConstant)
                    && !fontisCreditRecord.getTransactionCurrencyCode().equals(
                            SystemInformationManager.getInstance().getBaseCurrencyCode())) {
                fontisBatch.setForgnCurrBatch(true);
            }
        }
    }

    /**
     * Post the approved batches bypassing most of the validations
     * 
     * @param approvedBatchList
     * @param helper
     * @param env
     *            @
     */
    private void postApprovedBatches(ArrayList approvedBatchList, BankFusionEnvironment env) {
        if (approvedBatchList != null && approvedBatchList.size() > 0) {
            Iterator batchIterator = approvedBatchList.iterator();

            /* Changed for re-factoring */
            ArrayList columnList = new ArrayList();
            columnList.add(IBOGeneralFontis.STATUS);
            columnList.add(IBOGeneralFontis.TRANSACTION_REFERENCE);
            /* end */

            // Iterate through all the approved batches
            while (batchIterator.hasNext()) {
                FON_BatchRecord fontisBatchRecord = (FON_BatchRecord) batchIterator.next();
                boolean postingStatus;

                // Begin database transaction
                // env.getFactory().beginTransaction();
                logger.info("Transaction started for Batch : " + batchIterator.hashCode());
                try {
                    // checking for on-us / off-us transaction and setting
                    // EFT/SWIFT message flag
                    if (fontisBatchRecord.getFontisRecordType().equals(tppFileConstant)) {
                        checkOnUsTransaction(fontisBatchRecord);
                    }
                    // Check whether multi currency/cross currency transaction
                    checkMultiCurrBatch(fontisBatchRecord);

                    // Post the transaction & get the posting staus
                    postingStatus = postTransaction(fontisBatchRecord, true, env);

                    // where clause for updating the batch status after
                    // processing
                    String whereClause = "where " + IBOGeneralFontis.FONTIS_BATCH_ID + "='" + fontisBatchRecord.getFontisBatchId()
                            + "'";

                    // If posting successful
                    if (postingStatus) {

                        // if (isICSRequired(fontisBatchRecord)) {
                        UB_ICS_SettleICTransaction ICTransaction = new UB_ICS_SettleICTransaction();
                        ICTransaction.settleICTransactions(tempPostingMsg, env);
                        // }

                        // increment the posted batch counter
                        noOfPostedBatches++;

                        try {
                            // Update the batch status as 4
                            ArrayList valueList = new ArrayList();
                            valueList.add(new Integer(4));
                            valueList.add(fontisBatchRecord.getTransactionReference());
                            env.getFactory().bulkUpdate(IBOGeneralFontis.BONAME, whereClause, columnList, valueList);
                        }
                        catch (Exception ex) {
                            logger.error("Error occured while updating the general fontis record after successful posting. Rolling back...");
                            logger.error(ex);
                        }
                    }
                    // If posting failed
                    else {
                        // increment the failed batch counter
                        noOfFailedBatches++;

                        try {
                            // Update the batch status as 5
                            ArrayList valueList = new ArrayList();
                            valueList.add(new Integer(5));
                            valueList.add(fontisBatchRecord.getTransactionReference());
                            env.getFactory().bulkUpdate(IBOGeneralFontis.BONAME, whereClause, columnList, valueList);
                        }
                        catch (Exception ex) {
                            logger.error("Error occured while updating the general fontis record after failed posting. Rolling back...");
                            logger.error(ex);
                        }
                    }
                    // Commit database transaction
                    // env.getFactory().commitTransaction();
                }
                catch (BankFusionException bfe) {
                    logger.error("Error while posting fontis batch..", bfe);
                    // env.getFactory().rollbackTransaction();
                }
                catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    // env.getFactory().rollbackTransaction();
                }

                logger.info("Transaction committed for Batch : " + batchIterator.hashCode());
            }
        }
    }

    /**
     * This method calls createPostingMessages(), posts the transaction & receives the response from
     * the posting engine. Response is parsed & the posting status of the transaction is updated
     * accordingly & sent back to the caller method.
     * 
     * @param fontisBatch
     * @param isAuthorized
     *            TRUE - if its an authorized record FALSE - otherwise
     * @param env
     * @return TRUE - if posting is successful FALSE - otherwise @ * @
     */
    private boolean postTransaction(FON_BatchRecord fontisBatch, boolean isAuthorized, BankFusionEnvironment env) {

        boolean postingStatus = false;

        // Method calls have been moved into try...catch block so as to handle
        // posting message creation errors.
        HashMap postingDetails = null;
        ArrayList postingMessages = null;
        ArrayList responseMessages = null;
        IPostingEngine pe = null;

        try {
            // Get the posting messages using fontis batch & its details
            postingDetails = (HashMap) createPostingMessages(fontisBatch, isAuthorized, env);
            postingMessages = (ArrayList) postingDetails.get(POSTING_MESSAGES);
            /*
             * Perform Postings Get the PostingEngine Service first, and from that get a reference
             * to the router. Post the messages through the router, then indicate that the posting
             * has completed.
             */
            pe = (IPostingEngine) ServiceManager.getService(ServiceManager.POSTING_ENGINE_SERVICE);
            IPostingRouter pr = (IPostingRouter) pe.getNewInstance();
            // Post the transaction & get the response
            responseMessages = pr.post(postingMessages, env);
            pe.postingComplete();

            postingStatus = true;
        }
        finally {
            if (!postingStatus) { // status is false
                logger.error("Exception Occured at the time of Posting ");
                fontisBatch.setStatusFlag(1);
            }
        }

        // Iterator respIterator=responseMessages.iterator();

        ArrayList debitList = fontisBatch.getDebitRecords();
        ArrayList updatedDebitList = new ArrayList();
        ArrayList creditList = fontisBatch.getCreditRecords();
        ArrayList updatedCreditList = new ArrayList();
        int noOfDebitRecords = ((Integer) postingDetails.get(NO_OF_DEBIT_RECORDS)).intValue();
        int noOfCreditRecords = ((Integer) postingDetails.get(NO_OF_CREDIT_RECORDS)).intValue();
        int noOfDebitsFromPosAcc = ((Integer) postingDetails.get(NO_OF_DEBITS_FROM_POS_ACC)).intValue();
        int noOfCreditsToPosAcc = ((Integer) postingDetails.get(NO_OF_CREDITS_TO_POS_ACC)).intValue();

        int totalDebits = noOfDebitRecords + noOfDebitsFromPosAcc;
        int totalCredits = noOfCreditRecords + noOfCreditsToPosAcc;

        // If number of responses received != the number of trasaction legs
        if (totalDebits + totalCredits != responseMessages.size()) {
            // set the posting status as failed
            postingStatus = false;
            // Increment the failed batch counter
            noOfFailedBatches++;
            logger.error("BankFusionException: Total number of responses do not match total number of posting messages");
            // throw new BankFusionException(127, new Object[] {
            // "Error while posting Fontis Batch. Response received is not equal to the posted entries."
            // }, logger, env);
        }
        int debitRecords = 0;
        int creditRecords = 0;
        if ((responseMessages.size() % 2) == 0) {
            debitRecords = 0;
            creditRecords = 1;
        }
        else {
            debitRecords = 1;
            creditRecords = 0;
        }
        // read through the response list for debit response messages
        for (int cnt = 0; cnt < totalDebits; cnt++) {
            PostingResponse res = (PostingResponse) responseMessages.get(debitRecords);
            debitRecords = debitRecords + 2;
            if (cnt < noOfDebitRecords) {
                FON_DebitRecord debitRecord = (FON_DebitRecord) debitList.get(cnt);
                // If response status is Success
                if (res.getStatus() == 0) {
                    // set the debit posting status as success
                    debitRecord.setStatusFlag(postedBatchStatus);
                    logger.info("***Posting Details***\n");
                    logger.info("AccountID : " + res.getAccountID());
                    logger.info("Posted Amount : " + res.getActualPostedAmount());
                    logger.info("Response--AuthByUserID---" + res.getAuthByUserID());
                    logger.info("Response--BranchID---" + res.getBranchID());
                    logger.info("Message : " + res.getErrMessage());
                    logger.info("Response--MeaageType---" + res.getMessageType());
                    logger.info("Response--Status--" + res.getStatus());
                    logger.info("Response--TransactionID---" + res.getTransactionId());
                }
                // If response status is Failure
                else {
                    // set the debit posting status as failed
                    debitRecord.setStatusFlag(failedBatchStatus);
                    // set the error message
                    debitRecord.setErrMessage(res.getErrMessage());
                    logger.error("Error posting..");
                    logger.error(res.getErrMessage() + "\n" + ": Batch Number = " + fontisBatch.getBatchNo()
                            + ": Account Number = " + res.getAccountID() + ": Transaction Amount = " + res.getActualPostedAmount());
                    // set the posting status as failed
                    postingStatus = false;
                }
                // update the debit record list with the errors populated
                updatedDebitList.add(debitRecord);
            }
            // if position account entries have been posted
            else {
                // if posting has failed
                if (res.getStatus() != 0) {
                    logger.error("Error in posting to position account..");
                    logger.error(res.getErrMessage() + "\n" + ": Batch Number = " + fontisBatch.getBatchNo()
                            + ": Account Number = " + res.getAccountID() + ": Transaction Amount = " + res.getActualPostedAmount());
                    // Set posting status to false
                    postingStatus = false;
                }
                // If posted successfully
                else {
                    logger.info("***Posting Details***\n");
                    logger.info("AccountID : " + res.getAccountID());
                    logger.info("Posted Amount : " + res.getActualPostedAmount());
                    logger.info("Response--AuthByUserID---" + res.getAuthByUserID());
                    logger.info("Response--BranchID---" + res.getBranchID());
                    logger.info("Message : " + res.getErrMessage());
                    logger.info("Response--MeaageType---" + res.getMessageType());
                    logger.info("Response--Status--" + res.getStatus());
                    logger.info("Response--TransactionID---" + res.getTransactionId());
                }
            }
        }

        // read through the response list for credit response messages
        int creditCounter = totalDebits;
        for (int cnt = 0; cnt < totalCredits; cnt++) {
            PostingResponse res = (PostingResponse) responseMessages.get(creditRecords);
            creditRecords = creditRecords + 1;
            logger.debug("Count in Credit" + cnt);
            logger.debug("Credit Counter=" + creditCounter);
            if (cnt < noOfCreditRecords) {
                FON_CreditRecord creditRecord = (FON_CreditRecord) creditList.get(cnt);
                // if posted successfully
                if (res.getStatus() == 0) {
                    // set status as successfully posted
                    creditRecord.setStatusFlag(postedBatchStatus);
                    logger.info("***Posting Details***\n");
                    logger.info("AccountID : " + res.getAccountID());
                    logger.info("Posted Amount : " + res.getActualPostedAmount());
                    logger.info("Response--AuthByUserID---" + res.getAuthByUserID());
                    logger.info("Response--BranchID---" + res.getBranchID());
                    logger.info("Message : " + res.getErrMessage());
                    logger.info("Response--MeaageType---" + res.getMessageType());
                    logger.info("Response--Status--" + res.getStatus());
                    logger.info("Response--TransactionID---" + res.getTransactionId());
                }
                // If posting failed
                else {
                    // set status flag
                    creditRecord.setStatusFlag(failedBatchStatus);
                    // set the error messages
                    creditRecord.setErrMessage(res.getErrMessage());
                    logger.error("Error in posting to position account..");
                    logger.error(res.getErrMessage() + "\n" + ": Batch Number = " + fontisBatch.getBatchNo()
                            + ": Account Number = " + res.getAccountID() + ": Transaction Amount = " + res.getActualPostedAmount());
                    // set posting status as false
                    postingStatus = false;
                }
                // update the credit list with new details populated
                updatedCreditList.add(creditRecord);
            }
            // If position account entries were made
            else {
                // if failed
                if (res.getStatus() != 0) {
                    // set the error message & posting status to false
                    logger.error("Error in posting to position account..");
                    logger.error(res.getErrMessage() + "\n" + ": Batch Number = " + fontisBatch.getBatchNo()
                            + ": Account Number = " + res.getAccountID() + ": Transaction Amount = " + res.getActualPostedAmount());
                    postingStatus = false;
                }
                // if successfull
                else {
                    logger.info("***Posting Details***\n");
                    logger.info("AccountID : " + res.getAccountID());
                    logger.info("Posted Amount : " + res.getActualPostedAmount());
                    logger.info("Response--AuthByUserID---" + res.getAuthByUserID());
                    logger.info("Response--BranchID---" + res.getBranchID());
                    logger.info("Message : " + res.getErrMessage());
                    logger.info("Response--MeaageType---" + res.getMessageType());
                    logger.info("Response--Status--" + res.getStatus());
                    logger.info("Response--TransactionID---" + res.getTransactionId());
                }
            }
        }

        // update the batch object with the latest details populated
        fontisBatch.setDebitRecords(updatedDebitList);
        fontisBatch.setCreditRecords(updatedCreditList);

        // If successfully posted
        if (postingStatus) {
            // Set the batch status as success
            fontisBatch.setStatusFlag(postedBatchStatus);

            // updateTransactionReferenceInFontisConfig(getTransactionRefString(transactionReference),env);
            // The code below has to be used in release 3A & onwards for storing
            // the transaction reference number
            // in fontis configuration table
            updateTransactionReferenceInFontisConfig(fontisBatch.getTransactionReference(), env);

            // write into EFT File if an Off-us, Local currency & within the
            // country transaction
            generateEFTRecords(fontisBatch, env);

            // Update SWTDisposal tabble if destination country is different OR
            // same country but different
            // currency than base currency
            updateSWTTable(fontisBatch, txnID, env);
        }
        // if posting has failed
        else {
            // Set the batch status as failed
            fontisBatch.setStatusFlag(failedBatchStatus);
            // Set the transaction reference field in the batch to empty string
            fontisBatch.setTransactionReference(CommonConstants.EMPTY_STRING);
            // decrement the transaction reference counter
            transactionReference--;
        }

        return postingStatus;
    }

    /**
     * This method populates the account numbers with pseudonym as Fontis Insufficient funds
     * Suspense account pseudonym & EFT account pseudonym.
     * 
     * @param env
     *            @ * @ @
     */
    private String populateAccountsFromPseudoName(String contextName, String contextValue, String alphaCurrencyCode,
            String pseudoName, BankFusionEnvironment env) {

        // ArrayList paramList = new ArrayList();
        // logger.info("BranchSortCode=" + this.getF_IN_BranchSortCode());
        // paramList.add(this.getF_IN_BranchSortCode());
        // paramList.add("%" + pseudoName + "%");

        // paramList.add("%"+pseudoName+"%");
        String accountNumber = CommonConstants.EMPTY_STRING;

        accountNumber = (String) accountFromPseudoname.get(contextName + contextValue + alphaCurrencyCode + pseudoName);
        if (accountNumber != null) {
            return accountNumber;
        }

        try {

            IBOAttributeCollectionFeature accountValues = FinderMethods.findAccountByPseudonameAndContextValue("%" + contextName
                    + "%" + contextValue + "%" + pseudoName, alphaCurrencyCode, Boolean.TRUE, env, null);// PostingHelperdoName,
            // branchSortCode,
            // alphaCurrencyCode,
            // env);
            if (!(accountValues == null)) {
                accountNumber = accountValues.getBoID();

            }
        }
        catch (BankFusionException exception) {
            logger.info(exception.getLocalisedMessage());
            logger.error(exception);
        }
        accountFromPseudoname.put(contextName + contextValue + alphaCurrencyCode + pseudoName, accountNumber);
        return accountNumber;

    }

    /**
     * This method populates the this bank's BIC Code into a static variable
     * 
     * @param env
     *            @
     */
    private void populateSystemBICCode(BankFusionEnvironment env) {
        IBOBranch branchObj = (IBOBranch) env.getFactory().findByPrimaryKey(IBOBranch.BONAME, getF_IN_BranchSortCode(), true);

        if (branchObj == null) {
            logger.error("Branch object is not found for the given Branch Sort Code : " + getF_IN_BranchSortCode());
            /*
             * throw new BankFusionException(9013, new Object[] { getF_IN_BranchSortCode() },
             * logger, env);
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_BRANCH_OBJECT_NOT_FOUND_FOR_BRANCH_SORT_CODE,
                    new Object[] { getF_IN_BranchSortCode() }, new HashMap(), env);
        }
        else if (branchObj.getF_BICCODE() == null || branchObj.getF_BICCODE().trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
            logger.error("BIC Code is not specified for the given Branch Sort Code : " + getF_IN_BranchSortCode());
            /*
             * throw new BankFusionException(9014, new Object[] { getF_IN_BranchSortCode() },
             * logger, env);
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_IDENTIFIER_CODE_UNSPECIFIED_FOR_BRANCH_SORT_CODE,
                    new Object[] { getF_IN_BranchSortCode() }, new HashMap(), env);
        }
        else {
            systemBICCode = branchObj.getF_BICCODE().trim();
            logger.info("System BIC Code = " + systemBICCode);
        }
    }

    /**
     * This method compares the BIC code specified in all the credit records with that of this
     * Bank's BIC Code. And sets either EFT message flag or SWIFT message flag based on Bank code,
     * country code & currency code.
     * 
     * @param fontisBatch
     * @param env
     */
    private void checkOnUsTransaction(FON_BatchRecord fontisBatch) {
        logger.info("Inside checkOnUsTransaction()...");

        // String systemBICCode = this.getF_IN_BranchSortCode();
        logger.info("This Bank's BICCODE = " + systemBICCode);
        ArrayList creditRecordList = fontisBatch.getCreditRecords();
        ArrayList updatedCreditRecordList = new ArrayList();

        // Iterate through all the credit records
        for (int i = 0; i < creditRecordList.size(); i++) {
            FON_CreditRecord creditRecord = (FON_CreditRecord) creditRecordList.get(i);

            // get the BIC code from credit record
            String creditBicCode = creditRecord.getBeneficiaryBankCode();
            logger.info("BICCODE==" + creditBicCode);
            // If BIC Code format is valid
            if (creditBicCode != null && creditBicCode.trim().length() > 8) {
                // EFT Message : If different bank within the country AND
                // transaction currency is base currency
                if (!creditBicCode.substring(0, 4).equals(systemBICCode.substring(0, 4))
                        && creditBicCode.substring(4, 6).equals(systemBICCode.substring(4, 6))
                        && SystemInformationManager.getInstance().getBaseCurrencyCode()
                                .equalsIgnoreCase(creditRecord.getTransactionCurrencyCode())) {
                    // Set EFT message generation flag to TRUE
                    creditRecord.setEFTMessage(true);
                    logger.info("********EFT RECORD**********");
                }
                // SWIFT Message : If destination country is different OR same
                // country but different currency than base currency
                else if (!creditBicCode.substring(4, 6).equals(systemBICCode.substring(4, 6))
                        || (!creditBicCode.substring(0, 4).equals(systemBICCode.substring(0, 4))
                                && creditBicCode.substring(4, 6).equals(systemBICCode.substring(4, 6)) && !SystemInformationManager
                                .getInstance().getBaseCurrencyCode().equalsIgnoreCase(creditRecord.getTransactionCurrencyCode()))) {
                    // Set SWIFT message generation flag to TRUE
                    creditRecord.setSWIFTMessage(true);
                    logger.info("********SWIFT RECORD**********");
                }
            }
            // update the credit record list
            updatedCreditRecordList.add(creditRecord);
        }
        fontisBatch.setCreditRecords(updatedCreditRecordList);
        logger.info("End checkOnUsTransaction()...");
    }

    /**
     * This method creates posting messages to be passed on to the posting engine for all the legs
     * in the given batch & also for all the position account entries.
     * 
     * @param batchRecord
     * @param isAuthorized
     * @param env
     * @return A Map object of posting messages @ * @ @ * @
     */
    private Map createPostingMessages(FON_BatchRecord batchRecord, boolean isAuthorized, BankFusionEnvironment env) {
        String runtimeBPID = env.getRuntimeMicroflowID();
        Timestamp toDay = SystemInformationManager.getInstance().getBFBusinessDateTime(runtimeBPID);
        IBOFinancialPostingMessage postingMessage = null;
        ArrayList postingMessages = new ArrayList();
        ArrayList debitPostingMessages = new ArrayList();
        ArrayList creditPostingMessages = new ArrayList();
        ArrayList debitFromPosAccPostingMessages = new ArrayList();
        ArrayList creditToPosAccPostingMessages = new ArrayList();
        HashMap postingDetails = new HashMap();

        // Flag will be set if Bebit account is Invalid and verified at Credit
        // suspense account
        boolean creditToSuspenseAccount = false;

        tempPostingMsg = new ArrayList();

        // increment the transaction reference counter
        transactionReference++;
        // append FN to the transaction reference
        String batchTransactionRef = getTransactionRefString(transactionReference);

        // Required for tracking the transactions & also for reporting purposes,
        // strictly in & after UB-3A release
        batchRecord.setTransactionReference(batchTransactionRef);

        logger.info("Inside createPostingMessages()...");
        boolean success = false;
        try {
            Date valueDate = null;

            // Clear the previous currency totals if any.
            CurrencyPositionTotals.clear();

            // Depending upon the fontis configuration setting set the value
            // date
            if (fontisConfig.isF_VALUE_DATE_ON_MISMATCH()) {
                valueDate = new Date(SystemInformationManager.getInstance().getBFBusinessDateTime().getTime());
            }
            else {
                valueDate = AddDaysToDate.run(new Date(batchRecord.getValueDate().getTime()), fontisConfig.getF_SFIDAYS());
            }

            isForwardDated = PostingHelper.isForwardDatedTransaction(valueDate);

            // This is for Getting Fixed Narration from TransScreenControl
            // Using the Cache of TransactionScreenControl Table for fetching the details.
            MISTransactionCodeDetails mistransDetails;
            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo()).getMisTransactionCodeDetails(fontisConfig
                    .getF_TRANSACTION_TYPE());

            IBOTransactionScreenControl transBO = mistransDetails.getTransactionScreenControl();

            txnID = GUIDGen.getNewGUID();

            // creating debit transaction posting message
            ArrayList debitRecordList = batchRecord.getDebitRecords();
            for (int j = 0; j < debitRecordList.size(); j++) {
                FON_DebitRecord debitRecord = (FON_DebitRecord) debitRecordList.get(j);
                String accountID = null;
                BigDecimal amount = null;
                String transCode = null;
                StringBuffer transNarr = null;

                String sourceBranch = CommonConstants.EMPTY_STRING;
                IBOAccount accountValues = null;
                count = 0;

                // If debit from Fontis Suspense flag is set

                if (batchRecord.isInSuffIndPostAllCrDrToSuspense() && batchRecord.getFontisRecordType().equals(iatFileConstant)) {
                    logger.info("Reading Insufficient Funds Suspense account : " + fontisConfig.getF_SUSP_ACC_PSEUDONYM()
                            + debitRecord.getDebitCurrencyCode());
                    accountID = (String) populateAccountsFromPseudoName(CONTEXT_NAME, debitRecord.getDebitCurrencyCode(),
                            debitRecord.getDebitCurrencyCode(), fontisConfig.getF_SUSP_ACC_PSEUDONYM(), env);

                }
                else if (debitRecord.isDebitFromFontisSuspenseAccount()) {
                    logger.info("Reading Insufficient Funds Suspense account : " + fontisConfig.getF_SUSP_ACC_PSEUDONYM()
                            + debitRecord.getDebitCurrencyCode());
                    // populate fontis suspense account id from psuedoAccMap for
                    // respective currency code

                    accountID = (String) populateAccountsFromPseudoName(CONTEXT_NAME, debitRecord.getDebitCurrencyCode(),
                            debitRecord.getDebitCurrencyCode(), fontisConfig.getF_SUSP_ACC_PSEUDONYM(), env);
                }
                // If debit from Currency Suspense flag is set
                else if (debitRecord.isDebitFromCurrSuspenseAccount()) {
                    // populate currency suspense account id from psuedoAccMap
                    // for respective currency code
                    accountID = populateAccountsFromPseudoName(CONTEXT_NAME, debitRecord.getDebitCurrencyCode(),
                            debitRecord.getDebitCurrencyCode(), fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM(), env);
                }
                // if none of the flag is set
                else {
                    logger.info("Reading Debit account : " + debitRecord.getDebitAccountNo());
                    // use the account number coming in the file
                    accountID = debitRecord.getDebitAccountNo();

                    // if Authorized batch
                    if (isAuthorized) {
                        // set force post flag to 0
                        debitRecord.setForcePost(true);

                        // Commented to post entries to the same account instead
                        // of currency suspense after
                        // authorization.
                        /*
                         * FON_ValidationHelper validator = new FON_ValidationHelper();
                         * accountValues = (IBOAccount)
                         * env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID);
                         * if(validator.insufficientFunds(accountValues, debitRecord.getAmount(),
                         * env)) { accountID = getCurrencySuspenseAccountID
                         * (debitRecord.getDebitCurrencyCode(), env); }
                         */
                    }
                }

                amount = debitRecord.getAmount();
                transCode = fontisConfig.getF_TRANSACTION_TYPE();

                transNarr = new StringBuffer(transBO.getF_FIXEDNARRATIVE()); // Fixed Narrative
                // from
                // TransControlScreen----BUG:11825

                if (batchRecord.getFontisRecordType().equals(tppFileConstant))// For
                    // Tpp
                    // Narrative
                    // =
                    // Fixed
                    // Narr
                    // +GeneralNarrative
                    // in
                    // the
                    // file
                    transNarr.append(batchRecord.getNarrative());

                else transNarr.append(batchRecord.getGeneralComments()); // For IAT
                // Narrative
                // = Fixed
                // Narr +
                // General
                // Comments

                // sourceBranch = this.getF_IN_BranchSortCode();

                // Populate the debit account details
                accountValues = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID, true);

                // If account not found
                if (accountValues == null) {
                    // Bug Fix 2520: pseudopname may have been mapped into the
                    // accountId, so ...
                    accountID = populateAccountsFromPseudoName(CONTEXT_NAME, debitRecord.getDebitCurrencyCode(),
                            debitRecord.getDebitCurrencyCode(), fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM(), env);
                    // Use the currency suspense account for debit posting
                    accountValues = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID, false);
                    // Credit also should be posted to Currency Suspense Account
                    creditToSuspenseAccount = true;

                }

                sourceBranch = accountValues.getF_BRANCHSORTCODE();

                // Create an IBOFinancialPostingMessage object & set the default
                // values
                postingMessage = (IBOFinancialPostingMessage) env.getFactory().getStatelessNewInstance(
                        IBOFinancialPostingMessage.BONAME);
                FatomUtils.createStandardItemsMessage(postingMessage, env);
                PostingHelper.setDefaultValuesForFinPosting(postingMessage, env);

                // Set transaction details into the posting message
                postingMessage.setPrimaryID(accountID);
                postingMessage.setAcctCurrencyCode(accountValues.getF_ISOCURRENCYCODE());
                // Account currency amount
                postingMessage.setF_AMOUNT(amount.abs());
                postingMessage.setF_AMOUNTCREDIT(amount.abs().negate());
                postingMessage.setF_AMOUNTDEBIT(amount.abs());
                postingMessage.setF_ACTUALAMOUNT(amount.abs());
                postingMessage.setF_BASEEQUIVALENT(debitRecord.getAmountInBaseCurrency());
                postingMessage.setTransCode(transCode);
                // postingMessage.setNarrative(transNarr);
                postingMessage.setTransactionRef(batchTransactionRef);
                postingMessage.setBranchID(sourceBranch);
                postingMessage.setSerialNo(++count);
                postingMessage.setProductID(accountValues.getF_PRODUCTID());
                postingMessage.setPERouterProfileID(accountValues.getF_PEROUTERPROFILEID());
                postingMessage.setTransactionDate(toDay);
                postingMessage.setShortName(CommonConstants.EMPTY_STRING);
                postingMessage.setTransactionID(txnID);
                postingMessage.setF_TXNCURRENCYCODE(debitRecord.getTransactionCurrencyCode());
                postingMessage.setSign('-');
                postingMessage.setValueDate(valueDate);

                if (isForwardDated)
                    postingMessage.setForwardValued(true);

                if (debitRecord.isForcePost()) {
                    postingMessage.setForcePost(true);
                }
                postingMessage.setNarrative(PostingHelper.getBuildedNarrative(postingMessage, CommonConstants.EMPTY_STRING));
                // postingMessage.setChannelID("FONTS");
                // adding to postingMsg List
                debitPostingMessages.add(postingMessage);

                tempPostingMsg.add(postingMessage);
                // If a multi currency or cross currency transaction the update
                // the currency totals to post the net entry
                if (batchRecord.isForgnCurrBatch()) {
                    updateCurrencyPositionTotals(debitRecord.getDebitCurrencyCode(), debitRecord.getAmount().negate());
                }
            }

            // creating postingMessage for CreditRecords
            ArrayList creditRecordList = batchRecord.getCreditRecords();
            for (int j = 0; j < creditRecordList.size(); j++) {
                FON_CreditRecord creditRecord = (FON_CreditRecord) creditRecordList.get(j);

                // String currCode = null;
                BigDecimal amount = null;
                // String postingAction = null;
                String transCode = null;
                StringBuffer transNarr = null;
                String sourceBranch = CommonConstants.EMPTY_STRING;
                IBOAccount accountValues = null;
                // String txnID = GUIDGen.getNewGUID();
                // int count = 0;

                // If an EFT message flag is set
                if (batchRecord.isInSuffIndPostAllCrDrToSuspense() && batchRecord.getFontisRecordType().equals(iatFileConstant)) {
                    logger.info("Reading Insufficient Funds Suspense account : " + fontisConfig.getF_SUSP_ACC_PSEUDONYM()
                            + creditRecord.getCreditCurrencyCode());
                    accountID = (String) populateAccountsFromPseudoName(CONTEXT_NAME, creditRecord.getCreditCurrencyCode(),
                            creditRecord.getCreditCurrencyCode(), fontisConfig.getF_SUSP_ACC_PSEUDONYM(), env);
                }
                else if (creditRecord.isEFTMessage()) {
                    // use EFT account number
                    accountID = (String) populateAccountsFromPseudoName(CONTEXT_NAME, creditRecord.getTransactionCurrencyCode(),
                            creditRecord.getTransactionCurrencyCode(), fontisConfig.getF_EFT_ACC_PSEUDONYM(), env);
                    logger.debug("EFT ACC NUMBER :" + accountID);
                }
                // If credit to suspense flag is set or if a SWIFT message or
                // credit account number is not specified for an IAT
                else if (creditToSuspenseAccount
                        || creditRecord.isCreditToCurrSuspenseAccount()
                        || creditRecord.isSWIFTMessage()
                        || (batchRecord.getFontisRecordType().equalsIgnoreCase(iatFileConstant) && creditRecord
                                .getCreditAccountNo().equalsIgnoreCase(CommonConstants.EMPTY_STRING))) {
                    // get the currency suspense account id
                    accountID = (String) populateAccountsFromPseudoName(CONTEXT_NAME, creditRecord.getTransactionCurrencyCode(),
                            creditRecord.getTransactionCurrencyCode(), fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM(), env);
                }
                // if none of the above mentioned criterias are met
                else {
                    // read account id from the given file (IAT/TPP)
                    if (batchRecord.getFontisRecordType().equals(tppFileConstant))
                        accountID = creditRecord.getBeneficiaryAccountCode();
                    else accountID = creditRecord.getCreditAccountNo();

                    logger.debug("CREDIT ACCOUNT NUMBER :" + accountID);
                }
                amount = creditRecord.getAmount();
                transCode = fontisConfig.getF_TRANSACTION_TYPE();

                transNarr = new StringBuffer(transBO.getF_FIXEDNARRATIVE());// Fixed Narration
                // from
                // TransControlScreen--BUG:11825

                if (batchRecord.getFontisRecordType().equals(tppFileConstant))// Narratio=FixedNarr+General
                    // Narration
                    // from
                    // file
                    transNarr = transNarr.append(batchRecord.getNarrative());

                else transNarr = transNarr.append(batchRecord.getGeneralComments());// Narration=Fixed+General
                // Comments

                // sourceBranch = this.getF_IN_BranchSortCode();
                // read account details from account table

                accountValues = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID, true);

                // If account details are not found for the given account id
                if (accountValues == null) {
                    // Bug Fix 2520: pseudopname may have been mapped into the
                    // accountId, so ...
                    // read currency suspense account for credit posting
                    if (batchRecord.getFontisRecordType().equalsIgnoreCase(iatFileConstant)) {
                        accountID = populateAccountsFromPseudoName(CONTEXT_NAME, creditRecord.getCreditCurrencyCode(),
                                creditRecord.getCreditCurrencyCode(), fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM(), env);
                    }
                    else {
                        accountID = populateAccountsFromPseudoName(CONTEXT_NAME, creditRecord.getTransactionCurrencyCode(),
                                creditRecord.getTransactionCurrencyCode(), fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM(), env);
                    }

                    accountValues = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID, false);
                }

                sourceBranch = accountValues.getF_BRANCHSORTCODE();

                // if currency suspense account is also not found then throw
                // exception

                /*
                 * Any credit transaction (TPP on us/ IAT) on failure due to account password /
                 * dormancy reject and account closed should be posted to suspense account on
                 * authorization. Any credit transaction (TPP off us) on failure due to account
                 * password / dormancy reject and account closed should be posted to suspense
                 * account without authorization
                 */
                if (accountValues.isF_CLOSED()
                        || (accountValues.isF_DORMANTSTATUS() && FON_ValidationHelper.DormancyPostingAction
                                .equalsIgnoreCase(FON_ValidationHelper.MIS_REJECT_TRANSACTION))
                        || accountValues.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY
                        || accountValues.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY
                        || accountValues.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.CREDITS_NOT_ALLOWED) {
                    // get the currency suspense account details
                    accountID = populateAccountsFromPseudoName(CONTEXT_NAME, creditRecord.getTransactionCurrencyCode(),
                            creditRecord.getTransactionCurrencyCode(), fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM(), env);
                    accountValues = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID);
                }

                // Create an IBOFinancialPostingMessage object & set the default
                // values
                postingMessage = (IBOFinancialPostingMessage) env.getFactory().getStatelessNewInstance(
                        IBOFinancialPostingMessage.BONAME);
                FatomUtils.createStandardItemsMessage(postingMessage, env);
                PostingHelper.setDefaultValuesForFinPosting(postingMessage, env);

                // Set transaction details into the posting message
                postingMessage.setPrimaryID(accountID);
                postingMessage.setAcctCurrencyCode(accountValues.getF_ISOCURRENCYCODE());
                postingMessage.setF_AMOUNT(amount);
                postingMessage.setF_ACTUALAMOUNT(amount);
                postingMessage.setF_AMOUNTCREDIT(amount.abs());
                postingMessage.setF_AMOUNTDEBIT(amount.abs().negate());
                postingMessage.setF_BASEEQUIVALENT(creditRecord.getAmountInBaseCurrency());
                postingMessage.setTransCode(transCode);
                // postingMessage.setNarrative(transNarr);
                postingMessage.setTransactionRef(batchTransactionRef);
                postingMessage.setBranchID(sourceBranch);
                postingMessage.setSerialNo(++count);
                postingMessage.setProductID(accountValues.getF_PRODUCTID());
                postingMessage.setPERouterProfileID(accountValues.getF_PEROUTERPROFILEID());
                postingMessage.setTransactionDate(toDay);
                postingMessage.setShortName(CommonConstants.EMPTY_STRING);
                postingMessage.setTransactionID(txnID);
                postingMessage.setF_TXNCURRENCYCODE(creditRecord.getTransactionCurrencyCode());
                postingMessage.setSign('+');
                postingMessage.setValueDate(valueDate);

                if (isForwardDated)
                    postingMessage.setForwardValued(true);

                if (isAuthorized || creditRecord.isForcePost()) {
                    postingMessage.setForcePost(true);
                }
                postingMessage.setNarrative(PostingHelper.getBuildedNarrative(postingMessage, CommonConstants.EMPTY_STRING));

                // postingMessage.setChannelID("FONTS");

                creditPostingMessages.add(postingMessage);

                // if(creditRecord.getCreditBICCode().equalsIgnoreCase(systemBICCode))
                // {
                tempPostingMsg.add(postingMessage);
                // }

                // If a multi currency or cross currency transaction the update
                // the currency totals to post the net entry
                if (batchRecord.isForgnCurrBatch()) {
                    if (batchRecord.getFontisRecordType().equalsIgnoreCase(tppFileConstant)) {
                        // use transaction currency code
                        updateCurrencyPositionTotals(creditRecord.getTransactionCurrencyCode(), creditRecord.getAmount());
                    }
                    else {
                        // use credit currency code
                        updateCurrencyPositionTotals(creditRecord.getCreditCurrencyCode(), creditRecord.getAmount());
                    }
                }
            }

            // If a multi currency or cross currency transaction
            if (batchRecord.isForgnCurrBatch()) {
                // create position account posting entries
                createPositionAccountEntries(debitFromPosAccPostingMessages, creditToPosAccPostingMessages, valueDate,
                        batchTransactionRef, env);
            }

            // populate number of debit & credit account entries
            postingDetails.put(NO_OF_DEBIT_RECORDS, new Integer(debitPostingMessages.size()));
            postingDetails.put(NO_OF_CREDIT_RECORDS, new Integer(creditPostingMessages.size()));

            // populate debit position account entries
            if (debitFromPosAccPostingMessages.size() > 0)
                debitPostingMessages.addAll(debitFromPosAccPostingMessages);
            // populate credit position account entries
            if (creditToPosAccPostingMessages.size() > 0)
                creditPostingMessages.addAll(creditToPosAccPostingMessages);

            // populate credit & debit the posting messages
            postingMessages.addAll(debitPostingMessages);
            postingMessages.addAll(creditPostingMessages);

            // Set total number of posting entries
            postingDetails.put(POSTING_MESSAGES, postingMessages);

            // Set total number of debit position account entries
            postingDetails.put(NO_OF_DEBITS_FROM_POS_ACC, new Integer(debitFromPosAccPostingMessages.size()));

            // Set total number of debit position account entries
            postingDetails.put(NO_OF_CREDITS_TO_POS_ACC, new Integer(creditToPosAccPostingMessages.size()));
            logger.info("End createPostingMessages()...");

            success = true;
        }
        catch (Exception exception) {
            logger.error(exception);
        }
        finally {
            if (!success) {
                // Fix for Bug#6212 : Decrement the transaction reference
                // counter
                transactionReference--;
            }
        }
        // TODO - UB_REFACTOR - Exception Handling. Why are ServiceException &
        // BODefnException consumed?
        return postingDetails;
    }

    // Not required - new method written. Expected - posting engine will be
    // handling position account entries required in future releases.
    /*
     * private String getCurrencyPostionAccountID(String currCode, BankFusionEnvironment env) {
     * String findByEquvAndISOCurrCode="WHERE "+IBOCurrencyPositionsFeature
     * .EQUIVALENTCURRENCYCODE+" = ? AND " +IBOCurrencyPositionsFeature.ISOCURRENCYCODE+" = ? " ;
     * ArrayList currParams=new ArrayList(); currParams.add(this.getF_IN_BaseCurrencyCode());
     * currParams.add(currCode); ArrayList currPositionAcc; try { currPositionAcc = (ArrayList)
     * env.getFactory().findByQuery(IBOCurrencyPositionsFeature.BONAME, findByEquvAndISOCurrCode,
     * currParams, null); } catch (BankFusionException e) { return null; } String
     * accountID=((IBOCurrencyPositionsFeature)currPositionAcc .get(0)).getF_ACCOUNTID();
     * 
     * return accountID; }
     */

    /**
     * This method fetches the currency suspense account id. return currency suspense account id
     */
    // public String getCurrencySuspenseAccountID(String
    // transactionCurrencyCode, BankFusionEnvironment env) {
    // String findByEquvAndISOCurrCode = "WHERE " + IBOAccount.PSEUDONAME +
    // " = ? ";
    // ArrayList currParams = new ArrayList();
    // currParams.add(fontisConfig.getF_CURR_SUSP_ACC_PSEUDONYM()+transactionCurrencyCode);
    // ArrayList currSuspenseAcc =(ArrayList)
    // env.getFactory().findByQuery(IBOAccount.BONAME,
    // findByEquvAndISOCurrCode, currParams, null, true);
    //
    // if (currSuspenseAcc == null) {
    // return null;
    // }
    // //TODO - UB_REFACTOR - Exception Handling
    //
    // String accountID = ((IBOAccount) currSuspenseAcc.get(0)).getBoID();
    //
    // return accountID;
    // }
    /**
     * This method updates the transaction reference number in the FONTISCONFIG table after posting
     * each transaction
     * 
     * @param latestTransactionReference
     * @param env
     *            @
     */
    private void updateTransactionReferenceInFontisConfig(String latestTransactionReference, BankFusionEnvironment env) {
        String whereClause = "where " + IBOFontisConfig.FONTIS_CONFIG_ID + "='" + fontisConfig.getBoID() + "'";
        ArrayList columnList = new ArrayList();
        ArrayList valueList = new ArrayList();
        columnList.add(IBOFontisConfig.TRANSACTION_REFERENCE);
        valueList.add(latestTransactionReference);

        env.getFactory().bulkUpdate(IBOFontisConfig.BONAME, whereClause, columnList, valueList);
        // TODO - UB_REFACTOR - Exception Handling
    }

    /**
     * This method is used to prefix FN & zeros to the transaction reference number if required
     * 
     * @param latestTransactionReference
     * @return transaction reference number (FN followed by 6 digit number)
     */
    private String getTransactionRefString(int latestTransactionReference) {

        String transRefStr = Integer.toString(latestTransactionReference);
        int strLength = transRefStr.length();
        String fnStr = "FN";
        String zeroStr = "000000";
        String newTransactionRef = CommonConstants.EMPTY_STRING;
        if (strLength <= 6)
            newTransactionRef = fnStr + zeroStr.substring(0, zeroStr.length() - strLength) + transRefStr;
        else if (strLength > 6)
            newTransactionRef = fnStr + zeroStr.substring(0, zeroStr.length() - 1) + "1";

        return newTransactionRef;
    }

    /**
     * This method updates the EFT File counter in FONTISCONFIG table each time a EFT file is
     * generated
     * 
     * @param latestFileId
     * @param env
     *            @
     */
    private void updateEFTFileIdInFontisConfig(int latestFileId, BankFusionEnvironment env) {
        String whereClause = "where " + IBOFontisConfig.FONTIS_CONFIG_ID + "='" + fontisConfig.getBoID() + "'";
        ArrayList columnList = new ArrayList();
        ArrayList valueList = new ArrayList();
        columnList.add(IBOFontisConfig.EFTFILEID);
        valueList.add(new Integer(latestFileId + 1));
        boolean success = false;
        try {
            // env.getFactory().beginTransaction();
            env.getFactory().bulkUpdate(IBOFontisConfig.BONAME, whereClause, columnList, valueList);
            // env.getFactory().commitTransaction();

            success = true;
        }
        finally {
            if (!success) {
                // env.getFactory().rollbackTransaction();
                logger.info("IBOFontisConfig update failed");
            }
        }
        // TODO - UB_REFACTOR - Exception Handling
    }

    /**
     * Generates & writes EFT file header details
     * 
     * @param env
     *            @
     */
    private void generateEFTHeader(BankFusionEnvironment env) {
        FileWriter eftFileWriter = null;

        try {
            eftFilePath = getF_IN_EFTOutputPath().substring(0, (getF_IN_EFTOutputPath().lastIndexOf(".")))
                    + fontisConfig.getF_EFTFILEID() + getF_IN_EFTOutputPath().substring(getF_IN_EFTOutputPath().lastIndexOf("."));
            File eftFile = new File(eftFilePath);
            StringHelper strHelper = new StringHelper();

            if (eftFile.exists()) {
                FileWriter temp = new FileWriter(eftFile, true);
                temp.close();
                /* throw new BankFusionException(9020, null, logger, env); */
                EventsHelper.handleEvent(ChannelsEventCodes.E_EFT_FILE_ALREADY_EXISTS, new Object[] {}, new HashMap(), env);
            }
            /*
             * else { eftFile.mkdirs(); }
             */

            eftFile.createNewFile();
            eftFileWriter = new FileWriter(eftFile, true);
            StringBuffer header = new StringBuffer();

            header.append("UHL");
            header.append(sdf.format(SystemInformationManager.getInstance().getBFBusinessDate()));
            header.append("CTSEF1");
            header.append(fontisConfig.getF_DEST_SORT_CODE().replaceAll("-", CommonConstants.EMPTY_STRING));
            header.append(strHelper.leftPad(CommonConstants.EMPTY_STRING + fontisConfig.getF_EFTFILEID(), "0", 6)); // Unique
            // File
            // number
            // :
            // Taking
            // YYMMDD
            header.append("DIRECT PAYMENTS");
            header.append("000.002");
            header.append("\r\n");

            eftFileWriter.write(header.toString());
            // eftFileWriter.close();
        }
        catch (Exception ex) {
            /* throw new BankFusionException(9019, new Object[] { ex.getMessage() }, logger, env); */
            EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_OCCURED_WHILE_CREATING_EFT_FILE, new Object[] { ex.getMessage() },
                    new HashMap(), env);
            logger.error(ex);
        }
        finally {
            if (eftFileWriter != null) {
                try {
                    eftFileWriter.close();
                }
                catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }
    }

    /**
     * Generates & writes EFT credit record into EFT file
     * 
     * @param fontisBatch
     * @param env
     */
    private void generateEFTRecords(FON_BatchRecord fontisBatch, BankFusionEnvironment env) {
        FileWriter eftFileWriter = null;

        try {
            StringHelper strHelper = new StringHelper();

            for (int i = 0; i < fontisBatch.getCreditRecords().size(); i++) {
                if (((FON_CreditRecord) fontisBatch.getCreditRecords().get(i)).isEFTMessage()) {
                    if (eftCreditCount == 0) {
                        this.generateEFTHeader(env);
                    }
                    eftFileWriter = new FileWriter(eftFilePath, true);
                    StringBuffer credits = new StringBuffer();

                    credits.append("PAY");
                    credits.append(strHelper.rightPad(
                            ((FON_CreditRecord) fontisBatch.getCreditRecords().get(i)).getBeneficiaryBankName(), " ", 6)); // Beneficiary
                    // sort
                    // code
                    credits.append(strHelper.rightPad(
                            ((FON_CreditRecord) fontisBatch.getCreditRecords().get(i)).getBeneficiaryAccountCode(), " ", 13));
                    credits.append("1");
                    credits.append(strHelper.leftPad(CommonConstants.EMPTY_STRING + FON_ValidationHelper.TransTypeNumericCode, "0",
                            2));
                    credits.append(fontisConfig.getF_DEST_SORT_CODE().replaceAll("-", CommonConstants.EMPTY_STRING)); // Stanbic
                    // sort
                    // code
                    // –
                    // same
                    // as
                    // receiver
                    // sort
                    // code
                    // in
                    // the
                    // header
                    credits.append(strHelper.rightPad(
                            (String) populateAccountsFromPseudoName(CONTEXT_NAME, SystemInformationManager.getInstance()
                                    .getBaseCurrencyCode(), SystemInformationManager.getInstance().getBaseCurrencyCode(),
                                    fontisConfig.getF_EFT_ACC_PSEUDONYM(), env), " ", 13));// CATS
                    // EFT
                    // Acc
                    // No.
                    credits.append("1");
                    credits.append(strHelper.rightPad(((FON_CreditRecord) fontisBatch.getCreditRecords().get(i)).getReference(),
                            " ", 15));
                    credits.append(strHelper.rightPad(
                            ((FON_CreditRecord) fontisBatch.getCreditRecords().get(i)).getBeneficiaryName(), " ", 30));
                    // BigDecimal creditAmount =
                    // ((FON_CreditRecord)fontisBatch.getCreditRecords().get(i)).getAmount();
                    // creditAmount.setScale(SystemInformationManager.getInstance().getCurrencyScale(SystemInformationManager.getInstance().getBaseCurrencyCode()));
                    BFCurrencyValue bfc = new BFCurrencyValue(SystemInformationManager.getInstance().getBaseCurrencyCode(),
                            ((FON_CreditRecord) fontisBatch.getCreditRecords().get(i)).getAmount(), env.getUserID());
                    BigDecimal creditAmount = bfc.getRoundedAmount(SystemInformationManager.getInstance().getBaseCurrencyCode());
                    credits.append(strHelper.leftPad(creditAmount.toString(), "0", 12));
                    eftCreditTotal = eftCreditTotal.add(creditAmount); // Credit
                    // total
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(SystemInformationManager.getInstance().getBFBusinessDate());
                    cal.add(Calendar.DATE, 4);// Add
                    // sfi
                    // days
                    // to
                    // the
                    // current
                    // date
                    if (!IsWorkingDay.run("BANK", CommonConstants.EMPTY_STRING, CommonConstants.INTEGER_ZERO, cal.getTime(), env)
                            .booleanValue()) {
                        cal.setTime(NextWorkingDateForDate.run("BANK", CommonConstants.EMPTY_STRING, CommonConstants.INTEGER_ZERO,
                                cal.getTime(), env));
                    }
                    credits.append(sdf.format(cal.getTime()));
                    credits.append("00");
                    credits.append("\r\n");

                    eftFileWriter.write(credits.toString());
                    eftCreditCount += 1;
                    eftFileWriter.close();
                    
                }
            }
        }
        catch (Exception ex) {
            logger.error("Error : Error occured while writing into EFT File. Continuing Processing with next record.");
            logger.error(ex);
        }
        finally {
             {
                try {
                	if (eftFileWriter != null)
                    eftFileWriter.close();
                }
                catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }
    }

    /**
     * Write Debit details & Trailer into the EFT file.
     * 
     * @param BankFusionEnvironment
     *            @
     */
    private void generateEFTTrailer(BankFusionEnvironment env) {
        FileWriter eftFileWriter = null;
        try {
            StringHelper strHelper = new StringHelper();
            StringBuffer debitnTrailer = new StringBuffer();
            eftFileWriter = new FileWriter(eftFilePath, true);
            // Write debit details
            if (eftCreditCount > 0) {
                eftDebitCount += 1;
                debitnTrailer.append("PAY");
                debitnTrailer.append(fontisConfig.getF_DEST_SORT_CODE().replaceAll("-", CommonConstants.EMPTY_STRING));
                debitnTrailer.append(strHelper.rightPad(
                        (String) populateAccountsFromPseudoName(CONTEXT_NAME, SystemInformationManager.getInstance()
                                .getBaseCurrencyCode(), SystemInformationManager.getInstance().getBaseCurrencyCode(), fontisConfig
                                .getF_EFT_ACC_PSEUDONYM(), env), " ", 13));
                debitnTrailer.append("1");
                debitnTrailer.append(strHelper.leftPad(CommonConstants.EMPTY_STRING + FON_ValidationHelper.TransTypeNumericCode,
                        "0", 2));
                debitnTrailer.append(fontisConfig.getF_DEST_SORT_CODE().replaceAll("-", CommonConstants.EMPTY_STRING));// Stanbic
                // sort
                // code
                // –
                // same
                // as
                // receiver
                // sort
                // code
                // in
                // the
                // header
                debitnTrailer.append(strHelper.rightPad(
                        (String) populateAccountsFromPseudoName(CONTEXT_NAME, SystemInformationManager.getInstance()
                                .getBaseCurrencyCode(), SystemInformationManager.getInstance().getBaseCurrencyCode(), fontisConfig
                                .getF_EFT_ACC_PSEUDONYM(), env), " ", 13));
                debitnTrailer.append("1");
                debitnTrailer.append(strHelper.rightPad("CONTRA ENTRY", " ", 15));
                debitnTrailer.append(strHelper.rightPad("CATS SETTLEMENT", " ", 15));
                debitnTrailer.append(strHelper.leftPad(eftCreditTotal.toString(), "0", 12));
                Calendar cal = Calendar.getInstance();
                cal.setTime(SystemInformationManager.getInstance().getBFBusinessDate());
                cal.add(Calendar.DATE, fontisConfig.getF_SFIDAYS());
                if (!IsWorkingDay.run("BANK", CommonConstants.EMPTY_STRING, CommonConstants.INTEGER_ZERO, cal.getTime(), env)
                        .booleanValue()) {
                    cal.setTime(NextWorkingDateForDate.run("BANK", CommonConstants.EMPTY_STRING, CommonConstants.INTEGER_ZERO,
                            cal.getTime(), env));
                }
                debitnTrailer.append(sdf.format(cal.getTime()));
                debitnTrailer.append("00");
                debitnTrailer.append("\r\n");

                eftFileWriter.write(debitnTrailer.toString());
                debitnTrailer.setLength(0);
            }

            // Write trailer details
            debitnTrailer.append("UTL");
            debitnTrailer.append(strHelper.leftPad(eftCreditTotal.toString(), "0", 14));// Debit
            // total
            // =
            // Credit
            // total
            debitnTrailer.append(strHelper.leftPad(eftCreditTotal.toString(), "0", 14));// Credit
            // total
            debitnTrailer.append(strHelper.leftPad(CommonConstants.EMPTY_STRING + eftDebitCount, "0", 6));
            debitnTrailer.append(strHelper.leftPad(CommonConstants.EMPTY_STRING + eftCreditCount, "0", 6));
            debitnTrailer.append("\r\n");

            eftFileWriter.write(debitnTrailer.toString());
            eftFileWriter.close();
        }
        catch (Exception ex) {
            logger.error("Error : Error occured while writing Trailer record into EFT File.");
            logger.error(ex);        
        }
        finally {
                try {
                	if(eftFileWriter!=null)
                    eftFileWriter.close();
                }
                catch (Exception ex) {
                    logger.error(ex);
                }
            
        }
    }

    /**
     * This method updates the totals for each currency passed
     * 
     * @param currencyCode
     * @param amount
     */
    private void updateCurrencyPositionTotals(String currencyCode, BigDecimal amount) {
        if (CurrencyPositionTotals.containsKey(currencyCode)) {
            amount = amount.add((BigDecimal) CurrencyPositionTotals.get(currencyCode));
        }
        CurrencyPositionTotals.put(currencyCode, amount);
    }

    /**
     * Create the position account posting messages.
     * 
     * @param debitFromPosAccPostingMessages
     * @param creditToPosAccPostingMessages
     * @param batchRecord
     * @param valueDate
     * @param transRefNumber
     * @param env
     *            @
     */
    private void createPositionAccountEntries(ArrayList debitFromPosAccPostingMessages, ArrayList creditToPosAccPostingMessages,
            Date valueDate, String transRefNumber, BankFusionEnvironment env) {
        Set keys = CurrencyPositionTotals.keySet();
        Iterator currencies = keys.iterator();
        // int ccount = batchRecord.getCreditRecords().size() - 1;
        // int dcount = batchRecord.getDebitRecords().size() - 1;

        while (currencies.hasNext()) {
            String currCode = (String) currencies.next();
            BigDecimal amount = (BigDecimal) CurrencyPositionTotals.get(currCode);
            if (amount.compareTo(CommonConstants.BIGDECIMAL_ZERO) != 0) {
                // currencyAccount =
                // (IBOCurrencyAccounts)env.getFactory().findByPrimaryKey(IBOCurrencyAccounts.BONAME,
                // currCode);

                IBOFinancialPostingMessage postingMessage = (IBOFinancialPostingMessage) env.getFactory().getStatelessNewInstance(
                        IBOFinancialPostingMessage.BONAME);
                FatomUtils.createStandardItemsMessage(postingMessage, env);
                PostingHelper.setDefaultValuesForFinPosting(postingMessage, env);

                // CREATE A DEFAULT FINANCIAL MESSAGE
                // GET THE POSITION ACCOUNT
                String spotPseudonym = ForexModuleConfiguration.getSpotPositionPseudonym();
                IBOAttributeCollectionFeature positionAccountItem = FinderMethods.findAccountByPseudonameAndContextValue(
                        "%CURRENCY%" + currCode + "%" + spotPseudonym, currCode, Boolean.TRUE, env, null);

                // List positionAccount =
                // FinderMethods.findAccountByPseudoNameAndBranch(spotPseudonym+currCode,
                // getF_IN_BranchSortCode(), env, null);
                // List positionAccount =
                // FinderMethods.findAccountByPseudoNameBranchAndCurrency(spotPseudonym,
                // getF_IN_BranchSortCode(), currCode, env, null);
                // IBOAttributeCollectionFeature positionAccountItem =
                // (IBOAttributeCollectionFeature)positionAccount.get(0);
                // IBOAttributeCollectionFeature positionAccountItem =
                // (IBOAttributeCollectionFeature) FinderMethods
                // .findDefaultCurrencyPositionAccount(currencyValue.getCurrencyCode(),
                // ForexConstants.SPOT_MODE,
                // getEnvironment(), null).get(0);
                // GET THE POSITION ACCOUNT

                postingMessage.setRunTimeBPID(env.getRuntimeMicroflowID());
                postingMessage.setBranchID(env.getUserBranch());
                postingMessage.setInitiatedByuserID(env.getUserID());
                postingMessage.setAuthenticatingUserID(env.getUserID());
                postingMessage.setTransactionID(txnID);// GUIDGen.getNewGUID());
                postingMessage.setTransactionRef(transRefNumber);
                postingMessage.setPrimaryID(positionAccountItem.getBoID());
                postingMessage.setProductID(positionAccountItem.getF_PRODUCTID());
                postingMessage.setMessageType(PostingMessageConstants.FINANCIAL_POSTING_MESSAGE);
                postingMessage.setValueDate(valueDate);
                // condition for forward dated transaction
                if (isForwardDated)
                    postingMessage.setForwardValued(true);

                postingMessage.setTransactionDate(SystemInformationManager.getInstance().getBFBusinessDate());
                postingMessage.setPERouterProfileID(positionAccountItem.getF_PEROUTERPROFILEID());
                postingMessage.setAcctCurrencyCode(positionAccountItem.getF_ISOCURRENCYCODE());
                postingMessage.setF_DRAWERNUMBER(env.getDrawerNumber());
                postingMessage.setF_USERTYPE(env.getUserType());
                // postingMessage.setNarrative("Currency Position Posting");
                postingMessage.setF_AMOUNT(amount.abs());
                postingMessage.setF_ACTUALAMOUNT(amount.abs());
                postingMessage.setF_BOOKBALANCE(positionAccountItem.getF_BOOKEDBALANCE());
                // postingMessage.setChannelID("FONTS");

                /*
                 * String debitTrCode = ((String) ((IServerBusinessInfo)
                 * (SystemInformationManager.getInstance())) .getModuleConfigurationValue
                 * (ForexConstants.MODULE_CONFIG_KEY,
                 * ForexConstants.PROPKEYSTR_POSITION_POSTING_DEBIT_TXNCODE, env));
                 * 
                 * String creditTrCode = ((String) ((IServerBusinessInfo)
                 * (SystemInformationManager.getInstance())) .getModuleConfigurationValue
                 * (ForexConstants.MODULE_CONFIG_KEY,
                 * ForexConstants.PROPKEYSTR_POSITION_POSTING_CREDIT_TXNCODE, env));
                 */

                // If the amount is negative
                if (amount.signum() == -1) {
                    // Debit balance : Post a Credit entry
                    postingMessage.setSign('+');
                    postingMessage.setSerialNo(++count);
                    postingMessage.setTransCode(fontisConfig.getF_TRANSACTION_TYPE());
                    postingMessage.setF_AMOUNTCREDIT(amount.abs());
                    postingMessage.setF_AMOUNTDEBIT(amount.abs().negate());
                    postingMessage.setNarrative(PostingHelper.getBuildedNarrative(postingMessage, CommonConstants.EMPTY_STRING));
                    creditToPosAccPostingMessages.add(postingMessage);
                }
                // If the amount is positive
                else {
                    // Credit balance : Post a Debit entry
                    postingMessage.setSign('-');
                    postingMessage.setSerialNo(++count);
                    postingMessage.setTransCode(fontisConfig.getF_TRANSACTION_TYPE());
                    postingMessage.setF_AMOUNTCREDIT(amount.abs().negate());
                    postingMessage.setF_AMOUNTDEBIT(amount.abs());
                    postingMessage.setNarrative(PostingHelper.getBuildedNarrative(postingMessage, CommonConstants.EMPTY_STRING));
                    debitFromPosAccPostingMessages.add(postingMessage);
                }
            }
        }
    }

    /**
     * This method creates the SWIFT records in SWIFTDISPOSAL table if any SWIFT transactions
     * come-in.
     * 
     * @param fontisBatch
     * @param env
     *            @ * @ @
     */
    private void updateSWTTable(FON_BatchRecord fontisBatch, String transactionID, BankFusionEnvironment env) {
        FON_SWTDisposalUpdate callSWIFT = new FON_SWTDisposalUpdate();
        callSWIFT.SWTDisposalUpdate(env, fontisBatch, transactionID, accountID);
    }

}
