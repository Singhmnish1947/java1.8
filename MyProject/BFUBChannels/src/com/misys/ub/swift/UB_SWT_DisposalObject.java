/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: UB_SWT_DisposalObject.java,v 1.4 2008/11/24 11:21:08 sukirtim Exp $
 * Updated by Rubalin Das
 */
package com.misys.ub.swift;

import java.math.BigDecimal;
import java.sql.Date;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.CommonConstants;
/**
 * @author Shaileja
 *
 */
public class UB_SWT_DisposalObject extends BankFusionObject {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String currentDealNumber = "CURRENT DEALNUMBER";
	private String relatedDealNumber = "RELATED DEALNUMBER";
	private String previousDealRecordNumber = "PREV DEALRECORDNUMBER";
	private String nextDealRecordNumber = "NEX TDEALRECORDNUMBER";
	private int paymentFlagMT202 = 1;
	private int receiptFlagMT210 = 1;
	private int confirmationFlag = 1;
	private int cancelFlag = 1;
	private int verifyFlag = 1;
	private String mainAccountNo = "MAIN ACC NO";
	private String contraAccountNo = "CONTRA ACC NO";
	private String messageType = "MTXXX";
	private String codeWord = "NEW";
    private Date valueDate = SystemInformationManager.getInstance().getBFSystemDate();
    private Date postDate = SystemInformationManager.getInstance().getBFSystemDate();
    private Date maturityDate = SystemInformationManager.getInstance().getBFSystemDate();
    private Date nextInterestDueDate = SystemInformationManager.getInstance().getBFSystemDate();
    private Date interestPeriodStartDate = SystemInformationManager.getInstance().getBFSystemDate();
    private Date interestPeriodEndDate = SystemInformationManager.getInstance().getBFSystemDate();
	private BigDecimal transactionAmount = new BigDecimal("1000.00");
	private BigDecimal contractAmount = new BigDecimal("1000.00");
	private BigDecimal interestAmount = new BigDecimal("1000.00");
	private BigDecimal interestOrExchangeRate = new BigDecimal("2.00");;
	//	private int interestPeriod=1;
	private String brokerNumber = "BROKER NO";
	private String draftNumber = "DRAFT NO";
	private String mainAccCustomerNumber = "MAIN ACC CUST NO";
	private String mainAccCurrencyCode = "MAIN ACC CURR CODE";
	private String contraAccCustomerNumber = "CONTRA ACC CUST NO";
	private String contraAccCurrencyCode = "CONTRA ACC CURR CODE";
	private int messageStatus = 001;                      
	private String PayReceiveFlag = "P";
	private String SI_PayReceiveFlag = "PAY";
	private String SI_PayToBICCode = "PAYBICXX";
	private String SI_PayToText1 = "PAYTOTEXT1";
	private String SI_PayToText2 = "PAYTOTEXT2";
	private String SI_PayToText3 = "PAYTOTEXT3";

	/*	Rubalin Added New Field For Rorganizing Settlement Instruction */

	private String SI_PayToText4 = "PAYTOTEXT4";

	/* Rubalin Changed For Rorganizing Settlement Instruction removed "SI_PayToAccInfo" &
	 * "SI_PayToNAT_CLR_Code" And Added "SI_PayToPartyIdentifier"*/

	private String SI_PayToPartyIdentifier = "PAYTOPARTYIDENTIFIER";
	private String SI_AccWithCode = "ACCWITHCODE";
	private String SI_AccWithText1 = "ACCWITHTEXT1";
	private String SI_AccWithText2 = "ACCWITHTEXT2";
	private String SI_AccWithText3 = "ACCWITHTEXT3";
	private String DayCountFraction = "";

  /* Rubalin Added New Field For Rorganizing Settlement Instruction*/

	public String getDayCountFraction() {
		return DayCountFraction;
	}

	public void setDayCountFraction(String dayCountFraction) {
		DayCountFraction = dayCountFraction;
	}

	private String SI_AccWithText4 = "ACCWITHTEXT4";
	private String SI_AccWithPartyIdentifier = "ACCWITHPARTYIDENTIFIER";
	private String SI_IntermediatoryCode = "INTERMCODE";
	private String SI_IntermediatoryText1 = "INTERMTEXT1";
	private String SI_IntermediatoryText2 = "INTERMTEXT2";
	private String SI_IntermediatoryText3 = "INTERMTEXT3";

 /* Added New Field For Rorganizing Settlement Instruction*/

	private String SI_IntermediatoryText4 = "INTERMTEXT4";
	/* Rubalin Changed For Rorganizing Settlement Instruction removed "SI_IntermediatoryAccInfo" &
	 * "SI_IntermediatoryNAT_CLR_Code" And Added "SI_IntermediaryPartyIdentifier"*/

	private String SI_IntermediaryPartyIdentifier = "INTERMEDIARYPARTYIDENIFIER";

	private String SI_ForAccountText1 = "FORACCTEXT1";
	private String SI_ForAccountText2 = "FORACCTEXT2";
	private String SI_ForAccountText3 = "FORACCTEXT3";
	 /* Rubalin Added New Field For Rorganizing Settlement Instruction*/
	private String SI_ForAccountText4 = "FORACCTEXT4";

	private String SI_ForAccountPartyIdentifier = "FORACCPARTYIDENTIFIER";

	private String SI_BankToBankInfo1 = "BANKTOBANKINFO1";
	private String SI_BankToBankInfo2 = "BANKTOBANKINFO2";
	private String SI_BankToBankInfo3 = "BANKTOBANKINFO3";
	private String SI_BankToBankInfo4 = "BANKTOBANKINFO4";
	private String SI_BankToBankInfo5 = "BANKTOBANKINFO5";
	private String SI_BankToBankInfo6 = "BANKTOBANKINFO6";
	private String SI_PayDetails1 = "PAYDETAIL1";
	private String SI_PayDetails2 = "PAYDETAIL2";
	private String SI_PayDetails3 = "PAYDETAIL3";
	private String SI_PayDetails4 = "PAYDETAIL4";
	private String SI_ChargeCode = "CHARGECODE";
	private String SI_BankOpCode = "BANKOPCODE";
	private String SI_BankInstructionCode = "BANKINSTCODE";
	private String SI_BankAddlInstrCode = "BANKADDLINSTCODE";
	private String SI_OrdCustBICCode = "ORDCUSTBICCODE";
	private String SI_OrdCustAccLine = "ORDCUSTACCLINE";
	private String SI_OrdCustText1 = "ORDCUSTTEXT1";
	private String SI_OrdCustText2 = "ORDCUSTTEXT2";
	private String SI_OrdCustText3 = "ORDCUSTTEXT3";

    /* Rubalin Added New Field For Rorganizing Settlement Instruction*/

	private String SI_OrdCustText4 = "ORDCUSTTEXT4";
	private String SI_OrdInstBICCode = "ORDINSTBICCODE";
	private String SI_OrdInstAccInfo = "ORDINSTACCINFO";
	private String SI_OrdInstText1 = "ORDINSTTEXT1";
	private String SI_OrdInstText2 = "ORDINSTTEXT2";
	private String SI_OrdInstText3 = "ORDINSTTEXT3";

	/* Rubalin Added New Field For Rorganizing Settlement Instruction*/

	private String SI_OrdInstText4 = "ORDINSTTEXT4";
	private BigDecimal SI_SendersCharges = new BigDecimal("1.000");
	private String clientNumber = "CLIENTNUMBER";
	private String transactionStatus = "1223333";
	private String SI_OrdCustAccInfo = "INFO";
	private String term = "TERM";
	private String disposalRef = "REF";
	private int crdrFlag;
	private String dealOriginator = CommonConstants.EMPTY_STRING;
	private String senderChargeCurrency = CommonConstants.EMPTY_STRING;
	private String partyIdentifier = CommonConstants.EMPTY_STRING;
	private String partyIdentifierAdd1 = CommonConstants.EMPTY_STRING;
	private String partyIdentifierAdd2 = CommonConstants.EMPTY_STRING;
	private String partyIdentifierAdd3 = CommonConstants.EMPTY_STRING;
	private String partyIdentifierAdd4 = CommonConstants.EMPTY_STRING;
	private String orderingInstitution=CommonConstants.EMPTY_STRING;
	private String generate103Plus="GENERATE103PLUSIND";
	private String forAccountIdentifierCode="FOR ACCOUNT IDENTIFIERCODE";
	private String orderingCustomerAccountNumber="ORDERING CUSTOMER ACCOUNT NUMBER";
	private String orderingCustomerIdentifierCode="ORDERING CUSTOMER IDENTIFIER CODE";
	private String senderToReceiverInfo1="SENDERTORECEIVERINFO1";
	private String senderToReceiverInfo2="SENDERTORECEIVERINFO2";
	private String senderToReceiverInfo3 ="SENDERTORECEIVERINFO3";
	private String senderToReceiverInfo4 ="SENDERTORECEIVERINFO4";
	private String senderToReceiverInfo5 ="SENDERTORECEIVERINFO5";
	private String senderToReceiverInfo6 ="SENDERTORECEIVERINFO6";
	/**
	 * ADDED 6  NEW "TERMS ANDCONDITION FOR DEALS" FIELD FOR REORGANIGING SETTLEMENT INSTRUCTION
	 */
	private String TermsAndConditionForDeals1 ="TERMSANDCONDITIONFORDEALS1";
	private String TermsAndConditionForDeals2 ="TERMSANDCONDITIONFORDEALS2";
	private String TermsAndConditionForDeals3 ="TERMSANDCONDITIONFORDEALS3";
	private String TermsAndConditionForDeals4 ="TERMSANDCONDITIONFORDEALS4";
	private String TermsAndConditionForDeals5 ="TERMSANDCONDITIONFORDEALS5";
	private String TermsAndConditionForDeals6 ="TERMSANDCONDITIONFORDEALS6";
	private String TransactionCode ="TRANSACTIONCODE";
	private byte[] messageXML;
	private BigDecimal ExchangeRate = new BigDecimal("1.000");
	private BigDecimal FundingAmount = new BigDecimal("1.000");
	private BigDecimal ReceiverChargeAmount = new BigDecimal("1.000");
	private String TransactionId = "TRANSACTIONID";

	// SWIFT 2017 new fields for MT300
	private String isNonDeliverable  = CommonConstants.EMPTY_STRING;
	private String isNDFOpen = CommonConstants.EMPTY_STRING;
	private String settlementCurrency= CommonConstants.EMPTY_STRING;
	private String settlementRateRC = CommonConstants.EMPTY_STRING;
	private String reOpeningConfirmation = CommonConstants.EMPTY_STRING;
	private String clearSettlementSession = CommonConstants.EMPTY_STRING;
    private Date valuationDate = SystemInformationManager.getInstance().getBFSystemDate();
	private String end2EndTxnRef = CommonConstants.EMPTY_STRING;
	private String serviceTypeId = CommonConstants.EMPTY_STRING;
    private String PaymentClearingCentre = CommonConstants.EMPTY_STRING;
    private String messagePreference = CommonConstants.EMPTY_STRING;
    private String instructedAmtCurrency = CommonConstants.EMPTY_STRING;
    private String creditExchangeRateType = CommonConstants.EMPTY_STRING;
	
	 // SWIFT 2019 changes start
//    private String SI_PayToText5 = "PAYTOTEXT5";
//    private String SI_OrdInstText5 = "ORDINSTTEXT5";
//    private String SI_IntermediatoryText5 = "INTERMTEXT5";
//    private String SI_AccWithText5 = "ACCWITHTEXT5";
    
    
    private String SI_PayToText5 =CommonConstants.EMPTY_STRING;
    private String SI_OrdInstText5 = CommonConstants.EMPTY_STRING;
    private String SI_IntermediatoryText5 =CommonConstants.EMPTY_STRING;
    private String SI_AccWithText5 = CommonConstants.EMPTY_STRING;

    public String getSI_AccWithText5() {
        return SI_AccWithText5;
    }

    public void setSI_AccWithText5(String sI_AccWithText5) {
        SI_AccWithText5 = sI_AccWithText5;
    }

    public String getSI_PayToText5() {
        return SI_PayToText5;
    }

    public void setSI_PayToText5(String sI_PayToText5) {
        SI_PayToText5 = sI_PayToText5;
    }

    public String getSI_OrdInstText5() {
        return SI_OrdInstText5;
    }

    public void setSI_OrdInstText5(String sI_OrdInstText5) {
        SI_OrdInstText5 = sI_OrdInstText5;
    }

    public String getSI_IntermediatoryText5() {
        return SI_IntermediatoryText5;
    }

    public void setSI_IntermediatoryText5(String sI_IntermediatoryText5) {
        SI_IntermediatoryText5 = sI_IntermediatoryText5;
    }

    // SWIFT 2019 changes end

    
    public String getCreditExchangeRateType() {
        return creditExchangeRateType;
    }

    public void setCreditExchangeRateType(String creditExchangeRateType) {
        this.creditExchangeRateType = creditExchangeRateType;
    }


    
    public String getInstructedAmtCurrency() {
        return instructedAmtCurrency;
    }

    public void setInstructedAmtCurrency(String instructedAmtCurrency) {
        this.instructedAmtCurrency = instructedAmtCurrency;
    }

    public String getPaymentClearingCentre() {
        return PaymentClearingCentre;
    }

    public void setPaymentClearingCentre(String PaymentClearingCentre) {
        this.PaymentClearingCentre = PaymentClearingCentre;
    }

	public String getIsNonDeliverable() {
		return isNonDeliverable;
	}

	public void setIsNonDeliverable(String isNonDeliverable) {
		this.isNonDeliverable = isNonDeliverable;
	}

	public String getIsNDFOpen() {
		return isNDFOpen;
	}

	public void setIsNDFOpen(String isNDFOpen) {
		this.isNDFOpen = isNDFOpen;
	}

	public String getSettlementCurrency() {
		return settlementCurrency;
	}

	public void setSettlementCurrency(String settlementCurrency) {
		this.settlementCurrency = settlementCurrency;
	}

	public String getSettlementRateRC() {
		return settlementRateRC;
	}

	public void setSettlementRateRC(String settlementRateRC) {
		this.settlementRateRC = settlementRateRC;
	}

	public String getReOpeningConfirmation() {
		return reOpeningConfirmation;
	}

	public void setReOpeningConfirmation(String reOpeningConfirmation) {
		this.reOpeningConfirmation = reOpeningConfirmation;
	}

	public String getClearSettlementSession() {
		return clearSettlementSession;
	}

	public void setClearSettlementSession(String clearSettlementSession) {
		this.clearSettlementSession = clearSettlementSession;
	}

	public Date getValuationDate() {
		return valuationDate;
	}

	public void setValuationDate(Date valuationDate) {
		this.valuationDate = valuationDate;
	}

	public String getTransactionId() {
		return this.TransactionId;
	}

	public void setTransactionId(String transactionId) {
		this.TransactionId = transactionId;
	}

	public String getTransactionCode() {
		return TransactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		TransactionCode = transactionCode;
	}

	public void setClientNumber(String clientNumber) {
		this.clientNumber = clientNumber;
	}

	public String getTermsAndConditionForDeals1() {
		return TermsAndConditionForDeals1;
	}

	public void setTermsAndConditionForDeals1(String termsAndConditionForDeals1) {
		TermsAndConditionForDeals1 = termsAndConditionForDeals1;
	}

	public String getTermsAndConditionForDeals2() {
		return TermsAndConditionForDeals2;
	}

	public void setTermsAndConditionForDeals2(String termsAndConditionForDeals2) {
		TermsAndConditionForDeals2 = termsAndConditionForDeals2;
	}

	public String getTermsAndConditionForDeals3() {
		return TermsAndConditionForDeals3;
	}

	public void setTermsAndConditionForDeals3(String termsAndConditionForDeals3) {
		TermsAndConditionForDeals3 = termsAndConditionForDeals3;
	}

	public String getTermsAndConditionForDeals4() {
		return TermsAndConditionForDeals4;
	}

	public void setTermsAndConditionForDeals4(String termsAndConditionForDeals4) {
		TermsAndConditionForDeals4 = termsAndConditionForDeals4;
	}

	public String getTermsAndConditionForDeals5() {
		return TermsAndConditionForDeals5;
	}

	public void setTermsAndConditionForDeals5(String termsAndConditionForDeals5) {
		TermsAndConditionForDeals5 = termsAndConditionForDeals5;
	}

	public String getTermsAndConditionForDeals6() {
		return TermsAndConditionForDeals6;
	}

	public void setTermsAndConditionForDeals6(String termsAndConditionForDeals6) {
		TermsAndConditionForDeals6 = termsAndConditionForDeals6;
	}

	public String getClientNumber() {
		return this.clientNumber;
	}

	public void setSenderChargeCurrency(String currency) {
		this.senderChargeCurrency = currency;
	}

	public String getSenderChargeCurrency() {
		return this.senderChargeCurrency;
	}

	public String getDealOriginator() {
		return this.dealOriginator;
	}

	public void setDealOriginator(String dealOriginator) {
		this.dealOriginator = dealOriginator;
	}

	public String getBrokerNumber() {
		return brokerNumber;
	}

	public void setBrokerNumber(String brokerNumber) {
		this.brokerNumber = brokerNumber;
	}

	public String getDraftNumber() {
		return draftNumber;
	}

	public void setDraftNumber(String draftNumber) {
		this.draftNumber = draftNumber;
	}

	public int getCancelFlag() {
		return cancelFlag;
	}

	public void setCancelFlag(int cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	public int getConfirmationFlag() {
		return confirmationFlag;
	}

	public void setConfirmationFlag(int confirmationFlag) {
		this.confirmationFlag = confirmationFlag;
	}

	public String getContraAccCurrencyCode() {
		return contraAccCurrencyCode;
	}

	public void setContraAccCurrencyCode(String contraAccCurrencyCode) {
		this.contraAccCurrencyCode = contraAccCurrencyCode;
	}

	public String getContraAccCustomerNumber() {
		return contraAccCustomerNumber;
	}

	public void setContraAccCustomerNumber(String contraAccCustomerNumber) {
		this.contraAccCustomerNumber = contraAccCustomerNumber;
	}

	public String getContraAccountNo() {
		return contraAccountNo;
	}

	public void setContraAccountNo(String contraAccountNo) {
		this.contraAccountNo = contraAccountNo;
	}

	public BigDecimal getContractAmount() {
		return contractAmount;
	}

	public void setContractAmount(BigDecimal contractAmount) {
		this.contractAmount = contractAmount;
	}

	public String getCurrentDealNumber() {
		return currentDealNumber;
	}

	public void setCurrentDealNumber(String currentDealNumber) {
		this.currentDealNumber = currentDealNumber;
	}

	public BigDecimal getInterestAmount() {
		return interestAmount;
	}

	public void setInterestAmount(BigDecimal interestAmount) {
		this.interestAmount = interestAmount;
	}

	public BigDecimal getInterestOrExchangeRate() {
		return interestOrExchangeRate;
	}

	public void setInterestOrExchangeRate(BigDecimal interestOrExchangeRate) {
		this.interestOrExchangeRate = interestOrExchangeRate;
	}

	public String getMainAccCurrencyCode() {
		return mainAccCurrencyCode;
	}

	public void setMainAccCurrencyCode(String mainAccCurrencyCode) {
		this.mainAccCurrencyCode = mainAccCurrencyCode;
	}

	public String getMainAccCustomerNumber() {
		return mainAccCustomerNumber;
	}

	public void setMainAccCustomerNumber(String mainAccCustomerNumber) {
		this.mainAccCustomerNumber = mainAccCustomerNumber;
	}

	public String getMainAccountNo() {
		return mainAccountNo;
	}

	public void setMainAccountNo(String mainAccountNo) {
		this.mainAccountNo = mainAccountNo;
	}

	public Date getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(Date maturityDate) {
		this.maturityDate = maturityDate;
	}

	public int getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(int messageStatus) {
		this.messageStatus = messageStatus;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public int getPaymentFlagMT202() {
		return paymentFlagMT202;
	}

	public void setPaymentFlagMT202(int paymentFlagMT202) {
		this.paymentFlagMT202 = paymentFlagMT202;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public String getPreviousDealRecordNumber() {
		return previousDealRecordNumber;
	}

	public void setPreviousDealRecordNumber(String previousDealRecordNumber) {
		this.previousDealRecordNumber = previousDealRecordNumber;
	}

	public int getReceiptFlagMT210() {
		return receiptFlagMT210;
	}

	public void setReceiptFlagMT210(int receiptFlagMT210) {
		this.receiptFlagMT210 = receiptFlagMT210;
	}

	public String getRelatedDealNumber() {
		return relatedDealNumber;
	}

	public void setRelatedDealNumber(String relatedDealNumber) {
		this.relatedDealNumber = relatedDealNumber;
	}


	public String getSI_AccWithCode() {
		return SI_AccWithCode;
	}

	public void setSI_AccWithCode(String accWithCode) {
		SI_AccWithCode = accWithCode;
	}



	public String getSI_AccWithText1() {
		return SI_AccWithText1;
	}

	public void setSI_AccWithText1(String accWithText1) {
		SI_AccWithText1 = accWithText1;
	}

	public String getSI_AccWithText2() {
		return SI_AccWithText2;
	}

	public void setSI_AccWithText2(String accWithText2) {
		SI_AccWithText2 = accWithText2;
	}

	public String getSI_AccWithText3() {
		return SI_AccWithText3;
	}

	public void setSI_AccWithText3(String accWithText3) {
		SI_AccWithText3 = accWithText3;
	}

	public String getSI_BankAddlInstrCode() {
		return SI_BankAddlInstrCode;
	}

	public void setSI_BankAddlInstrCode(String bankAddlInstrCode) {
		SI_BankAddlInstrCode = bankAddlInstrCode;
	}

	public String getSI_BankInstructionCode() {
		return SI_BankInstructionCode;
	}

	public void setSI_BankInstructionCode(String bankInstructionCode) {
		SI_BankInstructionCode = bankInstructionCode;
	}

	public String getSI_BankOpCode() {
		return SI_BankOpCode;
	}

	public void setSI_BankOpCode(String bankOpCode) {
		SI_BankOpCode = bankOpCode;
	}

	public String getSI_BankToBankInfo1() {
		return SI_BankToBankInfo1;
	}

	public void setSI_BankToBankInfo1(String bankToBankInfo1) {
		SI_BankToBankInfo1 = bankToBankInfo1;
	}

	public String getSI_BankToBankInfo2() {
		return SI_BankToBankInfo2;
	}

	public void setSI_BankToBankInfo2(String bankToBankInfo2) {
		SI_BankToBankInfo2 = bankToBankInfo2;
	}

	public String getSI_BankToBankInfo3() {
		return SI_BankToBankInfo3;
	}

	public void setSI_BankToBankInfo3(String bankToBankInfo3) {
		SI_BankToBankInfo3 = bankToBankInfo3;
	}

	public String getSI_BankToBankInfo4() {
		return SI_BankToBankInfo4;
	}

	public void setSI_BankToBankInfo4(String bankToBankInfo4) {
		SI_BankToBankInfo4 = bankToBankInfo4;
	}

	public String getSI_BankToBankInfo5() {
		return SI_BankToBankInfo5;
	}

	public void setSI_BankToBankInfo5(String bankToBankInfo5) {
		SI_BankToBankInfo5 = bankToBankInfo5;
	}

	public String getSI_BankToBankInfo6() {
		return SI_BankToBankInfo6;
	}

	public void setSI_BankToBankInfo6(String bankToBankInfo6) {
		SI_BankToBankInfo6 = bankToBankInfo6;
	}

	public String getSI_ChargeCode() {
		return SI_ChargeCode;
	}

	public void setSI_ChargeCode(String chargeCode) {
		SI_ChargeCode = chargeCode;
	}






	public String getSI_IntermediatoryCode() {
		return SI_IntermediatoryCode;
	}

	public void setSI_IntermediatoryCode(String intermediatoryCode) {
		SI_IntermediatoryCode = intermediatoryCode;
	}



	public String getSI_IntermediatoryText1() {
		return SI_IntermediatoryText1;
	}

	public void setSI_IntermediatoryText1(String intermediatoryText1) {
		SI_IntermediatoryText1 = intermediatoryText1;
	}

	public String getSI_IntermediatoryText2() {
		return SI_IntermediatoryText2;
	}

	public void setSI_IntermediatoryText2(String intermediatoryText2) {
		SI_IntermediatoryText2 = intermediatoryText2;
	}

	public String getSI_IntermediatoryText3() {
		return SI_IntermediatoryText3;
	}

	public void setSI_IntermediatoryText3(String intermediatoryText3) {
		SI_IntermediatoryText3 = intermediatoryText3;
	}

	public String getSI_OrdCustBICCode() {
		return SI_OrdCustBICCode;
	}

	public void setSI_OrdCustBICCode(String ordCustBICCode) {
		SI_OrdCustBICCode = ordCustBICCode;
	}

	public String getSI_OrdCustText1() {
		return SI_OrdCustText1;
	}

	public void setSI_OrdCustText1(String ordCustText1) {
		SI_OrdCustText1 = ordCustText1;
	}

	public String getSI_OrdCustText2() {
		return SI_OrdCustText2;
	}

	public void setSI_OrdCustText2(String ordCustText2) {
		SI_OrdCustText2 = ordCustText2;
	}

	public String getSI_OrdCustText3() {
		return SI_OrdCustText3;
	}

	public void setSI_OrdCustText3(String ordCustText3) {
		SI_OrdCustText3 = ordCustText3;
	}

	public String getSI_PayDetails1() {
		return SI_PayDetails1;
	}

	public void setSI_PayDetails1(String payDetails1) {
		SI_PayDetails1 = payDetails1;
	}

	public String getSI_PayDetails2() {
		return SI_PayDetails2;
	}

	public void setSI_PayDetails2(String payDetails2) {
		SI_PayDetails2 = payDetails2;
	}

	public String getSI_PayDetails3() {
		return SI_PayDetails3;
	}

	public void setSI_PayDetails3(String payDetails3) {
		SI_PayDetails3 = payDetails3;
	}

	public String getSI_PayDetails4() {
		return SI_PayDetails4;
	}

	public void setSI_PayDetails4(String payDetails4) {
		SI_PayDetails4 = payDetails4;
	}

	public String getSI_PayReceiveFlag() {
		return SI_PayReceiveFlag;
	}

	public void setSI_PayReceiveFlag(String payReceiveFlag) {
		SI_PayReceiveFlag = payReceiveFlag;
	}



	public String getSI_PayToBICCode() {
		return SI_PayToBICCode;
	}

	public void setSI_PayToBICCode(String payToBICCode) {
		SI_PayToBICCode = payToBICCode;
	}



	public String getSI_PayToText1() {
		return SI_PayToText1;
	}

	public void setSI_PayToText1(String payToText1) {
		SI_PayToText1 = payToText1;
	}

	public String getSI_PayToText2() {
		return SI_PayToText2;
	}

	public void setSI_PayToText2(String payToText2) {
		SI_PayToText2 = payToText2;
	}

	public String getSI_PayToText3() {
		return SI_PayToText3;
	}

	public void setSI_PayToText3(String payToText3) {
		SI_PayToText3 = payToText3;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public Date getValueDate() {
		return valueDate;
	}

	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}

	public int getVerifyFlag() {
		return verifyFlag;
	}

	public void setVerifyFlag(int verifyFlag) {
		this.verifyFlag = verifyFlag;
	}

	public String getSI_OrdInstBICCode() {
		return SI_OrdInstBICCode;
	}

	public void setSI_OrdInstBICCode(String ordInstBICCode) {
		SI_OrdInstBICCode = ordInstBICCode;
	}

	public String getSI_OrdInstText1() {
		return SI_OrdInstText1;
	}

	public void setSI_OrdInstText1(String ordInstText1) {
		SI_OrdInstText1 = ordInstText1;
	}

	public String getSI_OrdInstText2() {
		return SI_OrdInstText2;
	}

	public void setSI_OrdInstText2(String ordInstText2) {
		SI_OrdInstText2 = ordInstText2;
	}

	public String getSI_OrdInstText3() {
		return SI_OrdInstText3;
	}

	public void setSI_OrdInstText3(String ordInstText3) {
		SI_OrdInstText3 = ordInstText3;
	}

	public String getSI_OrdInstAccInfo() {
		return SI_OrdInstAccInfo;
	}

	public void setSI_OrdInstAccInfo(String ordInstAccInfo) {
		SI_OrdInstAccInfo = ordInstAccInfo;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public String getSI_OrdCustAccInfo() {
		return SI_OrdCustAccInfo;
	}

	public void setSI_OrdCustAccInfo(String ordCustAccInfo) {
		SI_OrdCustAccInfo = ordCustAccInfo;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String string) {
		this.term = string;
	}

	public BigDecimal getSI_SendersCharges() {
		return SI_SendersCharges;
	}

	public void setSI_SendersCharges(BigDecimal sendersCharges) {
		SI_SendersCharges = sendersCharges;
	}

	public String getDisposalRef() {
		return disposalRef;
	}

	public void setDisposalRef(String disposalRef) {
		this.disposalRef = disposalRef;
	}

	public String getCodeWord() {
		return codeWord;
	}

	public void setCodeWord(String codeWord) {
		this.codeWord = codeWord;
	}

	/*public int getInterestPeriod() {
		return interestPeriod;
	}
	public void setInterestPeriod(int interestPeriod) {
		this.interestPeriod = interestPeriod;
	}*/
	public String getNextDealRecordNumber() {
		return nextDealRecordNumber;
	}

	public void setNextDealRecordNumber(String nextDealRecordNumber) {
		this.nextDealRecordNumber = nextDealRecordNumber;
	}

	public String getSI_OrdCustAccLine() {
		return SI_OrdCustAccLine;
	}

	public void setSI_OrdCustAccLine(String ordCustAccLine) {
		SI_OrdCustAccLine = ordCustAccLine;
	}

	public UB_SWT_DisposalObject() {
		super();
	}

	public int getCrdrFlag() {
		return crdrFlag;
	}

	public void setCrdrFlag(int crdrFlag) {
		this.crdrFlag = crdrFlag;
	}

	public String getPartyIdentifier() {
		return partyIdentifier;
	}

	public void setPartyIdentifier(String partyIdentifier) {
		this.partyIdentifier = partyIdentifier;
	}

	public String getPartyIdentifierAdd1() {
		return partyIdentifierAdd1;
	}

	public void setPartyIdentifierAdd1(String partyIdentifierAdd1) {
		this.partyIdentifierAdd1 = partyIdentifierAdd1;
	}

	public String getPartyIdentifierAdd2() {
		return partyIdentifierAdd2;
	}

	public void setPartyIdentifierAdd2(String partyIdentifierAdd2) {
		this.partyIdentifierAdd2 = partyIdentifierAdd2;
	}

	public String getPartyIdentifierAdd3() {
		return partyIdentifierAdd3;
	}

	public void setPartyIdentifierAdd3(String partyIdentifierAdd3) {
		this.partyIdentifierAdd3 = partyIdentifierAdd3;
	}

	public String getPartyIdentifierAdd4() {
		return partyIdentifierAdd4;
	}

	public void setPartyIdentifierAdd4(String partyIdentifierAdd4) {
		this.partyIdentifierAdd4 = partyIdentifierAdd4;
	}

	public Date getInterestPeriodEndDate() {
		return interestPeriodEndDate;
	}

	public void setInterestPeriodEndDate(Date interestPeriodEndDate) {
		this.interestPeriodEndDate = interestPeriodEndDate;
	}

	public Date getInterestPeriodStartDate() {
		return interestPeriodStartDate;
	}

	public void setInterestPeriodStartDate(Date interestPeriodStartDate) {
		this.interestPeriodStartDate = interestPeriodStartDate;
	}

	public Date getNextInterestDueDate() {
		return nextInterestDueDate;
	}

	public void setNextInterestDueDate(Date nextInterestDueDate) {
		this.nextInterestDueDate = nextInterestDueDate;
	}

	public String getPayReceiveFlag() {
		return PayReceiveFlag;
	}

	public void setPayReceiveFlag(String payReceiveFlag) {
		PayReceiveFlag = payReceiveFlag;
	}

	public String getOrderingInstitution() {
		return orderingInstitution;
	}

	public void setOrderingInstitution(String orderingInstitution) {
		this.orderingInstitution = orderingInstitution;
	}
	public String getGenerate103Plus() {
		return generate103Plus;
	}

	public void setGenerate103Plus(String generate103Plus) {
		this.generate103Plus = generate103Plus;
	}

	public String getOrderingCustomerAccountNumber() {
		return orderingCustomerAccountNumber;
	}

	public void setOrderingCustomerAccountNumber(String orderingCustomerAccountNumber) {
		this.orderingCustomerAccountNumber = orderingCustomerAccountNumber;
	}
	public String getOrderingCustomerIdentifierCode () {
		return orderingCustomerIdentifierCode ;
	}

	public void setOrderingCustomerIdentifierCode (String orderingCustomerIdentifierCode ) {
		this.orderingCustomerIdentifierCode  = orderingCustomerIdentifierCode ;
	}
	public String getSenderToReceiverInfo1 () {
		return senderToReceiverInfo1 ;
	}

	public void setSenderToReceiverInfo1 (String senderToReceiverInfo1 ) {
		this.senderToReceiverInfo1  = senderToReceiverInfo1 ;
	}
	public String getSenderToReceiverInfo2 () {
		return senderToReceiverInfo2 ;
	}

	public void setSenderToReceiverInfo2 (String senderToReceiverInfo2 ) {
		this.senderToReceiverInfo2  = senderToReceiverInfo2 ;
	}
	public String getSenderToReceiverInfo3 () {
		return senderToReceiverInfo3;
	}

	public void setSenderToReceiverInfo3(String senderToReceiverInfo3) {
		this.senderToReceiverInfo3 = senderToReceiverInfo3;
	}
	public String getSenderToReceiverInfo4 () {
		return senderToReceiverInfo4;
	}

	public void setSenderToReceiverInfo4(String senderToReceiverInfo4) {
		this.senderToReceiverInfo4 = senderToReceiverInfo4;
	}
	public String getSI_PayToPartyIdentifier() {
		return SI_PayToPartyIdentifier;
	}

	public void setSI_PayToPartyIdentifier(String payToPartyIdentifier) {
		SI_PayToPartyIdentifier = payToPartyIdentifier;
	}

	public String getSenderToReceiverInfo5 () {
		return senderToReceiverInfo5;
	}

	public void setSenderToReceiverInfo5(String senderToReceiverInfo5) {
		this.senderToReceiverInfo5 = senderToReceiverInfo5;
	}
	public String getSenderToReceiverInfo6 () {
		return senderToReceiverInfo6;
	}

	public void setSenderToReceiverInfo6(String senderToReceiverInfo6) {
		this.senderToReceiverInfo6 = senderToReceiverInfo6;
	}

	public String getSI_IntermediaryPartyIdentifier() {
		return SI_IntermediaryPartyIdentifier;
	}

	public void setSI_IntermediaryPartyIdentifier(String intermediaryPartyIdentifier) {
		SI_IntermediaryPartyIdentifier = intermediaryPartyIdentifier;
	}

	public String getSI_AccWithText4() {
		return SI_AccWithText4;
	}

	public void setSI_AccWithText4(String accWithText4) {
		SI_AccWithText4 = accWithText4;
	}


	public String getSI_OrdCustText4() {
		return SI_OrdCustText4;
	}

	public void setSI_OrdCustText4(String ordCustText4) {
		SI_OrdCustText4 = ordCustText4;
	}

	public String getSI_OrdInstText4() {
		return SI_OrdInstText4;
	}

	public void setSI_OrdInstText4(String ordInstText4) {
		SI_OrdInstText4 = ordInstText4;
	}

	public String getSI_PayToText4() {
		return SI_PayToText4;
	}

	public void setSI_PayToText4(String payToText4) {
		SI_PayToText4 = payToText4;
	}

	public String getSI_IntermediatoryText4() {
		return SI_IntermediatoryText4;
	}

	public void setSI_IntermediatoryText4(String intermediatoryText4) {
		SI_IntermediatoryText4 = intermediatoryText4;
	}

	public String getSI_ForAccountText1() {
		return SI_ForAccountText1;
	}

	public void setSI_ForAccountText1(String forAccountText1) {
		SI_ForAccountText1 = forAccountText1;
	}

	public String getSI_ForAccountText2() {
		return SI_ForAccountText2;
	}

	public void setSI_ForAccountText2(String forAccountText2) {
		SI_ForAccountText2 = forAccountText2;
	}

	public String getSI_ForAccountText3() {
		return SI_ForAccountText3;
	}

	public void setSI_ForAccountText3(String forAccountText3) {
		SI_ForAccountText3 = forAccountText3;
	}

	public String getSI_ForAccountText4() {
		return SI_ForAccountText4;
	}

	public void setSI_ForAccountText4(String forAccountText4) {
		SI_ForAccountText4 = forAccountText4;
	}

	public String getSI_ForAccountPartyIdentifier() {
		return SI_ForAccountPartyIdentifier;
	}

	public void setSI_ForAccountPartyIdentifier(String forAccountPartyIdentifier) {
		SI_ForAccountPartyIdentifier = forAccountPartyIdentifier;
	}

	public String getSI_AccWithPartyIdentifier() {
		return SI_AccWithPartyIdentifier;
	}

	public void setSI_AccWithPartyIdentifier(String accWithPartyIdentifier) {
		SI_AccWithPartyIdentifier = accWithPartyIdentifier;
	}

	public String getForAccountIdentifierCode() {
		return forAccountIdentifierCode;
	}

	public void setForAccountIdentifierCode(String forAccountIdentifierCode) {
		this.forAccountIdentifierCode = forAccountIdentifierCode;
	}

	public byte[] getMessageXML() {
		return messageXML;                          
	}

	public void setMessageXML(byte[] messageXML) {  
		this.messageXML = messageXML;                        
	}

	public BigDecimal getExchangeRate() {
		return ExchangeRate;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		ExchangeRate = exchangeRate;
	}

	public BigDecimal getFundingAmount() {
		return FundingAmount;
	}

	public void setFundingAmount(BigDecimal fundingAmount) {
		FundingAmount = fundingAmount;
	}

	public BigDecimal getReceiverChargeAmount() {
		return ReceiverChargeAmount;
	}

	public void setReceiverChargeAmount(BigDecimal receiverChargeAmount) {
		ReceiverChargeAmount = receiverChargeAmount;
	}
	
	public String getEnd2EndTxnRef() {
		return end2EndTxnRef;
	}

	public void setEnd2EndTxnRef(String end2EndTxnRef) {
		this.end2EndTxnRef = end2EndTxnRef;
	}

	public String getServiceTypeId() {
		return serviceTypeId;
	}

	public void setServiceTypeId(String serviceTypeId) {
		this.serviceTypeId = serviceTypeId;
	}
	public String getMessagePreference() {
		return messagePreference;
	}

	public void setMessagePreference(String messagePreference) {
		this.messagePreference = messagePreference;
	}
}
