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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT350;
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
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT350Validate;
import com.trapedza.bankfusion.steps.refimpl.ISWT_MT350Validate;

/**
 * @author hardikp
 * 
 */
public class SWT_MT350ValidateFatom extends AbstractSWT_MT350Validate implements ISWT_MT350Validate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(SWT_MT350ValidateFatom.class.getName());
	private final static String NEW = "NEW";

	private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;
	private boolean generateMT202 = false;
	/**
	 * flag to decide whether contra acc is nostro acc
	 */
	private boolean contraAccIsNostroAcc = false;

	/**
	 * flag to generate MT210 message
	 */
	private boolean generateMT350 = false;

	/**
	 * flag to generate any message or not
	 */
	private boolean generateAnyMessage = true;
	private SWT_Util util=new SWT_Util();
	/**
	 * Desposal object
	 */
	private SWT_DisposalObject disposalObject = null;

	/**
	 * HashMap with keys as XML tag name and values as tag values for xml
	 * generation
	 */
	private HashMap xmlTagValueMap = null;
	private final static String DEFULTRECIEVER = "UNKNOWN";
	/**
	 * ArrayList for storing xmlTagValue HashMap
	 */
	private ArrayList xmlTagValueMapList = null;

	public SWT_MT350ValidateFatom(BankFusionEnvironment env) {
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
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT350Validate#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {

		init();

		IBOSwtCustomerDetail contraAccCustDetails = null;
		IBOSwtCustomerDetail mainAccCustDetails = null;
		UB_MT350 messageObject_350 = new UB_MT350();
		if (disposalObject != null) {

			Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
			boolean generate350 = SWT_DataCalculation.generateCategory2Message(((SWT_DisposalObject) disposalObject)
					.getValueDate(), ((SWT_DisposalObject) disposalObject).getPostDate(), env,
					((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(), new java.sql.Date(
							bankFusionSystemDate.getTime()), "350");
			if (generate350) {

				if (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(3));
					int cancelStatus = util.updateCancelFlag(env, 350, disposalObject.getDisposalRef());
					setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

				}
				else if (disposalObject.getConfirmationFlag() == 0) {
					setF_OUT_updatedFlag(new Integer(1));
					setF_OUT_cancelFlagStatus(new Integer(9));
				}
				setF_OUT_disposalId(disposalObject.getDisposalRef());
				contraAccIsNostroAcc=util.isSwiftNostro(disposalObject.getContraAccountNo(),env);
				try {
					// contra account customer details
					contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
							IBOSwtCustomerDetail.BONAME, disposalObject.getContraAccCustomerNumber());
					// main account customer details
					mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
							IBOSwtCustomerDetail.BONAME, disposalObject.getMainAccCustomerNumber());
				}
				catch (BankFusionException bfe) {
					logger.error("Error while getting CotraAccCustDetails OR Main Acc CUsto Details", bfe);
				}

				String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
				String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
				String accWithBICCode = util.verifyForNull(disposalObject.getSI_AccWithCode());

				// which messages to be generated...
				generateMT350 = true;

				messageObject_350.setMessageType("MT350");
				messageObject_350.setDisposalRef(disposalObject.getDisposalRef());

                IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
				messageObject_350.setSender(branchObj.getF_BICCODE());
				String receiever = mainAccCustDetails.getF_BICCODE();
				messageObject_350.setReceiver(receiever);
				messageObject_350.setSendersReference(disposalObject.getCurrentDealNumber());
				if (!disposalObject.getTransactionStatus().equalsIgnoreCase(NEW))
					messageObject_350.setRelatedReference(disposalObject.getCurrentDealNumber());
					messageObject_350.setTypeOfOperation("ADVC");
				if (mainAccCustDetails.getF_BICCODE() == null && mainAccCustDetails.getF_BICCODE().equals(EMPTYSTRING)) {
									messageObject_350.setCommonReference(disposalObject.getCurrentDealNumber());
				}else {
					String branchBicCode = branchObj.getF_BICCODE();

					String bicCheck1 = branchBicCode.substring(0, 4) + branchBicCode.substring(6, 8);
					String bicCheck2 = null;
					String bicTwo = null;
					try {
						bicCheck2 = receiever.substring(0, 4);
						bicTwo = receiever.substring(6, 8);
						if (!bicCheck2.equals(EMPTYSTRING) && bicTwo.equals(EMPTYSTRING)) {
							messageObject_350.setCommonReference(disposalObject.getCurrentDealNumber());
						}else {
							bicCheck2 = bicCheck2 + bicTwo;
							String value22C = CommonConstants.EMPTY_STRING;
							String valFormat = CommonConstants.EMPTY_STRING;
							if (bicCheck1.compareTo(bicCheck2) < 0)
								value22C = bicCheck1;
							else
								value22C = bicCheck2;
							if (disposalObject.getInterestOrExchangeRate().compareTo(new BigDecimal(0)) == 0) {
								valFormat = disposalObject.getCurrentDealNumber().trim();
								value22C = value22C + valFormat;
							}else {
								valFormat = disposalObject.getInterestOrExchangeRate().toString();
								value22C = value22C + util.nonZeroValues(valFormat);
							}
							if (bicCheck2.compareTo(bicCheck1) < 0)
								value22C += bicCheck1;
							else
								value22C += bicCheck2;
								messageObject_350.setCommonReference(value22C);
						}

					}
					catch (Exception e) {
						
						messageObject_350.setCommonReference(disposalObject.getCurrentDealNumber());
					}
				}
				messageObject_350.setPartyA(branchObj.getF_BICCODE());
				messageObject_350.setPartyAOption("A");
				if (util.forAccountDetailsExist(disposalObject)) {
					String partyB87 = util.getForAccountInfoString(disposalObject, false);
					String tag87 = "A";
					messageObject_350.setPartyB(partyB87);
					messageObject_350.setPartyBOption(tag87);
				}else {
					if (util.accountWithDetailsExists(disposalObject)) {
						String tempString = util.createSwiftTagString(accWithBICCode, disposalObject
								.getSI_AccWithAccInfo(), disposalObject.getSI_AccWithNAT_CLR_Code(), disposalObject
								.getSI_AccWithText1(), disposalObject.getSI_AccWithText2(), disposalObject
								.getSI_AccWithText3());
						if (!tempString.equals(EMPTYSTRING)) {
							String partyB87 = tempString.substring(0, tempString.length() - 1);
							String tag87 = tempString.substring(tempString.length() - 1);
							
							messageObject_350.setPartyB(partyB87);
						
							messageObject_350.setPartyBOption(tag87);
						}
					}else {
						if (util.payToDetailsExists(disposalObject)) {
							String tempString = util.createSwiftTagString(payToBICCode, disposalObject
									.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject
									.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject
									.getSI_PayToText3());
							if (!tempString.equals(EMPTYSTRING)) {
								String partyB87 = tempString.substring(0, tempString.length() - 1);
								String tag87 = tempString.substring(tempString.length() - 1);
								messageObject_350.setPartyB(receiever);
								messageObject_350.setPartyBOption("A");
							}
						}
					}
				}
				messageObject_350.setPartyB(receiever);
				messageObject_350.setPartyBOption("A");
				messageObject_350.setSenderToReceiverInfo(util.getBankToBankInfo(disposalObject));
				messageObject_350.setInterestPeriod(disposalObject
						.getInterestPeriodStartDate().toString()
						+ "/" + disposalObject.getInterestPeriodEndDate().toString());
				messageObject_350.setCcyPrincipalAmount(disposalObject.getContraAccCurrencyCode()
						+ util.DecimalRounding(disposalObject.getContractAmount().abs().toString(), util
								.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env)));
				messageObject_350.setValueDate(disposalObject.getValueDate().toString());
				messageObject_350.setCcyInterestAmount(disposalObject.getMainAccCurrencyCode()
						+ util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(), util
								.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env)));
				messageObject_350.setInterestRate(disposalObject.getInterestOrExchangeRate().toString());
				String str14D = get14DTagString(disposalObject.getMainAccCurrencyCode(), env);
				if (str14D != null)
					messageObject_350.setDayCountFraction(str14D);
				else {
					/*throw new BankFusionException(127,
							new Object[] { "YearDays not found in currency table" }, logger, env);*/
					EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { "YearDays not found in currency table" }, new HashMap(), env);
				}
				boolean interDetailsExistFlag = util.intermedaitoryDetailsExists(disposalObject);
				String devagent = CommonConstants.EMPTY_STRING;
				String tag53 = CommonConstants.EMPTY_STRING;
				if (contraAccIsNostroAcc) {
					devagent = contraAccCustDetails.getF_BICCODE();
					tag53 = "A";
				}
				messageObject_350.setDeliveryAgent(devagent);
				messageObject_350.setDeliveryAgentOption("A");
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
				messageObject_350.setInterMediary(intermediatory56a);
				messageObject_350.setInterMediaryOption(tag56);
				messageObject_350.setReceivingAgent(receiveagent571);
				messageObject_350.setReceivingAgentOption(tag571);
				if (disposalObject.getPaymentFlagMT202() == 0 || disposalObject.getPaymentFlagMT202() == 2)
					generateMT202 = true;
				xmlTagValueMapList.add(messageObject_350);
				int msgStatus = util.updateFlagValues(env, 350, disposalObject.getDisposalRef());
				setF_OUT_msgStatusFlag(new Integer(msgStatus));
			}else {
				generateAnyMessage = false;
			}
		}else {
			setF_OUT_disposalId("0");
			generateAnyMessage = false;
		}
		setF_OUT_generateAnyMessage(Boolean.valueOf(generateAnyMessage));
		setF_OUT_XMLTAGVALUEMAPLIST(xmlTagValueMapList);
		setF_OUT_generateMT350Msg(Boolean.valueOf(generateMT350));
		setF_OUT_generate202(Boolean.valueOf(generateMT202));
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
			if (prodIn.getF_CRINT_INTERESTBASEDAYSCR() != 0 && prodIn.getF_DRINT_INTERESTBASEDAYSDR() != 0) {
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
			return null;
		}
	}

}
