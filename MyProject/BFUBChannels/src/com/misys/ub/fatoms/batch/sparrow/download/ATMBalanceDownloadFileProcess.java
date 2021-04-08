/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * $Id: ATMBalanceDownloadFileProcess.java,v 1.1.4.4 2008/10/15 05:27:31 prashantk Exp $
 *
 * $Log: ATMBalanceDownloadFileProcess.java,v $
 * Revision 1.1.4.4  2008/10/15 05:27:31  prashantk
 * Update for BUG#12871.
 *
 * Revision 1.1.4.3  2008/10/14 23:35:55  prashantk
 * Update for BUG#12871. The Branch Details is being read from the ATMCIB  & ATMAccount BO rather than the Branch table
 *
 * Revision 1.1.4.2  2008/09/23 08:09:50  mangesh
 * BUGID - 12581 - new Batch process for processing ATM Balance Download.
 *
 *
 */
package com.misys.ub.fatoms.batch.sparrow.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.misys.cbs.common.util.log.CBSLogger;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.fatom.AbstractPersistableFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.bo.refimpl.IBOATMAccountDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountLimitFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ATMCIBTAG;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.boundary.outward.BankFusionPropertySupport;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.features.IFeature;
import com.trapedza.bankfusion.servercommon.products.ProductFactoryProvider;
import com.trapedza.bankfusion.servercommon.products.SimpleRuntimeProduct;

/**
 * This process reads all the accounts per branch and creates the download message required by the
 * sparrow interface. Each message consist of 10 accounts in the following structure.
 * MAILBOX // The value of this field should be "1".
 * IMDCODE // 6 digit IMD code for the branch sort code.
 * BRANCHNUMBER // This should be 4 digit Branch number.
 * ACCOUNT NUMBER //
 * BOOKBALANCE // 15
 * AVAILABLEBALANCE // 15
 * STATUS // 2
 *
 * The process create a new file based on the number worker set on batch process parameters.
 *
 * @author Mangesh Hagargi
 *
 */
public class ATMBalanceDownloadFileProcess extends AbstractBatchProcess {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     */
	private static final String CLASS_NAME = ATMBalanceDownloadFileProcess.class.getName();

	private static final transient CBSLogger logger = new CBSLogger(CLASS_NAME);

    private AbstractProcessAccumulator accumulator;

    private IPersistenceObjectsFactory factory = null;

    private static final String ACCOUNTID = "accountid";

    private static final String BOOKEDBALANCE = "bookedbalance";

    private static final String ISOCURRENCYCODE = "isocurrencycode";

    private static final String DORMANTSTATUS = "dormantstatus";

    private static final String STOPPED = "stopped";

    private static final String ACCRIGHTSINDICATOR = "accrightsindicator";

    private ATMBalanceDownloadFileContext balanceDownloadFileContext;

    private static int LENGTH_OF_AMOUNT = 14;

    private ATMHelper atmHelper = new ATMHelper();

    private static final int MESSAGE_SIZE = 10;

    // Open new file per process threads.
    private BufferedOutputStream atmDownloadFile = null;

    // New Fields added
    private static final String BLOCKEDBALANCE = "blockedbalance";
    private static final String PRODUCTID = "productid";
    private static final String CLEAREDBALANCE = "clearedbalance";

    private String accountID = "";
    private String productID = "";

    // Column added to calculate Available Balance.
    private static final String LIMIND = "limitindicator";
    private static final String CRLIMIT = "creditlimit";
    private static final String DRLIMIT = "debitlimit";
    private static final String LIMITFTR = "AccountLimitFeature";

    private BigDecimal crLimit = new BigDecimal(0);
    private BigDecimal drLimit = new BigDecimal(0);
    private int limitIndicator = 0;

    /*
     * This Sql will be Used to Select Accounts where Balance Download is Y.
     */

    private static final String getMovedAccountsSql = "SELECT " + "acc." + IBOAttributeCollectionFeature.ACCOUNTID + " AS "
            + ACCOUNTID + ", acc." + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " AS " + ISOCURRENCYCODE + ", acc."
            + IBOAttributeCollectionFeature.BOOKEDBALANCE + " AS " + BOOKEDBALANCE + ", acc."
            + IBOAttributeCollectionFeature.DORMANTSTATUS + " AS " + DORMANTSTATUS + ", acc."
            + IBOAttributeCollectionFeature.STOPPED + " AS " + STOPPED + ", acc." + IBOAttributeCollectionFeature.CLEAREDBALANCE
            + " AS " + CLEAREDBALANCE + ", acc." + IBOAttributeCollectionFeature.BLOCKEDBALANCE + " AS " + BLOCKEDBALANCE
            + ", acc." + IBOAttributeCollectionFeature.PRODUCTID + " AS " + PRODUCTID + ", acc."
            + IBOAttributeCollectionFeature.LIMITINDICATOR + " AS " + LIMIND
            + ", acc." + IBOAccount.CREDITLIMIT + " AS " + CRLIMIT + ", acc." + IBOAccount.DEBITLIMIT + " AS " + DRLIMIT + ", acc."
            + IBOAttributeCollectionFeature.ACCRIGHTSINDICATOR + " AS " + ACCRIGHTSINDICATOR + " FROM "
            + IBOAttributeCollectionFeature.BONAME + " acc,  " + IBOATMCardAccountMap.BONAME + " acm, "
            + IBOATMAccountDetails.BONAME + " atmacc " + " WHERE acc." + IBOAttributeCollectionFeature.ACCOUNTID + " = acm."
            + IBOATMCardAccountMap.ACCOUNTID + " and atmacc." + IBOATMAccountDetails.ACCOUNTID + "= acm."
            + IBOATMCardAccountMap.ACCOUNTID + " and atmacc." + IBOATMAccountDetails.BALANCEDOWNLOADFLAG + " = ? and atmacc."
            + IBOATMAccountDetails.CIBID + " = ?";

    private static final String whereClause = " WHERE " + IBOUB_ATMCIBTAG.UBROWSEQ + " between ? and ? ";

    /**
     * Query to Return the Alternate Account Number.
     */
    private static final String GET_ALTERNATE_ACC_NO = " WHERE " + IBOPseudonymAccountMap.ACCOUNTID + " = ? AND "
            + IBOPseudonymAccountMap.SORTCONTEXTVALUE + " = ? AND " + IBOPseudonymAccountMap.SORTCONTEXT + " = ?";



    private static final String EMPTY_ACCOUNT = "0000000000000";
    private static final String ZERO = "0";
    private static final String EMPTY_BALANCE = "000000000000000";
    private static final String EMPTY_STATUS = "00";

    // file name properties
    private static final String FILEDIR = "ATMDownload.FileDir";
    private static final String FILEPREFIX = "ATMBalances";
    private static final String FILEEXT = ".dwn";
    private static final String DOWNLOAD_FLAG = "Y";

    /**
	 * Constructor for ATM Balance Download Batch Process.
	 *
	 * Performs initialisation, sets context variables, inputTags and creates
	 * a new PagingData. This constructor needs to be used if running the
	 * process in multi-node environment.
	 *
	 * @param context A set of data passed to the PreProcess, Process and
	 * PostProcess classes
	 */
    public ATMBalanceDownloadFileProcess(AbstractPersistableFatomContext context) {
		super(context);
		this.context = (ATMBalanceDownloadFileContext) context;
		balanceDownloadFileContext = (ATMBalanceDownloadFileContext) context;

	}


    public ATMBalanceDownloadFileProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
        super(environment, context, priority);
        balanceDownloadFileContext = (ATMBalanceDownloadFileContext) context;
    }

    @Override
    public AbstractProcessAccumulator getAccumulator() {
        return accumulator;
    }

    @Override
    public void init() {
        initialiseAccumulator();

    }

    @Override
    protected void initialiseAccumulator() {
        Object[] accumulatorArgs = new Object[1];
        accumulatorArgs[0] = new Integer(0);
        accumulator = new ATMBalanceDownloadFileAccumulator(accumulatorArgs);
        accumulator.accumulateTotals(accumulatorArgs);

    }

    /**
     * Process all the accounts for the given branch sort code per page. The ATM Account details
     * should be updated with the new values with a configured commitPolicy.
     *
     * This method has two loops 1st to get the branch details and 2nd to get the account details
     * for this branch.
     *
     */
    @Override
    public AbstractProcessAccumulator process(int pageToProcess) {

        pagingData.setCurrentPageNumber(pageToProcess);
        factory = BankFusionThreadLocal.getPersistanceFactory();
        Timestamp businessDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
        StringBuffer recordBuffer = new StringBuffer();
        int numOfAccounts = 0;
        int numOfRecords = 0;

        Object[] totalRecords = new Object[1];
        totalRecords[0] = new Integer(0);

        factory = BankFusionThreadLocal.getPersistanceFactory();

        int pageSize = context.getPageSize();
        int fromValue = ((pageToProcess - 1) * pageSize) + 1;
        int toValue = pageToProcess * pageSize;

        ArrayList params1 = new ArrayList();
        params1.add(fromValue);
        params1.add(toValue);
        List branchList = factory.findByQuery(IBOUB_ATMCIBTAG.BONAME, whereClause, params1, null, false);

        Iterator branchIter = branchList.iterator();
        ArrayList params = new ArrayList();

        String processFileName = FILEPREFIX + pageToProcess + FILEEXT;

        openDownloadFile(processFileName);

        BigDecimal bookBalance = CommonConstants.BIGDECIMAL_ZERO;
        String currCode = CommonConstants.EMPTY_STRING;
        String bookBalanceSign = CommonConstants.EMPTY_STRING;
        String convertedBookBalance = CommonConstants.EMPTY_STRING;
        Integer rightsIndicator = 0;
        boolean accountStop = false;
        boolean dormant = false;
        BigDecimal availableBalance = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal clearBalance = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal blockedBalance = CommonConstants.BIGDECIMAL_ZERO;
        try {
            while (branchIter.hasNext()) {
                IBOUB_ATMCIBTAG branchDetails = (IBOUB_ATMCIBTAG) branchIter.next();
                // Get all the accounts for the branch sort code from the account and atm account
                // details. update atm account details
                // with the available balance.
                params.clear();
                params.add(DOWNLOAD_FLAG);
                params.add(branchDetails.getBoID());

                if (logger.isInfoEnabled())
                {
                    logger.info("process()"," Processing for Branch : " + branchDetails.getBoID());
                }

                /**
                 * Fetch account details for the Accounts in ATM.
                 */
                List<SimplePersistentObject> downloadDetailslst = (ArrayList) factory.executeGenericQuery(getMovedAccountsSql,
                        params, null, false);

                /**
                 * IF there are no Accounts for a given branch then return.
                 */
                if (downloadDetailslst == null || downloadDetailslst.isEmpty()) {
                	logger.warn("process()", "There are no accounts for the branch.");
                	continue;
                }

                /**
                 * Iterate through each account for the ATM branch.
                 */
                for (SimplePersistentObject downloadDetails : downloadDetailslst) {

                    accountID = (String) downloadDetails.getDataMap().get(ACCOUNTID);
                    currCode = (String) downloadDetails.getDataMap().get(ISOCURRENCYCODE);
                    bookBalance = (BigDecimal) downloadDetails.getDataMap().get(BOOKEDBALANCE);
                    bookBalanceSign = atmHelper.getSign(bookBalance);
                    convertedBookBalance = atmHelper.getScaledAmount(bookBalance, currCode, LENGTH_OF_AMOUNT);
                    accountStop = (Boolean) downloadDetails.getDataMap().get(STOPPED);
                    rightsIndicator = (Integer) downloadDetails.getDataMap().get(ACCRIGHTSINDICATOR);
                    dormant = (Boolean) downloadDetails.getDataMap().get(DORMANTSTATUS);
                    // get Product ID, Clear Balance, Blocked Balance
                    productID = (String) downloadDetails.getDataMap().get(PRODUCTID);
                    clearBalance = (BigDecimal) downloadDetails.getDataMap().get(CLEAREDBALANCE);
                    blockedBalance = (BigDecimal) downloadDetails.getDataMap().get(BLOCKEDBALANCE);
                    crLimit = (BigDecimal) downloadDetails.getDataMap().get(CRLIMIT);
                    drLimit = (BigDecimal) downloadDetails.getDataMap().get(DRLIMIT);
                    limitIndicator = (Integer) downloadDetails.getDataMap().get(LIMIND);

                    // get Available Balance
                    availableBalance = calculateAvailableBalance(clearBalance, blockedBalance);
                    String availableBalanceSign = atmHelper.getSign(availableBalance);
                    String convertedAvailableBalance = atmHelper.getScaledAmount(availableBalance, currCode, LENGTH_OF_AMOUNT);
                    String status = atmHelper.leftPad(getAccountStatus(accountStop, dormant, rightsIndicator, 0), ZERO, 2);

                    // Retrieve ATMAccountDetails for update
                    IBOATMAccountDetails atmAccountDetails = (IBOATMAccountDetails) factory.findByPrimaryKey(
                            IBOATMAccountDetails.BONAME, accountID);

                    // Changes for CRDB starts
                    accountID = getAlternateAccount(accountID, "SPARROWACCOUNT");
                    // changes for CRDB ends

                    // FIXME- Modified to sent cleared Balance, However this field must be moved
                    // based on Module Configuration - Book/Clear.
                    String clearBalanceSign = atmHelper.getSign(clearBalance);
                    String convertedClearBalance = atmHelper.getScaledAmount(clearBalance, currCode, LENGTH_OF_AMOUNT);
                    recordBuffer.append(accountID);
                    recordBuffer.append(clearBalanceSign);
                    recordBuffer.append(convertedClearBalance);
                    // recordBuffer.append(bookBalanceSign);
                    // recordBuffer.append(convertedBookBalance);
                    recordBuffer.append(availableBalanceSign);
                    recordBuffer.append(convertedAvailableBalance);
                    recordBuffer.append(status);
                    numOfAccounts++;
                    numOfRecords++;
                    int statusUpdate = Integer.parseInt(status);
                    // build message details
                    if (numOfAccounts == MESSAGE_SIZE) {
                        generateMessage(branchDetails, recordBuffer, numOfAccounts);
                        recordBuffer = new StringBuffer();
                        numOfAccounts = 0;

                    }

                    // Update the ATM Account details status.
                    atmAccountDetails.setF_SENTSTATUS(statusUpdate);
                    atmAccountDetails.setF_ATMBOOKBALANCE(bookBalance);
                    atmAccountDetails.setF_ATMCLEAREDBALANCE(availableBalance);
                    atmAccountDetails.setF_BALANCEDOWNLOADFLAG(false);
                    atmAccountDetails.setF_BALANCEMOVEDFLAG(false);
                    atmAccountDetails.setF_ACCOUNTSTATUSDATE(businessDate);
                    atmAccountDetails.setF_PREVIOUSAVAILABLEBALANCE(availableBalance);
                    atmAccountDetails.setF_PREVIOUSBOOKBALANCE(bookBalance);
                    atmAccountDetails.setF_PREVIOUSACCOUNTSTATUS(statusUpdate);
                    atmAccountDetails.setF_PREVIOUSBALANCEDOWNLOADDATE(businessDate);
                }

                /**
                 * A ATM File can contain only 10 accounts in one line of a file, if any of the line
                 * did not have 10 accounts , then fill the reminder by Empty spaces and ZERO.
                 */
                if (numOfAccounts > 0) {
                    if (numOfAccounts < MESSAGE_SIZE) {
                        int remaining = MESSAGE_SIZE - numOfAccounts;
                        for (int count = 0; count < remaining; count++) {
                            recordBuffer.append(EMPTY_ACCOUNT);
                            recordBuffer.append(EMPTY_BALANCE);
                            recordBuffer.append(EMPTY_BALANCE);
                            recordBuffer.append(EMPTY_STATUS);
                        }
                        generateMessage(branchDetails, recordBuffer, numOfAccounts);
                        recordBuffer = new StringBuffer();
                        numOfAccounts = 0;

                    }
                }
            }
        }
        finally {
            try {
                atmDownloadFile.flush();
            }
            catch (IOException e) {
            	logger.error("process()","IO Exception message:"+e.getLocalizedMessage());
                EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_WRITING_TO_THE_ATM_DOWNLOAD_FILE, new Object[] { e
                        .getMessage() }, new HashMap(), environment);
            }
            totalRecords[0] = numOfRecords;
            accumulator.accumulateTotals(totalRecords);
            closeDownloadFile();
        }

        return accumulator;
    }

    /**
     * Calculates the availableBalance Balance for a account.
     *
     * @param clearBalance
     * @param blockedBalance
     * @return
     */
    public BigDecimal calculateAvailableBalance(BigDecimal clearBalance, BigDecimal blockedBalance) {

        BigDecimal availableBalance = CommonConstants.BIGDECIMAL_ZERO;

        try {
            SimpleRuntimeProduct product = ProductFactoryProvider.getInstance().getProductFactory().getRuntimeProduct(productID,
                    BankFusionThreadLocal.getBankFusionEnvironment());
            IFeature avlFeature = product.getFeature(BankFusionThreadLocal.getBankFusionEnvironment(), LIMITFTR);
            //commented for artf693925
            //availableBalance = clearBalance.subtract(blockedBalance);
            // Limit Feature Attached check for limit Indicators
            if (avlFeature != null) {
                availableBalance = getLimitBalance(clearBalance, blockedBalance);
            }
            else {
                availableBalance = clearBalance.subtract(blockedBalance);
            }
            if (availableBalance.intValue()<= CommonConstants.INTEGER_ZERO){
		    	availableBalance= CommonConstants.BIGDECIMAL_ZERO;
		    }

        }
        finally {

        }

        return availableBalance;
    }

    /**
     * Gets the Available balance of the account and adds up the Debit or Credit Limit based on the
     * Limit Indicator.
     *
     * @param availableBalance
     * @return The available balance of the account.
     */
    private BigDecimal getLimitBalance(BigDecimal clearBalance, BigDecimal blockedBalance) {

        BigDecimal calculatedBalance = CommonConstants.BIGDECIMAL_ZERO;

        calculatedBalance = clearBalance;

        //Read Account Limit Details
        //Merger of BFUB-7780 Issue from AP9a
        Timestamp today = SystemInformationManager.getInstance().getBFBusinessDateTime();
    	IBOAccountLimitFeature accountLimitItem = (IBOAccountLimitFeature)BankFusionThreadLocal
    	.getPersistanceFactory().findByPrimaryKey(IBOAccountLimitFeature.BONAME, accountID, true);
    	Timestamp limitExpiryDate = accountLimitItem.getF_LIMITEXPIRYDATE();
    	Timestamp tempLimitExpiryDate = accountLimitItem.getF_TEMPLIMEXPIRYDATE();
    	BigDecimal tempLimit = accountLimitItem.getF_TEMPACCOUNTLIMIT();

    	if (limitExpiryDate.compareTo(today) <= 0){
    		//Still Check for Temporary Limit as Temporary Limit is now independent of regular limit
    		//If Temporary Limit is not expired and temporary limit is not zero then consider that
    		if (tempLimitExpiryDate.compareTo(today) > 0 && tempLimit.compareTo(CommonConstants.BIGDECIMAL_ZERO) > 0) {
    			calculatedBalance = clearBalance.subtract(blockedBalance).add(tempLimit);
    		} else {
    			calculatedBalance = clearBalance.subtract(blockedBalance);
    		}
    	}
    	else
    	{
	        switch (limitIndicator) {
	            case 0: // Cleared Balance - Blocked Balance
	                calculatedBalance = calculatedBalance.subtract(blockedBalance);
	                break;
	            case 1: // Cleared Balance + Debit limit - Blocked Balance
	                calculatedBalance = calculatedBalance.add(drLimit).subtract(blockedBalance);
	                break;

	            case 2: // Cleared Balance - Credit limit - Blocked Balance
	                calculatedBalance = calculatedBalance.subtract(crLimit).subtract(blockedBalance);
	                break;
	            case 3: // Cleared Balance + Debit limit - Blocked Balance
	                calculatedBalance = calculatedBalance.add(drLimit).subtract(blockedBalance);
	                break;

	            default:
	                calculatedBalance = CommonConstants.BIGDECIMAL_ZERO;
	                break;
	        }
    	}
        return calculatedBalance;
    }

    /**
     * Write the record for each account in the file.There can be only maximum of 10 records in one
     * line of the ATM File that is been created.
     *
     * @param branchDetails
     * @param recordBuffer
     * @param numOfAccounts
     */
    private void generateMessage(IBOUB_ATMCIBTAG branchDetails, StringBuffer recordBuffer, int numOfAccounts) {

        StringBuffer msgBuffer = new StringBuffer();
        msgBuffer.append(1);
        msgBuffer.append(atmHelper.leftPad(branchDetails.getF_UBIMDCODE(), ZERO, 6));
        msgBuffer.append(atmHelper.leftPad(branchDetails.getF_UBBMBRANCH(), ZERO, 4));
        msgBuffer.append(atmHelper.leftPad(String.valueOf(numOfAccounts), ZERO, 2));
        msgBuffer.append(recordBuffer.toString());
        msgBuffer.append("\n");

        try {
            atmDownloadFile.write(msgBuffer.toString().getBytes());

        }
        catch (IOException ioe) {
            // Use a proper Error Code.
            // throw new BankFusionException(7580,ioe.getMessage());
        	logger.error("generateMessage()","IO Exception is:"+ioe.getLocalizedMessage());
            EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_WRITING_TO_THE_ATM_DOWNLOAD_FILE, new Object[] { ioe
                    .getMessage() }, new HashMap(), environment);
        }
    }    /**
     * Determines the ATM account status based on the Account status.
     *
     * @param accountStop
     * @param dormant
     * @param rightsIndicator
     * @param accountStatus
     * @return
     */
    private String getAccountStatus(boolean accountStop, boolean dormant, Integer rightsIndicator, int accountStatus) {

        String currentStatus = "0";
        int atmStatus = 0;

        ATMControlDetails controlDetails = ATMConfigCache.getInstance().getInformation(
                BankFusionThreadLocal.getBankFusionEnvironment());

        if (accountStop) {
            currentStatus = (String) balanceDownloadFileContext.getAccountStatusForAccountIndicator().get(-9999);
        }

        if (dormant) {
            atmStatus = currentStatus.compareTo(controlDetails.getInactiveAccount());
            if (atmStatus < 0) {
                currentStatus = (String) balanceDownloadFileContext.getAccountStatusForAccountIndicator().get(-99991);
            }
        }

        String tempCurrentStatus = currentStatus;

        /**
         * Get the equivalent ATM status from the map for the Account Right Indicator
         */
        currentStatus = (String) balanceDownloadFileContext.getAccountStatusForAccountIndicator().get(rightsIndicator);

        atmStatus = tempCurrentStatus.compareTo(currentStatus);
        if (atmStatus > 0) {
            currentStatus = tempCurrentStatus;
        }
        // Checking for HOT Card Status.
        int tempStatus = new Integer(currentStatus).intValue();

        if (accountStatus > tempStatus) {
            tempStatus = accountStatus;
            currentStatus = String.valueOf(tempStatus);
        }
        return currentStatus;
    }

    /**
     * Creates a new File for the current running process and returns the Buffered Output Stream as
     * an output.
     *
     * @param fileNameOfATM
     */
    private void openDownloadFile(String fileNameOfATM) {

        // Get the file name from the properties file
        String path = BankFusionPropertySupport.getProperty(BankFusionPropertySupport.UB_PROPERTY_FILE_NAME, FILEDIR, "");
        String fileName = path + fileNameOfATM;
        File fout = new File(fileName);
        if (fout.exists()) {
            fout.delete();
        }
        BankFusionIOSupport.createNewFile(fout);
        atmDownloadFile = new BufferedOutputStream(BankFusionIOSupport.createBufferedOutputStream(fout, true));
    }

    /**
     * Closed the Downloaded File.
     */
    private void closeDownloadFile() {
        // TODO Auto-generated method stub
        if (atmDownloadFile != null) {
            try {
                atmDownloadFile.close();
            }
            catch (Exception e) {

                // ignore exception if unable to close.

            }
        }
    }

    /**
     * Fetches the Alternate Account
     *
     * @param accountId
     * @param context
     * @return
     */
    private String getAlternateAccount(String accountId, String context) {

        /**
         * For Sparrow Account , the Maximum length of account can be only 13. If the UB account is
         * 14 digit , then there would be a 13 digit Alternate account for the same 14 digit
         * account, If the UB account is 13 digit then , there is no need to check the alternate
         * account
         *
         * TODO: Need to put if the check is required in Module Configuration and then proceed.
         */
        if (accountId.length() > 13) {
            ArrayList params = new ArrayList();
            params.add(accountId);
            params.add(context);
            params.add("ALTERNATE");

            List<IBOPseudonymAccountMap> alternateAccount = factory.findByQuery(IBOPseudonymAccountMap.BONAME,
                    GET_ALTERNATE_ACC_NO, params, null, true);

            if (!alternateAccount.isEmpty()) {
                IBOPseudonymAccountMap accountMap = alternateAccount.get(0);
                return accountMap.getF_PSEUDONAME();
            }
        }
        return accountId;
    }
}
