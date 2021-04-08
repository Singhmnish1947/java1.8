/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: BPWFailedOfflineItemHandler.java,v 1.0.0.0  2009/11/0 06:31:04  kc satish $
 *
 */
package com.trapedza.bankfusion.scheduler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.eventcode.ErrorEvent;
import com.misys.bankfusion.subsystem.task.runtime.exception.CollectedEventsDialogException;
import com.misys.cbs.common.functions.CB_CMN_SetProperty;
import com.trapedza.bankfusion.bo.refimpl.IBOBPW_OfflinePostings;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.PostingEngineConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.fatoms.QueryCountFatom;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItem;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItemStatusCodes;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler;
import com.trapedza.bankfusion.scheduler.core.ExecuteScheduledBP;
import com.trapedza.bankfusion.scheduler.item.ItemStatusCodes;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

/**
 * The Class BPWFailedOfflineItemHandler will poll the UBTB_BPWOFFLINEPOSTINGS table to post the
 * failed offline transactions.
 * 
 * @AUTHOR Gaurav Aggarwal
 * @PROJECT BPW
 */

public class BPWFailedOfflineItemHandler extends BOBasedItemHandler {

    private static final String REQUESTER_ID = "Application.itemHandler.BPWFailedOfflineItemHandler";

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;

    /** <code>ID</code> = "STIH". */
    public static final String itemHandlerID = "BPW_OFFLINE_IH";

    /** For logging/debug/error message. */
    private transient final static Log logger = LogFactory.getLog(BPWFailedOfflineItemHandler.class.getName());

    /** number of retrys for the failed transactions */
    private static final int RETRY_COUNT = 5;

    private static final String QUERY = "where " + IBOBPW_OfflinePostings.UBSTATUS + " NOT IN (?, ?, ?, ?) AND "
            + IBOBPW_OfflinePostings.UBNEXTPROCESSINGDTTM + " < ? ORDER BY " + IBOBPW_OfflinePostings.UBNEXTPROCESSINGDTTM;

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.BOBasedItemHandler#getBOName()
     */
    public String getBOName() {
        return IBOBPW_OfflinePostings.BONAME;
    }

    /**
     * Should return a unique ID.
     * 
     * @return the item handler ID
     */
    public String getItemHandlerID() {
        return itemHandlerID;
    }

    /**
     * return the query for reading the queue.
     * 
     * @return the query
     */
    public String getQuery() {
        return QUERY;
    }

    @Override
    public String getQueryWithOrderBy() {
        return QUERY;
    }

    /**
     * returns the Vector that contains the query parameters.
     * 
     * @param from
     *            the from
     * @param to
     *            the to
     * @return the query parameters
     */
    public ArrayList getQueryParameters(Timestamp from, Timestamp to) {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(new Integer(ItemStatusCodes.PROCESSED));
        params.add(new Integer(ItemStatusCodes.QUEUED));
        params.add(new Integer(ItemStatusCodes.CANCELED));
        params.add(new Integer(IItemStatusCodes.DROPPED));
        params.add(SystemInformationManager.getInstance().getBFBusinessDateTime());
        // TimeStamp retryTime = new TimeStamp
        // (SystemInformationManager.getInstance().getBFBusinessDateTime().getTime() -
        // (RETRY_WITH_INTERVAL * 60 *1000));
        // params.add(new Integer(RETRY_COUNT));
        return params;
    }

    // @Override
    public List<String> getQueryColumnList() {
        ArrayList<String> colList = new ArrayList<String>();
        colList.add(IBOBPW_OfflinePostings.UBRECIDPK);
        return colList;
    }

    // @Override
    public List<SimplePersistentObject> populateSimplePersistentObject(List items) {
        ArrayList<SimplePersistentObject> itemList = new ArrayList<SimplePersistentObject>();
        for (int record = 0; record < items.size(); record++) {
            itemList.add(BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOBPW_OfflinePostings.BONAME,
                    items.get(record).toString(), false));
        }
        return itemList;
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.trapedza.bankfusion.scheduler.BOBasedItemHandler#populateScheduledItem(com.trapedza.
     * bankfusion.core.SimplePersistentObject)
     */
    public IItem populateScheduledItem(SimplePersistentObject persistedItem) {
        BPWFailedOfflineItem item = new BPWFailedOfflineItem();

        IBOBPW_OfflinePostings offlinePostings = null;
        try {
            offlinePostings = (IBOBPW_OfflinePostings) persistedItem;
            item.setItemID(offlinePostings.getBoID());
            item.setBpID(BPWFailedOfflineItem.BPID_SRV);
            item.setNextProcessingDateTime(SystemInformationManager.getInstance().getBFBusinessDateTime());
            Hashtable reqestMappings = (Hashtable) BankFusionIOSupport.convertFromBytes(offlinePostings.getF_UBPELEGS());
            item.setMappings(reqestMappings);
            item.setBpProperties(reqestMappings);
            item.setItemHanlderID(getItemHandlerID());
            item.setItemStatus(ItemStatusCodes.SCHEDULED);

        }
        catch (BankFusionException bfe) {
            if (logger.isErrorEnabled()) {
                logger.error(">>>>>>>>>>>" + Thread.currentThread().getName() + ": "
                        + "Error reading UBTB_BPWOFFLINEPOSTINGS queue for scheduling", bfe);
            }
        }
        return item;
    }

    public String getStatusQuery() {
        return "where " + IBOBPW_OfflinePostings.UBSTATUS + " = ? ";
    }

    /**
     * the scheduler will call this method when an item is requested to be scheduled. It is
     * particularly important to use scheduler manager to schedule items on behalf of item handlers.
     * As scheduler may want to check if the item is due immediately (i.e. within the current time
     * span of fetched items) The client modules of scheduler may want to bypass this if the
     * scheduled items are always ahead of the time span.
     * 
     * @param itemToSchedule
     *            the item to schedule
     * @param env
     *            the env @ * the bank fusion exception
     */
    public void scheduleItem(IItem itemToSchedule, BankFusionEnvironment env) {

    }

    /**
     *
     */
    public boolean executeItem(IItem scheduledItem) {

        BPWFailedOfflineItem bpwFailedOfflineItem = (BPWFailedOfflineItem) scheduledItem;

        // Check wether the transaction might posted in possible duplicate mode.
        String txnRef = (String) bpwFailedOfflineItem.getMappings()
                .get("1" + PostingEngineConstants.SEPERATOR + PostingEngineConstants.POSTINGMESSAGETRANSACTIONREFERENCE);
        if (isDuplicateTransaction(txnRef)) {
            bpwFailedOfflineItem.getMappings().put("duplicateTxn", true);
            return true;
        }
        bpwFailedOfflineItem.getMappings().put("duplicateTxn", false);
        boolean executed = false;
        try {
            executed = new ExecuteScheduledBP().executeBP(bpwFailedOfflineItem);
        }
        catch (Exception exception) {
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            executed = false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + "BPW OFFLINE TRANASACTION "
                    + bpwFailedOfflineItem.getItemID());
        }
        bpwFailedOfflineItem.getMappings().put("currentStatus", executed);
        return executed;

    }

    /**
     * This method is called twice - 1. before execution to update the item status to queued 2.
     * After scheduler is processed, if the status of the process is success, then the record will
     * be deleted rom the BPW_OfflinePostings table else, update the transaction status with
     * appropriate error code and increments the retry count.
     * 
     * @param scheduledItem
     *            the scheduled item
     */
    public void updateItem(IItem scheduledItem, IPersistenceObjectsFactory factory) {
        try {
            BPWFailedOfflineItem bpwFailedOfflineItem = (BPWFailedOfflineItem) scheduledItem;
            IBOBPW_OfflinePostings failedTransaction = (IBOBPW_OfflinePostings) factory
                    .findByPrimaryKey(IBOBPW_OfflinePostings.BONAME, bpwFailedOfflineItem.getItemID(), false);
            boolean duplicateTxn = false;
            if (bpwFailedOfflineItem.getMappings().keySet().contains("duplicateTxn")) {
                duplicateTxn = (Boolean) bpwFailedOfflineItem.getMappings().get("duplicateTxn");
                if (duplicateTxn) {
                    failedTransaction.setF_UBSTATUS(IItemStatusCodes.DROPPED);
                    failedTransaction.setF_UBERRORNUMBER(null);
                }
                else if (bpwFailedOfflineItem.getMappings().keySet().contains("currentStatus")) {
                    // boolean currStatus = (Boolean)
                    // bpwFailedOfflineItem.getMappings().get("currentStatus");
                    if (scheduledItem.getItemStatus() == IItemStatusCodes.PROCESSED) {
                        failedTransaction.setF_UBSTATUS(IItemStatusCodes.PROCESSED);
                        failedTransaction.setF_UBNEXTPROCESSINGDTTM(null);
                        failedTransaction.setF_UBERRORNUMBER(null);
                    }
                    else {
                        if (scheduledItem.getItemStatus() != IItemStatusCodes.QUEUED) {
                            int retryCount = failedTransaction.getF_UBRETRYCOUNT() + 1;
                            failedTransaction.setF_UBRETRYCOUNT(retryCount);
                            failedTransaction.setF_UBNEXTPROCESSINGDTTM(getNextProcessingTime());
                            if (retryCount < RETRY_COUNT) {
                                failedTransaction.setF_UBSTATUS(IItemStatusCodes.FAILED);
                            }
                            else {
                                failedTransaction.setF_UBSTATUS(IItemStatusCodes.CANCELED);
                                failedTransaction.setF_UBNEXTPROCESSINGDTTM(null);
                            }
                            updateOfflinePostingErrorNumber(scheduledItem, failedTransaction);
                        }
                    }
                }
            }
            failedTransaction.setF_UBLASTPROCESSEDDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
        }
        catch (BankFusionException exception) {
            if (logger.isErrorEnabled()) {
                logger.error("A Bankfusion exception has occured", exception);
            }
            return;
        }
    }

    /**
     * For otherthan queued items, the method will update the failure status with appropriate UB
     * errorcode.
     * 
     * @param scheduledItem
     * @param failedTransaction
     */
    private void updateOfflinePostingErrorNumber(IItem scheduledItem, IBOBPW_OfflinePostings failedTransaction) {
        if (scheduledItem.getBPReturnObject() != null) {
            if (scheduledItem.getBPReturnObject() instanceof CollectedEventsDialogException) {
                String eventString = "";
                List<ErrorEvent> errors = ((CollectedEventsDialogException) scheduledItem.getBPReturnObject()).getErrors();
                for (ErrorEvent runTimeError : errors) {
                    eventString = String.valueOf(runTimeError.getEventNumber());
                }
                failedTransaction.setF_UBERRORNUMBER(eventString);
                return;
            }
            if (scheduledItem.getBPReturnObject() instanceof BankFusionException) {
                BankFusionException exception = (BankFusionException) scheduledItem.getBPReturnObject();
                failedTransaction.setF_UBERRORNUMBER(String.valueOf(exception.getMessageNumber()));
                return;
            }
            else if (scheduledItem.getBPReturnObject() instanceof Exception) {
                failedTransaction.setF_UBERRORNUMBER("-1");
                return;
            }
            failedTransaction.setF_UBERRORNUMBER(null);
        }
        else {
            // An Unknown error occured while execution.
            failedTransaction.setF_UBERRORNUMBER("-99");
        }
    }

    /**
     * 
     * @param txnRef
     * @return
     */
    private boolean isDuplicateTransaction(String txnRef) {
        BankFusionEnvironment environment = BankFusionThreadLocal.getBankFusionEnvironment();
        VectorTable m = CB_CMN_SetProperty.run(new VectorTable(), "1", txnRef);

        // Check in TRANSACTION table
        QueryCountFatom queryCount = new QueryCountFatom(environment);
        queryCount.setF_IN_boName(IBOTransaction.BONAME);
        queryCount.setF_IN_whereClause(IBOTransaction.REFERENCE + " = ?");
        queryCount.setF_IN_Params(m);

        queryCount.process(environment);
        int txnCount = queryCount.getF_OUT_rowCount();

        if (txnCount > 0)
            return true;

        // Check in FINANCIALPOSTINGMSG table
        queryCount.setF_IN_boName(IBOFinancialPostingMessage.BONAME);
        queryCount.setF_IN_whereClause(IBOFinancialPostingMessage.TRANSACTIONREF + " = ? ");
        queryCount.setF_IN_Params(m);

        queryCount.process(environment);
        txnCount = queryCount.getF_OUT_rowCount();

        if (txnCount > 0)
            return true;

        return false;
    }

    /**
     * Remove item from the thread pool and the database
     */
    public void unScheduleItem(IItem item, BankFusionEnvironment env) {
        removeItem(item);
    }

    /**
     * Returns the next processing time for BPW offline records.
     * 
     * @return
     */
    private static Timestamp getNextProcessingTime() {
        return new Timestamp(SystemInformationManager.getInstance().getBFBusinessDateTime().getTime()
                + (BPWFailedOfflineItem.RETRY_ITEM_WITH_INTERVAL * 60 * 1000));
    }

    public String getRequesterID() {
        return REQUESTER_ID;
    }
}
