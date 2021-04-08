/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_MT202;
import com.misys.ub.swift.UB_SWT_DisposalObject;
import com.misys.ub.swift.UB_SWT_Util;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MT202Populate;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_MT202Populate;

import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;

/**
 * @author Bhavya Gupta
 * 
 */
public class UB_SWT_MT202Populate extends AbstractUB_SWT_MT202Populate implements IUB_SWT_MT202Populate {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(UB_SWT_MT202Populate.class.getName());

    private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;

    private final static String MT202STR = "202";

    private final static String MT300STR = "300";

    private final static String MT320STR = "320";

    private final static String MT330STR = "330";

    private final static String MT350STR = "350";

    /**
     * flag to generate MT103 message
     */
    private boolean generateMT103;

    /**
     * flag to generate MT202 message
     */
    private boolean generateMT202;

    /**
     * flag to generate MT292 message
     */
    private boolean generateMT292;

    /**
     * flag to generate any message or not
     */

    private boolean generateAnyMessage = true;

    /**
     * Disposal object
     */
    private UB_SWT_DisposalObject disposalObject;

    /**
     * flag to mark message as 'COVER' for SWIFT 2009
     */
    private boolean isOriginatedFromMT103OrMT103Plus;

    private String messageType;

    private UB_SWT_Util util = new UB_SWT_Util();
    ArrayList xmlTagValueList = new ArrayList();

    /**
     * Default constructor for the class.
     * 
     * @param env
     */
    public UB_SWT_MT202Populate(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * to set the flag values and populate the xmlTagValueMap
     * 
     */
    private void init() {
        disposalObject = (UB_SWT_DisposalObject) getF_IN_DisposalObject();
        generateMT202 = this.isF_IN_generateMT202().booleanValue();
        generateMT292 = this.isF_IN_generateMT292().booleanValue();
        generateMT103 = this.isF_IN_generateMT103().booleanValue();
        isOriginatedFromMT103OrMT103Plus = this.isF_IN_isOriginatedFromMT103_MT103Plus();

    }

    public void process(BankFusionEnvironment env) {

        init();

        String configLocation = null;
        IBOSwtCustomerDetail contraAccCustDetails = null;
        IBOSwtCustomerDetail mainAccCustDetails = null;
        IBOSwtCustomerDetail clientCustDetails = null;
        IBOBicCodes receiverBicCodeDetails = null;
        UB_MT202 messageObject_202 = new UB_MT202();
        Object object = new Object();
        String messageXmlString = null;
        String sender = CommonConstants.EMPTY_STRING;
        try {
            try {
                IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
                sender = branchObj.getF_BICCODE();
                messageObject_202.setSender(sender);
                contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                        disposalObject.getContraAccCustomerNumber());
                // main account customer details
                mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                        disposalObject.getMainAccCustomerNumber());
            }
            catch (Exception e) {
                logger.error("Error while getting  SettlementDetails OR COntraAccCustDetails OR Main Acc CUsto Details", e);
                generateAnyMessage = false;

                /*
                 * throw new BankFusionException(7400, new Object[] { e .getLocalizedMessage() },
                 * logger, env);
                 */
                EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_IN_SETTLEMT_OR_CONTRA_OR_MAIN_ACCT_DET, null,
                        new Object[] { e.getLocalizedMessage() }, new HashMap(), env);
            }
            String receiever = getReceiver(mainAccCustDetails, contraAccCustDetails);
            if (disposalObject.getDealOriginator().equals("I")) {
                byte messagexmlbytes[] = disposalObject.getMessageXML();
                messageXmlString = new String(messagexmlbytes);
                logger.info("messageXmlString" + messageXmlString);
                Mapping mapping = new Mapping(getClass().getClassLoader());
                // configLocation = System.getProperty("BFconfigLocation",
                // CommonConstants.EMPTY_STRING);
                configLocation = GetUBConfigLocation.getUBConfigLocation();
                mapping.loadMapping(configLocation + "conf/swift/" + "SwiftMessageMapping.xml");
                Unmarshaller unmarshaller = new Unmarshaller();
                unmarshaller.setMapping(mapping);
                object = unmarshaller.unmarshal(new InputSource(new StringReader(messageXmlString)));

                // if((messageXmlString!=null)&&!(messageXmlString.equals(""))){
                if (object instanceof UB_MT202) {
                    UB_MT202 object_202 = (UB_MT202) object;
                    // object_202.setReceiver(receiever);
                    object_202.setSender(sender);
                    object_202.setMessageType("MT202");
                    object_202.setEnd2EndTxnRef(disposalObject.getEnd2EndTxnRef());
                    object_202.setServiceTypeId(disposalObject.getServiceTypeId());
                    xmlTagValueList.add(object_202);
                    setF_OUT_xmlTagValueList(xmlTagValueList);
                    setF_OUT_ispublish(util.IsPublish(disposalObject.getMessageType(), disposalObject.getConfirmationFlag(),
                            disposalObject.getCancelFlag()));

                }
                else if (object instanceof UB_MT103) {

                    UB_MT103 messageObject_103 = (UB_MT103) object;
                    messageObject_202.setTransactionReferenceNumber(messageObject_103.getSendersReference());
                    messageObject_202.setRelatedReference(messageObject_103.getSendersReference());
                    messageObject_202.setOrderingInstitution(messageObject_103.getOrderingInstitution());
                    messageObject_202.setSendersCorrespondent(messageObject_103.getSendersCorrespondent());
                    messageObject_202.setReceiversCorrespondent(messageObject_103.getReceiversCorrespondent());
                    messageObject_202.setIntermediary(messageObject_103.getIntermediaryInstitution());
                    messageObject_202.setCoverMessage("COV");
                    messageObject_202.setEnd2EndTxnRef(messageObject_103.getEndtoendTxnRef());
                    messageObject_202.setServiceTypeId(messageObject_103.getServiceTypeId());
                    messageObject_202.setAccountWithInstitution(messageObject_103.getAccountWithInstitution());
                    messageObject_202.setSendertoReceiverInformation(messageObject_103.getSenderToReceiverInfo());
                    messageObject_202.setTdAmount(messageObject_103.getTdAmount());
                    messageObject_202.setTdCurrencyCode(messageObject_103.getTdCurrencyCode());
                    messageObject_202.setTdValueDate(messageObject_103.getTdValueDate());
                    messageObject_202.setMessageType("MT202");
                    messageObject_202.setReceiver(receiever);
                    messageObject_202.setSender(sender);
                    messageObject_202.setBeneficiary(disposalObject.getSI_AccWithText1() + "$"
                            + disposalObject.getSI_AccWithText2() + "$" + disposalObject.getSI_AccWithText3() + "$"
                            + disposalObject.getSI_AccWithText4());
                    messageObject_202.setBeneficiaryOption("A");
                    xmlTagValueList.add(messageObject_202);
                    setF_OUT_xmlTagValueList(xmlTagValueList);
                    setF_OUT_ispublish(util.IsPublish(disposalObject.getMessageType(), disposalObject.getConfirmationFlag(),
                            disposalObject.getCancelFlag()));
                }
            }

            else if (disposalObject != null) {
                Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
                boolean generate202 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject).getValueDate(),
                        ((UB_SWT_DisposalObject) disposalObject).getPostDate(), env,
                        ((UB_SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
                        new java.sql.Date(bankFusionSystemDate.getTime()), "202");
                if (generate202) {
                    // String receiever;
                    boolean isBicAuthorised;
                    try {
                        // Fx Customer Details
                        clientCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                                disposalObject.getClientNumber());

                        receiever = getReceiver(mainAccCustDetails, contraAccCustDetails);
                        receiverBicCodeDetails = (IBOBicCodes) env.getFactory().findByPrimaryKey(IBOBicCodes.BONAME, receiever);
                        isBicAuthorised = receiverBicCodeDetails.isF_BKEAUTH();
                    }
                    catch (Exception e) {
                        logger.error("Error while getting  SettlementDetails OR COntraAccCustDetails OR Main Acc CUsto Details", e);
                        generateAnyMessage = false;
                        throw new BankFusionException(ChannelsEventCodes.E_ERROR_IN_SETTLEMT_OR_CONTRA_OR_MAIN_ACCT_DET,
                                new Object[] { e.getLocalizedMessage() }, logger, env);
                        /*
                         * EventsHelper.handleEvent(ChannelsEventCodes.
                         * E_ERROR_IN_SETTLEMT_OR_CONTRA_OR_MAIN_ACCT_DET, new Object[] { e
                         * .getLocalizedMessage() }, new HashMap(), env);
                         */
                    }

                    if (isBicAuthorised) {
                        String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
                        String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
                        String accWithBICCode = util.verifyForNull(disposalObject.getSI_AccWithCode());
                        
                        BigDecimal finalSenderCharges = BigDecimal.ZERO;
						BigDecimal finaltdAmount = BigDecimal.ZERO;
						BigDecimal finalReceiverCharges = BigDecimal.ZERO;

                        messageType = disposalObject.getMessageType();

                        if (generateMT202 || generateMT292) {
                            // setting up fatom output values if MR202 OR MT292
                            // generation
                            if (generateMT202)
                                messageObject_202.setMessageType("MT202");
                            else messageObject_202.setMessageType("MT202");

                            messageObject_202.setDisposalRef(disposalObject.getDisposalRef());

                            messageObject_202.setReceiver(receiever);

                            messageObject_202.setTransactionReferenceNumber(disposalObject.getCurrentDealNumber());
                            String Rate = disposalObject.getInterestOrExchangeRate().toString();
                            if (disposalObject.getInterestOrExchangeRate().compareTo(BigDecimal.ZERO) == 0) {
                                Rate = "0000";
                            }
                            String relatedDealNumber = relatedDealNumber(sender, clientCustDetails.getF_BICCODE(),
                                    mainAccCustDetails.getF_BICCODE(), Rate, disposalObject.getCurrentDealNumber());

                            messageObject_202.setRelatedReference(relatedDealNumber);

                            // if (disposalObject.getTransactionStatus()
                            // .indexOf("ROL") != -1)
                            // messageObject_202.setTdValueDate(disposalObject
                            // .getPostDate().toString());
                            // else
                            messageObject_202.setTdValueDate(disposalObject.getValueDate().toString());

                            if (!(disposalObject.getDealOriginator().equals("7"))) {
                                messageObject_202.setTdCurrencyCode(disposalObject.getMainAccCurrencyCode());
                            }
                            else {
                                messageObject_202.setTdCurrencyCode(disposalObject.getContraAccCurrencyCode());
                            }

                            if ((disposalObject.getDealOriginator().trim().compareTo("F") == 0 || disposalObject
                                    .getContraAccCurrencyCode().compareTo(disposalObject.getMainAccCurrencyCode()) != 0)
                                    && !(disposalObject.getDealOriginator().equals("7"))) {
                                String amount = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                                        util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));

                                messageObject_202.setTdAmount(amount);
                            }
                            else if (disposalObject.getDealOriginator().equals("1")
                                    || disposalObject.getDealOriginator().equals("2")
                                    || disposalObject.getDealOriginator().equals("3")
                                    || disposalObject.getDealOriginator().equals("7")) {
                            	

                            	if((disposalObject.getSI_SendersCharges().abs()
        								.compareTo(BigDecimal.ZERO) > 0) && (disposalObject.getSI_ChargeCode().equalsIgnoreCase("BEN")))
                            	{
                            		if (!(disposalObject
											.getMainAccCurrencyCode()
											.equals(disposalObject
													.getContraAccCurrencyCode()))) {
										finalSenderCharges = calculateExchRateAmt(
												disposalObject
														.getMainAccCurrencyCode(),
												disposalObject
														.getContraAccCurrencyCode(),
												disposalObject
														.getExchangeRate(),
												disposalObject
														.getSI_SendersCharges());
										finaltdAmount = disposalObject
												.getTransactionAmount()
												.subtract(finalSenderCharges);
										messageObject_202
										.setTdAmount(util
												.DecimalRounding(
														finaltdAmount
																.abs()
																.toString(),
														util.noDecimalPlaces(
																disposalObject
																		.getContraAccCurrencyCode(),
																env)));
                            		}else{
                            			finaltdAmount = disposalObject
        										.getTransactionAmount().subtract(
        												disposalObject.getSI_SendersCharges());
                            			messageObject_202
        										.setTdAmount(util.DecimalRounding(
        												finaltdAmount.abs().toString(),
        												util.noDecimalPlaces(
        														disposalObject
        																.getContraAccCurrencyCode(),
        														env)));
                            		}
                            	}
                            	
                            	else if (disposalObject.getSI_ChargeCode().equals("OUR"))
   							 {
   								 	 
   									 finalReceiverCharges=disposalObject.getReceiverChargeAmount();
   									 finaltdAmount= disposalObject.getTransactionAmount().add(finalReceiverCharges);			 
   						messageObject_202
   									.setTdAmount(util.DecimalRounding(
   											finaltdAmount.abs().toString(),
   											util.noDecimalPlaces(disposalObject.getContraAccCurrencyCode(),env)));
   							
   							 }
                            	else{

                                String amount = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(),
                                        util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));

                                messageObject_202.setTdAmount(amount);
                            }
                            }
                            else {

                                String amount = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(),
                                        util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
                                messageObject_202.setTdAmount(amount);
                            }
                            if ((disposalObject.getDealOriginator().equals("8"))) {
                                String amount = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                                        util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
                                messageObject_202.setTdAmount(amount);
                            }
                            String orderInstitute52 = CommonConstants.EMPTY_STRING;
                            String tag52 = CommonConstants.EMPTY_STRING;
                            if (util.orderingInstituteDetailsExists(disposalObject)) {
                                String tempString = util.createSwiftTagString(disposalObject.getSI_OrdInstBICCode(),
                                        disposalObject.getSI_OrdInstAccInfo(), get35CharText(disposalObject.getSI_OrdInstText1()), 
                                        get35CharText(disposalObject.getSI_OrdInstText2()), get35CharText(disposalObject.getSI_OrdInstText3()),
                                        get35CharText(disposalObject.getSI_OrdInstText4()));// Added new field for
                                // Reorganization of Settlement
                                // Instruction.
                                //using get35CharText to get 35 characters in case of MT202 with MT300 in case of J option, where fields can be of 40 chars.
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    orderInstitute52 = tempString.substring(0, tempString.length() - 1);
                                    tag52 = tempString.substring(tempString.length() - 1);
                                }

                            }
                            messageObject_202.setOrderingInstitution(orderInstitute52);
                            messageObject_202.setOrderingInstitutionOption(tag52);
                            
                            String sendercorrespondingOption = CommonConstants.EMPTY_STRING;
                            String senderssCorrspondent53 = CommonConstants.EMPTY_STRING;
                            if (disposalObject.getDealOriginator().equalsIgnoreCase("F")
                                    || disposalObject.getDealOriginator().equalsIgnoreCase("7")) {
                                if (mainAccCustDetails.getF_SWTACCOUNTNO() != null) {
                                    senderssCorrspondent53 = mainAccCustDetails.getF_SWTACCOUNTNO();
                                    sendercorrespondingOption = "B";
                                }
                            }
                            else {
                                if (contraAccCustDetails.getF_SWTACCOUNTNO() != null) {
                                    senderssCorrspondent53 = contraAccCustDetails.getF_SWTACCOUNTNO();
                                    sendercorrespondingOption = "B";
                                }

                            }
                            messageObject_202.setSendersCorrespondent(senderssCorrspondent53);
                            messageObject_202.setSendersCorrespondentOption(sendercorrespondingOption);
                            boolean interDetailsExistFlag = util.intermedaitoryDetailsExists(disposalObject);
                            boolean payToDetailsExistFlag = util.payToDetailsExists(disposalObject);
                            String string57 = CommonConstants.EMPTY_STRING;
                            String tag57 = CommonConstants.EMPTY_STRING;
                            String string56 = CommonConstants.EMPTY_STRING;
                            String tag56 = CommonConstants.EMPTY_STRING;
                            String string58 = CommonConstants.EMPTY_STRING;
                            String tag58 = CommonConstants.EMPTY_STRING;
                            if (!generateMT103
                                    || disposalObject.getMessageType().equals(MT202STR)
                                    || (disposalObject.getMessageType().equals(MT300STR)
                                            || disposalObject.getMessageType().equals(MT320STR)
                                            || disposalObject.getMessageType().equals(MT330STR) || disposalObject.getMessageType()
                                            .equals(MT350STR))) {
                                if ((interDetailsExistFlag && !payToBICCode.equals(mainAccCustDetails.getF_BICCODE()) && disposalObject.getDealOriginator().equals("F"))
                                        || (interDetailsExistFlag && !payToBICCode.equals(contraAccCustDetails.getF_BICCODE()))) {

                                    String tempString = util.createSwiftTagString(
                                            payToBICCode,
                                            disposalObject.getSI_PayToPartyIdentifier(),
                                            // Changed to PayToPartyIdentifier inplace of
                                            // PayToAccInfo and
                                            // PayToNat_Clr_Code.
                                            get35CharText(disposalObject.getSI_PayToText1()), get35CharText(disposalObject.getSI_PayToText2()),
                                            get35CharText(disposalObject.getSI_PayToText3()), get35CharText(disposalObject.getSI_PayToText4()));
                                    // Add new Field PayToText4 for Reorganization
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                        string56 = tempString.substring(0, tempString.length() - 1);
                                        tag56 = tempString.substring(tempString.length() - 1);

                                    }
                                }
                                if ((!interDetailsExistFlag && payToDetailsExistFlag && !payToBICCode.equals(mainAccCustDetails
                                        .getF_BICCODE()))
                                        || ((!interDetailsExistFlag && payToDetailsExistFlag && !payToBICCode
                                                .equals(contraAccCustDetails.getF_BICCODE())))) {
                                    String tempString = util.createSwiftTagString(
                                            payToBICCode,
                                            disposalObject.getSI_PayToPartyIdentifier(),
                                            // Changed to PayToPartyIdentifier inplace of
                                            // PayToAccInfo and
                                            // PayToNat_Clr_Code.
                                            get35CharText(disposalObject.getSI_PayToText1()), get35CharText(disposalObject.getSI_PayToText2()),
                                            get35CharText(disposalObject.getSI_PayToText3()), get35CharText(disposalObject.getSI_PayToText4()));
                                    // Added new Field PayToText4 for Reorganization of Settlement
                                    // Instruction.
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                        string57 = tempString.substring(0, tempString.length() - 1);
                                        tag57 = tempString.substring(tempString.length() - 1);

                                    }

                                }
                                else if (interDetailsExistFlag) {
                                    String tempString = util.createSwiftTagString(intermediaryBICCode,
                                            disposalObject.getSI_IntermediaryPartyIdentifier(),
                                            // Changed to IntermediaryPartyIdentifier inplace of
                                            // IntermediaryAccInfo and
                                            // IntermediaryNatClrCode
                                            get35CharText(disposalObject.getSI_IntermediatoryText1()), get35CharText(disposalObject.getSI_IntermediatoryText2()),
                                            /*
                                             * Added field for Reorganization of Settlement
                                             * Instruction.
                                             */
                                            get35CharText(disposalObject.getSI_IntermediatoryText3()), get35CharText(disposalObject.getSI_IntermediatoryText4()));
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                        string57 = tempString.substring(0, tempString.length() - 1);
                                        tag57 = tempString.substring(tempString.length() - 1);

                                    }
                                }
                                /* Added field for Reorganization of Settlement Instruction. */
                                String tempString = util.createSwiftTagString(accWithBICCode,
                                        disposalObject.getSI_AccWithPartyIdentifier(), get35CharText(disposalObject.getSI_AccWithText1()),
                                        get35CharText(disposalObject.getSI_AccWithText2()), get35CharText(disposalObject.getSI_AccWithText3()),
                                        get35CharText(disposalObject.getSI_AccWithText4()));

                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                    string58 = tempString.substring(0, tempString.length() - 1);
                                    tag58 = tempString.substring(tempString.length() - 1);

                                }

                            }

                            if (generateMT103) {
                                String messageGenMatrix = getF_IN_messageGenMatrix();
                                String matrixKeyAccWith = messageGenMatrix.substring(4);
                                String matrixKeyInter = messageGenMatrix.substring(2, 4);
                                if (matrixKeyInter.equalsIgnoreCase("I1")) {
                                    if (matrixKeyAccWith.equalsIgnoreCase("B1") || matrixKeyAccWith.equalsIgnoreCase("B2")) {
                                        if (!payToBICCode.equals(mainAccCustDetails.getF_BICCODE())) {
                                            String tempString = util.createSwiftTagString(
                                                    payToBICCode,
                                                    disposalObject.getSI_PayToPartyIdentifier(),
                                                    // Changed to PayToPartyIdentifier inplace of
                                                    // PayToAccInfo and PayToNat_Clr_Code.
                                                    disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                    disposalObject.getSI_PayToText3(), disposalObject.getSI_PayToText4());
                                            // Added new field PayToText4 for Reorganization of
                                            // Settlement Instruction.
                                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                string57 = tempString.substring(0, tempString.length() - 1);
                                                tag57 = tempString.substring(tempString.length() - 1);

                                            }

                                        }
                                        String tempString = util.createSwiftTagString(accWithBICCode,
                                                disposalObject.getSI_AccWithPartyIdentifier(), disposalObject.getSI_AccWithText1(),
                                                disposalObject.getSI_AccWithText2(), disposalObject.getSI_AccWithText3(),
                                                disposalObject.getSI_AccWithText4());
                                        // Added new field PayToText4 for Reorganization of
                                        // Settlement Instruction.
                                        if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                            string58 = tempString.substring(0, tempString.length() - 1);
                                            tag58 = "A";

                                        }

                                    }
                                    else {
                                        if (matrixKeyAccWith.equalsIgnoreCase("B3")) {
                                            String tempString = util.createSwiftTagString(
                                                    intermediaryBICCode,
                                                    disposalObject.getSI_IntermediaryPartyIdentifier(),
                                                    // Changed to IntermediaryPartyIdentifier
                                                    // inplace of
                                                    // IntermediaryAccInfo and
                                                    // IntermediaryNatClrCode
                                                    /*
                                                     * Added field for Reorganization of Settlement
                                                     * Instruction.
                                                     */
                                                    disposalObject.getSI_IntermediatoryText1(),
                                                    disposalObject.getSI_IntermediatoryText2(),
                                                    disposalObject.getSI_IntermediatoryText3(),
                                                    disposalObject.getSI_IntermediatoryText4());
                                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                string57 = tempString.substring(0, tempString.length() - 1);
                                                tag57 = tempString.substring(tempString.length() - 1);

                                            }

                                            String tempString1 = util.createSwiftTagString(accWithBICCode,
                                                    disposalObject.getSI_AccWithPartyIdentifier(),
                                                    /*
                                                     * Added field for Reorganization of Settlement
                                                     * Instruction.
                                                     */
                                                    disposalObject.getSI_AccWithText1(), disposalObject.getSI_AccWithText2(),
                                                    disposalObject.getSI_AccWithText3(), disposalObject.getSI_AccWithText4());
                                            if (!tempString1.equals(CommonConstants.EMPTY_STRING)) {

                                                string58 = tempString1.substring(0, tempString1.length() - 1);
                                                tag58 = tempString1.substring(tempString1.length() - 1);

                                            }

                                        }
                                        else {
                                            String tempString = util.createSwiftTagString(
                                                    payToBICCode,
                                                    disposalObject.getSI_PayToPartyIdentifier(),
                                                    // Changed PayToPartyIdentifier onbehalf of
                                                    // PayToAccInfo and PayToNat_Clr_Code.
                                                    disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                    disposalObject.getSI_PayToText3(), disposalObject.getSI_PayToText4());
                                            /*
                                             * Added field for Reorganization of Settlement
                                             * Instruction.
                                             */
                                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                string58 = tempString.substring(0, tempString.length() - 1);
                                                tag58 = "A";

                                            }

                                        }
                                    }
                                }
                                else {
                                    if (matrixKeyAccWith.equalsIgnoreCase("B1")) {
                                        if (!payToBICCode.equals(contraAccCustDetails.getF_BICCODE())) {
                                            String tempString = util.createSwiftTagString(
                                                    payToBICCode,
                                                    disposalObject.getSI_PayToPartyIdentifier(),
                                                    // Changed to PayToPartyIdentifier inplace of
                                                    // PayToAccInfo and PayToNat_Clr_Code.
                                                    disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                    disposalObject.getSI_PayToText3(), disposalObject.getSI_PayToText4());
                                            // Added new field for Reorganization of Settlement
                                            // Instruction.
                                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                string56 = tempString.substring(0, tempString.length() - 1);
                                                tag56 = tempString.substring(tempString.length() - 1);

                                            }
                                        }
                                        /* Added field for Reorganization of Settlement Instruction. */
                                        String tempString = util.createSwiftTagString(
                                                intermediaryBICCode,
                                                disposalObject.getSI_IntermediaryPartyIdentifier(),
                                                // Changed to IntermediaryPartyIdentifier inplace of
                                                // IntermediaryAccInfo and IntermediaryNatClrCode,
                                                disposalObject.getSI_IntermediatoryText1(),
                                                disposalObject.getSI_IntermediatoryText2(),
                                                disposalObject.getSI_IntermediatoryText3(),
                                                disposalObject.getSI_IntermediatoryText4());

                                        if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                            string57 = tempString.substring(0, tempString.length() - 1);
                                            tag57 = tempString.substring(tempString.length() - 1);

                                        }
                                        /* Added field for Reorganization of Settlement Instruction. */
                                        String tempString1 = util.createSwiftTagString(accWithBICCode,
                                                disposalObject.getSI_AccWithPartyIdentifier(), disposalObject.getSI_AccWithText1(),
                                                disposalObject.getSI_AccWithText2(), disposalObject.getSI_AccWithText3(),
                                                disposalObject.getSI_AccWithText4());
                                        if (!tempString1.equals(CommonConstants.EMPTY_STRING)) {

                                            string58 = tempString1.substring(0, tempString1.length() - 1);
                                            tag58 = tempString1.substring(tempString1.length() - 1);

                                        }
                                    }
                                    else {
                                        if (matrixKeyInter.equalsIgnoreCase("I2")) {
                                            if (!payToBICCode.equals(contraAccCustDetails.getF_BICCODE())) {
                                                String tempString = util.createSwiftTagString(
                                                        payToBICCode,
                                                        disposalObject.getSI_PayToPartyIdentifier(),
                                                        // Changed to PayToPartyIdentifier inplace
                                                        // of PayToAccInfo and PayToNat_Clr_Code.
                                                        disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                        disposalObject.getSI_PayToText3(), disposalObject.getSI_PayToText4());
                                                // Added New field for Reorganization of Settlement
                                                // Instruction.
                                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                    string57 = tempString.substring(0, tempString.length() - 1);
                                                    tag57 = tempString.substring(tempString.length() - 1);

                                                }

                                            }
                                            /*
                                             * Added field for Reorganization of Settlement
                                             * Instruction.
                                             */
                                            String tempString = util.createSwiftTagString(
                                                    intermediaryBICCode,
                                                    disposalObject.getSI_IntermediaryPartyIdentifier(),
                                                    // Changed to IntermediaryPartyIdentifier
                                                    // inplace of
                                                    // IntermediaryAccInfo and
                                                    // IntermediaryNatClrCode
                                                    disposalObject.getSI_IntermediatoryText1(),
                                                    disposalObject.getSI_IntermediatoryText2(),
                                                    disposalObject.getSI_IntermediatoryText3(),
                                                    disposalObject.getSI_IntermediatoryText4());
                                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                string58 = tempString.substring(0, tempString.length() - 1);
                                                tag58 = "A";

                                            }
                                        }
                                        else {
                                            String tempString = util.createSwiftTagString(
                                                    payToBICCode,
                                                    disposalObject.getSI_PayToPartyIdentifier(),
                                                    // Changed to PayToPartyIdentifier inplace of
                                                    // PayToAccInfo and PayToNat_Clr_Code.
                                                    disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                    disposalObject.getSI_PayToText3(), disposalObject.getSI_PayToText4());
                                            // Added new field for Reorganization of Settlement
                                            // Instruction.
                                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                string58 = tempString.substring(0, tempString.length() - 1);
                                                tag58 = tempString.substring(tempString.length() - 1);

                                            }

                                        }
                                    }
                                }
                            }
                            messageObject_202.setIntermediary(string56);
                            messageObject_202.setIntermediaryOption(tag56);
                            messageObject_202.setAccountWithInstitution(string57);
                            messageObject_202.setAccountWithInstitutionOption(tag57);
                            messageObject_202.setBeneficiary(string58);

                            if (string58.equalsIgnoreCase(CommonConstants.EMPTY_STRING))
                                tag58 = CommonConstants.EMPTY_STRING;
                            messageObject_202.setBeneficiaryOption(tag58);
                            if (generateMT103)
                                messageObject_202.setSendertoReceiverInformation(util.getSenderToReceiverInfo(disposalObject));

                            else messageObject_202.setSendertoReceiverInformation(util.getBankToBankInfo(disposalObject));
                            //Change to generate 292 for 202 Cancellation - Start
                            if("CANCEL".equals(disposalObject.getTransactionStatus()) && !generateMT103){
                            	generateMT292 = true;
                            }
                            //Change to generate 292 for 202 Cancellation - End
                            if (generateMT292 && disposalObject.getCancelFlag() == 0) {

                                messageObject_202.setAction("C");
                                messageObject_202.setServiceTypeId(null);
                                messageObject_202.setEnd2EndTxnRef(null);
                            }
                            else if (disposalObject.getTransactionStatus().startsWith("AM")) {

                                messageObject_202.setAction("A");
                            }
                            logger.info("IN MT202 : after Action : generateMT292  " + generateMT292 + "CancelFlag "
                                    + disposalObject.getCancelFlag());

                            /*
                             * Added for SWIFT 2009 - If Message originated from MT103 or MT103+
                             * mark the message as COVER
                             */
                            if (isOriginatedFromMT103OrMT103Plus) {
                                messageObject_202.setCoverMessage("COV");
                            }
                            //UETR reference
                            if(!"CANCEL".equals(disposalObject.getTransactionStatus())){
                            	 if(StringUtils.isBlank(disposalObject.getEnd2EndTxnRef())){
                                     //generate UETR
                                     messageObject_202.setEnd2EndTxnRef(UUID.randomUUID().toString());
                                 }else {
                                     messageObject_202.setEnd2EndTxnRef(disposalObject.getEnd2EndTxnRef());
                                 }   	
                            }
                             
                            messageObject_202.setServiceTypeId(disposalObject.getServiceTypeId());
                            // setServiceIdentifierId
                            if (messageObject_202.getSender().substring(4, 6)
                                    .equals(messageObject_202.getReceiver().substring(4, 6))) {
                                IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
                                        .getInstance().getServiceManager()
                                        .getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
                                String serviceIdentifierId = ubInformationService.getBizInfo()
                                        .getModuleConfigurationValue("MT202", "SERVICEPROVIDERID", env).toString();
                                if (serviceIdentifierId != null && serviceIdentifierId.trim().length() > 0) {
                                    messageObject_202.setServiceIdentifierId(serviceIdentifierId);
                                }
                            }
                            // for MMM, so that the customer details are mapped to MT202 as part of
                            // FATF regulations.
                            ArrayList xmlTagValueList = new ArrayList();
                            xmlTagValueList.add(messageObject_202);
                            setF_OUT_xmlTagValueList(xmlTagValueList);

                            setF_OUT_ispublish(util.IsPublish(disposalObject.getMessageType(),
                                    disposalObject.getConfirmationFlag(), disposalObject.getCancelFlag()));
                        }
                        else {
                            generateAnyMessage = false;
                        }
                    }
                    else {
                        setF_OUT_msgStatusFlag(new Integer(9));
                    }
                }
                else {
                    generateAnyMessage = false;
                }
            }
            int msgStatus = util.updateFlagValues(env, 202, disposalObject.getDisposalRef());
            setF_OUT_msgStatusFlag(new Integer(msgStatus));
            if (disposalObject.getPaymentFlagMT202() == 0) {
                setF_OUT_updatedFlag(new Integer(1));
                generateMT202 = true;
                setF_OUT_cancelFlagStatus(new Integer(9));
            }
            if (disposalObject.getCancelFlag() == 0 && disposalObject.getPaymentFlagMT202() == 2) {
                generateMT292 = true;
                setF_OUT_updatedFlag(new Integer(3));
                int cancelStatus = util.updateCancelFlag(env, 202292, disposalObject.getDisposalRef());
                setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

            }

            setF_OUT_generateAnyMessage(Boolean.valueOf(generateAnyMessage));
            if (disposalObject != null) {
                setF_OUT_DisposalId(disposalObject.getDisposalRef());
            }
            else {
                setF_OUT_DisposalId("0");
            }

        }
        catch (Exception e1) {
           logger.error(ExceptionUtil.getExceptionAsString(e1));
        }
    }

    /**
     * 
     * @param sender
     * @param clientBICCode
     * @param mainACCBICCode
     * @param contraACCBICCode
     * @param Rate
     * @param dealNumber
     * @return relatedRef
     */
    private String relatedDealNumber(String sender, String clientBICCode, String mainACCBICCode, String Rate, String dealNumber) {

        if (disposalObject.getMessageType().compareTo("103") == 0 || disposalObject.getMessageType().compareTo("202") == 0)
            return disposalObject.getCurrentDealNumber();
        String relatedRef = CommonConstants.EMPTY_STRING;
        String firstRef = CommonConstants.EMPTY_STRING;
        String middleRef = CommonConstants.EMPTY_STRING;
        String lastRef = CommonConstants.EMPTY_STRING;
        if (mainACCBICCode.trim().length() > 0) {
            firstRef = sender.substring(0, 4) + sender.substring(6, 8);
            if (messageType.compareTo("202") == 0
                    || (messageType.compareTo("103") == 0 && disposalObject.getPaymentFlagMT202() == 0)) {
                middleRef = dealNumber.substring(0, 8);
            }
            else if (disposalObject.getPaymentFlagMT202() == 0 || disposalObject.getPaymentFlagMT202() == 2) {
                lastRef = clientBICCode.substring(0, 4) + clientBICCode.substring(6, 8);
            }

            if (lastRef.compareTo(firstRef) < 0)
                relatedRef = lastRef;
            else relatedRef = firstRef;

            middleRef = util.nonZeroValues(Rate);

            if (lastRef.compareTo(firstRef) > 0)
                relatedRef = relatedRef.concat(middleRef).concat(lastRef);
            else relatedRef = relatedRef.concat(middleRef).concat(firstRef);
        }

        return relatedRef;
    }

    
    
    private BigDecimal calculateExchRateAmt(String buyCurrency,
			String sellCurrency, BigDecimal exchangeRate, BigDecimal buyAmount) {

		RqHeader rqHeader = new RqHeader();
		Orig orig = new Orig();
		orig.setChannelId("SWIFT");
		rqHeader.setOrig(orig);
		CalcExchangeRateRq exchRq = new CalcExchangeRateRq();
		CalcExchRateDetails exchangeDtls = new CalcExchRateDetails();
		exchangeDtls.setSellAmount(BigDecimal.ZERO);
		if (buyAmount.signum() < 0) {
			exchangeDtls.setBuyAmount(buyAmount.abs());
		} else {
			exchangeDtls.setBuyAmount(buyAmount);
		}

		exchangeDtls.setBuyCurrency(buyCurrency);
		exchangeDtls.setSellCurrency(sellCurrency);
		exchRq.setCalcExchRateDetails(exchangeDtls);
		ExchangeRateDetails exchangeRateDetails = new ExchangeRateDetails();
		exchangeRateDetails.setExchangeRate(exchangeRate);
		exchangeRateDetails.setExchangeRateType("SPOT");
		exchangeDtls.setExchangeRateDetails(exchangeRateDetails);
		exchRq.setRqHeader(rqHeader);
		BankFusionEnvironment env = new BankFusionEnvironment(
				null);
		HashMap inputMap = new HashMap();
		inputMap.put("CalcExchangeRateRq", exchRq);
		env.setData(new HashMap());
		HashMap outputParams = MFExecuter.executeMF(
				"CB_FEX_CalculateExchangeRateAmount_SRV", env, inputMap);

		CalcExchangeRateRs calcExchangeRateRs = (CalcExchangeRateRs) outputParams
				.get("CalcExchangeRateRs");
		BigDecimal equivalentAmount = calcExchangeRateRs
				.getCalcExchRateResults().getSellAmountDetails().getAmount();
		if (buyAmount.signum() < 0) {
			equivalentAmount = BigDecimal.ZERO.subtract(equivalentAmount);
		}
		return equivalentAmount;
	}
    /**
     * 
     * @param disposalObject
     * @param env
     * @return amount
     */
    /*
     * private String calculateAmount(UB_SWT_DisposalObject disposalObject, BankFusionEnvironment
     * env) {
     * 
     * BigDecimal principalAmount = disposalObject.getContractAmount(); BigDecimal interestAmount =
     * disposalObject.getInterestAmount();
     * 
     * BigDecimal tempAmount = BigDecimal.ZERO;
     * 
     * if (disposalObject.getDealOriginator().equals("4")) { tempAmount = interestAmount;
     * 
     * } else if (disposalObject.getDealOriginator().equals("5")) {
     * 
     * tempAmount = principalAmount;
     * 
     * } else if (disposalObject.getDealOriginator().equals("6")) {
     * 
     * tempAmount = principalAmount.add(interestAmount); }
     * 
     * String amount = util.DecimalRounding(tempAmount.abs().toString(),
     * util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
     * 
     * return amount; }
     */

    /**
     * 
     * @param mainAccCustDetails
     * @param contraAccCustDetails
     * @return receiever
     */
    private String getReceiver(IBOSwtCustomerDetail mainAccCustDetails, IBOSwtCustomerDetail contraAccCustDetails) {
        String receiever = null;
        if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
            if (!(mainAccCustDetails.getF_ALTERNATEBICCODE() == null)
                    && !mainAccCustDetails.getF_ALTERNATEBICCODE().equals(EMPTYSTRING))
                receiever = mainAccCustDetails.getF_ALTERNATEBICCODE();
            else receiever = mainAccCustDetails.getF_BICCODE();
        }
        else {
            if (!(contraAccCustDetails.getF_ALTERNATEBICCODE() == null)
                    && !contraAccCustDetails.getF_ALTERNATEBICCODE().equals(EMPTYSTRING))
                receiever = contraAccCustDetails.getF_ALTERNATEBICCODE();
            else receiever = contraAccCustDetails.getF_BICCODE();
        }
        return receiever;
    }

    /**
     * Method Description:Append the transaction narrative in the Tag61 StatementLine
     * 
     * @param narration
     * @return
     */
    private String get35CharText(String text) {
        if (null != text && !text.isEmpty()) {
            if (text.length() > 35) {
            	text = text.substring(0, 35);
            }
        }
        return text;
    }
}