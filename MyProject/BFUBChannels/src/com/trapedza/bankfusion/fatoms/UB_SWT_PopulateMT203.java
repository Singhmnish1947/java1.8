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
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PopulateMT203;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_PopulateMT203;
import com.misys.ub.swift.UB_203Message_Details;
import com.misys.ub.swift.UB_MT201;
import com.misys.ub.swift.UB_MT203;
import com.misys.ub.swift.UB_MT202;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
/**
 * @author Gaurav Aggarwal
 * 
 */
public class UB_SWT_PopulateMT203 extends AbstractUB_SWT_PopulateMT203
		implements IUB_SWT_PopulateMT203 {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	private ArrayList inputValues = null;

	UB_MT202 messageObject_202=new UB_MT202();

	public UB_SWT_PopulateMT203(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }
	
	public void process(BankFusionEnvironment env) {
		
	inputValues=getF_IN_Object();
	ArrayList object=new ArrayList();
	UB_MT203 messageObject_203=(UB_MT203)inputValues.get(0);
	UB_203Message_Details messageObject_203MD = new UB_203Message_Details();
	ArrayList list = messageObject_203.getMessageDetails();
	Iterator it = list.iterator();
	
	while (it.hasNext()){
		 messageObject_203MD = (UB_203Message_Details) it.next();
		 messageObject_202.setSender(messageObject_203.getSender());
		 messageObject_202.setAccountWithInstitution(messageObject_203MD.getAccountWithInstitution());
		 messageObject_202.setAccountWithInstitutionOption(messageObject_203MD.getAccountWithInstitutionOption());
		 messageObject_202.setBeneficiary(messageObject_203MD.getBeneficiaryInstitution());
		 messageObject_202.setBeneficiaryOption(messageObject_203MD.getBeneficiaryInstitutionOption());
		 messageObject_202.setIntermediary(messageObject_203MD.getIntermediary());
		 messageObject_202.setIntermediaryOption(messageObject_203MD.getIntermediaryOption());
		 messageObject_202.setOrderingInstitution(messageObject_203.getOrderingInstitute());
		 messageObject_202.setOrderingInstitutionOption(messageObject_203.getOrderingInstituteOption());
		 messageObject_202.setReceiversCorrespondent(messageObject_203.getReceiversCorrespondent());
		 messageObject_202.setReceiversCorrespondentOption(messageObject_203.getReceiversCorrespondentOption());
		 messageObject_202.setRelatedReference(messageObject_203MD.getRelatedReference());
		 messageObject_202.setSendersCorrespondent(messageObject_203.getSendersCorrespondent());
		 messageObject_202.setSendersCorrespondentOption(messageObject_203.getSendersCorrespondentOption());
		 messageObject_202.setSendertoReceiverInformation(messageObject_203MD.getSenderToReceiverInformation());
		 messageObject_202.setTdAmount(messageObject_203MD.getAmount());
		 messageObject_202.setTdCurrencyCode(messageObject_203MD.getCurrency());
		 messageObject_202.setTdValueDate(messageObject_203.getValueDate());
		 messageObject_202.setTransactionReferenceNumber(messageObject_203MD.getTRN());
		 
		 object.clear();
		 object.add(messageObject_202);
		 
		 HashMap messageList202=new HashMap();
		 
		 messageList202.put("AccountWith", messageObject_203MD.getAccountWithInstitution());
		 
		 messageList202.put("AccountWithOption", messageObject_203MD.getAccountWithInstitutionOption());
		 
		 messageList202.put("BeneficiaryInstitution", messageObject_203MD.getBeneficiaryInstitution());
		
		 messageList202.put("BeneficiaryInstitutionOption", messageObject_203MD.getBeneficiaryInstitutionOption());
		 
		 messageList202.put("DealReference", messageObject_203MD.getTRN());
		 String InterBankSettledAmount = messageObject_203MD.getAmount();
		InterBankSettledAmount = InterBankSettledAmount.replaceAll(",", ".");
			
		 messageList202.put("InterBankSettledAmount", new BigDecimal(InterBankSettledAmount));
		 
		 messageList202.put("InterBankSettledCurrency", messageObject_203MD.getCurrency());
		 
		 messageList202.put("Intermediary", messageObject_203MD.getIntermediary());
		 messageList202.put("IntermediaryOption", messageObject_203MD.getIntermediaryOption());
		 messageList202.put("ReceiversCorrespondent", messageObject_203.getReceiversCorrespondent());
		 messageList202.put("ReceiversCorrespondentOption", messageObject_203.getReceiversCorrespondentOption());
		 messageList202.put("SendersCorrespondent", messageObject_203.getSendersCorrespondent());
		 messageList202.put("SendersCorrespondentOption", messageObject_203.getSendersCorrespondentOption());
		 messageList202.put("Sender", messageObject_203.getSender());
		 messageList202.put("MessageType", "MT202");
		 Date valueDate=StringToDate.run(messageObject_203.getValueDate());
		 messageList202.put("ValueDate",valueDate);
		 messageList202.put("relatedReference", messageObject_203MD.getRelatedReference());
		 messageList202.put("SendersCorrespondentOption", messageObject_203MD.getTRN());
		 	
	
		 messageList202.put("Object", object);
		 
		 MFExecuter.executeMF("UB_SWT_Incoming_MT202_Process_SRV", env,messageList202 );
		 
	}
 
	 }
		
	}
	

