package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.ModuleKeyRq;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractGetDebitAndTemporaryLimits;
public class GetDebitAndTemporaryLimits extends AbstractGetDebitAndTemporaryLimits 
{
	private static final long serialVersionUID = -5801162541095987296L;
	private static final transient Log LOGGER = LogFactory
			.getLog(GetDebitAndTemporaryLimits.class.getName());
	protected IPersistenceObjectsFactory factory = BankFusionThreadLocal
			.getPersistanceFactory();
	boolean paramValue;

	@SuppressWarnings("deprecation")
	public GetDebitAndTemporaryLimits(BankFusionEnvironment env) {
		super(env);
	}

	@SuppressWarnings("deprecation")
	public GetDebitAndTemporaryLimits() {
		super(BankFusionThreadLocal.getBankFusionEnvironment());
	}

	public void process(BankFusionEnvironment env) {
		paramValue = readModuleConfiguration(env);
		if(paramValue)
		calculateAmount();
		if(paramValue == false)
			setF_OUT_AvailableBalWithoutLimit(getF_IN_AvailableBalance());
	}

	public boolean readModuleConfiguration(BankFusionEnvironment env)
	{
		String value = CommonConstants.EMPTY_STRING;
		ReadModuleConfigurationRq readModuleConfigurationRq = new ReadModuleConfigurationRq();
		ReadModuleConfigurationRs readModuleConfigurationRs = null;
		ModuleKeyRq moduleKeyRq = new ModuleKeyRq();
		moduleKeyRq.setKey(getF_IN_ParamName());
		moduleKeyRq.setModuleId(getF_IN_ModuleName());
		readModuleConfigurationRq.setModuleKeyRq(moduleKeyRq);

		Map<String, Object> moduleConfigurationInputTag = new HashMap<String, Object>();

		moduleConfigurationInputTag.put("ReadModuleConfigurationRq", readModuleConfigurationRq);
		Map<String, Object> moduleConfigurationOutputTag = MFExecuter.executeMF("CB_CMN_ReadModuleConfiguration_SRV", BankFusionThreadLocal
				.getBankFusionEnvironment(), moduleConfigurationInputTag);

		if(moduleConfigurationOutputTag != null && moduleConfigurationOutputTag.containsKey("ReadModuleConfigurationRs")){
			readModuleConfigurationRs = (ReadModuleConfigurationRs) moduleConfigurationOutputTag.get("ReadModuleConfigurationRs");

			if(readModuleConfigurationRs!= null ){
				value = readModuleConfigurationRs.getModuleConfigDetails().getValue();
				String dataType = readModuleConfigurationRs.getModuleConfigDetails().getDataType();
				if (dataType.equalsIgnoreCase("Boolean")&&(value.equalsIgnoreCase(CommonConstants.TRUE) ||dataType.equalsIgnoreCase("Boolean")&&value.equalsIgnoreCase("Y"))) {
						paramValue = true;
				}
			}
		}
		return paramValue;
	}

	public void calculateAmount() {
		String ORIGINAL_AMOUNT = getF_IN_AccountId();
		final String GETAMOUNTS = "SELECT TEMPACCOUNTLIMIT, DEBITLIMIT, BOOKEDBALANCE FROM ACCOUNT WHERE ACCOUNTID = ?  ";

		BigDecimal tempAccountLimit;
		BigDecimal debitLimit;
		BigDecimal availableBalance,outAmount,finalAmount;
		Connection connection = null;
		PreparedStatement pstmt = null;
		connection=factory.getJDBCConnection();
		ResultSet rs=null;
		try {
			pstmt = connection.prepareStatement(GETAMOUNTS);
			pstmt.setString(1,ORIGINAL_AMOUNT);
			rs = pstmt.executeQuery();
			pstmt.close();

			while (rs.next()) {

				tempAccountLimit=rs.getBigDecimal(1);
				LOGGER.info("Temp Limit: "+ tempAccountLimit);

				debitLimit=rs.getBigDecimal(2);
				LOGGER.info("Debit Limit: "+ debitLimit);

				availableBalance=getF_IN_AvailableBalance();
				LOGGER.info("Avalable Balance : "+ availableBalance);

				outAmount=availableBalance.subtract(debitLimit);
				finalAmount=outAmount.subtract(tempAccountLimit);
				LOGGER.info("Avalable Balance Without Limit: "+ finalAmount);
				setF_OUT_AvailableBalWithoutLimit(finalAmount);
			}
		}
		catch (Exception sqlException ) {
			LOGGER.error(sqlException);
		}

		finally{
				if(rs!=null)
				{
					try {
						rs.close();
					} catch (SQLException e) {						
						LOGGER.error(ExceptionUtil.getExceptionAsString(e));
					}
				}
				if(pstmt!=null)
				{
					try {
						pstmt.close();
					} catch (SQLException e) {						
						LOGGER.error(ExceptionUtil.getExceptionAsString(e));
					}
				}
		}
	}
}
