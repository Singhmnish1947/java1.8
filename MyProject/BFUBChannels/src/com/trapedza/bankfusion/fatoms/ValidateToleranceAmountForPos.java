package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_POS_ValdateToleranceAmount;

public class ValidateToleranceAmountForPos extends AbstractUB_POS_ValdateToleranceAmount{

	public ValidateToleranceAmountForPos() {
        super(null);
    }

	public ValidateToleranceAmountForPos(BankFusionEnvironment env) {
        super(env);
    }
	private static final String MODULE_CONFIGURATION_VALUE = "select CBPARAMVALUE AS VALUE from CBTB_MODULECONFIGURATION where CBMODULENAME = 'ATM' AND CBPARAMNAME = 'AUTH_ALLOWEED_PERCENTAGE'";

	private IPersistenceObjectsFactory factory;
	private transient final static Log logger = LogFactory
			.getLog(UB_TXN_CompensateTransaction.class.getName());

	public void process(BankFusionEnvironment env)
	{
		if(getF_IN_ErrorCode() == null || (getF_IN_ErrorCode().equals(CommonConstants.EMPTY_STRING)))
		{
		BigDecimal blockedAmount = getF_IN_blockedAmount();
		BigDecimal requestedAmount = getF_IN_requestedAmount();

		PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            factory = BankFusionThreadLocal.getPersistanceFactory();
            Connection connection = factory.getJDBCConnection();
            ps = connection.prepareStatement(MODULE_CONFIGURATION_VALUE);
            rs = ps.executeQuery();
            String update = null;
            while (rs.next()) {
                update = rs.getString("VALUE");
            }

            BigDecimal tolerancePercentage = new BigDecimal(update);

            BigDecimal toleranceLimit = ((blockedAmount.multiply(tolerancePercentage)).divide(new BigDecimal("100")));

            if( (blockedAmount.add(toleranceLimit)).compareTo(requestedAmount) >= 0)
            {
            	setF_OUT_ErrorCode(null);
            }else{
            	setF_OUT_ErrorCode("40421545");
            }

        }
        catch(Exception e)
        {
        	logger.error(ExceptionUtil.getExceptionAsString(e));
        }finally{

        	if(ps!=null)
        		try {
        			ps.close();
        		} catch (SQLException e) {
        			logger.error(ExceptionUtil.getExceptionAsString(e));
        		}
        	if(rs!=null)
        		try {
        			rs.close();
        		} catch (SQLException e) {
        			logger.error(ExceptionUtil.getExceptionAsString(e));
        		}
        }
		}
		else{
			setF_OUT_ErrorCode(getF_IN_ErrorCode());
		}
	}
}
