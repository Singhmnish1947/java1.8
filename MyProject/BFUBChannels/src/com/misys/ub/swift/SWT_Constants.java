/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: SWT_Constants.java,v 1.4 2008/11/24 11:21:07 sukirtim Exp $
 *
 */
package com.misys.ub.swift;

/**
 * @author Vipesh
 * 
 */
public class SWT_Constants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public static final String delimiter = "$";

	public static final String MESSAGEHEADER_PREPEND = "UB_MT";
	public static final String MESSAGETYPE = "MessageType";

	public static final String SENDER = "Sender";

	public static final String RECEIVER = "Receiver";
	public static final String TRANSACTIONREF = "TransactionReferenceNumber_20";
	public static final String RELATEDREF = "RelatedReference_21";
	public static final String SEQUENCEA = "SequenceA";

	public static final String SEQUENCEB = "SequenceB";

	public static final String SEQUENCEC = "SequenceC";

	public static final String DISPOSALREF = "DisposalRef";

	public static final String TRANSREFNO20 = "TransRefNo_20";

	public static final String SENDERSREF20 = "Senders_Ref_20";

	public static final String RELATEDREF21 = "RelatedRef_21";

	public static final String TYPEOPERATION22A = "TypeOperation_22A";

	public static final String COMMONREF22C = "CommonRef_22C";

	public static final String BANKOPCODE23B = "BankOpCode_23B";

	public static final String INSTRUCTIONCODE23E = "InstructionCode_23E";

	public static final String TRADEDATE30T = "TradeDate_30T";

	public static final String VALUEDATE30V = "ValueDate_30V";

	public static final String TRANSDETAILS32A = "TransDetails_32A";
	public static final String TRANSDETAILS32B = "TransDetails_32B";

	public static final String VALUEDATE32A = "ValueDate";

	public static final String CURRENCYCODE32A = "CurrencyCode";

	public static final String AMOUNT32A = "Amount";

	public static final String CURRENCY33B = "Currency_33B";

	public static final String INSTRUCTEDAMOUNT33B = "InstructedAmount_33B";
	public static final String TRANSAMOUNT33B = "TransAmount_33B";

	public static final String EXCHANGERATE36 = "ExchangeRate_36";

	public static final String ORDERINGCUSTOMER50 = "OrderingCustomer_50";

	public static final String TAG50 = "Tag_50";

	public static final String ORDERINGINSTITUTION52 = "OrderingInstitution_52";

	public static final String TAG52 = "Tag_52";

	public static final String SENDERSCORRSPONDENT53 = "SendersCorrespondent_53";

	public static final String DELIVERYAGENT53 = "DeliveryAgent_53";

	public static final String TAG53 = "Tag_53";

	public static final String RECEIVERSCORRESPONDENT54 = "Receiverscorrespondent_54";

	public static final String TAG54 = "Tag_54";

	public static final String INTERMEDIARY56A = "Intermediary_56a";

	public static final String TAG56 = "Tag_56";

	public static final String RECEIVEAGENT57 = "ReceiveAgent_57";

	public static final String RECEIVEAGENT571 = "ReceiveAgent_57_1";

	public static final String ACCOUNTWITHINST57A = "AccountWithInstitution_57a";

	public static final String TAG571 = "Tag_57_1";

	public static final String TAG57 = "Tag_57";

	public static final String BENIFICIARYCUSTOMER59 = "BeneficiaryCustomer_59";

	public static final String REMITTANCEINFO70 = "RemittanceInformation_70";

	public static final String DETAILSOFCHARGES71A = "DetailsofCharges_71a";

	public static final String SENDERSCHARGES71F = "Senderscharges_71f";

	public static final String SENDERRECEIVERINFO72 = "SendertoReceiverInformation_72";

	public static final String NARRATION79 = "Narration_79";

	public static final String TAG11S = "Tag_11S";

	public static final String MESSAGETYPE11S = "MessageType";

	public static final String DATE11S = "Date";

	public static final String TIME11S = "Time";

	public static final String TAG58VALUE = "Tag58_Value";

	public static final String TAG58 = "Tag_58";

	public static final String PARTYA82 = "PartyA_82";

	public static final String PARTYB87 = "PartyB_87";

	public static final String TAG87 = "Tag_87";

	public static final String BROCKERID88 = "BrockerID_88";

	public static final String TAG88 = "Tag_88";

	/**
	 * XML GENERATION CONSTANTS
	 */
	public static final String tagFlagA = "A";

	public static final String tagFlagD = "D";

	public static final String INSERT_ACTION = "I";

	public static final String AMMEND_ACTION = "A";

	public static final String CANCEL_ACTION = "C";

	public static final String XML_PREPEND_START = "<";

	public static final String XML_APPPEND_START = ">";

	public static final String XML_PREPEND_END = "</";

	public static final String XML_APPPEND_END = ">";

	/**
	 * JMS[MQSERIES] PROPERTIES
	 */
	public static final String MQSERVER_PORT = "swift.meridian.request.port";

	public static final String MQSERVER_IP = "swift.meridian.request.ip";

	public static final String MQSERVER_CHANNEL = "swift.meridian.request.channelname";

	public static final String MQSERVER_QUEUENAME = "swift.meridian.request.queuename";
	
	public static final String MQSERVER_INCOMINGQUEUENAME = "swift.meridian.request.incomingqueuename";

	public static final String MQSERVER_ERRORQUEUENAME = "swift.meridian.request.errorqueuename";

	public static final String MQSERVER_QUEUEMGR = "swift.meridian.request.queuemgr";

	public static final String JMS_CONNECTION_FACTORY = "swift.jms.connection.factory.class";

}
