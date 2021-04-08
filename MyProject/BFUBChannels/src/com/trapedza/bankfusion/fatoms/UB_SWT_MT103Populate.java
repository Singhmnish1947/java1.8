/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.trapedza.bankfusion.fatoms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.InstructionCode;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SendersCharges;
import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_SWT_DisposalObject;
import com.misys.ub.swift.UB_SWT_Util;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MT103Populate;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_MT103Populate;

import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;

/**
 * @author Shaileja
 *
 */
public class UB_SWT_MT103Populate extends AbstractUB_SWT_MT103Populate implements IUB_SWT_MT103Populate {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public BankFusionObject getF_IN_DisposalObject() {
        return super.getF_IN_DisposalObject();
    }

    private transient final static Log logger = LogFactory.getLog(UB_SWT_MT103Populate.class.getName());
    private final static String SWIFT_MESSAGETYPES_FILE_NAME = "SWT_MessageTypes.properties";
    private final static String TagValueA = "A";
    private final static String TagValueK = "K";
    private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;
    private final static int DIGIT_THREE = 3;
    private final static int DIGIT_FIVE = 5;
    private final static char CHAR_DIGIT_ONE = '1';


    /**
     * This map holds PX-IX-BX as keys and values as 1 OR 0 for MT103, MT202, Flag72 and receiver
     * flag
     */
    HashMap messageGenerationMap = new HashMap();

    /**
     * flag to decide whether contra acc is nostro acc
     */
    private boolean contraAccIsNostroAcc;

    /**
     * flag to decide whether contra acc is nostro acc
     */

    /**
     * flag for MT103 generation
     */
    private boolean flagMT103;

    /**
     * flag for MT202 generation
     */
    private boolean flagMT202;

    /**
     * flag to check whether flag72 details should be use for message generation or not
     */
    private boolean flag72;
    /**
     * received flag value if any, should be use for message generation or not
     */
    private String receiverFlagValue;

    /**
     * flag to generate MT103 message
     */
    private boolean generateMT103;

    /**
     * flag to generate MT202 message
     */
    private String generateMT103Plus;

    /**
     * flag to generate MT103+ message
     */
    private boolean generateMT202;

    /**
     * fflag to generate MT192 message
     */
    private boolean generateMT192;

    /**
     * flag to generate MT292 message
     */
    private boolean generateMT292;

    /**
     * flag to generate any message or not
     */
    private boolean generateAnyMessage = true;

    /**
     * flag to generate any message or not
     */
    private boolean generateMT103OrMT192;

    /**
     * flag to generate any message or not
     */
    private boolean generateMT202OrMT292;

    private boolean isBelong;

    /**
     * Desposal object
     */
    private UB_SWT_DisposalObject disposalObject;

    /**
     * HashMap with keys as XML tag name and values as tag values for xml generation
     */

    /**
     * ArrayList for storing xmlTagValue HashMap
     */
    private ArrayList xmlTagValueMapList;
    UB_MT103 messageObject_103 = new UB_MT103();

    public UB_SWT_MT103Populate(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * This method is to initiliaze the frequently used variables @
     */
    private void init() {
        try {
            Object obj = getF_IN_DisposalObject();
            if (obj instanceof UB_SWT_DisposalObject) {
                disposalObject = (UB_SWT_DisposalObject) obj;

            }
            else {
                logger.info(obj.toString());
            }

        }
        catch (Exception e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        xmlTagValueMapList = new ArrayList();
        populateMessageTypesMap();
    }

    public void process(BankFusionEnvironment env) {
        String configLocation = null;
        init();

        IBOSwtCustomerDetail contraAccCustDetails = null;
        IBOSwtCustomerDetail mainAccCustDetails = null;
        UB_SWT_Util util = new UB_SWT_Util();

        String[] D49Countries = { "AD", "AT", "BE", "BG", "BV", "CH", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB", "GF",
                "GI", "GP", "GR", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MQ", "MT", "NL", "NO", "PL", "PM", "PT",
                "RE", "RO", "SE", "SI", "SJ", "SK", "SM", "TF", "VA" };

        String messageXmlString = null;
        IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
        try {
            int msgStatus = util.updateFlagValues(env, 103, disposalObject.getDisposalRef());
            String messagePreference = util.verifyForNull(disposalObject.getMessagePreference());
            if (PaymentSwiftConstants.PAYMENT_PREFERENCE_SERIAL.equalsIgnoreCase(messagePreference)) {
                msgStatus = 2;
            }
            setF_OUT_msgStatusFlag(new Integer(msgStatus));
            String curCodeForInstAmtFormat = StringUtils.isNotBlank(disposalObject.getInstructedAmtCurrency())
                    ? disposalObject.getInstructedAmtCurrency()
                    : disposalObject.getMainAccCurrencyCode();

            if (disposalObject.getDealOriginator().equals("I")) {
                logger.info("INSIDE  incomming if");
                byte messagexmlbytes[] = disposalObject.getMessageXML();
                messageXmlString = new String(messagexmlbytes);
                Mapping mapping = new Mapping(getClass().getClassLoader());
                configLocation = GetUBConfigLocation.getUBConfigLocation();
                mapping.loadMapping(configLocation + "conf/swift/" + "SwiftMessageMapping.xml");
                Unmarshaller unmarshaller = new Unmarshaller();
                unmarshaller.setMapping(mapping);
                messageObject_103 = (UB_MT103) unmarshaller.unmarshal(new InputSource(new StringReader(messageXmlString)));
                if (messageObject_103.getSenderToReceiverInfo().equals("")) {
                    messageObject_103.setSenderToReceiverInfo("/INS/" + messageObject_103.getSender());
                }
                messageObject_103.setSender(branchObj.getF_BICCODE());
                xmlTagValueMapList.add(messageObject_103);
                setF_OUT_XMLTAGVALUEMAPLIST(xmlTagValueMapList);
                if (disposalObject.getPaymentFlagMT202() == 0) {
                    setF_OUT_generateMT202Or292(true);
                    setF_OUT_GEN_MT292(true);
                    setF_OUT_GEN_MT202(true);
                }
                else {
                    setF_OUT_GEN_MT292(false);
                    setF_OUT_MT202Message(new Integer(9));
                    setF_OUT_GEN_MT202(false);
                    setF_OUT_generateMT202Or292(false);
                }
                setF_OUT_generateMT103Or192(true);

                setF_OUT_MESSAGEGEN(true);

                setF_OUT_MT210Generate(1);
                setF_OUT_updatedFlag(new Integer(1));
                setF_OUT_cancelFlagStatus(9);

            }
            else if (disposalObject != null) {
                logger.info("INSIDE  ougoing if");

                if (disposalObject.getConfirmationFlag() == 2 && disposalObject.getCancelFlag() == 0) {
                    setF_OUT_updatedFlag(new Integer(3));
                    int cancelStatus = util.updateCancelFlag(env, 103192, disposalObject.getDisposalRef());
                    setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

                }
                else if (disposalObject.getConfirmationFlag() == 0) {
                    setF_OUT_updatedFlag(new Integer(1));
                    setF_OUT_cancelFlagStatus(new Integer(9));
                }

                // check for contraAcc is nostro acc
                String AccountNo = null;
                String BicCode = null;

                try {
                    // contra account customer details

                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getContraAccCustomerNumber());
                    // main account customer details
                    mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getMainAccCustomerNumber());
                    if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
                        BicCode = mainAccCustDetails.getF_BICCODE();
                        AccountNo = disposalObject.getMainAccountNo();
                    }
                    else {
                        BicCode = contraAccCustDetails.getF_BICCODE();
                        AccountNo = disposalObject.getContraAccountNo();
                    }

                    contraAccIsNostroAcc = util.isSwiftNostro(AccountNo, env);

                }
                catch (BankFusionException bfe) {
                    logger.error("Error while getting  SettlementDetails OR COntraAccCustDetails OR Main Acc CUsto Details", bfe);
                    generateAnyMessage = false;
                }

                String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
                String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
                String accWithBICCode = util.verifyForNull(disposalObject.getSI_AccWithCode());

                logger.info("the contra account is nostro " + contraAccIsNostroAcc);
                // getting message value from propertis file
                String messageGenKey = null;
                try {
                    messageGenKey = getMessageGenMatrixKey(messagePreference, BicCode, payToBICCode, intermediaryBICCode,
                            accWithBICCode, env);
                    logger.info("Generated KEY " + messageGenKey);
                    if (messageGenKey != null && !messageGenKey.equals(EMPTYSTRING)) {
                        String messageGenValue = (String) messageGenerationMap.get(messageGenKey);
                        logger.info("Generated KEY " + messageGenValue);
                        if (messageGenValue != null && !messageGenValue.equals(EMPTYSTRING)) {
                            messageGenValue = messageGenValue.trim();

                            if (messageGenValue.length() == DIGIT_THREE) {
                                if (messageGenValue.charAt(0) == CHAR_DIGIT_ONE && contraAccIsNostroAcc) {
                                    flagMT103 = true;
                                    setF_OUT_MT103Message(new Integer(0));
                                }
                                if (messageGenValue.charAt(1) == CHAR_DIGIT_ONE) {
                                    if (PaymentSwiftConstants.PAYMENT_PREFERENCE_SERIAL.equalsIgnoreCase(messagePreference)) {
                                        flagMT202 = false;
                                        setF_OUT_MT202Message(new Integer(9));
                                    }
                                    else {
                                        flagMT202 = true;
                                        setF_OUT_MT202Message(new Integer(0));
                                    }
                                }
                                else {
                                    msgStatus = 2;
                                    setF_OUT_msgStatusFlag(new Integer(msgStatus));
                                    flagMT202 = false;
                                    setF_OUT_MT202Message(new Integer(9));
                                }
                                if (messageGenValue.charAt(2) == CHAR_DIGIT_ONE)
                                    flag72 = true;
                            }
                            else if (messageGenValue.length() == DIGIT_FIVE) {
                                if (messageGenValue.charAt(0) == CHAR_DIGIT_ONE && contraAccIsNostroAcc) {
                                    flagMT103 = true;
                                    setF_OUT_MT103Message(new Integer(0));
                                }
                                if (messageGenValue.charAt(1) == CHAR_DIGIT_ONE) {
                                    if (PaymentSwiftConstants.PAYMENT_PREFERENCE_SERIAL.equalsIgnoreCase(messagePreference)) {
                                        flagMT202 = false;
                                        setF_OUT_MT202Message(new Integer(9));
                                    }
                                    else {
                                        flagMT202 = true;
                                        setF_OUT_MT202Message(new Integer(0));
                                        
                                    }
                                }
                                else {
                                    msgStatus = 2;
                                    setF_OUT_msgStatusFlag(new Integer(msgStatus));
                                    flagMT202 = false;
                                    setF_OUT_MT202Message(new Integer(9));
                                }
                                if (messageGenValue.charAt(2) == CHAR_DIGIT_ONE)
                                    flag72 = true;
                                receiverFlagValue = messageGenValue.substring(DIGIT_THREE);
                            }
                        }

                    }
                    else {
                        generateAnyMessage = false;
                    }
                }
                catch (BankFusionException e) {
                    generateAnyMessage = false;
                    logger.error("Exception while generating matrix key", e);
                    EventsHelper.handleEvent(CommonsEventCodes.E_THE_INPUT_TAG_HAS_AN_INVALID_VALUE,
                            new Object[] { e.getLocalisedMessage() }, new HashMap(), env);
                }
                // which messages to be generated...
                logger.info("from the matrix 103 needed " + flagMT103);
                logger.info("from the matrix 202 needed " + flagMT202);

                if (flagMT103) {

                    if (disposalObject.getConfirmationFlag() == 0)
                        generateMT103 = true;

                    if (disposalObject.getCancelFlag() == 0 && disposalObject.getConfirmationFlag() == 2)
                        generateMT192 = true;

                    generateMT103OrMT192 = true;
                }

                if (flagMT202) {
                    if (disposalObject.getPaymentFlagMT202() == 0 || flagMT202)
                        generateMT202 = true;
                    // Updated for deal reversal from DIGT_ONE to 2
                    if (disposalObject.getCancelFlag() == 0 && disposalObject.getPaymentFlagMT202() == 2)
                        generateMT292 = true;

                    generateMT202OrMT292 = true;
                }
                Timestamp bankFusionSystemDate2 = SystemInformationManager.getInstance().getBFBusinessDateTime();
                boolean generate103 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject).getValueDate(),
                        ((UB_SWT_DisposalObject) disposalObject).getPostDate(), env,
                        ((UB_SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
                        new java.sql.Date(bankFusionSystemDate2.getTime()), "103");
                generateMT103Plus = disposalObject.getGenerate103Plus();
                if (generate103) {
                    logger.info("The date of generation of 103 Reached ");
                    // setting up fatom output values if MR103 OR MT192
                    // generation

                    String chargeCode = util.verifyForNull(disposalObject.getSI_ChargeCode());
                    if (generateMT103 || generateMT192) {
                        logger.info("passed the flag value ");
                        // message type
                        if ((generateMT103) && (generateMT103Plus.equals("Y"))) {
                            messageObject_103.setStp(CommonConstants.Y);
                        }

                        messageObject_103.setMessageType("MT103");

                        messageObject_103.setDisposalRef(disposalObject.getDisposalRef());
                        // sender bic code
                        messageObject_103.setSender(branchObj.getF_BICCODE());
                        messageObject_103.setTransactionTypeCode(disposalObject.getTransactionCode());

                        String SenderBicCode = branchObj.getF_BICCODE().substring(4, 6);

                        if (Arrays.asList(D49Countries).contains(SenderBicCode)) {
                            isBelong = true;
                        }
                        // receiver
                        String receiever = null;

                        if (receiverFlagValue == null || receiverFlagValue.equals(CommonConstants.EMPTY_STRING)) {
                            if (disposalObject.getDealOriginator().equals("F")) {
                                receiever = mainAccCustDetails.getF_BICCODE();
                            }
                            else {
                                receiever = contraAccCustDetails.getF_BICCODE();
                            }

                        }
                        else if (receiverFlagValue.equals("NO")) {
                            if (!disposalObject.getDealOriginator().equals("F")) {
                                receiever = contraAccCustDetails.getF_BICCODE();
                                if (receiever.trim().equals(CommonConstants.EMPTY_STRING)) {
                                    receiever = contraAccCustDetails.getF_ALTERNATEBICCODE();
                                }
                            }
                            else {
                                receiever = mainAccCustDetails.getF_ALTERNATEBICCODE();
                                if (receiever == null || CommonConstants.EMPTY_STRING.equals(receiever.trim())) {
                                    receiever = mainAccCustDetails.getF_BICCODE();
                                }
                            }
                        }
                        else if (receiverFlagValue.equals("B1"))
                            receiever = disposalObject.getSI_AccWithCode();
                        else if (receiverFlagValue.equals("I2"))
                            receiever = disposalObject.getSI_IntermediatoryCode();
                        else if (receiverFlagValue.equals("P2"))
                            receiever = disposalObject.getSI_PayToBICCode();

                        messageObject_103.setReceiver(receiever);

                        if (isBelong && Arrays.asList(D49Countries).contains(receiever.substring(4, 6))) {
                            isBelong = true;
                        }
                        else {
                            isBelong = false;
                        }
                        messageObject_103.setSendersReference(disposalObject.getCurrentDealNumber());

                        // setServiceIdentifierId

                        if (messageObject_103.getSender().substring(4, 6).equals(messageObject_103.getReceiver().substring(4, 6))) {
                            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
                                    .getInstance().getServiceManager()
                                    .getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
                            String serviceIdentifierId = ubInformationService.getBizInfo()
                                    .getModuleConfigurationValue("MT103", "SERVICEPROVIDERID", env).toString();

                            if (serviceIdentifierId != null && serviceIdentifierId.trim().length() > 0) {

                                messageObject_103.setServiceIdentifierId(serviceIdentifierId);
                            }
                        }

                        // bank op code 23B
                        if (disposalObject.getSI_BankOpCode() == null || disposalObject.getSI_BankOpCode().trim().length() == 0)
                            /*
                             * If Bank Operation Code is blank we are setting the default values as
                             * "CRED"
                             */
                            messageObject_103.setBankOperationCode("CRED");

                        else

                            messageObject_103.setBankOperationCode(disposalObject.getSI_BankOpCode());

                        // instruction code 23E
                        String instructionCode23E = CommonConstants.EMPTY_STRING;
                        // If Bank Operation Code is SPAY/SSTD the 23E will not
                        // come in picture
                        if (!(disposalObject.getSI_BankOpCode().equals("SPAY")
                                || disposalObject.getSI_BankOpCode().equals("SSTD"))) {
                            if (!disposalObject.getSI_BankInstructionCode().equals(CommonConstants.EMPTY_STRING)) {
                                instructionCode23E = disposalObject.getSI_BankInstructionCode();
                            }
                            if (!disposalObject.getSI_BankAddlInstrCode().equals(CommonConstants.EMPTY_STRING)) {
                                StringBuffer sb = new StringBuffer(instructionCode23E).append("/")
                                        .append(disposalObject.getSI_BankAddlInstrCode());
                                instructionCode23E = sb.toString();
                            }
                        }

                        InstructionCode instructionCode = new InstructionCode();
                        instructionCode.setInstructionCode(instructionCode23E);
                        messageObject_103.addInstruction(instructionCode);

                        // HashMap tag32aMap = new HashMap();

                        messageObject_103.setTdValueDate(disposalObject.getValueDate().toString());
                        // artf52851 Changes Start
                        String tdCurrency = CommonConstants.EMPTY_STRING;
                        // artf52851 Changes end
                        if (!(disposalObject.getDealOriginator().equals("7"))) {
                            // artf52851 Changes Start
                            tdCurrency = disposalObject.getMainAccCurrencyCode();
                            // artf52851 Changes end
                        }
                        else {
                            // artf52851 Changes Start
                            tdCurrency = disposalObject.getContraAccCurrencyCode();
                            // artf52851 Changes end
                        }
                        // artf52851 Changes Start
                        messageObject_103.setTdCurrencyCode(tdCurrency);
                        // artf52851 Changes end
                        /*
                         * If Charge does not exist 32A and 33B amount would be same If charge
                         * exists and Charge code is "BEN" , 32A and 33B amount would be same IF
                         * charge exists and Charge code is OUR or SHA then , the 32A amount is
                         * equal to 33B amount - charge amount.
                         */
                        String amount = CommonConstants.EMPTY_STRING;
                        if (disposalObject.getDealOriginator().equals("F")) {
                            amount = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                                    util.noDecimalPlaces(tdCurrency, env));
                        }
                        else if (disposalObject.getDealOriginator().equals("7")) {
                            amount = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(),
                                    util.noDecimalPlaces(tdCurrency, env));
                        }
                        else {
                            amount = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(),
                                    util.noDecimalPlaces(tdCurrency, env));

                        }
                        messageObject_103.setTdAmount(amount);

                        if ((disposalObject.getDealOriginator().equals("8"))) {
                            amount = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                                    util.noDecimalPlaces(tdCurrency, env));
                            messageObject_103.setTdAmount(amount);

                        }
                        String instructedCurrency = computeInstructedCurrency();
                        if (disposalObject.getContractAmount().compareTo(disposalObject.getTransactionAmount()) != 0) {
                            if (!(disposalObject.getDealOriginator().equals("7"))) {
                                messageObject_103.setInstructedCurrency(tdCurrency);
                            }
                            else {
                                messageObject_103.setInstructedCurrency(instructedCurrency);
                            }

                            String instructedAmt = null;
                            instructedAmt = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                                    util.noDecimalPlaces(curCodeForInstAmtFormat, env));

                            messageObject_103.setInstructedAmount(instructedAmt);
                            // artf731769 and artf731655 Changes Ends
                            // changes for artf50405 start
                        }
                        else if (disposalObject.getDealOriginator().equalsIgnoreCase("3")) {
                            String instructedAmt = null;
                            instructedAmt = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(),
                                    util.noDecimalPlaces(curCodeForInstAmtFormat, env));
                            messageObject_103.setInstructedAmount(instructedAmt);
                            if (!(disposalObject.getDealOriginator().equals("7"))) {
                                messageObject_103.setInstructedCurrency(tdCurrency);
                            }
                            else {
                                messageObject_103.setInstructedCurrency(instructedCurrency);
                            }
                        }
                        else {
                            messageObject_103.setInstructedAmount(CommonConstants.EMPTY_STRING);
                            messageObject_103.setInstructedCurrency(CommonConstants.EMPTY_STRING);
                        }
                        /*
                         * If the deal is FX deal then for MT103 there would be no exchange rate
                         * hence it is being sent blank if deal orginator is "F" .
                         */

                        if (!(disposalObject.getMainAccCurrencyCode().equals(disposalObject.getContraAccCurrencyCode()))
                                && disposalObject.getDealOriginator().equals("7")) {
                            String exchRate = "";
                            if (disposalObject.getExchangeRate().toString().length() > 12) {
                                exchRate = disposalObject.getExchangeRate().toString().substring(0, 12);
                            }
                            else {
                                exchRate = disposalObject.getExchangeRate().toString();
                            }
                            messageObject_103.setExchangeRate(exchRate);
                            messageObject_103
                                    .setInstructedAmount(util.DecimalRounding(disposalObject.getFundingAmount().abs().toString(),
                                            util.noDecimalPlaces(curCodeForInstAmtFormat, env)));
                            messageObject_103.setInstructedCurrency(disposalObject.getMainAccCurrencyCode());
                        }
                        else if (isBelong) {
                            messageObject_103
                                    .setInstructedAmount(util.DecimalRounding(disposalObject.getFundingAmount().abs().toString(),
                                            util.noDecimalPlaces(curCodeForInstAmtFormat, env)));
                            messageObject_103.setInstructedCurrency(disposalObject.getMainAccCurrencyCode());
                        }
                        else if (disposalObject.getMainAccCurrencyCode().equals(disposalObject.getContraAccCurrencyCode())
                                || disposalObject.getDealOriginator().equals("F")) {
                            messageObject_103.setExchangeRate(CommonConstants.EMPTY_STRING);
                        }

                        String orderingCustomer50 = CommonConstants.EMPTY_STRING;
                        String tag50 = CommonConstants.EMPTY_STRING;
                        if ((disposalObject.getPartyIdentifier() != null
                                || disposalObject.getOrderingCustomerAccountNumber() != null)
                                && (disposalObject.getPartyIdentifier().trim().length() > 0
                                        || disposalObject.getOrderingCustomerAccountNumber().trim().length() > 0)
                                && disposalObject.getPartyIdentifierAdd1() != null
                                && disposalObject.getPartyIdentifierAdd1().length() > 2
                                && disposalObject.getPartyIdentifierAdd1().substring(0, 2).equalsIgnoreCase("1/")) {
                            String custIdentification = CommonConstants.EMPTY_STRING;
                            if (!disposalObject.getOrderingCustomerAccountNumber().isEmpty()) {
                                custIdentification = disposalObject.getOrderingCustomerAccountNumber();
                            }
                            else {
                                custIdentification = disposalObject.getPartyIdentifier();
                            }
                            orderingCustomer50 = custIdentification + SWT_Constants.delimiter
                                    + disposalObject.getPartyIdentifierAdd1() + SWT_Constants.delimiter
                                    + disposalObject.getPartyIdentifierAdd2() + SWT_Constants.delimiter
                                    + disposalObject.getPartyIdentifierAdd3() + SWT_Constants.delimiter
                                    + disposalObject.getPartyIdentifierAdd4();
                            tag50 = "F";
                        }
                        else if (disposalObject.getSI_OrdCustAccInfo() != null
                                && (disposalObject.getSI_OrdCustAccInfo().trim().length() > 0)) {
                            // changes start for artf52729
                            if (disposalObject.getOrderingCustomerIdentifierCode() != null
                                    && disposalObject.getOrderingCustomerIdentifierCode().trim().length() > 0) {
                                orderingCustomer50 = disposalObject.getSI_OrdCustAccInfo() + SWT_Constants.delimiter
                                        + disposalObject.getOrderingCustomerIdentifierCode();
                                tag50 = "A";
                            }
                            else {
                                // changes end for artf52729
                                // Changes start for artf53203
                                if (disposalObject.getPartyIdentifierAdd1() != null
                                        && disposalObject.getPartyIdentifierAdd1().trim().length() > 0) {
                                    orderingCustomer50 = disposalObject.getSI_OrdCustAccInfo() + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd1())
                                            + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd2())
                                            + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd3())
                                            // artf53359 changes start
                                            + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd4());
                                    // artf53359 changes end
                                }
                                if (disposalObject.getSI_OrdCustText1() != null
                                        && disposalObject.getSI_OrdCustText1().trim().length() > 0) {
                                    orderingCustomer50 = disposalObject.getSI_OrdCustAccInfo() + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getSI_OrdCustText1())
                                            + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getSI_OrdCustText2())
                                            + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getSI_OrdCustText3())
                                            // artf53359 changes start
                                            + SWT_Constants.delimiter
                                            + util.replaceSpecialChars(disposalObject.getSI_OrdCustText4());
                                    // artf53359 changes end
                                }
                                // Changes end for artf53203
                                tag50 = "K";
                                // changes start for artf52729
                            }
                            // changes end for artf52729
                        }
                        else if (disposalObject.getDealOriginator().equals("F")) {
                            if (disposalObject.getOrderingCustomerIdentifierCode() != null
                                    && !disposalObject.getOrderingCustomerIdentifierCode().equals(CommonConstants.EMPTY_STRING)) {
                                orderingCustomer50 = disposalObject.getOrderingCustomerAccountNumber() + SWT_Constants.delimiter
                                        + disposalObject.getOrderingCustomerIdentifierCode();

                                tag50 = TagValueA;
                            }
                            else if ((disposalObject.getPartyIdentifierAdd1() != null)
                                    && !(disposalObject.getPartyIdentifierAdd1()).equals(CommonConstants.EMPTY_STRING)) {
                                orderingCustomer50 = disposalObject.getOrderingCustomerAccountNumber() + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd1())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd2())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd3())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd4());
                                tag50 = TagValueK;
                            }
                        }
                        else {
                            if (disposalObject.getOrderingCustomerIdentifierCode() != null
                                    && !disposalObject.getOrderingCustomerIdentifierCode().equals(CommonConstants.EMPTY_STRING)) {
                                orderingCustomer50 = disposalObject.getOrderingCustomerAccountNumber() + SWT_Constants.delimiter
                                        + disposalObject.getOrderingCustomerIdentifierCode();
                                tag50 = TagValueA;
                            }
                            else if ((disposalObject.getPartyIdentifierAdd1() != null)
                                    && !(disposalObject.getPartyIdentifierAdd1().equals(CommonConstants.EMPTY_STRING))) {
                                orderingCustomer50 = disposalObject.getOrderingCustomerAccountNumber() + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd1())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd2())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd3())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getPartyIdentifierAdd4());
                                tag50 = TagValueK;
                            }
                            else if (disposalObject.getSI_OrdCustText1() != null
                                    && disposalObject.getSI_OrdCustText1().trim().length() > 0) {
                                orderingCustomer50 = disposalObject.getSI_OrdCustAccInfo() + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getSI_OrdCustText1())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getSI_OrdCustText2())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getSI_OrdCustText3())
                                        + SWT_Constants.delimiter
                                        + util.replaceSpecialChars(disposalObject.getSI_OrdCustText4());
                                tag50 = "K";
                                
                            }
                            
                        }

                        messageObject_103.setOrderingCustomer(orderingCustomer50);

                        messageObject_103.setOrderingCustomerOption(tag50);
                        // ord institute - 52
                        String orderInstitute52 = CommonConstants.EMPTY_STRING;
                        String tag52 = CommonConstants.EMPTY_STRING;
                        if (util.orderingInstituteDetailsExists(disposalObject)) {
                            String tempString = util.createSwiftTagString(disposalObject.getSI_OrdInstBICCode(),
                               disposalObject.getSI_OrdInstAccInfo(), 
                               get35CharText(disposalObject.getSI_OrdInstText1()),
                               get35CharText(disposalObject.getSI_OrdInstText2()),
                               get35CharText(disposalObject.getSI_OrdInstText3()),
                               get35CharText(disposalObject.getSI_OrdInstText4()));
                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                orderInstitute52 = tempString.substring(0, tempString.length() - 1);
                                tag52 = tempString.substring(tempString.length() - 1);
                            }
                        }

                        messageObject_103.setOrderingInstitution(orderInstitute52);

                        messageObject_103.setOrderInstitutionOption(tag52);

                        boolean accWithDetailsExistFlag = util.accountWithDetailsExists(disposalObject);
                        boolean interDetailsExistFlag = util.intermedaitoryDetailsExists(disposalObject);

                        String accWithInstitute57a = CommonConstants.EMPTY_STRING;
                        String tag57 = CommonConstants.EMPTY_STRING;
                        String senderssCorrspondent53 = CommonConstants.EMPTY_STRING;
                        String tag53 = CommonConstants.EMPTY_STRING;
                        String intermediatory56a = CommonConstants.EMPTY_STRING;
                        String tag56 = CommonConstants.EMPTY_STRING;
                        String receiversCorrspondent54 = CommonConstants.EMPTY_STRING;
                        String tag54 = CommonConstants.EMPTY_STRING;

                        if (!generateMT202) {
                            tag53 = "B";
                            // senders corrspondent - 53
                            if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
                                senderssCorrspondent53 = mainAccCustDetails.getF_SWTACCOUNTNO();

                            }
                            else if (contraAccCustDetails.getF_SWTACCOUNTNO() != null) {
                                senderssCorrspondent53 = mainAccCustDetails.getF_SWTACCOUNTNO();

                            }

                            String matrixKeyAccWith = messageGenKey.substring(4);
                            String matrixKeyInter = messageGenKey.substring(2, 4);
                            if ((matrixKeyAccWith.equals("B4") && matrixKeyInter.equals("I1"))
                                    || (!accWithDetailsExistFlag && !interDetailsExistFlag)) {
                                if ((payToBICCode == null) || (payToBICCode != null && !payToBICCode.equals(receiever))) {
                                    String tempString = util.createSwiftTagString(payToBICCode,

get35CharText(disposalObject.getSI_PayToPartyIdentifier()), 
get35CharText(disposalObject.getSI_PayToText1()),
get35CharText( disposalObject.getSI_PayToText2()), 
get35CharText(disposalObject.getSI_PayToText3()),
get35CharText(disposalObject.getSI_PayToText4()));
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                        accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
                                        tag57 = tempString.substring(tempString.length() - 1);
                                    }
                                }
                            }
                            else {
                                if (payToBICCode.equals(receiever)) {
                                    if (interDetailsExistFlag) {

                                        String tempString = util.createSwiftTagString(intermediaryBICCode,
                                                disposalObject.getSI_IntermediaryPartyIdentifier(),

get35CharText( disposalObject.getSI_IntermediatoryText1()),

get35CharText(disposalObject.getSI_IntermediatoryText2()),

get35CharText(disposalObject.getSI_IntermediatoryText3()),

get35CharText(disposalObject.getSI_IntermediatoryText4()));
                                        if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                            intermediatory56a = tempString.substring(0, tempString.length() - 1);
                                            tag56 = tempString.substring(tempString.length() - 1);
                                        }
                                    }
                                }
                                else {

                                    String tempString = util.createSwiftTagString(payToBICCode,
                                            disposalObject.getSI_PayToPartyIdentifier(), 
                                            get35CharText(disposalObject.getSI_PayToText1()),

get35CharText(disposalObject.getSI_PayToText2()), 
get35CharText(disposalObject.getSI_PayToText3()),

get35CharText(disposalObject.getSI_PayToText4()));
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                        intermediatory56a = tempString.substring(0, tempString.length() - 1);
                                        tag56 = tempString.substring(tempString.length() - 1);
                                    }
                                }
                                if (!interDetailsExistFlag || !flag72) {
                                    String tempString = util.createSwiftTagString(accWithBICCode,
                                            disposalObject.getSI_AccWithPartyIdentifier(),
                                            get35CharText( disposalObject.getSI_AccWithText1()),

get35CharText(disposalObject.getSI_AccWithText2()), 
get35CharText(disposalObject.getSI_AccWithText3()),

get35CharText(disposalObject.getSI_AccWithText4()));
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                        accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
                                        tag57 = tempString.substring(tempString.length() - 1);
                                    }
                                }
                                else {

                                    String tempString = util.createSwiftTagString(intermediaryBICCode,
                                            disposalObject.getSI_IntermediaryPartyIdentifier(),

                                 get35CharText(disposalObject.getSI_IntermediatoryText1()),
                                 get35CharText( disposalObject.getSI_IntermediatoryText2()),
                                 get35CharText( disposalObject.getSI_IntermediatoryText3()), 
                                 get35CharText(disposalObject.getSI_IntermediatoryText4()));
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                        accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
                                        tag57 = tempString.substring(tempString.length() - 1);
                                    }
                                }
                            }
                        }
                        else {
                            tag53 = "A";
                            if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
                                senderssCorrspondent53 = mainAccCustDetails.getF_BICCODE();

                            }
                            else if (contraAccCustDetails.getF_SWTACCOUNTNO() != null) {
                                senderssCorrspondent53 = contraAccCustDetails.getF_BICCODE();

                            }

                            if (accWithBICCode.equals(receiever)) {
                                if (interDetailsExistFlag) {
                                    String tempString = util.createSwiftTagString(intermediaryBICCode,
                                            disposalObject.getSI_IntermediaryPartyIdentifier(),
                                            get35CharText(disposalObject.getSI_IntermediatoryText1()), get35CharText(disposalObject.getSI_IntermediatoryText2()),
                                            get35CharText( disposalObject.getSI_IntermediatoryText3()), get35CharText(disposalObject.getSI_IntermediatoryText4()));
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                        receiversCorrspondent54 = tempString.substring(0, tempString.length() - 1);
                                        tag54 = tempString.substring(tempString.length() - 1);
                                    }
                                }
                                else if (!payToBICCode.equals(receiever)) {
                                    String tempString = util.createSwiftTagString(payToBICCode,
                                            disposalObject.getSI_PayToPartyIdentifier(), get35CharText(disposalObject.getSI_PayToText1()),
                                            get35CharText(disposalObject.getSI_PayToText2()), get35CharText(disposalObject.getSI_PayToText3()),
                                            get35CharText(disposalObject.getSI_PayToText4()));
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                        receiversCorrspondent54 = tempString.substring(0, tempString.length() - 1);
                                        tag54 = tempString.substring(tempString.length() - 1);
                                    }
                                }

                            }
                            if (intermediaryBICCode.equals(receiever)) {
                                String tempString = util.createSwiftTagString(payToBICCode,
                                        disposalObject.getSI_PayToPartyIdentifier(), get35CharText(disposalObject.getSI_PayToText1()),
                                        get35CharText(disposalObject.getSI_PayToText2()), get35CharText(disposalObject.getSI_PayToText3()),
                                        get35CharText(disposalObject.getSI_PayToText4()));
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    receiversCorrspondent54 = tempString.substring(0, tempString.length() - 1);
                                    tag54 = tempString.substring(tempString.length() - 1);
                                }
                            }
                            if (accWithDetailsExistFlag
                                    && (intermediaryBICCode.equals(receiever) || payToBICCode.equals(receiever))) {
                                String tempString = util.createSwiftTagString(accWithBICCode,
                                        disposalObject.getSI_AccWithPartyIdentifier(), get35CharText(disposalObject.getSI_AccWithText1()),
                                        get35CharText(disposalObject.getSI_AccWithText2()), get35CharText(disposalObject.getSI_AccWithText3()),
                                        get35CharText(disposalObject.getSI_AccWithText4()));
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
                                    tag57 = tempString.substring(tempString.length() - 1);
                                }
                            }
                        }

                        messageObject_103.setSendersCorrespondent(senderssCorrspondent53);
                        messageObject_103.setSendersCorrespOption(tag53);
                        messageObject_103.setAccountWithInstitution(accWithInstitute57a);
                        messageObject_103.setAccountWithInstOption(tag57);
                        messageObject_103.setReceiversCorrespondent(receiversCorrspondent54);
                        messageObject_103.setReceiversCorrespOption(tag54);
                        messageObject_103.setIntermediaryInstitution(intermediatory56a);
                        messageObject_103.setIntermediaryInstOption(tag56);

                        // beneficiary costomer
                        String benCustomer59 = null;
                        String tag59 = null;

                        if (disposalObject.getSI_BankInstructionCode() != null
                                && disposalObject.getSI_BankInstructionCode().equals("CHQB")) {
                            benCustomer59 = util.getForAccountInfoString(disposalObject, true);

                        }
                        else benCustomer59 = util.getForAccountInfoString(disposalObject, false);
                        if (!benCustomer59.isEmpty()) {
                            if (benCustomer59.substring(0, 1).equalsIgnoreCase("/")
                                    && ((benCustomer59.indexOf('$') + 1 < benCustomer59.length())
                                            && (benCustomer59.indexOf('$') + 3 < benCustomer59.length())
                                            && benCustomer59
                                                    .substring(benCustomer59.indexOf('$') + 1, benCustomer59.indexOf('$') + 3)
                                                    .equalsIgnoreCase("1/"))) {
                                tag59 = "F";
                            }
                            if (benCustomer59.substring(0, 2).equalsIgnoreCase("1/"))
                                tag59 = "F";
                        }
                        if (benCustomer59 != null && benCustomer59.trim().length() == 0) {
                            // Changes end for artf49964
                            if (!(disposalObject.getSI_ForAccountPartyIdentifier().equals(CommonConstants.EMPTY_STRING))
                                    && !(disposalObject.getForAccountIdentifierCode().equals(CommonConstants.EMPTY_STRING))) {
                                // Changes start for artf49964
                                if (!(disposalObject.getSI_BankInstructionCode().equals("CHQB"))) {
                                    // Changes end for artf49964
                                    benCustomer59 = disposalObject.getSI_ForAccountPartyIdentifier() + "$"
                                            + disposalObject.getForAccountIdentifierCode();
                                    tag59 = TagValueA;
                                    // Changes start for artf49964
                                }
                                // Changes end for artf49964
                                else {
                                    benCustomer59 = disposalObject.getForAccountIdentifierCode();
                                    tag59 = TagValueA;
                                }
                            }
                            // Changes start for artf49964
                            else {
                                benCustomer59 = disposalObject.getForAccountIdentifierCode();
                                tag59 = TagValueA;
                            }
                        }
                        else if (benCustomer59 != null && benCustomer59.trim().length() > 0
                                && disposalObject.getSI_ForAccountText1().trim().length() == 0
                                && disposalObject.getSI_ForAccountText1() != null) {
                            StringBuilder sb = new StringBuilder(benCustomer59).append("$")
                                    .append(disposalObject.getForAccountIdentifierCode());
                            benCustomer59 = sb.toString();
                            tag59 = TagValueA;
                        }
                        // Changes end for artf49964
                        messageObject_103.setBeneficiaryCustomer(benCustomer59);
                        messageObject_103.setBeneficiaryCustOption(tag59);
                        messageObject_103.setRemittanceInfo(util.getTag70String(disposalObject));
                        // charge code
                        String chargeCode71 = null;
                        if (chargeCode == null || EMPTYSTRING.equals(chargeCode)) {
                            chargeCode71 = "SHA";
                        }
                        if (!EMPTYSTRING.equals(chargeCode))
                            chargeCode71 = chargeCode;
                        messageObject_103.setDetailsOfCharges(chargeCode71);

                        String Tag71F = CommonConstants.EMPTY_STRING;
                        String Tag71G = CommonConstants.EMPTY_STRING;
                        BigDecimal finalReceiverCharges = BigDecimal.ZERO;
                        BigDecimal finalSenderCharges = BigDecimal.ZERO;
                        BigDecimal finaltdAmount = BigDecimal.ZERO;

                        if (disposalObject.getSI_SendersCharges().abs().compareTo(new BigDecimal("0.00")) >= 0 && ((!("SHA"
                                .equals(chargeCode)
                                && (disposalObject.getSI_SendersCharges().abs().compareTo(CommonConstants.BIGDECIMAL_ZERO) == 0)))
                                && !"OUR".equals(chargeCode))) {
                            Tag71F = disposalObject.getMainAccCurrencyCode()
                                    + util.DecimalRounding(disposalObject.getSI_SendersCharges().abs().toString(),
                                            util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
                            if ("SHA".equals(chargeCode)) {
                                messageObject_103.setInstructedAmount(
                                        util.DecimalRounding(disposalObject.getFundingAmount().abs().toString(),
                                                util.noDecimalPlaces(curCodeForInstAmtFormat, env)));
                                messageObject_103.setInstructedCurrency(disposalObject.getMainAccCurrencyCode());
                            }
                            else if ("BEN".equals(chargeCode)) {
                                if (!(disposalObject.getMainAccCurrencyCode().equals(disposalObject.getContraAccCurrencyCode()))) {
                                    BigDecimal exchangeRate = disposalObject.getExchangeRate();
                                    if (!StringUtils.isBlank(disposalObject.getInstructedAmtCurrency()) && (!disposalObject
                                            .getInstructedAmtCurrency().equals(disposalObject.getMainAccCurrencyCode())
                                            || !disposalObject.getInstructedAmtCurrency()
                                                    .equals(disposalObject.getContraAccCurrencyCode()))) {
                                        exchangeRate = BigDecimal.ZERO;
                                    }
                                    finalSenderCharges = calculateExchRateAmt(disposalObject.getMainAccCurrencyCode(),
                                            disposalObject.getContraAccCurrencyCode(), exchangeRate,
                                            disposalObject.getSI_SendersCharges(), disposalObject.getCreditExchangeRateType());
                                    finaltdAmount = disposalObject.getTransactionAmount().subtract(finalSenderCharges);
                                    messageObject_103.setTdAmount(util.DecimalRounding(finaltdAmount.abs().toString(),
                                            util.noDecimalPlaces(tdCurrency, env)));
                                }
                                else {
                                    finaltdAmount = disposalObject.getTransactionAmount()
                                            .subtract(disposalObject.getSI_SendersCharges());
                                    messageObject_103.setTdAmount(util.DecimalRounding(finaltdAmount.abs().toString(),
                                            util.noDecimalPlaces(tdCurrency, env)));
                                    messageObject_103.setInstructedAmount(
                                            util.DecimalRounding(disposalObject.getFundingAmount().abs().toString(),
                                                    util.noDecimalPlaces(curCodeForInstAmtFormat, env)));
                                    messageObject_103.setInstructedCurrency(disposalObject.getMainAccCurrencyCode());
                                }
                            }
                        }
                        else if (disposalObject.getSI_SendersCharges().abs().compareTo(new BigDecimal("0.00")) >= 0
                                && "OUR".equals(chargeCode)) {
                            finalReceiverCharges = disposalObject.getReceiverChargeAmount();
                            finaltdAmount = disposalObject.getTransactionAmount().add(finalReceiverCharges);
                            if (finalReceiverCharges.abs().compareTo(BigDecimal.ZERO) != 0) {
                                Tag71G = disposalObject.getContraAccCurrencyCode()
                                        + util.DecimalRounding(finalReceiverCharges.abs().toString(),
                                                util.noDecimalPlaces(disposalObject.getContraAccCurrencyCode(), env));
                            }

                            messageObject_103.setTdAmount(util.DecimalRounding(finaltdAmount.abs().toString(),
                                    util.noDecimalPlaces(tdCurrency, env)));
                            messageObject_103
                                    .setInstructedAmount(util.DecimalRounding(disposalObject.getFundingAmount().abs().toString(),
                                            util.noDecimalPlaces(curCodeForInstAmtFormat, env)));
                            if (null == messageObject_103.getInstructedCurrency()
                                    || messageObject_103.getInstructedCurrency().isEmpty()) {
                                messageObject_103.setInstructedCurrency(disposalObject.getMainAccCurrencyCode());
                            }
                            // when DealOrgination is FX-Deal and ReceiverChargeAmount is empty
                            // ReceiversCharges tag will not be generated.
                            if (!("F".equals(disposalObject.getDealOriginator()) && disposalObject.getReceiverChargeAmount().abs()
                                    .compareTo(CommonConstants.BIGDECIMAL_ZERO) == 0)) {
                                messageObject_103.setReceiversCharges(Tag71G);
                            }
                        }
                        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
                                .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE))
                                        .getBizInfo();
                        String field71f = (String) bizInfo.getModuleConfigurationValue("SWIFT", "SHOWSENDERCHARGESHA", env);
                        SendersCharges senderChargeDetails = new SendersCharges();
                        if (field71f.trim().equalsIgnoreCase("Y") || "OUR".equalsIgnoreCase(chargeCode)
                                || "BEN".equalsIgnoreCase(chargeCode)) {
                            senderChargeDetails.setSenderCharge(Tag71F);
                            messageObject_103.addCharges(senderChargeDetails);
                        }

                        StringBuilder senderinf72 = new StringBuilder();
                        if (flag72) {

                            if (util.createSwiftTagString(disposalObject.getSI_IntermediatoryCode(),
                                    disposalObject.getSI_IntermediaryPartyIdentifier(), get35CharText(disposalObject.getSI_IntermediatoryText1()),
                                    get35CharText( disposalObject.getSI_IntermediatoryText2()),get35CharText(disposalObject.getSI_IntermediatoryText3()),
                                    disposalObject.getSI_IntermediatoryText4()).trim().length() != 0
                                    && util.createSwiftTagString(disposalObject.getSI_AccWithPartyIdentifier(),
                                    		get35CharText(disposalObject.getSI_AccWithCode()), get35CharText(disposalObject.getSI_AccWithText1()),
                                    		get35CharText(disposalObject.getSI_AccWithText2()), get35CharText(disposalObject.getSI_AccWithText3()),
                                            disposalObject.getSI_AccWithText4()).trim().length() != 0) {
                                senderinf72.append("/ACC/");
                                if (disposalObject.getSI_AccWithCode().trim().equals(CommonConstants.EMPTY_STRING)) {
                                    if (!disposalObject.getSI_AccWithPartyIdentifier().trim()
                                            .equals(CommonConstants.EMPTY_STRING)) {

                                        senderinf72.append(disposalObject.getSI_AccWithPartyIdentifier());
                                        senderinf72.append("$");
                                    }
                                    else {
                                        senderinf72.append("$");
                                    }
                                }
                                else {

                                    if (!disposalObject.getSI_AccWithPartyIdentifier().trim()
                                            .equals(CommonConstants.EMPTY_STRING)) {

                                        senderinf72.append(disposalObject.getSI_AccWithCode());
                                        senderinf72.append("$");
                                        senderinf72.append(getFormattedSecondLine(disposalObject.getSI_AccWithPartyIdentifier()));
                                    }
                                    else {

                                        senderinf72.append(disposalObject.getSI_AccWithCode());
                                        senderinf72.append("$");
                                    }
                                }
                            }
                            senderinf72
                                    .append(getFormattedSecondLine(util.replaceSpecialChars(disposalObject.getSI_AccWithText1())));
                            senderinf72
                                    .append(getFormattedSecondLine(util.replaceSpecialChars(disposalObject.getSI_AccWithText2())));
                            senderinf72
                                    .append(getFormattedSecondLine(util.replaceSpecialChars(disposalObject.getSI_AccWithText3())));

                            senderinf72.append(util.getBankToBankInfo(disposalObject));

                        }
                        else {
                            senderinf72.append(util.getBankToBankInfo(disposalObject));
                        }
                        String sender2ReceiverInfo72 = genericStringFormatter(senderinf72.toString());

                        messageObject_103.setSenderToReceiverInfo(sender2ReceiverInfo72);

                        messageObject_103.setEndtoendTxnRef(disposalObject.getEnd2EndTxnRef());
                        messageObject_103.setServiceTypeId(disposalObject.getServiceTypeId());
                        // for generating MT192 message
                        if (generateMT192) {
                            /*
                             * If the message type is 192 i.e cancellation the we are just setting
                             * the action tag as "C"
                             */

                            messageObject_103.setAction("C");
                            messageObject_103.setServiceTypeId(null);
                            messageObject_103.setEndtoendTxnRef(null);

                        }
                        else if (disposalObject.getTransactionStatus().startsWith("AM")) {
                            /*
                             * If the Transaction Type is AMEND (Code_Word) i.e amendment then we
                             * are just setting the action tag as "A"
                             */
                            messageObject_103.setAction("A");
                        }

                        // instructed ccy not same as debit currency not same as credit currency
                        if (!StringUtils.isBlank(disposalObject.getInstructedAmtCurrency()) && (!disposalObject
                                .getInstructedAmtCurrency().equals(disposalObject.getMainAccCurrencyCode())
                                || !disposalObject.getInstructedAmtCurrency().equals(disposalObject.getContraAccCurrencyCode()))) {
                            messageObject_103.setInstructedCurrency(disposalObject.getInstructedAmtCurrency());
                            // if 33B is not zero then exchange rate(36) must be present.
                            if (!messageObject_103.getInstructedAmount().isEmpty()
                                    && !("0.00").equals(messageObject_103.getInstructedAmount())) {
                                // if debit account currency not same credit account currency
                                if (!disposalObject.getMainAccCurrencyCode().equals(disposalObject.getContraAccCurrencyCode()))
                                    messageObject_103.setExchangeRate(disposalObject.getExchangeRate().toString());
                                // if instructed amount currency not same credit account currency
                                if (!disposalObject.getInstructedAmtCurrency().equals(disposalObject.getContraAccCurrencyCode())) {
                                    messageObject_103.setExchangeRate(disposalObject.getExchangeRate().toString());
                                }else {
                                    messageObject_103.setExchangeRate(CommonConstants.EMPTY_STRING);
                                }
                            }
                            else {
                                messageObject_103.setExchangeRate(CommonConstants.EMPTY_STRING);
                            }
                        }

                        // if instructed amount is zero then 33B should not be sent 
                        //added the equals check against 0 to support the JPY currency as well
                        if (("0.00").equals(messageObject_103.getInstructedAmount()) || ("0").equals(messageObject_103.getInstructedAmount())) {
                            messageObject_103.setInstructedAmount(CommonConstants.EMPTY_STRING);
                            messageObject_103.setExchangeRate(CommonConstants.EMPTY_STRING);
                        }

                        if (logger.isInfoEnabled()) {
                            logger.info("Instrucetd Amount::::::" + messageObject_103.getInstructedAmount());
                            logger.info("Instrucetd Amount Currency::::::" + messageObject_103.getInstructedCurrency());
                        }
                        xmlTagValueMapList.add(messageObject_103);
                        setF_OUT_XMLTAGVALUEMAPLIST(xmlTagValueMapList);
                        setF_OUT_messageGenMatrix(messageGenKey);
                    }
                    else if (!generateMT202OrMT292) {
                        generateAnyMessage = false;
                    }
                }
                // setting up outout fields
                setF_OUT_generateMT103Or192(Boolean.valueOf(generateMT103OrMT192));
                setF_OUT_generateMT202Or292(Boolean.valueOf(generateMT202OrMT292));
                setF_OUT_MESSAGEGEN(Boolean.valueOf(generateAnyMessage));
                setF_OUT_GEN_MT292(Boolean.valueOf(generateMT292));
                setF_OUT_GEN_MT202(Boolean.valueOf(generateMT202));
                if (disposalObject.getReceiptFlagMT210() == 0
                        || (disposalObject.getReceiptFlagMT210() == 2 && disposalObject.getCancelFlag() == 0)) {
                    setF_OUT_MT210Generate(new Integer(0));
                }
                else {
                    setF_OUT_MT210Generate(new Integer(1));
                }

            }

            else {
                generateAnyMessage = false;
            }
        }
        catch (Exception e1) {
            logger.error(ExceptionUtil.getExceptionAsString(e1));
        }

        if (disposalObject != null) {
            setF_OUT_DisposalId(disposalObject.getDisposalRef());
        }
        else {
            setF_OUT_DisposalId("0");
        }
    }

    /**
     *
     * This method takes value from disposal object and finds appropriate value from properties file
     * to decide which message to be genrated
     *
     * @param contraAccCustBICCode
     * @param payToBICCode
     * @param intermediaryBICCode
     * @param benBICCode
     * @param env
     * @return @
     */
    private String getMessageGenMatrixKey(String messagePreference, String contraAccCustBICCode, String payToBICCode,
            String intermediaryBICCode, String benBICCode, BankFusionEnvironment env) {
        // finding appropreate Mesaage generation from properties file
        // storing PX-IX-BX values

        StringBuffer tempBuffer = new StringBuffer();
        boolean intermediaryBkeyAuth = false;
        boolean beneficiaryBkeyAuth = false;

        // for PX
        IBOBicCodes payToBicCodeDetails = null;
        if (!payToBICCode.equals(EMPTYSTRING)) {
            payToBicCodeDetails = (IBOBicCodes) env.getFactory().findByPrimaryKey(IBOBicCodes.BONAME, payToBICCode);
        }
        if (payToBICCode != null && !payToBICCode.equals(EMPTYSTRING)) {
            if (payToBICCode.equals(contraAccCustBICCode))
                tempBuffer.append("P1");
            else if (payToBicCodeDetails != null && payToBicCodeDetails.isF_BKEAUTH())
                tempBuffer.append("P2");
            else tempBuffer.append("P3");
        }
        else tempBuffer.append("P4");

        // for IX
        IBOBicCodes intermediaryBICCodeDetails = null;
        if (!intermediaryBICCode.equals(EMPTYSTRING)) {
            intermediaryBICCodeDetails = (IBOBicCodes) env.getFactory().findByPrimaryKey(IBOBicCodes.BONAME, intermediaryBICCode);
        }
        if (PaymentSwiftConstants.PAYMENT_PREFERENCE_SERIAL.equalsIgnoreCase(messagePreference)) {
            intermediaryBkeyAuth = false;
        }
        else if (intermediaryBICCodeDetails != null) {
            intermediaryBkeyAuth = intermediaryBICCodeDetails.isF_BKEAUTH();
        }
        if (intermediaryBICCode != null && !intermediaryBICCode.equals(EMPTYSTRING)) {
            if (intermediaryBkeyAuth)
                tempBuffer.append("I2");
            else tempBuffer.append("I3");
        }
        else if (disposalObject.getSI_IntermediatoryText1() != null
                && !disposalObject.getSI_IntermediatoryText1().equals(EMPTYSTRING)) {
            tempBuffer.append("I4");
        }
        else {
            tempBuffer.append("I1");
        }
        // // for BX
        IBOBicCodes benBICCodeDetails = null;
        if (!benBICCode.equals(EMPTYSTRING)) {
            benBICCodeDetails = (IBOBicCodes) env.getFactory().findByPrimaryKey(IBOBicCodes.BONAME, benBICCode);
        }
        if (PaymentSwiftConstants.PAYMENT_PREFERENCE_SERIAL.equalsIgnoreCase(messagePreference)) {
            beneficiaryBkeyAuth = false;
        }
        else if (benBICCodeDetails != null) {
            beneficiaryBkeyAuth = benBICCodeDetails.isF_BKEAUTH();
        }
        if (benBICCode != null && !benBICCode.equals(EMPTYSTRING)) {
            if (beneficiaryBkeyAuth)
                tempBuffer.append("B1");
            else tempBuffer.append("B2");
        }
        else if (disposalObject.getSI_AccWithText1() != null && !disposalObject.getSI_AccWithText1().equals(EMPTYSTRING)) {
            tempBuffer.append("B3");
        }
        else {
            tempBuffer.append("B4");
        }
        return tempBuffer.toString();
    }

    /**
     *
     * This method populates map from properties file @
     */
    private void populateMessageTypesMap() {
        InputStream is = null;
        Properties swiftMessageProps = new Properties();
        String configLocation = null;
        try {
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            is = new FileInputStream(configLocation + "conf/swift/" + SWIFT_MESSAGETYPES_FILE_NAME);
            swiftMessageProps.load(is);

            Enumeration keys = swiftMessageProps.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = swiftMessageProps.getProperty(key);
                messageGenerationMap.put(key, value);
            }
        }
        catch (Exception ex) {
            if (is == null) {
                if (logger.isDebugEnabled())
                    logger.debug(configLocation + "conf/swift/" + SWIFT_MESSAGETYPES_FILE_NAME
                            + " not found as file, trying as resource");
                is = this.getClass().getClassLoader().getResourceAsStream("conf/swift/" + SWIFT_MESSAGETYPES_FILE_NAME);
                try {
                    swiftMessageProps.load(is);
                }
                catch (IOException e) {
                    logger.error(configLocation + "conf/swift/" + SWIFT_MESSAGETYPES_FILE_NAME + " not found as file");
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
            }
            logger.error(ExceptionUtil.getExceptionAsString(ex));
        }
    }

    /**
     * This method will take string as a parameter containing $ as a separator we are removing the
     * empty lines and taking only first 6 line because SWIFT accepts only 6 lines in filed 72.
     *
     * @param originalString
     * @return
     */
    private String genericStringFormatter(String originalString) {

        String remove$FromString = originalString.replace('$', '@');

        String removeBlankLines = remove$FromString.replaceAll("//@", CommonConstants.EMPTY_STRING);

        StringBuffer finalString = new StringBuffer();

        String tempString[] = removeBlankLines.split("@");

        if (tempString.length > 6) {
            for (int i = 0; i < 6; i++) {
                finalString.append(tempString[i].length()>35?tempString[i].substring(0, 35):tempString[i]);
                finalString.append("$");

            }
        }
        else {
            for (int j = 0; j < tempString.length; j++) {
                finalString.append(tempString[j].length()>35?tempString[j].substring(0, 35):tempString[j]);
                finalString.append("$");
            }
        }

        return finalString.substring(0, finalString.lastIndexOf("$"));
    }

    /**
     * Compute instructed currency
     * 
     * @return
     */
    private String computeInstructedCurrency() {
        String debitCurr = disposalObject.getMainAccCurrencyCode();
        String settlementCurr = disposalObject.getContraAccCurrencyCode();
        String instructedAmtCurr = disposalObject.getInstructedAmtCurrency();
        BigDecimal chargeAmt = disposalObject.getSI_SendersCharges();
        String chargeCode = disposalObject.getSI_ChargeCode();
        BigDecimal instructedAmt = disposalObject.getContractAmount();
        BigDecimal settlementAmt = disposalObject.getTransactionAmount();
        BigDecimal settlementAmtWithoutCharge = null;
        if (!StringUtils.isBlank(instructedAmtCurr)
                && (!instructedAmtCurr.equals(debitCurr) || !instructedAmtCurr.equals(settlementCurr))) {
            return instructedAmtCurr;
        }
        else {
            if (chargeAmt.compareTo(CommonConstants.BIGDECIMAL_ZERO) == 0 || "SHA".equals(chargeCode)) {
                if (instructedAmt.compareTo(settlementAmt) == 0) {
                    return settlementCurr;
                }
                else return debitCurr;
            }
            else {
                if ("OUR".equals(chargeCode)) {
                    settlementAmtWithoutCharge = settlementAmt.subtract(chargeAmt);
                }
                else {
                    settlementAmtWithoutCharge = settlementAmt.add(chargeAmt);
                }
                if (instructedAmt.compareTo(settlementAmtWithoutCharge) == 0) {
                    return settlementCurr;
                }
                else return debitCurr;
            }
        }
    }

    /**
     * @param buyCurrency
     * @param sellCurrency
     * @param exchangeRate
     * @param buyAmount
     * @param exchangeRateType
     * @return
     */
    private BigDecimal calculateExchRateAmt(String buyCurrency, String sellCurrency, BigDecimal exchangeRate, BigDecimal buyAmount,
            String exchangeRateType) {

        RqHeader rqHeader = new RqHeader();
        Orig orig = new Orig();
        orig.setChannelId("SWIFT");
        rqHeader.setOrig(orig);
        CalcExchangeRateRq exchRq = new CalcExchangeRateRq();
        CalcExchRateDetails exchangeDtls = new CalcExchRateDetails();
        exchangeDtls.setSellAmount(BigDecimal.ZERO);
        if (buyAmount.signum() < 0) {
            exchangeDtls.setBuyAmount(buyAmount.abs());
        }
        else {
            exchangeDtls.setBuyAmount(buyAmount);
        }

        exchangeDtls.setBuyCurrency(buyCurrency);
        exchangeDtls.setSellCurrency(sellCurrency);
        exchRq.setCalcExchRateDetails(exchangeDtls);
        ExchangeRateDetails exchangeRateDetails = new ExchangeRateDetails();
        exchangeRateDetails.setExchangeRate(exchangeRate);
        exchangeRateDetails.setExchangeRateType(!StringUtils.isBlank(exchangeRateType) ? exchangeRateType : "SPOT");
        exchangeDtls.setExchangeRateDetails(exchangeRateDetails);
        exchRq.setRqHeader(rqHeader);
        BankFusionEnvironment env = new BankFusionEnvironment(null);
        HashMap inputMap = new HashMap();
        inputMap.put("CalcExchangeRateRq", exchRq);
        env.setData(new HashMap());
        HashMap outputParams = MFExecuter.executeMF("CB_FEX_CalculateExchangeRateAmount_SRV", env, inputMap);

        CalcExchangeRateRs calcExchangeRateRs = (CalcExchangeRateRs) outputParams.get("CalcExchangeRateRs");
        BigDecimal equivalentAmount = calcExchangeRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
        if (buyAmount.signum() < 0) {
            equivalentAmount = BigDecimal.ZERO.subtract(equivalentAmount);
        }
        return equivalentAmount;
    }

    /**
     * @param input
     * @return
     */
    private String getFormattedSecondLine(String input) {
        if (input.trim().length() == 0)
            return input;
        if (input.startsWith("//")) {
            return input + "$";
        }
        else if ("/".equals(input.trim().charAt(0))) {
            return "/" + input + "$";
        }
        else {
            return "//" + input + "$";
        }

    }

    // this method is for Junit
    public void setDisposalObject(UB_SWT_DisposalObject disposalObject) {
        this.disposalObject = disposalObject;
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