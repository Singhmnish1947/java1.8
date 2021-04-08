/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: SWIFTDisposalItem.java,v 1.3 2008/10/25 11:30:42 utpals Exp $
 *
 * $Log: SWIFTDisposalItem.java,v $
 * Revision 1.3  2008/10/25 11:30:42  utpals
 * Interfaces : Sprint5 Delivery
 *
 * Revision 1.1  2008/10/23 07:19:43  shailejar
 * *** empty log message ***
 *
 * Revision 1.1.2.1  2008/10/17 23:54:35  zubink
 * SWIFT handler to pick up ietms from the disposal table and send them as messages.
 *
 * Revision 1.1  2008/09/13 06:32:45  sushmag
 *
 * 
 */
package com.trapedza.bankfusion.scheduler;

import java.util.Date;
import java.util.Hashtable;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItem;
import com.trapedza.bankfusion.scheduler.item.ItemStatusCodes;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * The Class SWIFTDisposalItem is a DTO/Value Object for the SWIFT disposal item handler.
 * 
 * @AUTHOR Zubin Kavarana
 * @PROJECT SWIFT
 */
public class SWIFTDisposalItem implements IItem {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;

    /** The bp ID. */
    private String bpID;// The bpID of the bp to be processed/MicroFlow to be run

    /** The bp properties. */
    private Hashtable bpProperties;// Hashtable of parameters that will passed on to the ScheduledBP

    /** The next processing date time. */
    private Date nextProcessingDateTime;// The next processing date time for this item

    /** The expiry date. */
    private Date expiryDate;// Maturity Date

    /** The item hanlder ID. */
    private String itemHanlderID;// ItemHandlerID that is supposed to handle this item

    /** The item ID. */
    private String itemID = GUIDGen.getNewGUID();// The unique ID associated with this item

    /** The item status. */
    private String itemStatus;

    /** The period code. */
    private String periodCode;

    /** The period multiplyer. */
    private final int periodMultiplyer = 0;// Forex Deals are not periodic i.e they have only one
                                           // start date and one end date

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getBpID()
     */
    public String getBpID() {
        return bpID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getBpProperties()
     */
    public Hashtable getBpProperties() {
        return bpProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getExpiryDate()
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getItemHanlderID()
     */
    public String getItemHanlderID() {
        return itemHanlderID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getItemID()
     */
    public String getItemID() {
        return itemID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getItemStatus()
     */
    public byte getItemStatus() {
        if (itemStatus.length() > 0) {
            return Byte.parseByte(itemStatus);
        }
        else {
            return ItemStatusCodes.UNKNOWN;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getMaxDelayPeriod()
     */
    public long getMaxDelayPeriod() {
        return 0L;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getPeriodCode()
     */
    public String getPeriodCode() {
        return this.periodCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getPeriodUnits()
     */
    public short getPeriodUnits() {
        return (short) periodMultiplyer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#getScheduleTime()
     */
    public long getScheduleTime() {
        return nextProcessingDateTime.getTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#setBpID(java.lang.String)
     */
    public void setBpID(String bpID) {
        this.bpID = bpID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#setBpProperties(java.util.Hashtable)
     */
    public void setBpProperties(Hashtable bpProperties) {
        this.bpProperties = bpProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#setItemHanlderID(java.lang.String)
     */
    public void setItemHanlderID(String itemHanlderID) {
        this.itemHanlderID = itemHanlderID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#setItemStatus(byte)
     */
    public void setItemStatus(byte itemStatus) {
        this.itemStatus = Byte.toString(itemStatus);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.Item#setNextProcessingDateTime(java.util.Date)
     */
    public void setNextProcessingDateTime(java.util.Date nextDateTime) {
        nextProcessingDateTime = nextDateTime;
    }

    /**
     * Item interface does not have this method nevertheless we may need to set the item ID from
     * outside the class.
     * 
     * @param itemID
     *            the item ID
     */
    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    private Object bpReturnObject = null;

    public Object getBPReturnObject() {
        return bpReturnObject;
    }

    public void setBPReturnObject(Object returnObject) {
        bpReturnObject = returnObject;
    }

    // SWIFT TAGS
    private String dealNumber = CommonConstants.EMPTY_STRING;

    private String disposalId = CommonConstants.EMPTY_STRING;

    public static final String MESSAGE_GENERATOR_SRV = "UB_SWT_MessageGenerator_SRV";

    public String getDealNumber() {
        return dealNumber;
    }

    public void setDealNumber(String dealNumber) {
        this.dealNumber = dealNumber;
    }

    public String getDisposalId() {
        return disposalId;
    }

    public void setDisposalId(String disposalId) {
        this.disposalId = disposalId;
    }
}
