package com.trapedza.bankfusion.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.DBEncryptionUtilHelper;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TXN_CounterPartyUpdate;

public class UB_TXN_CounterPartyUpdate extends AbstractUB_TXN_CounterPartyUpdate {

	/**
	 * @author Binit.Kumar
	 * @date Nov 13, 2015
	 * @project Universal Banking
	 * @Description: Code for storing Partner information which is not available in
	 *               TRANSACTION table in TRANSACTIONCOUNTERPARTYDATA table
	 */
	private static final long serialVersionUID = -5801162541095987296L;
	private static final transient Log LOGGER = LogFactory.getLog(UB_TXN_CounterPartyUpdate.class.getName());
	private static final String INSERT_SQL_QUERY_GROUP = "INSERT INTO UBTB_TRANSACTIONCOUNTERPTYDATA (UBTRANSACTIONCOUNTERPARTYIDPK, UBTRANSACTIONID, UBTRANSACTIONSUBTYPE, UBTRANSACTIONDIRECTION,UBCUSTOMERNAME,UBCONTRAACCNUM,UBCHANNELNAME,UBMERCHANTNAME,UBCARDNUMBER,VERSIONNUM, UBUNMASKEDCARDNUMBER) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String FIND_TRANSACTION_ID = "SELECT UBCONTRAACCNUM FROM UBTB_TRANSACTIONCOUNTERPTYDATA WHERE UBTRANSACTIONID = ?";
	protected IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	@SuppressWarnings("deprecation")
	public UB_TXN_CounterPartyUpdate(BankFusionEnvironment env) {
		super(env);
	}

	@SuppressWarnings("deprecation")
	public UB_TXN_CounterPartyUpdate() {
		super(BankFusionThreadLocal.getBankFusionEnvironment());
	}

	public void process(BankFusionEnvironment env) {
		if (!isTransactionIDExists()) {
			populateTable();
		}
	}

	private boolean isTransactionIDExists() {
		String txnId = getF_IN_TRANSACTIONID();
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		Connection connection = factory.getJDBCConnection();
		try {
			preparedStatement = connection.prepareStatement(FIND_TRANSACTION_ID, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			preparedStatement.setString(1, txnId);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			if (preparedStatement != null) {
				try {
					if (rs != null) {
						rs.close();
					}
					preparedStatement.close();
				} catch (SQLException sqlException) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(sqlException));
				}
			}
		}
		return false;
	}

	public void populateTable() {
		String PK = GUIDGen.getNewGUID();
		String TXN_ID = getF_IN_TRANSACTIONID();
		String TXN_SUBTYPE = getF_IN_TRANSACTIONSUBTYPE();
		String TXN_DIRECTION = getF_IN_TRANSACTIONDIRECTION();
		String CUST_NAME = getF_IN_CUSTOMERNAME();
		String CONTRA_ACC = getF_IN_CONTRAACCNUM();
		String CHANNEL_NAME = getF_IN_CHANNELNAME();
		String MERCHANT_NAME = getF_IN_MERCHANTNAME();
		String CARD_NUMBER = getF_IN_CARDNUMBER();
		String cardMask = "";
		int VERSIONNUM = 1;

		if (TXN_DIRECTION.equalsIgnoreCase("I") && !CHANNEL_NAME.isEmpty() && CHANNEL_NAME.equals("SWIFT")
				|| CHANNEL_NAME != null && CHANNEL_NAME.equals("SWIFT") && TXN_DIRECTION.equalsIgnoreCase("I")) {
			if (TXN_SUBTYPE.equals("") || TXN_SUBTYPE.equals(" ") || TXN_SUBTYPE.equals("A")) {
				if (CUST_NAME.length() > 0 && CUST_NAME != null) {
					CUST_NAME = CUST_NAME.substring(1, CUST_NAME.length());
				}
				int beginIndex = CUST_NAME.indexOf('$', 2);
				if (beginIndex != -1) {
					int endIndex = CUST_NAME.indexOf('$', beginIndex + 1);
					if (endIndex == -1) {
						endIndex = CUST_NAME.length();
					}
					CUST_NAME = CUST_NAME.substring(beginIndex + 1, endIndex);
				} else
					CUST_NAME = "";
			}

			if (TXN_SUBTYPE.equals("F")) {

				if (CUST_NAME.length() > 0 && CUST_NAME != null) {
					CUST_NAME = CUST_NAME.substring(1, CUST_NAME.length());
				}
				int beginIndex = CUST_NAME.indexOf('$', 2);
				if (beginIndex != -1) {
					int endIndex = CUST_NAME.indexOf('$', beginIndex + 1);
					if (endIndex == -1) {
						endIndex = CUST_NAME.length();
					}
					CUST_NAME = CUST_NAME.substring(beginIndex + 1, endIndex);
					CUST_NAME = (String) CUST_NAME.subSequence(2, CUST_NAME.length());
				} else
					CUST_NAME = "";
			}
		}
		if (CHANNEL_NAME.equalsIgnoreCase("SWIFT") && TXN_DIRECTION.equalsIgnoreCase("O") && CONTRA_ACC.length() > 0)

		{

			CONTRA_ACC = CONTRA_ACC.substring(1);

		}

		TXN_SUBTYPE = "";
		if (!CARD_NUMBER.isEmpty() && CARD_NUMBER != null) {
			String number = CARD_NUMBER;

			String lastFour = "";
			int length;
			int cardLength = 0;
			StringBuffer mask = new StringBuffer("");
			length = number.length();
			if (length > 4) {
				lastFour = number.substring(length - 4);

				cardLength = length - 4;
			}

			for (int i = 0; i < cardLength; i++) {
				mask = mask.append("x");
			}
			cardMask = mask + lastFour;
		}

		Connection connection = null;
		PreparedStatement pstmt = null;

		try {
			String cardNumber = CARD_NUMBER;
			if (DBEncryptionUtilHelper.isDBEncryptionDecryptionEnable()) {
				if (DBEncryptionUtilHelper.getEncryptedDBColumnListForTable("UBTB_TRANSACTIONCOUNTERPTYDATA") != null
						&& DBEncryptionUtilHelper.getEncryptedDBColumnListForTable("UBTB_TRANSACTIONCOUNTERPTYDATA")
								.contains("UBUNMASKEDCARDNUMBER")) {
					cardNumber = DBEncryptionUtilHelper.encrypt(CARD_NUMBER);
				}
			}
			connection = factory.getJDBCConnection();
			pstmt = connection.prepareStatement(INSERT_SQL_QUERY_GROUP);
			pstmt.setString(1, PK);
			pstmt.setString(2, TXN_ID);
			pstmt.setString(3, TXN_SUBTYPE);
			pstmt.setString(4, TXN_DIRECTION);
			pstmt.setString(5, CUST_NAME);
			pstmt.setString(6, CONTRA_ACC);
			pstmt.setString(7, CHANNEL_NAME);
			pstmt.setString(8, MERCHANT_NAME);
			pstmt.setString(9, cardMask);
			pstmt.setInt(10, VERSIONNUM);
			pstmt.setString(11, cardNumber);
			pstmt.executeUpdate();

		} catch (SQLException sqlException) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(sqlException));
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException sqlException) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(sqlException));
				}
			}
		}
	}
}