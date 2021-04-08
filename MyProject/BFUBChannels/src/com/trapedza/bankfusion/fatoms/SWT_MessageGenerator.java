/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.SWT_DataCalculation;
import com.misys.ub.swift.SWT_DisposalObject;
import com.misys.ub.swift.SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MessageGenerator;

public class SWT_MessageGenerator extends AbstractSWT_MessageGenerator {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    private final static String BF_CONFIG_LOCATION = "BFconfigLocation";
    private final static String CONF = "conf/swift/";
    private final static String MESSAGE_GENERATOR_CALLED_AND_EXECUTING_WITH_DEALNO = "message Generator is called and  executing the deal with Deal NO=";
    private final static String DISPOSAL_ID = "And the disposal id =";
    private final static String CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT = "Customer not available for the account ";
    private final static String ISOCURRENCY_CODE = "ISOCURRENCYCODE";
    private final static String VALUE_103 = "103";
    private final static String EMPTY_STRING = " ";
    private final static String NO_MICROFLOW_MENTIONED_IN_SWIFT = "No Microflow mentioned in the SWIFT.properties file for the Message type : ";
    private final static String DISPOSAL_OBJECT = "DisposalObject";
    private final static String VALUE_202 = "202";
    private final static String F = "F";
    private final static String VALUE_900 = "900";
    private final static String VALUE_210 = "210";

    private transient final static Log logger = LogFactory.getLog(SWT_MessageGenerator.class.getName());
    Properties messageMicroflowMap = new Properties();
    public static final String SWIFT_PROPERTY_FILENAME = "SWIFT.properties";

    /**
     * @param env
     */
    public SWT_MessageGenerator(BankFusionEnvironment env) {
        super(env);
    }

    /*
     * (non-Javadoc)
     *
     * @seecom.trapedza.bankfusion.steps.refimpl.AbstractSWT_MessageGenerator#process(com.trapedza.
     * bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        SWT_Util util = new SWT_Util();
        /**
         * /* Message Status values and what it means -1 - Posting is pending 0 - Messgae to be
         * generated 1 - Partial Sent 2 - Completed 3 - Rejected by MMM 4 - Corrected Rejected
         * message 5 - Corrected message sent
         */
        /*
         * String whereClause = "WHERE (" + IBOSWTDisposal.MESSAGESTATUS + " = '0' OR " +
         * IBOSWTDisposal.MESSAGESTATUS + " = '4') AND (" + IBOSWTDisposal.CANCELFLAG + " = '0'
         * OR" + IBOSWTDisposal.PAYMENTFLAG + " = '0' OR" + IBOSWTDisposal.CONFIRMATIONFLAG +
         * " = '0' OR" + IBOSWTDisposal.RECEIPTFLAG + " = '0')";
         */

        try {
            // String configLocation = System.getProperty(BF_CONFIG_LOCATION,
            // CommonConstants.EMPTY_STRING);
            String configLocation = GetUBConfigLocation.getUBConfigLocation();
            InputStream is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    CONF + SWIFT_PROPERTY_FILENAME, configLocation, BankFusionThreadLocal.getUserZone());
            messageMicroflowMap.load(is);
        }
        catch (Exception ex) {
            // new BankFusionException(9311, null, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_BASEEQUIVAL_WRONGLY_CAL, new Object[] {}, new HashMap(), env);
            logger.error(ExceptionUtil.getExceptionAsString(ex));
        }
        /*
         */
        // String query1 =
        // "SELECT T1.boID AS SWTDISPOSALID FROM SWTDisposal T1 WHERE T1.boID NOT IN ("
        // +
        // "SELECT T2.f_PREVSWTDISPOSALID FROM SWTDisposal T2 WHERE T2.f_DEALNO = ?) and T1.f_DEALNO
        // = ?";
        // */
        String whereClause1 = "WHERE (" + IBOSWTDisposal.MESSAGESTATUS + " = 0 OR " + IBOSWTDisposal.MESSAGESTATUS + " = 4 ) AND ("
                + IBOSWTDisposal.CANCELFLAG + " = 0 OR " + IBOSWTDisposal.PAYMENTFLAG + " = 0 OR " + IBOSWTDisposal.PAYMENTFLAG
                + " = 2 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 0 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 1 OR "
                + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 3 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 5 OR "
                + IBOSWTDisposal.CONFIRMATIONFLAG + " = 0 OR " + IBOSWTDisposal.RECEIPTFLAG + " = 0 OR "
                + IBOSWTDisposal.PAYMENTFLAG + " = 2) AND (" + IBOSWTDisposal.DEALNO + "='" + this.getF_IN_DealNumber() + "')";

        String whereClause2 = "WHERE (" + IBOSWTDisposal.MESSAGESTATUS + " = 0 OR " + IBOSWTDisposal.MESSAGESTATUS + " = 4 ) AND ("
                + IBOSWTDisposal.CANCELFLAG + " = 0 OR " + IBOSWTDisposal.PAYMENTFLAG + " = 0 OR " + IBOSWTDisposal.PAYMENTFLAG
                + " = 2 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 0 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 1 OR "
                + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 3 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 5 OR "
                + IBOSWTDisposal.CONFIRMATIONFLAG + " = 0 OR " + IBOSWTDisposal.RECEIPTFLAG + " = 0 OR "
                + IBOSWTDisposal.PAYMENTFLAG + " = 2) AND (" + IBOSWTDisposal.SWTDISPOSALID + "='" + this.getF_IN_DisposalId()
                + "')";

        String whereClause = "WHERE (" + IBOSWTDisposal.MESSAGESTATUS + " = 0 OR " + IBOSWTDisposal.MESSAGESTATUS + " = 4 OR "
                + IBOSWTDisposal.MESSAGESTATUS + " = 1 ) AND (" + IBOSWTDisposal.CANCELFLAG + " = 0 OR "
                + IBOSWTDisposal.PAYMENTFLAG + " = 0 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 0 OR "
                + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 1 OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 3 OR "
                + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = 5 OR " + IBOSWTDisposal.CONFIRMATIONFLAG + " = 0 OR "
                + IBOSWTDisposal.RECEIPTFLAG + " = 0)";

        List disposalList = null;
        //
        /*
         * if(this.getF_IN_DealNumber() != null && this.getF_IN_DealNumber().trim().length() > 0) {
         * ArrayList params = new ArrayList(); params.add(getF_IN_DealNumber());
         * params.add(getF_IN_DealNumber()); disposalList =
         * env.getFactory().executeGenericQuery(query1, params, null); } else {
         */
        if (this.getF_IN_DisposalId() != null && !this.getF_IN_DisposalId().trim().equals(CommonConstants.EMPTY_STRING)) {
            disposalList = env.getFactory().findByQuery(IBOSWTDisposal.BONAME, whereClause2, null);
        }
        else if (this.getF_IN_DealNumber() != null && !this.getF_IN_DealNumber().trim().equals(CommonConstants.EMPTY_STRING)) {
            disposalList = env.getFactory().findByQuery(IBOSWTDisposal.BONAME, whereClause1, null);
        }
        else {
            disposalList = env.getFactory().findByQuery(IBOSWTDisposal.BONAME, whereClause, null);
        }
        // }
        if (logger.isInfoEnabled()) {
            logger.info(MESSAGE_GENERATOR_CALLED_AND_EXECUTING_WITH_DEALNO + this.getF_IN_DealNumber() + DISPOSAL_ID
                    + this.getF_IN_DisposalId());
        }
        for (int i = 0; i < disposalList.size(); i++) {
            if (disposalList.get(i) != null) {
                IBOSWTDisposal disposalRecord = (IBOSWTDisposal) disposalList.get(i);

                BankFusionObject disposalObject = new SWT_DisposalObject();
                ((SWT_DisposalObject) disposalObject).setDisposalRef(disposalRecord.getBoID());
                ((SWT_DisposalObject) disposalObject).setBrokerNumber(disposalRecord.getF_BROKERCODE());
                ((SWT_DisposalObject) disposalObject).setCancelFlag(disposalRecord.getF_CANCELFLAG());
                ((SWT_DisposalObject) disposalObject).setConfirmationFlag(disposalRecord.getF_CONFIRMATIONFLAG());
                ((SWT_DisposalObject) disposalObject).setContraAccountNo(disposalRecord.getF_CONTRAACCOUNTID());
                ((SWT_DisposalObject) disposalObject).setContractAmount(disposalRecord.getF_CONTRACTAMOUNT());
                ((SWT_DisposalObject) disposalObject).setCurrentDealNumber(disposalRecord.getF_DEALNO());
                ((SWT_DisposalObject) disposalObject).setInterestAmount(disposalRecord.getF_INTERESTAMOUNT());
                ((SWT_DisposalObject) disposalObject).setInterestOrExchangeRate(disposalRecord.getF_INTERESTRATE());
                ((SWT_DisposalObject) disposalObject).setTerm(disposalRecord.getF_TERM() + CommonConstants.EMPTY_STRING);
                ((SWT_DisposalObject) disposalObject).setMainAccountNo(disposalRecord.getF_CUSTACCOUNTID());
                ((SWT_DisposalObject) disposalObject).setMaturityDate(disposalRecord.getF_MATURITYDATE());
                ((SWT_DisposalObject) disposalObject).setNextInterestDueDate(disposalRecord.getF_NEXTINTERESTDUEDT());
                ((SWT_DisposalObject) disposalObject).setInterestPeriodStartDate(disposalRecord.getF_INTERESTPERIODSTARTDT());
                ((SWT_DisposalObject) disposalObject).setInterestPeriodEndDate(disposalRecord.getF_INTERESTPERIODENDDT());
                ((SWT_DisposalObject) disposalObject).setMessageStatus(disposalRecord.getF_MESSAGESTATUS());
                ((SWT_DisposalObject) disposalObject).setMessageType(disposalRecord.getF_MESSAGETYPE());
                ((SWT_DisposalObject) disposalObject).setPaymentFlagMT202(disposalRecord.getF_PAYMENTFLAG());
                ((SWT_DisposalObject) disposalObject).setPostDate(disposalRecord.getF_POSTDATE());
                ((SWT_DisposalObject) disposalObject).setPreviousDealRecordNumber(disposalRecord.getF_PREVSWTDISPOSALID());
                ((SWT_DisposalObject) disposalObject).setReceiptFlagMT210(disposalRecord.getF_RECEIPTFLAG());
                // disposalObject.setRelatedDealNumber(disposalRecord.);
                ((SWT_DisposalObject) disposalObject).setRelatedDealNumber(CommonConstants.EMPTY_STRING);
                // ((SWT_DisposalObject)
                // disposalObject).setSI_AccWithAccInfo(disposalRecord.getF_BENEFICIARY_ACC_INFO());
                // ((SWT_DisposalObject)
                // disposalObject).setSI_AccWithCode(disposalRecord.getF_BENEFICIARY_CODE());
                // ((SWT_DisposalObject) disposalObject).setSI_AccWithNAT_CLR_Code(disposalRecord
                // .getF_BENEFICIARY_NAT_CLR_CODE());
                ((SWT_DisposalObject) disposalObject).setSI_AccWithText1(disposalRecord.getF_BENEFICIARY_TEXT1());
                ((SWT_DisposalObject) disposalObject).setSI_AccWithText2(disposalRecord.getF_BENEFICIARY_TEXT2());
                ((SWT_DisposalObject) disposalObject).setSI_AccWithText3(disposalRecord.getF_BENEFICIARY_TEXT3());
                ((SWT_DisposalObject) disposalObject).setSI_BankAddlInstrCode(disposalRecord.getF_BANK_ADDL_INSTRUCTION_CODE());
                ((SWT_DisposalObject) disposalObject).setSI_BankInstructionCode(disposalRecord.getF_BANK_INSTRUCTION_CODE());
                ((SWT_DisposalObject) disposalObject).setSI_BankOpCode(disposalRecord.getF_BANK_OPERATION_CODE());
                ((SWT_DisposalObject) disposalObject).setSI_BankToBankInfo1(disposalRecord.getF_BANK_TO_BANK_INFO1());
                ((SWT_DisposalObject) disposalObject).setSI_BankToBankInfo2(disposalRecord.getF_BANK_TO_BANK_INFO2());
                ((SWT_DisposalObject) disposalObject).setSI_BankToBankInfo3(disposalRecord.getF_BANK_TO_BANK_INFO3());
                ((SWT_DisposalObject) disposalObject).setSI_BankToBankInfo4(disposalRecord.getF_BANK_TO_BANK_INFO4());
                ((SWT_DisposalObject) disposalObject).setSI_BankToBankInfo5(disposalRecord.getF_BANK_TO_BANK_INFO5());
                ((SWT_DisposalObject) disposalObject).setSI_BankToBankInfo6(disposalRecord.getF_BANK_TO_BANK_INFO6());
                ((SWT_DisposalObject) disposalObject).setSI_ChargeCode(disposalRecord.getF_CHARGECODE());
                // ((SWT_DisposalObject)
                // disposalObject).setSI_ForAccountInfo(disposalRecord.getF_FOR_ACCOUNT_INFO());
                ((SWT_DisposalObject) disposalObject).setSI_ForAccountText1(disposalRecord.getF_FOR_ACCOUNT_TEXT1());
                ((SWT_DisposalObject) disposalObject).setSI_ForAccountText2(disposalRecord.getF_FOR_ACCOUNT_TEXT2());
                ((SWT_DisposalObject) disposalObject).setSI_ForAccountText3(disposalRecord.getF_FOR_ACCOUNT_TEXT3());
                // ((SWT_DisposalObject) disposalObject).setSI_IntermediatoryAccInfo(disposalRecord
                // .getF_INTERMEDIARY_ACC_INFO());
                ((SWT_DisposalObject) disposalObject).setSI_IntermediatoryCode(disposalRecord.getF_INTERMEDIARY_CODE());
                // ((SWT_DisposalObject)
                // disposalObject).setSI_IntermediatoryNAT_CLR_Code(disposalRecord
                // .getF_INTERMEDIARY_NAT_CLR_CODE());
                ((SWT_DisposalObject) disposalObject).setPayReceiveFlag(disposalRecord.getF_PAY_RECEIVE_FLAG());

                ((SWT_DisposalObject) disposalObject).setSI_IntermediatoryText1(disposalRecord.getF_INTERMEDIARY_TEXT1());
                ((SWT_DisposalObject) disposalObject).setSI_IntermediatoryText2(disposalRecord.getF_INTERMEDIARY_TEXT2());
                ((SWT_DisposalObject) disposalObject).setSI_IntermediatoryText3(disposalRecord.getF_INTERMEDIARY_TEXT3());

                ((SWT_DisposalObject) disposalObject).setSI_OrdCustAccInfo(disposalRecord.getF_ORDERINGCUSTDTL1());
                // disposalObject.setSI_OrdCustBICCode(disposalRecord.getF_ORDERINGCUSTOMER_CODE());
                ((SWT_DisposalObject) disposalObject).setSI_OrdCustText1(disposalRecord.getF_ORDERINGCUSTDTL2());
                ((SWT_DisposalObject) disposalObject).setSI_OrdCustText2(disposalRecord.getF_ORDERINGCUSTDTL3());
                ((SWT_DisposalObject) disposalObject).setSI_OrdCustText3(disposalRecord.getF_ORDERINGCUSTDTL4());

                /*
                 * ((SWT_DisposalObject)disposalObject).setSI_OrdCustAccInfo(CommonConstants.
                 * EMPTY_STRING );
                 * ((SWT_DisposalObject)disposalObject).setSI_OrdCustText1(CommonConstants.
                 * EMPTY_STRING );
                 * ((SWT_DisposalObject)disposalObject).setSI_OrdCustText2(CommonConstants.
                 * EMPTY_STRING );
                 * ((SWT_DisposalObject)disposalObject).setSI_OrdCustText3(CommonConstants.
                 * EMPTY_STRING );
                 */
                ((SWT_DisposalObject) disposalObject).setSI_OrdCustBICCode(CommonConstants.EMPTY_STRING);

                ((SWT_DisposalObject) disposalObject).setSI_OrdInstAccInfo(disposalRecord.getF_ORDERINGINSTITUTE_ACC_INFO());
                ((SWT_DisposalObject) disposalObject).setSI_OrdInstBICCode(disposalRecord.getF_ORDERINGINSTITUTE_CODE());
                ((SWT_DisposalObject) disposalObject).setSI_OrdInstText1(disposalRecord.getF_ORDERINGINSTITUTE_TEXT1());
                ((SWT_DisposalObject) disposalObject).setSI_OrdInstText2(disposalRecord.getF_ORDERINGINSTITUTE_TEXT2());
                ((SWT_DisposalObject) disposalObject).setSI_OrdInstText3(disposalRecord.getF_ORDERINGINSTITUTE_TEXT3());
                ((SWT_DisposalObject) disposalObject).setSI_PayDetails1(disposalRecord.getF_PAY_DETAILS1());
                ((SWT_DisposalObject) disposalObject).setSI_PayDetails2(disposalRecord.getF_PAY_DETAILS2());
                ((SWT_DisposalObject) disposalObject).setSI_PayDetails3(disposalRecord.getF_PAY_DETAILS3());
                ((SWT_DisposalObject) disposalObject).setSI_PayDetails4(disposalRecord.getF_PAY_DETAILS4());
                ((SWT_DisposalObject) disposalObject).setSI_PayReceiveFlag(disposalRecord.getF_PAY_RECEIVE_FLAG());
                // ((SWT_DisposalObject)
                // disposalObject).setSI_PayToAccInfo(disposalRecord.getF_PAY_TO_ACC_INFO());
                ((SWT_DisposalObject) disposalObject).setSI_PayToBICCode(disposalRecord.getF_PAY_TO_CODE());
                // ((SWT_DisposalObject) disposalObject)
                // .setSI_PayToNAT_CLR_Code(disposalRecord.getF_PAY_TO_NAT_CLR_CODE());
                ((SWT_DisposalObject) disposalObject).setSI_PayToText1(disposalRecord.getF_PAY_TO_TEXT1());
                ((SWT_DisposalObject) disposalObject).setSI_PayToText2(disposalRecord.getF_PAY_TO_TEXT2());
                ((SWT_DisposalObject) disposalObject).setSI_PayToText3(disposalRecord.getF_PAY_TO_TEXT3());
                ((SWT_DisposalObject) disposalObject).setSI_SendersCharges(disposalRecord.getF_SENDERCHARGEAMOUNT());
                ((SWT_DisposalObject) disposalObject).setTransactionAmount(disposalRecord.getF_TRANSACTIONAMOUNT());
                ((SWT_DisposalObject) disposalObject).setTransactionStatus(disposalRecord.getF_TRANSACTIONSTATUS());
                ((SWT_DisposalObject) disposalObject).setValueDate(disposalRecord.getF_VALUEDATE());
                ((SWT_DisposalObject) disposalObject).setVerifyFlag(Integer.parseInt(disposalRecord.getF_VERIFYFLAG()));
                ((SWT_DisposalObject) disposalObject).setCrdrFlag(disposalRecord.getF_CRDRCONFIRMATIONFLAG());
                ((SWT_DisposalObject) disposalObject).setDealOriginator(disposalRecord.getF_DEALORIGINATOR());
                ((SWT_DisposalObject) disposalObject).setSenderChargeCurrency(disposalRecord.getF_SENDERCHARGECURRENCYCODE());
                ((SWT_DisposalObject) disposalObject).setClientNumber(disposalRecord.getF_CUSTOMERCODE());
                ((SWT_DisposalObject) disposalObject).setPartyIdentifier(disposalRecord.getF_PARTYIDENTIFIER());
                ((SWT_DisposalObject) disposalObject).setPartyIdentifierAdd1(disposalRecord.getF_PARTYADDRESSLINE1());
                ((SWT_DisposalObject) disposalObject).setPartyIdentifierAdd2(disposalRecord.getF_PARTYADDRESSLINE2());
                ((SWT_DisposalObject) disposalObject).setPartyIdentifierAdd3(disposalRecord.getF_PARTYADDRESSLINE3());
                ((SWT_DisposalObject) disposalObject).setPartyIdentifierAdd4(disposalRecord.getF_PARTYADDRESSLINE4());
                ((SWT_DisposalObject) disposalObject).setOrderingInstitution(disposalRecord.getF_ORDERINGINSTITUTION());
                ArrayList params = new ArrayList();
                List list = null;
                SimplePersistentObject simplePersistentObject = null;
                final String fetchCurrQuery = " SELECT T1.f_ISOCURRENCYCODE AS ISOCURRENCYCODE FROM " + IBOAccount.BONAME
                        + " T1 where T1.boID = ?";
                String customerCode = FinderMethods.findCustomerCodeByAccount(disposalRecord.getF_CUSTACCOUNTID());
                if (customerCode != null) {
                    ((SWT_DisposalObject) disposalObject).setMainAccCustomerNumber(customerCode);
                }
                else {
                    logger.error(CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT + disposalRecord.getF_CUSTACCOUNTID());
                }

                IBOAccount accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME,
                        disposalRecord.getF_CUSTACCOUNTID(), false);
                String mainCurrencyCode = accountBO.getF_ISOCURRENCYCODE();
                customerCode = FinderMethods.findCustomerCodeByAccount(disposalRecord.getF_CONTRAACCOUNTID());
                if (customerCode != null) {
                    // SimplePersistentObject mainCustConfig = (SimplePersistentObject)
                    // customerConfigDetail.get(0);
                    ((SWT_DisposalObject) disposalObject).setContraAccCustomerNumber(customerCode);
                }
                else {
                    logger.error(CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT + disposalRecord.getF_CONTRAACCOUNTID());
                }

                list = env.getFactory().executeGenericQuery(fetchCurrQuery, params, null);
                simplePersistentObject = (SimplePersistentObject) list.get(0);
                String contraCurrecnyCode = (String) simplePersistentObject.getDataMap().get(ISOCURRENCY_CODE);

                ((SWT_DisposalObject) disposalObject).setContraAccCurrencyCode(contraCurrecnyCode);
                ((SWT_DisposalObject) disposalObject).setMainAccCurrencyCode(mainCurrencyCode);
                String microflowName = EMPTY_STRING;
                if (disposalRecord.getF_CONFIRMATIONFLAG() == 0 || disposalRecord.getF_CANCELFLAG() == 0) {
                    microflowName = messageMicroflowMap.getProperty(disposalRecord.getF_MESSAGETYPE());
                    Timestamp bankFusionSystemDate2 = SystemInformationManager.getInstance().getBFBusinessDateTime();
                    if (disposalRecord.getF_MESSAGETYPE().equalsIgnoreCase(VALUE_103)) {
                        boolean generate103 = SWT_DataCalculation.generateCategory2Message(
                                ((SWT_DisposalObject) disposalObject).getValueDate(),
                                ((SWT_DisposalObject) disposalObject).getPostDate(), env,
                                ((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
                                new java.sql.Date(bankFusionSystemDate2.getTime()), VALUE_103);

                        if (generate103) {
                            if (microflowName == null || microflowName.trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                                }
                            }
                            else {
                                Hashtable swtBPParams = new Hashtable();
                                swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                                // Execute the Post Loan All Interest Microflow.
                                MFExecuter.executeMF(microflowName, env, swtBPParams);
                            }
                        }
                    }
                    else {
                        if (microflowName == null || microflowName.trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                            if (logger.isInfoEnabled()) {
                                logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                            }
                        }
                        else {
                            Hashtable swtBPParams = new Hashtable();
                            swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                            // Execute the Post Loan All Interest Microflow.
                            MFExecuter.executeMF(microflowName, env, swtBPParams);
                        }
                    }
                }
                else {
                    Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
                    boolean generate202 = SWT_DataCalculation.generateCategory2Message(
                            ((SWT_DisposalObject) disposalObject).getValueDate(),
                            ((SWT_DisposalObject) disposalObject).getPostDate(), env,
                            ((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
                            new java.sql.Date(bankFusionSystemDate.getTime()), VALUE_202);

                    if (generate202) {
                        if (disposalRecord.getF_PAYMENTFLAG() == 0 || disposalRecord.getF_PAYMENTFLAG() == 2) {
                            microflowName = messageMicroflowMap.getProperty(VALUE_202);

                            if (microflowName == null || microflowName.trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                                }
                            }
                            else {
                                Hashtable swtBPParams = new Hashtable();
                                swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                                // Execute the Post Loan All Interest Microflow.
                                MFExecuter.executeMF(microflowName, env, swtBPParams);
                            }
                        }
                    }
                    String dealOriginator = ((SWT_DisposalObject) disposalObject).getDealOriginator();
                    Date date = null;
                    Timestamp bankFusionSystemDate1 = SystemInformationManager.getInstance().getBFBusinessDateTime();
                    String messageType = ((SWT_DisposalObject) disposalObject).getMessageType();
                    String Currency = null;
                    if (!(dealOriginator.equalsIgnoreCase(F))) {
                        date = ((SWT_DisposalObject) disposalObject).getValueDate();
                        Currency = ((SWT_DisposalObject) disposalObject).getMainAccCurrencyCode();
                    }
                    else {
                        date = ((SWT_DisposalObject) disposalObject).getMaturityDate();
                        Currency = ((SWT_DisposalObject) disposalObject).getContraAccCurrencyCode();
                        ;
                    }

                    boolean generate210 = SWT_DataCalculation.generateCategory2Message(date,
                            ((SWT_DisposalObject) disposalObject).getPostDate(), env, Currency,
                            new java.sql.Date(bankFusionSystemDate1.getTime()), VALUE_210);

                    if (generate210) {

                        if (disposalRecord.getF_RECEIPTFLAG() == 0) {
                            microflowName = messageMicroflowMap.getProperty(VALUE_210);

                            if (microflowName == null || microflowName.trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                                }
                            }
                            else {
                                Hashtable swtBPParams = new Hashtable();
                                swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                                // Execute the Post Loan All Interest Microflow.
                                MFExecuter.executeMF(microflowName, env, swtBPParams);
                            }
                        }
                    }

                    if (disposalRecord.getF_CRDRCONFIRMATIONFLAG() == 0 || disposalRecord.getF_CRDRCONFIRMATIONFLAG() == 1) {
                        microflowName = messageMicroflowMap.getProperty(VALUE_900);

                        if (microflowName == null || microflowName.trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                            if (logger.isInfoEnabled()) {
                                logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                            }
                        }
                        else {
                            Hashtable swtBPParams = new Hashtable();
                            swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                            // Execute the Post Loan All Interest Microflow.
                            MFExecuter.executeMF(microflowName, env, swtBPParams);
                        }
                    }
                }

            }
            else {
                continue;
            }
        }
    }

}
