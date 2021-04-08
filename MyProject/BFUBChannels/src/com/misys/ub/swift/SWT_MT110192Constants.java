package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class SWT_MT110192Constants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	//	general details
	public static String MESSAGENUMBER110 = "110";
	public static String MESSAGENUMBER192 = "192";
	public static String ROOT110 = "MT110template";
	public static String ROOT192 = "MTN92_template";
	public static String EMPTYSTRING = CommonConstants.EMPTY_STRING;
	public static String DELIMITER = "$";
	public static String A_FLAG = "A";
	public static String D_FLAG = "D";
	public static String B_FLAG = "B";
	public static String VOSTRO = "VOSTRO";
	public static String VOSTROCONSTANT = "/C/";
	public static String NONVOSTROCONSTANT = "/D/";

	//	MT110 specific details
	public static String MESSAGETYPE = "MessageType";
	public static String DISPOSALREF = "DisposalRef";
	public static String SENDER = "Sender";
	public static String RECEIVER = "Receiver";
	public static String SENDERS_REFERENCE_20 = "Senders_Reference_20";
	public static String SENDERS_CORRESPONDENT_53 = "Senders_Correspondent_53";
	public static String TAG_53 = "TAG_53";
	public static String RECEIVERS_CORRESPONDENT_54 = "Receivers_Correspondent_54";
	public static String TAG_54 = "TAG_54";
	public static String SENDER_TO_RECEIVER_INFORMATION_72 = "Sender_to_Receiver_Information_72";
	public static String CHEQUE_NUMBER_21 = "Cheque_Number_21";
	public static String DATE_OF_ISSUE_30 = "Date_of_issue_30";
	public static String AMOUNT_32B = "Amount_32B";
	public static String DRAWER_BANK_52 = "Drawer_Bank_52";
	public static String TAG_52 = "Tag_52";
	public static String PAYEE_59 = "Payee_59";
	// swift 2017
	public static String F_FLAG = "F";
	public static String K_FLAG = "K";
	//	MT192 specific details
	public static String TRANSACTIONREFERENCENUMBER_20 = "TransactionRefNumber_20";
	public static String RELATEDREFERENCE_21 = "RelatedReference_21";
	public static String NARRATIVE_79 = "Narrative_79";
}
