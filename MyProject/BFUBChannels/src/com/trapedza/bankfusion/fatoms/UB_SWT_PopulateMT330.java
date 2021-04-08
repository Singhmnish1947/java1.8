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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.payment.posting.SWTPostingUtils;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.UB_MT330;
import com.misys.ub.swift.UB_SWT_DisposalObject;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PopulateMT330;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_PopulateMT330;

/**
 * @author angraj
 * 
 */
public class UB_SWT_PopulateMT330 extends AbstractUB_SWT_PopulateMT330 implements IUB_SWT_PopulateMT330 {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(UB_SWT_PopulateMT330.class.getName());
	private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;
	private final static String NEGATIVE = "N";
	private UB_SWT_Util util=new UB_SWT_Util();
	public String msgObj="A";
	public String payRecFlag="R";
	public String InterBase="p";
	/**
	 * flag to decide whether contra acc is nostro acc
	 */
	private boolean contraAccIsNostroAcc = false;

	/**
	 * flag to generate MT210 message
	 */
	private boolean generateMT330 = false;

	/**
	 * flag to generate MT210 message
	 */
	private boolean generateMT210 = false;

	/**
	 * flag to generate MT202 message
	 */
	private boolean generateMT202 = false;
	private String codeWord = null;
	/**
	 * flag to generate any message or not
	 */
	private boolean generateAnyMessage = true;

	/**
	 * Desposal object
	 */
	private UB_SWT_DisposalObject disposalObject = null;

	/**
	 * HashMap with keys as XML tag name and values as tag values for xml
	 * generation
	 */
	private HashMap xmlTagValueMap = null;

	/**
	 * ArrayList for storing xmlTagValue HashMap
	 */
	private ArrayList xmlTagValueMapList = null;

	public UB_SWT_PopulateMT330(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * Initializing frequently usable variables with values.
	 * 
	 * @
	 */
	private void init() {
    	disposalObject = (UB_SWT_DisposalObject) getF_IN_DisposalObject();
		xmlTagValueMap = new HashMap();
		xmlTagValueMapList = new ArrayList();

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT330Validate#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {

		init();

		
		String contraAccDescription = CommonConstants.EMPTY_STRING;
	   IBOSwtCustomerDetail contraAccCustDetails = null;
		IBOSwtCustomerDetail mainAccCustDetails = null;
		UB_MT330 messageObject_330 = new UB_MT330();
		
		
		if (disposalObject != null) {

			Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
			boolean generate330 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject)
					.getValueDate(), ((UB_SWT_DisposalObject) disposalObject).getPostDate(), env,
					((UB_SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(), new java.sql.Date(
							bankFusionSystemDate.getTime()), "330");

			if (generate330) {

				if (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(3));
					int cancelStatus = util.updateCancelFlag(env, 330, disposalObject.getDisposalRef());
					setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

				}
				else if (disposalObject.getConfirmationFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(1));
					setF_OUT_cancelFlagStatus(new Integer(9));
				}
				contraAccIsNostroAcc=util.isSwiftNostro(disposalObject.getContraAccountNo(),env);
				// check for contraAcc is nostro acc
				 try {
				     IBOAccount accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME,
	                            disposalObject.getContraAccountNo());
				     contraAccDescription =  SWTPostingUtils.getCustomerDetails(accountBO.getF_CUSTOMERCODE(), disposalObject.getContraAccountNo());
	                    // main account customer details
                    mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getMainAccCustomerNumber(), false);
	                    
	                    
	                    
	                    // contra account customer details
                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getContraAccCustomerNumber(), false);
	                   /* mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
	                            IBOSwtCustomerDetail.BONAME, disposalObject.getMainAccCustomerNumber());
	                    // contra account customer details
	                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
	                            IBOSwtCustomerDetail.BONAME, disposalObject.getContraAccCustomerNumber());*/
                }
                catch (Exception e) {
                    logger.error("Error while getting CotraAccCustDetails OR Main Acc CUsto Details", e);
	                    //throw new BankFusionException(9466, null, logger, env);
	                    EventsHelper.handleEvent(ChannelsEventCodes.E_CONTRA_ACCOUNT_CUSTOMER_SHOULD_BE_SWIFT_ACTIVE, new Object[]{}, new HashMap(), env);
	                }

				String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
				String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
				setF_OUT_disposalID(disposalObject.getDisposalRef());
				// which messages to be generated...
				if (disposalObject.getConfirmationFlag() == 0
						|| (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0))
					generateMT330 = true;
				if (disposalObject.getPaymentFlagMT202() == 0 || disposalObject.getPaymentFlagMT202() == 2)
					generateMT202 = true;
				else if (disposalObject.getReceiptFlagMT210() == 0 || disposalObject.getReceiptFlagMT210() == 2)
					generateMT210 = true;

				
				messageObject_330.setMessageType("MT330");

				messageObject_330.setDisposalRef(disposalObject.getDisposalRef());
                IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
				messageObject_330.setSender(branchObj.getF_BICCODE());
				String receiever = mainAccCustDetails.getF_BICCODE();
     			messageObject_330.setReceiver(receiever);
				if (disposalObject.getCancelFlag() == 0
						&& (disposalObject.getRelatedDealNumber() != null && !disposalObject.getRelatedDealNumber()
								.equals(EMPTYSTRING)))
					messageObject_330.setSendersReference(disposalObject.getRelatedDealNumber());
				else
					messageObject_330.setSendersReference(disposalObject.getCurrentDealNumber());
				if (disposalObject.getCancelFlag() != 0)
					messageObject_330.setRelatedReference(disposalObject.getCurrentDealNumber());
				else if (disposalObject.getCurrentDealNumber() != null
						&& !disposalObject.getCurrentDealNumber().equals(CommonConstants.EMPTY_STRING))
						messageObject_330.setRelatedReference(disposalObject.getCurrentDealNumber());
				else
					messageObject_330.setRelatedReference("UNKNOWN");
				codeWord = disposalObject.getTransactionStatus();
				if (disposalObject.getTransactionStatus().equals("AMEND")
						|| disposalObject.getTransactionStatus().startsWith("AM")) {
					messageObject_330.setTypeOfOperation("CHNG");
					messageObject_330.setAction(msgObj);
				}
				if (disposalObject.getCancelFlag() == 0) {
					messageObject_330.setTypeOfOperation("CANC");
					messageObject_330.setTypeOfEvent("CONF");
					messageObject_330.setAction("C");
				}
				else if (codeWord.indexOf("NEW") != -1) {
					messageObject_330.setTypeOfOperation("NEWT");
					messageObject_330.setTypeOfEvent("CONF");
				}
				if (codeWord.indexOf("ROL") != -1) {
					messageObject_330.setTypeOfEvent("CHNG");
				}
				else if (codeWord.indexOf("MAT") != -1) {
					messageObject_330.setTypeOfEvent("SETT");
				}

				if (codeWord.indexOf("NEW") != -1)
					messageObject_330.setTypeOfEvent("CONF");
				if (mainAccCustDetails.getF_BICCODE() == null && mainAccCustDetails.getF_BICCODE().equals(EMPTYSTRING)) {
					messageObject_330.setCommonReference(disposalObject.getCurrentDealNumber());
				}else {
					String branchBicCode = branchObj.getF_BICCODE();
					String bicCheck1 = branchBicCode.substring(0, 4) + branchBicCode.substring(6, 8);
					String bicCheck2 = null;
					String bicTwo = null;
					try {
						bicCheck2 = receiever.substring(0, 4);
						bicTwo = receiever.substring(6, 8);
						if (!bicCheck2.equals(EMPTYSTRING) && bicTwo.equals(EMPTYSTRING)) {
							messageObject_330.setCommonReference(disposalObject.getCurrentDealNumber());
						}else {
							bicCheck2 = bicCheck2 + bicTwo;
							String value22C = CommonConstants.EMPTY_STRING;
							String valFormat = CommonConstants.EMPTY_STRING;
							if (bicCheck1.compareTo(bicCheck2) < 0)
								value22C = bicCheck1;
							else
								value22C = bicCheck2;
                            valFormat = disposalObject.getInterestOrExchangeRate().toPlainString();
							if (disposalObject.getInterestOrExchangeRate().compareTo(new BigDecimal(0)) == 0) {
								valFormat = "0000";
								value22C = value22C + valFormat;
							}else {
								valFormat = disposalObject.getInterestOrExchangeRate().toPlainString();
								value22C = value22C + util.nonZeroValues(valFormat);
							}
							if (bicCheck2.compareTo(bicCheck1) < 0)
								value22C += bicCheck1;
							else
								value22C += bicCheck2;
								messageObject_330.setCommonReference(value22C);
						}

					}
					catch (Exception e) {
							messageObject_330.setCommonReference(disposalObject.getCurrentDealNumber());
					logger.error(ExceptionUtil.getExceptionAsString(e));
					}
				}


				messageObject_330.setPartyA(branchObj.getF_BICCODE().substring(0, 8)
						+ branchObj.getF_BICCODE().substring(8, 11));
				messageObject_330.setPartyAOption(msgObj);
				messageObject_330.setPartyB(receiever);
				messageObject_330.setPartyBOption(msgObj);
				messageObject_330.setTermsAndConditions(util.getTermsAndConditionForDeals(disposalObject));
				 //39M fields PaymentClearingCentre
				messageObject_330.setPaymentClearingCentre(!StringUtils.isBlank(disposalObject.getPaymentClearingCentre())?disposalObject.getPaymentClearingCentre():StringUtils.EMPTY );
				String codeWord = disposalObject.getTransactionStatus();

				if (disposalObject.getPayReceiveFlag().equalsIgnoreCase(payRecFlag)) {
					messageObject_330.setPartyARole("L");
				}else {
					messageObject_330.setPartyARole("B");
				}				
				messageObject_330.setTradeDate(disposalObject.getPostDate().toString());
				messageObject_330.setValueDate(disposalObject.getValueDate().toString());
				messageObject_330.setPeriodOfNotice(disposalObject.getTerm());
				messageObject_330.setCurrencyBalance(value32B(env));
				messageObject_330.setAmountSettled(value32H(env));
				
				if(!(messageObject_330.getTypeOfEvent().equalsIgnoreCase("CONF"))){
				messageObject_330.setInterestDueDate(disposalObject.getMaturityDate().toString());
				messageObject_330.setCcyAndInterestAmount(value34E(env));
				}
				

				
				messageObject_330.setInterestRate(disposalObject.getInterestOrExchangeRate().toPlainString());

				String str14D = get14DTagString(disposalObject.getMainAccCurrencyCode(), env);

				if (str14D != null){

						messageObject_330.setDayCountFraction(str14D);
				}
				else {

					/*throw new BankFusionException(127,
							new Object[] { "YearDays not found in currency table" }, logger, env);*/
					EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[]{"YearDays not found in currency table"}, new HashMap(), env);
				}

    			boolean interDetailsExistFlag = util.intermedaitoryDetailsExists(disposalObject);

				boolean accountwithExistFlag = util.accountWithDetailsExists(disposalObject);

				String ContraBicCode = CommonConstants.EMPTY_STRING;
				String tag53 = CommonConstants.EMPTY_STRING;

				if (contraAccIsNostroAcc) {
					ContraBicCode = contraAccCustDetails.getF_BICCODE();
					tag53 = msgObj;
				}

				messageObject_330.setCDeliveryAgent(ContraBicCode);
				messageObject_330.setCDeliveryAgentOption(tag53);

				String intermediatory56a = EMPTYSTRING;
				String tag56 = EMPTYSTRING;
    			String receiveagent571 = "UNKNOWN";
				String tag571 = EMPTYSTRING;
				if (interDetailsExistFlag) {
					String tempString = util.createSwiftTagString(payToBICCode,
							disposalObject.getSI_PayToPartyIdentifier(),
							disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3(),disposalObject.getSI_PayToText4());
					if (!tempString.equals(EMPTYSTRING)) {
						intermediatory56a = tempString.substring(0, tempString.length() - 1);
						tag56 = tempString.substring(tempString.length() - 1);
					}

					String tempString1 = util.createSwiftTagString(intermediaryBICCode, disposalObject
							.getSI_IntermediaryPartyIdentifier(),
							disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
							disposalObject.getSI_IntermediatoryText3(),disposalObject.getSI_IntermediatoryText4());
					if (!tempString1.equals(EMPTYSTRING)) {
						receiveagent571 = tempString1.substring(0, tempString1.length() - 1);
						tag571 = tempString1.substring(tempString1.length() - 1);
					}

				}else {
					String tempString = util.createSwiftTagString(payToBICCode,
							disposalObject.getSI_PayToPartyIdentifier(),
							disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3(),disposalObject.getSI_PayToText4());
					if (!tempString.equals(EMPTYSTRING)) {
						receiveagent571 = tempString.substring(0, tempString.length() - 1);
					}
					if (receiveagent571.equalsIgnoreCase("$$$"))
						receiveagent571 = "UNKNOWN";
					if (!tempString.equals(EMPTYSTRING)) {
						tag571 = tempString.substring(tempString.length() - 1);
					}
				}

				messageObject_330.setCIntermediary(intermediatory56a);
				messageObject_330.setCIntermediaryOption(tag56);
				messageObject_330.setCReceivingAgent(receiveagent571);
				messageObject_330.setCReceivingAgentOption(tag571);
				
			    if (contraAccIsNostroAcc&&contraAccCustDetails!=null) {
                    if (!contraAccCustDetails.getF_SWTACCOUNTNO().equals(EMPTYSTRING))
                        receiveagent571 = contraAccCustDetails.getF_SWTACCOUNTNO().trim() + SWT_Constants.delimiter
                                + contraAccCustDetails.getF_BICCODE().trim();
                    else
                        receiveagent571 = contraAccCustDetails.getF_BICCODE().trim();
                       tag571 = "A";
                    
                }
                else if (contraAccDescription != null && !contraAccDescription.equals(CommonConstants.EMPTY_STRING)) {
                    receiveagent571 =  contraAccDescription;
                    tag571 = "D";
                }
                else {
                    receiveagent571 = "UNKNOWN";
                    tag571 = "D";
                }
					//messageObject_330.setDIntermediary(intermediatory56a);
					//messageObject_330.setDIntermediaryOption(tag56);
					messageObject_330.setDReceivingAgent(receiveagent571);
					messageObject_330.setDReceivingAgentOption(tag571);
					xmlTagValueMapList.add(messageObject_330);
					int msgStatus = util.updateFlagValues(env, 330, disposalObject.getDisposalRef());
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
		setF_OUT_generateMT330(Boolean.valueOf(generateMT330));
 
		
	}

	private String get14DTagString(String mainAccCurrencyCode, BankFusionEnvironment env) {
		String tag14 = CommonConstants.EMPTY_STRING;
		try {
			String accMain = disposalObject.getMainAccountNo();
/*
 * 			
 */

            
            Map<String, String> inputs = new HashMap<String, String>();
            inputs.put("AccountId", accMain);
            HashMap outputParams = MFExecuter.executeMF("UB_CHG_FindAccountDetails_SRV", com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal.getBankFusionEnvironment(),inputs);
			
			IPersistenceObjectsFactory factory = env.getFactory();
            IBOProductInheritance prodIn = (IBOProductInheritance) factory.findByPrimaryKey(IBOProductInheritance.BONAME,
                    outputParams.get("PRODUCTCONTEXTCODE").toString(), false);
			if (prodIn.getF_CRINT_INTERESTBASEDAYSCR() != 0 && prodIn.getF_DRINT_INTERESTBASEDAYSDR() != 0) {
				if (disposalObject.getSI_PayReceiveFlag().equalsIgnoreCase(InterBase)) {
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

			value32H = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(), util
					.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
			value32H = disposalObject.getMainAccCurrencyCode() + value32H;
			if (codeWord.toUpperCase().indexOf("DEINC") != -1 || codeWord.toUpperCase().indexOf("LODEC") != -1
					|| codeWord.toUpperCase().indexOf("MATLON") != -1) {
				value32H = NEGATIVE + value32H;
			}
		}
		return value32H;
	}

	private String value34E(BankFusionEnvironment env) {
		
		String value34E = util.DecimalRounding(disposalObject.getInterestAmount().abs().toString(), util
				.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
		value34E = disposalObject.getMainAccCurrencyCode() + value34E;
		if (disposalObject.getPayReceiveFlag().compareTo(payRecFlag) == 0)
			value34E = NEGATIVE + value34E;
		return value34E;
	}
	

	


}
