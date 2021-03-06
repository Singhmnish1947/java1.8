/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: SWT_MT320Constants.java,v 1.3 2008/08/12 20:13:08 vivekr Exp $
 *
 */
package com.misys.ub.swift;

public class SWT_MT320Constants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public static final String MESSAGETYPE = "MessageType";

	public static final String DISPOSALREF = "DisposalRef";

	public static final String SENDER = "Sender";

	public static final String RECEIVER = "Receiver";

	public static final String SEQUENCEA = "SequenceA";

	public static final String SENDERSREF20 = "Senders_Reference_20";

	public static final String RELATEDSREF21 = "ReleatedRef_21";

	public static final String TYPEOPERATION22A = "TypeOperation_22A";

	public static final String TYPEEVENT22B = "TypeEvent_22B";

	public static final String COMMONREF22C = "CommonRef_22C";

	public static final String PARTYA82 = "PartyA_82";

	public static final String PARTYB87 = "PartyB_87";

	public static final String TAG87 = "Tag_87";

	public static final String CONDITION77D = "Condition_77D";

	/**
	 * Sequence B
	 */
	public static final String SEQUENCEB = "SequenceB";

	public static final String ROLE17R = "Role_17R";

	public static final String TRADEDATE30T = "TradeDate_30T";

	public static final String VALUEDATE30V = "ValueDate_30V";

	public static final String MATURITYDATE30P = "MaturityDate_30P";

	public static final String PRINAMOUNT32B = "PrinAmount_32B";

	public static final String SETTLEAMOUNT32H = "SettlAmount_32H";

	public static final String NXTINTDATE30X = "NxtIntDate_30X";

	public static final String CURRINTAMT34E = "CurrencyIntAmt_34E";

	public static final String INTERESTRATE37G = "InterestRate_37G";

	public static final String DAYCOUNTFRACTION14D = "DayCountFraction_14D";

	/**
	 * Sequence C
	 */

	public static final String SEQUENCEC = "SequenceC";

	public static final String DELIVERYAGENT53 = "DeliveryAgent_53";

	public static final String TAG53 = "Tag_53";

	public static final String INTERMEDIARY56 = "InterMediary_56";

	public static final String TAG56 = "Tag_56";

	public static final String RECEIVEAGENT57 = "ReceivingAgent_57";

	public static final String TAG57 = "Tag_57";

	/**
	 * Sequence D
	 */

	public static final String SEQUENCED = "SequenceD";

}
