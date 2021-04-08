package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.misys.bankfusion.common.util.Utils;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOUBVW_FORWARDITEMS;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.CurrencyValue;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_TransactionListEnquiry;
import com.trapedza.bankfusion.steps.refimpl.IBPW_TransactionListEnquiry;

public class BPW_TransactionListEnquiry extends AbstractBPW_TransactionListEnquiry implements IBPW_TransactionListEnquiry {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final int MAX_TRANSACTIONS_REQUIRED = 10;

    public BPW_TransactionListEnquiry(BankFusionEnvironment env) {
        super(env);
    }

//    private transient final static Log logger = LogFactory.getLog(BPW_TransactionListEnquiry.class.getName());
    private static String MINISTMT_WHERECLAUSE = "where " + IBOUBVW_FORWARDITEMS.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
            + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " BETWEEN ? AND ? AND " + IBOUBVW_FORWARDITEMS.STATEMENTFLAG + " = ?  AND "
            + IBOUBVW_FORWARDITEMS.TYPE + " != ? AND " + IBOUBVW_FORWARDITEMS.REVERSALINDICATOR + " = ?  ORDER BY "
            + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " DESC";

    private static final String WHERE_CLAUSE1 = " WHERE " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
            + IBOTransaction.POSTINGDATE + " >= ? ORDER BY " + IBOTransaction.POSTINGDATE;;
    private static final String WHERE_CLAUSE3 = " WHERE " + IBOFinancialPostingMessage.PRIMARYID + " = ? AND " + "UB_TODATE("
            + IBOFinancialPostingMessage.TRANSACTIONDATE + ") >= ? ORDER BY "
            + IBOFinancialPostingMessage.TRANSACTIONDATE;

    private static final String WHERE_CLAUSE2 = " WHERE " + IBOUBVW_FORWARDITEMS.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
            + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER + " BETWEEN ? AND ? ORDER BY " + IBOUBVW_FORWARDITEMS.TRANSACTIONCOUNTER;
    private static final String SPECIAL_CHARS_REGULAR_EXP = "[^\00-\255]";
    
    int noRows;
    String accNum = CommonConstants.EMPTY_STRING;
    String blockingSign = "Z";

    public void process(BankFusionEnvironment env) {
        VectorTable result = new VectorTable();
        int nbtonlyflag = getF_IN_nbt_only_flag().intValue();
        accNum = getF_IN_AccountNumber();
        if (accNum != null) {
            if (nbtonlyflag == 3) {
                result = prepareMiniStatement();
            }
            else {
                result = prepareTxnListEnquiry();
            }
        }
        setF_OUT_NOOFROWS(noRows);
        setF_OUT_Result(result);
    }

    private VectorTable prepareTxnListEnquiry() {
        int transRef = 0;
        int nxtOrprv = getF_IN_NxtorPrv().intValue();
        Date transDate = getF_IN_TransDate();
        VectorTable transVector = new VectorTable();
        ArrayList params = new ArrayList();
        String query = CommonConstants.EMPTY_STRING;
        if (nxtOrprv == 0) {
            if (getF_IN_TransactionNumber().trim().contains("*")) {
                query = WHERE_CLAUSE2;
                params.add(accNum);
                params.add(1);
                params.add(MAX_TRANSACTIONS_REQUIRED);
            }
            else {
                transRef = Integer.parseInt(getF_IN_TransactionNumber().trim());
                if (transRef == 0) {
                    query = WHERE_CLAUSE2;
                    params.add(accNum);
                    params.add(transDate);
                    PagingData pagingData = new PagingData(1, 1);
                    List<IBOTransaction> txnList = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(
                            IBOTransaction.BONAME, WHERE_CLAUSE1, params, pagingData);
                    List<IBOFinancialPostingMessage> finList = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(
                            IBOFinancialPostingMessage.BONAME, WHERE_CLAUSE3, params, pagingData);
                    int txnCounter = 0, finCounter = 0;
                    if (txnList != null && !txnList.isEmpty()) {
                        IBOTransaction txnRecord = (IBOTransaction) txnList.get(0);
                        txnCounter = txnRecord.getF_TRANSACTIONCOUNTER();
                    }
                    if (finList != null && !finList.isEmpty()) {
                        IBOFinancialPostingMessage finRecord = (IBOFinancialPostingMessage) finList.get(0);
                        finCounter = finRecord.getF_TRANSACTIONCOUNTER();
                    }
                    params.clear();
                    query = WHERE_CLAUSE2;
                    params.add(accNum);
                    if (txnCounter == 0 && finCounter == 0) {
                        params.add(1);
                        params.add(MAX_TRANSACTIONS_REQUIRED);
                    }
                    else if (txnCounter > 0 && finCounter == 0) {
                        params.add(txnCounter);
                        params.add(txnCounter + MAX_TRANSACTIONS_REQUIRED);
                    }
                    else if (txnCounter == 0 && finCounter > 0) {
                        params.add(finCounter);
                        params.add(finCounter + MAX_TRANSACTIONS_REQUIRED);
                    }
                    else {
                        if (txnCounter < finCounter) {
                            params.add(txnCounter);
                            params.add(txnCounter + MAX_TRANSACTIONS_REQUIRED);
                        }
                        else {
                            params.add(finCounter);
                            params.add(finCounter + MAX_TRANSACTIONS_REQUIRED);
                        }
                    }
                }
                else {
                    query = WHERE_CLAUSE2;
                    params.add(accNum);
                    params.add(transRef);
                    params.add(transRef + MAX_TRANSACTIONS_REQUIRED);
                }
            }
        }
        else if (nxtOrprv == 1) {
            transRef = Integer.parseInt(getF_IN_TransactionNumber().trim());
            transRef = transRef + 1;
            query = WHERE_CLAUSE2;
            params.add(accNum);
            params.add(transRef);
            params.add(transRef + MAX_TRANSACTIONS_REQUIRED);
        }
        else if (nxtOrprv == 3) {
            transRef = Integer.parseInt(getF_IN_TransactionNumber().trim());
            transRef = transRef - 1;
            query = WHERE_CLAUSE2;
            params.add(accNum);
            params.add(transRef - MAX_TRANSACTIONS_REQUIRED);
            params.add(transRef);
        }
        PagingData pagingData = new PagingData(1, MAX_TRANSACTIONS_REQUIRED);
        List<IBOUBVW_FORWARDITEMS> list = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(
                IBOUBVW_FORWARDITEMS.BONAME, query, params, pagingData);
        transVector = prepareOutput(list);
        return transVector;
    }

    private VectorTable prepareMiniStatement() {
        VectorTable transVector = new VectorTable();
        IBOAccount accDetails = getAccountDetails(accNum);
        int fromCounter = accDetails.getF_UBACCTRANSCOUNTER() - MAX_TRANSACTIONS_REQUIRED;
        int toCounter = accDetails.getF_UBACCTRANSCOUNTER();
        ArrayList params = new ArrayList();
        params.add(accNum);
        params.add(fromCounter);
        params.add(toCounter);
        params.add(new Integer(0));
        params.add(blockingSign);
        params.add(new Integer(0));
        IPagingData pagingData = new PagingData(1, MAX_TRANSACTIONS_REQUIRED);
        pagingData.setCurrentPageNumber(1);
        pagingData.setRequiresTotalPages(true);
        pagingData.setPageSize(MAX_TRANSACTIONS_REQUIRED);
        List<IBOUBVW_FORWARDITEMS> list = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOUBVW_FORWARDITEMS.BONAME,
                MINISTMT_WHERECLAUSE, params, pagingData, false);
        transVector = prepareOutput(list);
        return transVector;
    }

    private boolean isNumeric(char c) {
        if (c >= '0' && c <= '9')
            return true;
        return false;
    }

    private Timestamp getBusinessDateTime() {
        return SystemInformationManager.getInstance().getBFBusinessDateTime();
    }

    /* method to get the account details for a given account */
    private IBOAccount getAccountDetails(String accountid) {
        IBOAccount accountItem = (IBOAccount) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOAccount.BONAME,
                accountid, false);
        return accountItem;
    }

    private VectorTable prepareOutput(List<IBOUBVW_FORWARDITEMS> result) {
        VectorTable transVector = new VectorTable();
        if (result != null && !result.isEmpty()) {
            MISTransactionCodeDetails mistransDetails;
            IBOMisTransactionCodes misTransactionCodes = null;
            Timestamp todaysDateTime = getBusinessDateTime();
            IBOAccount accDetails = getAccountDetails(accNum);
            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            for (int record = 0; record < result.size(); record++) {
                IBOUBVW_FORWARDITEMS transaction = (IBOUBVW_FORWARDITEMS) result.get(record);
                BigDecimal bookBalance = transaction.getF_BOOKBALANCE();
                BigDecimal clearedBalance = BigDecimal.ZERO;
                boolean accountClearedBalanceFound = false;
                if (todaysDateTime.compareTo(transaction.getF_VALUEDATE()) < 0) {
                    if (!accountClearedBalanceFound) {
                        clearedBalance = accDetails.getF_CLEAREDBALANCE();
                        accountClearedBalanceFound = true;
                    }
                }
                else clearedBalance = transaction.getF_CLEAREDBALANCE();
                HashMap attributes = new HashMap();
                attributes.put("TRANSACTIONLINETYPE", new Integer(0));
                attributes.put("DEALNO", new Integer(0));
                attributes.put("ACCRUEDINTEREST", BigDecimal.ZERO);
                attributes.put("PASSBOOKFLAG", new Integer(0));
                attributes.put("ISOCURRENCYCODE", transaction.getF_ISOCURRENCYCODE());
                attributes.put("AMOUNT", roundBalance(transaction.getF_AMOUNT(), transaction.getF_ISOCURRENCYCODE()));
                attributes.put("BOOKBALANCE", roundBalance(bookBalance, transaction.getF_ISOCURRENCYCODE()));
                attributes.put("CLEAREDRUNNINGBALANCE", roundBalance(clearedBalance, transaction.getF_ISOCURRENCYCODE()));
                if ((transaction.getF_UBCHANNELID().equals("ATM") || transaction.getF_UBCHANNELID().equals("POS"))
                        && transaction.getF_SHORTNAME().equalsIgnoreCase("") == false) {
                    String txnRef = transaction.getF_SHORTNAME();
                    attributes.put("REFERENCE", txnRef);
                }
                else if (transaction.getF_UBCHANNELID().equals("ATM")) {
                    String txnRef = transaction.getF_REFERENCE();
                    if (txnRef.length() >= 21) {
                        attributes.put("REFERENCE", txnRef.substring(13, 21));
                    }
                    else {
                        attributes.put("REFERENCE", txnRef);
                    }
                }
                else attributes.put("REFERENCE", transaction.getF_REFERENCE());
                attributes.put("PAGESRNUMBER", new Integer(transaction.getF_TRANSACTIONCOUNTER()));
                String narration = transaction.getF_NARRATION();
                narration = removeSpecialChars(narration);
                if (null != narration && narration.length() > 25) {
                    StringBuffer tempNarration = new StringBuffer();
                    int numOfNarrations = (narration.length() % 25 == 0) ? (narration.length() / 25)
                            : (narration.length() / 25) + 1;
                    for (int iter = 0; iter < numOfNarrations; iter++) {
                        int beginIndex = iter * 25;
                        int endIndex = (iter + 1 == numOfNarrations) ? (narration.length()) : ((iter + 1) * 25);
                        tempNarration.append(" ").append(narration.substring(beginIndex, endIndex).trim());
                    }
                    narration = tempNarration.toString().trim();
                }
                attributes.put("NARRATION", narration);
                attributes.put("BASEEQUIVALENT",
                        roundBalance(transaction.getF_BASEEQUIVALENT(), transaction.getF_ISOCURRENCYCODE()));
                attributes.put("OPPOSITECURRENCYCODE", transaction.getF_ISOCURRENCYCODE());
                attributes.put("POSTINGDATE", transaction.getF_POSTINGDATE());
                attributes.put("VALUEDATE", transaction.getF_VALUEDATE());
                if (transaction.getF_USERID().length() > 3)
                    attributes.put("USERID", transaction.getF_USERID().substring(0, 3));
                else attributes.put("USERID", transaction.getF_USERID());
                if (transaction.getF_AUTHORISEDUSERID().length() > 3)
                    attributes.put("SUPERVISORID", transaction.getF_AUTHORISEDUSERID().substring(0, 3));
                else attributes.put("SUPERVISORID", transaction.getF_AUTHORISEDUSERID());
                mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo())
                        .getMisTransactionCodeDetails(transaction.getF_CODE());
                misTransactionCodes = mistransDetails.getMisTransactionCodes();
                if (isNumeric(transaction.getF_CODE().charAt(0)) || transaction.getF_CODE().length() <= 2) {
                    attributes.put("CODE", misTransactionCodes.getF_PREFIXSUFFIX().substring(0, 3));
                }
                else attributes.put("CODE", transaction.getF_CODE());
                transVector.addAll(new VectorTable(attributes));
                noRows++;
            }
        }
        return transVector;
    }

    /**
     * 
     * @param amount
     * @param currencyCode
     * @return The Rounded Amount based on the currency code.
     */
    private static CurrencyValue roundBalance(BigDecimal amount, String currencyCode) {
        CurrencyValue currencyValue = new CurrencyValue(currencyCode, amount);
        BigDecimal amt = Utils.roundCurrency(currencyValue, SystemInformationManager.getInstance().getCurrencyRoundingUnits(
                currencyCode), SystemInformationManager.getInstance().getCurrencyScale(currencyCode));
        return new CurrencyValue(currencyCode, amt);
    }

    private String removeSpecialChars(String narration) {
        Pattern unicodeOutliers = Pattern.compile(SPECIAL_CHARS_REGULAR_EXP, Pattern.UNICODE_CASE | Pattern.CANON_EQ
                | Pattern.CASE_INSENSITIVE);
        Matcher unicodeOutlierMatcher = unicodeOutliers.matcher(narration);
        narration = unicodeOutlierMatcher.replaceAll(CommonConstants.EMPTY_STRING);
        return narration;
    }
}
