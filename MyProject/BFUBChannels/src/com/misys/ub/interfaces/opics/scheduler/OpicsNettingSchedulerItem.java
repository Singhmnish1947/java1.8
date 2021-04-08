/* **********************************************************
 * Copyright (c) 2009 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: OPICSNostroAndPositionNettingSchedulerItem.java,v 1.0 2009/05/14 ashishv Exp $
 * This is the scheduled item for Nostro and Transaction (Position) Update transaction netting 
 * which have to be handed-off to OPICS.
 */
package com.misys.ub.interfaces.opics.scheduler;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;

import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItem;
import com.trapedza.bankfusion.scheduler.item.ItemStatusCodes;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * 
 * 
 * @AUTHOR Zubin Kavarana/Ashish Vashishth
 * @PROJECT
 */
public class OpicsNettingSchedulerItem implements IItem {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}



    public static final long serialVersionUID = 1;

    private String bpID;

    private Hashtable bpProperties;

    private Date nextProcessingDateTime;

    private Date expiryDate;

    private String itemHanlderID;

    private String itemID = GUIDGen.getNewGUID();

    private String itemStatus;

    private String periodCode;

    private final int periodMultiplyer = 0;

    private Object bpReturnObject = null;
    
    public String getBpID() {
        return bpID;
    }

    public Hashtable getBpProperties() {
        return bpProperties;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public String getItemHanlderID() {
        return itemHanlderID;
    }

    public String getItemID() {
        return itemID;
    }

    public byte getItemStatus() {
        if (itemStatus.length() > 0) {
            return Byte.parseByte(itemStatus);
        }
        else {
            return ItemStatusCodes.UNKNOWN;
        }
    }

    public long getMaxDelayPeriod() {
        return 0L;
    }

    public String getPeriodCode() {
        return this.periodCode;
    }

    public short getPeriodUnits() {
        return (short) periodMultiplyer;
    }

    public long getScheduleTime() {
        if (processNostros && processPositions) {
            if (nextPositionsProcessingDateTime.before(nextNostroProcessingDateTime)) {
                return nextPositionsProcessingDateTime.getTime();
            } else {
                return nextNostroProcessingDateTime.getTime();
            }
        } else if (processNostros){
            return nextNostroProcessingDateTime.getTime();
        } else if (processPositions) {
            return nextPositionsProcessingDateTime.getTime();
        } else {
            return (SystemInformationManager.getInstance().getBFBusinessDateTime().getTime() + 6000);
        }
    }

    public void setBpID(String bpID) {
        this.bpID = bpID;
    }

    public void setBpProperties(Hashtable bpProperties) {
        this.bpProperties = bpProperties;
    }

    public void setItemHanlderID(String itemHanlderID) {
        this.itemHanlderID = itemHanlderID;
    }

    public void setItemStatus(byte itemStatus) {
        this.itemStatus = Byte.toString(itemStatus);
    }

    public void setNextProcessingDateTime(java.util.Date nextDateTime) {
        nextProcessingDateTime = nextDateTime;
    }
    
    public Date getNextProcessingDateTime() {
        return nextProcessingDateTime;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public Object getBPReturnObject() {
        return bpReturnObject;
    }

    public void setBPReturnObject(Object returnObject) {
        bpReturnObject = returnObject;
    }
    
    /* CUSTOM FIELDS FOR OPICS NETTING */
    private Timestamp nextPositionsProcessingDateTime;

    private Timestamp nextNostroProcessingDateTime;

    private boolean processNostros = false;

    private boolean processPositions = false;

    public boolean isProcessNostros() {
        return processNostros;
    }

    public void setProcessNostros(boolean processNostros) {
        this.processNostros = processNostros;
    }

    public boolean isProcessPositions() {
        return processPositions;
    }

    public void setProcessPositions(boolean processPositions) {
        this.processPositions = processPositions;
    }

    public Timestamp getNextPositionsProcessingDateTime() {
        return nextPositionsProcessingDateTime;
    }

    public void setNextPositionsProcessingDateTime(Timestamp nextPositionsProcessingDateTime) {
        this.nextPositionsProcessingDateTime = nextPositionsProcessingDateTime;
    }

    public Timestamp getNextNostroProcessingDateTime() {
        return nextNostroProcessingDateTime;
    }

    public void setNextNostroProcessingDateTime(Timestamp nextNostroProcessingDateTime) {
        this.nextNostroProcessingDateTime = nextNostroProcessingDateTime;
    }

}
