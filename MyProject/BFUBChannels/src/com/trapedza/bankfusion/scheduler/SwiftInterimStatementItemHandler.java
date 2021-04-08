/**
 * * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.

 */
package com.trapedza.bankfusion.scheduler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;

import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_SWTACCOUNTSTMT;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.gateway.scheduler.interfaces.IItem;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler;
import com.trapedza.bankfusion.scheduler.core.ExecuteScheduledBP;
import com.trapedza.bankfusion.scheduler.item.ItemStatusCodes;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * @author Vipesh.AP
 * 
 */
public class SwiftInterimStatementItemHandler extends BOBasedItemHandler {

    private static final String REQUESTER_ID = "Application.itemHandler.SwiftInterimStatementItemHandler";

    public static final String ID = "SWTID";
    public static final String Bpproccess942 = "UB_SWT_GenerateFrequencyBased942_SRV";
    public static final String Bpproccess940950 = "UB_SWT_GenerateMT940950_SRV";

    private static final String query = "where ( " + IBOUBTB_SWTACCOUNTSTMT.UBNEXTSTMTDT + " <= ? AND ("
            + IBOUBTB_SWTACCOUNTSTMT.UBMESSAGETYPE + " IN('942','950','940') AND " + IBOUBTB_SWTACCOUNTSTMT.UBFREQPERIODCODE
            + " IN('I')) ) ORDER BY " + IBOUBTB_SWTACCOUNTSTMT.UBNEXTSTMTDT;

    /**
     *
     */
    public SwiftInterimStatementItemHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#getBOName()
     */
    @Override
    public synchronized void lock() throws InterruptedException {
        // TODO Auto-generated method stub
        super.lock();
    }

    public synchronized void unLock() {
        // TODO Auto-generated method stub
        super.unLock();
    }

    public String getBOName() {
        // TODO Auto-generated method stub
        return IBOUBTB_SWTACCOUNTSTMT.BONAME;
    }

    public boolean executeItem(IItem stmtitem) {
        SwiftInterimStatementItem item = (SwiftInterimStatementItem) stmtitem;
        boolean executed = new ExecuteScheduledBP().executeBP(item);
        return executed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#getItemHandlerID()
     */
    @Override
    public String getItemHandlerID() {
        // TODO Auto-generated method stub
        return ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#getQuery()
     */
    @Override
    public String getQuery() {
        // TODO Auto-generated method stub
        return query;
    }

    @Override
    public String getQueryWithOrderBy() {
        return query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#getQueryParameters(java.sql.
     * Timestamp , java.sql.Timestamp)
     */
    @Override
    public ArrayList getQueryParameters(Timestamp arg0, Timestamp arg1) {
        // TODO Auto-generated method stub
        ArrayList params = new ArrayList();
        params.add(arg0);
        return params;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#getStatusQuery()
     */
    // @Override
    public String getStatusQuery() {
        // TODO Auto-generated method stub
        return "where " + IBOUBTB_SWTACCOUNTSTMT.UBMESSAGESTATUS + " = ? ";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#populateScheduledItem(com.trapedza
     * .bankfusion.core.SimplePersistentObject)
     */
    @Override
    public IItem populateScheduledItem(SimplePersistentObject persistedItem) {
        SwiftInterimStatementItem item = new SwiftInterimStatementItem();
        IBOUBTB_SWTACCOUNTSTMT accountstmt = (IBOUBTB_SWTACCOUNTSTMT) persistedItem;
        Hashtable startFatomData = new Hashtable();
        startFatomData.put("accountid", accountstmt.getF_UBACCOUNTID());
        startFatomData.put("laststatementnumber", accountstmt.getF_UBLASTSTMTNUMBER());
        startFatomData.put("periodcode", accountstmt.getF_UBFREQPERIODCODE());
        startFatomData.put("stmtmonth", accountstmt.getF_UBSTMTDAY());
        startFatomData.put("stmtday", accountstmt.getF_UBSTMTDAY());
        startFatomData.put("laststatementdate", accountstmt.getF_UBLASTSTMTDTTTM());
        startFatomData.put("swtmessagetype", accountstmt.getF_UBMESSAGETYPE());
        startFatomData.put("swtstmtflag", accountstmt.getF_UBTRANSSTMTFLAG());
        startFatomData.put("DeliveryChannel", accountstmt.getF_UBDELIVERYCHANNEL());
        startFatomData.put("BasisOfGeneration", accountstmt.getF_UBBASISOFGENERATION());
        startFatomData.put("StatementId", accountstmt.getBoID());
        startFatomData.put("interval", accountstmt.getF_UBINTERVAL());
        startFatomData.put("startTime", accountstmt.getF_UBSTARTTIME());
        startFatomData.put("numberOfStatements", accountstmt.getF_UBNUMBEROFSTATEMENTS());
        item.setMessagestatus(ItemStatusCodes.SCHEDULED);
        item.setBpProperties(startFatomData);
        int sume = 10;
        if ("942".equals(startFatomData.get("swtmessagetype").toString())) {
            item.setBpID(this.Bpproccess942);
        }
        else {
            item.setBpID(this.Bpproccess940950);
        }

        item.setNextProcessingDateTime(accountstmt.getF_UBNEXTSTMTDT());
        item.setItemHanlderID(this.getItemHandlerID());
        // TODO Auto-generated method stub
        return item;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#scheduleItem(com.trapedza.
     * bankfusion .gateway.scheduler.interfaces.IItem,
     * com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    @Override
    public void scheduleItem(IItem anitem, BankFusionEnvironment env) {
        /*
         * SwiftInterimStatementItem accitem = (SwiftInterimStatementItem) anitem; IBOScheduledItems
         * scheduledItems = (IBOScheduledItems) env.getFactory().getStatelessNewInstance(
         * IBOScheduledItems.BONAME); scheduledItems.setBoID(accitem.getItemID());
         * scheduledItems.setF_BUSINESSPROCESSID(accitem.getBpID());
         * scheduledItems.setF_BPPARAMS(BankFusionIOSupport
         * .convertToBytes(accitem.getBpProperties()));
         * env.getFactory().create(IBOScheduledItems.BONAME, scheduledItems);
         */
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#unScheduleItem(com.trapedza.
     * bankfusion .gateway.scheduler.interfaces.IItem,
     * com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    @Override
    public void unScheduleItem(IItem item, BankFusionEnvironment arg1) {
        removeItem(item);
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.scheduler.core.BOBasedItemHandler#updateItem(com.trapedza.bankfusion
     * .gateway.scheduler.interfaces.IItem,
     * com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory)
     */
    @Override
    public void updateItem(IItem stmtitem, IPersistenceObjectsFactory factory) {

        // IBOBankStatementFeature ibostmtItem = (IBOBankStatementFeature)
        // factory.findByPrimaryKey(IBOStandingOrder.BONAME, item.getSoNumber());
        // ClaculateNextStatementDate(ibostmtItem.getF_STMTDATE(),item.getPeriodecode(),item.getPeriodunit());
        switch (stmtitem.getItemStatus()) {
            case ItemStatusCodes.FAILED:
                ScheduledItemEventLogger.logFailuer(factory, stmtitem, null);
                break;
            case ItemStatusCodes.CANCELED:
                ScheduledItemEventLogger.logFailuer(factory, stmtitem, null);
                break;
            default:
                break;
        }

    }

    @Override
    public String getRequesterID() {
        return REQUESTER_ID;
    }

}
