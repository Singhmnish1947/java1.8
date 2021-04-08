/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: SWT_DisposalObject.java,v 1.4 2008/08/31 13:04:14 utpals Exp $
 *
 */
package com.misys.ub.swift;

import java.math.BigDecimal;
import java.sql.Date;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.CommonConstants;

public class SWT_DisposalObject extends BankFusionObject {

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
	private String SI_PayToAccInfo = "PAYTOaCCINFO";
	private String SI_PayToNAT_CLR_Code = "PAYTONATCLRCODE";
	private String SI_AccWithCode = "ACCWITHCODE";
	private String SI_AccWithText1 = "ACCWITHTEXT1";
	private String SI_AccWithText2 = "ACCWITHTEXT2";
	private String SI_AccWithText3 = "ACCWITHTEXT3";
	private String SI_AccWithAccInfo = "ACCWITHACCINFO";;
	private String SI_AccWithNAT_CLR_Code = "ACCWITHNATCLRCODE";;
	private String SI_IntermediatoryCode = "INTERMCODE";
	private String SI_IntermediatoryText1 = "INTERMTEXT1";
	private String SI_IntermediatoryText2 = "INTERMTEXT2";
	private String SI_IntermediatoryText3 = "INTERMTEXT3";
	private String SI_IntermediatoryAccInfo = "INTERMACCINFO";
	private String SI_IntermediatoryNAT_CLR_Code = "INTERMNATCLRCODE";
	private String SI_ForAccountText1 = "FORACCTEXT1";
	private String SI_ForAccountText2 = "FORACCTEXT2";
	private String SI_ForAccountText3 = "FORACCTEXT3";
	private String SI_ForAccountInfo = "FORACCINFO";
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
	private String SI_OrdInstBICCode = "ORDINSTBICCODE";
	private String SI_OrdInstAccInfo = "ORDINSTACCINFO";
	private String SI_OrdInstText1 = "ORDINSTTEXT1";
	private String SI_OrdInstText2 = "ORDINSTTEXT2";
	private String SI_OrdInstText3 = "ORDINSTTEXT3";
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

	public void setClientNumber(String clientNumber) {
		this.clientNumber = clientNumber;
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

	public String getSI_AccWithAccInfo() {
		return SI_AccWithAccInfo;
	}

	public void setSI_AccWithAccInfo(String accWithAccInfo) {
		SI_AccWithAccInfo = accWithAccInfo;
	}

	public String getSI_AccWithCode() {
		return SI_AccWithCode;
	}

	public void setSI_AccWithCode(String accWithCode) {
		SI_AccWithCode = accWithCode;
	}

	public String getSI_AccWithNAT_CLR_Code() {
		return SI_AccWithNAT_CLR_Code;
	}

	public void setSI_AccWithNAT_CLR_Code(String accWithNAT_CLR_Code) {
		SI_AccWithNAT_CLR_Code = accWithNAT_CLR_Code;
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

	public String getSI_ForAccountInfo() {
		return SI_ForAccountInfo;
	}

	public void setSI_ForAccountInfo(String forAccountInfo) {
		SI_ForAccountInfo = forAccountInfo;
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

	public String getSI_IntermediatoryAccInfo() {
		return SI_IntermediatoryAccInfo;
	}

	public void setSI_IntermediatoryAccInfo(String intermediatoryAccInfo) {
		SI_IntermediatoryAccInfo = intermediatoryAccInfo;
	}

	public String getSI_IntermediatoryCode() {
		return SI_IntermediatoryCode;
	}

	public void setSI_IntermediatoryCode(String intermediatoryCode) {
		SI_IntermediatoryCode = intermediatoryCode;
	}

	public String getSI_IntermediatoryNAT_CLR_Code() {
		return SI_IntermediatoryNAT_CLR_Code;
	}

	public void setSI_IntermediatoryNAT_CLR_Code(String intermediatoryNAT_CLR_Code) {
		SI_IntermediatoryNAT_CLR_Code = intermediatoryNAT_CLR_Code;
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

	public String getSI_PayToAccInfo() {
		return SI_PayToAccInfo;
	}

	public void setSI_PayToAccInfo(String payToAccInfo) {
		SI_PayToAccInfo = payToAccInfo;
	}

	public String getSI_PayToBICCode() {
		return SI_PayToBICCode;
	}

	public void setSI_PayToBICCode(String payToBICCode) {
		SI_PayToBICCode = payToBICCode;
	}

	public String getSI_PayToNAT_CLR_Code() {
		return SI_PayToNAT_CLR_Code;
	}

	public void setSI_PayToNAT_CLR_Code(String payToNAT_CLR_Code) {
		SI_PayToNAT_CLR_Code = payToNAT_CLR_Code;
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

	public SWT_DisposalObject() {
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
}
