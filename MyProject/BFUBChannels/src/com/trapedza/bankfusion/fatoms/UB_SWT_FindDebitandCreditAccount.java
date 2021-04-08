/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_FindDebitandCreditAccount;

/**
 * @author Gaurav Aggarwal & Vipesh
 * 
 */
public class UB_SWT_FindDebitandCreditAccount extends AbstractUB_SWT_FindDebitandCreditAccount {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    private transient final static Log logger = LogFactory.getLog(UB_SWT_FindDebitandCreditAccount.class.getName());
    private String DebitAccountNumber = null;
    private String CreditAccountNumber = null;
    BankFusionEnvironment env = null;
    static String flag = "";
    private static String SWTCUSTOMERWHERECLAUSE = " WHERE " + IBOSwtCustomerDetail.BICCODE + " = ?";
    @SuppressWarnings("FBPE")
    private static String FINDBRANCHQUERY = "SELECT " + IBOBranch.BICCODE + " FROM " + IBOBranch.BONAME + " WHERE "
            + IBOBranch.BICCODE + " = ?";
    private boolean accountFromMsg = false;

    public UB_SWT_FindDebitandCreditAccount(BankFusionEnvironment env) {
        super(env);

    }

    @SuppressWarnings("FBPE")
    public void process(BankFusionEnvironment environment) {

        this.env = environment;
        setF_OUT_BenficiaryCheckRequired(false);
        try {
            flag = "D";
            if ("N".equals(getF_IN_FromRemitScreen())) {
                String ThirdReimbursementInstitution = getF_IN_ThirdReimbursementInstitution();
                String ReceiversCorrespondent = getF_IN_ReceiversCorrespondent();
                String SendersCorrespondent = getF_IN_SendersCorrespondent();
                // fix for artf571608,when the Account number is not configured for
                // the provided BIC
                // then it
                // has to be sent for repair
                if (ThirdReimbursementInstitution.trim().length() == 0) {
                    DebitAccountNumber = getAccountNumberFromText(ReceiversCorrespondent, getF_IN_ReceiversCorrespondentOption());
                }
                else {
                    DebitAccountNumber = getAccountNumberFromText(ThirdReimbursementInstitution,
                            getF_IN_ThirdReimbursementInstitutionOption());
                }
                if (ThirdReimbursementInstitution.trim().length() == 0 && ReceiversCorrespondent.trim().length() == 0) {
                    DebitAccountNumber = getAccountNumberFromText(SendersCorrespondent, getF_IN_SendersCorrespondentOption());
                }
                if (ThirdReimbursementInstitution.trim().length() == 0 && ReceiversCorrespondent.trim().length() == 0
                        && SendersCorrespondent.trim().length() == 0) {
                    DebitAccountNumber = getAccountNumberFromBIC(getF_IN_Sender());
                }

                // debit account number from module Configuration
                if (StringUtils.isEmpty(DebitAccountNumber)) {
                    DebitAccountNumber = getDebitAccountNumberFromModuleConfig();
                }

                if (DebitAccountNumber.trim().length() == 0) {
                    setF_OUT_ErrorNumber("9700");
                    if (logger.isInfoEnabled()) {
                        logger.info(" Debit Account ErrorNumber: 9700");
                    }
                }
                // debit account is just a single character /D in 53B
                if (!isAccountExist(DebitAccountNumber.trim())) {
                    setF_OUT_ErrorNumber("9700");
                }
            }
            else {
                DebitAccountNumber = getF_IN_DebitAccount();
            }
        }
        catch (BankFusionException e) {
            logger.error("Debit Account ErrorNumber: 9700" + ExceptionUtil.getExceptionAsString(e));
            setF_OUT_ErrorNumber(StringUtils.isNotBlank(getF_OUT_ErrorNumber()) && getF_OUT_ErrorNumber().length() != 0
                    ? getF_OUT_ErrorNumber(): "9700");
        }
        catch (Exception e) {
            logger.error("Debit Account ErrorNumber: 9700" + ExceptionUtil.getExceptionAsString(e));
            setF_OUT_ErrorNumber(StringUtils.isNotBlank(getF_OUT_ErrorNumber()) && getF_OUT_ErrorNumber().length() != 0
                    ? getF_OUT_ErrorNumber(): "9700");
        }

        try {
            flag = "C";
            if ("N".equals(getF_IN_FromRemitScreen())) {
                accountFromMsg = false;
                if (logger.isInfoEnabled()) {
                    logger.info("MatrixCreditValue " + getF_IN_MatrixCreditValue());
                }
                // artf992641 starts
                String accWith = getF_IN_AccountWith();
                if ((accWith.length()) == 8) {
                    accWith = accWith + "XXX";
                }
                ArrayList params = new ArrayList();
                params.add(accWith);
                List branchList = null;
                if (accWith != null && accWith.length() != 0) {
                    branchList = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(FINDBRANCHQUERY, params, null,
                            false);
                    if (!(branchList.isEmpty()))
                        CreditAccountNumber = getAccountNumberFromText(getF_IN_BeneficiaryCustomer(),
                                getF_IN_BeneficiaryCustomerOption());
                    else CreditAccountNumber = splitAccountNumber(getF_IN_MatrixCreditValue().charAt(0));
                }
                else CreditAccountNumber = splitAccountNumber(getF_IN_MatrixCreditValue().charAt(0));
                // artf992641 ends
                if (!CreditAccountNumber.isEmpty() && accountFromMsg) {
                    List AccountList = FinderMethods.findAccountInfoByAccountID(CreditAccountNumber, environment, null);
                    IBOAttributeCollectionFeature accountInfo = (IBOAttributeCollectionFeature) AccountList.get(0);
                    CreditAccountNumber = accountInfo.getBoID();
                }
                if (CreditAccountNumber.length() == 0) {
                    setF_OUT_ErrorNumber(StringUtils.isNotBlank(getF_OUT_ErrorNumber()) && getF_OUT_ErrorNumber().length() != 0
                            ? getF_OUT_ErrorNumber()
                            : "9712");
                }
            }
            else {
                CreditAccountNumber = getF_IN_CreditAccount();
            }
        }
        catch (BankFusionException e) {
            logger.error(" Credit Account ErrorNumber = 9712" + ExceptionUtil.getExceptionAsString(e));
            setF_OUT_ErrorNumber(StringUtils.isNotBlank(getF_OUT_ErrorNumber()) && getF_OUT_ErrorNumber().length() != 0
                    ? getF_OUT_ErrorNumber(): "9712");
        }
        catch (Exception e) {
            logger.error(" Credit Account ErrorNumber = 9712" + ExceptionUtil.getExceptionAsString(e));
            setF_OUT_ErrorNumber(StringUtils.isNotBlank(getF_OUT_ErrorNumber()) && getF_OUT_ErrorNumber().length() != 0
                    ? getF_OUT_ErrorNumber(): "9712");
        }

        setF_OUT_DebitAccountNumber(DebitAccountNumber.trim());
        setF_OUT_CreditAccountNumber(CreditAccountNumber.trim());
        setF_OUT_OrderingCustBIC(getOrderingCustomerName(getF_IN_OrderingCustomer()));
        if (logger.isInfoEnabled()) {
            logger.info("DebitAccountNumber" + DebitAccountNumber);
            logger.info("CreditAccountNumber" + CreditAccountNumber);
        }

    }

    /**
     * This method is used to split the account number from text. This method is used for credit
     * account.
     * 
     * @param text
     * @return
     */
    private String splitAccountNumber(char text) {
        String CreditAccountNumber = "";
        String[] arrays = null;
        switch (text) {
            case 'I':
                CreditAccountNumber = getAccountNumberFromText(getF_IN_Intermediary(), getF_IN_IntermediaryOption());
                break;
            case 'A':
                CreditAccountNumber = getAccountNumberFromText(getF_IN_AccountWith(), getF_IN_AccountWithOption());
                break;
            case 'N':
                CreditAccountNumber = getF_IN_NostroAccount();
                break;
            default:
                if (getF_IN_MessageType().equals("MT103")) {
                    CreditAccountNumber = getAccountNumberFromText(getF_IN_BeneficiaryCustomer(),
                            getF_IN_BeneficiaryCustomerOption());
                    if (!CreditAccountNumber.equals(CommonConstants.EMPTY_STRING)) {
                        setF_OUT_isBeneficiaryAccount(true);
                    }
                    setF_OUT_BenficiaryCheckRequired(true);
                }
                else {
                    CreditAccountNumber = getAccountNumberFromText(getF_IN_BeneficiaryInstitute(),
                            getF_IN_BeneficiaryInstituteOption());
                    break;
                }
        }
        return CreditAccountNumber;
    }

    /**
     * This method is used to find out the account from text.
     * 
     * @param text
     * @param option
     * @return
     */
    private String getAccountNumberFromText(String text, String option) {
        String[] arrays = splitpartyIdentifier(text);
        String AccountNumber = "";
        if (arrays.length > 0) {
            // when 53B comes in the message like - 53B:/D/21986201 - 21986201 is the account number
            if (arrays[0].startsWith("/") && arrays[0].startsWith("/", 2) && !arrays[0].startsWith("//")) {
                AccountNumber = arrays[0].substring(3);
                accountFromMsg = true;
            }
            // when 53B comes in the message like - 53B:/21986201 - 21986201 is the account number
            else if (arrays[0].startsWith("/") && !arrays[0].startsWith("//")) {
                AccountNumber = arrays[0].substring(1);
                accountFromMsg = true;
            }

            else {
                AccountNumber = getAccountNumberFromBIC(arrays[0]);
            }
        }
        return AccountNumber;
    }

    /**
     * This method is used find out the account number from identifier code.
     * 
     * @param BIC
     * @return
     */
    private String getAccountNumberFromBIC(String BIC) {
        String accountId = CommonConstants.EMPTY_STRING;
        if ((BIC.length()) == 8) {
            BIC = BIC + "XXX";
        }
        ArrayList params = new ArrayList();
        Iterator customerList = null;
        IBOSwtCustomerDetail swtCustomerDetail = null;
        params.add(BIC);
        customerList = env.getFactory().findByQuery(IBOSwtCustomerDetail.BONAME, SWTCUSTOMERWHERECLAUSE, params, 1);
        if (customerList.hasNext()) {
            swtCustomerDetail = (IBOSwtCustomerDetail) customerList.next();
            if (flag.equals("D")) {
                accountId = swtCustomerDetail.getF_DRACCOUNTNUMBER();
                return getAccountFromAccountAndPseudonym(accountId);
            }
            else {
                accountId = swtCustomerDetail.getF_CRACCOUNTNUMBER();
                return getAccountFromAccountAndPseudonym(accountId);
            }
        }
        setF_OUT_DerivedBIC(BIC);

        return accountId;
    }

    /**
     * This method is used to split the party identifier code.
     * 
     * @param text
     * @return
     */
    private String[] splitpartyIdentifier(String text) {
        String[] arrays = new String[3];
        arrays = text.split("[$]");
        return arrays;
    }

    /**
     * Finds the account attached to the Pseudonym in TransactionCurrency with Context as Currency
     * 
     * @param AccountNumber
     * @return
     */
    private String getAccountFromAccountAndPseudonym(String AccountNumber) {
        String accNumber = CommonConstants.EMPTY_STRING;
        if (!(AccountNumber.trim().length() == 0)) {
            if (isAccountExist(AccountNumber.trim())) {
                accNumber = AccountNumber.trim();
            }
            else {
                String txnCurrencyCode = getF_IN_TxnCurrencyCode();
                IBOAttributeCollectionFeature accountValues = FinderMethods.findAccountByPseudonameAndContextValue(
                        "%CURRENCY%" + txnCurrencyCode + "%" + AccountNumber.trim(), txnCurrencyCode, Boolean.TRUE, env, null);
                if (!(accountValues == null)) {
                    accNumber = accountValues.getBoID();
                }
            }
        }
        return accNumber;
    }

    /**
     * This method is used to check account exist or not.
     * 
     * @param accountId
     * @return
     */
    private Boolean isAccountExist(String accountId) {
        @SuppressWarnings("FBPE")
        IBOAccount accountDtls = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountId, Boolean.TRUE);
        return (null != accountDtls && !accountDtls.getBoID().isEmpty()) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * To get the Ordering customer name from the Ordering Customer tag of MT103 message. It
     * extracts the ordering customer name for different options, i.e. 'A' or 'F' or 'K'.
     * 
     * @param OrderingCust
     * @return
     */
    private String getOrderingCustomerName(String OrderingCust) {
        // Check for Null
        if (OrderingCust == null || OrderingCust.length() == 0) {
            return OrderingCust;
        }
        boolean PartyIdentifier = true;
        String OrderingCustName = null;
        String subOrderingCustName = null;
        int beginIndex = 0;
        /* check if 1st character is "/" */
        if (OrderingCust.substring(0, 1).equals("/")) {
            PartyIdentifier = true;
        }
        else {
            PartyIdentifier = false;
        }
        if (PartyIdentifier) {
            beginIndex = OrderingCust.indexOf('$');
            subOrderingCustName = OrderingCust.substring(beginIndex + 1);

        }
        else {
            beginIndex = OrderingCust.indexOf('$');
            if (beginIndex > -1) {
                subOrderingCustName = OrderingCust.substring(0, beginIndex);
            }
            else {
                subOrderingCustName = OrderingCust.substring(0);
            }
        }

        int nextBeginIndex = subOrderingCustName.indexOf('$');

        if (nextBeginIndex == -1) {
            OrderingCustName = subOrderingCustName;
        }
        else {
            int endIndex = (beginIndex + 1) + nextBeginIndex;
            OrderingCustName = OrderingCust.substring(beginIndex + 1, endIndex);

            if (OrderingCustName.contains("/")) {
                OrderingCustName = OrderingCustName.substring(2);
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("Input Ordering Cust  : " + OrderingCust);
            logger.info("F_OUT_OrderingCustBIC : " + OrderingCustName);
        }
        return OrderingCustName;
    }

    /**
     * Method Description:Get debit account number from module configuration Module Name: FEX and
     * Param Value: DEFAULT_NOSTRO
     * 
     * @return
     */
    private String getDebitAccountNumberFromModuleConfig() {
        String accountNumber = StringUtils.EMPTY;
        String defaultNostroConfiguration = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.CHANNELID_SWIFT,
                "GenerateMessageToDefaultNostro");
        if (StringUtils.isNotEmpty(defaultNostroConfiguration) && defaultNostroConfiguration.equalsIgnoreCase("Y")) {
            String psuedonymVale = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.FEX_MODULE_ID,
                    PaymentSwiftConstants.NOSTRO_PSEDONYM_KEY);
            if (StringUtils.isNotEmpty(psuedonymVale)) {
                accountNumber = getAccountFromAccountAndPseudonym(psuedonymVale);
            }
        }

        return accountNumber;
    }

}
