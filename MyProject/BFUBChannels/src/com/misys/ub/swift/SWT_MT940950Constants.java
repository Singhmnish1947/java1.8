package com.misys.ub.swift;

public class SWT_MT940950Constants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public static String MESSAGENUMBER940 = "940";
	public static String MESSAGENUMBER950 = "950";
	public static String ROOT940 = "MT940template";
	public static String ROOT950 = "MT950template";
	public static String MESSAGETYPE = "MessageType";
	public static String SENDER = "Sender";
	public static String RECEIVER = "Receiver";
	public static String TRANSACTIONREFERENCENUMBER_20 = "TransactionReferenceNumber_20";
	public static String ACCOUNTIDENTIFICATION_25 = "AccountIdentification_25";
	public static String STATEMENTNUMBER_28C = "StatementNumber_28C";
	public static String OPENINGBALANCE_60 = "OpeningBalance_60";
	public static String TAG_60 = "TAG_60";
	public static String STATEMENTLINE_61 = "StatementLine_61";
	public static String INFORMATIONTOACCOUNTOWNER_86 = "InformationToAccountOwner_86";
	public static String CLOSINGBALANCE_62 = "ClosingBalance_62";
	public static String TAG_62 = "TAG_62";
	public static String CLOSINGAVAILABLEBALANCE_64 = "ClosingAvailableBalance_64";
	public static int MESSAGESIZE = 1536; //Maximum size of a 940/950 message
	public static int MINSIZETOPROCEED = 60; //Length of mandatory fields length
	public static String DEBITMARK = "D";
	public static String CREDITMARK = "C";
	public static String TRANDETAILS = "Transaction_Details";
	public static String DEBITREVERSALMARK = "RD";
	public static String CREDITREVERSALMARK = "RC";
	public static int REVERSALTRANSACTION = 1;
	public static int NONREVERSALTRANSACTION = 0;
	public static int ORGINALREVERSEDTRANSACTION=2;
	public static String DEBITFLAG = "D";
	public static String CREDITFLAG = "C";
	public static String TERMINALBALANCEMARK = "F";
	public static String INTERMEDIATEBALANCEMARK = "M";
}
