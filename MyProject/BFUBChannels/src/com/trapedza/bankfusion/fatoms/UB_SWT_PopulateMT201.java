/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigDecimal;


import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.StringToDate;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PopulateMT201;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_PopulateMT201;
import com.misys.ub.swift.UB_201Message_Details;
import com.misys.ub.swift.UB_MT201;
import com.misys.ub.swift.UB_MT200;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
/**
 * @author Gaurav Aggarwal
 * 
 */
public class UB_SWT_PopulateMT201 extends AbstractUB_SWT_PopulateMT201
		implements IUB_SWT_PopulateMT201 {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	private ArrayList inputValues = null;

	UB_MT200 messageObject_200=new UB_MT200();

	public UB_SWT_PopulateMT201(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }
	
	public void process(BankFusionEnvironment env) {
		
	inputValues=getF_IN_Object();
	ArrayList object=new ArrayList();
	UB_MT201 messageObject_201=(UB_MT201)inputValues.get(0);
	UB_201Message_Details messageObject_201MD = new UB_201Message_Details();
	ArrayList list = messageObject_201.getMessageDetails();
	Iterator it = list.iterator();
	
	while (it.hasNext()){
		 messageObject_201MD = (UB_201Message_Details) it.next();
		 messageObject_200.setAccountWithInstitution(messageObject_201MD.getAccountWithInstitution());
		 messageObject_200.setAccountWithInstOption(messageObject_201MD.getAccountWithInstitutionOption());
		 messageObject_200.setIntermediary(messageObject_201MD.getIntermediary());
		 messageObject_200.setIntermediaryOption(messageObject_201MD.getIntermediaryOption());
		 messageObject_200.setSendersCorrespondent(messageObject_201.getSendersCorrespondent());
		// messageObject_200.setSendersCorresOption(messageObject_201.getSendersCorrespondentOption());
		 messageObject_200.setSenderToReceiverInformation(messageObject_201MD.getSenderToReceiverInformation());
		 messageObject_200.setTdamount(messageObject_201MD.getAmount());
		 messageObject_200.setTdcurrencyCode(messageObject_201MD.getCurrency());
		 messageObject_200.setTdvalueDate(messageObject_201.getValueDate());
		 messageObject_200.setTransactionReferenceNumber(messageObject_201MD.getTRN());
		 messageObject_200.setDisposalRef(messageObject_201.getDisposalRef());
		 
		 object.clear();
		 object.add(messageObject_200);
		 
		 HashMap messageList200=new HashMap();
		 
		 messageList200.put("AccountWith", messageObject_201MD.getAccountWithInstitution());
		 
		 messageList200.put("AccountWithOption", messageObject_201MD.getAccountWithInstitutionOption());
		 
		 messageList200.put("DealReference", messageObject_201MD.getTRN());
		
		 messageList200.put("InterBankSettledAmount", messageObject_201MD.getAmount());
		 
		 messageList200.put("InterBankSettledCurrency", messageObject_201MD.getCurrency());
		 
		 messageList200.put("Intermediary", messageObject_201MD.getIntermediary());
		 messageList200.put("IntermediaryOption", messageObject_201MD.getIntermediaryOption());
		 messageList200.put("SendersCorrespondent", messageObject_201.getSendersCorrespondent());
		// messageList200.put("SendersCorrespondentOption", messageObject_201.getSendersCorrespondentOption());
		 String InterBankSettledAmount = messageObject_201MD.getAmount();
		InterBankSettledAmount = InterBankSettledAmount.replaceAll(",", ".");
			
		messageList200.put("InterBankSettledAmount", new BigDecimal(InterBankSettledAmount));			 
		messageList200.put("InterBankSettledCurrency", messageObject_201MD.getCurrency());
		Date valueDate=StringToDate.run(messageObject_201.getValueDate());
		messageList200.put("ValueDate",valueDate);
		messageList200.put("MessageType", "MT200");	
		messageList200.put("relatedReference",messageObject_201.getDisposalRef());
		messageList200.put("transactionReferenceNumber", messageObject_201MD.getTRN());	
	 
		 messageList200.put("Object", object);
		 
		 MFExecuter.executeMF("UB_SWT_Incoming_MT200_Process_SRV", env,messageList200 );
		 
	}
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	
		 
		 
	 
	 }
		
	}
	

