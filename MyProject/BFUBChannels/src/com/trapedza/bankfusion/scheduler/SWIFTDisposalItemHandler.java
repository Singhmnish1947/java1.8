/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: SWIFTDisposalItemHandler.java,v 1.1.2.2 2008/11/27 06:31:04 shreyasm Exp $
 *
 * $Log: SWIFTDisposalItemHandler.java,v $
 * Revision 1.1.2.2  2008/11/27 06:31:04  shreyasm
 * updated for bug#14800
 *
 * Revision 1.1.2.1  2008/10/17 23:54:35  zubink
 * SWIFT handler to pick up ietms from the disposal table and send them as messages.
 *
 *
 */
package com.trapedza.bankfusion.scheduler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItem;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItemStatusCodes;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler;
import com.trapedza.bankfusion.scheduler.core.ExecuteScheduledBP;
import com.trapedza.bankfusion.scheduler.item.ItemStatusCodes;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * The Class SWIFTDisposalItemHandler will poll the disposal table for messages to be sent.
 * 
 * @AUTHOR Zubin Kavarana
 * @PROJECT SWIFT
 */

public class SWIFTDisposalItemHandler extends BOBasedItemHandler {

    private static final String REQUESTER_ID = "Application.itemHandler.SWIFTDisposalItemHandler";

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;

    /** <code>ID</code> = "STIH". */
    public static final String itemHandlerID = "STIH";

    /** For logging/debug/error message. */
    private transient final static Log logger = LogFactory.getLog(SWIFTDisposalItemHandler.class.getName());

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.BOBasedItemHandler#getBOName()
     */
    public String getBOName() {
        return IBOSWTDisposal.BONAME;
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
        return "where " + IBOSWTDisposal.MESSAGESTATUS + " = ? AND " + IBOSWTDisposal.INTERESTDATE + " < ?";
    }

    /**
     * returns the Vector that contains the query parameters.
     * 
     * @param from
     *            the from
     * @param to
     *            the to
     * 
     * @return the query parameters
     */
    public ArrayList getQueryParameters(Timestamp from, Timestamp to) {
        ArrayList params = new ArrayList();// TODO
        params.add(0);
        params.add(SystemInformationManager.getInstance().getBFBusinessDate());
        return params;
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.trapedza.bankfusion.scheduler.BOBasedItemHandler#populateScheduledItem(com.trapedza.
     * bankfusion.core.SimplePersistentObject)
     */
    public IItem populateScheduledItem(SimplePersistentObject persistedItem) {
        SWIFTDisposalItem schedulerItem = new SWIFTDisposalItem();

        IBOSWTDisposal swiftDisposal = null;
        try {
            swiftDisposal = (IBOSWTDisposal) persistedItem;
            schedulerItem.setItemID(swiftDisposal.getBoID());
            schedulerItem.setBpID(SWIFTDisposalItem.MESSAGE_GENERATOR_SRV);
            schedulerItem.setNextProcessingDateTime(SystemInformationManager.getInstance().getBFBusinessDateTime());
            Hashtable startFatomData = new Hashtable();
            startFatomData.put("DealNumber", swiftDisposal.getF_DEALNO());
            startFatomData.put("DisposalId", swiftDisposal.getBoID());
            schedulerItem.setBpProperties(startFatomData);
            schedulerItem.setItemHanlderID(getItemHandlerID());
            schedulerItem.setItemStatus(ItemStatusCodes.SCHEDULED);
        }
        catch (BankFusionException bfe) {
                logger.error(
                        ">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + "Error reading swt disposal queue for scheduling",
                        bfe);
        }
        return schedulerItem;
    }

    public String getStatusQuery() {
        return "where " + IBOSWTDisposal.MESSAGESTATUS + " = ? ";
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

    public boolean executeItem(IItem scheduledItem) {

        SWIFTDisposalItem swiftDisposalItem = (SWIFTDisposalItem) scheduledItem;
        boolean executed = false;
        executed = new ExecuteScheduledBP().executeBP(swiftDisposalItem);
        return executed;
    }

    /**
     * This method is called twice -
     * 
     * 1. before execution to update the item status to queued
     * 
     * 2. After scheduler has processed the scheduled item this method is called to update the state
     * of the item and the deal. this has to be done as if processing breaks during a settlement
     * before setting the deal status to failed, it needs to be updated here.
     * 
     * @param scheduledItem
     *            the scheduled item
     */
    public void updateItem(IItem scheduledItem, IPersistenceObjectsFactory factory) {
        try {
            SWIFTDisposalItem swiftDisposalItem = (SWIFTDisposalItem) scheduledItem;
            IBOSWTDisposal swiftDisposal = (IBOSWTDisposal) factory.findByPrimaryKey(IBOSWTDisposal.BONAME,
                    swiftDisposalItem.getItemID(), false);
            int itemStatus = swiftDisposalItem.getItemStatus();
            swiftDisposal.setF_INTERESTDATE(SystemInformationManager.getInstance().getBFBusinessDate());
            updateSWIFTDisposalSchedulerStatus(itemStatus, swiftDisposal, scheduledItem, factory);
        }
        catch (BankFusionException exception) {
                logger.error("A Bankfusion exception has occured", exception);
            return;
        }
    }

    private void updateSWIFTDisposalSchedulerStatus(int itemStatus, IBOSWTDisposal swiftDisposal, IItem scheduledItem,
            IPersistenceObjectsFactory factory) {
        switch (itemStatus) {
            case IItemStatusCodes.DROPPED:
                break;
            case IItemStatusCodes.EXPIRED:
                break;
            case IItemStatusCodes.FAILED:
                swiftDisposal.setF_MESSAGESTATUS(16);
                ScheduledItemEventLogger.logFailuer(factory, scheduledItem, swiftDisposal.getF_CONTRAACCOUNTID());
                break;
            case IItemStatusCodes.PROCESSED:
                break;
            case IItemStatusCodes.QUEUED:
                break;
            case IItemStatusCodes.RESCHEDULED:
                break;
            case IItemStatusCodes.SCHEDULED:
                break;
            case IItemStatusCodes.UNKNOWN:
                break;
            default:
                break;
        }
    }

    /**
     * Remove item from the thread pool and the database
     */
    public void unScheduleItem(IItem item, BankFusionEnvironment env) {
        removeItem(item);
    }

    @Override
    public String getRequesterID() {
        return REQUESTER_ID;
    }
}
