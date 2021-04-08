/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMControlDetails.java,v $
 * Revision 1.8  2008/11/27 20:52:12  bhavyag
 * reverted changes of external branch code
 *
 * Revision 1.5  2008/10/22 04:04:23  bhavyag
 * reverted the changes for transaction codes.
 *
 * Revision 1.3  2008/10/17 09:17:06  bhavyag
 * removed the commented code.
 *
 * Revision 1.6  2008/08/12 20:15:29  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.4.4.2  2008/07/04 22:04:29  thrivikramj
 * Fix for the Bug 10921
 *
 * Revision 1.4.4.1  2008/07/03 17:55:25  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.6  2008/06/16 15:18:45  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.5  2008/06/12 10:51:00  arun
 *  RIO on Head
 *
 * Revision 1.4  2008/01/18 07:15:53  sushmax
 * Updated files
 *
 * Revision 1.3  2008/01/10 14:25:07  prashantk
 * Updations for Incorporating Module Config. Changes for ATM
 *
 * Revision 1.3  2007/11/12 10:15:55  sushmax
 * ATMConfiguration for Build 28 del
 *
 * Revision 1.2.4.1  2007/08/08 18:38:19  prashantk
 * Changes made to a few Attribute Types and Data Types to make it more Meaningful
 *
 * Revision 1.2  2007/05/16 08:32:00  sushmax
 * ATM Configuration
 *
 */
package com.trapedza.bankfusion.atm.sparrow.configuration;

import java.math.BigDecimal;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * The ATMControlDetails contains control file entries that have been configured in ATMConfig.xml
 */
public class ATMControlDetails {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
	 */

    public ATMControlDetails(BankFusionEnvironment environment) {
        this.env = environment;
    }

    private BankFusionEnvironment env;
    // ATM Keys
    private String ATM_BASE_CURRENCY = "ATM_BASE_CURRENCY";
    private String BALANCE_USED_FOR_AVAILABE_BALANCE = "BALANCE_USED_FOR_AVAILABE_BALANCE";
    private String DEFAULT_ATM_TRANSACTION = "DEFAULT_ATM_TRANSACTION";
    private String BRANCH_NUMBER_LENGTH = "BRANCH_NUMBER_LENGTH";
    private String STATEMENT_FLAG = "STATEMENT_FLAG";
    private String INTERBRANCH_FLAG = "INTERBRANCH_FLAG";

    private String SUSPECT_REVERSAL_NARRATIVE = "SUSPECT_REVERSAL_NARRATIVE";
    private String POSSIBLE_DUPLICATE_NARRATIVE = "POSSIBLE_DUPLICATE_NARRATIVE";
    private String CORRECRION_NARRATIVE = "CORRECRION_NARRATIVE";
    private String SOLICITED_MESSAGE_FLAG = "SOLICITED_MESSAGE_FLAG";
    private String BALANCE_DOWNLOAD_TYPE = "BALANCE_DOWNLOAD_TYPE";
    private String SETTLEMENT_NARRATIVE = "SETTLEMENT_NARRATIVE";
    private String DEFAULT_BLOCKING_PERIOD = "DEFAULT_BLOCKING_PERIOD";
    private String AUTH_ALLOWEED_PERCENTAGE = "AUTH_ALLOWEED_PERCENTAGE";
    private String DATE_USED_FOR_POSTING = "DATE_USED_FOR_POSTING";
    private String SHARED_SWITCH = "SHARED_SWITCH";
    // //Input tag added for ISO08583
    private String ATM_VALUE_DATE = "ATM_VALUE_DATE";
    private String ATM_COMMISION_RECEV_ACC = "ATM_COMMISION_RECEV_ACC";
    private String ATM_COMMISION_TRNS_CODE = "ATM_COMMISION_TRNS_CODE";
    private String COMMISION_BRANCH = "COMMISION_BRANCH";

    // Account Status
    private String INVALID_CARD = "INVALID_CARD";
    private String NOT_ON_CARD = "NOT_ON_CARD";
    private String NOT_AN_ATM_ACCOUNT = "NOT_AN_ATM_ACCOUNT";
    private String NOT_AN_GL_ACCOUNT = "NOT_AN_GL_ACCOUNT";
    private String INACTIVE_ACCOUNT = "INACTIVE_ACCOUNT";
    private String NO_PASSWORD_REQUIRED = "NO_PASSWORD_REQUIRED";
    private String PASSWORD_REQUIRED_FOR_POSTING = "PASSWORD_REQUIRED_FOR_POSTING";
    private String ACCOUNT_STOPPED = "ACCOUNT_STOPPED";
    private String ACCOUNT_STOPPED_PASWD_REQD_FOR_POSTING_AND_ENQUIRY = "ACCOUNT_STOPPED_PASWD_REQD_FOR_POSTING_AND_ENQUIRY";
    private String NO_DR_TRANSACTIONS_ALLOWED = "NO_DR_TRANSACTIONS_ALLOWED";
    private String PASS_REQD_FOR_ALL_TRANS = "PASS_REQD_FOR_ALL_TRANS";
    private String NO_CR_TRANSACTIONS_ALLOWED = "NO_CR_TRANSACTIONS_ALLOWED";
    private String PASS_REQD_FOR_CR_TRANS = "PASS_REQD_FOR_CR_TRANS";;
    private String PASS_REQD_FOR_DR_TRANS = "PASS_REQD_FOR_DR_TRANS";
    private String PASSWORD_REQD_FOR_ENQUIRY = "PASSWORD_REQD_FOR_ENQUIRY";
    private String HOT_CARD_STATUS = "HOT_CARD_STATUS";
    private String INVALID_CURRENCY_CODE_STATUS = "INVALID_CURRENCY_CODE_STATUS";
    // POS configuration
    private String POS_HOLDING_ACCOUNTS = "POS_HOLDING_ACCOUNTS";
    private String DEFAULT_POS_TRANSACTION = "DEFAULT_POS_TRANSACTION";

    // Credit/Debit suspense account
    private String ATM_CR_SUSPENSE_ACCOUNT = "ATM_CR_SUSPENSE_ACCOUNT";
    private String ATM_DR_SUSPENSE_ACCOUNT = "ATM_DR_SUSPENSE_ACCOUNT";
    private String NETWORK_DR_SUSPENSE_ACCOUNT = "NETWORK_DR_SUSPENSE_ACCOUNT";
    private String NETWORK_CR_SUSPENSE_ACCOUNT = "NETWORK_CR_SUSPENSE_ACCOUNT";
    private String CARD_HOLDERS_SUSPENSE_ACCOUNT = "CARD_HOLDERS_SUSPENSE_ACCOUNT";
    private String POS_DR_SUSPENSE_ACCOUNT = "POS_DR_SUSPENSE_ACCOUNT";
    private String POS_CR_SUSPENSE_ACCOUNT = "POS_CR_SUSPENSE_ACCOUNT";
    private String DEST_ACCOUNT_LENGTH = "DEST_ACCOUNT_LENGTH";

    // Priority Configuration
    private String PRIORITY1 = "PRIORITY1";
    private String PRIORITY2 = "PRIORITY2";
    private String PRIORITY3 = "PRIORITY3";
    private String PRIORITY4 = "PRIORITY4";
    private String PRIORITY5 = "PRIORITY5";

    // Charges & commission
    private String CHARGECOMMISSIONCODE = "CHARGECOMMISSIONCODE";
    private String CHARGEFEESCODE = "CHARGEFEESCODE";

    // External Branch Code settings
    private String EXTERNAL_BRANCH_CODE = "EXTERNAL_BRANCH_CODE";

    // New fields for SmartCard
    private String SMART_CARD_SUPPORTED = "SMART_CARD_SUPPORTED";
    private String SC_PURSE_POOL_ACCOUNT = "SC_PURSE_POOL_ACCOUNT";
    private String SC_MERCHANT_POOL_ACCOUNT = "SC_MERCHANT_POOL_ACCOUNT";
    private String SC_CREDIT_SUSPENSE_ACCOUNT = "SC_CREDIT_SUSPENSE_ACCOUNT";
    private String SC_DEBIT_SUSPENSE_ACCOUNT = "SC_DEBIT_SUSPENSE_ACCOUNT";
    private String SC_MERCHANT_CREDIT_SUSPENSE_ACCOUNT = "SC_MERCHANT_CREDIT_SUSPENSE_ACCOUNT";
    private String SC_MERCHANT_DEBIT_SUSPENSE_ACCOUNT = "SC_MERCHANT_DEBIT_SUSPENSE_ACCOUNT";
    private String SC_BLOCKING_PERIOD = "SC_BLOCKING_PERIOD";
    private String SC_DEFAULT_TRANSACTION_TYPE = "SC_DEFAULT_TRANSACTION_TYPE";
    private String PROCESS_MAGSTRIPE_TXNS = "PROCESS_MAGSTRIPE_TXNS";
    private String POS_OUTWARD_ACCOUNT = "POS_OUTWARD_ACCOUNT";

    public String getAtmBaseCurrency() {
        String value = getValue(ATM_BASE_CURRENCY).toString();
        return value;
    }

    public String getAtmCrSuspenseAccount() {
        String value = getValue(ATM_CR_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getAtmDrSuspenseAccount() {
        String value = getValue(ATM_DR_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getAtmTransactionType() {
        String value = getValue(DEFAULT_ATM_TRANSACTION).toString();
        return value;
    }

    public BigDecimal getAuthAllowedPercentage() {
        Object val = getValue(AUTH_ALLOWEED_PERCENTAGE);
        String valu = val.toString();
        BigDecimal value = new BigDecimal(valu);
        return value;
    }

    public String getBalanceDownloadType() {
        String value = getValue(BALANCE_DOWNLOAD_TYPE).toString();
        return value;
    }

    public Integer getBranchNumberLength() {
        Integer value = new Integer(getValue(BRANCH_NUMBER_LENGTH).toString());
        return value;
    }

    public String getCardHolderSuspenseAccount() {
        String value = getValue(CARD_HOLDERS_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getClearedOrBookBalance() {
        String value = getValue(BALANCE_USED_FOR_AVAILABE_BALANCE).toString();
        return value;
    }

    public String getCommissionCurrency() {
        return CommonConstants.EMPTY_STRING;
    }

    public String getCorrectionTxnNarr() {
        String value = getValue(CORRECRION_NARRATIVE).toString();
        return value;
    }

    public Integer getDefaultBlockingPeriod() {
        Integer value = new Integer(getValue(DEFAULT_BLOCKING_PERIOD).toString());
        return value;
    }

    public String getHotCardStatus() {
        String value = getValue(HOT_CARD_STATUS).toString();
        return value;
    }

    public String getInactiveAccount() {
        String value = getValue(INACTIVE_ACCOUNT).toString();
        return value;
    }

    public boolean isInterBranchFlag() {
        Boolean value = new Boolean(getValue(INTERBRANCH_FLAG).toString());
        return value.booleanValue();
    }

    public String getNetworkCrSuspenseAccount() {
        String value = getValue(NETWORK_CR_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getNetworkDrSuspenseAccount() {
        String value = getValue(NETWORK_DR_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getNoCard() {
        String value = getValue(INVALID_CARD).toString();
        return value;
    }

    public String getNoCrTxnAllowed() {
        String value = getValue(NO_CR_TRANSACTIONS_ALLOWED).toString();
        return value;
    }

    public String getNoDrTxnAllowed() {
        String value = getValue(NO_DR_TRANSACTIONS_ALLOWED).toString();
        return value;
    }

    public String getNoPasswordRequired() {
        String value = getValue(NO_PASSWORD_REQUIRED).toString();
        return value;
    }

    public String getNotAtmAccount() {
        String value = getValue(NOT_AN_ATM_ACCOUNT).toString();
        return value;
    }

    public String getNotGlAccount() {
        String value = getValue(NOT_AN_GL_ACCOUNT).toString();
        return value;
    }

    public String getNotOnCard() {
        String value = getValue(NOT_ON_CARD).toString();
        return value;
    }

    public String getPasswordRequiredForAllTxn() {
        String value = getValue(PASS_REQD_FOR_ALL_TRANS).toString();
        return value;
    }

    public String getPasswordRequiredForCrTxn() {
        String value = getValue(PASS_REQD_FOR_CR_TRANS).toString();
        return value;
    }

    public String getPasswordRequiredForDrTxn() {
        String value = getValue(PASS_REQD_FOR_DR_TRANS).toString();
        return value;
    }

    public String getPasswordRequiredForEnq() {
        String value = getValue(PASSWORD_REQD_FOR_ENQUIRY).toString();
        return value;
    }

    public String getPasswordRequiredForPosting() {
        String value = getValue(PASSWORD_REQUIRED_FOR_POSTING).toString();
        return value;
    }

    public String getPosCrSuspenseAccount() {
        String value = getValue(POS_CR_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getPosDrSuspenseAccount() {
        String value = getValue(POS_DR_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getPosHoldingAccount() {
        String value = getValue(POS_HOLDING_ACCOUNTS).toString();
        return value;
    }

    public String getPossibleDuplicateTxnNarr() {
        String value = getValue(POSSIBLE_DUPLICATE_NARRATIVE).toString();
        return value;
    }

    public String getDateUsedForPosting() {
        String value = getValue(DATE_USED_FOR_POSTING).toString();
        return value;
    }

    public String getPosTxnType() {
        String value = getValue(DEFAULT_POS_TRANSACTION).toString();
        return value;
    }

    public String getPriority1() {
        String value = getValue(PRIORITY1).toString();
        return value;
    }

    public String getPriority2() {
        String value = getValue(PRIORITY2).toString();
        return value;
    }

    public String getPriority3() {
        String value = getValue(PRIORITY3).toString();
        return value;
    }

    public String getPriority4() {
        String value = getValue(PRIORITY4).toString();
        return value;
    }

    public String getPriority5() {
        String value = getValue(PRIORITY5).toString();
        return value;
    }

    public String getSettlementNarrative() {
        String value = getValue(SETTLEMENT_NARRATIVE).toString();
        return value;
    }

    public boolean isSharedSwitch() {
        Boolean value = new Boolean(getValue(SHARED_SWITCH).toString());
        return value.booleanValue();
    }

    public boolean isSolicitedMessageFlag() {
        Boolean value = new Boolean(getValue(SOLICITED_MESSAGE_FLAG).toString());
        return value.booleanValue();
    }

    public boolean getStatementFlag() {
        Boolean value = new Boolean(getValue(STATEMENT_FLAG).toString());
        return value.booleanValue();
    }

    public String getStatusForInvalidISOCode() {
        String value = getValue(INVALID_CURRENCY_CODE_STATUS).toString();
        return value;
    }

    public String getStopped() {
        String value = getValue(ACCOUNT_STOPPED).toString();
        return value;
    }

    public String getStoppedPwdReqForPosAndEnq() {
        String value = getValue(ACCOUNT_STOPPED_PASWD_REQD_FOR_POSTING_AND_ENQUIRY).toString();
        return value;
    }

    public String getSuspectRevTxnNarr() {
        String value = getValue(SUSPECT_REVERSAL_NARRATIVE).toString();
        return value;
    }

    public Integer getDestAccountLength() {
        Integer value = new Integer(getValue(DEST_ACCOUNT_LENGTH).toString());
        return value;
    }

    private Object getValue(String key) {
        String moduleName = "ATM";
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();// fix
                                                                                                          // for
                                                                                                          // bug
                                                                                                          // 24510
        Object value = bizInfo.getModuleConfigurationValue(moduleName, key, env);
        return value;
    }

    // Commission & Charges starts
    public String getCOMMISSION_CHARGE_CODE() {
        String value = getValue(CHARGECOMMISSIONCODE).toString();
        return value;

    }

    public String getFEES_CHARGE_CODE() {
        String value = getValue(CHARGEFEESCODE).toString();
        return value;
    }

    // Commission & Charges ends
    // External branch code changes start
    public String getEXTERNAL_BRANCH_CODE() {
        String value = getValue(EXTERNAL_BRANCH_CODE).toString();
        return value;
    }

    // External branch code changes ends

    // Changes started for Smart card
    public String getSmartCardSupported() {
        String value = getValue(SMART_CARD_SUPPORTED).toString();
        return value;
    }

    public String getSmartCardPursePoolAccount() {
        String value = getValue(SC_PURSE_POOL_ACCOUNT).toString();
        return value;
    }

    public String getSmartCardMerchantPoolAccount() {
        String value = getValue(SC_MERCHANT_POOL_ACCOUNT).toString();
        return value;
    }

    public String getSmartCardCreditSuspenseAccount() {
        String value = getValue(SC_CREDIT_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getSmartCardDebitSuspenseAccount() {
        String value = getValue(SC_DEBIT_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getSmartCardMerchantCreditSuspenseAccount() {
        String value = getValue(SC_MERCHANT_CREDIT_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getSmartCardMerchantDebitSuspenseAccount() {
        String value = getValue(SC_MERCHANT_DEBIT_SUSPENSE_ACCOUNT).toString();
        return value;
    }

    public String getSmartCardBlockingPeriod() {
        String value = getValue(SC_BLOCKING_PERIOD).toString();
        return value;
    }

    public String getSmartCardDefaultTransactionType() {
        String value = getValue(SC_DEFAULT_TRANSACTION_TYPE).toString();
        return value;
    }

    public String getProcessMagstripeTxns() {
        String value = getValue(PROCESS_MAGSTRIPE_TXNS).toString();
        return value;
    }

    public String getPosOutwardAccount() {
        String value = getValue(POS_OUTWARD_ACCOUNT).toString();
        return value;
    }

    // Changes ended for Smart Card.
    // Input tag added for ISO08583
    public String getValueDate() {
        String value = getValue(ATM_VALUE_DATE).toString();
        return value;
    }

    public String getCommissionReceivingAccount() {
        String value = getValue(ATM_COMMISION_RECEV_ACC).toString();
        return value;
    }

    public String getCommissionTransactionCode() {
        String value = getValue(ATM_COMMISION_TRNS_CODE).toString();
        return value;
    }

    public String getCommissionBranchCode() {
        String value = getValue(COMMISION_BRANCH).toString();
        return value;
    }

}
