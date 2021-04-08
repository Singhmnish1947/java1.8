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
package com.misys.ub.interfaces.ti.steps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIPOSTINGMSG;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_EODPostingHandlerFatom;

/**
 * @author ravir
 * @date March 31, 2010
 * @project Universal Banking
 * @Description:
 */

public class EODReconPostingHandlerFatom extends AbstractUB_TIP_EODPostingHandlerFatom {

    public EODReconPostingHandlerFatom(BankFusionEnvironment env) {
        // TODO Auto-generated constructor stub
        super(env);
    }

    private static final transient Log logger = LogFactory.getLog(EODReconPostingHandlerFatom.class.getName());

    private final String updateErrorCodeInMessageHeaderWhereClause = " WHERE " + IBOUB_INF_MessageHeader.MESSAGEID2
            + " =  ? AND TO_DATE(TO_CHAR( " + IBOUB_INF_MessageHeader.MSGRECEIVEDTTM + ", 'DD-MON-YY')) = ?  ";

    private final String fetchTIUBTxnDetailsFromTIPosting = " WHERE " + IBOUB_TIP_TIPOSTINGMSG.TRANSACTIONID + " = ?  AND "
            + IBOUB_TIP_TIPOSTINGMSG.NARRATIVE + " = ?";

    public void process(BankFusionEnvironment env) throws BankFusionException {

        String tiTxnID = getF_IN_TITxnID();
        String ubTxnID = getF_IN_UBTxnID();
        String tiNarrative = getF_IN_TINarrative();
        String channelID = getF_IN_CHannelID();
        String columnName = getF_IN_ColumnName();
        boolean errorOccured = false;
        ArrayList params = new ArrayList();
        ArrayList columnList = new ArrayList();
        ArrayList valueList = new ArrayList();
        try {
            Map<String, String> valueMap = new HashMap();

            valueMap.put("TITxnID", tiTxnID);
            valueMap.put("UBTransactionID", ubTxnID);
            valueMap.put("TINarrative", tiNarrative);
            valueMap.put("ChannelID", channelID);
            valueMap.put("ColumnName", columnName);
            MFExecuter.executeMF("UB_TIP_EODPostingHandler_SRV", env, valueMap);

        }
        catch (BankFusionException exp) {

            errorOccured = true;
            Collection<IEvent> events = exp.getEvents();

            int eventNumber = 0;

            Iterator<IEvent> itr = events.iterator();

            while (itr.hasNext()) {

                IEvent e1 = itr.next();

                eventNumber = e1.getEventNumber();

                break;
            }
            if (eventNumber == CommonConstants.INTEGER_ZERO) {
                eventNumber = 40422013;
            }

            params.add(tiTxnID);
            params.add(tiNarrative);
            Date txnDate = getTransactionDate(params);
            params.clear();

            params.add(tiTxnID);
            params.add(txnDate);
            columnList.add(IBOUB_INF_MessageHeader.ERRORCODE);
            valueList.add(eventNumber);

        }
        finally {
            if (errorOccured) {
                BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                BankFusionThreadLocal.getPersistanceFactory().beginTransaction();  //

                updateMessageHeader(params, columnList, valueList);
            }
            else {
                BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
                BankFusionThreadLocal.getPersistanceFactory().beginTransaction();  //

            }
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
        }

    }

    public Date getTransactionDate(ArrayList params) {
        IBOUB_TIP_TIPOSTINGMSG getTIPostingtxnReference = (IBOUB_TIP_TIPOSTINGMSG) BankFusionThreadLocal.getPersistanceFactory()
                .findByQuery(IBOUB_TIP_TIPOSTINGMSG.BONAME, fetchTIUBTxnDetailsFromTIPosting, params, null).get(0);

        return new Date(getTIPostingtxnReference.getF_TRANSACTIONDTTM().getTime());
    }

    public void updateMessageHeader(ArrayList params, ArrayList columnList, ArrayList valueList) {

        BankFusionThreadLocal.getPersistanceFactory().bulkUpdate(IBOUB_INF_MessageHeader.BONAME,
                updateErrorCodeInMessageHeaderWhereClause, params, columnList, valueList);
        BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

    }
}
