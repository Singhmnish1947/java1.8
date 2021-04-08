/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMBalanceDownloadFatom.java,v $
 * Revision 1.4  2008/08/12 20:14:11  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.2.4.5  2008/07/16 17:48:55  sushmax
 * Corrected the header
 *
 * Revision 1.2.4.4  2008/07/16 16:13:01  varap
 * Code cleanup - CVS revision tag added.
 *
 * Revision 1.2.4.3  2008/07/16 15:58:54  varap
 * Bug fix for issue 11129.
 *
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOATMAccountDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMBalanceDownloadFatom;

/**
 * The ATMBalanceDownloadFatom processes the accounts in ATMAccountDetails table and produces the
 * message format of the balances to be downloaded.
 */
public class ATMBalanceDownloadFatom extends AbstractATMBalanceDownloadFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     */

    /**
     * Holds the reference for logger object
     */
    private transient final static Log logger = LogFactory.getLog(ATMBalanceDownloadFatom.class.getName());

    private static final String ACCOUNTID = "accountid";

    private static final String BOOKEDBALANCE = "bookedbalance";

    private static final String BRANCHSORTCODE = "branchsortcode";

    private static final String ISOCURRENCYCODE = "isocurrencycode";
    private static int LENGTH_OF_AMOUNT = 14;
    private static int LENGTH_OF_BRANCH = 4;
    private static int LENGTH_OF_IMD = 6;
    private static int LENGTH_OF_NOOFBALANCES = 2;
    private String ONLINE_DOWNLOAD_MESSAGE = "987";
    private String ONLINE_NEEDNEXT_MESSAGE = "929";

    private ATMHelper atmHelper = new ATMHelper();

    /*
     * This Sql will be Used to Initilize Balance Download for Moved Balance Download.
     */
    private static final String initilizeMovedBalanceDownloadsql = "where " + IBOATMAccountDetails.BALANCEMOVEDFLAG + " = ?";
    /*
     * This Sql will be Used to Select Accounts where Balance Download is Y.
     */
    private static final String getMovedAccountsSql = "SELECT " + "acc." + IBOAttributeCollectionFeature.ACCOUNTID + " AS "
            + ACCOUNTID + ", acc." + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " AS " + ISOCURRENCYCODE + ", acc."
            + IBOAttributeCollectionFeature.BOOKEDBALANCE + " AS " + BOOKEDBALANCE + ", acc."
            + IBOAttributeCollectionFeature.BRANCHSORTCODE + " AS " + BRANCHSORTCODE + " FROM "
            + IBOAttributeCollectionFeature.BONAME + " acc " + " WHERE acc." + IBOAttributeCollectionFeature.ACCOUNTID + " IN "
            + " (SELECT atmacc." + IBOATMAccountDetails.ACCOUNTID + " FROM " + IBOATMAccountDetails.BONAME + " atmacc WHERE "
            + IBOATMAccountDetails.BALANCEDOWNLOADFLAG + " = ?" + ")" + " ORDER BY " + IBOAttributeCollectionFeature.BRANCHSORTCODE;

    private static final int MAX_PAGE_SIZE = 10;
    /**
     * Stores the bankfusion environment
     */
    private BankFusionEnvironment environment;

    private boolean isOnlineDownload = false;
    private boolean isSolicitedDownload = false;
    private boolean isFullDownload = false;
    private boolean isNeedNextMessage = false;

    /**
     * Stores the transaction code of the message ( message type + transaction type)
     */
    private String messageType = null;
    private Integer lengthOfAccount = 0;

    /**
     * Holds the configuration details
     */
    private ATMControlDetails controlDetails = null;

    /**
     * Stores the number of records for balance download
     */
    private int recordsUpdated = 0;

    public ATMBalanceDownloadFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * This method gets the configuration details and processes the atm accounts
     */
    public void process(BankFusionEnvironment env) {
        environment = env;
        getATMConfigurationDetails(env);
        if (isF_IN_Initialiser().equals(new Boolean(true))) {
            if (!isNeedNextMessage) {
                initilizeBalanceDownload();
            }
        }
        if (isF_IN_Initialiser().equals(new Boolean(false)) || (!isOnlineDownload)) {
            processBalanceDownload(env);
        }
    }

    /**
     * This method gets the configuration details
     */
    public void getATMConfigurationDetails(BankFusionEnvironment env) {
        // get ATM configuration details
        controlDetails = ATMConfigCache.getInstance().getInformation(env);
        lengthOfAccount = controlDetails.getDestAccountLength();
        messageType = getF_IN_MessageType() + getF_IN_TransactionType();
        isSolicitedDownload = controlDetails.isSolicitedMessageFlag();
        if (messageType.equals(ONLINE_DOWNLOAD_MESSAGE)) {
            isOnlineDownload = true;
            if (controlDetails != null) {
                if (isSolicitedDownload) {
                    setF_OUT_NeedNextMsg(new Boolean(true));
                }
                else {
                    setF_OUT_NeedNextMsg(new Boolean(false));
                }
                if (controlDetails.getBalanceDownloadType().equals("0")) {
                    isFullDownload = true;
                    // 0=Full Download
                    // 1=Moved Download
                }
            }
        }
        else if (messageType.equals("ONLINE_NEEDNEXT_MESSAGE")) {
            isNeedNextMessage = true;
            isOnlineDownload = true;
            setF_OUT_NeedNextMsg(new Boolean(true));
            setF_OUT_MessageIsNeedNext(new Boolean(true));
            if (controlDetails.getBalanceDownloadType().equals("0")) {
                isFullDownload = true;
            }
        }
        else {
            isOnlineDownload = false;
            if (controlDetails.getBalanceDownloadType().equals("0")) {
                isFullDownload = true;
            }
        }
    }

    /**
     * This Function will Initilize the Balance Download Process. It will First Set the
     * BalanceDownloadFlag to true and will then populate the list of accounts for which Balance
     * Download needs to be performed.
     *
     * In ATMAccountDetails, check for the BalanceMovedFlag and BalanceDownloadFlag.
     *
     * For Full Download, Set the BalanceDownloadFlag = 'Y' then populate the list of accounts for
     * which Balance Download needs to be performed. ie. all accounts
     *
     * For Moved Download, Update BalanceDownloadFlag = ‘Y’ for all accounts for which balance has
     * moved (BalanceMovedFlag = 'Y'). Update BalanceDownloadFlag = ‘N’ for all accounts for which
     * balance has not moved (BalanceMovedFlag = 'N').
     */
    public void initilizeBalanceDownload() {

        if (isFullDownload) {
            ArrayList columnList = new ArrayList();
            columnList.add(IBOATMAccountDetails.BALANCEDOWNLOADFLAG);

            ArrayList valuesList = new ArrayList();
            valuesList.add(new Boolean(true));

            recordsUpdated = environment.getFactory().bulkUpdate(IBOATMAccountDetails.BONAME, columnList, valuesList);
        }
        else {
            ArrayList params = null;
            List valuesList = null;
            List columnList = null;

            // Updating Downloaded Status as Yes for all balances for which Balance has not Moved
            params = new ArrayList();
            valuesList = new ArrayList();
            columnList = new ArrayList();

            columnList.add(IBOATMAccountDetails.BALANCEDOWNLOADFLAG);
            params.add(new Boolean(true));
            valuesList.add(new Boolean(true));
            recordsUpdated = environment.getFactory().bulkUpdate(IBOATMAccountDetails.BONAME, initilizeMovedBalanceDownloadsql,
                    params, columnList, valuesList);
            // Updating Downloaded Status as No for all balances for which Balance has Moved

            params = new ArrayList();
            valuesList = new ArrayList();
            columnList = new ArrayList();

            columnList.add(IBOATMAccountDetails.BALANCEDOWNLOADFLAG);
            params.add(new Boolean(false));
            valuesList.add(new Boolean(false));

            environment.getFactory().bulkUpdate(IBOATMAccountDetails.BONAME, initilizeMovedBalanceDownloadsql, params, columnList,
                    valuesList);
        }
        setF_IN_TotalAccsFromInit(String.valueOf(recordsUpdated));
    }

    /**
     * For Full balance download, Create a query which selects records from ATMAccount details table
     * where BalanceDownloadFlag = 'Y' ATMAccountDetails.AccountId = and Account.AccountId and
     * Account.BranchSortCode = Branch.BranchSortCode and Branch.IMDCOde = Banks.IMDCode sorted on
     * branchcode( from the branch number).
     * 
     * Iterate on the resultset for 10 records or till End of File and call generateMessage().
     * 
     * Update all columns in ATMAccountdetails table for the accounts downloaded. Set
     * BalanceDownloadFlag = 'N' for these accounts. Set BalanceMovedFlag = 'N' for these accounts.
     * 
     * Set ContinueFlag = false if all the accounts are downloaded.
     * 
     * For Moved balance download, Create a query which selects records from ATMAccount details
     * table where BalanceDownloadFlag = 'Y' and BalanceMovedFlag = 'Y' ATMAccountDetails.AccountId
     * = and Account.AccountId and Account.BranchSortCode = Branch.BranchSortCode and Branch.IMDCOde
     * = Banks.IMDCode sorted on branchcode( from the branch number).
     * 
     * Iterate on the resultset for 10 records or till End of File and call generateMessage().
     * 
     * Update all columns in ATMAccountdetails table for the accounts downloaded. Set
     * BalanceDownloadFlag = 'N' for these accounts. Set BalanceMovedFlag = 'N' for these accounts.
     * Set ContinueFlag = false if all the accounts are downloaded.
     * 
     * @param env
     * @
     */

    private void processBalanceDownload(BankFusionEnvironment env) {

        VectorTable vectorTable = new VectorTable();
        String previousBranchSortCode = CommonConstants.EMPTY_STRING;
        String branchSortCode = CommonConstants.EMPTY_STRING;

        int recordCount = 0;
        int remainingRecs = 0;
        ArrayList params = new ArrayList();
        params.add(String.valueOf('Y'));
        Iterator iterator = environment.getFactory().executeGenericQuery(getMovedAccountsSql, params, MAX_PAGE_SIZE);
        HashMap attributes = new HashMap();
        while (iterator.hasNext()) {

            SimplePersistentObject simplePersistentObject = (SimplePersistentObject) iterator.next();
            String accountID = (String) simplePersistentObject.getDataMap().get(ACCOUNTID);
            logger.debug("accountID " + accountID);
            branchSortCode = (String) simplePersistentObject.getDataMap().get(BRANCHSORTCODE);
            String currCode = (String) simplePersistentObject.getDataMap().get(ISOCURRENCYCODE);
            if (previousBranchSortCode.equals(CommonConstants.EMPTY_STRING)) {
                previousBranchSortCode = branchSortCode;
            }
            if (!branchSortCode.equals(previousBranchSortCode)) {
                break;
            }
            else {
                BigDecimal bookBalance = (BigDecimal) simplePersistentObject.getDataMap().get(BOOKEDBALANCE);
                BigDecimal availableBalance = atmHelper.getAvailableBalance(accountID, environment);
                String availableBalanceSign = atmHelper.getSign(availableBalance);
                String bookBalanceSign = atmHelper.getSign(bookBalance);
                String convertedAvailableBalance = atmHelper.getScaledAmount(availableBalance, currCode, LENGTH_OF_AMOUNT);
                String convertedBookBalance = atmHelper.getScaledAmount(bookBalance, currCode, LENGTH_OF_AMOUNT);
                String status = atmHelper.leftPad((atmHelper.getStatus(accountID, environment) + CommonConstants.EMPTY_STRING), "0",
                        2);

                attributes.put("AccountNumber", accountID);
                attributes.put("AvailableBalance", availableBalanceSign + convertedAvailableBalance);
                attributes.put("BookBalance", bookBalanceSign + convertedBookBalance);
                attributes.put("Status", status);

                // Update ATMAccountDetails
                IBOATMAccountDetails atmAccountDetails = (IBOATMAccountDetails) environment.getFactory()
                        .findByPrimaryKey(IBOATMAccountDetails.BONAME, accountID);
                atmAccountDetails.setF_SENTSTATUS(Integer.parseInt(status));
                atmAccountDetails.setF_ATMBOOKBALANCE(bookBalance);
                atmAccountDetails.setF_ATMCLEAREDBALANCE(availableBalance);
                atmAccountDetails.setF_BALANCEDOWNLOADFLAG(false);
                atmAccountDetails.setF_BALANCEMOVEDFLAG(false);
                atmAccountDetails.setF_ACCOUNTSTATUSDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
                atmAccountDetails.setF_PREVIOUSAVAILABLEBALANCE(availableBalance);
                atmAccountDetails.setF_PREVIOUSBOOKBALANCE(bookBalance);
                atmAccountDetails.setF_PREVIOUSACCOUNTSTATUS(Integer.parseInt(status));
                atmAccountDetails.setF_PREVIOUSBALANCEDOWNLOADDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
            }
            recordCount++;
            vectorTable.addAll(new VectorTable(attributes));
            if (recordCount == MAX_PAGE_SIZE) {
                break;
            }
        }

        String emptyAccount = atmHelper.leftPad("0000000000000", "0", lengthOfAccount);
        if (recordCount > 0) {
            if (recordCount < MAX_PAGE_SIZE) {

                remainingRecs = (MAX_PAGE_SIZE - recordCount);
                for (int cnt = 0; cnt < remainingRecs; cnt++) {
                    attributes.put("AccountNumber", emptyAccount);
                    attributes.put("AvailableBalance", "000000000000000");
                    attributes.put("BookBalance", "000000000000000");
                    attributes.put("Status", "00");
                    vectorTable.addAll(new VectorTable(attributes));
                }
            }
            generateMessage(previousBranchSortCode, recordCount, vectorTable);
            setF_OUT_ContinueFlag(Boolean.TRUE);
        }
        else {
            // Changed
            setF_OUT_ContinueFlag(Boolean.FALSE);
        }
        if (isNeedNextMessage == true) {
            setF_OUT_TotalNoofAccsDownloaded(
                    "+" + atmHelper.leftPad((String.valueOf(recordCount) + CommonConstants.EMPTY_STRING), "0", LENGTH_OF_AMOUNT));
            setF_OUT_TotalDownloadsForReports(String.valueOf(recordCount));
            setF_OUT_NoofRecs(new Integer(recordCount));
        }
        else {
            setF_OUT_TotalNoofAccsDownloaded(
                    "+" + atmHelper.leftPad((getF_IN_TotalAccsFromInit() + CommonConstants.EMPTY_STRING), "0", LENGTH_OF_AMOUNT));
            setF_OUT_TotalDownloadsForReports(getF_IN_TotalAccsFromInit());
        }

    }

    /**
     * This method generates the message with details of account downloaded
     *
     */
    private void generateMessage(String branchSortCode, int numOfRecords, VectorTable accountDetails) {
        String imdCode = CommonConstants.EMPTY_STRING;
        String branchNumber = CommonConstants.EMPTY_STRING;
        try {
            IBOBranch branchDetails = (IBOBranch) environment.getFactory().findByPrimaryKey(IBOBranch.BONAME, branchSortCode);
            imdCode = String.valueOf(branchDetails.getF_IMDCODE());
            branchNumber = branchDetails.getF_BMBRANCH();
        }
        catch (BankFusionException exception) {
            imdCode = "000000";
            branchNumber = "0000";
            logger.error(ExceptionUtil.getExceptionAsString(exception));
        }

        setF_OUT_MailBox("1");
        setF_OUT_IMD(atmHelper.leftPad(imdCode, "0", LENGTH_OF_IMD));
        setF_OUT_Branch(atmHelper.leftPad(branchNumber, "0", LENGTH_OF_BRANCH));
        setF_OUT_NoOfBalances(atmHelper.leftPad(String.valueOf(numOfRecords), "0", LENGTH_OF_NOOFBALANCES));
        setF_OUT_AccountDetails(accountDetails);
        setF_OUT_AccountInEndMessage(atmHelper.leftPad("9999999999999", "9", lengthOfAccount));
    }

}
