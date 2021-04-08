/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT300;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
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
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT300Validate;
import com.trapedza.bankfusion.steps.refimpl.ISWT_MT300Validate;

/**
 * @author hardikp
 * 
 */
public class SWT_MT300ValidateFatom extends AbstractSWT_MT300Validate implements ISWT_MT300Validate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(SWT_MT300ValidateFatom.class.getName());

	private final static String NEW = "NEW";
	private final static String CANCEL = "CANCEL";
	private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;

	/**
	 * flag to decide whether contra acc is nostro acc
	 */
	private boolean mainAccIsNostroAcc = false;

	/**
	 * flag to decide whether contra acc is nostro acc
	 */
	private boolean contraAccIsNostroAcc = false;

	/**
	 * flag to generate MT210 message
	 */
	private boolean generateMT300 = false;

	/**
	 * flag to generate MT210 message
	 */
	private boolean generateMT210 = false;

	/**
	 * flag to generate MT202 message
	 */
	private boolean generateMT202 = false;

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
	private HashMap xmlTagValueMap = null;

	/**
	 * ArrayList for storing xmlTagValue HashMap
	 */
	private ArrayList xmlTagValueMapList = null;

	public SWT_MT300ValidateFatom(BankFusionEnvironment env) {
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
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT300Validate#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {

		init();

		IBOSwtCustomerDetail contraAccCustDetails = null;
		IBOSwtCustomerDetail mainAccCustDetails = null;
		IBOSwtCustomerDetail clientCustDetails = null;
		UB_MT300 messageObject_300 = new UB_MT300();
		SWT_Util util=new SWT_Util();
		String mainAccDescription = null;
		String contraAccDescription = CommonConstants.EMPTY_STRING;
		if (disposalObject != null) {
			Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
			boolean generate300 = SWT_DataCalculation.generateCategory2Message(((SWT_DisposalObject) disposalObject)
					.getValueDate(), ((SWT_DisposalObject) disposalObject).getPostDate(), env,
					((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(), new java.sql.Date(
							bankFusionSystemDate.getTime()), "300");
			if (generate300) {

				if (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(3));
					int cancelStatus = util.updateCancelFlag(env, 300, disposalObject.getDisposalRef());
					setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

				}
				else if (disposalObject.getConfirmationFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(1));
					setF_OUT_cancelFlagStatus(new Integer(9));
				}
				// check for contraAcc is nostro acc
				try {
					IBOAccount accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME,
							disposalObject.getMainAccountNo());
					
						mainAccIsNostroAcc = util.isSwiftNostro(disposalObject.getMainAccountNo(),env);;
						mainAccDescription = accountBO.getF_ACCOUNTDESCRIPTION();
				}
				catch (Exception e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
					
				}
				try {
					IBOAccount accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME,
							disposalObject.getContraAccountNo());
				
						
						contraAccIsNostroAcc = util.isSwiftNostro(disposalObject.getContraAccountNo(),env);;
						contraAccDescription = accountBO.getF_ACCOUNTDESCRIPTION();
				}
				catch (Exception e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
					
				}
				try {
					// contra account customer details
					contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
							IBOSwtCustomerDetail.BONAME, disposalObject.getContraAccCustomerNumber());
					// main account customer details
					mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
							IBOSwtCustomerDetail.BONAME, disposalObject.getMainAccCustomerNumber());

					// Client Details
					clientCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
							IBOSwtCustomerDetail.BONAME, disposalObject.getClientNumber());

					// Broker Details

				}
				catch (BankFusionException bfe) {
					logger.error(
							"Error while getting  SettlementDetails OR COntraAccCustDetails OR Main Acc CUsto Details",
							bfe);
					generateAnyMessage = false;
				}

				String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
				String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
				String accWithBICCode = util.verifyForNull(disposalObject.getSI_AccWithCode());

				// which messages to be generated...
				if (disposalObject.getConfirmationFlag() == 0
						|| (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0))
					generateMT300 = true;
				if (disposalObject.getPaymentFlagMT202() == 0
						|| (disposalObject.getPaymentFlagMT202() == 2 && disposalObject.getCancelFlag() == 0))
					generateMT202 = true;
				if (disposalObject.getReceiptFlagMT210() == 0
						|| (disposalObject.getReceiptFlagMT210() == 2 && disposalObject.getCancelFlag() == 0))
					generateMT210 = true;

				
				messageObject_300.setMessageType("MT300");
				
				messageObject_300.setDisposalRef(disposalObject.getDisposalRef());
				setF_OUT_disposalID(disposalObject.getDisposalRef());
                IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
				String sender = branchObj.getF_BICCODE();
				messageObject_300.setSender(sender);
				String receiever = clientCustDetails.getF_BICCODE();
				messageObject_300.setReceiver(receiever);
				messageObject_300.setSenderReference(disposalObject.getCurrentDealNumber());
				messageObject_300.setRelatedReference(disposalObject.getCurrentDealNumber());

				if (disposalObject.getTransactionStatus() != null
						&& !disposalObject.getTransactionStatus().equals(CommonConstants.EMPTY_STRING)) {
					if (disposalObject.getTransactionStatus().equalsIgnoreCase(NEW)
							&& disposalObject.getConfirmationFlag() == 0) {
						
						messageObject_300.setTypeOfOperation("NEWT");
					}
					else if (disposalObject.getTransactionStatus().startsWith("AM")) {
						
						messageObject_300.setTypeOfOperation("AMND");
						messageObject_300.setAction("A");

					}
					else if (disposalObject.getTransactionStatus().equalsIgnoreCase(CANCEL)
							|| disposalObject.getCancelFlag() == 0) {
					
						messageObject_300.setTypeOfOperation("CANC");
						messageObject_300.setAction("C");
					}
				}

				if (clientCustDetails.getF_BICCODE() == null || clientCustDetails.getF_BICCODE().equals(EMPTYSTRING)) {
				
					messageObject_300.setCommonReference(disposalObject.getCurrentDealNumber());
				}
				else {
					String branchBicCode = branchObj.getF_BICCODE();

					String bicCheck1 = branchBicCode.substring(0, 4) + branchBicCode.substring(6, 8);
					String bicCheck2 = null;
					String bicTwo = null;
					try {
						bicCheck2 = clientCustDetails.getF_BICCODE().substring(0, 4);
						bicTwo = clientCustDetails.getF_BICCODE().substring(6, 8);
						if (!bicCheck2.equals(EMPTYSTRING) && bicTwo.equals(EMPTYSTRING)) {
						
							messageObject_300.setCommonReference(disposalObject.getCurrentDealNumber());
						}
						else {
							bicCheck2 = bicCheck2 + bicTwo;
							/* The convertBicCheck is commented as it is converting the numbers
							 * to lower case characters and this throws error in MMM as SWIFT would
							 * not accept any lower case characters. */
							
							String value22C = CommonConstants.EMPTY_STRING;
							String valFormat = CommonConstants.EMPTY_STRING;
							if (bicCheck1.compareTo(bicCheck2) < 0)
								value22C = bicCheck1;
							else
								value22C = bicCheck2;

							if (disposalObject.getInterestOrExchangeRate().compareTo(new BigDecimal(0)) == 0) {
								valFormat = disposalObject.getCurrentDealNumber().trim();
								value22C = value22C + valFormat;
							}
							else {
								valFormat = disposalObject.getInterestOrExchangeRate().toString();
								value22C = value22C + util.nonZeroValues(valFormat).trim();
							}

							if (bicCheck2.compareTo(bicCheck1) < 0)
								value22C += bicCheck1;
							else
								value22C += bicCheck2;

							
							messageObject_300.setCommonReference(value22C);
						}

					}
					catch (Exception e) {
						
						messageObject_300.setCommonReference(disposalObject.getCurrentDealNumber());
                      logger.error(ExceptionUtil.getExceptionAsString(e));
					}
				}

				
				messageObject_300.setPartyA(branchObj.getF_BICCODE());
				messageObject_300.setPartyAOption("A");
			
				messageObject_300.setPartyB(receiever);

				messageObject_300.setPartyBOption("A");
				messageObject_300.setTradeDate(disposalObject.getPostDate().toString());
				
				messageObject_300.setValueDate(disposalObject.getMaturityDate().toString());
				
				messageObject_300.setExchangeRate(disposalObject.getInterestOrExchangeRate().toString());

				
				String amount = util.DecimalRounding(disposalObject.getTransactionAmount().toString(), util
						.noDecimalPlaces(disposalObject.getContraAccCurrencyCode(), env));
				

				messageObject_300.setB1CurrencyAmount(disposalObject.getContraAccCurrencyCode() + amount);

				String str57 = CommonConstants.EMPTY_STRING;
				String tag57 = CommonConstants.EMPTY_STRING;

				if (contraAccIsNostroAcc) {
					str57 = contraAccCustDetails.getF_SWTACCOUNTNO() + SWT_Constants.delimiter
							+ contraAccCustDetails.getF_BICCODE();

					tag57 = "A";
				}
				else if (contraAccDescription != null && !contraAccDescription.equals(CommonConstants.EMPTY_STRING)) {
					str57 = contraAccDescription;
					tag57 = "D";
				}
				else {
					str57 = "UNKNOWN";
					tag57 = "D";
				}
			
				messageObject_300.setB1ReceivingAgent(str57);

			
				messageObject_300.setB1ReceivingAgentOption(tag57);

				amount = util.DecimalRounding(disposalObject.getContractAmount().toString(), util
						.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));

				
				messageObject_300.setB2CurrencyAmount(disposalObject.getMainAccCurrencyCode() + amount);

				if (mainAccIsNostroAcc) {
					
					messageObject_300.setB2DeliveryAgent(mainAccCustDetails.getF_BICCODE());
					messageObject_300.setB2DeliveryAgentOption("A");
				}
				else {
					
					messageObject_300.setB2DeliveryAgent("INTERNAL");
					messageObject_300.setB2DeliveryAgentOption("D");

				}
				boolean interDetailsExistFlag = util.intermedaitoryDetailsExists(disposalObject);
				boolean payToDetailsExistFlag = util.payToDetailsExists(disposalObject);

				String intermediatory56a = CommonConstants.EMPTY_STRING;
				String tag56 = CommonConstants.EMPTY_STRING;
				String receiveagent571 = CommonConstants.EMPTY_STRING;
				String tag571 = CommonConstants.EMPTY_STRING;
				
				if (interDetailsExistFlag) {
					String tempString = util.createSwiftTagString(payToBICCode,
							disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
							disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3());
					if (!tempString.equals(EMPTYSTRING)) {
						intermediatory56a = tempString.substring(0, tempString.length() - 1);
						tag56 = tempString.substring(tempString.length() - 1);
					}

					String tempString1 = util.createSwiftTagString(intermediaryBICCode, disposalObject
							.getSI_IntermediatoryAccInfo(), disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
							disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
							disposalObject.getSI_IntermediatoryText3());
					if (!tempString1.equals(EMPTYSTRING)) {
						receiveagent571 = tempString1.substring(0, tempString1.length() - 1);
						tag571 = tempString1.substring(tempString1.length() - 1);
					}
				}
				else if (payToDetailsExistFlag) {
					String tempString = util.createSwiftTagString(payToBICCode,
							disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
							disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3());
					if (!tempString.equals(EMPTYSTRING)) {
						receiveagent571 = tempString.substring(0, tempString.length() - 1);
						tag571 = tempString.substring(tempString.length() - 1);
					}
				}
				else {
					receiveagent571 = "UNKNOWN";
					tag571 = "D";
				}
			
				messageObject_300.setB2Intermediary(intermediatory56a);
			
				messageObject_300.setB2IntermediaryOption(tag56);
				
				messageObject_300.setB2ReceivingAgent(receiveagent571);
				messageObject_300.setB2ReceivingAgentOption(tag571);

				

				String brockerBICCode = CommonConstants.EMPTY_STRING;
				String tag88 = CommonConstants.EMPTY_STRING;
				String brockerId88 = CommonConstants.EMPTY_STRING;
				
				if (disposalObject.getBrokerNumber() != null && disposalObject.getBrokerNumber().trim().length() > 0) {
					brockerBICCode = getBrockerBICCode(disposalObject.getBrokerNumber(), env);
					if (brockerBICCode != null) {
						brockerId88 = brockerBICCode;
						tag88 = "A";
					}
					else {
						String tempCustString = getCustomerDetailsString(disposalObject.getBrokerNumber(), env);
						if (tempCustString != null) {
							brockerId88 = tempCustString;
							tag88 = "D";
						}
					}
				}
			
				messageObject_300.setBrokerID(brockerId88);
				messageObject_300.setBrokerIDOption(tag88);
			
				messageObject_300.setSendersToReceiversInfo(util.getBankToBankInfo(disposalObject));
			

				xmlTagValueMapList.add(messageObject_300);

				int msgStatus = util.updateFlagValues(env, 300, disposalObject.getDisposalRef());
				setF_OUT_msgStatusFlag(new Integer(msgStatus));
			}
			else {
				generateAnyMessage = false;
			}
		}
		else {
			generateAnyMessage = false;
		}
		setF_OUT_generateAnyMessage(Boolean.valueOf(generateAnyMessage));
		setF_OUT_XMLTAGVALUEMAPLIST(xmlTagValueMapList);
		setF_OUT_generateMT210(Boolean.valueOf(generateMT210));
		setF_OUT_generateMT202(Boolean.valueOf(generateMT202));
		setF_OUT_generateMT300(Boolean.valueOf(generateMT300));

	}

	/**
	 * 
	 * This method retund brocker BICCODE from customer config table
	 * 
	 * @param custCode
	 * @param env
	 * @return
	 * @
	 */
	private String getBrockerBICCode(String custCode, BankFusionEnvironment env) {
		String barockerBicCodeString = null;
		String customerCode = null;
		try {
			customerCode = ((IBOBroker) env.getFactory().findByPrimaryKey(IBOBroker.BONAME, custCode))
					.getF_CUSTOMERCODE();
		}
		catch (Exception e) {
			logger.error("Error while getting brocker Code", e);
		}
		try {
			IBOSwtCustomerDetail brockerDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
					IBOSwtCustomerDetail.BONAME, customerCode);
			String brockerBICCode = brockerDetails.getF_BICCODE();
			if (brockerBICCode != null && !brockerBICCode.equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				barockerBicCodeString = brockerBICCode;
		}
		catch (Exception e) {
			logger.error("Error while getting brocker BICCode", e);
		}
		return barockerBicCodeString;
	}

	/**
	 * This method returns customer name and address details as a String with "$"
	 * as a delimiter
	 * 
	 * @param custCode
	 * @param env
	 * @return
	 * @
	 */
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
