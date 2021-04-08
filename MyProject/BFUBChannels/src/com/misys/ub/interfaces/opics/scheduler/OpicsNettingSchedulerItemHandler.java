/* **********************************************************
 * Copyright (c) 2009 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: OPICSNostroAndPositionNettingSchedulerItemHandler.java,v 1.0 2009/05/14 ashishv Exp $
 *
 */
package com.misys.ub.interfaces.opics.scheduler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_OPX_NettingScheduler;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItem;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItemStatusCodes;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.scheduler.ScheduledItemEventLogger;
import com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler;
import com.trapedza.bankfusion.scheduler.core.ExecuteScheduledBP;
import com.trapedza.bankfusion.scheduler.item.ItemStatusCodes;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;

/**
 * 
 * @AUTHOR Zubin Kavarana/Ashish Vashishth
 * @PROJECT Universal Banking
 */

public class OpicsNettingSchedulerItemHandler extends BOBasedItemHandler implements OpicsNettingSchedulerItemHandlerMBean {

    private static final String REQUESTER_ID = "Application.itemHandler.OpicsNettingSchedulerItemHandler";

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;

    /** <code>ID</code> = "OPIH". */
    private static final String itemHandlerID = "OPIH";

    private static final long oneMinute = 60000;

    private static final String opicsHandoffFrequencyCondition = "where " + IBOUB_OPX_NettingScheduler.NEXTNOSTROUPDTTM
            + " < ? OR " + IBOUB_OPX_NettingScheduler.NEXTPOSITIONUPDTTM + " < ? ";

    private static final String opicsHandoffSchedulerCondition = " (" + IBOUB_OPX_NettingScheduler.SCHEDULERSTATUS + " = "
            + IItemStatusCodes.SCHEDULED + " OR " + IBOUB_OPX_NettingScheduler.SCHEDULERSTATUS + " = "
            + IItemStatusCodes.RESCHEDULED + ")";

    private static final String daemonExecutionCondition = opicsHandoffFrequencyCondition + " AND "
            + opicsHandoffSchedulerCondition;

    /** For logging/debug/error message. */
    private transient final static Log logger = LogFactory.getLog(OpicsNettingSchedulerItemHandler.class.getName());

    private static final String NO = "N";

    private static final String YES = "Y";

    private static boolean isNostroUpdateEnabled;

    private static boolean isPositionUpdateEnabled;

    private static boolean updateParameters;

    private static int nostroUpdateFrequency = CommonConstants.INTEGER_ZERO;

    private static int positionUpdateFrequency = CommonConstants.INTEGER_ZERO;

    static {
        ObjectName mBeanName;
        try {
            mBeanName = new ObjectName("UniversalBanking:Impl=OpicsNettingSchedulerItemHandler");
            ServiceManager.getInstance().registerMBean(new OpicsNettingSchedulerItemHandler(), mBeanName);
        }
        catch (MalformedObjectNameException e1) {
                logger.error(ExceptionUtil.getExceptionAsString(e1));
        }
        catch (BankFusionException e1) {
                logger.error(ExceptionUtil.getExceptionAsString(e1));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.BOBasedItemHandler#getBOName()
     */
    public String getBOName() {
        return IBOUB_OPX_NettingScheduler.BONAME;
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
        return daemonExecutionCondition;
    }

    /**
     * return the query for reading the status of the Scheduler
     * 
     * @return the query
     */
    public String getStatusQuery() {
        return "where " + IBOUB_OPX_NettingScheduler.SCHEDULERSTATUS + " = ? ";
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
    public ArrayList<Timestamp> getQueryParameters(Timestamp from, Timestamp to) {
        ArrayList<Timestamp> params = new ArrayList<Timestamp>();
        params.add(to);
        params.add(to);
        return params;
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.trapedza.bankfusion.scheduler.BOBasedItemHandler#populateScheduledItem(com.trapedza.
     * bankfusion.core.SimplePersistentObject)
     */
    public IItem populateScheduledItem(SimplePersistentObject persistedItem) {
        OpicsNettingSchedulerItem schedulerItem = new OpicsNettingSchedulerItem();

        IBOUB_OPX_NettingScheduler opicsBalanceHandoffConfig;

        try {
            opicsBalanceHandoffConfig = (IBOUB_OPX_NettingScheduler) persistedItem;

            Timestamp nextNostroProcessing = opicsBalanceHandoffConfig.getF_NEXTNOSTROUPDTTM();
            Timestamp nextPositionProcessing = opicsBalanceHandoffConfig.getF_NEXTPOSITIONUPDTTM();
            Timestamp now = SystemInformationManager.getInstance().getBFBusinessDateTime();
            if (!nextNostroProcessing.after(now) && opicsBalanceHandoffConfig.isF_ISNOSTROHANDOFFENABLE()) {
                schedulerItem.setProcessNostros(true);
                schedulerItem.setNextNostroProcessingDateTime(now);
                schedulerItem.setNextProcessingDateTime(now);
            }
            if (!nextPositionProcessing.after(now) && opicsBalanceHandoffConfig.isF_ISPOSITIONHANDOFFENABLE()) {
                schedulerItem.setProcessPositions(true);
                schedulerItem.setNextPositionsProcessingDateTime(now);
                schedulerItem.setNextProcessingDateTime(now);
            }

            schedulerItem.setItemID(opicsBalanceHandoffConfig.getBoID());
            schedulerItem.setBpID("UB_OPX_PositionAndNostroUpdateScheduleItemHandler");

            Hashtable<String, String> startFatomData = new Hashtable<String, String>();
            String isNostroScheduled = (schedulerItem.isProcessNostros()) ? YES : NO;
            String isPositionScheduled = (schedulerItem.isProcessPositions()) ? YES : NO;

            startFatomData.put("NOSTROSCHEDULED", isNostroScheduled);
            startFatomData.put("POSITIONSCHEDULED", isPositionScheduled);
            schedulerItem.setBpProperties(startFatomData);
            schedulerItem.setItemHanlderID(getItemHandlerID());
            schedulerItem.setItemStatus(ItemStatusCodes.SCHEDULED);
        }
        catch (BankFusionException bfe) {
                logger.error("Exception caught", bfe);
        }
        return schedulerItem;
    }

    /**
     * This is a daemon so it cannot be scheduled.
     * 
     * @param itemToSchedule
     *            the item to schedule
     * @param env
     *            the env @ * the bank fusion exception
     */
    public void scheduleItem(IItem itemToSchedule, BankFusionEnvironment env) {
    }

    /**
     * When the exact time is due for the item to be processed this method is called on the
     * ItemHandler.
     */
    public boolean executeItem(IItem scheduledItem) {
        Hashtable bpProperties = scheduledItem.getBpProperties();
        boolean isNostroScheduled = (bpProperties.get("NOSTROSCHEDULED").toString().equals(YES) ? true : false);
        boolean isPositionScheduled = (bpProperties.get("POSITIONSCHEDULED").toString().equals(YES) ? true : false);
        if (isNostroScheduled || isPositionScheduled) {
            OpicsNettingSchedulerItem opicsNettedBalancesItem = (OpicsNettingSchedulerItem) scheduledItem;
            boolean executed = new ExecuteScheduledBP().executeBP(opicsNettedBalancesItem);
            return executed;
        }
        else {
            scheduledItem.setItemStatus(IItemStatusCodes.SCHEDULED);
            return true;
        }
    }

    /**
     * This method is called twice -
     * 
     * 1. before execution to update the item status to queued
     * 
     * 2. After scheduler has processed the scheduled item this method is called to update the state
     * of the item.
     * 
     * @param schedulerValueObject
     *            the scheduled item
     */
    public void updateItem(IItem schedulerValueObject, IPersistenceObjectsFactory factory) {
        try {

            OpicsNettingSchedulerItem opicsDaemonValueObject = (OpicsNettingSchedulerItem) schedulerValueObject;
            int itemStatus = opicsDaemonValueObject.getItemStatus();

            IBOUB_OPX_NettingScheduler opicsBalancesHandoffItem = (IBOUB_OPX_NettingScheduler) factory.findByPrimaryKey(
                    IBOUB_OPX_NettingScheduler.BONAME, opicsDaemonValueObject.getItemID(), false);

            if (updateParameters) {
                opicsBalancesHandoffItem.setF_NOSTROUPDATEFREQUENCY(nostroUpdateFrequency);
                opicsBalancesHandoffItem.setF_POSITIONUPDATEFREQUENCY(positionUpdateFrequency);
                opicsBalancesHandoffItem.setF_ISNOSTROHANDOFFENABLE(isNostroUpdateEnabled);
                opicsBalancesHandoffItem.setF_ISPOSITIONHANDOFFENABLE(isPositionUpdateEnabled);
                updateParameters = false;
            }

            // Update the static variables from the database
            nostroUpdateFrequency = opicsBalancesHandoffItem.getF_NOSTROUPDATEFREQUENCY();
            positionUpdateFrequency = opicsBalancesHandoffItem.getF_POSITIONUPDATEFREQUENCY();
            isNostroUpdateEnabled = opicsBalancesHandoffItem.isF_ISNOSTROHANDOFFENABLE();
            isPositionUpdateEnabled = opicsBalancesHandoffItem.isF_ISPOSITIONHANDOFFENABLE();

            updateOpicsNettedBalancesSchedulerStatus(itemStatus, opicsBalancesHandoffItem, schedulerValueObject, factory);
            if (itemStatus == IItemStatusCodes.FAILED) {
                logger.error("A Bankfusion exception has occured which has stopped the daemon");
            }
            if (itemStatus == IItemStatusCodes.PROCESSED) {
                Timestamp now = SystemInformationManager.getInstance().getBFBusinessDateTime();
                if (opicsDaemonValueObject.isProcessNostros() && opicsBalancesHandoffItem.isF_ISNOSTROHANDOFFENABLE()) {
                    long nostroOffset = (opicsBalancesHandoffItem.getF_NOSTROUPDATEFREQUENCY() * oneMinute) - 1000;
                    opicsBalancesHandoffItem.setF_NEXTNOSTROUPDTTM(new Timestamp(now.getTime() + nostroOffset));
                }
                if (opicsDaemonValueObject.isProcessPositions() && opicsBalancesHandoffItem.isF_ISPOSITIONHANDOFFENABLE()) {
                    long positionOffset = (opicsBalancesHandoffItem.getF_POSITIONUPDATEFREQUENCY() * oneMinute) - 1000;
                    opicsBalancesHandoffItem.setF_NEXTPOSITIONUPDTTM(new Timestamp(now.getTime() + positionOffset));
                }
            }
        }
        catch (BankFusionException exception) {
                logger.error("A Bankfusion exception has occured", exception);
            return;
        }
        catch (Exception exception) {
                logger.error("An unknown exception has occured", exception);
            return;
        }
    }

    private void updateOpicsNettedBalancesSchedulerStatus(int itemStatus, IBOUB_OPX_NettingScheduler opicsBalancesHandoffItem,
            IItem schedulerValueObject, IPersistenceObjectsFactory factory) {
        switch (itemStatus) {
            case IItemStatusCodes.DROPPED:
                opicsBalancesHandoffItem.setF_SCHEDULERSTATUS(IItemStatusCodes.RESCHEDULED);
                break;
            case IItemStatusCodes.EXPIRED:
                opicsBalancesHandoffItem.setF_SCHEDULERSTATUS(IItemStatusCodes.EXPIRED);
                break;
            case IItemStatusCodes.FAILED:
                opicsBalancesHandoffItem.setF_SCHEDULERSTATUS(IItemStatusCodes.FAILED);
                ScheduledItemEventLogger.logFailuer(factory, schedulerValueObject, null);
                break;
            case IItemStatusCodes.PROCESSED:
                opicsBalancesHandoffItem.setF_SCHEDULERSTATUS(IItemStatusCodes.SCHEDULED);
                break;
            case IItemStatusCodes.QUEUED:
                opicsBalancesHandoffItem.setF_SCHEDULERSTATUS(IItemStatusCodes.QUEUED);
                break;
            case IItemStatusCodes.RESCHEDULED:
                opicsBalancesHandoffItem.setF_SCHEDULERSTATUS(IItemStatusCodes.RESCHEDULED);
                break;
            case IItemStatusCodes.SCHEDULED:
                opicsBalancesHandoffItem.setF_SCHEDULERSTATUS(IItemStatusCodes.SCHEDULED);
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

    /**
     * This method would return the Nosrto Update Netting frequency
     */
    public int getNostroUpdateFrequency() {
        return nostroUpdateFrequency;
    }

    /**
     * This method would return the Position Update Netting frequency
     */
    public int getPositionsUpdateFrequency() {
        return positionUpdateFrequency;
    }

    /**
     * This method would set the Nostro Update Netting Frequency
     */
    public void setNostroUpdateFrequency(int frequency) {
        nostroUpdateFrequency = frequency;
        updateParameters = true;
    }

    /**
     * This method would set the Position Update Netting frequency
     */
    public void setPositionsUpdateFrequency(int frequency) {
        positionUpdateFrequency = frequency;
        updateParameters = true;
    }

    /**
     * This method would return a "Y" when the Nostro Update Netting is enabled else would return a
     * "N"
     */
    public String getIsNostroUpdateEnabled() {
        return (isNostroUpdateEnabled ? YES : NO);
    }

    /**
     * This method would return a "Y" when the Position Update Netting is enabled else would return
     * a "N"
     */
    public String getIsPositionUpdateEnabled() {
        return (isPositionUpdateEnabled ? YES : NO);
    }

    /**
     * This method would accept a "Y" when the Nostro Update Netting is to be enabled and an "N" for
     * it to be disabled
     */
    public void setIsNostroUpdateEnabled(String flag) {
        isNostroUpdateEnabled = (flag.equals(YES));
        updateParameters = true;
    }

    /**
     * This method would accept a "Y" when the Position Update Netting is to be enabled and an "N"
     * for it to be disabled
     */
    public void setIsPositionUpdateEnabled(String flag) {
        isPositionUpdateEnabled = (flag.equals(YES));
        updateParameters = true;
    }

    @Override
    public String getRequesterID() {
        return REQUESTER_ID;
    }

}
