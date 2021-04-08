package com.misys.ub.dc.types;

public class InterbankSOCRq 
{
	//control params
	/*String Ver, Operation,Service,txnReq,isPagingReq;
	String pageNo,pageSize,referral,referralCallBack,User,reqId,origReqId,overrides,channelId;
	*/
	
	//request params
	 String InterbankPaymentId;
	 String customerId;
	 String CustAcctType;
	 String CustAcctNo;
	 String Amt;
	 String Ccy;
	 String Freq;
	 String EndOfMonth;
	 String NumPayments;
	 String FrDt;
	 String ToDt;
	 String Time;
	 String HolidayTreatment;
	 String Retry;
	 String RetryCount;
	 String SuspendOnFailure;
	 String NumberOfFailures;
	 String Description;
	 String InstructionId;
	 String Setup;
	 String Success;
	 String Failure;
     String ChannelId;
     
   
	String ApplicationId;
     String UserId;
     String TxnReferenceID;
     String StandingOrderId;
     String AccountNumber;
     String msgType;
     
     
     public String getInterbankPaymentId() {
 		return InterbankPaymentId;
 	}
 	public void setInterbankPaymentId(String interbankPaymentId) {
 		InterbankPaymentId = interbankPaymentId;
 	}
     
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getCustAcctType() {
		return CustAcctType;
	}
	public void setCustAcctType(String custAcctType) {
		CustAcctType = custAcctType;
	}
	public String getCustAcctNo() {
		return CustAcctNo;
	}
	public void setCustAcctNo(String custAcctNo) {
		CustAcctNo = custAcctNo;
	}
	public String getAmt() {
		return Amt;
	}
	public void setAmt(String amt) {
		Amt = amt;
	}
	public String getCcy() {
		return Ccy;
	}
	public void setCcy(String ccy) {
		Ccy = ccy;
	}
	public String getFreq() {
		return Freq;
	}
	public void setFreq(String freq) {
		Freq = freq;
	}
	public String getEndOfMonth() {
		return EndOfMonth;
	}
	public void setEndOfMonth(String endOfMonth) {
		EndOfMonth = endOfMonth;
	}
	public String getNumPayments() {
		return NumPayments;
	}
	public void setNumPayments(String numPayments) {
		NumPayments = numPayments;
	}
	public String getFrDt() {
		return FrDt;
	}
	public void setFrDt(String frDt) {
		FrDt = frDt;
	}
	public String getToDt() {
		return ToDt;
	}
	public void setToDt(String toDt) {
		ToDt = toDt;
	}
	public String getTime() {
		return Time;
	}
	public void setTime(String time) {
		Time = time;
	}
	public String getHolidayTreatment() {
		return HolidayTreatment;
	}
	public void setHolidayTreatment(String holidayTreatment) {
		HolidayTreatment = holidayTreatment;
	}
	public String getRetry() {
		return Retry;
	}
	public void setRetry(String retry) {
		Retry = retry;
	}
	public String getRetryCount() {
		return RetryCount;
	}
	public void setRetryCount(String retryCount) {
		RetryCount = retryCount;
	}
	public String getSuspendOnFailure() {
		return SuspendOnFailure;
	}
	public void setSuspendOnFailure(String suspendOnFailure) {
		SuspendOnFailure = suspendOnFailure;
	}
	public String getNumberOfFailures() {
		return NumberOfFailures;
	}
	public void setNumberOfFailures(String numberOfFailures) {
		NumberOfFailures = numberOfFailures;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public String getInstructionId() {
		return InstructionId;
	}
	public void setInstructionId(String instructionId) {
		InstructionId = instructionId;
	}
	public String getSetup() {
		return Setup;
	}
	public void setSetup(String setup) {
		Setup = setup;
	}
	public String getSuccess() {
		return Success;
	}
	public void setSuccess(String success) {
		Success = success;
	}
	public String getFailure() {
		return Failure;
	}
	public void setFailure(String failure) {
		Failure = failure;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getChannelId() {
		return ChannelId;
	}
	public void setChannelId(String channelId) {
		this.ChannelId = ChannelId;
	}
	public String getApplicationId() {
		return ApplicationId;
	}
	public void setApplicationId(String applicationId) {
		ApplicationId = applicationId;
	}
	public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public String getTxnReferenceID() {
		return TxnReferenceID;
	}
	public void setTxnReferenceID(String txnReferenceID) {
		TxnReferenceID = txnReferenceID;
	}
	public String getStandingOrderId() {
		return StandingOrderId;
	}
	public void setStandingOrderId(String standingOrderId) {
		StandingOrderId = standingOrderId;
	}
	public String getAccountNumber() {
		return AccountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		AccountNumber = accountNumber;
	}
     
     

}
