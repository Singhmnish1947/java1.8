/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.swift.ChequeInfo;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_MT110192Constants;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT110;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_Generate_MT110192;

public class SWT_Generate_MT110192 extends AbstractSWT_Generate_MT110192 {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private boolean generateAnyMessage = true;
	private transient final static Log logger = LogFactory.getLog(SWT_Generate_MT110192.class.getName());
	public SWT_Generate_MT110192(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) getF_IN_SWT_DisposalObject();
		SWT_Util util=new SWT_Util();
		Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
		boolean generate110 = SWT_DataCalculation.generateCategory2Message(((SWT_DisposalObject) disposalObject)
				.getValueDate(), ((SWT_DisposalObject) disposalObject).getPostDate(), env,
				((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(), new java.sql.Date(
						bankFusionSystemDate.getTime()), "110");

		if (generate110) {

			ChequeInfo chequeDetails = new ChequeInfo();
			UB_MT110 messageObject_110 = new UB_MT110();

			ArrayList message = new ArrayList();

			if (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0) {
				setF_OUT_updatedFlag(new Integer(3));
				int cancelStatus = util.updateCancelFlag(env, 110192, disposalObject.getDisposalRef());
				setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

			}
			else if (disposalObject.getConfirmationFlag() == 0) {
				setF_OUT_updatedFlag(new Integer(1));
				setF_OUT_cancelFlagStatus(new Integer(9));
			}
			setF_OUT_disposalId(disposalObject.getDisposalRef());

			if (disposalObject.getCancelFlag() == 0) {

				messageObject_110.setAction("C");

			}
			else {

				messageObject_110.setMessageType("MT110");

			}

			messageObject_110.setDisposalRef(disposalObject.getCurrentDealNumber());

            IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());

			messageObject_110.setSender(branchObj.getF_BICCODE());

			String receiver = null;
			if (disposalObject.getSI_PayToBICCode() == null
					|| disposalObject.getSI_PayToBICCode().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)) {
				IBOSwtCustomerDetail swtCustObj = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
						IBOSwtCustomerDetail.BONAME, disposalObject.getContraAccCustomerNumber());
				receiver = swtCustObj.getF_BICCODE();
			}
			else {
				receiver = disposalObject.getSI_PayToBICCode().trim();
			}

			messageObject_110.setReceiver(receiver);

			messageObject_110.setSendersReference(disposalObject.getCurrentDealNumber());

			IBOSwtCustomerDetail contraCustObj = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
					IBOSwtCustomerDetail.BONAME, disposalObject.getContraAccCustomerNumber());
			
			if (disposalObject.getSI_PayToBICCode().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)
					&& contraCustObj.getF_BICCODE().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)) {
				// TODO: check Contra Customer Account Line is not spaces
                 
				if (util.isSwiftVostro(disposalObject.getContraAccountNo(), env)) {
					messageObject_110.setSendersCorrespondent(SWT_MT110192Constants.VOSTROCONSTANT);
					// TODO:Append Contra Customer Account Line
				}
				else {
					
					messageObject_110.setSendersCorrespondent(SWT_MT110192Constants.NONVOSTROCONSTANT);
					// TODO:Append Contra Customer Account Line
				}
				
				messageObject_110.setSendersCorrespOption(SWT_MT110192Constants.B_FLAG);

			}
			else if (disposalObject.getDealOriginator().equals("7")
					&& util.isSwiftNostro(disposalObject.getContraAccountNo(), env)) {

				messageObject_110.setSendersCorrespondent(contraCustObj.getF_SWTACCOUNTNO());
				messageObject_110.setSendersCorrespOption(SWT_MT110192Constants.B_FLAG);

			}
			else {
				if (!contraCustObj.getF_ALTERNATEBICCODE().trim().equalsIgnoreCase(contraCustObj.getF_BICCODE().trim())) {

					messageObject_110.setSendersCorrespondent(contraCustObj.getF_BICCODE().trim());

				}

				messageObject_110.setSendersCorrespOption(SWT_MT110192Constants.A_FLAG);
			}

			if (!disposalObject.getSI_IntermediatoryCode().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)) {
				
				messageObject_110.setReceiversCorrespondent(disposalObject.getSI_IntermediatoryAccInfo().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryCode().trim());
				

			}
			else if (!(disposalObject.getSI_IntermediatoryText1().trim().equalsIgnoreCase(
					SWT_MT110192Constants.EMPTYSTRING)
					|| disposalObject.getSI_IntermediatoryText2().trim().equalsIgnoreCase(
							SWT_MT110192Constants.EMPTYSTRING) || disposalObject.getSI_IntermediatoryText3().trim()
					.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING))) {
				
				messageObject_110.setReceiversCorrespondent(disposalObject.getSI_IntermediatoryAccInfo()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryText1().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryText2().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryText3().trim());
				
				messageObject_110.setReceiversCorrespOption(SWT_MT110192Constants.D_FLAG);
			}

			if (!(disposalObject.getSI_BankToBankInfo1().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)
					&& disposalObject.getSI_BankToBankInfo2().trim()
							.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)
					&& disposalObject.getSI_BankToBankInfo3().trim()
							.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)
					&& disposalObject.getSI_BankToBankInfo4().trim()
							.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)
					&& disposalObject.getSI_BankToBankInfo5().trim()
							.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING) && disposalObject
					.getSI_BankToBankInfo6().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING))) {

				messageObject_110.setSenderToReceiverInformation(disposalObject.getSI_BankToBankInfo1().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_BankToBankInfo2().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_BankToBankInfo3().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_BankToBankInfo4().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_BankToBankInfo5().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_BankToBankInfo6().trim());
			}
			chequeDetails.setChequeNumber(disposalObject.getBrokerNumber());

			
			chequeDetails.setDateOfIssue(disposalObject.getValueDate().toString());
			/*
			 * BPW-START messageContent.put(SWT_MT110192Constants.AMOUNT_32B,
			 * disposalObject.getContraAccCurrencyCode() +
			 * disposalObject.getTransactionAmount().toString());
			 */

			String amount = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(), util
					.noDecimalPlaces(disposalObject.getContraAccCurrencyCode(), env));

			
			chequeDetails.setAmount(disposalObject.getContraAccCurrencyCode() + amount);
			chequeDetails.setAmountOption("B");

			// BPW-END

			if (disposalObject.getSI_OrdInstBICCode().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)) {
				if ((disposalObject.getSI_OrdInstText1().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING))
						&& (disposalObject.getSI_OrdInstText2().trim()
								.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING))
						&& (disposalObject.getSI_OrdInstText3().trim()
								.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING))) {
					
					chequeDetails.setDrawerBank(disposalObject.getSI_OrdInstText1().trim()
							+ disposalObject.getSI_OrdInstText2() + disposalObject.getSI_OrdInstText3());

					chequeDetails.setDrawerBankOption("D");

				}
				else {
					
					chequeDetails.setDrawerBank(disposalObject.getSI_OrdInstText1().trim()
							+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_OrdInstText2()
							+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_OrdInstText3());
					chequeDetails.setDrawerBankOption("A");
				}
			}
			else {
				chequeDetails.setDrawerBank(disposalObject.getSI_OrdInstBICCode().trim());
				
			}

			if (!disposalObject.getSI_ForAccountInfo().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)
					|| !disposalObject.getSI_ForAccountText1().trim().equalsIgnoreCase(
							SWT_MT110192Constants.EMPTYSTRING)
					|| !disposalObject.getSI_ForAccountText2().trim().equalsIgnoreCase(
							SWT_MT110192Constants.EMPTYSTRING)
					|| !disposalObject.getSI_ForAccountText3().trim().equalsIgnoreCase(
							SWT_MT110192Constants.EMPTYSTRING)) {

				chequeDetails.setPayee(disposalObject.getSI_ForAccountText1().trim());
			}
			messageObject_110.addDetails(chequeDetails);
			message.clear();
			message.add(messageObject_110);
			int msgStatus = util.updateFlagValues(env, 110192, disposalObject.getDisposalRef());
			setF_OUT_msgStatusFlag(new Integer(msgStatus));

			setF_OUT_Message(message);

		}
		else {

			generateAnyMessage = false;
		}
	}
}
