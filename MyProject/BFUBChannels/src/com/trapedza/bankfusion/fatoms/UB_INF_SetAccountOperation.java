package com.trapedza.bankfusion.fatoms;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_SetAccountOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.ub.types.ubintfc.AccAndLobDtlsListRq;
import bf.com.misys.ub.types.ubintfc.AccIdList;

public class UB_INF_SetAccountOperation extends AbstractUB_INF_SetAccountOperation {

    private static final long serialVersionUID = 4926014834568159866L;
    private static final transient Log logger = LogFactory.getLog(UB_INF_AccountEnableDisable.class.getName());
    private static final String GET_ACCOUNT_STATUS = "SELECT INISACTIVE FROM INFTB_ACCOUNTINFMAP WHERE INACCOUNTID=? AND ININTERFACEID=?";
    protected IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    public UB_INF_SetAccountOperation() {
        super();
    }

    public UB_INF_SetAccountOperation(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) throws BankFusionException {
        logger.info("Start of SetAccountOperation");
        AccAndLobDtlsListRq accAndLobDtlsListRq = this.getF_IN_accAndLobDtlsListRq();
        Object[] accounts = this.getF_IN_ACCOUNTS().getColumnIgnoreCase("ACCOUNT");
        Object[] select = this.getF_IN_ACCOUNTS().getColumnIgnoreCase("SELECT");

        if (null != select) {
            int noOfSelectedAccounts = 0;
            for (int i = 0; i < select.length; ++i) {
                if ((boolean) select[i]) {
                    ++noOfSelectedAccounts;
                }
            }
            String lob = accAndLobDtlsListRq.getLob();
            AccIdList accId = null;
            AccIdList[] accIdList = new AccIdList[noOfSelectedAccounts];

            for (int i = 0, j = 0; i < accounts.length; ++i) {
                if ((boolean) select[i]) {
                    accId = new AccIdList();
                    accId.setAccId(accounts[i].toString());
                    accId.setAccOperation(fetchStatusFromTable(accId.getAccId(), lob));
                    accIdList[j] = accId;
                    logger.info("Account Id : " + accIdList[j].getAccId());
                    logger.info("Account Operation : " + accIdList[j].getAccOperation());
                    ++j;
                }
            }
            accAndLobDtlsListRq.setAccIdList(accIdList);
            logger.info("End of SetAccountOperation");
        }

    }

    private String fetchStatusFromTable(String accId, String lob) {
        String accountOperation = null;
        Connection connection = factory.getJDBCConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(GET_ACCOUNT_STATUS);
            pstmt.setString(1, accId);
            pstmt.setString(2, lob);
            rs = pstmt.executeQuery();
            // pstmt.close();
            if (rs.next()) {
                if (rs.getString(1).equalsIgnoreCase("Y")) {
                    accountOperation = "D";
                }
                else if (rs.getString(1).equalsIgnoreCase("N")) {
                    accountOperation = "R";
                }
            }
            else {
                accountOperation = "E";
            }
        }
        catch (SQLException e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (SQLException e) {
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException e) {
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
            }
        }

        return accountOperation;
    }
}