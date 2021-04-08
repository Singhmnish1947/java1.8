/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 *
 * $Log: ATMConfigDetailsFatom.java,v $
 * Revision 1.5  2008/11/27 20:52:13  bhavyag
 * reverted changes of external branch code
 *
 * Revision 1.3  2008/10/22 04:03:29  bhavyag
 * reverted the changes for transaction codes.
 *
 * Revision 1.1  2008/10/16 11:16:26  bhavyag
 * updated files after adding two new charge codes.
 *
 * Revision 1.6  2008/08/12 20:13:49  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.4.4.3  2008/07/16 17:49:28  sushmax
 * Corrected the header
 *
 * Revision 1.4.4.2  2008/07/04 22:06:42  thrivikramj
 * Fix for the Bug 10921
 *
 * Revision 1.4.4.1  2008/07/03 17:55:35  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.6  2008/06/19 09:26:32  arun
 * FatomUtils' usage of getBankFusionException changed to call BankFusionException directly
 *
 * Revision 1.5  2008/06/16 15:21:34  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:51:53  arun
 *  RIO on Head
 *
 * Revision 1.4  2008/01/17 13:20:37  sushmax
 * Updated files
 *
 * Revision 1.4  2008/01/10 14:25:07  prashantk
 * Updations for Incorporating Module Config. Changes for ATM
 *
 * Revision 1.1  2008/01/09 12:47:58  varap
 * get ATM Config details
 *
 * Revision 1.3  2007/11/13 06:58:36  sushmax
 * ATMConfiguration -build 28 del
 *
 * Revision 1.2.4.1  2007/08/08 18:38:05  prashantk
 * Changes made to a few Attribute Types and Data Types to make it more Meaningful
 *
 * Revision 1.2  2007/05/16 08:28:48  sushmax
 * ATM Configuration Fatom - Code review changes done
 *
 */
package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMConfigDetailsFatom;

/**
 * The ATMConfigDetails returns through the output tags the ATM Configuration Details from
 * ATMConfig.xml
 */
public class ATMConfigDetailsFatom extends AbstractATMConfigDetailsFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
	 */

    /**
     * Holds the reference for logger object
     */
    private transient final static Log logger = LogFactory.getLog(ATMConfigDetailsFatom.class.getName());

    /**
     * The constructor that indicates we're in a runtime environment and we should initialise the
     * Fatom with only those attributes necessary.
     * 
     * @param env
     *            The BankFusion Environment
     */
    public ATMConfigDetailsFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * @see com.trapedza.bankfusion.core.ExtensionPoint#process(BankFusionEnvironment)
     * @param env
     *            The BankFusion Environment @
     */
    public void process(BankFusionEnvironment env) {

        try {
            ATMControlDetails controlDetails = ATMConfigCache.getInstance().getInformation(env);

            // ATM Control Details
            setF_OUT_HOTCARDSTATUS(controlDetails.getHotCardStatus());
            setF_OUT_INACTIVEACCOUNTSSTATUS(controlDetails.getInactiveAccount());
            setF_OUT_INVALIDCARDSTATUS(controlDetails.getNoCard());
            setF_OUT_NOCRTXNALLOWEDSTATUS(controlDetails.getNoCrTxnAllowed());
            setF_OUT_NODRTXNALLOWEDSTATUS(controlDetails.getNoDrTxnAllowed());
            setF_OUT_NOPASSWORDREQUIREDSTATUS(controlDetails.getNoPasswordRequired());
            setF_OUT_NOTATMACCOUNTSTATUS(controlDetails.getNotAtmAccount());
            setF_OUT_NOTGLACCOUNTSTATUS(controlDetails.getNotGlAccount());
            setF_OUT_NOTONCARDSTATUS(controlDetails.getNotOnCard());
            setF_OUT_PASSWORDREQUIREDFORALLTXNSTATUS(controlDetails.getPasswordRequiredForAllTxn());
            setF_OUT_PASSWORDREQUIREDFORCRTXNSTATUS(controlDetails.getPasswordRequiredForCrTxn());
            setF_OUT_PASSWORDREQUIREDFORDRTXNSTATUS(controlDetails.getPasswordRequiredForDrTxn());
            setF_OUT_PASSWORDREQUIREDFORENQSTATUS(controlDetails.getPasswordRequiredForEnq());
            setF_OUT_PASSWORDREQUIREDFORPOSTINGSTATUS(controlDetails.getPasswordRequiredForPosting());
            setF_OUT_INVALIDISOCODESTATUS(controlDetails.getStatusForInvalidISOCode());
            setF_OUT_ACCOUNTSTOPPEDSTATUS(controlDetails.getStopped());
            setF_OUT_STOPPEDPWDREQFORPOSANDENQSTATUS(controlDetails.getStoppedPwdReqForPosAndEnq());
            setF_OUT_ATMBASECURRENCY(controlDetails.getAtmBaseCurrency());
            setF_OUT_INTERBRANCHFLAG(Boolean.valueOf(controlDetails.isInterBranchFlag()));
            setF_OUT_STATEMENTFLAG(Boolean.valueOf(controlDetails.getStatementFlag()));
            setF_OUT_SHAREDSWITCHFLAG(Boolean.valueOf(controlDetails.isSharedSwitch()));
            setF_OUT_SOLICITEDMESSAGEFLAG(Boolean.valueOf(controlDetails.isSolicitedMessageFlag()));
            setF_OUT_ATMTRANSACTIONTYPE(controlDetails.getAtmTransactionType());
            setF_OUT_CORRECTIONTRANSNARR(controlDetails.getCorrectionTxnNarr());
            setF_OUT_POSSIBLEDUPLICATETRANSNARR(controlDetails.getPossibleDuplicateTxnNarr());
            setF_OUT_SETTLEMENTNARRATIVE(controlDetails.getSettlementNarrative());
            setF_OUT_SUSPECTREVTRANSANARR(controlDetails.getSuspectRevTxnNarr());
            setF_OUT_AUTHALLOWEDPERCENTAGE(controlDetails.getAuthAllowedPercentage());
            setF_OUT_DEFAULTBLOCKINGPERIOD(controlDetails.getDefaultBlockingPeriod());
            setF_OUT_BRANCHNUMBERLENGTH(controlDetails.getBranchNumberLength());
            setF_OUT_BALANCEDOWNLOADTYPE(controlDetails.getBalanceDownloadType());
            setF_OUT_POSTINGDATEFLAG(controlDetails.getDateUsedForPosting());
            setF_OUT_AVAILABLEBALANCEFLAG(controlDetails.getClearedOrBookBalance());
            // Input tag added for ISO08583
            setF_OUT_ATM_VALUE_DATE(controlDetails.getValueDate());
            setF_OUT_ATM_COMMISION_RECEV_ACC(controlDetails.getCommissionReceivingAccount());
            setF_OUT_ATM_COMMISION_TRNS_CODE(controlDetails.getCommissionTransactionCode());
            setF_OUT_COMMISION_BRANCH(controlDetails.getCommissionBranchCode());

            // POS configuration
            setF_OUT_POSHOLDINGACCOUNT(controlDetails.getPosHoldingAccount());
            setF_OUT_POSTRANSACTIONTYPE(controlDetails.getPosTxnType());

            // Credit/Debit suspense account
            setF_OUT_ATMCREDITSUSPENSEACCOUNTNUMBER(controlDetails.getAtmCrSuspenseAccount());
            setF_OUT_ATMDEBITSUSPENSEACCOUNTNUMBER(controlDetails.getAtmDrSuspenseAccount());
            setF_OUT_CARDHOLDERSUSPENSEACCOUNT(controlDetails.getCardHolderSuspenseAccount());
            setF_OUT_NETWORKCREDITSUSPENSEACCOUNTNUMBER(controlDetails.getNetworkCrSuspenseAccount());
            setF_OUT_NETWORKDEBITSUSPENSEACCOUNTNUMBER(controlDetails.getNetworkDrSuspenseAccount());
            setF_OUT_POSCREDITSUSPENSEACCOUNT(controlDetails.getPosCrSuspenseAccount());
            setF_OUT_POSDEBITSUSPENSEACCOUNT(controlDetails.getPosDrSuspenseAccount());

            // commission and fee
            setF_OUT_COMMISSIONCHARGECODE(controlDetails.getCOMMISSION_CHARGE_CODE());
            setF_OUT_FEESCHARGECODE(controlDetails.getFEES_CHARGE_CODE());

            // Priority Configuration
            setF_OUT_PRIORITY1(controlDetails.getPriority1());
            setF_OUT_PRIORITY2(controlDetails.getPriority2());
            setF_OUT_PRIORITY3(controlDetails.getPriority3());
            setF_OUT_PRIORITY4(controlDetails.getPriority4());
            setF_OUT_PRIORITY5(controlDetails.getPriority5());
            setF_OUT_DestAccountLength(controlDetails.getDestAccountLength());
            setF_OUT_EXTERNALBRANCHCODE(controlDetails.getEXTERNAL_BRANCH_CODE());
            // Changes started for Smart card.
            setF_OUT_SMART_CARD_SUPPORTED(controlDetails.getSmartCardSupported());
            setF_OUT_SC_PURSE_POOL_ACCOUNT(controlDetails.getSmartCardPursePoolAccount());
            setF_OUT_SC_MERCHANT_POOL_ACCOUNT(controlDetails.getSmartCardMerchantPoolAccount());
            setF_OUT_SC_CREDIT_SUSPENSE_ACCOUNT(controlDetails.getSmartCardCreditSuspenseAccount());
            setF_OUT_SC_DEBIT_SUSPENSE_ACCOUNT(controlDetails.getSmartCardDebitSuspenseAccount());
            setF_OUT_SC_MERCHANT_CREDIT_SUSPENSE_ACCOUNT(controlDetails.getSmartCardMerchantCreditSuspenseAccount());
            setF_OUT_SC_MERCHANT_DEBIT_SUSPENSE_ACCOUNT(controlDetails.getSmartCardMerchantDebitSuspenseAccount());
            setF_OUT_SC_BLOCKING_PERIOD(controlDetails.getSmartCardBlockingPeriod());
            setF_OUT_SC_DEFAULT_TRANSACTION_TYPE(controlDetails.getSmartCardDefaultTransactionType());
            setF_OUT_PROCESS_MAGSTRIPE_TXNS(controlDetails.getProcessMagstripeTxns());
            setF_OUT_POS_OUTWARD_ACCOUNT(controlDetails.getPosOutwardAccount());
            // Changes ended for Smart card.
        }
        catch (Exception e) {
            new BankFusionException(ChannelsEventCodes.E_ATM_CONFIGURATION_DETAILS_COULD_NOT_BE_RETRIEVED, new Object[] {}, logger,
                    env);
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }

    }
}
