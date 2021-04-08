
/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **
 *Rubalin Das*/
package com.trapedza.bankfusion.fatoms;
import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_MT200;
import com.misys.ub.swift.UB_MT201;
import com.misys.ub.swift.UB_MT202;
import com.misys.ub.swift.UB_MT203;
import com.misys.ub.swift.UB_MT205;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.bo.refimpl.IBOEventCode;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_IncomingMessageUpdate;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_IncomingMessageUpdate;
import com.trapedza.bankfusion.utils.BankFusionMessages;
/**
 * @author Gaurav Aggarwal
 *
 */


	public class UB_SWT_IncomingMessageUpdate extends AbstractUB_SWT_IncomingMessageUpdate implements IUB_SWT_IncomingMessageUpdate
   {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

		private transient final static Log logger = LogFactory.getLog(UB_SWT_IncomingMessageUpdate.class.getName());

	  Object Message=new Object();
	  	private ArrayList inputValues = null;
	 
	  public UB_SWT_IncomingMessageUpdate(BankFusionEnvironment env)
	  	{
		super(env);
		}

	  public void process(BankFusionEnvironment env)
	  	{
		  	  try {
			  	
			  	inputValues=getF_IN_Message();
				ArrayList object=new ArrayList();
			  	String ErrorMessage = "R";
			  	int errorNum = Integer.parseInt(getF_IN_ErrorNumber());
			  	int errorNumber = errorNum + 70000000;
			  	String whereCluaseForAddressLink = " WHERE " + IBOEventCode.EVENTCODENUMBER + " = ?";
			  	ArrayList params = new ArrayList();
				params.add(errorNumber);
				ArrayList ErrorList = (ArrayList) env.getFactory().findByQuery(IBOEventCode.BONAME,
						whereCluaseForAddressLink, params, null);
			  	IBOEventCode eventcode = (IBOEventCode) ErrorList.get(0);
				String error = eventcode.getF_DESCRIPTION().toString();
			  	String errorMessage = ErrorMessage.concat(error);
			  	Object messageObject = inputValues.get(0);
			  	if (messageObject instanceof UB_MT103){
			  		UB_MT103 messageobj=(UB_MT103)messageObject;
			  		messageobj.setInternalRef(errorMessage);
			  		logger.info(ErrorMessage);
			  		object.add(messageobj);
			  		setF_OUT_Message(object);
			  	}
			  	if (messageObject instanceof UB_MT202){
			  		UB_MT202 messageobj=(UB_MT202)messageObject;
			  		messageobj.setInternalRef(errorMessage);
			  		logger.info(ErrorMessage);
			  		object.add(messageobj);
			  		setF_OUT_Message(object);
			  	}
			  	if (messageObject instanceof UB_MT200){
			  		UB_MT200 messageobj=(UB_MT200)messageObject;
			  		messageobj.setInternalRef(errorMessage);
			  		logger.info(ErrorMessage);
			  		object.add(messageobj);
			  		setF_OUT_Message(object);
			  	}
			  	if (messageObject instanceof UB_MT201){
			  		UB_MT201 messageobj=(UB_MT201)messageObject;
			  		messageobj.setInternalRef(errorMessage);
			  		logger.info(ErrorMessage);
			  		object.add(messageobj);
			  		setF_OUT_Message(object);
			  	}
			  	if (messageObject instanceof UB_MT203){
			  		UB_MT203 messageobj=(UB_MT203)messageObject;
			  		messageobj.setInternalRef(errorMessage);
			  		logger.info(ErrorMessage);
			  		object.add(messageobj);
			  		setF_OUT_Message(object);
			  	}
			  	if (messageObject instanceof UB_MT205){
			  		UB_MT205 messageobj=(UB_MT205)messageObject;
			  		messageobj.setInternalRef(errorMessage);
			  		logger.info(ErrorMessage);
			  		object.add(messageobj);
			  		setF_OUT_Message(object);
			  	}
				
	}
	catch (BankFusionException ex) 
	{
		logger.info(ex);
	}
		
}


}
