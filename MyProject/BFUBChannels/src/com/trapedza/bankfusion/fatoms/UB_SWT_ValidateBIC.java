package com.trapedza.bankfusion.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ValidateBIC;

public class UB_SWT_ValidateBIC extends AbstractUB_SWT_ValidateBIC{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	 private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	 private transient final static Log logger = LogFactory.getLog(UB_SWT_ValidateBIC.class.getName());
	 private final String INSERT_BIC_CODE ="INSERT INTO UBTB_SWTMSGBICMAP(UBSWTMSGBICMAPIDPK, UBCUSTOMERCODE, UBMESSAGETYPE, UBBICCODE, UBISTWENTYFIVEPREQ, VERSIONNUM) VALUES(?, ?, ?, ?, ?, ?)";
	 private final String UPDATE_BIC_CODE ="UPDATE UBTB_SWTMSGBICMAP SET UBBICCODE = ?  WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";
	 private final String RECORD_EXISTS ="SELECT UBCUSTOMERCODE FROM UBTB_SWTMSGBICMAP WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";
	 private final String DELETE_RECORD ="DELETE FROM UBTB_SWTMSGBICMAP WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	public UB_SWT_ValidateBIC(BankFusionEnvironment env) {
		super(env);
	}
	@Override
	public void process(BankFusionEnvironment env) {
	if(SWT_Stmnt940942_25PRequired()){
			Map hmpParamsAA = new HashMap();
			hmpParamsAA.put("IdentifierCode",getF_IN_BICfor25P());
			Map resultAA = MFExecuter.executeMF("UB_SWT_IdentifierCodeRead_SRV", env, hmpParamsAA);	
			Integer NoOfRows = (Integer) resultAA.get("NoOfRows");
		if(NoOfRows>0){
			insertOrUpdateBICDetails(getF_IN_AccountNumber(), getF_IN_StatementType() ,getF_IN_BICfor25P(), isF_IN_is25PRequired().toString());
		}else{
		//raise event if BIC not exists
			String[] Params = { "" };
			EventsHelper.handleEvent(40102316, Params, new HashMap(), env);
		}
		}
	if(creditConfirm()){
			Map hmpParamsCC = new HashMap();
			hmpParamsCC.put("IdentifierCode",getF_IN_BICfor25P910());
			Map resultCC = MFExecuter.executeMF("UB_SWT_IdentifierCodeRead_SRV", env, hmpParamsCC);	
			Integer NoOfRows1 = (Integer) resultCC.get("NoOfRows");
		if(NoOfRows1>0){
			insertOrUpdateBICDetails(getF_IN_customerCode(), "910" ,getF_IN_BICfor25P910(), isF_IN_is25PMT910Required().toString());
		}else{
		//raise event if BIC not exists
			String[] Params = { "" };
			EventsHelper.handleEvent(40102316, Params, new HashMap(), env);
		}
		}
	if(debitConfirm()){
			Map hmpParamsDC = new HashMap();
			hmpParamsDC.put("IdentifierCode",getF_IN_BICfor25P900());
			Map resultDC = MFExecuter.executeMF("UB_SWT_IdentifierCodeRead_SRV", env, hmpParamsDC);
			Integer NoOfRows2 = (Integer) resultDC.get("NoOfRows");
		if(NoOfRows2>0){
			insertOrUpdateBICDetails(getF_IN_customerCode(), "900" ,getF_IN_BICfor25P900(), isF_IN_is25PMT900Required().toString());
		}else{
			//raise event if  BIC not exists	
			String[] Params = { "" };
			EventsHelper.handleEvent(40102316, Params, new HashMap(), env);
			
			}
		}
	if(!isF_IN_is25PRequired() && getF_IN_StatementType().equals("942"))
			   deleteRecord( getF_IN_customerCode(),"942");
	if(!isF_IN_is25PRequired() && getF_IN_StatementType().equals("940"))
			   deleteRecord( getF_IN_customerCode(), "940");
	if((!isF_IN_is25PMT900Required() || getF_IN_debitConfirm().equals("N")) && getF_IN_StatementType()!="940" && getF_IN_StatementType()!="942")
		    	deleteRecord( getF_IN_customerCode(), "900" );
	if((!isF_IN_is25PMT910Required() || getF_IN_creditConfirm().equals("N")) && getF_IN_StatementType()!="940" && getF_IN_StatementType()!="942")
		    	deleteRecord( getF_IN_customerCode(), "910");
	}
	
	public boolean creditConfirm(){
		if(getF_IN_creditConfirm().equals("Y") && isF_IN_is25PMT910Required())
			return true;
		return false;
	}
	public boolean debitConfirm(){
		if(getF_IN_debitConfirm().equals("Y") && isF_IN_is25PMT900Required())
			return true;
	return false;
	}
	public boolean SWT_Stmnt940942_25PRequired(){ 
		if(isF_IN_is25PRequired() && getF_IN_BICfor25P()!="")
			return true;
		
		return false;
	}
	public void insertOrUpdateBICDetails(String customerNumber, String messageType, String BICCode, String is25PReq ){
		Connection connection = factory.getJDBCConnection();
		ResultSet rs = null;
	    PreparedStatement preparedStatement =null;
	    PreparedStatement preparedStatementUPDATE = null;
	    PreparedStatement preparedStatementINSERT =null;
	try {   
	        int count = 0;
	        preparedStatement = connection.prepareStatement(RECORD_EXISTS);
	        preparedStatement.setString(1,customerNumber);
		    preparedStatement.setString(2,messageType);
		    rs = preparedStatement.executeQuery();
		if(rs.next())
		    count++;
		    rs.close();
		if(count!=0){
		    preparedStatementUPDATE = connection.prepareStatement(UPDATE_BIC_CODE);
	        preparedStatementUPDATE.setString(1, BICCode);
	        preparedStatementUPDATE.setString(2, customerNumber);
	        preparedStatementUPDATE.setString(3, messageType);
	        preparedStatementUPDATE.executeUpdate();
	        preparedStatementUPDATE.close();
		}
		else{
			preparedStatementINSERT = connection.prepareStatement(INSERT_BIC_CODE);	
	        preparedStatementINSERT.setString(1, GUIDGen.getNewGUID());
	        preparedStatementINSERT.setString(2, customerNumber);
	        preparedStatementINSERT.setString(3, messageType);
	        preparedStatementINSERT.setString(4, BICCode);
	        preparedStatementINSERT.setString(5, is25PReq);
	        preparedStatementINSERT.setInt(6, 1); 
	        preparedStatementINSERT.executeUpdate();
	        preparedStatementINSERT.close();
	     	}
	    }
	    catch (SQLException e) {
	            logger.error(ExceptionUtil.getExceptionAsString(e));
	    }finally{

	    	if(preparedStatement!=null)
	    		try {
	    			preparedStatement.close();
	    		} catch (SQLException e) {
	    			logger.error(ExceptionUtil.getExceptionAsString(e));
	    		}
	    	if(rs!=null)
	    		try {
	    			rs.close();
	    		} catch (SQLException e1) {
	    			logger.error(ExceptionUtil.getExceptionAsString(e1));
	    		}
	    	if(preparedStatementUPDATE!=null)
	    		try {
	    			preparedStatementUPDATE.close();
	    		} catch (SQLException e1) {
	    			logger.error(ExceptionUtil.getExceptionAsString(e1));
	    		}
	    	if(preparedStatementINSERT!=null)
	    		try {
	    			preparedStatementINSERT.close();
	    		} catch (SQLException e) {
	    			logger.error(ExceptionUtil.getExceptionAsString(e));
	    		}
	    }
	}
	public void deleteRecord(String customerNumber, String messageType){
	 	 Connection connection1 = factory.getJDBCConnection();
	     PreparedStatement preparedStatementDELETE =null;
	try {
     	  preparedStatementDELETE = connection1.prepareStatement(DELETE_RECORD);
     	  preparedStatementDELETE.setString(1,customerNumber);
     	  preparedStatementDELETE.setString(2,messageType);
	      preparedStatementDELETE.executeUpdate();
	      preparedStatementDELETE.close();
		 }
     catch (SQLException e) {
         logger.error(ExceptionUtil.getExceptionAsString(e));
     }finally{
    	 if(preparedStatementDELETE!=null){
    		 try{
    			 preparedStatementDELETE.close();
    		 }catch(Exception e){
    			 logger.error(ExceptionUtil.getExceptionAsString(e));
    		 }
    	 }
    	 
     }
}
}
