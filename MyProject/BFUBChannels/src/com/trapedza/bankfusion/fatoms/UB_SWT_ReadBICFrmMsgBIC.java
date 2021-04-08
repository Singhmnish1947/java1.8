package com.trapedza.bankfusion.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ReadBICFrmMsgBIC;

public class UB_SWT_ReadBICFrmMsgBIC extends AbstractUB_SWT_ReadBICFrmMsgBIC {
	
	
	
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	private transient final static Log logger = LogFactory.getLog(UB_SWT_ReadBICFrmMsgBIC.class.getName());
	private final String RECORD_EXISTS ="SELECT UBBICCODE, UBISTWENTYFIVEPREQ FROM UBTB_SWTMSGBICMAP WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * @param env
	 */
	public UB_SWT_ReadBICFrmMsgBIC(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateBICCode#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {
		
		String customerCode = getF_IN_CustomerCode();
				String messageType  = getF_IN_StatmentType();
		String BICCODE_25P = null;
		String BICCODE_25PMT900 = null;
		String BICCODE_25PMT910 = null;
		String is25PReq = null;
		String is25PReqMT900 = null;
		String is25PReqMT910 = null; 
		Connection connection = factory.getJDBCConnection();
		ResultSet rs = null;
		ResultSet rs1 = null;
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement1 = null;
	    
		if(messageType.equals("950"))
			setF_OUT_isStatemetType950(false);
		else
			setF_OUT_isStatemetType950(true);
		
		
	    if(messageType.equals("940")){
	    	
	    	 try { 
	    		 preparedStatement = connection.prepareStatement(RECORD_EXISTS);
	    		 preparedStatement.setString(1,customerCode);
	    		 preparedStatement.setString(2,"940");
	    		 rs = preparedStatement.executeQuery();
              while(rs.next()){
                 BICCODE_25P   = rs.getString("UBBICCODE");
                  is25PReq     = rs.getString("UBISTWENTYFIVEPREQ"); 
                }
              rs.close();
             preparedStatement.close();
            } catch(SQLException e){
                logger.error(ExceptionUtil.getExceptionAsString(e));
            } finally {
            	if(preparedStatement != null)
            		try {
            			preparedStatement.close();
            		} catch (SQLException e) {
            			logger.error(ExceptionUtil.getExceptionAsString(e));
            		}
            	if(rs != null)
            		try {
            			rs.close();
            		} catch (SQLException e) {
            			logger.error(ExceptionUtil.getExceptionAsString(e));
            		}
            }
	    if(BICCODE_25P != null && is25PReq != null){
	    		setF_OUT_BICfor25P(BICCODE_25P);
	    		setF_OUT_is25PReq((is25PReq.equals("true")) ? true : false);
           }
		}
	    else if(messageType.equals("942")){
	    	
	    	 try { 
	    		 preparedStatement = connection.prepareStatement(RECORD_EXISTS);
	    		 preparedStatement.setString(1,customerCode);
	    		 preparedStatement.setString(2,"942");
	    		 rs = preparedStatement.executeQuery();
              while(rs.next()){
                 BICCODE_25P   = rs.getString("UBBICCODE");
                  is25PReq     = rs.getString("UBISTWENTYFIVEPREQ"); 
                }
              preparedStatement.close();
            } catch(SQLException e){
                logger.error(ExceptionUtil.getExceptionAsString(e));
            } finally {
            	try {
            		if(preparedStatement != null)
            			preparedStatement.close();
            		if(rs != null)
            			rs.close();
            	} catch(SQLException sq) {
            		logger.error(ExceptionUtil.getExceptionAsString(sq));
            	}
            }
	    if(BICCODE_25P != null && is25PReq != null){
	    		setF_OUT_BICfor25P(BICCODE_25P);
	    		setF_OUT_is25PReq((is25PReq.equals("true")) ? true : false);
           }
		} 
	    
	    else if(!messageType.equals("950"))
	    {
	       	
	    	try {   
	    		preparedStatement = connection.prepareStatement(RECORD_EXISTS);
			    preparedStatement.setString(1,customerCode);
				preparedStatement.setString(2,"900");
				rs = preparedStatement.executeQuery();
				while(rs.next()){
					BICCODE_25PMT900   = rs.getString("UBBICCODE");
			       	is25PReqMT900  	 = rs.getString("UBISTWENTYFIVEPREQ"); 
				}
				rs.close();
				preparedStatement.close();
			
				if(BICCODE_25PMT900 != null && is25PReqMT900 != null){
		    		setF_OUT_BICfor25PMT900(BICCODE_25PMT900);
		    		setF_OUT_is25PReqfor900((is25PReqMT900.equals("true")) ? true : false);
				}
		        preparedStatement1 = connection.prepareStatement(RECORD_EXISTS);
				preparedStatement1.setString(1,customerCode);
				preparedStatement1.setString(2,"910");
			    rs1 = preparedStatement1.executeQuery();
			       
			    while(rs1.next()){
			  	  BICCODE_25PMT910   = rs1.getString("UBBICCODE");
			   	  is25PReqMT910      = rs1.getString("UBISTWENTYFIVEPREQ"); 
				}
			    preparedStatement1.close();
			    rs1.close();
		        if(BICCODE_25PMT910 != null && is25PReqMT910 != null){
		        	setF_OUT_BICfor25PMT910(BICCODE_25PMT910);
		        	setF_OUT_is25PReqfor910((is25PReqMT910.equals("true")) ? true : false);
		        }

		        preparedStatement.close();
			  } catch(SQLException e) {
		         logger.error(ExceptionUtil.getExceptionAsString(e));
			  } finally {
				  if(preparedStatement != null)
					  try {
						  preparedStatement.close();
					  } catch (SQLException e) {
						  logger.error(ExceptionUtil.getExceptionAsString(e));
					  }
				  if(preparedStatement1 != null)
					  try {
						  preparedStatement1.close();
					  } catch (SQLException e) {
						  logger.error(ExceptionUtil.getExceptionAsString(e));
					  }
				  if(rs != null)
					  try {
						  rs.close();
					  } catch (SQLException e) {
						  logger.error(ExceptionUtil.getExceptionAsString(e));
					  }
				  if(rs1 != null)
					  try {
						  rs1.close();
					  } catch (SQLException e) {
						  logger.error(ExceptionUtil.getExceptionAsString(e));
					  }
			  }
	    }
	}
}