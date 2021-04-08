package com.misys.ub.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_FindSwiftBicForCustomer;

public class FindSwiftBicForCustomer extends
		AbstractUB_CMN_FindSwiftBicForCustomer {
	private static final long serialVersionUID = 8669591941954920544L;
	private IPersistenceObjectsFactory factory;
	private String findSwiftBicByCustomerId = "SELECT BICCODE FROM SWTCUSTOMERDETAIL WHERE CUSTOMERCODE = ?";

	private static final transient Log logger = LogFactory
			.getLog(FindSwiftBicForCustomer.class.getName());

	public FindSwiftBicForCustomer() {
		super();
	}

	@SuppressWarnings("deprecation")
	public FindSwiftBicForCustomer(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		factory = BankFusionThreadLocal.getPersistanceFactory();
		String CustomerId = getF_IN_readPersonPartyRs()
				.getReadPersonalPartyOutput().getPartyBasicDtls().getPartyId();
		@SuppressWarnings("deprecation")
		Connection connection = factory.getJDBCConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(findSwiftBicByCustomerId);
			ps.setString(1, CustomerId);
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
				String bicCode = rs.getString(1);
				getF_IN_readPersonPartyRs().getReadPersonalPartyOutput()
						.getPaymentData().setBicCode(bicCode);
				}
			}
		} catch (SQLException e) {
			logger.error("Error getting swift bic for the customer: "
					+ CustomerId);
			logger.error("Error Details: ", e);
		}
		finally {
			if (ps != null)
				try {	ps.close();}
			catch (SQLException e) {
				logger.error("Error Details: ", e);
			}
			if (rs != null)
				try{
					rs.close();}
			catch (SQLException e) {
				logger.error("Error Details: ", e);
			}
		} 
		}
}