/**
 * * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 
 */
package com.trapedza.bankfusion.scheduler;

import java.util.Date;
import java.util.Hashtable;

import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItem;

public class SwiftInterimStatementItem implements IItem {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private String businessProcessID;
    private Hashtable mappings;
    private String itemHanlderID;
    private Date nextProcessingDateTime;
    private Date laststatementdate;
    private int laststatementnumber;
    private String periodcode;
    private int stmtday;
    private int stmtmonth;
    private int periodunit;
    private String swtmessagetype;
    private String swtstmtflag;
    private String accountid;
    private int messagestatus;
    private int numberOfStatements;
    private int interval;
    private String startTime;
    private Hashtable bpProperties;
    private Object bpReturnObject = null;

    public String getBpID() {
        // TODO Auto-generated method stub
        return this.businessProcessID;
    }

    public Hashtable getBpProperties() {
        // TODO Auto-generated method stub
        return bpProperties;
    }

    public Date getExpiryDate() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getItemHanlderID() {
        // TODO Auto-generated method stub
        return itemHanlderID;
    }

    public String getItemID() {
        // TODO Auto-generated method stub
        return null;
    }

    public byte getItemStatus() {
        return (byte) messagestatus;
        // TODO Auto-generated method stub

    }

    public long getMaxDelayPeriod() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getPeriodCode() {
        // TODO Auto-generated method stub
        return periodcode;

    }

    public short getPeriodUnits() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getScheduleTime() {
        // TODO Auto-generated method stub
        return nextProcessingDateTime.getTime();
    }

    public Object getBPReturnObject() {
        return bpReturnObject;
    }

    public void setBPReturnObject(Object returnObject) {
        bpReturnObject = returnObject;
    }

    public void setBpID(String arg0) {
        this.businessProcessID = arg0;
        // TODO Auto-generated method stub

    }

    public void setBpProperties(Hashtable bpProperties) {
        this.bpProperties = bpProperties;
        // TODO Auto-generated method stub

    }

    public void setItemHanlderID(String arg0) {
        this.itemHanlderID = arg0;
        // TODO Auto-generated method stub

    }

    public void setItemStatus(byte status) {
        messagestatus = (int) status;

        // TODO Auto-generated method stub

    }

    public void setNextProcessingDateTime(Date arg0) {
        nextProcessingDateTime = arg0;

    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public int getLaststatementnumber() {
        return laststatementnumber;
    }

    public void setLaststatementnumber(int laststatementnumber) {
        this.laststatementnumber = laststatementnumber;
    }

    public Date getLaststatementdate() {
        return laststatementdate;
    }

    public void setLaststatementdate(Date laststatementdate) {
        this.laststatementdate = laststatementdate;
    }

    public void setPeriodcode(String periodcode) {
        this.periodcode = periodcode;
    }

    public int getPeriodunit() {
        return periodunit;
    }

    public void setPeriodunit(int periodunit) {
        this.periodunit = periodunit;
    }

    public int getStmtday() {
        return stmtday;
    }

    public void setStmtday(int stmtday) {
        this.stmtday = stmtday;
    }

    public int getStmtmonth() {
        return stmtmonth;
    }

    public void setStmtmonth(int stmtmonth) {
        this.stmtmonth = stmtmonth;
    }

    public String getSwtmessagetype() {
        return swtmessagetype;
    }

    public void setSwtmessagetype(String swtmessagetype) {
        this.swtmessagetype = swtmessagetype;
    }

    public String getSwtstmtflag() {
        return swtstmtflag;
    }

    public void setSwtstmtflag(String swtstmtflag) {
        this.swtstmtflag = swtstmtflag;
    }

    public int getMessagestatus() {
        return messagestatus;
    }

    public void setMessagestatus(int messagestatus) {
        this.messagestatus = messagestatus;
    }

    public int getNumberOfStatements() {
        return numberOfStatements;
    }

    public void setNumberOfStatements(int numberOfStatements) {
        this.numberOfStatements = numberOfStatements;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

}
