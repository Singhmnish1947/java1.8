/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: SWT_MT103192Constants.java,v 1.3 2008/08/12 20:13:10 vivekr Exp $
 *
 */
package com.misys.ub.swift;

public class SWT_MT103192Constants {

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

	public static final String TRANSREFNO20 = "TransactionReferenceNumber_20";

	public static final String RELATEDREF21 = "RelatedReference_21";

	public static final String BANKOPCODE23B = "BankOperationCode_23B";

	public static final String INSTRUCTIONCODE23E = "InstructionCode_23E";

	public static final String TRANSDETAILS32A = "TranDet_32A";

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

	public static final String SENDERSCORRSPONDENT53 = "DeliveryAgent_53";

	public static final String DELIVERYAGENT53 = "DeliveryAgent_53";

	public static final String TAG53 = "Tag_53";

	public static final String RECEIVERSCORRESPONDENT54 = "Receiverscorrespondent_54";

	public static final String TAG54 = "Tag_54";

	public static final String INTERMEDIARY56A = "Intermediary_56";

	public static final String TAG56 = "Tag_56";

	public static final String ACCOUNTWITHINST57 = "AccountWithInstitution_57";

	public static final String TAG57 = "Tag_57";

	public static final String BENIFICIARYCUSTOMER59 = "BeneficiaryCustomer_59";

	public static final String REMITTANCEINFO70 = "RemittanceInformation_70";

	public static final String DETAILSOFCHARGES71A = "DetailsofCharges_71A";

	public static final String SENDERSCHARGES71F = "Senderscharges_71F";

	public static final String SENDERRECEIVERINFO72 = "SendertoReceiverInformation_72";

	public static final String NARRATION79 = "Narration_79";

	public static final String TAG11S = "Tag_11S";

	public static final String MESSAGETYPE11S = "MessageType";

	public static final String DATE11S = "Date";

	public static final String TIME11S = "Time";

}
