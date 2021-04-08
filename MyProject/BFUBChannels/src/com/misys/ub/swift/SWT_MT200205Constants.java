/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: SWT_MT200205Constants.java,v 1.3 2008/08/12 20:13:09 vivekr Exp $
 *
 */
package com.misys.ub.swift;

public class SWT_MT200205Constants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public static final String MESSAGE292 = "292";

	public static final String MESSAGETYPE = "MessageType";

	public static final String DISPOSALREF = "DisposalRef";

	public static final String SENDER = "Sender";

	public static final String RECEIVER = "Receiver";

	public static final String TRANSREFNO20 = "TransactionReferenceNumber_20";

	public static final String TRANSDETAILS32A = "TranDet_32A";

	public static final String VALUEDATE32A = "ValueDate";

	public static final String CURRENCYCODE32A = "CurrencyCode";

	public static final String AMOUNT32A = "Amount";

	public static final String ORDERINGCUSTOMER50 = "OrderingCustomer_50";

	public static final String TAG50 = "Tag_50";

	public static final String SENDERSCORRSPONDENT53B = "SendersCorrespondent_53B";

	public static final String INTERMEDIARY56 = "Intermediary_56";

	public static final String TAG56 = "Tag_56";

	public static final String ACCOUNTWITHINST57 = "AccountWithInstitution_57";

	public static final String TAG57 = "Tag_57";

	public static final String SENDERRECEIVERINFO72 = "SendertoReceiverInformation_72";

	/**
	 * Extra tags for message type 205
	 */

	public static final String RELATEDREF21 = "RelatedReference_21";

	public static final String ORDERINGINSTITUTION52 = "OrderingInstitution_52";

	public static final String TAG52 = "Tag_52";

	public static final String SENDERSCORRSPONDENT53 = "SendersCorrespondent_53";

	public static final String TAG53 = "Tag_53";

	public static final String INTERMEDIARY56A = "Intermediary_56A";

	public static final String ACCOUNTWITHINST57A = "AccountWithInstitution_57a";

	public static final String BENIFICIARYINSTITUTE58 = "BeneficiaryInstitute_58";

	public static final String TAG58 = "Tag_58";

}
