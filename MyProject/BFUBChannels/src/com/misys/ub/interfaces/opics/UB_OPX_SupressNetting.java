package com.misys.ub.interfaces.opics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_SupressNetting;

public class UB_OPX_SupressNetting extends AbstractUB_OPX_SupressNetting {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final transient Log LOGGER = LogFactory.getLog(UB_OPX_SupressNetting.class.getName());
	private static final String UPDATE_SQL_QUERY_GROUP = "UPDATE BFDBUSR.INFTB_OPXPOSITIONACCTUPDTXN SET INSTATUS = 'P' WHERE INTRANSACTIONID = (SELECT TXN.TRANSACTIONSRID FROM BFDBUSR.UBTB_TRANSACTION TXN,BFDBUSR.FXDEALS FXDEALS WHERE TXN.TRANSACTIONSRID = ? and TXN.REFERENCE = FXDEALS.DEALREFERENCE)";
	protected IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	public UB_OPX_SupressNetting(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		factory.commitTransaction();
		factory.beginTransaction();
		String txnId = getF_IN_trsrid();
		PreparedStatement preparedStatement = null;
		Connection connection = factory.getJDBCConnection();
		try {
			preparedStatement = connection.prepareStatement(UPDATE_SQL_QUERY_GROUP);
			preparedStatement.setString(1, txnId);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			if (preparedStatement != null) {

				try {
					preparedStatement.close();
				} catch (SQLException e) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(e));
					;
				}
			}
		}
	}

}
