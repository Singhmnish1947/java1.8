/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * Revision 1.11  2007/07/05 07:58:30  sushmax
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.helper;

/**
 * The ATMConstants class  
 */
public class ATMConstants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */

	/*
	 * This is a Private cunstructor to avoid making instances of This class.
	 */
	private ATMConstants() {
		super();
	}

	/** <code>FORCEPOST_0</code> = 0. */
	public static final String FORCEPOST_0 = "0";

	/** <code>FORCEPOST_1</code> = 1. */
	public static final String FORCEPOST_1 = "1";

	/** <code>FORCEPOST_2</code> = 2. */
	public static final String FORCEPOST_2 = "2";

	/** <code>FORCEPOST_3</code> = 3. */
	public static final String FORCEPOST_3 = "3";

	/** <code>FORCEPOST_6</code> = 6. */
	public static final String FORCEPOST_6 = "6";

	/** <code>FORCEPOST_7</code> = 7. */
	public static final String FORCEPOST_7 = "7";

	public static final String FORCEPOST_5 = "5";

	public static final String FORCEPOST_8 = "8";

	/** <code>AUTHORIZED_MESSAGE_FLAG</code> = 0. */
	public static final String AUTHORIZED_MESSAGE_FLAG = "0";

	/** <code>NOTAUTHORIZED_MESSAGE_FLAG</code> = 1. */
	public static final String NOTAUTHORIZED_MESSAGE_FLAG = "1";

	/** <code>ACTIVITY_LOG_UPDATE_MICROFLOW_NAME</code> = "ATM_SPA_UpdateATMActivityLog" */
	public static final String BATCH_FAILED_REPORT = "UB_ATM_SPA_FailedBatchReport";
	
	/** <code>ACTIVITY_LOG_UPDATE_MICROFLOW_NAME</code> = "ATM_SPA_UpdateATMActivityLog" */
	public static final String ACTIVITY_LOG_UPDATE_MICROFLOW_NAME = "ATM_SPA_UpdateATMActivityLog";

	/** <code>FINANCIAL_POSTING_MICROFLOW_NAME</code> = "ATM_SPA_FinancialPostingEngine" */
	public static final String FINANCIAL_POSTING_MICROFLOW_NAME = "ATM_SPA_FinancialPostingEngine";

	/** <code>NOTIFICATION_TRANSACTION_FLAG</code> = 2. */
	public static final int NOTIFICATION_TRANSACTION_FLAG = 2;

	/** <code>SUPPORTED_TRANSACTION_FLAG</code> = 1. */
	public static final int SUPPORTED_TRANSACTION_FLAG = 1;

	/** <code>NOTSUPPORTED_TRANSACTION_FLAG</code> = 0. */
	public static final int NOTSUPPORTED_TRANSACTION_FLAG = 0;

	/** <code>ATM_TIMESTAMP_PATTERN</code>  = "yyMMddhhmmss" */
	public static final String ATM_TIMESTAMP_PATTERN = "yyMMddhhmmss";

	/** <code>MSG_TYPE_ATM_TRANSACTIONS</code> = 5*/
	public static final String MSG_TYPE_ATM_TRANSACTIONS = "5";

	/** <code>MSG_TYPE_ATM_TRANSACTIONS_REV</code> = 0*/
	public static final String MSG_TYPE_ATM_TRANSACTIONS_REV = "0";

	/** <code>MSG_TYPE_ATM_TRANSACTIONS_CORRECT</code> = 8*/
	public static final String MSG_TYPE_ATM_TRANSACTIONS_CORRECT = "8";

	/** <code>MSG_TYPE_EXNWORPOS_TRANSACTIONS</code>  = 6*/
	public static final String MSG_TYPE_EXNWORPOS_TRANSACTIONS = "6";

	/** <code>MSG_TYPE_EXNWORPOS_TRANSACTIONS_REV</code> = 7*/
	public static final String MSG_TYPE_EXNWORPOS_TRANSACTIONS_REV = "7";

	public static final String ERROR = "Error";

	public static final String INFORMATION = "Information";

	public static final String WARNING = "Warning";

	public static final String CRITICAL = "Critical";

	public static final String BLOCKING_TRANSACTION = "ATM_SPA_TellerBlocking";

	public static final String LOCAL_BALANCE_INQUIRY = "530";

	public static final String EXTERNAL_BALANCE_INQUIRY = "631";
	
	public static final String LOCAL_LORO = "599";

	public static final String LOCAL_MESSAGE_TYPE = "5";

	public static final String EXTERNAL_MESSAGE_TYPE = "6";
	
	//new constants for ATMSparrow [start]
	public static final String MAIN = "Main";
	
	public static final String ALTERNATE = "Alternate";
	
	public static final String SPARROW_SORT_CONTEXT = "ALTERNATE";
	
	public static final String SPARROW_SORT_CONTEXT_VALUE = "SPARROWACCOUNT";
	
	public static final String NORMAL = "001";
	
	public static final String WATCH_LIST = "005";
	
	public static final String GETALTERNATEACCOUNT_MICROFLOW_NAME = "UB_ATM_GetAlternateAccount_BalancedDownload_SRV";
	
	public static final String SOURCEACCOUNTTYPE = "S";
	
	public static final String DESTACCOUNTTYPE = "D";
	
	public static final String NARRATIVEGENERATOR = "NarrativeGenerator";
	
	public static final String FIXEDNARRATIVE = "FixedNarrative";
	
	public static final String CUSTOMERNARRATIVE = "CustomerNarrative";
													
	public static final String CONTRANARRATIVE = "ContraNarrative";
	
	public static final String MICROFLOWID = "MICROFLOW_ID";
	
	public static final String AUTHORISEDFLAG = "AuthorisedFlag";
	public static final String CARDDESTBRANCHCODE = "CardDestBranchCode";
	public static final String CARDDESTCOUNTRYCODE = "CardDestCountryCode";
	public static final String CARDDESTINATIONIMD = "CardDestinationIMD";
	public static final String CARDNUMBER = "CardNumber";
	public static final String CARDSEQUENCENO = "CardSequenceNo";
	public static final String DATETIMEOFTXN = "DateTimeofTxn";
	public static final String DESTINATIONMAILBOX = "DestinationMailBox";
	public static final String DEVICEID = "DeviceId";
	public static final String FORCEPOST = "ForcePost";
	public static final String MESSAGETYPE = "MessageType";
	public static final String SOURCEBRANCHCODE = "SourceBranchCode";
	public static final String SOURCECOUNTRYCODE = "SourceCountryCode";
	public static final String SOURCEIMD = "SourceIMD";
	public static final String SOURCEMAILBOX = "SourceMailBox";
	public static final String TRANSACTIONTYPE = "TransactionType";
	public static final String TXNDESCRIPTION = "TxnDescription";
	public static final String TXNSEQUENCENO = "TxnSequenceNo";
	 
	public static final String ACCOUNT = "Account";
	public static final String ACTIONCODE = "ActionCode";
	public static final String AMOUNT1 = "Amount1";
	public static final String AMOUNT2 = "Amount2";
	public static final String AMOUNT3 = "Amount3";
	public static final String AMOUNT4 = "Amount4";
	public static final String CURRENCYDESTDISPENSED = "CurrencyDestDispensed";
	public static final String CURRENCYSOURCEACCOUNT = "CurrencySourceAccount";
	public static final String DESCDESTACC = "DescDestAcc";
	public static final String DESTACCOUNTNUMBER = "DestAccountNumber";
	public static final String DESCSOURCEACC = "DescSourceAcc";
	public static final String LOCALCURRENCYCODE = "LocalCurrencyCode";
	public static final String LOROMAILBOX = "LoroMailbox";
	public static final String SUBINDEX = "SubIndex";
	public static final String VARIABLEDATATYPE = "VariableDataType";

	public static final String LOCALACTUALBALANCE = "LocalActualBalance";
	public static final String LOCALAVAILABLEBALANCE = "LocalAvailableBalance";
	public static final String BRANCHNAME = "BranchName";
	public static final String LOCALEXTENSIONVERSION = "LocalExtensionVersion";

	public static final String POSAUTHORISATIONCODE = "POSAuthorisationCode";
	public static final String POSCASHBACKAMOUNT = "POSCashBackAmount";
	public static final String EXTENSIONVERSION = "ExtensionVersion";
	public static final String EXTERNALTERMINALID = "ExternalTerminalID";
	public static final String POSMERCHANTCATEGORYCODE = "POSMerchantCategoryCode";
	public static final String MERCHANTID = "MerchantID";
	public static final String MERCHANTLOCATION = "MerchantLocation";
	public static final String MERCHANTNAME = "MerchantName";
	public static final String SETTLEMENTIDENTIFIER = "SettlementIdentifier";

	public static final String ACQUIRINGINSTITUTIONID = "AcquiringInstitutionID";
	public static final String ACTUALBALANCE = "ActualBalance";
	public static final String AUTHORISATIONCODE = "AuthorisationCode";
	public static final String AVAILABLEBALANCE = "AvailableBalance";
	public static final String CARDACCEPTORID = "CardAcceptorID";
	public static final String CARDACCEPTORNAME = "CardAcceptorName";
	public static final String CARDACCEPTORTERMINALID = "CardAcceptorTerminalID";
	public static final String CASHBACKACCOUNT = "CashBackAccount";
	public static final String CASHBACKAMOUNT = "CashBackAmount";
	public static final String CASHBACKDEVICE = "CashBackDevice";
	public static final String CONVERSIONRATE = "ConversionRate";
	public static final String CURRENCYCODE = "CurrencyCode";
	public static final String EXTENSIONNUMBER = "ExtensionNumber";
	public static final String EXTERNALNETWORKID = "ExternalNetworkID";
	public static final String FORWARDINGINSTITUTIONID = "ForwardingInstitutionID";
	public static final String MERCHANTCATEGORYCODE = "MerchantCategoryCode";
	public static final String SETTLEMENTAMOUNT = "SettlementAmount";
	public static final String SETTLEMENTCONVRATE = "SettlementConvRate";
	public static final String SETTLEMENTCURRENCY = "SettlementCurrency";
	public static final String TRANSACTIONAMOUNT = "TransactionAmount";
	public static final String LOGOFF_LOGON_COMMON = "ISO006000050080082200000000000000400000000000000";
	
	public static final String LOGOFF = "LOGOFF";
	
	public static final String LOGON = "LOGON";
	
	public static final String ECHO_TEST = "ECHO";
	
	public static final String MODULENAME="ATM";
	
	public static final String REGISTRATION_MSG="REGISTRATION_MSGTYPE";
	public static final String DEREGISTRATION_MSG="DEREGISTRATION_MSGTYPE";
	
	//new constant for ATMSparrow [end]
	
	//Constant for Event Based Charges - Start
	
	public static final String APPLY_EVENT_CHARGES_MICROFLOW_NAME = "CB_CHG_ApplyEventCharges_SRV";
	public static final String EVENT_ID_VAL = "40180217";
	public static final String MINI_STATE_EVENT_ID_VAL = "40180460";
	public static final String CALC_EVENT_CHARGE_REQ = "CalcEventChargeRq";
	
	//Constant for Event Based Charges - End
	
}
