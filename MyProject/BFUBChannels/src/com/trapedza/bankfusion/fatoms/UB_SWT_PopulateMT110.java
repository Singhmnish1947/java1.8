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
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SWT_MT110192Constants;
import com.misys.ub.swift.UB_MT110;
import com.misys.ub.swift.UB_MT111;
import com.misys.ub.swift.UB_SWT_DisposalObject;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PopulateMT110;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_PopulateMT110;

public class UB_SWT_PopulateMT110 extends AbstractUB_SWT_PopulateMT110 implements IUB_SWT_PopulateMT110 {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private static final int MAXNUMBER=9;
	private final static String TagValueA = "A";
	private final static String TagValueK = "K";
	private boolean generateAnyMessage = true;
	private transient final static Log logger = LogFactory.getLog(UB_SWT_PopulateMT110.class.getName());
	public UB_SWT_PopulateMT110(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		
		UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) getF_IN_SWT_DisposalObject();
        UB_SWT_Util util=new UB_SWT_Util();
		Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
        boolean generate110 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject)
                 .getValueDate(), ((UB_SWT_DisposalObject) disposalObject).getPostDate(), env,
                ((UB_SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(), new java.sql.Date(
                        bankFusionSystemDate.getTime()), "110");
    	int tepm=0;
		if (generate110) {

			ChequeInfo chequeDetails = new ChequeInfo();
			UB_MT110 messageObject_110 = new UB_MT110();
			UB_MT111 messageObject_111 = new UB_MT111();

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
			

				messageObject_110.setMessageType("MT" +disposalObject.getMessageType());

			

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
			if(disposalObject.getMessageType().equalsIgnoreCase("110")){
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
			
   //    getSI_IntermediatoryAccInfo() is changed to getSI_IntermediaryPartyIdentifier()
			
			if (!disposalObject.getSI_IntermediatoryCode().trim().equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)) {
			
				messageObject_110.setReceiversCorrespondent(disposalObject.getSI_IntermediaryPartyIdentifier().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryCode().trim());
				

			}
	//Add new Field getSI_IntermediatoryText4() for Reorganization
			
			else if (!(disposalObject.getSI_IntermediatoryText1().trim().equalsIgnoreCase(
					SWT_MT110192Constants.EMPTYSTRING)
					|| disposalObject.getSI_IntermediatoryText2().trim().equalsIgnoreCase(
							SWT_MT110192Constants.EMPTYSTRING)||disposalObject.getSI_IntermediatoryText3().trim()
							.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING)
				    || disposalObject.getSI_IntermediatoryText4().trim()
								.equalsIgnoreCase(SWT_MT110192Constants.EMPTYSTRING))) {
				
				messageObject_110.setReceiversCorrespondent(disposalObject.getSI_IntermediaryPartyIdentifier()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryText1().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryText2().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryText3().trim()
						+ SWT_MT110192Constants.DELIMITER + disposalObject.getSI_IntermediatoryText4().trim());
				
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
			/// changes should be made here // ordering Customer begin
			
			String orderingCustomer50 = CommonConstants.EMPTY_STRING;
			String tag50 = CommonConstants.EMPTY_STRING;
			if ((disposalObject.getPartyIdentifier() != null || disposalObject.getOrderingCustomerAccountNumber() !=null)
					&& (disposalObject.getPartyIdentifier().trim()
							.length() > 0 || disposalObject.getOrderingCustomerAccountNumber().trim().length() >0)&& (disposalObject.getSI_OrdCustText1()!=null && disposalObject.getSI_OrdCustText1().length()>0) && disposalObject.getSI_OrdCustText1().substring(0,2).equalsIgnoreCase("1/")) {
				String custIdentification = CommonConstants.EMPTY_STRING;
				if(!disposalObject.getOrderingCustomerAccountNumber().isEmpty())
				{
					 custIdentification = disposalObject.getOrderingCustomerAccountNumber();
				}
				else{
					 custIdentification = disposalObject.getPartyIdentifier();
				}
				orderingCustomer50 = custIdentification
						+ SWT_Constants.delimiter
						+ disposalObject.getSI_OrdCustText1()
						+ SWT_Constants.delimiter
						+ disposalObject.getSI_OrdCustText2()
						+ SWT_Constants.delimiter
						+ disposalObject.getSI_OrdCustText3()
						+ SWT_Constants.delimiter
						+ disposalObject.getSI_OrdCustText4();
				tag50 = "F";
			} else if (disposalObject.getSI_OrdCustAccInfo() != null
					&& (disposalObject.getSI_OrdCustAccInfo()
							.trim().length() > 0)) {
				// changes start for artf52729
				if (disposalObject
						.getOrderingCustomerIdentifierCode() != null
						&& disposalObject
								.getOrderingCustomerIdentifierCode()
								.trim().length() > 0) {
					orderingCustomer50 = disposalObject
							.getSI_OrdCustAccInfo()
							+ SWT_Constants.delimiter
							+ disposalObject
									.getOrderingCustomerIdentifierCode();
					tag50 = "A";
				} else {
					// changes end for artf52729
					// Changes start for artf53203
					if (disposalObject.getPartyIdentifierAdd1() != null
							&& disposalObject
									.getPartyIdentifierAdd1()
									.trim().length() > 0) {
						orderingCustomer50 = disposalObject
								.getSI_OrdCustAccInfo()
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getPartyIdentifierAdd1())
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getPartyIdentifierAdd2())
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getPartyIdentifierAdd3())
								// artf53359 changes start
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getPartyIdentifierAdd4());
						// artf53359 changes end
					}
					if (disposalObject.getSI_OrdCustText1() != null
							&& disposalObject.getSI_OrdCustText1()
									.trim().length() > 0) {
						orderingCustomer50 = disposalObject
								.getSI_OrdCustAccInfo()
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getSI_OrdCustText1())
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getSI_OrdCustText2())
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getSI_OrdCustText3())
								// artf53359 changes start
								+ SWT_Constants.delimiter
								+ util.replaceSpecialChars(disposalObject
										.getSI_OrdCustText4());
						// artf53359 changes end
					}
					// Changes end for artf53203
					tag50 = "K";
					// changes start for artf52729
				}
				// changes end for artf52729
			} else if (disposalObject.getDealOriginator().equals(
					"F")) {
				if (disposalObject
						.getOrderingCustomerIdentifierCode() != null
						&& !disposalObject
								.getOrderingCustomerIdentifierCode()
								.equals(CommonConstants.EMPTY_STRING)) {
					orderingCustomer50 = disposalObject
							.getOrderingCustomerAccountNumber()
							+ SWT_Constants.delimiter
							+ disposalObject
									.getOrderingCustomerIdentifierCode();

					tag50 = TagValueA;
				} else if ((disposalObject.getPartyIdentifierAdd1() != null)
						&& !(disposalObject
								.getPartyIdentifierAdd1())
								.equals(CommonConstants.EMPTY_STRING)) {
					orderingCustomer50 = disposalObject
							.getOrderingCustomerAccountNumber()
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd1())
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd2())
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd3())
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd4());
					tag50 = TagValueK;
				}
			} else {
				if (disposalObject
						.getOrderingCustomerIdentifierCode() != null
						&& !disposalObject
								.getOrderingCustomerIdentifierCode()
								.equals(CommonConstants.EMPTY_STRING)) {
					orderingCustomer50 = disposalObject
							.getOrderingCustomerAccountNumber()
							+ SWT_Constants.delimiter
							+ disposalObject
									.getOrderingCustomerIdentifierCode();
					tag50 = TagValueA;
				} else if ((disposalObject.getPartyIdentifierAdd1() != null)
						&& !(disposalObject
								.getPartyIdentifierAdd1()
								.equals(CommonConstants.EMPTY_STRING))) {
					orderingCustomer50 = disposalObject
							.getOrderingCustomerAccountNumber()
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd1())
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd2())
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd3())
							+ SWT_Constants.delimiter
							+ util.replaceSpecialChars(disposalObject
									.getPartyIdentifierAdd4());
					tag50 = TagValueK;
				}
			}

			messageObject_110.setOrderingCustomer(orderingCustomer50);
			messageObject_110.setOrderingCustomerOption(tag50);
			
			
			// ordering Customer begin ends
			}
			
			chequeDetails.setChequeNumber(appendZero(disposalObject.getDraftNumber(),MAXNUMBER));

			
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
			String DrawerBank= util.createSwiftTagString(disposalObject.getSI_OrdInstBICCode(),disposalObject.getSI_OrdInstAccInfo(),disposalObject.getSI_OrdInstText1(), disposalObject.getSI_OrdInstText2(), disposalObject.getSI_OrdInstText3(), disposalObject.getSI_OrdInstText4());
			
			
		
			
		chequeDetails.setDrawerBank(DrawerBank.substring(0,DrawerBank.length() - 1));
		chequeDetails.setDrawerBankOption(DrawerBank.substring(DrawerBank.length() - 1));
			
			// getSI_ForAccountInfo()is changed to getSI_ForAccountPartyIdentifier()
		// swift 2017 - adding option F in field 59a
		
		String benCustomer59 = "";
		String tag59 = "";

		if (disposalObject.getSI_BankInstructionCode() != null && disposalObject.getSI_BankInstructionCode().equals("CHQB"))
		{
			benCustomer59 = util.getForAccountInfoString(disposalObject, true);

		}
		else
			benCustomer59 = util.getForAccountInfoString(disposalObject, false);
		if(!benCustomer59.isEmpty())
		{
			if( benCustomer59.substring(0,1).equalsIgnoreCase("/") &&
					((benCustomer59.indexOf('$')+1 < benCustomer59.length()) && (benCustomer59.indexOf('$')+3 < benCustomer59.length()) && benCustomer59.substring(benCustomer59.indexOf('$')+1,benCustomer59.indexOf('$')+3).equalsIgnoreCase("1/"))){
					tag59="F";
			}
			if(benCustomer59.substring(0,2).equalsIgnoreCase("1/"))
				tag59="F";
		}
		if (benCustomer59 != null && benCustomer59.trim().length() == 0) {
			// Changes end for artf49964
			if (!(disposalObject.getSI_ForAccountPartyIdentifier().equals(CommonConstants.EMPTY_STRING))&& !(disposalObject.getForAccountIdentifierCode().equals(CommonConstants.EMPTY_STRING))) {
				// Changes start for artf49964
				if (!(disposalObject.getSI_BankInstructionCode().equals("CHQB"))) {
					// Changes end for artf49964
					benCustomer59 = disposalObject.getSI_ForAccountPartyIdentifier()+ "$"+ disposalObject.getForAccountIdentifierCode();
					tag59 = "";
					// Changes start for artf49964
				}
				// Changes end for artf49964
				else {
					benCustomer59 = disposalObject.getForAccountIdentifierCode();
					tag59 = "";
				}
			}
			// Changes start for artf49964
			else {
				benCustomer59 = disposalObject.getForAccountIdentifierCode();
				tag59 = "";
			}
		} else if (benCustomer59 != null && benCustomer59.trim().length() > 0 && disposalObject.getSI_ForAccountText1().trim().length() == 0 && disposalObject.getSI_ForAccountText1() != null) {
			StringBuilder sb = new StringBuilder(benCustomer59).append("$").append(disposalObject.getForAccountIdentifierCode());
			benCustomer59 = sb.toString(); 
			tag59 = "";
		}
		// Changes end for artf49964
		chequeDetails.setPayee(benCustomer59);
	//	chequeDetails.setPayeeOption(tag59);
		messageObject_110.setPayeeOption(tag59);
		
		// swift 2017 - adding option F in field 59a
		
		
		messageObject_110.addDetails(chequeDetails);
			message.clear();
			
			if(disposalObject.getMessageType().equalsIgnoreCase("111")){
				messageObject_111.setChequeNumber(chequeDetails.getChequeNumber());
				messageObject_111.setDateOfIssue(chequeDetails.getDateOfIssue());
				messageObject_111.setAmount(chequeDetails.getAmount());
				messageObject_111.setAmountOption(chequeDetails.getAmountOption());
				messageObject_111.setDrawerBank(chequeDetails.getDrawerBank());
				messageObject_111.setDrawerBankOption(chequeDetails.getDrawerBankOption());
				messageObject_111.setPayee(chequeDetails.getPayee());
				
				//messageObject_111.addDetails(chequeDetails);
				messageObject_111.setAction(messageObject_110.getAction());
				messageObject_111.setMessageType(messageObject_110.getMessageType());
				messageObject_111.setSender(messageObject_110.getSender());
				messageObject_111.setReceiver(messageObject_110.getReceiver());
				messageObject_111.setDisposalRef(messageObject_110.getDisposalRef());
				messageObject_111.setSendersReference(messageObject_110.getSendersReference());
				messageObject_111.setReceiver(messageObject_110.getReceiver());
				
				message.add(messageObject_111);
			}else{
				message.add(messageObject_110);
			}
			int msgStatus = util.updateFlagValues(env, 110192, disposalObject.getDisposalRef());
			setF_OUT_msgStatusFlag(new Integer(msgStatus));

			setF_OUT_Message(message);

		}
		else {

			generateAnyMessage = false;
		}
	}
	
	
	private String appendZero(String input,int MaxLength){
		StringBuffer st=new StringBuffer();
		for(int i=0;i<MaxLength-input.length();i++){
			st.append("0");
		}
		return st.append(input).toString();
		
	}
}
