/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_MT103192Constants;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT210;
import com.trapedza.bankfusion.bo.refimpl.IBOAddress;
import com.trapedza.bankfusion.bo.refimpl.IBOAddressLinks;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT210Validate;

/**
 * @author Madhuchandra
 * 
 */
public class SWT_MT210Validate extends AbstractSWT_MT210Validate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private transient final static Log logger = LogFactory.getLog(SWT_MT210Validate.class.getName());

	private String MT210STR = "210";

	private String MT292STR = "292";
	private SWT_Util util=new SWT_Util();
	public SWT_MT210Validate(BankFusionEnvironment env) {
		super(env);
		// Auto-generated constructor stub
	}

	String mainAccountBICCode = null;

	String clientBICCode = null;

	SWT_DisposalObject dispObject = null;

	String accountNumber = null;

	String customerNumber = null;

	String relatedReference = CommonConstants.EMPTY_STRING;

	public void process(BankFusionEnvironment env) {
		// Auto-generated method stub
		dispObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		super.process(env);
		UB_MT210 messageObject_210 = new UB_MT210();
		
		boolean generate210 = false;
		if (this.getF_IN_DisposalObject() != null) {

			Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
			generate210 = SWT_DataCalculation.generateCategory2Message(
					((SWT_DisposalObject) dispObject).getValueDate(), ((SWT_DisposalObject) dispObject).getPostDate(),
					env, ((SWT_DisposalObject) dispObject).getContraAccCurrencyCode(), new java.sql.Date(
							bankFusionSystemDate.getTime()), "210");

			if (generate210) {

				if (dispObject.getReceiptFlagMT210() == 2 && dispObject.getCancelFlag() == 0) {
					setF_OUT_UpdatedFlag(new Integer(3));
					generate210 = true;
					int cancelStatus = util.updateCancelFlag(env, 210292, dispObject.getDisposalRef());
					setF_OUT_cancelFlagStatus(new Integer(cancelStatus));
					// setF_OUT_Status("C");

				}
				else if (dispObject.getReceiptFlagMT210() == 0) {
					setF_OUT_UpdatedFlag(new Integer(1));
					generate210 = true;
					setF_OUT_cancelFlagStatus(new Integer("9"));
					// setF_OUT_Status("N");
				}

				int msgStatus = util.updateFlagValues(env, 210, dispObject.getDisposalRef());
				setF_OUT_msgStatusFlag(new Integer(msgStatus));

				String dealOriginator = ((SWT_DisposalObject) dispObject).getDealOriginator();
				Date date = null;
				Timestamp bankFusionSystemDate1 = SystemInformationManager.getInstance().getBFBusinessDateTime();
				String messageType1 = ((SWT_DisposalObject) dispObject).getMessageType();
				String Currency = null;
				if (!(dealOriginator.equalsIgnoreCase("F"))) {
					date = ((SWT_DisposalObject) dispObject).getValueDate();
					Currency = ((SWT_DisposalObject) dispObject).getMainAccCurrencyCode();
				}
				else {
					date = ((SWT_DisposalObject) dispObject).getMaturityDate();
					Currency = ((SWT_DisposalObject) dispObject).getContraAccCurrencyCode();
					;
				}
				if (generate210)
					generate210 = util.generateCategory2Message(date, env, Currency, new java.sql.Date(
							bankFusionSystemDate1.getTime()), messageType1);

				if (generate210) {
					ArrayList xmlTags = new ArrayList();

					if (dispObject.getCancelFlag() == 0 && dispObject.getReceiptFlagMT210() == 2) {
						messageObject_210.setAction("C");
					}
					else if (dispObject.getTransactionStatus().startsWith("AM")) {
						/*
						 * If the Transaction Type is AMEND (Code_Word) i.e amendment then we are
						 * just setting the action tag as "A"
						 */
						messageObject_210.setAction("A");
					}
					messageObject_210.setMessageType("MT210");
					String disposalRef = getDisposalReference(env);
					// xmlTagValueMap.put("DisposalRef", disposalRef);
					messageObject_210.setDisposalRef(disposalRef);
					setF_OUT_disposalID(disposalRef);
					String sender = this.getSender(env);
					// xmlTagValueMap.put("Sender", sender);
					messageObject_210.setSender(sender);
					String receiver = null;
					customerNumber = dispObject.getMainAccCustomerNumber();
					mainAccountBICCode = this.getMainCustDetails(env).getF_BICCODE();
					/*
					 * Commented for Issue 8310 on FD as wrong Receiver to take only contra
					 * account details
					 */
					// if (dispObject.getDealOriginator().compareTo("F") == 0) {
					accountNumber = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
					customerNumber = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccCustomerNumber();
					receiver = this.getMainCustDetails(env).getF_BICCODE();
					;
					/*
					 * } else { receiver = mainAccountBICCode; accountNumber =
					 * ((SWT_DisposalObject) this .getF_IN_DisposalObject()).getMainAccountNo();
					 * customerNumber = ((SWT_DisposalObject) this .getF_IN_DisposalObject())
					 * .getMainAccCustomerNumber(); }
					 */// xmlTagValueMap.put("Receiver", receiver);
					messageObject_210.setReceiver(receiver);
					// Tag 20
					String transactionReference = getTransactionReference(env);
					// xmlTagValueMap
					// .put("TransactionReference", transactionReference);
					messageObject_210.setTransactionReferenceNumber(transactionReference);
					// Tag 25
					// xmlTagValueMap.put("AccountNumber",
					// this.getMainCustDetails(env).getF_SWTACCOUNTNO());
					messageObject_210.setAccountIdentification(this.getMainCustDetails(env).getF_SWTACCOUNTNO());
					// tag 30
					String valueDate = CommonConstants.EMPTY_STRING;
					if (dispObject.getDealOriginator().compareTo("F") == 0)
						valueDate = ((SWT_DisposalObject) this.getF_IN_DisposalObject())
								.getMaturityDate().toString();
					else {
						if (dispObject.getTransactionStatus().indexOf("ROL") != -1)
							valueDate =((SWT_DisposalObject) this.getF_IN_DisposalObject())
											.getPostDate().toString();
						else
							valueDate = ((SWT_DisposalObject) this.getF_IN_DisposalObject())
											.getValueDate().toString();
						//valueDate = util.getSwiftDateString(((SWT_DisposalObject) this.getF_IN_DisposalObject()).getValueDate());
					}
					messageObject_210.setValueDate(valueDate);
					// xmlTagValueMap.put("ValueDate", valueDate);

					// Tag 21
					IBOSwtCustomerDetail swtCustDetails = null;
					customerNumber = dispObject.getClientNumber();
					swtCustDetails = getMainCustDetails(env);
					clientBICCode = swtCustDetails.getF_BICCODE();

					if (clientBICCode != null && clientBICCode.trim().length() > 0)
						relatedReference = getRelatedReference(env);
					else
						relatedReference = dispObject.getCurrentDealNumber();

					// xmlTagValueMap.put(SWT_MT900910Constants.ReleatedReference,
					// releatedReference);
					messageObject_210.setRelatedReference(relatedReference);

					messageObject_210.setCurrencyCodeAmount(getTag32(env));

					String name = CommonConstants.EMPTY_STRING;
					String orderingCustomer = CommonConstants.EMPTY_STRING;
					String custNameADD = CommonConstants.EMPTY_STRING;
					String custNameADD1 = CommonConstants.EMPTY_STRING;
					String tag52 = CommonConstants.EMPTY_STRING;
					if (dispObject.getDealOriginator().compareTo("F") == 0) {
						customerNumber = dispObject.getClientNumber();
						swtCustDetails = getMainCustDetails(env);
					}
					else {
						customerNumber = dispObject.getMainAccCustomerNumber();
						swtCustDetails = getMainCustDetails(env);
					}
					// String orderingCustomer50 = CommonConstants.EMPTY_STRING;
					String tag50 = CommonConstants.EMPTY_STRING;
					// String tempStr = CommonConstants.EMPTY_STRING;
					/*
					 * When the customer is a Financial institute take the bic code for
					 * orderingInstitute . If the customer is not a financial institute check
					 * with the party identifier .
					 */
					if ((swtCustDetails.getF_ISFINANCIALINSTITUTE().compareTo("N") == 0)
							&& (dispObject.getPartyIdentifier() != null)
							&& (dispObject.getPartyIdentifier().trim().length() > 0)) {
						StringBuffer strbuff = new StringBuffer();
						strbuff.append(dispObject.getPartyIdentifier());
						strbuff.append(SWT_Constants.delimiter);
						strbuff.append(dispObject.getPartyIdentifierAdd1());
						if (!dispObject.getPartyIdentifierAdd1().equals(CommonConstants.EMPTY_STRING))
							strbuff.append(SWT_Constants.delimiter);
						strbuff.append(dispObject.getPartyIdentifierAdd2());
						if (!dispObject.getPartyIdentifierAdd2().equals(CommonConstants.EMPTY_STRING))
							strbuff.append(SWT_Constants.delimiter);
						strbuff.append(dispObject.getPartyIdentifierAdd3());
						if (!dispObject.getPartyIdentifierAdd3().equals(CommonConstants.EMPTY_STRING))
							strbuff.append(SWT_Constants.delimiter);
						strbuff.append(dispObject.getPartyIdentifierAdd4());
						orderingCustomer = strbuff.toString().trim();
						tag50 = "F";
					}
					else if (swtCustDetails.getF_ISFINANCIALINSTITUTE().compareTo("N") == 0
							&& (swtCustDetails.getF_BICCODE() == null || swtCustDetails.getF_BICCODE().trim().length() == 0)) {
						orderingCustomer = getCustomerDetailsString(customerNumber, env);
						tag50 = CommonConstants.EMPTY_STRING;
					}
					else if (swtCustDetails.getF_ISFINANCIALINSTITUTE().compareTo("N") == 0
							&& (swtCustDetails.getF_BICCODE() != null || swtCustDetails.getF_BICCODE().trim().length() > 0)) {
						orderingCustomer = swtCustDetails.getF_BICCODE();
						tag50 = "C";
					}
					else if (swtCustDetails.getF_ISFINANCIALINSTITUTE().compareTo("Y") == 0
							&& (swtCustDetails.getF_BICCODE() == null || swtCustDetails.getF_BICCODE().trim().length() == 0)) {
						orderingCustomer = getCustomerDetailsString(customerNumber, env);
						tag52 = "D";
					}
					else if (swtCustDetails.getF_ISFINANCIALINSTITUTE().compareTo("Y") == 0
							&& (swtCustDetails.getF_BICCODE() != null || swtCustDetails.getF_BICCODE().trim().length() >= 0)) {
						orderingCustomer = swtCustDetails.getF_BICCODE();
						tag52 = "A";
					}
					/*
					 * If the Customer ordering the deal is a financial institution then
					 * populate tag 52A , and if it is not a financial institution populate tag
					 * 50A
					 */
					if (swtCustDetails.getF_ISFINANCIALINSTITUTE().compareTo("Y") == 0) {
						messageObject_210.setOrderingInstitution(orderingCustomer);
						messageObject_210.setOrderingInstitutionOption(tag52);
					}
					else {
						messageObject_210.setOrderingCustomer(orderingCustomer);
						messageObject_210.setOrderingCustomerOption(tag50);
					}
					// xmlTagValueMap.put("OrderingCustomer", name);
					// xmlTagValueMap.put("Tag50", tag50);
					// tag 52
					// xmlTagValueMap.put("OrderingInstitute", tag52);
					// xmlTagValueMap.put("Tag52", tag52);

					xmlTags.add(messageObject_210);
					this.setF_OUT_MessageMap(xmlTags);
					this.setF_OUT_GenerateMessage(new Boolean(true));
					
					 setF_OUT_ispublish(util.IsPublish(dispObject.getMessageType(), dispObject.getConfirmationFlag(), dispObject.getCancelFlag()));
				}

				else {
					this.setF_OUT_GenerateMessage(new Boolean(false));
				}
			}
		}

	}

	private HashMap getTag11s(BankFusionEnvironment env) {
		String MessageType = this.getActualMessageType(env);
		Timestamp toDateTime = SystemInformationManager.getInstance().getBFBusinessDateTime();
		String swiftDateTimeStr = util.getSwiftDateTimeString(toDateTime);
		StringTokenizer tempTok = new StringTokenizer(swiftDateTimeStr);
		HashMap tag11sMap = new HashMap();
		tag11sMap.put(SWT_MT103192Constants.MESSAGETYPE11S, "210");
		tag11sMap.put(SWT_MT103192Constants.DATE11S, tempTok.nextToken());
		tag11sMap.put(SWT_MT103192Constants.RELATEDREF21, getTransactionReference(env));
		tag11sMap.put(SWT_MT103192Constants.TRANSREFNO20, getTransactionReference(env));
		// tag11sMap.put(SWT_MT103192Constants.TIME11S, tempTok.nextToken());
		return tag11sMap;
	}

	private String getActualMessageType(BankFusionEnvironment env) {

		SWT_DisposalObject dispObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		if (dispObject.getReceiptFlagMT210() == 2 && dispObject.getCancelFlag() == 0) {

			// setF_OUT_Status("C");
			return MT292STR;
		}
		else {
			setF_OUT_UpdatedFlag(new Integer(1));
			// setF_OUT_Status("N");
			return MT210STR;
		}
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private String getTag52(BankFusionEnvironment env) {
		// Auto-generated method stub
		String tempOrderingInstitution = getOrderingInstitution(env);
		String tag52 = CommonConstants.EMPTY_STRING;
		if (!tempOrderingInstitution.equals(CommonConstants.EMPTY_STRING)) {
			tag52 = tempOrderingInstitution.substring(0, 1);
		}
		return tag52;
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private String getOrderingInstitution(BankFusionEnvironment env) {
		// Auto-generated method stub
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();
		String tempString = util.createSwiftTagString(disposalObject.getSI_OrdInstBICCode(), disposalObject
				.getSI_OrdInstAccInfo(),
				// disposalObject.getSI_O,
				CommonConstants.EMPTY_STRING, disposalObject.getSI_OrdInstText1(), disposalObject.getSI_OrdInstText2(),
				disposalObject.getSI_OrdInstText3());
		return tempString.trim();
	}

	/**
	 * 
	 * This method returns customer name and address details string with "$" as
	 * delimiter between fields.
	 * 
	 * @param custCode
	 * @param env
	 * @return
	 * @
	 */
	private String getCustomerDetailsString(String custCode, BankFusionEnvironment env) {
		String custNameAddressString = null;
		try {
			IBOCustomer customerBO = (IBOCustomer) env.getFactory().findByPrimaryKey(IBOCustomer.BONAME, custCode);
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
			if (!addressDetails.getF_ADDRESSLINE1().equals(CommonConstants.EMPTY_STRING)) {
				cBuffer.append(addressDetails.getF_ADDRESSLINE1() + "$");
			}
			if (!addressDetails.getF_ADDRESSLINE2().equals(CommonConstants.EMPTY_STRING)) {
				cBuffer.append(addressDetails.getF_ADDRESSLINE2() + "$");
			}
			cBuffer.append(addressDetails.getF_ADDRESSLINE3());

			custNameAddressString = cBuffer.toString();
		}
		catch (BankFusionException bfe) {
			logger.error(ExceptionUtil.getExceptionAsString(bfe));
			return CommonConstants.EMPTY_STRING;
			
			
		}
		return custNameAddressString;
	}

	private String getRelatedReference(BankFusionEnvironment env) {

		// Auto-generated method stub
		String relatedRef = null;
		String clientPartialBICCode = clientBICCode.substring(0, 4) + clientBICCode.substring(6, 8);
		/* ConvertBicCheck is commented as it is not required . */
		// clientPartialBICCode = SWT_Util.convertBicCheck(clientPartialBICCode);
		String BranchPartialBICCode = this.getSender(env).substring(0, 4) + this.getSender(env).substring(6, 8);
		// String mainAccountPartialBICCode = mainAccountBICCode.substring(0, 4) +
		// mainAccountBICCode.substring(6, 8);
		// BranchPartialBICCode = SWT_Util.convertBicCheck(BranchPartialBICCode);
		// mainAccountPartialBICCode =
		// SWT_Util.convertBicCheck(mainAccountPartialBICCode);

		if (clientPartialBICCode.compareTo(BranchPartialBICCode) < 0)
			relatedRef = clientPartialBICCode;
		else
			relatedRef = BranchPartialBICCode;

		if (dispObject.getInterestOrExchangeRate().compareTo(new BigDecimal(0)) == 0) {
			relatedRef = relatedRef + "0000";
		}
		else {
			String valFormat = dispObject.getInterestOrExchangeRate().toString();
			relatedRef = relatedRef + util.nonZeroValues(valFormat).trim();
		}

		if (clientPartialBICCode.compareTo(BranchPartialBICCode) > 0)
			relatedRef = relatedRef + clientPartialBICCode;
		else
			relatedRef = relatedRef + BranchPartialBICCode;

		return relatedRef;
	}

	private String getTransactionReference(BankFusionEnvironment env) {
		// Auto-generated method stub
		String tranRef = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getCurrentDealNumber();
		tranRef = (tranRef != null) ? tranRef : CommonConstants.EMPTY_STRING;
		return tranRef;
	}

	/*
	 * private HashMap getTag32(BankFusionEnvironment env) throws
	 * BankFusionException { // Auto-generated method stub String currencyCode =
	 * ((SWT_DisposalObject) this
	 * .getF_IN_DisposalObject()).getContraAccCurrencyCode();
	 * 
	 * String amount = ((SWT_DisposalObject) this.getF_IN_DisposalObject())
	 * .getTransactionAmount().toString(); currencyCode = (currencyCode != null) ?
	 * currencyCode : " "; amount = (amount != null) ? amount : " "; amount =
	 * SWT_Util.DecimalRounding(amount, SWT_Util.noDecimalPlaces( currencyCode,
	 * env)); HashMap retValue = new HashMap(); retValue.put("CurrencyCode",
	 * currencyCode); retValue.put("amount", amount); return retValue; }
	 */
	private String getTag32(BankFusionEnvironment env) {

		// Auto-generated method stub
		String amount = null;
		
		String currencyCode = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccCurrencyCode();
		if (((SWT_DisposalObject) this.getF_IN_DisposalObject()).getDealOriginator().equalsIgnoreCase("8")) {
			amount = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getContractAmount().abs().toString();
		}
		else {
			amount = ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getTransactionAmount().abs().toString();
		}
		currencyCode = (currencyCode != null) ? currencyCode : " ";
		amount = (amount != null) ? amount : " ";
		amount = util.DecimalRounding(amount, util.noDecimalPlaces(currencyCode, env));
		// HashMap retValue = new HashMap();
		// retValue.put("CurrencyCode", currencyCode);
		// retValue.put("amount", amount);

		return currencyCode + amount;
	}

	/**
	 * 
	 * @param env
	 * @return
	 */
	private String getMessageType(BankFusionEnvironment env) {
		// Auto-generated method stub
		SWT_DisposalObject disposalObject = (SWT_DisposalObject) this.getF_IN_DisposalObject();

		return MT210STR;

	}

	/**
	 * 
	 * @param env
	 * @return
	 */
	private String getDisposalReference(BankFusionEnvironment env) {
		// Auto-generated method stub
		return ((SWT_DisposalObject) this.getF_IN_DisposalObject()).getDisposalRef();
		// return null;
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private String getSender(BankFusionEnvironment env) {
		// Auto-generated method stub
        IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_Branch_Sort_Code());
		return branchObj.getF_BICCODE();
		// return null;
	}

	/**
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private IBOSwtCustomerDetail getMainCustDetails(BankFusionEnvironment env) {
		// main account customer details
		IBOSwtCustomerDetail mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
				IBOSwtCustomerDetail.BONAME, customerNumber);
		return mainAccCustDetails;

	}

	private IBOCustomer getCustomerDetails(BankFusionEnvironment env) {
		IBOCustomer mainAccCustDetails = (IBOCustomer) env.getFactory().findByPrimaryKey(IBOCustomer.BONAME,
				customerNumber);
		return mainAccCustDetails;
	}

	private IBOSwtCustomerDetail getContraCustDetails(BankFusionEnvironment env) {
		// Contra account customer details
		IBOSwtCustomerDetail mainContraCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
				IBOSwtCustomerDetail.BONAME,
				((SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccCustomerNumber());
		return mainContraCustDetails;

	}

}
