/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.fatoms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.events.Event;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.dc.common.MPMConstants;
import com.misys.ub.dc.common.OnlineTransactionMapper;
import com.misys.ub.dc.sql.constants.SqlSelectStatements;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.misys.ub.utils.restServices.RetrieveLOBServiceHelper;
import com.misys.ub.utils.types.LineOfBusinessListRq;
import com.misys.ub.utils.types.LineOfBusinessListRs;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_FAB_FABCONFIGURATION;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_TransactionPush;

import bf.com.misys.cbs.types.events.Event;

public class UB_CMN_TransactionPush extends AbstractUB_CMN_TransactionPush {

    /**
     * 
     */
	
	private IBusinessInformationService BIZ_INFO_SERVICE = (IBusinessInformationService)ServiceManagerFactory.getInstance().getServiceManager().getServiceForName("BusinessInformationService");
	
	private static final transient Log logger = LogFactory
			.getLog(UB_CMN_TransactionPush.class.getName());
    private static final long serialVersionUID = -7188325553059292057L;
	public static final String extnClauseForATMPOSAPITxns = "SELECT TCPD.*, TCPD.UBUNMASKEDCARDNUMBER AS ATMCARDID FROM UBTB_TRANSACTIONCOUNTERPTYDATA TCPD   "
			+ " WHERE UBTRANSACTIONID = ? AND UBTRANSACTIONDIRECTION = ?";
    
    public UB_CMN_TransactionPush() {
        super();
    }

    public UB_CMN_TransactionPush(BankFusionEnvironment env) {
        super(env);
    }

    @SuppressWarnings({ "deprecation", "rawtypes" })
    public void process(BankFusionEnvironment env) throws BankFusionException {

        String txnBO = (String) getF_IN_txnBO();
        if (txnBO == null || txnBO.isEmpty()) {
            return;
        }

        RetrieveLOBServiceHelper lobListRetriever = new RetrieveLOBServiceHelper();
        LineOfBusinessListRs resp = null;
        LineOfBusinessListRq inputRq = new LineOfBusinessListRq();
        Gson gson = new Gson();
        String response = null;
        IBOTransaction boTransaction = null;
        JsonObject txnObj = null;
        int eventId = 0;
        PreparedStatement stmt = null;
        ResultSet rs = null;
      
        boTransaction = (IBOTransaction) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOTransaction.BONAME, txnBO);
        boolean isAtmPosTxn = "ATM".equalsIgnoreCase(boTransaction.getF_UBCHANNELID())||"POS".equalsIgnoreCase(boTransaction.getF_UBCHANNELID());
        response = gson.toJson(boTransaction);

        inputRq.setAccountId(boTransaction.getF_ACCOUNTPRODUCT_ACCPRODID());
        resp = lobListRetriever.fetchLOBList(inputRq);

        txnObj = new JsonParser().parse(response).getAsJsonObject();
        txnObj.addProperty("f_CUSTOMERID", resp.getCustomerId());
        eventId = ((Event) env.getPassedData().get("CBS_EVENT_OBJECT")).getEventNumber();
        
       if (eventId == ChannelsEventCodes.I_TRANSACTION_EXTRENAL_PRODUCT) {
            txnObj.addProperty("f_EVENTTYPE", "EXT");
            String ATMMode= getModuleConfigValue("ATM_REQUEST_MODE", "ATM");
            try {
            	if(isAtmPosTxn) {
            		if (ATMMode.equals("API")) {
            			//for api
            			stmt = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(extnClauseForATMPOSAPITxns);
            		}else {
            			//for bfm
            			stmt = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(SqlSelectStatements.extnClauseForATMPOSTxns);
            		}
            	} else {
            		stmt = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(SqlSelectStatements.extnClause);
            	}
                
            	stmt.setString(1, boTransaction.getF_TRANSACTIONID());
                if (boTransaction.getF_DEBITCREDITFLAG().equals(MPMConstants.DEBIT_FLAG)) {
                    stmt.setString(2, "O");
                } else {
                    stmt.setString(2, "I");
                }
                rs = stmt.executeQuery();
                if ((!(rs == null)) && (rs.next() != false)) {
                    txnObj.addProperty("UBTRANSACTIONSUBTYPE", rs.getString("UBTRANSACTIONSUBTYPE"));
                    txnObj.addProperty("UBTRANSACTIONDIRECTION", rs.getString("UBTRANSACTIONDIRECTION"));
                    txnObj.addProperty("UBCUSTOMERNAME", rs.getString("UBCUSTOMERNAME"));
                    txnObj.addProperty("UBCONTRAACCNUM", rs.getString("UBCONTRAACCNUM"));
                    txnObj.addProperty("UBCHANNELNAME", rs.getString("UBCHANNELNAME"));
                    txnObj.addProperty("UBMERCHANTNAME", rs.getString("UBMERCHANTNAME"));
                    if(isAtmPosTxn) {
                    	txnObj.addProperty("UBCARDNUMBER", rs.getString("ATMCARDID"));}
                	else {
                		txnObj.addProperty("UBCARDNUMBER", "");}
                    
                }

                HashMap balances = AvailableBalanceFunction.run(boTransaction.getF_ACCOUNTPRODUCT_ACCPRODID());
                for (Object balance : balances.keySet()) {
                    txnObj.addProperty(balance.toString(), balances.get(balance).toString());
                }

                if (boTransaction.getF_UBCHANNELID().equals(MPMConstants.UB_CHANNEL_ID)
                    && boTransaction.getF_DEBITCREDITFLAG().equals(MPMConstants.DEBIT_FLAG)) {
                    stmt = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(SqlSelectStatements.ddTxnInfoClause);
                    stmt.setString(1, boTransaction.getF_TRANSACTIONID());
                    rs = stmt.executeQuery();

                    if (rs != null && rs.next() == true) {
                        txnObj.addProperty(MPMConstants.MP_PMT_ID_END_TO_END_ID, rs.getString(MPMConstants.MP_PMT_ID_END_TO_END_ID));
                        txnObj.addProperty(MPMConstants.MP_DD_TMRIA_ID_ORG_CD_SCHM_ID,
                            rs.getString(MPMConstants.MP_DD_TMRIA_ID_ORG_CD_SCHM_ID));
                        txnObj.addProperty(MPMConstants.MP_DD_TMRIMNDT_ID, rs.getString(MPMConstants.MP_DD_TMRIMNDT_ID));
                    }
                }
                if(boTransaction.getF_UBCHANNELID().equals(MPMConstants.BULK_PI_CHANNEL_ID) && boTransaction.getF_DEBITCREDITFLAG().equals(MPMConstants.DEBIT_FLAG)) {
                	stmt = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(SqlSelectStatements.ctTxnInfoClause);
                    stmt.setString(1, boTransaction.getF_TRANSACTIONID());
                    rs = stmt.executeQuery();
                    if (rs != null && rs.next() == true) {
                    	String fileName = rs.getString(MPMConstants.MP_CT_BULK_PMT_FILE_NAME);
                    	String[] fileNameTokens = fileName.split("_");
                    	if(fileNameTokens[0].equals("DC")) {
                    		txnObj.addProperty("DCBULKPMTREFERENCE", fileNameTokens[1]);
                    	}
                    }
                }

            } catch (Exception e) {
            	logger.error(ExceptionUtil.getExceptionAsString(e));
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    	logger.error(ExceptionUtil.getExceptionAsString(e));
                    }
                if (stmt != null)
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                    	logger.error(ExceptionUtil.getExceptionAsString(e));
                    }
            }
        } else if (eventId == ChannelsEventCodes.I_TRANSACTION_NOSTRO) {

            txnObj.addProperty("f_EVENTTYPE", "NOS");

            if (!resp.getLisOfBusinesses().contains("TREASURYFO")) {
                resp.getLisOfBusinesses().add("TREASURYFO");
            }

        } else if (eventId == ChannelsEventCodes.I_TRANSACTION_CURRENCY_POSITION) {

            txnObj.addProperty("f_EVENTTYPE", "POS");

            try {
                stmt = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection().prepareStatement(SqlSelectStatements.positionClause);
                stmt.setString(1, boTransaction.getBoID());
                rs = stmt.executeQuery();
                if ((!(rs == null)) && (rs.next() != false)) {
                    txnObj.addProperty("INAMOUNT1", rs.getDouble("INAMOUNT1"));
                    txnObj.addProperty("INCURRENCYCODE1", rs.getString("INCURRENCYCODE1"));
                    txnObj.addProperty("INAMOUNT2", rs.getDouble("INAMOUNT2"));
                    txnObj.addProperty("INCURRENCYCODE2", rs.getString("INCURRENCYCODE2"));
                    txnObj.addProperty("INEXCHANGERATE", rs.getString("INEXCHANGERATE"));
                } else {
                    return;
                }
            } catch (Exception e) {
            	logger.error(ExceptionUtil.getExceptionAsString(e));
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    	logger.error(ExceptionUtil.getExceptionAsString(e));
                    }
                if (stmt != null)
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                    	logger.error(ExceptionUtil.getExceptionAsString(e));
                    }
            }
            if (!resp.getLisOfBusinesses().contains("TREASURYFO")) {
                resp.getLisOfBusinesses().add("TREASURYFO");
            }
        }

        // Send message to each application which is in the list of LOBs
        sendMessageToApplications(resp, boTransaction, txnObj);
    }

    /**
     * @param LineOfBusinessListRs lineOfBusinessListRs
     * @param IBOTransaction boTransaction
     * @param JsonObject txnObj Sends message to each application which is in the list of LOBs
     */
    private void sendMessageToApplications(LineOfBusinessListRs lineOfBusinessListRs, IBOTransaction boTransaction, JsonObject txnObj) {
        for (String lob : lineOfBusinessListRs.getLisOfBusinesses()) {

            if ("FEESBILLING".equals(lob)) {
                if (isSendMessageToFeesBillingTrue(boTransaction)) {
                    try {
                        sendMessage(txnObj.toString(), "Transaction" + lob);
                    } catch (Exception e) {
                    	logger.error("Not able to connect to Queue:"+ "Transaction" + lob);
                    	logger.error(ExceptionUtil.getExceptionAsString(e));
                    }
                }
            }
            else if("DIGICHANNELS".equals(lob)) {
            	OnlineTransactionMapper dcTxnMapper=new OnlineTransactionMapper();
            	try {
            		logger.info("in sendMessageToApplications is " +txnObj.toString());
					dcTxnMapper.mapTransactionData(txnObj.toString());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("Exception occured while calling OnlineTransactionMapper::"+ExceptionUtil.getExceptionAsString(e));
				}
            	// sendMessage(txnObj.toString(), "Transaction" + lob);
            }
            else {
                try {
                    sendMessage(txnObj.toString(), "Transaction" + lob);
                } catch (Exception e) {
                	logger.error("Not able to connect to Queue:"+ "Transaction" + lob);
                	logger.error(ExceptionUtil.getExceptionAsString(e));
                }

            }
        }
    }

    /**
     * @param boTransaction check for fees & billing if transaction needs to be sent based on some conditions
     * @return isSendMessageToFeesBillingTrue
     */
    private boolean isSendMessageToFeesBillingTrue(IBOTransaction boTransaction) {
        boolean isSendMessageToFeesBillingTrue = false;
        IBOAccount accrow =
            (IBOAccount) getFactory().findByPrimaryKey(IBOAccount.BONAME, boTransaction.getF_ACCOUNTPRODUCT_ACCPRODID(), false);
        String productContextCode = accrow.getF_PRODUCTCONTEXTCODE();
        String whereClause = " WHERE " + IBOUB_FAB_FABCONFIGURATION.PRODUCTCONTEXTCODE + " = ?";
        ArrayList<String> params = new ArrayList<>();
        params.add(productContextCode);
        List<IBOUB_FAB_FABCONFIGURATION> records =
            getFactory().findByQuery(IBOUB_FAB_FABCONFIGURATION.BONAME, whereClause, params, null, false);

        if (records != null && !records.isEmpty() && accrow.getF_UBFABAPPLICABLE().equals("Y")) {
            ArrayList<String> txnCodes = new ArrayList<>();
            for (IBOUB_FAB_FABCONFIGURATION iboub_FAB_FABCONFIGURATION : records) {
                txnCodes.add(iboub_FAB_FABCONFIGURATION.getF_TRANSCODE());
            }
            if (!txnCodes.isEmpty() && txnCodes.contains(boTransaction.getF_CODE())) {
                isSendMessageToFeesBillingTrue = true;
            }
        }

        return isSendMessageToFeesBillingTrue;
    }

    private void sendMessage(String message, String endPoint) {
        MessageProducerUtil.sendMessage(message, endPoint);
    }

    private IPersistenceObjectsFactory getFactory() {
        return BankFusionThreadLocal.getPersistanceFactory();
    }
    private String getModuleConfigValue(String param, String moduleId) {

		String value = CommonConstants.EMPTY_STRING;
		value = (String) this.BIZ_INFO_SERVICE.getBizInfo().getModuleConfigurationValue(moduleId, param, null);
		return value;
	}
}
