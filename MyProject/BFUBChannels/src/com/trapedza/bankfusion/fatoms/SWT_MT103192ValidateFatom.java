/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.swift.InstructionCode;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.SendersCharges;
import com.misys.ub.swift.UB_MT103;
import com.trapedza.bankfusion.bo.refimpl.IBOAddress;
import com.trapedza.bankfusion.bo.refimpl.IBOAddressLinks;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT103192Validate;
import com.trapedza.bankfusion.steps.refimpl.ISWT_MT103192Validate;

/**
 * @author hardikp
 *
 */
/**
 * @author hardikp
 *
 */
public class SWT_MT103192ValidateFatom extends AbstractSWT_MT103192Validate implements ISWT_MT103192Validate {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public BankFusionObject getF_IN_DisposalObject() {
        // TODO Auto-generated method stub
        return super.getF_IN_DisposalObject();
    }

    private transient final static Log logger = LogFactory.getLog(SWT_MT103192ValidateFatom.class.getName());
    private final static String SWIFT_MESSAGETYPES_FILE_NAME = "SWT_MessageTypes.properties";
    private final static String TagValueA = "A";
    private final static String TagValueK = "K";
    private final static String EMPTYSTRING = CommonConstants.EMPTY_STRING;
    private final static int DIGIT_THREE = 3;
    private final static int DIGIT_ONE = 1;
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
    private boolean contraAccIsNostroAcc = false;

    /**
     * flag to decide whether contra acc is nostro acc
     */

    /**
     * flag for MT103 generation
     */
    private boolean flagMT103 = false;

    /**
     * flag for MT202 generation
     */
    private boolean flagMT202 = false;

    /**
     * flag to check whether flag72 details should be use for message generation or not
     */
    private boolean flag72 = false;

    /**
     * received flag value if any, should be use for message generation or not
     */
    private String receiverFlagValue = null;

    /**
     * flag to generate MT103 message
     */
    private boolean generateMT103 = false;

    /**
     * flag to generate MT202 message
     */
    private boolean generateMT202 = false;

    /**
     * fflag to generate MT192 message
     */
    private boolean generateMT192 = false;

    /**
     * flag to generate MT292 message
     */
    private boolean generateMT292 = false;

    /**
     * flag to generate any message or not
     */
    private boolean generateAnyMessage = true;

    /**
     * flag to generate any message or not
     */
    private boolean generateMT103OrMT192 = false;

    /**
     * flag to generate any message or not
     */
    private boolean generateMT202OrMT292 = false;

    /**
     * Desposal object
     */
    private SWT_DisposalObject disposalObject = null;

    /**
     * HashMap with keys as XML tag name and values as tag values for xml generation
     */
    private HashMap xmlTagValueMap = null;

    /**
     * ArrayList for storing xmlTagValue HashMap
     */
    private ArrayList xmlTagValueMapList = null;

    public SWT_MT103192ValidateFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * This method is to initiliaze the frequently used variables
     *
     * @
     */
    private void init() {
        try {
            Object obj = getF_IN_DisposalObject();
            if (obj instanceof SWT_DisposalObject) {
                disposalObject = (SWT_DisposalObject) obj;

            }
            else {
                logger.debug(obj.toString());
            }

        }
        catch (Exception e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        xmlTagValueMap = new HashMap();
        xmlTagValueMapList = new ArrayList();
        populateMessageTypesMap();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT103192Validate#process(com.trapedza.
     * bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {

        init();
        IBOSwtCustomerDetail contraAccCustDetails = null;
        IBOSwtCustomerDetail mainAccCustDetails = null;
        IBOSwtCustomerDetail clientDetails = null;
        SWT_Util util = new SWT_Util();
        UB_MT103 messageObject_103 = new UB_MT103();
        if (disposalObject != null) {

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
            // try {
            String AccountNo = null;
            if (disposalObject.getDealOriginator().equalsIgnoreCase("F"))
                AccountNo = disposalObject.getMainAccountNo();
            else AccountNo = disposalObject.getContraAccountNo();

            contraAccIsNostroAcc = util.isSwiftNostro(AccountNo, env);
            int msgStatus = util.updateFlagValues(env, 103, disposalObject.getDisposalRef());
            setF_OUT_msgStatusFlag(new Integer(msgStatus));
            try {
                // contra account customer details
                if ((disposalObject.getDealOriginator().equalsIgnoreCase("F"))
                        || disposalObject.getDealOriginator().equalsIgnoreCase("7")) {
                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getContraAccCustomerNumber());
                    // main account customer details
                    mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getMainAccCustomerNumber());
                }
                else {
                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getMainAccCustomerNumber());
                    // main account customer details
                    mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getContraAccCustomerNumber());
                }
                // Client Details for FX transaction
                if (disposalObject.getDealOriginator().compareTo("F") == 0)
                    clientDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getClientNumber());

            }
            catch (BankFusionException bfe) {
                logger.error("Error while getting  SettlementDetails OR COntraAccCustDetails OR Main Acc CUsto Details", bfe);
                generateAnyMessage = false;
            }

            String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
            String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
            String accWithBICCode = util.verifyForNull(disposalObject.getSI_AccWithCode());

            // getting message value from propertis file
            String messageGenKey = null;
            try {
                messageGenKey = getMessageGenMatrixKey(contraAccCustDetails.getF_BICCODE(), payToBICCode, intermediaryBICCode,
                        accWithBICCode, env);
                if (messageGenKey != null && !messageGenKey.equals(EMPTYSTRING)) {
                    String messageGenValue = (String) messageGenerationMap.get(messageGenKey);
                    if (messageGenValue != null && !messageGenValue.equals(EMPTYSTRING)) {
                        messageGenValue = messageGenValue.trim();
                        if (messageGenValue.length() == DIGIT_THREE) {
                            if (messageGenValue.charAt(0) == CHAR_DIGIT_ONE && contraAccIsNostroAcc) {
                                flagMT103 = true;
                                setF_OUT_MT103Message(new Integer(0));
                            }
                            if (messageGenValue.charAt(1) == CHAR_DIGIT_ONE) {
                                flagMT202 = true;
                                setF_OUT_MT202Message(new Integer(0));
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
                                flagMT202 = true;
                                setF_OUT_MT202Message(new Integer(0));
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
                // throw new BankFusionException(123, new Object[] { e.getLocalisedMessage() },
                // logger, env);
                EventsHelper.handleEvent(CommonsEventCodes.E_THE_INPUT_TAG_HAS_AN_INVALID_VALUE,
                        new Object[] { e.getLocalisedMessage() }, new HashMap(), env);
            }
            // which messages to be generated...

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

                if (disposalObject.getCancelFlag() == 0 && disposalObject.getPaymentFlagMT202() == DIGIT_ONE)
                    generateMT292 = true;

                generateMT202OrMT292 = true;
            }
            Timestamp bankFusionSystemDate2 = SystemInformationManager.getInstance().getBFBusinessDateTime();
            boolean generate103 = util.generateCategory2Message(((SWT_DisposalObject) disposalObject).getValueDate(), env,
                    ((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
                    new java.sql.Date(bankFusionSystemDate2.getTime()), "103");
            if (generate103) {

                // setting up fatom output values if MR103 OR MT192 generation
                if (generateMT103 || generateMT192) {

                    // message type
                    if (generateMT103)

                        messageObject_103.setMessageType("MT103");
                    else

                        messageObject_103.setMessageType("MT103");

                    messageObject_103.setDisposalRef(disposalObject.getDisposalRef());
                    // sender bic code
                    IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());

                    messageObject_103.setSender(branchObj.getF_BICCODE());
                    // receiver
                    String receiever = null;

                    if (receiverFlagValue == null || receiverFlagValue.equals(CommonConstants.EMPTY_STRING)) {
                        if (!disposalObject.getDealOriginator().equals("7")) {
                            receiever = mainAccCustDetails.getF_BICCODE();
                        }
                        else {
                            receiever = contraAccCustDetails.getF_BICCODE();
                        }

                    }
                    else if (receiverFlagValue.equals("NO")) {
                        if (disposalObject.getDealOriginator().equals("7")) {
                            receiever = contraAccCustDetails.getF_BICCODE();
                            if (receiever.trim().equals(CommonConstants.EMPTY_STRING)) {
                                receiever = contraAccCustDetails.getF_ALTERNATEBICCODE();
                            }
                        }
                        else {
                            receiever = mainAccCustDetails.getF_ALTERNATEBICCODE();
                            if (receiever.equals(CommonConstants.EMPTY_STRING) || receiever == null || receiever.equals(" ")) {
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

                    messageObject_103.setSendersReference(disposalObject.getCurrentDealNumber());
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
                    // If Bank Operation Code is SPAY/SSTD the 23E will not come in picture
                    if (!(disposalObject.getSI_BankOpCode().equals("SPAY") || disposalObject.getSI_BankOpCode().equals("SSTD"))) {
                        if (!disposalObject.getSI_BankInstructionCode().equals(CommonConstants.EMPTY_STRING)) {
                            instructionCode23E = disposalObject.getSI_BankInstructionCode();
                        }
                        if (!disposalObject.getSI_BankAddlInstrCode().equals(CommonConstants.EMPTY_STRING)) {
                            instructionCode23E = instructionCode23E + "/" + disposalObject.getSI_BankAddlInstrCode();
                        }
                    }

                    InstructionCode instructionCode = new InstructionCode();
                    instructionCode.setInstructionCode(instructionCode23E);
                    messageObject_103.addInstruction(instructionCode);

                    HashMap tag32aMap = new HashMap();

                    messageObject_103.setTdValueDate(disposalObject.getValueDate().toString());

                    if (!(disposalObject.getDealOriginator().equals("7"))) {
                        messageObject_103.setTdCurrencyCode(disposalObject.getMainAccCurrencyCode());
                    }
                    else {
                        messageObject_103.setTdCurrencyCode(disposalObject.getContraAccCurrencyCode());
                    }
                    /*
                     * If Charge does not exist 32A and 33B amount would be same If charge exists
                     * and Charge code is "BEN" , 32A and 33B amount would be same IF charge exists
                     * and Charge code is OUR or SHA then , the 32A amount is equal to 33B amount -
                     * charge amount.
                     */
                    String amount = CommonConstants.EMPTY_STRING;
                    if (disposalObject.getDealOriginator().equals("F") || disposalObject.getDealOriginator().equals("7")) {
                        amount = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                                util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
                    }
                    /*
                     * if ((disposalObject.getSI_SendersCharges().doubleValue() >= 0) &&
                     * (disposalObject.getSI_ChargeCode().equals("OUR") ||
                     * disposalObject.getSI_ChargeCode() .equals("SHA"))) {
                     * 
                     * /* if (!(disposalObject.getDealOriginator().equals("7"))) { amount =
                     * SWT_Util.DecimalRounding((SWT_Util.BigDecimalSubtract(disposalObject
                     * .getContractAmount(),
                     * disposalObject.getSI_SendersCharges()).abs()).toString(),
                     * SWT_Util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env)); }
                     * else {
                     */
                    /*
                     * amount =
                     * SWT_Util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString
                     * (), SWT_Util.noDecimalPlaces(disposalObject.getContraAccCurrencyCode(),
                     * env)); // }
                     * 
                     * // tag32aMap.put(SWT_Constants.AMOUNT32A, amount);
                     * messageObject_103.setTdAmount(amount); } else if
                     * (disposalObject.getSI_ChargeCode().equals("BEN")) { amount =
                     * SWT_Util.DecimalRounding((SWT_Util.BigDecimalSubtract(disposalObject
                     * .getContractAmount(),
                     * disposalObject.getSI_SendersCharges()).abs()).toString(),
                     * SWT_Util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
                     * messageObject_103.setTdAmount(amount); } else {
                     */
                    else {
                        amount = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(),
                                util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));

                    }
                    messageObject_103.setTdAmount(amount);

                    // }
                    if ((disposalObject.getDealOriginator().equals("8"))) {
                        amount = util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                                util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));
                        messageObject_103.setTdAmount(amount);

                    }
                    messageObject_103.setInstructedCurrency(disposalObject.getMainAccCurrencyCode());
                    messageObject_103.setInstructedAmount(util.DecimalRounding(disposalObject.getContractAmount().abs().toString(),
                            util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env)));
                    /*
                     * If the deal is FX deal then for MT103 there would be no exchange rate hence
                     * it is being sent blank if deal orginator is "F" .
                     */
                    if (disposalObject.getMainAccCurrencyCode().equals(disposalObject.getContraAccCurrencyCode())
                            || disposalObject.getDealOriginator().equals("F")) {
                        messageObject_103.setExchangeRate(CommonConstants.EMPTY_STRING);
                    }
                    else {
                        messageObject_103.setExchangeRate(disposalObject.getInterestOrExchangeRate().toString());
                    }

                    // ord customer 50
                    String orderingCustomer50 = CommonConstants.EMPTY_STRING;
                    String tag50 = CommonConstants.EMPTY_STRING;
                    String tempStr = CommonConstants.EMPTY_STRING;
                    if (disposalObject.getPartyIdentifier() != null && disposalObject.getPartyIdentifier().trim().length() > 0) {
                        orderingCustomer50 = disposalObject.getPartyIdentifier() + SWT_Constants.delimiter
                                + disposalObject.getPartyIdentifierAdd1() + SWT_Constants.delimiter
                                + disposalObject.getPartyIdentifierAdd2() + SWT_Constants.delimiter
                                + disposalObject.getPartyIdentifierAdd3() + SWT_Constants.delimiter
                                + disposalObject.getPartyIdentifierAdd4();
                        tag50 = "F";
                    }
                    else if (disposalObject.getSI_OrdCustAccInfo() != null
                            && (disposalObject.getSI_OrdCustAccInfo().trim().length() > 0)) {
                        orderingCustomer50 = disposalObject.getSI_OrdCustAccInfo() + SWT_Constants.delimiter
                                + disposalObject.getSI_OrdCustText1() + SWT_Constants.delimiter
                                + disposalObject.getSI_OrdCustText2() + SWT_Constants.delimiter
                                + disposalObject.getSI_OrdCustText3();
                        tag50 = "K";
                    }
                    else if (disposalObject.getDealOriginator().equals("F")) {
                        if (clientDetails.getF_BICCODE() != null
                                && !clientDetails.getF_BICCODE().equals(CommonConstants.EMPTY_STRING)) {
                            orderingCustomer50 = clientDetails.getF_SWTACCOUNTNO() + SWT_Constants.delimiter
                                    + clientDetails.getF_BICCODE();
                            tag50 = TagValueA;
                        }
                        else if ((tempStr = getCustomerDetailsString(disposalObject.getClientNumber(), env)) != null) {
                            orderingCustomer50 = clientDetails.getF_SWTACCOUNTNO() + SWT_Constants.delimiter + tempStr;
                            tag50 = TagValueK;
                        }
                    }
                    else {
                        if (contraAccCustDetails.getF_BICCODE() != null
                                && !contraAccCustDetails.getF_BICCODE().equals(CommonConstants.EMPTY_STRING)) {
                            orderingCustomer50 = contraAccCustDetails.getF_SWTACCOUNTNO() + SWT_Constants.delimiter
                                    + contraAccCustDetails.getF_BICCODE();
                            tag50 = TagValueA;
                        }
                        else if ((tempStr = getCustomerDetailsString(disposalObject.getMainAccCustomerNumber(), env)) != null) {
                            orderingCustomer50 = contraAccCustDetails.getF_SWTACCOUNTNO() + SWT_Constants.delimiter + tempStr;
                            tag50 = TagValueK;
                        }
                    }

                    messageObject_103.setOrderingCustomer(orderingCustomer50);

                    messageObject_103.setOrderingCustomerOption(tag50);
                    // ord institute - 52
                    String orderInstitute52 = CommonConstants.EMPTY_STRING;
                    String tag52 = CommonConstants.EMPTY_STRING;
                    if (util.orderingInstituteDetailsExists(disposalObject)) {
                        String tempString = util.createSwiftTagString(disposalObject.getSI_OrdInstBICCode(),
                                disposalObject.getSI_OrdInstAccInfo(), null, disposalObject.getSI_OrdInstText1(),
                                disposalObject.getSI_OrdInstText2(), disposalObject.getSI_OrdInstText3());
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

                    // TODO From this place the code should be reviewed - Start
                    if (!generateMT202) {
                        // senders corrspondent - 53
                        if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
                            senderssCorrspondent53 = mainAccCustDetails.getF_SWTACCOUNTNO();
                        }
                        else if (contraAccCustDetails.getF_SWTACCOUNTNO() != null) {
                            senderssCorrspondent53 = mainAccCustDetails.getF_SWTACCOUNTNO();
                            tag53 = "B";
                        }

                        String matrixKeyAccWith = messageGenKey.substring(4);
                        String matrixKeyInter = messageGenKey.substring(2, 4);
                        if ((matrixKeyAccWith.equals("B4") && matrixKeyInter.equals("I1"))
                                || (!accWithDetailsExistFlag && !interDetailsExistFlag)) {
                            if ((payToBICCode == null) || (payToBICCode != null && !payToBICCode.equals(receiever))) {
                                String tempString = util.createSwiftTagString(payToBICCode, disposalObject.getSI_PayToAccInfo(),
                                        disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject.getSI_PayToText1(),
                                        disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3());
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
                                            disposalObject.getSI_IntermediatoryAccInfo(),
                                            disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                            disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
                                            disposalObject.getSI_IntermediatoryText3());
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                        intermediatory56a = tempString.substring(0, tempString.length() - 1);
                                        tag56 = tempString.substring(tempString.length() - 1);
                                    }
                                }
                            }
                            else {

                                String tempString = util.createSwiftTagString(payToBICCode, disposalObject.getSI_PayToAccInfo(),
                                        disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject.getSI_PayToText1(),
                                        disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    intermediatory56a = tempString.substring(0, tempString.length() - 1);
                                    tag56 = tempString.substring(tempString.length() - 1);
                                }
                            }
                            if (!interDetailsExistFlag || !flag72) {
                                String tempString = util.createSwiftTagString(accWithBICCode, disposalObject.getSI_AccWithAccInfo(),
                                        disposalObject.getSI_AccWithNAT_CLR_Code(), disposalObject.getSI_AccWithText1(),
                                        disposalObject.getSI_AccWithText2(), disposalObject.getSI_AccWithText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
                                    tag57 = tempString.substring(tempString.length() - 1);
                                }
                            }
                            else {

                                String tempString = util.createSwiftTagString(intermediaryBICCode,
                                        disposalObject.getSI_IntermediatoryAccInfo(),
                                        disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                        disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
                                        disposalObject.getSI_IntermediatoryText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
                                    tag57 = tempString.substring(tempString.length() - 1);
                                }
                            }
                        }
                        // TODO Till Here code should be reviewed - End
                    }
                    else {
                        senderssCorrspondent53 = mainAccCustDetails.getF_BICCODE();
                        tag53 = "A";

                        if (accWithBICCode.equals(receiever)) {
                            if (interDetailsExistFlag) {
                                String tempString = util.createSwiftTagString(intermediaryBICCode,
                                        disposalObject.getSI_IntermediatoryAccInfo(),
                                        disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                        disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
                                        disposalObject.getSI_IntermediatoryText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    receiversCorrspondent54 = tempString.substring(0, tempString.length() - 1);
                                    tag54 = tempString.substring(tempString.length() - 1);
                                }
                            }
                            else if (!payToBICCode.equals(receiever)) {
                                String tempString = util.createSwiftTagString(payToBICCode, disposalObject.getSI_PayToAccInfo(),
                                        disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject.getSI_PayToText1(),
                                        disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    receiversCorrspondent54 = tempString.substring(0, tempString.length() - 1);
                                    tag54 = tempString.substring(tempString.length() - 1);
                                }
                            }

                        }
                        if (intermediaryBICCode.equals(receiever)) {
                            String tempString = util.createSwiftTagString(payToBICCode, disposalObject.getSI_PayToAccInfo(),
                                    disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject.getSI_PayToText1(),
                                    disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3());
                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                receiversCorrspondent54 = tempString.substring(0, tempString.length() - 1);
                                tag54 = tempString.substring(tempString.length() - 1);
                            }
                        }
                        if (accWithDetailsExistFlag) {
                            if (intermediaryBICCode.equals(receiever) || payToBICCode.equals(receiever)) {
                                String tempString = util.createSwiftTagString(accWithBICCode, disposalObject.getSI_AccWithAccInfo(),
                                        disposalObject.getSI_AccWithNAT_CLR_Code(), disposalObject.getSI_AccWithText1(),
                                        disposalObject.getSI_AccWithText2(), disposalObject.getSI_AccWithText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {
                                    accWithInstitute57a = tempString.substring(0, tempString.length() - 1);
                                    tag57 = tempString.substring(tempString.length() - 1);
                                }
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
                    if (disposalObject.getSI_BankInstructionCode() != null
                            && disposalObject.getSI_BankInstructionCode().equals("CHQB"))
                        benCustomer59 = util.getForAccountInfoString(disposalObject, true);

                    else benCustomer59 = util.getForAccountInfoString(disposalObject, false);

                    messageObject_103.setBeneficiaryCustomer(benCustomer59);
                    messageObject_103.setRemittanceInfo(util.getTag70String(disposalObject));
                    String chargeCode = util.verifyForNull(disposalObject.getSI_ChargeCode());
                    // charge code
                    String chargeCode71 = null;
                    if (chargeCode.equals(EMPTYSTRING) || chargeCode == null) {
                        chargeCode71 = "SHA";
                    }
                    if (!chargeCode.equals(EMPTYSTRING))
                        chargeCode71 = chargeCode;
                    messageObject_103.setDetailsOfCharges(chargeCode71);

                    String Tag71F = CommonConstants.EMPTY_STRING;
                    if (disposalObject.getSI_SendersCharges().abs().compareTo(new BigDecimal(0.00)) >= 0) {
                        String Currency = disposalObject.getSenderChargeCurrency().equals(CommonConstants.EMPTY_STRING)
                                ? disposalObject.getMainAccCurrencyCode()
                                : disposalObject.getSenderChargeCurrency();
                        if (!chargeCode.equals("OUR")) {
                            Tag71F = Currency + util.DecimalRounding(disposalObject.getSI_SendersCharges().abs().toString(),
                                    util.noDecimalPlaces(Currency, env));
                        }
                    }
                    SendersCharges senderChargeDetails = new SendersCharges();
                    senderChargeDetails.setSenderCharge(Tag71F);
                    messageObject_103.addCharges(senderChargeDetails);
                    String senderinf72 = CommonConstants.EMPTY_STRING;
                    if (flag72) {

                        if (util.createSwiftTagString(disposalObject.getSI_IntermediatoryCode(),
                                disposalObject.getSI_IntermediatoryAccInfo(), disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
                                disposalObject.getSI_IntermediatoryText3()).trim().length() != 0
                                && util.createSwiftTagString(disposalObject.getSI_AccWithAccInfo(),
                                        disposalObject.getSI_AccWithCode(), disposalObject.getSI_AccWithNAT_CLR_Code(),
                                        disposalObject.getSI_AccWithText1(), disposalObject.getSI_AccWithText1(),
                                        disposalObject.getSI_AccWithText1()).trim().length() != 0) {
                            if (disposalObject.getSI_AccWithCode().trim().equals(CommonConstants.EMPTY_STRING)) {
                                if (!disposalObject.getSI_AccWithAccInfo().trim().equals(CommonConstants.EMPTY_STRING)) {
                                    senderinf72 = "/ACC/" + "$" + "/" + disposalObject.getSI_AccWithAccInfo() + "$" + "//"
                                            + disposalObject.getSI_AccWithText1() + "$" + "//" + disposalObject.getSI_AccWithText2()
                                            + "$" + "//" + disposalObject.getSI_AccWithText3() + "$";
                                    senderinf72 = senderinf72 + "/" + util.getBankToBankInfo(disposalObject);
                                }
                                else {
                                    senderinf72 = "/ACC/" + "$" + "//" + disposalObject.getSI_AccWithText1() + "$" + "//"
                                            + disposalObject.getSI_AccWithText2() + "$" + "//" + disposalObject.getSI_AccWithText3()
                                            + "$";
                                    senderinf72 = senderinf72 + "/" + util.getBankToBankInfo(disposalObject);
                                }
                            }
                            else {

                                if (!disposalObject.getSI_AccWithAccInfo().trim().equals(CommonConstants.EMPTY_STRING)) {
                                    senderinf72 = "/ACC/" + disposalObject.getSI_AccWithCode() + "$" + "/"
                                            + disposalObject.getSI_AccWithAccInfo() + "$" + "//"
                                            + disposalObject.getSI_AccWithText1() + "$" + "//" + disposalObject.getSI_AccWithText2()
                                            + "$" + "//" + disposalObject.getSI_AccWithText3() + "$";
                                    senderinf72 = senderinf72 + "/" + util.getBankToBankInfo(disposalObject);
                                }
                                else {
                                    senderinf72 = "/ACC/" + disposalObject.getSI_AccWithCode() + "$" + "//"
                                            + disposalObject.getSI_AccWithText1() + "$" + "//" + disposalObject.getSI_AccWithText2()
                                            + "$" + "//" + disposalObject.getSI_AccWithText3() + "$";
                                    senderinf72 = senderinf72 + "/" + util.getBankToBankInfo(disposalObject);
                                }

                            }
                        }

                    }
                    else {
                        senderinf72 = util.getBankToBankInfo(disposalObject);
                    }
                    String sender2ReceiverInfo72 = genericStringFormatter(senderinf72);
                    messageObject_103.setSenderToReceiverInfo(sender2ReceiverInfo72);

                    HashMap tag11sMap = new HashMap();
                    // for generating MT192 message
                    if (generateMT192) {
                        /*
                         * If the message type is 192 i.e cancellation the we are just setting the
                         * action tag as "C"
                         */

                        messageObject_103.setAction("C");

                    }
                    else if (disposalObject.getTransactionStatus().startsWith("AM")) {
                        /*
                         * If the Transaction Type is AMEND (Code_Word) i.e amendment then we are
                         * just setting the action tag as "A"
                         */
                        messageObject_103.setAction("A");
                    }
                    xmlTagValueMapList.add(messageObject_103);
                    setF_OUT_XMLTAGVALUEMAPLIST(xmlTagValueMapList);
                    setF_OUT_messageGenMatrix(messageGenKey);
                }
                else if (!generateMT202OrMT292) {
                    generateAnyMessage = false;
                }
            }
        }
        else {
            generateAnyMessage = false;
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
    private String getMessageGenMatrixKey(String contraAccCustBICCode, String payToBICCode, String intermediaryBICCode,
            String benBICCode, BankFusionEnvironment env) {
        // finding appropreate Mesaage generation from properties file
        // storing PX-IX-BX values

        StringBuffer tempBuffer = new StringBuffer();
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
        if (intermediaryBICCode != null && !intermediaryBICCode.equals(EMPTYSTRING)) {
            if (intermediaryBICCodeDetails != null && intermediaryBICCodeDetails.isF_BKEAUTH())
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
        if (benBICCode != null && !benBICCode.equals(EMPTYSTRING)) {
            if (benBICCodeDetails != null && benBICCodeDetails.isF_BKEAUTH())
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
     * This method returns customer name and address details string with "$" as delimiter between
     * fields.
     *
     * @param custCode
     * @param env
     * @return @
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
            ArrayList addressLinkList = (ArrayList) env.getFactory().findByQuery(IBOAddressLinks.BONAME, whereCluaseForAddressLink,
                    params, null);
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

    /**
     *
     * This method populates map from properties file
     *
     * @
     */
    private void populateMessageTypesMap() {
        InputStream is = null;
        Properties swiftMessageProps = new Properties();
        String configLocation = null;
        try {
            // configLocation = System.getProperty("BFconfigLocation",
            // CommonConstants.EMPTY_STRING);
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
                finalString.append(tempString[i]);
                finalString.append("$");

            }
        }
        else {
            for (int j = 0; j < tempString.length; j++) {
                finalString.append(tempString[j]);
                finalString.append("$");
            }
        }

        return finalString.substring(0, finalString.lastIndexOf("$"));
    }
}
