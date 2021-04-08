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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_Util;
import com.misys.ub.swift.UB_MT202;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MT202292Validate;
import com.trapedza.bankfusion.steps.refimpl.ISWT_MT202292Validate;

/**
 * @author hardikp
 * 
 */
public class SWT_MT202292ValidateFatom extends AbstractSWT_MT202292Validate implements ISWT_MT202292Validate {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(SWT_MT202292ValidateFatom.class.getName());

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
     * Desposal object
     */
    private SWT_DisposalObject disposalObject;

    private String messageType;
    private SWT_Util util = new SWT_Util();

    public SWT_MT202292ValidateFatom(BankFusionEnvironment env) {
        super(env);
    }

    private void init() {
        disposalObject = (SWT_DisposalObject) getF_IN_DisposalObject();
        generateMT202 = this.isF_IN_generateMT202().booleanValue();
        generateMT292 = this.isF_IN_generateMT292().booleanValue();
        generateMT103 = this.isF_IN_generateMT103().booleanValue();

    }

    public void process(BankFusionEnvironment env) {

        init();
        IBOSwtCustomerDetail contraAccCustDetails = null;
        IBOSwtCustomerDetail mainAccCustDetails = null;
        IBOSwtCustomerDetail clientCustDetails = null;
        IBOBicCodes receiverBicCodeDetails = null;
        UB_MT202 messageObject_202 = new UB_MT202();
        if (disposalObject != null) {
            Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
            boolean generate202 = SWT_DataCalculation.generateCategory2Message(
                    ((SWT_DisposalObject) disposalObject).getValueDate(), ((SWT_DisposalObject) disposalObject).getPostDate(), env,
                    ((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
                    new java.sql.Date(bankFusionSystemDate.getTime()), "202");
            if (generate202) {
                String receiever;
                boolean isBicAuthorised;
                try {
                    // contra account customer details
                    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getContraAccCustomerNumber());
                    // main account customer details
                    mainAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            disposalObject.getMainAccCustomerNumber());
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
                    // EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_ACCOUNT, new Object[] {
                    // e.getLocalizedMessage() }, new HashMap(), env);
                }

                if (isBicAuthorised) {
                    int msgStatus = util.updateFlagValues(env, 202, disposalObject.getDisposalRef());
                    setF_OUT_msgStatusFlag(new Integer(msgStatus));
                    String payToBICCode = util.verifyForNull(disposalObject.getSI_PayToBICCode());
                    String intermediaryBICCode = util.verifyForNull(disposalObject.getSI_IntermediatoryCode());
                    String accWithBICCode = util.verifyForNull(disposalObject.getSI_AccWithCode());

                    messageType = disposalObject.getMessageType();
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

                    if (generateMT202 || generateMT292) {
                        // setting up fatom output values if MR202 OR MT292 generation
                        if (generateMT202)
                            messageObject_202.setMessageType("MT202");
                        else messageObject_202.setMessageType("MT202");

                        messageObject_202.setDisposalRef(disposalObject.getDisposalRef());
                        IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
                        String sender = branchObj.getF_BICCODE();
                        messageObject_202.setSender(sender);
                        messageObject_202.setReceiver(receiever);

                        messageObject_202.setTransactionReferenceNumber(disposalObject.getCurrentDealNumber());
                        String Rate = disposalObject.getInterestOrExchangeRate().toString();
                        if (disposalObject.getInterestOrExchangeRate().compareTo(BigDecimal.ZERO) == 0) {
                            Rate = "0000";
                        }
                        String relatedDealNumber = relatedDealNumber(sender, clientCustDetails.getF_BICCODE(),
                                mainAccCustDetails.getF_BICCODE(), Rate, disposalObject.getCurrentDealNumber());

                        messageObject_202.setRelatedReference(relatedDealNumber);

                        if (disposalObject.getTransactionStatus().indexOf("ROL") != -1)
                            messageObject_202.setTdValueDate(disposalObject.getPostDate().toString());
                        else messageObject_202.setTdValueDate(disposalObject.getValueDate().toString());

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
                        else if (disposalObject.getDealOriginator().equals("1") || disposalObject.getDealOriginator().equals("2")
                                || disposalObject.getDealOriginator().equals("3") || disposalObject.getDealOriginator().equals("7")) {

                            String amount = util.DecimalRounding(disposalObject.getTransactionAmount().abs().toString(),
                                    util.noDecimalPlaces(disposalObject.getMainAccCurrencyCode(), env));

                            messageObject_202.setTdAmount(amount);
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
                                    disposalObject.getSI_OrdInstAccInfo(), null, disposalObject.getSI_OrdInstText1(),
                                    disposalObject.getSI_OrdInstText2(), disposalObject.getSI_OrdInstText3());
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
                            if ((interDetailsExistFlag && !payToBICCode.equals(mainAccCustDetails.getF_BICCODE()) && disposalObject
                                    .getDealOriginator().equals("F"))
                                    || (interDetailsExistFlag && !payToBICCode.equals(contraAccCustDetails.getF_BICCODE()))) {

                                String tempString = util.createSwiftTagString(payToBICCode, disposalObject.getSI_PayToAccInfo(),
                                        disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject.getSI_PayToText1(),
                                        disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                    string56 = tempString.substring(0, tempString.length() - 1);
                                    tag56 = tempString.substring(tempString.length() - 1);

                                }
                            }
                            if ((!interDetailsExistFlag && payToDetailsExistFlag && !payToBICCode.equals(mainAccCustDetails
                                    .getF_BICCODE()))
                                    || ((!interDetailsExistFlag && payToDetailsExistFlag && !payToBICCode
                                            .equals(contraAccCustDetails.getF_BICCODE())))) {
                                String tempString = util.createSwiftTagString(payToBICCode, disposalObject.getSI_PayToAccInfo(),
                                        disposalObject.getSI_PayToNAT_CLR_Code(), disposalObject.getSI_PayToText1(),
                                        disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                    string57 = tempString.substring(0, tempString.length() - 1);
                                    tag57 = tempString.substring(tempString.length() - 1);

                                }

                            }
                            else if (interDetailsExistFlag) {
                                String tempString = util.createSwiftTagString(intermediaryBICCode,
                                        disposalObject.getSI_IntermediatoryAccInfo(),
                                        disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                        disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
                                        disposalObject.getSI_IntermediatoryText3());
                                if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                    string57 = tempString.substring(0, tempString.length() - 1);
                                    tag57 = tempString.substring(tempString.length() - 1);

                                }
                            }
                            String tempString = util.createSwiftTagString(accWithBICCode, disposalObject.getSI_AccWithAccInfo(),
                                    disposalObject.getSI_AccWithNAT_CLR_Code(), disposalObject.getSI_AccWithText1(),
                                    disposalObject.getSI_AccWithText2(), disposalObject.getSI_AccWithText3());
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
                                        String tempString = util.createSwiftTagString(payToBICCode,
                                                disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
                                                disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                disposalObject.getSI_PayToText3());
                                        if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                            string57 = tempString.substring(0, tempString.length() - 1);
                                            tag57 = tempString.substring(tempString.length() - 1);

                                        }

                                    }
                                    String tempString = util.createSwiftTagString(accWithBICCode,
                                            disposalObject.getSI_AccWithAccInfo(), disposalObject.getSI_AccWithNAT_CLR_Code(),
                                            disposalObject.getSI_AccWithText1(), disposalObject.getSI_AccWithText2(),
                                            disposalObject.getSI_AccWithText3());
                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                        string58 = tempString.substring(0, tempString.length() - 1);
                                        tag58 = "A";

                                    }

                                }
                                else {
                                    if (matrixKeyAccWith.equalsIgnoreCase("B3")) {
                                        String tempString = util.createSwiftTagString(intermediaryBICCode,
                                                disposalObject.getSI_IntermediatoryAccInfo(),
                                                disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                                disposalObject.getSI_IntermediatoryText1(),
                                                disposalObject.getSI_IntermediatoryText2(),
                                                disposalObject.getSI_IntermediatoryText3());
                                        if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                            string57 = tempString.substring(0, tempString.length() - 1);
                                            tag57 = tempString.substring(tempString.length() - 1);

                                        }

                                        String tempString1 = util.createSwiftTagString(accWithBICCode,
                                                disposalObject.getSI_AccWithAccInfo(), disposalObject.getSI_AccWithNAT_CLR_Code(),
                                                disposalObject.getSI_AccWithText1(), disposalObject.getSI_AccWithText2(),
                                                disposalObject.getSI_AccWithText3());
                                        if (!tempString1.equals(CommonConstants.EMPTY_STRING)) {

                                            string58 = tempString1.substring(0, tempString1.length() - 1);
                                            tag58 = tempString1.substring(tempString1.length() - 1);

                                        }

                                    }
                                    else {
                                        String tempString = util.createSwiftTagString(payToBICCode,
                                                disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
                                                disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                disposalObject.getSI_PayToText3());
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
                                        String tempString = util.createSwiftTagString(payToBICCode,
                                                disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
                                                disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                disposalObject.getSI_PayToText3());
                                        if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                            string56 = tempString.substring(0, tempString.length() - 1);
                                            tag56 = tempString.substring(tempString.length() - 1);

                                        }
                                    }
                                    String tempString = util.createSwiftTagString(intermediaryBICCode,
                                            disposalObject.getSI_IntermediatoryAccInfo(),
                                            disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                            disposalObject.getSI_IntermediatoryText1(), disposalObject.getSI_IntermediatoryText2(),
                                            disposalObject.getSI_IntermediatoryText3());

                                    if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                        string57 = tempString.substring(0, tempString.length() - 1);
                                        tag57 = tempString.substring(tempString.length() - 1);

                                    }

                                    String tempString1 = util.createSwiftTagString(accWithBICCode,
                                            disposalObject.getSI_AccWithAccInfo(), disposalObject.getSI_AccWithNAT_CLR_Code(),
                                            disposalObject.getSI_AccWithText1(), disposalObject.getSI_AccWithText2(),
                                            disposalObject.getSI_AccWithText3());
                                    if (!tempString1.equals(CommonConstants.EMPTY_STRING)) {

                                        string58 = tempString1.substring(0, tempString1.length() - 1);
                                        tag58 = tempString1.substring(tempString1.length() - 1);

                                    }
                                }
                                else {
                                    if (matrixKeyInter.equalsIgnoreCase("I2")) {
                                        if (!payToBICCode.equals(contraAccCustDetails.getF_BICCODE())) {
                                            String tempString = util.createSwiftTagString(payToBICCode,
                                                    disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
                                                    disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                    disposalObject.getSI_PayToText3());
                                            if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                                string57 = tempString.substring(0, tempString.length() - 1);
                                                tag57 = tempString.substring(tempString.length() - 1);

                                            }

                                        }
                                        String tempString = util.createSwiftTagString(intermediaryBICCode,
                                                disposalObject.getSI_IntermediatoryAccInfo(),
                                                disposalObject.getSI_IntermediatoryNAT_CLR_Code(),
                                                disposalObject.getSI_IntermediatoryText1(),
                                                disposalObject.getSI_IntermediatoryText2(),
                                                disposalObject.getSI_IntermediatoryText3());
                                        if (!tempString.equals(CommonConstants.EMPTY_STRING)) {

                                            string58 = tempString.substring(0, tempString.length() - 1);
                                            tag58 = "A";

                                        }
                                    }
                                    else {
                                        String tempString = util.createSwiftTagString(payToBICCode,
                                                disposalObject.getSI_PayToAccInfo(), disposalObject.getSI_PayToNAT_CLR_Code(),
                                                disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(),
                                                disposalObject.getSI_PayToText3());
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
                        messageObject_202.setSendertoReceiverInformation(util.getBankToBankInfo(disposalObject));

                        /*
                         * message Object creation is pending for MT292 that why commenting the
                         * entire If block
                         */
                        if (generateMT292) {

                            messageObject_202.setAction("C");
                        }
                        else if (disposalObject.getTransactionStatus().startsWith("AM")) {

                            messageObject_202.setAction("A");
                        }

                        ArrayList xmlTagValueList = new ArrayList();
                        xmlTagValueList.add(messageObject_202);
                        setF_OUT_xmlTagValueList(xmlTagValueList);

                        setF_OUT_ispublish(util.IsPublish(disposalObject.getMessageType(), disposalObject.getConfirmationFlag(),
                                disposalObject.getCancelFlag()));
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
        setF_OUT_generateAnyMessage(Boolean.valueOf(generateAnyMessage));
        if (disposalObject != null) {
            setF_OUT_DisposalId(disposalObject.getDisposalRef());
        }
        else {
            setF_OUT_DisposalId("0");
        }
    }

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

    /*
     * private String calculateAmount(SWT_DisposalObject disposalObject, BankFusionEnvironment env)
     * {
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
}
