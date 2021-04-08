/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_BatchRecord.java,v 1.7 2008/08/12 20:15:34 vivekr Exp $
 * **********************************************************************************
 * 
 * Revision 1.14  2008/02/06 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 */
package com.misys.ub.fontis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.sql.Date;

public class FON_BatchRecord {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Fontis Batch ID in UB : It's a GUID of max. 30 characters
	 */
	private String fontisBatchId;
	
	/**
	 * This is the character ‘G’ for general. It is always used to identify the general section of an IAT/TPP instruction.
	 */
	private String fontisRecordType;
	
	/**
	 * User ID
	 */
	private String userID;
	
	/**
	 * Instruction Batch Number. This a 7 digit sequence number which identifies an instruction batch.
	 * It ranges from 0000000-9999999.
	 */
	private String batchNo;
	
	/**
	 * Instruction Number
	 */
	private String instructionNo;
	
	/**
	 * User name of IAT/TPP creator
	 */
	private String userName;
	
	/**
	 * Address of IAT/TPP creator
	 */
	private String address;
	
	/**
	 * Time IAT/TPP was Created through FONTIS front end
	 */
	private String dateCreated;
	
	/**
	 * Time IAT/TPP was Processed through FONTIS front end
	 */
	private String dateProcessed;
	
	/**
	 * Number of debit details in the instruction
	 */
	private int no_Of_Debits;
	
	/**
	 * Sum of all the debit details
	 */
	private BigDecimal debitTotal;
	
	/**
	 * Number of credit details in the instruction
	 */
	private int no_Of_Credits;
	
	/**
	 * Sum of all the credit details
	 */
	private BigDecimal creditTotal;
	
	/**
	 * General Instruction Narrative
	 */
	private String generalComments;
	
	/**
	 * General Instruction Narrative
	 */
	private String narrative;
	
	/**
	 * Value Date of Transfer
	 */
	private Date valueDate;
	
	/**
	 * no. of decimals for base currency
	 */
	private int no_Of_Decs;
	
	/**
	 * Bearer of Bank Charges
	 */
	private char bankCharges;
	
	/**
	 * This Foreign Bank charges option is no longer used on the TPP instruction screen. It will still appear in 
	 * the processed TPP export file as a blank entry.
	 */
	private char foreignBankCharges;
	
	/**
	 * Bearer of Swift Charges
	 */
	private char swiftcharges;
	
	/**
	 * Bank to Bank Information. Your bank can specify default values for this field. Customer sites can
	 * then select the necessary value from a drop-down list within the Bank to Bank field when creating a TPP.
	 * These codes are defined in the EBW language file. Editing the codes contained in the Third Party
	 * Payments - Narratives display element within the EBW language and sending the new language file to the 
	 * customer site will allow them to select these codes when creating the instruction. These codes cannot be
	 *  more than 35 characters in length. See the FONTIS Language Editor in the System Management Function user guide.
	 */
	private String bankToBankInfo;
	
	/**
	 * One set of Extra option buttons. One option must be chosen. The number will indicate the button selected.
	 */
	private int one_Picked_Option;
	
	/**
	 * Additional Text1
	 */
	private String text1;
	
	/**
	 * Additional Text2
	 */
	private String text2;
	
	/**
	 * Number of options selected. This can range from 0 to 3.
	 */
	private int one_None_Both;
	
	/**
	 * Processing Staus flag:
	 * 0 - Unprocessed (first run of the program)
	 * 1 - Validation failed (debit transactions that have failed validation)
	 * 2 - Authorised (transactions that have failed validation can later be authorised by a supervisor using Fotis Authorization)
	 * 3 - Rejected (by supervisor)
	 * 4 - Processed and posted succesfully
	 * 5 - Failed and ignored (rejected during authorisation)
	 */
	private int statusFlag=0;
	
	/**
	 * Indicates Fontis batch has non base currency transaction
	 * TRUE-has non base currency transaction
	 * FALSE-do not have non base currency transaction
	 */
	private boolean isForgnCurrBatch=false;
	
	/**
	 * Mandatory validation failed fields :
	 * TRUE-Failed
	 * FALSE-Passed
	 */
	private boolean isMV_Failed=false;
	
	/**
	 * Error messages, if any.
	 */
	private String errMessage=null;
	
	/**
	 * Transaction reference number using which transaction was posted in UB. Required for tracking the
	 * transactions & also for reporting purposes, strictly in & after UB-3A release.
	 */
	private String transactionReference = "";
	
	/**
	 * Credit transaction records for this batch
	 */
	private ArrayList creditRecords=new ArrayList();
	
	/**
	 * Debit transaction records for this batch
	 */
	private ArrayList debitRecords=new ArrayList();
	
	/**
	 * Flag to check debit posted to suspense due to insufficient funds
	 */
	private boolean inSuffIndPostAllCrDrToSuspense=false;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	
	public String getGeneralComments() {
		return generalComments;
	}
	public void setGeneralComments(String generalComments) {
		this.generalComments = generalComments;
	}
	public String getInstructionNo() {
		return instructionNo;
	}
	public void setInstructionNo(String instructionNo) {
		this.instructionNo = instructionNo;
	}
	public int getNo_Of_Credits() {
		return no_Of_Credits;
	}
	public void setNo_Of_Credits(int no_Of_Credits) {
		this.no_Of_Credits = no_Of_Credits;
	}
	public int getNo_Of_Debits() {
		return no_Of_Debits;
	}
	public void setNo_Of_Debits(int no_Of_Debits) {
		this.no_Of_Debits = no_Of_Debits;
	}
	public int getNo_Of_Decs() {
		return no_Of_Decs;
	}
	public void setNo_Of_Decs(int no_Of_Decs) {
		this.no_Of_Decs = no_Of_Decs;
	}
	public int getOne_None_Both() {
		return one_None_Both;
	}
	public void setOne_None_Both(int one_None_Both) {
		this.one_None_Both = one_None_Both;
	}
	public String getText1() {
		return text1;
	}
	public void setText1(String text1) {
		this.text1 = text1;
	}
	public String getText2() {
		return text2;
	}
	public void setText2(String text2) {
		this.text2 = text2;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getDateProcessed() {
		return dateProcessed;
	}
	public void setDateProcessed(String dateProcessed) {
		this.dateProcessed = dateProcessed;
	}
	public BigDecimal getCreditTotal() {
		return creditTotal;
	}
	public void setCreditTotal(BigDecimal creditTotal) {
		this.creditTotal = creditTotal;
	}
	public BigDecimal getDebitTotal() {
		return debitTotal;
	}
	public void setDebitTotal(BigDecimal debitTotal) {
		this.debitTotal = debitTotal;
	}
	public Date getValueDate() {
		return valueDate;
	}
	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}
	public int getOne_Picked_Option() {
		return one_Picked_Option;
	}
	public void setOne_Picked_Option(int one_Picked_Option) {
		this.one_Picked_Option = one_Picked_Option;
	}
	public char getBankCharges() {
		return bankCharges;
	}
	public void setBankCharges(char bankCharges) {
		this.bankCharges = bankCharges;
	}
	public String getBankToBankInfo() {
		return bankToBankInfo;
	}
	public void setBankToBankInfo(String bankToBankInfo) {
		this.bankToBankInfo = bankToBankInfo;
	}
	public ArrayList getCreditRecords() {
		return creditRecords;
	}
	public void setCreditRecords(ArrayList creditRecords) {
		this.creditRecords = creditRecords;
	}
	public ArrayList getDebitRecords() {
		return debitRecords;
	}
	public void setDebitRecords(ArrayList debitRecords) {
		this.debitRecords = debitRecords;
	}
	public String getFontisRecordType() {
		return fontisRecordType;
	}
	public void setFontisRecordType(String fontisRecordType) {
		this.fontisRecordType = fontisRecordType;
	}
	public char getForeignBankCharges() {
		return foreignBankCharges;
	}
	public void setForeignBankCharges(char foreignBankCharges) {
		this.foreignBankCharges = foreignBankCharges;
	}
	public String getNarrative() {
		return narrative;
	}
	public void setNarrative(String narrative) {
		this.narrative = narrative;
	}
	public char getSwiftcharges() {
		return swiftcharges;
	}
	public void setSwiftcharges(char swiftcharges) {
		this.swiftcharges = swiftcharges;
	}
	public int getStatusFlag() {
		return statusFlag;
	}
	public void setStatusFlag(int statusFlag) {
		this.statusFlag = statusFlag;
	}
	public String getErrMessage() {
		return errMessage;
	}
	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}
	public boolean isForgnCurrBatch() {
		return isForgnCurrBatch;
	}
	public void setForgnCurrBatch(boolean isForgnCurrBatch) {
		this.isForgnCurrBatch = isForgnCurrBatch;
	}
	public boolean isMV_Failed() {
		return isMV_Failed;
	}
	public void setMV_Failed(boolean isMV_Failed) {
		this.isMV_Failed = isMV_Failed;
	}
	public String getFontisBatchId() {
		return fontisBatchId;
	}
	public void setFontisBatchId(String fontisBatchId) {
		this.fontisBatchId = fontisBatchId;
	}
	public String getTransactionReference() {
		return transactionReference;
	}
	public void setTransactionReference(String transactionReference) {
		this.transactionReference = transactionReference;
	}
	public boolean isInSuffIndPostAllCrDrToSuspense() {
		return inSuffIndPostAllCrDrToSuspense;
	}
	public void setInSuffIndPostAllCrDrToSuspense(
			boolean inSuffIndPostAllCrDrToSuspense) {
		this.inSuffIndPostAllCrDrToSuspense = inSuffIndPostAllCrDrToSuspense;
	}
	
	
	
	
	
}
