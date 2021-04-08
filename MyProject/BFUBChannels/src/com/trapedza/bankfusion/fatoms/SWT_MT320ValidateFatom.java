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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT320;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT320Validate;
import com.trapedza.bankfusion.steps.refimpl.ISWT_MT320Validate;

/**
 * @author hardikp
 * 
 */
public class SWT_MT320ValidateFatom extends AbstractSWT_MT320Validate implements ISWT_MT320Validate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(SWT_MT320ValidateFatom.class.getName());
	private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;
	private final static String DEFULTRECIEVER = "UNKNOWN";

	final String NEGATIVE = "N";

	/**
	 * flag to decide whether contra acc is nostro acc
	 */
	private boolean contraAccIsNostroAcc = false;
	private SWT_Util util=new SWT_Util();
	/**
	 * flag to generate MT210 message
	 */
	private boolean generateMT320 = false;

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
	private String codeWord = null;

	/**
	 * ArrayList for storing xmlTagValue HashMap
	 */
	private ArrayList xmlTagValueMapList = null;

	public SWT_MT320ValidateFatom(BankFusionEnvironment env) {
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
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT320Validate#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {

		init();

		String contraAccDescription = CommonConstants.EMPTY_STRING;
		IBOSwtCustomerDetail contraAccCustDetails = null;
		IBOSwtCustomerDetail mainAccCustDetails = null;
		UB_MT320 messageObject_320 = new UB_MT320();
		if (disposalObject != null) {

			Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
			boolean generate320 = SWT_DataCalculation.generateCategory2Message(((SWT_DisposalObject) disposalObject)
					.getValueDate(), ((SWT_DisposalObject) disposalObject).getPostDate(), env,
					((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(), new java.sql.Date(
							bankFusionSystemDate.getTime()), "320");
			if (generate320) {

				if (disposalObject.getCancelFlag() != 0 && disposalObject.getConfirmationFlag() == 0) {
					setF_OUT_Confirmation_Flag(new Integer(1));
					setF_OUT_cancelFlagStatus(new Integer(9));
				}
				else if (disposalObject.getCancelFlag() == 0 && disposalObject.getConfirmationFlag() == 2) {
					setF_OUT_Confirmation_Flag(new Integer(3));
					int cancelStatus = util.updateCancelFlag(env, 320, disposalObject.getDisposalRef());
					setF_OUT_cancelFlagStatus(new Integer(cancelStatus));
				}
				contraAccIsNostroAcc=util.isSwiftNostro(disposalObject.getContraAccountNo(),env);
				// check for contraAcc is nostro acc
				try {
					IBOAccount accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME,
							disposalObject.getContraAccountNo());
					contraAccDescription = accountBO.getF_ACCOUNTDESCRIPTION();
				}catch (Exception e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
				try {
					// main account customer details
                    mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getMainAccCustomerNumber(), true);
					// contra account customer details
                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getContraAccCustomerNumber(), true);
                   /* mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
                            IBOSwtCustomerDetail.BONAME, disposalObject.getMainAccCustomerNumber());
                    // contra account customer details
                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
                            IBOSwtCustomerDetail.BONAME, disposalObject.getContraAccCustomerNumber());*/
				}catch (BankFusionException bfe) {
					logger.error("Error while getting CotraAccCustDetails OR Main Acc CUsto Details", bfe);
				}
				String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
				String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
				setF_OUT_disposalID(disposalObject.getDisposalRef());
				// which messages to be generated...
				if (disposalObject.getConfirmationFlag() == 0
						|| (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0))
					generateMT320 = true;
				if (disposalObject.getPaymentFlagMT202() == 0 || disposalObject.getPaymentFlagMT202() == 2)
					generateMT202 = true;
				else if (disposalObject.getReceiptFlagMT210() == 0 || disposalObject.getReceiptFlagMT210() == 2)
					generateMT210 = true;
				messageObject_320.setMessageType("MT320");
				messageObject_320.setDisposalRef(disposalObject.getDisposalRef());
                IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
				messageObject_320.setSender(branchObj.getF_BICCODE());
				String receiever = mainAccCustDetails.getF_BICCODE();
				messageObject_320.setReceiver(receiever);
		       /**
				 * sequence A
				 */
				//check for codeword being populated
				disposalObject.setCodeWord(disposalObject.getTransactionStatus());
				if (disposalObject.getCancelFlag() == 0
						&& (disposalObject.getRelatedDealNumber() != null && !disposalObject.getRelatedDealNumber()
								.equals(EMPTYSTRING)))
					messageObject_320.setSendersReference(disposalObject.getRelatedDealNumber());
				else
					messageObject_320.setSendersReference(disposalObject.getCurrentDealNumber());
				if (disposalObject.getCancelFlag() != 0)
					messageObject_320.setRelatedReference(disposalObject.getCurrentDealNumber());
				else if (disposalObject.getCurrentDealNumber() != null
						&& !disposalObject.getCurrentDealNumber().equals(CommonConstants.EMPTY_STRING))
					messageObject_320.setRelatedReference(disposalObject.getCurrentDealNumber());
				else
					
					messageObject_320.setRelatedReference("UNKNOWN");
    	  			codeWord = disposalObject.getTransactionStatus();
				if (disposalObject.getTransactionStatus().equals("AMEND")
						|| disposalObject.getTransactionStatus().startsWith("AM")) {
					messageObject_320.setTypeOfOperation("AMND");
					messageObject_320.setAction("A");
				}
				if (disposalObject.getCancelFlag() == 0) {
					messageObject_320.setTypeOfOperation("CANC");
					messageObject_320.setTypeOfEvent("CONF");
					messageObject_320.setAction("C");
				}else if (codeWord.indexOf("NEW") != -1) {
					messageObject_320.setTypeOfOperation("NEWT");
					messageObject_320.setTypeOfEvent("CONF");
				}
				if (codeWord.indexOf("ROL") != -1) {
					messageObject_320.setTypeOfEvent("ROLL");
				}
				else if (codeWord.indexOf("MAT") != -1) {
					messageObject_320.setTypeOfEvent("MATU");
				}
				if (codeWord.indexOf("NEW") != -1)
					messageObject_320.setTypeOfEvent("CONF");
				setF_OUT_CodeWord(codeWord);
				if (disposalObject.getTransactionStatus().equals("BRKDEP")
						|| disposalObject.getTransactionStatus().equals("BRKLON")
						|| disposalObject.getTransactionStatus().startsWith("BR")) {
					messageObject_320.setTypeOfOperation("CANC");
				}
				if (mainAccCustDetails.getF_BICCODE() == null && mainAccCustDetails.getF_BICCODE().equals(EMPTYSTRING)) {
						messageObject_320.setCommonReference(disposalObject.getCurrentDealNumber());
				}else {
					String branchBicCode = branchObj.getF_BICCODE();
					String bicCheck1 = branchBicCode.substring(0, 4) + branchBicCode.substring(6, 8);
					String bicCheck2 = null;
					String bicTwo = null;
					try {
						bicCheck2 = receiever.substring(0, 4);
						bicTwo = receiever.substring(6, 8);
						if (!bicCheck2.equals(EMPTYSTRING) && bicTwo.equals(EMPTYSTRING)) {
							messageObject_320.setCommonReference(disposalObject.getCurrentDealNumber());
						}else {
							bicCheck2 = bicCheck2 + bicTwo;
							String value22C = CommonConstants.EMPTY_STRING;
							String valFormat = CommonConstants.EMPTY_STRING;
							if (bicCheck1.compareTo(bicCheck2) < 0)
								value22C = bicCheck1;
							else
								value22C = bicCheck2;

							if (disposalObject.getInterestOrExchangeRate().compareTo(new BigDecimal(0)) == 0) {
								valFormat = "0000";
								value22C = value22C + valFormat;
							}
							else {
								valFormat = disposalObject.getInterestOrExchangeRate().toString();
								value22C = value22C + util.nonZeroValues(valFormat);
							}

							if (bicCheck2.compareTo(bicCheck1) < 0)
								value22C += bicCheck1;
							else
								value22C += bicCheck2;

							
							messageObject_320.setCommonReference(value22C);
						}

					}
					catch (Exception e) {
						logger.error(ExceptionUtil.getExceptionAsString(e));
						messageObject_320.setCommonReference(disposalObject.getCurrentDealNumber());
					}
				}

				
				messageObject_320.setPartyA(branchObj.getF_BICCODE());
				messageObject_320.setPartyAOption("A");
				messageObject_320.setPartyB(receiever);
				messageObject_320.setPartyBOption("A");
				messageObject_320.setTermsAndConditions(util.getBankToBankInfo(disposalObject));
				
				/**
				 * sequence B
				 */
					disposalObject.setCodeWord(disposalObject.getTransactionStatus());

				
				if (disposalObject.getPayReceiveFlag().equalsIgnoreCase("R")) {
					messageObject_320.setPartyARole("L");
				}
				else {
					messageObject_320.setPartyARole("B");
				}
				
				if (codeWord.indexOf("MAT") != -1) {
					HashMap datemap = new HashMap();
					datemap = getDates(disposalObject.getPreviousDealRecordNumber(), env);
					messageObject_320.setTradeDate(datemap.get("POSTDATE").toString());
					messageObject_320.setValueDate(datemap.get("VALUEDATE").toString());
					messageObject_320.setMaturityDate(datemap.get("METURITYDATE").toString());
				}
				else {
					messageObject_320.setTradeDate(disposalObject.getPostDate().toString());
					messageObject_320.setValueDate(disposalObject.getValueDate().toString());
					messageObject_320.setMaturityDate(disposalObject.getMaturityDate().toString());
				}
				  messageObject_320.setCcyPrincipalAmount(value32B(env));
     			  messageObject_320.setAmountSettled(value32H(env));
				if (!(codeWord.toUpperCase().indexOf("MAT") != -1))
					messageObject_320.setNextInterestDueDate(disposalObject.getNextInterestDueDate().toString());
					messageObject_320.setCcyAndInterestAmount(value34E(env));
				    messageObject_320.setInterestRate(disposalObject.getInterestOrExchangeRate().toString());
         			String str14D = get14DTagString(disposalObject.getMainAccCurrencyCode(), env);
        			if (str14D != null)
					 		messageObject_320.setDayCountFraction(str14D);
				else {
					/*throw new BankFusionException(127,
							new Object[] { "YearDays not found in currency table" }, logger, env);*/
					EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "YearDays not found in currency table" }, new HashMap(), env);
				}

			
				/**
				 * sequence C
				 */

				boolean interDetailsExistFlag = util.intermedaitoryDetailsExists(disposalObject);
				boolean accountwithExistFlag = util.accountWithDetailsExists(disposalObject);
				String ContraBicCode = CommonConstants.EMPTY_STRING;
				String tag53 = CommonConstants.EMPTY_STRING;
				if (contraAccIsNostroAcc&&contraAccCustDetails!=null) {
					ContraBicCode = contraAccCustDetails.getF_BICCODE();
					tag53 = "A";
				}
				messageObject_320.setCDeliveryAgent(ContraBicCode);
				messageObject_320.setCDeliveryAgentOption(tag53);
				String intermediatory56a = EMPTYSTRING;
				String tag56 = EMPTYSTRING;
				String receiveagent571 = EMPTYSTRING;
				String tag571 = "D";
				if (interDetailsExistFlag) {
					String tempString = util.createSwiftTagString(payToBICCode,
							disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
							disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3());
					if (!tempString.equals(EMPTYSTRING)) {
						intermediatory56a = tempString.substring(0, tempString.length() - 1);
						tag56 = tempString.substring(tempString.length() - 1);

						String tempString1 = util.createSwiftTagString(intermediaryBICCode, disposalObject
								.getSI_IntermediatoryAccInfo(), disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
								disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
								disposalObject.getSI_IntermediatoryText3());
						receiveagent571 = tempString1.substring(0, tempString1.length() - 1);
						tag571 = tempString1.substring(tempString1.length() - 1);
					}
					
				}else {
					String tempString = util.createSwiftTagString(payToBICCode,
							disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
							disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3());
					if (!tempString.equals(EMPTYSTRING)) {
						receiveagent571 = tempString.substring(0, tempString.length() - 1);
						if (receiveagent571.equalsIgnoreCase("$$$"))
							receiveagent571 = DEFULTRECIEVER;
						tag571 = tempString.substring(tempString.length() - 1);
					}else {
						receiveagent571 = DEFULTRECIEVER;
					}
				}
				messageObject_320.setCIntermediary(intermediatory56a);
				messageObject_320.setCIntermediaryOption(tag56);
				messageObject_320.setCReceivingAgent(receiveagent571);
				messageObject_320.setCReceivingAgentOption(tag571);

			

				/**
				 * sequence D
				 */
			

			
				tag56 = EMPTYSTRING;
				intermediatory56a = EMPTYSTRING;
				if (contraAccIsNostroAcc&&contraAccCustDetails!=null) {
					if (!contraAccCustDetails.getF_SWTACCOUNTNO().equals(EMPTYSTRING))
						receiveagent571 = contraAccCustDetails.getF_SWTACCOUNTNO().trim() + SWT_Constants.delimiter
								+ contraAccCustDetails.getF_BICCODE().trim();
					else
						receiveagent571 = contraAccCustDetails.getF_BICCODE().trim();
				       tag571 = "A";
					
				}
				else if (contraAccDescription != null && !contraAccDescription.equals(CommonConstants.EMPTY_STRING)) {
					receiveagent571 = "WE WILL DEBIT" + "$" + contraAccDescription;
					tag571 = "D";
				}
				else {
					receiveagent571 = "UNKNOWN";
					tag571 = "D";
				}
				messageObject_320.setDReceivingAgent(receiveagent571);
				messageObject_320.setDReceivingAgentOption(tag571);
				xmlTagValueMapList.add(messageObject_320);
				int msgStatus = util.updateFlagValues(env, 320, disposalObject.getDisposalRef());
				setF_OUT_msgStatusFlag(new Integer(msgStatus));

			}else {
				generateAnyMessage = false;
			}
		}
		else {
			generateAnyMessage = false;
			setF_OUT_disposalID("0");
		}
		setF_OUT_generateAnyMessage(Boolean.valueOf(generateAnyMessage));
		setF_OUT_XMLTAGVALUEMAPLIST(xmlTagValueMapList);
		setF_OUT_generateMT210(Boolean.valueOf(generateMT210));
		setF_OUT_generateMT202(Boolean.valueOf(generateMT202));
		setF_OUT_generateMT320(Boolean.valueOf(generateMT320));
	}

	private HashMap getDates(String prevDisposal, BankFusionEnvironment env) {
		HashMap dateMap = new HashMap();
		IBOSWTDisposal Disposal = (IBOSWTDisposal) env.getFactory().findByPrimaryKey(IBOSWTDisposal.BONAME,
				prevDisposal);

		dateMap.put("POSTDATE", (Date) Disposal.getDataMap().get(IBOSWTDisposal.POSTDATE));
		dateMap.put("VALUEDATE", (Date) Disposal.getDataMap().get(IBOSWTDisposal.VALUEDATE));
		dateMap.put("METURITYDATE", (Date) Disposal.getDataMap().get(IBOSWTDisposal.MATURITYDATE));
		return dateMap;
	}

	private String get14DTagString(String mainAccCurrencyCode, BankFusionEnvironment env) {
		String tag14 = CommonConstants.EMPTY_STRING;
		try {
			String accMain = disposalObject.getMainAccountNo();
            Map<String, String> inputs = new HashMap<String, String>();
            inputs.put("AccountId", accMain);
            HashMap outputParams = MFExecuter.executeMF("UB_CHG_FindAccountDetails_SRV",
                    com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal.getBankFusionEnvironment(),
                    inputs);
            IPersistenceObjectsFactory factory = env.getFactory();
            IBOProductInheritance prodIn = (IBOProductInheritance) factory.findByPrimaryKey(IBOProductInheritance.BONAME,
                    outputParams.get("PRODUCTCONTEXTCODE").toString(), false);
			if (prodIn.getF_CRINT_INTERESTBASEDAYSCR() != 0 || prodIn.getF_DRINT_INTERESTBASEDAYSDR() != 0) {
				if (disposalObject.getSI_PayReceiveFlag().equalsIgnoreCase("p")) {
					tag14 = prodIn.getF_CRINT_INTERESTBASEDAYSCR() + CommonConstants.EMPTY_STRING;
				}
				else {
					tag14 = prodIn.getF_DRINT_INTERESTBASEDAYSDR() + CommonConstants.EMPTY_STRING;
				}

			}
			else {
                IBOCurrency currencyBO = CurrencyUtil.getCurrencyDetailsOfCurrentZone(mainAccCurrencyCode);
				tag14 = (new Integer(currencyBO.getF_YEARDAYS())).toString();
			}
			return "ACT/" + tag14;
		}
		catch (BankFusionException bfe) {
			logger.error(ExceptionUtil.getExceptionAsString(bfe));
			return null;
		}
	}

	private String value32B(BankFusionEnvironment env) {

		
		String value32B = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(), util
				.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
		
		return disposalObject.getMainAccCurrencyCode() + value32B;
	}

	private String value32H(BankFusionEnvironment env) {
		String value32H = CommonConstants.EMPTY_STRING;
		
		if (!(codeWord.toUpperCase().indexOf("NEW") != -1)) {

			value32H = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(), util
					.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
			value32H = disposalObject.getMainAccCurrencyCode() + value32H;
			if (codeWord.toUpperCase().indexOf("DEINC") != -1 || codeWord.toUpperCase().indexOf("LODEC") != -1
					|| codeWord.toUpperCase().indexOf("MATLON") != -1) {
				value32H = NEGATIVE + value32H;
			}
		}
		return value32H;
	}

	private String updateCodeWord(String prevId, BankFusionEnvironment env) {

		IBOSWTDisposal Disposal = (IBOSWTDisposal) env.getFactory().findByPrimaryKey(IBOSWTDisposal.BONAME, prevId);
		String codeword = (String) Disposal.getDataMap().get(IBOSWTDisposal.TRANSACTIONSTATUS);
		if (!codeword.startsWith("AM"))
			codeword = "AM" + codeword;
		return codeword;
	}

	private String value34E(BankFusionEnvironment env) {
		
		String value34E = util.DecimalRounding(disposalObject.getInterestAmount().abs().toString(), util
				.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
		value34E = disposalObject.getMainAccCurrencyCode() + value34E;
		if (disposalObject.getPayReceiveFlag().compareTo("R") == 0)
			value34E = NEGATIVE + value34E;
		return value34E;
	}
}
