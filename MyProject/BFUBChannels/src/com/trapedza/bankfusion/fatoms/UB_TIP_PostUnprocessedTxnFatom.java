/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UB_TIP_PostUnprocessedTxnFatom.java,v.1.0,Jun 17, 2009 11:40:12 AM ravir
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.misys.ub.forex.core.ForexConstants;
import com.misys.ub.ti.UB_TIP_ModuleConfigurationConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOFXPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIUBPOSTINGMSG;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_PostUnprocessedTxnFatom;

/**
 * @author ravir
 * @date Jun 17, 2009
 * @project Universal Banking
 * @Description:
 */

public class UB_TIP_PostUnprocessedTxnFatom extends AbstractUB_TIP_PostUnprocessedTxnFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    private final String MESSAGEID = "MESSAGEID";
    private final String BASEEQUIVALENT = "BASEEQUIVALENT";
    private final String TRANSACTIONCODE = "TRANSACTIONCODE";
    private final String TRANSACTIONDATE = "TRANSACTIONDATE";
    private final String BASECURRENCYCODE = "BASECURRENCYCODE";
    private final String TRANSACTIONREF = "TRANSACTIONREF";
    private final String TRANSACTIONID = "TRANSACTIONID";
    private final String FORCEPOST = "FORCEPOST";
    private final String PRIMARYID = "PRIMARYID";
    private final String NARRATIVE = "NARRATIVE";
    private final String AMOUNT = "AMOUNT";
    private final String TXNCURRENCYCODE = "TXNCURRENCYCODE";
    private final String SIGN = "SIGN";
    private final String EXCHRATETYPE = "EXCHRATETYPE";
    private final String SERIALNO = "SERIALNO";
    private final String VALUEDATE = "VALUEDATE";;

    private final String UB_TI_MESSAGETYPE = "GZH971";
    private final String UB_TI_PROCESSING_STATUS = "R";
    private final String UB_TI_CHANNEL_ID = "TI";
    private final String TXN_ID = "TXNID";
    private final String DEBIT_POSTING_ACTION = "-";
    private final String CREDIT_POSTING_ACTION = "+";
    private final int CREDIT_ACTION = 1;
    private final int DEBIT_ACTION = -1;
    private final int DEFAULT_SERIAL_NO = 99;
    private final String DEFAULT_EXCHANGE_RATE_TYPE = "SPOT";

    public UB_TIP_PostUnprocessedTxnFatom(BankFusionEnvironment env) {
        // TODO Auto-generated constructor stub
        super(env);
    }

    private final String fetchHeaderTxnDetailsQuery = "SELECT DISTINCT " + IBOUB_INF_MessageHeader.MESSAGEID2 + " AS TXNID FROM "
            + IBOUB_INF_MessageHeader.BONAME + " WHERE " + IBOUB_INF_MessageHeader.MESSAGETYPE + "= ?" + " AND "
            + IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ?" + " AND " + IBOUB_INF_MessageHeader.CHANNELID + " = ?";

    private final String fetchAllTxnDetails = " SELECT " + IBOUB_TIP_TIUBPOSTINGMSG.MESSAGEID + " AS MESSAGEID,"
            + IBOUB_TIP_TIUBPOSTINGMSG.AMOUNT + " AS AMOUNT," + IBOUB_TIP_TIUBPOSTINGMSG.BASEEQUIVALENT + " AS BASEEQUIVALENT , "
            + IBOUB_TIP_TIUBPOSTINGMSG.TRANSACTIONCODE + " AS TRANSACTIONCODE," + IBOUB_TIP_TIUBPOSTINGMSG.TRANSACTIONDATE
            + " AS TRANSACTIONDATE, " + IBOUB_TIP_TIUBPOSTINGMSG.BASECURRENCYCODE + " AS BASECURRENCYCODE , "
            + IBOUB_TIP_TIUBPOSTINGMSG.TRANSACTIONREF + " AS TRANSACTIONREF , " + IBOUB_TIP_TIUBPOSTINGMSG.TRANSACTIONID
            + " AS TRANSACTIONID , " + IBOUB_TIP_TIUBPOSTINGMSG.FORCEPOST + " AS FORCEPOST, " + IBOUB_TIP_TIUBPOSTINGMSG.PRIMARYID
            + " AS PRIMARYID, " + IBOUB_TIP_TIUBPOSTINGMSG.NARRATIVE + " AS NARRATIVE," + IBOUB_TIP_TIUBPOSTINGMSG.TXNCURRENCYCODE
            + " AS TXNCURRENCYCODE, " + IBOUB_TIP_TIUBPOSTINGMSG.SIGN + " AS SIGN," + IBOUB_TIP_TIUBPOSTINGMSG.EXCHRATETYPE
            + " AS EXCHRATETYPE," + IBOUB_TIP_TIUBPOSTINGMSG.SERIALNO + " AS SERIALNO," + IBOUB_TIP_TIUBPOSTINGMSG.VALUEDATE
            + " AS VALUEDATE " + " FROM " + IBOUB_TIP_TIUBPOSTINGMSG.BONAME + " WHERE " + IBOUB_TIP_TIUBPOSTINGMSG.TRANSACTIONID
            + " = ?";

    public void process(BankFusionEnvironment env) throws BankFusionException {

        String creditTxnCode = UB_TIP_ModuleConfigurationConstants.getUBTICreditTxnCode();
        String debitTxnCode = UB_TIP_ModuleConfigurationConstants.getUBTIDebitTxnCode();

        ArrayList<String> params = new ArrayList<String>();

        params.add(UB_TI_MESSAGETYPE);
        params.add(UB_TI_PROCESSING_STATUS);
        params.add(UB_TI_CHANNEL_ID);
        List messageHeaderTxnList = getFactory().executeGenericQuery(fetchHeaderTxnDetailsQuery, params, null);
        Iterator messageHeadertransactionItr = messageHeaderTxnList.iterator();
        while (messageHeadertransactionItr.hasNext()) {
            String txnID = CommonConstants.EMPTY_STRING;
            SimplePersistentObject simpleObject = null;
            Map resultMap = null;

            SimplePersistentObject transactionItem = (SimplePersistentObject) messageHeadertransactionItr.next();
            txnID = (String) transactionItem.getDataMap().get(TXN_ID);
            params.clear();
            params.add(txnID);
            List txnDetailsForPosting = getFactory().executeGenericQuery(fetchAllTxnDetails, params, null);
            BigDecimal finalDebitAmount = new BigDecimal(0);
            BigDecimal finalCreditAmount = new BigDecimal(0);
            BigDecimal finalContraAmount = new BigDecimal(0);
            Iterator transactionItr = txnDetailsForPosting.iterator();
            String messageID = CommonConstants.EMPTY_STRING;
            String debitCurrencyCode = CommonConstants.EMPTY_STRING;
            String creditCurrencyCode = CommonConstants.EMPTY_STRING;
            while (transactionItr.hasNext()) {

                resultMap = new HashMap();
                simpleObject = (SimplePersistentObject) transactionItr.next();
                messageID = (String) simpleObject.getDataMap().get(MESSAGEID);

                String postingAction = (String) simpleObject.getDataMap().get(SIGN);
                if (postingAction.equals(DEBIT_POSTING_ACTION)) {

                    BigDecimal debitTxnAmount = (BigDecimal) simpleObject.getDataMap().get(AMOUNT);
                    finalDebitAmount = finalDebitAmount.add(debitTxnAmount);
                    debitCurrencyCode = (String) simpleObject.getDataMap().get(TXNCURRENCYCODE);
                }
                else if (postingAction.equals(CREDIT_POSTING_ACTION)) {

                    BigDecimal creditTxnAmount = (BigDecimal) simpleObject.getDataMap().get(AMOUNT);
                    finalCreditAmount = finalCreditAmount.add(creditTxnAmount);
                    creditCurrencyCode = (String) (String) simpleObject.getDataMap().get(TXNCURRENCYCODE);
                }

                resultMap.put(MESSAGEID, simpleObject.getDataMap().get(MESSAGEID));
                resultMap.put(BASEEQUIVALENT, simpleObject.getDataMap().get(BASEEQUIVALENT));
                resultMap.put(TRANSACTIONCODE, simpleObject.getDataMap().get(TRANSACTIONCODE));
                resultMap.put(TRANSACTIONDATE, simpleObject.getDataMap().get(TRANSACTIONDATE));
                resultMap.put(BASECURRENCYCODE, simpleObject.getDataMap().get(BASECURRENCYCODE));
                resultMap.put(TRANSACTIONREF, simpleObject.getDataMap().get(TRANSACTIONREF));
                resultMap.put(TRANSACTIONID, simpleObject.getDataMap().get(TRANSACTIONID));
                resultMap.put(FORCEPOST, simpleObject.getDataMap().get(FORCEPOST));
                resultMap.put(PRIMARYID, simpleObject.getDataMap().get(PRIMARYID));
                resultMap.put(NARRATIVE, simpleObject.getDataMap().get(NARRATIVE));
                resultMap.put(AMOUNT, simpleObject.getDataMap().get(AMOUNT));
                resultMap.put(TXNCURRENCYCODE, simpleObject.getDataMap().get(TXNCURRENCYCODE));
                resultMap.put(SIGN, simpleObject.getDataMap().get(SIGN));
                resultMap.put(EXCHRATETYPE, simpleObject.getDataMap().get(EXCHRATETYPE));
                resultMap.put(SERIALNO, simpleObject.getDataMap().get(SERIALNO));
                resultMap.put(VALUEDATE, simpleObject.getDataMap().get(VALUEDATE));
                getF_OUT_PostingDetails().addAll(new VectorTable(resultMap));
            }
            int actionRequired = finalDebitAmount.compareTo(finalCreditAmount);
            Map contraMap = new HashMap();
            IBOUB_TIP_TIUBPOSTINGMSG postingDetails = (IBOUB_TIP_TIUBPOSTINGMSG) getFactory().findByPrimaryKey(
                    IBOUB_TIP_TIUBPOSTINGMSG.BONAME, messageID, false);
            if (actionRequired == DEBIT_ACTION) {
                finalContraAmount = finalCreditAmount.subtract(finalDebitAmount);
                getSuspenseAccountTxnDetails(finalContraAmount, postingDetails, contraMap, debitCurrencyCode, DEBIT_POSTING_ACTION,
                        debitTxnCode);

            }
            else if (actionRequired == CREDIT_ACTION) {
                finalContraAmount = finalDebitAmount.subtract(finalCreditAmount);
                getSuspenseAccountTxnDetails(finalContraAmount, postingDetails, contraMap, creditCurrencyCode,
                        CREDIT_POSTING_ACTION, creditTxnCode);
            }
            else {
                finalContraAmount = new BigDecimal(0);
                getSuspenseAccountTxnDetails(finalContraAmount, postingDetails, contraMap, creditCurrencyCode,
                        CREDIT_POSTING_ACTION, creditTxnCode);
            }

            getF_OUT_PostingDetails().addAll(new VectorTable(contraMap));
        }

    }

    public void getSuspenseAccountTxnDetails(BigDecimal finalContraAmout, IBOUB_TIP_TIUBPOSTINGMSG postingDetails, Map contraMap,
            String currencyCode, String sign, String txnCode) {
        String creditTxnCode = UB_TIP_ModuleConfigurationConstants.getUBTICreditTxnCode();
        String debitTxnCode = UB_TIP_ModuleConfigurationConstants.getUBTIDebitTxnCode();
        contraMap.put(MESSAGEID, "");
        contraMap.put(BASEEQUIVALENT, postingDetails.getF_BASEEQUIVALENT());
        contraMap.put(TRANSACTIONCODE, txnCode);
        contraMap.put(TRANSACTIONDATE, postingDetails.getF_TRANSACTIONDATE());
        contraMap.put(BASECURRENCYCODE, postingDetails.getF_BASECURRENCYCODE());
        contraMap.put(TRANSACTIONREF, postingDetails.TRANSACTIONREF);
        contraMap.put(TRANSACTIONID, postingDetails.getF_TRANSACTIONID());
        contraMap.put(FORCEPOST, postingDetails.isF_FORCEPOST());
        contraMap.put(PRIMARYID, postingDetails.getF_PRIMARYID());
        contraMap.put(NARRATIVE, postingDetails.getF_NARRATIVE());
        contraMap.put(AMOUNT, finalContraAmout);
        contraMap.put(TXNCURRENCYCODE, currencyCode);
        contraMap.put(SIGN, sign);
        contraMap.put(EXCHRATETYPE, DEFAULT_EXCHANGE_RATE_TYPE);
        contraMap.put(SERIALNO, new Integer(DEFAULT_SERIAL_NO));
        contraMap.put(VALUEDATE, postingDetails.getF_VALUEDATE());
    }

    private IPersistenceObjectsFactory getFactory() {

        return BankFusionThreadLocal.getPersistanceFactory();
    }

    private void setPseudonymContexts(IBOFXPostingMessage fxmessage) {
        Map<String, String> contextValues = new HashMap<String, String>();
        contextValues.put(ForexConstants.BRANCH_CONTEXT, "");
        contextValues.put(ForexConstants.DEALER_CONTEXT, "");
        contextValues.put(ForexConstants.CUSTOMER_CONTEXT, "");
        contextValues.put(ForexConstants.PRODUCT_CONTEXT, "");
        fxmessage.setF_PSEUDONYMCONTEXTVALUES(BankFusionIOSupport.convertToBytes(contextValues));
    }

}
