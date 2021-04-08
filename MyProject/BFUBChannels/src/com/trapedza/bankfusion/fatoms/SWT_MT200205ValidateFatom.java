/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT205;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT200205Validate;
import com.trapedza.bankfusion.steps.refimpl.ISWT_MT200205Validate;

/**
 * @author hardikp
 * 
 */
public class SWT_MT200205ValidateFatom extends AbstractSWT_MT200205Validate implements ISWT_MT200205Validate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(SWT_MT200205ValidateFatom.class.getName());
	private final static String MT200STR = "200";
	private final static String MT205STR = "205";
	private final static String NOSTRO = "NOSTRO";
	private final static String VOSTRO = "VOSTRO";
	private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;
	private final static String TagValueA = "A";

	/**
	 * This map holds PX-IX-BX as keys and values as 1 OR 0 for MT103, MT202,
	 * Flag72 and receiver flag
	 */
	HashMap messageGenerationMap = new HashMap();

	/**
	 * flag to decide whether contra acc is nostro acc
	 */
	private boolean mainAccIsNostroAcc = false;

	/**
	 * flag to decide whether contra acc is nostro acc
	 */
	private boolean contraAccIsVostroAcc = false;

	/**
	 * flag to generate any message or not
	 */
	private boolean generateAnyMessage = true;

	/**
	 * Desposal object
	 */
	private SWT_DisposalObject disposalObject = null;

	/**
	 * HashMap with keys as XML tag name and values as tag values for xml
	 * generation
	 */
	private HashMap xmlTagValueMap = new HashMap();

	/**
	 * ArrayList for storing xmlTagValue HashMap
	 */
	private ArrayList xmlTagValueMapList = null;

	/**
	 * ArrayList for storing xmlTagValue HashMap
	 */


	public SWT_MT200205ValidateFatom(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * Initializing frequently usable variables with values.
	 * 
	 * @
	 */
	private void init() {
		disposalObject = (SWT_DisposalObject) getF_IN_DisposalObject();
		xmlTagValueMap = new HashMap();
		xmlTagValueMapList = new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT200205Validate#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {

		init();
		IBOSwtCustomerDetail contraAccCustDetails = null;
		IBOSwtCustomerDetail mainAccCustDetails = null;
		IBOBicCodes receiverBicCodeDetails = null;
		ArrayList temp = new ArrayList();
		SWT_Util util=new SWT_Util();
		UB_MT205 messageObject_200205 = new UB_MT205();

		if (disposalObject != null) {
			/* 
			 * Check for look ahead days whether message should be generated or not .
			 *  
			 */
			Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
			boolean generate200205 = SWT_DataCalculation.generateCategory2Message(((SWT_DisposalObject) disposalObject)
					.getValueDate(), ((SWT_DisposalObject) disposalObject).getPostDate(), env,
					((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(), new java.sql.Date(
							bankFusionSystemDate.getTime()), disposalObject.getMessageType());
			if (generate200205) {
				boolean isBicAuthorised;
				if (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(3));
					int cancelStatus = util.updateCancelFlag(env, 200205, disposalObject.getDisposalRef());
					setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

				}
				else if (disposalObject.getConfirmationFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(1));
					setF_OUT_cancelFlagStatus(new Integer(9));
				}
				setF_OUT_disposalId(disposalObject.getDisposalRef());
				
					   contraAccIsVostroAcc=util.isSwiftVostro(disposalObject.getContraAccountNo(),env);
					   mainAccIsNostroAcc = util.isSwiftNostro(disposalObject.getMainAccountNo(),env);
				
				try {
					// contra account customer details
					contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
							IBOSwtCustomerDetail.BONAME, disposalObject.getContraAccCustomerNumber());
					// main account customer details
					mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
							IBOSwtCustomerDetail.BONAME, disposalObject.getMainAccCustomerNumber());
				}
				catch (BankFusionException bfe) {
					logger.error(
							"Error while getting  SettlementDetails OR COntraAccCustDetails OR Main Acc CUsto Details",
							bfe);
				}
				String receiever = contraAccCustDetails.getF_BICCODE();
				messageObject_200205.setReceiver(receiever);
				receiverBicCodeDetails = (IBOBicCodes) env.getFactory().findByPrimaryKey(IBOBicCodes.BONAME,receiever);
                isBicAuthorised = receiverBicCodeDetails.isF_BKEAUTH();

			if(isBicAuthorised){
				String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
				String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());

				
				messageObject_200205.setMessageType("MT" + disposalObject.getMessageType());

				
				messageObject_200205.setDisposalRef(disposalObject.getDisposalRef());

                    IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());

				

				messageObject_200205.setSender(branchObj.getF_BICCODE());

				


				
				messageObject_200205.setTransactionReferenceNumber(disposalObject.getCurrentDealNumber());
				if (disposalObject.getMessageType().equalsIgnoreCase(MT205STR)) {

					if (disposalObject.getRelatedDealNumber() != null
							&& !disposalObject.getRelatedDealNumber().equals(EMPTYSTRING))
					
						messageObject_200205.setRelatedReference(disposalObject.getRelatedDealNumber());
					else
					
						messageObject_200205.setRelatedReference(disposalObject.getCurrentDealNumber());

					String orderInstitute52 = CommonConstants.EMPTY_STRING;
					String tag52 = CommonConstants.EMPTY_STRING;
					
				orderInstitute52 = disposalObject.getOrderingInstitution();
				tag52 = TagValueA;
					messageObject_200205.setOrderingInstitution(orderInstitute52);
					
					messageObject_200205.setOrderingInstOption(tag52);

					String sendercorrsp53 = CommonConstants.EMPTY_STRING;
					String tag53 = CommonConstants.EMPTY_STRING;
					if (mainAccIsNostroAcc) {
						sendercorrsp53 = mainAccCustDetails.getF_BICCODE();
						tag53 = TagValueA;
					}
					
					messageObject_200205.setSendersCorrespondent(sendercorrsp53);
					
				}
				if (disposalObject.getMessageType().equalsIgnoreCase(MT200STR)) {
					String senderssCorrspondent53 = CommonConstants.EMPTY_STRING;
					if (contraAccCustDetails.getF_ALTERNATEACCOUNTNUMBER() != null
							&& contraAccCustDetails.getF_ALTERNATEACCOUNTNUMBER().trim().length() > 0) {
						if (contraAccIsVostroAcc)
							senderssCorrspondent53 = "/D/" + contraAccCustDetails.getF_ALTERNATEACCOUNTNUMBER();
						else
							senderssCorrspondent53 = "/C/" + contraAccCustDetails.getF_ALTERNATEACCOUNTNUMBER();
					}
					
					messageObject_200205.setSendersCorrespondent(senderssCorrspondent53);

				}

				HashMap tag32aMap = new HashMap();
						messageObject_200205.setTdvalueDate(disposalObject.getValueDate().toString());
				
				messageObject_200205.setTdcurrencyCode(disposalObject.getContraAccCurrencyCode());
				

				messageObject_200205.setTdamount(util.DecimalRounding(disposalObject.getTransactionAmount().abs()
						.toString(), util.noDecimalPlaces(disposalObject.getContraAccCurrencyCode(), env)));
				

				boolean interDetailsExistFlag = util.intermedaitoryDetailsExists(disposalObject);
				boolean beneficiaryExistFlag = util.accountWithDetailsExists(disposalObject);
				String accWithInstitute57a = EMPTYSTRING;
				String tag57 = EMPTYSTRING;
				String intermediatory56a = EMPTYSTRING;
				String tag56 = EMPTYSTRING;
				//TODO Looks like the code below is not correct.
				if (beneficiaryExistFlag) {
					if (interDetailsExistFlag) {
						String tempString = EMPTYSTRING;
						if (payToBICCode.compareTo(receiever) != 0) {
							tempString = util.createSwiftTagString(payToBICCode, disposalObject
									.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject
									.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3());
							if (!tempString.equals(EMPTYSTRING)) {
								intermediatory56a = tempString.substring(0, tempString.length() - 1);
								tag56 = tempString.substring(tempString.length() - 1);
							}
						}

						String tempString1 = util.createSwiftTagString(intermediaryBICCode, disposalObject
								.getSI_IntermediatoryAccInfo(), disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
								disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
								disposalObject.getSI_IntermediatoryText3());
						if (!tempString1.equals(EMPTYSTRING)) {
							accWithInstitute57a = tempString1.substring(0, tempString1.length() - 1);
							tag57 = tempString1.substring(tempString1.length() - 1);
						}
					}
					else {
						String tempString = util.createSwiftTagString(payToBICCode, disposalObject
								.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject
								.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
								.getSI_PayToText3());
						if (!tempString.equals(EMPTYSTRING)) {
							accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
							tag57 = tempString.substring(tempString.length() - 1);
						}
					}

					
					messageObject_200205.setIntermediary(intermediatory56a);
					
					messageObject_200205.setIntermediaryOption(tag56);
					
					messageObject_200205.setAccountWithInstitution(accWithInstitute57a);
					
					messageObject_200205.setAccountWithInstOption(tag57);

					if (disposalObject.getMessageType().equalsIgnoreCase(MT205STR)) {
						String tempString = util.createSwiftTagString(disposalObject.getSI_AccWithCode(),
								disposalObject.getSI_AccWithAccInfo(), disposalObject.getSI_AccWithNAT_CLR_Code(),
								disposalObject.getSI_AccWithText1(), disposalObject.getSI_AccWithText2(),
								disposalObject.getSI_AccWithText3());

						
						if (!tempString.equals(EMPTYSTRING)) {
							messageObject_200205.setBeneficiaryInstitute(tempString.substring(0,
									tempString.length() - 1));
						
							messageObject_200205
									.setBeneficiaryInstOption(tempString.substring(tempString.length() - 1));
						}
					} /* End of code for Beficiary for field 58 */

				} /* End of Beneficiary Exist if check */
				else {
					/* if beneficary details do not exist move PAY TO Details to Beneficiary*/
					String tempString = util.createSwiftTagString(payToBICCode,
							disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
							disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3());
					if (!tempString.equals(EMPTYSTRING)) {
						messageObject_200205.setBeneficiaryInstitute(tempString.substring(0, tempString.length() - 1));
						messageObject_200205.setBeneficiaryInstOption(tempString.substring(tempString.length() - 1));
					}
				}

			
				messageObject_200205.setSenderToReceiverInformation(util.getBankToBankInfo(disposalObject));
			
				
				if (disposalObject.getCancelFlag() == 0
						&& (disposalObject.getMessageType().equalsIgnoreCase(MT200STR) || disposalObject
								.getMessageType().equalsIgnoreCase(MT205STR))) {
					

					Timestamp toDateTime = SystemInformationManager.getInstance().getBFBusinessDateTime();
					String swiftDateTimeStr = util.getSwiftDateTimeString(toDateTime);
					StringTokenizer tempTok = new StringTokenizer(swiftDateTimeStr, " ");

				
					messageObject_200205.setMessageType(disposalObject.getMessageType());
					
					messageObject_200205.setTdvalueDate(tempTok.nextToken());
					
					messageObject_200205.setTransactionReferenceNumber(disposalObject.getCurrentDealNumber());
					
					messageObject_200205.setRelatedReference(disposalObject.getCurrentDealNumber());
					
				}
			
				temp.add(messageObject_200205);
				this.setF_OUT_XMLTAGVALUEMAPLIST(temp);
				int msgStatus = util.updateFlagValues(env, 200205, disposalObject.getDisposalRef());
				setF_OUT_msgStatusFlag(new Integer(msgStatus));

			}// End of Lookahead check if clause
			else{
				setF_OUT_msgStatusFlag(new Integer(9));
			}	
		 }
		}
		else {
			setF_OUT_disposalId("0");
			generateAnyMessage = false;
		}

		setF_OUT_generateAnyMessage(Boolean.valueOf(generateAnyMessage));

	}
}
