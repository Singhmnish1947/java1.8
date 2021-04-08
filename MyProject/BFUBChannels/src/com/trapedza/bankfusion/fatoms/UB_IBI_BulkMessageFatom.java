/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Trapedza Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 *
 ***********************************************************/
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.types.Date;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.GenericPersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.SubtractDaysFromDate;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_BulkMessageFatom;

public class UB_IBI_BulkMessageFatom extends AbstractUB_IBI_BulkMessageFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

    /**
     */

    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    public static final int CONSTANT_ZERO = 0;
    public static final int CONSTANT_ONE = 1;
    public static final int DATE_DELIMPOSITION_OR_XRATESCALE_CONSTANT = 4;
    public static final int DATETIME_END_INDEX = 14;
    public static final int SPACE_DELIMITER_POSITION = 8;
    public static final int TIME_DELIMITER_POSITION = 10;
    public static final int UB_TXN_NARRATIVE_MAX_LENGTH = 100;
    public static final int IFM_CUST_MEMO1_MAX_LENGTH = 25;
    public static final int IFM_CUST_MEMO2_MAX_LENGTH = 50;
    public static final String FINANTIAL_MESSAGE_INDICATOR = "N";
    public static final String TRANSACTION_DATE = "TXNDATE";
    public static final String VALUE_DATE = "VALUEDATE";
    public static final String ACCOUNT_ID = "ACCNO";
    public static final String PAY_TYPE = "PAYTYPE";
    public static final String BOOKED_BALANCE = "TXNRUNNINGBAL";
    public static final String X_FR_AMT = "XFRAMT";
    public static final String X_RATE = "XRATE";
    public static final String X_FR_CURR = "XFRCURR";
    public static final String PAYMENT_REF = "PAYMENTREF";
    public static final String AMOUNT = "AMOUNTVALUE";
    public static final String ACC_CLEARED_BAL = "ACCCLEAREDBAL";
    public static final String ACC_BOOKED_BAL = "ACCBOOKEDBAL";
    public static final String ACC_DATE_TIME = "ACCDATETIME";
    public static final String ACC_TXN_COUNTER = "ACCTXNCOUNTER";
    public static final String ROW_COUNT = "ROWCOUNT";
    public static final String ACC_TIME = "000000";
    public static final String VECTOR_ROWS_RETURNED = "VECTORTABLESIZE";
    public static final String FIN_POSTING_MSG_ALIAS = "IBOFinancialPostingMessage";
    public static final String FIN_TRANSACTION_ALIAS = "IBOTransaction";
    public static final String NO_TO_STATEMENTS = "N";
    

    public static final String CREDIT_POSTING_ACTION = "C";
    public static final String CREDIT_POSTING_SIGN = "+";
    public static final String DEBIT_POSTING_ACTION = "D";
    public static final String DEBIT_POSTING_SIGN = "-";
    public static final String PAY_TYPE_CREDIT = "CRED";
    public static final String PAY_TYPE_DEBIT = "DEBIT";
    public static final String EMPTY_STRING = " ";
    public static final String DEBIT_CREDIT_FLAG = "DEBITCREDITFLAG";
    public static final String UB_TXN_NARRATIVE = "UBTXNNARRATIVE";
    public static final String TRANSACTION_COUNTER = "TRANSACTIONCOUNTER";
    public static final String TXN_CURRENCY_CODE = "TXNCURRCODE";
    public static final String ACC_CURR_CODE = "ACCCURRCODE";
    public static final String DEFAULT_DELIMITER = "";
    public static final String TRANSACTION_ISO_CURR_CODE = "TRANSACTIONISOCURRCODE";
    public static final String ACC_LAST_CREDITED_DATE = "ACCLASTCREDITEDDATE";
    public static final String ACC_LAST_DEBITED_DATE = "ACCLASTDEBITEDDATE";

    public static final String FETCH_FWD_DATED_TXNS = "Customized Query for FinPostingMsg table executed:";
    public static final String FETCH_TXNS = "Customized Query for FinPostingMsg table executed:";
    public static final String NO_OF_RECORDS_READ_FROM_THE_RESULTSET = "Iteration number in UB_IBI_BulkMessageFatom";
    public static final String BF_CURR_VALUE = "BFCurrencyValue";
    public static final String POSTING_ACTION = "Posting Action for the transaction";
    public static final String XRATE_DECIMAL_CORRECTION = "Exchange Rate to be displayed as SCALE 8 : ";
    public static final String EXCEPTION_COLLECTIONS_SORT = "Arguments types prevent them from being compared by this comparator";
    public static final Boolean FALSE = false;
    public static final Boolean TRUE = true;
    public static final String IBI_ACCOUNTID = "ACCOUNTID";
    public static final String EXECUTE_MF = "Invoking MF ---> ";
    public static final String READ_IBIACCOUNT = "UB_IBI_ReadIBAccount_SRV";
    public static final String ACC_LAST_TRANSACTION_COUNTER = "ACCLASTTRANSCOUNTER";
    public static final String MF_FAILED = "Failed to execute ---> ";
    public Timestamp txnDates = new Timestamp(0);
    private transient final static Log logger = LogFactory.getLog(UB_IBI_BulkMessageFatom.class.getName());

    public UB_IBI_BulkMessageFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * Method Description: This method constructs queries based on the input params to the fatom. It
     * then invokes GetBulkTxnDetails() which executes the initialised query strings and computes
     * data to be sent to IFM.
     * 
     * @param env
     * @return void
     */
   
    public void process(BankFusionEnvironment env)
    {

        String txnBoCustomQuery = null;
        String finPostingMsgCustomQuery = null;
        VectorTable txnDetailsVector = new VectorTable();

        String accId = getF_IN_ACCID();
        int ubAccTxnCounter = getF_IN_UBACCTXNCOUNTER();
        int maxAllowedTxns = getF_IN_MAXALLOWEDTXNS();
        String txnDate = FetchDateForBulkMsgs();
        ArrayList params1 = new ArrayList();
        txnDates = Timestamp.valueOf(txnDate);
        if (accId != null) {

            final String bulkMsgTxnBOQuery =

            "SELECT T." + IBOTransaction.TRANSACTIONCOUNTER + " as " + TRANSACTION_COUNTER + ", T."
                    + IBOTransaction.TRANSACTIONDATE + " as " + TRANSACTION_DATE + ", T." + IBOTransaction.VALUEDATE + " as "
                    + VALUE_DATE + ", T." + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " as " + ACCOUNT_ID  + ", T."
                    + IBOTransaction.BOOKBALANCE + " as " + BOOKED_BALANCE + ", T."+ IBOTransaction.DEBITCREDITFLAG +" as "+DEBIT_CREDIT_FLAG + ", T." + IBOTransaction.NARRATION + " as " + UB_TXN_NARRATIVE + ", T."
                    + IBOTransaction.REFERENCE + " as " + PAYMENT_REF + ", T." + IBOTransaction.AMOUNT + " as " + AMOUNT + ", T."
                    + IBOTransaction.ISOCURRENCYCODE + " as " + TXN_CURRENCY_CODE + ", T." + IBOTransaction.ORIGINALAMOUNT + " as "
                    + X_FR_AMT + ", T." + IBOTransaction.EXCHANGERATE + " as " + X_RATE + ", T."
                    + IBOTransaction.OPPOSITECURRENCYCODE + " as " + X_FR_CURR + ", T."+IBOTransaction.ISOCURRENCYCODE +" as "+ TRANSACTION_ISO_CURR_CODE +", A." 
                    + IBOAttributeCollectionFeature.CLEAREDBALANCE + " AS "
                    + ACC_CLEARED_BAL + ", A." + IBOAttributeCollectionFeature.BOOKEDBALANCE + " AS " + ACC_BOOKED_BAL + ", A."
                    + IBOAttributeCollectionFeature.LASTTRANSACTIONDATE + " AS " + ACC_DATE_TIME + ", A." + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " AS "
                    + ACC_CURR_CODE + ", A."+ IBOAttributeCollectionFeature.LASTCREDITTRANSDTTM +" as "+ ACC_LAST_CREDITED_DATE +", A."+ IBOAttributeCollectionFeature.LASTDEBITTRANSDTTM +" as "+ ACC_LAST_DEBITED_DATE +" FROM " + IBOTransaction.BONAME + " T, " + IBOAttributeCollectionFeature.BONAME + " A " + " WHERE T."
                    + IBOTransaction.TRANSACTIONDATE + " >= ? AND T."+IBOTransaction.TYPE + " = '" + FINANTIAL_MESSAGE_INDICATOR + "' AND T." + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID
                    + " = '" + accId + "'" + " AND T." + IBOTransaction.STATEMENTFLAG + " = " + CONSTANT_ZERO + " AND A."
                    + IBOAttributeCollectionFeature.ACCOUNTID + " = T." + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " ORDER BY " + CONSTANT_ONE;

            final String bulkMsgFinPostingMsgBOTxnQuery =

            "SELECT F." + IBOFinancialPostingMessage.TRANSACTIONCOUNTER + " as " + TRANSACTION_COUNTER + ", F."
                    + IBOFinancialPostingMessage.TRANSACTIONDATE + " as " + TRANSACTION_DATE + ", F."
                    + IBOFinancialPostingMessage.VALUEDATE + " as " + VALUE_DATE + ", F." + IBOFinancialPostingMessage.PRIMARYID
                    + " as " + ACCOUNT_ID + ", F." + IBOFinancialPostingMessage.BOOKBALANCE + " as " + BOOKED_BALANCE + ", F."
                    + IBOFinancialPostingMessage.SIGN + " as " + DEBIT_CREDIT_FLAG + ", F." + IBOFinancialPostingMessage.NARRATIVE
                    + " as " + UB_TXN_NARRATIVE + ", F." + IBOFinancialPostingMessage.TRANSACTIONREF + " as " + PAYMENT_REF
                    + ", F." + IBOFinancialPostingMessage.AMOUNT + " as " + AMOUNT + ", F."
                    + IBOFinancialPostingMessage.TXNCURRENCYCODE + " as " + TXN_CURRENCY_CODE + ", F."
                    + IBOFinancialPostingMessage.ACTUALAMOUNT + " as " + X_FR_AMT + ", F." + IBOFinancialPostingMessage.EXCHRATE
                    + " as " + X_RATE + ", F." + IBOFinancialPostingMessage.TXNCURRENCYCODE + " as " + X_FR_CURR + ", F."+IBOFinancialPostingMessage.ACCTCURRENCYCODE +" as "+ TRANSACTION_ISO_CURR_CODE +", A."
                    + IBOAttributeCollectionFeature.CLEAREDBALANCE + " AS " + ACC_CLEARED_BAL + ", A." + IBOAttributeCollectionFeature.BOOKEDBALANCE + " AS "
                    + ACC_BOOKED_BAL + ", A." + IBOAttributeCollectionFeature.LASTTRANSACTIONDATE + " AS " + ACC_DATE_TIME + ", A."
                    + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " AS " + ACC_CURR_CODE + ", A."+ IBOAttributeCollectionFeature.LASTCREDITTRANSDTTM +" as "+ ACC_LAST_CREDITED_DATE +", A."+ IBOAttributeCollectionFeature.LASTDEBITTRANSDTTM +" as "+ ACC_LAST_DEBITED_DATE +" FROM " + IBOFinancialPostingMessage.BONAME + " F "
                    + ", " + IBOAttributeCollectionFeature.BONAME + " A " + " WHERE F." + IBOFinancialPostingMessage.PRIMARYID + " = A."
                    + IBOAttributeCollectionFeature.ACCOUNTID + " AND F." + IBOFinancialPostingMessage.PRIMARYID + " = '" + accId + "'"+" AND F."+ IBOFinancialPostingMessage.MESSAGETYPE + " = '" + FINANTIAL_MESSAGE_INDICATOR + "' AND F."
                    + IBOFinancialPostingMessage.TRANSACTIONDATE + " >= ? " + " AND F."
                    + IBOFinancialPostingMessage.TRANSACTIONCODE + " IN (" + "SELECT " + IBOMisTransactionCodes.CODE + " FROM "
                    + IBOMisTransactionCodes.BONAME + " WHERE " + IBOMisTransactionCodes.NOTONSTATEMENTS + " = '"
                    + NO_TO_STATEMENTS + "')" + " ORDER BY " + CONSTANT_ONE;

            final String bulkMsgQueryTxnBOCntr =

            "SELECT T." + IBOTransaction.TRANSACTIONCOUNTER + " as " + TRANSACTION_COUNTER + ", T."
                    + IBOTransaction.TRANSACTIONDATE + " as " + TRANSACTION_DATE + ", T." + IBOTransaction.VALUEDATE + " as "
                    + VALUE_DATE + ", T." + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " as " + ACCOUNT_ID + ", T."
                    + IBOTransaction.BOOKBALANCE + " as " + BOOKED_BALANCE + ", T." + IBOTransaction.DEBITCREDITFLAG + " as "
                    + DEBIT_CREDIT_FLAG +", T." + IBOTransaction.NARRATION + " as " + UB_TXN_NARRATIVE + ", T."
                    + IBOTransaction.REFERENCE + " as " + PAYMENT_REF + ", T." + IBOTransaction.AMOUNT + " as " + AMOUNT + ", T."
                    + IBOTransaction.ISOCURRENCYCODE + " as " + TXN_CURRENCY_CODE + ", T." + IBOTransaction.ORIGINALAMOUNT + " as "
                    + X_FR_AMT + ", T." + IBOTransaction.EXCHANGERATE + " as " + X_RATE + ", T."
                    + IBOTransaction.OPPOSITECURRENCYCODE + " as " + X_FR_CURR + ", T."+IBOTransaction.ISOCURRENCYCODE +" as "+ TRANSACTION_ISO_CURR_CODE 
                   + ", A." + IBOAttributeCollectionFeature.CLEAREDBALANCE + " AS "
                  + ACC_CLEARED_BAL + ", A." + IBOAttributeCollectionFeature.BOOKEDBALANCE + " AS " + ACC_BOOKED_BAL + ", A."
                   + IBOAttributeCollectionFeature.LASTTRANSACTIONDATE + " AS " + ACC_DATE_TIME + ", A." + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " AS "
                   + ACC_CURR_CODE + ", A."+ IBOAttributeCollectionFeature.LASTCREDITTRANSDTTM +" as "+ ACC_LAST_CREDITED_DATE +", A."+ IBOAttributeCollectionFeature.LASTDEBITTRANSDTTM +" as "+ ACC_LAST_DEBITED_DATE +" FROM " + IBOTransaction.BONAME + " T, " + IBOAttributeCollectionFeature.BONAME + " A " + " WHERE T."
                  + IBOTransaction.TRANSACTIONCOUNTER + " > " + ubAccTxnCounter + " AND T." + IBOTransaction.TYPE + " = '" + FINANTIAL_MESSAGE_INDICATOR + "' AND T."
                   + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = '" + accId + "'" + " AND T." + IBOTransaction.STATEMENTFLAG
                   + " = " + CONSTANT_ZERO + " AND A." + IBOAttributeCollectionFeature.ACCOUNTID + " = T." + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID
                   + " ORDER BY " + CONSTANT_ONE;

            final String bulkMsgQueryFinPostingMsgBOTxnCntr =

            "SELECT F." + IBOFinancialPostingMessage.TRANSACTIONCOUNTER + " as " + TRANSACTION_COUNTER + ", F."
                    + IBOFinancialPostingMessage.TRANSACTIONDATE + " as " + TRANSACTION_DATE + ", F."
                    + IBOFinancialPostingMessage.VALUEDATE + " as " + VALUE_DATE + ", F." + IBOFinancialPostingMessage.PRIMARYID
                    + " as " + ACCOUNT_ID + ", F." + IBOFinancialPostingMessage.BOOKBALANCE + " as " + BOOKED_BALANCE + ", F."
                    + IBOFinancialPostingMessage.SIGN + " as " + DEBIT_CREDIT_FLAG + ", F." + IBOFinancialPostingMessage.NARRATIVE
                    + " as " + UB_TXN_NARRATIVE + ", F." + IBOFinancialPostingMessage.TRANSACTIONREF + " as " + PAYMENT_REF
                    + ", F." + IBOFinancialPostingMessage.AMOUNT + " as " + AMOUNT + ", F."
                    + IBOFinancialPostingMessage.TXNCURRENCYCODE + " as " + TXN_CURRENCY_CODE + ", F."
                    + IBOFinancialPostingMessage.ACTUALAMOUNT + " as " + X_FR_AMT + ", F." + IBOFinancialPostingMessage.EXCHRATE
                    + " as " + X_RATE + ", F." + IBOFinancialPostingMessage.TXNCURRENCYCODE + " as " + X_FR_CURR +", F."+IBOFinancialPostingMessage.ACCTCURRENCYCODE +" as "+ TRANSACTION_ISO_CURR_CODE + ", A."
                    + IBOAttributeCollectionFeature.CLEAREDBALANCE + " AS " + ACC_CLEARED_BAL + ", A." + IBOAttributeCollectionFeature.BOOKEDBALANCE + " AS "
                    + ACC_BOOKED_BAL + ", A." + IBOAttributeCollectionFeature.LASTTRANSACTIONDATE + " AS " + ACC_DATE_TIME + ", A."
                    + IBOAttributeCollectionFeature.ISOCURRENCYCODE + " AS " + ACC_CURR_CODE + ", A."+ IBOAttributeCollectionFeature.LASTCREDITTRANSDTTM +" as "+ ACC_LAST_CREDITED_DATE +", A."+ IBOAttributeCollectionFeature.LASTDEBITTRANSDTTM +" as "+ ACC_LAST_DEBITED_DATE +" FROM " + IBOFinancialPostingMessage.BONAME + " F, "
                    + IBOAttributeCollectionFeature.BONAME + " A " + " WHERE F." + IBOFinancialPostingMessage.TRANSACTIONCOUNTER + " > "
                    + ubAccTxnCounter + " AND F."+IBOFinancialPostingMessage.MESSAGETYPE +" = '"+FINANTIAL_MESSAGE_INDICATOR +"' AND F." + IBOFinancialPostingMessage.PRIMARYID + " = '" + accId + "'" + " AND A."
                    + IBOAttributeCollectionFeature.ACCOUNTID + " = F." + IBOFinancialPostingMessage.PRIMARYID + " AND F."
                    + IBOFinancialPostingMessage.TRANSACTIONCODE + " IN (" + "SELECT " + IBOMisTransactionCodes.CODE + " FROM "
                    + IBOMisTransactionCodes.BONAME + " WHERE " + IBOMisTransactionCodes.NOTONSTATEMENTS + " = '"
                    + NO_TO_STATEMENTS + "')" + " ORDER BY " + CONSTANT_ONE;

            if (ubAccTxnCounter <= CONSTANT_ZERO) {
                finPostingMsgCustomQuery = bulkMsgFinPostingMsgBOTxnQuery;
                txnBoCustomQuery = bulkMsgTxnBOQuery;
                params1.add(txnDates);
            }
            if (ubAccTxnCounter > CONSTANT_ZERO) {
                finPostingMsgCustomQuery = bulkMsgQueryFinPostingMsgBOTxnCntr;
                txnBoCustomQuery = bulkMsgQueryTxnBOCntr;
            }

            try {
                GetBulkTxnDetails(txnBoCustomQuery, finPostingMsgCustomQuery, maxAllowedTxns, txnDetailsVector,params1, env);
            }
            catch (ParseException eParseException) {
                logger.error(ExceptionUtil.getExceptionAsString(eParseException));
            }

            if (txnDetailsVector.size() != CONSTANT_ZERO) {
                setF_OUT_TXNRESULTSET(txnDetailsVector);
            }
            setF_OUT_VECTORTABLESIZE(txnDetailsVector.size());
        }
    }

    /**
     * Method Description: Compute the date for sending bulk messages by subtracting the number of
     * days (obtained from module configuration) from the current business date
     * 
     * @return String
     */
    private String FetchDateForBulkMsgs() {

        String txnDate = null;
        try {
            txnDate = SubtractDaysFromDate.run(SystemInformationManager.getInstance().getBFBusinessDate(), getF_IN_CONFIGDAYS())
                    .toString();
            txnDate = txnDate.concat(EMPTY_STRING);
            txnDate = txnDate.concat(SystemInformationManager.getInstance().getBFBusinessTimeAsString().toString());
        }
        catch (BankFusionException bankFusionException) {
            logger.error(ExceptionUtil.getExceptionAsString(bankFusionException));
        }
        return txnDate;
    }

    /**
     * Method Description: This method executes the query constructed and arrives at the transaction
     * data to be sent to IFM
     * 
     * @param txnBoCustomQuery
     * @param finPostingMsgCustomQuery
     * @param maxTxnsAllowed
     * @param txnDetailsVector
     * @param params1 
     * @throws ParseException
     * @return void
     */

    private void GetBulkTxnDetails(String txnBoCustomQuery, String finPostingMsgCustomQuery, int maxTxnsAllowed,
            VectorTable txnDetailsVector, ArrayList params1, BankFusionEnvironment env) throws ParseException {

        int txnCounter = CONSTANT_ZERO;
        List resultLst = null;
        Map resultMap = new HashMap();
        List transactionBOResultLst = null;
        List finPostingMsgBOResultLst = null;
        GenericPersistentObject bulkTxnObj = null;

        try {

            finPostingMsgBOResultLst = factory.executeGenericQuery(finPostingMsgCustomQuery,params1, null, null);
            if (logger.isInfoEnabled()) {
                logger.info(FETCH_FWD_DATED_TXNS + finPostingMsgCustomQuery);
            }
            transactionBOResultLst = factory.executeGenericQuery(txnBoCustomQuery, params1 ,null, null);
            if (logger.isInfoEnabled()) {
                logger.info(FETCH_TXNS + txnBoCustomQuery);
            }
            if (finPostingMsgBOResultLst.size() != CONSTANT_ZERO || transactionBOResultLst.size() != CONSTANT_ZERO) {
                if (finPostingMsgBOResultLst.size() != CONSTANT_ZERO && transactionBOResultLst.size() != CONSTANT_ZERO) {
                    // merge the results obtained from Transaction & FinPostingMsg tables
                    resultLst = transactionBOResultLst;
                    resultLst.addAll(finPostingMsgBOResultLst);

                    try {
                        // Sort the merged list based on UBACCOUNTTXNCOUNTER column of the merged
                        // list
                        Collections.sort(resultLst, new UB_IBI_TransCounterComparator());
                    }
                    catch (ClassCastException eClassCastException) {
                        logger.error(EXCEPTION_COLLECTIONS_SORT + eClassCastException.getMessage());
                        logger.error(ExceptionUtil.getExceptionAsString(eClassCastException));
                    }

                }
                if (transactionBOResultLst.size() <= CONSTANT_ZERO) {
                    resultLst = finPostingMsgBOResultLst;
                }
                if (finPostingMsgBOResultLst.size() <= CONSTANT_ZERO) {
                    resultLst = transactionBOResultLst;

                }

                Iterator resultLstItr = resultLst.iterator();
                while (resultLstItr.hasNext()) {
                    if (txnCounter >= maxTxnsAllowed) {

                        break;
                    }
                    else {
                        bulkTxnObj = (GenericPersistentObject) resultLstItr.next();
                        CompareTxnCounterWithIBICounter(bulkTxnObj, env);

                        // Amount fields
                        BigDecimal txnBookedBalance = (BigDecimal) bulkTxnObj.getDataMap().get(BOOKED_BALANCE);
                        BigDecimal clearedBalance = (BigDecimal) bulkTxnObj.getDataMap().get(ACC_CLEARED_BAL);
                        BigDecimal accBookedBalance = (BigDecimal) bulkTxnObj.getDataMap().get(ACC_BOOKED_BAL);
                        BigDecimal amountValue = ((BigDecimal) bulkTxnObj.getDataMap().get(AMOUNT)).abs();
                        BigDecimal xFrAmt = (BigDecimal) bulkTxnObj.getDataMap().get(X_FR_AMT);
                        
                        //obtain PayType of the transaction
                        String crDr = bulkTxnObj.getDataMap().get(DEBIT_CREDIT_FLAG).toString();
                        String payType = getPayType(crDr);

                        // BULK message details
                        resultMap.put(ACCOUNT_ID, bulkTxnObj.getDataMap().get(ACCOUNT_ID).toString());
                        resultMap.put(TRANSACTION_DATE, ComputeTransactionDate(bulkTxnObj.getDataMap().get(TRANSACTION_DATE)
                                .toString()));
                        resultMap.put(VALUE_DATE, ComputeTransactionDate(bulkTxnObj.getDataMap().get(VALUE_DATE).toString()));
                        resultMap.put(PAY_TYPE, payType);
                        resultMap.put(BOOKED_BALANCE, txnBookedBalance.toString());
                        resultMap.put(ACC_TXN_COUNTER, bulkTxnObj.getDataMap().get(TRANSACTION_COUNTER));
                        resultMap.put(X_FR_AMT, (xFrAmt.abs()).toString());
                        if (bulkTxnObj.getDataMap().get(X_FR_CURR) == null) {
                            resultMap.put(X_FR_CURR, bulkTxnObj.getDataMap().get(TRANSACTION_ISO_CURR_CODE).toString());
                        }
                        else {
                            resultMap.put(X_FR_CURR, bulkTxnObj.getDataMap().get(X_FR_CURR).toString());
                        }
                        resultMap.put(X_RATE, roundExchangeRate(((BigDecimal) bulkTxnObj.getDataMap().get(X_RATE)).toPlainString()));
                        resultMap.put(AMOUNT, amountValue.toString());
                        resultMap.put(PAYMENT_REF, bulkTxnObj.getDataMap().get(PAYMENT_REF).toString());

                        //Compute Customer memo lines for IFM
                        getCustMemo(bulkTxnObj.getDataMap().get(UB_TXN_NARRATIVE).toString(), resultMap);

                        // BAL message details
                        resultMap.put(ACC_CLEARED_BAL, clearedBalance.toString());
                        resultMap.put(ACC_BOOKED_BAL, accBookedBalance.toString());
                        resultMap.put(ACC_DATE_TIME, GetAccDateTime(bulkTxnObj, payType));
                             
                        resultMap.put(ROW_COUNT, txnCounter);
                        txnDetailsVector.addAll(new VectorTable(resultMap));

                        if (logger.isInfoEnabled()) {
                            logger.info(NO_OF_RECORDS_READ_FROM_THE_RESULTSET + txnCounter);
                        }
                        // restrict the number of messages sent to IFM based on this counter
                        // variable
                        txnCounter++;

                    }

                }

            }
        }
        catch (BankFusionException bfe) {
            logger.error(ExceptionUtil.getExceptionAsString(bfe));
        }
    }
    /**
     * Method Description: This method decides the date and time associated with the booked and cleared balances based on the transactin paytype
     * 
     * @param bulkTxnObj
     * @param payType
     * @return String
     * @throws ParseException 
     */
    private String GetAccDateTime(
			GenericPersistentObject bulkTxnObj, String payType) throws ParseException {
    	String accCrORDrDate = EMPTY_STRING;
    	if(payType == PAY_TYPE_CREDIT)
    	{
    		accCrORDrDate = bulkTxnObj.getDataMap().get(ACC_LAST_CREDITED_DATE).toString();
    	}
    	if(payType == PAY_TYPE_DEBIT)
    	{
    		accCrORDrDate = bulkTxnObj.getDataMap().get(ACC_LAST_DEBITED_DATE).toString();
    	}
    	accCrORDrDate = ComputeAccDateTime(accCrORDrDate);
    	return accCrORDrDate;
    	
		
	}

	/**
     * Method Description: This method compares the current record's transCounter with the
     * IBIAccount table's lastTransCounter. If the current Txn Counter is greater than the
     * IBIAccount table's counter then return FALSE else return TRUE
     * 
     * @param bulkTxnObj
     * @param env
     * @return Boolean
     */
    private Boolean CompareTxnCounterWithIBICounter(GenericPersistentObject bulkTxnObj, BankFusionEnvironment env) {
        Boolean recordAlreadyExists = FALSE;
        HashMap params = new HashMap();
        int currentAccTxnCounter = 0;

        try {
            currentAccTxnCounter = Integer.parseInt((bulkTxnObj.getDataMap().get(TRANSACTION_COUNTER).toString()));
        }
        catch (NumberFormatException numberFormatException) {
            logger.error(ExceptionUtil.getExceptionAsString(numberFormatException));
        }
        params.put(IBI_ACCOUNTID, bulkTxnObj.getDataMap().get(ACCOUNT_ID).toString());
        if (logger.isInfoEnabled()) {
            logger.info(EXECUTE_MF + READ_IBIACCOUNT);
        }
        try {
            HashMap IBIAccountSRVResult = MFExecuter.executeMF(READ_IBIACCOUNT, env, params);
            int ibiLastTxnCounter = Integer.parseInt((IBIAccountSRVResult.get(ACC_LAST_TRANSACTION_COUNTER)).toString());
            if (currentAccTxnCounter <= ibiLastTxnCounter) {
                recordAlreadyExists = TRUE;
            }
        }
        catch (BankFusionException bfe) {
            logger.error(MF_FAILED + READ_IBIACCOUNT);
            logger.error(ExceptionUtil.getExceptionAsString(bfe));
        }

        return recordAlreadyExists;
    }

    /**
     * Method Description: This method computes date from a string containing datetime and invokes
     * RemoveDelimiter()which removes all the date delimiters and returns the date string in
     * yyyymmdd format
     * 
     * @param transactionDateTime
     * @return String
     */
    private String ComputeTransactionDate(String transactionDateTime) {
        String date = EMPTY_STRING;
        
        try {
            if (transactionDateTime.length() > CONSTANT_ZERO) {
               
                Date dt = Date.parseDate(transactionDateTime);
                               
                String txnDateTime = dt.toString();
                
                char dateDelimiterPosition = txnDateTime.charAt(DATE_DELIMPOSITION_OR_XRATESCALE_CONSTANT);
                String dateDelimiter = Character.toString(dateDelimiterPosition);
               
                //remove date delimiter
                date = RemoveDelimiter(txnDateTime, dateDelimiter);
               
               
            }
        }
        catch (ParseException eParseException) {
            logger.info(eParseException.getMessage());

        }
        return date;

    }

    /**
     * Method Description:This methods is used to removes all occurrence of a delimiter from the
     * transactionDate string
     * 
     * @param transactionDate
     * @param actualDelimiter
     * @return String
     */
    private String RemoveDelimiter(String transactionDate, String actualDelimiter) {

        String transactionDateModified = DEFAULT_DELIMITER;
        try {
            StringTokenizer transactionDateTokenizer = new StringTokenizer(transactionDate, actualDelimiter, false);

            while (transactionDateTokenizer.hasMoreElements()) {
                transactionDateModified += transactionDateTokenizer.nextElement();

            }
        }
        catch (NullPointerException eNullPointerException) {
            logger.error(ExceptionUtil.getExceptionAsString(eNullPointerException));
        }
        return transactionDateModified;
    }

    // /**
    // * Method Description: Rounds off the amount to the currency scale of the isoCurrencyCode
    // being
    // * passed
    // *
    // * @param amount
    // * @param isoCurrencyCode
    // * @return String
    // */
    // private String getRoundedAmount(BigDecimal amount, String isoCurrencyCode) {
    // String roundedAmt = EMPTY_STRING;
    // if (isoCurrencyCode.length() > CONSTANT_ZERO) {
    // BFCurrencyValue bfCurrencyAmt = new BFCurrencyValue(isoCurrencyCode, amount, null);
    // amount = bfCurrencyAmt.getRoundedAmount();
    // roundedAmt = amount.toPlainString();
    // if (logger.isInfoEnabled()) {
    // logger.info(BF_CURR_VALUE + amount);
    // }
    //
    // }
    // return roundedAmt;
    // }

    /**
     * Method Description:Exchange rate to display upto 8 decimal places is computed
     * 
     * @param exchangeRate
     * @return String
     */
    private String roundExchangeRate(String exchangeRate) {

        int strlen = exchangeRate.length();
        String xRate = EMPTY_STRING;
        if (strlen > CONSTANT_ZERO) {
            int lastIndex = strlen - CONSTANT_ONE;
            int indexForXRate = lastIndex - DATE_DELIMPOSITION_OR_XRATESCALE_CONSTANT;
            try {
                xRate = exchangeRate.substring(CONSTANT_ZERO, indexForXRate);
                if (logger.isInfoEnabled()) {
                    logger.info(XRATE_DECIMAL_CORRECTION + exchangeRate);
                }
            }
            catch (IndexOutOfBoundsException exIndexOutOfBoundsException) {
                logger.error(ExceptionUtil.getExceptionAsString(exIndexOutOfBoundsException));
            }

        }
        return xRate;
    }

    /**
     * Method Description: Pay type [credit/debit] is identified
     * 
     * @param drCrIndicator
     * @return String
     */
    private String getPayType(String drCrIndicator) {
        String payType = EMPTY_STRING;
        if (drCrIndicator.equalsIgnoreCase(CREDIT_POSTING_ACTION) || drCrIndicator.equals(CREDIT_POSTING_SIGN)) {
            payType = PAY_TYPE_CREDIT;
        }
        if (drCrIndicator.equalsIgnoreCase(DEBIT_POSTING_ACTION) || drCrIndicator.equals(DEBIT_POSTING_SIGN)) {
            payType = PAY_TYPE_DEBIT;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Posting Action for the transaction" + payType);
        }
        return payType;

    }

    /**
     * Method Description: This method invokes RemoveDelimiter()which removes all the date
     * delimiters and returns the date string in yyyymmddhhmmss format
     * 
     * @param accDate
     * @return String
     */
    private String ComputeAccDateTime(String accDatetime) throws ParseException {

        String date = EMPTY_STRING;
        if (accDatetime.length() > CONSTANT_ZERO) {
            String dateDelimiter = Character.toString(accDatetime.charAt(DATE_DELIMPOSITION_OR_XRATESCALE_CONSTANT));
            date = RemoveDelimiter(accDatetime, dateDelimiter);
            //remove the space
            char dateTimeDelimPosition = date.charAt(SPACE_DELIMITER_POSITION);
            String dtTimeDelimiter = Character.toString(dateTimeDelimPosition);
            date = RemoveDelimiter(date, dtTimeDelimiter);
            //remove the time delimiter
            char timeDelimPostion = date.charAt(TIME_DELIMITER_POSITION);
            String timeDelimiter = Character.toString(timeDelimPostion);
            date = RemoveDelimiter(date, timeDelimiter);
            //get yyyymmddhhmmss format only and exclude the remaining chars
            date = date.substring(CONSTANT_ZERO,DATETIME_END_INDEX);
        }
        return date;

    }

    /**
     * Method Description: Compute 25 chars of customer memoline from the input UB narration field
     * 
     * @param txnNarrative
     * @param resultMap
     */
    private void getCustMemo(String txnNarrative, Map resultMap) {

        if (txnNarrative.length() > CONSTANT_ZERO) {

            if (txnNarrative.length() > CONSTANT_ZERO) {

                try {
                    if (txnNarrative.length() <= IFM_CUST_MEMO1_MAX_LENGTH) {

                        resultMap.put("CUSTMEMOLINE1", txnNarrative.substring(CONSTANT_ZERO, txnNarrative.length()));
                        resultMap.put("CUSTMEMOLINE2", EMPTY_STRING);
                        resultMap.put("CUSTMEMOLINE3", EMPTY_STRING);

                    }
                    if (txnNarrative.length() > IFM_CUST_MEMO1_MAX_LENGTH && txnNarrative.length() <= IFM_CUST_MEMO2_MAX_LENGTH) {

                        resultMap.put("CUSTMEMOLINE1", txnNarrative.substring(CONSTANT_ZERO, IFM_CUST_MEMO1_MAX_LENGTH));
                        resultMap.put("CUSTMEMOLINE2", txnNarrative.substring(IFM_CUST_MEMO1_MAX_LENGTH, txnNarrative.length()));
                        resultMap.put("CUSTMEMOLINE3", EMPTY_STRING);
                    }
                    if (txnNarrative.length() > IFM_CUST_MEMO2_MAX_LENGTH && txnNarrative.length() <= UB_TXN_NARRATIVE_MAX_LENGTH) {
                        resultMap.put("CUSTMEMOLINE1", txnNarrative.substring(CONSTANT_ZERO, IFM_CUST_MEMO1_MAX_LENGTH));
                        resultMap
                                .put("CUSTMEMOLINE2", txnNarrative.substring(IFM_CUST_MEMO1_MAX_LENGTH, IFM_CUST_MEMO2_MAX_LENGTH));

                        if (txnNarrative.length() <= UB_TXN_NARRATIVE_MAX_LENGTH) {
                            resultMap
                                    .put("CUSTMEMOLINE3", txnNarrative.substring(IFM_CUST_MEMO2_MAX_LENGTH, txnNarrative.length()));

                        }
                        else {
                            resultMap.put("CUSTMEMOLINE3", txnNarrative.substring(IFM_CUST_MEMO2_MAX_LENGTH,
                                    UB_TXN_NARRATIVE_MAX_LENGTH));

                        }
                    }
                }
                catch (IndexOutOfBoundsException exIndexOutOfBoundsException) {
                    logger.error(ExceptionUtil.getExceptionAsString(exIndexOutOfBoundsException));
                }
            }
        }
        else {
            resultMap.put("CUSTMEMOLINE1", EMPTY_STRING);
            resultMap.put("CUSTMEMOLINE2", EMPTY_STRING);
            resultMap.put("CUSTMEMOLINE3", EMPTY_STRING);
        }
    }
}
