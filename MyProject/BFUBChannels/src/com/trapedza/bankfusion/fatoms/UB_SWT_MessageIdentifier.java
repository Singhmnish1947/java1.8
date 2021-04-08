/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

/**
 * @author Sharan
 *
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.exceptions.FinderException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MessageIdentifier;

/*
 * Exception number that are used in this Fatom are as follows
 * 9451 - If main and contra account are missing
 * 9452 - If main and contra customer not present in the swift customer detail table
 * 9453 - IF the specified transaction is not found
 * 9454 - If deal number does not exists in the disposal table
 * 9455 - If currency not found int he currency table
 */
/*
 * Pending work in this java program -
 * 1. When the deal is amended then according the payment, receipt, drcrConfrim flag should be updated.
 * 2. When the deal is coming for maturity that deal details should be handled properly.
 */

public class UB_SWT_MessageIdentifier extends AbstractUB_SWT_MessageIdentifier {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public UB_SWT_MessageIdentifier(BankFusionEnvironment env) {
        super(env);
    }

    private transient final static Log logger = LogFactory.getLog(UB_SWT_MessageIdentifier.class.getName());
    int payment_Flag = 9;
    int receipt_Flag = 9;
    int confirm_Flag = 9;
    int cancel_Flag = 9;
    int prev_Payment_Flag = 9;
    int prev_Receipt_Flag = 9;
    int prev_Confirm_Flag = 9;
    int prev_Cancel_Flag = 9;
    int error_Flag = 9;
    int prevCancelFlag = 9;
    int CRDRconfirm_Flag = 9;
    int prev_CRDRconfirm_Flag = 9;
    String Status_flag = "1";
    public static final String NO = "N";
    public static final String YES = "Y";
    private static final String NOMT202 = "3";
    public static final String MT103 = "103";
    public static final String MT110 = "110";
    public static final String MT202 = "202";
    public static final String MT200 = "200";
    public static final String MT205 = "205";
    public static final String MT300 = "300";
    public static final String MT320 = "320";
    public static final String MT330 = "330";
    public static final String MT350 = "350";
    public static final String MT900 = "900";
    public static final String MT910 = "910";
    public static final String NOSTRO = "NOSTRO";
    private boolean isMainAccountIsNostro = false;
    private boolean isContraAccountIsNostro = false;
    boolean cancelMessage;

    private static final String NEW = "NEW";
    private static final String ACCOUNT_NOT_AVAILABLE = "Account not available ";
    private static final String ACCOUNT_NOT_AVAILABLE_ON_THIS = "Account not available on this ";
    private static final String SWT_ACTIVE = "SWTACTIVE";
    private static final String CUSTOMER_CODE = "CUSTOMERCODE";
    private static final String CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT = "Customer not available for the account ";
    private static final String Y = "Y";
    private static final String N = "N";
    private static final String F = "F";
    private static final String NUM_900 = "900";
    private static final String NUM_910 = "910";
    private static final String NUM_103 = "103";
    private static final String CURRENCY_CODE_NOT_AVAILABLE = "Currency code not available on this ";
    private static final String ROL = "ROL";
    private static final String MAT = "MAT";
    private static final String NUM_350 = "350";
    private static final String SWT_DISPOSALID = "SWTDISPOSALID";
    private static final String BRKDEP = "BRKDEP";
    private static final String BRKLON = "BRKLON";
    private static final String SWT_DISPOSAL_PAYMENT_FLAG = "PAYMENTFLAG";
    private static final String CR_DR_CONFIRMATION_FLAG = "CRDRCONFIRMATIONFLAG";
    private static final String CONFIRMATION_FLAG = "CONFIRMATIONFLAG";
    private static final String CANCLE = "CANCEL";
    private static final String CUST_ACCOUNTID = "CUSTACCOUNTID";
    private static final String CONTRA_ACCOUNTID = "CONTRAACCOUNTID";
    private static final String DEAL_NOT_AVAILABLE = "Deal not available on this ";
    private static final String AMEND = "AMEND";
    private static final String TRANSACTION_STATUS = "TRANSACTIONSTATUS";
    private static final String AM = "AM";
    private static final String BR = "BR";
    private static final String NUM_0 = "0";
    private static final String NUM_1 = "1";
    private static final String MESSAGE_STATUS = "MESSAGESTATUS";
    private static final String PREV_SWT_DISPOSALID = "PREVSWTDISPOSALID";
    private static final String PAY_MSG_REQUIRED = "PAYMSGREQUIRED";
    private static final String NUM_2 = "2";
    private static final String SWT_DISPOSAL_RECEIPT_FLAG = "RECEIPTFLAG";
    private static final String CONVER_MSG_FLAG = "COVERMSGFLAG";
    private static final String DEPOSIT_LOAN_CONFIRM_REQUIRED = "DEPOSITLOANCONFIRMREQUIRED";
    private static final String IS_FINANCIAL_INSTITUTE = "ISFINANCIALINSTITUTE";
    private static final String BIC_CODE = "BICCODE";
    private static final String NUM_3 = "3";
    private static final String NUM_4 = "4";;
    private static final String NUM_5 = "5";
    private static final String NUM_6 = "6";
    private static final String NUM_8 = "8";
    private static final String NUM_7 = "7";
    private static final String NUM_9 = "9";
    private static final String PAYCAP = "PAYCAP";
    private static final String NEWLON = "NEWLON";
    private static final String MATDEP = "MATDEP";
    private static final String C = "C";
    private static final String ROLDEDEC = "ROLDEDEC";
    private static final String ROLLOINC = "ROLLOINC";
    private static final String ROLLOSAM = "ROLLOSAM";
    private static final String ADVICE_MESSAGE = "ADVICEMESSAGE";
    private static final String P = "P";
    private static final String COVER_MSG_FLAG = "COVERMSGFLAG";
    private static final String EMPTY_STRING = " ";
    private static final String CANCEL = "CANCEL";
    private static final String NEWDEP = "NEWDEP";
    private static final String ROLLODEC = "ROLLODEC";
    private static final String ROLDEINC = "ROLDEINC";
    private static final String ROLDESAM = "ROLDESAM";
    private static final String MATLON = "MATLON";
    private static final String MESSAGE_TYPE = "MESSAGETYPE";

    IBOAccount mainAccount = null;
    IBOAccount contraAccount = null;
    IBOCurrency mainCurrency = null;
    IBOCurrency contraCurrency = null;
    IBOSwtCustomerDetail mainCustomer = null;
    IBOSwtCustomerDetail contraCustomer = null;
    IBOSwtCustomerDetail fxCustomer = null;
    IBOMisTransactionCodes transactionCode = null;
    SimplePersistentObject prevDisposal = null;
    SimplePersistentObject mainCustConfig = null;
    SimplePersistentObject contraCustConfig = null;
    String prevDisposalID = null;
    String DealType = null;
    String portfolioCustomerNumber, AccountportID = null;
    boolean mainCustomerSettingFound = true;
    boolean contraCustomerSettingFound = true;
    boolean FXCustomerSettingFound = true;
    String DRCRConfirmation = CommonConstants.EMPTY_STRING;
    String SwtMainCurrency = null;
    String SwtContraCurrency = null;
    String mainCustomerSwiftActive = NO;
    String contraCustomerSwiftActive = NO;
    String fxCustomerSwiftActive = NO;

    private static final String fetchCustomer1 = "SELECT T1.boID AS CUSTOMERCODE, T1.f_ADVICEDAYS  AS ADVICEDAYS, T1.f_CRCONFIRMREQUIRED AS CRCONFIRMREQUIRED,"
            + "T1.f_DRCONFIRMREQUIRED AS DRCONFIRMREQUIRED, T1.f_FXDEALCONFIRMREQUIRED AS FXDEALCONFIRMREQUIRED,"
            + "T1.f_DEPOSITLOANCONFIRMREQUIRED AS DEPOSITLOANCONFIRMREQUIRED, T1.f_ISFINANCIALINSTITUTE AS ISFINANCIALINSTITUTE,"
            + "T1.f_PAYMSGREQUIRED AS PAYMSGREQUIRED, T1.f_COVERMSGFLAG AS COVERMSGFLAG, T1.f_ADVICEMSGREQUIRED AS ADVICEMESSAGE,"
            + "T1.f_BICCODE AS BICCODE, T1.f_SWTACTIVE as SWTACTIVE FROM " + IBOSwtCustomerDetail.BONAME + " T1 ,";
    @SuppressWarnings("FBPE")
    private static final String fetchCustomer2 = IBOAccount.BONAME + " T2 " + "WHERE T1.boID = T2." + IBOAccount.CUSTOMERCODE + " AND T2." + IBOAccount.ACCOUNTID
            + " = ? ";

    private static final String fetchDisposal = "SELECT T1.boID AS SWTDISPOSALID, T1.f_CUSTACCOUNTID as CUSTACCOUNTID,T1.f_MESSAGETYPE as MESSAGETYPE,T1.f_CONTRAACCOUNTID as CONTRAACCOUNTID,T1.f_PAYMENTFLAG as PAYMENTFLAG ,"
            + "T1.f_CANCELFLAG AS CANCELFLAG, T1.f_CONFIRMATIONFLAG AS CONFIRMATIONFLAG,T1.f_TRANSACTIONSTATUS AS TRANSACTIONSTATUS ,T1.f_PREVSWTDISPOSALID as PREVSWTDISPOSALID,T1.f_MESSAGESTATUS AS MESSAGESTATUS,T1.f_RECEIPTFLAG AS RECEIPTFLAG, "
            + "T1.f_CRDRCONFIRMATIONFLAG AS CRDRCONFIRMATIONFLAG FROM " + IBOSWTDisposal.BONAME + " T1 WHERE T1.boID NOT IN ("
            + "SELECT coalesce(T2.f_PREVSWTDISPOSALID, 'X') FROM " + IBOSWTDisposal.BONAME
            + " T2 WHERE T2.f_DEALNO = ?) and T1.f_DEALNO = ?";

    private static final String fetchDisposal1 = "SELECT T1.boID AS SWTDISPOSALID, T1.f_MESSAGETYPE as MESSAGETYPE,T1.f_CUSTACCOUNTID as CUSTACCOUNTID,T1.f_CONTRAACCOUNTID as CONTRAACCOUNTID,T1.f_MESSAGESTATUS AS MESSAGESTATUS,T1.f_PAYMENTFLAG as PAYMENTFLAG ,"
            + "T1.f_CANCELFLAG AS CANCELFLAG, T1.f_CONFIRMATIONFLAG AS CONFIRMATIONFLAG,T1.f_TRANSACTIONSTATUS AS TRANSACTIONSTATUS ,T1.f_PREVSWTDISPOSALID as PREVSWTDISPOSALID,T1.f_RECEIPTFLAG AS RECEIPTFLAG, "
            + "T1.f_CRDRCONFIRMATIONFLAG AS CRDRCONFIRMATIONFLAG FROM " + IBOSWTDisposal.BONAME + " T1 WHERE T1.boID =?";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MessageIdentifier#process(com.trapedza.
     * bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        IPersistenceObjectsFactory factory = env.getFactory();
        List customerConfigDetail = null;
        List disposalRecord = null;
        String currency1 = null;
        String currency2 = null;
        ArrayList params = new ArrayList();

        cancelMessage = false;
        if (getF_IN_Code_Word().trim().substring(0, 3).compareTo(NEW) == 0)
            DealType = NEW;
        else DealType = getF_IN_Code_Word().trim();

        try {
            mainAccount = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, getF_IN_Main_Account(), false);
            setF_OUT_MainAccountCurrencyCode(mainAccount.getF_ISOCURRENCYCODE());
            currency1 = mainAccount.getF_ISOCURRENCYCODE();
        }
        catch (FinderException e) {
                logger.error(ACCOUNT_NOT_AVAILABLE + getF_IN_Main_Account());
            // throw new BankFusionException(9451, new Object[] { getF_IN_Main_Account() }, logger,
            // env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_PARTY_ADDRESS_LINE_NOT_ALLOWED, new Object[] { getF_IN_Main_Account() },
                    new HashMap(), env);
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }

        try {
            contraAccount = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, getF_IN_Contra_ACcount(), false);
            currency2 = contraAccount.getF_ISOCURRENCYCODE();
        }
        catch (FinderException e) {
                logger.error(ACCOUNT_NOT_AVAILABLE_ON_THIS + getF_IN_Contra_ACcount());
            // throw new BankFusionException(9451, new Object[] { getF_IN_Contra_ACcount() },
            // logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_PARTY_ADDRESS_LINE_NOT_ALLOWED, new Object[] { getF_IN_Contra_ACcount() },
                    new HashMap(), env);
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }

        params.clear();
        params.add(getF_IN_Main_Account());
        customerConfigDetail = env.getFactory().executeGenericQuery(fetchCustomer1 + fetchCustomer2, params, null);
        if (customerConfigDetail.size() > 0) {
            mainCustConfig = (SimplePersistentObject) customerConfigDetail.get(0);
            mainCustomerSwiftActive = (String) mainCustConfig.getDataMap().get(SWT_ACTIVE);
            if (getF_IN_FXTransaction().intValue() == 0 || getF_IN_FXTransaction().intValue() == 7)
                setF_OUT_Customer_Number((String) mainCustConfig.getDataMap().get(CUSTOMER_CODE));
        }
        else {
            // As discuss with vipesh and Salil this comment is not required.Removed by Samkit.
            // logger.error("Customer not available for the account " + getF_IN_Main_Account());
            mainCustomerSettingFound = false;
        }

        params.clear();
        params.add(getF_IN_Contra_ACcount());

        customerConfigDetail = env.getFactory().executeGenericQuery(fetchCustomer1 + fetchCustomer2, params, null);
        if (customerConfigDetail.size() > 0) {
            contraCustConfig = (SimplePersistentObject) customerConfigDetail.get(0);
            contraCustomerSwiftActive = (String) contraCustConfig.getDataMap().get(SWT_ACTIVE);
        }
        else {
                logger.error(CUSTOMER_NOT_AVAILABLE_FOR_ACCOUNT + getF_IN_Contra_ACcount());
            contraCustomerSettingFound = false;
        }
        UB_SWT_Util util = new UB_SWT_Util();
        isMainAccountIsNostro = util.isSwiftNostro(getF_IN_Main_Account(), env);
        isContraAccountIsNostro = util.isSwiftNostro(getF_IN_Contra_ACcount(), env);
        // IN_FXTransaction value = 0 when it is not an FX Deal.
        // IN_FXTransaction value = 1 when it is an FX Deal but not an option
        // deal.
        // IN_FXTransaction value = 2 when it is an FX Option Deal .
        // IN_FXTransaction value = 3 when it is capitalize for a Money Market
        // Deal.
        // IN_FXTransaction value = 4 when it is pay away for Money Market Deal.
        if ((getF_IN_FXTransaction().intValue() == 1) || (getF_IN_FXTransaction().intValue() == 2)
                || (getF_IN_FXTransaction().intValue() == 7)) {
            if ((getF_IN_FXTransaction().intValue() == 7)) {
                setF_OUT_DealOriginator(NUM_7);
            }
            else {
                try {
                    fxCustomer = (IBOSwtCustomerDetail) factory.findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                            getF_IN_Customer_Number(), false);
                    setF_OUT_Customer_Number(fxCustomer.getBoID());
                    fxCustomerSwiftActive = fxCustomer.getF_SWTACTIVE();

                    if (fxCustomer.getF_FXDEALCONFIRMREQUIRED().compareTo(Y) != 0)
                        fxCustomerSwiftActive = N;

                    setF_OUT_DealOriginator(F);
                    if (fxCustomer.getF_BICCODE() != null && fxCustomer.getF_BICCODE().trim().length() == 0) {
                        if (!(getF_IN_Message_Type().equals(NUM_900) || getF_IN_Message_Type().equals(NUM_910))) {
                            setF_OUT_message_Type(NUM_103);
                            setF_IN_Message_Type(NUM_103); // added to fic Bug#13783 Anand Pasunoori
                        }
                    }

                }
                catch (FinderException e) {
                        logger.error(ACCOUNT_NOT_AVAILABLE_ON_THIS + getF_IN_Contra_ACcount());
                    FXCustomerSettingFound = false;
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
            }
        }

        mainCurrency = CurrencyUtil.getCurrencyDetailsOfCurrentZone(currency1);
        if(null != mainCurrency) {
            SwtMainCurrency = mainCurrency.getF_SWTCURRENCYINDICATOR();
        }
        else {
                logger.error(CURRENCY_CODE_NOT_AVAILABLE + currency1);
            // throw new BankFusionException(9455, new Object[] { currency1 }, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
                    new Object[] { currency1 }, new HashMap(), env);
        }

        contraCurrency = CurrencyUtil.getCurrencyDetailsOfCurrentZone(currency2);
        if(null != contraCurrency) {
            SwtContraCurrency = contraCurrency.getF_SWTCURRENCYINDICATOR();
        }
        else {
                logger.error(CURRENCY_CODE_NOT_AVAILABLE + currency2);
            // throw new BankFusionException(9455, new Object[] { currency2 }, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
                    new Object[] { currency2 }, new HashMap(), env);
        }

        if (getF_IN_Message_Type().equals(NUM_900) || getF_IN_Message_Type().equals(NUM_910)) {
            setF_OUT_prevDisposalID(getF_IN_DisposalID());
        }

        params.clear();
        params.add(getF_IN_Deal_Number().trim());
        params.add(getF_IN_Deal_Number().trim());
        if (getF_IN_Code_Word().trim().indexOf(ROL) != -1 || getF_IN_Code_Word().trim().indexOf(MAT) != -1
                || getF_IN_Message_Type().equals(NUM_350)) {
            disposalRecord = env.getFactory().executeGenericQuery(fetchDisposal, params, null);
            if (!disposalRecord.isEmpty()) {
                prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                FindLastGeneratedDeal(prevDisposal, env);
                prevDisposalID = (String) prevDisposal.getDataMap().get(SWT_DISPOSALID);
                setF_OUT_prevDisposalID(prevDisposalID);
            }
        }

        if (getF_IN_Message_Type().equals(NUM_350)
                && (DealType.equalsIgnoreCase(NEW) || DealType.equalsIgnoreCase(BRKDEP) || DealType.equalsIgnoreCase(BRKLON))) {
            if (DealType.equalsIgnoreCase(NEW)) {
                MT350MessageRequired();
                Other_Message_required();
                confirm_Flag = 0;
            }
            else {
                setF_OUT_prevDisposalID(getF_IN_DisposalID().trim());
                try {
                    params.clear();
                    params.add(getF_IN_DisposalID().trim());
                    disposalRecord = factory.executeGenericQuery(fetchDisposal1, params, null);
                    prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                    if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_PAYMENT_FLAG)).intValue() == 1
                            || ((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 2
                            || ((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)).intValue() == 1
                            || ((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 1) {
                        CancelDecideMessage();

                    }
                    else {
                        confirm_Flag = 9;
                        payment_Flag = 9;
                        receipt_Flag = 9;
                        CRDRconfirm_Flag = 9;
                        cancelMessage = true;
                    }
                }
                catch (FinderException e) {
                    cancelMessage = false;
                    confirm_Flag = 9;
                    payment_Flag = 9;
                    receipt_Flag = 9;
                    CRDRconfirm_Flag = 9;
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }

            }
        }
        else {

            params.clear();
            params.add(getF_IN_Deal_Number().trim());
            params.add(getF_IN_Deal_Number().trim());
            if (getF_IN_DisposalID().trim().length() > 0)
                setF_OUT_prevDisposalID(getF_IN_DisposalID().trim());
            if (DealType.compareTo(CANCEL) == 0) {
                try {
                    disposalRecord = env.getFactory().executeGenericQuery(fetchDisposal, params, null);
                    if (disposalRecord.size() > 1) {
                        prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                        String mainAccount = (String) prevDisposal.getDataMap().get(CUST_ACCOUNTID);
                        String contraAccount = (String) prevDisposal.getDataMap().get(CONTRA_ACCOUNTID);
                        if (getF_IN_Main_Account().compareTo(mainAccount) != 0
                                && contraAccount.compareTo(getF_IN_Contra_ACcount()) != 0) {
                            prevDisposal = (SimplePersistentObject) disposalRecord.get(1);
                        }
                    }
                    else {
                        prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                        FindLastGeneratedDeal(prevDisposal, env);
                    }
                    if (getF_IN_DisposalID() != null && getF_IN_DisposalID().trim().length() > 0) {
                        setF_OUT_prevDisposalID(getF_IN_DisposalID().trim());
                        params.clear();
                        params.add(getF_IN_DisposalID().trim());
                        disposalRecord = factory.executeGenericQuery(fetchDisposal1, params, null);
                        prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                        FindLastGeneratedDeal(prevDisposal, env);
                        prevDisposalID = (String) prevDisposal.getDataMap().get(SWT_DISPOSALID);
                        setF_OUT_prevDisposalID(prevDisposalID);

                    }
                    else {
                        prevDisposalID = (String) prevDisposal.getDataMap().get(SWT_DISPOSALID);
                        setF_OUT_prevDisposalID(prevDisposalID);
                    }
                }
                catch (FinderException e) {
                        logger.error(DEAL_NOT_AVAILABLE + getF_IN_Deal_Number().trim());
                    error_Flag = 1;
                    /*
                     * throw new BankFusionException(9454, new Object[] {
                     * getF_IN_Deal_Number().trim() }, logger, env);
                     */
                    EventsHelper.handleEvent(ChannelsEventCodes.E_ADDR_LINE_TO_AND_ORD_CUST_ID_CODE_EXISTS_SIMUL,
                            new Object[] { getF_IN_Deal_Number().trim() }, new HashMap(), env);
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
                CancelDecideMessage();
            }
            else if (DealType.compareTo(AMEND) == 0) {
                try {
                    disposalRecord = env.getFactory().executeGenericQuery(fetchDisposal, params, null);
                    prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                    String mainAccount = (String) prevDisposal.getDataMap().get(CUST_ACCOUNTID);
                    String contraAccount = (String) prevDisposal.getDataMap().get(CONTRA_ACCOUNTID);
                    if (getF_IN_Main_Account().compareTo(mainAccount) != 0
                            && contraAccount.compareTo(getF_IN_Contra_ACcount()) != 0) {
                        prevDisposal = (SimplePersistentObject) disposalRecord.get(1);
                    }
                    if (getF_IN_DisposalID() != null && getF_IN_DisposalID().trim().length() > 0) {
                        params.clear();
                        params.add(getF_IN_DisposalID().trim());
                        disposalRecord = factory.executeGenericQuery(fetchDisposal1, params, null);
                        prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                        setF_OUT_prevDisposalID(getF_IN_DisposalID().trim());
                    }
                    else {
                        prevDisposalID = (String) prevDisposal.getDataMap().get(SWT_DISPOSALID);
                        setF_OUT_prevDisposalID(prevDisposalID);
                    }

                }
                catch (FinderException e) {
                        logger.error(DEAL_NOT_AVAILABLE + getF_IN_Deal_Number().trim());
                    error_Flag = 1;
                    /*
                     * throw new BankFusionException(9454, new Object[] {
                     * getF_IN_Deal_Number().trim() }, logger, env);
                     */
                    EventsHelper.handleEvent(ChannelsEventCodes.E_ADDR_LINE_TO_AND_ORD_CUST_ID_CODE_EXISTS_SIMUL,
                            new Object[] { getF_IN_Deal_Number().trim() }, new HashMap(), env);
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
                AmendDecideMessage();
                NewDecideMessage();
                String code = (String) prevDisposal.getDataMap().get(TRANSACTION_STATUS);
                if (!code.startsWith(AM))
                    code = AM + code;
                setF_OUT_Code_Word(code);
            }
            else {
                if (DealType.equalsIgnoreCase(BRKDEP) || DealType.equalsIgnoreCase(BRKLON)) {
                    disposalRecord = env.getFactory().executeGenericQuery(fetchDisposal, params, null);
                    prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
                    FindLastGeneratedDeal(prevDisposal, env);
                    prevDisposalID = (String) prevDisposal.getDataMap().get(SWT_DISPOSALID);
                    setF_OUT_prevDisposalID(prevDisposalID);

                }

                NewDecideMessage();
                if (DealType.equalsIgnoreCase(BRKDEP) || DealType.equalsIgnoreCase(BRKLON)) {
                    String code = (String) prevDisposal.getDataMap().get(TRANSACTION_STATUS);
                    if (code.startsWith(AM))
                        code = code.replaceAll(AM, BR);
                    else code = "BR" + code;
                    setF_OUT_Code_Word(code);
                }
            }
        }

        if (isSwiftCurrency(SwtMainCurrency, SwtContraCurrency)) {
            setF_OUT_Confirm_Flag(new Integer(confirm_Flag));
            setF_OUT_Cancel_Flag(new Integer(cancel_Flag));
            setF_OUT_Error_Flag(new Integer(error_Flag));
            setF_OUT_Payment_Flag(new Integer(payment_Flag));
            setF_OUT_receipt_Flag(new Integer(receipt_Flag));
            setF_OUT_prevCancelFlag(new Integer(prevCancelFlag));
            setF_OUT_DealType(DealType);
            setF_OUT_CancelMessage(new Boolean(cancelMessage));
            setF_OUT_DRCRConfirmation(new Integer(CRDRconfirm_Flag));
            setF_OUT_MainAccountCurrencyCode(currency1);
            Status_flag = NUM_0;
        }
        setF_OUT_prevMsgStatus(new Integer(0));

        if (Status_flag.compareTo(NUM_0) == 0) {
            if (confirm_Flag == 0 || CRDRconfirm_Flag == 0 || CRDRconfirm_Flag == 1 || receipt_Flag == 0 || payment_Flag == 0
                    || prevCancelFlag == 0 || cancel_Flag == 0)
                Status_flag = NUM_0;
            else Status_flag = NUM_1;
        }

        setF_OUT_Status_Flag(Status_flag);

        if (getF_OUT_Code_Word().length() == 0) {
            setF_OUT_Code_Word(getF_IN_Code_Word());
        }
        if (getF_OUT_message_Type().length() == 0) {
            setF_OUT_message_Type(getF_IN_Message_Type());
        }
        if (getF_OUT_Customer_Number().length() == 0) {
            setF_OUT_Customer_Number(getF_IN_Customer_Number());
        }
    }

    /**
     * This method compares the currencies
     * 
     * @param cur1
     * @param cur2
     * @return
     */
    private boolean isSwiftCurrency(String cur1, String cur2) {
        if (cur1.compareTo(YES) == 0 && cur2.compareTo(YES) == 0)
            return true;
        else return false;
    }

    /**
     * This method will find the last generated deal
     * 
     * @param prevDisposal1
     * @param env
     */
    private void FindLastGeneratedDeal(SimplePersistentObject prevDisposal1, BankFusionEnvironment env) {
        int Messagestatus = ((Integer) prevDisposal1.getDataMap().get(MESSAGE_STATUS)).intValue();
        String transtatus = (String) prevDisposal1.getDataMap().get(TRANSACTION_STATUS);
        String messagetype = (String) prevDisposal1.getDataMap().get(MESSAGE_TYPE);
        ArrayList params = new ArrayList();
        List disposalRecord = null;
        while ((Messagestatus == 0 && (transtatus.indexOf(NEW) == -1))
                || (messagetype.equals(NUM_350) && getF_IN_Code_Word().equals(CANCEL))) {
            params.clear();
            params.add((String) prevDisposal.getDataMap().get(PREV_SWT_DISPOSALID));
            disposalRecord = env.getFactory().executeGenericQuery(fetchDisposal1, params, null);
            prevDisposal = (SimplePersistentObject) disposalRecord.get(0);
            Messagestatus = ((Integer) prevDisposal.getDataMap().get(MESSAGE_STATUS)).intValue();
            transtatus = (String) prevDisposal.getDataMap().get(TRANSACTION_STATUS);
            messagetype = (String) prevDisposal.getDataMap().get(MESSAGE_TYPE);
        }
    }

    /**
     * This method will decide the messge type
     * 
     */
    private void NewDecideMessage() {
        String paymentReq = NO;

        if (mainCustomerSettingFound) {
            paymentReq = (String) mainCustConfig.getDataMap().get(PAY_MSG_REQUIRED);

        }

        if (getF_IN_Message_Type().compareTo(MT103) == 0) {
            if (paymentReq.compareTo(YES) == 0 || getF_IN_FXTransaction().intValue() == 7)
                MT103_Message_required();
        }
        else if (getF_IN_Message_Type().compareTo(MT202) == 0) {
            if (paymentReq.compareTo(YES) == 0)
                payment_Flag = 0;
        }
        else if (getF_IN_Message_Type().compareTo(MT320) == 0 || getF_IN_Message_Type().compareTo(MT330) == 0) {
            // if (fixDepositLoanConfirmation.compareTo(YES) == 0)
            MT320_MT330_Message_required();
        }
        else if (getF_IN_Message_Type().compareTo(MT300) == 0) {

            MT300_Message_required();
        }
        else if (getF_IN_Message_Type().compareTo(MT110) == 0) {
            payment_Flag = 9;
            receipt_Flag = 9;
            confirm_Flag = 0;

        }
        else if (getF_IN_Message_Type().compareTo(MT200) == 0 || getF_IN_Message_Type().compareTo(MT205) == 0
                || getF_IN_Message_Type().compareTo(MT900) == 0 || getF_IN_Message_Type().compareTo(MT910) == 0) {
            Other_Message_required();
        }

        if (getF_IN_DRCRFlag().compareTo("0") == 0 || getF_IN_DRCRFlag().compareTo("1") == 0
                || getF_IN_DRCRFlag().compareTo("2") == 0)
            Other_Message_required();
    }

    /**
     * This method will cancel the decide message
     */
    private void CancelDecideMessage() {
        if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_PAYMENT_FLAG)).intValue() == 1
                || ((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_RECEIPT_FLAG)).intValue() == 1
                || ((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)).intValue() == 1
                || ((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 2
                || ((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 3)
            updateFlagsOnCancelOrAmend();
        else if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_PAYMENT_FLAG)).intValue() == 0
                || ((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_RECEIPT_FLAG)).intValue() == 0
                || ((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)).intValue() == 0
                || ((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 0)
            cancelMessage = true;
    }

    /**
     * This method will amend the decide message
     */
    private void AmendDecideMessage() {
        if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_PAYMENT_FLAG)).intValue() == 1
                || ((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_RECEIPT_FLAG)).intValue() == 1
                || ((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)).intValue() == 1
                || ((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 1) {
            prevCancelFlag = 0;

        }

        else if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_PAYMENT_FLAG)).intValue() == 0
                || ((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_RECEIPT_FLAG)).intValue() == 0
                || ((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)).intValue() == 0
                || ((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 0) {

            cancelMessage = true;
        }
        updateFlagsOnCancelOrAmend();
    }

    /**
     * This method will update flags for cancel or amend
     */
    private void updateFlagsOnCancelOrAmend() {
        if (DealType.compareTo(AMEND) == 0) {
            if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_PAYMENT_FLAG)).intValue() == 1)
                setF_OUT_prevpayment(new Integer(2));
            else setF_OUT_prevpayment(new Integer(9));
            if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_RECEIPT_FLAG)).intValue() == 1)
                setF_OUT_prevreciept(new Integer(2));
            else setF_OUT_prevreciept(new Integer(9));
            if (getF_IN_Message_Type().equals(NUM_103)) {
                if (((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)).intValue() == 1)
                    setF_OUT_prevconfirm(new Integer(2));
                else setF_OUT_prevconfirm(new Integer(9));
            }
            else setF_OUT_prevconfirm(((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)));

            if (((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 2)
                CRDRconfirm_Flag = 4;
            else if (((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 3)
                CRDRconfirm_Flag = 5;
            else CRDRconfirm_Flag = 9;
        }
        else {
            cancel_Flag = 0;
            if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_PAYMENT_FLAG)).intValue() == 1)
                payment_Flag = 2;
            else payment_Flag = 9;

            if (((Integer) prevDisposal.getDataMap().get(SWT_DISPOSAL_RECEIPT_FLAG)).intValue() == 1)
                receipt_Flag = 2;
            else receipt_Flag = 9;
            if (((Integer) prevDisposal.getDataMap().get(CONFIRMATION_FLAG)).intValue() == 1)
                confirm_Flag = 2;
            else confirm_Flag = 9;

            if (((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 2)
                CRDRconfirm_Flag = 4;
            else if (((Integer) prevDisposal.getDataMap().get(CR_DR_CONFIRMATION_FLAG)).intValue() == 3)
                CRDRconfirm_Flag = 5;
            else CRDRconfirm_Flag = 9;
        }
    }

    /**
     * This method checks whether to generate MT103 message
     */
    private void MT103_Message_required() {

        int coverFlag = 3;
        int mainCustomerCoverFlag = 3;
        if (contraCustomerSettingFound)
            coverFlag = ((Integer) contraCustConfig.getDataMap().get(COVER_MSG_FLAG)).intValue();

        if (mainCustomerSettingFound)

            mainCustomerCoverFlag = ((Integer) mainCustConfig.getDataMap().get(COVER_MSG_FLAG)).intValue();

        if (getF_IN_FXTransaction().intValue() == 1) {
            if (mainCustomerSwiftActive.compareTo(YES) == 0) {
                if (isMainAccountIsNostro) {
                    confirm_Flag = 0;
                    if (mainCustomerCoverFlag == 0 || mainCustomerCoverFlag == 2)
                        payment_Flag = 0;

                }

            }

            if (contraCustomerSwiftActive.compareTo(YES) == 0) {
                if (isContraAccountIsNostro) {
                    if (coverFlag == 0 || coverFlag == 1)
                        receipt_Flag = 0;
                }
            }

        }
        else if (getF_IN_FXTransaction().intValue() == 2) {

            confirm_Flag = 9;
            payment_Flag = 9;
            receipt_Flag = 9;

        }
        else {
            if (contraCustomerSwiftActive.compareTo(YES) == 0) {
                if (isContraAccountIsNostro) {
                    confirm_Flag = 0;
                    if (coverFlag == 0 || coverFlag == 2)
                        payment_Flag = 0;
                }

            }
            if (mainCustomerSwiftActive.compareTo(YES) == 0) {
                if (isMainAccountIsNostro) {
                    if (mainCustomerCoverFlag == 0 || mainCustomerCoverFlag == 1)
                        receipt_Flag = 0;
                }
            }
        }
    }

    /**
     * This method checks whether to generate MT320 or MT330 message
     */
    private void MT320_MT330_Message_required() {
        String financialInstitute = null;
        String BICode = null;
        int coverFlag = 3;
        String codeWord = getF_IN_Code_Word();
        if (getF_IN_Code_Word().trim().equalsIgnoreCase(AMEND))
            codeWord = (String) prevDisposal.getDataMap().get(TRANSACTION_STATUS);

        String fixDepositLoanConfirmation = (String) mainCustConfig.getDataMap().get(DEPOSIT_LOAN_CONFIRM_REQUIRED);
        if (mainCustomerSettingFound) {
            financialInstitute = (String) mainCustConfig.getDataMap().get(IS_FINANCIAL_INSTITUTE);
            BICode = (String) mainCustConfig.getDataMap().get(BIC_CODE);
        }
        else {
            financialInstitute = NO;
            BICode = " ";
        }

        if (contraCustomerSettingFound)
            coverFlag = ((Integer) contraCustConfig.getDataMap().get(COVER_MSG_FLAG)).intValue();

        if (getF_IN_IntPayCap().trim().compareTo(C) == 0)
            setF_OUT_DealOriginator(NUM_3);
        else if (getF_IN_IntPayCap().trim().compareTo(P) == 0)
            setF_OUT_DealOriginator(NUM_4);
        else if (getF_IN_IntPayCap().trim().compareTo(NUM_5) == 0)
            setF_OUT_DealOriginator(NUM_5);
        else if (getF_IN_IntPayCap().trim().compareTo(NUM_6) == 0)
            setF_OUT_DealOriginator(NUM_6);
        else if (getF_IN_IntPayCap().trim().compareTo(NUM_8) == 0)
            setF_OUT_DealOriginator(NUM_8);
        else if (getF_IN_IntPayCap().trim().compareTo(NUM_9) == 0)
            setF_OUT_DealOriginator(NUM_9);

        if (mainCustomerSwiftActive.compareTo(YES) == 0 && !getF_IN_Code_Word().trim().equalsIgnoreCase(PAYCAP)) {
            if ((financialInstitute.compareTo(YES) == 0) && (BICode.trim().length() > 0)
                    && (fixDepositLoanConfirmation.compareTo(YES) == 0))
                confirm_Flag = 0;
            else if ((codeWord.indexOf(NEWLON) != -1 || codeWord.indexOf(MATDEP) != -1 || codeWord.indexOf(BRKDEP) != -1)
                    && (fixDepositLoanConfirmation.compareTo(YES) == 0)) {
                setF_OUT_message_Type(NUM_103);
                setF_IN_Message_Type(NUM_103); // added to fix Bug#13783 Anand Pasunoori
                confirm_Flag = 0;
                setF_OUT_Code_Word(codeWord);
                if (codeWord.indexOf(MATDEP) != -1 && getF_IN_IntPayCap().trim().equalsIgnoreCase(C))
                    confirm_Flag = 9;
            }
        }
        if (getF_IN_IntPayCap().trim().equalsIgnoreCase(NUM_9) && !getF_IN_Message_Type().equalsIgnoreCase(NUM_103)) {
            confirm_Flag = 0;
        }
        if (contraCustomerSwiftActive.compareTo(YES) == 0) {
            if (isContraAccountIsNostro) {
                if (coverFlag == 0 || coverFlag == 1) {
                    if ((codeWord.indexOf(MATLON) != -1 && !getF_IN_IntPayCap().trim().equalsIgnoreCase(C))
                            || codeWord.indexOf(BRKLON) != -1 || codeWord.indexOf(NEWDEP) != -1 || codeWord.indexOf(ROLLODEC) != -1
                            || codeWord.indexOf(ROLDEINC) != -1
                            || ((codeWord.indexOf(NEW) != -1) && getF_IN_Message_Type().compareTo(MT330) == 0)) {
                        receipt_Flag = 0;
                    }
                    if (codeWord.equals(NEW) && getF_IN_Message_Type().equals(MT320)) {
                        receipt_Flag = 0;
                    }
                }

                if (coverFlag == 0 || coverFlag == 2) {
                    if (((codeWord.indexOf(MATDEP) != -1 && !getF_IN_IntPayCap().trim().equalsIgnoreCase(C))
                            || codeWord.indexOf(BRKDEP) != -1 && !(getF_OUT_DealOriginator().equals(NOMT202)))
                            || codeWord.indexOf(NEWLON) != -1 || codeWord.indexOf(ROLDEDEC) != -1
                            || codeWord.indexOf(ROLLOINC) != -1) {
                        payment_Flag = 0;
                    }
                }

            }
        }

        if (codeWord.indexOf(ROLLODEC) != -1 || codeWord.indexOf(NEWDEP) != -1 || codeWord.indexOf(ROLLOSAM) != -1
                || codeWord.indexOf(ROLDEINC) != -1) {
            if (getF_IN_Message_Type().compareTo(MT330) == 0) {
                if (mainCustomerSwiftActive.compareTo(YES) == 0)
                    receipt_Flag = 0;
            }
        }
        else {
            if (isContraAccountIsNostro) {
                if (codeWord.indexOf(ROLDEDEC) != -1 || codeWord.indexOf(ROLLOINC) != -1) {
                    if (contraCustomerSwiftActive.compareTo(YES) == 0)
                        payment_Flag = 0;
                }
            }
        }

        if (getF_IN_Message_Type().compareTo(MT350) == 0) {
            payment_Flag = 0;
        }
        if (getF_IN_Code_Word().trim().equalsIgnoreCase(PAYCAP))
            setF_OUT_Code_Word(NEW);
        setF_OUT_MT350Required(MT350MessageRequired());
    }

    /*
     * The following method should be changed later once the partial payment of interest is made.
     * This is taken care only for Deposit. For Loans it should be taken care once it is implemented
     * for Loans.
     */
    private Boolean MT350MessageRequired() {
        String adviceMessage = CommonConstants.EMPTY_STRING;
        Boolean Required350 = new Boolean(false);

        if (mainCustomerSettingFound)
            adviceMessage = (String) mainCustConfig.getDataMap().get(ADVICE_MESSAGE);
        if (getF_IN_Message_Type().equals(NUM_350)
                || (getF_IN_Code_Word().trim().substring(0, 3).compareTo(ROL) == 0 && getF_IN_PayReceiveFlag().compareTo(P) == 0)) {
            if (adviceMessage.compareTo(Y) == 0 && getF_IN_IntPayCap().compareTo(P) == 0) {
                if (mainCustomerSwiftActive.compareTo(YES) == 0)
                    Required350 = new Boolean(true);
            }
        }
        return Required350;
    }

    /**
     * This method checks whether to generate MT300 message
     */
    private void MT300_Message_required() {
        String clientBICode = null;
        int coverFlag = 3, contraCustomerCoverFlag = 3;
        String mainAccountBICCOde = null;

        if (mainCustomerSettingFound) {
            coverFlag = ((Integer) mainCustConfig.getDataMap().get(COVER_MSG_FLAG)).intValue();
            mainAccountBICCOde = (String) mainCustConfig.getDataMap().get(BIC_CODE);
        }

        if (contraCustomerSettingFound)
            contraCustomerCoverFlag = ((Integer) contraCustConfig.getDataMap().get(COVER_MSG_FLAG)).intValue();

        if (FXCustomerSettingFound)
            clientBICode = fxCustomer.getF_BICCODE();
        else clientBICode = EMPTY_STRING;

        if (clientBICode.trim().length() > 0) {
            if (fxCustomerSwiftActive.compareTo(YES) == 0 && fxCustomer.getF_FXDEALCONFIRMREQUIRED().compareTo(Y) == 0)
                confirm_Flag = 0;
            if (mainAccountBICCOde != null) {
                if (mainCustomerSwiftActive.compareTo(YES) == 0 && mainAccountBICCOde.trim().length() > 0) {
                    if (isMainAccountIsNostro) {
                        if (coverFlag == 0 || coverFlag == 2) {
                            payment_Flag = 0;
                        }
                    }
                }
            }
        }

        if (contraCustomerSwiftActive.compareTo(YES) == 0) {
            if (isContraAccountIsNostro) {
                if (contraCustomerCoverFlag == 0 || contraCustomerCoverFlag == 1) {
                    receipt_Flag = 0;
                }
            }
        }
        /*
         * When it is an FX option deal then no payment message (MT 202)and no receipt mesage (MT
         * 210 ) should be generated . Hence check if it is an option deal , set the receipt and
         * payment message to 9 , which would prevent the message generation.
         */
        if (getF_IN_FXTransaction().intValue() == 2) {
            payment_Flag = 9;
            receipt_Flag = 9;
        }
    }

    /**
     * This method checks whether to generate other messages
     */
    private void Other_Message_required() {
        int coverFlag = 3;
        String paymentReq = NO;
        String codeWord = getF_IN_Code_Word();
        if (mainCustomerSettingFound) {
            paymentReq = (String) mainCustConfig.getDataMap().get(PAY_MSG_REQUIRED);
        }

        if (contraCustomerSettingFound)
            coverFlag = ((Integer) contraCustConfig.getDataMap().get(COVER_MSG_FLAG)).intValue();

        if (paymentReq.compareTo(YES) == 0 && contraCustomerSettingFound
                && (getF_IN_Message_Type().compareTo(MT200) == 0 || getF_IN_Message_Type().compareTo(MT205) == 0))
            confirm_Flag = 0;

        /*
         * The condition is to check that the MT900/MT910 should not be generated only if the
         * Deposit / Rollover is not with same details . The MT900/MT910 should not be generate if
         * it is a Maturity with capitalise option .
         */

        if (((getF_IN_Message_Type().compareTo(MT900) == 0 || getF_IN_DRCRFlag().compareTo(NUM_0) == 0)
                && !(codeWord.indexOf(ROLLOSAM) != -1) // the transaction is not Roll over loan with
                                                       // same details
                && !(codeWord.indexOf(ROLDESAM) != -1) // the transaction is not Roll over deposit
                                                       // with same details
                && !(((codeWord.indexOf(MATDEP) != -1) // the transaction is not MATDEP with
                                                       // capitalise
                        || (codeWord.indexOf(MATLON) != -1)) && (getF_IN_IntPayCap().trim().compareTo(C) == 0))))

        {
            CRDRconfirm_Flag = 0;
            if (getF_OUT_DealOriginator().equalsIgnoreCase(F))
                payment_Flag = 9;
        }
        if (((getF_IN_Message_Type().compareTo(MT910) == 0 || getF_IN_DRCRFlag().compareTo(NUM_1) == 0)
                && !(codeWord.indexOf(ROLLOSAM) != -1) // the transaction is not Roll over loan with
                                                       // same details
                && !(codeWord.indexOf(ROLDESAM) != -1) // the transaction is not Roll over deposit
                                                       // with same details
                && !(((codeWord.indexOf(MATDEP) != -1) // the transaction is not MATDEP with
                                                       // capitalise
                        || (codeWord.indexOf(MATLON) != -1)) && (getF_IN_IntPayCap().trim().compareTo(C) == 0)))) {

            CRDRconfirm_Flag = 1;
            if (getF_OUT_DealOriginator().equalsIgnoreCase(F))
                payment_Flag = 9;
        }
        if (getF_IN_Message_Type().equalsIgnoreCase(NUM_350)) {
            if (contraCustomerSwiftActive.compareTo(YES) == 0) {
                if (isContraAccountIsNostro) {
                    if (coverFlag == 0 || coverFlag == 1) {
                        payment_Flag = 0;
                    }
                }
            }
        }
        if (contraCustomerSwiftActive.compareTo(YES) == 0) {
            if (isContraAccountIsNostro) {
                if (getF_IN_Message_Type().compareTo(MT200) == 0) {// here
                    // removed
                    // the 910
                    // check
                    if (coverFlag == 0 || coverFlag == 1) {
                        receipt_Flag = 0;
                    }
                } /*
                   * else if (getF_IN_Message_Type().compareTo(MT900) == 0){ removing this code
                   * becouse we dont nee 202 with 900 if (coverFlag == 0 || coverFlag == 2){
                   * payment_Flag = 0; } }
                   */
            }
        }
    }
}
