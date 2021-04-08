/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 */
package com.trapedza.bankfusion.fatoms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOCTLFailedTrans;
import com.trapedza.bankfusion.bo.refimpl.IBOCTLFileHistory;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CardTechMessageValidator;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.features.PseudoNameFeature;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractCardTech_FileProcessFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class CardTech_FileProcessFatom extends AbstractCardTech_FileProcessFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final String CRADTECH_PARAM_NAME = "FILEPATH";

    private static final String LOGHISTORY_FILE_NAME = "CardTechTransaction.log";

    private static final String CARDTECH_MODULE_NAME = "CARDTECH";

    private static final String CRADTECH_DEBIT_SUSPENSE_ACC = "DEBITSUSPENSEACC";

    private static final String CRADTECH_CREDIT_SUSPENSE_ACC = "CREDITSUSPENSEACC";

    private static final String CRADTECH_SETTLEMENTACCOUNT = "DOMESTICTXNSETTLEACC";

    private static final String CRADTECH_LASTDOMESTIC_FILEPROCESSED = "LASTDOMESTICFILEPROCESSED";

    private static final String CRADTECH_LASTINTERNATIONAL_FILEPROCESSED = "LASTINTLFILEPROCESSED";

    /* First three characters of the Domestic file */
    private static final String DOMESTIC = "AM4";

    /* First three characters of the International file */
    private static final String INTERNATIONAL = "TF4";

    private static final String SUCCESS = "Success";

    private static final String FAILED = "Failed";

    private static final String POSTED_TO_SUSPENSE = "Posted to Suspense";

    private List<String> domesticFilesList = new ArrayList<String>();
    private List<String> internationalFilesList = new ArrayList<String>();
    private String dirPath;
    int lastDomesticFileProcessed;
    int lastInterFileProcessed;
    String crSuspenseAccount;
    String drSuspenseAccount;
    String settlementaccount;
    private Date processedDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
    CardTechMessageValidator messageValidator = new CardTechMessageValidator();
    public String message = CommonConstants.EMPTY_STRING;
    private String errorMessage = CommonConstants.EMPTY_STRING;
    private String errorStatus = CommonConstants.EMPTY_STRING;
    private String fileType = CommonConstants.EMPTY_STRING;

    /**
     * Holds the reference for logger object
     */
    private transient final static Log logger = LogFactory.getLog(CardTech_FileProcessFatom.class.getName());

    public CardTech_FileProcessFatom(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("Process..");
        FileWriter writer = null;
        BufferedWriter outputFile = null;
        FileWriter logHistoryFileWriter = null;
        BufferedWriter logHistoryOutputFile = null;

        /* Get Card Tech Module configuration details */

        getModuleConfDetails(env);

        File dir = new File(dirPath);
        String[] files = dir.list();

        /* If we removed filepath from Module Congiguration,it will throw an error. */
        if (dirPath.equals("")) {
            // throw new BankFusionException(7585, new Object[] { }, logger, null);
            EventsHelper.handleEvent(ChannelsEventCodes.E_UPLOAD_FILES_PATH_IS_NOT_CONFIGURED, new Object[] {}, new HashMap(), env);

        }

        if (domesticFilesList.size()==0 && internationalFilesList.size()==0) {
            // throw new BankFusionException(7586, new Object[] { }, logger, null);
            EventsHelper.handleEvent(ChannelsEventCodes.E_INCORRECT_PATH_OR_FILES_NOT_PRESENT_IN_THE_PATH, new Object[] {},
                    new HashMap(), env);

        }

        /* Separate Domestic and International files */
        separateFiles(files);

        /* If consolidation is not required, do only sorting of input files */
        if (isF_IN_ConsolidationRequired()) {

            try {
                writer = new FileWriter(dirPath + "CardTech_ConsolidatedTransactions");
                outputFile = new BufferedWriter(writer);
                
            }
            catch (IOException e) {
                logger.error(e);
            }finally{
            	try{
            		if(writer!=null)
            			writer.close();
            	}catch(Exception e){
logger.error(e.getStackTrace());                 	}
            	try{
            		if(outputFile!=null)
        			outputFile.close();
            	}catch(Exception e){
            		logger.error(e);
            	}
            }
            try {
                File file = null;

                if (!((file = new File(dirPath + LOGHISTORY_FILE_NAME)).exists())) {

                    file.createNewFile();
                }
                logHistoryFileWriter = new FileWriter(file, true);
                logHistoryOutputFile = new BufferedWriter(logHistoryFileWriter);

            }
            catch (IOException e) {
                logger.error(e);
            }finally{
            	try{
            		if(logHistoryFileWriter!=null)
            			logHistoryFileWriter.close();
            	}catch(Exception e){
logger.error(e.getStackTrace());            	}
            	try{
            		if(logHistoryOutputFile!=null)
            			logHistoryOutputFile.close();
            	}catch(IOException ioe){
            		logger.error(ioe);	
            	}
            }
        }

        DomesticFiles(outputFile, logHistoryOutputFile, env);
        InternationalFiles(outputFile, logHistoryOutputFile, env);

        if (isF_IN_ConsolidationRequired()) {
            try {
                if (outputFile != null)
                    outputFile.close();
                if (logHistoryOutputFile != null)
                    logHistoryOutputFile.close();
                if (logHistoryFileWriter != null)
                    logHistoryFileWriter.close();
                if (writer != null)
                    writer.close();

            }
            catch (IOException e) {
                  logger.error(e);
            }
            finally{
            	if (writer != null){
            	try{
            		 writer.close();
            	}catch(Exception e){
            		logger.error(e);
            	}
            	}
            }
        }
    }

    private void separateFiles(String files[]) {

        /* separating the files in different arrays using vector */
        for (int i = 0; i < files.length; i++) {
            if (files[i].startsWith(DOMESTIC)) {
                domesticFilesList.add(files[i]);
                if (logger.isInfoEnabled())
                    logger.info("DOMESTIC:  " + files[i]);
            }
            else if (files[i].startsWith(INTERNATIONAL)) {
                internationalFilesList.add(files[i]);
                if (logger.isInfoEnabled())
                    logger.info("INTERNATIONAL:  " + files[i]);
            }
        }
        setF_OUT_DateProcessed(new Timestamp(processedDate.getTime()));
    }

    private void DomesticFiles(BufferedWriter outputFile, BufferedWriter logHistoryOutputFile, BankFusionEnvironment env) {
        if (logger.isInfoEnabled())
            logger.info("DomesticFiles..");
        Object[] am4Array = null;
        VectorTable domestic = new VectorTable();
        int domesticFilesCount = domesticFilesList.size();
        am4Array = domesticFilesList.toArray();

        am4Array = getSortedList(am4Array, lastDomesticFileProcessed);
        for (int i = 0; i < domesticFilesCount; i++) {
            HashMap files = new HashMap();
            String key = "FileName";
            Object value = am4Array[i];
            files.put(key, value);
            domestic.addAll(new VectorTable(files));
        }
        int numberOfDomRows = domestic.size();
        setF_OUT_ListOfDomesticFiles(domestic);
        setF_OUT_numberOfDomesticRows(numberOfDomRows);
        fileType = "D";

        if (isF_IN_ConsolidationRequired()) {
            consolidateFiles(am4Array, fileType, domesticFilesCount, dirPath, outputFile, logHistoryOutputFile, env);
        }

    }

    private void InternationalFiles(BufferedWriter outputFile, BufferedWriter logHistoryOutputFile, BankFusionEnvironment env) {

        Object[] tf4Array = null;
        VectorTable international = new VectorTable();
        int internationalFilesCount = internationalFilesList.size();
        tf4Array = internationalFilesList.toArray();
        tf4Array = getSortedList(tf4Array, lastInterFileProcessed);
        for (int i = 0; i < internationalFilesCount; i++) {
            HashMap files = new HashMap();
            String key = "FileName";
            Object value = tf4Array[i];
            files.put(key, value);
            international.addAll(new VectorTable(files));
        }
        int numberOfIntRows = international.size();

        setF_OUT_ListOfInternationalFiles(international);
        setF_OUT_numberOfIntRows(numberOfIntRows);
        fileType = "I";

        if (isF_IN_ConsolidationRequired()) {
            consolidateFiles(tf4Array, fileType, internationalFilesCount, dirPath, outputFile, logHistoryOutputFile, env);
        }

    }

    /*
     * Sort files - First all the files greater than the date of last file processed (with Julian
     * date extension) then the remaining files. E.g., Last processed date (Julian)is 364. Four
     * files with extension 001, 002, 365 and 366 are come for processing on January 02. Files
     * should be processed in this order - 365, 366, 001 and 002
     */
    private Object[] getSortedList(Object[] files, int lastProcessedFile) {
        TreeMap currentMap = new TreeMap();
        TreeMap nextYearMap = new TreeMap();
        String fileName = "";

        for (int i = 0, noOfFiles = files.length; i < noOfFiles; i++) {

            fileName = files[i].toString().substring(files[i].toString().length() - 3, files[i].toString().length());
            if (Integer.parseInt(fileName) <= lastProcessedFile) {

                nextYearMap.put(fileName, files[i]);
            }
            else {
                currentMap.put(fileName, files[i]);
            }
        }
        ArrayList list = new ArrayList();

        list.addAll(currentMap.values());
        list.addAll(nextYearMap.values());
        return list.toArray();
    }

    private void consolidateFiles(Object files[], String fileType, int size, String path, BufferedWriter outputFile,
            BufferedWriter logHistoryOutputFile, BankFusionEnvironment env) {
        if (logger.isInfoEnabled())
            logger.info("ConsolidateFiles..");
        FileReader input = null;
        BufferedReader fileReader=null;
        try {

            for (int i = 0; i < size; i++) {
                input = new FileReader(path + files[i]);
                fileReader = new BufferedReader(input);

                int succesfultxns = 0;
                int failledtxns = 0;
                int txnsPostedTosuspense = 0;
                String fileName = files[i].toString();
                String fileProcessedSuccesfully = "Y";
                String txnValidatedSuccessfully = "Y";

                while (null != (message = fileReader.readLine())) {

                    errorMessage = CommonConstants.EMPTY_STRING;
                    txnValidatedSuccessfully = "Y";

                    String accountID = message.substring(38, 52);
                    String drCrFlag = message.substring(100, 102);
                    String drCrSign = "";
                    if (drCrFlag.equals("CR")) {
                        drCrFlag = "C";
                        drCrSign = "+";
                    }
                    else {
                        drCrFlag = "D";
                        drCrSign = "-";
                    }

                    boolean isValidForSuspense = false;

                    /*
                     * Validate Card Number, Account Number and it's mapping with Card Number and
                     * account currency = billing currency If any of these validation fails write
                     * the record to Repair Queue table
                     */
                    boolean isValid = validateMessageDetails(message, accountID, env);
                    if (!isValid) {
                        failledtxns = failledtxns + 1;
                        fileProcessedSuccesfully = "N";
                        txnValidatedSuccessfully = "N";
                        updateRepairQueueTable(message, fileType, env);
                        updateLogHistoryFile(message, fileType, accountID, txnValidatedSuccessfully, isValidForSuspense, fileName,
                                logHistoryOutputFile, env);
                        continue;
                    }
                    else {
                        /*
                         * Total number of transactions posted
                         */
                        succesfultxns = succesfultxns + 1;
                    }
                    /*
                     * Validate Account Password, Account Dormancy and Closed Account. If any of
                     * these validation fails, post the transaction to respective configured DR or
                     * CR suspense account.
                     */
                    if (messageValidator.isAccountExist(accountID, env)) {
                        String suspenseAccountID = getSuspenseAccount(message, fileType, env);
                        if (!messageValidator.isAccountValid(accountID, env)) {
                            isValidForSuspense = true;
                            fileProcessedSuccesfully = "N";
                            txnValidatedSuccessfully = "N";
                            message = message.substring(0, 38) + suspenseAccountID + message.substring(51, message.length());
                            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407584,
                                    new Object[] { accountID }, BankFusionThreadLocal.getUserSession().getUserLocale());
                            errorStatus = ATMConstants.ERROR;
                            logger.error(errorStatus + ": " + errorMessage);
                        }
                        else if (messageValidator.isAccountPasswordProtected(accountID, drCrFlag, env)) {
                            isValidForSuspense = true;
                            fileProcessedSuccesfully = "N";
                            txnValidatedSuccessfully = "N";
                            message = message.substring(0, 38) + suspenseAccountID + message.substring(51, message.length());
                            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407582,
                                    new Object[] { accountID }, BankFusionThreadLocal.getUserSession().getUserLocale());
                            errorStatus = ATMConstants.ERROR;
                            logger.error(errorStatus + ": " + errorMessage);
                        }
                        else if (!messageValidator.accountDormantPostingAction(accountID, fileType, env)) {
                            isValidForSuspense = true;
                            fileProcessedSuccesfully = "N";
                            txnValidatedSuccessfully = "N";
                            message = message.substring(0, 38) + suspenseAccountID + message.substring(51, message.length());
                            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40507030,
                                    new Object[] { accountID }, BankFusionThreadLocal.getUserSession().getUserLocale());
                            errorStatus = ATMConstants.ERROR;
                            logger.error(errorStatus + ": " + errorMessage);

                        }
                        else if (messageValidator.getSettlementAccount(settlementaccount, env)) {
                            isValidForSuspense = false;
                            fileProcessedSuccesfully = "N";
                            txnValidatedSuccessfully = "N";
                            message = message.substring(0, 38) + suspenseAccountID + message.substring(51, message.length());
                            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40311514, new Object[] {},
                                    BankFusionThreadLocal.getUserSession().getUserLocale());
                            errorStatus = ATMConstants.ERROR;
                            logger.error(errorStatus + ": " + errorMessage);
                        }

                        if (isValidForSuspense) {
                            txnsPostedTosuspense = txnsPostedTosuspense + 1;
                        }
                    }

                    if (!isValidForSuspense) {
                        BigDecimal billingAmount = (new BigDecimal(message.substring(83, 99)));
                        String billingCurrency = messageValidator.getAlphaCurrencyCode(message.substring(77, 80), env);

                        /**
                         * Validate Group Limit details.
                         */
                        if (!(messageValidator.checkLimits(billingAmount, accountID, billingCurrency, drCrSign, env))) {
                            errorMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40507019, env,
                                    null);
                        }

                        /**
                         * Validate account limit.
                         */
                        if (!(messageValidator.checkAccountLimits(billingAmount, accountID, billingCurrency, drCrSign, env))) {
                            errorMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40507019, env,
                                    null);
                        }
                    }
                    if (errorMessage == "") {
                        if (logger.isInfoEnabled())
                            logger.info("errorMessage blank..");
                        succesfultxns = 1;
                        failledtxns = 0;
                    }
                    if (errorMessage != "") {
                        if (logger.isInfoEnabled())
                            logger.info("errorMessage not blank..");
                        txnValidatedSuccessfully = "N";
                        fileProcessedSuccesfully = "N";
                        succesfultxns = 0;
                        failledtxns = 1;
                    }

                    updateLogHistoryFile(message, fileType, accountID, txnValidatedSuccessfully, isValidForSuspense, fileName,
                            logHistoryOutputFile, env);
                    message = message.substring(0, 296) + fileType;
                    StringTokenizer tokenizer = new StringTokenizer(message, "\n");

                    StringBuffer temp = new StringBuffer("");
                    while (tokenizer.hasMoreTokens()) {
                        temp.append(tokenizer.nextToken());
                    }
                    temp.append("\n");
                    outputFile.write(temp.toString());

                }
                updateFileHistoryTable(succesfultxns, failledtxns, txnsPostedTosuspense, fileName, fileType,
                        fileProcessedSuccesfully, env);

                fileReader.close();
                input.close();

                File f = new File(path + files[i]);
                String path2 = path + "CardTechProcessedFiles/";
                f.renameTo(new File(path2 + files[i]));

            }
        }
        catch (Exception e) {
            logger.error(e);
        }finally{
        	if(input!=null){
        		try{
        			input.close();
        		}catch(Exception e){
        			logger.error(e);
        		}
        	}if(fileReader!=null){
        		try{
        			fileReader.close();
        		}catch(Exception e){
        			logger.error(e);
        		}
        	}
        	
        }
    }

    /*
     * Validate Card Number, Account Number and it's mapping with Card Number and account currency =
     * billing currency
     */
    private boolean validateMessageDetails(String message, String mainAccountID, BankFusionEnvironment env)
            throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("Module Configuration..");
        String cardNumber = message.substring(0, 16);
        String billingCurrencyNumericCode = message.substring(77, 80);
        String billingCurrency = messageValidator.getAlphaCurrencyCode(billingCurrencyNumericCode, env);

        if (!messageValidator.isCardNumberValid(cardNumber, env)) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407510, new Object[] { cardNumber },
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            return false;
        }
        else if (!messageValidator.isAccountExist(mainAccountID, env)) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407516, new Object[] {},
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            return false;
        }
        else if (!messageValidator.isAccountMappedToCard(cardNumber, mainAccountID, env)) {

            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407537,
                    new Object[] { cardNumber, mainAccountID }, BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            return false;
        }
        else if (!messageValidator.isCurrencyValid(mainAccountID, billingCurrency, env)) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407581, new Object[] {},
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            return false;
        }
        return true;
    }

    private void updateRepairQueueTable(String message, String fileType, BankFusionEnvironment env) throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("Module Configuration..");
        IBOCTLFailedTrans cardTechRepairQueue = (IBOCTLFailedTrans) env.getFactory().getStatelessNewInstance(
                IBOCTLFailedTrans.BONAME);

        String cardNumber = message.substring(0, 16);
        String cardHldrsBankName = message.substring(17, 37);
        String accountID = message.substring(38, 58);
        String billingCurrency = messageValidator.getAlphaCurrencyCode(message.substring(77, 80), env);
        BigDecimal billingAmount = (new BigDecimal(message.substring(83, 99)));
        int billingCurrencyDecimal = Integer.parseInt(message.substring(81, 82));
        String drCrFlag = message.substring(100, 102);
        /*
         * if (drCrFlag.equals("CR")) { drCrFlag = "C"; } else { drCrFlag = "D"; }
         */

        /* Date format - yyyyMMdd (e.g, 20070226) */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        java.util.Date postingDate = null;
        java.util.Date purchaseDate = null;
        try {
            postingDate = sdf.parse(message.substring(103, 111));
            purchaseDate = sdf.parse(message.substring(112, 120));

        }
        catch (ParseException e) {
            // throw business exception saying posting date format incorrect
            logger.error(e);
              
        }
        String txnCurrency = messageValidator.getAlphaCurrencyCode(message.substring(121, 124), env);
        BigDecimal txnAmount = new BigDecimal(message.substring(125, 141));
        String settlementCurrency = messageValidator.getAlphaCurrencyCode(message.substring(142, 145), env);
        BigDecimal settlementAmount = new BigDecimal(message.substring(146, 162));
        BigDecimal settlementRate = new BigDecimal(message.substring(163, 172)).movePointLeft(5);
        String messageDescription = message.substring(173, 198);
        String merchantCity = message.substring(199, 212);
        String merchantCountry = message.substring(213, 216);
        String txnTime = message.substring(290, 296);
        cardTechRepairQueue.setF_ATMCARDNUMBER(cardNumber);
        cardTechRepairQueue.setF_BANKNAME(cardHldrsBankName);
        cardTechRepairQueue.setF_ACCOUNTID(accountID);
        cardTechRepairQueue.setF_ATMCARDCURRENCYCODE(billingCurrency);
        cardTechRepairQueue.setF_BILLAMOUNT(billingAmount);
        cardTechRepairQueue.setF_BILLAMOUNTDECIMALS(billingCurrencyDecimal);
        cardTechRepairQueue.setF_TRANSFLAG(drCrFlag);
        if (postingDate != null){
        cardTechRepairQueue.setF_POSTINGDATE(new java.sql.Date(postingDate.getTime()));
        }
        if (purchaseDate != null){
        cardTechRepairQueue.setF_PURCHASEDATE(new java.sql.Date(purchaseDate.getTime()));
        }
        cardTechRepairQueue.setF_TRANSCURRENCYCODE(txnCurrency);
        cardTechRepairQueue.setF_TRANSAMOUNT(txnAmount);
        cardTechRepairQueue.setF_SETTLEMENTCURRENCYCODE(settlementCurrency);
        cardTechRepairQueue.setF_SETTLEMENTAMOUNT(settlementAmount);
        cardTechRepairQueue.setF_SETTLEMENTRATE(settlementRate);
        cardTechRepairQueue.setF_MESSAGEDESC(messageDescription);
        cardTechRepairQueue.setF_MERCHANTCITY(merchantCity);
        cardTechRepairQueue.setF_MERCHANTCOUNTRYCODE(merchantCountry);
        cardTechRepairQueue.setF_TRANSACTIONTIME(txnTime);
        cardTechRepairQueue.setF_ERRORDESC(errorMessage);
        cardTechRepairQueue.setF_PROCESSEDDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
        cardTechRepairQueue.setF_RECORDTYPE(fileType);
        env.getFactory().create(IBOCTLFailedTrans.BONAME, cardTechRepairQueue);

    }

    private void updateLogHistoryFile(String message, String fileType, String accountID, String processingStatus,
            boolean postedToSuspense, String fileName, BufferedWriter logHistoryOutputFile, BankFusionEnvironment env)
            throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("updateLogHistoryFile.");
        String suspenseMessage = "";
        if (postedToSuspense == true) {
            suspenseMessage = POSTED_TO_SUSPENSE;
        }
        String successMessage = FAILED;
        if ((processingStatus.equals("Y"))) {
            successMessage = SUCCESS;
        }
        String messageDescription = message.substring(173, 198);
        int billingCurrencyDecimal = Integer.parseInt(message.substring(81, 82));
        BigDecimal billingAmount = (new BigDecimal(message.substring(83, 99))).movePointLeft(billingCurrencyDecimal);
        String billingCurrency = messageValidator.getAlphaCurrencyCode(message.substring(77, 80), env);
        String cardNumber = message.substring(0, 16);
        String postingDate = message.substring(103, 111);
        String DrCrFlag = message.substring(100, 102);

        String finalRecord = messageDescription + " | " + successMessage + " | " + errorMessage + " | " + billingAmount
                + billingCurrency + " | " + suspenseMessage + " | " + cardNumber + " | " + accountID + " | " + postingDate + " | "
                + fileName + " | " + DrCrFlag;
        try {
            StringTokenizer tokenizer = new StringTokenizer(finalRecord, "\n");

            StringBuffer temp = new StringBuffer("");
            while (tokenizer.hasMoreTokens()) {
                temp.append(tokenizer.nextToken());
            }
            temp.append("\n");
            logHistoryOutputFile.write(temp.toString());
        }
        catch (IOException e) {
            logger.error(e);
        }
    }

    private void updateFileHistoryTable(int succesfultxns, int failledtxns, int txnsPostedTosuspense, String fileName,
            String fileType, String fileProcessedSuccesfully, BankFusionEnvironment env) throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("updateFileHistoryTable.");
        IBOCTLFileHistory fileHistory = (IBOCTLFileHistory) env.getFactory().getStatelessNewInstance(IBOCTLFileHistory.BONAME);
        fileHistory.setF_TOTALSUCCESSTRANS(succesfultxns);
        fileHistory.setF_TOTALFAILEDTRANS(failledtxns);
        fileHistory.setF_TRANSPOSTTOSUSPENSE(txnsPostedTosuspense);
        fileHistory.setF_FILENAME(fileName);
        fileHistory.setF_FILETYPE(fileType.substring(0, 1));
        fileHistory.setF_FILEPROCESSDATE(new Timestamp(processedDate.getTime()));
        fileHistory.setF_FILEPROCESSEDSTATUS(fileProcessedSuccesfully);
        env.getFactory().create(IBOCTLFileHistory.BONAME, fileHistory);

    }

    private void getModuleConfDetails(BankFusionEnvironment env) throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("GetModuleConfDetails..");
        crSuspenseAccount = getParamValueFromModuleConfiguration(CARDTECH_MODULE_NAME, CRADTECH_CREDIT_SUSPENSE_ACC, env);
        drSuspenseAccount = getParamValueFromModuleConfiguration(CARDTECH_MODULE_NAME, CRADTECH_DEBIT_SUSPENSE_ACC, env);
        settlementaccount = getParamValueFromModuleConfiguration(CARDTECH_MODULE_NAME, CRADTECH_SETTLEMENTACCOUNT, env);
        dirPath = getParamValueFromModuleConfiguration(CARDTECH_MODULE_NAME, CRADTECH_PARAM_NAME, env);
        String lastDomFileProcessed = getParamValueFromModuleConfiguration(CARDTECH_MODULE_NAME,
                CRADTECH_LASTDOMESTIC_FILEPROCESSED, env);
        lastDomesticFileProcessed = Integer.parseInt(lastDomFileProcessed.substring(lastDomFileProcessed.length() - 3,
                lastDomFileProcessed.length()));

        String lastinterFileProcessed = getParamValueFromModuleConfiguration(CARDTECH_MODULE_NAME,
                CRADTECH_LASTINTERNATIONAL_FILEPROCESSED, env);
        lastInterFileProcessed = Integer.parseInt(lastinterFileProcessed.substring(lastinterFileProcessed.length() - 3,
                lastinterFileProcessed.length()));

    }

    private String getSuspenseAccount(String message, String fileType, BankFusionEnvironment env) throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("GetSuspenseAccount.");
        String suspenseAccount;
        String accountID = null;
        String drCrFlag = message.substring(100, 102);
        String billingCurrency = messageValidator.getAlphaCurrencyCode(message.substring(77, 80), env);
        if (drCrFlag.equals("CR")) {
            suspenseAccount = crSuspenseAccount;
        }
        else {
            suspenseAccount = drSuspenseAccount;
        }
        PseudoNameFeature pseudoNameFeature = new PseudoNameFeature(env);
        pseudoNameFeature.setF_IN_CURRENCY(billingCurrency);
        pseudoNameFeature.setF_IN_PSEUDONAME(suspenseAccount);
        pseudoNameFeature.setF_IN_context("CURRENCY");
        try {
            pseudoNameFeature.process(env);
            accountID = pseudoNameFeature.getF_OUT_ACCOUNTID();
        }
        catch (Exception exception) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407583,
                    new Object[] { suspenseAccount, billingCurrency }, BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            updateRepairQueueTable(message, fileType, env);
            accountID = CommonConstants.EMPTY_STRING;
            logger.error(exception);
        }
        return accountID;
    }

    private static String getParamValueFromModuleConfiguration(String moduleName, String paramName, BankFusionEnvironment env)
            throws BankFusionException {
        if (logger.isInfoEnabled())
            logger.info("GetParamValueFromModuleConfiguration.");
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        String paramValue = ((String) (ubInformationService.getBizInfo().getModuleConfigurationValue(moduleName, paramName, env)));
        return paramValue;
    }

}
