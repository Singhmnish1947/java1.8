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

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_MT900910Constants;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT900910;
import com.trapedza.bankfusion.bo.refimpl.IBOAddress;
import com.trapedza.bankfusion.bo.refimpl.IBOAddressLinks;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOBroker;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT900910Validate;

/**
 * @author Madhuchandra Fatom class to validate disposal object to generate
 *         MT900, MT910 and MT992 message
 */
public class SWT_MT900910Validate extends AbstractSWT_MT900910Validate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	String MessageName = CommonConstants.EMPTY_STRING;

	/**
	 * Default constructor which overrides super class constructor
	 * 
	 * @param env
	 */
	private transient final static Log logger = LogFactory.getLog(SWT_MT900910Validate.class.getClass());
	private SWT_Util util=new SWT_Util();
	public SWT_MT900910Validate(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Super class process method overriding to generate messages
	 */
	public void process(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
		super.process(env);
		if (this.getF_IN_DisposalObject() != null) {
			SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
			int CrDrFlag = ((SWT_DisposalObject) getF_IN_DisposalObject()).getCrdrFlag();
			int CancelFlag = ((SWT_DisposalObject) getF_IN_DisposalObject()).getCancelFlag();
			String messageType = ((SWT_DisposalObject) getF_IN_DisposalObject()).getMessageType();
			IBOSwtCustomerDetail CustDetails=null;
			setF_OUT_disposalId(disposalObject.getDisposalRef());
			String debitCredit = null;
			String swiftActive = null;
			String receiverBic = CommonConstants.EMPTY_STRING;
			// if(disposalObject.getDealOriginator().equalsIgnoreCase("F")){
			if (getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
				CustDetails=getContraCustDetails(env);
				//debitCredit = getContraCustDetails(env).getF_DRCONFIRMREQUIRED();
				//swiftActive = getContraCustDetails(env).getF_SWTACTIVE();
				//receiverBic = getContraCustDetails(env).getF_BICCODE();

			}
			else {
				if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
					CustDetails= getMainCustDetails(env);
					//debitCredit = getMainCustDetails(env).getF_CRCONFIRMREQUIRED();
					//swiftActive = getContraCustDetails(env).getF_SWTACTIVE();
					//receiverBic = getMainCustDetails(env).getF_BICCODE();

				}
				else {
					CustDetails=getContraCustDetails(env);
					//debitCredit = getContraCustDetails(env).getF_CRCONFIRMREQUIRED();
					//swiftActive = getContraCustDetails(env).getF_SWTACTIVE();
					//receiverBic = getContraCustDetails(env).getF_BICCODE();
				}

			}
			// }else{

			// debitCredit= getContraCustDetails(env).getF_DRCONFIRMREQUIRED();
			// }
			if(CustDetails!=null){
				debitCredit = CustDetails.getF_CRCONFIRMREQUIRED();
				swiftActive = CustDetails.getF_SWTACTIVE();
				receiverBic = CustDetails.getF_BICCODE();
			if ((debitCredit.equalsIgnoreCase("Y") && swiftActive.equalsIgnoreCase("Y"))
					&& ((CrDrFlag < 8 && messageType.equalsIgnoreCase("900"))
							|| (CrDrFlag < 8 && messageType.equalsIgnoreCase("910")) || (CrDrFlag < 8 || CrDrFlag < 8))
					&& receiverBic != null && !receiverBic.trim().equals(CommonConstants.EMPTY_STRING)) {
				if (disposalObject.getCrdrFlag() == 0 || disposalObject.getCrdrFlag() == 1) {
					setF_OUT_cancelFlagStatus(new Integer(9));
					if (disposalObject.getCrdrFlag() == 0) {
						setF_OUT_updatedFlag(new Integer(2));
					}
					else
						setF_OUT_updatedFlag(new Integer(3));
				}
				else if ((disposalObject.getCrdrFlag() == 4 || disposalObject.getCrdrFlag() == 5)
						&& (disposalObject.getCancelFlag() == 0)) {
					int cancelStatus = util.updateCancelFlag(env, 900910, disposalObject.getDisposalRef());
					setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

					if (disposalObject.getCrdrFlag() == 4) {
						setF_OUT_updatedFlag(new Integer(6));
					}
					else
						setF_OUT_updatedFlag(new Integer(7));
				}
				ArrayList xmlTags = new ArrayList();

				//HashMap xmlTagValueMap = new HashMap();
				UB_MT900910 messageObject_900910 = new UB_MT900910();
				if (disposalObject.getTransactionStatus().indexOf("AM") != -1)
					messageObject_900910.setAction("A");

				if (CancelFlag == 0 && (CrDrFlag == 4 || CrDrFlag == 5)) {
					messageObject_900910.setAction("C");
					if (CrDrFlag == 4) {
						messageObject_900910.setMessageType("MT900");
						MessageName = "900";
					}
					else {
						messageObject_900910.setMessageType("MT910");
						MessageName = "910";
					}
					// xmlTagValueMap
					// .put(SWT_MT900910Constants.MessageType, "992");
					//	 messageObject_900910.setMessageType("MT992");

				}
				else if (messageType.equalsIgnoreCase("900") || CrDrFlag == 0) {
					// xmlTagValueMap
					// .put(SWT_MT900910Constants.MessageType, "900");

					messageObject_900910.setMessageType("MT900");
					MessageName = "900";

				}
				else {
					// xmlTagValueMap
					// .put(SWT_MT900910Constants.MessageType, "910");
					messageObject_900910.setMessageType("MT910");
					MessageName = "910";
				}
				boolean generate900910 = SWT_DataCalculation.generateCategory2Message(
						((SWT_DisposalObject) disposalObject).getValueDate(), ((SWT_DisposalObject) disposalObject)
								.getPostDate(), env, ((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
						new java.sql.Date(bankFusionSystemDate.getTime()), MessageName);

				if (generate900910) {

					String disposalRef = getDisposalReference(env);
					// xmlTagValueMap.put(SWT_MT900910Constants.DisposalRef,disposalRef);
					messageObject_900910.setDisposalRef(disposalRef);
					String sender = getSender(env);
					// xmlTagValueMap.put(SWT_MT900910Constants.Sender, sender);
					messageObject_900910.setSender(sender);
					String receiver = getReceiver(env);
					// xmlTagValueMap.put(SWT_MT900910Constants.Receiver, receiver);
					messageObject_900910.setReceiver(receiver);
					// String transactionReference = getTransactionReference(env);
					// xmlTagValueMap.put(SWT_MT900910Constants.TransactionReference,((SWT_DisposalObject)
					// getF_IN_DisposalObject()).getCurrentDealNumber());
					messageObject_900910.setTransactionReference(((SWT_DisposalObject) getF_IN_DisposalObject())
							.getCurrentDealNumber());
					// String releatedReference = getReleatedReference(env);
					// xmlTagValueMap.put(SWT_MT900910Constants.ReleatedReference,
					// ((SWT_DisposalObject) getF_IN_DisposalObject()).getCurrentDealNumber());
					messageObject_900910.setRelatedReference(((SWT_DisposalObject) getF_IN_DisposalObject())
							.getCurrentDealNumber());
					//HashMap TAG11S = null;
					// TAG11S.put(SWT_MT103192Constants.MESSAGETYPE11S, CommonConstants.EMPTY_STRING);
					// TAG11S.put(SWT_MT103192Constants.DATE11S, CommonConstants.EMPTY_STRING);
					// TAG11S.put(SWT_MT103192Constants.TRANSREFNO20, CommonConstants.EMPTY_STRING);
					// TAG11S.put(SWT_MT103192Constants.RELATEDREF21, CommonConstants.EMPTY_STRING);

					//if (CancelFlag == 0) {

					// messageObject_900910 = getTag11s(env, messageObject_900910);

					//}
					// xmlTagValueMap.put(SWT_MT900910Constants.TAG11S, TAG11S);
					String accountIdentification = getTransactionReference(env);
					// xmlTagValueMap.put(SWT_MT900910Constants.AccountIdentification,accountIdentification);
					messageObject_900910.setAccountIdentification(accountIdentification);
					messageObject_900910 = getTransactiondetail(env, messageObject_900910);
					// xmlTagValueMap.put(SWT_MT900910Constants.TransactionDetail,transactionDetail);

					String orderingInstitution = CommonConstants.EMPTY_STRING;
					String orderingCustomer = CommonConstants.EMPTY_STRING;
					IBOSwtCustomerDetail clientCustomerDetails = getClientCustDetail(env);
					if ((CrDrFlag == 1 || CrDrFlag == 5)
							&& (clientCustomerDetails.getF_ISFINANCIALINSTITUTE().equals("N"))) {

						String tag50 = CommonConstants.EMPTY_STRING;
						if (((SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier() != null
								&& ((SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier().trim().length() > 0) {
							orderingCustomer = ((SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier()
									+ SWT_Constants.delimiter
									+ ((SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1()
									+ SWT_Constants.delimiter
									+ ((SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd2()
									+ SWT_Constants.delimiter
									+ ((SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd3()
									+ SWT_Constants.delimiter
									+ ((SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd4();
							tag50 = "F";
						}
						else {
							orderingCustomer = getCustomerDetailsString(clientCustomerDetails.getBoID(), env);
							
						}
					}
					if (((SWT_DisposalObject) getF_IN_DisposalObject()).getMessageType().compareTo("103") != 0)
						orderingInstitution = getOrderingInstitution(env);
					else
						orderingInstitution = sender;

					// xmlTagValueMap.put(SWT_MT900910Constants.OrderingCustomer,
					// orderingCustomer);

					// xmlTagValueMap.put(SWT_MT900910Constants.TAG50, tag50);

					// xmlTagValueMap.put(SWT_MT900910Constants.OrderingInstitution,
					// orderingInstitution);
					//String tag52 = getTag52(env);
					/*
					 * We are taking main client customer to confirm whether its a financial
					 * institution or not this work around is only for FX . MM & (T & D) still
					 * not covered because we are not sure what customer they are sending.
					 */

					if (clientCustomerDetails.getF_ISFINANCIALINSTITUTE().equals("Y")) {
						messageObject_900910.setOrderingInstitution(orderingInstitution);
						messageObject_900910.setOrderingInstOption("A");
					}
					else if (CrDrFlag == 1 && clientCustomerDetails.getF_ISFINANCIALINSTITUTE().equals("N")) {
						messageObject_900910.setOrderingCustomer(orderingCustomer);
						messageObject_900910.setOrderingCustOption("F");
					}
					// xmlTagValueMap.put(SWT_MT900910Constants.Tag52, tag52);

					// if MT910 message
					if ((CrDrFlag == 1 || CrDrFlag == 5)
							&& ((SWT_DisposalObject) getF_IN_DisposalObject()).getMessageType().compareTo("103") != 0) {
						String InterMediatoryInformation = getIntermediatoryInformation(env);
						// xmlTagValueMap.put(SWT_MT900910Constants.InterMediatory,
						// InterMediatoryInformation);
						messageObject_900910.setIntermediary(InterMediatoryInformation);
						String tag56 = getTag56(env);
						// xmlTagValueMap.put(SWT_MT900910Constants.Tag56, tag56);
						messageObject_900910.setIntermediaryOption(tag56);
					}

					String senderToReceiverInformation = getSenderToReceiverInformation(env);
					// xmlTagValueMap.put(
					// SWT_MT900910Constants.SenderToReceiverInformation,
					// senderToReceiverInformation);
					messageObject_900910.setSenderToReceiverInformation(senderToReceiverInformation);

					// xmlTags.add(xmlTagValueMap);
					xmlTags.add(messageObject_900910);
					this.setF_OUT_XmlTagsInput(xmlTags);
					this.setF_OUT_BranchSortCode(this.getF_IN_BranchSortCode());
				
					 setF_OUT_ispublish(util.IsPublish(disposalObject.getMessageType(), disposalObject.getConfirmationFlag(), disposalObject.getCancelFlag()));
					this.setF_OUT_generateMessage(new Boolean(true));

				}
			}
		}
			else {
				setF_OUT_updatedFlag(new Integer(9));
			}
			int msgStatus = util.updateFlagValues(env, 900910, disposalObject.getDisposalRef());
			setF_OUT_msgStatusFlag(new Integer(msgStatus));

		}
		else { // if disposal object is null
			setF_OUT_disposalId("0");
			this.setF_OUT_generateMessage(new Boolean(false));
		}

	}

	
	/**
	 * Method to get Tag56
	 * 
	 * @param env
	 * @return
	 */
	private String getTag56(BankFusionEnvironment env) {
		// TODO Auto-generated method stub

		String intermediatory_Info = getIntermediatoryInformation(env);
		if (intermediatory_Info.equals(CommonConstants.EMPTY_STRING)) {
			return intermediatory_Info;
		}
		else {
			return intermediatory_Info.substring(0, 1);
		}

	}

	/**
	 * Method to get intermediatory info
	 * 
	 * @param env
	 * @return
	 */
	private String getIntermediatoryInformation(BankFusionEnvironment env) {
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		String tempString = util.createSwiftTagString(disposalObject.getSI_IntermediatoryCode(), disposalObject
				.getSI_IntermediatoryAccInfo(),
				// disposalObject.getSI_O,
				disposalObject.getSI_IntermediatoryNAT_CLR_Code(), disposalObject.getSI_IntermediatoryText1(),
				disposalObject.getSI_IntermediatoryText2(), disposalObject.getSI_IntermediatoryText3());
		return tempString.trim();
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private IBOSwtCustomerDetail getContraCustDetails(BankFusionEnvironment env) {
		// contra account customer details
		 IBOSwtCustomerDetail contraAccCustDetails =null;
		try {
            contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                    ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccCustomerNumber(), true);
		/*IBOSwtCustomerDetail contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
				IBOSwtCustomerDetail.BONAME,
				((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccCustomerNumber());*/
	}catch (Exception e) {
		logger.error(ExceptionUtil.getExceptionAsString(e));
	}
		return contraAccCustDetails;
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private IBOSwtCustomerDetail getMainCustDetails(BankFusionEnvironment env) {
		// main account customer details
		IBOSwtCustomerDetail mainAccCustDetails=null;
		try {
            mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                    ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccCustomerNumber(), true);
		/*IBOSwtCustomerDetail mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
				IBOSwtCustomerDetail.BONAME,
				((SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccCustomerNumber());*/
		}catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
		return mainAccCustDetails;

	}

	private IBOSwtCustomerDetail getClientCustDetail(BankFusionEnvironment env) {

		IBOSwtCustomerDetail clientCustomer = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
				IBOSwtCustomerDetail.BONAME, ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getClientNumber());
		return clientCustomer;
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private String getSenderToReceiverInformation(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		return util.getBankToBankInfo(disposalObject);
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	/* private String getTag52(BankFusionEnvironment env)  {
	  // TODO Auto-generated method stub
	  String OrderingInstitute = getOrderingInstitution(env).trim();
	  if (OrderingInstitute.trim().length() == 0)
	   return OrderingInstitute;
	  return OrderingInstitute.substring(OrderingInstitute.length() - 1);
	 }*/

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private String getOrderingInstitution(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		String tempString = CommonConstants.EMPTY_STRING;
		IBOSwtCustomerDetail clientsDetails = null;
		if (!(disposalObject.getSI_PayToBICCode().equals(CommonConstants.EMPTY_STRING))
				|| (disposalObject.getSI_PayToBICCode().equals(CommonConstants.EMPTY_STRING))) {
			clientsDetails = getClientCustDetail(env);
			tempString = clientsDetails.getF_BICCODE();
			return tempString;

		}
		else {
			tempString = util.createSwiftTagString(disposalObject.getSI_PayToBICCode(), disposalObject
					.getSI_PayToAccInfo(), CommonConstants.EMPTY_STRING, disposalObject.getSI_PayToText1(),
					disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3());
			if (tempString.trim().length() == 0)
				return tempString;
			return tempString.substring(0, tempString.length() - 1);

		}
	}

	/**
	 * 
	 * @param env
	 * @param xmlTagValueMap
	 * @return
	 * @
	 */
	private UB_MT900910 getTransactiondetail(BankFusionEnvironment env, UB_MT900910 messageObject_900910) {
		// TODO Auto-generated method stub
		String valueDate = CommonConstants.EMPTY_STRING;
		String currencyCode = CommonConstants.EMPTY_STRING;
		String amount = CommonConstants.EMPTY_STRING;
		// HashMap transDetails = new HashMap();

		if (((SWT_DisposalObject) this.getF_IN_DisposalObject()).getTransactionStatus().indexOf("ROL") != -1)
			valueDate = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getPostDate().toString();
		else
			valueDate =((SWT_DisposalObject) this.getF_IN_DisposalObject()).getValueDate().toString();
		if ((((SWT_DisposalObject) this.getF_IN_DisposalObject()).getDealOriginator().equals("F"))) {
			if (((SWT_DisposalObject) this.getF_IN_DisposalObject()).getMessageType().compareTo("900") == 0) {
				currencyCode = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccCurrencyCode();
				amount = util.DecimalRounding(((SWT_DisposalObject) this.getF_IN_DisposalObject())
						.getTransactionAmount().abs().toString(), util.noDecimalPlaces(currencyCode, env));
			}
			else {
				currencyCode = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccCurrencyCode();
				amount = util.DecimalRounding(((SWT_DisposalObject) this.getF_IN_DisposalObject())
						.getContractAmount().abs().toString(), util.noDecimalPlaces(currencyCode, env));
			}
		}
		else {
			currencyCode = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccCurrencyCode();
			amount = util.DecimalRounding(((SWT_DisposalObject) this.getF_IN_DisposalObject())
					.getTransactionAmount().abs().toString(), util.noDecimalPlaces(currencyCode, env));
		}
		if ((((SWT_DisposalObject) this.getF_IN_DisposalObject()).getDealOriginator().equals("8"))) {
			amount = util.DecimalRounding(((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContractAmount()
					.abs().toString(), util.noDecimalPlaces(currencyCode, env));
		}
		/*
		 * amount = ((SWT_DisposalObject) this.getF_IN_DisposalObject())
		 * .getContractAmount().abs().toString();
		 */

		// transDetails.put(SWT_MT900910Constants.ValueDate, valueDate);
		// transDetails.put(SWT_MT900910Constants.CurrencyCode, currencyCode);
		// transDetails.put(SWT_MT900910Constants.Amount, amount);
		messageObject_900910.setTdValueDate(valueDate);
		messageObject_900910.setTdCurrencyCode(currencyCode);
		messageObject_900910.setTdAmount(amount);
		return messageObject_900910;
	}

	private String getTransactionReference(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		//IBOSwtCustomerDetail custDetails = null;
		// String CustomerNumber = CommonConstants.EMPTY_STRING;
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
			if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
				return ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
				// return ((SWT_DisposalObject)
				// this.getF_IN_DisposalObject()).getMainAccountNo();
			}
			else {
				// return ((SWT_DisposalObject)
				// this.getF_IN_DisposalObject()).getContraAccountNo();
				return ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();

			}
		}
		else {
			return ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
		}

	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private String getReceiver(BankFusionEnvironment env) {
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		String bicCode = "";
		IBOSwtCustomerDetail iboSwtCustomerDetail;
		if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
			if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
				iboSwtCustomerDetail = this.getContraCustDetails(env);
				if(null!= iboSwtCustomerDetail) {
					bicCode = iboSwtCustomerDetail.getF_BICCODE();
				}
				return bicCode;

			}
			else {
				iboSwtCustomerDetail = this.getMainCustDetails(env);
				if(null!= iboSwtCustomerDetail) {
					bicCode = iboSwtCustomerDetail.getF_BICCODE();
				}
				return bicCode;

			}
		}
		else {
			iboSwtCustomerDetail = this.getContraCustDetails(env);
			if(null!= iboSwtCustomerDetail) {
				bicCode = iboSwtCustomerDetail.getF_BICCODE();
			}
			return bicCode;
		}

	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private String getSender(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
        IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
		return branchObj.getF_BICCODE();
		// return null;
	}

	/**
	 * 
	 * @param env
	 * @return
	 */
	private String getDisposalReference(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		return ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getDisposalRef();
		// return null;
	}

	/**
	 * 
	 * @param env
	 * @return
	 */
	private String getMessageType(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		if (disposalObject.getCancelFlag() == 0
				&& (disposalObject.getCrdrFlag() == 2 || disposalObject.getCrdrFlag() == 3))
			return SWT_MT900910Constants.CRDBCancel;
		if ((SWT_MT900910Constants.DBConfirm.equals(disposalObject.getMessageType()) || disposalObject.getCrdrFlag() == 0)
				&& disposalObject.getCancelFlag() > 0)
			return SWT_MT900910Constants.DBConfirm;
		if ((SWT_MT900910Constants.CRConfirm.equals(disposalObject.getMessageType()) || disposalObject.getCrdrFlag() == 1)
				&& disposalObject.getCancelFlag() > 0)
			return SWT_MT900910Constants.CRConfirm;

		return SWT_MT900910Constants.InvalidMessage;

	}

	private String getCustomerDetailsString(String custCode, BankFusionEnvironment env) {
		String custNameAddressString = null;
		try {
			IBOBroker BrokerBo = (IBOBroker) env.getFactory().findByPrimaryKey(IBOBroker.BONAME, custCode);
			IBOCustomer customerBO = (IBOCustomer) env.getFactory().findByPrimaryKey(IBOCustomer.BONAME,
					BrokerBo.getF_CUSTOMERCODE());
			StringBuffer cBuffer = new StringBuffer();
			cBuffer.append(customerBO.getF_SHORTNAME() + "$");
			String whereCluaseForAddressLink = " WHERE " + IBOAddressLinks.CUSTACC_KEY + " = ? AND "
					+ IBOAddressLinks.DEFAULTADDRINDICATOR + " = ?";
			ArrayList params = new ArrayList();
			params.add(customerBO.getBoID());
			params.add(new Boolean(true));
			ArrayList addressLinkList = (ArrayList) env.getFactory().findByQuery(IBOAddressLinks.BONAME,
					whereCluaseForAddressLink, params, null);
			IBOAddressLinks addressLink = (IBOAddressLinks) addressLinkList.get(0);
			IBOAddress addressDetails = (IBOAddress) env.getFactory().findByPrimaryKey(IBOAddress.BONAME,
					addressLink.getF_ADDRESSID());
			cBuffer.append(addressDetails.getF_ADDRESSLINE1() + "$" + addressDetails.getF_ADDRESSLINE2() + "$"
					+ addressDetails.getF_ADDRESSLINE3());
			custNameAddressString = cBuffer.toString();
		}
		catch (BankFusionException bfe) {
			logger.error("Error while getting customer name and address", bfe);
		}
		return custNameAddressString;
	}

}
