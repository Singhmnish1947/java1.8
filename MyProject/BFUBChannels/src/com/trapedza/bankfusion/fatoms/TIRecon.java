/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.bankfusion.subsystem.security.ISystemLoginProvider;
import com.misys.bankfusion.subsystem.security.runtime.impl.LoginProvider;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_BRANCH;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_ERRORMSGMAP;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIPOSTINGMSG;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_TIP_TIUBPOSTINGMSG;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_TIRecon;

/**
 * @author Gaurav Aggarwal
 * @date 25 Feb 2018
 * @project Universal Banking
 * @Description This class is used to reconcile all the TI failed transactions.
 * 
 */

public class TIRecon extends AbstractUB_TIP_TIRecon {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";

    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(TIRecon.class.getName());

    public TIRecon(BankFusionEnvironment env) {
        super(env);
    }

    private static final String microflowiD = "UB_TIP_AcceptPostingMessage_SRV";
    private static final String FAILED = "F";
    private static final String getMessageIdErrorWhereClause = " WHERE " + IBOUB_INF_MessageHeader.MESSAGEID2 + " = ? AND " + 
                        IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ? AND " +IBOUB_INF_MessageHeader.CHANNELID + " = 'TI' ";
    private static final String updateMessageStatus = " WHERE " + IBOUB_INF_MessageHeader.MESSAGEID2 + " = ? AND " + 
            IBOUB_INF_MessageHeader.MESSAGESTATUS + " IN ('P' , 'F') AND " +IBOUB_INF_MessageHeader.CHANNELID + " = 'TI' "; 
    
    

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_TIRecon#process(com.trapedza.bankfusion
     * .servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        List<String> faildTxnsList = listFaildTxns();
        for (String txnID : faildTxnsList) {
            reconcileTITxn(txnID);
            BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
        }
    }

    /**
     * This task of this method is to invoke all the relevant method for reconcilation.
     * 
     * @param txnID
     */
    private void reconcileTITxn(String txnID) {
        updateMessageHeader(txnID);
        deleteTIUBPostingMessageEntries(txnID);
        BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
        postTransaction(txnID);
        updateMessageHeaderForFailTxns(txnID);
    }
    
    private void updateMessageHeaderForFailTxns(String txnID) {
        ArrayList params = new ArrayList();
        params.add(txnID);
        params.add(FAILED);
        List<IBOUB_INF_MessageHeader> headerBOItems = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBOUB_INF_MessageHeader.BONAME, getMessageIdErrorWhereClause ,params, null,true);
        if (headerBOItems != null && !headerBOItems.isEmpty()) {
            params = new ArrayList();
            params.add(txnID);
            headerBOItems = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOUB_INF_MessageHeader.BONAME,
                    updateMessageStatus , params, null,true);
            for (IBOUB_INF_MessageHeader headerBOItem : headerBOItems) {
                headerBOItem.setF_MESSAGESTATUS("R");
            }
        }
    } 

    /**
     * This method is used to post the transaction. This method will post all legs of the
     * transaction. If any exception occurs system roll backs the transaction and set the failure
     * reason in message header with status and writes a record in error message map table.
     * 
     * @param txnID
     */
    private void postTransaction(String txnID) {
        String userLocator = null;
        String status = "P";
        ArrayList params = new ArrayList();
        params.add(txnID);
        List<IBOUB_TIP_TIPOSTINGMSG> tiPostingMsgList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBOUB_TIP_TIPOSTINGMSG.BONAME, " WHERE " + IBOUB_TIP_TIPOSTINGMSG.TRANSACTIONID + " = ? ", params, null);
        String messageId = CommonConstants.EMPTY_STRING;
        for (IBOUB_TIP_TIPOSTINGMSG tiPostingMsg : tiPostingMsgList) {
            messageId = tiPostingMsg.getBoID();
            params.clear();
            params.add(tiPostingMsg.getF_TRANSACTIONID());
            params.add(FAILED);
            List<IBOUB_INF_MessageHeader> errorInPreviousLeg = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                    IBOUB_INF_MessageHeader.BONAME, getMessageIdErrorWhereClause, params, null, false);
            if (errorInPreviousLeg == null || errorInPreviousLeg.size() <= 0) {
                try {
                    HashMap inputMap = prepareInputMap(tiPostingMsg);
                    
                   
                    HashMap outputParams=MFExecuter.executeMF(microflowiD, inputMap, BankFusionThreadLocal.getUserLocator().getStringRepresentation());
                    Integer errorCodeMF = (Integer) outputParams.get("errorCode");
                    String desc = (String) outputParams.get("errorDesc");
                    if (errorCodeMF.intValue() > 0) {
                        BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
                        status = FAILED;
                    }
                    IBOUB_INF_MessageHeader msgHeader = (IBOUB_INF_MessageHeader) BankFusionThreadLocal.getPersistanceFactory()
                            .findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, messageId);
                    msgHeader.setF_MESSAGESTATUS(status);
                    if (status.equals(FAILED))
                        msgHeader.setF_ERRORCODE(errorCodeMF);
                    if (status.equals(FAILED)) {
                        IBOUB_INF_ERRORMSGMAP insertErrorMsg = (IBOUB_INF_ERRORMSGMAP) BankFusionThreadLocal
                                .getPersistanceFactory().getStatelessNewInstance(IBOUB_INF_ERRORMSGMAP.BONAME);
                        insertErrorMsg.setBoID(messageId);
                        insertErrorMsg.setF_EVENTCODENUMBER(errorCodeMF);
                        insertErrorMsg.setF_IFMNAKMESSAGE(desc);
                        BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_ERRORMSGMAP.BONAME, insertErrorMsg);
                        break;
                    }
                }
                catch (BankFusionException bfException) {
                    BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                    BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
                    int errorCode = 0;
                    logger.error("Txn Failed due to : " + bfException.getMessage() + BatchUtil.getExceptionAsString(bfException));
                    int exceptionSize = bfException.getEvents().size();
                    if (exceptionSize >= 1) {
                        Iterator<IEvent> exception = bfException.getEvents().iterator();
                        while (exception.hasNext()) {
                            IEvent singleException = exception.next();
                            errorCode = singleException.getEventNumber();
                        }
                    }
                    String errorDesc = ((bfException.getLocalizedMessage() != null) ? bfException.getLocalizedMessage()
                            : bfException.getMessage());

                    IBOUB_INF_ERRORMSGMAP insertErrorMsg = (IBOUB_INF_ERRORMSGMAP) BankFusionThreadLocal.getPersistanceFactory()
                            .getStatelessNewInstance(IBOUB_INF_ERRORMSGMAP.BONAME);
                    insertErrorMsg.setBoID(messageId);
                    insertErrorMsg.setF_EVENTCODENUMBER(errorCode);
                    insertErrorMsg.setF_IFMNAKMESSAGE(errorDesc.length() < 100 ? errorDesc : errorDesc.substring(0, 99));
                    BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_ERRORMSGMAP.BONAME, insertErrorMsg);
                    IBOUB_INF_MessageHeader msgHeader = (IBOUB_INF_MessageHeader) BankFusionThreadLocal.getPersistanceFactory()
                            .findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, messageId);
                    msgHeader.setF_ERRORCODE(errorCode);
                    msgHeader.setF_MESSAGESTATUS(FAILED);
                    deleteTIUBPostingMessageEntries(tiPostingMsg.getF_TRANSACTIONID());
                    break;
                }
                catch (Exception exception) {
                    BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                    BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
                    int errorCode = 40000127;
                    logger.error("Txn Failed due to : " + exception.getMessage() + BatchUtil.getExceptionAsString(exception));
                    String errorDesc = ((exception.getLocalizedMessage() != null) ? exception.getLocalizedMessage() : exception
                            .getMessage());
                    IBOUB_INF_ERRORMSGMAP insertErrorMsg = (IBOUB_INF_ERRORMSGMAP) BankFusionThreadLocal.getPersistanceFactory()
                            .getStatelessNewInstance(IBOUB_INF_ERRORMSGMAP.BONAME);
                    insertErrorMsg.setBoID(messageId);
                    insertErrorMsg.setF_EVENTCODENUMBER(errorCode);
                    insertErrorMsg.setF_IFMNAKMESSAGE(errorDesc.length() < 100 ? errorDesc : errorDesc.substring(0, 99));
                    BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_ERRORMSGMAP.BONAME, insertErrorMsg);
                    IBOUB_INF_MessageHeader msgHeader = (IBOUB_INF_MessageHeader) BankFusionThreadLocal.getPersistanceFactory()
                            .findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, messageId);
                    msgHeader.setF_ERRORCODE(errorCode);
                    msgHeader.setF_MESSAGESTATUS(FAILED);
                    deleteTIUBPostingMessageEntries(tiPostingMsg.getF_TRANSACTIONID());
                    break;
                }
            }
        }
    }

   

    /**
     * This method is used to prepare the input for accept posting service.
     * 
     * @param tiPostingMsg
     * @return
     */
    private HashMap prepareInputMap(IBOUB_TIP_TIPOSTINGMSG tiPostingMsg) {
        HashMap inputParams = new HashMap();
        inputParams.put("ACCOUNTTYPE", tiPostingMsg.getF_ACCOUNTTYPE());
        inputParams.put("AMOUNT", tiPostingMsg.getF_AMOUNT().toString());
        inputParams.put("AMOUNTSIGN", tiPostingMsg.getF_AMOUNTSIGN());
        inputParams.put("BASECURRENCYCODE", tiPostingMsg.getF_BASEEQUIVCURRENCYCODE());
        inputParams.put("BASEEQUIVALENT", tiPostingMsg.getF_BASEEQUIVAMOUNT());
        inputParams.put("BranchSortCode", retrieveBranchCode(tiPostingMsg.getF_BRANCHNO()));
        inputParams.put("CustomerCode", tiPostingMsg.getF_CUSTOMERCODE());
        inputParams.put("Header_messageType", CommonConstants.EMPTY_STRING);
        inputParams.put("MESSAGEID", tiPostingMsg.getBoID());
        inputParams.put("MESSAGETYPE", CommonConstants.EMPTY_STRING);
        inputParams.put("NARRATIVE", tiPostingMsg.getF_NARRATIVE());
        inputParams.put("origCtxID", CommonConstants.EMPTY_STRING);
        inputParams.put("PRIMARYID", tiPostingMsg.getF_PRIMARYID());
        inputParams.put("SERIALNO",  tiPostingMsg.getF_SERIALNO());
        inputParams.put("SIGN", CommonConstants.EMPTY_STRING);
        inputParams.put("TOTALTXN", "" + tiPostingMsg.getF_TOTALTXNLEGS());
        inputParams.put("TRANSACTIONCODE", tiPostingMsg.getF_TRANSACTIONCODE());
        inputParams.put("TRANSACTIONDATE", tiPostingMsg.getF_TRANSACTIONDTTM());
        inputParams.put("TRANSACTIONID", tiPostingMsg.getF_TRANSACTIONID());
        inputParams.put("TRANSACTIONREF", tiPostingMsg.getF_TRANSACTIONREF());
        inputParams.put("TXNCURRENCYCODE", tiPostingMsg.getF_TXNCURRENCYCODE());
        inputParams.put("VALUEDATE", tiPostingMsg.getF_VALUEDTTM());
        return inputParams;
    }

    /**
     * This method is sued to update the status and error number in message header for the
     * transaction Id. This method will invoke the deletion of error message map table also.
     * 
     * @param txnID
     */
    private void updateMessageHeader(String txnID) {
        ArrayList params = new ArrayList();
        params.add(txnID);
        List<IBOUB_INF_MessageHeader> headerBOItems = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBOUB_INF_MessageHeader.BONAME, " WHERE " + IBOUB_INF_MessageHeader.MESSAGEID2 + " = ? AND " + IBOUB_INF_MessageHeader.CHANNELID + " = 'TI' " , params, null);
        for (IBOUB_INF_MessageHeader headerBOItem : headerBOItems) {
            headerBOItem.setF_ERRORCODE(0);
            headerBOItem.setF_MESSAGESTATUS("R");
            headerBOItem.setF_MSGLASTUPDTDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
            deleteErrorMessageMap(headerBOItem.getBoID());
        }
    }

    /**
     * This method is used to delete the records from TIUBPosting table for the transaction ID.
     * System will loop all the transactions for transaction id.
     * 
     * @param txnID
     */
    private void deleteTIUBPostingMessageEntries(String txnID) {
        ArrayList params = new ArrayList();
        params.add(txnID);
        logger.info("delete :"+txnID);
        List<IBOUB_TIP_TIUBPOSTINGMSG> tiPostingMsgList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBOUB_TIP_TIUBPOSTINGMSG.BONAME, " WHERE " + IBOUB_TIP_TIUBPOSTINGMSG.TITRANSACTIONID + " = ? ", params, null);
        for (IBOUB_TIP_TIUBPOSTINGMSG tiPostingMsg : tiPostingMsgList) {
            BankFusionThreadLocal.getPersistanceFactory().remove(IBOUB_TIP_TIUBPOSTINGMSG.BONAME, tiPostingMsg);
        }
    }

    /**
     * This method is used to delete all the failed transactions records from error message map
     * table. Deletion will be done by message id .
     * 
     * @param messageID
     */
    private void deleteErrorMessageMap(String messageID) {
        IBOUB_INF_ERRORMSGMAP errorMessageMap = (IBOUB_INF_ERRORMSGMAP) BankFusionThreadLocal.getPersistanceFactory()
            .findByPrimaryKey(IBOUB_INF_ERRORMSGMAP.BONAME, messageID, true);
        if(null != errorMessageMap)
            BankFusionThreadLocal.getPersistanceFactory().remove(IBOUB_INF_ERRORMSGMAP.BONAME, errorMessageMap.getBoID());
    }

    /**
     * This method is used to lust all the failed transaction needs to be reconciled with the
     * process.
     * 
     * @return
     */
    private List<String> listFaildTxns() {
        ArrayList<String> faildTxnIDList = new ArrayList<String>();
        List<IBOUB_INF_MessageHeader> faildTxnsList = new ArrayList<IBOUB_INF_MessageHeader>();
        String failedStatus = "F";
        String recievedStatus = "R";
        String channelID = "TI";
        ArrayList params = new ArrayList();
        params.add(channelID);
        params.add(failedStatus);
        params.add(recievedStatus);
        faildTxnsList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBOUB_INF_MessageHeader.BONAME,
                " WHERE " + IBOUB_INF_MessageHeader.CHANNELID + " = ? AND (" + IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ? OR "
                        + IBOUB_INF_MessageHeader.MESSAGESTATUS + " = ? )", params, null);
        for (IBOUB_INF_MessageHeader failedTxn : faildTxnsList) {
            if (!faildTxnIDList.contains(failedTxn.getF_MESSAGEID2())) {
                faildTxnIDList.add(failedTxn.getF_MESSAGEID2());
            }
        }
        return faildTxnIDList;
    }

    /**
     * This method is used to retrieve the branch code for TI.
     * 
     * @param branchCode
     * @return
     */
    private String retrieveBranchCode(String branchCode) {
        if (branchCode != null && branchCode.trim().length() > 0) {
            return branchCode;
        }
        else {
            ArrayList params = new ArrayList();
            params.add(BankFusionThreadLocal.getUserSession().getBranchSortCode());
            @SuppressWarnings("FBPE")
            List branchDetails = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOUB_INF_BRANCH.BONAME,
                    " WHERE " + IBOUB_INF_BRANCH.UBBRANCHSORTCODE + " = ? ", params, null);
            if (branchDetails != null && !branchDetails.isEmpty()) {
                return ((IBOUB_INF_BRANCH) branchDetails.get(0)).getF_BRANCHCODE();
            }
        }
        return CommonConstants.EMPTY_STRING;
    }
}