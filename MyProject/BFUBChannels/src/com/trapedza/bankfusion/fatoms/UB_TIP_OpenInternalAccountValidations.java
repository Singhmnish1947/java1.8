package com.trapedza.bankfusion.fatoms;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.CODESET_INCOMPATIBLE;

import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

import com.misys.ub.interfaces.opics.steps.IncomingAccountEntriesFatom;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.microflow.ActivityStep;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_OpenInternalAccountValidations;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.BankFusionThreadLocalWrapper;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuterWrapper;

public class UB_TIP_OpenInternalAccountValidations extends AbstractUB_TIP_OpenInternalAccountValidations{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String svnRevision = "$Revision: 1.0 $";
    private static final transient Log logger = LogFactory.getLog(UB_TIP_OpenInternalAccountValidations.class.getName());
	
	private static String microflowNameOne = null;


	static {

		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}	

    public UB_TIP_OpenInternalAccountValidations(BankFusionEnvironment env) {
        super(env);
    }

	
	
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
	
		
		try{
		
		bf.com.misys.cbs.msgs.v1r0.CreateAccountRq  request = getF_IN_createAccountRq();
		
		HashMap params = new HashMap();
		microflowNameOne = getF_IN_microflowID();
		params.put("createAccountRq", request);
		MFExecuterWrapper mfExec = new MFExecuterWrapper();
		HashMap resultOne = MFExecuter.executeMF(microflowNameOne, env, params);
		
		String branchCode = (String)resultOne.get("branchCode");
		String accountDescription = (String)resultOne.get("AccountDescription");
		String accountName = (String)resultOne.get("accountName");
		String branchName = (String)resultOne.get("branchName");
		String customerName = (String)resultOne.get("customerName");
		String productContextCode = (String)resultOne.get("productContextCode");
		String productDescription = (String)resultOne.get("productDescription");
		String productID = (String)resultOne.get("productID");
		String contextType = (String)resultOne.get("contextType");
		String contextValue = (String)resultOne.get("contextValue");
		String pseudonym = (String)resultOne.get("pseudonym");
		
		setF_OUT_accountDescription(accountDescription);
		setF_OUT_accountName(accountName);
		setF_OUT_branchCode(branchCode);
		setF_OUT_branchName(branchName);
		setF_OUT_customerName(customerName);
		setF_OUT_productContextCode(productContextCode);
		setF_OUT_productDescription(productDescription);
		setF_OUT_productID(productID);
		setF_OUT_contextType(contextType);
		setF_OUT_contextValue(contextValue);
		setF_OUT_pseudonym(pseudonym);
		setF_OUT_validationSuccessful(true);
	
	
	
		}catch(Exception e){
            logger.error(">>>>>>>>>>>" + Thread.currentThread().getName() + ": " + " Error while executing. "
                    + e.getLocalizedMessage());
            
   		 String eventNumber = null;
   		 
   		 if(e instanceof BankFusionException){
   	         Collection<IEvent> events = ((BankFusionException)e).getEvents();
   	         Iterator<IEvent> itr = events.iterator();
   	         while (itr.hasNext()) {
   	             IEvent e1 = itr.next();
   	             Integer number = e1.getEventNumber();
   	             eventNumber = number.toString();
   	             
   	             break;	             
   	         }
   			 
   		 }	// Exception other than Bankfusion exception
   		 else{
   			 eventNumber = "40409005";
   		     
   		 }
			bf.com.misys.cbs.msgs.v1r0.CreateAccountRs  response = new bf.com.misys.cbs.msgs.v1r0.CreateAccountRs();
			RsHeader rsHeader = new RsHeader();
			MessageStatus status = new MessageStatus();
	        SubCode code = new SubCode();
	        code.setCode(eventNumber);
	        code.setSeverity("E");
			status.addCodes(code );
			rsHeader.setStatus(status );
			response.setRsHeader(rsHeader);
			setF_OUT_createAccountRs(response);
			setF_OUT_validationSuccessful(false);
			
			
		
	}
		

}

	
}