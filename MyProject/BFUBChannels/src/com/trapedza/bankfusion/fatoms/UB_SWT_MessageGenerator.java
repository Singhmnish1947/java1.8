/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.swift.UB_SWT_DisposalObject;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MessageGenerator;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_MessageGenerator;

/**
 * @author Shaileja
 *
 */

public class UB_SWT_MessageGenerator extends AbstractUB_SWT_MessageGenerator implements IUB_SWT_MessageGenerator {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private final static String CONF = "conf/swift/";
    private final static String MESSAGE_GENERATOR_CALLED_AND_EXECUTING_WITH_DEALNO = "message Generator is called and  executing the deal with Deal NO=";
    private final static String DISPOSAL_ID = "And the disposal id =";
    private final static String CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT = "Customer not available for the account ";
    private final static String ISOCURRENCY_CODE = "ISOCURRENCYCODE";
    private final static String VALUE_103 = "103";
    private final static String NO_MICROFLOW_MENTIONED_IN_SWIFT = "No Microflow mentioned in the SWIFT.properties file for the Message type : ";
    private final static String DISPOSAL_OBJECT = "DisposalObject";
    private final static String VALUE_202 = "202";
    private final static String F = "F";
    private final static String VALUE_201 = "210";
    private final static String VALUE_900 = "900";
    private final static String AMEND = "AMEND";
    private final static String AM = "AM";
    private final static String GENERATEDMT292 = "generateMT292";
    private final static String ISCANCELFORAMEND = "isCancelForAmend";
    String customerCode = CommonConstants.EMPTY_STRING;
    transient IBOAccount accountBO;
    String mainCurrencyCode = CommonConstants.EMPTY_STRING;
    String contraCurrecnyCode = CommonConstants.EMPTY_STRING;
    private transient final static Log logger = LogFactory.getLog(UB_SWT_MessageGenerator.class.getName());
    private Properties messageMicroflowMap = new Properties();

    private static final String SWIFT_PROPERTY_FILENAME = "SWIFT.properties";

    private final static String WHERE_CLAUSE_1 = "WHERE (" + IBOSWTDisposal.MESSAGESTATUS + " = ? OR "
            + IBOSWTDisposal.MESSAGESTATUS + " = ? ) AND (" + IBOSWTDisposal.CANCELFLAG + " = ? OR " + IBOSWTDisposal.PAYMENTFLAG
            + " = ? OR " + IBOSWTDisposal.PAYMENTFLAG + " = ? OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR "
            + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR "
            + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR " + IBOSWTDisposal.CONFIRMATIONFLAG + " = ? OR "
            + IBOSWTDisposal.RECEIPTFLAG + " = ? OR " + IBOSWTDisposal.PAYMENTFLAG + " = ?) AND (" + IBOSWTDisposal.DEALNO + "= ?)";

    private final static String WHERE_CLAUSE = "WHERE (" + IBOSWTDisposal.MESSAGESTATUS + " = ? OR " + IBOSWTDisposal.MESSAGESTATUS
            + " = ? OR " + IBOSWTDisposal.MESSAGESTATUS + " = ? ) AND (" + IBOSWTDisposal.CANCELFLAG + " = ? OR "
            + IBOSWTDisposal.PAYMENTFLAG + " = ? OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR "
            + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR " + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR "
            + IBOSWTDisposal.CRDRCONFIRMATIONFLAG + " = ? OR " + IBOSWTDisposal.CONFIRMATIONFLAG + " = ? OR "
            + IBOSWTDisposal.RECEIPTFLAG + " = ?)";

    /**
     * @param env
     */
    public UB_SWT_MessageGenerator(BankFusionEnvironment env) {
        super(env);
    }


    public void process(BankFusionEnvironment env) {
        UB_SWT_Util util = new UB_SWT_Util();
        /**
         * /* Message Status values and what it means 1 - Posting is pending 0 - Messgae to be
         * generated 1 - Partial Sent 2 - Completed 3 - Rejected by MMM 4 - Corrected Rejected
         * message 5 - Corrected message sent
         */
        loadSwiftProperties(env);

        List disposalList = null;
        if (StringUtils.isNotBlank(this.getF_IN_DisposalId())) {
            disposalList = getDisposalDetails(
                    StringUtils.isNotBlank(this.getF_IN_DisposalId()) ? this.getF_IN_DisposalId() : this.getF_IN_DealNumber(), env);
        }
        else if (StringUtils.isNotBlank(this.getF_IN_DealNumber())) {
            ArrayList param = new ArrayList();
            param.clear();
            param.add(0);
            param.add(4);
            param.add(0);
            param.add(0);
            param.add(2);
            param.add(0);
            param.add(1);
            param.add(3);
            param.add(5);
            param.add(0);
            param.add(0);
            param.add(2);
            param.add(StringUtils.isNotBlank(this.getF_IN_DisposalId()) ? this.getF_IN_DisposalId() : this.getF_IN_DealNumber());

            disposalList = env.getFactory().findByQuery(IBOSWTDisposal.BONAME, WHERE_CLAUSE_1, param, null);
        }
        else {
            ArrayList parameter = new ArrayList();
            parameter.clear();
            parameter.add(0);
            parameter.add(4);
            parameter.add(1);
            parameter.add(0);
            parameter.add(0);
            parameter.add(0);
            parameter.add(1);
            parameter.add(3);
            parameter.add(5);
            parameter.add(0);
            parameter.add(0);

            disposalList = env.getFactory().findByQuery(IBOSWTDisposal.BONAME, WHERE_CLAUSE, parameter, null);
        }

        if (logger.isInfoEnabled()) {
            logger.info(MESSAGE_GENERATOR_CALLED_AND_EXECUTING_WITH_DEALNO + this.getF_IN_DealNumber() + DISPOSAL_ID
                    + this.getF_IN_DisposalId());
        }

        for (int i = 0; i < disposalList.size(); i++) {
            if (disposalList.get(i) != null) {
                IBOSWTDisposal disposalRecord = (IBOSWTDisposal) disposalList.get(i);

                BankFusionObject disposalObject = getDisposalObject(disposalRecord);
                
                ArrayList params = new ArrayList();
                List list = null;
                SimplePersistentObject simplePersistentObject = null;
                final String fetchCurrQuery = " SELECT T1.f_ISOCURRENCYCODE AS ISOCURRENCYCODE FROM " + IBOAccount.BONAME
                        + " T1 where T1.boID = ?";
                String microflowName = StringUtils.EMPTY;
                if (disposalRecord.getF_CONFIRMATIONFLAG() == 0 || disposalRecord.getF_CANCELFLAG() == 0) {
                    microflowName = messageMicroflowMap.getProperty(disposalRecord.getF_MESSAGETYPE());
                    Timestamp bankFusionSystemDate2 = SystemInformationManager.getInstance().getBFBusinessDateTime();
                    if (disposalRecord.getF_MESSAGETYPE().equalsIgnoreCase(VALUE_103)) {

                        boolean generate103 = util.generateCategory2Message(
                                ((UB_SWT_DisposalObject) disposalObject).getValueDate(), ((UB_SWT_DisposalObject) disposalObject)
                                        .getPostDate(), env, ((UB_SWT_DisposalObject) disposalObject).getContraAccCurrencyCode(),
                                new java.sql.Date(bankFusionSystemDate2.getTime()), VALUE_103);

                        if (generate103) {
                            if (StringUtils.isBlank(microflowName)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                                }
                            }
                            else {
                                HashMap swtBPParams = new HashMap();
                                swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                                // Execute the Post Loan All Interest Microflow.
                                MFExecuter.executeMF(microflowName, env, swtBPParams);
                            }
                        }
                    }
                    else {
                        if (StringUtils.isBlank(microflowName)) {
                            if (logger.isInfoEnabled()) {
                                logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                            }
                        }
                        else {
                            HashMap swtBPParams = new HashMap();
                            swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                            // Execute the Post Loan All Interest Microflow.
                            MFExecuter.executeMF(microflowName, env, swtBPParams);
                        }
                    }
                }
                else {
                    Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
                    boolean generate202 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject).getValueDate(),
                            ((UB_SWT_DisposalObject) disposalObject).getPostDate(), env, ((UB_SWT_DisposalObject) disposalObject)
                                    .getContraAccCurrencyCode(), new java.sql.Date(bankFusionSystemDate.getTime()), VALUE_202);

                    if (generate202) {
                        if (disposalRecord.getF_PAYMENTFLAG() == 0 || disposalRecord.getF_PAYMENTFLAG() == 2) {
                            microflowName = messageMicroflowMap.getProperty(VALUE_202);

                            if (StringUtils.isBlank(microflowName)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                                }
                            }
                            else {
                                HashMap swtBPParams = new HashMap();
                                swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                                // Execute the Post Loan All Interest Microflow.
                                MFExecuter.executeMF(microflowName, env, swtBPParams);
                            }
                        }
                    }
                    String dealOriginator = ((UB_SWT_DisposalObject) disposalObject).getDealOriginator();
                    java.sql.Date date = null;
                    Timestamp bankFusionSystemDate1 = SystemInformationManager.getInstance().getBFBusinessDateTime();

                    String Currency = null;
                    if (!(dealOriginator.equalsIgnoreCase(F))) {
                        date = ((UB_SWT_DisposalObject) disposalObject).getValueDate();
                        Currency = ((UB_SWT_DisposalObject) disposalObject).getMainAccCurrencyCode();
                    }
                    else {
                        date = ((UB_SWT_DisposalObject) disposalObject).getMaturityDate();
                        Currency = ((UB_SWT_DisposalObject) disposalObject).getContraAccCurrencyCode();
                    }

                    boolean generate210 = util.generateCategory2Message(date, ((UB_SWT_DisposalObject) disposalObject)
                            .getPostDate(), env, Currency, new java.sql.Date(bankFusionSystemDate1.getTime()), VALUE_201);

                    if (generate210) {
                        if (disposalRecord.getF_RECEIPTFLAG() == 0) {
                            microflowName = messageMicroflowMap.getProperty(VALUE_201);

                            if (StringUtils.isBlank(microflowName)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                                }
                            }
                            else {
                                HashMap swtBPParams = new HashMap();
                                swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                                // Execute the Post Loan All Interest Microflow.
                                MFExecuter.executeMF(microflowName, env, swtBPParams);
                            }
                        }
                    }

                    if (disposalRecord.getF_CRDRCONFIRMATIONFLAG() == 0 || disposalRecord.getF_CRDRCONFIRMATIONFLAG() == 1) {
                        microflowName = messageMicroflowMap.getProperty(VALUE_900);

                        if (StringUtils.isBlank(microflowName)) {
                            if (logger.isInfoEnabled()) {
                                logger.info(NO_MICROFLOW_MENTIONED_IN_SWIFT + disposalRecord.getF_MESSAGETYPE());
                            }
                        }
                        else {
                            HashMap swtBPParams = new HashMap();
                            swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                            // Execute the Post Loan All Interest Microflow.
                            MFExecuter.executeMF(microflowName, env, swtBPParams);
                        }
                    }
                }
                util.isSwiftNostro(disposalRecord.getF_CONTRAACCOUNTID(), env);
                HashMap swtBPParams = new HashMap();
                if ((disposalRecord.getF_TRANSACTIONSTATUS().equals(AMEND) || disposalRecord.getF_TRANSACTIONSTATUS()
                        .startsWith(AM))
                        && (disposalRecord.getF_CANCELFLAG() != 0 && !util
                                .isSwiftNostro(disposalRecord.getF_CONTRAACCOUNTID(), env))) {

                    disposalObject = util.getDisposalHistoryRecord(disposalRecord.getBoID(), env);
                    params.clear();
                    params.add(((UB_SWT_DisposalObject) disposalObject).getMainAccountNo());
                    
                    customerCode = FinderMethods.findCustomerCodeByAccount(((UB_SWT_DisposalObject) disposalObject)
                            .getMainAccountNo());
                    if (customerCode != null) {
                        ((UB_SWT_DisposalObject) disposalObject).setMainAccCustomerNumber(customerCode);
                    }
                    else {
                            logger.error(CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT + disposalRecord.getF_CUSTACCOUNTID());
                    }

                    params.clear();
                    params.add(((UB_SWT_DisposalObject) disposalObject).getContraAccountNo());
                    accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME,
                            disposalRecord.getF_CONTRAACCOUNTID(), false);
                    mainCurrencyCode = accountBO.getF_ISOCURRENCYCODE();
                    customerCode = FinderMethods.findCustomerCodeByAccount(((UB_SWT_DisposalObject) disposalObject)
                            .getContraAccountNo());
                    if (customerCode != null) {
                        ((UB_SWT_DisposalObject) disposalObject).setContraAccCustomerNumber(customerCode);
                    }
                    else {
                            logger.error(CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT + disposalRecord.getF_CONTRAACCOUNTID());
                    }

                    list = env.getFactory().executeGenericQuery(fetchCurrQuery, params, null);
                    simplePersistentObject = (SimplePersistentObject) list.get(0);
                    contraCurrecnyCode = (String) simplePersistentObject.getDataMap().get(ISOCURRENCY_CODE);
                    ((UB_SWT_DisposalObject) disposalObject).setContraAccCurrencyCode(contraCurrecnyCode);
                    ((UB_SWT_DisposalObject) disposalObject).setMainAccCurrencyCode(mainCurrencyCode);
                    if (((UB_SWT_DisposalObject) disposalObject).getPaymentFlagMT202() == 1) {
                        microflowName = messageMicroflowMap.getProperty(VALUE_202);
                        ((UB_SWT_DisposalObject) disposalObject).setCancelFlag(0);
                        swtBPParams.clear();
                        swtBPParams.put(GENERATEDMT292, true);
                        swtBPParams.put(ISCANCELFORAMEND, true);
                        swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                        MFExecuter.executeMF(microflowName, env, swtBPParams);
                    }
                    if (((UB_SWT_DisposalObject) disposalObject).getReceiptFlagMT210() == 1) {
                        swtBPParams.clear();
                        ((UB_SWT_DisposalObject) disposalObject).setCancelFlag(0);
                        swtBPParams.put(ISCANCELFORAMEND, true);
                        microflowName = messageMicroflowMap.getProperty(VALUE_201);
                        swtBPParams.put(DISPOSAL_OBJECT, disposalObject);
                        MFExecuter.executeMF(microflowName, env, swtBPParams);
                    }
                }

            }
            else {
                continue;
            }
        }
    }

    /**
     * Method Description:Prepare swift disposal Object
     * 
     * @param disposalRecord
     * @return
     */
    public BankFusionObject getDisposalObject(IBOSWTDisposal disposalRecord) {
        BankFusionObject disposalObject = new UB_SWT_DisposalObject();
        BankFusionEnvironment env = BankFusionThreadLocal.getBankFusionEnvironment();
        ((UB_SWT_DisposalObject) disposalObject).setDisposalRef(disposalRecord.getBoID());
        ((UB_SWT_DisposalObject) disposalObject).setBrokerNumber(disposalRecord.getF_BROKERCODE());
        ((UB_SWT_DisposalObject) disposalObject).setDraftNumber( disposalRecord.getF_DRAFTNUMBER().toString());
        ((UB_SWT_DisposalObject) disposalObject).setCancelFlag(disposalRecord.getF_CANCELFLAG());
        ((UB_SWT_DisposalObject) disposalObject).setConfirmationFlag(disposalRecord.getF_CONFIRMATIONFLAG());
        ((UB_SWT_DisposalObject) disposalObject).setContraAccountNo(disposalRecord.getF_CONTRAACCOUNTID());
        ((UB_SWT_DisposalObject) disposalObject).setContractAmount(disposalRecord.getF_CONTRACTAMOUNT());
        ((UB_SWT_DisposalObject) disposalObject).setCurrentDealNumber(disposalRecord.getF_DEALNO());
        ((UB_SWT_DisposalObject) disposalObject).setInterestAmount(disposalRecord.getF_INTERESTAMOUNT());
        ((UB_SWT_DisposalObject) disposalObject).setInterestOrExchangeRate(disposalRecord.getF_INTERESTRATE());
        ((UB_SWT_DisposalObject) disposalObject).setTerm(disposalRecord.getF_TERM() + CommonConstants.EMPTY_STRING);
        ((UB_SWT_DisposalObject) disposalObject).setMainAccountNo(disposalRecord.getF_CUSTACCOUNTID());
        ((UB_SWT_DisposalObject) disposalObject).setMaturityDate(disposalRecord.getF_MATURITYDATE());
        ((UB_SWT_DisposalObject) disposalObject).setNextInterestDueDate(disposalRecord.getF_NEXTINTERESTDUEDT());
        ((UB_SWT_DisposalObject) disposalObject).setInterestPeriodStartDate(disposalRecord.getF_INTERESTPERIODSTARTDT());
        ((UB_SWT_DisposalObject) disposalObject).setInterestPeriodEndDate(disposalRecord.getF_INTERESTPERIODENDDT());
        ((UB_SWT_DisposalObject) disposalObject).setMessageStatus(disposalRecord.getF_MESSAGESTATUS());
        ((UB_SWT_DisposalObject) disposalObject).setMessageType(disposalRecord.getF_MESSAGETYPE());
        ((UB_SWT_DisposalObject) disposalObject).setPaymentFlagMT202(disposalRecord.getF_PAYMENTFLAG());
        ((UB_SWT_DisposalObject) disposalObject).setPostDate(disposalRecord.getF_POSTDATE());
        ((UB_SWT_DisposalObject) disposalObject).setPreviousDealRecordNumber(disposalRecord.getF_PREVSWTDISPOSALID());
        ((UB_SWT_DisposalObject) disposalObject).setReceiptFlagMT210(disposalRecord.getF_RECEIPTFLAG());
        ((UB_SWT_DisposalObject) disposalObject).setRelatedDealNumber(CommonConstants.EMPTY_STRING);

        ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithCode(disposalRecord.getF_BENEFICIARY_CODE());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithPartyIdentifier(checkSlash(
                disposalRecord.getF_BENEFICIARY_PARTY_IDENTIFIER()));
        /* end of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText1(disposalRecord.getF_BENEFICIARY_TEXT1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText2(disposalRecord.getF_BENEFICIARY_TEXT2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText3(disposalRecord.getF_BENEFICIARY_TEXT3());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText4(disposalRecord.getF_BENEFICIARY_TEXT4());
        if (!StringUtils.isBlank(disposalRecord.getF_BENEFICIARY_TEXT5())) {
            ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText5(disposalRecord.getF_BENEFICIARY_TEXT5());
        }
        /* end of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankAddlInstrCode(disposalRecord.getF_BANK_ADDL_INSTRUCTION_CODE());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankInstructionCode(disposalRecord.getF_BANK_INSTRUCTION_CODE());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankOpCode(disposalRecord.getF_BANK_OPERATION_CODE());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo1(disposalRecord.getF_BANK_TO_BANK_INFO1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo2(disposalRecord.getF_BANK_TO_BANK_INFO2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo3(disposalRecord.getF_BANK_TO_BANK_INFO3());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo4(disposalRecord.getF_BANK_TO_BANK_INFO4());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo5(disposalRecord.getF_BANK_TO_BANK_INFO5());
        ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo6(disposalRecord.getF_BANK_TO_BANK_INFO6());
        ((UB_SWT_DisposalObject) disposalObject).setSI_ChargeCode(disposalRecord.getF_CHARGECODE());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountPartyIdentifier(checkSlash(
                disposalRecord.getF_FOR_ACCOUNT_PARTY_IDENTIFIER()));
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText1(disposalRecord.getF_FOR_ACCOUNT_TEXT1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText2(disposalRecord.getF_FOR_ACCOUNT_TEXT2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText3(disposalRecord.getF_FOR_ACCOUNT_TEXT3());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText4(disposalRecord.getF_FOR_ACCOUNT_TEXT4());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediaryPartyIdentifier(checkSlash(
                disposalRecord.getF_INTERMEDIARY_PARTY_IDENTIFIER()));
        /* end of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryCode(disposalRecord.getF_INTERMEDIARY_CODE());

        ((UB_SWT_DisposalObject) disposalObject).setPayReceiveFlag(disposalRecord.getF_PAY_RECEIVE_FLAG());

        ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText1(disposalRecord.getF_INTERMEDIARY_TEXT1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText2(disposalRecord.getF_INTERMEDIARY_TEXT2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText3(disposalRecord.getF_INTERMEDIARY_TEXT3());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText4(disposalRecord.getF_INTERMEDIARY_TEXT4());
        if (!StringUtils.isBlank(disposalRecord.getF_INTERMEDIARY_TEXT5())) {
            ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText5(disposalRecord.getF_INTERMEDIARY_TEXT5());
        }
        /* end of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustAccInfo(checkSlash(disposalRecord.getF_ORDERINGCUSTACCID()));
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText1(disposalRecord.getF_ORDERINGCUSTDTL1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText2(disposalRecord.getF_ORDERINGCUSTDTL2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText3(disposalRecord.getF_ORDERINGCUSTDTL3());
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText4(disposalRecord.getF_ORDERINGCUSTDTL4());
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustBICCode(CommonConstants.EMPTY_STRING);

        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstAccInfo(checkSlash(disposalRecord.getF_ORDERINGINSTITUTE_ACC_INFO()));
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstBICCode(disposalRecord.getF_ORDERINGINSTITUTE_CODE());
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText1(disposalRecord.getF_ORDERINGINSTITUTE_TEXT1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText2(disposalRecord.getF_ORDERINGINSTITUTE_TEXT2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText3(disposalRecord.getF_ORDERINGINSTITUTE_TEXT3());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText4(disposalRecord.getF_ORDERINGINSTITUTE_TEXT4());
        if (!StringUtils.isBlank(disposalRecord.getF_ORDERINGINSTITUTE_TEXT5())) {
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText5(disposalRecord.getF_ORDERINGINSTITUTE_TEXT5());
        }
        /* end of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails1(disposalRecord.getF_PAY_DETAILS1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails2(disposalRecord.getF_PAY_DETAILS2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails3(disposalRecord.getF_PAY_DETAILS3());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails4(disposalRecord.getF_PAY_DETAILS4());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayReceiveFlag(disposalRecord.getF_PAY_RECEIVE_FLAG());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayToBICCode(disposalRecord.getF_PAY_TO_CODE());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayToPartyIdentifier(checkSlash(
                disposalRecord.getF_PAY_TO_PARTY_IDENTIFIER()));
        /* end of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText1(disposalRecord.getF_PAY_TO_TEXT1());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText2(disposalRecord.getF_PAY_TO_TEXT2());
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText3(disposalRecord.getF_PAY_TO_TEXT3());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText4(disposalRecord.getF_PAY_TO_TEXT4());
        if (!StringUtils.isBlank(disposalRecord.getF_PAY_TO_TEXT5())) {
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText5(disposalRecord.getF_PAY_TO_TEXT5());
        }
        /* end of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setSI_SendersCharges(disposalRecord.getF_SENDERCHARGEAMOUNT());
        ((UB_SWT_DisposalObject) disposalObject).setTransactionAmount(disposalRecord.getF_TRANSACTIONAMOUNT());
        ((UB_SWT_DisposalObject) disposalObject).setTransactionStatus(disposalRecord.getF_TRANSACTIONSTATUS());
        ((UB_SWT_DisposalObject) disposalObject).setValueDate(disposalRecord.getF_VALUEDATE());
        ((UB_SWT_DisposalObject) disposalObject).setVerifyFlag(Integer.parseInt("1"));
        ((UB_SWT_DisposalObject) disposalObject).setCrdrFlag(disposalRecord.getF_CRDRCONFIRMATIONFLAG());
        ((UB_SWT_DisposalObject) disposalObject).setDealOriginator(disposalRecord.getF_DEALORIGINATOR());
        ((UB_SWT_DisposalObject) disposalObject).setSenderChargeCurrency(disposalRecord.getF_SENDERCHARGECURRENCYCODE());
        ((UB_SWT_DisposalObject) disposalObject).setClientNumber(disposalRecord.getF_CUSTOMERCODE());
        ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifier(disposalRecord.getF_PARTYIDENTIFIER());
        ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd1(disposalRecord.getF_PARTYADDRESSLINE1());
        ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd2(disposalRecord.getF_PARTYADDRESSLINE2());
        ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd3(disposalRecord.getF_PARTYADDRESSLINE3());
        ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd4(disposalRecord.getF_PARTYADDRESSLINE4());
        ((UB_SWT_DisposalObject) disposalObject).setOrderingInstitution(disposalRecord.getF_ORDERINGINSTITUTION());
        ((UB_SWT_DisposalObject) disposalObject).setGenerate103Plus(disposalRecord.getF_GENERATE103PLUSIND());
        ((UB_SWT_DisposalObject) disposalObject).setForAccountIdentifierCode(disposalRecord.getF_FOR_ACC_IDENTIFIERCODE());
        ((UB_SWT_DisposalObject) disposalObject)
                .setOrderingCustomerAccountNumber(checkSlash(disposalRecord.getF_ORDERINGCUSTACCID()));
        ((UB_SWT_DisposalObject) disposalObject).setOrderingCustomerIdentifierCode(disposalRecord
                .getF_ORDERINGCUST_IDENTIFIERCODE());
        ((UB_SWT_DisposalObject) disposalObject).setSenderToReceiverInfo1(disposalRecord.getF_SENDER_TO_RECEIVER_INFO1());
        ((UB_SWT_DisposalObject) disposalObject).setSenderToReceiverInfo2(disposalRecord.getF_SENDER_TO_RECEIVER_INFO2());
        ((UB_SWT_DisposalObject) disposalObject).setSenderToReceiverInfo3(disposalRecord.getF_SENDER_TO_RECEIVER_INFO3());
        ((UB_SWT_DisposalObject) disposalObject).setSenderToReceiverInfo4(disposalRecord.getF_SENDER_TO_RECEIVER_INFO4());
        ((UB_SWT_DisposalObject) disposalObject).setSenderToReceiverInfo5(disposalRecord.getF_SENDER_TO_RECEIVER_INFO5());
        ((UB_SWT_DisposalObject) disposalObject).setSenderToReceiverInfo6(disposalRecord.getF_SENDER_TO_RECEIVER_INFO6());
        /* start of Added new fields for the reorganized Settlement instruction by Sharan */
        ((UB_SWT_DisposalObject) disposalObject).setTermsAndConditionForDeals1(disposalRecord.getF_TERMS_CONDITIONS_INFO1());
        ((UB_SWT_DisposalObject) disposalObject).setTermsAndConditionForDeals2(disposalRecord.getF_TERMS_CONDITIONS_INFO2());
        ((UB_SWT_DisposalObject) disposalObject).setTermsAndConditionForDeals3(disposalRecord.getF_TERMS_CONDITIONS_INFO3());
        ((UB_SWT_DisposalObject) disposalObject).setTermsAndConditionForDeals4(disposalRecord.getF_TERMS_CONDITIONS_INFO4());
        ((UB_SWT_DisposalObject) disposalObject).setTermsAndConditionForDeals5(disposalRecord.getF_TERMS_CONDITIONS_INFO5());
        ((UB_SWT_DisposalObject) disposalObject).setTermsAndConditionForDeals6(disposalRecord.getF_TERMS_CONDITIONS_INFO6());
        ((UB_SWT_DisposalObject) disposalObject).setTransactionCode(disposalRecord.getF_TXNTYPECODE());
        ((UB_SWT_DisposalObject) disposalObject).setFundingAmount(disposalRecord.getF_FUNDINGAMOUNT());
        ((UB_SWT_DisposalObject) disposalObject).setExchangeRate(disposalRecord.getF_EXCHANGERATE());
        ((UB_SWT_DisposalObject) disposalObject).setReceiverChargeAmount(disposalRecord.getF_RECEIVERCHARGEAMOUNT());
        ((UB_SWT_DisposalObject) disposalObject).setTransactionId(disposalRecord.getF_TRANSACTIONID());
        ((UB_SWT_DisposalObject) disposalObject).setDayCountFraction(disposalRecord.getF_DAYCOUNTFRACTION());
        
        // SWIFT 2017-- new fields for MT300 
        ((UB_SWT_DisposalObject) disposalObject).setIsNDFOpen(disposalRecord.getF_UBISNDFOPEN());
        ((UB_SWT_DisposalObject) disposalObject).setIsNonDeliverable(disposalRecord.getF_UBISNONDELIVERABLE());
       
        ((UB_SWT_DisposalObject) disposalObject).setSettlementCurrency(disposalRecord.getF_UBSETTLEMENTCCY());
        ((UB_SWT_DisposalObject) disposalObject).setReOpeningConfirmation(disposalRecord.getF_UBREFOPENINGCONFIRMATION());
        
        ((UB_SWT_DisposalObject) disposalObject).setValuationDate(disposalRecord.getF_UBVALUATIONDATE());
        ((UB_SWT_DisposalObject) disposalObject).setSettlementRateRC(disposalRecord.getF_UBSETTLEMENTRATESRC());
        ((UB_SWT_DisposalObject) disposalObject).setClearSettlementSession(disposalRecord.getF_UBCLEARINGSETTLEMENTSESSION());
        if (logger.isInfoEnabled()) {
            logger.info("::::39M field PaymentClearingCentre:::::" + disposalRecord.getF_UBPAYMENTCLEARINGCENTER());
        }
        ((UB_SWT_DisposalObject) disposalObject).setPaymentClearingCentre(disposalRecord.getF_UBPAYMENTCLEARINGCENTER());
        ((UB_SWT_DisposalObject) disposalObject).setInstructedAmtCurrency(disposalRecord.getF_UBINSTRUCTEDAMTCURRENCY());
        //get ExchangeRateType
        ((UB_SWT_DisposalObject) disposalObject).setCreditExchangeRateType(disposalRecord.getF_UBCREDITEXCHRATETYPE());
      
        ((UB_SWT_DisposalObject) disposalObject).setMessageXML(disposalRecord.getF_MESSAGEXML());


        /* End of Added new fields for the reorganized Settlement instruction by Sharan */

        ArrayList params = new ArrayList();
        SimplePersistentObject simplePersistentObject = null;

        // Get the Customer number & Currency Code for Main Account Id
        params.clear();
        params.add(disposalRecord.getF_CUSTACCOUNTID());
        customerCode = FinderMethods.findCustomerCodeByAccount(disposalRecord.getF_CUSTACCOUNTID());
        if (customerCode != null) {
            ((UB_SWT_DisposalObject) disposalObject).setMainAccCustomerNumber(customerCode);
        }
        else {
                logger.error(CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT + disposalRecord.getF_CUSTACCOUNTID());
        }
        accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, disposalRecord.getF_CUSTACCOUNTID(), false);
        mainCurrencyCode = accountBO.getF_ISOCURRENCYCODE();
        // Get the Customer number & Currency Code for Contra Account ID
        params.clear();
        params.add(disposalRecord.getF_CONTRAACCOUNTID());
        customerCode = FinderMethods.findCustomerCodeByAccount(disposalRecord.getF_CONTRAACCOUNTID());
        if (customerCode != null) {
            ((UB_SWT_DisposalObject) disposalObject).setContraAccCustomerNumber(customerCode);
        }
        else {
                logger.error(CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT + disposalRecord.getF_CONTRAACCOUNTID());
        }

        accountBO = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, disposalRecord.getF_CONTRAACCOUNTID(), false);
        contraCurrecnyCode = accountBO.getF_ISOCURRENCYCODE();

        ((UB_SWT_DisposalObject) disposalObject).setContraAccCurrencyCode(contraCurrecnyCode);
        ((UB_SWT_DisposalObject) disposalObject).setMainAccCurrencyCode(mainCurrencyCode);
        
        ((UB_SWT_DisposalObject) disposalObject).setEnd2EndTxnRef(disposalRecord.getF_UBEND2ENDTXNREF());
        ((UB_SWT_DisposalObject) disposalObject).setServiceTypeId(messageMicroflowMap.getProperty("ServiceTypeId"));
        ((UB_SWT_DisposalObject) disposalObject).setMessagePreference(disposalRecord.getF_UBMESSAGEPREFERENCE());
        return disposalObject;
    }

    /**
     * Method Description:Check if the partyIdentifier starts with slash else add it
     * 
     * @param partyIdentifier
     * @return
     */
    private String checkSlash(String partyIdentifier) {
        String slash = "/";
        if (StringUtils.isNotBlank(partyIdentifier) && !partyIdentifier.startsWith("/")) {
            partyIdentifier = slash.concat(partyIdentifier);
        }

        return partyIdentifier;
    }

    /**
     * Method Description:Get Disposal details based on various flag values
     * 
     * @param disposalId
     * @param env
     * @return
     */
    private List getDisposalDetails(String disposalId, BankFusionEnvironment env) {
        List swtDispList = new ArrayList();
        IBOSWTDisposal disposalBO = (IBOSWTDisposal) env.getFactory().findByPrimaryKey(IBOSWTDisposal.BONAME, disposalId, false);

        if (isMessageStatusValid(disposalBO) && isOtherFlagsValid(disposalBO)) {
            swtDispList.add(disposalBO);
        }

        return swtDispList;
    }

    /**
     * Method Description:Check if the message status flag is 0 or 4
     * 
     * @param disposalBO
     * @return
     */
    private boolean isMessageStatusValid(IBOSWTDisposal disposalBO) {
        boolean isValid = Boolean.FALSE;

        if (disposalBO.getF_MESSAGESTATUS() == 0 || disposalBO.getF_MESSAGESTATUS() == 4) {
            isValid = Boolean.TRUE;
        }
        return isValid;
    }

    /**
     * Method Description:Check if the other status flag are valid
     * 
     * @param disposalBO
     * @return
     */
    private boolean isOtherFlagsValid(IBOSWTDisposal disposalBO) {
        boolean isValid = Boolean.FALSE;
        if (disposalBO.getF_CANCELFLAG() == 0 || isPaymentFlagValid(disposalBO) || isCRDRConfirmationFlagValid(disposalBO)
                || disposalBO.getF_CONFIRMATIONFLAG() == 0 || disposalBO.getF_RECEIPTFLAG() == 0) {
            isValid = Boolean.TRUE;
        }
        return isValid;
    }

    /**
     * Method Description:Check if the payment flag is 0 or 2
     * 
     * @param disposalBO
     * @return
     */
    private boolean isPaymentFlagValid(IBOSWTDisposal disposalBO) {
        boolean isValid = Boolean.FALSE;
        if (disposalBO.getF_PAYMENTFLAG() == 0 || disposalBO.getF_PAYMENTFLAG() == 2) {
            isValid = Boolean.TRUE;
        }
        return isValid;
    }

    /**
     * Method Description:Check if the CR DR confirmation flag is 1 1 3 or 5
     * 
     * @param disposalBO
     * @return
     */
    private boolean isCRDRConfirmationFlagValid(IBOSWTDisposal disposalBO) {
        boolean isValid = Boolean.FALSE;
        if (disposalBO.getF_CRDRCONFIRMATIONFLAG() == 0 || disposalBO.getF_CRDRCONFIRMATIONFLAG() == 1
                || disposalBO.getF_CRDRCONFIRMATIONFLAG() == 3 || disposalBO.getF_CRDRCONFIRMATIONFLAG() == 5) {
            isValid = Boolean.TRUE;
        }
        return isValid;
    }

    /**
     * Method Description:Load Swift.properties file
     * 
     * @param env
     */
    private void loadSwiftProperties(BankFusionEnvironment env) {
        try {
            String configLocation = GetUBConfigLocation.getUBConfigLocation();
            InputStream is = new FileInputStream(configLocation + CONF + SWIFT_PROPERTY_FILENAME);
            messageMicroflowMap.load(is);
        }
        catch (Exception ex) {
            logger.error(ExceptionUtil.getExceptionAsString(ex));
            EventsHelper.handleEvent(ChannelsEventCodes.E_BASEEQUIVAL_WRONGLY_CAL, new Object[] {}, new HashMap(), env);
        }
    }

}
