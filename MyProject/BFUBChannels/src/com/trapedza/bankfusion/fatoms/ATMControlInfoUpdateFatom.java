
/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMControlInfoUpdateFatom.java,v $
 * Revision 1.6  2008/11/27 20:52:13  bhavyag
 * reverted changes of external branch code
 *
 * Revision 1.4  2008/10/22 04:03:30  bhavyag
 * reverted the changes for transaction codes.
 *
 * Revision 1.2  2008/10/17 01:25:48  bhavyag
 * updated file.
 *
 * Revision 1.3  2008/08/12 20:14:05  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.1.4.4  2008/07/16 17:49:37  sushmax
 * Corrected the header
 *
 * Revision 1.1.4.3  2008/07/16 16:13:01  varap
 * Code cleanup - CVS revision tag added.
 *
 */

package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.misys.cbs.config.ModuleConfiguration;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CMN_ModuleConfiguration;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMControlInfoUpdateFatom;

public class ATMControlInfoUpdateFatom extends AbstractATMControlInfoUpdateFatom {

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
	
	  private transient ModuleConfiguration moduleConfiguration;

	private static final String MODULUE_CONFIG_QUERRY = "where " + IBOCB_CMN_ModuleConfiguration.MODULENAME + " = ?";

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
	//Account Status
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
	//POS configuration
	private String POS_HOLDING_ACCOUNTS = "POS_HOLDING_ACCOUNTS";
	private String DEFAULT_POS_TRANSACTION = "DEFAULT_POS_TRANSACTION";

	//Credit/Debit suspense account
	private String ATM_CR_SUSPENSE_ACCOUNT = "ATM_CR_SUSPENSE_ACCOUNT";
	private String ATM_DR_SUSPENSE_ACCOUNT = "ATM_DR_SUSPENSE_ACCOUNT";
	private String NETWORK_DR_SUSPENSE_ACCOUNT = "NETWORK_DR_SUSPENSE_ACCOUNT";
	private String NETWORK_CR_SUSPENSE_ACCOUNT = "NETWORK_CR_SUSPENSE_ACCOUNT";
	private String CARD_HOLDERS_SUSPENSE_ACCOUNT = "CARD_HOLDERS_SUSPENSE_ACCOUNT";
	private String POS_DR_SUSPENSE_ACCOUNT = "POS_DR_SUSPENSE_ACCOUNT";
	private String POS_CR_SUSPENSE_ACCOUNT = "POS_CR_SUSPENSE_ACCOUNT";

	//Priority Configuration
	private String PRIORITY1 = "PRIORITY1";
	private String PRIORITY2 = "PRIORITY2";
	private String PRIORITY3 = "PRIORITY3";
	private String PRIORITY4 = "PRIORITY4";
	private String PRIORITY5 = "PRIORITY5";

	private String DEST_ACCOUNT_LENGTH="DEST_ACCOUNT_LENGTH";
	private HashMap hashMap;
	
	//Commissions & charges
	private String CHARGECOMMISSIONCODE = "CHARGECOMMISSIONCODE";
	private String CHARGEFEESCODE = "CHARGEFEESCODE";
	private String EXTERNAL_BRANCH_CODE = "EXTERNAL_BRANCH_CODE";
	
	//New fields for SmartCard
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
	//Input tag added for ISO08583
	private String ATM_VALUE_DATE = "ATM_VALUE_DATE";
	private String ATM_COMMISION_RECEV_ACC = "ATM_COMMISION_RECEV_ACC";
	private String ATM_COMMISION_TRNS_CODE = "ATM_COMMISION_TRNS_CODE";
	private String COMMISION_BRANCH = "COMMISION_BRANCH";
	

	public ATMControlInfoUpdateFatom(BankFusionEnvironment env) {
		super(env);
	}

	private void getConfigInfo(BankFusionEnvironment env) {
		hashMap = new HashMap();
		ArrayList params = new ArrayList();
		params.add("ATM");
		List atmConfigDetail = new ArrayList();
		try {
			getModuleConfiguration().cleancache("ATM");
			atmConfigDetail = env.getFactory().findByQuery(IBOCB_CMN_ModuleConfiguration.BONAME, MODULUE_CONFIG_QUERRY,
					params, null);
		}
		catch (BankFusionException exception) {
			exception.getCause();
		}
		Iterator iterator = atmConfigDetail.iterator();
		while (iterator.hasNext()) {
			IBOCB_CMN_ModuleConfiguration object = (IBOCB_CMN_ModuleConfiguration) iterator.next();
			hashMap.put(object.getF_PARAMNAME(), object);
			
		}
	}

	private void updateKey(String key, String value) {
		getModuleConfiguration().cleancache("ATM");
		IBOCB_CMN_ModuleConfiguration moduleConfiguration = (IBOCB_CMN_ModuleConfiguration) hashMap.get(key);
		moduleConfiguration.setF_PARAMVALUE(value);
	}

	public void process(BankFusionEnvironment env) {
		getConfigInfo(env);
		//Update for all Keys.

		updateKey(ACCOUNT_STOPPED, getF_IN_AccountStopped());
		updateKey(ATM_BASE_CURRENCY, getF_IN_ATMBaseCurrencyCode());
		updateKey(ATM_CR_SUSPENSE_ACCOUNT, getF_IN_ATMCrSuspenseAccount());
		updateKey(ATM_DR_SUSPENSE_ACCOUNT, getF_IN_ATMDrSuspenseAccount());
		updateKey(BALANCE_USED_FOR_AVAILABE_BALANCE, getF_IN_AvailableBalanceFlag());
		updateKey(BALANCE_DOWNLOAD_TYPE, getF_IN_BalanceDownloadType());
		updateKey(CARD_HOLDERS_SUSPENSE_ACCOUNT, getF_IN_CardHoldersSuspenseAccount());
		updateKey(CORRECRION_NARRATIVE, getF_IN_CorrectionTxnNarrative());
		updateKey(DEFAULT_ATM_TRANSACTION, getF_IN_DefaultATMTransactionType());
		updateKey(DEFAULT_POS_TRANSACTION, getF_IN_DefaultPOSTransactionCode());
		updateKey(HOT_CARD_STATUS, getF_IN_HotCardStatus());
		updateKey(NOT_AN_GL_ACCOUNT, getF_IN_InvalidAccountStatus());
		updateKey(INVALID_CARD, getF_IN_InvalidCardStatus());
		updateKey(INVALID_CURRENCY_CODE_STATUS, getF_IN_InvalidISOCode());
		updateKey(NETWORK_CR_SUSPENSE_ACCOUNT, getF_IN_NetworkCrSuspenseAccount());
		updateKey(NETWORK_DR_SUSPENSE_ACCOUNT, getF_IN_NetworkDrSuspenseAccount());
		updateKey(NO_CR_TRANSACTIONS_ALLOWED, getF_IN_NoCrTxnsAllowed());
		updateKey(NO_DR_TRANSACTIONS_ALLOWED, getF_IN_NoDrTxnxAllowed());
		updateKey(NO_PASSWORD_REQUIRED, getF_IN_NoPasswordRequired());
		updateKey(NOT_AN_ATM_ACCOUNT, getF_IN_NotAnATMAccountStatus());
		updateKey(NOT_AN_GL_ACCOUNT, getF_IN_NotAnGLAccount());
		updateKey(NOT_ON_CARD, getF_IN_NotOnCard());
		updateKey(PASS_REQD_FOR_ALL_TRANS, getF_IN_PasswordReqdForAllTxn());
		updateKey(PASS_REQD_FOR_CR_TRANS, getF_IN_PasswordReqdForCRTxns());
		updateKey(PASS_REQD_FOR_DR_TRANS, getF_IN_PasswordReqdForDRTxns());
		updateKey(PASSWORD_REQD_FOR_ENQUIRY, getF_IN_PasswordReqdForEnquiry());
		updateKey(PASSWORD_REQUIRED_FOR_POSTING, getF_IN_PasswordReqdForPosting());
		updateKey(POS_CR_SUSPENSE_ACCOUNT, getF_IN_POSCrSuspenseAccount());
		updateKey(POS_DR_SUSPENSE_ACCOUNT, getF_IN_POSDrSuspenseAccount());
		updateKey(POS_HOLDING_ACCOUNTS, getF_IN_POSHoldingAccount());
		updateKey(POSSIBLE_DUPLICATE_NARRATIVE, getF_IN_PossibleDuplicateTxnNarrative());
		updateKey(DATE_USED_FOR_POSTING, getF_IN_PostingDateFlag());
		updateKey(PRIORITY1, getF_IN_Priority1());
		updateKey(PRIORITY2, getF_IN_Priority2());
		updateKey(PRIORITY3, getF_IN_Priority3());
		updateKey(PRIORITY4, getF_IN_Priority4());
		updateKey(PRIORITY5, getF_IN_Priority5());
		updateKey(SETTLEMENT_NARRATIVE, getF_IN_SettlementNarrative());
		updateKey(ACCOUNT_STOPPED_PASWD_REQD_FOR_POSTING_AND_ENQUIRY, getF_IN_StoppedPaswdReqdforPoastingandEnquiry());
		updateKey(SUSPECT_REVERSAL_NARRATIVE, getF_IN_SuspectReversalNarrative());
		updateKey(INTERBRANCH_FLAG, isF_IN_isInterBrachEnabled().toString());
		updateKey(SHARED_SWITCH, isF_IN_isSharedSwitchEnabled().toString());
		updateKey(SOLICITED_MESSAGE_FLAG, isF_IN_isSolicitedDownload().toString());
		updateKey(STATEMENT_FLAG, isF_IN_isAreAllTransactionsOnStatement().toString());
		updateKey(AUTH_ALLOWEED_PERCENTAGE, getF_IN_AuthAllowedPercentage().toString());
		updateKey(BRANCH_NUMBER_LENGTH, getF_IN_BranchNumberLength().toString());
		updateKey(DEFAULT_BLOCKING_PERIOD, getF_IN_DefaultBlockingPeriod().toString());
		updateKey(PASS_REQD_FOR_ALL_TRANS, getF_IN_PwdReqdForAllTxns());
		updateKey(INACTIVE_ACCOUNT, getF_IN_InactiveAccount());
		updateKey(DEST_ACCOUNT_LENGTH,getF_IN_DestAccountLength().toString());
		updateKey(CHARGECOMMISSIONCODE,getF_IN_COMMISSIONCHARGECODE());
		updateKey(CHARGEFEESCODE,getF_IN_FEESCHARGECODE());
		updateKey(EXTERNAL_BRANCH_CODE,getF_IN_ExternalBranchCode());
		////Input tag added for ISO08583
		updateKey(ATM_VALUE_DATE,getF_IN_ATM_VALUE_DATE());
		updateKey(ATM_COMMISION_RECEV_ACC,getF_IN_ATM_COMMISION_RECEV_ACC());
		updateKey(ATM_COMMISION_TRNS_CODE,getF_IN_ATM_COMMISION_TRNS_CODE());
		updateKey(COMMISION_BRANCH, getF_IN_COMMISION_BRANCH());
		//Changes started for Smart card.
		updateKey(SMART_CARD_SUPPORTED,getF_IN_SmartCardTransactionSupported());
		updateKey(SC_PURSE_POOL_ACCOUNT,getF_IN_SmartCardPursePoolAccount());
		updateKey(SC_MERCHANT_POOL_ACCOUNT,getF_IN_SmartCardMerchantPoolAccount());
		updateKey(SC_CREDIT_SUSPENSE_ACCOUNT,getF_IN_SmartCardCreditSuspenseAccount());
		updateKey(SC_DEBIT_SUSPENSE_ACCOUNT,getF_IN_SmartCardDebitSuspenseAccount());
		updateKey(SC_MERCHANT_CREDIT_SUSPENSE_ACCOUNT,getF_IN_SmartCardMerchantCreditSuspenseAccount());
		updateKey(SC_MERCHANT_DEBIT_SUSPENSE_ACCOUNT,getF_IN_SmartCardMerchantDebitSuspenseAccount());
		updateKey(SC_BLOCKING_PERIOD,getF_IN_SmartCardBlockingPeriod());
		updateKey(SC_DEFAULT_TRANSACTION_TYPE, getF_IN_SmartCardDefaultTransactionType());
		updateKey(PROCESS_MAGSTRIPE_TXNS, getF_IN_ProcessMagstripeTransactions());
		updateKey(POS_OUTWARD_ACCOUNT,getF_IN_POSOutwardAccount());
		updateKey(AUTH_ALLOWEED_PERCENTAGE,getF_IN_AuthAllowedPercentage().toString());
		//Changes ended for Smart Card.
		
	}
	
	  ModuleConfiguration getModuleConfiguration() {
	        if (this.moduleConfiguration == null) {
	            this.moduleConfiguration = ModuleConfiguration.getInstance();
	        }

	        return this.moduleConfiguration;
	    }
}
