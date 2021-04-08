package com.trapedza.bankfusion.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_AmendCustomerAccount;

public class UB_INF_AmendCustomerAccount extends AbstractUB_INF_AmendCustomerAccount  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param args
	 */
	 private static final transient   Log logger = LogFactory.getLog(UB_INF_AmendCustomerAccount.class.getName());
	 private static final String INSERT ="INSERT INTO INFTB_ACCOUNTINFMAP(INACCOUNTINFMAPIDPK, INACCOUNTID, ININTERFACEID, INISACTIVE, VERSIONNUM) VALUES(?, ?, ?, ?, ?)";
	 private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	 public static final String svnRevision = "$Revision: 1.0 $";
	 
	 static {
			com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	 }
	 
	 public UB_INF_AmendCustomerAccount(BankFusionEnvironment env) {
			super(env);
	 }

	 @Override
	 public void process(BankFusionEnvironment env) {
	 
		 String accNumber = getF_IN_AccNum();
		 String custNumber = getF_IN_CustNum();
		 String interfaceName = getF_IN_interfaceName();
		 PreparedStatement preparedStatementINSERT = null;
		 
		
		if(accNumber != null && accNumber !="") {
			Map hmpParamsAA = new HashMap();
			hmpParamsAA.put("AccountID",accNumber);
			Map result = MFExecuter.executeMF("CB_CMN_GetAccountCustomerDetails_SRV", env, hmpParamsAA);	
			String custnuMBR = (String) result.get("CUSTOMERCODE");
			if(custNumber != null && custNumber != custnuMBR) {
				String[] params = { "" };
				EventsHelper.handleEvent(20020011, params, new HashMap(), env);
			}
			else {	
			Connection connection = factory.getJDBCConnection();
			   
			try {
					preparedStatementINSERT = connection.prepareStatement(INSERT);	
			        preparedStatementINSERT.setString(1, GUIDGen.getNewGUID());
			        preparedStatementINSERT.setString(2, accNumber);
			        preparedStatementINSERT.setString(3, interfaceName);
			        preparedStatementINSERT.setString(4, "Y");
			        preparedStatementINSERT.setInt(5, 0);
			        preparedStatementINSERT.executeUpdate();
			        preparedStatementINSERT.close();
			        setF_OUT_statusMsg("Account "+ accNumber+" is enabled for "+interfaceName );
			} catch(SQLException e){
				 logger.error(ExceptionUtil.getExceptionAsString(e));			
			} finally{
				try {
					if(preparedStatementINSERT != null) {
						preparedStatementINSERT.close();
					}
				} catch(Exception e){
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
			
			}
			}
		 
		}
	 }
}
