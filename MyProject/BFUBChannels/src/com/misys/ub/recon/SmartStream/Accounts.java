package com.misys.ub.recon.SmartStream;

public class Accounts {

	private String CORONA_Acc; // CCORNUACC Corona Account
	private String CORONA_Acc_Name; // CCORNASHOR Corona Account Name
	private String ISOCurrencyCode; // MCURISO Currency ISO Code
	private String currency; // MCURNAME Currency Name
	private String currency_Frac; // MCURFRADIG Amount of Fractional Digits
	private String accountID; // CEXANUACC Physical Account Number
	private String accountName; // CEXANASHOR Name of the Account
	private String origin; // Origin
	private String initial_Stmt_No; // CEXANUISB Initial Statement Number
	private String initial_Stmt_Pg_No; // CEXANPISB Initial Statement Page
	// Number
	private String initial_Stmt_Bal_Date; // CEXADAISB Date of initial Statement
	// Balance
	private String initial_Stmt_Amount; // CEXAAMISB Amount of initial Statement
	// Balance
	private String initial_Stmt_Amount_Sign; // CEXASIISB Sign of initial
	// Statement Balance
	private String receiver_Addr; // RCVADDR Receiver Address
	private String reciever_Addr_Type; // RCVTYPE Receiver Address Type
	private String sender_Addr; // SNDADDR Sender Address
	private String sender_Addr_Type; // SNDTYPE Sender Address Type
	private String bankName; // MBNKBANK Bank Name

	public Accounts() {
		setCORONA_Acc_Name("");
		setCurrency("");
		setAccountName("");
		setCurrency_Frac("2");
		setInitial_Stmt_No_Initial_Stmt_Pg_No("", "");
		setInitial_Stmt_Bal_Date_Amount_Sign("", "", "");
		setReceiver_Addr("");
		setReciever_Addr_Type("");
		setBankName("");
	}

	public void setInitial_Stmt_No_Initial_Stmt_Pg_No(String initialStmtNo, String initialStmtPgNo) {
		this.initial_Stmt_No = initialStmtNo;
		this.initial_Stmt_Pg_No = initialStmtPgNo;
	}

	public void setInitial_Stmt_Bal_Date_Amount_Sign(String initialStmtBalDate, String initialStmtAmount, String initialStmtAmountSign) {
		this.initial_Stmt_Bal_Date = initialStmtBalDate;
		this.initial_Stmt_Amount_Sign = initialStmtAmountSign;
		this.initial_Stmt_Amount = initialStmtAmount;
	}

	public String getCORONA_Acc() {
		return CORONA_Acc;
	}

	public void setCORONA_Acc(String cORONAAcc) {
		CORONA_Acc = cORONAAcc;
	}

	public String getCORONA_Acc_Name() {
		return CORONA_Acc_Name;
	}

	public void setCORONA_Acc_Name(String cORONAAccName) {
		CORONA_Acc_Name = cORONAAccName;
	}

	public String getISOCurrencyCode() {
		return ISOCurrencyCode;
	}

	public void setISOCurrencyCode(String iSOCurrencyCode) {
		ISOCurrencyCode = iSOCurrencyCode;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCurrency_Frac() {
		return currency_Frac;
	}

	public void setCurrency_Frac(String currencyFrac) {
		currency_Frac = currencyFrac;
	}

	public String getAccountID() {
		return accountID;
	}

	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getInitial_Stmt_No() {
		return initial_Stmt_No;
	}

	public void setInitial_Stmt_No(String initialStmtNo) {
		initial_Stmt_No = initialStmtNo;
	}

	public String getInitial_Stmt_Pg_No() {
		return initial_Stmt_Pg_No;
	}

	public void setInitial_Stmt_Pg_No(String initialStmtPgNo) {
		initial_Stmt_Pg_No = initialStmtPgNo;
	}

	public String getInitial_Stmt_Bal_Date() {
		return initial_Stmt_Bal_Date;
	}

	public void setInitial_Stmt_Bal_Date(String initialStmtBalDate) {
		initial_Stmt_Bal_Date = initialStmtBalDate;
	}

	public String getInitial_Stmt_Amount() {
		return initial_Stmt_Amount;
	}

	public void setInitial_Stmt_Amount(String initialStmtAmount) {
		initial_Stmt_Amount = initialStmtAmount;
	}

	public String getInitial_Stmt_Amount_Sign() {
		return initial_Stmt_Amount_Sign;
	}

	public void setInitial_Stmt_Amount_Sign(String initialStmtAmountSign) {
		initial_Stmt_Amount_Sign = initialStmtAmountSign;
	}

	public String getReceiver_Addr() {
		return receiver_Addr;
	}

	public void setReceiver_Addr(String receiverAddr) {
		receiver_Addr = receiverAddr;
	}

	public String getReciever_Addr_Type() {
		return reciever_Addr_Type;
	}

	public void setReciever_Addr_Type(String recieverAddrType) {
		reciever_Addr_Type = recieverAddrType;
	}

	public String getSender_Addr() {
		return sender_Addr;
	}

	public void setSender_Addr(String senderAddr) {
		sender_Addr = senderAddr;
	}

	public String getSender_Addr_Type() {
		return sender_Addr_Type;
	}

	public void setSender_Addr_Type(String senderAddrType) {
		sender_Addr_Type = senderAddrType;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	@Override
	public String toString() {
		return "Accounts [CORONA_Acc=" + CORONA_Acc + ", CORONA_Acc_Name=" + CORONA_Acc_Name + ", ISOCurrencyCode=" + ISOCurrencyCode
				+ ", accountID=" + accountID + ", accountName=" + accountName + ", bankName=" + bankName + ", currency=" + currency
				+ ", currency_Frac=" + currency_Frac + ", initial_Stmt_Amount=" + initial_Stmt_Amount + ", initial_Stmt_Amount_Sign="
				+ initial_Stmt_Amount_Sign + ", initial_Stmt_Bal_Date=" + initial_Stmt_Bal_Date + ", initial_Stmt_No=" + initial_Stmt_No
				+ ", initial_Stmt_Pg_No=" + initial_Stmt_Pg_No + ", origin=" + origin + ", receiver_Addr=" + receiver_Addr
				+ ", reciever_Addr_Type=" + reciever_Addr_Type + ", sender_Addr=" + sender_Addr + ", sender_Addr_Type=" + sender_Addr_Type
				+ "]";
	}

}
