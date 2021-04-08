/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Trapedza Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 *
 ***********************************************************/
package com.misys.ub.interfaces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.fatoms.UB_IBI_PostInternalTransferTxnFatom;

/**
 * This class declares constants that can be used accross all IFM/IBI applications
 * @author Manu.Chadha
 *
 */
public class IfmConstants {
	/**
	 * Module name
	 */
	public static final String MODULE_NAME="IBI";
	/**
	 *  String constant for IFM reconciliation file separator key
	 */
	public static final String SEPARATOR_KEY ="SEPARATOR";
	/**
	 * String constant for IFM reconciliation transaction file path key
	 */
	
	public static final String TXN_FILE_PATH_KEY="TXNFILEPATH";
	/**
	 * String constant for IFM reconciliation transaction file name key
	 */
	
	public static final String TXN_FILE_NAME_KEY="TXNFILENAME";
	/**
	 * String constant for IFM reconciliation balance file name key
	 */
	
	public static final String BAL_FILE_NAME_KEY="BALFILENAME";
	/**
	 * String constant for IFM reconciliation balance file path key
	 */
	
	public static final String BAL_FILE_PATH_KEY="ACCBALFILEPATH";
	/**
	 * String constant for number of records key  for IFM reconciliation transaction file 
	 */
	public static final String NUM_OF_REC_TXN_FILE_KEY="NUMOFRECFORACCTXNFILE";
	/**
	 * String constant for number of records key  for IFM reconciliation balance file 
	 */
	
	public static final String NUM_OF_REC_BAL_FILE_KEY="NUMOFRECFORACCBALFILE";
	
	
	public static final  String POSTING_MICROFLOW = "UB_CNF_FinancialPosting_SRV";
	
	public static final  String GET_AVAILABLE_BALANCE="GetAvailableBalance";
	
	public static final String CreditAccountNumber=CommonConstants.EMPTY_STRING;
	
	public static final  String ACCOUNTID="ACCOUNTID";
	
	public static final  String ACCOUNTTYPE="ACCOUNTTYPE";
	
	public static final  String ACTION_POSTING="Posting";
	
	public static final  String ACTION_PAYMENTSERVICE="PS";
	
	public static final  String CR="CR";
	
	public static final  String DR="DR";
	
	public static final  String NAKNOFUNDS ="NAKNOFUNDS";
	
	public static final  String NAKNORATES="NAKNORATES";
	
	public static final  String NAKINTERNALERR="NAKINTERNALERR";
	
	public static final  String NAKDRACCTINV="NAKDRACCTINV";
	
	public static final  String NAKCRACCTINV="NAKCRACCTINV";
	
	public  static final String ACCOUNTVALIDATIONMICROFLOW="UB_IBI_AccountValidation_SRV";

	public static final String SYS_MODULE_NAME="SYS";

	//parameters for financial posting
	public static final String DEBITTRANSACTIONCODE="DebitTransCode";
	
	public static final  String SETTLEMENTACCOUNTID="SettlementAccountId";
	
	public static final  String DEBITTRANSACTIONAMOUNT="DebitTransactionAmount";
	
	public static final  String CREDITTRANSACTIONAMOUNT="CreditTransactionAmount";
	
	public static final  String DEBITTXNCURRENCYCODE="DebitTransactionCurrency";
	
	public static final  String CREDITTXNCURRENCYCODE="CreditTransactionCurrency";
	
	public static final  String CREDITPOSTINGACTION="creditPostingAction";
	
	public static final  String DEBITPOSTINGACTION="debitPostingAction";
	
	public static final  String CREDITTRANSACTIONCODE="creditTransCode";
	
	public static final  String ISOCURRENCYCODE="isocurrencycode";
	
	public static final  String MAINACCOUNTID="mainAccountId";
	
	public static final  String DEBITTRANSACTIONNARRATIVE="DebitTransactionNarrative";
	
	public static final  String CREDITTRANSACTIONNARRATIVE="CreditTransactionNarrative";
	
	public static final  String EXCHANGERATETYPE="ExchangeRateType";
	
	public static final  String E_INSUFFICIENT_FUND="40205037";
	
	public static final  String E_AN_INVALID_EXCHANGE_RATE_WAS_RETURNED="40507051";
	
	public static final  String E_FINANCIAL_POSTING_ERROR="40000408";
	
	public static final  String E_INVALID_ACCOUNT="40407516";
	
	public static final  String MODULE_KYC = "KYC";
	
	public static final  String IBI_PAYMENTS_PARAM = "IBI_PAYMENTS";
	
	public static final  String FBCC_STO_PARAM = "IS_KYC_REQD_FOR_CC_SO";	
	
	public static final  String LOAN_REPYMENT_RQ = "backOfficeAccountPostingRq";
	
	public static final  String LOAN_POSTING_ENGINE = "UB_R_UB_TXN_BackOfficeAccountPosting_SRV";
	
	public static final  String CHARGEPOSTINGTXNCODE="CHARGEPOSTINGTXNCODE";
	public static final  String TAXPOSTINGTXNCODE="TAXCODE";
	public static final  String CHARGEEXCHANGERATETYPE="CHARGEEXCHANGERATETYPE";
	public static final  String CHARGERECIEVINGACCOUNT="CHARGERECIEVINGACCOUNT";
	public static final  String CHARGEAMOUNT="CHARGEAMOUNT";
	public static final  String DEBIT="D";
	public static final  String CREDIT="C";
	public static final  String CHARGENARRATIVE="CHARGENARRATIVE";
	public static final  String TAXEXCHANGERATETYPE="TAXEXCHANGERATETYPE";
	public static final  String TAXRECIEVINGACCOUNT="TAXRECIEVINGACCOUNT";
	public static final  String TAXAMOUNT="TAXAMOUNT";
	public static final  String TAXCURRENCY = "TAXCURRENCY";
	public static final  String TAXNARRATIVE="TAXNARRATIVE";
	public static final  String AVAILABLE_BALANCE_KEY="AvailableBalance";
	public static final  String POSTING_ERROR="Error Occured While Posting";
	public static final  String NAKVALUEDATEINV="NAKVALUEDATEINV";
	public static final   String domesticSettlementPseudoName="DOMESTICSETTLEMENTACC";
	public static final   String nostroPseudoName="FOREIGNNOSTROACC";
	public static final  String CURRENCY_CONTEXT="CURRENCY";
	public static final  String BRANCH_CONTEXT="BRANCH";
	public static final  String SUSPENSEACCOUNT="IBICRSUSPACC";
	public static final  String SETTLEMENTACCT_CONTEXT_KEY="SETTLEMENTACCT_CONTEXT";
	public static final  String DOMESTIC_SETTLEMENT_ACC="DOM_PAY_SETTLEMENT_ACC";
	public static final  String DEBITEXCHANGERATE="DebitExchangeRate";
	public static final  String CREDITEXCHANGERATE="CreditExchangeRate";
	public static final  String CHANNELID="ChannelId";
	public static final  String IFMCHANNEL="IBI";
	public static final  String CHARGECURRENCY="CHARGECURRENCY";
	public static final String CHARGEAMOUNT_IN_FUND_ACC_CURRENCY = "CHARGEAMOUNT_IN_FUND_ACC_CURRENCY";
	public static final String SYS_MODULE_CONFIG_KEY = "SYS";
	public static final String SYS_POSITION_CONTEXT = "CONTEXT_POSITIONACCOUNT";
	public static final String TRANSACTION_REFERENCE="TxnRef";
	public static final String TRANSACTION_ID = "TransactionID";
    public static final String HAS_LEN_FEATURE ="HASLENDINGFTR";
    public static final String WHAT_PRODUCT_DEFAULTS ="WhatProductDefaults";
    public static final String IS_FWD_VALUE_TXN ="ISFORWARDDATEDTXN";
    public static final String FWD_DATED_INTO_VALUE ="FORWARDDATEDINTOVALUE";
    public static final String IS_EOD_IN_PROGRESS_SRV ="UB_IBI_IsEODInProgress_SRV";
    public static final String IS_INTERNET_BANKING_ACCT_SRV ="UB_IBI_IsInternetBankingAccount_SRV";
    public static final String UPDATE_BALANCE_CHANGED_SRV ="UB_IBI_UpdateBalanceChanged_SRV";
    public static final String UPDATE_BASECODERATE_CHANGED_SRV ="UB_IBI_UpdateBaseCodeRateChanged_SRV";
    public static final String PAYMENT_IDENTIFIER_CONTEXT = "PAYMENTIDENTIFIER";
    public static final String MODULE_FEX = "FEX";
    public static final String TAXCODE = "TAXCODE";
    public static final String TAXAMOUNT_IN_FUND_ACC_CURRENCY = "TAXAMOUNT_IN_FUND_ACC_CURRENCY";
}
